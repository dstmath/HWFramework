package com.android.server.pc;

import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.hardware.display.DisplayManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.DisplayMetrics;
import android.util.HwPCUtils;
import android.view.Display;
import android.view.InputEvent;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import com.android.server.LocalServices;
import com.android.server.gesture.GestureNavConst;
import com.android.server.input.HwInputManagerService;
import java.util.ArrayList;

public class HwPCMkManager {
    private static final double CONSIDER_ZERO = 1.0E-6d;
    public static final boolean DEBUG = SystemProperties.getBoolean("hw_pc_mkmanager_debug", false);
    private static final int FIRST_FINGER = 0;
    private static final double HALF_OF_PI = 1.57075d;
    private static final int MAX_CHECKED_PONTERS_COUNT = 2;
    private static final int MAX_FINGERS_SUPPORED = 2;
    private static final int MAX_TOUCH_SLOP = 4;
    public static final int MOTION_DOUBLE_CLICK_DELAY_TIME = 300;
    private static final int MOTION_LONG_PRESS_DELAY_TIME = 300;
    private static final int SECOND_FINGER = 1;
    public static final String TAG = "HwPCMkManager";
    private static HwPCMkManager mStatic;
    private final int SHOW_DEFAULT = 0;
    private final int SHOW_LASERPOINTER = 1;
    private final int SHOW_UNUSE = -1;
    final float default_height = 50.0f;
    final float default_width = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
    private int mClickCount = 0;
    private float mCoefficientX = 1.7f;
    private float mCoefficientY = 0.8f;
    private Context mContext;
    private PointF[] mCurrentEventPointersForTouch = {new PointF(GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO), new PointF(GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO)};
    private float mCurrentEventX = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
    private float mCurrentEventY = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
    private float mCurrentPointerX = 100.0f;
    private float mCurrentPointerY = 100.0f;
    private PointF[] mCurrentPointersForTouch = {new PointF(GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO), new PointF(GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO)};
    private float mDensity = 3.0f;
    private DisplayManager mDisplayManager;
    private MotionEvent mDownEvent = null;
    private MotionEvent mDownEventForTouch = null;
    private HwInputManagerService.HwInputManagerLocalService mInputManager = null;
    private boolean mIsMove = false;
    private boolean mIsSequent = false;
    private MotionEvent mLastMoveEventForTouch = null;
    /* access modifiers changed from: private */
    public boolean mLongPressed;
    private boolean mNeedInjectDownForTouch = true;
    private long mOldClickTime = 0;
    private HwPCManagerService mPCManager;
    private MotionEvent mPointerDownEventForTouch = null;
    private PoninterMode mPoninterMode = PoninterMode.NORMAL_POINTER_MODE;
    private ArrayList<PointF> mRecentEventPointers1stFinger = new ArrayList<>();
    private ArrayList<PointF> mRecentEventPointers2ndFinger = new ArrayList<>();
    private int mScreentHeight;
    private int mScreentWidth;
    private SendEventThread mSendEvent = null;
    private boolean mSendScrollDirectly;
    private boolean mSendZoomGestureDirectly;
    private int mShowMode = 0;
    /* access modifiers changed from: private */
    public boolean mTouchDownForLongPress;
    private int mTouchSlopSquare = 0;
    final float showlaserpointer_height = 90.0f;
    final float showlaserpointer_width = 46.0f;

    private enum PoninterMode {
        NORMAL_POINTER_MODE,
        MULTI_POINTER_MODE,
        SINGLE_POINTER_MODE
    }

    class SendEventThread extends HandlerThread implements Handler.Callback {
        public static final int MSG_LONGPRESSED = 2;
        public static final int MSG_LONGPRESSED_CANCELED = 3;
        public static final int MSG_MOTIONEVENT = 0;
        public static final int MSG_MOTIONEVENT_MOVE = 1;
        private Handler mEventHandler;

        public SendEventThread(String name) {
            super(name);
        }

        public SendEventThread(String name, int priority) {
            super(name, priority);
        }

