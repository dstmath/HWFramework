package com.msic.qarth;

import android.app.ActivityThread;
import android.system.ErrnoException;
import android.system.Os;
import android.util.Log;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QarthDisFileCreator {
    private static final String TAG = QarthDisFileCreator.class.getSimpleName();
    private static QarthDisFileCreator mQarthDisFileCreator = null;

    public static QarthDisFileCreator getQarthDisFileCreator() {
        if (mQarthDisFileCreator == null) {
            mQarthDisFileCreator = new QarthDisFileCreator();
        }
        return mQarthDisFileCreator;
    }

    public boolean disableExceptionQarthPatch(Throwable e) {
        StringWriter writer = new StringWriter();
        e.printStackTrace(new PrintWriter(writer));
        StringBuffer buffer = writer.getBuffer();
        if (!Pattern.compile("(\\S+)\\(<Qarth>\\)").matcher(buffer).find()) {
            Log.e(TAG, "pattern not match");
            return false;
        }
        Matcher matcherHook = Pattern.compile("\\((\\w+)_Hook(1|2|3)_\\S+\\)").matcher(buffer);
        if (matcherHook.find()) {
            return createDisableFileByType(buffer, matcherHook.group(1), matcherHook.group(2));
        }
        Log.e(TAG, "hook file pattern not match");
        return false;
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    private boolean createDisableFileByType(StringBuffer buffer, String hookClass, String hookType) {
        char c;
        String str = hookType;
        switch (hookType.hashCode()) {
            case 49:
                if (str.equals("1")) {
                    c = 0;
                    break;
                }
            case 50:
                if (str.equals("2")) {
                    c = 1;
                    break;
                }
            case 51:
                if (str.equals("3")) {
                    c = 2;
                    break;
                }
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
                return createDisableFile(buffer, "/patch_hw/fwkhotpatch/", "system/systemserver", "systemserver", hookClass);
            case 1:
                return createDisableFile(buffer, "/patch_hw/fwkhotpatch/", "system/all", "all", hookClass);
            case 2:
                String packageName = ActivityThread.currentPackageName();
                return createDisableFile(buffer, "/patch_hw/fwkhotpatch/", packageName, packageName, hookClass);
            default:
                return false;
        }
    }

    private boolean createDisableFile(StringBuffer buffer, String path, String disPath, String packageName, String hookClass) {
        String str;
        String fileName = null;
        File patchFile = new File(path + str);
        if (!patchFile.exists()) {
            Log.e(TAG, "createDisableFile dir is null");
            return false;
        }
        File[] patchFiles = patchFile.listFiles();
        if (patchFiles == null || patchFiles.length == 0) {
            String str2 = disPath;
            String str3 = hookClass;
            QarthLog.e(TAG, "create disable file not find patch in : " + str);
            return false;
        }
        int length = patchFiles.length;
        int i = 0;
        while (true) {
            if (i >= length) {
                String str4 = hookClass;
                break;
            }
            File f = patchFiles[i];
            Matcher fileMatcher = Pattern.compile("(\\S+)(_all|_v[0-9._]+)_{0,1}(.*).qarth$").matcher(f.getName());
            if (!fileMatcher.find()) {
                Log.e(TAG, "patch file pattern not match");
                String str5 = hookClass;
            } else if (fileMatcher.group(1).replace("_", ".").equals(hookClass.replace("_", "."))) {
                fileName = f.getName();
                Log.i(TAG, "patch fileName = " + fileName);
                break;
            }
            i++;
        }
        String fileName2 = fileName;
        if (fileName2 == null) {
            Log.e(TAG, "patch file not found: " + str);
            return false;
        }
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("/data/hotpatch/fwkpatchdir/");
            try {
                sb.append(disPath);
                sb.append(File.separator);
                sb.append(fileName2);
                sb.append(".disable");
                String fullPath = sb.toString();
                if (str.equals("all") && (ActivityThread.currentApplication().getApplicationInfo().flags & 1) == 0) {
                    String mCurPackageName = ActivityThread.currentPackageName();
                    fullPath = "/data/hotpatch/fwkpatchdir/" + mCurPackageName + File.separator + fileName2 + ".disable";
                    Log.i(TAG, "all path =" + fullPath);
                }
                File file = new File(fullPath);
                if (!file.getParentFile().exists()) {
                    if (file.getParentFile().mkdirs()) {
                        try {
                            Os.chmod(file.getParentFile().toString(), 484);
                        } catch (ErrnoException e) {
                            Log.e(TAG, "parentFile change mode exception");
                        }
                    } else {
                        Log.i(TAG, "parentFile create file failed");
                        return false;
                    }
                }
                if (file.exists()) {
                    return true;
                }
                if (file.createNewFile()) {
                    try {
                        Os.chmod(file.toString(), 420);
                    } catch (ErrnoException e2) {
                        Log.e(TAG, "patch disabled file change mode exception");
                    }
                    return true;
                }
                return false;
            } catch (IOException e3) {
                ex = e3;
                Log.e(TAG, "create file failed: " + str + "ex = " + ex.getMessage());
                return false;
            }
        } catch (IOException e4) {
            ex = e4;
            String str6 = disPath;
            Log.e(TAG, "create file failed: " + str + "ex = " + ex.getMessage());
            return false;
        }
    }
}
