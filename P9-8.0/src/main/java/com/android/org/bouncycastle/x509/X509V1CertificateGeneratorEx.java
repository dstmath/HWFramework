package com.android.org.bouncycastle.x509;

import com.android.org.bouncycastle.jce.X509PrincipalEx;
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
    private X509V1CertificateGenerator mGenerator = new X509V1CertificateGenerator();

    public void setIssuerDN(String sCommonName, String sOrganisationUnit, String sOrganisation, String sLocality, String sState, String sCountryCode, String sEmailAddress) {
        this.mGenerator.setIssuerDN(X509PrincipalEx.getX509Principal(sCommonName, sOrganisationUnit, sOrganisation, sLocality, sState, sCountryCode, sEmailAddress));
    }

    public void setNotBefore(Date date) {
        this.mGenerator.setNotBefore(date);
    }

    public void setNotAfter(Date date) {
        this.mGenerator.setNotAfter(date);
    }

    public void setSubjectDN(String sCommonName, String sOrganisationUnit, String sOrganisation, String sLocality, String sState, String sCountryCode, String sEmailAddress) {
        this.mGenerator.setSubjectDN(X509PrincipalEx.getX509Principal(sCommonName, sOrganisationUnit, sOrganisation, sLocality, sState, sCountryCode, sEmailAddress));
    }

    public void setPublicKey(PublicKey key) {
        this.mGenerator.setPublicKey(key);
    }

    public void setSignatureAlgorithm(String signatureAlgorithm) {
        this.mGenerator.setSignatureAlgorithm(signatureAlgorithm);
    }

    public void setSerialNumber(BigInteger serialNumber) {
        this.mGenerator.setSerialNumber(serialNumber);
    }

    public X509Certificate generate(PrivateKey key, String provider) throws CertificateEncodingException, IllegalStateException, NoSuchProviderException, NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        return this.mGenerator.generate(key, provider);
    }
}
