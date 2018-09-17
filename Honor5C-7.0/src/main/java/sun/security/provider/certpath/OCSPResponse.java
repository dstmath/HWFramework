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
import sun.util.calendar.BaseCalendar;
import sun.util.logging.PlatformLogger;

public final class OCSPResponse {
    private static final /* synthetic */ int[] -sun-security-provider-certpath-OCSPResponse$ResponseStatusSwitchesValues = null;
    private static final int CERT_STATUS_GOOD = 0;
    private static final int CERT_STATUS_REVOKED = 1;
    private static final int CERT_STATUS_UNKNOWN = 2;
    private static final int DEFAULT_MAX_CLOCK_SKEW = 900000;
    private static final int KEY_TAG = 2;
    private static final String KP_OCSP_SIGNING_OID = "1.3.6.1.5.5.7.3.9";
    private static final int MAX_CLOCK_SKEW = 0;
    private static final int NAME_TAG = 1;
    private static final ObjectIdentifier OCSP_BASIC_RESPONSE_OID = null;
    private static final Debug debug = null;
    private static final boolean dump = false;
    private static ResponseStatus[] rsvalues;
    private static CRLReason[] values;
    private List<X509CertImpl> certs;
    private KeyIdentifier responderKeyId;
    private X500Principal responderName;
    private final byte[] responseNonce;
    private final ResponseStatus responseStatus;
    private final AlgorithmId sigAlgId;
    private final byte[] signature;
    private X509CertImpl signerCert;
    private final Map<CertId, SingleResponse> singleResponseMap;
    private final byte[] tbsResponseData;

