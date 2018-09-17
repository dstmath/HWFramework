package android.app.backup;

import android.content.Context;
import android.os.ParcelFileDescriptor;
import java.io.File;

public class FileBackupHelper extends FileBackupHelperBase implements BackupHelper {
    private static final boolean DEBUG = false;
    private static final String TAG = "FileBackupHelper";
    Context mContext;
    String[] mFiles;
    File mFilesDir;

    public FileBackupHelper(Context context, String... files) {
        super(context);
        this.mContext = context;
        this.mFilesDir = context.getFilesDir();
        this.mFiles = files;
    }

    public void performBackup(ParcelFileDescriptor oldState, BackupDataOutput data, ParcelFileDescriptor newState) {
        String[] files = this.mFiles;
        File base = this.mContext.getFilesDir();
        int N = files.length;
        String[] fullPaths = new String[N];
        for (int i = 0; i < N; i++) {
            fullPaths[i] = new File(base, files[i]).getAbsolutePath();
        }
        FileBackupHelperBase.performBackup_checked(oldState, data, newState, fullPaths, files);
    }

    public void restoreEntity(BackupDataInputStream data) {
        String key = data.getKey();
        if (isKeyInList(key, this.mFiles)) {
            writeFile(new File(this.mFilesDir, key), data);
        }
    }
}
