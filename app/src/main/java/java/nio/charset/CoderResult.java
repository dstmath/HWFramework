package java.nio.charset;

import java.lang.ref.WeakReference;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.util.HashMap;
import java.util.Map;

public class CoderResult {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    private static final int CR_ERROR_MIN = 2;
    private static final int CR_MALFORMED = 2;
    private static final int CR_OVERFLOW = 1;
    private static final int CR_UNDERFLOW = 0;
    private static final int CR_UNMAPPABLE = 3;
    public static final CoderResult OVERFLOW = null;
    public static final CoderResult UNDERFLOW = null;
    private static Cache malformedCache;
    private static final String[] names = null;
    private static Cache unmappableCache;
    private final int length;
    private final int type;

    private static abstract class Cache {
        private Map<Integer, WeakReference<CoderResult>> cache;

        protected abstract CoderResult create(int i);

        private Cache() {
            this.cache = null;
        }

        private synchronized CoderResult get(int len) {
            CoderResult e;
            if (len <= 0) {
                throw new IllegalArgumentException("Non-positive length");
            }
            Integer k = new Integer(len);
            e = null;
            if (this.cache == null) {
                this.cache = new HashMap();
            } else {
                WeakReference<CoderResult> w = (WeakReference) this.cache.get(k);
                if (w != null) {
                    e = (CoderResult) w.get();
                }
            }
            if (e == null) {
                e = create(len);
                this.cache.put(k, new WeakReference(e));
            }
            return e;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.nio.charset.CoderResult.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.nio.charset.CoderResult.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.nio.charset.CoderResult.<clinit>():void");
    }

    private CoderResult(int type, int length) {
        this.type = type;
        this.length = length;
    }

    public String toString() {
        String nm = names[this.type];
        return isError() ? nm + "[" + this.length + "]" : nm;
    }

    public boolean isUnderflow() {
        return this.type == 0 ? true : -assertionsDisabled;
    }

    public boolean isOverflow() {
        return this.type == CR_OVERFLOW ? true : -assertionsDisabled;
    }

    public boolean isError() {
        return this.type >= CR_MALFORMED ? true : -assertionsDisabled;
    }

    public boolean isMalformed() {
        return this.type == CR_MALFORMED ? true : -assertionsDisabled;
    }

    public boolean isUnmappable() {
        return this.type == CR_UNMAPPABLE ? true : -assertionsDisabled;
    }

    public int length() {
        if (isError()) {
            return this.length;
        }
        throw new UnsupportedOperationException();
    }

    public static CoderResult malformedForLength(int length) {
        return malformedCache.get(length);
    }

    public static CoderResult unmappableForLength(int length) {
        return unmappableCache.get(length);
    }

    public void throwException() throws CharacterCodingException {
        switch (this.type) {
            case CR_UNDERFLOW /*0*/:
                throw new BufferUnderflowException();
            case CR_OVERFLOW /*1*/:
                throw new BufferOverflowException();
            case CR_MALFORMED /*2*/:
                throw new MalformedInputException(this.length);
            case CR_UNMAPPABLE /*3*/:
                throw new UnmappableCharacterException(this.length);
            default:
                if (!-assertionsDisabled) {
                    throw new AssertionError();
                }
        }
    }
}
