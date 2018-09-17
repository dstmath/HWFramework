package android.os;

import android.Manifest.permission;
import android.app.backup.FullBackup;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.IRecoverySystemProgressListener.Stub;
import android.rms.iaware.AwareConstant.Database.HwUserData;
import android.security.keymaster.KeymasterDefs;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.logging.MetricsLogger;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import sun.security.pkcs.PKCS7;
import sun.security.pkcs.SignerInfo;

public class RecoverySystem {
    public static final File BLOCK_MAP_FILE = null;
    private static String COMMAND_FILE_STRING = null;
    private static final File DEFAULT_KEYSTORE = null;
    private static final File LAST_INSTALL_FILE = null;
    private static final String LAST_PREFIX = "last_";
    private static final File LOG_FILE = null;
    private static final int LOG_FILE_MAX_LENGTH = 65536;
    private static final long PUBLISH_PROGRESS_INTERVAL_MS = 500;
    private static final File RECOVERY_DIR = null;
    private static final String TAG = "RecoverySystem";
    public static final File UNCRYPT_PACKAGE_FILE = null;
    private static final Object sRequestLock = null;
    private final IRecoverySystem mService;

    /* renamed from: android.os.RecoverySystem.1 */
    static class AnonymousClass1 extends InputStream {
        int lastPercent;
        long lastPublishTime;
        long soFar;
        long toRead;
        final /* synthetic */ int val$commentSize;
        final /* synthetic */ long val$fileLen;
        final /* synthetic */ ProgressListener val$listenerForInner;
        final /* synthetic */ RandomAccessFile val$raf;
        final /* synthetic */ long val$startTimeMillis;

        AnonymousClass1(long val$fileLen, int val$commentSize, long val$startTimeMillis, RandomAccessFile val$raf, ProgressListener val$listenerForInner) {
            this.val$fileLen = val$fileLen;
            this.val$commentSize = val$commentSize;
            this.val$startTimeMillis = val$startTimeMillis;
            this.val$raf = val$raf;
            this.val$listenerForInner = val$listenerForInner;
            this.toRead = (this.val$fileLen - ((long) this.val$commentSize)) - 2;
            this.soFar = 0;
            this.lastPercent = 0;
            this.lastPublishTime = this.val$startTimeMillis;
        }

        public int read() throws IOException {
            throw new UnsupportedOperationException();
        }

        public int read(byte[] b, int off, int len) throws IOException {
            if (this.soFar >= this.toRead || Thread.currentThread().isInterrupted()) {
                return -1;
            }
            int size = len;
            if (this.soFar + ((long) len) > this.toRead) {
                size = (int) (this.toRead - this.soFar);
            }
            int read = this.val$raf.read(b, off, size);
            this.soFar += (long) read;
            if (this.val$listenerForInner != null) {
                long now = System.currentTimeMillis();
                int p = (int) ((this.soFar * 100) / this.toRead);
                if (p > this.lastPercent && now - this.lastPublishTime > RecoverySystem.PUBLISH_PROGRESS_INTERVAL_MS) {
                    this.lastPercent = p;
                    this.lastPublishTime = now;
                    this.val$listenerForInner.onProgress(this.lastPercent);
                }
            }
            return read;
        }
    }

    /* renamed from: android.os.RecoverySystem.2 */
    static class AnonymousClass2 extends Stub {
        int lastProgress;
        long lastPublishTime;
        final /* synthetic */ ProgressListener val$listener;
        final /* synthetic */ Handler val$progressHandler;

        /* renamed from: android.os.RecoverySystem.2.1 */
        class AnonymousClass1 implements Runnable {
            final /* synthetic */ ProgressListener val$listener;
            final /* synthetic */ long val$now;
            final /* synthetic */ int val$progress;

            AnonymousClass1(int val$progress, long val$now, ProgressListener val$listener) {
                this.val$progress = val$progress;
                this.val$now = val$now;
                this.val$listener = val$listener;
            }

            public void run() {
                if (this.val$progress > AnonymousClass2.this.lastProgress && this.val$now - AnonymousClass2.this.lastPublishTime > RecoverySystem.PUBLISH_PROGRESS_INTERVAL_MS) {
                    AnonymousClass2.this.lastProgress = this.val$progress;
                    AnonymousClass2.this.lastPublishTime = this.val$now;
                    this.val$listener.onProgress(this.val$progress);
                }
            }
        }

        AnonymousClass2(Handler val$progressHandler, ProgressListener val$listener) {
            this.val$progressHandler = val$progressHandler;
            this.val$listener = val$listener;
            this.lastProgress = 0;
            this.lastPublishTime = System.currentTimeMillis();
        }

