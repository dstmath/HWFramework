package com.huawei.nb.model.ips;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import com.huawei.odmf.utils.BindUtils;
import java.sql.Blob;

public class IndoorDotSetHelper extends AEntityHelper<IndoorDotSet> {
    private static final IndoorDotSetHelper INSTANCE = new IndoorDotSetHelper();

    private IndoorDotSetHelper() {
    }

    public static IndoorDotSetHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, IndoorDotSet object) {
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
        Short floorNum = object.getFloorNum();
        if (floorNum != null) {
            statement.bindLong(3, (long) floorNum.shortValue());
        } else {
            statement.bindNull(3);
        }
        Double longitude = object.getLongitude();
        if (longitude != null) {
            statement.bindDouble(4, longitude.doubleValue());
        } else {
            statement.bindNull(4);
        }
        Double latitude = object.getLatitude();
        if (latitude != null) {
            statement.bindDouble(5, latitude.doubleValue());
        } else {
            statement.bindNull(5);
        }
        Integer dataType = object.getDataType();
        if (dataType != null) {
            statement.bindLong(6, (long) dataType.intValue());
        } else {
            statement.bindNull(6);
        }
        Blob data = object.getData();
        if (data != null) {
            statement.bindBlob(7, BindUtils.bindBlob(data));
        } else {
            statement.bindNull(7);
        }
        String reserved = object.getReserved();
        if (reserved != null) {
            statement.bindString(8, reserved);
        } else {
            statement.bindNull(8);
        }
    }

    public IndoorDotSet readObject(Cursor cursor, int offset) {
        return new IndoorDotSet(cursor);
    }

    public void setPrimaryKeyValue(IndoorDotSet object, long value) {
        object.setId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, IndoorDotSet object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
