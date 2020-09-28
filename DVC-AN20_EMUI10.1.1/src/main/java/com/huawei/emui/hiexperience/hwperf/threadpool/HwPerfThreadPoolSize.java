package com.huawei.emui.hiexperience.hwperf.threadpool;

import android.content.Context;
import android.os.Build;
import com.huawei.emui.hiexperience.hwperf.HwPerfBase;
import com.huawei.emui.hiexperience.hwperf.utils.HwLog;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.regex.Pattern;

public class HwPerfThreadPoolSize extends HwPerfBase {
    private static final int DEFAULT_CORE_SIZE = 8;
    private static final int GAIN_DEFAUTL = 2;
    private static final int POOL_SIZE_MAX = 16;
    private static final int POOL_SIZE_MIN = 1;
    private static final int POOL_SIZE_UNNORMAL = -1;
    private static final String TAG = "HwPerfThreadPoolSize";
    private int mPoolSize = DEFAULT_CORE_SIZE;

    public HwPerfThreadPoolSize(Context context) {
    }

    private int getCoreSize() throws IOException {
        File[] cpufiles;
        if (10 < Build.VERSION.SDK_INT && (cpufiles = new File("/sys/devices/system/cpu/").listFiles(new FileFilter() {
            /* class com.huawei.emui.hiexperience.hwperf.threadpool.HwPerfThreadPoolSize.AnonymousClass1 */

            public boolean accept(File pathcpu) {
                return Pattern.matches("cpu[0-9]", pathcpu.getName());
            }
        })) != null) {
            return cpufiles.length;
        }
        return POOL_SIZE_UNNORMAL;
    }

    public int HwPerfGetPoolSize() {
        return this.mPoolSize;
    }

    public void setPoolSize(int poolSize) {
        if (poolSize > POOL_SIZE_MAX || poolSize < 1) {
            try {
                int poolSizeTmp = getCoreSize() * 2;
                this.mPoolSize = poolSizeTmp >= 1 ? poolSizeTmp : DEFAULT_CORE_SIZE;
            } catch (IOException e) {
                this.mPoolSize = DEFAULT_CORE_SIZE;
                HwLog.e(TAG, ",PoolSize: Failed.");
            }
        } else {
            this.mPoolSize = poolSize;
        }
    }
}
