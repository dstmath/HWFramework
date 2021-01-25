package com.android.server.input;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import com.android.server.hidata.arbitration.HwArbitrationDEFS;
import com.android.server.input.HwCircleAnimation;
import com.huawei.hiai.awareness.AwarenessInnerConstants;

public class HwCircleWindow {
    public static final int ANIMATION_TYPE_APP = 4;
    public static final int ANIMATION_TYPE_BACK = 2;
    public static final int ANIMATION_TYPE_HOME = 1;
    public static final int ANIMATION_TYPE_MULTI = 3;
    public static final int APP_MODE = 0;
    private static final float INACCURACY_TO_CENTER = 0.5f;
    private static final float LIMIT = 0.2f;
    private static final int MSG_DISPATCH_CREATE_WIN = 1;
    private static final int MSG_DISPATCH_DESTORY_WIN = 2;
    private static final int MSG_DISPATCH_IMMERSIVEMODE = 7;
    private static final int MSG_DISPATCH_ON_EVENT = 3;
    private static final int MSG_DISPATCH_ON_EVENT_DEBUG = 5;
    private static final int MSG_DISPATCH_PROMPT_TIMEOUT = 6;
    private static final int MSG_DISPATCH_RESET_ANIMATION = 4;
    private static final int MSG_DISPATCH_UPDATEVIEW = 8;
    public static final int NAVI_MODE = 1;
    public static final int NEEDTIPNUM_DISABLE = 2;
    public static final int NEEDTIPNUM_DISPLAYED = 3;
    public static final int NEEDTIPNUM_ENABLE = 1;
    public static final int NEEDTIPNUM_FIRST = 0;
    private static final float PRESS_LAUNCH_LIMIT = 0.45f;
    private static final float PRESS_LAUNCH_LIMIT_COEF = 0.6f;
    private static final float PRESS_START_LIMIT = 0.3f;
    private static final int PROMPT_TIMEOUT = 100;
    private static final String TAG = "pressure:hwCircleWindow";
    private static HwCircleWindow sHwCircleWindow = null;
    private HandlerThread mCircleHandlerThread;
    private Context mContext;
    int mDisplayHeight;
    int mDisplayWidth;
    private Handler mHandler;
    private HwCircle mHwCircle;
    private float mInaccuracyToCenter = 0.5f;
    private boolean mIsCanNaviDraw = true;
    private boolean mIsDebug = false;
    private boolean mIsImmersiveMode = false;
    private boolean mIsNeedTip = false;
    private boolean mIsViewAdd = false;
    private WindowManager.LayoutParams mLayoutParams = null;
    private float mLimit = 0.2f;
    private final Object mLock = new Object();
    private int mMode = 0;
    private String mNeedTip = "pressure_needTip";
    private float mPressLaunchLimit = PRESS_LAUNCH_LIMIT;
    private float mPressStartLimit = PRESS_START_LIMIT;
    private WindowManager mWindowManager = null;

    protected HwCircleWindow(Context context) {
        this.mContext = context;
        createCircleNoWindow();
    }

    public static synchronized HwCircleWindow getInstance(Context context) {
        HwCircleWindow hwCircleWindow;
        synchronized (HwCircleWindow.class) {
            if (sHwCircleWindow == null) {
                sHwCircleWindow = new HwCircleWindow(context);
            }
            hwCircleWindow = sHwCircleWindow;
        }
        return hwCircleWindow;
    }

    public void setNeedTip(boolean isNeedtip) {
        this.mIsNeedTip = isNeedtip;
    }

    public void setDisplay(int width, int height) {
        this.mDisplayWidth = width;
        this.mDisplayHeight = height;
    }

    public void createCircleNoWindow() {
        if (this.mCircleHandlerThread == null) {
            this.mCircleHandlerThread = new HandlerThread("circle");
            this.mCircleHandlerThread.start();
        }
        if (this.mHandler == null) {
            this.mHandler = new HandlerEx(this.mCircleHandlerThread.getLooper());
        }
        if (this.mHwCircle == null) {
            this.mHwCircle = new HwCircle(this.mContext);
            this.mHwCircle.setNeedTip(this.mIsNeedTip);
        }
    }

