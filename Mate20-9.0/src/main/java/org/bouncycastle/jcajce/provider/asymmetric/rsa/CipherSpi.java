package org.bouncycastle.jcajce.provider.asymmetric.rsa;

import java.math.BigInteger;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.MGF1ParameterSpec;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.CryptoServicesRegistrar;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.encodings.ISO9796d1Encoding;
import org.bouncycastle.crypto.encodings.OAEPEncoding;
import org.bouncycastle.crypto.encodings.PKCS1Encoding;
import org.bouncycastle.crypto.engines.RSABlindedEngine;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.jcajce.provider.asymmetric.util.BaseCipherSpi;
import org.bouncycastle.jcajce.provider.util.BadBlockException;
import org.bouncycastle.jcajce.provider.util.DigestFactory;
import org.bouncycastle.jcajce.util.BCJcaJceHelper;
import org.bouncycastle.jcajce.util.JcaJceHelper;
import org.bouncycastle.pqc.jcajce.spec.McElieceCCA2KeyGenParameterSpec;
import org.bouncycastle.pqc.jcajce.spec.SPHINCS256KeyGenParameterSpec;
import org.bouncycastle.util.Strings;

public class CipherSpi extends BaseCipherSpi {
    private BaseCipherSpi.ErasableOutputStream bOut = new BaseCipherSpi.ErasableOutputStream();
    private AsymmetricBlockCipher cipher;
    private AlgorithmParameters engineParams;
    private final JcaJceHelper helper = new BCJcaJceHelper();
    private AlgorithmParameterSpec paramSpec;
    private boolean privateKeyOnly = false;
    private boolean publicKeyOnly = false;

    public static class ISO9796d1Padding extends CipherSpi {
        public ISO9796d1Padding() {
            super((AsymmetricBlockCipher) new ISO9796d1Encoding(new RSABlindedEngine()));
        }
    }

    public static class NoPadding extends CipherSpi {
        public NoPadding() {
            super((AsymmetricBlockCipher) new RSABlindedEngine());
        }
    }

    public static class OAEPPadding extends CipherSpi {
        public OAEPPadding() {
            super(OAEPParameterSpec.DEFAULT);
        }
    }

    public static class PKCS1v1_5Padding extends CipherSpi {
        public PKCS1v1_5Padding() {
            super((AsymmetricBlockCipher) new PKCS1Encoding(new RSABlindedEngine()));
        }
    }

    public static class PKCS1v1_5Padding_PrivateOnly extends CipherSpi {
        public PKCS1v1_5Padding_PrivateOnly() {
            super(false, true, new PKCS1Encoding(new RSABlindedEngine()));
        }
    }

    public static class PKCS1v1_5Padding_PublicOnly extends CipherSpi {
        public PKCS1v1_5Padding_PublicOnly() {
            super(true, false, new PKCS1Encoding(new RSABlindedEngine()));
        }
    }

