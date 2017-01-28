package progzesp.btchat.chat;

import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import progzesp.btchat.connection.NewConnectionListener;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class ChatService extends Service implements NewConnectionListener, NewMessageListener, LostConnectionListener {

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
    public void onNewConnection(BluetoothSocket socket) {
        remove(socket.getRemoteDevice());
        remoteDevices.add(new RemoteDevice(socket, this, this));
    }


    @Override
    public void onNewMessage(RemoteDevice originDevice, Object message) {
        for (RemoteDevice device : remoteDevices) {
            if (device != originDevice) {
                device.send((Serializable) message);
            }
        }
        if (message instanceof ChatMessage) {
            newChatMessageListener.onNewChatMessage((ChatMessage) message);
        }
    }

    @Override
    public void onLostConnection(BluetoothDevice device) {
        remove(device);
        lostConnectionListener.onLostConnection(device);
    }


    public class LocalBinder extends Binder {
        public ChatService getServiceInstance(){
            return ChatService.this;
        }
    }


    public void registerClient(Activity activity) {
        this.newChatMessageListener = (NewChatMessageListener) activity;
        this.lostConnectionListener = (LostConnectionListener) activity;
    }


    public void sendChatMessage(ChatMessage message) {
        for (RemoteDevice device : remoteDevices) {
            device.send(message);
        }
    }


    private void remove(BluetoothDevice device) {
        for (int i = 0; i < remoteDevices.size(); i++) {
            if (remoteDevices.get(i).getAddress().equals(device.getAddress())) {
                remoteDevices.remove(i);
                break;
            }
        }
    }

}