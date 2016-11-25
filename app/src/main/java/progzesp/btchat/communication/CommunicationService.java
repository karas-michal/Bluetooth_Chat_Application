package progzesp.btchat.communication;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import java.util.ArrayList;
import java.util.List;

public class CommunicationService extends Service {
    private static final String ACTION_NEW_SOCKET = "progzesp.btchat.communication.action.NEW_SOCKET";
    private static final String SOCKET = "progzesp.btchat.communication.extra.PARAM1";
    private List<SocketConnection> sockets;
    public CommunicationService() {
        sockets = new ArrayList<>();

    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(Intent.ACTION_SEND.equals(action)) {
                String message = intent.getStringExtra("msg");
                for(SocketConnection s : sockets){
                    s.sendTextToStream(message);
                }
            }
            else if (ACTION_NEW_SOCKET.equals(action)){
                sockets.add((SocketConnection)intent.getSerializableExtra(SOCKET));
            }
        }
    };

    @Override
    public void onCreate() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SEND);
        filter.addAction(ACTION_NEW_SOCKET);
        registerReceiver(broadcastReceiver, filter);
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
