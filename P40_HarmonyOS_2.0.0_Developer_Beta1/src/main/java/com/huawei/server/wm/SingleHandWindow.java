package com.huawei.server.wm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.display.DisplayManager;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.server.wm.WindowManagerServiceEx;
import com.android.server.wm.utils.HwDisplaySizeUtilEx;
import com.huawei.android.util.SlogEx;
import com.huawei.android.view.DisplayEx;
import com.huawei.android.view.DisplayInfoEx;
import com.huawei.android.view.WindowManagerEx;
import com.huawei.singlehandlib.BuildConfig;
import com.huawei.utils.HwPartResourceUtils;

/* access modifiers changed from: package-private */
public final class SingleHandWindow {
    private static final int ALPHA_VALUE = 45;
    private static final int ALP_BLACK_COLOR = -1728053248;
    private static final int ALP_BLACK_COLOR1 = -872415232;
    private static final int BLACK_COLOR = 0;
    private static final String BLURPAPER = "blurpaper";
    private static final String BLURPAPER_TOP = "blurpapertop";
    private static final int DOUBLE_VALUE = 2;
    private static final int FREEZE_ROTATION = -1;
    private static final float INITIAL_SCALE = 0.75f;
    private static final boolean IS_DEBUG = false;
    public static final int LAZY_MODE_OFF = 0;
    public static final int LAZY_MODE_ON_LEFT = 1;
    public static final int LAZY_MODE_ON_RIGHT = 2;
    private static final int NINTH_DENOMINATOR = 9;
    private static final int QUARTER_DENOMINATOR = 4;
    private static final String QUICK_SWITCH_SINGLE_HAND_TYPE = "launcher_quick_switch_single_hand_type";
    private static final String SINGLE_HAND_MODE_HINT_SHOWN = "single_hand_mode_hint_shown";
    private static final int SLEEP_TIME = 100;
    private static final int SLIDE_COLOR = 255;
    private static final String TAG = "SingleHand";
    private static final float WINDOW_ALPHA = 1.0f;
    private static final String YES = "yes";
    private View.OnClickListener mActionClickListener = new View.OnClickListener() {
        /* class com.huawei.server.wm.SingleHandWindow.AnonymousClass3 */

        @Override // android.view.View.OnClickListener
        public void onClick(View v) {
            if (SingleHandWindow.this.mIsHintShowing) {
                SingleHandWindow.this.showHint(false);
                return;
            }
            SingleHandWindow.this.quitSingleHand();
            SingleHandUtils.startSettingsIntent(SingleHandWindow.this.mContext);
        }
    };
    private Configuration mConfiguration = new Configuration();
    private View.OnClickListener mConfirmClickListener = new View.OnClickListener() {
        /* class com.huawei.server.wm.SingleHandWindow.AnonymousClass5 */

        @Override // android.view.View.OnClickListener
        public void onClick(View v) {
            SingleHandWindow.this.showHint(false);
            Settings.Global.putString(SingleHandWindow.this.mContext.getContentResolver(), SingleHandWindow.SINGLE_HAND_MODE_HINT_SHOWN, SingleHandWindow.YES);
        }
    };
    private final Context mContext;
    private DisplayInfoEx mDefaultDisplayInfo;
    private final DisplayManager.DisplayListener mDisplayListener = new DisplayManager.DisplayListener() {
        /* class com.huawei.server.wm.SingleHandWindow.AnonymousClass4 */

        @Override // android.hardware.display.DisplayManager.DisplayListener
        public void onDisplayAdded(int displayId) {
        }

        @Override // android.hardware.display.DisplayManager.DisplayListener
        public void onDisplayChanged(int displayId) {
            if (displayId != DisplayEx.getDisplayId(SingleHandWindow.this.mWindowManager.getDefaultDisplay())) {
                return;
            }
            if (!SingleHandWindow.this.updateDefaultDisplayInfo()) {
                SingleHandWindow.this.dismiss();
            } else if (SingleHandWindow.this.mIsNeedRelayout) {
                SingleHandWindow.this.relayout();
            }
        }

        @Override // android.hardware.display.DisplayManager.DisplayListener
        public void onDisplayRemoved(int displayId) {
            if (displayId == DisplayEx.getDisplayId(SingleHandWindow.this.mWindowManager.getDefaultDisplay())) {
                SingleHandWindow.this.dismiss();
            }
        }
    };
    private final DisplayManager mDisplayManager;
    private Handler mHandler;
    private int mHeight;
    private float mHeightScale;
    private ImageView mImageView;
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        /* class com.huawei.server.wm.SingleHandWindow.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null) {
                String action = intent.getAction();
                if ("android.intent.action.LOCALE_CHANGED".equals(action)) {
                    SingleHandWindow.this.updateLocale();
                }
                if ("android.intent.action.CONFIGURATION_CHANGED".equals(action)) {
                    SingleHandWindow.this.updateConfiguration();
                }
            }
        }
    };
    private boolean mIsAttachedToWindow = false;
    private boolean mIsHintShowing;
    private final boolean mIsLeft;
    private boolean mIsNeedRelayout = false;
    private boolean mIsPointDownOuter = false;
    private boolean mIsShownOnce = false;
    private boolean mIsWindowVisible;
    private ViewGroup.LayoutParams mLayoutParams;
    private int mLazyMode = 0;
    private final String mName;
    private final View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
        /* class com.huawei.server.wm.SingleHandWindow.AnonymousClass2 */

