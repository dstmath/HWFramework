package com.android.server.emcom;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.emcom.EmailInfo;
import android.emcom.IEmcomManager.Stub;
import android.emcom.SmartcareInfos.BrowserInfo;
import android.emcom.SmartcareInfos.HttpInfo;
import android.emcom.SmartcareInfos.TcpStatusInfo;
import android.emcom.VideoInfo;
import android.emcom.XEngineAppInfo;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.PowerManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.server.SystemService;
import com.android.server.emcom.DevicesManager.HighSpeedDevicesManager;
import com.android.server.emcom.daemon.DaemonCommand;
import com.android.server.emcom.daemon.DaemonCommand.DaemonReportCallback;
import com.android.server.emcom.grabservice.AutoGrabService;
import com.android.server.emcom.grabservice.NotificationListener;
import com.android.server.emcom.policy.HicomPolicyManager;
import com.android.server.emcom.util.EMCOMConstants;
import com.android.server.emcom.xengine.XEngineMpipControl;
import com.android.server.emcom.xengine.XEngineProcessor;
import com.huawei.pgmng.IPGPlugCallbacks;
import com.huawei.pgmng.PGAction;
import com.huawei.pgmng.PGPlug;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;

public class EmcomManagerService extends SystemService implements EMCOMConstants, DaemonReportCallback {
    private static final int MAX_PROP_KEY_LENGTH = 31;
    static final String TAG = "EmcomManagerService";
    private final Context mContext;
    private DaemonCommand mDaemonCommand;
    private EmcomManagerReceiver mEmcomManagerReceiver;
    private Handler mHandler;
    private HicomPolicyManager mHicomPolicyManager = null;
    private HighSpeedDevicesManager mHighSpeedDevicesManager = null;
    private PGPlug mPGPlug;
    private ParaManager mParaManager = null;
    private PgEventProcesser mPgEventProcesser = new PgEventProcesser(this, null);
    private int mScreenStatus = -1;
    private ArrayList<String> mSmartcareAppList = new ArrayList();
    private XEngineMpipControl mXEngineMpipControl;
    private XEngineProcessor mXEngineProcessor;

    private final class BinderService extends Stub {
        /* synthetic */ BinderService(EmcomManagerService this$0, BinderService -this1) {
            this();
        }

        private BinderService() {
        }

        protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            int opti = 0;
            boolean all = false;
            while (opti < args.length) {
                String opt = args[opti];
                if (opt == null || opt.length() <= 0 || opt.charAt(0) != '-') {
                    break;
                }
                opti++;
                if ("-a".equals(opt)) {
                    all = true;
                }
            }
            if (opti < args.length) {
                String cmd = args[opti];
                opti++;
                if ("smartcareProc".equals(cmd) || "i".equals(cmd)) {
                    SmartcareProc proc = SmartcareProc.getInstance();
                    if (proc != null) {
                        Log.e(EmcomManagerService.TAG, "dump error, SmartcareProc null reference");
                        proc.dumpInfo(fd, pw, args, all);
                    }
                }
            }
        }

        public void notifyEmailData(EmailInfo info) {
            EmcomManagerService.this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.SMARTCARE", null);
            Log.d(EmcomManagerService.TAG, "notifyEmailData  info = " + info);
            SmartcareProc proc = SmartcareProc.getInstance();
            if (proc == null) {
                Log.e(EmcomManagerService.TAG, "notifyEmailData error, SmartcareProc null reference");
                return;
            }
            EmailInfo emailInfo = proc.getEmailInfo();
            emailInfo.copyFrom(info);
            Log.d(EmcomManagerService.TAG, "notifyEmailData  emailInfo = " + emailInfo);
            proc.addToTask(emailInfo.appName, emailInfo);
        }

        public void notifyVideoData(VideoInfo info) {
            EmcomManagerService.this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.SMARTCARE", null);
            Log.d(EmcomManagerService.TAG, "notifyVideoData  info = " + info);
            SmartcareProc proc = SmartcareProc.getInstance();
            if (proc == null) {
                Log.e(EmcomManagerService.TAG, "notifyVideoData error, SmartcareProc null reference");
                return;
            }
            VideoInfo videoInfo = proc.getVideoInfo();
            videoInfo.copyFrom(info);
            Log.d(EmcomManagerService.TAG, "notifyVideoData  videoInfo = " + videoInfo.toString());
            proc.addToTask(videoInfo.appName, videoInfo);
        }

