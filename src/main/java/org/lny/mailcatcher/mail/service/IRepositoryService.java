package org.lny.mailcatcher.mail.service;

import org.json.simple.JSONArray;
import org.lny.mailcatcher.biz.Attachment;
import org.lny.mailcatcher.biz.Mail;
import org.lny.mailcatcher.mail.service.exception.RepositoryServiceException;

/**
 * Contrat d'interface pour le service de repository.
 *
 * @author NAGY Léventé
 * @since 1.0
 */
public interface IRepositoryService {

    /**
     * Initialise le répertoire d'écriture dans lequel seront stockés les mails.
     *
     * @throws RepositoryServiceException
     */
    Boolean initializeOutputDir() throws RepositoryServiceException;

    /**
     * Ecrit le fichier EML du mail dans le dossier de sortie.
     *
     * @param expeditor le mail de l'expediteur
     * @param index     l'index à laquel le fichier doit être écrit
     * @param message   le message a écrire
     * @throws RepositoryServiceException
     */
    void writeEml(String expeditor, Long index, Mail message) throws RepositoryServiceException;

    /**
     * Ecrit la pièce jointe d'un mail dans le dossier approprié.
     *
     * @param expeditor  le mail de l'expediteur
     * @param index      l'index à laquel le fichier doit être écrit
     * @param attachment la pièce jointe
     * @throws RepositoryServiceException
     */
    void writeAttachment(String expeditor, Long index, Attachment attachment) throws RepositoryServiceException;

    /**
     * Ecrit le fichier de résumé TXT dans le dossier approprié.
     *
     * @param expeditor le mail de l'expediteur
     * @param index     l'index à laquel le fichier doit être écrit
     * @param subject   le sujet du mail
     * @param content   le contenu du mail
     * @throws RepositoryServiceException
     */
    void writeTxtFile(String expeditor, Long index, String subject, String content) throws RepositoryServiceException;

    /**
     * Ecrit le fichier JSON de rapport pour un expéditeur donné.
     *
     * @param expeditor l'adresse mail de l'expéditeur
     * @param sumUp     le tableau JSON contenant les éléments à écrire dans le fichier.
     */
    void writeJsonFile(String expeditor, JSONArray sumUp) throws RepositoryServiceException;
}
