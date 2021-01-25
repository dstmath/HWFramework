package ohos.com.sun.org.apache.xalan.internal.xsltc.dom;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class BitArray implements Externalizable {
    private static final boolean DEBUG_ASSERTIONS = false;
    private static final int[] _masks = {Integer.MIN_VALUE, 1073741824, 536870912, 268435456, 134217728, 67108864, 33554432, 16777216, 8388608, 4194304, 2097152, 1048576, 524288, 262144, 131072, 65536, 32768, 16384, 8192, 4096, 2048, 1024, 512, 256, 128, 64, 32, 16, 8, 4, 2, 1};
    static final long serialVersionUID = -4876019880708377663L;
    private int _bit;
    private int _bitSize;
    private int[] _bits;
    int _first;
    private int _int;
    private int _intSize;
    int _last;
    private int _mask;
    private int _node;
    private int _pos;

    public BitArray() {
        this(32);
    }

    public BitArray(int i) {
        this._pos = Integer.MAX_VALUE;
        this._node = 0;
        this._int = 0;
        this._bit = 0;
        this._first = Integer.MAX_VALUE;
        this._last = Integer.MIN_VALUE;
        this._bitSize = i < 32 ? 32 : i;
        this._intSize = (this._bitSize >>> 5) + 1;
        this._bits = new int[(this._intSize + 1)];
    }

    public BitArray(int i, int[] iArr) {
        this._pos = Integer.MAX_VALUE;
        this._node = 0;
        this._int = 0;
        this._bit = 0;
        this._first = Integer.MAX_VALUE;
        this._last = Integer.MIN_VALUE;
        this._bitSize = i < 32 ? 32 : i;
        this._intSize = (this._bitSize >>> 5) + 1;
        this._bits = iArr;
    }

    public void setMask(int i) {
        this._mask = i;
    }

    public int getMask() {
        return this._mask;
    }

    public final int size() {
        return this._bitSize;
    }

    public final boolean getBit(int i) {
        return (this._bits[i >>> 5] & _masks[i % 32]) != 0;
    }

    public final int getNextBit(int i) {
        for (int i2 = i >>> 5; i2 <= this._intSize; i2++) {
            int i3 = this._bits[i2];
            if (i3 != 0) {
                for (int i4 = i % 32; i4 < 32; i4++) {
                    if ((_masks[i4] & i3) != 0) {
                        return (i2 << 5) + i4;
                    }
                }
                continue;
            }
            i = 0;
        }
        return -1;
    }

    public final int getBitNumber(int i) {
        int i2 = this._pos;
        if (i == i2) {
            return this._node;
        }
        if (i < i2) {
            this._pos = 0;
            this._bit = 0;
            this._int = 0;
        }
        while (true) {
            int i3 = this._int;
            if (i3 > this._intSize) {
                return 0;
            }
            int i4 = this._bits[i3];
            if (i4 != 0) {
                while (true) {
                    int i5 = this._bit;
                    if (i5 >= 32) {
                        this._bit = 0;
                        break;
                    }
                    if ((_masks[i5] & i4) != 0) {
                        int i6 = this._pos + 1;
                        this._pos = i6;
                        if (i6 == i) {
                            this._node = ((this._int << 5) + i5) - 1;
                            return this._node;
                        }
                    }
                    this._bit++;
                }
            }
            this._int++;
        }
    }

    public final int[] data() {
        return this._bits;
    }

    public final void setBit(int i) {
        if (i < this._bitSize) {
            int i2 = i >>> 5;
            if (i2 < this._first) {
                this._first = i2;
            }
            if (i2 > this._last) {
                this._last = i2;
            }
            int[] iArr = this._bits;
            iArr[i2] = _masks[i % 32] | iArr[i2];
        }
    }

    public final BitArray merge(BitArray bitArray) {
        if (this._last == -1) {
            this._bits = bitArray._bits;
        } else if (bitArray._last != -1) {
            int i = this._first;
            int i2 = bitArray._first;
            if (i >= i2) {
                i = i2;
            }
            int i3 = this._last;
            int i4 = bitArray._last;
            if (i3 <= i4) {
                i3 = i4;
            }
            int i5 = bitArray._intSize;
            int i6 = this._intSize;
            if (i5 > i6) {
                if (i3 > i6) {
                    i3 = i6;
                }
                while (i <= i3) {
                    int[] iArr = bitArray._bits;
                    iArr[i] = iArr[i] | this._bits[i];
                    i++;
                }
                this._bits = bitArray._bits;
            } else {
                if (i3 > i5) {
                    i3 = i5;
                }
                while (i <= i3) {
                    int[] iArr2 = this._bits;
                    iArr2[i] = iArr2[i] | bitArray._bits[i];
                    i++;
                }
            }
        }
        return this;
    }

    public final void resize(int i) {
        int i2 = this._bitSize;
        if (i > i2) {
            this._intSize = (i >>> 5) + 1;
            int[] iArr = new int[(this._intSize + 1)];
            System.arraycopy(this._bits, 0, iArr, 0, (i2 >>> 5) + 1);
            this._bits = iArr;
            this._bitSize = i;
        }
    }

    public BitArray cloneArray() {
        return new BitArray(this._intSize, this._bits);
    }

    @Override // java.io.Externalizable
    public void writeExternal(ObjectOutput objectOutput) throws IOException {
        objectOutput.writeInt(this._bitSize);
        objectOutput.writeInt(this._mask);
        objectOutput.writeObject(this._bits);
        objectOutput.flush();
    }

    @Override // java.io.Externalizable
    public void readExternal(ObjectInput objectInput) throws IOException, ClassNotFoundException {
        this._bitSize = objectInput.readInt();
        this._intSize = (this._bitSize >>> 5) + 1;
        this._mask = objectInput.readInt();
        this._bits = (int[]) objectInput.readObject();
    }
}
