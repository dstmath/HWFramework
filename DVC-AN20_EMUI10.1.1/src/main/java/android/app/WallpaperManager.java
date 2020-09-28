package android.app;

import android.annotation.SystemApi;
import android.annotation.UnsupportedAppUsage;
import android.app.IWallpaperManagerCallback;
import android.app.WallpaperManager;
import android.common.HwFrameworkFactory;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hwtheme.HwThemeManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.DeadSystemException;
import android.os.FileUtils;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.HwPCUtils;
import android.util.Log;
import android.util.Pair;
import android.view.Display;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import com.android.internal.R;
import com.google.android.collect.Lists;
import com.huawei.android.app.IWallpaperManagerEx;
import com.huawei.android.fsm.HwFoldScreenManager;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import libcore.io.IoUtils;

public class WallpaperManager {
    public static final String ACTION_CHANGE_LIVE_WALLPAPER = "android.service.wallpaper.CHANGE_LIVE_WALLPAPER";
    public static final String ACTION_CROP_AND_SET_WALLPAPER = "android.service.wallpaper.CROP_AND_SET_WALLPAPER";
    public static final String ACTION_LIVE_WALLPAPER_CHOOSER = "android.service.wallpaper.LIVE_WALLPAPER_CHOOSER";
    public static final String COMMAND_DROP = "android.home.drop";
    public static final String COMMAND_SECONDARY_TAP = "android.wallpaper.secondaryTap";
    public static final String COMMAND_TAP = "android.wallpaper.tap";
    private static boolean DEBUG = false;
    public static final String EXTRA_LIVE_WALLPAPER_COMPONENT = "android.service.wallpaper.extra.LIVE_WALLPAPER_COMPONENT";
    public static final String EXTRA_NEW_WALLPAPER_ID = "android.service.wallpaper.extra.ID";
    public static final int FLAG_LOCK = 2;
    public static final int FLAG_SYSTEM = 1;
    private static final String PROP_LOCK_WALLPAPER = "ro.config.lock_wallpaper";
    private static final String PROP_WALLPAPER = "ro.config.wallpaper";
    private static final String PROP_WALLPAPER_COMPONENT = "ro.config.wallpaper_component";
    private static String TAG = "WallpaperManager";
    public static final String WALLPAPER_PREVIEW_META_DATA = "android.wallpaper.preview";
    protected static final ArrayList<WeakReference<Object>> mCallbacks = Lists.newArrayList();
    private static IHwWallpaperManagerEx mIHwWallpaperManagerEx;
    @UnsupportedAppUsage
    private static Globals sGlobals;
    private static final Object sSync = new Object[0];
    private final Context mContext;
    private IWallpaperManagerEx mServiceEx;
    private float mWallpaperXStep = -1.0f;
    private float mWallpaperYStep = -1.0f;

    @Retention(RetentionPolicy.SOURCE)
    public @interface SetWallpaperFlags {
    }

    static class FastBitmapDrawable extends Drawable {
        private final Bitmap mBitmap;
        private int mDrawLeft;
        private int mDrawTop;
        private final int mHeight;
        private final Paint mPaint;
        private final int mWidth;

        private FastBitmapDrawable(Bitmap bitmap) {
            this.mBitmap = bitmap;
            this.mWidth = bitmap.getWidth();
            this.mHeight = bitmap.getHeight();
            setBounds(0, 0, this.mWidth, this.mHeight);
            this.mPaint = new Paint();
            this.mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        }

        @Override // android.graphics.drawable.Drawable
        public void draw(Canvas canvas) {
            canvas.drawBitmap(this.mBitmap, (float) this.mDrawLeft, (float) this.mDrawTop, this.mPaint);
        }

        @Override // android.graphics.drawable.Drawable
        public int getOpacity() {
            return -1;
        }

        @Override // android.graphics.drawable.Drawable
        public void setBounds(int left, int top, int right, int bottom) {
            this.mDrawLeft = (((right - left) - this.mWidth) / 2) + left;
            this.mDrawTop = (((bottom - top) - this.mHeight) / 2) + top;
        }

        @Override // android.graphics.drawable.Drawable
        public void setAlpha(int alpha) {
            throw new UnsupportedOperationException("Not supported with this drawable");
        }

        @Override // android.graphics.drawable.Drawable
        public void setColorFilter(ColorFilter colorFilter) {
            throw new UnsupportedOperationException("Not supported with this drawable");
        }

        @Override // android.graphics.drawable.Drawable
        public void setDither(boolean dither) {
            throw new UnsupportedOperationException("Not supported with this drawable");
        }

        @Override // android.graphics.drawable.Drawable
        public void setFilterBitmap(boolean filter) {
            throw new UnsupportedOperationException("Not supported with this drawable");
        }

        @Override // android.graphics.drawable.Drawable
        public int getIntrinsicWidth() {
            return this.mWidth;
        }

        @Override // android.graphics.drawable.Drawable
        public int getIntrinsicHeight() {
            return this.mHeight;
        }

        @Override // android.graphics.drawable.Drawable
        public int getMinimumWidth() {
            return this.mWidth;
        }

        @Override // android.graphics.drawable.Drawable
        public int getMinimumHeight() {
            return this.mHeight;
        }
    }

    /* access modifiers changed from: private */
    public static class Globals extends IWallpaperManagerCallback.Stub {
        private Bitmap mBlurWallpaper;
        private Bitmap mCachedWallpaper;
        private int mCachedWallpaperUserId;
        private boolean mColorCallbackRegistered;
        private final ArrayList<Pair<OnColorsChangedListener, Handler>> mColorListeners = new ArrayList<>();
        private Bitmap mDefaultWallpaper;
        private Handler mMainLooperHandler;
        private final IWallpaperManager mService;
        private int wallpaperHeight = 0;
        private int wallpaperWidth = 0;

        Globals(IWallpaperManager service, Looper looper) {
            this.mService = service;
            this.mMainLooperHandler = new Handler(looper);
            forgetLoadedWallpaper();
        }

        @Override // android.app.IWallpaperManagerCallback
        public void onWallpaperChanged() {
            forgetLoadedWallpaper();
        }

        public void addOnColorsChangedListener(OnColorsChangedListener callback, Handler handler, int userId, int displayId) {
            synchronized (this) {
                if (!this.mColorCallbackRegistered) {
                    try {
                        this.mService.registerWallpaperColorsCallback(this, userId, displayId);
                        this.mColorCallbackRegistered = true;
                    } catch (RemoteException e) {
                        Log.w(WallpaperManager.TAG, "Can't register for color updates", e);
                    }
                }
                this.mColorListeners.add(new Pair<>(callback, handler));
            }
        }

        public void removeOnColorsChangedListener(OnColorsChangedListener callback, int userId, int displayId) {
            synchronized (this) {
                this.mColorListeners.removeIf(new Predicate() {
                    /* class android.app.$$Lambda$WallpaperManager$Globals$2yG7V1sbMECCnlFTLyjKWKqNoYI */

                    @Override // java.util.function.Predicate
                    public final boolean test(Object obj) {
                        return WallpaperManager.Globals.lambda$removeOnColorsChangedListener$0(WallpaperManager.OnColorsChangedListener.this, (Pair) obj);
                    }
                });
                if (this.mColorListeners.size() == 0 && this.mColorCallbackRegistered) {
                    this.mColorCallbackRegistered = false;
                    try {
                        this.mService.unregisterWallpaperColorsCallback(this, userId, displayId);
                    } catch (RemoteException e) {
                        Log.w(WallpaperManager.TAG, "Can't unregister color updates", e);
                    }
                }
            }
        }

