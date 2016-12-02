package progzesp.btchat.connection;

import android.bluetooth.BluetoothSocket;

/**
 * Created by Krzysztof on 2016-11-15.
 */
public interface NewConnectionListener {

    void onNewConnection(BluetoothSocket socket);

}