        public void onProgress(int progress) {
            int i = progress;
            this.val$progressHandler.post(new AnonymousClass1(i, System.currentTimeMillis(), this.val$listener));
        }
    }

    /* renamed from: android.os.RecoverySystem.3 */
    static class AnonymousClass3 extends BroadcastReceiver {
        final /* synthetic */ ConditionVariable val$condition;

        AnonymousClass3(ConditionVariable val$condition) {
            this.val$condition = val$condition;
        }

        public void onReceive(Context context, Intent intent) {
            this.val$condition.open();
        }
    }

    public interface ProgressListener {
        void onProgress(int i);
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.os.RecoverySystem.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.os.RecoverySystem.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.os.RecoverySystem.<clinit>():void");
    }

    private static HashSet<X509Certificate> getTrustedCerts(File keystore) throws IOException, GeneralSecurityException {
        InputStream is;
        HashSet<X509Certificate> trusted = new HashSet();
        if (keystore == null) {
            keystore = DEFAULT_KEYSTORE;
        }
        ZipFile zip = new ZipFile(keystore);
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                is = zip.getInputStream((ZipEntry) entries.nextElement());
                trusted.add((X509Certificate) cf.generateCertificate(is));
                is.close();
            }
            zip.close();
            return trusted;
        } catch (Throwable th) {
            zip.close();
        }
    }

    public static void verifyPackage(File packageFile, ProgressListener listener, File deviceCertsZipFile) throws IOException, GeneralSecurityException {
        long fileLen = packageFile.length();
        RandomAccessFile raf = new RandomAccessFile(packageFile, FullBackup.ROOT_TREE_TOKEN);
        try {
            long startTimeMillis = System.currentTimeMillis();
            if (listener != null) {
                listener.onProgress(0);
            }
            raf.seek(fileLen - 6);
            byte[] footer = new byte[6];
            raf.readFully(footer);
            if (footer[2] == -1 && footer[3] == -1) {
                int commentSize = (footer[4] & Process.PROC_TERM_MASK) | ((footer[5] & Process.PROC_TERM_MASK) << 8);
                int signatureStart = (footer[0] & Process.PROC_TERM_MASK) | ((footer[1] & Process.PROC_TERM_MASK) << 8);
                byte[] eocd = new byte[(commentSize + 22)];
                raf.seek(fileLen - ((long) (commentSize + 22)));
                raf.readFully(eocd);
                if (eocd[0] == 80 && eocd[1] == 75 && eocd[2] == 5 && eocd[3] == 6) {
                    int i = 4;
                    while (i < eocd.length - 3) {
                        if (eocd[i] == 80 && eocd[i + 1] == 75 && eocd[i + 2] == 5 && eocd[i + 3] == 6) {
                            throw new SignatureException("EOCD marker found after start of EOCD");
                        }
                        i++;
                    }
                    PKCS7 block = new PKCS7(new ByteArrayInputStream(eocd, (commentSize + 22) - signatureStart, signatureStart));
                    X509Certificate[] certificates = block.getCertificates();
                    if (certificates == null || certificates.length == 0) {
                        throw new SignatureException("signature contains no certificates");
                    }
                    PublicKey signatureKey = certificates[0].getPublicKey();
                    SignerInfo[] signerInfos = block.getSignerInfos();
                    if (signerInfos == null || signerInfos.length == 0) {
                        throw new SignatureException("signature contains no signedData");
                    }
                    SignerInfo signerInfo = signerInfos[0];
                    boolean verified = false;
                    if (deviceCertsZipFile == null) {
                        deviceCertsZipFile = DEFAULT_KEYSTORE;
                    }
                    for (X509Certificate c : getTrustedCerts(deviceCertsZipFile)) {
                        if (c.getPublicKey().equals(signatureKey)) {
                            verified = true;
                            break;
                        }
                    }
                    if (verified) {
                        raf.seek(0);
                        ProgressListener listenerForInner = listener;
                        SignerInfo verifyResult = block.verify(signerInfo, new AnonymousClass1(fileLen, commentSize, startTimeMillis, raf, listener));
                        boolean interrupted = Thread.interrupted();
                        if (listener != null) {
                            listener.onProgress(100);
                        }
                        if (interrupted) {
                            throw new SignatureException("verification was interrupted");
                        } else if (verifyResult == null) {
                            throw new SignatureException("signature digest verification failed");
                        } else {
                            return;
                        }
                    }
                    throw new SignatureException("signature doesn't match any trusted key");
                }
                throw new SignatureException("no signature in file (bad footer)");
            }
            throw new SignatureException("no signature in file (no footer)");
        } finally {
            raf.close();
        }
    }

    public static void processPackage(Context context, File packageFile, ProgressListener listener, Handler handler) throws IOException {
        String filename = packageFile.getCanonicalPath();
        if (filename.startsWith("/data/")) {
            RecoverySystem rs = (RecoverySystem) context.getSystemService(PowerManager.REBOOT_RECOVERY);
            IRecoverySystemProgressListener iRecoverySystemProgressListener = null;
            if (listener != null) {
                Handler progressHandler;
                if (handler != null) {
                    progressHandler = handler;
                } else {
                    progressHandler = new Handler(context.getMainLooper());
                }
                iRecoverySystemProgressListener = new AnonymousClass2(progressHandler, listener);
            }
            if (!rs.uncrypt(filename, iRecoverySystemProgressListener)) {
                throw new IOException("process package failed");
            }
        }
    }

    public static void processPackage(Context context, File packageFile, ProgressListener listener) throws IOException {
        processPackage(context, packageFile, listener, null);
    }

    public static void installPackage(Context context, File packageFile) throws IOException {
        installPackage(context, packageFile, false);
    }

    public static void installPackage(Context context, File packageFile, boolean processed) throws IOException {
        synchronized (sRequestLock) {
        }
        LOG_FILE.delete();
        UNCRYPT_PACKAGE_FILE.delete();
        String filename = packageFile.getCanonicalPath();
        Log.w(TAG, "!!! REBOOTING TO INSTALL " + filename + " !!!");
        boolean securityUpdate = filename.endsWith("_s.zip");
        if (filename.startsWith("/data/")) {
            if (!processed) {
                FileWriter uncryptFile = new FileWriter(UNCRYPT_PACKAGE_FILE);
                try {
                    uncryptFile.write(filename + "\n");
                    if (!(UNCRYPT_PACKAGE_FILE.setReadable(true, false) && UNCRYPT_PACKAGE_FILE.setWritable(true, false))) {
                        Log.e(TAG, "Error setting permission for " + UNCRYPT_PACKAGE_FILE);
                    }
                    BLOCK_MAP_FILE.delete();
                } finally {
                    uncryptFile.close();
                }
            } else if (!BLOCK_MAP_FILE.exists()) {
                Log.e(TAG, "Package claimed to have been processed but failed to find the block map file.");
                throw new IOException("Failed to find block map file");
            }
            filename = "@/cache/recovery/block.map";
        }
        String filenameArg = "--update_package=" + filename + "\n";
        String securityArg = "--security\n";
        String command = filenameArg + ("--locale=" + Locale.getDefault().toString() + "\n");
        if (securityUpdate) {
            command = command + "--security\n";
        }
        if (((RecoverySystem) context.getSystemService(PowerManager.REBOOT_RECOVERY)).setupBcb(command)) {
            ((PowerManager) context.getSystemService(Context.POWER_SERVICE)).reboot(PowerManager.REBOOT_RECOVERY_UPDATE);
            throw new IOException("Reboot failed (no permissions?)");
        }
        throw new IOException("Setup BCB failed");
    }

    public static void scheduleUpdateOnBoot(Context context, File packageFile) throws IOException {
        String filename = packageFile.getCanonicalPath();
        boolean securityUpdate = filename.endsWith("_s.zip");
        if (filename.startsWith("/data/")) {
            filename = "@/cache/recovery/block.map";
        }
        String filenameArg = "--update_package=" + filename + "\n";
        String securityArg = "--security\n";
        String command = filenameArg + ("--locale=" + Locale.getDefault().toString() + "\n");
        if (securityUpdate) {
            command = command + "--security\n";
        }
        if (!((RecoverySystem) context.getSystemService(PowerManager.REBOOT_RECOVERY)).setupBcb(command)) {
            throw new IOException("schedule update on boot failed");
        }
    }

    public static void cancelScheduledUpdate(Context context) throws IOException {
        if (!((RecoverySystem) context.getSystemService(PowerManager.REBOOT_RECOVERY)).clearBcb()) {
            throw new IOException("cancel scheduled update failed");
        }
    }

    public static void rebootWipeUserData(Context context) throws IOException {
        rebootWipeUserData(context, false, context.getPackageName());
    }

    public static void rebootWipeUserData(Context context, String reason) throws IOException {
        rebootWipeUserData(context, false, reason);
    }

    public static void rebootWipeUserData(Context context, boolean shutdown) throws IOException {
        rebootWipeUserData(context, shutdown, context.getPackageName());
    }

    public static void rebootWipeUserData(Context context, boolean shutdown, String reason) throws IOException {
        if (((UserManager) context.getSystemService(Context.USER_SERVICE)).hasUserRestriction(UserManager.DISALLOW_FACTORY_RESET)) {
            throw new SecurityException("Wiping data is not allowed for this user.");
        }
        Intent intent;
        ConditionVariable condition = new ConditionVariable();
        if (SystemProperties.get("persist.sys.cc_mode", WifiEnterpriseConfig.ENGINE_DISABLE).equals(WifiEnterpriseConfig.ENGINE_DISABLE)) {
            intent = new Intent("android.intent.action.MASTER_CLEAR_NOTIFICATION");
        } else {
            intent = new Intent(Intent.ACTION_MASTER_CLEAR);
            intent.putExtra("masterClearWipeDataFactoryLowlevel", true);
        }
        intent.addFlags(KeymasterDefs.KM_ENUM);
        context.sendOrderedBroadcastAsUser(intent, UserHandle.SYSTEM, permission.MASTER_CLEAR, new AnonymousClass3(condition), null, 0, null, null);
        condition.block();
        String shutdownArg = null;
        if (shutdown) {
            shutdownArg = "--shutdown_after";
        }
        String reasonArg = null;
        if (!TextUtils.isEmpty(reason)) {
            reasonArg = "--reason=" + sanitizeArg(reason);
        }
        String localeArg = "--locale=" + Locale.getDefault().toString();
        bootCommand(context, shutdownArg, "--wipe_data", reasonArg, localeArg);
    }

    public static void rebootWipeCache(Context context) throws IOException {
        rebootWipeCache(context, context.getPackageName());
    }

    public static void rebootWipeCache(Context context, String reason) throws IOException {
        String reasonArg = null;
        if (!TextUtils.isEmpty(reason)) {
            reasonArg = "--reason=" + sanitizeArg(reason);
        }
        String localeArg = "--locale=" + Locale.getDefault().toString();
        bootCommand(context, "--wipe_cache", reasonArg, localeArg);
    }

    private static void bootCommand(Context context, String... args) throws IOException {
        synchronized (sRequestLock) {
        }
        LOG_FILE.delete();
        StringBuilder command = new StringBuilder();
        for (String arg : args) {
            if (!TextUtils.isEmpty(arg)) {
                command.append(arg);
                command.append("\n");
            }
        }
        ((RecoverySystem) context.getSystemService(PowerManager.REBOOT_RECOVERY)).setupBcb(command.toString());
        ((PowerManager) context.getSystemService(Context.POWER_SERVICE)).reboot(PowerManager.REBOOT_RECOVERY);
        throw new IOException("Reboot failed (no permissions?)");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void parseLastInstallLog(Context context) {
        Throwable th;
        IOException e;
        Throwable th2 = null;
        BufferedReader bufferedReader = null;
        try {
            BufferedReader in = new BufferedReader(new FileReader(LAST_INSTALL_FILE));
            int bytesWrittenInMiB = -1;
            int bytesStashedInMiB = -1;
            int timeTotal = -1;
            int sourceVersion = -1;
            while (true) {
                try {
                    String line = in.readLine();
                    if (line == null) {
                        break;
                    }
                    int numIndex = line.indexOf(58);
                    if (numIndex != -1 && numIndex + 1 < line.length()) {
                        try {
                            long parsedNum = Long.parseLong(line.substring(numIndex + 1).trim());
                            try {
                                int scaled;
                                if (line.startsWith("bytes")) {
                                    scaled = Math.toIntExact(parsedNum / Trace.TRACE_TAG_DATABASE);
                                } else {
                                    scaled = Math.toIntExact(parsedNum);
                                }
                                if (line.startsWith(HwUserData.TIME)) {
                                    timeTotal = scaled;
                                } else {
                                    if (line.startsWith("source_build")) {
                                        sourceVersion = scaled;
                                    } else {
                                        if (!line.startsWith("bytes_written")) {
                                            if (line.startsWith("bytes_stashed")) {
                                                if (bytesStashedInMiB == -1) {
                                                    bytesStashedInMiB = scaled;
                                                } else {
                                                    bytesStashedInMiB += scaled;
                                                }
                                            }
                                        } else if (bytesWrittenInMiB == -1) {
                                            bytesWrittenInMiB = scaled;
                                        } else {
                                            bytesWrittenInMiB += scaled;
                                        }
                                    }
                                }
                            } catch (ArithmeticException e2) {
                                Log.e(TAG, "Number overflows in " + line);
                            }
                        } catch (NumberFormatException e3) {
                            Log.e(TAG, "Failed to parse numbers in " + line);
                        }
                    }
                } catch (Throwable th3) {
                    th = th3;
                    bufferedReader = in;
                }
            }
            if (timeTotal != -1) {
                MetricsLogger.histogram(context, "ota_time_total", timeTotal);
            }
            if (sourceVersion != -1) {
                MetricsLogger.histogram(context, "ota_source_version", sourceVersion);
            }
            if (bytesWrittenInMiB != -1) {
                MetricsLogger.histogram(context, "ota_written_in_MiBs", bytesWrittenInMiB);
            }
            if (bytesStashedInMiB != -1) {
                MetricsLogger.histogram(context, "ota_stashed_in_MiBs", bytesStashedInMiB);
            }
            if (in != null) {
                try {
                    in.close();
                } catch (Throwable th4) {
                    th2 = th4;
                }
            }
            if (th2 != null) {
                try {
                    throw th2;
                } catch (IOException e4) {
                    e = e4;
                    bufferedReader = in;
                }
            }
        } catch (Throwable th5) {
            th = th5;
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (Throwable th6) {
                    if (th2 == null) {
                        th2 = th6;
                    } else if (th2 != th6) {
                        th2.addSuppressed(th6);
                    }
                }
            }
            if (th2 != null) {
                try {
                    throw th2;
                } catch (IOException e5) {
                    e = e5;
                }
            } else {
                throw th;
            }
        }
    }

    public static String handleAftermath(Context context) {
        String log = null;
        try {
            log = FileUtils.readTextFile(LOG_FILE, Color.RED, "...\n");
        } catch (FileNotFoundException e) {
            Log.i(TAG, "No recovery log file");
        } catch (IOException e2) {
            Log.e(TAG, "Error reading recovery log", e2);
        }
        if (log != null) {
            parseLastInstallLog(context);
        }
        boolean reservePackage = BLOCK_MAP_FILE.exists();
        if (!reservePackage && UNCRYPT_PACKAGE_FILE.exists()) {
            String filename = null;
            try {
                filename = FileUtils.readTextFile(UNCRYPT_PACKAGE_FILE, 0, null);
            } catch (IOException e22) {
                Log.e(TAG, "Error reading uncrypt file", e22);
            }
            if (filename != null && filename.startsWith("/data")) {
                if (UNCRYPT_PACKAGE_FILE.delete()) {
                    Log.i(TAG, "Deleted: " + filename);
                } else {
                    Log.e(TAG, "Can't delete: " + filename);
                }
            }
        }
        String[] names = RECOVERY_DIR.list();
        int i = 0;
        while (names != null && i < names.length) {
            if (!(names[i].startsWith(LAST_PREFIX) || ((reservePackage && names[i].equals(BLOCK_MAP_FILE.getName())) || ((reservePackage && names[i].equals(UNCRYPT_PACKAGE_FILE.getName())) || names[i].trim().equals(COMMAND_FILE_STRING))))) {
                recursiveDelete(new File(RECOVERY_DIR, names[i]));
            }
            i++;
        }
        return log;
    }

    private static void recursiveDelete(File name) {
        if (name.isDirectory()) {
            String[] files = name.list();
            int i = 0;
            while (files != null && i < files.length) {
                recursiveDelete(new File(name, files[i]));
                i++;
            }
        }
        if (name.delete()) {
            Log.i(TAG, "Deleted: " + name);
        } else {
            Log.e(TAG, "Can't delete: " + name);
        }
    }

    private boolean uncrypt(String packageFile, IRecoverySystemProgressListener listener) {
        try {
            return this.mService.uncrypt(packageFile, listener);
        } catch (RemoteException e) {
            return false;
        }
    }

    private boolean setupBcb(String command) {
        try {
            return this.mService.setupBcb(command);
        } catch (RemoteException e) {
            return false;
        }
    }

    private boolean clearBcb() {
        try {
            return this.mService.clearBcb();
        } catch (RemoteException e) {
            return false;
        }
    }

    private static String sanitizeArg(String arg) {
        return arg.replace('\u0000', '?').replace('\n', '?');
    }

    public RecoverySystem() {
        this.mService = null;
    }

    public RecoverySystem(IRecoverySystem service) {
        this.mService = service;
    }

    public static void hwBootCommand(Context context, String arg) throws IOException {
        bootCommand(context, arg);
    }
}