        static /* synthetic */ boolean lambda$removeOnColorsChangedListener$0(OnColorsChangedListener callback, Pair pair) {
            return pair.first == callback;
        }

        /* JADX WARN: Multi-variable type inference failed */
        @Override // android.app.IWallpaperManagerCallback
        public void onWallpaperColorsChanged(WallpaperColors colors, int which, int userId) {
            Handler handler;
            synchronized (this) {
                Iterator<Pair<OnColorsChangedListener, Handler>> it = this.mColorListeners.iterator();
                while (it.hasNext()) {
                    Pair<OnColorsChangedListener, Handler> listener = it.next();
                    Handler handler2 = listener.second;
                    if (listener.second == null) {
                        handler = this.mMainLooperHandler;
                    } else {
                        handler = handler2;
                    }
                    handler.post(new Runnable(listener, colors, which, userId) {
                        /* class android.app.$$Lambda$WallpaperManager$Globals$1AcnQUORvPlCjJoNqdxfQT4o4Nw */
                        private final /* synthetic */ Pair f$1;
                        private final /* synthetic */ WallpaperColors f$2;
                        private final /* synthetic */ int f$3;
                        private final /* synthetic */ int f$4;

                        {
                            this.f$1 = r2;
                            this.f$2 = r3;
                            this.f$3 = r4;
                            this.f$4 = r5;
                        }

                        public final void run() {
                            WallpaperManager.Globals.this.lambda$onWallpaperColorsChanged$1$WallpaperManager$Globals(this.f$1, this.f$2, this.f$3, this.f$4);
                        }
                    });
                }
            }
        }

        public /* synthetic */ void lambda$onWallpaperColorsChanged$1$WallpaperManager$Globals(Pair listener, WallpaperColors colors, int which, int userId) {
            boolean stillExists;
            synchronized (WallpaperManager.sGlobals) {
                stillExists = this.mColorListeners.contains(listener);
            }
            if (stillExists) {
                listener.first.onColorsChanged(colors, which, userId);
            }
        }

        /* access modifiers changed from: package-private */
        public WallpaperColors getWallpaperColors(int which, int userId, int displayId) {
            if (which == 2 || which == 1) {
                try {
                    return this.mService.getWallpaperColors(which, userId, displayId);
                } catch (RemoteException e) {
                    return null;
                }
            } else {
                throw new IllegalArgumentException("Must request colors for exactly one kind of wallpaper");
            }
        }

        public Bitmap peekWallpaperBitmap(Context context, boolean returnDefault, int which) {
            return peekWallpaperBitmap(context, returnDefault, which, context.getUserId(), false);
        }

