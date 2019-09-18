package com.android.server.wm;

import android.app.AbsWallpaperManagerInner;
import android.app.ActivityManager;
import android.app.HwWallpaperManager;
import android.app.WallpaperManager;
import android.content.Context;
import android.cover.CoverManager;
import android.database.ContentObserver;
import android.freeform.HwFreeFormManager;
import android.freeform.HwFreeFormUtils;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Slog;
import android.view.DisplayInfo;
import com.android.server.LocalServices;
import com.android.server.gesture.GestureNavConst;
import com.huawei.android.statistical.StatisticalUtils;
import java.util.ArrayList;
import java.util.Iterator;

final class SingleHandAdapter {
    static final boolean DEBUG = false;
    private static final float INITIAL_SCALE = 0.75f;
    private static final boolean IS_NOTCH_PROP = (!SystemProperties.get("ro.config.hw_notch_size", "").equals(""));
    public static final String KEY_SINGLE_HAND_SCREEN_ZOOM = "single_hand_screen_zoom";
    private static final int MSG_CLEAR_WALLPAPER = 1;
    private static final int MSG_ENTER_SINGLEHAND_TIMEOUT = 2;
    private static final String SHOW_ROUNDED_CORNERS = "show_rounded_corners";
    static final String TAG = "SingleHandAdapter";
    /* access modifiers changed from: private */
    public static boolean isSingleHandEnabled = true;
    private static boolean mIsFirstEnteredSingleHandMode = true;
    static final Object mLock = new Object();
    private static int mRoundedStateFlag = 1;
    private static int mRoundedStateFlagPre = 0;
    static Bitmap scaleWallpaper = null;
    /* access modifiers changed from: private */
    public Bitmap mBlurBitmap;
    private AbsWallpaperManagerInner.IBlurWallpaperCallback mBlurCallback;
    /* access modifiers changed from: private */
    public final Context mContext;
    private CoverManager mCoverManager = null;
    private boolean mCoverOpen = true;
    private String mCurrentOverlaySetting = "";
    /* access modifiers changed from: private */
    public DisplayInfo mDefaultDisplayInfo = new DisplayInfo();
    /* access modifiers changed from: private */
    public boolean mEnterSinglehandwindow2s = false;
    /* access modifiers changed from: private */
    public final Handler mHandler;
    private final ArrayList<SingleHandHandle> mOverlays = new ArrayList<>();
    private final Handler mPaperHandler;
    /* access modifiers changed from: private */
    public final WindowManagerService mService;
    /* access modifiers changed from: private */
    public final Handler mUiHandler;
    private WallpaperManager mWallpaperManager;

    private final class SingleHandHandle {
        private final Runnable mDismissRunnable = new Runnable() {
            public void run() {
                SingleHandWindow window = SingleHandHandle.this.mWindow;
                SingleHandWindow unused = SingleHandHandle.this.mWindow = null;
                if (window != null) {
                    window.dismiss();
                }
                SingleHandWindow window2 = SingleHandHandle.this.mWindowWalltop;
                SingleHandWindow unused2 = SingleHandHandle.this.mWindowWalltop = null;
                if (window2 != null) {
                    window2.dismiss();
                }
            }
        };
        /* access modifiers changed from: private */
        public final boolean mLeft;
        private final Runnable mShowRunnable = new Runnable() {
            public void run() {
                SingleHandWindow window = new SingleHandWindow(SingleHandAdapter.this.mContext, SingleHandHandle.this.mLeft, "virtual", SingleHandAdapter.this.mDefaultDisplayInfo.logicalWidth, SingleHandAdapter.this.mDefaultDisplayInfo.logicalHeight, SingleHandAdapter.this.mService);
                window.show();
                SingleHandWindow unused = SingleHandHandle.this.mWindow = window;
            }
        };
        private final Runnable mShowRunnableWalltop = new Runnable() {
            public void run() {
                SingleHandWindow window = new SingleHandWindow(SingleHandAdapter.this.mContext, SingleHandHandle.this.mLeft, "blurpapertop", SingleHandAdapter.this.mDefaultDisplayInfo.logicalWidth, SingleHandAdapter.this.mDefaultDisplayInfo.logicalHeight, SingleHandAdapter.this.mService);
                window.show();
                SingleHandWindow unused = SingleHandHandle.this.mWindowWalltop = window;
            }
        };
        /* access modifiers changed from: private */
        public SingleHandWindow mWindow;
        /* access modifiers changed from: private */
        public SingleHandWindow mWindowWalltop;

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

