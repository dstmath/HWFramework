package android.os;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;
import dalvik.system.VMRuntime;
import java.io.File;

public final class GraphicsEnvironment {
    private static final boolean DEBUG = false;
    private static final String PROPERTY_GFX_DRIVER = "ro.gfx.driver.0";
    private static final String TAG = "GraphicsEnvironment";

    private static native void setDriverPath(String str);

    public static void setupGraphicsEnvironment(Context context) {
        chooseDriver(context);
        new Thread(new -$Lambda$6x30vPJhBKUfNY8tswxuZo3DCe0(), "EGL Init").start();
    }

    private static void chooseDriver(Context context) {
        String driverPackageName = SystemProperties.get(PROPERTY_GFX_DRIVER);
        if (driverPackageName != null && !driverPackageName.isEmpty()) {
            ApplicationInfo ai = context.getApplicationInfo();
            if (!ai.isPrivilegedApp() && (!ai.isSystemApp() || (ai.isUpdatedSystemApp() ^ 1) == 0)) {
                try {
                    ApplicationInfo driverInfo = context.getPackageManager().getApplicationInfo(driverPackageName, 1048576);
                    String abi = chooseAbi(driverInfo);
                    if (abi != null) {
                        if (driverInfo.targetSdkVersion < 26) {
                            Log.w(TAG, "updated driver package is not known to be compatible with O");
                            return;
                        }
                        StringBuilder sb = new StringBuilder();
                        sb.append(driverInfo.nativeLibraryDir).append(File.pathSeparator);
                        sb.append(driverInfo.sourceDir).append("!/lib/").append(abi);
                        setDriverPath(sb.toString());
                    }
                } catch (NameNotFoundException e) {
                    Log.w(TAG, "driver package '" + driverPackageName + "' not installed");
                }
            }
        }
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
