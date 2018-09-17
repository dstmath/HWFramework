package sun.security.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Arrays;

public final class ObjectIdentifier implements Serializable {
    static final /* synthetic */ boolean -assertionsDisabled = (ObjectIdentifier.class.desiredAssertionStatus() ^ 1);
    private static final long serialVersionUID = 8697030238860181294L;
    private int componentLen = -1;
    private Object components = null;
    private transient boolean componentsCalculated = -assertionsDisabled;
    private byte[] encoding = null;
    private volatile transient String stringForm;

    static class HugeOidNotSupportedByOldJDK implements Serializable {
        private static final long serialVersionUID = 1;
        static HugeOidNotSupportedByOldJDK theOne = new HugeOidNotSupportedByOldJDK();

        HugeOidNotSupportedByOldJDK() {
        }
    }

    private void readObject(ObjectInputStream is) throws IOException, ClassNotFoundException {
        is.defaultReadObject();
        if (this.encoding == null) {
            init((int[]) this.components, this.componentLen);
        }
    }

    private void writeObject(ObjectOutputStream os) throws IOException {
        if (!this.componentsCalculated) {
            int[] comps = toIntArray();
            if (comps != null) {
                this.components = comps;
                this.componentLen = comps.length;
            } else {
                this.components = HugeOidNotSupportedByOldJDK.theOne;
            }
            this.componentsCalculated = true;
        }
        os.defaultWriteObject();
    }

    public ObjectIdentifier(String oid) throws IOException {
        int start = 0;
        int pos = 0;
        byte[] tmp = new byte[oid.length()];
        int first = 0;
        int count = 0;
        int end;
        do {
            try {
                String comp;
                int length;
                end = oid.indexOf(46, start);
                if (end == -1) {
                    comp = oid.substring(start);
                    length = oid.length() - start;
                } else {
                    comp = oid.substring(start, end);
                    length = end - start;
                }
                if (length > 9) {
                    BigInteger bignum = new BigInteger(comp);
                    if (count == 0) {
                        checkFirstComponent(bignum);
                        first = bignum.intValue();
                    } else {
                        if (count == 1) {
                            checkSecondComponent(first, bignum);
                            bignum = bignum.add(BigInteger.valueOf((long) (first * 40)));
                        } else {
                            checkOtherComponent(count, bignum);
                        }
                        pos += pack7Oid(bignum, tmp, pos);
                    }
                } else {
                    int num = Integer.parseInt(comp);
                    if (count == 0) {
                        checkFirstComponent(num);
                        first = num;
                    } else {
                        if (count == 1) {
                            checkSecondComponent(first, num);
                            num += first * 40;
                        } else {
                            checkOtherComponent(count, num);
                        }
                        pos += pack7Oid(num, tmp, pos);
                    }
                }
                start = end + 1;
                count++;
            } catch (IOException ioe) {
                throw ioe;
            } catch (Exception e) {
                throw new IOException("ObjectIdentifier() -- Invalid format: " + e.toString(), e);
            }
        } while (end != -1);
        checkCount(count);
        this.encoding = new byte[pos];
        System.arraycopy(tmp, 0, this.encoding, 0, pos);
        this.stringForm = oid;
    }

    public ObjectIdentifier(int[] values) throws IOException {
        checkCount(values.length);
        checkFirstComponent(values[0]);
        checkSecondComponent(values[0], values[1]);
        for (int i = 2; i < values.length; i++) {
            checkOtherComponent(i, values[i]);
        }
        init(values, values.length);
    }

    public ObjectIdentifier(DerInputStream in) throws IOException {
        int type_id = (byte) in.getByte();
        if (type_id != (byte) 6) {
            throw new IOException("ObjectIdentifier() -- data isn't an object ID (tag = " + type_id + ")");
        }
        this.encoding = new byte[in.getLength()];
        in.getBytes(this.encoding);
        check(this.encoding);
    }

    ObjectIdentifier(DerInputBuffer buf) throws IOException {
        DerInputStream in = new DerInputStream(buf);
        this.encoding = new byte[in.available()];
        in.getBytes(this.encoding);
        check(this.encoding);
    }

    private void init(int[] components, int length) {
        int pos;
        byte[] tmp = new byte[((length * 5) + 1)];
        if (components[1] < Integer.MAX_VALUE - (components[0] * 40)) {
            pos = pack7Oid((components[0] * 40) + components[1], tmp, 0) + 0;
        } else {
            pos = pack7Oid(BigInteger.valueOf((long) components[1]).add(BigInteger.valueOf((long) (components[0] * 40))), tmp, 0) + 0;
        }
        for (int i = 2; i < length; i++) {
            pos += pack7Oid(components[i], tmp, pos);
        }
        this.encoding = new byte[pos];
        System.arraycopy(tmp, 0, this.encoding, 0, pos);
    }

