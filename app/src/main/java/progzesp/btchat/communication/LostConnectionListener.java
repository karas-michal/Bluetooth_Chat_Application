package progzesp.btchat.communication;

import android.bluetooth.BluetoothDevice;

/**
 * Created by Krzysztof on 2017-01-12.
 */
public interface LostConnectionListener {

    // Ten listener wywoływany jest przez moduł komunikacji. Implementuje go moduł połączenia.
    void onLostConnection(BluetoothDevice device);

}
