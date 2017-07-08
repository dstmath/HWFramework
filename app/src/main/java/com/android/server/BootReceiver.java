package com.android.server;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.IPackageManager.Stub;
import android.os.Build;
import android.os.DropBoxManager;
import android.os.FileObserver;
import android.os.FileUtils;
import android.os.RecoverySystem;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.storage.StorageManager;
import android.provider.Downloads;
import android.util.AtomicFile;
import android.util.Slog;
import android.util.Xml;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.XmlUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import javax.microedition.khronos.opengles.GL10;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

public class BootReceiver extends BroadcastReceiver {
    private static final String LOG_FILES_FILE = "log-files.xml";
    private static final int LOG_SIZE = 0;
    private static final String OLD_UPDATER_CLASS = "com.google.android.systemupdater.SystemUpdateReceiver";
    private static final String OLD_UPDATER_PACKAGE = "com.google.android.systemupdater";
    private static final File PSTORE_DIR = null;
    private static final String TAG = "BootReceiver";
    private static final File TOMBSTONE_DIR = null;
    private static final AtomicFile sFile = null;
    private static FileObserver sPstoreObserver;
    private static FileObserver sTombstoneObserver;

    /* renamed from: com.android.server.BootReceiver.1 */
    class AnonymousClass1 extends Thread {
        final /* synthetic */ Context val$context;

        AnonymousClass1(Context val$context) {
            this.val$context = val$context;
        }

        public void run() {
            try {
                BootReceiver.this.logBootEvents(this.val$context);
            } catch (Exception e) {
                Slog.e(BootReceiver.TAG, "Can't log boot events", e);
            }
            boolean onlyCore = false;
            try {
                onlyCore = Stub.asInterface(ServiceManager.getService("package")).isOnlyCoreApps();
            } catch (RemoteException e2) {
            }
            if (!onlyCore) {
                try {
                    BootReceiver.this.removeOldUpdatePackages(this.val$context);
                } catch (Exception e3) {
                    Slog.e(BootReceiver.TAG, "Can't remove old update packages", e3);
                }
            }
        }
    }

    /* renamed from: com.android.server.BootReceiver.2 */
    class AnonymousClass2 extends FileObserver {
        final /* synthetic */ DropBoxManager val$db;
        final /* synthetic */ String val$headers;

        AnonymousClass2(String $anonymous0, int $anonymous1, DropBoxManager val$db, String val$headers) {
            this.val$db = val$db;
            this.val$headers = val$headers;
            super($anonymous0, $anonymous1);
        }

        public void onEvent(int event, String path) {
            if (path != null) {
                HashMap<String, Long> timestamps = BootReceiver.readTimestamps();
                try {
                    File file = new File(BootReceiver.TOMBSTONE_DIR, path);
                    if (file.isFile()) {
                        BootReceiver.addFileToDropBox(this.val$db, timestamps, this.val$headers, file.getPath(), BootReceiver.LOG_SIZE, "SYSTEM_TOMBSTONE");
                    }
                } catch (IOException e) {
                    Slog.e(BootReceiver.TAG, "Can't log tombstone", e);
                } catch (NullPointerException e2) {
                }
                BootReceiver.this.writeTimestamps(timestamps);
            }
        }
    }

    /* renamed from: com.android.server.BootReceiver.3 */
    class AnonymousClass3 extends FileObserver {
        final /* synthetic */ DropBoxManager val$db;
        final /* synthetic */ String val$headers;
        final /* synthetic */ HashMap val$timestamps;

        AnonymousClass3(String $anonymous0, int $anonymous1, DropBoxManager val$db, HashMap val$timestamps, String val$headers) {
            this.val$db = val$db;
            this.val$timestamps = val$timestamps;
            this.val$headers = val$headers;
            super($anonymous0, $anonymous1);
        }

