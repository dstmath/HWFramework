package com.android.server;

import android.os.StatFs;
import android.os.SystemProperties;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class HWDataSpaceHolder {
    private static final String DATA_SPACE_PATH = "/data";
    private static final boolean DEBUG_LOG = false;
    private static final int DEFAULT_RELASE_SIZE = 512;
    private static final int FILL_BUFF_SIZE = 262144;
    private static final int K_SIZE = 1024;
    private static final String PLACE_FILE_NAME = "/data/.hwplace";
    private static final int PLACE_FILE_SIZE = 62914560;
    private static final String PROP_REALSE_COUNT = "sys.hwsholder.count";
    private static final String PROP_REALSE_SIZE = "ro.config.hwsholder.releasesize";
    private static final int[] RELEASE_FACTOR = {1, 2, 4, 8, 16};
    private static final int SPACE_DRAIN_THRESHOLD = 524288;
    private static final int SPACE_FILL_THRESHOLD = 83886080;
    private static final String TAG = "HWDataSpaceHolder";

    public static void activePlaceFile() {
        try {
            PrintLog("start time: " + System.currentTimeMillis());
            long spaceSize = getAvaibleSize(DATA_SPACE_PATH);
            PrintLog("spaceSize: " + spaceSize);
            if (spaceSize > 83886080) {
                PrintLog("available space is rich!");
                int fillSize = PLACE_FILE_SIZE - getFileSize(PLACE_FILE_NAME);
                if (fillSize > 0) {
                    PrintLog("fill place file, size:" + fillSize);
                    fillFile(PLACE_FILE_NAME, fillSize, true);
                } else {
                    PrintLog("place file is full!");
                }
                SystemProperties.set(PROP_REALSE_COUNT, "0");
            } else if (spaceSize < 524288) {
                PrintLog("available space is low");
                int fileSize = getFileSize(PLACE_FILE_NAME);
                if (fileSize > 0) {
                    int realseSize = getReleaseSize();
                    if (realseSize > 0) {
                        int fillSize2 = fileSize - realseSize;
                        if (fillSize2 > 0) {
                            PrintLog("release space size:" + realseSize);
                            fillFile(PLACE_FILE_NAME, fillSize2, false);
                        } else {
                            deleteFile(PLACE_FILE_NAME);
                            PrintLog("release space size:" + fileSize);
                        }
                    }
                } else {
                    PrintLog("release space, but no place file");
                }
            } else {
                SystemProperties.set(PROP_REALSE_COUNT, "0");
                PrintLog("do nothing");
            }
            PrintLog("stop time: " + System.currentTimeMillis());
        } catch (Exception e) {
        }
    }

    private static int getFileSize(String path) {
        File file = new File(path);
        if (file.exists()) {
            return (int) file.length();
        }
        return 0;
    }

    private static long getAvaibleSize(String path) {
        StatFs statFs = new StatFs(path);
        return ((long) statFs.getAvailableBlocks()) * ((long) statFs.getBlockSize());
    }

    private static void fillFile(String fileName, int size, boolean isAppend) {
        if (size > 0 && size <= PLACE_FILE_SIZE) {
            byte[] buff = new byte[262144];
            FileOutputStream fos = null;
            File file = new File(fileName);
            try {
                if (!file.exists() && !file.createNewFile()) {
                    PrintLog("create file failed!");
                }
                FileOutputStream fos2 = new FileOutputStream(file, isAppend);
                int tmpSize = 0;
                while (tmpSize < size) {
                    int writeSize = 262144 < size - tmpSize ? 262144 : size - tmpSize;
                    fos2.write(buff, 0, writeSize);
                    tmpSize += writeSize;
                }
                try {
                    fos2.close();
                } catch (IOException e) {
                }
            } catch (Exception e2) {
                if (fos != null) {
                    fos.close();
                }
            } catch (Throwable th) {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e3) {
                    }
                }
                throw th;
            }
        }
    }

    private static void deleteFile(String path) {
        File file = new File(path);
        if (file.exists() && !file.delete()) {
            PrintLog("delete file failed!");
        }
    }

    private static int getReleaseSize() {
        int baseSize = SystemProperties.getInt(PROP_REALSE_SIZE, 512);
        if (baseSize <= 0) {
            return SPACE_DRAIN_THRESHOLD;
        }
        int count = SystemProperties.getInt(PROP_REALSE_COUNT, 0);
        if (count < 0 || count >= RELEASE_FACTOR.length) {
            SystemProperties.set(PROP_REALSE_COUNT, "0");
            return baseSize * 1024;
        }
        SystemProperties.set(PROP_REALSE_COUNT, String.valueOf(count + 1));
        return baseSize * 1024 * RELEASE_FACTOR[count];
    }

    private static void PrintLog(String msg) {
    }
}
