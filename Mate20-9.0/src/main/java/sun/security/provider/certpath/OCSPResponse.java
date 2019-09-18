package sun.security.provider.certpath;

import java.io.IOException;
import java.security.AccessController;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CRLReason;
import java.security.cert.CertPathValidatorException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateParsingException;
import java.security.cert.Extension;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.security.auth.x500.X500Principal;
import sun.misc.HexDumpEncoder;
import sun.security.action.GetIntegerAction;
import sun.security.provider.certpath.OCSP;
import sun.security.util.Debug;
import sun.security.util.DerInputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;
import sun.security.x509.AlgorithmId;
import sun.security.x509.KeyIdentifier;
import sun.security.x509.PKIXExtensions;
import sun.security.x509.X509CertImpl;

public final class OCSPResponse {
    private static final int CERT_STATUS_GOOD = 0;
    private static final int CERT_STATUS_REVOKED = 1;
    private static final int CERT_STATUS_UNKNOWN = 2;
    private static final int DEFAULT_MAX_CLOCK_SKEW = 900000;
    private static final int KEY_TAG = 2;
    private static final String KP_OCSP_SIGNING_OID = "1.3.6.1.5.5.7.3.9";
    private static final int MAX_CLOCK_SKEW = initializeClockSkew();
    private static final int NAME_TAG = 1;
    private static final ObjectIdentifier OCSP_BASIC_RESPONSE_OID = ObjectIdentifier.newInternal(new int[]{1, 3, 6, 1, 5, 5, 7, 48, 1, 1});
    /* access modifiers changed from: private */
    public static final Debug debug = Debug.getInstance("certpath");
    private static final boolean dump = (debug != null && Debug.isOn("ocsp"));
    private static ResponseStatus[] rsvalues = ResponseStatus.values();
    /* access modifiers changed from: private */
    public static CRLReason[] values = CRLReason.values();
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

    static final class SingleResponse implements OCSP.RevocationStatus {
        private final CertId certId;
        private final OCSP.RevocationStatus.CertStatus certStatus;
        /* access modifiers changed from: private */
        public final Date nextUpdate;
        private final CRLReason revocationReason;
        private final Date revocationTime;
        private final Map<String, Extension> singleExtensions;
        /* access modifiers changed from: private */
        public final Date thisUpdate;

