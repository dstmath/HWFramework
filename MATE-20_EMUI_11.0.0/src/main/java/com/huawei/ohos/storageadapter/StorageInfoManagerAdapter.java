package com.huawei.ohos.storageadapter;

import android.app.usage.StorageStats;
import android.app.usage.StorageStatsManager;
import android.content.pm.PackageManager;
import android.os.UserHandle;
import java.io.IOException;
import java.util.UUID;
import ohos.app.Context;
import ohos.global.resource.NotExistException;
import ohos.utils.Parcel;

public class StorageInfoManagerAdapter {
    private final StorageStatsManager mStatsManager;

    private StorageInfoManagerAdapter(StorageStatsManager storageStatsManager) {
        this.mStatsManager = storageStatsManager;
    }

    public static StorageInfoManagerAdapter newInstance(Context context) {
        if (context == null) {
            return null;
        }
        Object hostContext = context.getHostContext();
        if (!(hostContext instanceof android.content.Context)) {
            return null;
        }
        Object systemService = ((android.content.Context) hostContext).getSystemService("storagestats");
        if (!(systemService instanceof StorageStatsManager)) {
            return null;
        }
        return new StorageInfoManagerAdapter((StorageStatsManager) systemService);
    }

    public long getFreeSize(UUID uuid) throws IOException {
        return this.mStatsManager.getFreeBytes(uuid);
    }

    public Parcel queryInfoByPackageName(UUID uuid, String str, int i) throws IOException, NotExistException {
        boolean writeLong;
        try {
            StorageStats queryStatsForPackage = this.mStatsManager.queryStatsForPackage(uuid, str, UserHandle.getUserHandleForUid(i));
            Parcel create = Parcel.create();
            if (create.writeLong(queryStatsForPackage.getCacheBytes()) && (create.writeLong(queryStatsForPackage.getAppBytes()) & true & create.writeLong(queryStatsForPackage.getDataBytes()))) {
                return create;
            }
            throw new IOException();
        } catch (PackageManager.NameNotFoundException unused) {
            throw new NotExistException();
        }
    }

    public Parcel queryInfoByUid(UUID uuid, int i) throws IOException {
        boolean writeLong;
        StorageStats queryStatsForUid = this.mStatsManager.queryStatsForUid(uuid, i);
        Parcel create = Parcel.create();
        if (create.writeLong(queryStatsForUid.getCacheBytes()) && ((create.writeLong(queryStatsForUid.getAppBytes()) & true) & create.writeLong(queryStatsForUid.getDataBytes()))) {
            return create;
        }
        throw new IOException();
    }
}
