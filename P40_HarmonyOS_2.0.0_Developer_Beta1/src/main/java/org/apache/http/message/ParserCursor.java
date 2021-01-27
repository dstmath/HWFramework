package org.apache.http.message;

import org.apache.http.util.CharArrayBuffer;

@Deprecated
public class ParserCursor {
    private final int lowerBound;
    private int pos;
    private final int upperBound;

    public ParserCursor(int lowerBound2, int upperBound2) {
        if (lowerBound2 < 0) {
            throw new IndexOutOfBoundsException("Lower bound cannot be negative");
        } else if (lowerBound2 <= upperBound2) {
            this.lowerBound = lowerBound2;
            this.upperBound = upperBound2;
            this.pos = lowerBound2;
        } else {
            throw new IndexOutOfBoundsException("Lower bound cannot be greater then upper bound");
        }
    }

    public int getLowerBound() {
        return this.lowerBound;
    }

    public int getUpperBound() {
        return this.upperBound;
    }

    public int getPos() {
        return this.pos;
    }

    public void updatePos(int pos2) {
        if (pos2 < this.lowerBound) {
            throw new IndexOutOfBoundsException();
        } else if (pos2 <= this.upperBound) {
            this.pos = pos2;
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    public boolean atEnd() {
        return this.pos >= this.upperBound;
    }

    public String toString() {
        CharArrayBuffer buffer = new CharArrayBuffer(16);
        buffer.append('[');
        buffer.append(Integer.toString(this.lowerBound));
        buffer.append('>');
        buffer.append(Integer.toString(this.pos));
        buffer.append('>');
        buffer.append(Integer.toString(this.upperBound));
        buffer.append(']');
        return buffer.toString();
    }
}
