package progzesp.btchat;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.*;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import progzesp.btchat.communication.*;
import progzesp.btchat.connection.ConnectionProvider;
import progzesp.btchat.connection.NewConnectionListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity implements NewMessageListener, LostConnectionListener {

    private CommunicationService communicationService;
    private ConnectionProvider connectionProvider;
    private String bluetoothName;
    private BluetoothAdapter bluetoothAdapter;
    private Pattern pingRegex = Pattern.compile("^\\s*ping\\s*(\\d+)\\s*(\\d+)");
    private long pingTimeSent;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.connect:
                connectionProvider.findDevices();
                return true;
            case R.id.host:
                connectionProvider.acceptConnections();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                bluetoothName = bluetoothAdapter.getName();
            } else {
                Toast.makeText(this, getResources().getString(R.string.bluetooth_off), Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }


    @Override
    public void onNewMessage(RemoteDevice originDevice, Object message) {
        if (message instanceof PingMessage) {
            PingMessage ping = (PingMessage) message;
            if (ping.isResponse()) {
                long timeDiff = System.currentTimeMillis() - pingTimeSent;
                addMessage(getResources().getString(R.string.ping_response_received) + " " + timeDiff + " ms");
            } else {
                PingMessage response = new PingMessage(ping.getContents(), ping.getOriginalTimeToLive(), true);
                communicationService.send(response, originDevice);
                addMessage(getResources().getString(R.string.ping_response_sent));
            }
        } else if (message instanceof ChatMessage) {
            addMessage(message.toString());
        }
    }


    @Override
    public void onLostConnection(BluetoothDevice device) {
        addMessage(getResources().getString(R.string.disconnected_from) + " " + device.getName());
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Button button = (Button) findViewById(R.id.button);
        final EditText input = (EditText) findViewById(R.id.editText);
        final TextView view = (TextView) findViewById(R.id.textView);
        view.setMovementMethod(new ScrollingMovementMethod());
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        final View.OnClickListener onClickListener = new View.OnClickListener() {
            public void onClick(View v) {
                Matcher m = pingRegex.matcher(input.getText());
                if (m.find()) {
                    int ttl = Integer.parseInt(m.group(1));
                    int length = Integer.parseInt(m.group(2)) * 1024;
                    PingMessage message = new PingMessage(length, ttl, false);
                    pingTimeSent = System.currentTimeMillis();
                    communicationService.sendToAll(message);
                    addMessage(getResources().getString(R.string.ping_sent) + " " + length / 1024 + " KB");
                } else {
                    ChatMessage message = new ChatMessage(bluetoothName, input.getText().toString());
                    communicationService.sendToAll(message);
                    addMessage(message.toString());
                    input.setText("");
                }
            }
        };
        button.setOnClickListener(onClickListener);
        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    onClickListener.onClick(button);
                }
                return actionId == EditorInfo.IME_ACTION_DONE;
            }
        });

        Intent serviceIntent = new Intent(MainActivity.this, CommunicationService.class);
        startService(serviceIntent);
        bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);

        connectionProvider = new ConnectionProvider(this, bluetoothAdapter);
        connectionProvider.setNewConnectionListener(new NewConnectionListener() {
            @Override
            public void onNewConnection(final BluetoothSocket socket) {
                BluetoothDevice device = socket.getRemoteDevice();
                addMessage(getResources().getString(R.string.connected_to) + " " + device.getName());
                communicationService.onNewConnection(socket);
            }
        });

        if (bluetoothAdapter.isEnabled()) {
            bluetoothName = bluetoothAdapter.getName();
        } else {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, 1);
        }
    }


    private void addMessage(String _string) {
        final String string = _string;
        final TextView view = (TextView) findViewById(R.id.textView);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                view.setText(view.getText() + string + "\n");
                int scrollAmount = view.getLayout().getLineTop(view.getLineCount()) - view.getHeight();
                if (scrollAmount > 0) {
                    view.scrollTo(0, scrollAmount);
                } else {
                    view.scrollTo(0, 0);
                }
            }
        });
    }


    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            CommunicationService.LocalBinder binder = (CommunicationService.LocalBinder) iBinder;
            communicationService = binder.getServiceInstance();
            communicationService.registerClient(MainActivity.this);
        }


        @Override
        public void onServiceDisconnected(ComponentName arg0) {

        }

    };

}
