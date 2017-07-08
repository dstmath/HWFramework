package android.security.net.config;

import android.util.Pair;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import javax.net.ssl.X509TrustManager;

public final class ApplicationConfig {
    private static ApplicationConfig sInstance;
    private static Object sLock;
    private ConfigSource mConfigSource;
    private Set<Pair<Domain, NetworkSecurityConfig>> mConfigs;
    private NetworkSecurityConfig mDefaultConfig;
    private boolean mInitialized;
    private final Object mLock;
    private X509TrustManager mTrustManager;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.security.net.config.ApplicationConfig.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.security.net.config.ApplicationConfig.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.security.net.config.ApplicationConfig.<clinit>():void");
    }

    public ApplicationConfig(ConfigSource configSource) {
        this.mLock = new Object();
        this.mConfigSource = configSource;
        this.mInitialized = false;
    }

    public boolean hasPerDomainConfigs() {
        ensureInitialized();
        if (this.mConfigs == null || this.mConfigs.isEmpty()) {
            return false;
        }
        return true;
    }

    public NetworkSecurityConfig getConfigForHostname(String hostname) {
        ensureInitialized();
        if (hostname == null || hostname.isEmpty() || this.mConfigs == null) {
            return this.mDefaultConfig;
        }
        if (hostname.charAt(0) == '.') {
            throw new IllegalArgumentException("hostname must not begin with a .");
        }
        hostname = hostname.toLowerCase(Locale.US);
        if (hostname.charAt(hostname.length() - 1) == '.') {
            hostname = hostname.substring(0, hostname.length() - 1);
        }
        Pair bestMatch = null;
        for (Pair<Domain, NetworkSecurityConfig> entry : this.mConfigs) {
            Domain domain = entry.first;
            NetworkSecurityConfig config = entry.second;
            if (domain.hostname.equals(hostname)) {
                return config;
            }
            if (domain.subdomainsIncluded && hostname.endsWith(domain.hostname) && hostname.charAt((hostname.length() - domain.hostname.length()) - 1) == '.') {
                if (bestMatch == null) {
                    bestMatch = entry;
                } else if (domain.hostname.length() > ((Domain) bestMatch.first).hostname.length()) {
                    bestMatch = entry;
                }
            }
        }
        if (bestMatch != null) {
            return (NetworkSecurityConfig) bestMatch.second;
        }
        return this.mDefaultConfig;
    }

    public X509TrustManager getTrustManager() {
        ensureInitialized();
        return this.mTrustManager;
    }

    public boolean isCleartextTrafficPermitted() {
        ensureInitialized();
        if (this.mConfigs != null) {
            for (Pair<Domain, NetworkSecurityConfig> entry : this.mConfigs) {
                if (!((NetworkSecurityConfig) entry.second).isCleartextTrafficPermitted()) {
                    return false;
                }
            }
        }
        return this.mDefaultConfig.isCleartextTrafficPermitted();
    }

    public boolean isCleartextTrafficPermitted(String hostname) {
        return getConfigForHostname(hostname).isCleartextTrafficPermitted();
    }

    public void handleTrustStorageUpdate() {
        ensureInitialized();
        this.mDefaultConfig.handleTrustStorageUpdate();
        if (this.mConfigs != null) {
            Set<NetworkSecurityConfig> updatedConfigs = new HashSet(this.mConfigs.size());
            for (Pair<Domain, NetworkSecurityConfig> entry : this.mConfigs) {
                if (updatedConfigs.add((NetworkSecurityConfig) entry.second)) {
                    ((NetworkSecurityConfig) entry.second).handleTrustStorageUpdate();
                }
            }
        }
    }

    private void ensureInitialized() {
        synchronized (this.mLock) {
            if (this.mInitialized) {
                return;
            }
            this.mConfigs = this.mConfigSource.getPerDomainConfigs();
            this.mDefaultConfig = this.mConfigSource.getDefaultConfig();
            this.mConfigSource = null;
            this.mTrustManager = new RootTrustManager(this);
            this.mInitialized = true;
        }
    }

    public static void setDefaultInstance(ApplicationConfig config) {
        synchronized (sLock) {
            sInstance = config;
        }
    }

    public static ApplicationConfig getDefaultInstance() {
        ApplicationConfig applicationConfig;
        synchronized (sLock) {
            applicationConfig = sInstance;
        }
        return applicationConfig;
    }
}
