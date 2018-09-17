package android.app;

import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.IProcessObserver.Stub;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.Process;
import android.os.RemoteException;
import android.rms.iaware.DataContract.Apps.LaunchMode;
import android.util.SparseArray;
import java.util.List;

public class AppImportanceMonitor {
    static final int MSG_UPDATE = 1;
    final SparseArray<AppEntry> mApps;
    final Context mContext;
    final Handler mHandler;
    final IProcessObserver mProcessObserver;

    /* renamed from: android.app.AppImportanceMonitor.2 */
    class AnonymousClass2 extends Handler {
        AnonymousClass2(Looper $anonymous0) {
            super($anonymous0);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case AppImportanceMonitor.MSG_UPDATE /*1*/:
                    AppImportanceMonitor.this.onImportanceChanged(msg.arg1, msg.arg2 & PowerManager.WAKE_LOCK_LEVEL_MASK, msg.arg2 >> 16);
                default:
                    super.handleMessage(msg);
            }
        }
    }

    static class AppEntry {
        int importance;
        final SparseArray<Integer> procs;
        final int uid;

        AppEntry(int _uid) {
            this.procs = new SparseArray(AppImportanceMonitor.MSG_UPDATE);
            this.importance = Process.SYSTEM_UID;
            this.uid = _uid;
        }
    }

    public AppImportanceMonitor(Context context, Looper looper) {
        this.mApps = new SparseArray();
        this.mProcessObserver = new Stub() {
            public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) {
            }

            public void onProcessStateChanged(int pid, int uid, int procState) {
                synchronized (AppImportanceMonitor.this.mApps) {
                    AppImportanceMonitor.this.updateImportanceLocked(pid, uid, RunningAppProcessInfo.procStateToImportance(procState), true);
                }
            }

            public void onProcessDied(int pid, int uid) {
                synchronized (AppImportanceMonitor.this.mApps) {
                    AppImportanceMonitor.this.updateImportanceLocked(pid, uid, Process.SYSTEM_UID, true);
                }
            }
        };
        this.mContext = context;
        this.mHandler = new AnonymousClass2(looper);
        ActivityManager am = (ActivityManager) context.getSystemService(LaunchMode.ACTIVITY);
        try {
            ActivityManagerNative.getDefault().registerProcessObserver(this.mProcessObserver);
        } catch (RemoteException e) {
        }
        List<RunningAppProcessInfo> apps = am.getRunningAppProcesses();
        if (apps != null) {
            for (int i = 0; i < apps.size(); i += MSG_UPDATE) {
                RunningAppProcessInfo app = (RunningAppProcessInfo) apps.get(i);
                updateImportanceLocked(app.uid, app.pid, app.importance, false);
            }
        }
    }

    public int getImportance(int uid) {
        AppEntry ent = (AppEntry) this.mApps.get(uid);
        if (ent == null) {
            return Process.SYSTEM_UID;
        }
        return ent.importance;
    }

    public void onImportanceChanged(int uid, int importance, int oldImportance) {
    }

    void updateImportanceLocked(int uid, int pid, int importance, boolean repChange) {
        AppEntry ent = (AppEntry) this.mApps.get(uid);
        if (ent == null) {
            ent = new AppEntry(uid);
            this.mApps.put(uid, ent);
        }
        if (importance >= Process.SYSTEM_UID) {
            ent.procs.remove(pid);
        } else {
            ent.procs.put(pid, Integer.valueOf(importance));
        }
        updateImportanceLocked(ent, repChange);
    }

    void updateImportanceLocked(AppEntry ent, boolean repChange) {
        int appImp = Process.SYSTEM_UID;
        for (int i = 0; i < ent.procs.size(); i += MSG_UPDATE) {
            int procImp = ((Integer) ent.procs.valueAt(i)).intValue();
            if (procImp < appImp) {
                appImp = procImp;
            }
        }
        if (appImp != ent.importance) {
            int impCode = appImp | (ent.importance << 16);
            ent.importance = appImp;
            if (appImp >= Process.SYSTEM_UID) {
                this.mApps.remove(ent.uid);
            }
            if (repChange) {
                this.mHandler.obtainMessage(MSG_UPDATE, ent.uid, impCode).sendToTarget();
            }
        }
    }
}
