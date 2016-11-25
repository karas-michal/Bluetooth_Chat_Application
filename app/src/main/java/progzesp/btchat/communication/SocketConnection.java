package progzesp.btchat.communication;

import android.bluetooth.BluetoothSocket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by karas on 17.11.2016.
 */
public class SocketConnection {
    private InputStream inputStream;
    private OutputStream outputStream;
    private BluetoothSocket bluetoothSocket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;



    public SocketConnection(BluetoothSocket bluetoothSocket) {
        this.bluetoothSocket = bluetoothSocket;
        try {
            this.outputStream = bluetoothSocket.getOutputStream();
            this.inputStream = bluetoothSocket.getInputStream();
            dataInputStream = new DataInputStream(inputStream);
            dataOutputStream = new DataOutputStream(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    String getTextFromStream(){
        byte[] buffer = new byte[256];
        try {
            int bytes = dataInputStream.read(buffer);
            return new String(buffer, 0, bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    int sendTextToStream(String text){
        try {
            dataOutputStream.writeChars(text);
            return 1;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
