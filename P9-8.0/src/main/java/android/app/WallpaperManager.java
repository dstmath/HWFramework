package android.app;

import android.app.IWallpaperManagerCallback.Stub;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Matrix.ScaleToFit;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.camera2.params.TonemapCurve;
import android.hwtheme.HwThemeManager;
import android.media.MediaFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.DeadSystemException;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.os.ParcelFileDescriptor.AutoCloseOutputStream;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.System;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import com.google.android.collect.Lists;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
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
    private static Globals sGlobals;
    private static final Object sSync = new Object[0];
    private final Context mContext;
    private float mWallpaperXStep = -1.0f;
    private float mWallpaperYStep = -1.0f;

    static class FastBitmapDrawable extends Drawable {
        private final Bitmap mBitmap;
        private int mDrawLeft;
        private int mDrawTop;
        private final int mHeight;
        private final Paint mPaint;
        private final int mWidth;

        /* synthetic */ FastBitmapDrawable(Bitmap bitmap, FastBitmapDrawable -this1) {
            this(bitmap);
        }

        private FastBitmapDrawable(Bitmap bitmap) {
            this.mBitmap = bitmap;
            this.mWidth = bitmap.getWidth();
            this.mHeight = bitmap.getHeight();
            setBounds(0, 0, this.mWidth, this.mHeight);
            this.mPaint = new Paint();
            this.mPaint.setXfermode(new PorterDuffXfermode(Mode.SRC));
        }

        public void draw(Canvas canvas) {
            canvas.drawBitmap(this.mBitmap, (float) this.mDrawLeft, (float) this.mDrawTop, this.mPaint);
        }

        public int getOpacity() {
            return -1;
        }

        public void setBounds(int left, int top, int right, int bottom) {
            this.mDrawLeft = (((right - left) - this.mWidth) / 2) + left;
            this.mDrawTop = (((bottom - top) - this.mHeight) / 2) + top;
        }

        public void setAlpha(int alpha) {
            throw new UnsupportedOperationException("Not supported with this drawable");
        }

        public void setColorFilter(ColorFilter colorFilter) {
            throw new UnsupportedOperationException("Not supported with this drawable");
        }

        public void setDither(boolean dither) {
            throw new UnsupportedOperationException("Not supported with this drawable");
        }

        public void setFilterBitmap(boolean filter) {
            throw new UnsupportedOperationException("Not supported with this drawable");
        }

        public int getIntrinsicWidth() {
            return this.mWidth;
        }

        public int getIntrinsicHeight() {
            return this.mHeight;
        }

        public int getMinimumWidth() {
            return this.mWidth;
        }

        public int getMinimumHeight() {
            return this.mHeight;
        }
    }

    static class Globals extends Stub {
        private static final int MSG_CLEAR_WALLPAPER = 1;
        private Bitmap mBlurWallpaper;
        private Bitmap mCachedWallpaper;
        private int mCachedWallpaperUserId;
        private Bitmap mDefaultWallpaper;
        private final IWallpaperManager mService = IWallpaperManager.Stub.asInterface(ServiceManager.getService("wallpaper"));
        private int wallpaperHeight = 0;
        private int wallpaperWidth = 0;

        Globals(Looper looper) {
            forgetLoadedWallpaper();
        }

        public void onWallpaperChanged() {
            forgetLoadedWallpaper();
        }

        public Bitmap peekWallpaperBitmap(Context context, boolean returnDefault, int which) {
            return peekWallpaperBitmap(context, returnDefault, which, context.getUserId());
        }

        /* JADX WARNING: Missing block: B:39:0x0062, code:
            if (r9 == false) goto L_0x0087;
     */
        /* JADX WARNING: Missing block: B:40:0x0064, code:
            r0 = r7.mDefaultWallpaper;
     */
        /* JADX WARNING: Missing block: B:41:0x0066, code:
            if (r0 != null) goto L_0x007e;
     */
        /* JADX WARNING: Missing block: B:42:0x0068, code:
            r0 = getDefaultWallpaper(r8, r11);
     */
        /* JADX WARNING: Missing block: B:43:0x006c, code:
            if (r0 == null) goto L_0x007f;
     */
        /* JADX WARNING: Missing block: B:44:0x006e, code:
            r7.wallpaperWidth = r0.getWidth();
            r7.wallpaperHeight = r0.getHeight();
     */
        /* JADX WARNING: Missing block: B:45:0x007a, code:
            monitor-enter(r7);
     */
        /* JADX WARNING: Missing block: B:47:?, code:
            r7.mDefaultWallpaper = r0;
     */
        /* JADX WARNING: Missing block: B:48:0x007d, code:
            monitor-exit(r7);
     */
        /* JADX WARNING: Missing block: B:49:0x007e, code:
            return r0;
     */
        /* JADX WARNING: Missing block: B:50:0x007f, code:
            r7.wallpaperWidth = 0;
            r7.wallpaperHeight = 0;
     */
        /* JADX WARNING: Missing block: B:54:0x0087, code:
            return null;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public Bitmap peekWallpaperBitmap(Context context, boolean returnDefault, int which, int userId) {
            if (this.mService != null) {
                try {
                    if (!this.mService.isWallpaperSupported(context.getOpPackageName())) {
                        return null;
                    }
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            }
            synchronized (this) {
                Bitmap bitmap;
                if (this.mCachedWallpaper == null || this.mCachedWallpaperUserId != userId) {
                    this.mCachedWallpaper = null;
                    this.mCachedWallpaperUserId = 0;
                    try {
                        this.mCachedWallpaper = getCurrentWallpaperLocked(context, userId);
                        this.mCachedWallpaperUserId = userId;
                    } catch (OutOfMemoryError e2) {
                        Log.w(WallpaperManager.TAG, "No memory load current wallpaper", e2);
                    }
                    if (this.mCachedWallpaper != null) {
                        this.wallpaperWidth = this.mCachedWallpaper.getWidth();
                        this.wallpaperHeight = this.mCachedWallpaper.getHeight();
                        bitmap = this.mCachedWallpaper;
                        return bitmap;
                    }
                    this.wallpaperWidth = 0;
                    this.wallpaperHeight = 0;
                } else {
                    bitmap = this.mCachedWallpaper;
                    return bitmap;
                }
            }
        }

        void forgetLoadedWallpaper() {
            synchronized (this) {
                this.mCachedWallpaper = null;
                this.mCachedWallpaperUserId = 0;
                this.mDefaultWallpaper = null;
            }
        }

        private Bitmap getCurrentWallpaperLocked(Context context, int userId) {
            if (this.mService == null) {
                Log.w(WallpaperManager.TAG, "WallpaperService not running");
                return null;
            }
            try {
                Bundle params = new Bundle();
                ParcelFileDescriptor fd = this.mService.getWallpaper(this, 1, params, userId);
                if (fd != null) {
                    int width = params.getInt(MediaFormat.KEY_WIDTH, 0);
                    int height = params.getInt(MediaFormat.KEY_HEIGHT, 0);
                    Bitmap generateBitmap;
                    try {
                        generateBitmap = HwThemeManager.generateBitmap(context, this.mService.scaleWallpaperBitmapToScreenSize(BitmapFactory.decodeFileDescriptor(fd.getFileDescriptor(), null, new Options())), width, height);
                        return generateBitmap;
                    } catch (OutOfMemoryError e) {
                        generateBitmap = WallpaperManager.TAG;
                        Log.w(generateBitmap, "Can't decode file", e);
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
            InputStream is = HwThemeManager.getDefaultWallpaperIS(context, which);
            if (is == null) {
                is = WallpaperManager.openDefaultWallpaper(context, 1);
                Log.d(WallpaperManager.TAG, "WallpaperManager getDefaultWallpaper HwThemeManager.getDefaultWallpaperIS is null");
            }
            if (is != null) {
                try {
                    Bitmap bm = BitmapFactory.decodeStream(is, null, new Options());
                    try {
                        if (this.mService != null) {
                            bm = this.mService.scaleWallpaperBitmapToScreenSize(bm);
                        }
                    } catch (RemoteException e) {
                        Log.w(WallpaperManager.TAG, "scaleWallpaperBitmapToScreenSize fail");
                    }
                    String[] location = SystemProperties.get("ro.config.wallpaper_offset").split(",");
                    if (location.length == 4 && bm != null) {
                        int xStart = Integer.parseInt(location[0]);
                        int yStart = Integer.parseInt(location[1]);
                        int xWidth = Integer.parseInt(location[2]);
                        int yHeight = Integer.parseInt(location[3]);
                        if (xStart >= 0 && yStart >= 0 && xStart + xWidth <= bm.getWidth() && yStart + yHeight <= bm.getHeight()) {
                            bm = Bitmap.createBitmap(bm, xStart, yStart, xWidth, yHeight);
                            return bm;
                        }
                    }
                    if (bm != null) {
                        Bitmap createDefaultWallpaperBitmap = WallpaperManager.getInstance(context).createDefaultWallpaperBitmap(bm);
                        IoUtils.closeQuietly(is);
                        return createDefaultWallpaperBitmap;
                    }
                    IoUtils.closeQuietly(is);
                } catch (OutOfMemoryError e2) {
                    Log.w(WallpaperManager.TAG, "Can't decode stream");
                } finally {
                    IoUtils.closeQuietly(is);
                }
            }
            return null;
        }

        public void onBlurWallpaperChanged() {
            synchronized (this) {
                this.mBlurWallpaper = null;
            }
            if (WallpaperManager.mCallbacks != null) {
                int count = WallpaperManager.mCallbacks.size();
                for (int i = 0; i < count; i++) {
                    Object obj = ((WeakReference) WallpaperManager.mCallbacks.get(i)).get();
                    if (obj != null) {
                        try {
                            final Method callback = obj.getClass().getDeclaredMethod("onBlurWallpaperChanged", new Class[0]);
                            AccessController.doPrivileged(new PrivilegedAction() {
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
                try {
                    Bitmap bitmap;
                    if (rect.width() <= 0 || rect.height() <= 0) {
                        return null;
                    } else if (this.mBlurWallpaper == null || rect.bottom > this.mBlurWallpaper.getHeight()) {
                        this.mBlurWallpaper = null;
                        if (WallpaperManager.DEBUG) {
                            Log.d(WallpaperManager.TAG, "WallpaperManager peekBlurWallpaperBitmap begin");
                        }
                        long timeBegin = System.currentTimeMillis();
                        try {
                            ParcelFileDescriptor fd = this.mService.getBlurWallpaper(this);
                            if (fd != null) {
                                try {
                                    Options options = new Options();
                                    if (fd.getFileDescriptor() == null) {
                                        Log.e(WallpaperManager.TAG, "peekBlurWallpaperBitmap() has invalid fd!");
                                        try {
                                            fd.close();
                                        } catch (IOException e) {
                                        }
                                        return null;
                                    }
                                    Bitmap bitmap2 = BitmapFactory.decodeFileDescriptor(fd.getFileDescriptor(), null, options);
                                    if (bitmap2.getWidth() > 0 && bitmap2.getHeight() > 0 && rect.bottom > 0) {
                                        this.mBlurWallpaper = Bitmap.createBitmap(bitmap2, 0, 0, bitmap2.getWidth(), Math.min(bitmap2.getHeight(), rect.bottom));
                                    }
                                    try {
                                        fd.close();
                                    } catch (IOException e2) {
                                    }
                                } catch (OutOfMemoryError e3) {
                                    this.mBlurWallpaper = null;
                                    Log.w(WallpaperManager.TAG, "Can't decode file", e3);
                                    try {
                                        fd.close();
                                    } catch (IOException e4) {
                                    }
                                } catch (Exception ex) {
                                    this.mBlurWallpaper = null;
                                    Log.w(WallpaperManager.TAG, "Can't decode file", ex);
                                    try {
                                        fd.close();
                                    } catch (IOException e5) {
                                    }
                                } catch (Throwable th) {
                                    try {
                                        fd.close();
                                    } catch (IOException e6) {
                                    }
                                    throw th;
                                }
                            }
                        } catch (RemoteException e7) {
                        }
                        if (WallpaperManager.DEBUG) {
                            Log.d(WallpaperManager.TAG, "WallpaperManager peekBlurWallpaperBitmap takenTime = " + (System.currentTimeMillis() - timeBegin));
                        }
                        bitmap = this.mBlurWallpaper;
                        return bitmap;
                    } else {
                        bitmap = this.mBlurWallpaper;
                        return bitmap;
                    }
                } catch (Throwable th2) {
                    throw th2;
                }
            }
        }

        public void forgetLoadedBlurWallpaper() {
            synchronized (this) {
                if (!(this.mBlurWallpaper == null || (this.mBlurWallpaper.isRecycled() ^ 1) == 0)) {
                    this.mBlurWallpaper.recycle();
                    this.mBlurWallpaper = null;
                }
            }
        }

        public Bundle peekCurrentWallpaperBounds() {
            Bundle params;
            synchronized (this) {
                if (this.mService == null) {
                    Log.w(WallpaperManager.TAG, "WallpaperService not running");
                    return null;
                }
                ParcelFileDescriptor parcelFileDescriptor = null;
                try {
                    params = new Bundle();
                    parcelFileDescriptor = this.mService.getWallpaper(this, 1, params, this.mService.getWallpaperUserId());
                    params.remove(MediaFormat.KEY_WIDTH);
                    params.remove(MediaFormat.KEY_HEIGHT);
                    if (parcelFileDescriptor == null) {
                        Log.w(WallpaperManager.TAG, "cannot get ParcelFileDescriptor of current user wallpaper in read only mode, return null!");
                        if (parcelFileDescriptor != null) {
                            try {
                                parcelFileDescriptor.close();
                            } catch (IOException e) {
                            }
                        }
                    } else {
                        Options options = new Options();
                        options.inJustDecodeBounds = true;
                        BitmapFactory.decodeFileDescriptor(parcelFileDescriptor.getFileDescriptor(), null, options);
                        params.putInt("wallpaper_file_width", options.outWidth);
                        params.putInt("wallpaper_file_height", options.outHeight);
                        if (WallpaperManager.DEBUG) {
                            Log.d(WallpaperManager.TAG, "getUserWallpaperBounds return wallpaper_file_width=" + params.getInt("wallpaper_file_width") + ", wallpaper_file_height=" + params.getInt("wallpaper_file_height"));
                        }
                        if (parcelFileDescriptor != null) {
                            try {
                                parcelFileDescriptor.close();
                            } catch (IOException e2) {
                            }
                        }
                    }
                } catch (RemoteException e3) {
                    if (parcelFileDescriptor != null) {
                        try {
                            parcelFileDescriptor.close();
                        } catch (IOException e4) {
                        }
                    }
                } catch (OutOfMemoryError oome) {
                    Log.e(WallpaperManager.TAG, "No memory load current wallpaper bounds", oome);
                    if (parcelFileDescriptor != null) {
                        try {
                            parcelFileDescriptor.close();
                        } catch (IOException e5) {
                        }
                    }
                } catch (Exception e6) {
                    Log.e(WallpaperManager.TAG, "Error while getting current wallpaper bounds", e6);
                    if (parcelFileDescriptor != null) {
                        try {
                            parcelFileDescriptor.close();
                        } catch (IOException e7) {
                        }
                    }
                } catch (Throwable th) {
                    if (parcelFileDescriptor != null) {
                        try {
                            parcelFileDescriptor.close();
                        } catch (IOException e8) {
                        }
                    }
                }
            }
            return null;
            return params;
            return null;
        }

        public synchronized int getWallpaperWidth() {
            return this.wallpaperWidth;
        }

        public synchronized int getWallpaperHeight() {
            return this.wallpaperHeight;
        }
    }

    private class WallpaperSetCompletion extends Stub {
        final CountDownLatch mLatch = new CountDownLatch(1);

        public void waitForCompletion() {
            try {
                this.mLatch.await(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
            }
        }

        public void onWallpaperChanged() throws RemoteException {
            this.mLatch.countDown();
        }

        public void onBlurWallpaperChanged() throws RemoteException {
        }
    }

    static void initGlobals(Looper looper) {
        synchronized (sSync) {
            if (sGlobals == null) {
                sGlobals = new Globals(looper);
            }
        }
    }

    public WallpaperManager(Context context, Handler handler) {
        this.mContext = context;
        initGlobals(context.getMainLooper());
    }

    public static WallpaperManager getInstance(Context context) {
        return (WallpaperManager) context.getSystemService("wallpaper");
    }

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
        return getBuiltInDrawable(0, 0, false, TonemapCurve.LEVEL_BLACK, TonemapCurve.LEVEL_BLACK, 1);
    }

    public Drawable getBuiltInDrawable(int which) {
        return getBuiltInDrawable(0, 0, false, TonemapCurve.LEVEL_BLACK, TonemapCurve.LEVEL_BLACK, which);
    }

    public Drawable getBuiltInDrawable(int outWidth, int outHeight, boolean scaleToFit, float horizontalAlignment, float verticalAlignment) {
        return getBuiltInDrawable(outWidth, outHeight, scaleToFit, horizontalAlignment, verticalAlignment, 1);
    }

    public Drawable getBuiltInDrawable(int outWidth, int outHeight, boolean scaleToFit, float horizontalAlignment, float verticalAlignment, int which) {
        if (sGlobals.mService == null) {
            Log.w(TAG, "WallpaperService not running");
            throw new RuntimeException(new DeadSystemException());
        } else if (which == 1 || which == 2) {
            Resources resources = this.mContext.getResources();
            horizontalAlignment = Math.max(TonemapCurve.LEVEL_BLACK, Math.min(1.0f, horizontalAlignment));
            verticalAlignment = Math.max(TonemapCurve.LEVEL_BLACK, Math.min(1.0f, verticalAlignment));
            InputStream wpStream = openDefaultWallpaper(this.mContext, which);
            if (wpStream == null) {
                if (DEBUG) {
                    Log.w(TAG, "default wallpaper stream " + which + " is null");
                }
                return null;
            }
            InputStream bufferedInputStream = new BufferedInputStream(wpStream);
            if (outWidth <= 0 || outHeight <= 0) {
                return new BitmapDrawable(resources, BitmapFactory.decodeStream(bufferedInputStream, null, null));
            }
            Options options = new Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(bufferedInputStream, null, options);
            if (options.outWidth == 0 || options.outHeight == 0) {
                Log.e(TAG, "default wallpaper dimensions are 0");
                return null;
            }
            RectF cropRectF;
            int inWidth = options.outWidth;
            int inHeight = options.outHeight;
            bufferedInputStream = new BufferedInputStream(openDefaultWallpaper(this.mContext, which));
            outWidth = Math.min(inWidth, outWidth);
            outHeight = Math.min(inHeight, outHeight);
            if (scaleToFit) {
                cropRectF = getMaxCropRect(inWidth, inHeight, outWidth, outHeight, horizontalAlignment, verticalAlignment);
            } else {
                float left = ((float) (inWidth - outWidth)) * horizontalAlignment;
                float top = ((float) (inHeight - outHeight)) * verticalAlignment;
                cropRectF = new RectF(left, top, left + ((float) outWidth), top + ((float) outHeight));
            }
            Rect roundedTrueCrop = new Rect();
            cropRectF.roundOut(roundedTrueCrop);
            if (roundedTrueCrop.width() <= 0 || roundedTrueCrop.height() <= 0) {
                Log.w(TAG, "crop has bad values for full size image");
                return null;
            }
            int scaleDownSampleSize = Math.min(roundedTrueCrop.width() / outWidth, roundedTrueCrop.height() / outHeight);
            BitmapRegionDecoder decoder = null;
            try {
                decoder = BitmapRegionDecoder.newInstance(bufferedInputStream, true);
            } catch (IOException e) {
                Log.w(TAG, "cannot open region decoder for default wallpaper");
            }
            Bitmap crop = null;
            if (decoder != null) {
                options = new Options();
                if (scaleDownSampleSize > 1) {
                    options.inSampleSize = scaleDownSampleSize;
                }
                crop = decoder.decodeRegion(roundedTrueCrop, options);
                decoder.recycle();
            }
            if (crop == null) {
                bufferedInputStream = new BufferedInputStream(openDefaultWallpaper(this.mContext, which));
                options = new Options();
                if (scaleDownSampleSize > 1) {
                    options.inSampleSize = scaleDownSampleSize;
                }
                Bitmap fullSize = BitmapFactory.decodeStream(bufferedInputStream, null, options);
                if (fullSize != null) {
                    crop = Bitmap.createBitmap(fullSize, roundedTrueCrop.left, roundedTrueCrop.top, roundedTrueCrop.width(), roundedTrueCrop.height());
                }
            }
            if (crop == null) {
                Log.w(TAG, "cannot decode default wallpaper");
                return null;
            }
            if (outWidth > 0 && outHeight > 0 && !(crop.getWidth() == outWidth && crop.getHeight() == outHeight)) {
                Matrix m = new Matrix();
                RectF cropRect = new RectF(TonemapCurve.LEVEL_BLACK, TonemapCurve.LEVEL_BLACK, (float) crop.getWidth(), (float) crop.getHeight());
                RectF rectF = new RectF(TonemapCurve.LEVEL_BLACK, TonemapCurve.LEVEL_BLACK, (float) outWidth, (float) outHeight);
                m.setRectToRect(cropRect, rectF, ScaleToFit.FILL);
                Bitmap tmp = Bitmap.createBitmap((int) rectF.width(), (int) rectF.height(), Config.ARGB_8888);
                if (tmp != null) {
                    Canvas c = new Canvas(tmp);
                    Paint p = new Paint();
                    p.setFilterBitmap(true);
                    c.drawBitmap(crop, m, p);
                    crop = tmp;
                }
            }
            return new BitmapDrawable(resources, crop);
        } else {
            throw new IllegalArgumentException("Must request exactly one kind of wallpaper");
        }
    }

    private static RectF getMaxCropRect(int inWidth, int inHeight, int outWidth, int outHeight, float horizontalAlignment, float verticalAlignment) {
        RectF cropRect = new RectF();
        if (((float) inWidth) / ((float) inHeight) > ((float) outWidth) / ((float) outHeight)) {
            cropRect.top = TonemapCurve.LEVEL_BLACK;
            cropRect.bottom = (float) inHeight;
            float cropWidth = ((float) outWidth) * (((float) inHeight) / ((float) outHeight));
            cropRect.left = (((float) inWidth) - cropWidth) * horizontalAlignment;
            cropRect.right = cropRect.left + cropWidth;
        } else {
            cropRect.left = TonemapCurve.LEVEL_BLACK;
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
            return new FastBitmapDrawable(bm, null);
        }
        return null;
    }

    public Drawable peekFastDrawable() {
        Bitmap bm = sGlobals.peekWallpaperBitmap(this.mContext, false, 1);
        if (bm != null) {
            return new FastBitmapDrawable(bm, null);
        }
        return null;
    }

    public Bitmap getBitmap() {
        return getBitmapAsUser(this.mContext.getUserId());
    }

    public Bitmap getBitmapAsUser(int userId) {
        return sGlobals.peekWallpaperBitmap(this.mContext, true, 1, userId);
    }

    public ParcelFileDescriptor getWallpaperFile(int which) {
        return getWallpaperFile(which, this.mContext.getUserId());
    }

    public ParcelFileDescriptor getWallpaperFile(int which, int userId) {
        if (which != 1 && which != 2) {
            throw new IllegalArgumentException("Must request exactly one kind of wallpaper");
        } else if (sGlobals.mService == null) {
            Log.w(TAG, "WallpaperService not running");
            throw new RuntimeException(new DeadSystemException());
        } else {
            try {
                return sGlobals.mService.getWallpaper(null, which, new Bundle(), userId);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void forgetLoadedWallpaper() {
        sGlobals.forgetLoadedWallpaper();
    }

    public WallpaperInfo getWallpaperInfo() {
        try {
            if (sGlobals.mService != null) {
                return sGlobals.mService.getWallpaperInfo(UserHandle.myUserId());
            }
            Log.w(TAG, "WallpaperService not running");
            throw new RuntimeException(new DeadSystemException());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public WallpaperInfo getWallpaperInfo(int userId) {
        try {
            if (sGlobals.mService != null) {
                return sGlobals.mService.getWallpaperInfo(userId);
            }
            Log.w(TAG, "sGlobals.mService no running");
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
            cropAndSetWallpaperIntent.setPackage(this.mContext.getString(17039812));
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
        Throwable th;
        if (sGlobals.mService == null) {
            Log.w(TAG, "WallpaperService not running");
            throw new RuntimeException(new DeadSystemException());
        }
        Bundle result = new Bundle();
        WallpaperSetCompletion completion = new WallpaperSetCompletion();
        try {
            Resources resources = this.mContext.getResources();
            ParcelFileDescriptor fd = sGlobals.mService.setWallpaper("res:" + resources.getResourceName(resid), this.mContext.getOpPackageName(), null, false, result, which, completion, UserHandle.myUserId());
            if (fd != null) {
                FileOutputStream fos = null;
                try {
                    FileOutputStream fos2 = new AutoCloseOutputStream(fd);
                    try {
                        copyStreamToWallpaperFile(resources.openRawResource(resid), fos2);
                        fos2.close();
                        completion.waitForCompletion();
                        IoUtils.closeQuietly(fos2);
                    } catch (Throwable th2) {
                        th = th2;
                        fos = fos2;
                        IoUtils.closeQuietly(fos);
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    IoUtils.closeQuietly(fos);
                    throw th;
                }
            }
            return result.getInt(EXTRA_NEW_WALLPAPER_ID, 0);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setBitmap(Bitmap bitmap) throws IOException {
        setBitmap(bitmap, null, true);
    }

    public int setBitmap(Bitmap fullImage, Rect visibleCropHint, boolean allowBackup) throws IOException {
        return setBitmap(fullImage, visibleCropHint, allowBackup, 3);
    }

    public int setBitmap(Bitmap fullImage, Rect visibleCropHint, boolean allowBackup, int which) throws IOException {
        return setBitmap(fullImage, visibleCropHint, allowBackup, which, UserHandle.myUserId());
    }

    public int setBitmap(Bitmap fullImage, Rect visibleCropHint, boolean allowBackup, int which, int userId) throws IOException {
        Throwable th;
        validateRect(visibleCropHint);
        if (sGlobals.mService == null) {
            Log.w(TAG, "WallpaperService not running");
            throw new RuntimeException(new DeadSystemException());
        }
        Bundle result = new Bundle();
        WallpaperSetCompletion completion = new WallpaperSetCompletion();
        try {
            ParcelFileDescriptor fd = sGlobals.mService.setWallpaper(null, this.mContext.getOpPackageName(), visibleCropHint, allowBackup, result, which, completion, userId);
            if (fd != null) {
                FileOutputStream fos = null;
                try {
                    FileOutputStream fos2 = new AutoCloseOutputStream(fd);
                    try {
                        fullImage.compress(CompressFormat.PNG, 90, fos2);
                        fos2.close();
                        completion.waitForCompletion();
                        IoUtils.closeQuietly(fos2);
                    } catch (Throwable th2) {
                        th = th2;
                        fos = fos2;
                        IoUtils.closeQuietly(fos);
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    IoUtils.closeQuietly(fos);
                    throw th;
                }
            }
            return result.getInt(EXTRA_NEW_WALLPAPER_ID, 0);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
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
        byte[] buffer = new byte[32768];
        while (true) {
            int amt = data.read(buffer);
            if (amt > 0) {
                fos.write(buffer, 0, amt);
            } else {
                return;
            }
        }
    }

    public int setStream(InputStream bitmapData, Rect visibleCropHint, boolean allowBackup) throws IOException {
        return setStream(bitmapData, visibleCropHint, allowBackup, 3);
    }

    public int setStream(InputStream bitmapData, Rect visibleCropHint, boolean allowBackup, int which) throws IOException {
        Throwable th;
        validateRect(visibleCropHint);
        if (sGlobals.mService == null) {
            Log.w(TAG, "WallpaperService not running");
            throw new RuntimeException(new DeadSystemException());
        }
        Bundle result = new Bundle();
        WallpaperSetCompletion completion = new WallpaperSetCompletion();
        try {
            ParcelFileDescriptor fd = sGlobals.mService.setWallpaper(null, this.mContext.getOpPackageName(), visibleCropHint, allowBackup, result, which, completion, UserHandle.myUserId());
            if (fd != null) {
                FileOutputStream fos = null;
                try {
                    FileOutputStream fos2 = new AutoCloseOutputStream(fd);
                    try {
                        copyStreamToWallpaperFile(bitmapData, fos2);
                        fos2.close();
                        completion.waitForCompletion();
                        IoUtils.closeQuietly(fos2);
                    } catch (Throwable th2) {
                        th = th2;
                        fos = fos2;
                        IoUtils.closeQuietly(fos);
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    IoUtils.closeQuietly(fos);
                    throw th;
                }
            }
            return result.getInt(EXTRA_NEW_WALLPAPER_ID, 0);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean hasResourceWallpaper(int resid) {
        if (sGlobals.mService == null) {
            Log.w(TAG, "WallpaperService not running");
            throw new RuntimeException(new DeadSystemException());
        }
        try {
            return sGlobals.mService.hasNamedWallpaper("res:" + this.mContext.getResources().getResourceName(resid));
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getDesiredMinimumWidth() {
        if (sGlobals.mService == null) {
            Log.w(TAG, "WallpaperService not running");
            throw new RuntimeException(new DeadSystemException());
        }
        try {
            return sGlobals.mService.getWidthHint();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getDesiredMinimumHeight() {
        if (sGlobals.mService == null) {
            Log.w(TAG, "WallpaperService not running");
            throw new RuntimeException(new DeadSystemException());
        }
        try {
            return sGlobals.mService.getHeightHint();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
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
                minimumHeight = (int) (((double) (((float) maximumTextureSize) * aspect)) + 0.5d);
            } else {
                minimumHeight = maximumTextureSize;
                minimumWidth = (int) (((double) (((float) maximumTextureSize) / aspect)) + 0.5d);
            }
        }
        try {
            if (sGlobals.mService == null) {
                Log.w(TAG, "WallpaperService not running");
                throw new RuntimeException(new DeadSystemException());
            } else {
                sGlobals.mService.setDimensionHints(minimumWidth, minimumHeight, this.mContext.getOpPackageName());
            }
        } catch (RemoteException e2) {
            throw e2.rethrowFromSystemServer();
        }
    }

    public void setDisplayPadding(Rect padding) {
        try {
            if (sGlobals.mService == null) {
                Log.w(TAG, "WallpaperService not running");
                throw new RuntimeException(new DeadSystemException());
            } else {
                sGlobals.mService.setDisplayPadding(padding, this.mContext.getOpPackageName());
            }
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

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

    public void clearWallpaper(int which, int userId) {
        if (sGlobals.mService == null) {
            Log.w(TAG, "WallpaperService not running");
            throw new RuntimeException(new DeadSystemException());
        }
        try {
            sGlobals.mService.clearWallpaper(this.mContext.getOpPackageName(), which, userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean setWallpaperComponent(ComponentName name) {
        return setWallpaperComponent(name, UserHandle.myUserId());
    }

    public boolean setWallpaperComponent(ComponentName name, int userId) {
        if (sGlobals.mService == null) {
            Log.w(TAG, "WallpaperService not running");
            throw new RuntimeException(new DeadSystemException());
        }
        try {
            sGlobals.mService.setWallpaperComponentChecked(name, this.mContext.getOpPackageName(), userId);
            return true;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setWallpaperOffsets(IBinder windowToken, float xOffset, float yOffset) {
        try {
            Display display = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
            Point size = new Point();
            display.getRealSize(size);
            int width = size.y > size.x ? size.x : size.y;
            int height = size.y > size.x ? size.y : size.x;
            int wallpaperWidth = getWallpaperWidth();
            int wallpaperHeight = getWallpaperHeight();
            if (wallpaperWidth == 0 && wallpaperHeight == 0) {
                Bitmap currentWallpaper = getBitmap();
                if (currentWallpaper != null) {
                    wallpaperWidth = currentWallpaper.getWidth();
                    wallpaperHeight = currentWallpaper.getHeight();
                    currentWallpaper.recycle();
                }
            }
            boolean isSquare = wallpaperWidth == height && wallpaperHeight == height;
            boolean is_scroll = false;
            if (Math.abs(((((double) height) * 1.0d) / ((double) width)) - 2.0d) < 0.001d && isSquare) {
                is_scroll = System.getInt(this.mContext.getContentResolver(), "is_scroll", -1) == 1;
            }
            if (!(width == wallpaperWidth && height == wallpaperHeight) && (!isSquare || (is_scroll ^ 1) == 0)) {
                Log.i(TAG, "setWallpaperOffsets second wallpaper =[" + xOffset + "," + yOffset + "," + this.mWallpaperXStep + "," + this.mWallpaperYStep + "]");
                WindowManagerGlobal.getWindowSession().setWallpaperPosition(windowToken, xOffset, yOffset, this.mWallpaperXStep, this.mWallpaperYStep);
                return;
            }
            Log.i(TAG, "setWallpaperOffsets  first wallpaper =[0]");
            WindowManagerGlobal.getWindowSession().setWallpaperPosition(windowToken, TonemapCurve.LEVEL_BLACK, TonemapCurve.LEVEL_BLACK, TonemapCurve.LEVEL_BLACK, TonemapCurve.LEVEL_BLACK);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
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
        if (sGlobals.mService == null) {
            Log.w(TAG, "WallpaperService not running");
            throw new RuntimeException(new DeadSystemException());
        }
        try {
            return sGlobals.mService.isWallpaperSupported(this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isSetWallpaperAllowed() {
        if (sGlobals.mService == null) {
            Log.w(TAG, "WallpaperService not running");
            throw new RuntimeException(new DeadSystemException());
        }
        try {
            return sGlobals.mService.isSetWallpaperAllowed(this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
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
            return context.getResources().openRawResource(17302115);
        } catch (NotFoundException e2) {
            return null;
        }
    }

    public static ComponentName getDefaultWallpaperComponent(Context context) {
        ComponentName cn;
        String flat = SystemProperties.get(PROP_WALLPAPER_COMPONENT);
        if (TextUtils.isEmpty(flat)) {
            flat = HwThemeManager.getDefaultLiveWallpaper(UserHandle.myUserId());
        }
        if (!TextUtils.isEmpty(flat)) {
            cn = ComponentName.unflattenFromString(flat);
            if (cn != null) {
                return cn;
            }
        }
        flat = context.getString(17039881);
        if (!TextUtils.isEmpty(flat)) {
            cn = ComponentName.unflattenFromString(flat);
            if (cn != null) {
                return cn;
            }
        }
        return null;
    }

    public boolean setLockWallpaperCallback(IWallpaperManagerCallback callback) {
        if (sGlobals.mService == null) {
            Log.w(TAG, "WallpaperService not running");
            throw new RuntimeException(new DeadSystemException());
        }
        try {
            return sGlobals.mService.setLockWallpaperCallback(callback);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isWallpaperBackupEligible(int which) {
        if (sGlobals.mService == null) {
            Log.w(TAG, "WallpaperService not running");
            throw new RuntimeException(new DeadSystemException());
        }
        try {
            return sGlobals.mService.isWallpaperBackupEligible(which, this.mContext.getUserId());
        } catch (RemoteException e) {
            Log.e(TAG, "Exception querying wallpaper backup eligibility: " + e.getMessage());
            return false;
        }
    }

    public Context getContext() {
        return this.mContext;
    }

    public Bitmap peekBlurWallpaperBitmap(Rect rect) {
        return sGlobals.peekBlurWallpaperBitmap(rect);
    }

    public Bundle getUserWallpaperBounds() {
        return sGlobals.peekCurrentWallpaperBounds();
    }

    public Bitmap getBlurBitmap(Rect rect) {
        return null;
    }

    public void setCallback(Object callback) {
    }

    public void forgetLoadedBlurWallpaper() {
        sGlobals.forgetLoadedBlurWallpaper();
    }

    protected Bitmap createDefaultWallpaperBitmap(Bitmap bm) {
        Display display = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);
        return HwThemeManager.generateBitmap(this.mContext, bm, size.x < size.y ? size.x : size.y, size.x > size.y ? size.x : size.y);
    }

    public int[] getWallpaperStartingPoints() {
        return new int[]{-1, -1, -1, -1};
    }

    public void setBitmapWithOffsets(Bitmap bitmap, int[] offsets) throws IOException {
        setBitmap(bitmap);
    }

    public void setStreamWithOffsets(InputStream data, int[] offsets) throws IOException {
        setStream(data);
    }

    public int[] parseWallpaperOffsets(String srcFile) {
        return new int[]{-1, -1, -1, -1};
    }

    private int getWallpaperWidth() {
        return sGlobals.getWallpaperWidth();
    }

    private int getWallpaperHeight() {
        return sGlobals.getWallpaperHeight();
    }
}
