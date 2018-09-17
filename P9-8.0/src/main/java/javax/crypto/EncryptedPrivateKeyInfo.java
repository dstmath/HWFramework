package javax.crypto;

import java.io.IOException;
import java.security.AlgorithmParameters;
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

    public EncryptedPrivateKeyInfo(byte[] encoded) throws IOException {
        if (encoded == null) {
            throw new NullPointerException("the encoded parameter must be non-null");
        }
        this.encoded = (byte[]) encoded.clone();
        DerValue val = new DerValue(this.encoded);
        DerValue[] seq = new DerValue[]{val.data.getDerValue(), val.data.getDerValue()};
        if (val.data.available() != 0) {
            throw new IOException("overrun, bytes = " + val.data.available());
        }
        this.algid = AlgorithmId.parse(seq[0]);
        if (seq[0].data.available() != 0) {
            throw new IOException("encryptionAlgorithm field overrun");
        }
        this.encryptedData = seq[1].getOctetString();
        if (seq[1].data.available() != 0) {
            throw new IOException("encryptedData field overrun");
        }
    }

    public EncryptedPrivateKeyInfo(String algName, byte[] encryptedData) throws NoSuchAlgorithmException {
        if (algName == null) {
            throw new NullPointerException("the algName parameter must be non-null");
        }
        this.algid = AlgorithmId.get(algName);
        if (encryptedData == null) {
            throw new NullPointerException("the encryptedData parameter must be non-null");
        } else if (encryptedData.length == 0) {
            throw new IllegalArgumentException("the encryptedData parameter must not be empty");
        } else {
            this.encryptedData = (byte[]) encryptedData.clone();
            this.encoded = null;
        }
    }

    public EncryptedPrivateKeyInfo(AlgorithmParameters algParams, byte[] encryptedData) throws NoSuchAlgorithmException {
        if (algParams == null) {
            throw new NullPointerException("algParams must be non-null");
        }
        this.algid = AlgorithmId.get(algParams);
        if (encryptedData == null) {
            throw new NullPointerException("encryptedData must be non-null");
        } else if (encryptedData.length == 0) {
            throw new IllegalArgumentException("the encryptedData parameter must not be empty");
        } else {
            this.encryptedData = (byte[]) encryptedData.clone();
            this.encoded = null;
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

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0010 A:{Splitter: B:1:0x0001, ExcHandler: java.security.GeneralSecurityException (r1_0 'ex' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:5:0x0010 A:{Splitter: B:1:0x0001, ExcHandler: java.security.GeneralSecurityException (r1_0 'ex' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:5:0x0010, code:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:7:0x0019, code:
            throw new java.security.spec.InvalidKeySpecException("Cannot retrieve the PKCS8EncodedKeySpec", r1);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public PKCS8EncodedKeySpec getKeySpec(Cipher cipher) throws InvalidKeySpecException {
        try {
            byte[] encoded = cipher.doFinal(this.encryptedData);
            checkPKCS8Encoding(encoded);
            return new PKCS8EncodedKeySpec(encoded);
        } catch (Exception ex) {
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:9:0x0031 A:{Splitter: B:2:0x0003, ExcHandler: java.security.GeneralSecurityException (r2_0 'ex' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:9:0x0031, code:
            r2 = move-exception;
     */
    /* JADX WARNING: Missing block: B:11:0x003a, code:
            throw new java.security.InvalidKeyException("Cannot retrieve the PKCS8EncodedKeySpec", r2);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private PKCS8EncodedKeySpec getKeySpecImpl(Key decryptKey, Provider provider) throws NoSuchAlgorithmException, InvalidKeyException {
        Cipher c;
        if (provider == null) {
            try {
                c = Cipher.getInstance(this.algid.getName());
            } catch (NoSuchAlgorithmException nsae) {
                throw nsae;
            } catch (Exception ex) {
            }
        } else {
            c = Cipher.getInstance(this.algid.getName(), provider);
        }
        c.init(2, decryptKey, this.algid.getParameters());
        byte[] encoded = c.doFinal(this.encryptedData);
        checkPKCS8Encoding(encoded);
        return new PKCS8EncodedKeySpec(encoded);
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
        } else if (providerName == null) {
            throw new NullPointerException("provider is null");
        } else {
            Provider provider = Security.getProvider(providerName);
            if (provider != null) {
                return getKeySpecImpl(decryptKey, provider);
            }
            throw new NoSuchProviderException("provider " + providerName + " not found");
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
        DerInputStream algid = values[1].toDerInputStream();
        algid.getOID();
        if (algid.available() != 0) {
            algid.getDerValue();
        }
        checkTag(values[2], (byte) 4, "privateKey");
    }
}
