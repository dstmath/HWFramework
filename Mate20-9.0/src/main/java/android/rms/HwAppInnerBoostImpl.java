package android.rms;

import android.iawareperf.UniPerf;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.rms.iaware.IAwareSdk;
import android.util.Log;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.MotionEvent;

public class HwAppInnerBoostImpl implements IHwAppInnerBoost {
    private static final boolean BOOST_FLAG;
    private static final int DOWN_INTERVAL = 2000;
    private static final int LONG_SLIDE_BOOST_DURATION = 15000;
    private static final int LONG_SLIDE_THRESHOLD = 20;
    private static final int MAX_MOVE_COUNT = 20;
    private static final float MOVE_DISTANCE = 24.0f;
    private static final String MSG_TYPE_JITTER = "2";
    private static final String MSG_TYPE_TRAVERSAL = "1";
    private static final int REPORT_DURATION_CLICK = SystemProperties.getInt("persist.sys.iaware.appboost.click_duration", 1000);
    private static final int REPORT_DURATION_SLIDE = SystemProperties.getInt("persist.sys.iaware.appboost.slide_duration", 5000);
    private static final int REPORT_INTERVAL = 250;
    private static final int REPORT_TIMES_CLICK = SystemProperties.getInt("persist.sys.iaware.appboost.click_times", 3);
    private static final int REPORT_TIMES_SLIDE = SystemProperties.getInt("persist.sys.iaware.appboost.slide_times", 16);
    private static final int SCENE_TYPE_CLICK = 2;
    private static final int SCENE_TYPE_INVALID = 0;
    private static final int SCENE_TYPE_SLIDE = 1;
    private static final int SKIPPED_FRAMES = 3;
    private static final float SLIGHT_MOVE_DISTANCE = 15.0f;
    private static final String TAG = "HwAppInnerBoostImpl";
    private static volatile HwAppInnerBoostImpl instance = null;
    private boolean canLongSlideBoost = false;
    private float downX = 0.0f;
    private float downY = 0.0f;
    private boolean initialized = false;
    private int jitterReportTimes = 0;
    private long lastDownTime = 0;
    private long lastJitterReportTime = 0;
    private long lastLongSlideBoostTime = 0;
    private float lastMoveY = 0.0f;
    private long lastTraversalReportTime = 0;
    private int moveCount = 0;
    private String packageName = null;
    private int reportDuration = 0;
    private int reportTimes = 0;
    private int sceneType = 0;
    private int traversalReportTimes = 0;

    static {
        boolean z = false;
        if (SystemProperties.getBoolean("persist.sys.enable_iaware", false) && SystemProperties.getBoolean("persist.sys.iaware.appboost.switch", false)) {
            z = true;
        }
        BOOST_FLAG = z;
    }

    public static HwAppInnerBoostImpl getDefault() {
        if (instance == null) {
            synchronized (HwAppInnerBoostImpl.class) {
                if (instance == null) {
                    instance = new HwAppInnerBoostImpl();
                }
            }
        }
        return instance;
    }

    public void initialize(String packageName2) {
        if (!this.initialized) {
            this.initialized = true;
            this.packageName = packageName2;
            Log.d(TAG, "set config for " + packageName2 + " BOOST_FLAG=" + BOOST_FLAG + " REPORT_DURATION_CLICK=" + REPORT_DURATION_CLICK + " REPORT_TIMES_CLICK=" + REPORT_TIMES_CLICK + " REPORT_DURATION_SLIDE=" + REPORT_DURATION_SLIDE + " REPORT_TIMES_SLIDE=" + REPORT_TIMES_SLIDE);
        }
    }

    public void onInputEvent(InputEvent e) {
        if (BOOST_FLAG) {
            long now = SystemClock.uptimeMillis();
            if (e instanceof MotionEvent) {
                onMotionEvent((MotionEvent) e, now);
            } else if (e instanceof KeyEvent) {
                onKeyEvent((KeyEvent) e, now);
            }
        }
    }

    public void onTraversal() {
        if (BOOST_FLAG) {
            if (this.sceneType >= 1 || this.canLongSlideBoost) {
                long now = SystemClock.uptimeMillis();
                if (this.canLongSlideBoost) {
                    this.lastLongSlideBoostTime = now;
                    UniPerf.getInstance().uniPerfEvent(4107, "", new int[]{0});
                    this.canLongSlideBoost = false;
                } else if (now - this.lastDownTime > ((long) this.reportDuration) || this.traversalReportTimes >= this.reportTimes) {
                    this.sceneType = 0;
                } else if (now - this.lastTraversalReportTime >= 250) {
                    this.lastTraversalReportTime = now;
                    this.traversalReportTimes++;
                    asyncReport(now, "1", this.traversalReportTimes, 0);
                }
            }
        }
    }

