package sun.security.x509;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

public class Extension implements java.security.cert.Extension {
    private static final int hashMagic = 31;
    protected boolean critical = false;
    protected ObjectIdentifier extensionId = null;
    protected byte[] extensionValue = null;

    public Extension() {
    }

    public Extension(DerValue derVal) throws IOException {
        DerInputStream in = derVal.toDerInputStream();
        this.extensionId = in.getOID();
        DerValue val = in.getDerValue();
        if (val.tag == 1) {
            this.critical = val.getBoolean();
            this.extensionValue = in.getDerValue().getOctetString();
            return;
        }
        this.critical = false;
        this.extensionValue = val.getOctetString();
    }

    public Extension(ObjectIdentifier extensionId2, boolean critical2, byte[] extensionValue2) throws IOException {
        this.extensionId = extensionId2;
        this.critical = critical2;
        this.extensionValue = new DerValue(extensionValue2).getOctetString();
    }

    public Extension(Extension ext) {
        this.extensionId = ext.extensionId;
        this.critical = ext.critical;
        this.extensionValue = ext.extensionValue;
    }

    public static Extension newExtension(ObjectIdentifier extensionId2, boolean critical2, byte[] rawExtensionValue) throws IOException {
        Extension ext = new Extension();
        ext.extensionId = extensionId2;
        ext.critical = critical2;
        ext.extensionValue = rawExtensionValue;
        return ext;
    }

    public void encode(OutputStream out) throws IOException {
        if (out != null) {
            DerOutputStream dos1 = new DerOutputStream();
            DerOutputStream dos2 = new DerOutputStream();
            dos1.putOID(this.extensionId);
            if (this.critical) {
                dos1.putBoolean(this.critical);
            }
            dos1.putOctetString(this.extensionValue);
            dos2.write((byte) 48, dos1);
            out.write(dos2.toByteArray());
            return;
        }
        throw new NullPointerException();
    }

    public void encode(DerOutputStream out) throws IOException {
        if (this.extensionId == null) {
            throw new IOException("Null OID to encode for the extension!");
        } else if (this.extensionValue != null) {
            DerOutputStream dos = new DerOutputStream();
            dos.putOID(this.extensionId);
            if (this.critical) {
                dos.putBoolean(this.critical);
            }
            dos.putOctetString(this.extensionValue);
            out.write((byte) 48, dos);
        } else {
            throw new IOException("No value to encode for the extension!");
        }
    }

    public boolean isCritical() {
        return this.critical;
    }

    public ObjectIdentifier getExtensionId() {
        return this.extensionId;
    }

    public byte[] getValue() {
        return (byte[]) this.extensionValue.clone();
    }

    public byte[] getExtensionValue() {
        return this.extensionValue;
    }

    public String getId() {
        return this.extensionId.toString();
    }

    public String toString() {
        String s = "ObjectId: " + this.extensionId.toString();
        if (this.critical) {
            return s + " Criticality=true\n";
        }
        return s + " Criticality=false\n";
    }

    public int hashCode() {
        int h = 0;
        if (this.extensionValue != null) {
            byte[] val = this.extensionValue;
            int len = val.length;
            while (len > 0) {
                int len2 = len - 1;
                h += len * val[len2];
                len = len2;
            }
        }
        return (((h * 31) + this.extensionId.hashCode()) * 31) + (this.critical ? 1231 : 1237);
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Extension)) {
            return false;
        }
        Extension otherExt = (Extension) other;
        if (this.critical == otherExt.critical && this.extensionId.equals((Object) otherExt.extensionId)) {
            return Arrays.equals(this.extensionValue, otherExt.extensionValue);
        }
        return false;
    }
}
