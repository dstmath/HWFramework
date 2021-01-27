package ohos.bundlemgr;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.RemoteException;
import android.os.ServiceManager;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import ohos.appexecfwk.utils.AppLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.security.permission.PermissionInner;

public class PackageInstallerAdapter {
    private static final String ACTION_INSTALL_NOTIFICATION = "ohos.bundlemgr.packageinstaller.ACTION_INSTALL_COMMIT";
    private static final String ACTION_UNINSTALL_NOTIFICATION = "ohos.bundlemgr.packageinstaller.ACTION_UNINSTALL_COMMIT";
    private static final int DEFAULT_BUFFER_SIZE = 65536;
    private static final String ENTRY_APK = "entry.apk";
    private static final String FEATURE_APK = "feature.apk";
    private static final String INSTALL_SUCCEEDED = "INSTALL_SUCCEEDED";
    private static final String INTENT_NULL_ERROR = "INTENT_NULL_ERROR";
    private static final int MAX_INSTALL_WAIT_TIME_SECOND = 30;
    private static final long MAX_READ_FILE_SIZE = 4294967296L;
    private static final String PERMISSION_INSTALL_BUNDLE = "ohos.permission.INSTALL_BUNDLE";
    private static final HiLogLabel PI_ADAPTER_LABEL = new HiLogLabel(3, 218108160, "PackageInstallerAdapter");
    private static final String RESTRICTED_PERMISSION_DENY = "RESTRICED_PERMISSION_DEMY";
    private static final String SOURCE_DIR_ERROR = "SOURCE_DIR_ERROR";
    private final Context context;
    private PackageInstaller currentPackageInstaller;
    private int currentUserId = -1;
    private Signature[] platformSignatures = null;
    private List<String> restrictedPermissions = new ArrayList();
    private int uninstallSessionId = Integer.MIN_VALUE;

    public PackageInstallerAdapter(Context context2) {
        this.context = context2;
        initPlatformSignature();
    }

