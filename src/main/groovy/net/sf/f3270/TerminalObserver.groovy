package net.sf.f3270

import org.h3270.host.S3270

abstract class TerminalObserver {
    void screenUpdated() {}
    void commandIssued(String command, String returned, Parameter... parameters) {}
    void connect(S3270 s3270){}
    void disconnect() {}
}
