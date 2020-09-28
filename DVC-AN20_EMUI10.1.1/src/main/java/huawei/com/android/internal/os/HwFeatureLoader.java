package huawei.com.android.internal.os;

import android.common.HwFrameworkFactory;
import android.os.SystemClock;
import android.os.Trace;
import android.system.ErrnoException;
import android.system.Os;
import android.util.Log;
import com.huawei.sidetouch.TpCommandConstant;
import com.huawei.uikit.effect.BuildConfig;
import dalvik.system.BaseDexClassLoader;
import dalvik.system.VMRuntime;
import huawei.cust.HwCfgFilePolicy;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import libcore.io.IoUtils;

public class HwFeatureLoader implements HwFrameworkFactory.IHwFeatureLoader {
    private static final String FEATURE_PRELOADED_CLASSES = "/feature/preloaded-classes";
    private static final String PATH_FEATURE = "/feature/dexpaths";
    private static final int ROOT_GID = 0;
    private static final int ROOT_UID = 0;
    private static final String TAG = "HwFeatureLoader";
    private static final int UNPRIVILEGED_GID = 9999;
    private static final int UNPRIVILEGED_UID = 9999;

    public void addDexPaths() {
        BaseDexClassLoader systemload = (BaseDexClassLoader) ClassLoader.getSystemClassLoader();
        if (systemload == null) {
            Log.e(TAG, "SystemClassLoader is null!");
            return;
        }
        File pathFile = HwCfgFilePolicy.getCfgFile(PATH_FEATURE, 0);
        if (pathFile == null) {
            Log.d(TAG, "get pathFile :/feature/dexpaths failed!");
            return;
        }
        BufferedReader br = null;
        try {
            InputStream is = new FileInputStream(pathFile);
            try {
                BufferedReader br2 = new BufferedReader(new InputStreamReader(is, "UTF-8"), 256);
                while (true) {
                    String line = br2.readLine();
                    if (line != null) {
                        String line2 = line.trim();
                        if (!line2.startsWith(TpCommandConstant.SEPARATE)) {
                            if (!line2.equals(BuildConfig.FLAVOR)) {
                                Log.i(TAG, "addDexPath: " + line2);
                                systemload.addDexPath(line2);
                            }
                        }
                    } else {
                        IoUtils.closeQuietly(is);
                        try {
                            br2.close();
                            return;
                        } catch (IOException e) {
                            Log.e(TAG, "Error in close BufferedReader /feature/dexpaths.", e);
                            return;
                        }
                    }
                }
            } catch (IOException e2) {
                Log.e(TAG, "Error reading /feature/dexpaths.", e2);
                IoUtils.closeQuietly(is);
                if (0 != 0) {
                    br.close();
                }
            } catch (Throwable th) {
                IoUtils.closeQuietly(is);
                if (0 != 0) {
                    try {
                        br.close();
                    } catch (IOException e3) {
                        Log.e(TAG, "Error in close BufferedReader /feature/dexpaths.", e3);
                    }
                }
                throw th;
            }
        } catch (FileNotFoundException e4) {
            Log.e(TAG, "Couldn't find /feature/dexpaths.");
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:100:0x01cf  */
    /* JADX WARNING: Removed duplicated region for block: B:110:0x01e6 A[SYNTHETIC, Splitter:B:110:0x01e6] */
    /* JADX WARNING: Removed duplicated region for block: B:116:0x01f7  */
    /* JADX WARNING: Removed duplicated region for block: B:131:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:94:0x01be A[SYNTHETIC, Splitter:B:94:0x01be] */
    public void preloadClasses() {
        Throwable th;
        int regid;
        int reuid;
        BaseDexClassLoader systemload = (BaseDexClassLoader) ClassLoader.getSystemClassLoader();
        if (systemload == null) {
            Log.e(TAG, "SystemClassLoader is null!");
            return;
        }
        VMRuntime runtime = VMRuntime.getRuntime();
        File classesFile = HwCfgFilePolicy.getCfgFile(FEATURE_PRELOADED_CLASSES, 0);
        if (classesFile == null) {
            Log.d(TAG, "get classesFile :/feature/preloaded-classes failed!");
            return;
        }
        BufferedReader br = null;
        try {
            InputStream is = new FileInputStream(classesFile);
            Log.i(TAG, "Preloading classes...");
            long startTime = SystemClock.uptimeMillis();
            int reuid2 = Os.getuid();
            int regid2 = Os.getgid();
            boolean droppedPriviliges = false;
            if (reuid2 == 0 && regid2 == 0) {
                try {
                    Os.setregid(0, 9999);
                    Os.setreuid(0, 9999);
                    droppedPriviliges = true;
                } catch (ErrnoException ex) {
                    throw new RuntimeException("Failed to drop root", ex);
                }
            }
            float defaultUtilization = runtime.getTargetHeapUtilization();
            runtime.setTargetHeapUtilization(0.8f);
            try {
                try {
                    br = new BufferedReader(new InputStreamReader(is, "UTF-8"), 256);
                    int count = 0;
                    while (true) {
                        String line = br.readLine();
                        if (line == null) {
                            break;
                        }
                        String line2 = line.trim();
                        if (line2.startsWith(TpCommandConstant.SEPARATE)) {
                            reuid = reuid2;
                            regid = regid2;
                        } else if (line2.equals(BuildConfig.FLAVOR)) {
                            reuid = reuid2;
                            regid = regid2;
                        } else {
                            StringBuilder sb = new StringBuilder();
                            try {
                                sb.append("PreloadClass ");
                                sb.append(line2);
                                try {
                                    Trace.traceBegin(16384, sb.toString());
                                    try {
                                        Class.forName(line2, true, systemload);
                                        count++;
                                    } catch (ClassNotFoundException e) {
                                        Log.w(TAG, "Class not found for preloading: " + line2);
                                    } catch (UnsatisfiedLinkError e2) {
                                        Log.w(TAG, "Problem preloading " + line2 + ": " + e2);
                                    } catch (Throwable t) {
                                        Log.e(TAG, "Error preloading " + line2 + ".", t);
                                        if (t instanceof Error) {
                                            throw ((Error) t);
                                        } else if (t instanceof RuntimeException) {
                                            throw ((RuntimeException) t);
                                        } else {
                                            throw new RuntimeException(t);
                                        }
                                    }
                                    Trace.traceEnd(16384);
                                    reuid2 = reuid2;
                                    regid2 = regid2;
                                } catch (IOException e3) {
                                    e = e3;
                                    try {
                                        Log.e(TAG, "Error reading /feature/preloaded-classes.", e);
                                        IoUtils.closeQuietly(is);
                                        if (br != null) {
                                        }
                                        runtime.setTargetHeapUtilization(defaultUtilization);
                                        if (!droppedPriviliges) {
                                        }
                                    } catch (Throwable th2) {
                                        th = th2;
                                        IoUtils.closeQuietly(is);
                                        if (br != null) {
                                        }
                                        runtime.setTargetHeapUtilization(defaultUtilization);
                                        if (droppedPriviliges) {
                                        }
                                        throw th;
                                    }
                                }
                            } catch (IOException e4) {
                                e = e4;
                                Log.e(TAG, "Error reading /feature/preloaded-classes.", e);
                                IoUtils.closeQuietly(is);
                                if (br != null) {
                                }
                                runtime.setTargetHeapUtilization(defaultUtilization);
                                if (!droppedPriviliges) {
                                }
                            } catch (Throwable th3) {
                                th = th3;
                                IoUtils.closeQuietly(is);
                                if (br != null) {
                                }
                                runtime.setTargetHeapUtilization(defaultUtilization);
                                if (droppedPriviliges) {
                                }
                                throw th;
                            }
                        }
                        reuid2 = reuid;
                        regid2 = regid;
                    }
                    Log.i(TAG, "...preloaded " + count + " classes in " + (SystemClock.uptimeMillis() - startTime) + "ms.");
                    IoUtils.closeQuietly(is);
                    try {
                        br.close();
                    } catch (IOException e5) {
                        Log.e(TAG, "Error in close BufferedReader /feature/preloaded-classes.", e5);
                    }
                    runtime.setTargetHeapUtilization(defaultUtilization);
                    if (droppedPriviliges) {
                        try {
                            Os.setreuid(0, 0);
                            Os.setregid(0, 0);
                        } catch (ErrnoException ex2) {
                            throw new RuntimeException("Failed to restore root", ex2);
                        }
                    }
                } catch (IOException e6) {
                    e = e6;
                    Log.e(TAG, "Error reading /feature/preloaded-classes.", e);
                    IoUtils.closeQuietly(is);
                    if (br != null) {
                    }
                    runtime.setTargetHeapUtilization(defaultUtilization);
                    if (!droppedPriviliges) {
                    }
                } catch (Throwable th4) {
                    th = th4;
                    IoUtils.closeQuietly(is);
                    if (br != null) {
                    }
                    runtime.setTargetHeapUtilization(defaultUtilization);
                    if (droppedPriviliges) {
                    }
                    throw th;
                }
            } catch (IOException e7) {
                e = e7;
                Log.e(TAG, "Error reading /feature/preloaded-classes.", e);
                IoUtils.closeQuietly(is);
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e8) {
                        Log.e(TAG, "Error in close BufferedReader /feature/preloaded-classes.", e8);
                    }
                }
                runtime.setTargetHeapUtilization(defaultUtilization);
                if (!droppedPriviliges) {
                    try {
                        Os.setreuid(0, 0);
                        Os.setregid(0, 0);
                    } catch (ErrnoException ex3) {
                        throw new RuntimeException("Failed to restore root", ex3);
                    }
                }
            } catch (Throwable th5) {
                th = th5;
                IoUtils.closeQuietly(is);
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e9) {
                        Log.e(TAG, "Error in close BufferedReader /feature/preloaded-classes.", e9);
                    }
                }
                runtime.setTargetHeapUtilization(defaultUtilization);
                if (droppedPriviliges) {
                    try {
                        Os.setreuid(0, 0);
                        Os.setregid(0, 0);
                    } catch (ErrnoException ex4) {
                        throw new RuntimeException("Failed to restore root", ex4);
                    }
                }
                throw th;
            }
        } catch (FileNotFoundException e10) {
            Log.e(TAG, "Couldn't find /feature/preloaded-classes.");
        }
    }
}
