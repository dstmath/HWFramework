package com.huawei.nb.model.pengine;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class FilterListHelper extends AEntityHelper<FilterList> {
    private static final FilterListHelper INSTANCE = new FilterListHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, FilterList filterList) {
        return null;
    }

    private FilterListHelper() {
    }

    public static FilterListHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, FilterList filterList) {
        Integer id = filterList.getId();
        if (id != null) {
            statement.bindLong(1, (long) id.intValue());
        } else {
            statement.bindNull(1);
        }
        Long dataSerialNumber = filterList.getDataSerialNumber();
        if (dataSerialNumber != null) {
            statement.bindLong(2, dataSerialNumber.longValue());
        } else {
            statement.bindNull(2);
        }
        String packageName = filterList.getPackageName();
        if (packageName != null) {
            statement.bindString(3, packageName);
        } else {
            statement.bindNull(3);
        }
        Long timestamp = filterList.getTimestamp();
        if (timestamp != null) {
            statement.bindLong(4, timestamp.longValue());
        } else {
            statement.bindNull(4);
        }
        String column0 = filterList.getColumn0();
        if (column0 != null) {
            statement.bindString(5, column0);
        } else {
            statement.bindNull(5);
        }
        String column1 = filterList.getColumn1();
        if (column1 != null) {
            statement.bindString(6, column1);
        } else {
            statement.bindNull(6);
        }
        String column2 = filterList.getColumn2();
        if (column2 != null) {
            statement.bindString(7, column2);
        } else {
            statement.bindNull(7);
        }
        String column3 = filterList.getColumn3();
        if (column3 != null) {
            statement.bindString(8, column3);
        } else {
            statement.bindNull(8);
        }
        String column4 = filterList.getColumn4();
        if (column4 != null) {
            statement.bindString(9, column4);
        } else {
            statement.bindNull(9);
        }
        String column5 = filterList.getColumn5();
        if (column5 != null) {
            statement.bindString(10, column5);
        } else {
            statement.bindNull(10);
        }
        String column6 = filterList.getColumn6();
        if (column6 != null) {
            statement.bindString(11, column6);
        } else {
            statement.bindNull(11);
        }
        String column7 = filterList.getColumn7();
        if (column7 != null) {
            statement.bindString(12, column7);
        } else {
            statement.bindNull(12);
        }
        String column8 = filterList.getColumn8();
        if (column8 != null) {
            statement.bindString(13, column8);
        } else {
            statement.bindNull(13);
        }
        String column9 = filterList.getColumn9();
        if (column9 != null) {
            statement.bindString(14, column9);
        } else {
            statement.bindNull(14);
        }
        String column10 = filterList.getColumn10();
        if (column10 != null) {
            statement.bindString(15, column10);
        } else {
            statement.bindNull(15);
        }
        String column11 = filterList.getColumn11();
        if (column11 != null) {
            statement.bindString(16, column11);
        } else {
            statement.bindNull(16);
        }
        String column12 = filterList.getColumn12();
        if (column12 != null) {
            statement.bindString(17, column12);
        } else {
            statement.bindNull(17);
        }
        String column13 = filterList.getColumn13();
        if (column13 != null) {
            statement.bindString(18, column13);
        } else {
            statement.bindNull(18);
        }
        String column14 = filterList.getColumn14();
        if (column14 != null) {
            statement.bindString(19, column14);
        } else {
            statement.bindNull(19);
        }
        String column15 = filterList.getColumn15();
        if (column15 != null) {
            statement.bindString(20, column15);
        } else {
            statement.bindNull(20);
        }
        String column16 = filterList.getColumn16();
        if (column16 != null) {
            statement.bindString(21, column16);
        } else {
            statement.bindNull(21);
        }
        String column17 = filterList.getColumn17();
        if (column17 != null) {
            statement.bindString(22, column17);
        } else {
            statement.bindNull(22);
        }
        String column18 = filterList.getColumn18();
        if (column18 != null) {
            statement.bindString(23, column18);
        } else {
            statement.bindNull(23);
        }
        String column19 = filterList.getColumn19();
        if (column19 != null) {
            statement.bindString(24, column19);
        } else {
            statement.bindNull(24);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public FilterList readObject(Cursor cursor, int i) {
        return new FilterList(cursor);
    }

    public void setPrimaryKeyValue(FilterList filterList, long j) {
        filterList.setId(Integer.valueOf((int) j));
    }
}
