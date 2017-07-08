package android.icu.impl;

import android.icu.text.UTF16;
import android.icu.text.UnicodeSet;
import com.android.dex.DexFormat;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import org.w3c.dom.traversal.NodeFilter;

public final class Trie2_16 extends Trie2 {
    Trie2_16() {
    }

    public static Trie2_16 createFromSerialized(ByteBuffer bytes) throws IOException {
        return (Trie2_16) Trie2.createFromSerialized(bytes);
    }

    public final int get(int codePoint) {
        if (codePoint >= 0) {
            if (codePoint < UTF16.SURROGATE_MIN_VALUE || (codePoint > UTF16.LEAD_SURROGATE_MAX_VALUE && codePoint <= DexFormat.MAX_TYPE_IDX)) {
                return this.index[(this.index[codePoint >> 5] << 2) + (codePoint & 31)];
            } else if (codePoint <= DexFormat.MAX_TYPE_IDX) {
                return this.index[(this.index[((codePoint - UTF16.SURROGATE_MIN_VALUE) >> 5) + NodeFilter.SHOW_NOTATION] << 2) + (codePoint & 31)];
            } else if (codePoint < this.highStart) {
                return this.index[(this.index[this.index[(codePoint >> 11) + 2080] + ((codePoint >> 5) & 63)] << 2) + (codePoint & 31)];
            } else if (codePoint <= UnicodeSet.MAX_VALUE) {
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
            if (cp < UTF16.SURROGATE_MIN_VALUE || (cp > UTF16.LEAD_SURROGATE_MAX_VALUE && cp <= DexFormat.MAX_TYPE_IDX)) {
                index2Block = 0;
                block = this.index[cp >> 5] << 2;
            } else if (cp < DexFormat.MAX_TYPE_IDX) {
                index2Block = NodeFilter.SHOW_NOTATION;
                block = this.index[((cp - UTF16.SURROGATE_MIN_VALUE) >> 5) + NodeFilter.SHOW_NOTATION] << 2;
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
                cp += NodeFilter.SHOW_NOTATION;
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
