package android.app.backup;

import android.app.QueuedWork;
import android.content.Context;
import android.os.ParcelFileDescriptor;

public class SharedPreferencesBackupHelper extends FileBackupHelperBase implements BackupHelper {
    private static final boolean DEBUG = false;
    private static final String TAG = "SharedPreferencesBackupHelper";
    private Context mContext;
    private String[] mPrefGroups;

    public SharedPreferencesBackupHelper(Context context, String... prefGroups) {
        super(context);
        this.mContext = context;
        this.mPrefGroups = prefGroups;
    }

    public void performBackup(ParcelFileDescriptor oldState, BackupDataOutput data, ParcelFileDescriptor newState) {
        Context context = this.mContext;
        QueuedWork.waitToFinish();
        String[] prefGroups = this.mPrefGroups;
        int N = prefGroups.length;
        String[] files = new String[N];
        for (int i = 0; i < N; i++) {
            files[i] = context.getSharedPrefsFile(prefGroups[i]).getAbsolutePath();
        }
        FileBackupHelperBase.performBackup_checked(oldState, data, newState, files, prefGroups);
    }

    public void restoreEntity(BackupDataInputStream data) {
        Context context = this.mContext;
        String key = data.getKey();
        if (isKeyInList(key, this.mPrefGroups)) {
            writeFile(context.getSharedPrefsFile(key).getAbsoluteFile(), data);
        }
    }
}
