package org.bouncycastle.pqc.jcajce.provider.mceliece;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.X509ObjectIdentifiers;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.pqc.crypto.mceliece.McElieceCipher;
import org.bouncycastle.pqc.crypto.mceliece.McElieceKeyParameters;
import org.bouncycastle.pqc.jcajce.provider.util.AsymmetricBlockCipher;

public class McEliecePKCSCipherSpi extends AsymmetricBlockCipher implements PKCSObjectIdentifiers, X509ObjectIdentifiers {
    private McElieceCipher cipher;

    public static class McEliecePKCS extends McEliecePKCSCipherSpi {
        public McEliecePKCS() {
            super(new McElieceCipher());
        }
    }

    public McEliecePKCSCipherSpi(McElieceCipher mcElieceCipher) {
        this.cipher = mcElieceCipher;
    }

    public int getKeySize(Key key) throws InvalidKeyException {
        return this.cipher.getKeySize((McElieceKeyParameters) (key instanceof PublicKey ? McElieceKeysToParams.generatePublicKeyParameter((PublicKey) key) : McElieceKeysToParams.generatePrivateKeyParameter((PrivateKey) key)));
    }

    public String getName() {
        return "McEliecePKCS";
    }

    /* access modifiers changed from: protected */
    public void initCipherDecrypt(Key key, AlgorithmParameterSpec algorithmParameterSpec) throws InvalidKeyException, InvalidAlgorithmParameterException {
        this.cipher.init(false, McElieceKeysToParams.generatePrivateKeyParameter((PrivateKey) key));
        this.maxPlainTextSize = this.cipher.maxPlainTextSize;
        this.cipherTextSize = this.cipher.cipherTextSize;
    }

    /* access modifiers changed from: protected */
    public void initCipherEncrypt(Key key, AlgorithmParameterSpec algorithmParameterSpec, SecureRandom secureRandom) throws InvalidKeyException, InvalidAlgorithmParameterException {
        this.cipher.init(true, new ParametersWithRandom(McElieceKeysToParams.generatePublicKeyParameter((PublicKey) key), secureRandom));
        this.maxPlainTextSize = this.cipher.maxPlainTextSize;
        this.cipherTextSize = this.cipher.cipherTextSize;
    }

    /* access modifiers changed from: protected */
    public byte[] messageDecrypt(byte[] bArr) throws IllegalBlockSizeException, BadPaddingException {
        try {
            return this.cipher.messageDecrypt(bArr);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /* access modifiers changed from: protected */
    public byte[] messageEncrypt(byte[] bArr) throws IllegalBlockSizeException, BadPaddingException {
        try {
            return this.cipher.messageEncrypt(bArr);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
