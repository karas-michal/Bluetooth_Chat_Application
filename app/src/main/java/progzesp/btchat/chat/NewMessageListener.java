package progzesp.btchat.chat;

/**
 * Created by Krzysztof on 2017-01-13.
 */
public interface NewMessageListener {

    void onNewMessage(RemoteDevice device, String message);

}
