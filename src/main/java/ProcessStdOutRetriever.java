import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.concurrent.BlockingQueue;

import static java.lang.Math.min;

public class ProcessStdOutRetriever extends Thread {

    private static final Logger LOG = Logger.getLogger(ProcessStdOutRetriever.class);

    private static final int MAX_OUTPUT = 1048576;

    private Process process;
    private BlockingQueue<String> outputs;
    private String outputSeparator;

    public ProcessStdOutRetriever(String name, Process process, BlockingQueue<String> outputs, String outputSeparator) {
        super(name);
        this.process = process;
        this.outputs = outputs;
        this.outputSeparator = outputSeparator;
    }

    @SuppressWarnings("Since15")
    @Override
    public void run() {
        StringBuilder buf = new StringBuilder();
        InputStream in = process.getInputStream();

        while (!isInterrupted()) {
            if (!this.process.isAlive()) {
                outputs.add(buf.toString());
                break;
            }
            try {
                if (in != null) {
                    String data = read(in);
                    if (!data.isEmpty()) {
                        buf.append(data);
                        if (buf.length() > MAX_OUTPUT) {
                            buf.delete(0, buf.length() - MAX_OUTPUT);
                        }
                    }
                }
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            }
            if (buf.length() > 0) {
                extractOutput(buf);
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                break;
            }
        }

        LOG.debug(getName() + " has been stopped");
    }

    private InputStream getInput() throws IOException {
        return hasError()
                ? process.getErrorStream()
                : (hasInput()
                ? (hasError() ? process.getErrorStream() : process.getInputStream())
                : null);
    }

    private String wrap_mgs(String msg){
        return "######## STDOUT #####################\n" + msg + "\n######## /STDOUT #####################\n";
    }

    private void extractOutput(StringBuilder buf) {
        String out = buf.toString();
        outputs.add(out);
        System.out.println(wrap_mgs(out));
        buf.delete(0,out.length());
        /*
        int indexOf;
        while ((indexOf = buf.indexOf(outputSeparator)) >= 0) {
            String pdbOutput = buf.substring(0, indexOf);
            outputs.add(pdbOutput);
            System.out.println(wrap_mgs(pdbOutput));
            buf.delete(0, indexOf + outputSeparator.length());
        }
        */
    }

    private boolean hasError() throws IOException {
        return process.getErrorStream().available() != 0;
    }

    private boolean hasInput() throws IOException {
        return process.getInputStream().available() != 0;
    }

    private String read(InputStream in) throws IOException {
        int available = min(in.available(), MAX_OUTPUT);
        byte[] buf = new byte[available];
        int read = in.read(buf, 0, available);

        return new String(buf, 0, read, Charset.forName("UTF-8"));
    }
}
