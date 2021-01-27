package ohos.utils;

import java.util.LinkedList;
import java.util.List;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public final class Stopwatch {
    private final HiLogLabel label;
    private final List<String> nameList = new LinkedList();
    private final List<Long> splitList = new LinkedList();
    private final String taskName;

    public Stopwatch(HiLogLabel hiLogLabel, String str) {
        this.label = hiLogLabel;
        this.taskName = str;
        if (HiLog.isDebuggable()) {
            initLists();
        }
    }

    public void split(String str) {
        if (HiLog.isDebuggable()) {
            this.nameList.add(str);
            this.splitList.add(Long.valueOf(System.currentTimeMillis()));
        }
    }

    public void reset() {
        if (HiLog.isDebuggable()) {
            this.nameList.clear();
            this.splitList.clear();
            initLists();
        }
    }

    private void initLists() {
        this.splitList.add(Long.valueOf(System.currentTimeMillis()));
        this.nameList.add(this.taskName);
    }

    public void writeLog() {
        int i;
        if (HiLog.isDebuggable()) {
            HiLog.debug(this.label, "%{public}s: begin", this.taskName);
            int size = this.splitList.size();
            long longValue = this.splitList.get(size - 1).longValue() - this.splitList.get(0).longValue();
            for (int i2 = 1; i2 < size; i2++) {
                String str = this.nameList.get(i2);
                long longValue2 = this.splitList.get(i2).longValue() - this.splitList.get(i2 - 1).longValue();
                if (longValue == 0) {
                    i = 100;
                } else {
                    i = Math.round((((float) longValue2) / ((float) longValue)) * 100.0f);
                }
                HiLog.debug(this.label, "%{public}s:      %{public}d ms, %{public}d%%, %{public}s", this.taskName, Long.valueOf(longValue2), Integer.valueOf(i), str);
            }
            HiLog.debug(this.label, "%{public}s: end  %{public}d ms", this.taskName, Long.valueOf(longValue));
        }
    }
}
