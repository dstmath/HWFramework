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
    @Deprecated
    private int unusedBits = 0;

    public X509Key() {
    }

    private X509Key(AlgorithmId algid2, BitArray key2) throws InvalidKeyException {
        this.algid = algid2;
        setKey(key2);
        encode();
    }

    /* access modifiers changed from: protected */
    public void setKey(BitArray key2) {
        this.bitStringKey = (BitArray) key2.clone();
        this.key = key2.toByteArray();
        int remaining = key2.length() % 8;
        this.unusedBits = remaining == 0 ? 0 : 8 - remaining;
    }

    /* access modifiers changed from: protected */
    public BitArray getKey() {
        this.bitStringKey = new BitArray((this.key.length * 8) - this.unusedBits, this.key);
        return (BitArray) this.bitStringKey.clone();
    }

    public static PublicKey parse(DerValue in) throws IOException {
        if (in.tag == 48) {
            try {
                PublicKey subjectKey = buildX509Key(AlgorithmId.parse(in.data.getDerValue()), in.data.getUnalignedBitString());
                if (in.data.available() == 0) {
                    return subjectKey;
                }
                throw new IOException("excess subject key");
            } catch (InvalidKeyException e) {
                throw new IOException("subject key, " + e.getMessage(), e);
            }
        } else {
            throw new IOException("corrupt subject key");
        }
    }

    /* access modifiers changed from: protected */
    public void parseKeyBits() throws IOException, InvalidKeyException {
        encode();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:37:0x009f, code lost:
        throw new java.io.IOException(r2 + " [internal error]");
     */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x0088 A[ExcHandler: IllegalAccessException (e java.lang.IllegalAccessException), PHI: r2 
      PHI: (r2v3 'classname' java.lang.String) = (r2v2 'classname' java.lang.String), (r2v4 'classname' java.lang.String), (r2v4 'classname' java.lang.String) binds: [B:9:0x002f, B:16:0x0052, B:20:0x0059] A[DONT_GENERATE, DONT_INLINE], Splitter:B:9:0x002f] */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x00a0 A[ExcHandler: InstantiationException (e java.lang.InstantiationException), Splitter:B:9:0x002f] */
    static PublicKey buildX509Key(AlgorithmId algid2, BitArray key2) throws IOException, InvalidKeyException {
        Class<?> keyClass;
        DerOutputStream x509EncodedKeyStream = new DerOutputStream();
        encode(x509EncodedKeyStream, algid2, key2);
        try {
            return KeyFactory.getInstance(algid2.getName()).generatePublic(new X509EncodedKeySpec(x509EncodedKeyStream.toByteArray()));
        } catch (NoSuchAlgorithmException e) {
            String classname = "";
            try {
                Provider sunProvider = Security.getProvider("SUN");
                if (sunProvider != null) {
                    classname = sunProvider.getProperty("PublicKey.X.509." + algid2.getName());
                    if (classname != null) {
                        keyClass = null;
                        keyClass = Class.forName(classname);
                        Object inst = null;
                        if (keyClass != null) {
                            inst = keyClass.newInstance();
                        }
                        if (inst instanceof X509Key) {
                            X509Key result = (X509Key) inst;
                            result.algid = algid2;
                            result.setKey(key2);
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
        } catch (InvalidKeySpecException e5) {
            throw new InvalidKeyException(e5.getMessage(), e5);
        } catch (ClassNotFoundException e6) {
        } catch (InstantiationException e32) {
        } catch (IllegalAccessException e42) {
        }
        return new X509Key(algid2, key2);
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
        if (encoded != null) {
            return encoded;
        }
        try {
            DerOutputStream out = new DerOutputStream();
            encode(out);
            byte[] encoded2 = out.toByteArray();
            this.encodedKey = encoded2;
            return encoded2;
        } catch (IOException e) {
            throw new InvalidKeyException("IOException : " + e.getMessage());
        }
    }

    public String getFormat() {
        return "X.509";
    }

    public byte[] encode() throws InvalidKeyException {
        return (byte[]) getEncodedInternal().clone();
    }

    public String toString() {
        new HexDumpEncoder();
        return "algorithm = " + this.algid.toString() + ", unparsed keybits = \n" + encoder.encodeBuffer(this.key);
    }

    public void decode(InputStream in) throws InvalidKeyException {
        try {
            DerValue val = new DerValue(in);
            if (val.tag == 48) {
                this.algid = AlgorithmId.parse(val.data.getDerValue());
                setKey(val.data.getUnalignedBitString());
                parseKeyBits();
                if (val.data.available() != 0) {
                    throw new InvalidKeyException("excess key data");
                }
                return;
            }
            throw new InvalidKeyException("invalid key format");
        } catch (IOException e) {
            throw new InvalidKeyException("IOException: " + e.getMessage());
        }
    }

    public void decode(byte[] encodedKey2) throws InvalidKeyException {
        decode((InputStream) new ByteArrayInputStream(encodedKey2));
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
        byte[] otherEncoded;
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Key)) {
            return false;
        }
        try {
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
                r += (b & Character.DIRECTIONALITY_UNDEFINED) * 37;
            }
            return r;
        } catch (InvalidKeyException e) {
            return 0;
        }
    }

    static void encode(DerOutputStream out, AlgorithmId algid2, BitArray key2) throws IOException {
        DerOutputStream tmp = new DerOutputStream();
        algid2.encode(tmp);
        tmp.putUnalignedBitString(key2);
        out.write((byte) 48, tmp);
    }
}
