package android.cover;

public interface IHwCoverManager {
    public static final int TYPE_COVER_SCREEN = 2100;
    public static final int TYPE_COVER_SCREEN_ITEM = 2101;

    int getHallState(int i);

    boolean isCoverOpen();

    boolean registerHallCallback(String str, int i, IHallCallback iHallCallback);

    boolean setCoverForbiddened(boolean z);

    boolean unRegisterHallCallback(String str, int i);

    boolean unRegisterHallCallbackEx(int i, IHallCallback iHallCallback);
}
