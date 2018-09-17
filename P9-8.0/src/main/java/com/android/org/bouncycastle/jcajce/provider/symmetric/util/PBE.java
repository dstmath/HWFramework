package com.android.org.bouncycastle.jcajce.provider.symmetric.util;

import com.android.org.bouncycastle.crypto.CipherParameters;
import com.android.org.bouncycastle.crypto.PBEParametersGenerator;
import com.android.org.bouncycastle.crypto.digests.AndroidDigestFactory;
import com.android.org.bouncycastle.crypto.generators.OpenSSLPBEParametersGenerator;
import com.android.org.bouncycastle.crypto.generators.PKCS12ParametersGenerator;
import com.android.org.bouncycastle.crypto.generators.PKCS5S1ParametersGenerator;
import com.android.org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import com.android.org.bouncycastle.crypto.params.DESParameters;
import com.android.org.bouncycastle.crypto.params.KeyParameter;
import com.android.org.bouncycastle.crypto.params.ParametersWithIV;
import java.security.InvalidAlgorithmParameterException;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

public interface PBE {
    public static final int MD5 = 0;
    public static final int OPENSSL = 3;
    public static final int PKCS12 = 2;
    public static final int PKCS5S1 = 0;
    public static final int PKCS5S1_UTF8 = 4;
    public static final int PKCS5S2 = 1;
    public static final int PKCS5S2_UTF8 = 5;
    public static final int SHA1 = 1;
    public static final int SHA224 = 7;
    public static final int SHA256 = 4;
    public static final int SHA384 = 8;
    public static final int SHA512 = 9;

    public static class Util {
        private static PBEParametersGenerator makePBEGenerator(int type, int hash) {
            if (type == 0 || type == 4) {
                switch (hash) {
                    case 0:
                        return new PKCS5S1ParametersGenerator(AndroidDigestFactory.getMD5());
                    case 1:
                        return new PKCS5S1ParametersGenerator(AndroidDigestFactory.getSHA1());
                    default:
                        throw new IllegalStateException("PKCS5 scheme 1 only supports MD2, MD5 and SHA1.");
                }
            } else if (type == 1 || type == 5) {
                switch (hash) {
                    case 0:
                        return new PKCS5S2ParametersGenerator(AndroidDigestFactory.getMD5());
                    case 1:
                        return new PKCS5S2ParametersGenerator(AndroidDigestFactory.getSHA1());
                    case 4:
                        return new PKCS5S2ParametersGenerator(AndroidDigestFactory.getSHA256());
                    case 7:
                        return new PKCS5S2ParametersGenerator(AndroidDigestFactory.getSHA224());
                    case 8:
                        return new PKCS5S2ParametersGenerator(AndroidDigestFactory.getSHA384());
                    case 9:
                        return new PKCS5S2ParametersGenerator(AndroidDigestFactory.getSHA512());
                    default:
                        throw new IllegalStateException("unknown digest scheme for PBE PKCS5S2 encryption.");
                }
            } else if (type != 2) {
                return new OpenSSLPBEParametersGenerator();
            } else {
                switch (hash) {
                    case 0:
                        return new PKCS12ParametersGenerator(AndroidDigestFactory.getMD5());
                    case 1:
                        return new PKCS12ParametersGenerator(AndroidDigestFactory.getSHA1());
                    case 4:
                        return new PKCS12ParametersGenerator(AndroidDigestFactory.getSHA256());
                    case 7:
                        return new PKCS12ParametersGenerator(AndroidDigestFactory.getSHA224());
                    case 8:
                        return new PKCS12ParametersGenerator(AndroidDigestFactory.getSHA384());
                    case 9:
                        return new PKCS12ParametersGenerator(AndroidDigestFactory.getSHA512());
                    default:
                        throw new IllegalStateException("unknown digest scheme for PBE encryption.");
                }
            }
        }

        public static CipherParameters makePBEParameters(byte[] pbeKey, int scheme, int digest, int keySize, int ivSize, AlgorithmParameterSpec spec, String targetAlgorithm) throws InvalidAlgorithmParameterException {
            if (spec == null || ((spec instanceof PBEParameterSpec) ^ 1) != 0) {
                throw new InvalidAlgorithmParameterException("Need a PBEParameter spec with a PBE key.");
            }
            CipherParameters param;
            PBEParameterSpec pbeParam = (PBEParameterSpec) spec;
            PBEParametersGenerator generator = makePBEGenerator(scheme, digest);
            byte[] key = pbeKey;
            generator.init(pbeKey, pbeParam.getSalt(), pbeParam.getIterationCount());
            if (ivSize != 0) {
                param = generator.generateDerivedParameters(keySize, ivSize);
                AlgorithmParameterSpec parameterSpecFromPBEParameterSpec = getParameterSpecFromPBEParameterSpec(pbeParam);
                if ((scheme == 1 || scheme == 5) && (parameterSpecFromPBEParameterSpec instanceof IvParameterSpec)) {
                    param = new ParametersWithIV((KeyParameter) ((ParametersWithIV) param).getParameters(), ((IvParameterSpec) parameterSpecFromPBEParameterSpec).getIV());
                }
            } else {
                param = generator.generateDerivedParameters(keySize);
            }
            if (targetAlgorithm.startsWith("DES")) {
                if (param instanceof ParametersWithIV) {
                    DESParameters.setOddParity(((KeyParameter) ((ParametersWithIV) param).getParameters()).getKey());
                } else {
                    DESParameters.setOddParity(((KeyParameter) param).getKey());
                }
            }
            return param;
        }

