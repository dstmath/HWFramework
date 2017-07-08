package com.android.commands.bu;

import android.app.backup.IBackupManager;
import android.app.backup.IBackupManager.Stub;
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
        this.mBackupManager = Stub.asInterface(ServiceManager.getService("backup"));
        if (this.mBackupManager == null) {
            Log.e(TAG, "Can't obtain Backup Manager binder");
            return;
        }
        String arg = nextArg();
        if (arg.equals("backup")) {
            doFullBackup(OsConstants.STDOUT_FILENO);
        } else if (arg.equals("restore")) {
            doFullRestore(OsConstants.STDIN_FILENO);
        } else {
            Log.e(TAG, "Invalid operation '" + arg + "'");
        }
    }

    private void doFullBackup(int socketFd) {
        ArrayList<String> packages = new ArrayList();
        boolean saveApks = false;
        boolean saveObbs = false;
        boolean saveShared = false;
        boolean doEverything = false;
        boolean doWidgets = false;
        boolean allIncludesSystem = true;
        boolean doCompress = true;
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
            } else {
                Log.w(TAG, "Unknown backup flag " + arg);
            }
        }
        if (doEverything && packages.size() > 0) {
            Log.w(TAG, "-all passed for backup along with specific package names");
        }
        if (doEverything || saveShared || packages.size() != 0) {
            ParcelFileDescriptor parcelFileDescriptor = null;
            try {
                parcelFileDescriptor = ParcelFileDescriptor.adoptFd(socketFd);
                this.mBackupManager.fullBackup(parcelFileDescriptor, saveApks, saveObbs, saveShared, doWidgets, doEverything, allIncludesSystem, doCompress, (String[]) packages.toArray(new String[packages.size()]));
                if (parcelFileDescriptor != null) {
                    try {
                        parcelFileDescriptor.close();
                    } catch (IOException e) {
                    }
                }
            } catch (RemoteException e2) {
                Log.e(TAG, "Unable to invoke backup manager for backup");
                if (parcelFileDescriptor != null) {
                    try {
                        parcelFileDescriptor.close();
                    } catch (IOException e3) {
                    }
                }
            } catch (Throwable th) {
                if (parcelFileDescriptor != null) {
                    try {
                        parcelFileDescriptor.close();
                    } catch (IOException e4) {
                    }
                }
            }
            return;
        }
        Log.e(TAG, "no backup packages supplied and neither -shared nor -all given");
    }

    private void doFullRestore(int socketFd) {
        ParcelFileDescriptor parcelFileDescriptor = null;
        try {
            parcelFileDescriptor = ParcelFileDescriptor.adoptFd(socketFd);
            this.mBackupManager.fullRestore(parcelFileDescriptor);
            if (parcelFileDescriptor != null) {
                try {
                    parcelFileDescriptor.close();
                } catch (IOException e) {
                }
            }
        } catch (RemoteException e2) {
            Log.e(TAG, "Unable to invoke backup manager for restore");
            if (parcelFileDescriptor != null) {
                try {
                    parcelFileDescriptor.close();
                } catch (IOException e3) {
                }
            }
        } catch (Throwable th) {
            if (parcelFileDescriptor != null) {
                try {
                    parcelFileDescriptor.close();
                } catch (IOException e4) {
                }
            }
        }
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