        /* access modifiers changed from: protected */
        public void onLooperPrepared() {
            this.mEventHandler = new Handler(getLooper(), this);
        }

        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                case 1:
                    if (msg.obj instanceof InputEvent) {
                        boolean unused = HwPCMkManager.this.sendPointerSync((InputEvent) msg.obj);
                        break;
                    }
                    break;
                case 2:
                    if (HwPCMkManager.DEBUG) {
                        HwPCUtils.log(HwPCMkManager.TAG, "MSG_LONGPRESSED");
                    }
                    longPressed(msg);
                    break;
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
            boolean unused = HwPCMkManager.this.mLongPressed = true;
            boolean unused2 = HwPCMkManager.this.mTouchDownForLongPress = true;
        }
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

    private HwPCMkManager(Context context) {
        this.mContext = context;
        this.mDisplayManager = (DisplayManager) this.mContext.getSystemService("display");
        DisplayMetrics dm = new DisplayMetrics();
        Display mDefaultDisplay = this.mDisplayManager.getDisplay(0);
        if (mDefaultDisplay != null) {
            mDefaultDisplay.getMetrics(dm);
            this.mDensity = dm.density;
        }
        int touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        touchSlop = touchSlop > 4 ? 4 : touchSlop;
        this.mTouchSlopSquare = touchSlop * touchSlop;
        HwPCUtils.log(TAG, "MkManager mTouchSlopSquare:" + this.mTouchSlopSquare);
    }

