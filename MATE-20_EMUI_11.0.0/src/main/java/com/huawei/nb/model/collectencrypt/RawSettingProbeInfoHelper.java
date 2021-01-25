package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RawSettingProbeInfoHelper extends AEntityHelper<RawSettingProbeInfo> {
    private static final RawSettingProbeInfoHelper INSTANCE = new RawSettingProbeInfoHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, RawSettingProbeInfo rawSettingProbeInfo) {
        return null;
    }

    private RawSettingProbeInfoHelper() {
    }

    public static RawSettingProbeInfoHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, RawSettingProbeInfo rawSettingProbeInfo) {
        Integer mId = rawSettingProbeInfo.getMId();
        if (mId != null) {
            statement.bindLong(1, (long) mId.intValue());
        } else {
            statement.bindNull(1);
        }
        Date mTimeStamp = rawSettingProbeInfo.getMTimeStamp();
        if (mTimeStamp != null) {
            statement.bindLong(2, mTimeStamp.getTime());
        } else {
            statement.bindNull(2);
        }
        Long mSettingsStart = rawSettingProbeInfo.getMSettingsStart();
        if (mSettingsStart != null) {
            statement.bindLong(3, mSettingsStart.longValue());
        } else {
            statement.bindNull(3);
        }
        Integer mSettingsType = rawSettingProbeInfo.getMSettingsType();
        if (mSettingsType != null) {
            statement.bindLong(4, (long) mSettingsType.intValue());
        } else {
            statement.bindNull(4);
        }
        Integer mOperateType = rawSettingProbeInfo.getMOperateType();
        if (mOperateType != null) {
            statement.bindLong(5, (long) mOperateType.intValue());
        } else {
            statement.bindNull(5);
        }
        Integer mReservedInt = rawSettingProbeInfo.getMReservedInt();
        if (mReservedInt != null) {
            statement.bindLong(6, (long) mReservedInt.intValue());
        } else {
            statement.bindNull(6);
        }
        String mReservedText = rawSettingProbeInfo.getMReservedText();
        if (mReservedText != null) {
            statement.bindString(7, mReservedText);
        } else {
            statement.bindNull(7);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public RawSettingProbeInfo readObject(Cursor cursor, int i) {
        return new RawSettingProbeInfo(cursor);
    }

    public void setPrimaryKeyValue(RawSettingProbeInfo rawSettingProbeInfo, long j) {
        rawSettingProbeInfo.setMId(Integer.valueOf((int) j));
    }
}
