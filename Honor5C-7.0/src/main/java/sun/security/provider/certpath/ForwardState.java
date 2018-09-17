package sun.security.provider.certpath;

import java.io.IOException;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertificateException;
import java.security.cert.PKIXCertPathChecker;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import javax.security.auth.x500.X500Principal;
import sun.security.util.Debug;
import sun.security.x509.GeneralName;
import sun.security.x509.GeneralNameInterface;
import sun.security.x509.SubjectAlternativeNameExtension;
import sun.security.x509.X500Name;
import sun.security.x509.X509CertImpl;

class ForwardState implements State {
    private static final Debug debug = null;
    X509CertImpl cert;
    ArrayList<PKIXCertPathChecker> forwardCheckers;
    private boolean init;
    X500Principal issuerDN;
    boolean keyParamsNeededFlag;
    HashSet<GeneralNameInterface> subjectNamesTraversed;
    int traversedCACerts;
    UntrustedChecker untrustedChecker;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.provider.certpath.ForwardState.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.provider.certpath.ForwardState.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.security.provider.certpath.ForwardState.<clinit>():void");
    }

    ForwardState() {
        this.init = true;
        this.keyParamsNeededFlag = false;
    }

    public boolean isInitial() {
        return this.init;
    }

    public boolean keyParamsNeeded() {
        return this.keyParamsNeededFlag;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("State [");
        sb.append("\n  issuerDN of last cert: ").append(this.issuerDN);
        sb.append("\n  traversedCACerts: ").append(this.traversedCACerts);
        sb.append("\n  init: ").append(String.valueOf(this.init));
        sb.append("\n  keyParamsNeeded: ").append(String.valueOf(this.keyParamsNeededFlag));
        sb.append("\n  subjectNamesTraversed: \n").append(this.subjectNamesTraversed);
        sb.append("]\n");
        return sb.toString();
    }

    public void initState(List<PKIXCertPathChecker> certPathCheckers) throws CertPathValidatorException {
        this.subjectNamesTraversed = new HashSet();
        this.traversedCACerts = 0;
        this.forwardCheckers = new ArrayList();
        for (PKIXCertPathChecker checker : certPathCheckers) {
            if (checker.isForwardCheckingSupported()) {
                checker.init(true);
                this.forwardCheckers.add(checker);
            }
        }
        this.init = true;
    }

    public void updateState(X509Certificate cert) throws CertificateException, IOException, CertPathValidatorException {
        if (cert != null) {
            X509CertImpl icert = X509CertImpl.toImpl(cert);
            if (PKIX.isDSAPublicKeyWithoutParams(icert.getPublicKey())) {
                this.keyParamsNeededFlag = true;
            }
            this.cert = icert;
            this.issuerDN = cert.getIssuerX500Principal();
            if (!(X509CertImpl.isSelfIssued(cert) || this.init || cert.getBasicConstraints() == -1)) {
                this.traversedCACerts++;
            }
            if (this.init || !X509CertImpl.isSelfIssued(cert)) {
                this.subjectNamesTraversed.add(X500Name.asX500Name(cert.getSubjectX500Principal()));
                try {
                    SubjectAlternativeNameExtension subjAltNameExt = icert.getSubjectAlternativeNameExtension();
                    if (subjAltNameExt != null) {
                        for (GeneralName gName : subjAltNameExt.get(SubjectAlternativeNameExtension.SUBJECT_NAME).names()) {
                            this.subjectNamesTraversed.add(gName.getName());
                        }
                    }
                } catch (Throwable e) {
                    if (debug != null) {
                        debug.println("ForwardState.updateState() unexpected exception");
                        e.printStackTrace();
                    }
                    throw new CertPathValidatorException(e);
                }
            }
            this.init = false;
        }
    }

    public Object clone() {
        try {
            ForwardState clonedState = (ForwardState) super.clone();
            clonedState.forwardCheckers = (ArrayList) this.forwardCheckers.clone();
            ListIterator<PKIXCertPathChecker> li = clonedState.forwardCheckers.listIterator();
            while (li.hasNext()) {
                PKIXCertPathChecker checker = (PKIXCertPathChecker) li.next();
                if (checker instanceof Cloneable) {
                    li.set((PKIXCertPathChecker) checker.clone());
                }
            }
            clonedState.subjectNamesTraversed = (HashSet) this.subjectNamesTraversed.clone();
            return clonedState;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e.toString(), e);
        }
    }
}
