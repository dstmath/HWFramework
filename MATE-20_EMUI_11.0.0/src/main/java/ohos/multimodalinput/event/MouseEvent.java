package ohos.multimodalinput.event;

public abstract class MouseEvent extends CompositeEvent {
    public static final int AXIS_X = 0;
    public static final int AXIS_Y = 1;
    public static final int AXIS_Z = 2;
    public static final int BACK_BUTTON = 8;
    public static final int FORWARD_BUTTON = 16;
    public static final int HOVER_ENTER = 4;
    public static final int HOVER_EXIT = 6;
    public static final int HOVER_MOVE = 5;
    public static final int LEFT_BUTTON = 1;
    public static final int MIDDLE_BUTTON = 4;
    public static final int MOVE = 3;
    public static final int NONE = 0;
    public static final int NONE_BUTTON = 0;
    public static final int PRESS = 1;
    public static final int RELEASE = 2;
    public static final int RIGHT_BUTTON = 2;

    public abstract int getAction();

    public abstract int getActionButton();

    public abstract MmiPoint getCursor();

    public abstract float getCursorDelta(int i);

    public abstract int getPressedButtons();

    public abstract float getScrollingDelta(int i);

    public abstract void setCursorOffset(float f, float f2);
}
