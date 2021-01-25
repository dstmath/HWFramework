package com.android.org.bouncycastle.x509;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Date;

public class X509V1CertificateGeneratorEx {
    public void setIssuerDN(String sCommonName, String sOrganisationUnit, String sOrganisation, String sLocality, String sState, String sCountryCode, String sEmailAddress) {
    }

    public void setNotBefore(Date date) {
    }

    public void setNotAfter(Date date) {
    }

    public void setSubjectDN(String sCommonName, String sOrganisationUnit, String sOrganisation, String sLocality, String sState, String sCountryCode, String sEmailAddress) {
    }

    public void setPublicKey(PublicKey key) {
    }

    public void setSignatureAlgorithm(String signatureAlgorithm) {
    }

    public void setSerialNumber(BigInteger serialNumber) {
    }

    public X509Certificate generate(PrivateKey key, String provider) throws CertificateEncodingException, IllegalStateException, NoSuchProviderException, NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        return null;
    }
}
