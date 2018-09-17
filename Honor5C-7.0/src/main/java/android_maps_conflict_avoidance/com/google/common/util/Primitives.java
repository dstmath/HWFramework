package android_maps_conflict_avoidance.com.google.common.util;

public class Primitives {
    private static PrimitiveConverter converter;

    public static abstract class PrimitiveConverter {
        public abstract Integer toInteger(int i);

        public abstract Long toLong(long j);
    }

    private static class J2meConverter extends PrimitiveConverter {
        private Integer[] SMALL_INTS;
        private Long[] SMALL_LONGS;

        private J2meConverter() {
            this.SMALL_LONGS = new Long[]{new Long(0), new Long(1), new Long(2), new Long(3), new Long(4), new Long(5), new Long(6), new Long(7), new Long(8), new Long(9), new Long(10), new Long(11), new Long(12), new Long(13), new Long(14), new Long(15)};
            this.SMALL_INTS = new Integer[]{new Integer(0), new Integer(1), new Integer(2), new Integer(3), new Integer(4), new Integer(5), new Integer(6), new Integer(7), new Integer(8), new Integer(9), new Integer(10), new Integer(11), new Integer(12), new Integer(13), new Integer(14), new Integer(15)};
        }

        public Long toLong(long l) {
            Object obj = 1;
            if ((l < 0 ? 1 : null) == null) {
                if (l < ((long) this.SMALL_LONGS.length)) {
                    obj = null;
                }
                if (obj == null) {
                    return this.SMALL_LONGS[(int) l];
                }
            }
            return new Long(l);
        }

        public Integer toInteger(int i) {
            return (i >= 0 && i < this.SMALL_INTS.length) ? this.SMALL_INTS[i] : new Integer(i);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android_maps_conflict_avoidance.com.google.common.util.Primitives.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android_maps_conflict_avoidance.com.google.common.util.Primitives.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android_maps_conflict_avoidance.com.google.common.util.Primitives.<clinit>():void");
    }

    private Primitives() {
    }

    static void resetConverter() {
        converter = new J2meConverter();
    }

    public static Long toLong(long l) {
        return converter.toLong(l);
    }

    public static Integer toInteger(int i) {
        return converter.toInteger(i);
    }
}
