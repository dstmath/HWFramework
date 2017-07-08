package android_maps_conflict_avoidance.com.google.googlenav;

import android_maps_conflict_avoidance.com.google.common.Config;
import android_maps_conflict_avoidance.com.google.map.MapPoint;

public abstract class GmmSettings {
    private static final MapPoint FEATURE_TEST_DEFAULT_START = null;
    private final String defaultRemoteStringVersion;
    protected boolean isFirstInvocation;
    private boolean migrateLatitudeUserTermsPrefOnUpgrade;
    private final String remoteStringResource;
    private boolean requireTermsAndConditionsOnUpgrade;
    private boolean upgradeChecked;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android_maps_conflict_avoidance.com.google.googlenav.GmmSettings.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android_maps_conflict_avoidance.com.google.googlenav.GmmSettings.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android_maps_conflict_avoidance.com.google.googlenav.GmmSettings.<clinit>():void");
    }

    public GmmSettings() {
        boolean z = false;
        this.upgradeChecked = false;
        this.requireTermsAndConditionsOnUpgrade = false;
        this.migrateLatitudeUserTermsPrefOnUpgrade = false;
        this.defaultRemoteStringVersion = "no-remote-strings";
        this.remoteStringResource = "/strings_remote_no-remote-strings.dat";
        if (!isTermsAndConditionsPrefSet()) {
            z = true;
        }
        this.isFirstInvocation = z;
    }

    private static boolean isTermsAndConditionsPrefSet() {
        return Config.getInstance().getPersistentStore().readPreference("T_AND_C_ACCEPT") != null;
    }

    public static boolean isDebugBuild() {
        return false;
    }
}
