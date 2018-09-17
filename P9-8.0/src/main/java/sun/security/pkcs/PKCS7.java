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

    private static class WrappedX509Certificate extends X509Certificate {
        private final X509Certificate wrapped;

        public WrappedX509Certificate(X509Certificate wrapped) {
            this.wrapped = wrapped;
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

    private static class VerbatimX509Certificate extends WrappedX509Certificate {
        private byte[] encodedVerbatim;

        public VerbatimX509Certificate(X509Certificate wrapped, byte[] encodedVerbatim) {
            super(wrapped);
            this.encodedVerbatim = encodedVerbatim;
        }

        public byte[] getEncoded() throws CertificateEncodingException {
            return this.encodedVerbatim;
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

    private void parse(DerInputStream derin, boolean oldStyle) throws IOException {
        this.contentInfo = new ContentInfo(derin, oldStyle);
        this.contentType = this.contentInfo.contentType;
        DerValue content = this.contentInfo.getContent();
        if (this.contentType.equals(ContentInfo.SIGNED_DATA_OID)) {
            parseSignedData(content);
        } else if (this.contentType.equals(ContentInfo.OLD_SIGNED_DATA_OID)) {
            parseOldSignedData(content);
        } else if (this.contentType.equals(ContentInfo.NETSCAPE_CERT_SEQUENCE_OID)) {
            parseNetscapeCertChain(content);
        } else {
            throw new ParsingException("content type " + this.contentType + " not supported.");
        }
    }

    public PKCS7(AlgorithmId[] digestAlgorithmIds, ContentInfo contentInfo, X509Certificate[] certificates, X509CRL[] crls, SignerInfo[] signerInfos) {
        this.version = null;
        this.digestAlgorithmIds = null;
        this.contentInfo = null;
        this.certificates = null;
        this.crls = null;
        this.signerInfos = null;
        this.oldStyle = false;
        this.version = BigInteger.ONE;
        this.digestAlgorithmIds = digestAlgorithmIds;
        this.contentInfo = contentInfo;
        this.certificates = certificates;
        this.crls = crls;
        this.signerInfos = signerInfos;
    }

    public PKCS7(AlgorithmId[] digestAlgorithmIds, ContentInfo contentInfo, X509Certificate[] certificates, SignerInfo[] signerInfos) {
        this(digestAlgorithmIds, contentInfo, certificates, null, signerInfos);
    }

    private void parseNetscapeCertChain(DerValue val) throws ParsingException, IOException {
        ByteArrayInputStream bais;
        CertificateException ce;
        IOException ioe;
        Throwable th;
        DerValue[] contents = new DerInputStream(val.toByteArray()).getSequence(2, true);
        this.certificates = new X509Certificate[contents.length];
        CertificateFactory certfac = null;
        try {
            certfac = CertificateFactory.getInstance("X.509");
        } catch (CertificateException e) {
        }
        int i = 0;
        while (i < contents.length) {
            bais = null;
            try {
                byte[] original = contents[i].getOriginalEncodedForm();
                if (certfac == null) {
                    this.certificates[i] = new X509CertImpl(contents[i], original);
                } else {
                    ByteArrayInputStream bais2 = new ByteArrayInputStream(original);
                    try {
                        this.certificates[i] = new VerbatimX509Certificate((X509Certificate) certfac.generateCertificate(bais2), original);
                        bais2.close();
                        bais = null;
                    } catch (CertificateException e2) {
                        ce = e2;
                        bais = bais2;
                    } catch (IOException e3) {
                        ioe = e3;
                        bais = bais2;
                    } catch (Throwable th2) {
                        th = th2;
                        bais = bais2;
                    }
                }
                if (null != null) {
                    bais.close();
                }
                i++;
            } catch (CertificateException e4) {
                ce = e4;
            } catch (IOException e5) {
                ioe = e5;
            }
        }
        return;
        ParsingException pe;
        try {
            pe = new ParsingException(ioe.getMessage());
            pe.initCause(ioe);
            throw pe;
        } catch (Throwable th3) {
            th = th3;
            if (bais != null) {
                bais.close();
            }
            throw th;
        }
        pe = new ParsingException(ce.getMessage());
        pe.initCause(ce);
        throw pe;
    }

    private void parseSignedData(DerValue val) throws ParsingException, IOException {
        ParsingException parsingException;
        ByteArrayInputStream bais;
        ByteArrayInputStream bais2;
        CertificateException ce;
        Throwable ioe;
        Throwable th;
        CRLException e;
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
            } catch (IOException e2) {
                parsingException = new ParsingException("Error parsing digest AlgorithmId IDs: " + e2.getMessage());
                parsingException.initCause(e2);
                throw parsingException;
            }
        }
        this.contentInfo = new ContentInfo(dis);
        CertificateFactory certfac = null;
        try {
            certfac = CertificateFactory.getInstance("X.509");
        } catch (CertificateException e3) {
        }
        if (((byte) dis.peekByte()) == (byte) -96) {
            DerValue[] certVals = dis.getSet(2, true, true);
            len = certVals.length;
            this.certificates = new X509Certificate[len];
            int count = 0;
            i = 0;
            while (i < len) {
                bais = null;
                try {
                    if (certVals[i].getTag() == (byte) 48) {
                        byte[] original = certVals[i].getOriginalEncodedForm();
                        if (certfac == null) {
                            this.certificates[count] = new X509CertImpl(certVals[i], original);
                        } else {
                            bais2 = new ByteArrayInputStream(original);
                            try {
                                this.certificates[count] = new VerbatimX509Certificate((X509Certificate) certfac.generateCertificate(bais2), original);
                                bais2.close();
                                bais = null;
                            } catch (CertificateException e4) {
                                ce = e4;
                                bais = bais2;
                            } catch (IOException e5) {
                                ioe = e5;
                                bais = bais2;
                            } catch (Throwable th2) {
                                th = th2;
                                bais = bais2;
                            }
                        }
                        count++;
                    }
                    if (null != null) {
                        bais.close();
                    }
                    i++;
                } catch (CertificateException e6) {
                    ce = e6;
                } catch (IOException e7) {
                    ioe = e7;
                }
            }
            if (count != len) {
                this.certificates = (X509Certificate[]) Arrays.copyOf((Object[]) this.certificates, count);
            }
        }
        if (((byte) dis.peekByte()) == (byte) -95) {
            DerValue[] crlVals = dis.getSet(1, true);
            len = crlVals.length;
            this.crls = new X509CRL[len];
            for (i = 0; i < len; i++) {
                bais = null;
                if (certfac == null) {
                    try {
                        this.crls[i] = new X509CRLImpl(crlVals[i]);
                    } catch (CRLException e8) {
                        e = e8;
                    }
                } else {
                    bais2 = new ByteArrayInputStream(crlVals[i].toByteArray());
                    try {
                        this.crls[i] = (X509CRL) certfac.generateCRL(bais2);
                        bais2.close();
                        bais = null;
                    } catch (CRLException e9) {
                        e = e9;
                        bais = bais2;
                    } catch (Throwable th3) {
                        th = th3;
                        bais = bais2;
                    }
                }
                if (null != null) {
                    bais.close();
                }
            }
        }
        DerValue[] signerInfoVals = dis.getSet(1);
        len = signerInfoVals.length;
        this.signerInfos = new SignerInfo[len];
        for (i = 0; i < len; i++) {
            this.signerInfos[i] = new SignerInfo(signerInfoVals[i].toDerInputStream());
        }
        return;
        try {
            parsingException = new ParsingException(e.getMessage());
            parsingException.initCause(e);
            throw parsingException;
        } catch (Throwable th4) {
            th = th4;
            if (bais != null) {
                bais.close();
            }
            throw th;
        }
        parsingException = new ParsingException(ce.getMessage());
        parsingException.initCause(ce);
        throw parsingException;
        try {
            parsingException = new ParsingException(ioe.getMessage());
            parsingException.initCause(ioe);
            throw parsingException;
        } catch (Throwable th5) {
            th = th5;
            if (bais != null) {
                bais.close();
            }
            throw th;
        }
    }

    private void parseOldSignedData(DerValue val) throws ParsingException, IOException {
        ByteArrayInputStream bais;
        CertificateException ce;
        IOException ioe;
        Throwable th;
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
        len = certVals.length;
        this.certificates = new X509Certificate[len];
        i = 0;
        while (i < len) {
            bais = null;
            try {
                byte[] original = certVals[i].getOriginalEncodedForm();
                if (certfac == null) {
                    this.certificates[i] = new X509CertImpl(certVals[i], original);
                } else {
                    ByteArrayInputStream bais2 = new ByteArrayInputStream(original);
                    try {
                        this.certificates[i] = new VerbatimX509Certificate((X509Certificate) certfac.generateCertificate(bais2), original);
                        bais2.close();
                        bais = null;
                    } catch (CertificateException e3) {
                        ce = e3;
                        bais = bais2;
                    } catch (IOException e4) {
                        ioe = e4;
                        bais = bais2;
                    } catch (Throwable th2) {
                        th = th2;
                        bais = bais2;
                    }
                }
                if (null != null) {
                    bais.close();
                }
                i++;
            } catch (CertificateException e5) {
                ce = e5;
            } catch (IOException e6) {
                ioe = e6;
            }
        }
        dis.getSet(0);
        DerValue[] signerInfoVals = dis.getSet(1);
        len = signerInfoVals.length;
        this.signerInfos = new SignerInfo[len];
        for (i = 0; i < len; i++) {
            this.signerInfos[i] = new SignerInfo(signerInfoVals[i].toDerInputStream(), true);
        }
        return;
        ParsingException parsingException;
        try {
            parsingException = new ParsingException(ioe.getMessage());
            parsingException.initCause(ioe);
            throw parsingException;
        } catch (Throwable th3) {
            th = th3;
            if (bais != null) {
                bais.close();
            }
            throw th;
        }
        parsingException = new ParsingException(ce.getMessage());
        parsingException.initCause(ce);
        throw parsingException;
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
                    } catch (Throwable ce) {
                        throw new IOException(ce);
                    }
                }
            }
            signedData.putOrderedSetOf((byte) -96, implCerts);
        }
        if (!(this.crls == null || this.crls.length == 0)) {
            Set<X509CRLImpl> implCRLs = new HashSet(this.crls.length);
            for (X509CRL crl : this.crls) {
                if (crl instanceof X509CRLImpl) {
                    implCRLs.-java_util_stream_Collectors-mthref-4((X509CRLImpl) crl);
                } else {
                    try {
                        implCRLs.-java_util_stream_Collectors-mthref-4(new X509CRLImpl(crl.getEncoded()));
                    } catch (Throwable ce2) {
                        throw new IOException(ce2);
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
        Vector<SignerInfo> intResult = new Vector();
        for (SignerInfo verify : this.signerInfos) {
            SignerInfo signerInfo = verify(verify, bytes);
            if (signerInfo != null) {
                intResult.addElement(signerInfo);
            }
        }
        if (intResult.isEmpty()) {
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
            int i = 0;
            while (i < this.certificates.length) {
                X509Certificate cert = this.certificates[i];
                if (serial.equals(cert.getSerialNumber()) && issuerName.equals(this.certIssuerNames[i])) {
                    return cert;
                }
                i++;
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
        int i;
        String out = "" + this.contentInfo + "\n";
        if (this.version != null) {
            out = out + "PKCS7 :: version: " + Debug.toHexString(this.version) + "\n";
        }
        if (this.digestAlgorithmIds != null) {
            out = out + "PKCS7 :: digest AlgorithmIds: \n";
            for (Object obj : this.digestAlgorithmIds) {
                out = out + "\t" + obj + "\n";
            }
        }
        if (this.certificates != null) {
            out = out + "PKCS7 :: certificates: \n";
            for (i = 0; i < this.certificates.length; i++) {
                out = out + "\t" + i + ".   " + this.certificates[i] + "\n";
            }
        }
        if (this.crls != null) {
            out = out + "PKCS7 :: crls: \n";
            for (i = 0; i < this.crls.length; i++) {
                out = out + "\t" + i + ".   " + this.crls[i] + "\n";
            }
        }
        if (this.signerInfos != null) {
            out = out + "PKCS7 :: signer infos: \n";
            for (i = 0; i < this.signerInfos.length; i++) {
                out = out + "\t" + i + ".  " + this.signerInfos[i] + "\n";
            }
        }
        return out;
    }

    public boolean isOldStyle() {
        return this.oldStyle;
    }
}
