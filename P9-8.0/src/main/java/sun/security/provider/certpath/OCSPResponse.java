package sun.security.provider.certpath;

import java.io.IOException;
import java.security.AccessController;
import java.security.GeneralSecurityException;
import java.security.Signature;
import java.security.cert.CRLReason;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertPathValidatorException.BasicReason;
import java.security.cert.CertificateException;
import java.security.cert.Extension;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.security.auth.x500.X500Principal;
import sun.misc.HexDumpEncoder;
import sun.security.action.GetIntegerAction;
import sun.security.provider.certpath.OCSP.RevocationStatus;
import sun.security.provider.certpath.OCSP.RevocationStatus.CertStatus;
import sun.security.util.Debug;
import sun.security.util.DerInputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;
import sun.security.x509.AlgorithmId;
import sun.security.x509.KeyIdentifier;
import sun.security.x509.PKIXExtensions;
import sun.security.x509.X509CertImpl;

public final class OCSPResponse {
    private static final /* synthetic */ int[] -sun-security-provider-certpath-OCSPResponse$ResponseStatusSwitchesValues = null;
    private static final int CERT_STATUS_GOOD = 0;
    private static final int CERT_STATUS_REVOKED = 1;
    private static final int CERT_STATUS_UNKNOWN = 2;
    private static final int DEFAULT_MAX_CLOCK_SKEW = 900000;
    private static final int KEY_TAG = 2;
    private static final String KP_OCSP_SIGNING_OID = "1.3.6.1.5.5.7.3.9";
    private static final int MAX_CLOCK_SKEW = initializeClockSkew();
    private static final int NAME_TAG = 1;
    private static final ObjectIdentifier OCSP_BASIC_RESPONSE_OID = ObjectIdentifier.newInternal(new int[]{1, 3, 6, 1, 5, 5, 7, 48, 1, 1});
    private static final Debug debug = Debug.getInstance("certpath");
    private static final boolean dump = (debug != null ? Debug.isOn("ocsp") : false);
    private static ResponseStatus[] rsvalues = ResponseStatus.values();
    private static CRLReason[] values = CRLReason.values();
    private List<X509CertImpl> certs;
    private KeyIdentifier responderKeyId = null;
    private X500Principal responderName = null;
    private final byte[] responseNonce;
    private final ResponseStatus responseStatus;
    private final AlgorithmId sigAlgId;
    private final byte[] signature;
    private X509CertImpl signerCert = null;
    private final Map<CertId, SingleResponse> singleResponseMap;
    private final byte[] tbsResponseData;

    public enum ResponseStatus {
        SUCCESSFUL,
        MALFORMED_REQUEST,
        INTERNAL_ERROR,
        TRY_LATER,
        UNUSED,
        SIG_REQUIRED,
        UNAUTHORIZED
    }

    static final class SingleResponse implements RevocationStatus {
        private final CertId certId;
        private final CertStatus certStatus;
        private final Date nextUpdate;
        private final CRLReason revocationReason;
        private final Date revocationTime;
        private final Map<String, Extension> singleExtensions;
        private final Date thisUpdate;

        /* synthetic */ SingleResponse(DerValue der, SingleResponse -this1) {
            this(der);
        }

