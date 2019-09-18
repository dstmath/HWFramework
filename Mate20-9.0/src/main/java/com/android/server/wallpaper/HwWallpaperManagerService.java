package com.android.server.wallpaper;

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
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
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
import android.os.SystemProperties;
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
import com.android.server.SystemService;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.wallpaper.WallpaperManagerService;
import huawei.android.hwpicaveragenoises.HwPicAverageNoises;
import huawei.android.hwutil.FileUtil;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class HwWallpaperManagerService extends WallpaperManagerService {
    public static final boolean DEBUG = false;
    private static final String PERMISSION = "com.huawei.wallpaperservcie.permission.SET_WALLPAPER_OFFSET";
    private static final String PERMISSIONS_APS_RESOLUTION_CHANGE_ACTION = "huawei.intent.permissions.APS_RESOLUTION_CHANGE_ACTION";
    private static final String ROG_KILL_APP_END_ACTION = "com.huawei.systemmamanger.action.KILL_ROGAPP_END";
    static final String TAG = "HwWallpaperService";
    public static final String WALLPAPER_MANAGER_SERVICE_READY_ACTION = "android.intent.action.WALLPAPER_MANAGER_SERVICE_READY";
    protected int disHeight;
    protected int disWidth;

    public static class Lifecycle extends SystemService {
        private HwWallpaperManagerService mService;

        public Lifecycle(Context context) {
            super(context);
        }

        /* JADX WARNING: type inference failed for: r1v1, types: [com.android.server.wallpaper.HwWallpaperManagerService, android.os.IBinder] */
        public void onStart() {
            this.mService = new HwWallpaperManagerService(getContext());
            publishBinderService("wallpaper", this.mService);
        }

        public void onBootPhase(int phase) {
            Slog.d(HwWallpaperManagerService.TAG, "HwWallpaperManagerService.java#Lifecycle#systemRunning() phase:" + phase);
            if (phase == 550) {
                this.mService.systemRunning();
            } else if (phase == 600) {
                this.mService.switchUser(0, null);
            } else if (phase == 1000) {
                this.mService.wallpaperManagerServiceReady();
            }
        }

        public void onUnlockUser(int userHandle) {
            this.mService.onUnlockUser(userHandle);
        }
    }

    public HwWallpaperManagerService(Context context) {
        super(context);
        DisplayMetrics dm = new DisplayMetrics();
        ((WindowManager) getContext().getSystemService("window")).getDefaultDisplay().getMetrics(dm);
        this.disWidth = dm.widthPixels;
        this.disHeight = dm.heightPixels;
    }

    public void wallpaperManagerServiceReady() {
        createBlurWallpaper(false);
        Slog.i(TAG, "HwWallpaperManagerService.java#wallpaperManagerServiceReady() create blur wallpaper");
    }

    public void systemRunning() {
        HwWallpaperManagerService.super.systemReady();
        WallpaperManagerService.WallpaperData wallpaper = (WallpaperManagerService.WallpaperData) getWallpaperMap().get(0);
        synchronized (getLock()) {
            notifyBlurCallbacksLocked(wallpaper);
        }
        IntentFilter userFilter = new IntentFilter();
        userFilter.addAction(ROG_KILL_APP_END_ACTION);
        getContext().registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (HwWallpaperManagerService.ROG_KILL_APP_END_ACTION.equals(intent.getAction())) {
                    HwWallpaperManagerService.this.restartLiveWallpaperService();
                }
            }
        }, userFilter, PERMISSIONS_APS_RESOLUTION_CHANGE_ACTION, null);
    }

    public void handleWallpaperObserverEvent(final WallpaperManagerService.WallpaperData wallpaper) {
        new Thread("BlurWallpaperThread") {
            public void run() {
                Slog.d(HwWallpaperManagerService.TAG, "onEvent createBlurBitmap Blur wallpaper Begin");
                long timeBegin = System.currentTimeMillis();
                HwWallpaperManagerService.this.createBlurWallpaper(true);
                Slog.d(HwWallpaperManagerService.TAG, "onEvent createBlurBitmap Blur takenTime = " + (System.currentTimeMillis() - timeBegin));
                synchronized (HwWallpaperManagerService.this.getLock()) {
                    HwWallpaperManagerService.this.notifyBlurCallbacksLocked(wallpaper);
                }
            }
        }.start();
    }

    public void createBlurWallpaper(boolean force) {
        Bitmap wallpaper;
        String str;
        Bitmap bitmap;
        synchronized (getLock()) {
            if (!force) {
                try {
                    int wallpaperUserId = getWallpaperUserId();
                    String blurWallpaperPathName = Environment.getUserSystemDirectory(wallpaperUserId).getAbsolutePath() + "/blurwallpaper";
                    if (FileUtil.isFileExists(blurWallpaperPathName) && !FileUtil.isFileContentAllZero(blurWallpaperPathName)) {
                        return;
                    }
                } catch (OutOfMemoryError e) {
                    Slog.w(TAG, "No memory load current wallpaper", e);
                } catch (Throwable th) {
                    throw th;
                }
            }
            Object obj = "";
            WallpaperInfo info = getWallpaperInfo(getWallpaperUserId());
            if (info == null) {
                wallpaper = getCurrentWallpaperLocked(getContext());
                str = "static";
            } else {
                Drawable dr = HwFrameworkFactory.getHwWallpaperInfoStub(info).loadThumbnailWithoutTheme(getContext().getPackageManager());
                if (dr == null) {
                    bitmap = null;
                } else if (dr instanceof BitmapDrawable) {
                    bitmap = ((BitmapDrawable) dr).getBitmap();
                } else {
                    try {
                        wallpaper = Bitmap.createBitmap(dr.getIntrinsicWidth(), dr.getIntrinsicHeight(), dr.getOpacity() != -1 ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
                        Canvas c = new Canvas(wallpaper);
                        dr.setBounds(0, 0, dr.getIntrinsicWidth(), dr.getIntrinsicHeight());
                        dr.draw(c);
                    } catch (RuntimeException e2) {
                        Slog.e(TAG, "create blurwallpaper draw has some errors");
                        bitmap = null;
                    }
                    str = "live";
                }
                wallpaper = bitmap;
                str = "live";
            }
            String bitmapStyle = str;
            if (wallpaper == null || wallpaper.isRecycled()) {
                Slog.d(TAG, "wallpaper is null or has been recycled");
                wallpaper = getDefaultWallpaperLocked(getContext());
            }
            if (wallpaper != null) {
                int h = this.disHeight;
                int w = this.disWidth;
                if (!ViewConfiguration.get(getContext()).hasPermanentMenuKey()) {
                    h += getContext().getResources().getDimensionPixelSize(17105186);
                }
                if (wallpaper.getHeight() == h && wallpaper.getWidth() == 2 * w) {
                    int[] inPixels = new int[(this.disWidth * h)];
                    Bitmap bitmap2 = Bitmap.createBitmap(this.disWidth, h, Bitmap.Config.ARGB_8888);
                    wallpaper.getPixels(inPixels, 0, this.disWidth, 0, 0, this.disWidth, h);
                    bitmap2.setPixels(inPixels, 0, this.disWidth, 0, 0, this.disWidth, h);
                    Bitmap bitmap3 = bitmap2;
                    if (bitmap3 != wallpaper && !wallpaper.isRecycled()) {
                        wallpaper.recycle();
                        wallpaper = bitmap3;
                        if (info != null) {
                            removeThumbnailCache(info);
                        }
                    }
                }
                Bitmap bitmap4 = wallpaper;
                if (!(wallpaper.getHeight() == h / 4 && wallpaper.getWidth() == w / 4)) {
                    wallpaper = Bitmap.createScaledBitmap(bitmap4, this.disWidth / 4, h / 4, true);
                    if (!(wallpaper == null || wallpaper == bitmap4 || bitmap4.isRecycled())) {
                        bitmap4.recycle();
                        if (info != null) {
                            removeThumbnailCache(info);
                        }
                    }
                }
            }
            if (wallpaper != null) {
                Bitmap bmp = BlurUtils.stackBlur(wallpaper, 80);
                if (bmp != null) {
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
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0056, code lost:
        return null;
     */
    public ParcelFileDescriptor getBlurWallpaper(IWallpaperManagerCallback cb) {
        synchronized (getLock()) {
            int wallpaperUserId = getWallpaperUserId();
            WallpaperManagerService.WallpaperData wallpaper = (WallpaperManagerService.WallpaperData) getWallpaperMap().get(wallpaperUserId);
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
                        ParcelFileDescriptor open = ParcelFileDescriptor.open(f, 268435456);
                        return open;
                    } catch (FileNotFoundException e) {
                        Slog.w(TAG, "Error getting wallpaper", e);
                        return null;
                    }
                }
            }
        }
    }

    public int getWallpaperUserId() {
        int callingUid = Binder.getCallingUid();
        if (callingUid == 1000) {
            return getCurrentUserId();
        }
        return UserHandle.getUserId(callingUid);
    }

    private Bitmap getCurrentWallpaperLocked(Context context) {
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

    private Bitmap getDefaultWallpaperLocked(Context context) {
        InputStream is = HwThemeManager.getDefaultWallpaperIS(context, getCurrentUserId());
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

    /* access modifiers changed from: private */
    public void notifyBlurCallbacksLocked(WallpaperManagerService.WallpaperData wallpaper) {
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

    public ParcelFileDescriptor getWallpaper(Bundle outParams) {
        synchronized (getLock()) {
            int wallpaperUserId = getWallpaperUserId();
            WallpaperManagerService.WallpaperData wallpaper = (WallpaperManagerService.WallpaperData) getWallpaperMap().get(wallpaperUserId);
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
            ParcelFileDescriptor open = ParcelFileDescriptor.open(f, 268435456);
            return open;
        }
    }

    /* JADX WARNING: Unknown top exception splitter block from list: {B:30:0x0087=Splitter:B:30:0x0087, B:19:0x005a=Splitter:B:19:0x005a} */
    private void saveBlurWallpaperBitmapLocked(Bitmap blurImg) {
        ParcelFileDescriptor fd;
        FileOutputStream fos;
        Slog.d(TAG, "saveBlurWallpaperBitmapLocked begin");
        long timeBegin = System.currentTimeMillis();
        try {
            File dir = Environment.getUserSystemDirectory(getWallpaperUserId());
            if (!dir.exists() && dir.mkdir()) {
                FileUtils.setPermissions(dir.getPath(), 505, -1, -1);
            }
            File file = new File(dir, "blurwallpaper");
            fd = ParcelFileDescriptor.open(file, 939524096);
            if (SELinux.restorecon(file)) {
                fos = null;
                fos = new ParcelFileDescriptor.AutoCloseOutputStream(fd);
                blurImg.compress(Bitmap.CompressFormat.PNG, 90, fos);
                try {
                    fos.close();
                } catch (IOException e) {
                }
                if (fd != null) {
                    try {
                        fd.close();
                    } catch (IOException e2) {
                    }
                }
                Slog.d(TAG, "saveBlurWallpaperBitmapLocked takenTime = " + (System.currentTimeMillis() - timeBegin));
            }
            if (fd != null) {
                try {
                    fd.close();
                } catch (IOException e3) {
                }
            }
        } catch (FileNotFoundException e4) {
            Slog.w(TAG, "Error setting wallpaper", e4);
        } catch (Throwable th) {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e5) {
                }
            }
            if (fd != null) {
                try {
                    fd.close();
                } catch (IOException e6) {
                }
            }
            throw th;
        }
    }

    private Bitmap generateBitmap(Context context, Bitmap bm, int width, int height, WallpaperInfo info) {
        Context context2 = context;
        Bitmap bm2 = bm;
        int i = width;
        int i2 = height;
        WallpaperInfo wallpaperInfo = info;
        synchronized (getLock()) {
            if (context2 == null || bm2 == null) {
                return null;
            }
            try {
                DisplayMetrics metrics = new DisplayMetrics();
                ((WindowManager) context2.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
                bm2.setDensity(metrics.noncompatDensityDpi);
                if (i <= 0 || i2 <= 0 || (bm.getWidth() == i && bm.getHeight() == i2)) {
                    return bm2;
                }
                try {
                    Bitmap newBimap = Bitmap.createBitmap(i, i2, Bitmap.Config.ARGB_8888);
                    if (newBimap == null) {
                        Slog.w(TAG, "Can't generate bitmap, newBimap = null");
                        return bm2;
                    }
                    newBimap.setDensity(metrics.noncompatDensityDpi);
                    Canvas c = new Canvas(newBimap);
                    Rect targetRect = new Rect();
                    targetRect.right = bm.getWidth();
                    targetRect.bottom = bm.getHeight();
                    int deltaw = i - targetRect.right;
                    int deltah = i2 - targetRect.bottom;
                    if (deltaw > 0 || deltah > 0) {
                        float tempWidth = ((float) i) / ((float) targetRect.right);
                        float tempHeight = ((float) i2) / ((float) targetRect.bottom);
                        float scale = tempWidth > tempHeight ? tempWidth : tempHeight;
                        float f = tempHeight;
                        targetRect.right = (int) (((float) targetRect.right) * scale);
                        targetRect.bottom = (int) (((float) targetRect.bottom) * scale);
                        deltaw = i - targetRect.right;
                        deltah = i2 - targetRect.bottom;
                    }
                    targetRect.offset(deltaw / 2, deltah / 2);
                    Paint paint = new Paint();
                    paint.setFilterBitmap(true);
                    paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
                    c.drawBitmap(bm2, null, targetRect, paint);
                    bm.recycle();
                    bm2 = null;
                    if (wallpaperInfo != null) {
                        try {
                            removeThumbnailCache(wallpaperInfo);
                        } catch (OutOfMemoryError e) {
                            e = e;
                        }
                    }
                    return newBimap;
                } catch (OutOfMemoryError e2) {
                    e = e2;
                    Slog.w(TAG, "Can't generate bitmap:", e);
                    return bm2;
                } catch (Throwable th) {
                    th = th;
                    throw th;
                }
            } catch (Throwable th2) {
                th = th2;
                throw th;
            }
        }
    }

    private void removeThumbnailCache(WallpaperInfo info) {
        if (info != null) {
            info.removeThumbnailCache(getContext().getPackageManager());
        }
    }

    /* access modifiers changed from: protected */
    public boolean getHwWallpaperWidth(WallpaperManagerService.WallpaperData wallpaper, boolean success) {
        if (success) {
            return false;
        }
        Display d = ((WindowManager) getContext().getSystemService("window")).getDefaultDisplay();
        Point pSize = new Point();
        d.getSize(pSize);
        wallpaper.setWidth(SystemProperties.getInt("ro.config.hwwallpaperwidth", 2 * pSize.x));
        Slog.i(TAG, "loadSettings, mWidth:" + wallpaper.getWidth() + ", mHeight:" + wallpaper.getHeight() + ", display size:" + pSize);
        return true;
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
            int i2 = getWallpaperUserId();
            Settings.System.putStringForUser(getContext().getContentResolver(), "curr_wallpaper_offsets", sb.toString(), i2);
            Slog.i(TAG, "saveWallpaperOffsets(" + i2 + "):" + sb);
        }
    }

    /* access modifiers changed from: protected */
    public void updateWallpaperOffsets(WallpaperManagerService.WallpaperData wallpaper) {
        WallpaperManagerService.WallpaperData wallpaperData = wallpaper;
        Bitmap bm = getCurrentWallpaperLocked(getContext());
        if (bm != null) {
            DisplayMetrics metrics = new DisplayMetrics();
            Display display = ((WindowManager) getContext().getSystemService("window")).getDefaultDisplay();
            display.getMetrics(metrics);
            bm.setDensity(metrics.noncompatDensityDpi);
            Point size = new Point();
            display.getRealSize(size);
            int min = size.x < size.y ? size.x : size.y;
            int max = size.x > size.y ? size.x : size.y;
            int width = bm.getWidth();
            int height = bm.getHeight();
            int i = 0;
            if ((height != max && width != max) || (width != 2 * min && width != max)) {
                Slog.w(TAG, "Irregular bitmap: width=" + width + ", height=" + height);
                while (true) {
                    int i2 = i;
                    if (i2 >= wallpaperData.currOffsets.length) {
                        break;
                    }
                    wallpaperData.currOffsets[i2] = -1;
                    i = i2 + 1;
                }
            } else {
                if (wallpaperData.nextOffsets[0] == -1) {
                    wallpaperData.currOffsets[0] = -1;
                } else if (wallpaperData.nextOffsets[0] < -1) {
                    wallpaperData.currOffsets[0] = 0;
                } else if (wallpaperData.nextOffsets[0] <= width - min) {
                    wallpaperData.currOffsets[0] = wallpaperData.nextOffsets[0];
                } else if (wallpaperData.nextOffsets[0] > width - min) {
                    wallpaperData.currOffsets[0] = width - min;
                }
                wallpaperData.currOffsets[1] = 0;
                wallpaperData.currOffsets[2] = 0;
                if (wallpaperData.nextOffsets[3] == -1) {
                    wallpaperData.currOffsets[3] = -1;
                } else if (wallpaperData.nextOffsets[3] < -1) {
                    wallpaperData.currOffsets[3] = 0;
                } else if (wallpaperData.nextOffsets[3] <= height - min) {
                    wallpaperData.currOffsets[3] = wallpaperData.nextOffsets[3];
                } else if (wallpaperData.nextOffsets[3] > height - min) {
                    wallpaperData.currOffsets[3] = height - min;
                }
            }
            saveWallpaperOffsets(wallpaperData.currOffsets);
            if (!bm.isRecycled()) {
                bm.recycle();
            }
        }
    }

    public int[] getCurrOffsets() throws RemoteException {
        int[] offsets;
        synchronized (getLock()) {
            offsets = new int[]{-1, -1, -1, -1};
            int userId = getWallpaperUserId();
            WallpaperManagerService.WallpaperData wallpaper = (WallpaperManagerService.WallpaperData) getWallpaperMap().get(userId);
            String wallpaperOffsets = Settings.System.getStringForUser(getContext().getContentResolver(), "curr_wallpaper_offsets", userId);
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
        }
        return offsets;
    }

    public void setCurrOffsets(int[] offsets) throws RemoteException {
        getContext().enforceCallingOrSelfPermission(PERMISSION, null);
        synchronized (getLock()) {
            WallpaperManagerService.WallpaperData wallpaper = (WallpaperManagerService.WallpaperData) getWallpaperMap().get(getWallpaperUserId());
            if (!(offsets == null || wallpaper == null || offsets.length != wallpaper.currOffsets.length)) {
                System.arraycopy(offsets, 0, wallpaper.currOffsets, 0, wallpaper.currOffsets.length);
                Slog.i(TAG, "setCurrOffsets:" + offsets[0] + "," + offsets[1] + "," + offsets[2] + "," + offsets[3]);
            }
        }
    }

    public void setNextOffsets(int[] offsets) throws RemoteException {
        synchronized (getLock()) {
            WallpaperManagerService.WallpaperData wallpaper = (WallpaperManagerService.WallpaperData) getWallpaperMap().get(UserHandle.getCallingUserId());
            if (!(offsets == null || wallpaper == null || offsets.length != wallpaper.nextOffsets.length)) {
                System.arraycopy(offsets, 0, wallpaper.nextOffsets, 0, wallpaper.nextOffsets.length);
                Slog.i(TAG, "setNextOffsets:" + offsets[0] + "," + offsets[1] + "," + offsets[2] + "," + offsets[3]);
            }
        }
    }

    public Bitmap scaleWallpaperBitmapToScreenSize(Bitmap bitmap) {
        Bitmap bmp;
        if (bitmap == null) {
            return null;
        }
        Matrix m = new Matrix();
        Point size = new Point();
        int longSideLength = 0;
        int shortSideLength = 0;
        int bitmap_height = bitmap.getHeight();
        int bitmap_width = bitmap.getWidth();
        int bitmapLongSideLength = bitmap_height > bitmap_width ? bitmap_height : bitmap_width;
        int bitmapShortSideLength = bitmap_height > bitmap_width ? bitmap_width : bitmap_height;
        try {
            IWindowManager.Stub.asInterface(ServiceManager.checkService("window")).getBaseDisplaySize(0, size);
            longSideLength = size.x > size.y ? size.x : size.y;
            shortSideLength = size.x > size.y ? size.y : size.x;
        } catch (RemoteException e) {
            Slog.e(TAG, "IWindowManager null");
        }
        int shortSideLength2 = shortSideLength;
        if (longSideLength == 0 || shortSideLength2 == 0 || bitmapLongSideLength == longSideLength || (bitmapLongSideLength == 2 * shortSideLength2 && bitmapShortSideLength == longSideLength)) {
            int i = longSideLength;
            bmp = bitmap;
        } else {
            if ((((float) bitmapLongSideLength) / ((float) shortSideLength2)) / (((float) bitmapShortSideLength) / ((float) longSideLength)) == 2.0f) {
                longSideLength = shortSideLength2 * 2;
            }
            int longSideLength2 = longSideLength;
            float scale = ((float) longSideLength2) / ((float) bitmapLongSideLength);
            m.setScale(scale, scale);
            float f = scale;
            int i2 = longSideLength2;
            bmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap_width, bitmap_height, m, true);
        }
        return bmp;
    }

    /* access modifiers changed from: private */
    public void restartLiveWallpaperService() {
        WallpaperManagerService.WallpaperData wallpaper = (WallpaperManagerService.WallpaperData) getWallpaperMap().get(getWallpaperUserId());
        if (wallpaper != null && wallpaper.wallpaperComponent != null) {
            Slog.d(TAG, "restartLiveWallpaperService ");
            bindWallpaperComponentLocked(wallpaper.wallpaperComponent, true, false, wallpaper, null);
        }
    }

    public void reportWallpaper(ComponentName componentName) {
        if (componentName != null) {
            HwSysResManager resManager = HwSysResManager.getInstance();
            if (resManager != null && resManager.isResourceNeeded(AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RESOURCE_APPASSOC))) {
                Bundle bundleArgs = new Bundle();
                bundleArgs.putString(MemoryConstant.MEM_PREREAD_ITEM_NAME, componentName.getPackageName());
                bundleArgs.putInt("relationType", 17);
                CollectData data = new CollectData(AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RESOURCE_APPASSOC), System.currentTimeMillis(), bundleArgs);
                long id = Binder.clearCallingIdentity();
                resManager.reportData(data);
                Binder.restoreCallingIdentity(id);
            }
        }
    }
}
