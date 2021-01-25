package com.huawei.server.security.pwdprotect.model;

import android.os.Environment;
import java.io.File;

public class PasswordIvsCache {
    public static final File FILE_E_PIN2 = buildPath(Environment.getDataDirectory(), "system", "/hwSecurity/E_PIN2");
    public static final File FILE_E_PWDQ = buildPath(Environment.getDataDirectory(), "system", "/hwSecurity/E_PWDQ");
    public static final File FILE_E_PWDQANSWER = buildPath(Environment.getDataDirectory(), "system", "/hwSecurity/E_PWDQANSWER");
    public static final File FILE_E_SK2 = buildPath(Environment.getDataDirectory(), "system", "/hwSecurity/E_SK2");
    public static final File PWDPROTECT_DIR_PATH = buildPath(Environment.getDataDirectory(), "system/hwSecurity");

    private static File buildPath(File base, String... segments) {
        File file;
        File cur = base;
        for (String segment : segments) {
            if (cur == null) {
                file = new File(segment);
            } else {
                file = new File(cur, segment);
            }
            cur = file;
        }
        return cur;
    }
}
