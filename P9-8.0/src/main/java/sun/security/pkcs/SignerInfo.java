package sun.security.pkcs;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.CryptoPrimitive;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.Timestamp;
import java.security.cert.CertPath;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import sun.misc.HexDumpEncoder;
import sun.security.timestamp.TimestampToken;
import sun.security.util.Debug;
import sun.security.util.DerEncoder;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.DisabledAlgorithmConstraints;
import sun.security.util.ObjectIdentifier;
import sun.security.x509.AlgorithmId;
import sun.security.x509.KeyUsageExtension;
import sun.security.x509.X500Name;

public class SignerInfo implements DerEncoder {
    private static final Set<CryptoPrimitive> DIGEST_PRIMITIVE_SET = Collections.unmodifiableSet(EnumSet.of(CryptoPrimitive.MESSAGE_DIGEST));
    private static final DisabledAlgorithmConstraints JAR_DISABLED_CHECK = new DisabledAlgorithmConstraints(DisabledAlgorithmConstraints.PROPERTY_JAR_DISABLED_ALGS);
    private static final Set<CryptoPrimitive> SIG_PRIMITIVE_SET = Collections.unmodifiableSet(EnumSet.of(CryptoPrimitive.SIGNATURE));
    PKCS9Attributes authenticatedAttributes;
    BigInteger certificateSerialNumber;
    AlgorithmId digestAlgorithmId;
    AlgorithmId digestEncryptionAlgorithmId;
    byte[] encryptedDigest;
    private boolean hasTimestamp;
    X500Name issuerName;
    Timestamp timestamp;
    PKCS9Attributes unauthenticatedAttributes;
    BigInteger version;

    public SignerInfo(X500Name issuerName, BigInteger serial, AlgorithmId digestAlgorithmId, AlgorithmId digestEncryptionAlgorithmId, byte[] encryptedDigest) {
        this.hasTimestamp = true;
        this.version = BigInteger.ONE;
        this.issuerName = issuerName;
        this.certificateSerialNumber = serial;
        this.digestAlgorithmId = digestAlgorithmId;
        this.digestEncryptionAlgorithmId = digestEncryptionAlgorithmId;
        this.encryptedDigest = encryptedDigest;
    }

    public SignerInfo(X500Name issuerName, BigInteger serial, AlgorithmId digestAlgorithmId, PKCS9Attributes authenticatedAttributes, AlgorithmId digestEncryptionAlgorithmId, byte[] encryptedDigest, PKCS9Attributes unauthenticatedAttributes) {
        this.hasTimestamp = true;
        this.version = BigInteger.ONE;
        this.issuerName = issuerName;
        this.certificateSerialNumber = serial;
        this.digestAlgorithmId = digestAlgorithmId;
        this.authenticatedAttributes = authenticatedAttributes;
        this.digestEncryptionAlgorithmId = digestEncryptionAlgorithmId;
        this.encryptedDigest = encryptedDigest;
        this.unauthenticatedAttributes = unauthenticatedAttributes;
    }

    public SignerInfo(DerInputStream derin) throws IOException, ParsingException {
        this(derin, false);
    }

    public SignerInfo(DerInputStream derin, boolean oldStyle) throws IOException, ParsingException {
        this.hasTimestamp = true;
        this.version = derin.getBigInteger();
        DerValue[] issuerAndSerialNumber = derin.getSequence(2);
        this.issuerName = new X500Name(new DerValue((byte) 48, issuerAndSerialNumber[0].toByteArray()));
        this.certificateSerialNumber = issuerAndSerialNumber[1].getBigInteger();
        this.digestAlgorithmId = AlgorithmId.parse(derin.getDerValue());
        if (oldStyle) {
            derin.getSet(0);
        } else if (((byte) derin.peekByte()) == (byte) -96) {
            this.authenticatedAttributes = new PKCS9Attributes(derin);
        }
        this.digestEncryptionAlgorithmId = AlgorithmId.parse(derin.getDerValue());
        this.encryptedDigest = derin.getOctetString();
        if (oldStyle) {
            derin.getSet(0);
        } else if (derin.available() != 0 && ((byte) derin.peekByte()) == (byte) -95) {
            this.unauthenticatedAttributes = new PKCS9Attributes(derin, true);
        }
        if (derin.available() != 0) {
            throw new ParsingException("extra data at the end");
        }
    }

