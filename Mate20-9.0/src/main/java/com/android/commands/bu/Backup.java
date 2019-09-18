package com.android.commands.bu;

import android.app.backup.IBackupManager;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.system.OsConstants;
import android.util.Log;
import java.io.IOException;
import java.util.ArrayList;

public final class Backup {
    static final String TAG = "bu";
    static String[] mArgs;
    IBackupManager mBackupManager;
    int mNextArg;

    public static void main(String[] args) {
        Log.d(TAG, "Beginning: " + args[0]);
        mArgs = args;
        try {
            new Backup().run();
        } catch (Exception e) {
            Log.e(TAG, "Error running backup/restore", e);
        }
        Log.d(TAG, "Finished.");
    }

    public void run() {
        this.mBackupManager = IBackupManager.Stub.asInterface(ServiceManager.getService("backup"));
        if (this.mBackupManager == null) {
            Log.e(TAG, "Can't obtain Backup Manager binder");
            return;
        }
        String arg = nextArg();
        if (arg.equals("backup")) {
            doBackup(OsConstants.STDOUT_FILENO);
        } else if (arg.equals("restore")) {
            doRestore(OsConstants.STDIN_FILENO);
        } else {
            showUsage();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:101:0x01a0 A[SYNTHETIC, Splitter:B:101:0x01a0] */
    /* JADX WARNING: Removed duplicated region for block: B:92:0x0179 A[SYNTHETIC, Splitter:B:92:0x0179] */
    private void doBackup(int socketFd) {
        String arg;
        Throwable th;
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
            String nextArg = nextArg();
            arg = nextArg;
            if (nextArg == null) {
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
            } else {
                Log.w(TAG, "Unknown backup flag " + arg);
            }
        }
        if (doEverything && packages.size() > 0) {
            Log.w(TAG, "-all passed for backup along with specific package names");
        }
        if (doEverything || saveShared || packages.size() != 0) {
            ParcelFileDescriptor fd = null;
            try {
                ParcelFileDescriptor fd2 = ParcelFileDescriptor.adoptFd(socketFd);
                try {
                    IBackupManager iBackupManager = this.mBackupManager;
                    String[] strArr = (String[]) packages.toArray(new String[packages.size()]);
                    ArrayList<String> arrayList = packages;
                    ParcelFileDescriptor fd3 = fd2;
                    String str = arg;
                    try {
                        iBackupManager.adbBackup(fd2, saveApks, saveObbs, saveShared, doWidgets, doEverything, allIncludesSystem, doCompress, doKeyValue, strArr);
                        if (fd3 != null) {
                            try {
                                fd3.close();
                            } catch (IOException e) {
                                IOException iOException = e;
                                Log.e(TAG, "IO error closing output for backup: " + e.getMessage());
                            }
                        }
                    } catch (RemoteException e2) {
                        fd = fd3;
                        try {
                            Log.e(TAG, "Unable to invoke backup manager for backup");
                            if (fd != null) {
                            }
                            ParcelFileDescriptor parcelFileDescriptor = fd;
                            return;
                        } catch (Throwable th2) {
                            th = th2;
                            th = th;
                            if (fd != null) {
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        fd = fd3;
                        th = th;
                        if (fd != null) {
                        }
                        throw th;
                    }
                } catch (RemoteException e3) {
                    ArrayList<String> arrayList2 = packages;
                    String str2 = arg;
                    fd = fd2;
                    Log.e(TAG, "Unable to invoke backup manager for backup");
                    if (fd != null) {
                        try {
                            fd.close();
                        } catch (IOException e4) {
                            IOException iOException2 = e4;
                            Log.e(TAG, "IO error closing output for backup: " + e4.getMessage());
                        }
                    }
                    ParcelFileDescriptor parcelFileDescriptor2 = fd;
                    return;
                } catch (Throwable th4) {
                    ArrayList<String> arrayList3 = packages;
                    String str3 = arg;
                    fd = fd2;
                    th = th4;
                    if (fd != null) {
                        try {
                            fd.close();
                        } catch (IOException e5) {
                            IOException iOException3 = e5;
                            Log.e(TAG, "IO error closing output for backup: " + e5.getMessage());
                        }
                    }
                    throw th;
                }
            } catch (RemoteException e6) {
                ArrayList<String> arrayList4 = packages;
                String str4 = arg;
                Log.e(TAG, "Unable to invoke backup manager for backup");
                if (fd != null) {
                }
                ParcelFileDescriptor parcelFileDescriptor22 = fd;
                return;
            } catch (Throwable th5) {
                ArrayList<String> arrayList5 = packages;
                String str5 = arg;
                th = th5;
                if (fd != null) {
                }
                throw th;
            }
            return;
        }
        Log.e(TAG, "no backup packages supplied and neither -shared nor -all given");
    }

    private void doRestore(int socketFd) {
        ParcelFileDescriptor fd = null;
        try {
            fd = ParcelFileDescriptor.adoptFd(socketFd);
            this.mBackupManager.adbRestore(fd);
            if (fd == null) {
                return;
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Unable to invoke backup manager for restore");
            if (fd == null) {
                return;
            }
        } catch (Throwable th) {
            if (fd != null) {
                try {
                    fd.close();
                } catch (IOException e2) {
                }
            }
            throw th;
        }
        try {
            fd.close();
        } catch (IOException e3) {
        }
    }

    private static void showUsage() {
        System.err.println(" backup [-f FILE] [-apk|-noapk] [-obb|-noobb] [-shared|-noshared] [-all]");
        System.err.println("        [-system|-nosystem] [-keyvalue|-nokeyvalue] [PACKAGE...]");
        System.err.println("     write an archive of the device's data to FILE [default=backup.adb]");
        System.err.println("     package list optional if -all/-shared are supplied");
        System.err.println("     -apk/-noapk: do/don't back up .apk files (default -noapk)");
        System.err.println("     -obb/-noobb: do/don't back up .obb files (default -noobb)");
        System.err.println("     -shared|-noshared: do/don't back up shared storage (default -noshared)");
        System.err.println("     -all: back up all installed applications");
        System.err.println("     -system|-nosystem: include system apps in -all (default -system)");
        System.err.println("     -keyvalue|-nokeyvalue: include apps that perform key/value backups.");
        System.err.println("         (default -nokeyvalue)");
        System.err.println(" restore FILE             restore device contents from FILE");
    }

    private String nextArg() {
        if (this.mNextArg >= mArgs.length) {
            return null;
        }
        String arg = mArgs[this.mNextArg];
        this.mNextArg++;
        return arg;
    }
}
