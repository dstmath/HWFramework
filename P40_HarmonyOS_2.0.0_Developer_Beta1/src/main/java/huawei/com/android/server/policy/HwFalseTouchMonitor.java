package huawei.com.android.server.policy;

import android.os.Handler;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.MotionEvent;
import com.android.server.gesture.GestureNavConst;
import com.android.server.policy.DefaultHwFalseTouchMonitor;
import com.android.server.policy.HwPhoneWindowManager;
import com.android.server.policy.PhoneWindowManagerEx;
import com.android.server.policy.WindowManagerPolicyEx;
import com.huawei.android.fsm.HwFoldScreenManagerEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.util.IMonitorExt;
import com.huawei.android.view.DisplayEx;
import com.huawei.android.view.DisplayInfoEx;
import com.huawei.server.HwPartFsmFactory;
import com.huawei.server.fsm.DefaultPostureStateMachine;

public class HwFalseTouchMonitor extends DefaultHwFalseTouchMonitor {
    private static final short ALL_COUNT = 1;
    private static final int EDGE_WIDTH = 100;
    private static final int FALSE_TOUCH_REPORTE_CODE = 907400020;
    private static final float FLOAT_PRECISION = 1.0E-7f;
    private static final int FOLD_DISPLAY_FLAG = 1;
    private static final int FOLD_FALSE_TOUCH_REPORTE_CODE = 907400027;
    private static final int FOLD_MONITOR_TIME = 2000;
    private static final short FT_COUNT = 0;
    private static final short FT_NEW_WIN = 3;
    private static final short FT_OLD_WIN = 2;
    private static final int HALF_ORIENTATION = 2;
    private static final boolean IS_BETA;
    private static final boolean IS_CHINA_BETA = (USER_TYPE == 3);
    private static final boolean IS_FOLDABLE = HwFoldScreenManagerEx.isFoldable();
    private static final boolean IS_OVERSEA_BETA = (USER_TYPE == 5);
    private static final int MAX_RECORDS = 200;
    private static final float PI = 3.1415927f;
    private static final int POINTER_COUNT = 2;
    private static final long QUICK_QUIT_WINDOW_TIMEOUT = 800;
    private static final String SEPARATOR = ",";
    public static final int STATE_HAPPENED = 1;
    public static final int STATE_NOT_HAPPENED = 2;
    public static final int STATE_UNKOWN = 0;
    private static final long STATISTIC_PERIOD = 3600000;
    private static final String TAG = "HwFalseTouchMonitor";
    private static final long TAP_RESPOND_MAX_TIME = 500;
    private static final long TAP_RESPOND_MIN_TIME = 50;
    private static final int TREMBLE_MOVE_RANGE = 20;
    private static final int TYPE_CHINA_BETA = 3;
    private static final int TYPE_NO_EFFECT_CLICK = 2;
    private static final int TYPE_OVERSEA_BETA = 5;
    private static final int TYPE_QUICK_QUIT = 1;
    private static final int TYPE_TREMBLE = 3;
    private static final int USER_TYPE = SystemPropertiesEx.getInt("ro.logsystem.usertype", 1);
    private static final int WINDOW_NAME_MAX_LEN = 64;
    private static HwFalseTouchMonitor sInstance = null;
    private DisplayInfoEx mDisplayInfo = new DisplayInfoEx();
    private MotionEventListener mEventListener = null;
    private long mFocusedTime = 0;
    private WindowManagerPolicyEx.WindowStateEx mFocusedWindow = null;
    private int mFoldFalseTouchCount = 0;
    private FoldFalseTouchRunnable mFoldFalseTouchRunnable;
    private Handler mHandler = new Handler();
    private boolean mIsEnabled = false;
    private boolean mIsFocusedChecked = false;
    private boolean mIsFoldFalseTouch;
    private boolean mIsFoldFalseTouchFlag;
    private WindowManagerPolicyEx.WindowStateEx mLastFocusedWindow = null;
    private MonitorPoint mLastUpPoint = new MonitorPoint();
    private NoEffectClickChecker mNecChecker = new NoEffectClickChecker();
    private DefaultPostureStateMachine mPostureStateMachine;
    private long mStatisticCount = 0;
    private Object mStatisticLock = new Object();
    private long mStatisticStartTime = 0;
    private SparseArray<MonitorPoint> mTouchingPoints = new SparseArray<>();
    private TrembleChecker mTrembleChecker = new TrembleChecker();

