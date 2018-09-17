package android.view;

public class SoundEffectConstants {
    public static final int CLICK = 0;
    public static final int NAVIGATION_DOWN = 4;
    public static final int NAVIGATION_LEFT = 1;
    public static final int NAVIGATION_RIGHT = 3;
    public static final int NAVIGATION_UP = 2;

    private SoundEffectConstants() {
    }

    public static int getContantForFocusDirection(int direction) {
        switch (direction) {
            case 1:
            case 33:
                return 2;
            case 2:
            case 130:
                return 4;
            case 17:
                return 1;
            case 66:
                return 3;
            default:
                throw new IllegalArgumentException("direction must be one of {FOCUS_UP, FOCUS_DOWN, FOCUS_LEFT, FOCUS_RIGHT, FOCUS_FORWARD, FOCUS_BACKWARD}.");
        }
    }
}
