package sun.security.util;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Date;
import java.util.Vector;
import java.util.regex.Pattern;
import sun.misc.FloatConsts;
import sun.util.logging.PlatformLogger;

public class DerInputStream {
    DerInputBuffer buffer;
    public byte tag;

    public DerInputStream(byte[] data) throws IOException {
        init(data, 0, data.length);
    }

    public DerInputStream(byte[] data, int offset, int len) throws IOException {
        init(data, offset, len);
    }

    private void init(byte[] data, int offset, int len) throws IOException {
        if (offset + 2 > data.length || offset + len > data.length) {
            throw new IOException("Encoding bytes too short");
        }
        if (DerIndefLenConverter.isIndefinite(data[offset + 1])) {
            byte[] inData = new byte[len];
            System.arraycopy(data, offset, inData, 0, len);
            this.buffer = new DerInputBuffer(new DerIndefLenConverter().convert(inData));
        } else {
            this.buffer = new DerInputBuffer(data, offset, len);
        }
        this.buffer.mark(PlatformLogger.OFF);
    }

    DerInputStream(DerInputBuffer buf) {
        this.buffer = buf;
        this.buffer.mark(PlatformLogger.OFF);
    }

    public DerInputStream subStream(int len, boolean do_skip) throws IOException {
        DerInputBuffer newbuf = this.buffer.dup();
        newbuf.truncate(len);
        if (do_skip) {
            this.buffer.skip((long) len);
        }
        return new DerInputStream(newbuf);
    }

    public byte[] toByteArray() {
        return this.buffer.toByteArray();
    }

    public int getInteger() throws IOException {
        if (this.buffer.read() == 2) {
            return this.buffer.getInteger(getLength(this.buffer));
        }
        throw new IOException("DER input, Integer tag error");
    }

    public BigInteger getBigInteger() throws IOException {
        if (this.buffer.read() == 2) {
            return this.buffer.getBigInteger(getLength(this.buffer), false);
        }
        throw new IOException("DER input, Integer tag error");
    }

    public BigInteger getPositiveBigInteger() throws IOException {
        if (this.buffer.read() == 2) {
            return this.buffer.getBigInteger(getLength(this.buffer), true);
        }
        throw new IOException("DER input, Integer tag error");
    }

    public int getEnumerated() throws IOException {
        if (this.buffer.read() == 10) {
            return this.buffer.getInteger(getLength(this.buffer));
        }
        throw new IOException("DER input, Enumerated tag error");
    }

    public byte[] getBitString() throws IOException {
        if (this.buffer.read() == 3) {
            return this.buffer.getBitString(getLength(this.buffer));
        }
        throw new IOException("DER input not an bit string");
    }

    public BitArray getUnalignedBitString() throws IOException {
        if (this.buffer.read() != 3) {
            throw new IOException("DER input not a bit string");
        }
        int length = getLength(this.buffer) - 1;
        int validBits = (length * 8) - this.buffer.read();
        byte[] repn = new byte[length];
        if (length == 0 || this.buffer.read(repn) == length) {
            return new BitArray(validBits, repn);
        }
        throw new IOException("short read of DER bit string");
    }

    public byte[] getOctetString() throws IOException {
        if (this.buffer.read() != 4) {
            throw new IOException("DER input not an octet string");
        }
        int length = getLength(this.buffer);
        byte[] retval = new byte[length];
        if (length == 0 || this.buffer.read(retval) == length) {
            return retval;
        }
        throw new IOException("short read of DER octet string");
    }

    public void getBytes(byte[] val) throws IOException {
        if (val.length != 0 && this.buffer.read(val) != val.length) {
            throw new IOException("short read of DER octet string");
        }
    }

    public void getNull() throws IOException {
        if (this.buffer.read() != 5 || this.buffer.read() != 0) {
            throw new IOException("getNull, bad data");
        }
    }

    public ObjectIdentifier getOID() throws IOException {
        return new ObjectIdentifier(this);
    }

    public DerValue[] getSequence(int startLen, boolean originalEncodedFormRetained) throws IOException {
        this.tag = (byte) this.buffer.read();
        if (this.tag == 48) {
            return readVector(startLen, originalEncodedFormRetained);
        }
        throw new IOException("Sequence tag error");
    }

    public DerValue[] getSequence(int startLen) throws IOException {
        return getSequence(startLen, false);
    }

    public DerValue[] getSet(int startLen) throws IOException {
        this.tag = (byte) this.buffer.read();
        if (this.tag == 49) {
            return readVector(startLen);
        }
        throw new IOException("Set tag error");
    }

    public DerValue[] getSet(int startLen, boolean implicit) throws IOException {
        return getSet(startLen, implicit, false);
    }

