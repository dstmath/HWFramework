package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class MetaStatusPolicyHelper extends AEntityHelper<MetaStatusPolicy> {
    private static final MetaStatusPolicyHelper INSTANCE = new MetaStatusPolicyHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, MetaStatusPolicy metaStatusPolicy) {
        return null;
    }

    private MetaStatusPolicyHelper() {
    }

    public static MetaStatusPolicyHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, MetaStatusPolicy metaStatusPolicy) {
        Integer mId = metaStatusPolicy.getMId();
        if (mId != null) {
            statement.bindLong(1, (long) mId.intValue());
        } else {
            statement.bindNull(1);
        }
        String mPhoneState = metaStatusPolicy.getMPhoneState();
        if (mPhoneState != null) {
            statement.bindString(2, mPhoneState);
        } else {
            statement.bindNull(2);
        }
        String mRoamingState = metaStatusPolicy.getMRoamingState();
        if (mRoamingState != null) {
            statement.bindString(3, mRoamingState);
        } else {
            statement.bindNull(3);
        }
        String mBatteryState = metaStatusPolicy.getMBatteryState();
        if (mBatteryState != null) {
            statement.bindString(4, mBatteryState);
        } else {
            statement.bindNull(4);
        }
        String mPowerSaving = metaStatusPolicy.getMPowerSaving();
        if (mPowerSaving != null) {
            statement.bindString(5, mPowerSaving);
        } else {
            statement.bindNull(5);
        }
        String mPowerSupply = metaStatusPolicy.getMPowerSupply();
        if (mPowerSupply != null) {
            statement.bindString(6, mPowerSupply);
        } else {
            statement.bindNull(6);
        }
        String mScreenState = metaStatusPolicy.getMScreenState();
        if (mScreenState != null) {
            statement.bindString(7, mScreenState);
        } else {
            statement.bindNull(7);
        }
        String mHeadsetState = metaStatusPolicy.getMHeadsetState();
        if (mHeadsetState != null) {
            statement.bindString(8, mHeadsetState);
        } else {
            statement.bindNull(8);
        }
        String mBluetoothState = metaStatusPolicy.getMBluetoothState();
        if (mBluetoothState != null) {
            statement.bindString(9, mBluetoothState);
        } else {
            statement.bindNull(9);
        }
        String mWifiState = metaStatusPolicy.getMWifiState();
        if (mWifiState != null) {
            statement.bindString(10, mWifiState);
        } else {
            statement.bindNull(10);
        }
        String mStudentState = metaStatusPolicy.getMStudentState();
        if (mStudentState != null) {
            statement.bindString(11, mStudentState);
        } else {
            statement.bindNull(11);
        }
        String mAirplaneMode = metaStatusPolicy.getMAirplaneMode();
        if (mAirplaneMode != null) {
            statement.bindString(12, mAirplaneMode);
        } else {
            statement.bindNull(12);
        }
        String mNoDisturb = metaStatusPolicy.getMNoDisturb();
        if (mNoDisturb != null) {
            statement.bindString(13, mNoDisturb);
        } else {
            statement.bindNull(13);
        }
        String mSilentMode = metaStatusPolicy.getMSilentMode();
        if (mSilentMode != null) {
            statement.bindString(14, mSilentMode);
        } else {
            statement.bindNull(14);
        }
        String mNfcSwitch = metaStatusPolicy.getMNfcSwitch();
        if (mNfcSwitch != null) {
            statement.bindString(15, mNfcSwitch);
        } else {
            statement.bindNull(15);
        }
        String mEyeComfortSwitch = metaStatusPolicy.getMEyeComfortSwitch();
        if (mEyeComfortSwitch != null) {
            statement.bindString(16, mEyeComfortSwitch);
        } else {
            statement.bindNull(16);
        }
        String mHiBoardSwitch = metaStatusPolicy.getMHiBoardSwitch();
        if (mHiBoardSwitch != null) {
            statement.bindString(17, mHiBoardSwitch);
        } else {
            statement.bindNull(17);
        }
        Integer mReservedInt = metaStatusPolicy.getMReservedInt();
        if (mReservedInt != null) {
            statement.bindLong(18, (long) mReservedInt.intValue());
        } else {
            statement.bindNull(18);
        }
        String mReservedText = metaStatusPolicy.getMReservedText();
        if (mReservedText != null) {
            statement.bindString(19, mReservedText);
        } else {
            statement.bindNull(19);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public MetaStatusPolicy readObject(Cursor cursor, int i) {
        return new MetaStatusPolicy(cursor);
    }

    public void setPrimaryKeyValue(MetaStatusPolicy metaStatusPolicy, long j) {
        metaStatusPolicy.setMId(Integer.valueOf((int) j));
    }
}
