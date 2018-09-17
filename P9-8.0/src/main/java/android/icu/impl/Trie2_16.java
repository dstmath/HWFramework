package android.icu.impl;

import android.icu.text.DateTimePatternGenerator;
import android.icu.text.UTF16;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public final class Trie2_16 extends Trie2 {
    Trie2_16() {
    }

    public static Trie2_16 createFromSerialized(ByteBuffer bytes) throws IOException {
        return (Trie2_16) Trie2.createFromSerialized(bytes);
    }

    public final int get(int codePoint) {
        if (codePoint >= 0) {
            if (codePoint < 55296 || (codePoint > UTF16.LEAD_SURROGATE_MAX_VALUE && codePoint <= DateTimePatternGenerator.MATCH_ALL_FIELDS_LENGTH)) {
                return this.index[(this.index[codePoint >> 5] << 2) + (codePoint & 31)];
            } else if (codePoint <= DateTimePatternGenerator.MATCH_ALL_FIELDS_LENGTH) {
                return this.index[(this.index[((codePoint - 55296) >> 5) + 2048] << 2) + (codePoint & 31)];
            } else if (codePoint < this.highStart) {
                return this.index[(this.index[this.index[(codePoint >> 11) + 2080] + ((codePoint >> 5) & 63)] << 2) + (codePoint & 31)];
            } else if (codePoint <= 1114111) {
                return this.index[this.highValueIndex];
            }
        }
        return this.errorValue;
    }

    public int getFromU16SingleLead(char codeUnit) {
        return this.index[(this.index[codeUnit >> 5] << 2) + (codeUnit & 31)];
    }

    public int serialize(OutputStream os) throws IOException {
        DataOutputStream dos = new DataOutputStream(os);
        int bytesWritten = serializeHeader(dos) + 0;
        for (int i = 0; i < this.dataLength; i++) {
            dos.writeChar(this.index[this.data16 + i]);
        }
        return bytesWritten + (this.dataLength * 2);
    }

    public int getSerializedLength() {
        return ((this.header.indexLength + this.dataLength) * 2) + 16;
    }

    int rangeEnd(int startingCP, int limit, int value) {
        int cp = startingCP;
        loop0:
        while (cp < limit) {
            int index2Block;
            int block;
            if (cp < 55296 || (cp > UTF16.LEAD_SURROGATE_MAX_VALUE && cp <= DateTimePatternGenerator.MATCH_ALL_FIELDS_LENGTH)) {
                index2Block = 0;
                block = this.index[cp >> 5] << 2;
            } else if (cp < DateTimePatternGenerator.MATCH_ALL_FIELDS_LENGTH) {
                index2Block = 2048;
                block = this.index[((cp - 55296) >> 5) + 2048] << 2;
            } else if (cp < this.highStart) {
                index2Block = this.index[(cp >> 11) + 2080];
                block = this.index[((cp >> 5) & 63) + index2Block] << 2;
            } else if (value == this.index[this.highValueIndex]) {
                cp = limit;
            }
            if (index2Block == this.index2NullOffset) {
                if (value != this.initialValue) {
                    break;
                }
                cp += 2048;
            } else if (block == this.dataNullOffset) {
                if (value != this.initialValue) {
                    break;
                }
                cp += 32;
            } else {
                int startIx = block + (cp & 31);
                int limitIx = block + 32;
                for (int ix = startIx; ix < limitIx; ix++) {
                    if (this.index[ix] != value) {
                        cp += ix - startIx;
                        break loop0;
                    }
                }
                cp += limitIx - startIx;
            }
        }
        if (cp > limit) {
            cp = limit;
        }
        return cp - 1;
    }
}
