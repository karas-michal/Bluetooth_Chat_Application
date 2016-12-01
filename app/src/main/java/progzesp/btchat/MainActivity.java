package progzesp.btchat;

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

import progzesp.btchat.chat.ChatService;

public class MainActivity extends AppCompatActivity implements ChatService.Callbacks {

    private ChatService myService;

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
                // User chose the "Favorite" action, mark the current item
                // as a favorite...
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    private void addMessage(String s, TextView view){
        String newLine = "You: "+s+"\n";
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
                    addMessage(input.getText().toString(),view);
                    myService.sendMessage(input.getText().toString());
                    input.setText("");
                }
            }
        });

        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    if(!input.getText().toString().matches("")){
                        addMessage(input.getText().toString(),view);
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
