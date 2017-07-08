package java.util.stream;

import java.util.EnumMap;
import java.util.Map;
import java.util.Spliterator;

public enum StreamOpFlag {
    ;
    
    private static final int CLEAR_BITS = 2;
    private static final int FLAG_MASK = 0;
    private static final int FLAG_MASK_IS = 0;
    private static final int FLAG_MASK_NOT = 0;
    public static final int INITIAL_OPS_VALUE = 0;
    public static final int IS_DISTINCT = 0;
    public static final int IS_ORDERED = 0;
    public static final int IS_SHORT_CIRCUIT = 0;
    public static final int IS_SIZED = 0;
    public static final int IS_SORTED = 0;
    public static final int NOT_DISTINCT = 0;
    public static final int NOT_ORDERED = 0;
    public static final int NOT_SIZED = 0;
    public static final int NOT_SORTED = 0;
    public static final int OP_MASK = 0;
    private static final int PRESERVE_BITS = 3;
    private static final int SET_BITS = 1;
    public static final int SPLITERATOR_CHARACTERISTICS_MASK = 0;
    public static final int STREAM_MASK = 0;
    public static final int TERMINAL_OP_MASK = 0;
    public static final int UPSTREAM_TERMINAL_OP_MASK = 0;
    private final int bitPosition;
    private final int clear;
    private final Map<Type, Integer> maskTable;
    private final int preserve;
    private final int set;

    private static class MaskBuilder {
        final Map<Type, Integer> map;

        MaskBuilder(Map<Type, Integer> map) {
            this.map = map;
        }

        MaskBuilder mask(Type t, Integer i) {
            this.map.put(t, i);
            return this;
        }

        MaskBuilder set(Type t) {
            return mask(t, Integer.valueOf((int) StreamOpFlag.SET_BITS));
        }

        MaskBuilder clear(Type t) {
            return mask(t, Integer.valueOf((int) StreamOpFlag.CLEAR_BITS));
        }

        MaskBuilder setAndClear(Type t) {
            return mask(t, Integer.valueOf((int) StreamOpFlag.PRESERVE_BITS));
        }

        Map<Type, Integer> build() {
            Type[] values = Type.values();
            int length = values.length;
            for (int i = StreamOpFlag.OP_MASK; i < length; i += StreamOpFlag.SET_BITS) {
                this.map.putIfAbsent(values[i], Integer.valueOf((int) StreamOpFlag.OP_MASK));
            }
            return this.map;
        }
    }

    enum Type {
        private static final /* synthetic */ Type[] $VALUES = null;
        public static final Type OP = null;
        public static final Type SPLITERATOR = null;
        public static final Type STREAM = null;
        public static final Type TERMINAL_OP = null;
        public static final Type UPSTREAM_TERMINAL_OP = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.stream.StreamOpFlag.Type.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.stream.StreamOpFlag.Type.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.stream.StreamOpFlag.Type.<clinit>():void");
        }

        private Type(String str, int i) {
        }

        public static Type valueOf(String name) {
            return (Type) Enum.valueOf(Type.class, name);
        }

        public static Type[] values() {
            return $VALUES;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.stream.StreamOpFlag.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.stream.StreamOpFlag.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: java.util.stream.StreamOpFlag.<clinit>():void");
    }

    private static int getMask(int r1) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.stream.StreamOpFlag.getMask(int):int
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.util.stream.StreamOpFlag.getMask(int):int");
    }

    public static int toStreamFlags(int r1) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.stream.StreamOpFlag.toStreamFlags(int):int
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.util.stream.StreamOpFlag.toStreamFlags(int):int");
    }

    private static MaskBuilder set(Type t) {
        return new MaskBuilder(new EnumMap(Type.class)).set(t);
    }

    private StreamOpFlag(int position, MaskBuilder maskBuilder) {
        this.maskTable = maskBuilder.build();
        position *= CLEAR_BITS;
        this.bitPosition = position;
        this.set = SET_BITS << position;
        this.clear = CLEAR_BITS << position;
        this.preserve = PRESERVE_BITS << position;
    }

    public int set() {
        return this.set;
    }

    public int clear() {
        return this.clear;
    }

    public boolean isStreamFlag() {
        return ((Integer) this.maskTable.get(Type.STREAM)).intValue() > 0;
    }

    public boolean isKnown(int flags) {
        return (this.preserve & flags) == this.set;
    }

    public boolean isCleared(int flags) {
        return (this.preserve & flags) == this.clear;
    }

    public boolean isPreserved(int flags) {
        return (this.preserve & flags) == this.preserve;
    }

    public boolean canSet(Type t) {
        return (((Integer) this.maskTable.get(t)).intValue() & SET_BITS) > 0;
    }

    private static int createMask(Type t) {
        int mask = OP_MASK;
        StreamOpFlag[] values = values();
        int length = values.length;
        for (int i = OP_MASK; i < length; i += SET_BITS) {
            StreamOpFlag flag = values[i];
            mask |= ((Integer) flag.maskTable.get(t)).intValue() << flag.bitPosition;
        }
        return mask;
    }

    private static int createFlagMask() {
        int mask = OP_MASK;
        StreamOpFlag[] values = values();
        for (int i = OP_MASK; i < values.length; i += SET_BITS) {
            mask |= values[i].preserve;
        }
        return mask;
    }

    public static int combineOpFlags(int newStreamOrOpFlags, int prevCombOpFlags) {
        return (getMask(newStreamOrOpFlags) & prevCombOpFlags) | newStreamOrOpFlags;
    }

    public static int toCharacteristics(int streamFlags) {
        return SPLITERATOR_CHARACTERISTICS_MASK & streamFlags;
    }

    public static int fromCharacteristics(Spliterator<?> spliterator) {
        int characteristics = spliterator.characteristics();
        if ((characteristics & 4) == 0 || spliterator.getComparator() == null) {
            return SPLITERATOR_CHARACTERISTICS_MASK & characteristics;
        }
        return (SPLITERATOR_CHARACTERISTICS_MASK & characteristics) & -5;
    }

    public static int fromCharacteristics(int characteristics) {
        return SPLITERATOR_CHARACTERISTICS_MASK & characteristics;
    }
}
