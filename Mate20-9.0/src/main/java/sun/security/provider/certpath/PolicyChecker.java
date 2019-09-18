package sun.security.provider.certpath;

import java.io.IOException;
import java.security.cert.CertPathValidatorException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.PKIXCertPathChecker;
import java.security.cert.PKIXReason;
import java.security.cert.PolicyNode;
import java.security.cert.PolicyQualifierInfo;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import sun.security.util.Debug;
import sun.security.x509.CertificatePoliciesExtension;
import sun.security.x509.CertificatePolicyMap;
import sun.security.x509.InhibitAnyPolicyExtension;
import sun.security.x509.PKIXExtensions;
import sun.security.x509.PolicyConstraintsExtension;
import sun.security.x509.PolicyInformation;
import sun.security.x509.PolicyMappingsExtension;
import sun.security.x509.X509CertImpl;

class PolicyChecker extends PKIXCertPathChecker {
    static final String ANY_POLICY = "2.5.29.32.0";
    private static final Debug debug = Debug.getInstance("certpath");
    private final boolean anyPolicyInhibited;
    private int certIndex;
    private final int certPathLen;
    private final boolean expPolicyRequired;
    private int explicitPolicy;
    private int inhibitAnyPolicy;
    private final Set<String> initPolicies;
    private final boolean polMappingInhibited;
    private int policyMapping;
    private final boolean rejectPolicyQualifiers;
    private PolicyNodeImpl rootNode;
    private Set<String> supportedExts;

    PolicyChecker(Set<String> initialPolicies, int certPathLen2, boolean expPolicyRequired2, boolean polMappingInhibited2, boolean anyPolicyInhibited2, boolean rejectPolicyQualifiers2, PolicyNodeImpl rootNode2) {
        if (initialPolicies.isEmpty()) {
            this.initPolicies = new HashSet(1);
            this.initPolicies.add(ANY_POLICY);
        } else {
            this.initPolicies = new HashSet(initialPolicies);
        }
        this.certPathLen = certPathLen2;
        this.expPolicyRequired = expPolicyRequired2;
        this.polMappingInhibited = polMappingInhibited2;
        this.anyPolicyInhibited = anyPolicyInhibited2;
        this.rejectPolicyQualifiers = rejectPolicyQualifiers2;
        this.rootNode = rootNode2;
    }

    public void init(boolean forward) throws CertPathValidatorException {
        if (!forward) {
            this.certIndex = 1;
            int i = 0;
            this.explicitPolicy = this.expPolicyRequired ? 0 : this.certPathLen + 1;
            this.policyMapping = this.polMappingInhibited ? 0 : this.certPathLen + 1;
            if (!this.anyPolicyInhibited) {
                i = this.certPathLen + 1;
            }
            this.inhibitAnyPolicy = i;
            return;
        }
        throw new CertPathValidatorException("forward checking not supported");
    }

    public boolean isForwardCheckingSupported() {
        return false;
    }

    public Set<String> getSupportedExtensions() {
        if (this.supportedExts == null) {
            this.supportedExts = new HashSet(4);
            this.supportedExts.add(PKIXExtensions.CertificatePolicies_Id.toString());
            this.supportedExts.add(PKIXExtensions.PolicyMappings_Id.toString());
            this.supportedExts.add(PKIXExtensions.PolicyConstraints_Id.toString());
            this.supportedExts.add(PKIXExtensions.InhibitAnyPolicy_Id.toString());
            this.supportedExts = Collections.unmodifiableSet(this.supportedExts);
        }
        return this.supportedExts;
    }

    public void check(Certificate cert, Collection<String> unresCritExts) throws CertPathValidatorException {
        checkPolicy((X509Certificate) cert);
        if (unresCritExts != null && !unresCritExts.isEmpty()) {
            unresCritExts.remove(PKIXExtensions.CertificatePolicies_Id.toString());
            unresCritExts.remove(PKIXExtensions.PolicyMappings_Id.toString());
            unresCritExts.remove(PKIXExtensions.PolicyConstraints_Id.toString());
            unresCritExts.remove(PKIXExtensions.InhibitAnyPolicy_Id.toString());
        }
    }

