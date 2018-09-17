package com.android.server;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Binder;
import android.os.DropBoxManager;
import android.os.FileObserver;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.util.Slog;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;

public class SamplingProfilerService extends Binder {
    private static final boolean LOCAL_LOGV = false;
    public static final String SNAPSHOT_DIR = "/data/snapshots";
    private static final String TAG = "SamplingProfilerService";
    private final Context mContext;
    private FileObserver snapshotObserver;

    /* renamed from: com.android.server.SamplingProfilerService.1 */
    class AnonymousClass1 extends FileObserver {
        final /* synthetic */ DropBoxManager val$dropbox;

        AnonymousClass1(String $anonymous0, int $anonymous1, DropBoxManager val$dropbox) {
            this.val$dropbox = val$dropbox;
            super($anonymous0, $anonymous1);
        }

        public void onEvent(int event, String path) {
            SamplingProfilerService.this.handleSnapshotFile(new File(SamplingProfilerService.SNAPSHOT_DIR, path), this.val$dropbox);
        }
    }

    private class SamplingProfilerSettingsObserver extends ContentObserver {
        private ContentResolver mContentResolver;

        public SamplingProfilerSettingsObserver(ContentResolver contentResolver) {
            super(null);
            this.mContentResolver = contentResolver;
            onChange(SamplingProfilerService.LOCAL_LOGV);
        }

        public void onChange(boolean selfChange) {
            SystemProperties.set("persist.sys.profiler_ms", Integer.valueOf(Global.getInt(this.mContentResolver, "sampling_profiler_ms", 0)).toString());
        }
    }

    public SamplingProfilerService(Context context) {
        this.mContext = context;
        registerSettingObserver(context);
        startWorking(context);
    }

    private void startWorking(Context context) {
        DropBoxManager dropbox = (DropBoxManager) context.getSystemService("dropbox");
        File[] snapshotFiles = new File(SNAPSHOT_DIR).listFiles();
        int i = 0;
        while (snapshotFiles != null && i < snapshotFiles.length) {
            handleSnapshotFile(snapshotFiles[i], dropbox);
            i++;
        }
        this.snapshotObserver = new AnonymousClass1(SNAPSHOT_DIR, 4, dropbox);
        this.snapshotObserver.startWatching();
    }

    private void handleSnapshotFile(File file, DropBoxManager dropbox) {
        try {
            dropbox.addFile(TAG, file, 0);
        } catch (IOException e) {
            Slog.e(TAG, "Can't add " + file.getPath() + " to dropbox", e);
        } finally {
            file.delete();
        }
    }

    private void registerSettingObserver(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        contentResolver.registerContentObserver(Global.getUriFor("sampling_profiler_ms"), LOCAL_LOGV, new SamplingProfilerSettingsObserver(contentResolver));
    }

    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.DUMP", TAG);
        pw.println("SamplingProfilerService:");
        pw.println("Watching directory: /data/snapshots");
    }
}
