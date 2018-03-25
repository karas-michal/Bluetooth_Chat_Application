package progzesp.testSystem.connection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Toast;


public class DeviceFinder {

    private static final String TAG = "DeviceFinder";

    private boolean searching;
    private Context context;
    private BluetoothAdapter adapter;
    private NewDeviceListener newDeviceListener;
    private DiscoveryFinishedListener discoveryFinishedListener;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            synchronized (DeviceFinder.this) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    int  rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE);
                    Toast.makeText(context, "RSSI: "+rssi, Toast.LENGTH_LONG).show();
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    newDeviceListener.onNewDevice(device, rssi);
                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    searching = false;
                    context.unregisterReceiver(this);
                    discoveryFinishedListener.onDiscoveryFinished();
                }
            }
        }
    };


    public DeviceFinder(BluetoothAdapter adapter, Context context, NewDeviceListener devicesListener, DiscoveryFinishedListener discFinListener) {
        this.adapter = adapter;
        this.context = context;
        newDeviceListener = devicesListener;
        discoveryFinishedListener = discFinListener;
        startDiscovery();
    }


    public synchronized void startDiscovery() {
        if (!searching) {
            Log.d(TAG, "Starting discovery");
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            context.registerReceiver(receiver, filter);
            filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            context.registerReceiver(receiver, filter);
            BluetoothAdapter.getDefaultAdapter().startDiscovery();
            searching = true;
        }
    }


    public synchronized void stopDiscovery() {
        if (searching) {
            Log.d(TAG, "Stopping discovery");
            adapter.cancelDiscovery();
            context.unregisterReceiver(receiver);
            searching = false;
        }
    }

}

