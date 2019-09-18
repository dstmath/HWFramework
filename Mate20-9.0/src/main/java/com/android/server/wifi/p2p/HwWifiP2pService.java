package com.android.server.wifi.p2p;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.InterfaceConfiguration;
import android.net.LinkAddress;
import android.net.NetworkInfo;
import android.net.NetworkUtils;
import android.net.RouteInfo;
import android.net.TrafficStats;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.PowerManager;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.util.Slog;
import android.widget.Toast;
import com.android.internal.util.AsyncChannel;
import com.android.internal.util.State;
import com.android.server.wifi.HwQoE.HwQoEService;
import com.android.server.wifi.HwQoE.HwQoEUtils;
import com.android.server.wifi.HwWifiCHRService;
import com.android.server.wifi.HwWifiCHRServiceImpl;
import com.android.server.wifi.HwWifiStateMachine;
import com.android.server.wifi.WifiNativeUtils;
import com.android.server.wifi.WifiRepeater;
import com.android.server.wifi.WifiRepeaterConfigStore;
import com.android.server.wifi.WifiRepeaterController;
import com.android.server.wifi.p2p.HwWifiP2pService;
import com.android.server.wifi.p2p.WifiP2pServiceImpl;
import com.android.server.wifi.util.WifiCommonUtils;
import com.huawei.utils.reflect.EasyInvokeFactory;
import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class HwWifiP2pService extends WifiP2pServiceImpl {
    private static final String ACTION_DEVICE_DELAY_IDLE = "com.android.server.wifi.p2p.action.DEVICE_DELAY_IDLE";
    private static final int BAND_ERROR = -1;
    private static final int BASE = 143360;
    private static final String[] BLACKLIST_P2P_FIND = {"com.hp.android.printservice"};
    private static final int CHANNEL_ERROR = -1;
    public static final int CMD_BATTERY_CHANGED = 143469;
    public static final int CMD_DEVICE_DELAY_IDLE = 143465;
    public static final int CMD_LINKSPEED_POLL = 143470;
    public static final int CMD_REQUEST_REPEATER_CONFIG = 143463;
    public static final int CMD_RESPONSE_REPEATER_CONFIG = 143464;
    public static final int CMD_SCREEN_OFF = 143467;
    public static final int CMD_SCREEN_ON = 143466;
    public static final int CMD_SET_REPEATER_CONFIG = 143461;
    public static final int CMD_SET_REPEATER_CONFIG_COMPLETED = 143462;
    public static final int CMD_USER_PRESENT = 143468;
    private static final int CODE_GET_GROUP_CONFIG_INFO = 1005;
    private static final int CODE_GET_WIFI_REPEATER_CONFIG = 1001;
    private static final int CODE_SET_WIFI_REPEATER_CONFIG = 1002;
    private static final int CODE_WIFI_MAGICLINK_CONFIG_IP = 1003;
    private static final int CODE_WIFI_MAGICLINK_RELEASE_IP = 1004;
    private static final int CONNECT_FAILURE = -1;
    private static final int CONNECT_SUCCESS = 0;
    private static final boolean DBG = true;
    private static final long DEFAULT_IDLE_MS = 1800000;
    private static final long DEFAULT_LOW_DATA_TRAFFIC_LINE = 102400;
    private static final long DELAY_IDLE_MS = 60000;
    private static final String DESCRIPTOR = "android.net.wifi.p2p.IWifiP2pManager";
    private static final String HUAWEI_WIFI_DEVICE_DELAY_IDLE = "huawei.android.permission.WIFI_DEVICE_DELAY_IDLE";
    /* access modifiers changed from: private */
    public static final boolean HWDBG;
    private static final boolean HWLOGW_E = true;
    private static long INTERVAL_DISALLOW_P2P_FIND = 130000;
    private static final int LINKSPEED_ESTIMATE_TIMES = 4;
    private static final int LINKSPEED_POLL_INTERVAL = 1000;
    private static final Boolean NO_REINVOCATION = false;
    private static final int P2P_BAND_2G = 0;
    private static final int P2P_BAND_5G = 1;
    private static final int P2P_CHOOSE_CHANNEL_RANDOM = 0;
    /* access modifiers changed from: private */
    public static final Boolean RELOAD = true;
    private static final String SERVER_ADDRESS_WIFI_BRIDGE = "192.168.43.1";
    private static final String SERVER_ADDRESS_WIFI_BRIDGE_OTHER = "192.168.50.1";
    private static final String TAG = "HwWifiP2pService";
    /* access modifiers changed from: private */
    public static final Boolean TRY_REINVOCATION = true;
    private static final int WHITELIST_DURATION_MS = 15000;
    private static WifiNativeUtils wifiNativeUtils = EasyInvokeFactory.getInvokeUtils(WifiNativeUtils.class);
    /* access modifiers changed from: private */
    public static WifiP2pServiceUtils wifiP2pServiceUtils = EasyInvokeFactory.getInvokeUtils(WifiP2pServiceUtils.class);
    /* access modifiers changed from: private */
    public AlarmManager mAlarmManager;
    private final BroadcastReceiver mAlarmReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                Slog.d(HwWifiP2pService.TAG, "onReceive, action:" + action);
                if (action.equals(HwWifiP2pService.ACTION_DEVICE_DELAY_IDLE)) {
                    HwWifiP2pService.this.mP2pStateMachine.sendMessage(HwWifiP2pService.CMD_DEVICE_DELAY_IDLE);
                }
            }
        }
    };
    private String mConfigInfo;
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public PendingIntent mDefaultIdleIntent;
    /* access modifiers changed from: private */
    public PendingIntent mDelayIdleIntent;
    private HwP2pStateMachine mHwP2pStateMachine = null;
    HwWifiCHRService mHwWifiCHRService;
    private String mInterface = "";
    private boolean mIsWifiRepeaterTetherStarted = false;
    /* access modifiers changed from: private */
    public volatile int mLastLinkSpeed = 0;
    /* access modifiers changed from: private */
    public long mLastRxBytes = 0;
    /* access modifiers changed from: private */
    public long mLastTxBytes = 0;
    /* access modifiers changed from: private */
    public boolean mLegacyGO = false;
    /* access modifiers changed from: private */
    public int mLinkSpeedCounter = 0;
    /* access modifiers changed from: private */
    public int mLinkSpeedPollToken = 0;
    /* access modifiers changed from: private */
    public int[] mLinkSpeedWeights;
    /* access modifiers changed from: private */
    public int[] mLinkSpeeds = new int[4];
    /* access modifiers changed from: private */
    public boolean mMagicLinkDeviceFlag = false;
    NetworkInfo mNetworkInfo = new NetworkInfo(1, 0, "WIFI", "");
    private List<P2pFindProcessInfo> mP2pFindProcessInfoList = null;
    NetworkInfo mP2pNetworkInfo = new NetworkInfo(13, 0, "WIFI_P2P", "");
    private PowerManager mPowerManager = null;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Slog.d(HwWifiP2pService.TAG, "onReceive, action:" + action);
            if (action != null) {
                if (action.equals("android.intent.action.SCREEN_ON")) {
                    HwWifiP2pService.this.mP2pStateMachine.sendMessage(HwWifiP2pService.CMD_SCREEN_ON);
                } else if (action.equals("android.intent.action.USER_PRESENT")) {
                    HwWifiP2pService.this.mP2pStateMachine.sendMessage(HwWifiP2pService.CMD_USER_PRESENT);
                } else if (action.equals("android.intent.action.SCREEN_OFF")) {
                    HwWifiP2pService.this.mP2pStateMachine.sendMessage(HwWifiP2pService.CMD_SCREEN_OFF);
                } else if (action.equals("android.net.wifi.STATE_CHANGE")) {
                    NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    if (networkInfo != null) {
                        HwWifiP2pService.this.mNetworkInfo = networkInfo;
                    }
                } else if (action.equals("android.net.wifi.p2p.CONNECTION_STATE_CHANGE")) {
                    NetworkInfo p2pNetworkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    if (p2pNetworkInfo != null) {
                        HwWifiP2pService.this.mP2pNetworkInfo = p2pNetworkInfo;
                    }
                }
            }
        }
    };
    private String mTetherInterfaceName;
    /* access modifiers changed from: private */
    public List<Pair<String, Long>> mValidDeivceList = new ArrayList();
    /* access modifiers changed from: private */
    public Handler mWifiP2pDataTrafficHandler;
    /* access modifiers changed from: private */
    public WifiRepeater mWifiRepeater;
    private long mWifiRepeaterBeginWorkTime = 0;
    /* access modifiers changed from: private */
    public AsyncChannel mWifiRepeaterConfigChannel;
    private WifiRepeaterConfigStore mWifiRepeaterConfigStore;
    /* access modifiers changed from: private */
    public boolean mWifiRepeaterEnabled = false;
    private long mWifiRepeaterEndWorkTime = 0;
    private int mWifiRepeaterFreq = 0;
    HandlerThread wifip2pThread = new HandlerThread("WifiP2pService");

    class HwP2pStateMachine extends WifiP2pServiceImpl.P2pStateMachine {
        private static final int DEFAULT_GROUP_OWNER_INTENT = 6;
        private Message mCreatPskGroupMsg;

        HwP2pStateMachine(String name, Looper looper, boolean p2pSupported) {
            super(HwWifiP2pService.this, name, looper, p2pSupported);
        }

        public boolean handleDefaultStateMessage(Message message) {
            switch (message.what) {
                case HwWifiStateMachine.CMD_STOP_WIFI_REPEATER:
                    if (HwWifiP2pService.this.getWifiRepeaterEnabled()) {
                        sendMessage(139280);
                        break;
                    }
                    break;
                case 141264:
                    String addDeviceAddress = message.getData().getString("avlidDevice");
                    HwWifiP2pService hwWifiP2pService = HwWifiP2pService.this;
                    hwWifiP2pService.logd("add p2p deivce valid addDeviceAddress = " + addDeviceAddress);
                    addP2PValidDevice(addDeviceAddress);
                    break;
                case 141265:
                    String removeDeviceAddress = message.getData().getString("avlidDevice");
                    HwWifiP2pService hwWifiP2pService2 = HwWifiP2pService.this;
                    hwWifiP2pService2.logd("remove p2p valid deivce removeDeviceAddress = " + removeDeviceAddress);
                    removeP2PValidDevice(removeDeviceAddress);
                    break;
                case 141266:
                    HwWifiP2pService.this.logd("clear p2p valid deivce");
                    clearP2PValidDevice();
                    break;
                case 141268:
                case 141270:
                    HwWifiP2pService.wifiP2pServiceUtils.replyToMessage(HwWifiP2pService.this.mP2pStateMachine, message, 139278, 2);
                    break;
                case 141269:
                    HwWifiP2pService.wifiP2pServiceUtils.replyToMessage(HwWifiP2pService.this.mP2pStateMachine, message, 139272, 2);
                    break;
                case 141271:
                    HwWifiP2pService.wifiP2pServiceUtils.replyToMessage(HwWifiP2pService.this.mP2pStateMachine, message, 139281, 2);
                    if (HwWifiP2pService.this.getWifiRepeaterEnabled()) {
                        HwWifiP2pService.this.stopWifiRepeater(HwWifiP2pService.wifiP2pServiceUtils.getmGroup(HwWifiP2pService.this.mP2pStateMachine));
                        break;
                    }
                    break;
                case HwWifiP2pService.CMD_DEVICE_DELAY_IDLE /*143465*/:
                    HwWifiP2pService.this.mWifiP2pDataTrafficHandler.sendMessage(Message.obtain(HwWifiP2pService.this.mWifiP2pDataTrafficHandler, 0));
                    break;
                case HwWifiP2pService.CMD_SCREEN_ON /*143466*/:
                    Slog.d(HwWifiP2pService.TAG, "cancel alarm.");
                    HwWifiP2pService.this.mAlarmManager.cancel(HwWifiP2pService.this.mDefaultIdleIntent);
                    HwWifiP2pService.this.mAlarmManager.cancel(HwWifiP2pService.this.mDelayIdleIntent);
                    break;
                case HwWifiP2pService.CMD_SCREEN_OFF /*143467*/:
                    long unused = HwWifiP2pService.this.mLastTxBytes = 0;
                    long unused2 = HwWifiP2pService.this.mLastRxBytes = 0;
                    if (HwWifiP2pService.this.shouldDisconnectWifiP2p()) {
                        if (HwWifiP2pService.this.mNetworkInfo.getDetailedState() != NetworkInfo.DetailedState.CONNECTED || !HwWifiP2pService.this.mP2pNetworkInfo.isConnected()) {
                            if (HwWifiP2pService.this.mP2pNetworkInfo.isConnected()) {
                                Slog.d(HwWifiP2pService.TAG, "start to removeP2PGroup.");
                                HwWifiP2pService.this.handleUpdataDateTraffic();
                                break;
                            }
                        } else {
                            WifiP2pGroup wifiP2pGroup = HwWifiP2pService.wifiP2pServiceUtils.getmGroup(HwWifiP2pService.this.mP2pStateMachine);
                            long delayTimeMs = HwWifiP2pService.DEFAULT_IDLE_MS;
                            if (wifiP2pGroup != null && wifiP2pGroup.isGroupOwner() && wifiP2pGroup.getClientList().size() == 0) {
                                delayTimeMs = HwQoEService.GAME_RTT_NOTIFY_INTERVAL;
                            }
                            Slog.d(HwWifiP2pService.TAG, "set default idle timer: " + delayTimeMs + " ms");
                            HwWifiP2pService.this.mAlarmManager.set(0, System.currentTimeMillis() + delayTimeMs, HwWifiP2pService.this.mDefaultIdleIntent);
                            break;
                        }
                    }
                    break;
                case 147459:
                    HwWifiP2pService.this.sendNetworkConnectedBroadcast((String) message.obj);
                    break;
                case 147460:
                    HwWifiP2pService.this.sendNetworkDisconnectedBroadcast((String) message.obj);
                    break;
                case 147577:
                    break;
                default:
                    HwWifiP2pService hwWifiP2pService3 = HwWifiP2pService.this;
                    hwWifiP2pService3.loge("Unhandled message " + message);
                    return false;
            }
            return true;
        }

        public boolean handleP2pEnabledStateExMessage(Message message) {
            if (message.what != 147577) {
                HwWifiP2pService hwWifiP2pService = HwWifiP2pService.this;
                hwWifiP2pService.loge("Unhandled message " + message);
                return false;
            }
            HwWifiP2pService.this.sendHwP2pDeviceExInfoBroadcast((byte[]) message.obj);
            return true;
        }

        public boolean handleOngoingGroupRemovalStateExMessage(Message message) {
            if (message.what != 141271) {
                HwWifiP2pService hwWifiP2pService = HwWifiP2pService.this;
                hwWifiP2pService.loge("Unhandled message " + message);
                return false;
            }
            replyToMessage(message, 139282);
            return true;
        }

        public boolean handleGroupNegotiationStateExMessage(Message message) {
            if (message.what != 141271) {
                HwWifiP2pService hwWifiP2pService = HwWifiP2pService.this;
                hwWifiP2pService.loge("Unhandled message " + message);
                return false;
            }
            logd(getName() + " MAGICLINK_REMOVE_GC_GROUP");
            String p2pInterface = ((Bundle) message.obj).getString("iface");
            logd(getName() + "p2pInterface :" + p2pInterface);
            if (p2pInterface == null || p2pInterface.equals("")) {
                HwWifiP2pService.wifiP2pServiceUtils.replyToMessage(HwWifiP2pService.this.mP2pStateMachine, message, 139281, 0);
                transitionTo(HwWifiP2pService.wifiP2pServiceUtils.getmInactiveState(HwWifiP2pService.this.mP2pStateMachine));
            } else {
                logd(getName() + " MAGICLINK_REMOVE_GC_GROUP,p2pInterface !=null,now remove it");
                if (this.mWifiNative.p2pGroupRemove(p2pInterface)) {
                    replyToMessage(message, 139282);
                    HwWifiP2pService.wifiP2pServiceUtils.sendP2pConnectionChangedBroadcast(HwWifiP2pService.this.mP2pStateMachine);
                } else {
                    HwWifiP2pService.wifiP2pServiceUtils.handleGroupRemoved(HwWifiP2pService.this.mP2pStateMachine);
                    HwWifiP2pService.wifiP2pServiceUtils.replyToMessage(HwWifiP2pService.this.mP2pStateMachine, message, 139281, 0);
                }
                transitionTo(HwWifiP2pService.wifiP2pServiceUtils.getmInactiveState(HwWifiP2pService.this.mP2pStateMachine));
            }
            return true;
        }

        public boolean handleGroupCreatedStateExMessage(Message message) {
            int i = message.what;
            if (i == 141271) {
                logd(getName() + " MAGICLINK_REMOVE_GC_GROUP");
                HwWifiP2pService.wifiP2pServiceUtils.enableBTCoex(HwWifiP2pService.this.mP2pStateMachine);
                if (this.mWifiNative.p2pGroupRemove(HwWifiP2pService.wifiP2pServiceUtils.getmGroup(HwWifiP2pService.this.mP2pStateMachine).getInterface())) {
                    transitionTo(HwWifiP2pService.wifiP2pServiceUtils.getmOngoingGroupRemovalState(HwWifiP2pService.this.mP2pStateMachine));
                    replyToMessage(message, 139282);
                } else {
                    HwWifiP2pService.wifiP2pServiceUtils.handleGroupRemoved(HwWifiP2pService.this.mP2pStateMachine);
                    transitionTo(HwWifiP2pService.wifiP2pServiceUtils.getmInactiveState(HwWifiP2pService.this.mP2pStateMachine));
                    HwWifiP2pService.wifiP2pServiceUtils.replyToMessage(HwWifiP2pService.this.mP2pStateMachine, message, 139281, 0);
                }
                if (HwWifiP2pService.this.getWifiRepeaterEnabled()) {
                    HwWifiP2pService.this.stopWifiRepeater(HwWifiP2pService.wifiP2pServiceUtils.getmGroup(HwWifiP2pService.this.mP2pStateMachine));
                }
            } else if (i == 143374) {
                logd(" SET_MIRACAST_MODE: " + message.arg1);
                if (1 == message.arg1) {
                    int unused = HwWifiP2pService.this.mLastLinkSpeed = -1;
                    int unused2 = HwWifiP2pService.this.mLinkSpeedCounter = 0;
                    int unused3 = HwWifiP2pService.this.mLinkSpeedPollToken = 0;
                    sendMessage(HwWifiP2pService.CMD_LINKSPEED_POLL, HwWifiP2pService.this.mLinkSpeedPollToken);
                }
                return false;
            } else if (i != 143470) {
                HwWifiP2pService.this.loge("Unhandled message when=" + message.getWhen() + " what=" + message.what + " arg1=" + message.arg1 + " arg2=" + message.arg2);
                return false;
            } else if (HwWifiP2pService.this.mLinkSpeedPollToken == message.arg1) {
                String ifname = HwWifiP2pService.wifiP2pServiceUtils.getmGroup(HwWifiP2pService.this.mP2pStateMachine).getInterface();
                int linkSpeed = SystemProperties.getInt("wfd.config.linkspeed", 0);
                if (linkSpeed == 0) {
                    linkSpeed = this.mWifiNative.getLinkSpeed(ifname);
                }
                logd("ifname: " + ifname + ", get linkspeed from wpa: " + linkSpeed + ", mLinkSpeed " + linkSpeed);
                if (HwWifiP2pService.this.mLinkSpeedCounter < 4) {
                    HwWifiP2pService.this.mLinkSpeeds[HwWifiP2pService.this.mLinkSpeedCounter = HwWifiP2pService.this.mLinkSpeedCounter + 1] = linkSpeed;
                }
                if (HwWifiP2pService.this.mLinkSpeedCounter >= 4) {
                    int avarageLinkSpeed = 0;
                    for (int i2 = 0; i2 < 4; i2++) {
                        avarageLinkSpeed += HwWifiP2pService.this.mLinkSpeeds[i2] * HwWifiP2pService.this.mLinkSpeedWeights[i2];
                    }
                    int avarageLinkSpeed2 = avarageLinkSpeed / 100;
                    if (HwWifiP2pService.this.mLastLinkSpeed != avarageLinkSpeed2) {
                        int unused4 = HwWifiP2pService.this.mLastLinkSpeed = avarageLinkSpeed2;
                        HwWifiP2pService.this.sendLinkSpeedChangedBroadcast();
                    }
                    int unused5 = HwWifiP2pService.this.mLinkSpeedCounter = 0;
                }
                sendMessageDelayed(HwWifiP2pService.CMD_LINKSPEED_POLL, HwWifiP2pService.access$1604(HwWifiP2pService.this), 1000);
            }
            return true;
        }

        public boolean handleP2pNotSupportedStateMessage(Message message) {
            if (message.what != 141268) {
                HwWifiP2pService hwWifiP2pService = HwWifiP2pService.this;
                hwWifiP2pService.loge("Unhandled message " + message);
                return false;
            }
            HwWifiP2pService.wifiP2pServiceUtils.replyToMessage(HwWifiP2pService.this.mP2pStateMachine, message, 139278, 1);
            return true;
        }

        public boolean handleInactiveStateMessage(Message message) {
            boolean mret;
            int i = message.what;
            if (i == 143464) {
                WifiConfiguration config = (WifiConfiguration) message.obj;
                if (config != null) {
                    creatGroupForRepeater(config);
                } else {
                    HwWifiP2pService.this.loge("wifi repeater config is null!");
                }
            } else if (i != 147557) {
                switch (i) {
                    case 141267:
                        WifiP2pConfig beam_config = (WifiP2pConfig) message.obj;
                        HwWifiP2pService.wifiP2pServiceUtils.setAutonomousGroup(HwWifiP2pService.this, false);
                        HwWifiP2pService.this.updateGroupCapability(this.mPeers, beam_config.deviceAddress, this.mWifiNative.getGroupCapability(beam_config.deviceAddress));
                        if (beam_connect(beam_config, HwWifiP2pService.TRY_REINVOCATION.booleanValue()) != -1) {
                            HwWifiP2pService.this.updateStatus(this.mPeers, this.mSavedPeerConfig.deviceAddress, 1);
                            sendPeersChangedBroadcast();
                            replyToMessage(message, 139273);
                            transitionTo(this.mGroupNegotiationState);
                            break;
                        } else {
                            replyToMessage(message, 139272);
                            break;
                        }
                    case 141268:
                        if (!HwWifiP2pService.this.mWifiRepeater.isEncryptionTypeTetheringAllowed()) {
                            HwWifiP2pService.wifiP2pServiceUtils.replyToMessage(HwWifiP2pService.this.mP2pStateMachine, this.mCreatPskGroupMsg, 139278, 0);
                            HwWifiP2pService.this.setWifiRepeaterState(5);
                            break;
                        } else {
                            HwWifiP2pService.this.setWifiRepeaterState(3);
                            HwWifiP2pService.wifiP2pServiceUtils.setAutonomousGroup(HwWifiP2pService.this, true);
                            if (HwWifiP2pService.this.mWifiRepeaterConfigChannel != null) {
                                this.mCreatPskGroupMsg = message;
                                WifiConfiguration userconfig = (WifiConfiguration) message.obj;
                                if (userconfig != null) {
                                    HwWifiP2pService.this.mWifiRepeaterConfigChannel.sendMessage(HwWifiP2pService.CMD_SET_REPEATER_CONFIG, userconfig);
                                    creatGroupForRepeater(userconfig);
                                    break;
                                } else {
                                    HwWifiP2pService.this.mWifiRepeaterConfigChannel.sendMessage(HwWifiP2pService.CMD_REQUEST_REPEATER_CONFIG);
                                    break;
                                }
                            }
                        }
                        break;
                    case 141269:
                        String info = ((Bundle) message.obj).getString("cfg");
                        if (!TextUtils.isEmpty(info)) {
                            String[] tokens = info.split("\n");
                            if (tokens.length >= 4) {
                                StringBuffer buf = new StringBuffer();
                                buf.append("P\"" + tokens[0] + "\"\n" + tokens[1] + "\n\"" + tokens[2] + "\"\n" + tokens[3]);
                                for (int i2 = 4; i2 < tokens.length; i2++) {
                                    if (4 == i2) {
                                        try {
                                            boolean unused = HwWifiP2pService.this.mLegacyGO = 1 == Integer.parseInt(tokens[4]);
                                        } catch (Exception e) {
                                            Slog.e(HwWifiP2pService.TAG, "mLegacyGO = " + HwWifiP2pService.this.mLegacyGO);
                                            HwWifiP2pService.wifiP2pServiceUtils.replyToMessage(HwWifiP2pService.this.mP2pStateMachine, message, 139272, 0);
                                            return true;
                                        }
                                    }
                                    buf.append("\n" + tokens[i2]);
                                }
                                this.mWifiNative.magiclinkConnect(buf.toString());
                                break;
                            }
                        }
                        break;
                    case 141270:
                        HwWifiP2pService.wifiP2pServiceUtils.setAutonomousGroup(HwWifiP2pService.this, true);
                        int mnetId = message.arg1;
                        String freq = ((Bundle) message.obj).getString("freq");
                        if (mnetId == -2) {
                            int mnetId2 = this.mGroups.getNetworkId(HwWifiP2pService.wifiP2pServiceUtils.getmThisDevice(HwWifiP2pService.this).deviceAddress);
                            if (mnetId2 != -1) {
                                mret = this.mWifiNative.magiclinkGroupAdd(mnetId2, freq);
                            } else {
                                mret = this.mWifiNative.magiclinkGroupAdd(true, freq);
                            }
                        } else {
                            mret = this.mWifiNative.magiclinkGroupAdd(false, freq);
                        }
                        if (!mret) {
                            HwWifiP2pService.wifiP2pServiceUtils.replyToMessage(HwWifiP2pService.this.mP2pStateMachine, message, 139278, 0);
                            break;
                        } else {
                            replyToMessage(message, 139279);
                            transitionTo(this.mGroupNegotiationState);
                            break;
                        }
                    default:
                        HwWifiP2pService hwWifiP2pService = HwWifiP2pService.this;
                        hwWifiP2pService.loge("Unhandled message when=" + message.getWhen() + " what=" + message.what + " arg1=" + message.arg1 + " arg2=" + message.arg2);
                        return false;
                }
            } else {
                HwWifiP2pService.this.sendInterfaceCreatedBroadcast((String) message.obj);
                boolean unused2 = HwWifiP2pService.this.mMagicLinkDeviceFlag = !HwWifiP2pService.this.mLegacyGO;
                transitionTo(this.mGroupNegotiationState);
            }
            return true;
        }

        private void creatGroupForRepeater(WifiConfiguration config) {
            boolean unused = HwWifiP2pService.this.mWifiRepeaterEnabled = true;
            config.apChannel = HwWifiP2pService.this.mWifiRepeater.retrieveDownstreamChannel();
            config.apBand = HwWifiP2pService.this.mWifiRepeater.retrieveDownstreamBand();
            if (config.apChannel == -1 || config.apBand == -1) {
                HwWifiP2pService.wifiP2pServiceUtils.replyToMessage(HwWifiP2pService.this.mP2pStateMachine, this.mCreatPskGroupMsg, 139278, 0);
                HwWifiP2pService.this.setWifiRepeaterState(5);
                boolean unused2 = HwWifiP2pService.this.mWifiRepeaterEnabled = false;
                return;
            }
            if (HwWifiP2pService.this.isWifiConnected()) {
                StringBuilder sb = new StringBuilder("WifiRepeater=y");
                sb.append("\nssid=");
                sb.append(config.SSID);
                sb.append("\npsk=");
                sb.append(new SensitiveArg(config.preSharedKey));
                sb.append("\nchannel=");
                sb.append(config.apChannel);
                sb.append("\nband=");
                if (!this.mWifiNative.addP2pRptGroup(sb.append(config.apBand).toString())) {
                    boolean unused3 = HwWifiP2pService.this.mWifiRepeaterEnabled = false;
                }
            } else {
                boolean unused4 = HwWifiP2pService.this.mWifiRepeaterEnabled = false;
                HwWifiP2pService.this.loge("wifirpt: isWifiConnected = false");
            }
            if (HwWifiP2pService.this.mWifiRepeaterEnabled) {
                Settings.Global.putInt(HwWifiP2pService.this.mContext.getContentResolver(), "wifi_repeater_on", 6);
                replyToMessage(this.mCreatPskGroupMsg, 139279);
                transitionTo(this.mGroupNegotiationState);
                if (HwWifiP2pService.HWDBG) {
                    HwWifiP2pService.this.logd("wifirpt: CREATE_GROUP_PSK SUCCEEDED, now transitionTo GroupNegotiationState");
                }
            } else {
                HwWifiP2pService.wifiP2pServiceUtils.replyToMessage(HwWifiP2pService.this.mP2pStateMachine, this.mCreatPskGroupMsg, 139278, 0);
                HwWifiP2pService.this.setWifiRepeaterState(5);
                HwWifiP2pService.this.loge("wifirpt: CREATE_GROUP_PSK FAILED, remain at this state.");
            }
        }

        private synchronized void addP2PValidDevice(String deviceAddress) {
            if (deviceAddress != null) {
                Iterator<Pair<String, Long>> iter = HwWifiP2pService.this.mValidDeivceList.iterator();
                while (iter.hasNext()) {
                    if (((String) iter.next().first).equals(deviceAddress)) {
                        iter.remove();
                    }
                }
                HwWifiP2pService.this.mValidDeivceList.add(new Pair(deviceAddress, Long.valueOf(SystemClock.elapsedRealtime())));
            }
        }

        private synchronized void removeP2PValidDevice(String deviceAddress) {
            if (HwWifiP2pService.this.mValidDeivceList != null) {
                Iterator<Pair<String, Long>> iter = HwWifiP2pService.this.mValidDeivceList.iterator();
                while (iter.hasNext()) {
                    if (((String) iter.next().first).equals(deviceAddress)) {
                        iter.remove();
                    }
                }
            }
        }

        private void cleanupValidDevicelist() {
            long curTime = SystemClock.elapsedRealtime();
            Iterator<Pair<String, Long>> iter = HwWifiP2pService.this.mValidDeivceList.iterator();
            while (iter.hasNext()) {
                if (curTime - ((Long) iter.next().second).longValue() > 15000) {
                    iter.remove();
                }
            }
        }

        private synchronized boolean isP2PValidDevice(String deviceAddress) {
            cleanupValidDevicelist();
            for (Pair<String, Long> entry : HwWifiP2pService.this.mValidDeivceList) {
                if (((String) entry.first).equals(deviceAddress)) {
                    return true;
                }
            }
            return false;
        }

        private synchronized void clearP2PValidDevice() {
            HwWifiP2pService.this.mValidDeivceList.clear();
        }

        private int beam_connect(WifiP2pConfig config, boolean tryInvocation) {
            if (config == null) {
                HwWifiP2pService.this.loge("config is null");
                return -1;
            }
            this.mSavedPeerConfig = config;
            WifiP2pDevice dev = this.mPeers.get(config.deviceAddress);
            if (dev == null) {
                HwWifiP2pService.this.loge("target device not found ");
                return -1;
            }
            boolean join = dev.isGroupOwner();
            String ssid = this.mWifiNative.p2pGetSsid(dev.deviceAddress);
            HwWifiP2pService hwWifiP2pService = HwWifiP2pService.this;
            hwWifiP2pService.logd("target ssid is " + ssid + " join:" + join);
            if (join && dev.isGroupLimit()) {
                HwWifiP2pService.this.logd("target device reaches group limit.");
                join = false;
            } else if (join) {
                int netId = HwWifiP2pService.this.getNetworkId(this.mGroups, dev.deviceAddress, ssid);
                if (netId >= 0) {
                    if (!this.mWifiNative.p2pGroupAdd(netId)) {
                        return -1;
                    }
                    return 0;
                }
            }
            if (join || !dev.isDeviceLimit()) {
                if (!join && tryInvocation && dev.isInvitationCapable()) {
                    int netId2 = -2;
                    if (config.netId < 0) {
                        netId2 = HwWifiP2pService.this.getNetworkId(this.mGroups, dev.deviceAddress);
                    } else if (config.deviceAddress.equals(HwWifiP2pService.this.getOwnerAddr(this.mGroups, config.netId))) {
                        netId2 = config.netId;
                    }
                    if (netId2 < 0) {
                        netId2 = getNetworkIdFromClientList(dev.deviceAddress);
                    }
                    HwWifiP2pService hwWifiP2pService2 = HwWifiP2pService.this;
                    hwWifiP2pService2.logd("netId related with " + dev.deviceAddress + " = " + netId2);
                    if (netId2 >= 0) {
                        if (this.mWifiNative.p2pReinvoke(netId2, dev.deviceAddress)) {
                            this.mSavedPeerConfig.netId = netId2;
                            return 0;
                        }
                        HwWifiP2pService.this.loge("p2pReinvoke() failed, update networks");
                        updatePersistentNetworks(HwWifiP2pService.RELOAD.booleanValue());
                    }
                }
                this.mWifiNative.p2pStopFind();
                p2pBeamConnectWithPinDisplay(config);
                return 0;
            }
            HwWifiP2pService.this.loge("target device reaches the device limit.");
            return -1;
        }

        private void p2pBeamConnectWithPinDisplay(WifiP2pConfig config) {
            WifiP2pDevice dev = this.mPeers.get(config.deviceAddress);
            if (dev == null) {
                HwWifiP2pService.this.loge("target device is not found ");
                return;
            }
            String pin = this.mWifiNative.p2pConnect(config, dev.isGroupOwner());
            try {
                Integer.parseInt(pin);
                notifyInvitationSent(pin, config.deviceAddress);
            } catch (NumberFormatException e) {
            }
        }

        private void sendPeersChangedBroadcast() {
            Intent intent = new Intent("android.net.wifi.p2p.PEERS_CHANGED");
            intent.putExtra("wifiP2pDeviceList", new WifiP2pDeviceList(this.mPeers));
            intent.addFlags(67108864);
            HwWifiP2pService.this.mContext.sendBroadcast(intent, "android.permission.ACCESS_WIFI_STATE");
        }

        /* access modifiers changed from: private */
        public void sendP2pConnectionStateBroadcast(int state) {
            HwWifiP2pService hwWifiP2pService = HwWifiP2pService.this;
            hwWifiP2pService.logd("sending p2p connection state broadcast and state = " + state);
            Intent intent = new Intent("android.net.wifi.p2p.CONNECT_STATE_CHANGE");
            intent.addFlags(603979776);
            intent.putExtra("extraState", state);
            if (this.mSavedPeerConfig == null || state != 2) {
                HwWifiP2pService.this.loge("GroupCreatedState:mSavedConnectConfig is null");
            } else {
                String opposeInterfaceAddressString = this.mSavedPeerConfig.deviceAddress;
                String conDeviceName = null;
                intent.putExtra("interfaceAddress", opposeInterfaceAddressString);
                Iterator<WifiP2pDevice> it = this.mPeers.getDeviceList().iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    WifiP2pDevice d = it.next();
                    if (d.deviceAddress != null && d.deviceAddress.equals(this.mSavedPeerConfig.deviceAddress)) {
                        conDeviceName = d.deviceName;
                        break;
                    }
                }
                intent.putExtra("oppDeviceName", conDeviceName);
                HwWifiP2pService hwWifiP2pService2 = HwWifiP2pService.this;
                hwWifiP2pService2.logd("oppDeviceName = " + conDeviceName);
                HwWifiP2pService hwWifiP2pService3 = HwWifiP2pService.this;
                hwWifiP2pService3.logd("opposeInterfaceAddressString = " + opposeInterfaceAddressString);
            }
            HwWifiP2pService.this.mContext.sendBroadcast(intent, "android.permission.ACCESS_WIFI_STATE");
        }

        public boolean autoAcceptConnection() {
            if (!isP2PValidDevice(this.mSavedPeerConfig.deviceAddress) && !isP2PValidDevice(getDeviceName(this.mSavedPeerConfig.deviceAddress))) {
                return false;
            }
            HwWifiP2pService.this.logd("notifyInvitationReceived is a valid device");
            removeP2PValidDevice(this.mSavedPeerConfig.deviceAddress);
            sendMessage(HwWifiP2pService.wifiP2pServiceUtils.getPeerConnectionUserAccept(HwWifiP2pService.this));
            return true;
        }

        private String getDeviceName(String deviceAddress) {
            WifiP2pDevice d = this.mPeers.get(deviceAddress);
            if (d != null) {
                return d.deviceName;
            }
            return deviceAddress;
        }

        public String p2pBeamConnect(WifiP2pConfig config, boolean joinExistingGroup) {
            if (config == null) {
                return null;
            }
            List<String> args = new ArrayList<>();
            WpsInfo wps = config.wps;
            args.add(config.deviceAddress);
            switch (wps.setup) {
                case 0:
                    args.add("pbc");
                    break;
                case 1:
                    if (TextUtils.isEmpty(wps.pin)) {
                        args.add("pin");
                    } else {
                        args.add(wps.pin);
                    }
                    args.add("display");
                    break;
                case 2:
                    args.add(wps.pin);
                    args.add("keypad");
                    break;
                case 3:
                    args.add(wps.pin);
                    args.add("label");
                    break;
            }
            if (config.netId == -2) {
                args.add("persistent");
            }
            if (joinExistingGroup) {
                args.add("join");
            } else {
                int groupOwnerIntent = config.groupOwnerIntent;
                if (groupOwnerIntent < 0 || groupOwnerIntent > 15) {
                    groupOwnerIntent = 6;
                }
                args.add("go_intent=" + groupOwnerIntent);
            }
            args.add("beam");
            StringBuffer command = new StringBuffer("P2P_CONNECT ");
            for (String s : args) {
                command.append(s);
                command.append(" ");
            }
            return "success";
        }
    }

    private class P2pFindProcessInfo {
        public long mLastP2pFindTimestamp;
        public int mUid;

        public P2pFindProcessInfo(int uid, long p2pFindTimestamp) {
            this.mUid = uid;
            this.mLastP2pFindTimestamp = p2pFindTimestamp;
        }
    }

    public static class SensitiveArg {
        private final Object mArg;

        public SensitiveArg(Object arg) {
            this.mArg = arg;
        }

        public String toString() {
            return String.valueOf(this.mArg);
        }
    }

    private class WifiP2pDataTrafficHandler extends Handler {
        private static final int MSG_UPDATA_DATA_TAFFIC = 0;

        WifiP2pDataTrafficHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                HwWifiP2pService.this.handleUpdataDateTraffic();
            }
        }
    }

    static /* synthetic */ int access$1604(HwWifiP2pService x0) {
        int i = x0.mLinkSpeedPollToken + 1;
        x0.mLinkSpeedPollToken = i;
        return i;
    }

    static {
        boolean z = true;
        if (!Log.HWLog && (!Log.HWModuleLog || !Log.isLoggable(TAG, 3))) {
            z = false;
        }
        HWDBG = z;
    }

    public HwWifiP2pService(Context context) {
        super(context);
        this.mContext = context;
        if (this.mP2pStateMachine instanceof HwP2pStateMachine) {
            this.mHwP2pStateMachine = this.mP2pStateMachine;
        }
        this.wifip2pThread.start();
        this.mWifiP2pDataTrafficHandler = new WifiP2pDataTrafficHandler(this.wifip2pThread.getLooper());
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        this.mDefaultIdleIntent = PendingIntent.getBroadcast(this.mContext, 0, new Intent(ACTION_DEVICE_DELAY_IDLE, null), 0);
        this.mDelayIdleIntent = PendingIntent.getBroadcast(this.mContext, 0, new Intent(ACTION_DEVICE_DELAY_IDLE, null), 0);
        registerForBroadcasts();
        this.mWifiRepeater = new WifiRepeaterController(this.mContext, getP2pStateMachineMessenger());
        this.mHwWifiCHRService = HwWifiCHRServiceImpl.getInstance();
        this.mLinkSpeedWeights = new int[]{15, 20, 30, 35};
        this.mP2pFindProcessInfoList = new ArrayList();
        this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
    }

    private void registerForBroadcasts() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.SCREEN_ON");
        intentFilter.addAction("android.intent.action.USER_PRESENT");
        intentFilter.addAction("android.intent.action.SCREEN_OFF");
        intentFilter.addAction("android.net.wifi.STATE_CHANGE");
        intentFilter.addAction("android.net.wifi.p2p.CONNECTION_STATE_CHANGE");
        this.mContext.registerReceiver(this.mReceiver, intentFilter);
        this.mContext.registerReceiver(this.mAlarmReceiver, new IntentFilter(ACTION_DEVICE_DELAY_IDLE), HUAWEI_WIFI_DEVICE_DELAY_IDLE, null);
    }

    public boolean isWifiRepeaterStarted() {
        return 1 == Settings.Global.getInt(this.mContext.getContentResolver(), "wifi_repeater_on", 0);
    }

    /* access modifiers changed from: private */
    public boolean shouldDisconnectWifiP2p() {
        if (!this.mWifiRepeaterEnabled) {
            return true;
        }
        Slog.i(TAG, "WifiRepeater is open.");
        return false;
    }

    private boolean checkP2pDataTrafficLine() {
        long txBytes;
        WifiP2pGroup mWifiP2pGroup = wifiP2pServiceUtils.getmGroup(this.mP2pStateMachine);
        if (mWifiP2pGroup == null) {
            Slog.d(TAG, "WifiP2pGroup is null.");
            return true;
        }
        this.mInterface = mWifiP2pGroup.getInterface();
        Slog.d(TAG, "mInterface: " + this.mInterface);
        long txBytes2 = TrafficStats.getTxBytes(this.mInterface);
        long rxBytes = TrafficStats.getRxBytes(this.mInterface);
        long txSpeed = txBytes2 - this.mLastTxBytes;
        long rxSpeed = rxBytes - this.mLastRxBytes;
        Slog.d(TAG, " txBytes:" + txBytes2 + " rxBytes:" + rxBytes + " txSpeed:" + txSpeed + " rxSpeed:" + rxSpeed + " mLowDataTrafficLine:" + 102400 + " DELAY_IDLE_MS:" + 60000);
        if (this.mLastTxBytes == 0) {
            long txBytes3 = txBytes2;
            if (this.mLastRxBytes == 0) {
                this.mLastTxBytes = txBytes3;
                this.mLastRxBytes = rxBytes;
                return false;
            }
            txBytes = txBytes3;
        } else {
            txBytes = txBytes2;
        }
        this.mLastTxBytes = txBytes;
        this.mLastRxBytes = rxBytes;
        if (txSpeed + rxSpeed < 102400) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    public void handleUpdataDateTraffic() {
        Slog.d(TAG, "handleUpdataDateTraffic");
        if (!this.mP2pNetworkInfo.isConnected()) {
            Slog.d(TAG, "p2p is disconnected.");
        } else if (checkP2pDataTrafficLine()) {
            Slog.w(TAG, "remove group, disconnect wifi p2p");
            this.mP2pStateMachine.sendMessage(139280);
        } else {
            this.mAlarmManager.setExact(0, System.currentTimeMillis() + 60000, this.mDelayIdleIntent);
        }
    }

    /* access modifiers changed from: protected */
    public Object getHwP2pStateMachine(String name, Looper looper, boolean p2pSupported) {
        return new HwP2pStateMachine(name, looper, p2pSupported);
    }

    /* access modifiers changed from: protected */
    public boolean handleDefaultStateMessage(Message message) {
        if (this.mHwP2pStateMachine != null) {
            return this.mHwP2pStateMachine.handleDefaultStateMessage(message);
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean handleP2pNotSupportedStateMessage(Message message) {
        if (this.mHwP2pStateMachine != null) {
            return this.mHwP2pStateMachine.handleP2pNotSupportedStateMessage(message);
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean handleInactiveStateMessage(Message message) {
        if (this.mHwP2pStateMachine != null) {
            return this.mHwP2pStateMachine.handleInactiveStateMessage(message);
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean handleP2pEnabledStateExMessage(Message message) {
        if (this.mHwP2pStateMachine != null) {
            return this.mHwP2pStateMachine.handleP2pEnabledStateExMessage(message);
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean handleGroupNegotiationStateExMessage(Message message) {
        if (this.mHwP2pStateMachine != null) {
            return this.mHwP2pStateMachine.handleGroupNegotiationStateExMessage(message);
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean handleGroupCreatedStateExMessage(Message message) {
        if (this.mHwP2pStateMachine != null) {
            return this.mHwP2pStateMachine.handleGroupCreatedStateExMessage(message);
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean handleOngoingGroupRemovalStateExMessage(Message message) {
        if (this.mHwP2pStateMachine != null) {
            return this.mHwP2pStateMachine.handleOngoingGroupRemovalStateExMessage(message);
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void sendGroupConfigInfo(WifiP2pGroup mGroup) {
        this.mConfigInfo = mGroup.getNetworkName() + "\n" + mGroup.getOwner().deviceAddress + "\n" + mGroup.getPassphrase() + "\n" + mGroup.getFrequence();
        this.mContext.sendBroadcastAsUser(new Intent("android.net.wifi.p2p.CONFIG_INFO"), UserHandle.ALL, "com.huawei.instantshare.permission.ACCESS_INSTANTSHARE");
    }

    /* access modifiers changed from: private */
    public void sendInterfaceCreatedBroadcast(String ifName) {
        logd("sending interface created broadcast");
        Intent intent = new Intent("android.net.wifi.p2p.INTERFACE_CREATED");
        intent.putExtra("p2pInterfaceName", ifName);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, "com.huawei.instantshare.permission.ACCESS_INSTANTSHARE");
    }

    /* access modifiers changed from: private */
    public void sendNetworkConnectedBroadcast(String bssid) {
        logd("sending network connected broadcast");
        Intent intent = new Intent("android.net.wifi.p2p.NETWORK_CONNECTED_ACTION");
        intent.putExtra("bssid", bssid);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, "com.huawei.instantshare.permission.ACCESS_INSTANTSHARE");
    }

    /* access modifiers changed from: private */
    public void sendNetworkDisconnectedBroadcast(String bssid) {
        logd("sending network disconnected broadcast");
        Intent intent = new Intent("android.net.wifi.p2p.NETWORK_DISCONNECTED_ACTION");
        intent.putExtra("bssid", bssid);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, "com.huawei.instantshare.permission.ACCESS_INSTANTSHARE");
    }

    /* access modifiers changed from: private */
    public void sendLinkSpeedChangedBroadcast() {
        logd("sending linkspeed changed broadcast " + this.mLastLinkSpeed);
        Intent intent = new Intent("com.huawei.net.wifi.p2p.LINK_SPEED");
        intent.putExtra("linkSpeed", this.mLastLinkSpeed);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, "com.huawei.wfd.permission.ACCESS_HW_P2P_WFD");
    }

    /* access modifiers changed from: private */
    public void sendHwP2pDeviceExInfoBroadcast(byte[] info) {
        logd("sending HwP2pDeviceExInfo broadcast ");
        Intent intent = new Intent("com.huawei.net.wifi.p2p.peers.hw.extend.info");
        intent.putExtra("exinfo", info);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, "com.huawei.wfd.permission.ACCESS_HW_P2P_WFD");
    }

    /* access modifiers changed from: protected */
    public void handleTetheringDhcpRange(String[] tetheringDhcpRanges) {
        for (int i = tetheringDhcpRanges.length - 1; i >= 0; i--) {
            if ("192.168.49.2".equals(tetheringDhcpRanges[i])) {
                tetheringDhcpRanges[i] = "192.168.49.101";
                return;
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean handleClientHwMessage(Message message) {
        switch (message.what) {
            case 141264:
            case 141265:
            case 141266:
            case 141267:
            case 141268:
            case 141269:
            case 141270:
            case 141271:
                this.mP2pStateMachine.sendMessage(message);
                return true;
            default:
                Slog.d(TAG, "ClientHandler.handleMessage ignoring msg=" + message);
                return false;
        }
    }

    /* access modifiers changed from: protected */
    public void sendP2pConnectingStateBroadcast() {
        logd(" mHwP2pStateMachine = " + this.mHwP2pStateMachine + " this = " + this);
        this.mHwP2pStateMachine.sendP2pConnectionStateBroadcast(1);
    }

    /* access modifiers changed from: protected */
    public void sendP2pFailStateBroadcast() {
        logd(" mHwP2pStateMachine = " + this.mHwP2pStateMachine + " this = " + this);
        this.mHwP2pStateMachine.sendP2pConnectionStateBroadcast(3);
        if (this.mIsWifiRepeaterTetherStarted && this.mHwWifiCHRService != null) {
            this.mHwWifiCHRService.addRepeaterConnFailedCount(1);
        }
    }

    /* access modifiers changed from: protected */
    public void sendP2pConnectedStateBroadcast() {
        logd(" mHwP2pStateMachine = " + this.mHwP2pStateMachine + " this = " + this);
        this.mHwP2pStateMachine.sendP2pConnectionStateBroadcast(2);
    }

    /* access modifiers changed from: protected */
    public void clearValidDeivceList() {
        this.mValidDeivceList.clear();
    }

    /* access modifiers changed from: protected */
    public boolean autoAcceptConnection() {
        logd(" mHwP2pStateMachine = " + this.mHwP2pStateMachine + " this = " + this);
        return this.mHwP2pStateMachine.autoAcceptConnection();
    }

    /* access modifiers changed from: private */
    public void loge(String string) {
        Slog.e(TAG, string);
    }

    /* access modifiers changed from: private */
    public void logd(String string) {
        Slog.d(TAG, string);
    }

    private ConnectivityManager getConnectivityManager() {
        return (ConnectivityManager) this.mContext.getSystemService("connectivity");
    }

    /* access modifiers changed from: private */
    public boolean isWifiConnected() {
        if (this.mNetworkInfo != null) {
            return this.mNetworkInfo.isConnected();
        }
        return false;
    }

    private int convertFrequencyToChannelNumber(int frequency) {
        if (frequency >= 2412 && frequency <= 2484) {
            return ((frequency - 2412) / 5) + 1;
        }
        if (frequency < 5170 || frequency > 5825) {
            return 0;
        }
        return ((frequency - 5170) / 5) + 34;
    }

    /* access modifiers changed from: protected */
    public boolean startWifiRepeater(WifiP2pGroup group) {
        this.mTetherInterfaceName = group.getInterface();
        if (HWDBG) {
            logd("start wifi repeater, ifaceName=" + this.mTetherInterfaceName + ", mWifiRepeaterEnabled=" + this.mWifiRepeaterEnabled + ", isWifiConnected=" + isWifiConnected());
        }
        this.mWifiRepeaterFreq = group.getFrequence();
        if (isWifiConnected()) {
            int resultCode = getConnectivityManager().tether(this.mTetherInterfaceName);
            if (HWDBG) {
                logd("ConnectivityManager.tether resultCode = " + resultCode);
            }
            if (resultCode == 0) {
                this.mWifiRepeater.handleP2pTethered(group);
                this.mIsWifiRepeaterTetherStarted = true;
                setWifiRepeaterState(1);
                if (this.mHwWifiCHRService != null) {
                    this.mHwWifiCHRService.addWifiRepeaterOpenedCount(1);
                    this.mHwWifiCHRService.setWifiRepeaterStatus(true);
                }
                return true;
            }
        }
        setWifiRepeaterState(5);
        if (this.mHwWifiCHRService != null) {
            this.mHwWifiCHRService.updateRepeaterOpenOrCloseError(HwQoEUtils.QOE_MSG_SCREEN_OFF, 1, "REPEATER_OPEN_OR_CLOSE_FAILED_UNKNOWN");
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public String getWifiRepeaterServerAddress() {
        WifiManager mWM = (WifiManager) this.mContext.getSystemService("wifi");
        int defaultAddress = NetworkUtils.inetAddressToInt((Inet4Address) NetworkUtils.numericToInetAddress(SERVER_ADDRESS_WIFI_BRIDGE));
        if (mWM != null) {
            DhcpInfo dhcpInfo = mWM.getDhcpInfo();
            if (dhcpInfo != null) {
                int gateway = dhcpInfo.gateway;
                if (gateway > 0 && (16777215 & gateway) == (16777215 & defaultAddress)) {
                    if (HWDBG) {
                        logd("getWifiRepeaterServerAddress use SERVER_ADDRESS_WIFI_BRIDGE_OTHER");
                    }
                    return SERVER_ADDRESS_WIFI_BRIDGE_OTHER;
                }
            }
        }
        if (HWDBG) {
            logd("getWifiRepeaterServerAddress use SERVER_ADDRESS_WIFI_BRIDGE");
        }
        return SERVER_ADDRESS_WIFI_BRIDGE;
    }

    public WifiRepeater getWifiRepeater() {
        return this.mWifiRepeater;
    }

    public void notifyRptGroupRemoved() {
        this.mWifiRepeater.handleP2pUntethered();
    }

    public int getWifiRepeaterFreq() {
        return this.mWifiRepeaterFreq;
    }

    public int getWifiRepeaterChannel() {
        return convertFrequencyToChannelNumber(this.mWifiRepeaterFreq);
    }

    public boolean getWifiRepeaterTetherStarted() {
        return this.mIsWifiRepeaterTetherStarted;
    }

    public void handleClientConnect(WifiP2pGroup group) {
        if (this.mIsWifiRepeaterTetherStarted && group != null) {
            if (group.getClientList().size() >= 1) {
                DecisionUtil.bindService(this.mContext);
                Log.d(TAG, "bindService");
            }
            this.mWifiRepeater.handleClientListChanged(group);
            if (0 == this.mWifiRepeaterBeginWorkTime && group.getClientList().size() == 1) {
                this.mWifiRepeaterBeginWorkTime = SystemClock.elapsedRealtime();
            }
            this.mHwWifiCHRService.setRepeaterMaxClientCount(group.getClientList().size() > 0 ? group.getClientList().size() : 0);
        }
    }

    public void handleClientDisconnect(WifiP2pGroup group) {
        if (this.mIsWifiRepeaterTetherStarted) {
            this.mWifiRepeater.handleClientListChanged(group);
            if (group.getClientList().size() == 0 && this.mHwWifiCHRService != null) {
                this.mWifiRepeaterEndWorkTime = SystemClock.elapsedRealtime();
                this.mHwWifiCHRService.setWifiRepeaterWorkingTime((this.mWifiRepeaterEndWorkTime - this.mWifiRepeaterBeginWorkTime) / 1000);
                this.mWifiRepeaterEndWorkTime = 0;
                this.mWifiRepeaterBeginWorkTime = 0;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void stopWifiRepeater(WifiP2pGroup group) {
        setWifiRepeaterState(2);
        this.mWifiRepeaterEnabled = false;
        this.mWifiRepeaterEndWorkTime = SystemClock.elapsedRealtime();
        if (!(group == null || group.getClientList().size() <= 0 || this.mHwWifiCHRService == null)) {
            this.mHwWifiCHRService.setWifiRepeaterWorkingTime((this.mWifiRepeaterEndWorkTime - this.mWifiRepeaterBeginWorkTime) / 1000);
        }
        this.mWifiRepeaterBeginWorkTime = 0;
        this.mWifiRepeaterEndWorkTime = 0;
        this.mWifiRepeaterFreq = 0;
        if (this.mHwWifiCHRService != null) {
            this.mHwWifiCHRService.setWifiRepeaterFreq(this.mWifiRepeaterFreq);
        }
        if (this.mIsWifiRepeaterTetherStarted) {
            int resultCode = getConnectivityManager().untether(this.mTetherInterfaceName);
            if (HWDBG) {
                logd("ConnectivityManager.untether resultCode = " + resultCode);
            }
            if (resultCode == 0) {
                this.mIsWifiRepeaterTetherStarted = false;
                setWifiRepeaterState(0);
                if (this.mHwWifiCHRService != null) {
                    this.mHwWifiCHRService.setWifiRepeaterStatus(false);
                    return;
                }
                return;
            }
            loge("Untether initiate failed!");
            setWifiRepeaterState(4);
            if (this.mHwWifiCHRService != null) {
                this.mHwWifiCHRService.updateRepeaterOpenOrCloseError(HwQoEUtils.QOE_MSG_SCREEN_OFF, 0, "REPEATER_OPEN_OR_CLOSE_FAILED_UNKNOWN");
                return;
            }
            return;
        }
        setWifiRepeaterState(0);
    }

    public void setWifiRepeaterState(int state) {
        if (this.mContext != null) {
            Settings.Global.putInt(this.mContext.getContentResolver(), "wifi_repeater_on", state);
            Intent intent = new Intent("com.huawei.android.net.wifi.p2p.action.WIFI_RPT_STATE_CHANGED");
            intent.putExtra("wifi_rpt_state", state);
            this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
        }
    }

    /* access modifiers changed from: protected */
    public boolean getWifiRepeaterEnabled() {
        return this.mWifiRepeaterEnabled;
    }

    /* access modifiers changed from: protected */
    public void initWifiRepeaterConfig() {
        if (this.mWifiRepeaterConfigChannel == null) {
            this.mWifiRepeaterConfigChannel = new AsyncChannel();
            this.mWifiRepeaterConfigStore = WifiRepeaterConfigStore.makeWifiRepeaterConfigStore(this.mP2pStateMachine.getHandler());
            this.mWifiRepeaterConfigStore.loadRepeaterConfiguration();
            this.mWifiRepeaterConfigChannel.connectSync(this.mContext, this.mP2pStateMachine.getHandler(), this.mWifiRepeaterConfigStore.getMessenger());
        }
    }

    public void setWifiRepeaterConfiguration(WifiConfiguration config) {
        if (this.mWifiRepeaterConfigChannel != null && config != null) {
            this.mWifiRepeaterConfigChannel.sendMessage(CMD_SET_REPEATER_CONFIG, config);
        }
    }

    public WifiConfiguration syncGetWifiRepeaterConfiguration() {
        if (this.mWifiRepeaterConfigChannel == null) {
            return null;
        }
        Message resultMsg = this.mWifiRepeaterConfigChannel.sendMessageSynchronously(CMD_REQUEST_REPEATER_CONFIG);
        WifiConfiguration ret = (WifiConfiguration) resultMsg.obj;
        resultMsg.recycle();
        return ret;
    }

    private void enforceAccessPermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.ACCESS_WIFI_STATE", "WifiService");
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v4, resolved type: android.net.wifi.WifiConfiguration} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v8, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v11, resolved type: java.lang.String} */
    /* JADX WARNING: type inference failed for: r0v0 */
    /* JADX WARNING: type inference failed for: r0v10 */
    /* JADX WARNING: type inference failed for: r0v18 */
    /* JADX WARNING: type inference failed for: r0v19 */
    /* JADX WARNING: Multi-variable type inference failed */
    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        String _arg2;
        String _arg1;
        ? _arg0 = 0;
        switch (code) {
            case CODE_GET_WIFI_REPEATER_CONFIG /*1001*/:
                data.enforceInterface(DESCRIPTOR);
                enforceAccessPermission();
                Slog.d(TAG, "GetWifiRepeaterConfiguration ");
                WifiConfiguration _result = syncGetWifiRepeaterConfiguration();
                reply.writeNoException();
                if (_result != null) {
                    reply.writeInt(1);
                    _result.writeToParcel(reply, 1);
                } else {
                    reply.writeInt(0);
                }
                return true;
            case CODE_SET_WIFI_REPEATER_CONFIG /*1002*/:
                data.enforceInterface(DESCRIPTOR);
                enforceAccessPermission();
                Slog.d(TAG, "setWifiRepeaterConfiguration ");
                if (data.readInt() != 0) {
                    _arg0 = (WifiConfiguration) WifiConfiguration.CREATOR.createFromParcel(data);
                }
                setWifiRepeaterConfiguration(_arg0);
                reply.writeNoException();
                return true;
            case CODE_WIFI_MAGICLINK_CONFIG_IP /*1003*/:
                data.enforceInterface(DESCRIPTOR);
                enforceAccessPermission();
                Slog.d(TAG, "configIPAddr ");
                if (data.readInt() != 0) {
                    String _arg02 = data.readString();
                    String readString = data.readString();
                    _arg1 = _arg02;
                    _arg0 = data.readString();
                    _arg2 = readString;
                } else {
                    _arg1 = null;
                    _arg2 = null;
                }
                configIPAddr(_arg1, _arg2, _arg0);
                reply.writeNoException();
                return true;
            case CODE_WIFI_MAGICLINK_RELEASE_IP /*1004*/:
                data.enforceInterface(DESCRIPTOR);
                enforceAccessPermission();
                Slog.d(TAG, "setWifiRepeaterConfiguration ");
                if (data.readInt() != 0) {
                    _arg0 = data.readString();
                }
                releaseIPAddr(_arg0);
                reply.writeNoException();
                return true;
            case CODE_GET_GROUP_CONFIG_INFO /*1005*/:
                if (!wifiP2pServiceUtils.checkSignMatchOrIsSystemApp(this.mContext)) {
                    Log.e(TAG, "WifiP2pService  CODE_GET_GROUP_CONFIG_INFO  SIGNATURE_NO_MATCH or not systemApp");
                    reply.writeInt(0);
                    reply.writeNoException();
                    return false;
                }
                String temp = this.mConfigInfo;
                this.mConfigInfo = "";
                data.enforceInterface(DESCRIPTOR);
                enforceAccessPermission();
                reply.writeNoException();
                if (temp == null) {
                    reply.writeInt(0);
                    return true;
                }
                reply.writeInt(1);
                reply.writeString(temp);
                return true;
            default:
                return HwWifiP2pService.super.onTransact(code, data, reply, flags);
        }
    }

    /* access modifiers changed from: protected */
    public boolean processMessageForP2pCollision(Message msg, State state) {
        boolean mIsP2pCollision = false;
        if (state instanceof WifiP2pServiceImpl.P2pStateMachine.DefaultState) {
            switch (msg.what) {
                case 139265:
                case 139268:
                case 139271:
                case 139274:
                case 139277:
                case 139315:
                case 139318:
                case 139321:
                    if (this.mWifiRepeaterEnabled) {
                        showUserToastIfP2pCollision();
                        mIsP2pCollision = true;
                        break;
                    }
                    break;
            }
        }
        if (state instanceof WifiP2pServiceImpl.P2pStateMachine.P2pEnabledState) {
            switch (msg.what) {
                case 139265:
                case 139315:
                case 139318:
                case 139329:
                case 139332:
                case 139335:
                    if (this.mWifiRepeaterEnabled) {
                        showUserToastIfP2pCollision();
                        mIsP2pCollision = true;
                        break;
                    }
                    break;
            }
        }
        if (state instanceof WifiP2pServiceImpl.P2pStateMachine.InactiveState) {
            switch (msg.what) {
                case 139265:
                case 139274:
                case 139277:
                case 139329:
                case 139332:
                case 139335:
                    if (this.mWifiRepeaterEnabled) {
                        showUserToastIfP2pCollision();
                        mIsP2pCollision = true;
                        break;
                    }
                    break;
            }
        }
        if (state instanceof WifiP2pServiceImpl.P2pStateMachine.GroupCreatingState) {
            int i = msg.what;
            if ((i == 139265 || i == 139274) && this.mWifiRepeaterEnabled) {
                showUserToastIfP2pCollision();
                mIsP2pCollision = true;
            }
        }
        if (!(state instanceof WifiP2pServiceImpl.P2pStateMachine.GroupCreatedState) || msg.what != 139271 || !this.mWifiRepeaterEnabled) {
            return mIsP2pCollision;
        }
        showUserToastIfP2pCollision();
        return true;
    }

    private void showUserToastIfP2pCollision() {
        Toast.makeText(this.mContext, 33685839, 0).show();
    }

    /* access modifiers changed from: protected */
    public boolean getMagicLinkDeviceFlag() {
        return this.mMagicLinkDeviceFlag;
    }

    /* access modifiers changed from: protected */
    public void setmMagicLinkDeviceFlag(boolean magicLinkDeviceFlag) {
        this.mMagicLinkDeviceFlag = magicLinkDeviceFlag;
        if (!this.mMagicLinkDeviceFlag) {
            this.mLegacyGO = false;
        }
    }

    /* access modifiers changed from: protected */
    public void notifyP2pChannelNumber(int channel) {
        if (channel > 13) {
            channel = 0;
        }
        WifiCommonUtils.notifyDeviceState("WLAN-P2P", String.valueOf(channel), "");
    }

    /* access modifiers changed from: protected */
    public void notifyP2pState(String state) {
        WifiCommonUtils.notifyDeviceState("WLAN-P2P", state, "");
    }

    private String buildPrintableIpAddress(String originIpAddr) {
        if (originIpAddr == null) {
            return null;
        }
        byte[] ipAddrArray = NetworkUtils.numericToInetAddress(originIpAddr).getAddress();
        if (ipAddrArray.length != 4) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int index = 0; index < 3; index++) {
            sb.append(ipAddrArray[index]);
            sb.append(".");
        }
        sb.append("***");
        return sb.toString();
    }

    private boolean configIPAddr(String ifName, String ipAddr, String gateway) {
        Slog.d(TAG, "configIPAddr: " + ifName + " " + buildPrintableIpAddress(ipAddr));
        try {
            this.mNwService.enableIpv6(ifName);
            InterfaceConfiguration ifcg = new InterfaceConfiguration();
            ifcg.setLinkAddress(new LinkAddress(NetworkUtils.numericToInetAddress(ipAddr), 24));
            ifcg.setInterfaceUp();
            this.mNwService.setInterfaceConfig(ifName, ifcg);
            RouteInfo connectedRoute = new RouteInfo(new LinkAddress((Inet4Address) NetworkUtils.numericToInetAddress(ipAddr), 24), null, ifName);
            List<RouteInfo> routes = new ArrayList<>(3);
            routes.add(connectedRoute);
            routes.add(new RouteInfo(null, NetworkUtils.numericToInetAddress(gateway), ifName));
            Log.e(TAG, "add new RouteInfo() gateway:" + buildPrintableIpAddress(gateway) + " iface:" + ifName);
            this.mNwService.addInterfaceToLocalNetwork(ifName, routes);
        } catch (Exception e) {
            Log.i(TAG, "", e);
        }
        Slog.d(TAG, "configIPAddr: " + ifName + " " + buildPrintableIpAddress(ipAddr) + "* ok");
        return true;
    }

    private boolean releaseIPAddr(String ifName) {
        if (ifName == null) {
            return false;
        }
        try {
            this.mNwService.disableIpv6(ifName);
            this.mNwService.clearInterfaceAddresses(ifName);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to clear addresses or disable IPv6" + e);
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public int addScanChannelInTimeout(int channelID, int timeout) {
        int ret = (channelID << 16) + (timeout & 255);
        logd("discover time " + ret);
        return ret;
    }

    /* access modifiers changed from: protected */
    public boolean allowP2pFind(int uid) {
        boolean allow;
        if (Process.isCoreUid(uid)) {
            return true;
        }
        boolean isBlackApp = isInBlacklistForP2pFind(uid);
        if (isScreenOn()) {
            if (isBlackApp) {
                allow = allowP2pFindByTime(uid);
            } else {
                allow = true;
            }
        } else if (isBlackApp) {
            allow = false;
        } else {
            allow = allowP2pFindByTime(uid);
        }
        if (!allow) {
            Log.d(TAG, "p2p find disallowed, uid:" + uid);
        }
        return allow;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x002c, code lost:
        return;
     */
    public synchronized void handleP2pStopFind(int uid) {
        if (this.mP2pFindProcessInfoList != null) {
            if (uid < 0) {
                this.mP2pFindProcessInfoList.clear();
            }
            Iterator<P2pFindProcessInfo> it = this.mP2pFindProcessInfoList.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                P2pFindProcessInfo p2pInfo = it.next();
                if (uid == p2pInfo.mUid) {
                    this.mP2pFindProcessInfoList.remove(p2pInfo);
                    break;
                }
            }
        }
    }

    private boolean isScreenOn() {
        if (this.mPowerManager == null || this.mPowerManager.isScreenOn()) {
            return true;
        }
        return false;
    }

    private boolean isInBlacklistForP2pFind(int uid) {
        if (this.mContext == null) {
            return false;
        }
        PackageManager pkgMgr = this.mContext.getPackageManager();
        if (pkgMgr == null) {
            return false;
        }
        String pkgName = pkgMgr.getNameForUid(uid);
        for (String black : BLACKLIST_P2P_FIND) {
            if (black.equals(pkgName)) {
                Log.d(TAG, "p2p-find blacklist: " + pkgName);
                return true;
            }
        }
        return false;
    }

    private synchronized boolean allowP2pFindByTime(int uid) {
        if (this.mP2pFindProcessInfoList == null) {
            return true;
        }
        long now = System.currentTimeMillis();
        this.mP2pFindProcessInfoList.clear();
        this.mP2pFindProcessInfoList.addAll((List) this.mP2pFindProcessInfoList.stream().filter(new Predicate(now) {
            private final /* synthetic */ long f$0;

            {
                this.f$0 = r1;
            }

            public final boolean test(Object obj) {
                return HwWifiP2pService.lambda$allowP2pFindByTime$0(this.f$0, (HwWifiP2pService.P2pFindProcessInfo) obj);
            }
        }).collect(Collectors.toList()));
        for (P2pFindProcessInfo p2pInfo : this.mP2pFindProcessInfoList) {
            if (uid == p2pInfo.mUid) {
                return false;
            }
        }
        this.mP2pFindProcessInfoList.add(new P2pFindProcessInfo(uid, now));
        return true;
    }

    static /* synthetic */ boolean lambda$allowP2pFindByTime$0(long now, P2pFindProcessInfo P2pFindProcessInfo2) {
        return now - P2pFindProcessInfo2.mLastP2pFindTimestamp <= INTERVAL_DISALLOW_P2P_FIND;
    }
}
