package net.sf.f3270.impersonator

import groovy.util.logging.Slf4j
import org.apache.commons.io.IOUtils

@Slf4j
class TN3270Impersonator {

    static void main(String[] args) {
        new TN3270Impersonator(1111, "net/sf/f3270/impersonator/data.txt")
    }

    private List<DataBlock> data
    private final int port

    TN3270Impersonator(int port, String dataFilePath) {
        this.port = port
        parseDataFile(dataFilePath)
        startMainThread()
    }

    private void startMainThread() {
        Runnable r = new Runnable() {
            void run() {
                try {
                    log.info("waiting for client to connect")
                    Socket socket = new ServerSocket(port).accept()
                    log.info("client connected")

                    InputStream is = socket.getInputStream()
                    OutputStream os = socket.getOutputStream()

                    List<Integer> in_ = new ArrayList<Integer>()
                    int current = 0
                    while (true) {
                        if (current >= data.size()) {
                            break
                        }
                        DataBlock entry = data.get(current)

                        if (entry.getIn().length == 0) {
                            write(os, entry.getOut())
                            current++
                            in_.clear()
                            continue
                        }

                        int b = is.read()
                        in_.add(b)

                        if (isInputMatch(in_, entry.getIn())) {
                            write(os, entry.getOut())
                            current++
                            in_.clear()
                        }
                    }

                } catch (IOException e) {
                    throw new RuntimeException(e)
                }

                log.info("no more recorded data to replay")

                while (true) {
                    try {
                        Thread.sleep(1000)
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e)
                    }
                }
            }
        }
        new Thread(r).start()
    }

    private void write(OutputStream os, int[] out) throws IOException {
        for (int b : out) {
            os.write(b)
        }
    }

    private boolean isInputMatch(List<Integer> list, int[] array) {
        if (list.size() != array.length) {
            return false
        }
        for (int i = 0; i < array.length; i++) {
            if (list.get(i) != array[i]) {
                return false
            }
        }
        true
    }

    private void parseDataFile(String dataFilePath) {
        data = new ArrayList<DataBlock>()
        List<String> lines = readLines(dataFilePath)
        int[] in_ = []
        int[] out = []
        for (String line : lines) {
            String[] tokens = line.split(" ")
            if (tokens[0] == ">") {
                in_ = toIntArray(tokens)
            } else {
                out = toIntArray(tokens)
                data.add(new DataBlock(in_, out))
                in_ = []
                out = []
            }
        }
        if (in_.length != 0 || out.length != 0) {
            data.add(new DataBlock(in_, out))
        }
    }

    private int[] toIntArray(String[] tokens) {
        int[] a = new int[tokens.length - 1]
        for (int i = 1; i < tokens.length; i++) {
            String token = tokens[i]
            int b = Integer.parseInt(token)
            a[i - 1] = b
        }
        a
    }

    private List<String> readLines(String dataFilePath) {
        try {
            return IOUtils.readLines(TN3270Impersonator.class.getClassLoader().getResourceAsStream(dataFilePath))
        } catch (IOException e) {
            throw new RuntimeException(e)
        }
    }

}
