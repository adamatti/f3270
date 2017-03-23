package net.sf.f3270

import org.h3270.host.S3270

class TerminalWindowObserver extends TerminalObserver {
	private TerminalWindow terminalWindow

    void commandIssued(String command, String returned, Parameter... parameters) {
        terminalWindow.update(command, returned, parameters)
    }

    void connect(S3270 s3270) {
        terminalWindow = new TerminalWindow(s3270)

        terminalWindow.update("new Terminal", null, new Parameter("hostname", s3270.getHostname()), new Parameter(
                "port", s3270.getPort()), new Parameter("type", s3270.getType().getType()), new Parameter("mode", s3270
                .getMode().getMode()))
    }

    void disconnect() {
        terminalWindow.close()
    }

}