    public static ObjectIdentifier newInternal(int[] values) {
        try {
            return new ObjectIdentifier(values);
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }

    void encode(DerOutputStream out) throws IOException {
        out.write((byte) 6, this.encoding);
    }

    @Deprecated
    public boolean equals(ObjectIdentifier other) {
        return equals((Object) other);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ObjectIdentifier)) {
            return -assertionsDisabled;
        }
        return Arrays.equals(this.encoding, ((ObjectIdentifier) obj).encoding);
    }

    public int hashCode() {
        return Arrays.hashCode(this.encoding);
    }

    public int[] toIntArray() {
        int length = this.encoding.length;
        int[] result = new int[20];
        int fromPos = 0;
        int i = 0;
        int which = 0;
        while (i < length) {
            int i2;
            if ((this.encoding[i] & 128) == 0) {
                if ((i - fromPos) + 1 > 4) {
                    BigInteger big = new BigInteger(pack(this.encoding, fromPos, (i - fromPos) + 1, 7, 8));
                    if (fromPos == 0) {
                        i2 = which + 1;
                        result[which] = 2;
                        BigInteger second = big.subtract(BigInteger.valueOf(80));
                        if (second.compareTo(BigInteger.valueOf(2147483647L)) == 1) {
                            return null;
                        }
                        which = i2 + 1;
                        result[i2] = second.intValue();
                        i2 = which;
                    } else if (big.compareTo(BigInteger.valueOf(2147483647L)) == 1) {
                        return null;
                    } else {
                        i2 = which + 1;
                        result[which] = big.intValue();
                    }
                } else {
                    int retval = 0;
                    for (int j = fromPos; j <= i; j++) {
                        retval = (retval << 7) | (this.encoding[j] & 127);
                    }
                    if (fromPos != 0) {
                        i2 = which + 1;
                        result[which] = retval;
                    } else if (retval < 80) {
                        i2 = which + 1;
                        result[which] = retval / 40;
                        which = i2 + 1;
                        result[i2] = retval % 40;
                        i2 = which;
                    } else {
                        i2 = which + 1;
                        result[which] = 2;
                        which = i2 + 1;
                        result[i2] = retval - 80;
                        i2 = which;
                    }
                }
                fromPos = i + 1;
            } else {
                i2 = which;
            }
            if (i2 >= result.length) {
                result = Arrays.copyOf(result, i2 + 10);
            }
            i++;
            which = i2;
        }
        return Arrays.copyOf(result, which);
    }

    public String toString() {
        String s = this.stringForm;
        if (s != null) {
            return s;
        }
        int length = this.encoding.length;
        StringBuffer sb = new StringBuffer(length * 4);
        int fromPos = 0;
        for (int i = 0; i < length; i++) {
            if ((this.encoding[i] & 128) == 0) {
                if (fromPos != 0) {
                    sb.append('.');
                }
                if ((i - fromPos) + 1 > 4) {
                    Object big = new BigInteger(pack(this.encoding, fromPos, (i - fromPos) + 1, 7, 8));
                    if (fromPos == 0) {
                        sb.append("2.");
                        sb.append(big.subtract(BigInteger.valueOf(80)));
                    } else {
                        sb.append(big);
                    }
                } else {
                    int retval = 0;
                    for (int j = fromPos; j <= i; j++) {
                        retval = (retval << 7) | (this.encoding[j] & 127);
                    }
                    if (fromPos != 0) {
                        sb.append(retval);
                    } else if (retval < 80) {
                        sb.append(retval / 40);
                        sb.append('.');
                        sb.append(retval % 40);
                    } else {
                        sb.append("2.");
                        sb.append(retval - 80);
                    }
                }
                fromPos = i + 1;
            }
        }
        s = sb.toString();
        this.stringForm = s;
        return s;
    }

    private static byte[] pack(byte[] in, int ioffset, int ilength, int iw, int ow) {
        if (!-assertionsDisabled && (iw <= 0 || iw > 8)) {
            throw new AssertionError((Object) "input NUB must be between 1 and 8");
        } else if (!-assertionsDisabled && (ow <= 0 || ow > 8)) {
            throw new AssertionError((Object) "output NUB must be between 1 and 8");
        } else if (iw == ow) {
            return (byte[]) in.clone();
        } else {
            int bits = ilength * iw;
            byte[] out = new byte[(((bits + ow) - 1) / ow)];
            int ipos = 0;
            int opos = ((((bits + ow) - 1) / ow) * ow) - bits;
            while (ipos < bits) {
                int count = iw - (ipos % iw);
                if (count > ow - (opos % ow)) {
                    count = ow - (opos % ow);
                }
                int i = opos / ow;
                out[i] = (byte) (out[i] | ((((in[(ipos / iw) + ioffset] + 256) >> ((iw - (ipos % iw)) - count)) & ((1 << count) - 1)) << ((ow - (opos % ow)) - count)));
                ipos += count;
                opos += count;
            }
            return out;
        }
    }

    private static int pack7Oid(byte[] in, int ioffset, int ilength, byte[] out, int ooffset) {
        byte[] pack = pack(in, ioffset, ilength, 8, 7);
        int firstNonZero = pack.length - 1;
        for (int i = pack.length - 2; i >= 0; i--) {
            if (pack[i] != (byte) 0) {
                firstNonZero = i;
            }
            pack[i] = (byte) (pack[i] | 128);
        }
        System.arraycopy(pack, firstNonZero, out, ooffset, pack.length - firstNonZero);
        return pack.length - firstNonZero;
    }

    private static int pack8(byte[] in, int ioffset, int ilength, byte[] out, int ooffset) {
        byte[] pack = pack(in, ioffset, ilength, 7, 8);
        int firstNonZero = pack.length - 1;
        for (int i = pack.length - 2; i >= 0; i--) {
            if (pack[i] != (byte) 0) {
                firstNonZero = i;
            }
        }
        System.arraycopy(pack, firstNonZero, out, ooffset, pack.length - firstNonZero);
        return pack.length - firstNonZero;
    }

    private static int pack7Oid(int input, byte[] out, int ooffset) {
        return pack7Oid(new byte[]{(byte) (input >> 24), (byte) (input >> 16), (byte) (input >> 8), (byte) input}, 0, 4, out, ooffset);
    }

    private static int pack7Oid(BigInteger input, byte[] out, int ooffset) {
        byte[] b = input.toByteArray();
        return pack7Oid(b, 0, b.length, out, ooffset);
    }

    private static void check(byte[] encoding) throws IOException {
        int length = encoding.length;
        if (length < 1 || (encoding[length - 1] & 128) != 0) {
            throw new IOException("ObjectIdentifier() -- Invalid DER encoding, not ended");
        }
        int i = 0;
        while (i < length) {
            if (encoding[i] == Byte.MIN_VALUE && (i == 0 || (encoding[i - 1] & 128) == 0)) {
                throw new IOException("ObjectIdentifier() -- Invalid DER encoding, useless extra octet detected");
            }
            i++;
        }
    }

    private static void checkCount(int count) throws IOException {
        if (count < 2) {
            throw new IOException("ObjectIdentifier() -- Must be at least two oid components ");
        }
    }

    private static void checkFirstComponent(int first) throws IOException {
        if (first < 0 || first > 2) {
            throw new IOException("ObjectIdentifier() -- First oid component is invalid ");
        }
    }

    private static void checkFirstComponent(BigInteger first) throws IOException {
        if (first.signum() == -1 || first.compareTo(BigInteger.valueOf(2)) == 1) {
            throw new IOException("ObjectIdentifier() -- First oid component is invalid ");
        }
    }

    private static void checkSecondComponent(int first, int second) throws IOException {
        if (second < 0 || (first != 2 && second > 39)) {
            throw new IOException("ObjectIdentifier() -- Second oid component is invalid ");
        }
    }

    private static void checkSecondComponent(int first, BigInteger second) throws IOException {
        if (second.signum() == -1 || (first != 2 && second.compareTo(BigInteger.valueOf(39)) == 1)) {
            throw new IOException("ObjectIdentifier() -- Second oid component is invalid ");
        }
    }

    private static void checkOtherComponent(int i, int num) throws IOException {
        if (num < 0) {
            throw new IOException("ObjectIdentifier() -- oid component #" + (i + 1) + " must be non-negative ");
        }
    }

    private static void checkOtherComponent(int i, BigInteger num) throws IOException {
        if (num.signum() == -1) {
            throw new IOException("ObjectIdentifier() -- oid component #" + (i + 1) + " must be non-negative ");
        }
    }
}
