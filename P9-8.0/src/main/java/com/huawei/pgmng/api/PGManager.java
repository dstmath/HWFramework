package com.huawei.pgmng.api;

import android.content.Context;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import com.huawei.pgmng.api.IPGManager.Stub;
import java.util.ArrayList;
import java.util.List;

public class PGManager {
    private static final String TAG = "PGManager";
    private static PGManager sInstance = null;
    private Context mContext;
    private IPGManager mService = Stub.asInterface(ServiceManager.getService("pgservice"));

    private PGManager(Context context) {
        this.mContext = context;
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
        try {
            if (this.mService != null) {
                return this.mService.getWakeLockByUid(uid, wakeflag);
            }
            return false;
        } catch (Exception ex) {
            Log.w(TAG, "getWakeLockByUid Exception: ", ex);
            return false;
        }
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

    public void refreshPackageWhitelist(int type, List<String> pkgList) {
        try {
            this.mService.refreshPackageWhitelist(type, pkgList);
        } catch (Exception e) {
            Log.w(TAG, "refreshGpsWhitelist failed", e);
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

    public void getWlBatteryStats(List<String> list) {
        try {
            if (this.mService != null) {
                this.mService.getWlBatteryStats(list);
            }
        } catch (Exception ex) {
            Log.w(TAG, "getWlBatteryStats Exception: ", ex);
        }
    }

    public boolean setPgConfig(int type, int subType, List<String> value) {
        try {
            return this.mService.setPgConfig(type, subType, value);
        } catch (Exception e) {
            Log.w(TAG, "pg config failed", e);
            return false;
        }
    }

    public boolean closeSocketsForUid(int uid) {
        try {
            if (this.mService != null) {
                return this.mService.closeSocketsForUid(uid);
            }
        } catch (Exception ex) {
            Log.w(TAG, "closeSocketsForUid Exception: ", ex);
        }
        return false;
    }

    public void killProc(int pid) {
        try {
            if (this.mService != null) {
                this.mService.killProc(pid);
            }
        } catch (RemoteException ex) {
            Log.w(TAG, "killProc RemoteException: ", ex);
        }
    }
}
