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

public class DefaultJcaJceHelper implements JcaJceHelper {
    @Override // org.bouncycastle.jcajce.util.JcaJceHelper
    public AlgorithmParameterGenerator createAlgorithmParameterGenerator(String str) throws NoSuchAlgorithmException {
        return AlgorithmParameterGenerator.getInstance(str);
    }

    @Override // org.bouncycastle.jcajce.util.JcaJceHelper
    public AlgorithmParameters createAlgorithmParameters(String str) throws NoSuchAlgorithmException {
        return AlgorithmParameters.getInstance(str);
    }

    @Override // org.bouncycastle.jcajce.util.JcaJceHelper
    public CertPathBuilder createCertPathBuilder(String str) throws NoSuchAlgorithmException {
        return CertPathBuilder.getInstance(str);
    }

    @Override // org.bouncycastle.jcajce.util.JcaJceHelper
    public CertPathValidator createCertPathValidator(String str) throws NoSuchAlgorithmException {
        return CertPathValidator.getInstance(str);
    }

    @Override // org.bouncycastle.jcajce.util.JcaJceHelper
    public CertStore createCertStore(String str, CertStoreParameters certStoreParameters) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        return CertStore.getInstance(str, certStoreParameters);
    }

    @Override // org.bouncycastle.jcajce.util.JcaJceHelper
    public CertificateFactory createCertificateFactory(String str) throws CertificateException {
        return CertificateFactory.getInstance(str);
    }

    @Override // org.bouncycastle.jcajce.util.JcaJceHelper
    public Cipher createCipher(String str) throws NoSuchAlgorithmException, NoSuchPaddingException {
        return Cipher.getInstance(str);
    }

    @Override // org.bouncycastle.jcajce.util.JcaJceHelper
    public MessageDigest createDigest(String str) throws NoSuchAlgorithmException {
        return MessageDigest.getInstance(str);
    }

    @Override // org.bouncycastle.jcajce.util.JcaJceHelper
    public ExemptionMechanism createExemptionMechanism(String str) throws NoSuchAlgorithmException {
        return ExemptionMechanism.getInstance(str);
    }

    @Override // org.bouncycastle.jcajce.util.JcaJceHelper
    public KeyAgreement createKeyAgreement(String str) throws NoSuchAlgorithmException {
        return KeyAgreement.getInstance(str);
    }

    @Override // org.bouncycastle.jcajce.util.JcaJceHelper
    public KeyFactory createKeyFactory(String str) throws NoSuchAlgorithmException {
        return KeyFactory.getInstance(str);
    }

    @Override // org.bouncycastle.jcajce.util.JcaJceHelper
    public KeyGenerator createKeyGenerator(String str) throws NoSuchAlgorithmException {
        return KeyGenerator.getInstance(str);
    }

    @Override // org.bouncycastle.jcajce.util.JcaJceHelper
    public KeyPairGenerator createKeyPairGenerator(String str) throws NoSuchAlgorithmException {
        return KeyPairGenerator.getInstance(str);
    }

    @Override // org.bouncycastle.jcajce.util.JcaJceHelper
    public KeyStore createKeyStore(String str) throws KeyStoreException {
        return KeyStore.getInstance(str);
    }

    @Override // org.bouncycastle.jcajce.util.JcaJceHelper
    public Mac createMac(String str) throws NoSuchAlgorithmException {
        return Mac.getInstance(str);
    }

    @Override // org.bouncycastle.jcajce.util.JcaJceHelper
    public MessageDigest createMessageDigest(String str) throws NoSuchAlgorithmException {
        return MessageDigest.getInstance(str);
    }

    @Override // org.bouncycastle.jcajce.util.JcaJceHelper
    public SecretKeyFactory createSecretKeyFactory(String str) throws NoSuchAlgorithmException {
        return SecretKeyFactory.getInstance(str);
    }

    @Override // org.bouncycastle.jcajce.util.JcaJceHelper
    public SecureRandom createSecureRandom(String str) throws NoSuchAlgorithmException {
        return SecureRandom.getInstance(str);
    }

    @Override // org.bouncycastle.jcajce.util.JcaJceHelper
    public Signature createSignature(String str) throws NoSuchAlgorithmException {
        return Signature.getInstance(str);
    }
}
