package progzesp.btchat.chat;

import java.io.Serializable;

/**
 * Created by Krzysztof on 2017-01-28.
 */
public class ChatMessage implements Serializable {
    private String contents;
    private String sender;

    public ChatMessage(String sender, String contents) {
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
