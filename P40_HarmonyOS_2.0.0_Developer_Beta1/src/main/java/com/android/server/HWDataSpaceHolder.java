package com.android.server;

import android.os.StatFs;
import android.os.SystemProperties;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class HWDataSpaceHolder {
    private static final String DATA_SPACE_PATH = "/data";
    private static final int DEFAULT_RELASE_SIZE = 512;
    private static final int FILL_BUFF_SIZE = 262144;
    private static final boolean IS_DEBUG_LOG = false;
    private static final int K_SIZE = 1024;
    private static final String PLACE_FILE_NAME = "/data/.hwplace";
    private static final int PLACE_FILE_SIZE = 62914560;
    private static final String PROP_REALSE_COUNT = "sys.hwsholder.count";
    private static final String PROP_REALSE_SIZE = "ro.config.hwsholder.releasesize";
    private static final int[] RELEASE_FACTORS = {1, 2, 4, 8, 16};
    private static final int SPACE_DRAIN_THRESHOLD = 524288;
    private static final int SPACE_FILL_THRESHOLD = 83886080;
    private static final String TAG = "HWDataSpaceHolder";

    public static void activePlaceFile() {
        try {
            printLog("start time: " + System.currentTimeMillis());
            long spaceSize = getAvaibleSize(DATA_SPACE_PATH);
            printLog("spaceSize: " + spaceSize);
            if (spaceSize > 83886080) {
                printLog("available space is rich!");
                int fillSize = PLACE_FILE_SIZE - getFileSize(PLACE_FILE_NAME);
                if (fillSize > 0) {
                    printLog("fill place file, size:" + fillSize);
                    fillFile(PLACE_FILE_NAME, fillSize, true);
                } else {
                    printLog("place file is full!");
                }
                resetDropRealseCount();
            } else if (spaceSize < 524288) {
                printLog("available space is low");
                int fileSize = getFileSize(PLACE_FILE_NAME);
                if (fileSize > 0) {
                    int realseSize = getReleaseSize();
                    if (realseSize > 0) {
                        int fillSize2 = fileSize - realseSize;
                        if (fillSize2 > 0) {
                            printLog("release space size:" + realseSize);
                            fillFile(PLACE_FILE_NAME, fillSize2, false);
                        } else {
                            deleteFile(PLACE_FILE_NAME);
                            printLog("release space size:" + fileSize);
                        }
                    }
                } else {
                    printLog("release space, but no place file");
                }
            } else {
                resetDropRealseCount();
                printLog("do nothing");
            }
            printLog("stop time: " + System.currentTimeMillis());
        } catch (Exception e) {
            Log.e(TAG, "activePlaceFile catch Exception");
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
            byte[] buffs = new byte[262144];
            FileOutputStream fos = null;
            File file = new File(fileName);
            try {
                if (!file.exists() && !file.createNewFile()) {
                    printLog("create file failed!");
                }
                FileOutputStream fos2 = new FileOutputStream(file, isAppend);
                int tmpSize = 0;
                while (tmpSize < size) {
                    int writeSize = size - tmpSize > 262144 ? 262144 : size - tmpSize;
                    fos2.write(buffs, 0, writeSize);
                    tmpSize += writeSize;
                }
                try {
                    fos2.close();
                } catch (IOException e) {
                    Log.e(TAG, "close file catch Exception");
                }
            } catch (Exception e2) {
                if (0 != 0) {
                    fos.close();
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    try {
                        fos.close();
                    } catch (IOException e3) {
                        Log.e(TAG, "close file catch Exception");
                    }
                }
                throw th;
            }
        }
    }

    private static void deleteFile(String path) {
        File file = new File(path);
        if (file.exists() && !file.delete()) {
            printLog("delete file failed!");
        }
    }

    private static int getReleaseSize() {
        int baseSize = SystemProperties.getInt(PROP_REALSE_SIZE, 512);
        if (baseSize <= 0) {
            return 524288;
        }
        int count = SystemProperties.getInt(PROP_REALSE_COUNT, 0);
        if (count < 0 || count >= RELEASE_FACTORS.length) {
            resetDropRealseCount();
            return baseSize * 1024;
        }
        SystemProperties.set(PROP_REALSE_COUNT, String.valueOf(count + 1));
        return baseSize * 1024 * RELEASE_FACTORS[count];
    }

    private static void resetDropRealseCount() {
        SystemProperties.set(PROP_REALSE_COUNT, "0");
    }

    private static void printLog(String msg) {
    }
}