        /* JADX WARNING: Code restructure failed: missing block: B:37:0x0088, code lost:
            if (r8 == false) goto L_0x00af;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:38:0x008a, code lost:
            r1 = r6.mDefaultWallpaper;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:39:0x008c, code lost:
            if (r1 != null) goto L_?;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:40:0x008e, code lost:
            r2 = getDefaultWallpaper(r7, r10);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:41:0x0092, code lost:
            if (r2 == null) goto L_0x00a1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:42:0x0094, code lost:
            r6.wallpaperWidth = r2.getWidth();
            r6.wallpaperHeight = r2.getHeight();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:43:0x00a1, code lost:
            r6.wallpaperWidth = 0;
            r6.wallpaperHeight = 0;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:44:0x00a5, code lost:
            monitor-enter(r6);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:46:?, code lost:
            r6.mDefaultWallpaper = r2;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:47:0x00a8, code lost:
            monitor-exit(r6);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:52:0x00af, code lost:
            return null;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:56:?, code lost:
            return r1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:57:?, code lost:
            return r2;
         */
        public Bitmap peekWallpaperBitmap(Context context, boolean returnDefault, int which, int userId, boolean hardware) {
            IWallpaperManager iWallpaperManager = this.mService;
            if (iWallpaperManager != null) {
                try {
                    if (!iWallpaperManager.isWallpaperSupported(context.getOpPackageName())) {
                        return null;
                    }
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            }
            synchronized (this) {
                if (this.mCachedWallpaper == null || this.mCachedWallpaperUserId != userId || this.mCachedWallpaper.isRecycled()) {
                    this.mCachedWallpaper = null;
                    this.mCachedWallpaperUserId = 0;
                    try {
                        this.mCachedWallpaper = getCurrentWallpaperLocked(context, userId, hardware);
                        this.mCachedWallpaperUserId = userId;
                    } catch (OutOfMemoryError e2) {
                        String str = WallpaperManager.TAG;
                        Log.w(str, "Out of memory loading the current wallpaper: " + e2);
                    } catch (SecurityException e3) {
                        if (context.getApplicationInfo().targetSdkVersion < 27) {
                            Log.w(WallpaperManager.TAG, "No permission to access wallpaper, suppressing exception to avoid crashing legacy app.");
                        } else {
                            throw e3;
                        }
                    }
                    if (this.mCachedWallpaper != null) {
                        this.wallpaperWidth = this.mCachedWallpaper.getWidth();
                        this.wallpaperHeight = this.mCachedWallpaper.getHeight();
                        return this.mCachedWallpaper;
                    }
                    this.wallpaperWidth = 0;
                    this.wallpaperHeight = 0;
                } else {
                    return this.mCachedWallpaper;
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void forgetLoadedWallpaper() {
            synchronized (this) {
                this.mCachedWallpaper = null;
                this.mCachedWallpaperUserId = 0;
                this.mDefaultWallpaper = null;
            }
        }

        private Bitmap getCurrentWallpaperLocked(Context context, int userId, boolean hardware) {
            if (this.mService == null) {
                Log.w(WallpaperManager.TAG, "WallpaperService not running");
                return null;
            }
            try {
                Bundle params = new Bundle();
                ParcelFileDescriptor fd = this.mService.getWallpaper(context.getOpPackageName(), this, 1, params, userId);
                if (fd != null) {
                    int i = 0;
                    int width = params.getInt("width", 0);
                    int height = params.getInt("height", 0);
                    try {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        if (hardware) {
                            options.inPreferredConfig = Bitmap.Config.HARDWARE;
                        }
                        Bitmap bm = this.mService.scaleWallpaperBitmapToScreenSize(BitmapFactory.decodeFileDescriptor(fd.getFileDescriptor(), null, options));
                        if (HwFoldScreenManager.isFoldable()) {
                            String str = WallpaperManager.TAG;
                            StringBuilder sb = new StringBuilder();
                            sb.append(" WallapperManger#getCurrentWallpaperLocked width = ");
                            sb.append(bm != null ? bm.getWidth() : 0);
                            sb.append(" height = ");
                            if (bm != null) {
                                i = bm.getHeight();
                            }
                            sb.append(i);
                            Log.i(str, sb.toString());
                            return bm;
                        }
                        Bitmap generateBitmap = HwThemeManager.generateBitmap(context, bm, width, height);
                        IoUtils.closeQuietly(fd);
                        return generateBitmap;
                    } catch (OutOfMemoryError e) {
                        Log.w(WallpaperManager.TAG, "Can't decode file", e);
                    } finally {
                        IoUtils.closeQuietly(fd);
                    }
                }
                return null;
            } catch (RemoteException e2) {
                throw e2.rethrowFromSystemServer();
            }
        }

        private Bitmap getDefaultWallpaper(Context context, int which) {
            InputStream is;
            InputStream is2 = HwThemeManager.getDefaultWallpaperIS(context, which);
            if (is2 == null) {
                long time1 = System.currentTimeMillis();
                InputStream is3 = WallpaperManager.openDefaultWallpaper(context, 1);
                String str = WallpaperManager.TAG;
                Log.d(str, "getDefaultWallpaper in openDefaultWallpaper cost time: " + (System.currentTimeMillis() - time1) + "ms");
                Log.d(WallpaperManager.TAG, "WallpaperManager getDefaultWallpaper HwThemeManager.getDefaultWallpaperIS is null");
                is = is3;
            } else {
                is = is2;
            }
            if (is == null) {
                return null;
            }
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                long time2 = System.currentTimeMillis();
                Bitmap bm = BitmapFactory.decodeStream(is, null, options);
                String str2 = WallpaperManager.TAG;
                Log.d(str2, "getDefaultWallpaper in decodeStream cost time: " + (System.currentTimeMillis() - time2) + "ms");
                try {
                    if (this.mService != null) {
                        long time3 = System.currentTimeMillis();
                        bm = this.mService.scaleWallpaperBitmapToScreenSize(bm);
                        String str3 = WallpaperManager.TAG;
                        Log.d(str3, "getDefaultWallpaper in scaleWallpaperBitmapToScreenSize cost time: " + (System.currentTimeMillis() - time3) + "ms");
                    }
                } catch (RemoteException e) {
                    Log.w(WallpaperManager.TAG, "scaleWallpaperBitmapToScreenSize fail");
                }
                String[] location = SystemProperties.get("ro.config.wallpaper_offset").split(SmsManager.REGEX_PREFIX_DELIMITER);
                if (location.length == 4 && bm != null) {
                    int xStart = Integer.parseInt(location[0]);
                    int yStart = Integer.parseInt(location[1]);
                    int xWidth = Integer.parseInt(location[2]);
                    int yHeight = Integer.parseInt(location[3]);
                    if (xStart >= 0 && yStart >= 0 && xStart + xWidth <= bm.getWidth() && yStart + yHeight <= bm.getHeight()) {
                        Bitmap bm2 = Bitmap.createBitmap(bm, xStart, yStart, xWidth, yHeight);
                        IoUtils.closeQuietly(is);
                        return bm2;
                    }
                }
                if (bm != null) {
                    long time4 = System.currentTimeMillis();
                    Bitmap bitmap = WallpaperManager.mIHwWallpaperManagerEx.createDefaultWallpaperBitmap(bm);
                    String str4 = WallpaperManager.TAG;
                    Log.d(str4, "getDefaultWallpaper createDefaultWallpaperBitmap cost time: " + (System.currentTimeMillis() - time4) + "ms");
                    IoUtils.closeQuietly(is);
                    return bitmap;
                }
            } catch (OutOfMemoryError e2) {
                Log.w(WallpaperManager.TAG, "Can't decode stream");
            } catch (Throwable th) {
                IoUtils.closeQuietly(is);
                throw th;
            }
            IoUtils.closeQuietly(is);
            return null;
        }

        @Override // android.app.IWallpaperManagerCallback
        public void onBlurWallpaperChanged() {
            synchronized (this) {
                this.mBlurWallpaper = null;
            }
            if (WallpaperManager.mCallbacks != null) {
                int count = WallpaperManager.mCallbacks.size();
                for (int i = 0; i < count; i++) {
                    Object obj = WallpaperManager.mCallbacks.get(i).get();
                    if (obj != null) {
                        try {
                            final Method callback = obj.getClass().getDeclaredMethod("onBlurWallpaperChanged", new Class[0]);
                            AccessController.doPrivileged(new PrivilegedAction() {
                                /* class android.app.WallpaperManager.Globals.AnonymousClass1 */

                                @Override // java.security.PrivilegedAction
                                public Object run() {
                                    callback.setAccessible(true);
                                    return null;
                                }
                            });
                            callback.invoke(obj, new Object[0]);
                        } catch (RuntimeException e) {
                        } catch (Exception ex) {
                            throw new IllegalStateException("Unable to get onBlurWallpaperChanged method", ex);
                        }
                    }
                }
            }
        }

        public Bitmap peekBlurWallpaperBitmap(Rect rect) {
            synchronized (this) {
                if (rect.width() > 0) {
                    if (rect.height() > 0) {
                        if (this.mBlurWallpaper == null || rect.bottom > this.mBlurWallpaper.getHeight()) {
                            this.mBlurWallpaper = null;
                            if (WallpaperManager.DEBUG) {
                                Log.d(WallpaperManager.TAG, "WallpaperManager peekBlurWallpaperBitmap begin");
                            }
                            long timeBegin = System.currentTimeMillis();
                            try {
                                ParcelFileDescriptor fd = this.mService.getBlurWallpaper(this);
                                if (fd != null) {
                                    try {
                                        BitmapFactory.Options options = new BitmapFactory.Options();
                                        if (fd.getFileDescriptor() == null) {
                                            Log.e(WallpaperManager.TAG, "peekBlurWallpaperBitmap() has invalid fd!");
                                            try {
                                                fd.close();
                                            } catch (IOException e) {
                                            }
                                            return null;
                                        }
                                        Bitmap bitmap = BitmapFactory.decodeFileDescriptor(fd.getFileDescriptor(), null, options);
                                        if (bitmap.getWidth() > 0 && bitmap.getHeight() > 0 && rect.bottom > 0) {
                                            this.mBlurWallpaper = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), Math.min(bitmap.getHeight(), rect.bottom));
                                        }
                                        try {
                                            fd.close();
                                        } catch (IOException e2) {
                                        }
                                    } catch (OutOfMemoryError e3) {
                                        this.mBlurWallpaper = null;
                                        Log.w(WallpaperManager.TAG, "Can't decode file", e3);
                                        fd.close();
                                    } catch (Exception ex) {
                                        this.mBlurWallpaper = null;
                                        Log.w(WallpaperManager.TAG, "Can't decode file", ex);
                                        fd.close();
                                    } catch (Throwable th) {
                                        try {
                                            fd.close();
                                        } catch (IOException e4) {
                                        }
                                        throw th;
                                    }
                                }
                            } catch (RemoteException e5) {
                            }
                            if (WallpaperManager.DEBUG) {
                                String str = WallpaperManager.TAG;
                                Log.d(str, "WallpaperManager peekBlurWallpaperBitmap takenTime = " + (System.currentTimeMillis() - timeBegin));
                            }
                            return this.mBlurWallpaper;
                        }
                        return this.mBlurWallpaper;
                    }
                }
                return null;
            }
        }

        public void forgetLoadedBlurWallpaper() {
            synchronized (this) {
                if (this.mBlurWallpaper != null && !this.mBlurWallpaper.isRecycled()) {
                    this.mBlurWallpaper.recycle();
                    this.mBlurWallpaper = null;
                }
            }
        }

        public Bundle peekCurrentWallpaperBounds(Context context) {
            synchronized (this) {
                if (this.mService == null) {
                    Log.w(WallpaperManager.TAG, "WallpaperService not running");
                    return null;
                }
                ParcelFileDescriptor fd = null;
                try {
                    Bundle params = new Bundle();
                    ParcelFileDescriptor fd2 = this.mService.getWallpaper(context.getOpPackageName(), this, 1, params, this.mService.getWallpaperUserId());
                    params.remove("width");
                    params.remove("height");
                    if (fd2 == null) {
                        Log.w(WallpaperManager.TAG, "cannot get ParcelFileDescriptor of current user wallpaper in read only mode, return null!");
                        if (fd2 != null) {
                            try {
                                fd2.close();
                            } catch (IOException e) {
                            }
                        }
                        return null;
                    }
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeFileDescriptor(fd2.getFileDescriptor(), null, options);
                    params.putInt("wallpaper_file_width", options.outWidth);
                    params.putInt("wallpaper_file_height", options.outHeight);
                    if (WallpaperManager.DEBUG) {
                        String str = WallpaperManager.TAG;
                        Log.d(str, "getUserWallpaperBounds return wallpaper_file_width=" + params.getInt("wallpaper_file_width") + ", wallpaper_file_height=" + params.getInt("wallpaper_file_height"));
                    }
                    try {
                        fd2.close();
                    } catch (IOException e2) {
                    }
                    return params;
                } catch (RemoteException e3) {
                    if (0 != 0) {
                        fd.close();
                    }
                } catch (OutOfMemoryError oome) {
                    Log.e(WallpaperManager.TAG, "No memory load current wallpaper bounds", oome);
                    if (0 != 0) {
                        fd.close();
                    }
                } catch (Exception e4) {
                    Log.e(WallpaperManager.TAG, "Error while getting current wallpaper bounds", e4);
                    if (0 != 0) {
                        try {
                            fd.close();
                        } catch (IOException e5) {
                        }
                    }
                } catch (Throwable th) {
                    if (0 != 0) {
                        try {
                            fd.close();
                        } catch (IOException e6) {
                        }
                    }
                    throw th;
                }
            }
            return null;
        }

        public synchronized int getWallpaperWidth() {
            return this.wallpaperWidth;
        }

        public synchronized int getWallpaperHeight() {
            return this.wallpaperHeight;
        }
    }

