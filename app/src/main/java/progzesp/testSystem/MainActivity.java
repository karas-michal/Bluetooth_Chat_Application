package progzesp.testSystem;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.*;
import android.os.Bundle;
import android.os.IBinder;
import android.support.multidex.MultiDex;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.*;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import progzesp.testSystem.communication.TextMessage;
import progzesp.testSystem.communication.CommunicationService;
import progzesp.testSystem.communication.HelloMessage;
import progzesp.testSystem.communication.LostConnectionListener;
import progzesp.testSystem.communication.NewChatMessageListener;
import progzesp.testSystem.communication.NewDeviceMessage;
import progzesp.testSystem.connection.ConnectionProvider;
import progzesp.testSystem.connection.DeviceFinder;
import progzesp.testSystem.connection.DiscoveryFinishedListener;
import progzesp.testSystem.connection.NewConnectionListener;
import progzesp.testSystem.connection.NewDeviceListener;


public class MainActivity extends AppCompatActivity implements NewChatMessageListener, LostConnectionListener {

    private CommunicationService myService;
    private ConnectionProvider connectionProvider;
    private String bluetoothName;
    private BluetoothAdapter bluetoothAdapter;
    private ArrayList<deviceInfo> directlyConnected;
    private ArrayList<IndirectDevice> indirectlyConnected;

    /**
     * Inicjaja zawartości standardowego menu opcji aktywności
     * @param menu
     * @return true aby wyświetlić menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    /**
     * Ustalenie akcji wykonywanych po wyborze opcji z menu
     * @param item
     * @return true aby zmienić opcje
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.connect:
                int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
                connectionProvider.findDevices();
                return true;
            case R.id.host:
                connectionProvider.acceptConnections();
                final DeviceFinder finder = new DeviceFinder(bluetoothAdapter, getApplicationContext(),
                        new NewDeviceListener() {
                    @Override
                    public void onNewDevice(BluetoothDevice device, int rssi) {
                        for (deviceInfo a : directlyConnected) {
                            if (a.getAddress().equals(device.getAddress())){
                                a.setRange(rssi);
                            }
                        }
                        for (IndirectDevice a : indirectlyConnected) {
                            if (a.getAddress().equals(device.getAddress())){
                                a.setRange(rssi);
                            }
                        }
                    }
                }, new DiscoveryFinishedListener() {
                    @Override
                    public void onDiscoveryFinished() {

                    }
                });
                finder.startDiscovery();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Dodaje informację o wyłączonym BT w przypadku wyjścia z aktywności bez resultatu
     * @param requestCode
     * @param resultCode
     * @param data
     */
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

    /**
     * Odbiera wiadomości od dostawcy i wywołuje metodę wyświetlającą na ekranie.
     * @param message Wiadomość otrzymana z dostawcy
     */
    @Override
    public void onNewTextMessage(TextMessage message) { //chat to newtork
        addMessage(message.toString(), (TextView) findViewById(R.id.textView));
    }

    @Override
    public void onHelloMessage(HelloMessage message) {
        directlyConnected.add(new deviceInfo(message.getSenderAddress(), message.getSender(), message.getRange(),
                message.getSenderDevice()));
    }

    @Override
    public void onNewDeviceMessage(NewDeviceMessage message) {
        indirectlyConnected.add(new IndirectDevice(message.getNewDeviceAddress(),
                message.getSenderAddress()));
    }

    /**
     * Callback informujący użytkownika od przerwaniu połączenia z danym urządzniem.
     * @param device urządzenie które się rozłączyło
     */
    @Override
    public void onLostConnection(BluetoothDevice device) {
        addMessage(getResources().getString(R.string.disconnected_from) + " " + device.getName(), (TextView) findViewById(R.id.textView));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MultiDex.install(this);
        this.directlyConnected = new ArrayList<>();
        this.indirectlyConnected = new ArrayList<>();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Button button = (Button) findViewById(R.id.button);
        final EditText input = (EditText) findViewById(R.id.editText);
        final TextView view = (TextView) findViewById(R.id.textView);
        view.setMovementMethod(new ScrollingMovementMethod());

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        final View.OnClickListener onClickListener = new View.OnClickListener() {
            public void onClick(View v) {
                if(!input.getText().toString().matches("")) {
                    if (input.getText().toString().matches("dc")) {
                        for (deviceInfo a:directlyConnected) {
                            addMessage(a.getName()+" "+" "+a.getAddress()+" "+a.getRange(),view);
                            Log.e("info", "dc ");
                        }
                    } else if (input.getText().toString().matches("ndc")){
                        for (IndirectDevice a:indirectlyConnected) {
                            addMessage(a.getAddress()+" "+a.getThroughAddress(),view);
                            Log.e("info", "dc ");
                        }
                    } else
                    {
                        TextMessage message = new TextMessage(bluetoothName, input.getText().toString());
                        addMessage(message.toString(), view);
                        myService.sendTextMessage(message);
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
        Intent serviceIntent = new Intent(MainActivity.this, CommunicationService.class);
        startService(serviceIntent);
        bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);


        connectionProvider = new ConnectionProvider(this, bluetoothAdapter);
        connectionProvider.setNewConnectionListener(new NewConnectionListener() {
            @Override
            public void onNewConnection(final BluetoothSocket socket, int rssi, int flag) {
                BluetoothDevice device = socket.getRemoteDevice();
                addMessage(getResources().getString(R.string.connected_to) + " " + device.getName(), view);
                String macAddress = android.provider.Settings.Secure.getString(getApplicationContext().getContentResolver(), "bluetooth_address");
                HelloMessage helloMessage = new HelloMessage(device, macAddress, rssi, bluetoothName);
                myService.onNewConnection(socket, rssi, flag);
                myService.sendHelloMessage(helloMessage, socket);
            }

        });
        /* poproś w włącznie BT jeśli jest wyłączony */
        if (bluetoothAdapter.isEnabled()) {
            bluetoothName = bluetoothAdapter.getName();
        } else {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, 1);
        }
    }

    /**
     * Metoda wyświetlająca wiadomości na ekranie
     * @param _string tekt wiadomości
     * @param _view TextView w którym ma być wyświetlona wiadomość
     */
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
            CommunicationService.LocalBinder binder = (CommunicationService.LocalBinder) iBinder;
            myService = binder.getServiceInstance();
            myService.registerClient(MainActivity.this);
        }


        @Override
        public void onServiceDisconnected(ComponentName arg0) {

        }

    };


    public void onPause(){
        super.onPause();
    }

    public void onResume(){
        super.onResume();
    }

}
