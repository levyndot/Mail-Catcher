package org.lny.mailcatcher.tecnical.constant;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Constantes de paramétrage de l'application.
 *
 * @author NAGY Léventé
 * @since 1.0
 */
public interface ConfigurationConstants {
    /**
     * Paramétrage du protocole
     */
    String MAIL_STORE_PROTOCOL = "mail.store.protocol";
    String MAIL_STORE_PROTOCOL_IMAP = "imap";
    String MAIL_STORE_PROTOCOL_POP3 = "pop3";
    String MAIL_STORE_PROTOCOL_MICROSOFT_EWS = "ews";
    String MAIL_STORE_PROTOCOL_DEFAULT_VALUE = MAIL_STORE_PROTOCOL_IMAP;
    List<String> HANDLED_PROTOTOLS = Collections.unmodifiableList(Arrays.asList(MAIL_STORE_PROTOCOL_IMAP,
            MAIL_STORE_PROTOCOL_POP3, MAIL_STORE_PROTOCOL_MICROSOFT_EWS));

    /**
     * Paramétrage du serveur et des credentials
     */
    String MAIL_HOST = "mail.host";
    String MAIL_USER = "mail.user";
    String MAIL_PASSWORD = "mail.password";
    String MAIL_EWS_DOMAIN = "mail.ews.domain";
    String MAIL_EWS_EMAIL = "mail.ews.email";
    String MAIL_ROOT_DIRECTORY_NAME = "INBOX";
    String HTTPS_PREFIX = "https://";
    String MAIL_IMAP_STARTTLS_ENABLE = "mail.imap.starttls.enable";
    String MAIL_IMAP_SSL_ENABLE = "mail.imap.ssl.enable";


    /**
     * Paramétrage de l'application.
     */
    String MAIL_DEBUG_ENABLE = "mail.debug.enable";
    String MAIL_OUTPUT_PATH = "mail.output.path";
    String MAIL_OUTPUT_PATH_DEFAULT = "output";

    String MAIL_FILTER_FROM = "mail.filter.from";
    String MAIL_FILTER_HAS_PJ = "mail.filter.has.pj";
    String MAIL_FILTER_SUBJECT = "mail.filter.subject";
}
