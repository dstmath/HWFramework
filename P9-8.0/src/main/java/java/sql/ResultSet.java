package java.sql;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Calendar;
import java.util.Map;

public interface ResultSet extends Wrapper, AutoCloseable {
    public static final int CLOSE_CURSORS_AT_COMMIT = 2;
    public static final int CONCUR_READ_ONLY = 1007;
    public static final int CONCUR_UPDATABLE = 1008;
    public static final int FETCH_FORWARD = 1000;
    public static final int FETCH_REVERSE = 1001;
    public static final int FETCH_UNKNOWN = 1002;
    public static final int HOLD_CURSORS_OVER_COMMIT = 1;
    public static final int TYPE_FORWARD_ONLY = 1003;
    public static final int TYPE_SCROLL_INSENSITIVE = 1004;
    public static final int TYPE_SCROLL_SENSITIVE = 1005;

    boolean absolute(int i) throws SQLException;

    void afterLast() throws SQLException;

    void beforeFirst() throws SQLException;

    void cancelRowUpdates() throws SQLException;

    void clearWarnings() throws SQLException;

    void close() throws SQLException;

    void deleteRow() throws SQLException;

    int findColumn(String str) throws SQLException;

    boolean first() throws SQLException;

    Array getArray(int i) throws SQLException;

    Array getArray(String str) throws SQLException;

    InputStream getAsciiStream(int i) throws SQLException;

    InputStream getAsciiStream(String str) throws SQLException;

    BigDecimal getBigDecimal(int i) throws SQLException;

    @Deprecated
    BigDecimal getBigDecimal(int i, int i2) throws SQLException;

    BigDecimal getBigDecimal(String str) throws SQLException;

    @Deprecated
    BigDecimal getBigDecimal(String str, int i) throws SQLException;

    InputStream getBinaryStream(int i) throws SQLException;

    InputStream getBinaryStream(String str) throws SQLException;

    Blob getBlob(int i) throws SQLException;

    Blob getBlob(String str) throws SQLException;

    boolean getBoolean(int i) throws SQLException;

    boolean getBoolean(String str) throws SQLException;

    byte getByte(int i) throws SQLException;

    byte getByte(String str) throws SQLException;

    byte[] getBytes(int i) throws SQLException;

    byte[] getBytes(String str) throws SQLException;

    Reader getCharacterStream(int i) throws SQLException;

    Reader getCharacterStream(String str) throws SQLException;

    Clob getClob(int i) throws SQLException;

    Clob getClob(String str) throws SQLException;

    int getConcurrency() throws SQLException;

    String getCursorName() throws SQLException;

    Date getDate(int i) throws SQLException;

    Date getDate(int i, Calendar calendar) throws SQLException;

    Date getDate(String str) throws SQLException;

    Date getDate(String str, Calendar calendar) throws SQLException;

    double getDouble(int i) throws SQLException;

    double getDouble(String str) throws SQLException;

    int getFetchDirection() throws SQLException;

    int getFetchSize() throws SQLException;

    float getFloat(int i) throws SQLException;

    float getFloat(String str) throws SQLException;

    int getHoldability() throws SQLException;

    int getInt(int i) throws SQLException;

    int getInt(String str) throws SQLException;

    long getLong(int i) throws SQLException;

    long getLong(String str) throws SQLException;

    ResultSetMetaData getMetaData() throws SQLException;

    Reader getNCharacterStream(int i) throws SQLException;

    Reader getNCharacterStream(String str) throws SQLException;

    NClob getNClob(int i) throws SQLException;

    NClob getNClob(String str) throws SQLException;

    String getNString(int i) throws SQLException;

    String getNString(String str) throws SQLException;

    Object getObject(int i) throws SQLException;

    Object getObject(int i, Map<String, Class<?>> map) throws SQLException;

    Object getObject(String str) throws SQLException;

    Object getObject(String str, Map<String, Class<?>> map) throws SQLException;

