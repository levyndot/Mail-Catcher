package org.lny.mailcatcher.mail.protocol.exception;

/**
 * @author NAGY Léventé
 * @since 1.0
 */
public class Pop3ImapStoreManagerException extends StoreManagerException {
    public Pop3ImapStoreManagerException(Throwable e) {
        super(e);
    }

    public Pop3ImapStoreManagerException(String message) {
        super(message);
    }

    public Pop3ImapStoreManagerException(String message, Throwable e) {
        super(message, e);
    }
}
