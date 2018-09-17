package com.android.server.pm;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageParser.Package;
import android.os.Binder;
import android.os.IBackupSessionCallback;
import android.os.IBackupSessionCallback.Stub;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import com.android.server.pm.Installer.InstallerException;
import java.util.ArrayList;
import java.util.List;

public final class HwFileBackupManager {
    public static final int BACKUP_TASK_CMD_ARG_MAX = 6;
    public static final int BACKUP_TASK_FAILED = -1;
    public static final int BACKUP_TASK_NO_PERMISSION = -2;
    public static final int BACKUP_TASK_SUCCESS = 0;
    public static final int BACKUP_TASK_UNSUPPORTED_CMD = -3;
    private static final String[] PACKAGE_NAMES_FILE_BACKUP = new String[]{"com.hicloud.android.clone", "com.huawei.intelligent", "com.huawei.KoBackup", "com.huawei.hidisk"};
    private static final String TAG = "HwFileBackupManager_BackupSession";
    private static volatile HwFileBackupManager mInstance;
    private final ArrayList<BackupDeathHandler> mBackupDeathHandlers = new ArrayList();
    private final Installer mInstaller;
    private NativeBackupCallback mNativeBackupCallback = new NativeBackupCallback(this, null);
    private final SparseArray<IBackupSessionCallback> mSessions = new SparseArray();

    private class BackupDeathHandler implements DeathRecipient {
        public IBinder mCb;
        public int mSessionId;

        BackupDeathHandler(int sessionId, IBinder cb) {
            this.mSessionId = sessionId;
            this.mCb = cb;
        }

        public void binderDied() {
            Log.w(HwFileBackupManager.TAG, "backup client with sessionId " + this.mSessionId + " died");
            long ident = Binder.clearCallingIdentity();
            HwFileBackupManager.this.finishBackupSession(this.mSessionId);
            Binder.restoreCallingIdentity(ident);
        }

        public IBinder getBinder() {
            return this.mCb;
        }

        public int getSessionId() {
            return this.mSessionId;
        }
    }

    private final class NativeBackupCallback extends Stub {
        /* synthetic */ NativeBackupCallback(HwFileBackupManager this$0, NativeBackupCallback -this1) {
            this();
        }

        private NativeBackupCallback() {
        }

        public void onTaskStatusChanged(int sessionId, int taskId, int statusCode, String appendData) {
            HwFileBackupManager.this.handleNativeBackupSessionCallback(sessionId, taskId, statusCode, appendData);
        }
    }

    private HwFileBackupManager(Installer installer) {
        this.mInstaller = installer;
    }

    public static HwFileBackupManager getInstance(Installer installer) {
        if (mInstance == null) {
            synchronized (HwFileBackupManager.class) {
                if (mInstance == null) {
                    mInstance = new HwFileBackupManager(installer);
                }
            }
        }
        return mInstance;
    }

    private void handleNativeBackupSessionCallback(int sessionId, int taskId, int statusCode, String appendData) {
        synchronized (this.mSessions) {
            IBackupSessionCallback callback = (IBackupSessionCallback) this.mSessions.get(sessionId);
            if (callback == null) {
                Log.e(TAG, "no callback set for session:" + sessionId);
                return;
            }
            try {
                callback.onTaskStatusChanged(sessionId, taskId, statusCode, appendData);
            } catch (RemoteException e) {
                Log.w(TAG, "callback binder death!");
            }
        }
    }

    public int startBackupSession(IBackupSessionCallback callback) {
        Slog.i(TAG, "application bind call startBackupSession");
        try {
            if (this.mInstaller == null) {
                Slog.e(TAG, "installer is null!");
                return -1;
            }
            int sessionId = this.mInstaller.startBackupSession(this.mNativeBackupCallback);
            Slog.i(TAG, "application startBackupSession sessionid:" + sessionId);
            if (sessionId < 0) {
                return -1;
            }
            synchronized (this.mBackupDeathHandlers) {
                BackupDeathHandler hdlr = new BackupDeathHandler(sessionId, callback.asBinder());
                try {
                    callback.asBinder().linkToDeath(hdlr, 0);
                    this.mBackupDeathHandlers.add(hdlr);
                } catch (RemoteException e) {
                    Log.w(TAG, "startBackupSession() could not link to " + callback.asBinder() + " binder death");
                    return -1;
                }
            }
            synchronized (this.mSessions) {
                this.mSessions.put(sessionId, callback);
            }
            return sessionId;
        } catch (InstallerException e2) {
            Slog.w(TAG, "Trouble startBackupSession", e2);
            return -1;
        }
    }

