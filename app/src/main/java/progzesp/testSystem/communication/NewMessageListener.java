package progzesp.testSystem.communication;


public interface NewMessageListener {

    void onNewMessage(RemoteDevice device, Object message);

}
