package android.os;

import android.annotation.SuppressLint;
import android.annotation.SystemApi;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.BatteryStats;
import android.os.IRecoverySystemProgressListener;
import android.provider.Settings;
import android.rms.AppAssociate;
import android.telephony.euicc.EuiccManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.WindowManager;
import com.android.internal.logging.MetricsLogger;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SignatureException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import libcore.io.Streams;

public class RecoverySystem {
    private static final String ACTION_EUICC_FACTORY_RESET = "com.android.internal.action.EUICC_FACTORY_RESET";
    public static final File BLOCK_MAP_FILE = new File(RECOVERY_DIR, "block.map");
    private static String COMMAND_FILE_STRING = "command";
    private static final long DEFAULT_EUICC_FACTORY_RESET_TIMEOUT_MILLIS = 30000;
    private static final File DEFAULT_KEYSTORE = new File("/system/etc/security/otacerts.zip");
    private static final File LAST_INSTALL_FILE = new File(RECOVERY_DIR, "last_install");
    private static final String LAST_PREFIX = "last_";
    private static final File LOG_FILE = new File(RECOVERY_DIR, "log");
    private static final int LOG_FILE_MAX_LENGTH = 65536;
    private static final long MAX_EUICC_FACTORY_RESET_TIMEOUT_MILLIS = 60000;
    private static final long MIN_EUICC_FACTORY_RESET_TIMEOUT_MILLIS = 5000;
    private static final String PACKAGE_NAME_WIPING_EUICC_DATA_CALLBACK = "android";
    private static final long PUBLISH_PROGRESS_INTERVAL_MS = 500;
    private static final File RECOVERY_DIR = new File("/cache/recovery");
    private static final String TAG = "RecoverySystem";
    public static final File UNCRYPT_PACKAGE_FILE = new File(RECOVERY_DIR, "uncrypt_file");
    public static final File UNCRYPT_STATUS_FILE = new File(RECOVERY_DIR, "uncrypt_status");
    private static final Object sRequestLock = new Object();
    private final IRecoverySystem mService;

    public interface ProgressListener {
        void onProgress(int i);
    }

