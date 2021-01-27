package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class MetaCellRecordHelper extends AEntityHelper<MetaCellRecord> {
    private static final MetaCellRecordHelper INSTANCE = new MetaCellRecordHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, MetaCellRecord metaCellRecord) {
        return null;
    }

    private MetaCellRecordHelper() {
    }

    public static MetaCellRecordHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, MetaCellRecord metaCellRecord) {
        Integer mId = metaCellRecord.getMId();
        if (mId != null) {
            statement.bindLong(1, (long) mId.intValue());
        } else {
            statement.bindNull(1);
        }
        Date mTimeStamp = metaCellRecord.getMTimeStamp();
        if (mTimeStamp != null) {
            statement.bindLong(2, mTimeStamp.getTime());
        } else {
            statement.bindNull(2);
        }
        Integer mCellID1 = metaCellRecord.getMCellID1();
        if (mCellID1 != null) {
            statement.bindLong(3, (long) mCellID1.intValue());
        } else {
            statement.bindNull(3);
        }
        Integer mCellMCC1 = metaCellRecord.getMCellMCC1();
        if (mCellMCC1 != null) {
            statement.bindLong(4, (long) mCellMCC1.intValue());
        } else {
            statement.bindNull(4);
        }
        Integer mCellMNC1 = metaCellRecord.getMCellMNC1();
        if (mCellMNC1 != null) {
            statement.bindLong(5, (long) mCellMNC1.intValue());
        } else {
            statement.bindNull(5);
        }
        Integer mCellLAC1 = metaCellRecord.getMCellLAC1();
        if (mCellLAC1 != null) {
            statement.bindLong(6, (long) mCellLAC1.intValue());
        } else {
            statement.bindNull(6);
        }
        Integer mCellRSSI1 = metaCellRecord.getMCellRSSI1();
        if (mCellRSSI1 != null) {
            statement.bindLong(7, (long) mCellRSSI1.intValue());
        } else {
            statement.bindNull(7);
        }
        Integer mCellID2 = metaCellRecord.getMCellID2();
        if (mCellID2 != null) {
            statement.bindLong(8, (long) mCellID2.intValue());
        } else {
            statement.bindNull(8);
        }
        Integer mCellMCC2 = metaCellRecord.getMCellMCC2();
        if (mCellMCC2 != null) {
            statement.bindLong(9, (long) mCellMCC2.intValue());
        } else {
            statement.bindNull(9);
        }
        Integer mCellMNC2 = metaCellRecord.getMCellMNC2();
        if (mCellMNC2 != null) {
            statement.bindLong(10, (long) mCellMNC2.intValue());
        } else {
            statement.bindNull(10);
        }
        Integer mCellLAC2 = metaCellRecord.getMCellLAC2();
        if (mCellLAC2 != null) {
            statement.bindLong(11, (long) mCellLAC2.intValue());
        } else {
            statement.bindNull(11);
        }
        Integer mCellRSSI2 = metaCellRecord.getMCellRSSI2();
        if (mCellRSSI2 != null) {
            statement.bindLong(12, (long) mCellRSSI2.intValue());
        } else {
            statement.bindNull(12);
        }
        Integer mCellID3 = metaCellRecord.getMCellID3();
        if (mCellID3 != null) {
            statement.bindLong(13, (long) mCellID3.intValue());
        } else {
            statement.bindNull(13);
        }
        Integer mCellMCC3 = metaCellRecord.getMCellMCC3();
        if (mCellMCC3 != null) {
            statement.bindLong(14, (long) mCellMCC3.intValue());
        } else {
            statement.bindNull(14);
        }
        Integer mCellMNC3 = metaCellRecord.getMCellMNC3();
        if (mCellMNC3 != null) {
            statement.bindLong(15, (long) mCellMNC3.intValue());
        } else {
            statement.bindNull(15);
        }
        Integer mCellLAC3 = metaCellRecord.getMCellLAC3();
        if (mCellLAC3 != null) {
            statement.bindLong(16, (long) mCellLAC3.intValue());
        } else {
            statement.bindNull(16);
        }
        Integer mCellRSSI3 = metaCellRecord.getMCellRSSI3();
        if (mCellRSSI3 != null) {
            statement.bindLong(17, (long) mCellRSSI3.intValue());
        } else {
            statement.bindNull(17);
        }
        Integer mReservedInt = metaCellRecord.getMReservedInt();
        if (mReservedInt != null) {
            statement.bindLong(18, (long) mReservedInt.intValue());
        } else {
            statement.bindNull(18);
        }
        String mReservedText = metaCellRecord.getMReservedText();
        if (mReservedText != null) {
            statement.bindString(19, mReservedText);
        } else {
            statement.bindNull(19);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public MetaCellRecord readObject(Cursor cursor, int i) {
        return new MetaCellRecord(cursor);
    }

    public void setPrimaryKeyValue(MetaCellRecord metaCellRecord, long j) {
        metaCellRecord.setMId(Integer.valueOf((int) j));
    }
}
