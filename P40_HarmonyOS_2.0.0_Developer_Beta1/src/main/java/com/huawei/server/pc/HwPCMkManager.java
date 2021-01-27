package com.huawei.server.pc;

import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.hardware.display.DisplayManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.HwPCUtils;
import android.view.Display;
import android.view.InputEvent;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import com.android.server.input.InputManagerServiceEx;
import com.huawei.android.hardware.input.InputManagerEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.server.LocalServicesExt;
import com.huawei.android.view.HwWindowManager;
import com.huawei.android.view.MotionEventEx;
import com.huawei.hwpartpowerofficeservices.BuildConfig;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HwPCMkManager {
    private static final double CONSIDER_ZERO = 1.0E-6d;
    private static final boolean DEBUG = SystemPropertiesEx.getBoolean("hw_pc_mkmanager_debug", false);
    private static final float DEFAULT_CURRENT_POINTER = 100.0f;
    private static final float DEFAULT_DENSITY = 3.0f;
    private static final float DEFAULT_HEIGHT = 50.0f;
    private static final float DEFAULT_WIDTH = 0.0f;
    private static final int FIRST_FINGER = 0;
    private static final double HALF_OF_PI = 1.57075d;
    private static final int MAX_CHECKED_PONTERS_COUNT = 2;
    private static final int MAX_FINGERS_SUPPORED = 2;
    private static final int MAX_TOUCH_SLOP = 4;
    private static final int MOTION_DOUBLE_CLICK_DELAY_TIME = 300;
    private static final int MOTION_LONG_PRESS_DELAY_TIME = 300;
    private static final int SECOND_FINGER = 1;
    private static final int SHOW_DEFAULT = 0;
    private static final int SHOW_LASERPOINTER = 1;
    private static final float SHOW_LASER_POINTER_HEIGHT = 90.0f;
    private static final float SHOW_LASER_POINTER_WIDTH = 46.0f;
    private static final int SHOW_UNUSE = -1;
    private static final String TAG = "HwPCMkManager";
    private static Set<String> listenGenericMotionPackages = new HashSet();
    private static HwPCMkManager mStatic;
    private boolean isMove = false;
    private boolean isNeedInjectDownForTouch = true;
    private boolean isSequent = false;
    private int mClickCount = 0;
    private Context mContext;
    private PointF[] mCurrentEventPointersForTouch = {new PointF(DEFAULT_WIDTH, DEFAULT_WIDTH), new PointF(DEFAULT_WIDTH, DEFAULT_WIDTH)};
    private float mCurrentEventX = DEFAULT_WIDTH;
    private float mCurrentEventY = DEFAULT_WIDTH;
    private float mCurrentPointerX = DEFAULT_CURRENT_POINTER;
    private float mCurrentPointerY = DEFAULT_CURRENT_POINTER;
    private PointF[] mCurrentPointersForTouch = {new PointF(DEFAULT_WIDTH, DEFAULT_WIDTH), new PointF(DEFAULT_WIDTH, DEFAULT_WIDTH)};
    private float mDensity = DEFAULT_DENSITY;
    private DisplayManager mDisplayManager;
    private MotionEvent mDownEvent = null;
    private MotionEvent mDownEventForTouch = null;
    private InputManagerServiceEx.DefaultHwInputManagerLocalService mInputManager = null;
    private MotionEvent mLastMoveEventForTouch = null;
    private boolean mLongPressed;
    private long mOldClickTime = 0;
    private HwPCManagerService mPCManager;
    private MotionEvent mPointerDownEventForTouch = null;
    private PoninterMode mPoninterMode = PoninterMode.NORMAL_POINTER_MODE;
    private List<PointF> mRecentEventPointers1stFinger = new ArrayList();
    private List<PointF> mRecentEventPointers2ndFinger = new ArrayList();
    private int mScreentHeight;
    private int mScreentWidth;
    private SendEventThread mSendEvent = null;
    private boolean mSendScrollDirectly;
    private boolean mSendZoomGestureDirectly;
    private int mShowMode = 0;
    private boolean mTouchDownForLongPress;
    private int mTouchSlopSquare = 0;

    /* access modifiers changed from: private */
    public enum PoninterMode {
        NORMAL_POINTER_MODE,
        MULTI_POINTER_MODE,
        SINGLE_POINTER_MODE
    }

    static {
        listenGenericMotionPackages.add("com.huawei.desktop.explorer");
        listenGenericMotionPackages.add("com.huawei.desktop.systemui");
    }

    private HwPCMkManager(Context context) {
        this.mContext = context;
        this.mDisplayManager = (DisplayManager) this.mContext.getSystemService("display");
        DisplayMetrics dm = new DisplayMetrics();
        Display defaultDisplay = this.mDisplayManager.getDisplay(0);
        if (defaultDisplay != null) {
            defaultDisplay.getMetrics(dm);
            this.mDensity = dm.density;
        }
        int touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        touchSlop = touchSlop > 4 ? 4 : touchSlop;
        this.mTouchSlopSquare = touchSlop * touchSlop;
        HwPCUtils.log(TAG, "MkManager mTouchSlopSquare:" + this.mTouchSlopSquare);
    }

    public static synchronized HwPCMkManager getInstance(Context context) {
        HwPCMkManager hwPCMkManager;
        synchronized (HwPCMkManager.class) {
            if (mStatic == null) {
                mStatic = new HwPCMkManager(context);
            }
            hwPCMkManager = mStatic;
        }
        return hwPCMkManager;
    }

    public void initCrop(Context context, HwPCManagerService pcManager) {
        this.mPCManager = pcManager;
        DisplayManager displayManager = this.mDisplayManager;
        if (displayManager != null) {
            Display display = displayManager.getDisplay(HwPCUtils.getPCDisplayID());
            if (display != null) {
                Point size = new Point();
                display.getRealSize(size);
                this.mScreentWidth = size.x;
                this.mScreentHeight = size.y;
            } else {
                HwPCUtils.log(TAG, "initCrop display is null");
            }
        }
        HwPCUtils.log(TAG, "HwPCMkManager mScreentWidth:" + this.mScreentWidth + " mScreentHeight:" + this.mScreentHeight + " mDensity = " + this.mDensity + " mTouchSlopSquare = " + this.mTouchSlopSquare);
    }

    public void startSendEventThread() {
        synchronized (this) {
            if (this.mSendEvent == null) {
                this.mSendEvent = new SendEventThread("PCSendEvent", -4);
                this.mSendEvent.start();
            }
        }
    }

    public void stopSendEventThreadAndRelease() {
        synchronized (this) {
            this.mScreentWidth = 0;
            this.mScreentHeight = 0;
            if (this.mSendEvent != null) {
                this.mSendEvent.clear();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public final float dipsToPixels(float dips) {
        return (this.mDensity * dips) + 0.5f;
    }

    /* access modifiers changed from: package-private */
    public final float pixelsToDips(float pixels) {
        return (pixels / this.mDensity) + 0.5f;
    }

    public boolean sendEvent(MotionEvent event, Rect visibleRect, Rect displayRect, int mode) {
        if (event == null || this.mSendEvent == null) {
            return false;
        }
        if (DEBUG) {
            HwPCUtils.log(TAG, "sendEvent event = " + event + "  visibleRect = " + visibleRect + " displayRect = " + displayRect);
        }
        if (!isInTouchRect(event, visibleRect, displayRect, mode)) {
            return false;
        }
        int pointerCount = event.getPointerCount();
        if (DEBUG) {
            HwPCUtils.log(TAG, "sendEvent pointerCount:" + pointerCount + " mPoninterMode:" + this.mPoninterMode + " event:" + event);
        }
        onDownEventProcess(event);
        checkRemoveLongPressedMsg(event);
        if (this.mPoninterMode == PoninterMode.NORMAL_POINTER_MODE) {
            if (pointerCount > 1) {
                this.mPoninterMode = PoninterMode.MULTI_POINTER_MODE;
            } else {
                this.mPoninterMode = PoninterMode.SINGLE_POINTER_MODE;
            }
        }
        if (this.mPoninterMode == PoninterMode.SINGLE_POINTER_MODE) {
            onSinglePointerMode(event);
        } else if (this.mPoninterMode == PoninterMode.MULTI_POINTER_MODE) {
            MotionEvent motionEvent = this.mDownEvent;
            if (motionEvent != null) {
                this.mDownEventForTouch = MotionEvent.obtain(motionEvent);
            }
            this.mDownEvent = null;
            this.mClickCount = 0;
            onMultiPointerMode(event);
        } else if (DEBUG) {
            HwPCUtils.log(TAG, " mPoninterMode:" + this.mPoninterMode);
        }
        return true;
    }

    private boolean isInTouchRect(MotionEvent event, Rect visibleRect, Rect displayRect, int mode) {
        if (mode != -1) {
            this.mShowMode = mode;
        }
        float height = DEFAULT_HEIGHT;
        float width = DEFAULT_WIDTH;
        if (displayRect.right < displayRect.bottom) {
            if (this.mShowMode == 1) {
                height = SHOW_LASER_POINTER_HEIGHT;
            }
            if (event.getX() < ((float) visibleRect.left) || event.getX() > ((float) visibleRect.right) || event.getY() < ((float) visibleRect.top) + dipsToPixels(height) || event.getY() > ((float) visibleRect.bottom)) {
                HwPCUtils.log(TAG, "sendEvent donot in visibleRect, right < bottom");
                return false;
            }
        } else {
            if (this.mShowMode == 1) {
                width = SHOW_LASER_POINTER_WIDTH;
            }
            if (event.getX() < ((float) visibleRect.left) || event.getX() > ((float) visibleRect.right) - dipsToPixels(width) || event.getY() < ((float) visibleRect.top) + dipsToPixels(DEFAULT_HEIGHT) || event.getY() > ((float) visibleRect.bottom)) {
                HwPCUtils.log(TAG, "sendEvent donot in visibleRect right >= bottom");
                return false;
            }
        }
        return true;
    }

    private void onDownEventProcess(MotionEvent event) {
        int action = event.getAction();
        if (action == 0) {
            if (DEBUG) {
                HwPCUtils.log(TAG, "onDownEventProcess ACTION_DOWN mPoninterMode:" + this.mPoninterMode);
            }
            this.mPoninterMode = PoninterMode.SINGLE_POINTER_MODE;
            this.isSequent = true;
        } else if ((action & 255) == 5) {
            if (DEBUG) {
                HwPCUtils.log(TAG, "onDownEventProcess ACTION_POINTER_DOWN mPoninterMode:" + this.mPoninterMode);
            }
            if (this.mPoninterMode == PoninterMode.NORMAL_POINTER_MODE) {
                this.mPoninterMode = PoninterMode.MULTI_POINTER_MODE;
            }
            if (this.isSequent) {
                this.mPoninterMode = PoninterMode.MULTI_POINTER_MODE;
            }
        } else if (this.mPoninterMode == PoninterMode.SINGLE_POINTER_MODE && action == 2 && this.isSequent) {
            HwPCUtils.log(TAG, "onDownEventProcess do nothing");
        } else {
            this.isSequent = false;
        }
    }

    private void onSinglePointerMode(MotionEvent event) {
        if (DEBUG) {
            HwPCUtils.log(TAG, "onSinglePointerMode event:" + event);
        }
        int action = event.getAction();
        MotionEvent motionEvent = this.mDownEvent;
        if (motionEvent != null) {
            int pointerId = motionEvent.getPointerId(0);
            int curPointerId = event.getPointerId(0);
            if (pointerId != curPointerId) {
                if (DEBUG) {
                    HwPCUtils.log(TAG, "sendEvent pointerId:" + pointerId + " curPointerId:" + curPointerId);
                }
                this.isMove = false;
                this.mDownEvent = null;
                this.mClickCount = 0;
                this.mPoninterMode = PoninterMode.NORMAL_POINTER_MODE;
            } else if (action == 1) {
                processUpActionForTouchInSingleMode(event, action);
            } else if (action == 2) {
                processMoveActionForTouchInSingleMode(event, action);
            } else if (((65280 & action) >> 8) <= 0) {
                if (DEBUG) {
                    HwPCUtils.log(TAG, "sendEvent nukown action:" + action + " mClickCount:" + this.mClickCount + " isMove:" + this.isMove + " mOldClickTime:" + this.mOldClickTime);
                }
                onUpMouseEvent(event);
                this.mOldClickTime = 0;
                this.isMove = false;
                this.mDownEvent = null;
                this.mClickCount = 0;
                this.mPoninterMode = PoninterMode.NORMAL_POINTER_MODE;
            }
        } else if (action == 0) {
            processDownActionForTouchInSingleMode(event, action);
        }
    }

    private void processDownActionForTouchInSingleMode(MotionEvent event, int action) {
        this.isMove = false;
        this.mCurrentEventX = event.getX();
        this.mCurrentEventY = event.getY();
        this.mDownEvent = MotionEvent.obtain(event);
        long clickTime = SystemClock.uptimeMillis() - this.mOldClickTime;
        if (DEBUG) {
            HwPCUtils.log(TAG, "sendEvent ACTION_DOWN clickTime:" + clickTime + " mOldClickTime:" + this.mOldClickTime + " mClickCount:" + this.mClickCount);
        }
        this.mSendEvent.sendCheckLongPress();
        if (clickTime > 300) {
            this.mOldClickTime = 0;
            this.mClickCount = 0;
        }
        if (this.mClickCount >= 1) {
            onClickMouseEvent(event);
        }
        updatePointersForTouchInSingleMode(action);
    }

    private void processMoveActionForTouchInSingleMode(MotionEvent event, int action) {
        if (this.isMove || !isShakeMove(event.getX(), event.getY(), this.mDownEvent.getX(), this.mDownEvent.getY())) {
            this.isMove = true;
        }
        if (DEBUG) {
            HwPCUtils.log(TAG, "sendEvent ACTION_MOVE mClickCount:" + this.mClickCount + ", mLongPressed= " + this.mLongPressed + ", mTouchDownForLongPress= " + this.mTouchDownForLongPress);
        }
        if (this.mLongPressed) {
            if (this.mTouchDownForLongPress) {
                onHoverExitForRightClick(event);
                onLongPressedMouseDownEvent(event);
                onMouseEvent(event);
                this.mTouchDownForLongPress = false;
            } else {
                onMouseEvent(event);
            }
        } else if (this.mClickCount >= 1) {
            onMouseEvent(event);
        } else {
            onHoverEvent(event);
        }
        updatePointersForTouchInSingleMode(action);
    }

    private void processUpActionForTouchInSingleMode(MotionEvent event, int action) {
        if (DEBUG) {
            HwPCUtils.log(TAG, "sendEvent ACTION_UP mClickCount:" + this.mClickCount + " isMove:" + this.isMove + ", mSendZoomGestureDirectly = " + this.mSendZoomGestureDirectly + ", mLongPressed= " + this.mLongPressed + ", mTouchDownForLongPress= " + this.mTouchDownForLongPress);
        }
        if (this.mSendZoomGestureDirectly) {
            onTouchEvent(event, 1, event.getActionIndex(), true);
            this.mSendZoomGestureDirectly = false;
        }
        if (!this.mLongPressed) {
            if (!this.isMove) {
                if (this.mClickCount >= 1) {
                    onClickUpMouseEvent(event);
                    this.mOldClickTime = 0;
                    this.mClickCount = 0;
                } else {
                    onClickMouseEvent(this.mDownEvent, event);
                }
                this.mClickCount++;
                this.mOldClickTime = SystemClock.uptimeMillis();
            } else {
                if (this.mClickCount >= 1) {
                    onClickUpMouseEvent(event);
                } else {
                    onUpMouseEvent(event);
                }
                this.mOldClickTime = 0;
                this.mClickCount = 0;
            }
        }
        updatePointersForTouchInSingleMode(action);
        this.isMove = false;
        this.mDownEvent = null;
        this.mPoninterMode = PoninterMode.NORMAL_POINTER_MODE;
    }

    private void onLongPressedMouseDownEvent(MotionEvent event) {
        if (this.mSendEvent != null) {
            updatePointer(event);
            this.mSendEvent.send(obtainMotionEvent(event, this.mCurrentPointerX, this.mCurrentPointerY, 0, 1));
        }
    }

    private void onLongPressedMouseUpEvent(MotionEvent event) {
        if (this.mSendEvent != null) {
            updatePointer(event);
            this.mSendEvent.send(obtainMotionEvent(event, this.mCurrentPointerX, this.mCurrentPointerY, 1, 1));
        }
    }

    public void updatePointersForTouchInSingleMode(int action) {
        if (action == 0) {
            PointF[] pointFArr = this.mCurrentEventPointersForTouch;
            pointFArr[0].x = this.mCurrentEventX;
            pointFArr[0].y = this.mCurrentEventY;
            PointF[] pointFArr2 = this.mCurrentPointersForTouch;
            pointFArr2[0].x = this.mCurrentPointerX;
            pointFArr2[0].y = this.mCurrentPointerY;
        } else if (action == 1) {
            PointF[] pointFArr3 = this.mCurrentEventPointersForTouch;
            pointFArr3[0].x = DEFAULT_WIDTH;
            pointFArr3[0].y = DEFAULT_WIDTH;
            PointF[] pointFArr4 = this.mCurrentPointersForTouch;
            pointFArr4[0].x = DEFAULT_WIDTH;
            pointFArr4[0].y = DEFAULT_WIDTH;
        } else if (action == 2) {
            PointF[] pointFArr5 = this.mCurrentEventPointersForTouch;
            pointFArr5[0].x = this.mCurrentEventX;
            pointFArr5[0].y = this.mCurrentEventY;
            PointF[] pointFArr6 = this.mCurrentPointersForTouch;
            pointFArr6[0].x = this.mCurrentPointerX;
            pointFArr6[0].y = this.mCurrentPointerY;
        }
    }

    private void onMultiPointerMode(MotionEvent event) {
        if (DEBUG) {
            HwPCUtils.log(TAG, "onMultiPointerMode event:" + event);
        }
        int action = event.getAction();
        int i = action & 255;
        if (i == 5) {
            processPointDownEvent(event);
        } else if (i == 6) {
            processPointUpEvent(event);
        } else if (action == 2) {
            if (DEBUG) {
                HwPCUtils.log(TAG, "sendEvent multi pointer ACTION_MOVE mCurrentPointerX:" + this.mCurrentPointerX + " mCurrentPointerY:" + this.mCurrentPointerY + " eventX:" + event.getX() + " eventY:" + event.getY());
            }
            if (this.isMove || !isShakeMove(event.getX(), event.getY(), this.mCurrentEventPointersForTouch[event.getActionIndex()].x, this.mCurrentEventPointersForTouch[event.getActionIndex()].y)) {
                this.isMove = true;
                handlePointersForTouchInMultiMode(event);
            } else if (DEBUG) {
                HwPCUtils.log(TAG, "onMultiPointerMode isShakeMove");
            }
        } else if (action == 3) {
            this.isMove = false;
            this.mPoninterMode = PoninterMode.NORMAL_POINTER_MODE;
            handlePointersForTouchInMultiMode(event);
        } else if (DEBUG) {
            HwPCUtils.log(TAG, "action is not move and cancel, do nothing");
        }
    }

    private void processPointDownEvent(MotionEvent event) {
        this.mCurrentEventX = event.getX();
        this.mCurrentEventY = event.getY();
        if (DEBUG) {
            HwPCUtils.log(TAG, "sendEvent ACTION_POINTER_DOWN mCurrentEventX:" + this.mCurrentEventX + " mCurrentEventY:" + this.mCurrentEventY);
        }
        handlePointersForTouchInMultiMode(event);
        this.isMove = false;
        if (!this.mSendZoomGestureDirectly) {
            onHoverExitForRightClick(event);
        }
    }

    private void processPointUpEvent(MotionEvent event) {
        MotionEvent motionEvent;
        if (DEBUG) {
            HwPCUtils.log(TAG, "sendEvent ACTION_POINTER_UP mCurrentPointerX:" + this.mCurrentPointerX + " mCurrentPointerY:" + this.mCurrentPointerY);
        }
        if (!this.isMove && (motionEvent = this.mPointerDownEventForTouch) != null) {
            onMouseSecondaryClick(motionEvent, event, this.mCurrentPointerX, this.mCurrentPointerY);
        }
        this.isMove = false;
        handlePointersForTouchInMultiMode(event);
        onHoverEnterForRightClick(event);
        this.mPoninterMode = PoninterMode.NORMAL_POINTER_MODE;
    }

    public void handlePointersForTouchInMultiMode(MotionEvent event) {
        if (DEBUG) {
            HwPCUtils.log(TAG, "handlePointersForTouchInMultiMode event = " + event);
        }
        if (event.getPointerCount() > 2) {
            HwPCUtils.log(TAG, "only two fingers supported");
            return;
        }
        int action = event.getActionMasked();
        if (action == 2) {
            handlePointerMoveEventForTouch(event);
        } else if (action == 3) {
            this.mPointerDownEventForTouch = null;
            this.mLastMoveEventForTouch = null;
        } else if (action == 5) {
            handlePointerDownEventForTouch(event);
        } else if (action == 6) {
            handlePointerUpEventForTouch(event);
        }
    }

    private void adjustPointersExceedScreen(PointF pointer) {
        if (pointer.x < DEFAULT_WIDTH) {
            pointer.x = DEFAULT_WIDTH;
        }
        if (pointer.y < DEFAULT_WIDTH) {
            pointer.y = DEFAULT_WIDTH;
        }
    }

    private void calPointersCoordsByEventForTouch(float eventX, float eventY, int index) {
        this.mCurrentPointersForTouch[index].x += eventX - this.mCurrentEventPointersForTouch[index].x;
        this.mCurrentPointersForTouch[index].y += eventY - this.mCurrentEventPointersForTouch[index].y;
    }

    private MotionEvent.PointerCoords getPointerCoords(MotionEvent event, int index) {
        MotionEvent.PointerCoords outPointerCoords = new MotionEvent.PointerCoords();
        event.getPointerCoords(event.getActionIndex(), outPointerCoords);
        return outPointerCoords;
    }

    private void handlePointerDownEventForTouch(MotionEvent event) {
        if (event.getActionIndex() > 0) {
            MotionEvent.PointerCoords outPointerCoords = getPointerCoords(event, event.getActionIndex());
            this.mCurrentEventPointersForTouch[1].x = outPointerCoords.x;
            this.mCurrentEventPointersForTouch[1].y = outPointerCoords.y;
            PointF[] pointFArr = this.mCurrentPointersForTouch;
            pointFArr[1].x = (pointFArr[0].x + outPointerCoords.x) - this.mCurrentEventPointersForTouch[0].x;
            PointF[] pointFArr2 = this.mCurrentPointersForTouch;
            pointFArr2[1].y = (pointFArr2[0].y + outPointerCoords.y) - this.mCurrentEventPointersForTouch[0].y;
            adjustPointersExceedScreen(this.mCurrentPointersForTouch[1]);
            this.isNeedInjectDownForTouch = true;
            this.mRecentEventPointers1stFinger.clear();
            this.mRecentEventPointers2ndFinger.clear();
            this.mSendZoomGestureDirectly = false;
            this.mSendScrollDirectly = false;
            this.mPointerDownEventForTouch = MotionEvent.obtain(event);
        }
    }

    private void handlePointerUpEventForTouch(MotionEvent event) {
        int actionIndex = event.getActionIndex();
        onTouchEvent(event, (actionIndex << 8) | 6, actionIndex, true);
        if (DEBUG) {
            HwPCUtils.log(TAG, "handlePointerUpEventForTouch mLastMoveEventForTouch = " + this.mLastMoveEventForTouch);
        }
        if (this.mLastMoveEventForTouch != null) {
            onTouchUpEvent(actionIndex == 0 ? 1 : 0);
            this.mLastMoveEventForTouch = null;
        }
        if (event.getActionIndex() != 0) {
            this.mPointerDownEventForTouch = null;
        } else {
            this.mDownEventForTouch = this.mPointerDownEventForTouch;
        }
        this.mPoninterMode = PoninterMode.NORMAL_POINTER_MODE;
        if (DEBUG) {
            HwPCUtils.log(TAG, "handlePointerUpEventForTouch event.getActionIndex():" + event.getActionIndex());
        }
        if (event.getActionIndex() == 0) {
            PointF[] pointFArr = this.mCurrentEventPointersForTouch;
            pointFArr[0].x = pointFArr[1].x;
            PointF[] pointFArr2 = this.mCurrentEventPointersForTouch;
            pointFArr2[0].y = pointFArr2[1].y;
            PointF[] pointFArr3 = this.mCurrentPointersForTouch;
            pointFArr3[0].x = pointFArr3[1].x;
            PointF[] pointFArr4 = this.mCurrentPointersForTouch;
            pointFArr4[0].y = pointFArr4[1].y;
        }
        PointF[] pointFArr5 = this.mCurrentEventPointersForTouch;
        pointFArr5[1].x = DEFAULT_WIDTH;
        pointFArr5[1].y = DEFAULT_WIDTH;
        pointFArr5[1].x = DEFAULT_WIDTH;
        pointFArr5[1].y = DEFAULT_WIDTH;
    }

    private void handlePointerMoveEventForTouch(MotionEvent event) {
        if (event.getPointerCount() != 2) {
            HwPCUtils.log(TAG, "must two fingers when moving");
            return;
        }
        this.mLastMoveEventForTouch = MotionEvent.obtain(event);
        calPointersCoordsByEventForTouch(event.getX(), event.getY(), 0);
        this.mCurrentEventPointersForTouch[0].x = event.getX();
        this.mCurrentEventPointersForTouch[0].y = event.getY();
        MotionEvent.PointerCoords outPointerCoords = getPointerCoords(event, 1);
        event.getPointerCoords(1, outPointerCoords);
        calPointersCoordsByEventForTouch(outPointerCoords.x, outPointerCoords.y, 1);
        this.mCurrentEventPointersForTouch[1].x = outPointerCoords.x;
        this.mCurrentEventPointersForTouch[1].y = outPointerCoords.y;
        adjustPointersExceedScreen(this.mCurrentPointersForTouch[0]);
        adjustPointersExceedScreen(this.mCurrentPointersForTouch[1]);
        if (DEBUG) {
            HwPCUtils.log(TAG, "handlePointerMoveEventForTouch mSendScrollDirectly:" + this.mSendScrollDirectly + ", mSendZoomGestureDirectly = " + this.mSendZoomGestureDirectly);
        }
        if (!this.mSendScrollDirectly && !this.mSendZoomGestureDirectly) {
            insertPointerToArray(new PointF(event.getX(), event.getY()), new PointF(outPointerCoords.x, outPointerCoords.y));
            sendScrollEventIfNecessary(event);
        } else if (this.mSendScrollDirectly) {
            setCurrentEventXY(event);
        } else {
            if (!(!this.isNeedInjectDownForTouch || this.mDownEventForTouch == null || this.mPointerDownEventForTouch == null)) {
                if (DEBUG) {
                    HwPCUtils.log(TAG, "handlePointerMoveEventForTouch inject down event for touch");
                }
                onHoverExitForRightClick(event);
                onTouchDownEvent(this.mDownEventForTouch, this.mPointerDownEventForTouch);
                this.isNeedInjectDownForTouch = false;
                this.mRecentEventPointers1stFinger.clear();
                this.mRecentEventPointers2ndFinger.clear();
            }
            onTouchEvent(event, 2, event.getActionIndex(), true);
        }
    }

    private void setCurrentEventXY(MotionEvent event) {
        if (DEBUG) {
            HwPCUtils.log(TAG, "sendScrollEventIfNecessary, inject scroll event directly");
        }
        if (!isNeedSendGenericMotionEvent()) {
            onTouchEvent(event, 2, 0, false);
            this.mCurrentEventX = event.getX();
            this.mCurrentEventY = event.getY();
        } else if (onScrollEvent(event, event.getX() - this.mCurrentEventX, event.getY() - this.mCurrentEventY)) {
            this.mCurrentEventX = event.getX();
            this.mCurrentEventY = event.getY();
        }
    }

    private boolean isNeedSendGenericMotionEvent() {
        if (listenGenericMotionPackages.contains(getCurrFocusedWinInExtDisplay())) {
            return true;
        }
        return false;
    }

    private String getCurrFocusedWinInExtDisplay() {
        Bundle outBundle = new Bundle();
        HwWindowManager.getCurrFocusedWinInExtDisplay(outBundle);
        return outBundle.containsKey("pkgName") ? outBundle.getString("pkgName") : BuildConfig.FLAVOR;
    }

    private void insertPointerToArray(PointF eventPointer1stFinger, PointF eventPointer2ndFinger) {
        if (DEBUG) {
            HwPCUtils.log(TAG, "insertPointerToArray, size1 = " + this.mRecentEventPointers1stFinger.size() + ", size2 = " + this.mRecentEventPointers2ndFinger.size());
        }
        if (this.mSendZoomGestureDirectly || this.mSendScrollDirectly) {
            HwPCUtils.log(TAG, "insertPointerToArray, ignore check scroll event");
            return;
        }
        int sizeOfEventPointer1 = this.mRecentEventPointers1stFinger.size();
        int sizeOfEventPointer2 = this.mRecentEventPointers2ndFinger.size();
        if (sizeOfEventPointer1 == 0) {
            this.mRecentEventPointers1stFinger.add(eventPointer1stFinger);
            sizeOfEventPointer1++;
        }
        if (sizeOfEventPointer2 == 0) {
            this.mRecentEventPointers2ndFinger.add(eventPointer2ndFinger);
            sizeOfEventPointer2++;
        }
        if (!this.mRecentEventPointers1stFinger.get(sizeOfEventPointer1 - 1).equals(eventPointer1stFinger) && sizeOfEventPointer1 <= 2) {
            this.mRecentEventPointers1stFinger.add(eventPointer1stFinger);
        }
        if (!this.mRecentEventPointers2ndFinger.get(sizeOfEventPointer2 - 1).equals(eventPointer2ndFinger) && sizeOfEventPointer2 <= 2) {
            this.mRecentEventPointers2ndFinger.add(eventPointer2ndFinger);
        }
    }

    private boolean isScrollEvent(List<PointF> list0, List<PointF> list1) {
        if (list0.size() < 2 || list1.size() < 2) {
            return false;
        }
        PointF a = list0.get(0);
        PointF b = list0.get(list0.size() - 1);
        PointF c = list1.get(0);
        PointF d = list1.get(list1.size() - 1);
        PointF e = new PointF(d.x - (c.x - a.x), d.y - (c.y - a.y));
        double distanceAB = Math.sqrt(Math.pow((double) (a.x - b.x), 2.0d) + Math.pow((double) (a.y - b.y), 2.0d));
        double distanceAE = Math.sqrt(Math.pow((double) (a.x - e.x), 2.0d) + Math.pow((double) (a.y - e.y), 2.0d));
        double distanceBE = Math.sqrt(Math.pow((double) (b.x - e.x), 2.0d) + Math.pow((double) (b.y - e.y), 2.0d));
        if (distanceAB >= CONSIDER_ZERO && distanceAE >= CONSIDER_ZERO) {
            return Math.acos(((Math.pow(distanceAB, 2.0d) + Math.pow(distanceAE, 2.0d)) - Math.pow(distanceBE, 2.0d)) / ((2.0d * distanceAB) * distanceAE)) < HALF_OF_PI;
        }
        HwPCUtils.log(TAG, "isScrollEvent, distanceAB or distanceAE is 0");
        return true;
    }

    private void sendScrollEventIfNecessary(MotionEvent event) {
        if (DEBUG) {
            HwPCUtils.log(TAG, "sendScrollEventIfNecessary mSendZoomGestureDirectly = " + this.mSendZoomGestureDirectly);
        }
        if (this.mRecentEventPointers1stFinger.size() < 2 || this.mRecentEventPointers2ndFinger.size() < 2 || this.mSendZoomGestureDirectly) {
            HwPCUtils.log(TAG, "sendScrollEventIfNecessary, size < MAX or mSendZoomGestureDirectly");
        } else if (!isScrollEvent(this.mRecentEventPointers1stFinger, this.mRecentEventPointers2ndFinger)) {
            this.mSendScrollDirectly = false;
            this.mSendZoomGestureDirectly = true;
        } else if (!isNeedSendGenericMotionEvent()) {
            MotionEvent motionEvent = this.mDownEventForTouch;
            if (motionEvent != null) {
                onTouchDownEvent(motionEvent, null);
            }
            onTouchEvent(event, 2, 0, false);
            this.mCurrentEventX = event.getX();
            this.mCurrentEventY = event.getY();
            this.mSendScrollDirectly = true;
            this.mSendZoomGestureDirectly = false;
        } else if (onScrollEvent(event, event.getX() - this.mCurrentEventX, event.getY() - this.mCurrentEventY)) {
            this.mCurrentEventX = event.getX();
            this.mCurrentEventY = event.getY();
            this.mSendScrollDirectly = true;
            this.mSendZoomGestureDirectly = false;
        }
    }

    private void onTouchDownEvent(MotionEvent downEvent, MotionEvent motionEvent) {
        if (DEBUG) {
            HwPCUtils.log(TAG, "onTouchDownEvent, downEvent = " + downEvent + ", motionEvent = " + motionEvent);
        }
        if (this.mSendEvent != null) {
            MotionEvent downEvent0 = obtainTouchEvent(downEvent, this.mCurrentPointersForTouch[0].x, this.mCurrentPointersForTouch[0].y, 0, 0, false, null, false);
            this.mSendEvent.send(downEvent0);
            if (motionEvent != null) {
                this.mSendEvent.send(obtainTouchEvent(motionEvent, this.mCurrentPointersForTouch[1].x, this.mCurrentPointersForTouch[1].y, 261, 1, true, downEvent0, false));
            }
        }
    }

    private void onTouchUpEvent(int index) {
        if (DEBUG) {
            HwPCUtils.log(TAG, "onTouchUpEvent, index = " + index);
        }
        if (this.mSendEvent != null) {
            this.mSendEvent.send(obtainTouchEvent(this.mLastMoveEventForTouch, this.mCurrentPointersForTouch[index].x, this.mCurrentPointersForTouch[index].y, 1, index, false, null, false));
        }
    }

    private void onTouchEvent(MotionEvent motionEvent, int action, int actionIndex, boolean isMultiPointerEvent) {
        if (DEBUG) {
            HwPCUtils.log(TAG, "onTouchEvent motionEvent = " + motionEvent + ", action = " + action + ", actionIndex = " + actionIndex + ", isMultiPointerEvent = " + isMultiPointerEvent);
        }
        if (this.mSendEvent != null) {
            this.mSendEvent.send(obtainTouchEvent(motionEvent, -1.0f, -1.0f, action, actionIndex, isMultiPointerEvent, null, true));
        }
    }

    private PointF getPointFByIndex(MotionEvent event, int index) {
        event.getPointerCount();
        MotionEvent.PointerCoords outPointerCoords = new MotionEvent.PointerCoords();
        event.getPointerCoords(index, outPointerCoords);
        return new PointF(outPointerCoords.x, outPointerCoords.y);
    }

    private MotionEvent obtainTouchEvent(MotionEvent event, float x, float y, int action, int actionIndex, boolean isMultiPointerEvent, MotionEvent eventIndex, boolean fromCurrenPoints) {
        int pointerCount = event.getPointerCount();
        int realpointerCount = isMultiPointerEvent ? pointerCount : 1;
        int realpointerCount2 = 2;
        if (realpointerCount <= 2) {
            realpointerCount2 = realpointerCount;
        }
        MotionEvent.PointerProperties[] pointerProperties = new MotionEvent.PointerProperties[realpointerCount2];
        MotionEvent.PointerCoords[] pointerCoords = new MotionEvent.PointerCoords[realpointerCount2];
        if (DEBUG) {
            HwPCUtils.log(TAG, "obtainTouchEvent, pointerCount = " + pointerCount + ", actionIndex = " + actionIndex + ", isMultiPointerEvent = " + isMultiPointerEvent);
        }
        int realIndex = 0;
        int index = 0;
        while (true) {
            if (index >= pointerCount) {
                break;
            }
            if (isMultiPointerEvent || actionIndex == index) {
                MotionEvent.PointerProperties outPointerProperties = new MotionEvent.PointerProperties();
                event.getPointerProperties(index, outPointerProperties);
                outPointerProperties.toolType = 1;
                pointerProperties[realIndex] = outPointerProperties;
                MotionEvent.PointerCoords outPointerCoords = getPointerCoords(event, index);
                pointerCoords[realIndex] = outPointerCoords;
                if (fromCurrenPoints) {
                    outPointerCoords.x = this.mCurrentPointersForTouch[index].x;
                    outPointerCoords.y = this.mCurrentPointersForTouch[index].y;
                } else if (!isMultiPointerEvent || index != 0) {
                    outPointerCoords.x = x;
                    outPointerCoords.y = y;
                } else {
                    PointF tempPointF = getPointFByIndex(eventIndex, 0);
                    outPointerCoords.x = tempPointF.x;
                    outPointerCoords.y = tempPointF.y;
                }
                realIndex++;
                if (!isMultiPointerEvent) {
                    break;
                }
            }
            index++;
        }
        return MotionEventEx.obtain(event.getDownTime(), event.getEventTime(), action, realpointerCount2, pointerProperties, pointerCoords, event.getMetaState(), event.getButtonState(), event.getXPrecision(), event.getYPrecision(), event.getDeviceId(), event.getEdgeFlags(), 4098, HwPCUtils.getPCDisplayID(), 0);
    }

    private boolean isShakeMove(float moveX, float moveY, float downX, float downY) {
        int deltaX = (int) (moveX - downX);
        int deltaY = (int) (moveY - downY);
        int distance = (deltaX * deltaX) + (deltaY * deltaY);
        if (DEBUG) {
            HwPCUtils.log(TAG, "distance:" + distance + " mTouchSlopSquare:" + this.mTouchSlopSquare);
        }
        return distance < this.mTouchSlopSquare;
    }

    private void onMouseSecondaryClick(MotionEvent downEvent, MotionEvent upEvent, float x, float y) {
        if (this.mSendEvent != null) {
            this.mSendEvent.send(obtainMotionEvent(downEvent, x, y, 0, 2, 2));
            this.mSendEvent.send(obtainMotionEvent(downEvent, x, y, 11, 2, 2));
            this.mSendEvent.send(obtainMotionEvent(upEvent, x, y, 12, 2, 2));
            this.mSendEvent.send(obtainMotionEvent(upEvent, x, y, 1, upEvent.getButtonState(), 0));
        }
    }

    private MotionEvent obtainMotionEvent(MotionEvent event, float x, float y, int action, int buttonState) {
        MotionEvent.PointerProperties[] pointerProperties = new MotionEvent.PointerProperties[1];
        MotionEvent.PointerCoords[] pointerCoords = new MotionEvent.PointerCoords[1];
        for (int index = 0; index < 1; index++) {
            MotionEvent.PointerProperties outPointerProperties = new MotionEvent.PointerProperties();
            event.getPointerProperties(index, outPointerProperties);
            outPointerProperties.toolType = 3;
            pointerProperties[index] = outPointerProperties;
            MotionEvent.PointerCoords outPointerCoords = new MotionEvent.PointerCoords();
            event.getPointerCoords(index, outPointerCoords);
            pointerCoords[index] = outPointerCoords;
            if (index == 0) {
                outPointerCoords.x = x;
                outPointerCoords.y = y;
            }
        }
        return MotionEventEx.obtain(event.getDownTime(), event.getEventTime(), action, 1, pointerProperties, pointerCoords, event.getMetaState(), buttonState, event.getXPrecision(), event.getYPrecision(), event.getDeviceId(), event.getEdgeFlags(), 8194, HwPCUtils.getPCDisplayID(), 0);
    }

    public static MotionEvent obtainMotionEvent(MotionEvent event, float x, float y, int action, int buttonState, float vScroll, float hScroll) {
        MotionEvent.PointerProperties[] pointerProperties = new MotionEvent.PointerProperties[1];
        MotionEvent.PointerCoords[] pointerCoords = new MotionEvent.PointerCoords[1];
        for (int index = 0; index < 1; index++) {
            MotionEvent.PointerProperties outPointerProperties = new MotionEvent.PointerProperties();
            event.getPointerProperties(index, outPointerProperties);
            outPointerProperties.toolType = 3;
            pointerProperties[index] = outPointerProperties;
            MotionEvent.PointerCoords outPointerCoords = new MotionEvent.PointerCoords();
            event.getPointerCoords(index, outPointerCoords);
            outPointerCoords.setAxisValue(9, vScroll);
            outPointerCoords.setAxisValue(10, hScroll);
            pointerCoords[index] = outPointerCoords;
            if (index == 0) {
                outPointerCoords.x = x;
                outPointerCoords.y = y;
            }
        }
        return MotionEventEx.obtain(event.getDownTime(), event.getEventTime(), action, 1, pointerProperties, pointerCoords, event.getMetaState(), buttonState, event.getXPrecision(), event.getYPrecision(), event.getDeviceId(), event.getEdgeFlags(), 8194, HwPCUtils.getPCDisplayID(), 0);
    }

    private MotionEvent obtainMotionEvent(MotionEvent event, float x, float y, int action, int buttonState, int actionButton) {
        MotionEvent motionEvent = obtainMotionEvent(event, x, y, action, buttonState);
        MotionEventEx.setActionButton(motionEvent, actionButton);
        return motionEvent;
    }

    private void updatePointer(MotionEvent motionEvent) {
        if (DEBUG) {
            HwPCUtils.log(TAG, "updatePointer mCurrentEventX:" + this.mCurrentEventX + " mCurrentEventY:" + this.mCurrentEventY + " mCurrentPointerX:" + this.mCurrentPointerX + " mCurrentEventY:" + this.mCurrentPointerY + " motionX:" + motionEvent.getX() + " motionY:" + motionEvent.getY());
        }
        float x = (motionEvent.getX() - this.mCurrentEventX) + this.mCurrentPointerX;
        float y = (motionEvent.getY() - this.mCurrentEventY) + this.mCurrentPointerY;
        this.mCurrentEventX = motionEvent.getX();
        this.mCurrentEventY = motionEvent.getY();
        if (x < DEFAULT_WIDTH) {
            x = DEFAULT_WIDTH;
        }
        int i = this.mScreentWidth;
        if (x >= ((float) i)) {
            x = (float) (i - 1);
        }
        if (y < DEFAULT_WIDTH) {
            y = DEFAULT_WIDTH;
        }
        int i2 = this.mScreentHeight;
        if (y >= ((float) i2)) {
            y = (float) (i2 - 1);
        }
        this.mCurrentPointerX = x;
        this.mCurrentPointerY = y;
        if (DEBUG) {
            HwPCUtils.log(TAG, "updatePointer end mCurrentPointerX:" + this.mCurrentPointerX + " mCurrentPointerY:" + this.mCurrentPointerY);
        }
    }

    private boolean onScrollEvent(MotionEvent motionEvent, float distanceX, float distanceY) {
        if (DEBUG) {
            HwPCUtils.log(TAG, "onScrollEvent distanceX:" + distanceX + " distanceY:" + distanceY + " mDensity" + this.mDensity);
        }
        if (this.mSendEvent == null) {
            return false;
        }
        float distance = this.mDensity * 16.0f;
        if (Math.abs(distanceX) < distance && Math.abs(distanceY) < distance) {
            return false;
        }
        float vScroll = DEFAULT_WIDTH;
        float hScroll = DEFAULT_WIDTH;
        float f = 1.0f;
        if (Math.abs(distanceY) >= distance) {
            vScroll = distanceY > DEFAULT_WIDTH ? 1.0f : -1.0f;
        }
        if (Math.abs(distanceX) >= distance) {
            if (distanceX <= DEFAULT_WIDTH) {
                f = -1.0f;
            }
            hScroll = f;
        }
        this.mSendEvent.sendMoveEvent(obtainMotionEvent(motionEvent, this.mCurrentPointerX, this.mCurrentPointerY, 8, motionEvent.getButtonState(), vScroll, hScroll));
        return true;
    }

    private void onHoverEvent(MotionEvent motionEvent) {
        if (this.mSendEvent != null) {
            updatePointer(motionEvent);
            this.mSendEvent.sendMoveEvent(obtainMotionEvent(motionEvent, this.mCurrentPointerX, this.mCurrentPointerY, 7, motionEvent.getButtonState()));
        }
    }

    private void onHoverEnterForRightClick(MotionEvent motionEvent) {
        if (this.mSendEvent != null) {
            this.mSendEvent.send(obtainMotionEvent(motionEvent, this.mCurrentPointerX, this.mCurrentPointerY, 9, 2, 0));
        }
    }

    private void onHoverExitForRightClick(MotionEvent motionEvent) {
        if (this.mSendEvent != null) {
            this.mSendEvent.send(obtainMotionEvent(motionEvent, this.mCurrentPointerX, this.mCurrentPointerY, 10, 2, 0));
        }
    }

    private void onMouseEvent(MotionEvent motionEvent) {
        if (this.mSendEvent != null) {
            updatePointer(motionEvent);
            this.mSendEvent.sendMoveEvent(obtainMotionEvent(motionEvent, this.mCurrentPointerX, this.mCurrentPointerY, motionEvent.getAction(), 1));
        }
    }

    private void onClickMouseEvent(MotionEvent motionEvent) {
        if (this.mSendEvent != null) {
            modifyToMouseEvent(motionEvent, this.mCurrentPointerX, this.mCurrentPointerY, 1);
            this.mSendEvent.send(obtainMotionEvent(motionEvent, this.mCurrentPointerX, this.mCurrentPointerY, 11, 1, 1));
        }
    }

    private void onUpMouseEvent(MotionEvent upEvent) {
        modifyToMouseEvent(upEvent, this.mCurrentPointerX, this.mCurrentPointerY, upEvent.getButtonState());
    }

    private void onClickUpMouseEvent(MotionEvent upEvent) {
        if (this.mSendEvent != null) {
            this.mSendEvent.send(obtainMotionEvent(upEvent, this.mCurrentPointerX, this.mCurrentPointerY, 12, 1, 1));
            modifyToMouseEvent(upEvent, this.mCurrentPointerX, this.mCurrentPointerY, upEvent.getButtonState());
        }
    }

    private void onClickMouseEvent(MotionEvent downEvent, MotionEvent upEvent) {
        if (this.mSendEvent != null) {
            sendMouseEvent(0, 1, this.mCurrentPointerX, this.mCurrentPointerY, 0, 0);
            this.mSendEvent.send(obtainMotionEvent(downEvent, this.mCurrentPointerX, this.mCurrentPointerY, 11, 1, 1));
            this.mSendEvent.send(obtainMotionEvent(upEvent, this.mCurrentPointerX, this.mCurrentPointerY, 12, 1, 1));
            sendMouseEvent(1, 0, this.mCurrentPointerX, this.mCurrentPointerY, 0, 0);
        }
    }

    private void modifyToMouseEvent(MotionEvent event, float x, float y, int buttonState) {
        if (this.mSendEvent != null) {
            int pointerCount = event.getPointerCount();
            MotionEvent.PointerProperties[] pointerProperties = new MotionEvent.PointerProperties[pointerCount];
            MotionEvent.PointerCoords[] pointerCoords = new MotionEvent.PointerCoords[pointerCount];
            for (int index = 0; index < pointerCount; index++) {
                MotionEvent.PointerProperties outPointerProperties = new MotionEvent.PointerProperties();
                event.getPointerProperties(index, outPointerProperties);
                outPointerProperties.toolType = 3;
                pointerProperties[index] = outPointerProperties;
                MotionEvent.PointerCoords outPointerCoords = new MotionEvent.PointerCoords();
                event.getPointerCoords(index, outPointerCoords);
                pointerCoords[index] = outPointerCoords;
                if (index == 0) {
                    outPointerCoords.x = x;
                    outPointerCoords.y = y;
                }
            }
            this.mSendEvent.send(MotionEventEx.obtain(event.getDownTime(), event.getEventTime(), event.getAction(), event.getPointerCount(), pointerProperties, pointerCoords, event.getMetaState(), buttonState, event.getXPrecision(), event.getYPrecision(), event.getDeviceId(), event.getEdgeFlags(), 8194, HwPCUtils.getPCDisplayID(), 0));
        }
    }

    private void sendMouseEvent(int action, int buttonState, float xx, float yy, int offsetX, int offsetY) {
        SendEventThread sendEventThread = this.mSendEvent;
        if (sendEventThread != null) {
            sendEventThread.send(obtainMouseEvent(action, buttonState, xx, yy, offsetX, offsetY));
        }
    }

    public void sendFakedMouseMoveEvent() {
        SendEventThread sendEventThread = this.mSendEvent;
        if (sendEventThread != null) {
            sendEventThread.sendMoveEvent(obtainMouseEvent(2, 0, this.mCurrentPointerX, this.mCurrentPointerY, 0, 0));
        }
    }

    public static MotionEvent obtainMouseEvent(int action, int buttonState, float xx, float yy, int offsetX, int offsetY) {
        long eventTime = SystemClock.uptimeMillis();
        MotionEvent.PointerProperties[] props = {new MotionEvent.PointerProperties()};
        props[0].id = 0;
        props[0].toolType = 3;
        MotionEvent.PointerCoords[] coords = {new MotionEvent.PointerCoords()};
        coords[0].x = xx + ((float) offsetX);
        coords[0].y = yy + ((float) offsetY);
        return MotionEventEx.obtain(eventTime, eventTime, action, 1, props, coords, 0, buttonState, (float) DEFAULT_WIDTH, (float) DEFAULT_WIDTH, -1, 0, 8194, HwPCUtils.getPCDisplayID(), 0);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean sendPointerSync(InputEvent event) {
        if (this.mInputManager == null) {
            this.mInputManager = (InputManagerServiceEx.DefaultHwInputManagerLocalService) LocalServicesExt.getService(InputManagerServiceEx.DefaultHwInputManagerLocalService.class);
        }
        if (this.mInputManager == null) {
            return false;
        }
        if (DEBUG) {
            HwPCUtils.log(TAG, "sendPointerSync event = " + event);
        }
        return this.mInputManager.injectInputEvent(event, InputManagerEx.getInjectInputEventModeAsync());
    }

    /* access modifiers changed from: package-private */
    public class SendEventThread extends HandlerThread implements Handler.Callback {
        public static final int MSG_LONGPRESSED = 2;
        public static final int MSG_LONGPRESSED_CANCELED = 3;
        public static final int MSG_MOTIONEVENT = 0;
        public static final int MSG_MOTIONEVENT_MOVE = 1;
        private Handler mEventHandler;

        SendEventThread(String name) {
            super(name);
        }

        SendEventThread(String name, int priority) {
            super(name, priority);
        }

        /* access modifiers changed from: protected */
        @Override // android.os.HandlerThread
        public void onLooperPrepared() {
            this.mEventHandler = new Handler(getLooper(), this);
        }

        @Override // android.os.Handler.Callback
        public boolean handleMessage(Message msg) {
            int i = msg.what;
            if (i == 0 || i == 1) {
                if (msg.obj instanceof InputEvent) {
                    HwPCMkManager.this.sendPointerSync((InputEvent) msg.obj);
                }
            } else if (i == 2) {
                if (HwPCMkManager.DEBUG) {
                    HwPCUtils.log(HwPCMkManager.TAG, "MSG_LONGPRESSED");
                }
                longPressed(msg);
            }
            return true;
        }

        public void send(InputEvent event) {
            if (HwPCMkManager.DEBUG) {
                HwPCUtils.log(HwPCMkManager.TAG, "SendEventThread send event:" + event);
            }
            Message msg = Message.obtain();
            msg.what = 0;
            msg.obj = event;
            this.mEventHandler.sendMessage(msg);
        }

        public void sendMoveEvent(InputEvent event) {
            if (HwPCMkManager.DEBUG) {
                HwPCUtils.log(HwPCMkManager.TAG, "SendEventThread send move event:" + event);
            }
            this.mEventHandler.removeMessages(1);
            Message msg = Message.obtain();
            msg.what = 1;
            msg.obj = event;
            this.mEventHandler.sendMessage(msg);
        }

        public void clear() {
            this.mEventHandler.removeMessages(1);
            this.mEventHandler.removeMessages(0);
        }

        public void removeMessageLongPressed() {
            if (HwPCMkManager.DEBUG) {
                HwPCUtils.log(HwPCMkManager.TAG, "removeMessageLongPressed");
            }
            if (this.mEventHandler.hasMessages(2)) {
                this.mEventHandler.removeMessages(2);
            }
        }

        public void sendCheckLongPress() {
            if (HwPCMkManager.DEBUG) {
                HwPCUtils.log(HwPCMkManager.TAG, "sendCheckLongPress");
            }
            this.mEventHandler.removeMessages(2);
            Message msg = Message.obtain();
            msg.what = 2;
            this.mEventHandler.sendMessageDelayed(msg, 300);
        }

        private void longPressed(Message msg) {
            if (HwPCMkManager.DEBUG) {
                HwPCUtils.log(HwPCMkManager.TAG, "longPressed");
            }
            HwPCMkManager.this.mLongPressed = true;
            HwPCMkManager.this.mTouchDownForLongPress = true;
        }
    }

    public void updatePointerAxis(float[] axis) {
        this.mCurrentPointerX = axis[0];
        this.mCurrentPointerY = axis[1];
    }

    private void checkRemoveLongPressedMsg(MotionEvent event) {
        if (DEBUG) {
            HwPCUtils.log(TAG, "checkRemoveLongPressedMsg event:" + event + ", mLongPressed = " + this.mLongPressed);
        }
        if (event.getAction() == 2) {
            if (isShakeMove(event.getX(), event.getY(), this.mCurrentEventX, this.mCurrentEventY)) {
                if (DEBUG) {
                    HwPCUtils.log(TAG, "checkRemoveLongPressedMsg isShakeMove");
                }
            } else if (!this.mLongPressed) {
                this.mSendEvent.removeMessageLongPressed();
            }
        } else if (this.mPoninterMode == PoninterMode.SINGLE_POINTER_MODE && event.getAction() == 0) {
            if (DEBUG) {
                HwPCUtils.log(TAG, "checkRemoveLongPressedMsg event:" + event);
            }
        } else if (this.mLongPressed) {
            onLongPressedMouseUpEvent(event);
            onHoverEnterForRightClick(event);
            resetLongPress();
        } else {
            this.mSendEvent.removeMessageLongPressed();
        }
    }

    private void resetLongPress() {
        this.mLongPressed = false;
        this.mTouchDownForLongPress = false;
    }
}
