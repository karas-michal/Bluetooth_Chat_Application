package progzesp.btchat.communication;

import android.bluetooth.BluetoothDevice;


public interface LostConnectionListener {

    void onLostConnection(BluetoothDevice device);

}
