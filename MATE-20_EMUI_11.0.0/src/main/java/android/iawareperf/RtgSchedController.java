package android.iawareperf;

public class RtgSchedController {
    private static final String TAG = "RtgSchedController";
    private RtgSchedIntf mRtgImplObject = RtgSchedImpl.getInstance();

    public void init() {
        RtgSchedIntf rtgSchedIntf = this.mRtgImplObject;
        if (rtgSchedIntf != null) {
            rtgSchedIntf.init();
        }
    }

    public void setRtgFreqEnable(int enable, String configs) {
        RtgSchedIntf rtgSchedIntf = this.mRtgImplObject;
        if (rtgSchedIntf != null) {
            rtgSchedIntf.setRtgFreqEnable(enable, configs);
        }
    }

    public void setFreqParam(String params) {
        RtgSchedIntf rtgSchedIntf = this.mRtgImplObject;
        if (rtgSchedIntf != null) {
            rtgSchedIntf.setFreqParam(params);
        }
    }

    public void setRtgThread(String threads) {
        RtgSchedIntf rtgSchedIntf = this.mRtgImplObject;
        if (rtgSchedIntf != null) {
            rtgSchedIntf.setRtgThread(threads);
        }
    }

    public void deInit() {
        RtgSchedIntf rtgSchedIntf = this.mRtgImplObject;
        if (rtgSchedIntf != null) {
            rtgSchedIntf.deInit();
        }
    }
}