    public void initCrop(Context context, HwPCManagerService pcManager) {
        this.mPCManager = pcManager;
        if (this.mDisplayManager != null) {
            Display mDisplay = this.mDisplayManager.getDisplay(HwPCUtils.getPCDisplayID());
            if (mDisplay != null) {
                Point size = new Point();
                mDisplay.getRealSize(size);
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
        OnDownEventProcess(event);
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
            if (this.mDownEvent != null) {
                this.mDownEventForTouch = MotionEvent.obtain(this.mDownEvent);
            }
            this.mDownEvent = null;
            this.mClickCount = 0;
            onMultiPointerMode(event);
        }
        return true;
    }

    private boolean isInTouchRect(MotionEvent event, Rect visibleRect, Rect displayRect, int mode) {
        if (mode != -1) {
            this.mShowMode = mode;
        }
        float height = 50.0f;
        float width = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        if (displayRect.right < displayRect.bottom) {
            if (this.mShowMode == 1) {
                height = 90.0f;
            }
            if (event.getX() < ((float) visibleRect.left) || event.getX() > ((float) visibleRect.right) || event.getY() < ((float) visibleRect.top) + dipsToPixels(height) || event.getY() > ((float) visibleRect.bottom)) {
                HwPCUtils.log(TAG, "sendEvent donot in visibleRect, right < bottom");
                return false;
            }
        } else {
            if (this.mShowMode == 1) {
                width = 46.0f;
            }
            if (event.getX() < ((float) visibleRect.left) || event.getX() > ((float) visibleRect.right) - dipsToPixels(width) || event.getY() < ((float) visibleRect.top) + dipsToPixels(50.0f) || event.getY() > ((float) visibleRect.bottom)) {
                HwPCUtils.log(TAG, "sendEvent donot in visibleRect right >= bottom");
                return false;
            }
        }
        return true;
    }

    private void OnDownEventProcess(MotionEvent event) {
        int action = event.getAction();
        if (action == 0) {
            if (DEBUG) {
                HwPCUtils.log(TAG, "OnDownEventProcess ACTION_DOWN mPoninterMode:" + this.mPoninterMode);
            }
            this.mPoninterMode = PoninterMode.SINGLE_POINTER_MODE;
            this.mIsSequent = true;
        } else if ((action & 255) == 5) {
            if (DEBUG) {
                HwPCUtils.log(TAG, "OnDownEventProcess ACTION_POINTER_DOWN mPoninterMode:" + this.mPoninterMode);
            }
            if (this.mPoninterMode == PoninterMode.NORMAL_POINTER_MODE) {
                this.mPoninterMode = PoninterMode.MULTI_POINTER_MODE;
            }
            if (this.mIsSequent) {
                this.mPoninterMode = PoninterMode.MULTI_POINTER_MODE;
            }
        } else if (this.mPoninterMode != PoninterMode.SINGLE_POINTER_MODE || action != 2 || !this.mIsSequent) {
            this.mIsSequent = false;
        }
    }

    private void onSinglePointerMode(MotionEvent event) {
        if (DEBUG) {
            HwPCUtils.log(TAG, "onSinglePointerMode event:" + event);
        }
        int action = event.getAction();
        if (this.mDownEvent != null) {
            if (this.mDownEvent.getPointerId(0) == event.getPointerId(0)) {
                switch (action) {
                    case 1:
                        if (DEBUG) {
                            HwPCUtils.log(TAG, "sendEvent ACTION_UP mClickCount:" + this.mClickCount + " mIsMove:" + this.mIsMove + ", mSendZoomGestureDirectly = " + this.mSendZoomGestureDirectly + ", mLongPressed= " + this.mLongPressed + ", mTouchDownForLongPress= " + this.mTouchDownForLongPress);
                        }
                        if (this.mSendZoomGestureDirectly) {
                            onTouchEvent(event, 1, event.getActionIndex());
                            this.mSendZoomGestureDirectly = false;
                        }
                        if (!this.mLongPressed) {
                            if (!this.mIsMove) {
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
                        this.mIsMove = false;
                        this.mDownEvent = null;
                        this.mPoninterMode = PoninterMode.NORMAL_POINTER_MODE;
                        break;
                    case 2:
                        if (this.mIsMove || !isShakeMove(event.getX(), event.getY(), this.mDownEvent.getX(), this.mDownEvent.getY())) {
                            this.mIsMove = true;
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
                        break;
                    default:
                        if (((65280 & action) >> 8) <= 0) {
                            if (DEBUG) {
                                HwPCUtils.log(TAG, "sendEvent nukown action:" + action + " mClickCount:" + this.mClickCount + " mIsMove:" + this.mIsMove + " mOldClickTime:" + this.mOldClickTime);
                            }
                            onUpMouseEvent(event);
                            this.mOldClickTime = 0;
                            this.mIsMove = false;
                            this.mDownEvent = null;
                            this.mClickCount = 0;
                            this.mPoninterMode = PoninterMode.NORMAL_POINTER_MODE;
                            break;
                        }
                        break;
                }
            } else {
                if (DEBUG) {
                    HwPCUtils.log(TAG, "sendEvent pointerId:" + pointerId + " curPointerId:" + curPointerId);
                }
                this.mIsMove = false;
                this.mDownEvent = null;
                this.mClickCount = 0;
                this.mPoninterMode = PoninterMode.NORMAL_POINTER_MODE;
            }
        } else if (action == 0) {
            this.mIsMove = false;
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
        switch (action) {
            case 0:
                this.mCurrentEventPointersForTouch[0].x = this.mCurrentEventX;
                this.mCurrentEventPointersForTouch[0].y = this.mCurrentEventY;
                this.mCurrentPointersForTouch[0].x = this.mCurrentPointerX;
                this.mCurrentPointersForTouch[0].y = this.mCurrentPointerY;
                return;
            case 1:
                this.mCurrentEventPointersForTouch[0].x = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
                this.mCurrentEventPointersForTouch[0].y = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
                this.mCurrentPointersForTouch[0].x = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
                this.mCurrentPointersForTouch[0].y = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
                return;
            case 2:
                this.mCurrentEventPointersForTouch[0].x = this.mCurrentEventX;
                this.mCurrentEventPointersForTouch[0].y = this.mCurrentEventY;
                this.mCurrentPointersForTouch[0].x = this.mCurrentPointerX;
                this.mCurrentPointersForTouch[0].y = this.mCurrentPointerY;
                return;
            default:
                return;
        }
    }

    private void onMultiPointerMode(MotionEvent event) {
        if (DEBUG) {
            HwPCUtils.log(TAG, "onMultiPointerMode event:" + event);
        }
        int action = event.getAction();
        switch (action & 255) {
            case 5:
                this.mCurrentEventX = event.getX();
                this.mCurrentEventY = event.getY();
                if (DEBUG) {
                    HwPCUtils.log(TAG, "sendEvent ACTION_POINTER_DOWN mCurrentEventX:" + this.mCurrentEventX + " mCurrentEventY:" + this.mCurrentEventY);
                }
                handlePointersForTouchInMultiMode(event);
                this.mIsMove = false;
                if (!this.mSendZoomGestureDirectly) {
                    onHoverExitForRightClick(event);
                    break;
                }
                break;
            case 6:
                if (DEBUG) {
                    HwPCUtils.log(TAG, "sendEvent ACTION_POINTER_UP mCurrentPointerX:" + this.mCurrentPointerX + " mCurrentPointerY:" + this.mCurrentPointerY);
                }
                if (!this.mIsMove && this.mPointerDownEventForTouch != null) {
                    onMouseSecondaryClick(this.mPointerDownEventForTouch, event, this.mCurrentPointerX, this.mCurrentPointerY);
                }
                this.mIsMove = false;
                handlePointersForTouchInMultiMode(event);
                onHoverEnterForRightClick(event);
                this.mPoninterMode = PoninterMode.NORMAL_POINTER_MODE;
                break;
            default:
                if (action != 2) {
                    if (action == 3) {
                        this.mIsMove = false;
                        this.mPoninterMode = PoninterMode.NORMAL_POINTER_MODE;
                        handlePointersForTouchInMultiMode(event);
                        break;
                    }
                } else {
                    if (DEBUG) {
                        HwPCUtils.log(TAG, "sendEvent multi pointer ACTION_MOVE mCurrentPointerX:" + this.mCurrentPointerX + " mCurrentPointerY:" + this.mCurrentPointerY + " eventX:" + event.getX() + " eventY:" + event.getY());
                    }
                    if (this.mIsMove || !isShakeMove(event.getX(), event.getY(), this.mCurrentEventPointersForTouch[event.getActionIndex()].x, this.mCurrentEventPointersForTouch[event.getActionIndex()].y)) {
                        this.mIsMove = true;
                        handlePointersForTouchInMultiMode(event);
                        break;
                    } else {
                        if (DEBUG) {
                            HwPCUtils.log(TAG, "onMultiPointerMode isShakeMove");
                        }
                        return;
                    }
                }
                break;
        }
    }

    public void handlePointersForTouchInMultiMode(MotionEvent event) {
        if (DEBUG) {
            HwPCUtils.log(TAG, "handlePointersForTouchInMultiMode event = " + event);
        }
        if (event.getPointerCount() > 2) {
            HwPCUtils.log(TAG, "only two fingers supported");
            return;
        }
        switch (event.getActionMasked()) {
            case 2:
                handlePointerMoveEventForTouch(event);
                break;
            case 3:
                this.mPointerDownEventForTouch = null;
                this.mLastMoveEventForTouch = null;
                break;
            case 5:
                handlePointerDownEventForTouch(event);
                break;
            case 6:
                handlePointerUpEventForTouch(event);
                break;
        }
    }

    private void adjustPointersExceedScreen(PointF pointer) {
        if (pointer.x < GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO) {
            pointer.x = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        }
        if (pointer.y < GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO) {
            pointer.y = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
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
            this.mCurrentPointersForTouch[1].x = (this.mCurrentPointersForTouch[0].x + outPointerCoords.x) - this.mCurrentEventPointersForTouch[0].x;
            this.mCurrentPointersForTouch[1].y = (this.mCurrentPointersForTouch[0].y + outPointerCoords.y) - this.mCurrentEventPointersForTouch[0].y;
            adjustPointersExceedScreen(this.mCurrentPointersForTouch[1]);
            this.mNeedInjectDownForTouch = true;
            this.mRecentEventPointers1stFinger.clear();
            this.mRecentEventPointers2ndFinger.clear();
            this.mSendZoomGestureDirectly = false;
            this.mSendScrollDirectly = false;
            this.mPointerDownEventForTouch = MotionEvent.obtain(event);
        }
    }

    private void handlePointerUpEventForTouch(MotionEvent event) {
        int actionIndex = event.getActionIndex();
        onTouchEvent(event, (actionIndex << 8) | 6, actionIndex);
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
            this.mCurrentEventPointersForTouch[0].x = this.mCurrentEventPointersForTouch[1].x;
            this.mCurrentEventPointersForTouch[0].y = this.mCurrentEventPointersForTouch[1].y;
            this.mCurrentPointersForTouch[0].x = this.mCurrentPointersForTouch[1].x;
            this.mCurrentPointersForTouch[0].y = this.mCurrentPointersForTouch[1].y;
        }
        this.mCurrentEventPointersForTouch[1].x = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        this.mCurrentEventPointersForTouch[1].y = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        this.mCurrentEventPointersForTouch[1].x = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        this.mCurrentEventPointersForTouch[1].y = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
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
            if (DEBUG) {
                HwPCUtils.log(TAG, "sendScrollEventIfNecessary, inject scroll event directly");
            }
            if (onScrollEvent(event, event.getX() - this.mCurrentEventX, event.getY() - this.mCurrentEventY)) {
                this.mCurrentEventX = event.getX();
                this.mCurrentEventY = event.getY();
            }
        } else {
            if (!(!this.mNeedInjectDownForTouch || this.mDownEventForTouch == null || this.mPointerDownEventForTouch == null)) {
                if (DEBUG) {
                    HwPCUtils.log(TAG, "handlePointerMoveEventForTouch inject down event for touch");
                }
                onHoverExitForRightClick(event);
                onTouchDownEvent(this.mDownEventForTouch, this.mPointerDownEventForTouch);
                this.mNeedInjectDownForTouch = false;
                this.mRecentEventPointers1stFinger.clear();
                this.mRecentEventPointers2ndFinger.clear();
            }
            onTouchEvent(event, 2, event.getActionIndex());
        }
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

    private boolean isScrollEvent(ArrayList<PointF> list0, ArrayList<PointF> list1) {
        ArrayList<PointF> arrayList = list0;
        ArrayList<PointF> arrayList2 = list1;
        if (list0.size() < 2 || list1.size() < 2) {
            return false;
        }
        PointF a = arrayList.get(0);
        PointF b = arrayList.get(list0.size() - 1);
        PointF c = arrayList2.get(0);
        PointF d = arrayList2.get(list1.size() - 1);
        PointF e = new PointF(d.x - (c.x - a.x), d.y - (c.y - a.y));
        double distanceAB = Math.sqrt(Math.pow((double) (a.x - b.x), 2.0d) + Math.pow((double) (a.y - b.y), 2.0d));
        PointF pointF = c;
        double distanceAE = Math.sqrt(Math.pow((double) (a.x - e.x), 2.0d) + Math.pow((double) (a.y - e.y), 2.0d));
        double distanceBE = Math.sqrt(Math.pow((double) (b.x - e.x), 2.0d) + Math.pow((double) (b.y - e.y), 2.0d));
        if (distanceAB < CONSIDER_ZERO || distanceAE < CONSIDER_ZERO) {
            HwPCUtils.log(TAG, "isScrollEvent, distanceAB or distanceAE is 0");
            return true;
        }
        return Math.acos(((Math.pow(distanceAB, 2.0d) + Math.pow(distanceAE, 2.0d)) - Math.pow(distanceBE, 2.0d)) / ((2.0d * distanceAB) * distanceAE)) < HALF_OF_PI;
    }

    private void sendScrollEventIfNecessary(MotionEvent event) {
        if (DEBUG) {
            HwPCUtils.log(TAG, "sendScrollEventIfNecessary mSendZoomGestureDirectly = " + this.mSendZoomGestureDirectly);
        }
        if (this.mRecentEventPointers1stFinger.size() < 2 || this.mRecentEventPointers2ndFinger.size() < 2 || this.mSendZoomGestureDirectly) {
            HwPCUtils.log(TAG, "sendScrollEventIfNecessary, size < MAX or mSendZoomGestureDirectly");
            return;
        }
        if (!isScrollEvent(this.mRecentEventPointers1stFinger, this.mRecentEventPointers2ndFinger)) {
            this.mSendScrollDirectly = false;
            this.mSendZoomGestureDirectly = true;
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
            this.mSendEvent.send(obtainTouchEvent(motionEvent, this.mCurrentPointersForTouch[1].x, this.mCurrentPointersForTouch[1].y, 261, 1, true, downEvent0, false));
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

    private void onTouchEvent(MotionEvent motionEvent, int action, int actionIndex) {
        if (DEBUG) {
            HwPCUtils.log(TAG, "onTouchEvent motionEvent = " + motionEvent + ", action = " + action + ", actionIndex = " + actionIndex);
        }
        if (this.mSendEvent != null) {
            this.mSendEvent.send(obtainTouchEvent(motionEvent, -1.0f, -1.0f, action, actionIndex, true, null, true));
        }
    }

    private PointF getPointFByIndex(MotionEvent event, int index) {
        int pointerCount = event.getPointerCount();
        MotionEvent.PointerCoords outPointerCoords = new MotionEvent.PointerCoords();
        event.getPointerCoords(index, outPointerCoords);
        return new PointF(outPointerCoords.x, outPointerCoords.y);
    }

    private MotionEvent obtainTouchEvent(MotionEvent event, float x, float y, int action, int actionIndex, boolean isMultiPointerEvent, MotionEvent eventIndex0, boolean fromCurrenPoints) {
        MotionEvent motionEvent = event;
        int i = actionIndex;
        boolean z = isMultiPointerEvent;
        int pointerCount = event.getPointerCount();
        int i2 = 1;
        int realpointerCount = z ? pointerCount : 1;
        int i3 = 2;
        if (realpointerCount <= 2) {
            i3 = realpointerCount;
        }
        int realpointerCount2 = i3;
        MotionEvent.PointerProperties[] pointerProperties = new MotionEvent.PointerProperties[realpointerCount2];
        MotionEvent.PointerCoords[] pointerCoords = new MotionEvent.PointerCoords[realpointerCount2];
        if (DEBUG) {
            HwPCUtils.log(TAG, "obtainTouchEvent, pointerCount = " + pointerCount + ", actionIndex = " + i + ", isMultiPointerEvent = " + z);
        }
        int realIndex = 0;
        int index = 0;
        while (true) {
            if (index >= pointerCount) {
                float f = x;
                float f2 = y;
                MotionEvent motionEvent2 = eventIndex0;
                break;
            }
            if (z || i == index) {
                MotionEvent.PointerProperties outPointerProperties = new MotionEvent.PointerProperties();
                motionEvent.getPointerProperties(index, outPointerProperties);
                outPointerProperties.toolType = i2;
                pointerProperties[realIndex] = outPointerProperties;
                MotionEvent.PointerCoords outPointerCoords = getPointerCoords(motionEvent, index);
                pointerCoords[realIndex] = outPointerCoords;
                if (fromCurrenPoints) {
                    outPointerCoords.x = this.mCurrentPointersForTouch[index].x;
                    outPointerCoords.y = this.mCurrentPointersForTouch[index].y;
                    float f3 = x;
                    float f4 = y;
                    MotionEvent motionEvent3 = eventIndex0;
                } else if (!z || index != 0) {
                    MotionEvent motionEvent4 = eventIndex0;
                    outPointerCoords.x = x;
                    outPointerCoords.y = y;
                } else {
                    PointF tempPointF = getPointFByIndex(eventIndex0, 0);
                    outPointerCoords.x = tempPointF.x;
                    outPointerCoords.y = tempPointF.y;
                    float f5 = x;
                    float f6 = y;
                }
                realIndex++;
                if (!z) {
                    break;
                }
            } else {
                float f7 = x;
                float f8 = y;
                MotionEvent motionEvent5 = eventIndex0;
            }
            index++;
            i2 = 1;
        }
        MotionEvent.PointerCoords[] pointerCoordsArr = pointerCoords;
        return MotionEvent.obtain(event.getDownTime(), event.getEventTime(), action, realpointerCount2, pointerProperties, pointerCoords, event.getMetaState(), event.getButtonState(), event.getXPrecision(), event.getYPrecision(), event.getDeviceId(), event.getEdgeFlags(), 4098, 0);
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
            float f = x;
            float f2 = y;
            this.mSendEvent.send(obtainMotionEvent(downEvent, f, f2, 11, 2, 2));
            MotionEvent motionEvent = upEvent;
            this.mSendEvent.send(obtainMotionEvent(motionEvent, f, f2, 12, 2, 2));
            this.mSendEvent.send(obtainMotionEvent(motionEvent, f, f2, 1, upEvent.getButtonState(), 0));
        }
    }

    private MotionEvent obtainMotionEvent(MotionEvent event, float x, float y, int action, int buttonState) {
        MotionEvent motionEvent = event;
        MotionEvent.PointerProperties[] pointerProperties = new MotionEvent.PointerProperties[1];
        MotionEvent.PointerCoords[] pointerCoords = new MotionEvent.PointerCoords[1];
        for (int index = 0; index < 1; index++) {
            MotionEvent.PointerProperties outPointerProperties = new MotionEvent.PointerProperties();
            motionEvent.getPointerProperties(index, outPointerProperties);
            outPointerProperties.toolType = 3;
            pointerProperties[index] = outPointerProperties;
            MotionEvent.PointerCoords outPointerCoords = new MotionEvent.PointerCoords();
            motionEvent.getPointerCoords(index, outPointerCoords);
            pointerCoords[index] = outPointerCoords;
            if (index == 0) {
                outPointerCoords.x = x;
                outPointerCoords.y = y;
            } else {
                float f = x;
                float f2 = y;
            }
        }
        float f3 = x;
        float f4 = y;
        long downTime = event.getDownTime();
        long eventTime = event.getEventTime();
        int metaState = event.getMetaState();
        float xPrecision = event.getXPrecision();
        float f5 = xPrecision;
        MotionEvent.PointerCoords[] pointerCoordsArr = pointerCoords;
        MotionEvent.PointerProperties[] pointerPropertiesArr = pointerProperties;
        return MotionEvent.obtain(downTime, eventTime, action, 1, pointerProperties, pointerCoords, metaState, buttonState, f5, event.getYPrecision(), event.getDeviceId(), event.getEdgeFlags(), 8194, 0);
    }

    public static MotionEvent obtainMotionEvent(MotionEvent event, float x, float y, int action, int buttonState, float vScroll, float hScroll) {
        MotionEvent motionEvent = event;
        MotionEvent.PointerProperties[] pointerProperties = new MotionEvent.PointerProperties[1];
        MotionEvent.PointerCoords[] pointerCoords = new MotionEvent.PointerCoords[1];
        for (int index = 0; index < 1; index++) {
            MotionEvent.PointerProperties outPointerProperties = new MotionEvent.PointerProperties();
            motionEvent.getPointerProperties(index, outPointerProperties);
            outPointerProperties.toolType = 3;
            pointerProperties[index] = outPointerProperties;
            MotionEvent.PointerCoords outPointerCoords = new MotionEvent.PointerCoords();
            motionEvent.getPointerCoords(index, outPointerCoords);
            outPointerCoords.setAxisValue(9, vScroll);
            outPointerCoords.setAxisValue(10, hScroll);
            pointerCoords[index] = outPointerCoords;
            if (index == 0) {
                outPointerCoords.x = x;
                outPointerCoords.y = y;
            } else {
                float f = x;
                float f2 = y;
            }
        }
        float f3 = x;
        float f4 = y;
        float f5 = vScroll;
        float f6 = hScroll;
        MotionEvent.PointerCoords[] pointerCoordsArr = pointerCoords;
        MotionEvent.PointerProperties[] pointerPropertiesArr = pointerProperties;
        return MotionEvent.obtain(event.getDownTime(), event.getEventTime(), action, 1, pointerProperties, pointerCoords, event.getMetaState(), buttonState, event.getXPrecision(), event.getYPrecision(), event.getDeviceId(), event.getEdgeFlags(), 8194, 0);
    }

    private MotionEvent obtainMotionEvent(MotionEvent event, float x, float y, int action, int buttonState, int actionButton) {
        MotionEvent motionEvent = obtainMotionEvent(event, x, y, action, buttonState);
        motionEvent.setActionButton(actionButton);
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
        if (x < GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO) {
            x = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        }
        if (x >= ((float) this.mScreentWidth)) {
            x = (float) (this.mScreentWidth - 1);
        }
        if (y < GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO) {
            y = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        }
        if (y >= ((float) this.mScreentHeight)) {
            y = (float) (this.mScreentHeight - 1);
        }
        this.mCurrentPointerX = x;
        this.mCurrentPointerY = y;
        if (DEBUG) {
            HwPCUtils.log(TAG, "updatePointer end mCurrentPointerX:" + this.mCurrentPointerX + " mCurrentPointerY:" + this.mCurrentPointerY);
        }
    }

    private boolean onScrollEvent(MotionEvent motionEvent, float distanceX, float distanceY) {
        float f = distanceX;
        float f2 = distanceY;
        if (DEBUG) {
            HwPCUtils.log(TAG, "onScrollEvent distanceX:" + f + " distanceY:" + f2 + " mDensity" + this.mDensity);
        }
        if (this.mSendEvent == null) {
            return false;
        }
        float distance = 16.0f * this.mDensity;
        if (Math.abs(distanceX) < distance && Math.abs(distanceY) < distance) {
            return false;
        }
        float vScroll = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        float hScroll = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        float f3 = -1.0f;
        if (Math.abs(distanceY) >= distance) {
            vScroll = f2 > GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO ? 1.0f : -1.0f;
        }
        if (Math.abs(distanceX) >= distance) {
            if (f > GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO) {
                f3 = 1.0f;
            }
            hScroll = f3;
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
        MotionEvent motionEvent = event;
        if (this.mSendEvent != null) {
            int pointerCount = event.getPointerCount();
            MotionEvent.PointerProperties[] pointerProperties = new MotionEvent.PointerProperties[pointerCount];
            MotionEvent.PointerCoords[] pointerCoords = new MotionEvent.PointerCoords[pointerCount];
            for (int index = 0; index < pointerCount; index++) {
                MotionEvent.PointerProperties outPointerProperties = new MotionEvent.PointerProperties();
                motionEvent.getPointerProperties(index, outPointerProperties);
                outPointerProperties.toolType = 3;
                pointerProperties[index] = outPointerProperties;
                MotionEvent.PointerCoords outPointerCoords = new MotionEvent.PointerCoords();
                motionEvent.getPointerCoords(index, outPointerCoords);
                pointerCoords[index] = outPointerCoords;
                if (index == 0) {
                    outPointerCoords.x = x;
                    outPointerCoords.y = y;
                } else {
                    float f = x;
                    float f2 = y;
                }
            }
            float f3 = x;
            float f4 = y;
            MotionEvent.PointerCoords[] pointerCoordsArr = pointerCoords;
            MotionEvent.PointerProperties[] pointerPropertiesArr = pointerProperties;
            this.mSendEvent.send(MotionEvent.obtain(event.getDownTime(), event.getEventTime(), event.getAction(), event.getPointerCount(), pointerProperties, pointerCoords, event.getMetaState(), buttonState, event.getXPrecision(), event.getYPrecision(), event.getDeviceId(), event.getEdgeFlags(), 8194, 0));
        }
    }

    private void sendMouseEvent(int action, int buttonState, float xx, float yy, int offsetX, int offsetY) {
        if (this.mSendEvent != null) {
            this.mSendEvent.send(obtainMouseEvent(action, buttonState, xx, yy, offsetX, offsetY));
        }
    }

    public void sendFakedMouseMoveEvent() {
        if (this.mSendEvent != null) {
            this.mSendEvent.sendMoveEvent(obtainMouseEvent(2, 0, this.mCurrentPointerX, this.mCurrentPointerY, 0, 0));
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
        return MotionEvent.obtain(eventTime, eventTime, action, 1, props, coords, 0, buttonState, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, -1, 0, 8194, 0);
    }

    /* access modifiers changed from: private */
    public boolean sendPointerSync(InputEvent event) {
        if (this.mInputManager == null) {
            this.mInputManager = (HwInputManagerService.HwInputManagerLocalService) LocalServices.getService(HwInputManagerService.HwInputManagerLocalService.class);
        }
        if (this.mInputManager == null) {
            return false;
        }
        if (DEBUG) {
            HwPCUtils.log(TAG, "sendPointerSync event = " + event);
        }
        return this.mInputManager.injectInputEvent(event, 0);
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
        } else if (!(this.mPoninterMode == PoninterMode.SINGLE_POINTER_MODE && event.getAction() == 0)) {
            if (this.mLongPressed) {
                onLongPressedMouseUpEvent(event);
                onHoverEnterForRightClick(event);
                resetLongPress();
            } else {
                this.mSendEvent.removeMessageLongPressed();
            }
        }
    }

    private void resetLongPress() {
        this.mLongPressed = false;
        this.mTouchDownForLongPress = false;
    }
}
