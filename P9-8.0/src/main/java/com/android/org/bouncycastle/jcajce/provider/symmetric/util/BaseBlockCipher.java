package com.android.org.bouncycastle.jcajce.provider.symmetric.util;

import com.android.org.bouncycastle.asn1.cms.GCMParameters;
import com.android.org.bouncycastle.crypto.BlockCipher;
import com.android.org.bouncycastle.crypto.BufferedBlockCipher;
import com.android.org.bouncycastle.crypto.CipherParameters;
import com.android.org.bouncycastle.crypto.DataLengthException;
import com.android.org.bouncycastle.crypto.InvalidCipherTextException;
import com.android.org.bouncycastle.crypto.OutputLengthException;
import com.android.org.bouncycastle.crypto.modes.AEADBlockCipher;
import com.android.org.bouncycastle.crypto.modes.CBCBlockCipher;
import com.android.org.bouncycastle.crypto.modes.CCMBlockCipher;
import com.android.org.bouncycastle.crypto.modes.CFBBlockCipher;
import com.android.org.bouncycastle.crypto.modes.CTSBlockCipher;
import com.android.org.bouncycastle.crypto.modes.GCMBlockCipher;
import com.android.org.bouncycastle.crypto.modes.OFBBlockCipher;
import com.android.org.bouncycastle.crypto.modes.SICBlockCipher;
import com.android.org.bouncycastle.crypto.paddings.BlockCipherPadding;
import com.android.org.bouncycastle.crypto.paddings.ISO10126d2Padding;
import com.android.org.bouncycastle.crypto.paddings.ISO7816d4Padding;
import com.android.org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import com.android.org.bouncycastle.crypto.paddings.TBCPadding;
import com.android.org.bouncycastle.crypto.paddings.X923Padding;
import com.android.org.bouncycastle.crypto.paddings.ZeroBytePadding;
import com.android.org.bouncycastle.crypto.params.AEADParameters;
import com.android.org.bouncycastle.crypto.params.KeyParameter;
import com.android.org.bouncycastle.crypto.params.ParametersWithIV;
import com.android.org.bouncycastle.crypto.params.ParametersWithRandom;
import com.android.org.bouncycastle.jcajce.PKCS12Key;
import com.android.org.bouncycastle.jcajce.PKCS12KeyWithParameters;
import com.android.org.bouncycastle.jcajce.provider.symmetric.util.PBE.Util;
import com.android.org.bouncycastle.jcajce.spec.AEADParameterSpec;
import com.android.org.bouncycastle.util.Strings;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;
import javax.crypto.interfaces.PBEKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

public class BaseBlockCipher extends BaseWrapCipher implements PBE {
    private static final Class gcmSpecClass = lookup("javax.crypto.spec.GCMParameterSpec");
    private AEADParameters aeadParams;
    private Class[] availableSpecs;
    private BlockCipher baseEngine;
    private GenericBlockCipher cipher;
    private int digest;
    private BlockCipherProvider engineProvider;
    private boolean fixedIv;
    private int ivLength;
    private ParametersWithIV ivParam;
    private int keySizeInBits;
    private String modeName;
    private boolean padded;
    private String pbeAlgorithm;
    private PBEParameterSpec pbeSpec;
    private int scheme;

    private interface GenericBlockCipher {
        int doFinal(byte[] bArr, int i) throws IllegalStateException, BadPaddingException;

        String getAlgorithmName();

        int getOutputSize(int i);

        BlockCipher getUnderlyingCipher();

        int getUpdateOutputSize(int i);

        void init(boolean z, CipherParameters cipherParameters) throws IllegalArgumentException;

        int processByte(byte b, byte[] bArr, int i) throws DataLengthException;

        int processBytes(byte[] bArr, int i, int i2, byte[] bArr2, int i3) throws DataLengthException;

        void updateAAD(byte[] bArr, int i, int i2);

        boolean wrapOnNoPadding();
    }

    private static class AEADGenericBlockCipher implements GenericBlockCipher {
        private static final Constructor aeadBadTagConstructor;
        private AEADBlockCipher cipher;

        static {
            Class aeadBadTagClass = BaseBlockCipher.lookup("javax.crypto.AEADBadTagException");
            if (aeadBadTagClass != null) {
                aeadBadTagConstructor = findExceptionConstructor(aeadBadTagClass);
            } else {
                aeadBadTagConstructor = null;
            }
        }

