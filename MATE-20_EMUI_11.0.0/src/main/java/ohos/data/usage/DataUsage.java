package ohos.data.usage;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.RemoteException;
import ohos.utils.net.Uri;

public final class DataUsage {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218109441, "DataUsage");
    public static final UUID UUID_DATA_USAGE = UUID.fromString("41217664-9172-527a-b3d5-edabb50a7d69");

    public static boolean isSupported() {
        return true;
    }

    private DataUsage() {
    }

    public static MountState getDiskMountedStatus() {
        try {
            return DataUsageProxy.getInstance().getDiskMountedStatus();
        } catch (RemoteException unused) {
            return MountState.DISK_UNKNOWN;
        }
    }

    public static MountState getDiskMountedStatus(File file) {
        try {
            return DataUsageProxy.getInstance().getDiskMountedStatus(file);
        } catch (RemoteException unused) {
            return MountState.DISK_UNKNOWN;
        }
    }

    public static boolean isDiskPluggable() {
        try {
            return DataUsageProxy.getInstance().isDiskPluggable();
        } catch (RemoteException unused) {
            return false;
        }
    }

    public static boolean isDiskPluggable(File file) {
        try {
            return DataUsageProxy.getInstance().isDiskPluggable(file);
        } catch (RemoteException unused) {
            return false;
        }
    }

    public static Optional<List<Volume>> getVolumes() {
        try {
            return DataUsageProxy.getInstance().getVolumes();
        } catch (RemoteException unused) {
            return Optional.empty();
        }
    }

    public static Optional<Volume> getPrimaryVolume() {
        try {
            return DataUsageProxy.getInstance().getPrimaryVolume();
        } catch (RemoteException unused) {
            return Optional.empty();
        }
    }

    public static Optional<Volume> getVolume(File file) {
        try {
            return DataUsageProxy.getInstance().getVolume(file);
        } catch (RemoteException unused) {
            return Optional.empty();
        }
    }

    public static Optional<Volume> getVolume(Context context, Uri uri) {
        try {
            return DataUsageProxy.getInstance().getVolume(context, uri);
        } catch (RemoteException unused) {
            return Optional.empty();
        }
    }

    public static boolean isDiskEmulated() {
        try {
            return DataUsageProxy.getInstance().isDiskEmulated();
        } catch (RemoteException unused) {
            return false;
        }
    }

    public static boolean isDiskEmulated(File file) {
        try {
            return DataUsageProxy.getInstance().isDiskEmulated(file);
        } catch (RemoteException unused) {
            return false;
        }
    }

    public static boolean registerCallback(IDataUsageCallback iDataUsageCallback) {
        try {
            return DataUsageProxy.getInstance().registerCallback(iDataUsageCallback);
        } catch (RemoteException unused) {
            return false;
        }
    }

    public static boolean unRegisterCallback(IDataUsageCallback iDataUsageCallback) {
        try {
            return DataUsageProxy.getInstance().unRegisterCallback(iDataUsageCallback);
        } catch (RemoteException unused) {
            return false;
        }
    }

    public static void installVolume(String str) {
        try {
            DataUsageProxy.getInstance().installVolume(str);
        } catch (RemoteException unused) {
            HiLog.error(LABEL, "installVolume fail", new Object[0]);
        }
    }

    public static void uninstallVolume(String str) {
        try {
            DataUsageProxy.getInstance().uninstallVolume(str);
        } catch (RemoteException unused) {
            HiLog.error(LABEL, "uninstallVolume fail", new Object[0]);
        }
    }

    public static void formatVolume(String str) {
        try {
            DataUsageProxy.getInstance().formatVolume(str);
        } catch (RemoteException unused) {
            HiLog.error(LABEL, "formatVolume fail", new Object[0]);
        }
    }

    public static void partitionPublicDisk(String str) {
        try {
            DataUsageProxy.getInstance().partitionPublicDisk(str);
        } catch (RemoteException unused) {
            HiLog.error(LABEL, "partitionPublicDisk fail", new Object[0]);
        }
    }

    public static void partitionPrivateDisk(String str) {
        try {
            DataUsageProxy.getInstance().partitionPrivateDisk(str);
        } catch (RemoteException unused) {
            HiLog.error(LABEL, "partitionPrivateDisk fail", new Object[0]);
        }
    }

    public static void partitionMixedDisk(String str, int i) {
        try {
            DataUsageProxy.getInstance().partitionMixedDisk(str, i);
        } catch (RemoteException unused) {
            HiLog.error(LABEL, "partitionMixedDisk fail", new Object[0]);
        }
    }

    public static Optional<List<VolumeView>> getVolumeViews() {
        try {
            return DataUsageProxy.getInstance().getVolumeViews();
        } catch (RemoteException unused) {
            HiLog.error(LABEL, "getVolumeViews fail", new Object[0]);
            return Optional.empty();
        }
    }
}
