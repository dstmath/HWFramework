package android.cover;

public interface IHwCoverManager {
    public static final int TYPE_COVER_SCREEN = 2100;
    public static final int TYPE_COVER_SCREEN_ITEM = 2101;

    boolean isCoverOpen();

    boolean setCoverForbiddened(boolean z);
}