        private static Constructor findExceptionConstructor(Class clazz) {
            try {
                return clazz.getConstructor(new Class[]{String.class});
            } catch (Exception e) {
                return null;
            }
        }

        AEADGenericBlockCipher(AEADBlockCipher cipher) {
            this.cipher = cipher;
        }

        public void init(boolean forEncryption, CipherParameters params) throws IllegalArgumentException {
            this.cipher.init(forEncryption, params);
        }

        public String getAlgorithmName() {
            return this.cipher.getUnderlyingCipher().getAlgorithmName();
        }

        public boolean wrapOnNoPadding() {
            return false;
        }

        public BlockCipher getUnderlyingCipher() {
            return this.cipher.getUnderlyingCipher();
        }

        public int getOutputSize(int len) {
            return this.cipher.getOutputSize(len);
        }

        public int getUpdateOutputSize(int len) {
            return this.cipher.getUpdateOutputSize(len);
        }

        public void updateAAD(byte[] input, int offset, int length) {
            this.cipher.processAADBytes(input, offset, length);
        }

        public int processByte(byte in, byte[] out, int outOff) throws DataLengthException {
            return this.cipher.processByte(in, out, outOff);
        }

        public int processBytes(byte[] in, int inOff, int len, byte[] out, int outOff) throws DataLengthException {
            return this.cipher.processBytes(in, inOff, len, out, outOff);
        }

        public int doFinal(byte[] out, int outOff) throws IllegalStateException, BadPaddingException {
            try {
                return this.cipher.doFinal(out, outOff);
            } catch (InvalidCipherTextException e) {
                if (aeadBadTagConstructor != null) {
                    BadPaddingException aeadBadTag = null;
                    try {
                        aeadBadTag = (BadPaddingException) aeadBadTagConstructor.newInstance(new Object[]{e.getMessage()});
                    } catch (Exception e2) {
                    }
                    if (aeadBadTag != null) {
                        throw aeadBadTag;
                    }
                }
                throw new BadPaddingException(e.getMessage());
            }
        }
    }

    private static class BufferedGenericBlockCipher implements GenericBlockCipher {
        private BufferedBlockCipher cipher;

        BufferedGenericBlockCipher(BufferedBlockCipher cipher) {
            this.cipher = cipher;
        }

        BufferedGenericBlockCipher(BlockCipher cipher) {
            this.cipher = new PaddedBufferedBlockCipher(cipher);
        }

        BufferedGenericBlockCipher(BlockCipher cipher, BlockCipherPadding padding) {
            this.cipher = new PaddedBufferedBlockCipher(cipher, padding);
        }

        public void init(boolean forEncryption, CipherParameters params) throws IllegalArgumentException {
            this.cipher.init(forEncryption, params);
        }

        public boolean wrapOnNoPadding() {
            return (this.cipher instanceof CTSBlockCipher) ^ 1;
        }

        public String getAlgorithmName() {
            return this.cipher.getUnderlyingCipher().getAlgorithmName();
        }

        public BlockCipher getUnderlyingCipher() {
            return this.cipher.getUnderlyingCipher();
        }

        public int getOutputSize(int len) {
            return this.cipher.getOutputSize(len);
        }

        public int getUpdateOutputSize(int len) {
            return this.cipher.getUpdateOutputSize(len);
        }

        public void updateAAD(byte[] input, int offset, int length) {
            throw new UnsupportedOperationException("AAD is not supported in the current mode.");
        }

        public int processByte(byte in, byte[] out, int outOff) throws DataLengthException {
            return this.cipher.processByte(in, out, outOff);
        }

        public int processBytes(byte[] in, int inOff, int len, byte[] out, int outOff) throws DataLengthException {
            return this.cipher.processBytes(in, inOff, len, out, outOff);
        }

        public int doFinal(byte[] out, int outOff) throws IllegalStateException, BadPaddingException {
            try {
                return this.cipher.doFinal(out, outOff);
            } catch (InvalidCipherTextException e) {
                throw new BadPaddingException(e.getMessage());
            }
        }
    }

    private static class InvalidKeyOrParametersException extends InvalidKeyException {
        private final Throwable cause;