    public void createCircleWindow() {
        Handler handler = this.mHandler;
        if (handler != null) {
            this.mHandler.sendMessage(handler.obtainMessage(1));
        }
        HwCircle hwCircle = this.mHwCircle;
        if (hwCircle != null) {
            hwCircle.setNeedTip(this.mIsNeedTip);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void createCircleWindowLock() {
        synchronized (this.mLock) {
            if (!this.mIsViewAdd) {
                if (this.mLayoutParams == null) {
                    this.mLayoutParams = new WindowManager.LayoutParams(-1, -1);
                }
                this.mLayoutParams.type = HwArbitrationDEFS.MSG_MPLINK_STOP_COEX_SUCC;
                this.mLayoutParams.flags = 1304;
                if (ActivityManager.isHighEndGfx()) {
                    this.mLayoutParams.flags |= 16777216;
                    this.mLayoutParams.privateFlags |= 2;
                }
                this.mLayoutParams.privateFlags |= 16;
                this.mLayoutParams.format = -3;
                this.mLayoutParams.setTitle("hwPressueWindow");
                this.mLayoutParams.inputFeatures |= 2;
                if (this.mWindowManager == null) {
                    this.mWindowManager = (WindowManager) this.mContext.getSystemService("window");
                }
                this.mWindowManager.addView(this.mHwCircle, this.mLayoutParams);
                this.mIsViewAdd = true;
                if (this.mHandler != null) {
                    this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(6), 100);
                }
                Log.v(TAG, "createCircleWindowLock mIsViewAdd = " + this.mIsViewAdd);
            } else if (this.mHandler != null) {
                this.mHandler.sendMessage(this.mHandler.obtainMessage(8));
            }
        }
    }

    public void destoryCircleWindowForAPP() {
        synchronized (this.mLock) {
            if (this.mMode == 0 && this.mIsViewAdd) {
                destoryCircleWindowAndBitmap();
            }
        }
    }

    public void destoryCircleWindowAndBitmap() {
        Handler handler = this.mHandler;
        if (handler != null) {
            this.mHandler.sendMessage(handler.obtainMessage(2));
        }
    }

    public void destoryCircleWindowAndBitmapLock() {
        synchronized (this.mLock) {
            if (!(!this.mIsViewAdd || this.mHwCircle == null || this.mWindowManager == null)) {
                this.mHwCircle.release();
                this.mWindowManager.removeView(this.mHwCircle);
                Log.v(TAG, "destoryCircleWindowAndBitmapLock mIsViewAdd = " + this.mIsViewAdd);
                this.mIsViewAdd = false;
            }
            this.mLayoutParams = null;
            this.mWindowManager = null;
        }
    }

    public void onTouchEvent(MotionEvent event, int type) {
        Handler handler = this.mHandler;
        if (handler != null) {
            Message msg1 = handler.obtainMessage(3, event);
            msg1.arg1 = type;
            this.mHandler.sendMessage(msg1);
        }
    }

    public void onTouchEventDebug(MotionEvent event) {
        Handler handler = this.mHandler;
        if (handler != null) {
            this.mHandler.sendMessage(handler.obtainMessage(5, event));
        }
    }

    /* access modifiers changed from: private */
    public class HandlerEx extends Handler {
        HandlerEx(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    HwCircleWindow.this.createCircleWindowLock();
                    return;
                case 2:
                    HwCircleWindow.this.destoryCircleWindowAndBitmapLock();
                    return;
                case 3:
                    if (HwCircleWindow.this.mHwCircle != null && msg.obj != null) {
                        HwCircleWindow.this.mHwCircle.onTouchEvent((MotionEvent) msg.obj, msg.arg1);
                        return;
                    }
                    return;
                case 4:
                    if (HwCircleWindow.this.mHwCircle != null) {
                        HwCircleWindow.this.mHwCircle.resetAnimaion();
                        return;
                    }
                    return;
                case 5:
                    if (HwCircleWindow.this.mHwCircle != null && msg.obj != null) {
                        HwCircleWindow.this.mHwCircle.onTouchEventDebug((MotionEvent) msg.obj);
                        return;
                    }
                    return;
                case 6:
                    handleDispatchPromptTimeout();
                    return;
                case 7:
                    if (HwCircleWindow.this.mIsImmersiveMode && HwCircleWindow.this.mHwCircle != null) {
                        HwCircleWindow.this.mHwCircle.onAnimationUpdate();
                        return;
                    }
                    return;
                case 8:
                    if (HwCircleWindow.this.mHwCircle != null) {
                        HwCircleWindow.this.mHwCircle.onAnimationUpdate();
                        return;
                    }
                    return;
                default:
                    return;
            }
        }

        private void handleDispatchPromptTimeout() {
            if (HwCircleWindow.this.mHwCircle != null) {
                Log.d(HwCircleWindow.TAG, "hwCircleWindow timeout");
                if (Settings.System.getIntForUser(HwCircleWindow.this.mContext.getContentResolver(), HwCircleWindow.this.mNeedTip, 0, ActivityManager.getCurrentUser()) == 1) {
                    Settings.System.putIntForUser(HwCircleWindow.this.mContext.getContentResolver(), HwCircleWindow.this.mNeedTip, 3, ActivityManager.getCurrentUser());
                    HwCircleWindow.this.mIsNeedTip = false;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setPressureLimit(float limit) {
        if (this.mIsDebug) {
            Log.d(TAG, "hwCircleAnimation limit = " + limit);
        }
        this.mLimit = limit;
    }

    public float getPressureLimit(int keytype) {
        if (keytype == 3) {
            this.mInaccuracyToCenter = 0.5f;
        } else if (keytype == 4) {
            this.mInaccuracyToCenter = PRESS_LAUNCH_LIMIT;
        } else if (keytype != 187) {
            this.mInaccuracyToCenter = PRESS_LAUNCH_LIMIT;
        } else {
            this.mInaccuracyToCenter = PRESS_LAUNCH_LIMIT;
        }
        return this.mLimit * this.mInaccuracyToCenter;
    }

    public void resetAnimaion() {
        Handler handler = this.mHandler;
        if (handler != null) {
            this.mHandler.sendMessage(handler.obtainMessage(4));
        }
    }

    public void setMode(int mode) {
        synchronized (this.mLock) {
            if (this.mIsDebug) {
                Log.d(TAG, "setMode mode = " + mode);
            }
            this.mMode = mode;
        }
    }

    public int getMode(int mode) {
        int i;
        synchronized (this.mLock) {
            i = this.mMode;
        }
        return i;
    }

    public void setImmersiveMode(boolean isImmersiveMode) {
        if (this.mIsImmersiveMode != isImmersiveMode) {
            this.mIsImmersiveMode = isImmersiveMode;
            if (this.mIsDebug) {
                Log.d(TAG, "hwCircleWindow setImmersiveMode has Changedi mode = " + isImmersiveMode);
            }
            Handler handler = this.mHandler;
            if (handler != null) {
                this.mHandler.sendMessage(handler.obtainMessage(7));
            }
        }
    }

    public void setCanNaviDraw(boolean isCanDraw) {
        if (this.mIsCanNaviDraw != isCanDraw) {
            this.mIsCanNaviDraw = isCanDraw;
            Handler handler = this.mHandler;
            if (handler != null) {
                this.mHandler.sendMessage(handler.obtainMessage(8));
            }
        }
    }

    /* access modifiers changed from: package-private */
    public class HwCircle extends View implements HwCircleAnimation.AnimationUpdateListener {
        private static final int CIRCLE_COLOR = -7960954;
        private static final int CORNER_RADIUS = 4;
        private static final int PAINT_COLOR = 100;
        private static final int SMALL_RADIUS = 4;
        private static final int WIDTH_BISECT_COEF = 2;
        private static final int WIDTH_COEF = 3;
        private static final int WIDTH_QUAD_COEF = 4;
        private HwCircleAnimation mCircleAnim;
        private HwCirclePrompt mCirclePrompt;
        CornerPathEffect mCornerEffect;
        private boolean mIsCanClear;
        private boolean mIsCanPrompt;
        private boolean mIsLunched;
        private Paint mNaviPaint;
        private int mOldX;
        private int mOldY;
        private float mProcess;

        HwCircle(Context context) {
            super(context);
            this.mCircleAnim = null;
            this.mCirclePrompt = null;
            this.mIsLunched = false;
            this.mIsCanPrompt = false;
            this.mNaviPaint = null;
            this.mIsCanClear = false;
            this.mOldX = 0;
            this.mOldY = 0;
            this.mCircleAnim = new HwCircleAnimation(this, getResources());
            this.mCirclePrompt = new HwCirclePrompt(context, getResources());
            initNaviPaint();
        }

        public boolean onTouchEvent(MotionEvent event, int type) {
            if (HwCircleWindow.this.mIsDebug) {
                Log.d(HwCircleWindow.TAG, "action xxx" + event.getAction() + ", " + event.getX() + AwarenessInnerConstants.DASH_KEY + event.getY() + " press = " + event.getPressure());
            }
            if (HwCircleWindow.this.mIsDebug) {
                Log.e(HwCircleWindow.TAG, "mPressLaunchLimit = " + HwCircleWindow.this.mPressLaunchLimit + " mPressStartLimit = " + HwCircleWindow.this.mPressStartLimit);
            }
            HwCircleWindow hwCircleWindow = HwCircleWindow.this;
            hwCircleWindow.mPressLaunchLimit = hwCircleWindow.mLimit * HwCircleWindow.this.mInaccuracyToCenter;
            HwCircleWindow hwCircleWindow2 = HwCircleWindow.this;
            hwCircleWindow2.mPressStartLimit = hwCircleWindow2.mPressLaunchLimit * 0.6f;
            int action = event.getAction();
            if (action == 0) {
                this.mIsLunched = false;
                return true;
            } else if (action != 2) {
                this.mCircleAnim.endRingOutAnim(this.mIsLunched);
                Log.e(HwCircleWindow.TAG, "mode is APP_MODE destoryCircleWindowForAPP");
                HwCircleWindow.this.destoryCircleWindowForAPP();
                return true;
            } else {
                this.mCircleAnim.setViewSize(HwCircleWindow.this.mDisplayWidth, HwCircleWindow.this.mDisplayHeight);
                int currentX = (int) event.getX();
                int currentY = (int) event.getY();
                this.mCircleAnim.setCenterCoordinate(currentX, currentY);
                this.mCircleAnim.setType(type);
                if (!(currentX == this.mOldX && currentY == this.mOldY)) {
                    this.mOldX = currentX;
                    this.mOldY = currentY;
                    onAnimationUpdate();
                }
                float pressure = event.getPressure();
                if (this.mIsLunched || pressure <= HwCircleWindow.this.mPressStartLimit) {
                    return true;
                }
                synchronized (HwCircleWindow.this.mLock) {
                    if (HwCircleWindow.this.mMode == 0) {
                        Log.e(HwCircleWindow.TAG, "mode is APP_MODE createCircleWindow");
                        HwCircleWindow.this.createCircleWindow();
                    }
                }
                this.mCircleAnim.startRingOutAnim(currentX, currentY, type);
                this.mProcess = (pressure - HwCircleWindow.this.mPressStartLimit) / (HwCircleWindow.this.mPressLaunchLimit - HwCircleWindow.this.mPressStartLimit);
                this.mIsLunched = this.mCircleAnim.setFillProcess(this.mProcess);
                return true;
            }
        }

        public boolean onTouchEventDebug(MotionEvent event) {
            int action = event.getAction();
            if (action == 0 || action != 2) {
                return true;
            }
            invalidate();
            return true;
        }

        public void resetAnimaion() {
            HwCircleAnimation hwCircleAnimation = this.mCircleAnim;
            if (hwCircleAnimation != null) {
                hwCircleAnimation.resetAnimaion();
            }
        }

        public void setNeedTip(boolean isNeedTip) {
            this.mIsCanPrompt = isNeedTip;
        }

        /* access modifiers changed from: protected */
        @Override // android.view.View
        public void onDraw(Canvas canvas) {
            if (this.mIsCanPrompt && this.mCirclePrompt != null) {
                if (HwCircleWindow.this.mIsDebug) {
                    Log.d(HwCircleWindow.TAG, "hwCircleWindow prompt draw.");
                }
                this.mCirclePrompt.setViewSize(HwCircleWindow.this.mDisplayWidth, HwCircleWindow.this.mDisplayHeight);
                this.mCirclePrompt.draw(canvas);
                this.mIsCanPrompt = false;
                HwCircleAnimation hwCircleAnimation = this.mCircleAnim;
                if (hwCircleAnimation != null) {
                    hwCircleAnimation.setTipShowStatus(true);
                }
                this.mIsCanClear = true;
            } else if (this.mIsCanClear) {
                if (HwCircleWindow.this.mIsDebug) {
                    Log.d(HwCircleWindow.TAG, "hwCircleWindow prompt not draw.");
                }
                Paint paint = new Paint();
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
                canvas.drawPaint(paint);
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
                HwCircleAnimation hwCircleAnimation2 = this.mCircleAnim;
                if (hwCircleAnimation2 != null) {
                    hwCircleAnimation2.setTipShowStatus(false);
                }
                this.mIsCanClear = false;
            } else {
                Log.d(HwCircleWindow.TAG, "mIsCanClear is false");
            }
            HwCircleAnimation hwCircleAnimation3 = this.mCircleAnim;
            if (hwCircleAnimation3 != null) {
                hwCircleAnimation3.draw(canvas);
            }
            if (!HwCircleWindow.this.mIsImmersiveMode && HwCircleWindow.this.mIsCanNaviDraw) {
                drawNavi(canvas);
            }
        }

        @Override // com.android.server.input.HwCircleAnimation.AnimationUpdateListener
        public void onAnimationUpdate() {
            invalidate();
        }

        public void release() {
            HwCirclePrompt hwCirclePrompt = this.mCirclePrompt;
            if (hwCirclePrompt != null) {
                hwCirclePrompt.release();
            }
        }

        private void initNaviPaint() {
            this.mNaviPaint = new Paint();
            this.mNaviPaint.setColor(CIRCLE_COLOR);
            this.mNaviPaint.setAlpha(100);
            this.mNaviPaint.setStrokeCap(Paint.Cap.ROUND);
            this.mNaviPaint.setAntiAlias(true);
            this.mCornerEffect = new CornerPathEffect(4.0f);
            this.mNaviPaint.setPathEffect(this.mCornerEffect);
        }

        private void drawNavi(Canvas canvas) {
            if (HwCircleWindow.this.mDisplayHeight > HwCircleWindow.this.mDisplayWidth) {
                if (HwCircleWindow.this.mIsDebug) {
                    Log.d(HwCircleWindow.TAG, "hwCircleWindow prompt drawSmall 0000.");
                }
                drawSmall(canvas, HwCircleWindow.this.mDisplayWidth / 4, HwCircleWindow.this.mDisplayHeight - 4, this.mNaviPaint);
                drawSmall(canvas, HwCircleWindow.this.mDisplayWidth / 2, HwCircleWindow.this.mDisplayHeight - 4, this.mNaviPaint);
                drawSmall(canvas, (HwCircleWindow.this.mDisplayWidth * 3) / 4, HwCircleWindow.this.mDisplayHeight - 4, this.mNaviPaint);
                return;
            }
            if (HwCircleWindow.this.mIsDebug) {
                Log.d(HwCircleWindow.TAG, "hwCircleWindow prompt drawSmall 1111.");
            }
            drawSmall(canvas, HwCircleWindow.this.mDisplayWidth - 4, HwCircleWindow.this.mDisplayHeight / 4, this.mNaviPaint);
            drawSmall(canvas, HwCircleWindow.this.mDisplayWidth - 4, HwCircleWindow.this.mDisplayHeight / 2, this.mNaviPaint);
            drawSmall(canvas, HwCircleWindow.this.mDisplayWidth - 4, (HwCircleWindow.this.mDisplayHeight * 3) / 4, this.mNaviPaint);
        }

        private void drawSmall(Canvas canvas, int cx, int cy, Paint paint) {
            canvas.drawCircle((float) cx, (float) cy, 4.0f, paint);
        }
    }
}
