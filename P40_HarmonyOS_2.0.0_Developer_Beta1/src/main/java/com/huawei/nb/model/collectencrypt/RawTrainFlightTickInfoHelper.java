package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RawTrainFlightTickInfoHelper extends AEntityHelper<RawTrainFlightTickInfo> {
    private static final RawTrainFlightTickInfoHelper INSTANCE = new RawTrainFlightTickInfoHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, RawTrainFlightTickInfo rawTrainFlightTickInfo) {
        return null;
    }

    private RawTrainFlightTickInfoHelper() {
    }

    public static RawTrainFlightTickInfoHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, RawTrainFlightTickInfo rawTrainFlightTickInfo) {
        Integer mId = rawTrainFlightTickInfo.getMId();
        if (mId != null) {
            statement.bindLong(1, (long) mId.intValue());
        } else {
            statement.bindNull(1);
        }
        Date mTimeStamp = rawTrainFlightTickInfo.getMTimeStamp();
        if (mTimeStamp != null) {
            statement.bindLong(2, mTimeStamp.getTime());
        } else {
            statement.bindNull(2);
        }
        String mPassengerName = rawTrainFlightTickInfo.getMPassengerName();
        if (mPassengerName != null) {
            statement.bindString(3, mPassengerName);
        } else {
            statement.bindNull(3);
        }
        String mTrainFlightNo = rawTrainFlightTickInfo.getMTrainFlightNo();
        if (mTrainFlightNo != null) {
            statement.bindString(4, mTrainFlightNo);
        } else {
            statement.bindNull(4);
        }
        String mSeatNo = rawTrainFlightTickInfo.getMSeatNo();
        if (mSeatNo != null) {
            statement.bindString(5, mSeatNo);
        } else {
            statement.bindNull(5);
        }
        Date mTrainFlightStartTime = rawTrainFlightTickInfo.getMTrainFlightStartTime();
        if (mTrainFlightStartTime != null) {
            statement.bindLong(6, mTrainFlightStartTime.getTime());
        } else {
            statement.bindNull(6);
        }
        Date mTrainFlightArrivalTime = rawTrainFlightTickInfo.getMTrainFlightArrivalTime();
        if (mTrainFlightArrivalTime != null) {
            statement.bindLong(7, mTrainFlightArrivalTime.getTime());
        } else {
            statement.bindNull(7);
        }
        String mTrainFlightStartPlace = rawTrainFlightTickInfo.getMTrainFlightStartPlace();
        if (mTrainFlightStartPlace != null) {
            statement.bindString(8, mTrainFlightStartPlace);
        } else {
            statement.bindNull(8);
        }
        String mTrainFlightArrivalPlace = rawTrainFlightTickInfo.getMTrainFlightArrivalPlace();
        if (mTrainFlightArrivalPlace != null) {
            statement.bindString(9, mTrainFlightArrivalPlace);
        } else {
            statement.bindNull(9);
        }
        Integer mReservedInt = rawTrainFlightTickInfo.getMReservedInt();
        if (mReservedInt != null) {
            statement.bindLong(10, (long) mReservedInt.intValue());
        } else {
            statement.bindNull(10);
        }
        String mReservedText = rawTrainFlightTickInfo.getMReservedText();
        if (mReservedText != null) {
            statement.bindString(11, mReservedText);
        } else {
            statement.bindNull(11);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public RawTrainFlightTickInfo readObject(Cursor cursor, int i) {
        return new RawTrainFlightTickInfo(cursor);
    }

    public void setPrimaryKeyValue(RawTrainFlightTickInfo rawTrainFlightTickInfo, long j) {
        rawTrainFlightTickInfo.setMId(Integer.valueOf((int) j));
    }
}
