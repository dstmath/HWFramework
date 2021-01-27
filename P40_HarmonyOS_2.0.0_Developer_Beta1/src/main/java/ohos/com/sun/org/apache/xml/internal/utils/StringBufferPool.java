package ohos.com.sun.org.apache.xml.internal.utils;

public class StringBufferPool {
    private static ObjectPool m_stringBufPool = new ObjectPool(FastStringBuffer.class);

    public static synchronized FastStringBuffer get() {
        FastStringBuffer fastStringBuffer;
        synchronized (StringBufferPool.class) {
            fastStringBuffer = (FastStringBuffer) m_stringBufPool.getInstance();
        }
        return fastStringBuffer;
    }

    public static synchronized void free(FastStringBuffer fastStringBuffer) {
        synchronized (StringBufferPool.class) {
            fastStringBuffer.setLength(0);
            m_stringBufPool.freeInstance(fastStringBuffer);
        }
    }
}
