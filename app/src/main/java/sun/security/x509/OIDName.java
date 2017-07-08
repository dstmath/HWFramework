package sun.security.x509;

import java.io.IOException;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

public class OIDName implements GeneralNameInterface {
    private ObjectIdentifier oid;

    public OIDName(DerValue derValue) throws IOException {
        this.oid = derValue.getOID();
    }

    public OIDName(ObjectIdentifier oid) {
        this.oid = oid;
    }

    public OIDName(String name) throws IOException {
        try {
            this.oid = new ObjectIdentifier(name);
        } catch (Object e) {
            throw new IOException("Unable to create OIDName: " + e);
        }
    }

    public int getType() {
        return 8;
    }

    public void encode(DerOutputStream out) throws IOException {
        out.putOID(this.oid);
    }

    public String toString() {
        return "OIDName: " + this.oid.toString();
    }

    public ObjectIdentifier getOID() {
        return this.oid;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof OIDName)) {
            return false;
        }
        return this.oid.equals(((OIDName) obj).oid);
    }

    public int hashCode() {
        return this.oid.hashCode();
    }

    public int constrains(GeneralNameInterface inputName) throws UnsupportedOperationException {
        if (inputName == null) {
            return -1;
        }
        if (inputName.getType() != 8) {
            return -1;
        }
        if (equals((OIDName) inputName)) {
            return 0;
        }
        throw new UnsupportedOperationException("Narrowing and widening are not supported for OIDNames");
    }

    public int subtreeDepth() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("subtreeDepth() not supported for OIDName.");
    }
}
