package sun.security.x509;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import javax.security.auth.x500.X500Principal;
import sun.misc.BASE64Decoder;
import sun.misc.HexDumpEncoder;
import sun.security.provider.X509Factory;
import sun.security.util.DerEncoder;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;
import sun.util.calendar.BaseCalendar;
import sun.util.logging.PlatformLogger;

public class X509CertImpl extends X509Certificate implements DerEncoder {
    public static final String ALG_ID = "algorithm";
    private static final String AUTH_INFO_ACCESS_OID = "1.3.6.1.5.5.7.1.1";
    private static final String BASIC_CONSTRAINT_OID = "2.5.29.19";
    private static final String DOT = ".";
    private static final String EXTENDED_KEY_USAGE_OID = "2.5.29.37";
    public static final String INFO = "info";
    private static final String ISSUER_ALT_NAME_OID = "2.5.29.18";
    public static final String ISSUER_DN = "x509.info.issuer.dname";
    private static final String KEY_USAGE_OID = "2.5.29.15";
    public static final String NAME = "x509";
    private static final int NUM_STANDARD_KEY_USAGE = 9;
    public static final String PUBLIC_KEY = "x509.info.key.value";
    public static final String SERIAL_ID = "x509.info.serialNumber.number";
    public static final String SIG = "x509.signature";
    public static final String SIGNATURE = "signature";
    public static final String SIGNED_CERT = "signed_cert";
    public static final String SIG_ALG = "x509.algorithm";
    private static final String SUBJECT_ALT_NAME_OID = "2.5.29.17";
    public static final String SUBJECT_DN = "x509.info.subject.dname";
    public static final String VERSION = "x509.info.version.number";
    private static final long serialVersionUID = -3457612960190864406L;
    protected AlgorithmId algId;
    private Set<AccessDescription> authInfoAccess;
    private List<String> extKeyUsage;
    private ConcurrentHashMap<String, String> fingerprints;
    protected X509CertInfo info;
    private Collection<List<?>> issuerAlternativeNames;
    private boolean readOnly;
    protected byte[] signature;
    private byte[] signedCert;
    private Collection<List<?>> subjectAlternativeNames;
    private boolean verificationResult;
    private String verifiedProvider;
    private PublicKey verifiedPublicKey;

    public X509CertImpl() {
        this.readOnly = false;
        this.signedCert = null;
        this.info = null;
        this.algId = null;
        this.signature = null;
        this.fingerprints = new ConcurrentHashMap(2);
    }

    public X509CertImpl(byte[] certData) throws CertificateException {
        this.readOnly = false;
        this.signedCert = null;
        this.info = null;
        this.algId = null;
        this.signature = null;
        this.fingerprints = new ConcurrentHashMap(2);
        try {
            parse(new DerValue(certData));
        } catch (Object e) {
            this.signedCert = null;
            throw new CertificateException("Unable to initialize, " + e, e);
        }
    }

    public X509CertImpl(InputStream in) throws CertificateException {
        DerValue der;
        this.readOnly = false;
        this.signedCert = null;
        this.info = null;
        this.algId = null;
        this.signature = null;
        this.fingerprints = new ConcurrentHashMap(2);
        InputStream inBuffered = new BufferedInputStream(in);
        try {
            inBuffered.mark(PlatformLogger.OFF);
            der = readRFC1421Cert(inBuffered);
        } catch (IOException e) {
            try {
                inBuffered.reset();
                der = new DerValue(inBuffered);
            } catch (IOException ioe1) {
                throw new CertificateException("Input stream must be either DER-encoded bytes or RFC1421 hex-encoded DER-encoded bytes: " + ioe1.getMessage(), ioe1);
            }
        }
        try {
            parse(der);
        } catch (Object ioe) {
            this.signedCert = null;
            throw new CertificateException("Unable to parse DER value of certificate, " + ioe, ioe);
        }
    }

    private DerValue readRFC1421Cert(InputStream in) throws IOException {
        BufferedReader certBufferedReader = new BufferedReader(new InputStreamReader(in, "ASCII"));
        try {
            if (certBufferedReader.readLine().equals(X509Factory.BEGIN_CERT)) {
                BASE64Decoder decoder = new BASE64Decoder();
                ByteArrayOutputStream decstream = new ByteArrayOutputStream();
                while (true) {
                    try {
                        String line = certBufferedReader.readLine();
                        if (line == null) {
                            return null;
                        }
                        if (line.equals(X509Factory.END_CERT)) {
                            return new DerValue(decstream.toByteArray());
                        }
                        decstream.write(decoder.decodeBuffer(line));
                    } catch (IOException ioe2) {
                        throw new IOException("Unable to read InputStream: " + ioe2.getMessage());
                    }
                }
            }
            throw new IOException("InputStream is not RFC1421 hex-encoded DER bytes");
        } catch (IOException ioe1) {
            throw new IOException("Unable to read InputStream: " + ioe1.getMessage());
        }
    }

