package org.lny.mailcatcher.mail.protocol.exception;

import org.lny.mailcatcher.mail.exception.MailCatcherException;

/**
 * @author NAGY Léventé
 * @since 1.0
 */
public class StoreManagerException extends MailCatcherException {
    public StoreManagerException(Throwable e) {
        super(e);
    }

    public StoreManagerException(String message) {
        super(message);
    }

    public StoreManagerException(String message, Throwable e) {
        super(message, e);
    }
}
