package com.huawei.networkit.grs.common;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import com.huawei.android.os.BuildEx;
import com.huawei.libcore.io.ExternalStorageFile;
import com.huawei.libcore.io.ExternalStorageFileInputStream;
import com.huawei.libcore.io.ExternalStorageFileOutputStream;
import com.huawei.libcore.io.ExternalStorageRandomAccessFile;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;

public class CreateFileUtil {
    private static final String BUILDEX_NAME = "com.huawei.android.os.BuildEx";
    private static final String EXTERNAL_FILE_NAME = "com.huawei.libcore.io.ExternalStorageFile";
    private static final String EXTERNAL_INPUTSTREAM_NAME = "com.huawei.libcore.io.ExternalStorageFileInputStream";
    private static final String EXTERNAL_OUTPUTSTREAM_NAME = "com.huawei.libcore.io.ExternalStorageFileOutputStream";
    private static final String RANDOM_ACCESS_FILE_NAME = "com.huawei.libcore.io.ExternalStorageRandomAccessFile";
    private static final String TAG = "CreateFileUtil";

    public static boolean isPVersion() {
        return checkCompatible(BUILDEX_NAME) && BuildEx.VERSION.EMUI_SDK_INT >= 17;
    }

    public static String getCacheDirPath(Context context) {
        if (context == null) {
            return "";
        }
        if (Build.VERSION.SDK_INT < 24) {
            return context.getCacheDir().getPath();
        }
        return context.createDeviceProtectedStorageContext().getCacheDir().getPath();
    }

    public static File newFile(String path) {
        if (path == null) {
            return null;
        }
        if (!isPVersion()) {
            return new File(path);
        }
        if (checkCompatible(EXTERNAL_FILE_NAME)) {
            return new ExternalStorageFile(path);
        }
        return new File(path);
    }

    public static FileInputStream newFileInputStream(String path) throws FileNotFoundException {
        if (path == null) {
            Logger.w(TAG, "newFileInputStream  file is null");
            throw new FileNotFoundException("file is null");
        } else if (!isPVersion()) {
            return new FileInputStream(path);
        } else {
            if (checkCompatible(EXTERNAL_INPUTSTREAM_NAME)) {
                return new ExternalStorageFileInputStream(path);
            }
            return new FileInputStream(path);
        }
    }

    public static FileOutputStream newFileOutputStream(File file) throws FileNotFoundException {
        if (file == null) {
            Logger.e(TAG, "newFileOutputStream  file is null");
            throw new FileNotFoundException("file is null");
        } else if (!isPVersion()) {
            return new FileOutputStream(file);
        } else {
            if (checkCompatible(EXTERNAL_OUTPUTSTREAM_NAME)) {
                return new ExternalStorageFileOutputStream(file);
            }
            return new FileOutputStream(file);
        }
    }

    public static RandomAccessFile newRandomAccessFile(String filePath, String mode) throws FileNotFoundException {
        if (filePath == null) {
            Logger.w(TAG, "newFileOutputStream  file is null");
            throw new FileNotFoundException("file is null");
        } else if (!isPVersion()) {
            return new RandomAccessFile(filePath, mode);
        } else {
            if (checkCompatible(RANDOM_ACCESS_FILE_NAME)) {
                return new ExternalStorageRandomAccessFile(filePath, mode);
            }
            return new RandomAccessFile(filePath, mode);
        }
    }

    private static void tryLoadClass(String className) throws ClassNotFoundException {
        ClassLoader cl = CreateFileUtil.class.getClassLoader();
        if (cl != null) {
            cl.loadClass(className);
            return;
        }
        throw new ClassNotFoundException("not found classloader");
    }

    private static boolean checkCompatible(String className) {
        try {
            tryLoadClass(className);
            return true;
        } catch (Exception e) {
            Logger.w(TAG, className + "ClassNotFoundException");
            return false;
        }
    }

    public static void deleteSecure(File file) {
        if (file != null && file.exists() && !file.delete()) {
            Logger.w(TAG, "deleteSecure exception");
        }
    }

    public static void deleteSecure(String filePath) {
        if (!TextUtils.isEmpty(filePath)) {
            deleteSecure(newFile(filePath));
        }
    }
}
