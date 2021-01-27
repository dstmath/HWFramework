package ohos.agp.vsync;

import android.app.ActivityManager;
import android.os.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import ohos.aafwk.utils.log.LogDomain;
import ohos.agp.vsync.VsyncScheduler;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class VsyncSchedulerNativeAdapter {
    private static final VsyncScheduler.FrameCallback FRAME_CALLBACK = $$Lambda$VsyncSchedulerNativeAdapter$N0MovFYA__Hlb3IbI3CuC9wmKsU.INSTANCE;
    private static final ArrayList<Long> NATIVE_OBJECTS = new ArrayList<>();
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogDomain.END, "VsyncSchedulerNativeAdapter");

    private static native void nativeOnVsync(long j, long j2);

    static /* synthetic */ void lambda$static$0(long j) {
        ArrayList arrayList;
        synchronized (NATIVE_OBJECTS) {
            arrayList = new ArrayList(NATIVE_OBJECTS);
            NATIVE_OBJECTS.clear();
        }
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            nativeOnVsync(((Long) it.next()).longValue(), j);
        }
    }

    private static void requestVsync(long j) {
        boolean isEmpty;
        synchronized (NATIVE_OBJECTS) {
            isEmpty = NATIVE_OBJECTS.isEmpty();
            if (!NATIVE_OBJECTS.contains(Long.valueOf(j))) {
                NATIVE_OBJECTS.add(Long.valueOf(j));
            }
        }
        if (isEmpty) {
            VsyncScheduler.getInstance().lambda$postRequestVsync$1$VsyncScheduler(FRAME_CALLBACK);
        }
    }

    private static void postRenderThreadInfo(int i, int i2) {
        try {
            ActivityManager.getService().setHmThreadToRtg("mode:add;tids:" + i2);
        } catch (RemoteException unused) {
            HiLog.error(TAG, "setHmThreadToRtg failed!", new Object[0]);
        }
    }
}
