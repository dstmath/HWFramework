package android.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.rms.AppAssociate;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewRootImpl;
import android.view.WindowManager;

@Deprecated
public class ZoomButtonsController implements View.OnTouchListener {
    private static final int MSG_DISMISS_ZOOM_CONTROLS = 3;
    private static final int MSG_POST_CONFIGURATION_CHANGED = 2;
    private static final int MSG_POST_SET_VISIBLE = 4;
    private static final String TAG = "ZoomButtonsController";
    /* access modifiers changed from: private */
    public static final int ZOOM_CONTROLS_TIMEOUT = ((int) ViewConfiguration.getZoomControlsTimeout());
    private static final int ZOOM_CONTROLS_TOUCH_PADDING = 20;
    private boolean mAutoDismissControls = true;
    /* access modifiers changed from: private */
    public OnZoomListener mCallback;
    private final IntentFilter mConfigurationChangedFilter = new IntentFilter("android.intent.action.CONFIGURATION_CHANGED");
    private final BroadcastReceiver mConfigurationChangedReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (ZoomButtonsController.this.mIsVisible) {
                ZoomButtonsController.this.mHandler.removeMessages(2);
                ZoomButtonsController.this.mHandler.sendEmptyMessage(2);
            }
        }
    };
    private final FrameLayout mContainer;
    private WindowManager.LayoutParams mContainerLayoutParams;
    private final int[] mContainerRawLocation = new int[2];
    private final Context mContext;
    private ZoomControls mControls;
    /* access modifiers changed from: private */
    public final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 2:
                    ZoomButtonsController.this.onPostConfigurationChanged();
                    return;
                case 3:
                    ZoomButtonsController.this.setVisible(false);
                    return;
                case 4:
                    if (ZoomButtonsController.this.mOwnerView.getWindowToken() == null) {
                        Log.e(ZoomButtonsController.TAG, "Cannot make the zoom controller visible if the owner view is not attached to a window.");
                        return;
                    } else {
                        ZoomButtonsController.this.setVisible(true);
                        return;
                    }
                default:
                    return;
            }
        }
    };
    /* access modifiers changed from: private */
    public boolean mIsVisible;
    /* access modifiers changed from: private */
    public final View mOwnerView;
    private final int[] mOwnerViewRawLocation = new int[2];
    private Runnable mPostedVisibleInitializer;
    private boolean mReleaseTouchListenerOnUp;
    private final int[] mTempIntArray = new int[2];
    private final Rect mTempRect = new Rect();
    private int mTouchPaddingScaledSq;
    private View mTouchTargetView;
    private final int[] mTouchTargetWindowLocation = new int[2];
    private final WindowManager mWindowManager;

    private class Container extends FrameLayout {
        public Container(Context context) {
            super(context);
        }

        public boolean dispatchKeyEvent(KeyEvent event) {
            if (ZoomButtonsController.this.onContainerKey(event)) {
                return true;
            }
            return super.dispatchKeyEvent(event);
        }
    }

    public interface OnZoomListener {
        void onVisibilityChanged(boolean z);

        void onZoom(boolean z);
    }

    public ZoomButtonsController(View ownerView) {
        this.mContext = ownerView.getContext();
        this.mWindowManager = (WindowManager) this.mContext.getSystemService(AppAssociate.ASSOC_WINDOW);
        this.mOwnerView = ownerView;
        this.mTouchPaddingScaledSq = (int) (20.0f * this.mContext.getResources().getDisplayMetrics().density);
        this.mTouchPaddingScaledSq *= this.mTouchPaddingScaledSq;
        this.mContainer = createContainer();
    }

    public void setZoomInEnabled(boolean enabled) {
        this.mControls.setIsZoomInEnabled(enabled);
    }

    public void setZoomOutEnabled(boolean enabled) {
        this.mControls.setIsZoomOutEnabled(enabled);
    }

    public void setZoomSpeed(long speed) {
        this.mControls.setZoomSpeed(speed);
    }

    private FrameLayout createContainer() {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(-2, -2);
        lp.gravity = 8388659;
        lp.flags = 131608;
        lp.height = -2;
        lp.width = -1;
        lp.type = 1000;
        lp.format = -3;
        lp.windowAnimations = 16974601;
        this.mContainerLayoutParams = lp;
        FrameLayout container = new Container(this.mContext);
        container.setLayoutParams(lp);
        container.setMeasureAllChildren(true);
        ((LayoutInflater) this.mContext.getSystemService("layout_inflater")).inflate(17367341, (ViewGroup) container);
        this.mControls = (ZoomControls) container.findViewById(16909549);
        this.mControls.setOnZoomInClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ZoomButtonsController.this.dismissControlsDelayed(ZoomButtonsController.ZOOM_CONTROLS_TIMEOUT);
                if (ZoomButtonsController.this.mCallback != null) {
                    ZoomButtonsController.this.mCallback.onZoom(true);
                }
            }
        });
        this.mControls.setOnZoomOutClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ZoomButtonsController.this.dismissControlsDelayed(ZoomButtonsController.ZOOM_CONTROLS_TIMEOUT);
                if (ZoomButtonsController.this.mCallback != null) {
                    ZoomButtonsController.this.mCallback.onZoom(false);
                }
            }
        });
        return container;
    }

    public void setOnZoomListener(OnZoomListener listener) {
        this.mCallback = listener;
    }

    public void setFocusable(boolean focusable) {
        int oldFlags = this.mContainerLayoutParams.flags;
        if (focusable) {
            this.mContainerLayoutParams.flags &= -9;
        } else {
            this.mContainerLayoutParams.flags |= 8;
        }
        if (this.mContainerLayoutParams.flags != oldFlags && this.mIsVisible) {
            this.mWindowManager.updateViewLayout(this.mContainer, this.mContainerLayoutParams);
        }
    }

    public boolean isAutoDismissed() {
        return this.mAutoDismissControls;
    }

    public void setAutoDismissed(boolean autoDismiss) {
        if (this.mAutoDismissControls != autoDismiss) {
            this.mAutoDismissControls = autoDismiss;
        }
    }

    public boolean isVisible() {
        return this.mIsVisible;
    }

    public void setVisible(boolean visible) {
        if (visible) {
            if (this.mOwnerView.getWindowToken() == null) {
                if (!this.mHandler.hasMessages(4)) {
                    this.mHandler.sendEmptyMessage(4);
                }
                return;
            }
            dismissControlsDelayed(ZOOM_CONTROLS_TIMEOUT);
        }
        if (this.mIsVisible != visible) {
            this.mIsVisible = visible;
            if (visible) {
                if (this.mContainerLayoutParams.token == null) {
                    this.mContainerLayoutParams.token = this.mOwnerView.getWindowToken();
                }
                this.mWindowManager.addView(this.mContainer, this.mContainerLayoutParams);
                if (this.mPostedVisibleInitializer == null) {
                    this.mPostedVisibleInitializer = new Runnable() {
                        public void run() {
                            ZoomButtonsController.this.refreshPositioningVariables();
                            if (ZoomButtonsController.this.mCallback != null) {
                                ZoomButtonsController.this.mCallback.onVisibilityChanged(true);
                            }
                        }
                    };
                }
                this.mHandler.post(this.mPostedVisibleInitializer);
                this.mContext.registerReceiver(this.mConfigurationChangedReceiver, this.mConfigurationChangedFilter);
                this.mOwnerView.setOnTouchListener(this);
                this.mReleaseTouchListenerOnUp = false;
            } else {
                if (this.mTouchTargetView != null) {
                    this.mReleaseTouchListenerOnUp = true;
                } else {
                    this.mOwnerView.setOnTouchListener(null);
                }
                this.mContext.unregisterReceiver(this.mConfigurationChangedReceiver);
                this.mWindowManager.removeViewImmediate(this.mContainer);
                this.mHandler.removeCallbacks(this.mPostedVisibleInitializer);
                if (this.mCallback != null) {
                    this.mCallback.onVisibilityChanged(false);
                }
            }
        }
    }

    public ViewGroup getContainer() {
        return this.mContainer;
    }

    public View getZoomControls() {
        return this.mControls;
    }

    /* access modifiers changed from: private */
    public void dismissControlsDelayed(int delay) {
        if (this.mAutoDismissControls) {
            this.mHandler.removeMessages(3);
            this.mHandler.sendEmptyMessageDelayed(3, (long) delay);
        }
    }

    /* access modifiers changed from: private */
    public void refreshPositioningVariables() {
        if (this.mOwnerView.getWindowToken() != null) {
            int ownerHeight = this.mOwnerView.getHeight();
            int ownerWidth = this.mOwnerView.getWidth();
            int containerOwnerYOffset = ownerHeight - this.mContainer.getHeight();
            this.mOwnerView.getLocationOnScreen(this.mOwnerViewRawLocation);
            this.mContainerRawLocation[0] = this.mOwnerViewRawLocation[0];
            this.mContainerRawLocation[1] = this.mOwnerViewRawLocation[1] + containerOwnerYOffset;
            int[] ownerViewWindowLoc = this.mTempIntArray;
            this.mOwnerView.getLocationInWindow(ownerViewWindowLoc);
            this.mContainerLayoutParams.x = ownerViewWindowLoc[0];
            this.mContainerLayoutParams.width = ownerWidth;
            this.mContainerLayoutParams.y = ownerViewWindowLoc[1] + containerOwnerYOffset;
            if (this.mIsVisible) {
                this.mWindowManager.updateViewLayout(this.mContainer, this.mContainerLayoutParams);
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean onContainerKey(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (isInterestingKey(keyCode)) {
            if (keyCode != 4) {
                dismissControlsDelayed(ZOOM_CONTROLS_TIMEOUT);
            } else if (event.getAction() == 0 && event.getRepeatCount() == 0) {
                if (this.mOwnerView != null) {
                    KeyEvent.DispatcherState ds = this.mOwnerView.getKeyDispatcherState();
                    if (ds != null) {
                        ds.startTracking(event, this);
                    }
                }
                return true;
            } else if (event.getAction() == 1 && event.isTracking() && !event.isCanceled()) {
                setVisible(false);
                return true;
            }
            return false;
        }
        ViewRootImpl viewRoot = this.mOwnerView.getViewRootImpl();
        if (viewRoot != null) {
            viewRoot.dispatchInputEvent(event);
        }
        return true;
    }

    private boolean isInterestingKey(int keyCode) {
        if (!(keyCode == 4 || keyCode == 66)) {
            switch (keyCode) {
                case 19:
                case 20:
                case 21:
                case 22:
                case 23:
                    break;
                default:
                    return false;
            }
        }
        return true;
    }

    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();
        if (event.getPointerCount() > 1) {
            return false;
        }
        if (this.mReleaseTouchListenerOnUp) {
            if (action == 1 || action == 3) {
                this.mOwnerView.setOnTouchListener(null);
                setTouchTargetView(null);
                this.mReleaseTouchListenerOnUp = false;
            }
            return true;
        }
        dismissControlsDelayed(ZOOM_CONTROLS_TIMEOUT);
        View targetView = this.mTouchTargetView;
        if (action != 3) {
            switch (action) {
                case 0:
                    targetView = findViewForTouch((int) event.getRawX(), (int) event.getRawY());
                    setTouchTargetView(targetView);
                    break;
                case 1:
                    break;
            }
        }
        setTouchTargetView(null);
        if (targetView == null) {
            return false;
        }
        int targetViewRawX = this.mContainerRawLocation[0] + this.mTouchTargetWindowLocation[0];
        int targetViewRawY = this.mContainerRawLocation[1] + this.mTouchTargetWindowLocation[1];
        MotionEvent containerEvent = MotionEvent.obtain(event);
        containerEvent.offsetLocation((float) (this.mOwnerViewRawLocation[0] - targetViewRawX), (float) (this.mOwnerViewRawLocation[1] - targetViewRawY));
        float containerX = containerEvent.getX();
        float containerY = containerEvent.getY();
        if (containerX < 0.0f && containerX > -20.0f) {
            containerEvent.offsetLocation(-containerX, 0.0f);
        }
        if (containerY < 0.0f && containerY > -20.0f) {
            containerEvent.offsetLocation(0.0f, -containerY);
        }
        boolean retValue = targetView.dispatchTouchEvent(containerEvent);
        containerEvent.recycle();
        return retValue;
    }

    private void setTouchTargetView(View view) {
        this.mTouchTargetView = view;
        if (view != null) {
            view.getLocationInWindow(this.mTouchTargetWindowLocation);
        }
    }

    private View findViewForTouch(int rawX, int rawY) {
        int distanceX;
        int distanceY;
        int containerCoordsX = rawX - this.mContainerRawLocation[0];
        int containerCoordsY = rawY - this.mContainerRawLocation[1];
        Rect frame = this.mTempRect;
        View closestChild = null;
        int closestChildDistanceSq = Integer.MAX_VALUE;
        int i = this.mContainer.getChildCount() - 1;
        while (true) {
            int i2 = i;
            if (i2 < 0) {
                return closestChild;
            }
            View child = this.mContainer.getChildAt(i2);
            if (child.getVisibility() == 0) {
                child.getHitRect(frame);
                if (frame.contains(containerCoordsX, containerCoordsY)) {
                    return child;
                }
                if (containerCoordsX < frame.left || containerCoordsX > frame.right) {
                    distanceX = Math.min(Math.abs(frame.left - containerCoordsX), Math.abs(containerCoordsX - frame.right));
                } else {
                    distanceX = 0;
                }
                if (containerCoordsY < frame.top || containerCoordsY > frame.bottom) {
                    distanceY = Math.min(Math.abs(frame.top - containerCoordsY), Math.abs(containerCoordsY - frame.bottom));
                } else {
                    distanceY = 0;
                }
                int distanceSq = (distanceX * distanceX) + (distanceY * distanceY);
                if (distanceSq < this.mTouchPaddingScaledSq && distanceSq < closestChildDistanceSq) {
                    closestChild = child;
                    closestChildDistanceSq = distanceSq;
                }
            }
            i = i2 - 1;
        }
    }

    /* access modifiers changed from: private */
    public void onPostConfigurationChanged() {
        dismissControlsDelayed(ZOOM_CONTROLS_TIMEOUT);
        refreshPositioningVariables();
    }
}