    public enum ResponseStatus {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.provider.certpath.OCSPResponse.ResponseStatus.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.provider.certpath.OCSPResponse.ResponseStatus.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.security.provider.certpath.OCSPResponse.ResponseStatus.<clinit>():void");
        }
    }

    static final class SingleResponse implements RevocationStatus {
        private final CertId certId;
        private final CertStatus certStatus;
        private final Date nextUpdate;
        private final CRLReason revocationReason;
        private final Date revocationTime;
        private final Map<String, Extension> singleExtensions;
        private final Date thisUpdate;

        /* synthetic */ SingleResponse(DerValue der, SingleResponse singleResponse) {
            this(der);
        }

        private SingleResponse(DerValue der) throws IOException {
            if (der.tag != 48) {
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
                    for (int i = OCSPResponse.MAX_CLOCK_SKEW; i < singleExtDer.length; i += OCSPResponse.NAME_TAG) {
                        Object ext = new sun.security.x509.Extension(singleExtDer[i]);
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
            return sb.toString();
        }
    }

    private static /* synthetic */ int[] -getsun-security-provider-certpath-OCSPResponse$ResponseStatusSwitchesValues() {
        if (-sun-security-provider-certpath-OCSPResponse$ResponseStatusSwitchesValues != null) {
            return -sun-security-provider-certpath-OCSPResponse$ResponseStatusSwitchesValues;
        }
        int[] iArr = new int[ResponseStatus.values().length];
        try {
            iArr[ResponseStatus.INTERNAL_ERROR.ordinal()] = NAME_TAG;
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
            iArr[ResponseStatus.SUCCESSFUL.ordinal()] = KEY_TAG;
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.provider.certpath.OCSPResponse.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.provider.certpath.OCSPResponse.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.security.provider.certpath.OCSPResponse.<clinit>():void");
    }

    private static int initializeClockSkew() {
        Integer tmp = (Integer) AccessController.doPrivileged(new GetIntegerAction("com.sun.security.ocsp.clockSkew"));
        if (tmp == null || tmp.intValue() < 0) {
            return DEFAULT_MAX_CLOCK_SKEW;
        }
        return tmp.intValue() * PlatformLogger.SEVERE;
    }

    OCSPResponse(byte[] bytes) throws IOException {
        this.signerCert = null;
        this.responderName = null;
        this.responderKeyId = null;
        if (dump) {
            debug.println("OCSPResponse bytes...\n\n" + new HexDumpEncoder().encode(bytes) + "\n");
        }
        DerValue der = new DerValue(bytes);
        byte b = der.tag;
        if (r0 != 48) {
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
            b = tmp.tag;
            if (r0 != 48) {
                throw new IOException("Bad encoding in responseBytes element of OCSP response: expected ASN.1 SEQUENCE tag.");
            }
            derIn = tmp.data;
            ObjectIdentifier responseType = derIn.getOID();
            if (responseType.equals(OCSP_BASIC_RESPONSE_OID)) {
                if (debug != null) {
                    debug.println("OCSP response type: basic");
                }
                DerValue[] seqTmp = new DerInputStream(derIn.getOctetString()).getSequence(KEY_TAG);
                int length = seqTmp.length;
                if (r0 < 3) {
                    throw new IOException("Unexpected BasicOCSPResponse value");
                }
                DerValue responseData = seqTmp[MAX_CLOCK_SKEW];
                this.tbsResponseData = seqTmp[MAX_CLOCK_SKEW].toByteArray();
                b = responseData.tag;
                if (r0 != 48) {
                    throw new IOException("Bad encoding in tbsResponseData element of OCSP response: expected ASN.1 SEQUENCE tag.");
                }
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
                if (tag == NAME_TAG) {
                    this.responderName = new X500Principal(seq.getData().toByteArray());
                    if (debug != null) {
                        debug.println("Responder's name: " + this.responderName);
                    }
                } else if (tag == KEY_TAG) {
                    this.responderKeyId = new KeyIdentifier(seq.getData().getOctetString());
                    if (debug != null) {
                        debug.println("Responder's key ID: " + Debug.toString(this.responderKeyId.getIdentifier()));
                    }
                } else {
                    throw new IOException("Bad encoding in responderID element of OCSP response: expected ASN.1 context specific tag 0 or 1");
                }
                seq = seqDerIn.getDerValue();
                if (debug != null) {
                    Object producedAtDate = seq.getGeneralizedTime();
                    debug.println("OCSP response produced at: " + producedAtDate);
                }
                DerValue[] singleResponseDer = seqDerIn.getSequence(NAME_TAG);
                this.singleResponseMap = new HashMap(singleResponseDer.length);
                if (debug != null) {
                    debug.println("OCSP number of SingleResponses: " + singleResponseDer.length);
                }
                int i = MAX_CLOCK_SKEW;
                while (true) {
                    length = singleResponseDer.length;
                    if (i >= r0) {
                        break;
                    }
                    SingleResponse singleResponse = new SingleResponse(singleResponseDer[i], null);
                    this.singleResponseMap.put(singleResponse.getCertId(), singleResponse);
                    i += NAME_TAG;
                }
                byte[] bArr = null;
                if (seqDerIn.available() > 0) {
                    seq = seqDerIn.getDerValue();
                    if (seq.isContextSpecific((byte) 1)) {
                        Object ext;
                        DerValue[] responseExtDer = seq.data.getSequence(3);
                        i = MAX_CLOCK_SKEW;
                        while (true) {
                            length = responseExtDer.length;
                            if (i >= r0) {
                                break;
                            }
                            ext = new sun.security.x509.Extension(responseExtDer[i]);
                            if (debug != null) {
                                debug.println("OCSP extension: " + ext);
                            }
                            if (ext.getExtensionId().equals(OCSP.NONCE_EXTENSION_OID)) {
                                bArr = ext.getExtensionValue();
                            } else if (ext.isCritical()) {
                                break;
                            }
                            i += NAME_TAG;
                        }
                        throw new IOException("Unsupported OCSP critical extension: " + ext.getExtensionId());
                    }
                }
                this.responseNonce = bArr;
                this.sigAlgId = AlgorithmId.parse(seqTmp[NAME_TAG]);
                this.signature = seqTmp[KEY_TAG].getBitString();
                length = seqTmp.length;
                if (r0 > 3) {
                    DerValue seqCert = seqTmp[3];
                    if (seqCert.isContextSpecific((byte) 0)) {
                        DerValue[] derCerts = seqCert.getData().getSequence(3);
                        this.certs = new ArrayList(derCerts.length);
                        i = MAX_CLOCK_SKEW;
                        while (true) {
                            try {
                                length = derCerts.length;
                                if (i >= r0) {
                                    break;
                                }
                                X509CertImpl cert = new X509CertImpl(derCerts[i].toByteArray());
                                this.certs.add(cert);
                                if (debug != null) {
                                    debug.println("OCSP response cert #" + (i + NAME_TAG) + ": " + cert.getSubjectX500Principal());
                                }
                                i += NAME_TAG;
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
            case NAME_TAG /*1*/:
            case BaseCalendar.TUESDAY /*3*/:
                throw new CertPathValidatorException("OCSP response error: " + this.responseStatus, null, null, -1, BasicReason.UNDETERMINED_REVOCATION_STATUS);
            case KEY_TAG /*2*/:
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
                        this.certs.add(X509CertImpl.toImpl(issuerCert));
                        if (responderCert != null) {
                            this.certs.add(X509CertImpl.toImpl(responderCert));
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
                                if (keyPurposes.contains(KP_OCSP_SIGNING_OID)) {
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
                            if (!Arrays.equals(nonce, this.responseNonce)) {
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
                                debug.println("Response's validity interval is from " + sr2.thisUpdate + until);
                            }
                            if (sr2.thisUpdate != null) {
                                if (date2.before(sr2.thisUpdate)) {
                                    throw new CertPathValidatorException("Response is unreliable: its validity interval is out-of-date");
                                }
                            }
                            if (sr2.nextUpdate != null) {
                                if (date2.after(sr2.nextUpdate)) {
                                    throw new CertPathValidatorException("Response is unreliable: its validity interval is out-of-date");
                                }
                            }
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
            throw new CertPathValidatorException(e);
        }
    }

    SingleResponse getSingleResponse(CertId certId) {
        return (SingleResponse) this.singleResponseMap.get(certId);
    }

    X509Certificate getSignerCertificate() {
        return this.signerCert;
    }
}
