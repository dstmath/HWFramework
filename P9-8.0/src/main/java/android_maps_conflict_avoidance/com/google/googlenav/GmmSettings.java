package android_maps_conflict_avoidance.com.google.googlenav;

import android_maps_conflict_avoidance.com.google.common.Config;
import android_maps_conflict_avoidance.com.google.map.MapPoint;

public abstract class GmmSettings {
    private static final MapPoint FEATURE_TEST_DEFAULT_START = new MapPoint(40000000, -94000000);
    private final String defaultRemoteStringVersion = "no-remote-strings";
    protected boolean isFirstInvocation;
    private boolean migrateLatitudeUserTermsPrefOnUpgrade = false;
    private final String remoteStringResource = "/strings_remote_no-remote-strings.dat";
    private boolean requireTermsAndConditionsOnUpgrade = false;
    private boolean upgradeChecked = false;

    public GmmSettings() {
        boolean z = false;
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
