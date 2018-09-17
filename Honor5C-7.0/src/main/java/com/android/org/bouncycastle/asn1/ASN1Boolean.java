package com.android.org.bouncycastle.asn1;

import com.android.org.bouncycastle.util.Arrays;
import java.io.IOException;

public class ASN1Boolean extends ASN1Primitive {
    public static final ASN1Boolean FALSE = null;
    private static final byte[] FALSE_VALUE = null;
    public static final ASN1Boolean TRUE = null;
    private static final byte[] TRUE_VALUE = null;
    private final byte[] value;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.bouncycastle.asn1.ASN1Boolean.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.bouncycastle.asn1.ASN1Boolean.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.asn1.ASN1Boolean.<clinit>():void");
    }

    public static ASN1Boolean getInstance(Object obj) {
        if (obj == null || (obj instanceof ASN1Boolean)) {
            return (ASN1Boolean) obj;
        }
        if (obj instanceof byte[]) {
            try {
                return (ASN1Boolean) ASN1Primitive.fromByteArray((byte[]) obj);
            } catch (IOException e) {
                throw new IllegalArgumentException("failed to construct boolean from byte[]: " + e.getMessage());
            }
        }
        throw new IllegalArgumentException("illegal object in getInstance: " + obj.getClass().getName());
    }

    public static ASN1Boolean getInstance(boolean value) {
        return value ? TRUE : FALSE;
    }

    public static ASN1Boolean getInstance(int value) {
        return value != 0 ? TRUE : FALSE;
    }

    public static ASN1Boolean getInstance(byte[] octets) {
        return octets[0] != null ? TRUE : FALSE;
    }

    public static ASN1Boolean getInstance(ASN1TaggedObject obj, boolean explicit) {
        Object o = obj.getObject();
        if (explicit || (o instanceof ASN1Boolean)) {
            return getInstance(o);
        }
        return fromOctetString(((ASN1OctetString) o).getOctets());
    }

    protected ASN1Boolean(byte[] value) {
        if (value.length != 1) {
            throw new IllegalArgumentException("byte value should have 1 byte in it");
        } else if (value[0] == null) {
            this.value = FALSE_VALUE;
        } else if ((value[0] & 255) == 255) {
            this.value = TRUE_VALUE;
        } else {
            this.value = Arrays.clone(value);
        }
    }

    protected ASN1Boolean(boolean value) {
        this.value = value ? TRUE_VALUE : FALSE_VALUE;
    }

    public boolean isTrue() {
        return this.value[0] != null;
    }

    boolean isConstructed() {
        return false;
    }

    int encodedLength() {
        return 3;
    }

    void encode(ASN1OutputStream out) throws IOException {
        out.writeEncoded(1, this.value);
    }

    protected boolean asn1Equals(ASN1Primitive o) {
        boolean z = false;
        if (!(o instanceof ASN1Boolean)) {
            return false;
        }
        if (this.value[0] == ((ASN1Boolean) o).value[0]) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        return this.value[0];
    }

    public String toString() {
        return this.value[0] != null ? "TRUE" : "FALSE";
    }

    static ASN1Boolean fromOctetString(byte[] value) {
        if (value.length != 1) {
            throw new IllegalArgumentException("BOOLEAN value should have 1 byte in it");
        } else if (value[0] == null) {
            return FALSE;
        } else {
            if ((value[0] & 255) == 255) {
                return TRUE;
            }
            return new ASN1Boolean(value);
        }
    }
}