    public CipherSpi(OAEPParameterSpec oAEPParameterSpec) {
        try {
            initFromSpec(oAEPParameterSpec);
        } catch (NoSuchPaddingException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public CipherSpi(AsymmetricBlockCipher asymmetricBlockCipher) {
        this.cipher = asymmetricBlockCipher;
    }

    public CipherSpi(boolean z, boolean z2, AsymmetricBlockCipher asymmetricBlockCipher) {
        this.publicKeyOnly = z;
        this.privateKeyOnly = z2;
        this.cipher = asymmetricBlockCipher;
    }

    private byte[] getOutput() throws BadPaddingException {
        try {
            byte[] processBlock = this.cipher.processBlock(this.bOut.getBuf(), 0, this.bOut.size());
            this.bOut.erase();
            return processBlock;
        } catch (InvalidCipherTextException e) {
            throw new BadBlockException("unable to decrypt block", e);
        } catch (ArrayIndexOutOfBoundsException e2) {
            throw new BadBlockException("unable to decrypt block", e2);
        } catch (Throwable th) {
            this.bOut.erase();
            throw th;
        }
    }

    private void initFromSpec(OAEPParameterSpec oAEPParameterSpec) throws NoSuchPaddingException {
        MGF1ParameterSpec mGF1ParameterSpec = (MGF1ParameterSpec) oAEPParameterSpec.getMGFParameters();
        Digest digest = DigestFactory.getDigest(mGF1ParameterSpec.getDigestAlgorithm());
        if (digest != null) {
            this.cipher = new OAEPEncoding(new RSABlindedEngine(), digest, ((PSource.PSpecified) oAEPParameterSpec.getPSource()).getValue());
            this.paramSpec = oAEPParameterSpec;
            return;
        }
        throw new NoSuchPaddingException("no match on OAEP constructor for digest algorithm: " + mGF1ParameterSpec.getDigestAlgorithm());
    }

    /* access modifiers changed from: protected */
    public int engineDoFinal(byte[] bArr, int i, int i2, byte[] bArr2, int i3) throws IllegalBlockSizeException, BadPaddingException, ShortBufferException {
        if (engineGetOutputSize(i2) + i3 <= bArr2.length) {
            if (bArr != null) {
                this.bOut.write(bArr, i, i2);
            }
            if (this.cipher instanceof RSABlindedEngine) {
                if (this.bOut.size() > this.cipher.getInputBlockSize() + 1) {
                    throw new ArrayIndexOutOfBoundsException("too much data for RSA block");
                }
            } else if (this.bOut.size() > this.cipher.getInputBlockSize()) {
                throw new ArrayIndexOutOfBoundsException("too much data for RSA block");
            }
            byte[] output = getOutput();
            for (int i4 = 0; i4 != output.length; i4++) {
                bArr2[i3 + i4] = output[i4];
            }
            return output.length;
        }
        throw new ShortBufferException("output buffer too short for input.");
    }

    /* access modifiers changed from: protected */
    public byte[] engineDoFinal(byte[] bArr, int i, int i2) throws IllegalBlockSizeException, BadPaddingException {
        if (bArr != null) {
            this.bOut.write(bArr, i, i2);
        }
        if (this.cipher instanceof RSABlindedEngine) {
            if (this.bOut.size() > this.cipher.getInputBlockSize() + 1) {
                throw new ArrayIndexOutOfBoundsException("too much data for RSA block");
            }
        } else if (this.bOut.size() > this.cipher.getInputBlockSize()) {
            throw new ArrayIndexOutOfBoundsException("too much data for RSA block");
        }
        return getOutput();
    }

    /* access modifiers changed from: protected */
    public int engineGetBlockSize() {
        try {
            return this.cipher.getInputBlockSize();
        } catch (NullPointerException e) {
            throw new IllegalStateException("RSA Cipher not initialised");
        }
    }

    /* access modifiers changed from: protected */
    public int engineGetKeySize(Key key) {
        BigInteger modulus;
        if (key instanceof RSAPrivateKey) {
            modulus = ((RSAPrivateKey) key).getModulus();
        } else if (key instanceof RSAPublicKey) {
            modulus = ((RSAPublicKey) key).getModulus();
        } else {
            throw new IllegalArgumentException("not an RSA key!");
        }
        return modulus.bitLength();
    }

    /* access modifiers changed from: protected */
    public int engineGetOutputSize(int i) {
        try {
            return this.cipher.getOutputBlockSize();
        } catch (NullPointerException e) {
            throw new IllegalStateException("RSA Cipher not initialised");
        }
    }

    /* access modifiers changed from: protected */
    public AlgorithmParameters engineGetParameters() {
        if (this.engineParams == null && this.paramSpec != null) {
            try {
                this.engineParams = this.helper.createAlgorithmParameters("OAEP");
                this.engineParams.init(this.paramSpec);
            } catch (Exception e) {
                throw new RuntimeException(e.toString());
            }
        }
        return this.engineParams;
    }

    /* access modifiers changed from: protected */
    public void engineInit(int i, Key key, AlgorithmParameters algorithmParameters, SecureRandom secureRandom) throws InvalidKeyException, InvalidAlgorithmParameterException {
        AlgorithmParameterSpec algorithmParameterSpec;
        if (algorithmParameters != null) {
            try {
                algorithmParameterSpec = algorithmParameters.getParameterSpec(OAEPParameterSpec.class);
            } catch (InvalidParameterSpecException e) {
                throw new InvalidAlgorithmParameterException("cannot recognise parameters: " + e.toString(), e);
            }
        } else {
            algorithmParameterSpec = null;
        }
        this.engineParams = algorithmParameters;
        engineInit(i, key, algorithmParameterSpec, secureRandom);
    }

    /* access modifiers changed from: protected */
    public void engineInit(int i, Key key, SecureRandom secureRandom) throws InvalidKeyException {
        try {
            engineInit(i, key, (AlgorithmParameterSpec) null, secureRandom);
        } catch (InvalidAlgorithmParameterException e) {
            throw new InvalidKeyException("Eeeek! " + e.toString(), e);
        }
    }

    /* access modifiers changed from: protected */
    public void engineInit(int i, Key key, AlgorithmParameterSpec algorithmParameterSpec, SecureRandom secureRandom) throws InvalidKeyException, InvalidAlgorithmParameterException {
        RSAKeyParameters rSAKeyParameters;
        if (algorithmParameterSpec == null || (algorithmParameterSpec instanceof OAEPParameterSpec)) {
            if (key instanceof RSAPublicKey) {
                if (!this.privateKeyOnly || i != 1) {
                    rSAKeyParameters = RSAUtil.generatePublicKeyParameter((RSAPublicKey) key);
                } else {
                    throw new InvalidKeyException("mode 1 requires RSAPrivateKey");
                }
            } else if (!(key instanceof RSAPrivateKey)) {
                throw new InvalidKeyException("unknown key type passed to RSA");
            } else if (!this.publicKeyOnly || i != 1) {
                rSAKeyParameters = RSAUtil.generatePrivateKeyParameter((RSAPrivateKey) key);
            } else {
                throw new InvalidKeyException("mode 2 requires RSAPublicKey");
            }
            if (algorithmParameterSpec != null) {
                OAEPParameterSpec oAEPParameterSpec = (OAEPParameterSpec) algorithmParameterSpec;
                this.paramSpec = algorithmParameterSpec;
                if (!oAEPParameterSpec.getMGFAlgorithm().equalsIgnoreCase("MGF1") && !oAEPParameterSpec.getMGFAlgorithm().equals(PKCSObjectIdentifiers.id_mgf1.getId())) {
                    throw new InvalidAlgorithmParameterException("unknown mask generation function specified");
                } else if (oAEPParameterSpec.getMGFParameters() instanceof MGF1ParameterSpec) {
                    Digest digest = DigestFactory.getDigest(oAEPParameterSpec.getDigestAlgorithm());
                    if (digest != null) {
                        MGF1ParameterSpec mGF1ParameterSpec = (MGF1ParameterSpec) oAEPParameterSpec.getMGFParameters();
                        Digest digest2 = DigestFactory.getDigest(mGF1ParameterSpec.getDigestAlgorithm());
                        if (digest2 != null) {
                            this.cipher = new OAEPEncoding(new RSABlindedEngine(), digest, digest2, ((PSource.PSpecified) oAEPParameterSpec.getPSource()).getValue());
                        } else {
                            throw new InvalidAlgorithmParameterException("no match on MGF digest algorithm: " + mGF1ParameterSpec.getDigestAlgorithm());
                        }
                    } else {
                        throw new InvalidAlgorithmParameterException("no match on digest algorithm: " + oAEPParameterSpec.getDigestAlgorithm());
                    }
                } else {
                    throw new InvalidAlgorithmParameterException("unkown MGF parameters");
                }
            }
            CipherParameters parametersWithRandom = !(this.cipher instanceof RSABlindedEngine) ? secureRandom != null ? new ParametersWithRandom(rSAKeyParameters, secureRandom) : new ParametersWithRandom(rSAKeyParameters, CryptoServicesRegistrar.getSecureRandom()) : rSAKeyParameters;
            this.bOut.reset();
            switch (i) {
                case 1:
                case 3:
                    this.cipher.init(true, parametersWithRandom);
                    return;
                case 2:
                case 4:
                    this.cipher.init(false, parametersWithRandom);
                    return;
                default:
                    throw new InvalidParameterException("unknown opmode " + i + " passed to RSA");
            }
        } else {
            throw new InvalidAlgorithmParameterException("unknown parameter type: " + algorithmParameterSpec.getClass().getName());
        }
    }

    /* access modifiers changed from: protected */
    public void engineSetMode(String str) throws NoSuchAlgorithmException {
        String upperCase = Strings.toUpperCase(str);
        if (!upperCase.equals("NONE") && !upperCase.equals("ECB")) {
            if (upperCase.equals("1")) {
                this.privateKeyOnly = true;
                this.publicKeyOnly = false;
            } else if (upperCase.equals("2")) {
                this.privateKeyOnly = false;
                this.publicKeyOnly = true;
            } else {
                throw new NoSuchAlgorithmException("can't support mode " + str);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void engineSetPadding(String str) throws NoSuchPaddingException {
        OAEPParameterSpec oAEPParameterSpec;
        AsymmetricBlockCipher iSO9796d1Encoding;
        String upperCase = Strings.toUpperCase(str);
        if (upperCase.equals("NOPADDING")) {
            iSO9796d1Encoding = new RSABlindedEngine();
        } else if (upperCase.equals("PKCS1PADDING")) {
            iSO9796d1Encoding = new PKCS1Encoding(new RSABlindedEngine());
        } else if (upperCase.equals("ISO9796-1PADDING")) {
            iSO9796d1Encoding = new ISO9796d1Encoding(new RSABlindedEngine());
        } else {
            if (upperCase.equals("OAEPWITHMD5ANDMGF1PADDING")) {
                oAEPParameterSpec = new OAEPParameterSpec("MD5", "MGF1", new MGF1ParameterSpec("MD5"), PSource.PSpecified.DEFAULT);
            } else if (upperCase.equals("OAEPPADDING") || upperCase.equals("OAEPWITHSHA1ANDMGF1PADDING") || upperCase.equals("OAEPWITHSHA-1ANDMGF1PADDING")) {
                oAEPParameterSpec = OAEPParameterSpec.DEFAULT;
            } else if (upperCase.equals("OAEPWITHSHA224ANDMGF1PADDING") || upperCase.equals("OAEPWITHSHA-224ANDMGF1PADDING")) {
                oAEPParameterSpec = new OAEPParameterSpec(McElieceCCA2KeyGenParameterSpec.SHA224, "MGF1", new MGF1ParameterSpec(McElieceCCA2KeyGenParameterSpec.SHA224), PSource.PSpecified.DEFAULT);
            } else if (upperCase.equals("OAEPWITHSHA256ANDMGF1PADDING") || upperCase.equals("OAEPWITHSHA-256ANDMGF1PADDING")) {
                oAEPParameterSpec = new OAEPParameterSpec(McElieceCCA2KeyGenParameterSpec.SHA256, "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT);
            } else if (upperCase.equals("OAEPWITHSHA384ANDMGF1PADDING") || upperCase.equals("OAEPWITHSHA-384ANDMGF1PADDING")) {
                oAEPParameterSpec = new OAEPParameterSpec(McElieceCCA2KeyGenParameterSpec.SHA384, "MGF1", MGF1ParameterSpec.SHA384, PSource.PSpecified.DEFAULT);
            } else if (upperCase.equals("OAEPWITHSHA512ANDMGF1PADDING") || upperCase.equals("OAEPWITHSHA-512ANDMGF1PADDING")) {
                oAEPParameterSpec = new OAEPParameterSpec(McElieceCCA2KeyGenParameterSpec.SHA512, "MGF1", MGF1ParameterSpec.SHA512, PSource.PSpecified.DEFAULT);
            } else if (upperCase.equals("OAEPWITHSHA3-224ANDMGF1PADDING")) {
                oAEPParameterSpec = new OAEPParameterSpec("SHA3-224", "MGF1", new MGF1ParameterSpec("SHA3-224"), PSource.PSpecified.DEFAULT);
            } else if (upperCase.equals("OAEPWITHSHA3-256ANDMGF1PADDING")) {
                oAEPParameterSpec = new OAEPParameterSpec(SPHINCS256KeyGenParameterSpec.SHA3_256, "MGF1", new MGF1ParameterSpec(SPHINCS256KeyGenParameterSpec.SHA3_256), PSource.PSpecified.DEFAULT);
            } else if (upperCase.equals("OAEPWITHSHA3-384ANDMGF1PADDING")) {
                oAEPParameterSpec = new OAEPParameterSpec("SHA3-384", "MGF1", new MGF1ParameterSpec("SHA3-384"), PSource.PSpecified.DEFAULT);
            } else if (upperCase.equals("OAEPWITHSHA3-512ANDMGF1PADDING")) {
                oAEPParameterSpec = new OAEPParameterSpec("SHA3-512", "MGF1", new MGF1ParameterSpec("SHA3-512"), PSource.PSpecified.DEFAULT);
            } else {
                throw new NoSuchPaddingException(str + " unavailable with RSA.");
            }
            initFromSpec(oAEPParameterSpec);
            return;
        }
        this.cipher = iSO9796d1Encoding;
    }

    /* access modifiers changed from: protected */
    public int engineUpdate(byte[] bArr, int i, int i2, byte[] bArr2, int i3) {
        this.bOut.write(bArr, i, i2);
        if (this.cipher instanceof RSABlindedEngine) {
            if (this.bOut.size() > this.cipher.getInputBlockSize() + 1) {
                throw new ArrayIndexOutOfBoundsException("too much data for RSA block");
            }
        } else if (this.bOut.size() > this.cipher.getInputBlockSize()) {
            throw new ArrayIndexOutOfBoundsException("too much data for RSA block");
        }
        return 0;
    }

    /* access modifiers changed from: protected */
    public byte[] engineUpdate(byte[] bArr, int i, int i2) {
        this.bOut.write(bArr, i, i2);
        if (this.cipher instanceof RSABlindedEngine) {
            if (this.bOut.size() > this.cipher.getInputBlockSize() + 1) {
                throw new ArrayIndexOutOfBoundsException("too much data for RSA block");
            }
        } else if (this.bOut.size() > this.cipher.getInputBlockSize()) {
            throw new ArrayIndexOutOfBoundsException("too much data for RSA block");
        }
        return null;
    }
}
