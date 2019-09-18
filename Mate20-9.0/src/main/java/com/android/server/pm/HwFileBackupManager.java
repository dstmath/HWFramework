package com.android.server.pm;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageParser;
import android.os.Binder;
import android.os.IBackupSessionCallback;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManagerInternal;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import com.android.server.LocalServices;
import com.android.server.pm.Installer;
import java.util.ArrayList;
import java.util.List;

public final class HwFileBackupManager {
    public static final int BACKUP_TASK_CMD_ARG_MAX = 6;
    public static final int BACKUP_TASK_FAILED = -1;
    public static final int BACKUP_TASK_NO_PERMISSION = -2;
    public static final int BACKUP_TASK_SUCCESS = 0;
    public static final int BACKUP_TASK_UNSUPPORTED_CMD = -3;
    private static final String[] PACKAGE_NAMES_FILE_BACKUP = {"com.hicloud.android.clone", "com.huawei.intelligent", "com.huawei.KoBackup", "com.huawei.hidisk"};
    private static final String TAG = "HwFileBackupManager_BackupSession";
    private static final int VERSION_CODE = 1;
    private static volatile HwFileBackupManager mInstance;
    private final ArrayList<BackupDeathHandler> mBackupDeathHandlers = new ArrayList<>();
    private final Installer mInstaller;
    private NativeBackupCallback mNativeBackupCallback = new NativeBackupCallback();
    private final SparseArray<IBackupSessionCallback> mSessions = new SparseArray<>();
    private UserManagerInternal mUserManagerInternal;

    private class BackupDeathHandler implements IBinder.DeathRecipient {
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

    private final class NativeBackupCallback extends IBackupSessionCallback.Stub {
        private NativeBackupCallback() {
        }

        public void onTaskStatusChanged(int sessionId, int taskId, int statusCode, String appendData) {
            HwFileBackupManager.this.handleNativeBackupSessionCallback(sessionId, taskId, statusCode, appendData);
        }
    }

    private static class PathData {
        private static final String DATA_DATA_PATH = "/data/data/";
        private static final String MULTI_USER_PATH = "/data/user/";
        public boolean isAppDataPath = true;
        public boolean isMultiUserPath;
        public String packageName;
        public String path;
        public int userId = -10000;

        private PathData() {
        }

        public static PathData create(String path2) {
            if (TextUtils.isEmpty(path2)) {
                return null;
            }
            PathData instance = new PathData();
            instance.path = path2;
            if (path2.startsWith(MULTI_USER_PATH)) {
                instance.isMultiUserPath = true;
            } else if (path2.startsWith(DATA_DATA_PATH)) {
                instance.isMultiUserPath = false;
            } else {
                instance.isAppDataPath = false;
            }
            instance.parsePath();
            return instance;
        }

        private void parsePath() {
            if (!this.isAppDataPath) {
                Slog.d(HwFileBackupManager.TAG, this.path + " is not a app data path,no need parse!");
                return;
            }
            if (this.isMultiUserPath) {
                parseMultiUserPath();
            } else {
                parseDefaultDataPath();
            }
        }

        private void parseMultiUserPath() {
            int startIndex = MULTI_USER_PATH.length();
            if (startIndex >= this.path.length()) {
                Slog.e(HwFileBackupManager.TAG, this.path + " does not contain userId!");
                return;
            }
            int userIdLocation = this.path.indexOf("/", startIndex);
            if (-1 == userIdLocation) {
                Slog.e(HwFileBackupManager.TAG, this.path + " does not contain userId or package name!");
                return;
            }
            String userIdStr = this.path.substring(startIndex, userIdLocation);
            if (!TextUtils.isEmpty(userIdStr)) {
                try {
                    this.userId = Integer.parseInt(userIdStr);
                } catch (NumberFormatException e) {
                    Slog.e(HwFileBackupManager.TAG, this.path + " does not contain correct userId, find:" + userIdStr);
                    return;
                }
            }
            this.packageName = getPackageName(this.path, userIdLocation + 1);
        }

        private void parseDefaultDataPath() {
            this.packageName = getPackageName(this.path, DATA_DATA_PATH.length());
        }

