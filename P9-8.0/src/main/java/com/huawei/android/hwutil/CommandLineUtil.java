package com.huawei.android.hwutil;

import android.os.FileUtils;
import android.system.ErrnoException;
import android.system.Os;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.RILConstants;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;

public class CommandLineUtil {
    public static final int FILE_RULE = 509;
    private static final String TAG = "CommandLineUtil";

    public static String addQuoteMark(String param) {
        if (TextUtils.isEmpty(param) || param.charAt(0) == '\"' || (param.contains(PhoneConstants.APN_TYPE_ALL) ^ 1) == 0) {
            return param;
        }
        return "\"" + param + "\"";
    }

    /* JADX WARNING: Removed duplicated region for block: B:31:0x0096 A:{SYNTHETIC, Splitter: B:31:0x0096} */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x006e A:{SYNTHETIC, Splitter: B:23:0x006e} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static Boolean echo(String role, String wrrule, String path) {
        Boolean valueOf;
        Throwable th;
        if (path == null) {
            return Boolean.valueOf(false);
        }
        File file = new File(path);
        if (file.exists()) {
            FileOutputStream os = null;
            try {
                FileOutputStream os2 = new FileOutputStream(file);
                try {
                    os2.write(wrrule.getBytes());
                    if (os2 != null) {
                        try {
                            os2.close();
                        } catch (Exception e) {
                        }
                    }
                    return Boolean.valueOf(true);
                } catch (IOException e2) {
                    os = os2;
                    Log.e(TAG, "Write failed to file " + path);
                    valueOf = Boolean.valueOf(false);
                    if (os != null) {
                        try {
                            os.close();
                        } catch (Exception e3) {
                        }
                    }
                    return valueOf;
                } catch (Exception e4) {
                    os = os2;
                    try {
                        Log.e(TAG, "Write failed to file " + path);
                        valueOf = Boolean.valueOf(false);
                        if (os != null) {
                            try {
                                os.close();
                            } catch (Exception e5) {
                            }
                        }
                        return valueOf;
                    } catch (Throwable th2) {
                        th = th2;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    os = os2;
                    if (os != null) {
                        try {
                            os.close();
                        } catch (Exception e6) {
                        }
                    }
                    throw th;
                }
            } catch (IOException e7) {
                Log.e(TAG, "Write failed to file " + path);
                valueOf = Boolean.valueOf(false);
                if (os != null) {
                }
                return valueOf;
            } catch (Exception e8) {
                Log.e(TAG, "Write failed to file " + path);
                valueOf = Boolean.valueOf(false);
                if (os != null) {
                }
                return valueOf;
            }
        }
        Log.e(TAG, "file is not exists " + path);
        return Boolean.valueOf(false);
    }

    private static boolean chmodAllFiles(File file) {
        int i = 0;
        if (file == null || (file.exists() ^ 1) != 0) {
            return false;
        }
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null) {
                return false;
            }
            int length = files.length;
            while (i < length) {
                File sfile = files[i];
                if (sfile.isDirectory()) {
                    chmodAllFiles(sfile);
                }
                FileUtils.setPermissions(sfile, 509, 1000, RILConstants.RIL_UNSOL_RESTRICTED_STATE_CHANGED);
                i++;
            }
            if (file.exists()) {
                FileUtils.setPermissions(file, 509, 1000, RILConstants.RIL_UNSOL_RESTRICTED_STATE_CHANGED);
            }
            return true;
        }
        FileUtils.setPermissions(file, 509, 1000, RILConstants.RIL_UNSOL_RESTRICTED_STATE_CHANGED);
        return false;
    }

    public static Boolean chmod(String rule, String chrule, String path) {
        if (path == null) {
            return Boolean.valueOf(false);
        }
        return Boolean.valueOf(chmodAllFiles(new File(path)));
    }

    public static boolean chown(String rule, String owner, String group, String path) {
        if (path == null) {
            return false;
        }
        return chmodAllFiles(new File(path));
    }

    public static boolean mkdir(String rule, String path) {
        if (path == null) {
            return false;
        }
        try {
            if (new File(path).mkdir()) {
                return true;
            }
            Log.e(TAG, "mkdir failed path = " + path);
            return false;
        } catch (Exception ex) {
            Log.e(TAG, "create folder exception: " + ex.getMessage());
            return true;
        }
    }

    public static boolean mv(String rule, String opath, String npath) {
        if (opath == null || npath == null) {
            return false;
        }
        return renameFile(new File(opath), new File(npath));
    }

    private static boolean renameFile(File fileSrc, File fileTarget) {
        try {
            if (fileTarget.delete()) {
                Log.i(TAG, "renamefile, delete target success");
            }
            if (fileSrc.renameTo(fileTarget)) {
                return true;
            }
            Log.e(TAG, "rename file failed.");
            return false;
        } catch (Exception ex) {
            Log.e(TAG, "rename file exception:" + ex.getMessage());
            return false;
        }
    }

    public static boolean rm(String rule, String path) {
        if (path == null) {
            return false;
        }
        return deleteAll(new File(path));
    }

    private static boolean deleteAll(File file) {
        int i = 0;
        if (file == null || (file.exists() ^ 1) != 0) {
            return false;
        }
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null) {
                return false;
            }
            int length = files.length;
            while (i < length) {
                File sfile = files[i];
                if (sfile.isDirectory()) {
                    deleteAll(sfile);
                }
                if (sfile.delete()) {
                    Log.i(TAG, ": delete file success :" + sfile);
                }
                i++;
            }
            if (file.exists() && !file.delete()) {
                Log.e(TAG, "FatherFile delete false");
            }
            return true;
        }
        if (file.delete()) {
            Log.i(TAG, ": delete file success :" + file);
        }
        return false;
    }

    public static boolean link(String role, String oldpath, String newpath) {
        try {
            Files.createSymbolicLink(Paths.get(newpath, new String[0]), Paths.get(oldpath, new String[0]), new FileAttribute[0]);
            return true;
        } catch (IOException e) {
            Log.e(TAG, "link error = " + e.getMessage());
            return false;
        }
    }

    public static boolean unlink(String path) {
        try {
            Os.unlink(path);
            return true;
        } catch (ErrnoException e) {
            Log.e(TAG, "unlink error = " + e.getMessage());
            return false;
        }
    }
}
