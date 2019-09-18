package com.huawei.libcore.io;

import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class ExternalStorageFileInputStream extends FileInputStream {
    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    public ExternalStorageFileInputStream(String name) throws FileNotFoundException {
        this((File) name != null ? new ExternalStorageFile(name) : null);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public ExternalStorageFileInputStream(File file) throws FileNotFoundException {
        super(file instanceof ExternalStorageFile ? ((ExternalStorageFile) file).getInternalFile() : file);
        try {
            Os.access(file.getAbsolutePath(), OsConstants.F_OK);
        } catch (ErrnoException e) {
        }
    }

    public ExternalStorageFileInputStream(FileDescriptor fdObj) {
        super(fdObj, false);
    }
}
