package java.sql;

import java.util.Map;

public interface Struct {
    Object[] getAttributes() throws SQLException;

    Object[] getAttributes(Map<String, Class<?>> map) throws SQLException;

    String getSQLTypeName() throws SQLException;
}
