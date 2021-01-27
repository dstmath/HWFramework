package com.huawei.android.os;

import android.os.ParcelFileDescriptor;
import java.io.FileDescriptor;

public class ParcelFileDescriptorExt {
    public static ParcelFileDescriptor getParcelFileDescriptor(FileDescriptor fd) {
        return new ParcelFileDescriptor(fd);
    }
}