    public PackageInstalledStatus installShellApkByPms(InstallShellInfo installShellInfo) {
        Throwable th;
        int i;
        IOException e;
        SecurityException e2;
        InterruptedException e3;
        PackageInstalledStatus packageInstalledStatus = new PackageInstalledStatus();
        if (this.context == null) {
            AppLog.i(PI_ADAPTER_LABEL, "Shell apk install failure: PackageInstallerAdapter context is null", new Object[0]);
            return packageInstalledStatus;
        }
        PackageInstaller packageInstaller = getPackageInstaller(installShellInfo.getUserId());
        if (packageInstaller == null) {
            AppLog.i(PI_ADAPTER_LABEL, "Shell apk install failure: get PackageInstaller is null", new Object[0]);
            return packageInstalledStatus;
        }
        try {
            this.restrictedPermissions = installShellInfo.getRestrictedPermissions();
            i = createSessionInner(packageInstaller, installShellInfo.getPackageName(), installShellInfo.isAppend(), installShellInfo.getInstallLocation(), installShellInfo.getInstallerUid());
            try {
                PackageInstaller.Session openSession = packageInstaller.openSession(i);
                if (!writeToSessionStream(openSession, installShellInfo, packageInstalledStatus)) {
                    AppLog.i(PI_ADAPTER_LABEL, "Shell apk install failure: Write to session stream failure", new Object[0]);
                    closeAndAbandonSession(packageInstaller, openSession, i);
                    return packageInstalledStatus;
                }
                CountDownLatch countDownLatch = new CountDownLatch(1);
                IntentSender sessionCommitCallback = sessionCommitCallback(installShellInfo.getPackageName(), i, packageInstalledStatus, countDownLatch);
                if (sessionCommitCallback == null) {
                    AppLog.e(PI_ADAPTER_LABEL, "Shell apk install failure: intentSender is null", new Object[0]);
                    closeAndAbandonSession(packageInstaller, openSession, i);
                    return packageInstalledStatus;
                }
                openSession.commit(sessionCommitCallback);
                if (!countDownLatch.await(30, TimeUnit.SECONDS)) {
                    AppLog.e(PI_ADAPTER_LABEL, "Shell apk install failure: timeout", new Object[0]);
                }
                closeAndAbandonSession(packageInstaller, openSession, i);
                return packageInstalledStatus;
            } catch (IOException e4) {
                e = e4;
                AppLog.i(PI_ADAPTER_LABEL, "Shell apk install failure: Cannot open session while install %{private}s : %{public}s", installShellInfo.getPackageName(), e.getMessage());
                closeAndAbandonSession(packageInstaller, null, i);
                return packageInstalledStatus;
            } catch (SecurityException e5) {
                e2 = e5;
                AppLog.i(PI_ADAPTER_LABEL, "Shell apk install failure: No permission to install %{private}s : %{public}s", installShellInfo.getPackageName(), e2.getMessage());
                closeAndAbandonSession(packageInstaller, null, i);
                return packageInstalledStatus;
            } catch (InterruptedException e6) {
                e3 = e6;
                try {
                    AppLog.i(PI_ADAPTER_LABEL, "Shell apk install failure: Wait Interrupt exception %{private}s : %{public}s", installShellInfo.getPackageName(), e3.getMessage());
                    closeAndAbandonSession(packageInstaller, null, i);
                    return packageInstalledStatus;
                } catch (Throwable th2) {
                    th = th2;
                    closeAndAbandonSession(packageInstaller, null, i);
                    throw th;
                }
            }
        } catch (IOException e7) {
            e = e7;
            i = 0;
            AppLog.i(PI_ADAPTER_LABEL, "Shell apk install failure: Cannot open session while install %{private}s : %{public}s", installShellInfo.getPackageName(), e.getMessage());
            closeAndAbandonSession(packageInstaller, null, i);
            return packageInstalledStatus;
        } catch (SecurityException e8) {
            e2 = e8;
            i = 0;
            AppLog.i(PI_ADAPTER_LABEL, "Shell apk install failure: No permission to install %{private}s : %{public}s", installShellInfo.getPackageName(), e2.getMessage());
            closeAndAbandonSession(packageInstaller, null, i);
            return packageInstalledStatus;
        } catch (InterruptedException e9) {
            e3 = e9;
            i = 0;
            AppLog.i(PI_ADAPTER_LABEL, "Shell apk install failure: Wait Interrupt exception %{private}s : %{public}s", installShellInfo.getPackageName(), e3.getMessage());
            closeAndAbandonSession(packageInstaller, null, i);
            return packageInstalledStatus;
        } catch (Throwable th3) {
            th = th3;
            i = 0;
            closeAndAbandonSession(packageInstaller, null, i);
            throw th;
        }
    }

    public PackageInstalledStatus uninstallShellApkByPms(String str, int i, boolean z) {
        PackageInstaller packageInstaller;
        PackageInstalledStatus packageInstalledStatus = new PackageInstalledStatus();
        if (this.context == null) {
            AppLog.i(PI_ADAPTER_LABEL, "Shell apk uninstall failure: PackageInstallerAdapter context is null", new Object[0]);
            return packageInstalledStatus;
        } else if (str == null || str.isEmpty()) {
            AppLog.i(PI_ADAPTER_LABEL, "Shell apk uninstall failure: Param packageName is error", new Object[0]);
            return packageInstalledStatus;
        } else {
            CountDownLatch countDownLatch = new CountDownLatch(1);
            int i2 = this.uninstallSessionId + 1;
            this.uninstallSessionId = i2;
            IntentSender uninstallCallback = uninstallCallback(str, i2, packageInstalledStatus, countDownLatch);
            if (uninstallCallback == null || (packageInstaller = getPackageInstaller(i)) == null) {
                return packageInstalledStatus;
            }
            if (i == -1) {
                boolean z2 = z ? 1 : 0;
                char c = z ? 1 : 0;
                z = z2 | true;
            }
            try {
                int i3 = z ? 1 : 0;
                int i4 = z ? 1 : 0;
                int i5 = z ? 1 : 0;
                packageInstaller.uninstall(str, i3, uninstallCallback);
                if (!countDownLatch.await(30, TimeUnit.SECONDS)) {
                    AppLog.e(PI_ADAPTER_LABEL, "Shell apk uninstall failure: timeout", new Object[0]);
                }
                return packageInstalledStatus;
            } catch (InterruptedException unused) {
                AppLog.d(PI_ADAPTER_LABEL, "Unexpected error wait condition", new Object[0]);
                return packageInstalledStatus;
            }
        }
    }

