package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class DSExpressHelper extends AEntityHelper<DSExpress> {
    private static final DSExpressHelper INSTANCE = new DSExpressHelper();

    private DSExpressHelper() {
    }

    public static DSExpressHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, DSExpress object) {
        Integer id = object.getId();
        if (id != null) {
            statement.bindLong(1, (long) id.intValue());
        } else {
            statement.bindNull(1);
        }
        String expressNumber = object.getExpressNumber();
        if (expressNumber != null) {
            statement.bindString(2, expressNumber);
        } else {
            statement.bindNull(2);
        }
        String expressCompany = object.getExpressCompany();
        if (expressCompany != null) {
            statement.bindString(3, expressCompany);
        } else {
            statement.bindNull(3);
        }
        String companyCode = object.getCompanyCode();
        if (companyCode != null) {
            statement.bindString(4, companyCode);
        } else {
            statement.bindNull(4);
        }
        String cabinetCompany = object.getCabinetCompany();
        if (cabinetCompany != null) {
            statement.bindString(5, cabinetCompany);
        } else {
            statement.bindNull(5);
        }
        Integer mState = object.getMState();
        if (mState != null) {
            statement.bindLong(6, (long) mState.intValue());
        } else {
            statement.bindNull(6);
        }
        Integer oldState = object.getOldState();
        if (oldState != null) {
            statement.bindLong(7, (long) oldState.intValue());
        } else {
            statement.bindNull(7);
        }
        String detail = object.getDetail();
        if (detail != null) {
            statement.bindString(8, detail);
        } else {
            statement.bindNull(8);
        }
        Long updateTime = object.getUpdateTime();
        if (updateTime != null) {
            statement.bindLong(9, updateTime.longValue());
        } else {
            statement.bindNull(9);
        }
        Long lastUpdateTime = object.getLastUpdateTime();
        if (lastUpdateTime != null) {
            statement.bindLong(10, lastUpdateTime.longValue());
        } else {
            statement.bindNull(10);
        }
        Long newestTime = object.getNewestTime();
        if (newestTime != null) {
            statement.bindLong(11, newestTime.longValue());
        } else {
            statement.bindNull(11);
        }
        String code = object.getCode();
        if (code != null) {
            statement.bindString(12, code);
        } else {
            statement.bindNull(12);
        }
        String cabinetLocation = object.getCabinetLocation();
        if (cabinetLocation != null) {
            statement.bindString(13, cabinetLocation);
        } else {
            statement.bindNull(13);
        }
        String latitude = object.getLatitude();
        if (latitude != null) {
            statement.bindString(14, latitude);
        } else {
            statement.bindNull(14);
        }
        String longitude = object.getLongitude();
        if (longitude != null) {
            statement.bindString(15, longitude);
        } else {
            statement.bindNull(15);
        }
        String appName = object.getAppName();
        if (appName != null) {
            statement.bindString(16, appName);
        } else {
            statement.bindNull(16);
        }
        String appPackage = object.getAppPackage();
        if (appPackage != null) {
            statement.bindString(17, appPackage);
        } else {
            statement.bindNull(17);
        }
        Integer source = object.getSource();
        if (source != null) {
            statement.bindLong(18, (long) source.intValue());
        } else {
            statement.bindNull(18);
        }
        String dataSource = object.getDataSource();
        if (dataSource != null) {
            statement.bindString(19, dataSource);
        } else {
            statement.bindNull(19);
        }
        String courierName = object.getCourierName();
        if (courierName != null) {
            statement.bindString(20, courierName);
        } else {
            statement.bindNull(20);
        }
        String courierPhone = object.getCourierPhone();
        if (courierPhone != null) {
            statement.bindString(21, courierPhone);
        } else {
            statement.bindNull(21);
        }
        Integer subscribeState = object.getSubscribeState();
        if (subscribeState != null) {
            statement.bindLong(22, (long) subscribeState.intValue());
        } else {
            statement.bindNull(22);
        }
        Long sendTime = object.getSendTime();
        if (sendTime != null) {
            statement.bindLong(23, sendTime.longValue());
        } else {
            statement.bindNull(23);
        }
        Long signTime = object.getSignTime();
        if (signTime != null) {
            statement.bindLong(24, signTime.longValue());
        } else {
            statement.bindNull(24);
        }
        String signPerson = object.getSignPerson();
        if (signPerson != null) {
            statement.bindString(25, signPerson);
        } else {
            statement.bindNull(25);
        }
        Integer expressFlow = object.getExpressFlow();
        if (expressFlow != null) {
            statement.bindLong(26, (long) expressFlow.intValue());
        } else {
            statement.bindNull(26);
        }
        String extras = object.getExtras();
        if (extras != null) {
            statement.bindString(27, extras);
        } else {
            statement.bindNull(27);
        }
        Long createTime = object.getCreateTime();
        if (createTime != null) {
            statement.bindLong(28, createTime.longValue());
        } else {
            statement.bindNull(28);
        }
        String expand = object.getExpand();
        if (expand != null) {
            statement.bindString(29, expand);
        } else {
            statement.bindNull(29);
        }
        Integer subWithImei = object.getSubWithImei();
        if (subWithImei != null) {
            statement.bindLong(30, (long) subWithImei.intValue());
        } else {
            statement.bindNull(30);
        }
        String reserved0 = object.getReserved0();
        if (reserved0 != null) {
            statement.bindString(31, reserved0);
        } else {
            statement.bindNull(31);
        }
        String reserved1 = object.getReserved1();
        if (reserved1 != null) {
            statement.bindString(32, reserved1);
        } else {
            statement.bindNull(32);
        }
        String reserved2 = object.getReserved2();
        if (reserved2 != null) {
            statement.bindString(33, reserved2);
        } else {
            statement.bindNull(33);
        }
        String reserved3 = object.getReserved3();
        if (reserved3 != null) {
            statement.bindString(34, reserved3);
        } else {
            statement.bindNull(34);
        }
        String reserved4 = object.getReserved4();
        if (reserved4 != null) {
            statement.bindString(35, reserved4);
        } else {
            statement.bindNull(35);
        }
        String reserved5 = object.getReserved5();
        if (reserved5 != null) {
            statement.bindString(36, reserved5);
        } else {
            statement.bindNull(36);
        }
    }

    public DSExpress readObject(Cursor cursor, int offset) {
        return new DSExpress(cursor);
    }

    public void setPrimaryKeyValue(DSExpress object, long value) {
        object.setId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, DSExpress object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
