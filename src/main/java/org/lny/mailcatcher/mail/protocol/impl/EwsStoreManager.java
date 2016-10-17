package org.lny.mailcatcher.mail.protocol.impl;

import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.PropertySet;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;
import microsoft.exchange.webservices.data.core.enumeration.property.BasePropertySet;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.enumeration.search.LogicalOperator;
import microsoft.exchange.webservices.data.core.enumeration.search.OffsetBasePoint;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.core.service.item.Item;
import microsoft.exchange.webservices.data.core.service.schema.EmailMessageSchema;
import microsoft.exchange.webservices.data.core.service.schema.ItemSchema;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.search.FindItemsResults;
import microsoft.exchange.webservices.data.search.ItemView;
import microsoft.exchange.webservices.data.search.filter.SearchFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.lny.mailcatcher.biz.Mail;
import org.lny.mailcatcher.mail.protocol.IStoreManager;
import org.lny.mailcatcher.mail.protocol.exception.EwsStoreManagerException;
import org.lny.mailcatcher.tecnical.Configuration;
import org.lny.mailcatcher.tecnical.constant.ConfigurationConstants;
import org.lny.mailcatcher.util.MailUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe de gestion de la récupération des mail par Exchange EWS.
 *
 * @author NAGY Léventé
 * @since 1.0
 */
public class EwsStoreManager implements IStoreManager {

    private static final Logger LOGGER = Logger.getLogger(EwsStoreManager.class);

    private static final Integer PAGE_SIZE = 100;
    private static final Integer VIEW_OFFSET = 0;
    private static final String STRING_QUOTE = "'";
    private static final String STRING_LIBELLE_AUCUN = "aucun";

    private ExchangeService service;

