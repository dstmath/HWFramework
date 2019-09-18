package org.bouncycastle.x509;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.PKIXCertPathChecker;
import java.security.cert.PKIXParameters;
import java.security.cert.PolicyNode;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLEntry;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import javax.security.auth.x500.X500Principal;
import org.bouncycastle.asn1.ASN1Enumerated;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.x509.AccessDescription;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.AuthorityInformationAccess;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.CRLDistPoint;
import org.bouncycastle.asn1.x509.DistributionPoint;
import org.bouncycastle.asn1.x509.DistributionPointName;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.GeneralSubtree;
import org.bouncycastle.asn1.x509.IssuingDistributionPoint;
import org.bouncycastle.asn1.x509.NameConstraints;
import org.bouncycastle.asn1.x509.PolicyInformation;
import org.bouncycastle.asn1.x509.qualified.MonetaryValue;
import org.bouncycastle.asn1.x509.qualified.QCStatement;
import org.bouncycastle.i18n.ErrorBundle;
import org.bouncycastle.i18n.LocaleString;
import org.bouncycastle.i18n.filter.TrustedInput;
import org.bouncycastle.i18n.filter.UntrustedInput;
import org.bouncycastle.i18n.filter.UntrustedUrlInput;
import org.bouncycastle.jce.provider.AnnotatedException;
import org.bouncycastle.jce.provider.PKIXNameConstraintValidator;
import org.bouncycastle.jce.provider.PKIXNameConstraintValidatorException;
import org.bouncycastle.jce.provider.PKIXPolicyNode;
import org.bouncycastle.util.Integers;

public class PKIXCertPathReviewer extends CertPathValidatorUtilities {
    private static final String AUTH_INFO_ACCESS = Extension.authorityInfoAccess.getId();
    private static final String CRL_DIST_POINTS = Extension.cRLDistributionPoints.getId();
    private static final String QC_STATEMENT = Extension.qCStatements.getId();
    private static final String RESOURCE_NAME = "org.bouncycastle.x509.CertPathReviewerMessages";
    protected CertPath certPath;
    protected List certs;
    protected List[] errors;
    private boolean initialized;
    protected int n;
    protected List[] notifications;
    protected PKIXParameters pkixParams;
    protected PolicyNode policyTree;
    protected PublicKey subjectPublicKey;
    protected TrustAnchor trustAnchor;
    protected Date validDate;

    public PKIXCertPathReviewer() {
    }

    public PKIXCertPathReviewer(CertPath certPath2, PKIXParameters pKIXParameters) throws CertPathReviewerException {
        init(certPath2, pKIXParameters);
    }

    private String IPtoString(byte[] bArr) {
        try {
            return InetAddress.getByAddress(bArr).getHostAddress();
        } catch (Exception e) {
            StringBuffer stringBuffer = new StringBuffer();
            for (int i = 0; i != bArr.length; i++) {
                stringBuffer.append(Integer.toHexString(bArr[i] & 255));
                stringBuffer.append(' ');
            }
            return stringBuffer.toString();
        }
    }

