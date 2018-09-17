package com.android.org.bouncycastle.jcajce.provider.asymmetric.x509;

import com.android.org.bouncycastle.asn1.ASN1BitString;
import com.android.org.bouncycastle.asn1.ASN1Encodable;
import com.android.org.bouncycastle.asn1.ASN1Encoding;
import com.android.org.bouncycastle.asn1.ASN1InputStream;
import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import com.android.org.bouncycastle.asn1.ASN1OctetString;
import com.android.org.bouncycastle.asn1.ASN1OutputStream;
import com.android.org.bouncycastle.asn1.ASN1Primitive;
import com.android.org.bouncycastle.asn1.ASN1Sequence;
import com.android.org.bouncycastle.asn1.ASN1String;
import com.android.org.bouncycastle.asn1.DERBitString;
import com.android.org.bouncycastle.asn1.DERIA5String;
import com.android.org.bouncycastle.asn1.DERNull;
import com.android.org.bouncycastle.asn1.misc.MiscObjectIdentifiers;
import com.android.org.bouncycastle.asn1.misc.NetscapeCertType;
import com.android.org.bouncycastle.asn1.misc.NetscapeRevocationURL;
import com.android.org.bouncycastle.asn1.misc.VerisignCzagExtension;
import com.android.org.bouncycastle.asn1.util.ASN1Dump;
import com.android.org.bouncycastle.asn1.x500.X500Name;
import com.android.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import com.android.org.bouncycastle.asn1.x509.BasicConstraints;
import com.android.org.bouncycastle.asn1.x509.Certificate;
import com.android.org.bouncycastle.asn1.x509.Extension;
import com.android.org.bouncycastle.asn1.x509.Extensions;
import com.android.org.bouncycastle.asn1.x509.GeneralName;
import com.android.org.bouncycastle.asn1.x509.KeyUsage;
import com.android.org.bouncycastle.asn1.x509.X509Name;
import com.android.org.bouncycastle.jcajce.provider.asymmetric.util.PKCS12BagAttributeCarrierImpl;
import com.android.org.bouncycastle.jcajce.util.JcaJceHelper;
import com.android.org.bouncycastle.jce.X509Principal;
import com.android.org.bouncycastle.jce.interfaces.PKCS12BagAttributeCarrier;
import com.android.org.bouncycastle.jce.provider.BouncyCastleProvider;
import com.android.org.bouncycastle.util.Integers;
import com.android.org.bouncycastle.util.Strings;
import com.android.org.bouncycastle.util.encoders.Hex;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.security.auth.x500.X500Principal;

class X509CertificateObject extends X509Certificate implements PKCS12BagAttributeCarrier {
    private PKCS12BagAttributeCarrier attrCarrier = new PKCS12BagAttributeCarrierImpl();
    private BasicConstraints basicConstraints;
    private JcaJceHelper bcHelper;
    private Certificate c;
    private byte[] encoded;
    private int hashValue;
    private boolean hashValueSet;
    private boolean[] keyUsage;

    public X509CertificateObject(JcaJceHelper bcHelper, Certificate c) throws CertificateParsingException {
        int i = 9;
        this.bcHelper = bcHelper;
        this.c = c;
        try {
            byte[] bytes = getExtensionBytes("2.5.29.19");
            if (bytes != null) {
                this.basicConstraints = BasicConstraints.getInstance(ASN1Primitive.fromByteArray(bytes));
            }
            try {
                bytes = getExtensionBytes("2.5.29.15");
                if (bytes != null) {
                    ASN1BitString bits = DERBitString.getInstance(ASN1Primitive.fromByteArray(bytes));
                    bytes = bits.getBytes();
                    int length = (bytes.length * 8) - bits.getPadBits();
                    if (length >= 9) {
                        i = length;
                    }
                    this.keyUsage = new boolean[i];
                    for (int i2 = 0; i2 != length; i2++) {
                        boolean z;
                        boolean[] zArr = this.keyUsage;
                        if ((bytes[i2 / 8] & (128 >>> (i2 % 8))) != 0) {
                            z = true;
                        } else {
                            z = false;
                        }
                        zArr[i2] = z;
                    }
                    return;
                }
                this.keyUsage = null;
            } catch (Exception e) {
                throw new CertificateParsingException("cannot construct KeyUsage: " + e);
            }
        } catch (Exception e2) {
            throw new CertificateParsingException("cannot construct BasicConstraints: " + e2);
        }
    }

