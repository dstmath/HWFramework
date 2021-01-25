package ohos.data.async;

import ohos.data.resultset.ResultSet;

public interface QueryCallback {
    void onQueryDone(Integer num, Object obj, ResultSet resultSet);
}
