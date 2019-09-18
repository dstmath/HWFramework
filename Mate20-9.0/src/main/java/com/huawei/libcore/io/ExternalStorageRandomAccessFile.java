package com.huawei.libcore.io;

import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;

public class ExternalStorageRandomAccessFile extends RandomAccessFile {
    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    public ExternalStorageRandomAccessFile(String name, String mode) throws FileNotFoundException {
        this((File) name != null ? new ExternalStorageFile(name) : null, mode);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public ExternalStorageRandomAccessFile(File file, String mode) throws FileNotFoundException {
        super(file instanceof ExternalStorageFile ? ((ExternalStorageFile) file).getInternalFile() : file, mode);
        try {
            Os.access(file.getAbsolutePath(), OsConstants.F_OK);
        } catch (ErrnoException e) {
        }
    }
}
