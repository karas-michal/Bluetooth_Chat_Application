package progzesp.btchat.communication;

import java.io.Serializable;
import java.util.Random;


public class PingMessage implements Serializable {

    private byte[] contents;
    private int ttl;
    private int originalTtl;
    private boolean response;


    public PingMessage(int size, int ttl, boolean response) {
        contents = new byte[size];
        new Random().nextBytes(contents);
        this.ttl = ttl;
        originalTtl = ttl;
        this.response = response;
    }


    public PingMessage(byte[] contents, int ttl, boolean response) {
        this.contents = contents;
        this.ttl = ttl;
        originalTtl = ttl;
        this.response = response;
    }


    public byte[] getContents() {
        return contents;
    }


    public void decrementTimeToLive() {
        ttl--;
    }


    public int getTimeToLive() {
        return ttl;
    }


    public int getOriginalTimeToLive() {
        return originalTtl;
    }


    public boolean isResponse() {
        return response;
    }

}
