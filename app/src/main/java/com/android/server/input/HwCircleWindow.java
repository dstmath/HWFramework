package com.android.server.input;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings.System;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import com.android.server.input.HwCircleAnimation.AnimationUpdateListener;
import com.android.server.wifipro.WifiProCommonUtils;
import huawei.com.android.server.policy.HwGlobalActionsData;

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
    private static HwCircleWindow mhwCircleWindow;
    private boolean DEBUG;
    private String TAG;
    private boolean canNaviDraw;
    private float inaccuracyToCenter;
    private LayoutParams layoutParams;
    private final Looper looper;
    private HandlerThread mCircleHandlerThread;
    private Context mContext;
    int mDisplayHeight;
    int mDisplayWidth;
    private Handler mHandler;
    private boolean mImmersiveMode;
    private float mLimit;
    private Object mLock;
    private int mMode;
    private boolean mNeedTip;
    private WindowManager mWindowManager;
    private hwCircle mhwCircle;
    private String needTip;
    private float pressLaunchLimit;
    private float pressStartLimit;
    private boolean viewAdd;

    private class HandlerEx extends Handler {
        public HandlerEx(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HwCircleWindow.NEEDTIPNUM_ENABLE /*1*/:
                    HwCircleWindow.this.createCircleWindowLock();
                case HwCircleWindow.NEEDTIPNUM_DISABLE /*2*/:
                    HwCircleWindow.this.destoryCircleWindowAndBitmapLock();
                case HwCircleWindow.NEEDTIPNUM_DISPLAYED /*3*/:
                    if (HwCircleWindow.this.mhwCircle != null && msg.obj != null) {
                        HwCircleWindow.this.mhwCircle.onTouchEvent((MotionEvent) msg.obj, msg.arg1);
                    }
                case HwCircleWindow.MSG_DISPATCH_RESET_ANIMATION /*4*/:
                    if (HwCircleWindow.this.mhwCircle != null) {
                        HwCircleWindow.this.mhwCircle.resetAnimaion();
                    }
                case HwCircleWindow.MSG_DISPATCH_ON_EVENT_DEBUG /*5*/:
                    if (HwCircleWindow.this.mhwCircle != null && msg.obj != null) {
                        HwCircleWindow.this.mhwCircle.onTouchEventDebug((MotionEvent) msg.obj);
                    }
                case HwCircleWindow.MSG_DISPATCH_PROMPT_TIMEOUT /*6*/:
                    if (HwCircleWindow.this.mhwCircle != null) {
                        Log.d(HwCircleWindow.this.TAG, "hwCircleWindow timeout");
                        if (HwCircleWindow.NEEDTIPNUM_ENABLE == System.getIntForUser(HwCircleWindow.this.mContext.getContentResolver(), HwCircleWindow.this.needTip, HwCircleWindow.NEEDTIPNUM_FIRST, ActivityManager.getCurrentUser())) {
                            System.putIntForUser(HwCircleWindow.this.mContext.getContentResolver(), HwCircleWindow.this.needTip, HwCircleWindow.NEEDTIPNUM_DISPLAYED, ActivityManager.getCurrentUser());
                            HwCircleWindow.this.mNeedTip = false;
                        }
                    }
                case HwCircleWindow.MSG_DISPATCH_IMMERSIVEMODE /*7*/:
                    if (HwCircleWindow.this.mImmersiveMode && HwCircleWindow.this.mhwCircle != null) {
                        HwCircleWindow.this.mhwCircle.onAnimationUpdate();
                    }
                case HwCircleWindow.MSG_DISPATCH_UPDATEVIEW /*8*/:
                    if (HwCircleWindow.this.mhwCircle != null) {
                        HwCircleWindow.this.mhwCircle.onAnimationUpdate();
                    }
                default:
            }
        }
    }

    class hwCircle extends View implements AnimationUpdateListener {
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
            this.CORNER_RADIUS = HwCircleWindow.MSG_DISPATCH_RESET_ANIMATION;
            this.SMALL_RADIUS = HwCircleWindow.MSG_DISPATCH_RESET_ANIMATION;
            this.CIRCLE_COLOR = -7960954;
            this.NaviPaint = null;
            this.canClear = false;
            this.mOldX = HwCircleWindow.NEEDTIPNUM_FIRST;
            this.mOldY = HwCircleWindow.NEEDTIPNUM_FIRST;
            this.mCircleAnim = new HwCircleAnimation(this, getResources());
            this.mCirclePrompt = new HwCirclePrompt(context, getResources());
            initNaviPaint();
        }

        public boolean onTouchEvent(MotionEvent event, int type) {
            if (HwCircleWindow.this.DEBUG) {
                Log.d(HwCircleWindow.this.TAG, "action xxx" + event.getAction() + ", " + event.getX() + "-" + event.getY() + " press = " + event.getPressure());
            }
            if (HwCircleWindow.this.DEBUG) {
                Log.e(HwCircleWindow.this.TAG, "pressLaunchLimit  = " + HwCircleWindow.this.pressLaunchLimit + "  pressStartLimit  = " + HwCircleWindow.this.pressStartLimit);
            }
            float pressure = event.getPressure();
            HwCircleWindow.this.pressLaunchLimit = HwCircleWindow.this.mLimit * HwCircleWindow.this.inaccuracyToCenter;
            HwCircleWindow.this.pressStartLimit = HwCircleWindow.this.pressLaunchLimit * HwCirclePrompt.BG_ALPHA;
            switch (event.getAction()) {
                case HwCircleWindow.NEEDTIPNUM_FIRST /*0*/:
                    this.isLunched = false;
                    break;
                case HwCircleWindow.NEEDTIPNUM_DISABLE /*2*/:
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
                            break;
                        }
                        this.mCircleAnim.startRingOutAnim(currentX, currentY, type);
                        this.process = (pressure - HwCircleWindow.this.pressStartLimit) / (HwCircleWindow.this.pressLaunchLimit - HwCircleWindow.this.pressStartLimit);
                        this.isLunched = this.mCircleAnim.setFillProcess(this.process);
                        break;
                    }
                default:
                    this.mCircleAnim.endRingOutAnim(this.isLunched);
                    Log.e(HwCircleWindow.this.TAG, " mode is APP_MODE destoryCircleWindowForAPP");
                    HwCircleWindow.this.destoryCircleWindowForAPP();
                    break;
            }
            return true;
        }

        public boolean onTouchEventDebug(MotionEvent event) {
            switch (event.getAction()) {
                case HwCircleWindow.NEEDTIPNUM_DISABLE /*2*/:
                    invalidate();
                    break;
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

        protected void onDraw(Canvas canvas) {
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
                p.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
                canvas.drawPaint(p);
                p.setXfermode(new PorterDuffXfermode(Mode.SRC));
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
            this.NaviPaint.setAlpha(HwCircleWindow.PROMPT_TIMEOUT);
            this.NaviPaint.setStrokeCap(Cap.ROUND);
            this.NaviPaint.setAntiAlias(true);
            this.mCornerEffect = new CornerPathEffect((float) this.CORNER_RADIUS);
            this.NaviPaint.setPathEffect(this.mCornerEffect);
        }

        private void drawNavi(Canvas canvas) {
            if (HwCircleWindow.this.mDisplayHeight > HwCircleWindow.this.mDisplayWidth) {
                if (HwCircleWindow.this.DEBUG) {
                    Log.d(HwCircleWindow.this.TAG, "hwCircleWindow prompt drawSmall 0000 ");
                }
                drawSmall(canvas, HwCircleWindow.this.mDisplayWidth / HwCircleWindow.MSG_DISPATCH_RESET_ANIMATION, HwCircleWindow.this.mDisplayHeight - this.SMALL_RADIUS, this.NaviPaint);
                drawSmall(canvas, HwCircleWindow.this.mDisplayWidth / HwCircleWindow.NEEDTIPNUM_DISABLE, HwCircleWindow.this.mDisplayHeight - this.SMALL_RADIUS, this.NaviPaint);
                drawSmall(canvas, (HwCircleWindow.this.mDisplayWidth * HwCircleWindow.NEEDTIPNUM_DISPLAYED) / HwCircleWindow.MSG_DISPATCH_RESET_ANIMATION, HwCircleWindow.this.mDisplayHeight - this.SMALL_RADIUS, this.NaviPaint);
                return;
            }
            if (HwCircleWindow.this.DEBUG) {
                Log.d(HwCircleWindow.this.TAG, "hwCircleWindow prompt drawSmall 1111 ");
            }
            drawSmall(canvas, HwCircleWindow.this.mDisplayWidth - this.SMALL_RADIUS, HwCircleWindow.this.mDisplayHeight / HwCircleWindow.MSG_DISPATCH_RESET_ANIMATION, this.NaviPaint);
            drawSmall(canvas, HwCircleWindow.this.mDisplayWidth - this.SMALL_RADIUS, HwCircleWindow.this.mDisplayHeight / HwCircleWindow.NEEDTIPNUM_DISABLE, this.NaviPaint);
            drawSmall(canvas, HwCircleWindow.this.mDisplayWidth - this.SMALL_RADIUS, (HwCircleWindow.this.mDisplayHeight * HwCircleWindow.NEEDTIPNUM_DISPLAYED) / HwCircleWindow.MSG_DISPATCH_RESET_ANIMATION, this.NaviPaint);
        }

        private void drawSmall(Canvas canvas, int cx, int cy, Paint paint) {
            canvas.drawCircle((float) cx, (float) cy, (float) this.SMALL_RADIUS, paint);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.input.HwCircleWindow.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.input.HwCircleWindow.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.input.HwCircleWindow.<clinit>():void");
    }

    protected HwCircleWindow(Context context) {
        this.TAG = "pressure:hwCircleWindow";
        this.DEBUG = false;
        this.looper = Looper.myLooper();
        this.mWindowManager = null;
        this.layoutParams = null;
        this.mNeedTip = false;
        this.mLock = new Object();
        this.viewAdd = false;
        this.needTip = "pressure_needTip";
        this.pressStartLimit = 0.3f;
        this.pressLaunchLimit = 0.45f;
        this.inaccuracyToCenter = WifiProCommonUtils.RECOVERY_PERCENTAGE;
        this.mLimit = HwCircleAnimation.BG_ALPHA_FILL;
        this.mMode = NEEDTIPNUM_FIRST;
        this.canNaviDraw = true;
        this.mImmersiveMode = false;
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
            this.mHandler.sendMessage(this.mHandler.obtainMessage(NEEDTIPNUM_ENABLE));
        }
        if (this.mhwCircle != null) {
            this.mhwCircle.setNeedTip(this.mNeedTip);
        }
    }

    private void createCircleWindowLock() {
        synchronized (this.mLock) {
            if (!this.viewAdd) {
                LayoutParams layoutParams;
                if (this.layoutParams == null) {
                    this.layoutParams = new LayoutParams(-1, -1);
                }
                this.layoutParams.type = 2015;
                this.layoutParams.flags = 1304;
                if (ActivityManager.isHighEndGfx()) {
                    layoutParams = this.layoutParams;
                    layoutParams.flags |= HwGlobalActionsData.FLAG_SHUTDOWN;
                    layoutParams = this.layoutParams;
                    layoutParams.privateFlags |= NEEDTIPNUM_DISABLE;
                }
                layoutParams = this.layoutParams;
                layoutParams.privateFlags |= 16;
                this.layoutParams.format = -3;
                this.layoutParams.setTitle("hwPressueWindow");
                layoutParams = this.layoutParams;
                layoutParams.inputFeatures |= NEEDTIPNUM_DISABLE;
                if (this.mWindowManager == null) {
                    this.mWindowManager = (WindowManager) this.mContext.getSystemService("window");
                }
                this.mWindowManager.addView(this.mhwCircle, this.layoutParams);
                this.viewAdd = true;
                if (this.mHandler != null) {
                    this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(MSG_DISPATCH_PROMPT_TIMEOUT), 100);
                }
                Log.v(this.TAG, "createCircleWindowLock viewAdd = " + this.viewAdd);
            } else if (this.mHandler != null) {
                this.mHandler.sendMessage(this.mHandler.obtainMessage(MSG_DISPATCH_UPDATEVIEW));
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
            this.mHandler.sendMessage(this.mHandler.obtainMessage(NEEDTIPNUM_DISABLE));
        }
    }

    public void destoryCircleWindowAndBitmapLock() {
        synchronized (this.mLock) {
            if (this.viewAdd && this.mhwCircle != null) {
                this.mhwCircle.release();
                this.mWindowManager.removeView(this.mhwCircle);
                Log.v(this.TAG, " destoryCircleWindowAndBitmapLock viewAdd = " + this.viewAdd);
                this.viewAdd = false;
            }
            this.layoutParams = null;
            this.mWindowManager = null;
        }
    }

    public void onTouchEvent(MotionEvent event, int type) {
        if (this.mHandler != null) {
            Message msg1 = this.mHandler.obtainMessage(NEEDTIPNUM_DISPLAYED, event);
            msg1.arg1 = type;
            this.mHandler.sendMessage(msg1);
        }
    }

    public void onTouchEventDebug(MotionEvent event) {
        if (this.mHandler != null) {
            this.mHandler.sendMessage(this.mHandler.obtainMessage(MSG_DISPATCH_ON_EVENT_DEBUG, event));
        }
    }

    void setPressureLimit(float limit) {
        if (this.DEBUG) {
            Log.d(this.TAG, "hwCircleAnimation limit =  " + limit);
        }
        this.mLimit = limit;
    }

    public float getPressureLimit(int keytype) {
        switch (keytype) {
            case NEEDTIPNUM_DISPLAYED /*3*/:
                this.inaccuracyToCenter = WifiProCommonUtils.RECOVERY_PERCENTAGE;
                break;
            case MSG_DISPATCH_RESET_ANIMATION /*4*/:
                this.inaccuracyToCenter = 0.45f;
                break;
            case 187:
                this.inaccuracyToCenter = 0.45f;
                break;
            default:
                this.inaccuracyToCenter = 0.45f;
                break;
        }
        return this.mLimit * this.inaccuracyToCenter;
    }

    public void resetAnimaion() {
        if (this.mHandler != null) {
            this.mHandler.sendMessage(this.mHandler.obtainMessage(MSG_DISPATCH_RESET_ANIMATION));
        }
    }

    public void setMode(int mode) {
        synchronized (this.mLock) {
            if (this.DEBUG) {
                Log.d(this.TAG, "setMode mode = " + mode);
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
                Log.d(this.TAG, "hwCircleWindow setImmersiveMode  has Changedi mode = " + mode);
            }
            if (this.mHandler != null) {
                this.mHandler.sendMessage(this.mHandler.obtainMessage(MSG_DISPATCH_IMMERSIVEMODE));
            }
        }
    }

    public void setCanNaviDraw(boolean canDraw) {
        if (this.canNaviDraw != canDraw) {
            this.canNaviDraw = canDraw;
            if (this.mHandler != null) {
                this.mHandler.sendMessage(this.mHandler.obtainMessage(MSG_DISPATCH_UPDATEVIEW));
            }
        }
    }
}
