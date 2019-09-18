package java.nio;

public final class ByteOrder {
    public static final ByteOrder BIG_ENDIAN = new ByteOrder("BIG_ENDIAN");
    public static final ByteOrder LITTLE_ENDIAN = new ByteOrder("LITTLE_ENDIAN");
    private String name;

    private ByteOrder(String name2) {
        this.name = name2;
    }

    public static ByteOrder nativeOrder() {
        return Bits.byteOrder();
    }

    public String toString() {
        return this.name;
    }
}
