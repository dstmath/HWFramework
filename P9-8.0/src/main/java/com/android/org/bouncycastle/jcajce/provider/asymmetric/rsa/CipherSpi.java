package com.android.org.bouncycastle.jcajce.provider.asymmetric.rsa;

import com.android.org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import com.android.org.bouncycastle.crypto.AsymmetricBlockCipher;
import com.android.org.bouncycastle.crypto.CipherParameters;
import com.android.org.bouncycastle.crypto.Digest;
import com.android.org.bouncycastle.crypto.InvalidCipherTextException;
import com.android.org.bouncycastle.crypto.encodings.OAEPEncoding;
import com.android.org.bouncycastle.crypto.encodings.PKCS1Encoding;
import com.android.org.bouncycastle.crypto.engines.RSABlindedEngine;
import com.android.org.bouncycastle.crypto.params.ParametersWithRandom;
import com.android.org.bouncycastle.jcajce.provider.asymmetric.util.BaseCipherSpi;
import com.android.org.bouncycastle.jcajce.provider.util.BadBlockException;
import com.android.org.bouncycastle.jcajce.provider.util.DigestFactory;
import com.android.org.bouncycastle.jcajce.util.BCJcaJceHelper;
import com.android.org.bouncycastle.jcajce.util.JcaJceHelper;
import com.android.org.bouncycastle.util.Strings;
import java.io.ByteArrayOutputStream;
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
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource.PSpecified;

public class CipherSpi extends BaseCipherSpi {
    private ByteArrayOutputStream bOut = new ByteArrayOutputStream();
    private AsymmetricBlockCipher cipher;
    private AlgorithmParameters engineParams;
    private final JcaJceHelper helper = new BCJcaJceHelper();
    private AlgorithmParameterSpec paramSpec;
    private boolean privateKeyOnly = false;
    private boolean publicKeyOnly = false;

    public static class NoPadding extends CipherSpi {
        public NoPadding() {
            super(new RSABlindedEngine());
        }
    }

    public CipherSpi(AsymmetricBlockCipher engine) {
        this.cipher = engine;
    }

