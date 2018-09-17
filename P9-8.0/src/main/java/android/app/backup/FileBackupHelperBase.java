package android.app.backup;

import android.content.Context;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import java.io.File;
import java.io.FileDescriptor;

class FileBackupHelperBase {
    private static final String TAG = "FileBackupHelperBase";
    Context mContext;
    boolean mExceptionLogged;
    long mPtr = ctor();

    private static native long ctor();

    private static native void dtor(long j);

    private static native int performBackup_native(FileDescriptor fileDescriptor, long j, FileDescriptor fileDescriptor2, String[] strArr, String[] strArr2);

    private static native int writeFile_native(long j, String str, long j2);

    private static native int writeSnapshot_native(long j, FileDescriptor fileDescriptor);

    FileBackupHelperBase(Context context) {
        this.mContext = context;
    }

    protected void finalize() throws Throwable {
        try {
            dtor(this.mPtr);
        } finally {
            super.finalize();
        }
    }

    static void performBackup_checked(ParcelFileDescriptor oldState, BackupDataOutput data, ParcelFileDescriptor newState, String[] files, String[] keys) {
        if (files.length != 0) {
            for (String f : files) {
                if (f.charAt(0) != '/') {
                    throw new RuntimeException("files must have all absolute paths: " + f);
                }
            }
            if (files.length != keys.length) {
                throw new RuntimeException("files.length=" + files.length + " keys.length=" + keys.length);
            }
            FileDescriptor oldStateFd = oldState != null ? oldState.getFileDescriptor() : null;
            FileDescriptor newStateFd = newState.getFileDescriptor();
            if (newStateFd == null) {
                throw new NullPointerException();
            }
            int err = performBackup_native(oldStateFd, data.mBackupWriter, newStateFd, files, keys);
            if (err != 0) {
                throw new RuntimeException("Backup failed 0x" + Integer.toHexString(err));
            }
        }
    }

    boolean writeFile(File f, BackupDataInputStream in) {
        f.getParentFile().mkdirs();
        int result = writeFile_native(this.mPtr, f.getAbsolutePath(), in.mData.mBackupReader);
        if (!(result == 0 || this.mExceptionLogged)) {
            Log.e(TAG, "Failed restoring file '" + f + "' for app '" + this.mContext.getPackageName() + "' result=0x" + Integer.toHexString(result));
            this.mExceptionLogged = true;
        }
        if (result == 0) {
            return true;
        }
        return false;
    }

    public void writeNewStateDescription(ParcelFileDescriptor fd) {
        int result = writeSnapshot_native(this.mPtr, fd.getFileDescriptor());
    }

    boolean isKeyInList(String key, String[] list) {
        for (String s : list) {
            if (s.equals(key)) {
                return true;
            }
        }
        return false;
    }
}
