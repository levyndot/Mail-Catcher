package org.lny.mailcatcher.mail.service.exception;

import org.lny.mailcatcher.mail.exception.MailCatcherException;

/**
 * @author NAGY Léventé
 * @since 1.0
 */
public class RepositoryServiceException extends MailCatcherException {
    public RepositoryServiceException(Throwable e) {
        super(e);
    }

    public RepositoryServiceException(String message) {
        super(message);
    }
}
