package huawei.com.android.internal.os;

import android.util.Log;
import com.huawei.sidetouch.TpCommandConstant;
import com.huawei.uikit.effect.BuildConfig;
import dalvik.system.PathClassLoader;
import huawei.cust.HwCfgFilePolicy;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class HwFLClassLoader extends PathClassLoader {
    public static final String TAG = "HwFLClassLoader";
    private static final String USE_FEATURE_LIST = "/feature/used-list";
    private static boolean mInitUsedList = false;
    private static List<String> mUsedFeatureList = new ArrayList();

    static {
        initUsedList();
    }

    public HwFLClassLoader(String dexPath, ClassLoader parent) {
        super(dexPath, parent);
    }

    public HwFLClassLoader(String dexPath, String librarySearchPath, ClassLoader parent) {
        super(dexPath, librarySearchPath, parent);
    }

    private static void initUsedList() {
        File pathFile = HwCfgFilePolicy.getCfgFile(USE_FEATURE_LIST, 0);
        if (pathFile == null) {
            Log.d(TAG, "get used feature list :/feature/used-list failed!");
            return;
        }
        BufferedReader br = null;
        try {
            try {
                BufferedReader br2 = new BufferedReader(new InputStreamReader(new FileInputStream(pathFile), "UTF-8"), 256);
                while (true) {
                    String line = br2.readLine();
                    if (line != null) {
                        String line2 = line.trim();
                        if (!line2.startsWith(TpCommandConstant.SEPARATE) && !line2.equals(BuildConfig.FLAVOR)) {
                            Log.v(TAG, "add package: " + line2 + " in FEATURE_USED_LIST");
                            mUsedFeatureList.add(line2);
                        }
                    } else {
                        try {
                            break;
                        } catch (IOException e) {
                            Log.e(TAG, "Error in close BufferedReader /feature/used-list.", e);
                        }
                    }
                }
                br2.close();
            } catch (IOException e2) {
                Log.e(TAG, "Error reading /feature/used-list.", e2);
                if (0 != 0) {
                    br.close();
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    try {
                        br.close();
                    } catch (IOException e3) {
                        Log.e(TAG, "Error in close BufferedReader /feature/used-list.", e3);
                    }
                }
                throw th;
            }
            mInitUsedList = true;
        } catch (FileNotFoundException e4) {
            Log.e(TAG, "Couldn't find /feature/used-list.");
        }
    }

    private static boolean isUseFeature(String dexPath) {
        if (!mInitUsedList) {
            Log.d(TAG, "USE_FEATURE_LIST had not init! ");
            return false;
        } else if (dexPath == null || dexPath.isEmpty()) {
            return false;
        } else {
            for (String feature : mUsedFeatureList) {
                if (dexPath.contains(feature)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static ClassLoader getHwFLClassLoaderParent(String dexPath) {
        if (isUseFeature(dexPath)) {
            return ClassLoader.getSystemClassLoader();
        }
        return null;
    }
}
