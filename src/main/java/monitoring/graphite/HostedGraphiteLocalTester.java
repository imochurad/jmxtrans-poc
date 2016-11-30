package monitoring.graphite;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class HostedGraphiteLocalTester {

    public static void main(String[] args) throws UnknownHostException, IOException, InterruptedException {

        String apikey = "blah-blah";
        Socket conn = new Socket("carbon.hostedgraphite.com", 2003);
        Random rand = new Random();
        try {
            while (true) {
                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
                dos.writeBytes(String.format("%s.%s %d\n", apikey, "server_name.metrics_name", rand.nextInt(1000)));
                System.out.println(".... and sent");
                TimeUnit.SECONDS.sleep(30);
            }
        } finally {
            conn.close();
        }
    }

}
