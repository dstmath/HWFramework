package com.huawei.nb.model.ips;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import com.huawei.odmf.utils.BindUtils;
import java.sql.Blob;

public class IndoorDicHelper extends AEntityHelper<IndoorDic> {
    private static final IndoorDicHelper INSTANCE = new IndoorDicHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, IndoorDic indoorDic) {
        return null;
    }

    private IndoorDicHelper() {
    }

    public static IndoorDicHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, IndoorDic indoorDic) {
        Integer id = indoorDic.getId();
        if (id != null) {
            statement.bindLong(1, (long) id.intValue());
        } else {
            statement.bindNull(1);
        }
        String venueId = indoorDic.getVenueId();
        if (venueId != null) {
            statement.bindString(2, venueId);
        } else {
            statement.bindNull(2);
        }
        Integer type = indoorDic.getType();
        if (type != null) {
            statement.bindLong(3, (long) type.intValue());
        } else {
            statement.bindNull(3);
        }
        Blob key = indoorDic.getKey();
        if (key != null) {
            statement.bindBlob(4, BindUtils.bindBlob(key));
        } else {
            statement.bindNull(4);
        }
        Short value = indoorDic.getValue();
        if (value != null) {
            statement.bindLong(5, (long) value.shortValue());
        } else {
            statement.bindNull(5);
        }
        String reserved = indoorDic.getReserved();
        if (reserved != null) {
            statement.bindString(6, reserved);
        } else {
            statement.bindNull(6);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public IndoorDic readObject(Cursor cursor, int i) {
        return new IndoorDic(cursor);
    }

    public void setPrimaryKeyValue(IndoorDic indoorDic, long j) {
        indoorDic.setId(Integer.valueOf((int) j));
    }
}
