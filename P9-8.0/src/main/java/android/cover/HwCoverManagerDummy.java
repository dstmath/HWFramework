package android.cover;

public class HwCoverManagerDummy implements IHwCoverManager {
    private static HwCoverManagerDummy sSelf = null;

    public static HwCoverManagerDummy getDefault() {
        if (sSelf == null) {
            sSelf = new HwCoverManagerDummy();
        }
        return sSelf;
    }

    public boolean isCoverOpen() {
        return true;
    }

    public boolean setCoverForbiddened(boolean forbiddened) {
        return false;
    }
}
