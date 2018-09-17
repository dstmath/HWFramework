package sun.security.x509;

import java.io.IOException;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.util.calendar.BaseCalendar;

public class GeneralName {
    private GeneralNameInterface name;

    public GeneralName(GeneralNameInterface name) {
        this.name = null;
        if (name == null) {
            throw new NullPointerException("GeneralName must not be null");
        }
        this.name = name;
    }

    public GeneralName(DerValue encName) throws IOException {
        this(encName, false);
    }

    public GeneralName(DerValue encName, boolean nameConstraint) throws IOException {
        this.name = null;
        int tag = (short) ((byte) (encName.tag & 31));
        switch (tag) {
            case GeneralNameInterface.NAME_MATCH /*0*/:
                if (encName.isContextSpecific() && encName.isConstructed()) {
                    encName.resetTag(DerValue.tag_SequenceOf);
                    this.name = new OtherName(encName);
                    return;
                }
                throw new IOException("Invalid encoding of Other-Name");
            case BaseCalendar.SUNDAY /*1*/:
                if (!encName.isContextSpecific() || encName.isConstructed()) {
                    throw new IOException("Invalid encoding of RFC822 name");
                }
                encName.resetTag(DerValue.tag_IA5String);
                this.name = new RFC822Name(encName);
            case BaseCalendar.MONDAY /*2*/:
                if (!encName.isContextSpecific() || encName.isConstructed()) {
                    throw new IOException("Invalid encoding of DNS name");
                }
                encName.resetTag(DerValue.tag_IA5String);
                this.name = new DNSName(encName);
            case BaseCalendar.WEDNESDAY /*4*/:
                if (encName.isContextSpecific() && encName.isConstructed()) {
                    this.name = new X500Name(encName.getData());
                    return;
                }
                throw new IOException("Invalid encoding of Directory name");
            case BaseCalendar.THURSDAY /*5*/:
                if (encName.isContextSpecific() && encName.isConstructed()) {
                    encName.resetTag(DerValue.tag_SequenceOf);
                    this.name = new EDIPartyName(encName);
                    return;
                }
                throw new IOException("Invalid encoding of EDI name");
            case BaseCalendar.JUNE /*6*/:
                if (!encName.isContextSpecific() || encName.isConstructed()) {
                    throw new IOException("Invalid encoding of URI");
                }
                GeneralNameInterface nameConstraint2;
                encName.resetTag(DerValue.tag_IA5String);
                if (nameConstraint) {
                    nameConstraint2 = URIName.nameConstraint(encName);
                } else {
                    nameConstraint2 = new URIName(encName);
                }
                this.name = nameConstraint2;
            case BaseCalendar.SATURDAY /*7*/:
                if (!encName.isContextSpecific() || encName.isConstructed()) {
                    throw new IOException("Invalid encoding of IP address");
                }
                encName.resetTag((byte) 4);
                this.name = new IPAddressName(encName);
            case BaseCalendar.AUGUST /*8*/:
                if (!encName.isContextSpecific() || encName.isConstructed()) {
                    throw new IOException("Invalid encoding of OID name");
                }
                encName.resetTag((byte) 6);
                this.name = new OIDName(encName);
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
            out.writeImplicit(DerValue.createTag(DerValue.TAG_CONTEXT, true, (byte) nameType), tmp);
        } else if (nameType == 4) {
            out.write(DerValue.createTag(DerValue.TAG_CONTEXT, true, (byte) nameType), tmp);
        } else {
            out.writeImplicit(DerValue.createTag(DerValue.TAG_CONTEXT, false, (byte) nameType), tmp);
        }
    }
}
