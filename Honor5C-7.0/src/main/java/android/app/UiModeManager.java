package android.app;

import android.app.IUiModeManager.Stub;
import android.content.Context;
import android.os.RemoteException;
import android.os.ServiceManager;

public class UiModeManager {
    public static String ACTION_ENTER_CAR_MODE = null;
    public static String ACTION_ENTER_DESK_MODE = null;
    public static String ACTION_EXIT_CAR_MODE = null;
    public static String ACTION_EXIT_DESK_MODE = null;
    public static final int DISABLE_CAR_MODE_GO_HOME = 1;
    public static final int ENABLE_CAR_MODE_ALLOW_SLEEP = 2;
    public static final int ENABLE_CAR_MODE_GO_CAR_HOME = 1;
    public static final int MODE_NIGHT_AUTO = 0;
    public static final int MODE_NIGHT_NO = 1;
    public static final int MODE_NIGHT_YES = 2;
    private static final String TAG = "UiModeManager";
    private IUiModeManager mService;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.app.UiModeManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.app.UiModeManager.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.app.UiModeManager.<clinit>():void");
    }

    UiModeManager() {
        this.mService = Stub.asInterface(ServiceManager.getService(Context.UI_MODE_SERVICE));
    }

    public void enableCarMode(int flags) {
        if (this.mService != null) {
            try {
                this.mService.enableCarMode(flags);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void disableCarMode(int flags) {
        if (this.mService != null) {
            try {
                this.mService.disableCarMode(flags);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public int getCurrentModeType() {
        if (this.mService == null) {
            return MODE_NIGHT_NO;
        }
        try {
            return this.mService.getCurrentModeType();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setNightMode(int mode) {
        if (this.mService != null) {
            try {
                this.mService.setNightMode(mode);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public int getNightMode() {
        if (this.mService == null) {
            return -1;
        }
        try {
            return this.mService.getNightMode();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isUiModeLocked() {
        if (this.mService == null) {
            return true;
        }
        try {
            return this.mService.isUiModeLocked();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isNightModeLocked() {
        if (this.mService == null) {
            return true;
        }
        try {
            return this.mService.isNightModeLocked();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }
}
