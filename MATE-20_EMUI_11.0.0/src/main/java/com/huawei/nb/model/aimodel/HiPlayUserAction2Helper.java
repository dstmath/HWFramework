package com.huawei.nb.model.aimodel;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class HiPlayUserAction2Helper extends AEntityHelper<HiPlayUserAction2> {
    private static final HiPlayUserAction2Helper INSTANCE = new HiPlayUserAction2Helper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, HiPlayUserAction2 hiPlayUserAction2) {
        return null;
    }

    private HiPlayUserAction2Helper() {
    }

    public static HiPlayUserAction2Helper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, HiPlayUserAction2 hiPlayUserAction2) {
        Long id = hiPlayUserAction2.getId();
        if (id != null) {
            statement.bindLong(1, id.longValue());
        } else {
            statement.bindNull(1);
        }
        String business = hiPlayUserAction2.getBusiness();
        if (business != null) {
            statement.bindString(2, business);
        } else {
            statement.bindNull(2);
        }
        String sub_business = hiPlayUserAction2.getSub_business();
        if (sub_business != null) {
            statement.bindString(3, sub_business);
        } else {
            statement.bindNull(3);
        }
        String content_main_type = hiPlayUserAction2.getContent_main_type();
        if (content_main_type != null) {
            statement.bindString(4, content_main_type);
        } else {
            statement.bindNull(4);
        }
        String device_id = hiPlayUserAction2.getDevice_id();
        if (device_id != null) {
            statement.bindString(5, device_id);
        } else {
            statement.bindNull(5);
        }
        String device_type = hiPlayUserAction2.getDevice_type();
        if (device_type != null) {
            statement.bindString(6, device_type);
        } else {
            statement.bindNull(6);
        }
        String reserver1 = hiPlayUserAction2.getReserver1();
        if (reserver1 != null) {
            statement.bindString(7, reserver1);
        } else {
            statement.bindNull(7);
        }
        String reserver2 = hiPlayUserAction2.getReserver2();
        if (reserver2 != null) {
            statement.bindString(8, reserver2);
        } else {
            statement.bindNull(8);
        }
        Long timestamp = hiPlayUserAction2.getTimestamp();
        if (timestamp != null) {
            statement.bindLong(9, timestamp.longValue());
        } else {
            statement.bindNull(9);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public HiPlayUserAction2 readObject(Cursor cursor, int i) {
        return new HiPlayUserAction2(cursor);
    }

    public void setPrimaryKeyValue(HiPlayUserAction2 hiPlayUserAction2, long j) {
        hiPlayUserAction2.setId(Long.valueOf(j));
    }
}
