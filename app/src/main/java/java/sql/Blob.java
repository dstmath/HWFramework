package java.sql;

import java.io.InputStream;
import java.io.OutputStream;

public interface Blob {
    void free() throws SQLException;

    InputStream getBinaryStream() throws SQLException;

    InputStream getBinaryStream(long j, long j2) throws SQLException;

    byte[] getBytes(long j, int i) throws SQLException;

    long length() throws SQLException;

    long position(Blob blob, long j) throws SQLException;

    long position(byte[] bArr, long j) throws SQLException;

    OutputStream setBinaryStream(long j) throws SQLException;

    int setBytes(long j, byte[] bArr) throws SQLException;

    int setBytes(long j, byte[] bArr, int i, int i2) throws SQLException;

    void truncate(long j) throws SQLException;
}
