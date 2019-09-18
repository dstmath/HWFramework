package com.android.org.bouncycastle.asn1;

import com.android.org.bouncycastle.util.Arrays;
import com.android.org.bouncycastle.util.Strings;
import java.io.IOException;

public class DERT61String extends ASN1Primitive implements ASN1String {
    private byte[] string;

    public static DERT61String getInstance(Object obj) {
        if (obj == null || (obj instanceof DERT61String)) {
            return (DERT61String) obj;
        }
        if (obj instanceof byte[]) {
            try {
                return (DERT61String) fromByteArray((byte[]) obj);
            } catch (Exception e) {
                throw new IllegalArgumentException("encoding error in getInstance: " + e.toString());
            }
        } else {
            throw new IllegalArgumentException("illegal object in getInstance: " + obj.getClass().getName());
        }
    }

    public static DERT61String getInstance(ASN1TaggedObject obj, boolean explicit) {
        ASN1Primitive o = obj.getObject();
        if (explicit || (o instanceof DERT61String)) {
            return getInstance(o);
        }
        return new DERT61String(ASN1OctetString.getInstance(o).getOctets());
    }

    public DERT61String(byte[] string2) {
        this.string = Arrays.clone(string2);
    }

    public DERT61String(String string2) {
        this.string = Strings.toByteArray(string2);
    }

    public String getString() {
        return Strings.fromByteArray(this.string);
    }

    public String toString() {
        return getString();
    }

    /* access modifiers changed from: package-private */
    public boolean isConstructed() {
        return false;
    }

    /* access modifiers changed from: package-private */
    public int encodedLength() {
        return 1 + StreamUtil.calculateBodyLength(this.string.length) + this.string.length;
    }

    /* access modifiers changed from: package-private */
    public void encode(ASN1OutputStream out) throws IOException {
        out.writeEncoded(20, this.string);
    }

    public byte[] getOctets() {
        return Arrays.clone(this.string);
    }

    /* access modifiers changed from: package-private */
    public boolean asn1Equals(ASN1Primitive o) {
        if (!(o instanceof DERT61String)) {
            return false;
        }
        return Arrays.areEqual(this.string, ((DERT61String) o).string);
    }

    public int hashCode() {
        return Arrays.hashCode(this.string);
    }
}
