package org.lny.mailcatcher.mail.service;

import org.lny.mailcatcher.biz.Mail;

/**
 * Contrat d'interface pour le service de mail.
 *
 * @author NAGY Léventé
 * @since 1.0
 */
public interface IMailService {

    /**
     * Traite un mail.
     *
     * @param mail le mail à traiter.
     */
    void processMail(Mail mail);

    /**
     * Ecrit le fichier de rapport JSON pour chaque expéditeurs trouvé lors d'un traitement.
     */
    void writeJsonReport();
}
