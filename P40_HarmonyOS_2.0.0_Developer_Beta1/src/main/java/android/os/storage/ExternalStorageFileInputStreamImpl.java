package android.os.storage;

import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class ExternalStorageFileInputStreamImpl extends FileInputStream {
    private static boolean mDoAccessDefalut = true;

    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    public ExternalStorageFileInputStreamImpl(String name) throws FileNotFoundException {
        this(name != null ? new ExternalStorageFileImpl(name) : null);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public ExternalStorageFileInputStreamImpl(File file) throws FileNotFoundException {
        super(file instanceof ExternalStorageFileImpl ? ((ExternalStorageFileImpl) file).getInternalFile() : file);
        if (mDoAccessDefalut) {
            refreshSDCardFSCache(file.getAbsolutePath());
        }
    }

    public ExternalStorageFileInputStreamImpl(FileDescriptor fdObj) {
        super(fdObj, false);
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
