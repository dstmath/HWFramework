package com.android.org.bouncycastle.x509;

import com.android.org.bouncycastle.asn1.ASN1EncodableVector;
import com.android.org.bouncycastle.asn1.ASN1Encoding;
import com.android.org.bouncycastle.asn1.ASN1Integer;
import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import com.android.org.bouncycastle.asn1.DERBitString;
import com.android.org.bouncycastle.asn1.DERSequence;
import com.android.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import com.android.org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import com.android.org.bouncycastle.asn1.x509.TBSCertificate;
import com.android.org.bouncycastle.asn1.x509.Time;
import com.android.org.bouncycastle.asn1.x509.V1TBSCertificateGenerator;
import com.android.org.bouncycastle.asn1.x509.X509Name;
import com.android.org.bouncycastle.jcajce.provider.asymmetric.x509.CertificateFactory;
import com.android.org.bouncycastle.jcajce.util.BCJcaJceHelper;
import com.android.org.bouncycastle.jcajce.util.JcaJceHelper;
import com.android.org.bouncycastle.jce.X509Principal;
import com.android.org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Iterator;
import javax.security.auth.x500.X500Principal;

public class X509V1CertificateGenerator {
    private final JcaJceHelper bcHelper = new BCJcaJceHelper();
    private final CertificateFactory certificateFactory = new CertificateFactory();
    private AlgorithmIdentifier sigAlgId;
    private ASN1ObjectIdentifier sigOID;
    private String signatureAlgorithm;
    private V1TBSCertificateGenerator tbsGen = new V1TBSCertificateGenerator();

    public void reset() {
        this.tbsGen = new V1TBSCertificateGenerator();
    }

    public void setSerialNumber(BigInteger serialNumber) {
        if (serialNumber.compareTo(BigInteger.ZERO) <= 0) {
            throw new IllegalArgumentException("serial number must be a positive integer");
        }
        this.tbsGen.setSerialNumber(new ASN1Integer(serialNumber));
    }

    public void setIssuerDN(X500Principal issuer) {
        try {
            this.tbsGen.setIssuer(new X509Principal(issuer.getEncoded()));
        } catch (IOException e) {
            throw new IllegalArgumentException("can't process principal: " + e);
        }
    }

    public void setIssuerDN(X509Name issuer) {
        this.tbsGen.setIssuer(issuer);
    }

    public void setNotBefore(Date date) {
        this.tbsGen.setStartDate(new Time(date));
    }

    public void setNotAfter(Date date) {
        this.tbsGen.setEndDate(new Time(date));
    }

    public void setSubjectDN(X500Principal subject) {
        try {
            this.tbsGen.setSubject(new X509Principal(subject.getEncoded()));
        } catch (IOException e) {
            throw new IllegalArgumentException("can't process principal: " + e);
        }
    }

    public void setSubjectDN(X509Name subject) {
        this.tbsGen.setSubject(subject);
    }

    public void setPublicKey(PublicKey key) {
        try {
            this.tbsGen.setSubjectPublicKeyInfo(SubjectPublicKeyInfo.getInstance(key.getEncoded()));
        } catch (Exception e) {
            throw new IllegalArgumentException("unable to process key - " + e.toString());
        }
    }

    public void setSignatureAlgorithm(String signatureAlgorithm) {
        this.signatureAlgorithm = signatureAlgorithm;
        try {
            this.sigOID = X509Util.getAlgorithmOID(signatureAlgorithm);
            this.sigAlgId = X509Util.getSigAlgID(this.sigOID, signatureAlgorithm);
            this.tbsGen.setSignature(this.sigAlgId);
        } catch (Exception e) {
            throw new IllegalArgumentException("Unknown signature type requested");
        }
    }

    public X509Certificate generateX509Certificate(PrivateKey key) throws SecurityException, SignatureException, InvalidKeyException {
        try {
            return generateX509Certificate(key, BouncyCastleProvider.PROVIDER_NAME, null);
        } catch (NoSuchProviderException e) {
            throw new SecurityException("BC provider not installed!");
        }
    }

    public X509Certificate generateX509Certificate(PrivateKey key, SecureRandom random) throws SecurityException, SignatureException, InvalidKeyException {
        try {
            return generateX509Certificate(key, BouncyCastleProvider.PROVIDER_NAME, random);
        } catch (NoSuchProviderException e) {
            throw new SecurityException("BC provider not installed!");
        }
    }

    public X509Certificate generateX509Certificate(PrivateKey key, String provider) throws NoSuchProviderException, SecurityException, SignatureException, InvalidKeyException {
        return generateX509Certificate(key, provider, null);
    }

    public X509Certificate generateX509Certificate(PrivateKey key, String provider, SecureRandom random) throws NoSuchProviderException, SecurityException, SignatureException, InvalidKeyException {
        try {
            return generate(key, provider, random);
        } catch (NoSuchProviderException e) {
            throw e;
        } catch (SignatureException e2) {
            throw e2;
        } catch (InvalidKeyException e3) {
            throw e3;
        } catch (GeneralSecurityException e4) {
            throw new SecurityException("exception: " + e4);
        }
    }

    public X509Certificate generate(PrivateKey key) throws CertificateEncodingException, IllegalStateException, NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        return generate(key, (SecureRandom) null);
    }

    public X509Certificate generate(PrivateKey key, SecureRandom random) throws CertificateEncodingException, IllegalStateException, NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        TBSCertificate tbsCert = this.tbsGen.generateTBSCertificate();
        try {
            return generateJcaObject(tbsCert, X509Util.calculateSignature(this.sigOID, this.signatureAlgorithm, key, random, tbsCert));
        } catch (IOException e) {
            throw new ExtCertificateEncodingException("exception encoding TBS cert", e);
        }
    }

    public X509Certificate generate(PrivateKey key, String provider) throws CertificateEncodingException, IllegalStateException, NoSuchProviderException, NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        return generate(key, provider, null);
    }

    public X509Certificate generate(PrivateKey key, String provider, SecureRandom random) throws CertificateEncodingException, IllegalStateException, NoSuchProviderException, NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        TBSCertificate tbsCert = this.tbsGen.generateTBSCertificate();
        try {
            return generateJcaObject(tbsCert, X509Util.calculateSignature(this.sigOID, this.signatureAlgorithm, provider, key, random, tbsCert));
        } catch (IOException e) {
            throw new ExtCertificateEncodingException("exception encoding TBS cert", e);
        }
    }

    private X509Certificate generateJcaObject(TBSCertificate tbsCert, byte[] signature) throws CertificateEncodingException {
        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(tbsCert);
        v.add(this.sigAlgId);
        v.add(new DERBitString(signature));
        try {
            return (X509Certificate) this.certificateFactory.engineGenerateCertificate(new ByteArrayInputStream(new DERSequence(v).getEncoded(ASN1Encoding.DER)));
        } catch (Exception e) {
            throw new ExtCertificateEncodingException("exception producing certificate object", e);
        }
    }

    public Iterator getSignatureAlgNames() {
        return X509Util.getAlgNames();
    }
}
