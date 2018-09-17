package com.android.server;

import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.IPackageManager;
import android.content.pm.IPackageManager.Stub;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings.Secure;
import android.util.Slog;
import com.android.server.pfw.autostartup.comm.XmlConst.ControlScope;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import java.util.List;

public class PrivacyModeChangeReceiver extends BroadcastReceiver {
    private static final boolean DEBUG = false;
    private static final String DESCRIPTOR = "huawei.com.android.server.IPackageManager";
    private static int MAX_NUM = 0;
    private static int MAX_PKG = 0;
    private static final String TAG = "PrivacyModeChangeReceiver";
    public static final int transaction_setEnabledVisitorSetting = 1001;
    private ActivityManager am;
    private Context mContext;
    private IPackageManager mPackageManagerService;
    private PackageManager pm;

    /* renamed from: com.android.server.PrivacyModeChangeReceiver.1 */
    class AnonymousClass1 extends Thread {
        AnonymousClass1(String $anonymous0) {
            super($anonymous0);
        }

        public void run() {
            try {
                PrivacyModeChangeReceiver.this.removeAllRecentTask();
                PrivacyModeChangeReceiver.this.transactToPackageManagerService(PrivacyModeChangeReceiver.transaction_setEnabledVisitorSetting, "setEnabledVisitorSetting", 2, 0);
            } catch (Exception e) {
                Slog.e(PrivacyModeChangeReceiver.TAG, "change to visitor  mode failure  ", e);
            }
        }
    }

    /* renamed from: com.android.server.PrivacyModeChangeReceiver.2 */
    class AnonymousClass2 extends Thread {
        AnonymousClass2(String $anonymous0) {
            super($anonymous0);
        }

        public void run() {
            try {
                PrivacyModeChangeReceiver.this.transactToPackageManagerService(PrivacyModeChangeReceiver.transaction_setEnabledVisitorSetting, "setEnabledVisitorSetting", 1, 0);
            } catch (Exception e) {
                Slog.e(PrivacyModeChangeReceiver.TAG, "change to host mode failure ", e);
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.PrivacyModeChangeReceiver.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.PrivacyModeChangeReceiver.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.PrivacyModeChangeReceiver.<clinit>():void");
    }

    public void onReceive(Context context, Intent intent) {
        this.mContext = context;
        this.pm = this.mContext.getPackageManager();
        this.am = (ActivityManager) this.mContext.getSystemService("activity");
        if (intent.getIntExtra("privacy_mode_value", 1) == 1) {
            new AnonymousClass1("privacymodechange").start();
        } else if (intent.getIntExtra("privacy_mode_value", 0) == 0) {
            new AnonymousClass2("privacymodechange").start();
        }
    }

    private void updateAppfromSetting(int state) {
        String pkgNameList = Secure.getString(this.mContext.getContentResolver(), "privacy_app_list");
        if (pkgNameList == null) {
            Slog.e(TAG, " pkgNameList = null ");
        } else if (pkgNameList.contains(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER)) {
            String[] pkgNameArray = pkgNameList.split(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
            int i = 0;
            while (i < MAX_PKG && i < pkgNameArray.length) {
                setApplicationEnabledSetting(pkgNameArray[i], state);
                i++;
            }
        } else {
            setApplicationEnabledSetting(pkgNameList, state);
        }
    }

    private void removeAllRecentTask() {
        List<RecentTaskInfo> recentTasks = this.am.getRecentTasks(MAX_NUM, 10);
        if (recentTasks != null) {
            int size = recentTasks.size();
            int i = 0;
            while (i < size && i < MAX_NUM) {
                RecentTaskInfo recentInfo = (RecentTaskInfo) recentTasks.get(i);
                Intent intent = new Intent(recentInfo.baseIntent);
                if (recentInfo.origActivity != null) {
                    intent.setComponent(recentInfo.origActivity);
                }
                if (isCurrentHomeActivity(intent.getComponent(), null)) {
                    Slog.e(TAG, " isCurrentHomeActivity");
                } else if (!intent.getComponent().getPackageName().equals(this.mContext.getPackageName())) {
                    this.am.removeTask(recentInfo.persistentId);
                }
                i++;
            }
            recentTasks.clear();
        }
    }

    private boolean isCurrentHomeActivity(ComponentName component, ActivityInfo homeInfo) {
        if (homeInfo == null) {
            homeInfo = new Intent("android.intent.action.MAIN").addCategory("android.intent.category.HOME").resolveActivityInfo(this.mContext.getPackageManager(), 0);
        }
        if (homeInfo == null || !homeInfo.packageName.equals(component.getPackageName())) {
            return DEBUG;
        }
        return homeInfo.name.equals(component.getClassName());
    }

    private void setApplicationEnabledSetting(String pkgName, int enabledStatus) {
        try {
            if (isAppExits(pkgName)) {
                this.pm.setApplicationEnabledSetting(pkgName, enabledStatus, 0);
                Slog.e(TAG, "the pkg " + pkgName + " enablestatus: " + enabledStatus);
            }
        } catch (SecurityException e) {
            e.printStackTrace();
            Slog.e(TAG, "Unable to change enabled state of package xxx SecurityException: " + pkgName + e.toString());
        } catch (Exception e2) {
            e2.printStackTrace();
            Slog.e(TAG, "Unable to change enabled state of package 1: " + pkgName + e2.toString());
        }
    }

    private boolean isAppExits(String pkgName) {
        boolean z = DEBUG;
        if (this.pm == null || pkgName == null || AppHibernateCst.INVALID_PKG.equals(pkgName)) {
            return DEBUG;
        }
        try {
            if (this.pm.getPackageInfo(pkgName, 0) != null) {
                z = true;
            }
            return z;
        } catch (NameNotFoundException e) {
            Slog.e(TAG, "The packageName " + pkgName + " is not exit: \n" + e.toString());
            return DEBUG;
        }
    }

    private IPackageManager getPackageManager() {
        if (this.mPackageManagerService == null) {
            this.mPackageManagerService = Stub.asInterface(ServiceManager.getService(ControlScope.PACKAGE_ELEMENT_KEY));
        }
        return this.mPackageManagerService;
    }

    private synchronized boolean transactToPackageManagerService(int code, String transactName, int enabledStatus, int flag) {
        boolean success;
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        success = DEBUG;
        try {
            IBinder packageManagerServiceBinder = getPackageManager().asBinder();
            if (packageManagerServiceBinder != null) {
                _data.writeInterfaceToken(DESCRIPTOR);
                _data.writeInt(enabledStatus);
                _data.writeInt(flag);
                _data.writeInt(this.mContext.getUserId());
                packageManagerServiceBinder.transact(code, _data, _reply, 0);
                _reply.readException();
                success = _reply.readInt() == 0 ? true : DEBUG;
            }
            _reply.recycle();
            _data.recycle();
        } catch (RemoteException localRemoteException) {
            localRemoteException.printStackTrace();
            _reply.recycle();
            _data.recycle();
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
        }
        return success;
    }
}