        private SingleResponse(DerValue der) throws IOException {
            if (der.tag != (byte) 48) {
                throw new IOException("Bad ASN.1 encoding in SingleResponse");
            }
            DerInputStream tmp = der.data;
            this.certId = new CertId(tmp.getDerValue().data);
            DerValue derVal = tmp.getDerValue();
            short tag = (short) ((byte) (derVal.tag & 31));
            if (tag == (short) 1) {
                this.certStatus = CertStatus.REVOKED;
                this.revocationTime = derVal.data.getGeneralizedTime();
                if (derVal.data.available() != 0) {
                    DerValue dv = derVal.data.getDerValue();
                    if (((short) ((byte) (dv.tag & 31))) == (short) 0) {
                        int reason = dv.data.getEnumerated();
                        if (reason < 0 || reason >= OCSPResponse.values.length) {
                            this.revocationReason = CRLReason.UNSPECIFIED;
                        } else {
                            this.revocationReason = OCSPResponse.values[reason];
                        }
                    } else {
                        this.revocationReason = CRLReason.UNSPECIFIED;
                    }
                } else {
                    this.revocationReason = CRLReason.UNSPECIFIED;
                }
                if (OCSPResponse.debug != null) {
                    OCSPResponse.debug.println("Revocation time: " + this.revocationTime);
                    OCSPResponse.debug.println("Revocation reason: " + this.revocationReason);
                }
            } else {
                this.revocationTime = null;
                this.revocationReason = CRLReason.UNSPECIFIED;
                if (tag == (short) 0) {
                    this.certStatus = CertStatus.GOOD;
                } else if (tag == (short) 2) {
                    this.certStatus = CertStatus.UNKNOWN;
                } else {
                    throw new IOException("Invalid certificate status");
                }
            }
            this.thisUpdate = tmp.getGeneralizedTime();
            if (tmp.available() == 0) {
                this.nextUpdate = null;
            } else {
                derVal = tmp.getDerValue();
                if (((short) ((byte) (derVal.tag & 31))) == (short) 0) {
                    this.nextUpdate = derVal.data.getGeneralizedTime();
                    if (tmp.available() != 0) {
                        tag = (short) ((byte) (tmp.getDerValue().tag & 31));
                    }
                } else {
                    this.nextUpdate = null;
                }
            }
            if (tmp.available() > 0) {
                derVal = tmp.getDerValue();
                if (derVal.isContextSpecific((byte) 1)) {
                    DerValue[] singleExtDer = derVal.data.getSequence(3);
                    this.singleExtensions = new HashMap(singleExtDer.length);
                    for (DerValue extension : singleExtDer) {
                        Object ext = new sun.security.x509.Extension(extension);
                        if (OCSPResponse.debug != null) {
                            OCSPResponse.debug.println("OCSP single extension: " + ext);
                        }
                        if (ext.isCritical()) {
                            throw new IOException("Unsupported OCSP critical extension: " + ext.getExtensionId());
                        }
                        this.singleExtensions.put(ext.getId(), ext);
                    }
                    return;
                }
                this.singleExtensions = Collections.emptyMap();
                return;
            }
            this.singleExtensions = Collections.emptyMap();
        }

        public CertStatus getCertStatus() {
            return this.certStatus;
        }

        private CertId getCertId() {
            return this.certId;
        }

        public Date getRevocationTime() {
            return (Date) this.revocationTime.clone();
        }

        public CRLReason getRevocationReason() {
            return this.revocationReason;
        }

        public Map<String, Extension> getSingleExtensions() {
            return Collections.unmodifiableMap(this.singleExtensions);
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("SingleResponse:  \n");
            sb.append(this.certId);
            sb.append("\nCertStatus: ").append(this.certStatus).append("\n");
            if (this.certStatus == CertStatus.REVOKED) {
                sb.append("revocationTime is ").append(this.revocationTime).append("\n");
                sb.append("revocationReason is ").append(this.revocationReason).append("\n");
            }
            sb.append("thisUpdate is ").append(this.thisUpdate).append("\n");
            if (this.nextUpdate != null) {
                sb.append("nextUpdate is ").append(this.nextUpdate).append("\n");
            }
            return sb.-java_util_stream_Collectors-mthref-7();
        }
    }

