package progzesp.btchat.connection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.io.IOException;
import java.util.*;

/**
 * Created by Krzysztof on 2016-11-15.
 */
public class ConnectionClient implements Runnable {

    private static final String TAG = "ConnectionClient";

    private Queue<BluetoothDevice> devices = new LinkedList<>();
    private Object devicesMutex = new Object();
    private Thread thread;
    private UUID uuid;
    private Context context;
    private BluetoothAdapter adapter;
    private volatile BluetoothSocket socket;
    private NewConnectionListener newConnectionListener;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                attemptConnection(device);
                stopDiscovery();
            }// else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action) && socket == null) {
            //    startDiscovery();
            //}
        }
    };


    public ConnectionClient(BluetoothAdapter adapter, Context context, UUID uuid, NewConnectionListener connListener) {
        this.adapter = adapter;
        this.uuid = uuid;
        this.context = context;
        newConnectionListener = connListener;
        thread = new Thread(this);
        startDiscovery();
    }


    public void terminate() {
        stopDiscovery();
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
        } catch (Exception e) {
            try {
                Log.e(TAG, "Error while attempting to connect", e);
                socket.close();
            } catch (Exception e2) {
                Log.e(TAG, "Error while closing socket during connection failure", e2);
            }
            socket = null;
            startDiscovery();
        }
    }


    private void attemptConnection(BluetoothDevice device) {
        try {
            socket = device.createInsecureRfcommSocketToServiceRecord(uuid);
            thread.start();
        } catch (Exception e) {
            Log.e(TAG, "Error while creating socket", e);
            startDiscovery();
        }
    }


    private void startDiscovery() {
        Log.d(TAG, "Starting discovery");
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        context.registerReceiver(receiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        context.registerReceiver(receiver, filter);
        adapter.startDiscovery();
    }


    private void stopDiscovery() {
        Log.d(TAG, "Stopping discovery");
        adapter.cancelDiscovery();
        //context.unregisterReceiver(receiver);
    }

}