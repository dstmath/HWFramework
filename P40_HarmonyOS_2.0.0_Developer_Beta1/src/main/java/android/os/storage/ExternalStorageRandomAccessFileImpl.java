package android.os.storage;

import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;

public class ExternalStorageRandomAccessFileImpl extends RandomAccessFile {
    private static boolean mDoAccessDefalut = true;

    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    public ExternalStorageRandomAccessFileImpl(String name, String mode) throws FileNotFoundException {
        this(name != null ? new ExternalStorageFileImpl(name) : null, mode);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public ExternalStorageRandomAccessFileImpl(File file, String mode) throws FileNotFoundException {
        super(file instanceof ExternalStorageFileImpl ? ((ExternalStorageFileImpl) file).getInternalFile() : file, mode);
        if (mDoAccessDefalut) {
            refreshSDCardFSCache(file.getAbsolutePath());
        }
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
