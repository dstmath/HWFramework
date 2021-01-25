package huawei.com.android.server.policy.stylus;

import android.common.HwFrameworkFactory;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Flog;
import android.util.Log;
import android.view.GestureDetector;
import android.view.InputDevice;
import android.view.MotionEvent;
import com.android.server.appactcontrol.AppActConstant;
import com.android.server.lights.LightsManagerEx;
import com.huawei.android.inputmethod.HwInputMethodManager;
import java.util.ServiceConfigurationError;

public class StylusGestureDetector implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {
    private static final long DEFAULT_TIME = -1;
    private static final int DEFAULT_TOOL_TYPE = -1;
    private static final int DOUBLE_KNOCK_RADIUS = 10;
    private static final int DOUBLE_KNOCK_TIMEOUT_MS = 400;
    private static final int HW_S_KEYBOARD_PRODUCT_ID = 4817;
    private static final int HW_S_KEYBOARD_VENDOR_ID = 1455;
    public static final boolean IS_TABLET = "tablet".equals(SystemProperties.get("ro.build.characteristics", AppActConstant.VALUE_DEFAULT));
    private static final int MIN_DELAY_FOR_SCREENSHOT = 1000;
    private static final String TAG = "StylusGestureDetector";
    private static final String WRITE_IME_ENABLE = "support_write_ime";
    private Context mContext;
    private final GestureDetector mGestureDetector;
    private final StylusGestureListener mGestureListener;
    private boolean mIsBtnPressedWhenMotionUp = false;
    private boolean mIsFirstSwtich = false;
    private boolean mIsHideMenuView = false;
    private long mLastDownTime;
    private long mLastScreenshotTime = DEFAULT_TIME;
    private float mLastX;
    private float mLastY;
    private final StylusGestureRecognizeListener mStylusGestureListener;
    private int mToolType = -1;

    public interface StylusGestureRecognizeListener {
        void notifySwtichInputMethod(boolean z);

        void onStylusDoubleTapPerformed();

        void onStylusSingleTapPerformed(MotionEvent motionEvent, boolean z);
    }

    public StylusGestureDetector(Context context, StylusGestureListener listener) {
        this.mContext = context;
        this.mStylusGestureListener = listener;
        this.mGestureListener = listener;
        this.mGestureDetector = new GestureDetector(this.mContext, this);
        this.mGestureDetector.setIsLongpressEnabled(true);
        this.mGestureDetector.setOnDoubleTapListener(this);
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (event == null) {
            Log.w(TAG, "StylusGestureDetector <- onTouchEvent event is null");
            return false;
        }
        if (event.getAction() == 1 || event.getAction() == 3) {
            Log.d(TAG, "StylusGestureDetector <- onTouchEvent.ACTION_UP");
            if (this.mIsHideMenuView) {
                this.mStylusGestureListener.onStylusSingleTapPerformed(event, false);
                this.mIsHideMenuView = false;
            }
        }
        this.mGestureDetector.onTouchEvent(event);
        return false;
    }

    public void shouldSwtichInputMethod(MotionEvent motionEvent) {
        if (motionEvent != null && motionEvent.getAction() == 0 && !motionEvent.isButtonPressed(32) && !motionEvent.isButtonPressed(64)) {
            boolean z = false;
            int currentToolType = motionEvent.getToolType(0);
            boolean isStylus = currentToolType == 2;
            if (!IS_TABLET) {
                if (!isStylus) {
                    z = true;
                }
                HwInputMethodManager.setInputSource(z);
                return;
            }
            if (isStylus && isHardwareKeyboardConnected()) {
                Settings.Secure.putInt(this.mContext.getContentResolver(), WRITE_IME_ENABLE, 1);
                Log.i(TAG, "shouldSwtichInputMethod-showWriteIme");
            } else if (isStylus || !isHardwareKeyboardConnected()) {
                Log.w(TAG, "hardware keyboard connected fail");
            } else {
                Settings.Secure.putInt(this.mContext.getContentResolver(), WRITE_IME_ENABLE, 0);
            }
            if (!this.mIsFirstSwtich) {
                this.mIsFirstSwtich = true;
                this.mToolType = currentToolType;
                this.mStylusGestureListener.notifySwtichInputMethod(isStylus);
            } else if (this.mToolType != currentToolType) {
                this.mToolType = currentToolType;
                this.mStylusGestureListener.notifySwtichInputMethod(isStylus);
            }
        }
    }

    public void setToolType() {
        this.mToolType = -1;
    }

    private boolean isHardwareKeyboardConnected() {
        Log.i(TAG, "isHardwareKeyboardConnected--begin");
        int[] devices = InputDevice.getDeviceIds();
        boolean isConnected = false;
        int i = 0;
        while (true) {
            if (i >= devices.length) {
                break;
            }
            InputDevice device = InputDevice.getDevice(devices[i]);
            if (device != null) {
                if (device.getProductId() != HW_S_KEYBOARD_PRODUCT_ID || device.getVendorId() != HW_S_KEYBOARD_VENDOR_ID) {
                    if (device.isExternal() && (device.getSources() & LightsManagerEx.LIGHT_ID_SMARTBACKLIGHT) != 0) {
                        isConnected = true;
                        break;
                    }
                } else {
                    isConnected = true;
                    break;
                }
            }
            i++;
        }
        Log.i(TAG, "isHardwareKeyboardConnected--end");
        return isConnected;
    }

