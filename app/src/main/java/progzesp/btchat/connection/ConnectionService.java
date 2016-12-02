package progzesp.btchat.connection;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by Krzysztof on 2016-11-15.
 */
public class ConnectionService extends Service implements DiscoverabilityProvider {

    private static final UUID APP_UUID = UUID.fromString("5fc104c0-0fe6-448d-8a7d-06a9f16cef94");

    private BluetoothAdapter adapter;
    private NewConnectionListener newConnectionListener;
    private ConnectionClient client;
    private ConnectionServer server;


    public ConnectionService(BluetoothAdapter adapter) {
        super();
        this.adapter = adapter;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    public void startAttemptingConnections() {
        if (client != null) {
            return;
        }
        client = new ConnectionClient(adapter, APP_UUID, this, new NewConnectionListener() {
            @Override
            public void onNewConnection(BluetoothSocket socket) {
                stopAttemptingConnections();
                if (newConnectionListener != null) {
                    newConnectionListener.onNewConnection(socket);
                }
            }
        });
    }


    public void stopAttemptingConnections() {
        if (client != null) {
            client.terminate();
            client = null;
        }
    }


    public void startAcceptingConnections() {
        if (server != null) {
            return;
        }
        try {
            server = new ConnectionServer(adapter, APP_UUID, this, new NewConnectionListener() {
                @Override
                public void onNewConnection(BluetoothSocket socket) {
                    stopAcceptingConnections();
                    if (newConnectionListener != null) {
                        newConnectionListener.onNewConnection(socket);
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void stopAcceptingConnections() {
        if (server != null) {
            server.terminate();
            server = null;
        }
    }


    public void setNewConnectionListener(NewConnectionListener listener) {
        newConnectionListener = listener;
    }


    public void ensureDiscoverability() {
        if (adapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

}
