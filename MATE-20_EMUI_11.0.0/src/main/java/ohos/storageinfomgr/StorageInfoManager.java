package ohos.storageinfomgr;

import com.huawei.ohos.storageadapter.StorageInfoManagerAdapter;
import java.io.IOException;
import java.util.UUID;
import ohos.app.Context;
import ohos.global.resource.NotExistException;
import ohos.utils.Parcel;

public final class StorageInfoManager {
    private final StorageInfoManagerAdapter managerAdapter;

    private StorageInfoManager(Object obj) {
        if (obj instanceof StorageInfoManagerAdapter) {
            this.managerAdapter = (StorageInfoManagerAdapter) obj;
        } else {
            this.managerAdapter = null;
        }
    }

    public static StorageInfoManager newInstance(Context context) {
        StorageInfoManagerAdapter newInstance;
        if (context == null || (newInstance = StorageInfoManagerAdapter.newInstance(context)) == null) {
            return null;
        }
        return new StorageInfoManager(newInstance);
    }

    public long getFreeSize(UUID uuid) throws IOException {
        return this.managerAdapter.getFreeSize(uuid);
    }

    public StorageInfo queryInfoByPackageName(UUID uuid, String str, int i) throws IOException, NotExistException {
        Parcel queryInfoByPackageName = this.managerAdapter.queryInfoByPackageName(uuid, str, i);
        StorageInfo createFromParcel = StorageInfo.PRODUCER.createFromParcel(queryInfoByPackageName);
        queryInfoByPackageName.reclaim();
        return createFromParcel;
    }

    public StorageInfo queryInfoByUid(UUID uuid, int i) throws IOException {
        Parcel queryInfoByUid = this.managerAdapter.queryInfoByUid(uuid, i);
        StorageInfo createFromParcel = StorageInfo.PRODUCER.createFromParcel(queryInfoByUid);
        queryInfoByUid.reclaim();
        return createFromParcel;
    }
}