    public void checkValidity() throws CertificateExpiredException, CertificateNotYetValidException {
        checkValidity(new Date());
    }

    public void checkValidity(Date date) throws CertificateExpiredException, CertificateNotYetValidException {
        if (date.getTime() > getNotAfter().getTime()) {
            throw new CertificateExpiredException("certificate expired on " + this.c.getEndDate().getTime());
        } else if (date.getTime() < getNotBefore().getTime()) {
            throw new CertificateNotYetValidException("certificate not valid till " + this.c.getStartDate().getTime());
        }
    }

    public int getVersion() {
        return this.c.getVersionNumber();
    }

    public BigInteger getSerialNumber() {
        return this.c.getSerialNumber().getValue();
    }

    public Principal getIssuerDN() {
        try {
            return new X509Principal(X500Name.getInstance(this.c.getIssuer().getEncoded()));
        } catch (IOException e) {
            return null;
        }
    }

    public X500Principal getIssuerX500Principal() {
        try {
            ByteArrayOutputStream bOut = new ByteArrayOutputStream();
            new ASN1OutputStream(bOut).writeObject(this.c.getIssuer());
            return new X500Principal(bOut.toByteArray());
        } catch (IOException e) {
            throw new IllegalStateException("can't encode issuer DN");
        }
    }

    public Principal getSubjectDN() {
        return new X509Principal(X500Name.getInstance(this.c.getSubject().toASN1Primitive()));
    }

    public X500Principal getSubjectX500Principal() {
        try {
            ByteArrayOutputStream bOut = new ByteArrayOutputStream();
            new ASN1OutputStream(bOut).writeObject(this.c.getSubject());
            return new X500Principal(bOut.toByteArray());
        } catch (IOException e) {
            throw new IllegalStateException("can't encode issuer DN");
        }
    }

    public Date getNotBefore() {
        return this.c.getStartDate().getDate();
    }

    public Date getNotAfter() {
        return this.c.getEndDate().getDate();
    }

    public byte[] getTBSCertificate() throws CertificateEncodingException {
        try {
            return this.c.getTBSCertificate().getEncoded(ASN1Encoding.DER);
        } catch (IOException e) {
            throw new CertificateEncodingException(e.toString());
        }
    }

    public byte[] getSignature() {
        return this.c.getSignature().getOctets();
    }

    public String getSigAlgName() {
        return X509SignatureUtil.getSignatureName(this.c.getSignatureAlgorithm());
    }

    public String getSigAlgOID() {
        return this.c.getSignatureAlgorithm().getAlgorithm().getId();
    }

    public byte[] getSigAlgParams() {
        if (this.c.getSignatureAlgorithm().getParameters() == null) {
            return null;
        }
        try {
            return this.c.getSignatureAlgorithm().getParameters().toASN1Primitive().getEncoded(ASN1Encoding.DER);
        } catch (IOException e) {
            return null;
        }
    }

    public boolean[] getIssuerUniqueID() {
        DERBitString id = this.c.getTBSCertificate().getIssuerUniqueId();
        if (id == null) {
            return null;
        }
        byte[] bytes = id.getBytes();
        boolean[] boolId = new boolean[((bytes.length * 8) - id.getPadBits())];
        for (int i = 0; i != boolId.length; i++) {
            boolean z;
            if ((bytes[i / 8] & (128 >>> (i % 8))) != 0) {
                z = true;
            } else {
                z = false;
            }
            boolId[i] = z;
        }
        return boolId;
    }

    public boolean[] getSubjectUniqueID() {
        DERBitString id = this.c.getTBSCertificate().getSubjectUniqueId();
        if (id == null) {
            return null;
        }
        byte[] bytes = id.getBytes();
        boolean[] boolId = new boolean[((bytes.length * 8) - id.getPadBits())];
        for (int i = 0; i != boolId.length; i++) {
            boolean z;
            if ((bytes[i / 8] & (128 >>> (i % 8))) != 0) {
                z = true;
            } else {
                z = false;
            }
            boolId[i] = z;
        }
        return boolId;
    }

    public boolean[] getKeyUsage() {
        return this.keyUsage;
    }