    static {
        boolean z = true;
        if (!IS_CHINA_BETA && !IS_OVERSEA_BETA) {
            z = false;
        }
        IS_BETA = z;
    }

    /* access modifiers changed from: private */
    public class FoldFalseTouchRunnable implements Runnable {
        private FoldFalseTouchRunnable() {
        }

        @Override // java.lang.Runnable
        public void run() {
            HwFalseTouchMonitor.this.mIsFoldFalseTouchFlag = false;
        }
    }

    private HwFalseTouchMonitor() {
        this.mIsEnabled = IS_FOLDABLE ? IS_BETA : IS_CHINA_BETA;
        Log.d(TAG, "HwFalseTouchMonitor enabled?" + this.mIsEnabled);
        if (this.mIsEnabled) {
            this.mEventListener = new MotionEventListener();
            this.mDisplayInfo = DisplayEx.getDisplayInfo(0);
            if (this.mDisplayInfo != null) {
                Log.d(TAG, "DisplayInfo logicalHeight=" + this.mDisplayInfo.getLogicalHeight() + ",logicalWidth=" + this.mDisplayInfo.getLogicalWidth());
            }
            if (IS_FOLDABLE) {
                this.mFoldFalseTouchRunnable = new FoldFalseTouchRunnable();
                this.mPostureStateMachine = HwPartFsmFactory.loadFactory().getPostureStateMachine();
            }
        }
    }

    public static synchronized HwFalseTouchMonitor getInstance() {
        HwFalseTouchMonitor hwFalseTouchMonitor;
        synchronized (HwFalseTouchMonitor.class) {
            if (sInstance == null) {
                sInstance = new HwFalseTouchMonitor();
            }
            hwFalseTouchMonitor = sInstance;
        }
        return hwFalseTouchMonitor;
    }

    public DefaultHwFalseTouchMonitor.DefaultMotionEventListener getEventListener() {
        return this.mEventListener;
    }

    public boolean isFalseTouchFeatureOn() {
        return this.mIsEnabled;
    }

    public class FalseTouchChecker {
        protected int mState = 0;

        public FalseTouchChecker() {
        }

        public boolean reportData() {
            if (this.mState != 1 || !HwFalseTouchMonitor.this.checkStatistic()) {
                return false;
            }
            return true;
        }

        public boolean processActionDown(MonitorPoint point) {
            reset();
            if (!HwFalseTouchMonitor.this.edgePoint(point)) {
                Log.d(HwFalseTouchMonitor.TAG, point + " faraway from the edge, ignore it");
                this.mState = 2;
                return false;
            }
            Log.d(HwFalseTouchMonitor.TAG, point + " nearby the edge, keep checking it");
            return true;
        }

        public void reset() {
        }

        public void setState(int state) {
            this.mState = state;
        }
    }

    public class TrembleChecker extends FalseTouchChecker {
        public static final float MOVE_PRECISION = 0.1f;
        public static final int TREMBLE_MIN_COUNT = 10;
        private int mAboveCount = 0;
        private int mBelowCount = 0;
        private MonitorPoint mDownPoint;
        private SparseArray<MonitorPoint> mLastMovePoints = new SparseArray<>();
        private int mLeftCount = 0;
        private int mRightCount = 0;

        public TrembleChecker() {
            super();
        }

