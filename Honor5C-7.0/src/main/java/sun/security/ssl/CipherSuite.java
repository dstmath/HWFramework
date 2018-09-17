package sun.security.ssl;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.Map;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

final class CipherSuite implements Comparable {
    private static final boolean ALLOW_ECC = false;
    static final BulkCipher B_3DES = null;
    static final BulkCipher B_AES_128 = null;
    static final BulkCipher B_AES_256 = null;
    static final BulkCipher B_DES = null;
    static final BulkCipher B_DES_40 = null;
    static final BulkCipher B_IDEA = null;
    static final BulkCipher B_NULL = null;
    static final BulkCipher B_RC2_40 = null;
    static final BulkCipher B_RC4_128 = null;
    static final BulkCipher B_RC4_40 = null;
    static final CipherSuite C_NULL = null;
    static final CipherSuite C_SCSV = null;
    static final int DEFAULT_SUITES_PRIORITY = 300;
    static final boolean DYNAMIC_AVAILABILITY = true;
    static final MacAlg M_MD5 = null;
    static final MacAlg M_NULL = null;
    static final MacAlg M_SHA = null;
    static final MacAlg M_SHA256 = null;
    static final MacAlg M_SHA384 = null;
    static final int SUPPORTED_SUITES_PRIORITY = 1;
    private static final Map<Integer, CipherSuite> idMap = null;
    private static final Map<String, CipherSuite> nameMap = null;
    final boolean allowed;
    final BulkCipher cipher;
    final boolean exportable;
    final int id;
    final KeyExchange keyExchange;
    final MacAlg macAlg;
    final String name;
    final int obsoleted;
    final PRF prfAlg;
    final int priority;
    final int supported;

    static final class BulkCipher {
        private static final Map<BulkCipher, Boolean> availableCache = null;
        final String algorithm;
        final boolean allowed;
        final String description;
        final int expandedKeySize;
        final boolean exportable;
        final boolean isCBCMode;
        final int ivSize;
        final int keySize;
        final String transformation;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.ssl.CipherSuite.BulkCipher.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.ssl.CipherSuite.BulkCipher.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.security.ssl.CipherSuite.BulkCipher.<clinit>():void");
        }

        BulkCipher(String transformation, int keySize, int expandedKeySize, int ivSize, boolean allowed) {
            boolean z = CipherSuite.ALLOW_ECC;
            this.transformation = transformation;
            String[] splits = transformation.split("/");
            this.algorithm = splits[0];
            if (splits.length > CipherSuite.SUPPORTED_SUITES_PRIORITY) {
                z = "CBC".equalsIgnoreCase(splits[CipherSuite.SUPPORTED_SUITES_PRIORITY]);
            }
            this.isCBCMode = z;
            this.description = this.algorithm + "/" + (keySize << 3);
            this.keySize = keySize;
            this.ivSize = ivSize;
            this.allowed = allowed;
            this.expandedKeySize = expandedKeySize;
            this.exportable = CipherSuite.DYNAMIC_AVAILABILITY;
        }

        BulkCipher(String transformation, int keySize, int ivSize, boolean allowed) {
            this.transformation = transformation;
            String[] splits = transformation.split("/");
            this.algorithm = splits[0];
            this.isCBCMode = splits.length <= CipherSuite.SUPPORTED_SUITES_PRIORITY ? CipherSuite.ALLOW_ECC : "CBC".equalsIgnoreCase(splits[CipherSuite.SUPPORTED_SUITES_PRIORITY]);
            this.description = this.algorithm + "/" + (keySize << 3);
            this.keySize = keySize;
            this.ivSize = ivSize;
            this.allowed = allowed;
            this.expandedKeySize = keySize;
            this.exportable = CipherSuite.ALLOW_ECC;
        }

        CipherBox newCipher(ProtocolVersion version, SecretKey key, IvParameterSpec iv, SecureRandom random, boolean encrypt) throws NoSuchAlgorithmException {
            return CipherBox.newCipherBox(version, this, key, iv, random, encrypt);
        }

        boolean isAvailable() {
            if (!this.allowed) {
                return CipherSuite.ALLOW_ECC;
            }
            if (this == CipherSuite.B_AES_256) {
                return isAvailable(this);
            }
            return CipherSuite.DYNAMIC_AVAILABILITY;
        }