    public List getExtendedKeyUsage() throws CertificateParsingException {
        byte[] bytes = getExtensionBytes("2.5.29.37");
        if (bytes == null) {
            return null;
        }
        try {
            ASN1Sequence seq = (ASN1Sequence) new ASN1InputStream(bytes).readObject();
            List list = new ArrayList();
            for (int i = 0; i != seq.size(); i++) {
                list.add(((ASN1ObjectIdentifier) seq.getObjectAt(i)).getId());
            }
            return Collections.unmodifiableList(list);
        } catch (Exception e) {
            throw new CertificateParsingException("error processing extended key usage extension");
        }
    }

    public int getBasicConstraints() {
        if (this.basicConstraints == null || !this.basicConstraints.isCA()) {
            return -1;
        }
        if (this.basicConstraints.getPathLenConstraint() == null) {
            return Integer.MAX_VALUE;
        }
        return this.basicConstraints.getPathLenConstraint().intValue();
    }

    public Collection getSubjectAlternativeNames() throws CertificateParsingException {
        return getAlternativeNames(getExtensionBytes(Extension.subjectAlternativeName.getId()));
    }

    public Collection getIssuerAlternativeNames() throws CertificateParsingException {
        return getAlternativeNames(getExtensionBytes(Extension.issuerAlternativeName.getId()));
    }

    public Set getCriticalExtensionOIDs() {
        if (getVersion() == 3) {
            Set set = new HashSet();
            Extensions extensions = this.c.getTBSCertificate().getExtensions();
            if (extensions != null) {
                Enumeration e = extensions.oids();
                while (e.hasMoreElements()) {
                    ASN1ObjectIdentifier oid = (ASN1ObjectIdentifier) e.nextElement();
                    if (extensions.getExtension(oid).isCritical()) {
                        set.add(oid.getId());
                    }
                }
                return set;
            }
        }
        return null;
    }

    private byte[] getExtensionBytes(String oid) {
        Extensions exts = this.c.getTBSCertificate().getExtensions();
        if (exts != null) {
            Extension ext = exts.getExtension(new ASN1ObjectIdentifier(oid));
            if (ext != null) {
                return ext.getExtnValue().getOctets();
            }
        }
        return null;
    }

    public byte[] getExtensionValue(String oid) {
        Extensions exts = this.c.getTBSCertificate().getExtensions();
        if (exts != null) {
            Extension ext = exts.getExtension(new ASN1ObjectIdentifier(oid));
            if (ext != null) {
                try {
                    return ext.getExtnValue().getEncoded();
                } catch (Exception e) {
                    throw new IllegalStateException("error parsing " + e.toString());
                }
            }
        }
        return null;
    }

    public Set getNonCriticalExtensionOIDs() {
        if (getVersion() == 3) {
            Set set = new HashSet();
            Extensions extensions = this.c.getTBSCertificate().getExtensions();
            if (extensions != null) {
                Enumeration e = extensions.oids();
                while (e.hasMoreElements()) {
                    ASN1ObjectIdentifier oid = (ASN1ObjectIdentifier) e.nextElement();
                    if (!extensions.getExtension(oid).isCritical()) {
                        set.add(oid.getId());
                    }
                }
                return set;
            }
        }
        return null;
    }

