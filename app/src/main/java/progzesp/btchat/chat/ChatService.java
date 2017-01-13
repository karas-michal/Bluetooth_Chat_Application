package progzesp.btchat.chat;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;
import progzesp.btchat.connection.NewConnectionListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

public class ChatService extends Service implements NewConnectionListener, NewMessageListener {

    NewMessageListener activity;
    private final IBinder mBinder = new LocalBinder();
    Handler handler = new Handler();
    List<RemoteDevice> remoteDevices = new LinkedList<>();


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Do what you need in onStartCommand when service has been started
        return START_NOT_STICKY;
    }


    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    @Override
    public void onNewConnection(BluetoothSocket socket) {
        for (RemoteDevice device : remoteDevices) {
            if (device.getAddress().equals(socket.getRemoteDevice().getAddress())) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }
        }
        remoteDevices.add(new RemoteDevice(handler, socket, this));
    }


    @Override
    public void onNewMessage(RemoteDevice originDevice, String message) {
        for (RemoteDevice device : remoteDevices) {
            if (device != originDevice) {
                device.send(message);
            }
        }
        activity.onNewMessage(originDevice, message);
    }


    //returns the instance of the service
    public class LocalBinder extends Binder {
        public ChatService getServiceInstance(){
            return ChatService.this;
        }
    }


    //Here Activity register to the service as NewMessageListener client
    public void registerClient(Activity activity){
        this.activity = (NewMessageListener)activity;
    }


    public void sendMessage(String message) throws IOException {
        for (RemoteDevice device : remoteDevices) {
            device.send(message);
        }
    }

}