    public X509CertImpl(X509CertInfo certInfo) {
        this.readOnly = false;
        this.signedCert = null;
        this.info = null;
        this.algId = null;
        this.signature = null;
        this.fingerprints = new ConcurrentHashMap(2);
        this.info = certInfo;
    }

    public X509CertImpl(DerValue derVal) throws CertificateException {
        this.readOnly = false;
        this.signedCert = null;
        this.info = null;
        this.algId = null;
        this.signature = null;
        this.fingerprints = new ConcurrentHashMap(2);
        try {
            parse(derVal);
        } catch (Object e) {
            this.signedCert = null;
            throw new CertificateException("Unable to initialize, " + e, e);
        }
    }

    public X509CertImpl(DerValue derVal, byte[] encoded) throws CertificateException {
        this.readOnly = false;
        this.signedCert = null;
        this.info = null;
        this.algId = null;
        this.signature = null;
        this.fingerprints = new ConcurrentHashMap(2);
        try {
            parse(derVal, encoded);
        } catch (Object e) {
            this.signedCert = null;
            throw new CertificateException("Unable to initialize, " + e, e);
        }
    }

    public void encode(OutputStream out) throws CertificateEncodingException {
        if (this.signedCert == null) {
            throw new CertificateEncodingException("Null certificate to encode");
        }
        try {
            out.write((byte[]) this.signedCert.clone());
        } catch (IOException e) {
            throw new CertificateEncodingException(e.toString());
        }
    }

    public void derEncode(OutputStream out) throws IOException {
        if (this.signedCert == null) {
            throw new IOException("Null certificate to encode");
        }
        out.write((byte[]) this.signedCert.clone());
    }

    public byte[] getEncoded() throws CertificateEncodingException {
        return (byte[]) getEncodedInternal().clone();
    }

    public byte[] getEncodedInternal() throws CertificateEncodingException {
        if (this.signedCert != null) {
            return this.signedCert;
        }
        throw new CertificateEncodingException("Null certificate to encode");
    }

    public void verify(PublicKey key) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {
        verify(key, "");
    }

    public synchronized void verify(PublicKey key, String sigProvider) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {
        if (sigProvider == null) {
            sigProvider = "";
        }
        if (this.verifiedPublicKey != null && this.verifiedPublicKey.equals(key) && sigProvider.equals(this.verifiedProvider)) {
            if (!this.verificationResult) {
                throw new SignatureException("Signature does not match.");
            }
        } else if (this.signedCert == null) {
            throw new CertificateEncodingException("Uninitialized certificate");
        } else {
            Signature sigVerf;
            if (sigProvider.length() == 0) {
                sigVerf = Signature.getInstance(this.algId.getName());
            } else {
                sigVerf = Signature.getInstance(this.algId.getName(), sigProvider);
            }
            sigVerf.initVerify(key);
            byte[] rawCert = this.info.getEncodedInfo();
            sigVerf.update(rawCert, 0, rawCert.length);
            this.verificationResult = sigVerf.verify(this.signature);
            this.verifiedPublicKey = key;
            this.verifiedProvider = sigProvider;
            if (!this.verificationResult) {
                throw new SignatureException("Signature does not match.");
            }
        }
    }

