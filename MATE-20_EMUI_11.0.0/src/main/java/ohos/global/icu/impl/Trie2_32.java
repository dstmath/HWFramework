package ohos.global.icu.impl;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class Trie2_32 extends Trie2 {
    Trie2_32() {
    }

    public static Trie2_32 createFromSerialized(ByteBuffer byteBuffer) throws IOException {
        return (Trie2_32) Trie2.createFromSerialized(byteBuffer);
    }

    @Override // ohos.global.icu.impl.Trie2
    public final int get(int i) {
        if (i >= 0) {
            if (i < 55296 || (i > 56319 && i <= 65535)) {
                return this.data32[(this.index[i >> 5] << 2) + (i & 31)];
            } else if (i <= 65535) {
                return this.data32[(this.index[((i - 55296) >> 5) + 2048] << 2) + (i & 31)];
            } else if (i < this.highStart) {
                int i2 = this.index[(i >> 11) + 2080] + ((i >> 5) & 63);
                return this.data32[(this.index[i2] << 2) + (i & 31)];
            } else if (i <= 1114111) {
                return this.data32[this.highValueIndex];
            }
        }
        return this.errorValue;
    }

    @Override // ohos.global.icu.impl.Trie2
    public int getFromU16SingleLead(char c) {
        return this.data32[(this.index[c >> 5] << 2) + (c & 31)];
    }

    public int serialize(OutputStream outputStream) throws IOException {
        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
        int serializeHeader = serializeHeader(dataOutputStream) + 0;
        for (int i = 0; i < this.dataLength; i++) {
            dataOutputStream.writeInt(this.data32[i]);
        }
        return serializeHeader + (this.dataLength * 4);
    }

    public int getSerializedLength() {
        return (this.header.indexLength * 2) + 16 + (this.dataLength * 4);
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.global.icu.impl.Trie2
    public int rangeEnd(int i, int i2, int i3) {
        char c;
        loop0:
        while (true) {
            if (i >= i2) {
                break;
            }
            char c2 = 2048;
            if (i < 55296 || (i > 56319 && i <= 65535)) {
                c2 = 0;
                c = this.index[i >> 5];
            } else if (i < 65535) {
                c = this.index[((i - 55296) >> 5) + 2048];
            } else if (i < this.highStart) {
                c2 = this.index[(i >> 11) + 2080];
                c = this.index[((i >> 5) & 63) + c2];
            } else if (i3 == this.data32[this.highValueIndex]) {
                i = i2;
            }
            int i4 = c << 2;
            if (c2 == this.index2NullOffset) {
                if (i3 != this.initialValue) {
                    break;
                }
                i += 2048;
            } else if (i4 != this.dataNullOffset) {
                int i5 = (i & 31) + i4;
                int i6 = i4 + 32;
                for (int i7 = i5; i7 < i6; i7++) {
                    if (this.data32[i7] != i3) {
                        i += i7 - i5;
                        break loop0;
                    }
                }
                i += i6 - i5;
            } else if (i3 != this.initialValue) {
                break;
            } else {
                i += 32;
            }
        }
        if (i > i2) {
            i = i2;
        }
        return i - 1;
    }
}
