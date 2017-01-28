package progzesp.btchat;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.*;
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
import progzesp.btchat.chat.ChatMessage;
import progzesp.btchat.chat.ChatService;
import progzesp.btchat.chat.GetTime;
import progzesp.btchat.chat.LostConnectionListener;
import progzesp.btchat.chat.NewChatMessageListener;
import progzesp.btchat.connection.ConnectionProvider;
import progzesp.btchat.connection.NewConnectionListener;

import static progzesp.btchat.chat.messageType.ANSWER;
import static progzesp.btchat.chat.messageType.PING;


public class MainActivity extends AppCompatActivity implements NewChatMessageListener, LostConnectionListener {

    private ChatService myService;
    private ConnectionProvider connectionProvider;
    private String bluetoothName;
    private BluetoothAdapter bluetoothAdapter;
    private SharedPreferences settings;
    private int devicePosition;
    private boolean podajIlosc = false;
    private boolean podajPozycje = false;
    private int chainLength;
    private long time;
    private long timeServerMinusSystem;
    private int messageLength;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    private String generateString(int length, char c){
        StringBuffer outputBuffer = new StringBuffer(length);
        for (int i = 0; i < length; i++){
            outputBuffer.append(c);
        }
        return outputBuffer.toString();
    }

    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch(NumberFormatException e) {
            return false;
        } catch(NullPointerException e) {
            return false;
        }
        // only got here if we didn't return false
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
    public void onNewChatMessage(ChatMessage message) {
        if (message.getTtl() == 0 && message.getType() == PING){
            long currTime = System.currentTimeMillis()-timeServerMinusSystem;
            long timeDiff = message.getTime()-currTime;
            addMessage("czas podróży: "+timeDiff, (TextView) findViewById(R.id.textView));
            String msg = generateString(messageLength, 'a');
            ChatMessage rMessage = new ChatMessage(bluetoothName, msg, message.getTime(), chainLength-2, ANSWER);
            myService.sendChatMessage(rMessage);
            addMessage("wyslano odpowiedz", (TextView) findViewById(R.id.textView));
        }
        else if(message.getTtl()== 0 && message.getType() == ANSWER){
            long timeDiff = System.currentTimeMillis()-timeServerMinusSystem-time;
            addMessage("odebrano odpowiedz\nczas: "+timeDiff, (TextView) findViewById(R.id.textView));
        }
        else
            addMessage("błąd", (TextView) findViewById(R.id.textView));
        //addMessage(message.toString(), (TextView) findViewById(R.id.textView));
    }


    @Override
    public void onLostConnection(BluetoothDevice device) {
        addMessage(getResources().getString(R.string.disconnected_from) + " " + device.getName(), (TextView) findViewById(R.id.textView));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.settings = getPreferences(MODE_PRIVATE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Button button = (Button) findViewById(R.id.button);
        final EditText input = (EditText) findViewById(R.id.editText);
        final TextView view = (TextView) findViewById(R.id.textView);
        view.setMovementMethod(new ScrollingMovementMethod());
        timeServerMinusSystem = 0;//GetTime.getTme();
        timeServerMinusSystem = System.currentTimeMillis()-timeServerMinusSystem;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        final View.OnClickListener onClickListener = new View.OnClickListener() {
            public void onClick(View v) {
                if(!input.getText().toString().matches("") && !podajIlosc){
                    String msg = input.getText().toString();
                    int number = Integer.valueOf(msg);
                    addMessage("liczba urządzeń w łańcuchu: "+String.valueOf(number), view);
                    //ChatMessage message = new ChatMessage(bluetoothName, input.getText().toString());
                    //addMessage(message.toString(), view);
                    //myService.sendChatMessage(message);
                    chainLength = number;
                    podajIlosc = true;
                    input.setText("");
                }
                else if(podajIlosc && input.getText().toString().matches("ping")){
                    input.setText("");
                    time = System.currentTimeMillis()-timeServerMinusSystem;
                    String msg = generateString(messageLength, 's');
                    ChatMessage message = new ChatMessage(bluetoothName, msg, time, chainLength-2, PING);
                    myService.sendChatMessage(message);
                    addMessage("rozpoczęto test",view);
                }
                else if(podajIlosc && isInteger(input.getText().toString())){
                    messageLength = Integer.valueOf(input.getText().toString());
                    addMessage("zmiana wielkosci pakietu do "
                            +String.valueOf(messageLength)+" znakow",view);
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
        bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);


        connectionProvider = new ConnectionProvider(this, bluetoothAdapter);
        connectionProvider.setNewConnectionListener(new NewConnectionListener() {
            @Override
            public void onNewConnection(final BluetoothSocket socket) {
                BluetoothDevice device = socket.getRemoteDevice();
                addMessage(getResources().getString(R.string.connected_to) + " " + device.getName(), view);
                myService.onNewConnection(socket);
            }
        });

        if (bluetoothAdapter.isEnabled()) {
            bluetoothName = bluetoothAdapter.getName();
        } else {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, 1);
        }
        input.setText("2");
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


    public void onPause(){
        super.onPause();
        //SharedPreferences.Editor editor = this.settings.edit();
        //final TextView view = (TextView) findViewById(R.id.textView);
        //Gson gson = new Gson();
        //TODO cale myservice jest duze jesli trzeba jakies pole to mozna getterem wyciagnac i dolozyc
        //String json = gson.toJson(this.myService);
        //editor.putString("ChatService", json);
        //editor.putString("ChatMessages", view.getText().toString() );
        //editor.apply();
    }


    public void onResume(){
        super.onResume();
        //Gson gson = new Gson();
        //String json = settings.getString("ChatService", "");
        //this.myService = gson.fromJson(json, ChatService.class);
        //final TextView view = (TextView) findViewById(R.id.textView);
        //view.setText(settings.getString("ChatMessages", ""));
    }

}
