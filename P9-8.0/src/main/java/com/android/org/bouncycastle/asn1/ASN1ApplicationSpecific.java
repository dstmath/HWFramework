package com.android.org.bouncycastle.asn1;

import com.android.org.bouncycastle.util.Arrays;
import java.io.IOException;

public abstract class ASN1ApplicationSpecific extends ASN1Primitive {
    protected final boolean isConstructed;
    protected final byte[] octets;
    protected final int tag;

    ASN1ApplicationSpecific(boolean isConstructed, int tag, byte[] octets) {
        this.isConstructed = isConstructed;
        this.tag = tag;
        this.octets = Arrays.clone(octets);
    }

    public static ASN1ApplicationSpecific getInstance(Object obj) {
        if (obj == null || (obj instanceof ASN1ApplicationSpecific)) {
            return (ASN1ApplicationSpecific) obj;
        }
        if (obj instanceof byte[]) {
            try {
                return getInstance(ASN1Primitive.fromByteArray((byte[]) obj));
            } catch (IOException e) {
                throw new IllegalArgumentException("Failed to construct object from byte[]: " + e.getMessage());
            }
        }
        throw new IllegalArgumentException("unknown object in getInstance: " + obj.getClass().getName());
    }

    protected static int getLengthOfHeader(byte[] data) {
        int length = data[1] & 255;
        if (length == 128 || length <= 127) {
            return 2;
        }
        int size = length & 127;
        if (size <= 4) {
            return size + 2;
        }
        throw new IllegalStateException("DER length more than 4 bytes: " + size);
    }

    public boolean isConstructed() {
        return this.isConstructed;
    }

    public byte[] getContents() {
        return Arrays.clone(this.octets);
    }

    public int getApplicationTag() {
        return this.tag;
    }

    public ASN1Primitive getObject() throws IOException {
        return ASN1Primitive.fromByteArray(getContents());
    }

    public ASN1Primitive getObject(int derTagNo) throws IOException {
        if (derTagNo >= 31) {
            throw new IOException("unsupported tag number");
        }
        byte[] orig = getEncoded();
        byte[] tmp = replaceTagNumber(derTagNo, orig);
        if ((orig[0] & 32) != 0) {
            tmp[0] = (byte) (tmp[0] | 32);
        }
        return ASN1Primitive.fromByteArray(tmp);
    }

    int encodedLength() throws IOException {
        return (StreamUtil.calculateTagLength(this.tag) + StreamUtil.calculateBodyLength(this.octets.length)) + this.octets.length;
    }

    void encode(ASN1OutputStream out) throws IOException {
        int classBits = 64;
        if (this.isConstructed) {
            classBits = 96;
        }
        out.writeEncoded(classBits, this.tag, this.octets);
    }

    boolean asn1Equals(ASN1Primitive o) {
        boolean z = false;
        if (!(o instanceof ASN1ApplicationSpecific)) {
            return false;
        }
        ASN1ApplicationSpecific other = (ASN1ApplicationSpecific) o;
        if (this.isConstructed == other.isConstructed && this.tag == other.tag) {
            z = Arrays.areEqual(this.octets, other.octets);
        }
        return z;
    }

    public int hashCode() {
        return ((this.isConstructed ? 1 : 0) ^ this.tag) ^ Arrays.hashCode(this.octets);
    }

    private byte[] replaceTagNumber(int newTag, byte[] input) throws IOException {
        int index = 1;
        if ((input[0] & 31) == 31) {
            int tagNo = 0;
            int b = input[1] & 255;
            if ((b & 127) == 0) {
                throw new ASN1ParsingException("corrupted stream - invalid high tag number found");
            }
            int index2 = 2;
            while (b >= 0 && (b & 128) != 0) {
                tagNo = (tagNo | (b & 127)) << 7;
                b = input[index2] & 255;
                index2++;
            }
            index = index2;
        }
        byte[] tmp = new byte[((input.length - index) + 1)];
        System.arraycopy(input, index, tmp, 1, tmp.length - 1);
        tmp[0] = (byte) newTag;
        return tmp;
    }
}
