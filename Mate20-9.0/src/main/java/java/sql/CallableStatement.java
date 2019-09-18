package java.sql;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Calendar;
import java.util.Map;

public interface CallableStatement extends PreparedStatement {
    Array getArray(int i) throws SQLException;

    Array getArray(String str) throws SQLException;

    BigDecimal getBigDecimal(int i) throws SQLException;

    @Deprecated
    BigDecimal getBigDecimal(int i, int i2) throws SQLException;

    BigDecimal getBigDecimal(String str) throws SQLException;

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

    Date getDate(int i) throws SQLException;

    Date getDate(int i, Calendar calendar) throws SQLException;

    Date getDate(String str) throws SQLException;

    Date getDate(String str, Calendar calendar) throws SQLException;

    double getDouble(int i) throws SQLException;

    double getDouble(String str) throws SQLException;

    float getFloat(int i) throws SQLException;

    float getFloat(String str) throws SQLException;

    int getInt(int i) throws SQLException;

    int getInt(String str) throws SQLException;

    long getLong(int i) throws SQLException;

    long getLong(String str) throws SQLException;

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

    RowId getRowId(int i) throws SQLException;

    RowId getRowId(String str) throws SQLException;

    SQLXML getSQLXML(int i) throws SQLException;

    SQLXML getSQLXML(String str) throws SQLException;

    short getShort(int i) throws SQLException;

    short getShort(String str) throws SQLException;

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

    URL getURL(int i) throws SQLException;

    URL getURL(String str) throws SQLException;

    void registerOutParameter(int i, int i2) throws SQLException;

    void registerOutParameter(int i, int i2, int i3) throws SQLException;

    void registerOutParameter(int i, int i2, String str) throws SQLException;

    void registerOutParameter(String str, int i) throws SQLException;

    void registerOutParameter(String str, int i, int i2) throws SQLException;

    void registerOutParameter(String str, int i, String str2) throws SQLException;

    void setAsciiStream(String str, InputStream inputStream) throws SQLException;

    void setAsciiStream(String str, InputStream inputStream, int i) throws SQLException;

    void setAsciiStream(String str, InputStream inputStream, long j) throws SQLException;

    void setBigDecimal(String str, BigDecimal bigDecimal) throws SQLException;

    void setBinaryStream(String str, InputStream inputStream) throws SQLException;

    void setBinaryStream(String str, InputStream inputStream, int i) throws SQLException;

    void setBinaryStream(String str, InputStream inputStream, long j) throws SQLException;

    void setBlob(String str, InputStream inputStream) throws SQLException;

    void setBlob(String str, InputStream inputStream, long j) throws SQLException;

    void setBlob(String str, Blob blob) throws SQLException;

    void setBoolean(String str, boolean z) throws SQLException;

    void setByte(String str, byte b) throws SQLException;

    void setBytes(String str, byte[] bArr) throws SQLException;

    void setCharacterStream(String str, Reader reader) throws SQLException;

    void setCharacterStream(String str, Reader reader, int i) throws SQLException;

    void setCharacterStream(String str, Reader reader, long j) throws SQLException;

    void setClob(String str, Reader reader) throws SQLException;

    void setClob(String str, Reader reader, long j) throws SQLException;

    void setClob(String str, Clob clob) throws SQLException;

    void setDate(String str, Date date) throws SQLException;

    void setDate(String str, Date date, Calendar calendar) throws SQLException;

    void setDouble(String str, double d) throws SQLException;

    void setFloat(String str, float f) throws SQLException;

    void setInt(String str, int i) throws SQLException;

    void setLong(String str, long j) throws SQLException;

    void setNCharacterStream(String str, Reader reader) throws SQLException;

    void setNCharacterStream(String str, Reader reader, long j) throws SQLException;

    void setNClob(String str, Reader reader) throws SQLException;

    void setNClob(String str, Reader reader, long j) throws SQLException;

    void setNClob(String str, NClob nClob) throws SQLException;

    void setNString(String str, String str2) throws SQLException;

    void setNull(String str, int i) throws SQLException;

    void setNull(String str, int i, String str2) throws SQLException;

    void setObject(String str, Object obj) throws SQLException;

    void setObject(String str, Object obj, int i) throws SQLException;

    void setObject(String str, Object obj, int i, int i2) throws SQLException;

    void setRowId(String str, RowId rowId) throws SQLException;

    void setSQLXML(String str, SQLXML sqlxml) throws SQLException;

    void setShort(String str, short s) throws SQLException;

    void setString(String str, String str2) throws SQLException;

    void setTime(String str, Time time) throws SQLException;

    void setTime(String str, Time time, Calendar calendar) throws SQLException;

    void setTimestamp(String str, Timestamp timestamp) throws SQLException;

    void setTimestamp(String str, Timestamp timestamp, Calendar calendar) throws SQLException;

    void setURL(String str, URL url) throws SQLException;

    boolean wasNull() throws SQLException;
}