        public static CipherParameters makePBEParameters(BCPBEKey pbeKey, AlgorithmParameterSpec spec, String targetAlgorithm) {
            if (spec == null || ((spec instanceof PBEParameterSpec) ^ 1) != 0) {
                throw new IllegalArgumentException("Need a PBEParameter spec with a PBE key.");
            }
            CipherParameters param;
            PBEParameterSpec pbeParam = (PBEParameterSpec) spec;
            PBEParametersGenerator generator = makePBEGenerator(pbeKey.getType(), pbeKey.getDigest());
            byte[] key = pbeKey.getEncoded();
            if (pbeKey.shouldTryWrongPKCS12()) {
                key = new byte[2];
            }
            generator.init(key, pbeParam.getSalt(), pbeParam.getIterationCount());
            if (pbeKey.getIvSize() != 0) {
                param = generator.generateDerivedParameters(pbeKey.getKeySize(), pbeKey.getIvSize());
                AlgorithmParameterSpec parameterSpecFromPBEParameterSpec = getParameterSpecFromPBEParameterSpec(pbeParam);
                if ((pbeKey.getType() == 1 || pbeKey.getType() == 5) && (parameterSpecFromPBEParameterSpec instanceof IvParameterSpec)) {
                    param = new ParametersWithIV((KeyParameter) ((ParametersWithIV) param).getParameters(), ((IvParameterSpec) parameterSpecFromPBEParameterSpec).getIV());
                }
            } else {
                param = generator.generateDerivedParameters(pbeKey.getKeySize());
            }
            if (targetAlgorithm.startsWith("DES")) {
                if (param instanceof ParametersWithIV) {
                    DESParameters.setOddParity(((KeyParameter) ((ParametersWithIV) param).getParameters()).getKey());
                } else {
                    DESParameters.setOddParity(((KeyParameter) param).getKey());
                }
            }
            return param;
        }

        public static CipherParameters makePBEMacParameters(BCPBEKey pbeKey, AlgorithmParameterSpec spec) {
            if (spec == null || ((spec instanceof PBEParameterSpec) ^ 1) != 0) {
                throw new IllegalArgumentException("Need a PBEParameter spec with a PBE key.");
            }
            PBEParameterSpec pbeParam = (PBEParameterSpec) spec;
            PBEParametersGenerator generator = makePBEGenerator(pbeKey.getType(), pbeKey.getDigest());
            generator.init(pbeKey.getEncoded(), pbeParam.getSalt(), pbeParam.getIterationCount());
            return generator.generateDerivedMacParameters(pbeKey.getKeySize());
        }

        public static CipherParameters makePBEMacParameters(PBEKeySpec keySpec, int type, int hash, int keySize) {
            PBEParametersGenerator generator = makePBEGenerator(type, hash);
            byte[] key = convertPassword(type, keySpec);
            generator.init(key, keySpec.getSalt(), keySpec.getIterationCount());
            CipherParameters param = generator.generateDerivedMacParameters(keySize);
            for (int i = 0; i != key.length; i++) {
                key[i] = (byte) 0;
            }
            return param;
        }

        public static CipherParameters makePBEParameters(PBEKeySpec keySpec, int type, int hash, int keySize, int ivSize) {
            CipherParameters param;
            PBEParametersGenerator generator = makePBEGenerator(type, hash);
            byte[] key = convertPassword(type, keySpec);
            generator.init(key, keySpec.getSalt(), keySpec.getIterationCount());
            if (ivSize != 0) {
                param = generator.generateDerivedParameters(keySize, ivSize);
            } else {
                param = generator.generateDerivedParameters(keySize);
            }
            for (int i = 0; i != key.length; i++) {
                key[i] = (byte) 0;
            }
            return param;
        }

        public static CipherParameters makePBEMacParameters(SecretKey key, int type, int hash, int keySize, PBEParameterSpec pbeSpec) {
            PBEParametersGenerator generator = makePBEGenerator(type, hash);
            byte[] keyBytes = key.getEncoded();
            generator.init(key.getEncoded(), pbeSpec.getSalt(), pbeSpec.getIterationCount());
            CipherParameters param = generator.generateDerivedMacParameters(keySize);
            for (int i = 0; i != keyBytes.length; i++) {
                keyBytes[i] = (byte) 0;
            }
            return param;
        }

        public static AlgorithmParameterSpec getParameterSpecFromPBEParameterSpec(PBEParameterSpec pbeParameterSpec) {
            try {
                return (AlgorithmParameterSpec) PBE.class.getClassLoader().loadClass("javax.crypto.spec.PBEParameterSpec").getMethod("getParameterSpec", new Class[0]).invoke(pbeParameterSpec, new Object[0]);
            } catch (Exception e) {
                return null;
            }
        }

        private static byte[] convertPassword(int type, PBEKeySpec keySpec) {
            if (type == 2) {
                return PBEParametersGenerator.PKCS12PasswordToBytes(keySpec.getPassword());
            }
            if (type == 5 || type == 4) {
                return PBEParametersGenerator.PKCS5PasswordToUTF8Bytes(keySpec.getPassword());
            }
            return PBEParametersGenerator.PKCS5PasswordToBytes(keySpec.getPassword());
        }
    }
}
