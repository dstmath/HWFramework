package huawei.android.os;

public class HwFileBackupManager {
    private static final String TAG = "HwFileBackupManager";
    private static HwFileBackupManager sInstance = null;

    public static synchronized HwFileBackupManager getInstance() {
        HwFileBackupManager hwFileBackupManager;
        synchronized (HwFileBackupManager.class) {
            if (sInstance == null) {
                sInstance = new HwFileBackupManager();
            }
            hwFileBackupManager = sInstance;
        }
        return hwFileBackupManager;
    }

    public void startFileBackup() {
        HwGeneralManager.getInstance().startFileBackup();
    }
}
