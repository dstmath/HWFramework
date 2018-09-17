package com.android.server.pm;

import android.content.pm.ShortcutInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.Icon;
import android.os.SystemClock;
import android.util.Log;
import android.util.Slog;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.Preconditions;
import com.android.server.pm.-$Lambda$hS1mIPNPrUgj3Ey9GdylMJh-bQA.AnonymousClass1;
import java.io.ByteArrayOutputStream;
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
    @GuardedBy("mPendingItems")
    private final Deque<PendingItem> mPendingItems = new LinkedBlockingDeque();
    private final Runnable mRunnable = new -$Lambda$hS1mIPNPrUgj3Ey9GdylMJh-bQA(this);
    private final ShortcutService mService;

    private static class PendingItem {
        public final byte[] bytes;
        private final long mInstantiatedUptimeMillis;
        public final ShortcutInfo shortcut;

        /* synthetic */ PendingItem(ShortcutInfo shortcut, byte[] bytes, PendingItem -this2) {
            this(shortcut, bytes);
        }

        private PendingItem(ShortcutInfo shortcut, byte[] bytes) {
            this.shortcut = shortcut;
            this.bytes = bytes;
            this.mInstantiatedUptimeMillis = SystemClock.uptimeMillis();
        }

        public String toString() {
            return "PendingItem{size=" + this.bytes.length + " age=" + (SystemClock.uptimeMillis() - this.mInstantiatedUptimeMillis) + "ms" + " shortcut=" + this.shortcut.toInsecureString() + "}";
        }
    }

    public ShortcutBitmapSaver(ShortcutService service) {
        this.mService = service;
    }

    public boolean waitForAllSavesLocked() {
        CountDownLatch latch = new CountDownLatch(1);
        this.mExecutor.execute(new AnonymousClass1(latch));
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
        if (waitForAllSavesLocked() && shortcut.hasIconFile()) {
            return shortcut.getBitmapPath();
        }
        return null;
    }

    public void removeIcon(ShortcutInfo shortcut) {
        shortcut.setIconResourceId(0);
        shortcut.setIconResName(null);
        shortcut.setBitmapPath(null);
        shortcut.clearFlags(2572);
    }

    /* JADX WARNING: Removed duplicated region for block: B:26:0x0066 A:{Splitter: B:4:0x0028, ExcHandler: java.io.IOException (r3_0 'e' java.lang.Throwable)} */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0066 A:{Splitter: B:4:0x0028, ExcHandler: java.io.IOException (r3_0 'e' java.lang.Throwable)} */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x007b A:{SYNTHETIC, Splitter: B:36:0x007b} */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x008e A:{Catch:{ Throwable -> 0x0083, all -> 0x0081 }} */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x0080 A:{SYNTHETIC, Splitter: B:39:0x0080} */
    /* JADX WARNING: Missing block: B:26:0x0066, code:
            r3 = move-exception;
     */
    /* JADX WARNING: Missing block: B:27:0x0067, code:
            android.util.Slog.wtf(TAG, "Unable to write bitmap to file", r3);
     */
    /* JADX WARNING: Missing block: B:28:0x0070, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void saveBitmapLocked(ShortcutInfo shortcut, int maxDimension, CompressFormat format, int quality) {
        Throwable th;
        Icon icon = shortcut.getIcon();
        Preconditions.checkNotNull(icon);
        Bitmap original = icon.getBitmap();
        if (original == null) {
            Log.e(TAG, "Missing icon: " + shortcut);
            return;
        }
        try {
            ShortcutService shortcutService = this.mService;
            Bitmap shrunk = ShortcutService.shrinkBitmap(original, maxDimension);
            Throwable th2 = null;
            ByteArrayOutputStream out = null;
            try {
                ByteArrayOutputStream out2 = new ByteArrayOutputStream(65536);
                try {
                    if (!shrunk.compress(format, quality, out2)) {
                        Slog.wtf(TAG, "Unable to compress bitmap");
                    }
                    out2.flush();
                    byte[] bytes = out2.toByteArray();
                    out2.close();
                    if (out2 != null) {
                        try {
                            out2.close();
                        } catch (Throwable th3) {
                            th2 = th3;
                        }
                    }
                    if (th2 != null) {
                        try {
                            throw th2;
                        } catch (Throwable th4) {
                            th = th4;
                            out = out2;
                        }
                    } else {
                        if (shrunk != original) {
                            shrunk.recycle();
                        }
                        shortcut.addFlags(2056);
                        if (icon.getType() == 5) {
                            shortcut.addFlags(512);
                        }
                        PendingItem item = new PendingItem(shortcut, bytes, null);
                        synchronized (this.mPendingItems) {
                            this.mPendingItems.add(item);
                        }
                        this.mExecutor.execute(this.mRunnable);
                    }
                } catch (Throwable th5) {
                    th = th5;
                    out = out2;
                    if (out != null) {
                        try {
                            out.close();
                        } catch (Throwable th6) {
                            th = th6;
                            if (shrunk != original) {
                                shrunk.recycle();
                            }
                            throw th;
                        }
                    }
                    if (th2 == null) {
                        throw th2;
                    }
                    throw th;
                }
            } catch (Throwable th7) {
                th = th7;
                if (out != null) {
                }
                if (th2 == null) {
                }
            }
        } catch (Throwable e) {
        }
    }

    /* synthetic */ void lambda$-com_android_server_pm_ShortcutBitmapSaver_7645() {
        do {
        } while (processPendingItems());
    }

    /* JADX WARNING: Removed duplicated region for block: B:44:0x0077 A:{Splitter: B:28:0x0046, ExcHandler: java.io.IOException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:14:0x001b, code:
            r4 = r2.shortcut;
     */
    /* JADX WARNING: Missing block: B:15:0x0021, code:
            if (r4.isIconPendingSave() != false) goto L_0x0045;
     */
    /* JADX WARNING: Missing block: B:27:0x0045, code:
            r1 = null;
     */
    /* JADX WARNING: Missing block: B:29:?, code:
            r3 = r10.mService.openIconFileForWrite(r4.getUserId(), r4);
            r1 = r3.getFile();
     */
    /* JADX WARNING: Missing block: B:31:?, code:
            r3.write(r2.bytes);
     */
    /* JADX WARNING: Missing block: B:33:?, code:
            libcore.io.IoUtils.closeQuietly(r3);
            r4.setBitmapPath(r1.getAbsolutePath());
     */
    /* JADX WARNING: Missing block: B:34:0x0063, code:
            if (r4 == null) goto L_0x0071;
     */
    /* JADX WARNING: Missing block: B:36:0x0069, code:
            if (r4.getBitmapPath() != null) goto L_0x006e;
     */
    /* JADX WARNING: Missing block: B:37:0x006b, code:
            removeIcon(r4);
     */
    /* JADX WARNING: Missing block: B:38:0x006e, code:
            r4.clearFlags(2048);
     */
    /* JADX WARNING: Missing block: B:39:0x0071, code:
            return true;
     */
    /* JADX WARNING: Missing block: B:42:?, code:
            libcore.io.IoUtils.closeQuietly(r3);
     */
    /* JADX WARNING: Missing block: B:44:0x0077, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:46:?, code:
            android.util.Slog.e(TAG, "Unable to write bitmap to file", r0);
     */
    /* JADX WARNING: Missing block: B:50:0x0089, code:
            r1.delete();
     */
    /* JADX WARNING: Missing block: B:51:0x008c, code:
            if (r4 != null) goto L_0x008e;
     */
    /* JADX WARNING: Missing block: B:53:0x0092, code:
            if (r4.getBitmapPath() == null) goto L_0x0094;
     */
    /* JADX WARNING: Missing block: B:54:0x0094, code:
            removeIcon(r4);
     */
    /* JADX WARNING: Missing block: B:55:0x0097, code:
            r4.clearFlags(2048);
     */
    /* JADX WARNING: Missing block: B:56:0x009a, code:
            return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean processPendingItems() {
        ShortcutInfo shortcut = null;
        try {
            synchronized (this.mPendingItems) {
                if (this.mPendingItems.size() == 0) {
                    return false;
                }
                PendingItem item = (PendingItem) this.mPendingItems.pop();
            }
            return true;
        } finally {
            if (shortcut != null) {
                if (shortcut.getBitmapPath() == null) {
                    removeIcon(shortcut);
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
