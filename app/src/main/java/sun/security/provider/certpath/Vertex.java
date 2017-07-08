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
    private static final Debug debug = null;
    private X509Certificate cert;
    private int index;
    private Throwable throwable;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.provider.certpath.Vertex.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.provider.certpath.Vertex.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.security.provider.certpath.Vertex.<clinit>():void");
    }

    Vertex(X509Certificate cert) {
        this.cert = cert;
        this.index = -1;
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
                    sb.append("SubjKeyID:  ").append(sKeyID.get(SubjectKeyIdentifierExtension.KEY_ID).toString());
                }
                AuthorityKeyIdentifierExtension aKeyID = x509Cert.getAuthorityKeyIdentifierExtension();
                if (aKeyID != null) {
                    sb.append("AuthKeyID:  ").append(((KeyIdentifier) aKeyID.get(SubjectKeyIdentifierExtension.KEY_ID)).toString());
                }
            } catch (IOException e) {
                if (debug != null) {
                    debug.println("Vertex.certToString() unexpected exception");
                    e.printStackTrace();
                }
            }
            return sb.toString();
        } catch (CertificateException ce) {
            if (debug != null) {
                debug.println("Vertex.certToString() unexpected exception");
                ce.printStackTrace();
            }
            return sb.toString();
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
        return sb.toString();
    }

    public String moreToString() {
        StringBuilder sb = new StringBuilder("Last cert?  ");
        sb.append(this.index == -1 ? "Yes" : "No");
        sb.append("\n");
        return sb.toString();
    }

    public String indexToString() {
        return "Index:      " + this.index + "\n";
    }
}
