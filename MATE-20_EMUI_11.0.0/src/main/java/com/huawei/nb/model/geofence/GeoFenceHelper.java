package com.huawei.nb.model.geofence;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class GeoFenceHelper extends AEntityHelper<GeoFence> {
    private static final GeoFenceHelper INSTANCE = new GeoFenceHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, GeoFence geoFence) {
        return null;
    }

    private GeoFenceHelper() {
    }

    public static GeoFenceHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, GeoFence geoFence) {
        Integer mRuleId = geoFence.getMRuleId();
        if (mRuleId != null) {
            statement.bindLong(1, (long) mRuleId.intValue());
        } else {
            statement.bindNull(1);
        }
        String mFenceID = geoFence.getMFenceID();
        if (mFenceID != null) {
            statement.bindString(2, mFenceID);
        } else {
            statement.bindNull(2);
        }
        String mOutFenceID = geoFence.getMOutFenceID();
        if (mOutFenceID != null) {
            statement.bindString(3, mOutFenceID);
        } else {
            statement.bindNull(3);
        }
        String mBlocksID = geoFence.getMBlocksID();
        if (mBlocksID != null) {
            statement.bindString(4, mBlocksID);
        } else {
            statement.bindNull(4);
        }
        String mCategory = geoFence.getMCategory();
        if (mCategory != null) {
            statement.bindString(5, mCategory);
        } else {
            statement.bindNull(5);
        }
        String mSubCategory = geoFence.getMSubCategory();
        if (mSubCategory != null) {
            statement.bindString(6, mSubCategory);
        } else {
            statement.bindNull(6);
        }
        String mName = geoFence.getMName();
        if (mName != null) {
            statement.bindString(7, mName);
        } else {
            statement.bindNull(7);
        }
        Character mInoutdoor = geoFence.getMInoutdoor();
        if (mInoutdoor != null) {
            statement.bindString(8, String.valueOf(mInoutdoor));
        } else {
            statement.bindNull(8);
        }
        String mFloor = geoFence.getMFloor();
        if (mFloor != null) {
            statement.bindString(9, mFloor);
        } else {
            statement.bindNull(9);
        }
        Character mTeleoperators = geoFence.getMTeleoperators();
        if (mTeleoperators != null) {
            statement.bindString(10, String.valueOf(mTeleoperators));
        } else {
            statement.bindNull(10);
        }
        String mNearCellid = geoFence.getMNearCellid();
        if (mNearCellid != null) {
            statement.bindString(11, mNearCellid);
        } else {
            statement.bindNull(11);
        }
        String mEnterCellid = geoFence.getMEnterCellid();
        if (mEnterCellid != null) {
            statement.bindString(12, mEnterCellid);
        } else {
            statement.bindNull(12);
        }
        Character mShape = geoFence.getMShape();
        if (mShape != null) {
            statement.bindString(13, String.valueOf(mShape));
        } else {
            statement.bindNull(13);
        }
        String mEnterLocation = geoFence.getMEnterLocation();
        if (mEnterLocation != null) {
            statement.bindString(14, mEnterLocation);
        } else {
            statement.bindNull(14);
        }
        String mNearLocation = geoFence.getMNearLocation();
        if (mNearLocation != null) {
            statement.bindString(15, mNearLocation);
        } else {
            statement.bindNull(15);
        }
        String mCenter = geoFence.getMCenter();
        if (mCenter != null) {
            statement.bindString(16, mCenter);
        } else {
            statement.bindNull(16);
        }
        Integer mCityCode = geoFence.getMCityCode();
        if (mCityCode != null) {
            statement.bindLong(17, (long) mCityCode.intValue());
        } else {
            statement.bindNull(17);
        }
        Character mImportance = geoFence.getMImportance();
        if (mImportance != null) {
            statement.bindString(18, String.valueOf(mImportance));
        } else {
            statement.bindNull(18);
        }
        String mLastUpdated = geoFence.getMLastUpdated();
        if (mLastUpdated != null) {
            statement.bindString(19, mLastUpdated);
        } else {
            statement.bindNull(19);
        }
        Short mStatus = geoFence.getMStatus();
        if (mStatus != null) {
            statement.bindLong(20, (long) mStatus.shortValue());
        } else {
            statement.bindNull(20);
        }
        String mReserved1 = geoFence.getMReserved1();
        if (mReserved1 != null) {
            statement.bindString(21, mReserved1);
        } else {
            statement.bindNull(21);
        }
        String mReserved2 = geoFence.getMReserved2();
        if (mReserved2 != null) {
            statement.bindString(22, mReserved2);
        } else {
            statement.bindNull(22);
        }
        Long mLastLeftTime = geoFence.getMLastLeftTime();
        if (mLastLeftTime != null) {
            statement.bindLong(23, mLastLeftTime.longValue());
        } else {
            statement.bindNull(23);
        }
        Short mEnteredNum = geoFence.getMEnteredNum();
        if (mEnteredNum != null) {
            statement.bindLong(24, (long) mEnteredNum.shortValue());
        } else {
            statement.bindNull(24);
        }
        Character mDisabled = geoFence.getMDisabled();
        if (mDisabled != null) {
            statement.bindString(25, String.valueOf(mDisabled));
        } else {
            statement.bindNull(25);
        }
        String mNearWifiBssid = geoFence.getMNearWifiBssid();
        if (mNearWifiBssid != null) {
            statement.bindString(26, mNearWifiBssid);
        } else {
            statement.bindNull(26);
        }
        String mEnterWifiBssid = geoFence.getMEnterWifiBssid();
        if (mEnterWifiBssid != null) {
            statement.bindString(27, mEnterWifiBssid);
        } else {
            statement.bindNull(27);
        }
        String mWifiChannel = geoFence.getMWifiChannel();
        if (mWifiChannel != null) {
            statement.bindString(28, mWifiChannel);
        } else {
            statement.bindNull(28);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public GeoFence readObject(Cursor cursor, int i) {
        return new GeoFence(cursor);
    }

    public void setPrimaryKeyValue(GeoFence geoFence, long j) {
        geoFence.setMRuleId(Integer.valueOf((int) j));
    }
}
