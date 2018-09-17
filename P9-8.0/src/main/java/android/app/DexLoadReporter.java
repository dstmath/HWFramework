package android.app;

import android.os.FileUtils;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.internal.annotations.GuardedBy;
import dalvik.system.BaseDexClassLoader.Reporter;
import dalvik.system.VMRuntime;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class DexLoadReporter implements Reporter {
    private static final boolean DEBUG = false;
    private static final DexLoadReporter INSTANCE = new DexLoadReporter();
    private static final String TAG = "DexLoadReporter";
    @GuardedBy("mDataDirs")
    private final Set<String> mDataDirs = new HashSet();

    private DexLoadReporter() {
    }

    static DexLoadReporter getInstance() {
        return INSTANCE;
    }

    void registerAppDataDir(String packageName, String dataDir) {
        if (dataDir != null) {
            synchronized (this.mDataDirs) {
                this.mDataDirs.add(dataDir);
            }
        }
    }

    public void report(List<String> dexPaths) {
        if (!dexPaths.isEmpty()) {
            notifyPackageManager(dexPaths);
            registerSecondaryDexForProfiling(dexPaths);
        }
    }

    private void notifyPackageManager(List<String> dexPaths) {
        String packageName = ActivityThread.currentPackageName();
        try {
            ActivityThread.getPackageManager().notifyDexLoad(packageName, dexPaths, VMRuntime.getRuntime().vmInstructionSet());
        } catch (RemoteException re) {
            Slog.e(TAG, "Failed to notify PM about dex load for package " + packageName, re);
        }
    }

    private void registerSecondaryDexForProfiling(List<String> dexPaths) {
        if (SystemProperties.getBoolean("dalvik.vm.dexopt.secondary", false)) {
            String[] dataDirs;
            synchronized (this.mDataDirs) {
                dataDirs = (String[]) this.mDataDirs.toArray(new String[0]);
            }
            for (String dexPath : dexPaths) {
                registerSecondaryDexForProfiling(dexPath, dataDirs);
            }
        }
    }

    private void registerSecondaryDexForProfiling(String dexPath, String[] dataDirs) {
        if (isSecondaryDexFile(dexPath, dataDirs)) {
            File secondaryProfile = getSecondaryProfileFile(dexPath);
            try {
                boolean created = secondaryProfile.createNewFile();
                VMRuntime.registerAppInfo(secondaryProfile.getPath(), new String[]{dexPath});
            } catch (IOException ex) {
                Slog.e(TAG, "Failed to create profile for secondary dex " + secondaryProfile + ":" + ex.getMessage());
            }
        }
    }

    private boolean isSecondaryDexFile(String dexPath, String[] dataDirs) {
        for (String dataDir : dataDirs) {
            if (FileUtils.contains(dataDir, dexPath)) {
                return true;
            }
        }
        return false;
    }

    private File getSecondaryProfileFile(String dexPath) {
        return new File(dexPath + ".prof");
    }
}
