package org.bouncycastle.jcajce.provider.symmetric.util;

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
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.RC2ParameterSpec;
import javax.crypto.spec.RC5ParameterSpec;
import org.bouncycastle.asn1.cms.GCMParameters;
import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.CryptoServicesRegistrar;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.OutputLengthException;
import org.bouncycastle.crypto.engines.DSTU7624Engine;
import org.bouncycastle.crypto.modes.AEADBlockCipher;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.modes.CCMBlockCipher;
import org.bouncycastle.crypto.modes.CFBBlockCipher;
import org.bouncycastle.crypto.modes.CTSBlockCipher;
import org.bouncycastle.crypto.modes.EAXBlockCipher;
import org.bouncycastle.crypto.modes.GCFBBlockCipher;
import org.bouncycastle.crypto.modes.GCMBlockCipher;
import org.bouncycastle.crypto.modes.GOFBBlockCipher;
import org.bouncycastle.crypto.modes.KCCMBlockCipher;
import org.bouncycastle.crypto.modes.KCTRBlockCipher;
import org.bouncycastle.crypto.modes.KGCMBlockCipher;
import org.bouncycastle.crypto.modes.OCBBlockCipher;
import org.bouncycastle.crypto.modes.OFBBlockCipher;
import org.bouncycastle.crypto.modes.OpenPGPCFBBlockCipher;
import org.bouncycastle.crypto.modes.PGPCFBBlockCipher;
import org.bouncycastle.crypto.modes.SICBlockCipher;
import org.bouncycastle.crypto.paddings.BlockCipherPadding;
import org.bouncycastle.crypto.paddings.ISO10126d2Padding;
import org.bouncycastle.crypto.paddings.ISO7816d4Padding;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.paddings.TBCPadding;
import org.bouncycastle.crypto.paddings.X923Padding;
import org.bouncycastle.crypto.paddings.ZeroBytePadding;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.crypto.params.ParametersWithSBox;
import org.bouncycastle.crypto.params.RC2Parameters;
import org.bouncycastle.crypto.params.RC5Parameters;
import org.bouncycastle.jcajce.PBKDF1Key;
import org.bouncycastle.jcajce.PBKDF1KeyWithParameters;
import org.bouncycastle.jcajce.PKCS12Key;
import org.bouncycastle.jcajce.PKCS12KeyWithParameters;
import org.bouncycastle.jcajce.provider.symmetric.util.PBE;
import org.bouncycastle.jcajce.spec.AEADParameterSpec;
import org.bouncycastle.jcajce.spec.GOST28147ParameterSpec;
import org.bouncycastle.jcajce.spec.RepeatedSecretKeySpec;
import org.bouncycastle.util.Strings;

public class BaseBlockCipher extends BaseWrapCipher implements PBE {
    private static final Class gcmSpecClass = ClassUtil.loadClass(BaseBlockCipher.class, "javax.crypto.spec.GCMParameterSpec");
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

    private static class AEADGenericBlockCipher implements GenericBlockCipher {
        private static final Constructor aeadBadTagConstructor;
        /* access modifiers changed from: private */
        public AEADBlockCipher cipher;

        static {
            Class loadClass = ClassUtil.loadClass(BaseBlockCipher.class, "javax.crypto.AEADBadTagException");
            aeadBadTagConstructor = loadClass != null ? findExceptionConstructor(loadClass) : null;
        }

        AEADGenericBlockCipher(AEADBlockCipher aEADBlockCipher) {
            this.cipher = aEADBlockCipher;
        }

        private static Constructor findExceptionConstructor(Class cls) {
            try {
                return cls.getConstructor(new Class[]{String.class});
            } catch (Exception e) {
                return null;
            }
        }

        public int doFinal(byte[] bArr, int i) throws IllegalStateException, BadPaddingException {
            try {
                return this.cipher.doFinal(bArr, i);
            } catch (InvalidCipherTextException e) {
                if (aeadBadTagConstructor != null) {
                    BadPaddingException badPaddingException = null;
                    try {
                        badPaddingException = (BadPaddingException) aeadBadTagConstructor.newInstance(new Object[]{e.getMessage()});
                    } catch (Exception e2) {
                    }
                    if (badPaddingException != null) {
                        throw badPaddingException;
                    }
                }
                throw new BadPaddingException(e.getMessage());
            }
        }

        public String getAlgorithmName() {
            return this.cipher.getUnderlyingCipher().getAlgorithmName();
        }

        public int getOutputSize(int i) {
            return this.cipher.getOutputSize(i);
        }

        public BlockCipher getUnderlyingCipher() {
            return this.cipher.getUnderlyingCipher();
        }

        public int getUpdateOutputSize(int i) {
            return this.cipher.getUpdateOutputSize(i);
        }

        public void init(boolean z, CipherParameters cipherParameters) throws IllegalArgumentException {
            this.cipher.init(z, cipherParameters);
        }

        public int processByte(byte b, byte[] bArr, int i) throws DataLengthException {
            return this.cipher.processByte(b, bArr, i);
        }

        public int processBytes(byte[] bArr, int i, int i2, byte[] bArr2, int i3) throws DataLengthException {
            return this.cipher.processBytes(bArr, i, i2, bArr2, i3);
        }

        public void updateAAD(byte[] bArr, int i, int i2) {
            this.cipher.processAADBytes(bArr, i, i2);
        }

        public boolean wrapOnNoPadding() {
            return false;
        }
    }

    private static class BufferedGenericBlockCipher implements GenericBlockCipher {
        private BufferedBlockCipher cipher;

        BufferedGenericBlockCipher(BlockCipher blockCipher) {
            this.cipher = new PaddedBufferedBlockCipher(blockCipher);
        }

        BufferedGenericBlockCipher(BlockCipher blockCipher, BlockCipherPadding blockCipherPadding) {
            this.cipher = new PaddedBufferedBlockCipher(blockCipher, blockCipherPadding);
        }

        BufferedGenericBlockCipher(BufferedBlockCipher bufferedBlockCipher) {
            this.cipher = bufferedBlockCipher;
        }

        public int doFinal(byte[] bArr, int i) throws IllegalStateException, BadPaddingException {
            try {
                return this.cipher.doFinal(bArr, i);
            } catch (InvalidCipherTextException e) {
                throw new BadPaddingException(e.getMessage());
            }
        }

