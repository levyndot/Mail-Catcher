package org.lny.mailcatcher.tecnical.filter;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.lny.mailcatcher.biz.Mail;
import org.lny.mailcatcher.tecnical.Configuration;
import org.lny.mailcatcher.tecnical.constant.ConfigurationConstants;
import org.lny.mailcatcher.tecnical.exception.MailFilterException;
import org.lny.mailcatcher.util.MailUtils;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Filtre pour les mails IMAP et POP3.
 *
 * @author NAGY Léventé
 * @since 1.0
 */
public class Pop3ImapMailFilter {

    private static final Logger LOGGER = Logger.getLogger(Pop3ImapMailFilter.class);
    private static final String STRING_QUOTE = "'";
    private static final String STRING_LIBELLE_AUCUN = "aucun";

    /**
     * Filtre les messages présent dans un dossier.
     *
     * @param folder le dossier contenant les mails à filtrer
     * @return
     */
    protected List<Mail> filterMessages(Folder folder) throws MailFilterException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(">> Pop3ImapMailFilter::filterMessages");
        }

        List<Mail> foundMessages = new ArrayList<>();
        if (folder != null) {
            Message[] messages;
            String from = Configuration.getInstance().getString(ConfigurationConstants.MAIL_FILTER_FROM);
            String subjectTerm = Configuration.getInstance().getString(ConfigurationConstants.MAIL_FILTER_SUBJECT);
            String filterHasPJ = Configuration.getInstance().getString(ConfigurationConstants.MAIL_FILTER_HAS_PJ);

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
            LOGGER.info(String.format("Filtres : from='%s', subjectTerm=%s, filterHasPJ=%s",
                    from, subjectTerm, filterHasPJ));

            try {
                // Lancement de la recherche sur le folder.
                messages = folder.search(new Pop3ImapMailMatcher());
            } catch (MessagingException e) {
                LOGGER.fatal("Une erreur s'est produite durant l'application des filtres sur les messages.");
                throw new MailFilterException(e);
            }
            if (messages != null && messages.length > 0) {
                // Si on a des résultat alors on les converti et on les retourne.
                Arrays.stream(messages).filter(message -> message != null).forEach(message ->
                        foundMessages.add(MailUtils.getInstance().convertPop3ImapMessageToMail(message)));
            }
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("<< Pop3ImapMailFilter::filterMessages");
        }

        return foundMessages;
    }
}
