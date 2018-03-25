package progzesp.testSystem.communication;

import java.io.Serializable;

/**
 * Klasa enkapsulująca wiadomość w czacie
 */
public class TextMessage implements Serializable {

    private String contents;
    private String sender;

    public TextMessage(String sender, String contents) {
        this.contents = contents;
        this.sender = sender;
    }

    public String getContents() {
        return contents;
    }

    public String getSender() {
        return sender;
    }

    public String toString() { return sender + ": " + contents; }
}