        @Override // huawei.com.android.server.policy.HwFalseTouchMonitor.FalseTouchChecker
        public boolean reportData() {
            if (this.mState == 2 || !HwFalseTouchMonitor.this.checkStatistic()) {
                return false;
            }
            Log.d(HwFalseTouchMonitor.TAG, "TrembleChecker check mRightCount=" + this.mRightCount + " mLeftCount=" + this.mLeftCount + " mBelowCount=" + this.mBelowCount + " mAboveCount=" + this.mAboveCount);
            if ((this.mLeftCount < 10 || this.mRightCount < 10) && (this.mBelowCount < 10 || this.mAboveCount < 10)) {
                return true;
            }
            HwFalseTouchMonitor.this.updateStatistic();
            IMonitorExt.EventStreamExt eventStream = IMonitorExt.openEventStream((int) HwFalseTouchMonitor.FALSE_TOUCH_REPORTE_CODE);
            eventStream.setParam(eventStream, (short) HwFalseTouchMonitor.FT_COUNT, 3);
            eventStream.setParam(eventStream, (short) HwFalseTouchMonitor.FT_OLD_WIN, HwFalseTouchMonitor.this.mLastUpPoint.getX() + HwFalseTouchMonitor.SEPARATOR + HwFalseTouchMonitor.this.mLastUpPoint.getY());
            IMonitorExt.sendEvent(eventStream);
            IMonitorExt.closeEventStream(eventStream);
            Log.i(HwFalseTouchMonitor.TAG, "TrembleChecker tremble happened, down at" + this.mDownPoint);
            return true;
        }

        @Override // huawei.com.android.server.policy.HwFalseTouchMonitor.FalseTouchChecker
        public void reset() {
            this.mState = 0;
            this.mLastMovePoints.clear();
            this.mDownPoint = null;
            this.mLeftCount = 0;
            this.mRightCount = 0;
            this.mBelowCount = 0;
            this.mAboveCount = 0;
        }

        @Override // huawei.com.android.server.policy.HwFalseTouchMonitor.FalseTouchChecker
        public boolean processActionDown(MonitorPoint point) {
            if (!super.processActionDown(point)) {
                Log.d(HwFalseTouchMonitor.TAG, "tremble super.processActionDown return");
                return false;
            }
            this.mLastMovePoints.put(point.getId(), point);
            Log.i(HwFalseTouchMonitor.TAG, "point(" + point.getX() + HwFalseTouchMonitor.SEPARATOR + point.getY() + ") nearby the edge, add it");
            this.mDownPoint = new MonitorPoint(point.mId, point.getX(), point.getY(), point.getOrientation(), point.getTime());
            return true;
        }

        public void processActionMove(MotionEvent event) {
            if (this.mState == 0) {
                if (event.getPointerCount() != 1 || this.mDownPoint == null) {
                    Log.d(HwFalseTouchMonitor.TAG, "tremble not happened");
                    this.mState = 2;
                    return;
                }
                float eventX = event.getX(0) + (event.getRawX() - event.getX());
                float eventY = event.getY(0) + (event.getRawY() - event.getY());
                int id = event.getPointerId(0);
                int orientation = HwFalseTouchMonitor.this.transformOrientation(event.getOrientation(0));
                long eventTime = event.getEventTime();
                if (Math.abs(eventX - this.mDownPoint.getX()) <= 20.0f) {
                    if (Math.abs(eventY - this.mDownPoint.getY()) <= 20.0f) {
                        MonitorPoint lastPosition = this.mLastMovePoints.get(id);
                        if (lastPosition != null) {
                            Log.d(HwFalseTouchMonitor.TAG, "change to " + eventX + HwFalseTouchMonitor.SEPARATOR + eventY);
                            if (lastPosition.mId == this.mDownPoint.mId && HwFalseTouchMonitor.this.pointMoved(lastPosition.getX(), lastPosition.getY(), eventX, eventY)) {
                                if (eventX > lastPosition.getX()) {
                                    this.mRightCount++;
                                } else if (eventX < lastPosition.getX()) {
                                    this.mLeftCount++;
                                }
                                if (eventY > lastPosition.getY()) {
                                    this.mBelowCount++;
                                } else if (eventY < lastPosition.getY()) {
                                    this.mAboveCount++;
                                }
                            }
                            lastPosition.setX(eventX);
                            lastPosition.setY(eventY);
                            return;
                        }
                        this.mLastMovePoints.put(id, new MonitorPoint(id, eventX, eventY, orientation, eventTime));
                        return;
                    }
                }
                this.mState = 2;
                Log.d(HwFalseTouchMonitor.TAG, "point move out of the tremble range!");
            }
        }
    }

