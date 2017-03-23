package net.sf.f3270

import java.text.MessageFormat

import org.apache.commons.lang.StringUtils;
class TerminalScreenToConsoleObserver extends TerminalObserver {
    private Terminal terminal;
    private String screenContents;

    TerminalScreenToConsoleObserver(Terminal terminal) {
        this.terminal = terminal;
    }

    @Override
    void screenUpdated() {
        super.screenUpdated();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(stream);
        terminal.printScreen(printStream);
        screenContents = stream.toString();
    }

    @Override
    void commandIssued(String command, String returned, Parameter... parameters) {
        super.commandIssued(command, returned, parameters);
        String output = MessageFormat.format("{0}({1})", command, StringUtils.join(parameters, ", "))
        if (returned != null) {
            output += ("=" + returned)
        }
        System.out.println(output)
        delayedPrintScreen()
    }

    private void delayedPrintScreen() {
        if (screenContents != null) {
            System.out.println()
            System.out.print(screenContents)
            screenContents = null
        }
    }
}
