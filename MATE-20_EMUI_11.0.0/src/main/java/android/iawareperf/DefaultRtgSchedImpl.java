package android.iawareperf;

import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class DefaultRtgSchedImpl implements IHwRtgSchedImpl {
    private static final Object SLOCK = new Object();
    private static DefaultRtgSchedImpl sRtgSched;

    public static DefaultRtgSchedImpl getInstance() {
        DefaultRtgSchedImpl defaultRtgSchedImpl;
        synchronized (SLOCK) {
            if (sRtgSched == null) {
                sRtgSched = new DefaultRtgSchedImpl();
            }
            defaultRtgSchedImpl = sRtgSched;
        }
        return defaultRtgSchedImpl;
    }

    @Override // android.iawareperf.IHwRtgSchedImpl
    public void resetRtgSchedHandle(int enable) {
    }

    public void markFrameSchedStart(int type) {
    }

    @Override // android.iawareperf.IHwRtgSchedImpl
    public void beginDoFrame(boolean enable) {
    }

    @Override // android.iawareperf.IHwRtgSchedImpl
    public void endDoFrame(boolean enable) {
    }

    @Override // android.iawareperf.IHwRtgSchedImpl
    public void beginActivityTransaction() {
    }

    @Override // android.iawareperf.IHwRtgSchedImpl
    public void endActivityTransaction() {
    }

    @Override // android.iawareperf.IHwRtgSchedImpl
    public void beginDoTraversal() {
    }

    @Override // android.iawareperf.IHwRtgSchedImpl
    public void endDoTraversal() {
    }

    @Override // android.iawareperf.IHwRtgSchedImpl
    public void beginClickFreq() {
    }

    @Override // android.iawareperf.IHwRtgSchedImpl
    public void endClickFreq() {
    }

    @Override // android.iawareperf.IHwRtgSchedImpl
    public void doDeliverInput() {
    }

    @Override // android.iawareperf.IHwRtgSchedImpl
    public void doAnimation() {
    }

    @Override // android.iawareperf.IHwRtgSchedImpl
    public void doFlingStart() {
    }

    @Override // android.iawareperf.IHwRtgSchedImpl
    public void doFlingStop() {
    }

    @Override // android.iawareperf.IHwRtgSchedImpl
    public void doInflate() {
    }

    @Override // android.iawareperf.IHwRtgSchedImpl
    public void doObtainView() {
    }

    @Override // android.iawareperf.IHwRtgSchedImpl
    public void doMeasure() {
    }

    @Override // android.iawareperf.IHwRtgSchedImpl
    public void sendMmEvent(int event, int value) {
    }
}
