package javax.sip;

/* JADX INFO: Failed to restore enum class, 'enum' modifier removed */
public final class DialogState extends Enum<DialogState> {
    private static final /* synthetic */ DialogState[] $VALUES;
    public static final DialogState CONFIRMED = new DialogState("CONFIRMED", 1);
    public static final DialogState EARLY = new DialogState("EARLY", 0);
    public static final DialogState TERMINATED = new DialogState("TERMINATED", 2);
    public static final int _CONFIRMED = CONFIRMED.ordinal();
    public static final int _EARLY;
    public static final int _TERMINATED = TERMINATED.ordinal();

    private DialogState(String str, int i) {
    }

    public static DialogState valueOf(String name) {
        return (DialogState) Enum.valueOf(DialogState.class, name);
    }

    public static DialogState[] values() {
        return (DialogState[]) $VALUES.clone();
    }

    static {
        DialogState dialogState = EARLY;
        $VALUES = new DialogState[]{dialogState, CONFIRMED, TERMINATED};
        _EARLY = dialogState.ordinal();
    }

    public static DialogState getObject(int state) {
        try {
            return values()[state];
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Invalid dialog state: " + state);
        }
    }

    public int getValue() {
        return ordinal();
    }
}
