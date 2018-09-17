package huawei.com.android.internal.os;

import android.common.HwFrameworkFactory.IHwFeatureLoader;
import android.os.SystemClock;
import android.os.Trace;
import android.system.ErrnoException;
import android.system.Os;
import android.util.Log;
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

public class HwFeatureLoader implements IHwFeatureLoader {
    private static final String FEATURE_PRELOADED_CLASSES = "/feature/preloaded-classes";
    private static final String PATH_FEATURE = "/feature/dexpaths";
    private static final int ROOT_GID = 0;
    private static final int ROOT_UID = 0;
    private static final String TAG = "HwFeatureLoader";
    private static final int UNPRIVILEGED_GID = 9999;
    private static final int UNPRIVILEGED_UID = 9999;

    /* JADX WARNING: Removed duplicated region for block: B:27:0x0082 A:{SYNTHETIC, Splitter: B:27:0x0082} */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x00b1 A:{SYNTHETIC, Splitter: B:43:0x00b1} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void addDexPaths() {
        IOException e;
        Throwable th;
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
            try {
                BufferedReader br2 = new BufferedReader(new InputStreamReader(new FileInputStream(pathFile), "UTF-8"), 256);
                while (true) {
                    try {
                        String line = br2.readLine();
                        if (line == null) {
                            break;
                        }
                        line = line.trim();
                        if (!(line.startsWith("#") || line.equals(""))) {
                            Log.i(TAG, "addDexPath: " + line);
                            systemload.addDexPath(line);
                        }
                    } catch (IOException e2) {
                        e = e2;
                        br = br2;
                        try {
                            Log.e(TAG, "Error reading /feature/dexpaths.", e);
                            if (br != null) {
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            if (br != null) {
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        br = br2;
                        if (br != null) {
                            try {
                                br.close();
                            } catch (IOException e3) {
                                Log.e(TAG, "Error in close BufferedReader /feature/dexpaths.", e3);
                            }
                        }
                        throw th;
                    }
                }
                if (br2 != null) {
                    try {
                        br2.close();
                    } catch (IOException e32) {
                        Log.e(TAG, "Error in close BufferedReader /feature/dexpaths.", e32);
                    }
                }
            } catch (IOException e4) {
                e32 = e4;
                Log.e(TAG, "Error reading /feature/dexpaths.", e32);
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e322) {
                        Log.e(TAG, "Error in close BufferedReader /feature/dexpaths.", e322);
                    }
                }
            }
        } catch (FileNotFoundException e5) {
            Log.e(TAG, "Couldn't find /feature/dexpaths.");
        }
    }

    public void preloadClasses() {
        IOException e;
        Throwable th;
        ClassLoader systemload = (BaseDexClassLoader) ClassLoader.getSystemClassLoader();
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
        BufferedReader bufferedReader = null;
        try {
            InputStream is = new FileInputStream(classesFile);
            Log.i(TAG, "Preloading classes...");
            long startTime = SystemClock.uptimeMillis();
            int reuid = Os.getuid();
            int regid = Os.getgid();
            boolean droppedPriviliges = false;
            if (reuid == 0 && regid == 0) {
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
                BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"), 256);
                int count = 0;
                while (true) {
                    String line;
                    try {
                        line = br.readLine();
                        if (line != null) {
                            line = line.trim();
                            if (!(line.startsWith("#") || line.equals(""))) {
                                Trace.traceBegin(16384, "PreloadClass " + line);
                                Class.forName(line, true, systemload);
                                count++;
                                Trace.traceEnd(16384);
                            }
                        } else {
                            Log.i(TAG, "...preloaded " + count + " classes in " + (SystemClock.uptimeMillis() - startTime) + "ms.");
                            if (br != null) {
                                try {
                                    br.close();
                                } catch (IOException e2) {
                                    Log.e(TAG, "Error in close BufferedReader /feature/preloaded-classes.", e2);
                                }
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
                        }
                    } catch (ClassNotFoundException e3) {
                        Log.w(TAG, "Class not found for preloading: " + line);
                    } catch (UnsatisfiedLinkError e4) {
                        Log.w(TAG, "Problem preloading " + line + ": " + e4);
                    } catch (IOException e5) {
                        e2 = e5;
                        bufferedReader = br;
                    } catch (Throwable th2) {
                        th = th2;
                        bufferedReader = br;
                    }
                }
            } catch (IOException e6) {
                e2 = e6;
                try {
                    Log.e(TAG, "Error reading /feature/preloaded-classes.", e2);
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e22) {
                            Log.e(TAG, "Error in close BufferedReader /feature/preloaded-classes.", e22);
                        }
                    }
                    runtime.setTargetHeapUtilization(defaultUtilization);
                    if (droppedPriviliges) {
                        try {
                            Os.setreuid(0, 0);
                            Os.setregid(0, 0);
                        } catch (ErrnoException ex22) {
                            throw new RuntimeException("Failed to restore root", ex22);
                        }
                    }
                } catch (Throwable th3) {
                    th = th3;
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e222) {
                            Log.e(TAG, "Error in close BufferedReader /feature/preloaded-classes.", e222);
                        }
                    }
                    runtime.setTargetHeapUtilization(defaultUtilization);
                    if (droppedPriviliges) {
                        try {
                            Os.setreuid(0, 0);
                            Os.setregid(0, 0);
                        } catch (ErrnoException ex222) {
                            throw new RuntimeException("Failed to restore root", ex222);
                        }
                    }
                    throw th;
                }
            }
        } catch (FileNotFoundException e7) {
            Log.e(TAG, "Couldn't find /feature/preloaded-classes.");
        }
    }
}
