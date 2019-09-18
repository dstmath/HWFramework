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

public class HwCircleWindow {
    public static final int ANIMATION_TYPE_APP = 4;
    public static final int ANIMATION_TYPE_BACK = 2;
    public static final int ANIMATION_TYPE_HOME = 1;
    public static final int ANIMATION_TYPE_MULTI = 3;
    public static final int APP_MODE = 0;
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
    private static final int PROMPT_TIMEOUT = 100;
    private static HwCircleWindow mhwCircleWindow = null;
    /* access modifiers changed from: private */
    public boolean DEBUG = false;
    /* access modifiers changed from: private */
    public String TAG = "pressure:hwCircleWindow";
    /* access modifiers changed from: private */
    public boolean canNaviDraw = true;
    /* access modifiers changed from: private */
    public float inaccuracyToCenter = 0.5f;
    private WindowManager.LayoutParams layoutParams = null;
    private final Looper looper = Looper.myLooper();
    private HandlerThread mCircleHandlerThread;
    /* access modifiers changed from: private */
    public Context mContext;
    int mDisplayHeight;
    int mDisplayWidth;
    private Handler mHandler;
    /* access modifiers changed from: private */
    public boolean mImmersiveMode = false;
    /* access modifiers changed from: private */
    public float mLimit = 0.2f;
    /* access modifiers changed from: private */
    public Object mLock = new Object();
    /* access modifiers changed from: private */
    public int mMode = 0;
    /* access modifiers changed from: private */
    public boolean mNeedTip = false;
    private WindowManager mWindowManager = null;
    /* access modifiers changed from: private */
    public hwCircle mhwCircle;
    /* access modifiers changed from: private */
    public String needTip = "pressure_needTip";
    /* access modifiers changed from: private */
    public float pressLaunchLimit = 0.45f;
    /* access modifiers changed from: private */
    public float pressStartLimit = 0.3f;
    private boolean viewAdd = false;

