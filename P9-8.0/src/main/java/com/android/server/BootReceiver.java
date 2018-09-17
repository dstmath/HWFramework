package com.android.server;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.IPackageManager.Stub;
import android.os.Build;
import android.os.DropBoxManager;
import android.os.Environment;
import android.os.FileObserver;
import android.os.FileUtils;
import android.os.RecoverySystem;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.storage.StorageManager;
import android.provider.Downloads;
import android.provider.Telephony.Sms.Intents;
import android.util.AtomicFile;
import android.util.LogException;
import android.util.Slog;
import android.util.Xml;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.XmlUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class BootReceiver extends BroadcastReceiver {
    private static final String FSCK_FS_MODIFIED = "FILE SYSTEM WAS MODIFIED";
    private static final String FSCK_PASS_PATTERN = "Pass ([1-9]E?):";
    private static final String FSCK_TREE_OPTIMIZATION_PATTERN = "Inode [0-9]+ extent tree.*could be shorter";
    private static final int FS_STAT_FS_FIXED = 1024;
    private static final String FS_STAT_PATTERN = "fs_stat,[^,]*/([^/,]+),(0x[0-9a-fA-F]+)";
    private static final String LAST_HEADER_FILE = "last-header.txt";
    private static final String[] LAST_KMSG_FILES = new String[]{"/sys/fs/pstore/console-ramoops", "/proc/last_kmsg"};
    private static final String LAST_SHUTDOWN_TIME_PATTERN = "powerctl_shutdown_time_ms:([0-9]+):([0-9]+)";
    private static final String LOG_FILES_FILE = "log-files.xml";
    private static final int LOG_SIZE = (SystemProperties.getInt("ro.debuggable", 0) == 1 ? 98304 : 65536);
    private static final String[] MOUNT_DURATION_PROPS_POSTFIX = new String[]{"early", PhoneConstants.APN_TYPE_DEFAULT, "late"};
    private static final String OLD_UPDATER_CLASS = "com.google.android.systemupdater.SystemUpdateReceiver";
    private static final String OLD_UPDATER_PACKAGE = "com.google.android.systemupdater";
    private static final File PSTORE_DIR = new File("/mnt/pstore");
    private static final String TAG = "BootReceiver";
    private static final File TOMBSTONE_DIR = new File("/data/tombstones");
    private static final int UMOUNT_STATUS_NOT_AVAILABLE = 4;
    private static final File lastHeaderFile = new File(Environment.getDataSystemDirectory(), LAST_HEADER_FILE);
    private static final AtomicFile sFile = new AtomicFile(new File(Environment.getDataSystemDirectory(), LOG_FILES_FILE));
    private static FileObserver sPstoreObserver = null;
    private static FileObserver sTombstoneObserver = null;

    public void onReceive(final Context context, Intent intent) {
        new Thread() {
            public void run() {
                try {
                    BootReceiver.this.logBootEvents(context);
                } catch (Exception e) {
                    Slog.e(BootReceiver.TAG, "Can't log boot events", e);
                }
                boolean onlyCore = false;
                try {
                    onlyCore = Stub.asInterface(ServiceManager.getService(Intents.EXTRA_PACKAGE_NAME)).isOnlyCoreApps();
                } catch (RemoteException e2) {
                }
                if (!onlyCore) {
                    try {
                        BootReceiver.this.removeOldUpdatePackages(context);
                    } catch (Exception e3) {
                        Slog.e(BootReceiver.TAG, "Can't remove old update packages", e3);
                    }
                }
            }
        }.start();
    }

    private void removeOldUpdatePackages(Context context) {
        Downloads.removeAllDownloadsByPackage(context, OLD_UPDATER_PACKAGE, OLD_UPDATER_CLASS);
    }

    private String getPreviousBootHeaders() {
        try {
            return FileUtils.readTextFile(lastHeaderFile, 0, null);
        } catch (IOException e) {
            Slog.e(TAG, "Error reading " + lastHeaderFile, e);
            return null;
        }
    }

    private String getCurrentBootHeaders() throws IOException {
        return "Build: " + Build.FINGERPRINT + "\n" + "Hardware: " + Build.BOARD + "\n" + "Revision: " + SystemProperties.get("ro.revision", LogException.NO_VALUE) + "\n" + "Bootloader: " + Build.BOOTLOADER + "\n" + "Radio: " + Build.RADIO + "\n" + "Kernel: " + FileUtils.readTextFile(new File("/proc/version"), 1024, "...\n") + "\n";
    }

    private String getBootHeadersToLogAndUpdate() throws IOException {
        String oldHeaders = getPreviousBootHeaders();
        String newHeaders = getCurrentBootHeaders();
        try {
            FileUtils.stringToFile(lastHeaderFile, newHeaders);
        } catch (IOException e) {
            Slog.e(TAG, "Error writing " + lastHeaderFile, e);
        }
        if (oldHeaders == null) {
            return "isPrevious: false\n" + newHeaders;
        }
        return "isPrevious: true\n" + oldHeaders;
    }

    private void logBootEvents(Context ctx) throws IOException {
        DropBoxManager db = (DropBoxManager) ctx.getSystemService("dropbox");
        String headers = getBootHeadersToLogAndUpdate();
        String bootReason = SystemProperties.get("ro.boot.bootreason", null);
        String recovery = RecoverySystem.handleAftermath(ctx);
        if (!(recovery == null || db == null)) {
            db.addText("SYSTEM_RECOVERY_LOG", headers + recovery);
        }
        String lastKmsgFooter = LogException.NO_VALUE;
        if (bootReason != null) {
            lastKmsgFooter = "\n" + "Boot info:\n" + "Last boot reason: " + bootReason + "\n";
        }
        HashMap<String, Long> timestamps = readTimestamps();
        if (SystemProperties.getLong("ro.runtime.firstboot", 0) == 0) {
            if (!StorageManager.inCryptKeeperBounce()) {
                SystemProperties.set("ro.runtime.firstboot", Long.toString(System.currentTimeMillis()));
            }
            if (db != null) {
                db.addText("SYSTEM_BOOT", headers);
            }
            addFileWithFootersToDropBox(db, timestamps, headers, lastKmsgFooter, "/proc/last_kmsg", -LOG_SIZE, "SYSTEM_LAST_KMSG");
            addFileWithFootersToDropBox(db, timestamps, headers, lastKmsgFooter, "/sys/fs/pstore/console-ramoops", -LOG_SIZE, "SYSTEM_LAST_KMSG");
            addFileToDropBox(db, timestamps, headers, "/cache/recovery/log", -LOG_SIZE, "SYSTEM_RECOVERY_LOG");
            addFileToDropBox(db, timestamps, headers, "/cache/recovery/last_kmsg", -LOG_SIZE, "SYSTEM_RECOVERY_KMSG");
            addAuditErrorsToDropBox(db, timestamps, headers, -LOG_SIZE, "SYSTEM_AUDIT");
        } else if (db != null) {
            db.addText("SYSTEM_RESTART", headers);
        }
        logFsShutdownTime();
        logFsMountTime();
        addFsckErrorsToDropBoxAndLogFsStat(db, timestamps, headers, -LOG_SIZE, "SYSTEM_FSCK");
        File[] tombstoneFiles = TOMBSTONE_DIR.listFiles();
        int i = 0;
        while (tombstoneFiles != null && i < tombstoneFiles.length) {
            if (tombstoneFiles[i].isFile()) {
                addFileToDropBox(db, timestamps, headers, tombstoneFiles[i].getPath(), LOG_SIZE, "SYSTEM_TOMBSTONE");
            }
            i++;
        }
        writeTimestamps(timestamps);
        if (!(TOMBSTONE_DIR.exists() || TOMBSTONE_DIR.mkdirs())) {
            Slog.e(TAG, "Can't create a empty TOMBSTONE_DIR");
        }
        final DropBoxManager dropBoxManager = db;
        final String str = headers;
        sTombstoneObserver = new FileObserver(TOMBSTONE_DIR.getPath(), 8) {
            public void onEvent(int event, String path) {
                if (path != null) {
                    HashMap<String, Long> timestamps = BootReceiver.readTimestamps();
                    try {
                        File file = new File(BootReceiver.TOMBSTONE_DIR, path);
                        if (file.isFile()) {
                            BootReceiver.addFileToDropBox(dropBoxManager, timestamps, str, file.getPath(), BootReceiver.LOG_SIZE, "SYSTEM_TOMBSTONE");
                        }
                    } catch (IOException e) {
                        Slog.e(BootReceiver.TAG, "Can't log tombstone", e);
                    } catch (NullPointerException e2) {
                    }
                    BootReceiver.this.writeTimestamps(timestamps);
                }
            }
        };
        sTombstoneObserver.startWatching();
        File[] pstoreFiles = PSTORE_DIR.listFiles();
        i = 0;
        while (pstoreFiles != null && i < pstoreFiles.length) {
            addFileToDropBox(db, timestamps, headers, pstoreFiles[i].getPath(), -LOG_SIZE, "SYSTEM_PSTORE");
            i++;
        }
        dropBoxManager = db;
        final HashMap<String, Long> hashMap = timestamps;
        final String str2 = headers;
        sPstoreObserver = new FileObserver(PSTORE_DIR.getPath(), 8) {
            public void onEvent(int event, String path) {
                try {
                    BootReceiver.addFileToDropBox(dropBoxManager, hashMap, str2, new File(BootReceiver.PSTORE_DIR, path).getPath(), -BootReceiver.LOG_SIZE, "SYSTEM_PSTORE");
                } catch (IOException e) {
                    Slog.e(BootReceiver.TAG, "Can't log pstore", e);
                } catch (NullPointerException e2) {
                }
            }
        };
        sPstoreObserver.startWatching();
    }

    private static void addFileToDropBox(DropBoxManager db, HashMap<String, Long> timestamps, String headers, String filename, int maxSize, String tag) throws IOException {
        addFileWithFootersToDropBox(db, timestamps, headers, LogException.NO_VALUE, filename, maxSize, tag);
    }

    private static void addFileWithFootersToDropBox(DropBoxManager db, HashMap<String, Long> timestamps, String headers, String footers, String filename, int maxSize, String tag) throws IOException {
        if (db != null && (db.isTagEnabled(tag) ^ 1) == 0) {
            File file = new File(filename);
            if (!file.isDirectory()) {
                long fileTime = file.lastModified();
                if (fileTime > 0) {
                    if (!timestamps.containsKey(filename) || ((Long) timestamps.get(filename)).longValue() != fileTime) {
                        timestamps.put(filename, Long.valueOf(fileTime));
                        Slog.i(TAG, "Copying " + filename + " to DropBox (" + tag + ")");
                        db.addText(tag, headers + FileUtils.readTextFile(file, maxSize, "[[TRUNCATED]]\n") + footers);
                    }
                }
            }
        }
    }

    private static void addAuditErrorsToDropBox(DropBoxManager db, HashMap<String, Long> timestamps, String headers, int maxSize, String tag) throws IOException {
        if (db != null && (db.isTagEnabled(tag) ^ 1) == 0) {
            Slog.i(TAG, "Copying audit failures to DropBox");
            File file = new File("/proc/last_kmsg");
            long fileTime = file.lastModified();
            if (fileTime <= 0) {
                file = new File("/sys/fs/pstore/console-ramoops");
                fileTime = file.lastModified();
            }
            if (fileTime > 0) {
                if (!timestamps.containsKey(tag) || ((Long) timestamps.get(tag)).longValue() != fileTime) {
                    timestamps.put(tag, Long.valueOf(fileTime));
                    String log = FileUtils.readTextFile(file, maxSize, "[[TRUNCATED]]\n");
                    StringBuilder sb = new StringBuilder();
                    for (String line : log.split("\n")) {
                        if (line.contains("audit")) {
                            sb.append(line).append("\n");
                        }
                    }
                    Slog.i(TAG, "Copied " + sb.toString().length() + " worth of audits to DropBox");
                    db.addText(tag, headers + sb.toString());
                }
            }
        }
    }

    private static void addFsckErrorsToDropBoxAndLogFsStat(DropBoxManager db, HashMap<String, Long> timestamps, String headers, int maxSize, String tag) throws IOException {
        boolean uploadEnabled = true;
        if (db == null || (db.isTagEnabled(tag) ^ 1) != 0) {
            uploadEnabled = false;
        }
        boolean uploadNeeded = false;
        Slog.i(TAG, "Checking for fsck errors");
        File file = new File("/dev/fscklogs/log");
        if (file.lastModified() > 0) {
            String log = FileUtils.readTextFile(file, maxSize, "[[TRUNCATED]]\n");
            Pattern pattern = Pattern.compile(FS_STAT_PATTERN);
            String[] lines = log.split("\n");
            int lineNumber = 0;
            int lastFsStatLineNumber = 0;
            for (String line : lines) {
                if (line.contains(FSCK_FS_MODIFIED)) {
                    uploadNeeded = true;
                } else if (line.contains("fs_stat")) {
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        handleFsckFsStat(matcher, lines, lastFsStatLineNumber, lineNumber);
                        lastFsStatLineNumber = lineNumber;
                    } else {
                        Slog.w(TAG, "cannot parse fs_stat:" + line);
                    }
                }
                lineNumber++;
            }
            if (uploadEnabled && uploadNeeded) {
                addFileToDropBox(db, timestamps, headers, "/dev/fscklogs/log", maxSize, tag);
            }
            file.delete();
        }
    }

    private static void logFsMountTime() {
        for (String propPostfix : MOUNT_DURATION_PROPS_POSTFIX) {
            int duration = SystemProperties.getInt("ro.boottime.init.mount_all." + propPostfix, 0);
            if (duration != 0) {
                MetricsLogger.histogram(null, "boot_mount_all_duration_" + propPostfix, duration);
            }
        }
    }

    private static void logFsShutdownTime() {
        File f = null;
        for (String fileName : LAST_KMSG_FILES) {
            File file = new File(fileName);
            if (file.exists()) {
                f = file;
                break;
            }
        }
        if (f != null) {
            try {
                Matcher matcher = Pattern.compile(LAST_SHUTDOWN_TIME_PATTERN, 8).matcher(FileUtils.readTextFile(f, -16384, null));
                if (matcher.find()) {
                    MetricsLogger.histogram(null, "boot_fs_shutdown_duration", Integer.parseInt(matcher.group(1)));
                    MetricsLogger.histogram(null, "boot_fs_shutdown_umount_stat", Integer.parseInt(matcher.group(2)));
                    Slog.i(TAG, "boot_fs_shutdown," + matcher.group(1) + "," + matcher.group(2));
                } else {
                    MetricsLogger.histogram(null, "boot_fs_shutdown_umount_stat", 4);
                    Slog.w(TAG, "boot_fs_shutdown, string not found");
                }
            } catch (IOException e) {
                Slog.w(TAG, "cannot read last msg", e);
            }
        }
    }

    public static int fixFsckFsStat(String partition, int statOrg, String[] lines, int startLineNumber, int endLineNumber) {
        int stat = statOrg;
        if ((statOrg & 1024) == 0) {
            return stat;
        }
        Pattern passPattern = Pattern.compile(FSCK_PASS_PATTERN);
        Pattern treeOptPattern = Pattern.compile(FSCK_TREE_OPTIMIZATION_PATTERN);
        String currentPass = LogException.NO_VALUE;
        boolean foundTreeOptimization = false;
        boolean foundQuotaFix = false;
        boolean foundOtherFix = false;
        String otherFixLine = null;
        for (int i = startLineNumber; i < endLineNumber; i++) {
            String line = lines[i];
            if (line.contains(FSCK_FS_MODIFIED)) {
                break;
            }
            if (line.startsWith("Pass ")) {
                Matcher matcher = passPattern.matcher(line);
                if (matcher.find()) {
                    currentPass = matcher.group(1);
                }
            } else if (line.startsWith("Inode ")) {
                if (!treeOptPattern.matcher(line).find() || !currentPass.equals("1")) {
                    foundOtherFix = true;
                    otherFixLine = line;
                    break;
                }
                foundTreeOptimization = true;
                Slog.i(TAG, "fs_stat, partition:" + partition + " found tree optimization:" + line);
            } else if (line.startsWith("[QUOTA WARNING]") && currentPass.equals("5")) {
                Slog.i(TAG, "fs_stat, partition:" + partition + " found quota warning:" + line);
                foundQuotaFix = true;
                if (!foundTreeOptimization) {
                    otherFixLine = line;
                    break;
                }
            } else if (!line.startsWith("Update quota info") || !currentPass.equals("5")) {
                line = line.trim();
                if (!(line.isEmpty() || (currentPass.isEmpty() ^ 1) == 0)) {
                    foundOtherFix = true;
                    otherFixLine = line;
                    break;
                }
            }
        }
        if (!foundOtherFix && foundTreeOptimization && foundQuotaFix) {
            Slog.i(TAG, "fs_stat, partition:" + partition + " quota fix due to tree optimization");
            return statOrg & -1025;
        } else if (otherFixLine == null) {
            return stat;
        } else {
            Slog.i(TAG, "fs_stat, partition:" + partition + " fix:" + otherFixLine);
            return stat;
        }
    }

    private static void handleFsckFsStat(Matcher match, String[] lines, int startLineNumber, int endLineNumber) {
        String partition = match.group(1);
        try {
            int stat = fixFsckFsStat(partition, Integer.decode(match.group(2)).intValue(), lines, startLineNumber, endLineNumber);
            MetricsLogger.histogram(null, "boot_fs_stat_" + partition, stat);
            Slog.i(TAG, "fs_stat, partition:" + partition + " stat:0x" + Integer.toHexString(stat));
        } catch (NumberFormatException e) {
            Slog.w(TAG, "cannot parse fs_stat: partition:" + partition + " stat:" + match.group(2));
        }
    }

    private static HashMap<String, Long> readTimestamps() {
        HashMap<String, Long> timestamps;
        boolean success;
        Throwable th;
        FileInputStream stream;
        Throwable th2;
        synchronized (sFile) {
            timestamps = new HashMap();
            success = false;
            th = null;
            stream = null;
            try {
                int type;
                stream = sFile.openRead();
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(stream, StandardCharsets.UTF_8.name());
                do {
                    type = parser.next();
                    if (type == 2) {
                        break;
                    }
                } while (type != 1);
                if (type != 2) {
                    throw new IllegalStateException("no start tag found");
                }
                int outerDepth = parser.getDepth();
                while (true) {
                    type = parser.next();
                    if (type == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                        success = true;
                    } else if (!(type == 3 || type == 4)) {
                        if (parser.getName().equals("log")) {
                            timestamps.put(parser.getAttributeValue(null, "filename"), Long.valueOf(Long.valueOf(parser.getAttributeValue(null, "timestamp")).longValue()));
                        } else {
                            Slog.w(TAG, "Unknown tag: " + parser.getName());
                            XmlUtils.skipCurrentTag(parser);
                        }
                    }
                }
                success = true;
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (Throwable th3) {
                        th = th3;
                    }
                }
                if (th != null) {
                    throw th;
                } else {
                    if (1 == null) {
                        timestamps.clear();
                    }
                }
            } catch (Throwable th4) {
                Throwable th5 = th4;
                th4 = th2;
                th2 = th5;
            }
        }
        return timestamps;
        if (stream != null) {
            try {
                stream.close();
            } catch (Throwable th6) {
                if (th4 == null) {
                    th4 = th6;
                } else if (th4 != th6) {
                    th4.addSuppressed(th6);
                }
            }
        }
        if (th4 != null) {
            try {
                throw th4;
            } catch (FileNotFoundException e) {
                Slog.i(TAG, "No existing last log timestamp file " + sFile.getBaseFile() + "; starting empty");
                if (!success) {
                    timestamps.clear();
                }
            } catch (IOException e2) {
                Slog.w(TAG, "Failed parsing " + e2);
                if (!success) {
                    timestamps.clear();
                }
            } catch (IllegalStateException e3) {
                Slog.w(TAG, "Failed parsing " + e3);
                if (!success) {
                    timestamps.clear();
                }
            } catch (NullPointerException e4) {
                Slog.w(TAG, "Failed parsing " + e4);
                if (!success) {
                    timestamps.clear();
                }
            } catch (XmlPullParserException e5) {
                Slog.w(TAG, "Failed parsing " + e5);
                if (!success) {
                    timestamps.clear();
                }
            } catch (Throwable th7) {
                if (!success) {
                    timestamps.clear();
                }
            }
        } else {
            throw th2;
        }
    }

    private void writeTimestamps(HashMap<String, Long> timestamps) {
        synchronized (sFile) {
            try {
                FileOutputStream stream = sFile.startWrite();
                try {
                    XmlSerializer out = new FastXmlSerializer();
                    out.setOutput(stream, StandardCharsets.UTF_8.name());
                    out.startDocument(null, Boolean.valueOf(true));
                    out.startTag(null, "log-files");
                    for (String filename : timestamps.keySet()) {
                        out.startTag(null, "log");
                        out.attribute(null, "filename", filename);
                        out.attribute(null, "timestamp", ((Long) timestamps.get(filename)).toString());
                        out.endTag(null, "log");
                    }
                    out.endTag(null, "log-files");
                    out.endDocument();
                    sFile.finishWrite(stream);
                } catch (IOException e) {
                    Slog.w(TAG, "Failed to write timestamp file, using the backup: " + e);
                    sFile.failWrite(stream);
                }
            } catch (IOException e2) {
                Slog.w(TAG, "Failed to write timestamp file: " + e2);
                return;
            }
        }
        return;
    }
}
