package progzesp.btchat.communication;

import android.bluetooth.BluetoothSocket;
import progzesp.btchat.connection.NewConnectionListener;


public class CommunicationService implements NewConnectionListener {

    private LostConnectionListener lostConnectionListener;
    private NewMessageListener newMessageListener;


    public void setLostConnectionListener(LostConnectionListener listener) {
        lostConnectionListener = listener;
    }


    public void setNewMessageListener(NewMessageListener listener) {
        newMessageListener = listener;
    }


    @Override
    public void onNewConnection(BluetoothSocket socket) {
        // Nowe połączenie
    }

}
