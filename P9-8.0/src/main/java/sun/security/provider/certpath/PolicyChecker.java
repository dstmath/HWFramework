package sun.security.provider.certpath;

import java.io.IOException;
import java.security.cert.CertPathValidatorException;
import java.security.cert.Certificate;
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

    PolicyChecker(Set<String> initialPolicies, int certPathLen, boolean expPolicyRequired, boolean polMappingInhibited, boolean anyPolicyInhibited, boolean rejectPolicyQualifiers, PolicyNodeImpl rootNode) {
        if (initialPolicies.isEmpty()) {
            this.initPolicies = new HashSet(1);
            this.initPolicies.-java_util_stream_Collectors-mthref-4(ANY_POLICY);
        } else {
            this.initPolicies = new HashSet((Collection) initialPolicies);
        }
        this.certPathLen = certPathLen;
        this.expPolicyRequired = expPolicyRequired;
        this.polMappingInhibited = polMappingInhibited;
        this.anyPolicyInhibited = anyPolicyInhibited;
        this.rejectPolicyQualifiers = rejectPolicyQualifiers;
        this.rootNode = rootNode;
    }

    public void init(boolean forward) throws CertPathValidatorException {
        int i = 0;
        if (forward) {
            throw new CertPathValidatorException("forward checking not supported");
        }
        this.certIndex = 1;
        this.explicitPolicy = this.expPolicyRequired ? 0 : this.certPathLen + 1;
        this.policyMapping = this.polMappingInhibited ? 0 : this.certPathLen + 1;
        if (!this.anyPolicyInhibited) {
            i = this.certPathLen + 1;
        }
        this.inhibitAnyPolicy = i;
    }

    public boolean isForwardCheckingSupported() {
        return false;
    }

    public Set<String> getSupportedExtensions() {
        if (this.supportedExts == null) {
            this.supportedExts = new HashSet(4);
            this.supportedExts.-java_util_stream_Collectors-mthref-4(PKIXExtensions.CertificatePolicies_Id.toString());
            this.supportedExts.-java_util_stream_Collectors-mthref-4(PKIXExtensions.PolicyMappings_Id.toString());
            this.supportedExts.-java_util_stream_Collectors-mthref-4(PKIXExtensions.PolicyConstraints_Id.toString());
            this.supportedExts.-java_util_stream_Collectors-mthref-4(PKIXExtensions.InhibitAnyPolicy_Id.toString());
            this.supportedExts = Collections.unmodifiableSet(this.supportedExts);
        }
        return this.supportedExts;
    }

    public void check(Certificate cert, Collection<String> unresCritExts) throws CertPathValidatorException {
        checkPolicy((X509Certificate) cert);
        if (unresCritExts != null && (unresCritExts.isEmpty() ^ 1) != 0) {
            unresCritExts.remove(PKIXExtensions.CertificatePolicies_Id.toString());
            unresCritExts.remove(PKIXExtensions.PolicyMappings_Id.toString());
            unresCritExts.remove(PKIXExtensions.PolicyConstraints_Id.toString());
            unresCritExts.remove(PKIXExtensions.InhibitAnyPolicy_Id.toString());
        }
    }

    private void checkPolicy(X509Certificate currCert) throws CertPathValidatorException {
        String msg = "certificate policies";
        if (debug != null) {
            debug.println("PolicyChecker.checkPolicy() ---checking " + msg + "...");
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
                debug.println("PolicyChecker.checkPolicy() " + msg + " verified");
            }
        } catch (Throwable ce) {
            throw new CertPathValidatorException(ce);
        }
    }

    static int mergeExplicitPolicy(int explicitPolicy, X509CertImpl currCert, boolean finalCert) throws CertPathValidatorException {
        if (explicitPolicy > 0 && (X509CertImpl.isSelfIssued(currCert) ^ 1) != 0) {
            explicitPolicy--;
        }
        try {
            PolicyConstraintsExtension polConstExt = currCert.getPolicyConstraintsExtension();
            if (polConstExt == null) {
                return explicitPolicy;
            }
            int require = polConstExt.get(PolicyConstraintsExtension.REQUIRE).lambda$-java_util_stream_IntPipeline_14709();
            if (debug != null) {
                debug.println("PolicyChecker.mergeExplicitPolicy() require Index from cert = " + require);
            }
            if (finalCert) {
                if (require == 0) {
                    explicitPolicy = require;
                }
            } else if (require != -1 && (explicitPolicy == -1 || require < explicitPolicy)) {
                explicitPolicy = require;
            }
            return explicitPolicy;
        } catch (Throwable e) {
            if (debug != null) {
                debug.println("PolicyChecker.mergeExplicitPolicy unexpected exception");
                e.printStackTrace();
            }
            throw new CertPathValidatorException(e);
        }
    }

    static int mergePolicyMapping(int policyMapping, X509CertImpl currCert) throws CertPathValidatorException {
        if (policyMapping > 0 && (X509CertImpl.isSelfIssued(currCert) ^ 1) != 0) {
            policyMapping--;
        }
        try {
            PolicyConstraintsExtension polConstExt = currCert.getPolicyConstraintsExtension();
            if (polConstExt == null) {
                return policyMapping;
            }
            int inhibit = polConstExt.get(PolicyConstraintsExtension.INHIBIT).lambda$-java_util_stream_IntPipeline_14709();
            if (debug != null) {
                debug.println("PolicyChecker.mergePolicyMapping() inhibit Index from cert = " + inhibit);
            }
            if (inhibit != -1 && (policyMapping == -1 || inhibit < policyMapping)) {
                policyMapping = inhibit;
            }
            return policyMapping;
        } catch (Throwable e) {
            if (debug != null) {
                debug.println("PolicyChecker.mergePolicyMapping unexpected exception");
                e.printStackTrace();
            }
            throw new CertPathValidatorException(e);
        }
    }

    static int mergeInhibitAnyPolicy(int inhibitAnyPolicy, X509CertImpl currCert) throws CertPathValidatorException {
        if (inhibitAnyPolicy > 0 && (X509CertImpl.isSelfIssued(currCert) ^ 1) != 0) {
            inhibitAnyPolicy--;
        }
        try {
            InhibitAnyPolicyExtension inhAnyPolExt = (InhibitAnyPolicyExtension) currCert.getExtension(PKIXExtensions.InhibitAnyPolicy_Id);
            if (inhAnyPolExt == null) {
                return inhibitAnyPolicy;
            }
            int skipCerts = inhAnyPolExt.get(InhibitAnyPolicyExtension.SKIP_CERTS).lambda$-java_util_stream_IntPipeline_14709();
            if (debug != null) {
                debug.println("PolicyChecker.mergeInhibitAnyPolicy() skipCerts Index from cert = " + skipCerts);
            }
            if (skipCerts != -1 && skipCerts < inhibitAnyPolicy) {
                inhibitAnyPolicy = skipCerts;
            }
            return inhibitAnyPolicy;
        } catch (Throwable e) {
            if (debug != null) {
                debug.println("PolicyChecker.mergeInhibitAnyPolicy unexpected exception");
                e.printStackTrace();
            }
            throw new CertPathValidatorException(e);
        }
    }

    static PolicyNodeImpl processPolicies(int certIndex, Set<String> initPolicies, int explicitPolicy, int policyMapping, int inhibitAnyPolicy, boolean rejectPolicyQualifiers, PolicyNodeImpl origRootNode, X509CertImpl currCert, boolean finalCert) throws CertPathValidatorException {
        PolicyNodeImpl rootNode;
        boolean policiesCritical = false;
        Set<PolicyQualifierInfo> anyQuals = new HashSet();
        if (origRootNode == null) {
            rootNode = null;
        } else {
            rootNode = origRootNode.copyTree();
        }
        CertificatePoliciesExtension currCertPolicies = currCert.getCertificatePoliciesExtension();
        if (currCertPolicies != null && rootNode != null) {
            policiesCritical = currCertPolicies.isCritical();
            if (debug != null) {
                debug.println("PolicyChecker.processPolicies() policiesCritical = " + policiesCritical);
            }
            try {
                List<PolicyInformation> policyInfo = currCertPolicies.get(CertificatePoliciesExtension.POLICIES);
                if (debug != null) {
                    debug.println("PolicyChecker.processPolicies() rejectPolicyQualifiers = " + rejectPolicyQualifiers);
                }
                boolean foundAnyPolicy = false;
                for (PolicyInformation curPolInfo : policyInfo) {
                    String curPolicy = curPolInfo.getPolicyIdentifier().getIdentifier().toString();
                    if (curPolicy.equals(ANY_POLICY)) {
                        foundAnyPolicy = true;
                        anyQuals = curPolInfo.getPolicyQualifiers();
                    } else {
                        if (debug != null) {
                            debug.println("PolicyChecker.processPolicies() processing policy: " + curPolicy);
                        }
                        Set<PolicyQualifierInfo> pQuals = curPolInfo.getPolicyQualifiers();
                        if (!pQuals.isEmpty() && rejectPolicyQualifiers && policiesCritical) {
                            throw new CertPathValidatorException("critical policy qualifiers present in certificate", null, null, -1, PKIXReason.INVALID_POLICY);
                        } else if (!processParents(certIndex, policiesCritical, rejectPolicyQualifiers, rootNode, curPolicy, pQuals, false)) {
                            processParents(certIndex, policiesCritical, rejectPolicyQualifiers, rootNode, curPolicy, pQuals, true);
                        }
                    }
                }
                if (foundAnyPolicy && (inhibitAnyPolicy > 0 || (!finalCert && X509CertImpl.isSelfIssued(currCert)))) {
                    if (debug != null) {
                        debug.println("PolicyChecker.processPolicies() processing policy: 2.5.29.32.0");
                    }
                    processParents(certIndex, policiesCritical, rejectPolicyQualifiers, rootNode, ANY_POLICY, anyQuals, true);
                }
                rootNode.prune(certIndex);
                if (!rootNode.getChildren().hasNext()) {
                    rootNode = null;
                }
            } catch (Throwable ioe) {
                throw new CertPathValidatorException("Exception while retrieving policyOIDs", ioe);
            }
        } else if (currCertPolicies == null) {
            if (debug != null) {
                debug.println("PolicyChecker.processPolicies() no policies present in cert");
            }
            rootNode = null;
        }
        if (!(rootNode == null || finalCert)) {
            rootNode = processPolicyMappings(currCert, certIndex, policyMapping, rootNode, policiesCritical, anyQuals);
        }
        if (rootNode != null) {
            if (!((initPolicies.contains(ANY_POLICY) ^ 1) == 0 || currCertPolicies == null)) {
                rootNode = removeInvalidNodes(rootNode, certIndex, initPolicies, currCertPolicies);
                if (rootNode != null && finalCert) {
                    rootNode = rewriteLeafNodes(certIndex, initPolicies, rootNode);
                }
            }
        }
        if (finalCert) {
            explicitPolicy = mergeExplicitPolicy(explicitPolicy, currCert, finalCert);
        }
        if (explicitPolicy != 0 || rootNode != null) {
            return rootNode;
        }
        throw new CertPathValidatorException("non-null policy tree required and policy tree is null", null, null, -1, PKIXReason.INVALID_POLICY);
    }

    private static PolicyNodeImpl rewriteLeafNodes(int certIndex, Set<String> initPolicies, PolicyNodeImpl rootNode) {
        Set<PolicyNodeImpl> anyNodes = rootNode.getPolicyNodesValid(certIndex, ANY_POLICY);
        if (anyNodes.isEmpty()) {
            return rootNode;
        }
        PolicyNodeImpl node;
        PolicyNodeImpl anyNode = (PolicyNodeImpl) anyNodes.iterator().next();
        PolicyNodeImpl parentNode = (PolicyNodeImpl) anyNode.getParent();
        parentNode.deleteChild(anyNode);
        Set<String> initial = new HashSet((Collection) initPolicies);
        for (PolicyNodeImpl node2 : rootNode.getPolicyNodes(certIndex)) {
            initial.remove(node2.getValidPolicy());
        }
        if (initial.isEmpty()) {
            rootNode.prune(certIndex);
            if (!rootNode.getChildren().hasNext()) {
                rootNode = null;
            }
        } else {
            boolean anyCritical = anyNode.isCritical();
            Set<PolicyQualifierInfo> anyQualifiers = anyNode.getPolicyQualifiers();
            for (String policy : initial) {
                node2 = new PolicyNodeImpl(parentNode, policy, anyQualifiers, anyCritical, Collections.singleton(policy), false);
            }
        }
        return rootNode;
    }

    private static boolean processParents(int certIndex, boolean policiesCritical, boolean rejectPolicyQualifiers, PolicyNodeImpl rootNode, String curPolicy, Set<PolicyQualifierInfo> pQuals, boolean matchAny) throws CertPathValidatorException {
        boolean foundMatch = false;
        if (debug != null) {
            debug.println("PolicyChecker.processParents(): matchAny = " + matchAny);
        }
        for (PolicyNodeImpl curParent : rootNode.getPolicyNodesExpected(certIndex - 1, curPolicy, matchAny)) {
            if (debug != null) {
                debug.println("PolicyChecker.processParents() found parent:\n" + curParent.asString());
            }
            foundMatch = true;
            String curParPolicy = curParent.getValidPolicy();
            if (curPolicy.equals(ANY_POLICY)) {
                for (String curParExpPol : curParent.getExpectedPolicies()) {
                    Iterator<PolicyNodeImpl> childIter = curParent.getChildren();
                    while (childIter.hasNext()) {
                        String childPolicy = ((PolicyNodeImpl) childIter.next()).getValidPolicy();
                        if (curParExpPol.equals(childPolicy)) {
                            if (debug != null) {
                                debug.println(childPolicy + " in parent's " + "expected policy set already appears in " + "child node");
                            }
                        }
                    }
                    Set<String> expPols = new HashSet();
                    expPols.-java_util_stream_Collectors-mthref-4(curParExpPol);
                    PolicyNodeImpl curNode = new PolicyNodeImpl(curParent, curParExpPol, pQuals, policiesCritical, expPols, false);
                }
            } else {
                Set<String> curExpPols = new HashSet();
                curExpPols.-java_util_stream_Collectors-mthref-4(curPolicy);
                PolicyNodeImpl policyNodeImpl = new PolicyNodeImpl(curParent, curPolicy, pQuals, policiesCritical, curExpPols, false);
            }
        }
        return foundMatch;
    }

    private static PolicyNodeImpl processPolicyMappings(X509CertImpl currCert, int certIndex, int policyMapping, PolicyNodeImpl rootNode, boolean policiesCritical, Set<PolicyQualifierInfo> anyQuals) throws CertPathValidatorException {
        PolicyMappingsExtension polMappingsExt = currCert.getPolicyMappingsExtension();
        if (polMappingsExt == null) {
            return rootNode;
        }
        if (debug != null) {
            debug.println("PolicyChecker.processPolicyMappings() inside policyMapping check");
        }
        try {
            boolean childDeleted = false;
            for (CertificatePolicyMap polMap : polMappingsExt.get(PolicyMappingsExtension.MAP)) {
                String issuerDomain = polMap.getIssuerIdentifier().getIdentifier().toString();
                String subjectDomain = polMap.getSubjectIdentifier().getIdentifier().toString();
                if (debug != null) {
                    debug.println("PolicyChecker.processPolicyMappings() issuerDomain = " + issuerDomain);
                    debug.println("PolicyChecker.processPolicyMappings() subjectDomain = " + subjectDomain);
                }
                if (issuerDomain.equals(ANY_POLICY)) {
                    throw new CertPathValidatorException("encountered an issuerDomainPolicy of ANY_POLICY", null, null, -1, PKIXReason.INVALID_POLICY);
                }
                if (subjectDomain.equals(ANY_POLICY)) {
                    throw new CertPathValidatorException("encountered a subjectDomainPolicy of ANY_POLICY", null, null, -1, PKIXReason.INVALID_POLICY);
                }
                Set<PolicyNodeImpl> validNodes = rootNode.getPolicyNodesValid(certIndex, issuerDomain);
                PolicyNodeImpl curNode;
                if (!validNodes.isEmpty()) {
                    for (PolicyNodeImpl curNode2 : validNodes) {
                        if (policyMapping > 0 || policyMapping == -1) {
                            curNode2.addExpectedPolicy(subjectDomain);
                        } else if (policyMapping == 0) {
                            PolicyNodeImpl parentNode = (PolicyNodeImpl) curNode2.getParent();
                            if (debug != null) {
                                debug.println("PolicyChecker.processPolicyMappings() before deleting: policy tree = " + rootNode);
                            }
                            parentNode.deleteChild(curNode2);
                            childDeleted = true;
                            if (debug != null) {
                                debug.println("PolicyChecker.processPolicyMappings() after deleting: policy tree = " + rootNode);
                            }
                        }
                    }
                } else if (policyMapping > 0 || policyMapping == -1) {
                    for (PolicyNodeImpl curAnyNode : rootNode.getPolicyNodesValid(certIndex, ANY_POLICY)) {
                        PolicyNodeImpl curAnyNodeParent = (PolicyNodeImpl) curAnyNode.getParent();
                        Set<String> expPols = new HashSet();
                        expPols.-java_util_stream_Collectors-mthref-4(subjectDomain);
                        curNode2 = new PolicyNodeImpl(curAnyNodeParent, issuerDomain, anyQuals, policiesCritical, expPols, true);
                    }
                }
            }
            if (childDeleted) {
                rootNode.prune(certIndex);
                if (!rootNode.getChildren().hasNext()) {
                    if (debug != null) {
                        debug.println("setting rootNode to null");
                    }
                    rootNode = null;
                }
            }
            return rootNode;
        } catch (IOException e) {
            if (debug != null) {
                debug.println("PolicyChecker.processPolicyMappings() mapping exception");
                e.printStackTrace();
            }
            throw new CertPathValidatorException("Exception while checking mapping", e);
        }
    }

    private static PolicyNodeImpl removeInvalidNodes(PolicyNodeImpl rootNode, int certIndex, Set<String> initPolicies, CertificatePoliciesExtension currCertPolicies) throws CertPathValidatorException {
        try {
            boolean childDeleted = false;
            for (PolicyInformation curPolInfo : currCertPolicies.get(CertificatePoliciesExtension.POLICIES)) {
                String curPolicy = curPolInfo.getPolicyIdentifier().getIdentifier().toString();
                if (debug != null) {
                    debug.println("PolicyChecker.processPolicies() processing policy second time: " + curPolicy);
                }
                for (PolicyNodeImpl curNode : rootNode.getPolicyNodesValid(certIndex, curPolicy)) {
                    PolicyNodeImpl parentNode = (PolicyNodeImpl) curNode.getParent();
                    if (!(!parentNode.getValidPolicy().equals(ANY_POLICY) || initPolicies.contains(curPolicy) || (curPolicy.equals(ANY_POLICY) ^ 1) == 0)) {
                        if (debug != null) {
                            debug.println("PolicyChecker.processPolicies() before deleting: policy tree = " + rootNode);
                        }
                        parentNode.deleteChild(curNode);
                        childDeleted = true;
                        if (debug != null) {
                            debug.println("PolicyChecker.processPolicies() after deleting: policy tree = " + rootNode);
                        }
                    }
                }
            }
            if (!childDeleted) {
                return rootNode;
            }
            rootNode.prune(certIndex);
            if (rootNode.getChildren().hasNext()) {
                return rootNode;
            }
            return null;
        } catch (IOException ioe) {
            throw new CertPathValidatorException("Exception while retrieving policyOIDs", ioe);
        }
    }

    PolicyNode getPolicyTree() {
        if (this.rootNode == null) {
            return null;
        }
        PolicyNodeImpl policyTree = this.rootNode.copyTree();
        policyTree.setImmutable();
        return policyTree;
    }
}
