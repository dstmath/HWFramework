package sun.nio.cs;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

public class ThreadLocalCoders {
    private static final int CACHE_SIZE = 3;
    private static Cache decoderCache;
    private static Cache encoderCache;

    private static abstract class Cache {
        private ThreadLocal cache;
        private final int size;

        abstract Object create(Object obj);

        abstract boolean hasName(Object obj, Object obj2);

        Cache(int size) {
            this.cache = new ThreadLocal();
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

    /* renamed from: sun.nio.cs.ThreadLocalCoders.1 */
    static class AnonymousClass1 extends Cache {
        static final /* synthetic */ boolean -assertionsDisabled = false;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.nio.cs.ThreadLocalCoders.1.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.nio.cs.ThreadLocalCoders.1.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.nio.cs.ThreadLocalCoders.1.<clinit>():void");
        }

        AnonymousClass1(int $anonymous0) {
            super($anonymous0);
        }

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
    }

    /* renamed from: sun.nio.cs.ThreadLocalCoders.2 */
    static class AnonymousClass2 extends Cache {
        static final /* synthetic */ boolean -assertionsDisabled = false;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.nio.cs.ThreadLocalCoders.2.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.nio.cs.ThreadLocalCoders.2.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.nio.cs.ThreadLocalCoders.2.<clinit>():void");
        }

        AnonymousClass2(int $anonymous0) {
            super($anonymous0);
        }

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
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.nio.cs.ThreadLocalCoders.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.nio.cs.ThreadLocalCoders.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.nio.cs.ThreadLocalCoders.<clinit>():void");
    }

    public ThreadLocalCoders() {
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