    private static HashSet<X509Certificate> getTrustedCerts(File keystore) throws IOException, GeneralSecurityException {
        InputStream is;
        HashSet<X509Certificate> trusted = new HashSet<>();
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
            throw th;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00a1, code lost:
        r9 = new sun.security.pkcs.PKCS7(new java.io.ByteArrayInputStream(r4, (r15 + 22) - r7, r7));
        r2 = r9.getCertificates();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x00b4, code lost:
        if (r2 == null) goto L_0x01a4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x00b7, code lost:
        if (r2.length == 0) goto L_0x01a4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00b9, code lost:
        r3 = r2[0];
        r1 = r3.getPublicKey();
        r8 = r9.getSignerInfos();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00c4, code lost:
        if (r8 == null) goto L_0x018b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x00c7, code lost:
        if (r8.length == 0) goto L_0x018b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x00c9, code lost:
        r0 = r8[0];
        r16 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x00ce, code lost:
        if (r33 != null) goto L_0x00d7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x00d0, code lost:
        r24 = r2;
        r2 = DEFAULT_KEYSTORE;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x00d7, code lost:
        r24 = r2;
        r2 = r33;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x00db, code lost:
        r2 = getTrustedCerts(r2);
        r25 = r3;
        r3 = r2.iterator();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00e9, code lost:
        if (r3.hasNext() == false) goto L_0x010c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x00eb, code lost:
        r27 = r2;
        r28 = r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x0101, code lost:
        if (r3.next().getPublicKey().equals(r1) == false) goto L_0x0106;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x0103, code lost:
        r16 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x0106, code lost:
        r2 = r27;
        r3 = r28;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x010c, code lost:
        r27 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x010e, code lost:
        if (r16 == false) goto L_0x0170;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x0110, code lost:
        r13.seek(0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x0115, code lost:
        r18 = r8;
        r2 = r32;
        r8 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x011c, code lost:
        r19 = r1;
        r1 = r1;
        r12 = r1;
        r29 = r14;
        r20 = r24;
        r21 = r25;
        r22 = r27;
        r14 = r2;
        r2 = r10;
        r23 = r4;
        r4 = r15;
        r24 = r7;
        r7 = r13;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:?, code lost:
        r1 = new android.os.RecoverySystem.AnonymousClass1();
        r1 = r9.verify(r0, r12);
        r2 = java.lang.Thread.interrupted();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x013b, code lost:
        if (r14 == null) goto L_0x0142;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:0x013d, code lost:
        r14.onProgress(100);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x0142, code lost:
        if (r2 != false) goto L_0x0163;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x0144, code lost:
        if (r1 == null) goto L_0x015a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x0146, code lost:
        r13.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:69:0x014e, code lost:
        if (readAndVerifyPackageCompatibilityEntry(r31) == false) goto L_0x0151;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:70:0x0150, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:72:0x0159, code lost:
        throw new java.security.SignatureException("package compatibility verification failed");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:75:0x0162, code lost:
        throw new java.security.SignatureException("signature digest verification failed");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:77:0x016b, code lost:
        throw new java.security.SignatureException("verification was interrupted");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:78:0x016c, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:79:0x016d, code lost:
        r14 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:80:0x0170, code lost:
        r19 = r1;
        r23 = r4;
        r18 = r8;
        r29 = r14;
        r20 = r24;
        r21 = r25;
        r22 = r27;
        r14 = r32;
        r24 = r7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:81:0x018a, code lost:
        throw new java.security.SignatureException("signature doesn't match any trusted key");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:82:0x018b, code lost:
        r19 = r1;
        r20 = r2;
        r21 = r3;
        r23 = r4;
        r24 = r7;
        r18 = r8;
        r29 = r14;
        r14 = r32;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:83:0x01a3, code lost:
        throw new java.security.SignatureException("signature contains no signedData");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:84:0x01a4, code lost:
        r20 = r2;
        r23 = r4;
        r24 = r7;
        r29 = r14;
        r14 = r32;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:85:0x01b6, code lost:
        throw new java.security.SignatureException("signature contains no certificates");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:92:0x01d8, code lost:
        r0 = th;
     */
    public static void verifyPackage(File packageFile, ProgressListener listener, File deviceCertsZipFile) throws IOException, GeneralSecurityException {
        ProgressListener progressListener = listener;
        long fileLen = packageFile.length();
        RandomAccessFile raf = new RandomAccessFile(packageFile, "r");
        try {
            final long startTimeMillis = System.currentTimeMillis();
            if (progressListener != null) {
                progressListener.onProgress(0);
            }
            raf.seek(fileLen - 6);
            byte[] footer = new byte[6];
            raf.readFully(footer);
            if (footer[2] == -1) {
                int i = 3;
                if (footer[3] == -1) {
                    int commentSize = ((footer[5] & BatteryStats.HistoryItem.CMD_NULL) << 8) | (footer[4] & 255);
                    byte b = (footer[0] & BatteryStats.HistoryItem.CMD_NULL) | ((footer[1] & BatteryStats.HistoryItem.CMD_NULL) << 8);
                    byte[] eocd = new byte[(commentSize + 22)];
                    try {
                        raf.seek(fileLen - ((long) (commentSize + 22)));
                        raf.readFully(eocd);
                        if (eocd[0] == 80 && eocd[1] == 75 && eocd[2] == 5 && eocd[3] == 6) {
                            int i2 = 4;
                            while (true) {
                                int i3 = i2;
                                if (i3 >= eocd.length - i) {
                                    break;
                                }
                                if (eocd[i3] == 80 && eocd[i3 + 1] == 75 && eocd[i3 + 2] == 5) {
                                    if (eocd[i3 + 3] == 6) {
                                        throw new SignatureException("EOCD marker found after start of EOCD");
                                    }
                                }
                                i2 = i3 + 1;
                                i = 3;
                            }
                        } else {
                            byte b2 = b;
                            byte[] bArr = footer;
                            ProgressListener progressListener2 = listener;
                            throw new SignatureException("no signature in file (bad footer)");
                        }
                    } catch (Throwable th) {
                        signerInfo = th;
                        ProgressListener progressListener3 = listener;
                        raf.close();
                        throw signerInfo;
                    }
                }
            }
            byte[] bArr2 = footer;
            ProgressListener progressListener4 = progressListener;
            throw new SignatureException("no signature in file (no footer)");
        } catch (Throwable th2) {
            signerInfo = th2;
            ProgressListener progressListener5 = progressListener;
            raf.close();
            throw signerInfo;
        }
    }

    private static boolean verifyPackageCompatibility(InputStream inputStream) throws IOException {
        long entrySize;
        ArrayList<String> list = new ArrayList<>();
        ZipInputStream zis = new ZipInputStream(inputStream);
        while (true) {
            ZipEntry nextEntry = zis.getNextEntry();
            ZipEntry entry = nextEntry;
            if (nextEntry != null) {
                entrySize = entry.getSize();
                if (entrySize > 2147483647L || entrySize < 0) {
                } else {
                    byte[] bytes = new byte[((int) entrySize)];
                    Streams.readFully(zis, bytes);
                    list.add(new String(bytes, StandardCharsets.UTF_8));
                }
            } else if (!list.isEmpty()) {
                return VintfObject.verify((String[]) list.toArray(new String[list.size()])) == 0;
            } else {
                throw new IOException("no entries found in the compatibility file");
            }
        }
        throw new IOException("invalid entry size (" + entrySize + ") in the compatibility file");
    }

    private static boolean readAndVerifyPackageCompatibilityEntry(File packageFile) throws IOException {
        InputStream inputStream;
        ZipFile zip = new ZipFile(packageFile);
        try {
            ZipEntry entry = zip.getEntry("compatibility.zip");
            if (entry == null) {
                $closeResource(null, zip);
                return true;
            }
            inputStream = zip.getInputStream(entry);
            Boolean bl = Boolean.valueOf(verifyPackageCompatibility(inputStream));
            inputStream.close();
            boolean booleanValue = bl.booleanValue();
            $closeResource(null, zip);
            return booleanValue;
        } catch (IOException e) {
            Log.e(TAG, "Exception happend when excute verifyPackageCompatibility");
            throw e;
        } catch (IOException e2) {
            Log.e(TAG, "InputStream close failure");
        } catch (Throwable th) {
            Throwable th2 = th;
            try {
                throw th2;
            } catch (Throwable th3) {
                $closeResource(th2, zip);
                throw th3;
            }
        }
    }

    private static /* synthetic */ void $closeResource(Throwable x0, AutoCloseable x1) {
        if (x0 != null) {
            try {
                x1.close();
            } catch (Throwable th) {
                x0.addSuppressed(th);
            }
        } else {
            x1.close();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0015, code lost:
        throw r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:5:0x000e, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0012, code lost:
        $closeResource(r1, r0);
     */
    @SuppressLint({"Doclava125"})
    @SystemApi
    public static boolean verifyPackageCompatibility(File compatibilityFile) throws IOException {
        InputStream inputStream = new FileInputStream(compatibilityFile);
        boolean verifyPackageCompatibility = verifyPackageCompatibility(inputStream);
        $closeResource(null, inputStream);
        return verifyPackageCompatibility;
    }

    @SystemApi
    public static void processPackage(Context context, File packageFile, final ProgressListener listener, Handler handler) throws IOException {
        final Handler progressHandler;
        String filename = packageFile.getCanonicalPath();
        if (filename.startsWith("/data/")) {
            RecoverySystem rs = (RecoverySystem) context.getSystemService(PowerManager.REBOOT_RECOVERY);
            IRecoverySystemProgressListener progressListener = null;
            if (listener != null) {
                if (handler != null) {
                    progressHandler = handler;
                } else {
                    progressHandler = new Handler(context.getMainLooper());
                }
                progressListener = new IRecoverySystemProgressListener.Stub() {
                    int lastProgress = 0;
                    long lastPublishTime = System.currentTimeMillis();

                    public void onProgress(final int progress) {
                        final long now = System.currentTimeMillis();
                        Handler.this.post(new Runnable() {
                            public void run() {
                                if (progress > AnonymousClass2.this.lastProgress && now - AnonymousClass2.this.lastPublishTime > 500) {
                                    AnonymousClass2.this.lastProgress = progress;
                                    AnonymousClass2.this.lastPublishTime = now;
                                    listener.onProgress(progress);
                                }
                            }
                        });
                    }
                };
            }
            if (!rs.uncrypt(filename, progressListener)) {
                throw new IOException("process package failed");
            }
        }
    }

    @SystemApi
    public static void processPackage(Context context, File packageFile, ProgressListener listener) throws IOException {
        processPackage(context, packageFile, listener, null);
    }

    public static void installPackage(Context context, File packageFile) throws IOException {
        installPackage(context, packageFile, false);
    }

    /* JADX INFO: finally extract failed */
    @SystemApi
    public static void installPackage(Context context, File packageFile, boolean processed) throws IOException {
        synchronized (sRequestLock) {
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
                        uncryptFile.close();
                        if (!UNCRYPT_PACKAGE_FILE.setReadable(true, false) || !UNCRYPT_PACKAGE_FILE.setWritable(true, false)) {
                            Log.e(TAG, "Error setting permission for " + UNCRYPT_PACKAGE_FILE);
                        }
                        BLOCK_MAP_FILE.delete();
                    } catch (Throwable th) {
                        uncryptFile.close();
                        throw th;
                    }
                } else if (!BLOCK_MAP_FILE.exists()) {
                    Log.e(TAG, "Package claimed to have been processed but failed to find the block map file.");
                    throw new IOException("Failed to find block map file");
                }
                filename = "@/cache/recovery/block.map";
            }
            String filenameArg = "--update_package=" + filename + "\n";
            String localeArg = "--locale=" + Locale.getDefault().toLanguageTag() + "\n";
            String command = filenameArg + localeArg;
            if (securityUpdate) {
                command = command + "--security\n";
            }
            if (((RecoverySystem) context.getSystemService(PowerManager.REBOOT_RECOVERY)).setupBcb(command)) {
                PowerManager pm = (PowerManager) context.getSystemService("power");
                String reason = PowerManager.REBOOT_RECOVERY_UPDATE;
                if (context.getPackageManager().hasSystemFeature("android.software.leanback") && ((WindowManager) context.getSystemService(AppAssociate.ASSOC_WINDOW)).getDefaultDisplay().getState() != 2) {
                    reason = reason + ",quiescent";
                }
                pm.reboot(reason);
                throw new IOException("Reboot failed (no permissions?)");
            }
            throw new IOException("Setup BCB failed");
        }
    }

    @SystemApi
    public static void scheduleUpdateOnBoot(Context context, File packageFile) throws IOException {
        String filename = packageFile.getCanonicalPath();
        boolean securityUpdate = filename.endsWith("_s.zip");
        if (filename.startsWith("/data/")) {
            filename = "@/cache/recovery/block.map";
        }
        String filenameArg = "--update_package=" + filename + "\n";
        String localeArg = "--locale=" + Locale.getDefault().toLanguageTag() + "\n";
        String command = filenameArg + localeArg;
        if (securityUpdate) {
            command = command + "--security\n";
        }
        if (!((RecoverySystem) context.getSystemService(PowerManager.REBOOT_RECOVERY)).setupBcb(command)) {
            throw new IOException("schedule update on boot failed");
        }
    }

    @SystemApi
    public static void cancelScheduledUpdate(Context context) throws IOException {
        if (!((RecoverySystem) context.getSystemService(PowerManager.REBOOT_RECOVERY)).clearBcb()) {
            throw new IOException("cancel scheduled update failed");
        }
    }

    public static void rebootWipeUserData(Context context) throws IOException {
        rebootWipeUserData(context, false, context.getPackageName(), false, false);
    }

    public static void rebootWipeUserData(Context context, String reason) throws IOException {
        rebootWipeUserData(context, false, reason, false, false);
    }

    public static void rebootWipeUserData(Context context, boolean shutdown) throws IOException {
        rebootWipeUserData(context, shutdown, context.getPackageName(), false, false);
    }

    public static void rebootWipeUserData(Context context, boolean shutdown, String reason, boolean force) throws IOException {
        rebootWipeUserData(context, shutdown, reason, force, false);
    }

    public static void rebootWipeUserData(Context context, boolean shutdown, String reason, boolean force, boolean wipeEuicc) throws IOException {
        Intent intent;
        Context context2 = context;
        UserManager um = (UserManager) context2.getSystemService("user");
        if (force || !um.hasUserRestriction(UserManager.DISALLOW_FACTORY_RESET)) {
            final ConditionVariable condition = new ConditionVariable();
            if (SystemProperties.get("persist.sys.cc_mode", WifiEnterpriseConfig.ENGINE_DISABLE).equals(WifiEnterpriseConfig.ENGINE_DISABLE)) {
                intent = new Intent("android.intent.action.MASTER_CLEAR_NOTIFICATION");
            } else {
                intent = new Intent("android.intent.action.MASTER_CLEAR");
                intent.putExtra("masterClearWipeDataFactoryLowlevel", true);
            }
            Intent intent2 = intent;
            intent2.addFlags(285212672);
            context2.sendOrderedBroadcastAsUser(intent2, UserHandle.SYSTEM, "android.permission.MASTER_CLEAR", new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    ConditionVariable.this.open();
                }
            }, null, 0, null, null);
            condition.block();
            if (wipeEuicc) {
                wipeEuiccData(context2, "android");
            }
            String shutdownArg = null;
            if (shutdown) {
                shutdownArg = "--shutdown_after";
            }
            String reasonArg = null;
            if (!TextUtils.isEmpty(reason)) {
                reasonArg = "--reason=" + sanitizeArg(reason);
            }
            bootCommand(context2, shutdownArg, "--wipe_data", reasonArg, "--locale=" + Locale.getDefault().toLanguageTag());
            return;
        }
        throw new SecurityException("Wiping data is not allowed for this user.");
    }

    public static boolean wipeEuiccData(Context context, String packageName) {
        long waitingTimeMillis;
        Context context2 = context;
        if (Settings.Global.getInt(context.getContentResolver(), Settings.Global.EUICC_PROVISIONED, 0) == 0) {
            Log.d(TAG, "Skipping eUICC wipe/retain as it is not provisioned");
            return true;
        }
        EuiccManager euiccManager = (EuiccManager) context2.getSystemService("euicc");
        if (euiccManager == null || !euiccManager.isEnabled()) {
            String str = packageName;
            return false;
        }
        final CountDownLatch euiccFactoryResetLatch = new CountDownLatch(1);
        final AtomicBoolean wipingSucceeded = new AtomicBoolean(false);
        BroadcastReceiver euiccWipeFinishReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (RecoverySystem.ACTION_EUICC_FACTORY_RESET.equals(intent.getAction())) {
                    if (getResultCode() != 0) {
                        int detailedCode = intent.getIntExtra(EuiccManager.EXTRA_EMBEDDED_SUBSCRIPTION_DETAILED_CODE, 0);
                        Log.e(RecoverySystem.TAG, "Error wiping euicc data, Detailed code = " + detailedCode);
                    } else {
                        Log.d(RecoverySystem.TAG, "Successfully wiped euicc data.");
                        wipingSucceeded.set(true);
                    }
                    euiccFactoryResetLatch.countDown();
                }
            }
        };
        Intent intent = new Intent(ACTION_EUICC_FACTORY_RESET);
        intent.setPackage(packageName);
        PendingIntent callbackIntent = PendingIntent.getBroadcastAsUser(context2, 0, intent, 134217728, UserHandle.SYSTEM);
        IntentFilter filterConsent = new IntentFilter();
        filterConsent.addAction(ACTION_EUICC_FACTORY_RESET);
        HandlerThread euiccHandlerThread = new HandlerThread("euiccWipeFinishReceiverThread");
        euiccHandlerThread.start();
        context.getApplicationContext().registerReceiver(euiccWipeFinishReceiver, filterConsent, null, new Handler(euiccHandlerThread.getLooper()));
        euiccManager.eraseSubscriptions(callbackIntent);
        try {
            CountDownLatch euiccFactoryResetLatch2 = euiccFactoryResetLatch;
            try {
                waitingTimeMillis = Settings.Global.getLong(context.getContentResolver(), Settings.Global.EUICC_FACTORY_RESET_TIMEOUT_MILLIS, DEFAULT_EUICC_FACTORY_RESET_TIMEOUT_MILLIS);
                if (waitingTimeMillis < 5000) {
                    waitingTimeMillis = 5000;
                } else if (waitingTimeMillis > 60000) {
                    waitingTimeMillis = 60000;
                }
            } catch (InterruptedException e) {
                e = e;
                CountDownLatch countDownLatch = euiccFactoryResetLatch2;
                try {
                    Thread.currentThread().interrupt();
                    Log.e(TAG, "Wiping eUICC data interrupted", e);
                    context.getApplicationContext().unregisterReceiver(euiccWipeFinishReceiver);
                    return false;
                } catch (Throwable th) {
                    e = th;
                    context.getApplicationContext().unregisterReceiver(euiccWipeFinishReceiver);
                    throw e;
                }
            } catch (Throwable th2) {
                e = th2;
                CountDownLatch countDownLatch2 = euiccFactoryResetLatch2;
                context.getApplicationContext().unregisterReceiver(euiccWipeFinishReceiver);
                throw e;
            }
            try {
                if (!euiccFactoryResetLatch2.await(waitingTimeMillis, TimeUnit.MILLISECONDS)) {
                    Log.e(TAG, "Timeout wiping eUICC data.");
                    context.getApplicationContext().unregisterReceiver(euiccWipeFinishReceiver);
                    return false;
                }
                context.getApplicationContext().unregisterReceiver(euiccWipeFinishReceiver);
                return wipingSucceeded.get();
            } catch (InterruptedException e2) {
                e = e2;
                Thread.currentThread().interrupt();
                Log.e(TAG, "Wiping eUICC data interrupted", e);
                context.getApplicationContext().unregisterReceiver(euiccWipeFinishReceiver);
                return false;
            }
        } catch (InterruptedException e3) {
            e = e3;
            CountDownLatch countDownLatch3 = euiccFactoryResetLatch;
            Thread.currentThread().interrupt();
            Log.e(TAG, "Wiping eUICC data interrupted", e);
            context.getApplicationContext().unregisterReceiver(euiccWipeFinishReceiver);
            return false;
        } catch (Throwable th3) {
            e = th3;
            CountDownLatch countDownLatch4 = euiccFactoryResetLatch;
            context.getApplicationContext().unregisterReceiver(euiccWipeFinishReceiver);
            throw e;
        }
    }

    public static void rebootPromptAndWipeUserData(Context context, String reason) throws IOException {
        String reasonArg = null;
        if (!TextUtils.isEmpty(reason)) {
            reasonArg = "--reason=" + sanitizeArg(reason);
        }
        bootCommand(context, null, "--prompt_and_wipe_data", reasonArg, "--locale=" + Locale.getDefault().toString());
    }

    public static void rebootWipeCache(Context context) throws IOException {
        rebootWipeCache(context, context.getPackageName());
    }

    public static void rebootWipeCache(Context context, String reason) throws IOException {
        String reasonArg = null;
        if (!TextUtils.isEmpty(reason)) {
            reasonArg = "--reason=" + sanitizeArg(reason);
        }
        bootCommand(context, "--wipe_cache", reasonArg, "--locale=" + Locale.getDefault().toLanguageTag());
    }

    @SystemApi
    public static void rebootWipeAb(Context context, File packageFile, String reason) throws IOException {
        String reasonArg = null;
        if (!TextUtils.isEmpty(reason)) {
            reasonArg = "--reason=" + sanitizeArg(reason);
        }
        String filename = packageFile.getCanonicalPath();
        bootCommand(context, "--wipe_ab", "--wipe_package=" + filename, reasonArg, "--locale=" + Locale.getDefault().toLanguageTag());
    }

    private static void bootCommand(Context context, String... args) throws IOException {
        LOG_FILE.delete();
        StringBuilder command = new StringBuilder();
        for (String arg : args) {
            if (!TextUtils.isEmpty(arg)) {
                command.append(arg);
                command.append("\n");
            }
        }
        ((RecoverySystem) context.getSystemService(PowerManager.REBOOT_RECOVERY)).rebootRecoveryWithCommand(command.toString());
        throw new IOException("Reboot failed (no permissions?)");
    }

    private static void parseLastInstallLog(Context context) {
        int causeCode;
        Throwable th;
        BufferedReader in;
        int errorCode;
        Context context2 = context;
        try {
            BufferedReader in2 = new BufferedReader(new FileReader(LAST_INSTALL_FILE));
            int timeTotal = -1;
            int uncryptTime = -1;
            int i = -1;
            int errorCode2 = -1;
            int temperatureMax = -1;
            int temperatureEnd = -1;
            int temperatureStart = -1;
            int sourceVersion = -1;
            int bytesStashedInMiB = -1;
            int bytesWrittenInMiB = -1;
            int scaled = -1;
            while (true) {
                causeCode = scaled;
                try {
                    String readLine = in2.readLine();
                    String line = readLine;
                    if (readLine == null) {
                        break;
                    }
                    try {
                        int numIndex = line.indexOf(58);
                        if (numIndex == i) {
                            in = in2;
                            errorCode = errorCode2;
                        } else if (numIndex + 1 >= line.length()) {
                            in = in2;
                            errorCode = errorCode2;
                        } else {
                            String numString = line.substring(numIndex + 1).trim();
                            try {
                                long parsedNum = Long.parseLong(numString);
                                try {
                                    if (line.startsWith("bytes")) {
                                        in = in2;
                                        int i2 = numIndex;
                                        String str = numString;
                                        errorCode = errorCode2;
                                        try {
                                            scaled = Math.toIntExact(parsedNum / Trace.TRACE_TAG_DATABASE);
                                        } catch (ArithmeticException e) {
                                            ignored = e;
                                            StringBuilder sb = new StringBuilder();
                                            ArithmeticException arithmeticException = ignored;
                                            sb.append("Number overflows in ");
                                            sb.append(line);
                                            Log.e(TAG, sb.toString());
                                            scaled = causeCode;
                                            in2 = in;
                                            errorCode2 = errorCode;
                                            i = -1;
                                        }
                                    } else {
                                        in = in2;
                                        int i3 = numIndex;
                                        String str2 = numString;
                                        errorCode = errorCode2;
                                        scaled = Math.toIntExact(parsedNum);
                                    }
                                } catch (ArithmeticException e2) {
                                    ignored = e2;
                                    in = in2;
                                    int i4 = numIndex;
                                    String str3 = numString;
                                    errorCode = errorCode2;
                                    long j = parsedNum;
                                    StringBuilder sb2 = new StringBuilder();
                                    ArithmeticException arithmeticException2 = ignored;
                                    sb2.append("Number overflows in ");
                                    sb2.append(line);
                                    Log.e(TAG, sb2.toString());
                                    scaled = causeCode;
                                    in2 = in;
                                    errorCode2 = errorCode;
                                    i = -1;
                                }
                                try {
                                    if (line.startsWith("time")) {
                                        timeTotal = scaled;
                                    } else if (line.startsWith("uncrypt_time")) {
                                        uncryptTime = scaled;
                                    } else if (line.startsWith("source_build")) {
                                        sourceVersion = scaled;
                                    } else if (line.startsWith("bytes_written")) {
                                        bytesWrittenInMiB = bytesWrittenInMiB == -1 ? scaled : bytesWrittenInMiB + scaled;
                                    } else if (line.startsWith("bytes_stashed")) {
                                        bytesStashedInMiB = bytesStashedInMiB == -1 ? scaled : bytesStashedInMiB + scaled;
                                    } else if (line.startsWith("temperature_start")) {
                                        temperatureStart = scaled;
                                    } else if (line.startsWith("temperature_end")) {
                                        temperatureEnd = scaled;
                                    } else if (line.startsWith("temperature_max")) {
                                        temperatureMax = scaled;
                                    } else if (line.startsWith("error")) {
                                        errorCode2 = scaled;
                                        scaled = causeCode;
                                        in2 = in;
                                        i = -1;
                                    } else if (line.startsWith("cause")) {
                                        errorCode2 = errorCode;
                                        in2 = in;
                                        i = -1;
                                    }
                                    scaled = causeCode;
                                    errorCode2 = errorCode;
                                    in2 = in;
                                } catch (Throwable th2) {
                                    th = th2;
                                    in2 = in;
                                    th = null;
                                    $closeResource(th, in2);
                                    throw th;
                                }
                            } catch (NumberFormatException e3) {
                                in = in2;
                                int i5 = numIndex;
                                String str4 = numString;
                                errorCode = errorCode2;
                                NumberFormatException numberFormatException = e3;
                                Log.e(TAG, "Failed to parse numbers in " + line);
                            }
                            i = -1;
                        }
                        scaled = causeCode;
                        in2 = in;
                        errorCode2 = errorCode;
                        i = -1;
                    } catch (Throwable th3) {
                        th = th3;
                        th = null;
                        $closeResource(th, in2);
                        throw th;
                    }
                } catch (Throwable th4) {
                    th = th4;
                    th = null;
                    $closeResource(th, in2);
                    throw th;
                }
            }
            BufferedReader in3 = in2;
            int errorCode3 = errorCode2;
            if (timeTotal != -1) {
                MetricsLogger.histogram(context2, "ota_time_total", timeTotal);
            }
            if (uncryptTime != -1) {
                MetricsLogger.histogram(context2, "ota_uncrypt_time", uncryptTime);
            }
            if (sourceVersion != -1) {
                MetricsLogger.histogram(context2, "ota_source_version", sourceVersion);
            }
            if (bytesWrittenInMiB != -1) {
                MetricsLogger.histogram(context2, "ota_written_in_MiBs", bytesWrittenInMiB);
            }
            if (bytesStashedInMiB != -1) {
                MetricsLogger.histogram(context2, "ota_stashed_in_MiBs", bytesStashedInMiB);
            }
            if (temperatureStart != -1) {
                MetricsLogger.histogram(context2, "ota_temperature_start", temperatureStart);
            }
            if (temperatureEnd != -1) {
                MetricsLogger.histogram(context2, "ota_temperature_end", temperatureEnd);
            }
            if (temperatureMax != -1) {
                MetricsLogger.histogram(context2, "ota_temperature_max", temperatureMax);
            }
            int errorCode4 = errorCode3;
            if (errorCode4 != -1) {
                MetricsLogger.histogram(context2, "ota_non_ab_error_code", errorCode4);
            }
            if (causeCode != -1) {
                MetricsLogger.histogram(context2, "ota_non_ab_cause_code", causeCode);
            }
            $closeResource(null, in3);
        } catch (IOException e4) {
            Log.e(TAG, "Failed to read lines in last_install", e4);
        }
    }

    public static String handleAftermath(Context context) {
        String log = null;
        try {
            log = FileUtils.readTextFile(LOG_FILE, Menu.CATEGORY_MASK, "...\n");
        } catch (FileNotFoundException e) {
            Log.i(TAG, "No recovery log file");
        } catch (IOException e2) {
            Log.e(TAG, "Error reading recovery log", e2);
        }
        if (log != null) {
            parseLastInstallLog(context);
        }
        boolean reservePackage = BLOCK_MAP_FILE.exists();
        int i = 0;
        if (!reservePackage && UNCRYPT_PACKAGE_FILE.exists()) {
            String filename = null;
            try {
                filename = FileUtils.readTextFile(UNCRYPT_PACKAGE_FILE, 0, null);
            } catch (IOException e3) {
                Log.e(TAG, "Error reading uncrypt file", e3);
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
        while (names != null && i < names.length) {
            if (!names[i].startsWith(LAST_PREFIX) && ((!reservePackage || !names[i].equals(BLOCK_MAP_FILE.getName())) && ((!reservePackage || !names[i].equals(UNCRYPT_PACKAGE_FILE.getName())) && !names[i].trim().equals(COMMAND_FILE_STRING)))) {
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
        if (!name.delete()) {
            Log.e(TAG, "Can't delete: " + name);
            return;
        }
        Log.i(TAG, "Deleted: " + name);
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

    private void rebootRecoveryWithCommand(String command) {
        try {
            this.mService.rebootRecoveryWithCommand(command);
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to execute rebootRecoveryWithCommand!");
        }
    }

    private static String sanitizeArg(String arg) {
        return arg.replace(0, '?').replace(10, '?');
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

    public static void hwWipeEuiccData(Context context) {
        wipeEuiccData(context, "android");
    }
}
