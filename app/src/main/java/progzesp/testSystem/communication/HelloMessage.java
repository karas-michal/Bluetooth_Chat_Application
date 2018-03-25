package progzesp.testSystem.communication;

import android.bluetooth.BluetoothDevice;

import java.io.Serializable;



public class HelloMessage extends TextMessage implements Serializable {
    private transient BluetoothDevice senderDevice;
    private String senderAddress;
    private int range;

    public HelloMessage(BluetoothDevice sender, String senderAddress, int range, String senderName) {
        super(senderName,"");
        this.senderDevice = sender;
        this.senderAddress = senderAddress;
        this.range = range;
    }

    public String getSenderAddress() {
        return senderAddress;
    }

    public int getRange() {
        return range;
    }

    public BluetoothDevice getSenderDevice() {
        return senderDevice;
    }
}
