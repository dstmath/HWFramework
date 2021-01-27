package com.huawei.nb.model.policy;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class ObservedGeoFenceHelper extends AEntityHelper<ObservedGeoFence> {
    private static final ObservedGeoFenceHelper INSTANCE = new ObservedGeoFenceHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, ObservedGeoFence observedGeoFence) {
        return null;
    }

    private ObservedGeoFenceHelper() {
    }

    public static ObservedGeoFenceHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, ObservedGeoFence observedGeoFence) {
        Long mid = observedGeoFence.getMID();
        if (mid != null) {
            statement.bindLong(1, mid.longValue());
        } else {
            statement.bindNull(1);
        }
        String mFenceID = observedGeoFence.getMFenceID();
        if (mFenceID != null) {
            statement.bindString(2, mFenceID);
        } else {
            statement.bindNull(2);
        }
        String mName = observedGeoFence.getMName();
        if (mName != null) {
            statement.bindString(3, mName);
        } else {
            statement.bindNull(3);
        }
        String mCategory = observedGeoFence.getMCategory();
        if (mCategory != null) {
            statement.bindString(4, mCategory);
        } else {
            statement.bindNull(4);
        }
        String mSubCategory = observedGeoFence.getMSubCategory();
        if (mSubCategory != null) {
            statement.bindString(5, mSubCategory);
        } else {
            statement.bindNull(5);
        }
        Integer mShape = observedGeoFence.getMShape();
        if (mShape != null) {
            statement.bindLong(6, (long) mShape.intValue());
        } else {
            statement.bindNull(6);
        }
        String mGeoValue = observedGeoFence.getMGeoValue();
        if (mGeoValue != null) {
            statement.bindString(7, mGeoValue);
        } else {
            statement.bindNull(7);
        }
        Short mStatus = observedGeoFence.getMStatus();
        if (mStatus != null) {
            statement.bindLong(8, (long) mStatus.shortValue());
        } else {
            statement.bindNull(8);
        }
        String mReserve = observedGeoFence.getMReserve();
        if (mReserve != null) {
            statement.bindString(9, mReserve);
        } else {
            statement.bindNull(9);
        }
        String mWorkTime = observedGeoFence.getMWorkTime();
        if (mWorkTime != null) {
            statement.bindString(10, mWorkTime);
        } else {
            statement.bindNull(10);
        }
        String mSameFenceMaxTriggersPerDay = observedGeoFence.getMSameFenceMaxTriggersPerDay();
        if (mSameFenceMaxTriggersPerDay != null) {
            statement.bindString(11, mSameFenceMaxTriggersPerDay);
        } else {
            statement.bindNull(11);
        }
        String mSameFenceMinTriggerInterval = observedGeoFence.getMSameFenceMinTriggerInterval();
        if (mSameFenceMinTriggerInterval != null) {
            statement.bindString(12, mSameFenceMinTriggerInterval);
        } else {
            statement.bindNull(12);
        }
        String mMaxTriggersPerDay = observedGeoFence.getMMaxTriggersPerDay();
        if (mMaxTriggersPerDay != null) {
            statement.bindString(13, mMaxTriggersPerDay);
        } else {
            statement.bindNull(13);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public ObservedGeoFence readObject(Cursor cursor, int i) {
        return new ObservedGeoFence(cursor);
    }

    public void setPrimaryKeyValue(ObservedGeoFence observedGeoFence, long j) {
        observedGeoFence.setMID(Long.valueOf(j));
    }
}
