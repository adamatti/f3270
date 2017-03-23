package net.sf.f3270

import org.h3270.host.Field
import org.h3270.host.InputField
import org.h3270.host.S3270
import org.h3270.host.Screen
import org.h3270.host.S3270.TerminalMode
import org.h3270.host.S3270.TerminalType
import org.h3270.render.TextRenderer

class Terminal {
    private static final int SCREEN_WIDTH_IN_CHARS = 80

    private S3270 s3270
    private final Collection<TerminalObserver> observers = new ArrayList<TerminalObserver>()

    private final String s3270Path
    private final String hostname
    private final int port
    private final TerminalType type
    private final TerminalMode mode
	private final boolean showTerminalWindow
    private static final char MAINFRAME_BLANK_CHAR = '\u0000'
    private static final char SINGLE_SPACE = ' '

    Terminal(final String s3270Path, final String hostname, final int port, final TerminalType type,final TerminalMode mode, final boolean showTerminalWindow) {
        this.s3270Path = s3270Path
        this.hostname = hostname
        this.port = port
        this.type = type
        this.mode = mode
		this.showTerminalWindow = showTerminalWindow

        addDefaultObservers()
    }

    private void addDefaultObservers() {
        addObserver(new TerminalScreenToConsoleObserver(this))
        if (showTerminalWindow) {
        	addObserver(new TerminalWindowObserver())
		}
    }

    void addObserver(TerminalObserver observer) {
        observers.add(observer)
    }
    
    void removeObserver(TerminalObserver observer) {
        observers.remove(observer);
    }

    Terminal connect() {
        s3270 = new S3270(s3270Path, hostname, port, type, mode)
        updateScreen();
        for (TerminalObserver observer : observers) {
            observer.connect(s3270);
        }
        commandIssued("connect", null)
        return this
    }

    void disconnect() {
        assertConnected()
        s3270.disconnect()
        for (TerminalObserver observer : observers) {
            observer.disconnect()
        }
    }

    private void assertConnected() {
        if (s3270 == null) {
            throw new RuntimeException("not connected")
        }
    }

    private void commandIssued(String command, String returned, Parameter... parameters) {
        for (TerminalObserver observer : observers) {
            observer.commandIssued(command, returned, parameters)
        }
    }

    private void updateScreen() {
        s3270.updateScreen()
        for (TerminalObserver observer : observers) {
            observer.screenUpdated()
        }
    }

    String getScreenText() {
        assertConnected()
        new TextRenderer().render(s3270.getScreen())
    }

    String getLine(final int line) {
        assertConnected();
        final Screen screen = s3270.getScreen();
        final StringBuilder sb = new StringBuilder();
        for (int col = 0; col < screen.getWidth(); col++) {
            sb.append(replaceNull(screen.charAt(col, line)));
        }
        sb.toString()
    }

    int getWidth() {
        s3270.getScreen().getWidth()
    }
    
    int getHeight() {
        s3270.getScreen().getHeight()
    }

    void enter() {
        assertConnected();
        s3270.submitScreen();
        s3270.enter();
        updateScreen();
        commandIssued("enter", null);
    }

    void pf(final int n) {
        assertConnected()
        s3270.submitScreen()
        s3270.pf(n)
        updateScreen()
        commandIssued("pf", null, new Parameter("n", n))
    }

    void pa(final int n) {
        assertConnected()
        s3270.pa(n)
        updateScreen()
        commandIssued("pa", null, new Parameter("n", n))
    }
    
    void clear() {
        assertConnected()
        s3270.clear()
        updateScreen()
        commandIssued("clear", null)
    }

    void type(final String text) {
        assertConnected();
        InputField field = s3270.getScreen().getFocusedField();
        field.setValue(text);
        commandIssued("type", null, new Parameter("text", text));
    }

    void clearScreen() {
        assertConnected();
        s3270.eraseEOF();
        updateScreen();
        commandIssued("clearScreen", null)
    }

    /**
     * @deprecated Use {@link @link Terminal#write (FieldIdentifier)} instead
     */
    // TODO : delete method (deprecated on 2010-04-15)
    void write(final String label, final String value) {
        write(new FieldIdentifier(label), value)
    }

    /**
     * @deprecated Use {@link @link Terminal#write (FieldIdentifier)} instead
     */
    // TODO : delete method (deprecated on 2010-04-15)
    void write(final String label, final String value, final MatchMode matchMode) {
        write(new FieldIdentifier(label, matchMode), value)
    }

    /**
     * @deprecated Use {@link @link Terminal#write (FieldIdentifier)} instead
     */
    // TODO : delete method (deprecated on 2010-04-15)
    void write(final String label, final String value, final int skip) {
        write(new FieldIdentifier(label, skip), value)
    }

    /**
     * @deprecated Use {@link @link Terminal#write (FieldIdentifier)} instead
     */
    // TODO : delete method (deprecated on 2010-04-15)
    void write(final String label, final String value, final int skip, final MatchMode matchMode) {
        write(new FieldIdentifier(label, skip, matchMode), value)
    }

    /**
     * @deprecated Use {@link @link Terminal#write (FieldIdentifier)} instead
     */
    // TODO : delete method (deprecated on 2010-04-15)
    void write(String label, String value, int skip, int matchNumber, MatchMode matchMode) {
        write(new FieldIdentifier(label, skip, matchNumber, matchMode), value)
    }

    void write(FieldIdentifier fieldIdentifier, String value) {
        assertConnected()
        getInputField(fieldIdentifier).setValue(value)
        commandIssued("write", null, buildParameters(fieldIdentifier, value))
    }

