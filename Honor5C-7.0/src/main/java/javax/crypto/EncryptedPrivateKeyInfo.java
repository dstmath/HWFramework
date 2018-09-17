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
import sun.security.x509.X509CertInfo;
import sun.util.calendar.BaseCalendar;

public class EncryptedPrivateKeyInfo {
    private AlgorithmId algid;
    private byte[] encoded;
    private byte[] encryptedData;

    public EncryptedPrivateKeyInfo(byte[] encoded) throws IOException {
        this.encoded = null;
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
        this.encoded = null;
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
        this.encoded = null;
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

    public PKCS8EncodedKeySpec getKeySpec(Cipher cipher) throws InvalidKeySpecException {
        InvalidKeySpecException ikse;
        try {
            byte[] encoded = cipher.doFinal(this.encryptedData);
            checkPKCS8Encoding(encoded);
            return new PKCS8EncodedKeySpec(encoded);
        } catch (GeneralSecurityException gse) {
            ikse = new InvalidKeySpecException("Cannot retrieve the PKCS8EncodedKeySpec");
            ikse.initCause(gse);
            throw ikse;
        } catch (IOException ioe) {
            ikse = new InvalidKeySpecException("Cannot retrieve the PKCS8EncodedKeySpec");
            ikse.initCause(ioe);
            throw ikse;
        } catch (IllegalStateException ise) {
            ikse = new InvalidKeySpecException("Cannot retrieve the PKCS8EncodedKeySpec");
            ikse.initCause(ise);
            throw ikse;
        }
    }

    private PKCS8EncodedKeySpec getKeySpecImpl(Key decryptKey, Provider provider) throws NoSuchAlgorithmException, InvalidKeyException {
        Cipher c;
        if (provider == null) {
            try {
                c = Cipher.getInstance(this.algid.getName());
            } catch (NoSuchAlgorithmException nsae) {
                throw nsae;
            } catch (GeneralSecurityException gse) {
                InvalidKeyException ike = new InvalidKeyException("Cannot retrieve the PKCS8EncodedKeySpec");
                ike.initCause(gse);
                throw ike;
            } catch (IOException ioe) {
                ike = new InvalidKeyException("Cannot retrieve the PKCS8EncodedKeySpec");
                ike.initCause(ioe);
                throw ike;
            }
        }
        c = Cipher.getInstance(this.algid.getName(), provider);
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
            out.write((byte) DerValue.tag_SequenceOf, tmp);
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
            case BaseCalendar.TUESDAY /*3*/:
                break;
            case BaseCalendar.WEDNESDAY /*4*/:
                checkTag(values[3], DerValue.TAG_CONTEXT, "attributes");
                break;
            default:
                throw new IOException("invalid key encoding");
        }
        checkTag(values[0], (byte) 2, X509CertInfo.VERSION);
        DerInputStream algid = values[1].toDerInputStream();
        algid.getOID();
        if (algid.available() != 0) {
            algid.getDerValue();
        }
        checkTag(values[2], (byte) 4, "privateKey");
    }
}
