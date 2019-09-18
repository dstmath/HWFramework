package com.huawei.android.hwutil;

import android.os.FileUtils;
import android.system.ErrnoException;
import android.system.Os;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.telephony.PhoneConstants;
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
        if (TextUtils.isEmpty(param) || param.charAt(0) == '\"' || param.contains(PhoneConstants.APN_TYPE_ALL)) {
            return param;
        }
        return "\"" + param + "\"";
    }

    public static Boolean echo(String role, String wrrule, String path) {
        if (path == null) {
            return false;
        }
        File file = new File(path);
        if (!file.exists()) {
            Log.e(TAG, "file is not exists " + path);
            return false;
        }
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(file);
            os.write(wrrule.getBytes());
            try {
                os.close();
            } catch (Exception e) {
            }
            return true;
        } catch (IOException e2) {
            Log.e(TAG, "Write failed to file " + path);
            if (os != null) {
                try {
                    os.close();
                } catch (Exception e3) {
                }
            }
            return false;
        } catch (Exception e4) {
            Log.e(TAG, "Write failed to file " + path);
            if (os != null) {
                try {
                    os.close();
                } catch (Exception e5) {
                }
            }
            return false;
        } catch (Throwable th) {
            if (os != null) {
                try {
                    os.close();
                } catch (Exception e6) {
                }
            }
            throw th;
        }
    }

    private static boolean chmodAllFiles(File file) {
        if (file == null || !file.exists()) {
            return false;
        }
        if (!file.isDirectory()) {
            FileUtils.setPermissions(file, 509, 1000, 1023);
            return false;
        }
        File[] files = file.listFiles();
        if (files == null) {
            return false;
        }
        for (File sfile : files) {
            if (sfile.isDirectory()) {
                chmodAllFiles(sfile);
            }
            FileUtils.setPermissions(sfile, 509, 1000, 1023);
        }
        if (file.exists()) {
            FileUtils.setPermissions(file, 509, 1000, 1023);
        }
        return true;
    }

    public static Boolean chmod(String rule, String chrule, String path) {
        if (path == null) {
            return false;
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
        if (file == null || !file.exists()) {
            return false;
        }
        if (!file.isDirectory()) {
            if (file.delete()) {
                Log.i(TAG, ": delete file success :" + file);
            }
            return false;
        }
        File[] files = file.listFiles();
        if (files == null) {
            return false;
        }
        for (File sfile : files) {
            if (sfile.isDirectory()) {
                deleteAll(sfile);
            }
            if (sfile.delete()) {
                Log.i(TAG, ": delete file success :" + sfile);
            }
        }
        if (file.exists() && !file.delete()) {
            Log.e(TAG, "FatherFile delete false");
        }
        return true;
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
