package javax.sql;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;

public interface RowSet extends ResultSet {
    void addRowSetListener(RowSetListener rowSetListener);

    void clearParameters() throws SQLException;

    void execute() throws SQLException;

    String getCommand();

    String getDataSourceName();

    boolean getEscapeProcessing() throws SQLException;

    int getMaxFieldSize() throws SQLException;

    int getMaxRows() throws SQLException;

    String getPassword();

    int getQueryTimeout() throws SQLException;

    int getTransactionIsolation();

    Map<String, Class<?>> getTypeMap() throws SQLException;

    String getUrl() throws SQLException;

    String getUsername();

    boolean isReadOnly();

    void removeRowSetListener(RowSetListener rowSetListener);

    void setArray(int i, Array array) throws SQLException;

    void setAsciiStream(int i, InputStream inputStream) throws SQLException;

    void setAsciiStream(int i, InputStream inputStream, int i2) throws SQLException;

    void setAsciiStream(String str, InputStream inputStream) throws SQLException;

    void setAsciiStream(String str, InputStream inputStream, int i) throws SQLException;

    void setBigDecimal(int i, BigDecimal bigDecimal) throws SQLException;

    void setBigDecimal(String str, BigDecimal bigDecimal) throws SQLException;

    void setBinaryStream(int i, InputStream inputStream) throws SQLException;

    void setBinaryStream(int i, InputStream inputStream, int i2) throws SQLException;

    void setBinaryStream(String str, InputStream inputStream) throws SQLException;

    void setBinaryStream(String str, InputStream inputStream, int i) throws SQLException;

    void setBlob(int i, InputStream inputStream) throws SQLException;

    void setBlob(int i, InputStream inputStream, long j) throws SQLException;

    void setBlob(int i, Blob blob) throws SQLException;

    void setBlob(String str, InputStream inputStream) throws SQLException;

    void setBlob(String str, InputStream inputStream, long j) throws SQLException;

    void setBlob(String str, Blob blob) throws SQLException;

    void setBoolean(int i, boolean z) throws SQLException;

    void setBoolean(String str, boolean z) throws SQLException;

    void setByte(int i, byte b) throws SQLException;

    void setByte(String str, byte b) throws SQLException;

    void setBytes(int i, byte[] bArr) throws SQLException;

    void setBytes(String str, byte[] bArr) throws SQLException;

    void setCharacterStream(int i, Reader reader) throws SQLException;

    void setCharacterStream(int i, Reader reader, int i2) throws SQLException;

    void setCharacterStream(String str, Reader reader) throws SQLException;

    void setCharacterStream(String str, Reader reader, int i) throws SQLException;

    void setClob(int i, Reader reader) throws SQLException;

    void setClob(int i, Reader reader, long j) throws SQLException;

    void setClob(int i, Clob clob) throws SQLException;

    void setClob(String str, Reader reader) throws SQLException;

    void setClob(String str, Reader reader, long j) throws SQLException;

    void setClob(String str, Clob clob) throws SQLException;

    void setCommand(String str) throws SQLException;

    void setConcurrency(int i) throws SQLException;

    void setDataSourceName(String str) throws SQLException;

    void setDate(int i, Date date) throws SQLException;

    void setDate(int i, Date date, Calendar calendar) throws SQLException;

    void setDate(String str, Date date) throws SQLException;

    void setDate(String str, Date date, Calendar calendar) throws SQLException;

    void setDouble(int i, double d) throws SQLException;

    void setDouble(String str, double d) throws SQLException;

    void setEscapeProcessing(boolean z) throws SQLException;

    void setFloat(int i, float f) throws SQLException;

    void setFloat(String str, float f) throws SQLException;

    void setInt(int i, int i2) throws SQLException;

    void setInt(String str, int i) throws SQLException;

    void setLong(int i, long j) throws SQLException;

    void setLong(String str, long j) throws SQLException;

    void setMaxFieldSize(int i) throws SQLException;

    void setMaxRows(int i) throws SQLException;

    void setNCharacterStream(int i, Reader reader) throws SQLException;

    void setNCharacterStream(int i, Reader reader, long j) throws SQLException;

    void setNCharacterStream(String str, Reader reader) throws SQLException;

    void setNCharacterStream(String str, Reader reader, long j) throws SQLException;

    void setNClob(int i, Reader reader) throws SQLException;

    void setNClob(int i, Reader reader, long j) throws SQLException;

    void setNClob(int i, NClob nClob) throws SQLException;

    void setNClob(String str, Reader reader) throws SQLException;

    void setNClob(String str, Reader reader, long j) throws SQLException;

    void setNClob(String str, NClob nClob) throws SQLException;

    void setNString(int i, String str) throws SQLException;

    void setNString(String str, String str2) throws SQLException;

    void setNull(int i, int i2) throws SQLException;

    void setNull(int i, int i2, String str) throws SQLException;

    void setNull(String str, int i) throws SQLException;

    void setNull(String str, int i, String str2) throws SQLException;

    void setObject(int i, Object obj) throws SQLException;

    void setObject(int i, Object obj, int i2) throws SQLException;

    void setObject(int i, Object obj, int i2, int i3) throws SQLException;

    void setObject(String str, Object obj) throws SQLException;

    void setObject(String str, Object obj, int i) throws SQLException;

    void setObject(String str, Object obj, int i, int i2) throws SQLException;

    void setPassword(String str) throws SQLException;

    void setQueryTimeout(int i) throws SQLException;

    void setReadOnly(boolean z) throws SQLException;

    void setRef(int i, Ref ref) throws SQLException;

    void setRowId(int i, RowId rowId) throws SQLException;

    void setRowId(String str, RowId rowId) throws SQLException;

    void setSQLXML(int i, SQLXML sqlxml) throws SQLException;

    void setSQLXML(String str, SQLXML sqlxml) throws SQLException;

    void setShort(int i, short s) throws SQLException;

    void setShort(String str, short s) throws SQLException;

    void setString(int i, String str) throws SQLException;

    void setString(String str, String str2) throws SQLException;

    void setTime(int i, Time time) throws SQLException;

    void setTime(int i, Time time, Calendar calendar) throws SQLException;

    void setTime(String str, Time time) throws SQLException;

    void setTime(String str, Time time, Calendar calendar) throws SQLException;

    void setTimestamp(int i, Timestamp timestamp) throws SQLException;

    void setTimestamp(int i, Timestamp timestamp, Calendar calendar) throws SQLException;

    void setTimestamp(String str, Timestamp timestamp) throws SQLException;

    void setTimestamp(String str, Timestamp timestamp, Calendar calendar) throws SQLException;

    void setTransactionIsolation(int i) throws SQLException;

    void setType(int i) throws SQLException;

    void setTypeMap(Map<String, Class<?>> map) throws SQLException;

    void setURL(int i, URL url) throws SQLException;

    void setUrl(String str) throws SQLException;

    void setUsername(String str) throws SQLException;
}
