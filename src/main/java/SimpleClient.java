import org.newsclub.net.unix.AFUNIXSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;
import org.newsclub.net.unix.AFUNIXSocketException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


public class SimpleClient {

    public static void main(String[] args) throws IOException {

        BlockingQueue<String> queue = new ArrayBlockingQueue(10);
        UnixSocketMsgRetriever msgRetrieverThread = new UnixSocketMsgRetriever("/tmp/socketname", queue);
        msgRetrieverThread.start();
        while(true){
            try {
                System.out.println(queue.take());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}