    public void encode(DerOutputStream out) throws IOException {
        derEncode(out);
    }

    public void derEncode(OutputStream out) throws IOException {
        DerOutputStream seq = new DerOutputStream();
        seq.putInteger(this.version);
        DerOutputStream issuerAndSerialNumber = new DerOutputStream();
        this.issuerName.encode(issuerAndSerialNumber);
        issuerAndSerialNumber.putInteger(this.certificateSerialNumber);
        seq.write((byte) 48, issuerAndSerialNumber);
        this.digestAlgorithmId.encode(seq);
        if (this.authenticatedAttributes != null) {
            this.authenticatedAttributes.encode((byte) -96, seq);
        }
        this.digestEncryptionAlgorithmId.encode(seq);
        seq.putOctetString(this.encryptedDigest);
        if (this.unauthenticatedAttributes != null) {
            this.unauthenticatedAttributes.encode((byte) -95, seq);
        }
        DerOutputStream tmp = new DerOutputStream();
        tmp.write((byte) 48, seq);
        out.write(tmp.toByteArray());
    }

    public X509Certificate getCertificate(PKCS7 block) throws IOException {
        return block.getCertificate(this.certificateSerialNumber, this.issuerName);
    }

    public ArrayList<X509Certificate> getCertificateChain(PKCS7 block) throws IOException {
        X509Certificate userCert = block.getCertificate(this.certificateSerialNumber, this.issuerName);
        if (userCert == null) {
            return null;
        }
        ArrayList<X509Certificate> certList = new ArrayList();
        certList.add(userCert);
        X509Certificate[] pkcsCerts = block.getCertificates();
        if (pkcsCerts == null || userCert.getSubjectDN().equals(userCert.getIssuerDN())) {
            return certList;
        }
        Principal issuer = userCert.getIssuerDN();
        int start = 0;
        boolean match;
        do {
            match = false;
            int i = start;
            while (i < pkcsCerts.length) {
                if (issuer.equals(pkcsCerts[i].getSubjectDN())) {
                    certList.add(pkcsCerts[i]);
                    if (pkcsCerts[i].getSubjectDN().equals(pkcsCerts[i].getIssuerDN())) {
                        start = pkcsCerts.length;
                    } else {
                        issuer = pkcsCerts[i].getIssuerDN();
                        X509Certificate tmpCert = pkcsCerts[start];
                        pkcsCerts[start] = pkcsCerts[i];
                        pkcsCerts[i] = tmpCert;
                        start++;
                    }
                    match = true;
                    continue;
                } else {
                    i++;
                }
            }
        } while (match);
        return certList;
    }

