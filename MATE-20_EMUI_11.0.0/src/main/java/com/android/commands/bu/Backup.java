package com.android.commands.bu;

import android.app.backup.IBackupManager;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.system.OsConstants;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import java.io.IOException;
import java.util.ArrayList;

public final class Backup {
    static final String TAG = "bu";
    static String[] mArgs;
    IBackupManager mBackupManager;
    int mNextArg;

    @VisibleForTesting
    Backup(IBackupManager backupManager) {
        this.mBackupManager = backupManager;
    }

    Backup() {
        this.mBackupManager = IBackupManager.Stub.asInterface(ServiceManager.getService("backup"));
    }

    public static void main(String[] args) {
        try {
            new Backup().run(args);
        } catch (Exception e) {
            Log.e(TAG, "Error running backup/restore", e);
        }
        Log.d(TAG, "Finished.");
    }

    public void run(String[] args) {
        if (this.mBackupManager == null) {
            Log.e(TAG, "Can't obtain Backup Manager binder");
            return;
        }
        Log.d(TAG, "Beginning: " + args[0]);
        mArgs = args;
        int userId = parseUserId();
        if (!isBackupActiveForUser(userId)) {
            Log.e(TAG, "BackupManager is not available for user " + userId);
            return;
        }
        String arg = nextArg();
        if (arg.equals("backup")) {
            doBackup(OsConstants.STDOUT_FILENO, userId);
        } else if (arg.equals("restore")) {
            doRestore(OsConstants.STDIN_FILENO, userId);
        } else {
            showUsage();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:103:0x01b4 A[SYNTHETIC, Splitter:B:103:0x01b4] */
    /* JADX WARNING: Removed duplicated region for block: B:95:0x018d A[SYNTHETIC, Splitter:B:95:0x018d] */
    private void doBackup(int socketFd, int userId) {
        ParcelFileDescriptor fd;
        Throwable th;
        String str;
        ArrayList<String> packages = new ArrayList<>();
        boolean saveShared = false;
        boolean doEverything = false;
        boolean doKeyValue = false;
        boolean doCompress = true;
        boolean allIncludesSystem = true;
        boolean doWidgets = false;
        boolean saveObbs = false;
        boolean saveApks = false;
        while (true) {
            String arg = nextArg();
            if (arg == null) {
                break;
            } else if (!arg.startsWith("-")) {
                packages.add(arg);
            } else if ("-apk".equals(arg)) {
                saveApks = true;
            } else if ("-noapk".equals(arg)) {
                saveApks = false;
            } else if ("-obb".equals(arg)) {
                saveObbs = true;
            } else if ("-noobb".equals(arg)) {
                saveObbs = false;
            } else if ("-shared".equals(arg)) {
                saveShared = true;
            } else if ("-noshared".equals(arg)) {
                saveShared = false;
            } else if ("-system".equals(arg)) {
                allIncludesSystem = true;
            } else if ("-nosystem".equals(arg)) {
                allIncludesSystem = false;
            } else if ("-widgets".equals(arg)) {
                doWidgets = true;
            } else if ("-nowidgets".equals(arg)) {
                doWidgets = false;
            } else if ("-all".equals(arg)) {
                doEverything = true;
            } else if ("-compress".equals(arg)) {
                doCompress = true;
            } else if ("-nocompress".equals(arg)) {
                doCompress = false;
            } else if ("-keyvalue".equals(arg)) {
                doKeyValue = true;
            } else if ("-nokeyvalue".equals(arg)) {
                doKeyValue = false;
            } else if ("-user".equals(arg)) {
                nextArg();
            } else {
                Log.w(TAG, "Unknown backup flag " + arg);
            }
        }
        if (doEverything && packages.size() > 0) {
            Log.w(TAG, "-all passed for backup along with specific package names");
        }
        if (doEverything || saveShared || packages.size() != 0) {
            ParcelFileDescriptor fd2 = null;
            try {
                fd = ParcelFileDescriptor.adoptFd(socketFd);
                try {
                    IBackupManager iBackupManager = this.mBackupManager;
                    String[] strArr = (String[]) packages.toArray(new String[packages.size()]);
                    str = TAG;
                    try {
                        iBackupManager.adbBackup(userId, fd, saveApks, saveObbs, saveShared, doWidgets, doEverything, allIncludesSystem, doCompress, doKeyValue, strArr);
                        if (fd != null) {
                            try {
                                fd.close();
                            } catch (IOException e) {
                                Log.e(str, "IO error closing output for backup: " + e.getMessage());
                            }
                        }
                    } catch (RemoteException e2) {
                        fd2 = fd;
                        try {
                            Log.e(str, "Unable to invoke backup manager for backup");
                            if (fd2 != null) {
                            }
                        } catch (Throwable th2) {
                            fd = fd2;
                            th = th2;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        if (fd != null) {
                        }
                        throw th;
                    }
                } catch (RemoteException e3) {
                    str = TAG;
                    fd2 = fd;
                    Log.e(str, "Unable to invoke backup manager for backup");
                    if (fd2 != null) {
                        try {
                            fd2.close();
                        } catch (IOException e4) {
                            Log.e(str, "IO error closing output for backup: " + e4.getMessage());
                        }
                    }
                } catch (Throwable th4) {
                    str = TAG;
                    th = th4;
                    if (fd != null) {
                        try {
                            fd.close();
                        } catch (IOException e5) {
                            Log.e(str, "IO error closing output for backup: " + e5.getMessage());
                        }
                    }
                    throw th;
                }
            } catch (RemoteException e6) {
                str = TAG;
                Log.e(str, "Unable to invoke backup manager for backup");
                if (fd2 != null) {
                }
            } catch (Throwable th5) {
                str = TAG;
                fd = null;
                th = th5;
                if (fd != null) {
                }
                throw th;
            }
        } else {
            Log.e(TAG, "no backup packages supplied and neither -shared nor -all given");
        }
    }

    private void doRestore(int socketFd, int userId) {
        ParcelFileDescriptor fd = null;
        try {
            fd = ParcelFileDescriptor.adoptFd(socketFd);
            this.mBackupManager.adbRestore(userId, fd);
            if (fd != null) {
                try {
                    fd.close();
                } catch (IOException e) {
                }
            }
        } catch (RemoteException e2) {
            Log.e(TAG, "Unable to invoke backup manager for restore");
            if (fd != null) {
                fd.close();
            }
        } catch (Throwable th) {
            if (fd != null) {
                try {
                    fd.close();
                } catch (IOException e3) {
                }
            }
            throw th;
        }
    }

    private int parseUserId() {
        int argNumber = 0;
        while (true) {
            String[] strArr = mArgs;
            if (argNumber >= strArr.length - 1) {
                return 0;
            }
            if ("-user".equals(strArr[argNumber])) {
                return UserHandle.parseUserArg(mArgs[argNumber + 1]);
            }
            argNumber++;
        }
    }

    private boolean isBackupActiveForUser(int userId) {
        try {
            return this.mBackupManager.isBackupServiceActive(userId);
        } catch (RemoteException e) {
            Log.e(TAG, "Could not access BackupManager: " + e.toString());
            return false;
        }
    }

    private static void showUsage() {
        System.err.println(" backup [-user USER_ID] [-f FILE] [-apk|-noapk] [-obb|-noobb] [-shared|-noshared]");
        System.err.println("        [-all] [-system|-nosystem] [-keyvalue|-nokeyvalue] [PACKAGE...]");
        System.err.println("     write an archive of the device's data to FILE [default=backup.adb]");
        System.err.println("     package list optional if -all/-shared are supplied");
        System.err.println("     -user: user ID for which to perform the operation (default - system user)");
        System.err.println("     -apk/-noapk: do/don't back up .apk files (default -noapk)");
        System.err.println("     -obb/-noobb: do/don't back up .obb files (default -noobb)");
        System.err.println("     -shared|-noshared: do/don't back up shared storage (default -noshared)");
        System.err.println("     -all: back up all installed applications");
        System.err.println("     -system|-nosystem: include system apps in -all (default -system)");
        System.err.println("     -keyvalue|-nokeyvalue: include apps that perform key/value backups.");
        System.err.println("         (default -nokeyvalue)");
        System.err.println(" restore [-user USER_ID] FILE       restore device contents from FILE");
        System.err.println("     -user: user ID for which to perform the operation (default - system user)");
    }

    private String nextArg() {
        int i = this.mNextArg;
        String[] strArr = mArgs;
        if (i >= strArr.length) {
            return null;
        }
        String arg = strArr[i];
        this.mNextArg = i + 1;
        return arg;
    }
}
