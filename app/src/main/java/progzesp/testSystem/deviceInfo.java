package progzesp.testSystem;

import android.bluetooth.BluetoothDevice;



public class deviceInfo {
    private String address;
    private String name;
    private int range;
    private BluetoothDevice remoteDevice;

    public deviceInfo(String address, String name, int range, BluetoothDevice remoteDevice) {
        this.address = address;
        this.name = name;
        this.range = range;
        this.remoteDevice = remoteDevice;
    }

    public String getAddress() {
        return address;
    }

    public String getName() {
        return name;
    }

    public int getRange() {
        return range;
    }

    public BluetoothDevice getRemoteDevice() {
        return remoteDevice;
    }

    public void setRange(int range) {
        this.range = range;
    }
}
