package com.android.org.bouncycastle.crypto.signers;

import com.android.org.bouncycastle.asn1.ASN1Encoding;
import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import com.android.org.bouncycastle.asn1.DERNull;
import com.android.org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import com.android.org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import com.android.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import com.android.org.bouncycastle.asn1.x509.DigestInfo;
import com.android.org.bouncycastle.asn1.x509.X509ObjectIdentifiers;
import com.android.org.bouncycastle.crypto.AsymmetricBlockCipher;
import com.android.org.bouncycastle.crypto.CipherParameters;
import com.android.org.bouncycastle.crypto.CryptoException;
import com.android.org.bouncycastle.crypto.DataLengthException;
import com.android.org.bouncycastle.crypto.Digest;
import com.android.org.bouncycastle.crypto.Signer;
import com.android.org.bouncycastle.crypto.encodings.PKCS1Encoding;
import com.android.org.bouncycastle.crypto.engines.RSABlindedEngine;
import com.android.org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import com.android.org.bouncycastle.crypto.params.ParametersWithRandom;
import com.android.org.bouncycastle.util.Arrays;
import java.io.IOException;
import java.util.Hashtable;

public class RSADigestSigner implements Signer {
    private static final Hashtable oidMap = new Hashtable();
    private final AlgorithmIdentifier algId;
    private final Digest digest;
    private boolean forSigning;
    private final AsymmetricBlockCipher rsaEngine;

    static {
        oidMap.put("SHA-1", X509ObjectIdentifiers.id_SHA1);
        oidMap.put("SHA-224", NISTObjectIdentifiers.id_sha224);
        oidMap.put("SHA-256", NISTObjectIdentifiers.id_sha256);
        oidMap.put("SHA-384", NISTObjectIdentifiers.id_sha384);
        oidMap.put("SHA-512", NISTObjectIdentifiers.id_sha512);
        oidMap.put("SHA-512/224", NISTObjectIdentifiers.id_sha512_224);
        oidMap.put("SHA-512/256", NISTObjectIdentifiers.id_sha512_256);
        oidMap.put("MD5", PKCSObjectIdentifiers.md5);
    }

    public RSADigestSigner(Digest digest2) {
        this(digest2, (ASN1ObjectIdentifier) oidMap.get(digest2.getAlgorithmName()));
    }

    public RSADigestSigner(Digest digest2, ASN1ObjectIdentifier digestOid) {
        this.rsaEngine = new PKCS1Encoding(new RSABlindedEngine());
        this.digest = digest2;
        this.algId = new AlgorithmIdentifier(digestOid, DERNull.INSTANCE);
    }

    public String getAlgorithmName() {
        return this.digest.getAlgorithmName() + "withRSA";
    }

    public void init(boolean forSigning2, CipherParameters parameters) {
        AsymmetricKeyParameter k;
        this.forSigning = forSigning2;
        if (parameters instanceof ParametersWithRandom) {
            k = (AsymmetricKeyParameter) ((ParametersWithRandom) parameters).getParameters();
        } else {
            k = (AsymmetricKeyParameter) parameters;
        }
        if (forSigning2 && !k.isPrivate()) {
            throw new IllegalArgumentException("signing requires private key");
        } else if (forSigning2 || !k.isPrivate()) {
            reset();
            this.rsaEngine.init(forSigning2, parameters);
        } else {
            throw new IllegalArgumentException("verification requires public key");
        }
    }

    public void update(byte input) {
        this.digest.update(input);
    }

    public void update(byte[] input, int inOff, int length) {
        this.digest.update(input, inOff, length);
    }

    public byte[] generateSignature() throws CryptoException, DataLengthException {
        if (this.forSigning) {
            byte[] hash = new byte[this.digest.getDigestSize()];
            this.digest.doFinal(hash, 0);
            try {
                byte[] data = derEncode(hash);
                return this.rsaEngine.processBlock(data, 0, data.length);
            } catch (IOException e) {
                throw new CryptoException("unable to encode signature: " + e.getMessage(), e);
            }
        } else {
            throw new IllegalStateException("RSADigestSigner not initialised for signature generation.");
        }
    }

    public boolean verifySignature(byte[] signature) {
        if (!this.forSigning) {
            byte[] hash = new byte[this.digest.getDigestSize()];
            boolean z = false;
            this.digest.doFinal(hash, 0);
            try {
                byte[] sig = this.rsaEngine.processBlock(signature, 0, signature.length);
                byte[] expected = derEncode(hash);
                if (sig.length == expected.length) {
                    return Arrays.constantTimeAreEqual(sig, expected);
                }
                if (sig.length == expected.length - 2) {
                    int sigOffset = (sig.length - hash.length) - 2;
                    int expectedOffset = (expected.length - hash.length) - 2;
                    expected[1] = (byte) (expected[1] - 2);
                    expected[3] = (byte) (expected[3] - 2);
                    int nonEqual = 0;
                    for (int i = 0; i < hash.length; i++) {
                        nonEqual |= sig[sigOffset + i] ^ expected[expectedOffset + i];
                    }
                    for (int i2 = 0; i2 < sigOffset; i2++) {
                        nonEqual |= sig[i2] ^ expected[i2];
                    }
                    if (nonEqual == 0) {
                        z = true;
                    }
                    return z;
                }
                Arrays.constantTimeAreEqual(expected, expected);
                return false;
            } catch (Exception e) {
                return false;
            }
        } else {
            throw new IllegalStateException("RSADigestSigner not initialised for verification");
        }
    }

    public void reset() {
        this.digest.reset();
    }

    private byte[] derEncode(byte[] hash) throws IOException {
        return new DigestInfo(this.algId, hash).getEncoded(ASN1Encoding.DER);
    }
}
