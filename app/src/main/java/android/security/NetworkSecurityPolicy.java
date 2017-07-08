package android.security;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.security.net.config.ApplicationConfig;
import android.security.net.config.ManifestConfigSource;

public class NetworkSecurityPolicy {
    private static final NetworkSecurityPolicy INSTANCE = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.security.NetworkSecurityPolicy.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.security.NetworkSecurityPolicy.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.security.NetworkSecurityPolicy.<clinit>():void");
    }

    private NetworkSecurityPolicy() {
    }

    public static NetworkSecurityPolicy getInstance() {
        return INSTANCE;
    }

    public boolean isCleartextTrafficPermitted() {
        return libcore.net.NetworkSecurityPolicy.getInstance().isCleartextTrafficPermitted();
    }

    public boolean isCleartextTrafficPermitted(String hostname) {
        return libcore.net.NetworkSecurityPolicy.getInstance().isCleartextTrafficPermitted(hostname);
    }

    public void setCleartextTrafficPermitted(boolean permitted) {
        libcore.net.NetworkSecurityPolicy.setInstance(new FrameworkNetworkSecurityPolicy(permitted));
    }

    public void handleTrustStorageUpdate() {
        ApplicationConfig.getDefaultInstance().handleTrustStorageUpdate();
    }

    public static ApplicationConfig getApplicationConfigForPackage(Context context, String packageName) throws NameNotFoundException {
        return new ApplicationConfig(new ManifestConfigSource(context.createPackageContext(packageName, 0)));
    }
}