    private void checkPolicy(X509Certificate currCert) throws CertPathValidatorException {
        if (debug != null) {
            debug.println("PolicyChecker.checkPolicy() ---checking " + "certificate policies" + "...");
            debug.println("PolicyChecker.checkPolicy() certIndex = " + this.certIndex);
            debug.println("PolicyChecker.checkPolicy() BEFORE PROCESSING: explicitPolicy = " + this.explicitPolicy);
            debug.println("PolicyChecker.checkPolicy() BEFORE PROCESSING: policyMapping = " + this.policyMapping);
            debug.println("PolicyChecker.checkPolicy() BEFORE PROCESSING: inhibitAnyPolicy = " + this.inhibitAnyPolicy);
            debug.println("PolicyChecker.checkPolicy() BEFORE PROCESSING: policyTree = " + this.rootNode);
        }
        try {
            X509CertImpl currCertImpl = X509CertImpl.toImpl(currCert);
            boolean finalCert = this.certIndex == this.certPathLen;
            this.rootNode = processPolicies(this.certIndex, this.initPolicies, this.explicitPolicy, this.policyMapping, this.inhibitAnyPolicy, this.rejectPolicyQualifiers, this.rootNode, currCertImpl, finalCert);
            if (!finalCert) {
                this.explicitPolicy = mergeExplicitPolicy(this.explicitPolicy, currCertImpl, finalCert);
                this.policyMapping = mergePolicyMapping(this.policyMapping, currCertImpl);
                this.inhibitAnyPolicy = mergeInhibitAnyPolicy(this.inhibitAnyPolicy, currCertImpl);
            }
            this.certIndex++;
            if (debug != null) {
                debug.println("PolicyChecker.checkPolicy() AFTER PROCESSING: explicitPolicy = " + this.explicitPolicy);
                debug.println("PolicyChecker.checkPolicy() AFTER PROCESSING: policyMapping = " + this.policyMapping);
                debug.println("PolicyChecker.checkPolicy() AFTER PROCESSING: inhibitAnyPolicy = " + this.inhibitAnyPolicy);
                debug.println("PolicyChecker.checkPolicy() AFTER PROCESSING: policyTree = " + this.rootNode);
                debug.println("PolicyChecker.checkPolicy() " + "certificate policies" + " verified");
            }
        } catch (CertificateException ce) {
            throw new CertPathValidatorException((Throwable) ce);
        }
    }

    static int mergeExplicitPolicy(int explicitPolicy2, X509CertImpl currCert, boolean finalCert) throws CertPathValidatorException {
        if (explicitPolicy2 > 0 && !X509CertImpl.isSelfIssued(currCert)) {
            explicitPolicy2--;
        }
        try {
            PolicyConstraintsExtension polConstExt = currCert.getPolicyConstraintsExtension();
            if (polConstExt == null) {
                return explicitPolicy2;
            }
            int require = polConstExt.get(PolicyConstraintsExtension.REQUIRE).intValue();
            if (debug != null) {
                debug.println("PolicyChecker.mergeExplicitPolicy() require Index from cert = " + require);
            }
            if (!finalCert) {
                if (require != -1 && (explicitPolicy2 == -1 || require < explicitPolicy2)) {
                    explicitPolicy2 = require;
                }
            } else if (require == 0) {
                explicitPolicy2 = require;
            }
            return explicitPolicy2;
        } catch (IOException e) {
            if (debug != null) {
                debug.println("PolicyChecker.mergeExplicitPolicy unexpected exception");
                e.printStackTrace();
            }
            throw new CertPathValidatorException((Throwable) e);
        }
    }

    static int mergePolicyMapping(int policyMapping2, X509CertImpl currCert) throws CertPathValidatorException {
        if (policyMapping2 > 0 && !X509CertImpl.isSelfIssued(currCert)) {
            policyMapping2--;
        }
        try {
            PolicyConstraintsExtension polConstExt = currCert.getPolicyConstraintsExtension();
            if (polConstExt == null) {
                return policyMapping2;
            }
            int inhibit = polConstExt.get(PolicyConstraintsExtension.INHIBIT).intValue();
            if (debug != null) {
                debug.println("PolicyChecker.mergePolicyMapping() inhibit Index from cert = " + inhibit);
            }
            if (inhibit != -1 && (policyMapping2 == -1 || inhibit < policyMapping2)) {
                policyMapping2 = inhibit;
            }
            return policyMapping2;
        } catch (IOException e) {
            if (debug != null) {
                debug.println("PolicyChecker.mergePolicyMapping unexpected exception");
                e.printStackTrace();
            }
            throw new CertPathValidatorException((Throwable) e);
        }
    }

