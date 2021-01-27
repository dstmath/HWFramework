package ohos.multimodalinput.event;

public abstract class StylusEvent extends ManipulationEvent {
    public static final int BUTTON_PRESS = 1;
    public static final int BUTTON_RELEASE = 2;
    public static final int FIRST_BUTTON = 1;
    public static final int NONE = 0;
    public static final int NONE_BUTTON = 0;
    public static final int STYLUS_DOWN = 3;
    public static final int STYLUS_MOVE = 4;
    public static final int STYLUS_UP = 5;

    public abstract int getAction();

    public abstract int getButtons();
}
