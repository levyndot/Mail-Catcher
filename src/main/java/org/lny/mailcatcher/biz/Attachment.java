package org.lny.mailcatcher.biz;

/**
 * Classe générique pour contenir les pièces jointes.
 *
 * @author NAGY Léventé
 * @since 1.0
 */
public class Attachment<T> {

    /**
     * La pièce jointe
     */
    private T attachment;

    public Attachment(T attachment) {
        this.attachment = attachment;
    }

    public T getAttachment() {
        return attachment;
    }
}
