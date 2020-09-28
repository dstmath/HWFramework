package android.iawareperf;

public class RtgSchedImpl implements RtgSchedIntf {
    private static final int PERFORM_CLICK_TYPE = 1;
    private static final int PERFORM_END_TYPE = 2;
    private static final Object SLOCK = new Object();
    private static final String TAG = "RtgSchedImpl";
    private static RtgSchedImpl sInstance;
    private long mNativeProxy = 0;

    private native void nBeginActivityTransaction(long j);

    private native void nBeginDoFrame(long j);

    private native void nBeginDoTraversal(long j);

    private native void nBeginEventFreq(long j, int i);

    private native long nCreateProxy();

    private native void nDeleteProxy(long j);

    private native void nDoAnimation(long j);

    private native void nDoDeliverInput(long j);

    private native void nDoFlingStart(long j);

    private native void nDoFlingStop(long j);

    private native void nDoInflate(long j);

    private native void nDoMeasure(long j);

    private native void nDoObtainView(long j);

    private native void nDoScrollBeginDoFrame(long j);

    private native void nDoScrollEndDoFrame(long j);

    private native void nDoTraversalEnd(long j);

    private native void nDoTraversalStart(long j);

    private native void nEndActivityTransaction(long j);

    private native void nEndDoFrame(long j);

    private native void nEndDoTraversal(long j);

    private native void nEndEventFreq(long j, int i);

    private native int nGetRtgSchedEnable(long j);

    private native void nSendMmEvent(long j, int i, int i2);

    private native void nSetFreqParam(long j, String str);

    private native void nSetRtgFreqEnable(long j, int i, String str);

    private native void nSetRtgThread(long j, String str);

    private RtgSchedImpl() {
    }

    public static RtgSchedImpl getInstance() {
        RtgSchedImpl rtgSchedImpl;
        synchronized (SLOCK) {
            if (sInstance == null) {
                sInstance = new RtgSchedImpl();
            }
            rtgSchedImpl = sInstance;
        }
        return rtgSchedImpl;
    }

    @Override // android.iawareperf.RtgSchedIntf
    public void init() {
        synchronized (SLOCK) {
            if (this.mNativeProxy == 0) {
                this.mNativeProxy = nCreateProxy();
            }
        }
    }

    @Override // android.iawareperf.RtgSchedIntf
    public int getRtgSchedEnable() {
        synchronized (SLOCK) {
            if (this.mNativeProxy == 0) {
                this.mNativeProxy = nCreateProxy();
                if (this.mNativeProxy == 0) {
                    return 0;
                }
            }
            return nGetRtgSchedEnable(this.mNativeProxy);
        }
    }

    @Override // android.iawareperf.RtgSchedIntf
    public void beginDoFrame() {
        nBeginDoFrame(this.mNativeProxy);
    }

    @Override // android.iawareperf.RtgSchedIntf
    public void endDoFrame() {
        nEndDoFrame(this.mNativeProxy);
    }

    @Override // android.iawareperf.RtgSchedIntf
    public void beginDoTraversal() {
        nBeginDoTraversal(this.mNativeProxy);
    }

    @Override // android.iawareperf.RtgSchedIntf
    public void doTraversalStart() {
        nDoTraversalStart(this.mNativeProxy);
    }

    @Override // android.iawareperf.RtgSchedIntf
    public void endDoTraversal() {
        nEndDoTraversal(this.mNativeProxy);
    }

    @Override // android.iawareperf.RtgSchedIntf
    public void doTraversalEnd() {
        nDoTraversalEnd(this.mNativeProxy);
    }

    @Override // android.iawareperf.RtgSchedIntf
    public void beginClickFreq() {
        nBeginEventFreq(this.mNativeProxy, 1);
    }

    @Override // android.iawareperf.RtgSchedIntf
    public void endClickFreq() {
        nEndEventFreq(this.mNativeProxy, 1);
    }

    @Override // android.iawareperf.RtgSchedIntf
    public void beginActivityTransaction() {
        nBeginActivityTransaction(this.mNativeProxy);
    }

    @Override // android.iawareperf.RtgSchedIntf
    public void endActivityTransaction() {
        nEndActivityTransaction(this.mNativeProxy);
    }

    @Override // android.iawareperf.RtgSchedIntf
    public void setRtgFreqEnable(int enable, String configs) {
        long j = this.mNativeProxy;
        if (j != 0) {
            nSetRtgFreqEnable(j, enable, configs);
        }
    }

    @Override // android.iawareperf.RtgSchedIntf
    public void setFreqParam(String params) {
        long j = this.mNativeProxy;
        if (j != 0) {
            nSetFreqParam(j, params);
        }
    }

    @Override // android.iawareperf.RtgSchedIntf
    public void setRtgThread(String threads) {
        long j = this.mNativeProxy;
        if (j != 0) {
            nSetRtgThread(j, threads);
        }
    }

    @Override // android.iawareperf.RtgSchedIntf
    public void deInit() {
        synchronized (SLOCK) {
            if (this.mNativeProxy == 0) {
                nDeleteProxy(this.mNativeProxy);
                this.mNativeProxy = 0;
            }
        }
    }

    @Override // android.iawareperf.RtgSchedIntf
    public void endFreq() {
        nEndEventFreq(this.mNativeProxy, 2);
    }

    @Override // android.iawareperf.RtgSchedIntf
    public void doFrameStart() {
        nDoScrollBeginDoFrame(this.mNativeProxy);
    }

    @Override // android.iawareperf.RtgSchedIntf
    public void doFrameEnd() {
        nDoScrollEndDoFrame(this.mNativeProxy);
    }

    @Override // android.iawareperf.RtgSchedIntf
    public void doDeliverInput() {
        nDoDeliverInput(this.mNativeProxy);
    }

    @Override // android.iawareperf.RtgSchedIntf
    public void doAnimation() {
        nDoAnimation(this.mNativeProxy);
    }

    @Override // android.iawareperf.RtgSchedIntf
    public void doFlingStart() {
        nDoFlingStart(this.mNativeProxy);
    }

    @Override // android.iawareperf.RtgSchedIntf
    public void doFlingStop() {
        nDoFlingStop(this.mNativeProxy);
    }

    @Override // android.iawareperf.RtgSchedIntf
    public void doInflate() {
        nDoInflate(this.mNativeProxy);
    }

    @Override // android.iawareperf.RtgSchedIntf
    public void doObtainView() {
        nDoObtainView(this.mNativeProxy);
    }

    @Override // android.iawareperf.RtgSchedIntf
    public void doMeasure() {
        nDoMeasure(this.mNativeProxy);
    }

    @Override // android.iawareperf.RtgSchedIntf
    public void sendMmEvent(int event, int value) {
        nSendMmEvent(this.mNativeProxy, event, value);
    }
}
