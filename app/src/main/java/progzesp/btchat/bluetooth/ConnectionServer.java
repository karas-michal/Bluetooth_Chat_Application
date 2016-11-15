package progzesp.btchat.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by Krzysztof on 2016-11-15.
 */
public class ConnectionServer implements Runnable {

    private Thread thread;
    private BluetoothServerSocket serverSocket;
    private NewConnectionListener listener;


    public ConnectionServer(BluetoothAdapter adapter, UUID uuid, NewConnectionListener listener) throws IOException {
        this.listener = listener;
        serverSocket = adapter.listenUsingInsecureRfcommWithServiceRecord("", uuid);
        thread = new Thread(this);
        thread.start();
    }


    public void terminate() {

    }


    @Override
    public void run() {

    }

}
