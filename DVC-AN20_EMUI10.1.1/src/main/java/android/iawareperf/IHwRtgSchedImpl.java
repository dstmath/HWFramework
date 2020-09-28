package android.iawareperf;

public interface IHwRtgSchedImpl {
    void beginActivityTransaction();

    void beginClickFreq();

    void beginDoFrame(boolean z);

    void beginDoTraversal();

    void doAnimation();

    void doDeliverInput();

    void doFlingStart();

    void doFlingStop();

    void doInflate();

    void doMeasure();

    void doObtainView();

    void endActivityTransaction();

    void endClickFreq();

    void endDoFrame(boolean z);

    void endDoTraversal();

    void resetRtgSchedHandle(int i);

    void sendMmEvent(int i, int i2);
}
