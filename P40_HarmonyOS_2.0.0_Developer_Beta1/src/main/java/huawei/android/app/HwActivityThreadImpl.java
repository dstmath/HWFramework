package huawei.android.app;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.IAwareBitmapCacher;
import android.graphics.drawable.Drawable;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Process;
import android.os.RemoteException;
import android.os.StrictMode;
import android.rms.iaware.AwareAppLiteSysLoadManager;
import android.rms.iaware.AwareAppScheduleManager;
import android.rms.iaware.scenerecog.HwAppSceneImpl;
import android.util.Log;
import android.view.View;
import com.huawei.android.app.ActivityEx;
import com.huawei.android.app.ActivityThreadEx;
import com.huawei.android.app.HwActivityManager;
import com.huawei.android.content.IntentFilterEx;
import com.huawei.android.content.pm.ApplicationInfoEx;
import com.huawei.android.content.pm.HwPackageManager;
import com.huawei.android.content.res.ConfigurationAdapter;
import com.huawei.android.os.RemoteExceptionEx;
import com.huawei.android.os.ServiceManagerEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.util.SlogEx;
import com.huawei.bd.Reporter;
import com.huawei.dalvik.system.VMRuntimeEx;
import com.huawei.hwpartbasicplatform.BuildConfig;
import com.huawei.iaware.HwPartIAwareFactory;
import com.huawei.utils.HwPartFactoryWraper;
import huawei.android.hwcolorpicker.HwColorPicker;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.UUID;

