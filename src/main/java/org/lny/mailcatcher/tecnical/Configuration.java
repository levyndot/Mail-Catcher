package org.lny.mailcatcher.tecnical;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.lny.mailcatcher.tecnical.constant.ConfigurationConstants;
import org.lny.mailcatcher.tecnical.exception.ConfigurationException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author NAGY Léventé
 * @since 1.0
 */
public class Configuration {

    private static final Logger LOGGER = Logger.getLogger(Configuration.class);
    private static final String CONFIGURATION_FILENAME = "application.properties";

    /**
     * Instance unique
     */
    private static Configuration INSTANCE;

    /**
     * Les properties
     */
    private Properties properties;

    /**
     * Constructeur privé
     */
    private Configuration() {
        load();
    }

    /**
     * Point d'accès pour l'instance unique du singleton
     */
    public static synchronized Configuration getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Configuration();
        }
        return INSTANCE;
    }

    /**
     * Charge le fichier application.properties en mémoire.
     */
    private void load() {
        properties = new Properties();
        ClassLoader loader = getClass().getClassLoader();
        InputStream configFileStream = loader.getResourceAsStream(CONFIGURATION_FILENAME);
        try {
            properties.load(configFileStream);
        } catch (IOException e) {
            LOGGER.error("Une erreur est survenue lors du chargement de la configuration de l'application");
        }
    }

    /**
     * Vérification de la configuration de l'application.
     */
    public void checkConfiguration() throws ConfigurationException {
        // Check mode debug
        if (getBoolean(ConfigurationConstants.MAIL_DEBUG_ENABLE)) {
            LogManager.getRootLogger().setLevel(Level.DEBUG);
        } else {
            LogManager.getRootLogger().setLevel(Level.INFO);
        }

        // On vérifie le filtre sur l'expediteur qui est requis
        if (StringUtils.isBlank(getString(ConfigurationConstants.MAIL_FILTER_FROM))) {
            LOGGER.fatal("Le filtre sur l'expéditeur est obligatoire.");
            throw new ConfigurationException("Le filtre sur l'expéditeur est obligatoire.");
        }
    }

    /**
     * Renvoi la valeur String associée à la clé fournie. Si la clé n'existe pas, alors le retour sera <code>null</code>.
     *
     * @param key la clé de property
     * @return La valeur associée sinon <code>null</code>.
     */
    public String getString(String key) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format(">> Configuration:getString(key = '%s')", key));
        }

        String value = null;
        if (properties != null) {
            value = properties.getProperty(key);
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("<< Configuration:getString()");
        }
        return value;
    }

    /**
     * Renvoi la valeur Boolean associée à la clé fournie. Si la clé n'existe pas, alors le retour sera <code>false</code>.
     *
     * @param key la clé de property
     * @return La valeur associée sinon <code>false</code>.
     */
    public Boolean getBoolean(String key) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format(">> Configuration:getBoolean(key = '%s')", key));
        }

        String stringValue = getString(key);
        Boolean booleanValue = Boolean.FALSE;

        if (StringUtils.isNotBlank(stringValue)) {
            booleanValue = BooleanUtils.toBoolean(stringValue);
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("<< Configuration:getBoolean()");
        }
        return booleanValue;
    }

    /**
     * Renvoi le protocol paramétré dans le fichier de configuration.
     * Par défaut, POP3 est utilisé si la valeur n'est pas paramétrée ou bien non présente.
     *
     * @return Le protocol paramétré.
     */
    public String getProtocol() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(">> Configuration:getProtocol()");
        }
        String protocol = ConfigurationConstants.MAIL_STORE_PROTOCOL_DEFAULT_VALUE;
        String value = getString(ConfigurationConstants.MAIL_STORE_PROTOCOL);

        // On vérifie si le protocol est paramétré et qu'il fait parti des protocols gérés par l'application.
        if (StringUtils.isNoneBlank(value) && ConfigurationConstants.HANDLED_PROTOTOLS.contains(value)) {
            protocol = value;
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("<< Configuration:getProtocol()");
        }
        return protocol;
    }

    /**
     * Renvoi le chemin du répertoire où l'application ira stocker les mails.
     *
     * @return Le path du répertoire.
     */
    public String getOutputDirPath() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(">> Configuration:getOutputDirPath()");
        }
        String outputDirectory = ConfigurationConstants.MAIL_OUTPUT_PATH_DEFAULT;
        String value = getString(ConfigurationConstants.MAIL_OUTPUT_PATH);

        if (StringUtils.isNoneBlank(value)) {
            outputDirectory = value;
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("<< Configuration:getOutputDirPath()");
        }

        return outputDirectory;
    }
}