package com.huawei.nb.model.policy;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class DisabledGeoFenceHelper extends AEntityHelper<DisabledGeoFence> {
    private static final DisabledGeoFenceHelper INSTANCE = new DisabledGeoFenceHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, DisabledGeoFence disabledGeoFence) {
        return null;
    }

    private DisabledGeoFenceHelper() {
    }

    public static DisabledGeoFenceHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, DisabledGeoFence disabledGeoFence) {
        Long mid = disabledGeoFence.getMID();
        if (mid != null) {
            statement.bindLong(1, mid.longValue());
        } else {
            statement.bindNull(1);
        }
        String mFenceID = disabledGeoFence.getMFenceID();
        if (mFenceID != null) {
            statement.bindString(2, mFenceID);
        } else {
            statement.bindNull(2);
        }
        String mName = disabledGeoFence.getMName();
        if (mName != null) {
            statement.bindString(3, mName);
        } else {
            statement.bindNull(3);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public DisabledGeoFence readObject(Cursor cursor, int i) {
        return new DisabledGeoFence(cursor);
    }

    public void setPrimaryKeyValue(DisabledGeoFence disabledGeoFence, long j) {
        disabledGeoFence.setMID(Long.valueOf(j));
    }
}
