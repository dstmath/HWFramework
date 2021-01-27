package com.huawei.nb.model.coordinator;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class PushMsgControlHelper extends AEntityHelper<PushMsgControl> {
    private static final PushMsgControlHelper INSTANCE = new PushMsgControlHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, PushMsgControl pushMsgControl) {
        return null;
    }

    private PushMsgControlHelper() {
    }

    public static PushMsgControlHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, PushMsgControl pushMsgControl) {
        Long id = pushMsgControl.getId();
        if (id != null) {
            statement.bindLong(1, id.longValue());
        } else {
            statement.bindNull(1);
        }
        String msgType = pushMsgControl.getMsgType();
        if (msgType != null) {
            statement.bindString(2, msgType);
        } else {
            statement.bindNull(2);
        }
        Integer presetCount = pushMsgControl.getPresetCount();
        if (presetCount != null) {
            statement.bindLong(3, (long) presetCount.intValue());
        } else {
            statement.bindNull(3);
        }
        Integer count = pushMsgControl.getCount();
        if (count != null) {
            statement.bindLong(4, (long) count.intValue());
        } else {
            statement.bindNull(4);
        }
        Integer maxReportInterval = pushMsgControl.getMaxReportInterval();
        if (maxReportInterval != null) {
            statement.bindLong(5, (long) maxReportInterval.intValue());
        } else {
            statement.bindNull(5);
        }
        String updateTime = pushMsgControl.getUpdateTime();
        if (updateTime != null) {
            statement.bindString(6, updateTime);
        } else {
            statement.bindNull(6);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public PushMsgControl readObject(Cursor cursor, int i) {
        return new PushMsgControl(cursor);
    }

    public void setPrimaryKeyValue(PushMsgControl pushMsgControl, long j) {
        pushMsgControl.setId(Long.valueOf(j));
    }
}
