package com.android.org.bouncycastle.jcajce.provider.symmetric.util;

import com.android.org.bouncycastle.crypto.CipherParameters;
import com.android.org.bouncycastle.crypto.Mac;
import com.android.org.bouncycastle.crypto.params.AEADParameters;
import com.android.org.bouncycastle.crypto.params.KeyParameter;
import com.android.org.bouncycastle.crypto.params.ParametersWithIV;
import com.android.org.bouncycastle.jcajce.PKCS12Key;
import com.android.org.bouncycastle.jcajce.provider.symmetric.util.PBE.Util;
import com.android.org.bouncycastle.jcajce.spec.AEADParameterSpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Hashtable;
import java.util.Map;
import javax.crypto.MacSpi;
import javax.crypto.SecretKey;
import javax.crypto.interfaces.PBEKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEParameterSpec;

public class BaseMac extends MacSpi implements PBE {
    private static final Class gcmSpecClass = lookup("javax.crypto.spec.GCMParameterSpec");
    private int keySize = 160;
    private Mac macEngine;
    private int pbeHash = 1;
    private int scheme = 2;

    protected BaseMac(Mac macEngine) {
        this.macEngine = macEngine;
    }

    protected BaseMac(Mac macEngine, int scheme, int pbeHash, int keySize) {
        this.macEngine = macEngine;
        this.scheme = scheme;
        this.pbeHash = pbeHash;
        this.keySize = keySize;
    }

    protected void engineInit(Key key, AlgorithmParameterSpec params) throws InvalidKeyException, InvalidAlgorithmParameterException {
        if (key == null) {
            throw new InvalidKeyException("key is null");
        }
        CipherParameters param;
        KeyParameter keyParam;
        if (key instanceof PKCS12Key) {
            try {
                SecretKey k = (SecretKey) key;
                try {
                    PBEParameterSpec pbeSpec = (PBEParameterSpec) params;
                    if ((k instanceof PBEKey) && pbeSpec == null) {
                        pbeSpec = new PBEParameterSpec(((PBEKey) k).getSalt(), ((PBEKey) k).getIterationCount());
                    }
                    int digest = 1;
                    int keySize = 160;
                    if (this.macEngine.getAlgorithmName().startsWith("SHA256")) {
                        digest = 4;
                        keySize = 256;
                    }
                    param = Util.makePBEMacParameters(k, 2, digest, keySize, pbeSpec);
                } catch (Exception e) {
                    throw new InvalidAlgorithmParameterException("PKCS12 requires a PBEParameterSpec");
                }
            } catch (Exception e2) {
                throw new InvalidKeyException("PKCS12 requires a SecretKey/PBEKey");
            }
        } else if (key instanceof BCPBEKey) {
            BCPBEKey k2 = (BCPBEKey) key;
            if (k2.getParam() != null) {
                param = k2.getParam();
            } else if (params instanceof PBEParameterSpec) {
                param = Util.makePBEMacParameters(k2, params);
            } else {
                throw new InvalidAlgorithmParameterException("PBE requires PBE parameters to be set.");
            }
        } else if (params instanceof PBEParameterSpec) {
            throw new InvalidAlgorithmParameterException("inappropriate parameter type: " + params.getClass().getName());
        } else {
            param = new KeyParameter(key.getEncoded());
        }
        if (param instanceof ParametersWithIV) {
            keyParam = (KeyParameter) ((ParametersWithIV) param).getParameters();
        } else {
            keyParam = (KeyParameter) param;
        }
        if (params instanceof AEADParameterSpec) {
            AEADParameterSpec aeadSpec = (AEADParameterSpec) params;
            param = new AEADParameters(keyParam, aeadSpec.getMacSizeInBits(), aeadSpec.getNonce(), aeadSpec.getAssociatedData());
        } else if (params instanceof IvParameterSpec) {
            param = new ParametersWithIV(keyParam, ((IvParameterSpec) params).getIV());
        } else if (params == null) {
            param = new KeyParameter(key.getEncoded());
        } else if (gcmSpecClass != null && gcmSpecClass.isAssignableFrom(params.getClass())) {
            try {
                AlgorithmParameterSpec algorithmParameterSpec = params;
                param = new AEADParameters(keyParam, ((Integer) gcmSpecClass.getDeclaredMethod("getTLen", new Class[0]).invoke(algorithmParameterSpec, new Object[0])).intValue(), (byte[]) gcmSpecClass.getDeclaredMethod("getIV", new Class[0]).invoke(params, new Object[0]));
            } catch (Exception e3) {
                throw new InvalidAlgorithmParameterException("Cannot process GCMParameterSpec.");
            }
        } else if (!(params instanceof PBEParameterSpec)) {
            throw new InvalidAlgorithmParameterException("unknown parameter type: " + params.getClass().getName());
        }
        try {
            this.macEngine.init(param);
        } catch (Exception e4) {
            throw new InvalidAlgorithmParameterException("cannot initialize MAC: " + e4.getMessage());
        }
    }

    protected int engineGetMacLength() {
        return this.macEngine.getMacSize();
    }

    protected void engineReset() {
        this.macEngine.reset();
    }

    protected void engineUpdate(byte input) {
        this.macEngine.update(input);
    }

    protected void engineUpdate(byte[] input, int offset, int len) {
        this.macEngine.update(input, offset, len);
    }

    protected byte[] engineDoFinal() {
        byte[] out = new byte[engineGetMacLength()];
        this.macEngine.doFinal(out, 0);
        return out;
    }

    private static Hashtable copyMap(Map paramsMap) {
        Hashtable newTable = new Hashtable();
        for (Object key : paramsMap.keySet()) {
            newTable.put(key, paramsMap.get(key));
        }
        return newTable;
    }

    private static Class lookup(String className) {
        try {
            return BaseBlockCipher.class.getClassLoader().loadClass(className);
        } catch (Exception e) {
            return null;
        }
    }
}
