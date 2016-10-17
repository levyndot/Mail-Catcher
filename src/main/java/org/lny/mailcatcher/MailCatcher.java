package org.lny.mailcatcher;

import org.apache.log4j.Logger;
import org.lny.mailcatcher.biz.Mail;
import org.lny.mailcatcher.mail.exception.MailCatcherException;
import org.lny.mailcatcher.mail.protocol.IStoreManager;
import org.lny.mailcatcher.mail.protocol.impl.EwsStoreManager;
import org.lny.mailcatcher.mail.protocol.impl.Pop3ImapStoreManager;
import org.lny.mailcatcher.mail.service.IMailService;
import org.lny.mailcatcher.mail.service.impl.MailServiceImpl;
import org.lny.mailcatcher.mail.service.impl.RepositoryServiceImpl;
import org.lny.mailcatcher.tecnical.Configuration;
import org.lny.mailcatcher.tecnical.constant.ConfigurationConstants;

import java.util.List;

/**
 * @author NAGY Léventé
 * @since 1.0
 */
class MailCatcher {

    private static final Logger LOGGER = Logger.getLogger(MailCatcher.class);

    public static void main(String... argv) throws MailCatcherException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(">> MailCatcher:main()");
        }

        LOGGER.info(">> Démarrage de Mail Catcher <<");

        LOGGER.info(">> Vérification de la configuration <<");
        // On vérifie que la configuration minimale requise est présente.
        Configuration.getInstance().checkConfiguration();

        LOGGER.info(">> Détermination du protocole <<");

        String protocol = Configuration.getInstance().getProtocol();
        LOGGER.info(String.format("Protocole utilisé : '%s'", protocol));

        // On choisi le storeManager selon le paramétrage.
        IStoreManager storeManager;
        switch (protocol) {
            case ConfigurationConstants.MAIL_STORE_PROTOCOL_IMAP:
            case ConfigurationConstants.MAIL_STORE_PROTOCOL_POP3:
                storeManager = new Pop3ImapStoreManager();
                break;
            case ConfigurationConstants.MAIL_STORE_PROTOCOL_MICROSOFT_EWS:
                storeManager = new EwsStoreManager();
                break;
            default:
                storeManager = null;
                break;
        }

        if (storeManager != null) {
            LOGGER.info(">> Initialisation du store <<");
            // On initialise le store
            storeManager.init();

            // Si pop3 ou imap alors besoin d'ouvrir la connexion.
            if (storeManager instanceof Pop3ImapStoreManager) {
                LOGGER.info(">> Connexion au store <<");
                ((Pop3ImapStoreManager) storeManager).openConnection();
            }

            LOGGER.info(">> Chargement des mails <<");
            List<Mail> mails = storeManager.fetchMails();

            LOGGER.info(">> Initialisation du dossier de sauvegarde des mails <<");
            // On initialise le service de d'écriture des fichiers ainsi que le dossier de sortie des mails.
            RepositoryServiceImpl.getInstance().initializeOutputDir();

            if (mails != null && !mails.isEmpty()) {
                Integer countMessageTotal = mails.size();
                Integer processingCounter = 1;
                LOGGER.info(String.format("Il y a %d à traiter", countMessageTotal));

                // On initialise le service de traitement des mails
                IMailService mailService = new MailServiceImpl();

                // On parcours les mails et les traites un par un.
                for (Mail mail : mails) {
                    LOGGER.info(String.format("------- Traitement %d sur %d -------",
                            processingCounter++, countMessageTotal));
                    // On appelle le service de traitement de mail.
                    mailService.processMail(mail);
                }

                LOGGER.info(">> Création des rapports JSON <<");
                // On écrit les rapports JSON de chaque expéditeurs.
                mailService.writeJsonReport();
            } else {
                LOGGER.info("Aucun mails ne correspond a votre recherche ou bien la boite mail est vide.");
            }

            // Si pop3 ou imap alors besoin de refermer la connexion
            if (storeManager instanceof Pop3ImapStoreManager) {
                LOGGER.info(">> Connexion au store <<");
                // On ouvre la connexion vers la boite mail
                ((Pop3ImapStoreManager) storeManager).closeConnection();
            }
        } else {
            LOGGER.error(String.format("Protocol '%s' non reconnu. Arrêt du programme.", protocol));
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("<< MailCatcher:main()");
        }
    }
}
