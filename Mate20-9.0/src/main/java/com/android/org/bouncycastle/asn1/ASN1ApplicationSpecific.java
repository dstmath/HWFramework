package com.android.org.bouncycastle.asn1;

import com.android.org.bouncycastle.util.Arrays;
import java.io.IOException;

public abstract class ASN1ApplicationSpecific extends ASN1Primitive {
    protected final boolean isConstructed;
    protected final byte[] octets;
    protected final int tag;

    ASN1ApplicationSpecific(boolean isConstructed2, int tag2, byte[] octets2) {
        this.isConstructed = isConstructed2;
        this.tag = tag2;
        this.octets = Arrays.clone(octets2);
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
        } else {
            throw new IllegalArgumentException("unknown object in getInstance: " + obj.getClass().getName());
        }
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
        if (derTagNo < 31) {
            byte[] orig = getEncoded();
            byte[] tmp = replaceTagNumber(derTagNo, orig);
            if ((orig[0] & 32) != 0) {
                tmp[0] = (byte) (tmp[0] | 32);
            }
            return ASN1Primitive.fromByteArray(tmp);
        }
        throw new IOException("unsupported tag number");
    }

    /* access modifiers changed from: package-private */
    public int encodedLength() throws IOException {
        return StreamUtil.calculateTagLength(this.tag) + StreamUtil.calculateBodyLength(this.octets.length) + this.octets.length;
    }

    /* access modifiers changed from: package-private */
    public void encode(ASN1OutputStream out) throws IOException {
        int classBits = 64;
        if (this.isConstructed) {
            classBits = 64 | 32;
        }
        out.writeEncoded(classBits, this.tag, this.octets);
    }

    /* access modifiers changed from: package-private */
    public boolean asn1Equals(ASN1Primitive o) {
        boolean z = false;
        if (!(o instanceof ASN1ApplicationSpecific)) {
            return false;
        }
        ASN1ApplicationSpecific other = (ASN1ApplicationSpecific) o;
        if (this.isConstructed == other.isConstructed && this.tag == other.tag && Arrays.areEqual(this.octets, other.octets)) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        return (this.isConstructed ^ this.tag) ^ Arrays.hashCode(this.octets) ? 1 : 0;
    }

    private byte[] replaceTagNumber(int newTag, byte[] input) throws IOException {
        int index;
        if ((input[0] & 31) == 31) {
            int tagNo = 0;
            index = 1 + 1;
            int b = input[1] & 255;
            if ((b & 127) != 0) {
                while (b >= 0 && (b & 128) != 0) {
                    tagNo = (tagNo | (b & 127)) << 7;
                    b = input[index] & 255;
                    index++;
                }
            } else {
                throw new ASN1ParsingException("corrupted stream - invalid high tag number found");
            }
        } else {
            index = 1;
        }
        byte[] tmp = new byte[((input.length - index) + 1)];
        System.arraycopy(input, index, tmp, 1, tmp.length - 1);
        tmp[0] = (byte) newTag;
        return tmp;
    }
}