        InvalidKeyOrParametersException(String msg, Throwable cause) {
            super(msg);
            this.cause = cause;
        }

        public Throwable getCause() {
            return this.cause;
        }
    }

    private static Class lookup(String className) {
        try {
            return BaseBlockCipher.class.getClassLoader().loadClass(className);
        } catch (Exception e) {
            return null;
        }
    }

    protected BaseBlockCipher(BlockCipher engine) {
        this.availableSpecs = new Class[]{gcmSpecClass, IvParameterSpec.class, PBEParameterSpec.class};
        this.scheme = -1;
        this.ivLength = 0;
        this.fixedIv = true;
        this.pbeSpec = null;
        this.pbeAlgorithm = null;
        this.modeName = null;
        this.baseEngine = engine;
        this.cipher = new BufferedGenericBlockCipher(engine);
    }

    protected BaseBlockCipher(BlockCipher engine, int scheme, int digest, int keySizeInBits, int ivLength) {
        this.availableSpecs = new Class[]{gcmSpecClass, IvParameterSpec.class, PBEParameterSpec.class};
        this.scheme = -1;
        this.ivLength = 0;
        this.fixedIv = true;
        this.pbeSpec = null;
        this.pbeAlgorithm = null;
        this.modeName = null;
        this.baseEngine = engine;
        this.scheme = scheme;
        this.digest = digest;
        this.keySizeInBits = keySizeInBits;
        this.ivLength = ivLength;
        this.cipher = new BufferedGenericBlockCipher(engine);
    }

    protected BaseBlockCipher(BlockCipherProvider provider) {
        this.availableSpecs = new Class[]{gcmSpecClass, IvParameterSpec.class, PBEParameterSpec.class};
        this.scheme = -1;
        this.ivLength = 0;
        this.fixedIv = true;
        this.pbeSpec = null;
        this.pbeAlgorithm = null;
        this.modeName = null;
        this.baseEngine = provider.get();
        this.engineProvider = provider;
        this.cipher = new BufferedGenericBlockCipher(provider.get());
    }

    protected BaseBlockCipher(AEADBlockCipher engine) {
        this.availableSpecs = new Class[]{gcmSpecClass, IvParameterSpec.class, PBEParameterSpec.class};
        this.scheme = -1;
        this.ivLength = 0;
        this.fixedIv = true;
        this.pbeSpec = null;
        this.pbeAlgorithm = null;
        this.modeName = null;
        this.baseEngine = engine.getUnderlyingCipher();
        this.ivLength = this.baseEngine.getBlockSize();
        this.cipher = new AEADGenericBlockCipher(engine);
    }

    protected BaseBlockCipher(AEADBlockCipher engine, boolean fixedIv, int ivLength) {
        this.availableSpecs = new Class[]{gcmSpecClass, IvParameterSpec.class, PBEParameterSpec.class};
        this.scheme = -1;
        this.ivLength = 0;
        this.fixedIv = true;
        this.pbeSpec = null;
        this.pbeAlgorithm = null;
        this.modeName = null;
        this.baseEngine = engine.getUnderlyingCipher();
        this.fixedIv = fixedIv;
        this.ivLength = ivLength;
        this.cipher = new AEADGenericBlockCipher(engine);
    }

    protected BaseBlockCipher(BlockCipher engine, int ivLength) {
        this.availableSpecs = new Class[]{gcmSpecClass, IvParameterSpec.class, PBEParameterSpec.class};
        this.scheme = -1;
        this.ivLength = 0;
        this.fixedIv = true;
        this.pbeSpec = null;
        this.pbeAlgorithm = null;
        this.modeName = null;
        this.baseEngine = engine;
        this.cipher = new BufferedGenericBlockCipher(engine);
        this.ivLength = ivLength / 8;
    }

    protected BaseBlockCipher(BufferedBlockCipher engine, int ivLength) {
        this.availableSpecs = new Class[]{gcmSpecClass, IvParameterSpec.class, PBEParameterSpec.class};
        this.scheme = -1;
        this.ivLength = 0;
        this.fixedIv = true;
        this.pbeSpec = null;
        this.pbeAlgorithm = null;
        this.modeName = null;
        this.baseEngine = engine.getUnderlyingCipher();
        this.cipher = new BufferedGenericBlockCipher(engine);
        this.ivLength = ivLength / 8;
    }

