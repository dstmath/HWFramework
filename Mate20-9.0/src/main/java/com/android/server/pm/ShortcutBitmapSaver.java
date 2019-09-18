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
import java.io.File;
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
    private final Executor mExecutor;
    @GuardedBy("mPendingItems")
    private final Deque<PendingItem> mPendingItems;
    private final Runnable mRunnable;
    private final ShortcutService mService;

    private static class PendingItem {
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
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(0, 1, 60, TimeUnit.SECONDS, new LinkedBlockingQueue());
        this.mExecutor = threadPoolExecutor;
        this.mPendingItems = new LinkedBlockingDeque();
        this.mRunnable = new Runnable() {
            public final void run() {
                ShortcutBitmapSaver.lambda$new$1(ShortcutBitmapSaver.this);
            }
        };
        this.mService = service;
    }

    public boolean waitForAllSavesLocked() {
        CountDownLatch latch = new CountDownLatch(1);
        this.mExecutor.execute(new Runnable(latch) {
            private final /* synthetic */ CountDownLatch f$0;

            {
                this.f$0 = r1;
            }

            public final void run() {
                this.f$0.countDown();
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

    public void saveBitmapLocked(ShortcutInfo shortcut, int maxDimension, Bitmap.CompressFormat format, int quality) {
        ByteArrayOutputStream out;
        Icon icon = shortcut.getIcon();
        Preconditions.checkNotNull(icon);
        Bitmap original = icon.getBitmap();
        if (original == null) {
            Log.e(TAG, "Missing icon: " + shortcut);
            return;
        }
        StrictMode.ThreadPolicy oldPolicy = StrictMode.getThreadPolicy();
        try {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder(oldPolicy).permitCustomSlowCalls().build());
            ShortcutService shortcutService = this.mService;
            Bitmap shrunk = ShortcutService.shrinkBitmap(original, maxDimension);
            try {
                out = new ByteArrayOutputStream(65536);
                if (!shrunk.compress(format, quality, out)) {
                    Slog.wtf(TAG, "Unable to compress bitmap");
                }
                out.flush();
                byte[] bytes = out.toByteArray();
                out.close();
                out.close();
                byte[] bytes2 = bytes;
                if (shrunk != original) {
                    shrunk.recycle();
                }
                StrictMode.setThreadPolicy(oldPolicy);
                shortcut.addFlags(2056);
                if (icon.getType() == 5) {
                    shortcut.addFlags(512);
                }
                PendingItem item = new PendingItem(shortcut, bytes2);
                synchronized (this.mPendingItems) {
                    this.mPendingItems.add(item);
                }
                this.mExecutor.execute(this.mRunnable);
                return;
            } catch (Throwable th) {
                if (shrunk != original) {
                    shrunk.recycle();
                }
                throw th;
            }
        } catch (IOException | OutOfMemoryError | RuntimeException e) {
            try {
                Slog.wtf(TAG, "Unable to write bitmap to file", e);
                return;
            } finally {
                StrictMode.setThreadPolicy(oldPolicy);
            }
        }
        throw th;
    }

    public static /* synthetic */ void lambda$new$1(ShortcutBitmapSaver shortcutBitmapSaver) {
        do {
        } while (shortcutBitmapSaver.processPendingItems());
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0017, code lost:
        if (r1.getBitmapPath() != null) goto L_0x001c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0019, code lost:
        removeIcon(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x001c, code lost:
        r1.clearFlags(2048);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x001f, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:?, code lost:
        r1 = r4.shortcut;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0031, code lost:
        if (r1.isIconPendingSave() != false) goto L_0x0044;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0034, code lost:
        if (r1 == null) goto L_0x0042;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x003a, code lost:
        if (r1.getBitmapPath() != null) goto L_0x003f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x003c, code lost:
        removeIcon(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x003f, code lost:
        r1.clearFlags(2048);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0042, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:?, code lost:
        r3 = r8.mService.openIconFileForWrite(r1.getUserId(), r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0052, code lost:
        r0 = r3.getFile();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:?, code lost:
        r3.write(r4.bytes);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:?, code lost:
        libcore.io.IoUtils.closeQuietly(r3);
        r1.setBitmapPath(r0.getAbsolutePath());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0064, code lost:
        if (r1 == null) goto L_0x0072;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x006a, code lost:
        if (r1.getBitmapPath() != null) goto L_0x006f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x006c, code lost:
        removeIcon(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x006f, code lost:
        r1.clearFlags(2048);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x0072, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x0073, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:?, code lost:
        libcore.io.IoUtils.closeQuietly(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x0077, code lost:
        throw r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x0078, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:?, code lost:
        android.util.Slog.e(TAG, "Unable to write bitmap to file", r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x0088, code lost:
        r0.delete();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x008c, code lost:
        if (r1 != null) goto L_0x008e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x0092, code lost:
        if (r1.getBitmapPath() == null) goto L_0x0094;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x0094, code lost:
        removeIcon(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x0097, code lost:
        r1.clearFlags(2048);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x009a, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0011, code lost:
        if (r1 == null) goto L_0x001f;
     */
    private boolean processPendingItems() {
        File file = null;
        ShortcutInfo shortcut = null;
        try {
            synchronized (this.mPendingItems) {
                if (this.mPendingItems.size() != 0) {
                    PendingItem item = this.mPendingItems.pop();
                }
            }
        } catch (Throwable th) {
            if (shortcut != null) {
                if (shortcut.getBitmapPath() == null) {
                    removeIcon(shortcut);
                }
                shortcut.clearFlags(2048);
            }
            throw th;
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
