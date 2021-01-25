package com.huawei.nb.model;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class RequestUrlHelper extends AEntityHelper<RequestUrl> {
    private static final RequestUrlHelper INSTANCE = new RequestUrlHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, RequestUrl requestUrl) {
        return null;
    }

    private RequestUrlHelper() {
    }

    public static RequestUrlHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, RequestUrl requestUrl) {
        Integer id = requestUrl.getId();
        if (id != null) {
            statement.bindLong(1, (long) id.intValue());
        } else {
            statement.bindNull(1);
        }
        String url = requestUrl.getUrl();
        if (url != null) {
            statement.bindString(2, url);
        } else {
            statement.bindNull(2);
        }
        statement.bindLong(3, requestUrl.getTimeStamp());
        statement.bindLong(4, requestUrl.getIsExpired() ? 1 : 0);
        String district = requestUrl.getDistrict();
        if (district != null) {
            statement.bindString(5, district);
        } else {
            statement.bindNull(5);
        }
        String appName = requestUrl.getAppName();
        if (appName != null) {
            statement.bindString(6, appName);
        } else {
            statement.bindNull(6);
        }
        String serviceName = requestUrl.getServiceName();
        if (serviceName != null) {
            statement.bindString(7, serviceName);
        } else {
            statement.bindNull(7);
        }
        String key = requestUrl.getKey();
        if (key != null) {
            statement.bindString(8, key);
        } else {
            statement.bindNull(8);
        }
        statement.bindLong(9, requestUrl.getExpiredTime());
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public RequestUrl readObject(Cursor cursor, int i) {
        return new RequestUrl(cursor);
    }

    public void setPrimaryKeyValue(RequestUrl requestUrl, long j) {
        requestUrl.setId(Integer.valueOf((int) j));
    }
}
