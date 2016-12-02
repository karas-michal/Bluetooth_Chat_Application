package progzesp.btchat.connection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by Krzysztof on 2016-11-15.
 */
public class ConnectionServer implements Runnable {

    private static final String TAG = "ConnectionServer";

    private Thread thread;
    private BluetoothServerSocket serverSocket;
    private NewConnectionListener newConnectionListener;
    private DiscoverabilityProvider discoverabilityProvider;


    public ConnectionServer(BluetoothAdapter adapter, UUID uuid, DiscoverabilityProvider discProvider,
                            NewConnectionListener newConnListener) throws IOException {
        newConnectionListener = newConnListener;
        discoverabilityProvider = discProvider;
        serverSocket = adapter.listenUsingInsecureRfcommWithServiceRecord("", uuid);
        thread = new Thread(this);
        thread.start();
    }


    public void terminate() {
        Log.d(TAG, "Stopping accepting thread");
        try {
            serverSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Error while closing server socket", e);
        }
    }


    @Override
    public void run() {
        Log.d(TAG, "Started accepting thread");
        BluetoothSocket socket;
        while (true) {
            discoverabilityProvider.ensureDiscoverability();
            try {
                socket = serverSocket.accept();
            } catch (IOException e) {
                Log.e(TAG, "Error while accepting", e);
                break;
            }
            if (socket != null) {
                newConnectionListener.onNewConnection(socket);
            }
        }
        Log.d(TAG, "Accepting thread ended");
    }

}
