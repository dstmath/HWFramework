package com.huawei.nb.model.policy;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class DisabledGeoFenceHelper extends AEntityHelper<DisabledGeoFence> {
    private static final DisabledGeoFenceHelper INSTANCE = new DisabledGeoFenceHelper();

    private DisabledGeoFenceHelper() {
    }

    public static DisabledGeoFenceHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, DisabledGeoFence object) {
        Long mID = object.getMID();
        if (mID != null) {
            statement.bindLong(1, mID.longValue());
        } else {
            statement.bindNull(1);
        }
        String mFenceID = object.getMFenceID();
        if (mFenceID != null) {
            statement.bindString(2, mFenceID);
        } else {
            statement.bindNull(2);
        }
        String mName = object.getMName();
        if (mName != null) {
            statement.bindString(3, mName);
        } else {
            statement.bindNull(3);
        }
    }

    public DisabledGeoFence readObject(Cursor cursor, int offset) {
        return new DisabledGeoFence(cursor);
    }

    public void setPrimaryKeyValue(DisabledGeoFence object, long value) {
        object.setMID(Long.valueOf(value));
    }

    public Object getRelationshipObject(String field, DisabledGeoFence object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
