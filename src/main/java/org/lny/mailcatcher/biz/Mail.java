package org.lny.mailcatcher.biz;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe générique pour transporter les informations d'un mail.
 *
 * @author NAGY Léventé
 * @since 1.0
 */
public class Mail<T> {

    /**
     * Le mail de l'expéditeur.
     */
    private String from;
    /**
     * L'objet du mail.
     */
    private String subject;
    /**
     * Le contenu du mail.
     */
    private String body;
    /**
     * Les pièces jointes s'il y en a.
     */
    private List<Attachment> piecesJointes;

    /**
     * Le message d'origine.
     */
    private T message;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public List<Attachment> getPiecesJointes() {
        if (piecesJointes == null) {
            piecesJointes = new ArrayList<>();
        }
        return piecesJointes;
    }

    public T getMessage() {
        return message;
    }

    public void setMessage(T message) {
        this.message = message;
    }
}
