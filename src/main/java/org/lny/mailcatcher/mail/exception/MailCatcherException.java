package org.lny.mailcatcher.mail.exception;

/**
 * @author NAGY Léventé
 * @since 1.0
 */
public class MailCatcherException extends Exception {
    public MailCatcherException(Throwable e) {
        super(e);
    }

    public MailCatcherException(String message) {
        super(message);
    }

    public MailCatcherException(String message, Throwable e) {
        super(message, e);
    }
}
