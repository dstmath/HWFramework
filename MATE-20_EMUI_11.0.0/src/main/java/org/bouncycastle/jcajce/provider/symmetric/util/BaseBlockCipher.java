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
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.cms.GCMParameters;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.CryptoServicesRegistrar;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.OutputLengthException;
import org.bouncycastle.crypto.engines.DSTU7624Engine;
import org.bouncycastle.crypto.modes.AEADBlockCipher;
import org.bouncycastle.crypto.modes.AEADCipher;
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
import org.bouncycastle.jcajce.provider.symmetric.util.BaseWrapCipher;
import org.bouncycastle.jcajce.provider.symmetric.util.PBE;
import org.bouncycastle.jcajce.spec.AEADParameterSpec;
import org.bouncycastle.jcajce.spec.GOST28147ParameterSpec;
import org.bouncycastle.jcajce.spec.RepeatedSecretKeySpec;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Strings;

public class BaseBlockCipher extends BaseWrapCipher implements PBE {
    private static final int BUF_SIZE = 512;
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

    /* access modifiers changed from: private */
    public static class AEADGenericBlockCipher implements GenericBlockCipher {
        private static final Constructor aeadBadTagConstructor;
        private AEADCipher cipher;

        static {
            Class loadClass = ClassUtil.loadClass(BaseBlockCipher.class, "javax.crypto.AEADBadTagException");
            aeadBadTagConstructor = loadClass != null ? findExceptionConstructor(loadClass) : null;
        }

        AEADGenericBlockCipher(AEADCipher aEADCipher) {
            this.cipher = aEADCipher;
        }

        private static Constructor findExceptionConstructor(Class cls) {
            try {
                return cls.getConstructor(String.class);
            } catch (Exception e) {
                return null;
            }
        }

        @Override // org.bouncycastle.jcajce.provider.symmetric.util.BaseBlockCipher.GenericBlockCipher
        public int doFinal(byte[] bArr, int i) throws IllegalStateException, BadPaddingException {
            BadPaddingException badPaddingException;
            try {
                return this.cipher.doFinal(bArr, i);
            } catch (InvalidCipherTextException e) {
                Constructor constructor = aeadBadTagConstructor;
                if (constructor != null) {
                    try {
                        badPaddingException = (BadPaddingException) constructor.newInstance(e.getMessage());
                    } catch (Exception e2) {
                        badPaddingException = null;
                    }
                    if (badPaddingException != null) {
                        throw badPaddingException;
                    }
                }
                throw new BadPaddingException(e.getMessage());
            }
        }

        @Override // org.bouncycastle.jcajce.provider.symmetric.util.BaseBlockCipher.GenericBlockCipher
        public String getAlgorithmName() {
            AEADCipher aEADCipher = this.cipher;
            return aEADCipher instanceof AEADBlockCipher ? ((AEADBlockCipher) aEADCipher).getUnderlyingCipher().getAlgorithmName() : aEADCipher.getAlgorithmName();
        }

        @Override // org.bouncycastle.jcajce.provider.symmetric.util.BaseBlockCipher.GenericBlockCipher
        public int getOutputSize(int i) {
            return this.cipher.getOutputSize(i);
        }

        @Override // org.bouncycastle.jcajce.provider.symmetric.util.BaseBlockCipher.GenericBlockCipher
        public BlockCipher getUnderlyingCipher() {
            AEADCipher aEADCipher = this.cipher;
            if (aEADCipher instanceof AEADBlockCipher) {
                return ((AEADBlockCipher) aEADCipher).getUnderlyingCipher();
            }
            return null;
        }

        @Override // org.bouncycastle.jcajce.provider.symmetric.util.BaseBlockCipher.GenericBlockCipher
        public int getUpdateOutputSize(int i) {
            return this.cipher.getUpdateOutputSize(i);
        }

        @Override // org.bouncycastle.jcajce.provider.symmetric.util.BaseBlockCipher.GenericBlockCipher
        public void init(boolean z, CipherParameters cipherParameters) throws IllegalArgumentException {
            this.cipher.init(z, cipherParameters);
        }

        @Override // org.bouncycastle.jcajce.provider.symmetric.util.BaseBlockCipher.GenericBlockCipher
        public int processByte(byte b, byte[] bArr, int i) throws DataLengthException {
            return this.cipher.processByte(b, bArr, i);
        }

        @Override // org.bouncycastle.jcajce.provider.symmetric.util.BaseBlockCipher.GenericBlockCipher
        public int processBytes(byte[] bArr, int i, int i2, byte[] bArr2, int i3) throws DataLengthException {
            return this.cipher.processBytes(bArr, i, i2, bArr2, i3);
        }

        @Override // org.bouncycastle.jcajce.provider.symmetric.util.BaseBlockCipher.GenericBlockCipher
        public void updateAAD(byte[] bArr, int i, int i2) {
            this.cipher.processAADBytes(bArr, i, i2);
        }

        @Override // org.bouncycastle.jcajce.provider.symmetric.util.BaseBlockCipher.GenericBlockCipher
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

