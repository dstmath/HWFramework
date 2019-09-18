package com.huawei.emui.hiexperience.hwperf.threadpool;

import android.content.Context;
import android.os.Build;
import com.huawei.emui.hiexperience.hwperf.HwPerfBase;
import com.huawei.emui.hiexperience.hwperf.utils.HwPerfLog;
import java.io.File;
import java.io.FileFilter;
import java.util.regex.Pattern;

public class HwPerfThreadPoolSize extends HwPerfBase {
    private static int DEVICEINFO_UNKNOWN = -1;
    private final int DEFAULT_CORE_SIZE = 8;
    private final int EFFECTIVE_CORE_SIZE = 1;
    private int PoolSize = -1;
    private final int SHUT_DOWN_THREAD_POOL_SIZE = -1;
    private String TAG = "HwPerfThreadPoolSize";
    private int mPoolSizeValue = -1;

    class FilterCpu implements FileFilter {
        FilterCpu() {
        }

        public boolean accept(File pathcpu) {
            if (Pattern.matches("cpu[0-9]", pathcpu.getName())) {
                return true;
            }
            return false;
        }
    }

    public HwPerfThreadPoolSize(Context context) {
    }

    private int GetCoreSize() {
        if (Build.VERSION.SDK_INT <= 10) {
            return DEVICEINFO_UNKNOWN;
        }
        try {
            return new File("/sys/devices/system/cpu/").listFiles(new FilterCpu()).length;
        } catch (Exception e) {
            HwPerfLog.e(this.TAG, ",PoolSize: Failed.");
            return DEVICEINFO_UNKNOWN;
        }
    }

    public int HwPerfGetPoolSize() {
        if (this.mPoolSizeValue == -1) {
            HwPerfLog.i(this.TAG, "shut down thread pool size optimation!");
            return -1;
        } else if (this.mPoolSizeValue >= 1) {
            String str = this.TAG;
            HwPerfLog.i(str, " thread pool size is effective, the core number mPoolSizeValue is: " + this.mPoolSizeValue);
            return this.mPoolSizeValue;
        } else if (this.PoolSize > 1) {
            String str2 = this.TAG;
            HwPerfLog.i(str2, " thread pool size is effective, the core number PoolSize is: " + this.PoolSize);
            return this.PoolSize;
        } else {
            HwPerfLog.i(this.TAG, " thread pool size is effective, the core number is DEFAULT_CORE_SIZE 8 ");
            return 8;
        }
    }

    public void setPoolSize(int poolsize) {
        this.mPoolSizeValue = poolsize;
        if (this.mPoolSizeValue > 0) {
            this.PoolSize = this.mPoolSizeValue;
            return;
        }
        this.PoolSize = GetCoreSize() * 2;
        String str = this.TAG;
        HwPerfLog.d(str, ",CorelSize:" + (this.PoolSize / 2));
    }
}