    public class NoEffectClickChecker extends FalseTouchChecker {
        public static final int CLICK_INTERVAL_TIMEOUT = 1000;
        public static final int CLICK_TIME = 100;
        public static final int NEARBY_CHECK_RANGE = 20;
        private MonitorPoint mCentralClickPoint = null;
        private WindowManagerPolicyEx.WindowStateEx mClickDownWindow = null;
        private MonitorPoint mLastPointerDown = new MonitorPoint();
        private MonitorPoint mNearbyEdgePoint = null;
        private int mRepeatClickCount = 0;

        public NoEffectClickChecker() {
            super();
        }

        @Override // huawei.com.android.server.policy.HwFalseTouchMonitor.FalseTouchChecker
        public boolean reportData() {
            if (this.mState != 1 || !HwFalseTouchMonitor.this.checkStatistic() || this.mNearbyEdgePoint == null || this.mCentralClickPoint == null) {
                return false;
            }
            IMonitorExt.EventStreamExt eventStream = IMonitorExt.openEventStream((int) HwFalseTouchMonitor.FALSE_TOUCH_REPORTE_CODE);
            eventStream.setParam(eventStream, (short) HwFalseTouchMonitor.FT_COUNT, 2);
            eventStream.setParam(eventStream, (short) HwFalseTouchMonitor.FT_OLD_WIN, this.mNearbyEdgePoint.getX() + HwFalseTouchMonitor.SEPARATOR + this.mNearbyEdgePoint.getY());
            eventStream.setParam(eventStream, (short) HwFalseTouchMonitor.FT_NEW_WIN, this.mCentralClickPoint.getX() + HwFalseTouchMonitor.SEPARATOR + this.mCentralClickPoint.getY());
            IMonitorExt.sendEvent(eventStream);
            IMonitorExt.closeEventStream(eventStream);
            Log.i(HwFalseTouchMonitor.TAG, "NoEffectClickChecker, report data mNearbyEdgePoint:" + this.mNearbyEdgePoint + ",mCentralClickPoint:" + this.mCentralClickPoint + ",falseTouch window:" + HwFalseTouchMonitor.this.getWindowTitle(this.mClickDownWindow));
            return true;
        }

        @Override // huawei.com.android.server.policy.HwFalseTouchMonitor.FalseTouchChecker
        public void reset() {
            this.mState = 0;
            this.mCentralClickPoint = null;
            this.mNearbyEdgePoint = null;
            this.mRepeatClickCount = 0;
            this.mLastPointerDown = null;
        }

        @Override // huawei.com.android.server.policy.HwFalseTouchMonitor.FalseTouchChecker
        public boolean processActionDown(MonitorPoint point) {
            if (!super.processActionDown(point)) {
                return false;
            }
            this.mNearbyEdgePoint = point;
            this.mClickDownWindow = HwFalseTouchMonitor.this.getFocusWindow();
            return true;
        }

