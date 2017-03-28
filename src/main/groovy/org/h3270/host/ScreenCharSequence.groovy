package org.h3270.host

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

import org.h3270.render.TextRenderer

/**
 * @author Andre Spiegel spiegel@gnu.org
 * @version $Id: ScreenCharSequence.java,v 1.6 2006/10/25 11:20:09 spiegel Exp $
 */
class ScreenCharSequence {

    private Screen screen = null
    private String text = null
    private int width = 0
    // private int height = 0

    private TextRenderer renderer = new TextRenderer()

    ScreenCharSequence(final Screen s) {
        screen = s
        width = screen.width
        // height = screen.getHeight()
        text = renderer.render(s)
    }

    int length() {
        text.length()
    }

    char charAt(final int index) {
        text.charAt(index)
    }

    String subSequence(final int start, final int end) {
         text.substring(start, end)
    }

     String toString() {
         text
    }

    Field getFieldAt(final int index) {
        if (index < 0 || index >= length()) {
            throw new IndexOutOfBoundsException()
        }
        final int y = index / (width + 1)
        final int x = index % (width + 1)
        if (x == width) {
            return null
        }
        return screen.getInputFieldAt(x, y)
    }
}