    static void initGlobals(IWallpaperManager service, Looper looper) {
        synchronized (sSync) {
            if (sGlobals == null) {
                sGlobals = new Globals(service, looper);
            }
        }
    }

    WallpaperManager(IWallpaperManager service, Context context, Handler handler) {
        this.mContext = context;
        initGlobals(service, context.getMainLooper());
        mIHwWallpaperManagerEx = HwFrameworkFactory.getHuaweiWallpaperManager(context, service, this);
    }

    public static WallpaperManager getInstance(Context context) {
        return (WallpaperManager) context.getSystemService("wallpaper");
    }

    @UnsupportedAppUsage
    public IWallpaperManager getIWallpaperManager() {
        return sGlobals.mService;
    }

    public Drawable getDrawable() {
        Bitmap bm = sGlobals.peekWallpaperBitmap(this.mContext, true, 1);
        if (bm == null) {
            return null;
        }
        Drawable dr = new BitmapDrawable(this.mContext.getResources(), bm);
        dr.setDither(false);
        return dr;
    }

    public Drawable getBuiltInDrawable() {
        return getBuiltInDrawable(0, 0, false, 0.0f, 0.0f, 1);
    }

    public Drawable getBuiltInDrawable(int which) {
        return getBuiltInDrawable(0, 0, false, 0.0f, 0.0f, which);
    }

    public Drawable getBuiltInDrawable(int outWidth, int outHeight, boolean scaleToFit, float horizontalAlignment, float verticalAlignment) {
        return getBuiltInDrawable(outWidth, outHeight, scaleToFit, horizontalAlignment, verticalAlignment, 1);
    }

    /* JADX WARN: Type inference failed for: r1v3, types: [android.graphics.Rect, android.graphics.BitmapFactory$Options] */
    /* JADX WARN: Type inference failed for: r1v5 */
    /* JADX WARN: Type inference failed for: r1v18 */
    public Drawable getBuiltInDrawable(int outWidth, int outHeight, boolean scaleToFit, float horizontalAlignment, float verticalAlignment, int which) {
        ?? r1;
        RectF cropRectF;
        int outWidth2;
        InputStream is;
        if (sGlobals.mService == null) {
            Log.w(TAG, "WallpaperService not running");
            throw new RuntimeException(new DeadSystemException());
        } else if (which == 1 || which == 2) {
            Resources resources = this.mContext.getResources();
            float horizontalAlignment2 = Math.max(0.0f, Math.min(1.0f, horizontalAlignment));
            float verticalAlignment2 = Math.max(0.0f, Math.min(1.0f, verticalAlignment));
            InputStream wpStream = openDefaultWallpaper(this.mContext, which);
            if (wpStream == null) {
                if (DEBUG) {
                    Log.w(TAG, "default wallpaper stream " + which + " is null");
                }
                return null;
            }
            InputStream is2 = new BufferedInputStream(wpStream);
            if (outWidth <= 0) {
                r1 = 0;
            } else if (outHeight <= 0) {
                r1 = 0;
            } else {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(is2, null, options);
                if (options.outWidth == 0 || options.outHeight == 0) {
                    Log.e(TAG, "default wallpaper dimensions are 0");
                    return null;
                }
                int inWidth = options.outWidth;
                int inHeight = options.outHeight;
                InputStream is3 = new BufferedInputStream(openDefaultWallpaper(this.mContext, which));
                int outWidth3 = Math.min(inWidth, outWidth);
                int outHeight2 = Math.min(inHeight, outHeight);
                if (scaleToFit) {
                    is = is3;
                    outWidth2 = outWidth3;
                    cropRectF = getMaxCropRect(inWidth, inHeight, outWidth3, outHeight2, horizontalAlignment2, verticalAlignment2);
                } else {
                    is = is3;
                    outWidth2 = outWidth3;
                    float left = ((float) (inWidth - outWidth2)) * horizontalAlignment2;
                    float top = ((float) (inHeight - outHeight2)) * verticalAlignment2;
                    cropRectF = new RectF(left, top, ((float) outWidth2) + left, ((float) outHeight2) + top);
                }
                Rect roundedTrueCrop = new Rect();
                cropRectF.roundOut(roundedTrueCrop);
                if (roundedTrueCrop.width() > 0) {
                    if (roundedTrueCrop.height() > 0) {
                        int scaleDownSampleSize = Math.min(roundedTrueCrop.width() / outWidth2, roundedTrueCrop.height() / outHeight2);
                        BitmapRegionDecoder decoder = null;
                        try {
                            decoder = BitmapRegionDecoder.newInstance(is, true);
                        } catch (IOException e) {
                            Log.w(TAG, "cannot open region decoder for default wallpaper");
                        }
                        Bitmap crop = null;
                        if (decoder != null) {
                            BitmapFactory.Options options2 = new BitmapFactory.Options();
                            if (scaleDownSampleSize > 1) {
                                options2.inSampleSize = scaleDownSampleSize;
                            }
                            crop = decoder.decodeRegion(roundedTrueCrop, options2);
                            decoder.recycle();
                        }
                        if (crop == null) {
                            InputStream is4 = new BufferedInputStream(openDefaultWallpaper(this.mContext, which));
                            BitmapFactory.Options options3 = new BitmapFactory.Options();
                            if (scaleDownSampleSize > 1) {
                                options3.inSampleSize = scaleDownSampleSize;
                            }
                            Bitmap fullSize = BitmapFactory.decodeStream(is4, null, options3);
                            if (fullSize != null) {
                                crop = Bitmap.createBitmap(fullSize, roundedTrueCrop.left, roundedTrueCrop.top, roundedTrueCrop.width(), roundedTrueCrop.height());
                            }
                        }
                        if (crop == null) {
                            Log.w(TAG, "cannot decode default wallpaper");
                            return null;
                        }
                        if (outWidth2 > 0 && outHeight2 > 0) {
                            if (crop.getWidth() != outWidth2 || crop.getHeight() != outHeight2) {
                                Matrix m = new Matrix();
                                RectF cropRect = new RectF(0.0f, 0.0f, (float) crop.getWidth(), (float) crop.getHeight());
                                RectF returnRect = new RectF(0.0f, 0.0f, (float) outWidth2, (float) outHeight2);
                                m.setRectToRect(cropRect, returnRect, Matrix.ScaleToFit.FILL);
                                Bitmap tmp = Bitmap.createBitmap((int) returnRect.width(), (int) returnRect.height(), Bitmap.Config.ARGB_8888);
                                if (tmp != null) {
                                    Canvas c = new Canvas(tmp);
                                    Paint p = new Paint();
                                    p.setFilterBitmap(true);
                                    c.drawBitmap(crop, m, p);
                                    crop = tmp;
                                }
                            }
                        }
                        return new BitmapDrawable(resources, crop);
                    }
                }
                Log.w(TAG, "crop has bad values for full size image");
                return null;
            }
            return new BitmapDrawable(resources, BitmapFactory.decodeStream(is2, r1, r1));
        } else {
            throw new IllegalArgumentException("Must request exactly one kind of wallpaper");
        }
    }

