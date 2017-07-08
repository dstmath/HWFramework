package sun.security.provider.certpath;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.PublicKey;
import java.security.cert.CertPathBuilderException;
import java.security.cert.CertPathBuilderResult;
import java.security.cert.CertPathBuilderSpi;
import java.security.cert.CertPathChecker;
import java.security.cert.CertPathParameters;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertPathValidatorException.BasicReason;
import java.security.cert.CertSelector;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXCertPathBuilderResult;
import java.security.cert.PKIXCertPathChecker;
import java.security.cert.PKIXReason;
import java.security.cert.PKIXRevocationChecker;
import java.security.cert.PolicyNode;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.security.auth.x500.X500Principal;
import sun.security.util.Debug;
import sun.security.x509.PKIXExtensions;

public final class SunCertPathBuilder extends CertPathBuilderSpi {
    private static final Debug debug = null;
    private BuilderParams buildParams;
    private CertificateFactory cf;
    private PublicKey finalPublicKey;
    private boolean pathCompleted;
    private PolicyNode policyTreeResult;
    private TrustAnchor trustAnchor;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.provider.certpath.SunCertPathBuilder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.provider.certpath.SunCertPathBuilder.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: sun.security.provider.certpath.SunCertPathBuilder.<clinit>():void");
    }

    public SunCertPathBuilder() throws CertPathBuilderException {
        this.pathCompleted = false;
        try {
            this.cf = CertificateFactory.getInstance("X.509");
        } catch (Throwable e) {
            throw new CertPathBuilderException(e);
        }
    }

    public CertPathChecker engineGetRevocationChecker() {
        return new RevocationChecker();
    }

    public CertPathBuilderResult engineBuild(CertPathParameters params) throws CertPathBuilderException, InvalidAlgorithmParameterException {
        if (debug != null) {
            debug.println("SunCertPathBuilder.engineBuild(" + params + ")");
        }
        this.buildParams = PKIX.checkBuilderParams(params);
        return build();
    }

    private PKIXCertPathBuilderResult build() throws CertPathBuilderException {
        List<List<Vertex>> adjList = new ArrayList();
        PKIXCertPathBuilderResult result = buildCertPath(false, adjList);
        if (result == null) {
            if (debug != null) {
                debug.println("SunCertPathBuilder.engineBuild: 2nd pass");
            }
            adjList.clear();
            result = buildCertPath(true, adjList);
            if (result == null) {
                throw new SunCertPathBuilderException("unable to find valid certification path to requested target", new AdjacencyList(adjList));
            }
        }
        return result;
    }

    private PKIXCertPathBuilderResult buildCertPath(boolean searchAllCertStores, List<List<Vertex>> adjList) throws CertPathBuilderException {
        this.pathCompleted = false;
        this.trustAnchor = null;
        this.finalPublicKey = null;
        this.policyTreeResult = null;
        List certPathList = new LinkedList();
        try {
            if (this.buildParams.buildForward()) {
                buildForward(adjList, certPathList, searchAllCertStores);
            } else {
                buildReverse(adjList, certPathList);
            }
            try {
                if (!this.pathCompleted) {
                    return null;
                }
                if (debug != null) {
                    debug.println("SunCertPathBuilder.engineBuild() pathCompleted");
                }
                Collections.reverse(certPathList);
                return new SunCertPathBuilderResult(this.cf.generateCertPath(certPathList), this.trustAnchor, this.policyTreeResult, this.finalPublicKey, new AdjacencyList(adjList));
            } catch (CertificateException e) {
                if (debug != null) {
                    debug.println("SunCertPathBuilder.engineBuild() exception in wrap-up");
                    e.printStackTrace();
                }
                throw new SunCertPathBuilderException("unable to find valid certification path to requested target", e, new AdjacencyList(adjList));
            }
        } catch (Exception e2) {
            if (debug != null) {
                debug.println("SunCertPathBuilder.engineBuild() exception in build");
                e2.printStackTrace();
            }
            throw new SunCertPathBuilderException("unable to find valid certification path to requested target", e2, new AdjacencyList(adjList));
        }
    }

    private void buildReverse(List<List<Vertex>> adjacencyList, LinkedList<X509Certificate> certPathList) throws GeneralSecurityException, IOException {
        if (debug != null) {
            debug.println("SunCertPathBuilder.buildReverse()...");
            debug.println("SunCertPathBuilder.buildReverse() InitialPolicies: " + this.buildParams.initialPolicies());
        }
        ReverseState currentState = new ReverseState();
        adjacencyList.clear();
        adjacencyList.add(new LinkedList());
        Iterator<TrustAnchor> iter = this.buildParams.trustAnchors().iterator();
        while (iter.hasNext()) {
            TrustAnchor anchor = (TrustAnchor) iter.next();
            if (anchorIsTarget(anchor, this.buildParams.targetCertConstraints())) {
                this.trustAnchor = anchor;
                this.pathCompleted = true;
                this.finalPublicKey = anchor.getTrustedCert().getPublicKey();
                break;
            }
            PublicKey pubKey;
            X509Certificate trustedCert = anchor.getTrustedCert();
            if (trustedCert != null) {
                pubKey = trustedCert.getPublicKey();
            } else {
                pubKey = anchor.getCAPublicKey();
            }
            if (!PKIX.isDSAPublicKeyWithoutParams(pubKey)) {
                currentState.initState(this.buildParams);
                currentState.updateState(anchor, this.buildParams);
                currentState.algorithmChecker = new AlgorithmChecker(anchor);
                currentState.untrustedChecker = new UntrustedChecker();
                try {
                    depthFirstSearchReverse(null, currentState, new ReverseBuilder(this.buildParams), adjacencyList, certPathList);
                    if (this.pathCompleted) {
                        break;
                    }
                } catch (Exception e) {
                    if (!iter.hasNext()) {
                        throw e;
                    }
                }
            }
        }
        if (debug != null) {
            debug.println("SunCertPathBuilder.buildReverse() returned from depthFirstSearchReverse()");
            debug.println("SunCertPathBuilder.buildReverse() certPathList.size: " + certPathList.size());
        }
    }

    private void buildForward(List<List<Vertex>> adjacencyList, LinkedList<X509Certificate> certPathList, boolean searchAllCertStores) throws GeneralSecurityException, IOException {
        if (debug != null) {
            debug.println("SunCertPathBuilder.buildForward()...");
        }
        ForwardState currentState = new ForwardState();
        currentState.initState(this.buildParams.certPathCheckers());
        adjacencyList.clear();
        adjacencyList.add(new LinkedList());
        currentState.untrustedChecker = new UntrustedChecker();
        depthFirstSearchForward(this.buildParams.targetSubject(), currentState, new ForwardBuilder(this.buildParams, searchAllCertStores), adjacencyList, certPathList);
    }

    private void depthFirstSearchForward(X500Principal dN, ForwardState currentState, ForwardBuilder builder, List<List<Vertex>> adjList, LinkedList<X509Certificate> cpList) throws GeneralSecurityException, IOException {
        if (debug != null) {
            debug.println("SunCertPathBuilder.depthFirstSearchForward(" + dN + ", " + currentState.toString() + ")");
        }
        List<Vertex> vertices = addVertices(builder.getMatchingCerts(currentState, this.buildParams.certStores()), adjList);
        if (debug != null) {
            debug.println("SunCertPathBuilder.depthFirstSearchForward(): certs.size=" + vertices.size());
        }
        for (Vertex vertex : vertices) {
            ForwardState nextState = (ForwardState) currentState.clone();
            X509Certificate cert = vertex.getCertificate();
            try {
                builder.verifyCert(cert, nextState, cpList);
                if (builder.isPathCompleted(cert)) {
                    if (debug != null) {
                        debug.println("SunCertPathBuilder.depthFirstSearchForward(): commencing final verification");
                    }
                    List<X509Certificate> arrayList = new ArrayList((Collection) cpList);
                    if (builder.trustAnchor.getTrustedCert() == null) {
                        arrayList.add(0, cert);
                    }
                    PolicyNodeImpl rootNode = new PolicyNodeImpl(null, "2.5.29.32.0", null, false, Collections.singleton("2.5.29.32.0"), false);
                    List<PKIXCertPathChecker> checkers = new ArrayList();
                    PolicyChecker policyChecker = new PolicyChecker(this.buildParams.initialPolicies(), arrayList.size(), this.buildParams.explicitPolicyRequired(), this.buildParams.policyMappingInhibited(), this.buildParams.anyPolicyInhibited(), this.buildParams.policyQualifiersRejected(), rootNode);
                    checkers.add(policyChecker);
                    checkers.add(new AlgorithmChecker(builder.trustAnchor));
                    BasicChecker basicChecker = null;
                    if (nextState.keyParamsNeeded()) {
                        PublicKey rootKey = cert.getPublicKey();
                        if (builder.trustAnchor.getTrustedCert() == null) {
                            rootKey = builder.trustAnchor.getCAPublicKey();
                            if (debug != null) {
                                debug.println("SunCertPathBuilder.depthFirstSearchForward using buildParams public key: " + rootKey.toString());
                            }
                        }
                        TrustAnchor trustAnchor = new TrustAnchor(cert.getSubjectX500Principal(), rootKey, null);
                        BasicChecker basicChecker2 = new BasicChecker(r0, this.buildParams.date(), this.buildParams.sigProvider(), true);
                        checkers.add(basicChecker2);
                    }
                    this.buildParams.setCertPath(this.cf.generateCertPath((List) arrayList));
                    boolean revCheckerAdded = false;
                    List<PKIXCertPathChecker> ckrs = this.buildParams.certPathCheckers();
                    for (PKIXCertPathChecker ckr : ckrs) {
                        if (ckr instanceof PKIXRevocationChecker) {
                            if (revCheckerAdded) {
                                throw new CertPathValidatorException("Only one PKIXRevocationChecker can be specified");
                            }
                            revCheckerAdded = true;
                            if (ckr instanceof RevocationChecker) {
                                ((RevocationChecker) ckr).init(builder.trustAnchor, this.buildParams);
                            }
                        }
                    }
                    if (this.buildParams.revocationEnabled() && !revCheckerAdded) {
                        checkers.add(new RevocationChecker(builder.trustAnchor, this.buildParams));
                    }
                    checkers.addAll(ckrs);
                    for (int i = 0; i < arrayList.size(); i++) {
                        Certificate currCert = (X509Certificate) arrayList.get(i);
                        if (debug != null) {
                            debug.println("current subject = " + currCert.getSubjectX500Principal());
                        }
                        Set<String> unresCritExts = currCert.getCriticalExtensionOIDs();
                        if (unresCritExts == null) {
                            unresCritExts = Collections.emptySet();
                        }
                        for (PKIXCertPathChecker currChecker : checkers) {
                            if (!currChecker.isForwardCheckingSupported()) {
                                if (i == 0) {
                                    currChecker.init(false);
                                    if (currChecker instanceof AlgorithmChecker) {
                                        ((AlgorithmChecker) currChecker).trySetTrustAnchor(builder.trustAnchor);
                                    }
                                }
                                try {
                                    currChecker.check(currCert, unresCritExts);
                                } catch (Throwable cpve) {
                                    if (debug != null) {
                                        debug.println("SunCertPathBuilder.depthFirstSearchForward(): final verification failed: " + cpve);
                                    }
                                    if (this.buildParams.targetCertConstraints().match(currCert) && cpve.getReason() == BasicReason.REVOKED) {
                                        throw cpve;
                                    }
                                    vertex.setThrowable(cpve);
                                }
                            }
                        }
                        for (PKIXCertPathChecker checker : this.buildParams.certPathCheckers()) {
                            if (checker.isForwardCheckingSupported()) {
                                Set<String> suppExts = checker.getSupportedExtensions();
                                if (suppExts != null) {
                                    unresCritExts.removeAll(suppExts);
                                }
                            }
                        }
                        if (!unresCritExts.isEmpty()) {
                            unresCritExts.remove(PKIXExtensions.BasicConstraints_Id.toString());
                            unresCritExts.remove(PKIXExtensions.NameConstraints_Id.toString());
                            unresCritExts.remove(PKIXExtensions.CertificatePolicies_Id.toString());
                            unresCritExts.remove(PKIXExtensions.PolicyMappings_Id.toString());
                            unresCritExts.remove(PKIXExtensions.PolicyConstraints_Id.toString());
                            unresCritExts.remove(PKIXExtensions.InhibitAnyPolicy_Id.toString());
                            unresCritExts.remove(PKIXExtensions.SubjectAlternativeName_Id.toString());
                            unresCritExts.remove(PKIXExtensions.KeyUsage_Id.toString());
                            unresCritExts.remove(PKIXExtensions.ExtendedKeyUsage_Id.toString());
                            if (!unresCritExts.isEmpty()) {
                                throw new CertPathValidatorException("unrecognized critical extension(s)", null, null, -1, PKIXReason.UNRECOGNIZED_CRIT_EXT);
                            }
                        }
                    }
                    if (debug != null) {
                        debug.println("SunCertPathBuilder.depthFirstSearchForward(): final verification succeeded - path completed!");
                    }
                    this.pathCompleted = true;
                    if (builder.trustAnchor.getTrustedCert() == null) {
                        builder.addCertToPath(cert, cpList);
                    }
                    this.trustAnchor = builder.trustAnchor;
                    if (basicChecker != null) {
                        this.finalPublicKey = basicChecker.getPublicKey();
                    } else {
                        Certificate finalCert;
                        if (cpList.isEmpty()) {
                            finalCert = builder.trustAnchor.getTrustedCert();
                        } else {
                            finalCert = (Certificate) cpList.getLast();
                        }
                        this.finalPublicKey = finalCert.getPublicKey();
                    }
                    this.policyTreeResult = policyChecker.getPolicyTree();
                    return;
                }
                builder.addCertToPath(cert, cpList);
                nextState.updateState(cert);
                adjList.add(new LinkedList());
                vertex.setIndex(adjList.size() - 1);
                depthFirstSearchForward(cert.getIssuerX500Principal(), nextState, builder, adjList, cpList);
                if (!this.pathCompleted) {
                    if (debug != null) {
                        debug.println("SunCertPathBuilder.depthFirstSearchForward(): backtracking");
                    }
                    builder.removeFinalCertFromPath(cpList);
                } else {
                    return;
                }
            } catch (Throwable gse) {
                if (debug != null) {
                    debug.println("SunCertPathBuilder.depthFirstSearchForward(): validation failed: " + gse);
                    gse.printStackTrace();
                }
                vertex.setThrowable(gse);
            }
        }
    }

    private void depthFirstSearchReverse(X500Principal dN, ReverseState currentState, ReverseBuilder builder, List<List<Vertex>> adjList, LinkedList<X509Certificate> cpList) throws GeneralSecurityException, IOException {
        if (debug != null) {
            debug.println("SunCertPathBuilder.depthFirstSearchReverse(" + dN + ", " + currentState.toString() + ")");
        }
        List<Vertex> vertices = addVertices(builder.getMatchingCerts(currentState, this.buildParams.certStores()), adjList);
        if (debug != null) {
            debug.println("SunCertPathBuilder.depthFirstSearchReverse(): certs.size=" + vertices.size());
        }
        for (Vertex vertex : vertices) {
            ReverseState nextState = (ReverseState) currentState.clone();
            X509Certificate cert = vertex.getCertificate();
            try {
                builder.verifyCert(cert, nextState, cpList);
                if (!currentState.isInitial()) {
                    builder.addCertToPath(cert, cpList);
                }
                this.trustAnchor = currentState.trustAnchor;
                if (builder.isPathCompleted(cert)) {
                    if (debug != null) {
                        debug.println("SunCertPathBuilder.depthFirstSearchReverse(): path completed!");
                    }
                    this.pathCompleted = true;
                    PolicyNodeImpl rootNode = nextState.rootNode;
                    if (rootNode == null) {
                        this.policyTreeResult = null;
                    } else {
                        this.policyTreeResult = rootNode.copyTree();
                        ((PolicyNodeImpl) this.policyTreeResult).setImmutable();
                    }
                    this.finalPublicKey = cert.getPublicKey();
                    if (PKIX.isDSAPublicKeyWithoutParams(this.finalPublicKey)) {
                        this.finalPublicKey = BasicChecker.makeInheritedParamsKey(this.finalPublicKey, currentState.pubKey);
                    }
                    return;
                }
                nextState.updateState(cert);
                adjList.add(new LinkedList());
                vertex.setIndex(adjList.size() - 1);
                depthFirstSearchReverse(cert.getSubjectX500Principal(), nextState, builder, adjList, cpList);
                if (!this.pathCompleted) {
                    if (debug != null) {
                        debug.println("SunCertPathBuilder.depthFirstSearchReverse(): backtracking");
                    }
                    if (!currentState.isInitial()) {
                        builder.removeFinalCertFromPath(cpList);
                    }
                } else {
                    return;
                }
            } catch (Object gse) {
                if (debug != null) {
                    debug.println("SunCertPathBuilder.depthFirstSearchReverse(): validation failed: " + gse);
                }
                vertex.setThrowable(gse);
            }
        }
        if (debug != null) {
            debug.println("SunCertPathBuilder.depthFirstSearchReverse() all certs in this adjacency list checked");
        }
    }

    private static List<Vertex> addVertices(Collection<X509Certificate> certs, List<List<Vertex>> adjList) {
        List<Vertex> l = (List) adjList.get(adjList.size() - 1);
        for (X509Certificate cert : certs) {
            l.add(new Vertex(cert));
        }
        return l;
    }

    private static boolean anchorIsTarget(TrustAnchor anchor, CertSelector sel) {
        X509Certificate anchorCert = anchor.getTrustedCert();
        if (anchorCert != null) {
            return sel.match(anchorCert);
        }
        return false;
    }
}
