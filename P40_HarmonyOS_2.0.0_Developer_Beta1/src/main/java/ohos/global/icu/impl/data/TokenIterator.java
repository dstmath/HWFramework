package ohos.global.icu.impl.data;

import java.io.IOException;
import ohos.global.icu.impl.PatternProps;
import ohos.global.icu.impl.Utility;
import ohos.global.icu.text.UTF16;

public class TokenIterator {
    private StringBuffer buf = new StringBuffer();
    private boolean done = false;
    private int lastpos = -1;
    private String line = null;
    private int pos = -1;
    private ResourceReader reader;

    public TokenIterator(ResourceReader resourceReader) {
        this.reader = resourceReader;
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
            int i = this.pos;
            this.lastpos = i;
            this.pos = nextToken(i);
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

    private int nextToken(int i) {
        int skipWhiteSpace = PatternProps.skipWhiteSpace(this.line, i);
        if (skipWhiteSpace == this.line.length()) {
            return -1;
        }
        int i2 = skipWhiteSpace + 1;
        char charAt = this.line.charAt(skipWhiteSpace);
        if (charAt != '\"') {
            if (charAt == '#') {
                return -1;
            }
            if (charAt != '\'') {
                this.buf.append(charAt);
                charAt = 0;
            }
        }
        int[] iArr = null;
        while (i2 < this.line.length()) {
            char charAt2 = this.line.charAt(i2);
            if (charAt2 == '\\') {
                if (iArr == null) {
                    iArr = new int[1];
                }
                iArr[0] = i2 + 1;
                int unescapeAt = Utility.unescapeAt(this.line, iArr);
                if (unescapeAt >= 0) {
                    UTF16.append(this.buf, unescapeAt);
                    i2 = iArr[0];
                } else {
                    throw new RuntimeException("Invalid escape at " + this.reader.describePosition() + ':' + i2);
                }
            } else if ((charAt != 0 && charAt2 == charAt) || (charAt == 0 && PatternProps.isWhiteSpace(charAt2))) {
                return i2 + 1;
            } else {
                if (charAt == 0 && charAt2 == '#') {
                    return i2;
                }
                this.buf.append(charAt2);
                i2++;
            }
        }
        if (charAt == 0) {
            return i2;
        }
        throw new RuntimeException("Unterminated quote at " + this.reader.describePosition() + ':' + skipWhiteSpace);
    }
}
