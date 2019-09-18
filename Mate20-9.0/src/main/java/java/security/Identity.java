package java.security;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
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

    public Identity(String name2, IdentityScope scope2) throws KeyManagementException {
        this(name2);
        if (scope2 != null) {
            scope2.addIdentity(this);
        }
        this.scope = scope2;
    }

    public Identity(String name2) {
        this.info = "No further information available.";
        this.name = name2;
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
        this.certificates = new Vector<>();
    }

    public void setInfo(String info2) {
        check("setIdentityInfo");
        this.info = info2;
    }

    public String getInfo() {
        return this.info;
    }

    public void addCertificate(Certificate certificate) throws KeyManagementException {
        check("addIdentityCertificate");
        if (this.certificates == null) {
            this.certificates = new Vector<>();
        }
        if (this.publicKey == null) {
            this.publicKey = certificate.getPublicKey();
        } else if (!keyEquals(this.publicKey, certificate.getPublicKey())) {
            throw new KeyManagementException("public key different from cert public key");
        }
        this.certificates.addElement(certificate);
    }

    private boolean keyEquals(PublicKey aKey, PublicKey anotherKey) {
        String aKeyFormat = aKey.getFormat();
        String anotherKeyFormat = anotherKey.getFormat();
        boolean z = true;
        boolean z2 = aKeyFormat == null;
        if (anotherKeyFormat != null) {
            z = false;
        }
        if (z ^ z2) {
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
        if (certificate == null || !this.certificates.contains(certificate)) {
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

    /* access modifiers changed from: protected */
    public boolean identityEquals(Identity identity) {
        if (!this.name.equalsIgnoreCase(identity.name)) {
            return false;
        }
        if ((this.publicKey == null) ^ (identity.publicKey == null)) {
            return false;
        }
        if (this.publicKey == null || identity.publicKey == null || this.publicKey.equals(identity.publicKey)) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public String fullName() {
        String parsable = this.name;
        if (this.scope == null) {
            return parsable;
        }
        return parsable + "." + this.scope.getName();
    }

    public String toString() {
        check("printIdentity");
        String printable = this.name;
        if (this.scope == null) {
            return printable;
        }
        return printable + "[" + this.scope.getName() + "]";
    }

    public String toString(boolean detailed) {
        String out = toString();
        if (!detailed) {
            return out;
        }
        String out2 = out + "\n";
        String out3 = out2 + printKeys();
        String out4 = out3 + "\n" + printCertificates();
        if (this.info != null) {
            return out4 + "\n\t" + this.info;
        }
        return out4 + "\n\tno additional information available.";
    }

    /* access modifiers changed from: package-private */
    public String printKeys() {
        if (this.publicKey != null) {
            return "\tpublic key initialized";
        }
        return "\tno public key";
    }

    /* access modifiers changed from: package-private */
    public String printCertificates() {
        if (this.certificates == null) {
            return "\tno certificates";
        }
        String out = "" + "\tcertificates: \n";
        int i = 1;
        Iterator<Certificate> it = this.certificates.iterator();
        while (it.hasNext()) {
            Certificate cert = it.next();
            StringBuilder sb = new StringBuilder();
            sb.append(out);
            sb.append("\tcertificate ");
            sb.append(i);
            sb.append("\tfor  : ");
            sb.append((Object) cert.getPrincipal());
            sb.append("\n");
            String out2 = sb.toString();
            out = out2 + "\t\t\tfrom : " + cert.getGuarantor() + "\n";
            i++;
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
