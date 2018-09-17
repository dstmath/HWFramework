package android.app.mtm;

import android.app.mtm.IMultiTaskManagerService.Stub;
import android.app.mtm.iaware.RSceneData;
import android.os.Bundle;
import android.os.FreezeScreenScene;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.rms.iaware.RPolicyData;
import android.util.Log;
import android.util.Slog;
import com.huawei.hsm.permission.StubController;

public class MultiTaskManager {
    static final boolean DEBUG = false;
    static final String TAG = "MultiTaskManager";
    private static MultiTaskManager instance;
    static final Object mLock = null;
    private IMultiTaskManagerService mService;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.app.mtm.MultiTaskManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.app.mtm.MultiTaskManager.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.app.mtm.MultiTaskManager.<clinit>():void");
    }

    private MultiTaskManager() {
        IMultiTaskManagerService service = getService();
        if (service == null) {
            Slog.e(TAG, "multi task service is null in constructor");
        }
        this.mService = service;
    }

    public static MultiTaskManager getInstance() {
        synchronized (mLock) {
            if (SystemProperties.getBoolean("persist.sys.enable_iaware", DEBUG)) {
                if (instance == null) {
                    if (Log.HWINFO) {
                        Slog.i(TAG, "first time to initialize MultiTaskManager, this log should not appear again!");
                    }
                    instance = new MultiTaskManager();
                    if (instance.mService == null) {
                        instance = null;
                    }
                }
                MultiTaskManager multiTaskManager = instance;
                return multiTaskManager;
            }
            Slog.e(TAG, "multitask service is not running because prop is false, so getInstance return null");
            return null;
        }
    }

    public MultiTaskPolicy getMultiTaskPolicy(int resourcetype, String resourceextend, int resourcestatus, Bundle args) {
        try {
            if (this.mService == null) {
                this.mService = getService();
            }
            if (this.mService != null) {
                return this.mService.getMultiTaskPolicy(resourcetype, resourceextend, resourcestatus, args);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "can not connect to MultiTaskManagerService");
        }
        return null;
    }

    public void notifyResourceStatusOverload(int resourcetype, String resourceextend, int resourcestatus, Bundle args) {
        try {
            if (this.mService == null) {
                this.mService = getService();
            }
            if (this.mService != null) {
                this.mService.notifyResourceStatusOverload(resourcetype, resourceextend, resourcestatus, args);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "can not connect to MultiTaskManagerService");
        }
    }

    public int getMultiTaskProcessGroup(int schedGroup, int pid, int uid, int clientpid, int clientuid, String adjType) {
        String TAG_POLICYVALUE = "policyvalue23";
        Bundle args = new Bundle();
        args.putInt(FreezeScreenScene.PID_PARAM, pid);
        args.putInt(StubController.TABLE_COLUM_UID, uid);
        args.putInt("clientpid", clientpid);
        args.putInt("clientuid", clientuid);
        MultiTaskPolicy mpolicy = getMultiTaskPolicy(23, adjType, schedGroup, args);
        if (mpolicy == null) {
            Slog.e(TAG, "get null policy in getMultiTaskProcessGroup");
            return schedGroup;
        } else if (mpolicy.getPolicy() == StubController.PERMISSION_ACCESS_WIFI) {
            return mpolicy.getPolicyData().getInt(TAG_POLICYVALUE, schedGroup);
        } else {
            return schedGroup;
        }
    }

    public void registerObserver(IMultiTaskProcessObserver observer) {
        try {
            if (this.mService != null) {
                this.mService.registerObserver(observer);
                return;
            }
            this.mService = getService();
            if (this.mService != null) {
                this.mService.registerObserver(observer);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "can not connect to MultiTaskManagerService");
        }
    }

    public void unregisterObserver(IMultiTaskProcessObserver observer) {
        try {
            if (this.mService != null) {
                this.mService.unregisterObserver(observer);
                return;
            }
            this.mService = getService();
            if (this.mService != null) {
                this.mService.unregisterObserver(observer);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "can not connect to MultiTaskManagerService");
        }
    }

    public void notifyProcessGroupChange(int pid, int uid) {
        try {
            if (this.mService == null) {
                this.mService = getService();
            }
            if (this.mService != null) {
                this.mService.notifyProcessGroupChange(pid, uid);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "can not connect to MultiTaskManagerService");
        }
    }

    public boolean killProcess(int pid, boolean restartservice) {
        try {
            if (this.mService == null) {
                this.mService = getService();
            }
            if (this.mService != null) {
                return this.mService.killProcess(pid, restartservice);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "can not connect to MultiTaskManagerService");
        }
        return DEBUG;
    }

    public boolean forcestopApps(int pid) {
        try {
            if (this.mService == null) {
                this.mService = getService();
            }
            if (this.mService != null) {
                return this.mService.forcestopApps(pid);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "can not connect to MultiTaskManagerService");
        }
        return DEBUG;
    }

    public boolean reportScene(int featureId, RSceneData scene) {
        try {
            if (this.mService == null) {
                this.mService = getService();
            }
            if (this.mService != null) {
                return this.mService.reportScene(featureId, scene);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "reportScene can not connect to MultiTaskManagerService");
        }
        return DEBUG;
    }

    public RPolicyData acquirePolicyData(int featureId, RSceneData scene) {
        try {
            if (this.mService == null) {
                this.mService = getService();
            }
            if (this.mService != null) {
                return this.mService.acquirePolicyData(featureId, scene);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "acquirePolicyData can not connect to MultiTaskManagerService");
        }
        return null;
    }

    private IMultiTaskManagerService getService() {
        return Stub.asInterface(ServiceManager.getService("multi_task"));
    }
}
