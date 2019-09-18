package android.os.storage;

import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;

public class ExternalStorageRandomAccessFileImpl extends RandomAccessFile {
    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    public ExternalStorageRandomAccessFileImpl(String name, String mode) throws FileNotFoundException {
        this((File) name != null ? new ExternalStorageFileImpl(name) : null, mode);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public ExternalStorageRandomAccessFileImpl(File file, String mode) throws FileNotFoundException {
        super(file instanceof ExternalStorageFileImpl ? ((ExternalStorageFileImpl) file).getInternalFile() : file, mode);
        try {
            Os.access(file.getAbsolutePath(), OsConstants.F_OK);
        } catch (ErrnoException e) {
        }
    }
}
