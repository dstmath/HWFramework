package gov.nist.core;

import java.text.ParseException;
import java.util.Vector;

public class StringTokenizer {
    protected String buffer;
    protected int bufferLen;
    protected int ptr;
    protected int savedPtr;

    protected StringTokenizer() {
    }

    public StringTokenizer(String buffer) {
        this.buffer = buffer;
        this.bufferLen = buffer.length();
        this.ptr = 0;
    }

    public String nextToken() {
        int startIdx = this.ptr;
        while (this.ptr < this.bufferLen) {
            char c = this.buffer.charAt(this.ptr);
            this.ptr++;
            if (c == 10) {
                break;
            }
        }
        return this.buffer.substring(startIdx, this.ptr);
    }

    public boolean hasMoreChars() {
        return this.ptr < this.bufferLen;
    }

    public static boolean isHexDigit(char ch) {
        if ((ch < 'A' || ch > 'F') && (ch < 'a' || ch > 'f')) {
            return isDigit(ch);
        }
        return true;
    }

    public static boolean isAlpha(char ch) {
        boolean z = true;
        if (ch <= 127) {
            if ((ch < 'a' || ch > 'z') && (ch < 'A' || ch > 'Z')) {
                z = false;
            }
            return z;
        }
        if (!Character.isLowerCase(ch)) {
            z = Character.isUpperCase(ch);
        }
        return z;
    }

    public static boolean isDigit(char ch) {
        boolean z = false;
        if (ch > 127) {
            return Character.isDigit(ch);
        }
        if (ch <= '9' && ch >= '0') {
            z = true;
        }
        return z;
    }

    public static boolean isAlphaDigit(char ch) {
        boolean z = true;
        if (ch <= 127) {
            if ((ch < 'a' || ch > 'z') && ((ch < 'A' || ch > 'Z') && (ch > '9' || ch < '0'))) {
                z = false;
            }
            return z;
        }
        if (!(Character.isLowerCase(ch) || Character.isUpperCase(ch))) {
            z = Character.isDigit(ch);
        }
        return z;
    }

    public String getLine() {
        int startIdx = this.ptr;
        while (this.ptr < this.bufferLen && this.buffer.charAt(this.ptr) != 10) {
            this.ptr++;
        }
        if (this.ptr < this.bufferLen && this.buffer.charAt(this.ptr) == 10) {
            this.ptr++;
        }
        return this.buffer.substring(startIdx, this.ptr);
    }

    public String peekLine() {
        int curPos = this.ptr;
        String retval = getLine();
        this.ptr = curPos;
        return retval;
    }

    public char lookAhead() throws ParseException {
        return lookAhead(0);
    }

    public char lookAhead(int k) throws ParseException {
        try {
            return this.buffer.charAt(this.ptr + k);
        } catch (IndexOutOfBoundsException e) {
            return 0;
        }
    }

    public char getNextChar() throws ParseException {
        if (this.ptr >= this.bufferLen) {
            throw new ParseException(this.buffer + " getNextChar: End of buffer", this.ptr);
        }
        String str = this.buffer;
        int i = this.ptr;
        this.ptr = i + 1;
        return str.charAt(i);
    }

    public void consume() {
        this.ptr = this.savedPtr;
    }

    public void consume(int k) {
        this.ptr += k;
    }

    public Vector<String> getLines() {
        Vector<String> result = new Vector();
        while (hasMoreChars()) {
            result.addElement(getLine());
        }
        return result;
    }

    public String getNextToken(char delim) throws ParseException {
        int startIdx = this.ptr;
        while (true) {
            char la = lookAhead(0);
            if (la == delim) {
                return this.buffer.substring(startIdx, this.ptr);
            }
            if (la == 0) {
                throw new ParseException("EOL reached", 0);
            }
            consume(1);
        }
    }

    public static String getSDPFieldName(String line) {
        if (line == null) {
            return null;
        }
        try {
            return line.substring(0, line.indexOf(Separators.EQUALS));
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }
}
