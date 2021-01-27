package com.huawei.android.internal.content;

import com.android.internal.content.NativeLibraryHelper;
import java.io.File;
import java.io.IOException;

public class NativeLibraryHelperEx {
    public static final String LIB64_DIR_NAME = "lib64";
    public static final String LIB_DIR_NAME = "lib";

    public static void createNativeLibrarySubdir(File path) throws IOException {
        NativeLibraryHelper.createNativeLibrarySubdir(path);
    }
}
