package com.huawei.nb.model.pengine;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class ClusterLocCacheHelper extends AEntityHelper<ClusterLocCache> {
    private static final ClusterLocCacheHelper INSTANCE = new ClusterLocCacheHelper();

    private ClusterLocCacheHelper() {
    }

    public static ClusterLocCacheHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, ClusterLocCache object) {
        Integer id = object.getId();
        if (id != null) {
            statement.bindLong(1, (long) id.intValue());
        } else {
            statement.bindNull(1);
        }
        Integer dataType = object.getDataType();
        if (dataType != null) {
            statement.bindLong(2, (long) dataType.intValue());
        } else {
            statement.bindNull(2);
        }
        Long timestamp = object.getTimestamp();
        if (timestamp != null) {
            statement.bindLong(3, timestamp.longValue());
        } else {
            statement.bindNull(3);
        }
        String column0 = object.getColumn0();
        if (column0 != null) {
            statement.bindString(4, column0);
        } else {
            statement.bindNull(4);
        }
        String column1 = object.getColumn1();
        if (column1 != null) {
            statement.bindString(5, column1);
        } else {
            statement.bindNull(5);
        }
        String column2 = object.getColumn2();
        if (column2 != null) {
            statement.bindString(6, column2);
        } else {
            statement.bindNull(6);
        }
        String column3 = object.getColumn3();
        if (column3 != null) {
            statement.bindString(7, column3);
        } else {
            statement.bindNull(7);
        }
        String column4 = object.getColumn4();
        if (column4 != null) {
            statement.bindString(8, column4);
        } else {
            statement.bindNull(8);
        }
        String column5 = object.getColumn5();
        if (column5 != null) {
            statement.bindString(9, column5);
        } else {
            statement.bindNull(9);
        }
        String column6 = object.getColumn6();
        if (column6 != null) {
            statement.bindString(10, column6);
        } else {
            statement.bindNull(10);
        }
        String column7 = object.getColumn7();
        if (column7 != null) {
            statement.bindString(11, column7);
        } else {
            statement.bindNull(11);
        }
        String column8 = object.getColumn8();
        if (column8 != null) {
            statement.bindString(12, column8);
        } else {
            statement.bindNull(12);
        }
        String column9 = object.getColumn9();
        if (column9 != null) {
            statement.bindString(13, column9);
        } else {
            statement.bindNull(13);
        }
        String column10 = object.getColumn10();
        if (column10 != null) {
            statement.bindString(14, column10);
        } else {
            statement.bindNull(14);
        }
        String column11 = object.getColumn11();
        if (column11 != null) {
            statement.bindString(15, column11);
        } else {
            statement.bindNull(15);
        }
        String column12 = object.getColumn12();
        if (column12 != null) {
            statement.bindString(16, column12);
        } else {
            statement.bindNull(16);
        }
        String column13 = object.getColumn13();
        if (column13 != null) {
            statement.bindString(17, column13);
        } else {
            statement.bindNull(17);
        }
        String column14 = object.getColumn14();
        if (column14 != null) {
            statement.bindString(18, column14);
        } else {
            statement.bindNull(18);
        }
        String column15 = object.getColumn15();
        if (column15 != null) {
            statement.bindString(19, column15);
        } else {
            statement.bindNull(19);
        }
        String column16 = object.getColumn16();
        if (column16 != null) {
            statement.bindString(20, column16);
        } else {
            statement.bindNull(20);
        }
        String column17 = object.getColumn17();
        if (column17 != null) {
            statement.bindString(21, column17);
        } else {
            statement.bindNull(21);
        }
        String column18 = object.getColumn18();
        if (column18 != null) {
            statement.bindString(22, column18);
        } else {
            statement.bindNull(22);
        }
        String column19 = object.getColumn19();
        if (column19 != null) {
            statement.bindString(23, column19);
        } else {
            statement.bindNull(23);
        }
    }

    public ClusterLocCache readObject(Cursor cursor, int offset) {
        return new ClusterLocCache(cursor);
    }

    public void setPrimaryKeyValue(ClusterLocCache object, long value) {
        object.setId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, ClusterLocCache object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
