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
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import progzesp.btchat.chat.ChatService;
import progzesp.btchat.communication.CommunicationService;
import progzesp.btchat.connection.ConnectionProvider;
import progzesp.btchat.connection.NewConnectionListener;

public class MainActivity extends AppCompatActivity implements ChatService.Callbacks {

    private ChatService myService;
    private ConnectionProvider connectionProvider;
    private boolean connected = false;
    private CommunicationService communicationService;

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
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Button button = (Button) findViewById(R.id.button);
        final EditText input = (EditText) findViewById(R.id.editText);
        final TextView view = (TextView) findViewById(R.id.textView);
        view.setMovementMethod(new ScrollingMovementMethod());

        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        final String bluetoothName = adapter.getName();
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
        startService(serviceIntent); //Starting the service
        bindService(serviceIntent, mConnection,  Context.BIND_AUTO_CREATE); //Binding to the service!
        Toast.makeText(MainActivity.this, "Button checked", Toast.LENGTH_SHORT).show();


        connectionProvider = new ConnectionProvider(this, adapter);
        communicationService = new CommunicationService();
        connectionProvider.setNewConnectionListener(communicationService);
        communicationService.setLostConnectionListener(connectionProvider);
        connectionProvider.setNewConnectionListener(new NewConnectionListener() {
            @Override
            public void onNewConnection(final BluetoothSocket socket) {
                BluetoothDevice device = socket.getRemoteDevice();
                addMessage("Connected to " + device.getName(), view);
                connected = true;
                try {
                    myService.setBluetoothSocket(socket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        if (!adapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, 1);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {

            } else {
                Toast.makeText(this, "Bluetooth not enabled", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    public void updateClient(String text) {
        addMessage(text, (TextView) findViewById(R.id.textView));
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Toast.makeText(MainActivity.this, "onServiceConnected called", Toast.LENGTH_SHORT).show();
            // We've binded to LocalService, cast the IBinder and get LocalService instance
            ChatService.LocalBinder binder = (ChatService.LocalBinder) iBinder;
            myService = binder.getServiceInstance(); //Get instance of your service!
            myService.registerClient(MainActivity.this); //Activity register in the service as client for callabcks!
        }


        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Toast.makeText(MainActivity.this, "onServiceDisconnected called", Toast.LENGTH_SHORT).show();
        }
    };

}
