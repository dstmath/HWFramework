package org.bouncycastle.jcajce.util;

import java.security.AlgorithmParameterGenerator;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertPathValidator;
import java.security.cert.CertStore;
import java.security.cert.CertStoreParameters;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import javax.crypto.Cipher;
import javax.crypto.ExemptionMechanism;
import javax.crypto.KeyAgreement;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;

public class NamedJcaJceHelper implements JcaJceHelper {
    protected final String providerName;

    public NamedJcaJceHelper(String str) {
        this.providerName = str;
    }

    @Override // org.bouncycastle.jcajce.util.JcaJceHelper
    public AlgorithmParameterGenerator createAlgorithmParameterGenerator(String str) throws NoSuchAlgorithmException, NoSuchProviderException {
        return AlgorithmParameterGenerator.getInstance(str, this.providerName);
    }

    @Override // org.bouncycastle.jcajce.util.JcaJceHelper
    public AlgorithmParameters createAlgorithmParameters(String str) throws NoSuchAlgorithmException, NoSuchProviderException {
        return AlgorithmParameters.getInstance(str, this.providerName);
    }

    @Override // org.bouncycastle.jcajce.util.JcaJceHelper
    public CertPathBuilder createCertPathBuilder(String str) throws NoSuchAlgorithmException, NoSuchProviderException {
        return CertPathBuilder.getInstance(str, this.providerName);
    }

    @Override // org.bouncycastle.jcajce.util.JcaJceHelper
    public CertPathValidator createCertPathValidator(String str) throws NoSuchAlgorithmException, NoSuchProviderException {
        return CertPathValidator.getInstance(str, this.providerName);
    }

    @Override // org.bouncycastle.jcajce.util.JcaJceHelper
    public CertStore createCertStore(String str, CertStoreParameters certStoreParameters) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchProviderException {
        return CertStore.getInstance(str, certStoreParameters, this.providerName);
    }

    @Override // org.bouncycastle.jcajce.util.JcaJceHelper
    public CertificateFactory createCertificateFactory(String str) throws CertificateException, NoSuchProviderException {
        return CertificateFactory.getInstance(str, this.providerName);
    }

    @Override // org.bouncycastle.jcajce.util.JcaJceHelper
    public Cipher createCipher(String str) throws NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException {
        return Cipher.getInstance(str, this.providerName);
    }

    @Override // org.bouncycastle.jcajce.util.JcaJceHelper
    public MessageDigest createDigest(String str) throws NoSuchAlgorithmException, NoSuchProviderException {
        return MessageDigest.getInstance(str, this.providerName);
    }

    @Override // org.bouncycastle.jcajce.util.JcaJceHelper
    public ExemptionMechanism createExemptionMechanism(String str) throws NoSuchAlgorithmException, NoSuchProviderException {
        return ExemptionMechanism.getInstance(str, this.providerName);
    }

    @Override // org.bouncycastle.jcajce.util.JcaJceHelper
    public KeyAgreement createKeyAgreement(String str) throws NoSuchAlgorithmException, NoSuchProviderException {
        return KeyAgreement.getInstance(str, this.providerName);
    }

    @Override // org.bouncycastle.jcajce.util.JcaJceHelper
    public KeyFactory createKeyFactory(String str) throws NoSuchAlgorithmException, NoSuchProviderException {
        return KeyFactory.getInstance(str, this.providerName);
    }

    @Override // org.bouncycastle.jcajce.util.JcaJceHelper
    public KeyGenerator createKeyGenerator(String str) throws NoSuchAlgorithmException, NoSuchProviderException {
        return KeyGenerator.getInstance(str, this.providerName);
    }

    @Override // org.bouncycastle.jcajce.util.JcaJceHelper
    public KeyPairGenerator createKeyPairGenerator(String str) throws NoSuchAlgorithmException, NoSuchProviderException {
        return KeyPairGenerator.getInstance(str, this.providerName);
    }

    @Override // org.bouncycastle.jcajce.util.JcaJceHelper
    public KeyStore createKeyStore(String str) throws KeyStoreException, NoSuchProviderException {
        return KeyStore.getInstance(str, this.providerName);
    }

    @Override // org.bouncycastle.jcajce.util.JcaJceHelper
    public Mac createMac(String str) throws NoSuchAlgorithmException, NoSuchProviderException {
        return Mac.getInstance(str, this.providerName);
    }

    @Override // org.bouncycastle.jcajce.util.JcaJceHelper
    public MessageDigest createMessageDigest(String str) throws NoSuchAlgorithmException, NoSuchProviderException {
        return MessageDigest.getInstance(str, this.providerName);
    }

    @Override // org.bouncycastle.jcajce.util.JcaJceHelper
    public SecretKeyFactory createSecretKeyFactory(String str) throws NoSuchAlgorithmException, NoSuchProviderException {
        return SecretKeyFactory.getInstance(str, this.providerName);
    }

    @Override // org.bouncycastle.jcajce.util.JcaJceHelper
    public SecureRandom createSecureRandom(String str) throws NoSuchAlgorithmException, NoSuchProviderException {
        return SecureRandom.getInstance(str, this.providerName);
    }

    @Override // org.bouncycastle.jcajce.util.JcaJceHelper
    public Signature createSignature(String str) throws NoSuchAlgorithmException, NoSuchProviderException {
        return Signature.getInstance(str, this.providerName);
    }
}
