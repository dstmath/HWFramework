package com.huawei.nb.model.ips;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import com.huawei.odmf.utils.BindUtils;
import java.sql.Blob;

public class IndoorDotSetHelper extends AEntityHelper<IndoorDotSet> {
    private static final IndoorDotSetHelper INSTANCE = new IndoorDotSetHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, IndoorDotSet indoorDotSet) {
        return null;
    }

    private IndoorDotSetHelper() {
    }

    public static IndoorDotSetHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, IndoorDotSet indoorDotSet) {
        Integer id = indoorDotSet.getId();
        if (id != null) {
            statement.bindLong(1, (long) id.intValue());
        } else {
            statement.bindNull(1);
        }
        String venueId = indoorDotSet.getVenueId();
        if (venueId != null) {
            statement.bindString(2, venueId);
        } else {
            statement.bindNull(2);
        }
        Short floorNum = indoorDotSet.getFloorNum();
        if (floorNum != null) {
            statement.bindLong(3, (long) floorNum.shortValue());
        } else {
            statement.bindNull(3);
        }
        Double longitude = indoorDotSet.getLongitude();
        if (longitude != null) {
            statement.bindDouble(4, longitude.doubleValue());
        } else {
            statement.bindNull(4);
        }
        Double latitude = indoorDotSet.getLatitude();
        if (latitude != null) {
            statement.bindDouble(5, latitude.doubleValue());
        } else {
            statement.bindNull(5);
        }
        Integer dataType = indoorDotSet.getDataType();
        if (dataType != null) {
            statement.bindLong(6, (long) dataType.intValue());
        } else {
            statement.bindNull(6);
        }
        Blob data = indoorDotSet.getData();
        if (data != null) {
            statement.bindBlob(7, BindUtils.bindBlob(data));
        } else {
            statement.bindNull(7);
        }
        String reserved = indoorDotSet.getReserved();
        if (reserved != null) {
            statement.bindString(8, reserved);
        } else {
            statement.bindNull(8);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public IndoorDotSet readObject(Cursor cursor, int i) {
        return new IndoorDotSet(cursor);
    }

    public void setPrimaryKeyValue(IndoorDotSet indoorDotSet, long j) {
        indoorDotSet.setId(Integer.valueOf((int) j));
    }
}
