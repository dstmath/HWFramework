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
        if (direction == 17) {
            return 1;
        }
        if (direction != 33) {
            if (direction == 66) {
                return 3;
            }
            if (direction != 130) {
                switch (direction) {
                    case 1:
                        break;
                    case 2:
                        break;
                    default:
                        throw new IllegalArgumentException("direction must be one of {FOCUS_UP, FOCUS_DOWN, FOCUS_LEFT, FOCUS_RIGHT, FOCUS_FORWARD, FOCUS_BACKWARD}.");
                }
            }
            return 4;
        }
        return 2;
    }
}
