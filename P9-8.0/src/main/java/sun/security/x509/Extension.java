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

    public Extension(DerValue derVal) throws IOException {
        DerInputStream in = derVal.toDerInputStream();
        this.extensionId = in.getOID();
        DerValue val = in.getDerValue();
        if (val.tag == (byte) 1) {
            this.critical = val.getBoolean();
            this.extensionValue = in.getDerValue().getOctetString();
            return;
        }
        this.critical = false;
        this.extensionValue = val.getOctetString();
    }

    public Extension(ObjectIdentifier extensionId, boolean critical, byte[] extensionValue) throws IOException {
        this.extensionId = extensionId;
        this.critical = critical;
        this.extensionValue = new DerValue(extensionValue).getOctetString();
    }

    public Extension(Extension ext) {
        this.extensionId = ext.extensionId;
        this.critical = ext.critical;
        this.extensionValue = ext.extensionValue;
    }

    public static Extension newExtension(ObjectIdentifier extensionId, boolean critical, byte[] rawExtensionValue) throws IOException {
        Extension ext = new Extension();
        ext.extensionId = extensionId;
        ext.critical = critical;
        ext.extensionValue = rawExtensionValue;
        return ext;
    }

    public void encode(OutputStream out) throws IOException {
        if (out == null) {
            throw new NullPointerException();
        }
        DerOutputStream dos1 = new DerOutputStream();
        DerOutputStream dos2 = new DerOutputStream();
        dos1.putOID(this.extensionId);
        if (this.critical) {
            dos1.putBoolean(this.critical);
        }
        dos1.putOctetString(this.extensionValue);
        dos2.write((byte) 48, dos1);
        out.write(dos2.toByteArray());
    }

    public void encode(DerOutputStream out) throws IOException {
        if (this.extensionId == null) {
            throw new IOException("Null OID to encode for the extension!");
        } else if (this.extensionValue == null) {
            throw new IOException("No value to encode for the extension!");
        } else {
            DerOutputStream dos = new DerOutputStream();
            dos.putOID(this.extensionId);
            if (this.critical) {
                dos.putBoolean(this.critical);
            }
            dos.putOctetString(this.extensionValue);
            out.write((byte) 48, dos);
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
            int length = val.length;
            while (length > 0) {
                int len = length - 1;
                h += val[len] * length;
                length = len;
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
        if (this.critical == otherExt.critical && this.extensionId.equals(otherExt.extensionId)) {
            return Arrays.equals(this.extensionValue, otherExt.extensionValue);
        }
        return false;
    }
}
