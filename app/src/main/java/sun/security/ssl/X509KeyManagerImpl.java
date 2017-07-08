package sun.security.ssl;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.net.Socket;
import java.security.AlgorithmConstraints;
import java.security.KeyStore;
import java.security.KeyStore.Builder;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.CertPathValidatorException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import javax.net.ssl.ExtendedSSLSession;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.net.ssl.X509KeyManager;
import sun.security.provider.certpath.AlgorithmChecker;
import sun.util.locale.BaseLocale;

final class X509KeyManagerImpl extends X509ExtendedKeyManager implements X509KeyManager {
    private static final Debug debug = null;
    private static final boolean useDebug = false;
    private static Date verificationDate;
    private final List<Builder> builders;
    private final Map<String, Reference<PrivateKeyEntry>> entryCacheMap;
    private final AtomicLong uidCounter;

    private enum CheckResult {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.ssl.X509KeyManagerImpl.CheckResult.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.ssl.X509KeyManagerImpl.CheckResult.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: sun.security.ssl.X509KeyManagerImpl.CheckResult.<clinit>():void");
        }
    }

    private enum CheckType {
        ;
        
        final Set<String> validEku;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.ssl.X509KeyManagerImpl.CheckType.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.ssl.X509KeyManagerImpl.CheckType.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: sun.security.ssl.X509KeyManagerImpl.CheckType.<clinit>():void");
        }

        private CheckType(Set<String> validEku) {
            this.validEku = validEku;
        }

        private static boolean getBit(boolean[] keyUsage, int bit) {
            return bit < keyUsage.length ? keyUsage[bit] : false;
        }

        CheckResult check(X509Certificate cert, Date date) {
            if (this == NONE) {
                return CheckResult.OK;
            }
            try {
                List<String> certEku = cert.getExtendedKeyUsage();
                if (certEku != null && Collections.disjoint(this.validEku, certEku)) {
                    return CheckResult.EXTENSION_MISMATCH;
                }
                boolean[] ku = cert.getKeyUsage();
                if (ku != null) {
                    String algorithm = cert.getPublicKey().getAlgorithm();
                    boolean kuSignature = getBit(ku, 0);
                    if (algorithm.equals("RSA")) {
                        if (!kuSignature && (this == CLIENT || !getBit(ku, 2))) {
                            return CheckResult.EXTENSION_MISMATCH;
                        }
                    } else if (algorithm.equals("DSA")) {
                        if (!kuSignature) {
                            return CheckResult.EXTENSION_MISMATCH;
                        }
                    } else if (algorithm.equals("DH")) {
                        if (!getBit(ku, 4)) {
                            return CheckResult.EXTENSION_MISMATCH;
                        }
                    } else if (algorithm.equals("EC")) {
                        if (!kuSignature) {
                            return CheckResult.EXTENSION_MISMATCH;
                        }
                        if (this == SERVER && !getBit(ku, 4)) {
                            return CheckResult.EXTENSION_MISMATCH;
                        }
                    }
                }
                try {
                    cert.checkValidity(date);
                    return CheckResult.OK;
                } catch (CertificateException e) {
                    return CheckResult.EXPIRED;
                }
            } catch (CertificateException e2) {
                return CheckResult.EXTENSION_MISMATCH;
            }
        }
    }

    private static class EntryStatus implements Comparable<EntryStatus> {
        final String alias;
        final int builderIndex;
        final CheckResult checkResult;
        final int keyIndex;

        EntryStatus(int builderIndex, int keyIndex, String alias, Certificate[] chain, CheckResult checkResult) {
            this.builderIndex = builderIndex;
            this.keyIndex = keyIndex;
            this.alias = alias;
            this.checkResult = checkResult;
        }

        public /* bridge */ /* synthetic */ int compareTo(Object other) {
            return compareTo((EntryStatus) other);
        }

        public int compareTo(EntryStatus other) {
            int result = this.checkResult.compareTo(other.checkResult);
            return result == 0 ? this.keyIndex - other.keyIndex : result;
        }

        public String toString() {
            String s = this.alias + " (verified: " + this.checkResult + ")";
            if (this.builderIndex == 0) {
                return s;
            }
            return "Builder #" + this.builderIndex + ", alias: " + s;
        }
    }

    private static class KeyType {
        final String keyAlgorithm;
        final String sigKeyAlgorithm;

        KeyType(String algorithm) {
            int k = algorithm.indexOf(BaseLocale.SEP);
            if (k == -1) {
                this.keyAlgorithm = algorithm;
                this.sigKeyAlgorithm = null;
                return;
            }
            this.keyAlgorithm = algorithm.substring(0, k);
            this.sigKeyAlgorithm = algorithm.substring(k + 1);
        }

        boolean matches(Certificate[] chain) {
            if (!chain[0].getPublicKey().getAlgorithm().equals(this.keyAlgorithm)) {
                return false;
            }
            if (this.sigKeyAlgorithm == null) {
                return true;
            }
            if (chain.length > 1) {
                return this.sigKeyAlgorithm.equals(chain[1].getPublicKey().getAlgorithm());
            }
            return chain[0].getSigAlgName().toUpperCase(Locale.ENGLISH).contains("WITH" + this.sigKeyAlgorithm.toUpperCase(Locale.ENGLISH));
        }
    }

    private static class SizedMap<K, V> extends LinkedHashMap<K, V> {
        /* synthetic */ SizedMap(SizedMap sizedMap) {
            this();
        }

        private SizedMap() {
        }

        protected boolean removeEldestEntry(Entry<K, V> entry) {
            return size() > 10;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.ssl.X509KeyManagerImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.ssl.X509KeyManagerImpl.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: sun.security.ssl.X509KeyManagerImpl.<clinit>():void");
    }

    X509KeyManagerImpl(Builder builder) {
        this(Collections.singletonList(builder));
    }

    X509KeyManagerImpl(List<Builder> builders) {
        this.builders = builders;
        this.uidCounter = new AtomicLong();
        this.entryCacheMap = Collections.synchronizedMap(new SizedMap());
    }

    public X509Certificate[] getCertificateChain(String alias) {
        PrivateKeyEntry entry = getEntry(alias);
        if (entry == null) {
            return null;
        }
        return (X509Certificate[]) entry.getCertificateChain();
    }

    public PrivateKey getPrivateKey(String alias) {
        PrivateKeyEntry entry = getEntry(alias);
        if (entry == null) {
            return null;
        }
        return entry.getPrivateKey();
    }

    public String chooseClientAlias(String[] keyTypes, Principal[] issuers, Socket socket) {
        return chooseAlias(getKeyTypes(keyTypes), issuers, CheckType.CLIENT, getAlgorithmConstraints(socket));
    }

    public String chooseEngineClientAlias(String[] keyTypes, Principal[] issuers, SSLEngine engine) {
        return chooseAlias(getKeyTypes(keyTypes), issuers, CheckType.CLIENT, getAlgorithmConstraints(engine));
    }

    public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
        return chooseAlias(getKeyTypes(keyType), issuers, CheckType.SERVER, getAlgorithmConstraints(socket));
    }

    public String chooseEngineServerAlias(String keyType, Principal[] issuers, SSLEngine engine) {
        return chooseAlias(getKeyTypes(keyType), issuers, CheckType.SERVER, getAlgorithmConstraints(engine));
    }

    public String[] getClientAliases(String keyType, Principal[] issuers) {
        return getAliases(keyType, issuers, CheckType.CLIENT, null);
    }

    public String[] getServerAliases(String keyType, Principal[] issuers) {
        return getAliases(keyType, issuers, CheckType.SERVER, null);
    }

    private AlgorithmConstraints getAlgorithmConstraints(Socket socket) {
        if (socket == null || !socket.isConnected() || !(socket instanceof SSLSocket)) {
            return new SSLAlgorithmConstraints((SSLSocket) null, true);
        }
        SSLSocket sslSocket = (SSLSocket) socket;
        SSLSession session = sslSocket.getHandshakeSession();
        if (session == null || ProtocolVersion.valueOf(session.getProtocol()).v < ProtocolVersion.TLS12.v) {
            return new SSLAlgorithmConstraints(sslSocket, true);
        }
        String[] peerSupportedSignAlgs = null;
        if (session instanceof ExtendedSSLSession) {
            peerSupportedSignAlgs = ((ExtendedSSLSession) session).getPeerSupportedSignatureAlgorithms();
        }
        return new SSLAlgorithmConstraints(sslSocket, peerSupportedSignAlgs, true);
    }

    private AlgorithmConstraints getAlgorithmConstraints(SSLEngine engine) {
        if (engine != null) {
            SSLSession session = engine.getHandshakeSession();
            if (session != null && ProtocolVersion.valueOf(session.getProtocol()).v >= ProtocolVersion.TLS12.v) {
                String[] peerSupportedSignAlgs = null;
                if (session instanceof ExtendedSSLSession) {
                    peerSupportedSignAlgs = ((ExtendedSSLSession) session).getPeerSupportedSignatureAlgorithms();
                }
                return new SSLAlgorithmConstraints(engine, peerSupportedSignAlgs, true);
            }
        }
        return new SSLAlgorithmConstraints(engine, true);
    }

    private String makeAlias(EntryStatus entry) {
        return this.uidCounter.incrementAndGet() + "." + entry.builderIndex + "." + entry.alias;
    }

    private PrivateKeyEntry getEntry(String alias) {
        if (alias == null) {
            return null;
        }
        PrivateKeyEntry entry;
        Reference<PrivateKeyEntry> ref = (Reference) this.entryCacheMap.get(alias);
        if (ref != null) {
            entry = (PrivateKeyEntry) ref.get();
        } else {
            entry = null;
        }
        if (entry != null) {
            return entry;
        }
        int firstDot = alias.indexOf(46);
        int secondDot = alias.indexOf(46, firstDot + 1);
        if (firstDot == -1 || secondDot == firstDot) {
            return null;
        }
        try {
            int builderIndex = Integer.parseInt(alias.substring(firstDot + 1, secondDot));
            Builder builder = (Builder) this.builders.get(builderIndex);
            KeyStore.Entry newEntry = builder.getKeyStore().getEntry(alias.substring(secondDot + 1), builder.getProtectionParameter(alias));
            if (!(newEntry instanceof PrivateKeyEntry)) {
                return null;
            }
            entry = (PrivateKeyEntry) newEntry;
            this.entryCacheMap.put(alias, new SoftReference(entry));
            return entry;
        } catch (Exception e) {
            return null;
        }
    }

    private static List<KeyType> getKeyTypes(String... keyTypes) {
        int i = 0;
        if (keyTypes == null || keyTypes.length == 0 || keyTypes[0] == null) {
            return null;
        }
        List<KeyType> list = new ArrayList(keyTypes.length);
        int length = keyTypes.length;
        while (i < length) {
            list.add(new KeyType(keyTypes[i]));
            i++;
        }
        return list;
    }

    private String chooseAlias(List<KeyType> keyTypeList, Principal[] issuers, CheckType checkType, AlgorithmConstraints constraints) {
        if (keyTypeList == null || keyTypeList.size() == 0) {
            return null;
        }
        Set<Principal> issuerSet = getIssuerSet(issuers);
        int i = 0;
        int n = this.builders.size();
        List<EntryStatus> allResults = null;
        while (i < n) {
            List<EntryStatus> allResults2;
            try {
                List<EntryStatus> results = getAliases(i, keyTypeList, issuerSet, false, checkType, constraints);
                if (results != null) {
                    Object status = (EntryStatus) results.get(0);
                    if (status.checkResult == CheckResult.OK) {
                        if (useDebug) {
                            debug.println("KeyMgr: choosing key: " + status);
                        }
                        return makeAlias(status);
                    }
                    if (allResults == null) {
                        allResults2 = new ArrayList();
                    } else {
                        allResults2 = allResults;
                    }
                    try {
                        allResults2.addAll(results);
                    } catch (Exception e) {
                    }
                } else {
                    allResults2 = allResults;
                }
            } catch (Exception e2) {
                allResults2 = allResults;
            }
            i++;
            allResults = allResults2;
        }
        if (allResults == null) {
            if (useDebug) {
                debug.println("KeyMgr: no matching key found");
            }
            return null;
        }
        Collections.sort(allResults);
        if (useDebug) {
            debug.println("KeyMgr: no good matching key found, returning best match out of:");
            debug.println(allResults.toString());
        }
        return makeAlias((EntryStatus) allResults.get(0));
    }

    public String[] getAliases(String keyType, Principal[] issuers, CheckType checkType, AlgorithmConstraints constraints) {
        if (keyType == null) {
            return null;
        }
        Set<Principal> issuerSet = getIssuerSet(issuers);
        List<KeyType> keyTypeList = getKeyTypes(keyType);
        int i = 0;
        int n = this.builders.size();
        List<EntryStatus> allResults = null;
        while (i < n) {
            List<EntryStatus> allResults2;
            try {
                List<EntryStatus> results = getAliases(i, keyTypeList, issuerSet, true, checkType, constraints);
                if (results != null) {
                    if (allResults == null) {
                        allResults2 = new ArrayList();
                    } else {
                        allResults2 = allResults;
                    }
                    try {
                        allResults2.addAll(results);
                    } catch (Exception e) {
                    }
                } else {
                    allResults2 = allResults;
                }
            } catch (Exception e2) {
                allResults2 = allResults;
            }
            i++;
            allResults = allResults2;
        }
        if (allResults == null || allResults.size() == 0) {
            if (useDebug) {
                debug.println("KeyMgr: no matching alias found");
            }
            return null;
        }
        Collections.sort(allResults);
        if (useDebug) {
            debug.println("KeyMgr: getting aliases: " + allResults);
        }
        return toAliases(allResults);
    }

    private String[] toAliases(List<EntryStatus> results) {
        String[] s = new String[results.size()];
        int i = 0;
        for (EntryStatus result : results) {
            int i2 = i + 1;
            s[i] = makeAlias(result);
            i = i2;
        }
        return s;
    }

    private Set<Principal> getIssuerSet(Principal[] issuers) {
        if (issuers == null || issuers.length == 0) {
            return null;
        }
        return new HashSet(Arrays.asList(issuers));
    }

    private List<EntryStatus> getAliases(int builderIndex, List<KeyType> keyTypes, Set<Principal> issuerSet, boolean findAll, CheckType checkType, AlgorithmConstraints constraints) throws Exception {
        KeyStore ks = ((Builder) this.builders.get(builderIndex)).getKeyStore();
        List<EntryStatus> results = null;
        Date date = verificationDate;
        boolean preferred = false;
        Enumeration<String> e = ks.aliases();
        while (e.hasMoreElements()) {
            String alias = (String) e.nextElement();
            if (ks.isKeyEntry(alias)) {
                Certificate[] chain = ks.getCertificateChain(alias);
                if (!(chain == null || chain.length == 0)) {
                    boolean incompatible = false;
                    for (Certificate cert : chain) {
                        if (!(cert instanceof X509Certificate)) {
                            incompatible = true;
                            break;
                        }
                    }
                    if (incompatible) {
                        continue;
                    } else {
                        int keyIndex = -1;
                        int j = 0;
                        for (KeyType keyType : keyTypes) {
                            if (keyType.matches(chain)) {
                                keyIndex = j;
                                break;
                            }
                            j++;
                        }
                        if (keyIndex != -1) {
                            if (issuerSet != null) {
                                boolean found = false;
                                for (Certificate xcert : chain) {
                                    if (issuerSet.contains(((X509Certificate) xcert).getIssuerX500Principal())) {
                                        found = true;
                                        break;
                                    }
                                }
                                if (!found) {
                                    if (useDebug) {
                                        debug.println("Ignoring alias " + alias + ": issuers do not match");
                                    }
                                }
                            }
                            if (constraints == null || conformsToAlgorithmConstraints(constraints, chain)) {
                                if (date == null) {
                                    date = new Date();
                                }
                                CheckResult checkResult = checkType.check((X509Certificate) chain[0], date);
                                EntryStatus status = new EntryStatus(builderIndex, keyIndex, alias, chain, checkResult);
                                if (!preferred && checkResult == CheckResult.OK && keyIndex == 0) {
                                    preferred = true;
                                }
                                if (preferred && !findAll) {
                                    return Collections.singletonList(status);
                                }
                                if (results == null) {
                                    results = new ArrayList();
                                }
                                results.add(status);
                            } else if (useDebug) {
                                debug.println("Ignoring alias " + alias + ": certificate list does not conform to " + "algorithm constraints");
                            }
                        } else if (useDebug) {
                            debug.println("Ignoring alias " + alias + ": key algorithm does not match");
                        }
                    }
                }
            }
        }
        return results;
    }

    private static boolean conformsToAlgorithmConstraints(AlgorithmConstraints constraints, Certificate[] chain) {
        AlgorithmChecker checker = new AlgorithmChecker(constraints);
        try {
            checker.init(false);
            int i = chain.length - 1;
            while (i >= 0) {
                try {
                    checker.check(chain[i], Collections.emptySet());
                    i--;
                } catch (CertPathValidatorException e) {
                    return false;
                }
            }
            return true;
        } catch (CertPathValidatorException e2) {
            return false;
        }
    }
}
