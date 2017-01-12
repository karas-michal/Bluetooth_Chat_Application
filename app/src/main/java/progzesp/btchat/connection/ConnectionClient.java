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
    private Thread thread;
    private UUID uuid;
    private Context context;
    private BluetoothAdapter adapter;
    private BluetoothSocket socket;
    private NewConnectionListener newConnectionListener;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            synchronized (ConnectionClient.this) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    synchronized (this) {
                        devices.add(device);
                    }
                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    attemptConnections();
                }
            }
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


    public synchronized void terminate() {
        stopDiscovery();
        Log.d(TAG, "Terminating connection thread");
        try {
            devices.clear();
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
        BluetoothDevice device = null;
        synchronized (this) {
            device = devices.poll();
        }
        while (device != null) {
            try {
                socket = device.createInsecureRfcommSocketToServiceRecord(uuid);
                socket.connect();
                newConnectionListener.onNewConnection(socket);
                break;
            } catch (IOException e) {
                try {
                    Log.e(TAG, "Error while attempting to connect to " + device.getName(), e);
                    if (socket != null) {
                        socket.close();
                    }
                } catch (IOException e2) {
                    Log.e(TAG, "Error while closing socket during connection failure", e2);
                }
                synchronized (this) {
                    device = devices.poll();
                }
            }
        }
        thread = null;
        if (device == null) {
            startDiscovery();
        }
    }


    private void attemptConnections() {
        thread = new Thread(this);
        thread.start();
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
