package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class MetaDeviceInfoHelper extends AEntityHelper<MetaDeviceInfo> {
    private static final MetaDeviceInfoHelper INSTANCE = new MetaDeviceInfoHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, MetaDeviceInfo metaDeviceInfo) {
        return null;
    }

    private MetaDeviceInfoHelper() {
    }

    public static MetaDeviceInfoHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, MetaDeviceInfo metaDeviceInfo) {
        Integer mId = metaDeviceInfo.getMId();
        if (mId != null) {
            statement.bindLong(1, (long) mId.intValue());
        } else {
            statement.bindNull(1);
        }
        Date mTimeStamp = metaDeviceInfo.getMTimeStamp();
        if (mTimeStamp != null) {
            statement.bindLong(2, mTimeStamp.getTime());
        } else {
            statement.bindNull(2);
        }
        String mDeviceName = metaDeviceInfo.getMDeviceName();
        if (mDeviceName != null) {
            statement.bindString(3, mDeviceName);
        } else {
            statement.bindNull(3);
        }
        String mHardwareVer = metaDeviceInfo.getMHardwareVer();
        if (mHardwareVer != null) {
            statement.bindString(4, mHardwareVer);
        } else {
            statement.bindNull(4);
        }
        String mSoftwareVer = metaDeviceInfo.getMSoftwareVer();
        if (mSoftwareVer != null) {
            statement.bindString(5, mSoftwareVer);
        } else {
            statement.bindNull(5);
        }
        String mimei1 = metaDeviceInfo.getMIMEI1();
        if (mimei1 != null) {
            statement.bindString(6, mimei1);
        } else {
            statement.bindNull(6);
        }
        String mimei2 = metaDeviceInfo.getMIMEI2();
        if (mimei2 != null) {
            statement.bindString(7, mimei2);
        } else {
            statement.bindNull(7);
        }
        String mimsi1 = metaDeviceInfo.getMIMSI1();
        if (mimsi1 != null) {
            statement.bindString(8, mimsi1);
        } else {
            statement.bindNull(8);
        }
        String mimsi2 = metaDeviceInfo.getMIMSI2();
        if (mimsi2 != null) {
            statement.bindString(9, mimsi2);
        } else {
            statement.bindNull(9);
        }
        String msn = metaDeviceInfo.getMSN();
        if (msn != null) {
            statement.bindString(10, msn);
        } else {
            statement.bindNull(10);
        }
        String mLanguageRegion = metaDeviceInfo.getMLanguageRegion();
        if (mLanguageRegion != null) {
            statement.bindString(11, mLanguageRegion);
        } else {
            statement.bindNull(11);
        }
        Integer mReservedInt = metaDeviceInfo.getMReservedInt();
        if (mReservedInt != null) {
            statement.bindLong(12, (long) mReservedInt.intValue());
        } else {
            statement.bindNull(12);
        }
        String mReservedText = metaDeviceInfo.getMReservedText();
        if (mReservedText != null) {
            statement.bindString(13, mReservedText);
        } else {
            statement.bindNull(13);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public MetaDeviceInfo readObject(Cursor cursor, int i) {
        return new MetaDeviceInfo(cursor);
    }

    public void setPrimaryKeyValue(MetaDeviceInfo metaDeviceInfo, long j) {
        metaDeviceInfo.setMId(Integer.valueOf((int) j));
    }
}
