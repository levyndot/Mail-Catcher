package org.lny.mailcatcher.mail.service.impl;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.lny.mailcatcher.biz.Attachment;
import org.lny.mailcatcher.biz.Mail;
import org.lny.mailcatcher.mail.service.IMailService;
import org.lny.mailcatcher.mail.service.exception.RepositoryServiceException;

import java.util.HashMap;
import java.util.Map;

/**
 * @author NAGY Léventé
 * @since 1.0
 */
public class MailServiceImpl implements IMailService {

    private static final Logger LOGGER = Logger.getLogger(MailServiceImpl.class);

    /**
     * Clés JSON pour le fichier résumé json.
     */
    private static final String JSON_KEY_ID = "id";
    private static final String JSON_KEY_SUBJECT = "subject";

    /**
     * Map contenant les compteur d'index pour chaque expéditeurs trouvé par l'application.
     */
    private Map<String, Long> mapIndexByExpeditors;

    /**
     * Map contenant les compteur d'index pour chaque expéditeurs trouvé par l'application.
     */
    private Map<String, JSONArray> mapJsonByExpeditors;

    public MailServiceImpl() {
        mapIndexByExpeditors = new HashMap<>();
        mapJsonByExpeditors = new HashMap<>();
    }

    /**
     * @see {@link IMailService#processMail(Mail)}
     */
    @Override
    @SuppressWarnings("unchecked")
    public void processMail(Mail mail) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(">> MailServiceImpl::processMail()");
        }

        if (mail != null) {
            // On récupère l'index courant pour l'expéditeur courant (on le créé s'il l'expéditeur est nouveau).
            if (mapIndexByExpeditors.containsKey(mail.getFrom())) {
                Long currentIndex = mapIndexByExpeditors.get(mail.getFrom());
                mapIndexByExpeditors.put(mail.getFrom(), ++currentIndex);
            } else {
                mapIndexByExpeditors.put(mail.getFrom(), 1L);
            }
            Long indexToFiles = mapIndexByExpeditors.get(mail.getFrom());

            // On ajoute le nouvel élement dans le tableau JSON
            JSONArray currentJsonArray;
            if (mapJsonByExpeditors.containsKey(mail.getFrom())) {
                currentJsonArray = mapJsonByExpeditors.get(mail.getFrom());
            } else {
                currentJsonArray = new JSONArray();
                mapJsonByExpeditors.put(mail.getFrom(), currentJsonArray);
            }
            // On créé le nouvel objet JSON pour le traitement courant
            JSONObject currentJsonMail = new JSONObject();
            currentJsonMail.put(JSON_KEY_SUBJECT, mail.getSubject());
            currentJsonMail.put(JSON_KEY_ID, indexToFiles);
            currentJsonArray.add(currentJsonMail);

            // Création du fichier EML
            try {
                RepositoryServiceImpl.getInstance().writeEml(mail.getFrom(), indexToFiles, mail);
            } catch (RepositoryServiceException e) {
                LOGGER.fatal("Une erreur est survenue lors de l'écriture du fichier EML.", e);
            }
            // Création du fichier txt contenant le sujet et le contenu du mail.
            try {
                RepositoryServiceImpl.getInstance().writeTxtFile(mail.getFrom(), indexToFiles, mail.getSubject(), mail.getBody());
            } catch (RepositoryServiceException e) {
                LOGGER.fatal("Une erreur est survenue lors de l'écriture du fichier TXT.", e);
            }
            // Création des fichiers PJ
            mail.getPiecesJointes().forEach(message -> {
                try {
                    RepositoryServiceImpl.getInstance().writeAttachment(mail.getFrom(), indexToFiles,
                            (Attachment) message);
                } catch (RepositoryServiceException e) {
                    LOGGER.fatal("Une erreur est survenue lors de l'écriture de la pièce jointe.", e);
                }
            });
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("<< MailServiceImpl::processMail()");
        }
    }

    /**
     * @see {@link IMailService#writeJsonReport()}
     */
    @Override
    @SuppressWarnings("unchecked")
    public void writeJsonReport() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(">> MailServiceImpl::writeJsonReport()");
        }
        mapJsonByExpeditors.forEach((email, json) -> {
            // Création du fichier json contenant le sujet et le contenu du mail.
            try {
                RepositoryServiceImpl.getInstance().writeJsonFile(email, json);
            } catch (RepositoryServiceException e) {
                LOGGER.fatal("Une erreur est survenue lors de l'écriture du fichier TXT.", e);
            }
        });
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("<< MailServiceImpl::writeJsonReport()");
        }
    }
}
