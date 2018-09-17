package sun.security.provider.certpath;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.CertStore;
import java.security.cert.CertStoreException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import sun.security.util.Debug;
import sun.security.x509.GeneralNameInterface;
import sun.security.x509.GeneralNames;
import sun.security.x509.GeneralSubtrees;
import sun.security.x509.NameConstraintsExtension;
import sun.security.x509.SubjectAlternativeNameExtension;
import sun.security.x509.X500Name;
import sun.security.x509.X509CertImpl;
import sun.util.calendar.BaseCalendar;

public abstract class Builder {
    static final boolean USE_AIA = false;
    private static final Debug debug = null;
    final BuilderParams buildParams;
    private Set<String> matchingPolicies;
    final X509CertSelector targetCertConstraints;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.provider.certpath.Builder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.provider.certpath.Builder.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.security.provider.certpath.Builder.<clinit>():void");
    }

    abstract void addCertToPath(X509Certificate x509Certificate, LinkedList<X509Certificate> linkedList);

    abstract Collection<X509Certificate> getMatchingCerts(State state, List<CertStore> list) throws CertStoreException, CertificateException, IOException;

    abstract boolean isPathCompleted(X509Certificate x509Certificate);

    abstract void removeFinalCertFromPath(LinkedList<X509Certificate> linkedList);

    abstract void verifyCert(X509Certificate x509Certificate, State state, List<X509Certificate> list) throws GeneralSecurityException;

    Builder(BuilderParams buildParams) {
        this.buildParams = buildParams;
        this.targetCertConstraints = (X509CertSelector) buildParams.targetCertConstraints();
    }

    static int distance(GeneralNameInterface base, GeneralNameInterface test, int incomparable) {
        switch (base.constrains(test)) {
            case GeneralNameInterface.NAME_DIFF_TYPE /*-1*/:
                if (debug != null) {
                    debug.println("Builder.distance(): Names are different types");
                }
                return incomparable;
            case GeneralNameInterface.NAME_MATCH /*0*/:
                return 0;
            case BaseCalendar.SUNDAY /*1*/:
            case BaseCalendar.MONDAY /*2*/:
                return test.subtreeDepth() - base.subtreeDepth();
            case BaseCalendar.TUESDAY /*3*/:
                if (debug != null) {
                    debug.println("Builder.distance(): Names are same type but in different subtrees");
                }
                return incomparable;
            default:
                return incomparable;
        }
    }

    static int hops(GeneralNameInterface base, GeneralNameInterface test, int incomparable) {
        switch (base.constrains(test)) {
            case GeneralNameInterface.NAME_DIFF_TYPE /*-1*/:
                if (debug != null) {
                    debug.println("Builder.hops(): Names are different types");
                }
                return incomparable;
            case GeneralNameInterface.NAME_MATCH /*0*/:
                return 0;
            case BaseCalendar.SUNDAY /*1*/:
                return test.subtreeDepth() - base.subtreeDepth();
            case BaseCalendar.MONDAY /*2*/:
                return test.subtreeDepth() - base.subtreeDepth();
            case BaseCalendar.TUESDAY /*3*/:
                if (base.getType() != 4) {
                    if (debug != null) {
                        debug.println("Builder.hops(): hopDistance not implemented for this name type");
                    }
                    return incomparable;
                }
                X500Name baseName = (X500Name) base;
                X500Name testName = (X500Name) test;
                X500Name commonName = baseName.commonAncestor(testName);
                if (commonName == null) {
                    if (debug != null) {
                        debug.println("Builder.hops(): Names are in different namespaces");
                    }
                    return incomparable;
                }
                return (baseName.subtreeDepth() + testName.subtreeDepth()) - (commonName.subtreeDepth() * 2);
            default:
                return incomparable;
        }
    }

    static int targetDistance(NameConstraintsExtension constraints, X509Certificate cert, GeneralNameInterface target) throws IOException {
        if (constraints == null || constraints.verify(cert)) {
            try {
                X509CertImpl certImpl = X509CertImpl.toImpl(cert);
                if (X500Name.asX500Name(certImpl.getSubjectX500Principal()).equals(target)) {
                    return 0;
                }
                int n;
                SubjectAlternativeNameExtension altNameExt = certImpl.getSubjectAlternativeNameExtension();
                if (altNameExt != null) {
                    GeneralNames altNames = altNameExt.get(SubjectAlternativeNameExtension.SUBJECT_NAME);
                    if (altNames != null) {
                        n = altNames.size();
                        for (int j = 0; j < n; j++) {
                            if (altNames.get(j).getName().equals(target)) {
                                return 0;
                            }
                        }
                    }
                }
                NameConstraintsExtension ncExt = certImpl.getNameConstraintsExtension();
                if (ncExt == null) {
                    return -1;
                }
                if (constraints != null) {
                    constraints.merge(ncExt);
                } else {
                    constraints = (NameConstraintsExtension) ncExt.clone();
                }
                if (debug != null) {
                    debug.println("Builder.targetDistance() merged constraints: " + String.valueOf((Object) constraints));
                }
                Object permitted = constraints.get(NameConstraintsExtension.PERMITTED_SUBTREES);
                GeneralSubtrees excluded = constraints.get(NameConstraintsExtension.EXCLUDED_SUBTREES);
                if (permitted != null) {
                    permitted.reduce(excluded);
                }
                if (debug != null) {
                    debug.println("Builder.targetDistance() reduced constraints: " + permitted);
                }
                if (!constraints.verify(target)) {
                    throw new IOException("New certificate not allowed to sign certificate for target");
                } else if (permitted == null) {
                    return -1;
                } else {
                    n = permitted.size();
                    for (int i = 0; i < n; i++) {
                        int distance = distance(permitted.get(i).getName().getName(), target, -1);
                        if (distance >= 0) {
                            return distance + 1;
                        }
                    }
                    return -1;
                }
            } catch (CertificateException e) {
                throw new IOException("Invalid certificate", e);
            }
        }
        throw new IOException("certificate does not satisfy existing name constraints");
    }

    Set<String> getMatchingPolicies() {
        if (this.matchingPolicies != null) {
            Collection initialPolicies = this.buildParams.initialPolicies();
            if (initialPolicies.isEmpty() || initialPolicies.contains("2.5.29.32.0") || !this.buildParams.policyMappingInhibited()) {
                this.matchingPolicies = Collections.emptySet();
            } else {
                this.matchingPolicies = new HashSet(initialPolicies);
                this.matchingPolicies.add("2.5.29.32.0");
            }
        }
        return this.matchingPolicies;
    }

    boolean addMatchingCerts(X509CertSelector selector, Collection<CertStore> certStores, Collection<X509Certificate> resultCerts, boolean checkAll) {
        X509Certificate targetCert = selector.getCertificate();
        if (targetCert == null) {
            boolean add = false;
            for (CertStore store : certStores) {
                try {
                    for (Certificate cert : store.getCertificates(selector)) {
                        if (!X509CertImpl.isSelfSigned((X509Certificate) cert, this.buildParams.sigProvider()) && resultCerts.add((X509Certificate) cert)) {
                            add = true;
                        }
                    }
                    if (!checkAll && add) {
                        return true;
                    }
                } catch (Object cse) {
                    if (debug != null) {
                        debug.println("Builder.addMatchingCerts, non-fatal exception retrieving certs: " + cse);
                        cse.printStackTrace();
                    }
                }
            }
            return add;
        } else if (!selector.match(targetCert) || X509CertImpl.isSelfSigned(targetCert, this.buildParams.sigProvider())) {
            return false;
        } else {
            if (debug != null) {
                debug.println("Builder.addMatchingCerts: adding target cert");
            }
            return resultCerts.add(targetCert);
        }
    }
}