        @Override // org.bouncycastle.jcajce.provider.symmetric.util.BaseBlockCipher.GenericBlockCipher
        public int doFinal(byte[] bArr, int i) throws IllegalStateException, BadPaddingException {
            try {
                return this.cipher.doFinal(bArr, i);
            } catch (InvalidCipherTextException e) {
                throw new BadPaddingException(e.getMessage());
            }
        }

        @Override // org.bouncycastle.jcajce.provider.symmetric.util.BaseBlockCipher.GenericBlockCipher
        public String getAlgorithmName() {
            return this.cipher.getUnderlyingCipher().getAlgorithmName();
        }

        @Override // org.bouncycastle.jcajce.provider.symmetric.util.BaseBlockCipher.GenericBlockCipher
        public int getOutputSize(int i) {
            return this.cipher.getOutputSize(i);
        }

        @Override // org.bouncycastle.jcajce.provider.symmetric.util.BaseBlockCipher.GenericBlockCipher
        public BlockCipher getUnderlyingCipher() {
            return this.cipher.getUnderlyingCipher();
        }

        @Override // org.bouncycastle.jcajce.provider.symmetric.util.BaseBlockCipher.GenericBlockCipher
        public int getUpdateOutputSize(int i) {
            return this.cipher.getUpdateOutputSize(i);
        }

        @Override // org.bouncycastle.jcajce.provider.symmetric.util.BaseBlockCipher.GenericBlockCipher
        public void init(boolean z, CipherParameters cipherParameters) throws IllegalArgumentException {
            this.cipher.init(z, cipherParameters);
        }

        @Override // org.bouncycastle.jcajce.provider.symmetric.util.BaseBlockCipher.GenericBlockCipher
        public int processByte(byte b, byte[] bArr, int i) throws DataLengthException {
            return this.cipher.processByte(b, bArr, i);
        }

        @Override // org.bouncycastle.jcajce.provider.symmetric.util.BaseBlockCipher.GenericBlockCipher
        public int processBytes(byte[] bArr, int i, int i2, byte[] bArr2, int i3) throws DataLengthException {
            return this.cipher.processBytes(bArr, i, i2, bArr2, i3);
        }

        @Override // org.bouncycastle.jcajce.provider.symmetric.util.BaseBlockCipher.GenericBlockCipher
        public void updateAAD(byte[] bArr, int i, int i2) {
            throw new UnsupportedOperationException("AAD is not supported in the current mode.");
        }

        @Override // org.bouncycastle.jcajce.provider.symmetric.util.BaseBlockCipher.GenericBlockCipher
        public boolean wrapOnNoPadding() {
            return !(this.cipher instanceof CTSBlockCipher);
        }
    }

    /* access modifiers changed from: private */
    public interface GenericBlockCipher {
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

