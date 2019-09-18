package dalvik.system;

import java.nio.ByteBuffer;

public final class InMemoryDexClassLoader extends BaseDexClassLoader {
    public InMemoryDexClassLoader(ByteBuffer[] dexBuffers, ClassLoader parent) {
        super(dexBuffers, parent);
    }

    public InMemoryDexClassLoader(ByteBuffer dexBuffer, ClassLoader parent) {
        this(new ByteBuffer[]{dexBuffer}, parent);
    }
}
