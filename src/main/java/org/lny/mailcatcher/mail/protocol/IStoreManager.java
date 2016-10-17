package org.lny.mailcatcher.mail.protocol;

import org.lny.mailcatcher.biz.Mail;
import org.lny.mailcatcher.mail.protocol.exception.StoreManagerException;

import java.util.List;

/**
 * Contrat d'interface pour les Stores manager.
 *
 * @author NAGY Léventé
 * @since 1.0
 */
public interface IStoreManager {

    /**
     * Initialise le manager en chargeant la configuration requise et en initalisant le store.
     *
     * @throws StoreManagerException
     */
    void init() throws StoreManagerException;

    /**
     * Parcours les mails pour les traiter.
     *
     * @throws StoreManagerException
     */
    List<Mail> fetchMails() throws StoreManagerException;
}
