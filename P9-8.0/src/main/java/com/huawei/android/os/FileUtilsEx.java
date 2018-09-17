package com.huawei.android.os;

import android.os.FileUtils;
import com.huawei.android.app.AppOpsManagerEx;
import java.io.File;
import java.io.IOException;

public class FileUtilsEx {
    public static int getSIRWXU() {
        return 448;
    }

    public static int getSIRUSR() {
        return 256;
    }

    public static int getSIWUSR() {
        return AppOpsManagerEx.TYPE_MICROPHONE;
    }

    public static int getSIXUSR() {
        return 64;
    }

    public static int getSIRWXG() {
        return 56;
    }

    public static int getSIRGRP() {
        return 32;
    }

    public static int getSIWGRP() {
        return 16;
    }

    public static int getSIXGRP() {
        return 8;
    }

    public static int getSIRWXO() {
        return 7;
    }

    public static int getSIROTH() {
        return 4;
    }

    public static int getSIWOTH() {
        return 2;
    }

    public static int getSIXOTH() {
        return 1;
    }

    public static int setPermissions(String path, int mode, int uid, int gid) {
        return FileUtils.setPermissions(path, mode, uid, gid);
    }

    public static void stringToFile(String filename, String string) throws IOException {
        FileUtils.stringToFile(filename, string);
    }

    public static String readTextFile(File file, int max, String ellipsis) throws IOException {
        return FileUtils.readTextFile(file, max, ellipsis);
    }
}
