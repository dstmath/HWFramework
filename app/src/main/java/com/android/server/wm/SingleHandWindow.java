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
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.android.server.security.securitydiagnose.HwSecDiagnoseConstant;
import com.android.server.wifipro.WifiProCommonDefs;
import com.android.server.wifipro.WifiProCommonUtils;
import huawei.android.provider.FrontFingerPrintSettings;
import huawei.com.android.server.policy.HwGlobalActionsData;

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
    private OnClickListener mActionClickListener;
    private boolean mAttachedToWindow;
    private Configuration mConfiguration;
    private final Context mContext;
    private final Display mDefaultDisplay;
    private DisplayInfo mDefaultDisplayInfo;
    private final DisplayListener mDisplayListener;
    private final DisplayManager mDisplayManager;
    private Handler mHandler;
    private int mHeight;
    private float mHeightScale;
    private ImageView mImageView;
    private BroadcastReceiver mIntentReceiver;
    private boolean mIsNeedRelayout;
    private LayoutParams mLayoutParams;
    private final boolean mLeft;
    private final String mName;
    private final OnTouchListener mOnTouchListener;
    private boolean mPointDownOuter;
    private DisplayInfo mPreDisplayInfo;
    private RelativeLayout mRelateViewbottom;
    private RelativeLayout mRelateViewtop;
    private final WindowManagerService mService;
    private int mWidth;
    private float mWidthScale;
    private View mWindowContent;
    private final WindowManager mWindowManager;
    private WindowManager.LayoutParams mWindowParams;
    private boolean mWindowVisible;
    private TextView overlay_display_window;
    private TextView singlehandmode_slide_hint;

    public SingleHandWindow(Context context, boolean left, String name, int width, int height, WindowManagerService service) {
        this.mDefaultDisplayInfo = new DisplayInfo();
        this.mConfiguration = new Configuration();
        this.mPreDisplayInfo = new DisplayInfo();
        this.mPointDownOuter = DEBUG;
        this.overlay_display_window = null;
        this.singlehandmode_slide_hint = null;
        this.mAttachedToWindow = DEBUG;
        this.mIsNeedRelayout = DEBUG;
        this.mIntentReceiver = new BroadcastReceiver() {
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
        this.mDisplayListener = new DisplayListener() {
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
        this.mOnTouchListener = new OnTouchListener() {
            public boolean onTouch(View view, MotionEvent event) {
                boolean z = SingleHandWindow.DEBUG;
                boolean inRegion = SingleHandWindow.this.singlehandRegionContainsPoint((int) event.getRawX(), (int) event.getRawY());
                switch (event.getActionMasked()) {
                    case WifiProCommonUtils.HISTORY_ITEM_NO_INTERNET /*0*/:
                        SingleHandWindow singleHandWindow = SingleHandWindow.this;
                        if (!inRegion) {
                            z = true;
                        }
                        singleHandWindow.mPointDownOuter = z;
                        break;
                    case HwGlobalActionsData.FLAG_AIRPLANEMODE_ON /*1*/:
                        ImageView imageView = (ImageView) SingleHandWindow.this.mWindowContent.findViewById(34603150);
                        if (imageView == null || imageView.getVisibility() != 0) {
                            SingleHandWindow.this.showHint(SingleHandWindow.DEBUG);
                            Global.putString(SingleHandWindow.this.mContext.getContentResolver(), SingleHandWindow.SINGLE_HAND_MODE_HINT_SHOWN, SingleHandWindow.YES);
                        } else if (!inRegion && SingleHandWindow.this.mPointDownOuter) {
                            Global.putString(SingleHandWindow.this.mContext.getContentResolver(), "single_hand_mode", AppHibernateCst.INVALID_PKG);
                        }
                        SingleHandWindow.this.mPointDownOuter = SingleHandWindow.DEBUG;
                        break;
                    case WifiProCommonDefs.WIFI_SECURITY_PHISHING_FAILED /*3*/:
                        SingleHandWindow.this.mPointDownOuter = SingleHandWindow.DEBUG;
                        break;
                }
                return true;
            }
        };
        this.mActionClickListener = new OnClickListener() {
            public void onClick(View v) {
                SingleHandWindow.this.showHint(true);
            }
        };
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
        this.mDefaultDisplayInfo = this.mService.getDefaultDisplayInfoLocked();
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
            this.mAttachedToWindow = DEBUG;
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
                this.mWindowVisible = DEBUG;
            } else {
                this.mDisplayManager.unregisterDisplayListener(this.mDisplayListener);
            }
        }
    }

    public void relayout() {
        if (this.mWindowVisible && this.mName.contains("blurpaper")) {
            this.mWindowManager.removeView(this.mWindowContent);
            this.mWindowVisible = DEBUG;
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
            crop = Bitmap.createBitmap(SingleHandAdapter.scaleWallpaper, (int) (((float) w) * INITIAL_SCALE), (int) (((float) h) * 0.25f), (int) (((float) w) - (((float) w) * INITIAL_SCALE)), (int) (((float) h) * INITIAL_SCALE));
        } else {
            crop = Bitmap.createBitmap(SingleHandAdapter.scaleWallpaper, 0, (int) (((float) h) * 0.25f), (int) (((float) w) - (((float) w) * INITIAL_SCALE)), (int) (((float) h) * INITIAL_SCALE));
        }
        return crop;
    }

    void updateLocale() {
        Slog.d(TAG, "updateLocale .");
        if (this.overlay_display_window != null) {
            this.overlay_display_window.setText(this.mContext.getResources().getString(33685738));
        }
        if (this.singlehandmode_slide_hint == null) {
            return;
        }
        if (isFingerPrintHand()) {
            this.singlehandmode_slide_hint.setText(this.mContext.getResources().getString(33685897));
        } else {
            this.singlehandmode_slide_hint.setText(this.mContext.getResources().getString(33685739));
        }
    }

    void updateConfiguration() {
        Configuration newConfiguration = this.mContext.getResources().getConfiguration();
        int diff = this.mConfiguration.diff(newConfiguration);
        this.mConfiguration = newConfiguration;
        if ((diff & HwSecDiagnoseConstant.BIT_VERIFYBOOT) != 0) {
            Global.putString(this.mContext.getContentResolver(), "single_hand_mode", AppHibernateCst.INVALID_PKG);
        }
    }

    private boolean updateDefaultDisplayInfo() {
        this.mIsNeedRelayout = DEBUG;
        if (!this.mDefaultDisplay.getDisplayInfo(this.mDefaultDisplayInfo)) {
            Slog.w(TAG, "Cannot show overlay display because there is no default display upon which to show it.");
            return DEBUG;
        } else if (this.mPreDisplayInfo == null) {
            return DEBUG;
        } else {
            if (!this.mPreDisplayInfo.equals(this.mDefaultDisplayInfo)) {
                this.mWidthScale = ((float) this.mDefaultDisplayInfo.logicalWidth) / ((float) this.mPreDisplayInfo.logicalWidth);
                this.mHeightScale = ((float) this.mDefaultDisplayInfo.logicalHeight) / ((float) this.mPreDisplayInfo.logicalHeight);
                if (this.mDefaultDisplayInfo.logicalWidth == this.mPreDisplayInfo.logicalWidth && this.mDefaultDisplayInfo.logicalHeight == this.mPreDisplayInfo.logicalHeight) {
                    if (this.mDefaultDisplayInfo.logicalDensityDpi != this.mPreDisplayInfo.logicalDensityDpi) {
                    }
                    this.mPreDisplayInfo.copyFrom(this.mDefaultDisplayInfo);
                }
                this.mIsNeedRelayout = true;
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
                this.mRelateViewtop = (RelativeLayout) this.mWindowContent.findViewById(34603148);
                this.mLayoutParams = this.mRelateViewtop.getLayoutParams();
                this.mLayoutParams.height = this.mDefaultDisplayInfo.logicalHeight / 4;
                this.mRelateViewtop.setLayoutParams(this.mLayoutParams);
                this.mRelateViewtop.setBackground(new BitmapDrawable(this.mRelateViewtop.getResources(), cropwallpaper(true)));
                this.mRelateViewbottom = (RelativeLayout) this.mWindowContent.findViewById(34603153);
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
                this.mRelateViewbottom.setBackground(new BitmapDrawable(this.mRelateViewbottom.getResources(), cropwallpaper(DEBUG)));
            }
        }
        this.mWindowParams = new WindowManager.LayoutParams(2026);
        WindowManager.LayoutParams layoutParams = this.mWindowParams;
        layoutParams.flags |= 16778024;
        layoutParams = this.mWindowParams;
        layoutParams.privateFlags |= 2;
        this.mWindowParams.alpha = WINDOW_ALPHA;
        this.mWindowParams.gravity = 51;
        this.mWindowParams.format = -3;
        if (this.mName.contains("blurpaper")) {
            showHint(hintShown ? DEBUG : true);
            this.mWidthScale = WINDOW_ALPHA;
            this.mHeightScale = WINDOW_ALPHA;
        }
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
        if (value == null || !value.equals(YES)) {
            return DEBUG;
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
        this.mImageView = (ImageView) this.mWindowContent.findViewById(34603150);
        ((FrameLayout) this.mWindowContent.findViewById(34603149)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                SingleHandWindow.this.mImageView.performClick();
            }
        });
        if (!(visible || this.mImageView == null)) {
            this.mImageView.setOnClickListener(this.mActionClickListener);
        }
        show(this.mImageView, visible ? DEBUG : true);
        if (visible) {
            this.mWindowContent.setBackgroundColor(-1728053248);
        } else {
            this.mWindowContent.setBackgroundColor(0);
        }
        this.overlay_display_window = (TextView) this.mWindowContent.findViewById(34603151);
        show(this.overlay_display_window, visible);
        show((ImageView) this.mWindowContent.findViewById(34603152), visible);
        LinearLayout viewSlideHint = (LinearLayout) this.mWindowContent.findViewById(34603154);
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
        this.singlehandmode_slide_hint = (TextView) this.mWindowContent.findViewById(34603155);
        show(this.singlehandmode_slide_hint, visible);
        if (isFingerPrintHand()) {
            show((ImageView) this.mWindowContent.findViewById(34603157), visible);
            show((ImageView) this.mWindowContent.findViewById(34603158), visible);
        } else {
            ImageView imageView = (ImageView) this.mWindowContent.findViewById(34603156);
            if (imageView != null) {
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) imageView.getLayoutParams();
                params.width = (int) (((float) this.mDefaultDisplayInfo.logicalWidth) * INITIAL_SCALE);
                params.height = (int) (((float) this.mContext.getResources().getDimensionPixelSize(17104920)) * INITIAL_SCALE);
                imageView.setLayoutParams(params);
            }
            show(imageView, visible);
        }
        if (visible) {
            this.mWindowParams.setTitle("hwSingleMode_windowbg_hint");
        } else if (this.mLeft) {
            this.mWindowParams.setTitle("hwSingleMode_windowbg_left");
        } else {
            this.mWindowParams.setTitle("hwSingleMode_windowbg_right");
        }
        if (this.mWindowVisible) {
            this.mWindowManager.updateViewLayout(this.mWindowContent, this.mWindowParams);
        }
    }

    boolean singlehandRegionContainsPoint(int x, int y) {
        int top = (int) (((float) this.mDefaultDisplayInfo.logicalHeight) * 0.25f);
        int bottom = this.mDefaultDisplayInfo.logicalHeight;
        int left;
        int right;
        if (this.mLeft) {
            left = 0;
            right = (int) (((float) this.mDefaultDisplayInfo.logicalWidth) * INITIAL_SCALE);
        } else {
            left = (int) (((float) this.mDefaultDisplayInfo.logicalWidth) * 0.25f);
            right = this.mDefaultDisplayInfo.logicalWidth;
        }
        if (y < top || y >= bottom || x < left || x >= right) {
            return DEBUG;
        }
        return true;
    }

    public void onBlurWallpaperChanged() {
        this.mRelateViewtop = (RelativeLayout) this.mWindowContent.findViewById(34603148);
        this.mRelateViewtop.setBackground(new BitmapDrawable(this.mRelateViewtop.getResources(), cropwallpaper(true)));
        this.mRelateViewbottom = (RelativeLayout) this.mWindowContent.findViewById(34603153);
        this.mRelateViewbottom.setBackground(new BitmapDrawable(this.mRelateViewbottom.getResources(), cropwallpaper(DEBUG)));
    }

    private boolean isFingerPrintHand() {
        if (!FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION || FrontFingerPrintSettings.isNaviBarEnabled(this.mContext.getContentResolver())) {
            return DEBUG;
        }
        return true;
    }
}
