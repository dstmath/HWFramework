package org.bouncycastle.jce.provider;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.CertPathValidatorException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.ASN1GeneralizedTime;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1String;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.bsi.BSIObjectIdentifiers;
import org.bouncycastle.asn1.cryptopro.CryptoProObjectIdentifiers;
import org.bouncycastle.asn1.eac.EACObjectIdentifiers;
import org.bouncycastle.asn1.isara.IsaraObjectIdentifiers;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.ocsp.BasicOCSPResponse;
import org.bouncycastle.asn1.ocsp.CertID;
import org.bouncycastle.asn1.ocsp.OCSPObjectIdentifiers;
import org.bouncycastle.asn1.ocsp.OCSPResponse;
import org.bouncycastle.asn1.ocsp.ResponderID;
import org.bouncycastle.asn1.ocsp.ResponseBytes;
import org.bouncycastle.asn1.ocsp.ResponseData;
import org.bouncycastle.asn1.ocsp.RevokedInfo;
import org.bouncycastle.asn1.ocsp.SingleResponse;
import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.RSASSAPSSparams;
import org.bouncycastle.asn1.rosstandart.RosstandartObjectIdentifiers;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStrictStyle;
import org.bouncycastle.asn1.x509.AccessDescription;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.AuthorityInformationAccess;
import org.bouncycastle.asn1.x509.Certificate;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.bouncycastle.jcajce.PKIXCertRevocationChecker;
import org.bouncycastle.jcajce.PKIXCertRevocationCheckerParameters;
import org.bouncycastle.jcajce.util.JcaJceHelper;
import org.bouncycastle.jcajce.util.MessageDigestUtils;
import org.bouncycastle.jce.exception.ExtCertPathValidatorException;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Properties;

/* access modifiers changed from: package-private */
public class ProvOcspRevocationChecker implements PKIXCertRevocationChecker {
    private static final int DEFAULT_OCSP_MAX_RESPONSE_SIZE = 32768;
    private static final int DEFAULT_OCSP_TIMEOUT = 15000;
    private static final Map oids = new HashMap();
    private final JcaJceHelper helper;
    private boolean isEnabledOCSP;
    private String ocspURL;
    private PKIXCertRevocationCheckerParameters parameters;
    private final ProvRevocationChecker parent;

