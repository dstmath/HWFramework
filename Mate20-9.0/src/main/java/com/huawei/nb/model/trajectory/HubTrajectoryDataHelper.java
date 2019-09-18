package com.huawei.nb.model.trajectory;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class HubTrajectoryDataHelper extends AEntityHelper<HubTrajectoryData> {
    private static final HubTrajectoryDataHelper INSTANCE = new HubTrajectoryDataHelper();

    private HubTrajectoryDataHelper() {
    }

    public static HubTrajectoryDataHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, HubTrajectoryData object) {
        Integer id = object.getId();
        if (id != null) {
            statement.bindLong(1, (long) id.intValue());
        } else {
            statement.bindNull(1);
        }
        Date mTimeStamp = object.getMTimeStamp();
        if (mTimeStamp != null) {
            statement.bindLong(2, mTimeStamp.getTime());
        } else {
            statement.bindNull(2);
        }
        Integer mCellID = object.getMCellID();
        if (mCellID != null) {
            statement.bindLong(3, (long) mCellID.intValue());
        } else {
            statement.bindNull(3);
        }
        Character mCellLAC = object.getMCellLAC();
        if (mCellLAC != null) {
            statement.bindString(4, String.valueOf(mCellLAC));
        } else {
            statement.bindNull(4);
        }
        Integer mCellRssi = object.getMCellRssi();
        if (mCellRssi != null) {
            statement.bindLong(5, (long) mCellRssi.intValue());
        } else {
            statement.bindNull(5);
        }
        Character mMCC = object.getMMCC();
        if (mMCC != null) {
            statement.bindString(6, String.valueOf(mMCC));
        } else {
            statement.bindNull(6);
        }
        Character mMNC = object.getMMNC();
        if (mMNC != null) {
            statement.bindString(7, String.valueOf(mMNC));
        } else {
            statement.bindNull(7);
        }
        Integer mCellID1 = object.getMCellID1();
        if (mCellID1 != null) {
            statement.bindLong(8, (long) mCellID1.intValue());
        } else {
            statement.bindNull(8);
        }
        Character mCellLAC1 = object.getMCellLAC1();
        if (mCellLAC1 != null) {
            statement.bindString(9, String.valueOf(mCellLAC1));
        } else {
            statement.bindNull(9);
        }
        Integer mCellRssi1 = object.getMCellRssi1();
        if (mCellRssi1 != null) {
            statement.bindLong(10, (long) mCellRssi1.intValue());
        } else {
            statement.bindNull(10);
        }
        Character mMCC1 = object.getMMCC1();
        if (mMCC1 != null) {
            statement.bindString(11, String.valueOf(mMCC1));
        } else {
            statement.bindNull(11);
        }
        Character mMNC1 = object.getMMNC1();
        if (mMNC1 != null) {
            statement.bindString(12, String.valueOf(mMNC1));
        } else {
            statement.bindNull(12);
        }
        Integer mCellID2 = object.getMCellID2();
        if (mCellID2 != null) {
            statement.bindLong(13, (long) mCellID2.intValue());
        } else {
            statement.bindNull(13);
        }
        Character mCellLAC2 = object.getMCellLAC2();
        if (mCellLAC2 != null) {
            statement.bindString(14, String.valueOf(mCellLAC2));
        } else {
            statement.bindNull(14);
        }
        Integer mCellRssi2 = object.getMCellRssi2();
        if (mCellRssi2 != null) {
            statement.bindLong(15, (long) mCellRssi2.intValue());
        } else {
            statement.bindNull(15);
        }
        Character mMCC2 = object.getMMCC2();
        if (mMCC2 != null) {
            statement.bindString(16, String.valueOf(mMCC2));
        } else {
            statement.bindNull(16);
        }
        Character mMNC2 = object.getMMNC2();
        if (mMNC2 != null) {
            statement.bindString(17, String.valueOf(mMNC2));
        } else {
            statement.bindNull(17);
        }
        Integer mReserved1 = object.getMReserved1();
        if (mReserved1 != null) {
            statement.bindLong(18, (long) mReserved1.intValue());
        } else {
            statement.bindNull(18);
        }
        String mReserved2 = object.getMReserved2();
        if (mReserved2 != null) {
            statement.bindString(19, mReserved2);
        } else {
            statement.bindNull(19);
        }
    }

    public HubTrajectoryData readObject(Cursor cursor, int offset) {
        return new HubTrajectoryData(cursor);
    }

    public void setPrimaryKeyValue(HubTrajectoryData object, long value) {
        object.setId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, HubTrajectoryData object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
