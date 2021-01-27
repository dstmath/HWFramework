package com.android.server.backup.utils;

import android.content.Context;
import android.content.IIntentReceiver;
import android.content.IIntentSender;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.content.pm.PackageManagerInternal;
import android.content.pm.Signature;
import android.os.Bundle;
import android.os.IBinder;
import android.os.UserHandle;
import android.util.Slog;
import com.android.internal.annotations.GuardedBy;
import com.android.server.LocalServices;
import com.android.server.backup.BackupManagerService;
import com.android.server.backup.FileMetadata;
import com.android.server.backup.restore.RestoreDeleteObserver;
import com.android.server.backup.restore.RestorePolicy;
import com.android.server.pm.DumpState;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

public class RestoreUtils {
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:120:0x0239 A[ExcHandler: IOException (e java.io.IOException), Splitter:B:83:0x0179] */
    /* JADX WARNING: Removed duplicated region for block: B:130:0x026f A[SYNTHETIC, Splitter:B:130:0x026f] */
    /* JADX WARNING: Removed duplicated region for block: B:139:0x028c  */
    public static boolean installApk(InputStream instream, Context context, RestoreDeleteObserver deleteObserver, HashMap<String, Signature[]> manifestSignatures, HashMap<String, RestorePolicy> packagePolicies, FileMetadata info, String installerPackageName, BytesReadListener bytesReadListener, int userId) {
        Exception t;
        Throwable th;
        PackageInstaller.Session session;
        Throwable th2;
        PackageManager packageManager;
        boolean okay;
        boolean okay2;
        long toRead;
        FileMetadata fileMetadata = info;
        boolean okay3 = true;
        Slog.d(BackupManagerService.TAG, "Installing from backup: " + fileMetadata.packageName);
        try {
            new LocalIntentReceiver();
            PackageManager packageManager2 = context.getPackageManager();
            PackageInstaller installer = packageManager2.getPackageInstaller();
            PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(1);
            try {
                params.setInstallerPackageName(installerPackageName);
                int sessionId = installer.createSession(params);
                try {
                    PackageInstaller.Session session2 = installer.openSession(sessionId);
                    try {
                        try {
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
                                                } catch (Throwable th3) {
                                                    th2 = th3;
                                                    session = session2;
                                                    try {
                                                        throw th2;
                                                    } catch (Throwable th4) {
                                                        th = th4;
                                                        try {
                                                            throw th;
                                                        } catch (Exception e) {
                                                            t = e;
                                                            try {
                                                                installer.abandonSession(sessionId);
                                                                throw t;
                                                            } catch (IOException e2) {
                                                                Slog.e(BackupManagerService.TAG, "Unable to transcribe restored apk for install");
                                                                return false;
                                                            }
                                                        } catch (Throwable th5) {
                                                            if (session != null) {
                                                                $closeResource(th, session);
                                                            }
                                                            throw th5;
                                                        }
                                                    }
                                                }
                                            } else {
                                                toRead = size;
                                            }
                                        } catch (Throwable th6) {
                                            th2 = th6;
                                            session = session2;
                                            throw th2;
                                        }
                                        try {
                                            int didRead = instream.read(buffer, 0, (int) toRead);
                                            if (didRead >= 0) {
                                                try {
                                                    bytesReadListener.onBytesRead((long) didRead);
                                                } catch (Throwable th7) {
                                                    th2 = th7;
                                                    session = session2;
                                                    throw th2;
                                                }
                                            }
                                            apkStream.write(buffer, 0, didRead);
                                            size -= (long) didRead;
                                            fileMetadata = info;
                                            packageManager2 = packageManager2;
                                            okay3 = okay3;
                                        } catch (Throwable th8) {
                                            th2 = th8;
                                            session = session2;
                                            throw th2;
                                        }
                                    }
                                    if (apkStream != null) {
                                        try {
                                            $closeResource(null, apkStream);
                                        } catch (Throwable th9) {
                                            th = th9;
                                            session = session2;
                                        }
                                    }
                                    try {
                                        session2.abandon();
                                        try {
                                            $closeResource(null, session2);
                                            Intent result = null;
                                            if (1 != 0) {
                                                try {
                                                    try {
                                                        if (packagePolicies.get(info.packageName) != RestorePolicy.ACCEPT) {
                                                            return false;
                                                        }
                                                        return okay3;
                                                    } catch (IOException e3) {
                                                        Slog.e(BackupManagerService.TAG, "Unable to transcribe restored apk for install");
                                                        return false;
                                                    }
                                                } catch (IOException e4) {
                                                    Slog.e(BackupManagerService.TAG, "Unable to transcribe restored apk for install");
                                                    return false;
                                                }
                                            } else {
                                                boolean uninstall = false;
                                                try {
                                                    String installedPackageName = result.getStringExtra("android.content.pm.extra.PACKAGE_NAME");
                                                    if (!installedPackageName.equals(info.packageName)) {
                                                        Slog.w(BackupManagerService.TAG, "Restore stream claimed to include apk for " + info.packageName + " but apk was really " + installedPackageName);
                                                        uninstall = true;
                                                        packageManager = packageManager2;
                                                        okay = false;
                                                    } else {
                                                        try {
                                                            packageManager = packageManager2;
                                                            try {
                                                                PackageInfo pkg = packageManager.getPackageInfoAsUser(info.packageName, DumpState.DUMP_HWFEATURES, userId);
                                                                if ((pkg.applicationInfo.flags & 32768) == 0) {
                                                                    StringBuilder sb = new StringBuilder();
                                                                    sb.append("Restore stream contains apk of package ");
                                                                    sb.append(info.packageName);
                                                                    sb.append(" but it disallows backup/restore");
                                                                    Slog.w(BackupManagerService.TAG, sb.toString());
                                                                    okay = false;
                                                                } else {
                                                                    try {
                                                                        try {
                                                                            if (!AppBackupUtils.signaturesMatch(manifestSignatures.get(info.packageName), pkg, (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class))) {
                                                                                Slog.w(BackupManagerService.TAG, "Installed app " + info.packageName + " signatures do not match restore manifest");
                                                                                okay = false;
                                                                                uninstall = true;
                                                                            } else if (!UserHandle.isCore(((ApplicationInfo) pkg.applicationInfo).uid) || pkg.applicationInfo.backupAgentName != null) {
                                                                                okay = okay3;
                                                                            } else {
                                                                                Slog.w(BackupManagerService.TAG, "Installed app " + info.packageName + " has restricted uid and no agent");
                                                                                okay = false;
                                                                            }
                                                                        } catch (PackageManager.NameNotFoundException e5) {
                                                                            try {
                                                                                Slog.w(BackupManagerService.TAG, "Install of package " + info.packageName + " succeeded but now not found");
                                                                                okay = false;
                                                                                if (!uninstall) {
                                                                                }
                                                                                return okay2;
                                                                            } catch (IOException e6) {
                                                                                Slog.e(BackupManagerService.TAG, "Unable to transcribe restored apk for install");
                                                                                return false;
                                                                            }
                                                                        }
                                                                    } catch (PackageManager.NameNotFoundException e7) {
                                                                        Slog.w(BackupManagerService.TAG, "Install of package " + info.packageName + " succeeded but now not found");
                                                                        okay = false;
                                                                        if (!uninstall) {
                                                                        }
                                                                        return okay2;
                                                                    }
                                                                }
                                                            } catch (PackageManager.NameNotFoundException e8) {
                                                                Slog.w(BackupManagerService.TAG, "Install of package " + info.packageName + " succeeded but now not found");
                                                                okay = false;
                                                                if (!uninstall) {
                                                                }
                                                                return okay2;
                                                            } catch (IOException e9) {
                                                            }
                                                        } catch (PackageManager.NameNotFoundException e10) {
                                                            packageManager = packageManager2;
                                                            Slog.w(BackupManagerService.TAG, "Install of package " + info.packageName + " succeeded but now not found");
                                                            okay = false;
                                                            if (!uninstall) {
                                                            }
                                                            return okay2;
                                                        }
                                                    }
                                                    if (!uninstall) {
                                                        try {
                                                            deleteObserver.reset();
                                                            okay2 = okay;
                                                            try {
                                                                packageManager.deletePackage(installedPackageName, deleteObserver, 0);
                                                                deleteObserver.waitForCompletion();
                                                            } catch (IOException e11) {
                                                            }
                                                        } catch (IOException e12) {
                                                            Slog.e(BackupManagerService.TAG, "Unable to transcribe restored apk for install");
                                                            return false;
                                                        }
                                                    } else {
                                                        okay2 = okay;
                                                    }
                                                    return okay2;
                                                } catch (IOException e13) {
                                                    Slog.e(BackupManagerService.TAG, "Unable to transcribe restored apk for install");
                                                    return false;
                                                }
                                            }
                                        } catch (Exception e14) {
                                            t = e14;
                                            installer.abandonSession(sessionId);
                                            throw t;
                                        } catch (IOException e15) {
                                            Slog.e(BackupManagerService.TAG, "Unable to transcribe restored apk for install");
                                            return false;
                                        }
                                    } catch (Throwable th10) {
                                        session = session2;
                                        th = th10;
                                        throw th;
                                    }
                                } catch (Throwable th11) {
                                    session = session2;
                                    th2 = th11;
                                    throw th2;
                                }
                            } catch (Throwable th12) {
                                session = session2;
                                th = th12;
                                throw th;
                            }
                        } catch (Throwable th13) {
                            session = session2;
                            th = th13;
                            throw th;
                        }
                    } catch (Throwable th14) {
                        session = session2;
                        th = th14;
                        throw th;
                    }
                } catch (Exception e16) {
                    t = e16;
                    installer.abandonSession(sessionId);
                    throw t;
                }
            } catch (IOException e17) {
                Slog.e(BackupManagerService.TAG, "Unable to transcribe restored apk for install");
                return false;
            }
        } catch (IOException e18) {
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

    /* access modifiers changed from: private */
    public static class LocalIntentReceiver {
        private IIntentSender.Stub mLocalSender;
        private final Object mLock;
        @GuardedBy({"mLock"})
        private Intent mResult;

        private LocalIntentReceiver() {
            this.mLock = new Object();
            this.mResult = null;
            this.mLocalSender = new IIntentSender.Stub() {
                /* class com.android.server.backup.utils.RestoreUtils.LocalIntentReceiver.AnonymousClass1 */

                public void send(int code, Intent intent, String resolvedType, IBinder whitelistToken, IIntentReceiver finishedReceiver, String requiredPermission, Bundle options) {
                    synchronized (LocalIntentReceiver.this.mLock) {
                        LocalIntentReceiver.this.mResult = intent;
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
}
