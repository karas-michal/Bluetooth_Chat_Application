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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ChatService extends Service {
    OutputStream outputStream;
    InputStream inputStream;
    NotificationManager notificationManager;
    NotificationCompat.Builder mBuilder;
    BluetoothSocket socket;
    Callbacks activity;
    private final IBinder mBinder = new LocalBinder();
    Handler handler = new Handler();
    byte[] buffer = new byte[1024];
    int bytes = 0;
    Runnable serviceRunnable = new Runnable() {
        @Override
        public void run() {
            System.out.println(bytes);
            try {
                if (inputStream.available()>0)
                    bytes = inputStream.read(buffer, 0, 1024 - bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (bytes >0 ) {
                activity.updateClient(new String(buffer)); //Update Activity (client) by the implementd callback
                bytes = 0;
            }
            handler.postDelayed(serviceRunnable, 1000);
        }
    };


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //Do what you need in onStartCommand when service has been started
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    //returns the instance of the service
    public class LocalBinder extends Binder {
        public ChatService getServiceInstance(){
            return ChatService.this;
        }
    }

    //Here Activity register to the service as Callbacks client
    public void registerClient(Activity activity){
        this.activity = (Callbacks)activity;
    }

    public void sendMessage(String text) throws IOException {
        //TODO
        outputStream.write(text.getBytes());
        Toast.makeText(getApplicationContext(), "Message sent", Toast.LENGTH_SHORT).show();
    }

    public void setBluetoothSocket(BluetoothSocket s) throws IOException {
        this.socket = s;
        this.inputStream = socket.getInputStream();
        this.outputStream = socket.getOutputStream();
        handler.postDelayed(serviceRunnable, 0);
    }

    public void stopCounter(){
        handler.removeCallbacks(serviceRunnable);
    }


    //callbacks interface for communication with service clients!
    public interface Callbacks{
        void updateClient(String data);
    }
}