package com.huawei.nb.model.ips;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class IndoorVenueHelper extends AEntityHelper<IndoorVenue> {
    private static final IndoorVenueHelper INSTANCE = new IndoorVenueHelper();

    private IndoorVenueHelper() {
    }

    public static IndoorVenueHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, IndoorVenue object) {
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
        String blockId = object.getBlockId();
        if (blockId != null) {
            statement.bindString(3, blockId);
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
        String reserved = object.getReserved();
        if (reserved != null) {
            statement.bindString(6, reserved);
        } else {
            statement.bindNull(6);
        }
    }

    public IndoorVenue readObject(Cursor cursor, int offset) {
        return new IndoorVenue(cursor);
    }

    public void setPrimaryKeyValue(IndoorVenue object, long value) {
        object.setId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, IndoorVenue object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
