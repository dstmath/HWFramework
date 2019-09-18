package android.os.storage;

import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class ExternalStorageFileInputStreamImpl extends FileInputStream {
    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    public ExternalStorageFileInputStreamImpl(String name) throws FileNotFoundException {
        this((File) name != null ? new ExternalStorageFileImpl(name) : null);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public ExternalStorageFileInputStreamImpl(File file) throws FileNotFoundException {
        super(file instanceof ExternalStorageFileImpl ? ((ExternalStorageFileImpl) file).getInternalFile() : file);
        try {
            Os.access(file.getAbsolutePath(), OsConstants.F_OK);
        } catch (ErrnoException e) {
        }
    }

    public ExternalStorageFileInputStreamImpl(FileDescriptor fdObj) {
        super(fdObj, false);
    }
}
