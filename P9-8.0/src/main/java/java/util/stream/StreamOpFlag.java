package java.util.stream;

import java.util.EnumMap;
import java.util.Map;
import java.util.Spliterator;

public enum StreamOpFlag {
    DISTINCT(0, set(Type.SPLITERATOR).set(Type.STREAM).setAndClear(Type.OP)),
    SORTED(1, set(Type.SPLITERATOR).set(Type.STREAM).setAndClear(Type.OP)),
    ORDERED(2, set(Type.SPLITERATOR).set(Type.STREAM).setAndClear(Type.OP).clear(Type.TERMINAL_OP).clear(Type.UPSTREAM_TERMINAL_OP)),
    SIZED(3, set(Type.SPLITERATOR).set(Type.STREAM).clear(Type.OP)),
    SHORT_CIRCUIT(12, set(Type.OP).set(Type.TERMINAL_OP));
    
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
            return mask(t, Integer.valueOf(1));
        }

        MaskBuilder clear(Type t) {
            return mask(t, Integer.valueOf(2));
        }

        MaskBuilder setAndClear(Type t) {
            return mask(t, Integer.valueOf(3));
        }

        Map<Type, Integer> build() {
            for (Type t : Type.values()) {
                this.map.putIfAbsent(t, Integer.valueOf(0));
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

        private Type(String str, int i) {
        }

        public static Type valueOf(String name) {
            return (Type) Enum.valueOf(Type.class, name);
        }

        public static Type[] values() {
            return $VALUES;
        }

        static {
            SPLITERATOR = new Type("SPLITERATOR", 0);
            STREAM = new Type("STREAM", 1);
            OP = new Type("OP", 2);
            TERMINAL_OP = new Type("TERMINAL_OP", 3);
            UPSTREAM_TERMINAL_OP = new Type("UPSTREAM_TERMINAL_OP", 4);
            $VALUES = new Type[]{SPLITERATOR, STREAM, OP, TERMINAL_OP, UPSTREAM_TERMINAL_OP};
        }
    }

    static {
        SPLITERATOR_CHARACTERISTICS_MASK = createMask(Type.SPLITERATOR);
        STREAM_MASK = createMask(Type.STREAM);
        OP_MASK = createMask(Type.OP);
        TERMINAL_OP_MASK = createMask(Type.TERMINAL_OP);
        UPSTREAM_TERMINAL_OP_MASK = createMask(Type.UPSTREAM_TERMINAL_OP);
        FLAG_MASK = createFlagMask();
        FLAG_MASK_IS = STREAM_MASK;
        FLAG_MASK_NOT = STREAM_MASK << 1;
        INITIAL_OPS_VALUE = FLAG_MASK_IS | FLAG_MASK_NOT;
        IS_DISTINCT = DISTINCT.set;
        NOT_DISTINCT = DISTINCT.clear;
        IS_SORTED = SORTED.set;
        NOT_SORTED = SORTED.clear;
        IS_ORDERED = ORDERED.set;
        NOT_ORDERED = ORDERED.clear;
        IS_SIZED = SIZED.set;
        NOT_SIZED = SIZED.clear;
        IS_SHORT_CIRCUIT = SHORT_CIRCUIT.set;
    }

    private static MaskBuilder set(Type t) {
        return new MaskBuilder(new EnumMap(Type.class)).set(t);
    }

    private StreamOpFlag(int position, MaskBuilder maskBuilder) {
        this.maskTable = maskBuilder.build();
        position *= 2;
        this.bitPosition = position;
        this.set = 1 << position;
        this.clear = 2 << position;
        this.preserve = 3 << position;
    }

    public int set() {
        return this.set;
    }

    public int clear() {
        return this.clear;
    }

    public boolean isStreamFlag() {
        return ((Integer) this.maskTable.get(Type.STREAM)).lambda$-java_util_stream_IntPipeline_14709() > 0;
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
        return (((Integer) this.maskTable.get(t)).lambda$-java_util_stream_IntPipeline_14709() & 1) > 0;
    }

    private static int createMask(Type t) {
        int mask = 0;
        for (StreamOpFlag flag : values()) {
            mask |= ((Integer) flag.maskTable.get(t)).lambda$-java_util_stream_IntPipeline_14709() << flag.bitPosition;
        }
        return mask;
    }

    private static int createFlagMask() {
        int mask = 0;
        for (StreamOpFlag flag : values()) {
            mask |= flag.preserve;
        }
        return mask;
    }

    private static int getMask(int flags) {
        if (flags == 0) {
            return FLAG_MASK;
        }
        return ~((((FLAG_MASK_IS & flags) << 1) | flags) | ((FLAG_MASK_NOT & flags) >> 1));
    }

    public static int combineOpFlags(int newStreamOrOpFlags, int prevCombOpFlags) {
        return (getMask(newStreamOrOpFlags) & prevCombOpFlags) | newStreamOrOpFlags;
    }

    public static int toStreamFlags(int combOpFlags) {
        return (((~combOpFlags) >> 1) & FLAG_MASK_IS) & combOpFlags;
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
