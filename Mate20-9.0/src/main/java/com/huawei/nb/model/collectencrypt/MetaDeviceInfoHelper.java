package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class MetaDeviceInfoHelper extends AEntityHelper<MetaDeviceInfo> {
    private static final MetaDeviceInfoHelper INSTANCE = new MetaDeviceInfoHelper();

    private MetaDeviceInfoHelper() {
    }

    public static MetaDeviceInfoHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, MetaDeviceInfo object) {
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
        String mDeviceName = object.getMDeviceName();
        if (mDeviceName != null) {
            statement.bindString(3, mDeviceName);
        } else {
            statement.bindNull(3);
        }
        String mHardwareVer = object.getMHardwareVer();
        if (mHardwareVer != null) {
            statement.bindString(4, mHardwareVer);
        } else {
            statement.bindNull(4);
        }
        String mSoftwareVer = object.getMSoftwareVer();
        if (mSoftwareVer != null) {
            statement.bindString(5, mSoftwareVer);
        } else {
            statement.bindNull(5);
        }
        String mIMEI1 = object.getMIMEI1();
        if (mIMEI1 != null) {
            statement.bindString(6, mIMEI1);
        } else {
            statement.bindNull(6);
        }
        String mIMEI2 = object.getMIMEI2();
        if (mIMEI2 != null) {
            statement.bindString(7, mIMEI2);
        } else {
            statement.bindNull(7);
        }
        String mIMSI1 = object.getMIMSI1();
        if (mIMSI1 != null) {
            statement.bindString(8, mIMSI1);
        } else {
            statement.bindNull(8);
        }
        String mIMSI2 = object.getMIMSI2();
        if (mIMSI2 != null) {
            statement.bindString(9, mIMSI2);
        } else {
            statement.bindNull(9);
        }
        String mSN = object.getMSN();
        if (mSN != null) {
            statement.bindString(10, mSN);
        } else {
            statement.bindNull(10);
        }
        String mLanguageRegion = object.getMLanguageRegion();
        if (mLanguageRegion != null) {
            statement.bindString(11, mLanguageRegion);
        } else {
            statement.bindNull(11);
        }
        Integer mReservedInt = object.getMReservedInt();
        if (mReservedInt != null) {
            statement.bindLong(12, (long) mReservedInt.intValue());
        } else {
            statement.bindNull(12);
        }
        String mReservedText = object.getMReservedText();
        if (mReservedText != null) {
            statement.bindString(13, mReservedText);
        } else {
            statement.bindNull(13);
        }
    }

    public MetaDeviceInfo readObject(Cursor cursor, int offset) {
        return new MetaDeviceInfo(cursor);
    }

    public void setPrimaryKeyValue(MetaDeviceInfo object, long value) {
        object.setMId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, MetaDeviceInfo object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
