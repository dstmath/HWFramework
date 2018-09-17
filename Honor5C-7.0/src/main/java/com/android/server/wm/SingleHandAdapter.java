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
import android.os.Looper;
import android.os.Message;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import android.util.Slog;
import android.view.DisplayInfo;
import com.android.server.input.HwCircleAnimation;
import com.android.server.jankshield.TableJankEvent;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.huawei.android.statistical.StatisticalUtils;
import huawei.android.app.HwWallpaperManager;
import java.util.ArrayList;

final class SingleHandAdapter {
    static final boolean DEBUG = false;
    private static final float INITIAL_SCALE = 0.75f;
    public static final String KEY_SINGLE_HAND_SCREEN_ZOOM = "single_hand_screen_zoom";
    private static final int MSG_CLEAR_WALLPAPER = 1;
    private static final int MSG_ENTER_SINGLEHAND_TIMEOUT = 2;
    static final String TAG = "SingleHandAdapter";
    private static boolean isSingleHandEnabled;
    static final Object mLock = null;
    static Bitmap scaleWallpaper;
    private Bitmap mBlurBitmap;
    private IBlurWallpaperCallback mBlurCallback;
    private final Context mContext;
    private String mCurrentOverlaySetting;
    private DisplayInfo mDefaultDisplayInfo;
    private boolean mEnterSinglehandwindow2s;
    private final Handler mHandler;
    private final ArrayList<SingleHandHandle> mOverlays;
    private final Handler mPaperHandler;
    private final WindowManagerService mService;
    private final Handler mUiHandler;
    private WallpaperManager mWallpaperManager;

