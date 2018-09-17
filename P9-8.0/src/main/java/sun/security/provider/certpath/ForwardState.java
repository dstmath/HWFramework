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
    private static final Debug debug = Debug.getInstance("certpath");
    X509CertImpl cert;
    ArrayList<PKIXCertPathChecker> forwardCheckers;
    private boolean init = true;
    X500Principal issuerDN;
    boolean keyParamsNeededFlag = false;
    HashSet<GeneralNameInterface> subjectNamesTraversed;
    int traversedCACerts;

    ForwardState() {
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
        return sb.-java_util_stream_Collectors-mthref-7();
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
            if (this.init || (X509CertImpl.isSelfIssued(cert) ^ 1) != 0) {
                this.subjectNamesTraversed.-java_util_stream_DistinctOps$1-mthref-1(X500Name.asX500Name(cert.getSubjectX500Principal()));
                try {
                    SubjectAlternativeNameExtension subjAltNameExt = icert.getSubjectAlternativeNameExtension();
                    if (subjAltNameExt != null) {
                        for (GeneralName gName : subjAltNameExt.get(SubjectAlternativeNameExtension.SUBJECT_NAME).names()) {
                            this.subjectNamesTraversed.-java_util_stream_DistinctOps$1-mthref-1(gName.getName());
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
