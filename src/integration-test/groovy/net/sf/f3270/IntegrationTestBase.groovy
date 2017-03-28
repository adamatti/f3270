package net.sf.f3270

import net.sf.f3270.impersonator.TN3270Impersonator
import net.sf.f3270.impersonator.TN3270ProxyRecorder

import org.h3270.host.S3270.TerminalMode
import org.h3270.host.S3270.TerminalType
import org.junit.Assert

abstract class IntegrationTestBase {
    enum Mode {
        DIRECT, RECORD, REPLAY
    }

    protected Mode getMode(){
        Mode.DIRECT
    }

    protected abstract String getHostname()

    protected int getPort() {
        23
    }

    protected int getImpersonatorPort() {
        2323
    }

    protected Terminal terminal
    private TN3270ProxyRecorder recorder

    protected final void connect() {
        String dataFilePath = "${this.class.package.name.replace('.', '/')}/${this.class.simpleName}.txt"

        String hostname = null
        int port = 0
        if (getMode() == Mode.RECORD) {
            recorder = new TN3270ProxyRecorder(
                getImpersonatorPort(),
                getHostname(),
                getPort(),
                "src/integration-test/resources/${dataFilePath}"
            )
            hostname = "127.0.0.1"
            port = getImpersonatorPort()
        }
        if (getMode() == Mode.REPLAY) {
            new TN3270Impersonator(getImpersonatorPort(), dataFilePath)
            hostname = "127.0.0.1"
            port = getImpersonatorPort()
        }
        if (getMode() == Mode.DIRECT) {
            hostname = getHostname()
            port = 23
        }
        
        String os = System.getProperty("os.name")
        String s3270Path = "s3270"
        if (os.toLowerCase().contains("windows")) {
            s3270Path = "s3270/cygwin/s3270.exe"
        }

        terminal = new Terminal(
            s3270Path,
            hostname,
            port,
            TerminalType.TYPE_3279,
            TerminalMode.MODE_80_24,
            true
        )
        terminal.connect()
    }

    protected final void disconnect() {
        if (recorder != null) {
            recorder.dump();
        }
    }

    protected void sleep(int millis) {
        try {
            Thread.sleep(millis)
        } catch (InterruptedException e) {
            throw new RuntimeException(e)
        }
    }

    protected void assertTerminal(String text) {
        assertText(terminal,text)
    }

    protected void assertText(Terminal terminal, String text) {
        Assert.assertTrue("screen doesn't contain " + text, terminal.screenText.contains(text))
    }
}
