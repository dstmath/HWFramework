package android.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.KeyEvent.DispatcherState;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewRootImpl;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import com.android.internal.R;
import com.android.internal.os.HwBootFail;
import com.android.internal.telephony.RILConstants;
import com.hisi.perfhub.PerfHub;
import com.huawei.hwperformance.HwPerformance;
import huawei.cust.HwCfgFilePolicy;

public class ZoomButtonsController implements OnTouchListener {
    private static final int MSG_DISMISS_ZOOM_CONTROLS = 3;
    private static final int MSG_POST_CONFIGURATION_CHANGED = 2;
    private static final int MSG_POST_SET_VISIBLE = 4;
    private static final String TAG = "ZoomButtonsController";
    private static final int ZOOM_CONTROLS_TIMEOUT = 0;
    private static final int ZOOM_CONTROLS_TOUCH_PADDING = 20;
    private boolean mAutoDismissControls;
    private OnZoomListener mCallback;
    private final IntentFilter mConfigurationChangedFilter;
    private final BroadcastReceiver mConfigurationChangedReceiver;
    private final FrameLayout mContainer;
    private LayoutParams mContainerLayoutParams;
    private final int[] mContainerRawLocation;
    private final Context mContext;
    private ZoomControls mControls;
    private final Handler mHandler;
    private boolean mIsVisible;
    private final View mOwnerView;
    private final int[] mOwnerViewRawLocation;
    private Runnable mPostedVisibleInitializer;
    private boolean mReleaseTouchListenerOnUp;
    private final int[] mTempIntArray;
    private final Rect mTempRect;
    private int mTouchPaddingScaledSq;
    private View mTouchTargetView;
    private final int[] mTouchTargetWindowLocation;
    private final WindowManager mWindowManager;

    private class Container extends FrameLayout {
        public Container(Context context) {
            super(context);
        }

        public boolean dispatchKeyEvent(KeyEvent event) {
            return ZoomButtonsController.this.onContainerKey(event) ? true : super.dispatchKeyEvent(event);
        }
    }

    public interface OnZoomListener {
        void onVisibilityChanged(boolean z);

        void onZoom(boolean z);
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.widget.ZoomButtonsController.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.widget.ZoomButtonsController.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.widget.ZoomButtonsController.<clinit>():void");
    }

