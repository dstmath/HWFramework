package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class MetaTripInfoHelper extends AEntityHelper<MetaTripInfo> {
    private static final MetaTripInfoHelper INSTANCE = new MetaTripInfoHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, MetaTripInfo metaTripInfo) {
        return null;
    }

    private MetaTripInfoHelper() {
    }

    public static MetaTripInfoHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, MetaTripInfo metaTripInfo) {
        Integer mId = metaTripInfo.getMId();
        if (mId != null) {
            statement.bindLong(1, (long) mId.intValue());
        } else {
            statement.bindNull(1);
        }
        Date mTimeStamp = metaTripInfo.getMTimeStamp();
        if (mTimeStamp != null) {
            statement.bindLong(2, mTimeStamp.getTime());
        } else {
            statement.bindNull(2);
        }
        String mTripType = metaTripInfo.getMTripType();
        if (mTripType != null) {
            statement.bindString(3, mTripType);
        } else {
            statement.bindNull(3);
        }
        String mTripNo = metaTripInfo.getMTripNo();
        if (mTripNo != null) {
            statement.bindString(4, mTripNo);
        } else {
            statement.bindNull(4);
        }
        String mSeatNo = metaTripInfo.getMSeatNo();
        if (mSeatNo != null) {
            statement.bindString(5, mSeatNo);
        } else {
            statement.bindNull(5);
        }
        Date mStartTime = metaTripInfo.getMStartTime();
        if (mStartTime != null) {
            statement.bindLong(6, mStartTime.getTime());
        } else {
            statement.bindNull(6);
        }
        String mStartPlace = metaTripInfo.getMStartPlace();
        if (mStartPlace != null) {
            statement.bindString(7, mStartPlace);
        } else {
            statement.bindNull(7);
        }
        String mArrivalPlace = metaTripInfo.getMArrivalPlace();
        if (mArrivalPlace != null) {
            statement.bindString(8, mArrivalPlace);
        } else {
            statement.bindNull(8);
        }
        String mProvider = metaTripInfo.getMProvider();
        if (mProvider != null) {
            statement.bindString(9, mProvider);
        } else {
            statement.bindNull(9);
        }
        String mTripSeat = metaTripInfo.getMTripSeat();
        if (mTripSeat != null) {
            statement.bindString(10, mTripSeat);
        } else {
            statement.bindNull(10);
        }
        Integer mReservedInt = metaTripInfo.getMReservedInt();
        if (mReservedInt != null) {
            statement.bindLong(11, (long) mReservedInt.intValue());
        } else {
            statement.bindNull(11);
        }
        String mReservedText = metaTripInfo.getMReservedText();
        if (mReservedText != null) {
            statement.bindString(12, mReservedText);
        } else {
            statement.bindNull(12);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public MetaTripInfo readObject(Cursor cursor, int i) {
        return new MetaTripInfo(cursor);
    }

    public void setPrimaryKeyValue(MetaTripInfo metaTripInfo, long j) {
        metaTripInfo.setMId(Integer.valueOf((int) j));
    }
}
