package android.app.backup;

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

    public /* bridge */ /* synthetic */ void writeNewStateDescription(ParcelFileDescriptor parcelFileDescriptor) {
        super.writeNewStateDescription(parcelFileDescriptor);
    }

    public WallpaperBackupHelper(Context context, String[] keys) {
        super(context);
        this.mContext = context;
        this.mKeys = keys;
        this.mWpm = (WallpaperManager) context.getSystemService("wallpaper");
    }

    public void performBackup(ParcelFileDescriptor oldState, BackupDataOutput data, ParcelFileDescriptor newState) {
    }

    public void restoreEntity(BackupDataInputStream data) {
        FileInputStream in;
        String key = data.getKey();
        if (isKeyInList(key, this.mKeys) && key.equals(WALLPAPER_IMAGE_KEY)) {
            File stage = new File(STAGE_FILE);
            try {
                if (writeFile(stage, data)) {
                    try {
                        in = new FileInputStream(stage);
                        this.mWpm.setStream(in);
                        in.close();
                    } catch (IOException e) {
                        Slog.e(TAG, "Unable to set restored wallpaper: " + e.getMessage());
                    } catch (Throwable th) {
                        r3.addSuppressed(th);
                    }
                } else {
                    Slog.e(TAG, "Unable to save restored wallpaper");
                }
                return;
            } finally {
                stage.delete();
            }
        } else {
            return;
        }
        throw th;
    }
}
