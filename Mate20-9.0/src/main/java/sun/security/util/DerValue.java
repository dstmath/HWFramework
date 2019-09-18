package sun.security.util;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Date;
import sun.misc.IOUtils;

public class DerValue {
    public static final byte TAG_APPLICATION = 64;
    public static final byte TAG_CONTEXT = Byte.MIN_VALUE;
    public static final byte TAG_PRIVATE = -64;
    public static final byte TAG_UNIVERSAL = 0;
    public static final byte tag_BMPString = 30;
    public static final byte tag_BitString = 3;
    public static final byte tag_Boolean = 1;
    public static final byte tag_Enumerated = 10;
    public static final byte tag_GeneralString = 27;
    public static final byte tag_GeneralizedTime = 24;
    public static final byte tag_IA5String = 22;
    public static final byte tag_Integer = 2;
    public static final byte tag_Null = 5;
    public static final byte tag_ObjectId = 6;
    public static final byte tag_OctetString = 4;
    public static final byte tag_PrintableString = 19;
    public static final byte tag_Sequence = 48;
    public static final byte tag_SequenceOf = 48;
    public static final byte tag_Set = 49;
    public static final byte tag_SetOf = 49;
    public static final byte tag_T61String = 20;
    public static final byte tag_UTF8String = 12;
    public static final byte tag_UniversalString = 28;
    public static final byte tag_UtcTime = 23;
    protected DerInputBuffer buffer;
    public final DerInputStream data;
    private int length;
    private byte[] originalEncodedForm;
    public byte tag;

    public boolean isUniversal() {
        return (this.tag & TAG_PRIVATE) == 0;
    }

    public boolean isApplication() {
        return (this.tag & TAG_PRIVATE) == 64;
    }

    public boolean isContextSpecific() {
        return (this.tag & TAG_PRIVATE) == 128;
    }

