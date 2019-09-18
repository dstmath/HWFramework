package sun.nio.cs;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

public class ThreadLocalCoders {
    private static final int CACHE_SIZE = 3;
    private static Cache decoderCache = new Cache(3) {
        static final /* synthetic */ boolean $assertionsDisabled = false;

        static {
            Class<ThreadLocalCoders> cls = ThreadLocalCoders.class;
        }

        /* access modifiers changed from: package-private */
        public boolean hasName(Object ob, Object name) {
            if (name instanceof String) {
                return ((CharsetDecoder) ob).charset().name().equals(name);
            }
            if (name instanceof Charset) {
                return ((CharsetDecoder) ob).charset().equals(name);
            }
            return false;
        }

        /* access modifiers changed from: package-private */
        public Object create(Object name) {
            if (name instanceof String) {
                return Charset.forName((String) name).newDecoder();
            }
            if (name instanceof Charset) {
                return ((Charset) name).newDecoder();
            }
            return null;
        }
    };
    private static Cache encoderCache = new Cache(3) {
        static final /* synthetic */ boolean $assertionsDisabled = false;

        static {
            Class<ThreadLocalCoders> cls = ThreadLocalCoders.class;
        }

        /* access modifiers changed from: package-private */
        public boolean hasName(Object ob, Object name) {
            if (name instanceof String) {
                return ((CharsetEncoder) ob).charset().name().equals(name);
            }
            if (name instanceof Charset) {
                return ((CharsetEncoder) ob).charset().equals(name);
            }
            return false;
        }

        /* access modifiers changed from: package-private */
        public Object create(Object name) {
            if (name instanceof String) {
                return Charset.forName((String) name).newEncoder();
            }
            if (name instanceof Charset) {
                return ((Charset) name).newEncoder();
            }
            return null;
        }
    };

    private static abstract class Cache {
        private ThreadLocal<Object[]> cache = new ThreadLocal<>();
        private final int size;

        /* access modifiers changed from: package-private */
        public abstract Object create(Object obj);

        /* access modifiers changed from: package-private */
        public abstract boolean hasName(Object obj, Object obj2);

        Cache(int size2) {
            this.size = size2;
        }

        private void moveToFront(Object[] oa, int i) {
            Object ob = oa[i];
            for (int j = i; j > 0; j--) {
                oa[j] = oa[j - 1];
            }
            oa[0] = ob;
        }

        /* access modifiers changed from: package-private */
        public Object forName(Object name) {
            Object[] oa = this.cache.get();
            if (oa == null) {
                oa = new Object[this.size];
                this.cache.set(oa);
            } else {
                for (int i = 0; i < oa.length; i++) {
                    Object ob = oa[i];
                    if (ob != null && hasName(ob, name)) {
                        if (i > 0) {
                            moveToFront(oa, i);
                        }
                        return ob;
                    }
                }
            }
            Object ob2 = create(name);
            oa[oa.length - 1] = ob2;
            moveToFront(oa, oa.length - 1);
            return ob2;
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
