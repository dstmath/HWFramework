package huawei.android.app;

import android.app.Activity;
import android.app.ActivityThread;
import android.app.Application;
import android.app.LoadedApk;
import android.common.HwActivityThread;
import android.common.HwFrameworkFactory;
import android.common.HwFrameworkMonitor;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.IAwareBitmapCacher;
import android.graphics.drawable.Drawable;
import android.hwcontrol.HwWidgetFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.MessageQueue;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.rms.iaware.AppSceneRecogManager;
import android.rms.iaware.AppTypeInfo;
import android.rms.iaware.AwareAppLiteSysLoadManager;
import android.rms.iaware.AwareAppScheduleManager;
import android.util.Log;
import android.view.View;
import com.android.internal.os.BackgroundThread;
import com.huawei.android.app.HwActivityManager;
import com.huawei.android.content.pm.HwPackageManager;
import com.huawei.hsm.permission.StubController;
import dalvik.system.VMDebug;
import dalvik.system.VMRuntime;
import huawei.android.hwcolorpicker.HwColorPicker;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Map;

public class HwActivityThreadImpl implements HwActivityThread {
    private static final int APP_CP_LIMIT = 102400;
    private static final boolean DEBUG_BIG_DATA = false;
    private static int DOWN_FACTOR = 5;
    private static final boolean IS_MAPLE_PROCESS = ActivityThread.sIsMygote;
    private static final int LOG_LIMIT = 4000;
    private static final String TAG = "HwActivityThreadImpl";
    private static final boolean mDecodeBitmapOptEnable = SystemProperties.getBoolean("persist.kirin.decodebitmap_opt", false);
    private static int mHight = 0;
    private static HwFrameworkMonitor mMonitor = HwFrameworkFactory.getHwFrameworkMonitor();
    private static int mWidth = 0;
    private static HwActivityThreadImpl sInstance;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private int mPerfOptEnable = -1;

    private class DrawThread extends Thread {
        Configuration config = null;
        Handler h = null;
        Activity r = null;

        DrawThread(String name, Activity activity, Handler handle, Configuration configuration) {
            super(name);
            this.r = activity;
            this.h = handle;
            this.config = configuration;
        }

        public void run() {
            HwActivityThreadImpl.this.setNavigationBarColor(this.r, this.h, this.config);
        }
    }

    public static synchronized HwActivityThreadImpl getDefault() {
        HwActivityThreadImpl hwActivityThreadImpl;
        synchronized (HwActivityThreadImpl.class) {
            if (sInstance == null) {
                sInstance = new HwActivityThreadImpl();
            }
            hwActivityThreadImpl = sInstance;
        }
        return hwActivityThreadImpl;
    }

    public void changeToSpecialModel(String pkgName) {
        String strHwModel = SystemProperties.get("ro.product.hw_model", "");
        if (pkgName != null && !strHwModel.equals("")) {
            if (pkgName.equals("com.sina.weibo") || pkgName.equals("com.tencent.mobileqq")) {
                try {
                    Field field = Build.class.getField("MODEL");
                    field.setAccessible(true);
                    field.set(null, strHwModel);
                } catch (IllegalAccessException | NoSuchFieldException e) {
                    Log.e(TAG, "modify Build.MODEL fail!");
                }
            }
        }
    }

    public int isPerfOptEnable(int optTypeId) {
        if (this.mPerfOptEnable != -1) {
            return this.mPerfOptEnable;
        }
        if (ActivityThread.currentActivityThread() == null) {
            return 0;
        }
        String packageName = ActivityThread.currentPackageName();
        if (packageName != null) {
            try {
                if (HwPackageManager.getService().isPerfOptEnable(packageName, optTypeId)) {
                    this.mPerfOptEnable = 1;
                } else {
                    this.mPerfOptEnable = 0;
                }
            } catch (RemoteException ex) {
                throw ex.rethrowFromSystemServer();
            }
        } else {
            this.mPerfOptEnable = 1;
        }
        return this.mPerfOptEnable;
    }

