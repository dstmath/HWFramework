package org.bouncycastle.jcajce.provider.asymmetric.rsa;

import java.io.IOException;
import java.security.AlgorithmParameters;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.SignatureSpi;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.AlgorithmParameterSpec;
import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.teletrust.TeleTrusTObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.DigestInfo;
import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.MD2Digest;
import org.bouncycastle.crypto.digests.MD4Digest;
import org.bouncycastle.crypto.digests.NullDigest;
import org.bouncycastle.crypto.digests.RIPEMD128Digest;
import org.bouncycastle.crypto.digests.RIPEMD160Digest;
import org.bouncycastle.crypto.digests.RIPEMD256Digest;
import org.bouncycastle.crypto.encodings.PKCS1Encoding;
import org.bouncycastle.crypto.engines.RSABlindedEngine;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.util.DigestFactory;
import org.bouncycastle.util.Arrays;

public class DigestSignatureSpi extends SignatureSpi {
    private AlgorithmIdentifier algId;
    private AsymmetricBlockCipher cipher;
    private Digest digest;

    public static class MD2 extends DigestSignatureSpi {
        public MD2() {
            super(PKCSObjectIdentifiers.md2, new MD2Digest(), new PKCS1Encoding(new RSABlindedEngine()));
        }
    }

    public static class MD4 extends DigestSignatureSpi {
        public MD4() {
            super(PKCSObjectIdentifiers.md4, new MD4Digest(), new PKCS1Encoding(new RSABlindedEngine()));
        }
    }

    public static class MD5 extends DigestSignatureSpi {
        public MD5() {
            super(PKCSObjectIdentifiers.md5, DigestFactory.createMD5(), new PKCS1Encoding(new RSABlindedEngine()));
        }
    }

    public static class RIPEMD128 extends DigestSignatureSpi {
        public RIPEMD128() {
            super(TeleTrusTObjectIdentifiers.ripemd128, new RIPEMD128Digest(), new PKCS1Encoding(new RSABlindedEngine()));
        }
    }

    public static class RIPEMD160 extends DigestSignatureSpi {
        public RIPEMD160() {
            super(TeleTrusTObjectIdentifiers.ripemd160, new RIPEMD160Digest(), new PKCS1Encoding(new RSABlindedEngine()));
        }
    }

    public static class RIPEMD256 extends DigestSignatureSpi {
        public RIPEMD256() {
            super(TeleTrusTObjectIdentifiers.ripemd256, new RIPEMD256Digest(), new PKCS1Encoding(new RSABlindedEngine()));
        }
    }

    public static class SHA1 extends DigestSignatureSpi {
        public SHA1() {
            super(OIWObjectIdentifiers.idSHA1, DigestFactory.createSHA1(), new PKCS1Encoding(new RSABlindedEngine()));
        }
    }

    public static class SHA224 extends DigestSignatureSpi {
        public SHA224() {
            super(NISTObjectIdentifiers.id_sha224, DigestFactory.createSHA224(), new PKCS1Encoding(new RSABlindedEngine()));
        }
    }

    public static class SHA256 extends DigestSignatureSpi {
        public SHA256() {
            super(NISTObjectIdentifiers.id_sha256, DigestFactory.createSHA256(), new PKCS1Encoding(new RSABlindedEngine()));
        }
    }

    public static class SHA384 extends DigestSignatureSpi {
        public SHA384() {
            super(NISTObjectIdentifiers.id_sha384, DigestFactory.createSHA384(), new PKCS1Encoding(new RSABlindedEngine()));
        }
    }

    public static class SHA3_224 extends DigestSignatureSpi {
        public SHA3_224() {
            super(NISTObjectIdentifiers.id_sha3_224, DigestFactory.createSHA3_224(), new PKCS1Encoding(new RSABlindedEngine()));
        }
    }

    public static class SHA3_256 extends DigestSignatureSpi {
        public SHA3_256() {
            super(NISTObjectIdentifiers.id_sha3_256, DigestFactory.createSHA3_256(), new PKCS1Encoding(new RSABlindedEngine()));
        }
    }

    public static class SHA3_384 extends DigestSignatureSpi {
        public SHA3_384() {
            super(NISTObjectIdentifiers.id_sha3_384, DigestFactory.createSHA3_384(), new PKCS1Encoding(new RSABlindedEngine()));
        }
    }

    public static class SHA3_512 extends DigestSignatureSpi {
        public SHA3_512() {
            super(NISTObjectIdentifiers.id_sha3_512, DigestFactory.createSHA3_512(), new PKCS1Encoding(new RSABlindedEngine()));
        }
    }

    public static class SHA512 extends DigestSignatureSpi {
        public SHA512() {
            super(NISTObjectIdentifiers.id_sha512, DigestFactory.createSHA512(), new PKCS1Encoding(new RSABlindedEngine()));
        }
    }

    public static class SHA512_224 extends DigestSignatureSpi {
        public SHA512_224() {
            super(NISTObjectIdentifiers.id_sha512_224, DigestFactory.createSHA512_224(), new PKCS1Encoding(new RSABlindedEngine()));
        }
    }

