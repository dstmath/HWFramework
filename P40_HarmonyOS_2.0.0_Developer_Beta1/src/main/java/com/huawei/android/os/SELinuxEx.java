package com.huawei.android.os;

import android.os.SELinux;
import java.io.File;

public class SELinuxEx {
    public static boolean restoreconRecursive(File file) {
        return SELinux.restoreconRecursive(file);
    }

    public static boolean restorecon(String pathname) throws NullPointerException {
        return SELinux.restorecon(pathname);
    }
}
