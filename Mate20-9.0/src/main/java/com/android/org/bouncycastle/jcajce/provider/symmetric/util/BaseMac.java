package com.android.org.bouncycastle.jcajce.provider.symmetric.util;

import com.android.org.bouncycastle.crypto.CipherParameters;
import com.android.org.bouncycastle.crypto.Mac;
import com.android.org.bouncycastle.crypto.macs.HMac;
import com.android.org.bouncycastle.crypto.params.AEADParameters;
import com.android.org.bouncycastle.crypto.params.KeyParameter;
import com.android.org.bouncycastle.crypto.params.ParametersWithIV;
import com.android.org.bouncycastle.jcajce.PKCS12Key;
import com.android.org.bouncycastle.jcajce.provider.symmetric.util.PBE;
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

    protected BaseMac(Mac macEngine2) {
        this.macEngine = macEngine2;
    }

    protected BaseMac(Mac macEngine2, int scheme2, int pbeHash2, int keySize2) {
        this.macEngine = macEngine2;
        this.scheme = scheme2;
        this.pbeHash = pbeHash2;
        this.keySize = keySize2;
    }

    /* access modifiers changed from: protected */
    public void engineInit(Key key, AlgorithmParameterSpec params) throws InvalidKeyException, InvalidAlgorithmParameterException {
        CipherParameters param;
        KeyParameter keyParam;
        CipherParameters param2;
        if (key != null) {
            if (key instanceof PKCS12Key) {
                try {
                    SecretKey k = (SecretKey) key;
                    try {
                        PBEParameterSpec pbeSpec = (PBEParameterSpec) params;
                        if ((k instanceof PBEKey) && pbeSpec == null) {
                            pbeSpec = new PBEParameterSpec(((PBEKey) k).getSalt(), ((PBEKey) k).getIterationCount());
                        }
                        int digest = 1;
                        int keySize2 = 160;
                        if ((this.macEngine instanceof HMac) && !this.macEngine.getAlgorithmName().startsWith("SHA-1")) {
                            if (this.macEngine.getAlgorithmName().startsWith("SHA-224")) {
                                digest = 7;
                                keySize2 = 224;
                            } else if (this.macEngine.getAlgorithmName().startsWith("SHA-256")) {
                                digest = 4;
                                keySize2 = 256;
                            } else if (this.macEngine.getAlgorithmName().startsWith("SHA-384")) {
                                digest = 8;
                                keySize2 = 384;
                            } else if (this.macEngine.getAlgorithmName().startsWith("SHA-512")) {
                                digest = 9;
                                keySize2 = 512;
                            } else {
                                throw new InvalidAlgorithmParameterException("no PKCS12 mapping for HMAC: " + this.macEngine.getAlgorithmName());
                            }
                        }
                        param = PBE.Util.makePBEMacParameters(k, 2, digest, keySize2, pbeSpec);
                    } catch (Exception e) {
                        throw new InvalidAlgorithmParameterException("PKCS12 requires a PBEParameterSpec");
                    }
                } catch (Exception e2) {
                    throw new InvalidKeyException("PKCS12 requires a SecretKey/PBEKey");
                }
            } else if (key instanceof BCPBEKey) {
                BCPBEKey k2 = (BCPBEKey) key;
                if (k2.getParam() != null) {
                    param2 = k2.getParam();
                } else if (params instanceof PBEParameterSpec) {
                    param2 = PBE.Util.makePBEMacParameters(k2, params);
                } else {
                    throw new InvalidAlgorithmParameterException("PBE requires PBE parameters to be set.");
                }
                param = param2;
            } else if (!(params instanceof PBEParameterSpec)) {
                param = new KeyParameter(key.getEncoded());
            } else {
                throw new InvalidAlgorithmParameterException("inappropriate parameter type: " + params.getClass().getName());
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
                    param = new AEADParameters(keyParam, ((Integer) gcmSpecClass.getDeclaredMethod("getTLen", new Class[0]).invoke(params, new Object[0])).intValue(), (byte[]) gcmSpecClass.getDeclaredMethod("getIV", new Class[0]).invoke(params, new Object[0]));
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
        } else {
            throw new InvalidKeyException("key is null");
        }
    }

    /* access modifiers changed from: protected */
    public int engineGetMacLength() {
        return this.macEngine.getMacSize();
    }

    /* access modifiers changed from: protected */
    public void engineReset() {
        this.macEngine.reset();
    }

    /* access modifiers changed from: protected */
    public void engineUpdate(byte input) {
        this.macEngine.update(input);
    }

    /* access modifiers changed from: protected */
    public void engineUpdate(byte[] input, int offset, int len) {
        this.macEngine.update(input, offset, len);
    }

    /* access modifiers changed from: protected */
    public byte[] engineDoFinal() {
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
