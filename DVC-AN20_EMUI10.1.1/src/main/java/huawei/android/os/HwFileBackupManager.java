package huawei.android.os;

public class HwFileBackupManager {
    private static final String TAG = "HwFileBackupManager";
    private static volatile HwFileBackupManager mInstance = null;

    public static synchronized HwFileBackupManager getInstance() {
        HwFileBackupManager hwFileBackupManager;
        synchronized (HwFileBackupManager.class) {
            if (mInstance == null) {
                mInstance = new HwFileBackupManager();
            }
            hwFileBackupManager = mInstance;
        }
        return hwFileBackupManager;
    }

    public void startFileBackup() {
        HwGeneralManager.getInstance().startFileBackup();
    }
}
