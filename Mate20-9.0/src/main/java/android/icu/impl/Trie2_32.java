package android.icu.impl;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class Trie2_32 extends Trie2 {
    Trie2_32() {
    }

    public static Trie2_32 createFromSerialized(ByteBuffer bytes) throws IOException {
        return (Trie2_32) Trie2.createFromSerialized(bytes);
    }

    public final int get(int codePoint) {
        if (codePoint >= 0) {
            if (codePoint < 55296 || (codePoint > 56319 && codePoint <= 65535)) {
                return this.data32[(this.index[codePoint >> 5] << 2) + (codePoint & 31)];
            } else if (codePoint <= 65535) {
                return this.data32[(this.index[2048 + ((codePoint - 55296) >> 5)] << 2) + (codePoint & 31)];
            } else if (codePoint < this.highStart) {
                int ix = this.index[2080 + (codePoint >> 11)] + ((codePoint >> 5) & '?');
                return this.data32[(this.index[ix] << 2) + (codePoint & 31)];
            } else if (codePoint <= 1114111) {
                return this.data32[this.highValueIndex];
            }
        }
        return this.errorValue;
    }

    public int getFromU16SingleLead(char codeUnit) {
        return this.data32[(this.index[codeUnit >> 5] << 2) + (codeUnit & 31)];
    }

    public int serialize(OutputStream os) throws IOException {
        DataOutputStream dos = new DataOutputStream(os);
        int bytesWritten = 0 + serializeHeader(dos);
        for (int i = 0; i < this.dataLength; i++) {
            dos.writeInt(this.data32[i]);
        }
        return bytesWritten + (this.dataLength * 4);
    }

    public int getSerializedLength() {
        return 16 + (this.header.indexLength * 2) + (this.dataLength * 4);
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Incorrect type for immutable var: ssa=char, code=int, for r2v3, types: [char, int] */
    public int rangeEnd(int startingCP, int limit, int value) {
        int index2Block;
        int block;
        int cp = startingCP;
        loop0:
        while (true) {
            if (cp >= limit) {
                break;
            }
            if (cp < 55296 || (cp > 56319 && cp <= 65535)) {
                index2Block = 0;
                block = this.index[cp >> 5] << 2;
            } else if (cp < 65535) {
                index2Block = 2048;
                block = this.index[((cp - 55296) >> 5) + 2048] << 2;
            } else if (cp < this.highStart) {
                index2Block = this.index[2080 + (cp >> 11)];
                block = this.index[((cp >> 5) & '?') + index2Block] << 2;
            } else if (value == this.data32[this.highValueIndex]) {
                cp = limit;
            }
            if (index2Block == this.index2NullOffset) {
                if (value != this.initialValue) {
                    break;
                }
                cp += 2048;
            } else if (block != this.dataNullOffset) {
                int startIx = (cp & 31) + block;
                int limitIx = block + 32;
                for (int ix = startIx; ix < limitIx; ix++) {
                    if (this.data32[ix] != value) {
                        cp += ix - startIx;
                        break loop0;
                    }
                }
                cp += limitIx - startIx;
            } else if (value != this.initialValue) {
                break;
            } else {
                cp += 32;
            }
        }
        if (cp > limit) {
            cp = limit;
        }
        return cp - 1;
    }
}
