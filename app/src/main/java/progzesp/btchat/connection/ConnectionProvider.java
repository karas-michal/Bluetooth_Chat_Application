package progzesp.btchat.connection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by Krzysztof on 2016-11-15.
 */
public class ConnectionProvider {

    private static final UUID APP_UUID = UUID.fromString("5fc104c0-0fe6-448d-8a7d-06a9f16cef94");

    private BluetoothAdapter adapter;
    private Context context;
    private NewConnectionListener newConnectionListener;
    private ConnectionClient client;
    private ConnectionServer server;


    public ConnectionProvider(Context context, BluetoothAdapter adapter) {
        super();
        this.context = context;
        this.adapter = adapter;
    }


    public void startAttemptingConnections() {
        if (client != null) {
            return;
        }
        client = new ConnectionClient(adapter, context, APP_UUID, new NewConnectionListener() {
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
            server = new ConnectionServer(adapter, context, APP_UUID, new NewConnectionListener() {
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

}
