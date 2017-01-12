package progzesp.btchat.connection;

import android.bluetooth.BluetoothDevice;

import java.util.List;

/**
 * Created by Krzysztof on 2016-11-15.
 */
public interface NewDeviceListener {

    void onNewDevice(BluetoothDevice device);

}
