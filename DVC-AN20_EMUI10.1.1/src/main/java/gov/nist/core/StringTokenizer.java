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

    public StringTokenizer(String buffer2) {
        this.buffer = buffer2;
        this.bufferLen = buffer2.length();
        this.ptr = 0;
    }

    public String nextToken() {
        char c;
        int startIdx = this.ptr;
        do {
            int i = this.ptr;
            if (i >= this.bufferLen) {
                break;
            }
            c = this.buffer.charAt(i);
            this.ptr++;
        } while (c != '\n');
        return this.buffer.substring(startIdx, this.ptr);
    }

    public boolean hasMoreChars() {
        return this.ptr < this.bufferLen;
    }

    public static boolean isHexDigit(char ch) {
        return (ch >= 'A' && ch <= 'F') || (ch >= 'a' && ch <= 'f') || isDigit(ch);
    }

    public static boolean isAlpha(char ch) {
        return ch <= 127 ? (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') : Character.isLowerCase(ch) || Character.isUpperCase(ch);
    }

    public static boolean isDigit(char ch) {
        if (ch <= 127) {
            return ch <= '9' && ch >= '0';
        }
        return Character.isDigit(ch);
    }

    public static boolean isAlphaDigit(char ch) {
        return ch <= 127 ? (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || (ch <= '9' && ch >= '0') : Character.isLowerCase(ch) || Character.isUpperCase(ch) || Character.isDigit(ch);
    }

    public String getLine() {
        int startIdx = this.ptr;
        while (true) {
            int i = this.ptr;
            if (i >= this.bufferLen || this.buffer.charAt(i) == '\n') {
                int i2 = this.ptr;
            } else {
                this.ptr++;
            }
        }
        int i22 = this.ptr;
        if (i22 < this.bufferLen && this.buffer.charAt(i22) == '\n') {
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
        int i = this.ptr;
        if (i < this.bufferLen) {
            String str = this.buffer;
            this.ptr = i + 1;
            return str.charAt(i);
        }
        throw new ParseException(this.buffer + " getNextChar: End of buffer", this.ptr);
    }

    public void consume() {
        this.ptr = this.savedPtr;
    }

    public void consume(int k) {
        this.ptr += k;
    }

    public Vector<String> getLines() {
        Vector<String> result = new Vector<>();
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
            if (la != 0) {
                consume(1);
            } else {
                throw new ParseException("EOL reached", 0);
            }
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
