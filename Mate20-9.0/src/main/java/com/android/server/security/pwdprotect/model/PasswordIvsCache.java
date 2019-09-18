package com.android.server.security.pwdprotect.model;

import android.os.Environment;
import java.io.File;

public class PasswordIvsCache {
    public static final File FILE_E_PIN2 = Environment.buildPath(Environment.getDataDirectory(), new String[]{"system", "/hwSecurity/E_PIN2"});
    public static final File FILE_E_PWDQ = Environment.buildPath(Environment.getDataDirectory(), new String[]{"system", "/hwSecurity/E_PWDQ"});
    public static final File FILE_E_PWDQANSWER = Environment.buildPath(Environment.getDataDirectory(), new String[]{"system", "/hwSecurity/E_PWDQANSWER"});
    public static final File FILE_E_SK2 = Environment.buildPath(Environment.getDataDirectory(), new String[]{"system", "/hwSecurity/E_SK2"});
    public static final File PWDPROTECT_DIR_PATH = Environment.buildPath(Environment.getDataDirectory(), new String[]{"system/hwSecurity"});
}