    protected int engineGetBlockSize() {
        return this.baseEngine.getBlockSize();
    }

    protected byte[] engineGetIV() {
        byte[] bArr = null;
        if (this.aeadParams != null) {
            return this.aeadParams.getNonce();
        }
        if (this.ivParam != null) {
            bArr = this.ivParam.getIV();
        }
        return bArr;
    }

    protected int engineGetKeySize(Key key) {
        return key.getEncoded().length * 8;
    }

    protected int engineGetOutputSize(int inputLen) {
        return this.cipher.getOutputSize(inputLen);
    }

    protected AlgorithmParameters engineGetParameters() {
        if (this.engineParams == null) {
            if (this.pbeSpec != null) {
                try {
                    this.engineParams = createParametersInstance(this.pbeAlgorithm);
                    this.engineParams.init(this.pbeSpec);
                } catch (Exception e) {
                    return null;
                }
            } else if (this.aeadParams != null) {
                try {
                    this.engineParams = createParametersInstance("GCM");
                    this.engineParams.init(new GCMParameters(this.aeadParams.getNonce(), this.aeadParams.getMacSize() / 8).getEncoded());
                } catch (Exception e2) {
                    throw new RuntimeException(e2.toString());
                }
            } else if (this.ivParam != null) {
                String name = this.cipher.getUnderlyingCipher().getAlgorithmName();
                if (name.indexOf(47) >= 0) {
                    name = name.substring(0, name.indexOf(47));
                }
                try {
                    this.engineParams = createParametersInstance(name);
                    this.engineParams.init(this.ivParam.getIV());
                } catch (Exception e22) {
                    throw new RuntimeException(e22.toString());
                }
            }
        }
        return this.engineParams;
    }

    protected void engineSetMode(String mode) throws NoSuchAlgorithmException {
        this.modeName = Strings.toUpperCase(mode);
        if (this.modeName.equals("ECB")) {
            this.ivLength = 0;
            this.cipher = new BufferedGenericBlockCipher(this.baseEngine);
        } else if (this.modeName.equals("CBC")) {
            this.ivLength = this.baseEngine.getBlockSize();
            this.cipher = new BufferedGenericBlockCipher(new CBCBlockCipher(this.baseEngine));
        } else if (this.modeName.startsWith("OFB")) {
            this.ivLength = this.baseEngine.getBlockSize();
            if (this.modeName.length() != 3) {
                this.cipher = new BufferedGenericBlockCipher(new OFBBlockCipher(this.baseEngine, Integer.parseInt(this.modeName.substring(3))));
                return;
            }
            this.cipher = new BufferedGenericBlockCipher(new OFBBlockCipher(this.baseEngine, this.baseEngine.getBlockSize() * 8));
        } else if (this.modeName.startsWith("CFB")) {
            this.ivLength = this.baseEngine.getBlockSize();
            if (this.modeName.length() != 3) {
                this.cipher = new BufferedGenericBlockCipher(new CFBBlockCipher(this.baseEngine, Integer.parseInt(this.modeName.substring(3))));
                return;
            }
            this.cipher = new BufferedGenericBlockCipher(new CFBBlockCipher(this.baseEngine, this.baseEngine.getBlockSize() * 8));
        } else if (this.modeName.startsWith("CTR")) {
            this.ivLength = this.baseEngine.getBlockSize();
            this.fixedIv = false;
            this.cipher = new BufferedGenericBlockCipher(new BufferedBlockCipher(new SICBlockCipher(this.baseEngine)));
        } else if (this.modeName.startsWith("CTS")) {
            this.ivLength = this.baseEngine.getBlockSize();
            this.cipher = new BufferedGenericBlockCipher(new CTSBlockCipher(new CBCBlockCipher(this.baseEngine)));
        } else if (this.modeName.startsWith("CCM")) {
            this.ivLength = 13;
            this.cipher = new AEADGenericBlockCipher(new CCMBlockCipher(this.baseEngine));
        } else if (this.modeName.startsWith("GCM")) {
            this.ivLength = this.baseEngine.getBlockSize();
            this.cipher = new AEADGenericBlockCipher(new GCMBlockCipher(this.baseEngine));
        } else {
            throw new NoSuchAlgorithmException("can't support mode " + mode);
        }
    }

