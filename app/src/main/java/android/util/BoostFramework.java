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
    private static Method mAcquireFunc;
    private static Method mAcquireTouchFunc;
    private static Constructor<Class> mConstructor;
    private static Method mIOPStart;
    private static Method mIOPStop;
    private static boolean mIsLoaded;
    private static Method mReleaseFunc;
    private Object mPerf;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.util.BoostFramework.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.util.BoostFramework.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.util.BoostFramework.<clinit>():void");
    }

    private boolean checkExistinSys(String path) {
        File fileSys = new File(Environment.getRootDirectory().getPath() + "/framework", path);
        Log.v(TAG, fileSys.getAbsolutePath() + " be checked ! ");
        if (fileSys.exists()) {
            return true;
        }
        return false;
    }

    public BoostFramework() {
        this.mPerf = null;
        if (!mIsLoaded) {
            try {
                PathClassLoader perfClassLoader;
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
        int ret = -1;
        try {
            ret = ((Integer) mAcquireFunc.invoke(this.mPerf, new Object[]{Integer.valueOf(duration), list})).intValue();
        } catch (Exception e) {
            Log.e(TAG, "Exception " + e);
        }
        return ret;
    }

    public int perfLockRelease() {
        int ret = -1;
        try {
            ret = ((Integer) mReleaseFunc.invoke(this.mPerf, new Object[0])).intValue();
        } catch (Exception e) {
            Log.e(TAG, "Exception " + e);
        }
        return ret;
    }

    public int perfLockAcquireTouch(MotionEvent ev, DisplayMetrics metrics, int duration, int... list) {
        int ret = -1;
        try {
            ret = ((Integer) mAcquireTouchFunc.invoke(this.mPerf, new Object[]{ev, metrics, Integer.valueOf(duration), list})).intValue();
        } catch (Exception e) {
            Log.e(TAG, "Exception " + e);
        }
        return ret;
    }

    public int perfIOPrefetchStart(int pid, String pkg_name) {
        int ret = -1;
        try {
            ret = ((Integer) mIOPStart.invoke(this.mPerf, new Object[]{Integer.valueOf(pid), pkg_name})).intValue();
        } catch (Exception e) {
            Log.e(TAG, "Exception " + e);
        }
        return ret;
    }

    public int perfIOPrefetchStop() {
        int ret = -1;
        try {
            ret = ((Integer) mIOPStop.invoke(this.mPerf, new Object[0])).intValue();
        } catch (Exception e) {
            Log.e(TAG, "Exception " + e);
        }
        return ret;
    }
}
