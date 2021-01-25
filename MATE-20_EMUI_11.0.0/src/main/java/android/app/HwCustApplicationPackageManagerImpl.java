package android.app;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.SystemProperties;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.provider.Settings;
import android.util.Log;
import java.util.List;

public class HwCustApplicationPackageManagerImpl extends HwCustApplicationPackageManager {
    private static final int SD_VOLUME_ID_LENGTH = 3;
    private static final String TAG = "HwCustApplicationPackageManagerImpl";
    private boolean mSdInstallEnabled = SystemProperties.getBoolean("ro.config.hw_sdInstall_enable", false);

    public void removeMmsResolveInfo(List<ResolveInfo> resolveInfo, Intent intent, Context context) {
        if (resolveInfo != null && intent != null && context != null) {
            if (("android.intent.action.SEND".equals(intent.getAction()) || "android.intent.action.SEND_MULTIPLE".equals(intent.getAction())) && "true".equals(Settings.System.getString(context.getContentResolver(), "hw_share_app_no_mms"))) {
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
        if (!isSdInstallEnabled() || storage == null) {
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
        String currentDiskID = vol.getDisk().getId();
        String currentVolumeID = vol.getId();
        String[] currentDiskIDSplitstr = currentDiskID.split(":");
        String[] currentVolumeIDSplitstr = currentVolumeID.split(":");
        if (currentDiskIDSplitstr.length != SD_VOLUME_ID_LENGTH || currentVolumeIDSplitstr.length != SD_VOLUME_ID_LENGTH) {
            return false;
        }
        try {
            int diskID = Integer.valueOf(currentDiskIDSplitstr[2]).intValue();
            int volumeID = Integer.valueOf(currentVolumeIDSplitstr[2]).intValue();
            if (volumeID == diskID + 1 || volumeID == diskID) {
                return true;
            }
            return false;
        } catch (NumberFormatException e) {
            Log.e(TAG, "isFirstSdVolume number format failed");
            return false;
        }
    }

    public boolean isSdVol(VolumeInfo vol) {
        if (isSdInstallEnabled() && vol != null && vol.getDisk() != null && vol.getDisk().isSd() && isFirstSdVolume(vol)) {
            return true;
        }
        return false;
    }
}
