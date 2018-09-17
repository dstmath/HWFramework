package sun.security.provider.certpath;

import java.io.IOException;
import java.security.PublicKey;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertificateException;
import java.security.cert.PKIXCertPathChecker;
import java.security.cert.PKIXRevocationChecker;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.ListIterator;
import java.util.Set;
import javax.security.auth.x500.X500Principal;
import sun.security.util.Debug;
import sun.security.x509.NameConstraintsExtension;
import sun.security.x509.SubjectKeyIdentifierExtension;
import sun.security.x509.X509CertImpl;
import sun.util.logging.PlatformLogger;

class ReverseState implements State {
    private static final Debug debug = null;
    AlgorithmChecker algorithmChecker;
    int certIndex;
    boolean crlSign;
    int explicitPolicy;
    int inhibitAnyPolicy;
    private boolean init;
    NameConstraintsExtension nc;
    int policyMapping;
    PublicKey pubKey;
    int remainingCACerts;
    RevocationChecker revChecker;
    PolicyNodeImpl rootNode;
    SubjectKeyIdentifierExtension subjKeyId;
    X500Principal subjectDN;
    TrustAnchor trustAnchor;
    UntrustedChecker untrustedChecker;
    ArrayList<PKIXCertPathChecker> userCheckers;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.provider.certpath.ReverseState.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.provider.certpath.ReverseState.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: sun.security.provider.certpath.ReverseState.<clinit>():void");
    }

    ReverseState() {
        this.init = true;
        this.crlSign = true;
    }

    public boolean isInitial() {
        return this.init;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("State [");
        sb.append("\n  subjectDN of last cert: ").append(this.subjectDN);
        sb.append("\n  subjectKeyIdentifier: ").append(String.valueOf(this.subjKeyId));
        sb.append("\n  nameConstraints: ").append(String.valueOf(this.nc));
        sb.append("\n  certIndex: ").append(this.certIndex);
        sb.append("\n  explicitPolicy: ").append(this.explicitPolicy);
        sb.append("\n  policyMapping:  ").append(this.policyMapping);
        sb.append("\n  inhibitAnyPolicy:  ").append(this.inhibitAnyPolicy);
        sb.append("\n  rootNode: ").append(this.rootNode);
        sb.append("\n  remainingCACerts: ").append(this.remainingCACerts);
        sb.append("\n  crlSign: ").append(this.crlSign);
        sb.append("\n  init: ").append(this.init);
        sb.append("\n]\n");
        return sb.toString();
    }

    public void initState(BuilderParams buildParams) throws CertPathValidatorException {
        int i;
        int maxPathLen = buildParams.maxPathLength();
        if (maxPathLen == -1) {
            i = PlatformLogger.OFF;
        } else {
            i = maxPathLen;
        }
        this.remainingCACerts = i;
        if (buildParams.explicitPolicyRequired()) {
            this.explicitPolicy = 0;
        } else {
            this.explicitPolicy = maxPathLen == -1 ? maxPathLen : maxPathLen + 2;
        }
        if (buildParams.policyMappingInhibited()) {
            this.policyMapping = 0;
        } else {
            this.policyMapping = maxPathLen == -1 ? maxPathLen : maxPathLen + 2;
        }
        if (buildParams.anyPolicyInhibited()) {
            this.inhibitAnyPolicy = 0;
        } else {
            if (maxPathLen != -1) {
                maxPathLen += 2;
            }
            this.inhibitAnyPolicy = maxPathLen;
        }
        this.certIndex = 1;
        Set<String> initExpPolSet = new HashSet(1);
        initExpPolSet.add("2.5.29.32.0");
        this.rootNode = new PolicyNodeImpl(null, "2.5.29.32.0", null, false, initExpPolSet, false);
        this.userCheckers = new ArrayList(buildParams.certPathCheckers());
        for (PKIXCertPathChecker checker : this.userCheckers) {
            checker.init(false);
        }
        this.crlSign = true;
        this.init = true;
    }

    public void updateState(TrustAnchor anchor, BuilderParams buildParams) throws CertificateException, IOException, CertPathValidatorException {
        this.trustAnchor = anchor;
        X509Certificate trustedCert = anchor.getTrustedCert();
        if (trustedCert != null) {
            updateState(trustedCert);
        } else {
            updateState(anchor.getCAPublicKey(), anchor.getCA());
        }
        boolean revCheckerAdded = false;
        for (PKIXCertPathChecker checker : this.userCheckers) {
            if (checker instanceof AlgorithmChecker) {
                ((AlgorithmChecker) checker).trySetTrustAnchor(anchor);
            } else if (!(checker instanceof PKIXRevocationChecker)) {
                continue;
            } else if (revCheckerAdded) {
                throw new CertPathValidatorException("Only one PKIXRevocationChecker can be specified");
            } else {
                if (checker instanceof RevocationChecker) {
                    ((RevocationChecker) checker).init(anchor, buildParams);
                }
                ((PKIXRevocationChecker) checker).init(false);
                revCheckerAdded = true;
            }
        }
        if (buildParams.revocationEnabled() && !revCheckerAdded) {
            this.revChecker = new RevocationChecker(anchor, buildParams);
            this.revChecker.init(false);
        }
        this.init = false;
    }

    private void updateState(PublicKey pubKey, X500Principal subjectDN) {
        this.subjectDN = subjectDN;
        this.pubKey = pubKey;
    }

    public void updateState(X509Certificate cert) throws CertificateException, IOException, CertPathValidatorException {
        if (cert != null) {
            this.subjectDN = cert.getSubjectX500Principal();
            X509CertImpl icert = X509CertImpl.toImpl(cert);
            PublicKey newKey = cert.getPublicKey();
            if (PKIX.isDSAPublicKeyWithoutParams(newKey)) {
                newKey = BasicChecker.makeInheritedParamsKey(newKey, this.pubKey);
            }
            this.pubKey = newKey;
            if (this.init) {
                this.init = false;
                return;
            }
            this.subjKeyId = icert.getSubjectKeyIdentifierExtension();
            this.crlSign = RevocationChecker.certCanSignCrl(cert);
            if (this.nc != null) {
                this.nc.merge(icert.getNameConstraintsExtension());
            } else {
                this.nc = icert.getNameConstraintsExtension();
                if (this.nc != null) {
                    this.nc = (NameConstraintsExtension) this.nc.clone();
                }
            }
            this.explicitPolicy = PolicyChecker.mergeExplicitPolicy(this.explicitPolicy, icert, false);
            this.policyMapping = PolicyChecker.mergePolicyMapping(this.policyMapping, icert);
            this.inhibitAnyPolicy = PolicyChecker.mergeInhibitAnyPolicy(this.inhibitAnyPolicy, icert);
            this.certIndex++;
            this.remainingCACerts = ConstraintsChecker.mergeBasicConstraints(cert, this.remainingCACerts);
            this.init = false;
        }
    }

    public boolean keyParamsNeeded() {
        return false;
    }

    public Object clone() {
        try {
            ReverseState clonedState = (ReverseState) super.clone();
            clonedState.userCheckers = (ArrayList) this.userCheckers.clone();
            ListIterator<PKIXCertPathChecker> li = clonedState.userCheckers.listIterator();
            while (li.hasNext()) {
                PKIXCertPathChecker checker = (PKIXCertPathChecker) li.next();
                if (checker instanceof Cloneable) {
                    li.set((PKIXCertPathChecker) checker.clone());
                }
            }
            if (this.nc != null) {
                clonedState.nc = (NameConstraintsExtension) this.nc.clone();
            }
            if (this.rootNode != null) {
                clonedState.rootNode = this.rootNode.copyTree();
            }
            return clonedState;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e.toString(), e);
        }
    }
}
