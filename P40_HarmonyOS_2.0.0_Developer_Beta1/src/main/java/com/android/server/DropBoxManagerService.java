package com.android.server;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Binder;
import android.os.DropBoxManager;
import android.os.FileUtils;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ResultReceiver;
import android.os.ShellCallback;
import android.os.ShellCommand;
import android.os.StatFs;
import android.os.SystemClock;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Slog;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.os.IDropBoxManagerService;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.ObjectUtils;
import com.android.server.job.controllers.JobStatus;
import com.android.server.pm.PackageManagerService;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.zip.GZIPOutputStream;
import libcore.io.IoUtils;

public final class DropBoxManagerService extends SystemService {
    private static final int DEFAULT_AGE_SECONDS = 259200;
    private static final int DEFAULT_MAX_FILES = 1000;
    private static final int DEFAULT_MAX_FILES_LOWRAM = 300;
    private static final int DEFAULT_QUOTA_KB = 5120;
    private static final int DEFAULT_QUOTA_PERCENT = 10;
    private static final int DEFAULT_RESERVE_PERCENT = 10;
    private static final boolean PROFILE_DUMP = false;
    private static final int QUOTA_RESCAN_MILLIS = 5000;
    private static final String TAG = "DropBoxManagerService";
    private FileList mAllFiles;
    private int mBlockSize;
    private volatile boolean mBooted;
    private int mCachedQuotaBlocks;
    private long mCachedQuotaUptimeMillis;
    private final ContentResolver mContentResolver;
    private final File mDropBoxDir;
    private ArrayMap<String, FileList> mFilesByTag;
    private final DropBoxManagerBroadcastHandler mHandler;
    private long mLowPriorityRateLimitPeriod;
    private ArraySet<String> mLowPriorityTags;
    private int mMaxFiles;
    private final BroadcastReceiver mReceiver;
    private StatFs mStatFs;
    private final IDropBoxManagerService.Stub mStub;

    private class ShellCmd extends ShellCommand {
        private ShellCmd() {
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        public int onCommand(String cmd) {
            if (cmd == null) {
                return handleDefaultCommands(cmd);
            }
            PrintWriter pw = getOutPrintWriter();
            char c = 65535;
            try {
                switch (cmd.hashCode()) {
                    case -1412652367:
                        if (cmd.equals("restore-defaults")) {
                            c = 3;
                            break;
                        }
                        break;
                    case -529247831:
                        if (cmd.equals("add-low-priority")) {
                            c = 1;
                            break;
                        }
                        break;
                    case -444925274:
                        if (cmd.equals("remove-low-priority")) {
                            c = 2;
                            break;
                        }
                        break;
                    case 1936917209:
                        if (cmd.equals("set-rate-limit")) {
                            c = 0;
                            break;
                        }
                        break;
                }
                if (c == 0) {
                    DropBoxManagerService.this.setLowPriorityRateLimit(Long.parseLong(getNextArgRequired()));
                } else if (c == 1) {
                    DropBoxManagerService.this.addLowPriorityTag(getNextArgRequired());
                } else if (c == 2) {
                    DropBoxManagerService.this.removeLowPriorityTag(getNextArgRequired());
                } else if (c != 3) {
                    return handleDefaultCommands(cmd);
                } else {
                    DropBoxManagerService.this.restoreDefaults();
                }
            } catch (Exception e) {
                pw.println(e);
            }
            return 0;
        }

        public void onHelp() {
            PrintWriter pw = getOutPrintWriter();
            pw.println("Dropbox manager service commands:");
            pw.println("  help");
            pw.println("    Print this help text.");
            pw.println("  set-rate-limit PERIOD");
            pw.println("    Sets low priority broadcast rate limit period to PERIOD ms");
            pw.println("  add-low-priority TAG");
            pw.println("    Add TAG to dropbox low priority list");
            pw.println("  remove-low-priority TAG");
            pw.println("    Remove TAG from dropbox low priority list");
            pw.println("  restore-defaults");
            pw.println("    restore dropbox settings to defaults");
        }
    }

    /* access modifiers changed from: private */
    public class DropBoxManagerBroadcastHandler extends Handler {
        static final int MSG_SEND_BROADCAST = 1;
        static final int MSG_SEND_DEFERRED_BROADCAST = 2;
        @GuardedBy({"mLock"})
        private final ArrayMap<String, Intent> mDeferredMap = new ArrayMap<>();
        private final Object mLock = new Object();

        DropBoxManagerBroadcastHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            Intent deferredIntent;
            int i = msg.what;
            if (i == 1) {
                prepareAndSendBroadcast((Intent) msg.obj);
            } else if (i == 2) {
                synchronized (this.mLock) {
                    deferredIntent = this.mDeferredMap.remove((String) msg.obj);
                }
                if (deferredIntent != null) {
                    prepareAndSendBroadcast(deferredIntent);
                }
            }
        }

        private void prepareAndSendBroadcast(Intent intent) {
            if (!DropBoxManagerService.this.mBooted) {
                intent.addFlags(1073741824);
            }
            DropBoxManagerService.this.getContext().sendBroadcastAsUser(intent, UserHandle.SYSTEM, "android.permission.READ_LOGS");
        }

        private Intent createIntent(String tag, long time) {
            Intent dropboxIntent = new Intent("android.intent.action.DROPBOX_ENTRY_ADDED");
            dropboxIntent.putExtra("tag", tag);
            dropboxIntent.putExtra("time", time);
            return dropboxIntent;
        }

        public void sendBroadcast(String tag, long time) {
            sendMessage(obtainMessage(1, createIntent(tag, time)));
        }

