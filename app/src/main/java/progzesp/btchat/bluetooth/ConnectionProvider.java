package progzesp.btchat.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by Krzysztof on 2016-11-15.
 */
public class ConnectionProvider {

    private static final UUID APP_UUID = UUID.fromString("5fc104c0-0fe6-448d-8a7d-06a9f16cef94");

    private BluetoothAdapter adapter;
    private NewConnectionListener newConnectionListener;
    private ConnectionClient client;
    private ConnectionServer server;


    public ConnectionProvider(BluetoothAdapter adapter) {
        this.adapter = adapter;
    }


    public void startAttemptingConnections() {
        if (client != null) {
            return;
        }
        client = new ConnectionClient(adapter, APP_UUID, new NewConnectionListener() {
            @Override
            public void onNewConnection(BluetoothSocket socket) {
                client = null;
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
            server = new ConnectionServer(adapter, APP_UUID, new NewConnectionListener() {
                @Override
                public void onNewConnection(BluetoothSocket socket) {
                    server = null;
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

}