        public XEngineAppInfo getAppInfo(String packageName) {
            return EmcomManagerService.this.mXEngineProcessor.getAppInfo(packageName);
        }

        public void accelerate(String packageName, int grade) {
            accelerateWithMainCardServiceStatus(packageName, grade, 0);
        }

        public void accelerateWithMainCardServiceStatus(String packageName, int grade, int mCardStatus) {
            EmcomManagerService.this.mXEngineProcessor.accelerate(packageName, grade, mCardStatus);
        }

        public void responseForParaUpgrade(int paratype, int pathtype, int result) {
            if (EmcomManagerService.this.mParaManager == null) {
                Log.e(EmcomManagerService.TAG, "mParaManager is null");
            } else {
                EmcomManagerService.this.mParaManager.responseForParaUpgrade(paratype, pathtype, result);
            }
        }

        public void updateAppExperienceStatus(int uid, int experience, int rtt) {
            Log.i(EmcomManagerService.TAG, "updateAppExperienceStatus: uid: " + uid + " experience: " + experience + " rtt: " + rtt);
            EmcomManagerService.this.mHicomPolicyManager.updateAppExperienceStatus(rtt);
        }

        public void notifyRunningStatus(int type, String packageName) {
            Log.d(EmcomManagerService.TAG, "notifyRunningStatus the type is " + type + " and the PackageName is " + packageName);
        }
    }

    private class EmcomHandle extends Handler {
        public EmcomHandle(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    EmcomManagerService.this.doBroadcastMessage(msg);
                    break;
                case 2:
                    EmcomManagerService.this.updateEmcomConfig(true);
                    SmartcareProc proc = SmartcareProc.getInstance();
                    if (proc != null) {
                        proc.processCotaUpdate();
                        break;
                    } else {
                        Log.e(EmcomManagerService.TAG, "handleMessage error, SmartcareProc null reference");
                        return;
                    }
                case 6:
                    EmcomManagerService.this.handleTimerStopArrive(msg);
                    break;
                case 7:
                case 8:
                    Log.d(EmcomManagerService.TAG, "EmcomManagerService EmcomHandle reply  msg = " + msg.what);
                    break;
                case 9:
                    EmcomManagerService.this.handleDelayTimeArrive(msg);
                    break;
                case 33:
                    EmcomManagerService.this.handlerForePackageChange(msg.obj);
                    break;
                default:
                    Log.e(EmcomManagerService.TAG, "Unknown message what for EmcomHandler");
                    break;
            }
        }
    }

    private class EmcomManagerReceiver extends BroadcastReceiver {
        /* synthetic */ EmcomManagerReceiver(EmcomManagerService this$0, EmcomManagerReceiver -this1) {
            this();
        }

        private EmcomManagerReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                Log.e(EmcomManagerService.TAG, "Intent is null!");
                return;
            }
            String action = intent.getAction();
            Log.d(EmcomManagerService.TAG, "EmcomManagerReceiver action" + action);
            if (action == null) {
                Log.e(EmcomManagerService.TAG, "action is null!");
                return;
            }
            int actionId = 0;
            if (action.equals("android.intent.action.PACKAGE_ADDED")) {
                actionId = 1;
            } else if (action.equals("android.intent.action.PACKAGE_CHANGED")) {
                actionId = 2;
            } else if (action.equals("android.intent.action.PACKAGE_REMOVED")) {
                actionId = 3;
            } else if (action.equals("android.intent.action.TIME_SET")) {
                actionId = 4;
            } else if (action.equals("android.intent.action.TIMEZONE_CHANGED")) {
                actionId = 5;
            } else if (action.equals("android.intent.action.SCREEN_ON")) {
                actionId = 6;
            } else if (action.equals("android.intent.action.SCREEN_OFF")) {
                actionId = 7;
            } else if (action.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
                actionId = 9;
            } else if (action.equals("android.intent.action.ANY_DATA_STATE")) {
                actionId = 10;
            }
            if (actionId != 0) {
                Message msg = EmcomManagerService.this.mHandler.obtainMessage();
                msg.what = 1;
                msg.arg1 = actionId;
                switch (actionId) {
                    case 1:
                    case 2:
                    case 3:
                        msg.obj = intent.getDataString().substring(8);
                        break;
                    case 9:
                    case 10:
                        msg.obj = intent;
                        break;
                }
                EmcomManagerService.this.mHandler.sendMessage(msg);
            }
        }
    }

    private class PgEventProcesser implements IPGPlugCallbacks {
        /* synthetic */ PgEventProcesser(EmcomManagerService this$0, PgEventProcesser -this1) {
            this();
        }

        private PgEventProcesser() {
        }

        public void onDaemonConnected() {
        }

        public void onConnectedTimeout() {
        }

        public boolean onEvent(int actionID, String msg) {
            if (PGAction.checkActionType(actionID) == 1 && PGAction.checkActionFlag(actionID) == 3) {
                Message message = EmcomManagerService.this.mHandler.obtainMessage();
                message.what = 33;
                message.obj = msg;
                EmcomManagerService.this.mHandler.sendMessage(message);
            }
            return true;
        }
    }

    public EmcomManagerService(Context context) {
        super(context);
        this.mContext = context;
        this.mParaManager = ParaManager.getInstance();
        this.mHighSpeedDevicesManager = HighSpeedDevicesManager.getInstance();
    }

    public void onStart() {
        Log.d(TAG, "EmcomManagerService onstart");
        initBroadcastReceiver();
        this.mHandler = new EmcomHandle(EmcomThread.getInstanceLooper());
        this.mDaemonCommand = DaemonCommand.getInstance();
        this.mDaemonCommand.registerDaemonCallback(this);
        this.mXEngineProcessor = new XEngineProcessor(this.mContext, this.mHandler);
        this.mContext.startService(new Intent(this.mContext, AutoGrabService.class));
        this.mContext.startService(new Intent(this.mContext, NotificationListener.class));
        publishBinderService("EmcomManager", new BinderService(this, null));
        initParaManager(this.mContext);
        initHighSpeedDevicesManager();
        initHicomPolicyManager();
        initNetworkStatusReceiver();
        this.mXEngineMpipControl = XEngineMpipControl.getInstance(this.mContext);
    }

    private void initHicomPolicyManager() {
        this.mHicomPolicyManager = HicomPolicyManager.getInstance();
        this.mHicomPolicyManager.init(this.mContext, this.mHandler);
    }

    private void initParaManager(Context context) {
        this.mParaManager = ParaManager.make(context, this.mHandler);
    }

    private void initHighSpeedDevicesManager() {
        if (this.mHighSpeedDevicesManager == null) {
            Log.e(TAG, "mHighSpeedDevicesManager is null!");
        } else {
            this.mHighSpeedDevicesManager.startObserving();
        }
    }

    public void onBootPhase(int phase) {
        Log.d(TAG, "EmcomManagerService phase = " + phase);
        if (phase == 1000) {
            handlerBootComplete();
            updateEmcomConfig(false);
            initPgPlugThread();
            if (SmartcareConfigSerializer.getInstance().smartcareSwitchOn()) {
                SmartcareProc.make(this.mContext, ((TelephonyManager) this.mContext.getSystemService("phone")).getPhoneCount());
            }
            if (this.mHighSpeedDevicesManager != null && ((PowerManager) this.mContext.getSystemService("power")).isScreenOn()) {
                Log.d(TAG, "HighSpeedDevicesObserver checkScreen is on");
                this.mHighSpeedDevicesManager.reportScreenState(true);
            }
        }
    }

    private void handleTimerStopArrive(Message msg) {
        this.mXEngineProcessor.handleTimerStopArrive(msg);
    }

    private void handleDelayTimeArrive(Message msg) {
        if (msg != null) {
            int fromWhich = msg.arg2;
            int actionId = msg.arg1;
            if (fromWhich == 0) {
                this.mXEngineProcessor.handleScreenStatusChanged(actionId);
            } else if (fromWhich == 1) {
                this.mHicomPolicyManager.handleScreenStatusChange(actionId);
            } else {
                Log.e(TAG, "wrong screen time delay msg");
            }
        }
    }

    private void initBroadcastReceiver() {
        this.mEmcomManagerReceiver = new EmcomManagerReceiver(this, null);
        initPackageReceiver();
        initGenericReceiver();
        initScreenStatusReceiver();
    }

    public void updateEmcomConfig(boolean needRsp) {
        this.mXEngineProcessor.updateEmcomConfig(needRsp);
    }

    private void initGenericReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.TIME_SET");
        filter.addAction("android.intent.action.TIMEZONE_CHANGED");
        this.mContext.registerReceiver(this.mEmcomManagerReceiver, filter);
    }

    private void initPackageReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.PACKAGE_ADDED");
        filter.addAction("android.intent.action.PACKAGE_REMOVED");
        filter.addAction("android.intent.action.PACKAGE_CHANGED");
        filter.addDataScheme("package");
        this.mContext.registerReceiver(this.mEmcomManagerReceiver, filter);
    }

    private void initScreenStatusReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SCREEN_ON");
        filter.addAction("android.intent.action.SCREEN_OFF");
        this.mContext.registerReceiver(this.mEmcomManagerReceiver, filter);
    }

    private void initNetworkStatusReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        filter.addAction("android.intent.action.ANY_DATA_STATE");
        this.mContext.registerReceiver(this.mEmcomManagerReceiver, filter);
    }

    private void doBroadcastMessage(Message msg) {
        int actionId = msg.arg1;
        Log.d(TAG, "doBroadcastMessage  action id: " + actionId);
        switch (actionId) {
            case 1:
            case 2:
            case 3:
                handlerPackageChanged(actionId, msg.obj);
                return;
            case 4:
            case 5:
                Log.d(TAG, "Time changed, reset all Timer");
                resetAllTimer();
                return;
            case 6:
            case 7:
                handlerScreenStatus(actionId);
                return;
            case 9:
                Log.d(TAG, "Receive ACTION_WIFI_NETWORK_STATE_CHANGED ");
                this.mHicomPolicyManager.handleNetworkStatus();
                NetworkInfo networkInfo = (NetworkInfo) msg.obj.getParcelableExtra("networkInfo");
                if (networkInfo != null) {
                    this.mXEngineMpipControl.handleVpnStatus(networkInfo);
                    return;
                }
                return;
            case 10:
                Log.d(TAG, "Receive ACTION_ANY_DATA_CONNECTION_STATE_CHANGED ");
                Intent intent = msg.obj;
                this.mXEngineMpipControl.onDataStateChanged(intent.getStringExtra("apnType"), intent.getStringExtra("state"), intent.getStringExtra("reason"), intent.getIntExtra("subscription", -1));
                return;
            default:
                Log.e(TAG, "Unknown action id: " + actionId);
                return;
        }
    }

    private void resetAllTimer() {
        this.mXEngineProcessor.resetAllTimer();
    }

    private void handlerBootComplete() {
        Log.d(TAG, "handlerBootComplete");
        if (this.mDaemonCommand != null) {
            this.mDaemonCommand.exeBootComplete(this.mHandler.obtainMessage(3));
        }
    }

    private void handlerPackageChanged(int type, String packageName) {
        Log.d(TAG, "handlerPackageChanged  type = " + type + " packageName = " + packageName);
        this.mXEngineProcessor.handlerPackageChanged(type, packageName);
        if (this.mSmartcareAppList.contains(packageName)) {
            Message m = this.mHandler.obtainMessage(5);
            if (this.mDaemonCommand != null) {
                this.mDaemonCommand.exePackageChanged(type, packageName, m);
            }
        }
        sendToGrabService(10, packageName);
    }

    private void handlerScreenStatus(int actionId) {
        boolean z = true;
        Log.d(TAG, "handlerScreenStatus  actionId = " + actionId);
        if (6 == actionId) {
            this.mScreenStatus = 1;
        } else if (7 == actionId) {
            this.mScreenStatus = 2;
        } else {
            this.mScreenStatus = -1;
        }
        if (this.mParaManager != null) {
            this.mParaManager.updateScreenStatus(this.mScreenStatus);
            if (this.mParaManager.needtoCheckBasicImsNVUpgrade()) {
                this.mParaManager.checkIfReadytoUpgradePara(1, 1);
            }
            if (this.mParaManager.needtoCheckCarrierConfigUpgrade()) {
                this.mParaManager.checkIfReadytoUpgradePara(512, 1);
            }
        }
        if (this.mHighSpeedDevicesManager != null) {
            HighSpeedDevicesManager highSpeedDevicesManager = this.mHighSpeedDevicesManager;
            if (this.mScreenStatus != 1) {
                z = false;
            }
            highSpeedDevicesManager.reportScreenState(z);
        }
        this.mXEngineProcessor.delayToHandleScreenStatusChanged(actionId);
        this.mHicomPolicyManager.delayToHandleScreenStatusChanged(actionId);
    }

    private void initPgPlugThread() {
        this.mPGPlug = new PGPlug(this.mPgEventProcesser, TAG);
        new Thread(this.mPGPlug, TAG).start();
    }

    private void handlerForePackageChange(String msg) {
        if (TextUtils.isEmpty(msg)) {
            Log.e(TAG, "Foreground package change message is empty.");
            return;
        }
        String[] splits = msg.split("\t");
        Object pkgName = null;
        if (splits.length > 0) {
            pkgName = splits[0];
            Log.d(TAG, "forePackage name is " + pkgName);
        }
        if (!TextUtils.isEmpty(pkgName)) {
            this.mXEngineProcessor.handlerAppForeground(pkgName);
        }
    }

    private void sendToGrabService(int what, Object obj) {
        Handler handler = AutoGrabService.getHandler();
        if (handler == null) {
            Log.w(TAG, "AutoGrabService handler is null.");
        } else {
            Message.obtain(handler, what, obj).sendToTarget();
        }
    }

    public void onUpdateAppList(Parcel p) {
        int num = p.readInt();
        this.mSmartcareAppList.clear();
        for (int i = 0; i < num; i++) {
            String str = p.readString();
            if (!this.mSmartcareAppList.contains(str)) {
                this.mSmartcareAppList.add(str);
            }
        }
        Log.d(TAG, "  processReport EMCOM_DS_APP_LIST  num   = " + num + ",list = " + this.mSmartcareAppList);
    }

    public void onUpdateBrowserInfo(Parcel p) {
        boolean z = true;
        SmartcareProc proc = SmartcareProc.getInstance();
        if (proc == null) {
            Log.e(TAG, "onUpdateBrowserInfo error, SmartcareProc null reference");
            return;
        }
        BrowserInfo browserInfo = proc.getBrowserInfo();
        browserInfo.pageId = p.readLong();
        browserInfo.pageLatency = p.readInt();
        if (p.readByte() != (byte) 1) {
            z = false;
        }
        browserInfo.result = z;
        browserInfo.rspCode = (short) p.readInt();
        if (!browserInfo.result) {
            proc.requestLocationUpdate();
        }
        Log.d(TAG, "onUpdateBrowserInfo, pageLatency=" + browserInfo.pageLatency + ",result=" + browserInfo.result + ",pageId=" + browserInfo.pageId + ",rspCode=" + browserInfo.rspCode);
    }

    public void onUpdateHttpInfo(Parcel p) {
        SmartcareProc proc = SmartcareProc.getInstance();
        if (proc == null) {
            Log.e(TAG, "onUpdateHttpInfo error, SmartcareProc null reference");
            return;
        }
        HttpInfo httpInfo = proc.getHttpInfo();
        httpInfo.host = p.readString();
        httpInfo.startDate = ((p.readInt() * 10000) + (p.readInt() * 100)) + p.readInt();
        httpInfo.startTime = ((p.readInt() * 10000) + (p.readInt() * 100)) + p.readInt();
        httpInfo.endTime = ((p.readInt() * 10000) + (p.readInt() * 100)) + p.readInt();
        p.setDataPosition(p.dataPosition() + 16);
        httpInfo.uid = p.readInt();
        httpInfo.appName = p.readString();
        Log.d(TAG, "onUpdateHttpInfo host  = " + httpInfo.host + ",startDate=" + httpInfo.startDate + ",startTime=" + httpInfo.startTime + ",endTime=" + httpInfo.endTime + ",numStreams=" + httpInfo.numStreams + ",uid=" + httpInfo.uid + ",appName=" + httpInfo.appName);
        if (proc.getCurrentMemberBrowserInfo().result) {
            proc.addToTask(httpInfo.appName, httpInfo);
        }
    }

    public void onUpdateTcpStatusInfo(Parcel p) {
        SmartcareProc proc = SmartcareProc.getInstance();
        if (proc == null) {
            Log.e(TAG, "onUpdateTcpStatusInfo error, SmartcareProc null reference");
            return;
        }
        HttpInfo httpInfo = proc.getHttpInfo();
        TcpStatusInfo tcpStatusInfo = proc.getTcpStatusInfo();
        tcpStatusInfo.dnsDelay = p.readInt();
        tcpStatusInfo.synRtt = p.readInt();
        tcpStatusInfo.tcpUlPackages = p.readInt();
        tcpStatusInfo.tcpDlPackages = p.readInt();
        tcpStatusInfo.synRtrans = (short) p.readInt();
        tcpStatusInfo.tcpULWinZeroCount = (short) p.readInt();
        tcpStatusInfo.tcpDLWinZeroCount = (short) p.readInt();
        tcpStatusInfo.tcpUlFastRetrans = (short) p.readInt();
        tcpStatusInfo.tcpUlTimeoutRetrans = (short) p.readInt();
        tcpStatusInfo.tcpDlThreeDupAcks = (short) p.readInt();
        tcpStatusInfo.tcpDlDisorderPkts = (short) p.readInt();
        Log.d(TAG, "onUpdateTcpStatusInfo tcpStatusInfo.tcpUlPackages=" + tcpStatusInfo.tcpUlPackages + ",tcpDlPackages=" + tcpStatusInfo.tcpDlPackages + ",synRtt=" + tcpStatusInfo.synRtt + ",synRtrans=" + tcpStatusInfo.synRtrans + ",dnsDelay=" + tcpStatusInfo.dnsDelay + ",tcpDLWinZeroCount=" + tcpStatusInfo.tcpDLWinZeroCount + ",tcpUlTimeoutRetrans=" + tcpStatusInfo.tcpUlTimeoutRetrans + ",tcpULWinZeroCount=" + tcpStatusInfo.tcpULWinZeroCount + ",tcpUlFastRetrans=" + tcpStatusInfo.tcpUlFastRetrans + ",tcpDlThreeDupAcks=" + tcpStatusInfo.tcpDlThreeDupAcks + ",tcpDlDisorderPkts=" + tcpStatusInfo.tcpDlDisorderPkts);
        proc.addToTask(httpInfo.appName, tcpStatusInfo);
    }

    public void onUpdateSampleWinStat(boolean sampleWinOpen) {
        Log.d(TAG, "sampleWinOpen = " + sampleWinOpen);
        SmartcareProc proc = SmartcareProc.getInstance();
        if (proc == null) {
            Log.e(TAG, "onUpdateSampleWinStat error, SmartcareProc null reference");
            return;
        }
        proc.handleSamplesWinStat(sampleWinOpen);
        sendToGrabService(8, Boolean.valueOf(sampleWinOpen));
    }

    public void onUpdatePageId(int pageId) {
        SmartcareProc proc = SmartcareProc.getInstance();
        if (proc == null) {
            Log.e(TAG, "onUpdatePageId error, SmartcareProc null reference");
        } else {
            proc.updatePageId(pageId);
        }
    }

    public void onReportDevFail() {
        this.mXEngineMpipControl.onNotifyInterfaceFailure();
    }
}
