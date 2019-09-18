package javax.crypto;

import java.io.IOException;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.x509.AlgorithmId;

public class EncryptedPrivateKeyInfo {
    private AlgorithmId algid;
    private byte[] encoded = null;
    private byte[] encryptedData;

    public EncryptedPrivateKeyInfo(byte[] encoded2) throws IOException {
        if (encoded2 != null) {
            this.encoded = (byte[]) encoded2.clone();
            DerValue val = new DerValue(this.encoded);
            DerValue[] seq = {val.data.getDerValue(), val.data.getDerValue()};
            if (val.data.available() == 0) {
                this.algid = AlgorithmId.parse(seq[0]);
                if (seq[0].data.available() == 0) {
                    this.encryptedData = seq[1].getOctetString();
                    if (seq[1].data.available() != 0) {
                        throw new IOException("encryptedData field overrun");
                    }
                    return;
                }
                throw new IOException("encryptionAlgorithm field overrun");
            }
            throw new IOException("overrun, bytes = " + val.data.available());
        }
        throw new NullPointerException("the encoded parameter must be non-null");
    }

    public EncryptedPrivateKeyInfo(String algName, byte[] encryptedData2) throws NoSuchAlgorithmException {
        if (algName != null) {
            this.algid = AlgorithmId.get(algName);
            if (encryptedData2 == null) {
                throw new NullPointerException("the encryptedData parameter must be non-null");
            } else if (encryptedData2.length != 0) {
                this.encryptedData = (byte[]) encryptedData2.clone();
                this.encoded = null;
            } else {
                throw new IllegalArgumentException("the encryptedData parameter must not be empty");
            }
        } else {
            throw new NullPointerException("the algName parameter must be non-null");
        }
    }

    public EncryptedPrivateKeyInfo(AlgorithmParameters algParams, byte[] encryptedData2) throws NoSuchAlgorithmException {
        if (algParams != null) {
            this.algid = AlgorithmId.get(algParams);
            if (encryptedData2 == null) {
                throw new NullPointerException("encryptedData must be non-null");
            } else if (encryptedData2.length != 0) {
                this.encryptedData = (byte[]) encryptedData2.clone();
                this.encoded = null;
            } else {
                throw new IllegalArgumentException("the encryptedData parameter must not be empty");
            }
        } else {
            throw new NullPointerException("algParams must be non-null");
        }
    }

    public String getAlgName() {
        return this.algid.getName();
    }

    public AlgorithmParameters getAlgParameters() {
        return this.algid.getParameters();
    }

    public byte[] getEncryptedData() {
        return (byte[]) this.encryptedData.clone();
    }

    public PKCS8EncodedKeySpec getKeySpec(Cipher cipher) throws InvalidKeySpecException {
        try {
            byte[] encoded2 = cipher.doFinal(this.encryptedData);
            checkPKCS8Encoding(encoded2);
            return new PKCS8EncodedKeySpec(encoded2);
        } catch (IOException | IllegalStateException | GeneralSecurityException ex) {
            throw new InvalidKeySpecException("Cannot retrieve the PKCS8EncodedKeySpec", ex);
        }
    }

    private PKCS8EncodedKeySpec getKeySpecImpl(Key decryptKey, Provider provider) throws NoSuchAlgorithmException, InvalidKeyException {
        Cipher c;
        if (provider == null) {
            try {
                c = Cipher.getInstance(this.algid.getName());
            } catch (NoSuchAlgorithmException nsae) {
                throw nsae;
            } catch (IOException | GeneralSecurityException ex) {
                throw new InvalidKeyException("Cannot retrieve the PKCS8EncodedKeySpec", ex);
            }
        } else {
            c = Cipher.getInstance(this.algid.getName(), provider);
        }
        c.init(2, decryptKey, this.algid.getParameters());
        byte[] encoded2 = c.doFinal(this.encryptedData);
        checkPKCS8Encoding(encoded2);
        return new PKCS8EncodedKeySpec(encoded2);
    }

    public PKCS8EncodedKeySpec getKeySpec(Key decryptKey) throws NoSuchAlgorithmException, InvalidKeyException {
        if (decryptKey != null) {
            return getKeySpecImpl(decryptKey, null);
        }
        throw new NullPointerException("decryptKey is null");
    }

    public PKCS8EncodedKeySpec getKeySpec(Key decryptKey, String providerName) throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeyException {
        if (decryptKey == null) {
            throw new NullPointerException("decryptKey is null");
        } else if (providerName != null) {
            Provider provider = Security.getProvider(providerName);
            if (provider != null) {
                return getKeySpecImpl(decryptKey, provider);
            }
            throw new NoSuchProviderException("provider " + providerName + " not found");
        } else {
            throw new NullPointerException("provider is null");
        }
    }

    public PKCS8EncodedKeySpec getKeySpec(Key decryptKey, Provider provider) throws NoSuchAlgorithmException, InvalidKeyException {
        if (decryptKey == null) {
            throw new NullPointerException("decryptKey is null");
        } else if (provider != null) {
            return getKeySpecImpl(decryptKey, provider);
        } else {
            throw new NullPointerException("provider is null");
        }
    }

    public byte[] getEncoded() throws IOException {
        if (this.encoded == null) {
            DerOutputStream out = new DerOutputStream();
            DerOutputStream tmp = new DerOutputStream();
            this.algid.encode(tmp);
            tmp.putOctetString(this.encryptedData);
            out.write((byte) 48, tmp);
            this.encoded = out.toByteArray();
        }
        return (byte[]) this.encoded.clone();
    }

    private static void checkTag(DerValue val, byte tag, String valName) throws IOException {
        if (val.getTag() != tag) {
            throw new IOException("invalid key encoding - wrong tag for " + valName);
        }
    }

    private static void checkPKCS8Encoding(byte[] encodedKey) throws IOException {
        DerValue[] values = new DerInputStream(encodedKey).getSequence(3);
        switch (values.length) {
            case 3:
                break;
            case 4:
                checkTag(values[3], Byte.MIN_VALUE, "attributes");
                break;
            default:
                throw new IOException("invalid key encoding");
        }
        checkTag(values[0], (byte) 2, "version");
        DerInputStream algid2 = values[1].toDerInputStream();
        algid2.getOID();
        if (algid2.available() != 0) {
            algid2.getDerValue();
        }
        checkTag(values[2], (byte) 4, "privateKey");
    }
}