    public ZoomButtonsController(View ownerView) {
        this.mAutoDismissControls = true;
        this.mOwnerViewRawLocation = new int[MSG_POST_CONFIGURATION_CHANGED];
        this.mContainerRawLocation = new int[MSG_POST_CONFIGURATION_CHANGED];
        this.mTouchTargetWindowLocation = new int[MSG_POST_CONFIGURATION_CHANGED];
        this.mTempRect = new Rect();
        this.mTempIntArray = new int[MSG_POST_CONFIGURATION_CHANGED];
        this.mConfigurationChangedFilter = new IntentFilter("android.intent.action.CONFIGURATION_CHANGED");
        this.mConfigurationChangedReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (ZoomButtonsController.this.mIsVisible) {
                    ZoomButtonsController.this.mHandler.removeMessages(ZoomButtonsController.MSG_POST_CONFIGURATION_CHANGED);
                    ZoomButtonsController.this.mHandler.sendEmptyMessage(ZoomButtonsController.MSG_POST_CONFIGURATION_CHANGED);
                }
            }
        };
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case ZoomButtonsController.MSG_POST_CONFIGURATION_CHANGED /*2*/:
                        ZoomButtonsController.this.onPostConfigurationChanged();
                    case ZoomButtonsController.MSG_DISMISS_ZOOM_CONTROLS /*3*/:
                        ZoomButtonsController.this.setVisible(false);
                    case ZoomButtonsController.MSG_POST_SET_VISIBLE /*4*/:
                        if (ZoomButtonsController.this.mOwnerView.getWindowToken() == null) {
                            Log.e(ZoomButtonsController.TAG, "Cannot make the zoom controller visible if the owner view is not attached to a window.");
                        } else {
                            ZoomButtonsController.this.setVisible(true);
                        }
                    default:
                }
            }
        };
        this.mContext = ownerView.getContext();
        this.mWindowManager = (WindowManager) this.mContext.getSystemService("window");
        this.mOwnerView = ownerView;
        this.mTouchPaddingScaledSq = (int) (this.mContext.getResources().getDisplayMetrics().density * 20.0f);
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
        LayoutParams lp = new LayoutParams(-2, -2);
        lp.gravity = 8388659;
        lp.flags = 131608;
        lp.height = -2;
        lp.width = -1;
        lp.type = RILConstants.RIL_UNSOL_RESPONSE_RADIO_STATE_CHANGED;
        lp.format = -3;
        lp.windowAnimations = R.style.Animation_ZoomButtons;
        this.mContainerLayoutParams = lp;
        ViewGroup container = new Container(this.mContext);
        container.setLayoutParams(lp);
        container.setMeasureAllChildren(true);
        ((LayoutInflater) this.mContext.getSystemService("layout_inflater")).inflate((int) R.layout.zoom_container, container);
        this.mControls = (ZoomControls) container.findViewById(R.id.zoomControls);
        this.mControls.setOnZoomInClickListener(new OnClickListener() {
            public void onClick(View v) {
                ZoomButtonsController.this.dismissControlsDelayed(ZoomButtonsController.ZOOM_CONTROLS_TIMEOUT);
                if (ZoomButtonsController.this.mCallback != null) {
                    ZoomButtonsController.this.mCallback.onZoom(true);
                }
            }
        });
        this.mControls.setOnZoomOutClickListener(new OnClickListener() {
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
        LayoutParams layoutParams;
        if (focusable) {
            layoutParams = this.mContainerLayoutParams;
            layoutParams.flags &= -9;
        } else {
            layoutParams = this.mContainerLayoutParams;
            layoutParams.flags |= 8;
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
                if (!this.mHandler.hasMessages(MSG_POST_SET_VISIBLE)) {
                    this.mHandler.sendEmptyMessage(MSG_POST_SET_VISIBLE);
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

    private void dismissControlsDelayed(int delay) {
        if (this.mAutoDismissControls) {
            this.mHandler.removeMessages(MSG_DISMISS_ZOOM_CONTROLS);
            this.mHandler.sendEmptyMessageDelayed(MSG_DISMISS_ZOOM_CONTROLS, (long) delay);
        }
    }

    private void refreshPositioningVariables() {
        if (this.mOwnerView.getWindowToken() != null) {
            int ownerHeight = this.mOwnerView.getHeight();
            int ownerWidth = this.mOwnerView.getWidth();
            int containerOwnerYOffset = ownerHeight - this.mContainer.getHeight();
            this.mOwnerView.getLocationOnScreen(this.mOwnerViewRawLocation);
            this.mContainerRawLocation[ZOOM_CONTROLS_TIMEOUT] = this.mOwnerViewRawLocation[ZOOM_CONTROLS_TIMEOUT];
            this.mContainerRawLocation[1] = this.mOwnerViewRawLocation[1] + containerOwnerYOffset;
            int[] ownerViewWindowLoc = this.mTempIntArray;
            this.mOwnerView.getLocationInWindow(ownerViewWindowLoc);
            this.mContainerLayoutParams.x = ownerViewWindowLoc[ZOOM_CONTROLS_TIMEOUT];
            this.mContainerLayoutParams.width = ownerWidth;
            this.mContainerLayoutParams.y = ownerViewWindowLoc[1] + containerOwnerYOffset;
            if (this.mIsVisible) {
                this.mWindowManager.updateViewLayout(this.mContainer, this.mContainerLayoutParams);
            }
        }
    }

    private boolean onContainerKey(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (isInterestingKey(keyCode)) {
            if (keyCode != MSG_POST_SET_VISIBLE) {
                dismissControlsDelayed(ZOOM_CONTROLS_TIMEOUT);
            } else if (event.getAction() == 0 && event.getRepeatCount() == 0) {
                if (this.mOwnerView != null) {
                    DispatcherState ds = this.mOwnerView.getKeyDispatcherState();
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
        switch (keyCode) {
            case MSG_POST_SET_VISIBLE /*4*/:
            case PerfHub.PERF_TAG_IPA_SUSTAINABLE_POWER /*19*/:
            case ZOOM_CONTROLS_TOUCH_PADDING /*20*/:
            case HwPerformance.PERF_TAG_DEF_L_CPU_MIN /*21*/:
            case HwPerformance.PERF_TAG_DEF_L_CPU_MAX /*22*/:
            case HwPerformance.PERF_TAG_DEF_B_CPU_MIN /*23*/:
            case RILConstants.RIL_REQUEST_QUERY_AVAILABLE_BAND_MODE /*66*/:
                return true;
            default:
                return false;
        }
    }

    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();
        if (event.getPointerCount() > 1) {
            return false;
        }
        if (this.mReleaseTouchListenerOnUp) {
            if (action == 1 || action == MSG_DISMISS_ZOOM_CONTROLS) {
                this.mOwnerView.setOnTouchListener(null);
                setTouchTargetView(null);
                this.mReleaseTouchListenerOnUp = false;
            }
            return true;
        }
        dismissControlsDelayed(ZOOM_CONTROLS_TIMEOUT);
        View targetView = this.mTouchTargetView;
        switch (action) {
            case ZOOM_CONTROLS_TIMEOUT /*0*/:
                targetView = findViewForTouch((int) event.getRawX(), (int) event.getRawY());
                setTouchTargetView(targetView);
                break;
            case HwCfgFilePolicy.EMUI /*1*/:
            case MSG_DISMISS_ZOOM_CONTROLS /*3*/:
                setTouchTargetView(null);
                break;
        }
        if (targetView == null) {
            return false;
        }
        int targetViewRawX = this.mContainerRawLocation[ZOOM_CONTROLS_TIMEOUT] + this.mTouchTargetWindowLocation[ZOOM_CONTROLS_TIMEOUT];
        int targetViewRawY = this.mContainerRawLocation[1] + this.mTouchTargetWindowLocation[1];
        MotionEvent containerEvent = MotionEvent.obtain(event);
        containerEvent.offsetLocation((float) (this.mOwnerViewRawLocation[ZOOM_CONTROLS_TIMEOUT] - targetViewRawX), (float) (this.mOwnerViewRawLocation[1] - targetViewRawY));
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
        int containerCoordsX = rawX - this.mContainerRawLocation[ZOOM_CONTROLS_TIMEOUT];
        int containerCoordsY = rawY - this.mContainerRawLocation[1];
        Rect frame = this.mTempRect;
        View closestChild = null;
        int closestChildDistanceSq = HwBootFail.STAGE_BOOT_SUCCESS;
        for (int i = this.mContainer.getChildCount() - 1; i >= 0; i--) {
            View child = this.mContainer.getChildAt(i);
            if (child.getVisibility() == 0) {
                child.getHitRect(frame);
                if (frame.contains(containerCoordsX, containerCoordsY)) {
                    return child;
                }
                int distanceX;
                int distanceY;
                if (containerCoordsX < frame.left || containerCoordsX > frame.right) {
                    distanceX = Math.min(Math.abs(frame.left - containerCoordsX), Math.abs(containerCoordsX - frame.right));
                } else {
                    distanceX = ZOOM_CONTROLS_TIMEOUT;
                }
                if (containerCoordsY < frame.top || containerCoordsY > frame.bottom) {
                    distanceY = Math.min(Math.abs(frame.top - containerCoordsY), Math.abs(containerCoordsY - frame.bottom));
                } else {
                    distanceY = ZOOM_CONTROLS_TIMEOUT;
                }
                int distanceSq = (distanceX * distanceX) + (distanceY * distanceY);
                if (distanceSq < this.mTouchPaddingScaledSq && distanceSq < closestChildDistanceSq) {
                    closestChild = child;
                    closestChildDistanceSq = distanceSq;
                }
            }
        }
        return closestChild;
    }

    private void onPostConfigurationChanged() {
        dismissControlsDelayed(ZOOM_CONTROLS_TIMEOUT);
        refreshPositioningVariables();
    }
}