    protected BaseBlockCipher(AEADCipher aEADCipher, boolean z, int i) {
        this.availableSpecs = new Class[]{RC2ParameterSpec.class, RC5ParameterSpec.class, gcmSpecClass, GOST28147ParameterSpec.class, IvParameterSpec.class, PBEParameterSpec.class};
        this.scheme = -1;
        this.ivLength = 0;
        this.fixedIv = true;
        this.pbeSpec = null;
        this.pbeAlgorithm = null;
        this.modeName = null;
        this.baseEngine = null;
        this.fixedIv = z;
        this.ivLength = i;
        this.cipher = new AEADGenericBlockCipher(aEADCipher);
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
            } else if (!(algorithmParameterSpec instanceof GOST28147ParameterSpec)) {
                return cipherParameters;
            } else {
                GOST28147ParameterSpec gOST28147ParameterSpec = (GOST28147ParameterSpec) algorithmParameterSpec;
                ParametersWithSBox parametersWithSBox = new ParametersWithSBox(cipherParameters, gOST28147ParameterSpec.getSbox());
                if (gOST28147ParameterSpec.getIV() == null || this.ivLength == 0) {
                    return parametersWithSBox;
                }
                this.ivParam = new ParametersWithIV(parameters, gOST28147ParameterSpec.getIV());
                return this.ivParam;
            }
        } else if (algorithmParameterSpec instanceof IvParameterSpec) {
            this.ivParam = new ParametersWithIV(cipherParameters, ((IvParameterSpec) algorithmParameterSpec).getIV());
        } else if (!(algorithmParameterSpec instanceof GOST28147ParameterSpec)) {
            return cipherParameters;
        } else {
            GOST28147ParameterSpec gOST28147ParameterSpec2 = (GOST28147ParameterSpec) algorithmParameterSpec;
            ParametersWithSBox parametersWithSBox2 = new ParametersWithSBox(cipherParameters, gOST28147ParameterSpec2.getSbox());
            return (gOST28147ParameterSpec2.getIV() == null || this.ivLength == 0) ? parametersWithSBox2 : new ParametersWithIV(parametersWithSBox2, gOST28147ParameterSpec2.getIV());
        }
        return this.ivParam;
    }

    private boolean isAEADModeName(String str) {
        return "CCM".equals(str) || "EAX".equals(str) || "GCM".equals(str) || "OCB".equals(str);
    }

    /* access modifiers changed from: protected */
    @Override // org.bouncycastle.jcajce.provider.symmetric.util.BaseWrapCipher, javax.crypto.CipherSpi
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
    @Override // org.bouncycastle.jcajce.provider.symmetric.util.BaseWrapCipher, javax.crypto.CipherSpi
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
    @Override // org.bouncycastle.jcajce.provider.symmetric.util.BaseWrapCipher, javax.crypto.CipherSpi
    public int engineGetBlockSize() {
        BlockCipher blockCipher = this.baseEngine;
        if (blockCipher == null) {
            return -1;
        }
        return blockCipher.getBlockSize();
    }

    /* access modifiers changed from: protected */
    @Override // org.bouncycastle.jcajce.provider.symmetric.util.BaseWrapCipher, javax.crypto.CipherSpi
    public byte[] engineGetIV() {
        AEADParameters aEADParameters = this.aeadParams;
        if (aEADParameters != null) {
            return aEADParameters.getNonce();
        }
        ParametersWithIV parametersWithIV = this.ivParam;
        if (parametersWithIV != null) {
            return parametersWithIV.getIV();
        }
        return null;
    }

    /* access modifiers changed from: protected */
    @Override // org.bouncycastle.jcajce.provider.symmetric.util.BaseWrapCipher, javax.crypto.CipherSpi
    public int engineGetKeySize(Key key) {
        return key.getEncoded().length * 8;
    }

    /* access modifiers changed from: protected */
    @Override // org.bouncycastle.jcajce.provider.symmetric.util.BaseWrapCipher, javax.crypto.CipherSpi
    public int engineGetOutputSize(int i) {
        return this.cipher.getOutputSize(i);
    }

    /* access modifiers changed from: protected */
    @Override // org.bouncycastle.jcajce.provider.symmetric.util.BaseWrapCipher, javax.crypto.CipherSpi
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
                if (this.baseEngine == null) {
                    try {
                        this.engineParams = createParametersInstance(PKCSObjectIdentifiers.id_alg_AEADChaCha20Poly1305.getId());
                        this.engineParams.init(new DEROctetString(this.aeadParams.getNonce()).getEncoded());
                    } catch (Exception e2) {
                        throw new RuntimeException(e2.toString());
                    }
                } else {
                    try {
                        this.engineParams = createParametersInstance("GCM");
                        this.engineParams.init(new GCMParameters(this.aeadParams.getNonce(), this.aeadParams.getMacSize() / 8).getEncoded());
                    } catch (Exception e3) {
                        throw new RuntimeException(e3.toString());
                    }
                }
            } else if (this.ivParam != null) {
                String algorithmName = this.cipher.getUnderlyingCipher().getAlgorithmName();
                if (algorithmName.indexOf(47) >= 0) {
                    algorithmName = algorithmName.substring(0, algorithmName.indexOf(47));
                }
                try {
                    this.engineParams = createParametersInstance(algorithmName);
                    this.engineParams.init(new IvParameterSpec(this.ivParam.getIV()));
                } catch (Exception e4) {
                    throw new RuntimeException(e4.toString());
                }
            }
        }
        return this.engineParams;
    }

    /* access modifiers changed from: protected */
    @Override // org.bouncycastle.jcajce.provider.symmetric.util.BaseWrapCipher, javax.crypto.CipherSpi
    public void engineInit(int i, Key key, AlgorithmParameters algorithmParameters, SecureRandom secureRandom) throws InvalidKeyException, InvalidAlgorithmParameterException {
        AlgorithmParameterSpec algorithmParameterSpec = null;
        if (algorithmParameters != null) {
            int i2 = 0;
            while (true) {
                Class[] clsArr = this.availableSpecs;
                if (i2 == clsArr.length) {
                    break;
                }
                if (clsArr[i2] != null) {
                    try {
                        algorithmParameterSpec = algorithmParameters.getParameterSpec(clsArr[i2]);
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
    @Override // org.bouncycastle.jcajce.provider.symmetric.util.BaseWrapCipher, javax.crypto.CipherSpi
    public void engineInit(int i, Key key, SecureRandom secureRandom) throws InvalidKeyException {
        try {
            engineInit(i, key, (AlgorithmParameterSpec) null, secureRandom);
        } catch (InvalidAlgorithmParameterException e) {
            throw new InvalidKeyException(e.getMessage());
        }
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:201:0x0467 */
    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:223:0x04be */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r5v0 */
    /* JADX WARN: Type inference failed for: r5v1, types: [org.bouncycastle.crypto.CipherParameters] */
    /* JADX WARN: Type inference failed for: r5v2, types: [org.bouncycastle.crypto.CipherParameters] */
    /* JADX WARN: Type inference failed for: r5v9 */
    /* JADX WARN: Type inference failed for: r5v12, types: [org.bouncycastle.crypto.params.RC5Parameters, org.bouncycastle.crypto.CipherParameters] */
    /* JADX WARN: Type inference failed for: r5v15, types: [org.bouncycastle.crypto.params.RC2Parameters, org.bouncycastle.crypto.CipherParameters] */
    /* JADX WARN: Type inference failed for: r5v18 */
    /* JADX WARN: Type inference failed for: r5v19, types: [org.bouncycastle.crypto.params.ParametersWithSBox, org.bouncycastle.crypto.CipherParameters] */
    /* JADX WARN: Type inference failed for: r5v22 */
    /* JADX WARN: Type inference failed for: r5v25, types: [org.bouncycastle.crypto.params.AEADParameters] */
    /* JADX WARN: Type inference failed for: r5v63 */
    /* JADX WARN: Type inference failed for: r5v64 */
    /* JADX WARN: Type inference failed for: r5v65 */
    /* JADX WARN: Type inference failed for: r5v66 */
    /* JADX WARN: Type inference failed for: r5v67 */
    /* JADX WARN: Type inference failed for: r5v68 */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x00ac, code lost:
        if (r7 != false) goto L_0x00ae;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x00f9, code lost:
        if (r7 != false) goto L_0x00ae;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x0147, code lost:
        if (r7 != false) goto L_0x00ae;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:98:0x01ff, code lost:
        if (r7 != false) goto L_0x00ae;
     */
    /* JADX WARNING: Removed duplicated region for block: B:101:0x0208  */
    /* JADX WARNING: Removed duplicated region for block: B:112:0x0247  */
    /* JADX WARNING: Removed duplicated region for block: B:213:0x0485  */
    /* JADX WARNING: Removed duplicated region for block: B:214:0x048a  */
    /* JADX WARNING: Removed duplicated region for block: B:228:0x04cc  */
    /* JADX WARNING: Removed duplicated region for block: B:242:0x0504 A[Catch:{ IllegalArgumentException -> 0x04f1, Exception -> 0x04ef }] */
    /* JADX WARNING: Removed duplicated region for block: B:253:? A[ADDED_TO_REGION, RETURN, SYNTHETIC] */
    /* JADX WARNING: Unknown variable types count: 1 */
    @Override // org.bouncycastle.jcajce.provider.symmetric.util.BaseWrapCipher, javax.crypto.CipherSpi
    public void engineInit(int i, Key key, AlgorithmParameterSpec algorithmParameterSpec, SecureRandom secureRandom) throws InvalidKeyException, InvalidAlgorithmParameterException {
        ParametersWithIV parametersWithIV;
        SecureRandom secureRandom2;
        ParametersWithIV parametersWithIV2;
        CipherParameters cipherParameters;
        CipherParameters cipherParameters2;
        CipherParameters cipherParameters3;
        BlockCipher blockCipher;
        ?? r5 = 0;
        String str = null;
        this.pbeSpec = null;
        this.pbeAlgorithm = null;
        this.engineParams = null;
        this.aeadParams = null;
        if (!(key instanceof SecretKey)) {
            StringBuilder sb = new StringBuilder();
            sb.append("Key for algorithm ");
            if (key != null) {
                str = key.getAlgorithm();
            }
            sb.append(str);
            sb.append(" not suitable for symmetric enryption.");
            throw new InvalidKeyException(sb.toString());
        } else if (algorithmParameterSpec != null || (blockCipher = this.baseEngine) == null || !blockCipher.getAlgorithmName().startsWith("RC5-64")) {
            int i2 = this.scheme;
            if (i2 == 2 || (key instanceof PKCS12Key)) {
                try {
                    SecretKey secretKey = (SecretKey) key;
                    if (algorithmParameterSpec instanceof PBEParameterSpec) {
                        this.pbeSpec = (PBEParameterSpec) algorithmParameterSpec;
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
                        if (key instanceof BCPBEKey) {
                            CipherParameters param = ((BCPBEKey) key).getParam();
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
                        cipherParameters2 = cipherParameters;
                        r5 = cipherParameters;
                    } else {
                        throw new InvalidKeyException("Algorithm requires a PBE key");
                    }
                } catch (Exception e) {
                    throw new InvalidKeyException("PKCS12 requires a SecretKey/PBEKey");
                }
            } else if (key instanceof PBKDF1Key) {
                PBKDF1Key pBKDF1Key = (PBKDF1Key) key;
                if (algorithmParameterSpec instanceof PBEParameterSpec) {
                    this.pbeSpec = (PBEParameterSpec) algorithmParameterSpec;
                }
                if ((pBKDF1Key instanceof PBKDF1KeyWithParameters) && this.pbeSpec == null) {
                    PBKDF1KeyWithParameters pBKDF1KeyWithParameters = (PBKDF1KeyWithParameters) pBKDF1Key;
                    this.pbeSpec = new PBEParameterSpec(pBKDF1KeyWithParameters.getSalt(), pBKDF1KeyWithParameters.getIterationCount());
                }
                CipherParameters makePBEParameters = PBE.Util.makePBEParameters(pBKDF1Key.getEncoded(), 0, this.digest, this.keySizeInBits, this.ivLength * 8, this.pbeSpec, this.cipher.getAlgorithmName());
                boolean z3 = makePBEParameters instanceof ParametersWithIV;
                cipherParameters2 = makePBEParameters;
                r5 = makePBEParameters;
            } else if (key instanceof BCPBEKey) {
                BCPBEKey bCPBEKey = (BCPBEKey) key;
                this.pbeAlgorithm = bCPBEKey.getOID() != null ? bCPBEKey.getOID().getId() : bCPBEKey.getAlgorithm();
                if (bCPBEKey.getParam() != null) {
                    cipherParameters3 = adjustParameters(algorithmParameterSpec, bCPBEKey.getParam());
                } else if (algorithmParameterSpec instanceof PBEParameterSpec) {
                    this.pbeSpec = (PBEParameterSpec) algorithmParameterSpec;
                    cipherParameters3 = PBE.Util.makePBEParameters(bCPBEKey, algorithmParameterSpec, this.cipher.getUnderlyingCipher().getAlgorithmName());
                } else {
                    throw new InvalidAlgorithmParameterException("PBE requires PBE parameters to be set.");
                }
                boolean z4 = cipherParameters3 instanceof ParametersWithIV;
                cipherParameters2 = cipherParameters3;
                r5 = cipherParameters3;
            } else {
                if (key instanceof PBEKey) {
                    PBEKey pBEKey2 = (PBEKey) key;
                    this.pbeSpec = (PBEParameterSpec) algorithmParameterSpec;
                    if ((pBEKey2 instanceof PKCS12KeyWithParameters) && this.pbeSpec == null) {
                        this.pbeSpec = new PBEParameterSpec(pBEKey2.getSalt(), pBEKey2.getIterationCount());
                    }
                    CipherParameters makePBEParameters2 = PBE.Util.makePBEParameters(pBEKey2.getEncoded(), this.scheme, this.digest, this.keySizeInBits, this.ivLength * 8, this.pbeSpec, this.cipher.getAlgorithmName());
                    boolean z5 = makePBEParameters2 instanceof ParametersWithIV;
                    cipherParameters2 = makePBEParameters2;
                    r5 = makePBEParameters2;
                } else if (!(key instanceof RepeatedSecretKeySpec)) {
                    if (i2 == 0 || i2 == 4 || i2 == 1 || i2 == 5) {
                        throw new InvalidKeyException("Algorithm requires a PBE key");
                    }
                    r5 = new KeyParameter(key.getEncoded());
                }
                if (!(algorithmParameterSpec instanceof AEADParameterSpec)) {
                    if (isAEADModeName(this.modeName) || (this.cipher instanceof AEADGenericBlockCipher)) {
                        AEADParameterSpec aEADParameterSpec = (AEADParameterSpec) algorithmParameterSpec;
                        r5 = new AEADParameters(r5 instanceof ParametersWithIV ? (KeyParameter) ((ParametersWithIV) r5).getParameters() : (KeyParameter) r5, aEADParameterSpec.getMacSizeInBits(), aEADParameterSpec.getNonce(), aEADParameterSpec.getAssociatedData());
                        this.aeadParams = r5;
                    } else {
                        throw new InvalidAlgorithmParameterException("AEADParameterSpec can only be used with AEAD modes.");
                    }
                } else if (!(algorithmParameterSpec instanceof IvParameterSpec)) {
                    if (algorithmParameterSpec instanceof GOST28147ParameterSpec) {
                        GOST28147ParameterSpec gOST28147ParameterSpec = (GOST28147ParameterSpec) algorithmParameterSpec;
                        r5 = new ParametersWithSBox(new KeyParameter(key.getEncoded()), gOST28147ParameterSpec.getSbox());
                        if (!(gOST28147ParameterSpec.getIV() == null || this.ivLength == 0)) {
                            parametersWithIV2 = r5 instanceof ParametersWithIV ? new ParametersWithIV(((ParametersWithIV) r5).getParameters(), gOST28147ParameterSpec.getIV()) : new ParametersWithIV(r5, gOST28147ParameterSpec.getIV());
                        }
                    } else if (algorithmParameterSpec instanceof RC2ParameterSpec) {
                        RC2ParameterSpec rC2ParameterSpec = (RC2ParameterSpec) algorithmParameterSpec;
                        r5 = new RC2Parameters(key.getEncoded(), rC2ParameterSpec.getEffectiveKeyBits());
                        if (!(rC2ParameterSpec.getIV() == null || this.ivLength == 0)) {
                            parametersWithIV2 = r5 instanceof ParametersWithIV ? new ParametersWithIV(((ParametersWithIV) r5).getParameters(), rC2ParameterSpec.getIV()) : new ParametersWithIV(r5, rC2ParameterSpec.getIV());
                        }
                    } else if (algorithmParameterSpec instanceof RC5ParameterSpec) {
                        RC5ParameterSpec rC5ParameterSpec = (RC5ParameterSpec) algorithmParameterSpec;
                        r5 = new RC5Parameters(key.getEncoded(), rC5ParameterSpec.getRounds());
                        if (this.baseEngine.getAlgorithmName().startsWith("RC5")) {
                            if (this.baseEngine.getAlgorithmName().equals("RC5-32")) {
                                if (rC5ParameterSpec.getWordSize() != 32) {
                                    throw new InvalidAlgorithmParameterException("RC5 already set up for a word size of 32 not " + rC5ParameterSpec.getWordSize() + ".");
                                }
                            } else if (this.baseEngine.getAlgorithmName().equals("RC5-64") && rC5ParameterSpec.getWordSize() != 64) {
                                throw new InvalidAlgorithmParameterException("RC5 already set up for a word size of 64 not " + rC5ParameterSpec.getWordSize() + ".");
                            }
                            if (!(rC5ParameterSpec.getIV() == null || this.ivLength == 0)) {
                                parametersWithIV2 = r5 instanceof ParametersWithIV ? new ParametersWithIV(((ParametersWithIV) r5).getParameters(), rC5ParameterSpec.getIV()) : new ParametersWithIV(r5, rC5ParameterSpec.getIV());
                            }
                        } else {
                            throw new InvalidAlgorithmParameterException("RC5 parameters passed to a cipher that is not RC5.");
                        }
                    } else {
                        Class cls = gcmSpecClass;
                        if (cls == null || !cls.isInstance(algorithmParameterSpec)) {
                            if (algorithmParameterSpec != null && !(algorithmParameterSpec instanceof PBEParameterSpec)) {
                                throw new InvalidAlgorithmParameterException("unknown parameter type.");
                            }
                        } else if (isAEADModeName(this.modeName) || (this.cipher instanceof AEADGenericBlockCipher)) {
                            try {
                                Method declaredMethod = gcmSpecClass.getDeclaredMethod("getTLen", new Class[0]);
                                Method declaredMethod2 = gcmSpecClass.getDeclaredMethod("getIV", new Class[0]);
                                boolean z6 = r5 instanceof ParametersWithIV;
                                KeyParameter keyParameter = r5;
                                if (z6) {
                                    keyParameter = ((ParametersWithIV) r5).getParameters();
                                }
                                AEADParameters aEADParameters = new AEADParameters(keyParameter, ((Integer) declaredMethod.invoke(algorithmParameterSpec, new Object[0])).intValue(), (byte[]) declaredMethod2.invoke(algorithmParameterSpec, new Object[0]));
                                this.aeadParams = aEADParameters;
                                r5 = aEADParameters;
                            } catch (Exception e2) {
                                throw new InvalidAlgorithmParameterException("Cannot process GCMParameterSpec.");
                            }
                        } else {
                            throw new InvalidAlgorithmParameterException("GCMParameterSpec can only be used with AEAD modes.");
                        }
                    }
                    this.ivParam = parametersWithIV2;
                    r5 = parametersWithIV2;
                } else if (this.ivLength != 0) {
                    IvParameterSpec ivParameterSpec = (IvParameterSpec) algorithmParameterSpec;
                    if (ivParameterSpec.getIV().length == this.ivLength || (this.cipher instanceof AEADGenericBlockCipher) || !this.fixedIv) {
                        r5 = r5 instanceof ParametersWithIV ? new ParametersWithIV(((ParametersWithIV) r5).getParameters(), ivParameterSpec.getIV()) : new ParametersWithIV(r5, ivParameterSpec.getIV());
                        this.ivParam = (ParametersWithIV) r5;
                    } else {
                        throw new InvalidAlgorithmParameterException("IV must be " + this.ivLength + " bytes long.");
                    }
                } else {
                    String str2 = this.modeName;
                    if (str2 != null && str2.equals("ECB")) {
                        throw new InvalidAlgorithmParameterException("ECB mode does not use an IV");
                    }
                }
                if (this.ivLength != 0 && !(r5 instanceof ParametersWithIV) && !(r5 instanceof AEADParameters)) {
                    secureRandom2 = secureRandom != null ? CryptoServicesRegistrar.getSecureRandom() : secureRandom;
                    if (i != 1 || i == 3) {
                        byte[] bArr = new byte[this.ivLength];
                        secureRandom2.nextBytes(bArr);
                        parametersWithIV = new ParametersWithIV(r5, bArr);
                        this.ivParam = parametersWithIV;
                        if (secureRandom != null && this.padded) {
                            parametersWithIV = new ParametersWithRandom(parametersWithIV, secureRandom);
                        }
                        if (i != 1) {
                            if (i != 2) {
                                if (i != 3) {
                                    if (i != 4) {
                                        try {
                                            throw new InvalidParameterException("unknown opmode " + i + " passed");
                                        } catch (IllegalArgumentException e3) {
                                            throw new InvalidAlgorithmParameterException(e3.getMessage(), e3);
                                        } catch (Exception e4) {
                                            throw new BaseWrapCipher.InvalidKeyOrParametersException(e4.getMessage(), e4);
                                        }
                                    }
                                }
                            }
                            this.cipher.init(false, parametersWithIV);
                            if ((this.cipher instanceof AEADGenericBlockCipher) && this.aeadParams == null) {
                                this.aeadParams = new AEADParameters((KeyParameter) this.ivParam.getParameters(), ((AEADGenericBlockCipher) this.cipher).cipher.getMac().length * 8, this.ivParam.getIV());
                                return;
                            }
                            return;
                        }
                        this.cipher.init(true, parametersWithIV);
                        if (this.cipher instanceof AEADGenericBlockCipher) {
                            return;
                        }
                        return;
                    } else if (this.cipher.getUnderlyingCipher().getAlgorithmName().indexOf("PGPCFB") < 0) {
                        throw new InvalidAlgorithmParameterException("no IV set when one expected");
                    }
                }
                parametersWithIV = r5;
                parametersWithIV = new ParametersWithRandom(parametersWithIV, secureRandom);
                if (i != 1) {
                }
                this.cipher.init(true, parametersWithIV);
                if (this.cipher instanceof AEADGenericBlockCipher) {
                }
            }
            this.ivParam = (ParametersWithIV) cipherParameters2;
            r5 = cipherParameters2;
            if (!(algorithmParameterSpec instanceof AEADParameterSpec)) {
            }
            if (secureRandom != null) {
            }
            if (i != 1) {
            }
            byte[] bArr2 = new byte[this.ivLength];
            secureRandom2.nextBytes(bArr2);
            parametersWithIV = new ParametersWithIV(r5, bArr2);
            this.ivParam = parametersWithIV;
            parametersWithIV = new ParametersWithRandom(parametersWithIV, secureRandom);
            if (i != 1) {
            }
            this.cipher.init(true, parametersWithIV);
            if (this.cipher instanceof AEADGenericBlockCipher) {
            }
        } else {
            throw new InvalidAlgorithmParameterException("RC5 requires an RC5ParametersSpec to be passed in.");
        }
    }

    /* access modifiers changed from: protected */
    @Override // org.bouncycastle.jcajce.provider.symmetric.util.BaseWrapCipher, javax.crypto.CipherSpi
    public void engineSetMode(String str) throws NoSuchAlgorithmException {
        GenericBlockCipher aEADGenericBlockCipher;
        GenericBlockCipher aEADGenericBlockCipher2;
        if (this.baseEngine != null) {
            this.modeName = Strings.toUpperCase(str);
            if (this.modeName.equals("ECB")) {
                this.ivLength = 0;
                aEADGenericBlockCipher2 = new BufferedGenericBlockCipher(this.baseEngine);
            } else if (this.modeName.equals("CBC")) {
                this.ivLength = this.baseEngine.getBlockSize();
                aEADGenericBlockCipher2 = new BufferedGenericBlockCipher(new CBCBlockCipher(this.baseEngine));
            } else {
                if (this.modeName.startsWith("OFB")) {
                    this.ivLength = this.baseEngine.getBlockSize();
                    if (this.modeName.length() != 3) {
                        aEADGenericBlockCipher = new BufferedGenericBlockCipher(new OFBBlockCipher(this.baseEngine, Integer.parseInt(this.modeName.substring(3))));
                    } else {
                        BlockCipher blockCipher = this.baseEngine;
                        aEADGenericBlockCipher2 = new BufferedGenericBlockCipher(new OFBBlockCipher(blockCipher, blockCipher.getBlockSize() * 8));
                    }
                } else if (this.modeName.startsWith("CFB")) {
                    this.ivLength = this.baseEngine.getBlockSize();
                    if (this.modeName.length() != 3) {
                        aEADGenericBlockCipher = new BufferedGenericBlockCipher(new CFBBlockCipher(this.baseEngine, Integer.parseInt(this.modeName.substring(3))));
                    } else {
                        BlockCipher blockCipher2 = this.baseEngine;
                        aEADGenericBlockCipher2 = new BufferedGenericBlockCipher(new CFBBlockCipher(blockCipher2, blockCipher2.getBlockSize() * 8));
                    }
                } else if (this.modeName.startsWith("PGP")) {
                    boolean equalsIgnoreCase = this.modeName.equalsIgnoreCase("PGPCFBwithIV");
                    this.ivLength = this.baseEngine.getBlockSize();
                    aEADGenericBlockCipher = new BufferedGenericBlockCipher(new PGPCFBBlockCipher(this.baseEngine, equalsIgnoreCase));
                } else if (this.modeName.equalsIgnoreCase("OpenPGPCFB")) {
                    this.ivLength = 0;
                    aEADGenericBlockCipher2 = new BufferedGenericBlockCipher(new OpenPGPCFBBlockCipher(this.baseEngine));
                } else if (this.modeName.startsWith("SIC")) {
                    this.ivLength = this.baseEngine.getBlockSize();
                    if (this.ivLength >= 16) {
                        this.fixedIv = false;
                        aEADGenericBlockCipher2 = new BufferedGenericBlockCipher(new BufferedBlockCipher(new SICBlockCipher(this.baseEngine)));
                    } else {
                        throw new IllegalArgumentException("Warning: SIC-Mode can become a twotime-pad if the blocksize of the cipher is too small. Use a cipher with a block size of at least 128 bits (e.g. AES)");
                    }
                } else if (this.modeName.startsWith("CTR")) {
                    this.ivLength = this.baseEngine.getBlockSize();
                    this.fixedIv = false;
                    BlockCipher blockCipher3 = this.baseEngine;
                    aEADGenericBlockCipher = blockCipher3 instanceof DSTU7624Engine ? new BufferedGenericBlockCipher(new BufferedBlockCipher(new KCTRBlockCipher(blockCipher3))) : new BufferedGenericBlockCipher(new BufferedBlockCipher(new SICBlockCipher(blockCipher3)));
                } else if (this.modeName.startsWith("GOFB")) {
                    this.ivLength = this.baseEngine.getBlockSize();
                    aEADGenericBlockCipher2 = new BufferedGenericBlockCipher(new BufferedBlockCipher(new GOFBBlockCipher(this.baseEngine)));
                } else if (this.modeName.startsWith("GCFB")) {
                    this.ivLength = this.baseEngine.getBlockSize();
                    aEADGenericBlockCipher2 = new BufferedGenericBlockCipher(new BufferedBlockCipher(new GCFBBlockCipher(this.baseEngine)));
                } else if (this.modeName.startsWith("CTS")) {
                    this.ivLength = this.baseEngine.getBlockSize();
                    aEADGenericBlockCipher2 = new BufferedGenericBlockCipher(new CTSBlockCipher(new CBCBlockCipher(this.baseEngine)));
                } else if (this.modeName.startsWith("CCM")) {
                    this.ivLength = 12;
                    BlockCipher blockCipher4 = this.baseEngine;
                    aEADGenericBlockCipher = blockCipher4 instanceof DSTU7624Engine ? new AEADGenericBlockCipher(new KCCMBlockCipher(blockCipher4)) : new AEADGenericBlockCipher(new CCMBlockCipher(blockCipher4));
                } else if (this.modeName.startsWith("OCB")) {
                    BlockCipherProvider blockCipherProvider = this.engineProvider;
                    if (blockCipherProvider != null) {
                        this.ivLength = 15;
                        aEADGenericBlockCipher2 = new AEADGenericBlockCipher(new OCBBlockCipher(this.baseEngine, blockCipherProvider.get()));
                    } else {
                        throw new NoSuchAlgorithmException("can't support mode " + str);
                    }
                } else if (this.modeName.startsWith("EAX")) {
                    this.ivLength = this.baseEngine.getBlockSize();
                    aEADGenericBlockCipher2 = new AEADGenericBlockCipher(new EAXBlockCipher(this.baseEngine));
                } else if (this.modeName.startsWith("GCM")) {
                    this.ivLength = this.baseEngine.getBlockSize();
                    BlockCipher blockCipher5 = this.baseEngine;
                    aEADGenericBlockCipher = blockCipher5 instanceof DSTU7624Engine ? new AEADGenericBlockCipher(new KGCMBlockCipher(blockCipher5)) : new AEADGenericBlockCipher(new GCMBlockCipher(blockCipher5));
                } else {
                    throw new NoSuchAlgorithmException("can't support mode " + str);
                }
                this.cipher = aEADGenericBlockCipher;
                return;
            }
            this.cipher = aEADGenericBlockCipher2;
            return;
        }
        throw new NoSuchAlgorithmException("no mode supported for this algorithm");
    }

    /* access modifiers changed from: protected */
    @Override // org.bouncycastle.jcajce.provider.symmetric.util.BaseWrapCipher, javax.crypto.CipherSpi
    public void engineSetPadding(String str) throws NoSuchPaddingException {
        BufferedGenericBlockCipher bufferedGenericBlockCipher;
        if (this.baseEngine != null) {
            String upperCase = Strings.toUpperCase(str);
            if (upperCase.equals("NOPADDING")) {
                if (this.cipher.wrapOnNoPadding()) {
                    bufferedGenericBlockCipher = new BufferedGenericBlockCipher(new BufferedBlockCipher(this.cipher.getUnderlyingCipher()));
                } else {
                    return;
                }
            } else if (upperCase.equals("WITHCTS") || upperCase.equals("CTSPADDING") || upperCase.equals("CS3PADDING")) {
                bufferedGenericBlockCipher = new BufferedGenericBlockCipher(new CTSBlockCipher(this.cipher.getUnderlyingCipher()));
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
            return;
        }
        throw new NoSuchPaddingException("no padding supported for this algorithm");
    }

    /* access modifiers changed from: protected */
    @Override // org.bouncycastle.jcajce.provider.symmetric.util.BaseWrapCipher, javax.crypto.CipherSpi
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
    @Override // org.bouncycastle.jcajce.provider.symmetric.util.BaseWrapCipher, javax.crypto.CipherSpi
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
    @Override // javax.crypto.CipherSpi
    public void engineUpdateAAD(ByteBuffer byteBuffer) {
        int remaining = byteBuffer.remaining();
        if (remaining >= 1) {
            if (byteBuffer.hasArray()) {
                engineUpdateAAD(byteBuffer.array(), byteBuffer.arrayOffset() + byteBuffer.position(), remaining);
                byteBuffer.position(byteBuffer.limit());
            } else if (remaining <= 512) {
                byte[] bArr = new byte[remaining];
                byteBuffer.get(bArr);
                engineUpdateAAD(bArr, 0, bArr.length);
                Arrays.fill(bArr, (byte) 0);
            } else {
                byte[] bArr2 = new byte[512];
                do {
                    int min = Math.min(bArr2.length, remaining);
                    byteBuffer.get(bArr2, 0, min);
                    engineUpdateAAD(bArr2, 0, min);
                    remaining -= min;
                } while (remaining > 0);
                Arrays.fill(bArr2, (byte) 0);
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.CipherSpi
    public void engineUpdateAAD(byte[] bArr, int i, int i2) {
        this.cipher.updateAAD(bArr, i, i2);
    }
}
