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
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.huawei.android.inputmethod.HwInputMethodManager;
import java.util.ServiceConfigurationError;

public class StylusGestureDetector implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {
    private static final int DOUBLE_KNOCK_RADIUS = 10;
    private static final int DOUBLE_KNOCK_TIMEOUT_MS = 400;
    public static final boolean IS_TABLET = "tablet".equals(SystemProperties.get("ro.build.characteristics", MemoryConstant.MEM_SCENE_DEFAULT));
    private static final int MIN_DELAY_FOR_SCREENSHOT = 1000;
    private static final String TAG = "StylusGestureDetector";
    private static final String WRITE_IME_ENABLE = "support_write_ime";
    private boolean isFirstSwtich = false;
    private Context mContext;
    private final GestureDetector mGestureDetector;
    private boolean mHideMenuView = false;
    private long mLastDownTime;
    private long mLastScreenshotTime = -1;
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
        this.mGestureDetector = new GestureDetector(this.mContext, this);
        this.mGestureDetector.setIsLongpressEnabled(true);
        this.mGestureDetector.setOnDoubleTapListener(this);
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == 1 || event.getAction() == 3) {
            Log.d(TAG, "StylusGestureDetector <- onTouchEvent.ACTION_UP");
            if (this.mHideMenuView) {
                this.mStylusGestureListener.onStylusSingleTapPerformed(event, false);
                this.mHideMenuView = false;
            }
        }
        this.mGestureDetector.onTouchEvent(event);
        return false;
    }

    public void shouldSwtichInputMethod(MotionEvent motionEvent) {
        if (motionEvent.getAction() == 0 && !motionEvent.isButtonPressed(32) && !motionEvent.isButtonPressed(64)) {
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
            } else if (!isStylus && isHardwareKeyboardConnected()) {
                Settings.Secure.putInt(this.mContext.getContentResolver(), WRITE_IME_ENABLE, 0);
            }
            if (!this.isFirstSwtich) {
                this.isFirstSwtich = true;
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
                if (device.getProductId() != 4817 || device.getVendorId() != 1455) {
                    if (device.isExternal() && (device.getSources() & 257) != 0) {
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

    private void preloadFloatMenuService(MotionEvent e) {
        if (e.getToolType(0) == 2 && e.getButtonState() == 32) {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(StylusGestureSettings.FLOAT_ENTRANCE_PACKAGE_NAME, StylusGestureSettings.FLOAT_ENTRANCE_CLASSNAME));
            intent.putExtra("positionX", (int) e.getRawX());
            intent.putExtra("positionY", (int) e.getRawY());
            intent.putExtra("prepareStatus", 0);
            try {
                this.mContext.startServiceAsUser(intent, UserHandle.CURRENT_OR_SELF);
            } catch (ServiceConfigurationError sce) {
                Log.w(TAG, "can not start service: " + sce);
            } catch (Exception ex) {
                Log.w(TAG, "can not start service: " + ex);
            }
        }
    }

    public boolean onSingleTapUp(MotionEvent e) {
        Log.d(TAG, "StylusGestureDetector <- onSingleTapUp");
        if (IS_TABLET) {
            preloadFloatMenuService(e);
        }
        return false;
    }

    public boolean onSingleTapConfirmed(MotionEvent e) {
        Log.d(TAG, "StylusGestureDetector <- onSingleTapConfirmed");
        if (e.getToolType(0) == 2 && e.getButtonState() == 32) {
            this.mStylusGestureListener.onStylusSingleTapPerformed(e, true);
        }
        return false;
    }

    public boolean onDoubleTap(MotionEvent e) {
        Log.d(TAG, "StylusGestureDetector <- onDoubleTap");
        boolean isCoverOpen = HwFrameworkFactory.getCoverManager().isCoverOpen();
        if (!isCoverOpen) {
            Log.i(TAG, "onDoubleTap, isCoverOpen = " + isCoverOpen);
            return false;
        }
        this.mStylusGestureListener.onStylusSingleTapPerformed(e, false);
        if (System.currentTimeMillis() - this.mLastScreenshotTime > 1000) {
            if (e.getToolType(0) == 2 && e.getButtonState() == 32) {
                this.mStylusGestureListener.onStylusDoubleTapPerformed();
            }
            this.mLastScreenshotTime = System.currentTimeMillis();
        }
        return false;
    }

    public boolean onDoubleTapEvent(MotionEvent e) {
        Log.d(TAG, "StylusGestureDetector <- onDoubleTapEvent");
        return false;
    }

    public void onShowPress(MotionEvent e) {
        Log.d(TAG, "StylusGestureDetector <- onShowPress");
    }

    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        Log.d(TAG, "StylusGestureDetector <- onScroll");
        this.mHideMenuView = true;
        return false;
    }

    public void onLongPress(MotionEvent e) {
        Log.d(TAG, "StylusGestureDetector <- onLongPress");
        this.mHideMenuView = true;
        Flog.bdReport(this.mContext, 952, "true");
    }

    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        Log.d(TAG, "StylusGestureDetector <- onFling");
        return false;
    }

    public boolean onDown(MotionEvent e) {
        Log.d(TAG, "StylusGestureDetector <- onDown");
        double dist = Math.sqrt(Math.pow((double) (e.getX() - this.mLastX), 2.0d) + Math.pow((double) (e.getY() - this.mLastY), 2.0d));
        if (e.getEventTime() - this.mLastDownTime >= 400 || dist >= 10.0d) {
            this.mLastDownTime = e.getEventTime();
            this.mLastX = e.getX();
            this.mLastY = e.getY();
        } else {
            onDoubleTap(e);
            this.mLastDownTime = -1;
        }
        return false;
    }
}
