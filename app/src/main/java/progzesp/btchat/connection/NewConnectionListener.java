package progzesp.btchat.connection;

import android.bluetooth.BluetoothSocket;


public interface NewConnectionListener {

    void onNewConnection(BluetoothSocket socket);

}