    protected void engineSetPadding(String padding) throws NoSuchPaddingException {
        String paddingName = Strings.toUpperCase(padding);
        if (paddingName.equals("NOPADDING")) {
            if (this.cipher.wrapOnNoPadding()) {
                this.cipher = new BufferedGenericBlockCipher(new BufferedBlockCipher(this.cipher.getUnderlyingCipher()));
            }
        } else if (paddingName.equals("WITHCTS")) {
            this.cipher = new BufferedGenericBlockCipher(new CTSBlockCipher(this.cipher.getUnderlyingCipher()));
        } else {
            this.padded = true;
            if (isAEADModeName(this.modeName)) {
                throw new NoSuchPaddingException("Only NoPadding can be used with AEAD modes.");
            } else if (paddingName.equals("PKCS5PADDING") || paddingName.equals("PKCS7PADDING")) {
                this.cipher = new BufferedGenericBlockCipher(this.cipher.getUnderlyingCipher());
            } else if (paddingName.equals("ZEROBYTEPADDING")) {
                this.cipher = new BufferedGenericBlockCipher(this.cipher.getUnderlyingCipher(), new ZeroBytePadding());
            } else if (paddingName.equals("ISO10126PADDING") || paddingName.equals("ISO10126-2PADDING")) {
                this.cipher = new BufferedGenericBlockCipher(this.cipher.getUnderlyingCipher(), new ISO10126d2Padding());
            } else if (paddingName.equals("X9.23PADDING") || paddingName.equals("X923PADDING")) {
                this.cipher = new BufferedGenericBlockCipher(this.cipher.getUnderlyingCipher(), new X923Padding());
            } else if (paddingName.equals("ISO7816-4PADDING") || paddingName.equals("ISO9797-1PADDING")) {
                this.cipher = new BufferedGenericBlockCipher(this.cipher.getUnderlyingCipher(), new ISO7816d4Padding());
            } else if (paddingName.equals("TBCPADDING")) {
                this.cipher = new BufferedGenericBlockCipher(this.cipher.getUnderlyingCipher(), new TBCPadding());
            } else {
                throw new NoSuchPaddingException("Padding " + padding + " unknown.");
            }
        }
    }

    private boolean isBCPBEKeyWithoutIV(Key key) {
        return key instanceof BCPBEKey ? (((BCPBEKey) key).getParam() instanceof ParametersWithIV) ^ 1 : false;
    }

