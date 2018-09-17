package android.icu.impl.coll;

import android.icu.text.UTF16;
import com.android.dex.DexFormat;
import dalvik.bytecode.Opcodes;

public final class CollationFCD {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    private static final int[] lcccBits = null;
    private static final byte[] lcccIndex = null;
    private static final int[] tcccBits = null;
    private static final byte[] tcccIndex = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.coll.CollationFCD.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.coll.CollationFCD.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.coll.CollationFCD.<clinit>():void");
    }

    public static boolean hasLccc(int c) {
        if (!-assertionsDisabled) {
            if (!(c <= DexFormat.MAX_TYPE_IDX)) {
                throw new AssertionError();
            }
        }
        if (c >= CollationSettings.CASE_FIRST_AND_UPPER_MASK) {
            int i = lcccIndex[c >> 5];
            if (i != 0) {
                if ((lcccBits[i] & (1 << (c & 31))) != 0) {
                    return true;
                }
                return false;
            }
        }
        return false;
    }

    public static boolean hasTccc(int c) {
        if (!-assertionsDisabled) {
            if (!(c <= DexFormat.MAX_TYPE_IDX)) {
                throw new AssertionError();
            }
        }
        if (c >= Opcodes.OP_AND_LONG_2ADDR) {
            int i = tcccIndex[c >> 5];
            if (i != 0) {
                if ((tcccBits[i] & (1 << (c & 31))) != 0) {
                    return true;
                }
                return false;
            }
        }
        return false;
    }

    static boolean mayHaveLccc(int c) {
        boolean z = true;
        if (c < CollationSettings.CASE_FIRST_AND_UPPER_MASK) {
            return false;
        }
        if (c > DexFormat.MAX_TYPE_IDX) {
            c = UTF16.getLeadSurrogate(c);
        }
        int i = lcccIndex[c >> 5];
        if (i == 0) {
            z = false;
        } else if ((lcccBits[i] & (1 << (c & 31))) == 0) {
            z = false;
        }
        return z;
    }

    static boolean maybeTibetanCompositeVowel(int c) {
        return (2096897 & c) == 3841;
    }

    static boolean isFCD16OfTibetanCompositeVowel(int fcd16) {
        return fcd16 == 33154 || fcd16 == 33156;
    }
}
