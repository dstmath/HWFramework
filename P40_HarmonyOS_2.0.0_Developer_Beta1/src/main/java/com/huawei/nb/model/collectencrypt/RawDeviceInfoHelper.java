package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RawDeviceInfoHelper extends AEntityHelper<RawDeviceInfo> {
    private static final RawDeviceInfoHelper INSTANCE = new RawDeviceInfoHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, RawDeviceInfo rawDeviceInfo) {
        return null;
    }

    private RawDeviceInfoHelper() {
    }

    public static RawDeviceInfoHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, RawDeviceInfo rawDeviceInfo) {
        Integer mId = rawDeviceInfo.getMId();
        if (mId != null) {
            statement.bindLong(1, (long) mId.intValue());
        } else {
            statement.bindNull(1);
        }
        Date mTimeStamp = rawDeviceInfo.getMTimeStamp();
        if (mTimeStamp != null) {
            statement.bindLong(2, mTimeStamp.getTime());
        } else {
            statement.bindNull(2);
        }
        String mDeviceName = rawDeviceInfo.getMDeviceName();
        if (mDeviceName != null) {
            statement.bindString(3, mDeviceName);
        } else {
            statement.bindNull(3);
        }
        String mHardwareVer = rawDeviceInfo.getMHardwareVer();
        if (mHardwareVer != null) {
            statement.bindString(4, mHardwareVer);
        } else {
            statement.bindNull(4);
        }
        String mSoftwareVer = rawDeviceInfo.getMSoftwareVer();
        if (mSoftwareVer != null) {
            statement.bindString(5, mSoftwareVer);
        } else {
            statement.bindNull(5);
        }
        String mimei1 = rawDeviceInfo.getMIMEI1();
        if (mimei1 != null) {
            statement.bindString(6, mimei1);
        } else {
            statement.bindNull(6);
        }
        String mimei2 = rawDeviceInfo.getMIMEI2();
        if (mimei2 != null) {
            statement.bindString(7, mimei2);
        } else {
            statement.bindNull(7);
        }
        String mimsi1 = rawDeviceInfo.getMIMSI1();
        if (mimsi1 != null) {
            statement.bindString(8, mimsi1);
        } else {
            statement.bindNull(8);
        }
        String mimsi2 = rawDeviceInfo.getMIMSI2();
        if (mimsi2 != null) {
            statement.bindString(9, mimsi2);
        } else {
            statement.bindNull(9);
        }
        String msn = rawDeviceInfo.getMSN();
        if (msn != null) {
            statement.bindString(10, msn);
        } else {
            statement.bindNull(10);
        }
        String mLanguageRegion = rawDeviceInfo.getMLanguageRegion();
        if (mLanguageRegion != null) {
            statement.bindString(11, mLanguageRegion);
        } else {
            statement.bindNull(11);
        }
        Integer mReservedInt = rawDeviceInfo.getMReservedInt();
        if (mReservedInt != null) {
            statement.bindLong(12, (long) mReservedInt.intValue());
        } else {
            statement.bindNull(12);
        }
        String mReservedText = rawDeviceInfo.getMReservedText();
        if (mReservedText != null) {
            statement.bindString(13, mReservedText);
        } else {
            statement.bindNull(13);
        }
        String mPhoneNum = rawDeviceInfo.getMPhoneNum();
        if (mPhoneNum != null) {
            statement.bindString(14, mPhoneNum);
        } else {
            statement.bindNull(14);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public RawDeviceInfo readObject(Cursor cursor, int i) {
        return new RawDeviceInfo(cursor);
    }

    public void setPrimaryKeyValue(RawDeviceInfo rawDeviceInfo, long j) {
        rawDeviceInfo.setMId(Integer.valueOf((int) j));
    }
}