    public boolean isContextSpecific(byte cntxtTag) {
        boolean z = false;
        if (!isContextSpecific()) {
            return false;
        }
        if ((this.tag & 31) == cntxtTag) {
            z = true;
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public boolean isPrivate() {
        return (this.tag & TAG_PRIVATE) == 192;
    }

    public boolean isConstructed() {
        return (this.tag & 32) == 32;
    }

    public boolean isConstructed(byte constructedTag) {
        boolean z = false;
        if (!isConstructed()) {
            return false;
        }
        if ((this.tag & 31) == constructedTag) {
            z = true;
        }
        return z;
    }

    public DerValue(String value) throws IOException {
        boolean isPrintableString = true;
        int i = 0;
        while (true) {
            if (i >= value.length()) {
                break;
            } else if (!isPrintableStringChar(value.charAt(i))) {
                isPrintableString = false;
                break;
            } else {
                i++;
            }
        }
        this.data = init(isPrintableString ? (byte) 19 : 12, value);
    }

    public DerValue(byte stringTag, String value) throws IOException {
        this.data = init(stringTag, value);
    }

    public DerValue(byte tag2, byte[] data2) {
        this.tag = tag2;
        this.buffer = new DerInputBuffer((byte[]) data2.clone());
        this.length = data2.length;
        this.data = new DerInputStream(this.buffer);
        this.data.mark(Integer.MAX_VALUE);
    }

    DerValue(DerInputBuffer in, boolean originalEncodedFormRetained) throws IOException {
        int startPosInInput = in.getPos();
        this.tag = (byte) in.read();
        byte lenByte = (byte) in.read();
        this.length = DerInputStream.getLength(lenByte, in);
        if (this.length == -1) {
            DerInputBuffer inbuf = in.dup();
            int readLen = inbuf.available();
            byte[] indefData = new byte[(readLen + 2)];
            indefData[0] = this.tag;
            indefData[1] = lenByte;
            DataInputStream dis = new DataInputStream(inbuf);
            dis.readFully(indefData, 2, readLen);
            dis.close();
            DerInputBuffer inbuf2 = new DerInputBuffer(new DerIndefLenConverter().convert(indefData));
            if (this.tag == inbuf2.read()) {
                this.length = DerInputStream.getLength(inbuf2);
                this.buffer = inbuf2.dup();
                this.buffer.truncate(this.length);
                this.data = new DerInputStream(this.buffer);
                in.skip((long) (this.length + 2));
            } else {
                throw new IOException("Indefinite length encoding not supported");
            }
        } else {
            this.buffer = in.dup();
            this.buffer.truncate(this.length);
            this.data = new DerInputStream(this.buffer);
            in.skip((long) this.length);
        }
        if (originalEncodedFormRetained) {
            this.originalEncodedForm = in.getSlice(startPosInInput, in.getPos() - startPosInInput);
        }
    }

    public DerValue(byte[] buf) throws IOException {
        this.data = init(true, (InputStream) new ByteArrayInputStream(buf));
    }

    public DerValue(byte[] buf, int offset, int len) throws IOException {
        this.data = init(true, (InputStream) new ByteArrayInputStream(buf, offset, len));
    }

    public DerValue(InputStream in) throws IOException {
        this.data = init(false, in);
    }

    private DerInputStream init(byte stringTag, String value) throws IOException {
        String enc;
        this.tag = stringTag;
        if (stringTag != 12) {
            if (!(stringTag == 22 || stringTag == 27)) {
                if (stringTag != 30) {
                    switch (stringTag) {
                        case 19:
                            break;
                        case 20:
                            enc = "ISO-8859-1";
                            break;
                        default:
                            throw new IllegalArgumentException("Unsupported DER string type");
                    }
                } else {
                    enc = "UnicodeBigUnmarked";
                }
            }
            enc = "ASCII";
        } else {
            enc = "UTF8";
        }
        byte[] buf = value.getBytes(enc);
        this.length = buf.length;
        this.buffer = new DerInputBuffer(buf);
        DerInputStream result = new DerInputStream(this.buffer);
        result.mark(Integer.MAX_VALUE);
        return result;
    }

    private DerInputStream init(boolean fullyBuffered, InputStream in) throws IOException {
        this.tag = (byte) in.read();
        byte lenByte = (byte) in.read();
        this.length = DerInputStream.getLength(lenByte, in);
        if (this.length == -1) {
            int readLen = in.available();
            byte[] indefData = new byte[(readLen + 2)];
            indefData[0] = this.tag;
            indefData[1] = lenByte;
            DataInputStream dis = new DataInputStream(in);
            dis.readFully(indefData, 2, readLen);
            dis.close();
            in = new ByteArrayInputStream(new DerIndefLenConverter().convert(indefData));
            if (this.tag == in.read()) {
                this.length = DerInputStream.getLength(in);
            } else {
                throw new IOException("Indefinite length encoding not supported");
            }
        }
        if (!fullyBuffered || in.available() == this.length) {
            this.buffer = new DerInputBuffer(IOUtils.readFully(in, this.length, true));
            return new DerInputStream(this.buffer);
        }
        throw new IOException("extra data given to DerValue constructor");
    }

    public void encode(DerOutputStream out) throws IOException {
        out.write(this.tag);
        out.putLength(this.length);
        if (this.length > 0) {
            byte[] value = new byte[this.length];
            synchronized (this.data) {
                this.buffer.reset();
                if (this.buffer.read(value) == this.length) {
                    out.write(value);
                } else {
                    throw new IOException("short DER value read (encode)");
                }
            }
        }
    }

    public final DerInputStream getData() {
        return this.data;
    }

    public final byte getTag() {
        return this.tag;
    }

    public boolean getBoolean() throws IOException {
        if (this.tag != 1) {
            throw new IOException("DerValue.getBoolean, not a BOOLEAN " + this.tag);
        } else if (this.length != 1) {
            throw new IOException("DerValue.getBoolean, invalid length " + this.length);
        } else if (this.buffer.read() != 0) {
            return true;
        } else {
            return false;
        }
    }

    public ObjectIdentifier getOID() throws IOException {
        if (this.tag == 6) {
            return new ObjectIdentifier(this.buffer);
        }
        throw new IOException("DerValue.getOID, not an OID " + this.tag);
    }

    private byte[] append(byte[] a, byte[] b) {
        if (a == null) {
            return b;
        }
        byte[] ret = new byte[(a.length + b.length)];
        System.arraycopy(a, 0, ret, 0, a.length);
        System.arraycopy(b, 0, ret, a.length, b.length);
        return ret;
    }

    public byte[] getOctetString() throws IOException {
        if (this.tag == 4 || isConstructed((byte) 4)) {
            byte[] bytes = new byte[this.length];
            if (this.length == 0) {
                return bytes;
            }
            if (this.buffer.read(bytes) == this.length) {
                if (isConstructed()) {
                    DerInputStream in = new DerInputStream(bytes);
                    bytes = null;
                    while (in.available() != 0) {
                        bytes = append(bytes, in.getOctetString());
                    }
                }
                return bytes;
            }
            throw new IOException("short read on DerValue buffer");
        }
        throw new IOException("DerValue.getOctetString, not an Octet String: " + this.tag);
    }

    public int getInteger() throws IOException {
        if (this.tag == 2) {
            return this.buffer.getInteger(this.data.available());
        }
        throw new IOException("DerValue.getInteger, not an int " + this.tag);
    }

    public BigInteger getBigInteger() throws IOException {
        if (this.tag == 2) {
            return this.buffer.getBigInteger(this.data.available(), false);
        }
        throw new IOException("DerValue.getBigInteger, not an int " + this.tag);
    }

    public BigInteger getPositiveBigInteger() throws IOException {
        if (this.tag == 2) {
            return this.buffer.getBigInteger(this.data.available(), true);
        }
        throw new IOException("DerValue.getBigInteger, not an int " + this.tag);
    }

    public int getEnumerated() throws IOException {
        if (this.tag == 10) {
            return this.buffer.getInteger(this.data.available());
        }
        throw new IOException("DerValue.getEnumerated, incorrect tag: " + this.tag);
    }

    public byte[] getBitString() throws IOException {
        if (this.tag == 3) {
            return this.buffer.getBitString();
        }
        throw new IOException("DerValue.getBitString, not a bit string " + this.tag);
    }

    public BitArray getUnalignedBitString() throws IOException {
        if (this.tag == 3) {
            return this.buffer.getUnalignedBitString();
        }
        throw new IOException("DerValue.getBitString, not a bit string " + this.tag);
    }

    public String getAsString() throws IOException {
        if (this.tag == 12) {
            return getUTF8String();
        }
        if (this.tag == 19) {
            return getPrintableString();
        }
        if (this.tag == 20) {
            return getT61String();
        }
        if (this.tag == 22) {
            return getIA5String();
        }
        if (this.tag == 30) {
            return getBMPString();
        }
        if (this.tag == 27) {
            return getGeneralString();
        }
        return null;
    }

    public byte[] getBitString(boolean tagImplicit) throws IOException {
        if (tagImplicit || this.tag == 3) {
            return this.buffer.getBitString();
        }
        throw new IOException("DerValue.getBitString, not a bit string " + this.tag);
    }

    public BitArray getUnalignedBitString(boolean tagImplicit) throws IOException {
        if (tagImplicit || this.tag == 3) {
            return this.buffer.getUnalignedBitString();
        }
        throw new IOException("DerValue.getBitString, not a bit string " + this.tag);
    }

    public byte[] getDataBytes() throws IOException {
        byte[] retVal = new byte[this.length];
        synchronized (this.data) {
            this.data.reset();
            this.data.getBytes(retVal);
        }
        return retVal;
    }

    public String getPrintableString() throws IOException {
        if (this.tag == 19) {
            return new String(getDataBytes(), "ASCII");
        }
        throw new IOException("DerValue.getPrintableString, not a string " + this.tag);
    }

    public String getT61String() throws IOException {
        if (this.tag == 20) {
            return new String(getDataBytes(), "ISO-8859-1");
        }
        throw new IOException("DerValue.getT61String, not T61 " + this.tag);
    }

    public String getIA5String() throws IOException {
        if (this.tag == 22) {
            return new String(getDataBytes(), "ASCII");
        }
        throw new IOException("DerValue.getIA5String, not IA5 " + this.tag);
    }

    public String getBMPString() throws IOException {
        if (this.tag == 30) {
            return new String(getDataBytes(), "UnicodeBigUnmarked");
        }
        throw new IOException("DerValue.getBMPString, not BMP " + this.tag);
    }

    public String getUTF8String() throws IOException {
        if (this.tag == 12) {
            return new String(getDataBytes(), "UTF8");
        }
        throw new IOException("DerValue.getUTF8String, not UTF-8 " + this.tag);
    }

    public String getGeneralString() throws IOException {
        if (this.tag == 27) {
            return new String(getDataBytes(), "ASCII");
        }
        throw new IOException("DerValue.getGeneralString, not GeneralString " + this.tag);
    }

    public Date getUTCTime() throws IOException {
        if (this.tag == 23) {
            return this.buffer.getUTCTime(this.data.available());
        }
        throw new IOException("DerValue.getUTCTime, not a UtcTime: " + this.tag);
    }

    public Date getGeneralizedTime() throws IOException {
        if (this.tag == 24) {
            return this.buffer.getGeneralizedTime(this.data.available());
        }
        throw new IOException("DerValue.getGeneralizedTime, not a GeneralizedTime: " + this.tag);
    }

    public boolean equals(Object other) {
        if (other instanceof DerValue) {
            return equals((DerValue) other);
        }
        return false;
    }

    public boolean equals(DerValue other) {
        boolean z;
        if (this == other) {
            return true;
        }
        if (this.tag != other.tag) {
            return false;
        }
        if (this.data == other.data) {
            return true;
        }
        if (System.identityHashCode(this.data) > System.identityHashCode(other.data)) {
            z = doEquals(this, other);
        } else {
            z = doEquals(other, this);
        }
        return z;
    }

    private static boolean doEquals(DerValue d1, DerValue d2) {
        boolean equals;
        synchronized (d1.data) {
            synchronized (d2.data) {
                d1.data.reset();
                d2.data.reset();
                equals = d1.buffer.equals(d2.buffer);
            }
        }
        return equals;
    }

    public String toString() {
        try {
            String str = getAsString();
            if (str != null) {
                return "\"" + str + "\"";
            } else if (this.tag == 5) {
                return "[DerValue, null]";
            } else {
                if (this.tag == 6) {
                    return "OID." + getOID();
                }
                return "[DerValue, tag = " + this.tag + ", length = " + this.length + "]";
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("misformatted DER value");
        }
    }

    public byte[] getOriginalEncodedForm() {
        if (this.originalEncodedForm != null) {
            return (byte[]) this.originalEncodedForm.clone();
        }
        return null;
    }

    public byte[] toByteArray() throws IOException {
        DerOutputStream out = new DerOutputStream();
        encode(out);
        this.data.reset();
        return out.toByteArray();
    }

    public DerInputStream toDerInputStream() throws IOException {
        if (this.tag == 48 || this.tag == 49) {
            return new DerInputStream(this.buffer);
        }
        throw new IOException("toDerInputStream rejects tag type " + this.tag);
    }

    public int length() {
        return this.length;
    }

    public static boolean isPrintableStringChar(char ch) {
        if ((ch < 'a' || ch > 'z') && ((ch < 'A' || ch > 'Z') && !((ch >= '0' && ch <= '9') || ch == ' ' || ch == ':' || ch == '=' || ch == '?'))) {
            switch (ch) {
                case '\'':
                case '(':
                case ')':
                    break;
                default:
                    switch (ch) {
                        case '+':
                        case ',':
                        case '-':
                        case ZipConstants.CENHDR:
                        case '/':
                            break;
                        default:
                            return false;
                    }
            }
        }
        return true;
    }

    public static byte createTag(byte tagClass, boolean form, byte val) {
        byte tag2 = (byte) (tagClass | val);
        if (form) {
            return (byte) (tag2 | 32);
        }
        return tag2;
    }

    public void resetTag(byte tag2) {
        this.tag = tag2;
    }

    public int hashCode() {
        return toString().hashCode();
    }
}
