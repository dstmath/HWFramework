package com.huawei.nb.model.coordinator;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class PushMsgControlHelper extends AEntityHelper<PushMsgControl> {
    private static final PushMsgControlHelper INSTANCE = new PushMsgControlHelper();

    private PushMsgControlHelper() {
    }

    public static PushMsgControlHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, PushMsgControl object) {
        Long id = object.getId();
        if (id != null) {
            statement.bindLong(1, id.longValue());
        } else {
            statement.bindNull(1);
        }
        String msgType = object.getMsgType();
        if (msgType != null) {
            statement.bindString(2, msgType);
        } else {
            statement.bindNull(2);
        }
        Integer presetCount = object.getPresetCount();
        if (presetCount != null) {
            statement.bindLong(3, (long) presetCount.intValue());
        } else {
            statement.bindNull(3);
        }
        Integer count = object.getCount();
        if (count != null) {
            statement.bindLong(4, (long) count.intValue());
        } else {
            statement.bindNull(4);
        }
        Integer maxReportInterval = object.getMaxReportInterval();
        if (maxReportInterval != null) {
            statement.bindLong(5, (long) maxReportInterval.intValue());
        } else {
            statement.bindNull(5);
        }
        String updateTime = object.getUpdateTime();
        if (updateTime != null) {
            statement.bindString(6, updateTime);
        } else {
            statement.bindNull(6);
        }
    }

    public PushMsgControl readObject(Cursor cursor, int offset) {
        return new PushMsgControl(cursor);
    }

    public void setPrimaryKeyValue(PushMsgControl object, long value) {
        object.setId(Long.valueOf(value));
    }

    public Object getRelationshipObject(String field, PushMsgControl object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
