package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class MetaDeviceStatusInfoHelper extends AEntityHelper<MetaDeviceStatusInfo> {
    private static final MetaDeviceStatusInfoHelper INSTANCE = new MetaDeviceStatusInfoHelper();

    private MetaDeviceStatusInfoHelper() {
    }

    public static MetaDeviceStatusInfoHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, MetaDeviceStatusInfo object) {
        Integer mId = object.getMId();
        if (mId != null) {
            statement.bindLong(1, (long) mId.intValue());
        } else {
            statement.bindNull(1);
        }
        Date mTimeStamp = object.getMTimeStamp();
        if (mTimeStamp != null) {
            statement.bindLong(2, mTimeStamp.getTime());
        } else {
            statement.bindNull(2);
        }
        String mMemoryInfo = object.getMMemoryInfo();
        if (mMemoryInfo != null) {
            statement.bindString(3, mMemoryInfo);
        } else {
            statement.bindNull(3);
        }
        String mCPUInfo = object.getMCPUInfo();
        if (mCPUInfo != null) {
            statement.bindString(4, mCPUInfo);
        } else {
            statement.bindNull(4);
        }
        String mBatteryInfo = object.getMBatteryInfo();
        if (mBatteryInfo != null) {
            statement.bindString(5, mBatteryInfo);
        } else {
            statement.bindNull(5);
        }
        String mNetStat = object.getMNetStat();
        if (mNetStat != null) {
            statement.bindString(6, mNetStat);
        } else {
            statement.bindNull(6);
        }
        Integer mBluetoohStat = object.getMBluetoohStat();
        if (mBluetoohStat != null) {
            statement.bindLong(7, (long) mBluetoohStat.intValue());
        } else {
            statement.bindNull(7);
        }
        Integer mReservedInt = object.getMReservedInt();
        if (mReservedInt != null) {
            statement.bindLong(8, (long) mReservedInt.intValue());
        } else {
            statement.bindNull(8);
        }
        String mReservedText = object.getMReservedText();
        if (mReservedText != null) {
            statement.bindString(9, mReservedText);
        } else {
            statement.bindNull(9);
        }
    }

    public MetaDeviceStatusInfo readObject(Cursor cursor, int offset) {
        return new MetaDeviceStatusInfo(cursor);
    }

    public void setPrimaryKeyValue(MetaDeviceStatusInfo object, long value) {
        object.setMId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, MetaDeviceStatusInfo object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