        public String getAlgorithmName() {
            return this.cipher.getUnderlyingCipher().getAlgorithmName();
        }

        public int getOutputSize(int i) {
            return this.cipher.getOutputSize(i);
        }

        public BlockCipher getUnderlyingCipher() {
            return this.cipher.getUnderlyingCipher();
        }

        public int getUpdateOutputSize(int i) {
            return this.cipher.getUpdateOutputSize(i);
        }

        public void init(boolean z, CipherParameters cipherParameters) throws IllegalArgumentException {
            this.cipher.init(z, cipherParameters);
        }

        public int processByte(byte b, byte[] bArr, int i) throws DataLengthException {
            return this.cipher.processByte(b, bArr, i);
        }

        public int processBytes(byte[] bArr, int i, int i2, byte[] bArr2, int i3) throws DataLengthException {
            return this.cipher.processBytes(bArr, i, i2, bArr2, i3);
        }

        public void updateAAD(byte[] bArr, int i, int i2) {
            throw new UnsupportedOperationException("AAD is not supported in the current mode.");
        }

        public boolean wrapOnNoPadding() {
            return !(this.cipher instanceof CTSBlockCipher);
        }
    }

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

    private static class InvalidKeyOrParametersException extends InvalidKeyException {
        private final Throwable cause;

        InvalidKeyOrParametersException(String str, Throwable th) {
            super(str);
            this.cause = th;
        }

        public Throwable getCause() {
            return this.cause;
        }
    }

    protected BaseBlockCipher(BlockCipher blockCipher) {
        this.availableSpecs = new Class[]{RC2ParameterSpec.class, RC5ParameterSpec.class, gcmSpecClass, GOST28147ParameterSpec.class, IvParameterSpec.class, PBEParameterSpec.class};
        this.scheme = -1;
        this.ivLength = 0;
        this.fixedIv = true;
        this.pbeSpec = null;
        this.pbeAlgorithm = null;
        this.modeName = null;
        this.baseEngine = blockCipher;
        this.cipher = new BufferedGenericBlockCipher(blockCipher);
    }

    protected BaseBlockCipher(BlockCipher blockCipher, int i) {
        this(blockCipher, true, i);
    }

    protected BaseBlockCipher(BlockCipher blockCipher, int i, int i2, int i3, int i4) {
        this.availableSpecs = new Class[]{RC2ParameterSpec.class, RC5ParameterSpec.class, gcmSpecClass, GOST28147ParameterSpec.class, IvParameterSpec.class, PBEParameterSpec.class};
        this.scheme = -1;
        this.ivLength = 0;
        this.fixedIv = true;
        this.pbeSpec = null;
        this.pbeAlgorithm = null;
        this.modeName = null;
        this.baseEngine = blockCipher;
        this.scheme = i;
        this.digest = i2;
        this.keySizeInBits = i3;
        this.ivLength = i4;
        this.cipher = new BufferedGenericBlockCipher(blockCipher);
    }

    protected BaseBlockCipher(BlockCipher blockCipher, boolean z, int i) {
        this.availableSpecs = new Class[]{RC2ParameterSpec.class, RC5ParameterSpec.class, gcmSpecClass, GOST28147ParameterSpec.class, IvParameterSpec.class, PBEParameterSpec.class};
        this.scheme = -1;
        this.ivLength = 0;
        this.fixedIv = true;
        this.pbeSpec = null;
        this.pbeAlgorithm = null;
        this.modeName = null;
        this.baseEngine = blockCipher;
        this.fixedIv = z;
        this.cipher = new BufferedGenericBlockCipher(blockCipher);
        this.ivLength = i / 8;
    }

    protected BaseBlockCipher(BufferedBlockCipher bufferedBlockCipher, int i) {
        this(bufferedBlockCipher, true, i);
    }

    protected BaseBlockCipher(BufferedBlockCipher bufferedBlockCipher, boolean z, int i) {
        this.availableSpecs = new Class[]{RC2ParameterSpec.class, RC5ParameterSpec.class, gcmSpecClass, GOST28147ParameterSpec.class, IvParameterSpec.class, PBEParameterSpec.class};
        this.scheme = -1;
        this.ivLength = 0;
        this.fixedIv = true;
        this.pbeSpec = null;
        this.pbeAlgorithm = null;
        this.modeName = null;
        this.baseEngine = bufferedBlockCipher.getUnderlyingCipher();
        this.cipher = new BufferedGenericBlockCipher(bufferedBlockCipher);
        this.fixedIv = z;
        this.ivLength = i / 8;
    }

    protected BaseBlockCipher(AEADBlockCipher aEADBlockCipher) {
        this.availableSpecs = new Class[]{RC2ParameterSpec.class, RC5ParameterSpec.class, gcmSpecClass, GOST28147ParameterSpec.class, IvParameterSpec.class, PBEParameterSpec.class};
        this.scheme = -1;
        this.ivLength = 0;
        this.fixedIv = true;
        this.pbeSpec = null;
        this.pbeAlgorithm = null;
        this.modeName = null;
        this.baseEngine = aEADBlockCipher.getUnderlyingCipher();
        this.ivLength = this.baseEngine.getBlockSize();
        this.cipher = new AEADGenericBlockCipher(aEADBlockCipher);
    }

    protected BaseBlockCipher(AEADBlockCipher aEADBlockCipher, boolean z, int i) {
        this.availableSpecs = new Class[]{RC2ParameterSpec.class, RC5ParameterSpec.class, gcmSpecClass, GOST28147ParameterSpec.class, IvParameterSpec.class, PBEParameterSpec.class};
        this.scheme = -1;
        this.ivLength = 0;
        this.fixedIv = true;
        this.pbeSpec = null;
        this.pbeAlgorithm = null;
        this.modeName = null;
        this.baseEngine = aEADBlockCipher.getUnderlyingCipher();
        this.fixedIv = z;
        this.ivLength = i;
        this.cipher = new AEADGenericBlockCipher(aEADBlockCipher);
    }

