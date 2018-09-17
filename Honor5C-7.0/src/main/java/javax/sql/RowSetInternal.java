package javax.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface RowSetInternal {
    Connection getConnection() throws SQLException;

    ResultSet getOriginal() throws SQLException;

    ResultSet getOriginalRow() throws SQLException;

    Object[] getParams() throws SQLException;

    void setMetaData(RowSetMetaData rowSetMetaData) throws SQLException;
}