    private class HandlerEx extends Handler {
        public HandlerEx(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    HwCircleWindow.this.createCircleWindowLock();
                    return;
                case 2:
                    HwCircleWindow.this.destoryCircleWindowAndBitmapLock();
                    return;
                case 3:
                    if (HwCircleWindow.this.mhwCircle != null && msg.obj != null) {
                        HwCircleWindow.this.mhwCircle.onTouchEvent((MotionEvent) msg.obj, msg.arg1);
                        return;
                    }
                    return;
                case 4:
                    if (HwCircleWindow.this.mhwCircle != null) {
                        HwCircleWindow.this.mhwCircle.resetAnimaion();
                        return;
                    }
                    return;
                case 5:
                    if (HwCircleWindow.this.mhwCircle != null && msg.obj != null) {
                        HwCircleWindow.this.mhwCircle.onTouchEventDebug((MotionEvent) msg.obj);
                        return;
                    }
                    return;
                case 6:
                    if (HwCircleWindow.this.mhwCircle != null) {
                        Log.d(HwCircleWindow.this.TAG, "hwCircleWindow timeout");
                        if (1 == Settings.System.getIntForUser(HwCircleWindow.this.mContext.getContentResolver(), HwCircleWindow.this.needTip, 0, ActivityManager.getCurrentUser())) {
                            Settings.System.putIntForUser(HwCircleWindow.this.mContext.getContentResolver(), HwCircleWindow.this.needTip, 3, ActivityManager.getCurrentUser());
                            boolean unused = HwCircleWindow.this.mNeedTip = false;
                            return;
                        }
                        return;
                    }
                    return;
                case 7:
                    if (HwCircleWindow.this.mImmersiveMode && HwCircleWindow.this.mhwCircle != null) {
                        HwCircleWindow.this.mhwCircle.onAnimationUpdate();
                        return;
                    }
                    return;
                case 8:
                    if (HwCircleWindow.this.mhwCircle != null) {
                        HwCircleWindow.this.mhwCircle.onAnimationUpdate();
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    class hwCircle extends View implements HwCircleAnimation.AnimationUpdateListener {
        private int CIRCLE_COLOR;
        private int CORNER_RADIUS;
        private Paint NaviPaint;
        private int SMALL_RADIUS;
        private boolean canClear;
        private boolean canPrompt;
        private boolean isLunched;
        private HwCircleAnimation mCircleAnim;
        private HwCirclePrompt mCirclePrompt;
        CornerPathEffect mCornerEffect;
        private int mOldX;
        private int mOldY;
        private float process;

        public hwCircle(Context context) {
            super(context);
            this.mCircleAnim = null;
            this.mCirclePrompt = null;
            this.isLunched = false;
            this.canPrompt = false;
            this.CORNER_RADIUS = 4;
            this.SMALL_RADIUS = 4;
            this.CIRCLE_COLOR = -7960954;
            this.NaviPaint = null;
            this.canClear = false;
            this.mOldX = 0;
            this.mOldY = 0;
            this.mCircleAnim = new HwCircleAnimation(this, getResources());
            this.mCirclePrompt = new HwCirclePrompt(context, getResources());
            initNaviPaint();
        }

        public boolean onTouchEvent(MotionEvent event, int type) {
            if (HwCircleWindow.this.DEBUG) {
                String access$200 = HwCircleWindow.this.TAG;
                Log.d(access$200, "action xxx" + event.getAction() + ", " + event.getX() + "-" + event.getY() + " press = " + event.getPressure());
            }
            if (HwCircleWindow.this.DEBUG) {
                String access$2002 = HwCircleWindow.this.TAG;
                Log.e(access$2002, "pressLaunchLimit  = " + HwCircleWindow.this.pressLaunchLimit + "  pressStartLimit  = " + HwCircleWindow.this.pressStartLimit);
            }
            float pressure = event.getPressure();
            float unused = HwCircleWindow.this.pressLaunchLimit = HwCircleWindow.this.mLimit * HwCircleWindow.this.inaccuracyToCenter;
            float unused2 = HwCircleWindow.this.pressStartLimit = HwCircleWindow.this.pressLaunchLimit * 0.6f;
            int action = event.getAction();
            if (action == 0) {
                this.isLunched = false;
            } else if (action != 2) {
                this.mCircleAnim.endRingOutAnim(this.isLunched);
                Log.e(HwCircleWindow.this.TAG, " mode is APP_MODE destoryCircleWindowForAPP");
                HwCircleWindow.this.destoryCircleWindowForAPP();
            } else {
                this.mCircleAnim.setViewSize(HwCircleWindow.this.mDisplayWidth, HwCircleWindow.this.mDisplayHeight);
                int currentX = (int) event.getX();
                int currentY = (int) event.getY();
                this.mCircleAnim.setXY((int) event.getX(), (int) event.getY());
                this.mCircleAnim.setType(type);
                if (!(currentX == this.mOldX && currentY == this.mOldY)) {
                    this.mOldX = currentX;
                    this.mOldY = currentY;
                    onAnimationUpdate();
                }
                if (!this.isLunched && pressure > HwCircleWindow.this.pressStartLimit) {
                    synchronized (HwCircleWindow.this.mLock) {
                        if (HwCircleWindow.this.mMode == 0) {
                            Log.e(HwCircleWindow.this.TAG, " mode is APP_MODE createCircleWindow");
                            HwCircleWindow.this.createCircleWindow();
                        }
                    }
                    this.mCircleAnim.startRingOutAnim(currentX, currentY, type);
                    this.process = (pressure - HwCircleWindow.this.pressStartLimit) / (HwCircleWindow.this.pressLaunchLimit - HwCircleWindow.this.pressStartLimit);
                    this.isLunched = this.mCircleAnim.setFillProcess(this.process);
                }
            }
            return true;
        }

        public boolean onTouchEventDebug(MotionEvent event) {
            int action = event.getAction();
            if (action != 0 && action == 2) {
                invalidate();
            }
            return true;
        }

        public void resetAnimaion() {
            if (this.mCircleAnim != null) {
                this.mCircleAnim.resetAnimaion();
            }
        }

        public void setNeedTip(boolean needtip) {
            this.canPrompt = needtip;
        }

        /* access modifiers changed from: protected */
        public void onDraw(Canvas canvas) {
            if (this.canPrompt && this.mCirclePrompt != null) {
                if (HwCircleWindow.this.DEBUG) {
                    Log.d(HwCircleWindow.this.TAG, "hwCircleWindow prompt draw ");
                }
                this.mCirclePrompt.setViewSize(HwCircleWindow.this.mDisplayWidth, HwCircleWindow.this.mDisplayHeight);
                this.mCirclePrompt.draw(canvas);
                this.canPrompt = false;
                if (this.mCircleAnim != null) {
                    this.mCircleAnim.setTipShowStatus(true);
                }
                this.canClear = true;
            } else if (this.canClear) {
                if (HwCircleWindow.this.DEBUG) {
                    Log.d(HwCircleWindow.this.TAG, "hwCircleWindow prompt not draw ");
                }
                Paint p = new Paint();
                p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
                canvas.drawPaint(p);
                p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
                if (this.mCircleAnim != null) {
                    this.mCircleAnim.setTipShowStatus(false);
                }
                this.canClear = false;
            }
            if (this.mCircleAnim != null) {
                this.mCircleAnim.draw(canvas);
            }
            if (!HwCircleWindow.this.mImmersiveMode && HwCircleWindow.this.canNaviDraw) {
                drawNavi(canvas);
            }
        }

        public void onAnimationUpdate() {
            invalidate();
        }

        public void release() {
            if (this.mCirclePrompt != null) {
                this.mCirclePrompt.release();
            }
        }

        private void initNaviPaint() {
            this.NaviPaint = new Paint();
            this.NaviPaint.setColor(this.CIRCLE_COLOR);
            this.NaviPaint.setAlpha(100);
            this.NaviPaint.setStrokeCap(Paint.Cap.ROUND);
            this.NaviPaint.setAntiAlias(true);
            this.mCornerEffect = new CornerPathEffect((float) this.CORNER_RADIUS);
            this.NaviPaint.setPathEffect(this.mCornerEffect);
        }

        private void drawNavi(Canvas canvas) {
            if (HwCircleWindow.this.mDisplayHeight > HwCircleWindow.this.mDisplayWidth) {
                if (HwCircleWindow.this.DEBUG) {
                    Log.d(HwCircleWindow.this.TAG, "hwCircleWindow prompt drawSmall 0000 ");
                }
                drawSmall(canvas, HwCircleWindow.this.mDisplayWidth / 4, HwCircleWindow.this.mDisplayHeight - this.SMALL_RADIUS, this.NaviPaint);
                drawSmall(canvas, HwCircleWindow.this.mDisplayWidth / 2, HwCircleWindow.this.mDisplayHeight - this.SMALL_RADIUS, this.NaviPaint);
                drawSmall(canvas, (HwCircleWindow.this.mDisplayWidth * 3) / 4, HwCircleWindow.this.mDisplayHeight - this.SMALL_RADIUS, this.NaviPaint);
                return;
            }
            if (HwCircleWindow.this.DEBUG) {
                Log.d(HwCircleWindow.this.TAG, "hwCircleWindow prompt drawSmall 1111 ");
            }
            drawSmall(canvas, HwCircleWindow.this.mDisplayWidth - this.SMALL_RADIUS, HwCircleWindow.this.mDisplayHeight / 4, this.NaviPaint);
            drawSmall(canvas, HwCircleWindow.this.mDisplayWidth - this.SMALL_RADIUS, HwCircleWindow.this.mDisplayHeight / 2, this.NaviPaint);
            drawSmall(canvas, HwCircleWindow.this.mDisplayWidth - this.SMALL_RADIUS, (HwCircleWindow.this.mDisplayHeight * 3) / 4, this.NaviPaint);
        }

        private void drawSmall(Canvas canvas, int cx, int cy, Paint paint) {
            canvas.drawCircle((float) cx, (float) cy, (float) this.SMALL_RADIUS, paint);
        }
    }

    protected HwCircleWindow(Context context) {
        this.mContext = context;
        createCircleNoWindow();
    }

    public static synchronized HwCircleWindow getInstance(Context context) {
        HwCircleWindow hwCircleWindow;
        synchronized (HwCircleWindow.class) {
            if (mhwCircleWindow == null) {
                mhwCircleWindow = new HwCircleWindow(context);
            }
            hwCircleWindow = mhwCircleWindow;
        }
        return hwCircleWindow;
    }

    public void setNeedTip(boolean needtip) {
        this.mNeedTip = needtip;
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
        if (this.mhwCircle == null) {
            this.mhwCircle = new hwCircle(this.mContext);
            this.mhwCircle.setNeedTip(this.mNeedTip);
        }
    }

    public void createCircleWindow() {
        if (this.mHandler != null) {
            this.mHandler.sendMessage(this.mHandler.obtainMessage(1));
        }
        if (this.mhwCircle != null) {
            this.mhwCircle.setNeedTip(this.mNeedTip);
        }
    }

    /* access modifiers changed from: private */
    public void createCircleWindowLock() {
        synchronized (this.mLock) {
            if (!this.viewAdd) {
                if (this.layoutParams == null) {
                    this.layoutParams = new WindowManager.LayoutParams(-1, -1);
                }
                this.layoutParams.type = HwArbitrationDEFS.MSG_MPLINK_STOP_COEX_SUCC;
                this.layoutParams.flags = 1304;
                if (ActivityManager.isHighEndGfx()) {
                    this.layoutParams.flags |= 16777216;
                    this.layoutParams.privateFlags |= 2;
                }
                this.layoutParams.privateFlags |= 16;
                this.layoutParams.format = -3;
                this.layoutParams.setTitle("hwPressueWindow");
                this.layoutParams.inputFeatures |= 2;
                if (this.mWindowManager == null) {
                    this.mWindowManager = (WindowManager) this.mContext.getSystemService("window");
                }
                this.mWindowManager.addView(this.mhwCircle, this.layoutParams);
                this.viewAdd = true;
                if (this.mHandler != null) {
                    this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(6), 100);
                }
                Log.v(this.TAG, "createCircleWindowLock viewAdd = " + this.viewAdd);
            } else if (this.mHandler != null) {
                this.mHandler.sendMessage(this.mHandler.obtainMessage(8));
            }
        }
    }

    public void destoryCircleWindowForAPP() {
        synchronized (this.mLock) {
            if (this.mMode == 0 && this.viewAdd) {
                destoryCircleWindowAndBitmap();
            }
        }
    }

    public void destoryCircleWindowAndBitmap() {
        if (this.mHandler != null) {
            this.mHandler.sendMessage(this.mHandler.obtainMessage(2));
        }
    }

    public void destoryCircleWindowAndBitmapLock() {
        synchronized (this.mLock) {
            if (this.viewAdd && this.mhwCircle != null) {
                this.mhwCircle.release();
                this.mWindowManager.removeView(this.mhwCircle);
                String str = this.TAG;
                Log.v(str, " destoryCircleWindowAndBitmapLock viewAdd = " + this.viewAdd);
                this.viewAdd = false;
            }
            this.layoutParams = null;
            this.mWindowManager = null;
        }
    }

    public void onTouchEvent(MotionEvent event, int type) {
        if (this.mHandler != null) {
            Message msg1 = this.mHandler.obtainMessage(3, event);
            msg1.arg1 = type;
            this.mHandler.sendMessage(msg1);
        }
    }

    public void onTouchEventDebug(MotionEvent event) {
        if (this.mHandler != null) {
            this.mHandler.sendMessage(this.mHandler.obtainMessage(5, event));
        }
    }

    /* access modifiers changed from: package-private */
    public void setPressureLimit(float limit) {
        if (this.DEBUG) {
            String str = this.TAG;
            Log.d(str, "hwCircleAnimation limit =  " + limit);
        }
        this.mLimit = limit;
    }

    public float getPressureLimit(int keytype) {
        if (keytype != 187) {
            switch (keytype) {
                case 3:
                    this.inaccuracyToCenter = 0.5f;
                    break;
                case 4:
                    this.inaccuracyToCenter = 0.45f;
                    break;
                default:
                    this.inaccuracyToCenter = 0.45f;
                    break;
            }
        } else {
            this.inaccuracyToCenter = 0.45f;
        }
        return this.mLimit * this.inaccuracyToCenter;
    }

    public void resetAnimaion() {
        if (this.mHandler != null) {
            this.mHandler.sendMessage(this.mHandler.obtainMessage(4));
        }
    }

    public void setMode(int mode) {
        synchronized (this.mLock) {
            if (this.DEBUG) {
                String str = this.TAG;
                Log.d(str, "setMode mode = " + mode);
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

    public void setImmersiveMode(boolean mode) {
        if (this.mImmersiveMode != mode) {
            this.mImmersiveMode = mode;
            if (this.DEBUG) {
                String str = this.TAG;
                Log.d(str, "hwCircleWindow setImmersiveMode  has Changedi mode = " + mode);
            }
            if (this.mHandler != null) {
                this.mHandler.sendMessage(this.mHandler.obtainMessage(7));
            }
        }
    }

    public void setCanNaviDraw(boolean canDraw) {
        if (this.canNaviDraw != canDraw) {
            this.canNaviDraw = canDraw;
            if (this.mHandler != null) {
                this.mHandler.sendMessage(this.mHandler.obtainMessage(8));
            }
        }
    }
}
