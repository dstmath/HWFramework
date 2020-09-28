package com.huawei.security.keystore;

import android.security.keystore.KeyGenParameterSpec;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import com.huawei.internal.util.ArrayUtilsEx;
import com.huawei.security.HwCredentials;
import com.huawei.security.HwKeystoreManager;
import com.huawei.security.keymaster.HwKeyCharacteristics;
import com.huawei.security.keymaster.HwKeymasterArguments;
import com.huawei.security.keymaster.HwKeymasterBlob;
import com.huawei.security.keymaster.HwKeymasterCertificateChain;
import com.huawei.security.keymaster.HwKeymasterDefs;
import com.huawei.security.keymaster.HwKeymasterUtils;
import com.huawei.security.keystore.HwKeyGenParameterSpec;
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
    private static final int NUM_OF_EC_NAME = 10;
    private static final int RSA_DEFAULT_KEY_SIZE = 2048;
    private static final int RSA_MAX_KEY_SIZE = 4096;
    private static final int RSA_MIN_KEY_SIZE = 512;
    private static final List<String> SUPPORTED_EC_NIST_CURVE_NAMES = new ArrayList(10);
    private static final Map<String, Integer> SUPPORTED_EC_NIST_CURVE_NAME_TO_SIZE = new HashMap(10);
    private static final List<Integer> SUPPORTED_EC_NIST_CURVE_SIZES = new ArrayList(10);
    private static final String TAG = "HwKeyPairGenerator";
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

    static {
        SUPPORTED_EC_NIST_CURVE_NAME_TO_SIZE.put("p-224", 224);
        SUPPORTED_EC_NIST_CURVE_NAME_TO_SIZE.put("secp224r1", 224);
        SUPPORTED_EC_NIST_CURVE_NAME_TO_SIZE.put("p-256", 256);
        SUPPORTED_EC_NIST_CURVE_NAME_TO_SIZE.put("secp256r1", 256);
        SUPPORTED_EC_NIST_CURVE_NAME_TO_SIZE.put("prime256v1", 256);
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

    private static void checkValidKeySize(int keymasterAlgorithm, int keySize) throws InvalidAlgorithmParameterException {
        if (keymasterAlgorithm != 1) {
            if (keymasterAlgorithm != 3) {
                throw new ProviderException("Unsupported algorithm: " + keymasterAlgorithm);
            } else if (!SUPPORTED_EC_NIST_CURVE_SIZES.contains(Integer.valueOf(keySize))) {
                throw new InvalidAlgorithmParameterException("Unsupported EC key size: " + keySize + " bits. Supported: " + SUPPORTED_EC_NIST_CURVE_SIZES);
            }
        } else if (keySize < 512 || keySize > 4096) {
            throw new InvalidAlgorithmParameterException("RSA key size must be >= 512 and <= 4096");
        }
    }

    private static int getDefaultKeySize(int keymasterAlgorithm) {
        if (keymasterAlgorithm == 1) {
            return 2048;
        }
        if (keymasterAlgorithm == 3) {
            return 256;
        }
        throw new ProviderException("Unsupported algorithm: " + keymasterAlgorithm);
    }

    @Nullable
    private static String getCertificateSignatureAlgorithm(int keymasterAlgorithm, int keySizeBits, HwKeyGenParameterSpec spec) {
        int bestKeymasterDigest;
        if ((spec.getPurposes() & 4) == 0 || spec.isUserAuthenticationRequired() || !spec.isDigestsSpecified()) {
            return null;
        }
        if (keymasterAlgorithm != 1) {
            if (keymasterAlgorithm == 3) {
                int bestKeymasterDigest2 = getBestKeymasterDigestEc(getAvailableKeymasterSignatureDigests(spec.getDigests(), HwUniversalKeyStoreProvider.getSupportedEcdsaSignatureDigests()), keySizeBits);
                if (bestKeymasterDigest2 == -1) {
                    return null;
                }
                return HwKeyProperties.Digest.fromKeymasterToSignatureAlgorithmDigest(bestKeymasterDigest2) + "WithECDSA";
            }
            throw new ProviderException("Unsupported algorithm: " + keymasterAlgorithm);
        } else if (!ArrayUtilsEx.contains(HwKeyProperties.SignaturePadding.allToKeymaster(spec.getSignaturePaddings()), 5) || (bestKeymasterDigest = getBestKeymasterDigestRsa(getAvailableKeymasterSignatureDigests(spec.getDigests(), HwUniversalKeyStoreProvider.getSupportedEcdsaSignatureDigests()), keySizeBits)) == -1) {
            return null;
        } else {
            return HwKeyProperties.Digest.fromKeymasterToSignatureAlgorithmDigest(bestKeymasterDigest) + "WithRSA";
        }
    }

    private static int getBestKeymasterDigestRsa(Set<Integer> availableKeymasterDigests, int keySizeBits) {
        int maxDigestOutputSizeBits = keySizeBits - 240;
        int bestKeymasterDigest = -1;
        int bestDigestOutputSizeBits = -1;
        for (Integer num : availableKeymasterDigests) {
            int keymasterDigest = num.intValue();
            int outputSizeBits = HwKeymasterUtils.getDigestOutputSizeBits(keymasterDigest);
            if (outputSizeBits <= maxDigestOutputSizeBits) {
                if (bestKeymasterDigest == -1) {
                    bestKeymasterDigest = keymasterDigest;
                    bestDigestOutputSizeBits = outputSizeBits;
                } else if (outputSizeBits > bestDigestOutputSizeBits) {
                    bestKeymasterDigest = keymasterDigest;
                    bestDigestOutputSizeBits = outputSizeBits;
                }
            }
        }
        return bestKeymasterDigest;
    }

    private static int getBestKeymasterDigestEc(Set<Integer> availableKeymasterDigests, int keySizeBits) {
        int bestKeymasterDigest = -1;
        int bestDigestOutputSizeBits = -1;
        for (Integer num : availableKeymasterDigests) {
            int keymasterDigest = num.intValue();
            int outputSizeBits = HwKeymasterUtils.getDigestOutputSizeBits(keymasterDigest);
            if (outputSizeBits == keySizeBits) {
                return keymasterDigest;
            }
            if (bestKeymasterDigest == -1) {
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
        return bestKeymasterDigest;
    }

    private static Set<Integer> getAvailableKeymasterSignatureDigests(String[] authorizedKeyDigests, String[] supportedSignatureDigests) {
        int[] inputAuthorizedKeyDigests = HwKeyProperties.Digest.allToKeymaster(authorizedKeyDigests);
        Set<Integer> authorizedKeymasterKeyDigests = new HashSet<>(inputAuthorizedKeyDigests.length);
        for (int keymasterDigest : inputAuthorizedKeyDigests) {
            authorizedKeymasterKeyDigests.add(Integer.valueOf(keymasterDigest));
        }
        int[] inputSupportedSignatureDigests = HwKeyProperties.Digest.allToKeymaster(supportedSignatureDigests);
        Set<Integer> supportedKeymasterSignatureDigests = new HashSet<>(inputSupportedSignatureDigests.length);
        for (int keymasterDigest2 : inputSupportedSignatureDigests) {
            supportedKeymasterSignatureDigests.add(Integer.valueOf(keymasterDigest2));
        }
        Set<Integer> result = new HashSet<>(supportedKeymasterSignatureDigests);
        result.retainAll(authorizedKeymasterKeyDigests);
        return result;
    }

    @Override // java.security.KeyPairGeneratorSpi
    public void initialize(int keysize, SecureRandom random) {
        throw new IllegalArgumentException(HwKeyGenParameterSpec.class.getName() + " required to initialize this HwKeyPairGenerator");
    }

    @Override // java.security.KeyPairGeneratorSpi
    public void initialize(AlgorithmParameterSpec params, SecureRandom random) throws InvalidAlgorithmParameterException {
        resetAll();
        Log.i(TAG, "HwUniversalKeyStoreKeyPairGeneratorSpi initialize");
        if (params != null) {
            HwKeyGenParameterSpec spec = convertToHwKeyGenParamSpec(params);
            int keymasterAlgorithm = this.mOriginalKeymasterAlgorithm;
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
            try {
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

    /* JADX INFO: Multiple debug info for r0v4 com.huawei.security.keystore.HwKeyGenParameterSpec: [D('spec' com.huawei.security.keystore.HwKeyGenParameterSpec), D('tmpBuilder' com.huawei.security.keystore.HwKeyGenParameterSpec$Builder)] */
    @NonNull
    private HwKeyGenParameterSpec convertToHwKeyGenParamSpec(AlgorithmParameterSpec params) throws InvalidAlgorithmParameterException {
        if (params instanceof HwKeyGenParameterSpec) {
            return (HwKeyGenParameterSpec) params;
        }
        if (params instanceof KeyGenParameterSpec) {
            return HwKeyGenParameterSpec.getInstance(new HwKeyGenParameterSpec.Builder((KeyGenParameterSpec) params));
        }
        throw new InvalidAlgorithmParameterException("Unsupported params class: " + params.getClass().getName() + ". Supported: " + HwKeyGenParameterSpec.class.getName());
    }

    private void initAlgorithmSpecificParameters() throws InvalidAlgorithmParameterException {
        AlgorithmParameterSpec algSpecificSpec = this.mSpec.getAlgorithmParameterSpec();
        int i = this.mKeymasterAlgorithm;
        if (i == 1) {
            BigInteger publicExponent = null;
            if (algSpecificSpec instanceof RSAKeyGenParameterSpec) {
                RSAKeyGenParameterSpec rsaSpec = (RSAKeyGenParameterSpec) algSpecificSpec;
                initKeySizeBitsFromRsaParam(algSpecificSpec, rsaSpec);
                publicExponent = rsaSpec.getPublicExponent();
            } else if (algSpecificSpec != null) {
                throw new InvalidAlgorithmParameterException("RSA may only use RSAKeyGenParameterSpec");
            }
            this.mRSAPublicExponent = enforceGetValidPublicExponent(publicExponent);
        } else if (i != 3) {
            throw new ProviderException("Unsupported algorithm: " + this.mKeymasterAlgorithm);
        } else if (algSpecificSpec instanceof ECGenParameterSpec) {
            initKeySizeBitsFromEcParam((ECGenParameterSpec) algSpecificSpec, algSpecificSpec);
        } else if (algSpecificSpec != null) {
            throw new InvalidAlgorithmParameterException("EC may only use ECGenParameterSpec");
        }
    }

    @NonNull
    private BigInteger enforceGetValidPublicExponent(BigInteger publicExponent) throws InvalidAlgorithmParameterException {
        if (publicExponent == null) {
            publicExponent = RSAKeyGenParameterSpec.F4;
        }
        if (publicExponent.compareTo(BigInteger.ZERO) < 1) {
            throw new InvalidAlgorithmParameterException("RSA public exponent must be positive: " + publicExponent);
        } else if (publicExponent.compareTo(HwKeymasterArguments.UINT64_MAX_VALUE) <= 0) {
            return publicExponent;
        } else {
            throw new InvalidAlgorithmParameterException("Unsupported RSA public exponent: " + publicExponent + ". Maximum supported value: " + HwKeymasterArguments.UINT64_MAX_VALUE);
        }
    }

    private void initKeySizeBitsFromRsaParam(AlgorithmParameterSpec algSpecificSpec, RSAKeyGenParameterSpec rsaSpec) throws InvalidAlgorithmParameterException {
        int i = this.mKeySizeBits;
        if (i == -1) {
            this.mKeySizeBits = rsaSpec.getKeysize();
        } else if (i != rsaSpec.getKeysize()) {
            throw new InvalidAlgorithmParameterException("RSA key size must match  between " + this.mSpec + " and " + algSpecificSpec + ": " + this.mKeySizeBits + " vs " + rsaSpec.getKeysize());
        }
    }

    private void initKeySizeBitsFromEcParam(ECGenParameterSpec ecSpec, AlgorithmParameterSpec algSpecificSpec) throws InvalidAlgorithmParameterException {
        String curveName = ecSpec.getName();
        Integer ecSpecKeySizeBits = SUPPORTED_EC_NIST_CURVE_NAME_TO_SIZE.get(curveName.toLowerCase(Locale.US));
        if (ecSpecKeySizeBits != null) {
            int i = this.mKeySizeBits;
            if (i == -1) {
                this.mKeySizeBits = ecSpecKeySizeBits.intValue();
            } else if (i != ecSpecKeySizeBits.intValue()) {
                throw new InvalidAlgorithmParameterException("EC key size must match  between " + this.mSpec + " and " + algSpecificSpec + ": " + this.mKeySizeBits + " vs " + ecSpecKeySizeBits);
            }
        } else {
            throw new InvalidAlgorithmParameterException("Unsupported EC curve name: " + curveName + ". Supported: " + SUPPORTED_EC_NIST_CURVE_NAMES);
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r10v0, resolved type: com.huawei.security.keystore.HwUniversalKeyStoreKeyPairGeneratorSpi */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r1v2, types: [boolean, int] */
    public KeyPair generateKeyPair() {
        HwKeystoreManager hwKeystoreManager = this.mKeyStore;
        if (hwKeystoreManager == null || this.mSpec == null) {
            throw new IllegalStateException("Not initialized");
        }
        ?? r1 = this.mEncryptionAtRestRequired;
        if (((r1 == true ? 1 : 0) & 1) == 0 || hwKeystoreManager.state() == HwKeystoreManager.State.UNLOCKED) {
            byte[] additionalEntropy = getRandomBytesToMixIntoKeystoreRng(this.mRng, (this.mKeySizeBits + 7) / 8);
            HwCredentials.deleteAllTypesForAlias(this.mKeyStore, this.mEntryAlias, this.mEntryUid);
            String privateKeyAlias = HwCredentials.USER_PRIVATE_KEY + this.mEntryAlias;
            boolean success = false;
            try {
                generateKeystoreKeyPair(privateKeyAlias, constructKeyGenerationArguments(), additionalEntropy, r1 == true ? 1 : 0);
                KeyPair keyPair = loadKeystoreKeyPair(privateKeyAlias);
                byte[] certChainBytes = createCertificateChainBytes(privateKeyAlias, keyPair);
                if (certChainBytes == null) {
                    Log.e(TAG, "generateKeyPair failed, CertificateChain is null!");
                    boolean success2 = false;
                    return null;
                }
                storeCertificateChainBytes(r1, certChainBytes);
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

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0044, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:?, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0049, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x004a, code lost:
        r3.addSuppressed(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x004d, code lost:
        throw r4;
     */
    private void storeCertificateChain(int flags, Iterable<byte[]> iterable) throws ProviderException {
        if (iterable != null) {
            Iterator<byte[]> iterator = iterable.iterator();
            storeCertificate(HwCredentials.USER_CERTIFICATE, iterator.next(), flags, "Failed to store certificate");
            if (iterator.hasNext()) {
                ByteArrayOutputStream certificateConcatenationStream = new ByteArrayOutputStream();
                while (iterator.hasNext()) {
                    byte[] data = iterator.next();
                    certificateConcatenationStream.write(data, 0, data.length);
                }
                storeCertificate(HwCredentials.CA_CERTIFICATE, certificateConcatenationStream.toByteArray(), flags, "Failed to store attestation CA certificate");
                try {
                    certificateConcatenationStream.close();
                } catch (IOException | ProviderException e) {
                    throw new ProviderException("Failed to store attestation CA certificate", e);
                }
            }
        } else {
            throw new ProviderException("Input param is invalid.");
        }
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
        } catch (RuntimeException | CertificateEncodingException e) {
            return generateSelfSignedCertificateWithFakeSignature(publicKey);
        }
    }

    private X509Certificate generateSelfSignedCertificateWithValidSignature(PrivateKey privateKey, PublicKey publicKey, String signatureAlgorithm) throws CertificateEncodingException {
        try {
            return new HwUniversalKeyStoreCertificateGenerator(this.mSpec.getCertificateSubject(), this.mSpec.getCertificateSerialNumber(), this.mSpec.getCertificateNotBefore(), this.mSpec.getCertificateNotAfter()).generateCertificateWithValidSignature(privateKey, publicKey, signatureAlgorithm);
        } catch (Exception e) {
            throw new CertificateEncodingException("generateCertificateWithValidSignature failed!");
        }
    }

    private X509Certificate generateSelfSignedCertificateWithFakeSignature(PublicKey publicKey) throws IOException, CertificateParsingException {
        return new HwUniversalKeyStoreCertificateGenerator(this.mSpec.getCertificateSubject(), this.mSpec.getCertificateSerialNumber(), this.mSpec.getCertificateNotBefore(), this.mSpec.getCertificateNotAfter()).generateCertificateWithFakeSignature(publicKey, this.mKeymasterAlgorithm);
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

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0028, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:?, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x002d, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x002e, code lost:
        r3.addSuppressed(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0031, code lost:
        throw r4;
     */
    private byte[] getAttestationChainBytes(String privateKeyAlias, KeyPair keyPair, HwKeymasterArguments args) throws ProviderException {
        Iterator<byte[]> iterator = getAttestationChain(privateKeyAlias, keyPair, args).iterator();
        try {
            ByteArrayOutputStream certificateConcatenationStream = new ByteArrayOutputStream();
            do {
                byte[] data = iterator.next();
                certificateConcatenationStream.write(data, 0, data.length);
            } while (iterator.hasNext());
            byte[] byteArray = certificateConcatenationStream.toByteArray();
            certificateConcatenationStream.close();
            return byteArray;
        } catch (IOException e) {
            throw new ProviderException("Failed to get Attestation Chain Bytes", e);
        }
    }

    private void generateKeystoreKeyPair(String privateKeyAlias, HwKeymasterArguments args, byte[] additionalEntropy, int flags) throws ProviderException {
        int errorCode = this.mKeyStore.generateKey(privateKeyAlias, args, additionalEntropy, this.mEntryUid, flags, new HwKeyCharacteristics());
        if (errorCode != 1) {
            throw new ProviderException("Failed to generate key pair", HwKeystoreManager.getKeyStoreException(errorCode));
        }
    }

    /* access modifiers changed from: protected */
    public KeyPair loadKeystoreKeyPair(String privateKeyAlias) throws ProviderException {
        try {
            return HwUniversalKeyStoreProvider.loadHwKeyStoreKeyPairFromKeystore(this.mKeyStore, privateKeyAlias, this.mEntryUid);
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
        HwKeymasterUtils.addUserAuthArgs(args, this.mSpec, 0);
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
    public byte[] getChallenge(HwKeyGenParameterSpec Spec) {
        return Spec.getAttestationChallenge();
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

    public static class RSA extends HwUniversalKeyStoreKeyPairGeneratorSpi {
        public RSA() {
            super(1);
        }
    }

    public static class EC extends HwUniversalKeyStoreKeyPairGeneratorSpi {
        public EC() {
            super(3);
        }
    }
}
