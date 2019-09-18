package com.huawei.nb.model.geofence;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class GeoFenceHelper extends AEntityHelper<GeoFence> {
    private static final GeoFenceHelper INSTANCE = new GeoFenceHelper();

    private GeoFenceHelper() {
    }

    public static GeoFenceHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, GeoFence object) {
        Integer mRuleId = object.getMRuleId();
        if (mRuleId != null) {
            statement.bindLong(1, (long) mRuleId.intValue());
        } else {
            statement.bindNull(1);
        }
        String mFenceID = object.getMFenceID();
        if (mFenceID != null) {
            statement.bindString(2, mFenceID);
        } else {
            statement.bindNull(2);
        }
        String mOutFenceID = object.getMOutFenceID();
        if (mOutFenceID != null) {
            statement.bindString(3, mOutFenceID);
        } else {
            statement.bindNull(3);
        }
        String mBlocksID = object.getMBlocksID();
        if (mBlocksID != null) {
            statement.bindString(4, mBlocksID);
        } else {
            statement.bindNull(4);
        }
        String mCategory = object.getMCategory();
        if (mCategory != null) {
            statement.bindString(5, mCategory);
        } else {
            statement.bindNull(5);
        }
        String mSubCategory = object.getMSubCategory();
        if (mSubCategory != null) {
            statement.bindString(6, mSubCategory);
        } else {
            statement.bindNull(6);
        }
        String mName = object.getMName();
        if (mName != null) {
            statement.bindString(7, mName);
        } else {
            statement.bindNull(7);
        }
        Character mInoutdoor = object.getMInoutdoor();
        if (mInoutdoor != null) {
            statement.bindString(8, String.valueOf(mInoutdoor));
        } else {
            statement.bindNull(8);
        }
        String mFloor = object.getMFloor();
        if (mFloor != null) {
            statement.bindString(9, mFloor);
        } else {
            statement.bindNull(9);
        }
        Character mTeleoperators = object.getMTeleoperators();
        if (mTeleoperators != null) {
            statement.bindString(10, String.valueOf(mTeleoperators));
        } else {
            statement.bindNull(10);
        }
        String mNearCellid = object.getMNearCellid();
        if (mNearCellid != null) {
            statement.bindString(11, mNearCellid);
        } else {
            statement.bindNull(11);
        }
        String mEnterCellid = object.getMEnterCellid();
        if (mEnterCellid != null) {
            statement.bindString(12, mEnterCellid);
        } else {
            statement.bindNull(12);
        }
        Character mShape = object.getMShape();
        if (mShape != null) {
            statement.bindString(13, String.valueOf(mShape));
        } else {
            statement.bindNull(13);
        }
        String mEnterLocation = object.getMEnterLocation();
        if (mEnterLocation != null) {
            statement.bindString(14, mEnterLocation);
        } else {
            statement.bindNull(14);
        }
        String mNearLocation = object.getMNearLocation();
        if (mNearLocation != null) {
            statement.bindString(15, mNearLocation);
        } else {
            statement.bindNull(15);
        }
        String mCenter = object.getMCenter();
        if (mCenter != null) {
            statement.bindString(16, mCenter);
        } else {
            statement.bindNull(16);
        }
        Integer mCityCode = object.getMCityCode();
        if (mCityCode != null) {
            statement.bindLong(17, (long) mCityCode.intValue());
        } else {
            statement.bindNull(17);
        }
        Character mImportance = object.getMImportance();
        if (mImportance != null) {
            statement.bindString(18, String.valueOf(mImportance));
        } else {
            statement.bindNull(18);
        }
        String mLastUpdated = object.getMLastUpdated();
        if (mLastUpdated != null) {
            statement.bindString(19, mLastUpdated);
        } else {
            statement.bindNull(19);
        }
        Short mStatus = object.getMStatus();
        if (mStatus != null) {
            statement.bindLong(20, (long) mStatus.shortValue());
        } else {
            statement.bindNull(20);
        }
        String mReserved1 = object.getMReserved1();
        if (mReserved1 != null) {
            statement.bindString(21, mReserved1);
        } else {
            statement.bindNull(21);
        }
        String mReserved2 = object.getMReserved2();
        if (mReserved2 != null) {
            statement.bindString(22, mReserved2);
        } else {
            statement.bindNull(22);
        }
        Long mLastLeftTime = object.getMLastLeftTime();
        if (mLastLeftTime != null) {
            statement.bindLong(23, mLastLeftTime.longValue());
        } else {
            statement.bindNull(23);
        }
        Short mEnteredNum = object.getMEnteredNum();
        if (mEnteredNum != null) {
            statement.bindLong(24, (long) mEnteredNum.shortValue());
        } else {
            statement.bindNull(24);
        }
        Character mDisabled = object.getMDisabled();
        if (mDisabled != null) {
            statement.bindString(25, String.valueOf(mDisabled));
        } else {
            statement.bindNull(25);
        }
        String mNearWifiBssid = object.getMNearWifiBssid();
        if (mNearWifiBssid != null) {
            statement.bindString(26, mNearWifiBssid);
        } else {
            statement.bindNull(26);
        }
        String mEnterWifiBssid = object.getMEnterWifiBssid();
        if (mEnterWifiBssid != null) {
            statement.bindString(27, mEnterWifiBssid);
        } else {
            statement.bindNull(27);
        }
        String mWifiChannel = object.getMWifiChannel();
        if (mWifiChannel != null) {
            statement.bindString(28, mWifiChannel);
        } else {
            statement.bindNull(28);
        }
    }

    public GeoFence readObject(Cursor cursor, int offset) {
        return new GeoFence(cursor);
    }

    public void setPrimaryKeyValue(GeoFence object, long value) {
        object.setMRuleId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, GeoFence object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
