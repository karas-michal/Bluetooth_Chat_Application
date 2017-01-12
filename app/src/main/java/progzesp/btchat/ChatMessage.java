package progzesp.btchat;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by karas on 30.11.2016.
 */

public class ChatMessage {
    private String text;
    private String sender;
    private SimpleDateFormat time;

    public ChatMessage(String text, String sender) {
        this.text = text;
        this.sender = sender;
        this.time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    }

    public String getText() {
        return text;
    }

    public String getSender() {
        return sender;
    }

    public SimpleDateFormat getTime() {
        return time;
    }
}
