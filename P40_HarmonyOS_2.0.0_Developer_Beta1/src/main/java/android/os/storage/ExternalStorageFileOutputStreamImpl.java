package android.os.storage;

import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class ExternalStorageFileOutputStreamImpl extends FileOutputStream {
    private static boolean mDoAccessDefalut = true;

    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    public ExternalStorageFileOutputStreamImpl(String name) throws FileNotFoundException {
        this(name != null ? new ExternalStorageFileImpl(name).getInternalFile() : null, false);
    }

    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    public ExternalStorageFileOutputStreamImpl(String name, boolean append) throws FileNotFoundException {
        this(name != null ? new ExternalStorageFileImpl(name).getInternalFile() : null, append);
    }

    public ExternalStorageFileOutputStreamImpl(File file) throws FileNotFoundException {
        this(file, false);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public ExternalStorageFileOutputStreamImpl(File file, boolean append) throws FileNotFoundException {
        super(file instanceof ExternalStorageFileImpl ? ((ExternalStorageFileImpl) file).getInternalFile() : file, append);
        if (mDoAccessDefalut) {
            refreshSDCardFSCache(file.getAbsolutePath());
        }
    }

    public ExternalStorageFileOutputStreamImpl(FileDescriptor fdObj) {
        super(fdObj);
    }

    public static void refreshSDCardFSCache(String path) {
        try {
            Os.access(path, OsConstants.F_OK);
        } catch (ErrnoException e) {
        }
    }

    public static void acquireSelfRefreshSDCardFSCache() {
        mDoAccessDefalut = false;
    }

    public static void releaseSelfRefreshSDCardFSCache() {
        mDoAccessDefalut = true;
    }
}
