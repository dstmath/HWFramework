package android.icu.impl.data;

import android.icu.impl.PatternProps;
import android.icu.impl.Utility;
import android.icu.text.UTF16;
import java.io.IOException;

public class TokenIterator {
    private StringBuffer buf = new StringBuffer();
    private boolean done = false;
    private int lastpos = -1;
    private String line = null;
    private int pos = -1;
    private ResourceReader reader;

    public TokenIterator(ResourceReader r) {
        this.reader = r;
    }

    public String next() throws IOException {
        if (this.done) {
            return null;
        }
        while (true) {
            if (this.line == null) {
                this.line = this.reader.readLineSkippingComments();
                if (this.line == null) {
                    this.done = true;
                    return null;
                }
                this.pos = 0;
            }
            this.buf.setLength(0);
            this.lastpos = this.pos;
            this.pos = nextToken(this.pos);
            if (this.pos >= 0) {
                return this.buf.toString();
            }
            this.line = null;
        }
    }

    public int getLineNumber() {
        return this.reader.getLineNumber();
    }

    public String describePosition() {
        return this.reader.describePosition() + ':' + (this.lastpos + 1);
    }

    private int nextToken(int position) {
        int position2 = PatternProps.skipWhiteSpace(this.line, position);
        if (position2 == this.line.length()) {
            return -1;
        }
        int startpos = position2;
        int position3 = position2 + 1;
        char c = this.line.charAt(position2);
        char quote = 0;
        if (c != '\'') {
            switch (c) {
                case '\"':
                    break;
                case '#':
                    return -1;
                default:
                    this.buf.append(c);
                    break;
            }
        }
        quote = c;
        int[] posref = null;
        while (position3 < this.line.length()) {
            char c2 = this.line.charAt(position3);
            if (c2 == '\\') {
                if (posref == null) {
                    posref = new int[1];
                }
                posref[0] = position3 + 1;
                int c32 = Utility.unescapeAt(this.line, posref);
                if (c32 >= 0) {
                    UTF16.append(this.buf, c32);
                    position3 = posref[0];
                } else {
                    throw new RuntimeException("Invalid escape at " + this.reader.describePosition() + ':' + position3);
                }
            } else if ((quote != 0 && c2 == quote) || (quote == 0 && PatternProps.isWhiteSpace(c2))) {
                return position3 + 1;
            } else {
                if (quote == 0 && c2 == '#') {
                    return position3;
                }
                this.buf.append(c2);
                position3++;
            }
        }
        if (quote == 0) {
            return position3;
        }
        throw new RuntimeException("Unterminated quote at " + this.reader.describePosition() + ':' + startpos);
    }
}
