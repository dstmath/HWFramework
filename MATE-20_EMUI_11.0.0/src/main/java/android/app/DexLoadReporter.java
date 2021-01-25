package android.app;

import android.os.FileUtils;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.provider.SettingsStringUtil;
import android.util.Slog;
import com.android.internal.annotations.GuardedBy;
import dalvik.system.BaseDexClassLoader;
import dalvik.system.VMRuntime;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/* access modifiers changed from: package-private */
public class DexLoadReporter implements BaseDexClassLoader.Reporter {
    private static final boolean DEBUG = false;
    private static final DexLoadReporter INSTANCE = new DexLoadReporter();
    private static final String TAG = "DexLoadReporter";
    @GuardedBy({"mDataDirs"})
    private final Set<String> mDataDirs = new HashSet();

    private DexLoadReporter() {
    }

    static DexLoadReporter getInstance() {
        return INSTANCE;
    }

    /* access modifiers changed from: package-private */
    public void registerAppDataDir(String packageName, String dataDir) {
        if (dataDir != null) {
            synchronized (this.mDataDirs) {
                this.mDataDirs.add(dataDir);
            }
        }
    }

    public void report(List<ClassLoader> classLoadersChain, List<String> classPaths) {
        if (classLoadersChain.size() != classPaths.size()) {
            Slog.wtf(TAG, "Bad call to DexLoadReporter: argument size mismatch");
        } else if (classPaths.isEmpty()) {
            Slog.wtf(TAG, "Bad call to DexLoadReporter: empty dex paths");
        } else {
            String[] dexPathsForRegistration = classPaths.get(0).split(File.pathSeparator);
            if (dexPathsForRegistration.length != 0) {
                notifyPackageManager(classLoadersChain, classPaths);
                registerSecondaryDexForProfiling(dexPathsForRegistration);
            }
        }
    }

    private void notifyPackageManager(List<ClassLoader> classLoadersChain, List<String> classPaths) {
        List<String> classLoadersNames = new ArrayList<>(classPaths.size());
        for (ClassLoader classLoader : classLoadersChain) {
            classLoadersNames.add(classLoader.getClass().getName());
        }
        String packageName = ActivityThread.currentPackageName();
        try {
            ActivityThread.getPackageManager().notifyDexLoad(packageName, classLoadersNames, classPaths, VMRuntime.getRuntime().vmInstructionSet());
        } catch (RemoteException re) {
            Slog.e(TAG, "Failed to notify PM about dex load for package " + packageName, re);
        }
    }

    private void registerSecondaryDexForProfiling(String[] dexPaths) {
        String[] dataDirs;
        if (SystemProperties.getBoolean("dalvik.vm.dexopt.secondary", false)) {
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
            File dexPathFile = new File(dexPath);
            File secondaryProfileDir = new File(dexPathFile.getParent(), "oat");
            File secondaryProfile = new File(secondaryProfileDir, dexPathFile.getName() + ".cur.prof");
            if (secondaryProfileDir.exists() || secondaryProfileDir.mkdir()) {
                try {
                    secondaryProfile.createNewFile();
                    VMRuntime.registerAppInfo(secondaryProfile.getPath(), new String[]{dexPath});
                } catch (IOException ex) {
                    Slog.e(TAG, "Failed to create profile for secondary dex " + dexPath + SettingsStringUtil.DELIMITER + ex.getMessage());
                }
            } else {
                Slog.e(TAG, "Could not create the profile directory: " + secondaryProfile);
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
}
