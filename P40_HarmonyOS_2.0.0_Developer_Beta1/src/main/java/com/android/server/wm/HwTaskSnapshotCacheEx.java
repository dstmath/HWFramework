package com.android.server.wm;

import android.os.Debug;
import android.os.Process;
import android.os.SystemProperties;
import android.util.Slog;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedList;

public final class HwTaskSnapshotCacheEx implements IHwTaskSnapshotCacheEx {
    private static final int CONFIG_INDEX_FOR_2G_RAM = 0;
    private static final int CONFIG_INDEX_FOR_3G_RAM = 1;
    private static final int CONFIG_INDEX_FOR_4G_RAM = 2;
    private static final int CONFIG_INDEX_FOR_HIGH_RAM = 3;
    private static final int CONFIG_NUMS = 4;
    private static final int DEFAULT_SNAPSHOT_MAX_CACHE_NUM = 48;
    private static final long RAM_SIZE_2G = 2147483648L;
    private static final long RAM_SIZE_3G = 3221225472L;
    private static final long RAM_SIZE_4G = 4294967296L;
    private LinkedList<Integer> mLruTaskIdList = new LinkedList<>();
    private int mTaskIdMaxNum = getMaxSnapshotNum();

    public boolean isOverMaxCacheThreshold() {
        return this.mLruTaskIdList.size() > this.mTaskIdMaxNum;
    }

    public int getLeastRecentTaskId() {
        return this.mLruTaskIdList.getFirst().intValue();
    }

    public void removeLruTaskIdList(int taskId) {
        Slog.i("TaskSnapshot", "taskId:" + taskId + " Callers=" + Debug.getCallers(8));
        Iterator<Integer> it = this.mLruTaskIdList.iterator();
        while (it.hasNext()) {
            if (taskId == it.next().intValue()) {
                it.remove();
                return;
            }
        }
    }

    public void addLruTaskIdList(int taskId) {
        this.mLruTaskIdList.add(Integer.valueOf(taskId));
    }

    private int getMaxSnapshotNum() {
        long totalRam = Process.getTotalMemory();
        String[] configNums = SystemProperties.get("ro.config.max_snapshot_num", "").split(",");
        if (configNums.length != 4) {
            return 48;
        }
        int[] maxNums = new int[4];
        for (int i = 0; i < 4; i++) {
            try {
                int itemToInt = Integer.parseInt(configNums[i]);
                maxNums[i] = itemToInt >= 0 ? itemToInt : 48;
            } catch (NumberFormatException e) {
                Slog.e("TaskSnapshot", "encount error when parse config");
                return 48;
            }
        }
        if (totalRam <= RAM_SIZE_2G) {
            return maxNums[0];
        }
        if (totalRam <= RAM_SIZE_3G) {
            return maxNums[1];
        }
        if (totalRam <= RAM_SIZE_4G) {
            return maxNums[2];
        }
        return maxNums[3];
    }

    public void dump(PrintWriter pw, String prefix) {
        String doublePrefix = prefix + "  ";
        pw.println(prefix + "mLruTaskIdList");
        pw.println(doublePrefix + " mTaskIdMaxNum = " + this.mTaskIdMaxNum);
        Iterator<Integer> it = this.mLruTaskIdList.iterator();
        while (it.hasNext()) {
            pw.println(doublePrefix + "Entry taskId= " + it.next());
        }
    }
}