    protected BaseBlockCipher(BlockCipherProvider blockCipherProvider) {
        this.availableSpecs = new Class[]{RC2ParameterSpec.class, RC5ParameterSpec.class, gcmSpecClass, GOST28147ParameterSpec.class, IvParameterSpec.class, PBEParameterSpec.class};
        this.scheme = -1;
        this.ivLength = 0;
        this.fixedIv = true;
        this.pbeSpec = null;
        this.pbeAlgorithm = null;
        this.modeName = null;
        this.baseEngine = blockCipherProvider.get();
        this.engineProvider = blockCipherProvider;
        this.cipher = new BufferedGenericBlockCipher(blockCipherProvider.get());
    }

    private CipherParameters adjustParameters(AlgorithmParameterSpec algorithmParameterSpec, CipherParameters cipherParameters) {
        if (cipherParameters instanceof ParametersWithIV) {
            CipherParameters parameters = ((ParametersWithIV) cipherParameters).getParameters();
            if (algorithmParameterSpec instanceof IvParameterSpec) {
                this.ivParam = new ParametersWithIV(parameters, ((IvParameterSpec) algorithmParameterSpec).getIV());
            } else {
                if (algorithmParameterSpec instanceof GOST28147ParameterSpec) {
                    GOST28147ParameterSpec gOST28147ParameterSpec = (GOST28147ParameterSpec) algorithmParameterSpec;
                    ParametersWithSBox parametersWithSBox = new ParametersWithSBox(cipherParameters, gOST28147ParameterSpec.getSbox());
                    if (gOST28147ParameterSpec.getIV() == null || this.ivLength == 0) {
                        return parametersWithSBox;
                    }
                    this.ivParam = new ParametersWithIV(parameters, gOST28147ParameterSpec.getIV());
                    return this.ivParam;
                }
                return cipherParameters;
            }
        } else if (algorithmParameterSpec instanceof IvParameterSpec) {
            this.ivParam = new ParametersWithIV(cipherParameters, ((IvParameterSpec) algorithmParameterSpec).getIV());
        } else {
            if (algorithmParameterSpec instanceof GOST28147ParameterSpec) {
                GOST28147ParameterSpec gOST28147ParameterSpec2 = (GOST28147ParameterSpec) algorithmParameterSpec;
                ParametersWithSBox parametersWithSBox2 = new ParametersWithSBox(cipherParameters, gOST28147ParameterSpec2.getSbox());
                if (gOST28147ParameterSpec2.getIV() != null && this.ivLength != 0) {
                    return new ParametersWithIV(parametersWithSBox2, gOST28147ParameterSpec2.getIV());
                }
                cipherParameters = parametersWithSBox2;
            }
            return cipherParameters;
        }
        return this.ivParam;
    }

    private boolean isAEADModeName(String str) {
        return "CCM".equals(str) || "EAX".equals(str) || "GCM".equals(str) || "OCB".equals(str);
    }

    /* access modifiers changed from: protected */
    public int engineDoFinal(byte[] bArr, int i, int i2, byte[] bArr2, int i3) throws IllegalBlockSizeException, BadPaddingException, ShortBufferException {
        int i4;
        if (engineGetOutputSize(i2) + i3 <= bArr2.length) {
            if (i2 != 0) {
                try {
                    i4 = this.cipher.processBytes(bArr, i, i2, bArr2, i3);
                } catch (OutputLengthException e) {
                    throw new IllegalBlockSizeException(e.getMessage());
                } catch (DataLengthException e2) {
                    throw new IllegalBlockSizeException(e2.getMessage());
                }
            } else {
                i4 = 0;
            }
            return i4 + this.cipher.doFinal(bArr2, i3 + i4);
        }
        throw new ShortBufferException("output buffer too short for input.");
    }

    /* access modifiers changed from: protected */
    public byte[] engineDoFinal(byte[] bArr, int i, int i2) throws IllegalBlockSizeException, BadPaddingException {
        byte[] bArr2 = new byte[engineGetOutputSize(i2)];
        int processBytes = i2 != 0 ? this.cipher.processBytes(bArr, i, i2, bArr2, 0) : 0;
        try {
            int doFinal = processBytes + this.cipher.doFinal(bArr2, processBytes);
            if (doFinal == bArr2.length) {
                return bArr2;
            }
            byte[] bArr3 = new byte[doFinal];
            System.arraycopy(bArr2, 0, bArr3, 0, doFinal);
            return bArr3;
        } catch (DataLengthException e) {
            throw new IllegalBlockSizeException(e.getMessage());
        }
    }

    /* access modifiers changed from: protected */
    public int engineGetBlockSize() {
        return this.baseEngine.getBlockSize();
    }

    /* access modifiers changed from: protected */
    public byte[] engineGetIV() {
        if (this.aeadParams != null) {
            return this.aeadParams.getNonce();
        }
        if (this.ivParam != null) {
            return this.ivParam.getIV();
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public int engineGetKeySize(Key key) {
        return key.getEncoded().length * 8;
    }

    /* access modifiers changed from: protected */
    public int engineGetOutputSize(int i) {
        return this.cipher.getOutputSize(i);
    }

    /* access modifiers changed from: protected */
    public AlgorithmParameters engineGetParameters() {
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
                String algorithmName = this.cipher.getUnderlyingCipher().getAlgorithmName();
                if (algorithmName.indexOf(47) >= 0) {
                    algorithmName = algorithmName.substring(0, algorithmName.indexOf(47));
                }
                try {
                    this.engineParams = createParametersInstance(algorithmName);
                    this.engineParams.init(new IvParameterSpec(this.ivParam.getIV()));
                } catch (Exception e3) {
                    throw new RuntimeException(e3.toString());
                }
            }
        }
        return this.engineParams;
    }

    /* access modifiers changed from: protected */
    public void engineInit(int i, Key key, AlgorithmParameters algorithmParameters, SecureRandom secureRandom) throws InvalidKeyException, InvalidAlgorithmParameterException {
        AlgorithmParameterSpec algorithmParameterSpec = null;
        if (algorithmParameters != null) {
            int i2 = 0;
            while (true) {
                if (i2 == this.availableSpecs.length) {
                    break;
                }
                if (this.availableSpecs[i2] != null) {
                    try {
                        algorithmParameterSpec = algorithmParameters.getParameterSpec(this.availableSpecs[i2]);
                        break;
                    } catch (Exception e) {
                    }
                }
                i2++;
            }
            if (algorithmParameterSpec == null) {
                throw new InvalidAlgorithmParameterException("can't handle parameter " + algorithmParameters.toString());
            }
        }
        engineInit(i, key, algorithmParameterSpec, secureRandom);
        this.engineParams = algorithmParameters;
    }

