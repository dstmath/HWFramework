package com.android.server.hidata.hicure;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.LinkProperties;
import android.net.NetworkUtils;
import android.os.Handler;
import android.os.Message;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.SettingsObserver;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.connectivity.NetworkAgentInfo;
import com.android.server.hidata.arbitration.HwArbitrationDEFS;
import com.android.server.wifipro.WifiProCommonUtils;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DnsHiCureEngine extends StateMachine {
    private static final String CHR_BROADCAST_PERMISSION = "com.huawei.android.permission.GET_CHR_DATA";
    private static final int DELAY_TIME = 1000;
    private static final int DELAY_TIME_MONITOR_AGAIN = 30000;
    private static final int DNS_FAILED_THRESHOLD = 2;
    private static final int DNS_MAX_COUNT = 4;
    public static final String DNS_MONITOR_FLAG = "hw.hicure.dns_fail_count";
    private static final String DORECOVERY_FLAG = "radio.data.stall.recovery.action";
    private static final int EVENT_DATA_CONNECTED = 102;
    private static final int EVENT_DATA_DISCONNECTED = 101;
    private static final int EVENT_DNS_HICURE = 104;
    private static final int EVENT_DNS_MONITOR = 103;
    private static final String EVENT_DNS_MONITOR_ACTION = "telephony.dataconnection.DNS_MONITOR_ACTION";
    private static final int EVENT_DNS_MONITOR_AGAIN = 110;
    private static final int EVENT_DNS_PROBE_SUCC = 112;
    private static final int EVENT_DNS_PROBE_TIMEOUT = 111;
    private static final int EVENT_DNS_PUNISH = 108;
    private static final String EVENT_DNS_PUNISH_ACTION = "telephony.dataconnection.DNS_PUNISH_ACTION";
    private static final int EVENT_DNS_SET_BACK = 107;
    private static final int EVENT_DNS_SET_COMPLETE = 106;
    private static final int EVENT_PRIVATE_DNS_SETTINGS_CHANGED = 109;
    private static final int EVENT_SET_DNS = 105;
    private static final String INTENT_DS_DNS_HICURE_RESULT = "com.android.intent.action.dns_hicure_result";
    private static final int ITERATION_TIME = 6000;
    private static final int PUNISH_TIME = 21600000;
    private static final String TAG = "DnsHiCureEngine";
    private static final int WAIT_TIME = 2000;
    private static final int WAIT_VERIFY_TIME = 9000;
    private static DnsHiCureEngine mDnsHiCureEngine = null;
    private static final String[] mDnsWhiteList = {"10.8.2.1", "10.8.2.2"};
    /* access modifiers changed from: private */
    public static final String[] mGlobalDns = {"8.8.8.8", "208.67.222.222"};
    /* access modifiers changed from: private */
    public static final String[] mGlobalDomain = {"www.google.com", "www.facebook.com"};
    /* access modifiers changed from: private */
    public static final String[] mGlobalVerifyDomain = {"www.youtube.com", "www.amazon.com"};
    /* access modifiers changed from: private */
    public static final String[] mHomeDns = {"180.76.76.76", "223.5.5.5"};
    /* access modifiers changed from: private */
    public static final String[] mHomeDomain = {"www.baidu.com", "www.youku.com"};
    /* access modifiers changed from: private */
    public static final String[] mHomeVerifyDomain = {"www.taobao.com", "www.qq.com"};
    /* access modifiers changed from: private */
    public boolean dnsSetPunishFlag = false;
    /* access modifiers changed from: private */
    public String[] domains = new String[2];
    /* access modifiers changed from: private */
    public boolean dsFlag = false;
    PhoneStateListener listenerSim0 = new PhoneStateListener(0) {
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            SignalStrength unused = DnsHiCureEngine.this.mSignalStrengthSim0 = signalStrength;
        }

        public void onServiceStateChanged(ServiceState state) {
            ServiceState unused = DnsHiCureEngine.this.mServiceState0 = state;
        }
    };
    PhoneStateListener listenerSim1 = new PhoneStateListener(1) {
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            SignalStrength unused = DnsHiCureEngine.this.mSignalStrengthSim1 = signalStrength;
        }

        public void onServiceStateChanged(ServiceState state) {
            ServiceState unused = DnsHiCureEngine.this.mServiceState1 = state;
        }
    };
    /* access modifiers changed from: private */
    public String[] mCellDnses = new String[4];
    /* access modifiers changed from: private */
    public int mCellDnsesNum = 0;
    private final ContentResolver mContentResolver;
    /* access modifiers changed from: private */
    public Context mContext;
    private State mDefaultState;
    /* access modifiers changed from: private */
    public boolean mDnsCureState = false;
    private DnsProbe mDnsProbe;
    /* access modifiers changed from: private */
    public String[] mDnses = new String[4];
    private Handler mHandler;
    /* access modifiers changed from: private */
    public State mHiCureState;
    /* access modifiers changed from: private */
    public LinkProperties mLinkProperties = new LinkProperties();
    /* access modifiers changed from: private */
    public State mMonitoredState;
    private List<InetAddress> mNetDns = new ArrayList();
    /* access modifiers changed from: private */
    public int mNetId;
    /* access modifiers changed from: private */
    public ServiceState mServiceState0 = null;
    /* access modifiers changed from: private */
    public ServiceState mServiceState1 = null;
    private final SettingsObserver mSettingsObserver;
    /* access modifiers changed from: private */
    public SignalStrength mSignalStrengthSim0 = null;
    /* access modifiers changed from: private */
    public SignalStrength mSignalStrengthSim1 = null;
    /* access modifiers changed from: private */
    public TelephonyManager mTelephonyManager;
    /* access modifiers changed from: private */
    public State mUnmonitoredState;
    /* access modifiers changed from: private */
    public String[] mVerifyDomains = new String[2];
    /* access modifiers changed from: private */
    public String[] publicDns = new String[2];
    /* access modifiers changed from: private */
    public boolean publicDnsFlag = false;
    /* access modifiers changed from: private */
    public boolean useHostname = false;

    class DefaultState extends State {
        DefaultState() {
        }

        public boolean processMessage(Message message) {
            int i = message.what;
            DnsHiCureEngine dnsHiCureEngine = DnsHiCureEngine.this;
            dnsHiCureEngine.log("DefaultState: " + message.what);
            return true;
        }
    }

    class HiCureState extends State {
        HiCureState() {
        }

        public void enter() {
            DnsHiCureEngine.this.log("HiCureState start");
            if (true == DnsHiCureEngine.this.publicDnsFlag) {
                DnsHiCureEngine.this.sendMessage(107);
            } else {
                DnsHiCureEngine.this.sendMessage(105);
            }
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case 101:
                    DnsHiCureEngine.this.log("mHiCureState processMessage EVENT_DATA_DISCONNECTED");
                    DnsHiCureEngine.this.transitionTo(DnsHiCureEngine.this.mUnmonitoredState);
                    return true;
                case 105:
                    DnsHiCureEngine.this.log("mHiCureState processMessage EVENT_SET_DNS");
                    DnsHiCureEngine.this.mDnses[0] = DnsHiCureEngine.this.publicDns[0];
                    DnsHiCureEngine.this.mDnses[1] = DnsHiCureEngine.this.mCellDnses[0];
                    DnsHiCureEngine.this.mDnses[2] = DnsHiCureEngine.this.publicDns[1];
                    DnsHiCureEngine.this.mDnses[3] = DnsHiCureEngine.this.mCellDnses[1];
                    DnsHiCureEngine.this.setDns(DnsHiCureEngine.this.mDnses, DnsHiCureEngine.this.mLinkProperties, DnsHiCureEngine.this.mNetId);
                    boolean unused = DnsHiCureEngine.this.publicDnsFlag = true;
                    DnsHiCureEngine.this.sendMessageDelayed(106, 1000);
                    return true;
                case 106:
                    DnsHiCureEngine.this.log("mHiCureState processMessage EVENT_DNS_SET_COMPLETE");
                    boolean unused2 = DnsHiCureEngine.this.mDnsCureState = true;
                    DnsHiCureEngine.this.startProbeThread(DnsHiCureEngine.this.mVerifyDomains, DnsHiCureEngine.WAIT_VERIFY_TIME, DnsHiCureEngine.this.mNetId);
                    return true;
                case 107:
                    DnsHiCureEngine.this.log("mHiCureState processMessage EVENT_DNS_SET_BACK");
                    DnsHiCureEngine.this.setDns(DnsHiCureEngine.this.mCellDnses, DnsHiCureEngine.this.mLinkProperties, DnsHiCureEngine.this.mNetId);
                    boolean unused3 = DnsHiCureEngine.this.publicDnsFlag = false;
                    boolean unused4 = DnsHiCureEngine.this.dnsSetPunishFlag = true;
                    boolean unused5 = DnsHiCureEngine.this.mDnsCureState = false;
                    DnsHiCureEngine.this.transitionTo(DnsHiCureEngine.this.mUnmonitoredState);
                    return true;
                case 109:
                    DnsHiCureEngine.this.log("mHiCureState processMessage EVENT_PRIVATE_DNS_SETTINGS_CHANGED");
                    DnsHiCureEngine.this.updataprivateDNScfg();
                    if (true != DnsHiCureEngine.this.useHostname) {
                        return true;
                    }
                    DnsHiCureEngine.this.transitionTo(DnsHiCureEngine.this.mUnmonitoredState);
                    return true;
                case 110:
                    DnsHiCureEngine.this.transitionTo(DnsHiCureEngine.this.mMonitoredState);
                    return true;
                case 111:
                    DnsHiCureEngine.this.log("mHiCureState processMessage EVENT_DNS_PROBE_TIMEOUT");
                    DnsHiCureEngine.this.removeMessages(112);
                    DnsHiCureEngine.this.notifyChrDnsHiCureResult(DnsHiCureEngine.this.mCellDnses[0], DnsHiCureEngine.this.mCellDnses[1], false);
                    DnsHiCureEngine.this.sendMessage(107);
                    return true;
                case 112:
                    DnsHiCureEngine.this.log("mHiCureState processMessage EVENT_DNS_PROBE_SUCC");
                    DnsHiCureEngine.this.removeMessages(111);
                    DnsHiCureEngine.this.notifyChrDnsHiCureResult(DnsHiCureEngine.this.mCellDnses[0], DnsHiCureEngine.this.mCellDnses[1], true);
                    DnsHiCureEngine.this.sendMessageDelayed(110, HwArbitrationDEFS.DelayTimeMillisA);
                    return true;
                default:
                    DnsHiCureEngine dnsHiCureEngine = DnsHiCureEngine.this;
                    dnsHiCureEngine.log("mHiCureState: default message.what=" + message.what);
                    return false;
            }
        }
    }

    class MonitoredState extends State {
        Intent intent = new Intent(DnsHiCureEngine.EVENT_DNS_MONITOR_ACTION).setPackage(DnsHiCureEngine.this.mContext.getPackageName());
        AlarmManager mAlarmManager = ((AlarmManager) DnsHiCureEngine.this.mContext.getSystemService("alarm"));
        private int mCurrDnsFailedCounter;
        PendingIntent mDnsAlarmIntent = PendingIntent.getBroadcast(DnsHiCureEngine.this.mContext, 0, this.intent, 134217728);
        private int mLastDnsFailedCounter;

        MonitoredState() {
        }

        public void enter() {
            DnsHiCureEngine.this.log("MonitoredState start");
            this.mLastDnsFailedCounter = getCurrentDnsFailedCounter();
            this.mAlarmManager.set(3, SystemClock.elapsedRealtime() + 6000, this.mDnsAlarmIntent);
            String[] unused = DnsHiCureEngine.this.publicDns = DnsHiCureEngine.mGlobalDns;
            String[] unused2 = DnsHiCureEngine.this.domains = DnsHiCureEngine.mGlobalDomain;
            String[] unused3 = DnsHiCureEngine.this.mVerifyDomains = DnsHiCureEngine.mGlobalVerifyDomain;
            if (DnsHiCureEngine.this.mTelephonyManager == null) {
                DnsHiCureEngine.this.log("getSimOperator: mTelephonyManager is null, return!");
                return;
            }
            if (DnsHiCureEngine.this.mTelephonyManager.getSimOperator().startsWith(WifiProCommonUtils.COUNTRY_CODE_CN)) {
                String[] unused4 = DnsHiCureEngine.this.publicDns = DnsHiCureEngine.mHomeDns;
                String[] unused5 = DnsHiCureEngine.this.domains = DnsHiCureEngine.mHomeDomain;
                String[] unused6 = DnsHiCureEngine.this.mVerifyDomains = DnsHiCureEngine.mHomeVerifyDomain;
            }
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case 101:
                    DnsHiCureEngine.this.log("mMonitoredState processMessage EVENT_DATA_DISCONNECTED");
                    DnsHiCureEngine.this.transitionTo(DnsHiCureEngine.this.mUnmonitoredState);
                    return true;
                case 103:
                    dnsMonitor();
                    return true;
                case 104:
                    DnsHiCureEngine.this.log("mMonitoredState processMessage EVENT_DNS_HICURE");
                    DnsHiCureEngine.this.transitionTo(DnsHiCureEngine.this.mHiCureState);
                    return true;
                case 109:
                    DnsHiCureEngine.this.log("mMonitoredState processMessage EVENT_PRIVATE_DNS_SETTINGS_CHANGED");
                    DnsHiCureEngine.this.updataprivateDNScfg();
                    if (!DnsHiCureEngine.this.useHostname) {
                        return true;
                    }
                    DnsHiCureEngine.this.transitionTo(DnsHiCureEngine.this.mUnmonitoredState);
                    return true;
                case 111:
                    DnsHiCureEngine.this.log("mMonitoredState processMessage EVENT_DNS_PROBE_TIMEOUT");
                    DnsHiCureEngine.this.removeMessages(112);
                    DnsHiCureEngine.this.sendMessage(104);
                    return true;
                case 112:
                    DnsHiCureEngine.this.log("mMonitoredState processMessage EVENT_DNS_PROBE_SUCC");
                    DnsHiCureEngine.this.removeMessages(111);
                    return true;
                default:
                    DnsHiCureEngine dnsHiCureEngine = DnsHiCureEngine.this;
                    dnsHiCureEngine.log("mMonitoredState: default message.what=" + message.what);
                    return false;
            }
        }

        public void exit() {
            this.mAlarmManager.cancel(this.mDnsAlarmIntent);
        }

        public void dnsMonitor() {
            this.mCurrDnsFailedCounter = getCurrentDnsFailedCounter();
            int deltaFailedDns = this.mCurrDnsFailedCounter - this.mLastDnsFailedCounter;
            this.mLastDnsFailedCounter = this.mCurrDnsFailedCounter;
            if (deltaFailedDns > 0) {
                DnsHiCureEngine dnsHiCureEngine = DnsHiCureEngine.this;
                dnsHiCureEngine.log("deltaFailedDns = " + deltaFailedDns);
            }
            if (deltaFailedDns < 2 || !DnsHiCureEngine.this.isDnsCureSuitable() || DnsHiCureEngine.this.isRecoveryAction()) {
                this.mAlarmManager.set(3, SystemClock.elapsedRealtime() + 6000, this.mDnsAlarmIntent);
                return;
            }
            int waitTime = DnsHiCureEngine.WAIT_VERIFY_TIME;
            if (!DnsHiCureEngine.this.isDnsCuring()) {
                waitTime = (DnsHiCureEngine.this.mCellDnsesNum * 2000) + 1000;
            }
            DnsHiCureEngine.this.startProbeThread(DnsHiCureEngine.this.domains, waitTime, DnsHiCureEngine.this.mNetId);
            this.mAlarmManager.set(3, SystemClock.elapsedRealtime() + 6000 + ((long) waitTime), this.mDnsAlarmIntent);
        }

        public int getCurrentDnsFailedCounter() {
            try {
                return Integer.parseInt(SystemProperties.get(DnsHiCureEngine.DNS_MONITOR_FLAG, "0"));
            } catch (NumberFormatException e) {
                DnsHiCureEngine.this.loge("NumberFormatException");
                return 0;
            }
        }
    }

    class UnmonitoredState extends State {
        Intent intent = new Intent(DnsHiCureEngine.EVENT_DNS_PUNISH_ACTION).setPackage(DnsHiCureEngine.this.mContext.getPackageName());
        AlarmManager mAlarmManager = ((AlarmManager) DnsHiCureEngine.this.mContext.getSystemService("alarm"));
        PendingIntent mDnsAlarmIntent = PendingIntent.getBroadcast(DnsHiCureEngine.this.mContext, 0, this.intent, 134217728);

        UnmonitoredState() {
        }

        public void enter() {
            DnsHiCureEngine.this.log("UnmonitoredState start");
            DnsHiCureEngine.this.updataprivateDNScfg();
            if (DnsHiCureEngine.this.dnsSetPunishFlag) {
                this.mAlarmManager.set(3, SystemClock.elapsedRealtime() + 21600000, this.mDnsAlarmIntent);
            }
        }

        public boolean processMessage(Message message) {
            int i = message.what;
            if (i != 102) {
                switch (i) {
                    case 108:
                        DnsHiCureEngine.this.log("mUnmonitoredState processMessagedefault EVENT_DNS_PUNISH");
                        boolean unused = DnsHiCureEngine.this.dnsSetPunishFlag = false;
                        judgeTransition();
                        return true;
                    case 109:
                        DnsHiCureEngine.this.log("mUnmonitoredState processMessage EVENT_PRIVATE_DNS_SETTINGS_CHANGED");
                        DnsHiCureEngine.this.updataprivateDNScfg();
                        judgeTransition();
                        return true;
                    default:
                        DnsHiCureEngine dnsHiCureEngine = DnsHiCureEngine.this;
                        dnsHiCureEngine.log("mUnmonitoredState: default message.what=" + message.what);
                        return false;
                }
            } else {
                DnsHiCureEngine.this.log("mUnmonitoredState processMessage EVENT_DATA_CONNECTED");
                judgeTransition();
                return true;
            }
        }

        private void judgeTransition() {
            if (!DnsHiCureEngine.this.useHostname && true == DnsHiCureEngine.this.dsFlag && !DnsHiCureEngine.this.dnsSetPunishFlag) {
                DnsHiCureEngine.this.transitionTo(DnsHiCureEngine.this.mMonitoredState);
            }
        }
    }

    public static synchronized DnsHiCureEngine getInstance(Context context) {
        DnsHiCureEngine dnsHiCureEngine;
        synchronized (DnsHiCureEngine.class) {
            if (mDnsHiCureEngine == null) {
                mDnsHiCureEngine = new DnsHiCureEngine(context);
            }
            dnsHiCureEngine = mDnsHiCureEngine;
        }
        return dnsHiCureEngine;
    }

    public static synchronized DnsHiCureEngine getInstance() {
        DnsHiCureEngine dnsHiCureEngine;
        synchronized (DnsHiCureEngine.class) {
            dnsHiCureEngine = mDnsHiCureEngine;
        }
        return dnsHiCureEngine;
    }

    private DnsHiCureEngine(Context context) {
        super(TAG);
        this.mContext = context;
        this.mContentResolver = this.mContext.getContentResolver();
        this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        registerListener();
        this.mMonitoredState = new MonitoredState();
        this.mUnmonitoredState = new UnmonitoredState();
        this.mHiCureState = new HiCureState();
        this.mDefaultState = new DefaultState();
        this.mHandler = getHandler();
        this.mSettingsObserver = new SettingsObserver(this.mContext, this.mHandler);
        registerPrivateDnsSettingsCallbacks();
        registerReceivers();
        addState(this.mDefaultState);
        addState(this.mMonitoredState, this.mDefaultState);
        addState(this.mUnmonitoredState, this.mDefaultState);
        addState(this.mHiCureState, this.mDefaultState);
        setInitialState(this.mUnmonitoredState);
        start();
        log("DnsHiCureEngine start!");
    }

    private void registerPrivateDnsSettingsCallbacks() {
        this.mSettingsObserver.observe(Settings.Global.getUriFor("private_dns_mode"), 109);
    }

    private void registerReceivers() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(EVENT_DNS_MONITOR_ACTION);
        intentFilter.addAction(EVENT_DNS_PUNISH_ACTION);
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (DnsHiCureEngine.EVENT_DNS_MONITOR_ACTION.equals(intent.getAction())) {
                    DnsHiCureEngine.this.sendMessage(103);
                } else if (DnsHiCureEngine.EVENT_DNS_PUNISH_ACTION.equals(intent.getAction())) {
                    DnsHiCureEngine.this.sendMessage(108);
                }
            }
        }, intentFilter);
    }

    private void registerListener() {
        this.mTelephonyManager.listen(this.listenerSim0, 257);
        this.mTelephonyManager.listen(this.listenerSim1, 257);
    }

    /* access modifiers changed from: private */
    public boolean isDnsCureSuitable() {
        int subId = SubscriptionManager.getDefaultDataSubscriptionId();
        boolean signalPoor = isSignalPoor(subId);
        boolean dataServiceIn = isDataServiceIn(subId);
        boolean isWhiteDns = isOneInWhiteList();
        boolean notCallState = isNotCalling();
        log("isDnsCureSuitable " + signalPoor + "," + dataServiceIn + "," + isWhiteDns + "," + notCallState);
        return !signalPoor && dataServiceIn && !isWhiteDns && notCallState;
    }

    private boolean isSignalPoor(int subId) {
        SignalStrength signalStrength;
        boolean ret = true;
        int RAT_class = TelephonyManager.getNetworkClass(this.mTelephonyManager.getNetworkType(subId));
        if (subId == 0) {
            signalStrength = this.mSignalStrengthSim0;
        } else {
            signalStrength = this.mSignalStrengthSim1;
        }
        if (signalStrength == null) {
            log("signalStrength is null");
            return true;
        }
        if (1 != RAT_class && signalStrength.getLevel() >= 2) {
            ret = false;
        }
        return ret;
    }

    private boolean isDataServiceIn(int subId) {
        ServiceState serviceState;
        boolean ret = false;
        if (subId == 0) {
            serviceState = this.mServiceState0;
        } else {
            serviceState = this.mServiceState1;
        }
        if (serviceState == null) {
            log("serviceState is null");
            return false;
        }
        if (serviceState.getDataRegState() == 0) {
            ret = true;
        }
        return ret;
    }

    private boolean isNotCalling() {
        int callState0 = this.mTelephonyManager.getCallState(0);
        int callState1 = this.mTelephonyManager.getCallState(1);
        if (callState0 == 0 && callState1 == 0) {
            return true;
        }
        return false;
    }

    private boolean isOneInWhiteList() {
        return isDnsInWhiteList(this.mCellDnses[0]) || isDnsInWhiteList(this.mCellDnses[1]);
    }

    private boolean isDnsInWhiteList(String dns) {
        for (String wDns : mDnsWhiteList) {
            if (wDns.equals(dns)) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    public boolean isDnsCuring() {
        return this.mDnsCureState;
    }

    /* access modifiers changed from: private */
    public void startProbeThread(String[] domain, int time, int netid) {
        sendMessageDelayed(111, (long) time);
        DnsProbe dnsProbe = new DnsProbe(domain, time, netid, getHandler(), 112, 111);
        this.mDnsProbe = dnsProbe;
        this.mDnsProbe.startProbe();
    }

    /* access modifiers changed from: private */
    public void setDns(String[] ndnses, LinkProperties lp, int netid) {
        log("setdns:publicDnsFlag=" + this.publicDnsFlag);
        Collection<InetAddress> newdnses = new ArrayList<>();
        int length = ndnses.length;
        for (int i = 0; i < length; i++) {
            String dnsAddr = ndnses[i];
            if (dnsAddr != null && !dnsAddr.isEmpty()) {
                String dnsAddr2 = dnsAddr.trim();
                try {
                    InetAddress ia = NetworkUtils.numericToInetAddress(dnsAddr2);
                    if (!ia.isAnyLocalAddress()) {
                        newdnses.add(ia);
                    }
                } catch (IllegalArgumentException e) {
                    log("Non-numeric dns addr=" + dnsAddr2);
                }
            }
        }
        if (lp != null) {
            lp.setDnsServers(newdnses);
            setDnsConfigurationForNetwork(netid, lp, false);
        }
    }

    private void setDnsConfigurationForNetwork(int netid, LinkProperties newlinkproperties, boolean isdefaultnetwork) {
        try {
            ServiceManager.getService("connectivity").getDnsManager().setDnsConfigurationForNetwork(netid, newlinkproperties, isdefaultnetwork);
        } catch (Exception e) {
            loge("Exception in setDnsConfigurationForNetwork: " + e);
        }
    }

    /* access modifiers changed from: private */
    public boolean isRecoveryAction() {
        int action = 0;
        try {
            action = Settings.System.getInt(this.mContentResolver, DORECOVERY_FLAG);
        } catch (Settings.SettingNotFoundException e) {
            log("Settings Exception Reading Dorecovery Values");
        }
        log("isRecoveryAction:" + action);
        if (action > 0) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    public void updataprivateDNScfg() {
        if ("hostname".equals(Settings.Global.getString(this.mContentResolver, "private_dns_mode"))) {
            this.useHostname = true;
        } else {
            this.useHostname = false;
        }
        this.publicDnsFlag = false;
    }

    public void notifyConnectedInfo(NetworkAgentInfo nai) {
        if (nai != null) {
            this.mLinkProperties = new LinkProperties(nai.linkProperties);
            this.mNetId = nai.network.netId;
            this.publicDnsFlag = false;
            this.mNetDns = this.mLinkProperties.getDnsServers();
            if (this.mNetDns == null) {
                log("NetDns = null");
            } else if (this.mNetDns.size() < 1) {
                log("NetDns.size() = " + this.mNetDns.size());
            } else {
                log("NetDns.size() = " + this.mNetDns.size());
                this.mCellDnsesNum = 0;
                int i = 4;
                if (this.mNetDns.size() <= 4) {
                    i = this.mNetDns.size();
                }
                int dnsCnt = i;
                for (int i2 = 0; i2 < dnsCnt; i2++) {
                    String temp = this.mNetDns.get(i2).toString();
                    this.mCellDnses[i2] = temp.substring(1, temp.length());
                    log("cellDnses = " + this.mCellDnses[i2] + ", temp =" + temp);
                    if (this.mCellDnses[i2].length() > 0) {
                        this.mCellDnsesNum++;
                    }
                }
                this.mDnsCureState = false;
                this.dsFlag = true;
                sendMessage(102);
            }
        }
    }

    public void notifyDisconnectedInfo() {
        this.mLinkProperties = null;
        this.mNetId = 0;
        this.dsFlag = false;
        this.mDnsCureState = false;
        sendMessage(101);
    }

    public void notifyChrDnsHiCureResult(String firstip, String secondip, boolean result) {
        Intent intent = new Intent(INTENT_DS_DNS_HICURE_RESULT);
        intent.putExtra("FirstDnsAddress", firstip);
        intent.putExtra("SecondDnsAddress", secondip);
        intent.putExtra("HiCureResult", result);
        log("sendBroadcast com.android.intent.action.dns_hicure_result");
        this.mContext.sendBroadcast(intent, CHR_BROADCAST_PERMISSION);
    }
}