        @Override // android.view.View.OnTouchListener
        public boolean onTouch(View view, MotionEvent event) {
            boolean inRegion = SingleHandWindow.this.singlehandRegionContainsPoint((int) event.getRawX(), (int) event.getRawY());
            int actionMasked = event.getActionMasked();
            if (actionMasked == 0) {
                SingleHandWindow.this.mIsPointDownOuter = !inRegion;
            } else if (actionMasked == 1) {
                ImageView imageView = (ImageView) SingleHandWindow.this.mWindowContent.findViewById(34603162);
                if (imageView == null || imageView.getVisibility() != 0) {
                    SingleHandWindow.this.showHint(false);
                } else if (!inRegion && SingleHandWindow.this.mIsPointDownOuter) {
                    SingleHandWindow.this.quitSingleHand();
                }
                SingleHandWindow.this.mIsPointDownOuter = false;
            } else if (actionMasked == 3) {
                SingleHandWindow.this.mIsPointDownOuter = false;
            }
            return true;
        }
    };
    private DisplayInfoEx mPreDisplayInfo = new DisplayInfoEx();
    private RelativeLayout mRelateViewtop;
    private final WindowManagerServiceEx mService;
    private int mSettingMarginEnd;
    private IsingleHandInner mSingleHandInner;
    private int mTipsMarginEnd;
    private int mTipsMarginStart;
    private int mWidth;
    private float mWidthScale;
    private View mWindowContent;
    private final WindowManager mWindowManager;
    private WindowManagerEx.LayoutParamsEx mWindowParams;

    SingleHandWindow(boolean isLeft, String name, int width, int height, IsingleHandInner singleHandInner) {
        this.mSingleHandInner = singleHandInner;
        this.mContext = singleHandInner.getWindowManagerServiceEx().getContext();
        this.mName = name;
        this.mWidth = width;
        this.mHeight = height;
        this.mIsLeft = isLeft;
        this.mHandler = new Handler(Looper.myLooper());
        this.mWindowManager = (WindowManager) this.mContext.getSystemService("window");
        this.mDisplayManager = (DisplayManager) this.mContext.getSystemService("display");
        this.mService = singleHandInner.getWindowManagerServiceEx();
        this.mDefaultDisplayInfo = this.mService.getDefaultDisplayContentLocked().getDisplayInfoEx();
        this.mConfiguration = this.mContext.getResources().getConfiguration();
        this.mPreDisplayInfo.copyFrom(this.mDefaultDisplayInfo);
        if (this.mName.contains(BLURPAPER)) {
            createWindow();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void quitSingleHand() {
        if (!this.mSingleHandInner.isDoAnimation()) {
            Settings.Global.putString(this.mContext.getContentResolver(), "single_hand_mode", BuildConfig.FLAVOR);
        }
    }

    private void show(View v, boolean isVis) {
        if (v == null) {
            return;
        }
        if (isVis) {
            v.setVisibility(0);
        } else {
            v.setVisibility(QUARTER_DENOMINATOR);
        }
    }

    public void show() {
        if (!this.mIsWindowVisible) {
            if (!this.mName.contains(BLURPAPER)) {
                this.mService.freezeOrThawRotation(0);
                this.mLazyMode = this.mIsLeft ? 1 : 2;
                this.mSingleHandInner.doAnimation(isQuickMode(), true);
                this.mService.setLazyMode(this.mLazyMode, this.mIsHintShowing, this.mName);
            }
            this.mDisplayManager.registerDisplayListener(this.mDisplayListener, null);
            if (!updateDefaultDisplayInfo()) {
                this.mDisplayManager.unregisterDisplayListener(this.mDisplayListener);
                return;
            }
            if (BLURPAPER_TOP.equals(this.mName)) {
                this.mWindowParams.getLayoutParams().x = 0;
                this.mWindowParams.getLayoutParams().y = 0;
                this.mWindowParams.getLayoutParams().width = this.mWidth;
                this.mWindowParams.getLayoutParams().height = this.mHeight;
                this.mWindowContent.setOnTouchListener(this.mOnTouchListener);
                this.mWindowManager.addView(this.mWindowContent, this.mWindowParams.getLayoutParams());
            }
            this.mIsWindowVisible = true;
        }
    }

    public void dismiss() {
        if (this.mIsAttachedToWindow) {
            this.mIsAttachedToWindow = false;
            this.mContext.unregisterReceiver(this.mIntentReceiver);
        }
        if (this.mIsWindowVisible) {
            if (updateDefaultDisplayInfo()) {
                this.mDisplayManager.unregisterDisplayListener(this.mDisplayListener);
            }
            if (!this.mName.contains(BLURPAPER)) {
                this.mHandler.postDelayed(new Runnable() {
                    /* class com.huawei.server.wm.SingleHandWindow.AnonymousClass6 */

                    @Override // java.lang.Runnable
                    public void run() {
                        SingleHandWindow.this.mService.freezeOrThawRotation((int) SingleHandWindow.FREEZE_ROTATION);
                        SlogEx.i(SingleHandWindow.TAG, "postDelayed FREEZE_ROTATION");
                    }
                }, 100);
                this.mLazyMode = 0;
                this.mIsHintShowing = false;
                this.mSingleHandInner.doAnimation(false, false);
                this.mService.setLazyMode(this.mLazyMode, this.mIsHintShowing, this.mName);
            } else {
                this.mWindowManager.removeView(this.mWindowContent);
            }
            this.mIsWindowVisible = false;
        }
    }

    public void relayout() {
        if (this.mIsWindowVisible && this.mName.contains(BLURPAPER)) {
            this.mWindowManager.removeView(this.mWindowContent);
            this.mIsWindowVisible = false;
            createWindow();
            updateWindowParams();
            this.mWindowContent.setOnTouchListener(this.mOnTouchListener);
            this.mWindowManager.addView(this.mWindowContent, this.mWindowParams.getLayoutParams());
            this.mIsWindowVisible = true;
            this.mIsShownOnce = true;
            flushWallPaper();
            this.mSingleHandInner.relayoutMatrix();
        }
    }

    /* access modifiers changed from: package-private */
    public void updateLocale() {
        TextView textView = (TextView) this.mWindowContent.findViewById(HwPartResourceUtils.getResourceId("overlay_display_window_title"));
        if (textView != null) {
            textView.setText(this.mContext.getResources().getString(HwPartResourceUtils.getResourceId("tips_title_info")));
            TextView textView2 = (TextView) this.mWindowContent.findViewById(HwPartResourceUtils.getResourceId("tips_info"));
            if (textView2 != null) {
                textView2.setText(this.mContext.getResources().getString(HwPartResourceUtils.getResourceId("tips_info_detail")));
                if (HwDisplaySizeUtilEx.hasSideInScreen()) {
                    updateSettingContainer();
                    updateTextContainer();
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void updateConfiguration() {
        Configuration newConfiguration = this.mContext.getResources().getConfiguration();
        int diff = this.mConfiguration.diff(newConfiguration);
        this.mConfiguration = newConfiguration;
        if ((diff & 128) != 0) {
            quitSingleHand();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean updateDefaultDisplayInfo() {
        boolean isValue = DisplayEx.getDisplayInfo(this.mWindowManager.getDefaultDisplay(), this.mDefaultDisplayInfo);
        this.mIsNeedRelayout = false;
        if (!isValue) {
            SlogEx.w(TAG, "there is no default display");
            return false;
        }
        DisplayInfoEx displayInfoEx = this.mPreDisplayInfo;
        if (displayInfoEx == null) {
            return false;
        }
        if (!displayInfoEx.equals(this.mDefaultDisplayInfo)) {
            this.mWidthScale = ((float) this.mDefaultDisplayInfo.getLogicalWidth()) / ((float) this.mPreDisplayInfo.getLogicalWidth());
            this.mHeightScale = ((float) this.mDefaultDisplayInfo.getLogicalHeight()) / ((float) this.mPreDisplayInfo.getLogicalHeight());
            if (!(this.mDefaultDisplayInfo.getLogicalWidth() == this.mPreDisplayInfo.getLogicalWidth() && this.mDefaultDisplayInfo.getLogicalHeight() == this.mPreDisplayInfo.getLogicalHeight() && this.mDefaultDisplayInfo.getLogicalDensityDpi() == this.mPreDisplayInfo.getLogicalDensityDpi())) {
                this.mIsNeedRelayout = true;
            }
            this.mPreDisplayInfo.copyFrom(this.mDefaultDisplayInfo);
        }
        return true;
    }

    private void handleBlurpaper() {
        int quarterLogicalHeight = this.mDefaultDisplayInfo.getLogicalHeight() / QUARTER_DENOMINATOR;
        this.mRelateViewtop = (RelativeLayout) this.mWindowContent.findViewById(34603160);
        this.mLayoutParams = this.mRelateViewtop.getLayoutParams();
        ViewGroup.LayoutParams layoutParams = this.mLayoutParams;
        layoutParams.height = quarterLogicalHeight;
        this.mRelateViewtop.setLayoutParams(layoutParams);
    }

    private void createWindow() {
        this.mWindowContent = LayoutInflater.from(new ContextThemeWrapper(this.mContext, this.mContext.getResources().getIdentifier("androidhwext:style/Theme.Emui", null, null))).inflate(34013238, (ViewGroup) null);
        boolean z = true;
        if (!this.mIsAttachedToWindow) {
            this.mIsAttachedToWindow = true;
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.LOCALE_CHANGED");
            filter.addAction("android.intent.action.CONFIGURATION_CHANGED");
            this.mContext.registerReceiver(this.mIntentReceiver, filter, null, this.mHandler);
        }
        if (this.mName.contains(BLURPAPER)) {
            handleBlurpaper();
        }
        this.mWindowParams = new WindowManagerEx.LayoutParamsEx(new WindowManager.LayoutParams(2040));
        this.mWindowParams.getLayoutParams().flags |= 16778024;
        WindowManagerEx.LayoutParamsEx layoutParamsEx = this.mWindowParams;
        layoutParamsEx.addPrivateFlags(WindowManagerEx.LayoutParamsEx.getPrivateFlags(layoutParamsEx.getLayoutParams()) | WindowManagerEx.LayoutParamsEx.getPrivateFlagForceHardwareAccelerated());
        this.mWindowParams.getLayoutParams().alpha = 1.0f;
        this.mWindowParams.getLayoutParams().gravity = 8388659;
        this.mWindowParams.getLayoutParams().format = -3;
        this.mWindowParams.setDisplaySideMode(2);
        this.mWindowParams.addHwFlags(64);
        boolean hintShown = isSingleHandModeHintShown(SINGLE_HAND_MODE_HINT_SHOWN) || this.mIsShownOnce;
        if (this.mName.contains(BLURPAPER)) {
            this.mIsHintShowing = !hintShown;
            if (hintShown) {
                z = false;
            }
            showHint(z);
            this.mWidthScale = 1.0f;
            this.mHeightScale = 1.0f;
        }
        updateRootLayout();
        if (HwDisplaySizeUtilEx.hasSideInScreen()) {
            initMarginSize();
        }
        updateLocale();
    }

    private void updateRootLayout() {
        ViewGroup.MarginLayoutParams marginParams;
        RelativeLayout layout = (RelativeLayout) this.mWindowContent.findViewById(HwPartResourceUtils.getResourceId("hint_section_top"));
        int top = this.mContext.getApplicationContext().getResources().getDimensionPixelSize(HwPartResourceUtils.getResourceId("status_bar_height"));
        ViewGroup.LayoutParams lp = layout.getLayoutParams();
        if (lp instanceof ViewGroup.MarginLayoutParams) {
            marginParams = (ViewGroup.MarginLayoutParams) lp;
        } else {
            marginParams = new ViewGroup.MarginLayoutParams(lp);
        }
        marginParams.setMargins(0, top, -2, -2);
        layout.setLayoutParams(marginParams);
    }

    private void updateWindowParams() {
        this.mWindowParams.getLayoutParams().x = 0;
        this.mWindowParams.getLayoutParams().y = 0;
        this.mWindowParams.getLayoutParams().width = this.mDefaultDisplayInfo.getLogicalWidth();
        this.mWindowParams.getLayoutParams().height = this.mDefaultDisplayInfo.getLogicalHeight();
        this.mWidth = (int) (((float) this.mWidth) * this.mWidthScale);
        this.mHeight = (int) (((float) this.mHeight) * this.mHeightScale);
    }

    /* access modifiers changed from: package-private */
    public boolean isSingleHandModeHintShown(String tag) {
        String value = Settings.Global.getString(this.mContext.getContentResolver(), tag);
        SlogEx.d(TAG, "tag " + tag + "value " + value);
        if (value == null || !value.equals(YES)) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showHint(boolean isVisible) {
        ImageView imageView;
        this.mImageView = (ImageView) this.mWindowContent.findViewById(34603162);
        if (!isVisible && (imageView = this.mImageView) != null) {
            imageView.setOnClickListener(this.mActionClickListener);
        }
        show(this.mImageView, !isVisible);
        if (!isVisible) {
            this.mWindowContent.setBackgroundColor(0);
        } else {
            this.mWindowContent.setBackgroundColor(ALP_BLACK_COLOR);
        }
        handleTipsInfoView(!isVisible);
        showGuideView(isVisible);
        setWindowTitle(isVisible);
        flushWallPaper();
        if (this.mIsWindowVisible) {
            this.mWindowManager.updateViewLayout(this.mWindowContent, this.mWindowParams.getLayoutParams());
        }
    }

    private void setWindowTitle(boolean isVis) {
        if (isVis) {
            this.mIsHintShowing = true;
            this.mWindowParams.getLayoutParams().setTitle("hwSingleMode_windowbg_hint");
        } else {
            this.mIsHintShowing = false;
            if (this.mIsLeft) {
                this.mWindowParams.getLayoutParams().setTitle("hwSingleMode_windowbg_left");
            } else {
                this.mWindowParams.getLayoutParams().setTitle("hwSingleMode_windowbg_right");
            }
        }
        WindowManagerServiceEx windowManagerServiceEx = this.mService;
        windowManagerServiceEx.setLazyMode(windowManagerServiceEx.getLazyMode(), this.mIsHintShowing, this.mName);
    }

    /* access modifiers changed from: package-private */
    public boolean singlehandRegionContainsPoint(int x, int y) {
        int right;
        int left;
        int top = (int) (((float) this.mDefaultDisplayInfo.getLogicalHeight()) * 0.25f);
        int bottom = this.mDefaultDisplayInfo.getLogicalHeight();
        if (this.mIsLeft) {
            left = 0;
            right = (int) (((float) this.mDefaultDisplayInfo.getLogicalWidth()) * INITIAL_SCALE);
        } else {
            left = (int) (((float) this.mDefaultDisplayInfo.getLogicalWidth()) * 0.25f);
            right = this.mDefaultDisplayInfo.getLogicalWidth();
        }
        if (y < top || y >= bottom || x < left || x >= right) {
            return false;
        }
        return true;
    }

    private Bitmap updateBackGround() {
        String str;
        String str2;
        Bitmap newBitmap;
        float roundCorn;
        String str3;
        float f;
        float endWidth;
        float endWidth2;
        float rightWidth;
        int width = this.mDefaultDisplayInfo.getLogicalWidth();
        int height = this.mDefaultDisplayInfo.getLogicalHeight();
        Bitmap newBitmap2 = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        if (newBitmap2 == null) {
            SlogEx.e(TAG, "newBitmap is null");
            return null;
        }
        Canvas canvas = new Canvas(newBitmap2);
        canvas.drawColor(-16777216);
        Paint paint = new Paint();
        paint.reset();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeWidth(4.0f);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        paint.setAlpha(0);
        float roundCorn2 = SingleHandUtils.getDeviceRoundRadiusSize(this.mContext.getResources().getDisplayMetrics()) * 0.6f;
        if (this.mIsLeft) {
            str3 = " [";
            roundCorn = roundCorn2;
            newBitmap = newBitmap2;
            f = 4.0f;
            canvas.drawRoundRect(2.0f, (((float) height) * 0.25f) + 2.0f, (((float) width) * INITIAL_SCALE) - 2.0f, ((float) height) - 2.0f, roundCorn2, roundCorn2, paint);
            str = " ,";
            str2 = "]";
        } else {
            newBitmap = newBitmap2;
            str3 = " [";
            f = 4.0f;
            float rightWidth2 = (((float) width) * 0.25f) + 2.0f;
            float endWidth3 = ((float) width) - 2.0f;
            if (HwDisplaySizeUtilEx.hasSideInScreen()) {
                rightWidth = rightWidth2 - ((float) (HwDisplaySizeUtilEx.getInstance(this.mService).getSafeSideWidth() * 2));
                endWidth2 = endWidth3 - ((float) (HwDisplaySizeUtilEx.getInstance(this.mService).getSafeSideWidth() * 2));
            } else {
                rightWidth = rightWidth2;
                endWidth2 = endWidth3;
            }
            SlogEx.i(TAG, "roundCorn1: " + roundCorn2 + str3 + rightWidth + " ," + width + "]");
            str2 = "]";
            str = " ,";
            roundCorn = roundCorn2;
            canvas.drawRoundRect(rightWidth, (((float) height) * 0.25f) + 2.0f, endWidth2, ((float) height) - 2.0f, roundCorn2, roundCorn2, paint);
        }
        paint.reset();
        paint.setAntiAlias(true);
        paint.setARGB(ALPHA_VALUE, SLIDE_COLOR, SLIDE_COLOR, SLIDE_COLOR);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(f);
        if (this.mIsLeft) {
            canvas.drawRoundRect(2.0f, (((float) height) * 0.25f) + 2.0f, (((float) width) * INITIAL_SCALE) - 2.0f, ((float) height) - 2.0f, roundCorn, roundCorn, paint);
        } else {
            float rightWidth3 = (((float) width) * 0.25f) + 2.0f;
            float endWidth4 = ((float) width) - 2.0f;
            if (HwDisplaySizeUtilEx.hasSideInScreen()) {
                rightWidth3 -= (float) (HwDisplaySizeUtilEx.getInstance(this.mService).getSafeSideWidth() * 2);
                endWidth = endWidth4 - ((float) (HwDisplaySizeUtilEx.getInstance(this.mService).getSafeSideWidth() * 2));
            } else {
                endWidth = endWidth4;
            }
            SlogEx.i(TAG, "roundCorn0: " + roundCorn + str3 + rightWidth3 + str + width + str2);
            canvas.drawRoundRect(rightWidth3, (((float) height) * 0.25f) + 2.0f, endWidth, ((float) height) - 2.0f, roundCorn, roundCorn, paint);
        }
        return newBitmap;
    }

    private void flushWallPaper() {
        LinearLayout layout;
        this.mWindowContent.setBackground(new BitmapDrawable(updateBackGround()));
        if (this.mIsHintShowing && (layout = (LinearLayout) this.mWindowContent.findViewById(HwPartResourceUtils.getResourceId("freshman_layout"))) != null && layout.getVisibility() == 0) {
            layout.setBackgroundColor(ALP_BLACK_COLOR1);
        }
    }

    private void showGuideView(boolean isVisible) {
        if (isVisible) {
            this.mIsShownOnce = true;
        }
        LinearLayout linearLayout = (LinearLayout) this.mWindowContent.findViewById(HwPartResourceUtils.getResourceId("freshman_layout"));
        show(linearLayout, isVisible);
        show((LinearLayout) this.mWindowContent.findViewById(HwPartResourceUtils.getResourceId("freshman_layout_text")), isVisible);
        SingleHandUtils.showFreshmanGuide(this.mWindowContent, isVisible, this.mContext);
        ImageView imageView = (ImageView) linearLayout.findViewById(HwPartResourceUtils.getResourceId("freshman_guide_image_id"));
        if (imageView != null) {
            if (SingleHandUtils.getCurrentEnteringMode(this.mContext.getContentResolver()) == 2) {
                imageView.setImageResource(HwPartResourceUtils.getResourceId("navigator_guide"));
            } else {
                imageView.setImageResource(HwPartResourceUtils.getResourceId("gesture_guide"));
            }
            imageView.setMaxHeight(this.mDefaultDisplayInfo.getLogicalHeight() / 2);
            show(imageView, isVisible);
            LinearLayout relativeLayout = (LinearLayout) linearLayout.findViewById(HwPartResourceUtils.getResourceId("freshman_layout_button"));
            if (relativeLayout != null) {
                show(relativeLayout, isVisible);
                Button button = (Button) relativeLayout.findViewById(HwPartResourceUtils.getResourceId("confirm_info"));
                if (button != null) {
                    button.setMinWidth(this.mDefaultDisplayInfo.getLogicalWidth() / 2);
                    button.setText(this.mContext.getResources().getString(HwPartResourceUtils.getResourceId("freshman_guide_button_info")));
                    if (isVisible) {
                        button.setOnClickListener(this.mConfirmClickListener);
                    }
                    show(button, isVisible);
                }
            }
        }
    }

    private void handleTipsInfoView(boolean isVisible) {
        TextView textView = (TextView) this.mWindowContent.findViewById(HwPartResourceUtils.getResourceId("overlay_display_window_title"));
        if (textView != null) {
            textView.setText(this.mContext.getResources().getString(HwPartResourceUtils.getResourceId("tips_title_info")));
            show(textView, isVisible);
            TextView textView2 = (TextView) this.mWindowContent.findViewById(HwPartResourceUtils.getResourceId("tips_info"));
            if (textView2 != null) {
                textView2.setText(this.mContext.getResources().getString(HwPartResourceUtils.getResourceId("tips_info_detail")));
                show(textView2, isVisible);
            }
        }
    }

    private void updateTextContainer() {
        LinearLayout linearLayout = (LinearLayout) this.mWindowContent.findViewById(HwPartResourceUtils.getResourceId("hint_section_text"));
        ViewGroup.LayoutParams layoutParams = linearLayout.getLayoutParams();
        if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) layoutParams;
            if (SingleHandUtils.isMirrorLanguage()) {
                marginParams.setMarginStart(this.mTipsMarginStart + (HwDisplaySizeUtilEx.getInstance(this.mService).getSafeSideWidth() * 2));
                marginParams.setMarginEnd(this.mTipsMarginEnd);
            } else {
                marginParams.setMarginStart(this.mTipsMarginStart);
                marginParams.setMarginEnd(this.mTipsMarginEnd + (HwDisplaySizeUtilEx.getInstance(this.mService).getSafeSideWidth() * 2));
            }
            linearLayout.setLayoutParams(marginParams);
        }
    }

    private void updateSettingContainer() {
        LinearLayout linearLayout = (LinearLayout) this.mWindowContent.findViewById(HwPartResourceUtils.getResourceId("hint_setting_container"));
        ViewGroup.LayoutParams layoutParams = linearLayout.getLayoutParams();
        if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) layoutParams;
            if (!SingleHandUtils.isMirrorLanguage()) {
                marginParams.setMarginEnd(this.mSettingMarginEnd + (HwDisplaySizeUtilEx.getInstance(this.mService).getSafeSideWidth() * 2));
            } else {
                marginParams.setMarginEnd(this.mSettingMarginEnd);
            }
            linearLayout.setLayoutParams(marginParams);
        }
    }

    private void initMarginSize() {
        ViewGroup.LayoutParams layoutParams = ((LinearLayout) this.mWindowContent.findViewById(HwPartResourceUtils.getResourceId("hint_section_text"))).getLayoutParams();
        if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) layoutParams;
            this.mTipsMarginStart = marginParams.getMarginStart();
            this.mTipsMarginEnd = marginParams.getMarginEnd();
            ViewGroup.LayoutParams layoutParams2 = ((LinearLayout) this.mWindowContent.findViewById(HwPartResourceUtils.getResourceId("hint_setting_container"))).getLayoutParams();
            if (layoutParams2 instanceof ViewGroup.MarginLayoutParams) {
                this.mSettingMarginEnd = ((ViewGroup.MarginLayoutParams) layoutParams2).getMarginEnd();
            }
        }
    }

    private boolean isQuickMode() {
        String type = Settings.Global.getString(this.mContext.getContentResolver(), QUICK_SWITCH_SINGLE_HAND_TYPE);
        SlogEx.i(TAG, "type: " + type);
        if ("app".equals(type)) {
            return true;
        }
        return false;
    }
}