    /**
     * @deprecated Use {@link @link Terminal#read (FieldIdentifier)} instead
     */
    // TODO : delete method (deprecated on 2010-04-15)
    String read(final String label) {
        read(new FieldIdentifier(label))
    }

    /**
     * @deprecated Use {@link @link Terminal#read (FieldIdentifier)} instead
     */
    // TODO : delete method (deprecated on 2010-04-15)
    String read(final String label, final int skip) {
        read(new FieldIdentifier(label, skip))
    }

    /**
     * @deprecated Use {@link @link Terminal#read (FieldIdentifier)} instead
     */
    // TODO : delete method (deprecated on 2010-04-15)
    String read(final String label, final MatchMode matchMode) {
        read(new FieldIdentifier(label, matchMode))
    }

    /**
     * @deprecated Use {@link @link Terminal#read (FieldIdentifier)} instead
     */
    // TODO : delete method (deprecated on 2010-04-15)
    String read(final String label, final int skip, final MatchMode matchMode) {
        read(new FieldIdentifier(label, skip, matchMode))
    }

    /**
     * @deprecated Use {@link @link Terminal#read (FieldIdentifier)} instead
     */
    // TODO : delete method (deprecated on 2010-04-15)
    String read(final String label, final int skip, final int matchNumber, final MatchMode matchMode) {
        read(new FieldIdentifier(label, skip, matchNumber, matchMode))
    }

    String read(FieldIdentifier fieldIdentifier) {
        assertConnected()
        Field field = getField(fieldIdentifier)
        String value = read(field)
        commandIssued("read", value, buildParameters(fieldIdentifier, null))
        value
    }

    private InputField getInputField(FieldIdentifier fieldIdentifier) {
        Field field = getField(fieldIdentifier)
        if (!(field instanceof InputField)) {
            throw new RuntimeException(
                    String.format("field [%s] after match [%d] for [%s] with skip [%d] found with match mode [%s] is not an input field",
                    read(field),
                    fieldIdentifier.matchNumber,
                    fieldIdentifier.label,
                    fieldIdentifier.skip,
                    fieldIdentifier.matchMode))
        }
        field
    }

    String read(Field field) {
        replaceNulls(field.getValue()).trim()
    }

    private String replaceNulls(String value) {
        value.replace(MAINFRAME_BLANK_CHAR, SINGLE_SPACE)
    }

    private char replaceNull(char c) {
        c == MAINFRAME_BLANK_CHAR ? SINGLE_SPACE : c
    }

    private Parameter[] buildParameters(FieldIdentifier fieldIdentifier, String value) {
        Collection<Parameter> parameters = fieldIdentifier.buildParameters()
        if (value != null) {
            parameters.add(new Parameter("value", value))
        }
        parameters.toArray(new Parameter[parameters.size()])
    }

    /**
     * @deprecated Use {@link @link Terminal#getField (FieldIdentifier)} instead
     */
    // TODO : delete method (deprecated on 2010-04-15)
    Field fieldAfterLabel(String label) {
        getField(new FieldIdentifier(label))
    }

    /**
     * @deprecated Use {@link @link Terminal#getField (FieldIdentifier)} instead
     */
    // TODO : delete method (deprecated on 2010-04-15)
    Field fieldAfterLabel(String label, int skip) {
        getField(new FieldIdentifier(label, skip))
    }

    /**
     * @deprecated Use {@link @link Terminal#getField (FieldIdentifier)} instead
     */
    // TODO : delete method (deprecated on 2010-04-15)
    Field fieldAfterLabel(String label, int skip, int matchNumber) {
        getField(new FieldIdentifier(label, skip, matchNumber))
    }

    /**
     * @deprecated Use {@link @link Terminal#getField (FieldIdentifier)} instead
     */
    // TODO : Inline into Terminal#getField (FieldIdentifier) (deprecated on 2010-04-15)
    Field fieldAfterLabel(final String label, final int skip, final int matchNumber, final MatchMode matchMode) {
        getField(new FieldIdentifier(label, skip, matchNumber, matchMode))
    }

    Field getField(FieldIdentifier fieldIdentifier) {
        assertConnected()
        List<Field> fields = s3270.getScreen().getFields()
        fieldIdentifier.find(fields)
    }

    /**
     * @deprecated Should not be using field indexes use other methods on {@link @link Terminal} to achieve desired behaviour
     */
    // TODO : delete method (deprecated on 2010-04-15)
    int getFieldIndex(final String label, final int matchNumber, final MatchMode matchMode) {
        assertConnected()
        new FieldIdentifier(label, matchNumber, matchMode).getFieldIndexOfLabel(s3270.getScreen().getFields())
    }
    
    boolean screenHasLabel(FieldIdentifier fieldIdentifier) {
        fieldIdentifier.getFieldIndexOfLabel(s3270.getScreen().getFields()) != -1
    }

    void printFields() {
        printFields(System.out)
    }

    void printFields(PrintStream stream) {
        assertConnected()
        List<Field> fields = s3270.getScreen().getFields()
        for (int i = 0; i < fields.size(); i++) {
            String value = replaceNulls(fields.get(i).getValue())
            stream.println(String.format("%d=[%s]", i, value))
        }
    }

    private static final String SCREEN_SEPARATOR = "+--------------------------------------------------------------------------------+";

    void printScreen() {
        printScreen(System.out);
    }

    void printScreen(PrintStream stream) {
        assertConnected();
        final String[] lines = getScreenText().split("\n");
        final String blanks = "                                                                                ";
        stream.println(SCREEN_SEPARATOR);
        for (String line : lines) {
            final String fixedLine = (line + blanks).substring(0, SCREEN_WIDTH_IN_CHARS);
            stream.println(String.format("|%s|", fixedLine));
        }
        stream.println(SCREEN_SEPARATOR);
    }

}