public class HwActivityThreadImpl extends DefaultHwActivityThreadImpl {
    private static final int ACTIVITY_PROCESS_ARGS = 3;
    private static final int APP_CP_LIMIT = 102400;
    private static int DOWN_FACTOR = 5;
    private static final int EVENT_SCHED_THREAD_RT = 20032;
    private static final int PRELOAD_ACCEPT = 2;
    private static final int PRELOAD_CMD_CONTINUE = 1;
    private static final int PRELOAD_CONNECT = 3;
    private static final int PRELOAD_INIT = 1;
    private static final int PROCESS_PRELOADED = 2;
    private static final int PROCESS_PRELOADING = 1;
    private static final int PROCESS_STARTED = 3;
    private static final int READ_TIMEOUT = 200;
    private static final String TAG = "HwActivityThreadImpl";
    private static final long TRACE_TAG_ACTIVITY_MANAGER = 64;
    private static final boolean mDecodeBitmapOptEnable = SystemPropertiesEx.getBoolean("persist.kirin.decodebitmap_opt", false);
    private static int mHight = 0;
    private static int mWidth = 0;
    private static HwActivityThreadImpl sInstance;
    private IBinder mAwareService = null;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private int mPerfOptEnable = -1;
    private long mPreloadStatus = 0;
    private LocalServerSocket mServerSocket = null;
    private String mSocketName = null;

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
        String strHwModel = SystemPropertiesEx.get("ro.product.hw_model", BuildConfig.FLAVOR);
        if (pkgName != null && !strHwModel.equals(BuildConfig.FLAVOR)) {
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
        int i = this.mPerfOptEnable;
        if (i != -1) {
            return i;
        }
        if (ActivityThreadEx.currentActivityThread() == null) {
            return 0;
        }
        String packageName = ActivityThreadEx.currentPackageName();
        if (packageName != null) {
            try {
                if (HwPackageManager.getService().isPerfOptEnable(packageName, optTypeId)) {
                    this.mPerfOptEnable = 1;
                } else {
                    this.mPerfOptEnable = 0;
                }
            } catch (RemoteException ex) {
                throw RemoteExceptionEx.rethrowFromSystemServer(ex);
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
        HwAppSceneImpl appSceneImpl;
        AwareAppScheduleManager.getInstance().init(processName, app);
        AwareAppLiteSysLoadManager.getInstance().init(processName, app);
        IAwareBitmapCacher obj = HwPartIAwareFactory.getHwIAwareBitmapCacher();
        if (obj != null) {
            obj.init(processName, app);
        }
        if (app != null && (appSceneImpl = HwAppSceneImpl.getDefault()) != null) {
            appSceneImpl.reportBindApplicationToAware(app.getBaseContext());
        }
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

    public boolean getScanOpt() {
        return AwareAppScheduleManager.getInstance().getScanOpt();
    }

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

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            HwActivityThreadImpl.this.setNavigationBarColor(this.r, this.h, this.config);
        }
    }

    public void setNavigationBarColorFromActivityThread(Activity activity, Handler handle, Configuration configuration) {
        new DrawThread("DrawThread", activity, handle, configuration).start();
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
            if (0 != 0) {
                outputStream.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
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
        if (SystemPropertiesEx.getInt("persist.sys.navigationbar.mode.savebitmap", 0) == 1) {
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
    /* access modifiers changed from: public */
    private void setNavigationBarColor(final Activity activity, Handler handle, Configuration configuration) {
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
            } else if (HwPartFactoryWraper.isHwTheme(activity)) {
                Log.d(TAG, "is HwTheme, not pick color, return!");
            } else if (activity.getWindow().isFloating() || activity.isInMultiWindowMode() || activity.isInPictureInPictureMode()) {
                Log.d(TAG, "is Floating or InMultiWindowMode or PIP Mode, not pick color, return!");
            } else if (ConfigurationAdapter.getNonFullScreen(configuration) != 0) {
                Log.d(TAG, "not full screen, not pick color, return!");
            } else {
                int sysUiVisibility = activity.getWindow().getDecorView().getSystemUiVisibility();
                if ((sysUiVisibility & 2) == 0 || (sysUiVisibility & 4) == 0) {
                    if (ActivityEx.getNavigationBarColor(activity) == 0) {
                        int width = decor.getWidth();
                        int i = DOWN_FACTOR;
                        int width2 = width / i;
                        int height = 40 / i;
                        try {
                            if (!(width2 == mWidth && height == mHight)) {
                                this.mBitmap = Bitmap.createBitmap(width2, height, Bitmap.Config.ARGB_8888);
                                this.mCanvas = new Canvas(this.mBitmap);
                                this.mCanvas.scale(1.0f / ((float) DOWN_FACTOR), 1.0f / ((float) DOWN_FACTOR));
                                this.mCanvas.translate(0.0f, (float) (-(decor.getHeight() - 40)));
                                mWidth = width2;
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
                            ActivityEx.setNavigationBarColor(activity, color);
                            saveNavigationBarBitmap(activity);
                            Log.d(TAG, "get NavigationBarColor color: " + color);
                        } catch (Exception e) {
                            Log.d(TAG, "get NavigationBar Color error!");
                        }
                    }
                    handle.post(new Runnable() {
                        /* class huawei.android.app.HwActivityThreadImpl.AnonymousClass1 */

                        @Override // java.lang.Runnable
                        public void run() {
                            try {
                                activity.getWindow().addFlags(Integer.MIN_VALUE);
                                activity.getWindow().clearFlags(512);
                                activity.getWindow().clearFlags(134217728);
                                activity.getWindow().setNavigationBarColor(ActivityEx.getNavigationBarColor(activity));
                                Log.d(HwActivityThreadImpl.TAG, "set NavigationBar Color: " + ActivityEx.getNavigationBarColor(activity));
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

    public IntentFilter setFilterIdentifier(IntentFilter filter, BroadcastReceiver receiver, Context context, String basePackageName) {
        if (filter != null) {
            if (receiver == null || context == null || basePackageName == null) {
                IntentFilterEx.setIdentifier(filter, BuildConfig.FLAVOR);
            } else {
                int filterHashcode = getActionsHashcode(filter);
                IntentFilterEx.setIdentifier(filter, basePackageName + "+" + Process.myPid() + "+" + getObjectSimplifiedString(context) + "+" + getClassSimplifiedString(basePackageName, receiver.getClass()) + "+" + filterHashcode);
            }
        }
        return filter;
    }

    private int getActionsHashcode(IntentFilter filter) {
        Iterator<String> it;
        if (filter == null || filter.countActions() <= 0 || (it = filter.actionsIterator()) == null) {
            return 0;
        }
        StringBuilder sb = new StringBuilder();
        while (it.hasNext()) {
            sb.append(it.next());
        }
        return sb.toString().hashCode();
    }

    private static String getObjectSimplifiedString(Object obj) {
        if (obj == null) {
            return null;
        }
        return obj.getClass().getSimpleName() + "@" + Integer.toHexString(obj.hashCode());
    }

    private static String getClassSimplifiedString(String basePkgName, Class clazz) {
        if (clazz == null) {
            return null;
        }
        String className = clazz.getName();
        if (className.startsWith("com.android.server")) {
            return className.replaceFirst("com.android.server", BuildConfig.FLAVOR);
        }
        if (className.startsWith(basePkgName)) {
            return className.replaceFirst(basePkgName, BuildConfig.FLAVOR);
        }
        return className;
    }

    public void reportWebViewInit(Context context) {
        AwareAppScheduleManager.getInstance().reportWebViewInit(context);
    }

    public void reportLoadUrl() {
        AwareAppScheduleManager.getInstance().reportLoadUrl();
    }

    public boolean initHwArgs(ActivityThreadAdapterEx thread, String[] args) {
        if (thread == null || args == null) {
            return false;
        }
        return initVRArgs(thread, args);
    }

    private boolean initVRArgs(ActivityThreadAdapterEx thread, String[] args) {
        if (args.length == 3) {
            try {
                int displayId = Integer.parseInt(args[0]);
                if (HwPartFactoryWraper.getVRSystemServiceManager().isVRDisplay(displayId, Integer.parseInt(args[1]), Integer.parseInt(args[2]))) {
                    Log.i(TAG, "initVRArgs displayid = " + displayId);
                    HwPartFactoryWraper.getVRSystemServiceManager().setVRDisplayID(displayId, true);
                    thread.setDisplayId(displayId);
                    return true;
                }
            } catch (NumberFormatException e) {
                Log.i(TAG, "args format error.");
            }
        }
        return false;
    }

    public void loadAppCyclePatternAsync(AssetManager asset, ApplicationInfo appInfo, String processName) {
        if (System.getenv("MAPLE_RUNTIME") != null && asset != null && appInfo != null && processName != null) {
            if (VMRuntimeEx.loadAppCyclePattern(appInfo.packageName, processName, appInfo.dataDir, ApplicationInfoEx.getLongVersionCode(appInfo), 1)) {
                copyCyclePatternFromAssets(asset, "cycle.pattern", appInfo.dataDir + "/cycle-pattern", "cycle.pattern." + ApplicationInfoEx.getLongVersionCode(appInfo) + "." + processName);
                VMRuntimeEx.loadAppCyclePattern(appInfo.packageName, processName, appInfo.dataDir, ApplicationInfoEx.getLongVersionCode(appInfo), 2);
            }
            VMRuntimeEx.loadAppCyclePattern(appInfo.packageName, processName, appInfo.dataDir, ApplicationInfoEx.getLongVersionCode(appInfo), 0);
        }
    }

    private void copyCyclePatternFromAssets(AssetManager asset, String oldFile, String newPath, String newName) {
        InputStream is = null;
        FileOutputStream fos = null;
        try {
            if (!new File(newPath).mkdirs()) {
                Log.w(TAG, "Failed to create dir : " + newPath);
                closeStream(null);
                closeStream(null);
                return;
            }
            is = asset.open(oldFile);
            int size = is.available();
            if (size <= 0 || size > APP_CP_LIMIT) {
                Log.w(TAG, "Failed to copy cycle.pattern since size=" + size + ", limit=" + APP_CP_LIMIT);
                closeStream(is);
                closeStream(null);
                return;
            }
            fos = new FileOutputStream(new File(newPath + "/" + newName));
            byte[] buffer = new byte[Reporter.MAX_CONTENT_SIZE];
            while (true) {
                int byteCount = is.read(buffer);
                if (byteCount == -1) {
                    break;
                }
                fos.write(buffer, 0, byteCount);
            }
            fos.flush();
            closeStream(is);
            closeStream(fos);
        } catch (FileNotFoundException e) {
            Log.w(TAG, "copyCyclePatternFromAssets catch ");
        } catch (IOException ioe) {
            Log.w(TAG, "copyCyclePatternFromAssets catch " + ioe);
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
                Log.w(TAG, "closeStream error.");
            }
        }
    }

    public void schedThreadToRtg(int tid, boolean enable) {
        if (this.mAwareService == null) {
            this.mAwareService = ServiceManagerEx.getService("hwsysresmanager");
        }
        if (this.mAwareService != null) {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            try {
                data.writeInterfaceToken("android.rms.IHwSysResManager");
                data.writeInt(tid);
                data.writeInt(enable ? 1 : 0);
                this.mAwareService.transact(EVENT_SCHED_THREAD_RT, data, reply, 0);
                reply.readException();
            } catch (RemoteException e) {
                Log.e("TAG", "mAwareService ontransact failed");
            } catch (Throwable th) {
                data.recycle();
                reply.recycle();
                throw th;
            }
            data.recycle();
            reply.recycle();
        }
    }

    public void setHwPreloadStatus(long preloadStatus) {
        this.mPreloadStatus = preloadStatus;
    }

    public long getHwPreloadStatus() {
        return this.mPreloadStatus;
    }

    public void handleHwPreloadStatus(int opType) {
        if (opType == 1) {
            initPreloadedSocked();
        } else if (opType == 2) {
            acceptPreloadedApplication();
        } else if (opType == 3) {
            connectContinuePreload();
        }
    }

    private void initPreloadedSocked() {
        if (this.mPreloadStatus == 1) {
            this.mSocketName = UUID.randomUUID().toString();
            try {
                SlogEx.i(TAG, "mSocketName: " + this.mSocketName);
                this.mServerSocket = new LocalServerSocket(this.mSocketName);
            } catch (IOException e) {
                SlogEx.e(TAG, "create LocalServerSocket Error.");
            }
            this.mPreloadStatus = 2;
            SlogEx.i(TAG, "initPreloadedSocked end mPreloadStatus " + this.mPreloadStatus);
        }
    }

    private void acceptPreloadedApplication() {
        if (this.mPreloadStatus == 2) {
            if (this.mSocketName == null || this.mServerSocket == null) {
                SlogEx.i(TAG, "accpect failed");
            } else {
                LocalSocket session = null;
                DataOutputStream out = null;
                DataInputStream in = null;
                try {
                    SlogEx.i(TAG, "accept socket mSocketName: " + this.mSocketName);
                    StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
                    session = this.mServerSocket.accept();
                    SlogEx.i(TAG, "accept socket end");
                    out = new DataOutputStream(session.getOutputStream());
                    in = new DataInputStream(session.getInputStream());
                    out.writeInt(1);
                    in.readInt();
                } catch (IOException e) {
                    SlogEx.e(TAG, "accept Error.");
                } catch (Throwable th) {
                    closeStream(in);
                    closeStream(out);
                    closeStream(session);
                    closeStream(this.mServerSocket);
                    throw th;
                }
                closeStream(in);
                closeStream(out);
                closeStream(session);
                closeStream(this.mServerSocket);
            }
            this.mPreloadStatus = 3;
            SlogEx.i(TAG, "acceptPreloadedApplication end mPreloadStatus " + this.mPreloadStatus);
        }
    }

    private void connectContinuePreload() {
        if (this.mPreloadStatus == 2) {
            if (this.mSocketName != null) {
                LocalSocket client = null;
                DataInputStream in = null;
                DataOutputStream out = null;
                try {
                    client = new LocalSocket();
                    SlogEx.i(TAG, "before connect app.socketName:" + this.mSocketName);
                    client.connect(new LocalSocketAddress(this.mSocketName));
                    client.setSoTimeout(200);
                    in = new DataInputStream(client.getInputStream());
                    out = new DataOutputStream(client.getOutputStream());
                    in.readInt();
                    out.writeInt(1);
                } catch (IOException e) {
                    SlogEx.e(TAG, "connect Error.");
                } catch (Throwable th) {
                    closeStream(in);
                    closeStream(out);
                    closeStream(client);
                    throw th;
                }
                closeStream(in);
                closeStream(out);
                closeStream(client);
            }
            this.mPreloadStatus = 3;
            SlogEx.i(TAG, "connectContinuePreload end mPreloadStatus " + this.mPreloadStatus);
        }
    }
}