    static {
        oids.put(new ASN1ObjectIdentifier("1.2.840.113549.1.1.5"), "SHA1WITHRSA");
        oids.put(PKCSObjectIdentifiers.sha224WithRSAEncryption, "SHA224WITHRSA");
        oids.put(PKCSObjectIdentifiers.sha256WithRSAEncryption, "SHA256WITHRSA");
        oids.put(PKCSObjectIdentifiers.sha384WithRSAEncryption, "SHA384WITHRSA");
        oids.put(PKCSObjectIdentifiers.sha512WithRSAEncryption, "SHA512WITHRSA");
        oids.put(CryptoProObjectIdentifiers.gostR3411_94_with_gostR3410_94, "GOST3411WITHGOST3410");
        oids.put(CryptoProObjectIdentifiers.gostR3411_94_with_gostR3410_2001, "GOST3411WITHECGOST3410");
        oids.put(RosstandartObjectIdentifiers.id_tc26_signwithdigest_gost_3410_12_256, "GOST3411-2012-256WITHECGOST3410-2012-256");
        oids.put(RosstandartObjectIdentifiers.id_tc26_signwithdigest_gost_3410_12_512, "GOST3411-2012-512WITHECGOST3410-2012-512");
        oids.put(BSIObjectIdentifiers.ecdsa_plain_SHA1, "SHA1WITHPLAIN-ECDSA");
        oids.put(BSIObjectIdentifiers.ecdsa_plain_SHA224, "SHA224WITHPLAIN-ECDSA");
        oids.put(BSIObjectIdentifiers.ecdsa_plain_SHA256, "SHA256WITHPLAIN-ECDSA");
        oids.put(BSIObjectIdentifiers.ecdsa_plain_SHA384, "SHA384WITHPLAIN-ECDSA");
        oids.put(BSIObjectIdentifiers.ecdsa_plain_SHA512, "SHA512WITHPLAIN-ECDSA");
        oids.put(BSIObjectIdentifiers.ecdsa_plain_RIPEMD160, "RIPEMD160WITHPLAIN-ECDSA");
        oids.put(EACObjectIdentifiers.id_TA_ECDSA_SHA_1, "SHA1WITHCVC-ECDSA");
        oids.put(EACObjectIdentifiers.id_TA_ECDSA_SHA_224, "SHA224WITHCVC-ECDSA");
        oids.put(EACObjectIdentifiers.id_TA_ECDSA_SHA_256, "SHA256WITHCVC-ECDSA");
        oids.put(EACObjectIdentifiers.id_TA_ECDSA_SHA_384, "SHA384WITHCVC-ECDSA");
        oids.put(EACObjectIdentifiers.id_TA_ECDSA_SHA_512, "SHA512WITHCVC-ECDSA");
        oids.put(IsaraObjectIdentifiers.id_alg_xmss, "XMSS");
        oids.put(IsaraObjectIdentifiers.id_alg_xmssmt, "XMSSMT");
        oids.put(new ASN1ObjectIdentifier("1.2.840.113549.1.1.4"), "MD5WITHRSA");
        oids.put(new ASN1ObjectIdentifier("1.2.840.113549.1.1.2"), "MD2WITHRSA");
        oids.put(new ASN1ObjectIdentifier("1.2.840.10040.4.3"), "SHA1WITHDSA");
        oids.put(X9ObjectIdentifiers.ecdsa_with_SHA1, "SHA1WITHECDSA");
        oids.put(X9ObjectIdentifiers.ecdsa_with_SHA224, "SHA224WITHECDSA");
        oids.put(X9ObjectIdentifiers.ecdsa_with_SHA256, "SHA256WITHECDSA");
        oids.put(X9ObjectIdentifiers.ecdsa_with_SHA384, "SHA384WITHECDSA");
        oids.put(X9ObjectIdentifiers.ecdsa_with_SHA512, "SHA512WITHECDSA");
        oids.put(OIWObjectIdentifiers.sha1WithRSA, "SHA1WITHRSA");
        oids.put(OIWObjectIdentifiers.dsaWithSHA1, "SHA1WITHDSA");
        oids.put(NISTObjectIdentifiers.dsa_with_sha224, "SHA224WITHDSA");
        oids.put(NISTObjectIdentifiers.dsa_with_sha256, "SHA256WITHDSA");
    }

    public ProvOcspRevocationChecker(ProvRevocationChecker provRevocationChecker, JcaJceHelper jcaJceHelper) {
        this.parent = provRevocationChecker;
        this.helper = jcaJceHelper;
    }

    private static byte[] calcKeyHash(MessageDigest messageDigest, PublicKey publicKey) {
        return messageDigest.digest(SubjectPublicKeyInfo.getInstance(publicKey.getEncoded()).getPublicKeyData().getBytes());
    }

    private CertID createCertID(CertID certID, Certificate certificate, ASN1Integer aSN1Integer) throws CertPathValidatorException {
        return createCertID(certID.getHashAlgorithm(), certificate, aSN1Integer);
    }

    private CertID createCertID(AlgorithmIdentifier algorithmIdentifier, Certificate certificate, ASN1Integer aSN1Integer) throws CertPathValidatorException {
        try {
            MessageDigest createMessageDigest = this.helper.createMessageDigest(MessageDigestUtils.getDigestName(algorithmIdentifier.getAlgorithm()));
            return new CertID(algorithmIdentifier, new DEROctetString(createMessageDigest.digest(certificate.getSubject().getEncoded(ASN1Encoding.DER))), new DEROctetString(createMessageDigest.digest(certificate.getSubjectPublicKeyInfo().getPublicKeyData().getBytes())), aSN1Integer);
        } catch (Exception e) {
            throw new CertPathValidatorException("problem creating ID: " + e, e);
        }
    }