    /**
     * @see {@link IStoreManager#init()}
     */
    public void init() throws EwsStoreManagerException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(">> EwsStoreManager:init()");
        }

        if (service == null) {
            String host = Configuration.getInstance().getString(ConfigurationConstants.MAIL_HOST);
            String user = Configuration.getInstance().getString(ConfigurationConstants.MAIL_USER);
            String password = Configuration.getInstance().getString(ConfigurationConstants.MAIL_PASSWORD);
            String domain = Configuration.getInstance().getString(ConfigurationConstants.MAIL_EWS_DOMAIN);

            LOGGER.info(String.format("Connexion au serveur : '%s'", host));
            LOGGER.info(String.format("Utilisateur : '%s'", user));
            LOGGER.info(String.format("Mot de passe : '%s'", password));
            LOGGER.info(String.format("Domaine : '%s'", domain));

            // Paramétrage du service EWS
            service = new ExchangeService(ExchangeVersion.Exchange2010_SP2);
            service.setCredentials(new WebCredentials(user, password, domain));
            service.setTraceEnabled(Configuration.getInstance().getBoolean(ConfigurationConstants.MAIL_DEBUG_ENABLE));

            try {
                service.setUrl(new URI(host));
            } catch (URISyntaxException e) {
                LOGGER.fatal(e);
                throw new EwsStoreManagerException(e);
            }
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("<< EwsStoreManager:init()");
        }
    }

    /**
     * @see {@link IStoreManager#fetchMails()}
     */
    @Override
    public List<Mail> fetchMails() throws EwsStoreManagerException {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(">> EwsStoreManager:fetchMails()");
        }

        List<Mail> messageList = new ArrayList<>();

        if (service != null) {
            // On parcours la boite mail
            Boolean hasMoreMails = Boolean.TRUE;

            // On initialise la vue
            ItemView view = new ItemView(PAGE_SIZE, VIEW_OFFSET, OffsetBasePoint.Beginning);
            view.setPropertySet(new PropertySet(BasePropertySet.IdOnly, EmailMessageSchema.From,
                    ItemSchema.Subject, ItemSchema.HasAttachments));

            FindItemsResults<Item> findResults;
            while (hasMoreMails) {
                try {
                    // On lance la recherche de mail en récupérant les filtres adéquats.
                    findResults = service.findItems(WellKnownFolderName.Inbox, getFilters(), view);

                    // Si on a un résultat alors on le converti pour traitement.
                    if (findResults.getItems() != null && !findResults.getItems().isEmpty()) {
                        service.loadPropertiesForItems(findResults, new PropertySet(BasePropertySet.FirstClassProperties,
                                EmailMessageSchema.Attachments, EmailMessageSchema.MimeContent));
                        // On converti les éléments en objet Mail
                        findResults.getItems().forEach(email -> messageList.add(MailUtils.getInstance().
                                convertEwsEmailMessageToMail((EmailMessage) email)));
                    }

                    // Si nous avons encore des mail, alors on récupère la page suivante.
                    hasMoreMails = findResults.isMoreAvailable();
                    if (hasMoreMails) {
                        view.setOffset(view.getOffset() + PAGE_SIZE);
                    }
                } catch (Exception e) {
                    EwsStoreManagerException exception =
                            new EwsStoreManagerException("Erreur lors de la récupération des mails.", e);
                    LOGGER.error(exception);
                    throw exception;
                }
            }
        } else {
            EwsStoreManagerException exception = new EwsStoreManagerException("Le store n'est pas initialisé " +
                    "ou bien la connexion n'est pas ouverte.");
            LOGGER.error(exception);
            throw exception;
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("<< EwsStoreManager:fetchMails()");
        }

        return messageList;
    }

    /**
     * Construit le(s) filtre(s) à appliquer.
     *
     * @return L'agrégat de filtres.
     */
    private SearchFilter getFilters() {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(">> EwsStoreManager:getFilters()");
        }

        // Récupération des paramétrages
        String from = Configuration.getInstance().getString(ConfigurationConstants.MAIL_FILTER_FROM);
        String subjectTerm = Configuration.getInstance().getString(ConfigurationConstants.MAIL_FILTER_SUBJECT);
        String filterHasPJ = Configuration.getInstance().getString(ConfigurationConstants.MAIL_FILTER_HAS_PJ);
        Boolean hasPJ = (StringUtils.isNotBlank(filterHasPJ) ?
                Configuration.getInstance().getBoolean(ConfigurationConstants.MAIL_FILTER_HAS_PJ) : null);

        // Construction des filtres.
        SearchFilter.IsEqualTo filterFrom = new SearchFilter.IsEqualTo(EmailMessageSchema.From, from);
        SearchFilter.ContainsSubstring filterSubject = new SearchFilter.ContainsSubstring(ItemSchema.Subject, subjectTerm);
        SearchFilter.IsEqualTo filterPJ = new SearchFilter.IsEqualTo(ItemSchema.HasAttachments, hasPJ);

        SearchFilter.SearchFilterCollection searchFilter =
                new SearchFilter.SearchFilterCollection(LogicalOperator.And, filterFrom);

        // On ajoute le filtre sur l'objet s'il y en a un.
        if (StringUtils.isNotBlank(subjectTerm)) {
            searchFilter.add(filterSubject);
        }
        // On ajoute le filtre sur les PJ s'il y en a un.
        if (hasPJ != null) {
            searchFilter.add(filterPJ);
        }

        if (StringUtils.isNotBlank(subjectTerm)) {
            subjectTerm = STRING_QUOTE + subjectTerm + STRING_QUOTE;
        } else {
            subjectTerm = STRING_LIBELLE_AUCUN;
        }
        if (StringUtils.isNotBlank(filterHasPJ)) {
            filterHasPJ = STRING_QUOTE + filterHasPJ + STRING_QUOTE;
        } else {
            filterHasPJ = STRING_LIBELLE_AUCUN;
        }

        LOGGER.info(">> Application des filtres <<");
        LOGGER.info(String.format("Filtres : from='%s', subjectTerm=%s, filterHasPJ=%s", from, subjectTerm, filterHasPJ));


        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("<< EwsStoreManager:getFilters()");
        }

        return searchFilter;
    }
}
