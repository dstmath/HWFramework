package com.android.server.backup.utils;

import android.content.Context;
import android.content.IIntentReceiver;
import android.content.IIntentSender;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.content.pm.PackageManagerInternal;
import android.content.pm.Signature;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Slog;
import com.android.internal.annotations.GuardedBy;
import com.android.server.LocalServices;
import com.android.server.backup.BackupManagerService;
import com.android.server.backup.FileMetadata;
import com.android.server.backup.restore.RestoreDeleteObserver;
import com.android.server.backup.restore.RestorePolicy;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

public class RestoreUtils {

    private static class LocalIntentReceiver {
        private IIntentSender.Stub mLocalSender;
        /* access modifiers changed from: private */
        public final Object mLock;
        /* access modifiers changed from: private */
        @GuardedBy("mLock")
        public Intent mResult;

        private LocalIntentReceiver() {
            this.mLock = new Object();
            this.mResult = null;
            this.mLocalSender = new IIntentSender.Stub() {
                public void send(int code, Intent intent, String resolvedType, IBinder whitelistToken, IIntentReceiver finishedReceiver, String requiredPermission, Bundle options) {
                    synchronized (LocalIntentReceiver.this.mLock) {
                        Intent unused = LocalIntentReceiver.this.mResult = intent;
                        LocalIntentReceiver.this.mLock.notifyAll();
                    }
                }
            };
        }

        public IntentSender getIntentSender() {
            return new IntentSender(this.mLocalSender);
        }

