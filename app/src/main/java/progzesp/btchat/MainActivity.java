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
            case R.id.action_settings:
                //TODO
                // User chose the "Settings" item, show the app settings UI...
                return true;

            case R.id.action_quick_sync:
                //connectionProvider.startAcceptingConnections();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    private void addMessage(String s, TextView view, String id){
        String newLine = id+": "+s+"\n";
        view.setText(view.getText()+newLine);
        final int scrollAmount = view.getLayout().getLineTop(view.getLineCount()) - view.getHeight();
        // if there is no need to scroll, scrollAmount will be <=0
        if (scrollAmount > 0)
            view.scrollTo(0, scrollAmount);
        else
            view.scrollTo(0, 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Button button = (Button) findViewById(R.id.button);
        final EditText input = (EditText) findViewById(R.id.editText);
        final TextView view = (TextView) findViewById(R.id.textView);
        view.setMovementMethod(new ScrollingMovementMethod());
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(!input.getText().toString().matches("")){
                    addMessage(input.getText().toString(),view, "You");

                    try {
                        if (connected)
                            myService.sendMessage(input.getText().toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    input.setText("");
                }
            }
        });

        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    if(!input.getText().toString().matches("")){
                        addMessage(input.getText().toString(),view, "You");

                        input.setText("");
                    }
                }
                return actionId == EditorInfo.IME_ACTION_DONE;
            }
        });
        Intent serviceIntent = new Intent(MainActivity.this, ChatService.class);
        startService(serviceIntent); //Starting the service
        bindService(serviceIntent, mConnection,  Context.BIND_AUTO_CREATE); //Binding to the service!
        Toast.makeText(MainActivity.this, "Button checked", Toast.LENGTH_SHORT).show();


        final TextView connectionView = (TextView) findViewById(R.id.textView2);
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        connectionProvider = new ConnectionProvider(this, adapter);
        communicationService = new CommunicationService();
        connectionProvider.setNewConnectionListener(communicationService);
        communicationService.setLostConnectionListener(connectionProvider);
        connectionProvider.setNewConnectionListener(new NewConnectionListener() {
            @Override
            public void onNewConnection(final BluetoothSocket socket) {
                final BluetoothDevice device = socket.getRemoteDevice();
                runOnUiThread(new Runnable() {
                                  @Override
                                  public void run() {
                                      connectionView.setText("Connected to " + device.getName());
                                      connected = true;
                                      try {
                                          myService.setBluetoothSocket(socket);
                                      } catch (IOException e) {
                                          e.printStackTrace();
                                      }
                                  }
                              });
            }
        });
        final Button connectButton = (Button) findViewById(R.id.connectButton);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectionProvider.findDevices();
            }
        });
        final Button hostButton = (Button) findViewById(R.id.hostButton);
        hostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectionProvider.acceptConnections();
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
    public void updateClient(String text, String id) {
        addMessage(text, (TextView) findViewById(R.id.textView), id);
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