    protected void engineInit(int opmode, Key key, AlgorithmParameterSpec params, SecureRandom random) throws InvalidKeyException, InvalidAlgorithmParameterException {
        this.pbeSpec = null;
        this.pbeAlgorithm = null;
        this.engineParams = null;
        this.aeadParams = null;
        if (!(key instanceof SecretKey)) {
            throw new InvalidKeyException("Key for algorithm " + (key != null ? key.getAlgorithm() : null) + " not suitable for symmetric enryption.");
        } else if (params == null && this.baseEngine.getAlgorithmName().startsWith("RC5-64")) {
            throw new InvalidAlgorithmParameterException("RC5 requires an RC5ParametersSpec to be passed in.");
        } else {
            CipherParameters param;
            CipherParameters keyParameter;
            CipherParameters param2;
            if ((this.scheme == 2 || (key instanceof PKCS12Key)) && (isBCPBEKeyWithoutIV(key) ^ 1) != 0) {
                try {
                    SecretKey k = (SecretKey) key;
                    if (params instanceof PBEParameterSpec) {
                        this.pbeSpec = (PBEParameterSpec) params;
                    }
                    if ((k instanceof PBEKey) && this.pbeSpec == null) {
                        PBEKey pbeKey = (PBEKey) k;
                        if (pbeKey.getSalt() == null) {
                            throw new InvalidAlgorithmParameterException("PBEKey requires parameters to specify salt");
                        }
                        this.pbeSpec = new PBEParameterSpec(pbeKey.getSalt(), pbeKey.getIterationCount());
                    }
                    if (this.pbeSpec != null || ((k instanceof PBEKey) ^ 1) == 0) {
                        if (key instanceof BCPBEKey) {
                            CipherParameters pbeKeyParam = ((BCPBEKey) key).getParam();
                            if (pbeKeyParam instanceof ParametersWithIV) {
                                param = pbeKeyParam;
                            } else if (pbeKeyParam == null) {
                                throw new IllegalStateException("Unreachable code");
                            } else {
                                throw new InvalidKeyException("Algorithm requires a PBE key suitable for PKCS12");
                            }
                        }
                        param = Util.makePBEParameters(k.getEncoded(), 2, this.digest, this.keySizeInBits, this.ivLength * 8, this.pbeSpec, this.cipher.getAlgorithmName());
                        if (param instanceof ParametersWithIV) {
                            this.ivParam = (ParametersWithIV) param;
                        }
                    } else {
                        throw new InvalidKeyException("Algorithm requires a PBE key");
                    }
                } catch (Exception e) {
                    throw new InvalidKeyException("PKCS12 requires a SecretKey/PBEKey");
                }
            } else if (key instanceof BCPBEKey) {
                BCPBEKey k2 = (BCPBEKey) key;
                if (k2.getOID() != null) {
                    this.pbeAlgorithm = k2.getOID().getId();
                } else {
                    this.pbeAlgorithm = k2.getAlgorithm();
                }
                if (k2.getParam() != null) {
                    param = adjustParameters(params, k2.getParam());
                } else if (params instanceof PBEParameterSpec) {
                    BCPBEKey k3;
                    this.pbeSpec = (PBEParameterSpec) params;
                    if (this.pbeSpec.getSalt().length == 0 || this.pbeSpec.getIterationCount() <= 0) {
                        k3 = k2;
                    } else {
                        k3 = new BCPBEKey(k2.getAlgorithm(), k2.getOID(), k2.getType(), k2.getDigest(), k2.getKeySize(), k2.getIvSize(), new PBEKeySpec(k2.getPassword(), this.pbeSpec.getSalt(), this.pbeSpec.getIterationCount(), k2.getKeySize()), null);
                    }
                    param = Util.makePBEParameters(k3, params, this.cipher.getUnderlyingCipher().getAlgorithmName());
                } else {
                    throw new InvalidAlgorithmParameterException("PBE requires PBE parameters to be set.");
                }
                if (param instanceof ParametersWithIV) {
                    this.ivParam = (ParametersWithIV) param;
                }
            } else if (key instanceof PBEKey) {
                PBEKey k4 = (PBEKey) key;
                this.pbeSpec = (PBEParameterSpec) params;
                if ((k4 instanceof PKCS12KeyWithParameters) && this.pbeSpec == null) {
                    this.pbeSpec = new PBEParameterSpec(k4.getSalt(), k4.getIterationCount());
                }
                param = Util.makePBEParameters(k4.getEncoded(), this.scheme, this.digest, this.keySizeInBits, this.ivLength * 8, this.pbeSpec, this.cipher.getAlgorithmName());
                if (param instanceof ParametersWithIV) {
                    this.ivParam = (ParametersWithIV) param;
                }
            } else if (this.scheme == 0 || this.scheme == 4 || this.scheme == 1 || this.scheme == 5) {
                throw new InvalidKeyException("Algorithm requires a PBE key");
            } else {
                keyParameter = new KeyParameter(key.getEncoded());
            }
            KeyParameter keyParam;
            if (params instanceof AEADParameterSpec) {
                if (isAEADModeName(this.modeName) || ((this.cipher instanceof AEADGenericBlockCipher) ^ 1) == 0) {
                    AEADParameterSpec aeadSpec = (AEADParameterSpec) params;
                    if (param instanceof ParametersWithIV) {
                        keyParam = (KeyParameter) ((ParametersWithIV) param).getParameters();
                    } else {
                        keyParam = (KeyParameter) param;
                    }
                    keyParameter = new AEADParameters(keyParam, aeadSpec.getMacSizeInBits(), aeadSpec.getNonce(), aeadSpec.getAssociatedData());
                    this.aeadParams = keyParameter;
                } else {
                    throw new InvalidAlgorithmParameterException("AEADParameterSpec can only be used with AEAD modes.");
                }
            } else if (params instanceof IvParameterSpec) {
                if (this.ivLength != 0) {
                    IvParameterSpec p = (IvParameterSpec) params;
                    if (p.getIV().length == this.ivLength || ((this.cipher instanceof AEADGenericBlockCipher) ^ 1) == 0 || !this.fixedIv) {
                        if (param instanceof ParametersWithIV) {
                            param = new ParametersWithIV(((ParametersWithIV) param).getParameters(), p.getIV());
                        } else {
                            param = new ParametersWithIV(param, p.getIV());
                        }
                        this.ivParam = (ParametersWithIV) param;
                    } else {
                        throw new InvalidAlgorithmParameterException("IV must be " + this.ivLength + " bytes long.");
                    }
                } else if (this.modeName != null && this.modeName.equals("ECB")) {
                    throw new InvalidAlgorithmParameterException("ECB mode does not use an IV");
                }
            } else if (gcmSpecClass != null && gcmSpecClass.isInstance(params)) {
                if (isAEADModeName(this.modeName) || ((this.cipher instanceof AEADGenericBlockCipher) ^ 1) == 0) {
                    try {
                        Method tLen = gcmSpecClass.getDeclaredMethod("getTLen", new Class[0]);
                        Method iv = gcmSpecClass.getDeclaredMethod("getIV", new Class[0]);
                        if (param instanceof ParametersWithIV) {
                            keyParam = (KeyParameter) ((ParametersWithIV) param).getParameters();
                        } else {
                            keyParam = (KeyParameter) param;
                        }
                        keyParameter = new AEADParameters(keyParam, ((Integer) tLen.invoke(params, new Object[0])).intValue(), (byte[]) iv.invoke(params, new Object[0]));
                        this.aeadParams = keyParameter;
                    } catch (Exception e2) {
                        throw new InvalidAlgorithmParameterException("Cannot process GCMParameterSpec.");
                    }
                }
                throw new InvalidAlgorithmParameterException("GCMParameterSpec can only be used with AEAD modes.");
            } else if (!(params == null || ((params instanceof PBEParameterSpec) ^ 1) == 0)) {
                throw new InvalidAlgorithmParameterException("unknown parameter type.");
            }
            if (this.ivLength == 0 || ((param instanceof ParametersWithIV) ^ 1) == 0) {
                param2 = param;
            } else {
                if (((param instanceof AEADParameters) ^ 1) != 0) {
                    SecureRandom ivRandom = random;
                    if (random == null) {
                        ivRandom = new SecureRandom();
                    }
                    if (opmode == 1 || opmode == 3) {
                        byte[] iv2 = new byte[this.ivLength];
                        if (!isBCPBEKeyWithoutIV(key)) {
                            ivRandom.nextBytes(iv2);
                        }
                        keyParameter = new ParametersWithIV(param, iv2);
                        this.ivParam = (ParametersWithIV) keyParameter;
                    } else if (this.cipher.getUnderlyingCipher().getAlgorithmName().indexOf("PGPCFB") < 0) {
                        if (isBCPBEKeyWithoutIV(key)) {
                            keyParameter = new ParametersWithIV(param, new byte[this.ivLength]);
                            this.ivParam = (ParametersWithIV) keyParameter;
                        } else {
                            throw new InvalidAlgorithmParameterException("no IV set when one expected");
                        }
                    }
                }
                param2 = param;
            }
            if (random == null) {
                param = param2;
            } else if (this.padded) {
                keyParameter = new ParametersWithRandom(param2, random);
            } else {
                param = param2;
            }
            switch (opmode) {
                case 1:
                case 3:
                    this.cipher.init(true, param);
                    break;
                case 2:
                case 4:
                    this.cipher.init(false, param);
                    break;
                default:
                    try {
                        throw new InvalidParameterException("unknown opmode " + opmode + " passed");
                    } catch (Exception e3) {
                        throw new InvalidKeyOrParametersException(e3.getMessage(), e3);
                    }
            }
            if ((this.cipher instanceof AEADGenericBlockCipher) && this.aeadParams == null) {
                this.aeadParams = new AEADParameters((KeyParameter) this.ivParam.getParameters(), ((AEADGenericBlockCipher) this.cipher).cipher.getMac().length * 8, this.ivParam.getIV());
            }
        }
    }

