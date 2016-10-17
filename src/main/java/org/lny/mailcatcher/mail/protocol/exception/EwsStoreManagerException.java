package org.lny.mailcatcher.mail.protocol.exception;

/**
 * @author NAGY Léventé
 * @since 1.0
 */
public class EwsStoreManagerException extends StoreManagerException {
    public EwsStoreManagerException(Throwable e) {
        super(e);
    }

    public EwsStoreManagerException(String message) {
        super(message);
    }

    public EwsStoreManagerException(String message, Throwable e) {
        super(message, e);
    }
}
