package com.huawei.nb.model.ips;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class IndoorVenueHelper extends AEntityHelper<IndoorVenue> {
    private static final IndoorVenueHelper INSTANCE = new IndoorVenueHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, IndoorVenue indoorVenue) {
        return null;
    }

    private IndoorVenueHelper() {
    }

    public static IndoorVenueHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, IndoorVenue indoorVenue) {
        Integer id = indoorVenue.getId();
        if (id != null) {
            statement.bindLong(1, (long) id.intValue());
        } else {
            statement.bindNull(1);
        }
        String venueId = indoorVenue.getVenueId();
        if (venueId != null) {
            statement.bindString(2, venueId);
        } else {
            statement.bindNull(2);
        }
        String blockId = indoorVenue.getBlockId();
        if (blockId != null) {
            statement.bindString(3, blockId);
        } else {
            statement.bindNull(3);
        }
        Double longitude = indoorVenue.getLongitude();
        if (longitude != null) {
            statement.bindDouble(4, longitude.doubleValue());
        } else {
            statement.bindNull(4);
        }
        Double latitude = indoorVenue.getLatitude();
        if (latitude != null) {
            statement.bindDouble(5, latitude.doubleValue());
        } else {
            statement.bindNull(5);
        }
        String reserved = indoorVenue.getReserved();
        if (reserved != null) {
            statement.bindString(6, reserved);
        } else {
            statement.bindNull(6);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public IndoorVenue readObject(Cursor cursor, int i) {
        return new IndoorVenue(cursor);
    }

    public void setPrimaryKeyValue(IndoorVenue indoorVenue, long j) {
        indoorVenue.setId(Integer.valueOf((int) j));
    }
}
