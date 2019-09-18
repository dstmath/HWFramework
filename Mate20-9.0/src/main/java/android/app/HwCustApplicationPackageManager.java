package android.app;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import java.util.List;

public class HwCustApplicationPackageManager {
    private static final String TAG = "HwCustApplicationPackageManager";

    public void removeMmsResolveInfo(List<ResolveInfo> list, Intent intent, Context context) {
    }

    public boolean isSdInstallEnabled() {
        return false;
    }

    public VolumeInfo getSDVolume(StorageManager storage) {
        return null;
    }

    public boolean isSdVol(VolumeInfo vol) {
        return false;
    }

    public boolean isSdMoveabled(ApplicationInfo app) {
        return false;
    }
}