    public synchronized void verify(PublicKey key, Provider sigProvider) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        if (this.signedCert == null) {
            throw new CertificateEncodingException("Uninitialized certificate");
        }
        Signature sigVerf;
        if (sigProvider == null) {
            sigVerf = Signature.getInstance(this.algId.getName());
        } else {
            sigVerf = Signature.getInstance(this.algId.getName(), sigProvider);
        }
        sigVerf.initVerify(key);
        byte[] rawCert = this.info.getEncodedInfo();
        sigVerf.update(rawCert, 0, rawCert.length);
        this.verificationResult = sigVerf.verify(this.signature);
        this.verifiedPublicKey = key;
        if (!this.verificationResult) {
            throw new SignatureException("Signature does not match.");
        }
    }

    public static void verify(X509Certificate cert, PublicKey key, Provider sigProvider) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        cert.verify(key, sigProvider);
    }

    public void sign(PrivateKey key, String algorithm) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {
        sign(key, algorithm, null);
    }

    public void sign(PrivateKey key, String algorithm, String provider) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {
        try {
            if (this.readOnly) {
                throw new CertificateEncodingException("cannot over-write existing certificate");
            }
            Signature sigEngine;
            DerOutputStream out;
            DerOutputStream tmp;
            byte[] rawCert;
            if (provider != null) {
                if (provider.length() != 0) {
                    sigEngine = Signature.getInstance(algorithm, provider);
                    sigEngine.initSign(key);
                    this.algId = AlgorithmId.get(sigEngine.getAlgorithm());
                    out = new DerOutputStream();
                    tmp = new DerOutputStream();
                    this.info.encode(tmp);
                    rawCert = tmp.toByteArray();
                    this.algId.encode(tmp);
                    sigEngine.update(rawCert, 0, rawCert.length);
                    this.signature = sigEngine.sign();
                    tmp.putBitString(this.signature);
                    out.write((byte) DerValue.tag_SequenceOf, tmp);
                    this.signedCert = out.toByteArray();
                    this.readOnly = true;
                }
            }
            sigEngine = Signature.getInstance(algorithm);
            sigEngine.initSign(key);
            this.algId = AlgorithmId.get(sigEngine.getAlgorithm());
            out = new DerOutputStream();
            tmp = new DerOutputStream();
            this.info.encode(tmp);
            rawCert = tmp.toByteArray();
            this.algId.encode(tmp);
            sigEngine.update(rawCert, 0, rawCert.length);
            this.signature = sigEngine.sign();
            tmp.putBitString(this.signature);
            out.write((byte) DerValue.tag_SequenceOf, tmp);
            this.signedCert = out.toByteArray();
            this.readOnly = true;
        } catch (IOException e) {
            throw new CertificateEncodingException(e.toString());
        }
    }

    public void checkValidity() throws CertificateExpiredException, CertificateNotYetValidException {
        checkValidity(new Date());
    }

    public void checkValidity(Date date) throws CertificateExpiredException, CertificateNotYetValidException {
        try {
            CertificateValidity interval = (CertificateValidity) this.info.get(X509CertInfo.VALIDITY);
            if (interval == null) {
                throw new CertificateNotYetValidException("Null validity period");
            }
            interval.valid(date);
        } catch (Exception e) {
            throw new CertificateNotYetValidException("Incorrect validity period");
        }
    }

    public Object get(String name) throws CertificateParsingException {
        X509AttributeName attr = new X509AttributeName(name);
        String id = attr.getPrefix();
        if (id.equalsIgnoreCase(NAME)) {
            X509AttributeName attr2 = new X509AttributeName(attr.getSuffix());
            id = attr2.getPrefix();
            if (id.equalsIgnoreCase(INFO)) {
                if (this.info == null) {
                    return null;
                }
                if (attr2.getSuffix() == null) {
                    return this.info;
                }
                try {
                    return this.info.get(attr2.getSuffix());
                } catch (IOException e) {
                    throw new CertificateParsingException(e.toString());
                } catch (CertificateException e2) {
                    throw new CertificateParsingException(e2.toString());
                }
            } else if (id.equalsIgnoreCase(ALG_ID)) {
                return this.algId;
            } else {
                if (id.equalsIgnoreCase(SIGNATURE)) {
                    if (this.signature != null) {
                        return this.signature.clone();
                    }
                    return null;
                } else if (!id.equalsIgnoreCase(SIGNED_CERT)) {
                    throw new CertificateParsingException("Attribute name not recognized or get() not allowed for the same: " + id);
                } else if (this.signedCert != null) {
                    return this.signedCert.clone();
                } else {
                    return null;
                }
            }
        }
        throw new CertificateParsingException("Invalid root of attribute name, expected [x509], received [" + id + "]");
    }

    public void set(String name, Object obj) throws CertificateException, IOException {
        if (this.readOnly) {
            throw new CertificateException("cannot over-write existing certificate");
        }
        X509AttributeName attr = new X509AttributeName(name);
        String id = attr.getPrefix();
        if (id.equalsIgnoreCase(NAME)) {
            X509AttributeName attr2 = new X509AttributeName(attr.getSuffix());
            id = attr2.getPrefix();
            if (!id.equalsIgnoreCase(INFO)) {
                throw new CertificateException("Attribute name not recognized or set() not allowed for the same: " + id);
            } else if (attr2.getSuffix() != null) {
                this.info.set(attr2.getSuffix(), obj);
                this.signedCert = null;
                return;
            } else if (obj instanceof X509CertInfo) {
                this.info = (X509CertInfo) obj;
                this.signedCert = null;
                return;
            } else {
                throw new CertificateException("Attribute value should be of type X509CertInfo.");
            }
        }
        throw new CertificateException("Invalid root of attribute name, expected [x509], received " + id);
    }

    public void delete(String name) throws CertificateException, IOException {
        if (this.readOnly) {
            throw new CertificateException("cannot over-write existing certificate");
        }
        X509AttributeName attr = new X509AttributeName(name);
        String id = attr.getPrefix();
        if (id.equalsIgnoreCase(NAME)) {
            X509AttributeName attr2 = new X509AttributeName(attr.getSuffix());
            id = attr2.getPrefix();
            if (id.equalsIgnoreCase(INFO)) {
                if (attr2.getSuffix() != null) {
                    this.info = null;
                    return;
                } else {
                    this.info.delete(attr2.getSuffix());
                    return;
                }
            } else if (id.equalsIgnoreCase(ALG_ID)) {
                this.algId = null;
                return;
            } else if (id.equalsIgnoreCase(SIGNATURE)) {
                this.signature = null;
                return;
            } else if (id.equalsIgnoreCase(SIGNED_CERT)) {
                this.signedCert = null;
                return;
            } else {
                throw new CertificateException("Attribute name not recognized or delete() not allowed for the same: " + id);
            }
        }
        throw new CertificateException("Invalid root of attribute name, expected [x509], received " + id);
    }

    public Enumeration<String> getElements() {
        AttributeNameEnumeration elements = new AttributeNameEnumeration();
        elements.addElement(X509CertInfo.IDENT);
        elements.addElement(SIG_ALG);
        elements.addElement(SIG);
        elements.addElement("x509.signed_cert");
        return elements.elements();
    }

    public String getName() {
        return NAME;
    }

    public String toString() {
        if (this.info == null || this.algId == null || this.signature == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        sb.append(this.info.toString()).append("\n");
        sb.append("  Algorithm: [").append(this.algId.toString()).append("]\n");
        sb.append("  Signature:\n").append(new HexDumpEncoder().encodeBuffer(this.signature));
        sb.append("\n]");
        return sb.toString();
    }

    public PublicKey getPublicKey() {
        if (this.info == null) {
            return null;
        }
        try {
            return (PublicKey) this.info.get("key.value");
        } catch (Exception e) {
            return null;
        }
    }

    public int getVersion() {
        if (this.info == null) {
            return -1;
        }
        try {
            return ((Integer) this.info.get("version.number")).intValue() + 1;
        } catch (Exception e) {
            return -1;
        }
    }

    public BigInteger getSerialNumber() {
        SerialNumber ser = getSerialNumberObject();
        if (ser != null) {
            return ser.getNumber();
        }
        return null;
    }

    public SerialNumber getSerialNumberObject() {
        if (this.info == null) {
            return null;
        }
        try {
            return (SerialNumber) this.info.get("serialNumber.number");
        } catch (Exception e) {
            return null;
        }
    }

    public Principal getSubjectDN() {
        if (this.info == null) {
            return null;
        }
        try {
            return (Principal) this.info.get("subject.dname");
        } catch (Exception e) {
            return null;
        }
    }

    public X500Principal getSubjectX500Principal() {
        if (this.info == null) {
            return null;
        }
        try {
            return (X500Principal) this.info.get("subject.x500principal");
        } catch (Exception e) {
            return null;
        }
    }

    public Principal getIssuerDN() {
        if (this.info == null) {
            return null;
        }
        try {
            return (Principal) this.info.get("issuer.dname");
        } catch (Exception e) {
            return null;
        }
    }

    public X500Principal getIssuerX500Principal() {
        if (this.info == null) {
            return null;
        }
        try {
            return (X500Principal) this.info.get("issuer.x500principal");
        } catch (Exception e) {
            return null;
        }
    }

    public Date getNotBefore() {
        if (this.info == null) {
            return null;
        }
        try {
            return (Date) this.info.get("validity.notBefore");
        } catch (Exception e) {
            return null;
        }
    }

    public Date getNotAfter() {
        if (this.info == null) {
            return null;
        }
        try {
            return (Date) this.info.get("validity.notAfter");
        } catch (Exception e) {
            return null;
        }
    }

    public byte[] getTBSCertificate() throws CertificateEncodingException {
        if (this.info != null) {
            return this.info.getEncodedInfo();
        }
        throw new CertificateEncodingException("Uninitialized certificate");
    }

    public byte[] getSignature() {
        if (this.signature == null) {
            return null;
        }
        byte[] dup = new byte[this.signature.length];
        System.arraycopy(this.signature, 0, dup, 0, dup.length);
        return dup;
    }

    public String getSigAlgName() {
        if (this.algId == null) {
            return null;
        }
        return this.algId.getName();
    }

    public String getSigAlgOID() {
        if (this.algId == null) {
            return null;
        }
        return this.algId.getOID().toString();
    }

    public byte[] getSigAlgParams() {
        if (this.algId == null) {
            return null;
        }
        try {
            return this.algId.getEncodedParams();
        } catch (IOException e) {
            return null;
        }
    }

    public boolean[] getIssuerUniqueID() {
        if (this.info == null) {
            return null;
        }
        try {
            UniqueIdentity id = (UniqueIdentity) this.info.get(X509CertInfo.ISSUER_ID);
            if (id == null) {
                return null;
            }
            return id.getId();
        } catch (Exception e) {
            return null;
        }
    }

    public boolean[] getSubjectUniqueID() {
        if (this.info == null) {
            return null;
        }
        try {
            UniqueIdentity id = (UniqueIdentity) this.info.get(X509CertInfo.SUBJECT_ID);
            if (id == null) {
                return null;
            }
            return id.getId();
        } catch (Exception e) {
            return null;
        }
    }

    public KeyIdentifier getAuthKeyId() {
        AuthorityKeyIdentifierExtension aki = getAuthorityKeyIdentifierExtension();
        if (aki != null) {
            try {
                return (KeyIdentifier) aki.get(SubjectKeyIdentifierExtension.KEY_ID);
            } catch (IOException e) {
            }
        }
        return null;
    }

    public KeyIdentifier getSubjectKeyId() {
        SubjectKeyIdentifierExtension ski = getSubjectKeyIdentifierExtension();
        if (ski != null) {
            try {
                return ski.get(SubjectKeyIdentifierExtension.KEY_ID);
            } catch (IOException e) {
            }
        }
        return null;
    }

    public AuthorityKeyIdentifierExtension getAuthorityKeyIdentifierExtension() {
        return (AuthorityKeyIdentifierExtension) getExtension(PKIXExtensions.AuthorityKey_Id);
    }

    public BasicConstraintsExtension getBasicConstraintsExtension() {
        return (BasicConstraintsExtension) getExtension(PKIXExtensions.BasicConstraints_Id);
    }

    public CertificatePoliciesExtension getCertificatePoliciesExtension() {
        return (CertificatePoliciesExtension) getExtension(PKIXExtensions.CertificatePolicies_Id);
    }

    public ExtendedKeyUsageExtension getExtendedKeyUsageExtension() {
        return (ExtendedKeyUsageExtension) getExtension(PKIXExtensions.ExtendedKeyUsage_Id);
    }

    public IssuerAlternativeNameExtension getIssuerAlternativeNameExtension() {
        return (IssuerAlternativeNameExtension) getExtension(PKIXExtensions.IssuerAlternativeName_Id);
    }

    public NameConstraintsExtension getNameConstraintsExtension() {
        return (NameConstraintsExtension) getExtension(PKIXExtensions.NameConstraints_Id);
    }

    public PolicyConstraintsExtension getPolicyConstraintsExtension() {
        return (PolicyConstraintsExtension) getExtension(PKIXExtensions.PolicyConstraints_Id);
    }

    public PolicyMappingsExtension getPolicyMappingsExtension() {
        return (PolicyMappingsExtension) getExtension(PKIXExtensions.PolicyMappings_Id);
    }

    public PrivateKeyUsageExtension getPrivateKeyUsageExtension() {
        return (PrivateKeyUsageExtension) getExtension(PKIXExtensions.PrivateKeyUsage_Id);
    }

    public SubjectAlternativeNameExtension getSubjectAlternativeNameExtension() {
        return (SubjectAlternativeNameExtension) getExtension(PKIXExtensions.SubjectAlternativeName_Id);
    }

    public SubjectKeyIdentifierExtension getSubjectKeyIdentifierExtension() {
        return (SubjectKeyIdentifierExtension) getExtension(PKIXExtensions.SubjectKey_Id);
    }

    public CRLDistributionPointsExtension getCRLDistributionPointsExtension() {
        return (CRLDistributionPointsExtension) getExtension(PKIXExtensions.CRLDistributionPoints_Id);
    }

    public boolean hasUnsupportedCriticalExtension() {
        if (this.info == null) {
            return false;
        }
        try {
            CertificateExtensions exts = (CertificateExtensions) this.info.get(X509CertInfo.EXTENSIONS);
            if (exts == null) {
                return false;
            }
            return exts.hasUnsupportedCriticalExtension();
        } catch (Exception e) {
            return false;
        }
    }

    public Set<String> getCriticalExtensionOIDs() {
        if (this.info == null) {
            return null;
        }
        try {
            CertificateExtensions exts = (CertificateExtensions) this.info.get(X509CertInfo.EXTENSIONS);
            if (exts == null) {
                return null;
            }
            Set<String> extSet = new TreeSet();
            for (Extension ex : exts.getAllExtensions()) {
                if (ex.isCritical()) {
                    extSet.add(ex.getExtensionId().toString());
                }
            }
            return extSet;
        } catch (Exception e) {
            return null;
        }
    }

    public Set<String> getNonCriticalExtensionOIDs() {
        if (this.info == null) {
            return null;
        }
        try {
            CertificateExtensions exts = (CertificateExtensions) this.info.get(X509CertInfo.EXTENSIONS);
            if (exts == null) {
                return null;
            }
            Set<String> extSet = new TreeSet();
            for (Extension ex : exts.getAllExtensions()) {
                if (!ex.isCritical()) {
                    extSet.add(ex.getExtensionId().toString());
                }
            }
            extSet.addAll(exts.getUnparseableExtensions().keySet());
            return extSet;
        } catch (Exception e) {
            return null;
        }
    }

    public Extension getExtension(ObjectIdentifier oid) {
        if (this.info == null) {
            return null;
        }
        try {
            CertificateExtensions extensions = (CertificateExtensions) this.info.get(X509CertInfo.EXTENSIONS);
            if (extensions == null) {
                return null;
            }
            try {
                Extension ex = extensions.getExtension(oid.toString());
                if (ex != null) {
                    return ex;
                }
                for (Extension ex2 : extensions.getAllExtensions()) {
                    if (ex2.getExtensionId().equals((Object) oid)) {
                        return ex2;
                    }
                }
                return null;
            } catch (IOException e) {
                return null;
            }
        } catch (CertificateException e2) {
            return null;
        }
    }

    public Extension getUnparseableExtension(ObjectIdentifier oid) {
        if (this.info == null) {
            return null;
        }
        try {
            CertificateExtensions extensions = (CertificateExtensions) this.info.get(X509CertInfo.EXTENSIONS);
            if (extensions == null) {
                return null;
            }
            try {
                return (Extension) extensions.getUnparseableExtensions().get(oid.toString());
            } catch (IOException e) {
                return null;
            }
        } catch (CertificateException e2) {
            return null;
        }
    }

    public byte[] getExtensionValue(String oid) {
        try {
            Object findOID = new ObjectIdentifier(oid);
            String extAlias = OIDMap.getName(findOID);
            Extension certExt = null;
            CertificateExtensions exts = (CertificateExtensions) this.info.get(X509CertInfo.EXTENSIONS);
            if (extAlias == null) {
                if (exts != null) {
                    for (Extension ex : exts.getAllExtensions()) {
                        if (ex.getExtensionId().equals(findOID)) {
                            certExt = ex;
                            break;
                        }
                    }
                }
                return null;
            }
            try {
                certExt = (Extension) get(extAlias);
            } catch (CertificateException e) {
            }
            if (certExt == null) {
                if (exts != null) {
                    certExt = (Extension) exts.getUnparseableExtensions().get(oid);
                }
                if (certExt == null) {
                    return null;
                }
            }
            byte[] extData = certExt.getExtensionValue();
            if (extData == null) {
                return null;
            }
            DerOutputStream out = new DerOutputStream();
            out.putOctetString(extData);
            return out.toByteArray();
        } catch (Exception e2) {
            return null;
        }
    }

    public boolean[] getKeyUsage() {
        try {
            String extAlias = OIDMap.getName(PKIXExtensions.KeyUsage_Id);
            if (extAlias == null) {
                return null;
            }
            KeyUsageExtension certExt = (KeyUsageExtension) get(extAlias);
            if (certExt == null) {
                return null;
            }
            boolean[] ret = certExt.getBits();
            if (ret.length < NUM_STANDARD_KEY_USAGE) {
                boolean[] usageBits = new boolean[NUM_STANDARD_KEY_USAGE];
                System.arraycopy(ret, 0, usageBits, 0, ret.length);
                ret = usageBits;
            }
            return ret;
        } catch (Exception e) {
            return null;
        }
    }

    public synchronized List<String> getExtendedKeyUsage() throws CertificateParsingException {
        if (!this.readOnly || this.extKeyUsage == null) {
            ExtendedKeyUsageExtension ext = getExtendedKeyUsageExtension();
            if (ext == null) {
                return null;
            }
            this.extKeyUsage = Collections.unmodifiableList(ext.getExtendedKeyUsage());
            return this.extKeyUsage;
        }
        return this.extKeyUsage;
    }

    public static List<String> getExtendedKeyUsage(X509Certificate cert) throws CertificateParsingException {
        try {
            byte[] ext = cert.getExtensionValue(EXTENDED_KEY_USAGE_OID);
            if (ext == null) {
                return null;
            }
            return Collections.unmodifiableList(new ExtendedKeyUsageExtension(Boolean.FALSE, new DerValue(ext).getOctetString()).getExtendedKeyUsage());
        } catch (Throwable ioe) {
            throw new CertificateParsingException(ioe);
        }
    }

    public int getBasicConstraints() {
        try {
            String extAlias = OIDMap.getName(PKIXExtensions.BasicConstraints_Id);
            if (extAlias == null) {
                return -1;
            }
            BasicConstraintsExtension certExt = (BasicConstraintsExtension) get(extAlias);
            if (certExt != null && ((Boolean) certExt.get(BasicConstraintsExtension.IS_CA)).booleanValue()) {
                return ((Integer) certExt.get(BasicConstraintsExtension.PATH_LEN)).intValue();
            }
            return -1;
        } catch (Exception e) {
            return -1;
        }
    }

    private static Collection<List<?>> makeAltNames(GeneralNames names) {
        if (names.isEmpty()) {
            return Collections.emptySet();
        }
        List<List<?>> newNames = new ArrayList();
        for (GeneralName gname : names.names()) {
            GeneralNameInterface name = gname.getName();
            List<Object> nameEntry = new ArrayList(2);
            nameEntry.add(Integer.valueOf(name.getType()));
            switch (name.getType()) {
                case BaseCalendar.SUNDAY /*1*/:
                    nameEntry.add(((RFC822Name) name).getName());
                    break;
                case BaseCalendar.MONDAY /*2*/:
                    nameEntry.add(((DNSName) name).getName());
                    break;
                case BaseCalendar.WEDNESDAY /*4*/:
                    nameEntry.add(((X500Name) name).getRFC2253Name());
                    break;
                case BaseCalendar.JUNE /*6*/:
                    nameEntry.add(((URIName) name).getName());
                    break;
                case BaseCalendar.SATURDAY /*7*/:
                    try {
                        nameEntry.add(((IPAddressName) name).getName());
                        break;
                    } catch (IOException ioe) {
                        throw new RuntimeException("IPAddress cannot be parsed", ioe);
                    }
                case BaseCalendar.AUGUST /*8*/:
                    nameEntry.add(((OIDName) name).getOID().toString());
                    break;
                default:
                    DerOutputStream derOut = new DerOutputStream();
                    try {
                        name.encode(derOut);
                        nameEntry.add(derOut.toByteArray());
                        break;
                    } catch (IOException ioe2) {
                        throw new RuntimeException("name cannot be encoded", ioe2);
                    }
            }
            newNames.add(Collections.unmodifiableList(nameEntry));
        }
        return Collections.unmodifiableCollection(newNames);
    }

    private static Collection<List<?>> cloneAltNames(Collection<List<?>> altNames) {
        boolean mustClone = false;
        for (List<?> nameEntry : altNames) {
            if (nameEntry.get(1) instanceof byte[]) {
                mustClone = true;
            }
        }
        if (!mustClone) {
            return altNames;
        }
        List<List<?>> namesCopy = new ArrayList();
        for (Collection nameEntry2 : altNames) {
            Object nameObject = nameEntry2.get(1);
            if (nameObject instanceof byte[]) {
                List<Object> nameEntryCopy = new ArrayList(nameEntry2);
                nameEntryCopy.set(1, ((byte[]) nameObject).clone());
                namesCopy.add(Collections.unmodifiableList(nameEntryCopy));
            } else {
                namesCopy.add(nameEntry2);
            }
        }
        return Collections.unmodifiableCollection(namesCopy);
    }

    public synchronized Collection<List<?>> getSubjectAlternativeNames() throws CertificateParsingException {
        if (!this.readOnly || this.subjectAlternativeNames == null) {
            SubjectAlternativeNameExtension subjectAltNameExt = getSubjectAlternativeNameExtension();
            if (subjectAltNameExt == null) {
                return null;
            }
            try {
                this.subjectAlternativeNames = makeAltNames(subjectAltNameExt.get(SubjectAlternativeNameExtension.SUBJECT_NAME));
                return this.subjectAlternativeNames;
            } catch (IOException e) {
                return Collections.emptySet();
            }
        }
        return cloneAltNames(this.subjectAlternativeNames);
    }

    public static Collection<List<?>> getSubjectAlternativeNames(X509Certificate cert) throws CertificateParsingException {
        try {
            byte[] ext = cert.getExtensionValue(SUBJECT_ALT_NAME_OID);
            if (ext == null) {
                return null;
            }
            try {
                return makeAltNames(new SubjectAlternativeNameExtension(Boolean.FALSE, new DerValue(ext).getOctetString()).get(SubjectAlternativeNameExtension.SUBJECT_NAME));
            } catch (IOException e) {
                return Collections.emptySet();
            }
        } catch (Throwable ioe) {
            throw new CertificateParsingException(ioe);
        }
    }

    public synchronized Collection<List<?>> getIssuerAlternativeNames() throws CertificateParsingException {
        if (!this.readOnly || this.issuerAlternativeNames == null) {
            IssuerAlternativeNameExtension issuerAltNameExt = getIssuerAlternativeNameExtension();
            if (issuerAltNameExt == null) {
                return null;
            }
            try {
                this.issuerAlternativeNames = makeAltNames(issuerAltNameExt.get(IssuerAlternativeNameExtension.ISSUER_NAME));
                return this.issuerAlternativeNames;
            } catch (IOException e) {
                return Collections.emptySet();
            }
        }
        return cloneAltNames(this.issuerAlternativeNames);
    }

    public static Collection<List<?>> getIssuerAlternativeNames(X509Certificate cert) throws CertificateParsingException {
        try {
            byte[] ext = cert.getExtensionValue(ISSUER_ALT_NAME_OID);
            if (ext == null) {
                return null;
            }
            try {
                return makeAltNames(new IssuerAlternativeNameExtension(Boolean.FALSE, new DerValue(ext).getOctetString()).get(IssuerAlternativeNameExtension.ISSUER_NAME));
            } catch (IOException e) {
                return Collections.emptySet();
            }
        } catch (Throwable ioe) {
            throw new CertificateParsingException(ioe);
        }
    }

    public AuthorityInfoAccessExtension getAuthorityInfoAccessExtension() {
        return (AuthorityInfoAccessExtension) getExtension(PKIXExtensions.AuthInfoAccess_Id);
    }

    private void parse(DerValue val) throws CertificateException, IOException {
        parse(val, null);
    }

    private void parse(DerValue val, byte[] originalEncodedForm) throws CertificateException, IOException {
        if (this.readOnly) {
            throw new CertificateParsingException("cannot over-write existing certificate");
        } else if (val.data == null || val.tag != DerValue.tag_SequenceOf) {
            throw new CertificateParsingException("invalid DER-encoded certificate data");
        } else {
            if (originalEncodedForm == null) {
                originalEncodedForm = val.toByteArray();
            }
            this.signedCert = originalEncodedForm;
            DerValue[] seq = new DerValue[]{val.data.getDerValue(), val.data.getDerValue(), val.data.getDerValue()};
            if (val.data.available() != 0) {
                throw new CertificateParsingException("signed overrun, bytes = " + val.data.available());
            } else if (seq[0].tag != DerValue.tag_SequenceOf) {
                throw new CertificateParsingException("signed fields invalid");
            } else {
                this.algId = AlgorithmId.parse(seq[1]);
                this.signature = seq[2].getBitString();
                if (seq[1].data.available() != 0) {
                    throw new CertificateParsingException("algid field overrun");
                } else if (seq[2].data.available() != 0) {
                    throw new CertificateParsingException("signed fields overrun");
                } else {
                    this.info = new X509CertInfo(seq[0]);
                    if (this.algId.equals((AlgorithmId) this.info.get("algorithmID.algorithm"))) {
                        this.readOnly = true;
                        return;
                    }
                    throw new CertificateException("Signature algorithm mismatch");
                }
            }
        }
    }

    private static X500Principal getX500Principal(X509Certificate cert, boolean getIssuer) throws Exception {
        DerValue tmp;
        DerInputStream tbsIn = new DerInputStream(cert.getEncoded()).getSequence(3)[0].data;
        if (tbsIn.getDerValue().isContextSpecific((byte) 0)) {
            tmp = tbsIn.getDerValue();
        }
        tmp = tbsIn.getDerValue();
        tmp = tbsIn.getDerValue();
        if (!getIssuer) {
            tmp = tbsIn.getDerValue();
            tmp = tbsIn.getDerValue();
        }
        return new X500Principal(tmp.toByteArray());
    }

    public static X500Principal getSubjectX500Principal(X509Certificate cert) {
        try {
            return getX500Principal(cert, false);
        } catch (Exception e) {
            throw new RuntimeException("Could not parse subject", e);
        }
    }

    public static X500Principal getIssuerX500Principal(X509Certificate cert) {
        try {
            return getX500Principal(cert, true);
        } catch (Exception e) {
            throw new RuntimeException("Could not parse issuer", e);
        }
    }

    public static byte[] getEncodedInternal(Certificate cert) throws CertificateEncodingException {
        if (cert instanceof X509CertImpl) {
            return ((X509CertImpl) cert).getEncodedInternal();
        }
        return cert.getEncoded();
    }

    public static X509CertImpl toImpl(X509Certificate cert) throws CertificateException {
        if (cert instanceof X509CertImpl) {
            return (X509CertImpl) cert;
        }
        return X509Factory.intern(cert);
    }

    public static boolean isSelfIssued(X509Certificate cert) {
        return cert.getSubjectX500Principal().equals(cert.getIssuerX500Principal());
    }

    public static boolean isSelfSigned(X509Certificate cert, String sigProvider) {
        if (isSelfIssued(cert)) {
            if (sigProvider == null) {
                try {
                    cert.verify(cert.getPublicKey());
                } catch (Exception e) {
                }
            } else {
                cert.verify(cert.getPublicKey(), sigProvider);
            }
            return true;
        }
        return false;
    }

    public String getFingerprint(String algorithm) {
        if (!this.fingerprints.containsKey(algorithm)) {
            this.fingerprints.put(algorithm, getCertificateFingerPrint(algorithm));
        }
        return (String) this.fingerprints.get(algorithm);
    }

    private String getCertificateFingerPrint(String mdAlg) {
        String fingerPrint = "";
        try {
            byte[] digest = MessageDigest.getInstance(mdAlg).digest(getEncoded());
            StringBuffer buf = new StringBuffer();
            for (byte byte2hex : digest) {
                byte2hex(byte2hex, buf);
            }
            fingerPrint = buf.toString();
        } catch (NoSuchAlgorithmException e) {
        }
        return fingerPrint;
    }

    private static void byte2hex(byte b, StringBuffer buf) {
        char[] hexChars = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        int low = b & 15;
        buf.append(hexChars[(b & 240) >> 4]);
        buf.append(hexChars[low]);
    }
}