        public void processPointerDown(MotionEvent event) {
            if (this.mState == 0) {
                if (HwFalseTouchMonitor.this.mTouchingPoints.size() > 2) {
                    Log.d(HwFalseTouchMonitor.TAG, "more than one point touch down, no longer check no-effect click");
                    HwFalseTouchMonitor.this.mNecChecker.setState(2);
                    return;
                }
                long interval = 0;
                if (this.mLastPointerDown != null) {
                    interval = event.getEventTime() - this.mLastPointerDown.getTime();
                }
                if (interval == 0 || interval <= 1000) {
                    float offsetX = event.getRawX() - event.getX();
                    float offsetY = event.getRawY() - event.getY();
                    int actionIndex = event.getActionIndex();
                    float eventX = event.getX(actionIndex) + offsetX;
                    float eventY = event.getY(actionIndex) + offsetY;
                    long time = event.getEventTime();
                    int id = event.getPointerId(actionIndex);
                    int orientation = HwFalseTouchMonitor.this.transformOrientation(event.getOrientation());
                    Log.d(HwFalseTouchMonitor.TAG, "NoEffectClickChecker processPointerDown check the no-primary point[" + actionIndex + "]:x=" + eventX + ",y=" + eventY + ",id=" + id);
                    if (HwFalseTouchMonitor.this.mTouchingPoints.get(id) == null) {
                        MonitorPoint curPoint = new MonitorPoint(id, eventX, eventY, orientation, time);
                        HwFalseTouchMonitor.this.mTouchingPoints.put(id, curPoint);
                        this.mLastPointerDown = curPoint;
                        if (HwFalseTouchMonitor.this.edgePoint(curPoint) || this.mCentralClickPoint != null) {
                            MonitorPoint monitorPoint = this.mCentralClickPoint;
                            if (monitorPoint == null) {
                                Log.d(HwFalseTouchMonitor.TAG, "NoEffectClickChecker no central point click before");
                            } else if (!pointNearby(monitorPoint, curPoint)) {
                                Log.d(HwFalseTouchMonitor.TAG, "NoEffectClickChecker current point click is far from the last clicked central point!");
                                this.mState = 2;
                            } else {
                                Log.d(HwFalseTouchMonitor.TAG, "NoEffectClickChecker current point click down nearby the last clicked central point");
                            }
                        } else {
                            this.mCentralClickPoint = curPoint;
                            Log.i(HwFalseTouchMonitor.TAG, "NoEffectClickChecker found the first central point!");
                        }
                    }
                } else {
                    this.mState = 2;
                    Log.d(HwFalseTouchMonitor.TAG, "NoEffectClickChecker click time interval exceeds the limits form last click!");
                }
            }
        }

        public void processPointerUp(MotionEvent event) {
            float offsetX = event.getRawX() - event.getX();
            float offsetY = event.getRawY() - event.getY();
            int actionIndex = event.getActionIndex();
            float eventX = event.getX(actionIndex) + offsetX;
            float eventY = event.getY(actionIndex) + offsetY;
            long time = event.getEventTime();
            int id = event.getPointerId(actionIndex);
            HwFalseTouchMonitor.this.mTouchingPoints.remove(id);
            if (this.mState == 0) {
                Log.d(HwFalseTouchMonitor.TAG, "NoEffectClickChecker processPointerUp point[" + actionIndex + "]:x=" + eventX + ",y=" + eventY + ",id=" + id);
                MonitorPoint monitorPoint = this.mNearbyEdgePoint;
                if (monitorPoint == null || monitorPoint.getId() == id) {
                    this.mState = 2;
                    Log.i(HwFalseTouchMonitor.TAG, "NoEffectClickChecker processPointerUp edgePoint is up!");
                    return;
                }
                MonitorPoint monitorPoint2 = this.mLastPointerDown;
                if (monitorPoint2 == null || monitorPoint2.getId() != id) {
                    Log.i(HwFalseTouchMonitor.TAG, "NoEffectClickChecker pointer up id not matched");
                } else if (pointNearby(this.mLastPointerDown.getX(), this.mLastPointerDown.getY(), eventX, eventY)) {
                    long clickTime = time - this.mLastPointerDown.getTime();
                    if (clickTime > 0 && clickTime <= 100) {
                        this.mRepeatClickCount++;
                        Log.i(HwFalseTouchMonitor.TAG, "NoEffectClickChecker ,mRepeatClickCount=" + this.mRepeatClickCount);
                        if (this.mRepeatClickCount > 2) {
                            Log.i(HwFalseTouchMonitor.TAG, "NoEffectClickChecker ,mRepeatClickCount happend");
                            this.mState = 1;
                        }
                    }
                } else {
                    Log.i(HwFalseTouchMonitor.TAG, "NoEffectClickChecker , central point swipe, not an click operation");
                    this.mState = 2;
                }
            }
        }

