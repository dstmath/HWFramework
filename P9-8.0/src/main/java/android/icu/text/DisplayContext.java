package android.icu.text;

public enum DisplayContext {
    STANDARD_NAMES(Type.DIALECT_HANDLING, 0),
    DIALECT_NAMES(Type.DIALECT_HANDLING, 1),
    CAPITALIZATION_NONE(Type.CAPITALIZATION, 0),
    CAPITALIZATION_FOR_MIDDLE_OF_SENTENCE(Type.CAPITALIZATION, 1),
    CAPITALIZATION_FOR_BEGINNING_OF_SENTENCE(Type.CAPITALIZATION, 2),
    CAPITALIZATION_FOR_UI_LIST_OR_MENU(Type.CAPITALIZATION, 3),
    CAPITALIZATION_FOR_STANDALONE(Type.CAPITALIZATION, 4),
    LENGTH_FULL(Type.DISPLAY_LENGTH, 0),
    LENGTH_SHORT(Type.DISPLAY_LENGTH, 1),
    SUBSTITUTE(Type.SUBSTITUTE_HANDLING, 0),
    NO_SUBSTITUTE(Type.SUBSTITUTE_HANDLING, 1);
    
    private final Type type;
    private final int value;

    public enum Type {
        private static final /* synthetic */ Type[] $VALUES = null;
        public static final Type CAPITALIZATION = null;
        public static final Type DIALECT_HANDLING = null;
        public static final Type DISPLAY_LENGTH = null;
        public static final Type SUBSTITUTE_HANDLING = null;

        private Type(String str, int i) {
        }

        public static Type valueOf(String name) {
            return (Type) Enum.valueOf(Type.class, name);
        }

        public static Type[] values() {
            return $VALUES;
        }

        static {
            DIALECT_HANDLING = new Type("DIALECT_HANDLING", 0);
            CAPITALIZATION = new Type("CAPITALIZATION", 1);
            DISPLAY_LENGTH = new Type("DISPLAY_LENGTH", 2);
            SUBSTITUTE_HANDLING = new Type("SUBSTITUTE_HANDLING", 3);
            $VALUES = new Type[]{DIALECT_HANDLING, CAPITALIZATION, DISPLAY_LENGTH, SUBSTITUTE_HANDLING};
        }
    }

    private DisplayContext(Type type, int value) {
        this.type = type;
        this.value = value;
    }

    public Type type() {
        return this.type;
    }

    public int value() {
        return this.value;
    }
}
