package progzesp.testSystem.communication;

import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import progzesp.testSystem.connection.NewConnectionListener;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * Serwis odpowiedzialny za przekazywanie wiadomości pomiędzy aktywnością
 * a podłączonymi urządzeniami
 */
public class CommunicationService extends Service implements NewConnectionListener, NewMessageListener, LostConnectionListener {

    private NewChatMessageListener newChatMessageListener;
    private LostConnectionListener lostConnectionListener;
    private IBinder mBinder = new LocalBinder();
    private List<RemoteDevice> remoteDevices = new LinkedList<>();


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }


    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    @Override
    public void onNewConnection(BluetoothSocket socket, int rssi, int flag) {
        remove(socket.getRemoteDevice());
        remoteDevices.add(new RemoteDevice(socket, this, this, rssi));
        String macAddress = android.provider.Settings.Secure.getString(getApplicationContext().getContentResolver(), "bluetooth_address");
        sendNewDeviceMessage(socket.getRemoteDevice().getAddress(), rssi, macAddress);
        informNewDevice(socket.getRemoteDevice().getAddress(), macAddress);
    }

    private void informNewDevice(String newDeviceAddress, String myMacAddress) {
        int number = 0;
        for (int i = 0; i < remoteDevices.size(); i++) {
            if (remoteDevices.get(i).getAddress().equals(newDeviceAddress)) {
                number = i;
                break;
            }
        }
        for (int i = 0; i < remoteDevices.size(); i++) {
            if (!remoteDevices.get(i).getAddress().equals(newDeviceAddress)) {
                NewDeviceMessage message = new NewDeviceMessage(myMacAddress,remoteDevices.get(i).getAddress(),
                        "", 0);//TODO new device name
                remoteDevices.get(number).send(message);
            }
        }
    }


    @Override
    public void onNewMessage(RemoteDevice originDevice, Object message) {

        if (message instanceof  HelloMessage) {
            newChatMessageListener.onHelloMessage((HelloMessage) message);
        } else
        if (message instanceof NewDeviceMessage){
            newChatMessageListener.onNewDeviceMessage((NewDeviceMessage) message);
            for (RemoteDevice device : remoteDevices) {
                if (device != originDevice) {
                    device.send((Serializable) message);
                }
            }
        } else
        if (message instanceof TextMessage) {
            newChatMessageListener.onNewTextMessage((TextMessage) message);
            for (RemoteDevice device : remoteDevices) {
                if (device != originDevice) {
                    device.send((Serializable) message);
                }
            }
        }
    }

    /**
     * Metoda usuwająca urządzenie z listy podłączonych urządzeń po
     * zerwaniu połączenia i wywołująca callback z aktywności.
     * @param device urządznie które przerwało połączenie
     */
    @Override
    public void onLostConnection(BluetoothDevice device) {
        remove(device);
        lostConnectionListener.onLostConnection(device);
    }

    public void sendNewDeviceMessage(String newAddress, int range, String senderAddress) {
        for (int i = 0; i < remoteDevices.size(); i++) {
            if (!remoteDevices.get(i).getAddress().equals(senderAddress) &&
                    !remoteDevices.get(i).getAddress().equals(newAddress)) {
                NewDeviceMessage message = new NewDeviceMessage(senderAddress, newAddress,
                        "", range);//TODO new device name
                remoteDevices.get(i).send(message);
            }
        }
    }

    public void sendHelloMessage(HelloMessage helloMessage, BluetoothSocket socket) {
        BluetoothDevice device = socket.getRemoteDevice();
        for (int i = 0; i < remoteDevices.size(); i++) {
            if (remoteDevices.get(i).getAddress().equals(device.getAddress())) {
                remoteDevices.get(i).send(helloMessage);
                break;
            }
        }
    }


    public class LocalBinder extends Binder {
        public CommunicationService getServiceInstance(){
            return CommunicationService.this;
        }
    }

    /**
     * rejestruje aktywność jako Listener wiadomości czatu
     * @param activity actywność spełniająca NewChatMessageListener i
     *                 LostConnection Listener
     */
    public void registerClient(Activity activity) {
        this.newChatMessageListener = (NewChatMessageListener) activity;
        this.lostConnectionListener = (LostConnectionListener) activity;
    }

    /**
     * Wysyła wiadomośc do wszystkich podłączonych urządzeń
     * @param message wiadomość do wysłania
     */
    public void sendTextMessage(TextMessage message) {
        for (RemoteDevice device : remoteDevices) {
            device.send(message);
        }
    }

    /**
     * Usuwa podane urządzenie jeśli już istnieje w liście podłączonych urządzeń
     * @param device urządznie do usunięcia
     */
    private void remove(BluetoothDevice device) {
        for (int i = 0; i < remoteDevices.size(); i++) {
            if (remoteDevices.get(i).getAddress().equals(device.getAddress())) {
                remoteDevices.remove(i);
                break;
            }
        }
    }

}