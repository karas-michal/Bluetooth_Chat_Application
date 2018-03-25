package progzesp.testSystem.communication;

import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Klasa reprezentująca urządznie podłączone, instancja dla każdego
 * podłączonego urządzenia działa w tle odbierając wiadomości i
 * wysyłając za pomocą callbacków.
 */
public class RemoteDevice implements Runnable {
    private BluetoothSocket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private Thread thread;
    private NewMessageListener newMessageListener;
    private LostConnectionListener lostConnectionListener;
    private int  rssi;


    public RemoteDevice(BluetoothSocket socket, NewMessageListener nmListener, LostConnectionListener lcListener,
        int rssi) {
        this.rssi = rssi;
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

    /**
     * Metoda sprawdza strumień wejsciowy czy istnieje jakaś czekająca
     * wiadomość.
     */
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

    /**
     * Wstawia wiadomość do strumienia wyjścia danego urządzenia.
     * @param message wiadomość do wysłania do urządzenia
     */
    public void send(Serializable message) {
        try {
            outputStream.writeObject(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @return adres sprzętowy podłączonego urządzenia BT.
     */
    public String getAddress() {
        return socket.getRemoteDevice().getAddress();
    }

}