        private String getPackageName(String path2, int startIndex) {
            String pkgName;
            if (TextUtils.isEmpty(path2) || startIndex >= path2.length()) {
                Slog.e(HwFileBackupManager.TAG, path2 + " does not contain package name!");
                return null;
            }
            int endIndex = path2.indexOf("/", startIndex);
            if (endIndex == -1) {
                pkgName = path2.substring(startIndex).trim();
            } else {
                pkgName = path2.substring(startIndex, endIndex).trim();
            }
            Slog.d(HwFileBackupManager.TAG, pkgName + " found in " + path2);
            return pkgName;
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

    /* access modifiers changed from: private */
    public void handleNativeBackupSessionCallback(int sessionId, int taskId, int statusCode, String appendData) {
        synchronized (this.mSessions) {
            IBackupSessionCallback callback = this.mSessions.get(sessionId);
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
        } catch (Installer.InstallerException e2) {
            Slog.w(TAG, "Trouble startBackupSession", e2);
            return -1;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x004b, code lost:
        if (android.text.TextUtils.isEmpty(r7) == false) goto L_0x004f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x004e, code lost:
        return -3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0051, code lost:
        if (r5.mInstaller != null) goto L_0x005b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0053, code lost:
        android.util.Slog.e(TAG, "installer is null!");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x005a, code lost:
        return -1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0061, code lost:
        return r5.mInstaller.executeBackupTask(r6, r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0062, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0063, code lost:
        android.util.Slog.w(TAG, "Trouble executeBackupTask", r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x006a, code lost:
        return -1;
     */
    public int executeBackupTask(int sessionId, String taskCmd) {
        Slog.i(TAG, "bind call executeBackupTask on session:" + sessionId);
        if (sessionId == -1 && "getVersionCode".equalsIgnoreCase(taskCmd)) {
            return 1;
        }
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
                    BackupDeathHandler hdlr = this.mBackupDeathHandlers.get(i);
                    if (hdlr.getSessionId() == sessionId) {
                        this.mBackupDeathHandlers.remove(i);
                        try {
                            hdlr.getBinder().unlinkToDeath(hdlr, 0);
                        } catch (Exception e) {
                            Slog.e(TAG, "finishBackupSession, ignore exceptions, the handler is being removed");
                        }
                    }
                }
            }
            return result;
        } catch (Installer.InstallerException e2) {
            Slog.w(TAG, "Trouble finishBackupSession", e2);
        }
    }

    public boolean checkBackupPackageName(String pkgName) {
        boolean result = false;
        String[] strArr = PACKAGE_NAMES_FILE_BACKUP;
        int length = strArr.length;
        int i = 0;
        while (true) {
            if (i >= length) {
                break;
            } else if (TextUtils.equals(pkgName, strArr[i])) {
                result = true;
                break;
            } else {
                i++;
            }
        }
        if (!result) {
            Slog.d(TAG, "BackupSession checkBackupPackageName failed, pkgName is " + pkgName);
        }
        return result;
    }

    private String normalizeTaskCmd(String taskCmd, List<String> cmdInfo) {
        String normalizedTaskCmd;
        String[] args = taskCmd.split(" ");
        StringBuilder sbTaskCmd = new StringBuilder();
        for (String cmd : args) {
            String arg = cmd.replace(" ", "");
            if (!TextUtils.isEmpty(arg)) {
                if (cmdInfo != null) {
                    cmdInfo.add(arg);
                }
                sbTaskCmd.append(arg);
                sbTaskCmd.append(" ");
            }
        }
        Slog.d(TAG, "BackupSession prepareBackupTaskCmd,after normalize is:" + normalizedTaskCmd);
        return normalizedTaskCmd;
    }

    public String prepareBackupTaskCmd(String taskCmd, ArrayMap<String, PackageParser.Package> packages) {
        if (TextUtils.isEmpty(taskCmd)) {
            return null;
        }
        List<String> cmdInfo = new ArrayList<>(6);
        String normalizedTaskCmd = normalizeTaskCmd(taskCmd, cmdInfo);
        if (cmdInfo.size() > 6) {
            return null;
        }
        if (cmdInfo.size() < 4) {
            return normalizedTaskCmd;
        }
        String srcPath = cmdInfo.get(2);
        if (!isUsableSrcPath(PathData.create(srcPath))) {
            Slog.e(TAG, srcPath + " is not a supported src data path!");
            return null;
        }
        String destPath = cmdInfo.get(3);
        PathData destPathData = PathData.create(destPath);
        if (!isUsableDestPath(destPathData)) {
            Slog.e(TAG, destPath + " is not a supported dest path!");
            return null;
        }
        synchronized (packages) {
            PackageParser.Package pkg = packages.get(destPathData.packageName);
            if (pkg == null || pkg.applicationInfo == null) {
                Slog.d(TAG, "BackupSession prepareBackupTaskCmd, target path must begin with aexisting app's data directory since we need get seinfo for task cmd!");
                return null;
            }
            ApplicationInfo app = pkg.applicationInfo;
            StringBuilder sb = new StringBuilder(normalizedTaskCmd);
            sb.append(" ");
            sb.append(app.seInfo);
            sb.append(" ");
            if (destPathData.isMultiUserPath) {
                sb.append(UserHandle.getUid(destPathData.userId, app.uid));
            } else {
                sb.append(app.uid);
            }
            String sb2 = sb.toString();
            return sb2;
        }
    }

    private UserManagerInternal getUserManagerInternal() {
        if (this.mUserManagerInternal == null) {
            this.mUserManagerInternal = (UserManagerInternal) LocalServices.getService(UserManagerInternal.class);
        }
        return this.mUserManagerInternal;
    }

    private boolean isUsableSrcPath(PathData pathData) {
        if (pathData == null) {
            return false;
        }
        if (!pathData.isAppDataPath) {
            return true;
        }
        return isUsableAppDataPath(pathData);
    }

    private boolean isUsableDestPath(PathData pathData) {
        if (pathData != null && !TextUtils.isEmpty(pathData.packageName)) {
            return isUsableAppDataPath(pathData);
        }
        Slog.e(TAG, "dest path does not contain package name, check package name is null!");
        return false;
    }

    private boolean isUsableAppDataPath(PathData pathData) {
        if (pathData == null || !pathData.isAppDataPath) {
            return false;
        }
        if (!pathData.isMultiUserPath || pathData.userId == 0) {
            return true;
        }
        if (HwPackageManagerServiceEx.isSupportCloneAppInCust(pathData.packageName) && getUserManagerInternal().isClonedProfile(pathData.userId)) {
            return true;
        }
        Slog.e(TAG, pathData.path + " is not a support clone app data path!");
        return false;
    }
}