    private CipherParameters adjustParameters(AlgorithmParameterSpec params, CipherParameters param) {
        if (param instanceof ParametersWithIV) {
            CipherParameters key = ((ParametersWithIV) param).getParameters();
            if (!(params instanceof IvParameterSpec)) {
                return param;
            }
            this.ivParam = new ParametersWithIV(key, ((IvParameterSpec) params).getIV());
            return this.ivParam;
        } else if (!(params instanceof IvParameterSpec)) {
            return param;
        } else {
            this.ivParam = new ParametersWithIV(param, ((IvParameterSpec) params).getIV());
            return this.ivParam;
        }
    }

    protected void engineInit(int opmode, Key key, AlgorithmParameters params, SecureRandom random) throws InvalidKeyException, InvalidAlgorithmParameterException {
        AlgorithmParameterSpec paramSpec = null;
        if (params != null) {
            for (int i = 0; i != this.availableSpecs.length; i++) {
                if (this.availableSpecs[i] != null) {
                    try {
                        paramSpec = params.getParameterSpec(this.availableSpecs[i]);
                        break;
                    } catch (Exception e) {
                    }
                }
            }
            if (paramSpec == null) {
                throw new InvalidAlgorithmParameterException("can't handle parameter " + params.toString());
            }
        }
        engineInit(opmode, key, paramSpec, random);
        this.engineParams = params;
    }

