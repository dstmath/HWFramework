package com.android.server.wm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.display.DisplayManager;
import android.hardware.display.DisplayManager.DisplayListener;
import android.os.Handler;
import android.provider.Settings.Global;
import android.util.Slog;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import huawei.android.provider.FrontFingerPrintSettings;

final class SingleHandWindow {
    private static final boolean DEBUG = false;
    private static final String HINT_INFO_TAG = "hint_info";
    private static final float INITIAL_SCALE = 0.75f;
    private static final float MAX_SCALE = 1.0f;
    private static final float MIN_SCALE = 0.3f;
    private static final String SINGLE_HAND_MODE_HINT_SHOWN = "single_hand_mode_hint_shown";
    private static final String TAG = "SingleHandWindow";
    private static final float WINDOW_ALPHA = 1.0f;
    private static final String WINDOW_BG_TAG = "other_area";
    private static final String YES = "yes";
    private OnClickListener mActionClickListener = new OnClickListener() {
        public void onClick(View v) {
            SingleHandWindow.this.showHint(true);
        }
    };
    private boolean mAttachedToWindow = false;
    private Configuration mConfiguration = new Configuration();
    private final Context mContext;
    private final Display mDefaultDisplay;
    private DisplayInfo mDefaultDisplayInfo = new DisplayInfo();
    private final DisplayListener mDisplayListener = new DisplayListener() {
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
    private ImageView mImageView;
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
    private boolean mIsNeedRelayout = false;
    private LayoutParams mLayoutParams;
    private final boolean mLeft;
    private final String mName;
    private final OnTouchListener mOnTouchListener = new OnTouchListener() {
        public boolean onTouch(View view, MotionEvent event) {
            boolean inRegion = SingleHandWindow.this.singlehandRegionContainsPoint((int) event.getRawX(), (int) event.getRawY());
            switch (event.getActionMasked()) {
                case 0:
                    SingleHandWindow.this.mPointDownOuter = inRegion ^ 1;
                    break;
                case 1:
                    ImageView imageView = (ImageView) SingleHandWindow.this.mWindowContent.findViewById(34603162);
                    if (imageView == null || imageView.getVisibility() != 0) {
                        SingleHandWindow.this.showHint(false);
                        Global.putString(SingleHandWindow.this.mContext.getContentResolver(), SingleHandWindow.SINGLE_HAND_MODE_HINT_SHOWN, SingleHandWindow.YES);
                    } else if (!inRegion && SingleHandWindow.this.mPointDownOuter) {
                        Global.putString(SingleHandWindow.this.mContext.getContentResolver(), "single_hand_mode", "");
                    }
                    SingleHandWindow.this.mPointDownOuter = false;
                    break;
                case 3:
                    SingleHandWindow.this.mPointDownOuter = false;
                    break;
            }
            return true;
        }
    };
    private boolean mPointDownOuter = false;
    private DisplayInfo mPreDisplayInfo = new DisplayInfo();
    private RelativeLayout mRelateViewbottom;
    private RelativeLayout mRelateViewtop;
    private final WindowManagerService mService;
    private int mWidth;
    private float mWidthScale;
    private View mWindowContent;
    private final WindowManager mWindowManager;
    private WindowManager.LayoutParams mWindowParams;
    private boolean mWindowVisible;
    private TextView overlay_display_window = null;
    private TextView singlehandmode_slide_hint = null;

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
            if (updateDefaultDisplayInfo()) {
                if (this.mName.equals("blurpapertop")) {
                    this.mWindowParams.x = 0;
                    this.mWindowParams.y = 0;
                    this.mWindowParams.width = this.mWidth;
                    this.mWindowParams.height = this.mHeight;
                    this.mWindowContent.setOnTouchListener(this.mOnTouchListener);
                    this.mWindowManager.addView(this.mWindowContent, this.mWindowParams);
                }
                this.mWindowVisible = true;
            } else {
                this.mDisplayManager.unregisterDisplayListener(this.mDisplayListener);
            }
        }
    }

    public void dismiss() {
        if (this.mAttachedToWindow) {
            this.mAttachedToWindow = false;
            this.mContext.unregisterReceiver(this.mIntentReceiver);
        }
        if (this.mWindowVisible) {
            if (updateDefaultDisplayInfo()) {
                if (this.mName.contains("blurpaper")) {
                    this.mWindowManager.removeView(this.mWindowContent);
                } else {
                    this.mHandler.postDelayed(new Runnable() {
                        public void run() {
                            SingleHandWindow.this.mService.freezeOrThawRotation(-1);
                        }
                    }, 100);
                    this.mService.setLazyMode(0);
                }
                this.mWindowVisible = false;
            } else {
                this.mDisplayManager.unregisterDisplayListener(this.mDisplayListener);
            }
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
        if (SingleHandAdapter.scaleWallpaper == null) {
            Slog.e(TAG, "scaleWallpaper null.");
            return null;
        }
        Bitmap crop;
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

    void updateLocale() {
        Slog.d(TAG, "updateLocale .");
        if (this.overlay_display_window != null) {
            this.overlay_display_window.setText(this.mContext.getResources().getString(33685745));
        }
        if (this.singlehandmode_slide_hint == null) {
            return;
        }
        if (isFingerPrintHand()) {
            this.singlehandmode_slide_hint.setText(this.mContext.getResources().getString(33685925));
        } else {
            this.singlehandmode_slide_hint.setText(this.mContext.getResources().getString(33685746));
        }
    }

    void updateConfiguration() {
        Configuration newConfiguration = this.mContext.getResources().getConfiguration();
        int diff = this.mConfiguration.diff(newConfiguration);
        this.mConfiguration = newConfiguration;
        if ((diff & 128) != 0) {
            Global.putString(this.mContext.getContentResolver(), "single_hand_mode", "");
        }
    }

    private boolean updateDefaultDisplayInfo() {
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
        this.mLayoutParams = this.mRelateViewtop.getLayoutParams();
        this.mLayoutParams.height = this.mDefaultDisplayInfo.logicalHeight / 4;
        this.mRelateViewtop.setLayoutParams(this.mLayoutParams);
        this.mLayoutParams = this.mRelateViewbottom.getLayoutParams();
        this.mLayoutParams.height = (this.mDefaultDisplayInfo.logicalHeight * 3) / 4;
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
        MarginLayoutParams marginParams;
        LayoutInflater inflater = LayoutInflater.from(this.mContext);
        if (isFingerPrintHand()) {
            this.mWindowContent = inflater.inflate(34013239, null);
        } else {
            this.mWindowContent = inflater.inflate(34013238, null);
        }
        boolean hintShown = isSingleHandModeHintShown();
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
                this.mRelateViewtop = (RelativeLayout) this.mWindowContent.findViewById(34603160);
                this.mLayoutParams = this.mRelateViewtop.getLayoutParams();
                this.mLayoutParams.height = this.mDefaultDisplayInfo.logicalHeight / 4;
                this.mRelateViewtop.setLayoutParams(this.mLayoutParams);
                this.mRelateViewtop.setBackground(new BitmapDrawable(this.mRelateViewtop.getResources(), cropwallpaper(true)));
                this.mRelateViewbottom = (RelativeLayout) this.mWindowContent.findViewById(34603161);
                this.mLayoutParams = this.mRelateViewbottom.getLayoutParams();
                this.mLayoutParams.height = (this.mDefaultDisplayInfo.logicalHeight * 3) / 4;
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
        this.mWindowParams = new WindowManager.LayoutParams(2026);
        WindowManager.LayoutParams layoutParams = this.mWindowParams;
        layoutParams.flags |= 16778024;
        layoutParams = this.mWindowParams;
        layoutParams.privateFlags |= 2;
        this.mWindowParams.alpha = 1.0f;
        this.mWindowParams.gravity = 51;
        this.mWindowParams.format = -3;
        if (this.mName.contains("blurpaper")) {
            showHint(hintShown ^ 1);
            this.mWidthScale = 1.0f;
            this.mHeightScale = 1.0f;
        }
        RelativeLayout layout = (RelativeLayout) this.mWindowContent.findViewById(34603032);
        int top = this.mContext.getApplicationContext().getResources().getDimensionPixelSize(17105234);
        LayoutParams LayoutParam = layout.getLayoutParams();
        if (LayoutParam instanceof MarginLayoutParams) {
            marginParams = (MarginLayoutParams) LayoutParam;
        } else {
            marginParams = new MarginLayoutParams(LayoutParam);
        }
        marginParams.setMargins(0, top, -2, -2);
        layout.setLayoutParams(marginParams);
    }

    private void updateWindowParams() {
        this.mWindowParams.x = 0;
        this.mWindowParams.y = 0;
        this.mWindowParams.width = this.mDefaultDisplayInfo.logicalWidth;
        this.mWindowParams.height = this.mDefaultDisplayInfo.logicalHeight;
        this.mWidth = (int) (((float) this.mWidth) * this.mWidthScale);
        this.mHeight = (int) (((float) this.mHeight) * this.mHeightScale);
    }

    boolean isSingleHandModeHintShown() {
        String value = Global.getString(this.mContext.getContentResolver(), SINGLE_HAND_MODE_HINT_SHOWN);
        if (value == null || (value.equals(YES) ^ 1) != 0) {
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

    private void showHint(boolean visible) {
        this.mImageView = (ImageView) this.mWindowContent.findViewById(34603162);
        ((FrameLayout) this.mWindowContent.findViewById(34603163)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                SingleHandWindow.this.mImageView.performClick();
            }
        });
        if (!(visible || this.mImageView == null)) {
            this.mImageView.setOnClickListener(this.mActionClickListener);
        }
        show(this.mImageView, visible ^ 1);
        if (visible) {
            this.mWindowContent.setBackgroundColor(-1728053248);
        } else {
            this.mWindowContent.setBackgroundColor(0);
        }
        this.overlay_display_window = (TextView) this.mWindowContent.findViewById(34603164);
        show(this.overlay_display_window, visible ^ 1);
        LinearLayout viewSlideHint = (LinearLayout) this.mWindowContent.findViewById(34603166);
        if (viewSlideHint != null) {
            if (visible) {
                if (!isFingerPrintHand()) {
                    LayoutParams layoutParams = viewSlideHint.getLayoutParams();
                    if (this.mLeft) {
                        ((RelativeLayout.LayoutParams) layoutParams).addRule(9);
                    } else {
                        ((RelativeLayout.LayoutParams) layoutParams).addRule(11);
                    }
                    viewSlideHint.setLayoutParams(layoutParams);
                }
                viewSlideHint.setVisibility(0);
            } else {
                viewSlideHint.setVisibility(4);
            }
        }
        this.singlehandmode_slide_hint = (TextView) this.mWindowContent.findViewById(34603167);
        show(this.singlehandmode_slide_hint, visible);
        if (isFingerPrintHand()) {
            show((ImageView) this.mWindowContent.findViewById(34603168), visible);
            show((ImageView) this.mWindowContent.findViewById(34603169), visible);
        } else {
            ImageView imageView = (ImageView) this.mWindowContent.findViewById(34603170);
            if (imageView != null) {
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) imageView.getLayoutParams();
                params.width = (int) (((float) this.mDefaultDisplayInfo.logicalWidth) * 0.75f);
                params.height = (int) (((float) this.mContext.getResources().getDimensionPixelSize(17105141)) * 0.75f);
                imageView.setLayoutParams(params);
            }
            show(imageView, visible);
        }
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

    boolean singlehandRegionContainsPoint(int x, int y) {
        int top = (int) (((float) this.mDefaultDisplayInfo.logicalHeight) * 0.25f);
        int bottom = this.mDefaultDisplayInfo.logicalHeight;
        int left;
        int right;
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

    private boolean isFingerPrintHand() {
        if (FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION) {
            return FrontFingerPrintSettings.isNaviBarEnabled(this.mContext.getContentResolver()) ^ 1;
        }
        return false;
    }
}
