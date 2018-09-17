package com.android.server.wm;

import android.app.AbsWallpaperManagerInner.IBlurWallpaperCallback;
import android.app.ActivityManager;
import android.app.WallpaperManager;
import android.content.Context;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import android.util.Slog;
import android.view.DisplayInfo;
import com.android.server.devicepolicy.StorageUtils;
import com.huawei.android.statistical.StatisticalUtils;
import huawei.android.app.HwWallpaperManager;
import java.util.ArrayList;

final class SingleHandAdapter {
    static final boolean DEBUG = false;
    private static final float INITIAL_SCALE = 0.75f;
    private static final boolean IS_NOTCH_PROP = (!SystemProperties.get("ro.config.hw_notch_size", "").equals(""));
    public static final String KEY_SINGLE_HAND_SCREEN_ZOOM = "single_hand_screen_zoom";
    private static final int MSG_CLEAR_WALLPAPER = 1;
    private static final int MSG_ENTER_SINGLEHAND_TIMEOUT = 2;
    private static final String SHOW_ROUNDED_CORNERS = "show_rounded_corners";
    static final String TAG = "SingleHandAdapter";
    private static boolean isSingleHandEnabled = true;
    static final Object mLock = new Object();
    private static int mRoundedStateFlag = 1;
    static Bitmap scaleWallpaper = null;
    private Bitmap mBlurBitmap;
    private IBlurWallpaperCallback mBlurCallback;
    private final Context mContext;
    private String mCurrentOverlaySetting = "";
    private DisplayInfo mDefaultDisplayInfo = new DisplayInfo();
    private boolean mEnterSinglehandwindow2s = false;
    private final Handler mHandler;
    private final ArrayList<SingleHandHandle> mOverlays = new ArrayList();
    private final Handler mPaperHandler;
    private final WindowManagerService mService;
    private final Handler mUiHandler;
    private WallpaperManager mWallpaperManager;

    private final class SingleHandHandle {
        private final Runnable mDismissRunnable = new Runnable() {
            public void run() {
                SingleHandWindow window = SingleHandHandle.this.mWindow;
                SingleHandHandle.this.mWindow = null;
                if (window != null) {
                    window.dismiss();
                }
                window = SingleHandHandle.this.mWindowWalltop;
                SingleHandHandle.this.mWindowWalltop = null;
                if (window != null) {
                    window.dismiss();
                }
            }
        };
        private final boolean mLeft;
        private final Runnable mShowRunnable = new Runnable() {
            public void run() {
                SingleHandWindow window = new SingleHandWindow(SingleHandAdapter.this.mContext, SingleHandHandle.this.mLeft, "virtual", SingleHandAdapter.this.mDefaultDisplayInfo.logicalWidth, SingleHandAdapter.this.mDefaultDisplayInfo.logicalHeight, SingleHandAdapter.this.mService);
                window.show();
                SingleHandHandle.this.mWindow = window;
            }
        };
        private final Runnable mShowRunnableWalltop = new Runnable() {
            public void run() {
                SingleHandWindow window = new SingleHandWindow(SingleHandAdapter.this.mContext, SingleHandHandle.this.mLeft, "blurpapertop", SingleHandAdapter.this.mDefaultDisplayInfo.logicalWidth, SingleHandAdapter.this.mDefaultDisplayInfo.logicalHeight, SingleHandAdapter.this.mService);
                window.show();
                SingleHandHandle.this.mWindowWalltop = window;
            }
        };
        private SingleHandWindow mWindow;
        private SingleHandWindow mWindowWalltop;

        public SingleHandHandle(boolean left) {
            this.mLeft = left;
            synchronized (SingleHandAdapter.mLock) {
                SingleHandAdapter.this.mUiHandler.post(this.mShowRunnableWalltop);
                SingleHandAdapter.this.mUiHandler.postDelayed(this.mShowRunnable, 10);
            }
        }

        public void dismissLocked() {
            SingleHandAdapter.this.mUiHandler.removeCallbacks(this.mShowRunnable);
            SingleHandAdapter.this.mUiHandler.removeCallbacks(this.mShowRunnableWalltop);
            SingleHandAdapter.this.mUiHandler.post(this.mDismissRunnable);
        }

