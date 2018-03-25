package progzesp.testSystem.communication;

import android.bluetooth.BluetoothDevice;

public interface LostConnectionListener {

    void onLostConnection(BluetoothDevice device);

}
