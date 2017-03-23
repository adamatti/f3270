package org.h3270.host;

/*
 * Copyright (C) 2003-2006 akquinet framework solutions
 *
 * This file is part of h3270.
 *
 * h3270 is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * h3270 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with h3270; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston,
 * MA 02110-1301 USA
 */

/**
 * Represents a Field on a Screen.
 * 
 * @author Andre Spiegel spiegel@gnu.org
 * @version $Id: Field.java,v 1.21 2006/10/25 11:20:09 spiegel Exp $
 */
class Field {

    // bits controlling the field type (mask = c0)

    public static final byte ATTR_PROTECTED = 0x20
    public static final byte ATTR_NUMERIC = 0x10
    public static final byte ATTR_DISP_1 = 0x08
    public static final byte ATTR_DISP_2 = 0x04

    // attributes for extended highlighting (mask = 41)

    public static final int ATTR_EH_DEFAULT = 0x00
    public static final int ATTR_EH_BLINK = 0x80
    public static final int ATTR_EH_REV_VIDEO = 0xF2
    public static final int ATTR_EH_UNDERSCORE = 0xF4

    // attributes for colors (mask = 42)

    public static final int ATTR_COL_DEFAULT = 0x00
    public static final int ATTR_COL_BLUE = 0xF1
    public static final int ATTR_COL_RED = 0xF2
    public static final int ATTR_COL_PINK = 0xF3
    public static final int ATTR_COL_GREEN = 0xF4
    public static final int ATTR_COL_TURQUOISE = 0xF5
    public static final int ATTR_COL_YELLOW = 0xF6
    public static final int ATTR_COL_WHITE = 0xF7

    protected Screen screen

    protected int startx
    protected int starty
    protected int endx
    protected int endy

    protected String value

    public static final int DISPLAY_NORMAL = 0
    public static final int DISPLAY_INTENSIFIED = 1
    public static final int DISPLAY_HIDDEN = 2

    private int displayMode = DISPLAY_NORMAL

    /**
     * Value of the extended highlighting attribute for this field
     */
    private int extendedHighlight = ATTR_EH_DEFAULT

    /**
     * Value of the color extended attribute for this field
     */
    private int extendedColor = ATTR_COL_DEFAULT;

    Field(final Screen screen, final byte fieldCode, final int startx, final int starty, final int endx,
            final int endy, final int extendedColor, final int extendedHighlight) {
        this.screen = screen
        this.startx = startx
        this.starty = starty
        this.endx = endx
        this.endy = endy

        this.extendedColor = extendedColor
        this.extendedHighlight = extendedHighlight

        if ((fieldCode & ATTR_DISP_1) == 0) {
            displayMode = DISPLAY_NORMAL
        } else if ((fieldCode & ATTR_DISP_2) == 0) {
            displayMode = DISPLAY_INTENSIFIED
        } else {
            displayMode = DISPLAY_HIDDEN
        }
    }

    Field(final Screen screen, final byte fieldCode, final int startx, final int starty, final int endx,final int endy) {
        this(screen, fieldCode, startx, starty, endx, endy, ATTR_COL_DEFAULT, ATTR_EH_DEFAULT);
    }

    /**
     * Returns the x coordinate (column) at which this Field begins. Column numbers start at zero. The number returned
     * is the position of the Field's first character, not of the control character that opens the Field.
     */
    int getStartX() {
        startx
    }

    /**
     * Returns the y coordinate (row) in which this Field begins. Row numbers start at zero, increasing downward from
     * the top.
     */
    int getStartY() {
        starty
    }

    /**
     * Returns the x coordinate (column) at which this Field ends. Column numbers start at zero. The number returned is
     * the position of the Field's last character, not of the control character that terminates the Field.
     */
    int getEndX() {
        endx
    }

    /**
     * Returns the y coordinate (row) in which this Field ends. Row numbers start at zero, increasing downward from the
     * top.
     */
    int getEndY() {
        return endy
    }

    /**
     * Returns the width (number of characters) of this Field. This does not include the control characters that delimit
     * the Field.
     * 
     * @deprecated this method will disappear soon
     */
    int getWidth() {
        return endx - startx + 1
    }

    /**
     * Returns the height of this Field, in lines. If the control characters at the start or the end of this Field are
     * at the end or the start of the preceding or next line, respectively, then those lines are not incluced in the
     * height count -- only the actual text of the Field counts.
     */
    int getHeight() {
        endy - starty + 1
    }

    /**
     * Returns the Screen of which this Field is a part.
     */
    Screen getScreen() {
        screen
    }

    /**
     * Returns the current value of this Field. This does not include the control character that starts the field, and
     * it does not include leading or trailing newlines. If this Field is a multi-line field, the individual lines are
     * separated by newlines.
     */
    String getValue() {
        if (value == null) {
            value = screen.substring(startx, starty, endx, endy)
        }
        return value
    }

    String getValue(final int lineNumber) {
        getValue(getValue(), lineNumber)
    }

    /**
     * Returns one of the lines of a potentially multi-line Field.
     * 
     * @param lineNumber
     *            the number of the line to retrieve, starting at zero
     */
    static String getValue(final String value, final int lineNumber) {
        final StringTokenizer st = new StringTokenizer(value, "\n");

        int row = 0

        String line = st.nextToken()

        while (row++ != lineNumber) {
            line = st.nextToken()
        }
        return line
    }

    /**
     * Returns the textual representation of this Field. Unlike getValue(), this methods prepends a blank for the
     * opening field attribute, and adds newline characters at the beginning and the end as needed. Calling this method
     * for each Field of a Screen, and concatenating the results, creates a full printable, textual representation of
     * the screen.
     */
    String getText() {
        final StringBuffer result = new StringBuffer();
        if (startx == 0) {
            if (starty > 0) {
                result.append(" \n")
            }
        } else {
            result.append(" ")
        }
        result.append(this.getValue())
        if (endx == screen.getWidth() - 1 && starty <= endy) {
            result.append("\n")
        }
        result.toString()
    }

    boolean isMultiline() {
        endy > starty
    }

    boolean isEmpty() {
        if (starty == endy) {
            return startx > endx
        }

        starty > endy
    }

    boolean isIntensified() {
        displayMode == DISPLAY_INTENSIFIED
    }

    /**
     * Returns true if this field has a 3270 extended color.
     */
    boolean hasExtendedColor() {
        extendedColor != ATTR_COL_DEFAULT
    }

    /**
     * Returns true if this field has 3270 extended highlighting.
     */
    boolean hasExtendedHighlight() {
        extendedHighlight != ATTR_EH_DEFAULT
    }

    boolean isHidden() {
        return displayMode == DISPLAY_HIDDEN
    }
    
    boolean isWritable() {
        false
    }

    /**
     * If this Field has an extended color, returns the index of that color (0xf1 through 0xf7). If this Field does not
     * have an extended color assigned, returns zero.
     */
    int getExtendedColor() {
        extendedColor
    }

    /**
     * If this field has extended highlighting, returns the index of that highlighting scheme. If this Field does have
     * extended highlighting, zero is returned.
     */
    int getExtendedHighlight() {
        extendedHighlight
    }
}
