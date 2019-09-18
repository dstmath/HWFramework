package android.os;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.provider.SettingsStringUtil;
import android.util.Log;
import dalvik.system.VMRuntime;
import java.io.File;

public class GraphicsEnvironment {
    private static final boolean DEBUG = false;
    private static final String PROPERTY_GFX_DRIVER = "ro.gfx.driver.0";
    private static final String TAG = "GraphicsEnvironment";
    private static final GraphicsEnvironment sInstance = new GraphicsEnvironment();
    private ClassLoader mClassLoader;
    private String mDebugLayerPath;
    private String mLayerPath;

    private static native void setDebugLayers(String str);

    private static native void setDriverPath(String str);

    private static native void setLayerPaths(ClassLoader classLoader, String str);

    public static GraphicsEnvironment getInstance() {
        return sInstance;
    }

    public void setup(Context context) {
        setupGpuLayers(context);
        chooseDriver(context);
    }

    private static boolean isDebuggable(Context context) {
        return (context.getApplicationInfo().flags & 2) > 0;
    }

    public void setLayerPaths(ClassLoader classLoader, String layerPath, String debugLayerPath) {
        this.mClassLoader = classLoader;
        this.mLayerPath = layerPath;
        this.mDebugLayerPath = debugLayerPath;
    }

    private void setupGpuLayers(Context context) {
        String layerPaths = "";
        if (isDebuggable(context) && Settings.Global.getInt(context.getContentResolver(), Settings.Global.ENABLE_GPU_DEBUG_LAYERS, 0) != 0) {
            String gpuDebugApp = Settings.Global.getString(context.getContentResolver(), Settings.Global.GPU_DEBUG_APP);
            String packageName = context.getPackageName();
            if (gpuDebugApp != null && packageName != null && !gpuDebugApp.isEmpty() && !packageName.isEmpty() && gpuDebugApp.equals(packageName)) {
                Log.i(TAG, "GPU debug layers enabled for " + packageName);
                layerPaths = this.mDebugLayerPath + SettingsStringUtil.DELIMITER;
                String layers = Settings.Global.getString(context.getContentResolver(), Settings.Global.GPU_DEBUG_LAYERS);
                Log.i(TAG, "Debug layer list: " + layers);
                if (layers != null && !layers.isEmpty()) {
                    setDebugLayers(layers);
                }
            }
        }
        setLayerPaths(this.mClassLoader, layerPaths + this.mLayerPath);
    }

    private static void chooseDriver(Context context) {
        String driverPackageName = SystemProperties.get(PROPERTY_GFX_DRIVER);
        if (driverPackageName != null && !driverPackageName.isEmpty()) {
            ApplicationInfo ai = context.getApplicationInfo();
            if (!ai.isPrivilegedApp() && (!ai.isSystemApp() || ai.isUpdatedSystemApp())) {
                try {
                    ApplicationInfo driverInfo = context.getPackageManager().getApplicationInfo(driverPackageName, 1048576);
                    String abi = chooseAbi(driverInfo);
                    if (abi != null) {
                        if (driverInfo.targetSdkVersion < 26) {
                            Log.w(TAG, "updated driver package is not known to be compatible with O");
                            return;
                        }
                        setDriverPath(driverInfo.nativeLibraryDir + File.pathSeparator + driverInfo.sourceDir + "!/lib/" + abi);
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    Log.w(TAG, "driver package '" + driverPackageName + "' not installed");
                }
            }
        }
    }

    public static void earlyInitEGL() {
        new Thread($$Lambda$GraphicsEnvironment$U4RqBlx5Js3171IFOgvpvoAFg.INSTANCE, "EGL Init").start();
    }

    private static String chooseAbi(ApplicationInfo ai) {
        String isa = VMRuntime.getCurrentInstructionSet();
        if (ai.primaryCpuAbi != null && isa.equals(VMRuntime.getInstructionSet(ai.primaryCpuAbi))) {
            return ai.primaryCpuAbi;
        }
        if (ai.secondaryCpuAbi == null || !isa.equals(VMRuntime.getInstructionSet(ai.secondaryCpuAbi))) {
            return null;
        }
        return ai.secondaryCpuAbi;
    }
}
