package progzesp.btchat.connection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Krzysztof on 2017-01-12.
 */
public class DeviceFinder {

    private static final String TAG = "DeviceFinder";

    private Context context;
    private BluetoothAdapter adapter;
    private NewDeviceListener newDeviceListener;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            synchronized (DeviceFinder.this) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    newDeviceListener.onNewDevice(device);
                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {

                }
            }
        }
    };


    public DeviceFinder(BluetoothAdapter adapter, Context context, NewDeviceListener devicesListener) {
        this.adapter = adapter;
        this.context = context;
        newDeviceListener = devicesListener;
        startDiscovery();
    }


    public void startDiscovery() {
        Log.d(TAG, "Starting discovery");
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        context.registerReceiver(receiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        context.registerReceiver(receiver, filter);
        adapter.startDiscovery();
    }


    public void stopDiscovery() {
        Log.d(TAG, "Stopping discovery");
        adapter.cancelDiscovery();
        //context.unregisterReceiver(receiver);
    }

}

