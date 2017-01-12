package progzesp.btchat.connection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import progzesp.btchat.communication.LostConnectionListener;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by Krzysztof on 2016-11-15.
 */
public class ConnectionProvider implements LostConnectionListener {

    private static final UUID APP_UUID = UUID.fromString("5fc104c0-0fe6-448d-8a7d-06a9f16cef94");

    private BluetoothAdapter adapter;
    private Context context;
    private NewConnectionListener newConnectionListener;
    private ConnectionClient client;
    private ConnectionServer server;
    private BluetoothDevice remoteClient;
    private BluetoothDevice remoteServer;


    public ConnectionProvider(Context context, BluetoothAdapter adapter) {
        super();
        this.context = context;
        this.adapter = adapter;
    }


    public synchronized void startAttemptingConnections() {
        if (client != null) {
            return;
        }
        stopAcceptingConnections();
        client = new ConnectionClient(adapter, context, APP_UUID, new NewConnectionListener() {
            @Override
            public void onNewConnection(BluetoothSocket socket) {
                if (newConnectionListener != null) {
                    remoteServer = socket.getRemoteDevice();
                    newConnectionListener.onNewConnection(socket);
                }
            }
        });
    }


    public synchronized void stopAttemptingConnections() {
        if (client != null) {
            client.terminate();
            client = null;
        }
    }


    public synchronized void startAcceptingConnections() {
        if (server != null) {
            return;
        }
        stopAttemptingConnections();
        try {
            server = new ConnectionServer(adapter, context, APP_UUID, new NewConnectionListener() {
                @Override
                public void onNewConnection(BluetoothSocket socket) {
                    if (newConnectionListener != null) {
                        remoteClient = socket.getRemoteDevice();
                        newConnectionListener.onNewConnection(socket);
                    }
                }
            }, new FinishedListener() {
                @Override
                public void onFinished() {
                    if (remoteServer == null) {
                        stopAttemptingConnections();
                        startAttemptingConnections();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public synchronized void stopAcceptingConnections() {
        if (server != null) {
            server.terminate();
            server = null;
        }
    }


    public void setNewConnectionListener(NewConnectionListener listener) {
        newConnectionListener = listener;
    }


    @Override
    public void onLostConnection(BluetoothDevice device) {
        if (device.equals(remoteClient)) {
            remoteClient = null;
            startAcceptingConnections();
        } else if (device.equals(remoteServer)) {
            remoteServer = null;
            startAttemptingConnections();
        }
    }

}