    private Certificate extractCert() throws CertPathValidatorException {
        try {
            return Certificate.getInstance(this.parameters.getSigningCert().getEncoded());
        } catch (Exception e) {
            throw new CertPathValidatorException("cannot process signing cert: " + e.getMessage(), e, this.parameters.getCertPath(), this.parameters.getIndex());
        }
    }

    private static String getDigestName(ASN1ObjectIdentifier aSN1ObjectIdentifier) {
        String digestName = MessageDigestUtils.getDigestName(aSN1ObjectIdentifier);
        int indexOf = digestName.indexOf(45);
        if (indexOf <= 0 || digestName.startsWith("SHA3")) {
            return digestName;
        }
        return digestName.substring(0, indexOf) + digestName.substring(indexOf + 1);
    }

    static URI getOcspResponderURI(X509Certificate x509Certificate) {
        byte[] extensionValue = x509Certificate.getExtensionValue(Extension.authorityInfoAccess.getId());
        if (extensionValue == null) {
            return null;
        }
        AccessDescription[] accessDescriptions = AuthorityInformationAccess.getInstance(ASN1OctetString.getInstance(extensionValue).getOctets()).getAccessDescriptions();
        for (int i = 0; i != accessDescriptions.length; i++) {
            AccessDescription accessDescription = accessDescriptions[i];
            if (AccessDescription.id_ad_ocsp.equals((ASN1Primitive) accessDescription.getAccessMethod())) {
                GeneralName accessLocation = accessDescription.getAccessLocation();
                if (accessLocation.getTagNo() == 6) {
                    try {
                        return new URI(((ASN1String) accessLocation.getName()).getString());
                    } catch (URISyntaxException e) {
                    }
                } else {
                    continue;
                }
            }
        }
        return null;
    }

    private static String getSignatureName(AlgorithmIdentifier algorithmIdentifier) {
        ASN1Encodable parameters2 = algorithmIdentifier.getParameters();
        if (parameters2 == null || DERNull.INSTANCE.equals(parameters2) || !algorithmIdentifier.getAlgorithm().equals((ASN1Primitive) PKCSObjectIdentifiers.id_RSASSA_PSS)) {
            return oids.containsKey(algorithmIdentifier.getAlgorithm()) ? (String) oids.get(algorithmIdentifier.getAlgorithm()) : algorithmIdentifier.getAlgorithm().getId();
        }
        RSASSAPSSparams instance = RSASSAPSSparams.getInstance(parameters2);
        return getDigestName(instance.getHashAlgorithm().getAlgorithm()) + "WITHRSAANDMGF1";
    }

    private static X509Certificate getSignerCert(BasicOCSPResponse basicOCSPResponse, X509Certificate x509Certificate, X509Certificate x509Certificate2, JcaJceHelper jcaJceHelper) throws NoSuchProviderException, NoSuchAlgorithmException {
        ResponderID responderID = basicOCSPResponse.getTbsResponseData().getResponderID();
        byte[] keyHash = responderID.getKeyHash();
        if (keyHash != null) {
            MessageDigest createMessageDigest = jcaJceHelper.createMessageDigest("SHA1");
            if (x509Certificate2 != null && Arrays.areEqual(keyHash, calcKeyHash(createMessageDigest, x509Certificate2.getPublicKey()))) {
                return x509Certificate2;
            }
            if (x509Certificate == null || !Arrays.areEqual(keyHash, calcKeyHash(createMessageDigest, x509Certificate.getPublicKey()))) {
                return null;
            }
            return x509Certificate;
        }
        X500Name instance = X500Name.getInstance(BCStrictStyle.INSTANCE, responderID.getName());
        if (x509Certificate2 != null && instance.equals(X500Name.getInstance(BCStrictStyle.INSTANCE, x509Certificate2.getSubjectX500Principal().getEncoded()))) {
            return x509Certificate2;
        }
        if (x509Certificate == null || !instance.equals(X500Name.getInstance(BCStrictStyle.INSTANCE, x509Certificate.getSubjectX500Principal().getEncoded()))) {
            return null;
        }
        return x509Certificate;
    }

