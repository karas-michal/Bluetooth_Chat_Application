package progzesp.btchat.connection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by Krzysztof on 2016-11-15.
 */
public class ConnectionServer implements Runnable {

    private static final String TAG = "ConnectionServer";

    private Thread thread;
    private BluetoothAdapter adapter;
    private Context context;
    private BluetoothServerSocket serverSocket;
    private NewConnectionListener newConnectionListener;


    public ConnectionServer(BluetoothAdapter adapter, Context context, UUID uuid, NewConnectionListener newConnListener) throws IOException {
        this.adapter = adapter;
        this.context = context;
        newConnectionListener = newConnListener;
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
            ensureDiscoverability();
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


    private void ensureDiscoverability() {
        if (adapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            context.startActivity(discoverableIntent);
        }
    }

}
