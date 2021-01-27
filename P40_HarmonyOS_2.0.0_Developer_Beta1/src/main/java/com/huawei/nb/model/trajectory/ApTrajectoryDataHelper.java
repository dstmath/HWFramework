package com.huawei.nb.model.trajectory;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class ApTrajectoryDataHelper extends AEntityHelper<ApTrajectoryData> {
    private static final ApTrajectoryDataHelper INSTANCE = new ApTrajectoryDataHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, ApTrajectoryData apTrajectoryData) {
        return null;
    }

    private ApTrajectoryDataHelper() {
    }

    public static ApTrajectoryDataHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, ApTrajectoryData apTrajectoryData) {
        Integer id = apTrajectoryData.getId();
        if (id != null) {
            statement.bindLong(1, (long) id.intValue());
        } else {
            statement.bindNull(1);
        }
        Date timeStamp = apTrajectoryData.getTimeStamp();
        if (timeStamp != null) {
            statement.bindLong(2, timeStamp.getTime());
        } else {
            statement.bindNull(2);
        }
        Character loncationType = apTrajectoryData.getLoncationType();
        if (loncationType != null) {
            statement.bindString(3, String.valueOf(loncationType));
        } else {
            statement.bindNull(3);
        }
        Double longitude = apTrajectoryData.getLongitude();
        if (longitude != null) {
            statement.bindDouble(4, longitude.doubleValue());
        } else {
            statement.bindNull(4);
        }
        Double latitude = apTrajectoryData.getLatitude();
        if (latitude != null) {
            statement.bindDouble(5, latitude.doubleValue());
        } else {
            statement.bindNull(5);
        }
        Integer cellId = apTrajectoryData.getCellId();
        if (cellId != null) {
            statement.bindLong(6, (long) cellId.intValue());
        } else {
            statement.bindNull(6);
        }
        Integer cellLac = apTrajectoryData.getCellLac();
        if (cellLac != null) {
            statement.bindLong(7, (long) cellLac.intValue());
        } else {
            statement.bindNull(7);
        }
        Integer cellRssi = apTrajectoryData.getCellRssi();
        if (cellRssi != null) {
            statement.bindLong(8, (long) cellRssi.intValue());
        } else {
            statement.bindNull(8);
        }
        String wifiBssId = apTrajectoryData.getWifiBssId();
        if (wifiBssId != null) {
            statement.bindString(9, wifiBssId);
        } else {
            statement.bindNull(9);
        }
        Integer wifiRssi = apTrajectoryData.getWifiRssi();
        if (wifiRssi != null) {
            statement.bindLong(10, (long) wifiRssi.intValue());
        } else {
            statement.bindNull(10);
        }
        Integer reserved1 = apTrajectoryData.getReserved1();
        if (reserved1 != null) {
            statement.bindLong(11, (long) reserved1.intValue());
        } else {
            statement.bindNull(11);
        }
        String reserved2 = apTrajectoryData.getReserved2();
        if (reserved2 != null) {
            statement.bindString(12, reserved2);
        } else {
            statement.bindNull(12);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public ApTrajectoryData readObject(Cursor cursor, int i) {
        return new ApTrajectoryData(cursor);
    }

    public void setPrimaryKeyValue(ApTrajectoryData apTrajectoryData, long j) {
        apTrajectoryData.setId(Integer.valueOf((int) j));
    }
}
