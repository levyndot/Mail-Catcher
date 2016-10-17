package org.lny.mailcatcher.tecnical.filter;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.lny.mailcatcher.tecnical.Configuration;
import org.lny.mailcatcher.tecnical.constant.ConfigurationConstants;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.search.SearchTerm;
import java.io.IOException;

/**
 * Surcharge du {@link SearchTerm} pour appliquer les filtres de l'application.
 *
 * @author NAGY Léventé
 * @since 1.0
 */
class Pop3ImapMailMatcher extends SearchTerm {

    private static final Logger LOGGER = Logger.getLogger(Pop3ImapMailMatcher.class);

    /**
     * Définition du filtre pour les messages.
     *
     * @param message le message à évaluer
     * @return <code>true</code> si le message réponds aux critères.
     */
    @Override
    public boolean match(Message message) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(">> Pop3ImapMailMatcher::match()");
        }

        Boolean isValid = Boolean.FALSE;
        if (message != null) {
            String from = Configuration.getInstance().getString(ConfigurationConstants.MAIL_FILTER_FROM);
            String subjectTerm = Configuration.getInstance().getString(ConfigurationConstants.MAIL_FILTER_SUBJECT);
            String filterHasPJ = Configuration.getInstance().getString(ConfigurationConstants.MAIL_FILTER_HAS_PJ);

            try {
                // Vérification du filtre sur le mail de l'expéditeur.
                if (StringUtils.isNotBlank(from) && message.getFrom() != null && message.getFrom()[0] != null &&
                        from.equals(((InternetAddress) message.getFrom()[0]).getAddress())) {
                    isValid = Boolean.TRUE;
                }
                // Si le message est valide et que l'un des autres filtres est renseigné, alors on vérifie les autres filtres
                if (isValid && (StringUtils.isNotBlank(subjectTerm) || StringUtils.isNotBlank(filterHasPJ))) {
                    // Vérification du filtre objet du mail
                    if (StringUtils.isNotBlank(subjectTerm) && !message.getSubject().contains(subjectTerm)) {
                        isValid = Boolean.FALSE;
                    }
                    // Vérification du filtre sur la présence de pièce jointe.
                    if (isValid && StringUtils.isNotBlank(filterHasPJ)) {
                        try {
                            Boolean hasPJ = BooleanUtils.toBoolean(filterHasPJ,
                                    Boolean.TRUE.toString(), Boolean.FALSE.toString());
                            Boolean foundPJ = Boolean.FALSE;
                            // On récupère les pièces jointes
                            if (message.getContent() instanceof Multipart) {
                                Multipart multipart = (Multipart) message.getContent();
                                // On parcours les différentes parties du mail.
                                Integer i = 0;
                                while (Boolean.FALSE.equals(foundPJ) && i < multipart.getCount()) {
                                    BodyPart bodyPart = multipart.getBodyPart(i);
                                    if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition()) &&
                                            StringUtils.isNotBlank(bodyPart.getFileName())) {
                                        // On a trouvé une pièce jointe.
                                        foundPJ = Boolean.TRUE;
                                    } else {
                                        i++;
                                    }
                                }
                                isValid = foundPJ.equals(hasPJ);
                            }
                        } catch (IllegalArgumentException e) {
                            LOGGER.error(String.format("Le filtre pièce jointe a une valeur non authorisée - " +
                                    "actuelle : '%s' - attendu : '%s' ou '%s'", filterHasPJ, Boolean.TRUE, Boolean.FALSE));
                        }
                    }
                }
            } catch (MessagingException | IOException e) {
                isValid = Boolean.FALSE;
            }
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("<< Pop3ImapMailMatcher::match()");
        }

        return isValid;
    }
}
