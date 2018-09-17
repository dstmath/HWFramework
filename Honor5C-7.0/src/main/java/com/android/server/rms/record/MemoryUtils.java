package com.android.server.rms.record;

import android.util.Log;
import com.android.internal.util.MemInfoReader;
import com.android.server.rms.collector.MemoryFragReader;
import com.android.server.rms.utils.Utils;

public class MemoryUtils {
    private static final String AVAILABLE_MEMORY_NUM = "AVAILABLE_MEMORY_NUM";
    private static final int BIG_LOG_FRAGLEVEL_FACTOR = 20;
    private static final long DEFAULT_SUM_MEM_FREE = -1;
    private static final String MEMORY_CACHED_FREE = "Cached + Free";
    private static final long SIZE_AVAILABLE_MEMORY = 3000;
    private static final long SIZE_MEMORY_CACHED_FREE = 500;
    private static final String TAG = "RMS.MemoryUtils";

    public static void uploadMemoryStatusToBigDataLog(long paramSumOfMemAndFree, int firstSumOfAvailableMemoryBlock) {
        if (Utils.DEBUG || Utils.HWFLOW) {
            Log.i(TAG, "invoke the uploadMemoryStatusToBigDataLog method,paramSumOfMemAndFree:" + paramSumOfMemAndFree + ",firstSumOfAvailableMemoryBlock:" + firstSumOfAvailableMemoryBlock);
        }
        long differenceMemAndFree = calculateDifferenceMemAndFree(paramSumOfMemAndFree);
        if (Utils.DEBUG || Utils.HWFLOW) {
            Log.i(TAG, "uploadMemoryStatusToBigDataLog:uploadBigDataLog,cached+free:" + differenceMemAndFree);
        }
        if (differenceMemAndFree >= SIZE_MEMORY_CACHED_FREE) {
            uploadBigDataLog(MEMORY_CACHED_FREE, differenceMemAndFree);
        }
        int availableNum = MemoryFragReader.getMemoryAvailableNumInAllZones();
        if (Utils.DEBUG || Utils.HWFLOW) {
            Log.i(TAG, "uploadMemoryStatusToBigDataLog:uploadBigDataLog,available memory block num(after the Order 4 memory block,include order 4 in the all zones):" + availableNum);
        }
        if (((long) (availableNum - firstSumOfAvailableMemoryBlock)) >= SIZE_AVAILABLE_MEMORY) {
            uploadBigDataLog(AVAILABLE_MEMORY_NUM, (long) (availableNum - firstSumOfAvailableMemoryBlock));
        }
    }

    private static void uploadBigDataLog(String memType, long value) {
        if (memType == null) {
            Log.e(TAG, "uploadBigDataLog, the param memType is null");
            return;
        }
        int arg2Value = 0;
        try {
            arg2Value = Integer.parseInt(String.valueOf(value));
        } catch (Exception e) {
            Log.e(TAG, "uploadBigDataLog,value can't be converted to int value:" + value);
        }
        String resourceLog = "#Memory Compact Job: memType :" + memType + " value:" + arg2Value;
        if (Utils.DEBUG || Utils.HWFLOW) {
            Log.i(TAG, "uploadBigDataLog_mem: arg1:" + memType + " arg2:" + arg2Value);
        }
        JankLogProxy.getInstance().jlog_d(memType, arg2Value, resourceLog);
    }

    private static long calculateDifferenceMemAndFree(long paramSumOfMemAndFree) {
        if (Utils.DEBUG || Utils.HWFLOW) {
            Log.i(TAG, "invoke the calculateDifferenceMemAndFree method");
        }
        long sumOfMemAndFree = 0;
        if (paramSumOfMemAndFree != DEFAULT_SUM_MEM_FREE) {
            sumOfMemAndFree = paramSumOfMemAndFree;
        }
        return Math.abs(getSumOfMemoryCacheAndFree() - sumOfMemAndFree) / 1024;
    }

    public static long getSumOfMemoryCacheAndFree() {
        if (Utils.DEBUG || Utils.HWFLOW) {
            Log.i(TAG, "invoke the getSumOfMemoryCacheAndFree method");
        }
        MemInfoReader memInfo = new MemInfoReader();
        memInfo.readMemInfo();
        return memInfo.getCachedSizeKb() + memInfo.getFreeSizeKb();
    }
}
