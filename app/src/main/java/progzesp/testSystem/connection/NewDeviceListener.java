package progzesp.testSystem.connection;

import android.bluetooth.BluetoothDevice;


public interface NewDeviceListener {

    void onNewDevice(BluetoothDevice device, int rssi);

}