    private int createSessionInner(PackageInstaller packageInstaller, String str, boolean z, int i, int i2) throws IOException {
        int i3 = 2;
        AppLog.d(PI_ADAPTER_LABEL, "createSessionInner isAppend:%{private}d, installLocation:%{private}d", Integer.valueOf(z ? 1 : 0), Integer.valueOf(i));
        if (!z) {
            i3 = 1;
        }
        PackageInstaller.SessionParams sessionParams = new PackageInstaller.SessionParams(i3);
        sessionParams.setInstallLocation(i);
        String nameForUid = this.context.getPackageManager().getNameForUid(i2);
        if (nameForUid != null) {
            sessionParams.installerPackageName = nameForUid;
        } else {
            sessionParams.installerPackageName = "";
        }
        if (z) {
            sessionParams.setAppPackageName(str);
            sessionParams.setDontKillApp(z);
        }
        return packageInstaller.createSession(sessionParams);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0061, code lost:
        r8 = "";
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0064, code lost:
        if (r24 == false) goto L_0x00a5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0066, code lost:
        r12 = r22.lastIndexOf(47);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x006c, code lost:
        if (r12 == -1) goto L_0x0082;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x006e, code lost:
        r8 = r22.substring(0, r12 + 1) + r23;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0086, code lost:
        if (r8.isEmpty() != false) goto L_0x00a5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0088, code lost:
        r12 = new java.io.File(r8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:?, code lost:
        r8 = r12.getCanonicalPath();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0096, code lost:
        r8 = new java.io.FileOutputStream(r12);
        r0 = r8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x009c, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x009d, code lost:
        r8 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x00a0, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x00a1, code lost:
        r2 = null;
        r8 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00a5, code lost:
        r12 = null;
        r0 = r8;
        r8 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:?, code lost:
        r3 = r21.openWrite(getApkFileName(r4.getName(), r23), 0, -1);
        r4 = new byte[65536];
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00be, code lost:
        r13 = r7.read(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x00c2, code lost:
        if (r13 == -1) goto L_0x00df;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00c4, code lost:
        r9 = r9 + ((long) r13);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00cd, code lost:
        if (r9 <= ohos.bundlemgr.PackageInstallerAdapter.MAX_READ_FILE_SIZE) goto L_0x00d6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00cf, code lost:
        closeStreams(r7, r3, r8);
        safeDeleteApkFile(r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x00d5, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x00d6, code lost:
        r3.write(r4, 0, r13);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x00d9, code lost:
        if (r8 == null) goto L_0x00be;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x00db, code lost:
        r8.write(r4, 0, r13);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x00df, code lost:
        r3.flush();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x00e2, code lost:
        if (r8 == null) goto L_0x00e7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x00e4, code lost:
        r8.flush();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x00eb, code lost:
        if (r0.isEmpty() != false) goto L_0x0109;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x00f3, code lost:
        if (ohos.bundlemgr.PackageInstallerAdapter.ENTRY_APK.equals(r23) == false) goto L_0x0109;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x00f9, code lost:
        if (checkProvisioningPermission(r0) != false) goto L_0x0109;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x00fb, code lost:
        r25.setStatusMessage(ohos.bundlemgr.PackageInstallerAdapter.RESTRICTED_PERMISSION_DENY);
        closeStreams(r7, r3, r8);
        safeDeleteApkFile(r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x0108, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x0109, code lost:
        ohos.appexecfwk.utils.AppLog.i(ohos.bundlemgr.PackageInstallerAdapter.PI_ADAPTER_LABEL, "extractApkForInstall success", new java.lang.Object[0]);
        r21.fsync(r3);
        closeStreams(r7, r3, r8);
        safeDeleteApkFile(r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x011d, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x011e, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x0120, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x0121, code lost:
        r2 = null;
     */
    private boolean extractApkForInstall(PackageInstaller.Session session, String str, String str2, boolean z, PackageInstalledStatus packageInstalledStatus) {
        File file;
        FileOutputStream fileOutputStream;
        ZipInputStream zipInputStream;
        Throwable th;
        OutputStream outputStream;
        IOException e;
        AppLog.i(PI_ADAPTER_LABEL, "Extract apk %{public}s from %{private}s", str2, str);
        ZipInputStream zipInputStream2 = null;
        r3 = null;
        r3 = null;
        OutputStream outputStream2 = null;
        try {
            File file2 = new File(str);
            if (file2.exists() && file2.isFile() && file2.canRead()) {
                long j = 0;
                if (file2.length() != 0) {
                    zipInputStream = new ZipInputStream(new FileInputStream(file2));
                    while (true) {
                        try {
                            ZipEntry nextEntry = zipInputStream.getNextEntry();
                            if (nextEntry == null) {
                                closeStreams(zipInputStream, null, null);
                                safeDeleteApkFile(null);
                                break;
                            } else if (!nextEntry.isDirectory()) {
                                if (nextEntry.getName().toLowerCase(Locale.ENGLISH).endsWith(str2)) {
                                    break;
                                }
                            }
                        } catch (IOException e2) {
                            e = e2;
                            outputStream = null;
                            fileOutputStream = null;
                            file = null;
                            zipInputStream2 = zipInputStream;
                            try {
                                AppLog.i(PI_ADAPTER_LABEL, "extractApkForInstall exception %{public}s", e.getMessage());
                                closeStreams(zipInputStream2, outputStream, fileOutputStream);
                                safeDeleteApkFile(file);
                                return false;
                            } catch (Throwable th2) {
                                th = th2;
                                zipInputStream = zipInputStream2;
                                outputStream2 = outputStream;
                                closeStreams(zipInputStream, outputStream2, fileOutputStream);
                                safeDeleteApkFile(file);
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            fileOutputStream = null;
                            file = fileOutputStream;
                            closeStreams(zipInputStream, outputStream2, fileOutputStream);
                            safeDeleteApkFile(file);
                            throw th;
                        }
                    }
                    return false;
                }
            }
            AppLog.i(PI_ADAPTER_LABEL, "Shell apk install failure: path is illegal", new Object[0]);
            closeStreams(null, null, null);
            safeDeleteApkFile(null);
            return false;
        } catch (IOException e3) {
            e = e3;
            outputStream = null;
            fileOutputStream = null;
            file = null;
            AppLog.i(PI_ADAPTER_LABEL, "extractApkForInstall exception %{public}s", e.getMessage());
            closeStreams(zipInputStream2, outputStream, fileOutputStream);
            safeDeleteApkFile(file);
            return false;
        } catch (Throwable th4) {
            th = th4;
            zipInputStream = null;
            fileOutputStream = null;
            file = fileOutputStream;
            closeStreams(zipInputStream, outputStream2, fileOutputStream);
            safeDeleteApkFile(file);
            throw th;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:28:0x008a  */
    private boolean writeToSessionStream(PackageInstaller.Session session, InstallShellInfo installShellInfo, PackageInstalledStatus packageInstalledStatus) {
        boolean z;
        boolean z2;
        boolean z3;
        if (".*".equals(installShellInfo.getProvisioningBundleName())) {
            AppLog.i(PI_ADAPTER_LABEL, "no need to check provision ", new Object[0]);
            z = false;
        } else {
            z = true;
        }
        if (installShellInfo.getEntryHap() == null || installShellInfo.getEntryHap().isEmpty()) {
            if (!installShellInfo.isAppend()) {
                String[] featureHaps = installShellInfo.getFeatureHaps();
                int length = featureHaps.length;
                int i = 0;
                while (true) {
                    if (i >= length) {
                        z3 = false;
                        break;
                    } else if (extractApkForInstall(session, featureHaps[i], ENTRY_APK, z, packageInstalledStatus)) {
                        z3 = true;
                        break;
                    } else {
                        i++;
                    }
                }
                if (!z3) {
                    AppLog.i(PI_ADAPTER_LABEL, "Entry apk extract from feature hap failed", new Object[0]);
                    return false;
                }
            } else {
                AppLog.i(PI_ADAPTER_LABEL, "Current is inherit install", new Object[0]);
                z2 = false;
                boolean z4 = false;
                for (String str : installShellInfo.getFeatureHaps()) {
                    if (extractApkForInstall(session, str, FEATURE_APK, z, packageInstalledStatus)) {
                        z4 = true;
                    }
                }
                if (!z2 || z4) {
                    return true;
                }
                installSuccessResult(installShellInfo.getPackageName(), INSTALL_SUCCEEDED, packageInstalledStatus);
                return false;
            }
        } else if (!extractApkForInstall(session, installShellInfo.getEntryHap(), ENTRY_APK, z, packageInstalledStatus)) {
            AppLog.i(PI_ADAPTER_LABEL, "Entry apk extract from entry hap failed", new Object[0]);
            return false;
        }
        z2 = true;
        boolean z42 = false;
        while (r12 < r11) {
        }
        if (!z2) {
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void installSuccessResult(String str, String str2, PackageInstalledStatus packageInstalledStatus) {
        try {
            String str3 = this.context.getPackageManager().getApplicationInfo(str, 4202496).sourceDir;
            if (str3 != null) {
                int lastIndexOf = str3.lastIndexOf(File.separator);
                if (lastIndexOf != -1) {
                    packageInstalledStatus.setShellInstalledDir(str3.substring(0, lastIndexOf));
                }
                packageInstalledStatus.setStatus(0);
                packageInstalledStatus.setStatusMessage(str2);
            } else {
                packageInstalledStatus.setStatus(1);
                packageInstalledStatus.setStatusMessage(SOURCE_DIR_ERROR);
            }
            AppLog.i(PI_ADAPTER_LABEL, "Install source path = %{private}s", packageInstalledStatus.getShellInstalledDir());
        } catch (PackageManager.NameNotFoundException unused) {
            AppLog.i(PI_ADAPTER_LABEL, "Shell apk install failure:get shell install dir exception", new Object[0]);
        }
    }

    private PackageInstaller getPackageInstaller(int i) {
        PackageInstaller packageInstaller;
        if (i == -1) {
            i = 0;
        }
        if (i == this.currentUserId && (packageInstaller = this.currentPackageInstaller) != null) {
            return packageInstaller;
        }
        try {
            this.currentPackageInstaller = new PackageInstaller(IPackageManager.Stub.asInterface(ServiceManager.getService("package")).getPackageInstaller(), this.context.getPackageName(), i);
            this.currentUserId = i;
        } catch (RemoteException unused) {
            this.currentPackageInstaller = null;
            AppLog.i(PI_ADAPTER_LABEL, "Unexpected error get IPackageInstaller form PackageManageer", new Object[0]);
        }
        return this.currentPackageInstaller;
    }

    private IntentSender uninstallCallback(String str, int i, final PackageInstalledStatus packageInstalledStatus, final CountDownLatch countDownLatch) {
        String str2 = "ohos.bundlemgr.packageinstaller.ACTION_UNINSTALL_COMMIT." + str;
        this.context.registerReceiver(new BroadcastReceiver() {
            /* class ohos.bundlemgr.PackageInstallerAdapter.AnonymousClass1 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if (context != null) {
                    context.unregisterReceiver(this);
                }
                if (intent != null) {
                    packageInstalledStatus.setStatusMessage(intent.getStringExtra("android.content.pm.extra.STATUS_MESSAGE"));
                    packageInstalledStatus.setStatus(intent.getIntExtra("android.content.pm.extra.STATUS", 1));
                } else {
                    packageInstalledStatus.setStatusMessage(PackageInstallerAdapter.INTENT_NULL_ERROR);
                    packageInstalledStatus.setStatus(1);
                }
                countDownLatch.countDown();
            }
        }, new IntentFilter(str2), PERMISSION_INSTALL_BUNDLE, null);
        Intent intent = new Intent(str2);
        intent.setFlags(268435456);
        PendingIntent broadcast = PendingIntent.getBroadcast(this.context, i, intent, 134217728);
        if (broadcast != null) {
            return broadcast.getIntentSender();
        }
        return null;
    }

    private IntentSender sessionCommitCallback(final String str, int i, final PackageInstalledStatus packageInstalledStatus, final CountDownLatch countDownLatch) {
        AnonymousClass2 r0 = new BroadcastReceiver() {
            /* class ohos.bundlemgr.PackageInstallerAdapter.AnonymousClass2 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if (context != null) {
                    context.unregisterReceiver(this);
                }
                if (intent == null) {
                    packageInstalledStatus.setStatusMessage(PackageInstallerAdapter.INTENT_NULL_ERROR);
                    packageInstalledStatus.setStatus(1);
                } else {
                    int intExtra = intent.getIntExtra("android.content.pm.extra.STATUS", 1);
                    String stringExtra = intent.getStringExtra("android.content.pm.extra.STATUS_MESSAGE");
                    if (intExtra == 0) {
                        PackageInstallerAdapter.this.installSuccessResult(str, stringExtra, packageInstalledStatus);
                    } else {
                        packageInstalledStatus.setStatus(intExtra);
                        packageInstalledStatus.setStatusMessage(stringExtra);
                    }
                }
                countDownLatch.countDown();
            }
        };
        String str2 = "ohos.bundlemgr.packageinstaller.ACTION_INSTALL_COMMIT." + str;
        this.context.registerReceiver(r0, new IntentFilter(str2), PERMISSION_INSTALL_BUNDLE, null);
        Intent intent = new Intent(str2);
        intent.putExtra("android.content.pm.extra.SESSION_ID", i);
        PendingIntent broadcast = PendingIntent.getBroadcast(this.context, i, intent, 134217728);
        if (broadcast != null) {
            return broadcast.getIntentSender();
        }
        AppLog.e(PI_ADAPTER_LABEL, "sessionCommitCallback getBroadcast failed", new Object[0]);
        return null;
    }

    private void safeCloseStream(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException unused) {
                AppLog.i(PI_ADAPTER_LABEL, "safeCloseStream failure", new Object[0]);
            }
        }
    }

    private void closeAndAbandonSession(PackageInstaller packageInstaller, PackageInstaller.Session session, int i) {
        if (session != null) {
            session.close();
        }
        try {
            packageInstaller.abandonSession(i);
        } catch (SecurityException e) {
            AppLog.i(PI_ADAPTER_LABEL, "Unexpected error abandon session for %{public}s", e.getMessage());
        }
    }

    private PackageInfo getPackageArchiveInfo(String str) {
        PackageManager packageManager;
        AppLog.d(PI_ADAPTER_LABEL, "getPackageArchiveInfo apkfile : %{private}s", str);
        Context context2 = this.context;
        if (context2 == null || (packageManager = context2.getPackageManager()) == null) {
            return null;
        }
        return packageManager.getPackageArchiveInfo(str, 134238272);
    }

    private boolean checkRestrictedPermissions(String[] strArr) {
        if (!(strArr == null || strArr.length == 0)) {
            for (String str : strArr) {
                if (PermissionInner.isRestrictedPermission(str) && !checkRestrictedPermission(str)) {
                    AppLog.w(PI_ADAPTER_LABEL, "checkRestrictedPermissions failed", new Object[0]);
                    return false;
                }
            }
            AppLog.i(PI_ADAPTER_LABEL, "checkRestrictedPermissions success", new Object[0]);
        }
        return true;
    }

    private boolean checkRestrictedPermission(String str) {
        AppLog.d(PI_ADAPTER_LABEL, "checkRestrictedPermission, permission : %{private}s", str);
        if (!this.restrictedPermissions.isEmpty()) {
            return this.restrictedPermissions.contains(str);
        }
        AppLog.w(PI_ADAPTER_LABEL, "checkRestrictedPermission, restrictedPermissions is null", new Object[0]);
        return false;
    }

    private void initPlatformSignature() {
        try {
            if (this.context == null) {
                AppLog.w(PI_ADAPTER_LABEL, "initPlatformSignature, context is null", new Object[0]);
                return;
            }
            PackageManager packageManager = this.context.getPackageManager();
            if (packageManager == null) {
                AppLog.w(PI_ADAPTER_LABEL, "initPlatformSignature, pm is null", new Object[0]);
                return;
            }
            PackageInfo packageInfo = packageManager.getPackageInfo("android", 134217728);
            if (packageInfo == null) {
                AppLog.w(PI_ADAPTER_LABEL, "initPlatformSignature, pkgInfo is null", new Object[0]);
            } else if (packageInfo.signingInfo == null) {
                AppLog.w(PI_ADAPTER_LABEL, "initPlatformSignature, signingInfo is null", new Object[0]);
            } else {
                this.platformSignatures = packageInfo.signingInfo.getApkContentsSigners();
            }
        } catch (PackageManager.NameNotFoundException e) {
            AppLog.w(PI_ADAPTER_LABEL, "initPlatformSignature error : %{private}s", e.getMessage());
        }
    }

    private boolean isPlatformSignature(Signature[] signatureArr) {
        Signature[] signatureArr2 = this.platformSignatures;
        if (signatureArr2 == null || signatureArr2.length == 0) {
            initPlatformSignature();
        }
        Signature[] signatureArr3 = this.platformSignatures;
        if (signatureArr3 != null && signatureArr3[0] != null) {
            return signatureArr3[0].equals(signatureArr[0]);
        }
        AppLog.w(PI_ADAPTER_LABEL, "isPlatformSignature, platformSignatures is null", new Object[0]);
        return false;
    }

    private boolean checkProvisioningPermission(String str) {
        PackageInfo packageInfo;
        boolean z;
        AppLog.i(PI_ADAPTER_LABEL, "checkProvisioningPermission", new Object[0]);
        PackageInfo packageArchiveInfo = getPackageArchiveInfo(str);
        if (packageArchiveInfo == null || packageArchiveInfo.signingInfo == null) {
            AppLog.e(PI_ADAPTER_LABEL, "checkProvisioningPermission, pkgInfo or signingInfo is null", new Object[0]);
            return false;
        }
        PackageManager packageManager = this.context.getPackageManager();
        try {
            packageInfo = packageManager.getPackageInfo(packageArchiveInfo.packageName, 16384);
            z = true;
        } catch (PackageManager.NameNotFoundException e) {
            AppLog.i(PI_ADAPTER_LABEL, "checkProvisioningPermission, error : %{private}s", e.getMessage());
            packageInfo = null;
            z = false;
        }
        if (z) {
            if (packageInfo == null || packageInfo.applicationInfo == null) {
                AppLog.e(PI_ADAPTER_LABEL, "checkProvisioningPermission, pkgInfoOld or applicationInfo is null", new Object[0]);
                return false;
            } else if (packageInfo.applicationInfo.isSystemApp() || packageInfo.applicationInfo.isUpdatedSystemApp() || packageInfo.applicationInfo.isPrivilegedApp() || packageInfo.applicationInfo.uid < 10000) {
                AppLog.i(PI_ADAPTER_LABEL, "checkProvisioningPermission, system or privileged app, do not check", new Object[0]);
                return true;
            }
        }
        if (isPlatformSignature(packageArchiveInfo.signingInfo.getApkContentsSigners())) {
            AppLog.i(PI_ADAPTER_LABEL, "checkProvisioningPermission, platform signature, do not check", new Object[0]);
            return true;
        }
        if (packageArchiveInfo.sharedUserId != null && !packageArchiveInfo.sharedUserId.isEmpty()) {
            try {
                if (packageManager.getUidForSharedUser(packageArchiveInfo.sharedUserId) < 10000) {
                    AppLog.i(PI_ADAPTER_LABEL, "checkProvisioningPermission, sharedUserid < 10000", new Object[0]);
                    return true;
                }
            } catch (PackageManager.NameNotFoundException unused) {
                AppLog.w(PI_ADAPTER_LABEL, "checkProvisioningPermission, shareUserId is error", new Object[0]);
                return false;
            }
        }
        return checkRestrictedPermissions(packageArchiveInfo.requestedPermissions);
    }

    private String getApkFileName(String str, String str2) {
        String str3 = "";
        if (str == null) {
            AppLog.i(PI_ADAPTER_LABEL, "getApkFileName, hapFileName is null", new Object[0]);
            return str3;
        }
        int indexOf = str.indexOf(46);
        if (indexOf > 0) {
            str3 = str.substring(0, indexOf);
        }
        return str3 + str2;
    }

    private void safeDeleteApkFile(File file) {
        if (file == null) {
            AppLog.w(PI_ADAPTER_LABEL, "safeDeleteApkFile failed, apkFile is null", new Object[0]);
            return;
        }
        try {
            AppLog.i(PI_ADAPTER_LABEL, "safeDeleteApkFile, delete: %{public}b", Boolean.valueOf(file.delete()));
        } catch (SecurityException e) {
            AppLog.w(PI_ADAPTER_LABEL, "safeDeleteApkFile failed, exception: %{public}s", e.getMessage());
        }
    }

    private void closeStreams(ZipInputStream zipInputStream, OutputStream outputStream, OutputStream outputStream2) {
        if (zipInputStream != null) {
            safeCloseStream(zipInputStream);
        }
        if (outputStream != null) {
            safeCloseStream(outputStream);
        }
        if (outputStream2 != null) {
            safeCloseStream(outputStream2);
        }
    }
}