        private SingleResponse(DerValue der) throws IOException {
            if (der.tag == 48) {
                DerInputStream tmp = der.data;
                this.certId = new CertId(tmp.getDerValue().data);
                DerValue derVal = tmp.getDerValue();
                short tag = (short) ((byte) (derVal.tag & 31));
                if (tag == 1) {
                    this.certStatus = OCSP.RevocationStatus.CertStatus.REVOKED;
                    this.revocationTime = derVal.data.getGeneralizedTime();
                    if (derVal.data.available() != 0) {
                        DerValue dv = derVal.data.getDerValue();
                        if (((short) ((byte) (dv.tag & 31))) == 0) {
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
                        Debug access$500 = OCSPResponse.debug;
                        access$500.println("Revocation time: " + this.revocationTime);
                        Debug access$5002 = OCSPResponse.debug;
                        access$5002.println("Revocation reason: " + this.revocationReason);
                    }
                } else {
                    this.revocationTime = null;
                    this.revocationReason = CRLReason.UNSPECIFIED;
                    if (tag == 0) {
                        this.certStatus = OCSP.RevocationStatus.CertStatus.GOOD;
                    } else if (tag == 2) {
                        this.certStatus = OCSP.RevocationStatus.CertStatus.UNKNOWN;
                    } else {
                        throw new IOException("Invalid certificate status");
                    }
                }
                this.thisUpdate = tmp.getGeneralizedTime();
                if (tmp.available() == 0) {
                    this.nextUpdate = null;
                } else {
                    DerValue derVal2 = tmp.getDerValue();
                    if (((short) ((byte) (derVal2.tag & 31))) == 0) {
                        this.nextUpdate = derVal2.data.getGeneralizedTime();
                        if (tmp.available() != 0) {
                            short tag2 = (short) ((byte) (tmp.getDerValue().tag & 31));
                        }
                    } else {
                        this.nextUpdate = null;
                    }
                }
                if (tmp.available() > 0) {
                    DerValue derVal3 = tmp.getDerValue();
                    if (derVal3.isContextSpecific((byte) 1)) {
                        DerValue[] singleExtDer = derVal3.data.getSequence(3);
                        this.singleExtensions = new HashMap(singleExtDer.length);
                        int i = 0;
                        while (i < singleExtDer.length) {
                            sun.security.x509.Extension ext = new sun.security.x509.Extension(singleExtDer[i]);
                            if (OCSPResponse.debug != null) {
                                Debug access$5003 = OCSPResponse.debug;
                                access$5003.println("OCSP single extension: " + ext);
                            }
                            if (!ext.isCritical()) {
                                this.singleExtensions.put(ext.getId(), ext);
                                i++;
                            } else {
                                throw new IOException("Unsupported OCSP critical extension: " + ext.getExtensionId());
                            }
                        }
                        return;
                    }
                    this.singleExtensions = Collections.emptyMap();
                    return;
                }
                this.singleExtensions = Collections.emptyMap();
                return;
            }
            throw new IOException("Bad ASN.1 encoding in SingleResponse");
        }

        public OCSP.RevocationStatus.CertStatus getCertStatus() {
            return this.certStatus;
        }

        /* access modifiers changed from: private */
        public CertId getCertId() {
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
            sb.append((Object) this.certId);
            sb.append("\nCertStatus: " + this.certStatus + "\n");
            if (this.certStatus == OCSP.RevocationStatus.CertStatus.REVOKED) {
                sb.append("revocationTime is " + this.revocationTime + "\n");
                sb.append("revocationReason is " + this.revocationReason + "\n");
            }
            sb.append("thisUpdate is " + this.thisUpdate + "\n");
            if (this.nextUpdate != null) {
                sb.append("nextUpdate is " + this.nextUpdate + "\n");
            }
            return sb.toString();
        }
    }

    private static int initializeClockSkew() {
        Integer tmp = (Integer) AccessController.doPrivileged(new GetIntegerAction("com.sun.security.ocsp.clockSkew"));
        if (tmp == null || tmp.intValue() < 0) {
            return DEFAULT_MAX_CLOCK_SKEW;
        }
        return tmp.intValue() * 1000;
    }

