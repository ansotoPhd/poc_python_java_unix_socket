import org.newsclub.net.unix.AFUNIXSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;
import org.newsclub.net.unix.AFUNIXSocketException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class UnixSocketMsgRetriever extends Thread{

    private File socketFile;
    private AFUNIXSocket sock;
    private BlockingQueue<String> queue;
    private String startSubString = "<trepan_cmd>";
    private String endSubString = "</trepan_cmd>";

    public UnixSocketMsgRetriever(
            String unixSocketPath, BlockingQueue<String> queue
    ) throws IOException {
        this.socketFile = new File(unixSocketPath);
        this.sock = AFUNIXSocket.newInstance();
        this.queue = queue;
    }

    @Override
    public void run() {

        try {
            // · Connect to unix socket
            int tries = 0;
            boolean connected = false;
            while (tries < 10 && !connected){
                tries += 1;
                try {
                    sock.connect(new AFUNIXSocketAddress(socketFile));
                    connected = true;
                } catch (AFUNIXSocketException e) {
                    Thread.sleep(500);
                }
            }
            if(tries==10){
                System.out.println("Cannot connect to server. Have you started it?");
                System.out.flush();
                throw new Exception("Cannot connect to server. Have you started it?");
            }

            System.out.println("Connected");
            // · Getting socket input stream
            InputStream is = sock.getInputStream();
            // · Split data received through socket into msg with help of tags
            String buffer = "";
            byte[] buf = new byte[1024];
            int startIndex = 0; boolean startFlag = false;
            int endIndex = 0; boolean endFlag = false;
            while(true) {
                int read = is.read(buf);
                if(read > 0)
                    buffer += new String(buf, 0, read);
                boolean found=true;
                while (found && !buffer.isEmpty()){
                    found = false;
                    if(!startFlag) {
                        startIndex = buffer.indexOf(startSubString);
                        if (startIndex != -1)
                            startFlag = true;
                    }
                    if(!endFlag) {
                        endIndex = buffer.indexOf(endSubString);
                        if (endIndex != -1)
                            endFlag = true;
                    }
                    if(startFlag && endFlag){
                        found = true;
                        startFlag = false;
                        endFlag = false;
                        String msg = buffer.substring(startIndex + startSubString.length(), endIndex);
                        System.out.println("*********** socket *******************************");
                        System.out.println(msg);
                        System.out.println("********** /socket *******************************\n");
                        this.queue.put(msg);
                        buffer = buffer.substring(endIndex+endSubString.length(), buffer.length());
                    }
                }
            }
        }catch (Exception e){
            System.out.println(e);
        }
    }
}
