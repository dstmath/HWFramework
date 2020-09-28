package android.iawareperf;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.os.Trace;
import android.util.Slog;

public class RtgSched implements IHwRtgSchedImpl {
    private static final int CLICK_DELAY_TIME = SystemProperties.getInt("persist.sys.rtg.delay.click", 2000);
    public static final int CLICK_EVENT = 1;
    private static final int FIRST_START_DELAY_TIME = SystemProperties.getInt("persist.sys.rtg.delay.start", 2000);
    private static final int FLING_STOP_DELAY_TIME = SystemProperties.getInt("persist.sys.flingstop.delay", 1000);
    public static final int FLING_STOP_EVENT = 4;
    private static final int MIN_QOS_RTG_ENABLE = 0;
    public static final int MOVE_EVENT = 2;
    private static final int MSG_CLICK_BEGIN = 2;
    private static final int MSG_FLING_BEGIN = 3;
    private static final int MSG_START_BEGIN = 1;
    public static final int NO_EVENT = 0;
    private static final int ON_MOTION_UP_DELAY_TIME = SystemProperties.getInt("persist.sys.onmotionup.delay", 5000);
    private static final int ON_MOVE_DELAY_TIME = SystemProperties.getInt("persist.sys.onmove.delay", 5000);
    private static final int RME_ENABLE = SystemProperties.getInt("hw.enableEOS", 0);
    public static final int SLIDE_EVENT = 3;
    private static final Object SLOCK = new Object();
    private static final String TAG = "RtgSched";
    private static boolean sFrameSchedClicked = false;
    private static boolean sFrameSchedEnable = false;
    private static boolean sFrameSchedFling = false;
    private static RtgSched sRtgSched;
    private RtgSchedIntf mRtgDummyObject = new RtgSchedDummyImpl();
    private RtgSchedIntf mRtgImplObject = null;
    private RtgSchedHandle mRtgSchedHandler = null;

    private RtgSched() {
    }

    public static RtgSched getInstance() {
        RtgSched rtgSched;
        synchronized (SLOCK) {
            if (sRtgSched == null) {
                sRtgSched = new RtgSched();
            }
            rtgSched = sRtgSched;
        }
        return rtgSched;
    }

