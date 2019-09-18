package android.view;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Point;
import android.inputmethodservice.InputMethodService;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.HwPCUtils;
import android.util.HwStylusUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.WindowManager;
import com.huawei.hsm.permission.StubController;
import huawei.android.provider.FingerSenseSettings;
import huawei.android.provider.HwSettings;

public class HwViewRootImpl implements IHwViewRootImpl {
    private static final boolean FRONT_FINGERPRINT_NAVIGATION = SystemProperties.getBoolean("ro.config.hw_front_fp_navi", false);
    private static final int FRONT_FINGERPRINT_NAVIGATION_TRIKEY = SystemProperties.getInt("ro.config.hw_front_fp_trikey", 0);
    private static final boolean IS_TABLET = "tablet".equals(SystemProperties.get("ro.build.characteristics", "default"));
    private static final int MOTION_EVENT_INJECTION_DELAY_MILLIS = 20;
    private static final int NAVIGATION_DISABLE = 0;
    private static final int NAVIGATION_ENABLE = 1;
    static final boolean isHwNaviBar = SystemProperties.getBoolean("ro.config.hw_navigationbar", false);
    private static HwViewRootImpl mInstance = null;
    private boolean isDecorPointerEvent = false;
    Point mDisplayPoint;
    private MotionEvent mDownEvent = null;
    int mHitRegionToMax = 20;
    private boolean mIsRedispatchDownAction = false;
    private boolean mIsStylusEffective = true;
    private StylusTouchListener mStylusTouchListener = null;