        public void onBlurWallpaperChanged() {
            if (this.mWindowWalltop != null) {
                this.mWindowWalltop.onBlurWallpaperChanged();
            }
        }
    }

    private class blurCallback implements IBlurWallpaperCallback {
        public void onBlurWallpaperChanged() {
            SingleHandAdapter.this.updateBlur();
        }
    }

    public SingleHandAdapter(Context context, Handler handler, Handler uiHandler, WindowManagerService service) {
        this.mHandler = handler;
        this.mContext = context;
        this.mUiHandler = uiHandler;
        this.mService = service;
        this.mDefaultDisplayInfo = this.mService.getDefaultDisplayContentLocked().getDisplayInfo();
        this.mWallpaperManager = (WallpaperManager) this.mContext.getSystemService("wallpaper");
        this.mBlurCallback = new blurCallback();
        if (this.mWallpaperManager instanceof HwWallpaperManager) {
            ((HwWallpaperManager) this.mWallpaperManager).setCallback(this.mBlurCallback);
        }
        this.mPaperHandler = new Handler(this.mUiHandler.getLooper()) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        Slog.i(SingleHandAdapter.TAG, "for BlurWallpaper :Wallpaper changed.");
                        SingleHandAdapter.this.updateScaleWallpaperForBlur();
                        return;
                    case 2:
                        Slog.i(SingleHandAdapter.TAG, "enter singlehandwindow 2s.");
                        synchronized (SingleHandAdapter.mLock) {
                            SingleHandAdapter.this.mEnterSinglehandwindow2s = true;
                        }
                        return;
                    default:
                        return;
                }
            }
        };
        updateBlur();
    }

    public void registerLocked() {
        Global.putString(this.mContext.getContentResolver(), "single_hand_mode", "");
        this.mHandler.post(new Runnable() {
            public void run() {
                SingleHandAdapter.this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("single_hand_mode"), true, new ContentObserver(SingleHandAdapter.this.mHandler) {
                    public void onChange(boolean selfChange) {
                        Slog.i(SingleHandAdapter.TAG, "onChange..");
                        SingleHandAdapter.this.updateSingleHandMode();
                    }
                });
                SingleHandAdapter.this.mContext.getContentResolver().registerContentObserver(System.getUriFor("single_hand_screen_zoom"), true, new ContentObserver(SingleHandAdapter.this.mHandler) {
                    public void onChange(boolean selfChange) {
                        boolean z = true;
                        if (System.getIntForUser(SingleHandAdapter.this.mContext.getContentResolver(), "single_hand_screen_zoom", 1, ActivityManager.getCurrentUser()) != 1) {
                            z = false;
                        }
                        SingleHandAdapter.isSingleHandEnabled = z;
                        Slog.i(SingleHandAdapter.TAG, " KEY_SINGLE_HAND_SCREEN_ZOOM onChange isSingleHandEnabled=" + SingleHandAdapter.isSingleHandEnabled);
                        if (SingleHandAdapter.isSingleHandEnabled) {
                            SingleHandAdapter.this.updateBlur();
                            SingleHandAdapter.this.updateScaleWallpaperForBlur();
                            return;
                        }
                        synchronized (SingleHandAdapter.mLock) {
                            if (SingleHandAdapter.this.mBlurBitmap != null) {
                                SingleHandAdapter.this.mBlurBitmap.recycle();
                                SingleHandAdapter.this.mBlurBitmap = null;
                            }
                            if (SingleHandAdapter.scaleWallpaper != null) {
                                SingleHandAdapter.scaleWallpaper.recycle();
                                SingleHandAdapter.scaleWallpaper = null;
                            }
                        }
                    }
                });
            }
        });
    }

    private void updateSingleHandMode() {
        int i = 1;
        String value = Global.getString(this.mContext.getContentResolver(), "single_hand_mode");
        Slog.i(TAG, "updateSingleHandMode value: " + value + " cur: " + this.mCurrentOverlaySetting);
        if (value == null) {
            value = "";
        }
        if (IS_NOTCH_PROP) {
            if (!"".equals(value)) {
                i = 0;
            }
            mRoundedStateFlag = i;
            Global.putInt(this.mContext.getContentResolver(), SHOW_ROUNDED_CORNERS, mRoundedStateFlag);
        }
        if (!value.equals(this.mCurrentOverlaySetting)) {
            this.mCurrentOverlaySetting = value;
            synchronized (mLock) {
                if (!this.mOverlays.isEmpty()) {
                    Slog.i(TAG, "Dismissing all overlay display devices.");
                    for (SingleHandHandle overlay : this.mOverlays) {
                        overlay.dismissLocked();
                    }
                    this.mOverlays.clear();
                    if (!this.mEnterSinglehandwindow2s) {
                        StatisticalUtils.reportc(this.mContext, 43);
                        this.mEnterSinglehandwindow2s = false;
                    }
                    StatisticalUtils.reportc(this.mContext, 42);
                }
                if (!"".equals(value)) {
                    boolean left = value.contains("left");
                    this.mOverlays.add(new SingleHandHandle(left));
                    this.mEnterSinglehandwindow2s = false;
                    String targetStateName = StorageUtils.SDCARD_RWMOUNTED_STATE;
                    if (left) {
                        targetStateName = StorageUtils.SDCARD_ROMOUNTED_STATE;
                    }
                    StatisticalUtils.reporte(this.mContext, 41, String.format("{state:left=%s}", new Object[]{targetStateName}));
                    if (this.mPaperHandler.hasMessages(2)) {
                        this.mPaperHandler.removeMessages(2);
                    }
                    this.mPaperHandler.sendEmptyMessageDelayed(2, 2000);
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:37:0x00dc, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void updateScaleWallpaperForBlur() {
        synchronized (mLock) {
            if (scaleWallpaper != null) {
                scaleWallpaper.recycle();
                scaleWallpaper = null;
            }
            if (this.mBlurBitmap == null) {
                Slog.e(TAG, "getBlurBitmap return null");
                return;
            }
            int wwidth = (int) (((float) this.mBlurBitmap.getWidth()) * 1.0f);
            int hheight = (int) (((float) this.mBlurBitmap.getHeight()) * 1.0f);
            scaleWallpaper = Bitmap.createBitmap(wwidth, hheight, Config.ARGB_8888);
            if (scaleWallpaper == null) {
                Slog.e(TAG, "createBitmap return null");
                return;
            }
            Canvas canvas = new Canvas(scaleWallpaper);
            Paint p = new Paint();
            p.setColor(-1845493760);
            canvas.drawBitmap(this.mBlurBitmap, 0.0f, 0.0f, null);
            canvas.drawRect(0.0f, 0.0f, (float) this.mBlurBitmap.getWidth(), (float) this.mBlurBitmap.getHeight(), p);
            int[] inPixels = new int[(wwidth * hheight)];
            scaleWallpaper.getPixels(inPixels, 0, wwidth, 0, 0, wwidth, hheight);
            for (int y = 0; y < hheight; y++) {
                for (int x = 0; x < wwidth; x++) {
                    int index = (y * wwidth) + x;
                    inPixels[index] = inPixels[index] | -16777216;
                }
            }
            scaleWallpaper.setPixels(inPixels, 0, wwidth, 0, 0, wwidth, hheight);
            if (!this.mOverlays.isEmpty()) {
                for (SingleHandHandle overlay : this.mOverlays) {
                    overlay.onBlurWallpaperChanged();
                }
            }
        }
    }

    private void updateBlur() {
        Bitmap bitmap = null;
        if (this.mWallpaperManager != null) {
            bitmap = this.mWallpaperManager.getBlurBitmap(new Rect(0, 0, this.mDefaultDisplayInfo.logicalWidth, this.mDefaultDisplayInfo.logicalHeight));
        }
        synchronized (mLock) {
            this.mBlurBitmap = bitmap;
        }
        if (this.mPaperHandler.hasMessages(1)) {
            this.mPaperHandler.removeMessages(1);
        }
        this.mPaperHandler.sendEmptyMessageDelayed(1, 1000);
    }
}
