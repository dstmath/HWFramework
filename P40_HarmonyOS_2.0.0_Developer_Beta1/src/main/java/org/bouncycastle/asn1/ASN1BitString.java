package org.bouncycastle.asn1;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import org.bouncycastle.pqc.crypto.rainbow.util.GF2Field;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.io.Streams;

public abstract class ASN1BitString extends ASN1Primitive implements ASN1String {
    private static final char[] table = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    protected final byte[] data;
    protected final int padBits;

    protected ASN1BitString(byte b, int i) {
        if (i > 7 || i < 0) {
            throw new IllegalArgumentException("pad bits cannot be greater than 7 or less than 0");
        }
        this.data = new byte[]{b};
        this.padBits = i;
    }

    public ASN1BitString(byte[] bArr, int i) {
        if (bArr == null) {
            throw new NullPointerException("'data' cannot be null");
        } else if (bArr.length == 0 && i != 0) {
            throw new IllegalArgumentException("zero length data with non-zero pad bits");
        } else if (i > 7 || i < 0) {
            throw new IllegalArgumentException("pad bits cannot be greater than 7 or less than 0");
        } else {
            this.data = Arrays.clone(bArr);
            this.padBits = i;
        }
    }

    protected static byte[] derForm(byte[] bArr, int i) {
        if (bArr.length == 0) {
            return bArr;
        }
        byte[] clone = Arrays.clone(bArr);
        int length = bArr.length - 1;
        clone[length] = (byte) ((GF2Field.MASK << i) & clone[length]);
        return clone;
    }

    static ASN1BitString fromInputStream(int i, InputStream inputStream) throws IOException {
        if (i >= 1) {
            int read = inputStream.read();
            byte[] bArr = new byte[(i - 1)];
            if (bArr.length != 0) {
                if (Streams.readFully(inputStream, bArr) != bArr.length) {
                    throw new EOFException("EOF encountered in middle of BIT STRING");
                } else if (read > 0 && read < 8 && bArr[bArr.length - 1] != ((byte) (bArr[bArr.length - 1] & (GF2Field.MASK << read)))) {
                    return new DLBitString(bArr, read);
                }
            }
            return new DERBitString(bArr, read);
        }
        throw new IllegalArgumentException("truncated BIT STRING detected");
    }

    protected static byte[] getBytes(int i) {
        if (i == 0) {
            return new byte[0];
        }
        int i2 = 4;
        int i3 = 3;
        while (i3 >= 1 && ((GF2Field.MASK << (i3 * 8)) & i) == 0) {
            i2--;
            i3--;
        }
        byte[] bArr = new byte[i2];
        for (int i4 = 0; i4 < i2; i4++) {
            bArr[i4] = (byte) ((i >> (i4 * 8)) & GF2Field.MASK);
        }
        return bArr;
    }

    protected static int getPadBits(int i) {
        int i2;
        int i3 = 3;
        while (true) {
            if (i3 < 0) {
                i2 = 0;
                break;
            }
            if (i3 != 0) {
                int i4 = i >> (i3 * 8);
                if (i4 != 0) {
                    i2 = i4 & GF2Field.MASK;
                    break;
                }
            } else if (i != 0) {
                i2 = i & GF2Field.MASK;
                break;
            }
            i3--;
        }
        if (i2 == 0) {
            return 0;
        }
        int i5 = 1;
        while (true) {
            i2 <<= 1;
            if ((i2 & GF2Field.MASK) == 0) {
                return 8 - i5;
            }
            i5++;
        }
    }

    /* access modifiers changed from: package-private */
    @Override // org.bouncycastle.asn1.ASN1Primitive
    public boolean asn1Equals(ASN1Primitive aSN1Primitive) {
        if (!(aSN1Primitive instanceof ASN1BitString)) {
            return false;
        }
        ASN1BitString aSN1BitString = (ASN1BitString) aSN1Primitive;
        if (this.padBits != aSN1BitString.padBits) {
            return false;
        }
        byte[] bArr = this.data;
        byte[] bArr2 = aSN1BitString.data;
        int length = bArr.length;
        if (length != bArr2.length) {
            return false;
        }
        int i = length - 1;
        if (i < 0) {
            return true;
        }
        for (int i2 = 0; i2 < i; i2++) {
            if (bArr[i2] != bArr2[i2]) {
                return false;
            }
        }
        byte b = bArr[i];
        int i3 = this.padBits;
        return ((byte) (b & (GF2Field.MASK << i3))) == ((byte) (bArr2[i] & (GF2Field.MASK << i3)));
    }

    /* access modifiers changed from: package-private */
    @Override // org.bouncycastle.asn1.ASN1Primitive
    public abstract void encode(ASN1OutputStream aSN1OutputStream, boolean z) throws IOException;

    public byte[] getBytes() {
        return derForm(this.data, this.padBits);
    }

    public ASN1Primitive getLoadedObject() {
        return toASN1Primitive();
    }

    public byte[] getOctets() {
        if (this.padBits == 0) {
            return Arrays.clone(this.data);
        }
        throw new IllegalStateException("attempt to get non-octet aligned data from BIT STRING");
    }

    public int getPadBits() {
        return this.padBits;
    }

    @Override // org.bouncycastle.asn1.ASN1String
    public String getString() {
        StringBuffer stringBuffer = new StringBuffer("#");
        try {
            byte[] encoded = getEncoded();
            for (int i = 0; i != encoded.length; i++) {
                stringBuffer.append(table[(encoded[i] >>> 4) & 15]);
                stringBuffer.append(table[encoded[i] & 15]);
            }
            return stringBuffer.toString();
        } catch (IOException e) {
            throw new ASN1ParsingException("Internal error encoding BitString: " + e.getMessage(), e);
        }
    }

    @Override // org.bouncycastle.asn1.ASN1Primitive, org.bouncycastle.asn1.ASN1Object
    public int hashCode() {
        byte[] bArr = this.data;
        int length = bArr.length - 1;
        if (length < 0) {
            return 1;
        }
        return ((Arrays.hashCode(bArr, 0, length) * 257) ^ ((byte) (bArr[length] & (GF2Field.MASK << this.padBits)))) ^ this.padBits;
    }

    public int intValue() {
        int min = Math.min(4, this.data.length - 1);
        int i = 0;
        for (int i2 = 0; i2 < min; i2++) {
            i |= (255 & this.data[i2]) << (i2 * 8);
        }
        return (min < 0 || min >= 4) ? i : i | ((((byte) (this.data[min] & (GF2Field.MASK << this.padBits))) & 255) << (min * 8));
    }

    /* access modifiers changed from: package-private */
    @Override // org.bouncycastle.asn1.ASN1Primitive
    public ASN1Primitive toDERObject() {
        return new DERBitString(this.data, this.padBits);
    }

    /* access modifiers changed from: package-private */
    @Override // org.bouncycastle.asn1.ASN1Primitive
    public ASN1Primitive toDLObject() {
        return new DLBitString(this.data, this.padBits);
    }

    public String toString() {
        return getString();
    }
}
