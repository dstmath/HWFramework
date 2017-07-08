package com.huawei.android.hwutil;

import android.text.TextUtils;
import android.util.Log;
import com.android.internal.telephony.PhoneConstants;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class CommandLineUtil {
    private static final String TAG = "CommandLineUtil";

    public static String addQuoteMark(String param) {
        if (TextUtils.isEmpty(param) || param.charAt(0) == '\"' || param.contains(PhoneConstants.APN_TYPE_ALL)) {
            return param;
        }
        return "\"" + param + "\"";
    }

    public static Boolean echo(String role, String wrrule, String path) {
        Boolean valueOf;
        Throwable th;
        if (path == null) {
            return Boolean.valueOf(false);
        }
        File file = new File(path);
        if (file.exists()) {
            FileOutputStream fileOutputStream = null;
            try {
                FileOutputStream os = new FileOutputStream(file);
                try {
                    os.write(wrrule.getBytes());
                    if (os != null) {
                        try {
                            os.close();
                        } catch (Exception e) {
                        }
                    }
                    return Boolean.valueOf(true);
                } catch (IOException e2) {
                    fileOutputStream = os;
                    Log.e(TAG, "Write failed to file " + path);
                    valueOf = Boolean.valueOf(false);
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (Exception e3) {
                        }
                    }
                    return valueOf;
                } catch (Exception e4) {
                    fileOutputStream = os;
                    try {
                        Log.e(TAG, "Write failed to file " + path);
                        valueOf = Boolean.valueOf(false);
                        if (fileOutputStream != null) {
                            try {
                                fileOutputStream.close();
                            } catch (Exception e5) {
                            }
                        }
                        return valueOf;
                    } catch (Throwable th2) {
                        th = th2;
                        if (fileOutputStream != null) {
                            try {
                                fileOutputStream.close();
                            } catch (Exception e6) {
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    fileOutputStream = os;
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                    throw th;
                }
            } catch (IOException e7) {
                Log.e(TAG, "Write failed to file " + path);
                valueOf = Boolean.valueOf(false);
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                return valueOf;
            } catch (Exception e8) {
                Log.e(TAG, "Write failed to file " + path);
                valueOf = Boolean.valueOf(false);
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                return valueOf;
            }
        }
        Log.e(TAG, "file is not exists " + path);
        return Boolean.valueOf(false);
    }

    public static Boolean chmod(String rule, String chrule, String path) {
        return Boolean.valueOf(run(rule, "chmod -R %s %s", new Object[]{chrule, path}));
    }

    public static boolean chown(String rule, String owner, String group, String path) {
        return run(rule, "chown %s.%s %s", new Object[]{owner, group, path});
    }

    public static boolean mkdir(String rule, String path) {
        return run(rule, "mkdir -p %s", new Object[]{path});
    }

    public static boolean mv(String rule, String opath, String npath) {
        return run(rule, "mv %s %s", new Object[]{opath, npath});
    }

    public static boolean rm(String rule, String path) {
        Object[] obj = new Object[2];
        obj[0] = path;
        return run(rule, "rm -r %s", obj);
    }

    public static boolean link(String role, String oldpath, String newpath) {
        return run(role, "ln -s %s %s", new Object[]{oldpath, newpath});
    }

    public static boolean run(String role, String cmd1, Object[] cmd2) {
        if (run(false, role, cmd1, cmd2) != null) {
            return true;
        }
        return false;
    }

    public static boolean sync(String rule) {
        return run(rule, "sync %s", new Object[]{" "});
    }

    private static InputStream run(boolean bool, String rule, String cmd1, Object[] cmd2) {
        String[] str2 = new String[3];
        if (cmd2.length <= 0) {
            return null;
        }
        String str1 = String.format(cmd1, cmd2);
        if (TextUtils.isEmpty(rule)) {
            return null;
        }
        str2[0] = "/system/bin/sh";
        str2[1] = "-c";
        str2[2] = str1;
        return runInner(bool, str2);
    }

    private static InputStream runInner(boolean bool, String[] cmd) {
        try {
            Process process = Runtime.getRuntime().exec(cmd);
            InputStream inputStream = process.getInputStream();
            if (bool || process.waitFor() == 0) {
                return inputStream;
            }
            inputStream.close();
            return null;
        } catch (Exception e) {
            Log.e(TAG, "e is " + e);
            return null;
        }
    }
}
