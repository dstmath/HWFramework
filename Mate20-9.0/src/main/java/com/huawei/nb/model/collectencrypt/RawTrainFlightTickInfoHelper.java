package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RawTrainFlightTickInfoHelper extends AEntityHelper<RawTrainFlightTickInfo> {
    private static final RawTrainFlightTickInfoHelper INSTANCE = new RawTrainFlightTickInfoHelper();

    private RawTrainFlightTickInfoHelper() {
    }

    public static RawTrainFlightTickInfoHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, RawTrainFlightTickInfo object) {
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
        String mPassengerName = object.getMPassengerName();
        if (mPassengerName != null) {
            statement.bindString(3, mPassengerName);
        } else {
            statement.bindNull(3);
        }
        String mTrainFlightNo = object.getMTrainFlightNo();
        if (mTrainFlightNo != null) {
            statement.bindString(4, mTrainFlightNo);
        } else {
            statement.bindNull(4);
        }
        String mSeatNo = object.getMSeatNo();
        if (mSeatNo != null) {
            statement.bindString(5, mSeatNo);
        } else {
            statement.bindNull(5);
        }
        Date mTrainFlightStartTime = object.getMTrainFlightStartTime();
        if (mTrainFlightStartTime != null) {
            statement.bindLong(6, mTrainFlightStartTime.getTime());
        } else {
            statement.bindNull(6);
        }
        Date mTrainFlightArrivalTime = object.getMTrainFlightArrivalTime();
        if (mTrainFlightArrivalTime != null) {
            statement.bindLong(7, mTrainFlightArrivalTime.getTime());
        } else {
            statement.bindNull(7);
        }
        String mTrainFlightStartPlace = object.getMTrainFlightStartPlace();
        if (mTrainFlightStartPlace != null) {
            statement.bindString(8, mTrainFlightStartPlace);
        } else {
            statement.bindNull(8);
        }
        String mTrainFlightArrivalPlace = object.getMTrainFlightArrivalPlace();
        if (mTrainFlightArrivalPlace != null) {
            statement.bindString(9, mTrainFlightArrivalPlace);
        } else {
            statement.bindNull(9);
        }
        Integer mReservedInt = object.getMReservedInt();
        if (mReservedInt != null) {
            statement.bindLong(10, (long) mReservedInt.intValue());
        } else {
            statement.bindNull(10);
        }
        String mReservedText = object.getMReservedText();
        if (mReservedText != null) {
            statement.bindString(11, mReservedText);
        } else {
            statement.bindNull(11);
        }
    }

    public RawTrainFlightTickInfo readObject(Cursor cursor, int offset) {
        return new RawTrainFlightTickInfo(cursor);
    }

    public void setPrimaryKeyValue(RawTrainFlightTickInfo object, long value) {
        object.setMId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, RawTrainFlightTickInfo object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
