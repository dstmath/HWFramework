package com.huawei.internal.widget;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import com.huawei.android.app.HwActivityTaskManager;

public class HwDragBarPopupWindow extends PopupWindow {
    private static final String ACTION_CONFIRM_APPLOCK_CREDENTIAL = "huawei.intent.action.APPLOCK_FRAMEWORK_MANAGER";
    private static final String ACTION_CONFIRM_APPLOCK_CREDENTIAL_OPAQUE = "huawei.intent.action.APPLOCK_FRAMEWORK_MANAGER_LOCKSCREEN";
    private static final int AUTO_DELAY_DISMISS_TIME = 3000;
    private static final int AUTO_DISMISS_POPUP = 2020;
    private static final int BET_DIS = 6;
    private static final int BUBBLE_LEG_HEIGHT = 8;
    private static final int CAL_VAL = 2;
    private static final int DELAY_DISMISS_TIME = 200;
    private static final int DEVIATION = 1;
    private static final float DIP_TO_PIXEL_CONSTANT = 0.5f;
    private static final int LR_DIS = 4;
    private static final String PACKAGE_NAME_DOCK = "com.huawei.hwdockbar";
    private static final int PADDING_TOP = 12;
    private static final int RL_SHADOW_NEEDED_PADDING = 12;
    private static final int ROUND_DIS = 8;
    private static final int ROUND_VAL = 3;
    private static final String TAG = "HwDragBarPopupWindow";
    private static final int TB_DIS = 3;
    private int mAnimationStyle;
    private HwFreeFormCaptionView mCaptionView;
    private View mContentView;
    private Rect mDragBarRect;
    private Handler mHandler;
    private HwDragBarPopupBubbleLayout mHwBubbleLayout;
    private boolean mIsTouchDragBarDismiss;
    private float mLeftBoundary;
    private View mPopClose;
    private View mPopMaximize;
    private View mPopMinimize;
    private int mRatioValue;
    private float mRightBoundary;

    public HwDragBarPopupWindow(HwFreeFormCaptionView captionView) {
        this.mCaptionView = captionView;
        init();
    }

    private void init() {
        HwFreeFormCaptionView hwFreeFormCaptionView = this.mCaptionView;
        if (hwFreeFormCaptionView == null || hwFreeFormCaptionView.mContext == null) {
            Log.e(TAG, "init null return");
            return;
        }
        setWidth(-1);
        setHeight(-2);
        this.mContentView = LayoutInflater.from(this.mCaptionView.mContext).inflate(34013352, (ViewGroup) null);
        if (this.mContentView == null) {
            Log.e(TAG, "get mContentView fail");
            return;
        }
        getImageViews();
        this.mHwBubbleLayout = new HwDragBarPopupBubbleLayout(this.mCaptionView.mContext);
        this.mHwBubbleLayout.setBackgroundColor(0);
        this.mHwBubbleLayout.addView(this.mContentView);
        setContentView(this.mHwBubbleLayout);
        this.mAnimationStyle = this.mCaptionView.mContext.getResources().getIdentifier("androidhwext:style/mypopwindow_anim_style", null, null);
        setAnimationStyle(this.mAnimationStyle);
        setBackgroundDrawable(new ColorDrawable(0));
        setOutsideTouchable(true);
        setTouchable(true);
        initHandler();
    }