    static int mergeInhibitAnyPolicy(int inhibitAnyPolicy2, X509CertImpl currCert) throws CertPathValidatorException {
        if (inhibitAnyPolicy2 > 0 && !X509CertImpl.isSelfIssued(currCert)) {
            inhibitAnyPolicy2--;
        }
        try {
            InhibitAnyPolicyExtension inhAnyPolExt = (InhibitAnyPolicyExtension) currCert.getExtension(PKIXExtensions.InhibitAnyPolicy_Id);
            if (inhAnyPolExt == null) {
                return inhibitAnyPolicy2;
            }
            int skipCerts = inhAnyPolExt.get(InhibitAnyPolicyExtension.SKIP_CERTS).intValue();
            if (debug != null) {
                debug.println("PolicyChecker.mergeInhibitAnyPolicy() skipCerts Index from cert = " + skipCerts);
            }
            if (skipCerts != -1 && skipCerts < inhibitAnyPolicy2) {
                inhibitAnyPolicy2 = skipCerts;
            }
            return inhibitAnyPolicy2;
        } catch (IOException e) {
            if (debug != null) {
                debug.println("PolicyChecker.mergeInhibitAnyPolicy unexpected exception");
                e.printStackTrace();
            }
            throw new CertPathValidatorException((Throwable) e);
        }
    }

    static PolicyNodeImpl processPolicies(int certIndex2, Set<String> initPolicies2, int explicitPolicy2, int policyMapping2, int inhibitAnyPolicy2, boolean rejectPolicyQualifiers2, PolicyNodeImpl origRootNode, X509CertImpl currCert, boolean finalCert) throws CertPathValidatorException {
        PolicyNodeImpl rootNode2;
        Set<PolicyQualifierInfo> anyQuals;
        int explicitPolicy3;
        Iterator<PolicyInformation> it;
        int i = certIndex2;
        Set<String> set = initPolicies2;
        boolean z = rejectPolicyQualifiers2;
        boolean z2 = finalCert;
        boolean policiesCritical = false;
        Set<PolicyQualifierInfo> anyQuals2 = new HashSet<>();
        if (origRootNode == null) {
            rootNode2 = null;
        } else {
            rootNode2 = origRootNode.copyTree();
        }
        PolicyNodeImpl rootNode3 = rootNode2;
        CertificatePoliciesExtension currCertPolicies = currCert.getCertificatePoliciesExtension();
        if (currCertPolicies == null || rootNode3 == null) {
            if (currCertPolicies == null) {
                if (debug != null) {
                    debug.println("PolicyChecker.processPolicies() no policies present in cert");
                }
                rootNode3 = null;
            }
            anyQuals = anyQuals2;
        } else {
            boolean policiesCritical2 = currCertPolicies.isCritical();
            if (debug != null) {
                debug.println("PolicyChecker.processPolicies() policiesCritical = " + policiesCritical2);
            }
            try {
                List<PolicyInformation> policyInfo = currCertPolicies.get(CertificatePoliciesExtension.POLICIES);
                if (debug != null) {
                    debug.println("PolicyChecker.processPolicies() rejectPolicyQualifiers = " + z);
                }
                Iterator<PolicyInformation> it2 = policyInfo.iterator();
                boolean foundAnyPolicy = false;
                anyQuals = anyQuals2;
                while (it2.hasNext()) {
                    PolicyInformation curPolInfo = it2.next();
                    String curPolicy = curPolInfo.getPolicyIdentifier().getIdentifier().toString();
                    if (curPolicy.equals(ANY_POLICY)) {
                        anyQuals = curPolInfo.getPolicyQualifiers();
                        foundAnyPolicy = true;
                        it = it2;
                    } else {
                        if (debug != null) {
                            debug.println("PolicyChecker.processPolicies() processing policy: " + curPolicy);
                        }
                        Set<PolicyQualifierInfo> pQuals = curPolInfo.getPolicyQualifiers();
                        if (pQuals.isEmpty() || !z || !policiesCritical2) {
                            Set<PolicyQualifierInfo> pQuals2 = pQuals;
                            String curPolicy2 = curPolicy;
                            PolicyInformation policyInformation = curPolInfo;
                            it = it2;
                            if (!processParents(i, policiesCritical2, z, rootNode3, curPolicy, pQuals2, false)) {
                                processParents(i, policiesCritical2, z, rootNode3, curPolicy2, pQuals2, true);
                            }
                        } else {
                            CertPathValidatorException certPathValidatorException = new CertPathValidatorException("critical policy qualifiers present in certificate", null, null, -1, PKIXReason.INVALID_POLICY);
                            throw certPathValidatorException;
                        }
                    }
                    it2 = it;
                }
                if (foundAnyPolicy && (inhibitAnyPolicy2 > 0 || (!z2 && X509CertImpl.isSelfIssued(currCert)))) {
                    if (debug != null) {
                        debug.println("PolicyChecker.processPolicies() processing policy: 2.5.29.32.0");
                    }
                    processParents(i, policiesCritical2, z, rootNode3, ANY_POLICY, anyQuals, true);
                }
                rootNode3.prune(i);
                if (!rootNode3.getChildren().hasNext()) {
                    rootNode3 = null;
                }
                policiesCritical = policiesCritical2;
            } catch (IOException ioe) {
                throw new CertPathValidatorException("Exception while retrieving policyOIDs", ioe);
            }
        }
        if (rootNode3 != null && !z2) {
            rootNode3 = processPolicyMappings(currCert, i, policyMapping2, rootNode3, policiesCritical, anyQuals);
        }
        if (!(rootNode3 == null || set.contains(ANY_POLICY) || currCertPolicies == null)) {
            rootNode3 = removeInvalidNodes(rootNode3, i, set, currCertPolicies);
            if (rootNode3 != null && z2) {
                rootNode3 = rewriteLeafNodes(i, set, rootNode3);
            }
        }
        if (z2) {
            explicitPolicy3 = mergeExplicitPolicy(explicitPolicy2, currCert, z2);
        } else {
            explicitPolicy3 = explicitPolicy2;
            X509CertImpl x509CertImpl = currCert;
        }
        if (explicitPolicy3 != 0 || rootNode3 != null) {
            return rootNode3;
        }
        CertPathValidatorException certPathValidatorException2 = new CertPathValidatorException("non-null policy tree required and policy tree is null", null, null, -1, PKIXReason.INVALID_POLICY);
        throw certPathValidatorException2;
    }

