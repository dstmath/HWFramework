package sun.security.pkcs;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CRLException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import javax.security.auth.x500.X500Principal;
import sun.security.util.Debug;
import sun.security.util.DerEncoder;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;
import sun.security.x509.AlgorithmId;
import sun.security.x509.X500Name;
import sun.security.x509.X509CRLImpl;
import sun.security.x509.X509CertImpl;
import sun.security.x509.X509CertInfo;

public class PKCS7 {
    private Principal[] certIssuerNames;
    private X509Certificate[] certificates;
    private ContentInfo contentInfo;
    private ObjectIdentifier contentType;
    private X509CRL[] crls;
    private AlgorithmId[] digestAlgorithmIds;
    private boolean oldStyle;
    private SignerInfo[] signerInfos;
    private BigInteger version;

    private static class VerbatimX509Certificate extends WrappedX509Certificate {
        private byte[] encodedVerbatim;

        public VerbatimX509Certificate(X509Certificate wrapped, byte[] encodedVerbatim2) {
            super(wrapped);
            this.encodedVerbatim = encodedVerbatim2;
        }

        public byte[] getEncoded() throws CertificateEncodingException {
            return this.encodedVerbatim;
        }
    }

    private static class WrappedX509Certificate extends X509Certificate {
        private final X509Certificate wrapped;

        public WrappedX509Certificate(X509Certificate wrapped2) {
            this.wrapped = wrapped2;
        }

        public Set<String> getCriticalExtensionOIDs() {
            return this.wrapped.getCriticalExtensionOIDs();
        }

        public byte[] getExtensionValue(String oid) {
            return this.wrapped.getExtensionValue(oid);
        }

        public Set<String> getNonCriticalExtensionOIDs() {
            return this.wrapped.getNonCriticalExtensionOIDs();
        }

        public boolean hasUnsupportedCriticalExtension() {
            return this.wrapped.hasUnsupportedCriticalExtension();
        }

        public void checkValidity() throws CertificateExpiredException, CertificateNotYetValidException {
            this.wrapped.checkValidity();
        }

        public void checkValidity(Date date) throws CertificateExpiredException, CertificateNotYetValidException {
            this.wrapped.checkValidity(date);
        }

        public int getVersion() {
            return this.wrapped.getVersion();
        }

        public BigInteger getSerialNumber() {
            return this.wrapped.getSerialNumber();
        }

        public Principal getIssuerDN() {
            return this.wrapped.getIssuerDN();
        }

        public Principal getSubjectDN() {
            return this.wrapped.getSubjectDN();
        }

        public Date getNotBefore() {
            return this.wrapped.getNotBefore();
        }

        public Date getNotAfter() {
            return this.wrapped.getNotAfter();
        }

        public byte[] getTBSCertificate() throws CertificateEncodingException {
            return this.wrapped.getTBSCertificate();
        }

        public byte[] getSignature() {
            return this.wrapped.getSignature();
        }

        public String getSigAlgName() {
            return this.wrapped.getSigAlgName();
        }

        public String getSigAlgOID() {
            return this.wrapped.getSigAlgOID();
        }

        public byte[] getSigAlgParams() {
            return this.wrapped.getSigAlgParams();
        }

        public boolean[] getIssuerUniqueID() {
            return this.wrapped.getIssuerUniqueID();
        }

        public boolean[] getSubjectUniqueID() {
            return this.wrapped.getSubjectUniqueID();
        }

        public boolean[] getKeyUsage() {
            return this.wrapped.getKeyUsage();
        }

        public int getBasicConstraints() {
            return this.wrapped.getBasicConstraints();
        }

        public byte[] getEncoded() throws CertificateEncodingException {
            return this.wrapped.getEncoded();
        }

        public void verify(PublicKey key) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {
            this.wrapped.verify(key);
        }

        public void verify(PublicKey key, String sigProvider) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {
            this.wrapped.verify(key, sigProvider);
        }

        public String toString() {
            return this.wrapped.toString();
        }

        public PublicKey getPublicKey() {
            return this.wrapped.getPublicKey();
        }

        public List<String> getExtendedKeyUsage() throws CertificateParsingException {
            return this.wrapped.getExtendedKeyUsage();
        }

        public Collection<List<?>> getIssuerAlternativeNames() throws CertificateParsingException {
            return this.wrapped.getIssuerAlternativeNames();
        }

        public X500Principal getIssuerX500Principal() {
            return this.wrapped.getIssuerX500Principal();
        }

        public Collection<List<?>> getSubjectAlternativeNames() throws CertificateParsingException {
            return this.wrapped.getSubjectAlternativeNames();
        }