    private static boolean responderMatches(ResponderID responderID, X509Certificate x509Certificate, JcaJceHelper jcaJceHelper) throws NoSuchProviderException, NoSuchAlgorithmException {
        byte[] keyHash = responderID.getKeyHash();
        return keyHash != null ? Arrays.areEqual(keyHash, calcKeyHash(jcaJceHelper.createMessageDigest("SHA1"), x509Certificate.getPublicKey())) : X500Name.getInstance(BCStrictStyle.INSTANCE, responderID.getName()).equals(X500Name.getInstance(BCStrictStyle.INSTANCE, x509Certificate.getSubjectX500Principal().getEncoded()));
    }

    static boolean validatedOcspResponse(BasicOCSPResponse basicOCSPResponse, PKIXCertRevocationCheckerParameters pKIXCertRevocationCheckerParameters, byte[] bArr, X509Certificate x509Certificate, JcaJceHelper jcaJceHelper) throws CertPathValidatorException {
        try {
            ASN1Sequence certs = basicOCSPResponse.getCerts();
            Signature createSignature = jcaJceHelper.createSignature(getSignatureName(basicOCSPResponse.getSignatureAlgorithm()));
            X509Certificate signerCert = getSignerCert(basicOCSPResponse, pKIXCertRevocationCheckerParameters.getSigningCert(), x509Certificate, jcaJceHelper);
            if (signerCert == null) {
                if (certs == null) {
                    throw new CertPathValidatorException("OCSP responder certificate not found");
                }
            }
            if (signerCert != null) {
                createSignature.initVerify(signerCert.getPublicKey());
            } else {
                X509Certificate x509Certificate2 = (X509Certificate) jcaJceHelper.createCertificateFactory("X.509").generateCertificate(new ByteArrayInputStream(certs.getObjectAt(0).toASN1Primitive().getEncoded()));
                x509Certificate2.verify(pKIXCertRevocationCheckerParameters.getSigningCert().getPublicKey());
                x509Certificate2.checkValidity(pKIXCertRevocationCheckerParameters.getValidDate());
                if (responderMatches(basicOCSPResponse.getTbsResponseData().getResponderID(), x509Certificate2, jcaJceHelper)) {
                    List<String> extendedKeyUsage = x509Certificate2.getExtendedKeyUsage();
                    if (extendedKeyUsage == null || !extendedKeyUsage.contains(KeyPurposeId.id_kp_OCSPSigning.getId())) {
                        throw new CertPathValidatorException("responder certificate not valid for signing OCSP responses", null, pKIXCertRevocationCheckerParameters.getCertPath(), pKIXCertRevocationCheckerParameters.getIndex());
                    }
                    createSignature.initVerify(x509Certificate2);
                } else {
                    throw new CertPathValidatorException("responder certificate does not match responderID", null, pKIXCertRevocationCheckerParameters.getCertPath(), pKIXCertRevocationCheckerParameters.getIndex());
                }
            }
            createSignature.update(basicOCSPResponse.getTbsResponseData().getEncoded(ASN1Encoding.DER));
            if (!createSignature.verify(basicOCSPResponse.getSignature().getBytes())) {
                return false;
            }
            if (bArr == null) {
                return true;
            }
            if (Arrays.areEqual(bArr, basicOCSPResponse.getTbsResponseData().getResponseExtensions().getExtension(OCSPObjectIdentifiers.id_pkix_ocsp_nonce).getExtnValue().getOctets())) {
                return true;
            }
            throw new CertPathValidatorException("nonce mismatch in OCSP response", null, pKIXCertRevocationCheckerParameters.getCertPath(), pKIXCertRevocationCheckerParameters.getIndex());
        } catch (CertPathValidatorException e) {
            throw e;
        } catch (GeneralSecurityException e2) {
            throw new CertPathValidatorException("OCSP response failure: " + e2.getMessage(), e2, pKIXCertRevocationCheckerParameters.getCertPath(), pKIXCertRevocationCheckerParameters.getIndex());
        } catch (IOException e3) {
            throw new CertPathValidatorException("OCSP response failure: " + e3.getMessage(), e3, pKIXCertRevocationCheckerParameters.getCertPath(), pKIXCertRevocationCheckerParameters.getIndex());
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:33:0x00d1  */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x00f6  */
    /* JADX WARNING: Removed duplicated region for block: B:88:0x0282  */
    @Override // org.bouncycastle.jcajce.PKIXCertRevocationChecker
    public void check(java.security.cert.Certificate certificate) throws CertPathValidatorException {
        URI uri;
        byte[] bArr;
        boolean z;
        List<java.security.cert.Extension> ocspExtensions;
        int i;
        X509Certificate x509Certificate = (X509Certificate) certificate;
        Map<X509Certificate, byte[]> ocspResponses = this.parent.getOcspResponses();
        URI ocspResponder = this.parent.getOcspResponder();
        if (ocspResponder == null) {
            String str = this.ocspURL;
            if (str != null) {
                try {
                    uri = new URI(str);
                    if (ocspResponses.get(x509Certificate) == null || uri == null) {
                        ocspExtensions = this.parent.getOcspExtensions();
                        bArr = null;
                        for (i = 0; i != ocspExtensions.size(); i++) {
                            java.security.cert.Extension extension = ocspExtensions.get(i);
                            byte[] value = extension.getValue();
                            if (OCSPObjectIdentifiers.id_pkix_ocsp_nonce.getId().equals(extension.getId())) {
                                bArr = value;
                            }
                        }
                        z = false;
                    } else if (this.ocspURL == null && this.parent.getOcspResponder() == null && !this.isEnabledOCSP) {
                        throw new RecoverableCertPathValidatorException("OCSP disabled by \"ocsp.enable\" setting", null, this.parameters.getCertPath(), this.parameters.getIndex());
                    } else {
                        try {
                            ocspResponses.put(x509Certificate, OcspCache.getOcspResponse(createCertID(new AlgorithmIdentifier(OIWObjectIdentifiers.idSHA1), extractCert(), new ASN1Integer(x509Certificate.getSerialNumber())), this.parameters, uri, this.parent.getOcspResponderCert(), this.parent.getOcspExtensions(), this.helper).getEncoded());
                            z = true;
                            bArr = null;
                        } catch (IOException e) {
                            throw new CertPathValidatorException("unable to encode OCSP response", e, this.parameters.getCertPath(), this.parameters.getIndex());
                        }
                    }
                    if (ocspResponses.isEmpty()) {
                        OCSPResponse instance = OCSPResponse.getInstance(ocspResponses.get(x509Certificate));
                        ASN1Integer aSN1Integer = new ASN1Integer(x509Certificate.getSerialNumber());
                        if (instance == null) {
                            throw new RecoverableCertPathValidatorException("no OCSP response found for certificate", null, this.parameters.getCertPath(), this.parameters.getIndex());
                        } else if (instance.getResponseStatus().getValue().intValueExact() == 0) {
                            ResponseBytes instance2 = ResponseBytes.getInstance(instance.getResponseBytes());
                            if (instance2.getResponseType().equals((ASN1Primitive) OCSPObjectIdentifiers.id_pkix_ocsp_basic)) {
                                try {
                                    BasicOCSPResponse instance3 = BasicOCSPResponse.getInstance(instance2.getResponse().getOctets());
                                    if (z || validatedOcspResponse(instance3, this.parameters, bArr, this.parent.getOcspResponderCert(), this.helper)) {
                                        ASN1Sequence responses = ResponseData.getInstance(instance3.getTbsResponseData()).getResponses();
                                        CertID certID = null;
                                        for (int i2 = 0; i2 != responses.size(); i2++) {
                                            SingleResponse instance4 = SingleResponse.getInstance(responses.getObjectAt(i2));
                                            if (aSN1Integer.equals((ASN1Primitive) instance4.getCertID().getSerialNumber())) {
                                                ASN1GeneralizedTime nextUpdate = instance4.getNextUpdate();
                                                if (nextUpdate != null) {
                                                    if (this.parameters.getValidDate().after(nextUpdate.getDate())) {
                                                        throw new ExtCertPathValidatorException("OCSP response expired");
                                                    }
                                                }
                                                if (certID == null || !certID.getHashAlgorithm().equals(instance4.getCertID().getHashAlgorithm())) {
                                                    certID = createCertID(instance4.getCertID(), extractCert(), aSN1Integer);
                                                }
                                                if (certID.equals(instance4.getCertID())) {
                                                    if (instance4.getCertStatus().getTagNo() != 0) {
                                                        if (instance4.getCertStatus().getTagNo() == 1) {
                                                            RevokedInfo instance5 = RevokedInfo.getInstance(instance4.getCertStatus().getStatus());
                                                            throw new CertPathValidatorException("certificate revoked, reason=(" + instance5.getRevocationReason() + "), date=" + instance5.getRevocationTime().getDate(), null, this.parameters.getCertPath(), this.parameters.getIndex());
                                                        }
                                                        throw new CertPathValidatorException("certificate revoked, details unknown", null, this.parameters.getCertPath(), this.parameters.getIndex());
                                                    }
                                                    return;
                                                }
                                            }
                                        }
                                        return;
                                    }
                                    return;
                                } catch (CertPathValidatorException e2) {
                                    throw e2;
                                } catch (Exception e3) {
                                    throw new CertPathValidatorException("unable to process OCSP response", e3, this.parameters.getCertPath(), this.parameters.getIndex());
                                }
                            } else {
                                return;
                            }
                        } else {
                            throw new CertPathValidatorException("OCSP response failed: " + instance.getResponseStatus().getValue(), null, this.parameters.getCertPath(), this.parameters.getIndex());
                        }
                    } else {
                        throw new RecoverableCertPathValidatorException("no OCSP response found for any certificate", null, this.parameters.getCertPath(), this.parameters.getIndex());
                    }
                } catch (URISyntaxException e4) {
                    throw new CertPathValidatorException("configuration error: " + e4.getMessage(), e4, this.parameters.getCertPath(), this.parameters.getIndex());
                }
            } else {
                ocspResponder = getOcspResponderURI(x509Certificate);
            }
        }
        uri = ocspResponder;
        if (ocspResponses.get(x509Certificate) == null) {
        }
        ocspExtensions = this.parent.getOcspExtensions();
        bArr = null;
        while (i != ocspExtensions.size()) {
        }
        z = false;
        if (ocspResponses.isEmpty()) {
        }
    }

    public List<CertPathValidatorException> getSoftFailExceptions() {
        return null;
    }

    public Set<String> getSupportedExtensions() {
        return null;
    }

    public void init(boolean z) throws CertPathValidatorException {
        if (z) {
            throw new CertPathValidatorException("forward checking not supported");
        }
    }

    @Override // org.bouncycastle.jcajce.PKIXCertRevocationChecker
    public void initialize(PKIXCertRevocationCheckerParameters pKIXCertRevocationCheckerParameters) {
        this.parameters = pKIXCertRevocationCheckerParameters;
        this.isEnabledOCSP = Properties.isOverrideSet("ocsp.enable");
        this.ocspURL = Properties.getPropertyValue("ocsp.responderURL");
    }

    public boolean isForwardCheckingSupported() {
        return false;
    }

    @Override // org.bouncycastle.jcajce.PKIXCertRevocationChecker
    public void setParameter(String str, Object obj) {
    }
}