    private static PolicyNodeImpl rewriteLeafNodes(int certIndex2, Set<String> initPolicies2, PolicyNodeImpl rootNode2) {
        int i = certIndex2;
        PolicyNodeImpl rootNode3 = rootNode2;
        Set<PolicyNodeImpl> anyNodes = rootNode3.getPolicyNodesValid(i, ANY_POLICY);
        if (anyNodes.isEmpty()) {
            return rootNode3;
        }
        PolicyNodeImpl anyNode = anyNodes.iterator().next();
        PolicyNodeImpl parentNode = (PolicyNodeImpl) anyNode.getParent();
        parentNode.deleteChild(anyNode);
        Set<String> initial = new HashSet<>((Collection<? extends String>) initPolicies2);
        for (PolicyNodeImpl node : rootNode3.getPolicyNodes(i)) {
            initial.remove(node.getValidPolicy());
        }
        if (initial.isEmpty()) {
            rootNode3.prune(i);
            if (!rootNode2.getChildren().hasNext()) {
                rootNode3 = null;
            }
        } else {
            boolean anyCritical = anyNode.isCritical();
            Set<PolicyQualifierInfo> anyQualifiers = anyNode.getPolicyQualifiers();
            for (Iterator<String> it = initial.iterator(); it.hasNext(); it = it) {
                String policy = it.next();
                String str = policy;
                new PolicyNodeImpl(parentNode, policy, anyQualifiers, anyCritical, Collections.singleton(policy), false);
            }
        }
        return rootNode3;
    }

