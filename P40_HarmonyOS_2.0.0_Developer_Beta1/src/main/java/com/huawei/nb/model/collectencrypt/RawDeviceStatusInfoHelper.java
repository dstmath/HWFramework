package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RawDeviceStatusInfoHelper extends AEntityHelper<RawDeviceStatusInfo> {
    private static final RawDeviceStatusInfoHelper INSTANCE = new RawDeviceStatusInfoHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, RawDeviceStatusInfo rawDeviceStatusInfo) {
        return null;
    }

    private RawDeviceStatusInfoHelper() {
    }

    public static RawDeviceStatusInfoHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, RawDeviceStatusInfo rawDeviceStatusInfo) {
        Integer mId = rawDeviceStatusInfo.getMId();
        if (mId != null) {
            statement.bindLong(1, (long) mId.intValue());
        } else {
            statement.bindNull(1);
        }
        Date mTimeStamp = rawDeviceStatusInfo.getMTimeStamp();
        if (mTimeStamp != null) {
            statement.bindLong(2, mTimeStamp.getTime());
        } else {
            statement.bindNull(2);
        }
        String mMemoryInfo = rawDeviceStatusInfo.getMMemoryInfo();
        if (mMemoryInfo != null) {
            statement.bindString(3, mMemoryInfo);
        } else {
            statement.bindNull(3);
        }
        String mCPUInfo = rawDeviceStatusInfo.getMCPUInfo();
        if (mCPUInfo != null) {
            statement.bindString(4, mCPUInfo);
        } else {
            statement.bindNull(4);
        }
        String mBatteryInfo = rawDeviceStatusInfo.getMBatteryInfo();
        if (mBatteryInfo != null) {
            statement.bindString(5, mBatteryInfo);
        } else {
            statement.bindNull(5);
        }
        String mNetStat = rawDeviceStatusInfo.getMNetStat();
        if (mNetStat != null) {
            statement.bindString(6, mNetStat);
        } else {
            statement.bindNull(6);
        }
        Integer mBluetoohStat = rawDeviceStatusInfo.getMBluetoohStat();
        if (mBluetoohStat != null) {
            statement.bindLong(7, (long) mBluetoohStat.intValue());
        } else {
            statement.bindNull(7);
        }
        Integer mReservedInt = rawDeviceStatusInfo.getMReservedInt();
        if (mReservedInt != null) {
            statement.bindLong(8, (long) mReservedInt.intValue());
        } else {
            statement.bindNull(8);
        }
        String mReservedText = rawDeviceStatusInfo.getMReservedText();
        if (mReservedText != null) {
            statement.bindString(9, mReservedText);
        } else {
            statement.bindNull(9);
        }
        String mAppInstalled = rawDeviceStatusInfo.getMAppInstalled();
        if (mAppInstalled != null) {
            statement.bindString(10, mAppInstalled);
        } else {
            statement.bindNull(10);
        }
        String mAppUsageTime = rawDeviceStatusInfo.getMAppUsageTime();
        if (mAppUsageTime != null) {
            statement.bindString(11, mAppUsageTime);
        } else {
            statement.bindNull(11);
        }
        String mWifiDataTotal = rawDeviceStatusInfo.getMWifiDataTotal();
        if (mWifiDataTotal != null) {
            statement.bindString(12, mWifiDataTotal);
        } else {
            statement.bindNull(12);
        }
        String mMobileDataTotal = rawDeviceStatusInfo.getMMobileDataTotal();
        if (mMobileDataTotal != null) {
            statement.bindString(13, mMobileDataTotal);
        } else {
            statement.bindNull(13);
        }
        String mMobileDataSurplus = rawDeviceStatusInfo.getMMobileDataSurplus();
        if (mMobileDataSurplus != null) {
            statement.bindString(14, mMobileDataSurplus);
        } else {
            statement.bindNull(14);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public RawDeviceStatusInfo readObject(Cursor cursor, int i) {
        return new RawDeviceStatusInfo(cursor);
    }

    public void setPrimaryKeyValue(RawDeviceStatusInfo rawDeviceStatusInfo, long j) {
        rawDeviceStatusInfo.setMId(Integer.valueOf((int) j));
    }
}
