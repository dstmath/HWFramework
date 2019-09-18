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
        DIALECT_HANDLING,
        CAPITALIZATION,
        DISPLAY_LENGTH,
        SUBSTITUTE_HANDLING
    }

    private DisplayContext(Type type2, int value2) {
        this.type = type2;
        this.value = value2;
    }

    public Type type() {
        return this.type;
    }

    public int value() {
        return this.value;
    }
}
