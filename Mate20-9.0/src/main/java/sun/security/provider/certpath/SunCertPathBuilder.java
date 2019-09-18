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
import sun.security.provider.certpath.PKIX;
import sun.security.util.Debug;
import sun.security.x509.PKIXExtensions;

public final class SunCertPathBuilder extends CertPathBuilderSpi {
    private static final Debug debug = Debug.getInstance("certpath");
    private PKIX.BuilderParams buildParams;
    private CertificateFactory cf;
    private PublicKey finalPublicKey;
    private boolean pathCompleted = false;
    private PolicyNode policyTreeResult;
    private TrustAnchor trustAnchor;

    public SunCertPathBuilder() throws CertPathBuilderException {
        try {
            this.cf = CertificateFactory.getInstance("X.509");
        } catch (CertificateException e) {
            throw new CertPathBuilderException((Throwable) e);
        }
    }

    public CertPathChecker engineGetRevocationChecker() {
        return new RevocationChecker();
    }

    public CertPathBuilderResult engineBuild(CertPathParameters params) throws CertPathBuilderException, InvalidAlgorithmParameterException {
        if (debug != null) {
            Debug debug2 = debug;
            debug2.println("SunCertPathBuilder.engineBuild(" + params + ")");
        }
        this.buildParams = PKIX.checkBuilderParams(params);
        return build();
    }

