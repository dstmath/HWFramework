package com.huawei.nb.model.ips;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import com.huawei.odmf.utils.BindUtils;
import java.sql.Blob;

public class IndoorTrackHelper extends AEntityHelper<IndoorTrack> {
    private static final IndoorTrackHelper INSTANCE = new IndoorTrackHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, IndoorTrack indoorTrack) {
        return null;
    }

    private IndoorTrackHelper() {
    }

    public static IndoorTrackHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, IndoorTrack indoorTrack) {
        Integer id = indoorTrack.getId();
        if (id != null) {
            statement.bindLong(1, (long) id.intValue());
        } else {
            statement.bindNull(1);
        }
        String areaCode = indoorTrack.getAreaCode();
        if (areaCode != null) {
            statement.bindString(2, areaCode);
        } else {
            statement.bindNull(2);
        }
        Integer type = indoorTrack.getType();
        if (type != null) {
            statement.bindLong(3, (long) type.intValue());
        } else {
            statement.bindNull(3);
        }
        Short floorNum = indoorTrack.getFloorNum();
        if (floorNum != null) {
            statement.bindLong(4, (long) floorNum.shortValue());
        } else {
            statement.bindNull(4);
        }
        Double longitude = indoorTrack.getLongitude();
        if (longitude != null) {
            statement.bindDouble(5, longitude.doubleValue());
        } else {
            statement.bindNull(5);
        }
        Double latitude = indoorTrack.getLatitude();
        if (latitude != null) {
            statement.bindDouble(6, latitude.doubleValue());
        } else {
            statement.bindNull(6);
        }
        Blob dataAdd = indoorTrack.getDataAdd();
        if (dataAdd != null) {
            statement.bindBlob(7, BindUtils.bindBlob(dataAdd));
        } else {
            statement.bindNull(7);
        }
        Blob dataDel = indoorTrack.getDataDel();
        if (dataDel != null) {
            statement.bindBlob(8, BindUtils.bindBlob(dataDel));
        } else {
            statement.bindNull(8);
        }
        Blob dataUpdate = indoorTrack.getDataUpdate();
        if (dataUpdate != null) {
            statement.bindBlob(9, BindUtils.bindBlob(dataUpdate));
        } else {
            statement.bindNull(9);
        }
        Long timestamp = indoorTrack.getTimestamp();
        if (timestamp != null) {
            statement.bindLong(10, timestamp.longValue());
        } else {
            statement.bindNull(10);
        }
        String reserved = indoorTrack.getReserved();
        if (reserved != null) {
            statement.bindString(11, reserved);
        } else {
            statement.bindNull(11);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public IndoorTrack readObject(Cursor cursor, int i) {
        return new IndoorTrack(cursor);
    }

    public void setPrimaryKeyValue(IndoorTrack indoorTrack, long j) {
        indoorTrack.setId(Integer.valueOf((int) j));
    }
}
