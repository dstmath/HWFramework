package com.huawei.pgmng.api;

import android.content.Context;
import android.os.ServiceManager;
import android.util.Log;
import com.huawei.pgmng.api.IPGManager.Stub;
import java.util.ArrayList;
import java.util.List;

public class PGManager {
    private static final String TAG = "PGManager";
    private static PGManager sInstance;
    private Context mContext;
    private IPGManager mService;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.pgmng.api.PGManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.pgmng.api.PGManager.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.pgmng.api.PGManager.<clinit>():void");
    }

    private PGManager(Context context) {
        this.mContext = context;
        this.mService = Stub.asInterface(ServiceManager.getService("pgservice"));
    }

    public static PGManager getInstance(Context context) {
        PGManager pGManager;
        synchronized (PGManager.class) {
            if (sInstance == null) {
                sInstance = new PGManager(context);
            }
            pGManager = sInstance;
        }
        return pGManager;
    }

    public long proxyBroadcast(List<String> pkgs, boolean proxy) {
        try {
            return this.mService.proxyBroadcast(pkgs, proxy);
        } catch (Exception e) {
            Log.w(TAG, "proxy broadcast failed", e);
            return -1;
        }
    }

    public long proxyBroadcastByPid(List<Integer> pids, boolean proxy) {
        List spids = new ArrayList();
        if (pids != null) {
            for (Integer pid : pids) {
                spids.add(pid.toString());
            }
        } else {
            spids = null;
        }
        try {
            return this.mService.proxyBroadcastByPid(spids, proxy);
        } catch (Exception e) {
            Log.w(TAG, "proxy broadcast by pid failed", e);
            return -1;
        }
    }

    public void setProxyBCActions(List<String> actions) {
        try {
            this.mService.setProxyBCActions(actions);
        } catch (Exception e) {
            Log.w(TAG, "set proxy broadcast actions", e);
        }
    }

    public void setActionExcludePkg(String action, String pkg) {
        try {
            this.mService.setActionExcludePkg(action, pkg);
        } catch (Exception e) {
            Log.w(TAG, "set action exclude pkg", e);
        }
    }

    public void proxyBCConfig(int type, String key, List<String> value) {
        try {
            this.mService.proxyBCConfig(type, key, value);
        } catch (Exception e) {
            Log.w(TAG, "config proxy broadcast", e);
        }
    }

    public void proxyWakeLockByPidUid(int pid, int uid, boolean proxy) {
        try {
            this.mService.proxyWakeLockByPidUid(pid, uid, proxy);
        } catch (Exception e) {
            Log.w(TAG, "proxyWakeLockByPidUid Exception: ", e);
        }
    }

    public void forceReleaseWakeLockByPidUid(int pid, int uid) {
        try {
            this.mService.forceReleaseWakeLockByPidUid(pid, uid);
        } catch (Exception e) {
            Log.w(TAG, "forceReleaseWakeLockByPidUid Exception: ", e);
        }
    }

    public void forceRestoreWakeLockByPidUid(int pid, int uid) {
        try {
            this.mService.forceRestoreWakeLockByPidUid(pid, uid);
        } catch (Exception e) {
            Log.w(TAG, "forceRestoreWakeLockByPidUid Exception: ", e);
        }
    }

    public boolean getWakeLockByUid(int uid, int wakeflag) {
        boolean flag = false;
        try {
            if (this.mService != null) {
                flag = this.mService.getWakeLockByUid(uid, wakeflag);
            }
        } catch (Exception ex) {
            Log.w(TAG, "getWakeLockByUid Exception: ", ex);
        }
        return flag;
    }

    public void setLcdRatio(int ratio, boolean autoAdjust) {
        try {
            if (this.mService != null) {
                this.mService.setLcdRatio(ratio, autoAdjust);
            }
        } catch (Exception ex) {
            Log.w(TAG, "setLcdRatio Exception: ", ex);
        }
    }

    public boolean proxyApp(String pkg, int uid, boolean proxy) {
        try {
            return this.mService.proxyApp(pkg, uid, proxy);
        } catch (Exception e) {
            Log.w(TAG, "proxy app failed", e);
            return false;
        }
    }

    public void configBrightnessRange(int ratioMin, int ratioMax, int autoLimit) {
        try {
            if (this.mService != null) {
                this.mService.configBrightnessRange(ratioMin, ratioMax, autoLimit);
            }
        } catch (Exception ex) {
            Log.w(TAG, "configBrightnessRange Exception: ", ex);
        }
    }
}
