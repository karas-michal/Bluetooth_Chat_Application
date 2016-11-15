package progzesp.btchat.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by Krzysztof on 2016-11-15.
 */
public class ConnectionClient implements Runnable {

    private Thread thread;
    private UUID uuid;
    private BluetoothSocket socket;
    private NewConnectionListener listener;


    public ConnectionClient(BluetoothAdapter adapter, UUID uuid, NewConnectionListener listener) {
        this.uuid = uuid;
        this.listener = listener;
        thread = new Thread(this);
        // Start device discovery; on detection start thread
    }


    public void terminate() {

    }


    @Override
    public void run() {

    }


    private void attemptConnection(BluetoothDevice device) throws IOException {
        socket = device.createInsecureRfcommSocketToServiceRecord(uuid);
        thread.start();
    }

}
