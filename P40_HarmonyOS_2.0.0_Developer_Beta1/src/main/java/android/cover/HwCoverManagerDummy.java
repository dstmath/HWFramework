package android.cover;

public class HwCoverManagerDummy implements IHwCoverManager {
    private static HwCoverManagerDummy sSelf = null;

    public static HwCoverManagerDummy getDefault() {
        if (sSelf == null) {
            sSelf = new HwCoverManagerDummy();
        }
        return sSelf;
    }

    @Override // android.cover.IHwCoverManager
    public boolean isCoverOpen() {
        return true;
    }

    @Override // android.cover.IHwCoverManager
    public boolean setCoverForbiddened(boolean isForbiddened) {
        return false;
    }

    @Override // android.cover.IHwCoverManager
    public int getHallState(int hallType) {
        return -1;
    }

    @Override // android.cover.IHwCoverManager
    public boolean registerHallCallback(String receiverName, int hallType, IHallCallback callback) {
        return false;
    }

    @Override // android.cover.IHwCoverManager
    public boolean unRegisterHallCallback(String receiverName, int hallType) {
        return false;
    }

    @Override // android.cover.IHwCoverManager
    public boolean unRegisterHallCallbackEx(int hallType, IHallCallback callback) {
        return false;
    }
}
