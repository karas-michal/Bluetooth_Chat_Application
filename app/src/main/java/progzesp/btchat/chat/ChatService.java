package progzesp.btchat.chat;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;

public class ChatService extends Service {
    NotificationManager notificationManager;
    NotificationCompat.Builder mBuilder;
    Callbacks activity;
    private final IBinder mBinder = new LocalBinder();
    Handler handler = new Handler();
    Runnable serviceRunnable = new Runnable() {
        @Override
        public void run() {
            activity.updateClient("SampleText"); //Update Activity (client) by the implementd callback
            handler.postDelayed(this, 1000);
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

    public void sendMessage(String text){
        //TODO
        handler.postDelayed(serviceRunnable, 0);
        Toast.makeText(getApplicationContext(), "msg send", Toast.LENGTH_SHORT).show();
    }

    public void stopCounter(){
        handler.removeCallbacks(serviceRunnable);
    }


    //callbacks interface for communication with service clients!
    public interface Callbacks{
        void updateClient(String data);
    }
}