    private static boolean processParents(int certIndex2, boolean policiesCritical, boolean rejectPolicyQualifiers2, PolicyNodeImpl rootNode2, String curPolicy, Set<PolicyQualifierInfo> pQuals, boolean matchAny) throws CertPathValidatorException {
        Set<String> curExpPols;
        Set<String> parExpPols;
        String str = curPolicy;
        boolean z = matchAny;
        boolean foundMatch = false;
        if (debug != null) {
            debug.println("PolicyChecker.processParents(): matchAny = " + z);
        }
        for (PolicyNodeImpl curParent : rootNode2.getPolicyNodesExpected(certIndex2 - 1, str, z)) {
            if (debug != null) {
                debug.println("PolicyChecker.processParents() found parent:\n" + curParent.asString());
            }
            String validPolicy = curParent.getValidPolicy();
            Set<String> curExpPols2 = null;
            if (str.equals(ANY_POLICY)) {
                Set<String> parExpPols2 = curParent.getExpectedPolicies();
                for (String curParExpPol : parExpPols2) {
                    Iterator<PolicyNodeImpl> childIter = curParent.getChildren();
                    while (true) {
                        if (!childIter.hasNext()) {
                            curExpPols = curExpPols2;
                            parExpPols = parExpPols2;
                            Set<String> expPols = new HashSet<>();
                            expPols.add(curParExpPol);
                            PolicyNodeImpl policyNodeImpl = new PolicyNodeImpl(curParent, curParExpPol, pQuals, policiesCritical, expPols, false);
                            PolicyNodeImpl curNode = policyNodeImpl;
                            break;
                        }
                        String childPolicy = childIter.next().getValidPolicy();
                        if (!curParExpPol.equals(childPolicy)) {
                            Set<String> set = parExpPols2;
                        } else if (debug != null) {
                            Debug debug2 = debug;
                            curExpPols = curExpPols2;
                            StringBuilder sb = new StringBuilder();
                            sb.append(childPolicy);
                            parExpPols = parExpPols2;
                            sb.append(" in parent's expected policy set already appears in child node");
                            debug2.println(sb.toString());
                        } else {
                            curExpPols = curExpPols2;
                            parExpPols = parExpPols2;
                        }
                    }
                    curExpPols2 = curExpPols;
                    parExpPols2 = parExpPols;
                }
                Set<String> set2 = curExpPols2;
            } else {
                Set<String> curExpPols3 = new HashSet<>();
                curExpPols3.add(str);
                PolicyNodeImpl policyNodeImpl2 = curParent;
                new PolicyNodeImpl(curParent, str, pQuals, policiesCritical, curExpPols3, false);
            }
            foundMatch = true;
        }
        return foundMatch;
    }

    private static PolicyNodeImpl processPolicyMappings(X509CertImpl currCert, int certIndex2, int policyMapping2, PolicyNodeImpl rootNode2, boolean policiesCritical, Set<PolicyQualifierInfo> anyQuals) throws CertPathValidatorException {
        PolicyNodeImpl rootNode3;
        List<CertificatePolicyMap> maps;
        List<CertificatePolicyMap> maps2;
        List<CertificatePolicyMap> maps3;
        int i = certIndex2;
        int i2 = policyMapping2;
        PolicyNodeImpl policyNodeImpl = rootNode2;
        PolicyMappingsExtension polMappingsExt = currCert.getPolicyMappingsExtension();
        if (polMappingsExt == null) {
            return policyNodeImpl;
        }
        if (debug != null) {
            debug.println("PolicyChecker.processPolicyMappings() inside policyMapping check");
        }
        try {
            List<CertificatePolicyMap> maps4 = polMappingsExt.get(PolicyMappingsExtension.MAP);
            boolean childDeleted = false;
            for (CertificatePolicyMap polMap : maps4) {
                String issuerDomain = polMap.getIssuerIdentifier().getIdentifier().toString();
                String subjectDomain = polMap.getSubjectIdentifier().getIdentifier().toString();
                if (debug != null) {
                    debug.println("PolicyChecker.processPolicyMappings() issuerDomain = " + issuerDomain);
                    debug.println("PolicyChecker.processPolicyMappings() subjectDomain = " + subjectDomain);
                }
                if (issuerDomain.equals(ANY_POLICY)) {
                    String str = subjectDomain;
                    CertPathValidatorException certPathValidatorException = new CertPathValidatorException("encountered an issuerDomainPolicy of ANY_POLICY", null, null, -1, PKIXReason.INVALID_POLICY);
                    throw certPathValidatorException;
                } else if (!subjectDomain.equals(ANY_POLICY)) {
                    Set<PolicyNodeImpl> validNodes = policyNodeImpl.getPolicyNodesValid(i, issuerDomain);
                    int i3 = -1;
                    if (!validNodes.isEmpty()) {
                        for (PolicyNodeImpl curNode : validNodes) {
                            if (i2 > 0) {
                                maps3 = maps4;
                            } else if (i2 == i3) {
                                maps3 = maps4;
                            } else {
                                if (i2 == 0) {
                                    PolicyNodeImpl parentNode = (PolicyNodeImpl) curNode.getParent();
                                    if (debug != null) {
                                        Debug debug2 = debug;
                                        StringBuilder sb = new StringBuilder();
                                        maps2 = maps4;
                                        sb.append("PolicyChecker.processPolicyMappings() before deleting: policy tree = ");
                                        sb.append((Object) policyNodeImpl);
                                        debug2.println(sb.toString());
                                    } else {
                                        maps2 = maps4;
                                    }
                                    parentNode.deleteChild(curNode);
                                    if (debug != null) {
                                        debug.println("PolicyChecker.processPolicyMappings() after deleting: policy tree = " + policyNodeImpl);
                                    }
                                    childDeleted = true;
                                } else {
                                    maps2 = maps4;
                                }
                                maps4 = maps2;
                                i3 = -1;
                            }
                            curNode.addExpectedPolicy(subjectDomain);
                            maps4 = maps2;
                            i3 = -1;
                        }
                        maps = maps4;
                    } else {
                        maps = maps4;
                        if (i2 > 0 || i2 == -1) {
                            Iterator<PolicyNodeImpl> it = policyNodeImpl.getPolicyNodesValid(i, ANY_POLICY).iterator();
                            while (it.hasNext()) {
                                PolicyNodeImpl curAnyNode = it.next();
                                Set<PolicyNodeImpl> expPols = new HashSet<>();
                                expPols.add(subjectDomain);
                                PolicyNodeImpl policyNodeImpl2 = curAnyNode;
                                new PolicyNodeImpl((PolicyNodeImpl) curAnyNode.getParent(), issuerDomain, anyQuals, policiesCritical, expPols, true);
                                it = it;
                                validNodes = validNodes;
                                subjectDomain = subjectDomain;
                            }
                        }
                    }
                    maps4 = maps;
                } else {
                    String str2 = subjectDomain;
                    CertPathValidatorException certPathValidatorException2 = new CertPathValidatorException("encountered a subjectDomainPolicy of ANY_POLICY", null, null, -1, PKIXReason.INVALID_POLICY);
                    throw certPathValidatorException2;
                }
            }
            if (childDeleted) {
                policyNodeImpl.prune(i);
                if (!rootNode2.getChildren().hasNext()) {
                    if (debug != null) {
                        debug.println("setting rootNode to null");
                    }
                    rootNode3 = null;
                    return rootNode3;
                }
            }
            rootNode3 = policyNodeImpl;
            return rootNode3;
        } catch (IOException e) {
            if (debug != null) {
                debug.println("PolicyChecker.processPolicyMappings() mapping exception");
                e.printStackTrace();
            }
            throw new CertPathValidatorException("Exception while checking mapping", e);
        }
    }

