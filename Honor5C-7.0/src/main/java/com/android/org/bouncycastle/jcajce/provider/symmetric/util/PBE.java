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
    public static final int SHA256 = 4;

    public static class Util {
        private static PBEParametersGenerator makePBEGenerator(int type, int hash) {
            if (type == 0 || type == PBE.SHA256) {
                switch (hash) {
                    case PBE.PKCS5S1 /*0*/:
                        return new PKCS5S1ParametersGenerator(AndroidDigestFactory.getMD5());
                    case PBE.SHA1 /*1*/:
                        return new PKCS5S1ParametersGenerator(AndroidDigestFactory.getSHA1());
                    default:
                        throw new IllegalStateException("PKCS5 scheme 1 only supports MD2, MD5 and SHA1.");
                }
            } else if (type == PBE.SHA1 || type == PBE.PKCS5S2_UTF8) {
                switch (hash) {
                    case PBE.PKCS5S1 /*0*/:
                        return new PKCS5S2ParametersGenerator(AndroidDigestFactory.getMD5());
                    case PBE.SHA1 /*1*/:
                        return new PKCS5S2ParametersGenerator(AndroidDigestFactory.getSHA1());
                    case PBE.SHA256 /*4*/:
                        return new PKCS5S2ParametersGenerator(AndroidDigestFactory.getSHA256());
                    default:
                        throw new IllegalStateException("unknown digest scheme for PBE PKCS5S2 encryption.");
                }
            } else if (type != PBE.PKCS12) {
                return new OpenSSLPBEParametersGenerator();
            } else {
                switch (hash) {
                    case PBE.PKCS5S1 /*0*/:
                        return new PKCS12ParametersGenerator(AndroidDigestFactory.getMD5());
                    case PBE.SHA1 /*1*/:
                        return new PKCS12ParametersGenerator(AndroidDigestFactory.getSHA1());
                    case PBE.SHA256 /*4*/:
                        return new PKCS12ParametersGenerator(AndroidDigestFactory.getSHA256());
                    default:
                        throw new IllegalStateException("unknown digest scheme for PBE encryption.");
                }
            }
        }

        public static CipherParameters makePBEParameters(byte[] pbeKey, int scheme, int digest, int keySize, int ivSize, AlgorithmParameterSpec spec, String targetAlgorithm) throws InvalidAlgorithmParameterException {
            if (spec == null || !(spec instanceof PBEParameterSpec)) {
                throw new InvalidAlgorithmParameterException("Need a PBEParameter spec with a PBE key.");
            }
            CipherParameters param;
            PBEParameterSpec pbeParam = (PBEParameterSpec) spec;
            PBEParametersGenerator generator = makePBEGenerator(scheme, digest);
            byte[] key = pbeKey;
            generator.init(pbeKey, pbeParam.getSalt(), pbeParam.getIterationCount());
            if (ivSize != 0) {
                param = generator.generateDerivedParameters(keySize, ivSize);
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
            for (int i = PBE.PKCS5S1; i != pbeKey.length; i += PBE.SHA1) {
                pbeKey[i] = (byte) 0;
            }
            return param;
        }

        public static CipherParameters makePBEParameters(BCPBEKey pbeKey, AlgorithmParameterSpec spec, String targetAlgorithm) {
            if (spec == null || !(spec instanceof PBEParameterSpec)) {
                throw new IllegalArgumentException("Need a PBEParameter spec with a PBE key.");
            }
            CipherParameters param;
            PBEParameterSpec pbeParam = (PBEParameterSpec) spec;
            PBEParametersGenerator generator = makePBEGenerator(pbeKey.getType(), pbeKey.getDigest());
            byte[] key = pbeKey.getEncoded();
            if (pbeKey.shouldTryWrongPKCS12()) {
                key = new byte[PBE.PKCS12];
            }
            generator.init(key, pbeParam.getSalt(), pbeParam.getIterationCount());
            if (pbeKey.getIvSize() != 0) {
                param = generator.generateDerivedParameters(pbeKey.getKeySize(), pbeKey.getIvSize());
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
            for (int i = PBE.PKCS5S1; i != key.length; i += PBE.SHA1) {
                key[i] = (byte) 0;
            }
            return param;
        }

        public static CipherParameters makePBEMacParameters(BCPBEKey pbeKey, AlgorithmParameterSpec spec) {
            if (spec == null || !(spec instanceof PBEParameterSpec)) {
                throw new IllegalArgumentException("Need a PBEParameter spec with a PBE key.");
            }
            PBEParameterSpec pbeParam = (PBEParameterSpec) spec;
            PBEParametersGenerator generator = makePBEGenerator(pbeKey.getType(), pbeKey.getDigest());
            byte[] key = pbeKey.getEncoded();
            generator.init(key, pbeParam.getSalt(), pbeParam.getIterationCount());
            CipherParameters param = generator.generateDerivedMacParameters(pbeKey.getKeySize());
            for (int i = PBE.PKCS5S1; i != key.length; i += PBE.SHA1) {
                key[i] = (byte) 0;
            }
            return param;
        }

        public static CipherParameters makePBEMacParameters(PBEKeySpec keySpec, int type, int hash, int keySize) {
            PBEParametersGenerator generator = makePBEGenerator(type, hash);
            byte[] key = convertPassword(type, keySpec);
            generator.init(key, keySpec.getSalt(), keySpec.getIterationCount());
            CipherParameters param = generator.generateDerivedMacParameters(keySize);
            for (int i = PBE.PKCS5S1; i != key.length; i += PBE.SHA1) {
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
            for (int i = PBE.PKCS5S1; i != key.length; i += PBE.SHA1) {
                key[i] = (byte) 0;
            }
            return param;
        }

        public static CipherParameters makePBEMacParameters(SecretKey key, int type, int hash, int keySize, PBEParameterSpec pbeSpec) {
            PBEParametersGenerator generator = makePBEGenerator(type, hash);
            byte[] keyBytes = key.getEncoded();
            generator.init(key.getEncoded(), pbeSpec.getSalt(), pbeSpec.getIterationCount());
            CipherParameters param = generator.generateDerivedMacParameters(keySize);
            for (int i = PBE.PKCS5S1; i != keyBytes.length; i += PBE.SHA1) {
                keyBytes[i] = (byte) 0;
            }
            return param;
        }

        private static byte[] convertPassword(int type, PBEKeySpec keySpec) {
            if (type == PBE.PKCS12) {
                return PBEParametersGenerator.PKCS12PasswordToBytes(keySpec.getPassword());
            }
            if (type == PBE.PKCS5S2_UTF8 || type == PBE.SHA256) {
                return PBEParametersGenerator.PKCS5PasswordToUTF8Bytes(keySpec.getPassword());
            }
            return PBEParametersGenerator.PKCS5PasswordToBytes(keySpec.getPassword());
        }
    }
}
