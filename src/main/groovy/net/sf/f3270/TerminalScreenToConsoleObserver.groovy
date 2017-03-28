package net.sf.f3270

import groovy.util.logging.Slf4j

import java.text.MessageFormat

import org.apache.commons.lang.StringUtils

@Slf4j
class TerminalScreenToConsoleObserver extends TerminalObserver {
    private Terminal terminal
    private String screenContents

    TerminalScreenToConsoleObserver(Terminal terminal) {
        this.terminal = terminal
    }

    void screenUpdated() {
        super.screenUpdated()
        ByteArrayOutputStream stream = new ByteArrayOutputStream()
        PrintStream printStream = new PrintStream(stream)
        terminal.printScreen(printStream)
        screenContents = stream.toString()
    }

    void commandIssued(String command, String returned, Parameter... parameters) {
        super.commandIssued(command, returned, parameters)
        String output = MessageFormat.format("{0}({1})", command, StringUtils.join(parameters, ", "))
        if (returned) {
            output += ("=" + returned)
        }
        log.info output
        delayedPrintScreen()
    }

    private void delayedPrintScreen() {
        if (screenContents) {
            System.out.println()
            System.out.print(screenContents)
            screenContents = null
        }
    }
}
