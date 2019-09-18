package com.huawei.nb.model.ips;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import com.huawei.odmf.utils.BindUtils;
import java.sql.Blob;

public class IndoorDicHelper extends AEntityHelper<IndoorDic> {
    private static final IndoorDicHelper INSTANCE = new IndoorDicHelper();

    private IndoorDicHelper() {
    }

    public static IndoorDicHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, IndoorDic object) {
        Integer id = object.getId();
        if (id != null) {
            statement.bindLong(1, (long) id.intValue());
        } else {
            statement.bindNull(1);
        }
        String venueId = object.getVenueId();
        if (venueId != null) {
            statement.bindString(2, venueId);
        } else {
            statement.bindNull(2);
        }
        Integer type = object.getType();
        if (type != null) {
            statement.bindLong(3, (long) type.intValue());
        } else {
            statement.bindNull(3);
        }
        Blob key = object.getKey();
        if (key != null) {
            statement.bindBlob(4, BindUtils.bindBlob(key));
        } else {
            statement.bindNull(4);
        }
        Short value = object.getValue();
        if (value != null) {
            statement.bindLong(5, (long) value.shortValue());
        } else {
            statement.bindNull(5);
        }
        String reserved = object.getReserved();
        if (reserved != null) {
            statement.bindString(6, reserved);
        } else {
            statement.bindNull(6);
        }
    }

    public IndoorDic readObject(Cursor cursor, int offset) {
        return new IndoorDic(cursor);
    }

    public void setPrimaryKeyValue(IndoorDic object, long value) {
        object.setId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, IndoorDic object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
