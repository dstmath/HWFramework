package com.android.server.zrhung;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.ZRHung;
import android.zrhung.IFaultEventService;
import android.zrhung.ZrHungData;
import android.zrhung.appeye.AppEyeBinderBlock;
import com.android.commgmt.zrhung.DefaultZrHungService;
import com.android.server.zrhung.appeye.AppEyeMessage;
import com.android.server.zrhung.appeye.AppEyeSocketThread;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.app.HwActivityManager;
import com.huawei.android.os.ServiceManagerEx;
import com.huawei.android.server.SystemServiceEx;
import com.huawei.android.util.SlogEx;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ZRHungService extends DefaultZrHungService {
    private static final int CMD_CHECK = 2;
    private static final String FAULT_NOTIFY_SERVER_NAME = "hwFaultNotifyService";
    private static final String FAULT_TYPE_ANR = "anr";
    private static final String FAULT_TYPE_CRASH = "crash";
    private static final String FAULT_TYPE_TOMBSTONE = "tombstone";
    private static final int INIT_LIST_SIZE = 16;
    private static final int MSG_ZRHUNG_INIT = 1;
    private static final int MSG_ZRHUNG_NOTIFY_ANR_APP = 4;
    private static final int MSG_ZRHUNG_NOTIFY_CRASH_APP = 5;
    private static final int MSG_ZRHUNG_NOTIFY_TOMBSTONE_APP = 6;
    private static final int MSG_ZRHUNG_SOCKET_EVENT_RECEIVE = 2;
    private static final int MSG_ZRHUNG_USER_EVENT_DELAY = 3;
    private static final String TAG = "ZRHungService";
    private static final int ZRHUNG_ANR_INTERVAL = 6000;
    private static final int ZRHUNG_USER_EVENT_TIMEOUT = 10000;
    private static IZRHungService service = null;
    private final Map<String, AppEyeAnrData> mANRMap = new HashMap((int) INIT_LIST_SIZE);
    private ActivityManager mActivityManager = null;
    private final Context mContext;
    private String[] mDirectRecoverPackages = null;
    private final Handler mHandler;
    private Handler.Callback mHandlerCallback = new Handler.Callback() {
        /* class com.android.server.zrhung.ZRHungService.AnonymousClass1 */

        @Override // android.os.Handler.Callback
        public boolean handleMessage(Message msg) {
            SlogEx.d(ZRHungService.TAG, "handleMessage :" + msg.what);
            AppEyeMessage message = null;
            if (msg.obj instanceof AppEyeMessage) {
                message = (AppEyeMessage) msg.obj;
            }
            int i = msg.what;
            if (i == 1) {
                ZRHungService.this.handleInitService();
                return true;
            } else if (i != 2) {
                if (i != ZRHungService.MSG_ZRHUNG_USER_EVENT_DELAY) {
                    if (i != ZRHungService.MSG_ZRHUNG_NOTIFY_ANR_APP || message == null) {
                        return false;
                    }
                    ZRHungService.this.startNotifyApp(message.getAppProcessName(), ZRHungService.FAULT_TYPE_ANR);
                    return true;
                } else if (message == null) {
                    return false;
                } else {
                    String packageName = message.getAppPkgName();
                    ZrHungData data = new ZrHungData();
                    data.putString("packageName", packageName);
                    ZRHungService.this.handleUserEvent(data, true);
                    return true;
                }
            } else if (message == null) {
                return false;
            } else {
                ZRHungService.this.handleAppFreezeEvent(message);
                return true;
            }
        }
    };
    private boolean mIsDirectRecoveryConfiged = false;
    private boolean mIsDirectRecoveryEnabled = false;
    private AppEyeSocketThread mSocketThread = null;

    public ZRHungService(Context context) {
        super(context);
        setInstance(this);
        this.mContext = context;
        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        this.mHandler = new Handler(handlerThread.getLooper(), this.mHandlerCallback);
        this.mActivityManager = (ActivityManager) context.getSystemService("activity");
        startFaultNotifyService(context);
        SlogEx.d(TAG, "ZRHungService on create!");
    }

    /* JADX WARN: Type inference failed for: r0v0, types: [com.android.server.zrhung.FaultNotifyService, android.os.IBinder] */
    /* JADX WARNING: Unknown variable types count: 1 */
    private void startFaultNotifyService(Context context) {
        ServiceManagerEx.addService(FAULT_NOTIFY_SERVER_NAME, (IBinder) new FaultNotifyService(this.mContext));
    }

    public static synchronized IZRHungService getInstance() {
        IZRHungService iZRHungService;
        synchronized (ZRHungService.class) {
            iZRHungService = service;
        }
        return iZRHungService;
    }

    private static void setInstance(IZRHungService zrHungService) {
        service = zrHungService;
    }

    public void onStart() {
        SlogEx.d(TAG, "onStart!");
    }

    public void onBootPhase(int phase) {
        Handler handler;
        SlogEx.d(TAG, "onBootPhase!");
        if (phase == SystemServiceEx.PHASE_BOOT_COMPLETED && (handler = this.mHandler) != null) {
            handler.sendEmptyMessage(1);
        }
    }

    public boolean sendEvent(ZrHungData zrhungData) {
        if (zrhungData == null) {
            return false;
        }
        String eventType = zrhungData.getString("eventtype");
        char c = 65535;
        switch (eventType.hashCode()) {
            case -74099055:
                if (eventType.equals("socketrecover")) {
                    c = 0;
                    break;
                }
                break;
            case 1777228417:
                if (eventType.equals("recoverresult")) {
                    c = 1;
                    break;
                }
                break;
            case 1902096952:
                if (eventType.equals("notifyapp")) {
                    c = MSG_ZRHUNG_NOTIFY_ANR_APP;
                    break;
                }
                break;
            case 1914956048:
                if (eventType.equals("showanrdialog")) {
                    c = MSG_ZRHUNG_USER_EVENT_DELAY;
                    break;
                }
                break;
            case 1985941039:
                if (eventType.equals("settime")) {
                    c = 2;
                    break;
                }
                break;
        }
        if (c == 0) {
            AppEyeMessage appEyeMessage = null;
            if (zrhungData.get("appeyemessage") instanceof AppEyeMessage) {
                appEyeMessage = (AppEyeMessage) zrhungData.get("appeyemessage");
            }
            if (appEyeMessage != null) {
                Message message = this.mHandler.obtainMessage();
                message.what = 2;
                message.obj = appEyeMessage;
                message.sendToTarget();
                SlogEx.i(TAG, "Receive a socket message from zrhung");
                return true;
            }
            SlogEx.i(TAG, "NULL message");
            return false;
        } else if (c != 1) {
            if (c == 2) {
                setAppEyeAnrDataLocked(zrhungData);
                return true;
            } else if (c == MSG_ZRHUNG_USER_EVENT_DELAY) {
                return canShowAnrDialogs(zrhungData);
            } else {
                if (c != MSG_ZRHUNG_NOTIFY_ANR_APP) {
                    return false;
                }
                return readyToNotifyApp(zrhungData);
            }
        } else if (handleUserEvent(zrhungData, false)) {
            return true;
        } else {
            SlogEx.w(TAG, "sendEvent  recoverresult error!");
            return false;
        }
    }

    private boolean readyToNotifyApp(ZrHungData args) {
        AppEyeMessage appEyeMessage = new AppEyeMessage();
        List<String> lists = new ArrayList<>((int) INIT_LIST_SIZE);
        lists.add("processName:" + args.getString("processName"));
        if (appEyeMessage.parseMsg(lists) != 0) {
            SlogEx.e(TAG, "Ready to notify app but no process name");
            return false;
        }
        Message message = this.mHandler.obtainMessage();
        message.what = getFaultType(args.getString("faulttype"));
        message.obj = appEyeMessage;
        message.sendToTarget();
        return true;
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x003e  */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x0047 A[RETURN] */
    private int getFaultType(String faultType) {
        char c;
        String newFaultType = faultType.toLowerCase(Locale.ROOT);
        int hashCode = newFaultType.hashCode();
        if (hashCode != 96741) {
            if (hashCode != 94921639) {
                if (hashCode == 1836179733 && newFaultType.equals(FAULT_TYPE_TOMBSTONE)) {
                    c = 2;
                    if (c != 0) {
                        return MSG_ZRHUNG_NOTIFY_ANR_APP;
                    }
                    if (c == 1) {
                        return MSG_ZRHUNG_NOTIFY_CRASH_APP;
                    }
                    if (c != 2) {
                        return 0;
                    }
                    return MSG_ZRHUNG_NOTIFY_TOMBSTONE_APP;
                }
            } else if (newFaultType.equals(FAULT_TYPE_CRASH)) {
                c = 1;
                if (c != 0) {
                }
            }
        } else if (newFaultType.equals(FAULT_TYPE_ANR)) {
            c = 0;
            if (c != 0) {
            }
        }
        c = 65535;
        if (c != 0) {
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startNotifyApp(String processName, String faultType) {
        try {
            IBinder faultServer = ServiceManagerEx.getService(FAULT_NOTIFY_SERVER_NAME);
            if (faultServer != null) {
                IFaultEventService.Stub.asInterface(faultServer).callBack(processName, faultType, new ArrayList<>((int) INIT_LIST_SIZE));
            }
        } catch (RemoteException e) {
            SlogEx.e(TAG, "failed to notify application");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleInitService() {
        if (this.mSocketThread == null) {
            this.mSocketThread = new AppEyeSocketThread();
            this.mSocketThread.start();
            SlogEx.d(TAG, "handleInitService!");
            return;
        }
        SlogEx.d(TAG, "socketThread already start!");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleAppFreezeEvent(AppEyeMessage message) {
        if (message == null) {
            SlogEx.e(TAG, "null message or mPath received from Server");
            return;
        }
        SlogEx.d(TAG, "received message:" + message.toString());
        String command = message.getCommand();
        char c = 65535;
        int hashCode = command.hashCode();
        if (hashCode != -1547232625) {
            if (hashCode != -1164891468) {
                if (hashCode == 1702423020 && command.equals(AppEyeMessage.KILL_MULTI_PROCESSES)) {
                    c = 2;
                }
            } else if (command.equals(AppEyeMessage.NOTIFY_USER)) {
                c = 0;
            }
        } else if (command.equals(AppEyeMessage.KILL_PROCESS)) {
            c = 1;
        }
        if (c == 0) {
            notifyRecoverEventToUser(message);
        } else if (c == 1) {
            killApplicationProcess(message.getAppPid(), message.getAppPkgName());
        } else if (c != 2) {
            SlogEx.e(TAG, "invalid command: " + command);
        } else {
            for (Integer num : message.getAppPidList()) {
                killApplicationProcess(num.intValue(), message.getAppPkgName());
            }
        }
    }

    private void notifyRecoverEventToUser(AppEyeMessage message) {
        int pid = message.getAppPid();
        int uid = message.getAppUid();
        String processName = message.getAppProcessName();
        String packageName = message.getAppPkgName();
        if (processName != null && packageName != null && !HwActivityManager.handleANRFilterFIFO(uid, 2)) {
            SlogEx.d(TAG, "notifyRecoverEventToUser -- pid:" + pid + " uid:" + uid + " processName:" + processName + " packageName:" + packageName);
            int foregroundAppPid = getForegroundPid();
            if (pid != foregroundAppPid) {
                SlogEx.d(TAG, "notifyRecoverEventToUser return due to no match foreground app foregroundAppPid:" + foregroundAppPid + " mTargetAppPid:" + pid + " mTargetAppUid:" + uid);
                return;
            }
            this.mHandler.removeMessages(MSG_ZRHUNG_USER_EVENT_DELAY);
            HwActivityManager.handleShowAppEyeAnrUi(pid, uid, processName, packageName);
            Message msg = this.mHandler.obtainMessage();
            msg.what = MSG_ZRHUNG_USER_EVENT_DELAY;
            msg.obj = message;
            this.mHandler.sendMessageDelayed(msg, 10000);
        }
    }

    private int getForegroundPid() {
        Bundle topBundle = ActivityManagerEx.getTopActivity();
        if (topBundle != null) {
            return topBundle.getInt("pid");
        }
        SlogEx.d(TAG, "can not get current top activity!");
        return 0;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean handleUserEvent(ZrHungData zrhungData, boolean isTimeout) {
        if (zrhungData == null) {
            SlogEx.e(TAG, "null args received!");
            return false;
        } else if (handleDirectRecoverPackages(zrhungData.getString("packageName"), zrhungData.getInt("pid"))) {
            return true;
        } else {
            if (isTimeout) {
                zrhungData.putString("result", "Timeout");
            } else if (!this.mHandler.hasMessages(MSG_ZRHUNG_USER_EVENT_DELAY) || zrhungData.getString("result") == null) {
                return false;
            } else {
                this.mHandler.removeMessages(MSG_ZRHUNG_USER_EVENT_DELAY);
            }
            if (!sendAppFreezeEvent(278, zrhungData, null, "user handled recover")) {
                SlogEx.e(TAG, "send App Freeze recover result error!");
            }
            return true;
        }
    }

    private boolean sendAppFreezeEvent(short wpId, ZrHungData zrhungData, String cmdBuf, String buffer) {
        StringBuilder sb = new StringBuilder();
        if (zrhungData != null) {
            try {
                String recoverresult = zrhungData.getString("result");
                if (recoverresult != null) {
                    sb.append("result = ");
                    sb.append(recoverresult);
                    sb.append(System.lineSeparator());
                }
                int uid = zrhungData.getInt("uid");
                if (uid > 0) {
                    sb.append("uid = ");
                    sb.append(Integer.toString(uid));
                    sb.append(System.lineSeparator());
                }
                int pid = zrhungData.getInt("pid");
                if (pid > 0) {
                    sb.append("pid = ");
                    sb.append(Integer.toString(pid));
                    sb.append(System.lineSeparator());
                }
                String pkgName = zrhungData.getString("packageName");
                if (pkgName != null) {
                    sb.append("packageName = ");
                    sb.append(pkgName);
                    sb.append(System.lineSeparator());
                }
                String procName = zrhungData.getString("processName");
                if (procName != null) {
                    sb.append("processName = ");
                    sb.append(procName);
                    sb.append(System.lineSeparator());
                }
            } catch (Exception e) {
                SlogEx.e(TAG, "exception info ex");
                return false;
            }
        }
        if (buffer != null) {
            sb.append(buffer);
        }
        boolean isSendSuccess = ZRHung.sendHungEvent(wpId, cmdBuf, sb.toString());
        SlogEx.d(TAG, " sendAppFreezeEvent message:" + sb.toString());
        if (isSendSuccess) {
            return true;
        }
        SlogEx.e(TAG, " sendAppFreezeEvent failed!");
        return true;
    }

    /* access modifiers changed from: package-private */
    public static final class AppEyeAnrData {
        public static final int ANR_DIALOG_REMOVED = 1;
        public static final int ANR_DIALOG_SHOWING = 0;
        private int mAnrDialogStatus = 1;
        private long mAnrTime = 0;
        private String mAnrType = null;

        AppEyeAnrData(String anrType, long currentTime) {
            this.mAnrType = anrType;
            this.mAnrTime = currentTime;
        }
    }

    private boolean canShowAnrDialogs(ZrHungData args) {
        boolean isProcessAlive = true;
        synchronized (this.mANRMap) {
            if (args != null) {
                String packageName = args.getString("packageName");
                String currentAnrType = args.getString("result");
                int pid = args.getInt("pid");
                if (handleDirectRecoverPackages(packageName, pid)) {
                    return false;
                }
                if (packageName != null) {
                    if (currentAnrType != null) {
                        if (currentAnrType.equals("original")) {
                            if (!canShowOriginalAnrDialogsLocked(packageName)) {
                                SlogEx.d(TAG, " forbid ORIGNAL ANR Dialogs");
                                isProcessAlive = false;
                            } else {
                                SlogEx.d(TAG, " show ORIGNAL ANR Dialogs");
                            }
                        } else if (!canShowAppEyeAnrDialogsLocked(packageName)) {
                            SlogEx.d(TAG, " forbid APPEYE ANR Dialogs");
                            isProcessAlive = false;
                        } else {
                            SlogEx.d(TAG, " show APPEYE ANR Dialogs");
                        }
                        if (isProcessAlive && isTargetAnrProcessStillAlive(pid)) {
                            resetAppEyeAnrDataLocked(packageName, currentAnrType, 0);
                        }
                    }
                }
                return true;
            }
            return isProcessAlive;
        }
    }

    private boolean canShowOriginalAnrDialogsLocked(String packageName) {
        AppEyeAnrData lastAnrData;
        long currentTime = SystemClock.uptimeMillis();
        if (packageName == null || (lastAnrData = this.mANRMap.get(packageName)) == null || !lastAnrData.mAnrType.equals("appeye")) {
            return true;
        }
        return updateShowAnrDialogFlagIfNeeded(lastAnrData, currentTime, "OriginalANRDialog");
    }

    private boolean canShowAppEyeAnrDialogsLocked(String packageName) {
        AppEyeAnrData lastAnrData;
        long currentTime = SystemClock.uptimeMillis();
        if (packageName == null || (lastAnrData = this.mANRMap.get(packageName)) == null) {
            return true;
        }
        return updateShowAnrDialogFlagIfNeeded(lastAnrData, currentTime, "AppEyeANRDialog");
    }

    private boolean updateShowAnrDialogFlagIfNeeded(AppEyeAnrData lastAnrData, long curTime, String dialogType) {
        boolean isShowDialog = true;
        String str = lastAnrData.mAnrType;
        int dialogStatus = lastAnrData.mAnrDialogStatus;
        long lastAnrTime = lastAnrData.mAnrTime;
        StringBuilder sb = new StringBuilder(" forbid Show ");
        sb.append(dialogType);
        if (dialogStatus == 1 && curTime - lastAnrTime < 6000) {
            sb.append(", last anr dialog removed less than 6s");
            isShowDialog = false;
        }
        if (dialogStatus == 0 && curTime - lastAnrTime < 10000) {
            sb.append(", exist showing or pending dialog");
            isShowDialog = false;
        }
        if (!isShowDialog) {
            SlogEx.d(TAG, sb.toString());
        }
        return isShowDialog;
    }

    private void resetAppEyeAnrDataLocked(String packageName, String anrType, int dialogStatus) {
        long currentTime = SystemClock.uptimeMillis();
        AppEyeAnrData lastAnrData = this.mANRMap.get(packageName);
        if (lastAnrData != null) {
            lastAnrData.mAnrTime = currentTime;
            lastAnrData.mAnrType = anrType;
            lastAnrData.mAnrDialogStatus = dialogStatus;
            return;
        }
        this.mANRMap.put(packageName, new AppEyeAnrData(anrType, currentTime));
    }

    private void setAppEyeAnrDataLocked(ZrHungData zrhungData) {
        AppEyeAnrData lastAnrData;
        synchronized (this.mANRMap) {
            String packageName = zrhungData.getString("packageName");
            if (!(packageName == null || (lastAnrData = this.mANRMap.get(packageName)) == null)) {
                lastAnrData.mAnrTime = SystemClock.uptimeMillis();
                lastAnrData.mAnrDialogStatus = 1;
            }
        }
    }

    private void killApplicationProcess(int pid, String pkgName) {
        if (pid <= 0) {
            return;
        }
        if (AppEyeBinderBlock.isNativeProcess(pid) != 0) {
            SlogEx.e(TAG, " process is native process or not exist, pid:" + pid);
            return;
        }
        try {
            if (this.mActivityManager != null) {
                ActivityManagerEx.forceStopPackageAsUser(pkgName, -2);
                SlogEx.e(TAG, "BF and NFW forceStop package: " + pkgName);
                return;
            }
            SlogEx.e(TAG, "forcestopApps preocess: mActivityManager is null");
        } catch (Exception e) {
            SlogEx.e(TAG, "kill process error");
        }
    }

    private boolean handleDirectRecoverPackages(String packageName, int pid) {
        String[] strArr;
        if (packageName == null) {
            return false;
        }
        if (!this.mIsDirectRecoveryConfiged) {
            initRecoverPackageList();
        }
        if (!this.mIsDirectRecoveryEnabled || (strArr = this.mDirectRecoverPackages) == null) {
            return false;
        }
        for (String directRecoverPackage : strArr) {
            if (packageName.equals(directRecoverPackage) && pid > 0) {
                SlogEx.e(TAG, "kill process pid:" + pid + " packageName:" + packageName + " reason:directRecover");
                Process.killProcess(pid);
                ZrHungData data = new ZrHungData();
                data.putString("packageName", packageName);
                data.putString("result", "Direct Kill");
                sendAppFreezeEvent(278, data, null, "direct recover");
                return true;
            }
        }
        return false;
    }

    private void initRecoverPackageList() {
        ZRHung.HungConfig cfg = ZRHung.getHungConfig(278);
        if (cfg == null || cfg.status != 0) {
            SlogEx.e(TAG, "initRecoverPackageList failed!");
            return;
        }
        this.mDirectRecoverPackages = cfg.value.split(",");
        String[] strArr = this.mDirectRecoverPackages;
        if (strArr == null) {
            SlogEx.e(TAG, "initRecoverPackageList failed, due to null list!");
            this.mIsDirectRecoveryConfiged = true;
        } else if (strArr.length < 2) {
            SlogEx.e(TAG, "initRecoverPackageList failed, due to err config!");
            this.mIsDirectRecoveryConfiged = true;
        } else {
            this.mIsDirectRecoveryConfiged = true;
            this.mIsDirectRecoveryEnabled = strArr[0].trim().equals("1");
        }
    }

    private boolean isTargetAnrProcessStillAlive(int pid) {
        SlogEx.d("AppEyeUiProbe", "isTargetAlive:" + pid);
        return Files.exists(Paths.get("/proc/" + pid + "/comm", new String[0]), new LinkOption[0]);
    }
}
