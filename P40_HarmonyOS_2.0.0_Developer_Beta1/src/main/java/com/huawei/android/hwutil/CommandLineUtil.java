package com.huawei.android.hwutil;

import android.hwtheme.HwThemeManager;
import android.os.FileUtils;
import android.system.ErrnoException;
import android.system.Os;
import android.text.TextUtils;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;

public class CommandLineUtil {
    public static final int FILE_RULE_771 = 505;
    public static final int FILE_RULE_775 = 509;
    private static final int LENGTH = 1024;
    private static final String SYSTEM = "system";
    private static final String TAG = "CommandLineUtil";

    public static String addQuoteMark(String param) {
        if (TextUtils.isEmpty(param) || param.charAt(0) == '\"' || param.contains("*")) {
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

    private static boolean chmodAllFiles(File file, int rule) {
        if (file == null || !file.exists()) {
            return false;
        }
        if (!file.isDirectory()) {
            FileUtils.setPermissions(file, rule, 1000, 1023);
            return false;
        }
        File[] files = file.listFiles();
        if (files == null) {
            return false;
        }
        for (File sfile : files) {
            if (sfile.isDirectory()) {
                chmodAllFiles(sfile, rule);
            }
            FileUtils.setPermissions(sfile, rule, 1000, 1023);
        }
        if (!file.exists()) {
            return true;
        }
        FileUtils.setPermissions(file, rule, 1000, 1023);
        return true;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0020, code lost:
        if (r6.equals(android.hwtheme.HwThemeManager.HWT_MODE_ALL_0775) == false) goto L_0x002d;
     */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0030  */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0038  */
    public static Boolean chmod(String rule, String chrule, String path) {
        int fileRule;
        boolean z = false;
        if (path == null) {
            return false;
        }
        int hashCode = rule.hashCode();
        if (hashCode != 1484577) {
            if (hashCode == 1484581) {
            }
        } else if (rule.equals(HwThemeManager.HWT_MODE_ALL_0771)) {
            z = true;
            if (z) {
                fileRule = 509;
            } else if (!z) {
                fileRule = 505;
            } else {
                fileRule = 505;
            }
            return Boolean.valueOf(chmodAllFiles(new File(path), fileRule));
        }
        z = true;
        if (z) {
        }
        return Boolean.valueOf(chmodAllFiles(new File(path), fileRule));
    }

    public static boolean chown(String rule, String owner, String group, String path) {
        if (path == null) {
            return false;
        }
        return chmod(rule, null, path).booleanValue();
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
        if (!file.exists() || file.delete()) {
            return true;
        }
        Log.e(TAG, "FatherFile delete false");
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

    public static boolean copyFolder(String rule, String opath, String npath) {
        File temp;
        if (opath == null || npath == null) {
            return false;
        }
        if (!new File(npath).exists()) {
            Log.e(TAG, "newFile is not excet");
            return false;
        }
        File oldFile = new File(opath);
        if (!oldFile.exists()) {
            Log.e(TAG, "oldFile is not exist");
            return false;
        }
        String[] files = oldFile.list();
        if (files == null) {
            Log.e(TAG, "copyFolder:  oldFile is null");
            return false;
        }
        for (String file : files) {
            if (opath.endsWith(File.separator)) {
                temp = new File(opath + file);
            } else {
                temp = new File(opath + File.separator + file);
            }
            if (temp.isDirectory()) {
                copyFolder("system", opath + File.separator + file, npath + File.separator + file);
            } else if (!temp.exists()) {
                Log.e(TAG, "copyFolder:  oldFile not exist.");
            } else if (!temp.isFile()) {
                Log.e(TAG, "copyFolder:  oldFile not file.");
            } else if (!temp.canRead()) {
                Log.e(TAG, "copyFolder:  oldFile cannot read.");
            } else {
                cp(temp, npath);
            }
        }
        return true;
    }

    private static boolean cp(File oldFile, String npath) {
        FileInputStream fileInputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            FileInputStream fileInputStream2 = new FileInputStream(oldFile);
            FileOutputStream fileOutputStream2 = new FileOutputStream(npath + File.separator + oldFile.getName());
            byte[] buffer = new byte[1024];
            while (true) {
                int byteRead = fileInputStream2.read(buffer);
                if (byteRead != -1) {
                    fileOutputStream2.write(buffer, 0, byteRead);
                } else {
                    try {
                        break;
                    } catch (IOException e) {
                        Log.e(TAG, "fileInputStream close failed");
                    }
                }
            }
            fileInputStream2.close();
            try {
                fileOutputStream2.flush();
                try {
                    fileOutputStream2.close();
                    return true;
                } catch (IOException e2) {
                    Log.e(TAG, "fileOutputStream close failed");
                    return true;
                }
            } catch (IOException e3) {
                Log.e(TAG, "fileOutputStream flush failed");
                fileOutputStream2.close();
                return true;
            } catch (Throwable th) {
                try {
                    fileOutputStream2.close();
                } catch (IOException e4) {
                    Log.e(TAG, "fileOutputStream close failed");
                }
                throw th;
            }
        } catch (IOException e5) {
            Log.e(TAG, "Write failed to file " + e5.toString());
            if (0 != 0) {
                try {
                    fileInputStream.close();
                } catch (IOException e6) {
                    Log.e(TAG, "fileInputStream close failed");
                }
            }
            if (0 != 0) {
                try {
                    fileOutputStream.flush();
                    try {
                        fileOutputStream.close();
                    } catch (IOException e7) {
                        Log.e(TAG, "fileOutputStream close failed");
                    }
                } catch (IOException e8) {
                    Log.e(TAG, "fileOutputStream flush failed");
                    fileOutputStream.close();
                } catch (Throwable th2) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e9) {
                        Log.e(TAG, "fileOutputStream close failed");
                    }
                    throw th2;
                }
            }
            return false;
        } catch (Exception e10) {
            Log.e(TAG, "Write failed to file " + e10.toString());
            if (0 != 0) {
                try {
                    fileInputStream.close();
                } catch (IOException e11) {
                    Log.e(TAG, "fileInputStream close failed");
                }
            }
            if (0 != 0) {
                try {
                    fileOutputStream.flush();
                    try {
                        fileOutputStream.close();
                    } catch (IOException e12) {
                        Log.e(TAG, "fileOutputStream close failed");
                    }
                } catch (IOException e13) {
                    Log.e(TAG, "fileOutputStream flush failed");
                    fileOutputStream.close();
                } catch (Throwable th3) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e14) {
                        Log.e(TAG, "fileOutputStream close failed");
                    }
                    throw th3;
                }
            }
            return false;
        } catch (Throwable th4) {
            if (0 != 0) {
                try {
                    fileInputStream.close();
                } catch (IOException e15) {
                    Log.e(TAG, "fileInputStream close failed");
                }
            }
            if (0 != 0) {
                try {
                    fileOutputStream.flush();
                    try {
                        fileOutputStream.close();
                    } catch (IOException e16) {
                        Log.e(TAG, "fileOutputStream close failed");
                    }
                } catch (IOException e17) {
                    Log.e(TAG, "fileOutputStream flush failed");
                    fileOutputStream.close();
                } catch (Throwable th5) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e18) {
                        Log.e(TAG, "fileOutputStream close failed");
                    }
                    throw th5;
                }
            }
            throw th4;
        }
    }
}
