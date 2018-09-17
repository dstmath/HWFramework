package sun.security.ssl;

import java.lang.reflect.Modifier;
import java.security.AlgorithmConstraints;
import java.security.CryptoPrimitive;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import sun.security.util.KeyUtil;
import sun.security.x509.GeneralNameInterface;
import sun.util.calendar.BaseCalendar;
import sun.util.logging.PlatformLogger;

final class SignatureAndHashAlgorithm {
    private static final Set<CryptoPrimitive> SIGNATURE_PRIMITIVE_SET = null;
    static final int SUPPORTED_ALG_PRIORITY_MAX_NUM = 240;
    private static final Map<Integer, SignatureAndHashAlgorithm> priorityMap = null;
    private static final Map<Integer, SignatureAndHashAlgorithm> supportedMap = null;
    private String algorithm;
    private HashAlgorithm hash;
    private int id;
    private int priority;
    private SignatureAlgorithm signature;

    enum HashAlgorithm {
        ;
        
        final int length;
        final String name;
        final String standardName;
        final int value;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.ssl.SignatureAndHashAlgorithm.HashAlgorithm.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.ssl.SignatureAndHashAlgorithm.HashAlgorithm.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.security.ssl.SignatureAndHashAlgorithm.HashAlgorithm.<clinit>():void");
        }

        private HashAlgorithm(String name, String standardName, int value, int length) {
            this.name = name;
            this.standardName = standardName;
            this.value = value;
            this.length = length;
        }

