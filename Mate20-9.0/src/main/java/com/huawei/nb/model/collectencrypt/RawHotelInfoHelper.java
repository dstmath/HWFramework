package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RawHotelInfoHelper extends AEntityHelper<RawHotelInfo> {
    private static final RawHotelInfoHelper INSTANCE = new RawHotelInfoHelper();

    private RawHotelInfoHelper() {
    }

    public static RawHotelInfoHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, RawHotelInfo object) {
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
        String mHotelTelNo = object.getMHotelTelNo();
        if (mHotelTelNo != null) {
            statement.bindString(3, mHotelTelNo);
        } else {
            statement.bindNull(3);
        }
        String mHotelAddr = object.getMHotelAddr();
        if (mHotelAddr != null) {
            statement.bindString(4, mHotelAddr);
        } else {
            statement.bindNull(4);
        }
        String mHotelName = object.getMHotelName();
        if (mHotelName != null) {
            statement.bindString(5, mHotelName);
        } else {
            statement.bindNull(5);
        }
        Date mCheckinTime = object.getMCheckinTime();
        if (mCheckinTime != null) {
            statement.bindLong(6, mCheckinTime.getTime());
        } else {
            statement.bindNull(6);
        }
        Integer mReservedInt = object.getMReservedInt();
        if (mReservedInt != null) {
            statement.bindLong(7, (long) mReservedInt.intValue());
        } else {
            statement.bindNull(7);
        }
        String mReservedText = object.getMReservedText();
        if (mReservedText != null) {
            statement.bindString(8, mReservedText);
        } else {
            statement.bindNull(8);
        }
    }

    public RawHotelInfo readObject(Cursor cursor, int offset) {
        return new RawHotelInfo(cursor);
    }

    public void setPrimaryKeyValue(RawHotelInfo object, long value) {
        object.setMId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, RawHotelInfo object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
