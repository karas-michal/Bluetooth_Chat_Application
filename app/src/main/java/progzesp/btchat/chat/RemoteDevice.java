package progzesp.btchat.chat;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Krzysztof on 2017-01-13.
 */
public class RemoteDevice implements Runnable {

    private BluetoothDevice device;
    private OutputStream outputStream;
    private InputStream inputStream;
    private Handler handler;
    private NewMessageListener newMessageListener;
    private LostConnectionListener lostConnectionListener;


    public RemoteDevice(Handler handler, BluetoothSocket socket, NewMessageListener nmListener, LostConnectionListener lcListener) {
        this.handler = handler;
        this.device = socket.getRemoteDevice();
        newMessageListener = nmListener;
        lostConnectionListener = lcListener;
        try {
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        handler.postDelayed(this, 500);
    }


    public void run() {
        byte[] buffer = new byte[1024];
        int bytes = 0;
        System.out.println(bytes);
        try {
            if (inputStream.available() > 0)
                bytes = inputStream.read(buffer, 0, 1024 - bytes);
        } catch (IOException e) {
            e.printStackTrace();
            lostConnectionListener.onLostConnection(device);
        }
        if (bytes > 0) {
            newMessageListener.onNewMessage(this, new String(buffer));
        }
        handler.postDelayed(this, 500);
    }


    public void send(String message) {
        try {
            outputStream.write(message.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public String getAddress() {
        return device.getAddress();
    }


}