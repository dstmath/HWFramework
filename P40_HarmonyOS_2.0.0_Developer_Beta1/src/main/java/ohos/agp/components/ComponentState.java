package ohos.agp.components;

public class ComponentState {
    public static final int COMPONENT_STATE_CHECKED = 64;
    public static final int COMPONENT_STATE_DISABLED = 32;
    public static final int COMPONENT_STATE_EMPTY = 0;
    public static final int COMPONENT_STATE_FOCUSED = 2;
    public static final int COMPONENT_STATE_HOVERED = 268435456;
    public static final int COMPONENT_STATE_PRESSED = 16384;
    public static final int COMPONENT_STATE_SELECTED = 4;

    public static boolean isStateMatched(int i, int i2) {
        return i == i2 || (i & i2) != 0;
    }
}
