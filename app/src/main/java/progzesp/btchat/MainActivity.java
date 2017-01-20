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
import progzesp.btchat.chat.ChatService;
import progzesp.btchat.chat.LostConnectionListener;
import progzesp.btchat.chat.NewMessageListener;
import progzesp.btchat.chat.RemoteDevice;
import progzesp.btchat.connection.ConnectionProvider;
import progzesp.btchat.connection.NewConnectionListener;

import java.io.IOException;


public class MainActivity extends AppCompatActivity implements NewMessageListener, LostConnectionListener {

    private ChatService myService;
    private ConnectionProvider connectionProvider;
    private boolean connected = false;
    private String bluetoothName;
    private BluetoothAdapter bluetoothAdapter;

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
    public void onNewMessage(RemoteDevice device, String text) {
        addMessage(text, (TextView) findViewById(R.id.textView));
    }


    @Override
    public void onLostConnection(BluetoothDevice device) {
        addMessage(getResources().getString(R.string.disconnected_from) + " " + device.getName(), (TextView) findViewById(R.id.textView));
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
                if(!input.getText().toString().matches("")){
                    String message = bluetoothName + ": " + input.getText().toString();
                    addMessage(message, view);

                    try {
                        if (connected)
                            myService.sendMessage(message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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
        Intent serviceIntent = new Intent(MainActivity.this, ChatService.class);
        startService(serviceIntent);
        bindService(serviceIntent, mConnection,  Context.BIND_AUTO_CREATE);


        connectionProvider = new ConnectionProvider(this, bluetoothAdapter);
        connectionProvider.setNewConnectionListener(new NewConnectionListener() {
            @Override
            public void onNewConnection(final BluetoothSocket socket) {
                BluetoothDevice device = socket.getRemoteDevice();
                addMessage(getResources().getString(R.string.connected_to) + " " + device.getName(), view);
                connected = true;
                myService.onNewConnection(socket);
            }
        });

        if (bluetoothAdapter.isEnabled()) {
            bluetoothName = bluetoothAdapter.getName();
        } else {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, 1);
        }
    }


    private void addMessage(String _string, TextView _view) {
        final String string = _string;
        final TextView view = _view;
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
            ChatService.LocalBinder binder = (ChatService.LocalBinder) iBinder;
            myService = binder.getServiceInstance();
            myService.registerClient(MainActivity.this);
        }


        @Override
        public void onServiceDisconnected(ComponentName arg0) {

        }
    };

}
