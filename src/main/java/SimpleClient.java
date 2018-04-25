import org.newsclub.net.unix.AFUNIXSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;
import org.newsclub.net.unix.AFUNIXSocketException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


public class SimpleClient {

    public static boolean isAlive(Process p) {
        try {
            p.exitValue();
            return false;
        } catch (IllegalThreadStateException e) {
            return true;
        }
    }

    public static void main(String[] args) throws IOException {

        // · Process launcher --> python process acts as unix socket server
        List<String> commands = new ArrayList<String>();
//      commands.add("python");
//      commands.add("-u");
//      commands.add("/home/asoriano/Stratio/Stratio-Intelligence/stratio-intelligence/POCs/poc_python_java_unix_socket/src/main/python/SocketServer.py");
        commands.add("/home/asoriano/Stratio/Stratio-Intelligence/stratio-intelligence/eclipse-che-workspace-python/stratio-trepan/trepan/cli.py");
        commands.add("-s");
        commands.add("/tmp/trepan.sock");
        commands.add("/home/asoriano/Stratio/Stratio-Intelligence/stratio-intelligence/eclipse-che-workspace-python/stratio-trepan/test/example/gcd.py");
        commands.add("2");
        commands.add("9");


        ProcessBuilder processBuilder = new ProcessBuilder(commands);
        processBuilder.redirectErrorStream(true);
        Map<String, String> env = processBuilder.environment();
        env.put("PYTHONUNBUFFERED","1");
        env.put("PYTHONPATH","/home/asoriano/Stratio/Stratio-Intelligence/stratio-intelligence/eclipse-che-workspace-python/stratio-trepan");

        Process process = processBuilder.start();

        // · Pipe stdin to process
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), "UTF-8"));
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));

        // · Stdout monitoring
        BlockingQueue<String> stdoutQueue = new ArrayBlockingQueue(100);
        ProcessStdOutRetriever outputReader =
                new ProcessStdOutRetriever("python process output reader",
                        process, stdoutQueue, "\n");
        //outputReader.setDaemon(true);
        outputReader.start();

        // · Unix socket monitoring thread
        BlockingQueue<String> socketQueue = new ArrayBlockingQueue(100);
        UnixSocketMsgRetriever msgRetrieverThread = new UnixSocketMsgRetriever("/tmp/trepan.sock", socketQueue);
        msgRetrieverThread.start();

        while (true) {
            try {
                String currInputLine = br.readLine();
                bw.write(currInputLine);
                bw.newLine();
                bw.flush();

/*                while (!socketQueue.isEmpty()) {
                    System.out.println(socketQueue.take());
                }*/
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}