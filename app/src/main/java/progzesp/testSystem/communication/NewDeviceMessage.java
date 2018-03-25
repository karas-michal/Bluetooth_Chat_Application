package progzesp.testSystem.communication;

import android.bluetooth.BluetoothDevice;

import java.io.Serializable;


public class NewDeviceMessage extends TextMessage implements Serializable {
    private transient BluetoothDevice senderDevice;
    private String senderAddress;
    private String newDeviceAddress;
    private String newDeviceName;
    private int range;

    public NewDeviceMessage(String senderAddress,
                            String newDeviceAddress, String newDeviceName, int range) {
        super("", "");
        this.senderAddress = senderAddress;
        this.newDeviceAddress = newDeviceAddress;
        this.newDeviceName = newDeviceName;
        this.range = range;
    }

    public String getSenderAddress() {
        return senderAddress;
    }

    public int getRange() {
        return range;
    }

    public String getNewDeviceAddress() {
        return newDeviceAddress;
    }

    public String getNewDeviceName() {
        return newDeviceName;
    }
}
