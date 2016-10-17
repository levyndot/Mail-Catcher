package org.lny.mailcatcher.util;

import com.sun.mail.util.BASE64DecoderStream;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.property.complex.AttachmentCollection;
import microsoft.exchange.webservices.data.property.complex.FileAttachment;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.lny.mailcatcher.biz.Attachment;
import org.lny.mailcatcher.biz.Mail;
import org.lny.mailcatcher.mail.service.constant.ContentTypeConstants;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import java.io.IOException;

/**
 * @author NAGY Léventé
 * @since 1.0
 */
public class MailUtils {

    private static final Logger LOGGER = Logger.getLogger(MailUtils.class);

    /**
     * Unique instance de l'utilitaire.
     */
    private static MailUtils INSTANCE;

    private static final Integer INTEGER_ZERO = 0;

    /**
     * Le construteur privé.
     */
    private MailUtils() {
    }

    /**
     * Renvoi l'unique instance de l'utilitaire. S'il n'existe pas il l'a créé.
     *
     * @return L'instance.
     */
    public static MailUtils getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MailUtils();
        }
        return INSTANCE;
    }

    /**
     * Converti un message des protocoles IMAP et POP3 en objet {@link Mail}.
     *
     * @param message le message a convertir
     * @return Le message converti en objet {@link Mail}.
     */
    @SuppressWarnings("unchecked")
    public Mail convertPop3ImapMessageToMail(Message message) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(">> MailUtils::convertPop3ImapMessageToMail()");
        }
        Mail<Message> result = null;
        if (message != null) {
            result = new Mail<>();
            result.setMessage(message);
            try {
                // On récupère l'expéditeur du mail
                if (message.getFrom() != null && message.getFrom().length > 0 && message.getFrom()[0] != null) {
                    result.setFrom(((InternetAddress) message.getFrom()[0]).getAddress());
                }

                // On récupère l'objet du mail.
                result.setSubject(message.getSubject());

                // On récupère le contenu du mail
                result.setBody(getBodyContent(message));

                // On récupère les pièces jointes
                if (message.getContent() instanceof Multipart) {
                    Multipart multipart = (Multipart) message.getContent();
                    // On parcours les différentes parties du mail.
                    for (Integer i = INTEGER_ZERO; i < multipart.getCount(); i++) {
                        BodyPart bodyPart = multipart.getBodyPart(i);
                        if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition()) &&
                                StringUtils.isNotBlank(bodyPart.getFileName())) {
                            // On récupère la pièce jointe du mail.
                            result.getPiecesJointes().add(new Attachment(bodyPart));
                        }
                    }
                }
            } catch (MessagingException | IOException e) {
                LOGGER.fatal("Une erreur est survenue lors de la décomposition du mail.", e);
            }
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("<< MailUtils::convertPop3ImapMessageToMail()");
        }
        return result;
    }

    /**
     * Converti un message du protocole EWS en objet {@link Mail}.
     *
     * @param email le message a convertir
     * @return Le message converti en objet {@link Mail}.
     */
    @SuppressWarnings("unchecked")
    public Mail convertEwsEmailMessageToMail(EmailMessage email) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(">> MailUtils::convertEwsEmailMessageToMail()");
        }
        Mail<EmailMessage> result = null;
        if (email != null) {
            result = new Mail<>();
            result.setMessage(email);
            try {
                result.setFrom(email.getFrom().getAddress());
                result.setSubject(email.getSubject());
                result.setBody(email.getBody().toString());
                if (email.getHasAttachments()) {
                    AttachmentCollection attachmentsCol = email.getAttachments();
                    for (int i = INTEGER_ZERO; i < attachmentsCol.getCount(); i++) {
                        FileAttachment attachment = (FileAttachment) attachmentsCol.getPropertyAtIndex(i);
                        result.getPiecesJointes().add(new Attachment(attachment));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("<< MailUtils::convertEwsEmailMessageToMail()");
        }
        return result;
    }

    /**
     * Parcours récursivement la partie du mail fournie pour renvoyer le contenu du corps du mail.
     *
     * @param part l'élément du message a parcourir
     * @return Le body du message, sinon <code>null</code>.
     */
    private String getBodyContent(Part part) throws MessagingException, IOException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(">> MailServiceImpl::getBodyContent()");
        }

        String bodyContent = null;

        if (part.isMimeType(ContentTypeConstants.TEXT_ALL) &&
                !(part.getContent() instanceof BASE64DecoderStream)) { // On ne traite pas les images embarquée dans les mails.
            bodyContent = (String) part.getContent();
        } else if (part.isMimeType(ContentTypeConstants.MULTIPART_ALTERNATIVE)) {
            Multipart mp = (Multipart) part.getContent();
            for (Integer i = 0; i < mp.getCount(); i++) {
                Part bp = mp.getBodyPart(i);
                if (bp.isMimeType(ContentTypeConstants.TEXT_PLAIN)) {// Traitement des contenus texte
                    if (StringUtils.isBlank(bodyContent)) {
                        bodyContent = getBodyContent(bp);
                    }
                } else if (bp.isMimeType(ContentTypeConstants.TEXT_HTML)) { // Traitement des contenus HTML
                    String s = getBodyContent(bp);
                    if (StringUtils.isNotBlank(s)) {
                        bodyContent = s;
                    }
                } else {
                    bodyContent = getBodyContent(bp);
                }
            }
        } else if (part.isMimeType(ContentTypeConstants.MULTIPART_ALL)) {
            Multipart mp = (Multipart) part.getContent();
            for (Integer i = 0; i < mp.getCount(); i++) {
                String s = getBodyContent(mp.getBodyPart(i));
                if (StringUtils.isNotBlank(s)) {
                    bodyContent = s;
                }
            }
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("<< MailServiceImpl::getBodyContent()");
        }
        return bodyContent;
    }
}
