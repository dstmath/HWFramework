package com.huawei.android.os;

import android.os.ParcelFileDescriptor;
import java.io.IOException;

public class ParcelFileDescriptorEx {
    public static ParcelFileDescriptor fromData(byte[] data, String name) throws IOException {
        return ParcelFileDescriptor.fromData(data, name);
    }
}
