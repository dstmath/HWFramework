package java.security.cert;

import java.io.IOException;
import java.security.PublicKey;
import javax.security.auth.x500.X500Principal;
import sun.security.x509.NameConstraintsExtension;

public class TrustAnchor {
    private final String caName;
    private final X500Principal caPrincipal;
    private NameConstraintsExtension nc;
    private byte[] ncBytes;
    private final PublicKey pubKey;
    private final X509Certificate trustedCert;

    public TrustAnchor(X509Certificate trustedCert2, byte[] nameConstraints) {
        if (trustedCert2 != null) {
            this.trustedCert = trustedCert2;
            this.pubKey = null;
            this.caName = null;
            this.caPrincipal = null;
            setNameConstraints(nameConstraints);
            return;
        }
        throw new NullPointerException("the trustedCert parameter must be non-null");
    }

    public TrustAnchor(X500Principal caPrincipal2, PublicKey pubKey2, byte[] nameConstraints) {
        if (caPrincipal2 == null || pubKey2 == null) {
            throw new NullPointerException();
        }
        this.trustedCert = null;
        this.caPrincipal = caPrincipal2;
        this.caName = caPrincipal2.getName();
        this.pubKey = pubKey2;
        setNameConstraints(nameConstraints);
    }

    public TrustAnchor(String caName2, PublicKey pubKey2, byte[] nameConstraints) {
        if (pubKey2 == null) {
            throw new NullPointerException("the pubKey parameter must be non-null");
        } else if (caName2 == null) {
            throw new NullPointerException("the caName parameter must be non-null");
        } else if (caName2.length() != 0) {
            this.caPrincipal = new X500Principal(caName2);
            this.pubKey = pubKey2;
            this.caName = caName2;
            this.trustedCert = null;
            setNameConstraints(nameConstraints);
        } else {
            throw new IllegalArgumentException("the caName parameter must be a non-empty String");
        }
    }

    public final X509Certificate getTrustedCert() {
        return this.trustedCert;
    }

    public final X500Principal getCA() {
        return this.caPrincipal;
    }

    public final String getCAName() {
        return this.caName;
    }

    public final PublicKey getCAPublicKey() {
        return this.pubKey;
    }

    private void setNameConstraints(byte[] bytes) {
        if (bytes == null) {
            this.ncBytes = null;
            this.nc = null;
            return;
        }
        this.ncBytes = (byte[]) bytes.clone();
        try {
            this.nc = new NameConstraintsExtension(Boolean.FALSE, (Object) bytes);
        } catch (IOException ioe) {
            IllegalArgumentException iae = new IllegalArgumentException(ioe.getMessage());
            iae.initCause(ioe);
            throw iae;
        }
    }

    public final byte[] getNameConstraints() {
        if (this.ncBytes == null) {
            return null;
        }
        return (byte[]) this.ncBytes.clone();
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[\n");
        if (this.pubKey != null) {
            sb.append("  Trusted CA Public Key: " + this.pubKey.toString() + "\n");
            sb.append("  Trusted CA Issuer Name: " + String.valueOf((Object) this.caName) + "\n");
        } else {
            sb.append("  Trusted CA cert: " + this.trustedCert.toString() + "\n");
        }
        if (this.nc != null) {
            sb.append("  Name Constraints: " + this.nc.toString() + "\n");
        }
        return sb.toString();
    }
}
