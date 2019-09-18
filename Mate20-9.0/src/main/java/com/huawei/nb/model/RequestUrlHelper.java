package com.huawei.nb.model;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class RequestUrlHelper extends AEntityHelper<RequestUrl> {
    private static final RequestUrlHelper INSTANCE = new RequestUrlHelper();

    private RequestUrlHelper() {
    }

    public static RequestUrlHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, RequestUrl object) {
        Integer id = object.getId();
        if (id != null) {
            statement.bindLong(1, (long) id.intValue());
        } else {
            statement.bindNull(1);
        }
        String url = object.getUrl();
        if (url != null) {
            statement.bindString(2, url);
        } else {
            statement.bindNull(2);
        }
        statement.bindLong(3, object.getTimeStamp());
        statement.bindLong(4, object.getIsExpired() ? 1 : 0);
        String district = object.getDistrict();
        if (district != null) {
            statement.bindString(5, district);
        } else {
            statement.bindNull(5);
        }
        String appName = object.getAppName();
        if (appName != null) {
            statement.bindString(6, appName);
        } else {
            statement.bindNull(6);
        }
        String serviceName = object.getServiceName();
        if (serviceName != null) {
            statement.bindString(7, serviceName);
        } else {
            statement.bindNull(7);
        }
        String key = object.getKey();
        if (key != null) {
            statement.bindString(8, key);
        } else {
            statement.bindNull(8);
        }
        statement.bindLong(9, object.getExpiredTime());
    }

    public RequestUrl readObject(Cursor cursor, int offset) {
        return new RequestUrl(cursor);
    }

    public void setPrimaryKeyValue(RequestUrl object, long value) {
        object.setId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, RequestUrl object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