    Ref getRef(int i) throws SQLException;

    Ref getRef(String str) throws SQLException;

    int getRow() throws SQLException;

    RowId getRowId(int i) throws SQLException;

    RowId getRowId(String str) throws SQLException;

    SQLXML getSQLXML(int i) throws SQLException;

    SQLXML getSQLXML(String str) throws SQLException;

    short getShort(int i) throws SQLException;

    short getShort(String str) throws SQLException;

    Statement getStatement() throws SQLException;

    String getString(int i) throws SQLException;

    String getString(String str) throws SQLException;

    Time getTime(int i) throws SQLException;

    Time getTime(int i, Calendar calendar) throws SQLException;

    Time getTime(String str) throws SQLException;

    Time getTime(String str, Calendar calendar) throws SQLException;

    Timestamp getTimestamp(int i) throws SQLException;

    Timestamp getTimestamp(int i, Calendar calendar) throws SQLException;

    Timestamp getTimestamp(String str) throws SQLException;

    Timestamp getTimestamp(String str, Calendar calendar) throws SQLException;

    int getType() throws SQLException;

    URL getURL(int i) throws SQLException;

    URL getURL(String str) throws SQLException;

    @Deprecated
    InputStream getUnicodeStream(int i) throws SQLException;

    @Deprecated
    InputStream getUnicodeStream(String str) throws SQLException;

    SQLWarning getWarnings() throws SQLException;

    void insertRow() throws SQLException;

    boolean isAfterLast() throws SQLException;

    boolean isBeforeFirst() throws SQLException;

    boolean isClosed() throws SQLException;

    boolean isFirst() throws SQLException;

    boolean isLast() throws SQLException;

    boolean last() throws SQLException;

    void moveToCurrentRow() throws SQLException;

    void moveToInsertRow() throws SQLException;

    boolean next() throws SQLException;

    boolean previous() throws SQLException;

    void refreshRow() throws SQLException;

    boolean relative(int i) throws SQLException;

    boolean rowDeleted() throws SQLException;

    boolean rowInserted() throws SQLException;

    boolean rowUpdated() throws SQLException;

    void setFetchDirection(int i) throws SQLException;

    void setFetchSize(int i) throws SQLException;

    void updateArray(int i, Array array) throws SQLException;

    void updateArray(String str, Array array) throws SQLException;

    void updateAsciiStream(int i, InputStream inputStream) throws SQLException;

    void updateAsciiStream(int i, InputStream inputStream, int i2) throws SQLException;

    void updateAsciiStream(int i, InputStream inputStream, long j) throws SQLException;

    void updateAsciiStream(String str, InputStream inputStream) throws SQLException;

    void updateAsciiStream(String str, InputStream inputStream, int i) throws SQLException;

    void updateAsciiStream(String str, InputStream inputStream, long j) throws SQLException;

    void updateBigDecimal(int i, BigDecimal bigDecimal) throws SQLException;

    void updateBigDecimal(String str, BigDecimal bigDecimal) throws SQLException;

    void updateBinaryStream(int i, InputStream inputStream) throws SQLException;

    void updateBinaryStream(int i, InputStream inputStream, int i2) throws SQLException;

    void updateBinaryStream(int i, InputStream inputStream, long j) throws SQLException;

    void updateBinaryStream(String str, InputStream inputStream) throws SQLException;

    void updateBinaryStream(String str, InputStream inputStream, int i) throws SQLException;

    void updateBinaryStream(String str, InputStream inputStream, long j) throws SQLException;

    void updateBlob(int i, InputStream inputStream) throws SQLException;

    void updateBlob(int i, InputStream inputStream, long j) throws SQLException;

    void updateBlob(int i, Blob blob) throws SQLException;

    void updateBlob(String str, InputStream inputStream) throws SQLException;

    void updateBlob(String str, InputStream inputStream, long j) throws SQLException;

    void updateBlob(String str, Blob blob) throws SQLException;

