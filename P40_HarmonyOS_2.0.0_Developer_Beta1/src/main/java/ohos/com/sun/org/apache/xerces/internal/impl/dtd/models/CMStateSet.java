package ohos.com.sun.org.apache.xerces.internal.impl.dtd.models;

public class CMStateSet {
    int fBitCount;
    int fBits1;
    int fBits2;
    byte[] fByteArray;
    int fByteCount;

    public CMStateSet(int i) {
        this.fBitCount = i;
        int i2 = this.fBitCount;
        if (i2 >= 0) {
            if (i2 > 64) {
                this.fByteCount = i2 / 8;
                if (i2 % 8 != 0) {
                    this.fByteCount++;
                }
                this.fByteArray = new byte[this.fByteCount];
            }
            zeroBits();
            return;
        }
        throw new RuntimeException("ImplementationMessages.VAL_CMSI");
    }

    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        try {
            stringBuffer.append("{");
            for (int i = 0; i < this.fBitCount; i++) {
                if (getBit(i)) {
                    stringBuffer.append(" " + i);
                }
            }
            stringBuffer.append(" }");
        } catch (RuntimeException unused) {
        }
        return stringBuffer.toString();
    }

    public final void intersection(CMStateSet cMStateSet) {
        if (this.fBitCount < 65) {
            this.fBits1 &= cMStateSet.fBits1;
            this.fBits2 = cMStateSet.fBits2 & this.fBits2;
            return;
        }
        for (int i = this.fByteCount - 1; i >= 0; i--) {
            byte[] bArr = this.fByteArray;
            bArr[i] = (byte) (bArr[i] & cMStateSet.fByteArray[i]);
        }
    }

    public final boolean getBit(int i) {
        int i2 = this.fBitCount;
        if (i >= i2) {
            throw new RuntimeException("ImplementationMessages.VAL_CMSI");
        } else if (i2 < 65) {
            int i3 = 1 << (i % 32);
            return i < 32 ? (this.fBits1 & i3) != 0 : (this.fBits2 & i3) != 0;
        } else {
            return (this.fByteArray[i >> 3] & ((byte) (1 << (i % 8)))) != 0;
        }
    }

    public final boolean isEmpty() {
        if (this.fBitCount < 65) {
            return this.fBits1 == 0 && this.fBits2 == 0;
        }
        for (int i = this.fByteCount - 1; i >= 0; i--) {
            if (this.fByteArray[i] != 0) {
                return false;
            }
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public final boolean isSameSet(CMStateSet cMStateSet) {
        int i = this.fBitCount;
        if (i != cMStateSet.fBitCount) {
            return false;
        }
        if (i < 65) {
            return this.fBits1 == cMStateSet.fBits1 && this.fBits2 == cMStateSet.fBits2;
        }
        for (int i2 = this.fByteCount - 1; i2 >= 0; i2--) {
            if (this.fByteArray[i2] != cMStateSet.fByteArray[i2]) {
                return false;
            }
        }
        return true;
    }

    public final void union(CMStateSet cMStateSet) {
        if (this.fBitCount < 65) {
            this.fBits1 |= cMStateSet.fBits1;
            this.fBits2 = cMStateSet.fBits2 | this.fBits2;
            return;
        }
        for (int i = this.fByteCount - 1; i >= 0; i--) {
            byte[] bArr = this.fByteArray;
            bArr[i] = (byte) (bArr[i] | cMStateSet.fByteArray[i]);
        }
    }

    public final void setBit(int i) {
        int i2 = this.fBitCount;
        if (i >= i2) {
            throw new RuntimeException("ImplementationMessages.VAL_CMSI");
        } else if (i2 < 65) {
            int i3 = 1 << (i % 32);
            if (i < 32) {
                this.fBits1 &= ~i3;
                this.fBits1 |= i3;
                return;
            }
            this.fBits2 &= ~i3;
            this.fBits2 |= i3;
        } else {
            byte b = (byte) (1 << (i % 8));
            int i4 = i >> 3;
            byte[] bArr = this.fByteArray;
            bArr[i4] = (byte) (bArr[i4] & (~b));
            bArr[i4] = (byte) (b | bArr[i4]);
        }
    }

    public final void setTo(CMStateSet cMStateSet) {
        int i = this.fBitCount;
        if (i != cMStateSet.fBitCount) {
            throw new RuntimeException("ImplementationMessages.VAL_CMSI");
        } else if (i < 65) {
            this.fBits1 = cMStateSet.fBits1;
            this.fBits2 = cMStateSet.fBits2;
        } else {
            for (int i2 = this.fByteCount - 1; i2 >= 0; i2--) {
                this.fByteArray[i2] = cMStateSet.fByteArray[i2];
            }
        }
    }

    public final void zeroBits() {
        if (this.fBitCount < 65) {
            this.fBits1 = 0;
            this.fBits2 = 0;
            return;
        }
        for (int i = this.fByteCount - 1; i >= 0; i--) {
            this.fByteArray[i] = 0;
        }
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof CMStateSet)) {
            return false;
        }
        return isSameSet((CMStateSet) obj);
    }

    public int hashCode() {
        if (this.fBitCount < 65) {
            return this.fBits1 + (this.fBits2 * 31);
        }
        int i = 0;
        for (int i2 = this.fByteCount - 1; i2 >= 0; i2--) {
            i = (i * 31) + this.fByteArray[i2];
        }
        return i;
    }
}
