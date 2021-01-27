package android.view;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Point;
import android.inputmethodservice.InputMethodService;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.Settings;
import android.rms.HwAppInnerBoostImpl;
import android.rms.iaware.AwareUiRenderParallelManager;
import android.scrollerboostmanager.ScrollerBoostManager;
import android.util.HwPCUtils;
import android.util.HwStylusUtils;
import android.util.Jlog;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.WindowManager;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.os.ServiceManagerEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.os.TraceEx;
import com.huawei.android.provider.SettingsEx;
import com.huawei.android.view.MotionEventEx;
import com.huawei.android.view.ViewEx;
import com.huawei.android.view.WindowManagerEx;
import com.huawei.hwpartbasicplatform.BuildConfig;
import com.huawei.utils.HwPartResourceUtils;
import huawei.android.provider.HwSettings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class HwViewRootImpl extends DefaultHwViewRootImpl {
    private static final boolean FRONT_FINGERPRINT_NAVIGATION = SystemPropertiesEx.getBoolean("ro.config.hw_front_fp_navi", false);
    private static final int FRONT_FINGERPRINT_NAVIGATION_TRIKEY = SystemPropertiesEx.getInt("ro.config.hw_front_fp_trikey", 0);
    private static final boolean IS_TABLET = "tablet".equals(SystemPropertiesEx.get("ro.build.characteristics", "default"));
    private static final int KEYCODE_F20 = 718;
    private static final int MOTION_EVENT_INJECTION_DELAY_MILLIS = 20;
    private static final int NAVIGATION_DISABLE = 0;
    private static final int NAVIGATION_ENABLE = 1;
    private static final String RUN_MODE = SystemPropertiesEx.get("ro.runmode", "normal");
    private static final String TAG = "HwViewRootImpl";
    static final boolean isHwNaviBar = SystemPropertiesEx.getBoolean("ro.config.hw_navigationbar", false);
    private static HwViewRootImpl mInstance = null;
    private boolean isDecorPointerEvent;
    private ChoreographerState mChgState;
    Point mDisplayPoint;
    private MotionEvent mDownEvent;
    int mHitRegionToMax;
    private final HwAppInnerBoostImpl mHwAppInnerBoost;
    private boolean mIsRedispatchDownAction;
    private boolean mIsStylusButtonDown;
    private boolean mIsStylusEffective;
    private JankDetector mJankDetector;
    private StylusTouchListener mStylusTouchListener;
    private TraceInputInfoManager mTraceInputInfoManager;
    private int mTraceInputParams;

    protected HwViewRootImpl() {
        this.mHitRegionToMax = MOTION_EVENT_INJECTION_DELAY_MILLIS;
        this.mIsRedispatchDownAction = false;
        this.mDownEvent = null;
        this.isDecorPointerEvent = false;
        this.mStylusTouchListener = null;
        this.mIsStylusEffective = true;
        this.mChgState = null;
        this.mJankDetector = null;
        this.mTraceInputInfoManager = null;
        this.mHwAppInnerBoost = HwAppInnerBoostImpl.getDefault();
        this.mTraceInputParams = -1;
        this.mIsStylusButtonDown = false;
        this.mChgState = new ChoreographerState();
        this.mJankDetector = new JankDetector(this.mChgState);
        this.mTraceInputInfoManager = new TraceInputInfoManager();
    }

    public static synchronized HwViewRootImpl getDefault() {
        HwViewRootImpl hwViewRootImpl;
        synchronized (HwViewRootImpl.class) {
            if (mInstance == null) {
                mInstance = new HwViewRootImpl();
            }
            hwViewRootImpl = mInstance;
        }
        return hwViewRootImpl;
    }

    public void setRealSize(Point point) {
        this.mDisplayPoint = point;
    }

    public void clearDisplayPoint() {
        this.mDisplayPoint = null;
    }

    public boolean filterDecorPointerEvent(Context context, MotionEvent event, int action, WindowManager.LayoutParams windowattr, Display disp) {
        if (HwPCUtils.isValidExtDisplayId(context) || ((disp != null && HwPCUtils.isValidExtDisplayId(disp.getDisplayId())) || context == null || !isHwNaviBar || disp == null || windowattr == null || (WindowManagerEx.LayoutParamsEx.getHwFlags(windowattr) & 2097152) != 0 || (WindowManagerEx.LayoutParamsEx.getPrivateFlags(windowattr) & WindowManagerEx.LayoutParamsEx.getPrivateFlagKeyguard()) != 0)) {
            return false;
        }
        boolean z = true;
        if (action == 0) {
            this.isDecorPointerEvent = false;
            Point pt = this.mDisplayPoint;
            if (pt == null) {
                pt = getDisplayPoint(disp);
            }
            this.mHitRegionToMax = (int) (((double) context.getResources().getDimensionPixelSize(HwPartResourceUtils.getResourceId("navigation_bar_height"))) / 3.5d);
            if (pt.y > pt.x) {
                this.isDecorPointerEvent = event.getRawY() > ((float) (pt.y - this.mHitRegionToMax));
            } else {
                this.isDecorPointerEvent = event.getRawX() > ((float) (pt.x - this.mHitRegionToMax));
            }
            this.isDecorPointerEvent = this.isDecorPointerEvent && canNavBarFlingOut(context, windowattr);
            if (this.isDecorPointerEvent) {
                this.mDownEvent = MotionEventEx.copy(event);
                return true;
            }
            this.mDownEvent = null;
            this.mIsRedispatchDownAction = false;
        } else if (action == 3) {
            this.mDownEvent = null;
            this.isDecorPointerEvent = false;
        } else if (action == 1) {
            Point pt2 = this.mDisplayPoint;
            if (pt2 == null) {
                pt2 = getDisplayPoint(disp);
            }
            if (!this.isDecorPointerEvent) {
                this.mIsRedispatchDownAction = false;
            } else if (pt2.y > pt2.x) {
                if (event.getRawY() <= ((float) (pt2.y - this.mHitRegionToMax))) {
                    z = false;
                }
                this.mIsRedispatchDownAction = z;
            } else {
                if (event.getRawX() <= ((float) (pt2.x - this.mHitRegionToMax))) {
                    z = false;
                }
                this.mIsRedispatchDownAction = z;
            }
            if (!this.mIsRedispatchDownAction) {
                this.mDownEvent = null;
            }
            this.isDecorPointerEvent = false;
        }
        return false;
    }

    private boolean canNavBarFlingOut(Context context, WindowManager.LayoutParams windowattr) {
        boolean isGestureNavigationMode;
        boolean z = false;
        if (!isNaviEnable(context)) {
            return false;
        }
        boolean navBarIsMin = Settings.Global.getInt(context.getContentResolver(), SettingsEx.System.NAVIGATIONBAR_IS_MIN, SettingsEx.System.NAVIGATIONBAR_IS_MIN_DEFAULT) == 1;
        if (2000 != windowattr.type || (WindowManagerEx.LayoutParamsEx.getHwFlags(windowattr) & 4) == 0) {
            isGestureNavigationMode = Settings.Secure.getInt(context.getContentResolver(), HwSettings.Secure.KEY_SECURE_GESTURE_NAVIGATION, 0) == 1;
        } else {
            isGestureNavigationMode = SettingsEx.Secure.getIntForUser(context.getContentResolver(), HwSettings.Secure.KEY_SECURE_GESTURE_NAVIGATION, 0, ActivityManagerEx.getCurrentUser()) == 1;
        }
        if (!isGestureNavigationMode) {
            z = true;
        }
        return z & navBarIsMin;
    }

    public MotionEvent getRedispatchEvent() {
        if (!this.mIsRedispatchDownAction || this.mDownEvent == null) {
            return null;
        }
        MotionEvent mv = this.mDownEvent;
        this.mDownEvent = null;
        return mv;
    }

    private Point getDisplayPoint(Display disp) {
        if (this.mDisplayPoint == null) {
            Point pt = new Point();
            disp.getRealSize(pt);
            this.mDisplayPoint = pt;
        }
        return this.mDisplayPoint;
    }

    private boolean isFingerSenseEnabled(ContentResolver resolver) {
        if ("factory".equals(RUN_MODE) || SystemPropertiesEx.getBoolean("sys.super_power_save", false)) {
            return false;
        }
        if (Settings.Global.getInt(resolver, "fingersense_enabled", 1) == 1) {
            return true;
        }
        return false;
    }

    public boolean shouldQueueInputEvent(InputEvent event, Context context, View view, WindowManager.LayoutParams attr) {
        Context viewContext;
        if ((event instanceof KeyEvent) && ((KeyEvent) event).getKeyCode() == KEYCODE_F20) {
            if (((KeyEvent) event).getAction() == 0) {
                this.mIsStylusButtonDown = true;
            } else {
                this.mIsStylusButtonDown = false;
            }
        }
        if (!(event instanceof MotionEvent)) {
            return true;
        }
        if (this.mStylusTouchListener == null && HwStylusUtils.hasStylusFeature(context)) {
            Log.d("stylus", "init stylus touchlistener.");
            this.mStylusTouchListener = new StylusTouchListener(context);
        }
        MotionEvent motionEvent = (MotionEvent) event;
        if (isStylusButtonPressed(context, attr.type, motionEvent)) {
            return false;
        }
        int pointerCount = motionEvent.getPointerCount();
        for (int i = 0; i < pointerCount; i++) {
            if (motionEvent.getToolType(i) != 7) {
                return true;
            }
        }
        if (!((WindowManagerEx.LayoutParamsEx.getHwFlags(attr) & 2097152) == 0 && isFingerSenseEnabled(context.getContentResolver()) && (attr.flags & 4096) == 0)) {
            return true;
        }
        if (view == null || (viewContext = view.getContext()) == null || !(viewContext instanceof ContextThemeWrapper)) {
            return false;
        }
        return ((ContextThemeWrapper) viewContext).getBaseContext() instanceof InputMethodService;
    }

    private boolean isStylusButtonPressed(Context context, int windowType, MotionEvent motionEvent) {
        if (HwStylusUtils.hasStylusFeature(context)) {
            boolean stylusPrimaryButtonPressed = motionEvent.getToolType(0) == 2 && (motionEvent.getButtonState() == 32 || this.mIsStylusButtonDown);
            if (motionEvent.getAction() == 0) {
                this.mIsStylusEffective = stylusPrimaryButtonPressed;
            }
            if (stylusPrimaryButtonPressed && this.mStylusTouchListener != null && this.mIsStylusEffective && isStylusEnable(context)) {
                this.mStylusTouchListener.updateViewContext(context, windowType);
                this.mStylusTouchListener.onTouchEvent(motionEvent);
                return true;
            } else if (motionEvent.getAction() == 1 || motionEvent.getAction() == 3) {
                this.mIsStylusEffective = true;
            }
        }
        return false;
    }

    private boolean isStylusEnable(Context context) {
        boolean stylusEnabled = true;
        if (Settings.System.getInt(context.getContentResolver(), "stylus_enable", 1) == 0) {
            stylusEnabled = false;
        }
        return stylusEnabled;
    }

    private boolean isNaviEnable(Context mContext) {
        return Settings.System.getInt(mContext.getContentResolver(), "enable_navbar", getDefaultNavConfig()) != 0;
    }

    private int getDefaultNavConfig() {
        if (!FRONT_FINGERPRINT_NAVIGATION) {
            return 1;
        }
        int i = FRONT_FINGERPRINT_NAVIGATION_TRIKEY;
        if (i == 0) {
            if (isChinaArea()) {
                return 0;
            }
            return 1;
        } else if (i == 1) {
            return 0;
        } else {
            return 1;
        }
    }

    private static boolean isChinaArea() {
        return SystemPropertiesEx.get("ro.config.hw_optb", "0").equals("156");
    }

    public boolean interceptMotionEvent(View view, MotionEvent event) {
        if ((event.getMetaState() & 4096) == 0 || (event.getSource() & 2) == 0 || event.getAction() != 8) {
            return false;
        }
        return multiPointerGesture(view, event.getX(), event.getY(), event.getAxisValue(9));
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0051: APUT  
      (r11v1 'ppCoords' android.view.MotionEvent$PointerCoords[][] A[D('ppCoords' android.view.MotionEvent$PointerCoords[][])])
      (0 ??[int, short, byte, char])
      (r13v0 'pointerCoordsX' android.view.MotionEvent$PointerCoords[] A[D('pointerCoordsX' android.view.MotionEvent$PointerCoords[])])
     */
    private boolean multiPointerGesture(View view, float x, float y, float value) {
        int ponterCount = 2;
        int guide = value > 0.0f ? 1 : -1;
        float pointerX1 = x - 200.0f;
        float pointerY1 = y - 200.0f;
        float pointerX2 = x + 200.0f;
        float pointerY2 = y + 200.0f;
        MotionEvent.PointerCoords[][] ppCoords = new MotionEvent.PointerCoords[2][];
        MotionEvent.PointerCoords[] pointerCoordsX = new MotionEvent.PointerCoords[4];
        MotionEvent.PointerCoords[] pointerCoordsY = new MotionEvent.PointerCoords[4];
        int index = 1;
        for (int i = 4; index <= i; i = 4) {
            float dis = ((float) index) * 30.0f * ((float) guide);
            pointerCoordsX[index - 1] = getPonterCoords(pointerX1 - dis, pointerY1 - dis);
            pointerCoordsY[index - 1] = getPonterCoords(pointerX2 + dis, pointerY2 + dis);
            index++;
            ponterCount = ponterCount;
        }
        ppCoords[0] = pointerCoordsX;
        ppCoords[1] = pointerCoordsY;
        return performMultiPointerGesture(view, ppCoords);
    }

    private MotionEvent.PointerCoords getPonterCoords(float x, float y) {
        MotionEvent.PointerCoords pc1 = new MotionEvent.PointerCoords();
        pc1.x = x;
        pc1.y = y;
        pc1.pressure = 1.0f;
        pc1.size = 1.0f;
        return pc1;
    }

    private boolean performMultiPointerGesture(View view, MotionEvent.PointerCoords[]... touches) {
        int maxSteps = 0;
        for (int x = 0; x < touches.length; x++) {
            maxSteps = maxSteps < touches[x].length ? touches[x].length : maxSteps;
        }
        MotionEvent.PointerProperties[] properties = new MotionEvent.PointerProperties[touches.length];
        MotionEvent.PointerCoords[] pointerCoords = new MotionEvent.PointerCoords[touches.length];
        for (int x2 = 0; x2 < touches.length; x2++) {
            MotionEvent.PointerProperties prop = new MotionEvent.PointerProperties();
            prop.id = x2;
            prop.toolType = 1;
            properties[x2] = prop;
            pointerCoords[x2] = touches[x2][0];
        }
        long downTime = SystemClock.uptimeMillis();
        boolean ret = true & injectEventSync(view, MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), 0, 1, properties, pointerCoords, 0, 0, 1.0f, 1.0f, 0, 0, 4098, 0));
        for (int x3 = 1; x3 < touches.length; x3++) {
            ret &= injectEventSync(view, MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), getPointerAction(5, x3), x3 + 1, properties, pointerCoords, 0, 0, 1.0f, 1.0f, 0, 0, 4098, 0));
            SystemClock.sleep(20);
        }
        for (int i = 1; i < maxSteps - 1; i++) {
            for (int x4 = 0; x4 < touches.length; x4++) {
                if (touches[x4].length > i) {
                    pointerCoords[x4] = touches[x4][i];
                } else {
                    pointerCoords[x4] = touches[x4][touches[x4].length - 1];
                }
            }
            ret &= injectEventSync(view, MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), 2, touches.length, properties, pointerCoords, 0, 0, 1.0f, 1.0f, 0, 0, 4098, 0));
            SystemClock.sleep(20);
        }
        for (int x5 = 0; x5 < touches.length; x5++) {
            pointerCoords[x5] = touches[x5][touches[x5].length - 1];
        }
        for (int x6 = 1; x6 < touches.length; x6++) {
            ret &= injectEventSync(view, MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), getPointerAction(6, x6), x6 + 1, properties, pointerCoords, 0, 0, 1.0f, 1.0f, 0, 0, 4098, 0));
        }
        return ret & injectEventSync(view, MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), 1, 1, properties, pointerCoords, 0, 0, 1.0f, 1.0f, 0, 0, 4098, 0));
    }

    private boolean injectEventSync(View view, MotionEvent event) {
        return ViewEx.dispatchPointerEvent(view, event);
    }

    private int getPointerAction(int motionEnvent, int index) {
        return (index << 8) + motionEnvent;
    }

    public void setIsFirstFrame(boolean isFirstFrame) {
        synchronized (this) {
            this.mChgState.mIsFirstFrame = isFirstFrame;
        }
    }

    public void setIsNeedDraw(boolean isNeedDraw) {
        this.mChgState.mIsNeedDraw = isNeedDraw;
    }

    public void processJank(boolean scroll, long[] jankdrawdata, String windowtitle, int windowtype) {
        boolean not_care_window = false;
        if (this.mChgState.mIsFirstFrame) {
            setIsFirstFrame(false);
            Jlog.d(337, windowtitle, BuildConfig.FLAVOR);
            Jlog.frameCount = 0;
        }
        if (!(windowtype <= 1999 || windowtype == WindowManagerEx.LayoutParamsEx.getTypeKeyguard() || windowtype == 2013 || windowtype == 2011)) {
            not_care_window = true;
        }
        if (!not_care_window && this.mChgState.mIsNeedDraw) {
            this.mJankDetector.processAfterTraversal(scroll, jankdrawdata, windowtitle);
        }
    }

    public void onChgCallBackCountsChanged(int changes) {
        this.mChgState.mCallBackCounts += changes;
    }

    public void updateDoframeStatus(boolean indoframe) {
        ChoreographerState choreographerState = this.mChgState;
        choreographerState.mInDoframe = indoframe;
        if (!indoframe) {
            choreographerState.mOldestInputTime = Long.MAX_VALUE;
        }
    }

    public void setRealFrameTime(long time) {
        this.mChgState.mRealFrameTime = time;
    }

    public void updateLastTraversal(boolean status) {
        this.mChgState.mLastTraversal = status;
    }

    public void updateOldestInputTime(long time) {
        if (time < this.mChgState.mOldestInputTime) {
            this.mChgState.mOldestInputTime = time;
        }
    }

    public void checkOldestInputTime() {
        if (!this.mChgState.mInDoframe && this.mChgState.mCallBackCounts <= 0) {
            ChoreographerState choreographerState = this.mChgState;
            choreographerState.mOldestInputTime = Long.MAX_VALUE;
            choreographerState.reportDrawRequestResult(false);
        }
    }

    public void savePkgName(String pkgName) {
        this.mChgState.savePkgName(pkgName);
    }

    public void updateInputState(MotionEvent me) {
        this.mChgState.updateInputState(me);
    }

    public void reportDrawRequestResult() {
        this.mChgState.reportDrawRequestResult(true);
    }

    public final class JankDetector {
        private static final long VSYNC_SPAN = 16666667;
        private final int JANK_SKIPPED_THRESHOLD = SystemPropertiesEx.getInt("ro.config.jank_skipped_threshold", 5);
        private ChoreographerState mChgState = null;
        final ArrayList<Pair<Long, Long>> mJankList = new ArrayList<>();
        private long mLatestSkips = 0;

        public JankDetector(ChoreographerState chgstate) {
            this.mChgState = chgstate;
        }

        private void removeInvalidNode(long time) {
            int size = this.mJankList.size();
            if (size == 0) {
                Log.i(HwViewRootImpl.TAG, "removeInvalidNode jank list is null");
            } else if (((Long) this.mJankList.get(size - 1).first).longValue() < time) {
                Log.i(HwViewRootImpl.TAG, "removeInvalidNode all the node in jank list is out of time");
                this.mJankList.clear();
            } else {
                int index = Collections.binarySearch(this.mJankList, new Pair<>(Long.valueOf(time), 0L), new Comparator<Pair<Long, Long>>() {
                    /* class android.view.HwViewRootImpl.JankDetector.AnonymousClass1 */

                    public int compare(Pair<Long, Long> p1, Pair<Long, Long> p2) {
                        if (p1 == null || p2 == null) {
                            return -1;
                        }
                        if (((Long) p1.first).longValue() > ((Long) p2.first).longValue()) {
                            return 1;
                        }
                        if (((Long) p1.first).longValue() < ((Long) p2.first).longValue()) {
                            return -1;
                        }
                        return 0;
                    }
                });
                if (index == 0) {
                    this.mJankList.remove(0);
                } else if (index > 0) {
                    this.mJankList.subList(0, index + 1).clear();
                }
            }
        }

        private int getTotalJankNum() {
            int totalCount = 0;
            int size = this.mJankList.size();
            for (int i = 0; i < size; i++) {
                totalCount = (int) (((long) totalCount) + ((Long) this.mJankList.get(i).second).longValue());
            }
            return totalCount;
        }

        /* JADX INFO: Multiple debug info for r1v5 long: [D('node' android.util.Pair<java.lang.Long, java.lang.Long>), D('totalJankNum' long)] */
        private void newCheckSkippedFrame(long nowtime, long frameVsynctime, String windowtitle) {
            long skippedFrames;
            long totalJankNum;
            long j;
            boolean bLastTraversal = this.mChgState.mLastTraversal;
            long LastSkippedFrameEnd = this.mChgState.mLastSkippedFrameEnd;
            this.mLatestSkips = 0;
            if (bLastTraversal) {
                Jlog.frameCount++;
                if (LastSkippedFrameEnd > frameVsynctime) {
                    skippedFrames = (nowtime - LastSkippedFrameEnd) / VSYNC_SPAN;
                } else {
                    skippedFrames = (nowtime - frameVsynctime) / VSYNC_SPAN;
                }
                this.mLatestSkips = skippedFrames;
                if (skippedFrames >= ((long) this.JANK_SKIPPED_THRESHOLD)) {
                    totalJankNum = 0;
                    TraceEx.traceBegin(TraceEx.getTraceTagView(), "jank_event_sync: start_ts=" + frameVsynctime + ",end_ts=" + nowtime + ", appid=" + Process.myPid());
                    TraceEx.traceEnd(TraceEx.getTraceTagView());
                    Jlog.d(37, "#P:" + windowtitle + "#SK:" + skippedFrames + "#FRT:" + (frameVsynctime / 10000) + "#DNT:" + (nowtime / 10000));
                    this.mChgState.mLastSkippedFrameEnd = nowtime;
                } else {
                    totalJankNum = 0;
                }
                if (HwViewRootImpl.this.mHwAppInnerBoost != null) {
                    HwViewRootImpl.this.mHwAppInnerBoost.onJitter(skippedFrames);
                }
                if (skippedFrames >= 15) {
                    this.mJankList.clear();
                    j = nowtime;
                } else if (skippedFrames < 1 || skippedFrames >= 15) {
                    j = nowtime;
                } else {
                    Pair<Long, Long> node = new Pair<>(Long.valueOf(nowtime), Long.valueOf(skippedFrames));
                    removeInvalidNode(nowtime - 3000000000L);
                    this.mJankList.add(node);
                    long totalJankNum2 = (long) getTotalJankNum();
                    if (totalJankNum2 >= 90) {
                        StringBuilder seqFrameSkipMsg = new StringBuilder();
                        seqFrameSkipMsg.append("#P:");
                        seqFrameSkipMsg.append(windowtitle);
                        seqFrameSkipMsg.append("#SK:");
                        seqFrameSkipMsg.append(totalJankNum2);
                        seqFrameSkipMsg.append("#FRT:");
                        seqFrameSkipMsg.append(frameVsynctime / 10000);
                        seqFrameSkipMsg.append("#DNT:");
                        j = nowtime;
                        seqFrameSkipMsg.append(j / 10000);
                        Jlog.d(362, seqFrameSkipMsg.toString());
                        this.mJankList.clear();
                    } else {
                        j = nowtime;
                    }
                    this.mChgState.checkTounchResponseTime(windowtitle, j);
                    ScrollerBoostManager.getInstance().updateFrameJankInfo(skippedFrames);
                }
                this.mChgState.checkTounchResponseTime(windowtitle, j);
                ScrollerBoostManager.getInstance().updateFrameJankInfo(skippedFrames);
            }
        }

        public void processAfterTraversal(boolean scroll, long[] jankdrawData, String windowtitle) {
            newCheckSkippedFrame(System.nanoTime(), this.mChgState.mRealFrameTime, windowtitle);
        }
    }

    public final class ChoreographerState {
        private static final int JLID_APP_DRAW_FRAME_REQUEST = 470;
        private static final int REPORT_INTERVAL = 800;
        public static final long TOUNCH_RESPONSE_TIME_LIMIT = 500000000;
        private boolean isInputReceived = false;
        public int mCallBackCounts = 0;
        public boolean mInDoframe = false;
        public boolean mIsFirstFrame = false;
        public boolean mIsNeedDraw = false;
        public long mLastInputTime = 0;
        public long mLastSkippedFrameEnd = 0;
        public boolean mLastTraversal = false;
        public long mOldestInputTime = Long.MAX_VALUE;
        public long mRealFrameTime = 0;
        private int moveCount = 0;
        private String pkg = BuildConfig.FLAVOR;
        private long reportTime = 0;

        public ChoreographerState() {
        }

        public void savePkgName(String pkgName) {
            if (this.pkg.length() == 0) {
                this.pkg = pkgName;
            }
        }

        public void updateInputState(MotionEvent me) {
            int act = me.getActionMasked();
            if (act != 4) {
                if (act == 0 || act == 2 || act == 1 || act == 5 || act == 6) {
                    if (act == 2) {
                        this.moveCount++;
                        long currentTime = System.currentTimeMillis();
                        if (currentTime >= this.reportTime + 800) {
                            this.reportTime = currentTime;
                        } else {
                            return;
                        }
                    } else {
                        this.moveCount = 0;
                        this.reportTime = System.currentTimeMillis();
                    }
                    this.isInputReceived = true;
                }
            }
        }

        public void reportDrawRequestResult(boolean isScheduledDraw) {
            if (this.isInputReceived) {
                if (!isScheduledDraw) {
                    Jlog.d((int) JLID_APP_DRAW_FRAME_REQUEST, "#ARG1:<" + this.pkg + ">#ARG2:<0>#" + this.moveCount + "#ScheduledDraw:0");
                } else {
                    boolean z = this.mIsNeedDraw;
                    Jlog.d((int) JLID_APP_DRAW_FRAME_REQUEST, "#ARG1:<" + this.pkg + ">#ARG2:<" + (z ? 1 : 0) + ">#" + this.moveCount + "#ScheduledDraw:1");
                }
                this.isInputReceived = false;
            }
        }

        /* access modifiers changed from: protected */
        public void checkTounchResponseTime(String title, long nowTime) {
            long j = this.mOldestInputTime;
            if (j != Long.MAX_VALUE && this.mLastInputTime != j) {
                this.mLastInputTime = j;
                long tounchResponseTime = nowTime - j;
                if (tounchResponseTime >= TOUNCH_RESPONSE_TIME_LIMIT) {
                    Jlog.d(360, "#ARG1:<" + title + ">#ARG2:<" + (tounchResponseTime / 1000000) + ">");
                }
            }
        }
    }

    public void traceInputEventInfo(InputEvent event) {
        TraceInputInfoManager traceInputInfoManager;
        if (this.mTraceInputParams == -1) {
            this.mTraceInputParams = SystemPropertiesEx.getInt("sys.itouch.config.trace_scenes", 0);
            Log.d(TAG, "GPLOG: TraceInputParams = " + this.mTraceInputParams);
        }
        int i = this.mTraceInputParams;
        if (i != 0 && (traceInputInfoManager = this.mTraceInputInfoManager) != null) {
            traceInputInfoManager.mTraceInfoMaxNum = i;
            this.mTraceInputInfoManager.beginTraceEvent(event);
        }
    }

    public boolean isInputInAdvance() {
        return AwareUiRenderParallelManager.getInstance().isInputInAdvance();
    }

    public boolean isAnimInAdvance() {
        return AwareUiRenderParallelManager.getInstance().isAnimInAdvance();
    }

    public boolean isTouchDownEvent() {
        return AwareUiRenderParallelManager.getInstance().isTouchDownEvent();
    }

    private static class TraceInputInfoManager {
        private static final long NS_PER_MS = 1000000;
        private double mDownEventMaxHandleTime;
        private boolean mIsTrace;
        private double mMoveEventMaxHandleTime;
        private StringBuilder mTraceInfoContent;
        private int mTraceInfoMaxNum;
        private int mTraceInfoNum;

        private TraceInputInfoManager() {
            this.mTraceInfoMaxNum = 0;
            this.mTraceInfoNum = 0;
            this.mTraceInfoContent = new StringBuilder();
            this.mIsTrace = false;
            this.mMoveEventMaxHandleTime = 0.0d;
            this.mDownEventMaxHandleTime = 0.0d;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void beginTraceEvent(InputEvent event) {
            if (event instanceof MotionEvent) {
                MotionEvent me = (MotionEvent) event;
                if (me.getPointerCount() <= 2) {
                    collectTouchEventInfo(me);
                }
            }
        }

        private void collectTouchEventInfo(MotionEvent me) {
            String str;
            int actionMasked = me.getActionMasked();
            long eventTimeMs = me.getEventTime();
            long currentTimeMs = System.nanoTime() / NS_PER_MS;
            if (actionMasked == 0 || actionMasked == 5) {
                notifyTouchDownOccur(me);
                if (this.mTraceInfoContent.length() != 0) {
                    sendTraceInputInfoToItouch(this.mTraceInfoContent.toString(), this.mTraceInfoNum, this.mDownEventMaxHandleTime, this.mMoveEventMaxHandleTime);
                    clearTraceInfo();
                }
                StringBuilder traceInputInfo = new StringBuilder();
                traceInputInfo.append(System.currentTimeMillis());
                traceInputInfo.append(",");
                traceInputInfo.append(me.getAction());
                traceInputInfo.append(",");
                traceInputInfo.append(me.getPointerCount());
                traceInputInfo.append(",");
                traceInputInfo.append(eventTimeMs);
                traceInputInfo.append(",");
                traceInputInfo.append(currentTimeMs);
                StringBuilder sb = this.mTraceInfoContent;
                sb.append((CharSequence) traceInputInfo);
                sb.append(System.lineSeparator());
                this.mTraceInfoNum = 1;
                this.mIsTrace = true;
                if (((double) (currentTimeMs - eventTimeMs)) > this.mDownEventMaxHandleTime) {
                    this.mDownEventMaxHandleTime = (double) (currentTimeMs - eventTimeMs);
                }
            }
            if (actionMasked != 2 || !this.mIsTrace) {
                str = ",";
            } else {
                if (me.getHistorySize() > 0) {
                    eventTimeMs = me.getHistoricalEventTime(0);
                }
                StringBuilder traceInputInfo2 = new StringBuilder();
                traceInputInfo2.append(System.currentTimeMillis());
                traceInputInfo2.append(",");
                traceInputInfo2.append(me.getAction());
                traceInputInfo2.append(",");
                traceInputInfo2.append(me.getPointerCount());
                traceInputInfo2.append(",");
                traceInputInfo2.append(eventTimeMs);
                traceInputInfo2.append(",");
                traceInputInfo2.append(currentTimeMs);
                StringBuilder sb2 = this.mTraceInfoContent;
                sb2.append((CharSequence) traceInputInfo2);
                sb2.append(System.lineSeparator());
                if (((double) (currentTimeMs - eventTimeMs)) > this.mMoveEventMaxHandleTime) {
                    this.mMoveEventMaxHandleTime = (double) (currentTimeMs - eventTimeMs);
                }
                this.mTraceInfoNum++;
                if (this.mTraceInfoNum >= this.mTraceInfoMaxNum) {
                    str = ",";
                    sendTraceInputInfoToItouch(this.mTraceInfoContent.toString(), this.mTraceInfoNum, this.mDownEventMaxHandleTime, this.mMoveEventMaxHandleTime);
                    clearTraceInfo();
                    this.mIsTrace = false;
                } else {
                    str = ",";
                }
            }
            if ((actionMasked == 1 || actionMasked == 6) && this.mIsTrace) {
                StringBuilder traceInputInfo3 = new StringBuilder();
                traceInputInfo3.append(System.currentTimeMillis());
                traceInputInfo3.append(str);
                traceInputInfo3.append(me.getAction());
                traceInputInfo3.append(str);
                traceInputInfo3.append(me.getPointerCount());
                traceInputInfo3.append(str);
                traceInputInfo3.append(eventTimeMs);
                traceInputInfo3.append(str);
                traceInputInfo3.append(currentTimeMs);
                StringBuilder sb3 = this.mTraceInfoContent;
                sb3.append((CharSequence) traceInputInfo3);
                sb3.append(System.lineSeparator());
                sendTraceInputInfoToItouch(this.mTraceInfoContent.toString(), this.mTraceInfoNum, this.mDownEventMaxHandleTime, this.mMoveEventMaxHandleTime);
                clearTraceInfo();
                this.mIsTrace = false;
            }
        }

        private void notifyTouchDownOccur(MotionEvent me) {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            try {
                IBinder itouchService = ServiceManagerEx.getService("ITouchservice");
                if (itouchService != null) {
                    data.writeInt(me.getAction());
                    data.writeDouble((double) me.getEventTime());
                    itouchService.transact(20004, data, reply, 1);
                }
            } catch (RemoteException e) {
                Log.e(HwViewRootImpl.TAG, "notify itouch failed!!!");
            } catch (Throwable th) {
                reply.recycle();
                data.recycle();
                throw th;
            }
            reply.recycle();
            data.recycle();
        }

        private void sendTraceInputInfoToItouch(String traceInputInfo, int recordInfoNum, double downDelayTime, double moveDelayTime) {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            try {
                IBinder itouchService = ServiceManagerEx.getService("ITouchservice");
                if (itouchService != null) {
                    data.writeString(traceInputInfo);
                    data.writeInt(recordInfoNum);
                    data.writeDouble(downDelayTime);
                    data.writeDouble(moveDelayTime);
                    itouchService.transact(20000, data, reply, 1);
                }
            } catch (RemoteException e) {
                Log.e(HwViewRootImpl.TAG, "send traceInputInfo failed!!!");
            } catch (Throwable th) {
                reply.recycle();
                data.recycle();
                throw th;
            }
            reply.recycle();
            data.recycle();
        }

        private void clearTraceInfo() {
            this.mDownEventMaxHandleTime = 0.0d;
            this.mMoveEventMaxHandleTime = 0.0d;
            this.mTraceInfoContent.setLength(0);
            this.mTraceInfoNum = 0;
        }
    }
}
