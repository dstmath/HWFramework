package com.android.server.pm;

import android.content.pm.ShortcutInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Icon;
import android.os.StrictMode;
import android.os.SystemClock;
import android.util.Log;
import android.util.Slog;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.Preconditions;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Deque;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ShortcutBitmapSaver {
    private static final boolean ADD_DELAY_BEFORE_SAVE_FOR_TEST = false;
    private static final boolean DEBUG = false;
    private static final long SAVE_DELAY_MS_FOR_TEST = 1000;
    private static final String TAG = "ShortcutService";
    private final long SAVE_WAIT_TIMEOUT_MS = 30000;
    private final Executor mExecutor = new ThreadPoolExecutor(0, 1, 60, TimeUnit.SECONDS, new LinkedBlockingQueue());
    @GuardedBy({"mPendingItems"})
    private final Deque<PendingItem> mPendingItems = new LinkedBlockingDeque();
    private final Runnable mRunnable = new Runnable() {
        /* class com.android.server.pm.$$Lambda$ShortcutBitmapSaver$AUDgG57FGyGDUVDAjL7cuiE0pM */

        @Override // java.lang.Runnable
        public final void run() {
            ShortcutBitmapSaver.this.lambda$new$1$ShortcutBitmapSaver();
        }
    };
    private final ShortcutService mService;

    /* access modifiers changed from: private */
    public static class PendingItem {
        public final byte[] bytes;
        private final long mInstantiatedUptimeMillis;
        public final ShortcutInfo shortcut;

        private PendingItem(ShortcutInfo shortcut2, byte[] bytes2) {
            this.shortcut = shortcut2;
            this.bytes = bytes2;
            this.mInstantiatedUptimeMillis = SystemClock.uptimeMillis();
        }

        public String toString() {
            return "PendingItem{size=" + this.bytes.length + " age=" + (SystemClock.uptimeMillis() - this.mInstantiatedUptimeMillis) + "ms shortcut=" + this.shortcut.toInsecureString() + "}";
        }
    }

    public ShortcutBitmapSaver(ShortcutService service) {
        this.mService = service;
    }

    public boolean waitForAllSavesLocked() {
        CountDownLatch latch = new CountDownLatch(1);
        this.mExecutor.execute(new Runnable(latch) {
            /* class com.android.server.pm.$$Lambda$ShortcutBitmapSaver$xgjvZfaiKXavxgGCSta_eIdVBnk */
            private final /* synthetic */ CountDownLatch f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.lang.Runnable
            public final void run() {
                ShortcutBitmapSaver.lambda$waitForAllSavesLocked$0(this.f$0);
            }
        });
        try {
            if (latch.await(30000, TimeUnit.MILLISECONDS)) {
                return true;
            }
            this.mService.wtf("Timed out waiting on saving bitmaps.");
            return false;
        } catch (InterruptedException e) {
            Slog.w(TAG, "interrupted");
            return false;
        }
    }

    public String getBitmapPathMayWaitLocked(ShortcutInfo shortcut) {
        if (!waitForAllSavesLocked() || !shortcut.hasIconFile()) {
            return null;
        }
        return shortcut.getBitmapPath();
    }

    public void removeIcon(ShortcutInfo shortcut) {
        shortcut.setIconResourceId(0);
        shortcut.setIconResName(null);
        shortcut.setBitmapPath(null);
        shortcut.clearFlags(2572);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0096, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:?, code lost:
        r4.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x009b, code lost:
        r7 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x009c, code lost:
        r5.addSuppressed(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x009f, code lost:
        throw r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x00aa, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:?, code lost:
        android.util.Slog.wtf(com.android.server.pm.ShortcutBitmapSaver.TAG, "Unable to write bitmap to file", r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x00b2, code lost:
        android.os.StrictMode.setThreadPolicy(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x00b5, code lost:
        return;
     */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x00aa A[ExcHandler: IOException | OutOfMemoryError | RuntimeException (r3v12 'e' java.lang.Throwable A[CUSTOM_DECLARE]), Splitter:B:14:0x0061] */
    public void saveBitmapLocked(ShortcutInfo shortcut, int maxDimension, Bitmap.CompressFormat format, int quality) {
        Icon icon = shortcut.getIcon();
        Preconditions.checkNotNull(icon);
        Bitmap original = icon.getBitmap();
        if (original == null) {
            Log.e(TAG, "Missing icon: " + shortcut);
            return;
        }
        StrictMode.ThreadPolicy oldPolicy = StrictMode.getThreadPolicy();
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder(oldPolicy).permitCustomSlowCalls().build());
        ShortcutService shortcutService = this.mService;
        Bitmap shrunk = ShortcutService.shrinkBitmap(original, maxDimension);
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream(65536);
            if (!shrunk.compress(format, quality, out)) {
                Slog.wtf(TAG, "Unable to compress bitmap");
            }
            out.flush();
            byte[] bytes = out.toByteArray();
            out.close();
            out.close();
            if (shrunk != original) {
                try {
                    shrunk.recycle();
                } catch (IOException | OutOfMemoryError | RuntimeException e) {
                } catch (Throwable th) {
                    StrictMode.setThreadPolicy(oldPolicy);
                    throw th;
                }
            }
            StrictMode.setThreadPolicy(oldPolicy);
            shortcut.addFlags(2056);
            if (icon.getType() == 5) {
                shortcut.addFlags(512);
            }
            PendingItem item = new PendingItem(shortcut, bytes);
            synchronized (this.mPendingItems) {
                this.mPendingItems.add(item);
            }
            this.mExecutor.execute(this.mRunnable);
        } catch (Throwable th2) {
            if (shrunk != original) {
                shrunk.recycle();
            }
            throw th2;
        }
    }

    public /* synthetic */ void lambda$new$1$ShortcutBitmapSaver() {
        do {
        } while (processPendingItems());
    }

    /* JADX INFO: finally extract failed */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0028, code lost:
        r2 = r3.shortcut;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0030, code lost:
        if (r2.isIconPendingSave() != false) goto L_0x0041;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0038, code lost:
        if (r2.getBitmapPath() != null) goto L_0x003d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x003a, code lost:
        removeIcon(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x003d, code lost:
        r2.clearFlags(2048);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0040, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0041, code lost:
        r5 = r8.mService.openIconFileForWrite(r2.getUserId(), r2);
        r6 = r5.getFile();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:?, code lost:
        r5.write(r3.bytes);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:?, code lost:
        libcore.io.IoUtils.closeQuietly(r5);
        r2.setBitmapPath(r6.getAbsolutePath());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0067, code lost:
        if (r2.getBitmapPath() != null) goto L_0x006c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0069, code lost:
        removeIcon(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x006c, code lost:
        r2.clearFlags(2048);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x006f, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0070, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0071, code lost:
        libcore.io.IoUtils.closeQuietly(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0074, code lost:
        throw r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0075, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0076, code lost:
        android.util.Slog.e(com.android.server.pm.ShortcutBitmapSaver.TAG, "Unable to write bitmap to file", r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x0085, code lost:
        r6.delete();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x008e, code lost:
        if (r2.getBitmapPath() == null) goto L_0x0090;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x0090, code lost:
        removeIcon(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x0093, code lost:
        r2.clearFlags(2048);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x0096, code lost:
        return true;
     */
    private boolean processPendingItems() {
        ShortcutInfo shortcut = null;
        try {
            synchronized (this.mPendingItems) {
                if (this.mPendingItems.size() != 0) {
                    PendingItem item = this.mPendingItems.pop();
                }
            }
            return false;
        } finally {
            if (0 != 0) {
                if (shortcut.getBitmapPath() == null) {
                    removeIcon(null);
                }
                shortcut.clearFlags(2048);
            }
        }
    }

    public void dumpLocked(PrintWriter pw, String prefix) {
        synchronized (this.mPendingItems) {
            int N = this.mPendingItems.size();
            pw.print(prefix);
            pw.println("Pending saves: Num=" + N + " Executor=" + this.mExecutor);
            for (PendingItem item : this.mPendingItems) {
                pw.print(prefix);
                pw.print("  ");
                pw.println(item);
            }
        }
    }
}
