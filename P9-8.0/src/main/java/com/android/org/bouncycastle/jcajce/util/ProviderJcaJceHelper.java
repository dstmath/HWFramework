package com.android.org.bouncycastle.jcajce.util;

import java.security.AlgorithmParameterGenerator;
import java.security.AlgorithmParameters;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;

public class ProviderJcaJceHelper implements JcaJceHelper {
    protected final Provider provider;

    public ProviderJcaJceHelper(Provider provider) {
        this.provider = provider;
    }

    public Cipher createCipher(String algorithm) throws NoSuchAlgorithmException, NoSuchPaddingException {
        return Cipher.getInstance(algorithm, this.provider);
    }

    public Mac createMac(String algorithm) throws NoSuchAlgorithmException {
        return Mac.getInstance(algorithm, this.provider);
    }

    public KeyAgreement createKeyAgreement(String algorithm) throws NoSuchAlgorithmException {
        return KeyAgreement.getInstance(algorithm, this.provider);
    }

    public AlgorithmParameterGenerator createAlgorithmParameterGenerator(String algorithm) throws NoSuchAlgorithmException {
        return AlgorithmParameterGenerator.getInstance(algorithm, this.provider);
    }

    public AlgorithmParameters createAlgorithmParameters(String algorithm) throws NoSuchAlgorithmException {
        return AlgorithmParameters.getInstance(algorithm, this.provider);
    }

    public KeyGenerator createKeyGenerator(String algorithm) throws NoSuchAlgorithmException {
        return KeyGenerator.getInstance(algorithm, this.provider);
    }

    public KeyFactory createKeyFactory(String algorithm) throws NoSuchAlgorithmException {
        return KeyFactory.getInstance(algorithm, this.provider);
    }

    public SecretKeyFactory createSecretKeyFactory(String algorithm) throws NoSuchAlgorithmException {
        return SecretKeyFactory.getInstance(algorithm, this.provider);
    }

    public KeyPairGenerator createKeyPairGenerator(String algorithm) throws NoSuchAlgorithmException {
        return KeyPairGenerator.getInstance(algorithm, this.provider);
    }

    public MessageDigest createDigest(String algorithm) throws NoSuchAlgorithmException {
        return MessageDigest.getInstance(algorithm, this.provider);
    }

    public Signature createSignature(String algorithm) throws NoSuchAlgorithmException {
        return Signature.getInstance(algorithm, this.provider);
    }

    public CertificateFactory createCertificateFactory(String algorithm) throws CertificateException {
        return CertificateFactory.getInstance(algorithm, this.provider);
    }

    public SecureRandom createSecureRandom(String algorithm) throws NoSuchAlgorithmException {
        return SecureRandom.getInstance(algorithm, this.provider);
    }
}
