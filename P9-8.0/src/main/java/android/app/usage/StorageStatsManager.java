package android.app.usage;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.ParcelableException;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.storage.StorageManager;
import com.android.internal.util.Preconditions;
import java.io.IOException;
import java.util.UUID;

public class StorageStatsManager {
    private final Context mContext;
    private final IStorageStatsManager mService;

    public StorageStatsManager(Context context, IStorageStatsManager service) {
        this.mContext = (Context) Preconditions.checkNotNull(context);
        this.mService = (IStorageStatsManager) Preconditions.checkNotNull(service);
    }

    public boolean isQuotaSupported(UUID storageUuid) {
        try {
            return this.mService.isQuotaSupported(StorageManager.convert(storageUuid), this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public boolean isQuotaSupported(String uuid) {
        return isQuotaSupported(StorageManager.convert(uuid));
    }

    public long getTotalBytes(UUID storageUuid) throws IOException {
        try {
            return this.mService.getTotalBytes(StorageManager.convert(storageUuid), this.mContext.getOpPackageName());
        } catch (ParcelableException e) {
            e.maybeRethrow(IOException.class);
            throw new RuntimeException(e);
        } catch (RemoteException e2) {
            throw e2.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public long getTotalBytes(String uuid) throws IOException {
        return getTotalBytes(StorageManager.convert(uuid));
    }

    public long getFreeBytes(UUID storageUuid) throws IOException {
        try {
            return this.mService.getFreeBytes(StorageManager.convert(storageUuid), this.mContext.getOpPackageName());
        } catch (ParcelableException e) {
            e.maybeRethrow(IOException.class);
            throw new RuntimeException(e);
        } catch (RemoteException e2) {
            throw e2.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public long getFreeBytes(String uuid) throws IOException {
        return getFreeBytes(StorageManager.convert(uuid));
    }

    public long getCacheBytes(UUID storageUuid) throws IOException {
        try {
            return this.mService.getCacheBytes(StorageManager.convert(storageUuid), this.mContext.getOpPackageName());
        } catch (ParcelableException e) {
            e.maybeRethrow(IOException.class);
            throw new RuntimeException(e);
        } catch (RemoteException e2) {
            throw e2.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public long getCacheBytes(String uuid) throws IOException {
        return getCacheBytes(StorageManager.convert(uuid));
    }

    public StorageStats queryStatsForPackage(UUID storageUuid, String packageName, UserHandle user) throws NameNotFoundException, IOException {
        try {
            return this.mService.queryStatsForPackage(StorageManager.convert(storageUuid), packageName, user.getIdentifier(), this.mContext.getOpPackageName());
        } catch (ParcelableException e) {
            e.maybeRethrow(NameNotFoundException.class);
            e.maybeRethrow(IOException.class);
            throw new RuntimeException(e);
        } catch (RemoteException e2) {
            throw e2.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public StorageStats queryStatsForPackage(String uuid, String packageName, UserHandle user) throws NameNotFoundException, IOException {
        return queryStatsForPackage(StorageManager.convert(uuid), packageName, user);
    }

    public StorageStats queryStatsForUid(UUID storageUuid, int uid) throws IOException {
        try {
            return this.mService.queryStatsForUid(StorageManager.convert(storageUuid), uid, this.mContext.getOpPackageName());
        } catch (ParcelableException e) {
            e.maybeRethrow(IOException.class);
            throw new RuntimeException(e);
        } catch (RemoteException e2) {
            throw e2.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public StorageStats queryStatsForUid(String uuid, int uid) throws IOException {
        return queryStatsForUid(StorageManager.convert(uuid), uid);
    }

    public StorageStats queryStatsForUser(UUID storageUuid, UserHandle user) throws IOException {
        try {
            return this.mService.queryStatsForUser(StorageManager.convert(storageUuid), user.getIdentifier(), this.mContext.getOpPackageName());
        } catch (ParcelableException e) {
            e.maybeRethrow(IOException.class);
            throw new RuntimeException(e);
        } catch (RemoteException e2) {
            throw e2.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public StorageStats queryStatsForUser(String uuid, UserHandle user) throws IOException {
        return queryStatsForUser(StorageManager.convert(uuid), user);
    }

    public ExternalStorageStats queryExternalStatsForUser(UUID storageUuid, UserHandle user) throws IOException {
        try {
            return this.mService.queryExternalStatsForUser(StorageManager.convert(storageUuid), user.getIdentifier(), this.mContext.getOpPackageName());
        } catch (ParcelableException e) {
            e.maybeRethrow(IOException.class);
            throw new RuntimeException(e);
        } catch (RemoteException e2) {
            throw e2.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public ExternalStorageStats queryExternalStatsForUser(String uuid, UserHandle user) throws IOException {
        return queryExternalStatsForUser(StorageManager.convert(uuid), user);
    }

    public long getCacheQuotaBytes(String volumeUuid, int uid) {
        try {
            return this.mService.getCacheQuotaBytes(volumeUuid, uid, this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }
}
