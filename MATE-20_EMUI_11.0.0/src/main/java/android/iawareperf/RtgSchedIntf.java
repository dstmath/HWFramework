package android.iawareperf;

public interface RtgSchedIntf {
    void beginActivityTransaction();

    void beginClickFreq();

    void beginDoFrame();

    void beginDoTraversal();

    void deInit();

    void doAnimation();

    void doDeliverInput();

    void doFlingStart();

    void doFlingStop();

    void doFrameEnd();

    void doFrameStart();

    void doInflate();

    void doMeasure();

    void doObtainView();

    void doTraversalEnd();

    void doTraversalStart();

    void endActivityTransaction();

    void endClickFreq();

    void endDoFrame();

    void endDoTraversal();

    void endFreq();

    int getRtgSchedEnable();

    void init();

    void sendMmEvent(int i, int i2);

    void setFreqParam(String str);

    void setRtgFreqEnable(int i, String str);

    void setRtgThread(String str);
}