        public Intent getResult() {
            Intent intent;
            synchronized (this.mLock) {
                while (this.mResult == null) {
                    try {
                        this.mLock.wait();
                    } catch (InterruptedException e) {
                    }
                }
                intent = this.mResult;
            }
            return intent;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:152:0x02c9, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:153:0x02ca, code lost:
        r12 = r25;
        r14 = r26;
        r10 = r27;
        r4 = r28;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:187:?, code lost:
        $closeResource(r1, r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:188:0x037e, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x0118, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x0119, code lost:
        r12 = r25;
        r14 = r26;
        r10 = r27;
        r1 = null;
        r4 = r28;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:139:0x02a9 A[SYNTHETIC, Splitter:B:139:0x02a9] */
    /* JADX WARNING: Removed duplicated region for block: B:148:0x02bf  */
    /* JADX WARNING: Removed duplicated region for block: B:162:0x02f8 A[SYNTHETIC, Splitter:B:162:0x02f8] */
    /* JADX WARNING: Removed duplicated region for block: B:186:0x037a A[SYNTHETIC, Splitter:B:186:0x037a] */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x0118 A[ExcHandler: all (th java.lang.Throwable), Splitter:B:61:0x0114] */
    public static boolean installApk(InputStream instream, Context context, RestoreDeleteObserver deleteObserver, HashMap<String, Signature[]> manifestSignatures, HashMap<String, RestorePolicy> packagePolicies, FileMetadata info, String installerPackageName, BytesReadListener bytesReadListener) {
        PackageInstaller.Session session;
        Throwable th;
        Throwable th2;
        Throwable th3;
        Throwable th4;
        boolean okay;
        boolean okay2;
        long toRead;
        PackageInstaller.SessionParams params;
        FileMetadata fileMetadata = info;
        Slog.d(BackupManagerService.TAG, "Installing from backup: " + fileMetadata.packageName);
        try {
            LocalIntentReceiver receiver = new LocalIntentReceiver();
            PackageManager packageManager = context.getPackageManager();
            PackageInstaller installer = packageManager.getPackageInstaller();
            PackageInstaller.SessionParams params2 = new PackageInstaller.SessionParams(1);
            try {
                params2.setInstallerPackageName(installerPackageName);
                int sessionId = installer.createSession(params2);
                try {
                    PackageInstaller.Session session2 = installer.openSession(sessionId);
                    try {
                        LocalIntentReceiver localIntentReceiver = receiver;
                        try {
                            session = session2;
                            try {
                                OutputStream apkStream = session2.openWrite(fileMetadata.packageName, 0, fileMetadata.size);
                                try {
                                    byte[] buffer = new byte[32768];
                                    long size = fileMetadata.size;
                                    while (size > 0) {
                                        try {
                                            if (((long) buffer.length) < size) {
                                                try {
                                                    toRead = (long) buffer.length;
                                                } catch (Throwable th5) {
                                                    th = th5;
                                                    RestoreDeleteObserver restoreDeleteObserver = deleteObserver;
                                                    HashMap<String, Signature[]> hashMap = manifestSignatures;
                                                    HashMap<String, RestorePolicy> hashMap2 = packagePolicies;
                                                    BytesReadListener bytesReadListener2 = bytesReadListener;
                                                    FileMetadata fileMetadata2 = fileMetadata;
                                                    PackageInstaller.SessionParams sessionParams = params2;
                                                    th = null;
                                                    th3 = null;
                                                    InputStream inputStream = instream;
                                                    if (apkStream != null) {
                                                    }
                                                    throw th;
                                                }
                                            } else {
                                                toRead = size;
                                            }
                                            params = params2;
                                        } catch (Throwable th6) {
                                            th = th6;
                                            BytesReadListener bytesReadListener3 = bytesReadListener;
                                            PackageInstaller.SessionParams sessionParams2 = params2;
                                            InputStream inputStream2 = instream;
                                            RestoreDeleteObserver restoreDeleteObserver2 = deleteObserver;
                                            HashMap<String, Signature[]> hashMap3 = manifestSignatures;
                                            HashMap<String, RestorePolicy> hashMap4 = packagePolicies;
                                            FileMetadata fileMetadata3 = fileMetadata;
                                            th = null;
                                            th3 = null;
                                            if (apkStream != null) {
                                            }
                                            throw th;
                                        }
                                        try {
                                            int didRead = instream.read(buffer, 0, (int) toRead);
                                            if (didRead >= 0) {
                                                try {
                                                    bytesReadListener.onBytesRead((long) didRead);
                                                } catch (Throwable th7) {
                                                    th = th7;
                                                    RestoreDeleteObserver restoreDeleteObserver3 = deleteObserver;
                                                    HashMap<String, Signature[]> hashMap5 = manifestSignatures;
                                                    HashMap<String, RestorePolicy> hashMap6 = packagePolicies;
                                                    th = null;
                                                    FileMetadata fileMetadata4 = info;
                                                    th3 = null;
                                                    if (apkStream != null) {
                                                    }
                                                    throw th;
                                                }
                                            } else {
                                                BytesReadListener bytesReadListener4 = bytesReadListener;
                                            }
                                            apkStream.write(buffer, 0, didRead);
                                            size -= (long) didRead;
                                            params2 = params;
                                            fileMetadata = info;
                                        } catch (Throwable th8) {
                                            th = th8;
                                            BytesReadListener bytesReadListener5 = bytesReadListener;
                                            RestoreDeleteObserver restoreDeleteObserver4 = deleteObserver;
                                            HashMap<String, Signature[]> hashMap7 = manifestSignatures;
                                            HashMap<String, RestorePolicy> hashMap8 = packagePolicies;
                                            FileMetadata fileMetadata5 = fileMetadata;
                                            th = null;
                                            th3 = null;
                                            if (apkStream != null) {
                                            }
                                            throw th;
                                        }
                                    }
                                    BytesReadListener bytesReadListener6 = bytesReadListener;
                                    PackageInstaller.SessionParams sessionParams3 = params2;
                                    InputStream inputStream3 = instream;
                                    if (apkStream != null) {
                                        try {
                                            $closeResource(null, apkStream);
                                        } catch (Throwable th9) {
                                        }
                                    }
                                    session.abandon();
                                    if (session != null) {
                                        try {
                                            $closeResource(null, session);
                                        } catch (Exception e) {
                                            t = e;
                                            RestoreDeleteObserver restoreDeleteObserver5 = deleteObserver;
                                            HashMap<String, Signature[]> hashMap9 = manifestSignatures;
                                            HashMap<String, RestorePolicy> hashMap10 = packagePolicies;
                                            FileMetadata fileMetadata6 = info;
                                        } catch (IOException e2) {
                                            RestoreDeleteObserver restoreDeleteObserver6 = deleteObserver;
                                            HashMap<String, Signature[]> hashMap11 = manifestSignatures;
                                            HashMap<String, RestorePolicy> hashMap12 = packagePolicies;
                                            FileMetadata fileMetadata7 = info;
                                            Slog.e(BackupManagerService.TAG, "Unable to transcribe restored apk for install");
                                            return false;
                                        }
                                    }
                                    Intent result = null;
                                    if (1 != 0) {
                                        try {
                                            try {
                                                if (packagePolicies.get(info.packageName) != RestorePolicy.ACCEPT) {
                                                    RestoreDeleteObserver restoreDeleteObserver7 = deleteObserver;
                                                    HashMap<String, Signature[]> hashMap13 = manifestSignatures;
                                                    return false;
                                                }
                                                RestoreDeleteObserver restoreDeleteObserver8 = deleteObserver;
                                                HashMap<String, Signature[]> hashMap14 = manifestSignatures;
                                                return true;
                                            } catch (IOException e3) {
                                                RestoreDeleteObserver restoreDeleteObserver9 = deleteObserver;
                                                HashMap<String, Signature[]> hashMap15 = manifestSignatures;
                                                Slog.e(BackupManagerService.TAG, "Unable to transcribe restored apk for install");
                                                return false;
                                            }
                                        } catch (IOException e4) {
                                            HashMap<String, RestorePolicy> hashMap16 = packagePolicies;
                                            RestoreDeleteObserver restoreDeleteObserver92 = deleteObserver;
                                            HashMap<String, Signature[]> hashMap152 = manifestSignatures;
                                            Slog.e(BackupManagerService.TAG, "Unable to transcribe restored apk for install");
                                            return false;
                                        }
                                    } else {
                                        HashMap<String, RestorePolicy> hashMap17 = packagePolicies;
                                        FileMetadata fileMetadata8 = info;
                                        boolean uninstall = false;
                                        String installedPackageName = result.getStringExtra("android.content.pm.extra.PACKAGE_NAME");
                                        if (!installedPackageName.equals(fileMetadata8.packageName)) {
                                            Slog.w(BackupManagerService.TAG, "Restore stream claimed to include apk for " + fileMetadata8.packageName + " but apk was really " + installedPackageName);
                                            okay2 = false;
                                            uninstall = true;
                                            HashMap<String, Signature[]> hashMap18 = manifestSignatures;
                                            Intent intent = result;
                                        } else {
                                            try {
                                                PackageInfo pkg = packageManager.getPackageInfo(fileMetadata8.packageName, 134217728);
                                                if ((pkg.applicationInfo.flags & 32768) == 0) {
                                                    try {
                                                        Slog.w(BackupManagerService.TAG, "Restore stream contains apk of package " + fileMetadata8.packageName + " but it disallows backup/restore");
                                                        HashMap<String, Signature[]> hashMap19 = manifestSignatures;
                                                        Intent intent2 = result;
                                                        okay = false;
                                                    } catch (PackageManager.NameNotFoundException e5) {
                                                        HashMap<String, Signature[]> hashMap20 = manifestSignatures;
                                                        Intent intent3 = result;
                                                        try {
                                                            Slog.w(BackupManagerService.TAG, "Install of package " + fileMetadata8.packageName + " succeeded but now not found");
                                                            okay2 = false;
                                                            okay = okay2;
                                                            if (!uninstall) {
                                                            }
                                                            return okay;
                                                        } catch (IOException e6) {
                                                            RestoreDeleteObserver restoreDeleteObserver10 = deleteObserver;
                                                            Slog.e(BackupManagerService.TAG, "Unable to transcribe restored apk for install");
                                                            return false;
                                                        }
                                                    }
                                                    if (!uninstall) {
                                                        try {
                                                            deleteObserver.reset();
                                                            try {
                                                                packageManager.deletePackage(installedPackageName, deleteObserver, 0);
                                                                deleteObserver.waitForCompletion();
                                                            } catch (IOException e7) {
                                                            }
                                                        } catch (IOException e8) {
                                                            RestoreDeleteObserver restoreDeleteObserver11 = deleteObserver;
                                                            Slog.e(BackupManagerService.TAG, "Unable to transcribe restored apk for install");
                                                            return false;
                                                        }
                                                    } else {
                                                        RestoreDeleteObserver restoreDeleteObserver12 = deleteObserver;
                                                    }
                                                    return okay;
                                                }
                                                try {
                                                    if (AppBackupUtils.signaturesMatch(manifestSignatures.get(fileMetadata8.packageName), pkg, (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class))) {
                                                        Intent intent4 = result;
                                                        try {
                                                            if (pkg.applicationInfo.uid < 10000) {
                                                                try {
                                                                    if (pkg.applicationInfo.backupAgentName == null) {
                                                                        StringBuilder sb = new StringBuilder();
                                                                        PackageInfo packageInfo = pkg;
                                                                        sb.append("Installed app ");
                                                                        sb.append(fileMetadata8.packageName);
                                                                        sb.append(" has restricted uid and no agent");
                                                                        Slog.w(BackupManagerService.TAG, sb.toString());
                                                                        okay = false;
                                                                    }
                                                                } catch (PackageManager.NameNotFoundException e9) {
                                                                    Slog.w(BackupManagerService.TAG, "Install of package " + fileMetadata8.packageName + " succeeded but now not found");
                                                                    okay2 = false;
                                                                    okay = okay2;
                                                                    if (!uninstall) {
                                                                    }
                                                                    return okay;
                                                                }
                                                            }
                                                            okay = true;
                                                        } catch (PackageManager.NameNotFoundException e10) {
                                                            Slog.w(BackupManagerService.TAG, "Install of package " + fileMetadata8.packageName + " succeeded but now not found");
                                                            okay2 = false;
                                                            okay = okay2;
                                                            if (!uninstall) {
                                                            }
                                                            return okay;
                                                        }
                                                    } else {
                                                        PackageInfo packageInfo2 = pkg;
                                                        Intent intent5 = result;
                                                        Slog.w(BackupManagerService.TAG, "Installed app " + fileMetadata8.packageName + " signatures do not match restore manifest");
                                                        okay = false;
                                                        uninstall = true;
                                                    }
                                                } catch (PackageManager.NameNotFoundException e11) {
                                                    Intent intent6 = result;
                                                    Slog.w(BackupManagerService.TAG, "Install of package " + fileMetadata8.packageName + " succeeded but now not found");
                                                    okay2 = false;
                                                    okay = okay2;
                                                    if (!uninstall) {
                                                    }
                                                    return okay;
                                                }
                                                if (!uninstall) {
                                                }
                                                return okay;
                                            } catch (PackageManager.NameNotFoundException e12) {
                                                HashMap<String, Signature[]> hashMap21 = manifestSignatures;
                                                Intent intent62 = result;
                                                Slog.w(BackupManagerService.TAG, "Install of package " + fileMetadata8.packageName + " succeeded but now not found");
                                                okay2 = false;
                                                okay = okay2;
                                                if (!uninstall) {
                                                }
                                                return okay;
                                            } catch (IOException e13) {
                                                HashMap<String, Signature[]> hashMap22 = manifestSignatures;
                                                RestoreDeleteObserver restoreDeleteObserver102 = deleteObserver;
                                                Slog.e(BackupManagerService.TAG, "Unable to transcribe restored apk for install");
                                                return false;
                                            }
                                        }
                                        okay = okay2;
                                        if (!uninstall) {
                                        }
                                        return okay;
                                    }
                                } catch (Throwable th10) {
                                    th = th10;
                                    RestoreDeleteObserver restoreDeleteObserver13 = deleteObserver;
                                    HashMap<String, Signature[]> hashMap23 = manifestSignatures;
                                    HashMap<String, RestorePolicy> hashMap24 = packagePolicies;
                                    BytesReadListener bytesReadListener7 = bytesReadListener;
                                    FileMetadata fileMetadata9 = fileMetadata;
                                    PackageInstaller.SessionParams sessionParams4 = params2;
                                    th = null;
                                    InputStream inputStream4 = instream;
                                    th3 = null;
                                    if (apkStream != null) {
                                        try {
                                            $closeResource(th3, apkStream);
                                        } catch (Throwable th11) {
                                            th = th11;
                                            if (session != null) {
                                            }
                                            throw th;
                                        }
                                    }
                                    throw th;
                                }
                            } catch (Throwable th12) {
                                th = th12;
                                RestoreDeleteObserver restoreDeleteObserver14 = deleteObserver;
                                HashMap<String, Signature[]> hashMap25 = manifestSignatures;
                                HashMap<String, RestorePolicy> hashMap26 = packagePolicies;
                                BytesReadListener bytesReadListener8 = bytesReadListener;
                                FileMetadata fileMetadata10 = fileMetadata;
                                PackageInstaller.SessionParams sessionParams5 = params2;
                                th = null;
                                InputStream inputStream5 = instream;
                                if (session != null) {
                                }
                                throw th;
                            }
                        } catch (Throwable th13) {
                            th = th13;
                            RestoreDeleteObserver restoreDeleteObserver15 = deleteObserver;
                            HashMap<String, RestorePolicy> hashMap27 = packagePolicies;
                            BytesReadListener bytesReadListener9 = bytesReadListener;
                            FileMetadata fileMetadata11 = fileMetadata;
                            PackageInstaller.SessionParams sessionParams6 = params2;
                            session = session2;
                            th = null;
                            InputStream inputStream6 = instream;
                            HashMap<String, Signature[]> hashMap28 = manifestSignatures;
                            if (session != null) {
                            }
                            throw th;
                        }
                    } catch (Throwable th14) {
                        th = th14;
                        RestoreDeleteObserver restoreDeleteObserver16 = deleteObserver;
                        HashMap<String, RestorePolicy> hashMap29 = packagePolicies;
                        BytesReadListener bytesReadListener10 = bytesReadListener;
                        LocalIntentReceiver localIntentReceiver2 = receiver;
                        PackageInstaller.SessionParams sessionParams7 = params2;
                        session = session2;
                        InputStream inputStream7 = instream;
                        HashMap<String, Signature[]> hashMap30 = manifestSignatures;
                        FileMetadata fileMetadata12 = fileMetadata;
                        th = null;
                        if (session != null) {
                        }
                        throw th;
                    }
                } catch (Exception e14) {
                    t = e14;
                    RestoreDeleteObserver restoreDeleteObserver17 = deleteObserver;
                    HashMap<String, Signature[]> hashMap31 = manifestSignatures;
                    HashMap<String, RestorePolicy> hashMap32 = packagePolicies;
                    BytesReadListener bytesReadListener11 = bytesReadListener;
                    LocalIntentReceiver localIntentReceiver3 = receiver;
                    PackageInstaller.SessionParams sessionParams8 = params2;
                    InputStream inputStream8 = instream;
                    FileMetadata fileMetadata13 = fileMetadata;
                    try {
                        installer.abandonSession(sessionId);
                        throw t;
                    } catch (IOException e15) {
                        Slog.e(BackupManagerService.TAG, "Unable to transcribe restored apk for install");
                        return false;
                    }
                }
            } catch (IOException e16) {
                InputStream inputStream9 = instream;
                RestoreDeleteObserver restoreDeleteObserver18 = deleteObserver;
                HashMap<String, Signature[]> hashMap33 = manifestSignatures;
                HashMap<String, RestorePolicy> hashMap34 = packagePolicies;
                BytesReadListener bytesReadListener12 = bytesReadListener;
                FileMetadata fileMetadata14 = fileMetadata;
                Slog.e(BackupManagerService.TAG, "Unable to transcribe restored apk for install");
                return false;
            }
        } catch (IOException e17) {
            InputStream inputStream10 = instream;
            RestoreDeleteObserver restoreDeleteObserver19 = deleteObserver;
            HashMap<String, Signature[]> hashMap35 = manifestSignatures;
            HashMap<String, RestorePolicy> hashMap36 = packagePolicies;
            String str = installerPackageName;
            BytesReadListener bytesReadListener122 = bytesReadListener;
            FileMetadata fileMetadata142 = fileMetadata;
            Slog.e(BackupManagerService.TAG, "Unable to transcribe restored apk for install");
            return false;
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
}
