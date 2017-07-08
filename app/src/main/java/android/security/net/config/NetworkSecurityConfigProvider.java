package android.security.net.config;

import android.content.Context;
import java.security.Provider;
import java.security.Security;
import libcore.net.NetworkSecurityPolicy;

public final class NetworkSecurityConfigProvider extends Provider {
    private static final String PREFIX = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.security.net.config.NetworkSecurityConfigProvider.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.security.net.config.NetworkSecurityConfigProvider.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.security.net.config.NetworkSecurityConfigProvider.<clinit>():void");
    }

    public NetworkSecurityConfigProvider() {
        super("AndroidNSSP", 1.0d, "Android Network Security Policy Provider");
        put("TrustManagerFactory.PKIX", PREFIX + "RootTrustManagerFactorySpi");
        put("Alg.Alias.TrustManagerFactory.X509", "PKIX");
    }

    public static void install(Context context) {
        ApplicationConfig config = new ApplicationConfig(new ManifestConfigSource(context));
        ApplicationConfig.setDefaultInstance(config);
        int pos = Security.insertProviderAt(new NetworkSecurityConfigProvider(), 1);
        if (pos != 1) {
            throw new RuntimeException("Failed to install provider as highest priority provider. Provider was installed at position " + pos);
        }
        NetworkSecurityPolicy.setInstance(new ConfigNetworkSecurityPolicy(config));
    }
}
