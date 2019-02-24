/*
 * @(#) SQLParseText.java
 *
 * doric Column-oriented database system
 * Copyright (c) 2019 Peter Wall
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.pwall.doric.sql;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

import net.pwall.util.ParseText;

/**
 * Class to extend {@link ParseText} to cover SQL syntax, reading multiple lines as required.
 */
public class SQLParseText extends ParseText {

    private BufferedReader brdr;

    public SQLParseText(Reader rdr) throws IOException {
        super("dummy");
        brdr = rdr instanceof BufferedReader ? (BufferedReader)rdr : new BufferedReader(rdr);
        String line = brdr.readLine();
        if (line == null)
            skipToEnd();
        else
            setText(line);
    }

    /**
     * Copied from {@link ParseText}, with comparison changed to case-insensitive.
     *
     * @param   target      the target string
     * @return  {@code true} if target string found
     */
    @Override
    public boolean matchName(CharSequence target) {
        int len = target.length();
        if (getIndex() + len > getTextLength())
            return false;
        int i = getIndex();
        int j = 0;
        for (; len > 0; len--)
            if (!equalIgnoreCase(charAt(i++), target.charAt(j++)))
                return false;
        if (i < getTextLength() && isNameContinuation(charAt(i)))
            return false;
        return matchSuccess(i);
    }

    @Override
    public boolean isNameStart(char ch) {
        return ch >= 'A' && ch <= 'Z' || ch >= 'a' && ch <= 'z' || ch == '_';
    }

    private static boolean equalIgnoreCase(char a, char b) {
        return a == b ||
                a == (Character.isLowerCase(a) ? Character.toLowerCase(b) :
                        Character.toUpperCase(b));
    }

    /**
     * Extended version of {@link ParseText#skipSpaces()} that reads the next line when index reaches end, or when
     * "{@code --}" (start of comment) is seen.
     *
     * @throws  IOException if thrown by the {@link BufferedReader}
     */
    public SQLParseText skipSpacesMultiLine() throws IOException {
        for (;;) {
            skipSpaces();
            if (!(isExhausted() || match("--")))
                break;
            String line = brdr.readLine();
            if (line == null) {
                skipToEnd();
                break;
            }
            setText(line);
        }
        return this;
    }

}
