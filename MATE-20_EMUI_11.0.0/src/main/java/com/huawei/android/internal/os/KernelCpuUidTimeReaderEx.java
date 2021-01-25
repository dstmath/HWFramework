package com.huawei.android.internal.os;

import com.android.internal.os.KernelCpuUidTimeReader;

public class KernelCpuUidTimeReaderEx {

    public static class CallbackEx {
        private KernelCpuUidTimeReader.Callback mCallback = new KernelCpuUidTimeReader.Callback<long[]>() {
            /* class com.huawei.android.internal.os.KernelCpuUidTimeReaderEx.CallbackEx.AnonymousClass1 */

            public void onUidCpuTime(int uid, long[] time) {
                CallbackEx.this.onUidCpuTime(uid, time);
            }
        };

        public void onUidCpuTime(int uid, long[] time) {
        }

        public KernelCpuUidTimeReader.Callback getCallback() {
            return this.mCallback;
        }
    }

    public static class KernelCpuUidUserSysTimeReaderEx {
        private KernelCpuUidTimeReader.KernelCpuUidUserSysTimeReader mKernelCpuUidUserSysTimeReader;

        public KernelCpuUidUserSysTimeReaderEx(boolean throttle) {
            this.mKernelCpuUidUserSysTimeReader = new KernelCpuUidTimeReader.KernelCpuUidUserSysTimeReader(throttle);
        }

        public void readDelta(CallbackEx callbackEx) {
            this.mKernelCpuUidUserSysTimeReader.readDelta(callbackEx.getCallback());
        }
    }
}
