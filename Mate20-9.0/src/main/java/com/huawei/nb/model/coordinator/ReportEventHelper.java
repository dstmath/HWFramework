package com.huawei.nb.model.coordinator;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class ReportEventHelper extends AEntityHelper<ReportEvent> {
    private static final ReportEventHelper INSTANCE = new ReportEventHelper();

    private ReportEventHelper() {
    }

    public static ReportEventHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, ReportEvent object) {
        Integer eventNo = object.getEventNo();
        if (eventNo != null) {
            statement.bindLong(1, (long) eventNo.intValue());
        } else {
            statement.bindNull(1);
        }
        String id = object.getId();
        if (id != null) {
            statement.bindString(2, id);
        } else {
            statement.bindNull(2);
        }
        String type = object.getType();
        if (type != null) {
            statement.bindString(3, type);
        } else {
            statement.bindNull(3);
        }
        String params = object.getParams();
        if (params != null) {
            statement.bindString(4, params);
        } else {
            statement.bindNull(4);
        }
    }

    public ReportEvent readObject(Cursor cursor, int offset) {
        return new ReportEvent(cursor);
    }

    public void setPrimaryKeyValue(ReportEvent object, long value) {
        object.setEventNo(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, ReportEvent object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
