package android.icu.impl.data;

import android.icu.impl.PatternProps;
import android.icu.impl.PatternTokenizer;
import android.icu.impl.Utility;
import android.icu.text.UTF16;
import dalvik.bytecode.Opcodes;
import java.io.IOException;

public class TokenIterator {
    private StringBuffer buf;
    private boolean done;
    private int lastpos;
    private String line;
    private int pos;
    private ResourceReader reader;

    public TokenIterator(ResourceReader r) {
        this.reader = r;
        this.line = null;
        this.done = false;
        this.buf = new StringBuffer();
        this.lastpos = -1;
        this.pos = -1;
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
        position = PatternProps.skipWhiteSpace(this.line, position);
        if (position == this.line.length()) {
            return -1;
        }
        int startpos = position;
        int position2 = position + 1;
        char c = this.line.charAt(position);
        char quote = '\u0000';
        switch (c) {
            case Opcodes.OP_NEW_INSTANCE /*34*/:
            case Opcodes.OP_THROW /*39*/:
                quote = c;
                break;
            case Opcodes.OP_NEW_ARRAY /*35*/:
                return -1;
            default:
                this.buf.append(c);
                break;
        }
        int[] iArr = null;
        while (position2 < this.line.length()) {
            c = this.line.charAt(position2);
            if (c == PatternTokenizer.BACK_SLASH) {
                if (iArr == null) {
                    iArr = new int[1];
                }
                iArr[0] = position2 + 1;
                int c32 = Utility.unescapeAt(this.line, iArr);
                if (c32 < 0) {
                    throw new RuntimeException("Invalid escape at " + this.reader.describePosition() + ':' + position2);
                }
                UTF16.append(this.buf, c32);
                position2 = iArr[0];
            } else if ((quote != '\u0000' && c == quote) || (quote == '\u0000' && PatternProps.isWhiteSpace(c))) {
                return position2 + 1;
            } else {
                if (quote == '\u0000' && c == '#') {
                    return position2;
                }
                this.buf.append(c);
                position2++;
            }
        }
        if (quote == '\u0000') {
            return position2;
        }
        throw new RuntimeException("Unterminated quote at " + this.reader.describePosition() + ':' + position);
    }
}
