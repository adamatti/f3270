package net.sf.f3270;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.MessageFormat;

import org.apache.commons.lang.StringUtils;

public class TerminalScreenToConsoleObserver extends TerminalObserver {
    private Terminal terminal;
    private String screenContents;
    private PrintStream outputStream = System.out;

    public TerminalScreenToConsoleObserver(Terminal terminal) {
        this.terminal = terminal;
    }

    @Override
    public void screenUpdated() {
        super.screenUpdated();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(stream);
        terminal.printScreen(printStream);
        screenContents = stream.toString();
    }

    @Override
    public void commandIssued(String command, String returned, Parameter... parameters) {
        super.commandIssued(command, returned, parameters);
        String output = MessageFormat.format("{0}({1})", command, StringUtils.join(parameters, ", "));
        if (returned != null) {
            output += ("=" + returned);
        }
        outputStream.println(output);
        delayedPrintScreen();
    }

    private void delayedPrintScreen() {
        if (screenContents != null) {
            outputStream.println();
            outputStream.print(screenContents);
            screenContents = null;
        }
    }
}