package com.huawei.security.keystore;

import android.security.keystore.KeyGenParameterSpec;
import android.support.annotation.Nullable;
import android.util.Log;
import com.android.internal.util.ArrayUtils;
import com.android.org.bouncycastle.asn1.ASN1EncodableVector;
import com.android.org.bouncycastle.asn1.ASN1InputStream;
import com.android.org.bouncycastle.asn1.ASN1Integer;
import com.android.org.bouncycastle.asn1.DERBitString;
import com.android.org.bouncycastle.asn1.DERInteger;
import com.android.org.bouncycastle.asn1.DERNull;
import com.android.org.bouncycastle.asn1.DERSequence;
import com.android.org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import com.android.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import com.android.org.bouncycastle.asn1.x509.Certificate;
import com.android.org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import com.android.org.bouncycastle.asn1.x509.TBSCertificate;
import com.android.org.bouncycastle.asn1.x509.Time;
import com.android.org.bouncycastle.asn1.x509.V3TBSCertificateGenerator;
import com.android.org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import com.android.org.bouncycastle.jce.X509Principal;
import com.android.org.bouncycastle.jce.provider.X509CertificateObject;
import com.android.org.bouncycastle.x509.X509V3CertificateGenerator;
import com.huawei.security.HwCredentials;
import com.huawei.security.HwKeystoreManager;
import com.huawei.security.keymaster.HwKeyCharacteristics;
import com.huawei.security.keymaster.HwKeymasterArguments;
import com.huawei.security.keymaster.HwKeymasterBlob;
import com.huawei.security.keymaster.HwKeymasterCertificateChain;
import com.huawei.security.keymaster.HwKeymasterDefs;
import com.huawei.security.keymaster.HwKeymasterUtils;
import com.huawei.security.keystore.HwKeyProperties;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGeneratorSpi;
import java.security.PrivateKey;
import java.security.ProviderException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.RSAKeyGenParameterSpec;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class HwUniversalKeyStoreKeyPairGeneratorSpi extends KeyPairGeneratorSpi {
    private static final int EC_DEFAULT_KEY_SIZE = 256;
    private static final int RSA_DEFAULT_KEY_SIZE = 2048;
    private static final int RSA_MAX_KEY_SIZE = 4096;
    private static final int RSA_MIN_KEY_SIZE = 512;
    private static final List<String> SUPPORTED_EC_NIST_CURVE_NAMES = new ArrayList();
    private static final Map<String, Integer> SUPPORTED_EC_NIST_CURVE_NAME_TO_SIZE = new HashMap();
    private static final List<Integer> SUPPORTED_EC_NIST_CURVE_SIZES = new ArrayList();
    public static final String TAG = "HwKeyPairGenerator";
    private boolean mEncryptionAtRestRequired;
    private String mEntryAlias;
    private int mEntryUid;
    private int mKeySizeBits;
    private HwKeystoreManager mKeyStore;
    private int mKeymasterAlgorithm = -1;
    private int[] mKeymasterBlockModes;
    private int[] mKeymasterDigests;
    private int[] mKeymasterEncryptionPaddings;
    private int[] mKeymasterPurposes;
    private int[] mKeymasterSignaturePaddings;
    private int mOriginalKeymasterAlgorithm;
    private BigInteger mRSAPublicExponent;
    private SecureRandom mRng;
    private HwKeyGenParameterSpec mSpec;

    public static class EC extends HwUniversalKeyStoreKeyPairGeneratorSpi {
        public EC() {
            super(3);
        }
    }

    public static class RSA extends HwUniversalKeyStoreKeyPairGeneratorSpi {
        public RSA() {
            super(1);
        }
    }

    static {
        SUPPORTED_EC_NIST_CURVE_NAME_TO_SIZE.put("p-224", 224);
        SUPPORTED_EC_NIST_CURVE_NAME_TO_SIZE.put("secp224r1", 224);
        SUPPORTED_EC_NIST_CURVE_NAME_TO_SIZE.put("p-256", Integer.valueOf(EC_DEFAULT_KEY_SIZE));
        SUPPORTED_EC_NIST_CURVE_NAME_TO_SIZE.put("secp256r1", Integer.valueOf(EC_DEFAULT_KEY_SIZE));
        SUPPORTED_EC_NIST_CURVE_NAME_TO_SIZE.put("prime256v1", Integer.valueOf(EC_DEFAULT_KEY_SIZE));
        SUPPORTED_EC_NIST_CURVE_NAME_TO_SIZE.put("p-384", 384);
        SUPPORTED_EC_NIST_CURVE_NAME_TO_SIZE.put("secp384r1", 384);
        SUPPORTED_EC_NIST_CURVE_NAME_TO_SIZE.put("p-521", 521);
        SUPPORTED_EC_NIST_CURVE_NAME_TO_SIZE.put("secp521r1", 521);
        SUPPORTED_EC_NIST_CURVE_NAMES.addAll(SUPPORTED_EC_NIST_CURVE_NAME_TO_SIZE.keySet());
        Collections.sort(SUPPORTED_EC_NIST_CURVE_NAMES);
        SUPPORTED_EC_NIST_CURVE_SIZES.addAll(new HashSet(SUPPORTED_EC_NIST_CURVE_NAME_TO_SIZE.values()));
        Collections.sort(SUPPORTED_EC_NIST_CURVE_SIZES);
    }

    protected HwUniversalKeyStoreKeyPairGeneratorSpi(int keymasterAlgorithm) {
        this.mOriginalKeymasterAlgorithm = keymasterAlgorithm;
    }

    public void initialize(int keysize, SecureRandom random) {
        throw new IllegalArgumentException(HwKeyGenParameterSpec.class.getName() + " required to initialize this HwKeyPairGenerator");
    }

    public void initialize(AlgorithmParameterSpec params, SecureRandom random) throws InvalidAlgorithmParameterException {
        HwKeyGenParameterSpec spec;
        resetAll();
        Log.e(TAG, "HwUniversalKeyStoreKeyPairGeneratorSpi initialize");
        if (params != null) {
            try {
                int keymasterAlgorithm = this.mOriginalKeymasterAlgorithm;
                if (params instanceof HwKeyGenParameterSpec) {
                    spec = (HwKeyGenParameterSpec) params;
                } else if (params instanceof KeyGenParameterSpec) {
                    spec = HwKeyGenParameterSpec.getInstance((KeyGenParameterSpec) params);
                } else {
                    throw new InvalidAlgorithmParameterException("Unsupported params class: " + params.getClass().getName() + ". Supported: " + HwKeyGenParameterSpec.class.getName());
                }
                this.mEntryAlias = getEntryAlias(spec.getKeystoreAlias());
                this.mEntryUid = spec.getUid();
                this.mSpec = spec;
                this.mKeymasterAlgorithm = keymasterAlgorithm;
                this.mKeySizeBits = spec.getKeySize();
                initAlgorithmSpecificParameters();
                if (this.mKeySizeBits == -1) {
                    this.mKeySizeBits = getDefaultKeySize(keymasterAlgorithm);
                }
                checkValidKeySize(keymasterAlgorithm, this.mKeySizeBits);
                if (spec.getKeystoreAlias() != null) {
                    this.mKeymasterPurposes = HwKeyProperties.Purpose.allToKeymaster(spec.getPurposes());
                    this.mKeymasterBlockModes = HwKeyProperties.BlockMode.allToKeymaster(spec.getBlockModes());
                    this.mKeymasterEncryptionPaddings = HwKeyProperties.EncryptionPadding.allToKeymaster(spec.getEncryptionPaddings());
                    this.mKeymasterSignaturePaddings = HwKeyProperties.SignaturePadding.allToKeymaster(spec.getSignaturePaddings());
                    if (spec.isDigestsSpecified()) {
                        this.mKeymasterDigests = HwKeyProperties.Digest.allToKeymaster(spec.getDigests());
                    }
                    this.mRng = random;
                    this.mKeyStore = HwKeystoreManager.getInstance();
                    if (1 == 0) {
                        resetAll();
                        return;
                    }
                    return;
                }
                throw new InvalidAlgorithmParameterException("KeyStore entry alias not provided");
            } catch (IllegalArgumentException | IllegalStateException e) {
                throw new InvalidAlgorithmParameterException(e);
            } catch (Throwable th) {
                if (0 == 0) {
                    resetAll();
                }
                throw th;
            }
        } else {
            throw new InvalidAlgorithmParameterException("Must supply params of type " + HwKeyGenParameterSpec.class.getName());
        }
    }

    private void initAlgorithmSpecificParameters() throws InvalidAlgorithmParameterException {
        AlgorithmParameterSpec algSpecificSpec = this.mSpec.getAlgorithmParameterSpec();
        int i = this.mKeymasterAlgorithm;
        if (i == 1) {
            BigInteger publicExponent = null;
            if (algSpecificSpec instanceof RSAKeyGenParameterSpec) {
                RSAKeyGenParameterSpec rsaSpec = (RSAKeyGenParameterSpec) algSpecificSpec;
                if (this.mKeySizeBits == -1) {
                    this.mKeySizeBits = rsaSpec.getKeysize();
                } else if (this.mKeySizeBits != rsaSpec.getKeysize()) {
                    throw new InvalidAlgorithmParameterException("RSA key size must match  between " + this.mSpec + " and " + algSpecificSpec + ": " + this.mKeySizeBits + " vs " + rsaSpec.getKeysize());
                }
                publicExponent = rsaSpec.getPublicExponent();
            } else if (algSpecificSpec != null) {
                throw new InvalidAlgorithmParameterException("RSA may only use RSAKeyGenParameterSpec");
            }
            if (publicExponent == null) {
                publicExponent = RSAKeyGenParameterSpec.F4;
            }
            if (publicExponent.compareTo(BigInteger.ZERO) < 1) {
                throw new InvalidAlgorithmParameterException("RSA public exponent must be positive: " + publicExponent);
            } else if (publicExponent.compareTo(HwKeymasterArguments.UINT64_MAX_VALUE) <= 0) {
                this.mRSAPublicExponent = publicExponent;
            } else {
                throw new InvalidAlgorithmParameterException("Unsupported RSA public exponent: " + publicExponent + ". Maximum supported value: " + HwKeymasterArguments.UINT64_MAX_VALUE);
            }
        } else if (i != 3) {
            throw new ProviderException("Unsupported algorithm: " + this.mKeymasterAlgorithm);
        } else if (algSpecificSpec instanceof ECGenParameterSpec) {
            String curveName = ((ECGenParameterSpec) algSpecificSpec).getName();
            Integer ecSpecKeySizeBits = SUPPORTED_EC_NIST_CURVE_NAME_TO_SIZE.get(curveName.toLowerCase(Locale.US));
            if (ecSpecKeySizeBits == null) {
                throw new InvalidAlgorithmParameterException("Unsupported EC curve name: " + curveName + ". Supported: " + SUPPORTED_EC_NIST_CURVE_NAMES);
            } else if (this.mKeySizeBits == -1) {
                this.mKeySizeBits = ecSpecKeySizeBits.intValue();
            } else if (this.mKeySizeBits != ecSpecKeySizeBits.intValue()) {
                throw new InvalidAlgorithmParameterException("EC key size must match  between " + this.mSpec + " and " + algSpecificSpec + ": " + this.mKeySizeBits + " vs " + ecSpecKeySizeBits);
            }
        } else if (algSpecificSpec != null) {
            throw new InvalidAlgorithmParameterException("EC may only use ECGenParameterSpec");
        }
    }

    private static void checkValidKeySize(int keymasterAlgorithm, int keySize) throws InvalidAlgorithmParameterException {
        if (keymasterAlgorithm != 1) {
            if (keymasterAlgorithm != 3) {
                throw new ProviderException("Unsupported algorithm: " + keymasterAlgorithm);
            } else if (!SUPPORTED_EC_NIST_CURVE_SIZES.contains(Integer.valueOf(keySize))) {
                throw new InvalidAlgorithmParameterException("Unsupported EC key size: " + keySize + " bits. Supported: " + SUPPORTED_EC_NIST_CURVE_SIZES);
            }
        } else if (keySize < RSA_MIN_KEY_SIZE || keySize > RSA_MAX_KEY_SIZE) {
            throw new InvalidAlgorithmParameterException("RSA key size must be >= 512 and <= 4096");
        }
    }

    private static int getDefaultKeySize(int keymasterAlgorithm) {
        if (keymasterAlgorithm == 1) {
            return RSA_DEFAULT_KEY_SIZE;
        }
        if (keymasterAlgorithm == 3) {
            return EC_DEFAULT_KEY_SIZE;
        }
        throw new ProviderException("Unsupported algorithm: " + keymasterAlgorithm);
    }

    public KeyPair generateKeyPair() {
        if (this.mKeyStore == null || this.mSpec == null) {
            throw new IllegalStateException("Not initialized");
        }
        int flags = this.mEncryptionAtRestRequired;
        if ((flags & 1) == 0 || this.mKeyStore.state() == HwKeystoreManager.State.UNLOCKED) {
            byte[] additionalEntropy = getRandomBytesToMixIntoKeystoreRng(this.mRng, (this.mKeySizeBits + 7) / 8);
            HwCredentials.deleteAllTypesForAlias(this.mKeyStore, this.mEntryAlias, this.mEntryUid);
            String privateKeyAlias = HwCredentials.USER_PRIVATE_KEY + this.mEntryAlias;
            boolean success = false;
            try {
                generateKeystoreKeyPair(privateKeyAlias, constructKeyGenerationArguments(), additionalEntropy, (int) flags);
                KeyPair keyPair = loadKeystoreKeyPair(privateKeyAlias);
                byte[] certChainBytes = createCertificateChainBytes(privateKeyAlias, keyPair);
                if (certChainBytes == null) {
                    Log.e(TAG, "generateKeyPair failed, CertificateChain is null!");
                    success = false;
                    return null;
                }
                storeCertificateChainBytes(flags, certChainBytes);
                Log.i(TAG, "generateKeyPair successed");
                if (1 == 0) {
                    HwCredentials.deleteAllTypesForAlias(this.mKeyStore, this.mEntryAlias, this.mEntryUid);
                }
                return keyPair;
            } finally {
                if (!success) {
                    HwCredentials.deleteAllTypesForAlias(this.mKeyStore, this.mEntryAlias, this.mEntryUid);
                }
            }
        } else {
            throw new IllegalStateException("Encryption at rest using secure lock screen credential requested for key pair, but the user has not yet entered the credential");
        }
    }

    private void storeCertificateChainBytes(int flags, byte[] bytes) throws ProviderException {
        if (bytes != null) {
            HwKeymasterBlob blob = new HwKeymasterBlob(bytes);
            HwKeystoreManager hwKeystoreManager = this.mKeyStore;
            int insertErrorCode = hwKeystoreManager.set(HwCredentials.CERTIFICATE_CHAIN + this.mEntryAlias, blob, this.mEntryUid);
            if (insertErrorCode != 1) {
                throw new ProviderException("Failed to store attestation certificate chain", HwKeystoreManager.getKeyStoreException(insertErrorCode));
            }
            return;
        }
        throw new ProviderException("Input param is invalid.");
    }

    private void storeCertificateChain(int flags, Iterable<byte[]> iterable) throws ProviderException {
        if (iterable != null) {
            Iterator<byte[]> iter = iterable.iterator();
            storeCertificate(HwCredentials.USER_CERTIFICATE, iter.next(), flags, "Failed to store certificate");
            if (iter.hasNext()) {
                ByteArrayOutputStream certificateConcatenationStream = new ByteArrayOutputStream();
                while (iter.hasNext()) {
                    byte[] data = iter.next();
                    certificateConcatenationStream.write(data, 0, data.length);
                }
                storeCertificate(HwCredentials.CA_CERTIFICATE, certificateConcatenationStream.toByteArray(), flags, "Failed to store attestation CA certificate");
                return;
            }
            return;
        }
        throw new ProviderException("Input param is invalid.");
    }

    private void storeCertificate(String prefix, byte[] certificateBytes, int flags, String failureMessage) throws ProviderException {
        if (certificateBytes == null) {
            Log.e(TAG, "storeCertificate certificateBytes is null");
            return;
        }
        HwKeymasterBlob blob = new HwKeymasterBlob(certificateBytes);
        HwKeystoreManager hwKeystoreManager = this.mKeyStore;
        int insertErrorCode = hwKeystoreManager.set(prefix + this.mEntryAlias, blob, this.mEntryUid);
        if (insertErrorCode != 1) {
            throw new ProviderException(failureMessage, HwKeystoreManager.getKeyStoreException(insertErrorCode));
        }
    }

    private Iterable<byte[]> createCertificateChain(String privateKeyAlias, KeyPair keyPair) throws ProviderException {
        byte[] challenge = this.mSpec.getAttestationChallenge();
        if (challenge == null) {
            return Collections.singleton(generateSelfSignedCertificateBytes(keyPair));
        }
        HwKeymasterArguments args = new HwKeymasterArguments();
        args.addBytes(HwKeymasterDefs.KM_TAG_ATTESTATION_CHALLENGE, challenge);
        return getAttestationChain(privateKeyAlias, keyPair, args);
    }

    private byte[] createCertificateChainBytes(String privateKeyAlias, KeyPair keyPair) throws ProviderException {
        byte[] challenge = getChallenge(this.mSpec);
        if (challenge == null) {
            return generateSelfSignedCertificateBytes(keyPair);
        }
        HwKeymasterArguments args = new HwKeymasterArguments();
        args.addBytes(HwKeymasterDefs.KM_TAG_ATTESTATION_CHALLENGE, challenge);
        return getAttestationChainBytes(privateKeyAlias, keyPair, args);
    }

    private byte[] generateSelfSignedCertificateBytes(KeyPair keyPair) throws ProviderException {
        try {
            return generateSelfSignedCertificate(keyPair.getPrivate(), keyPair.getPublic()).getEncoded();
        } catch (IOException | CertificateParsingException e) {
            throw new ProviderException("Failed to generate self-signed certificate", e);
        } catch (CertificateEncodingException e2) {
            throw new ProviderException("Failed to obtain encoded form of self-signed certificate", e2);
        }
    }

    private X509Certificate generateSelfSignedCertificate(PrivateKey privateKey, PublicKey publicKey) throws CertificateParsingException, IOException {
        String signatureAlgorithm = getCertificateSignatureAlgorithm(this.mKeymasterAlgorithm, this.mKeySizeBits, this.mSpec);
        if (signatureAlgorithm == null) {
            return generateSelfSignedCertificateWithFakeSignature(publicKey);
        }
        try {
            return generateSelfSignedCertificateWithValidSignature(privateKey, publicKey, signatureAlgorithm);
        } catch (Exception e) {
            return generateSelfSignedCertificateWithFakeSignature(publicKey);
        }
    }

    private X509Certificate generateSelfSignedCertificateWithValidSignature(PrivateKey privateKey, PublicKey publicKey, String signatureAlgorithm) throws Exception {
        X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();
        certGen.setPublicKey(publicKey);
        certGen.setSerialNumber(this.mSpec.getCertificateSerialNumber());
        certGen.setSubjectDN(this.mSpec.getCertificateSubject());
        certGen.setIssuerDN(this.mSpec.getCertificateSubject());
        certGen.setNotBefore(this.mSpec.getCertificateNotBefore());
        certGen.setNotAfter(this.mSpec.getCertificateNotAfter());
        certGen.setSignatureAlgorithm(signatureAlgorithm);
        return certGen.generate(privateKey);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x00dd, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x00e1, code lost:
        if (r5 != null) goto L_0x00e3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:?, code lost:
        r4.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x00e7, code lost:
        r7 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x00e8, code lost:
        r5.addSuppressed(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x00ec, code lost:
        r4.close();
     */
    private X509Certificate generateSelfSignedCertificateWithFakeSignature(PublicKey publicKey) throws IOException, CertificateParsingException {
        byte[] signature;
        AlgorithmIdentifier sigAlgId;
        V3TBSCertificateGenerator tbsGenerator = new V3TBSCertificateGenerator();
        int i = this.mKeymasterAlgorithm;
        if (i == 1) {
            signature = new byte[1];
            sigAlgId = new AlgorithmIdentifier(PKCSObjectIdentifiers.sha256WithRSAEncryption, DERNull.INSTANCE);
        } else if (i == 3) {
            sigAlgId = new AlgorithmIdentifier(X9ObjectIdentifiers.ecdsa_with_SHA256);
            ASN1EncodableVector v = new ASN1EncodableVector();
            v.add(new DERInteger(0));
            v.add(new DERInteger(0));
            signature = new DERSequence().getEncoded();
        } else {
            throw new ProviderException("Unsupported key algorithm: " + this.mKeymasterAlgorithm);
        }
        byte[] signature2 = signature;
        ASN1InputStream publicKeyInfoIn = new ASN1InputStream(publicKey.getEncoded());
        tbsGenerator.setSubjectPublicKeyInfo(SubjectPublicKeyInfo.getInstance(publicKeyInfoIn.readObject()));
        publicKeyInfoIn.close();
        tbsGenerator.setSerialNumber(new ASN1Integer(this.mSpec.getCertificateSerialNumber()));
        X509Principal subject = new X509Principal(this.mSpec.getCertificateSubject().getEncoded());
        tbsGenerator.setSubject(subject);
        tbsGenerator.setIssuer(subject);
        tbsGenerator.setStartDate(new Time(this.mSpec.getCertificateNotBefore()));
        tbsGenerator.setEndDate(new Time(this.mSpec.getCertificateNotAfter()));
        tbsGenerator.setSignature(sigAlgId);
        TBSCertificate tbsCertificate = tbsGenerator.generateTBSCertificate();
        ASN1EncodableVector result = new ASN1EncodableVector();
        result.add(tbsCertificate);
        result.add(sigAlgId);
        result.add(new DERBitString(signature2));
        return new X509CertificateObject(Certificate.getInstance(new DERSequence(result)));
        throw result;
    }

    @Nullable
    private static String getCertificateSignatureAlgorithm(int keymasterAlgorithm, int keySizeBits, HwKeyGenParameterSpec spec) {
        if ((spec.getPurposes() & 4) == 0 || spec.isUserAuthenticationRequired() || !spec.isDigestsSpecified()) {
            return null;
        }
        if (keymasterAlgorithm != 1) {
            if (keymasterAlgorithm == 3) {
                int bestKeymasterDigest = -1;
                int bestDigestOutputSizeBits = -1;
                Iterator<Integer> it = getAvailableKeymasterSignatureDigests(spec.getDigests(), HwUniversalKeyStoreProvider.getSupportedEcdsaSignatureDigests()).iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    int keymasterDigest = it.next().intValue();
                    int outputSizeBits = HwKeymasterUtils.getDigestOutputSizeBits(keymasterDigest);
                    if (outputSizeBits == keySizeBits) {
                        bestKeymasterDigest = keymasterDigest;
                        int bestDigestOutputSizeBits2 = outputSizeBits;
                        break;
                    } else if (bestKeymasterDigest == -1) {
                        bestKeymasterDigest = keymasterDigest;
                        bestDigestOutputSizeBits = outputSizeBits;
                    } else if (bestDigestOutputSizeBits < keySizeBits) {
                        if (outputSizeBits > bestDigestOutputSizeBits) {
                            bestKeymasterDigest = keymasterDigest;
                            bestDigestOutputSizeBits = outputSizeBits;
                        }
                    } else if (outputSizeBits < bestDigestOutputSizeBits && outputSizeBits >= keySizeBits) {
                        bestKeymasterDigest = keymasterDigest;
                        bestDigestOutputSizeBits = outputSizeBits;
                    }
                }
                if (bestKeymasterDigest == -1) {
                    return null;
                }
                return HwKeyProperties.Digest.fromKeymasterToSignatureAlgorithmDigest(bestKeymasterDigest) + "WithECDSA";
            }
            throw new ProviderException("Unsupported algorithm: " + keymasterAlgorithm);
        } else if (!ArrayUtils.contains(HwKeyProperties.SignaturePadding.allToKeymaster(spec.getSignaturePaddings()), 5)) {
            return null;
        } else {
            int maxDigestOutputSizeBits = keySizeBits - 240;
            int bestKeymasterDigest2 = -1;
            int bestDigestOutputSizeBits3 = -1;
            for (Integer intValue : getAvailableKeymasterSignatureDigests(spec.getDigests(), HwUniversalKeyStoreProvider.getSupportedEcdsaSignatureDigests())) {
                int keymasterDigest2 = intValue.intValue();
                int outputSizeBits2 = HwKeymasterUtils.getDigestOutputSizeBits(keymasterDigest2);
                if (outputSizeBits2 <= maxDigestOutputSizeBits) {
                    if (bestKeymasterDigest2 == -1) {
                        bestKeymasterDigest2 = keymasterDigest2;
                        bestDigestOutputSizeBits3 = outputSizeBits2;
                    } else if (outputSizeBits2 > bestDigestOutputSizeBits3) {
                        bestKeymasterDigest2 = keymasterDigest2;
                        bestDigestOutputSizeBits3 = outputSizeBits2;
                    }
                }
            }
            if (bestKeymasterDigest2 == -1) {
                return null;
            }
            return HwKeyProperties.Digest.fromKeymasterToSignatureAlgorithmDigest(bestKeymasterDigest2) + "WithRSA";
        }
    }

    private static Set<Integer> getAvailableKeymasterSignatureDigests(String[] authorizedKeyDigests, String[] supportedSignatureDigests) {
        Set<Integer> authorizedKeymasterKeyDigests = new HashSet<>();
        for (int keymasterDigest : HwKeyProperties.Digest.allToKeymaster(authorizedKeyDigests)) {
            authorizedKeymasterKeyDigests.add(Integer.valueOf(keymasterDigest));
        }
        Set<Integer> supportedKeymasterSignatureDigests = new HashSet<>();
        for (int keymasterDigest2 : HwKeyProperties.Digest.allToKeymaster(supportedSignatureDigests)) {
            supportedKeymasterSignatureDigests.add(Integer.valueOf(keymasterDigest2));
        }
        Set<Integer> result = new HashSet<>(supportedKeymasterSignatureDigests);
        result.retainAll(authorizedKeymasterKeyDigests);
        return result;
    }

    private Iterable<byte[]> getAttestationChain(String privateKeyAlias, KeyPair keyPair, HwKeymasterArguments args) throws ProviderException {
        HwKeymasterCertificateChain outChain = new HwKeymasterCertificateChain();
        int errorCode = this.mKeyStore.attestKey(privateKeyAlias, this.mEntryUid, args, outChain);
        if (errorCode == 1) {
            Collection<byte[]> chain = outChain.getCertificates();
            if (chain.size() >= 2) {
                return chain;
            }
            throw new ProviderException("Attestation certificate chain contained " + chain.size() + " entries. At least two are required.");
        }
        throw new ProviderException("Failed to generate attestation certificate chain", HwKeystoreManager.getKeyStoreException(errorCode));
    }

    private byte[] getAttestationChainBytes(String privateKeyAlias, KeyPair keyPair, HwKeymasterArguments args) throws ProviderException {
        Iterator<byte[]> iter = getAttestationChain(privateKeyAlias, keyPair, args).iterator();
        ByteArrayOutputStream certificateConcatenationStream = new ByteArrayOutputStream();
        do {
            byte[] data = iter.next();
            certificateConcatenationStream.write(data, 0, data.length);
        } while (iter.hasNext());
        return certificateConcatenationStream.toByteArray();
    }

    private void generateKeystoreKeyPair(String privateKeyAlias, HwKeymasterArguments args, byte[] additionalEntropy, int flags) throws ProviderException {
        String str = privateKeyAlias;
        HwKeymasterArguments hwKeymasterArguments = args;
        byte[] bArr = additionalEntropy;
        int errorCode = this.mKeyStore.generateKey(str, hwKeymasterArguments, bArr, this.mEntryUid, flags, new HwKeyCharacteristics());
        if (errorCode != 1) {
            throw new ProviderException("Failed to generate key pair", HwKeystoreManager.getKeyStoreException(errorCode));
        }
    }

    /* access modifiers changed from: protected */
    public KeyPair loadKeystoreKeyPair(String privateKeyAlias) throws ProviderException {
        try {
            return HwUniversalKeyStoreProvider.loadAndroidKeyStoreKeyPairFromKeystore(this.mKeyStore, privateKeyAlias, this.mEntryUid);
        } catch (UnrecoverableKeyException e) {
            throw new ProviderException("Failed to load generated key pair from keystore", e);
        }
    }

    private HwKeymasterArguments constructKeyGenerationArguments() {
        HwKeymasterArguments args = new HwKeymasterArguments();
        args.addUnsignedInt(HwKeymasterDefs.KM_TAG_KEY_SIZE, (long) this.mKeySizeBits);
        args.addEnum(HwKeymasterDefs.KM_TAG_ALGORITHM, this.mKeymasterAlgorithm);
        args.addEnums(HwKeymasterDefs.KM_TAG_PURPOSE, this.mKeymasterPurposes);
        args.addEnums(HwKeymasterDefs.KM_TAG_BLOCK_MODE, this.mKeymasterBlockModes);
        args.addEnums(HwKeymasterDefs.KM_TAG_PADDING, this.mKeymasterEncryptionPaddings);
        args.addEnums(HwKeymasterDefs.KM_TAG_PADDING, this.mKeymasterSignaturePaddings);
        args.addEnums(HwKeymasterDefs.KM_TAG_DIGEST, this.mKeymasterDigests);
        if (this.mSpec.isAdditionalProtectionAllowed()) {
            args.addBoolean(HwKeymasterDefs.KM_TAG_ADDITIONAL_PROTECTION_ALLOWED);
        }
        if (this.mSpec.isInvalidatedBySystemRooting()) {
            args.addBoolean(HwKeymasterDefs.KM_TAG_INVALIDATED_BY_ROOTING);
        }
        HwKeymasterUtils.addUserAuthArgs(args, this.mSpec.isUserAuthenticationRequired(), this.mSpec.getUserAuthenticationValidityDurationSeconds(), this.mSpec.isUserAuthenticationValidWhileOnBody(), this.mSpec.isInvalidatedByBiometricEnrollment(), 0);
        args.addDateIfNotNull(HwKeymasterDefs.KM_TAG_ACTIVE_DATETIME, this.mSpec.getKeyValidityStart());
        args.addDateIfNotNull(HwKeymasterDefs.KM_TAG_ORIGINATION_EXPIRE_DATETIME, this.mSpec.getKeyValidityForOriginationEnd());
        args.addDateIfNotNull(HwKeymasterDefs.KM_TAG_USAGE_EXPIRE_DATETIME, this.mSpec.getKeyValidityForConsumptionEnd());
        addAlgorithmSpecificParameters(args);
        if (this.mSpec.isUniqueIdIncluded()) {
            args.addBoolean(HwKeymasterDefs.KM_TAG_INCLUDE_UNIQUE_ID);
        }
        return args;
    }

    private void addAlgorithmSpecificParameters(HwKeymasterArguments keymasterArgs) {
        int i = this.mKeymasterAlgorithm;
        if (i == 1) {
            keymasterArgs.addUnsignedLong(HwKeymasterDefs.KM_TAG_RSA_PUBLIC_EXPONENT, this.mRSAPublicExponent);
            addExtraParameters(keymasterArgs);
        } else if (i != 3) {
            throw new ProviderException("Unsupported algorithm: " + this.mKeymasterAlgorithm);
        }
    }

    private byte[] getRandomBytesToMixIntoKeystoreRng(SecureRandom rng, int sizeBytes) {
        if (sizeBytes <= 0) {
            return new byte[0];
        }
        if (rng == null) {
            rng = new SecureRandom();
        }
        byte[] result = new byte[sizeBytes];
        rng.nextBytes(result);
        return result;
    }

    /* access modifiers changed from: protected */
    public String getEntryAlias(String keystoreAlias) {
        return keystoreAlias;
    }

    /* access modifiers changed from: protected */
    public byte[] getChallenge(HwKeyGenParameterSpec mSpec2) {
        return mSpec2.getAttestationChallenge();
    }

    /* access modifiers changed from: protected */
    public void addExtraParameters(HwKeymasterArguments keymasterArgs) {
    }

    /* access modifiers changed from: protected */
    public HwKeystoreManager getKeyStoreManager() {
        return this.mKeyStore;
    }

    /* access modifiers changed from: protected */
    public int getEntryUid() {
        return this.mEntryUid;
    }

    /* access modifiers changed from: protected */
    public void resetAll() {
        this.mEntryAlias = null;
        this.mEntryUid = -1;
        this.mKeymasterAlgorithm = -1;
        this.mKeymasterPurposes = null;
        this.mKeymasterBlockModes = null;
        this.mKeymasterEncryptionPaddings = null;
        this.mKeymasterSignaturePaddings = null;
        this.mKeymasterDigests = null;
        this.mKeySizeBits = 0;
        this.mSpec = null;
        this.mRSAPublicExponent = null;
        this.mEncryptionAtRestRequired = false;
        this.mRng = null;
        this.mKeyStore = null;
    }
}