    protected void engineInit(int opmode, Key key, SecureRandom random) throws InvalidKeyException {
        try {
            engineInit(opmode, key, (AlgorithmParameterSpec) null, random);
        } catch (InvalidAlgorithmParameterException e) {
            throw new InvalidKeyException(e.getMessage());
        }
    }

    protected void engineUpdateAAD(byte[] input, int offset, int length) {
        this.cipher.updateAAD(input, offset, length);
    }

    protected void engineUpdateAAD(ByteBuffer bytebuffer) {
        engineUpdateAAD(bytebuffer.array(), bytebuffer.arrayOffset() + bytebuffer.position(), bytebuffer.limit() - bytebuffer.position());
    }

    protected byte[] engineUpdate(byte[] input, int inputOffset, int inputLen) {
        int length = this.cipher.getUpdateOutputSize(inputLen);
        if (length > 0) {
            byte[] out = new byte[length];
            int len = this.cipher.processBytes(input, inputOffset, inputLen, out, 0);
            if (len == 0) {
                return null;
            }
            if (len == out.length) {
                return out;
            }
            byte[] tmp = new byte[len];
            System.arraycopy(out, 0, tmp, 0, len);
            return tmp;
        }
        this.cipher.processBytes(input, inputOffset, inputLen, null, 0);
        return null;
    }

    protected int engineUpdate(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset) throws ShortBufferException {
        if (this.cipher.getUpdateOutputSize(inputLen) + outputOffset > output.length) {
            throw new ShortBufferException("output buffer too short for input.");
        }
        try {
            return this.cipher.processBytes(input, inputOffset, inputLen, output, outputOffset);
        } catch (DataLengthException e) {
            throw new IllegalStateException(e.toString());
        }
    }

    protected byte[] engineDoFinal(byte[] input, int inputOffset, int inputLen) throws IllegalBlockSizeException, BadPaddingException {
        int len = 0;
        byte[] tmp = new byte[engineGetOutputSize(inputLen)];
        if (inputLen != 0) {
            len = this.cipher.processBytes(input, inputOffset, inputLen, tmp, 0);
        }
        try {
            len += this.cipher.doFinal(tmp, len);
            if (len == tmp.length) {
                return tmp;
            }
            byte[] out = new byte[len];
            System.arraycopy(tmp, 0, out, 0, len);
            return out;
        } catch (DataLengthException e) {
            throw new IllegalBlockSizeException(e.getMessage());
        }
    }

    protected int engineDoFinal(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset) throws IllegalBlockSizeException, BadPaddingException, ShortBufferException {
        int len = 0;
        if (engineGetOutputSize(inputLen) + outputOffset > output.length) {
            throw new ShortBufferException("output buffer too short for input.");
        }
        if (inputLen != 0) {
            try {
                len = this.cipher.processBytes(input, inputOffset, inputLen, output, outputOffset);
            } catch (OutputLengthException e) {
                throw new IllegalBlockSizeException(e.getMessage());
            } catch (DataLengthException e2) {
                throw new IllegalBlockSizeException(e2.getMessage());
            }
        }
        return this.cipher.doFinal(output, outputOffset + len) + len;
    }

    private boolean isAEADModeName(String modeName) {
        return !"CCM".equals(modeName) ? "GCM".equals(modeName) : true;
    }
}
