package java.sql;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Calendar;

public interface PreparedStatement extends Statement {
    void addBatch() throws SQLException;

    void clearParameters() throws SQLException;

    boolean execute() throws SQLException;

    ResultSet executeQuery() throws SQLException;

    int executeUpdate() throws SQLException;

    ResultSetMetaData getMetaData() throws SQLException;

    ParameterMetaData getParameterMetaData() throws SQLException;

    void setArray(int i, Array array) throws SQLException;

    void setAsciiStream(int i, InputStream inputStream) throws SQLException;

    void setAsciiStream(int i, InputStream inputStream, int i2) throws SQLException;

    void setAsciiStream(int i, InputStream inputStream, long j) throws SQLException;

    void setBigDecimal(int i, BigDecimal bigDecimal) throws SQLException;

    void setBinaryStream(int i, InputStream inputStream) throws SQLException;

    void setBinaryStream(int i, InputStream inputStream, int i2) throws SQLException;

    void setBinaryStream(int i, InputStream inputStream, long j) throws SQLException;

    void setBlob(int i, InputStream inputStream) throws SQLException;

    void setBlob(int i, InputStream inputStream, long j) throws SQLException;

    void setBlob(int i, Blob blob) throws SQLException;

    void setBoolean(int i, boolean z) throws SQLException;

    void setByte(int i, byte b) throws SQLException;

    void setBytes(int i, byte[] bArr) throws SQLException;

    void setCharacterStream(int i, Reader reader) throws SQLException;

    void setCharacterStream(int i, Reader reader, int i2) throws SQLException;

    void setCharacterStream(int i, Reader reader, long j) throws SQLException;

    void setClob(int i, Reader reader) throws SQLException;

    void setClob(int i, Reader reader, long j) throws SQLException;

    void setClob(int i, Clob clob) throws SQLException;

    void setDate(int i, Date date) throws SQLException;

    void setDate(int i, Date date, Calendar calendar) throws SQLException;

    void setDouble(int i, double d) throws SQLException;

    void setFloat(int i, float f) throws SQLException;

    void setInt(int i, int i2) throws SQLException;

    void setLong(int i, long j) throws SQLException;

    void setNCharacterStream(int i, Reader reader) throws SQLException;

    void setNCharacterStream(int i, Reader reader, long j) throws SQLException;

    void setNClob(int i, Reader reader) throws SQLException;

    void setNClob(int i, Reader reader, long j) throws SQLException;

    void setNClob(int i, NClob nClob) throws SQLException;

    void setNString(int i, String str) throws SQLException;

    void setNull(int i, int i2) throws SQLException;

    void setNull(int i, int i2, String str) throws SQLException;

    void setObject(int i, Object obj) throws SQLException;

    void setObject(int i, Object obj, int i2) throws SQLException;

    void setObject(int i, Object obj, int i2, int i3) throws SQLException;

    void setRef(int i, Ref ref) throws SQLException;

    void setRowId(int i, RowId rowId) throws SQLException;

    void setSQLXML(int i, SQLXML sqlxml) throws SQLException;

    void setShort(int i, short s) throws SQLException;

    void setString(int i, String str) throws SQLException;

    void setTime(int i, Time time) throws SQLException;

    void setTime(int i, Time time, Calendar calendar) throws SQLException;

    void setTimestamp(int i, Timestamp timestamp) throws SQLException;

    void setTimestamp(int i, Timestamp timestamp, Calendar calendar) throws SQLException;

    void setURL(int i, URL url) throws SQLException;

    @Deprecated
    void setUnicodeStream(int i, InputStream inputStream, int i2) throws SQLException;
}
