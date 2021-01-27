package com.huawei.nb.model.trajectory;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class HubTrajectoryDataHelper extends AEntityHelper<HubTrajectoryData> {
    private static final HubTrajectoryDataHelper INSTANCE = new HubTrajectoryDataHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, HubTrajectoryData hubTrajectoryData) {
        return null;
    }

    private HubTrajectoryDataHelper() {
    }

    public static HubTrajectoryDataHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, HubTrajectoryData hubTrajectoryData) {
        Integer id = hubTrajectoryData.getId();
        if (id != null) {
            statement.bindLong(1, (long) id.intValue());
        } else {
            statement.bindNull(1);
        }
        Date mTimeStamp = hubTrajectoryData.getMTimeStamp();
        if (mTimeStamp != null) {
            statement.bindLong(2, mTimeStamp.getTime());
        } else {
            statement.bindNull(2);
        }
        Integer mCellID = hubTrajectoryData.getMCellID();
        if (mCellID != null) {
            statement.bindLong(3, (long) mCellID.intValue());
        } else {
            statement.bindNull(3);
        }
        Character mCellLAC = hubTrajectoryData.getMCellLAC();
        if (mCellLAC != null) {
            statement.bindString(4, String.valueOf(mCellLAC));
        } else {
            statement.bindNull(4);
        }
        Integer mCellRssi = hubTrajectoryData.getMCellRssi();
        if (mCellRssi != null) {
            statement.bindLong(5, (long) mCellRssi.intValue());
        } else {
            statement.bindNull(5);
        }
        Character mmcc = hubTrajectoryData.getMMCC();
        if (mmcc != null) {
            statement.bindString(6, String.valueOf(mmcc));
        } else {
            statement.bindNull(6);
        }
        Character mmnc = hubTrajectoryData.getMMNC();
        if (mmnc != null) {
            statement.bindString(7, String.valueOf(mmnc));
        } else {
            statement.bindNull(7);
        }
        Integer mCellID1 = hubTrajectoryData.getMCellID1();
        if (mCellID1 != null) {
            statement.bindLong(8, (long) mCellID1.intValue());
        } else {
            statement.bindNull(8);
        }
        Character mCellLAC1 = hubTrajectoryData.getMCellLAC1();
        if (mCellLAC1 != null) {
            statement.bindString(9, String.valueOf(mCellLAC1));
        } else {
            statement.bindNull(9);
        }
        Integer mCellRssi1 = hubTrajectoryData.getMCellRssi1();
        if (mCellRssi1 != null) {
            statement.bindLong(10, (long) mCellRssi1.intValue());
        } else {
            statement.bindNull(10);
        }
        Character mmcc1 = hubTrajectoryData.getMMCC1();
        if (mmcc1 != null) {
            statement.bindString(11, String.valueOf(mmcc1));
        } else {
            statement.bindNull(11);
        }
        Character mmnc1 = hubTrajectoryData.getMMNC1();
        if (mmnc1 != null) {
            statement.bindString(12, String.valueOf(mmnc1));
        } else {
            statement.bindNull(12);
        }
        Integer mCellID2 = hubTrajectoryData.getMCellID2();
        if (mCellID2 != null) {
            statement.bindLong(13, (long) mCellID2.intValue());
        } else {
            statement.bindNull(13);
        }
        Character mCellLAC2 = hubTrajectoryData.getMCellLAC2();
        if (mCellLAC2 != null) {
            statement.bindString(14, String.valueOf(mCellLAC2));
        } else {
            statement.bindNull(14);
        }
        Integer mCellRssi2 = hubTrajectoryData.getMCellRssi2();
        if (mCellRssi2 != null) {
            statement.bindLong(15, (long) mCellRssi2.intValue());
        } else {
            statement.bindNull(15);
        }
        Character mmcc2 = hubTrajectoryData.getMMCC2();
        if (mmcc2 != null) {
            statement.bindString(16, String.valueOf(mmcc2));
        } else {
            statement.bindNull(16);
        }
        Character mmnc2 = hubTrajectoryData.getMMNC2();
        if (mmnc2 != null) {
            statement.bindString(17, String.valueOf(mmnc2));
        } else {
            statement.bindNull(17);
        }
        Integer mReserved1 = hubTrajectoryData.getMReserved1();
        if (mReserved1 != null) {
            statement.bindLong(18, (long) mReserved1.intValue());
        } else {
            statement.bindNull(18);
        }
        String mReserved2 = hubTrajectoryData.getMReserved2();
        if (mReserved2 != null) {
            statement.bindString(19, mReserved2);
        } else {
            statement.bindNull(19);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public HubTrajectoryData readObject(Cursor cursor, int i) {
        return new HubTrajectoryData(cursor);
    }

    public void setPrimaryKeyValue(HubTrajectoryData hubTrajectoryData, long j) {
        hubTrajectoryData.setId(Integer.valueOf((int) j));
    }
}
