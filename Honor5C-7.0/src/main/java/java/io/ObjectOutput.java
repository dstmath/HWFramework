package java.io;

public interface ObjectOutput extends DataOutput, AutoCloseable {
    void close() throws IOException;

    void flush() throws IOException;

    void write(int i) throws IOException;

    void write(byte[] bArr) throws IOException;

    void write(byte[] bArr, int i, int i2) throws IOException;

    void writeObject(Object obj) throws IOException;
}