        static HashAlgorithm valueOf(int value) {
            HashAlgorithm algorithm = UNDEFINED;
            switch (value) {
                case GeneralNameInterface.NAME_MATCH /*0*/:
                    return NONE;
                case BaseCalendar.SUNDAY /*1*/:
                    return MD5;
                case BaseCalendar.MONDAY /*2*/:
                    return SHA1;
                case BaseCalendar.TUESDAY /*3*/:
                    return SHA224;
                case BaseCalendar.WEDNESDAY /*4*/:
                    return SHA256;
                case BaseCalendar.THURSDAY /*5*/:
                    return SHA384;
                case BaseCalendar.JUNE /*6*/:
                    return SHA512;
                default:
                    return algorithm;
            }
        }
    }

    enum SignatureAlgorithm {
        ;
        
        final String name;
        final int value;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.ssl.SignatureAndHashAlgorithm.SignatureAlgorithm.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.ssl.SignatureAndHashAlgorithm.SignatureAlgorithm.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.security.ssl.SignatureAndHashAlgorithm.SignatureAlgorithm.<clinit>():void");
        }

        private SignatureAlgorithm(String name, int value) {
            this.name = name;
            this.value = value;
        }

        static SignatureAlgorithm valueOf(int value) {
            SignatureAlgorithm algorithm = UNDEFINED;
            switch (value) {
                case GeneralNameInterface.NAME_MATCH /*0*/:
                    return ANONYMOUS;
                case BaseCalendar.SUNDAY /*1*/:
                    return RSA;
                case BaseCalendar.MONDAY /*2*/:
                    return DSA;
                case BaseCalendar.TUESDAY /*3*/:
                    return ECDSA;
                default:
                    return algorithm;
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.ssl.SignatureAndHashAlgorithm.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.ssl.SignatureAndHashAlgorithm.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.security.ssl.SignatureAndHashAlgorithm.<clinit>():void");
    }

    private SignatureAndHashAlgorithm(HashAlgorithm hash, SignatureAlgorithm signature, String algorithm, int priority) {
        this.hash = hash;
        this.signature = signature;
        this.algorithm = algorithm;
        this.id = ((hash.value & 255) << 8) | (signature.value & 255);
        this.priority = priority;
    }

    private SignatureAndHashAlgorithm(String algorithm, int id, int sequence) {
        this.hash = HashAlgorithm.valueOf((id >> 8) & 255);
        this.signature = SignatureAlgorithm.valueOf(id & 255);
        this.algorithm = algorithm;
        this.id = id;
        this.priority = (sequence + SUPPORTED_ALG_PRIORITY_MAX_NUM) + 1;
    }

    static SignatureAndHashAlgorithm valueOf(int hash, int signature, int sequence) {
        hash &= 255;
        signature &= 255;
        int id = (hash << 8) | signature;
        SignatureAndHashAlgorithm signAlg = (SignatureAndHashAlgorithm) supportedMap.get(Integer.valueOf(id));
        if (signAlg == null) {
            return new SignatureAndHashAlgorithm("Unknown (hash:0x" + Integer.toString(hash, 16) + ", signature:0x" + Integer.toString(signature, 16) + ")", id, sequence);
        }
        return signAlg;
    }

    int getHashValue() {
        return (this.id >> 8) & 255;
    }

    int getSignatureValue() {
        return this.id & 255;
    }

    String getAlgorithmName() {
        return this.algorithm;
    }

    static int sizeInRecord() {
        return 2;
    }

    static Collection<SignatureAndHashAlgorithm> getSupportedAlgorithms(AlgorithmConstraints constraints) {
        Collection<SignatureAndHashAlgorithm> supported = new ArrayList();
        synchronized (priorityMap) {
            for (SignatureAndHashAlgorithm sigAlg : priorityMap.values()) {
                if (sigAlg.priority <= SUPPORTED_ALG_PRIORITY_MAX_NUM && constraints.permits(SIGNATURE_PRIMITIVE_SET, sigAlg.algorithm, null)) {
                    supported.add(sigAlg);
                }
            }
        }
        return supported;
    }

    static Collection<SignatureAndHashAlgorithm> getSupportedAlgorithms(Collection<SignatureAndHashAlgorithm> algorithms) {
        Collection<SignatureAndHashAlgorithm> supported = new ArrayList();
        for (SignatureAndHashAlgorithm sigAlg : algorithms) {
            if (sigAlg.priority <= SUPPORTED_ALG_PRIORITY_MAX_NUM) {
                supported.add(sigAlg);
            }
        }
        return supported;
    }

    static String[] getAlgorithmNames(Collection<SignatureAndHashAlgorithm> algorithms) {
        ArrayList<String> algorithmNames = new ArrayList();
        if (algorithms != null) {
            for (SignatureAndHashAlgorithm sigAlg : algorithms) {
                algorithmNames.add(sigAlg.algorithm);
            }
        }
        return (String[]) algorithmNames.toArray(new String[algorithmNames.size()]);
    }

    static Set<String> getHashAlgorithmNames(Collection<SignatureAndHashAlgorithm> algorithms) {
        Set<String> algorithmNames = new HashSet();
        if (algorithms != null) {
            for (SignatureAndHashAlgorithm sigAlg : algorithms) {
                if (sigAlg.hash.value > 0) {
                    algorithmNames.add(sigAlg.hash.standardName);
                }
            }
        }
        return algorithmNames;
    }

    static String getHashAlgorithmName(SignatureAndHashAlgorithm algorithm) {
        return algorithm.hash.standardName;
    }

    private static void supports(HashAlgorithm hash, SignatureAlgorithm signature, String algorithm, int priority) {
        SignatureAndHashAlgorithm pair = new SignatureAndHashAlgorithm(hash, signature, algorithm, priority);
        if (supportedMap.put(Integer.valueOf(pair.id), pair) != null) {
            throw new RuntimeException("Duplicate SignatureAndHashAlgorithm definition, id: " + pair.id);
        } else if (priorityMap.put(Integer.valueOf(pair.priority), pair) != null) {
            throw new RuntimeException("Duplicate SignatureAndHashAlgorithm definition, priority: " + pair.priority);
        }
    }

    static SignatureAndHashAlgorithm getPreferableAlgorithm(Collection<SignatureAndHashAlgorithm> algorithms, String expected) {
        return getPreferableAlgorithm(algorithms, expected, null);
    }

    static SignatureAndHashAlgorithm getPreferableAlgorithm(Collection<SignatureAndHashAlgorithm> algorithms, String expected, PrivateKey signingKey) {
        if (expected == null && !algorithms.isEmpty()) {
            for (SignatureAndHashAlgorithm sigAlg : algorithms) {
                if (sigAlg.priority <= SUPPORTED_ALG_PRIORITY_MAX_NUM) {
                    return sigAlg;
                }
            }
            return null;
        } else if (expected == null) {
            return null;
        } else {
            int maxDigestLength = PlatformLogger.OFF;
            if (signingKey != null && "rsa".equalsIgnoreCase(signingKey.getAlgorithm()) && expected.equalsIgnoreCase("rsa")) {
                int keySize = KeyUtil.getKeySize(signingKey);
                if (keySize >= 768) {
                    maxDigestLength = HashAlgorithm.SHA512.length;
                } else if (keySize >= Modifier.INTERFACE && keySize < 768) {
                    maxDigestLength = HashAlgorithm.SHA256.length;
                } else if (keySize > 0 && keySize < Modifier.INTERFACE) {
                    maxDigestLength = HashAlgorithm.SHA1.length;
                }
            }
            for (SignatureAndHashAlgorithm algorithm : algorithms) {
                int signValue = algorithm.id & 255;
                if (!expected.equalsIgnoreCase("rsa") || signValue != SignatureAlgorithm.RSA.value) {
                    if (!((expected.equalsIgnoreCase("dsa") && signValue == SignatureAlgorithm.DSA.value) || (expected.equalsIgnoreCase("ecdsa") && signValue == SignatureAlgorithm.ECDSA.value))) {
                        if (expected.equalsIgnoreCase("ec") && signValue == SignatureAlgorithm.ECDSA.value) {
                        }
                    }
                    return algorithm;
                } else if (algorithm.hash.length <= maxDigestLength) {
                    return algorithm;
                }
            }
            return null;
        }
    }
}
