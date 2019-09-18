package sun.security.x509;

import java.io.IOException;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class GeneralName {
    private GeneralNameInterface name;

    public GeneralName(GeneralNameInterface name2) {
        this.name = null;
        if (name2 != null) {
            this.name = name2;
            return;
        }
        throw new NullPointerException("GeneralName must not be null");
    }

    public GeneralName(DerValue encName) throws IOException {
        this(encName, false);
    }

    public GeneralName(DerValue encName, boolean nameConstraint) throws IOException {
        URIName uRIName;
        this.name = null;
        short tag = (short) ((byte) (encName.tag & 31));
        switch (tag) {
            case 0:
                if (!encName.isContextSpecific() || !encName.isConstructed()) {
                    throw new IOException("Invalid encoding of Other-Name");
                }
                encName.resetTag((byte) 48);
                this.name = new OtherName(encName);
                return;
            case 1:
                if (!encName.isContextSpecific() || encName.isConstructed()) {
                    throw new IOException("Invalid encoding of RFC822 name");
                }
                encName.resetTag((byte) 22);
                this.name = new RFC822Name(encName);
                return;
            case 2:
                if (!encName.isContextSpecific() || encName.isConstructed()) {
                    throw new IOException("Invalid encoding of DNS name");
                }
                encName.resetTag((byte) 22);
                this.name = new DNSName(encName);
                return;
            case 4:
                if (!encName.isContextSpecific() || !encName.isConstructed()) {
                    throw new IOException("Invalid encoding of Directory name");
                }
                this.name = new X500Name(encName.getData());
                return;
            case 5:
                if (!encName.isContextSpecific() || !encName.isConstructed()) {
                    throw new IOException("Invalid encoding of EDI name");
                }
                encName.resetTag((byte) 48);
                this.name = new EDIPartyName(encName);
                return;
            case 6:
                if (!encName.isContextSpecific() || encName.isConstructed()) {
                    throw new IOException("Invalid encoding of URI");
                }
                encName.resetTag((byte) 22);
                if (nameConstraint) {
                    uRIName = URIName.nameConstraint(encName);
                } else {
                    uRIName = new URIName(encName);
                }
                this.name = uRIName;
                return;
            case 7:
                if (!encName.isContextSpecific() || encName.isConstructed()) {
                    throw new IOException("Invalid encoding of IP address");
                }
                encName.resetTag((byte) 4);
                this.name = new IPAddressName(encName);
                return;
            case 8:
                if (!encName.isContextSpecific() || encName.isConstructed()) {
                    throw new IOException("Invalid encoding of OID name");
                }
                encName.resetTag((byte) 6);
                this.name = new OIDName(encName);
                return;
            default:
                throw new IOException("Unrecognized GeneralName tag, (" + tag + ")");
        }
    }

    public int getType() {
        return this.name.getType();
    }

    public GeneralNameInterface getName() {
        return this.name;
    }

    public String toString() {
        return this.name.toString();
    }

    public boolean equals(Object other) {
        boolean z = true;
        if (this == other) {
            return true;
        }
        if (!(other instanceof GeneralName)) {
            return false;
        }
        try {
            if (this.name.constrains(((GeneralName) other).name) != 0) {
                z = false;
            }
            return z;
        } catch (UnsupportedOperationException e) {
            return false;
        }
    }

    public int hashCode() {
        return this.name.hashCode();
    }

    public void encode(DerOutputStream out) throws IOException {
        DerOutputStream tmp = new DerOutputStream();
        this.name.encode(tmp);
        int nameType = this.name.getType();
        if (nameType == 0 || nameType == 3 || nameType == 5) {
            out.writeImplicit(DerValue.createTag(Byte.MIN_VALUE, true, (byte) nameType), tmp);
        } else if (nameType == 4) {
            out.write(DerValue.createTag(Byte.MIN_VALUE, true, (byte) nameType), tmp);
        } else {
            out.writeImplicit(DerValue.createTag(Byte.MIN_VALUE, false, (byte) nameType), tmp);
        }
    }
}
