package com.android.server.pm;

import android.content.Context;
import android.content.pm.PackageParser;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Slog;
import com.android.internal.util.ArrayUtils;

public class GunstallUtil {
    private static final String GMS_CORE_NAME = "com.google.android.gms";
    private static final int GUNSTALL_USER_STATE_ALLOW = 1;
    private static final int GUNSTALL_USER_STATE_AUTO = 0;
    private static final int GUNSTALL_USER_STATE_FORBID = 2;
    private static final GunstallUtil INSTANCE = new GunstallUtil();
    private static final String PLAY_STORE_NAME = "com.android.vending";
    private static final String TAG = "GunstallUtil";
    private Context mContext = null;
    private IHwPackageManagerInner mIPmsInner = null;

    private GunstallUtil() {
    }

    public static GunstallUtil getInstance() {
        return INSTANCE;
    }

    public void initPackageManagerInner(IHwPackageManagerInner pms, Context context) {
        this.mIPmsInner = pms;
        this.mContext = context;
    }

    public boolean forbidGMSUpgrade(PackageParser.Package pkg, PackageParser.Package oldPackage, int callingSessionUid, HwGunstallSwitchState hwGSwitchState) {
        if (pkg == null || oldPackage == null || hwGSwitchState == null || callingSessionUid == -1 || !hwGSwitchState.getGunstallShowSwitch()) {
            return false;
        }
        String updatingPackageName = pkg.packageName;
        if (TextUtils.isEmpty(updatingPackageName)) {
            Slog.w(TAG, "forbidGMSUpgrade updatingPackageName is empty");
            return false;
        }
        String[] callerUidPacakge = this.mIPmsInner.getPackagesForUid(callingSessionUid);
        if (!(callerUidPacakge == null || callerUidPacakge[0] == null)) {
            Slog.i(TAG, "forbidGMSUpgrade: callerUidPacakge=" + callerUidPacakge[0] + ", updatingPackageName=" + updatingPackageName);
        }
        if (updatingPackageName.equals("com.google.android.gms") && callerUidPacakge != null && callerUidPacakge[0] != null && (callerUidPacakge[0].equals(PLAY_STORE_NAME) || ArrayUtils.contains(callerUidPacakge, PLAY_STORE_NAME))) {
            int oldVersionCode = oldPackage.mVersionCode;
            int updatingVersionCode = pkg.mVersionCode;
            Slog.i(TAG, "forbidGMSUpgrade: updatingVersionCode=" + updatingVersionCode + ",oldVersionCode=" + oldVersionCode);
            if (hwGSwitchState.getGunstallUserState() == 0) {
                if (!hwGSwitchState.getGunstallUpdateState() || updatingVersionCode < hwGSwitchState.getGunstallAppVersion()) {
                    Slog.i(TAG, "cloud set allow, cloud update state: " + hwGSwitchState.getGunstallUpdateState() + ", cloud app version: " + hwGSwitchState.getGunstallAppVersion());
                } else {
                    Slog.i(TAG, "cloud set forbid");
                    return true;
                }
            } else if (hwGSwitchState.getGunstallUserState() == 2) {
                Slog.i(TAG, "user set forbid");
                return true;
            } else {
                Slog.i(TAG, "user set allow");
            }
        }
        return false;
    }

    private boolean isEnableGupdateSwitch() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), "gunstall_show_switch", 0) != 0;
    }

    private int getGunstallUserState() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), "gunstall_user_state", 0);
    }

    private boolean getGunstallCloudUpdateState() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), "gunstall_update_state", 0) != 0;
    }

    private int getGunstallCloudAppVersion() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), "gunstall_app_version", Integer.MAX_VALUE);
    }

    public HwGunstallSwitchState updateGunstallState() {
        HwGunstallSwitchState hwGunstallSwitchState = HwGunstallSwitchState.getInstance();
        if (hwGunstallSwitchState != null) {
            boolean showSwitch = isEnableGupdateSwitch();
            hwGunstallSwitchState.setGunstallShowSwitch(showSwitch);
            if (showSwitch) {
                hwGunstallSwitchState.setGunstallUserState(getGunstallUserState());
                hwGunstallSwitchState.setGunstallUpdateState(getGunstallCloudUpdateState());
                hwGunstallSwitchState.setGunstallAppVersion(getGunstallCloudAppVersion());
            }
        }
        return hwGunstallSwitchState;
    }
}
