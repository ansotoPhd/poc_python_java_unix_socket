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

    public UnixSocketMsgRetriever(String unixSocketPath, BlockingQueue<String> queue ) throws IOException {
        this.socketFile = new File(unixSocketPath);
        this.sock = AFUNIXSocket.newInstance();
        this.queue = queue;
    }

    @Override
    public void run() {

        try {
            // · Connect to unix socket
            try {
                sock.connect(new AFUNIXSocketAddress(socketFile));
            } catch (AFUNIXSocketException e) {
                System.out.println("Cannot connect to server. Have you started it?");
                System.out.flush();
                throw e;
            }
            System.out.println("Connected");
            // · Getting socket input stream
            InputStream is = sock.getInputStream();
            // · Split data received through socket into msg with help of tags
            String buffer = "";
            List<String> msg_list = new ArrayList<String>();
            byte[] buf = new byte[5];
            String startSubString = "<msg>"; int startIndex = 0; boolean startFlag = false;
            String endSubString = "</msg>"; int endIndex = 0; boolean endFlag = false;
            while(true) {
                int read = is.read(buf);
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
                        msg_list.add(msg);
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
