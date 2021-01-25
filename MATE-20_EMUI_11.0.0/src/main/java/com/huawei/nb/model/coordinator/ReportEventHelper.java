package com.huawei.nb.model.coordinator;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class ReportEventHelper extends AEntityHelper<ReportEvent> {
    private static final ReportEventHelper INSTANCE = new ReportEventHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, ReportEvent reportEvent) {
        return null;
    }

    private ReportEventHelper() {
    }

    public static ReportEventHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, ReportEvent reportEvent) {
        Integer eventNo = reportEvent.getEventNo();
        if (eventNo != null) {
            statement.bindLong(1, (long) eventNo.intValue());
        } else {
            statement.bindNull(1);
        }
        String id = reportEvent.getId();
        if (id != null) {
            statement.bindString(2, id);
        } else {
            statement.bindNull(2);
        }
        String type = reportEvent.getType();
        if (type != null) {
            statement.bindString(3, type);
        } else {
            statement.bindNull(3);
        }
        String params = reportEvent.getParams();
        if (params != null) {
            statement.bindString(4, params);
        } else {
            statement.bindNull(4);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public ReportEvent readObject(Cursor cursor, int i) {
        return new ReportEvent(cursor);
    }

    public void setPrimaryKeyValue(ReportEvent reportEvent, long j) {
        reportEvent.setEventNo(Integer.valueOf((int) j));
    }
}
