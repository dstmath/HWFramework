package java.sql;

public interface ResultSetMetaData extends Wrapper {
    public static final int columnNoNulls = 0;
    public static final int columnNullable = 1;
    public static final int columnNullableUnknown = 2;

    String getCatalogName(int i) throws SQLException;

    String getColumnClassName(int i) throws SQLException;

    int getColumnCount() throws SQLException;

    int getColumnDisplaySize(int i) throws SQLException;

    String getColumnLabel(int i) throws SQLException;

    String getColumnName(int i) throws SQLException;

    int getColumnType(int i) throws SQLException;

    String getColumnTypeName(int i) throws SQLException;

    int getPrecision(int i) throws SQLException;

    int getScale(int i) throws SQLException;

    String getSchemaName(int i) throws SQLException;

    String getTableName(int i) throws SQLException;

    boolean isAutoIncrement(int i) throws SQLException;

    boolean isCaseSensitive(int i) throws SQLException;

    boolean isCurrency(int i) throws SQLException;

    boolean isDefinitelyWritable(int i) throws SQLException;

    int isNullable(int i) throws SQLException;

    boolean isReadOnly(int i) throws SQLException;

    boolean isSearchable(int i) throws SQLException;

    boolean isSigned(int i) throws SQLException;

    boolean isWritable(int i) throws SQLException;
}
