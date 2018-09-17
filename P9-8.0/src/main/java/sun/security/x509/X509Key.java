package sun.security.x509;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import sun.misc.HexDumpEncoder;
import sun.security.util.BitArray;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class X509Key implements PublicKey {
    private static final long serialVersionUID = -5359250853002055002L;
    protected AlgorithmId algid;
    private BitArray bitStringKey = null;
    protected byte[] encodedKey;
    @Deprecated
    protected byte[] key = null;
    private int unusedBits = 0;

    private X509Key(AlgorithmId algid, BitArray key) throws InvalidKeyException {
        this.algid = algid;
        setKey(key);
        encode();
    }

    protected void setKey(BitArray key) {
        this.bitStringKey = (BitArray) key.clone();
        this.key = key.toByteArray();
        int remaining = key.length() % 8;
        this.unusedBits = remaining == 0 ? 0 : 8 - remaining;
    }

    protected BitArray getKey() {
        this.bitStringKey = new BitArray((this.key.length * 8) - this.unusedBits, this.key);
        return (BitArray) this.bitStringKey.clone();
    }

    public static PublicKey parse(DerValue in) throws IOException {
        if (in.tag != (byte) 48) {
            throw new IOException("corrupt subject key");
        }
        try {
            PublicKey subjectKey = buildX509Key(AlgorithmId.parse(in.data.getDerValue()), in.data.getUnalignedBitString());
            if (in.data.available() == 0) {
                return subjectKey;
            }
            throw new IOException("excess subject key");
        } catch (InvalidKeyException e) {
            throw new IOException("subject key, " + e.getMessage(), e);
        }
    }

    protected void parseKeyBits() throws IOException, InvalidKeyException {
        encode();
    }

    /* JADX WARNING: Removed duplicated region for block: B:22:0x0075 A:{Splitter: B:9:0x0035, ExcHandler: java.lang.InstantiationException (e java.lang.InstantiationException)} */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x00a6 A:{Splitter: B:9:0x0035, ExcHandler: java.lang.IllegalAccessException (e java.lang.IllegalAccessException), PHI: r3 } */
    /* JADX WARNING: Missing block: B:40:0x00c2, code:
            throw new java.io.IOException(r3 + " [internal error]");
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static PublicKey buildX509Key(AlgorithmId algid, BitArray key) throws IOException, InvalidKeyException {
        DerOutputStream x509EncodedKeyStream = new DerOutputStream();
        encode(x509EncodedKeyStream, algid, key);
        try {
            return KeyFactory.getInstance(algid.getName()).generatePublic(new X509EncodedKeySpec(x509EncodedKeyStream.toByteArray()));
        } catch (NoSuchAlgorithmException e) {
            String classname = "";
            Class keyClass;
            try {
                Provider sunProvider = Security.getProvider("SUN");
                if (sunProvider == null) {
                    throw new InstantiationException();
                }
                classname = sunProvider.getProperty("PublicKey.X.509." + algid.getName());
                if (classname == null) {
                    throw new InstantiationException();
                }
                keyClass = null;
                keyClass = Class.forName(classname);
                Object inst = null;
                if (keyClass != null) {
                    inst = keyClass.newInstance();
                }
                if (inst instanceof X509Key) {
                    X509Key result = (X509Key) inst;
                    result.algid = algid;
                    result.setKey(key);
                    result.parseKeyBits();
                    return result;
                }
            } catch (ClassNotFoundException e2) {
                ClassLoader cl = ClassLoader.getSystemClassLoader();
                if (cl != null) {
                    keyClass = cl.loadClass(classname);
                }
            } catch (InstantiationException e3) {
            } catch (IllegalAccessException e4) {
            }
        } catch (InvalidKeySpecException e5) {
            throw new InvalidKeyException(e5.getMessage(), e5);
        } catch (ClassNotFoundException e6) {
        } catch (InstantiationException e32) {
        } catch (IllegalAccessException e42) {
        }
        return new X509Key(algid, key);
    }

    public String getAlgorithm() {
        return this.algid.getName();
    }

    public AlgorithmId getAlgorithmId() {
        return this.algid;
    }

    public final void encode(DerOutputStream out) throws IOException {
        encode(out, this.algid, getKey());
    }

    public byte[] getEncoded() {
        try {
            return (byte[]) getEncodedInternal().clone();
        } catch (InvalidKeyException e) {
            return null;
        }
    }

    public byte[] getEncodedInternal() throws InvalidKeyException {
        byte[] encoded = this.encodedKey;
        if (encoded == null) {
            try {
                DerOutputStream out = new DerOutputStream();
                encode(out);
                encoded = out.toByteArray();
                this.encodedKey = encoded;
            } catch (IOException e) {
                throw new InvalidKeyException("IOException : " + e.getMessage());
            }
        }
        return encoded;
    }

    public String getFormat() {
        return "X.509";
    }

    public byte[] encode() throws InvalidKeyException {
        return (byte[]) getEncodedInternal().clone();
    }

    public String toString() {
        return "algorithm = " + this.algid.toString() + ", unparsed keybits = \n" + new HexDumpEncoder().encodeBuffer(this.key);
    }

    public void decode(InputStream in) throws InvalidKeyException {
        try {
            DerValue val = new DerValue(in);
            if (val.tag != (byte) 48) {
                throw new InvalidKeyException("invalid key format");
            }
            this.algid = AlgorithmId.parse(val.data.getDerValue());
            setKey(val.data.getUnalignedBitString());
            parseKeyBits();
            if (val.data.available() != 0) {
                throw new InvalidKeyException("excess key data");
            }
        } catch (IOException e) {
            throw new InvalidKeyException("IOException: " + e.getMessage());
        }
    }

    public void decode(byte[] encodedKey) throws InvalidKeyException {
        decode(new ByteArrayInputStream(encodedKey));
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.write(getEncoded());
    }

    private void readObject(ObjectInputStream stream) throws IOException {
        try {
            decode((InputStream) stream);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            throw new IOException("deserialized key is invalid: " + e.getMessage());
        }
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Key)) {
            return false;
        }
        try {
            byte[] otherEncoded;
            byte[] thisEncoded = getEncodedInternal();
            if (obj instanceof X509Key) {
                otherEncoded = ((X509Key) obj).getEncodedInternal();
            } else {
                otherEncoded = ((Key) obj).getEncoded();
            }
            return Arrays.equals(thisEncoded, otherEncoded);
        } catch (InvalidKeyException e) {
            return false;
        }
    }

    public int hashCode() {
        try {
            byte[] b1 = getEncodedInternal();
            int r = b1.length;
            for (byte b : b1) {
                r += (b & 255) * 37;
            }
            return r;
        } catch (InvalidKeyException e) {
            return 0;
        }
    }

    static void encode(DerOutputStream out, AlgorithmId algid, BitArray key) throws IOException {
        DerOutputStream tmp = new DerOutputStream();
        algid.encode(tmp);
        tmp.putUnalignedBitString(key);
        out.write((byte) 48, tmp);
    }
}
