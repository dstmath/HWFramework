package org.bouncycastle.jcajce.util;

import java.security.AlgorithmParameterGenerator;
import java.security.AlgorithmParameters;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
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

public class NamedJcaJceHelper implements JcaJceHelper {
    protected final String providerName;

    public NamedJcaJceHelper(String str) {
        this.providerName = str;
    }

    public AlgorithmParameterGenerator createAlgorithmParameterGenerator(String str) throws NoSuchAlgorithmException, NoSuchProviderException {
        return AlgorithmParameterGenerator.getInstance(str, this.providerName);
    }

    public AlgorithmParameters createAlgorithmParameters(String str) throws NoSuchAlgorithmException, NoSuchProviderException {
        return AlgorithmParameters.getInstance(str, this.providerName);
    }

    public CertificateFactory createCertificateFactory(String str) throws CertificateException, NoSuchProviderException {
        return CertificateFactory.getInstance(str, this.providerName);
    }

    public Cipher createCipher(String str) throws NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException {
        return Cipher.getInstance(str, this.providerName);
    }

    public MessageDigest createDigest(String str) throws NoSuchAlgorithmException, NoSuchProviderException {
        return MessageDigest.getInstance(str, this.providerName);
    }

    public KeyAgreement createKeyAgreement(String str) throws NoSuchAlgorithmException, NoSuchProviderException {
        return KeyAgreement.getInstance(str, this.providerName);
    }

    public KeyFactory createKeyFactory(String str) throws NoSuchAlgorithmException, NoSuchProviderException {
        return KeyFactory.getInstance(str, this.providerName);
    }

    public KeyGenerator createKeyGenerator(String str) throws NoSuchAlgorithmException, NoSuchProviderException {
        return KeyGenerator.getInstance(str, this.providerName);
    }

    public KeyPairGenerator createKeyPairGenerator(String str) throws NoSuchAlgorithmException, NoSuchProviderException {
        return KeyPairGenerator.getInstance(str, this.providerName);
    }

    public Mac createMac(String str) throws NoSuchAlgorithmException, NoSuchProviderException {
        return Mac.getInstance(str, this.providerName);
    }

    public SecretKeyFactory createSecretKeyFactory(String str) throws NoSuchAlgorithmException, NoSuchProviderException {
        return SecretKeyFactory.getInstance(str, this.providerName);
    }

    public SecureRandom createSecureRandom(String str) throws NoSuchAlgorithmException, NoSuchProviderException {
        return SecureRandom.getInstance(str, this.providerName);
    }

    public Signature createSignature(String str) throws NoSuchAlgorithmException, NoSuchProviderException {
        return Signature.getInstance(str, this.providerName);
    }
}
