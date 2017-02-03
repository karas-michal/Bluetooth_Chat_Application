package progzesp.btchat.communication;

import java.io.Serializable;


public class ChatMessage implements Serializable {

    private String contents;
    private String sender;

    public ChatMessage(String sender, String contents) {
        this.contents = contents;
        this.sender = sender;
    }


    public String toString() {
        return sender + ": " + contents;
    }

}
