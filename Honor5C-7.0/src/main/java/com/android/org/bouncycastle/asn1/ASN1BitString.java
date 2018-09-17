package com.android.org.bouncycastle.asn1;

import com.android.org.bouncycastle.util.Arrays;
import com.android.org.bouncycastle.util.io.Streams;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public abstract class ASN1BitString extends ASN1Primitive implements ASN1String {
    private static final char[] table = null;
    protected final byte[] data;
    protected final int padBits;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.bouncycastle.asn1.ASN1BitString.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.bouncycastle.asn1.ASN1BitString.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.asn1.ASN1BitString.<clinit>():void");
    }

    abstract void encode(ASN1OutputStream aSN1OutputStream) throws IOException;

    protected static int getPadBits(int bitString) {
        int val = 0;
        for (int i = 3; i >= 0; i--) {
            if (i != 0) {
                if ((bitString >> (i * 8)) != 0) {
                    val = (bitString >> (i * 8)) & 255;
                    break;
                }
            } else if (bitString != 0) {
                val = bitString & 255;
                break;
            }
        }
        if (val == 0) {
            return 0;
        }
        int bits = 1;
        while (true) {
            val <<= 1;
            if ((val & 255) == 0) {
                return 8 - bits;
            }
            bits++;
        }
    }

    protected static byte[] getBytes(int bitString) {
        if (bitString == 0) {
            return new byte[0];
        }
        int bytes = 4;
        int i = 3;
        while (i >= 1 && ((255 << (i * 8)) & bitString) == 0) {
            bytes--;
            i--;
        }
        byte[] result = new byte[bytes];
        for (i = 0; i < bytes; i++) {
            result[i] = (byte) ((bitString >> (i * 8)) & 255);
        }
        return result;
    }

    public ASN1BitString(byte[] data, int padBits) {
        if (data == null) {
            throw new NullPointerException("data cannot be null");
        } else if (data.length == 0 && padBits != 0) {
            throw new IllegalArgumentException("zero length data with non-zero pad bits");
        } else if (padBits > 7 || padBits < 0) {
            throw new IllegalArgumentException("pad bits cannot be greater than 7 or less than 0");
        } else {
            this.data = Arrays.clone(data);
            this.padBits = padBits;
        }
    }

    public String getString() {
        StringBuffer buf = new StringBuffer("#");
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        try {
            new ASN1OutputStream(bOut).writeObject(this);
            byte[] string = bOut.toByteArray();
            for (int i = 0; i != string.length; i++) {
                buf.append(table[(string[i] >>> 4) & 15]);
                buf.append(table[string[i] & 15]);
            }
            return buf.toString();
        } catch (IOException e) {
            throw new ASN1ParsingException("Internal error encoding BitString: " + e.getMessage(), e);
        }
    }

    public int intValue() {
        int value = 0;
        byte[] string = this.data;
        if (this.padBits > 0 && this.data.length <= 4) {
            string = derForm(this.data, this.padBits);
        }
        int i = 0;
        while (i != string.length && i != 4) {
            value |= (string[i] & 255) << (i * 8);
            i++;
        }
        return value;
    }

    public byte[] getOctets() {
        if (this.padBits == 0) {
            return Arrays.clone(this.data);
        }
        throw new IllegalStateException("attempt to get non-octet aligned data from BIT STRING");
    }

    public byte[] getBytes() {
        return derForm(this.data, this.padBits);
    }

    public int getPadBits() {
        return this.padBits;
    }

    public String toString() {
        return getString();
    }

    public int hashCode() {
        return this.padBits ^ Arrays.hashCode(getBytes());
    }

    protected boolean asn1Equals(ASN1Primitive o) {
        boolean z = false;
        if (!(o instanceof ASN1BitString)) {
            return false;
        }
        ASN1BitString other = (ASN1BitString) o;
        if (this.padBits == other.padBits) {
            z = Arrays.areEqual(getBytes(), other.getBytes());
        }
        return z;
    }

    protected static byte[] derForm(byte[] data, int padBits) {
        byte[] rv = Arrays.clone(data);
        if (padBits > 0) {
            int length = data.length - 1;
            rv[length] = (byte) (rv[length] & (255 << padBits));
        }
        return rv;
    }

    static ASN1BitString fromInputStream(int length, InputStream stream) throws IOException {
        if (length < 1) {
            throw new IllegalArgumentException("truncated BIT STRING detected");
        }
        int padBits = stream.read();
        byte[] data = new byte[(length - 1)];
        if (data.length != 0) {
            if (Streams.readFully(stream, data) != data.length) {
                throw new EOFException("EOF encountered in middle of BIT STRING");
            } else if (padBits > 0 && padBits < 8 && data[data.length - 1] != ((byte) (data[data.length - 1] & (255 << padBits)))) {
                return new DLBitString(data, padBits);
            }
        }
        return new DERBitString(data, padBits);
    }

    public ASN1Primitive getLoadedObject() {
        return toASN1Primitive();
    }

    ASN1Primitive toDERObject() {
        return new DERBitString(this.data, this.padBits);
    }

    ASN1Primitive toDLObject() {
        return new DLBitString(this.data, this.padBits);
    }
}
