package org.bouncycastle.crypto.signers;

import java.io.IOException;
import java.util.Hashtable;
import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.teletrust.TeleTrusTObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.DigestInfo;
import org.bouncycastle.asn1.x509.X509ObjectIdentifiers;
import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.Signer;
import org.bouncycastle.crypto.encodings.PKCS1Encoding;
import org.bouncycastle.crypto.engines.RSABlindedEngine;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.pqc.jcajce.spec.McElieceCCA2KeyGenParameterSpec;
import org.bouncycastle.pqc.jcajce.spec.SPHINCS256KeyGenParameterSpec;
import org.bouncycastle.util.Arrays;

public class RSADigestSigner implements Signer {
    private static final Hashtable oidMap = new Hashtable();
    private final AlgorithmIdentifier algId;
    private final Digest digest;
    private boolean forSigning;
    private final AsymmetricBlockCipher rsaEngine;

    static {
        oidMap.put("RIPEMD128", TeleTrusTObjectIdentifiers.ripemd128);
        oidMap.put("RIPEMD160", TeleTrusTObjectIdentifiers.ripemd160);
        oidMap.put("RIPEMD256", TeleTrusTObjectIdentifiers.ripemd256);
        oidMap.put(McElieceCCA2KeyGenParameterSpec.SHA1, X509ObjectIdentifiers.id_SHA1);
        oidMap.put(McElieceCCA2KeyGenParameterSpec.SHA224, NISTObjectIdentifiers.id_sha224);
        oidMap.put(McElieceCCA2KeyGenParameterSpec.SHA256, NISTObjectIdentifiers.id_sha256);
        oidMap.put(McElieceCCA2KeyGenParameterSpec.SHA384, NISTObjectIdentifiers.id_sha384);
        oidMap.put(McElieceCCA2KeyGenParameterSpec.SHA512, NISTObjectIdentifiers.id_sha512);
        oidMap.put("SHA-512/224", NISTObjectIdentifiers.id_sha512_224);
        oidMap.put("SHA-512/256", NISTObjectIdentifiers.id_sha512_256);
        oidMap.put("SHA3-224", NISTObjectIdentifiers.id_sha3_224);
        oidMap.put(SPHINCS256KeyGenParameterSpec.SHA3_256, NISTObjectIdentifiers.id_sha3_256);
        oidMap.put("SHA3-384", NISTObjectIdentifiers.id_sha3_384);
        oidMap.put("SHA3-512", NISTObjectIdentifiers.id_sha3_512);
        oidMap.put("MD2", PKCSObjectIdentifiers.md2);
        oidMap.put("MD4", PKCSObjectIdentifiers.md4);
        oidMap.put("MD5", PKCSObjectIdentifiers.md5);
    }

    public RSADigestSigner(Digest digest2) {
        this(digest2, (ASN1ObjectIdentifier) oidMap.get(digest2.getAlgorithmName()));
    }

    public RSADigestSigner(Digest digest2, ASN1ObjectIdentifier aSN1ObjectIdentifier) {
        this.rsaEngine = new PKCS1Encoding(new RSABlindedEngine());
        this.digest = digest2;
        this.algId = new AlgorithmIdentifier(aSN1ObjectIdentifier, DERNull.INSTANCE);
    }

    private byte[] derEncode(byte[] bArr) throws IOException {
        return new DigestInfo(this.algId, bArr).getEncoded(ASN1Encoding.DER);
    }

    public byte[] generateSignature() throws CryptoException, DataLengthException {
        if (this.forSigning) {
            byte[] bArr = new byte[this.digest.getDigestSize()];
            this.digest.doFinal(bArr, 0);
            try {
                byte[] derEncode = derEncode(bArr);
                return this.rsaEngine.processBlock(derEncode, 0, derEncode.length);
            } catch (IOException e) {
                throw new CryptoException("unable to encode signature: " + e.getMessage(), e);
            }
        } else {
            throw new IllegalStateException("RSADigestSigner not initialised for signature generation.");
        }
    }

    public String getAlgorithmName() {
        return this.digest.getAlgorithmName() + "withRSA";
    }

    public void init(boolean z, CipherParameters cipherParameters) {
        this.forSigning = z;
        AsymmetricKeyParameter asymmetricKeyParameter = cipherParameters instanceof ParametersWithRandom ? (AsymmetricKeyParameter) ((ParametersWithRandom) cipherParameters).getParameters() : (AsymmetricKeyParameter) cipherParameters;
        if (z && !asymmetricKeyParameter.isPrivate()) {
            throw new IllegalArgumentException("signing requires private key");
        } else if (z || !asymmetricKeyParameter.isPrivate()) {
            reset();
            this.rsaEngine.init(z, cipherParameters);
        } else {
            throw new IllegalArgumentException("verification requires public key");
        }
    }

    public void reset() {
        this.digest.reset();
    }

    public void update(byte b) {
        this.digest.update(b);
    }

    public void update(byte[] bArr, int i, int i2) {
        this.digest.update(bArr, i, i2);
    }

    public boolean verifySignature(byte[] bArr) {
        if (!this.forSigning) {
            byte[] bArr2 = new byte[this.digest.getDigestSize()];
            boolean z = false;
            this.digest.doFinal(bArr2, 0);
            try {
                byte[] processBlock = this.rsaEngine.processBlock(bArr, 0, bArr.length);
                byte[] derEncode = derEncode(bArr2);
                if (processBlock.length == derEncode.length) {
                    return Arrays.constantTimeAreEqual(processBlock, derEncode);
                }
                if (processBlock.length == derEncode.length - 2) {
                    int length = (processBlock.length - bArr2.length) - 2;
                    int length2 = (derEncode.length - bArr2.length) - 2;
                    derEncode[1] = (byte) (derEncode[1] - 2);
                    derEncode[3] = (byte) (derEncode[3] - 2);
                    byte b = 0;
                    for (int i = 0; i < bArr2.length; i++) {
                        b |= processBlock[length + i] ^ derEncode[length2 + i];
                    }
                    for (int i2 = 0; i2 < length; i2++) {
                        b |= processBlock[i2] ^ derEncode[i2];
                    }
                    if (b == 0) {
                        z = true;
                    }
                    return z;
                }
                Arrays.constantTimeAreEqual(derEncode, derEncode);
                return false;
            } catch (Exception e) {
                return false;
            }
        } else {
            throw new IllegalStateException("RSADigestSigner not initialised for verification");
        }
    }
}