        private boolean pointNearby(float x1, float y1, float x2, float y2) {
            return Math.abs(x1 - x2) < 20.0f || Math.abs(y1 - y2) < 20.0f;
        }

        private boolean pointNearby(MonitorPoint p1, MonitorPoint p2) {
            return pointNearby(p1.getX(), p1.getY(), p2.getX(), p2.getY());
        }
    }

    public static class MonitorPoint {
        private static final int DEFAULT_ID = -1;
        private static final float DEFAULT_POINT_VALUE = -1.0f;
        private float mEventX = DEFAULT_POINT_VALUE;
        private float mEventY = DEFAULT_POINT_VALUE;
        private int mId = -1;
        private int mOrientation = 0;
        private long mTime = 0;

        public MonitorPoint() {
        }

        public MonitorPoint(int id, float eventX, float eventY, int orientation, long time) {
            this.mId = id;
            this.mEventX = eventX;
            this.mEventY = eventY;
            this.mOrientation = orientation;
            this.mTime = time;
        }

        public void set(int id, float eventX, float eventY, int orientation, long time) {
            this.mId = id;
            this.mEventX = eventX;
            this.mEventY = eventY;
            this.mOrientation = orientation;
            this.mTime = time;
        }

        public String toString() {
            return "point(" + this.mEventX + HwFalseTouchMonitor.SEPARATOR + this.mEventY + "), orientation:" + this.mOrientation;
        }

        public int getId() {
            return this.mId;
        }

        public float getX() {
            return this.mEventX;
        }

        public void setX(float eventX) {
            this.mEventX = eventX;
        }

        public float getY() {
            return this.mEventY;
        }

        public void setY(float eventY) {
            this.mEventY = eventY;
        }

        public long getTime() {
            return this.mTime;
        }

        public int getOrientation() {
            return this.mOrientation;
        }
    }

    public class MotionEventListener extends DefaultHwFalseTouchMonitor.DefaultMotionEventListener {
        public MotionEventListener() {
        }

        public void onPointerEvent(MotionEvent motionEvent) {
            HwFalseTouchMonitor.this.handleMotionEvent(motionEvent);
        }
    }

    public void handleFocusChanged(WindowManagerPolicyEx.WindowStateEx lastFocus, WindowManagerPolicyEx.WindowStateEx newFocus) {
        if (this.mIsEnabled && newFocus != null) {
            synchronized (this) {
                this.mFocusedTime = System.currentTimeMillis();
                this.mIsFocusedChecked = false;
                this.mFocusedWindow = newFocus;
                this.mLastFocusedWindow = lastFocus;
                this.mIsFoldFalseTouch = this.mIsFoldFalseTouchFlag;
            }
        }
    }

    public void handleKeyEvent(KeyEvent keyEvent) {
        long interval;
        long tapRespondTime;
        if (this.mIsEnabled && keyEvent.getAction() == 0) {
            int keyCode = keyEvent.getKeyCode();
            if (keyCode == 3) {
                reportFoldFalseTouch();
            } else if (keyCode == 4) {
                reportFoldFalseTouch();
                synchronized (this) {
                    if (!this.mIsFocusedChecked) {
                        if (edgePoint(this.mLastUpPoint)) {
                            interval = System.currentTimeMillis() - this.mFocusedTime;
                            tapRespondTime = this.mFocusedTime - this.mLastUpPoint.getTime();
                            this.mIsFocusedChecked = true;
                        } else {
                            return;
                        }
                    } else {
                        return;
                    }
                }
                if (interval <= 0 || interval >= QUICK_QUIT_WINDOW_TIMEOUT || tapRespondTime <= TAP_RESPOND_MIN_TIME || tapRespondTime >= TAP_RESPOND_MAX_TIME) {
                    Log.d(TAG, "handleKeyEvent current window enter " + interval + "ms ago, last motion up before" + tapRespondTime + " ms ago from current window entered");
                    return;
                }
                Log.i(TAG, "enter current window and quit it quickly");
                if (checkStatistic()) {
                    reportQuickQuitWindow();
                }
            }
        }
    }

