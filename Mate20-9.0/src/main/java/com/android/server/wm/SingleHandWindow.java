package com.android.server.wm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.display.DisplayManager;
import android.os.Handler;
import android.provider.Settings;
import android.util.Slog;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.server.hidata.arbitration.HwArbitrationDEFS;
import huawei.android.provider.FrontFingerPrintSettings;

final class SingleHandWindow {
    private static final boolean DEBUG = false;
    private static final String HINT_INFO_TAG = "hint_info";
    private static final float INITIAL_SCALE = 0.75f;
    private static final float MAX_SCALE = 1.0f;
    private static final float MIN_SCALE = 0.3f;
    private static final String SINGLE_HAND_MODE_GUIDE_SHOWN = "single_hand_mode_guide_shown";
    private static final String SINGLE_HAND_MODE_HINT_SHOWN = "single_hand_mode_hint_shown";
    private static final String TAG = "SingleHandWindow";
    private static final float WINDOW_ALPHA = 1.0f;
    private static final String WINDOW_BG_TAG = "other_area";
    private static final String YES = "yes";
    private View.OnClickListener mActionClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            SingleHandWindow.this.showHint(true);
            Settings.Global.putString(SingleHandWindow.this.mContext.getContentResolver(), SingleHandWindow.SINGLE_HAND_MODE_GUIDE_SHOWN, SingleHandWindow.YES);
        }
    };
    private boolean mAttachedToWindow = false;
    private Configuration mConfiguration = new Configuration();
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    public final Display mDefaultDisplay;
    private DisplayInfo mDefaultDisplayInfo = new DisplayInfo();
    private final DisplayManager.DisplayListener mDisplayListener = new DisplayManager.DisplayListener() {
        public void onDisplayAdded(int displayId) {
        }

        public void onDisplayChanged(int displayId) {
            if (displayId != SingleHandWindow.this.mDefaultDisplay.getDisplayId()) {
                return;
            }
            if (!SingleHandWindow.this.updateDefaultDisplayInfo()) {
                SingleHandWindow.this.dismiss();
            } else if (SingleHandWindow.this.mIsNeedRelayout) {
                SingleHandWindow.this.relayout();
            }
        }

        public void onDisplayRemoved(int displayId) {
            if (displayId == SingleHandWindow.this.mDefaultDisplay.getDisplayId()) {
                SingleHandWindow.this.dismiss();
            }
        }
    };
    private final DisplayManager mDisplayManager;
    private Handler mHandler;
    private int mHeight;
    private float mHeightScale;
    /* access modifiers changed from: private */
    public ImageView mImageView;
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
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
    /* access modifiers changed from: private */
    public boolean mIsNeedRelayout = false;
    private ViewGroup.LayoutParams mLayoutParams;
    private final boolean mLeft;
    private final String mName;
    private final View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
        public boolean onTouch(View view, MotionEvent event) {
            boolean inRegion = SingleHandWindow.this.singlehandRegionContainsPoint((int) event.getRawX(), (int) event.getRawY());
            int actionMasked = event.getActionMasked();
            if (actionMasked != 3) {
                switch (actionMasked) {
                    case 0:
                        boolean unused = SingleHandWindow.this.mPointDownOuter = !inRegion;
                        break;
                    case 1:
                        ImageView imageView = (ImageView) SingleHandWindow.this.mWindowContent.findViewById(34603162);
                        if (imageView == null || imageView.getVisibility() != 0) {
                            SingleHandWindow.this.showHint(false);
                            Settings.Global.putString(SingleHandWindow.this.mContext.getContentResolver(), SingleHandWindow.SINGLE_HAND_MODE_HINT_SHOWN, SingleHandWindow.YES);
                        } else if (!inRegion && SingleHandWindow.this.mPointDownOuter) {
                            Settings.Global.putString(SingleHandWindow.this.mContext.getContentResolver(), "single_hand_mode", "");
                        }
                        boolean unused2 = SingleHandWindow.this.mPointDownOuter = false;
                        break;
                }
            } else {
                boolean unused3 = SingleHandWindow.this.mPointDownOuter = false;
            }
            return true;
        }
    };
    /* access modifiers changed from: private */
    public boolean mPointDownOuter = false;
    private DisplayInfo mPreDisplayInfo = new DisplayInfo();
    private RelativeLayout mRelateViewbottom;
    private RelativeLayout mRelateViewtop;
    /* access modifiers changed from: private */
    public final WindowManagerService mService;
    private int mWidth;
    private float mWidthScale;
    /* access modifiers changed from: private */
    public View mWindowContent;
    private final WindowManager mWindowManager;
    private WindowManager.LayoutParams mWindowParams;
    private boolean mWindowVisible;
    private TextView overlay_display_window = null;
    private TextView overlay_guide_window = null;
    private TextView singlehandmode_slide_hint_text_1 = null;
    private TextView singlehandmode_slide_hint_text_2 = null;
    private TextView singlehandmode_slide_hint_text_fp_navi = null;

    public SingleHandWindow(Context context, boolean left, String name, int width, int height, WindowManagerService service) {
        this.mContext = context;
        this.mName = name;
        this.mWidth = width;
        this.mHeight = height;
        this.mLeft = left;
        this.mHandler = new Handler();
        this.mWindowManager = (WindowManager) context.getSystemService("window");
        this.mDisplayManager = (DisplayManager) context.getSystemService("display");
        this.mService = service;
        this.mDefaultDisplay = this.mWindowManager.getDefaultDisplay();
        this.mDefaultDisplayInfo = this.mService.getDefaultDisplayContentLocked().getDisplayInfo();
        this.mConfiguration = this.mContext.getResources().getConfiguration();
        this.mPreDisplayInfo.copyFrom(this.mDefaultDisplayInfo);
        if (this.mName.contains("blurpaper")) {
            createWindow();
        }
    }

    public void show() {
        if (!this.mWindowVisible) {
            if (!this.mName.contains("blurpaper")) {
                this.mService.freezeOrThawRotation(0);
                this.mService.setLazyMode(this.mLeft ? 1 : 2);
            }
            this.mDisplayManager.registerDisplayListener(this.mDisplayListener, null);
            if (!updateDefaultDisplayInfo()) {
                this.mDisplayManager.unregisterDisplayListener(this.mDisplayListener);
                return;
            }
            if (this.mName.equals("blurpapertop")) {
                this.mWindowParams.x = 0;
                this.mWindowParams.y = 0;
                this.mWindowParams.width = this.mWidth;
                this.mWindowParams.height = this.mHeight;
                this.mWindowContent.setOnTouchListener(this.mOnTouchListener);
                this.mWindowManager.addView(this.mWindowContent, this.mWindowParams);
            }
            this.mWindowVisible = true;
        }
    }

    public void dismiss() {
        if (this.mAttachedToWindow) {
            this.mAttachedToWindow = false;
            this.mContext.unregisterReceiver(this.mIntentReceiver);
        }
        if (this.mWindowVisible) {
            if (updateDefaultDisplayInfo()) {
                this.mDisplayManager.unregisterDisplayListener(this.mDisplayListener);
            }
            if (!this.mName.contains("blurpaper")) {
                this.mHandler.postDelayed(new Runnable() {
                    public void run() {
                        SingleHandWindow.this.mService.freezeOrThawRotation(-1);
                    }
                }, 100);
                this.mService.setLazyMode(0);
            } else {
                this.mWindowManager.removeView(this.mWindowContent);
            }
            this.mWindowVisible = false;
        }
    }

    public void relayout() {
        if (this.mWindowVisible && this.mName.contains("blurpaper")) {
            this.mWindowManager.removeView(this.mWindowContent);
            this.mWindowVisible = false;
            createWindow();
            updateWindowParams();
            this.mWindowContent.setOnTouchListener(this.mOnTouchListener);
            this.mWindowManager.addView(this.mWindowContent, this.mWindowParams);
            this.mWindowVisible = true;
        }
    }

    private Bitmap cropwallpaper(boolean isTop) {
        Bitmap crop;
        if (SingleHandAdapter.scaleWallpaper == null) {
            Slog.e(TAG, "scaleWallpaper null.");
            return null;
        }
        int w = SingleHandAdapter.scaleWallpaper.getWidth();
        int h = SingleHandAdapter.scaleWallpaper.getHeight();
        if (isTop) {
            crop = Bitmap.createBitmap(SingleHandAdapter.scaleWallpaper, 0, 0, w, (int) (((float) h) * 0.25f));
        } else if (this.mLeft) {
            crop = Bitmap.createBitmap(SingleHandAdapter.scaleWallpaper, (int) (((float) w) * 0.75f), (int) (((float) h) * 0.25f), (int) (((float) w) - (((float) w) * 0.75f)), (int) (((float) h) * 0.75f));
        } else {
            crop = Bitmap.createBitmap(SingleHandAdapter.scaleWallpaper, 0, (int) (((float) h) * 0.25f), (int) (((float) w) - (((float) w) * 0.75f)), (int) (((float) h) * 0.75f));
        }
        return crop;
    }

    /* access modifiers changed from: package-private */
    public void updateLocale() {
        Slog.d(TAG, "updateLocale .");
        if (this.overlay_display_window != null) {
            this.overlay_display_window.setText(this.mContext.getResources().getString(33685760));
        }
        if (this.overlay_guide_window != null) {
            this.overlay_guide_window.setText(this.mContext.getResources().getString(33686224));
            this.overlay_guide_window.setBackgroundDrawable(this.mContext.getResources().getDrawable(33752058));
        }
        if (this.singlehandmode_slide_hint_text_1 != null) {
            this.singlehandmode_slide_hint_text_1.setText(this.mContext.getResources().getString(33686225, new Object[]{1}));
        }
        if (this.singlehandmode_slide_hint_text_2 != null) {
            this.singlehandmode_slide_hint_text_2.setText(this.mContext.getResources().getString(33686226, new Object[]{2}));
        }
        if (this.singlehandmode_slide_hint_text_fp_navi != null) {
            this.singlehandmode_slide_hint_text_fp_navi.setText(this.mContext.getResources().getString(33686229));
        }
    }

    /* access modifiers changed from: package-private */
    public void updateConfiguration() {
        Configuration newConfiguration = this.mContext.getResources().getConfiguration();
        int diff = this.mConfiguration.diff(newConfiguration);
        this.mConfiguration = newConfiguration;
        if ((diff & 128) != 0) {
            Settings.Global.putString(this.mContext.getContentResolver(), "single_hand_mode", "");
        }
    }

    /* access modifiers changed from: private */
    public boolean updateDefaultDisplayInfo() {
        this.mIsNeedRelayout = false;
        if (!this.mDefaultDisplay.getDisplayInfo(this.mDefaultDisplayInfo)) {
            Slog.w(TAG, "Cannot show overlay display because there is no default display upon which to show it.");
            return false;
        } else if (this.mPreDisplayInfo == null) {
            return false;
        } else {
            if (!this.mPreDisplayInfo.equals(this.mDefaultDisplayInfo)) {
                this.mWidthScale = ((float) this.mDefaultDisplayInfo.logicalWidth) / ((float) this.mPreDisplayInfo.logicalWidth);
                this.mHeightScale = ((float) this.mDefaultDisplayInfo.logicalHeight) / ((float) this.mPreDisplayInfo.logicalHeight);
                if (!(this.mDefaultDisplayInfo.logicalWidth == this.mPreDisplayInfo.logicalWidth && this.mDefaultDisplayInfo.logicalHeight == this.mPreDisplayInfo.logicalHeight && this.mDefaultDisplayInfo.logicalDensityDpi == this.mPreDisplayInfo.logicalDensityDpi)) {
                    this.mIsNeedRelayout = true;
                }
                this.mPreDisplayInfo.copyFrom(this.mDefaultDisplayInfo);
            }
            return true;
        }
    }

    public void updateLayoutParams() {
        int quarterLogicalHeight = this.mDefaultDisplayInfo.logicalHeight / 4;
        this.mLayoutParams = this.mRelateViewtop.getLayoutParams();
        this.mLayoutParams.height = quarterLogicalHeight;
        this.mRelateViewtop.setLayoutParams(this.mLayoutParams);
        this.mLayoutParams = this.mRelateViewbottom.getLayoutParams();
        this.mLayoutParams.height = this.mDefaultDisplayInfo.logicalHeight - quarterLogicalHeight;
        this.mLayoutParams.width = this.mDefaultDisplayInfo.logicalWidth / 4;
        if (this.mLeft) {
            ((RelativeLayout.LayoutParams) this.mLayoutParams).addRule(11);
        } else {
            ((RelativeLayout.LayoutParams) this.mLayoutParams).addRule(9);
        }
        ((RelativeLayout.LayoutParams) this.mLayoutParams).addRule(12);
        this.mRelateViewbottom.setLayoutParams(this.mLayoutParams);
    }

    private void createWindow() {
        ViewGroup.MarginLayoutParams marginParams;
        this.mWindowContent = LayoutInflater.from(this.mContext).inflate(34013238, null);
        boolean hintShown = isSingleHandModeHintShown(SINGLE_HAND_MODE_HINT_SHOWN);
        if (!this.mAttachedToWindow) {
            this.mAttachedToWindow = true;
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.LOCALE_CHANGED");
            filter.addAction("android.intent.action.CONFIGURATION_CHANGED");
            this.mContext.registerReceiver(this.mIntentReceiver, filter, null, this.mHandler);
        }
        if (this.mName.contains("blurpaper")) {
            synchronized (SingleHandAdapter.mLock) {
                this.mWindowContent.setBackgroundColor(0);
                int quarterLogicalHeight = this.mDefaultDisplayInfo.logicalHeight / 4;
                this.mRelateViewtop = (RelativeLayout) this.mWindowContent.findViewById(34603160);
                this.mLayoutParams = this.mRelateViewtop.getLayoutParams();
                this.mLayoutParams.height = quarterLogicalHeight;
                this.mRelateViewtop.setLayoutParams(this.mLayoutParams);
                this.mRelateViewtop.setBackground(new BitmapDrawable(this.mRelateViewtop.getResources(), cropwallpaper(true)));
                this.mRelateViewbottom = (RelativeLayout) this.mWindowContent.findViewById(34603161);
                this.mLayoutParams = this.mRelateViewbottom.getLayoutParams();
                this.mLayoutParams.height = this.mDefaultDisplayInfo.logicalHeight - quarterLogicalHeight;
                this.mLayoutParams.width = this.mDefaultDisplayInfo.logicalWidth / 4;
                if (this.mLeft) {
                    ((RelativeLayout.LayoutParams) this.mLayoutParams).addRule(11);
                } else {
                    ((RelativeLayout.LayoutParams) this.mLayoutParams).addRule(9);
                }
                ((RelativeLayout.LayoutParams) this.mLayoutParams).addRule(12);
                this.mRelateViewbottom.setLayoutParams(this.mLayoutParams);
                this.mRelateViewbottom.setBackground(new BitmapDrawable(this.mRelateViewbottom.getResources(), cropwallpaper(false)));
            }
        }
        this.mWindowParams = new WindowManager.LayoutParams(HwArbitrationDEFS.MSG_HISTREAM_TRIGGER_MPPLINK_INTERNAL);
        this.mWindowParams.flags |= 16778024;
        this.mWindowParams.privateFlags |= 2;
        this.mWindowParams.alpha = 1.0f;
        this.mWindowParams.gravity = 51;
        this.mWindowParams.format = -3;
        if (this.mName.contains("blurpaper")) {
            showHint(!hintShown);
            this.mWidthScale = 1.0f;
            this.mHeightScale = 1.0f;
        }
        RelativeLayout layout = (RelativeLayout) this.mWindowContent.findViewById(34603143);
        int top = this.mContext.getApplicationContext().getResources().getDimensionPixelSize(17105318);
        ViewGroup.LayoutParams LayoutParam = layout.getLayoutParams();
        if (LayoutParam instanceof ViewGroup.MarginLayoutParams) {
            marginParams = (ViewGroup.MarginLayoutParams) LayoutParam;
        } else {
            marginParams = new ViewGroup.MarginLayoutParams(LayoutParam);
        }
        marginParams.setMargins(0, top, -2, -2);
        layout.setLayoutParams(marginParams);
        updateLocale();
    }

    private void updateWindowParams() {
        this.mWindowParams.x = 0;
        this.mWindowParams.y = 0;
        this.mWindowParams.width = this.mDefaultDisplayInfo.logicalWidth;
        this.mWindowParams.height = this.mDefaultDisplayInfo.logicalHeight;
        this.mWidth = (int) (((float) this.mWidth) * this.mWidthScale);
        this.mHeight = (int) (((float) this.mHeight) * this.mHeightScale);
    }

    /* access modifiers changed from: package-private */
    public boolean isSingleHandModeHintShown(String tag) {
        String value = Settings.Global.getString(this.mContext.getContentResolver(), tag);
        Slog.d(TAG, "tag " + tag + "value " + value);
        if (value == null || !value.equals(YES)) {
            return false;
        }
        return true;
    }

    private void show(View v, boolean visible) {
        if (v == null) {
            return;
        }
        if (visible) {
            v.setVisibility(0);
        } else {
            v.setVisibility(4);
        }
    }

    /* access modifiers changed from: private */
    public void showHint(boolean visible) {
        this.mImageView = (ImageView) this.mWindowContent.findViewById(34603162);
        ((FrameLayout) this.mWindowContent.findViewById(34603163)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SingleHandWindow.this.mImageView.performClick();
            }
        });
        if (!visible && this.mImageView != null) {
            this.mImageView.setOnClickListener(this.mActionClickListener);
        }
        show(this.mImageView, !visible);
        int i = 0;
        if (!visible) {
            this.mWindowContent.setBackgroundColor(0);
        } else {
            this.mWindowContent.setBackgroundColor(-1728053248);
        }
        this.overlay_display_window = (TextView) this.mWindowContent.findViewById(34603164);
        this.overlay_guide_window = (TextView) this.mWindowContent.findViewById(34603353);
        if (!isSingleHandModeHintShown(SINGLE_HAND_MODE_GUIDE_SHOWN)) {
            show(this.overlay_guide_window, !visible);
        } else {
            show(this.overlay_guide_window, false);
        }
        show(this.overlay_display_window, !visible);
        boolean fpTrikeyNaviVisiable = true;
        boolean virtualNaviVisiable = visible && !isFpTrikeyDevice();
        if (!visible || !isFpTrikeyDevice()) {
            fpTrikeyNaviVisiable = false;
        }
        this.singlehandmode_slide_hint_text_1 = (TextView) this.mWindowContent.findViewById(34603397);
        show(this.singlehandmode_slide_hint_text_1, virtualNaviVisiable);
        this.singlehandmode_slide_hint_text_2 = (TextView) this.mWindowContent.findViewById(34603398);
        show(this.singlehandmode_slide_hint_text_2, virtualNaviVisiable);
        this.singlehandmode_slide_hint_text_fp_navi = (TextView) this.mWindowContent.findViewById(34603399);
        show(this.singlehandmode_slide_hint_text_fp_navi, fpTrikeyNaviVisiable);
        LinearLayout viewSlideHint = (LinearLayout) this.mWindowContent.findViewById(34603166);
        if (viewSlideHint != null) {
            if (visible) {
                viewSlideHint.setVisibility(virtualNaviVisiable ? 0 : 8);
            } else {
                viewSlideHint.setVisibility(4);
            }
        }
        LinearLayout viewSlideHintFpNavi = (LinearLayout) this.mWindowContent.findViewById(34603403);
        if (viewSlideHintFpNavi != null) {
            if (visible) {
                if (!fpTrikeyNaviVisiable) {
                    i = 8;
                }
                viewSlideHintFpNavi.setVisibility(i);
            } else {
                viewSlideHintFpNavi.setVisibility(4);
            }
        }
        ImageView imageView = (ImageView) this.mWindowContent.findViewById(34603400);
        if (imageView != null) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) imageView.getLayoutParams();
            params.width = (this.mDefaultDisplayInfo.logicalWidth * 4) / 9;
            params.height = params.width;
            imageView.setLayoutParams(params);
        }
        show(imageView, virtualNaviVisiable);
        ImageView imageView2 = (ImageView) this.mWindowContent.findViewById(34603401);
        if (imageView2 != null) {
            LinearLayout.LayoutParams params2 = (LinearLayout.LayoutParams) imageView2.getLayoutParams();
            params2.width = (this.mDefaultDisplayInfo.logicalWidth * 4) / 9;
            params2.height = params2.width;
            imageView2.setLayoutParams(params2);
        }
        show(imageView2, virtualNaviVisiable);
        show((ImageView) this.mWindowContent.findViewById(34603402), fpTrikeyNaviVisiable);
        setWindowTitle(visible);
        if (this.mWindowVisible) {
            this.mWindowManager.updateViewLayout(this.mWindowContent, this.mWindowParams);
        }
    }

    private void setWindowTitle(boolean visible) {
        if (visible) {
            this.mWindowParams.setTitle("hwSingleMode_windowbg_hint");
        } else if (this.mLeft) {
            this.mWindowParams.setTitle("hwSingleMode_windowbg_left");
        } else {
            this.mWindowParams.setTitle("hwSingleMode_windowbg_right");
        }
    }

    /* access modifiers changed from: package-private */
    public boolean singlehandRegionContainsPoint(int x, int y) {
        int right;
        int left;
        int top = (int) (((float) this.mDefaultDisplayInfo.logicalHeight) * 0.25f);
        int bottom = this.mDefaultDisplayInfo.logicalHeight;
        if (this.mLeft) {
            left = 0;
            right = (int) (((float) this.mDefaultDisplayInfo.logicalWidth) * 0.75f);
        } else {
            left = (int) (((float) this.mDefaultDisplayInfo.logicalWidth) * 0.25f);
            right = this.mDefaultDisplayInfo.logicalWidth;
        }
        if (y < top || y >= bottom || x < left || x >= right) {
            return false;
        }
        return true;
    }

    public void onBlurWallpaperChanged() {
        this.mRelateViewtop = (RelativeLayout) this.mWindowContent.findViewById(34603160);
        this.mRelateViewtop.setBackground(new BitmapDrawable(this.mRelateViewtop.getResources(), cropwallpaper(true)));
        this.mRelateViewbottom = (RelativeLayout) this.mWindowContent.findViewById(34603161);
        this.mRelateViewbottom.setBackground(new BitmapDrawable(this.mRelateViewbottom.getResources(), cropwallpaper(false)));
    }

    private boolean isFpTrikeyDevice() {
        return FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION && FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION_TRIKEY == 1;
    }
}