    public DerValue[] getSet(int startLen, boolean implicit, boolean originalEncodedFormRetained) throws IOException {
        this.tag = (byte) this.buffer.read();
        if (implicit || this.tag == 49) {
            return readVector(startLen, originalEncodedFormRetained);
        }
        throw new IOException("Set tag error");
    }

    protected DerValue[] readVector(int startLen) throws IOException {
        return readVector(startLen, false);
    }

    protected DerValue[] readVector(int startLen, boolean originalEncodedFormRetained) throws IOException {
        byte lenByte = (byte) this.buffer.read();
        int len = getLength(lenByte & 255, this.buffer);
        if (len == -1) {
            int readLen = this.buffer.available();
            byte[] indefData = new byte[(readLen + 2)];
            indefData[0] = this.tag;
            indefData[1] = lenByte;
            DataInputStream dis = new DataInputStream(this.buffer);
            dis.readFully(indefData, 2, readLen);
            dis.close();
            this.buffer = new DerInputBuffer(new DerIndefLenConverter().convert(indefData));
            if (this.tag != this.buffer.read()) {
                throw new IOException("Indefinite length encoding not supported");
            }
            len = getLength(this.buffer);
        }
        if (len == 0) {
            return new DerValue[0];
        }
        DerInputStream newstr;
        if (this.buffer.available() == len) {
            newstr = this;
        } else {
            newstr = subStream(len, true);
        }
        Vector<DerValue> vec = new Vector(startLen);
        do {
            vec.addElement(new DerValue(newstr.buffer, originalEncodedFormRetained));
        } while (newstr.available() > 0);
        if (newstr.available() != 0) {
            throw new IOException("extra data at end of vector");
        }
        int max = vec.size();
        DerValue[] retval = new DerValue[max];
        for (int i = 0; i < max; i++) {
            retval[i] = (DerValue) vec.elementAt(i);
        }
        return retval;
    }

    public DerValue getDerValue() throws IOException {
        return new DerValue(this.buffer);
    }

    public String getUTF8String() throws IOException {
        return readString(DerValue.tag_UTF8String, "UTF-8", "UTF8");
    }

    public String getPrintableString() throws IOException {
        return readString(DerValue.tag_PrintableString, "Printable", "ASCII");
    }

    public String getT61String() throws IOException {
        return readString(DerValue.tag_T61String, "T61", "ISO-8859-1");
    }

    public String getIA5String() throws IOException {
        return readString(DerValue.tag_IA5String, "IA5", "ASCII");
    }

    public String getBMPString() throws IOException {
        return readString(DerValue.tag_BMPString, "BMP", "UnicodeBigUnmarked");
    }

    public String getGeneralString() throws IOException {
        return readString(DerValue.tag_GeneralString, "General", "ASCII");
    }

    private String readString(byte stringTag, String stringName, String enc) throws IOException {
        if (this.buffer.read() != stringTag) {
            throw new IOException("DER input not a " + stringName + " string");
        }
        int length = getLength(this.buffer);
        byte[] retval = new byte[length];
        if (length == 0 || this.buffer.read(retval) == length) {
            return new String(retval, enc);
        }
        throw new IOException("short read of DER " + stringName + " string");
    }

    public Date getUTCTime() throws IOException {
        if (this.buffer.read() == 23) {
            return this.buffer.getUTCTime(getLength(this.buffer));
        }
        throw new IOException("DER input, UTCtime tag invalid ");
    }

    public Date getGeneralizedTime() throws IOException {
        if (this.buffer.read() == 24) {
            return this.buffer.getGeneralizedTime(getLength(this.buffer));
        }
        throw new IOException("DER input, GeneralizedTime tag invalid ");
    }

    int getByte() throws IOException {
        return this.buffer.read() & 255;
    }

    public int peekByte() throws IOException {
        return this.buffer.peek();
    }

    int getLength() throws IOException {
        return getLength(this.buffer);
    }

    static int getLength(InputStream in) throws IOException {
        return getLength(in.read(), in);
    }

    static int getLength(int lenByte, InputStream in) throws IOException {
        int value;
        int tmp = lenByte;
        if ((lenByte & Pattern.CANON_EQ) == 0) {
            value = lenByte;
        } else {
            tmp = lenByte & FloatConsts.MAX_EXPONENT;
            if (tmp == 0) {
                return -1;
            }
            if (tmp < 0 || tmp > 4) {
                throw new IOException("DerInputStream.getLength(): lengthTag=" + tmp + ", " + (tmp < 0 ? "incorrect DER encoding." : "too big."));
            }
            value = 0;
            while (tmp > 0) {
                value = (value << 8) + (in.read() & 255);
                tmp--;
            }
            if (value < 0) {
                throw new IOException("DerInputStream.getLength(): Invalid length bytes");
            }
        }
        return value;
    }

    public void mark(int value) {
        this.buffer.mark(value);
    }

    public void reset() {
        this.buffer.reset();
    }

    public int available() {
        return this.buffer.available();
    }
}