    public void show() {
        HwFreeFormCaptionView hwFreeFormCaptionView;
        InputMethodManager imm;
        if (this.mHwBubbleLayout == null || (hwFreeFormCaptionView = this.mCaptionView) == null || hwFreeFormCaptionView.mDragBar == null || this.mCaptionView.mCaption == null) {
            Log.e(TAG, "show null return");
            return;
        }
        this.mHwBubbleLayout.setContentWidth(this.mCaptionView.mCaption.getWidth());
        this.mRatioValue = Math.round(this.mHwBubbleLayout.getRatioValue());
        Log.d(TAG, "mRatioValue = " + this.mRatioValue);
        checkLayoutSizeAndDirection();
        checkAppLockAction();
        this.mHwBubbleLayout.updateRightAndLeftPadding();
        this.mHwBubbleLayout.setVisibility(0);
        if (!(this.mCaptionView.mContext == null || (imm = (InputMethodManager) this.mCaptionView.mContext.getSystemService(InputMethodManager.class)) == null)) {
            imm.hideSoftInputFromWindow(this.mCaptionView.mCaption.getWindowToken(), 2);
        }
        View view = this.mCaptionView.mDragBar;
        int height = this.mCaptionView.mDragBar.getHeight();
        HwDragBarPopupBubbleLayout hwDragBarPopupBubbleLayout = this.mHwBubbleLayout;
        showAtLocation(view, 0, 0, height - dip2px(7.0f));
        float f = this.mHwBubbleLayout.mPaddingRL;
        HwDragBarPopupBubbleLayout hwDragBarPopupBubbleLayout2 = this.mHwBubbleLayout;
        this.mLeftBoundary = f - ((float) dip2px(24.0f));
        float f2 = this.mHwBubbleLayout.mPaddingRL + this.mHwBubbleLayout.mWidthTmpSize;
        HwDragBarPopupBubbleLayout hwDragBarPopupBubbleLayout3 = this.mHwBubbleLayout;
        this.mRightBoundary = f2 + ((float) dip2px(24.0f));
        setTouchInterceptorForPop();
    }

