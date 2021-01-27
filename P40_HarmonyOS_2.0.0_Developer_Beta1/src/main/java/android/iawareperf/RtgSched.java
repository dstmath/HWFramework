package android.iawareperf;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.os.TraceEx;
import com.huawei.android.os.TraceExt;
import com.huawei.android.util.SlogEx;

public class RtgSched extends DefaultRtgSchedImpl {
    private static final int CLICK_DELAY_TIME = SystemPropertiesEx.getInt("persist.sys.rtg.delay.click", 2000);
    public static final int CLICK_EVENT = 1;
    private static final int EVENT_SCENE_CHANGED = 5;
    private static final int FIRST_START_DELAY_TIME = SystemPropertiesEx.getInt("persist.sys.rtg.delay.start", 2000);
    private static final int FLING_STOP_DELAY_TIME = SystemPropertiesEx.getInt("persist.sys.flingstop.delay", 1000);
    public static final int FLING_STOP_EVENT = 4;
    private static final Object HANDLER_LOCK = new Object();
    private static final int MIN_QOS_RTG_ENABLE = 0;
    public static final int MOVE_EVENT = 2;
    private static final int MSG_CLICK_BEGIN = 2;
    private static final int MSG_FLING_BEGIN = 3;
    private static final int MSG_START_BEGIN = 1;
    public static final int NO_EVENT = 0;
    private static final int ON_MOTION_UP_DELAY_TIME = SystemPropertiesEx.getInt("persist.sys.onmotionup.delay", 5000);
    private static final int ON_MOVE_DELAY_TIME = SystemPropertiesEx.getInt("persist.sys.onmove.delay", 5000);
    private static final int RME_ENABLE = SystemPropertiesEx.getInt("hw.enableEOS", 0);
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
    private RtgSchedVideo mRtgSchedVideo = new RtgSchedVideo();

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
            synchronized (HANDLER_LOCK) {
                frameSchedClear();
                resetFlingState();
                if (this.mRtgImplObject.getRtgSchedEnable() <= 0) {
                    SlogEx.d(TAG, "resetRtgSchedHandle failed enable:" + enable);
                    this.mRtgImplObject = this.mRtgDummyObject;
                    this.mRtgSchedHandler = null;
                } else {
                    sFrameSchedEnable = true;
                    if (this.mRtgSchedHandler == null) {
                        this.mRtgSchedHandler = new RtgSchedHandle();
                    }
                    this.mRtgSchedHandler.sendEmptyMessageDelayed(1, (long) FIRST_START_DELAY_TIME);
                    this.mRtgSchedVideo.setRtgEnable(enable);
                }
            }
        }
    }

    public void markFrameSchedStart(int type) {
        synchronized (HANDLER_LOCK) {
            if (this.mRtgSchedHandler != null) {
                TraceExt.traceCounter(TraceEx.getTraceTagView(), "Schedstatus", type);
                if (type == 1) {
                    markFrameSchedStartClickEvent();
                } else if (type == 2) {
                    markFrameSchedStartMoveEvent();
                } else if (type == 3) {
                    markFrameSchedStartSlideEvent();
                } else if (type == 4) {
                    markFrameSchedStartFlingStopEvent();
                }
            }
        }
    }

    private void markFrameSchedStartClickEvent() {
        TraceEx.traceBegin(TraceEx.getTraceTagView(), "markFrameSchedStart");
        frameSchedClear();
        resetFlingState();
        sFrameSchedClicked = true;
        this.mRtgSchedVideo.clickBegin();
        this.mRtgSchedHandler.sendEmptyMessageDelayed(2, (long) CLICK_DELAY_TIME);
        TraceEx.traceEnd(TraceEx.getTraceTagView());
    }

    private void markFrameSchedStartMoveEvent() {
        if (RME_ENABLE != 0) {
            resetClickState();
            if (!sFrameSchedFling) {
                this.mRtgSchedHandler.removeMessages(3);
                sFrameSchedFling = true;
                this.mRtgSchedVideo.slideBegin();
                this.mRtgSchedHandler.sendEmptyMessageDelayed(3, (long) ON_MOVE_DELAY_TIME);
            }
        }
    }

    private void markFrameSchedStartSlideEvent() {
        if (RME_ENABLE != 0) {
            resetClickState();
            if (sFrameSchedFling) {
                this.mRtgSchedHandler.removeMessages(3);
            }
            sFrameSchedFling = true;
            this.mRtgSchedVideo.slideBegin();
            this.mRtgSchedHandler.sendEmptyMessageDelayed(3, (long) ON_MOTION_UP_DELAY_TIME);
        }
    }

    private void markFrameSchedStartFlingStopEvent() {
        if (RME_ENABLE != 0) {
            resetClickState();
            if (sFrameSchedFling) {
                this.mRtgSchedHandler.removeMessages(3);
            }
            sFrameSchedFling = true;
            this.mRtgSchedVideo.slideBegin();
            this.mRtgSchedHandler.sendEmptyMessageDelayed(3, (long) FLING_STOP_DELAY_TIME);
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
        this.mRtgSchedVideo.clickEnd();
    }

    private void resetFlingState() {
        RtgSchedHandle rtgSchedHandle = this.mRtgSchedHandler;
        if (rtgSchedHandle != null && sFrameSchedFling) {
            rtgSchedHandle.removeMessages(3);
            sFrameSchedFling = false;
            this.mRtgSchedVideo.slideEnd();
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
            this.mRtgSchedVideo.clickEnd();
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

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            TraceEx.traceBegin(TraceEx.getTraceTagView(), "markFrameSchedEnd");
            int i = msg.what;
            if (i == 1) {
                boolean unused = RtgSched.sFrameSchedEnable = false;
                if (RtgSched.this.mRtgImplObject != null) {
                    RtgSched.this.mRtgImplObject.endFreq();
                }
            } else if (i == 2) {
                boolean unused2 = RtgSched.sFrameSchedClicked = false;
                RtgSched.this.mRtgSchedVideo.clickEnd();
                if (RtgSched.this.mRtgImplObject != null) {
                    RtgSched.this.mRtgImplObject.endFreq();
                }
            } else if (i != 3) {
                SlogEx.d(RtgSched.TAG, "handleMessage err msg:" + msg.what);
            } else {
                TraceExt.traceCounter(TraceEx.getTraceTagView(), "Schedstatus", 0);
                boolean unused3 = RtgSched.sFrameSchedFling = false;
                RtgSched.this.mRtgSchedVideo.slideEnd();
                if (RtgSched.this.mRtgImplObject != null) {
                    RtgSched.this.mRtgImplObject.doFlingStop();
                }
            }
            TraceEx.traceEnd(TraceEx.getTraceTagView());
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

    private boolean checkVideoEnable() {
        if (!checkRtgFrame()) {
            return false;
        }
        return this.mRtgSchedVideo.isVideoEnable();
    }

    public void beginDoFrame(boolean enable) {
        if (enable && checkRtgFrameEnable()) {
            this.mRtgImplObject.beginDoFrame();
        }
        if (checkRmeFrameEnable() || checkVideoEnable()) {
            this.mRtgImplObject.doFrameStart();
        }
        if (checkRtgFrame()) {
            this.mRtgSchedVideo.doFrameStart();
        }
    }

    public void endDoFrame(boolean enable) {
        if (enable && checkRtgFrame()) {
            this.mRtgImplObject.endDoFrame();
        }
        if (checkRmeFrameEnable() || checkVideoEnable()) {
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
    }

    public void doAnimation() {
        if (checkRmeFrameEnable()) {
            this.mRtgImplObject.doAnimation();
        }
    }

    public void doFlingStart() {
    }

    public void doFlingStop() {
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

    private void sceneChanged(int scene) {
        RtgSchedIntf rtgSchedIntf;
        if (!sFrameSchedEnable && !sFrameSchedClicked && !sFrameSchedFling && scene == 0 && (rtgSchedIntf = this.mRtgImplObject) != null) {
            rtgSchedIntf.endFreq();
        }
    }

    public void sendMmEvent(int event, int value) {
        if (checkRtgFrame()) {
            if (event == 5) {
                sceneChanged(value);
            }
            this.mRtgImplObject.sendMmEvent(event, value);
        }
    }
}
