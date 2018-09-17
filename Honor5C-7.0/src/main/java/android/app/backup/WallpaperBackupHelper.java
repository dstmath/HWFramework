package android.app.backup;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Point;
import android.hwtheme.HwThemeManager;
import android.os.ParcelFileDescriptor;
import android.rms.AppAssociate;
import android.util.Slog;
import android.view.Display;
import android.view.WindowManager;
import java.io.File;

public class WallpaperBackupHelper extends FileBackupHelperBase implements BackupHelper {
    private static final boolean DEBUG = false;
    private static final double MAX_HEIGHT_RATIO = 1.35d;
    private static final double MIN_HEIGHT_RATIO = 0.0d;
    private static final boolean REJECT_OUTSIZED_RESTORE = true;
    private static final String STAGE_FILE = null;
    private static final String TAG = "WallpaperBackupHelper";
    public static final String WALLPAPER_IMAGE = null;
    public static final String WALLPAPER_IMAGE_KEY = "/data/data/com.android.settings/files/wallpaper";
    public static final String WALLPAPER_INFO = null;
    public static final String WALLPAPER_INFO_KEY = "/data/system/wallpaper_info.xml";
    Context mContext;
    double mDesiredMinHeight;
    double mDesiredMinWidth;
    String[] mFiles;
    String[] mKeys;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.app.backup.WallpaperBackupHelper.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.app.backup.WallpaperBackupHelper.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.app.backup.WallpaperBackupHelper.<clinit>():void");
    }

    public /* bridge */ /* synthetic */ void writeNewStateDescription(ParcelFileDescriptor fd) {
        super.writeNewStateDescription(fd);
    }

    public WallpaperBackupHelper(Context context, String[] files, String[] keys) {
        super(context);
        this.mContext = context;
        this.mFiles = files;
        this.mKeys = keys;
        WallpaperManager wpm = (WallpaperManager) context.getSystemService(HwThemeManager.TAG_WALLPAPER);
        Display d = ((WindowManager) context.getSystemService(AppAssociate.ASSOC_WINDOW)).getDefaultDisplay();
        Point size = new Point();
        d.getSize(size);
        this.mDesiredMinWidth = (double) Math.min(size.x, size.y);
        this.mDesiredMinHeight = (double) wpm.getDesiredMinimumHeight();
        if (this.mDesiredMinHeight <= MIN_HEIGHT_RATIO) {
            this.mDesiredMinHeight = (double) size.y;
        }
    }

    public void performBackup(ParcelFileDescriptor oldState, BackupDataOutput data, ParcelFileDescriptor newState) {
        FileBackupHelperBase.performBackup_checked(oldState, data, newState, this.mFiles, this.mKeys);
    }

    public void restoreEntity(BackupDataInputStream data) {
        String key = data.getKey();
        if (isKeyInList(key, this.mKeys)) {
            if (key.equals(WALLPAPER_IMAGE_KEY)) {
                File f = new File(STAGE_FILE);
                if (writeFile(f, data)) {
                    Options options = new Options();
                    options.inJustDecodeBounds = REJECT_OUTSIZED_RESTORE;
                    BitmapFactory.decodeFile(STAGE_FILE, options);
                    double heightRatio = this.mDesiredMinHeight / ((double) options.outHeight);
                    if (((double) options.outWidth) < this.mDesiredMinWidth || ((double) options.outHeight) < this.mDesiredMinWidth || heightRatio >= MAX_HEIGHT_RATIO || heightRatio <= MIN_HEIGHT_RATIO) {
                        Slog.i(TAG, "Restored image dimensions (w=" + options.outWidth + ", h=" + options.outHeight + ") too far off target (tw=" + this.mDesiredMinWidth + ", th=" + this.mDesiredMinHeight + "); falling back to default wallpaper.");
                        f.delete();
                    }
                }
            } else if (key.equals(WALLPAPER_INFO_KEY)) {
                writeFile(new File(WALLPAPER_INFO), data);
            }
        }
    }

    public void onRestoreFinished() {
        File f = new File(STAGE_FILE);
        if (f.exists()) {
            Slog.d(TAG, "Applying restored wallpaper image.");
            f.renameTo(new File(WALLPAPER_IMAGE));
        }
    }
}