    private static RectF getMaxCropRect(int inWidth, int inHeight, int outWidth, int outHeight, float horizontalAlignment, float verticalAlignment) {
        RectF cropRect = new RectF();
        if (((float) inWidth) / ((float) inHeight) > ((float) outWidth) / ((float) outHeight)) {
            cropRect.top = 0.0f;
            cropRect.bottom = (float) inHeight;
            float cropWidth = ((float) outWidth) * (((float) inHeight) / ((float) outHeight));
            cropRect.left = (((float) inWidth) - cropWidth) * horizontalAlignment;
            cropRect.right = cropRect.left + cropWidth;
        } else {
            cropRect.left = 0.0f;
            cropRect.right = (float) inWidth;
            float cropHeight = ((float) outHeight) * (((float) inWidth) / ((float) outWidth));
            cropRect.top = (((float) inHeight) - cropHeight) * verticalAlignment;
            cropRect.bottom = cropRect.top + cropHeight;
        }
        return cropRect;
    }

    public Drawable peekDrawable() {
        Bitmap bm = sGlobals.peekWallpaperBitmap(this.mContext, false, 1);
        if (bm == null) {
            return null;
        }
        Drawable dr = new BitmapDrawable(this.mContext.getResources(), bm);
        dr.setDither(false);
        return dr;
    }

    public Drawable getFastDrawable() {
        Bitmap bm = sGlobals.peekWallpaperBitmap(this.mContext, true, 1);
        if (bm != null) {
            return new FastBitmapDrawable(bm);
        }
        return null;
    }

    public Drawable peekFastDrawable() {
        Bitmap bm = sGlobals.peekWallpaperBitmap(this.mContext, false, 1);
        if (bm != null) {
            return new FastBitmapDrawable(bm);
        }
        return null;
    }

    @UnsupportedAppUsage
    public Bitmap getBitmap() {
        return getBitmap(false);
    }

    @UnsupportedAppUsage
    public Bitmap getBitmap(boolean hardware) {
        if (HwPCUtils.enabledInPad() && HwPCUtils.isValidExtDisplayId(this.mContext)) {
            forgetLoadedWallpaper();
        }
        return getBitmapAsUser(this.mContext.getUserId(), hardware);
    }

    public Bitmap getBitmapAsUser(int userId, boolean hardware) {
        return sGlobals.peekWallpaperBitmap(this.mContext, true, 1, userId, hardware);
    }

    public ParcelFileDescriptor getWallpaperFile(int which) {
        return getWallpaperFile(which, this.mContext.getUserId());
    }

    public void addOnColorsChangedListener(OnColorsChangedListener listener, Handler handler) {
        addOnColorsChangedListener(listener, handler, this.mContext.getUserId());
    }

    @UnsupportedAppUsage
    public void addOnColorsChangedListener(OnColorsChangedListener listener, Handler handler, int userId) {
        sGlobals.addOnColorsChangedListener(listener, handler, userId, this.mContext.getDisplayId());
    }

    public void removeOnColorsChangedListener(OnColorsChangedListener callback) {
        removeOnColorsChangedListener(callback, this.mContext.getUserId());
    }

    public void removeOnColorsChangedListener(OnColorsChangedListener callback, int userId) {
        sGlobals.removeOnColorsChangedListener(callback, userId, this.mContext.getDisplayId());
    }

    public WallpaperColors getWallpaperColors(int which) {
        return getWallpaperColors(which, this.mContext.getUserId());
    }

    @UnsupportedAppUsage
    public WallpaperColors getWallpaperColors(int which, int userId) {
        return sGlobals.getWallpaperColors(which, userId, this.mContext.getDisplayId());
    }

    @UnsupportedAppUsage
    public ParcelFileDescriptor getWallpaperFile(int which, int userId) {
        if (which != 1 && which != 2) {
            throw new IllegalArgumentException("Must request exactly one kind of wallpaper");
        } else if (sGlobals.mService != null) {
            try {
                return sGlobals.mService.getWallpaper(this.mContext.getOpPackageName(), null, which, new Bundle(), userId);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            } catch (SecurityException e2) {
                if (this.mContext.getApplicationInfo().targetSdkVersion < 27) {
                    Log.w(TAG, "No permission to access wallpaper, suppressing exception to avoid crashing legacy app.");
                    return null;
                }
                throw e2;
            }
        } else {
            Log.w(TAG, "WallpaperService not running");
            throw new RuntimeException(new DeadSystemException());
        }
    }

    public void forgetLoadedWallpaper() {
        sGlobals.forgetLoadedWallpaper();
    }

    public WallpaperInfo getWallpaperInfo() {
        return getWallpaperInfo(this.mContext.getUserId());
    }

