package org.lny.mailcatcher.mail.service.impl;

import microsoft.exchange.webservices.data.core.exception.service.local.ServiceLocalException;
import microsoft.exchange.webservices.data.core.service.item.Item;
import microsoft.exchange.webservices.data.property.complex.FileAttachment;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.lny.mailcatcher.biz.Attachment;
import org.lny.mailcatcher.biz.Mail;
import org.lny.mailcatcher.mail.service.IRepositoryService;
import org.lny.mailcatcher.mail.service.constant.RepositoryConstants;
import org.lny.mailcatcher.mail.service.exception.RepositoryServiceException;
import org.lny.mailcatcher.tecnical.Configuration;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import java.io.*;
import java.util.Arrays;

/**
 * @author NAGY Léventé
 * @since 1.0
 */
public class RepositoryServiceImpl implements IRepositoryService {

    private static final Logger LOGGER = Logger.getLogger(MailServiceImpl.class);

    private File outputDirectory;

    /**
     * Unique instance du repository.
     */
    private static IRepositoryService INSTANCE;

    /**
     * Constructeur privé pour empêcher l'instanciation.
     */
    private RepositoryServiceImpl() {
    }

    /**
     * Retourne l'unique instance du repository.
     *
     * @return L'instance.
     */
    public static IRepositoryService getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new RepositoryServiceImpl();
        }
        return INSTANCE;
    }

    /**
     * @see {@link IRepositoryService#initializeOutputDir()}
     */
    @Override
    public Boolean initializeOutputDir() throws RepositoryServiceException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(">> MailServiceImpl:initOutputDir()");
        }

        outputDirectory = new File(Configuration.getInstance().getOutputDirPath());

        // On nettoie le répertoire de sortie s'il existe pour une nouvelle analyse
        if (outputDirectory.exists()) {
            try {
                FileUtils.cleanDirectory(outputDirectory);
            } catch (IOException e) {
                RepositoryServiceException exception = new RepositoryServiceException("Impossible de nettoyer le " +
                        "dossier de sortie existant.");
                LOGGER.fatal(exception);
                throw exception;
            }
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("<< MailServiceImpl:initOutputDir()");
        }

        return Boolean.TRUE;
    }

    /**
     * @see {@link IRepositoryService#writeEml(String, Long, Mail)}
     */
    @Override
    public void writeEml(String expeditor, Long index, Mail mail) throws RepositoryServiceException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format(">> RepositoryServiceImp::writeEml(expeditor = '%s', index = %d)",
                    expeditor, index));
        }

        if (StringUtils.isNotBlank(expeditor) && index != null && mail != null) {
            File emlFile = getFile(expeditor, index, index + RepositoryConstants.EML_FILE_EXTENSION);
            // Pour les messages provenant d'IMAP ou de POP3
            if (mail.getMessage() instanceof Message) {
                try {
                    ((Message) mail.getMessage()).writeTo(FileUtils.openOutputStream(emlFile));
                } catch (IOException | MessagingException e) {
                    RepositoryServiceException exception = new RepositoryServiceException(String.format(
                            "Erreur lors de l'écriture du fichier EML (expediteur = '%s', index = %d", expeditor, index));
                    LOGGER.fatal(exception);
                    throw exception;
                }
            } else if (mail.getMessage() instanceof Item) { // Pour les messages provenant d'exchange
                try {
                    String mimeContent = ((Item) mail.getMessage()).getMimeContent().toString();
                    if (StringUtils.isNotBlank(mimeContent)) {
                        FileUtils.copyToFile(new ByteArrayInputStream(mimeContent.getBytes()), emlFile);
                    }
                } catch (ServiceLocalException | IOException e) {
                    RepositoryServiceException exception = new RepositoryServiceException(String.format(
                            "Erreur lors de l'écriture du fichier EML (expediteur = '%s', index = %d", expeditor, index));
                    LOGGER.fatal(exception);
                    throw exception;
                }

            }
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(">> RepositoryServiceImp::writeEml()");
        }
    }

    /**
     * @see {@link IRepositoryService#writeAttachment(String, Long, Attachment)}
     */
    @Override
    public void writeAttachment(String expeditor, Long index, Attachment attachment) throws RepositoryServiceException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format(">> RepositoryServiceImp::writeAttachment(expeditor = '%s', index = %d)",
                    expeditor, index));
        }

        if (StringUtils.isNotBlank(expeditor) && index != null && attachment != null) {
            if (attachment.getAttachment() instanceof BodyPart) { // Pour les PJ provenant d'IMAP ou de POP3
                BodyPart pj = (BodyPart) attachment.getAttachment();
                try {
                    File attachmentFile = getFile(expeditor, index,
                            index + RepositoryConstants.STRING_UNDERSCORE + pj.getFileName());
                    FileUtils.copyToFile(pj.getInputStream(), attachmentFile);
                } catch (MessagingException | IOException e) {
                    RepositoryServiceException exception = new RepositoryServiceException(String.format("Erreur lors de l'écriture d'une " +
                            "pièce joint de l'expéditeur '%s', index = %d", expeditor, index));
                    LOGGER.fatal(exception);
                    throw exception;
                }
            } else if (attachment.getAttachment() instanceof FileAttachment) { // Pour les PJ provenant d'exchange
                FileAttachment pj = (FileAttachment) attachment.getAttachment();
                OutputStream fileStream = null;
                try {
                    File attachmentFile = getFile(expeditor, index,
                            index + RepositoryConstants.STRING_UNDERSCORE + pj.getName());
                    fileStream = new FileOutputStream(attachmentFile);
                    pj.load(fileStream);
                } catch (Exception e) {
                    LOGGER.fatal(String.format("Une erreur s'est produite durant l'écriture de la piece jointe" +
                            " '%s'", pj.getFileName()));
                    throw new RepositoryServiceException(e);
                } finally {
                    if (fileStream != null) {
                        try {
                            fileStream.flush();
                            fileStream.close();
                        } catch (IOException e) {
                            LOGGER.error("Erreur lors de la fermeture du flux d'ecriture pour une pièce jointe.");
                        }
                    }
                }
            }
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("<< RepositoryServiceImp::writeAttachment()");
        }
    }

    /**
     * @see {@link IRepositoryService#writeTxtFile(String, Long, String, String)}
     */
    @Override
    public void writeTxtFile(String expeditor, Long index, String subject, String content) throws
            RepositoryServiceException {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format(">> RepositoryServiceImp::writeTxtFile(expeditor = '%s'," +
                    " index = '%d', subject = '%s')", expeditor, index, subject));
        }

        if (StringUtils.isNotBlank(expeditor) && index != null) {
            File txtFile = getFile(expeditor, index, index + RepositoryConstants.TEXT_FILE_EXTENSION);
            try {
                FileUtils.writeLines(txtFile, Arrays.asList(RepositoryConstants.LIBELLE_SUBJECT + subject,
                        RepositoryConstants.LIBELLE_CONTENT + content));
            } catch (IOException e) {
                LOGGER.fatal(String.format("Une erreur s'est produite lors de l'écriture du fichier TXT - " +
                        "expeditor = %s / index = %d", expeditor, index));
                throw new RepositoryServiceException(e);
            }
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("<< RepositoryServiceImp::writeTxtFile()");
        }

    }

    /**
     * @see {@link IRepositoryService#writeJsonFile(String, JSONArray)}
     */
    @Override
    public void writeJsonFile(String expeditor, JSONArray sumUp) throws RepositoryServiceException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format(">> RepositoryServiceImp::writeJsonFile(expeditor = '%s')", expeditor));
        }

        if (StringUtils.isNotBlank(expeditor) && sumUp != null) {
            File sumUpFile = getFile(expeditor, null, RepositoryConstants.JSON_FILE_NAME);
            FileWriter fileWriter = null;
            try {
                fileWriter = new FileWriter(sumUpFile);
                fileWriter.write(sumUp.toJSONString());
            } catch (IOException e) {
                LOGGER.fatal(String.format("Une erreur s'est produite lors de l'écriture du résumé JSON - " +
                        "expeditor = %s", expeditor));
                throw new RepositoryServiceException(e);
            } finally {
                if (fileWriter != null) {
                    try {
                        fileWriter.flush();
                        fileWriter.close();
                    } catch (IOException e) {
                        LOGGER.error(String.format("Une erreur s'est produite lors de l'écriture du résumé JSON - " +
                                "expeditor = %s", expeditor));
                    }
                }
            }
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("<< RepositoryServiceImp::writeJsonFile()");
        }
    }

    /**
     * Renvoi l'objet File pour un fichier d'un expéditeur à un index donné.
     *
     * @param expeditor l'expéditeur
     * @param index     l'index (optionel)
     * @param filename  le nom du fichier
     * @return L'objet {@link File} du fichier.
     */

    private File getFile(String expeditor, Long index, String filename) {
        return new File(outputDirectory.getAbsolutePath() + File.separator + expeditor.toLowerCase() +
                (index != null ? File.separator + index : StringUtils.EMPTY) + File.separator + filename);

    }
}
