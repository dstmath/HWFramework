package com.huawei.libcore.io;

import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class ExternalStorageFileOutputStream extends FileOutputStream {
    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    public ExternalStorageFileOutputStream(String name) throws FileNotFoundException {
        this(name != null ? new ExternalStorageFile(name).getInternalFile() : null, false);
    }

    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    public ExternalStorageFileOutputStream(String name, boolean append) throws FileNotFoundException {
        this(name != null ? new ExternalStorageFile(name).getInternalFile() : null, append);
    }

    public ExternalStorageFileOutputStream(File file) throws FileNotFoundException {
        this(file, false);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public ExternalStorageFileOutputStream(File file, boolean append) throws FileNotFoundException {
        super(file instanceof ExternalStorageFile ? ((ExternalStorageFile) file).getInternalFile() : file, append);
        try {
            Os.access(file.getAbsolutePath(), OsConstants.F_OK);
        } catch (ErrnoException e) {
        }
    }

    public ExternalStorageFileOutputStream(FileDescriptor fdObj) {
        super(fdObj, false);
    }
}