    SignerInfo verify(PKCS7 block, byte[] data) throws NoSuchAlgorithmException, SignatureException {
        try {
            return verify(block, new ByteArrayInputStream(data));
        } catch (IOException e) {
            return null;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:32:0x00ea A:{Splitter: B:0:0x0000, ExcHandler: java.security.InvalidKeyException (r13_0 'e' java.security.InvalidKeyException)} */
    /* JADX WARNING: Missing block: B:32:0x00ea, code:
            r13 = move-exception;
     */
    /* JADX WARNING: Missing block: B:34:0x0108, code:
            throw new java.security.SignatureException("InvalidKey: " + r13.getMessage());
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    SignerInfo verify(PKCS7 block, InputStream inputStream) throws NoSuchAlgorithmException, SignatureException, IOException {
        try {
            InputStream dataSigned;
            byte[] buffer;
            int read;
            ContentInfo content = block.getContentInfo();
            if (inputStream == null) {
                inputStream = new ByteArrayInputStream(content.getContentBytes());
            }
            String digestAlgname = getDigestAlgorithmId().getName();
            if (this.authenticatedAttributes == null) {
                dataSigned = inputStream;
            } else {
                ObjectIdentifier contentType = (ObjectIdentifier) this.authenticatedAttributes.getAttributeValue(PKCS9Attribute.CONTENT_TYPE_OID);
                if (contentType == null || (contentType.equals((Object) content.contentType) ^ 1) != 0) {
                    return null;
                }
                byte[] messageDigest = (byte[]) this.authenticatedAttributes.getAttributeValue(PKCS9Attribute.MESSAGE_DIGEST_OID);
                if (messageDigest == null) {
                    return null;
                }
                if (JAR_DISABLED_CHECK.permits(DIGEST_PRIMITIVE_SET, digestAlgname, null)) {
                    MessageDigest md = MessageDigest.getInstance(digestAlgname);
                    buffer = new byte[4096];
                    while (true) {
                        read = inputStream.read(buffer);
                        if (read == -1) {
                            break;
                        }
                        md.update(buffer, 0, read);
                    }
                    byte[] computedMessageDigest = md.digest();
                    if (messageDigest.length != computedMessageDigest.length) {
                        return null;
                    }
                    for (int i = 0; i < messageDigest.length; i++) {
                        if (messageDigest[i] != computedMessageDigest[i]) {
                            return null;
                        }
                    }
                    dataSigned = new ByteArrayInputStream(this.authenticatedAttributes.getDerEncoding());
                } else {
                    throw new SignatureException("Digest check failed. Disabled algorithm used: " + digestAlgname);
                }
            }
            String encryptionAlgname = getDigestEncryptionAlgorithmId().getName();
            String tmp = AlgorithmId.getEncAlgFromSigAlg(encryptionAlgname);
            if (tmp != null) {
                encryptionAlgname = tmp;
            }
            String algname = AlgorithmId.makeSigAlg(digestAlgname, encryptionAlgname);
            if (JAR_DISABLED_CHECK.permits(SIG_PRIMITIVE_SET, algname, null)) {
                X509Certificate cert = getCertificate(block);
                PublicKey key = cert.getPublicKey();
                if (cert == null) {
                    return null;
                }
                if (!JAR_DISABLED_CHECK.permits(SIG_PRIMITIVE_SET, (Key) key)) {
                    throw new SignatureException("Public key check failed. Disabled algorithm used: " + key.getAlgorithm());
                } else if (cert.hasUnsupportedCriticalExtension()) {
                    throw new SignatureException("Certificate has unsupported critical extension(s)");
                } else {
                    boolean[] keyUsageBits = cert.getKeyUsage();
                    if (keyUsageBits != null) {
                        KeyUsageExtension keyUsage = new KeyUsageExtension(keyUsageBits);
                        boolean digSigAllowed = keyUsage.get(KeyUsageExtension.DIGITAL_SIGNATURE).booleanValue();
                        boolean nonRepuAllowed = keyUsage.get(KeyUsageExtension.NON_REPUDIATION).booleanValue();
                        if (!(digSigAllowed || (nonRepuAllowed ^ 1) == 0)) {
                            throw new SignatureException("Key usage restricted: cannot be used for digital signatures");
                        }
                    }
                    Signature sig = Signature.getInstance(algname);
                    sig.initVerify(key);
                    buffer = new byte[4096];
                    while (true) {
                        read = dataSigned.read(buffer);
                        if (read == -1) {
                            break;
                        }
                        sig.update(buffer, 0, read);
                    }
                    if (sig.verify(this.encryptedDigest)) {
                        return this;
                    }
                    return null;
                }
            }
            throw new SignatureException("Signature check failed. Disabled algorithm used: " + algname);
        } catch (IOException e) {
            throw new SignatureException("Failed to parse keyUsage extension");
        } catch (InvalidKeyException e2) {
        } catch (IOException e3) {
            throw new SignatureException("IO error verifying signature:\n" + e3.getMessage());
        }
    }

    SignerInfo verify(PKCS7 block) throws NoSuchAlgorithmException, SignatureException {
        return verify(block, (byte[]) null);
    }

    public BigInteger getVersion() {
        return this.version;
    }

    public X500Name getIssuerName() {
        return this.issuerName;
    }

    public BigInteger getCertificateSerialNumber() {
        return this.certificateSerialNumber;
    }

    public AlgorithmId getDigestAlgorithmId() {
        return this.digestAlgorithmId;
    }

    public PKCS9Attributes getAuthenticatedAttributes() {
        return this.authenticatedAttributes;
    }

    public AlgorithmId getDigestEncryptionAlgorithmId() {
        return this.digestEncryptionAlgorithmId;
    }

    public byte[] getEncryptedDigest() {
        return this.encryptedDigest;
    }

    public PKCS9Attributes getUnauthenticatedAttributes() {
        return this.unauthenticatedAttributes;
    }

    public Timestamp getTimestamp() throws IOException, NoSuchAlgorithmException, SignatureException, CertificateException {
        if (this.timestamp != null || (this.hasTimestamp ^ 1) != 0) {
            return this.timestamp;
        }
        if (this.unauthenticatedAttributes == null) {
            this.hasTimestamp = false;
            return null;
        }
        PKCS9Attribute tsTokenAttr = this.unauthenticatedAttributes.getAttribute(PKCS9Attribute.SIGNATURE_TIMESTAMP_TOKEN_OID);
        if (tsTokenAttr == null) {
            this.hasTimestamp = false;
            return null;
        }
        PKCS7 tsToken = new PKCS7((byte[]) tsTokenAttr.getValue());
        byte[] encTsTokenInfo = tsToken.getContentInfo().getData();
        CertPath tsaChain = CertificateFactory.getInstance("X.509").generateCertPath(tsToken.verify(encTsTokenInfo)[0].getCertificateChain(tsToken));
        TimestampToken tsTokenInfo = new TimestampToken(encTsTokenInfo);
        verifyTimestamp(tsTokenInfo);
        this.timestamp = new Timestamp(tsTokenInfo.getDate(), tsaChain);
        return this.timestamp;
    }

    private void verifyTimestamp(TimestampToken token) throws NoSuchAlgorithmException, SignatureException {
        String digestAlgname = token.getHashAlgorithm().getName();
        if (JAR_DISABLED_CHECK.permits(DIGEST_PRIMITIVE_SET, digestAlgname, null)) {
            if (!Arrays.equals(token.getHashedMessage(), MessageDigest.getInstance(digestAlgname).digest(this.encryptedDigest))) {
                throw new SignatureException("Signature timestamp (#" + token.getSerialNumber() + ") generated on " + token.getDate() + " is inapplicable");
            }
            return;
        }
        throw new SignatureException("Timestamp token digest check failed. Disabled algorithm used: " + digestAlgname);
    }

    public String toString() {
        HexDumpEncoder hexDump = new HexDumpEncoder();
        String out = ((("" + "Signer Info for (issuer): " + this.issuerName + "\n") + "\tversion: " + Debug.toHexString(this.version) + "\n") + "\tcertificateSerialNumber: " + Debug.toHexString(this.certificateSerialNumber) + "\n") + "\tdigestAlgorithmId: " + this.digestAlgorithmId + "\n";
        if (this.authenticatedAttributes != null) {
            out = out + "\tauthenticatedAttributes: " + this.authenticatedAttributes + "\n";
        }
        out = (out + "\tdigestEncryptionAlgorithmId: " + this.digestEncryptionAlgorithmId + "\n") + "\tencryptedDigest: \n" + hexDump.encodeBuffer(this.encryptedDigest) + "\n";
        if (this.unauthenticatedAttributes != null) {
            return out + "\tunauthenticatedAttributes: " + this.unauthenticatedAttributes + "\n";
        }
        return out;
    }
}
