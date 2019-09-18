package sun.security.util;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

public class BitArray {
    private static final int BITS_PER_UNIT = 8;
    private static final int BYTES_PER_LINE = 8;
    private static final byte[][] NYBBLE = {new byte[]{48, 48, 48, 48}, new byte[]{48, 48, 48, 49}, new byte[]{48, 48, 49, 48}, new byte[]{48, 48, 49, 49}, new byte[]{48, 49, 48, 48}, new byte[]{48, 49, 48, 49}, new byte[]{48, 49, 49, 48}, new byte[]{48, 49, 49, 49}, new byte[]{49, 48, 48, 48}, new byte[]{49, 48, 48, 49}, new byte[]{49, 48, 49, 48}, new byte[]{49, 48, 49, 49}, new byte[]{49, 49, 48, 48}, new byte[]{49, 49, 48, 49}, new byte[]{49, 49, 49, 48}, new byte[]{49, 49, 49, 49}};
    private int length;
    private byte[] repn;

    private static int subscript(int idx) {
        return idx / 8;
    }

    private static int position(int idx) {
        return 1 << (7 - (idx % 8));
    }

    public BitArray(int length2) throws IllegalArgumentException {
        if (length2 >= 0) {
            this.length = length2;
            this.repn = new byte[(((length2 + 8) - 1) / 8)];
            return;
        }
        throw new IllegalArgumentException("Negative length for BitArray");
    }

    public BitArray(int length2, byte[] a) throws IllegalArgumentException {
        if (length2 < 0) {
            throw new IllegalArgumentException("Negative length for BitArray");
        } else if (a.length * 8 >= length2) {
            this.length = length2;
            int repLength = ((length2 + 8) - 1) / 8;
            byte bitMask = (byte) (255 << ((repLength * 8) - length2));
            this.repn = new byte[repLength];
            System.arraycopy(a, 0, this.repn, 0, repLength);
            if (repLength > 0) {
                byte[] bArr = this.repn;
                int i = repLength - 1;
                bArr[i] = (byte) (bArr[i] & bitMask);
            }
        } else {
            throw new IllegalArgumentException("Byte array too short to represent bit array of given length");
        }
    }

    public BitArray(boolean[] bits) {
        this.length = bits.length;
        this.repn = new byte[((this.length + 7) / 8)];
        for (int i = 0; i < this.length; i++) {
            set(i, bits[i]);
        }
    }

    private BitArray(BitArray ba) {
        this.length = ba.length;
        this.repn = (byte[]) ba.repn.clone();
    }

    public boolean get(int index) throws ArrayIndexOutOfBoundsException {
        if (index >= 0 && index < this.length) {
            return (this.repn[subscript(index)] & position(index)) != 0;
        }
        throw new ArrayIndexOutOfBoundsException(Integer.toString(index));
    }

    public void set(int index, boolean value) throws ArrayIndexOutOfBoundsException {
        if (index < 0 || index >= this.length) {
            throw new ArrayIndexOutOfBoundsException(Integer.toString(index));
        }
        int idx = subscript(index);
        int bit = position(index);
        if (value) {
            byte[] bArr = this.repn;
            bArr[idx] = (byte) (bArr[idx] | bit);
            return;
        }
        byte[] bArr2 = this.repn;
        bArr2[idx] = (byte) (bArr2[idx] & (~bit));
    }

    public int length() {
        return this.length;
    }

    public byte[] toByteArray() {
        return (byte[]) this.repn.clone();
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || !(obj instanceof BitArray)) {
            return false;
        }
        BitArray ba = (BitArray) obj;
        if (ba.length != this.length) {
            return false;
        }
        for (int i = 0; i < this.repn.length; i++) {
            if (this.repn[i] != ba.repn[i]) {
                return false;
            }
        }
        return true;
    }

    public boolean[] toBooleanArray() {
        boolean[] bits = new boolean[this.length];
        for (int i = 0; i < this.length; i++) {
            bits[i] = get(i);
        }
        return bits;
    }

    public int hashCode() {
        int hashCode = 0;
        for (byte b : this.repn) {
            hashCode = (31 * hashCode) + b;
        }
        return this.length ^ hashCode;
    }

    public Object clone() {
        return new BitArray(this);
    }

    public String toString() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (int i = 0; i < this.repn.length - 1; i++) {
            out.write(NYBBLE[(this.repn[i] >> 4) & 15], 0, 4);
            out.write(NYBBLE[this.repn[i] & 15], 0, 4);
            if (i % 8 == 7) {
                out.write(10);
            } else {
                out.write(32);
            }
        }
        int i2 = 8 * (this.repn.length - 1);
        while (true) {
            int i3 = i2;
            if (i3 >= this.length) {
                return new String(out.toByteArray());
            }
            out.write(get(i3) ? 49 : 48);
            i2 = i3 + 1;
        }
    }

    public BitArray truncate() {
        for (int i = this.length - 1; i >= 0; i--) {
            if (get(i)) {
                return new BitArray(i + 1, Arrays.copyOf(this.repn, (i + 8) / 8));
            }
        }
        return new BitArray(1);
    }
}