    public CipherSpi(OAEPParameterSpec pSpec) {
        try {
            initFromSpec(pSpec);
        } catch (NoSuchPaddingException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public CipherSpi(boolean publicKeyOnly, boolean privateKeyOnly, AsymmetricBlockCipher engine) {
        this.publicKeyOnly = publicKeyOnly;
        this.privateKeyOnly = privateKeyOnly;
        this.cipher = engine;
    }

    private void initFromSpec(OAEPParameterSpec pSpec) throws NoSuchPaddingException {
        MGF1ParameterSpec mgfParams = (MGF1ParameterSpec) pSpec.getMGFParameters();
        Digest digest = DigestFactory.getDigest(mgfParams.getDigestAlgorithm());
        if (digest == null) {
            throw new NoSuchPaddingException("no match on OAEP constructor for digest algorithm: " + mgfParams.getDigestAlgorithm());
        }
        this.cipher = new OAEPEncoding(new RSABlindedEngine(), digest, ((PSpecified) pSpec.getPSource()).getValue());
        this.paramSpec = pSpec;
    }

    protected int engineGetBlockSize() {
        try {
            return this.cipher.getInputBlockSize();
        } catch (NullPointerException e) {
            throw new IllegalStateException("RSA Cipher not initialised");
        }
    }

    protected int engineGetKeySize(Key key) {
        if (key instanceof RSAPrivateKey) {
            return ((RSAPrivateKey) key).getModulus().bitLength();
        }
        if (key instanceof RSAPublicKey) {
            return ((RSAPublicKey) key).getModulus().bitLength();
        }
        throw new IllegalArgumentException("not an RSA key!");
    }

    protected int engineGetOutputSize(int inputLen) {
        try {
            return this.cipher.getOutputBlockSize();
        } catch (NullPointerException e) {
            throw new IllegalStateException("RSA Cipher not initialised");
        }
    }

    protected AlgorithmParameters engineGetParameters() {
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

    protected void engineSetMode(String mode) throws NoSuchAlgorithmException {
        String md = Strings.toUpperCase(mode);
        if (!md.equals("NONE") && !md.equals("ECB")) {
            if (md.equals("1")) {
                this.privateKeyOnly = true;
                this.publicKeyOnly = false;
            } else if (md.equals("2")) {
                this.privateKeyOnly = false;
                this.publicKeyOnly = true;
            } else {
                throw new NoSuchAlgorithmException("can't support mode " + mode);
            }
        }
    }

    protected void engineSetPadding(String padding) throws NoSuchPaddingException {
        String pad = Strings.toUpperCase(padding);
        if (pad.equals("NOPADDING")) {
            this.cipher = new RSABlindedEngine();
        } else if (pad.equals("PKCS1PADDING")) {
            this.cipher = new PKCS1Encoding(new RSABlindedEngine());
        } else if (pad.equals("OAEPWITHMD5ANDMGF1PADDING")) {
            initFromSpec(new OAEPParameterSpec("MD5", "MGF1", new MGF1ParameterSpec("MD5"), PSpecified.DEFAULT));
        } else if (pad.equals("OAEPPADDING")) {
            initFromSpec(OAEPParameterSpec.DEFAULT);
        } else if (pad.equals("OAEPWITHSHA1ANDMGF1PADDING") || pad.equals("OAEPWITHSHA-1ANDMGF1PADDING")) {
            initFromSpec(OAEPParameterSpec.DEFAULT);
        } else if (pad.equals("OAEPWITHSHA224ANDMGF1PADDING") || pad.equals("OAEPWITHSHA-224ANDMGF1PADDING")) {
            initFromSpec(new OAEPParameterSpec("SHA-224", "MGF1", new MGF1ParameterSpec("SHA-224"), PSpecified.DEFAULT));
        } else if (pad.equals("OAEPWITHSHA256ANDMGF1PADDING") || pad.equals("OAEPWITHSHA-256ANDMGF1PADDING")) {
            initFromSpec(new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSpecified.DEFAULT));
        } else if (pad.equals("OAEPWITHSHA384ANDMGF1PADDING") || pad.equals("OAEPWITHSHA-384ANDMGF1PADDING")) {
            initFromSpec(new OAEPParameterSpec("SHA-384", "MGF1", MGF1ParameterSpec.SHA384, PSpecified.DEFAULT));
        } else if (pad.equals("OAEPWITHSHA512ANDMGF1PADDING") || pad.equals("OAEPWITHSHA-512ANDMGF1PADDING")) {
            initFromSpec(new OAEPParameterSpec("SHA-512", "MGF1", MGF1ParameterSpec.SHA512, PSpecified.DEFAULT));
        } else {
            throw new NoSuchPaddingException(padding + " unavailable with RSA.");
        }
    }

    protected void engineInit(int opmode, Key key, AlgorithmParameterSpec params, SecureRandom random) throws InvalidKeyException, InvalidAlgorithmParameterException {
        if (params == null || (params instanceof OAEPParameterSpec)) {
            CipherParameters param;
            CipherParameters param2;
            if (key instanceof RSAPublicKey) {
                if (this.privateKeyOnly && opmode == 1) {
                    throw new InvalidKeyException("mode 1 requires RSAPrivateKey");
                }
                param = RSAUtil.generatePublicKeyParameter((RSAPublicKey) key);
            } else if (!(key instanceof RSAPrivateKey)) {
                throw new InvalidKeyException("unknown key type passed to RSA");
            } else if (this.publicKeyOnly && opmode == 1) {
                throw new InvalidKeyException("mode 2 requires RSAPublicKey");
            } else {
                param = RSAUtil.generatePrivateKeyParameter((RSAPrivateKey) key);
            }
            if (params != null) {
                OAEPParameterSpec spec = (OAEPParameterSpec) params;
                this.paramSpec = params;
                if (!spec.getMGFAlgorithm().equalsIgnoreCase("MGF1") && (spec.getMGFAlgorithm().equals(PKCSObjectIdentifiers.id_mgf1.getId()) ^ 1) != 0) {
                    throw new InvalidAlgorithmParameterException("unknown mask generation function specified");
                } else if (spec.getMGFParameters() instanceof MGF1ParameterSpec) {
                    Digest digest = DigestFactory.getDigest(spec.getDigestAlgorithm());
                    if (digest == null) {
                        throw new InvalidAlgorithmParameterException("no match on digest algorithm: " + spec.getDigestAlgorithm());
                    }
                    MGF1ParameterSpec mgfParams = (MGF1ParameterSpec) spec.getMGFParameters();
                    Digest mgfDigest = DigestFactory.getDigest(mgfParams.getDigestAlgorithm());
                    if (mgfDigest == null) {
                        throw new InvalidAlgorithmParameterException("no match on MGF digest algorithm: " + mgfParams.getDigestAlgorithm());
                    }
                    this.cipher = new OAEPEncoding(new RSABlindedEngine(), digest, mgfDigest, ((PSpecified) spec.getPSource()).getValue());
                } else {
                    throw new InvalidAlgorithmParameterException("unkown MGF parameters");
                }
            }
            if (this.cipher instanceof RSABlindedEngine) {
                param2 = param;
            } else if (random != null) {
                param2 = new ParametersWithRandom(param, random);
            } else {
                param2 = new ParametersWithRandom(param, new SecureRandom());
            }
            this.bOut.reset();
            switch (opmode) {
                case 1:
                case 3:
                    this.cipher.init(true, param2);
                    return;
                case 2:
                case 4:
                    this.cipher.init(false, param2);
                    return;
                default:
                    throw new InvalidParameterException("unknown opmode " + opmode + " passed to RSA");
            }
        }
        throw new InvalidAlgorithmParameterException("unknown parameter type: " + params.getClass().getName());
    }

    protected void engineInit(int opmode, Key key, AlgorithmParameters params, SecureRandom random) throws InvalidKeyException, InvalidAlgorithmParameterException {
        AlgorithmParameterSpec paramSpec = null;
        if (params != null) {
            try {
                paramSpec = params.getParameterSpec(OAEPParameterSpec.class);
            } catch (InvalidParameterSpecException e) {
                throw new InvalidAlgorithmParameterException("cannot recognise parameters: " + e.toString(), e);
            }
        }
        this.engineParams = params;
        engineInit(opmode, key, paramSpec, random);
    }

    protected void engineInit(int opmode, Key key, SecureRandom random) throws InvalidKeyException {
        try {
            engineInit(opmode, key, (AlgorithmParameterSpec) null, random);
        } catch (InvalidAlgorithmParameterException e) {
            throw new InvalidKeyException("Eeeek! " + e.toString(), e);
        }
    }

    protected byte[] engineUpdate(byte[] input, int inputOffset, int inputLen) {
        this.bOut.write(input, inputOffset, inputLen);
        if (this.cipher instanceof RSABlindedEngine) {
            if (this.bOut.size() > this.cipher.getInputBlockSize() + 1) {
                throw new ArrayIndexOutOfBoundsException("too much data for RSA block");
            }
        } else if (this.bOut.size() > this.cipher.getInputBlockSize()) {
            throw new ArrayIndexOutOfBoundsException("too much data for RSA block");
        }
        return null;
    }

    protected int engineUpdate(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset) {
        this.bOut.write(input, inputOffset, inputLen);
        if (this.cipher instanceof RSABlindedEngine) {
            if (this.bOut.size() > this.cipher.getInputBlockSize() + 1) {
                throw new ArrayIndexOutOfBoundsException("too much data for RSA block");
            }
        } else if (this.bOut.size() > this.cipher.getInputBlockSize()) {
            throw new ArrayIndexOutOfBoundsException("too much data for RSA block");
        }
        return 0;
    }

    protected byte[] engineDoFinal(byte[] input, int inputOffset, int inputLen) throws IllegalBlockSizeException, BadPaddingException {
        if (input != null) {
            this.bOut.write(input, inputOffset, inputLen);
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

    protected int engineDoFinal(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset) throws IllegalBlockSizeException, BadPaddingException {
        if (input != null) {
            this.bOut.write(input, inputOffset, inputLen);
        }
        if (this.cipher instanceof RSABlindedEngine) {
            if (this.bOut.size() > this.cipher.getInputBlockSize() + 1) {
                throw new ArrayIndexOutOfBoundsException("too much data for RSA block");
            }
        } else if (this.bOut.size() > this.cipher.getInputBlockSize()) {
            throw new ArrayIndexOutOfBoundsException("too much data for RSA block");
        }
        byte[] out = getOutput();
        for (int i = 0; i != out.length; i++) {
            output[outputOffset + i] = out[i];
        }
        return out.length;
    }

    private byte[] getOutput() throws BadPaddingException {
        try {
            byte[] bytes = this.bOut.toByteArray();
            byte[] processBlock = this.cipher.processBlock(bytes, 0, bytes.length);
            this.bOut.reset();
            return processBlock;
        } catch (InvalidCipherTextException e) {
            throw new BadBlockException("unable to decrypt block", e);
        } catch (ArrayIndexOutOfBoundsException e2) {
            throw new BadBlockException("unable to decrypt block", e2);
        } catch (Throwable th) {
            this.bOut.reset();
        }
    }
}
