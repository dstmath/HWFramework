package tmsdk.common.utils;

import tmsdk.common.TMSDKContext;
import tmsdkobf.fj;
import tmsdkobf.fs;
import tmsdkobf.ki;
import tmsdkobf.kv;

public class s {
    private static long nd = 0;

    /* JADX WARNING: Missing block: B:9:0x0045, code:
            return;
     */
    /* JADX WARNING: Missing block: B:12:0x004f, code:
            if ((tmsdkobf.gf.S().ak() & r12) == 0) goto L_0x0044;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static synchronized void bW(int i) {
        Object obj = 1;
        synchronized (s.class) {
            kv.d("WakeupUtil", "1]wakeup-flag:[" + i + "]");
            if (i != -1) {
            }
            final boolean[] zArr = new boolean[]{true};
            if (i != -1) {
                zArr[0] = false;
                long currentTimeMillis = System.currentTimeMillis();
                if (currentTimeMillis - nd < 3540000) {
                    obj = null;
                }
                if (obj == null) {
                    return;
                }
                nd = currentTimeMillis;
            }
            ((ki) fj.D(4)).addTask(new Runnable() {
                public void run() {
                    kv.d("WakeupUtil", "processWakeLogicSync");
                    fs.c(TMSDKContext.getApplicaionContext()).c(zArr[0]);
                }
            }, "checkStart");
        }
    }
}
