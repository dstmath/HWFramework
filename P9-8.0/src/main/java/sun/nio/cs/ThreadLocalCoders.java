package sun.nio.cs;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

public class ThreadLocalCoders {
    private static final int CACHE_SIZE = 3;
    private static Cache decoderCache = new Cache(3) {
        static final /* synthetic */ boolean -assertionsDisabled = (AnonymousClass1.class.desiredAssertionStatus() ^ 1);

        boolean hasName(Object ob, Object name) {
            if (name instanceof String) {
                return ((CharsetDecoder) ob).charset().name().equals(name);
            }
            if (name instanceof Charset) {
                return ((CharsetDecoder) ob).charset().equals(name);
            }
            return false;
        }

        Object create(Object name) {
            if (name instanceof String) {
                return Charset.forName((String) name).newDecoder();
            }
            if (name instanceof Charset) {
                return ((Charset) name).newDecoder();
            }
            if (-assertionsDisabled) {
                return null;
            }
            throw new AssertionError();
        }
    };
    private static Cache encoderCache = new Cache(3) {
        static final /* synthetic */ boolean -assertionsDisabled = (AnonymousClass2.class.desiredAssertionStatus() ^ 1);

        boolean hasName(Object ob, Object name) {
            if (name instanceof String) {
                return ((CharsetEncoder) ob).charset().name().equals(name);
            }
            if (name instanceof Charset) {
                return ((CharsetEncoder) ob).charset().equals(name);
            }
            return false;
        }

        Object create(Object name) {
            if (name instanceof String) {
                return Charset.forName((String) name).newEncoder();
            }
            if (name instanceof Charset) {
                return ((Charset) name).newEncoder();
            }
            if (-assertionsDisabled) {
                return null;
            }
            throw new AssertionError();
        }
    };

    private static abstract class Cache {
        private ThreadLocal<Object[]> cache = new ThreadLocal();
        private final int size;

        abstract Object create(Object obj);

        abstract boolean hasName(Object obj, Object obj2);

        Cache(int size) {
            this.size = size;
        }

        private void moveToFront(Object[] oa, int i) {
            Object ob = oa[i];
            for (int j = i; j > 0; j--) {
                oa[j] = oa[j - 1];
            }
            oa[0] = ob;
        }

        Object forName(Object name) {
            Object ob;
            Object[] oa = (Object[]) this.cache.get();
            if (oa == null) {
                oa = new Object[this.size];
                this.cache.set(oa);
            } else {
                for (int i = 0; i < oa.length; i++) {
                    ob = oa[i];
                    if (ob != null && hasName(ob, name)) {
                        if (i > 0) {
                            moveToFront(oa, i);
                        }
                        return ob;
                    }
                }
            }
            ob = create(name);
            oa[oa.length - 1] = ob;
            moveToFront(oa, oa.length - 1);
            return ob;
        }
    }

    public static CharsetDecoder decoderFor(Object name) {
        CharsetDecoder cd = (CharsetDecoder) decoderCache.forName(name);
        cd.reset();
        return cd;
    }

    public static CharsetEncoder encoderFor(Object name) {
        CharsetEncoder ce = (CharsetEncoder) encoderCache.forName(name);
        ce.reset();
        return ce;
    }
}