    protected HwViewRootImpl() {
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
        if (HwPCUtils.isValidExtDisplayId(context) || ((disp != null && HwPCUtils.isValidExtDisplayId(disp.getDisplayId())) || context == null || !isHwNaviBar || disp == null || windowattr == null || (windowattr.hwFlags & StubController.PERMISSION_WIFI) != 0 || (windowattr.privateFlags & 1024) != 0)) {
            return false;
        }
        boolean z = true;
        if (action == 0) {
            this.isDecorPointerEvent = false;
            Point pt = this.mDisplayPoint == null ? getDisplayPoint(disp) : this.mDisplayPoint;
            this.mHitRegionToMax = (int) (((double) context.getResources().getDimensionPixelSize(17105186)) / 3.5d);
            if (pt.y > pt.x) {
                this.isDecorPointerEvent = event.getRawY() > ((float) (pt.y - this.mHitRegionToMax));
            } else {
                this.isDecorPointerEvent = event.getRawX() > ((float) (pt.x - this.mHitRegionToMax));
            }
            this.isDecorPointerEvent = this.isDecorPointerEvent && canNavBarFlingOut(context, windowattr);
            if (this.isDecorPointerEvent) {
                this.mDownEvent = event.copy();
                return true;
            }
            this.mDownEvent = null;
            this.mIsRedispatchDownAction = false;
        } else if (action == 3) {
            this.mDownEvent = null;
            this.isDecorPointerEvent = false;
        } else if (action == 1) {
            Point pt2 = this.mDisplayPoint == null ? getDisplayPoint(disp) : this.mDisplayPoint;
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
        boolean navBarIsMin = Settings.Global.getInt(context.getContentResolver(), HwSettings.System.NAVIGATIONBAR_IS_MIN, 0) == 1;
        if (2000 != windowattr.type || (windowattr.hwFlags & 4) == 0) {
            isGestureNavigationMode = Settings.Secure.getInt(context.getContentResolver(), HwSettings.Secure.KEY_SECURE_GESTURE_NAVIGATION, 0) == 1;
        } else {
            isGestureNavigationMode = Settings.Secure.getIntForUser(context.getContentResolver(), HwSettings.Secure.KEY_SECURE_GESTURE_NAVIGATION, 0, ActivityManager.getCurrentUser()) == 1;
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

    public boolean shouldQueueInputEvent(InputEvent event, Context context, View view, WindowManager.LayoutParams attr) {
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
        if ((attr.hwFlags & StubController.PERMISSION_WIFI) != 0 || !FingerSenseSettings.isFingerSenseEnabled(context.getContentResolver()) || (attr.flags & 4096) != 0) {
            return true;
        }
        if (view == null) {
            return false;
        }
        Context viewContext = view.getContext();
        if (viewContext == null || !(viewContext instanceof ContextThemeWrapper)) {
            return false;
        }
        return ((ContextThemeWrapper) viewContext).getBaseContext() instanceof InputMethodService;
    }

    private boolean isStylusButtonPressed(Context context, int windowType, MotionEvent motionEvent) {
        if (HwStylusUtils.hasStylusFeature(context)) {
            boolean stylusPrimaryButtonPressed = motionEvent.getToolType(0) == 2 && motionEvent.getButtonState() == 32;
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
        return Settings.System.getInt(mContext.getContentResolver(), HwSettings.System.NAVIGATION_BAR_ENABLE, getDefaultNavConfig()) != 0;
    }

    private int getDefaultNavConfig() {
        if (!FRONT_FINGERPRINT_NAVIGATION) {
            return 1;
        }
        if (FRONT_FINGERPRINT_NAVIGATION_TRIKEY == 0) {
            if (isChinaArea()) {
                return 0;
            }
            return 1;
        } else if (FRONT_FINGERPRINT_NAVIGATION_TRIKEY == 1) {
            return 0;
        } else {
            return 1;
        }
    }

    private static boolean isChinaArea() {
        return SystemProperties.get("ro.config.hw_optb", HwSettings.System.FINGERSENSE_KNUCKLE_GESTURE_OFF).equals("156");
    }

    public boolean interceptMotionEvent(View view, MotionEvent event) {
        if ((event.getMetaState() & 4096) == 0 || (event.getSource() & 2) == 0 || event.getAction() != 8) {
            return false;
        }
        return multiPointerGesture(view, event.getX(), event.getY(), event.getAxisValue(9));
    }

    private boolean multiPointerGesture(View view, float x, float y, float value) {
        int ponterCount = 2;
        int guide = value > 0.0f ? 1 : -1;
        float pointerX1 = x - 200.0f;
        float pointerY1 = y - 200.0f;
        float pointerX2 = x + 200.0f;
        float pointerY2 = y + 200.0f;
        MotionEvent.PointerCoords[][] ppCoords = new MotionEvent.PointerCoords[2][];
        int i = 4;
        MotionEvent.PointerCoords[] pointerCoordsX = new MotionEvent.PointerCoords[4];
        MotionEvent.PointerCoords[] pointerCoordsY = new MotionEvent.PointerCoords[4];
        int index = 1;
        while (true) {
            int index2 = index;
            if (index2 <= i) {
                float dis = 30.0f * ((float) index2) * ((float) guide);
                int ponterCount2 = ponterCount;
                MotionEvent.PointerCoords pcx = getPonterCoords(pointerX1 - dis, pointerY1 - dis);
                pointerCoordsX[index2 - 1] = pcx;
                MotionEvent.PointerCoords pointerCoords = pcx;
                pointerCoordsY[index2 - 1] = getPonterCoords(pointerX2 + dis, pointerY2 + dis);
                index = index2 + 1;
                ponterCount = ponterCount2;
                i = 4;
            } else {
                ppCoords[0] = pointerCoordsX;
                ppCoords[1] = pointerCoordsY;
                return performMultiPointerGesture(view, ppCoords);
            }
        }
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
        View view2 = view;
        MotionEvent.PointerCoords[][] pointerCoordsArr = touches;
        int maxSteps = 0;
        for (int x = 0; x < pointerCoordsArr.length; x++) {
            maxSteps = maxSteps < pointerCoordsArr[x].length ? pointerCoordsArr[x].length : maxSteps;
        }
        MotionEvent.PointerProperties[] properties = new MotionEvent.PointerProperties[pointerCoordsArr.length];
        MotionEvent.PointerCoords[] pointerCoords = new MotionEvent.PointerCoords[pointerCoordsArr.length];
        for (int x2 = 0; x2 < pointerCoordsArr.length; x2++) {
            MotionEvent.PointerProperties prop = new MotionEvent.PointerProperties();
            prop.id = x2;
            prop.toolType = 1;
            properties[x2] = prop;
            pointerCoords[x2] = pointerCoordsArr[x2][0];
        }
        long downTime = SystemClock.uptimeMillis();
        int x3 = 1;
        MotionEvent.PointerCoords[] pointerCoords2 = pointerCoords;
        MotionEvent event = MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), 0, 1, properties, pointerCoords, 0, 0, 1.0f, 1.0f, 0, 0, 4098, 0);
        boolean ret = true & injectEventSync(view2, event);
        MotionEvent motionEvent = event;
        int x4 = 1;
        while (x4 < pointerCoordsArr.length) {
            MotionEvent event2 = MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), getPointerAction(5, x4), x4 + 1, properties, pointerCoords2, 0, 0, 1.0f, 1.0f, 0, 0, 4098, 0);
            ret &= injectEventSync(view2, event2);
            SystemClock.sleep(20);
            x4++;
            MotionEvent motionEvent2 = event2;
            maxSteps = maxSteps;
        }
        int i = 1;
        while (i < maxSteps - 1) {
            for (int x5 = 0; x5 < pointerCoordsArr.length; x5++) {
                if (pointerCoordsArr[x5].length > i) {
                    pointerCoords2[x5] = pointerCoordsArr[x5][i];
                } else {
                    pointerCoords2[x5] = pointerCoordsArr[x5][pointerCoordsArr[x5].length - 1];
                }
            }
            MotionEvent event3 = MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), 2, pointerCoordsArr.length, properties, pointerCoords2, 0, 0, 1.0f, 1.0f, 0, 0, 4098, 0);
            ret &= injectEventSync(view2, event3);
            SystemClock.sleep(20);
            i++;
            MotionEvent motionEvent3 = event3;
        }
        int x6 = 0;
        while (true) {
            int x7 = x6;
            if (x7 >= pointerCoordsArr.length) {
                break;
            }
            pointerCoords2[x7] = pointerCoordsArr[x7][pointerCoordsArr[x7].length - 1];
            x6 = x7 + 1;
        }
        while (true) {
            int x8 = x3;
            if (x8 < pointerCoordsArr.length) {
                MotionEvent event4 = MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), getPointerAction(6, x8), x8 + 1, properties, pointerCoords2, 0, 0, 1.0f, 1.0f, 0, 0, 4098, 0);
                ret &= injectEventSync(view2, event4);
                x3 = x8 + 1;
                MotionEvent motionEvent4 = event4;
            } else {
                return ret & injectEventSync(view2, MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), 1, 1, properties, pointerCoords2, 0, 0, 1.0f, 1.0f, 0, 0, 4098, 0));
            }
        }
    }

    private boolean injectEventSync(View view, MotionEvent event) {
        return view.dispatchPointerEvent(event);
    }

    private int getPointerAction(int motionEnvent, int index) {
        return (index << 8) + motionEnvent;
    }
}
