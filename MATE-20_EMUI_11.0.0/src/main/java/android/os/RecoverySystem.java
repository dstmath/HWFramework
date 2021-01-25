package android.os;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.SystemApi;
import android.annotation.UnsupportedAppUsage;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.IRecoverySystemProgressListener;
import android.os.IVold;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.telephony.euicc.EuiccManager;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.WindowManager;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import libcore.io.Streams;
import sun.security.pkcs.PKCS7;
import sun.security.pkcs.SignerInfo;

public class RecoverySystem {
    private static final String ACTION_EUICC_FACTORY_RESET = "com.android.internal.action.EUICC_FACTORY_RESET";
    public static final File BLOCK_MAP_FILE = new File(RECOVERY_DIR, "block.map");
    private static String COMMAND_FILE_STRING = "command";
    private static final long DEFAULT_EUICC_FACTORY_RESET_TIMEOUT_MILLIS = 30000;
    private static final File DEFAULT_KEYSTORE = new File("/system/etc/security/otacerts.zip");
    private static final String DEVICE_PROPER_CONFIG = "ro.ril.esim_type";
    private static final int DEVICE_TYPE_DUAL_SIM_AND_ESIM = 3;
    private static final int DEVICE_TYPE_NONE_ESIM = 0;
    private static final int DEVICE_TYPE_SINGLE_SIM_AND_ESIM = 2;
    private static final String LAST_INSTALL_PATH = "last_install";
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
        HashSet<X509Certificate> trusted = new HashSet<>();
        if (keystore == null) {
            keystore = DEFAULT_KEYSTORE;
        }
        ZipFile zip = new ZipFile(keystore);
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                InputStream is = zip.getInputStream((ZipEntry) entries.nextElement());
                try {
                    trusted.add((X509Certificate) cf.generateCertificate(is));
                } finally {
                    is.close();
                }
            }
            return trusted;
        } finally {
            zip.close();
        }
    }

    public static void verifyPackage(File packageFile, final ProgressListener listener, File deviceCertsZipFile) throws IOException, GeneralSecurityException {
        Throwable th;
        boolean verified;
        final long fileLen = packageFile.length();
        final RandomAccessFile raf = new RandomAccessFile(packageFile, "r");
        try {
            final long startTimeMillis = System.currentTimeMillis();
            if (listener != null) {
                listener.onProgress(0);
            }
            raf.seek(fileLen - 6);
            byte[] footer = new byte[6];
            raf.readFully(footer);
            if (footer[2] == -1) {
                if (footer[3] == -1) {
                    final int commentSize = ((footer[5] & 255) << 8) | (footer[4] & 255);
                    int signatureStart = (footer[0] & 255) | ((footer[1] & 255) << 8);
                    byte[] eocd = new byte[(commentSize + 22)];
                    try {
                        raf.seek(fileLen - ((long) (commentSize + 22)));
                        raf.readFully(eocd);
                        if (eocd[0] == 80 && eocd[1] == 75 && eocd[2] == 5 && eocd[3] == 6) {
                            int i = 4;
                            for (int i2 = 3; i < eocd.length - i2; i2 = 3) {
                                if (eocd[i] == 80 && eocd[i + 1] == 75 && eocd[i + 2] == 5) {
                                    if (eocd[i + 3] == 6) {
                                        throw new SignatureException("EOCD marker found after start of EOCD");
                                    }
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
                            Iterator<X509Certificate> it = getTrustedCerts(deviceCertsZipFile == null ? DEFAULT_KEYSTORE : deviceCertsZipFile).iterator();
                            while (true) {
                                if (!it.hasNext()) {
                                    verified = false;
                                    break;
                                } else if (it.next().getPublicKey().equals(signatureKey)) {
                                    verified = true;
                                    break;
                                } else {
                                    signerInfos = signerInfos;
                                }
                            }
                            if (verified) {
                                raf.seek(0);
                                SignerInfo verifyResult = block.verify(signerInfo, new InputStream() {
                                    /* class android.os.RecoverySystem.AnonymousClass1 */
                                    int lastPercent = 0;
                                    long lastPublishTime = startTimeMillis;
                                    long soFar = 0;
                                    long toRead = ((fileLen - ((long) commentSize)) - 2);

                                    @Override // java.io.InputStream
                                    public int read() throws IOException {
                                        throw new UnsupportedOperationException();
                                    }

                                    @Override // java.io.InputStream
                                    public int read(byte[] b, int off, int len) throws IOException {
                                        if (this.soFar >= this.toRead || Thread.currentThread().isInterrupted()) {
                                            return -1;
                                        }
                                        int size = len;
                                        long j = this.soFar;
                                        long j2 = this.toRead;
                                        if (((long) size) + j > j2) {
                                            size = (int) (j2 - j);
                                        }
                                        int read = raf.read(b, off, size);
                                        this.soFar += (long) read;
                                        if (listener != null) {
                                            long now = System.currentTimeMillis();
                                            int p = (int) ((this.soFar * 100) / this.toRead);
                                            if (p > this.lastPercent && now - this.lastPublishTime > RecoverySystem.PUBLISH_PROGRESS_INTERVAL_MS) {
                                                this.lastPercent = p;
                                                this.lastPublishTime = now;
                                                listener.onProgress(this.lastPercent);
                                            }
                                        }
                                        return read;
                                    }
                                });
                                boolean interrupted = Thread.interrupted();
                                if (listener != null) {
                                    try {
                                        listener.onProgress(100);
                                    } catch (Throwable th2) {
                                        th = th2;
                                        raf.close();
                                        throw th;
                                    }
                                }
                                if (interrupted) {
                                    throw new SignatureException("verification was interrupted");
                                } else if (verifyResult != null) {
                                    raf.close();
                                    if (!readAndVerifyPackageCompatibilityEntry(packageFile)) {
                                        throw new SignatureException("package compatibility verification failed");
                                    }
                                    return;
                                } else {
                                    throw new SignatureException("signature digest verification failed");
                                }
                            } else {
                                throw new SignatureException("signature doesn't match any trusted key");
                            }
                        } else {
                            throw new SignatureException("no signature in file (bad footer)");
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        raf.close();
                        throw th;
                    }
                }
            }
            throw new SignatureException("no signature in file (no footer)");
        } catch (Throwable th4) {
            th = th4;
            raf.close();
            throw th;
        }
    }

    @UnsupportedAppUsage
    private static boolean verifyPackageCompatibility(InputStream inputStream) throws IOException {
        long entrySize;
        ArrayList<String> list = new ArrayList<>();
        ZipInputStream zis = new ZipInputStream(inputStream);
        while (true) {
            ZipEntry entry = zis.getNextEntry();
            if (entry != null) {
                entrySize = entry.getSize();
                if (entrySize > 2147483647L || entrySize < 0) {
                    break;
                }
                byte[] bytes = new byte[((int) entrySize)];
                Streams.readFully(zis, bytes);
                list.add(new String(bytes, StandardCharsets.UTF_8));
            } else if (!list.isEmpty()) {
                return VintfObject.verify((String[]) list.toArray(new String[list.size()])) == 0;
            } else {
                throw new IOException("no entries found in the compatibility file");
            }
        }
        throw new IOException("invalid entry size (" + entrySize + ") in the compatibility file");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0021, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0022, code lost:
        $closeResource(r1, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0025, code lost:
        throw r2;
     */
    private static boolean readAndVerifyPackageCompatibilityEntry(File packageFile) throws IOException {
        ZipFile zip = new ZipFile(packageFile);
        ZipEntry entry = zip.getEntry("compatibility.zip");
        if (entry == null) {
            $closeResource(null, zip);
            return true;
        }
        boolean verifyPackageCompatibility = verifyPackageCompatibility(zip.getInputStream(entry));
        $closeResource(null, zip);
        return verifyPackageCompatibility;
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

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0014, code lost:
        throw r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0010, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0011, code lost:
        $closeResource(r1, r0);
     */
    @SystemApi
    @SuppressLint({"Doclava125"})
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
            RecoverySystem rs = (RecoverySystem) context.getSystemService("recovery");
            IRecoverySystemProgressListener progressListener = null;
            if (listener != null) {
                if (handler != null) {
                    progressHandler = handler;
                } else {
                    progressHandler = new Handler(context.getMainLooper());
                }
                progressListener = new IRecoverySystemProgressListener.Stub() {
                    /* class android.os.RecoverySystem.AnonymousClass2 */
                    int lastProgress = 0;
                    long lastPublishTime = System.currentTimeMillis();

                    @Override // android.os.IRecoverySystemProgressListener
                    public void onProgress(final int progress) {
                        final long now = System.currentTimeMillis();
                        Handler.this.post(new Runnable() {
                            /* class android.os.RecoverySystem.AnonymousClass2.AnonymousClass1 */

                            @Override // java.lang.Runnable
                            public void run() {
                                if (progress > AnonymousClass2.this.lastProgress && now - AnonymousClass2.this.lastPublishTime > RecoverySystem.PUBLISH_PROGRESS_INTERVAL_MS) {
                                    AnonymousClass2 r0 = AnonymousClass2.this;
                                    r0.lastProgress = progress;
                                    r0.lastPublishTime = now;
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
            String command = ("--update_package=" + filename + "\n") + ("--locale=" + Locale.getDefault().toLanguageTag() + "\n");
            if (securityUpdate) {
                command = command + "--security\n";
            }
            if (((RecoverySystem) context.getSystemService("recovery")).setupBcb(command)) {
                PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                String reason = PowerManager.REBOOT_RECOVERY_UPDATE;
                if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_LEANBACK) && ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getState() != 2) {
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
        String command = ("--update_package=" + filename + "\n") + ("--locale=" + Locale.getDefault().toLanguageTag() + "\n");
        if (securityUpdate) {
            command = command + "--security\n";
        }
        if (!((RecoverySystem) context.getSystemService("recovery")).setupBcb(command)) {
            throw new IOException("schedule update on boot failed");
        }
    }

    @SystemApi
    public static void cancelScheduledUpdate(Context context) throws IOException {
        if (!((RecoverySystem) context.getSystemService("recovery")).clearBcb()) {
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
        UserManager um = (UserManager) context.getSystemService("user");
        if (force || !um.hasUserRestriction(UserManager.DISALLOW_FACTORY_RESET)) {
            final ConditionVariable condition = new ConditionVariable();
            Intent intent = new Intent(Intent.ACTION_MASTER_CLEAR_NOTIFICATION);
            intent.addFlags(285212672);
            context.sendOrderedBroadcastAsUser(intent, UserHandle.SYSTEM, Manifest.permission.MASTER_CLEAR, new BroadcastReceiver() {
                /* class android.os.RecoverySystem.AnonymousClass3 */

                @Override // android.content.BroadcastReceiver
                public void onReceive(Context context, Intent intent) {
                    ConditionVariable.this.open();
                }
            }, null, 0, null, null);
            condition.block();
            if (wipeEuicc) {
                wipeEuiccData(context, "android");
            }
            String shutdownArg = null;
            if (shutdown) {
                shutdownArg = "--shutdown_after";
            }
            String reasonArg = null;
            if (!TextUtils.isEmpty(reason)) {
                String timeStamp = DateFormat.format("yyyy-MM-ddTHH:mm:ssZ", System.currentTimeMillis()).toString();
                StringBuilder sb = new StringBuilder();
                sb.append("--reason=");
                sb.append(sanitizeArg(reason + SmsManager.REGEX_PREFIX_DELIMITER + timeStamp));
                reasonArg = sb.toString();
            }
            bootCommand(context, shutdownArg, "--wipe_data", reasonArg, "--locale=" + Locale.getDefault().toLanguageTag());
            return;
        }
        throw new SecurityException("Wiping data is not allowed for this user.");
    }

    public static boolean wipeEuiccData(Context context, String packageName) {
        InterruptedException e;
        InterruptedException e2;
        if (Settings.Global.getInt(context.getContentResolver(), Settings.Global.EUICC_PROVISIONED, 0) != 0 || isNeedWipeEuiccWithDeviceType()) {
            EuiccManager euiccManager = (EuiccManager) context.getSystemService(Context.EUICC_SERVICE);
            if (euiccManager == null || !euiccManager.isEnabled()) {
                return false;
            }
            final CountDownLatch euiccFactoryResetLatch = new CountDownLatch(1);
            final AtomicBoolean wipingSucceeded = new AtomicBoolean(false);
            BroadcastReceiver euiccWipeFinishReceiver = new BroadcastReceiver() {
                /* class android.os.RecoverySystem.AnonymousClass4 */

                @Override // android.content.BroadcastReceiver
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
            PendingIntent callbackIntent = PendingIntent.getBroadcastAsUser(context, 0, intent, 134217728, UserHandle.SYSTEM);
            IntentFilter filterConsent = new IntentFilter();
            filterConsent.addAction(ACTION_EUICC_FACTORY_RESET);
            HandlerThread euiccHandlerThread = new HandlerThread("euiccWipeFinishReceiverThread");
            euiccHandlerThread.start();
            context.getApplicationContext().registerReceiver(euiccWipeFinishReceiver, filterConsent, null, new Handler(euiccHandlerThread.getLooper()));
            euiccManager.eraseSubscriptions(callbackIntent);
            try {
                try {
                    long waitingTimeMillis = Settings.Global.getLong(context.getContentResolver(), Settings.Global.EUICC_FACTORY_RESET_TIMEOUT_MILLIS, 30000);
                    if (waitingTimeMillis < 5000) {
                        waitingTimeMillis = 5000;
                    } else if (waitingTimeMillis > 60000) {
                        waitingTimeMillis = 60000;
                    }
                    try {
                        if (!euiccFactoryResetLatch.await(waitingTimeMillis, TimeUnit.MILLISECONDS)) {
                            Log.e(TAG, "Timeout wiping eUICC data.");
                            context.getApplicationContext().unregisterReceiver(euiccWipeFinishReceiver);
                            return false;
                        }
                        context.getApplicationContext().unregisterReceiver(euiccWipeFinishReceiver);
                        return wipingSucceeded.get();
                    } catch (InterruptedException e3) {
                        e2 = e3;
                        try {
                            Thread.currentThread().interrupt();
                            Log.e(TAG, "Wiping eUICC data interrupted", e2);
                            context.getApplicationContext().unregisterReceiver(euiccWipeFinishReceiver);
                            return false;
                        } catch (Throwable th) {
                            e = th;
                            context.getApplicationContext().unregisterReceiver(euiccWipeFinishReceiver);
                            throw e;
                        }
                    }
                } catch (InterruptedException e4) {
                    e2 = e4;
                    Thread.currentThread().interrupt();
                    Log.e(TAG, "Wiping eUICC data interrupted", e2);
                    context.getApplicationContext().unregisterReceiver(euiccWipeFinishReceiver);
                    return false;
                } catch (Throwable th2) {
                    e = th2;
                    context.getApplicationContext().unregisterReceiver(euiccWipeFinishReceiver);
                    throw e;
                }
            } catch (InterruptedException e5) {
                e2 = e5;
                Thread.currentThread().interrupt();
                Log.e(TAG, "Wiping eUICC data interrupted", e2);
                context.getApplicationContext().unregisterReceiver(euiccWipeFinishReceiver);
                return false;
            } catch (Throwable th3) {
                e = th3;
                context.getApplicationContext().unregisterReceiver(euiccWipeFinishReceiver);
                throw e;
            }
        } else {
            Log.d(TAG, "Skipping eUICC wipe/retain as it is not provisioned");
            return true;
        }
    }

    public static void rebootPromptAndWipeUserData(Context context, String reason) throws IOException {
        boolean checkpointing = false;
        IVold vold = null;
        try {
            vold = IVold.Stub.asInterface(ServiceManager.checkService("vold"));
            if (vold != null) {
                checkpointing = vold.needsCheckpoint();
            } else {
                Log.w(TAG, "Failed to get vold");
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to check for checkpointing");
        }
        if (checkpointing) {
            try {
                vold.abortChanges("rescueparty", false);
                Log.i(TAG, "Rescue Party requested wipe. Aborting update");
            } catch (Exception e2) {
                Log.i(TAG, "Rescue Party requested wipe. Rebooting instead.");
                ((PowerManager) context.getSystemService(Context.POWER_SERVICE)).reboot("rescueparty");
            }
        } else {
            String reasonArg = null;
            if (!TextUtils.isEmpty(reason)) {
                reasonArg = "--reason=" + sanitizeArg(reason);
            }
            bootCommand(context, null, "--prompt_and_wipe_data", reasonArg, "--locale=" + Locale.getDefault().toString());
        }
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
        bootCommand(context, "--wipe_ab", "--wipe_package=" + packageFile.getCanonicalPath(), reasonArg, "--locale=" + Locale.getDefault().toLanguageTag());
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
        ((RecoverySystem) context.getSystemService("recovery")).rebootRecoveryWithCommand(command.toString());
        throw new IOException("Reboot failed (no permissions?)");
    }

    public static String handleAftermath(Context context) {
        String log = null;
        try {
            log = FileUtils.readTextFile(LOG_FILE, -65536, "...\n");
        } catch (FileNotFoundException e) {
            Log.i(TAG, "No recovery log file");
        } catch (IOException e2) {
            Log.e(TAG, "Error reading recovery log", e2);
        }
        boolean reservePackage = BLOCK_MAP_FILE.exists();
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
        int i = 0;
        while (names != null && i < names.length) {
            if (!names[i].startsWith(LAST_PREFIX) && !names[i].equals(LAST_INSTALL_PATH) && ((!reservePackage || !names[i].equals(BLOCK_MAP_FILE.getName())) && ((!reservePackage || !names[i].equals(UNCRYPT_PACKAGE_FILE.getName())) && !names[i].trim().equals(COMMAND_FILE_STRING)))) {
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
        }
    }

    private static String sanitizeArg(String arg) {
        return arg.replace((char) 0, '?').replace('\n', '?');
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

    public static boolean hwWipeEuiccData(Context context) {
        return wipeEuiccData(context, "android");
    }

    private static boolean isNeedWipeEuiccWithDeviceType() {
        int deviceType = SystemProperties.getInt(DEVICE_PROPER_CONFIG, 0);
        Log.i(TAG, "isNeedWipeEuiccWithDeviceType deviceType=" + deviceType);
        if (deviceType == 3 || deviceType == 2) {
            return true;
        }
        return false;
    }
}
