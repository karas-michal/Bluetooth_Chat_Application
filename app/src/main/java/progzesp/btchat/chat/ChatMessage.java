package progzesp.btchat.chat;

import java.io.Serializable;

/**
 * Created by Krzysztof on 2017-01-28.
 */
public class ChatMessage implements Serializable {
    private String contents;
    private String sender;
    private int ttl;
    private int originalTtl;
    private messageType type;

    public messageType getType() {
        return type;
    }

    public void setType(messageType type) {
        this.type = type;
    }

    public ChatMessage(String sender, String contents) {
        type = messageType.MESSAGE;
        this.contents = contents;
        this.sender = sender;
    }

    public ChatMessage(String sender, String contents, int ttl, messageType type) {
        this.sender = sender;
        this.contents = contents;
        this.ttl = ttl;
        this.originalTtl = ttl;
        this.type = type;
    }

    public int getTtl() {
        return ttl;
    }

    public int getOriginalTtl() {
        return originalTtl;
    }

    public void setTtl(int ttl) {
        this.ttl = ttl;
    }

    public String getContents() {
        return contents;
    }

    public String getSender() {
        return sender;
    }

    public String toString() { return sender + ": " + contents; }
}