    void updateBoolean(int i, boolean z) throws SQLException;

    void updateBoolean(String str, boolean z) throws SQLException;

    void updateByte(int i, byte b) throws SQLException;

    void updateByte(String str, byte b) throws SQLException;

    void updateBytes(int i, byte[] bArr) throws SQLException;

    void updateBytes(String str, byte[] bArr) throws SQLException;

    void updateCharacterStream(int i, Reader reader) throws SQLException;

    void updateCharacterStream(int i, Reader reader, int i2) throws SQLException;

    void updateCharacterStream(int i, Reader reader, long j) throws SQLException;

    void updateCharacterStream(String str, Reader reader) throws SQLException;

    void updateCharacterStream(String str, Reader reader, int i) throws SQLException;

    void updateCharacterStream(String str, Reader reader, long j) throws SQLException;

    void updateClob(int i, Reader reader) throws SQLException;

    void updateClob(int i, Reader reader, long j) throws SQLException;

    void updateClob(int i, Clob clob) throws SQLException;

    void updateClob(String str, Reader reader) throws SQLException;

    void updateClob(String str, Reader reader, long j) throws SQLException;

    void updateClob(String str, Clob clob) throws SQLException;

    void updateDate(int i, Date date) throws SQLException;

    void updateDate(String str, Date date) throws SQLException;

    void updateDouble(int i, double d) throws SQLException;

    void updateDouble(String str, double d) throws SQLException;

    void updateFloat(int i, float f) throws SQLException;

    void updateFloat(String str, float f) throws SQLException;

    void updateInt(int i, int i2) throws SQLException;

    void updateInt(String str, int i) throws SQLException;

    void updateLong(int i, long j) throws SQLException;

    void updateLong(String str, long j) throws SQLException;

    void updateNCharacterStream(int i, Reader reader) throws SQLException;

    void updateNCharacterStream(int i, Reader reader, long j) throws SQLException;

    void updateNCharacterStream(String str, Reader reader) throws SQLException;

    void updateNCharacterStream(String str, Reader reader, long j) throws SQLException;

    void updateNClob(int i, Reader reader) throws SQLException;

    void updateNClob(int i, Reader reader, long j) throws SQLException;

    void updateNClob(int i, NClob nClob) throws SQLException;

    void updateNClob(String str, Reader reader) throws SQLException;

    void updateNClob(String str, Reader reader, long j) throws SQLException;

    void updateNClob(String str, NClob nClob) throws SQLException;

    void updateNString(int i, String str) throws SQLException;

    void updateNString(String str, String str2) throws SQLException;

    void updateNull(int i) throws SQLException;

    void updateNull(String str) throws SQLException;

    void updateObject(int i, Object obj) throws SQLException;

    void updateObject(int i, Object obj, int i2) throws SQLException;

    void updateObject(String str, Object obj) throws SQLException;

    void updateObject(String str, Object obj, int i) throws SQLException;

    void updateRef(int i, Ref ref) throws SQLException;

    void updateRef(String str, Ref ref) throws SQLException;

    void updateRow() throws SQLException;

    void updateRowId(int i, RowId rowId) throws SQLException;

    void updateRowId(String str, RowId rowId) throws SQLException;

    void updateSQLXML(int i, SQLXML sqlxml) throws SQLException;

    void updateSQLXML(String str, SQLXML sqlxml) throws SQLException;

    void updateShort(int i, short s) throws SQLException;

    void updateShort(String str, short s) throws SQLException;

    void updateString(int i, String str) throws SQLException;

    void updateString(String str, String str2) throws SQLException;

    void updateTime(int i, Time time) throws SQLException;

    void updateTime(String str, Time time) throws SQLException;

    void updateTimestamp(int i, Timestamp timestamp) throws SQLException;

    void updateTimestamp(String str, Timestamp timestamp) throws SQLException;

    boolean wasNull() throws SQLException;
}
