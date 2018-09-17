package java.util.zip;

public interface Checksum {
    long getValue();

    void reset();

    void update(int i);

    void update(byte[] bArr, int i, int i2);
}