    /* JADX WARNING: Missing block: B:11:0x0047, code:
            if (android.text.TextUtils.isEmpty(r8) == false) goto L_0x004e;
     */
    /* JADX WARNING: Missing block: B:13:0x004a, code:
            return -3;
     */
    /* JADX WARNING: Missing block: B:19:0x0050, code:
            if (r6.mInstaller != null) goto L_0x005c;
     */
    /* JADX WARNING: Missing block: B:20:0x0052, code:
            android.util.Slog.e(TAG, "installer is null!");
     */
    /* JADX WARNING: Missing block: B:21:0x005b, code:
            return -1;
     */
    /* JADX WARNING: Missing block: B:23:0x0062, code:
            return r6.mInstaller.executeBackupTask(r7, r8);
     */
    /* JADX WARNING: Missing block: B:24:0x0063, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:25:0x0064, code:
            android.util.Slog.w(TAG, "Trouble executeBackupTask", r0);
     */
    /* JADX WARNING: Missing block: B:26:0x006d, code:
            return -1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int executeBackupTask(int sessionId, String taskCmd) {
        Slog.i(TAG, "bind call executeBackupTask on session:" + sessionId);
        synchronized (this.mSessions) {
            if (this.mSessions.indexOfKey(sessionId) < 0) {
                Slog.e(TAG, "no session with id=" + sessionId);
                return -1;
            }
        }
    }

    public int finishBackupSession(int sessionId) {
        Slog.i(TAG, "bind call finishBackupSession sessionId:" + sessionId);
        int result = -1;
        try {
            if (this.mInstaller == null) {
                Slog.e(TAG, "installer is null!");
                return -1;
            }
            result = this.mInstaller.finishBackupSession(sessionId);
            synchronized (this.mSessions) {
                this.mSessions.remove(sessionId);
            }
            synchronized (this.mBackupDeathHandlers) {
                for (int i = this.mBackupDeathHandlers.size() - 1; i >= 0; i--) {
                    BackupDeathHandler hdlr = (BackupDeathHandler) this.mBackupDeathHandlers.get(i);
                    if (hdlr.getSessionId() == sessionId) {
                        this.mBackupDeathHandlers.remove(i);
                        try {
                            hdlr.getBinder().unlinkToDeath(hdlr, 0);
                        } catch (Exception e) {
                        }
                    }
                }
            }
            return result;
        } catch (InstallerException e2) {
            Slog.w(TAG, "Trouble finishBackupSession", e2);
        }
    }

    public boolean checkBackupPackageName(String pkgName) {
        boolean result = false;
        for (CharSequence equals : PACKAGE_NAMES_FILE_BACKUP) {
            if (TextUtils.equals(pkgName, equals)) {
                result = true;
                break;
            }
        }
        if (!result) {
            Slog.d(TAG, "BackupSession checkBackupPackageName failed, pkgName is " + pkgName);
        }
        return result;
    }

    private String normalizeTaskCmd(String taskCmd, List<String> cmdInfo) {
        String[] args = taskCmd.split(" ");
        StringBuilder sbTaskCmd = new StringBuilder();
        for (String replace : args) {
            String arg = replace.replace(" ", "");
            if (!TextUtils.isEmpty(arg)) {
                if (cmdInfo != null) {
                    cmdInfo.add(arg);
                }
                sbTaskCmd.append(arg);
                sbTaskCmd.append(" ");
            }
        }
        String normalizedTaskCmd = sbTaskCmd.toString().trim();
        Slog.d(TAG, "BackupSession prepareBackupTaskCmd,after normalize is:" + normalizedTaskCmd);
        return normalizedTaskCmd;
    }

    private String getPackageNameForTaskCmd(String taskCmd, List<String> cmdInfo) {
        if (cmdInfo == null || cmdInfo.size() < 4) {
            return null;
        }
        String destPath = (String) cmdInfo.get(3);
        String dataPath = "/data/data/";
        if (!destPath.startsWith(dataPath)) {
            return null;
        }
        int startIndex = dataPath.length();
        if (startIndex == destPath.length()) {
            return null;
        }
        String pkgName = null;
        if (taskCmd.startsWith("restore")) {
            pkgName = destPath.substring(startIndex);
        } else if (taskCmd.startsWith("backup") || taskCmd.startsWith("move")) {
            int endIndex = destPath.indexOf("/", startIndex);
            if (endIndex == -1) {
                pkgName = destPath.substring(startIndex).trim();
            } else {
                pkgName = destPath.substring(startIndex, endIndex).trim();
            }
        }
        return pkgName;
    }

    public String prepareBackupTaskCmd(String taskCmd, ArrayMap<String, Package> packages) {
        if (TextUtils.isEmpty(taskCmd)) {
            return null;
        }
        List<String> cmdInfo = new ArrayList(6);
        String normalizedTaskCmd = normalizeTaskCmd(taskCmd, cmdInfo);
        if (cmdInfo.size() > 6) {
            return null;
        }
        if (cmdInfo.size() < 4) {
            return normalizedTaskCmd;
        }
        String pkgName = getPackageNameForTaskCmd(normalizedTaskCmd, cmdInfo);
        Slog.d(TAG, "BackupSession prepareBackupTaskCmd, pkgName is " + pkgName);
        if (TextUtils.isEmpty(pkgName)) {
            return null;
        }
        synchronized (packages) {
            Package pkg = (Package) packages.get(pkgName);
            if (pkg == null || pkg.applicationInfo == null) {
                Slog.d(TAG, "BackupSession prepareBackupTaskCmd, target path must begin with a existing app's data directory since we need get seinfo for task cmd!");
                return null;
            }
            ApplicationInfo app = pkg.applicationInfo;
            StringBuilder sb = new StringBuilder(normalizedTaskCmd);
            sb.append(" ");
            sb.append(app.seInfo);
            sb.append(" ");
            sb.append(app.uid);
            String stringBuilder = sb.toString();
            return stringBuilder;
        }
    }
}