    private static /* synthetic */ int[] -getsun-security-provider-certpath-OCSPResponse$ResponseStatusSwitchesValues() {
        if (-sun-security-provider-certpath-OCSPResponse$ResponseStatusSwitchesValues != null) {
            return -sun-security-provider-certpath-OCSPResponse$ResponseStatusSwitchesValues;
        }
        int[] iArr = new int[ResponseStatus.values().length];
        try {
            iArr[ResponseStatus.INTERNAL_ERROR.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[ResponseStatus.MALFORMED_REQUEST.ordinal()] = 5;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[ResponseStatus.SIG_REQUIRED.ordinal()] = 6;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[ResponseStatus.SUCCESSFUL.ordinal()] = 2;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[ResponseStatus.TRY_LATER.ordinal()] = 3;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[ResponseStatus.UNAUTHORIZED.ordinal()] = 4;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[ResponseStatus.UNUSED.ordinal()] = 7;
        } catch (NoSuchFieldError e7) {
        }
        -sun-security-provider-certpath-OCSPResponse$ResponseStatusSwitchesValues = iArr;
        return iArr;
    }

    private static int initializeClockSkew() {
        Integer tmp = (Integer) AccessController.doPrivileged(new GetIntegerAction("com.sun.security.ocsp.clockSkew"));
        if (tmp == null || tmp.lambda$-java_util_stream_IntPipeline_14709() < 0) {
            return DEFAULT_MAX_CLOCK_SKEW;
        }
        return tmp.lambda$-java_util_stream_IntPipeline_14709() * 1000;
    }

    OCSPResponse(byte[] bytes) throws IOException {
        if (dump) {
            debug.println("OCSPResponse bytes...\n\n" + new HexDumpEncoder().encode(bytes) + "\n");
        }
        DerValue der = new DerValue(bytes);
        if (der.tag != (byte) 48) {
            throw new IOException("Bad encoding in OCSP response: expected ASN.1 SEQUENCE tag.");
        }
        DerInputStream derIn = der.getData();
        int status = derIn.getEnumerated();
        if (status < 0 || status >= rsvalues.length) {
            throw new IOException("Unknown OCSPResponse status: " + status);
        }
        this.responseStatus = rsvalues[status];
        if (debug != null) {
            debug.println("OCSP response status: " + this.responseStatus);
        }
        if (this.responseStatus != ResponseStatus.SUCCESSFUL) {
            this.singleResponseMap = Collections.emptyMap();
            this.certs = new ArrayList();
            this.sigAlgId = null;
            this.signature = null;
            this.tbsResponseData = null;
            this.responseNonce = null;
            return;
        }
        der = derIn.getDerValue();
        if (der.isContextSpecific((byte) 0)) {
            DerValue tmp = der.data.getDerValue();
            if (tmp.tag != (byte) 48) {
                throw new IOException("Bad encoding in responseBytes element of OCSP response: expected ASN.1 SEQUENCE tag.");
            }
            derIn = tmp.data;
            ObjectIdentifier responseType = derIn.getOID();
            if (responseType.equals((Object) OCSP_BASIC_RESPONSE_OID)) {
                if (debug != null) {
                    debug.println("OCSP response type: basic");
                }
                DerValue[] seqTmp = new DerInputStream(derIn.getOctetString()).getSequence(2);
                if (seqTmp.length < 3) {
                    throw new IOException("Unexpected BasicOCSPResponse value");
                }
                DerValue responseData = seqTmp[0];
                this.tbsResponseData = seqTmp[0].toByteArray();
                if (responseData.tag != (byte) 48) {
                    throw new IOException("Bad encoding in tbsResponseData element of OCSP response: expected ASN.1 SEQUENCE tag.");
                }
                int i;
                DerInputStream seqDerIn = responseData.data;
                DerValue seq = seqDerIn.getDerValue();
                if (seq.isContextSpecific((byte) 0) && seq.isConstructed() && seq.isContextSpecific()) {
                    seq = seq.data.getDerValue();
                    int version = seq.getInteger();
                    if (seq.data.available() != 0) {
                        throw new IOException("Bad encoding in version  element of OCSP response: bad format");
                    }
                    seq = seqDerIn.getDerValue();
                }
                short tag = (short) ((byte) (seq.tag & 31));
                if (tag == (short) 1) {
                    this.responderName = new X500Principal(seq.getData().toByteArray());
                    if (debug != null) {
                        debug.println("Responder's name: " + this.responderName);
                    }
                } else if (tag == (short) 2) {
                    this.responderKeyId = new KeyIdentifier(seq.getData().getOctetString());
                    if (debug != null) {
                        debug.println("Responder's key ID: " + Debug.toString(this.responderKeyId.getIdentifier()));
                    }
                } else {
                    throw new IOException("Bad encoding in responderID element of OCSP response: expected ASN.1 context specific tag 0 or 1");
                }
                seq = seqDerIn.getDerValue();
                if (debug != null) {
                    debug.println("OCSP response produced at: " + seq.getGeneralizedTime());
                }
                DerValue[] singleResponseDer = seqDerIn.getSequence(1);
                this.singleResponseMap = new HashMap(singleResponseDer.length);
                if (debug != null) {
                    debug.println("OCSP number of SingleResponses: " + singleResponseDer.length);
                }
                for (DerValue singleResponse : singleResponseDer) {
                    SingleResponse singleResponse2 = new SingleResponse(singleResponse, null);
                    this.singleResponseMap.put(singleResponse2.getCertId(), singleResponse2);
                }
                byte[] bArr = null;
                if (seqDerIn.available() > 0) {
                    seq = seqDerIn.getDerValue();
                    if (seq.isContextSpecific((byte) 1)) {
                        DerValue[] responseExtDer = seq.data.getSequence(3);
                        for (DerValue singleResponse3 : responseExtDer) {
                            Object ext = new sun.security.x509.Extension(singleResponse3);
                            if (debug != null) {
                                debug.println("OCSP extension: " + ext);
                            }
                            if (ext.getExtensionId().equals(OCSP.NONCE_EXTENSION_OID)) {
                                bArr = ext.getExtensionValue();
                            } else if (ext.isCritical()) {
                                throw new IOException("Unsupported OCSP critical extension: " + ext.getExtensionId());
                            }
                        }
                    }
                }
                this.responseNonce = bArr;
                this.sigAlgId = AlgorithmId.parse(seqTmp[1]);
                this.signature = seqTmp[2].getBitString();
                if (seqTmp.length > 3) {
                    DerValue seqCert = seqTmp[3];
                    if (seqCert.isContextSpecific((byte) 0)) {
                        DerValue[] derCerts = seqCert.getData().getSequence(3);
                        this.certs = new ArrayList(derCerts.length);
                        i = 0;
                        while (i < derCerts.length) {
                            try {
                                X509CertImpl cert = new X509CertImpl(derCerts[i].toByteArray());
                                this.certs.-java_util_stream_Collectors-mthref-2(cert);
                                if (debug != null) {
                                    debug.println("OCSP response cert #" + (i + 1) + ": " + cert.getSubjectX500Principal());
                                }
                                i++;
                            } catch (CertificateException ce) {
                                throw new IOException("Bad encoding in X509 Certificate", ce);
                            }
                        }
                    }
                    throw new IOException("Bad encoding in certs element of OCSP response: expected ASN.1 context specific tag 0.");
                }
                this.certs = new ArrayList();
                return;
            }
            if (debug != null) {
                debug.println("OCSP response type: " + responseType);
            }
            throw new IOException("Unsupported OCSP response type: " + responseType);
        }
        throw new IOException("Bad encoding in responseBytes element of OCSP response: expected ASN.1 context specific tag 0.");
    }

    void verify(List<CertId> certIds, X509Certificate issuerCert, X509Certificate responderCert, Date date, byte[] nonce) throws CertPathValidatorException {
        switch (-getsun-security-provider-certpath-OCSPResponse$ResponseStatusSwitchesValues()[this.responseStatus.ordinal()]) {
            case 1:
            case 3:
                throw new CertPathValidatorException("OCSP response error: " + this.responseStatus, null, null, -1, BasicReason.UNDETERMINED_REVOCATION_STATUS);
            case 2:
                SingleResponse sr;
                for (Object certId : certIds) {
                    sr = getSingleResponse(certId);
                    if (sr == null) {
                        if (debug != null) {
                            debug.println("No response found for CertId: " + certId);
                        }
                        throw new CertPathValidatorException("OCSP response does not include a response for a certificate supplied in the OCSP request");
                    } else if (debug != null) {
                        debug.println("Status of certificate (with serial number " + certId.getSerialNumber() + ") is: " + sr.getCertStatus());
                    }
                }
                if (this.signerCert == null) {
                    try {
                        this.certs.-java_util_stream_Collectors-mthref-2(X509CertImpl.toImpl(issuerCert));
                        if (responderCert != null) {
                            this.certs.-java_util_stream_Collectors-mthref-2(X509CertImpl.toImpl(responderCert));
                        }
                        if (this.responderName != null) {
                            for (X509CertImpl cert : this.certs) {
                                if (cert.getSubjectX500Principal().equals(this.responderName)) {
                                    this.signerCert = cert;
                                }
                            }
                        } else if (this.responderKeyId != null) {
                            for (X509CertImpl cert2 : this.certs) {
                                KeyIdentifier certKeyId = cert2.getSubjectKeyId();
                                if (certKeyId == null || !this.responderKeyId.equals(certKeyId)) {
                                    try {
                                        certKeyId = new KeyIdentifier(cert2.getPublicKey());
                                    } catch (IOException e) {
                                    }
                                    if (this.responderKeyId.equals(certKeyId)) {
                                        this.signerCert = cert2;
                                    }
                                } else {
                                    this.signerCert = cert2;
                                }
                            }
                        }
                    } catch (CertificateException ce) {
                        throw new CertPathValidatorException("Invalid issuer or trusted responder certificate", ce);
                    }
                }
                if (this.signerCert != null) {
                    if (this.signerCert.equals(issuerCert)) {
                        if (debug != null) {
                            debug.println("OCSP response is signed by the target's Issuing CA");
                        }
                    } else if (this.signerCert.equals(responderCert)) {
                        if (debug != null) {
                            debug.println("OCSP response is signed by a Trusted Responder");
                        }
                    } else if (this.signerCert.getIssuerX500Principal().equals(issuerCert.getSubjectX500Principal())) {
                        try {
                            List<String> keyPurposes = this.signerCert.getExtendedKeyUsage();
                            if (keyPurposes != null) {
                                if ((keyPurposes.contains(KP_OCSP_SIGNING_OID) ^ 1) == 0) {
                                    AlgorithmChecker algChecker = new AlgorithmChecker(new TrustAnchor(issuerCert, null));
                                    algChecker.init(false);
                                    algChecker.check(this.signerCert, Collections.emptySet());
                                    if (date == null) {
                                        try {
                                            this.signerCert.checkValidity();
                                        } catch (Throwable e2) {
                                            throw new CertPathValidatorException("Responder's certificate not within the validity period", e2);
                                        }
                                    }
                                    this.signerCert.checkValidity(date);
                                    if (!(this.signerCert.getExtension(PKIXExtensions.OCSPNoCheck_Id) == null || debug == null)) {
                                        debug.println("Responder's certificate includes the extension id-pkix-ocsp-nocheck.");
                                    }
                                    try {
                                        this.signerCert.verify(issuerCert.getPublicKey());
                                        if (debug != null) {
                                            debug.println("OCSP response is signed by an Authorized Responder");
                                        }
                                    } catch (GeneralSecurityException e3) {
                                        this.signerCert = null;
                                    }
                                }
                            }
                            throw new CertPathValidatorException("Responder's certificate not valid for signing OCSP responses");
                        } catch (Throwable cpe) {
                            throw new CertPathValidatorException("Responder's certificate not valid for signing OCSP responses", cpe);
                        }
                    } else {
                        throw new CertPathValidatorException("Responder's certificate is not authorized to sign OCSP responses");
                    }
                }
                if (this.signerCert != null) {
                    AlgorithmChecker.check(this.signerCert.getPublicKey(), this.sigAlgId);
                    if (verifySignature(this.signerCert)) {
                        if (!(nonce == null || this.responseNonce == null)) {
                            if ((Arrays.equals(nonce, this.responseNonce) ^ 1) != 0) {
                                throw new CertPathValidatorException("Nonces don't match");
                            }
                        }
                        long now = date == null ? System.currentTimeMillis() : date.getTime();
                        Date date2 = new Date(((long) MAX_CLOCK_SKEW) + now);
                        date2 = new Date(now - ((long) MAX_CLOCK_SKEW));
                        for (SingleResponse sr2 : this.singleResponseMap.values()) {
                            if (debug != null) {
                                String until = "";
                                if (sr2.nextUpdate != null) {
                                    until = " until " + sr2.nextUpdate;
                                }
                                debug.println("OCSP response validity interval is from " + sr2.thisUpdate + until);
                                debug.println("Checking validity of OCSP response on: " + new Date(now));
                            }
                            if (!date2.before(sr2.thisUpdate)) {
                                if (date2.after(sr2.nextUpdate != null ? sr2.nextUpdate : sr2.thisUpdate)) {
                                }
                            }
                            throw new CertPathValidatorException("Response is unreliable: its validity interval is out-of-date");
                        }
                        return;
                    }
                    throw new CertPathValidatorException("Error verifying OCSP Response's signature");
                }
                throw new CertPathValidatorException("Unable to verify OCSP Response's signature");
            default:
                throw new CertPathValidatorException("OCSP response error: " + this.responseStatus);
        }
    }

    ResponseStatus getResponseStatus() {
        return this.responseStatus;
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x003a A:{Splitter: B:0:0x0000, ExcHandler: java.security.InvalidKeyException (r0_0 'e' java.lang.Throwable)} */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x003a A:{Splitter: B:0:0x0000, ExcHandler: java.security.InvalidKeyException (r0_0 'e' java.lang.Throwable)} */
    /* JADX WARNING: Missing block: B:13:0x003a, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:15:0x0040, code:
            throw new java.security.cert.CertPathValidatorException(r0);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean verifySignature(X509Certificate cert) throws CertPathValidatorException {
        try {
            Signature respSignature = Signature.getInstance(this.sigAlgId.getName());
            respSignature.initVerify(cert.getPublicKey());
            respSignature.update(this.tbsResponseData);
            if (respSignature.verify(this.signature)) {
                if (debug != null) {
                    debug.println("Verified signature of OCSP Response");
                }
                return true;
            }
            if (debug != null) {
                debug.println("Error verifying signature of OCSP Response");
            }
            return false;
        } catch (Throwable e) {
        }
    }

    SingleResponse getSingleResponse(CertId certId) {
        return (SingleResponse) this.singleResponseMap.get(certId);
    }

    X509Certificate getSignerCertificate() {
        return this.signerCert;
    }
}