        public void maybeDeferBroadcast(String tag, long time) {
            synchronized (this.mLock) {
                Intent intent = this.mDeferredMap.get(tag);
                if (intent == null) {
                    this.mDeferredMap.put(tag, createIntent(tag, time));
                    sendMessageDelayed(obtainMessage(2, tag), DropBoxManagerService.this.mLowPriorityRateLimitPeriod);
                    return;
                }
                intent.putExtra("time", time);
                intent.putExtra("android.os.extra.DROPPED_COUNT", intent.getIntExtra("android.os.extra.DROPPED_COUNT", 0) + 1);
            }
        }
    }

    public DropBoxManagerService(Context context) {
        this(context, new File("/data/system/dropbox"), FgThread.get().getLooper());
    }

    @VisibleForTesting
    public DropBoxManagerService(Context context, File path, Looper looper) {
        super(context);
        this.mAllFiles = null;
        this.mFilesByTag = null;
        this.mLowPriorityRateLimitPeriod = 0;
        this.mLowPriorityTags = null;
        this.mStatFs = null;
        this.mBlockSize = 0;
        this.mCachedQuotaBlocks = 0;
        this.mCachedQuotaUptimeMillis = 0;
        this.mBooted = false;
        this.mMaxFiles = -1;
        this.mReceiver = new BroadcastReceiver() {
            /* class com.android.server.DropBoxManagerService.AnonymousClass1 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                DropBoxManagerService.this.mCachedQuotaUptimeMillis = 0;
                new Thread() {
                    /* class com.android.server.DropBoxManagerService.AnonymousClass1.AnonymousClass1 */

                    @Override // java.lang.Thread, java.lang.Runnable
                    public void run() {
                        try {
                            DropBoxManagerService.this.init();
                            DropBoxManagerService.this.trimToFit();
                        } catch (IOException e) {
                            Slog.e(DropBoxManagerService.TAG, "Can't init", e);
                        }
                    }
                }.start();
            }
        };
        this.mStub = new IDropBoxManagerService.Stub() {
            /* class com.android.server.DropBoxManagerService.AnonymousClass2 */

            public void add(DropBoxManager.Entry entry) {
                DropBoxManagerService.this.add(entry);
            }

            public boolean isTagEnabled(String tag) {
                return DropBoxManagerService.this.isTagEnabled(tag);
            }

            public DropBoxManager.Entry getNextEntry(String tag, long millis, String callingPackage) {
                return DropBoxManagerService.this.getNextEntry(tag, millis, callingPackage);
            }

            public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
                DropBoxManagerService.this.dump(fd, pw, args);
            }

            /* JADX DEBUG: Multi-variable search result rejected for r8v0, resolved type: com.android.server.DropBoxManagerService$2 */
            /* JADX WARN: Multi-variable type inference failed */
            public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) {
                new ShellCmd().exec(this, in, out, err, args, callback, resultReceiver);
            }
        };
        this.mDropBoxDir = path;
        this.mContentResolver = getContext().getContentResolver();
        this.mHandler = new DropBoxManagerBroadcastHandler(looper);
    }

    @Override // com.android.server.SystemService
    public void onStart() {
        publishBinderService("dropbox", this.mStub);
    }

    @Override // com.android.server.SystemService
    public void onBootPhase(int phase) {
        if (phase == 500) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.DEVICE_STORAGE_LOW");
            getContext().registerReceiver(this.mReceiver, filter);
            this.mContentResolver.registerContentObserver(Settings.Global.CONTENT_URI, true, new ContentObserver(new Handler()) {
                /* class com.android.server.DropBoxManagerService.AnonymousClass3 */

                @Override // android.database.ContentObserver
                public void onChange(boolean selfChange) {
                    DropBoxManagerService.this.mReceiver.onReceive(DropBoxManagerService.this.getContext(), null);
                }
            });
            getLowPriorityResourceConfigs();
        } else if (phase == 1000) {
            this.mBooted = true;
        }
    }

    public IDropBoxManagerService getServiceStub() {
        return this.mStub;
    }

    /* JADX WARNING: Removed duplicated region for block: B:71:0x01c1  */
    /* JADX WARNING: Removed duplicated region for block: B:82:? A[RETURN, SYNTHETIC] */
    public void add(DropBoxManager.Entry entry) {
        Throwable th;
        IOException e;
        long max;
        long lastTrim;
        File temp = null;
        InputStream input = null;
        String tag = entry.getTag();
        try {
            int flags = entry.getFlags();
            Slog.i(TAG, "add tag=" + tag + " isTagEnabled=" + isTagEnabled(tag) + " flags=0x" + Integer.toHexString(flags));
            if ((flags & 1) == 0) {
                init();
                if (!isTagEnabled(tag)) {
                    IoUtils.closeQuietly((AutoCloseable) null);
                    IoUtils.closeQuietly((AutoCloseable) null);
                    entry.close();
                    if (0 != 0) {
                        temp.delete();
                        return;
                    }
                    return;
                }
                long max2 = trimToFit();
                long max3 = System.currentTimeMillis();
                byte[] buffer = new byte[this.mBlockSize];
                input = entry.getInputStream();
                int read = 0;
                while (read < buffer.length) {
                    try {
                        int n = input.read(buffer, read, buffer.length - read);
                        if (n <= 0) {
                            break;
                        }
                        read += n;
                    } catch (IOException e2) {
                        e = e2;
                        try {
                            Slog.e(TAG, "Can't write: " + tag, e);
                            IoUtils.closeQuietly((AutoCloseable) null);
                            IoUtils.closeQuietly(input);
                            entry.close();
                            if (temp == null) {
                                return;
                            }
                            temp.delete();
                        } catch (Throwable th2) {
                            th = th2;
                            IoUtils.closeQuietly((AutoCloseable) null);
                            IoUtils.closeQuietly(input);
                            entry.close();
                            if (temp != null) {
                                temp.delete();
                            }
                            throw th;
                        }
                    }
                }
                File file = this.mDropBoxDir;
                StringBuilder sb = new StringBuilder();
                try {
                    sb.append("drop");
                    long max4 = max2;
                    sb.append(Thread.currentThread().getId());
                    sb.append(".tmp");
                    File temp2 = new File(file, sb.toString());
                    int bufferSize = this.mBlockSize;
                    if (bufferSize > 4096) {
                        bufferSize = 4096;
                    }
                    if (bufferSize < 512) {
                        bufferSize = 512;
                    }
                    FileOutputStream foutput = new FileOutputStream(temp2);
                    OutputStream output = new BufferedOutputStream(foutput, bufferSize);
                    if (read == buffer.length && (flags & 4) == 0) {
                        output = new GZIPOutputStream(output);
                        flags |= 4;
                    }
                    while (true) {
                        output.write(buffer, 0, read);
                        long now = System.currentTimeMillis();
                        if (now - max3 > 30000) {
                            lastTrim = trimToFit();
                            max = now;
                        } else {
                            max = max3;
                            lastTrim = max4;
                        }
                        read = input.read(buffer);
                        if (read <= 0) {
                            FileUtils.sync(foutput);
                            output.close();
                            output = null;
                        } else {
                            output.flush();
                        }
                        if (temp2.length() > lastTrim) {
                            Slog.w(TAG, "Dropping: " + tag + " (" + temp2.length() + " > " + lastTrim + " bytes)");
                            temp2.delete();
                            temp2 = null;
                            break;
                        } else if (read <= 0) {
                            break;
                        } else {
                            max4 = lastTrim;
                            max3 = max;
                            bufferSize = bufferSize;
                            foutput = foutput;
                        }
                    }
                    long time = createEntry(temp2, tag, flags);
                    temp = null;
                    if (this.mLowPriorityTags == null || !this.mLowPriorityTags.contains(tag)) {
                        this.mHandler.sendBroadcast(tag, time);
                    } else {
                        this.mHandler.maybeDeferBroadcast(tag, time);
                    }
                    writeIndexFile(tag, time);
                    IoUtils.closeQuietly(output);
                    IoUtils.closeQuietly(input);
                    entry.close();
                    if (0 == 0) {
                        return;
                    }
                } catch (IOException e3) {
                    e = e3;
                    temp = null;
                    Slog.e(TAG, "Can't write: " + tag, e);
                    IoUtils.closeQuietly((AutoCloseable) null);
                    IoUtils.closeQuietly(input);
                    entry.close();
                    if (temp == null) {
                    }
                    temp.delete();
                } catch (Throwable th3) {
                    th = th3;
                    temp = null;
                    IoUtils.closeQuietly((AutoCloseable) null);
                    IoUtils.closeQuietly(input);
                    entry.close();
                    if (temp != null) {
                    }
                    throw th;
                }
                temp.delete();
            }
            throw new IllegalArgumentException();
        } catch (IOException e4) {
            e = e4;
            Slog.e(TAG, "Can't write: " + tag, e);
            IoUtils.closeQuietly((AutoCloseable) null);
            IoUtils.closeQuietly(input);
            entry.close();
            if (temp == null) {
            }
            temp.delete();
        } catch (Throwable th4) {
            th = th4;
            IoUtils.closeQuietly((AutoCloseable) null);
            IoUtils.closeQuietly(input);
            entry.close();
            if (temp != null) {
            }
            throw th;
        }
    }

    public boolean isTagEnabled(String tag) {
        long token = Binder.clearCallingIdentity();
        try {
            ContentResolver contentResolver = this.mContentResolver;
            return !"disabled".equals(Settings.Global.getString(contentResolver, "dropbox:" + tag));
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    private boolean checkPermission(int callingUid, String callingPackage) {
        getContext().enforceCallingOrSelfPermission("android.permission.READ_LOGS", TAG);
        int noteOp = ((AppOpsManager) getContext().getSystemService(AppOpsManager.class)).noteOp(43, callingUid, callingPackage);
        if (noteOp == 0) {
            return true;
        }
        if (noteOp != 3) {
            return false;
        }
        getContext().enforceCallingOrSelfPermission("android.permission.PACKAGE_USAGE_STATS", TAG);
        return true;
    }

    public synchronized DropBoxManager.Entry getNextEntry(String tag, long millis, String callingPackage) {
        if (!checkPermission(Binder.getCallingUid(), callingPackage)) {
            return null;
        }
        try {
            init();
            FileList list = tag == null ? this.mAllFiles : this.mFilesByTag.get(tag);
            if (list == null) {
                return null;
            }
            for (EntryFile entry : list.contents.tailSet(new EntryFile(millis + 1))) {
                if (entry.tag != null) {
                    if ((entry.flags & 1) != 0) {
                        return new DropBoxManager.Entry(entry.tag, entry.timestampMillis);
                    }
                    File file = entry.getFile(this.mDropBoxDir);
                    try {
                        return new DropBoxManager.Entry(entry.tag, entry.timestampMillis, file, entry.flags);
                    } catch (IOException e) {
                        Slog.wtf(TAG, "Can't read: " + file, e);
                    }
                }
            }
            return null;
        } catch (IOException e2) {
            Slog.e(TAG, "Can't init", e2);
            return null;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized void setLowPriorityRateLimit(long period) {
        this.mLowPriorityRateLimitPeriod = period;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized void addLowPriorityTag(String tag) {
        this.mLowPriorityTags.add(tag);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized void removeLowPriorityTag(String tag) {
        this.mLowPriorityTags.remove(tag);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized void restoreDefaults() {
        getLowPriorityResourceConfigs();
    }

    /* JADX WARNING: Removed duplicated region for block: B:179:0x0386  */
    /* JADX WARNING: Removed duplicated region for block: B:181:0x038b A[SYNTHETIC, Splitter:B:181:0x038b] */
    /* JADX WARNING: Removed duplicated region for block: B:187:0x0399  */
    /* JADX WARNING: Removed duplicated region for block: B:189:0x039e A[SYNTHETIC, Splitter:B:189:0x039e] */
    /* JADX WARNING: Removed duplicated region for block: B:195:0x03ac  */
    public synchronized void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        StringBuilder out;
        ArrayList<String> searchArgs;
        boolean doFile;
        boolean doPrint;
        int numFound;
        int numArgs;
        Time time;
        Iterator<EntryFile> it;
        Time time2;
        boolean doFile2;
        int numFound2;
        Iterator<EntryFile> it2;
        int numArgs2;
        InputStreamReader isr;
        DropBoxManager.Entry dbe;
        Throwable th;
        IOException e;
        ArrayList<String> searchArgs2;
        if (DumpUtils.checkDumpAndUsageStatsPermission(getContext(), TAG, pw)) {
            try {
                init();
                out = new StringBuilder();
                searchArgs = new ArrayList<>();
                int i = 0;
                doFile = false;
                doPrint = false;
                while (args != null && i < args.length) {
                    if (args[i].equals("-p") || args[i].equals("--print")) {
                        doPrint = true;
                    } else if (args[i].equals("-f") || args[i].equals("--file")) {
                        doFile = true;
                    } else if (args[i].equals("-h") || args[i].equals("--help")) {
                        pw.println("Dropbox (dropbox) dump options:");
                        pw.println("  [-h|--help] [-p|--print] [-f|--file] [timestamp]");
                        pw.println("    -h|--help: print this help");
                        pw.println("    -p|--print: print full contents of each entry");
                        pw.println("    -f|--file: print path of each entry's file");
                        pw.println("  [timestamp] optionally filters to only those entries.");
                        return;
                    } else if (args[i].startsWith("-")) {
                        out.append("Unknown argument: ");
                        out.append(args[i]);
                        out.append("\n");
                    } else {
                        searchArgs.add(args[i]);
                    }
                    i++;
                }
                out.append("Drop box contents: ");
                out.append(this.mAllFiles.contents.size());
                out.append(" entries\n");
                out.append("Max entries: ");
                out.append(this.mMaxFiles);
                out.append("\n");
                out.append("Low priority rate limit period: ");
                out.append(this.mLowPriorityRateLimitPeriod);
                out.append(" ms\n");
                out.append("Low priority tags: ");
                out.append(this.mLowPriorityTags);
                out.append("\n");
                if (!searchArgs.isEmpty()) {
                    out.append("Searching for:");
                    Iterator<String> it3 = searchArgs.iterator();
                    while (it3.hasNext()) {
                        out.append(" ");
                        out.append(it3.next());
                    }
                    out.append("\n");
                }
                numFound = 0;
                numArgs = searchArgs.size();
                time = new Time();
                out.append("\n");
                it = this.mAllFiles.contents.iterator();
                if (numFound == 0) {
                    out.append("(No entries found.)\n");
                }
                if (args == null || args.length == 0) {
                    if (!doPrint) {
                        out.append("\n");
                    }
                    out.append("Usage: dumpsys dropbox [--print|--file] [YYYY-mm-dd] [HH:MM:SS] [tag]\n");
                }
                pw.write(out.toString());
                return;
            } catch (IOException e2) {
                pw.println("Can't initialize: " + e2);
                Slog.e(TAG, "Can't init", e2);
                return;
            }
            while (it.hasNext()) {
                EntryFile entry = it.next();
                time.set(entry.timestampMillis);
                String date = time.format("%Y-%m-%d %H:%M:%S");
                boolean match = true;
                int i2 = 0;
                while (true) {
                    boolean z = true;
                    if (i2 >= numArgs || !match) {
                        break;
                    }
                    String arg = searchArgs.get(i2);
                    if (!date.contains(arg)) {
                        searchArgs2 = searchArgs;
                        if (!arg.equals(entry.tag)) {
                            z = false;
                        }
                    } else {
                        searchArgs2 = searchArgs;
                    }
                    match = z;
                    i2++;
                    searchArgs = searchArgs2;
                }
                if (!match) {
                    searchArgs = searchArgs;
                } else {
                    int numFound3 = numFound + 1;
                    if (doPrint) {
                        out.append("========================================\n");
                    }
                    out.append(date);
                    out.append(" ");
                    out.append(entry.tag == null ? "(no tag)" : entry.tag);
                    File file = entry.getFile(this.mDropBoxDir);
                    if (file == null) {
                        out.append(" (no file)\n");
                        numFound2 = numFound3;
                        numArgs2 = numArgs;
                        doFile2 = doFile;
                        time2 = time;
                        it2 = it;
                    } else if ((entry.flags & 1) != 0) {
                        out.append(" (contents lost)\n");
                        numFound2 = numFound3;
                        numArgs2 = numArgs;
                        doFile2 = doFile;
                        time2 = time;
                        it2 = it;
                    } else {
                        out.append(" (");
                        if ((entry.flags & 4) != 0) {
                            out.append("compressed ");
                        }
                        out.append((entry.flags & 2) != 0 ? "text" : "data");
                        out.append(", ");
                        numFound2 = numFound3;
                        numArgs2 = numArgs;
                        out.append(file.length());
                        out.append(" bytes)\n");
                        if (doFile || (doPrint && (entry.flags & 2) == 0)) {
                            if (!doPrint) {
                                out.append("    ");
                            }
                            out.append(file.getPath());
                            out.append("\n");
                        }
                        if ((entry.flags & 2) == 0) {
                            doFile2 = doFile;
                            time2 = time;
                            it2 = it;
                        } else if (doPrint || !doFile) {
                            DropBoxManager.Entry dbe2 = null;
                            InputStreamReader isr2 = null;
                            try {
                                dbe = null;
                                try {
                                    isr = null;
                                    doFile2 = doFile;
                                } catch (IOException e3) {
                                    e = e3;
                                    doFile2 = doFile;
                                    time2 = time;
                                    dbe2 = null;
                                    try {
                                        out.append("*** ");
                                        out.append(e.toString());
                                        out.append("\n");
                                        StringBuilder sb = new StringBuilder();
                                        it2 = it;
                                        sb.append("Can't read: ");
                                        sb.append(file);
                                        Slog.e(TAG, sb.toString(), e);
                                        if (dbe2 != null) {
                                            dbe2.close();
                                        }
                                        if (isr2 != null) {
                                            try {
                                                isr2.close();
                                            } catch (IOException e4) {
                                            }
                                        }
                                        if (doPrint) {
                                        }
                                        numArgs = numArgs2;
                                        it = it2;
                                        searchArgs = searchArgs;
                                        numFound = numFound2;
                                        doFile = doFile2;
                                        time = time2;
                                    } catch (Throwable th2) {
                                        dbe = dbe2;
                                        isr = isr2;
                                        th = th2;
                                    }
                                } catch (Throwable th3) {
                                    isr = null;
                                    th = th3;
                                    if (dbe != null) {
                                        dbe.close();
                                    }
                                    if (isr != null) {
                                        try {
                                            isr.close();
                                        } catch (IOException e5) {
                                        }
                                    }
                                    throw th;
                                }
                                try {
                                    time2 = time;
                                    try {
                                        dbe2 = new DropBoxManager.Entry(entry.tag, entry.timestampMillis, file, entry.flags);
                                        if (doPrint) {
                                            try {
                                                isr2 = new InputStreamReader(dbe2.getInputStream());
                                            } catch (IOException e6) {
                                                e = e6;
                                                isr2 = null;
                                                out.append("*** ");
                                                out.append(e.toString());
                                                out.append("\n");
                                                StringBuilder sb2 = new StringBuilder();
                                                it2 = it;
                                                sb2.append("Can't read: ");
                                                sb2.append(file);
                                                Slog.e(TAG, sb2.toString(), e);
                                                if (dbe2 != null) {
                                                }
                                                if (isr2 != null) {
                                                }
                                                if (doPrint) {
                                                }
                                                numArgs = numArgs2;
                                                it = it2;
                                                searchArgs = searchArgs;
                                                numFound = numFound2;
                                                doFile = doFile2;
                                                time = time2;
                                            } catch (Throwable th4) {
                                                dbe = dbe2;
                                                th = th4;
                                                if (dbe != null) {
                                                }
                                                if (isr != null) {
                                                }
                                                throw th;
                                            }
                                            try {
                                                char[] buf = new char[4096];
                                                boolean newline = false;
                                                while (true) {
                                                    int n = isr2.read(buf);
                                                    if (n <= 0) {
                                                        break;
                                                    }
                                                    out.append(buf, 0, n);
                                                    newline = buf[n + -1] == '\n';
                                                    try {
                                                        if (out.length() > 65536) {
                                                            pw.write(out.toString());
                                                            out.setLength(0);
                                                        }
                                                        isr2 = isr2;
                                                    } catch (IOException e7) {
                                                        e = e7;
                                                        isr2 = isr2;
                                                        out.append("*** ");
                                                        out.append(e.toString());
                                                        out.append("\n");
                                                        StringBuilder sb22 = new StringBuilder();
                                                        it2 = it;
                                                        sb22.append("Can't read: ");
                                                        sb22.append(file);
                                                        Slog.e(TAG, sb22.toString(), e);
                                                        if (dbe2 != null) {
                                                        }
                                                        if (isr2 != null) {
                                                        }
                                                        if (doPrint) {
                                                        }
                                                        numArgs = numArgs2;
                                                        it = it2;
                                                        searchArgs = searchArgs;
                                                        numFound = numFound2;
                                                        doFile = doFile2;
                                                        time = time2;
                                                    } catch (Throwable th5) {
                                                        dbe = dbe2;
                                                        isr = isr2;
                                                        th = th5;
                                                        if (dbe != null) {
                                                        }
                                                        if (isr != null) {
                                                        }
                                                        throw th;
                                                    }
                                                }
                                                if (!newline) {
                                                    try {
                                                        out.append("\n");
                                                    } catch (IOException e8) {
                                                        e = e8;
                                                    }
                                                }
                                                isr = isr2;
                                            } catch (IOException e9) {
                                                e = e9;
                                                out.append("*** ");
                                                out.append(e.toString());
                                                out.append("\n");
                                                StringBuilder sb222 = new StringBuilder();
                                                it2 = it;
                                                sb222.append("Can't read: ");
                                                sb222.append(file);
                                                Slog.e(TAG, sb222.toString(), e);
                                                if (dbe2 != null) {
                                                }
                                                if (isr2 != null) {
                                                }
                                                if (doPrint) {
                                                }
                                                numArgs = numArgs2;
                                                it = it2;
                                                searchArgs = searchArgs;
                                                numFound = numFound2;
                                                doFile = doFile2;
                                                time = time2;
                                            } catch (Throwable th6) {
                                                dbe = dbe2;
                                                isr = isr2;
                                                th = th6;
                                                if (dbe != null) {
                                                }
                                                if (isr != null) {
                                                }
                                                throw th;
                                            }
                                        } else {
                                            boolean truncated = false;
                                            String text = dbe2.getText(70);
                                            out.append("    ");
                                            if (text == null) {
                                                out.append("[null]");
                                            } else {
                                                if (text.length() == 70) {
                                                    truncated = true;
                                                }
                                                out.append(text.trim().replace('\n', '/'));
                                                if (truncated) {
                                                    out.append(" ...");
                                                }
                                            }
                                            out.append("\n");
                                        }
                                        dbe2.close();
                                        if (isr != null) {
                                            try {
                                                isr.close();
                                            } catch (IOException e10) {
                                            }
                                            it2 = it;
                                        } else {
                                            it2 = it;
                                        }
                                    } catch (IOException e11) {
                                        e = e11;
                                        dbe2 = null;
                                        isr2 = null;
                                        out.append("*** ");
                                        out.append(e.toString());
                                        out.append("\n");
                                        StringBuilder sb2222 = new StringBuilder();
                                        it2 = it;
                                        sb2222.append("Can't read: ");
                                        sb2222.append(file);
                                        Slog.e(TAG, sb2222.toString(), e);
                                        if (dbe2 != null) {
                                        }
                                        if (isr2 != null) {
                                        }
                                        if (doPrint) {
                                        }
                                        numArgs = numArgs2;
                                        it = it2;
                                        searchArgs = searchArgs;
                                        numFound = numFound2;
                                        doFile = doFile2;
                                        time = time2;
                                    } catch (Throwable th7) {
                                        th = th7;
                                        if (dbe != null) {
                                        }
                                        if (isr != null) {
                                        }
                                        throw th;
                                    }
                                } catch (IOException e12) {
                                    e = e12;
                                    time2 = time;
                                    dbe2 = null;
                                    isr2 = null;
                                    out.append("*** ");
                                    out.append(e.toString());
                                    out.append("\n");
                                    StringBuilder sb22222 = new StringBuilder();
                                    it2 = it;
                                    sb22222.append("Can't read: ");
                                    sb22222.append(file);
                                    Slog.e(TAG, sb22222.toString(), e);
                                    if (dbe2 != null) {
                                    }
                                    if (isr2 != null) {
                                    }
                                    if (doPrint) {
                                    }
                                    numArgs = numArgs2;
                                    it = it2;
                                    searchArgs = searchArgs;
                                    numFound = numFound2;
                                    doFile = doFile2;
                                    time = time2;
                                } catch (Throwable th8) {
                                    th = th8;
                                    if (dbe != null) {
                                    }
                                    if (isr != null) {
                                    }
                                    throw th;
                                }
                            } catch (IOException e13) {
                                e = e13;
                                doFile2 = doFile;
                                time2 = time;
                                out.append("*** ");
                                out.append(e.toString());
                                out.append("\n");
                                StringBuilder sb222222 = new StringBuilder();
                                it2 = it;
                                sb222222.append("Can't read: ");
                                sb222222.append(file);
                                Slog.e(TAG, sb222222.toString(), e);
                                if (dbe2 != null) {
                                }
                                if (isr2 != null) {
                                }
                                if (doPrint) {
                                }
                                numArgs = numArgs2;
                                it = it2;
                                searchArgs = searchArgs;
                                numFound = numFound2;
                                doFile = doFile2;
                                time = time2;
                            } catch (Throwable th9) {
                                dbe = null;
                                isr = null;
                                th = th9;
                                if (dbe != null) {
                                }
                                if (isr != null) {
                                }
                                throw th;
                            }
                        } else {
                            doFile2 = doFile;
                            time2 = time;
                            it2 = it;
                        }
                        if (doPrint) {
                            out.append("\n");
                        }
                    }
                    numArgs = numArgs2;
                    it = it2;
                    searchArgs = searchArgs;
                    numFound = numFound2;
                    doFile = doFile2;
                    time = time2;
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public static final class FileList implements Comparable<FileList> {
        public int blocks;
        public final TreeSet<EntryFile> contents;

        private FileList() {
            this.blocks = 0;
            this.contents = new TreeSet<>();
        }

        public final int compareTo(FileList o) {
            int i = this.blocks;
            int i2 = o.blocks;
            if (i != i2) {
                return i2 - i;
            }
            if (this == o) {
                return 0;
            }
            if (hashCode() < o.hashCode()) {
                return -1;
            }
            if (hashCode() > o.hashCode()) {
                return 1;
            }
            return 0;
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public static final class EntryFile implements Comparable<EntryFile> {
        public final int blocks;
        public final int flags;
        public final String tag;
        public final long timestampMillis;

        public final int compareTo(EntryFile o) {
            int comp = Long.compare(this.timestampMillis, o.timestampMillis);
            if (comp != 0) {
                return comp;
            }
            int comp2 = ObjectUtils.compare(this.tag, o.tag);
            if (comp2 != 0) {
                return comp2;
            }
            int comp3 = Integer.compare(this.flags, o.flags);
            if (comp3 != 0) {
                return comp3;
            }
            return Integer.compare(hashCode(), o.hashCode());
        }

        public EntryFile(File temp, File dir, String tag2, long timestampMillis2, int flags2, int blockSize) throws IOException {
            if ((flags2 & 1) == 0) {
                this.tag = TextUtils.safeIntern(tag2);
                this.timestampMillis = timestampMillis2;
                this.flags = flags2;
                File file = getFile(dir);
                if (temp.renameTo(file)) {
                    this.blocks = (int) (((file.length() + ((long) blockSize)) - 1) / ((long) blockSize));
                    return;
                }
                throw new IOException("Can't rename " + temp + " to " + file);
            }
            throw new IllegalArgumentException();
        }

        public EntryFile(File dir, String tag2, long timestampMillis2) throws IOException {
            this.tag = TextUtils.safeIntern(tag2);
            this.timestampMillis = timestampMillis2;
            this.flags = 1;
            this.blocks = 0;
            new FileOutputStream(getFile(dir)).close();
        }

        public EntryFile(File file, int blockSize) {
            boolean parseFailure = false;
            String name = file.getName();
            int flags2 = 0;
            String tag2 = null;
            long millis = 0;
            int at = name.lastIndexOf(64);
            if (at < 0) {
                parseFailure = true;
            } else {
                tag2 = Uri.decode(name.substring(0, at));
                if (name.endsWith(PackageManagerService.COMPRESSED_EXTENSION)) {
                    flags2 = 0 | 4;
                    name = name.substring(0, name.length() - 3);
                }
                if (name.endsWith(".lost")) {
                    flags2 |= 1;
                    name = name.substring(at + 1, name.length() - 5);
                } else if (name.endsWith(".txt")) {
                    flags2 |= 2;
                    name = name.substring(at + 1, name.length() - 4);
                } else if (name.endsWith(".dat")) {
                    name = name.substring(at + 1, name.length() - 4);
                } else {
                    parseFailure = true;
                }
                if (!parseFailure) {
                    try {
                        millis = Long.parseLong(name);
                    } catch (NumberFormatException e) {
                        parseFailure = true;
                    }
                }
            }
            if (parseFailure) {
                Slog.wtf(DropBoxManagerService.TAG, "Invalid filename: " + file);
                file.delete();
                this.tag = null;
                this.flags = 1;
                this.timestampMillis = 0;
                this.blocks = 0;
                return;
            }
            this.blocks = (int) (((file.length() + ((long) blockSize)) - 1) / ((long) blockSize));
            this.tag = TextUtils.safeIntern(tag2);
            this.flags = flags2;
            this.timestampMillis = millis;
        }

        public EntryFile(long millis) {
            this.tag = null;
            this.timestampMillis = millis;
            this.flags = 1;
            this.blocks = 0;
        }

        public boolean hasFile() {
            return this.tag != null;
        }

        private String getExtension() {
            if ((this.flags & 1) != 0) {
                return ".lost";
            }
            StringBuilder sb = new StringBuilder();
            sb.append((this.flags & 2) != 0 ? ".txt" : ".dat");
            sb.append((this.flags & 4) != 0 ? PackageManagerService.COMPRESSED_EXTENSION : "");
            return sb.toString();
        }

        public String getFilename() {
            if (!hasFile()) {
                return null;
            }
            return Uri.encode(this.tag) + "@" + this.timestampMillis + getExtension();
        }

        public File getFile(File dir) {
            if (hasFile()) {
                return new File(dir, getFilename());
            }
            return null;
        }

        public void deleteFile(File dir) {
            if (hasFile()) {
                getFile(dir).delete();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized void init() throws IOException {
        if (this.mStatFs == null) {
            if (!this.mDropBoxDir.isDirectory()) {
                if (!this.mDropBoxDir.mkdirs()) {
                    throw new IOException("Can't mkdir: " + this.mDropBoxDir);
                }
            }
            initIndexPath();
            try {
                this.mStatFs = new StatFs(this.mDropBoxDir.getPath());
                this.mBlockSize = this.mStatFs.getBlockSize();
            } catch (IllegalArgumentException e) {
                throw new IOException("Can't statfs: " + this.mDropBoxDir);
            }
        }
        if (this.mAllFiles == null) {
            File[] files = this.mDropBoxDir.listFiles();
            if (files != null) {
                this.mAllFiles = new FileList();
                this.mFilesByTag = new ArrayMap<>();
                for (File file : files) {
                    if (file.getName().endsWith(".tmp")) {
                        Slog.i(TAG, "Cleaning temp file: " + file);
                        file.delete();
                    } else {
                        EntryFile entry = new EntryFile(file, this.mBlockSize);
                        if (entry.hasFile()) {
                            enrollEntry(entry);
                        }
                    }
                }
            } else {
                throw new IOException("Can't list files: " + this.mDropBoxDir);
            }
        }
    }

    private synchronized void enrollEntry(EntryFile entry) {
        this.mAllFiles.contents.add(entry);
        this.mAllFiles.blocks += entry.blocks;
        if (entry.hasFile() && entry.blocks > 0) {
            FileList tagFiles = this.mFilesByTag.get(entry.tag);
            if (tagFiles == null) {
                tagFiles = new FileList();
                this.mFilesByTag.put(TextUtils.safeIntern(entry.tag), tagFiles);
            }
            tagFiles.contents.add(entry);
            tagFiles.blocks += entry.blocks;
        }
    }

    private synchronized long createEntry(File temp, String tag, int flags) throws IOException {
        long t;
        SortedSet<EntryFile> tail;
        long j;
        long t2 = System.currentTimeMillis();
        SortedSet<EntryFile> tail2 = this.mAllFiles.contents.tailSet(new EntryFile(JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY + t2));
        EntryFile[] future = null;
        if (!tail2.isEmpty()) {
            future = (EntryFile[]) tail2.toArray(new EntryFile[tail2.size()]);
            tail2.clear();
        }
        long j2 = 1;
        if (!this.mAllFiles.contents.isEmpty()) {
            t2 = Math.max(t2, this.mAllFiles.contents.last().timestampMillis + 1);
        }
        if (future != null) {
            int length = future.length;
            int i = 0;
            t = t2;
            while (i < length) {
                EntryFile late = future[i];
                this.mAllFiles.blocks -= late.blocks;
                FileList tagFiles = this.mFilesByTag.get(late.tag);
                if (tagFiles != null && tagFiles.contents.remove(late)) {
                    tagFiles.blocks -= late.blocks;
                }
                if ((late.flags & 1) == 0) {
                    tail = tail2;
                    enrollEntry(new EntryFile(late.getFile(this.mDropBoxDir), this.mDropBoxDir, late.tag, t, late.flags, this.mBlockSize));
                    t += j2;
                    j = 1;
                } else {
                    tail = tail2;
                    j = 1;
                    enrollEntry(new EntryFile(this.mDropBoxDir, late.tag, t));
                    t++;
                }
                i++;
                j2 = j;
                tail2 = tail;
            }
        } else {
            t = t2;
        }
        if (temp == null) {
            enrollEntry(new EntryFile(this.mDropBoxDir, tag, t));
        } else {
            enrollEntry(new EntryFile(temp, this.mDropBoxDir, tag, t, flags, this.mBlockSize));
        }
        return t;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized long trimToFit() throws IOException {
        long cutoffMillis;
        int ageSeconds;
        IOException e;
        int ageSeconds2 = Settings.Global.getInt(this.mContentResolver, "dropbox_age_seconds", DEFAULT_AGE_SECONDS);
        this.mMaxFiles = Settings.Global.getInt(this.mContentResolver, "dropbox_max_files", ActivityManager.isLowRamDeviceStatic() ? 300 : 1000);
        long cutoffMillis2 = System.currentTimeMillis() - ((long) (ageSeconds2 * 1000));
        while (true) {
            if (this.mAllFiles.contents.isEmpty()) {
                break;
            }
            EntryFile entry = this.mAllFiles.contents.first();
            if (entry.timestampMillis > cutoffMillis2 && this.mAllFiles.contents.size() < this.mMaxFiles) {
                break;
            }
            FileList tag = this.mFilesByTag.get(entry.tag);
            if (tag != null && tag.contents.remove(entry)) {
                tag.blocks -= entry.blocks;
            }
            if (this.mAllFiles.contents.remove(entry)) {
                this.mAllFiles.blocks -= entry.blocks;
            }
            entry.deleteFile(this.mDropBoxDir);
        }
        long uptimeMillis = SystemClock.uptimeMillis();
        if (uptimeMillis > this.mCachedQuotaUptimeMillis + 5000) {
            int quotaPercent = Settings.Global.getInt(this.mContentResolver, "dropbox_quota_percent", 10);
            int reservePercent = Settings.Global.getInt(this.mContentResolver, "dropbox_reserve_percent", 10);
            int quotaKb = Settings.Global.getInt(this.mContentResolver, "dropbox_quota_kb", DEFAULT_QUOTA_KB);
            try {
                this.mStatFs.restat(this.mDropBoxDir.getPath());
                this.mCachedQuotaBlocks = Math.min((quotaKb * 1024) / this.mBlockSize, Math.max(0, ((this.mStatFs.getAvailableBlocks() - ((this.mStatFs.getBlockCount() * reservePercent) / 100)) * quotaPercent) / 100));
                this.mCachedQuotaUptimeMillis = uptimeMillis;
            } catch (IllegalArgumentException e2) {
                throw new IOException("Can't restat: " + this.mDropBoxDir);
            }
        }
        if (this.mAllFiles.blocks > this.mCachedQuotaBlocks) {
            int unsqueezed = this.mAllFiles.blocks;
            int squeezed = 0;
            TreeSet<FileList> tags = new TreeSet<>(this.mFilesByTag.values());
            Iterator<FileList> it = tags.iterator();
            int unsqueezed2 = unsqueezed;
            while (it.hasNext()) {
                FileList tag2 = it.next();
                if (squeezed > 0 && tag2.blocks <= (this.mCachedQuotaBlocks - unsqueezed2) / squeezed) {
                    break;
                }
                unsqueezed2 -= tag2.blocks;
                squeezed++;
            }
            int tagQuota = (this.mCachedQuotaBlocks - unsqueezed2) / squeezed;
            Iterator<FileList> it2 = tags.iterator();
            while (true) {
                if (!it2.hasNext()) {
                    break;
                }
                FileList tag3 = it2.next();
                if (this.mAllFiles.blocks < this.mCachedQuotaBlocks) {
                    break;
                }
                while (tag3.blocks > tagQuota && !tag3.contents.isEmpty()) {
                    EntryFile entry2 = tag3.contents.first();
                    if (tag3.contents.remove(entry2)) {
                        tag3.blocks -= entry2.blocks;
                    }
                    if (this.mAllFiles.contents.remove(entry2)) {
                        this.mAllFiles.blocks -= entry2.blocks;
                    }
                    try {
                        entry2.deleteFile(this.mDropBoxDir);
                        ageSeconds = ageSeconds2;
                        cutoffMillis = cutoffMillis2;
                        try {
                            enrollEntry(new EntryFile(this.mDropBoxDir, entry2.tag, entry2.timestampMillis));
                        } catch (IOException e3) {
                            e = e3;
                        }
                    } catch (IOException e4) {
                        e = e4;
                        ageSeconds = ageSeconds2;
                        cutoffMillis = cutoffMillis2;
                        Slog.e(TAG, "Can't write tombstone file", e);
                        ageSeconds2 = ageSeconds;
                        cutoffMillis2 = cutoffMillis;
                    }
                    ageSeconds2 = ageSeconds;
                    cutoffMillis2 = cutoffMillis;
                }
                ageSeconds2 = ageSeconds2;
                cutoffMillis2 = cutoffMillis2;
            }
        }
        return (long) (this.mCachedQuotaBlocks * this.mBlockSize);
    }

    private void initIndexPath() throws IOException {
        File indexfilepath = new File("/data/log/fileindex");
        if (!indexfilepath.exists() && !indexfilepath.isDirectory() && !indexfilepath.mkdirs()) {
            throw new IOException("Can't mkdir: " + indexfilepath);
        }
    }

    private void writeIndexFile(String tag, long time) {
        File indexFilePath = new File("/data/log/fileindex");
        File file = new File(indexFilePath, tag + "@" + time);
        if (file.exists()) {
            file.delete();
        }
        try {
            file.createNewFile();
        } catch (IOException e) {
            Slog.e(TAG, "Can't write index file", e);
        }
    }

    private void getLowPriorityResourceConfigs() {
        this.mLowPriorityRateLimitPeriod = (long) Resources.getSystem().getInteger(17694807);
        String[] lowPrioritytags = Resources.getSystem().getStringArray(17236018);
        int size = lowPrioritytags.length;
        if (size == 0) {
            this.mLowPriorityTags = null;
            return;
        }
        this.mLowPriorityTags = new ArraySet<>(size);
        for (String str : lowPrioritytags) {
            this.mLowPriorityTags.add(str);
        }
    }
}
