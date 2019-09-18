package com.huawei.nb.model.policy;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class ObservedGeoFenceHelper extends AEntityHelper<ObservedGeoFence> {
    private static final ObservedGeoFenceHelper INSTANCE = new ObservedGeoFenceHelper();

    private ObservedGeoFenceHelper() {
    }

    public static ObservedGeoFenceHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, ObservedGeoFence object) {
        Long mID = object.getMID();
        if (mID != null) {
            statement.bindLong(1, mID.longValue());
        } else {
            statement.bindNull(1);
        }
        String mFenceID = object.getMFenceID();
        if (mFenceID != null) {
            statement.bindString(2, mFenceID);
        } else {
            statement.bindNull(2);
        }
        String mName = object.getMName();
        if (mName != null) {
            statement.bindString(3, mName);
        } else {
            statement.bindNull(3);
        }
        String mCategory = object.getMCategory();
        if (mCategory != null) {
            statement.bindString(4, mCategory);
        } else {
            statement.bindNull(4);
        }
        String mSubCategory = object.getMSubCategory();
        if (mSubCategory != null) {
            statement.bindString(5, mSubCategory);
        } else {
            statement.bindNull(5);
        }
        Integer mShape = object.getMShape();
        if (mShape != null) {
            statement.bindLong(6, (long) mShape.intValue());
        } else {
            statement.bindNull(6);
        }
        String mGeoValue = object.getMGeoValue();
        if (mGeoValue != null) {
            statement.bindString(7, mGeoValue);
        } else {
            statement.bindNull(7);
        }
        Short mStatus = object.getMStatus();
        if (mStatus != null) {
            statement.bindLong(8, (long) mStatus.shortValue());
        } else {
            statement.bindNull(8);
        }
        String mReserve = object.getMReserve();
        if (mReserve != null) {
            statement.bindString(9, mReserve);
        } else {
            statement.bindNull(9);
        }
        String mWorkTime = object.getMWorkTime();
        if (mWorkTime != null) {
            statement.bindString(10, mWorkTime);
        } else {
            statement.bindNull(10);
        }
        String mSameFenceMaxTriggersPerDay = object.getMSameFenceMaxTriggersPerDay();
        if (mSameFenceMaxTriggersPerDay != null) {
            statement.bindString(11, mSameFenceMaxTriggersPerDay);
        } else {
            statement.bindNull(11);
        }
        String mSameFenceMinTriggerInterval = object.getMSameFenceMinTriggerInterval();
        if (mSameFenceMinTriggerInterval != null) {
            statement.bindString(12, mSameFenceMinTriggerInterval);
        } else {
            statement.bindNull(12);
        }
        String mMaxTriggersPerDay = object.getMMaxTriggersPerDay();
        if (mMaxTriggersPerDay != null) {
            statement.bindString(13, mMaxTriggersPerDay);
        } else {
            statement.bindNull(13);
        }
    }

    public ObservedGeoFence readObject(Cursor cursor, int offset) {
        return new ObservedGeoFence(cursor);
    }

    public void setPrimaryKeyValue(ObservedGeoFence object, long value) {
        object.setMID(Long.valueOf(value));
    }

    public Object getRelationshipObject(String field, ObservedGeoFence object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