    private void checkCriticalExtensions() {
        int size;
        List<PKIXCertPathChecker> certPathCheckers = this.pkixParams.getCertPathCheckers();
        for (PKIXCertPathChecker init : certPathCheckers) {
            try {
                init.init(false);
            } catch (CertPathValidatorException e) {
                throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.certPathCheckerError", new Object[]{e.getMessage(), e, e.getClass().getName()}), e);
            } catch (CertPathValidatorException e2) {
                throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.criticalExtensionError", new Object[]{e2.getMessage(), e2, e2.getClass().getName()}), e2.getCause(), this.certPath, size);
            } catch (CertPathReviewerException e3) {
                addError(e3.getErrorMessage(), e3.getIndex());
                return;
            }
        }
        size = this.certs.size() - 1;
        while (size >= 0) {
            X509Certificate x509Certificate = (X509Certificate) this.certs.get(size);
            Set<String> criticalExtensionOIDs = x509Certificate.getCriticalExtensionOIDs();
            if (criticalExtensionOIDs != null) {
                if (!criticalExtensionOIDs.isEmpty()) {
                    criticalExtensionOIDs.remove(KEY_USAGE);
                    criticalExtensionOIDs.remove(CERTIFICATE_POLICIES);
                    criticalExtensionOIDs.remove(POLICY_MAPPINGS);
                    criticalExtensionOIDs.remove(INHIBIT_ANY_POLICY);
                    criticalExtensionOIDs.remove(ISSUING_DISTRIBUTION_POINT);
                    criticalExtensionOIDs.remove(DELTA_CRL_INDICATOR);
                    criticalExtensionOIDs.remove(POLICY_CONSTRAINTS);
                    criticalExtensionOIDs.remove(BASIC_CONSTRAINTS);
                    criticalExtensionOIDs.remove(SUBJECT_ALTERNATIVE_NAME);
                    criticalExtensionOIDs.remove(NAME_CONSTRAINTS);
                    if (criticalExtensionOIDs.contains(QC_STATEMENT) && processQcStatements(x509Certificate, size)) {
                        criticalExtensionOIDs.remove(QC_STATEMENT);
                    }
                    for (PKIXCertPathChecker check : certPathCheckers) {
                        check.check(x509Certificate, criticalExtensionOIDs);
                    }
                    if (!criticalExtensionOIDs.isEmpty()) {
                        for (String aSN1ObjectIdentifier : criticalExtensionOIDs) {
                            addError(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.unknownCriticalExt", new Object[]{new ASN1ObjectIdentifier(aSN1ObjectIdentifier)}), size);
                        }
                    }
                }
            }
            size--;
        }
    }

    private void checkNameConstraints() {
        GeneralName instance;
        PKIXNameConstraintValidator pKIXNameConstraintValidator = new PKIXNameConstraintValidator();
        for (int size = this.certs.size() - 1; size > 0; size--) {
            int i = this.n;
            X509Certificate x509Certificate = (X509Certificate) this.certs.get(size);
            if (!isSelfIssued(x509Certificate)) {
                X500Principal subjectPrincipal = getSubjectPrincipal(x509Certificate);
                try {
                    ASN1Sequence aSN1Sequence = (ASN1Sequence) new ASN1InputStream((InputStream) new ByteArrayInputStream(subjectPrincipal.getEncoded())).readObject();
                    pKIXNameConstraintValidator.checkPermittedDN(aSN1Sequence);
                    pKIXNameConstraintValidator.checkExcludedDN(aSN1Sequence);
                    ASN1Sequence aSN1Sequence2 = (ASN1Sequence) getExtensionValue(x509Certificate, SUBJECT_ALTERNATIVE_NAME);
                    if (aSN1Sequence2 != null) {
                        for (int i2 = 0; i2 < aSN1Sequence2.size(); i2++) {
                            instance = GeneralName.getInstance(aSN1Sequence2.getObjectAt(i2));
                            pKIXNameConstraintValidator.checkPermitted(instance);
                            pKIXNameConstraintValidator.checkExcluded(instance);
                        }
                    }
                } catch (AnnotatedException e) {
                    throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.ncExtError"), e, this.certPath, size);
                } catch (IOException e2) {
                    throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.ncSubjectNameError", new Object[]{new UntrustedInput(subjectPrincipal)}), e2, this.certPath, size);
                } catch (PKIXNameConstraintValidatorException e3) {
                    throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.notPermittedDN", new Object[]{new UntrustedInput(subjectPrincipal.getName())}), e3, this.certPath, size);
                } catch (PKIXNameConstraintValidatorException e4) {
                    throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.excludedDN", new Object[]{new UntrustedInput(subjectPrincipal.getName())}), e4, this.certPath, size);
                } catch (AnnotatedException e5) {
                    throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.subjAltNameExtError"), e5, this.certPath, size);
                } catch (PKIXNameConstraintValidatorException e6) {
                    throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.notPermittedEmail", new Object[]{new UntrustedInput(instance)}), e6, this.certPath, size);
                } catch (CertPathReviewerException e7) {
                    addError(e7.getErrorMessage(), e7.getIndex());
                    return;
                }
            }
            ASN1Sequence aSN1Sequence3 = (ASN1Sequence) getExtensionValue(x509Certificate, NAME_CONSTRAINTS);
            if (aSN1Sequence3 != null) {
                NameConstraints instance2 = NameConstraints.getInstance(aSN1Sequence3);
                GeneralSubtree[] permittedSubtrees = instance2.getPermittedSubtrees();
                if (permittedSubtrees != null) {
                    pKIXNameConstraintValidator.intersectPermittedSubtree(permittedSubtrees);
                }
                GeneralSubtree[] excludedSubtrees = instance2.getExcludedSubtrees();
                if (excludedSubtrees != null) {
                    for (int i3 = 0; i3 != excludedSubtrees.length; i3++) {
                        pKIXNameConstraintValidator.addExcludedSubtree(excludedSubtrees[i3]);
                    }
                }
            }
        }
    }

    private void checkPathLength() {
        BasicConstraints basicConstraints;
        int i = this.n;
        int i2 = i;
        int i3 = 0;
        for (int size = this.certs.size() - 1; size > 0; size--) {
            int i4 = this.n;
            X509Certificate x509Certificate = (X509Certificate) this.certs.get(size);
            if (!isSelfIssued(x509Certificate)) {
                if (i2 <= 0) {
                    addError(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.pathLengthExtended"));
                }
                i2--;
                i3++;
            }
            try {
                basicConstraints = BasicConstraints.getInstance(getExtensionValue(x509Certificate, BASIC_CONSTRAINTS));
            } catch (AnnotatedException e) {
                addError(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.processLengthConstError"), size);
                basicConstraints = null;
            }
            if (basicConstraints != null) {
                BigInteger pathLenConstraint = basicConstraints.getPathLenConstraint();
                if (pathLenConstraint != null) {
                    int intValue = pathLenConstraint.intValue();
                    if (intValue < i2) {
                        i2 = intValue;
                    }
                }
            }
        }
        addNotification(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.totalPathLength", new Object[]{Integers.valueOf(i3)}));
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v7, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r15v0, resolved type: java.security.cert.X509Certificate} */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Removed duplicated region for block: B:111:0x0261 A[Catch:{ AnnotatedException -> 0x0619, AnnotatedException -> 0x0450, AnnotatedException -> 0x0432, AnnotatedException -> 0x0420, AnnotatedException -> 0x040e, AnnotatedException -> 0x0383, CertPathValidatorException -> 0x0370, CertPathValidatorException -> 0x021b, CertPathValidatorException -> 0x00d3, CertPathReviewerException -> 0x062b }] */
    /* JADX WARNING: Removed duplicated region for block: B:181:0x03bc A[Catch:{ AnnotatedException -> 0x0619, AnnotatedException -> 0x0450, AnnotatedException -> 0x0432, AnnotatedException -> 0x0420, AnnotatedException -> 0x040e, AnnotatedException -> 0x0383, CertPathValidatorException -> 0x0370, CertPathValidatorException -> 0x021b, CertPathValidatorException -> 0x00d3, CertPathReviewerException -> 0x062b }] */
    /* JADX WARNING: Removed duplicated region for block: B:196:0x0400 A[Catch:{ AnnotatedException -> 0x0619, AnnotatedException -> 0x0450, AnnotatedException -> 0x0432, AnnotatedException -> 0x0420, AnnotatedException -> 0x040e, AnnotatedException -> 0x0383, CertPathValidatorException -> 0x0370, CertPathValidatorException -> 0x021b, CertPathValidatorException -> 0x00d3, CertPathReviewerException -> 0x062b }] */
    /* JADX WARNING: Removed duplicated region for block: B:337:0x0124 A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x0119 A[Catch:{ AnnotatedException -> 0x0619, AnnotatedException -> 0x0450, AnnotatedException -> 0x0432, AnnotatedException -> 0x0420, AnnotatedException -> 0x040e, AnnotatedException -> 0x0383, CertPathValidatorException -> 0x0370, CertPathValidatorException -> 0x021b, CertPathValidatorException -> 0x00d3, CertPathReviewerException -> 0x062b }] */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x0139 A[Catch:{ AnnotatedException -> 0x0619, AnnotatedException -> 0x0450, AnnotatedException -> 0x0432, AnnotatedException -> 0x0420, AnnotatedException -> 0x040e, AnnotatedException -> 0x0383, CertPathValidatorException -> 0x0370, CertPathValidatorException -> 0x021b, CertPathValidatorException -> 0x00d3, CertPathReviewerException -> 0x062b }] */
    /* JADX WARNING: Removed duplicated region for block: B:99:0x0235 A[Catch:{ AnnotatedException -> 0x0619, AnnotatedException -> 0x0450, AnnotatedException -> 0x0432, AnnotatedException -> 0x0420, AnnotatedException -> 0x040e, AnnotatedException -> 0x0383, CertPathValidatorException -> 0x0370, CertPathValidatorException -> 0x021b, CertPathValidatorException -> 0x00d3, CertPathReviewerException -> 0x062b }] */
    private void checkPolicy() {
        int size;
        int i;
        int i2;
        PKIXPolicyNode pKIXPolicyNode;
        int i3;
        PKIXPolicyNode pKIXPolicyNode2;
        ASN1Sequence aSN1Sequence;
        Set<String> set;
        X509Certificate x509Certificate;
        int i4;
        ASN1Sequence aSN1Sequence2;
        ASN1Integer aSN1Integer;
        HashSet hashSet;
        Enumeration objects;
        String str;
        int i5;
        PKIXPolicyNode pKIXPolicyNode3;
        ASN1Sequence aSN1Sequence3;
        Iterator it;
        X509Certificate x509Certificate2;
        HashSet hashSet2;
        int i6;
        Set criticalExtensionOIDs;
        Set<String> initialPolicies = this.pkixParams.getInitialPolicies();
        ArrayList[] arrayListArr = new ArrayList[(this.n + 1)];
        for (int i7 = 0; i7 < arrayListArr.length; i7++) {
            arrayListArr[i7] = new ArrayList();
        }
        HashSet hashSet3 = new HashSet();
        hashSet3.add(RFC3280CertPathUtilities.ANY_POLICY);
        PKIXPolicyNode pKIXPolicyNode4 = new PKIXPolicyNode(new ArrayList(), 0, hashSet3, null, new HashSet(), RFC3280CertPathUtilities.ANY_POLICY, false);
        arrayListArr[0].add(pKIXPolicyNode4);
        int i8 = this.pkixParams.isExplicitPolicyRequired() ? 0 : this.n + 1;
        int i9 = this.pkixParams.isAnyPolicyInhibited() ? 0 : this.n + 1;
        int i10 = this.pkixParams.isPolicyMappingInhibited() ? 0 : this.n + 1;
        try {
            size = this.certs.size() - 1;
            int i11 = i9;
            int i12 = i10;
            HashSet hashSet4 = null;
            PKIXPolicyNode pKIXPolicyNode5 = pKIXPolicyNode4;
            X509Certificate x509Certificate3 = null;
            while (size >= 0) {
                int i13 = this.n - size;
                X509Certificate x509Certificate4 = this.certs.get(size);
                ASN1Sequence aSN1Sequence4 = (ASN1Sequence) getExtensionValue(x509Certificate4, CERTIFICATE_POLICIES);
                if (aSN1Sequence4 == null || pKIXPolicyNode5 == null) {
                    set = initialPolicies;
                    pKIXPolicyNode2 = pKIXPolicyNode5;
                    i3 = i11;
                    aSN1Sequence = aSN1Sequence4;
                    x509Certificate = x509Certificate4;
                } else {
                    Enumeration objects2 = aSN1Sequence4.getObjects();
                    HashSet hashSet5 = new HashSet();
                    while (objects2.hasMoreElements()) {
                        PolicyInformation instance = PolicyInformation.getInstance(objects2.nextElement());
                        ASN1ObjectIdentifier policyIdentifier = instance.getPolicyIdentifier();
                        Enumeration enumeration = objects2;
                        hashSet5.add(policyIdentifier.getId());
                        Set<String> set2 = initialPolicies;
                        if (!RFC3280CertPathUtilities.ANY_POLICY.equals(policyIdentifier.getId())) {
                            Set qualifierSet = getQualifierSet(instance.getPolicyQualifiers());
                            if (!processCertD1i(i13, arrayListArr, policyIdentifier, qualifierSet)) {
                                processCertD1ii(i13, arrayListArr, policyIdentifier, qualifierSet);
                            }
                        }
                        objects2 = enumeration;
                        initialPolicies = set2;
                    }
                    set = initialPolicies;
                    if (hashSet4 != null) {
                        if (!hashSet4.contains(RFC3280CertPathUtilities.ANY_POLICY)) {
                            hashSet = new HashSet();
                            for (Object next : hashSet4) {
                                if (hashSet5.contains(next)) {
                                    hashSet.add(next);
                                }
                            }
                            if (i11 <= 0) {
                                if (i13 < this.n && isSelfIssued(x509Certificate4)) {
                                }
                                HashSet hashSet6 = hashSet;
                                pKIXPolicyNode2 = pKIXPolicyNode5;
                                i3 = i11;
                                aSN1Sequence = aSN1Sequence4;
                                x509Certificate = x509Certificate4;
                                for (i6 = i13 - 1; i6 >= 0; i6--) {
                                    ArrayList arrayList = arrayListArr[i6];
                                    PKIXPolicyNode pKIXPolicyNode6 = pKIXPolicyNode2;
                                    int i14 = 0;
                                    while (true) {
                                        if (i14 < arrayList.size()) {
                                            PKIXPolicyNode pKIXPolicyNode7 = (PKIXPolicyNode) arrayList.get(i14);
                                            if (!pKIXPolicyNode7.hasChildren()) {
                                                pKIXPolicyNode6 = removePolicyNode(pKIXPolicyNode6, arrayListArr, pKIXPolicyNode7);
                                                if (pKIXPolicyNode6 == null) {
                                                }
                                            }
                                            i14++;
                                        }
                                    }
                                    pKIXPolicyNode2 = pKIXPolicyNode6;
                                }
                                criticalExtensionOIDs = x509Certificate.getCriticalExtensionOIDs();
                                if (criticalExtensionOIDs != null) {
                                    boolean contains = criticalExtensionOIDs.contains(CERTIFICATE_POLICIES);
                                    ArrayList arrayList2 = arrayListArr[i13];
                                    for (int i15 = 0; i15 < arrayList2.size(); i15++) {
                                        ((PKIXPolicyNode) arrayList2.get(i15)).setCritical(contains);
                                    }
                                }
                                hashSet4 = hashSet6;
                            }
                            objects = aSN1Sequence4.getObjects();
                            while (true) {
                                if (!objects.hasMoreElements()) {
                                    PolicyInformation instance2 = PolicyInformation.getInstance(objects.nextElement());
                                    if (RFC3280CertPathUtilities.ANY_POLICY.equals(instance2.getPolicyIdentifier().getId())) {
                                        Set qualifierSet2 = getQualifierSet(instance2.getPolicyQualifiers());
                                        ArrayList arrayList3 = arrayListArr[i13 - 1];
                                        int i16 = 0;
                                        while (i16 < arrayList3.size()) {
                                            PKIXPolicyNode pKIXPolicyNode8 = (PKIXPolicyNode) arrayList3.get(i16);
                                            Iterator it2 = pKIXPolicyNode8.getExpectedPolicies().iterator();
                                            while (it2.hasNext()) {
                                                HashSet hashSet7 = hashSet;
                                                Object next2 = it2.next();
                                                ArrayList arrayList4 = arrayList3;
                                                if (next2 instanceof String) {
                                                    str = (String) next2;
                                                } else if (next2 instanceof ASN1ObjectIdentifier) {
                                                    str = ((ASN1ObjectIdentifier) next2).getId();
                                                } else {
                                                    int i17 = i11;
                                                    hashSet2 = hashSet7;
                                                    arrayList3 = arrayList4;
                                                }
                                                Iterator children = pKIXPolicyNode8.getChildren();
                                                boolean z = false;
                                                while (children.hasNext()) {
                                                    Iterator it3 = children;
                                                    if (str.equals(((PKIXPolicyNode) children.next()).getValidPolicy())) {
                                                        z = true;
                                                    }
                                                    children = it3;
                                                }
                                                if (!z) {
                                                    HashSet hashSet8 = new HashSet();
                                                    hashSet8.add(str);
                                                    pKIXPolicyNode3 = pKIXPolicyNode5;
                                                    it = it2;
                                                    aSN1Sequence3 = aSN1Sequence4;
                                                    i5 = i11;
                                                    x509Certificate2 = x509Certificate4;
                                                    PKIXPolicyNode pKIXPolicyNode9 = new PKIXPolicyNode(new ArrayList(), i13, hashSet8, pKIXPolicyNode8, qualifierSet2, str, false);
                                                    pKIXPolicyNode8.addChild(pKIXPolicyNode9);
                                                    arrayListArr[i13].add(pKIXPolicyNode9);
                                                } else {
                                                    pKIXPolicyNode3 = pKIXPolicyNode5;
                                                    i5 = i11;
                                                    it = it2;
                                                    aSN1Sequence3 = aSN1Sequence4;
                                                    x509Certificate2 = x509Certificate4;
                                                }
                                                x509Certificate4 = x509Certificate2;
                                                it2 = it;
                                                hashSet2 = hashSet7;
                                                arrayList3 = arrayList4;
                                                aSN1Sequence4 = aSN1Sequence3;
                                                pKIXPolicyNode5 = pKIXPolicyNode3;
                                                i11 = i5;
                                            }
                                            HashSet hashSet9 = hashSet;
                                            ArrayList arrayList5 = arrayList3;
                                            PKIXPolicyNode pKIXPolicyNode10 = pKIXPolicyNode5;
                                            int i18 = i11;
                                            ASN1Sequence aSN1Sequence5 = aSN1Sequence4;
                                            X509Certificate x509Certificate5 = x509Certificate4;
                                            i16++;
                                            i11 = i18;
                                        }
                                    } else {
                                        int i19 = i11;
                                    }
                                }
                            }
                            HashSet hashSet62 = hashSet;
                            pKIXPolicyNode2 = pKIXPolicyNode5;
                            i3 = i11;
                            aSN1Sequence = aSN1Sequence4;
                            x509Certificate = x509Certificate4;
                            while (i6 >= 0) {
                            }
                            criticalExtensionOIDs = x509Certificate.getCriticalExtensionOIDs();
                            if (criticalExtensionOIDs != null) {
                            }
                            hashSet4 = hashSet62;
                        }
                    }
                    hashSet = hashSet5;
                    if (i11 <= 0) {
                    }
                    objects = aSN1Sequence4.getObjects();
                    while (true) {
                        if (!objects.hasMoreElements()) {
                        }
                        int i192 = i11;
                    }
                    HashSet hashSet622 = hashSet;
                    pKIXPolicyNode2 = pKIXPolicyNode5;
                    i3 = i11;
                    aSN1Sequence = aSN1Sequence4;
                    x509Certificate = x509Certificate4;
                    while (i6 >= 0) {
                    }
                    criticalExtensionOIDs = x509Certificate.getCriticalExtensionOIDs();
                    if (criticalExtensionOIDs != null) {
                    }
                    hashSet4 = hashSet622;
                }
                PKIXPolicyNode pKIXPolicyNode11 = pKIXPolicyNode2;
                if (aSN1Sequence == null) {
                    pKIXPolicyNode11 = null;
                }
                if (i8 <= 0) {
                    if (pKIXPolicyNode11 == null) {
                        throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.noValidPolicyTree"));
                    }
                }
                if (i13 != this.n) {
                    ASN1Primitive extensionValue = getExtensionValue(x509Certificate, POLICY_MAPPINGS);
                    if (extensionValue != null) {
                        ASN1Sequence aSN1Sequence6 = (ASN1Sequence) extensionValue;
                        int i20 = 0;
                        while (i20 < aSN1Sequence6.size()) {
                            ASN1Sequence aSN1Sequence7 = (ASN1Sequence) aSN1Sequence6.getObjectAt(i20);
                            ASN1ObjectIdentifier aSN1ObjectIdentifier = (ASN1ObjectIdentifier) aSN1Sequence7.getObjectAt(1);
                            if (RFC3280CertPathUtilities.ANY_POLICY.equals(((ASN1ObjectIdentifier) aSN1Sequence7.getObjectAt(0)).getId())) {
                                throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.invalidPolicyMapping"), this.certPath, size);
                            } else if (!RFC3280CertPathUtilities.ANY_POLICY.equals(aSN1ObjectIdentifier.getId())) {
                                i20++;
                            } else {
                                throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.invalidPolicyMapping"), this.certPath, size);
                            }
                        }
                    }
                    if (extensionValue != null) {
                        ASN1Sequence aSN1Sequence8 = (ASN1Sequence) extensionValue;
                        HashMap hashMap = new HashMap();
                        HashSet<String> hashSet10 = new HashSet<>();
                        for (int i21 = 0; i21 < aSN1Sequence8.size(); i21++) {
                            ASN1Sequence aSN1Sequence9 = (ASN1Sequence) aSN1Sequence8.getObjectAt(i21);
                            String id = ((ASN1ObjectIdentifier) aSN1Sequence9.getObjectAt(0)).getId();
                            String id2 = ((ASN1ObjectIdentifier) aSN1Sequence9.getObjectAt(1)).getId();
                            if (!hashMap.containsKey(id)) {
                                HashSet hashSet11 = new HashSet();
                                hashSet11.add(id2);
                                hashMap.put(id, hashSet11);
                                hashSet10.add(id);
                            } else {
                                ((Set) hashMap.get(id)).add(id2);
                            }
                        }
                        for (String str2 : hashSet10) {
                            if (i12 > 0) {
                                prepareNextCertB1(i13, arrayListArr, str2, hashMap, x509Certificate);
                            } else if (i12 <= 0) {
                                pKIXPolicyNode11 = prepareNextCertB2(i13, arrayListArr, str2, pKIXPolicyNode11);
                            }
                        }
                    }
                    if (!isSelfIssued(x509Certificate)) {
                        if (i8 != 0) {
                            i8--;
                        }
                        if (i12 != 0) {
                            i12--;
                        }
                        if (i3 != 0) {
                            i4 = i3 - 1;
                            aSN1Sequence2 = (ASN1Sequence) getExtensionValue(x509Certificate, POLICY_CONSTRAINTS);
                            if (aSN1Sequence2 != null) {
                                Enumeration objects3 = aSN1Sequence2.getObjects();
                                while (objects3.hasMoreElements()) {
                                    ASN1TaggedObject aSN1TaggedObject = (ASN1TaggedObject) objects3.nextElement();
                                    switch (aSN1TaggedObject.getTagNo()) {
                                        case 0:
                                            int intValue = ASN1Integer.getInstance(aSN1TaggedObject, false).getValue().intValue();
                                            if (intValue >= i8) {
                                                break;
                                            } else {
                                                i8 = intValue;
                                                break;
                                            }
                                        case 1:
                                            int intValue2 = ASN1Integer.getInstance(aSN1TaggedObject, false).getValue().intValue();
                                            if (intValue2 >= i12) {
                                                break;
                                            } else {
                                                i12 = intValue2;
                                                break;
                                            }
                                    }
                                }
                            }
                            aSN1Integer = (ASN1Integer) getExtensionValue(x509Certificate, INHIBIT_ANY_POLICY);
                            if (aSN1Integer != null) {
                                int intValue3 = aSN1Integer.getValue().intValue();
                                if (intValue3 < i4) {
                                    i4 = intValue3;
                                }
                            }
                            i3 = i4;
                        }
                    }
                    i4 = i3;
                    aSN1Sequence2 = (ASN1Sequence) getExtensionValue(x509Certificate, POLICY_CONSTRAINTS);
                    if (aSN1Sequence2 != null) {
                    }
                    aSN1Integer = (ASN1Integer) getExtensionValue(x509Certificate, INHIBIT_ANY_POLICY);
                    if (aSN1Integer != null) {
                    }
                    i3 = i4;
                }
                pKIXPolicyNode5 = pKIXPolicyNode11;
                size--;
                x509Certificate3 = x509Certificate;
                initialPolicies = set;
                i11 = i3;
            }
            Set<String> set3 = initialPolicies;
            PKIXPolicyNode pKIXPolicyNode12 = pKIXPolicyNode5;
            if (!isSelfIssued(x509Certificate3) && i8 > 0) {
                i8--;
            }
            ASN1Sequence aSN1Sequence10 = (ASN1Sequence) getExtensionValue(x509Certificate3, POLICY_CONSTRAINTS);
            if (aSN1Sequence10 != null) {
                Enumeration objects4 = aSN1Sequence10.getObjects();
                i2 = i8;
                while (objects4.hasMoreElements()) {
                    ASN1TaggedObject aSN1TaggedObject2 = (ASN1TaggedObject) objects4.nextElement();
                    if (aSN1TaggedObject2.getTagNo() == 0) {
                        if (ASN1Integer.getInstance(aSN1TaggedObject2, false).getValue().intValue() == 0) {
                            i2 = 0;
                        }
                    }
                }
                i = 0;
            } else {
                i = 0;
                i2 = i8;
            }
            if (pKIXPolicyNode12 != null) {
                Set<String> set4 = set3;
                if (isAnyPolicy(set4)) {
                    if (this.pkixParams.isExplicitPolicyRequired()) {
                        if (!hashSet4.isEmpty()) {
                            HashSet<PKIXPolicyNode> hashSet12 = new HashSet<>();
                            for (int i22 = i; i22 < arrayListArr.length; i22++) {
                                ArrayList arrayList6 = arrayListArr[i22];
                                for (int i23 = i; i23 < arrayList6.size(); i23++) {
                                    PKIXPolicyNode pKIXPolicyNode13 = (PKIXPolicyNode) arrayList6.get(i23);
                                    if (RFC3280CertPathUtilities.ANY_POLICY.equals(pKIXPolicyNode13.getValidPolicy())) {
                                        Iterator children2 = pKIXPolicyNode13.getChildren();
                                        while (children2.hasNext()) {
                                            hashSet12.add(children2.next());
                                        }
                                    }
                                }
                            }
                            for (PKIXPolicyNode validPolicy : hashSet12) {
                                hashSet4.contains(validPolicy.getValidPolicy());
                            }
                            if (pKIXPolicyNode12 != null) {
                                int i24 = this.n - 1;
                                while (i24 >= 0) {
                                    ArrayList arrayList7 = arrayListArr[i24];
                                    PKIXPolicyNode pKIXPolicyNode14 = pKIXPolicyNode12;
                                    for (int i25 = i; i25 < arrayList7.size(); i25++) {
                                        PKIXPolicyNode pKIXPolicyNode15 = (PKIXPolicyNode) arrayList7.get(i25);
                                        if (!pKIXPolicyNode15.hasChildren()) {
                                            pKIXPolicyNode14 = removePolicyNode(pKIXPolicyNode14, arrayListArr, pKIXPolicyNode15);
                                        }
                                    }
                                    i24--;
                                    pKIXPolicyNode12 = pKIXPolicyNode14;
                                }
                            }
                        } else {
                            throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.explicitPolicy"), this.certPath, size);
                        }
                    }
                    pKIXPolicyNode = pKIXPolicyNode12;
                } else {
                    HashSet<PKIXPolicyNode> hashSet13 = new HashSet<>();
                    for (int i26 = i; i26 < arrayListArr.length; i26++) {
                        ArrayList arrayList8 = arrayListArr[i26];
                        for (int i27 = i; i27 < arrayList8.size(); i27++) {
                            PKIXPolicyNode pKIXPolicyNode16 = (PKIXPolicyNode) arrayList8.get(i27);
                            if (RFC3280CertPathUtilities.ANY_POLICY.equals(pKIXPolicyNode16.getValidPolicy())) {
                                Iterator children3 = pKIXPolicyNode16.getChildren();
                                while (children3.hasNext()) {
                                    PKIXPolicyNode pKIXPolicyNode17 = (PKIXPolicyNode) children3.next();
                                    if (!RFC3280CertPathUtilities.ANY_POLICY.equals(pKIXPolicyNode17.getValidPolicy())) {
                                        hashSet13.add(pKIXPolicyNode17);
                                    }
                                }
                            }
                        }
                    }
                    PKIXPolicyNode pKIXPolicyNode18 = pKIXPolicyNode12;
                    for (PKIXPolicyNode pKIXPolicyNode19 : hashSet13) {
                        if (!set4.contains(pKIXPolicyNode19.getValidPolicy())) {
                            pKIXPolicyNode18 = removePolicyNode(pKIXPolicyNode18, arrayListArr, pKIXPolicyNode19);
                        }
                    }
                    if (pKIXPolicyNode18 != null) {
                        int i28 = this.n - 1;
                        while (i28 >= 0) {
                            ArrayList arrayList9 = arrayListArr[i28];
                            PKIXPolicyNode pKIXPolicyNode20 = pKIXPolicyNode18;
                            for (int i29 = i; i29 < arrayList9.size(); i29++) {
                                PKIXPolicyNode pKIXPolicyNode21 = (PKIXPolicyNode) arrayList9.get(i29);
                                if (!pKIXPolicyNode21.hasChildren()) {
                                    pKIXPolicyNode20 = removePolicyNode(pKIXPolicyNode20, arrayListArr, pKIXPolicyNode21);
                                }
                            }
                            i28--;
                            pKIXPolicyNode18 = pKIXPolicyNode20;
                        }
                    }
                    pKIXPolicyNode = pKIXPolicyNode18;
                }
            } else if (!this.pkixParams.isExplicitPolicyRequired()) {
                pKIXPolicyNode = null;
            } else {
                throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.explicitPolicy"), this.certPath, size);
            }
            if (i2 <= 0 && pKIXPolicyNode == null) {
                throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.invalidPolicy"));
            }
        } catch (AnnotatedException e) {
            throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.policyConstExtError"), this.certPath, size);
        } catch (AnnotatedException e2) {
            throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.policyExtError"), e2, this.certPath, size);
        } catch (AnnotatedException e3) {
            throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.policyMapExtError"), e3, this.certPath, size);
        } catch (AnnotatedException e4) {
            throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.policyConstExtError"), this.certPath, size);
        } catch (AnnotatedException e5) {
            throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.policyInhibitExtError"), this.certPath, size);
        } catch (AnnotatedException e6) {
            throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.policyExtError"), e6, this.certPath, size);
        } catch (CertPathValidatorException e7) {
            throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.policyQualifierError"), e7, this.certPath, size);
        } catch (CertPathValidatorException e8) {
            throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.policyQualifierError"), e8, this.certPath, size);
        } catch (CertPathValidatorException e9) {
            throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.policyQualifierError"), e9, this.certPath, size);
        } catch (CertPathReviewerException e10) {
            addError(e10.getErrorMessage(), e10.getIndex());
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v6, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v2, resolved type: java.security.cert.X509Certificate} */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Removed duplicated region for block: B:101:0x02dc A[Catch:{ AnnotatedException -> 0x02e1 }] */
    /* JADX WARNING: Removed duplicated region for block: B:108:0x0301 A[LOOP:1: B:106:0x02fb->B:108:0x0301, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:112:0x032e A[LOOP:2: B:110:0x0328->B:112:0x032e, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:123:0x0375  */
    /* JADX WARNING: Removed duplicated region for block: B:125:0x037c  */
    /* JADX WARNING: Removed duplicated region for block: B:131:0x03af A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x0108  */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x0153  */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x0156  */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x0180  */
    /* JADX WARNING: Removed duplicated region for block: B:60:0x018f  */
    /* JADX WARNING: Removed duplicated region for block: B:90:0x02b6 A[SYNTHETIC, Splitter:B:90:0x02b6] */
    private void checkSignatures() {
        TrustAnchor trustAnchor2;
        TrustAnchor trustAnchor3;
        X500Principal x500Principal;
        PublicKey publicKey;
        X509Certificate x509Certificate;
        int size;
        X500Principal x500Principal2;
        X509Certificate x509Certificate2;
        PublicKey publicKey2;
        X509Certificate x509Certificate3;
        X500Principal x500Principal3;
        int i;
        X509Certificate x509Certificate4;
        PublicKey publicKey3;
        int i2;
        int i3;
        char c;
        char c2;
        PublicKey publicKey4;
        ErrorBundle errorBundle;
        CRLDistPoint cRLDistPoint;
        AuthorityInformationAccess authorityInformationAccess;
        Iterator it;
        int i4;
        Iterator it2;
        int i5;
        ASN1Primitive extensionValue;
        ErrorBundle errorBundle2;
        ErrorBundle errorBundle3;
        X500Principal x500Principal4;
        char c3 = 2;
        char c4 = 0;
        int i6 = 1;
        addNotification(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.certPathValidDate", new Object[]{new TrustedInput(this.validDate), new TrustedInput(new Date())}));
        try {
            X509Certificate x509Certificate5 = (X509Certificate) this.certs.get(this.certs.size() - 1);
            Collection trustAnchors = getTrustAnchors(x509Certificate5, this.pkixParams.getTrustAnchors());
            if (trustAnchors.size() > 1) {
                addError(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.conflictingTrustAnchors", new Object[]{Integers.valueOf(trustAnchors.size()), new UntrustedInput(x509Certificate5.getIssuerX500Principal())}));
            } else if (trustAnchors.isEmpty()) {
                addError(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.noTrustAnchorFound", new Object[]{new UntrustedInput(x509Certificate5.getIssuerX500Principal()), Integers.valueOf(this.pkixParams.getTrustAnchors().size())}));
            } else {
                trustAnchor2 = (TrustAnchor) trustAnchors.iterator().next();
                try {
                    try {
                        CertPathValidatorUtilities.verifyX509Certificate(x509Certificate5, trustAnchor2.getTrustedCert() != null ? trustAnchor2.getTrustedCert().getPublicKey() : trustAnchor2.getCAPublicKey(), this.pkixParams.getSigProvider());
                    } catch (SignatureException e) {
                        addError(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.trustButInvalidCert"));
                    } catch (Exception e2) {
                    }
                } catch (CertPathReviewerException e3) {
                    e = e3;
                } catch (Throwable th) {
                    th = th;
                    addError(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.unknown", new Object[]{new UntrustedInput(th.getMessage()), new UntrustedInput(th)}));
                    trustAnchor3 = trustAnchor2;
                    if (trustAnchor3 != null) {
                    }
                    if (trustAnchor3 != null) {
                    }
                    size = this.certs.size() - 1;
                    x500Principal2 = x500Principal;
                    x509Certificate2 = x509Certificate;
                    publicKey2 = publicKey;
                    while (size >= 0) {
                    }
                    this.trustAnchor = trustAnchor3;
                    this.subjectPublicKey = publicKey2;
                    return;
                }
                trustAnchor3 = trustAnchor2;
                if (trustAnchor3 != null) {
                    X509Certificate trustedCert = trustAnchor3.getTrustedCert();
                    if (trustedCert != null) {
                        try {
                            x500Principal4 = getSubjectPrincipal(trustedCert);
                        } catch (IllegalArgumentException e4) {
                            addError(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.trustDNInvalid", new Object[]{new UntrustedInput(trustAnchor3.getCAName())}));
                            x500Principal4 = null;
                        }
                    } else {
                        x500Principal4 = new X500Principal(trustAnchor3.getCAName());
                    }
                    if (trustedCert != null) {
                        boolean[] keyUsage = trustedCert.getKeyUsage();
                        if (keyUsage != null && !keyUsage[5]) {
                            addNotification(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.trustKeyUsage"));
                        }
                    }
                    x500Principal = x500Principal4;
                } else {
                    x500Principal = null;
                }
                if (trustAnchor3 != null) {
                    x509Certificate = trustAnchor3.getTrustedCert();
                    publicKey = x509Certificate != null ? x509Certificate.getPublicKey() : trustAnchor3.getCAPublicKey();
                    try {
                        AlgorithmIdentifier algorithmIdentifier = getAlgorithmIdentifier(publicKey);
                        algorithmIdentifier.getAlgorithm();
                        algorithmIdentifier.getParameters();
                    } catch (CertPathValidatorException e5) {
                        addError(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.trustPubKeyError"));
                    }
                } else {
                    x509Certificate = null;
                    publicKey = null;
                }
                size = this.certs.size() - 1;
                x500Principal2 = x500Principal;
                x509Certificate2 = x509Certificate;
                publicKey2 = publicKey;
                while (size >= 0) {
                    int i7 = this.n - size;
                    x509Certificate3 = this.certs.get(size);
                    if (publicKey2 != null) {
                        try {
                            CertPathValidatorUtilities.verifyX509Certificate(x509Certificate3, publicKey2, this.pkixParams.getSigProvider());
                        } catch (GeneralSecurityException e6) {
                            Object[] objArr = new Object[3];
                            objArr[c4] = e6.getMessage();
                            objArr[i6] = e6;
                            objArr[c3] = e6.getClass().getName();
                            errorBundle3 = new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.signatureNotVerified", objArr);
                        }
                    } else if (isSelfIssued(x509Certificate3)) {
                        try {
                            CertPathValidatorUtilities.verifyX509Certificate(x509Certificate3, x509Certificate3.getPublicKey(), this.pkixParams.getSigProvider());
                            addError(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.rootKeyIsValidButNotATrustAnchor"), size);
                        } catch (GeneralSecurityException e7) {
                            Object[] objArr2 = new Object[3];
                            objArr2[c4] = e7.getMessage();
                            objArr2[i6] = e7;
                            objArr2[c3] = e7.getClass().getName();
                            errorBundle3 = new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.signatureNotVerified", objArr2);
                        }
                    } else {
                        ErrorBundle errorBundle4 = new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.NoIssuerPublicKey");
                        byte[] extensionValue2 = x509Certificate3.getExtensionValue(Extension.authorityKeyIdentifier.getId());
                        if (extensionValue2 != null) {
                            AuthorityKeyIdentifier instance = AuthorityKeyIdentifier.getInstance(DEROctetString.getInstance(extensionValue2).getOctets());
                            GeneralNames authorityCertIssuer = instance.getAuthorityCertIssuer();
                            if (authorityCertIssuer != null) {
                                Object obj = authorityCertIssuer.getNames()[c4];
                                Object authorityCertSerialNumber = instance.getAuthorityCertSerialNumber();
                                if (authorityCertSerialNumber != null) {
                                    Object[] objArr3 = new Object[7];
                                    objArr3[c4] = new LocaleString(RESOURCE_NAME, "missingIssuer");
                                    objArr3[1] = " \"";
                                    objArr3[2] = obj;
                                    objArr3[3] = "\" ";
                                    objArr3[4] = new LocaleString(RESOURCE_NAME, "missingSerial");
                                    objArr3[5] = " ";
                                    objArr3[6] = authorityCertSerialNumber;
                                    errorBundle4.setExtraArguments(objArr3);
                                }
                            }
                        }
                        addError(errorBundle4, size);
                    }
                    try {
                        x509Certificate3.checkValidity(this.validDate);
                    } catch (CertificateNotYetValidException e8) {
                        Object[] objArr4 = new Object[1];
                        objArr4[c4] = new TrustedInput(x509Certificate3.getNotBefore());
                        errorBundle2 = new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.certificateNotYetValid", objArr4);
                    } catch (CertificateExpiredException e9) {
                        Object[] objArr5 = new Object[1];
                        objArr5[c4] = new TrustedInput(x509Certificate3.getNotAfter());
                        errorBundle2 = new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.certificateExpired", objArr5);
                    }
                    if (this.pkixParams.isRevocationEnabled()) {
                        try {
                            ASN1Primitive extensionValue3 = getExtensionValue(x509Certificate3, CRL_DIST_POINTS);
                            if (extensionValue3 != null) {
                                cRLDistPoint = CRLDistPoint.getInstance(extensionValue3);
                                extensionValue = getExtensionValue(x509Certificate3, AUTH_INFO_ACCESS);
                                if (extensionValue != null) {
                                    authorityInformationAccess = AuthorityInformationAccess.getInstance(extensionValue);
                                    Vector cRLDistUrls = getCRLDistUrls(cRLDistPoint);
                                    Vector oCSPUrls = getOCSPUrls(authorityInformationAccess);
                                    it = cRLDistUrls.iterator();
                                    while (it.hasNext()) {
                                        addNotification(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.crlDistPoint", new Object[]{new UntrustedUrlInput(it.next())}), size);
                                        i7 = i7;
                                    }
                                    i4 = i7;
                                    it2 = oCSPUrls.iterator();
                                    while (it2.hasNext()) {
                                        addNotification(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.ocspLocation", new Object[]{new UntrustedUrlInput(it2.next())}), size);
                                    }
                                    x509Certificate4 = x509Certificate3;
                                    i = i4;
                                    x500Principal3 = x500Principal2;
                                    Vector vector = cRLDistUrls;
                                    publicKey3 = publicKey2;
                                    i5 = size;
                                    checkRevocation(this.pkixParams, x509Certificate3, this.validDate, x509Certificate2, publicKey2, vector, oCSPUrls, size);
                                    i2 = i5;
                                }
                                authorityInformationAccess = null;
                                Vector cRLDistUrls2 = getCRLDistUrls(cRLDistPoint);
                                Vector oCSPUrls2 = getOCSPUrls(authorityInformationAccess);
                                it = cRLDistUrls2.iterator();
                                while (it.hasNext()) {
                                }
                                i4 = i7;
                                it2 = oCSPUrls2.iterator();
                                while (it2.hasNext()) {
                                }
                                x509Certificate4 = x509Certificate3;
                                i = i4;
                                x500Principal3 = x500Principal2;
                                Vector vector2 = cRLDistUrls2;
                                publicKey3 = publicKey2;
                                i5 = size;
                                try {
                                    checkRevocation(this.pkixParams, x509Certificate3, this.validDate, x509Certificate2, publicKey2, vector2, oCSPUrls2, size);
                                    i2 = i5;
                                } catch (CertPathReviewerException e10) {
                                    e = e10;
                                    i2 = i5;
                                    addError(e.getErrorMessage(), i2);
                                    if (x500Principal3 != null) {
                                    }
                                    c2 = 2;
                                    c = 0;
                                    i3 = 1;
                                    if (i != this.n) {
                                    }
                                    X500Principal subjectX500Principal = x509Certificate4.getSubjectX500Principal();
                                    publicKey4 = getNextWorkingKey(this.certs, i2);
                                    try {
                                        AlgorithmIdentifier algorithmIdentifier2 = getAlgorithmIdentifier(publicKey4);
                                        algorithmIdentifier2.getAlgorithm();
                                        algorithmIdentifier2.getParameters();
                                    } catch (CertPathValidatorException e11) {
                                    }
                                    size = i2 - 1;
                                    c3 = c2;
                                    i6 = i3;
                                    x509Certificate2 = x509Certificate4;
                                    publicKey2 = publicKey4;
                                    c4 = c;
                                    x500Principal2 = subjectX500Principal;
                                }
                            }
                        } catch (AnnotatedException e12) {
                            addError(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.crlDistPtExtError"), size);
                        }
                        cRLDistPoint = null;
                        try {
                            extensionValue = getExtensionValue(x509Certificate3, AUTH_INFO_ACCESS);
                            if (extensionValue != null) {
                            }
                        } catch (AnnotatedException e13) {
                            addError(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.crlAuthInfoAccError"), size);
                        }
                        authorityInformationAccess = null;
                        Vector cRLDistUrls22 = getCRLDistUrls(cRLDistPoint);
                        Vector oCSPUrls22 = getOCSPUrls(authorityInformationAccess);
                        it = cRLDistUrls22.iterator();
                        while (it.hasNext()) {
                        }
                        i4 = i7;
                        it2 = oCSPUrls22.iterator();
                        while (it2.hasNext()) {
                        }
                        try {
                            x509Certificate4 = x509Certificate3;
                            i = i4;
                            x500Principal3 = x500Principal2;
                            Vector vector22 = cRLDistUrls22;
                            publicKey3 = publicKey2;
                            i5 = size;
                            checkRevocation(this.pkixParams, x509Certificate3, this.validDate, x509Certificate2, publicKey2, vector22, oCSPUrls22, size);
                            i2 = i5;
                        } catch (CertPathReviewerException e14) {
                            e = e14;
                            x509Certificate4 = x509Certificate3;
                            x500Principal3 = x500Principal2;
                            publicKey3 = publicKey2;
                            i5 = size;
                            i = i4;
                            i2 = i5;
                            addError(e.getErrorMessage(), i2);
                            if (x500Principal3 != null) {
                            }
                            c2 = 2;
                            c = 0;
                            i3 = 1;
                            if (i != this.n) {
                            }
                            X500Principal subjectX500Principal2 = x509Certificate4.getSubjectX500Principal();
                            publicKey4 = getNextWorkingKey(this.certs, i2);
                            AlgorithmIdentifier algorithmIdentifier22 = getAlgorithmIdentifier(publicKey4);
                            algorithmIdentifier22.getAlgorithm();
                            algorithmIdentifier22.getParameters();
                            size = i2 - 1;
                            c3 = c2;
                            i6 = i3;
                            x509Certificate2 = x509Certificate4;
                            publicKey2 = publicKey4;
                            c4 = c;
                            x500Principal2 = subjectX500Principal2;
                        }
                    } else {
                        x509Certificate4 = x509Certificate3;
                        i = i7;
                        x500Principal3 = x500Principal2;
                        publicKey3 = publicKey2;
                        i2 = size;
                    }
                    if (x500Principal3 != null || x509Certificate4.getIssuerX500Principal().equals(x500Principal3)) {
                        c2 = 2;
                        c = 0;
                        i3 = 1;
                    } else {
                        c2 = 2;
                        c = 0;
                        i3 = 1;
                        addError(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.certWrongIssuer", new Object[]{x500Principal3.getName(), x509Certificate4.getIssuerX500Principal().getName()}), i2);
                    }
                    if (i != this.n) {
                        if (x509Certificate4 != null && x509Certificate4.getVersion() == i3) {
                            addError(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.noCACert"), i2);
                        }
                        try {
                            BasicConstraints instance2 = BasicConstraints.getInstance(getExtensionValue(x509Certificate4, BASIC_CONSTRAINTS));
                            if (instance2 != null) {
                                if (!instance2.isCA()) {
                                    errorBundle = new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.noCACert");
                                }
                                boolean[] keyUsage2 = x509Certificate4.getKeyUsage();
                                if (keyUsage2 != null && !keyUsage2[5]) {
                                    addError(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.noCertSign"), i2);
                                }
                            } else {
                                errorBundle = new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.noBasicConstraints");
                            }
                            addError(errorBundle, i2);
                        } catch (AnnotatedException e15) {
                            addError(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.errorProcesingBC"), i2);
                        }
                        boolean[] keyUsage22 = x509Certificate4.getKeyUsage();
                        addError(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.noCertSign"), i2);
                    }
                    X500Principal subjectX500Principal22 = x509Certificate4.getSubjectX500Principal();
                    try {
                        publicKey4 = getNextWorkingKey(this.certs, i2);
                        AlgorithmIdentifier algorithmIdentifier222 = getAlgorithmIdentifier(publicKey4);
                        algorithmIdentifier222.getAlgorithm();
                        algorithmIdentifier222.getParameters();
                    } catch (CertPathValidatorException e16) {
                        publicKey4 = publicKey3;
                        addError(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.pubKeyError"), i2);
                        size = i2 - 1;
                        c3 = c2;
                        i6 = i3;
                        x509Certificate2 = x509Certificate4;
                        publicKey2 = publicKey4;
                        c4 = c;
                        x500Principal2 = subjectX500Principal22;
                    }
                    size = i2 - 1;
                    c3 = c2;
                    i6 = i3;
                    x509Certificate2 = x509Certificate4;
                    publicKey2 = publicKey4;
                    c4 = c;
                    x500Principal2 = subjectX500Principal22;
                }
                this.trustAnchor = trustAnchor3;
                this.subjectPublicKey = publicKey2;
                return;
            }
            trustAnchor2 = null;
        } catch (CertPathReviewerException e17) {
            e = e17;
            trustAnchor2 = null;
            addError(e.getErrorMessage());
            trustAnchor3 = trustAnchor2;
            if (trustAnchor3 != null) {
            }
            if (trustAnchor3 != null) {
            }
            size = this.certs.size() - 1;
            x500Principal2 = x500Principal;
            x509Certificate2 = x509Certificate;
            publicKey2 = publicKey;
            while (size >= 0) {
            }
            this.trustAnchor = trustAnchor3;
            this.subjectPublicKey = publicKey2;
            return;
        } catch (Throwable th2) {
            th = th2;
            trustAnchor2 = null;
            addError(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.unknown", new Object[]{new UntrustedInput(th.getMessage()), new UntrustedInput(th)}));
            trustAnchor3 = trustAnchor2;
            if (trustAnchor3 != null) {
            }
            if (trustAnchor3 != null) {
            }
            size = this.certs.size() - 1;
            x500Principal2 = x500Principal;
            x509Certificate2 = x509Certificate;
            publicKey2 = publicKey;
            while (size >= 0) {
            }
            this.trustAnchor = trustAnchor3;
            this.subjectPublicKey = publicKey2;
            return;
        }
        trustAnchor3 = trustAnchor2;
        if (trustAnchor3 != null) {
        }
        if (trustAnchor3 != null) {
        }
        size = this.certs.size() - 1;
        x500Principal2 = x500Principal;
        x509Certificate2 = x509Certificate;
        publicKey2 = publicKey;
        while (size >= 0) {
        }
        this.trustAnchor = trustAnchor3;
        this.subjectPublicKey = publicKey2;
        return;
        addError(errorBundle3, size);
        x509Certificate3.checkValidity(this.validDate);
        if (this.pkixParams.isRevocationEnabled()) {
        }
        if (x500Principal3 != null) {
        }
        c2 = 2;
        c = 0;
        i3 = 1;
        if (i != this.n) {
        }
        X500Principal subjectX500Principal222 = x509Certificate4.getSubjectX500Principal();
        publicKey4 = getNextWorkingKey(this.certs, i2);
        AlgorithmIdentifier algorithmIdentifier2222 = getAlgorithmIdentifier(publicKey4);
        algorithmIdentifier2222.getAlgorithm();
        algorithmIdentifier2222.getParameters();
        size = i2 - 1;
        c3 = c2;
        i6 = i3;
        x509Certificate2 = x509Certificate4;
        publicKey2 = publicKey4;
        c4 = c;
        x500Principal2 = subjectX500Principal222;
        addError(errorBundle2, size);
        if (this.pkixParams.isRevocationEnabled()) {
        }
        if (x500Principal3 != null) {
        }
        c2 = 2;
        c = 0;
        i3 = 1;
        if (i != this.n) {
        }
        X500Principal subjectX500Principal2222 = x509Certificate4.getSubjectX500Principal();
        publicKey4 = getNextWorkingKey(this.certs, i2);
        AlgorithmIdentifier algorithmIdentifier22222 = getAlgorithmIdentifier(publicKey4);
        algorithmIdentifier22222.getAlgorithm();
        algorithmIdentifier22222.getParameters();
        size = i2 - 1;
        c3 = c2;
        i6 = i3;
        x509Certificate2 = x509Certificate4;
        publicKey2 = publicKey4;
        c4 = c;
        x500Principal2 = subjectX500Principal2222;
    }

    private X509CRL getCRL(String str) throws CertPathReviewerException {
        try {
            URL url = new URL(str);
            if (!url.getProtocol().equals("http")) {
                if (!url.getProtocol().equals("https")) {
                    return null;
                }
            }
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setUseCaches(false);
            httpURLConnection.setDoInput(true);
            httpURLConnection.connect();
            if (httpURLConnection.getResponseCode() == 200) {
                return (X509CRL) CertificateFactory.getInstance("X.509", "BC").generateCRL(httpURLConnection.getInputStream());
            }
            throw new Exception(httpURLConnection.getResponseMessage());
        } catch (Exception e) {
            throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.loadCrlDistPointError", new Object[]{new UntrustedInput(str), e.getMessage(), e, e.getClass().getName()}));
        }
    }

    private boolean processQcStatements(X509Certificate x509Certificate, int i) {
        ErrorBundle errorBundle;
        ErrorBundle errorBundle2;
        int i2 = i;
        char c = 0;
        try {
            ASN1Sequence aSN1Sequence = (ASN1Sequence) getExtensionValue(x509Certificate, QC_STATEMENT);
            int i3 = 0;
            boolean z = false;
            while (i3 < aSN1Sequence.size()) {
                QCStatement instance = QCStatement.getInstance(aSN1Sequence.getObjectAt(i3));
                if (QCStatement.id_etsi_qcs_QcCompliance.equals(instance.getStatementId())) {
                    errorBundle2 = new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.QcEuCompliance");
                } else {
                    if (!QCStatement.id_qcs_pkixQCSyntax_v1.equals(instance.getStatementId())) {
                        if (QCStatement.id_etsi_qcs_QcSSCD.equals(instance.getStatementId())) {
                            errorBundle2 = new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.QcSSCD");
                        } else if (QCStatement.id_etsi_qcs_LimiteValue.equals(instance.getStatementId())) {
                            MonetaryValue instance2 = MonetaryValue.getInstance(instance.getStatementInfo());
                            instance2.getCurrency();
                            double doubleValue = instance2.getAmount().doubleValue() * Math.pow(10.0d, instance2.getExponent().doubleValue());
                            if (instance2.getCurrency().isAlphabetic()) {
                                Object[] objArr = new Object[3];
                                objArr[c] = instance2.getCurrency().getAlphabetic();
                                objArr[1] = new TrustedInput(new Double(doubleValue));
                                objArr[2] = instance2;
                                errorBundle = new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.QcLimitValueAlpha", objArr);
                            } else {
                                errorBundle = new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.QcLimitValueNum", new Object[]{Integers.valueOf(instance2.getCurrency().getNumeric()), new TrustedInput(new Double(doubleValue)), instance2});
                            }
                            addNotification(errorBundle, i2);
                        } else {
                            addNotification(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.QcUnknownStatement", new Object[]{instance.getStatementId(), new UntrustedInput(instance)}), i2);
                            z = true;
                        }
                    }
                    i3++;
                    c = 0;
                }
                addNotification(errorBundle2, i2);
                i3++;
                c = 0;
            }
            return !z;
        } catch (AnnotatedException e) {
            addError(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.QcStatementExtError"), i2);
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public void addError(ErrorBundle errorBundle) {
        this.errors[0].add(errorBundle);
    }

    /* access modifiers changed from: protected */
    public void addError(ErrorBundle errorBundle, int i) {
        if (i < -1 || i >= this.n) {
            throw new IndexOutOfBoundsException();
        }
        this.errors[i + 1].add(errorBundle);
    }

    /* access modifiers changed from: protected */
    public void addNotification(ErrorBundle errorBundle) {
        this.notifications[0].add(errorBundle);
    }

    /* access modifiers changed from: protected */
    public void addNotification(ErrorBundle errorBundle, int i) {
        if (i < -1 || i >= this.n) {
            throw new IndexOutOfBoundsException();
        }
        this.notifications[i + 1].add(errorBundle);
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:101:0x02a3  */
    /* JADX WARNING: Removed duplicated region for block: B:102:0x02c2  */
    /* JADX WARNING: Removed duplicated region for block: B:98:0x028c  */
    public void checkCRLs(PKIXParameters pKIXParameters, X509Certificate x509Certificate, Date date, X509Certificate x509Certificate2, PublicKey publicKey, Vector vector, int i) throws CertPathReviewerException {
        Iterator it;
        X509CRL x509crl;
        boolean z;
        boolean z2;
        boolean z3;
        boolean z4;
        String str;
        PKIXParameters pKIXParameters2 = pKIXParameters;
        X509Certificate x509Certificate3 = x509Certificate;
        PublicKey publicKey2 = publicKey;
        int i2 = i;
        X509CRLStoreSelector x509CRLStoreSelector = new X509CRLStoreSelector();
        try {
            x509CRLStoreSelector.addIssuerName(getEncodedIssuerPrincipal(x509Certificate).getEncoded());
            x509CRLStoreSelector.setCertificateChecking(x509Certificate3);
            char c = 0;
            try {
                Set findCRLs = CRL_UTIL.findCRLs(x509CRLStoreSelector, pKIXParameters2);
                it = findCRLs.iterator();
                if (findCRLs.isEmpty()) {
                    ArrayList arrayList = new ArrayList();
                    for (X509CRL issuerX500Principal : CRL_UTIL.findCRLs(new X509CRLStoreSelector(), pKIXParameters2)) {
                        arrayList.add(issuerX500Principal.getIssuerX500Principal());
                    }
                    addNotification(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.noCrlInCertstore", new Object[]{new UntrustedInput(x509CRLStoreSelector.getIssuerNames()), new UntrustedInput(arrayList), Integers.valueOf(arrayList.size())}), i2);
                }
            } catch (AnnotatedException e) {
                addError(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.crlExtractionError", new Object[]{e.getCause().getMessage(), e.getCause(), e.getCause().getClass().getName()}), i2);
                it = new ArrayList().iterator();
            }
            X509CRL x509crl2 = null;
            while (true) {
                if (!it.hasNext()) {
                    x509crl = x509crl2;
                    z = false;
                    break;
                }
                x509crl2 = (X509CRL) it.next();
                if (x509crl2.getNextUpdate() == null || pKIXParameters.getDate().before(x509crl2.getNextUpdate())) {
                    addNotification(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.localValidCRL", new Object[]{new TrustedInput(x509crl2.getThisUpdate()), new TrustedInput(x509crl2.getNextUpdate())}), i2);
                    x509crl = x509crl2;
                    z = true;
                } else {
                    addNotification(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.localInvalidCRL", new Object[]{new TrustedInput(x509crl2.getThisUpdate()), new TrustedInput(x509crl2.getNextUpdate())}), i2);
                }
            }
            addNotification(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.localValidCRL", new Object[]{new TrustedInput(x509crl2.getThisUpdate()), new TrustedInput(x509crl2.getNextUpdate())}), i2);
            x509crl = x509crl2;
            z = true;
            if (!z) {
                Iterator it2 = vector.iterator();
                z2 = z;
                while (true) {
                    if (!it2.hasNext()) {
                        break;
                    }
                    try {
                        String str2 = (String) it2.next();
                        X509CRL crl = getCRL(str2);
                        if (crl != null) {
                            if (!x509Certificate.getIssuerX500Principal().equals(crl.getIssuerX500Principal())) {
                                try {
                                    Object[] objArr = new Object[3];
                                    objArr[c] = new UntrustedInput(crl.getIssuerX500Principal().getName());
                                    objArr[1] = new UntrustedInput(x509Certificate.getIssuerX500Principal().getName());
                                    objArr[2] = new UntrustedUrlInput(str2);
                                    addNotification(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.onlineCRLWrongCA", objArr), i2);
                                } catch (CertPathReviewerException e2) {
                                    e = e2;
                                    addNotification(e.getErrorMessage(), i2);
                                    c = 0;
                                }
                            } else {
                                if (crl.getNextUpdate() != null) {
                                    if (!this.pkixParams.getDate().before(crl.getNextUpdate())) {
                                        try {
                                            Object[] objArr2 = new Object[3];
                                            objArr2[0] = new TrustedInput(crl.getThisUpdate());
                                            objArr2[1] = new TrustedInput(crl.getNextUpdate());
                                            objArr2[2] = new UntrustedUrlInput(str2);
                                            addNotification(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.onlineInvalidCRL", objArr2), i2);
                                        } catch (CertPathReviewerException e3) {
                                            e = e3;
                                            addNotification(e.getErrorMessage(), i2);
                                            c = 0;
                                        }
                                    }
                                }
                                try {
                                    try {
                                        addNotification(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.onlineValidCRL", new Object[]{new TrustedInput(crl.getThisUpdate()), new TrustedInput(crl.getNextUpdate()), new UntrustedUrlInput(str2)}), i2);
                                        x509crl = crl;
                                        z2 = true;
                                        break;
                                    } catch (CertPathReviewerException e4) {
                                        e = e4;
                                    }
                                } catch (CertPathReviewerException e5) {
                                    e = e5;
                                    z2 = true;
                                    addNotification(e.getErrorMessage(), i2);
                                    c = 0;
                                }
                            }
                        }
                    } catch (CertPathReviewerException e6) {
                        e = e6;
                    }
                    c = 0;
                }
            } else {
                z2 = z;
            }
            if (x509crl != null) {
                if (x509Certificate2 != null) {
                    boolean[] keyUsage = x509Certificate2.getKeyUsage();
                    if (keyUsage != null && (keyUsage.length < 7 || !keyUsage[6])) {
                        throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.noCrlSigningPermited"));
                    }
                }
                if (publicKey2 != null) {
                    try {
                        x509crl.verify(publicKey2, "BC");
                        X509CRLEntry revokedCertificate = x509crl.getRevokedCertificate(x509Certificate.getSerialNumber());
                        if (revokedCertificate != null) {
                            if (revokedCertificate.hasExtensions()) {
                                try {
                                    ASN1Enumerated instance = ASN1Enumerated.getInstance(getExtensionValue(revokedCertificate, Extension.reasonCode.getId()));
                                    if (instance != null) {
                                        str = crlReasons[instance.getValue().intValue()];
                                        if (str == null) {
                                            str = crlReasons[7];
                                        }
                                        LocaleString localeString = new LocaleString(RESOURCE_NAME, str);
                                        if (!date.before(revokedCertificate.getRevocationDate())) {
                                            addNotification(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.revokedAfterValidation", new Object[]{new TrustedInput(revokedCertificate.getRevocationDate()), localeString}), i2);
                                        } else {
                                            throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.certRevoked", new Object[]{new TrustedInput(revokedCertificate.getRevocationDate()), localeString}));
                                        }
                                    }
                                } catch (AnnotatedException e7) {
                                    throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.crlReasonExtError"), e7);
                                }
                            }
                            str = null;
                            if (str == null) {
                            }
                            LocaleString localeString2 = new LocaleString(RESOURCE_NAME, str);
                            if (!date.before(revokedCertificate.getRevocationDate())) {
                            }
                        } else {
                            addNotification(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.notRevoked"), i2);
                        }
                        if (x509crl.getNextUpdate() == null || !x509crl.getNextUpdate().before(this.pkixParams.getDate())) {
                            z4 = false;
                            z3 = true;
                        } else {
                            z3 = true;
                            z4 = false;
                            addNotification(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.crlUpdateAvailable", new Object[]{new TrustedInput(x509crl.getNextUpdate())}), i2);
                        }
                        try {
                            ASN1Primitive extensionValue = getExtensionValue(x509crl, ISSUING_DISTRIBUTION_POINT);
                            try {
                                ASN1Primitive extensionValue2 = getExtensionValue(x509crl, DELTA_CRL_INDICATOR);
                                if (extensionValue2 != null) {
                                    X509CRLStoreSelector x509CRLStoreSelector2 = new X509CRLStoreSelector();
                                    try {
                                        x509CRLStoreSelector2.addIssuerName(getIssuerPrincipal(x509crl).getEncoded());
                                        x509CRLStoreSelector2.setMinCRLNumber(((ASN1Integer) extensionValue2).getPositiveValue());
                                        try {
                                            x509CRLStoreSelector2.setMaxCRLNumber(((ASN1Integer) getExtensionValue(x509crl, CRL_NUMBER)).getPositiveValue().subtract(BigInteger.valueOf(1)));
                                            try {
                                                Iterator it3 = CRL_UTIL.findCRLs(x509CRLStoreSelector2, pKIXParameters2).iterator();
                                                while (true) {
                                                    if (!it3.hasNext()) {
                                                        z3 = z4;
                                                        break;
                                                    }
                                                    try {
                                                        ASN1Primitive extensionValue3 = getExtensionValue((X509CRL) it3.next(), ISSUING_DISTRIBUTION_POINT);
                                                        if (extensionValue == null) {
                                                            if (extensionValue3 == null) {
                                                                break;
                                                            }
                                                        } else if (extensionValue.equals(extensionValue3)) {
                                                            break;
                                                        }
                                                    } catch (AnnotatedException e8) {
                                                        throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.distrPtExtError"), e8);
                                                    }
                                                }
                                                if (!z3) {
                                                    throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.noBaseCRL"));
                                                }
                                            } catch (AnnotatedException e9) {
                                                throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.crlExtractionError"), e9);
                                            }
                                        } catch (AnnotatedException e10) {
                                            throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.crlNbrExtError"), e10);
                                        }
                                    } catch (IOException e11) {
                                        throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.crlIssuerException"), e11);
                                    }
                                }
                                if (extensionValue != null) {
                                    IssuingDistributionPoint instance2 = IssuingDistributionPoint.getInstance(extensionValue);
                                    try {
                                        BasicConstraints instance3 = BasicConstraints.getInstance(getExtensionValue(x509Certificate3, BASIC_CONSTRAINTS));
                                        if (instance2.onlyContainsUserCerts() && instance3 != null && instance3.isCA()) {
                                            throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.crlOnlyUserCert"));
                                        } else if (instance2.onlyContainsCACerts() && (instance3 == null || !instance3.isCA())) {
                                            throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.crlOnlyCaCert"));
                                        } else if (instance2.onlyContainsAttributeCerts()) {
                                            throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.crlOnlyAttrCert"));
                                        }
                                    } catch (AnnotatedException e12) {
                                        throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.crlBCExtError"), e12);
                                    }
                                }
                            } catch (AnnotatedException e13) {
                                throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.deltaCrlExtError"));
                            }
                        } catch (AnnotatedException e14) {
                            throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.distrPtExtError"));
                        }
                    } catch (Exception e15) {
                        throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.crlVerifyFailed"), e15);
                    }
                } else {
                    throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.crlNoIssuerPublicKey"));
                }
            }
            if (!z2) {
                throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.noValidCrlFound"));
            }
        } catch (IOException e16) {
            throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.crlIssuerException"), e16);
        }
    }

    /* access modifiers changed from: protected */
    public void checkRevocation(PKIXParameters pKIXParameters, X509Certificate x509Certificate, Date date, X509Certificate x509Certificate2, PublicKey publicKey, Vector vector, Vector vector2, int i) throws CertPathReviewerException {
        checkCRLs(pKIXParameters, x509Certificate, date, x509Certificate2, publicKey, vector, i);
    }

    /* access modifiers changed from: protected */
    public void doChecks() {
        if (!this.initialized) {
            throw new IllegalStateException("Object not initialized. Call init() first.");
        } else if (this.notifications == null) {
            this.notifications = new List[(this.n + 1)];
            this.errors = new List[(this.n + 1)];
            for (int i = 0; i < this.notifications.length; i++) {
                this.notifications[i] = new ArrayList();
                this.errors[i] = new ArrayList();
            }
            checkSignatures();
            checkNameConstraints();
            checkPathLength();
            checkPolicy();
            checkCriticalExtensions();
        }
    }

    /* access modifiers changed from: protected */
    public Vector getCRLDistUrls(CRLDistPoint cRLDistPoint) {
        Vector vector = new Vector();
        if (cRLDistPoint != null) {
            DistributionPoint[] distributionPoints = cRLDistPoint.getDistributionPoints();
            for (DistributionPoint distributionPoint : distributionPoints) {
                DistributionPointName distributionPoint2 = distributionPoint.getDistributionPoint();
                if (distributionPoint2.getType() == 0) {
                    GeneralName[] names = GeneralNames.getInstance(distributionPoint2.getName()).getNames();
                    for (int i = 0; i < names.length; i++) {
                        if (names[i].getTagNo() == 6) {
                            vector.add(((DERIA5String) names[i].getName()).getString());
                        }
                    }
                }
            }
        }
        return vector;
    }

    public CertPath getCertPath() {
        return this.certPath;
    }

    public int getCertPathSize() {
        return this.n;
    }

    public List getErrors(int i) {
        doChecks();
        return this.errors[i + 1];
    }

    public List[] getErrors() {
        doChecks();
        return this.errors;
    }

    public List getNotifications(int i) {
        doChecks();
        return this.notifications[i + 1];
    }

    public List[] getNotifications() {
        doChecks();
        return this.notifications;
    }

    /* access modifiers changed from: protected */
    public Vector getOCSPUrls(AuthorityInformationAccess authorityInformationAccess) {
        Vector vector = new Vector();
        if (authorityInformationAccess != null) {
            AccessDescription[] accessDescriptions = authorityInformationAccess.getAccessDescriptions();
            for (int i = 0; i < accessDescriptions.length; i++) {
                if (accessDescriptions[i].getAccessMethod().equals(AccessDescription.id_ad_ocsp)) {
                    GeneralName accessLocation = accessDescriptions[i].getAccessLocation();
                    if (accessLocation.getTagNo() == 6) {
                        vector.add(((DERIA5String) accessLocation.getName()).getString());
                    }
                }
            }
        }
        return vector;
    }

    public PolicyNode getPolicyTree() {
        doChecks();
        return this.policyTree;
    }

    public PublicKey getSubjectPublicKey() {
        doChecks();
        return this.subjectPublicKey;
    }

    public TrustAnchor getTrustAnchor() {
        doChecks();
        return this.trustAnchor;
    }

    /* access modifiers changed from: protected */
    public Collection getTrustAnchors(X509Certificate x509Certificate, Set set) throws CertPathReviewerException {
        ArrayList arrayList = new ArrayList();
        Iterator it = set.iterator();
        X509CertSelector x509CertSelector = new X509CertSelector();
        try {
            x509CertSelector.setSubject(getEncodedIssuerPrincipal(x509Certificate).getEncoded());
            byte[] extensionValue = x509Certificate.getExtensionValue(Extension.authorityKeyIdentifier.getId());
            if (extensionValue != null) {
                AuthorityKeyIdentifier instance = AuthorityKeyIdentifier.getInstance(ASN1Primitive.fromByteArray(((ASN1OctetString) ASN1Primitive.fromByteArray(extensionValue)).getOctets()));
                x509CertSelector.setSerialNumber(instance.getAuthorityCertSerialNumber());
                byte[] keyIdentifier = instance.getKeyIdentifier();
                if (keyIdentifier != null) {
                    x509CertSelector.setSubjectKeyIdentifier(new DEROctetString(keyIdentifier).getEncoded());
                }
            }
            while (it.hasNext()) {
                TrustAnchor trustAnchor2 = (TrustAnchor) it.next();
                if (trustAnchor2.getTrustedCert() != null) {
                    if (!x509CertSelector.match(trustAnchor2.getTrustedCert())) {
                    }
                } else if (trustAnchor2.getCAName() != null) {
                    if (trustAnchor2.getCAPublicKey() != null) {
                        if (!getEncodedIssuerPrincipal(x509Certificate).equals(new X500Principal(trustAnchor2.getCAName()))) {
                        }
                    }
                }
                arrayList.add(trustAnchor2);
            }
            return arrayList;
        } catch (IOException e) {
            throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.trustAnchorIssuerError"));
        }
    }

    public void init(CertPath certPath2, PKIXParameters pKIXParameters) throws CertPathReviewerException {
        if (!this.initialized) {
            this.initialized = true;
            if (certPath2 != null) {
                this.certPath = certPath2;
                this.certs = certPath2.getCertificates();
                this.n = this.certs.size();
                if (!this.certs.isEmpty()) {
                    this.pkixParams = (PKIXParameters) pKIXParameters.clone();
                    this.validDate = getValidDate(this.pkixParams);
                    this.notifications = null;
                    this.errors = null;
                    this.trustAnchor = null;
                    this.subjectPublicKey = null;
                    this.policyTree = null;
                    return;
                }
                throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.emptyCertPath"));
            }
            throw new NullPointerException("certPath was null");
        }
        throw new IllegalStateException("object is already initialized!");
    }

    public boolean isValidCertPath() {
        doChecks();
        for (List isEmpty : this.errors) {
            if (!isEmpty.isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