        public void onEvent(int event, String path) {
            try {
                BootReceiver.addFileToDropBox(this.val$db, this.val$timestamps, this.val$headers, new File(BootReceiver.PSTORE_DIR, path).getPath(), -BootReceiver.LOG_SIZE, "SYSTEM_PSTORE");
            } catch (IOException e) {
                Slog.e(BootReceiver.TAG, "Can't log pstore", e);
            } catch (NullPointerException e2) {
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.BootReceiver.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.BootReceiver.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.BootReceiver.<clinit>():void");
    }

    public void onReceive(Context context, Intent intent) {
        new AnonymousClass1(context).start();
    }

    private void removeOldUpdatePackages(Context context) {
        Downloads.removeAllDownloadsByPackage(context, OLD_UPDATER_PACKAGE, OLD_UPDATER_CLASS);
    }

    private void logBootEvents(Context ctx) throws IOException {
        DropBoxManager db = (DropBoxManager) ctx.getSystemService("dropbox");
        String headers = new StringBuilder(GL10.GL_NEVER).append("Build: ").append(Build.FINGERPRINT).append("\n").append("Hardware: ").append(Build.BOARD).append("\n").append("Revision: ").append(SystemProperties.get("ro.revision", "")).append("\n").append("Bootloader: ").append(Build.BOOTLOADER).append("\n").append("Radio: ").append(Build.RADIO).append("\n").append("Kernel: ").append(FileUtils.readTextFile(new File("/proc/version"), GL10.GL_STENCIL_BUFFER_BIT, "...\n")).append("\n").toString();
        String bootReason = SystemProperties.get("ro.boot.bootreason", null);
        String recovery = RecoverySystem.handleAftermath(ctx);
        if (!(recovery == null || db == null)) {
            db.addText("SYSTEM_RECOVERY_LOG", headers + recovery);
        }
        String lastKmsgFooter = "";
        if (bootReason != null) {
            lastKmsgFooter = new StringBuilder(GL10.GL_NEVER).append("\n").append("Boot info:\n").append("Last boot reason: ").append(bootReason).append("\n").toString();
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
            addFsckErrorsToDropBox(db, timestamps, headers, -LOG_SIZE, "SYSTEM_FSCK");
        } else if (db != null) {
            db.addText("SYSTEM_RESTART", headers);
        }
        File[] tombstoneFiles = TOMBSTONE_DIR.listFiles();
        int i = LOG_SIZE;
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
        sTombstoneObserver = new AnonymousClass2(TOMBSTONE_DIR.getPath(), 8, db, headers);
        sTombstoneObserver.startWatching();
        File[] pstoreFiles = PSTORE_DIR.listFiles();
        i = LOG_SIZE;
        while (pstoreFiles != null && i < pstoreFiles.length) {
            addFileToDropBox(db, timestamps, headers, pstoreFiles[i].getPath(), -LOG_SIZE, "SYSTEM_PSTORE");
            i++;
        }
        sPstoreObserver = new AnonymousClass3(PSTORE_DIR.getPath(), 8, db, timestamps, headers);
        sPstoreObserver.startWatching();
    }

    private static void addFileToDropBox(DropBoxManager db, HashMap<String, Long> timestamps, String headers, String filename, int maxSize, String tag) throws IOException {
        addFileWithFootersToDropBox(db, timestamps, headers, "", filename, maxSize, tag);
    }

    private static void addFileWithFootersToDropBox(DropBoxManager db, HashMap<String, Long> timestamps, String headers, String footers, String filename, int maxSize, String tag) throws IOException {
        if (db != null && db.isTagEnabled(tag)) {
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
        if (db != null && db.isTagEnabled(tag)) {
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
                    String[] split = log.split("\n");
                    int length = split.length;
                    for (int i = LOG_SIZE; i < length; i++) {
                        String line = split[i];
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

    private static void addFsckErrorsToDropBox(DropBoxManager db, HashMap<String, Long> timestamps, String headers, int maxSize, String tag) throws IOException {
        boolean upload_needed = false;
        if (db != null && db.isTagEnabled(tag)) {
            Slog.i(TAG, "Checking for fsck errors");
            File file = new File("/dev/fscklogs/log");
            if (file.lastModified() > 0) {
                String log = FileUtils.readTextFile(file, maxSize, "[[TRUNCATED]]\n");
                StringBuilder sb = new StringBuilder();
                String[] split = log.split("\n");
                int length = split.length;
                for (int i = LOG_SIZE; i < length; i++) {
                    if (split[i].contains("FILE SYSTEM WAS MODIFIED")) {
                        upload_needed = true;
                        break;
                    }
                }
                if (upload_needed) {
                    addFileToDropBox(db, timestamps, headers, "/dev/fscklogs/log", maxSize, tag);
                }
                file.delete();
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static HashMap<String, Long> readTimestamps() {
        HashMap<String, Long> timestamps;
        Throwable th;
        synchronized (sFile) {
            timestamps = new HashMap();
            boolean success = false;
            Throwable th2 = null;
            FileInputStream fileInputStream = null;
            try {
                int type;
                fileInputStream = sFile.openRead();
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(fileInputStream, StandardCharsets.UTF_8.name());
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
                            Long valueOf = Long.valueOf(Long.valueOf(parser.getAttributeValue(null, "timestamp")).longValue());
                            timestamps.put(parser.getAttributeValue(null, "filename"), r17);
                        } else {
                            Slog.w(TAG, "Unknown tag: " + parser.getName());
                            XmlUtils.skipCurrentTag(parser);
                        }
                    }
                }
                success = true;
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (Throwable th3) {
                        th2 = th3;
                    }
                }
                if (th2 != null) {
                    throw th2;
                } else {
                    if (1 == null) {
                        timestamps.clear();
                    }
                }
            } catch (Throwable th22) {
                Throwable th4 = th22;
                th22 = th;
                th = th4;
            }
        }
        return timestamps;
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
            }
        }
    }
}
