package progzesp.btchat.communication;

/**
 * Created by Krzysztof on 2017-01-12.
 */
public interface NewMessageListener {

    // Ten listener wywoływany jest przez moduł komunikacji. Implementuje go moduł czatu.
    void onNewMessage(byte[] rawMessage);

}
