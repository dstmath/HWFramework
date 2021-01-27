package ohos.data.orm;

import ohos.data.rdb.Statement;
import ohos.data.resultset.ResultSet;

public interface EntityHelper<T> {
    void bindValue(Statement statement, T t);

    void bindValue(Statement statement, T t, long j);

    T createInstance(ResultSet resultSet);

    String getDeleteStatement();

    String getInsertStatement();

    String getTableName();

    String getUpdateStatement();

    void setPrimaryKeyValue(T t, long j);
}
