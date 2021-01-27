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

public interface JcaJceHelper {
    AlgorithmParameterGenerator createAlgorithmParameterGenerator(String str) throws NoSuchAlgorithmException, NoSuchProviderException;

    AlgorithmParameters createAlgorithmParameters(String str) throws NoSuchAlgorithmException, NoSuchProviderException;

    CertPathBuilder createCertPathBuilder(String str) throws NoSuchAlgorithmException, NoSuchProviderException;

    CertPathValidator createCertPathValidator(String str) throws NoSuchAlgorithmException, NoSuchProviderException;

    CertStore createCertStore(String str, CertStoreParameters certStoreParameters) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchProviderException;

    CertificateFactory createCertificateFactory(String str) throws NoSuchProviderException, CertificateException;

    Cipher createCipher(String str) throws NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException;

    MessageDigest createDigest(String str) throws NoSuchAlgorithmException, NoSuchProviderException;

    ExemptionMechanism createExemptionMechanism(String str) throws NoSuchAlgorithmException, NoSuchProviderException;

    KeyAgreement createKeyAgreement(String str) throws NoSuchAlgorithmException, NoSuchProviderException;

    KeyFactory createKeyFactory(String str) throws NoSuchAlgorithmException, NoSuchProviderException;

    KeyGenerator createKeyGenerator(String str) throws NoSuchAlgorithmException, NoSuchProviderException;

    KeyPairGenerator createKeyPairGenerator(String str) throws NoSuchAlgorithmException, NoSuchProviderException;

    KeyStore createKeyStore(String str) throws KeyStoreException, NoSuchProviderException;

    Mac createMac(String str) throws NoSuchAlgorithmException, NoSuchProviderException;

    MessageDigest createMessageDigest(String str) throws NoSuchAlgorithmException, NoSuchProviderException;

    SecretKeyFactory createSecretKeyFactory(String str) throws NoSuchAlgorithmException, NoSuchProviderException;

    SecureRandom createSecureRandom(String str) throws NoSuchAlgorithmException, NoSuchProviderException;

    Signature createSignature(String str) throws NoSuchAlgorithmException, NoSuchProviderException;
}
