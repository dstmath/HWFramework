package java.sql;

import java.util.Map;

public interface Ref {
    String getBaseTypeName() throws SQLException;

    Object getObject() throws SQLException;

    Object getObject(Map<String, Class<?>> map) throws SQLException;

    void setObject(Object obj) throws SQLException;
}