    public void onJitter(long skippedFrames) {
        if (BOOST_FLAG && this.sceneType >= 1 && skippedFrames >= 3) {
            long now = SystemClock.uptimeMillis();
            if (now - this.lastDownTime > ((long) this.reportDuration) || this.jitterReportTimes >= this.reportTimes) {
                this.sceneType = 0;
            } else if (now - this.lastJitterReportTime >= 250) {
                this.lastJitterReportTime = now;
                this.jitterReportTimes++;
                asyncReport(now, MSG_TYPE_JITTER, this.jitterReportTimes, skippedFrames);
            }
        }
    }

    private void onMotionEvent(MotionEvent event, long now) {
        switch (event.getActionMasked()) {
            case 0:
                onDown(now, event.getX(), event.getY());
                return;
            case 1:
                onMotionUp(now, event);
                return;
            case 2:
                onMove(now, event.getX(), event.getY());
                return;
            default:
                return;
        }
    }

    private void onKeyEvent(KeyEvent event, long now) {
        switch (event.getAction()) {
            case 0:
                onDown(now, 0.0f, 0.0f);
                return;
            case 1:
                onKeyUp(now, event);
                return;
            default:
                return;
        }
    }

    private void onDown(long now, float x, float y) {
        this.moveCount = 0;
        this.canLongSlideBoost = false;
        if (now - this.lastDownTime >= 2000) {
            this.downX = x;
            this.downY = y;
            this.lastMoveY = this.downY;
            this.traversalReportTimes = 0;
            this.jitterReportTimes = 0;
            this.reportDuration = 0;
            this.reportTimes = 0;
            this.lastDownTime = now;
            this.lastLongSlideBoostTime = 0;
            this.sceneType = 0;
        }
    }

    private void onMove(long now, float x, float y) {
        if (isMovedSlight(this.lastMoveY, y)) {
            this.moveCount++;
            if (this.moveCount > 20 && now - this.lastDownTime < 15000 && (this.lastLongSlideBoostTime == 0 || now - this.lastLongSlideBoostTime > 250)) {
                this.canLongSlideBoost = true;
                this.sceneType = 0;
            }
        }
        this.lastMoveY = y;
    }

    private void onMotionUp(long now, MotionEvent event) {
        if (this.sceneType != 1) {
            if (isMoved(this.downX, this.downY, event.getX(), event.getY())) {
                this.sceneType = 1;
                this.reportDuration = REPORT_DURATION_SLIDE;
                this.reportTimes = REPORT_TIMES_SLIDE;
            } else {
                this.sceneType = 2;
                this.reportDuration = REPORT_DURATION_CLICK;
                this.reportTimes = REPORT_TIMES_CLICK;
            }
            this.canLongSlideBoost = false;
        }
    }

    private void onKeyUp(long now, KeyEvent event) {
        if (4 != event.getKeyCode()) {
            this.sceneType = 2;
            this.reportDuration = REPORT_DURATION_CLICK;
            this.reportTimes = REPORT_TIMES_CLICK;
            return;
        }
        this.sceneType = 0;
    }

    private void asyncReport(long frameTime, String msgType, int frameTimes, long skippedFrames) {
        StringBuilder message = new StringBuilder();
        message.append(this.packageName);
        message.append(',');
        message.append(this.sceneType);
        message.append(',');
        message.append(msgType);
        message.append(',');
        message.append(frameTimes);
        message.append(',');
        message.append(skippedFrames);
        IAwareSdk.asyncReportData(3033, message.toString(), frameTime);
        Log.d(TAG, "asyncReportData " + message + " interval=" + (frameTime - this.lastDownTime));
    }

    private boolean isMoved(float x1, float y1, float x2, float y2) {
        return Math.abs(x1 - x2) > MOVE_DISTANCE || Math.abs(y1 - y2) > MOVE_DISTANCE;
    }

    private boolean isMovedSlight(float y1, float y2) {
        return Math.abs(y1 - y2) > SLIGHT_MOVE_DISTANCE;
    }
}
