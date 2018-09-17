package android.view;

import android.content.Context;
import android.graphics.Point;
import android.inputmethodservice.InputMethodService;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import android.provider.Settings.Global;
import android.util.HwPCUtils;
import android.util.HwStylusUtils;
import android.view.MotionEvent.PointerCoords;
import android.view.MotionEvent.PointerProperties;
import android.view.WindowManager.LayoutParams;
import android.vkey.SettingsHelper;
import huawei.android.provider.FingerSenseSettings;
import huawei.android.provider.HwSettings.System;

public class HwViewRootImpl implements IHwViewRootImpl {
    private static final boolean FRONT_FINGERPRINT_NAVIGATION = SystemProperties.getBoolean("ro.config.hw_front_fp_navi", false);
    private static final int FRONT_FINGERPRINT_NAVIGATION_TRIKEY = SystemProperties.getInt("ro.config.hw_front_fp_trikey", 0);
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

    /* JADX WARNING: Missing block: B:5:0x0015, code:
            return false;
     */
    /* JADX WARNING: Missing block: B:10:0x0020, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean filterDecorPointerEvent(Context context, MotionEvent event, int action, LayoutParams windowattr, Display disp) {
        boolean z = true;
        if (HwPCUtils.isValidExtDisplayId(context) || ((disp != null && HwPCUtils.isValidExtDisplayId(disp.getDisplayId())) || context == null || (isHwNaviBar ^ 1) != 0 || disp == null || windowattr == null || !isNaviEnable(context) || SettingsHelper.isTouchPlusOn(context) || (windowattr.privateFlags & 1024) != 0)) {
            return false;
        }
        Point pt;
        if (action == 0) {
            boolean z2;
            this.isDecorPointerEvent = false;
            pt = this.mDisplayPoint == null ? getDisplayPoint(disp) : this.mDisplayPoint;
            this.mHitRegionToMax = (int) (((double) context.getResources().getDimensionPixelSize(17105141)) / 3.5d);
            if (pt.y > pt.x) {
                if (event.getRawY() > ((float) (pt.y - this.mHitRegionToMax))) {
                    z2 = true;
                } else {
                    z2 = false;
                }
                this.isDecorPointerEvent = z2;
            } else {
                this.isDecorPointerEvent = event.getRawX() > ((float) (pt.x - this.mHitRegionToMax));
            }
            z2 = this.isDecorPointerEvent ? Global.getInt(context.getContentResolver(), System.NAVIGATIONBAR_IS_MIN, 0) != 0 : false;
            this.isDecorPointerEvent = z2;
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
            pt = this.mDisplayPoint == null ? getDisplayPoint(disp) : this.mDisplayPoint;
            if (!this.isDecorPointerEvent) {
                this.mIsRedispatchDownAction = false;
            } else if (pt.y > pt.x) {
                if (event.getRawY() <= ((float) (pt.y - this.mHitRegionToMax))) {
                    z = false;
                }
                this.mIsRedispatchDownAction = z;
            } else {
                if (event.getRawX() <= ((float) (pt.x - this.mHitRegionToMax))) {
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

    public boolean shouldQueueInputEvent(InputEvent event, Context context, View view, LayoutParams attr) {
        if (!(event instanceof MotionEvent)) {
            return true;
        }
        MotionEvent motionEvent = (MotionEvent) event;
        if (isStylusButtonPressed(context, motionEvent)) {
            return false;
        }
        int pointerCount = motionEvent.getPointerCount();
        for (int i = 0; i < pointerCount; i++) {
            if (motionEvent.getToolType(i) != 7) {
                return true;
            }
        }
        if (!FingerSenseSettings.isFingerSenseEnabled(context.getContentResolver()) || (attr.flags & 4096) != 0) {
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

    private boolean isStylusButtonPressed(Context context, MotionEvent motionEvent) {
        if (!HwStylusUtils.hasStylusFeature(context)) {
            return false;
        }
        boolean stylusPrimaryButtonPressed = motionEvent.getToolType(0) == 2 ? motionEvent.getButtonState() == 32 : false;
        return stylusPrimaryButtonPressed;
    }

    private boolean isNaviEnable(Context mContext) {
        return Settings.System.getInt(mContext.getContentResolver(), "enable_navbar", getDefaultNavConfig()) != 0;
    }

    private int getDefaultNavConfig() {
        if (FRONT_FINGERPRINT_NAVIGATION) {
            return FRONT_FINGERPRINT_NAVIGATION_TRIKEY == 0 ? isChinaArea() ? 0 : 1 : FRONT_FINGERPRINT_NAVIGATION_TRIKEY == 1 ? 0 : 1;
        } else {
            return 1;
        }
    }

    private static boolean isChinaArea() {
        return SystemProperties.get("ro.config.hw_optb", System.FINGERSENSE_KNUCKLE_GESTURE_OFF).equals("156");
    }

    public boolean interceptMotionEvent(View view, MotionEvent event) {
        if ((event.getMetaState() & 4096) == 0 || (event.getSource() & 2) == 0 || event.getAction() != 8) {
            return false;
        }
        return multiPointerGesture(view, event.getX(), event.getY(), event.getAxisValue(9));
    }

    private boolean multiPointerGesture(View view, float x, float y, float value) {
        int guide = value > 0.0f ? 1 : -1;
        float pointerX1 = x - 200.0f;
        float pointerY1 = y - 200.0f;
        float pointerX2 = x + 200.0f;
        float pointerY2 = y + 200.0f;
        PointerCoords[][] ppCoords = new PointerCoords[2][];
        PointerCoords[] pointerCoordsX = new PointerCoords[4];
        PointerCoords[] pointerCoordsY = new PointerCoords[4];
        for (int index = 1; index <= 4; index++) {
            float dis = (((float) index) * 30.0f) * ((float) guide);
            pointerCoordsX[index - 1] = getPonterCoords(pointerX1 - dis, pointerY1 - dis);
            pointerCoordsY[index - 1] = getPonterCoords(pointerX2 + dis, pointerY2 + dis);
        }
        ppCoords[0] = pointerCoordsX;
        ppCoords[1] = pointerCoordsY;
        return performMultiPointerGesture(view, ppCoords);
    }

    private PointerCoords getPonterCoords(float x, float y) {
        PointerCoords pc1 = new PointerCoords();
        pc1.x = x;
        pc1.y = y;
        pc1.pressure = 1.0f;
        pc1.size = 1.0f;
        return pc1;
    }

    private boolean performMultiPointerGesture(View view, PointerCoords[]... touches) {
        int x;
        int maxSteps = 0;
        for (x = 0; x < touches.length; x++) {
            if (maxSteps < touches[x].length) {
                maxSteps = touches[x].length;
            }
        }
        PointerProperties[] properties = new PointerProperties[touches.length];
        PointerCoords[] pointerCoords = new PointerCoords[touches.length];
        for (x = 0; x < touches.length; x++) {
            PointerProperties prop = new PointerProperties();
            prop.id = x;
            prop.toolType = 1;
            properties[x] = prop;
            pointerCoords[x] = touches[x][0];
        }
        long downTime = SystemClock.uptimeMillis();
        boolean ret = injectEventSync(view, MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), 0, 1, properties, pointerCoords, 0, 0, 1.0f, 1.0f, 0, 0, 4098, 0));
        for (x = 1; x < touches.length; x++) {
            ret &= injectEventSync(view, MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), getPointerAction(5, x), x + 1, properties, pointerCoords, 0, 0, 1.0f, 1.0f, 0, 0, 4098, 0));
            SystemClock.sleep(20);
        }
        for (int i = 1; i < maxSteps - 1; i++) {
            for (x = 0; x < touches.length; x++) {
                if (touches[x].length > i) {
                    pointerCoords[x] = touches[x][i];
                } else {
                    pointerCoords[x] = touches[x][touches[x].length - 1];
                }
            }
            ret &= injectEventSync(view, MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), 2, touches.length, properties, pointerCoords, 0, 0, 1.0f, 1.0f, 0, 0, 4098, 0));
            SystemClock.sleep(20);
        }
        for (x = 0; x < touches.length; x++) {
            pointerCoords[x] = touches[x][touches[x].length - 1];
        }
        for (x = 1; x < touches.length; x++) {
            ret &= injectEventSync(view, MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), getPointerAction(6, x), x + 1, properties, pointerCoords, 0, 0, 1.0f, 1.0f, 0, 0, 4098, 0));
        }
        return ret & injectEventSync(view, MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), 1, 1, properties, pointerCoords, 0, 0, 1.0f, 1.0f, 0, 0, 4098, 0));
    }

    private boolean injectEventSync(View view, MotionEvent event) {
        return view.dispatchPointerEvent(event);
    }

    private int getPointerAction(int motionEnvent, int index) {
        return (index << 8) + motionEnvent;
    }
}
