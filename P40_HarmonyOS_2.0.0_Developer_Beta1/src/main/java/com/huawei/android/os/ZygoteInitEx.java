package com.huawei.android.os;

import com.android.internal.os.ZygoteInit;
import java.util.List;

public class ZygoteInitEx {
    public static boolean isMygote() {
        return ZygoteInit.sIsMygote;
    }

    public static List<String> getNonBootClasspathList() {
        return ZygoteInit.getNonBootClasspathList();
    }
}
