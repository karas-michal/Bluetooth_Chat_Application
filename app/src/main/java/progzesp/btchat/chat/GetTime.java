package progzesp.btchat.chat;

/**
 * Created by karas on 27.01.2017.
 */

import org.apache.commons.net.time.TimeTCPClient;

import java.io.IOException;


public class GetTime {

    public static Long getTme() {
        try {
            TimeTCPClient client = new TimeTCPClient();
            try {
                // Set timeout of 60 seconds
                client.setDefaultTimeout(60000);
                // Connecting to time server
                // Other time servers can be found at : http://tf.nist.gov/tf-cgi/servers.cgi#
                // Make sure that your program NEVER queries a server more frequently than once every 4 seconds
                client.connect("nist.time.nosc.us");
                return client.getTime();
            } finally {
                client.disconnect();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}