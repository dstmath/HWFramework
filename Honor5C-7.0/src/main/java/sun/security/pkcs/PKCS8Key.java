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
import sun.misc.HexDumpEncoder;
import sun.security.util.Debug;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.x509.AlgorithmId;

public class PKCS8Key implements PrivateKey {
    private static final long serialVersionUID = -3836890099307167124L;
    public static final BigInteger version = null;
    protected AlgorithmId algid;
    protected byte[] encodedKey;
    protected byte[] key;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.pkcs.PKCS8Key.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.pkcs.PKCS8Key.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.security.pkcs.PKCS8Key.<clinit>():void");
    }

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
        if (in.tag != 48) {
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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static PrivateKey buildPKCS8Key(AlgorithmId algid, byte[] key) throws IOException, InvalidKeyException {
        String classname;
        Provider sunProvider;
        Class cls;
        Object inst;
        PKCS8Key result;
        DerOutputStream pkcs8EncodedKeyStream = new DerOutputStream();
        encode(pkcs8EncodedKeyStream, algid, key);
        try {
            return KeyFactory.getInstance(algid.getName()).generatePrivate(new PKCS8EncodedKeySpec(pkcs8EncodedKeyStream.toByteArray()));
        } catch (NoSuchAlgorithmException e) {
            classname = "";
            try {
                sunProvider = Security.getProvider("SUN");
                if (sunProvider != null) {
                    throw new InstantiationException();
                }
                classname = sunProvider.getProperty("PrivateKey.PKCS#8." + algid.getName());
                if (classname != null) {
                    throw new InstantiationException();
                }
                cls = null;
                cls = Class.forName(classname);
                inst = null;
                if (cls != null) {
                    inst = cls.newInstance();
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
            } catch (ClassNotFoundException e2) {
            } catch (InstantiationException e3) {
            } catch (IllegalAccessException e4) {
                throw new IOException(classname + " [internal error]");
            }
        } catch (InvalidKeySpecException e5) {
            classname = "";
            sunProvider = Security.getProvider("SUN");
            if (sunProvider != null) {
                classname = sunProvider.getProperty("PrivateKey.PKCS#8." + algid.getName());
                if (classname != null) {
                    cls = null;
                    cls = Class.forName(classname);
                    inst = null;
                    if (cls != null) {
                        inst = cls.newInstance();
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
                }
                throw new InstantiationException();
            }
            throw new InstantiationException();
        }
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
        byte[] bArr;
        bArr = null;
        try {
            bArr = encode();
        } catch (InvalidKeyException e) {
        }
        return bArr;
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

    public String toString() {
        return "algorithm = " + this.algid.toString() + ", unparsed keybits = \n" + new HexDumpEncoder().encodeBuffer(this.key);
    }

    public void decode(InputStream in) throws InvalidKeyException {
        try {
            DerValue val = new DerValue(in);
            if (val.tag != 48) {
                throw new InvalidKeyException("invalid key format");
            }
            BigInteger version = val.data.getBigInteger();
            if (version.equals(version)) {
                this.algid = AlgorithmId.parse(val.data.getDerValue());
                this.key = val.data.getOctetString();
                parseKeyBits();
                if (val.data.available() == 0) {
                    return;
                }
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
        out.write((byte) DerValue.tag_SequenceOf, tmp);
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
