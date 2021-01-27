package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RawPositionStateHelper extends AEntityHelper<RawPositionState> {
    private static final RawPositionStateHelper INSTANCE = new RawPositionStateHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, RawPositionState rawPositionState) {
        return null;
    }

    private RawPositionStateHelper() {
    }

    public static RawPositionStateHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, RawPositionState rawPositionState) {
        Integer mId = rawPositionState.getMId();
        if (mId != null) {
            statement.bindLong(1, (long) mId.intValue());
        } else {
            statement.bindNull(1);
        }
        Date mTimeStamp = rawPositionState.getMTimeStamp();
        if (mTimeStamp != null) {
            statement.bindLong(2, mTimeStamp.getTime());
        } else {
            statement.bindNull(2);
        }
        Integer mStatus = rawPositionState.getMStatus();
        if (mStatus != null) {
            statement.bindLong(3, (long) mStatus.intValue());
        } else {
            statement.bindNull(3);
        }
        Integer mReservedInt = rawPositionState.getMReservedInt();
        if (mReservedInt != null) {
            statement.bindLong(4, (long) mReservedInt.intValue());
        } else {
            statement.bindNull(4);
        }
        String mReservedText = rawPositionState.getMReservedText();
        if (mReservedText != null) {
            statement.bindString(5, mReservedText);
        } else {
            statement.bindNull(5);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public RawPositionState readObject(Cursor cursor, int i) {
        return new RawPositionState(cursor);
    }

    public void setPrimaryKeyValue(RawPositionState rawPositionState, long j) {
        rawPositionState.setMId(Integer.valueOf((int) j));
    }
}
