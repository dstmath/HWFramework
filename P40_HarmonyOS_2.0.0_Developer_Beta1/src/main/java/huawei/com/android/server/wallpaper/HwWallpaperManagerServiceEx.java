package huawei.com.android.server.wallpaper;

import android.app.IWallpaperManagerCallback;
import android.app.WallpaperInfo;
import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hwtheme.HwThemeManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.os.ParcelFileDescriptor;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SELinux;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.provider.Settings;
import android.rms.HwSysResManager;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.CollectData;
import android.util.DisplayMetrics;
import android.util.Slog;
import android.view.Display;
import android.view.IWindowManager;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import com.android.server.BlurUtils;
import com.android.server.wallpaper.IHwWallpaperManagerInner;
import com.android.server.wallpaper.IHwWallpaperManagerServiceEx;
import com.android.server.wallpaper.WallpaperManagerService;
import huawei.android.hwpicaveragenoises.HwPicAverageNoises;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class HwWallpaperManagerServiceEx implements IHwWallpaperManagerServiceEx {
    private static final boolean DEBUG = false;
    private static final String PERMISSION = "com.huawei.wallpaperservcie.permission.SET_WALLPAPER_OFFSET";
    private static final String PERMISSIONS_APS_RESOLUTION_CHANGE_ACTION = "huawei.intent.permissions.APS_RESOLUTION_CHANGE_ACTION";
    private static final int QUALITY_100 = 100;
    private static final int QUALITY_90 = 90;
    private static final String ROG_KILL_APP_END_ACTION = "com.huawei.systemmamanger.action.KILL_ROGAPP_END";
    private static final String TAG = "HwWallpaperManagerServiceEx";
    public static final String WALLPAPER_MANAGER_SERVICE_READY_ACTION = "android.intent.action.WALLPAPER_MANAGER_SERVICE_READY";
    private int disHeight;
    private int disWidth;
    private Context mContext;
    private IHwWallpaperManagerInner mIHwWallpaperManagerInner;

    public HwWallpaperManagerServiceEx(IHwWallpaperManagerInner iHwWallpaperInner, Context context) {
        this.mIHwWallpaperManagerInner = iHwWallpaperInner;
        this.mContext = context;
        DisplayMetrics dm = new DisplayMetrics();
        ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay().getMetrics(dm);
        this.disWidth = dm.widthPixels;
        this.disHeight = dm.heightPixels;
    }

    public void wallpaperManagerServiceReady() {
        createBlurWallpaper(false);
        Slog.i(TAG, "HwWallpaperManagerService.java#wallpaperManagerServiceReady() create blur wallpaper");
    }

    public void handleWallpaperObserverEvent(final WallpaperManagerService.WallpaperData wallpaper) {
        new Thread("BlurWallpaperThread") {
            /* class huawei.com.android.server.wallpaper.HwWallpaperManagerServiceEx.AnonymousClass1 */

            @Override // java.lang.Thread, java.lang.Runnable
            public void run() {
                Slog.d(HwWallpaperManagerServiceEx.TAG, "onEvent createBlurBitmap Blur wallpaper Begin");
                long timeBegin = System.currentTimeMillis();
                HwWallpaperManagerServiceEx.this.createBlurWallpaper(true);
                Slog.d(HwWallpaperManagerServiceEx.TAG, "onEvent createBlurBitmap Blur takenTime = " + (System.currentTimeMillis() - timeBegin));
                synchronized (HwWallpaperManagerServiceEx.this.mIHwWallpaperManagerInner.getLock()) {
                    HwWallpaperManagerServiceEx.this.notifyBlurCallbacksLocked(wallpaper);
                }
            }
        }.start();
    }

    public void hwSystemReady() {
        WallpaperManagerService.WallpaperData wallpaper = (WallpaperManagerService.WallpaperData) this.mIHwWallpaperManagerInner.getWallpaperMap().get(0);
        synchronized (this.mIHwWallpaperManagerInner.getLock()) {
            notifyBlurCallbacksLocked(wallpaper);
        }
        IntentFilter userFilter = new IntentFilter();
        userFilter.addAction(ROG_KILL_APP_END_ACTION);
        this.mContext.registerReceiver(new BroadcastReceiver() {
            /* class huawei.com.android.server.wallpaper.HwWallpaperManagerServiceEx.AnonymousClass2 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if (HwWallpaperManagerServiceEx.ROG_KILL_APP_END_ACTION.equals(intent.getAction())) {
                    HwWallpaperManagerServiceEx.this.mIHwWallpaperManagerInner.restartLiveWallpaperService();
                }
            }
        }, userFilter, PERMISSIONS_APS_RESOLUTION_CHANGE_ACTION, null);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyBlurCallbacksLocked(WallpaperManagerService.WallpaperData wallpaper) {
        RemoteCallbackList<IWallpaperManagerCallback> callbacks = wallpaper.getCallbacks();
        synchronized (callbacks) {
            int n = callbacks.beginBroadcast();
            for (int i = 0; i < n; i++) {
                try {
                    callbacks.getBroadcastItem(i).onBlurWallpaperChanged();
                } catch (RemoteException e) {
                }
            }
            callbacks.finishBroadcast();
        }
    }

    public int getWallpaperUserId() {
        int callingUid = Binder.getCallingUid();
        if (callingUid == 1000) {
            return this.mIHwWallpaperManagerInner.getCurrentUserId();
        }
        return UserHandle.getUserId(callingUid);
    }

    private Bitmap getCurrentWallpaperLocked() {
        ParcelFileDescriptor fd = getWallpaper(new Bundle());
        if (fd != null) {
            try {
                Bitmap bm = scaleWallpaperBitmapToScreenSize(BitmapFactory.decodeFileDescriptor(fd.getFileDescriptor(), null, new BitmapFactory.Options()));
                try {
                    fd.close();
                } catch (IOException e) {
                }
                return bm;
            } catch (OutOfMemoryError e2) {
                Slog.w(TAG, "Can't decode file", e2);
                try {
                    fd.close();
                } catch (IOException e3) {
                }
            } catch (Throwable th) {
                try {
                    fd.close();
                } catch (IOException e4) {
                }
                throw th;
            }
        }
        return null;
    }

    public Bitmap scaleWallpaperBitmapToScreenSize(Bitmap bitmap) {
        int shortSideLength;
        int longSideLength;
        if (bitmap == null) {
            return null;
        }
        Matrix m = new Matrix();
        Point size = new Point();
        int longSideLength2 = 0;
        int bitmap_height = bitmap.getHeight();
        int bitmap_width = bitmap.getWidth();
        int bitmapLongSideLength = bitmap_height > bitmap_width ? bitmap_height : bitmap_width;
        int bitmapShortSideLength = bitmap_height > bitmap_width ? bitmap_width : bitmap_height;
        try {
            IWindowManager.Stub.asInterface(ServiceManager.checkService("window")).getBaseDisplaySize(0, size);
            longSideLength2 = size.x > size.y ? size.x : size.y;
            shortSideLength = size.x > size.y ? size.y : size.x;
        } catch (RemoteException e) {
            Slog.e(TAG, "IWindowManager null");
            shortSideLength = 0;
        }
        if (longSideLength2 == 0 || shortSideLength == 0 || bitmapLongSideLength == longSideLength2 || (bitmapLongSideLength == shortSideLength * 2 && bitmapShortSideLength == longSideLength2)) {
            return bitmap;
        }
        if ((((float) bitmapLongSideLength) / ((float) shortSideLength)) / (((float) bitmapShortSideLength) / ((float) longSideLength2)) == 2.0f) {
            longSideLength = shortSideLength * 2;
        } else {
            longSideLength = longSideLength2;
        }
        float scale = ((float) longSideLength) / ((float) bitmapLongSideLength);
        m.setScale(scale, scale);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap_width, bitmap_height, m, true);
    }

    private ParcelFileDescriptor getWallpaper(Bundle outParams) {
        synchronized (this.mIHwWallpaperManagerInner.getLock()) {
            int wallpaperUserId = getWallpaperUserId();
            WallpaperManagerService.WallpaperData wallpaper = (WallpaperManagerService.WallpaperData) this.mIHwWallpaperManagerInner.getWallpaperMap().get(wallpaperUserId);
            if (outParams != null) {
                try {
                    outParams.putInt("width", wallpaper.getWidth());
                    outParams.putInt("height", wallpaper.getHeight());
                } catch (FileNotFoundException e) {
                    Slog.w(TAG, "Error getting wallpaper", e);
                    return null;
                }
            }
            File f = new File(Environment.getUserSystemDirectory(wallpaperUserId), "wallpaper");
            if (!f.exists()) {
                return null;
            }
            return ParcelFileDescriptor.open(f, 268435456);
        }
    }

    private Bitmap getDefaultWallpaperLocked(Context context) {
        InputStream is = HwThemeManager.getDefaultWallpaperIS(context, this.mIHwWallpaperManagerInner.getCurrentUserId());
        if (is != null) {
            try {
                Bitmap bm = scaleWallpaperBitmapToScreenSize(BitmapFactory.decodeStream(is, null, new BitmapFactory.Options()));
                try {
                    is.close();
                } catch (IOException e) {
                }
                return bm;
            } catch (OutOfMemoryError e2) {
                Slog.w(TAG, "Can't decode stream", e2);
                try {
                    is.close();
                } catch (IOException e3) {
                }
            } catch (Throwable th) {
                try {
                    is.close();
                } catch (IOException e4) {
                }
                throw th;
            }
        }
        return null;
    }

    private void removeThumbnailCache(WallpaperInfo info) {
        if (info != null) {
            info.removeThumbnailCache(this.mContext.getPackageManager());
        }
    }

    private void saveBlurWallpaperBitmapLocked(Bitmap blurImg) {
        saveWallpaperBitmapLocked(blurImg, "blurwallpaper", QUALITY_90);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0062, code lost:
        r0 = e;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x0062 A[ExcHandler: FileNotFoundException (e java.io.FileNotFoundException), Splitter:B:21:0x005e] */
    private File saveWallpaperBitmapLocked(Bitmap bitmap, String fileName, int quality) {
        FileNotFoundException e;
        Throwable th;
        Slog.d(TAG, "saveWallpaperBitmapLocked begin");
        long timeBegin = System.currentTimeMillis();
        try {
            File dir = Environment.getUserSystemDirectory(getWallpaperUserId());
            if (!dir.exists()) {
                try {
                    if (dir.mkdir()) {
                        FileUtils.setPermissions(dir.getPath(), 505, -1, -1);
                    }
                } catch (FileNotFoundException e2) {
                    e = e2;
                    Slog.w(TAG, "Error setting wallpaper", e);
                    return null;
                }
            }
            try {
                File file = new File(dir, fileName);
                ParcelFileDescriptor fd = ParcelFileDescriptor.open(file, 939524096);
                if (SELinux.restorecon(file)) {
                    FileOutputStream fos = null;
                    try {
                        fos = new ParcelFileDescriptor.AutoCloseOutputStream(fd);
                        try {
                            bitmap.compress(Bitmap.CompressFormat.PNG, quality, fos);
                            try {
                                fos.close();
                            } catch (IOException e3) {
                            } catch (FileNotFoundException e4) {
                            }
                            if (fd != null) {
                                fd.close();
                            }
                            try {
                                long takenTime = System.currentTimeMillis() - timeBegin;
                                StringBuilder sb = new StringBuilder();
                                try {
                                    sb.append("saveWallpaperBitmapLocked takenTime = ");
                                    sb.append(takenTime);
                                    Slog.d(TAG, sb.toString());
                                } catch (FileNotFoundException e5) {
                                    e = e5;
                                }
                            } catch (FileNotFoundException e6) {
                                e = e6;
                                Slog.w(TAG, "Error setting wallpaper", e);
                                return null;
                            }
                        } catch (Throwable th2) {
                            th = th2;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        if (fos != null) {
                            try {
                                fos.close();
                            } catch (IOException e7) {
                            }
                        }
                        if (fd != null) {
                            try {
                                fd.close();
                            } catch (IOException e8) {
                            }
                        }
                        throw th;
                    }
                }
                if (fd != null) {
                    try {
                        fd.close();
                    } catch (IOException e9) {
                    }
                }
                return file;
            } catch (FileNotFoundException e10) {
                e = e10;
                Slog.w(TAG, "Error setting wallpaper", e);
                return null;
            }
        } catch (FileNotFoundException e11) {
            e = e11;
            Slog.w(TAG, "Error setting wallpaper", e);
            return null;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void createBlurWallpaper(boolean force) {
        Bitmap wallpaper;
        Bitmap bmp;
        Bitmap.Config config;
        synchronized (this.mIHwWallpaperManagerInner.getLock()) {
            if (!force) {
                if (new File(Environment.getUserSystemDirectory(getWallpaperUserId()), "blurwallpaper").exists()) {
                    return;
                }
            }
            try {
                WallpaperInfo info = this.mIHwWallpaperManagerInner.getWallpaperInfo(getWallpaperUserId());
                if (info == null) {
                    wallpaper = getCurrentWallpaperLocked();
                } else {
                    Drawable dr = HwFrameworkFactory.getHwWallpaperInfoStub(info).loadThumbnailWithoutTheme(this.mContext.getPackageManager());
                    if (dr == null) {
                        wallpaper = null;
                    } else if (dr instanceof BitmapDrawable) {
                        wallpaper = ((BitmapDrawable) dr).getBitmap();
                    } else {
                        try {
                            int intrinsicWidth = dr.getIntrinsicWidth();
                            int intrinsicHeight = dr.getIntrinsicHeight();
                            if (dr.getOpacity() != -1) {
                                config = Bitmap.Config.ARGB_8888;
                            } else {
                                config = Bitmap.Config.RGB_565;
                            }
                            wallpaper = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, config);
                            Canvas c = new Canvas(wallpaper);
                            dr.setBounds(0, 0, dr.getIntrinsicWidth(), dr.getIntrinsicHeight());
                            dr.draw(c);
                        } catch (RuntimeException e) {
                            Slog.e(TAG, "create blurwallpaper draw has some errors");
                            wallpaper = null;
                        }
                    }
                }
                if (wallpaper == null || wallpaper.isRecycled()) {
                    Slog.d(TAG, "wallpaper is null or has been recycled");
                    wallpaper = getDefaultWallpaperLocked(this.mContext);
                }
                if (wallpaper != null) {
                    int h = this.disHeight;
                    int w = this.disWidth;
                    if (!ViewConfiguration.get(this.mContext).hasPermanentMenuKey()) {
                        h += this.mContext.getResources().getDimensionPixelSize(17105309);
                    }
                    if (wallpaper.getHeight() == h && wallpaper.getWidth() == w * 2) {
                        int[] inPixels = new int[(this.disWidth * h)];
                        Bitmap bitmap = Bitmap.createBitmap(this.disWidth, h, Bitmap.Config.ARGB_8888);
                        wallpaper.getPixels(inPixels, 0, this.disWidth, 0, 0, this.disWidth, h);
                        bitmap.setPixels(inPixels, 0, this.disWidth, 0, 0, this.disWidth, h);
                        if (bitmap != wallpaper && !wallpaper.isRecycled()) {
                            wallpaper.recycle();
                            wallpaper = bitmap;
                            if (info != null) {
                                removeThumbnailCache(info);
                            }
                        }
                    }
                    if (!((wallpaper.getHeight() == h / 4 && wallpaper.getWidth() == w / 4) || (wallpaper = Bitmap.createScaledBitmap(wallpaper, this.disWidth / 4, h / 4, true)) == null || wallpaper == wallpaper || wallpaper.isRecycled())) {
                        wallpaper.recycle();
                        if (info != null) {
                            removeThumbnailCache(info);
                        }
                    }
                }
                if (!(wallpaper == null || (bmp = BlurUtils.stackBlur(wallpaper, 80)) == null)) {
                    wallpaper.recycle();
                    if (HwPicAverageNoises.isAverageNoiseSupported()) {
                        saveBlurWallpaperBitmapLocked(bmp);
                    } else {
                        Bitmap blurImg = BlurUtils.addBlackBoard(bmp, 1275068416);
                        if (blurImg != null) {
                            bmp.recycle();
                            saveBlurWallpaperBitmapLocked(blurImg);
                        }
                    }
                }
            } catch (OutOfMemoryError e2) {
                Slog.w(TAG, "No memory load current wallpaper", e2);
            }
        }
    }

    public ParcelFileDescriptor getBlurWallpaper(IWallpaperManagerCallback cb) {
        synchronized (this.mIHwWallpaperManagerInner.getLock()) {
            int wallpaperUserId = getWallpaperUserId();
            WallpaperManagerService.WallpaperData wallpaper = (WallpaperManagerService.WallpaperData) this.mIHwWallpaperManagerInner.getWallpaperMap().get(wallpaperUserId);
            if (wallpaper != null) {
                if (wallpaper.getCallbacks() != null) {
                    if (cb != null && !wallpaper.getCallbacks().isContainIBinder(cb)) {
                        wallpaper.getCallbacks().register(cb);
                    }
                    try {
                        File f = new File(Environment.getUserSystemDirectory(wallpaperUserId), "blurwallpaper");
                        if (!f.exists()) {
                            return null;
                        }
                        return ParcelFileDescriptor.open(f, 268435456);
                    } catch (FileNotFoundException e) {
                        Slog.w(TAG, "Error getting wallpaper", e);
                        return null;
                    }
                }
            }
            return null;
        }
    }

    public void updateWallpaperOffsets(WallpaperManagerService.WallpaperData wallpaper) {
        int i;
        Bitmap bm = getCurrentWallpaperLocked();
        if (bm != null) {
            DisplayMetrics metrics = new DisplayMetrics();
            Display display = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
            display.getMetrics(metrics);
            bm.setDensity(metrics.noncompatDensityDpi);
            Point size = new Point();
            display.getRealSize(size);
            int min = size.x < size.y ? size.x : size.y;
            int max = size.x > size.y ? size.x : size.y;
            int width = bm.getWidth();
            int height = bm.getHeight();
            if ((height == max || width == max) && (width == min * 2 || width == max)) {
                if (wallpaper.nextOffsets[0] == -1) {
                    i = 0;
                    wallpaper.currOffsets[0] = -1;
                } else {
                    i = 0;
                    if (wallpaper.nextOffsets[0] < -1) {
                        wallpaper.currOffsets[0] = 0;
                    } else if (wallpaper.nextOffsets[0] <= width - min) {
                        wallpaper.currOffsets[0] = wallpaper.nextOffsets[0];
                    } else if (wallpaper.nextOffsets[0] > width - min) {
                        wallpaper.currOffsets[0] = width - min;
                    }
                }
                wallpaper.currOffsets[1] = i;
                wallpaper.currOffsets[2] = i;
                if (wallpaper.nextOffsets[3] == -1) {
                    wallpaper.currOffsets[3] = -1;
                } else if (wallpaper.nextOffsets[3] < -1) {
                    wallpaper.currOffsets[3] = 0;
                } else if (wallpaper.nextOffsets[3] <= height - min) {
                    wallpaper.currOffsets[3] = wallpaper.nextOffsets[3];
                } else if (wallpaper.nextOffsets[3] > height - min) {
                    wallpaper.currOffsets[3] = height - min;
                }
            } else {
                Slog.w(TAG, "Irregular bitmap: width=" + width + ", height=" + height);
                for (int i2 = 0; i2 < wallpaper.currOffsets.length; i2++) {
                    wallpaper.currOffsets[i2] = -1;
                }
            }
            saveWallpaperOffsets(wallpaper.currOffsets);
            if (!bm.isRecycled()) {
                bm.recycle();
            }
        }
    }

    private void saveWallpaperOffsets(int[] offsets) {
        if (offsets != null) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < offsets.length; i++) {
                sb.append(offsets[i]);
                if (i < offsets.length - 1) {
                    sb.append(",");
                }
            }
            int userId = getWallpaperUserId();
            Settings.System.putStringForUser(this.mContext.getContentResolver(), "curr_wallpaper_offsets", sb.toString(), userId);
            Slog.i(TAG, "saveWallpaperOffsets(" + userId + "):" + ((Object) sb));
        }
    }

    public int[] getCurrOffsets() {
        synchronized (this.mIHwWallpaperManagerInner.getLock()) {
            int[] offsets = {-1, -1, -1, -1};
            int userId = getWallpaperUserId();
            WallpaperManagerService.WallpaperData wallpaper = (WallpaperManagerService.WallpaperData) this.mIHwWallpaperManagerInner.getWallpaperMap().get(userId);
            String wallpaperOffsets = Settings.System.getStringForUser(this.mContext.getContentResolver(), "curr_wallpaper_offsets", userId);
            if (wallpaper == null) {
                Slog.e(TAG, "getCurrOffsets error, wallpaper is null");
                return offsets;
            }
            if (wallpaperOffsets != null) {
                String[] str = wallpaperOffsets.split(",");
                if (str.length == wallpaper.currOffsets.length) {
                    for (int i = 0; i < wallpaper.currOffsets.length; i++) {
                        wallpaper.currOffsets[i] = Integer.parseInt(str[i]);
                    }
                }
            }
            System.arraycopy(wallpaper.currOffsets, 0, offsets, 0, wallpaper.currOffsets.length);
            Slog.i(TAG, "getCurrOffsets(" + userId + "):" + offsets[0] + "," + offsets[1] + "," + offsets[2] + "," + offsets[3]);
            return offsets;
        }
    }

    public void setNextOffsets(int[] offsets) {
        synchronized (this.mIHwWallpaperManagerInner.getLock()) {
            WallpaperManagerService.WallpaperData wallpaper = (WallpaperManagerService.WallpaperData) this.mIHwWallpaperManagerInner.getWallpaperMap().get(UserHandle.getCallingUserId());
            if (!(offsets == null || wallpaper == null || offsets.length != wallpaper.nextOffsets.length)) {
                System.arraycopy(offsets, 0, wallpaper.nextOffsets, 0, wallpaper.nextOffsets.length);
                Slog.i(TAG, "setNextOffsets:" + offsets[0] + "," + offsets[1] + "," + offsets[2] + "," + offsets[3]);
            }
        }
    }

    public void setCurrOffsets(int[] offsets) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, null);
        synchronized (this.mIHwWallpaperManagerInner.getLock()) {
            WallpaperManagerService.WallpaperData wallpaper = (WallpaperManagerService.WallpaperData) this.mIHwWallpaperManagerInner.getWallpaperMap().get(getWallpaperUserId());
            if (!(offsets == null || wallpaper == null || offsets.length != wallpaper.currOffsets.length)) {
                System.arraycopy(offsets, 0, wallpaper.currOffsets, 0, wallpaper.currOffsets.length);
                Slog.i(TAG, "setCurrOffsets:" + offsets[0] + "," + offsets[1] + "," + offsets[2] + "," + offsets[3]);
            }
        }
    }

    public void reportWallpaper(ComponentName componentName) {
        HwSysResManager resManager;
        if (componentName != null && (resManager = HwSysResManager.getInstance()) != null && resManager.isResourceNeeded(AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RESOURCE_APPASSOC))) {
            Bundle bundleArgs = new Bundle();
            bundleArgs.putString("pkgname", componentName.getPackageName());
            bundleArgs.putInt("relationType", 17);
            CollectData data = new CollectData(AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RESOURCE_APPASSOC), System.currentTimeMillis(), bundleArgs);
            long id = Binder.clearCallingIdentity();
            resManager.reportData(data);
            Binder.restoreCallingIdentity(id);
        }
    }

    public ParcelFileDescriptor scaleBitmaptoFileDescriptor(Bitmap bitmap, float scale, Bitmap.Config config) {
        if (bitmap == null) {
            Slog.w(TAG, "bitmap is null");
            return null;
        }
        Bitmap result = Bitmap.createScaledBitmap(bitmap, (int) (((float) bitmap.getWidth()) * scale), (int) (((float) bitmap.getHeight()) * scale), true);
        if (result == null) {
            Slog.w(TAG, "createScaledBitmap return is null");
            return null;
        }
        if (config != null) {
            result = result.copy(config, true);
        }
        File file = saveWallpaperBitmapLocked(result, "live_wallpaper_screenshot", 100);
        if (file == null || !file.exists()) {
            Slog.w(TAG, "saveWallpaperBitmapLocked file is null");
            return null;
        }
        try {
            return ParcelFileDescriptor.open(file, 268435456);
        } catch (FileNotFoundException e) {
            Slog.e(TAG, "scale bitmap file not found error");
            return null;
        }
    }
}
