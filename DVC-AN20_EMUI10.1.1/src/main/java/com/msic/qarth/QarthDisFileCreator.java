package com.msic.qarth;

import android.system.ErrnoException;
import android.system.Os;
import android.util.Log;
import com.huawei.sidetouch.HwSideStatusManager;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QarthDisFileCreator {
    private static final int DIR_PERMISSION_MODE = 493;
    private static final QarthDisFileCreator QARTH_DISFILE_CREATOR = new QarthDisFileCreator();
    private static final String TAG = QarthDisFileCreator.class.getSimpleName();

    public static QarthDisFileCreator getQarthDisFileCreator() {
        return QARTH_DISFILE_CREATOR;
    }

    public boolean disableExceptionQarthPatch(Throwable e, String crashPackageName) {
        StringWriter writer = new StringWriter();
        e.printStackTrace(new PrintWriter(writer));
        StringBuffer buffer = writer.getBuffer();
        if (!Pattern.compile("(\\S+)\\(<Qarth>\\)").matcher(buffer).find()) {
            Log.i(TAG, "pattern not match");
            return false;
        }
        Matcher matcherHook = Pattern.compile("\\((\\w+)_Hook(1|2|3)_\\S+\\)").matcher(buffer);
        if (matcherHook.find()) {
            return createDisableFileByType(buffer, matcherHook.group(1), matcherHook.group(2), crashPackageName);
        }
        Log.i(TAG, "hook file pattern not match");
        return false;
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private boolean createDisableFileByType(StringBuffer buffer, String hookClass, String hookType, String crashPackageName) {
        char c;
        switch (hookType.hashCode()) {
            case 49:
                if (hookType.equals("1")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 50:
                if (hookType.equals(HwSideStatusManager.AUDIO_STATE_RING)) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 51:
                if (hookType.equals("3")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        if (c == 0) {
            return createDisableFile(buffer, "/patch_hw/fwkhotpatch/", "system/systemserver", "systemserver", hookClass, crashPackageName);
        }
        if (c == 1) {
            return createDisableFile(buffer, "/patch_hw/fwkhotpatch/", "system/all", "all", hookClass, crashPackageName);
        }
        if (c != 2) {
            return false;
        }
        return createDisableFile(buffer, "/patch_hw/fwkhotpatch/", crashPackageName, crashPackageName, hookClass, crashPackageName);
    }

    private boolean createDisableFile(StringBuffer buffer, String path, String disPath, String packageName, String hookClass, String crashPackageName) {
        String fileNameSuffix;
        String hotfixPath;
        String hotfixPath2 = "/data/hotpatch/fwkpatchdir/";
        String fileNameSuffix2 = ".disable";
        String fileName = null;
        File patchFile = new File(path + packageName);
        if (!patchFile.exists()) {
            Log.e(TAG, "createDisableFile dir is null");
            return false;
        }
        File[] patchFiles = patchFile.listFiles();
        if (patchFiles != null) {
            if (patchFiles.length != 0) {
                int length = patchFiles.length;
                int i = 0;
                while (true) {
                    if (i >= length) {
                        break;
                    }
                    File f = patchFiles[i];
                    Matcher fileMatcher = Pattern.compile("(\\S+)(_all|_v[0-9._]+)_{0,1}(.*).qarth$").matcher(f.getName());
                    if (!fileMatcher.find()) {
                        Log.e(TAG, "patch file pattern not match");
                        hotfixPath = hotfixPath2;
                        fileNameSuffix = fileNameSuffix2;
                    } else {
                        hotfixPath = hotfixPath2;
                        fileNameSuffix = fileNameSuffix2;
                        if (fileMatcher.group(1).replace("_", ".").equals(hookClass.replace("_", "."))) {
                            fileName = f.getName();
                            Log.i(TAG, "patch fileName = " + fileName);
                            break;
                        }
                    }
                    i++;
                    hotfixPath2 = hotfixPath;
                    fileNameSuffix2 = fileNameSuffix;
                }
                if (fileName == null) {
                    Log.e(TAG, "patch file not found: " + packageName);
                    return false;
                }
                try {
                    StringBuilder sb = new StringBuilder();
                    sb.append("/data/hotpatch/fwkpatchdir/");
                    try {
                        sb.append(disPath);
                        sb.append(File.separator);
                        sb.append(fileName);
                        sb.append(".disable");
                        String fullPath = sb.toString();
                        if (packageName.equals("all")) {
                            try {
                                if (!"android".equals(crashPackageName) && !PatchStore.isSystemApp(crashPackageName)) {
                                    fullPath = "/data/hotpatch/fwkpatchdir/" + crashPackageName + File.separator + fileName + ".disable";
                                    Log.i(TAG, "all qarth patch for non-system app to create disabled file.");
                                }
                            } catch (IOException e) {
                                ex = e;
                                Log.e(TAG, "create file failed: " + packageName + "ex = " + ex.getMessage());
                                return false;
                            }
                        }
                        File file = new File(fullPath);
                        if (!file.getParentFile().exists()) {
                            if (file.getParentFile().mkdirs()) {
                                try {
                                    Os.chmod(file.getParentFile().toString(), DIR_PERMISSION_MODE);
                                } catch (ErrnoException e2) {
                                    Log.e(TAG, "parentFile change mode exception");
                                }
                            } else {
                                Log.i(TAG, "parentFile create file failed");
                                return false;
                            }
                        }
                        if (file.exists()) {
                            Log.i(TAG, "the disabled file is already exists:" + fullPath);
                            return true;
                        } else if (!file.createNewFile()) {
                            return false;
                        } else {
                            try {
                                Log.i(TAG, "create disabled file success and change permission for file:" + fullPath);
                                Os.chmod(file.toString(), 420);
                                return true;
                            } catch (ErrnoException e3) {
                                Log.e(TAG, "patch disabled file change mode exception");
                                return true;
                            }
                        }
                    } catch (IOException e4) {
                        ex = e4;
                        Log.e(TAG, "create file failed: " + packageName + "ex = " + ex.getMessage());
                        return false;
                    }
                } catch (IOException e5) {
                    ex = e5;
                    Log.e(TAG, "create file failed: " + packageName + "ex = " + ex.getMessage());
                    return false;
                }
            }
        }
        QarthLog.e(TAG, "create disable file not find patch in : " + packageName);
        return false;
    }
}