    private void reportQuickQuitWindow() {
        IMonitorExt.EventStreamExt eventStream = IMonitorExt.openEventStream((int) FALSE_TOUCH_REPORTE_CODE);
        eventStream.setParam(eventStream, (short) FT_COUNT, 1);
        String windowName = getWindowTitle(getFocusWindow());
        String point = this.mLastUpPoint.getX() + SEPARATOR + this.mLastUpPoint.getY();
        if (windowName.length() > WINDOW_NAME_MAX_LEN) {
            windowName = windowName.substring(0, WINDOW_NAME_MAX_LEN);
        }
        eventStream.setParam(eventStream, (short) ALL_COUNT, windowName);
        eventStream.setParam(eventStream, (short) FT_OLD_WIN, point);
        IMonitorExt.sendEvent(eventStream);
        IMonitorExt.closeEventStream(eventStream);
        Log.i(TAG, "reportQuickQuitWindow point(" + this.mLastUpPoint.getX() + SEPARATOR + this.mLastUpPoint.getY() + ") click up, orientation=" + this.mLastUpPoint.getOrientation() + ",window:" + windowName);
        updateStatistic();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleMotionEvent(MotionEvent event) {
        int action = event.getActionMasked();
        boolean z = true;
        if (action == 0) {
            handleActionDown(event);
        } else if (action == 1) {
            handleActionUp(event);
        } else if (action == 2) {
            handleActionMove(event);
        } else if (action == 5) {
            handlePointerDown(event);
        } else if (action == 6) {
            handlePointerUp(event);
        }
        if (IS_FOLDABLE && !this.mIsFoldFalseTouchFlag) {
            if (((int) event.getAxisValue(39)) != 1) {
                z = false;
            }
            this.mIsFoldFalseTouchFlag = z;
            if (this.mIsFoldFalseTouchFlag) {
                Log.i(TAG, "motion event has folding flags");
                this.mHandler.postDelayed(this.mFoldFalseTouchRunnable, 2000);
            }
        }
    }

    private void handleActionDown(MotionEvent event) {
        float eventX = event.getRawX();
        float eventY = event.getRawY();
        long time = event.getEventTime();
        int id = event.getPointerId(event.getActionIndex());
        MonitorPoint downPoint = new MonitorPoint(id, eventX, eventY, transformOrientation(event.getOrientation()), time);
        this.mTouchingPoints.put(id, downPoint);
        this.mTrembleChecker.processActionDown(downPoint);
        this.mNecChecker.processActionDown(downPoint);
    }

    private void handleActionUp(MotionEvent event) {
        float eventX = event.getRawX();
        float eventY = event.getRawY();
        long time = System.currentTimeMillis();
        int id = event.getPointerId(event.getActionIndex());
        int orientation = transformOrientation(event.getOrientation());
        this.mTouchingPoints.clear();
        this.mLastUpPoint.set(id, eventX, eventY, orientation, time);
        this.mTrembleChecker.reportData();
        this.mNecChecker.reportData();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean pointMoved(float x1, float x2, float y1, float y2) {
        return Math.abs(x1 - x2) > 0.1f || Math.abs(y1 - y2) > 0.1f;
    }

    private void handleActionMove(MotionEvent event) {
        this.mTrembleChecker.processActionMove(event);
    }

    private void handlePointerDown(MotionEvent event) {
        Log.d(TAG, "more than one point touch down, no longer check tremble");
        this.mTrembleChecker.setState(2);
        this.mNecChecker.processPointerDown(event);
    }

    private void handlePointerUp(MotionEvent event) {
        this.mNecChecker.processPointerUp(event);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int transformOrientation(float orientation) {
        if (Math.abs(orientation - 1.5707964f) < FLOAT_PRECISION) {
            return 1;
        }
        if (Math.abs(1.5707964f + orientation) < FLOAT_PRECISION) {
            return 3;
        }
        return 0;
    }

    private void reportFoldFalseTouch() {
        if (this.mIsFoldFalseTouch) {
            int allCount = 0;
            this.mIsFoldFalseTouch = false;
            this.mFoldFalseTouchCount++;
            IMonitorExt.EventStreamExt eventStream = IMonitorExt.openEventStream((int) FOLD_FALSE_TOUCH_REPORTE_CODE);
            eventStream.setParam(eventStream, (short) FT_COUNT, this.mFoldFalseTouchCount);
            DefaultPostureStateMachine defaultPostureStateMachine = this.mPostureStateMachine;
            if (defaultPostureStateMachine != null) {
                allCount = defaultPostureStateMachine.getFoldStateChangeCount();
            }
            eventStream.setParam(eventStream, (short) ALL_COUNT, allCount);
            String oldWin = getWindowTitle(this.mLastFocusedWindow);
            String newWin = getWindowTitle(this.mFocusedWindow);
            eventStream.setParam(eventStream, (short) FT_OLD_WIN, oldWin);
            eventStream.setParam(eventStream, (short) FT_NEW_WIN, newWin);
            IMonitorExt.sendEvent(eventStream);
            IMonitorExt.closeEventStream(eventStream);
            Log.i(TAG, "FT_COUNT:" + this.mFoldFalseTouchCount + ",ALL_COUNT:" + allCount + ",FT_OLD_WIN:" + oldWin + ",FT_NEW_WIN:" + newWin);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean edgePoint(MonitorPoint point) {
        boolean isEdge;
        int width = this.mDisplayInfo.getLogicalWidth();
        int height = this.mDisplayInfo.getLogicalHeight();
        float eventX = point.getX();
        boolean z = true;
        if (point.getOrientation() == 0) {
            if ((eventX < GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO || eventX > 100.0f) && (eventX < ((float) (width - 100)) || eventX > ((float) width))) {
                z = false;
            }
            isEdge = z;
        } else {
            if ((eventX < GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO || eventX > 100.0f) && (eventX < ((float) (height - 100)) || eventX > ((float) height))) {
                z = false;
            }
            isEdge = z;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(point);
        sb.append(isEdge ? " is" : " not");
        sb.append(" a edge point");
        Log.i(TAG, sb.toString());
        return isEdge;
    }

    public WindowManagerPolicyEx.WindowStateEx getFocusWindow() {
        HwPhoneWindowManager policy = WindowManagerPolicyEx.getInstance().getHwPhoneWindowManager();
        if (policy != null) {
            return PhoneWindowManagerEx.getFocusedWindow(policy);
        }
        return null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String getWindowTitle(WindowManagerPolicyEx.WindowStateEx window) {
        if (window == null || window.getAttrs() == null) {
            return "unkown";
        }
        return window.getAttrs().getTitle().toString();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean checkStatistic() {
        synchronized (this.mStatisticLock) {
            if (this.mStatisticStartTime != 0) {
                if (this.mStatisticCount > 200) {
                    long now = System.currentTimeMillis();
                    if (now - this.mStatisticStartTime <= STATISTIC_PERIOD) {
                        return false;
                    }
                    this.mStatisticStartTime = now;
                    this.mStatisticCount = 0;
                    return true;
                }
            }
            return true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateStatistic() {
        synchronized (this.mStatisticLock) {
            if (this.mStatisticCount == 0) {
                this.mStatisticStartTime = System.currentTimeMillis();
            }
            this.mStatisticCount++;
        }
    }
}
