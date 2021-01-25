package com.huawei.android.app.admin;

import android.app.admin.SecurityLog;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class SecurityLogEx {
    public static final int TAG_CRYPTO_SELF_TEST_INIT = 210038;

    public static int writeEvent(int tag, String str) {
        if (str == null) {
            return -1;
        }
        return SecurityLog.writeEvent(tag, str);
    }

    public static int writeEvent(int tag, Object... payloads) {
        if (payloads == null) {
            return -1;
        }
        return SecurityLog.writeEvent(tag, payloads);
    }
}