    public void resetRtgSchedHandle(int enable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            this.mRtgImplObject = RtgSchedImpl.getInstance();
            if (enable == 1) {
                this.mRtgImplObject.init();
            }
            frameSchedClear();
            resetFlingState();
            if (this.mRtgImplObject.getRtgSchedEnable() <= 0) {
                Slog.d(TAG, "resetRtgSchedHandle failed enable:" + enable);
                this.mRtgImplObject = this.mRtgDummyObject;
                this.mRtgSchedHandler = null;
                return;
            }
            sFrameSchedEnable = true;
            if (this.mRtgSchedHandler == null) {
                this.mRtgSchedHandler = new RtgSchedHandle();
            }
            this.mRtgSchedHandler.sendEmptyMessageDelayed(1, (long) FIRST_START_DELAY_TIME);
        }
    }

    public void markFrameSchedStart(int type) {
        if (this.mRtgSchedHandler != null) {
            Trace.traceCounter(8, "Schedstatus", type);
            if (type == 1) {
                Trace.traceBegin(8, "markFrameSchedStart");
                frameSchedClear();
                resetFlingState();
                sFrameSchedClicked = true;
                this.mRtgSchedHandler.sendEmptyMessageDelayed(2, (long) CLICK_DELAY_TIME);
                Trace.traceEnd(8);
            } else if (type != 2) {
                if (type != 3) {
                    if (type == 4 && RME_ENABLE != 0) {
                        Trace.traceBegin(8, "flingstopevent");
                        resetClickState();
                        if (sFrameSchedFling) {
                            this.mRtgSchedHandler.removeMessages(3);
                        }
                        sFrameSchedFling = true;
                        this.mRtgSchedHandler.sendEmptyMessageDelayed(3, (long) FLING_STOP_DELAY_TIME);
                        Trace.traceEnd(8);
                    }
                } else if (RME_ENABLE != 0) {
                    Trace.traceBegin(8, "onMotionupSlide");
                    resetClickState();
                    if (sFrameSchedFling) {
                        this.mRtgSchedHandler.removeMessages(3);
                    }
                    sFrameSchedFling = true;
                    this.mRtgSchedHandler.sendEmptyMessageDelayed(3, (long) ON_MOTION_UP_DELAY_TIME);
                    Trace.traceEnd(8);
                }
            } else if (RME_ENABLE != 0) {
                Trace.traceBegin(8, "onMove");
                resetClickState();
                if (!sFrameSchedFling) {
                    this.mRtgSchedHandler.removeMessages(3);
                    sFrameSchedFling = true;
                    this.mRtgSchedHandler.sendEmptyMessageDelayed(3, (long) ON_MOVE_DELAY_TIME);
                }
                Trace.traceEnd(8);
            }
        }
    }

    private void frameSchedClear() {
        RtgSchedHandle rtgSchedHandle = this.mRtgSchedHandler;
        if (rtgSchedHandle != null) {
            rtgSchedHandle.removeMessages(1);
            this.mRtgSchedHandler.removeMessages(2);
        }
        sFrameSchedEnable = false;
        sFrameSchedClicked = false;
    }

    private void resetFlingState() {
        RtgSchedHandle rtgSchedHandle = this.mRtgSchedHandler;
        if (rtgSchedHandle != null && sFrameSchedFling) {
            rtgSchedHandle.removeMessages(3);
            sFrameSchedFling = false;
            RtgSchedIntf rtgSchedIntf = this.mRtgImplObject;
            if (rtgSchedIntf != null) {
                rtgSchedIntf.doFlingStop();
            }
        }
    }

    private void resetClickState() {
        RtgSchedHandle rtgSchedHandle = this.mRtgSchedHandler;
        if (rtgSchedHandle != null && sFrameSchedClicked) {
            rtgSchedHandle.removeMessages(2);
            sFrameSchedClicked = false;
            RtgSchedIntf rtgSchedIntf = this.mRtgImplObject;
            if (rtgSchedIntf != null) {
                rtgSchedIntf.endFreq();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public class RtgSchedHandle extends Handler {
        RtgSchedHandle() {
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Trace.traceBegin(8, "markFrameSchedEnd");
            int i = msg.what;
            if (i == 1) {
                boolean unused = RtgSched.sFrameSchedEnable = false;
                if (RtgSched.this.mRtgImplObject != null) {
                    RtgSched.this.mRtgImplObject.endFreq();
                }
            } else if (i == 2) {
                boolean unused2 = RtgSched.sFrameSchedClicked = false;
                if (RtgSched.this.mRtgImplObject != null) {
                    RtgSched.this.mRtgImplObject.endFreq();
                }
            } else if (i != 3) {
                Slog.d(RtgSched.TAG, "handleMessage err msg:" + msg.what);
            } else {
                Trace.traceCounter(8, "Schedstatus", 0);
                boolean unused3 = RtgSched.sFrameSchedFling = false;
                if (RtgSched.this.mRtgImplObject != null) {
                    RtgSched.this.mRtgImplObject.doFlingStop();
                }
            }
            Trace.traceEnd(8);
        }
    }

    private boolean checkRtgFrame() {
        if (this.mRtgImplObject == null) {
            this.mRtgImplObject = this.mRtgDummyObject;
        }
        if (this.mRtgImplObject != null && Looper.myLooper() == Looper.getMainLooper()) {
            return true;
        }
        return false;
    }

    private boolean checkRtgFrameEnable() {
        if (!checkRtgFrame()) {
            return false;
        }
        if (sFrameSchedEnable || sFrameSchedClicked) {
            return true;
        }
        return false;
    }

    private boolean checkRmeFrameEnable() {
        if (!checkRtgFrame()) {
            return false;
        }
        return sFrameSchedFling;
    }

    public void beginDoFrame(boolean enable) {
        if (enable && checkRtgFrameEnable()) {
            this.mRtgImplObject.beginDoFrame();
        }
        if (checkRmeFrameEnable()) {
            this.mRtgImplObject.doFrameStart();
        }
    }

    public void endDoFrame(boolean enable) {
        if (enable && checkRtgFrame()) {
            this.mRtgImplObject.endDoFrame();
        }
        if (checkRmeFrameEnable()) {
            this.mRtgImplObject.doFrameEnd();
        }
    }

    public void beginActivityTransaction() {
        if (checkRtgFrameEnable()) {
            this.mRtgImplObject.beginActivityTransaction();
        }
    }

    public void endActivityTransaction() {
        if (checkRtgFrame()) {
            this.mRtgImplObject.endActivityTransaction();
        }
    }

    public void beginDoTraversal() {
        if (checkRtgFrameEnable()) {
            this.mRtgImplObject.beginDoTraversal();
        }
        if (checkRmeFrameEnable()) {
            this.mRtgImplObject.doTraversalStart();
        }
    }

    public void endDoTraversal() {
        if (checkRtgFrame()) {
            this.mRtgImplObject.endDoTraversal();
        }
        if (checkRmeFrameEnable()) {
            this.mRtgImplObject.doTraversalEnd();
        }
    }

    public void beginClickFreq() {
        if (checkRtgFrameEnable()) {
            this.mRtgImplObject.beginClickFreq();
        }
    }

    public void endClickFreq() {
        if (checkRtgFrame()) {
            this.mRtgImplObject.endClickFreq();
        }
    }

    public void doDeliverInput() {
        Trace.traceBegin(8, "DeliverInput");
        Trace.traceEnd(8);
    }

    public void doAnimation() {
        if (checkRmeFrameEnable()) {
            this.mRtgImplObject.doAnimation();
        }
    }

    public void doFlingStart() {
        Trace.traceBegin(8, "FlingStart");
        Trace.traceEnd(8);
    }

    public void doFlingStop() {
        if (checkRmeFrameEnable()) {
            markFrameSchedStart(4);
        }
    }

    public void doInflate() {
        if (checkRmeFrameEnable()) {
            this.mRtgImplObject.doInflate();
        }
    }

    public void doObtainView() {
        if (checkRmeFrameEnable()) {
            this.mRtgImplObject.doObtainView();
        }
    }

    public void doMeasure() {
        if (checkRmeFrameEnable()) {
            this.mRtgImplObject.doMeasure();
        }
    }

    public void sendMmEvent(int event, int value) {
        if (checkRtgFrame()) {
            this.mRtgImplObject.sendMmEvent(event, value);
        }
    }
}
