package android.app.backup;

import android.annotation.UnsupportedAppUsage;
import android.app.WallpaperManager;
import android.content.Context;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.Slog;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class WallpaperBackupHelper extends FileBackupHelperBase implements BackupHelper {
    private static final boolean DEBUG = false;
    private static final String STAGE_FILE = new File(Environment.getUserSystemDirectory(0), "wallpaper-tmp").getAbsolutePath();
    private static final String TAG = "WallpaperBackupHelper";
    public static final String WALLPAPER_IMAGE_KEY = "/data/data/com.android.settings/files/wallpaper";
    public static final String WALLPAPER_INFO_KEY = "/data/system/wallpaper_info.xml";
    private final String[] mKeys;
    private final WallpaperManager mWpm;

    @Override // android.app.backup.FileBackupHelperBase, android.app.backup.BackupHelper
    @UnsupportedAppUsage
    public /* bridge */ /* synthetic */ void writeNewStateDescription(ParcelFileDescriptor parcelFileDescriptor) {
        super.writeNewStateDescription(parcelFileDescriptor);
    }

    public WallpaperBackupHelper(Context context, String[] keys) {
        super(context);
        this.mContext = context;
        this.mKeys = keys;
        this.mWpm = (WallpaperManager) context.getSystemService("wallpaper");
    }

    @Override // android.app.backup.BackupHelper
    public void performBackup(ParcelFileDescriptor oldState, BackupDataOutput data, ParcelFileDescriptor newState) {
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0033, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:?, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0038, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0039, code lost:
        r4.addSuppressed(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x003c, code lost:
        throw r5;
     */
    @Override // android.app.backup.BackupHelper
    public void restoreEntity(BackupDataInputStream data) {
        String key = data.getKey();
        if (isKeyInList(key, this.mKeys) && key.equals(WALLPAPER_IMAGE_KEY)) {
            File stage = new File(STAGE_FILE);
            try {
                if (writeFile(stage, data)) {
                    try {
                        FileInputStream in = new FileInputStream(stage);
                        this.mWpm.setStream(in);
                        in.close();
                    } catch (IOException e) {
                        Slog.e(TAG, "Unable to set restored wallpaper: " + e.getMessage());
                    }
                } else {
                    Slog.e(TAG, "Unable to save restored wallpaper");
                }
            } finally {
                stage.delete();
            }
        }
    }
}
