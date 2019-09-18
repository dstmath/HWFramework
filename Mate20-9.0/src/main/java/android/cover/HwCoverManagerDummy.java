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

    public int getHallState(int hallType) {
        return -1;
    }

    public boolean registerHallCallback(String receiverName, int hallType, IHallCallback callback) {
        return false;
    }

    public boolean unRegisterHallCallback(String receiverName, int hallType) {
        return false;
    }

    public boolean unRegisterHallCallbackEx(int hallType, IHallCallback callback) {
        return false;
    }
}
