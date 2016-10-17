package org.lny.mailcatcher.mail.protocol.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.lny.mailcatcher.biz.Mail;
import org.lny.mailcatcher.mail.protocol.IStoreManager;
import org.lny.mailcatcher.mail.protocol.exception.Pop3ImapStoreManagerException;
import org.lny.mailcatcher.tecnical.Configuration;
import org.lny.mailcatcher.tecnical.constant.ConfigurationConstants;
import org.lny.mailcatcher.tecnical.exception.MailFilterException;
import org.lny.mailcatcher.tecnical.filter.Pop3ImapMailFilter;

import javax.mail.*;
import java.util.List;
import java.util.Properties;

/**
 * Classe de gestion de la récupération des mail par POP3 ou IMAP.
 *
 * @author NAGY Léventé
 * @since 1.0
 */
public class Pop3ImapStoreManager extends Pop3ImapMailFilter implements IStoreManager {

    private static final Logger LOGGER = Logger.getLogger(Pop3ImapStoreManager.class);

    private Store store;

    /**
     * @see {@link IStoreManager#init()}
     */
    public void init() throws Pop3ImapStoreManagerException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(">> Pop3ImapStoreManager:init()");
        }

        // Récupération de la configuration
        String protocol = Configuration.getInstance().getProtocol();
        Properties props = new Properties();
        props.setProperty(ConfigurationConstants.MAIL_STORE_PROTOCOL, protocol);
        props.setProperty(ConfigurationConstants.MAIL_IMAP_STARTTLS_ENABLE, Boolean.TRUE.toString());
        props.setProperty(ConfigurationConstants.MAIL_IMAP_SSL_ENABLE, Boolean.TRUE.toString());

        // On récupère la session
        Session session = Session.getInstance(props, null);
        session.setDebug(Configuration.getInstance().getBoolean(ConfigurationConstants.MAIL_DEBUG_ENABLE));

        // On récupère le store
        try {
            store = session.getStore(protocol);
        } catch (NoSuchProviderException e) {
            LOGGER.fatal("Erreur lors de l'initialisation du store : ", e);
            throw new Pop3ImapStoreManagerException(e);
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("<< Pop3ImapStoreManager:init()");
        }
    }

    /**
     * Ouvre la connexion vers le store.
     *
     * @throws Pop3ImapStoreManagerException
     */
    public void openConnection() throws Pop3ImapStoreManagerException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(">> Pop3ImapStoreManager:openConnection()");
        }

        if (store != null && !store.isConnected()) {
            String host = Configuration.getInstance().getString(ConfigurationConstants.MAIL_HOST);
            String user = Configuration.getInstance().getString(ConfigurationConstants.MAIL_USER);
            String password = Configuration.getInstance().getString(ConfigurationConstants.MAIL_PASSWORD);

            if (StringUtils.isNotBlank(host) && StringUtils.isNotBlank(user) && StringUtils.isNotBlank(password)) {
                LOGGER.info(String.format("Connexion au serveur : '%s'", host));
                LOGGER.info(String.format("Utilisateur : '%s'", user));
                LOGGER.info(String.format("Mot de passe : '%s'", password));
                try {
                    store.connect(host, user, password);
                } catch (MessagingException e) {
                    LOGGER.fatal("Erreur lors de la connexion au serveur : ", e);
                    throw new Pop3ImapStoreManagerException(e);
                }
            } else {
                Pop3ImapStoreManagerException exception =
                        new Pop3ImapStoreManagerException("Connexion impossible. Il est " +
                                "nécessaire de fournir le host, user et mot de passe du serveur mail.");
                LOGGER.fatal(exception.getMessage());
                throw exception;
            }
        } else {
            Pop3ImapStoreManagerException exception =
                    new Pop3ImapStoreManagerException("Le store n'a pas été initialisé.");
            LOGGER.fatal(exception.getMessage());
            throw exception;
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("<< Pop3ImapStoreManager:openConnection()");
        }
    }


    /**
     * Ferme la connexion vers le store.
     *
     * @throws Pop3ImapStoreManagerException
     */
    public void closeConnection() throws Pop3ImapStoreManagerException {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("<< Pop3ImapStoreManager:openConnection()");
        }

        if (store != null && store.isConnected()) {
            try {
                store.close();
            } catch (MessagingException e) {
                Pop3ImapStoreManagerException exception =
                        new Pop3ImapStoreManagerException("Erreur lors de la fermeture du store", e);
                LOGGER.fatal(exception.getMessage());
                throw exception;
            }
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("<< Pop3ImapStoreManager:openConnection()");
        }
    }

    /**
     * @see {@link IStoreManager#fetchMails()}
     */
    @Override
    public List<Mail> fetchMails() throws Pop3ImapStoreManagerException {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("<< Pop3ImapStoreManager:openConnection()");
        }

        List<Mail> messageList;

        if (store != null && store.isConnected()) {

            // On récupère le répèrtoire paramétré.
            Folder emailFolder;
            try {
                emailFolder = store.getFolder(ConfigurationConstants.MAIL_ROOT_DIRECTORY_NAME);
            } catch (MessagingException e) {
                LOGGER.error("Erreur lors de la récupération du rootDirectory : '" +
                        ConfigurationConstants.MAIL_ROOT_DIRECTORY_NAME + "'. " +
                        "Tentative de récupération du rootDirectory par défaut.");
                emailFolder = null;
            }
            if (emailFolder == null) {
                // On tente de récupérer le défault
                try {
                    emailFolder = store.getDefaultFolder();
                } catch (MessagingException e) {
                    LOGGER.fatal("Impossible de récupérer le rootDirectory", e);
                    throw new Pop3ImapStoreManagerException(e);
                }
            }

            // On ouvre la boite mail en lecture seule.
            try {
                emailFolder.open(Folder.READ_ONLY);
            } catch (MessagingException e) {
                LOGGER.fatal("Impossible d'ouvrir le rootDirectory", e);
                throw new Pop3ImapStoreManagerException(e);
            }

            // On récupère la liste des mails filtrée
            try {
                messageList = filterMessages(emailFolder);
            } catch (MailFilterException e) {
                LOGGER.fatal("Erreur lors de la récupération des mails", e);
                throw new Pop3ImapStoreManagerException(e);
            }
        } else { // Problème avec la connexion du store.
            Pop3ImapStoreManagerException exception = new Pop3ImapStoreManagerException("Le store n'est pas initialisé " +
                    "ou bien la connexion n'est pas ouverte.");
            LOGGER.error(exception);
            throw exception;
        }

        return messageList;
    }
}
