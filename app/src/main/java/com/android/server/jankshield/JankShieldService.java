package com.android.server.jankshield;

import android.content.Context;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Binder;
import android.os.IJankShield.Stub;
import android.os.JankAppInfo;
import android.os.JankBdData;
import android.os.JankCheckPerfBug;
import android.os.JankCpuInfo;
import android.os.JankEventData;
import android.os.JankProductInfo;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.internal.os.ProcessCpuTracker;
import com.android.internal.os.ProcessCpuTracker.Stats;
import com.android.server.pfw.autostartup.comm.XmlConst.ControlScope;
import com.android.server.security.trustcircle.IOTController;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Locale;

public class JankShieldService extends Stub {
    public static final int MY_PID = 0;
    private static final String TAG = "JankShield";
    protected Context mContext;
    private JankEventDbUtil mJankDbUtil;
    IPackageManager pm;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.jankshield.JankShieldService.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.jankshield.JankShieldService.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.jankshield.JankShieldService.<clinit>():void");
    }

    public JankShieldService(Context context) {
        this.pm = null;
        this.mJankDbUtil = null;
        this.mContext = null;
        this.mJankDbUtil = new JankEventDbUtil(context);
        this.mContext = context;
    }

    public boolean getState(String name) throws RemoteException {
        return false;
    }

    public void insertJankEvent(JankEventData jankevent) {
        if (checkCallingPermission("android.permission.DUMP") != 0) {
            String msg = "Permission Denial: can't insertJankEvent from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid();
            Log.w(TAG, msg);
            throw new SecurityException(msg);
        } else if (this.mJankDbUtil != null && jankevent != null) {
            synchronized (this) {
                this.mJankDbUtil.insertEvent(jankevent);
            }
        }
    }

    public void insertJankBd(JankBdData jankbd) {
        if (checkCallingPermission("android.permission.DUMP") != 0) {
            String msg = "Permission Denial: can't insertJankBd from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid();
            Log.w(TAG, msg);
            throw new SecurityException(msg);
        } else if (this.mJankDbUtil != null && jankbd != null) {
            synchronized (this) {
                this.mJankDbUtil.insertBd(jankbd);
            }
        }
    }

    public JankCheckPerfBug checkPerfBug() {
        if (checkCallingPermission("android.permission.DUMP") != 0) {
            String msg = "Permission Denial: can't checkPerfBug from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid();
            Log.w(TAG, msg);
            throw new SecurityException(msg);
        }
        JankCheckPerfBug jankcheckbug = new JankCheckPerfBug();
        jankcheckbug.checkPerfBug(this.mContext);
        return jankcheckbug;
    }

    public JankAppInfo getJankAppInfo(String packageName) {
        if (checkCallingPermission("android.permission.DUMP") != 0) {
            String msg = "Permission Denial: can't getJankAppInfo from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid();
            Log.w(TAG, msg);
            throw new SecurityException(msg);
        }
        JankAppInfo jankappinfo = new JankAppInfo();
        PackageManager pm = this.mContext.getPackageManager();
        if (pm == null) {
            Log.w(TAG, "pm == null");
            return null;
        }
        try {
            PackageInfo packageInfo = pm.getPackageInfo(packageName, MY_PID);
            if (packageInfo == null) {
                return null;
            }
            jankappinfo.packageName = packageInfo.packageName;
            jankappinfo.versionCode = packageInfo.versionCode;
            jankappinfo.versionName = packageInfo.versionName;
            jankappinfo.coreApp = packageInfo.coreApp;
            jankappinfo.flags = -1;
            jankappinfo.systemApp = false;
            if (packageInfo.applicationInfo != null) {
                jankappinfo.flags = packageInfo.applicationInfo.flags;
                if ((packageInfo.applicationInfo.flags & 1) != 0) {
                    jankappinfo.systemApp = true;
                }
            }
            return jankappinfo;
        } catch (NameNotFoundException e) {
            Log.w(TAG, "1:Could not find packageinfo for: " + packageName);
            return null;
        }
    }

    public JankProductInfo getJankProductInfo() {
        if (checkCallingPermission("android.permission.DUMP") != 0) {
            String msg = "Permission Denial: can't getJankAppInfo from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid();
            Log.w(TAG, msg);
            throw new SecurityException(msg);
        }
        JankProductInfo jankproductinfo = new JankProductInfo();
        jankproductinfo.productIMEI = getIMEI().toUpperCase(Locale.US);
        return jankproductinfo;
    }

    public String getIMEI() {
        String deviceID = "000000000000000";
        if ("factory".equals(SystemProperties.get("ro.product.name", "NULL"))) {
            return !"NULL".equals(SystemProperties.get("ro.boardserialno", "NULL")) ? SystemProperties.get("ro.boardserialno", "NULL") : deviceID;
        } else {
            TelephonyManager phoneManager = (TelephonyManager) this.mContext.getSystemService("phone");
            if (phoneManager != null) {
                deviceID = phoneManager.getImei();
                if (deviceID != null) {
                    return deviceID;
                }
                deviceID = phoneManager.getDeviceId();
                if (deviceID == null) {
                    return "000000000000000";
                }
                return deviceID;
            }
            Log.e(TAG, "could not get TelephonyManager");
            return deviceID;
        }
    }

    public JankCpuInfo getJankCpuInfo(int topN) {
        if (checkCallingPermission("android.permission.DUMP") != 0) {
            String msg = "Permission Denial: can't getJankAppInfo from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid();
            Log.w(TAG, msg);
            throw new SecurityException(msg);
        }
        ProcessCpuTracker processCpuTracker = new ProcessCpuTracker(false);
        JankCpuInfo jankcpuinfo = new JankCpuInfo();
        processCpuTracker.init();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
        }
        processCpuTracker.update();
        int userTime = processCpuTracker.getLastUserTime();
        int systemTime = processCpuTracker.getLastSystemTime();
        int iowaitTime = processCpuTracker.getLastIoWaitTime();
        int irqTime = processCpuTracker.getLastIrqTime();
        int softIrqTime = processCpuTracker.getLastSoftIrqTime();
        int idleTime = processCpuTracker.getLastIdleTime();
        int totalTime = ((((userTime + systemTime) + iowaitTime) + irqTime) + softIrqTime) + idleTime;
        if (totalTime > 0) {
            jankcpuinfo.setVal(totalTime - idleTime, iowaitTime, totalTime);
            int N = processCpuTracker.countWorkingStats();
            if (topN < 0) {
                topN = N;
            }
            int i = MY_PID;
            while (i < N && i < topN) {
                Stats st = processCpuTracker.getWorkingStats(i);
                jankcpuinfo.addProcstats(st.pid, st.name, st.rel_uptime, st.rel_utime, st.rel_stime);
                i++;
            }
        }
        return jankcpuinfo;
    }

    int checkCallingPermission(String permission) {
        return checkPermission(permission, Binder.getCallingPid(), UserHandle.getAppId(Binder.getCallingUid()));
    }

    public int checkPermission(String permission, int pid, int uid) {
        if (permission == null) {
            return -1;
        }
        if (pid == MY_PID || uid == 0 || uid == IOTController.TYPE_MASTER) {
            return MY_PID;
        }
        if (UserHandle.isIsolated(uid)) {
            return -1;
        }
        try {
            if (this.pm == null) {
                this.pm = IPackageManager.Stub.asInterface(ServiceManager.getService(ControlScope.PACKAGE_ELEMENT_KEY));
            }
            if (this.pm != null) {
                return this.pm.checkUidPermission(permission, uid);
            }
            return -1;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (checkCallingPermission("android.permission.DUMP") != 0) {
            pw.println("Permission Denial: can't dump jankshield service from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
        } else {
            pw.println("JankShield is working ...");
        }
    }
}
