package sun.security.provider.certpath;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import sun.security.util.Debug;
import sun.security.x509.AuthorityKeyIdentifierExtension;
import sun.security.x509.KeyIdentifier;
import sun.security.x509.SubjectKeyIdentifierExtension;
import sun.security.x509.X509CertImpl;

public class Vertex {
    private static final Debug debug = Debug.getInstance("certpath");
    private X509Certificate cert;
    private int index = -1;
    private Throwable throwable;

    Vertex(X509Certificate cert) {
        this.cert = cert;
    }

    public X509Certificate getCertificate() {
        return this.cert;
    }

    public int getIndex() {
        return this.index;
    }

    void setIndex(int ndx) {
        this.index = ndx;
    }

    public Throwable getThrowable() {
        return this.throwable;
    }

    void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    public String toString() {
        return certToString() + throwableToString() + indexToString();
    }

    public String certToString() {
        StringBuilder sb = new StringBuilder();
        try {
            int i;
            X509CertImpl x509Cert = X509CertImpl.toImpl(this.cert);
            sb.append("Issuer:     ").append(x509Cert.getIssuerX500Principal()).append("\n");
            sb.append("Subject:    ").append(x509Cert.getSubjectX500Principal()).append("\n");
            sb.append("SerialNum:  ").append(x509Cert.getSerialNumber().toString(16)).append("\n");
            sb.append("Expires:    ").append(x509Cert.getNotAfter().toString()).append("\n");
            boolean[] iUID = x509Cert.getIssuerUniqueID();
            if (iUID != null) {
                sb.append("IssuerUID:  ");
                for (boolean b : iUID) {
                    if (b) {
                        i = 1;
                    } else {
                        i = 0;
                    }
                    sb.append(i);
                }
                sb.append("\n");
            }
            boolean[] sUID = x509Cert.getSubjectUniqueID();
            if (sUID != null) {
                sb.append("SubjectUID: ");
                for (boolean b2 : sUID) {
                    if (b2) {
                        i = 1;
                    } else {
                        i = 0;
                    }
                    sb.append(i);
                }
                sb.append("\n");
            }
            try {
                SubjectKeyIdentifierExtension sKeyID = x509Cert.getSubjectKeyIdentifierExtension();
                if (sKeyID != null) {
                    sb.append("SubjKeyID:  ").append(sKeyID.get("key_id").toString());
                }
                AuthorityKeyIdentifierExtension aKeyID = x509Cert.getAuthorityKeyIdentifierExtension();
                if (aKeyID != null) {
                    sb.append("AuthKeyID:  ").append(((KeyIdentifier) aKeyID.get("key_id")).toString());
                }
            } catch (IOException e) {
                if (debug != null) {
                    debug.println("Vertex.certToString() unexpected exception");
                    e.printStackTrace();
                }
            }
            return sb.-java_util_stream_Collectors-mthref-7();
        } catch (CertificateException ce) {
            if (debug != null) {
                debug.println("Vertex.certToString() unexpected exception");
                ce.printStackTrace();
            }
            return sb.-java_util_stream_Collectors-mthref-7();
        }
    }

    public String throwableToString() {
        StringBuilder sb = new StringBuilder("Exception:  ");
        if (this.throwable != null) {
            sb.append(this.throwable.toString());
        } else {
            sb.append("null");
        }
        sb.append("\n");
        return sb.-java_util_stream_Collectors-mthref-7();
    }

    public String moreToString() {
        StringBuilder sb = new StringBuilder("Last cert?  ");
        sb.append(this.index == -1 ? "Yes" : "No");
        sb.append("\n");
        return sb.-java_util_stream_Collectors-mthref-7();
    }

    public String indexToString() {
        return "Index:      " + this.index + "\n";
    }
}