        public X500Principal getSubjectX500Principal() {
            return this.wrapped.getSubjectX500Principal();
        }

        public void verify(PublicKey key, Provider sigProvider) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
            this.wrapped.verify(key, sigProvider);
        }
    }

    public PKCS7(InputStream in) throws ParsingException, IOException {
        this.version = null;
        this.digestAlgorithmIds = null;
        this.contentInfo = null;
        this.certificates = null;
        this.crls = null;
        this.signerInfos = null;
        this.oldStyle = false;
        DataInputStream dis = new DataInputStream(in);
        byte[] data = new byte[dis.available()];
        dis.readFully(data);
        parse(new DerInputStream(data));
    }

    public PKCS7(DerInputStream derin) throws ParsingException {
        this.version = null;
        this.digestAlgorithmIds = null;
        this.contentInfo = null;
        this.certificates = null;
        this.crls = null;
        this.signerInfos = null;
        this.oldStyle = false;
        parse(derin);
    }

    public PKCS7(byte[] bytes) throws ParsingException {
        this.version = null;
        this.digestAlgorithmIds = null;
        this.contentInfo = null;
        this.certificates = null;
        this.crls = null;
        this.signerInfos = null;
        this.oldStyle = false;
        try {
            parse(new DerInputStream(bytes));
        } catch (IOException ioe1) {
            ParsingException pe = new ParsingException("Unable to parse the encoded bytes");
            pe.initCause(ioe1);
            throw pe;
        }
    }

    private void parse(DerInputStream derin) throws ParsingException {
        try {
            derin.mark(derin.available());
            parse(derin, false);
        } catch (IOException ioe) {
            try {
                derin.reset();
                parse(derin, true);
                this.oldStyle = true;
            } catch (IOException ioe1) {
                ParsingException pe = new ParsingException(ioe1.getMessage());
                pe.initCause(ioe);
                pe.addSuppressed(ioe1);
                throw pe;
            }
        }
    }

    private void parse(DerInputStream derin, boolean oldStyle2) throws IOException {
        this.contentInfo = new ContentInfo(derin, oldStyle2);
        this.contentType = this.contentInfo.contentType;
        DerValue content = this.contentInfo.getContent();
        if (this.contentType.equals((Object) ContentInfo.SIGNED_DATA_OID)) {
            parseSignedData(content);
        } else if (this.contentType.equals((Object) ContentInfo.OLD_SIGNED_DATA_OID)) {
            parseOldSignedData(content);
        } else if (this.contentType.equals((Object) ContentInfo.NETSCAPE_CERT_SEQUENCE_OID)) {
            parseNetscapeCertChain(content);
        } else {
            throw new ParsingException("content type " + this.contentType + " not supported.");
        }
    }

    public PKCS7(AlgorithmId[] digestAlgorithmIds2, ContentInfo contentInfo2, X509Certificate[] certificates2, X509CRL[] crls2, SignerInfo[] signerInfos2) {
        this.version = null;
        this.digestAlgorithmIds = null;
        this.contentInfo = null;
        this.certificates = null;
        this.crls = null;
        this.signerInfos = null;
        this.oldStyle = false;
        this.version = BigInteger.ONE;
        this.digestAlgorithmIds = digestAlgorithmIds2;
        this.contentInfo = contentInfo2;
        this.certificates = certificates2;
        this.crls = crls2;
        this.signerInfos = signerInfos2;
    }

    public PKCS7(AlgorithmId[] digestAlgorithmIds2, ContentInfo contentInfo2, X509Certificate[] certificates2, SignerInfo[] signerInfos2) {
        this(digestAlgorithmIds2, contentInfo2, certificates2, null, signerInfos2);
    }

    private void parseNetscapeCertChain(DerValue val) throws ParsingException, IOException {
        DerValue[] contents = new DerInputStream(val.toByteArray()).getSequence(2, true);
        this.certificates = new X509Certificate[contents.length];
        CertificateFactory certfac = null;
        try {
            certfac = CertificateFactory.getInstance("X.509");
        } catch (CertificateException e) {
        }
        int i = 0;
        while (i < contents.length) {
            ByteArrayInputStream bais = null;
            try {
                byte[] original = contents[i].getOriginalEncodedForm();
                if (certfac == null) {
                    this.certificates[i] = new X509CertImpl(contents[i], original);
                } else {
                    ByteArrayInputStream bais2 = new ByteArrayInputStream(original);
                    this.certificates[i] = new VerbatimX509Certificate((X509Certificate) certfac.generateCertificate(bais2), original);
                    bais2.close();
                    bais = null;
                }
                if (bais != null) {
                    bais.close();
                }
                i++;
            } catch (CertificateException ce) {
                ParsingException pe = new ParsingException(ce.getMessage());
                pe.initCause(ce);
                throw pe;
            } catch (IOException ioe) {
                ParsingException pe2 = new ParsingException(ioe.getMessage());
                pe2.initCause(ioe);
                throw pe2;
            } catch (Throwable th) {
                if (bais != null) {
                    bais.close();
                }
                throw th;
            }
        }
    }

    private void parseSignedData(DerValue val) throws ParsingException, IOException {
        DerInputStream dis = val.toDerInputStream();
        this.version = dis.getBigInteger();
        DerValue[] digestAlgorithmIdVals = dis.getSet(1);
        int len = digestAlgorithmIdVals.length;
        this.digestAlgorithmIds = new AlgorithmId[len];
        int i = 0;
        while (i < len) {
            try {
                this.digestAlgorithmIds[i] = AlgorithmId.parse(digestAlgorithmIdVals[i]);
                i++;
            } catch (IOException e) {
                ParsingException pe = new ParsingException("Error parsing digest AlgorithmId IDs: " + e.getMessage());
                pe.initCause(e);
                throw pe;
            }
        }
        this.contentInfo = new ContentInfo(dis);
        CertificateFactory certfac = null;
        try {
            certfac = CertificateFactory.getInstance("X.509");
        } catch (CertificateException e2) {
        }
        if (((byte) dis.peekByte()) == -96) {
            DerValue[] certVals = dis.getSet(2, true, true);
            int len2 = certVals.length;
            this.certificates = new X509Certificate[len2];
            int count = 0;
            int count2 = 0;
            while (true) {
                int i2 = count2;
                if (i2 >= len2) {
                    break;
                }
                ByteArrayInputStream bais = null;
                try {
                    if (certVals[i2].getTag() == 48) {
                        byte[] original = certVals[i2].getOriginalEncodedForm();
                        if (certfac == null) {
                            this.certificates[count] = new X509CertImpl(certVals[i2], original);
                        } else {
                            ByteArrayInputStream bais2 = new ByteArrayInputStream(original);
                            this.certificates[count] = new VerbatimX509Certificate((X509Certificate) certfac.generateCertificate(bais2), original);
                            bais2.close();
                            bais = null;
                        }
                        count++;
                    }
                    if (bais != null) {
                        bais.close();
                    }
                    count2 = i2 + 1;
                } catch (CertificateException ce) {
                    ParsingException pe2 = new ParsingException(ce.getMessage());
                    pe2.initCause(ce);
                    throw pe2;
                } catch (IOException ioe) {
                    ParsingException pe3 = new ParsingException(ioe.getMessage());
                    pe3.initCause(ioe);
                    throw pe3;
                } catch (Throwable th) {
                    if (bais != null) {
                        bais.close();
                    }
                    throw th;
                }
            }
            if (count != len2) {
                this.certificates = (X509Certificate[]) Arrays.copyOf((T[]) this.certificates, count);
            }
        }
        if (((byte) dis.peekByte()) == -95) {
            DerValue[] crlVals = dis.getSet(1, true);
            int len3 = crlVals.length;
            this.crls = new X509CRL[len3];
            int i3 = 0;
            while (true) {
                int i4 = i3;
                if (i4 >= len3) {
                    break;
                }
                ByteArrayInputStream bais3 = null;
                if (certfac == null) {
                    try {
                        this.crls[i4] = new X509CRLImpl(crlVals[i4]);
                    } catch (CRLException e3) {
                        ParsingException pe4 = new ParsingException(e3.getMessage());
                        pe4.initCause(e3);
                        throw pe4;
                    } catch (Throwable th2) {
                        if (bais3 != null) {
                            bais3.close();
                        }
                        throw th2;
                    }
                } else {
                    ByteArrayInputStream bais4 = new ByteArrayInputStream(crlVals[i4].toByteArray());
                    this.crls[i4] = (X509CRL) certfac.generateCRL(bais4);
                    bais4.close();
                    bais3 = null;
                }
                if (bais3 != null) {
                    bais3.close();
                }
                i3 = i4 + 1;
            }
        }
        DerValue[] signerInfoVals = dis.getSet(1);
        int len4 = signerInfoVals.length;
        this.signerInfos = new SignerInfo[len4];
        int i5 = 0;
        while (true) {
            int i6 = i5;
            if (i6 < len4) {
                this.signerInfos[i6] = new SignerInfo(signerInfoVals[i6].toDerInputStream());
                i5 = i6 + 1;
            } else {
                return;
            }
        }
    }

    private void parseOldSignedData(DerValue val) throws ParsingException, IOException {
        DerInputStream dis = val.toDerInputStream();
        this.version = dis.getBigInteger();
        DerValue[] digestAlgorithmIdVals = dis.getSet(1);
        int len = digestAlgorithmIdVals.length;
        this.digestAlgorithmIds = new AlgorithmId[len];
        int i = 0;
        while (i < len) {
            try {
                this.digestAlgorithmIds[i] = AlgorithmId.parse(digestAlgorithmIdVals[i]);
                i++;
            } catch (IOException e) {
                throw new ParsingException("Error parsing digest AlgorithmId IDs");
            }
        }
        this.contentInfo = new ContentInfo(dis, true);
        CertificateFactory certfac = null;
        try {
            certfac = CertificateFactory.getInstance("X.509");
        } catch (CertificateException e2) {
        }
        DerValue[] certVals = dis.getSet(2, false, true);
        int len2 = certVals.length;
        this.certificates = new X509Certificate[len2];
        int i2 = 0;
        while (i2 < len2) {
            ByteArrayInputStream bais = null;
            try {
                byte[] original = certVals[i2].getOriginalEncodedForm();
                if (certfac == null) {
                    this.certificates[i2] = new X509CertImpl(certVals[i2], original);
                } else {
                    ByteArrayInputStream bais2 = new ByteArrayInputStream(original);
                    this.certificates[i2] = new VerbatimX509Certificate((X509Certificate) certfac.generateCertificate(bais2), original);
                    bais2.close();
                    bais = null;
                }
                if (bais != null) {
                    bais.close();
                }
                i2++;
            } catch (CertificateException ce) {
                ParsingException pe = new ParsingException(ce.getMessage());
                pe.initCause(ce);
                throw pe;
            } catch (IOException ioe) {
                ParsingException pe2 = new ParsingException(ioe.getMessage());
                pe2.initCause(ioe);
                throw pe2;
            } catch (Throwable th) {
                if (bais != null) {
                    bais.close();
                }
                throw th;
            }
        }
        dis.getSet(0);
        DerValue[] signerInfoVals = dis.getSet(1);
        int len3 = signerInfoVals.length;
        this.signerInfos = new SignerInfo[len3];
        for (int i3 = 0; i3 < len3; i3++) {
            this.signerInfos[i3] = new SignerInfo(signerInfoVals[i3].toDerInputStream(), true);
        }
    }

    public void encodeSignedData(OutputStream out) throws IOException {
        DerOutputStream derout = new DerOutputStream();
        encodeSignedData(derout);
        out.write(derout.toByteArray());
    }

    public void encodeSignedData(DerOutputStream out) throws IOException {
        DerOutputStream signedData = new DerOutputStream();
        signedData.putInteger(this.version);
        signedData.putOrderedSetOf((byte) 49, this.digestAlgorithmIds);
        this.contentInfo.encode(signedData);
        if (!(this.certificates == null || this.certificates.length == 0)) {
            X509CertImpl[] implCerts = new X509CertImpl[this.certificates.length];
            for (int i = 0; i < this.certificates.length; i++) {
                if (this.certificates[i] instanceof X509CertImpl) {
                    implCerts[i] = (X509CertImpl) this.certificates[i];
                } else {
                    try {
                        implCerts[i] = new X509CertImpl(this.certificates[i].getEncoded());
                    } catch (CertificateException ce) {
                        throw new IOException((Throwable) ce);
                    }
                }
            }
            signedData.putOrderedSetOf((byte) -96, implCerts);
        }
        if (!(this.crls == null || this.crls.length == 0)) {
            Set<X509CRLImpl> implCRLs = new HashSet<>(this.crls.length);
            for (X509CRL crl : this.crls) {
                if (crl instanceof X509CRLImpl) {
                    implCRLs.add((X509CRLImpl) crl);
                } else {
                    try {
                        implCRLs.add(new X509CRLImpl(crl.getEncoded()));
                    } catch (CRLException ce2) {
                        throw new IOException((Throwable) ce2);
                    }
                }
            }
            signedData.putOrderedSetOf((byte) -95, (DerEncoder[]) implCRLs.toArray(new X509CRLImpl[implCRLs.size()]));
        }
        signedData.putOrderedSetOf((byte) 49, this.signerInfos);
        new ContentInfo(ContentInfo.SIGNED_DATA_OID, new DerValue((byte) 48, signedData.toByteArray())).encode(out);
    }

    public SignerInfo verify(SignerInfo info, byte[] bytes) throws NoSuchAlgorithmException, SignatureException {
        return info.verify(this, bytes);
    }

    public SignerInfo verify(SignerInfo info, InputStream dataInputStream) throws NoSuchAlgorithmException, SignatureException, IOException {
        return info.verify(this, dataInputStream);
    }

    public SignerInfo[] verify(byte[] bytes) throws NoSuchAlgorithmException, SignatureException {
        Vector<SignerInfo> intResult = new Vector<>();
        for (SignerInfo verify : this.signerInfos) {
            SignerInfo signerInfo = verify(verify, bytes);
            if (signerInfo != null) {
                intResult.addElement(signerInfo);
            }
        }
        if (intResult.isEmpty() != 0) {
            return null;
        }
        SignerInfo[] result = new SignerInfo[intResult.size()];
        intResult.copyInto(result);
        return result;
    }

    public SignerInfo[] verify() throws NoSuchAlgorithmException, SignatureException {
        return verify(null);
    }

    public BigInteger getVersion() {
        return this.version;
    }

    public AlgorithmId[] getDigestAlgorithmIds() {
        return this.digestAlgorithmIds;
    }

    public ContentInfo getContentInfo() {
        return this.contentInfo;
    }

    public X509Certificate[] getCertificates() {
        if (this.certificates != null) {
            return (X509Certificate[]) this.certificates.clone();
        }
        return null;
    }

    public X509CRL[] getCRLs() {
        if (this.crls != null) {
            return (X509CRL[]) this.crls.clone();
        }
        return null;
    }

    public SignerInfo[] getSignerInfos() {
        return this.signerInfos;
    }

    public X509Certificate getCertificate(BigInteger serial, X500Name issuerName) {
        if (this.certificates != null) {
            if (this.certIssuerNames == null) {
                populateCertIssuerNames();
            }
            for (int i = 0; i < this.certificates.length; i++) {
                X509Certificate cert = this.certificates[i];
                if (serial.equals(cert.getSerialNumber()) && issuerName.equals(this.certIssuerNames[i])) {
                    return cert;
                }
            }
        }
        return null;
    }

    private void populateCertIssuerNames() {
        if (this.certificates != null) {
            this.certIssuerNames = new Principal[this.certificates.length];
            for (int i = 0; i < this.certificates.length; i++) {
                X509Certificate cert = this.certificates[i];
                Principal certIssuerName = cert.getIssuerDN();
                if (!(certIssuerName instanceof X500Name)) {
                    try {
                        certIssuerName = (Principal) new X509CertInfo(cert.getTBSCertificate()).get("issuer.dname");
                    } catch (Exception e) {
                    }
                }
                this.certIssuerNames[i] = certIssuerName;
            }
        }
    }

    public String toString() {
        String out;
        String out2 = "" + this.contentInfo + "\n";
        if (this.version != null) {
            out2 = out2 + "PKCS7 :: version: " + Debug.toHexString(this.version) + "\n";
        }
        int i = 0;
        if (this.digestAlgorithmIds != null) {
            String out3 = out2 + "PKCS7 :: digest AlgorithmIds: \n";
            for (int i2 = 0; i2 < this.digestAlgorithmIds.length; i2++) {
                out3 = out3 + "\t" + this.digestAlgorithmIds[i2] + "\n";
            }
            out2 = out3;
        }
        if (this.certificates != null) {
            String out4 = out2 + "PKCS7 :: certificates: \n";
            for (int i3 = 0; i3 < this.certificates.length; i3++) {
                out4 = out4 + "\t" + i3 + ".   " + this.certificates[i3] + "\n";
            }
            out2 = out4;
        }
        if (this.crls != null) {
            String out5 = out2 + "PKCS7 :: crls: \n";
            for (int i4 = 0; i4 < this.crls.length; i4++) {
                out5 = out5 + "\t" + i4 + ".   " + this.crls[i4] + "\n";
            }
            out2 = out5;
        }
        if (this.signerInfos != null) {
            out = out + "PKCS7 :: signer infos: \n";
            while (true) {
                int i5 = i;
                if (i5 >= this.signerInfos.length) {
                    break;
                }
                out = out + "\t" + i5 + ".  " + this.signerInfos[i5] + "\n";
                i = i5 + 1;
            }
        }
        return out;
    }

    public boolean isOldStyle() {
        return this.oldStyle;
    }
}
