package progzesp.testSystem.communication;


public interface NewChatMessageListener {

    void onNewTextMessage(TextMessage message);

    void onHelloMessage(HelloMessage message);

    void onNewDeviceMessage(NewDeviceMessage message);
}
