package org.bouncycastle.x509;

import java.io.ByteArrayInputStream;
import java.io.IOException;
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
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.provider.PKIXNameConstraintValidator;
import org.bouncycastle.jce.provider.PKIXNameConstraintValidatorException;
import org.bouncycastle.jce.provider.PKIXPolicyNode;
import org.bouncycastle.jce.provider.RFC3280CertPathUtilities;
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
        List<PKIXCertPathChecker> certPathCheckers = this.pkixParams.getCertPathCheckers();
        for (PKIXCertPathChecker pKIXCertPathChecker : certPathCheckers) {
            try {
                pKIXCertPathChecker.init(false);
            } catch (CertPathValidatorException e) {
                throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.certPathCheckerError", new Object[]{e.getMessage(), e, e.getClass().getName()}), e);
            }
        }
        try {
            for (int size = this.certs.size() - 1; size >= 0; size--) {
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
                        for (PKIXCertPathChecker pKIXCertPathChecker2 : certPathCheckers) {
                            try {
                                pKIXCertPathChecker2.check(x509Certificate, criticalExtensionOIDs);
                            } catch (CertPathValidatorException e2) {
                                throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.criticalExtensionError", new Object[]{e2.getMessage(), e2, e2.getClass().getName()}), e2.getCause(), this.certPath, size);
                            }
                        }
                        if (!criticalExtensionOIDs.isEmpty()) {
                            Iterator<String> it = criticalExtensionOIDs.iterator();
                            while (it.hasNext()) {
                                addError(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.unknownCriticalExt", new Object[]{new ASN1ObjectIdentifier(it.next())}), size);
                            }
                        }
                    }
                }
            }
        } catch (CertPathReviewerException e3) {
            addError(e3.getErrorMessage(), e3.getIndex());
        }
    }

    private void checkNameConstraints() {
        PKIXNameConstraintValidator pKIXNameConstraintValidator = new PKIXNameConstraintValidator();
        try {
            for (int size = this.certs.size() - 1; size > 0; size--) {
                int i = this.n;
                X509Certificate x509Certificate = (X509Certificate) this.certs.get(size);
                if (!isSelfIssued(x509Certificate)) {
                    X500Principal subjectPrincipal = getSubjectPrincipal(x509Certificate);
                    try {
                        ASN1Sequence aSN1Sequence = (ASN1Sequence) new ASN1InputStream(new ByteArrayInputStream(subjectPrincipal.getEncoded())).readObject();
                        try {
                            pKIXNameConstraintValidator.checkPermittedDN(aSN1Sequence);
                            try {
                                pKIXNameConstraintValidator.checkExcludedDN(aSN1Sequence);
                                try {
                                    ASN1Sequence aSN1Sequence2 = (ASN1Sequence) getExtensionValue(x509Certificate, SUBJECT_ALTERNATIVE_NAME);
                                    if (aSN1Sequence2 != null) {
                                        for (int i2 = 0; i2 < aSN1Sequence2.size(); i2++) {
                                            GeneralName instance = GeneralName.getInstance(aSN1Sequence2.getObjectAt(i2));
                                            try {
                                                pKIXNameConstraintValidator.checkPermitted(instance);
                                                pKIXNameConstraintValidator.checkExcluded(instance);
                                            } catch (PKIXNameConstraintValidatorException e) {
                                                throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.notPermittedEmail", new Object[]{new UntrustedInput(instance)}), e, this.certPath, size);
                                            }
                                        }
                                    }
                                } catch (AnnotatedException e2) {
                                    throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.subjAltNameExtError"), e2, this.certPath, size);
                                }
                            } catch (PKIXNameConstraintValidatorException e3) {
                                throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.excludedDN", new Object[]{new UntrustedInput(subjectPrincipal.getName())}), e3, this.certPath, size);
                            }
                        } catch (PKIXNameConstraintValidatorException e4) {
                            throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.notPermittedDN", new Object[]{new UntrustedInput(subjectPrincipal.getName())}), e4, this.certPath, size);
                        }
                    } catch (IOException e5) {
                        throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.ncSubjectNameError", new Object[]{new UntrustedInput(subjectPrincipal)}), e5, this.certPath, size);
                    }
                }
                try {
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
                } catch (AnnotatedException e6) {
                    throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.ncExtError"), e6, this.certPath, size);
                }
            }
        } catch (CertPathReviewerException e7) {
            addError(e7.getErrorMessage(), e7.getIndex());
        }
    }

    private void checkPathLength() {
        BasicConstraints basicConstraints;
        BigInteger pathLenConstraint;
        int intValue;
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
            if (!(basicConstraints == null || (pathLenConstraint = basicConstraints.getPathLenConstraint()) == null || (intValue = pathLenConstraint.intValue()) >= i2)) {
                i2 = intValue;
            }
        }
        addNotification(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.totalPathLength", new Object[]{Integers.valueOf(i3)}));
    }

    /* JADX WARNING: Removed duplicated region for block: B:107:0x0236  */
    /* JADX WARNING: Removed duplicated region for block: B:96:0x020a  */
    private void checkPolicy() {
        int i;
        int i2;
        PKIXPolicyNode pKIXPolicyNode;
        int i3;
        int i4;
        String str;
        PKIXPolicyNode pKIXPolicyNode2;
        Set<String> set;
        String str2;
        int i5;
        PKIXPolicyNode pKIXPolicyNode3;
        int i6;
        int i7;
        String str3;
        HashSet hashSet;
        String str4;
        int i8;
        int i9;
        Set<String> criticalExtensionOIDs;
        String str5 = "CertPathReviewer.policyExtError";
        Set<String> initialPolicies = this.pkixParams.getInitialPolicies();
        ArrayList[] arrayListArr = new ArrayList[(this.n + 1)];
        for (int i10 = 0; i10 < arrayListArr.length; i10++) {
            arrayListArr[i10] = new ArrayList();
        }
        HashSet hashSet2 = new HashSet();
        hashSet2.add(RFC3280CertPathUtilities.ANY_POLICY);
        PKIXPolicyNode pKIXPolicyNode4 = new PKIXPolicyNode(new ArrayList(), 0, hashSet2, null, new HashSet(), RFC3280CertPathUtilities.ANY_POLICY, false);
        arrayListArr[0].add(pKIXPolicyNode4);
        int i11 = this.pkixParams.isExplicitPolicyRequired() ? 0 : this.n + 1;
        int i12 = this.pkixParams.isAnyPolicyInhibited() ? 0 : this.n + 1;
        int i13 = this.pkixParams.isPolicyMappingInhibited() ? 0 : this.n + 1;
        try {
            int size = this.certs.size() - 1;
            int i14 = i12;
            int i15 = i13;
            HashSet hashSet3 = null;
            int i16 = i11;
            X509Certificate x509Certificate = null;
            while (size >= 0) {
                int i17 = this.n - size;
                X509Certificate x509Certificate2 = (X509Certificate) this.certs.get(size);
                try {
                    ASN1Sequence aSN1Sequence = (ASN1Sequence) getExtensionValue(x509Certificate2, CERTIFICATE_POLICIES);
                    if (aSN1Sequence == null || pKIXPolicyNode4 == null) {
                        set = initialPolicies;
                        str = str5;
                        i4 = i14;
                        i3 = i15;
                        pKIXPolicyNode2 = pKIXPolicyNode4;
                    } else {
                        Enumeration objects = aSN1Sequence.getObjects();
                        set = initialPolicies;
                        HashSet hashSet4 = new HashSet();
                        while (objects.hasMoreElements()) {
                            PolicyInformation instance = PolicyInformation.getInstance(objects.nextElement());
                            ASN1ObjectIdentifier policyIdentifier = instance.getPolicyIdentifier();
                            hashSet4.add(policyIdentifier.getId());
                            if (!RFC3280CertPathUtilities.ANY_POLICY.equals(policyIdentifier.getId())) {
                                try {
                                    Set qualifierSet = getQualifierSet(instance.getPolicyQualifiers());
                                    if (!processCertD1i(i17, arrayListArr, policyIdentifier, qualifierSet)) {
                                        processCertD1ii(i17, arrayListArr, policyIdentifier, qualifierSet);
                                    }
                                } catch (CertPathValidatorException e) {
                                    throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.policyQualifierError"), e, this.certPath, size);
                                }
                            }
                            pKIXPolicyNode4 = pKIXPolicyNode4;
                            str5 = str5;
                        }
                        str = str5;
                        pKIXPolicyNode2 = pKIXPolicyNode4;
                        if (hashSet3 != null && !hashSet3.contains(RFC3280CertPathUtilities.ANY_POLICY)) {
                            HashSet hashSet5 = new HashSet();
                            for (Object obj : hashSet3) {
                                if (hashSet4.contains(obj)) {
                                    hashSet5.add(obj);
                                }
                            }
                            hashSet4 = hashSet5;
                        }
                        if (i14 > 0 || (i17 < this.n && isSelfIssued(x509Certificate2))) {
                            Enumeration objects2 = aSN1Sequence.getObjects();
                            while (true) {
                                if (!objects2.hasMoreElements()) {
                                    break;
                                }
                                PolicyInformation instance2 = PolicyInformation.getInstance(objects2.nextElement());
                                if (RFC3280CertPathUtilities.ANY_POLICY.equals(instance2.getPolicyIdentifier().getId())) {
                                    try {
                                        Set qualifierSet2 = getQualifierSet(instance2.getPolicyQualifiers());
                                        ArrayList arrayList = arrayListArr[i17 - 1];
                                        hashSet = hashSet4;
                                        for (int i18 = 0; i18 < arrayList.size(); i18++) {
                                            PKIXPolicyNode pKIXPolicyNode5 = (PKIXPolicyNode) arrayList.get(i18);
                                            for (Object obj2 : pKIXPolicyNode5.getExpectedPolicies()) {
                                                if (obj2 instanceof String) {
                                                    str4 = (String) obj2;
                                                } else if (obj2 instanceof ASN1ObjectIdentifier) {
                                                    str4 = ((ASN1ObjectIdentifier) obj2).getId();
                                                } else {
                                                    arrayList = arrayList;
                                                    i14 = i14;
                                                }
                                                boolean z = false;
                                                for (Iterator children = pKIXPolicyNode5.getChildren(); children.hasNext(); children = children) {
                                                    if (str4.equals(((PKIXPolicyNode) children.next()).getValidPolicy())) {
                                                        z = true;
                                                    }
                                                }
                                                if (!z) {
                                                    HashSet hashSet6 = new HashSet();
                                                    hashSet6.add(str4);
                                                    i8 = i15;
                                                    PKIXPolicyNode pKIXPolicyNode6 = new PKIXPolicyNode(new ArrayList(), i17, hashSet6, pKIXPolicyNode5, qualifierSet2, str4, false);
                                                    pKIXPolicyNode5.addChild(pKIXPolicyNode6);
                                                    arrayListArr[i17].add(pKIXPolicyNode6);
                                                } else {
                                                    i8 = i15;
                                                }
                                                arrayList = arrayList;
                                                i14 = i14;
                                                i15 = i8;
                                            }
                                        }
                                    } catch (CertPathValidatorException e2) {
                                        throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.policyQualifierError"), e2, this.certPath, size);
                                    }
                                }
                            }
                            i4 = i14;
                            i3 = i15;
                            for (i9 = i17 - 1; i9 >= 0; i9--) {
                                ArrayList arrayList2 = arrayListArr[i9];
                                PKIXPolicyNode pKIXPolicyNode7 = pKIXPolicyNode2;
                                for (int i19 = 0; i19 < arrayList2.size(); i19++) {
                                    PKIXPolicyNode pKIXPolicyNode8 = (PKIXPolicyNode) arrayList2.get(i19);
                                    if (!pKIXPolicyNode8.hasChildren() && (pKIXPolicyNode7 = removePolicyNode(pKIXPolicyNode7, arrayListArr, pKIXPolicyNode8)) == null) {
                                        break;
                                    }
                                }
                                pKIXPolicyNode2 = pKIXPolicyNode7;
                            }
                            criticalExtensionOIDs = x509Certificate2.getCriticalExtensionOIDs();
                            if (criticalExtensionOIDs != null) {
                                boolean contains = criticalExtensionOIDs.contains(CERTIFICATE_POLICIES);
                                ArrayList arrayList3 = arrayListArr[i17];
                                for (int i20 = 0; i20 < arrayList3.size(); i20++) {
                                    ((PKIXPolicyNode) arrayList3.get(i20)).setCritical(contains);
                                }
                            }
                            hashSet3 = hashSet;
                        }
                        hashSet = hashSet4;
                        i4 = i14;
                        i3 = i15;
                        while (i9 >= 0) {
                        }
                        criticalExtensionOIDs = x509Certificate2.getCriticalExtensionOIDs();
                        if (criticalExtensionOIDs != null) {
                        }
                        hashSet3 = hashSet;
                    }
                    if (aSN1Sequence == null) {
                        pKIXPolicyNode2 = null;
                    }
                    if (i16 > 0 || pKIXPolicyNode2 != null) {
                        if (i17 != this.n) {
                            try {
                                ASN1Primitive extensionValue = getExtensionValue(x509Certificate2, POLICY_MAPPINGS);
                                if (extensionValue != null) {
                                    ASN1Sequence aSN1Sequence2 = (ASN1Sequence) extensionValue;
                                    for (int i21 = 0; i21 < aSN1Sequence2.size(); i21++) {
                                        ASN1Sequence aSN1Sequence3 = (ASN1Sequence) aSN1Sequence2.getObjectAt(i21);
                                        ASN1ObjectIdentifier aSN1ObjectIdentifier = (ASN1ObjectIdentifier) aSN1Sequence3.getObjectAt(1);
                                        if (RFC3280CertPathUtilities.ANY_POLICY.equals(((ASN1ObjectIdentifier) aSN1Sequence3.getObjectAt(0)).getId())) {
                                            throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.invalidPolicyMapping"), this.certPath, size);
                                        } else if (RFC3280CertPathUtilities.ANY_POLICY.equals(aSN1ObjectIdentifier.getId())) {
                                            throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.invalidPolicyMapping"), this.certPath, size);
                                        }
                                    }
                                }
                                if (extensionValue != null) {
                                    HashMap hashMap = new HashMap();
                                    HashSet<String> hashSet7 = new HashSet();
                                    int i22 = 0;
                                    for (ASN1Sequence aSN1Sequence4 = (ASN1Sequence) extensionValue; i22 < aSN1Sequence4.size(); aSN1Sequence4 = aSN1Sequence4) {
                                        ASN1Sequence aSN1Sequence5 = (ASN1Sequence) aSN1Sequence4.getObjectAt(i22);
                                        String id = ((ASN1ObjectIdentifier) aSN1Sequence5.getObjectAt(0)).getId();
                                        String id2 = ((ASN1ObjectIdentifier) aSN1Sequence5.getObjectAt(1)).getId();
                                        if (!hashMap.containsKey(id)) {
                                            HashSet hashSet8 = new HashSet();
                                            hashSet8.add(id2);
                                            hashMap.put(id, hashSet8);
                                            hashSet7.add(id);
                                        } else {
                                            ((Set) hashMap.get(id)).add(id2);
                                        }
                                        i22++;
                                    }
                                    pKIXPolicyNode3 = pKIXPolicyNode2;
                                    for (String str6 : hashSet7) {
                                        if (i3 > 0) {
                                            try {
                                                prepareNextCertB1(i17, arrayListArr, str6, hashMap, x509Certificate2);
                                                str3 = str;
                                            } catch (AnnotatedException e3) {
                                                throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, str), e3, this.certPath, size);
                                            } catch (CertPathValidatorException e4) {
                                                throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.policyQualifierError"), e4, this.certPath, size);
                                            }
                                        } else {
                                            str3 = str;
                                            if (i3 <= 0) {
                                                pKIXPolicyNode3 = prepareNextCertB2(i17, arrayListArr, str6, pKIXPolicyNode3);
                                            }
                                        }
                                        str = str3;
                                    }
                                    str2 = str;
                                } else {
                                    str2 = str;
                                    pKIXPolicyNode3 = pKIXPolicyNode2;
                                }
                                if (!isSelfIssued(x509Certificate2)) {
                                    if (i16 != 0) {
                                        i16--;
                                    }
                                    i5 = i3 != 0 ? i3 - 1 : i3;
                                    i6 = i4 != 0 ? i4 - 1 : i4;
                                } else {
                                    i6 = i4;
                                    i5 = i3;
                                }
                                try {
                                    ASN1Sequence aSN1Sequence6 = (ASN1Sequence) getExtensionValue(x509Certificate2, POLICY_CONSTRAINTS);
                                    if (aSN1Sequence6 != null) {
                                        Enumeration objects3 = aSN1Sequence6.getObjects();
                                        while (objects3.hasMoreElements()) {
                                            ASN1TaggedObject aSN1TaggedObject = (ASN1TaggedObject) objects3.nextElement();
                                            int tagNo = aSN1TaggedObject.getTagNo();
                                            if (tagNo == 0) {
                                                int intValueExact = ASN1Integer.getInstance(aSN1TaggedObject, false).intValueExact();
                                                if (intValueExact < i16) {
                                                    i16 = intValueExact;
                                                }
                                            } else if (tagNo == 1) {
                                                int intValueExact2 = ASN1Integer.getInstance(aSN1TaggedObject, false).intValueExact();
                                                if (intValueExact2 < i5) {
                                                    i5 = intValueExact2;
                                                }
                                            }
                                        }
                                    }
                                    try {
                                        ASN1Integer aSN1Integer = (ASN1Integer) getExtensionValue(x509Certificate2, INHIBIT_ANY_POLICY);
                                        if (aSN1Integer == null || (i7 = aSN1Integer.intValueExact()) >= i6) {
                                            i7 = i6;
                                        }
                                        i14 = i7;
                                        pKIXPolicyNode4 = pKIXPolicyNode3;
                                    } catch (AnnotatedException e5) {
                                        throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.policyInhibitExtError"), this.certPath, size);
                                    }
                                } catch (AnnotatedException e6) {
                                    throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.policyConstExtError"), this.certPath, size);
                                }
                            } catch (AnnotatedException e7) {
                                throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.policyMapExtError"), e7, this.certPath, size);
                            }
                        } else {
                            str2 = str;
                            pKIXPolicyNode4 = pKIXPolicyNode2;
                            i14 = i4;
                            i5 = i3;
                        }
                        size--;
                        x509Certificate = x509Certificate2;
                        str5 = str2;
                        i15 = i5;
                        initialPolicies = set;
                    } else {
                        throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.noValidPolicyTree"));
                    }
                } catch (AnnotatedException e8) {
                    throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, str5), e8, this.certPath, size);
                }
            }
            Set<String> set2 = initialPolicies;
            PKIXPolicyNode pKIXPolicyNode9 = pKIXPolicyNode4;
            if (!isSelfIssued(x509Certificate) && i16 > 0) {
                i16--;
            }
            try {
                ASN1Sequence aSN1Sequence7 = (ASN1Sequence) getExtensionValue(x509Certificate, POLICY_CONSTRAINTS);
                if (aSN1Sequence7 != null) {
                    Enumeration objects4 = aSN1Sequence7.getObjects();
                    i2 = i16;
                    while (objects4.hasMoreElements()) {
                        ASN1TaggedObject aSN1TaggedObject2 = (ASN1TaggedObject) objects4.nextElement();
                        if (aSN1TaggedObject2.getTagNo() == 0) {
                            if (ASN1Integer.getInstance(aSN1TaggedObject2, false).intValueExact() == 0) {
                                i2 = 0;
                            }
                        }
                    }
                    i = 0;
                } else {
                    i = 0;
                    i2 = i16;
                }
                if (pKIXPolicyNode9 == null) {
                    if (!this.pkixParams.isExplicitPolicyRequired()) {
                        pKIXPolicyNode = null;
                    } else {
                        throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.explicitPolicy"), this.certPath, size);
                    }
                } else if (isAnyPolicy(set2)) {
                    if (this.pkixParams.isExplicitPolicyRequired()) {
                        if (!hashSet3.isEmpty()) {
                            HashSet<PKIXPolicyNode> hashSet9 = new HashSet();
                            for (int i23 = i; i23 < arrayListArr.length; i23++) {
                                ArrayList arrayList4 = arrayListArr[i23];
                                for (int i24 = i; i24 < arrayList4.size(); i24++) {
                                    PKIXPolicyNode pKIXPolicyNode10 = (PKIXPolicyNode) arrayList4.get(i24);
                                    if (RFC3280CertPathUtilities.ANY_POLICY.equals(pKIXPolicyNode10.getValidPolicy())) {
                                        Iterator children2 = pKIXPolicyNode10.getChildren();
                                        while (children2.hasNext()) {
                                            hashSet9.add(children2.next());
                                        }
                                    }
                                }
                            }
                            for (PKIXPolicyNode pKIXPolicyNode11 : hashSet9) {
                                hashSet3.contains(pKIXPolicyNode11.getValidPolicy());
                            }
                            int i25 = this.n - 1;
                            while (i25 >= 0) {
                                ArrayList arrayList5 = arrayListArr[i25];
                                PKIXPolicyNode pKIXPolicyNode12 = pKIXPolicyNode9;
                                for (int i26 = i; i26 < arrayList5.size(); i26++) {
                                    PKIXPolicyNode pKIXPolicyNode13 = (PKIXPolicyNode) arrayList5.get(i26);
                                    if (!pKIXPolicyNode13.hasChildren()) {
                                        pKIXPolicyNode12 = removePolicyNode(pKIXPolicyNode12, arrayListArr, pKIXPolicyNode13);
                                    }
                                }
                                i25--;
                                pKIXPolicyNode9 = pKIXPolicyNode12;
                            }
                        } else {
                            throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.explicitPolicy"), this.certPath, size);
                        }
                    }
                    pKIXPolicyNode = pKIXPolicyNode9;
                } else {
                    HashSet<PKIXPolicyNode> hashSet10 = new HashSet();
                    for (int i27 = i; i27 < arrayListArr.length; i27++) {
                        ArrayList arrayList6 = arrayListArr[i27];
                        for (int i28 = i; i28 < arrayList6.size(); i28++) {
                            PKIXPolicyNode pKIXPolicyNode14 = (PKIXPolicyNode) arrayList6.get(i28);
                            if (RFC3280CertPathUtilities.ANY_POLICY.equals(pKIXPolicyNode14.getValidPolicy())) {
                                Iterator children3 = pKIXPolicyNode14.getChildren();
                                while (children3.hasNext()) {
                                    PKIXPolicyNode pKIXPolicyNode15 = (PKIXPolicyNode) children3.next();
                                    if (!RFC3280CertPathUtilities.ANY_POLICY.equals(pKIXPolicyNode15.getValidPolicy())) {
                                        hashSet10.add(pKIXPolicyNode15);
                                    }
                                }
                            }
                        }
                    }
                    PKIXPolicyNode pKIXPolicyNode16 = pKIXPolicyNode9;
                    for (PKIXPolicyNode pKIXPolicyNode17 : hashSet10) {
                        if (!set2.contains(pKIXPolicyNode17.getValidPolicy())) {
                            pKIXPolicyNode16 = removePolicyNode(pKIXPolicyNode16, arrayListArr, pKIXPolicyNode17);
                        }
                        set2 = set2;
                    }
                    if (pKIXPolicyNode16 != null) {
                        int i29 = this.n - 1;
                        while (i29 >= 0) {
                            ArrayList arrayList7 = arrayListArr[i29];
                            PKIXPolicyNode pKIXPolicyNode18 = pKIXPolicyNode16;
                            for (int i30 = i; i30 < arrayList7.size(); i30++) {
                                PKIXPolicyNode pKIXPolicyNode19 = (PKIXPolicyNode) arrayList7.get(i30);
                                if (!pKIXPolicyNode19.hasChildren()) {
                                    pKIXPolicyNode18 = removePolicyNode(pKIXPolicyNode18, arrayListArr, pKIXPolicyNode19);
                                }
                            }
                            i29--;
                            pKIXPolicyNode16 = pKIXPolicyNode18;
                        }
                    }
                    pKIXPolicyNode = pKIXPolicyNode16;
                }
                if (i2 <= 0 && pKIXPolicyNode == null) {
                    throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.invalidPolicy"));
                }
            } catch (AnnotatedException e9) {
                throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.policyConstExtError"), this.certPath, size);
            }
        } catch (CertPathReviewerException e10) {
            addError(e10.getErrorMessage(), e10.getIndex());
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:103:0x02bd A[Catch:{ AnnotatedException -> 0x02c2 }] */
    /* JADX WARNING: Removed duplicated region for block: B:110:0x02e0 A[LOOP:1: B:108:0x02da->B:110:0x02e0, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:114:0x030a A[LOOP:2: B:112:0x0304->B:114:0x030a, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:125:0x0359  */
    /* JADX WARNING: Removed duplicated region for block: B:127:0x0363  */
    /* JADX WARNING: Removed duplicated region for block: B:133:0x0393  */
    /* JADX WARNING: Removed duplicated region for block: B:155:0x03f1  */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x00ff  */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x0148  */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x014b  */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x0172  */
    /* JADX WARNING: Removed duplicated region for block: B:60:0x0181  */
    /* JADX WARNING: Removed duplicated region for block: B:93:0x029a A[SYNTHETIC, Splitter:B:93:0x029a] */
    private void checkSignatures() {
        TrustAnchor trustAnchor2;
        TrustAnchor trustAnchor3;
        int i;
        X500Principal x500Principal;
        PublicKey publicKey;
        X509Certificate x509Certificate;
        int size;
        X500Principal x500Principal2;
        X509Certificate x509Certificate2;
        PublicKey publicKey2;
        int i2;
        TrustAnchor trustAnchor4;
        PublicKey publicKey3;
        int i3;
        X509Certificate x509Certificate3;
        X500Principal x500Principal3;
        int i4;
        char c;
        char c2;
        char c3;
        CRLDistPoint cRLDistPoint;
        AuthorityInformationAccess authorityInformationAccess;
        Iterator it;
        Iterator it2;
        int i5;
        CertPathReviewerException e;
        ASN1Primitive extensionValue;
        ErrorBundle errorBundle;
        AuthorityKeyIdentifier instance;
        GeneralNames authorityCertIssuer;
        ErrorBundle errorBundle2;
        X500Principal x500Principal4;
        boolean[] keyUsage;
        CertPathReviewerException e2;
        Throwable th;
        char c4 = 2;
        char c5 = 0;
        char c6 = 1;
        addNotification(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.certPathValidDate", new Object[]{new TrustedInput(this.validDate), new TrustedInput(new Date())}));
        try {
            X509Certificate x509Certificate4 = (X509Certificate) this.certs.get(this.certs.size() - 1);
            Collection trustAnchors = getTrustAnchors(x509Certificate4, this.pkixParams.getTrustAnchors());
            if (trustAnchors.size() > 1) {
                addError(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.conflictingTrustAnchors", new Object[]{Integers.valueOf(trustAnchors.size()), new UntrustedInput(x509Certificate4.getIssuerX500Principal())}));
            } else if (trustAnchors.isEmpty()) {
                addError(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.noTrustAnchorFound", new Object[]{new UntrustedInput(x509Certificate4.getIssuerX500Principal()), Integers.valueOf(this.pkixParams.getTrustAnchors().size())}));
            } else {
                trustAnchor2 = (TrustAnchor) trustAnchors.iterator().next();
                try {
                    try {
                        CertPathValidatorUtilities.verifyX509Certificate(x509Certificate4, trustAnchor2.getTrustedCert() != null ? trustAnchor2.getTrustedCert().getPublicKey() : trustAnchor2.getCAPublicKey(), this.pkixParams.getSigProvider());
                    } catch (SignatureException e3) {
                        addError(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.trustButInvalidCert"));
                    } catch (Exception e4) {
                    }
                } catch (CertPathReviewerException e5) {
                    e2 = e5;
                    addError(e2.getErrorMessage());
                    trustAnchor3 = trustAnchor2;
                    i = 5;
                    if (trustAnchor3 == null) {
                    }
                    if (trustAnchor3 == null) {
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
                    addError(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.unknown", new Object[]{new UntrustedInput(th.getMessage()), new UntrustedInput(th)}));
                    trustAnchor3 = trustAnchor2;
                    i = 5;
                    if (trustAnchor3 == null) {
                    }
                    if (trustAnchor3 == null) {
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
                i = 5;
                if (trustAnchor3 == null) {
                    X509Certificate trustedCert = trustAnchor3.getTrustedCert();
                    if (trustedCert != null) {
                        try {
                            x500Principal4 = getSubjectPrincipal(trustedCert);
                        } catch (IllegalArgumentException e6) {
                            addError(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.trustDNInvalid", new Object[]{new UntrustedInput(trustAnchor3.getCAName())}));
                            x500Principal4 = null;
                        }
                    } else {
                        x500Principal4 = new X500Principal(trustAnchor3.getCAName());
                    }
                    if (!(trustedCert == null || (keyUsage = trustedCert.getKeyUsage()) == null || (keyUsage.length > 5 && keyUsage[5]))) {
                        addNotification(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.trustKeyUsage"));
                    }
                    x500Principal = x500Principal4;
                } else {
                    x500Principal = null;
                }
                if (trustAnchor3 == null) {
                    x509Certificate = trustAnchor3.getTrustedCert();
                    publicKey = x509Certificate != null ? x509Certificate.getPublicKey() : trustAnchor3.getCAPublicKey();
                    try {
                        AlgorithmIdentifier algorithmIdentifier = getAlgorithmIdentifier(publicKey);
                        algorithmIdentifier.getAlgorithm();
                        algorithmIdentifier.getParameters();
                    } catch (CertPathValidatorException e7) {
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
                    int i6 = this.n - size;
                    X509Certificate x509Certificate5 = (X509Certificate) this.certs.get(size);
                    if (publicKey2 != null) {
                        try {
                            CertPathValidatorUtilities.verifyX509Certificate(x509Certificate5, publicKey2, this.pkixParams.getSigProvider());
                            i2 = i;
                        } catch (GeneralSecurityException e8) {
                            Object[] objArr = new Object[3];
                            objArr[c5] = e8.getMessage();
                            objArr[c6] = e8;
                            objArr[c4] = e8.getClass().getName();
                            errorBundle2 = new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.signatureNotVerified", objArr);
                            addError(errorBundle2, size);
                            i2 = 5;
                            x509Certificate5.checkValidity(this.validDate);
                            if (this.pkixParams.isRevocationEnabled()) {
                            }
                            if (x500Principal3 != null) {
                            }
                            c2 = 2;
                            c = 0;
                            if (i3 != this.n) {
                            }
                            X500Principal subjectX500Principal = x509Certificate3.getSubjectX500Principal();
                            publicKey2 = getNextWorkingKey(this.certs, i4);
                            try {
                                AlgorithmIdentifier algorithmIdentifier2 = getAlgorithmIdentifier(publicKey2);
                                algorithmIdentifier2.getAlgorithm();
                                algorithmIdentifier2.getParameters();
                            } catch (CertPathValidatorException e9) {
                            }
                            size = i4 - 1;
                            c5 = c;
                            i = i2;
                            x509Certificate2 = x509Certificate3;
                            trustAnchor3 = trustAnchor4;
                            c4 = c2;
                            c6 = c3;
                            x500Principal2 = subjectX500Principal;
                        }
                    } else if (isSelfIssued(x509Certificate5)) {
                        try {
                            CertPathValidatorUtilities.verifyX509Certificate(x509Certificate5, x509Certificate5.getPublicKey(), this.pkixParams.getSigProvider());
                            addError(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.rootKeyIsValidButNotATrustAnchor"), size);
                        } catch (GeneralSecurityException e10) {
                            Object[] objArr2 = new Object[3];
                            objArr2[c5] = e10.getMessage();
                            objArr2[c6] = e10;
                            objArr2[c4] = e10.getClass().getName();
                            errorBundle2 = new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.signatureNotVerified", objArr2);
                        }
                        i2 = 5;
                    } else {
                        ErrorBundle errorBundle3 = new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.NoIssuerPublicKey");
                        byte[] extensionValue2 = x509Certificate5.getExtensionValue(Extension.authorityKeyIdentifier.getId());
                        if (!(extensionValue2 == null || (authorityCertIssuer = (instance = AuthorityKeyIdentifier.getInstance(DEROctetString.getInstance(extensionValue2).getOctets())).getAuthorityCertIssuer()) == null)) {
                            GeneralName generalName = authorityCertIssuer.getNames()[c5];
                            BigInteger authorityCertSerialNumber = instance.getAuthorityCertSerialNumber();
                            if (authorityCertSerialNumber != null) {
                                Object[] objArr3 = new Object[7];
                                objArr3[c5] = new LocaleString(RESOURCE_NAME, "missingIssuer");
                                objArr3[1] = " \"";
                                objArr3[2] = generalName;
                                objArr3[3] = "\" ";
                                objArr3[4] = new LocaleString(RESOURCE_NAME, "missingSerial");
                                i2 = 5;
                                objArr3[5] = " ";
                                objArr3[6] = authorityCertSerialNumber;
                                errorBundle3.setExtraArguments(objArr3);
                                addError(errorBundle3, size);
                            }
                        }
                        i2 = 5;
                        addError(errorBundle3, size);
                    }
                    try {
                        x509Certificate5.checkValidity(this.validDate);
                    } catch (CertificateNotYetValidException e11) {
                        Object[] objArr4 = new Object[1];
                        objArr4[c5] = new TrustedInput(x509Certificate5.getNotBefore());
                        errorBundle = new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.certificateNotYetValid", objArr4);
                    } catch (CertificateExpiredException e12) {
                        Object[] objArr5 = new Object[1];
                        objArr5[c5] = new TrustedInput(x509Certificate5.getNotAfter());
                        errorBundle = new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.certificateExpired", objArr5);
                    }
                    if (this.pkixParams.isRevocationEnabled()) {
                        try {
                            ASN1Primitive extensionValue3 = getExtensionValue(x509Certificate5, CRL_DIST_POINTS);
                            if (extensionValue3 != null) {
                                cRLDistPoint = CRLDistPoint.getInstance(extensionValue3);
                                extensionValue = getExtensionValue(x509Certificate5, AUTH_INFO_ACCESS);
                                if (extensionValue != null) {
                                    authorityInformationAccess = AuthorityInformationAccess.getInstance(extensionValue);
                                    Vector cRLDistUrls = getCRLDistUrls(cRLDistPoint);
                                    Vector oCSPUrls = getOCSPUrls(authorityInformationAccess);
                                    it = cRLDistUrls.iterator();
                                    while (it.hasNext()) {
                                        Object[] objArr6 = new Object[1];
                                        objArr6[c5] = new UntrustedUrlInput(it.next());
                                        addNotification(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.crlDistPoint", objArr6), size);
                                        x509Certificate5 = x509Certificate5;
                                    }
                                    it2 = oCSPUrls.iterator();
                                    while (it2.hasNext()) {
                                        Object[] objArr7 = new Object[1];
                                        objArr7[c5] = new UntrustedUrlInput(it2.next());
                                        addNotification(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.ocspLocation", objArr7), size);
                                    }
                                    x509Certificate3 = x509Certificate5;
                                    i3 = i6;
                                    x500Principal3 = x500Principal2;
                                    i5 = size;
                                    publicKey3 = publicKey2;
                                    i2 = 5;
                                    trustAnchor4 = trustAnchor3;
                                    checkRevocation(this.pkixParams, x509Certificate3, this.validDate, x509Certificate2, publicKey2, cRLDistUrls, oCSPUrls, i5);
                                    i4 = i5;
                                }
                                authorityInformationAccess = null;
                                Vector cRLDistUrls2 = getCRLDistUrls(cRLDistPoint);
                                Vector oCSPUrls2 = getOCSPUrls(authorityInformationAccess);
                                it = cRLDistUrls2.iterator();
                                while (it.hasNext()) {
                                }
                                it2 = oCSPUrls2.iterator();
                                while (it2.hasNext()) {
                                }
                                x509Certificate3 = x509Certificate5;
                                i3 = i6;
                                x500Principal3 = x500Principal2;
                                i5 = size;
                                publicKey3 = publicKey2;
                                i2 = 5;
                                trustAnchor4 = trustAnchor3;
                                try {
                                    checkRevocation(this.pkixParams, x509Certificate3, this.validDate, x509Certificate2, publicKey2, cRLDistUrls2, oCSPUrls2, i5);
                                    i4 = i5;
                                } catch (CertPathReviewerException e13) {
                                    e = e13;
                                    i4 = i5;
                                    addError(e.getErrorMessage(), i4);
                                    if (x500Principal3 != null) {
                                    }
                                    c2 = 2;
                                    c = 0;
                                    if (i3 != this.n) {
                                    }
                                    X500Principal subjectX500Principal2 = x509Certificate3.getSubjectX500Principal();
                                    publicKey2 = getNextWorkingKey(this.certs, i4);
                                    AlgorithmIdentifier algorithmIdentifier22 = getAlgorithmIdentifier(publicKey2);
                                    algorithmIdentifier22.getAlgorithm();
                                    algorithmIdentifier22.getParameters();
                                    size = i4 - 1;
                                    c5 = c;
                                    i = i2;
                                    x509Certificate2 = x509Certificate3;
                                    trustAnchor3 = trustAnchor4;
                                    c4 = c2;
                                    c6 = c3;
                                    x500Principal2 = subjectX500Principal2;
                                }
                            }
                        } catch (AnnotatedException e14) {
                            addError(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.crlDistPtExtError"), size);
                        }
                        cRLDistPoint = null;
                        try {
                            extensionValue = getExtensionValue(x509Certificate5, AUTH_INFO_ACCESS);
                            if (extensionValue != null) {
                            }
                        } catch (AnnotatedException e15) {
                            addError(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.crlAuthInfoAccError"), size);
                        }
                        authorityInformationAccess = null;
                        Vector cRLDistUrls22 = getCRLDistUrls(cRLDistPoint);
                        Vector oCSPUrls22 = getOCSPUrls(authorityInformationAccess);
                        it = cRLDistUrls22.iterator();
                        while (it.hasNext()) {
                        }
                        it2 = oCSPUrls22.iterator();
                        while (it2.hasNext()) {
                        }
                        try {
                            x509Certificate3 = x509Certificate5;
                            i3 = i6;
                            x500Principal3 = x500Principal2;
                            i5 = size;
                            publicKey3 = publicKey2;
                            i2 = 5;
                            trustAnchor4 = trustAnchor3;
                            checkRevocation(this.pkixParams, x509Certificate3, this.validDate, x509Certificate2, publicKey2, cRLDistUrls22, oCSPUrls22, i5);
                            i4 = i5;
                        } catch (CertPathReviewerException e16) {
                            e = e16;
                            i3 = i6;
                            x500Principal3 = x500Principal2;
                            publicKey3 = publicKey2;
                            i5 = size;
                            trustAnchor4 = trustAnchor3;
                            x509Certificate3 = x509Certificate5;
                            i2 = 5;
                            i4 = i5;
                            addError(e.getErrorMessage(), i4);
                            if (x500Principal3 != null) {
                            }
                            c2 = 2;
                            c = 0;
                            if (i3 != this.n) {
                            }
                            X500Principal subjectX500Principal22 = x509Certificate3.getSubjectX500Principal();
                            publicKey2 = getNextWorkingKey(this.certs, i4);
                            AlgorithmIdentifier algorithmIdentifier222 = getAlgorithmIdentifier(publicKey2);
                            algorithmIdentifier222.getAlgorithm();
                            algorithmIdentifier222.getParameters();
                            size = i4 - 1;
                            c5 = c;
                            i = i2;
                            x509Certificate2 = x509Certificate3;
                            trustAnchor3 = trustAnchor4;
                            c4 = c2;
                            c6 = c3;
                            x500Principal2 = subjectX500Principal22;
                        }
                    } else {
                        x509Certificate3 = x509Certificate5;
                        i3 = i6;
                        x500Principal3 = x500Principal2;
                        publicKey3 = publicKey2;
                        i4 = size;
                        trustAnchor4 = trustAnchor3;
                    }
                    if (x500Principal3 != null || x509Certificate3.getIssuerX500Principal().equals(x500Principal3)) {
                        c2 = 2;
                        c = 0;
                    } else {
                        c2 = 2;
                        c = 0;
                        addError(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.certWrongIssuer", new Object[]{x500Principal3.getName(), x509Certificate3.getIssuerX500Principal().getName()}), i4);
                    }
                    if (i3 != this.n) {
                        if (x509Certificate3 != null) {
                            c3 = 1;
                            if (x509Certificate3.getVersion() == 1) {
                                addError(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.noCACert"), i4);
                            }
                        } else {
                            c3 = 1;
                        }
                        try {
                            BasicConstraints instance2 = BasicConstraints.getInstance(getExtensionValue(x509Certificate3, BASIC_CONSTRAINTS));
                            if (instance2 == null) {
                                addError(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.noBasicConstraints"), i4);
                            } else if (!instance2.isCA()) {
                                addError(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.noCACert"), i4);
                            }
                        } catch (AnnotatedException e17) {
                            addError(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.errorProcesingBC"), i4);
                        }
                        boolean[] keyUsage2 = x509Certificate3.getKeyUsage();
                        if (keyUsage2 != null && (keyUsage2.length <= i2 || !keyUsage2[i2])) {
                            addError(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.noCertSign"), i4);
                        }
                    } else {
                        c3 = 1;
                    }
                    X500Principal subjectX500Principal222 = x509Certificate3.getSubjectX500Principal();
                    try {
                        publicKey2 = getNextWorkingKey(this.certs, i4);
                        AlgorithmIdentifier algorithmIdentifier2222 = getAlgorithmIdentifier(publicKey2);
                        algorithmIdentifier2222.getAlgorithm();
                        algorithmIdentifier2222.getParameters();
                    } catch (CertPathValidatorException e18) {
                        publicKey2 = publicKey3;
                        addError(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.pubKeyError"), i4);
                        size = i4 - 1;
                        c5 = c;
                        i = i2;
                        x509Certificate2 = x509Certificate3;
                        trustAnchor3 = trustAnchor4;
                        c4 = c2;
                        c6 = c3;
                        x500Principal2 = subjectX500Principal222;
                    }
                    size = i4 - 1;
                    c5 = c;
                    i = i2;
                    x509Certificate2 = x509Certificate3;
                    trustAnchor3 = trustAnchor4;
                    c4 = c2;
                    c6 = c3;
                    x500Principal2 = subjectX500Principal222;
                }
                this.trustAnchor = trustAnchor3;
                this.subjectPublicKey = publicKey2;
                return;
            }
            trustAnchor2 = null;
        } catch (CertPathReviewerException e19) {
            e2 = e19;
            trustAnchor2 = null;
            addError(e2.getErrorMessage());
            trustAnchor3 = trustAnchor2;
            i = 5;
            if (trustAnchor3 == null) {
            }
            if (trustAnchor3 == null) {
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
        } catch (Throwable th3) {
            th = th3;
            trustAnchor2 = null;
            addError(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.unknown", new Object[]{new UntrustedInput(th.getMessage()), new UntrustedInput(th)}));
            trustAnchor3 = trustAnchor2;
            i = 5;
            if (trustAnchor3 == null) {
            }
            if (trustAnchor3 == null) {
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
        i = 5;
        if (trustAnchor3 == null) {
        }
        if (trustAnchor3 == null) {
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
        addError(errorBundle, size);
        if (this.pkixParams.isRevocationEnabled()) {
        }
        if (x500Principal3 != null) {
        }
        c2 = 2;
        c = 0;
        if (i3 != this.n) {
        }
        X500Principal subjectX500Principal2222 = x509Certificate3.getSubjectX500Principal();
        publicKey2 = getNextWorkingKey(this.certs, i4);
        AlgorithmIdentifier algorithmIdentifier22222 = getAlgorithmIdentifier(publicKey2);
        algorithmIdentifier22222.getAlgorithm();
        algorithmIdentifier22222.getParameters();
        size = i4 - 1;
        c5 = c;
        i = i2;
        x509Certificate2 = x509Certificate3;
        trustAnchor3 = trustAnchor4;
        c4 = c2;
        c6 = c3;
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
                return (X509CRL) CertificateFactory.getInstance("X.509", BouncyCastleProvider.PROVIDER_NAME).generateCRL(httpURLConnection.getInputStream());
            }
            throw new Exception(httpURLConnection.getResponseMessage());
        } catch (Exception e) {
            throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.loadCrlDistPointError", new Object[]{new UntrustedInput(str), e.getMessage(), e, e.getClass().getName()}));
        }
    }

    private boolean processQcStatements(X509Certificate x509Certificate, int i) {
        ErrorBundle errorBundle;
        try {
            ASN1Sequence aSN1Sequence = (ASN1Sequence) getExtensionValue(x509Certificate, QC_STATEMENT);
            boolean z = false;
            for (int i2 = 0; i2 < aSN1Sequence.size(); i2++) {
                QCStatement instance = QCStatement.getInstance(aSN1Sequence.getObjectAt(i2));
                if (QCStatement.id_etsi_qcs_QcCompliance.equals((ASN1Primitive) instance.getStatementId())) {
                    errorBundle = new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.QcEuCompliance");
                } else {
                    if (!QCStatement.id_qcs_pkixQCSyntax_v1.equals((ASN1Primitive) instance.getStatementId())) {
                        if (QCStatement.id_etsi_qcs_QcSSCD.equals((ASN1Primitive) instance.getStatementId())) {
                            errorBundle = new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.QcSSCD");
                        } else if (QCStatement.id_etsi_qcs_LimiteValue.equals((ASN1Primitive) instance.getStatementId())) {
                            MonetaryValue instance2 = MonetaryValue.getInstance(instance.getStatementInfo());
                            instance2.getCurrency();
                            double doubleValue = instance2.getAmount().doubleValue() * Math.pow(10.0d, instance2.getExponent().doubleValue());
                            addNotification(instance2.getCurrency().isAlphabetic() ? new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.QcLimitValueAlpha", new Object[]{instance2.getCurrency().getAlphabetic(), new TrustedInput(new Double(doubleValue)), instance2}) : new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.QcLimitValueNum", new Object[]{Integers.valueOf(instance2.getCurrency().getNumeric()), new TrustedInput(new Double(doubleValue)), instance2}), i);
                        } else {
                            addNotification(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.QcUnknownStatement", new Object[]{instance.getStatementId(), new UntrustedInput(instance)}), i);
                            z = true;
                        }
                    }
                }
                addNotification(errorBundle, i);
            }
            return true ^ z;
        } catch (AnnotatedException e) {
            addError(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.QcStatementExtError"), i);
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

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x01d5: APUT  
      (r15v8 java.lang.Object[])
      (0 ??[int, short, byte, char])
      (wrap: org.bouncycastle.i18n.filter.TrustedInput : 0x01d1: CONSTRUCTOR  (r12v15 org.bouncycastle.i18n.filter.TrustedInput) = 
      (wrap: java.util.Date : 0x01cd: INVOKE  (r13v12 java.util.Date) = (r14v2 java.security.cert.X509CRL) type: VIRTUAL call: java.security.cert.X509CRL.getThisUpdate():java.util.Date)
     call: org.bouncycastle.i18n.filter.TrustedInput.<init>(java.lang.Object):void type: CONSTRUCTOR)
     */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0110, code lost:
        addNotification(new org.bouncycastle.i18n.ErrorBundle(org.bouncycastle.x509.PKIXCertPathReviewer.RESOURCE_NAME, "CertPathReviewer.localValidCRL", new java.lang.Object[]{new org.bouncycastle.i18n.filter.TrustedInput(r0.getThisUpdate()), new org.bouncycastle.i18n.filter.TrustedInput(r0.getNextUpdate())}), r27);
        r11 = r0;
        r13 = true;
     */
    /* JADX WARNING: Removed duplicated region for block: B:102:0x02cc  */
    /* JADX WARNING: Removed duplicated region for block: B:103:0x02e9  */
    /* JADX WARNING: Removed duplicated region for block: B:99:0x02b6  */
    public void checkCRLs(PKIXParameters pKIXParameters, X509Certificate x509Certificate, Date date, X509Certificate x509Certificate2, PublicKey publicKey, Vector vector, int i) throws CertPathReviewerException {
        Iterator it;
        X509CRL x509crl;
        X509CRL x509crl2;
        boolean z;
        X509CRL x509crl3;
        boolean z2;
        boolean z3;
        String str;
        boolean[] keyUsage;
        X509CRL x509crl4;
        Iterator it2;
        X509CRL x509crl5;
        CertPathReviewerException e;
        boolean z4;
        Iterator it3;
        X509CRLStoreSelector x509CRLStoreSelector = new X509CRLStoreSelector();
        try {
            x509CRLStoreSelector.addIssuerName(getEncodedIssuerPrincipal(x509Certificate).getEncoded());
            x509CRLStoreSelector.setCertificateChecking(x509Certificate);
            try {
                Set findCRLs = CRL_UTIL.findCRLs(x509CRLStoreSelector, pKIXParameters);
                Iterator it4 = findCRLs.iterator();
                if (findCRLs.isEmpty()) {
                    ArrayList arrayList = new ArrayList();
                    for (X509CRL x509crl6 : CRL_UTIL.findCRLs(new X509CRLStoreSelector(), pKIXParameters)) {
                        arrayList.add(x509crl6.getIssuerX500Principal());
                    }
                    it3 = it4;
                    addNotification(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.noCrlInCertstore", new Object[]{new UntrustedInput(x509CRLStoreSelector.getIssuerNames()), new UntrustedInput(arrayList), Integers.valueOf(arrayList.size())}), i);
                } else {
                    it3 = it4;
                }
                it = it3;
            } catch (AnnotatedException e2) {
                addError(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.crlExtractionError", new Object[]{e2.getCause().getMessage(), e2.getCause(), e2.getCause().getClass().getName()}), i);
                it = new ArrayList().iterator();
            }
            x509crl = null;
            if (!z) {
                Iterator it5 = vector.iterator();
                while (it5.hasNext()) {
                    try {
                        String str2 = (String) it5.next();
                        x509crl3 = getCRL(str2);
                        if (x509crl3 == null) {
                            x509crl5 = x509crl2;
                            it2 = it5;
                        } else if (!x509Certificate.getIssuerX500Principal().equals(x509crl3.getIssuerX500Principal())) {
                            try {
                                x509crl5 = x509crl2;
                                it2 = it5;
                                try {
                                    addNotification(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.onlineCRLWrongCA", new Object[]{new UntrustedInput(x509crl3.getIssuerX500Principal().getName()), new UntrustedInput(x509Certificate.getIssuerX500Principal().getName()), new UntrustedUrlInput(str2)}), i);
                                } catch (CertPathReviewerException e3) {
                                    e = e3;
                                    addNotification(e.getErrorMessage(), i);
                                    it5 = it2;
                                    x509crl2 = x509crl5;
                                }
                            } catch (CertPathReviewerException e4) {
                                e = e4;
                                x509crl5 = x509crl2;
                                it2 = it5;
                                addNotification(e.getErrorMessage(), i);
                                it5 = it2;
                                x509crl2 = x509crl5;
                            }
                        } else {
                            x509crl5 = x509crl2;
                            it2 = it5;
                            try {
                                if (x509crl3.getNextUpdate() != null) {
                                    if (!this.pkixParams.getDate().before(x509crl3.getNextUpdate())) {
                                        Object[] objArr = new Object[3];
                                        z4 = z;
                                        try {
                                            objArr[0] = new TrustedInput(x509crl3.getThisUpdate());
                                            objArr[1] = new TrustedInput(x509crl3.getNextUpdate());
                                            objArr[2] = new UntrustedUrlInput(str2);
                                            addNotification(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.onlineInvalidCRL", objArr), i);
                                            it5 = it2;
                                            x509crl2 = x509crl5;
                                            z = z4;
                                        } catch (CertPathReviewerException e5) {
                                            e = e5;
                                            z = z4;
                                        }
                                    }
                                }
                                try {
                                    addNotification(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.onlineValidCRL", new Object[]{new TrustedInput(x509crl3.getThisUpdate()), new TrustedInput(x509crl3.getNextUpdate()), new UntrustedUrlInput(str2)}), i);
                                    z = true;
                                    break;
                                } catch (CertPathReviewerException e6) {
                                    e = e6;
                                    z = true;
                                }
                            } catch (CertPathReviewerException e7) {
                                e = e7;
                                addNotification(e.getErrorMessage(), i);
                                it5 = it2;
                                x509crl2 = x509crl5;
                            }
                        }
                        z4 = z;
                        it5 = it2;
                        x509crl2 = x509crl5;
                        z = z4;
                    } catch (CertPathReviewerException e8) {
                        e = e8;
                        x509crl5 = x509crl2;
                        it2 = it5;
                        addNotification(e.getErrorMessage(), i);
                        it5 = it2;
                        x509crl2 = x509crl5;
                    }
                }
                x509crl4 = x509crl2;
            } else {
                x509crl4 = x509crl2;
            }
            x509crl3 = x509crl4;
            if (x509crl3 != null) {
                if (x509Certificate2 != null && (keyUsage = x509Certificate2.getKeyUsage()) != null && (keyUsage.length <= 6 || !keyUsage[6])) {
                    throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.noCrlSigningPermited"));
                } else if (publicKey != null) {
                    try {
                        x509crl3.verify(publicKey, BouncyCastleProvider.PROVIDER_NAME);
                        X509CRLEntry revokedCertificate = x509crl3.getRevokedCertificate(x509Certificate.getSerialNumber());
                        if (revokedCertificate != null) {
                            if (revokedCertificate.hasExtensions()) {
                                try {
                                    ASN1Enumerated instance = ASN1Enumerated.getInstance(getExtensionValue(revokedCertificate, Extension.reasonCode.getId()));
                                    if (instance != null) {
                                        str = crlReasons[instance.intValueExact()];
                                        if (str == null) {
                                            str = crlReasons[7];
                                        }
                                        LocaleString localeString = new LocaleString(RESOURCE_NAME, str);
                                        if (!date.before(revokedCertificate.getRevocationDate())) {
                                            addNotification(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.revokedAfterValidation", new Object[]{new TrustedInput(revokedCertificate.getRevocationDate()), localeString}), i);
                                        } else {
                                            throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.certRevoked", new Object[]{new TrustedInput(revokedCertificate.getRevocationDate()), localeString}));
                                        }
                                    }
                                } catch (AnnotatedException e9) {
                                    throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.crlReasonExtError"), e9);
                                }
                            }
                            str = null;
                            if (str == null) {
                            }
                            LocaleString localeString2 = new LocaleString(RESOURCE_NAME, str);
                            if (!date.before(revokedCertificate.getRevocationDate())) {
                            }
                        } else {
                            addNotification(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.notRevoked"), i);
                        }
                        if (x509crl3.getNextUpdate() == null || !x509crl3.getNextUpdate().before(this.pkixParams.getDate())) {
                            z3 = true;
                            z2 = false;
                        } else {
                            z3 = true;
                            z2 = false;
                            addNotification(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.crlUpdateAvailable", new Object[]{new TrustedInput(x509crl3.getNextUpdate())}), i);
                        }
                        try {
                            ASN1Primitive extensionValue = getExtensionValue(x509crl3, ISSUING_DISTRIBUTION_POINT);
                            try {
                                ASN1Primitive extensionValue2 = getExtensionValue(x509crl3, DELTA_CRL_INDICATOR);
                                if (extensionValue2 != null) {
                                    X509CRLStoreSelector x509CRLStoreSelector2 = new X509CRLStoreSelector();
                                    try {
                                        x509CRLStoreSelector2.addIssuerName(getIssuerPrincipal(x509crl3).getEncoded());
                                        x509CRLStoreSelector2.setMinCRLNumber(((ASN1Integer) extensionValue2).getPositiveValue());
                                        try {
                                            x509CRLStoreSelector2.setMaxCRLNumber(((ASN1Integer) getExtensionValue(x509crl3, CRL_NUMBER)).getPositiveValue().subtract(BigInteger.valueOf(1)));
                                            try {
                                                Iterator it6 = CRL_UTIL.findCRLs(x509CRLStoreSelector2, pKIXParameters).iterator();
                                                while (true) {
                                                    if (!it6.hasNext()) {
                                                        z3 = z2;
                                                        break;
                                                    }
                                                    try {
                                                        ASN1Primitive extensionValue3 = getExtensionValue((X509CRL) it6.next(), ISSUING_DISTRIBUTION_POINT);
                                                        if (extensionValue == null) {
                                                            if (extensionValue3 == null) {
                                                                break;
                                                            }
                                                        } else if (extensionValue.equals(extensionValue3)) {
                                                            break;
                                                        }
                                                    } catch (AnnotatedException e10) {
                                                        throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.distrPtExtError"), e10);
                                                    }
                                                }
                                                if (!z3) {
                                                    throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.noBaseCRL"));
                                                }
                                            } catch (AnnotatedException e11) {
                                                throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.crlExtractionError"), e11);
                                            }
                                        } catch (AnnotatedException e12) {
                                            throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.crlNbrExtError"), e12);
                                        }
                                    } catch (IOException e13) {
                                        throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.crlIssuerException"), e13);
                                    }
                                }
                                if (extensionValue != null) {
                                    IssuingDistributionPoint instance2 = IssuingDistributionPoint.getInstance(extensionValue);
                                    try {
                                        BasicConstraints instance3 = BasicConstraints.getInstance(getExtensionValue(x509Certificate, BASIC_CONSTRAINTS));
                                        if (instance2.onlyContainsUserCerts() && instance3 != null && instance3.isCA()) {
                                            throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.crlOnlyUserCert"));
                                        } else if (instance2.onlyContainsCACerts() && (instance3 == null || !instance3.isCA())) {
                                            throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.crlOnlyCaCert"));
                                        } else if (instance2.onlyContainsAttributeCerts()) {
                                            throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.crlOnlyAttrCert"));
                                        }
                                    } catch (AnnotatedException e14) {
                                        throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.crlBCExtError"), e14);
                                    }
                                }
                            } catch (AnnotatedException e15) {
                                throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.deltaCrlExtError"));
                            }
                        } catch (AnnotatedException e16) {
                            throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.distrPtExtError"));
                        }
                    } catch (Exception e17) {
                        throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.crlVerifyFailed"), e17);
                    }
                } else {
                    throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.crlNoIssuerPublicKey"));
                }
            }
            if (!z) {
                throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.noValidCrlFound"));
            }
            return;
        } catch (IOException e18) {
            throw new CertPathReviewerException(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.crlIssuerException"), e18);
        }
        while (true) {
            if (!it.hasNext()) {
                x509crl2 = x509crl;
                z = false;
                break;
            }
            x509crl = (X509CRL) it.next();
            if (x509crl.getNextUpdate() == null || pKIXParameters.getDate().before(x509crl.getNextUpdate())) {
                break;
            }
            addNotification(new ErrorBundle(RESOURCE_NAME, "CertPathReviewer.localInvalidCRL", new Object[]{new TrustedInput(x509crl.getThisUpdate()), new TrustedInput(x509crl.getNextUpdate())}), i);
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
            int i = this.n;
            this.notifications = new List[(i + 1)];
            this.errors = new List[(i + 1)];
            int i2 = 0;
            while (true) {
                List[] listArr = this.notifications;
                if (i2 < listArr.length) {
                    listArr[i2] = new ArrayList();
                    this.errors[i2] = new ArrayList();
                    i2++;
                } else {
                    checkSignatures();
                    checkNameConstraints();
                    checkPathLength();
                    checkPolicy();
                    checkCriticalExtensions();
                    return;
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public Vector getCRLDistUrls(CRLDistPoint cRLDistPoint) {
        DistributionPoint[] distributionPoints;
        Vector vector = new Vector();
        if (cRLDistPoint != null) {
            for (DistributionPoint distributionPoint : cRLDistPoint.getDistributionPoints()) {
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
                if (accessDescriptions[i].getAccessMethod().equals((ASN1Primitive) AccessDescription.id_ad_ocsp)) {
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
        int i = 0;
        while (true) {
            List[] listArr = this.errors;
            if (i >= listArr.length) {
                return true;
            }
            if (!listArr[i].isEmpty()) {
                return false;
            }
            i++;
        }
    }
}
