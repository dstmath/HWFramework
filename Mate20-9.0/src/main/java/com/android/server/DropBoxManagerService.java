package com.android.server;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Binder;
import android.os.DropBoxManager;
import android.os.FileUtils;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StatFs;
import android.os.SystemClock;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Slog;
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
    private static final int DEFAULT_RESERVE_PERCENT_FOR_DEBUGGABLE_DEVICES = 1;
    private static final int MSG_SEND_BROADCAST = 1;
    private static final boolean PROFILE_DUMP = false;
    private static final int QUOTA_RESCAN_MILLIS = 5000;
    private static final String TAG = "DropBoxManagerService";
    private FileList mAllFiles;
    private int mBlockSize;
    private volatile boolean mBooted;
    private int mCachedQuotaBlocks;
    /* access modifiers changed from: private */
    public long mCachedQuotaUptimeMillis;
    private final ContentResolver mContentResolver;
    private final File mDropBoxDir;
    private ArrayMap<String, FileList> mFilesByTag;
    private final Handler mHandler;
    private int mMaxFiles;
    /* access modifiers changed from: private */
    public final BroadcastReceiver mReceiver;
    private StatFs mStatFs;
    private final IDropBoxManagerService.Stub mStub;

    @VisibleForTesting
    static final class EntryFile implements Comparable<EntryFile> {
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
            sb.append((this.flags & 4) != 0 ? PackageManagerService.COMPRESSED_EXTENSION : BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
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

    private static final class FileList implements Comparable<FileList> {
        public int blocks;
        public final TreeSet<EntryFile> contents;

        private FileList() {
            this.blocks = 0;
            this.contents = new TreeSet<>();
        }

        public final int compareTo(FileList o) {
            if (this.blocks != o.blocks) {
                return o.blocks - this.blocks;
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

    public DropBoxManagerService(Context context) {
        this(context, new File("/data/system/dropbox"), FgThread.get().getLooper());
    }

    @VisibleForTesting
    public DropBoxManagerService(Context context, File path, Looper looper) {
        super(context);
        this.mAllFiles = null;
        this.mFilesByTag = null;
        this.mStatFs = null;
        this.mBlockSize = 0;
        this.mCachedQuotaBlocks = 0;
        this.mCachedQuotaUptimeMillis = 0;
        this.mBooted = false;
        this.mMaxFiles = -1;
        this.mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                long unused = DropBoxManagerService.this.mCachedQuotaUptimeMillis = 0;
                new Thread() {
                    public void run() {
                        try {
                            DropBoxManagerService.this.init();
                            long unused = DropBoxManagerService.this.trimToFit();
                        } catch (IOException e) {
                            Slog.e(DropBoxManagerService.TAG, "Can't init", e);
                        }
                    }
                }.start();
            }
        };
        this.mStub = new IDropBoxManagerService.Stub() {
            public void add(DropBoxManager.Entry entry) {
                DropBoxManagerService.this.add(entry);
            }

            public boolean isTagEnabled(String tag) {
                return DropBoxManagerService.this.isTagEnabled(tag);
            }

            public DropBoxManager.Entry getNextEntry(String tag, long millis) {
                return DropBoxManagerService.this.getNextEntry(tag, millis);
            }

            public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
                DropBoxManagerService.this.dump(fd, pw, args);
            }
        };
        this.mDropBoxDir = path;
        this.mContentResolver = getContext().getContentResolver();
        this.mHandler = new Handler(looper) {
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    DropBoxManagerService.this.getContext().sendBroadcastAsUser((Intent) msg.obj, UserHandle.SYSTEM, "android.permission.READ_LOGS");
                }
            }
        };
    }

    public void onStart() {
        publishBinderService("dropbox", this.mStub);
    }

    public void onBootPhase(int phase) {
        if (phase == 500) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.DEVICE_STORAGE_LOW");
            getContext().registerReceiver(this.mReceiver, filter);
            this.mContentResolver.registerContentObserver(Settings.Global.CONTENT_URI, true, new ContentObserver(new Handler()) {
                public void onChange(boolean selfChange) {
                    DropBoxManagerService.this.mReceiver.onReceive(DropBoxManagerService.this.getContext(), null);
                }
            });
        } else if (phase == 1000) {
            this.mBooted = true;
        }
    }

    public IDropBoxManagerService getServiceStub() {
        return this.mStub;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:49:0x0153, code lost:
        if (0 != 0) goto L_0x0155;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x0155, code lost:
        r2.delete();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x018c, code lost:
        if (r2 == null) goto L_0x018f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x018f, code lost:
        return;
     */
    public void add(DropBoxManager.Entry entry) {
        long max;
        long lastTrim;
        File temp = null;
        String tag = entry.getTag();
        try {
            int flags = entry.getFlags();
            if ((flags & 1) == 0) {
                init();
                if (!isTagEnabled(tag)) {
                    IoUtils.closeQuietly(null);
                    IoUtils.closeQuietly(null);
                    entry.close();
                    if (temp != null) {
                        temp.delete();
                    }
                    return;
                }
                long max2 = trimToFit();
                long max3 = System.currentTimeMillis();
                byte[] buffer = new byte[this.mBlockSize];
                InputStream input = entry.getInputStream();
                int read = 0;
                while (true) {
                    if (read >= buffer.length) {
                        break;
                    }
                    int n = input.read(buffer, read, buffer.length - read);
                    if (n <= 0) {
                        break;
                    }
                    read += n;
                }
                File file = this.mDropBoxDir;
                StringBuilder sb = new StringBuilder();
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
                        int i = bufferSize;
                        StringBuilder sb2 = new StringBuilder();
                        FileOutputStream fileOutputStream = foutput;
                        sb2.append("Dropping: ");
                        sb2.append(tag);
                        sb2.append(" (");
                        long j = now;
                        sb2.append(temp2.length());
                        sb2.append(" > ");
                        sb2.append(lastTrim);
                        sb2.append(" bytes)");
                        Slog.w(TAG, sb2.toString());
                        temp2.delete();
                        temp2 = null;
                        break;
                    }
                    int bufferSize2 = bufferSize;
                    FileOutputStream foutput2 = foutput;
                    if (read <= 0) {
                        break;
                    }
                    int i2 = flags;
                    max4 = lastTrim;
                    max3 = max;
                    bufferSize = bufferSize2;
                    foutput = foutput2;
                }
                if (temp2 != null) {
                    FileUtils.setPermissions(temp2, 432, -1, -1);
                }
                long time = createEntry(temp2, tag, flags);
                temp = null;
                Intent dropboxIntent = new Intent("android.intent.action.DROPBOX_ENTRY_ADDED");
                dropboxIntent.putExtra("tag", tag);
                dropboxIntent.putExtra("time", time);
                if (!this.mBooted) {
                    dropboxIntent.addFlags(1073741824);
                }
                int i3 = flags;
                this.mHandler.sendMessage(this.mHandler.obtainMessage(1, dropboxIntent));
                IoUtils.closeQuietly(output);
                IoUtils.closeQuietly(input);
                entry.close();
            } else {
                throw new IllegalArgumentException();
            }
        } catch (IOException e) {
            Slog.e(TAG, "Can't write: " + tag, e);
            IoUtils.closeQuietly(null);
            IoUtils.closeQuietly(null);
            entry.close();
        } catch (Throwable th) {
            IoUtils.closeQuietly(null);
            IoUtils.closeQuietly(null);
            entry.close();
            if (temp != null) {
                temp.delete();
            }
            throw th;
        }
    }

    /* JADX INFO: finally extract failed */
    public boolean isTagEnabled(String tag) {
        long token = Binder.clearCallingIdentity();
        try {
            boolean z = !"disabled".equals(Settings.Global.getString(this.mContentResolver, "dropbox:" + tag));
            Binder.restoreCallingIdentity(token);
            return z;
        } catch (RuntimeException e) {
            Slog.e(TAG, "Failure getting tag enabled", e);
            Binder.restoreCallingIdentity(token);
            return false;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
            throw th;
        }
    }

    public synchronized DropBoxManager.Entry getNextEntry(String tag, long millis) {
        if (getContext().checkCallingOrSelfPermission("android.permission.READ_LOGS") == 0) {
            try {
                init();
                FileList list = tag == null ? this.mAllFiles : this.mFilesByTag.get(tag);
                if (list == null) {
                    return null;
                }
                for (EntryFile entry : list.contents.tailSet(new EntryFile(1 + millis))) {
                    if (entry.tag != null) {
                        if ((entry.flags & 1) != 0) {
                            return new DropBoxManager.Entry(entry.tag, entry.timestampMillis);
                        }
                        File file = entry.getFile(this.mDropBoxDir);
                        try {
                            DropBoxManager.Entry entry2 = new DropBoxManager.Entry(entry.tag, entry.timestampMillis, file, entry.flags);
                            return entry2;
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
        } else {
            throw new SecurityException("READ_LOGS permission required");
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:173:0x031c A[SYNTHETIC, Splitter:B:173:0x031c] */
    /* JADX WARNING: Removed duplicated region for block: B:176:0x0321 A[SYNTHETIC, Splitter:B:176:0x0321] */
    /* JADX WARNING: Removed duplicated region for block: B:182:0x032b A[SYNTHETIC, Splitter:B:182:0x032b] */
    /* JADX WARNING: Removed duplicated region for block: B:185:0x0330 A[SYNTHETIC, Splitter:B:185:0x0330] */
    /* JADX WARNING: Removed duplicated region for block: B:192:0x033e A[Catch:{ IOException -> 0x0379 }] */
    public synchronized void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        boolean z;
        int numFound;
        boolean doFile;
        Time time;
        int numArgs;
        Throwable th;
        ArrayList<String> searchArgs;
        PrintWriter printWriter = pw;
        String[] strArr = args;
        synchronized (this) {
            if (DumpUtils.checkDumpAndUsageStatsPermission(getContext(), TAG, printWriter)) {
                try {
                    init();
                    StringBuilder out = new StringBuilder();
                    ArrayList<String> searchArgs2 = new ArrayList<>();
                    boolean doFile2 = false;
                    boolean doPrint = false;
                    int i = 0;
                    while (strArr != null && i < strArr.length) {
                        if (!strArr[i].equals("-p")) {
                            if (!strArr[i].equals("--print")) {
                                if (!strArr[i].equals("-f")) {
                                    if (!strArr[i].equals("--file")) {
                                        if (!strArr[i].equals("-h")) {
                                            if (!strArr[i].equals("--help")) {
                                                if (strArr[i].startsWith("-")) {
                                                    out.append("Unknown argument: ");
                                                    out.append(strArr[i]);
                                                    out.append("\n");
                                                } else {
                                                    searchArgs2.add(strArr[i]);
                                                }
                                                i++;
                                            }
                                        }
                                        printWriter.println("Dropbox (dropbox) dump options:");
                                        printWriter.println("  [-h|--help] [-p|--print] [-f|--file] [timestamp]");
                                        printWriter.println("    -h|--help: print this help");
                                        printWriter.println("    -p|--print: print full contents of each entry");
                                        printWriter.println("    -f|--file: print path of each entry's file");
                                        printWriter.println("  [timestamp] optionally filters to only those entries.");
                                        return;
                                    }
                                }
                                doFile2 = true;
                                i++;
                            }
                        }
                        doPrint = true;
                        i++;
                    }
                    out.append("Drop box contents: ");
                    out.append(this.mAllFiles.contents.size());
                    out.append(" entries\n");
                    out.append("Max entries: ");
                    out.append(this.mMaxFiles);
                    out.append("\n");
                    if (!searchArgs2.isEmpty()) {
                        out.append("Searching for:");
                        Iterator<String> it = searchArgs2.iterator();
                        while (it.hasNext()) {
                            out.append(" ");
                            out.append(it.next());
                        }
                        out.append("\n");
                    }
                    int numFound2 = 0;
                    int numArgs2 = searchArgs2.size();
                    Time time2 = new Time();
                    out.append("\n");
                    Iterator<EntryFile> it2 = this.mAllFiles.contents.iterator();
                    while (it2.hasNext()) {
                        EntryFile entry = it2.next();
                        time2.set(entry.timestampMillis);
                        String date = time2.format("%Y-%m-%d %H:%M:%S");
                        boolean match = true;
                        int i2 = 0;
                        while (true) {
                            z = true;
                            if (i2 >= numArgs2 || !match) {
                                ArrayList<String> searchArgs3 = searchArgs2;
                            } else {
                                String arg = searchArgs2.get(i2);
                                if (!date.contains(arg)) {
                                    searchArgs = searchArgs2;
                                    if (!arg.equals(entry.tag)) {
                                        z = false;
                                    }
                                } else {
                                    searchArgs = searchArgs2;
                                }
                                match = z;
                                i2++;
                                searchArgs2 = searchArgs;
                            }
                        }
                        ArrayList<String> searchArgs32 = searchArgs2;
                        if (!match) {
                            searchArgs2 = searchArgs32;
                        } else {
                            int numFound3 = numFound2 + 1;
                            if (doPrint) {
                                out.append("========================================\n");
                            }
                            out.append(date);
                            out.append(" ");
                            out.append(entry.tag == null ? "(no tag)" : entry.tag);
                            File file = entry.getFile(this.mDropBoxDir);
                            if (file == null) {
                                out.append(" (no file)\n");
                            } else if ((entry.flags & 1) != 0) {
                                out.append(" (contents lost)\n");
                            } else {
                                out.append(" (");
                                if ((entry.flags & 4) != 0) {
                                    out.append("compressed ");
                                }
                                out.append((entry.flags & 2) != 0 ? "text" : "data");
                                out.append(", ");
                                numArgs = numArgs2;
                                time = time2;
                                out.append(file.length());
                                out.append(" bytes)\n");
                                if (doFile2 || (doPrint && (entry.flags & 2) == 0)) {
                                    if (!doPrint) {
                                        out.append("    ");
                                    }
                                    out.append(file.getPath());
                                    out.append("\n");
                                }
                                if ((entry.flags & 2) == 0) {
                                    numFound = numFound3;
                                    File file2 = file;
                                    doFile = doFile2;
                                } else if (doPrint || !doFile2) {
                                    DropBoxManager.Entry dbe = null;
                                    InputStreamReader isr = null;
                                    try {
                                        doFile = doFile2;
                                        try {
                                            numFound = numFound3;
                                            try {
                                                DropBoxManager.Entry entry2 = new DropBoxManager.Entry(entry.tag, entry.timestampMillis, file, entry.flags);
                                                dbe = entry2;
                                                char c = 10;
                                                if (doPrint) {
                                                    try {
                                                        isr = new InputStreamReader(dbe.getInputStream());
                                                        char[] buf = new char[4096];
                                                        boolean newline = false;
                                                        while (true) {
                                                            int n = isr.read(buf);
                                                            if (n <= 0) {
                                                                break;
                                                            }
                                                            File file3 = file;
                                                            try {
                                                                out.append(buf, 0, n);
                                                                newline = buf[n + -1] == c;
                                                                if (out.length() > 65536) {
                                                                    printWriter.write(out.toString());
                                                                    out.setLength(0);
                                                                }
                                                                file = file3;
                                                                c = 10;
                                                            } catch (IOException e) {
                                                                e = e;
                                                                try {
                                                                    out.append("*** ");
                                                                    out.append(e.toString());
                                                                    out.append("\n");
                                                                    if (dbe != null) {
                                                                    }
                                                                    if (isr != null) {
                                                                    }
                                                                    if (doPrint) {
                                                                    }
                                                                    searchArgs2 = searchArgs32;
                                                                    numArgs2 = numArgs;
                                                                    time2 = time;
                                                                    doFile2 = doFile;
                                                                    numFound2 = numFound;
                                                                } catch (Throwable th2) {
                                                                    th = th2;
                                                                    if (dbe != null) {
                                                                    }
                                                                    if (isr != null) {
                                                                    }
                                                                    throw th;
                                                                }
                                                            }
                                                        }
                                                        if (!newline) {
                                                            try {
                                                                out.append("\n");
                                                            } catch (IOException e2) {
                                                                e = e2;
                                                                File file4 = file;
                                                            } catch (Throwable th3) {
                                                                th = th3;
                                                                File file5 = file;
                                                                if (dbe != null) {
                                                                }
                                                                if (isr != null) {
                                                                }
                                                                throw th;
                                                            }
                                                        }
                                                        File file6 = file;
                                                    } catch (IOException e3) {
                                                        e = e3;
                                                        File file7 = file;
                                                        out.append("*** ");
                                                        out.append(e.toString());
                                                        out.append("\n");
                                                        if (dbe != null) {
                                                        }
                                                        if (isr != null) {
                                                        }
                                                        if (doPrint) {
                                                        }
                                                        searchArgs2 = searchArgs32;
                                                        numArgs2 = numArgs;
                                                        time2 = time;
                                                        doFile2 = doFile;
                                                        numFound2 = numFound;
                                                    } catch (Throwable th4) {
                                                        File file8 = file;
                                                        th = th4;
                                                        if (dbe != null) {
                                                        }
                                                        if (isr != null) {
                                                        }
                                                        throw th;
                                                    }
                                                } else {
                                                    File file9 = file;
                                                    String text = dbe.getText(70);
                                                    out.append("    ");
                                                    if (text == null) {
                                                        out.append("[null]");
                                                    } else {
                                                        if (text.length() != 70) {
                                                            z = false;
                                                        }
                                                        boolean truncated = z;
                                                        out.append(text.trim().replace(10, '/'));
                                                        if (truncated) {
                                                            out.append(" ...");
                                                        }
                                                    }
                                                    out.append("\n");
                                                }
                                                dbe.close();
                                                if (isr != null) {
                                                    try {
                                                        isr.close();
                                                    } catch (IOException e4) {
                                                    }
                                                }
                                            } catch (IOException e5) {
                                                e = e5;
                                                File file10 = file;
                                                dbe = null;
                                                out.append("*** ");
                                                out.append(e.toString());
                                                out.append("\n");
                                                if (dbe != null) {
                                                }
                                                if (isr != null) {
                                                }
                                                if (doPrint) {
                                                }
                                                searchArgs2 = searchArgs32;
                                                numArgs2 = numArgs;
                                                time2 = time;
                                                doFile2 = doFile;
                                                numFound2 = numFound;
                                            } catch (Throwable th5) {
                                                File file11 = file;
                                                th = th5;
                                                dbe = null;
                                                if (dbe != null) {
                                                }
                                                if (isr != null) {
                                                }
                                                throw th;
                                            }
                                        } catch (IOException e6) {
                                            e = e6;
                                            numFound = numFound3;
                                            File file12 = file;
                                            dbe = null;
                                            out.append("*** ");
                                            out.append(e.toString());
                                            out.append("\n");
                                            if (dbe != null) {
                                                dbe.close();
                                            }
                                            if (isr != null) {
                                                isr.close();
                                            }
                                            if (doPrint) {
                                            }
                                            searchArgs2 = searchArgs32;
                                            numArgs2 = numArgs;
                                            time2 = time;
                                            doFile2 = doFile;
                                            numFound2 = numFound;
                                        } catch (Throwable th6) {
                                            int i3 = numFound3;
                                            File file13 = file;
                                            th = th6;
                                            dbe = null;
                                            if (dbe != null) {
                                                dbe.close();
                                            }
                                            if (isr != null) {
                                                try {
                                                    isr.close();
                                                } catch (IOException e7) {
                                                }
                                            }
                                            throw th;
                                        }
                                    } catch (IOException e8) {
                                        e = e8;
                                        numFound = numFound3;
                                        File file14 = file;
                                        doFile = doFile2;
                                        out.append("*** ");
                                        out.append(e.toString());
                                        out.append("\n");
                                        if (dbe != null) {
                                        }
                                        if (isr != null) {
                                        }
                                        if (doPrint) {
                                        }
                                        searchArgs2 = searchArgs32;
                                        numArgs2 = numArgs;
                                        time2 = time;
                                        doFile2 = doFile;
                                        numFound2 = numFound;
                                    } catch (Throwable th7) {
                                        int i4 = numFound3;
                                        File file15 = file;
                                        boolean z2 = doFile2;
                                        th = th7;
                                        if (dbe != null) {
                                        }
                                        if (isr != null) {
                                        }
                                        throw th;
                                    }
                                } else {
                                    numFound = numFound3;
                                    File file16 = file;
                                    doFile = doFile2;
                                }
                                if (doPrint) {
                                    out.append("\n");
                                }
                                searchArgs2 = searchArgs32;
                                numArgs2 = numArgs;
                                time2 = time;
                                doFile2 = doFile;
                                numFound2 = numFound;
                            }
                            numFound = numFound3;
                            doFile = doFile2;
                            numArgs = numArgs2;
                            time = time2;
                            searchArgs2 = searchArgs32;
                            numArgs2 = numArgs;
                            time2 = time;
                            doFile2 = doFile;
                            numFound2 = numFound;
                        }
                    }
                    boolean z3 = doFile2;
                    int i5 = numArgs2;
                    Time time3 = time2;
                    if (numFound2 == 0) {
                        out.append("(No entries found.)\n");
                    }
                    if (strArr == null || strArr.length == 0) {
                        if (!doPrint) {
                            out.append("\n");
                        }
                        out.append("Usage: dumpsys dropbox [--print|--file] [YYYY-mm-dd] [HH:MM:SS] [tag]\n");
                    }
                    printWriter.write(out.toString());
                } catch (IOException e9) {
                    IOException iOException = e9;
                    printWriter.println("Can't initialize: " + e9);
                    Slog.e(TAG, "Can't init", e9);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public synchronized void init() throws IOException {
        if (!this.mDropBoxDir.exists() || !this.mDropBoxDir.isDirectory()) {
            this.mStatFs = null;
            this.mAllFiles = null;
        }
        if (this.mStatFs == null) {
            if (!this.mDropBoxDir.isDirectory()) {
                if (!this.mDropBoxDir.mkdirs()) {
                    throw new IOException("Can't mkdir: " + this.mDropBoxDir);
                }
            }
            try {
                FileUtils.setPermissions(this.mDropBoxDir, 504, -1, -1);
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
        synchronized (this) {
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
                        EntryFile entryFile = r9;
                        EntryFile entryFile2 = new EntryFile(late.getFile(this.mDropBoxDir), this.mDropBoxDir, late.tag, t, late.flags, this.mBlockSize);
                        enrollEntry(entryFile);
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
                SortedSet<EntryFile> sortedSet = tail2;
                t = t2;
            }
            if (temp == null) {
                enrollEntry(new EntryFile(this.mDropBoxDir, tag, t));
            } else {
                EntryFile entryFile3 = new EntryFile(temp, this.mDropBoxDir, tag, t, flags, this.mBlockSize);
                enrollEntry(entryFile3);
            }
        }
        return t;
    }

    /* access modifiers changed from: private */
    public synchronized long trimToFit() throws IOException {
        int i;
        long j;
        long cutoffMillis;
        int ageSeconds;
        synchronized (this) {
            int ageSeconds2 = Settings.Global.getInt(this.mContentResolver, "dropbox_age_seconds", DEFAULT_AGE_SECONDS);
            ContentResolver contentResolver = this.mContentResolver;
            if (ActivityManager.isLowRamDeviceStatic()) {
                i = 300;
            } else {
                i = 1000;
            }
            this.mMaxFiles = Settings.Global.getInt(contentResolver, "dropbox_max_files", i);
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
                if (Log.HWINFO) {
                    reservePercent = 1;
                }
                int reservePercent2 = reservePercent;
                int quotaKb = Settings.Global.getInt(this.mContentResolver, "dropbox_quota_kb", DEFAULT_QUOTA_KB);
                try {
                    this.mStatFs.restat(this.mDropBoxDir.getPath());
                    this.mCachedQuotaBlocks = Math.min((quotaKb * 1024) / this.mBlockSize, Math.max(0, ((this.mStatFs.getAvailableBlocks() - ((this.mStatFs.getBlockCount() * reservePercent2) / 100)) * quotaPercent) / 100));
                    this.mCachedQuotaUptimeMillis = uptimeMillis;
                } catch (IllegalArgumentException e) {
                    throw new IOException("Can't restat: " + this.mDropBoxDir);
                }
            }
            if (this.mAllFiles.blocks > this.mCachedQuotaBlocks) {
                int unsqueezed = this.mAllFiles.blocks;
                TreeSet<FileList> tags = new TreeSet<>(this.mFilesByTag.values());
                Iterator<FileList> it = tags.iterator();
                int squeezed = 0;
                int unsqueezed2 = unsqueezed;
                while (true) {
                    if (it.hasNext() == 0) {
                        break;
                    }
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
                        int i2 = ageSeconds2;
                        long j2 = cutoffMillis2;
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
                            } catch (IOException e2) {
                                e = e2;
                            }
                        } catch (IOException e3) {
                            e = e3;
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
                j = (long) (this.mCachedQuotaBlocks * this.mBlockSize);
            }
            long j3 = cutoffMillis2;
            j = (long) (this.mCachedQuotaBlocks * this.mBlockSize);
        }
        return j;
    }
}