    private class blurCallback implements AbsWallpaperManagerInner.IBlurWallpaperCallback {
        public blurCallback() {
        }

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
            this.mWallpaperManager.setCallback(this.mBlurCallback);
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
                            boolean unused = SingleHandAdapter.this.mEnterSinglehandwindow2s = true;
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
        Settings.Global.putString(this.mContext.getContentResolver(), "single_hand_mode", "");
        this.mHandler.post(new Runnable() {
            public void run() {
                SingleHandAdapter.this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("single_hand_mode"), true, new ContentObserver(SingleHandAdapter.this.mHandler) {
                    public void onChange(boolean selfChange) {
                        Slog.i(SingleHandAdapter.TAG, "onChange..");
                        SingleHandAdapter.this.updateSingleHandMode();
                    }
                });
                SingleHandAdapter.this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("single_hand_screen_zoom"), true, new ContentObserver(SingleHandAdapter.this.mHandler) {
                    public void onChange(boolean selfChange) {
                        boolean z = true;
                        if (Settings.System.getIntForUser(SingleHandAdapter.this.mContext.getContentResolver(), "single_hand_screen_zoom", 1, ActivityManager.getCurrentUser()) != 1) {
                            z = false;
                        }
                        boolean unused = SingleHandAdapter.isSingleHandEnabled = z;
                        Slog.i(SingleHandAdapter.TAG, " KEY_SINGLE_HAND_SCREEN_ZOOM onChange isSingleHandEnabled=" + SingleHandAdapter.isSingleHandEnabled);
                        if (SingleHandAdapter.isSingleHandEnabled) {
                            SingleHandAdapter.this.updateBlur();
                            SingleHandAdapter.this.updateScaleWallpaperForBlur();
                            return;
                        }
                        synchronized (SingleHandAdapter.mLock) {
                            if (SingleHandAdapter.this.mBlurBitmap != null) {
                                SingleHandAdapter.this.mBlurBitmap.recycle();
                                Bitmap unused2 = SingleHandAdapter.this.mBlurBitmap = null;
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

    /* access modifiers changed from: private */
    public void updateSingleHandMode() {
        String value = Settings.Global.getString(this.mContext.getContentResolver(), "single_hand_mode");
        Slog.i(TAG, "updateSingleHandMode value: " + value + " cur: " + this.mCurrentOverlaySetting);
        if (value == null) {
            value = "";
        }
        if (IS_NOTCH_PROP) {
            if (this.mCoverManager == null) {
                this.mCoverManager = new CoverManager();
            }
            if (this.mCoverManager != null) {
                this.mCoverOpen = this.mCoverManager.isCoverOpen();
            }
            mRoundedStateFlagPre = mRoundedStateFlag;
            mRoundedStateFlag = "".equals(value) ? 1 : 0;
            if (this.mCoverOpen && mRoundedStateFlagPre != mRoundedStateFlag) {
                Settings.Global.putInt(this.mContext.getContentResolver(), SHOW_ROUNDED_CORNERS, mRoundedStateFlag);
            }
        }
        if (mIsFirstEnteredSingleHandMode) {
            updateBlur();
            updateScaleWallpaperForBlur();
        }
        if (!value.equals(this.mCurrentOverlaySetting)) {
            if (HwFreeFormUtils.isFreeFormEnable()) {
                HwFreeFormManager.getInstance(this.mContext).removeFloatListView();
                WindowManagerInternal windowManagerInternal = (WindowManagerInternal) LocalServices.getService(WindowManagerInternal.class);
                if (windowManagerInternal != null && windowManagerInternal.isStackVisible(5)) {
                    try {
                        ActivityManager.getService().removeStacksInWindowingModes(new int[]{5});
                    } catch (RemoteException e) {
                    }
                }
            }
            this.mCurrentOverlaySetting = value;
            synchronized (mLock) {
                if (!this.mOverlays.isEmpty()) {
                    Slog.i(TAG, "Dismissing all overlay display devices.");
                    Iterator<SingleHandHandle> it = this.mOverlays.iterator();
                    while (it.hasNext()) {
                        it.next().dismissLocked();
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
                    String targetStateName = "false";
                    if (left) {
                        targetStateName = "true";
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

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x00c5, code lost:
        return;
     */
    public void updateScaleWallpaperForBlur() {
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
            scaleWallpaper = Bitmap.createBitmap(wwidth, hheight, Bitmap.Config.ARGB_8888);
            if (scaleWallpaper == null) {
                Slog.e(TAG, "createBitmap return null");
                return;
            }
            Canvas canvas = new Canvas(scaleWallpaper);
            Paint p = new Paint();
            p.setColor(-1845493760);
            canvas.drawBitmap(this.mBlurBitmap, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, null);
            canvas.drawRect(GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, (float) this.mBlurBitmap.getWidth(), (float) this.mBlurBitmap.getHeight(), p);
            int[] inPixels = new int[(wwidth * hheight)];
            scaleWallpaper.getPixels(inPixels, 0, wwidth, 0, 0, wwidth, hheight);
            for (int y = 0; y < hheight; y++) {
                for (int x = 0; x < wwidth; x++) {
                    int index = (y * wwidth) + x;
                    inPixels[index] = -16777216 | inPixels[index];
                }
            }
            scaleWallpaper.setPixels(inPixels, 0, wwidth, 0, 0, wwidth, hheight);
            if (!this.mOverlays.isEmpty()) {
                Iterator<SingleHandHandle> it = this.mOverlays.iterator();
                while (it.hasNext()) {
                    it.next().onBlurWallpaperChanged();
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void updateBlur() {
        Bitmap bitmap = null;
        if (this.mWallpaperManager != null) {
            bitmap = this.mWallpaperManager.getBlurBitmap(new Rect(0, 0, this.mDefaultDisplayInfo.logicalWidth, this.mDefaultDisplayInfo.logicalHeight));
            if (bitmap != null) {
                mIsFirstEnteredSingleHandMode = false;
            }
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
