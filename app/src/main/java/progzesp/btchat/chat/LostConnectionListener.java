package progzesp.btchat.chat;

import android.bluetooth.BluetoothDevice;

/**
 * Created by Krzysztof on 2017-01-13.
 */
public interface LostConnectionListener {

    void onLostConnection(BluetoothDevice device);

}