    public WallpaperInfo getWallpaperInfo(int userId) {
        try {
            if (sGlobals.mService != null) {
                return sGlobals.mService.getWallpaperInfo(userId);
            }
            Log.w(TAG, "WallpaperService not running");
            throw new RuntimeException(new DeadSystemException());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getWallpaperId(int which) {
        return getWallpaperIdForUser(which, this.mContext.getUserId());
    }

    public int getWallpaperIdForUser(int which, int userId) {
        try {
            if (sGlobals.mService != null) {
                return sGlobals.mService.getWallpaperIdForUser(which, userId);
            }
            Log.w(TAG, "WallpaperService not running");
            throw new RuntimeException(new DeadSystemException());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public Intent getCropAndSetWallpaperIntent(Uri imageUri) {
        if (imageUri == null) {
            throw new IllegalArgumentException("Image URI must not be null");
        } else if ("content".equals(imageUri.getScheme())) {
            PackageManager packageManager = this.mContext.getPackageManager();
            Intent cropAndSetWallpaperIntent = new Intent(ACTION_CROP_AND_SET_WALLPAPER, imageUri);
            cropAndSetWallpaperIntent.addFlags(1);
            ResolveInfo resolvedHome = packageManager.resolveActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME), 65536);
            if (resolvedHome != null) {
                cropAndSetWallpaperIntent.setPackage(resolvedHome.activityInfo.packageName);
                if (packageManager.queryIntentActivities(cropAndSetWallpaperIntent, 0).size() > 0) {
                    return cropAndSetWallpaperIntent;
                }
            }
            cropAndSetWallpaperIntent.setPackage(this.mContext.getString(R.string.config_wallpaperCropperPackage));
            if (packageManager.queryIntentActivities(cropAndSetWallpaperIntent, 0).size() > 0) {
                return cropAndSetWallpaperIntent;
            }
            throw new IllegalArgumentException("Cannot use passed URI to set wallpaper; check that the type returned by ContentProvider matches image/*");
        } else {
            throw new IllegalArgumentException("Image URI must be of the content scheme type");
        }
    }

    public void setResource(int resid) throws IOException {
        setResource(resid, 3);
    }

    public int setResource(int resid, int which) throws IOException {
        if (sGlobals.mService != null) {
            Bundle result = new Bundle();
            WallpaperSetCompletion completion = new WallpaperSetCompletion();
            try {
                Resources resources = this.mContext.getResources();
                IWallpaperManager iWallpaperManager = sGlobals.mService;
                ParcelFileDescriptor fd = iWallpaperManager.setWallpaper("res:" + resources.getResourceName(resid), this.mContext.getOpPackageName(), null, false, result, which, completion, this.mContext.getUserId());
                if (fd != null) {
                    FileOutputStream fos = null;
                    try {
                        fos = new ParcelFileDescriptor.AutoCloseOutputStream(fd);
                        copyStreamToWallpaperFile(resources.openRawResource(resid), fos);
                        fos.close();
                        completion.waitForCompletion();
                    } finally {
                        IoUtils.closeQuietly(fos);
                    }
                }
                return result.getInt(EXTRA_NEW_WALLPAPER_ID, 0);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            Log.w(TAG, "WallpaperService not running");
            throw new RuntimeException(new DeadSystemException());
        }
    }

    public void setBitmap(Bitmap bitmap) throws IOException {
        setBitmap(bitmap, null, true);
    }

    public int setBitmap(Bitmap fullImage, Rect visibleCropHint, boolean allowBackup) throws IOException {
        return setBitmap(fullImage, visibleCropHint, allowBackup, 3);
    }

    public int setBitmap(Bitmap fullImage, Rect visibleCropHint, boolean allowBackup, int which) throws IOException {
        return setBitmap(fullImage, visibleCropHint, allowBackup, which, this.mContext.getUserId());
    }

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public int setBitmap(Bitmap fullImage, Rect visibleCropHint, boolean allowBackup, int which, int userId) throws IOException {
        validateRect(visibleCropHint);
        if (sGlobals.mService != null) {
            Bundle result = new Bundle();
            WallpaperSetCompletion completion = new WallpaperSetCompletion();
            try {
                ParcelFileDescriptor fd = sGlobals.mService.setWallpaper(null, this.mContext.getOpPackageName(), visibleCropHint, allowBackup, result, which, completion, userId);
                if (fd != null) {
                    FileOutputStream fos = null;
                    try {
                        fos = new ParcelFileDescriptor.AutoCloseOutputStream(fd);
                        fullImage.compress(Bitmap.CompressFormat.PNG, 90, fos);
                        fos.close();
                        completion.waitForCompletion();
                    } finally {
                        IoUtils.closeQuietly(fos);
                    }
                }
                return result.getInt(EXTRA_NEW_WALLPAPER_ID, 0);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            Log.w(TAG, "WallpaperService not running");
            throw new RuntimeException(new DeadSystemException());
        }
    }

    private final void validateRect(Rect rect) {
        if (rect != null && rect.isEmpty()) {
            throw new IllegalArgumentException("visibleCrop rectangle must be valid and non-empty");
        }
    }

    public void setStream(InputStream bitmapData) throws IOException {
        setStream(bitmapData, null, true);
    }

    private void copyStreamToWallpaperFile(InputStream data, FileOutputStream fos) throws IOException {
        FileUtils.copy(data, fos);
    }

    public int setStream(InputStream bitmapData, Rect visibleCropHint, boolean allowBackup) throws IOException {
        return setStream(bitmapData, visibleCropHint, allowBackup, 3);
    }

    public int setStream(InputStream bitmapData, Rect visibleCropHint, boolean allowBackup, int which) throws IOException {
        validateRect(visibleCropHint);
        if (sGlobals.mService != null) {
            Bundle result = new Bundle();
            WallpaperSetCompletion completion = new WallpaperSetCompletion();
            try {
                ParcelFileDescriptor fd = sGlobals.mService.setWallpaper(null, this.mContext.getOpPackageName(), visibleCropHint, allowBackup, result, which, completion, this.mContext.getUserId());
                if (fd != null) {
                    FileOutputStream fos = null;
                    try {
                        fos = new ParcelFileDescriptor.AutoCloseOutputStream(fd);
                        copyStreamToWallpaperFile(bitmapData, fos);
                        fos.close();
                        completion.waitForCompletion();
                    } finally {
                        IoUtils.closeQuietly(fos);
                    }
                }
                return result.getInt(EXTRA_NEW_WALLPAPER_ID, 0);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            Log.w(TAG, "WallpaperService not running");
            throw new RuntimeException(new DeadSystemException());
        }
    }

    public boolean hasResourceWallpaper(int resid) {
        if (sGlobals.mService != null) {
            try {
                Resources resources = this.mContext.getResources();
                return sGlobals.mService.hasNamedWallpaper("res:" + resources.getResourceName(resid));
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            Log.w(TAG, "WallpaperService not running");
            throw new RuntimeException(new DeadSystemException());
        }
    }

    public int getDesiredMinimumWidth() {
        if (sGlobals.mService != null) {
            try {
                return sGlobals.mService.getWidthHint(this.mContext.getDisplayId());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            Log.w(TAG, "WallpaperService not running");
            throw new RuntimeException(new DeadSystemException());
        }
    }

    public int getDesiredMinimumHeight() {
        if (sGlobals.mService != null) {
            try {
                return sGlobals.mService.getHeightHint(this.mContext.getDisplayId());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            Log.w(TAG, "WallpaperService not running");
            throw new RuntimeException(new DeadSystemException());
        }
    }

    public void suggestDesiredDimensions(int minimumWidth, int minimumHeight) {
        int maximumTextureSize;
        try {
            maximumTextureSize = SystemProperties.getInt("sys.max_texture_size", 0);
        } catch (Exception e) {
            maximumTextureSize = 0;
        }
        if (maximumTextureSize > 0 && (minimumWidth > maximumTextureSize || minimumHeight > maximumTextureSize)) {
            float aspect = ((float) minimumHeight) / ((float) minimumWidth);
            if (minimumWidth > minimumHeight) {
                minimumWidth = maximumTextureSize;
                minimumHeight = (int) (((double) (((float) minimumWidth) * aspect)) + 0.5d);
            } else {
                minimumHeight = maximumTextureSize;
                minimumWidth = (int) (((double) (((float) minimumHeight) / aspect)) + 0.5d);
            }
        }
        try {
            if (sGlobals.mService != null) {
                sGlobals.mService.setDimensionHints(minimumWidth, minimumHeight, this.mContext.getOpPackageName(), this.mContext.getDisplayId());
            } else {
                Log.w(TAG, "WallpaperService not running");
                throw new RuntimeException(new DeadSystemException());
            }
        } catch (RemoteException e2) {
            throw e2.rethrowFromSystemServer();
        }
    }

    public void setDisplayPadding(Rect padding) {
        try {
            if (sGlobals.mService != null) {
                sGlobals.mService.setDisplayPadding(padding, this.mContext.getOpPackageName(), this.mContext.getDisplayId());
            } else {
                Log.w(TAG, "WallpaperService not running");
                throw new RuntimeException(new DeadSystemException());
            }
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public void setDisplayOffset(IBinder windowToken, int x, int y) {
        try {
            WindowManagerGlobal.getWindowSession().setWallpaperDisplayOffset(windowToken, x, y);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void clearWallpaper() {
        clearWallpaper(2, this.mContext.getUserId());
        clearWallpaper(1, this.mContext.getUserId());
    }

    @SystemApi
    public void clearWallpaper(int which, int userId) {
        if (sGlobals.mService != null) {
            try {
                sGlobals.mService.clearWallpaper(this.mContext.getOpPackageName(), which, userId);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            Log.w(TAG, "WallpaperService not running");
            throw new RuntimeException(new DeadSystemException());
        }
    }

    @SystemApi
    public boolean setWallpaperComponent(ComponentName name) {
        return setWallpaperComponent(name, this.mContext.getUserId());
    }

    @UnsupportedAppUsage
    public boolean setWallpaperComponent(ComponentName name, int userId) {
        if (sGlobals.mService != null) {
            try {
                sGlobals.mService.setWallpaperComponentChecked(name, this.mContext.getOpPackageName(), userId);
                return true;
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            Log.w(TAG, "WallpaperService not running");
            throw new RuntimeException(new DeadSystemException());
        }
    }

    public void setWallpaperOffsets(IBinder windowToken, float xOffset, float yOffset) {
        int wallpaperHeight;
        int wallpaperWidth;
        boolean is_scroll;
        try {
            Display display = ((WindowManager) getContext().getSystemService("window")).getDefaultDisplay();
            Point size = new Point();
            display.getRealSize(size);
            int width = size.y > size.x ? size.x : size.y;
            int height = size.y > size.x ? size.y : size.x;
            int wallpaperWidth2 = getWallpaperWidth();
            int wallpaperHeight2 = getWallpaperHeight();
            if (wallpaperWidth2 == 0 && wallpaperHeight2 == 0) {
                try {
                    Bitmap currentWallpaper = getBitmap();
                    if (currentWallpaper != null) {
                        int wallpaperWidth3 = currentWallpaper.getWidth();
                        int wallpaperHeight3 = currentWallpaper.getHeight();
                        wallpaperWidth = wallpaperWidth3;
                        wallpaperHeight = wallpaperHeight3;
                        boolean is_scroll2 = false;
                        boolean isSquare = wallpaperWidth != height && wallpaperHeight == height;
                        if (height >= width * 2 || !isSquare) {
                            is_scroll = false;
                        } else {
                            if (Settings.System.getInt(this.mContext.getContentResolver(), "is_scroll", -1) == 1) {
                                is_scroll2 = true;
                            }
                            is_scroll = is_scroll2;
                        }
                        if ((width == wallpaperWidth || height != wallpaperHeight) && (!isSquare || is_scroll)) {
                            Log.i(TAG, "setWallpaperOffsets second wallpaper =[" + xOffset + SmsManager.REGEX_PREFIX_DELIMITER + yOffset + SmsManager.REGEX_PREFIX_DELIMITER + this.mWallpaperXStep + SmsManager.REGEX_PREFIX_DELIMITER + this.mWallpaperYStep + "]");
                            WindowManagerGlobal.getWindowSession().setWallpaperPosition(windowToken, xOffset, yOffset, this.mWallpaperXStep, this.mWallpaperYStep);
                        }
                        boolean isTahDevice = HwFoldScreenManager.isFoldable();
                        int displayMode = HwFoldScreenManager.getDisplayMode();
                        if (!isTahDevice || displayMode != 2) {
                            Log.i(TAG, "setWallpaperOffsets wallpaper = [0]");
                            WindowManagerGlobal.getWindowSession().setWallpaperPosition(windowToken, 0.0f, 0.0f, 0.0f, 0.0f);
                            return;
                        }
                        Log.i(TAG, "setWallpaperOffsets wallpaper = [" + xOffset + SmsManager.REGEX_PREFIX_DELIMITER + yOffset + SmsManager.REGEX_PREFIX_DELIMITER + this.mWallpaperXStep + SmsManager.REGEX_PREFIX_DELIMITER + this.mWallpaperYStep + "]");
                        WindowManagerGlobal.getWindowSession().setWallpaperPosition(windowToken, xOffset, yOffset, this.mWallpaperXStep, this.mWallpaperYStep);
                        return;
                    }
                } catch (SecurityException e) {
                    Log.w(TAG, "setWallpaperOffsets first wallpaper:" + e.getMessage());
                    WindowManagerGlobal.getWindowSession().setWallpaperPosition(windowToken, 0.0f, 0.0f, 0.0f, 0.0f);
                    return;
                }
            }
            wallpaperWidth = wallpaperWidth2;
            wallpaperHeight = wallpaperHeight2;
            boolean is_scroll22 = false;
            if (wallpaperWidth != height) {
            }
            if (height >= width * 2) {
            }
            is_scroll = false;
            if (width == wallpaperWidth) {
            }
            Log.i(TAG, "setWallpaperOffsets second wallpaper =[" + xOffset + SmsManager.REGEX_PREFIX_DELIMITER + yOffset + SmsManager.REGEX_PREFIX_DELIMITER + this.mWallpaperXStep + SmsManager.REGEX_PREFIX_DELIMITER + this.mWallpaperYStep + "]");
            WindowManagerGlobal.getWindowSession().setWallpaperPosition(windowToken, xOffset, yOffset, this.mWallpaperXStep, this.mWallpaperYStep);
        } catch (RemoteException e2) {
            throw e2.rethrowFromSystemServer();
        }
    }

    public void setWallpaperOffsetSteps(float xStep, float yStep) {
        this.mWallpaperXStep = xStep;
        this.mWallpaperYStep = yStep;
    }

    public void sendWallpaperCommand(IBinder windowToken, String action, int x, int y, int z, Bundle extras) {
        try {
            WindowManagerGlobal.getWindowSession().sendWallpaperCommand(windowToken, action, x, y, z, extras, false);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isWallpaperSupported() {
        if (sGlobals.mService != null) {
            try {
                return sGlobals.mService.isWallpaperSupported(this.mContext.getOpPackageName());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            Log.w(TAG, "WallpaperService not running");
            throw new RuntimeException(new DeadSystemException());
        }
    }

    public boolean isSetWallpaperAllowed() {
        if (sGlobals.mService != null) {
            try {
                return sGlobals.mService.isSetWallpaperAllowed(this.mContext.getOpPackageName());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            Log.w(TAG, "WallpaperService not running");
            throw new RuntimeException(new DeadSystemException());
        }
    }

    public void clearWallpaperOffsets(IBinder windowToken) {
        try {
            WindowManagerGlobal.getWindowSession().setWallpaperPosition(windowToken, -1.0f, -1.0f, -1.0f, -1.0f);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void clear() throws IOException {
        setStream(openDefaultWallpaper(this.mContext, 1), null, false);
    }

    public void clear(int which) throws IOException {
        if ((which & 1) != 0) {
            clear();
        }
        if ((which & 2) != 0) {
            clearWallpaper(2, this.mContext.getUserId());
        }
    }

    @UnsupportedAppUsage
    public static InputStream openDefaultWallpaper(Context context, int which) {
        if (which == 2) {
            return null;
        }
        String path = SystemProperties.get(PROP_WALLPAPER);
        if (!TextUtils.isEmpty(path)) {
            File file = new File(path);
            if (file.exists()) {
                try {
                    return new FileInputStream(file);
                } catch (IOException e) {
                }
            }
        }
        try {
            return context.getResources().openRawResource(R.drawable.default_wallpaper);
        } catch (Resources.NotFoundException e2) {
            return null;
        }
    }

    public static ComponentName getDefaultWallpaperComponent(Context context) {
        ComponentName cn = null;
        String flat = SystemProperties.get(PROP_WALLPAPER_COMPONENT);
        if (TextUtils.isEmpty(flat)) {
            flat = HwThemeManager.getDefaultLiveWallpaper(UserHandle.myUserId());
        }
        if (!TextUtils.isEmpty(flat)) {
            cn = ComponentName.unflattenFromString(flat);
        }
        if (cn == null) {
            String flat2 = context.getString(R.string.default_wallpaper_component);
            if (!TextUtils.isEmpty(flat2)) {
                cn = ComponentName.unflattenFromString(flat2);
            }
        }
        if (cn == null) {
            return cn;
        }
        try {
            context.getPackageManager().getPackageInfo(cn.getPackageName(), 786432);
            return cn;
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    public static ComponentName getDefaultWallpaperComponent(int userId) {
        ComponentName cn;
        String flat = HwThemeManager.getDefaultLiveWallpaper(userId);
        if (TextUtils.isEmpty(flat) || (cn = ComponentName.unflattenFromString(flat)) == null) {
            return null;
        }
        return cn;
    }

    public boolean setLockWallpaperCallback(IWallpaperManagerCallback callback) {
        if (sGlobals.mService != null) {
            try {
                return sGlobals.mService.setLockWallpaperCallback(callback);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            Log.w(TAG, "WallpaperService not running");
            throw new RuntimeException(new DeadSystemException());
        }
    }

    public boolean isWallpaperBackupEligible(int which) {
        if (sGlobals.mService != null) {
            try {
                return sGlobals.mService.isWallpaperBackupEligible(which, this.mContext.getUserId());
            } catch (RemoteException e) {
                String str = TAG;
                Log.e(str, "Exception querying wallpaper backup eligibility: " + e.getMessage());
                return false;
            }
        } else {
            Log.w(TAG, "WallpaperService not running");
            throw new RuntimeException(new DeadSystemException());
        }
    }

    /* access modifiers changed from: private */
    public class WallpaperSetCompletion extends IWallpaperManagerCallback.Stub {
        final CountDownLatch mLatch = new CountDownLatch(1);

        public WallpaperSetCompletion() {
        }

        public void waitForCompletion() {
            try {
                this.mLatch.await(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
            }
        }

        @Override // android.app.IWallpaperManagerCallback
        public void onWallpaperChanged() throws RemoteException {
            this.mLatch.countDown();
        }

        @Override // android.app.IWallpaperManagerCallback
        public void onWallpaperColorsChanged(WallpaperColors colors, int which, int userId) throws RemoteException {
            WallpaperManager.sGlobals.onWallpaperColorsChanged(colors, which, userId);
        }

        @Override // android.app.IWallpaperManagerCallback
        public void onBlurWallpaperChanged() throws RemoteException {
        }
    }

    public interface OnColorsChangedListener {
        void onColorsChanged(WallpaperColors wallpaperColors, int i);

        default void onColorsChanged(WallpaperColors colors, int which, int userId) {
            onColorsChanged(colors, which);
        }
    }

    public Context getContext() {
        return this.mContext;
    }

    public Bitmap peekBlurWallpaperBitmap(Rect rect) {
        return sGlobals.peekBlurWallpaperBitmap(rect);
    }

    public Bundle getUserWallpaperBounds(Context context) {
        return sGlobals.peekCurrentWallpaperBounds(context);
    }

    public void setCallback(Object callback) {
        int count = mCallbacks.size();
        for (int i = 0; i < count; i++) {
            if (mCallbacks.get(i).get() == callback) {
                return;
            }
        }
        mCallbacks.add(new WeakReference<>(callback));
    }

    public void forgetLoadedBlurWallpaper() {
        sGlobals.forgetLoadedBlurWallpaper();
    }

    public int[] getWallpaperStartingPoints() {
        int[] defaultOffsets = {-1, -1, -1, -1};
        int[] offsets = null;
        if (sGlobals.mService != null) {
            try {
                offsets = sGlobals.mService.getCurrOffsets();
            } catch (RemoteException e) {
            }
        }
        return offsets == null ? defaultOffsets : offsets;
    }

    public void setBitmapWithOffsets(Bitmap bitmap, int[] offsets) throws IOException {
        mIHwWallpaperManagerEx.setWallpaperStartingPoints(offsets);
        setBitmap(bitmap);
    }

    public void setStreamWithOffsets(InputStream data, int[] offsets) throws IOException {
        mIHwWallpaperManagerEx.setWallpaperStartingPoints(offsets);
        setStream(data);
    }

    private int getWallpaperWidth() {
        return sGlobals.getWallpaperWidth();
    }

    private int getWallpaperHeight() {
        return sGlobals.getWallpaperHeight();
    }

    public Bitmap getBlurBitmap(Rect rect) {
        return mIHwWallpaperManagerEx.getBlurBitmap(rect);
    }
}
