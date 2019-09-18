package android.util;

import android.os.Environment;
import android.view.MotionEvent;
import dalvik.system.PathClassLoader;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class BoostFramework {
    private static final String PERFORMANCE_CLASS = "com.qualcomm.qti.Performance";
    private static final String PERFORMANCE_JAR = "/system/framework/QPerformance.jar";
    private static final String PERFORMANCE_JAR_VENDOR = "/system/vendor/framework/QPerformance.jar";
    private static final String TAG = "BoostFramework";
    private static Method mAcquireFunc = null;
    private static Method mAcquireTouchFunc = null;
    private static Constructor<Class> mConstructor = null;
    private static Method mIOPStart = null;
    private static Method mIOPStop = null;
    private static boolean mIsLoaded = false;
    private static Method mReleaseFunc = null;
    private Object mPerf = null;

    private boolean checkExistinSys(String path) {
        File rootdir = Environment.getRootDirectory();
        File fileSys = new File(rootdir.getPath() + "/framework", path);
        Log.v(TAG, fileSys.getAbsolutePath() + " be checked ! ");
        if (fileSys.exists()) {
            return true;
        }
        return false;
    }

    public BoostFramework() {
        PathClassLoader perfClassLoader;
        if (!mIsLoaded) {
            try {
                if (checkExistinSys("QPerformance.jar")) {
                    perfClassLoader = new PathClassLoader(PERFORMANCE_JAR, ClassLoader.getSystemClassLoader());
                } else {
                    perfClassLoader = new PathClassLoader(PERFORMANCE_JAR_VENDOR, ClassLoader.getSystemClassLoader());
                }
                Class perfClass = perfClassLoader.loadClass(PERFORMANCE_CLASS);
                mConstructor = perfClass.getConstructor(new Class[0]);
                mAcquireFunc = perfClass.getDeclaredMethod("perfLockAcquire", new Class[]{Integer.TYPE, int[].class});
                Log.v(TAG, "mAcquireFunc method = " + mAcquireFunc);
                mReleaseFunc = perfClass.getDeclaredMethod("perfLockRelease", new Class[0]);
                Log.v(TAG, "mReleaseFunc method = " + mReleaseFunc);
                mAcquireTouchFunc = perfClass.getDeclaredMethod("perfLockAcquireTouch", new Class[]{MotionEvent.class, DisplayMetrics.class, Integer.TYPE, int[].class});
                Log.v(TAG, "mAcquireTouchFunc method = " + mAcquireTouchFunc);
                mIOPStart = perfClass.getDeclaredMethod("perfIOPrefetchStart", new Class[]{Integer.TYPE, String.class});
                Log.v(TAG, "mIOPStart method = " + mIOPStart);
                mIOPStop = perfClass.getDeclaredMethod("perfIOPrefetchStop", new Class[0]);
                Log.v(TAG, "mIOPStop method = " + mIOPStop);
                mIsLoaded = true;
            } catch (Exception e) {
                Log.e(TAG, "BoostFramework() : Exception_1 = " + e);
            }
        }
        try {
            if (mConstructor != null) {
                this.mPerf = mConstructor.newInstance(new Object[0]);
            }
        } catch (Exception e2) {
            Log.e(TAG, "BoostFramework() : Exception_2 = " + e2);
        }
        Log.v(TAG, "BoostFramework() : mPerf = " + this.mPerf);
    }

    public int perfLockAcquire(int duration, int... list) {
        try {
            return ((Integer) mAcquireFunc.invoke(this.mPerf, new Object[]{Integer.valueOf(duration), list})).intValue();
        } catch (Exception e) {
            Log.e(TAG, "Exception " + e);
            return -1;
        }
    }

    public int perfLockRelease() {
        try {
            return ((Integer) mReleaseFunc.invoke(this.mPerf, new Object[0])).intValue();
        } catch (Exception e) {
            Log.e(TAG, "Exception " + e);
            return -1;
        }
    }

    public int perfLockAcquireTouch(MotionEvent ev, DisplayMetrics metrics, int duration, int... list) {
        try {
            return ((Integer) mAcquireTouchFunc.invoke(this.mPerf, new Object[]{ev, metrics, Integer.valueOf(duration), list})).intValue();
        } catch (Exception e) {
            Log.e(TAG, "Exception " + e);
            return -1;
        }
    }

    public int perfIOPrefetchStart(int pid, String pkg_name) {
        try {
            return ((Integer) mIOPStart.invoke(this.mPerf, new Object[]{Integer.valueOf(pid), pkg_name})).intValue();
        } catch (Exception e) {
            Log.e(TAG, "Exception " + e);
            return -1;
        }
    }

    public int perfIOPrefetchStop() {
        try {
            return ((Integer) mIOPStop.invoke(this.mPerf, new Object[0])).intValue();
        } catch (Exception e) {
            Log.e(TAG, "Exception " + e);
            return -1;
        }
    }
}