    private PKIXCertPathBuilderResult build() throws CertPathBuilderException {
        List<List<Vertex>> adjList = new ArrayList<>();
        PKIXCertPathBuilderResult result = buildCertPath(false, adjList);
        if (result == null) {
            if (debug != null) {
                debug.println("SunCertPathBuilder.engineBuild: 2nd pass; try building again searching all certstores");
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
        LinkedList<X509Certificate> certPathList = new LinkedList<>();
        try {
            buildForward(adjList, certPathList, searchAllCertStores);
            try {
                if (!this.pathCompleted) {
                    return null;
                }
                if (debug != null) {
                    debug.println("SunCertPathBuilder.engineBuild() pathCompleted");
                }
                Collections.reverse(certPathList);
                SunCertPathBuilderResult sunCertPathBuilderResult = new SunCertPathBuilderResult(this.cf.generateCertPath((List<? extends Certificate>) certPathList), this.trustAnchor, this.policyTreeResult, this.finalPublicKey, new AdjacencyList(adjList));
                return sunCertPathBuilderResult;
            } catch (CertificateException e) {
                if (debug != null) {
                    debug.println("SunCertPathBuilder.engineBuild() exception in wrap-up");
                    e.printStackTrace();
                }
                throw new SunCertPathBuilderException("unable to find valid certification path to requested target", e, new AdjacencyList(adjList));
            }
        } catch (IOException | GeneralSecurityException e2) {
            if (debug != null) {
                debug.println("SunCertPathBuilder.engineBuild() exception in build");
                e2.printStackTrace();
            }
            throw new SunCertPathBuilderException("unable to find valid certification path to requested target", e2, new AdjacencyList(adjList));
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
        depthFirstSearchForward(this.buildParams.targetSubject(), currentState, new ForwardBuilder(this.buildParams, searchAllCertStores), adjacencyList, certPathList);
    }

    private void depthFirstSearchForward(X500Principal dN, ForwardState currentState, ForwardBuilder builder, List<List<Vertex>> adjList, LinkedList<X509Certificate> cpList) throws GeneralSecurityException, IOException {
        Iterator<Vertex> it;
        List<Vertex> vertices;
        Collection<X509Certificate> certs;
        BasicChecker basicChecker;
        Certificate finalCert;
        List<PKIXCertPathChecker> ckrs;
        List<X509Certificate> appendedCerts;
        List<PKIXCertPathChecker> checkers;
        Iterator<PKIXCertPathChecker> it2;
        boolean revCheckerAdded;
        ForwardBuilder forwardBuilder = builder;
        List<List<Vertex>> list = adjList;
        LinkedList<X509Certificate> linkedList = cpList;
        if (debug != null) {
            debug.println("SunCertPathBuilder.depthFirstSearchForward(" + dN + ", " + currentState.toString() + ")");
        } else {
            X500Principal x500Principal = dN;
        }
        Collection<X509Certificate> certs2 = forwardBuilder.getMatchingCerts(currentState, this.buildParams.certStores());
        List<Vertex> vertices2 = addVertices(certs2, list);
        if (debug != null) {
            debug.println("SunCertPathBuilder.depthFirstSearchForward(): certs.size=" + vertices2.size());
        }
        Iterator<Vertex> it3 = vertices2.iterator();
        while (it3.hasNext()) {
            Vertex vertex = it3.next();
            ForwardState nextState = (ForwardState) currentState.clone();
            X509Certificate cert = vertex.getCertificate();
            try {
                forwardBuilder.verifyCert(cert, nextState, linkedList);
                if (forwardBuilder.isPathCompleted(cert)) {
                    if (debug != null) {
                        debug.println("SunCertPathBuilder.depthFirstSearchForward(): commencing final verification");
                    }
                    List<X509Certificate> appendedCerts2 = new ArrayList<>((Collection<? extends X509Certificate>) linkedList);
                    if (forwardBuilder.trustAnchor.getTrustedCert() == null) {
                        appendedCerts2.add(0, cert);
                    }
                    PolicyNodeImpl policyNodeImpl = new PolicyNodeImpl(null, "2.5.29.32.0", null, false, Collections.singleton("2.5.29.32.0"), false);
                    PolicyChecker policyChecker = new PolicyChecker(this.buildParams.initialPolicies(), appendedCerts2.size(), this.buildParams.explicitPolicyRequired(), this.buildParams.policyMappingInhibited(), this.buildParams.anyPolicyInhibited(), this.buildParams.policyQualifiersRejected(), policyNodeImpl);
                    PolicyChecker policyChecker2 = policyChecker;
                    List<PKIXCertPathChecker> checkers2 = new ArrayList<>();
                    checkers2.add(policyChecker2);
                    checkers2.add(new AlgorithmChecker(forwardBuilder.trustAnchor));
                    if (nextState.keyParamsNeeded()) {
                        PublicKey rootKey = cert.getPublicKey();
                        if (forwardBuilder.trustAnchor.getTrustedCert() == null) {
                            rootKey = forwardBuilder.trustAnchor.getCAPublicKey();
                            if (debug != null) {
                                Debug debug2 = debug;
                                StringBuilder sb = new StringBuilder();
                                certs = certs2;
                                sb.append("SunCertPathBuilder.depthFirstSearchForward using buildParams public key: ");
                                sb.append(rootKey.toString());
                                debug2.println(sb.toString());
                                PublicKey publicKey = rootKey;
                                vertices = vertices2;
                                basicChecker = new BasicChecker(new TrustAnchor(cert.getSubjectX500Principal(), rootKey, (byte[]) null), this.buildParams.date(), this.buildParams.sigProvider(), true);
                                checkers2.add(basicChecker);
                            }
                        }
                        certs = certs2;
                        PublicKey publicKey2 = rootKey;
                        vertices = vertices2;
                        basicChecker = new BasicChecker(new TrustAnchor(cert.getSubjectX500Principal(), rootKey, (byte[]) null), this.buildParams.date(), this.buildParams.sigProvider(), true);
                        checkers2.add(basicChecker);
                    } else {
                        certs = certs2;
                        vertices = vertices2;
                        basicChecker = null;
                    }
                    this.buildParams.setCertPath(this.cf.generateCertPath((List<? extends Certificate>) appendedCerts2));
                    List<PKIXCertPathChecker> ckrs2 = this.buildParams.certPathCheckers();
                    Iterator<PKIXCertPathChecker> it4 = ckrs2.iterator();
                    boolean revCheckerAdded2 = false;
                    while (it4.hasNext()) {
                        PKIXCertPathChecker ckr = it4.next();
                        Iterator<PKIXCertPathChecker> it5 = it4;
                        if (ckr instanceof PKIXRevocationChecker) {
                            if (!revCheckerAdded2) {
                                if (ckr instanceof RevocationChecker) {
                                    PKIXCertPathChecker pKIXCertPathChecker = ckr;
                                    revCheckerAdded = true;
                                    ((RevocationChecker) ckr).init(forwardBuilder.trustAnchor, this.buildParams);
                                } else {
                                    revCheckerAdded = true;
                                }
                                revCheckerAdded2 = revCheckerAdded;
                            } else {
                                PKIXCertPathChecker pKIXCertPathChecker2 = ckr;
                                throw new CertPathValidatorException("Only one PKIXRevocationChecker can be specified");
                            }
                        }
                        it4 = it5;
                    }
                    if (!this.buildParams.revocationEnabled() || revCheckerAdded2) {
                    } else {
                        boolean z = revCheckerAdded2;
                        checkers2.add(new RevocationChecker(forwardBuilder.trustAnchor, this.buildParams));
                    }
                    checkers2.addAll(ckrs2);
                    int i = 0;
                    while (true) {
                        int i2 = i;
                        if (i2 < appendedCerts2.size()) {
                            X509Certificate currCert = appendedCerts2.get(i2);
                            if (debug != null) {
                                Debug debug3 = debug;
                                appendedCerts = appendedCerts2;
                                StringBuilder sb2 = new StringBuilder();
                                ckrs = ckrs2;
                                sb2.append("current subject = ");
                                sb2.append((Object) currCert.getSubjectX500Principal());
                                debug3.println(sb2.toString());
                            } else {
                                appendedCerts = appendedCerts2;
                                ckrs = ckrs2;
                            }
                            Set<String> unresCritExts = currCert.getCriticalExtensionOIDs();
                            if (unresCritExts == null) {
                                unresCritExts = Collections.emptySet();
                            }
                            Set<String> unresCritExts2 = unresCritExts;
                            Iterator<PKIXCertPathChecker> it6 = checkers2.iterator();
                            while (it6.hasNext()) {
                                PKIXCertPathChecker currChecker = it6.next();
                                if (!currChecker.isForwardCheckingSupported()) {
                                    if (i2 == 0) {
                                        it2 = it6;
                                        currChecker.init(false);
                                        if (currChecker instanceof AlgorithmChecker) {
                                            checkers = checkers2;
                                            ((AlgorithmChecker) currChecker).trySetTrustAnchor(forwardBuilder.trustAnchor);
                                            currChecker.check(currCert, unresCritExts2);
                                        }
                                    } else {
                                        it2 = it6;
                                    }
                                    checkers = checkers2;
                                    try {
                                        currChecker.check(currCert, unresCritExts2);
                                    } catch (CertPathValidatorException cpve) {
                                        CertPathValidatorException certPathValidatorException = cpve;
                                        if (debug != null) {
                                            Debug debug4 = debug;
                                            PKIXCertPathChecker pKIXCertPathChecker3 = currChecker;
                                            StringBuilder sb3 = new StringBuilder();
                                            it = it3;
                                            sb3.append("SunCertPathBuilder.depthFirstSearchForward(): final verification failed: ");
                                            sb3.append((Object) cpve);
                                            debug4.println(sb3.toString());
                                        } else {
                                            it = it3;
                                        }
                                        if (!this.buildParams.targetCertConstraints().match(currCert) || cpve.getReason() != CertPathValidatorException.BasicReason.REVOKED) {
                                            vertex.setThrowable(cpve);
                                        } else {
                                            throw cpve;
                                        }
                                    }
                                } else {
                                    it2 = it6;
                                    checkers = checkers2;
                                }
                                it6 = it2;
                                checkers2 = checkers;
                                it3 = it3;
                            }
                            List<PKIXCertPathChecker> checkers3 = checkers2;
                            Iterator<Vertex> it7 = it3;
                            for (PKIXCertPathChecker checker : this.buildParams.certPathCheckers()) {
                                if (checker.isForwardCheckingSupported()) {
                                    Set<String> suppExts = checker.getSupportedExtensions();
                                    if (suppExts != null) {
                                        unresCritExts2.removeAll(suppExts);
                                    }
                                }
                            }
                            if (!unresCritExts2.isEmpty()) {
                                unresCritExts2.remove(PKIXExtensions.BasicConstraints_Id.toString());
                                unresCritExts2.remove(PKIXExtensions.NameConstraints_Id.toString());
                                unresCritExts2.remove(PKIXExtensions.CertificatePolicies_Id.toString());
                                unresCritExts2.remove(PKIXExtensions.PolicyMappings_Id.toString());
                                unresCritExts2.remove(PKIXExtensions.PolicyConstraints_Id.toString());
                                unresCritExts2.remove(PKIXExtensions.InhibitAnyPolicy_Id.toString());
                                unresCritExts2.remove(PKIXExtensions.SubjectAlternativeName_Id.toString());
                                unresCritExts2.remove(PKIXExtensions.KeyUsage_Id.toString());
                                unresCritExts2.remove(PKIXExtensions.ExtendedKeyUsage_Id.toString());
                                if (!unresCritExts2.isEmpty()) {
                                    CertPathValidatorException certPathValidatorException2 = new CertPathValidatorException("unrecognized critical extension(s)", null, null, -1, PKIXReason.UNRECOGNIZED_CRIT_EXT);
                                    throw certPathValidatorException2;
                                }
                            }
                            i = i2 + 1;
                            appendedCerts2 = appendedCerts;
                            ckrs2 = ckrs;
                            checkers2 = checkers3;
                            it3 = it7;
                        } else {
                            List<X509Certificate> list2 = appendedCerts2;
                            List<PKIXCertPathChecker> list3 = ckrs2;
                            if (debug != null) {
                                debug.println("SunCertPathBuilder.depthFirstSearchForward(): final verification succeeded - path completed!");
                            }
                            this.pathCompleted = true;
                            if (forwardBuilder.trustAnchor.getTrustedCert() == null) {
                                forwardBuilder.addCertToPath(cert, linkedList);
                            }
                            this.trustAnchor = forwardBuilder.trustAnchor;
                            if (basicChecker != null) {
                                this.finalPublicKey = basicChecker.getPublicKey();
                            } else {
                                if (cpList.isEmpty()) {
                                    finalCert = forwardBuilder.trustAnchor.getTrustedCert();
                                } else {
                                    finalCert = cpList.getLast();
                                }
                                this.finalPublicKey = finalCert.getPublicKey();
                            }
                            this.policyTreeResult = policyChecker2.getPolicyTree();
                            return;
                        }
                    }
                } else {
                    certs = certs2;
                    vertices = vertices2;
                    it = it3;
                    forwardBuilder.addCertToPath(cert, linkedList);
                    nextState.updateState(cert);
                    list.add(new LinkedList());
                    vertex.setIndex(adjList.size() - 1);
                    X509Certificate x509Certificate = cert;
                    ForwardState forwardState = nextState;
                    Vertex vertex2 = vertex;
                    depthFirstSearchForward(cert.getIssuerX500Principal(), nextState, forwardBuilder, list, linkedList);
                    if (!this.pathCompleted) {
                        if (debug != null) {
                            debug.println("SunCertPathBuilder.depthFirstSearchForward(): backtracking");
                        }
                        forwardBuilder.removeFinalCertFromPath(linkedList);
                        certs2 = certs;
                        vertices2 = vertices;
                        it3 = it;
                        X500Principal x500Principal2 = dN;
                        ForwardState forwardState2 = currentState;
                    } else {
                        return;
                    }
                }
            } catch (GeneralSecurityException gse) {
                X509Certificate x509Certificate2 = cert;
                ForwardState forwardState3 = nextState;
                certs = certs2;
                vertices = vertices2;
                it = it3;
                Vertex vertex3 = vertex;
                GeneralSecurityException generalSecurityException = gse;
                if (debug != null) {
                    debug.println("SunCertPathBuilder.depthFirstSearchForward(): validation failed: " + gse);
                    gse.printStackTrace();
                }
                vertex3.setThrowable(gse);
            }
        }
        Collection<X509Certificate> collection = certs2;
        List<Vertex> list4 = vertices2;
    }

    private static List<Vertex> addVertices(Collection<X509Certificate> certs, List<List<Vertex>> adjList) {
        List<Vertex> l = adjList.get(adjList.size() - 1);
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
