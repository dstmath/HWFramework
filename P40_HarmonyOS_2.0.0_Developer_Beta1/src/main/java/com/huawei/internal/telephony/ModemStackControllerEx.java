package com.huawei.internal.telephony;

import android.os.Handler;
import com.huawei.android.util.NoExtAPIException;

public class ModemStackControllerEx {
    public static void registerForStackReady(Handler h, int what, Object obj) {
        throw new NoExtAPIException("method not supported.");
    }

    public static boolean isStackReady() {
        throw new NoExtAPIException("method not supported.");
    }

    public static int getPrimarySub() {
        throw new NoExtAPIException("method not supported.");
    }
}
