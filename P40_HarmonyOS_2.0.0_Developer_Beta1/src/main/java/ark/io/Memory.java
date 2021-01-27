package ark.io;

import java.nio.ByteOrder;

public final class Memory {
    public static void memmove(Object obj, int i, Object obj2, int i2, long j) {
        libcore.io.Memory.memmove(obj, i, obj2, i2, j);
    }

    public static int peekIntValue(byte[] bArr, int i, ByteOrder byteOrder) {
        return libcore.io.Memory.peekInt(bArr, i, byteOrder);
    }

    public static void pokeIntValue(byte[] bArr, int i, int i2, ByteOrder byteOrder) {
        libcore.io.Memory.pokeInt(bArr, i, i2, byteOrder);
    }
}