    OCSPResponse(byte[] bytes) throws IOException {
        DerValue seq;
        byte[] nonce;
        DerValue[] derCerts;
        DerInputStream seqDerIn;
        DerInputStream derIn;
        byte[] bArr = bytes;
        if (dump) {
            new HexDumpEncoder();
            debug.println("OCSPResponse bytes...\n\n" + hexEnc.encode(bArr) + "\n");
        }
        DerValue der = new DerValue(bArr);
        if (der.tag == 48) {
            DerInputStream derIn2 = der.getData();
            int status = derIn2.getEnumerated();
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
            DerValue der2 = derIn2.getDerValue();
            if (der2.isContextSpecific((byte) 0)) {
                DerValue tmp = der2.data.getDerValue();
                if (tmp.tag == 48) {
                    DerInputStream derIn3 = tmp.data;
                    if (derIn3.getOID().equals((Object) OCSP_BASIC_RESPONSE_OID)) {
                        if (debug != null) {
                            debug.println("OCSP response type: basic");
                        }
                        DerValue[] seqTmp = new DerInputStream(derIn3.getOctetString()).getSequence(2);
                        if (seqTmp.length >= 3) {
                            DerValue responseData = seqTmp[0];
                            this.tbsResponseData = seqTmp[0].toByteArray();
                            if (responseData.tag == 48) {
                                DerInputStream seqDerIn2 = responseData.data;
                                DerValue seq2 = seqDerIn2.getDerValue();
                                if (seq2.isContextSpecific((byte) 0) && seq2.isConstructed() && seq2.isContextSpecific()) {
                                    DerValue seq3 = seq2.data.getDerValue();
                                    int integer = seq3.getInteger();
                                    if (seq3.data.available() == 0) {
                                        seq2 = seqDerIn2.getDerValue();
                                    } else {
                                        throw new IOException("Bad encoding in version  element of OCSP response: bad format");
                                    }
                                }
                                short tag = (short) ((byte) (seq2.tag & 31));
                                if (tag == 1) {
                                    this.responderName = new X500Principal(seq2.getData().toByteArray());
                                    if (debug != null) {
                                        debug.println("Responder's name: " + this.responderName);
                                    }
                                } else if (tag == 2) {
                                    this.responderKeyId = new KeyIdentifier(seq2.getData().getOctetString());
                                    if (debug != null) {
                                        debug.println("Responder's key ID: " + Debug.toString(this.responderKeyId.getIdentifier()));
                                    }
                                } else {
                                    DerInputStream derInputStream = derIn3;
                                    DerInputStream derInputStream2 = seqDerIn2;
                                    throw new IOException("Bad encoding in responderID element of OCSP response: expected ASN.1 context specific tag 0 or 1");
                                }
                                DerValue seq4 = seqDerIn2.getDerValue();
                                if (debug != null) {
                                    Date producedAtDate = seq4.getGeneralizedTime();
                                    Debug debug2 = debug;
                                    StringBuilder sb = new StringBuilder();
                                    seq = seq4;
                                    sb.append("OCSP response produced at: ");
                                    sb.append((Object) producedAtDate);
                                    debug2.println(sb.toString());
                                } else {
                                    seq = seq4;
                                }
                                DerValue[] singleResponseDer = seqDerIn2.getSequence(1);
                                this.singleResponseMap = new HashMap(singleResponseDer.length);
                                if (debug != null) {
                                    debug.println("OCSP number of SingleResponses: " + singleResponseDer.length);
                                }
                                int i = 0;
                                while (i < singleResponseDer.length) {
                                    SingleResponse singleResponse = new SingleResponse(singleResponseDer[i]);
                                    this.singleResponseMap.put(singleResponse.getCertId(), singleResponse);
                                    i++;
                                    byte[] bArr2 = bytes;
                                }
                                if (seqDerIn2.available() > 0) {
                                    DerValue seq5 = seqDerIn2.getDerValue();
                                    if (seq5.isContextSpecific((byte) 1)) {
                                        DerValue[] responseExtDer = seq5.data.getSequence(3);
                                        nonce = null;
                                        int i2 = 0;
                                        while (true) {
                                            DerValue seq6 = seq5;
                                            if (i2 >= responseExtDer.length) {
                                                DerInputStream derInputStream3 = derIn3;
                                                DerInputStream derInputStream4 = seqDerIn2;
                                                break;
                                            }
                                            DerValue der3 = der2;
                                            sun.security.x509.Extension ext = new sun.security.x509.Extension(responseExtDer[i2]);
                                            if (debug != null) {
                                                Debug debug3 = debug;
                                                derIn = derIn3;
                                                StringBuilder sb2 = new StringBuilder();
                                                seqDerIn = seqDerIn2;
                                                sb2.append("OCSP extension: ");
                                                sb2.append((Object) ext);
                                                debug3.println(sb2.toString());
                                            } else {
                                                derIn = derIn3;
                                                seqDerIn = seqDerIn2;
                                            }
                                            if (ext.getExtensionId().equals((Object) OCSP.NONCE_EXTENSION_OID)) {
                                                nonce = ext.getExtensionValue();
                                            } else if (ext.isCritical()) {
                                                throw new IOException("Unsupported OCSP critical extension: " + ext.getExtensionId());
                                            }
                                            i2++;
                                            seq5 = seq6;
                                            der2 = der3;
                                            derIn3 = derIn;
                                            seqDerIn2 = seqDerIn;
                                        }
                                    } else {
                                        DerValue derValue = der2;
                                        DerInputStream derInputStream5 = derIn3;
                                        DerInputStream derInputStream6 = seqDerIn2;
                                        nonce = null;
                                    }
                                } else {
                                    DerInputStream derInputStream7 = derIn3;
                                    DerInputStream derInputStream8 = seqDerIn2;
                                    nonce = null;
                                    DerValue derValue2 = seq;
                                }
                                this.responseNonce = nonce;
                                this.sigAlgId = AlgorithmId.parse(seqTmp[1]);
                                this.signature = seqTmp[2].getBitString();
                                if (seqTmp.length > 3) {
                                    DerValue seqCert = seqTmp[3];
                                    int i3 = 0;
                                    if (seqCert.isContextSpecific((byte) 0)) {
                                        DerValue[] derCerts2 = seqCert.getData().getSequence(3);
                                        this.certs = new ArrayList(derCerts2.length);
                                        while (i3 < derCerts2.length) {
                                            try {
                                                X509CertImpl cert = new X509CertImpl(derCerts2[i3].toByteArray());
                                                this.certs.add(cert);
                                                if (debug != null) {
                                                    Debug debug4 = debug;
                                                    StringBuilder sb3 = new StringBuilder();
                                                    derCerts = derCerts2;
                                                    try {
                                                        sb3.append("OCSP response cert #");
                                                        sb3.append(i3 + 1);
                                                        sb3.append(": ");
                                                        sb3.append((Object) cert.getSubjectX500Principal());
                                                        debug4.println(sb3.toString());
                                                    } catch (CertificateException e) {
                                                        ce = e;
                                                    }
                                                } else {
                                                    derCerts = derCerts2;
                                                }
                                                i3++;
                                                derCerts2 = derCerts;
                                            } catch (CertificateException e2) {
                                                ce = e2;
                                                DerValue[] derValueArr = derCerts2;
                                                throw new IOException("Bad encoding in X509 Certificate", ce);
                                            }
                                        }
                                    } else {
                                        throw new IOException("Bad encoding in certs element of OCSP response: expected ASN.1 context specific tag 0.");
                                    }
                                } else {
                                    this.certs = new ArrayList();
                                }
                                return;
                            }
                            DerInputStream derInputStream9 = derIn3;
                            throw new IOException("Bad encoding in tbsResponseData element of OCSP response: expected ASN.1 SEQUENCE tag.");
                        }
                        DerInputStream derInputStream10 = derIn3;
                        throw new IOException("Unexpected BasicOCSPResponse value");
                    }
                    DerInputStream derInputStream11 = derIn3;
                    if (debug != null) {
                        debug.println("OCSP response type: " + responseType);
                    }
                    throw new IOException("Unsupported OCSP response type: " + responseType);
                }
                throw new IOException("Bad encoding in responseBytes element of OCSP response: expected ASN.1 SEQUENCE tag.");
            }
            throw new IOException("Bad encoding in responseBytes element of OCSP response: expected ASN.1 context specific tag 0.");
        }
        throw new IOException("Bad encoding in OCSP response: expected ASN.1 SEQUENCE tag.");
    }

