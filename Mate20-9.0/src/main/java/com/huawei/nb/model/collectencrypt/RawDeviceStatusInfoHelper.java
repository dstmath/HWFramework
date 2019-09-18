package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RawDeviceStatusInfoHelper extends AEntityHelper<RawDeviceStatusInfo> {
    private static final RawDeviceStatusInfoHelper INSTANCE = new RawDeviceStatusInfoHelper();

    private RawDeviceStatusInfoHelper() {
    }

    public static RawDeviceStatusInfoHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, RawDeviceStatusInfo object) {
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
        String mAppInstalled = object.getMAppInstalled();
        if (mAppInstalled != null) {
            statement.bindString(10, mAppInstalled);
        } else {
            statement.bindNull(10);
        }
        String mAppUsageTime = object.getMAppUsageTime();
        if (mAppUsageTime != null) {
            statement.bindString(11, mAppUsageTime);
        } else {
            statement.bindNull(11);
        }
        String mWifiDataTotal = object.getMWifiDataTotal();
        if (mWifiDataTotal != null) {
            statement.bindString(12, mWifiDataTotal);
        } else {
            statement.bindNull(12);
        }
        String mMobileDataTotal = object.getMMobileDataTotal();
        if (mMobileDataTotal != null) {
            statement.bindString(13, mMobileDataTotal);
        } else {
            statement.bindNull(13);
        }
        String mMobileDataSurplus = object.getMMobileDataSurplus();
        if (mMobileDataSurplus != null) {
            statement.bindString(14, mMobileDataSurplus);
        } else {
            statement.bindNull(14);
        }
    }

    public RawDeviceStatusInfo readObject(Cursor cursor, int offset) {
        return new RawDeviceStatusInfo(cursor);
    }

    public void setPrimaryKeyValue(RawDeviceStatusInfo object, long value) {
        object.setMId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, RawDeviceStatusInfo object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
