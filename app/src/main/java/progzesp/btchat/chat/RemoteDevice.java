package progzesp.btchat.chat;

import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Created by Krzysztof on 2017-01-13.
 */
public class RemoteDevice implements Runnable {
    private BluetoothSocket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private Thread thread;
    private NewMessageListener newMessageListener;
    private LostConnectionListener lostConnectionListener;


    public RemoteDevice(BluetoothSocket socket, NewMessageListener nmListener, LostConnectionListener lcListener) {
        this.socket = socket;
        newMessageListener = nmListener;
        lostConnectionListener = lcListener;
        try {
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.flush();
            inputStream = new ObjectInputStream(socket.getInputStream());
            thread = new Thread(this);
            thread.start();
        } catch (IOException e) {
            e.printStackTrace();
            lostConnectionListener.onLostConnection(socket.getRemoteDevice());
        }
    }


    public void run() {
        while (true) {
            try {
                Object message = inputStream.readObject();
                newMessageListener.onNewMessage(this, message);
            } catch (IOException e) {
                e.printStackTrace();
                lostConnectionListener.onLostConnection(socket.getRemoteDevice());
                break;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }


    public void disconnect() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void send(Serializable message) {
        try {
            outputStream.writeObject(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public String getAddress() {
        return socket.getRemoteDevice().getAddress();
    }

}