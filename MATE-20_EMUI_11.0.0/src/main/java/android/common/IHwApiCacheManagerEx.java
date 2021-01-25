package android.common;

import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.RemoteException;
import android.os.storage.IStorageManager;
import android.os.storage.StorageVolume;

public interface IHwApiCacheManagerEx {
    void apiPreCache(PackageManager packageManager);

    void disableCache();

    ApplicationInfo getApplicationInfoAsUser(IPackageManager iPackageManager, String str, int i, int i2) throws RemoteException;

    PackageInfo getPackageInfoAsUser(IPackageManager iPackageManager, String str, int i, int i2) throws RemoteException;

    int getPackageUidAsUser(IPackageManager iPackageManager, String str, int i, int i2) throws RemoteException;

    StorageVolume[] getVolumeList(IStorageManager iStorageManager, String str, int i, int i2) throws RemoteException;

    void notifyVolumeStateChanged(int i, int i2);
}