    public boolean decodeBitmapOptEnable() {
        return mDecodeBitmapOptEnable;
    }

    public void reportBindApplicationToAware(Application app, String processName) {
        AwareAppScheduleManager.getInstance().init(processName, app);
        AwareAppLiteSysLoadManager.getInstance().init(processName, app);
        IAwareBitmapCacher obj = HwFrameworkFactory.getHwIAwareBitmapCacher();
        if (obj != null) {
            obj.init(processName, app);
        }
        AppSceneRecogManager.getInstance().init(app, processName);
    }

    public Drawable getCacheDrawableFromAware(int resId, Resources wrapper, int cookie, AssetManager asset) {
        return AwareAppScheduleManager.getInstance().getCacheDrawableFromAware(resId, wrapper, cookie, asset);
    }

    public void postCacheDrawableToAware(int resId, Resources wrapper, long time, int cookie, AssetManager asset) {
        AwareAppScheduleManager.getInstance().postCacheDrawableToAware(resId, wrapper, time, cookie, asset);
    }

    public void hitDrawableCache(int resId) {
        AwareAppScheduleManager.getInstance().hitDrawableCache(resId);
    }

    public boolean getWechatScanOpt() {
        return AwareAppScheduleManager.getInstance().getWechatScanOpt();
    }

    public String getWechatScanActivity() {
        return AwareAppScheduleManager.getInstance().getWechatScanActivity();
    }

    public boolean isScene(int scene) {
        return AwareAppLiteSysLoadManager.getInstance().isInSysLoadScene(scene);
    }

    public boolean isLiteSysLoadEnable() {
        return AwareAppLiteSysLoadManager.getInstance().isLiteSysLoadEnable();
    }

    public void setNavigationBarColorFromActivityThread(Activity activity, Handler handle, Configuration configuration) {
        DrawThread thread = new DrawThread("DrawThread", activity, handle, configuration);
        thread.start();
    }

