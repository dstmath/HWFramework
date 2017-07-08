package sun.security.ssl;

import java.net.Socket;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.security.auth.x500.X500Principal;
import sun.util.locale.BaseLocale;

final class SunX509KeyManagerImpl extends X509ExtendedKeyManager {
    private static final String[] STRING0 = null;
    private static final Debug debug = null;
    private Map<String, X509Credentials> credentialsMap;
    private Map<String, String[]> serverAliasCache;

    private static class X509Credentials {
        X509Certificate[] certificates;
        private Set<X500Principal> issuerX500Principals;
        PrivateKey privateKey;

        X509Credentials(PrivateKey privateKey, X509Certificate[] certificates) {
            this.privateKey = privateKey;
            this.certificates = certificates;
        }

        synchronized Set<X500Principal> getIssuerX500Principals() {
            if (this.issuerX500Principals == null) {
                this.issuerX500Principals = new HashSet();
                for (X509Certificate issuerX500Principal : this.certificates) {
                    this.issuerX500Principals.add(issuerX500Principal.getIssuerX500Principal());
                }
            }
            return this.issuerX500Principals;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.ssl.SunX509KeyManagerImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.ssl.SunX509KeyManagerImpl.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: sun.security.ssl.SunX509KeyManagerImpl.<clinit>():void");
    }

    SunX509KeyManagerImpl(KeyStore ks, char[] password) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
        this.credentialsMap = new HashMap();
        this.serverAliasCache = new HashMap();
        if (ks != null) {
            Enumeration<String> aliases = ks.aliases();
            while (aliases.hasMoreElements()) {
                String alias = (String) aliases.nextElement();
                if (ks.isKeyEntry(alias)) {
                    Key key = ks.getKey(alias, password);
                    if (key instanceof PrivateKey) {
                        Certificate[] certs = ks.getCertificateChain(alias);
                        if (!(certs == null || certs.length == 0 || !(certs[0] instanceof X509Certificate))) {
                            if (!(certs instanceof X509Certificate[])) {
                                Object tmp = new X509Certificate[certs.length];
                                System.arraycopy((Object) certs, 0, tmp, 0, certs.length);
                                certs = tmp;
                            }
                            this.credentialsMap.put(alias, new X509Credentials((PrivateKey) key, (X509Certificate[]) certs));
                            if (debug != null && Debug.isOn("keymanager")) {
                                System.out.println("***");
                                System.out.println("found key for : " + alias);
                                for (int i = 0; i < certs.length; i++) {
                                    System.out.println("chain [" + i + "] = " + certs[i]);
                                }
                                System.out.println("***");
                            }
                        }
                    }
                }
            }
        }
    }

    public X509Certificate[] getCertificateChain(String alias) {
        if (alias == null) {
            return null;
        }
        X509Credentials cred = (X509Credentials) this.credentialsMap.get(alias);
        if (cred == null) {
            return null;
        }
        return (X509Certificate[]) cred.certificates.clone();
    }

    public PrivateKey getPrivateKey(String alias) {
        if (alias == null) {
            return null;
        }
        X509Credentials cred = (X509Credentials) this.credentialsMap.get(alias);
        if (cred == null) {
            return null;
        }
        return cred.privateKey;
    }

    public String chooseClientAlias(String[] keyTypes, Principal[] issuers, Socket socket) {
        if (keyTypes == null) {
            return null;
        }
        for (String clientAliases : keyTypes) {
            String[] aliases = getClientAliases(clientAliases, issuers);
            if (aliases != null && aliases.length > 0) {
                return aliases[0];
            }
        }
        return null;
    }

    public String chooseEngineClientAlias(String[] keyType, Principal[] issuers, SSLEngine engine) {
        return chooseClientAlias(keyType, issuers, null);
    }

    public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
        if (keyType == null) {
            return null;
        }
        String[] aliases;
        if (issuers == null || issuers.length == 0) {
            aliases = (String[]) this.serverAliasCache.get(keyType);
            if (aliases == null) {
                aliases = getServerAliases(keyType, issuers);
                if (aliases == null) {
                    aliases = STRING0;
                }
                this.serverAliasCache.put(keyType, aliases);
            }
        } else {
            aliases = getServerAliases(keyType, issuers);
        }
        if (aliases == null || aliases.length <= 0) {
            return null;
        }
        return aliases[0];
    }

    public String chooseEngineServerAlias(String keyType, Principal[] issuers, SSLEngine engine) {
        return chooseServerAlias(keyType, issuers, null);
    }

    public String[] getClientAliases(String keyType, Principal[] issuers) {
        return getAliases(keyType, issuers);
    }

    public String[] getServerAliases(String keyType, Principal[] issuers) {
        return getAliases(keyType, issuers);
    }

    private String[] getAliases(String keyType, Principal[] issuers) {
        if (keyType == null) {
            return null;
        }
        String sigType;
        if (issuers == null) {
            issuers = new X500Principal[0];
        }
        if (!(issuers instanceof X500Principal[])) {
            issuers = convertPrincipals(issuers);
        }
        if (keyType.contains(BaseLocale.SEP)) {
            int k = keyType.indexOf(BaseLocale.SEP);
            sigType = keyType.substring(k + 1);
            keyType = keyType.substring(0, k);
        } else {
            sigType = null;
        }
        X500Principal[] x500Issuers = (X500Principal[]) issuers;
        List<String> aliases = new ArrayList();
        for (Entry<String, X509Credentials> entry : this.credentialsMap.entrySet()) {
            String alias = (String) entry.getKey();
            X509Credentials credentials = (X509Credentials) entry.getValue();
            X509Certificate[] certs = credentials.certificates;
            if (keyType.equals(certs[0].getPublicKey().getAlgorithm())) {
                if (sigType != null) {
                    if (certs.length <= 1) {
                        if (!certs[0].getSigAlgName().toUpperCase(Locale.ENGLISH).contains("WITH" + sigType.toUpperCase(Locale.ENGLISH))) {
                        }
                    } else if (!sigType.equals(certs[1].getPublicKey().getAlgorithm())) {
                    }
                }
                if (issuers.length == 0) {
                    aliases.add(alias);
                    if (debug != null && Debug.isOn("keymanager")) {
                        System.out.println("matching alias: " + alias);
                    }
                } else {
                    Set<X500Principal> certIssuers = credentials.getIssuerX500Principals();
                    int i = 0;
                    while (i < x500Issuers.length) {
                        if (certIssuers.contains(issuers[i])) {
                            aliases.add(alias);
                            if (debug != null && Debug.isOn("keymanager")) {
                                System.out.println("matching alias: " + alias);
                            }
                        } else {
                            i++;
                        }
                    }
                }
            }
        }
        String[] aliasStrings = (String[]) aliases.toArray(STRING0);
        if (aliasStrings.length == 0) {
            aliasStrings = null;
        }
        return aliasStrings;
    }

    private static X500Principal[] convertPrincipals(Principal[] principals) {
        List<X500Principal> list = new ArrayList(principals.length);
        for (Principal p : principals) {
            if (p instanceof X500Principal) {
                list.add((X500Principal) p);
            } else {
                try {
                    list.add(new X500Principal(p.getName()));
                } catch (IllegalArgumentException e) {
                }
            }
        }
        return (X500Principal[]) list.toArray(new X500Principal[list.size()]);
    }
}
