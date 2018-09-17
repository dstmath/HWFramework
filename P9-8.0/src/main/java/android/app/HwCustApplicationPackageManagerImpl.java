package android.app;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.os.SystemProperties;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.provider.Settings.System;
import android.util.Log;
import java.io.File;
import java.util.List;

public class HwCustApplicationPackageManagerImpl extends HwCustApplicationPackageManager {
    private static final String TAG = "HwCustApplicationPackageManagerImpl";
    private boolean mSdInstallEnabled = SystemProperties.getBoolean("ro.config.hw_sdInstall_enable", false);

    public void removeMmsResolveInfo(List<ResolveInfo> resolveInfo, Intent intent, Context context) {
        if ("android.intent.action.SEND".equals(intent.getAction()) || "android.intent.action.SEND_MULTIPLE".equals(intent.getAction())) {
            if ("true".equals(System.getString(context.getContentResolver(), "hw_share_app_no_mms"))) {
                removeMMS(resolveInfo);
            }
        }
    }

    private void removeMMS(List<ResolveInfo> resolveInfos) {
        for (ResolveInfo info : resolveInfos) {
            if ("com.android.mms.ui.ComposeMessageActivity".compareTo(info.activityInfo.name) == 0) {
                resolveInfos.remove(info);
                return;
            }
        }
    }

    public boolean isSdInstallEnabled() {
        return this.mSdInstallEnabled;
    }

    public VolumeInfo getSDVolume(StorageManager storage) {
        if (!isSdInstallEnabled()) {
            return null;
        }
        for (VolumeInfo vol : storage.getVolumes()) {
            if (vol.getDisk() != null && vol.isMountedWritable() && vol.getDisk().isSd() && isFirstSdVolume(vol)) {
                return vol;
            }
        }
        return null;
    }

    private boolean isFirstSdVolume(VolumeInfo vol) {
        String CurrentDiskID = vol.getDisk().getId();
        String CurrentVolumeID = vol.getId();
        String[] CurrentDiskIDSplitstr = CurrentDiskID.split(":");
        String[] CurrentVolumeIDSplitstr = CurrentVolumeID.split(":");
        if (CurrentDiskIDSplitstr.length != 3 || CurrentVolumeIDSplitstr.length != 3) {
            return false;
        }
        try {
            int DiskID = Integer.valueOf(CurrentDiskIDSplitstr[2]).intValue();
            int VolumeID = Integer.valueOf(CurrentVolumeIDSplitstr[2]).intValue();
            if (VolumeID == DiskID + 1 || VolumeID == DiskID) {
                return true;
            }
            return false;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isSdVol(VolumeInfo vol) {
        if (isSdInstallEnabled() && vol.getDisk() != null && vol.getDisk().isSd() && isFirstSdVolume(vol)) {
            return true;
        }
        return false;
    }

    public boolean isSdMoveabled(ApplicationInfo app) {
        if (!isSdInstallEnabled() || app == null) {
            return false;
        }
        if (!app.isExternalAsec()) {
            File probe = new File(app.getCodePath());
            File probeOat = new File(probe, "oat");
            if (!(probe.isDirectory() && (probeOat.isDirectory() ^ 1) == 0)) {
                Log.d(TAG, "app.getCodePath " + app.getCodePath() + "isSdMoveabled =" + probeOat.isDirectory());
                return false;
            }
        }
        return true;
    }
}
