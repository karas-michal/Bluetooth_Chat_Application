package progzesp.testSystem;



public class IndirectDevice {
    private String address;
    private String throughAddress;
    private int range;

    public IndirectDevice(String address, String throughAddress) {
        this.address = address;
        this.throughAddress = throughAddress;
    }

    public String getThroughAddress() {
        return throughAddress;
    }

    public String getAddress() {
        return address;
    }

    public void setRange(int range) {
        this.range = range;
    }
}