    public boolean hasUnsupportedCriticalExtension() {
        if (getVersion() == 3) {
            Extensions extensions = this.c.getTBSCertificate().getExtensions();
            if (extensions != null) {
                Enumeration e = extensions.oids();
                while (e.hasMoreElements()) {
                    ASN1ObjectIdentifier oid = (ASN1ObjectIdentifier) e.nextElement();
                    if (!oid.equals(Extension.keyUsage) && !oid.equals(Extension.certificatePolicies) && !oid.equals(Extension.policyMappings) && !oid.equals(Extension.inhibitAnyPolicy) && !oid.equals(Extension.cRLDistributionPoints) && !oid.equals(Extension.issuingDistributionPoint) && !oid.equals(Extension.deltaCRLIndicator) && !oid.equals(Extension.policyConstraints) && !oid.equals(Extension.basicConstraints) && !oid.equals(Extension.subjectAlternativeName) && !oid.equals(Extension.nameConstraints) && extensions.getExtension(oid).isCritical()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public PublicKey getPublicKey() {
        try {
            return BouncyCastleProvider.getPublicKey(this.c.getSubjectPublicKeyInfo());
        } catch (IOException e) {
            return null;
        }
    }

    public byte[] getEncoded() throws CertificateEncodingException {
        try {
            if (this.encoded == null) {
                this.encoded = this.c.getEncoded(ASN1Encoding.DER);
            }
            return this.encoded;
        } catch (IOException e) {
            throw new CertificateEncodingException(e.toString());
        }
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof X509CertificateObject)) {
            return super.equals(o);
        }
        X509CertificateObject other = (X509CertificateObject) o;
        if (this.hashValueSet && other.hashValueSet && this.hashValue != other.hashValue) {
            return false;
        }
        return this.c.equals(other.c);
    }

    public synchronized int hashCode() {
        if (!this.hashValueSet) {
            this.hashValue = super.hashCode();
            this.hashValueSet = true;
        }
        return this.hashValue;
    }

    public int originalHashCode() {
        int hashCode = 0;
        try {
            byte[] certData = getEncoded();
            for (int i = 1; i < certData.length; i++) {
                hashCode += certData[i] * i;
            }
            return hashCode;
        } catch (CertificateEncodingException e) {
            return 0;
        }
    }

    public void setBagAttribute(ASN1ObjectIdentifier oid, ASN1Encodable attribute) {
        this.attrCarrier.setBagAttribute(oid, attribute);
    }

    public ASN1Encodable getBagAttribute(ASN1ObjectIdentifier oid) {
        return this.attrCarrier.getBagAttribute(oid);
    }

    public Enumeration getBagAttributeKeys() {
        return this.attrCarrier.getBagAttributeKeys();
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        String nl = Strings.lineSeparator();
        buf.append("  [0]         Version: ").append(getVersion()).append(nl);
        buf.append("         SerialNumber: ").append(getSerialNumber()).append(nl);
        buf.append("             IssuerDN: ").append(getIssuerDN()).append(nl);
        buf.append("           Start Date: ").append(getNotBefore()).append(nl);
        buf.append("           Final Date: ").append(getNotAfter()).append(nl);
        buf.append("            SubjectDN: ").append(getSubjectDN()).append(nl);
        buf.append("           Public Key: ").append(getPublicKey()).append(nl);
        buf.append("  Signature Algorithm: ").append(getSigAlgName()).append(nl);
        byte[] sig = getSignature();
        buf.append("            Signature: ").append(new String(Hex.encode(sig, 0, 20))).append(nl);
        for (int i = 20; i < sig.length; i += 20) {
            if (i < sig.length - 20) {
                buf.append("                       ").append(new String(Hex.encode(sig, i, 20))).append(nl);
            } else {
                buf.append("                       ").append(new String(Hex.encode(sig, i, sig.length - i))).append(nl);
            }
        }
        Extensions extensions = this.c.getTBSCertificate().getExtensions();
        if (extensions != null) {
            Enumeration e = extensions.oids();
            if (e.hasMoreElements()) {
                buf.append("       Extensions: \n");
            }
            while (e.hasMoreElements()) {
                ASN1ObjectIdentifier oid = (ASN1ObjectIdentifier) e.nextElement();
                Extension ext = extensions.getExtension(oid);
                if (ext.getExtnValue() != null) {
                    ASN1InputStream dIn = new ASN1InputStream(ext.getExtnValue().getOctets());
                    buf.append("                       critical(").append(ext.isCritical()).append(") ");
                    try {
                        if (oid.equals(Extension.basicConstraints)) {
                            buf.append(BasicConstraints.getInstance(dIn.readObject())).append(nl);
                        } else if (oid.equals(Extension.keyUsage)) {
                            buf.append(KeyUsage.getInstance(dIn.readObject())).append(nl);
                        } else if (oid.equals(MiscObjectIdentifiers.netscapeCertType)) {
                            buf.append(new NetscapeCertType((DERBitString) dIn.readObject())).append(nl);
                        } else if (oid.equals(MiscObjectIdentifiers.netscapeRevocationURL)) {
                            buf.append(new NetscapeRevocationURL((DERIA5String) dIn.readObject())).append(nl);
                        } else if (oid.equals(MiscObjectIdentifiers.verisignCzagExtension)) {
                            buf.append(new VerisignCzagExtension((DERIA5String) dIn.readObject())).append(nl);
                        } else {
                            buf.append(oid.getId());
                            buf.append(" value = ").append(ASN1Dump.dumpAsString(dIn.readObject())).append(nl);
                        }
                    } catch (Exception e2) {
                        buf.append(oid.getId());
                        buf.append(" value = ").append("*****").append(nl);
                    }
                } else {
                    buf.append(nl);
                }
            }
        }
        return buf.toString();
    }

    public final void verify(PublicKey key) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {
        Signature signature;
        String sigName = X509SignatureUtil.getSignatureName(this.c.getSignatureAlgorithm());
        try {
            signature = this.bcHelper.createSignature(sigName);
        } catch (Exception e) {
            signature = Signature.getInstance(sigName);
        }
        checkSignature(key, signature);
    }

    public final void verify(PublicKey key, String sigProvider) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {
        Signature signature;
        String sigName = X509SignatureUtil.getSignatureName(this.c.getSignatureAlgorithm());
        if (sigProvider != null) {
            signature = Signature.getInstance(sigName, sigProvider);
        } else {
            signature = Signature.getInstance(sigName);
        }
        checkSignature(key, signature);
    }

    public final void verify(PublicKey key, Provider sigProvider) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature signature;
        String sigName = X509SignatureUtil.getSignatureName(this.c.getSignatureAlgorithm());
        if (sigProvider != null) {
            signature = Signature.getInstance(sigName, sigProvider);
        } else {
            signature = Signature.getInstance(sigName);
        }
        checkSignature(key, signature);
    }

    private void checkSignature(PublicKey key, Signature signature) throws CertificateException, NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        if (isAlgIdEqual(this.c.getSignatureAlgorithm(), this.c.getTBSCertificate().getSignature())) {
            X509SignatureUtil.setSignatureParameters(signature, this.c.getSignatureAlgorithm().getParameters());
            signature.initVerify(key);
            signature.update(getTBSCertificate());
            if (!signature.verify(getSignature())) {
                throw new SignatureException("certificate does not verify with supplied key");
            }
            return;
        }
        throw new CertificateException("signature algorithm in TBS cert not same as outer cert");
    }

    private boolean isAlgIdEqual(AlgorithmIdentifier id1, AlgorithmIdentifier id2) {
        if (!id1.getAlgorithm().equals(id2.getAlgorithm())) {
            return false;
        }
        if (id1.getParameters() == null) {
            return id2.getParameters() == null || (id2.getParameters().equals(DERNull.INSTANCE) ^ 1) == 0;
        } else {
            if (id2.getParameters() == null) {
                return id1.getParameters() == null || (id1.getParameters().equals(DERNull.INSTANCE) ^ 1) == 0;
            } else {
                return id1.getParameters().equals(id2.getParameters());
            }
        }
    }

    private static Collection getAlternativeNames(byte[] extVal) throws CertificateParsingException {
        if (extVal == null) {
            return null;
        }
        try {
            Collection temp = new ArrayList();
            Enumeration it = ASN1Sequence.getInstance(extVal).getObjects();
            while (it.hasMoreElements()) {
                GeneralName genName = GeneralName.getInstance(it.nextElement());
                List list = new ArrayList();
                list.add(Integers.valueOf(genName.getTagNo()));
                switch (genName.getTagNo()) {
                    case 0:
                    case 3:
                    case 5:
                        list.add(genName.getEncoded());
                        break;
                    case 1:
                    case 2:
                    case 6:
                        list.add(((ASN1String) genName.getName()).getString());
                        break;
                    case 4:
                        list.add(X509Name.getInstance(genName.getName()).toString(true, X509Name.DefaultSymbols));
                        break;
                    case 7:
                        try {
                            list.add(InetAddress.getByAddress(ASN1OctetString.getInstance(genName.getName()).getOctets()).getHostAddress());
                            break;
                        } catch (UnknownHostException e) {
                            break;
                        }
                    case 8:
                        list.add(ASN1ObjectIdentifier.getInstance(genName.getName()).getId());
                        break;
                    default:
                        throw new IOException("Bad tag number: " + genName.getTagNo());
                }
                temp.add(Collections.unmodifiableList(list));
            }
            if (temp.size() == 0) {
                return null;
            }
            return Collections.unmodifiableCollection(temp);
        } catch (Exception e2) {
            throw new CertificateParsingException(e2.getMessage());
        }
    }
}