        static synchronized void clearAvailableCache() {
            synchronized (BulkCipher.class) {
                availableCache.clear();
            }
        }

        private static synchronized boolean isAvailable(BulkCipher cipher) {
            boolean booleanValue;
            synchronized (BulkCipher.class) {
                Boolean b = (Boolean) availableCache.get(cipher);
                if (b == null) {
                    try {
                        BulkCipher bulkCipher = cipher;
                        bulkCipher.newCipher(ProtocolVersion.DEFAULT, new SecretKeySpec(new byte[cipher.expandedKeySize], cipher.algorithm), new IvParameterSpec(new byte[cipher.ivSize]), null, CipherSuite.DYNAMIC_AVAILABILITY);
                        b = Boolean.TRUE;
                    } catch (NoSuchAlgorithmException e) {
                        b = Boolean.FALSE;
                    }
                    availableCache.put(cipher, b);
                }
                booleanValue = b.booleanValue();
            }
            return booleanValue;
        }

        public String toString() {
            return this.description;
        }
    }

    enum KeyExchange {
        ;
        
        final boolean allowed;
        private final boolean alwaysAvailable;
        final String name;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.ssl.CipherSuite.KeyExchange.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.ssl.CipherSuite.KeyExchange.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.security.ssl.CipherSuite.KeyExchange.<clinit>():void");
        }

        private KeyExchange(String name, boolean allowed) {
            boolean z = CipherSuite.ALLOW_ECC;
            this.name = name;
            this.allowed = allowed;
            if (!(!allowed || name.startsWith("EC") || name.startsWith("KRB"))) {
                z = CipherSuite.DYNAMIC_AVAILABILITY;
            }
            this.alwaysAvailable = z;
        }

        boolean isAvailable() {
            boolean z = CipherSuite.ALLOW_ECC;
            if (this.alwaysAvailable) {
                return CipherSuite.DYNAMIC_AVAILABILITY;
            }
            if (this.name.startsWith("EC")) {
                if (this.allowed) {
                    z = JsseJce.isEcAvailable();
                }
                return z;
            } else if (!this.name.startsWith("KRB")) {
                return this.allowed;
            } else {
                if (this.allowed) {
                    z = JsseJce.isKerberosAvailable();
                }
                return z;
            }
        }

        public String toString() {
            return this.name;
        }
    }

    static final class MacAlg {
        final int hashBlockSize;
        final int minimalPaddingSize;
        final String name;
        final int size;

        MacAlg(String name, int size, int hashBlockSize, int minimalPaddingSize) {
            this.name = name;
            this.size = size;
            this.hashBlockSize = hashBlockSize;
            this.minimalPaddingSize = minimalPaddingSize;
        }

        MAC newMac(ProtocolVersion protocolVersion, SecretKey secret) throws NoSuchAlgorithmException, InvalidKeyException {
            return new MAC(this, protocolVersion, secret);
        }

        public String toString() {
            return this.name;
        }
    }

    enum PRF {
        ;
        
        private final int prfBlockSize;
        private final String prfHashAlg;
        private final int prfHashLength;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.ssl.CipherSuite.PRF.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.ssl.CipherSuite.PRF.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.security.ssl.CipherSuite.PRF.<clinit>():void");
        }

        private PRF(String prfHashAlg, int prfHashLength, int prfBlockSize) {
            this.prfHashAlg = prfHashAlg;
            this.prfHashLength = prfHashLength;
            this.prfBlockSize = prfBlockSize;
        }

        String getPRFHashAlg() {
            return this.prfHashAlg;
        }

        int getPRFHashLength() {
            return this.prfHashLength;
        }

        int getPRFBlockSize() {
            return this.prfBlockSize;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.ssl.CipherSuite.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.ssl.CipherSuite.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.security.ssl.CipherSuite.<clinit>():void");
    }

    private CipherSuite(String name, int id, int priority, KeyExchange keyExchange, BulkCipher cipher, boolean allowed, int obsoleted, int supported, PRF prfAlg) {
        this.name = name;
        this.id = id;
        this.priority = priority;
        this.keyExchange = keyExchange;
        this.cipher = cipher;
        this.exportable = cipher.exportable;
        if (name.endsWith("_MD5")) {
            this.macAlg = M_MD5;
        } else if (name.endsWith("_SHA")) {
            this.macAlg = M_SHA;
        } else if (name.endsWith("_SHA256")) {
            this.macAlg = M_SHA256;
        } else if (name.endsWith("_SHA384")) {
            this.macAlg = M_SHA384;
        } else if (name.endsWith("_NULL")) {
            this.macAlg = M_NULL;
        } else if (name.endsWith("_SCSV")) {
            this.macAlg = M_NULL;
        } else {
            throw new IllegalArgumentException("Unknown MAC algorithm for ciphersuite " + name);
        }
        this.allowed = (allowed & keyExchange.allowed) & cipher.allowed;
        this.obsoleted = obsoleted;
        this.supported = supported;
        this.prfAlg = prfAlg;
    }

    private CipherSuite(String name, int id) {
        this.name = name;
        this.id = id;
        this.allowed = ALLOW_ECC;
        this.priority = 0;
        this.keyExchange = null;
        this.cipher = null;
        this.macAlg = null;
        this.exportable = ALLOW_ECC;
        this.obsoleted = 65535;
        this.supported = 0;
        this.prfAlg = PRF.P_NONE;
    }

    boolean isAvailable() {
        return (this.allowed && this.keyExchange.isAvailable()) ? this.cipher.isAvailable() : ALLOW_ECC;
    }

    boolean isNegotiable() {
        return this != C_SCSV ? isAvailable() : ALLOW_ECC;
    }

    public int compareTo(Object o) {
        return ((CipherSuite) o).priority - this.priority;
    }

    public String toString() {
        return this.name;
    }

    static CipherSuite valueOf(String s) {
        if (s == null) {
            throw new IllegalArgumentException("Name must not be null");
        }
        CipherSuite c = (CipherSuite) nameMap.get(s);
        if (c != null && c.allowed) {
            return c;
        }
        throw new IllegalArgumentException("Unsupported ciphersuite " + s);
    }

    static CipherSuite valueOf(int id1, int id2) {
        id1 &= 255;
        id2 &= 255;
        int id = (id1 << 8) | id2;
        CipherSuite c = (CipherSuite) idMap.get(Integer.valueOf(id));
        if (c != null) {
            return c;
        }
        String h1 = Integer.toString(id1, 16);
        return new CipherSuite("Unknown 0x" + h1 + ":0x" + Integer.toString(id2, 16), id);
    }

    static Collection<CipherSuite> allowedCipherSuites() {
        return nameMap.values();
    }

    private static void add(String name, int id, int priority, KeyExchange keyExchange, BulkCipher cipher, boolean allowed, int obsoleted, int supported, PRF prf) {
        CipherSuite c = new CipherSuite(name, id, priority, keyExchange, cipher, allowed, obsoleted, supported, prf);
        if (idMap.put(Integer.valueOf(id), c) != null) {
            throw new RuntimeException("Duplicate ciphersuite definition: " + id + ", " + name);
        } else if (c.allowed && nameMap.put(name, c) != null) {
            throw new RuntimeException("Duplicate ciphersuite definition: " + id + ", " + name);
        }
    }

    private static void add(String name, int id, int priority, KeyExchange keyExchange, BulkCipher cipher, boolean allowed, int obsoleted) {
        PRF prf = PRF.P_SHA256;
        if (obsoleted < ProtocolVersion.TLS12.v) {
            prf = PRF.P_NONE;
        }
        add(name, id, priority, keyExchange, cipher, allowed, obsoleted, 0, prf);
    }

    private static void add(String name, int id, int priority, KeyExchange keyExchange, BulkCipher cipher, boolean allowed) {
        add(name, id, priority, keyExchange, cipher, allowed, 65535);
    }

    private static void add(String name, int id) {
        if (idMap.put(Integer.valueOf(id), new CipherSuite(name, id)) != null) {
            throw new RuntimeException("Duplicate ciphersuite definition: " + id + ", " + name);
        }
    }
}
