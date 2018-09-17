package com.android.server;

import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.storage.VolumeInfo;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Slog;
import com.android.server.NativeDaemonConnector.Command;
import com.android.server.pm.PackageManagerService;

public class HwCustMountServiceImpl extends HwCustMountService {
    protected static final boolean HWDBG;
    private static final String TAG = "HwCustMSImpl";
    private int asecListResult;
    PackageManagerService mPms;
    private boolean mSdInstallEnabled;

    public HwCustMountServiceImpl() {
        this.mSdInstallEnabled = SystemProperties.getBoolean("ro.config.hw_sdInstall_enable", HWDBG);
        this.mPms = (PackageManagerService) ServiceManager.getService("package");
        this.asecListResult = 111;
    }

    static {
        boolean isLoggable = !Log.HWLog ? Log.HWModuleLog ? Log.isLoggable(TAG, 3) : HWDBG : true;
        HWDBG = isLoggable;
    }

    public boolean isSdInstallEnabled() {
        return this.mSdInstallEnabled;
    }

    private boolean isFirstSdVolume(VolumeInfo vol) {
        String CurrentDiskID = vol.getDisk().getId();
        String CurrentVolumeID = vol.getId();
        String[] CurrentDiskIDSplitstr = CurrentDiskID.split(":");
        String[] CurrentVolumeIDSplitstr = CurrentVolumeID.split(":");
        if (CurrentDiskIDSplitstr.length != 3 || CurrentVolumeIDSplitstr.length != 3) {
            return HWDBG;
        }
        try {
            int DiskID = Integer.valueOf(CurrentDiskIDSplitstr[2]).intValue();
            int VolumeID = Integer.valueOf(CurrentVolumeIDSplitstr[2]).intValue();
            if (VolumeID == DiskID + 1 || VolumeID == DiskID) {
                return true;
            }
            return HWDBG;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return HWDBG;
        }
    }

    public void notifyPmsUpdate(VolumeInfo vol, int newState) {
        if (isSdInstallEnabled() && vol.getDisk() != null && vol.getDisk().isSd()) {
            if (2 == newState && isFirstSdVolume(vol)) {
                if (HWDBG) {
                    Slog.d(TAG, "sdcard mounted newState is " + newState);
                }
                this.mPms.updateExternalMediaStatus(true, HWDBG);
            } else if ((newState == 0 || 5 == newState) && isFirstSdVolume(vol)) {
                if (HWDBG) {
                    Slog.d(TAG, "sdcard unmounted newState is " + newState);
                }
                this.mPms.updateExternalMediaStatus(HWDBG, true);
            }
        }
    }

    public void physicalWarnOnNotMounted(ArrayMap<String, VolumeInfo> Volumes, NativeDaemonConnector Connector) {
        boolean mount = HWDBG;
        if (this.mSdInstallEnabled) {
            for (int i = 0; i < Volumes.size(); i++) {
                VolumeInfo vol = (VolumeInfo) Volumes.valueAt(i);
                if (vol.getDisk() != null && vol.getDisk().isSd() && isFirstSdVolume(vol)) {
                    Slog.i(TAG, "MountService startup PhysicalVolumeState " + vol.getState());
                    mount = 2 != vol.getState() ? 1 == vol.getState() ? true : HWDBG : true;
                }
            }
            if (!mount) {
                String[] list = null;
                try {
                    list = NativeDaemonEvent.filterMessageList(Connector.executeForList("asec", new Object[]{"list"}), this.asecListResult);
                } catch (NativeDaemonConnectorException e) {
                    Slog.e(TAG, "get asec list fail");
                }
                if (list == null || list.length == 0) {
                    Slog.i(TAG, "MountService startup No secure containers on sdcard");
                } else {
                    for (String cid : list) {
                        if (cid != null) {
                            try {
                                Command cmd = new Command("asec", new Object[]{"unmount", list[r7]});
                                cmd.appendArg("force");
                                Connector.execute(cmd);
                            } catch (NativeDaemonConnectorException e2) {
                                Slog.e(TAG, "force unmount asec fail");
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean isSdVol(VolumeInfo vol) {
        if (!isSdInstallEnabled() || vol.getDisk() == null || !vol.getDisk().isSd() || !isFirstSdVolume(vol)) {
            return HWDBG;
        }
        Slog.i(TAG, "sdcard isSdVol true");
        return true;
    }
}
