package java.security;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Vector;

@Deprecated
public abstract class Identity implements Principal, Serializable {
    private static final long serialVersionUID = 3609922007826600659L;
    Vector<Certificate> certificates;
    String info;
    private String name;
    private PublicKey publicKey;
    IdentityScope scope;

    protected Identity() {
        this("restoring...");
    }

    public Identity(String name, IdentityScope scope) throws KeyManagementException {
        this(name);
        if (scope != null) {
            scope.addIdentity(this);
        }
        this.scope = scope;
    }

    public Identity(String name) {
        this.info = "No further information available.";
        this.name = name;
    }

    public final String getName() {
        return this.name;
    }

    public final IdentityScope getScope() {
        return this.scope;
    }

    public PublicKey getPublicKey() {
        return this.publicKey;
    }

    public void setPublicKey(PublicKey key) throws KeyManagementException {
        check("setIdentityPublicKey");
        this.publicKey = key;
        this.certificates = new Vector();
    }

    public void setInfo(String info) {
        check("setIdentityInfo");
        this.info = info;
    }

    public String getInfo() {
        return this.info;
    }

    public void addCertificate(Certificate certificate) throws KeyManagementException {
        check("addIdentityCertificate");
        if (this.certificates == null) {
            this.certificates = new Vector();
        }
        if (this.publicKey == null) {
            this.publicKey = certificate.getPublicKey();
        } else if (!keyEquals(this.publicKey, certificate.getPublicKey())) {
            throw new KeyManagementException("public key different from cert public key");
        }
        this.certificates.addElement(certificate);
    }

    private boolean keyEquals(PublicKey aKey, PublicKey anotherKey) {
        int i;
        int i2 = 1;
        String aKeyFormat = aKey.getFormat();
        String anotherKeyFormat = anotherKey.getFormat();
        if (aKeyFormat == null) {
            i = 1;
        } else {
            i = 0;
        }
        if (anotherKeyFormat != null) {
            i2 = 0;
        }
        if ((i2 ^ i) != 0) {
            return false;
        }
        if (aKeyFormat == null || anotherKeyFormat == null || aKeyFormat.equalsIgnoreCase(anotherKeyFormat)) {
            return Arrays.equals(aKey.getEncoded(), anotherKey.getEncoded());
        }
        return false;
    }

    public void removeCertificate(Certificate certificate) throws KeyManagementException {
        check("removeIdentityCertificate");
        if (this.certificates == null) {
            return;
        }
        if (certificate == null || (this.certificates.contains(certificate) ^ 1) != 0) {
            throw new KeyManagementException();
        }
        this.certificates.removeElement(certificate);
    }

    public Certificate[] certificates() {
        if (this.certificates == null) {
            return new Certificate[0];
        }
        Certificate[] certs = new Certificate[this.certificates.size()];
        this.certificates.copyInto(certs);
        return certs;
    }

    public final boolean equals(Object identity) {
        if (identity == this) {
            return true;
        }
        if (!(identity instanceof Identity)) {
            return false;
        }
        Identity i = (Identity) identity;
        if (fullName().equals(i.fullName())) {
            return true;
        }
        return identityEquals(i);
    }

    protected boolean identityEquals(Identity identity) {
        if (!this.name.equalsIgnoreCase(identity.name)) {
            return false;
        }
        int i;
        if (this.publicKey == null) {
            i = 1;
        } else {
            i = 0;
        }
        if ((i ^ (identity.publicKey == null ? 1 : 0)) != 0) {
            return false;
        }
        return this.publicKey == null || identity.publicKey == null || this.publicKey.lambda$-java_util_function_Predicate_4628(identity.publicKey);
    }

    String fullName() {
        String parsable = this.name;
        if (this.scope != null) {
            return parsable + "." + this.scope.getName();
        }
        return parsable;
    }

    public String toString() {
        check("printIdentity");
        String printable = this.name;
        if (this.scope != null) {
            return printable + "[" + this.scope.getName() + "]";
        }
        return printable;
    }

    public String toString(boolean detailed) {
        String out = toString();
        if (!detailed) {
            return out;
        }
        out = ((out + "\n") + printKeys()) + "\n" + printCertificates();
        if (this.info != null) {
            return out + "\n\t" + this.info;
        }
        return out + "\n\tno additional information available.";
    }

    String printKeys() {
        String key = "";
        if (this.publicKey != null) {
            return "\tpublic key initialized";
        }
        return "\tno public key";
    }

    String printCertificates() {
        String out = "";
        if (this.certificates == null) {
            return "\tno certificates";
        }
        out = out + "\tcertificates: \n";
        int i = 1;
        for (Certificate cert : this.certificates) {
            int i2 = i + 1;
            out = (out + "\tcertificate " + i + "\tfor  : " + cert.getPrincipal() + "\n") + "\t\t\tfrom : " + cert.getGuarantor() + "\n";
            i = i2;
        }
        return out;
    }

    public int hashCode() {
        return this.name.hashCode();
    }

    private static void check(String directive) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkSecurityAccess(directive);
        }
    }
}