    /* access modifiers changed from: package-private */
    public void verify(List<CertId> certIds, X509Certificate issuerCert, X509Certificate responderCert, Date date, byte[] nonce) throws CertPathValidatorException {
        switch (this.responseStatus) {
            case SUCCESSFUL:
                for (CertId certId : certIds) {
                    if (getSingleResponse(certId) == null) {
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
                        this.certs.add(X509CertImpl.toImpl(issuerCert));
                        if (responderCert != null) {
                            this.certs.add(X509CertImpl.toImpl(responderCert));
                        }
                        if (this.responderName != null) {
                            Iterator<X509CertImpl> it = this.certs.iterator();
                            while (true) {
                                if (it.hasNext()) {
                                    X509CertImpl cert = it.next();
                                    if (cert.getSubjectX500Principal().equals(this.responderName)) {
                                        this.signerCert = cert;
                                    }
                                }
                            }
                        } else if (this.responderKeyId != null) {
                            Iterator<X509CertImpl> it2 = this.certs.iterator();
                            while (true) {
                                if (it2.hasNext()) {
                                    X509CertImpl cert2 = it2.next();
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
                            if (keyPurposes == null || !keyPurposes.contains(KP_OCSP_SIGNING_OID)) {
                                throw new CertPathValidatorException("Responder's certificate not valid for signing OCSP responses");
                            }
                            AlgorithmChecker algChecker = new AlgorithmChecker(new TrustAnchor(issuerCert, null));
                            algChecker.init(false);
                            algChecker.check((Certificate) this.signerCert, (Collection<String>) Collections.emptySet());
                            if (date == null) {
                                try {
                                    this.signerCert.checkValidity();
                                } catch (CertificateException e2) {
                                    throw new CertPathValidatorException("Responder's certificate not within the validity period", e2);
                                }
                            } else {
                                this.signerCert.checkValidity(date);
                            }
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
                        } catch (CertificateParsingException cpe) {
                            throw new CertPathValidatorException("Responder's certificate not valid for signing OCSP responses", cpe);
                        }
                    } else {
                        throw new CertPathValidatorException("Responder's certificate is not authorized to sign OCSP responses");
                    }
                }
                if (this.signerCert != null) {
                    AlgorithmChecker.check(this.signerCert.getPublicKey(), this.sigAlgId);
                    if (!verifySignature(this.signerCert)) {
                        throw new CertPathValidatorException("Error verifying OCSP Response's signature");
                    } else if (nonce == null || this.responseNonce == null || Arrays.equals(nonce, this.responseNonce)) {
                        long now = date == null ? System.currentTimeMillis() : date.getTime();
                        Date nowPlusSkew = new Date(((long) MAX_CLOCK_SKEW) + now);
                        Date nowMinusSkew = new Date(now - ((long) MAX_CLOCK_SKEW));
                        for (SingleResponse sr : this.singleResponseMap.values()) {
                            if (debug != null) {
                                String until = "";
                                if (sr.nextUpdate != null) {
                                    until = " until " + sr.nextUpdate;
                                }
                                debug.println("OCSP response validity interval is from " + sr.thisUpdate + until);
                                debug.println("Checking validity of OCSP response on: " + new Date(now));
                            }
                            if (!nowPlusSkew.before(sr.thisUpdate)) {
                                if (nowMinusSkew.after(sr.nextUpdate != null ? sr.nextUpdate : sr.thisUpdate)) {
                                }
                            }
                            throw new CertPathValidatorException("Response is unreliable: its validity interval is out-of-date");
                        }
                        return;
                    } else {
                        throw new CertPathValidatorException("Nonces don't match");
                    }
                } else {
                    throw new CertPathValidatorException("Unable to verify OCSP Response's signature");
                }
            case TRY_LATER:
            case INTERNAL_ERROR:
                CertPathValidatorException certPathValidatorException = new CertPathValidatorException("OCSP response error: " + this.responseStatus, null, null, -1, CertPathValidatorException.BasicReason.UNDETERMINED_REVOCATION_STATUS);
                throw certPathValidatorException;
            default:
                throw new CertPathValidatorException("OCSP response error: " + this.responseStatus);
        }
    }

    /* access modifiers changed from: package-private */
    public ResponseStatus getResponseStatus() {
        return this.responseStatus;
    }

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
        } catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException e) {
            throw new CertPathValidatorException((Throwable) e);
        }
    }

    /* access modifiers changed from: package-private */
    public SingleResponse getSingleResponse(CertId certId) {
        return this.singleResponseMap.get(certId);
    }

    /* access modifiers changed from: package-private */
    public X509Certificate getSignerCertificate() {
        return this.signerCert;
    }
}
