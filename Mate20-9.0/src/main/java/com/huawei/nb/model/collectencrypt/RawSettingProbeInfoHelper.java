package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RawSettingProbeInfoHelper extends AEntityHelper<RawSettingProbeInfo> {
    private static final RawSettingProbeInfoHelper INSTANCE = new RawSettingProbeInfoHelper();

    private RawSettingProbeInfoHelper() {
    }

    public static RawSettingProbeInfoHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, RawSettingProbeInfo object) {
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
        Long mSettingsStart = object.getMSettingsStart();
        if (mSettingsStart != null) {
            statement.bindLong(3, mSettingsStart.longValue());
        } else {
            statement.bindNull(3);
        }
        Integer mSettingsType = object.getMSettingsType();
        if (mSettingsType != null) {
            statement.bindLong(4, (long) mSettingsType.intValue());
        } else {
            statement.bindNull(4);
        }
        Integer mOperateType = object.getMOperateType();
        if (mOperateType != null) {
            statement.bindLong(5, (long) mOperateType.intValue());
        } else {
            statement.bindNull(5);
        }
        Integer mReservedInt = object.getMReservedInt();
        if (mReservedInt != null) {
            statement.bindLong(6, (long) mReservedInt.intValue());
        } else {
            statement.bindNull(6);
        }
        String mReservedText = object.getMReservedText();
        if (mReservedText != null) {
            statement.bindString(7, mReservedText);
        } else {
            statement.bindNull(7);
        }
    }

    public RawSettingProbeInfo readObject(Cursor cursor, int offset) {
        return new RawSettingProbeInfo(cursor);
    }

    public void setPrimaryKeyValue(RawSettingProbeInfo object, long value) {
        object.setMId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, RawSettingProbeInfo object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
