package progzesp.btchat.connection;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;


public class ConnectionClient implements Runnable {

    private static final String TAG = "ConnectionClient";

    private Thread thread;
    private BluetoothSocket socket;
    private FailureToConnectListener failureToConnectListener;
    private NewConnectionListener newConnectionListener;


    public ConnectionClient(BluetoothDevice device, UUID uuid, NewConnectionListener connListener, FailureToConnectListener failListener) {
        failureToConnectListener = failListener;
        newConnectionListener = connListener;
        try {
            socket = device.createInsecureRfcommSocketToServiceRecord(uuid);
            thread = new Thread(this);
            thread.start();
        } catch (IOException e) {
            Log.e(TAG, "Error while creating socket", e);
            failureToConnectListener.onFailureToConnect();
        }
    }


    public void terminate() {
        Log.d(TAG, "Terminating connection thread");
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error while closing socket", e);
        }
    }


    @Override
    public void run() {
        Log.d(TAG, "Connection thread started");
        try {
            socket.connect();
            newConnectionListener.onNewConnection(socket);
        } catch (IOException e) {
            try {
                Log.e(TAG, "Error while attempting to connect to " + socket.getRemoteDevice().getName(), e);
                socket.close();
            } catch (IOException e2) {
                Log.e(TAG, "Error while closing socket during connection failure", e2);
            }
            failureToConnectListener.onFailureToConnect();
        }
        Log.d(TAG, "Connection thread ended");
    }

}
