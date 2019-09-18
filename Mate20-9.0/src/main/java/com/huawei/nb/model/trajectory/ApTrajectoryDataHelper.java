package com.huawei.nb.model.trajectory;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class ApTrajectoryDataHelper extends AEntityHelper<ApTrajectoryData> {
    private static final ApTrajectoryDataHelper INSTANCE = new ApTrajectoryDataHelper();

    private ApTrajectoryDataHelper() {
    }

    public static ApTrajectoryDataHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, ApTrajectoryData object) {
        Integer id = object.getId();
        if (id != null) {
            statement.bindLong(1, (long) id.intValue());
        } else {
            statement.bindNull(1);
        }
        Date timeStamp = object.getTimeStamp();
        if (timeStamp != null) {
            statement.bindLong(2, timeStamp.getTime());
        } else {
            statement.bindNull(2);
        }
        Character loncationType = object.getLoncationType();
        if (loncationType != null) {
            statement.bindString(3, String.valueOf(loncationType));
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
        Integer cellId = object.getCellId();
        if (cellId != null) {
            statement.bindLong(6, (long) cellId.intValue());
        } else {
            statement.bindNull(6);
        }
        Integer cellLac = object.getCellLac();
        if (cellLac != null) {
            statement.bindLong(7, (long) cellLac.intValue());
        } else {
            statement.bindNull(7);
        }
        Integer cellRssi = object.getCellRssi();
        if (cellRssi != null) {
            statement.bindLong(8, (long) cellRssi.intValue());
        } else {
            statement.bindNull(8);
        }
        String wifiBssId = object.getWifiBssId();
        if (wifiBssId != null) {
            statement.bindString(9, wifiBssId);
        } else {
            statement.bindNull(9);
        }
        Integer wifiRssi = object.getWifiRssi();
        if (wifiRssi != null) {
            statement.bindLong(10, (long) wifiRssi.intValue());
        } else {
            statement.bindNull(10);
        }
        Integer reserved1 = object.getReserved1();
        if (reserved1 != null) {
            statement.bindLong(11, (long) reserved1.intValue());
        } else {
            statement.bindNull(11);
        }
        String reserved2 = object.getReserved2();
        if (reserved2 != null) {
            statement.bindString(12, reserved2);
        } else {
            statement.bindNull(12);
        }
    }

    public ApTrajectoryData readObject(Cursor cursor, int offset) {
        return new ApTrajectoryData(cursor);
    }

    public void setPrimaryKeyValue(ApTrajectoryData object, long value) {
        object.setId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, ApTrajectoryData object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
