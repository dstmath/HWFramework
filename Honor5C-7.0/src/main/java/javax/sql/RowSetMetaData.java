package javax.sql;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public interface RowSetMetaData extends ResultSetMetaData {
    void setAutoIncrement(int i, boolean z) throws SQLException;

    void setCaseSensitive(int i, boolean z) throws SQLException;

    void setCatalogName(int i, String str) throws SQLException;

    void setColumnCount(int i) throws SQLException;

    void setColumnDisplaySize(int i, int i2) throws SQLException;

    void setColumnLabel(int i, String str) throws SQLException;

    void setColumnName(int i, String str) throws SQLException;

    void setColumnType(int i, int i2) throws SQLException;

    void setColumnTypeName(int i, String str) throws SQLException;

    void setCurrency(int i, boolean z) throws SQLException;

    void setNullable(int i, int i2) throws SQLException;

    void setPrecision(int i, int i2) throws SQLException;

    void setScale(int i, int i2) throws SQLException;

    void setSchemaName(int i, String str) throws SQLException;

    void setSearchable(int i, boolean z) throws SQLException;

    void setSigned(int i, boolean z) throws SQLException;

    void setTableName(int i, String str) throws SQLException;
}
