package android.app;

import android.content.ComponentName;
import android.content.pm.ActivityInfo;
import huawei.cust.HwCustUtils;

public class HwCustNonHardwareAcceleratedPackagesManager {
    private static HwCustNonHardwareAcceleratedPackagesManager sInstance;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.app.HwCustNonHardwareAcceleratedPackagesManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.app.HwCustNonHardwareAcceleratedPackagesManager.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.app.HwCustNonHardwareAcceleratedPackagesManager.<clinit>():void");
    }

    public static synchronized HwCustNonHardwareAcceleratedPackagesManager getDefault() {
        HwCustNonHardwareAcceleratedPackagesManager hwCustNonHardwareAcceleratedPackagesManager;
        synchronized (HwCustNonHardwareAcceleratedPackagesManager.class) {
            if (sInstance == null) {
                sInstance = (HwCustNonHardwareAcceleratedPackagesManager) HwCustUtils.createObj(HwCustNonHardwareAcceleratedPackagesManager.class, new Object[0]);
            }
            hwCustNonHardwareAcceleratedPackagesManager = sInstance;
        }
        return hwCustNonHardwareAcceleratedPackagesManager;
    }

    public boolean shouldForceEnabled(ActivityInfo ai, ComponentName instrumentationClass) {
        return false;
    }

    public void handlePackageAdded(String pkgName, boolean updated) {
    }

    public void handlePackageRemoved(String pkgName, boolean removed) {
    }

    public void setForceEnabled(String pkgName, boolean force) {
    }

    public boolean getForceEnabled(String pkgName) {
        return false;
    }

    public boolean hasPackage(String pkgName) {
        return false;
    }

    public void removePackage(String pkgName) {
    }
}