    private static void saveBitmapToFile(Bitmap bitmap, String filePath) {
        FileOutputStream outputStream = null;
        try {
            File file = new File(filePath);
            if ((file.exists() || file.isDirectory()) && !file.delete()) {
                Log.i(TAG, "saveBitmapToFile delete file error");
            }
            file.createNewFile();
            FileOutputStream outputStream2 = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 0, outputStream2);
            outputStream2.flush();
            try {
                outputStream2.close();
            } catch (IOException e) {
                Log.i(TAG, "saveBitmapToFile, finally IO Exception!");
            }
        } catch (IOException e2) {
            Log.i(TAG, "saveBitmapToFile, IO Exception!");
            if (outputStream != null) {
                outputStream.close();
            }
        } catch (Throwable th) {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e3) {
                    Log.i(TAG, "saveBitmapToFile, finally IO Exception!");
                }
            }
            throw th;
        }
    }

    private void saveNavigationBarBitmap(Activity activity) {
        if (SystemProperties.getInt("persist.sys.navigationbar.mode.savebitmap", 0) == 1) {
            View decor = activity.findViewById(16908290);
            if (decor == null) {
                Log.i(TAG, "getDecorView null");
                return;
            }
            try {
                Bitmap bitmap = Bitmap.createBitmap(decor.getWidth() / DOWN_FACTOR, decor.getHeight() / DOWN_FACTOR, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                canvas.scale(1.0f / ((float) DOWN_FACTOR), 1.0f / ((float) DOWN_FACTOR));
                canvas.translate(0.0f, 0.0f);
                canvas.drawColor(-197380);
                decor.draw(canvas);
                if (activity.checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") != 0) {
                    activity.requestPermissions(new String[]{"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"}, 1);
                }
                saveBitmapToFile(bitmap, "/sdcard/NavigationBarTest.jpg");
            } catch (Exception e) {
                Log.d(TAG, "get NavigationBar Color error!");
            }
        }
    }

    /* access modifiers changed from: private */
    public void setNavigationBarColor(final Activity activity, Handler handle, Configuration configuration) {
        if (!HwActivityManager.canPickColor(activity.getPackageName())) {
            Log.d(TAG, "pkg:" + activity.getPackageName() + " can not pick color, return!");
        } else if (activity.getWindow() == null) {
            Log.i(TAG, "getWindow null");
        } else {
            View decor = activity.findViewById(16908290);
            if (decor == null) {
                Log.i(TAG, "getDecorView null");
            } else if (decor.getWidth() < DOWN_FACTOR || decor.getHeight() < DOWN_FACTOR) {
                Log.d(TAG, "width:" + decor.getWidth() + ", height:" + decor.getHeight() + "view:" + decor + ", not pick color, return!");
            } else if (HwWidgetFactory.isHwTheme(activity)) {
                Log.d(TAG, "is HwTheme, not pick color, return!");
            } else if (activity.getWindow().isFloating() || activity.isInMultiWindowMode() || activity.isInPictureInPictureMode()) {
                Log.d(TAG, "is Floating or InMultiWindowMode or PIP Mode, not pick color, return!");
            } else if (configuration.nonFullScreen != 0) {
                Log.d(TAG, "not full screen, not pick color, return!");
            } else {
                int sysUiVisibility = activity.getWindow().getDecorView().getSystemUiVisibility();
                if ((sysUiVisibility & 2) == 0 || (sysUiVisibility & 4) == 0) {
                    if (activity.mNavigationBarColor == 0) {
                        int width = decor.getWidth() / DOWN_FACTOR;
                        int height = 40 / DOWN_FACTOR;
                        try {
                            if (!(width == mWidth && height == mHight)) {
                                this.mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                                this.mCanvas = new Canvas(this.mBitmap);
                                this.mCanvas.scale(1.0f / ((float) DOWN_FACTOR), 1.0f / ((float) DOWN_FACTOR));
                                this.mCanvas.translate(0.0f, (float) (-(decor.getHeight() - 40)));
                                mWidth = width;
                                mHight = height;
                            }
                            this.mCanvas.drawColor(-197380);
                            decor.draw(this.mCanvas);
                            HwColorPicker.PickedColor pickedColor = HwColorPicker.processBitmap(this.mBitmap);
                            int color = pickedColor.get(HwColorPicker.ResultType.Domain);
                            if (pickedColor.getState() <= 2) {
                                Log.d(TAG, "state err, return");
                                return;
                            }
                            activity.mNavigationBarColor = color;
                            saveNavigationBarBitmap(activity);
                            Log.d(TAG, "get NavigationBarColor color: " + color);
                        } catch (Exception e) {
                            Log.d(TAG, "get NavigationBar Color error!");
                        }
                    }
                    handle.post(new Runnable() {
                        public void run() {
                            try {
                                activity.getWindow().addFlags(AppTypeInfo.APP_ATTRIBUTE_OVERSEA);
                                activity.getWindow().clearFlags(512);
                                activity.getWindow().clearFlags(StubController.RHD_PERMISSION_CODE);
                                activity.getWindow().setNavigationBarColor(activity.mNavigationBarColor);
                                Log.d(HwActivityThreadImpl.TAG, "set NavigationBar Color: " + activity.mNavigationBarColor);
                            } catch (Exception e) {
                                Log.d(HwActivityThreadImpl.TAG, "set NavigationBar Color error!");
                            }
                        }
                    });
                    return;
                }
                Log.d(TAG, "hide navigation, not pick color, return!");
            }
        }
    }

    public void reportWebViewInit(Context context) {
        AwareAppScheduleManager.getInstance().reportWebViewInit(context);
    }

    public void reportLoadUrl() {
        AwareAppScheduleManager.getInstance().reportLoadUrl();
    }

    public boolean doReportRuntime(String procName, long startTime) {
        if (!IS_MAPLE_PROCESS || procName == null || startTime <= 0) {
            return false;
        }
        if ("system_server".equals(procName)) {
            BackgroundThread.get().getLooper().getQueue().addIdleHandler(new MessageQueue.IdleHandler(procName, startTime) {
                private final /* synthetic */ String f$1;
                private final /* synthetic */ long f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final boolean queueIdle() {
                    return HwActivityThreadImpl.this.doReportRuntimeByIdleHandler(this.f$1, this.f$2);
                }
            });
        } else {
            Looper.myQueue().addIdleHandler(new MessageQueue.IdleHandler(procName, startTime) {
                private final /* synthetic */ String f$1;
                private final /* synthetic */ long f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final boolean queueIdle() {
                    return HwActivityThreadImpl.this.doReportRuntimeByIdleHandler(this.f$1, this.f$2);
                }
            });
        }
        return true;
    }

    /* access modifiers changed from: private */
    public void doReportRuntimeByIdleHandler(String procName, long startTime) {
        String threads_local_water_line;
        int lengthOfWaterLine;
        String consum_class_locator;
        String reflect_manage_heap;
        String str = procName;
        Map<String, String> stats = VMDebug.getRuntimeStats();
        if (stats == null) {
        } else if (stats.size() == 8) {
            Map<String, String> map = stats;
        } else {
            int stat_duration = (int) ((System.currentTimeMillis() - startTime) / 1000);
            String gc_count = stats.get("mpl.mem.gc-count");
            String gc_max_time = stats.get("mpl.mem.gc-max-time");
            String leak_avg = stats.get("mpl.mem.leak-avg");
            String leak_peak = stats.get("mpl.mem.leak-peak");
            String allocation_utilization = stats.get("mpl.mem.allocation-utilization");
            String allocation_abnormal_count = stats.get("mpl.mem.allocation-abnormal-count");
            String rc_abnormal_count = stats.get("mpl.mem.rc-abnormal-count");
            String global_water_line = stats.get("mpl.ref.global-water-line");
            String weak_water_line = stats.get("mpl.ref.weak-water-line");
            String threads_local_water_line2 = stats.get("mpl.ref.threads-local-water-line");
            String consum_class_locator2 = stats.get("mpl.mem.consum-class-locator");
            String reflect_manage_heap2 = stats.get("mpl.mem.reflect-manage-heap");
            String gc_manage_heap = stats.get("mpl.mem.gc-manage-heap");
            String cycle_pattern = stats.get("mpl.mem.cycle-pattern");
            Map<String, String> map2 = stats;
            int lengthOfWaterLine2 = threads_local_water_line2.length();
            String consum_mpl_files = stats.get("mpl.mem.consum-mpl-files");
            int lengthOfCyclePattern = cycle_pattern.length();
            String native_table_size = stats.get("mpl.ref.native-table-size");
            if (lengthOfWaterLine2 > LOG_LIMIT) {
                int i = lengthOfWaterLine2;
                lengthOfWaterLine = 0;
                threads_local_water_line = threads_local_water_line2.substring(0, LOG_LIMIT);
            } else {
                lengthOfWaterLine = 0;
                threads_local_water_line = threads_local_water_line2;
            }
            if (lengthOfCyclePattern > LOG_LIMIT) {
                cycle_pattern = cycle_pattern.substring(lengthOfWaterLine, LOG_LIMIT);
            }
            Bundle data = new Bundle();
            try {
                data.putString("proc_name", str);
                data.putInt("stat_duration", stat_duration);
                data.putInt("circref_rcycl_cnt", Integer.valueOf(gc_count).intValue());
                data.putInt("circref_rcycl_max_duration", Integer.valueOf(gc_max_time).intValue() / 1000000);
                data.putInt("mem_leak_avrg", Integer.valueOf(leak_avg).intValue());
                data.putInt("mem_leak_peak", Integer.valueOf(leak_peak).intValue());
                data.putFloat("mem_alloc_space_util", Float.valueOf(allocation_utilization).floatValue());
                data.putInt("mem_alloc_abnormal", Integer.valueOf(allocation_abnormal_count).intValue());
                data.putInt("rc_abnormal", Integer.valueOf(rc_abnormal_count).intValue());
                data.putInt("global_water_line", Integer.valueOf(global_water_line).intValue());
                data.putInt("weak_water_line", Integer.valueOf(weak_water_line).intValue());
                try {
                    data.putString("threads_local_water_line", threads_local_water_line);
                    int i2 = stat_duration;
                    String str2 = gc_count;
                    String native_table_size2 = native_table_size;
                    try {
                        data.putInt("native_table_size", Integer.valueOf(native_table_size2).intValue());
                        String str3 = native_table_size2;
                        String consum_mpl_files2 = consum_mpl_files;
                        try {
                            data.putInt("consum_mpl_files", Integer.valueOf(consum_mpl_files2).intValue());
                            String str4 = consum_mpl_files2;
                            String consum_class_locator3 = consum_class_locator2;
                            try {
                                data.putInt("consum_class_locator", Integer.valueOf(consum_class_locator3).intValue());
                                String str5 = consum_class_locator3;
                                reflect_manage_heap = reflect_manage_heap2;
                            } catch (NumberFormatException e) {
                                e = e;
                                Bundle bundle = data;
                                String str6 = consum_class_locator3;
                                String str7 = reflect_manage_heap2;
                                consum_class_locator = gc_manage_heap;
                                StringBuilder sb = new StringBuilder();
                                String str8 = consum_class_locator;
                                sb.append("upload bigdata decode failed: ");
                                sb.append(str);
                                Log.e(TAG, sb.toString(), e);
                            }
                        } catch (NumberFormatException e2) {
                            e = e2;
                            Bundle bundle2 = data;
                            String str9 = consum_mpl_files2;
                            String str10 = consum_class_locator2;
                            String str11 = reflect_manage_heap2;
                            consum_class_locator = gc_manage_heap;
                            StringBuilder sb2 = new StringBuilder();
                            String str82 = consum_class_locator;
                            sb2.append("upload bigdata decode failed: ");
                            sb2.append(str);
                            Log.e(TAG, sb2.toString(), e);
                        }
                    } catch (NumberFormatException e3) {
                        e = e3;
                        Bundle bundle3 = data;
                        String str12 = native_table_size2;
                        String str13 = consum_class_locator2;
                        String str14 = reflect_manage_heap2;
                        consum_class_locator = gc_manage_heap;
                        String str15 = consum_mpl_files;
                        StringBuilder sb22 = new StringBuilder();
                        String str822 = consum_class_locator;
                        sb22.append("upload bigdata decode failed: ");
                        sb22.append(str);
                        Log.e(TAG, sb22.toString(), e);
                    }
                } catch (NumberFormatException e4) {
                    e = e4;
                    Bundle bundle4 = data;
                    int i3 = stat_duration;
                    String str16 = gc_count;
                    String str17 = consum_class_locator2;
                    String str18 = reflect_manage_heap2;
                    consum_class_locator = gc_manage_heap;
                    String str19 = consum_mpl_files;
                    String str20 = native_table_size;
                    StringBuilder sb222 = new StringBuilder();
                    String str8222 = consum_class_locator;
                    sb222.append("upload bigdata decode failed: ");
                    sb222.append(str);
                    Log.e(TAG, sb222.toString(), e);
                }
                try {
                    data.putInt("reflect_manage_heap", Integer.valueOf(reflect_manage_heap).intValue());
                    String str21 = reflect_manage_heap;
                    consum_class_locator = gc_manage_heap;
                    try {
                        data.putInt("gc_manage_heap", Integer.valueOf(consum_class_locator).intValue());
                        data.putString("cycle_pattern", cycle_pattern);
                        if (mMonitor == null || !mMonitor.monitor(942030001, data)) {
                        } else {
                            StringBuilder sb3 = new StringBuilder();
                            Bundle bundle5 = data;
                            sb3.append("upload bigdata success for: ");
                            sb3.append(str);
                            Log.i(TAG, sb3.toString());
                        }
                    } catch (NumberFormatException e5) {
                        e = e5;
                        Bundle bundle6 = data;
                        StringBuilder sb2222 = new StringBuilder();
                        String str82222 = consum_class_locator;
                        sb2222.append("upload bigdata decode failed: ");
                        sb2222.append(str);
                        Log.e(TAG, sb2222.toString(), e);
                    }
                } catch (NumberFormatException e6) {
                    e = e6;
                    Bundle bundle7 = data;
                    String str22 = reflect_manage_heap;
                    consum_class_locator = gc_manage_heap;
                    StringBuilder sb22222 = new StringBuilder();
                    String str822222 = consum_class_locator;
                    sb22222.append("upload bigdata decode failed: ");
                    sb22222.append(str);
                    Log.e(TAG, sb22222.toString(), e);
                }
            } catch (NumberFormatException e7) {
                e = e7;
                Bundle bundle8 = data;
                int i4 = stat_duration;
                String str23 = gc_count;
                String str24 = consum_class_locator2;
                String str25 = reflect_manage_heap2;
                consum_class_locator = gc_manage_heap;
                String str26 = consum_mpl_files;
                String str27 = native_table_size;
                String str28 = threads_local_water_line;
                StringBuilder sb222222 = new StringBuilder();
                String str8222222 = consum_class_locator;
                sb222222.append("upload bigdata decode failed: ");
                sb222222.append(str);
                Log.e(TAG, sb222222.toString(), e);
            }
        }
    }

    public void loadAppCyclePatternAsync(LoadedApk info, ApplicationInfo appInfo, String processName) {
        if (ActivityThread.sIsMygote && info != null && appInfo != null && processName != null) {
            VMRuntime runtime = VMRuntime.getRuntime();
            if (VMRuntime.loadAppCyclePattern(appInfo.packageName, processName, appInfo.dataDir, appInfo.longVersionCode, 1)) {
                copyCyclePatternFromAssets(info, "cycle.pattern", appInfo.dataDir + "/cycle-pattern", "cycle.pattern." + appInfo.longVersionCode + "." + processName);
                VMRuntime.loadAppCyclePattern(appInfo.packageName, processName, appInfo.dataDir, appInfo.longVersionCode, 2);
            }
            VMRuntime.loadAppCyclePattern(appInfo.packageName, processName, appInfo.dataDir, appInfo.longVersionCode, 0);
        }
    }

    private void copyCyclePatternFromAssets(LoadedApk info, String oldFile, String newPath, String newName) {
        InputStream is = null;
        FileOutputStream fos = null;
        try {
            new File(newPath).mkdirs();
            is = info.getAssets().open(oldFile);
            int size = is.available();
            if (size > 0) {
                if (size <= APP_CP_LIMIT) {
                    fos = new FileOutputStream(new File(newPath + "/" + newName));
                    byte[] buffer = new byte[1024];
                    while (true) {
                        int read = is.read(buffer);
                        int byteCount = read;
                        if (read == -1) {
                            break;
                        }
                        fos.write(buffer, 0, byteCount);
                    }
                    fos.flush();
                    closeStream(is);
                    closeStream(fos);
                    return;
                }
            }
            Log.w(TAG, "Failed to copy cycle.pattern since size=" + size + ", limit=" + APP_CP_LIMIT);
            closeStream(is);
            closeStream(null);
        } catch (FileNotFoundException e) {
            Log.w(TAG, "copyCyclePatternFromAssets catch file not found");
        } catch (IOException e2) {
            Log.w(TAG, "copyCyclePatternFromAssets catch IO exception");
        } catch (Throwable th) {
            closeStream(null);
            closeStream(null);
            throw th;
        }
    }

    private void closeStream(Closeable io) {
        if (io != null) {
            try {
                io.close();
            } catch (IOException e) {
                Log.w(TAG, "closeStream error");
            }
        }
    }
}
