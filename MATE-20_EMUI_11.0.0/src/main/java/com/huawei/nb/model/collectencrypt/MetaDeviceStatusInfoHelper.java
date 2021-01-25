package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class MetaDeviceStatusInfoHelper extends AEntityHelper<MetaDeviceStatusInfo> {
    private static final MetaDeviceStatusInfoHelper INSTANCE = new MetaDeviceStatusInfoHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, MetaDeviceStatusInfo metaDeviceStatusInfo) {
        return null;
    }

    private MetaDeviceStatusInfoHelper() {
    }

    public static MetaDeviceStatusInfoHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, MetaDeviceStatusInfo metaDeviceStatusInfo) {
        Integer mId = metaDeviceStatusInfo.getMId();
        if (mId != null) {
            statement.bindLong(1, (long) mId.intValue());
        } else {
            statement.bindNull(1);
        }
        Date mTimeStamp = metaDeviceStatusInfo.getMTimeStamp();
        if (mTimeStamp != null) {
            statement.bindLong(2, mTimeStamp.getTime());
        } else {
            statement.bindNull(2);
        }
        String mMemoryInfo = metaDeviceStatusInfo.getMMemoryInfo();
        if (mMemoryInfo != null) {
            statement.bindString(3, mMemoryInfo);
        } else {
            statement.bindNull(3);
        }
        String mCPUInfo = metaDeviceStatusInfo.getMCPUInfo();
        if (mCPUInfo != null) {
            statement.bindString(4, mCPUInfo);
        } else {
            statement.bindNull(4);
        }
        String mBatteryInfo = metaDeviceStatusInfo.getMBatteryInfo();
        if (mBatteryInfo != null) {
            statement.bindString(5, mBatteryInfo);
        } else {
            statement.bindNull(5);
        }
        String mNetStat = metaDeviceStatusInfo.getMNetStat();
        if (mNetStat != null) {
            statement.bindString(6, mNetStat);
        } else {
            statement.bindNull(6);
        }
        Integer mBluetoohStat = metaDeviceStatusInfo.getMBluetoohStat();
        if (mBluetoohStat != null) {
            statement.bindLong(7, (long) mBluetoohStat.intValue());
        } else {
            statement.bindNull(7);
        }
        Integer mReservedInt = metaDeviceStatusInfo.getMReservedInt();
        if (mReservedInt != null) {
            statement.bindLong(8, (long) mReservedInt.intValue());
        } else {
            statement.bindNull(8);
        }
        String mReservedText = metaDeviceStatusInfo.getMReservedText();
        if (mReservedText != null) {
            statement.bindString(9, mReservedText);
        } else {
            statement.bindNull(9);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public MetaDeviceStatusInfo readObject(Cursor cursor, int i) {
        return new MetaDeviceStatusInfo(cursor);
    }

    public void setPrimaryKeyValue(MetaDeviceStatusInfo metaDeviceStatusInfo, long j) {
        metaDeviceStatusInfo.setMId(Integer.valueOf((int) j));
    }
}
