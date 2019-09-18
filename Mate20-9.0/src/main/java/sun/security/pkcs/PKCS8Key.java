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

    public PKCS8Key() {
    }

    private PKCS8Key(AlgorithmId algid2, byte[] key2) throws InvalidKeyException {
        this.algid = algid2;
        this.key = key2;
        encode();
    }

    public static PKCS8Key parse(DerValue in) throws IOException {
        PrivateKey key2 = parseKey(in);
        if (key2 instanceof PKCS8Key) {
            return (PKCS8Key) key2;
        }
        throw new IOException("Provider did not return PKCS8Key");
    }

    public static PrivateKey parseKey(DerValue in) throws IOException {
        if (in.tag == 48) {
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
            } else {
                throw new IOException("version mismatch: (supported: " + Debug.toHexString(version) + ", parsed: " + Debug.toHexString(parsedVersion));
            }
        } else {
            throw new IOException("corrupt private key");
        }
    }

    /* access modifiers changed from: protected */
    public void parseKeyBits() throws IOException, InvalidKeyException {
        encode();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0095, code lost:
        throw new java.io.IOException(r2 + " [internal error]");
     */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x007e A[ExcHandler: IllegalAccessException (e java.lang.IllegalAccessException), PHI: r2 
      PHI: (r2v3 'classname' java.lang.String) = (r2v2 'classname' java.lang.String), (r2v4 'classname' java.lang.String), (r2v4 'classname' java.lang.String) binds: [B:6:0x0026, B:13:0x0049, B:17:0x0050] A[DONT_GENERATE, DONT_INLINE], Splitter:B:6:0x0026] */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x0096 A[ExcHandler: InstantiationException (e java.lang.InstantiationException), Splitter:B:6:0x0026] */
    static PrivateKey buildPKCS8Key(AlgorithmId algid2, byte[] key2) throws IOException, InvalidKeyException {
        Class<?> keyClass;
        DerOutputStream pkcs8EncodedKeyStream = new DerOutputStream();
        encode(pkcs8EncodedKeyStream, algid2, key2);
        try {
            return KeyFactory.getInstance(algid2.getName()).generatePrivate(new PKCS8EncodedKeySpec(pkcs8EncodedKeyStream.toByteArray()));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            String classname = "";
            try {
                Provider sunProvider = Security.getProvider("SUN");
                if (sunProvider != null) {
                    classname = sunProvider.getProperty("PrivateKey.PKCS#8." + algid2.getName());
                    if (classname != null) {
                        keyClass = null;
                        keyClass = Class.forName(classname);
                        Object inst = null;
                        if (keyClass != null) {
                            inst = keyClass.newInstance();
                        }
                        if (inst instanceof PKCS8Key) {
                            PKCS8Key result = (PKCS8Key) inst;
                            result.algid = algid2;
                            result.key = key2;
                            result.parseKeyBits();
                            return result;
                        }
                    } else {
                        throw new InstantiationException();
                    }
                } else {
                    throw new InstantiationException();
                }
            } catch (ClassNotFoundException e2) {
                ClassLoader cl = ClassLoader.getSystemClassLoader();
                if (cl != null) {
                    keyClass = cl.loadClass(classname);
                }
            } catch (InstantiationException e3) {
            } catch (IllegalAccessException e4) {
            }
        } catch (ClassNotFoundException e5) {
        } catch (InstantiationException e32) {
        } catch (IllegalAccessException e42) {
        }
        PKCS8Key result2 = new PKCS8Key();
        result2.algid = algid2;
        result2.key = key2;
        return result2;
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

    /* Debug info: failed to restart local var, previous not found, register: 2 */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [] */
    public synchronized byte[] getEncoded() {
        byte[] result;
        result = null;
        result = encode();
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
            if (val.tag == 48) {
                BigInteger version2 = val.data.getBigInteger();
                if (version2.equals(version)) {
                    this.algid = AlgorithmId.parse(val.data.getDerValue());
                    this.key = val.data.getOctetString();
                    parseKeyBits();
                    val.data.available();
                    return;
                }
                throw new IOException("version mismatch: (supported: " + Debug.toHexString(version) + ", parsed: " + Debug.toHexString(version2));
            }
            throw new InvalidKeyException("invalid key format");
        } catch (IOException e) {
            throw new InvalidKeyException("IOException : " + e.getMessage());
        }
    }

    public void decode(byte[] encodedKey2) throws InvalidKeyException {
        decode((InputStream) new ByteArrayInputStream(encodedKey2));
    }

    /* access modifiers changed from: protected */
    public Object writeReplace() throws ObjectStreamException {
        return new KeyRep(KeyRep.Type.PRIVATE, getAlgorithm(), getFormat(), getEncoded());
    }

    private void readObject(ObjectInputStream stream) throws IOException {
        try {
            decode((InputStream) stream);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            throw new IOException("deserialized key is invalid: " + e.getMessage());
        }
    }

    static void encode(DerOutputStream out, AlgorithmId algid2, byte[] key2) throws IOException {
        DerOutputStream tmp = new DerOutputStream();
        tmp.putInteger(version);
        algid2.encode(tmp);
        tmp.putOctetString(key2);
        out.write((byte) 48, tmp);
    }

    public boolean equals(Object object) {
        byte[] b1;
        if (this == object) {
            return true;
        }
        if (!(object instanceof Key)) {
            return false;
        }
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