    public static class SHA512_256 extends DigestSignatureSpi {
        public SHA512_256() {
            super(NISTObjectIdentifiers.id_sha512_256, DigestFactory.createSHA512_256(), new PKCS1Encoding(new RSABlindedEngine()));
        }
    }

    public static class noneRSA extends DigestSignatureSpi {
        public noneRSA() {
            super(new NullDigest(), new PKCS1Encoding(new RSABlindedEngine()));
        }
    }

    protected DigestSignatureSpi(ASN1ObjectIdentifier aSN1ObjectIdentifier, Digest digest2, AsymmetricBlockCipher asymmetricBlockCipher) {
        this.digest = digest2;
        this.cipher = asymmetricBlockCipher;
        this.algId = new AlgorithmIdentifier(aSN1ObjectIdentifier, DERNull.INSTANCE);
    }

    protected DigestSignatureSpi(Digest digest2, AsymmetricBlockCipher asymmetricBlockCipher) {
        this.digest = digest2;
        this.cipher = asymmetricBlockCipher;
        this.algId = null;
    }

    private byte[] derEncode(byte[] bArr) throws IOException {
        return this.algId == null ? bArr : new DigestInfo(this.algId, bArr).getEncoded(ASN1Encoding.DER);
    }

    private String getType(Object obj) {
        if (obj == null) {
            return null;
        }
        return obj.getClass().getName();
    }

    /* access modifiers changed from: protected */
    public Object engineGetParameter(String str) {
        return null;
    }

    /* access modifiers changed from: protected */
    public AlgorithmParameters engineGetParameters() {
        return null;
    }

    /* access modifiers changed from: protected */
    public void engineInitSign(PrivateKey privateKey) throws InvalidKeyException {
        if (privateKey instanceof RSAPrivateKey) {
            RSAKeyParameters generatePrivateKeyParameter = RSAUtil.generatePrivateKeyParameter((RSAPrivateKey) privateKey);
            this.digest.reset();
            this.cipher.init(true, generatePrivateKeyParameter);
            return;
        }
        throw new InvalidKeyException("Supplied key (" + getType(privateKey) + ") is not a RSAPrivateKey instance");
    }

    /* access modifiers changed from: protected */
    public void engineInitVerify(PublicKey publicKey) throws InvalidKeyException {
        if (publicKey instanceof RSAPublicKey) {
            RSAKeyParameters generatePublicKeyParameter = RSAUtil.generatePublicKeyParameter((RSAPublicKey) publicKey);
            this.digest.reset();
            this.cipher.init(false, generatePublicKeyParameter);
            return;
        }
        throw new InvalidKeyException("Supplied key (" + getType(publicKey) + ") is not a RSAPublicKey instance");
    }

    /* access modifiers changed from: protected */
    public void engineSetParameter(String str, Object obj) {
        throw new UnsupportedOperationException("engineSetParameter unsupported");
    }

    /* access modifiers changed from: protected */
    public void engineSetParameter(AlgorithmParameterSpec algorithmParameterSpec) {
        throw new UnsupportedOperationException("engineSetParameter unsupported");
    }

    /* access modifiers changed from: protected */
    public byte[] engineSign() throws SignatureException {
        byte[] bArr = new byte[this.digest.getDigestSize()];
        this.digest.doFinal(bArr, 0);
        try {
            byte[] derEncode = derEncode(bArr);
            return this.cipher.processBlock(derEncode, 0, derEncode.length);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new SignatureException("key too small for signature type");
        } catch (Exception e2) {
            throw new SignatureException(e2.toString());
        }
    }

    /* access modifiers changed from: protected */
    public void engineUpdate(byte b) throws SignatureException {
        this.digest.update(b);
    }

    /* access modifiers changed from: protected */
    public void engineUpdate(byte[] bArr, int i, int i2) throws SignatureException {
        this.digest.update(bArr, i, i2);
    }

    /* access modifiers changed from: protected */
    public boolean engineVerify(byte[] bArr) throws SignatureException {
        byte[] bArr2 = new byte[this.digest.getDigestSize()];
        this.digest.doFinal(bArr2, 0);
        try {
            byte[] processBlock = this.cipher.processBlock(bArr, 0, bArr.length);
            byte[] derEncode = derEncode(bArr2);
            if (processBlock.length == derEncode.length) {
                return Arrays.constantTimeAreEqual(processBlock, derEncode);
            }
            if (processBlock.length == derEncode.length - 2) {
                derEncode[1] = (byte) (derEncode[1] - 2);
                derEncode[3] = (byte) (derEncode[3] - 2);
                int i = 4 + derEncode[3];
                int i2 = i + 2;
                byte b = 0;
                for (int i3 = 0; i3 < derEncode.length - i2; i3++) {
                    b |= processBlock[i + i3] ^ derEncode[i2 + i3];
                }
                for (int i4 = 0; i4 < i; i4++) {
                    b |= processBlock[i4] ^ derEncode[i4];
                }
                return b == 0;
            }
            Arrays.constantTimeAreEqual(derEncode, derEncode);
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}
