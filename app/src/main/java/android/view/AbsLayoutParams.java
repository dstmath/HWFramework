package android.view;

public abstract class AbsLayoutParams {
    public static final int FLAG_DESTORY_SURFACE = 2;
    public static final int FLAG_DISABLE_KNUCKLE_TO_LAUNCH_APP = 4;
    public static final int FLAG_EMUI_LIGHT_STYLE = 16;
    public static final int FLAG_KEYEVENT_PASS_TO_USER_HOME = Integer.MIN_VALUE;
    public static final int FLAG_MMI_TEST_VOLUME_UP_DOWN = 8;
    public static final int FLAG_SHARE_DIALOG = 1;
    public static final int SYSTEM_UI_FLAG_IMMERSIVE_GESTURE_ISOLATED = 512;

    public int getHwFlags() {
        return 0;
    }

    public void addHwFlags(int hwFlags) {
    }

    public void clearHwFlags(int hwFlags) {
    }
}
