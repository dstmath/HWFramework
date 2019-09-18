package java.sql;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

public interface Clob {
    void free() throws SQLException;

    InputStream getAsciiStream() throws SQLException;

    Reader getCharacterStream() throws SQLException;

    Reader getCharacterStream(long j, long j2) throws SQLException;

    String getSubString(long j, int i) throws SQLException;

    long length() throws SQLException;

    long position(String str, long j) throws SQLException;

    long position(Clob clob, long j) throws SQLException;

    OutputStream setAsciiStream(long j) throws SQLException;

    Writer setCharacterStream(long j) throws SQLException;

    int setString(long j, String str) throws SQLException;

    int setString(long j, String str, int i, int i2) throws SQLException;

    void truncate(long j) throws SQLException;
}
