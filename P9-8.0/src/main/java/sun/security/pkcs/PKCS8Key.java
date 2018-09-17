package sun.security.pkcs;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyRep;
import java.security.KeyRep.Type;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import sun.security.util.Debug;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.x509.AlgorithmId;

public class PKCS8Key implements PrivateKey {
    private static final long serialVersionUID = -3836890099307167124L;
    public static final BigInteger version = BigInteger.ZERO;
    protected AlgorithmId algid;
    protected byte[] encodedKey;
    protected byte[] key;

    private PKCS8Key(AlgorithmId algid, byte[] key) throws InvalidKeyException {
        this.algid = algid;
        this.key = key;
        encode();
    }

    public static PKCS8Key parse(DerValue in) throws IOException {
        PrivateKey key = parseKey(in);
        if (key instanceof PKCS8Key) {
            return (PKCS8Key) key;
        }
        throw new IOException("Provider did not return PKCS8Key");
    }

    public static PrivateKey parseKey(DerValue in) throws IOException {
        if (in.tag != (byte) 48) {
            throw new IOException("corrupt private key");
        }
        BigInteger parsedVersion = in.data.getBigInteger();
        if (version.equals(parsedVersion)) {
            try {
                PrivateKey privKey = buildPKCS8Key(AlgorithmId.parse(in.data.getDerValue()), in.data.getOctetString());
                if (in.data.available() == 0) {
                    return privKey;
                }
                throw new IOException("excess private key");
            } catch (InvalidKeyException e) {
                throw new IOException("corrupt private key");
            }
        }
        throw new IOException("version mismatch: (supported: " + Debug.toHexString(version) + ", parsed: " + Debug.toHexString(parsedVersion));
    }

    protected void parseKeyBits() throws IOException, InvalidKeyException {
        encode();
    }

    /* JADX WARNING: Removed duplicated region for block: B:19:0x006a A:{Splitter: B:6:0x0028, ExcHandler: java.lang.InstantiationException (e java.lang.InstantiationException)} */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x009a A:{Splitter: B:6:0x0028, ExcHandler: java.lang.IllegalAccessException (e java.lang.IllegalAccessException), PHI: r3 } */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Missing block: B:32:0x008f, code:
            r2 = java.lang.ClassLoader.getSystemClassLoader();
     */
    /* JADX WARNING: Missing block: B:33:0x0093, code:
            if (r2 != null) goto L_0x0095;
     */
    /* JADX WARNING: Missing block: B:34:0x0095, code:
            r10 = r2.loadClass(r3);
     */
    /* JADX WARNING: Missing block: B:37:0x00b6, code:
            throw new java.io.IOException(r3 + " [internal error]");
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static PrivateKey buildPKCS8Key(AlgorithmId algid, byte[] key) throws IOException, InvalidKeyException {
        DerOutputStream pkcs8EncodedKeyStream = new DerOutputStream();
        encode(pkcs8EncodedKeyStream, algid, key);
        try {
            return KeyFactory.getInstance(algid.getName()).generatePrivate(new PKCS8EncodedKeySpec(pkcs8EncodedKeyStream.toByteArray()));
        } catch (NoSuchAlgorithmException e) {
        } catch (InvalidKeySpecException e2) {
        }
        PKCS8Key result;
        String classname = "";
        try {
            Provider sunProvider = Security.getProvider("SUN");
            if (sunProvider == null) {
                throw new InstantiationException();
            }
            classname = sunProvider.getProperty("PrivateKey.PKCS#8." + algid.getName());
            if (classname == null) {
                throw new InstantiationException();
            }
            Class keyClass = null;
            keyClass = Class.forName(classname);
            Object inst = null;
            if (keyClass != null) {
                inst = keyClass.newInstance();
            }
            if (inst instanceof PKCS8Key) {
                result = (PKCS8Key) inst;
                result.algid = algid;
                result.key = key;
                result.parseKeyBits();
                return result;
            }
            result = new PKCS8Key();
            result.algid = algid;
            result.key = key;
            return result;
        } catch (ClassNotFoundException e3) {
        } catch (InstantiationException e4) {
        } catch (IllegalAccessException e5) {
        }
        result = new PKCS8Key();
        result.algid = algid;
        result.key = key;
        return result;
    }

    public String getAlgorithm() {
        return this.algid.getName();
    }

    public AlgorithmId getAlgorithmId() {
        return this.algid;
    }

    public final void encode(DerOutputStream out) throws IOException {
        encode(out, this.algid, this.key);
    }

    public synchronized byte[] getEncoded() {
        byte[] result;
        result = null;
        try {
            result = encode();
        } catch (InvalidKeyException e) {
        }
        return result;
    }

    public String getFormat() {
        return "PKCS#8";
    }

    public byte[] encode() throws InvalidKeyException {
        if (this.encodedKey == null) {
            try {
                DerOutputStream out = new DerOutputStream();
                encode(out);
                this.encodedKey = out.toByteArray();
            } catch (IOException e) {
                throw new InvalidKeyException("IOException : " + e.getMessage());
            }
        }
        return (byte[]) this.encodedKey.clone();
    }

    public void decode(InputStream in) throws InvalidKeyException {
        try {
            DerValue val = new DerValue(in);
            if (val.tag != (byte) 48) {
                throw new InvalidKeyException("invalid key format");
            }
            BigInteger version = val.data.getBigInteger();
            if (version.equals(version)) {
                this.algid = AlgorithmId.parse(val.data.getDerValue());
                this.key = val.data.getOctetString();
                parseKeyBits();
                int available = val.data.available();
                return;
            }
            throw new IOException("version mismatch: (supported: " + Debug.toHexString(version) + ", parsed: " + Debug.toHexString(version));
        } catch (IOException e) {
            throw new InvalidKeyException("IOException : " + e.getMessage());
        }
    }

    public void decode(byte[] encodedKey) throws InvalidKeyException {
        decode(new ByteArrayInputStream(encodedKey));
    }

    protected Object writeReplace() throws ObjectStreamException {
        return new KeyRep(Type.PRIVATE, getAlgorithm(), getFormat(), getEncoded());
    }

    private void readObject(ObjectInputStream stream) throws IOException {
        try {
            decode((InputStream) stream);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            throw new IOException("deserialized key is invalid: " + e.getMessage());
        }
    }

    static void encode(DerOutputStream out, AlgorithmId algid, byte[] key) throws IOException {
        DerOutputStream tmp = new DerOutputStream();
        tmp.putInteger(version);
        algid.encode(tmp);
        tmp.putOctetString(key);
        out.write((byte) 48, tmp);
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Key)) {
            return false;
        }
        byte[] b1;
        if (this.encodedKey != null) {
            b1 = this.encodedKey;
        } else {
            b1 = getEncoded();
        }
        byte[] b2 = ((Key) object).getEncoded();
        if (b1.length != b2.length) {
            return false;
        }
        for (int i = 0; i < b1.length; i++) {
            if (b1[i] != b2[i]) {
                return false;
            }
        }
        return true;
    }

    public int hashCode() {
        int retval = 0;
        byte[] b1 = getEncoded();
        for (int i = 1; i < b1.length; i++) {
            retval += b1[i] * i;
        }
        return retval;
    }
}