    /* access modifiers changed from: protected */
    public void engineInit(int i, Key key, SecureRandom secureRandom) throws InvalidKeyException {
        try {
            engineInit(i, key, (AlgorithmParameterSpec) null, secureRandom);
        } catch (InvalidAlgorithmParameterException e) {
            throw new InvalidKeyException(e.getMessage());
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v2, resolved type: org.bouncycastle.crypto.params.ParametersWithIV} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v3, resolved type: org.bouncycastle.crypto.params.ParametersWithIV} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v7, resolved type: org.bouncycastle.crypto.params.ParametersWithRandom} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v8, resolved type: org.bouncycastle.crypto.params.ParametersWithIV} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v12, resolved type: org.bouncycastle.crypto.params.ParametersWithIV} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v5, resolved type: org.bouncycastle.crypto.params.AEADParameters} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v14, resolved type: org.bouncycastle.crypto.params.ParametersWithIV} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v54, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v56, resolved type: org.bouncycastle.crypto.CipherParameters} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v57, resolved type: org.bouncycastle.crypto.params.RC5Parameters} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v58, resolved type: org.bouncycastle.crypto.params.RC5Parameters} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v59, resolved type: org.bouncycastle.crypto.params.RC2Parameters} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v60, resolved type: org.bouncycastle.crypto.params.RC2Parameters} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v61, resolved type: org.bouncycastle.crypto.params.ParametersWithSBox} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v62, resolved type: org.bouncycastle.crypto.params.ParametersWithSBox} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v63, resolved type: org.bouncycastle.crypto.params.ParametersWithIV} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v64, resolved type: org.bouncycastle.crypto.params.AEADParameters} */
    /* JADX WARNING: type inference failed for: r5v0 */
    /* JADX WARNING: type inference failed for: r5v1, types: [org.bouncycastle.crypto.CipherParameters] */
    /* JADX WARNING: type inference failed for: r5v2, types: [org.bouncycastle.crypto.CipherParameters] */
    /* JADX WARNING: type inference failed for: r5v3 */
    /* JADX WARNING: type inference failed for: r5v65 */
    /* JADX WARNING: type inference failed for: r5v68 */
    /* JADX WARNING: type inference failed for: r5v69 */
    /* JADX WARNING: type inference failed for: r5v70 */
    /* JADX WARNING: type inference failed for: r5v72 */
    /* JADX WARNING: type inference failed for: r5v77 */
    /* JADX WARNING: type inference failed for: r5v78 */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x00a3, code lost:
        if (r6 != false) goto L_0x00a5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x00f1, code lost:
        if (r6 != false) goto L_0x00a5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x013b, code lost:
        if (r6 != false) goto L_0x00a5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:99:0x01fc, code lost:
        if (r6 != false) goto L_0x00a5;
     */
    /* JADX WARNING: Failed to insert additional move for type inference */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Removed duplicated region for block: B:102:0x0205  */
    /* JADX WARNING: Removed duplicated region for block: B:113:0x0244  */
    /* JADX WARNING: Removed duplicated region for block: B:215:0x048a  */
    /* JADX WARNING: Removed duplicated region for block: B:216:0x048f  */
    /* JADX WARNING: Removed duplicated region for block: B:231:0x04d3 A[SYNTHETIC, Splitter:B:231:0x04d3] */
    /* JADX WARNING: Removed duplicated region for block: B:233:0x04d6 A[Catch:{ Exception -> 0x052a }] */
    /* JADX WARNING: Removed duplicated region for block: B:234:0x04dc A[Catch:{ Exception -> 0x052a }] */
    /* JADX WARNING: Removed duplicated region for block: B:237:0x04e7 A[Catch:{ Exception -> 0x052a }] */
    /* JADX WARNING: Removed duplicated region for block: B:249:? A[ADDED_TO_REGION, Catch:{ Exception -> 0x052a }, RETURN, SYNTHETIC] */
    public void engineInit(int i, Key key, AlgorithmParameterSpec algorithmParameterSpec, SecureRandom secureRandom) throws InvalidKeyException, InvalidAlgorithmParameterException {
        ParametersWithIV parametersWithIV;
        SecureRandom secureRandom2;
        ParametersWithIV parametersWithIV2;
        CipherParameters cipherParameters;
        CipherParameters cipherParameters2;
        CipherParameters cipherParameters3;
        int i2 = i;
        Key key2 = key;
        AlgorithmParameterSpec algorithmParameterSpec2 = algorithmParameterSpec;
        SecureRandom secureRandom3 = secureRandom;
        AEADParameters aEADParameters = 0;
        this.pbeSpec = null;
        this.pbeAlgorithm = null;
        this.engineParams = null;
        this.aeadParams = null;
        if (!(key2 instanceof SecretKey)) {
            StringBuilder sb = new StringBuilder();
            sb.append("Key for algorithm ");
            if (key2 != null) {
                aEADParameters = key.getAlgorithm();
            }
            sb.append(aEADParameters);
            sb.append(" not suitable for symmetric enryption.");
            throw new InvalidKeyException(sb.toString());
        } else if (algorithmParameterSpec2 != null || !this.baseEngine.getAlgorithmName().startsWith("RC5-64")) {
            if (this.scheme == 2 || (key2 instanceof PKCS12Key)) {
                try {
                    SecretKey secretKey = (SecretKey) key2;
                    if (algorithmParameterSpec2 instanceof PBEParameterSpec) {
                        this.pbeSpec = (PBEParameterSpec) algorithmParameterSpec2;
                    }
                    boolean z = secretKey instanceof PBEKey;
                    if (z && this.pbeSpec == null) {
                        PBEKey pBEKey = (PBEKey) secretKey;
                        if (pBEKey.getSalt() != null) {
                            this.pbeSpec = new PBEParameterSpec(pBEKey.getSalt(), pBEKey.getIterationCount());
                        } else {
                            throw new InvalidAlgorithmParameterException("PBEKey requires parameters to specify salt");
                        }
                    }
                    if (this.pbeSpec != null || z) {
                        if (key2 instanceof BCPBEKey) {
                            CipherParameters param = ((BCPBEKey) key2).getParam();
                            if (!(param instanceof ParametersWithIV)) {
                                if (param == null) {
                                    param = PBE.Util.makePBEParameters(secretKey.getEncoded(), 2, this.digest, this.keySizeInBits, this.ivLength * 8, this.pbeSpec, this.cipher.getAlgorithmName());
                                } else {
                                    throw new InvalidKeyException("Algorithm requires a PBE key suitable for PKCS12");
                                }
                            }
                            cipherParameters = param;
                        } else {
                            cipherParameters = PBE.Util.makePBEParameters(secretKey.getEncoded(), 2, this.digest, this.keySizeInBits, this.ivLength * 8, this.pbeSpec, this.cipher.getAlgorithmName());
                        }
                        boolean z2 = cipherParameters instanceof ParametersWithIV;
                        aEADParameters = cipherParameters;
                        cipherParameters2 = cipherParameters;
                    } else {
                        throw new InvalidKeyException("Algorithm requires a PBE key");
                    }
                } catch (Exception e) {
                    throw new InvalidKeyException("PKCS12 requires a SecretKey/PBEKey");
                }
            } else if (key2 instanceof PBKDF1Key) {
                PBKDF1Key pBKDF1Key = (PBKDF1Key) key2;
                if (algorithmParameterSpec2 instanceof PBEParameterSpec) {
                    this.pbeSpec = (PBEParameterSpec) algorithmParameterSpec2;
                }
                if ((pBKDF1Key instanceof PBKDF1KeyWithParameters) && this.pbeSpec == null) {
                    PBKDF1KeyWithParameters pBKDF1KeyWithParameters = (PBKDF1KeyWithParameters) pBKDF1Key;
                    this.pbeSpec = new PBEParameterSpec(pBKDF1KeyWithParameters.getSalt(), pBKDF1KeyWithParameters.getIterationCount());
                }
                CipherParameters makePBEParameters = PBE.Util.makePBEParameters(pBKDF1Key.getEncoded(), 0, this.digest, this.keySizeInBits, this.ivLength * 8, this.pbeSpec, this.cipher.getAlgorithmName());
                boolean z3 = makePBEParameters instanceof ParametersWithIV;
                cipherParameters2 = makePBEParameters;
                aEADParameters = makePBEParameters;
            } else if (key2 instanceof BCPBEKey) {
                BCPBEKey bCPBEKey = (BCPBEKey) key2;
                this.pbeAlgorithm = bCPBEKey.getOID() != null ? bCPBEKey.getOID().getId() : bCPBEKey.getAlgorithm();
                if (bCPBEKey.getParam() != null) {
                    cipherParameters3 = adjustParameters(algorithmParameterSpec2, bCPBEKey.getParam());
                } else if (algorithmParameterSpec2 instanceof PBEParameterSpec) {
                    this.pbeSpec = (PBEParameterSpec) algorithmParameterSpec2;
                    cipherParameters3 = PBE.Util.makePBEParameters(bCPBEKey, algorithmParameterSpec2, this.cipher.getUnderlyingCipher().getAlgorithmName());
                } else {
                    throw new InvalidAlgorithmParameterException("PBE requires PBE parameters to be set.");
                }
                boolean z4 = cipherParameters3 instanceof ParametersWithIV;
                aEADParameters = cipherParameters3;
                cipherParameters2 = cipherParameters3;
            } else {
                if (key2 instanceof PBEKey) {
                    PBEKey pBEKey2 = (PBEKey) key2;
                    this.pbeSpec = (PBEParameterSpec) algorithmParameterSpec2;
                    if ((pBEKey2 instanceof PKCS12KeyWithParameters) && this.pbeSpec == null) {
                        this.pbeSpec = new PBEParameterSpec(pBEKey2.getSalt(), pBEKey2.getIterationCount());
                    }
                    CipherParameters makePBEParameters2 = PBE.Util.makePBEParameters(pBEKey2.getEncoded(), this.scheme, this.digest, this.keySizeInBits, this.ivLength * 8, this.pbeSpec, this.cipher.getAlgorithmName());
                    boolean z5 = makePBEParameters2 instanceof ParametersWithIV;
                    aEADParameters = makePBEParameters2;
                    cipherParameters2 = makePBEParameters2;
                } else if (!(key2 instanceof RepeatedSecretKeySpec)) {
                    if (this.scheme == 0 || this.scheme == 4 || this.scheme == 1 || this.scheme == 5) {
                        throw new InvalidKeyException("Algorithm requires a PBE key");
                    }
                    aEADParameters = new KeyParameter(key.getEncoded());
                }
                if (!(algorithmParameterSpec2 instanceof AEADParameterSpec)) {
                    if (isAEADModeName(this.modeName) || (this.cipher instanceof AEADGenericBlockCipher)) {
                        AEADParameterSpec aEADParameterSpec = (AEADParameterSpec) algorithmParameterSpec2;
                        AEADParameters aEADParameters2 = new AEADParameters(aEADParameters instanceof ParametersWithIV ? (KeyParameter) ((ParametersWithIV) aEADParameters).getParameters() : aEADParameters, aEADParameterSpec.getMacSizeInBits(), aEADParameterSpec.getNonce(), aEADParameterSpec.getAssociatedData());
                        this.aeadParams = aEADParameters2;
                        aEADParameters = aEADParameters2;
                    } else {
                        throw new InvalidAlgorithmParameterException("AEADParameterSpec can only be used with AEAD modes.");
                    }
                } else if (!(algorithmParameterSpec2 instanceof IvParameterSpec)) {
                    if (algorithmParameterSpec2 instanceof GOST28147ParameterSpec) {
                        GOST28147ParameterSpec gOST28147ParameterSpec = (GOST28147ParameterSpec) algorithmParameterSpec2;
                        ParametersWithSBox parametersWithSBox = new ParametersWithSBox(new KeyParameter(key.getEncoded()), gOST28147ParameterSpec.getSbox());
                        aEADParameters = parametersWithSBox;
                        if (gOST28147ParameterSpec.getIV() != null) {
                            aEADParameters = parametersWithSBox;
                            if (this.ivLength != 0) {
                                parametersWithIV2 = parametersWithSBox instanceof ParametersWithIV ? new ParametersWithIV(((ParametersWithIV) parametersWithSBox).getParameters(), gOST28147ParameterSpec.getIV()) : new ParametersWithIV(parametersWithSBox, gOST28147ParameterSpec.getIV());
                            }
                        }
                    } else if (algorithmParameterSpec2 instanceof RC2ParameterSpec) {
                        RC2ParameterSpec rC2ParameterSpec = (RC2ParameterSpec) algorithmParameterSpec2;
                        RC2Parameters rC2Parameters = new RC2Parameters(key.getEncoded(), rC2ParameterSpec.getEffectiveKeyBits());
                        aEADParameters = rC2Parameters;
                        if (rC2ParameterSpec.getIV() != null) {
                            aEADParameters = rC2Parameters;
                            if (this.ivLength != 0) {
                                parametersWithIV2 = rC2Parameters instanceof ParametersWithIV ? new ParametersWithIV(((ParametersWithIV) rC2Parameters).getParameters(), rC2ParameterSpec.getIV()) : new ParametersWithIV(rC2Parameters, rC2ParameterSpec.getIV());
                            }
                        }
                    } else if (algorithmParameterSpec2 instanceof RC5ParameterSpec) {
                        RC5ParameterSpec rC5ParameterSpec = (RC5ParameterSpec) algorithmParameterSpec2;
                        RC5Parameters rC5Parameters = new RC5Parameters(key.getEncoded(), rC5ParameterSpec.getRounds());
                        if (this.baseEngine.getAlgorithmName().startsWith("RC5")) {
                            if (this.baseEngine.getAlgorithmName().equals("RC5-32")) {
                                if (rC5ParameterSpec.getWordSize() != 32) {
                                    throw new InvalidAlgorithmParameterException("RC5 already set up for a word size of 32 not " + rC5ParameterSpec.getWordSize() + ".");
                                }
                            } else if (this.baseEngine.getAlgorithmName().equals("RC5-64") && rC5ParameterSpec.getWordSize() != 64) {
                                throw new InvalidAlgorithmParameterException("RC5 already set up for a word size of 64 not " + rC5ParameterSpec.getWordSize() + ".");
                            }
                            aEADParameters = rC5Parameters;
                            if (rC5ParameterSpec.getIV() != null) {
                                aEADParameters = rC5Parameters;
                                if (this.ivLength != 0) {
                                    parametersWithIV2 = rC5Parameters instanceof ParametersWithIV ? new ParametersWithIV(((ParametersWithIV) rC5Parameters).getParameters(), rC5ParameterSpec.getIV()) : new ParametersWithIV(rC5Parameters, rC5ParameterSpec.getIV());
                                }
                            }
                        } else {
                            throw new InvalidAlgorithmParameterException("RC5 parameters passed to a cipher that is not RC5.");
                        }
                    } else if (gcmSpecClass == null || !gcmSpecClass.isInstance(algorithmParameterSpec2)) {
                        if (algorithmParameterSpec2 != null && !(algorithmParameterSpec2 instanceof PBEParameterSpec)) {
                            throw new InvalidAlgorithmParameterException("unknown parameter type.");
                        }
                    } else if (isAEADModeName(this.modeName) || (this.cipher instanceof AEADGenericBlockCipher)) {
                        try {
                            Method declaredMethod = gcmSpecClass.getDeclaredMethod("getTLen", new Class[0]);
                            Method declaredMethod2 = gcmSpecClass.getDeclaredMethod("getIV", new Class[0]);
                            if (aEADParameters instanceof ParametersWithIV) {
                                aEADParameters = ((ParametersWithIV) aEADParameters).getParameters();
                            }
                            AEADParameters aEADParameters3 = new AEADParameters((KeyParameter) aEADParameters, ((Integer) declaredMethod.invoke(algorithmParameterSpec2, new Object[0])).intValue(), (byte[]) declaredMethod2.invoke(algorithmParameterSpec2, new Object[0]));
                            this.aeadParams = aEADParameters3;
                            aEADParameters = aEADParameters3;
                        } catch (Exception e2) {
                            throw new InvalidAlgorithmParameterException("Cannot process GCMParameterSpec.");
                        }
                    } else {
                        throw new InvalidAlgorithmParameterException("GCMParameterSpec can only be used with AEAD modes.");
                    }
                    this.ivParam = parametersWithIV2;
                    aEADParameters = parametersWithIV2;
                } else if (this.ivLength != 0) {
                    IvParameterSpec ivParameterSpec = (IvParameterSpec) algorithmParameterSpec2;
                    if (ivParameterSpec.getIV().length == this.ivLength || (this.cipher instanceof AEADGenericBlockCipher) || !this.fixedIv) {
                        ParametersWithIV parametersWithIV3 = aEADParameters instanceof ParametersWithIV ? new ParametersWithIV(((ParametersWithIV) aEADParameters).getParameters(), ivParameterSpec.getIV()) : new ParametersWithIV(aEADParameters, ivParameterSpec.getIV());
                        this.ivParam = parametersWithIV3;
                        aEADParameters = parametersWithIV3;
                    } else {
                        throw new InvalidAlgorithmParameterException("IV must be " + this.ivLength + " bytes long.");
                    }
                } else if (this.modeName != null && this.modeName.equals("ECB")) {
                    throw new InvalidAlgorithmParameterException("ECB mode does not use an IV");
                }
                if (this.ivLength != 0 && !(aEADParameters instanceof ParametersWithIV) && !(aEADParameters instanceof AEADParameters)) {
                    secureRandom2 = secureRandom3 != null ? CryptoServicesRegistrar.getSecureRandom() : secureRandom3;
                    if (i2 != 1 || i2 == 3) {
                        byte[] bArr = new byte[this.ivLength];
                        secureRandom2.nextBytes(bArr);
                        parametersWithIV = new ParametersWithIV(aEADParameters, bArr);
                        this.ivParam = parametersWithIV;
                        if (secureRandom3 != null && this.padded) {
                            parametersWithIV = new ParametersWithRandom(parametersWithIV, secureRandom3);
                        }
                        switch (i2) {
                            case 1:
                            case 3:
                                this.cipher.init(true, parametersWithIV);
                                break;
                            case 2:
                            case 4:
                                this.cipher.init(false, parametersWithIV);
                                break;
                            default:
                                try {
                                    throw new InvalidParameterException("unknown opmode " + i2 + " passed");
                                } catch (Exception e3) {
                                    throw new InvalidKeyOrParametersException(e3.getMessage(), e3);
                                }
                        }
                        if (!(this.cipher instanceof AEADGenericBlockCipher) && this.aeadParams == null) {
                            this.aeadParams = new AEADParameters((KeyParameter) this.ivParam.getParameters(), ((AEADGenericBlockCipher) this.cipher).cipher.getMac().length * 8, this.ivParam.getIV());
                            return;
                        }
                        return;
                    } else if (this.cipher.getUnderlyingCipher().getAlgorithmName().indexOf("PGPCFB") < 0) {
                        throw new InvalidAlgorithmParameterException("no IV set when one expected");
                    }
                }
                parametersWithIV = aEADParameters;
                parametersWithIV = new ParametersWithRandom(parametersWithIV, secureRandom3);
                switch (i2) {
                    case 1:
                    case 3:
                        break;
                    case 2:
                    case 4:
                        break;
                }
                if (!(this.cipher instanceof AEADGenericBlockCipher)) {
                    return;
                }
                return;
            }
            this.ivParam = (ParametersWithIV) cipherParameters2;
            aEADParameters = cipherParameters2;
            if (!(algorithmParameterSpec2 instanceof AEADParameterSpec)) {
            }
            if (secureRandom3 != null) {
            }
            if (i2 != 1) {
            }
            byte[] bArr2 = new byte[this.ivLength];
            secureRandom2.nextBytes(bArr2);
            parametersWithIV = new ParametersWithIV(aEADParameters, bArr2);
            this.ivParam = parametersWithIV;
            parametersWithIV = new ParametersWithRandom(parametersWithIV, secureRandom3);
            switch (i2) {
                case 1:
                case 3:
                    break;
                case 2:
                case 4:
                    break;
            }
            if (!(this.cipher instanceof AEADGenericBlockCipher)) {
            }
        } else {
            throw new InvalidAlgorithmParameterException("RC5 requires an RC5ParametersSpec to be passed in.");
        }
    }

    /* access modifiers changed from: protected */
    public void engineSetMode(String str) throws NoSuchAlgorithmException {
        GenericBlockCipher aEADGenericBlockCipher;
        BufferedGenericBlockCipher bufferedGenericBlockCipher;
        this.modeName = Strings.toUpperCase(str);
        if (this.modeName.equals("ECB")) {
            this.ivLength = 0;
            aEADGenericBlockCipher = new BufferedGenericBlockCipher(this.baseEngine);
        } else if (this.modeName.equals("CBC")) {
            this.ivLength = this.baseEngine.getBlockSize();
            aEADGenericBlockCipher = new BufferedGenericBlockCipher((BlockCipher) new CBCBlockCipher(this.baseEngine));
        } else {
            if (this.modeName.startsWith("OFB")) {
                this.ivLength = this.baseEngine.getBlockSize();
                if (this.modeName.length() != 3) {
                    bufferedGenericBlockCipher = new BufferedGenericBlockCipher((BlockCipher) new OFBBlockCipher(this.baseEngine, Integer.parseInt(this.modeName.substring(3))));
                } else {
                    aEADGenericBlockCipher = new BufferedGenericBlockCipher((BlockCipher) new OFBBlockCipher(this.baseEngine, 8 * this.baseEngine.getBlockSize()));
                }
            } else if (this.modeName.startsWith("CFB")) {
                this.ivLength = this.baseEngine.getBlockSize();
                if (this.modeName.length() != 3) {
                    bufferedGenericBlockCipher = new BufferedGenericBlockCipher((BlockCipher) new CFBBlockCipher(this.baseEngine, Integer.parseInt(this.modeName.substring(3))));
                } else {
                    aEADGenericBlockCipher = new BufferedGenericBlockCipher((BlockCipher) new CFBBlockCipher(this.baseEngine, 8 * this.baseEngine.getBlockSize()));
                }
            } else if (this.modeName.startsWith("PGP")) {
                boolean equalsIgnoreCase = this.modeName.equalsIgnoreCase("PGPCFBwithIV");
                this.ivLength = this.baseEngine.getBlockSize();
                bufferedGenericBlockCipher = new BufferedGenericBlockCipher((BlockCipher) new PGPCFBBlockCipher(this.baseEngine, equalsIgnoreCase));
            } else if (this.modeName.equalsIgnoreCase("OpenPGPCFB")) {
                this.ivLength = 0;
                aEADGenericBlockCipher = new BufferedGenericBlockCipher((BlockCipher) new OpenPGPCFBBlockCipher(this.baseEngine));
            } else if (this.modeName.startsWith("SIC")) {
                this.ivLength = this.baseEngine.getBlockSize();
                if (this.ivLength >= 16) {
                    this.fixedIv = false;
                    aEADGenericBlockCipher = new BufferedGenericBlockCipher(new BufferedBlockCipher(new SICBlockCipher(this.baseEngine)));
                } else {
                    throw new IllegalArgumentException("Warning: SIC-Mode can become a twotime-pad if the blocksize of the cipher is too small. Use a cipher with a block size of at least 128 bits (e.g. AES)");
                }
            } else if (this.modeName.startsWith("CTR")) {
                this.ivLength = this.baseEngine.getBlockSize();
                this.fixedIv = false;
                aEADGenericBlockCipher = this.baseEngine instanceof DSTU7624Engine ? new BufferedGenericBlockCipher(new BufferedBlockCipher(new KCTRBlockCipher(this.baseEngine))) : new BufferedGenericBlockCipher(new BufferedBlockCipher(new SICBlockCipher(this.baseEngine)));
            } else if (this.modeName.startsWith("GOFB")) {
                this.ivLength = this.baseEngine.getBlockSize();
                aEADGenericBlockCipher = new BufferedGenericBlockCipher(new BufferedBlockCipher(new GOFBBlockCipher(this.baseEngine)));
            } else if (this.modeName.startsWith("GCFB")) {
                this.ivLength = this.baseEngine.getBlockSize();
                aEADGenericBlockCipher = new BufferedGenericBlockCipher(new BufferedBlockCipher(new GCFBBlockCipher(this.baseEngine)));
            } else if (this.modeName.startsWith("CTS")) {
                this.ivLength = this.baseEngine.getBlockSize();
                aEADGenericBlockCipher = new BufferedGenericBlockCipher((BufferedBlockCipher) new CTSBlockCipher(new CBCBlockCipher(this.baseEngine)));
            } else if (this.modeName.startsWith("CCM")) {
                this.ivLength = 12;
                aEADGenericBlockCipher = this.baseEngine instanceof DSTU7624Engine ? new AEADGenericBlockCipher(new KCCMBlockCipher(this.baseEngine)) : new AEADGenericBlockCipher(new CCMBlockCipher(this.baseEngine));
            } else if (this.modeName.startsWith("OCB")) {
                if (this.engineProvider != null) {
                    this.ivLength = 15;
                    aEADGenericBlockCipher = new AEADGenericBlockCipher(new OCBBlockCipher(this.baseEngine, this.engineProvider.get()));
                } else {
                    throw new NoSuchAlgorithmException("can't support mode " + str);
                }
            } else if (this.modeName.startsWith("EAX")) {
                this.ivLength = this.baseEngine.getBlockSize();
                aEADGenericBlockCipher = new AEADGenericBlockCipher(new EAXBlockCipher(this.baseEngine));
            } else if (this.modeName.startsWith("GCM")) {
                this.ivLength = this.baseEngine.getBlockSize();
                aEADGenericBlockCipher = this.baseEngine instanceof DSTU7624Engine ? new AEADGenericBlockCipher(new KGCMBlockCipher(this.baseEngine)) : new AEADGenericBlockCipher(new GCMBlockCipher(this.baseEngine));
            } else {
                throw new NoSuchAlgorithmException("can't support mode " + str);
            }
            this.cipher = bufferedGenericBlockCipher;
            return;
        }
        this.cipher = aEADGenericBlockCipher;
    }

    /* access modifiers changed from: protected */
    public void engineSetPadding(String str) throws NoSuchPaddingException {
        BufferedGenericBlockCipher bufferedGenericBlockCipher;
        String upperCase = Strings.toUpperCase(str);
        if (upperCase.equals("NOPADDING")) {
            if (this.cipher.wrapOnNoPadding()) {
                bufferedGenericBlockCipher = new BufferedGenericBlockCipher(new BufferedBlockCipher(this.cipher.getUnderlyingCipher()));
            } else {
                return;
            }
        } else if (upperCase.equals("WITHCTS") || upperCase.equals("CTSPADDING") || upperCase.equals("CS3PADDING")) {
            bufferedGenericBlockCipher = new BufferedGenericBlockCipher((BufferedBlockCipher) new CTSBlockCipher(this.cipher.getUnderlyingCipher()));
        } else {
            this.padded = true;
            if (isAEADModeName(this.modeName)) {
                throw new NoSuchPaddingException("Only NoPadding can be used with AEAD modes.");
            } else if (upperCase.equals("PKCS5PADDING") || upperCase.equals("PKCS7PADDING")) {
                bufferedGenericBlockCipher = new BufferedGenericBlockCipher(this.cipher.getUnderlyingCipher());
            } else if (upperCase.equals("ZEROBYTEPADDING")) {
                bufferedGenericBlockCipher = new BufferedGenericBlockCipher(this.cipher.getUnderlyingCipher(), new ZeroBytePadding());
            } else if (upperCase.equals("ISO10126PADDING") || upperCase.equals("ISO10126-2PADDING")) {
                bufferedGenericBlockCipher = new BufferedGenericBlockCipher(this.cipher.getUnderlyingCipher(), new ISO10126d2Padding());
            } else if (upperCase.equals("X9.23PADDING") || upperCase.equals("X923PADDING")) {
                bufferedGenericBlockCipher = new BufferedGenericBlockCipher(this.cipher.getUnderlyingCipher(), new X923Padding());
            } else if (upperCase.equals("ISO7816-4PADDING") || upperCase.equals("ISO9797-1PADDING")) {
                bufferedGenericBlockCipher = new BufferedGenericBlockCipher(this.cipher.getUnderlyingCipher(), new ISO7816d4Padding());
            } else if (upperCase.equals("TBCPADDING")) {
                bufferedGenericBlockCipher = new BufferedGenericBlockCipher(this.cipher.getUnderlyingCipher(), new TBCPadding());
            } else {
                throw new NoSuchPaddingException("Padding " + str + " unknown.");
            }
        }
        this.cipher = bufferedGenericBlockCipher;
    }

    /* access modifiers changed from: protected */
    public int engineUpdate(byte[] bArr, int i, int i2, byte[] bArr2, int i3) throws ShortBufferException {
        if (this.cipher.getUpdateOutputSize(i2) + i3 <= bArr2.length) {
            try {
                return this.cipher.processBytes(bArr, i, i2, bArr2, i3);
            } catch (DataLengthException e) {
                throw new IllegalStateException(e.toString());
            }
        } else {
            throw new ShortBufferException("output buffer too short for input.");
        }
    }

    /* access modifiers changed from: protected */
    public byte[] engineUpdate(byte[] bArr, int i, int i2) {
        int updateOutputSize = this.cipher.getUpdateOutputSize(i2);
        if (updateOutputSize > 0) {
            byte[] bArr2 = new byte[updateOutputSize];
            int processBytes = this.cipher.processBytes(bArr, i, i2, bArr2, 0);
            if (processBytes == 0) {
                return null;
            }
            if (processBytes == bArr2.length) {
                return bArr2;
            }
            byte[] bArr3 = new byte[processBytes];
            System.arraycopy(bArr2, 0, bArr3, 0, processBytes);
            return bArr3;
        }
        this.cipher.processBytes(bArr, i, i2, null, 0);
        return null;
    }

    /* access modifiers changed from: protected */
    public void engineUpdateAAD(ByteBuffer byteBuffer) {
        engineUpdateAAD(byteBuffer.array(), byteBuffer.arrayOffset() + byteBuffer.position(), byteBuffer.limit() - byteBuffer.position());
    }

    /* access modifiers changed from: protected */
    public void engineUpdateAAD(byte[] bArr, int i, int i2) {
        this.cipher.updateAAD(bArr, i, i2);
    }
}
