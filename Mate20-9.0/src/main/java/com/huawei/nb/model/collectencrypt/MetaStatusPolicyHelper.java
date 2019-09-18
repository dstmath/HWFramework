package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class MetaStatusPolicyHelper extends AEntityHelper<MetaStatusPolicy> {
    private static final MetaStatusPolicyHelper INSTANCE = new MetaStatusPolicyHelper();

    private MetaStatusPolicyHelper() {
    }

    public static MetaStatusPolicyHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, MetaStatusPolicy object) {
        Integer mId = object.getMId();
        if (mId != null) {
            statement.bindLong(1, (long) mId.intValue());
        } else {
            statement.bindNull(1);
        }
        String mPhoneState = object.getMPhoneState();
        if (mPhoneState != null) {
            statement.bindString(2, mPhoneState);
        } else {
            statement.bindNull(2);
        }
        String mRoamingState = object.getMRoamingState();
        if (mRoamingState != null) {
            statement.bindString(3, mRoamingState);
        } else {
            statement.bindNull(3);
        }
        String mBatteryState = object.getMBatteryState();
        if (mBatteryState != null) {
            statement.bindString(4, mBatteryState);
        } else {
            statement.bindNull(4);
        }
        String mPowerSaving = object.getMPowerSaving();
        if (mPowerSaving != null) {
            statement.bindString(5, mPowerSaving);
        } else {
            statement.bindNull(5);
        }
        String mPowerSupply = object.getMPowerSupply();
        if (mPowerSupply != null) {
            statement.bindString(6, mPowerSupply);
        } else {
            statement.bindNull(6);
        }
        String mScreenState = object.getMScreenState();
        if (mScreenState != null) {
            statement.bindString(7, mScreenState);
        } else {
            statement.bindNull(7);
        }
        String mHeadsetState = object.getMHeadsetState();
        if (mHeadsetState != null) {
            statement.bindString(8, mHeadsetState);
        } else {
            statement.bindNull(8);
        }
        String mBluetoothState = object.getMBluetoothState();
        if (mBluetoothState != null) {
            statement.bindString(9, mBluetoothState);
        } else {
            statement.bindNull(9);
        }
        String mWifiState = object.getMWifiState();
        if (mWifiState != null) {
            statement.bindString(10, mWifiState);
        } else {
            statement.bindNull(10);
        }
        String mStudentState = object.getMStudentState();
        if (mStudentState != null) {
            statement.bindString(11, mStudentState);
        } else {
            statement.bindNull(11);
        }
        String mAirplaneMode = object.getMAirplaneMode();
        if (mAirplaneMode != null) {
            statement.bindString(12, mAirplaneMode);
        } else {
            statement.bindNull(12);
        }
        String mNoDisturb = object.getMNoDisturb();
        if (mNoDisturb != null) {
            statement.bindString(13, mNoDisturb);
        } else {
            statement.bindNull(13);
        }
        String mSilentMode = object.getMSilentMode();
        if (mSilentMode != null) {
            statement.bindString(14, mSilentMode);
        } else {
            statement.bindNull(14);
        }
        String mNfcSwitch = object.getMNfcSwitch();
        if (mNfcSwitch != null) {
            statement.bindString(15, mNfcSwitch);
        } else {
            statement.bindNull(15);
        }
        String mEyeComfortSwitch = object.getMEyeComfortSwitch();
        if (mEyeComfortSwitch != null) {
            statement.bindString(16, mEyeComfortSwitch);
        } else {
            statement.bindNull(16);
        }
        String mHiBoardSwitch = object.getMHiBoardSwitch();
        if (mHiBoardSwitch != null) {
            statement.bindString(17, mHiBoardSwitch);
        } else {
            statement.bindNull(17);
        }
        Integer mReservedInt = object.getMReservedInt();
        if (mReservedInt != null) {
            statement.bindLong(18, (long) mReservedInt.intValue());
        } else {
            statement.bindNull(18);
        }
        String mReservedText = object.getMReservedText();
        if (mReservedText != null) {
            statement.bindString(19, mReservedText);
        } else {
            statement.bindNull(19);
        }
    }

    public MetaStatusPolicy readObject(Cursor cursor, int offset) {
        return new MetaStatusPolicy(cursor);
    }

    public void setPrimaryKeyValue(MetaStatusPolicy object, long value) {
        object.setMId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, MetaStatusPolicy object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