    private void preloadFloatMenuService(MotionEvent event) {
        if (event.getToolType(0) == 2 && event.getButtonState() == 32) {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(StylusGestureSettings.FLOAT_ENTRANCE_PACKAGE_NAME, StylusGestureSettings.FLOAT_ENTRANCE_CLASSNAME));
            intent.putExtra("positionX", (int) event.getRawX());
            intent.putExtra("positionY", (int) event.getRawY());
            intent.putExtra("prepareStatus", 0);
            try {
                this.mContext.startServiceAsUser(intent, UserHandle.CURRENT_OR_SELF);
            } catch (ServiceConfigurationError sce) {
                Log.e(TAG, "can not start service: " + sce.getMessage());
            } catch (Exception e) {
                Log.e(TAG, "can not start service");
            }
        }
    }

    @Override // android.view.GestureDetector.OnGestureListener
    public boolean onSingleTapUp(MotionEvent event) {
        if (event == null) {
            return false;
        }
        Log.d(TAG, "StylusGestureDetector <- onSingleTapUp");
        if (IS_TABLET) {
            preloadFloatMenuService(event);
        }
        if (this.mGestureListener.isPenButtonPressed()) {
            this.mIsBtnPressedWhenMotionUp = true;
        }
        return false;
    }

    @Override // android.view.GestureDetector.OnDoubleTapListener
    public boolean onSingleTapConfirmed(MotionEvent event) {
        if (event == null) {
            return false;
        }
        Log.d(TAG, "StylusGestureDetector <- onSingleTapConfirmed");
        if (event.getToolType(0) == 2 && (event.getButtonState() == 32 || this.mIsBtnPressedWhenMotionUp)) {
            this.mStylusGestureListener.onStylusSingleTapPerformed(event, true);
            this.mIsBtnPressedWhenMotionUp = false;
        }
        return false;
    }

    @Override // android.view.GestureDetector.OnDoubleTapListener
    public boolean onDoubleTap(MotionEvent event) {
        if (event == null) {
            return false;
        }
        Log.d(TAG, "StylusGestureDetector <- onDoubleTap");
        boolean isCoverOpen = HwFrameworkFactory.getCoverManager().isCoverOpen();
        if (!isCoverOpen) {
            Log.i(TAG, "onDoubleTap, isCoverOpen = " + isCoverOpen);
            return false;
        }
        this.mStylusGestureListener.onStylusSingleTapPerformed(event, false);
        if (System.currentTimeMillis() - this.mLastScreenshotTime > 1000) {
            if (event.getToolType(0) == 2 && (event.getButtonState() == 32 || this.mGestureListener.isPenButtonPressed())) {
                this.mStylusGestureListener.onStylusDoubleTapPerformed();
            }
            this.mLastScreenshotTime = System.currentTimeMillis();
        }
        return false;
    }

    @Override // android.view.GestureDetector.OnDoubleTapListener
    public boolean onDoubleTapEvent(MotionEvent event) {
        Log.d(TAG, "StylusGestureDetector <- onDoubleTapEvent");
        return false;
    }

    @Override // android.view.GestureDetector.OnGestureListener
    public void onShowPress(MotionEvent event) {
        Log.d(TAG, "StylusGestureDetector <- onShowPress");
    }

    @Override // android.view.GestureDetector.OnGestureListener
    public boolean onScroll(MotionEvent event1, MotionEvent event2, float distanceX, float distanceY) {
        Log.d(TAG, "StylusGestureDetector <- onScroll");
        this.mIsHideMenuView = true;
        return false;
    }

    @Override // android.view.GestureDetector.OnGestureListener
    public void onLongPress(MotionEvent event) {
        Log.d(TAG, "StylusGestureDetector <- onLongPress");
        this.mIsHideMenuView = true;
        Flog.bdReport(991310952, "issuccess", AppActConstant.VALUE_TRUE);
    }

    @Override // android.view.GestureDetector.OnGestureListener
    public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
        Log.d(TAG, "StylusGestureDetector <- onFling");
        return false;
    }

    @Override // android.view.GestureDetector.OnGestureListener
    public boolean onDown(MotionEvent event) {
        if (event == null) {
            return false;
        }
        Log.d(TAG, "StylusGestureDetector <- onDown");
        double dist = Math.sqrt(Math.pow((double) (event.getX() - this.mLastX), 2.0d) + Math.pow((double) (event.getY() - this.mLastY), 2.0d));
        if (event.getEventTime() - this.mLastDownTime >= 400 || dist >= 10.0d) {
            this.mLastDownTime = event.getEventTime();
            this.mLastX = event.getX();
            this.mLastY = event.getY();
        } else {
            onDoubleTap(event);
            this.mLastDownTime = DEFAULT_TIME;
        }
        return false;
    }
}
