package sun.security.util;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

public class BitArray {
    private static final int BITS_PER_UNIT = 8;
    private static final int BYTES_PER_LINE = 8;
    private static final byte[][] NYBBLE = new byte[][]{new byte[]{(byte) 48, (byte) 48, (byte) 48, (byte) 48}, new byte[]{(byte) 48, (byte) 48, (byte) 48, (byte) 49}, new byte[]{(byte) 48, (byte) 48, (byte) 49, (byte) 48}, new byte[]{(byte) 48, (byte) 48, (byte) 49, (byte) 49}, new byte[]{(byte) 48, (byte) 49, (byte) 48, (byte) 48}, new byte[]{(byte) 48, (byte) 49, (byte) 48, (byte) 49}, new byte[]{(byte) 48, (byte) 49, (byte) 49, (byte) 48}, new byte[]{(byte) 48, (byte) 49, (byte) 49, (byte) 49}, new byte[]{(byte) 49, (byte) 48, (byte) 48, (byte) 48}, new byte[]{(byte) 49, (byte) 48, (byte) 48, (byte) 49}, new byte[]{(byte) 49, (byte) 48, (byte) 49, (byte) 48}, new byte[]{(byte) 49, (byte) 48, (byte) 49, (byte) 49}, new byte[]{(byte) 49, (byte) 49, (byte) 48, (byte) 48}, new byte[]{(byte) 49, (byte) 49, (byte) 48, (byte) 49}, new byte[]{(byte) 49, (byte) 49, (byte) 49, (byte) 48}, new byte[]{(byte) 49, (byte) 49, (byte) 49, (byte) 49}};
    private int length;
    private byte[] repn;

    private static int subscript(int idx) {
        return idx / 8;
    }

    private static int position(int idx) {
        return 1 << (7 - (idx % 8));
    }

    public BitArray(int length) throws IllegalArgumentException {
        if (length < 0) {
            throw new IllegalArgumentException("Negative length for BitArray");
        }
        this.length = length;
        this.repn = new byte[(((length + 8) - 1) / 8)];
    }

    public BitArray(int length, byte[] a) throws IllegalArgumentException {
        if (length < 0) {
            throw new IllegalArgumentException("Negative length for BitArray");
        } else if (a.length * 8 < length) {
            throw new IllegalArgumentException("Byte array too short to represent bit array of given length");
        } else {
            this.length = length;
            int repLength = ((length + 8) - 1) / 8;
            byte bitMask = (byte) (255 << ((repLength * 8) - length));
            this.repn = new byte[repLength];
            System.arraycopy(a, 0, this.repn, 0, repLength);
            if (repLength > 0) {
                byte[] bArr = this.repn;
                int i = repLength - 1;
                bArr[i] = (byte) (bArr[i] & bitMask);
            }
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
        if (index < 0 || index >= this.length) {
            throw new ArrayIndexOutOfBoundsException(Integer.toString(index));
        } else if ((this.repn[subscript(index)] & position(index)) != 0) {
            return true;
        } else {
            return false;
        }
    }

    public void set(int index, boolean value) throws ArrayIndexOutOfBoundsException {
        if (index < 0 || index >= this.length) {
            throw new ArrayIndexOutOfBoundsException(Integer.toString(index));
        }
        int idx = subscript(index);
        int bit = position(index);
        byte[] bArr;
        if (value) {
            bArr = this.repn;
            bArr[idx] = (byte) (bArr[idx] | bit);
            return;
        }
        bArr = this.repn;
        bArr[idx] = (byte) (bArr[idx] & (~bit));
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
        if (obj == null || ((obj instanceof BitArray) ^ 1) != 0) {
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
            hashCode = (hashCode * 31) + b;
        }
        return this.length ^ hashCode;
    }

    public Object clone() {
        return new BitArray(this);
    }

    public String toString() {
        int i;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (i = 0; i < this.repn.length - 1; i++) {
            out.write(NYBBLE[(this.repn[i] >> 4) & 15], 0, 4);
            out.write(NYBBLE[this.repn[i] & 15], 0, 4);
            if (i % 8 == 7) {
                out.write(10);
            } else {
                out.write(32);
            }
        }
        for (i = (this.repn.length - 1) * 8; i < this.length; i++) {
            out.write(get(i) ? 49 : 48);
        }
        return new String(out.toByteArray());
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
