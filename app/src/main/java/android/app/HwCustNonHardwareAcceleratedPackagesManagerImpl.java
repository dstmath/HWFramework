package android.app;

import android.app.INonHardwareAcceleratedPackagesManager.Stub;
import android.content.ComponentName;
import android.content.HwCustContext;
import android.content.pm.ActivityInfo;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.util.Log;
import android.util.Slog;

public class HwCustNonHardwareAcceleratedPackagesManagerImpl extends HwCustNonHardwareAcceleratedPackagesManager {
    private static boolean HWDBG = false;
    private static boolean HWFLOW = false;
    private static final String TAG = "NonHardAccelPkgs";
    private INonHardwareAcceleratedPackagesManager mService;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.app.HwCustNonHardwareAcceleratedPackagesManagerImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.app.HwCustNonHardwareAcceleratedPackagesManagerImpl.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.app.HwCustNonHardwareAcceleratedPackagesManagerImpl.<clinit>():void");
    }

    public HwCustNonHardwareAcceleratedPackagesManagerImpl() {
        this.mService = Stub.asInterface(ServiceManager.getService(HwCustContext.NON_HARD_ACCEL_PKGS_SERVICE));
    }

    public boolean shouldForceEnabled(ActivityInfo ai, ComponentName instrumentationClass) {
        if (!UserHandle.isApp(ai.applicationInfo.uid) || ai.applicationInfo.targetSdkVersion < 5 || (ai.flags & 512) != 0 || instrumentationClass != null) {
            return false;
        }
        String pkgName = ai.applicationInfo.packageName;
        boolean ret = false;
        if (this.mService != null) {
            try {
                if (this.mService.hasPackage(pkgName)) {
                    ret = this.mService.getForceEnabled(pkgName);
                } else {
                    this.mService.setForceEnabled(pkgName, false);
                }
            } catch (RemoteException e) {
                Log.e(TAG, "shouldForceEnabled: RemoteException", e);
            }
        }
        if (HWDBG) {
            Slog.d(TAG, "shouldForceEnabled: " + pkgName + " " + ret);
        }
        return ret;
    }

    public void handlePackageAdded(String pkgName, boolean updated) {
        if (updated && HWDBG) {
            Slog.d(TAG, "handlePackageAdded: ignore and keep existing record upon upgrading");
        }
    }

    public void handlePackageRemoved(String pkgName, boolean removed) {
        if (removed && HWDBG) {
            Slog.d(TAG, "handlePackageRemoved: ignore and keep existing record upon removing");
        }
    }

    public void setForceEnabled(String pkgName, boolean force) {
        if (this.mService != null) {
            try {
                this.mService.setForceEnabled(pkgName, force);
            } catch (RemoteException e) {
                Log.e(TAG, "setForceEnabled: RemoteException", e);
            }
        }
    }

    public boolean getForceEnabled(String pkgName) {
        if (this.mService != null) {
            try {
                return this.mService.getForceEnabled(pkgName);
            } catch (RemoteException e) {
                Log.e(TAG, "getForceEnabled: RemoteException", e);
            }
        }
        return false;
    }

    public boolean hasPackage(String pkgName) {
        if (this.mService != null) {
            try {
                return this.mService.hasPackage(pkgName);
            } catch (RemoteException e) {
                Log.e(TAG, "hasPackage: RemoteException", e);
            }
        }
        return false;
    }

    public void removePackage(String pkgName) {
        if (this.mService != null) {
            try {
                this.mService.removePackage(pkgName);
            } catch (RemoteException e) {
                Log.e(TAG, "removePackage: RemoteException", e);
            }
        }
    }
}
