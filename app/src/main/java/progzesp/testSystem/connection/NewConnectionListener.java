package progzesp.testSystem.connection;

import android.bluetooth.BluetoothSocket;


public interface NewConnectionListener {

    void onNewConnection(BluetoothSocket socket, int rssi, int flag);

}
