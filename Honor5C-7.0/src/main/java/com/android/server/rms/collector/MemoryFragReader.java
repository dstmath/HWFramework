package com.android.server.rms.collector;

import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public final class MemoryFragReader {
    private static final boolean DEBUG = false;
    private static final int DEFAULT_AVAILABLE_MEM_BLOCK_SUM = 0;
    private static final long DEFAULT_FRAGLEVEL = -1;
    private static final int DEFAULT_FRAG_PAGE_ORDER = 4;
    private static final int DEFAULT_FRAG_PAGE_ORDER_0 = 0;
    private static final long DEFAULT_SUM_LEVEL = 0;
    private static final int FRAG_ZONE_DMA = 0;
    private static final int INDEX_ZONE_NAME = 3;
    private static final String SPLIT_AVAILABLE_NUM = "\\s+";
    private static final String TAG = "RMS.MemoryFragReader";
    private int[] mInfos;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.rms.collector.MemoryFragReader.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.rms.collector.MemoryFragReader.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.rms.collector.MemoryFragReader.<clinit>():void");
    }

    public MemoryFragReader() {
        this.mInfos = null;
    }

    public void readMemFragInfo() {
        ThreadPolicy savedPolicy = StrictMode.allowThreadDiskReads();
        try {
            this.mInfos = getMemoryAvailableNumInZone(FRAG_ZONE_DMA);
        } finally {
            StrictMode.setThreadPolicy(savedPolicy);
        }
    }

    public int[] getMemFragInfo() {
        return this.mInfos;
    }

    public static long[] getMemoryFragLevelsOfAllZones() {
        if (DEBUG) {
            Log.i(TAG, "invoke the getMemoryFragLevelsOfAllZones method");
        }
        String[] zoneMemoryInfoArray = getZoneMemoryInfoArrayFromBuddyinfo();
        if (zoneMemoryInfoArray.length != 0) {
            return getAllZoneFragLevel(zoneMemoryInfoArray);
        }
        Log.e(TAG, "getMemoryFragLevelsOfAllZones,the zoneMemoryInfoArray is empty");
        return new long[FRAG_ZONE_DMA];
    }

    public static int getMemoryAvailableNumInAllZones() {
        int i = FRAG_ZONE_DMA;
        if (DEBUG) {
            Log.i(TAG, "invoke the getMemoryFragLevelsOfAllZones method");
        }
        String[] zoneMemoryInfoArray = getZoneMemoryInfoArrayFromBuddyinfo();
        if (zoneMemoryInfoArray.length == 0) {
            Log.e(TAG, "getMemoryAvailableNumInAllZones,the zoneMemoryInfoArray is empty");
            return FRAG_ZONE_DMA;
        }
        int orderMemorySum = FRAG_ZONE_DMA;
        int length = zoneMemoryInfoArray.length;
        while (i < length) {
            String zoneMemmoryInfo = zoneMemoryInfoArray[i];
            if (!(zoneMemmoryInfo == null || zoneMemmoryInfo.trim().isEmpty())) {
                orderMemorySum += calculateAvailableMemBlockNum(8, DEFAULT_FRAG_PAGE_ORDER, zoneMemmoryInfo.split(SPLIT_AVAILABLE_NUM));
            }
            i++;
        }
        return orderMemorySum;
    }

    private static int calculateAvailableMemBlockNum(int startIndex, int startPageIndex, String[] zoneArray) {
        if (DEBUG) {
            Log.d(TAG, "invoke the calculateAvailableMemBlockNum method");
        }
        int availableMemBlockSum = FRAG_ZONE_DMA;
        int pageIndex = startPageIndex;
        int index = startIndex;
        while (index < zoneArray.length) {
            try {
                availableMemBlockSum += Integer.parseInt(zoneArray[index]);
                pageIndex++;
                index++;
            } catch (Exception e) {
                Log.e(TAG, "calculateAvailableMemBlockNum, zone meminfo's value is invalid,pageIndex" + pageIndex + ",zone array index:" + index);
                return FRAG_ZONE_DMA;
            }
        }
        return availableMemBlockSum;
    }

    private static String[] getZoneMemoryInfoArrayFromBuddyinfo() {
        if (DEBUG) {
            Log.i(TAG, "invoke the getZoneMemoryInfoArrayFromBuddyinfo method");
        }
        String buddyInfoLines = getBuddyInfoLines();
        if (buddyInfoLines != null && !buddyInfoLines.trim().isEmpty()) {
            return buddyInfoLines.split("\n");
        }
        Log.e(TAG, "getZoneMemoryInfoArrayFromBuddyinfo,the buddyInfoLines is empty");
        return new String[FRAG_ZONE_DMA];
    }

    private static String getBuddyInfoLines() {
        if (DEBUG) {
            Log.i(TAG, "invoke the getBuddyInfoLines method");
        }
        String buddyInfoResult = null;
        try {
            buddyInfoResult = ResourceCollector.getBuddyInfo();
            if (DEBUG) {
                Log.i(TAG, "getBuddyInfoLines:buddyinfo:\n" + buddyInfoResult);
            }
        } catch (RuntimeException e) {
            Log.e(TAG, "RuntimeException happens,sysrms_jni not contains nativeCalculateFragment");
        } catch (Exception e2) {
            Log.e(TAG, "sysrms_jni not contains nativeCalculateFragment");
        }
        return buddyInfoResult;
    }

    private static long[] getAllZoneFragLevel(String[] zoneMemoryInfoArray) {
        if (DEBUG) {
            Log.i(TAG, "invoke the getAllZoneFragLevel method");
        }
        List<Long> zoneLevelList = new ArrayList();
        int length = zoneMemoryInfoArray.length;
        for (int i = FRAG_ZONE_DMA; i < length; i++) {
            String zoneInfo = zoneMemoryInfoArray[i];
            if (!(zoneInfo == null || zoneInfo.trim().isEmpty())) {
                String[] zoneArray = zoneInfo.split(SPLIT_AVAILABLE_NUM);
                if (zoneArray.length != 0 && zoneArray.length > INDEX_ZONE_NAME) {
                    zoneLevelList.add(Long.valueOf(calculateZoneFragLevel(zoneArray, DEFAULT_FRAG_PAGE_ORDER)));
                }
            }
        }
        if (zoneLevelList.size() == 0) {
            return new long[FRAG_ZONE_DMA];
        }
        long[] resultLevelArray = new long[zoneLevelList.size()];
        for (int index = FRAG_ZONE_DMA; index < zoneLevelList.size(); index++) {
            resultLevelArray[index] = ((Long) zoneLevelList.get(index)).longValue();
        }
        return resultLevelArray;
    }

    private static long calculateZoneFragLevel(String[] zoneArray, int memoryOrder) {
        if (DEBUG) {
            Log.i(TAG, "invoke the calculateZoneFragLevel method");
        }
        if (zoneArray.length <= memoryOrder + DEFAULT_FRAG_PAGE_ORDER) {
            Log.e(TAG, "calculateZoneFragLevel,zoneArray's length is invalid");
            return DEFAULT_FRAGLEVEL;
        }
        long fragLevelSum = calculateLevelSum(DEFAULT_FRAG_PAGE_ORDER, FRAG_ZONE_DMA, zoneArray);
        long orderMemorySum = calculateLevelSum(memoryOrder + DEFAULT_FRAG_PAGE_ORDER, memoryOrder, zoneArray);
        long fragLevel = DEFAULT_FRAGLEVEL;
        if (fragLevelSum != DEFAULT_SUM_LEVEL) {
            fragLevel = ((fragLevelSum - orderMemorySum) * 100) / fragLevelSum;
        }
        if (DEBUG) {
            Log.i(TAG, "calculateZoneFragLevel,frageLevel:" + fragLevel + ",fragLevelSum:" + fragLevelSum + ",orderMemorySum:" + orderMemorySum);
        }
        return fragLevel;
    }

    private static long calculateLevelSum(int startIndex, int startPageIndex, String[] zoneArray) {
        if (DEBUG) {
            Log.d(TAG, "invoke the calculateLevelSum method");
        }
        long levelSum = DEFAULT_SUM_LEVEL;
        int pageIndex = startPageIndex;
        int index = startIndex;
        while (index < zoneArray.length) {
            try {
                levelSum += ((long) (1 << pageIndex)) * Long.parseLong(zoneArray[index]);
                pageIndex++;
                index++;
            } catch (Exception e) {
                Log.e(TAG, "calculateLevelSum, zone meminfo's value is invalid,,pageIndex" + pageIndex + ",zone array index:" + index);
                return DEFAULT_SUM_LEVEL;
            }
        }
        return levelSum;
    }

    private int[] getMemoryAvailableNumInZone(int zoneIndex) {
        if (DEBUG) {
            Log.i(TAG, "invoke the getMemoryAvailableNumInZone method");
        }
        int[] availableNumArray = new int[FRAG_ZONE_DMA];
        if (zoneIndex < 0) {
            return availableNumArray;
        }
        String[] zoneMemoryInfoArray = getZoneMemoryInfoArrayFromBuddyinfo();
        if (zoneMemoryInfoArray.length == 0) {
            Log.e(TAG, "getMemoryAvailableNumInZone,the zoneMemoryInfoArray is empty");
            return availableNumArray;
        } else if (zoneIndex <= zoneMemoryInfoArray.length - 1) {
            return getAvailableMemBlockNum(DEFAULT_FRAG_PAGE_ORDER, FRAG_ZONE_DMA, zoneMemoryInfoArray[FRAG_ZONE_DMA].split(SPLIT_AVAILABLE_NUM));
        } else {
            Log.e(TAG, "getMemoryAvailableNumInZone,the zoneMemoryInfoArray is empty");
            return availableNumArray;
        }
    }

    private int[] getAvailableMemBlockNum(int startIndex, int startPageIndex, String[] zoneArray) {
        if (DEBUG) {
            Log.d(TAG, "invoke the getAvailableMemBlockNum method");
        }
        int pageIndex = startPageIndex;
        List<Integer> memoryNumList = new ArrayList();
        int index = startIndex;
        while (index < zoneArray.length) {
            try {
                memoryNumList.add(Integer.valueOf(Integer.parseInt(zoneArray[index])));
                pageIndex++;
                index++;
            } catch (Exception e) {
                Log.e(TAG, "getAvailableMemBlockNum, zone meminfo's value is invalid,pageIndex" + pageIndex + ",zone array index:" + index);
            }
        }
        if (memoryNumList.size() == 0) {
            return new int[FRAG_ZONE_DMA];
        }
        int[] availableArray = new int[memoryNumList.size()];
        for (index = FRAG_ZONE_DMA; index < availableArray.length; index++) {
            availableArray[index] = ((Integer) memoryNumList.get(index)).intValue();
        }
        return availableArray;
    }
}