    private static PolicyNodeImpl removeInvalidNodes(PolicyNodeImpl rootNode2, int certIndex2, Set<String> initPolicies2, CertificatePoliciesExtension currCertPolicies) throws CertPathValidatorException {
        try {
            boolean childDeleted = false;
            for (PolicyInformation curPolInfo : currCertPolicies.get(CertificatePoliciesExtension.POLICIES)) {
                String curPolicy = curPolInfo.getPolicyIdentifier().getIdentifier().toString();
                if (debug != null) {
                    Debug debug2 = debug;
                    debug2.println("PolicyChecker.processPolicies() processing policy second time: " + curPolicy);
                }
                for (PolicyNodeImpl curNode : rootNode2.getPolicyNodesValid(certIndex2, curPolicy)) {
                    PolicyNodeImpl parentNode = (PolicyNodeImpl) curNode.getParent();
                    if (parentNode.getValidPolicy().equals(ANY_POLICY) && !initPolicies2.contains(curPolicy) && !curPolicy.equals(ANY_POLICY)) {
                        if (debug != null) {
                            Debug debug3 = debug;
                            debug3.println("PolicyChecker.processPolicies() before deleting: policy tree = " + rootNode2);
                        }
                        parentNode.deleteChild(curNode);
                        childDeleted = true;
                        if (debug != null) {
                            Debug debug4 = debug;
                            debug4.println("PolicyChecker.processPolicies() after deleting: policy tree = " + rootNode2);
                        }
                    }
                }
            }
            if (!childDeleted) {
                return rootNode2;
            }
            rootNode2.prune(certIndex2);
            if (!rootNode2.getChildren().hasNext()) {
                return null;
            }
            return rootNode2;
        } catch (IOException ioe) {
            throw new CertPathValidatorException("Exception while retrieving policyOIDs", ioe);
        }
    }

    /* access modifiers changed from: package-private */
    public PolicyNode getPolicyTree() {
        if (this.rootNode == null) {
            return null;
        }
        PolicyNodeImpl policyTree = this.rootNode.copyTree();
        policyTree.setImmutable();
        return policyTree;
    }
}
