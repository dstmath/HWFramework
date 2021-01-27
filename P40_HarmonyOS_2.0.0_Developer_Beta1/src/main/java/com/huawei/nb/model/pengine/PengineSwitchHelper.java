package com.huawei.nb.model.pengine;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class PengineSwitchHelper extends AEntityHelper<PengineSwitch> {
    private static final PengineSwitchHelper INSTANCE = new PengineSwitchHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, PengineSwitch pengineSwitch) {
        return null;
    }

    private PengineSwitchHelper() {
    }

    public static PengineSwitchHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, PengineSwitch pengineSwitch) {
        Integer id = pengineSwitch.getId();
        if (id != null) {
            statement.bindLong(1, (long) id.intValue());
        } else {
            statement.bindNull(1);
        }
        Integer dataType = pengineSwitch.getDataType();
        if (dataType != null) {
            statement.bindLong(2, (long) dataType.intValue());
        } else {
            statement.bindNull(2);
        }
        Long timestamp = pengineSwitch.getTimestamp();
        if (timestamp != null) {
            statement.bindLong(3, timestamp.longValue());
        } else {
            statement.bindNull(3);
        }
        String upProperty = pengineSwitch.getUpProperty();
        if (upProperty != null) {
            statement.bindString(4, upProperty);
        } else {
            statement.bindNull(4);
        }
        String column0 = pengineSwitch.getColumn0();
        if (column0 != null) {
            statement.bindString(5, column0);
        } else {
            statement.bindNull(5);
        }
        String column1 = pengineSwitch.getColumn1();
        if (column1 != null) {
            statement.bindString(6, column1);
        } else {
            statement.bindNull(6);
        }
        String column2 = pengineSwitch.getColumn2();
        if (column2 != null) {
            statement.bindString(7, column2);
        } else {
            statement.bindNull(7);
        }
        String column3 = pengineSwitch.getColumn3();
        if (column3 != null) {
            statement.bindString(8, column3);
        } else {
            statement.bindNull(8);
        }
        String column4 = pengineSwitch.getColumn4();
        if (column4 != null) {
            statement.bindString(9, column4);
        } else {
            statement.bindNull(9);
        }
        String column5 = pengineSwitch.getColumn5();
        if (column5 != null) {
            statement.bindString(10, column5);
        } else {
            statement.bindNull(10);
        }
        String column6 = pengineSwitch.getColumn6();
        if (column6 != null) {
            statement.bindString(11, column6);
        } else {
            statement.bindNull(11);
        }
        String column7 = pengineSwitch.getColumn7();
        if (column7 != null) {
            statement.bindString(12, column7);
        } else {
            statement.bindNull(12);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public PengineSwitch readObject(Cursor cursor, int i) {
        return new PengineSwitch(cursor);
    }

    public void setPrimaryKeyValue(PengineSwitch pengineSwitch, long j) {
        pengineSwitch.setId(Integer.valueOf((int) j));
    }
}
