package java.sql;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;

public interface SQLInput {
    Array readArray() throws SQLException;

    InputStream readAsciiStream() throws SQLException;

    BigDecimal readBigDecimal() throws SQLException;

    InputStream readBinaryStream() throws SQLException;

    Blob readBlob() throws SQLException;

    boolean readBoolean() throws SQLException;

    byte readByte() throws SQLException;

    byte[] readBytes() throws SQLException;

    Reader readCharacterStream() throws SQLException;

    Clob readClob() throws SQLException;

    Date readDate() throws SQLException;

    double readDouble() throws SQLException;

    float readFloat() throws SQLException;

    int readInt() throws SQLException;

    long readLong() throws SQLException;

    NClob readNClob() throws SQLException;

    String readNString() throws SQLException;

    Object readObject() throws SQLException;

    Ref readRef() throws SQLException;

    RowId readRowId() throws SQLException;

    SQLXML readSQLXML() throws SQLException;

    short readShort() throws SQLException;

    String readString() throws SQLException;

    Time readTime() throws SQLException;

    Timestamp readTimestamp() throws SQLException;

    URL readURL() throws SQLException;

    boolean wasNull() throws SQLException;
}