    /* renamed from: com.android.server.wm.SingleHandAdapter.1 */
    class AnonymousClass1 extends Handler {
        AnonymousClass1(Looper $anonymous0) {
            super($anonymous0);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SingleHandAdapter.MSG_CLEAR_WALLPAPER /*1*/:
                    Slog.i(SingleHandAdapter.TAG, "for BlurWallpaper :Wallpaper changed.");
                    SingleHandAdapter.this.updateScaleWallpaperForBlur();
                case SingleHandAdapter.MSG_ENTER_SINGLEHAND_TIMEOUT /*2*/:
                    Slog.i(SingleHandAdapter.TAG, "enter singlehandwindow 2s.");
                    synchronized (SingleHandAdapter.mLock) {
                        SingleHandAdapter.this.mEnterSinglehandwindow2s = true;
                        break;
                    }
                default:
            }
        }
    }

    private final class SingleHandHandle {
        private final Runnable mDismissRunnable;
        private final boolean mLeft;
        private final Runnable mShowRunnable;
        private final Runnable mShowRunnableWalltop;
        private SingleHandWindow mWindow;
        private SingleHandWindow mWindowWalltop;

        public SingleHandHandle(boolean left) {
            this.mShowRunnable = new Runnable() {
                public void run() {
                    SingleHandWindow window = new SingleHandWindow(SingleHandAdapter.this.mContext, SingleHandHandle.this.mLeft, "virtual", SingleHandAdapter.this.mDefaultDisplayInfo.logicalWidth, SingleHandAdapter.this.mDefaultDisplayInfo.logicalHeight, SingleHandAdapter.this.mService);
                    window.show();
                    SingleHandHandle.this.mWindow = window;
                }
            };
            this.mShowRunnableWalltop = new Runnable() {
                public void run() {
                    SingleHandWindow window = new SingleHandWindow(SingleHandAdapter.this.mContext, SingleHandHandle.this.mLeft, "blurpapertop", SingleHandAdapter.this.mDefaultDisplayInfo.logicalWidth, SingleHandAdapter.this.mDefaultDisplayInfo.logicalHeight, SingleHandAdapter.this.mService);
                    window.show();
                    SingleHandHandle.this.mWindowWalltop = window;
                }
            };
            this.mDismissRunnable = new Runnable() {
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
            this.mLeft = left;
            synchronized (SingleHandAdapter.mLock) {
                SingleHandAdapter.this.mUiHandler.post(this.mShowRunnableWalltop);
                SingleHandAdapter.this.mUiHandler.postDelayed(this.mShowRunnable, 150);
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wm.SingleHandAdapter.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wm.SingleHandAdapter.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wm.SingleHandAdapter.<clinit>():void");
    }

    public SingleHandAdapter(Context context, Handler handler, Handler uiHandler, WindowManagerService service) {
        this.mOverlays = new ArrayList();
        this.mCurrentOverlaySetting = AppHibernateCst.INVALID_PKG;
        this.mDefaultDisplayInfo = new DisplayInfo();
        this.mEnterSinglehandwindow2s = DEBUG;
        this.mHandler = handler;
        this.mContext = context;
        this.mUiHandler = uiHandler;
        this.mService = service;
        this.mDefaultDisplayInfo = this.mService.getDefaultDisplayInfoLocked();
        this.mWallpaperManager = (WallpaperManager) this.mContext.getSystemService("wallpaper");
        this.mBlurCallback = new blurCallback();
        if (this.mWallpaperManager instanceof HwWallpaperManager) {
            ((HwWallpaperManager) this.mWallpaperManager).setCallback(this.mBlurCallback);
        }
        this.mPaperHandler = new AnonymousClass1(this.mUiHandler.getLooper());
        updateBlur();
    }

    public void registerLocked() {
        Global.putString(this.mContext.getContentResolver(), "single_hand_mode", AppHibernateCst.INVALID_PKG);
        this.mHandler.post(new Runnable() {

            /* renamed from: com.android.server.wm.SingleHandAdapter.2.1 */
            class AnonymousClass1 extends ContentObserver {
                AnonymousClass1(Handler $anonymous0) {
                    super($anonymous0);
                }

                public void onChange(boolean selfChange) {
                    Slog.i(SingleHandAdapter.TAG, "onChange..");
                    SingleHandAdapter.this.updateSingleHandMode();
                }
            }

            /* renamed from: com.android.server.wm.SingleHandAdapter.2.2 */
            class AnonymousClass2 extends ContentObserver {
                AnonymousClass2(Handler $anonymous0) {
                    super($anonymous0);
                }

                public void onChange(boolean selfChange) {
                    boolean z = true;
                    if (System.getIntForUser(SingleHandAdapter.this.mContext.getContentResolver(), SingleHandAdapter.KEY_SINGLE_HAND_SCREEN_ZOOM, SingleHandAdapter.MSG_CLEAR_WALLPAPER, ActivityManager.getCurrentUser()) != SingleHandAdapter.MSG_CLEAR_WALLPAPER) {
                        z = SingleHandAdapter.DEBUG;
                    }
                    SingleHandAdapter.isSingleHandEnabled = z;
                    Slog.i(SingleHandAdapter.TAG, " KEY_SINGLE_HAND_SCREEN_ZOOM onChange isSingleHandEnabled=" + SingleHandAdapter.isSingleHandEnabled);
                    synchronized (SingleHandAdapter.mLock) {
                        if (SingleHandAdapter.isSingleHandEnabled) {
                            SingleHandAdapter.this.updateBlur();
                            SingleHandAdapter.this.updateScaleWallpaperForBlur();
                        } else {
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
                }
            }

            public void run() {
                SingleHandAdapter.this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("single_hand_mode"), true, new AnonymousClass1(SingleHandAdapter.this.mHandler));
                SingleHandAdapter.this.mContext.getContentResolver().registerContentObserver(System.getUriFor(SingleHandAdapter.KEY_SINGLE_HAND_SCREEN_ZOOM), true, new AnonymousClass2(SingleHandAdapter.this.mHandler));
            }
        });
    }

    private void updateSingleHandMode() {
        String value = Global.getString(this.mContext.getContentResolver(), "single_hand_mode");
        Slog.i(TAG, "updateSingleHandMode value: " + value + " cur: " + this.mCurrentOverlaySetting);
        if (value == null) {
            value = AppHibernateCst.INVALID_PKG;
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
                        this.mEnterSinglehandwindow2s = DEBUG;
                    }
                    StatisticalUtils.reportc(this.mContext, 42);
                }
                if (!AppHibernateCst.INVALID_PKG.equals(value)) {
                    boolean left = value.contains("left");
                    this.mOverlays.add(new SingleHandHandle(left));
                    this.mEnterSinglehandwindow2s = DEBUG;
                    String targetStateName = "false";
                    if (left) {
                        targetStateName = "true";
                    }
                    Context context = this.mContext;
                    Object[] objArr = new Object[MSG_CLEAR_WALLPAPER];
                    objArr[0] = targetStateName;
                    StatisticalUtils.reporte(context, 41, String.format("{state:left=%s}", objArr));
                    if (this.mPaperHandler.hasMessages(MSG_ENTER_SINGLEHAND_TIMEOUT)) {
                        this.mPaperHandler.removeMessages(MSG_ENTER_SINGLEHAND_TIMEOUT);
                    }
                    this.mPaperHandler.sendEmptyMessageDelayed(MSG_ENTER_SINGLEHAND_TIMEOUT, TableJankEvent.recMAXCOUNT);
                }
            }
        }
    }

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
            int wwidth = (int) (((float) this.mBlurBitmap.getWidth()) * HwCircleAnimation.SMALL_ALPHA);
            int hheight = (int) (((float) this.mBlurBitmap.getHeight()) * HwCircleAnimation.SMALL_ALPHA);
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
            for (int y = 0; y < hheight; y += MSG_CLEAR_WALLPAPER) {
                for (int x = 0; x < wwidth; x += MSG_CLEAR_WALLPAPER) {
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
        synchronized (mLock) {
            if (this.mWallpaperManager != null) {
                this.mBlurBitmap = this.mWallpaperManager.getBlurBitmap(new Rect(0, 0, this.mDefaultDisplayInfo.logicalWidth, this.mDefaultDisplayInfo.logicalHeight));
            }
            if (this.mPaperHandler.hasMessages(MSG_CLEAR_WALLPAPER)) {
                this.mPaperHandler.removeMessages(MSG_CLEAR_WALLPAPER);
            }
            this.mPaperHandler.sendEmptyMessageDelayed(MSG_CLEAR_WALLPAPER, 1000);
        }
    }
}