    private void setTouchInterceptorForPop() {
        setTouchInterceptor(new View.OnTouchListener() {
            /* class com.huawei.internal.widget.HwDragBarPopupWindow.AnonymousClass1 */

            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View v, MotionEvent event) {
                int x = (int) event.getX();
                int y = (int) event.getY();
                if (event.getAction() == 0 && (((float) x) < HwDragBarPopupWindow.this.mLeftBoundary || ((float) x) > HwDragBarPopupWindow.this.mRightBoundary)) {
                    HwDragBarPopupWindow.this.dismiss();
                }
                if (event.getAction() == 4) {
                    if (HwDragBarPopupWindow.this.mDragBarRect != null && HwDragBarPopupWindow.this.mDragBarRect.contains(x, -y)) {
                        HwDragBarPopupWindow.this.mIsTouchDragBarDismiss = true;
                    }
                    if (x <= 0 && y <= 0) {
                        HwDragBarPopupWindow.this.mIsTouchDragBarDismiss = false;
                    }
                    if (HwDragBarPopupWindow.this.mHandler != null) {
                        HwDragBarPopupWindow.this.mHandler.removeMessages(HwDragBarPopupWindow.AUTO_DISMISS_POPUP);
                    }
                }
                return false;
            }
        });
    }

    public void delayDismiss() {
        Handler handler = this.mHandler;
        if (handler != null) {
            this.mHandler.sendMessageDelayed(handler.obtainMessage(AUTO_DISMISS_POPUP), 3000);
        }
    }

    private void initHandler() {
        this.mHandler = new Handler() {
            /* class com.huawei.internal.widget.HwDragBarPopupWindow.AnonymousClass2 */

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                if (msg.what == HwDragBarPopupWindow.AUTO_DISMISS_POPUP) {
                    HwDragBarPopupWindow.this.dismiss();
                }
            }
        };
    }

    public void setDragBarTouchRect(Rect dragBarRect) {
        this.mDragBarRect = dragBarRect;
    }

    public void setTouchDragBarDismissState(boolean state) {
        this.mIsTouchDragBarDismiss = state;
    }

    public boolean isTouchDragBarDismiss() {
        return this.mIsTouchDragBarDismiss;
    }

    private void getImageViews() {
        View view = this.mContentView;
        if (view != null) {
            this.mPopMaximize = view.findViewById(34603306);
            this.mPopMinimize = this.mContentView.findViewById(34603307);
            this.mPopClose = this.mContentView.findViewById(34603305);
            View view2 = this.mPopMaximize;
            if (view2 == null || this.mPopMinimize == null || this.mPopClose == null) {
                Log.e(TAG, "getImageViews null return");
                return;
            }
            view2.setOnClickListener(new ButtomClickListener());
            this.mPopMinimize.setOnClickListener(new ButtomClickListener());
            this.mPopClose.setOnClickListener(new ButtomClickListener());
        }
    }

    private void checkAppLockAction() {
        HwFreeFormCaptionView hwFreeFormCaptionView = this.mCaptionView;
        if (hwFreeFormCaptionView != null && hwFreeFormCaptionView.mOwner != null && (this.mCaptionView.mOwner.getContext() instanceof Activity) && this.mPopMaximize != null && this.mPopMinimize != null && this.mPopClose != null && this.mCaptionView.mContext != null) {
            Intent intent = ((Activity) this.mCaptionView.mOwner.getContext()).getIntent();
            if ((intent != null && isAppLockAction(intent.getAction())) || PACKAGE_NAME_DOCK.equals(this.mCaptionView.mContext.getPackageName())) {
                HwDragBarPopupBubbleLayout hwDragBarPopupBubbleLayout = this.mHwBubbleLayout;
                if (hwDragBarPopupBubbleLayout != null) {
                    hwDragBarPopupBubbleLayout.setContentWidthForAppLock();
                }
                this.mPopMaximize.setVisibility(8);
                this.mPopMinimize.setVisibility(8);
                if (this.mPopClose.getLayoutParams() instanceof RelativeLayout.LayoutParams) {
                    RelativeLayout.LayoutParams closeParams = (RelativeLayout.LayoutParams) this.mPopClose.getLayoutParams();
                    int i = this.mRatioValue;
                    closeParams.width = i * 20;
                    closeParams.height = i * 14;
                    this.mPopClose.setPadding(i * 6, i * 3, i * 6, i * 3);
                    closeParams.setMargins(0, 0, 0, 0);
                    this.mPopClose.setLayoutParams(closeParams);
                }
            }
        }
    }

    private boolean isAppLockAction(String action) {
        if (action == null) {
            return false;
        }
        if (ACTION_CONFIRM_APPLOCK_CREDENTIAL.equals(action) || ACTION_CONFIRM_APPLOCK_CREDENTIAL_OPAQUE.equals(action)) {
            return true;
        }
        return false;
    }

    private void checkLayoutSizeAndDirection() {
        HwFreeFormCaptionView hwFreeFormCaptionView;
        if (this.mPopMaximize == null || this.mPopMinimize == null || this.mPopClose == null || (hwFreeFormCaptionView = this.mCaptionView) == null || hwFreeFormCaptionView.mContext == null || !(this.mPopMaximize.getLayoutParams() instanceof RelativeLayout.LayoutParams) || !(this.mPopMinimize.getLayoutParams() instanceof RelativeLayout.LayoutParams) || !(this.mPopClose.getLayoutParams() instanceof RelativeLayout.LayoutParams)) {
            Log.e(TAG, "checkLayoutSizeAndDirection null or not RelativeLayout return");
            return;
        }
        RelativeLayout.LayoutParams maxParams = (RelativeLayout.LayoutParams) this.mPopMaximize.getLayoutParams();
        int i = this.mRatioValue;
        maxParams.width = i * 15;
        maxParams.height = i * 14;
        this.mPopMaximize.setPadding(i * 4, i * 3, i * 3, i * 3);
        maxParams.setMargins(0, 0, this.mRatioValue * 29, 0);
        if (this.mCaptionView.mIsRtl) {
            maxParams.setMargins(this.mRatioValue * 29, 0, 0, 0);
        }
        this.mPopMaximize.setLayoutParams(maxParams);
        RelativeLayout.LayoutParams minParams = (RelativeLayout.LayoutParams) this.mPopMinimize.getLayoutParams();
        int i2 = this.mRatioValue;
        minParams.width = i2 * 14;
        minParams.height = i2 * 14;
        this.mPopMinimize.setPadding(i2 * 3, i2 * 3, i2 * 3, i2 * 3);
        int i3 = this.mRatioValue;
        minParams.setMargins(i3 * 15, 0, i3 * 15, 0);
        this.mPopMinimize.setLayoutParams(minParams);
        RelativeLayout.LayoutParams closeParams = (RelativeLayout.LayoutParams) this.mPopClose.getLayoutParams();
        int i4 = this.mRatioValue;
        closeParams.width = i4 * 15;
        closeParams.height = i4 * 14;
        this.mPopClose.setPadding(i4 * 3, i4 * 3, i4 * 4, i4 * 3);
        closeParams.setMargins(this.mRatioValue * 29, 0, 0, 0);
        if (this.mCaptionView.mIsRtl) {
            closeParams.setMargins(0, 0, this.mRatioValue * 29, 0);
        }
        this.mPopClose.setLayoutParams(closeParams);
    }

    public void immediateInvisibleDismiss() {
        this.mHwBubbleLayout.setVisibility(4);
        delayRunDismiss();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void delayRunDismiss() {
        new Handler().postDelayed(new Runnable() {
            /* class com.huawei.internal.widget.HwDragBarPopupWindow.AnonymousClass3 */

            @Override // java.lang.Runnable
            public void run() {
                HwDragBarPopupWindow.this.dismiss();
            }
        }, 200);
    }

    /* access modifiers changed from: package-private */
    public class ButtomClickListener implements View.OnClickListener {
        ButtomClickListener() {
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View v) {
            if (HwDragBarPopupWindow.this.mCaptionView == null || HwDragBarPopupWindow.this.mCaptionView.mContext == null || HwDragBarPopupWindow.this.mCaptionView.mOwner == null || HwDragBarPopupWindow.this.mCaptionView.mOwner.getContext() == null || HwDragBarPopupWindow.this.mCaptionView.mCaption == null) {
                Log.e(HwDragBarPopupWindow.TAG, "onClick null return");
                return;
            }
            IBinder iBinder = null;
            switch (v.getId()) {
                case 34603305:
                    HwDragBarPopupWindow.this.dismiss();
                    InputMethodManager imm1 = (InputMethodManager) HwDragBarPopupWindow.this.mCaptionView.mContext.getSystemService(InputMethodManager.class);
                    if (imm1 != null) {
                        imm1.hideSoftInputFromWindow(HwDragBarPopupWindow.this.mCaptionView.mCaption.getWindowToken(), 2);
                    }
                    if (HwDragBarPopupWindow.this.mCaptionView.mOwner.getContext() instanceof Activity) {
                        iBinder = ((Activity) HwDragBarPopupWindow.this.mCaptionView.mOwner.getContext()).getActivityToken();
                    }
                    HwActivityTaskManager.removeTask(-1, iBinder, HwDragBarPopupWindow.this.mCaptionView.mOwner.getContext().getPackageName(), true, "pop-close-freeform");
                    return;
                case 34603306:
                    HwDragBarPopupWindow.this.dismiss();
                    IBinder appToken = HwDragBarPopupWindow.this.mCaptionView.mOwner.getAppToken();
                    if (appToken == null) {
                        HwDragBarPopupWindow.this.mCaptionView.onConfigurationChanged(false);
                    }
                    HwActivityTaskManager.toggleFreeformWindowingMode(appToken, HwDragBarPopupWindow.this.mCaptionView.mOwner.getContext().getPackageName());
                    return;
                case 34603307:
                    if (HwDragBarPopupWindow.this.mHwBubbleLayout != null) {
                        HwDragBarPopupWindow.this.mHwBubbleLayout.setVisibility(4);
                    }
                    if (HwDragBarPopupWindow.this.mCaptionView.mOwner.getContext() instanceof Activity) {
                        iBinder = ((Activity) HwDragBarPopupWindow.this.mCaptionView.mOwner.getContext()).getActivityToken();
                    }
                    HwActivityTaskManager.minimizeHwFreeForm(iBinder, HwDragBarPopupWindow.this.mCaptionView.mOwner.getContext().getPackageName(), true);
                    HwDragBarPopupWindow.this.delayRunDismiss();
                    return;
                default:
                    return;
            }
        }
    }

    private int dip2px(float dpValue) {
        HwFreeFormCaptionView hwFreeFormCaptionView = this.mCaptionView;
        if (hwFreeFormCaptionView == null || hwFreeFormCaptionView.mContext == null || this.mCaptionView.mContext.getResources() == null || this.mCaptionView.mContext.getResources().getDisplayMetrics() == null) {
            return 0;
        }
        return (int) ((dpValue * this.mCaptionView.mContext.getResources().getDisplayMetrics().density) + 0.5f);
    }
}
