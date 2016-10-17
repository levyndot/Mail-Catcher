package org.lny.mailcatcher.tecnical.exception;

import org.lny.mailcatcher.mail.exception.MailCatcherException;

/**
 * @author NAGY Léventé
 * @since 1.0
 */
public class ConfigurationException extends MailCatcherException {
    public ConfigurationException(String message) {
        super(message);
    }
}
