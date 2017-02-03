package progzesp.btchat.communication;


public interface NewMessageListener {

    void onNewMessage(RemoteDevice device, Object message);

}
