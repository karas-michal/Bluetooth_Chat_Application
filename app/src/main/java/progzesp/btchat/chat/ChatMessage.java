package progzesp.btchat.chat;

import java.io.Serializable;

/**
 * Created by Krzysztof on 2017-01-28.
 */
public class ChatMessage implements Serializable {
    private String contents;
    private String sender;
    private int ttl;
    private long time;
    private messageType type;

    public ChatMessage(long time, int ttl, String sender, String contents) {
        this.time = time;
        this.ttl = ttl;
        this.sender = sender;
        this.contents = contents;
    }

    public messageType getType() {
        return type;
    }

    public void setType(messageType type) {
        this.type = type;
    }

    public ChatMessage(String sender, String contents) {
        this.contents = contents;
        this.sender = sender;
    }

    public ChatMessage(String sender, String contents, long time, int ttl, messageType type) {
        this.sender = sender;
        this.contents = contents;
        this.time = time;
        this.ttl = ttl;
        this.type = type;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public ChatMessage(String contents, String sender, int ttl) {
        this.contents = contents;
        this.sender = sender;
        this.ttl = ttl;
    }

    public int getTtl() {
        return ttl;
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
