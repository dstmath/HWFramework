package com.android.server.wifi.p2p;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.InterfaceConfiguration;
import android.net.IpPrefix;
import android.net.LinkAddress;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
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
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.util.Slog;
import android.widget.Toast;
import com.android.internal.util.AsyncChannel;
import com.android.internal.util.State;
import com.android.server.wifi.HwCHRWifiCPUUsage;
import com.android.server.wifi.HwWifiCHRStateManager;
import com.android.server.wifi.HwWifiCHRStateManagerImpl;
import com.android.server.wifi.HwWifiServiceFactory;
import com.android.server.wifi.HwWifiStateMachine;
import com.android.server.wifi.WifiNativeUtils;
import com.android.server.wifi.WifiRepeaterConfigStore;
import com.android.server.wifi.p2p.WifiP2pServiceImpl.P2pStateMachine;
import com.android.server.wifi.p2p.WifiP2pServiceImpl.P2pStateMachine.DefaultState;
import com.android.server.wifi.p2p.WifiP2pServiceImpl.P2pStateMachine.GroupCreatedState;
import com.android.server.wifi.p2p.WifiP2pServiceImpl.P2pStateMachine.GroupCreatingState;
import com.android.server.wifi.p2p.WifiP2pServiceImpl.P2pStateMachine.InactiveState;
import com.android.server.wifi.p2p.WifiP2pServiceImpl.P2pStateMachine.P2pEnabledState;
import com.android.server.wifi.wifipro.wifiscangenie.WifiScanGenieController;
import com.android.server.wifi.wifipro.wifiscangenie.WifiScanGenieDataBaseImpl;
import com.huawei.utils.reflect.EasyInvokeFactory;
import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class HwWifiP2pService extends WifiP2pServiceImpl {
    private static final String ACTION_DEVICE_DELAY_IDLE = "com.android.server.wifi.p2p.action.DEVICE_DELAY_IDLE";
    private static final int BASE = 143360;
    public static final int CMD_BATTERY_CHANGED = 143469;
    public static final int CMD_DEVICE_DELAY_IDLE = 143465;
    public static final int CMD_REQUEST_REPEATER_CONFIG = 143463;
    public static final int CMD_RESPONSE_REPEATER_CONFIG = 143464;
    public static final int CMD_SCREEN_OFF = 143467;
    public static final int CMD_SCREEN_ON = 143466;
    public static final int CMD_SET_REPEATER_CONFIG = 143461;
    public static final int CMD_SET_REPEATER_CONFIG_COMPLETED = 143462;
    public static final int CMD_USER_PRESENT = 143468;
    private static final int CODE_GET_WIFI_REPEATER_CONFIG = 1001;
    private static final int CODE_SET_WIFI_REPEATER_CONFIG = 1002;
    private static final int CODE_WIFI_MAGICLINK_CONFIG_IP = 1003;
    private static final int CODE_WIFI_MAGICLINK_RELEASE_IP = 1004;
    private static final int CONNECT_FAILURE = -1;
    private static final int CONNECT_SUCCESS = 0;
    private static final boolean DBG = true;
    private static final long DEFAULT_IDLE_MS = 1800000;
    private static final long DEFAULT_LOW_DATA_TRAFFIC_LINE = 204800;
    private static final long DELAY_IDLE_MS = 120000;
    private static final String DESCRIPTOR = "android.net.wifi.p2p.IWifiP2pManager";
    private static final String HUAWEI_WIFI_DEVICE_DELAY_IDLE = "huawei.android.permission.WIFI_DEVICE_DELAY_IDLE";
    private static final boolean HWDBG;
    private static final boolean HWLOGW_E = true;
    private static final Boolean NO_REINVOCATION;
    private static final int P2P_BAND_2G = 0;
    private static final int P2P_BAND_5G = 1;
    private static final int P2P_CHOOSE_CHANNEL_RANDOM = 0;
    private static final Boolean RELOAD;
    private static final String SERVER_ADDRESS_WIFI_BRIDGE = "192.168.43.1";
    private static final String SERVER_ADDRESS_WIFI_BRIDGE_OTHER = "192.168.50.1";
    private static final String TAG = "HwWifiP2pService";
    private static final Boolean TRY_REINVOCATION;
    private static final int WHITELIST_DURATION_MS = 15000;
    private static final int WIFI_REPEATER_CLOSE = 0;
    private static final int WIFI_REPEATER_CLOSEING = 2;
    private static final int WIFI_REPEATER_CLOSE_FAIL = 4;
    private static final int WIFI_REPEATER_OPEN = 1;
    private static final int WIFI_REPEATER_OPENING = 3;
    private static final int WIFI_REPEATER_OPEN_FAIL = 5;
    private static final int WIFI_REPEATER_OPEN_GO_WITHOUT_THTHER = 6;
    private static Random mRandom;
    private static WifiNativeUtils wifiNativeUtils;
    private static WifiP2pServiceUtils wifiP2pServiceUtils;
    private AlarmManager mAlarmManager;
    private final BroadcastReceiver mAlarmReceiver;
    private Context mContext;
    private PendingIntent mDefaultIdleIntent;
    private PendingIntent mDelayIdleIntent;
    private HwP2pStateMachine mHwP2pStateMachine;
    private String mInterface;
    private boolean mIsWifiRepeaterTetherStarted;
    private long mLastRxBytes;
    private long mLastTxBytes;
    private boolean mMagicLinkDeviceFlag;
    NetworkInfo mNetworkInfo;
    NetworkInfo mP2pNetworkInfo;
    private final BroadcastReceiver mReceiver;
    private String mTetherInterfaceName;
    private List<Pair<String, Long>> mValidDeivceList;
    private HwWifiCHRStateManager mWiFiCHRManager;
    private Handler mWifiP2pDataTrafficHandler;
    private long mWifiRepeaterBeginWorkTime;
    private AsyncChannel mWifiRepeaterConfigChannel;
    private WifiRepeaterConfigStore mWifiRepeaterConfigStore;
    private boolean mWifiRepeaterEnabled;
    private long mWifiRepeaterEndWorkTime;
    private int mWifiRepeaterFreq;
    HandlerThread wifip2pThread;

    class HwP2pStateMachine extends P2pStateMachine {
        private static final int DEFAULT_GROUP_OWNER_INTENT = 6;
        private Message mCreatPskGroupMsg;

        HwP2pStateMachine(String name, Looper looper, boolean p2pSupported) {
            super(HwWifiP2pService.this, name, looper, p2pSupported);
        }

        public boolean handleDefaultStateMessage(Message message) {
            switch (message.what) {
                case HwWifiStateMachine.CMD_STOP_WIFI_REPEATER /*131577*/:
                    if (HwWifiP2pService.this.getWifiRepeaterEnabled()) {
                        sendMessage(139280);
                        break;
                    }
                    break;
                case 141264:
                    String addDeviceAddress = message.getData().getString("avlidDevice");
                    HwWifiP2pService.this.logd("add p2p deivce valid addDeviceAddress = " + addDeviceAddress);
                    addP2PValidDevice(addDeviceAddress);
                    break;
                case 141265:
                    String removeDeviceAddress = message.getData().getString("avlidDevice");
                    HwWifiP2pService.this.logd("remove p2p valid deivce removeDeviceAddress = " + removeDeviceAddress);
                    removeP2PValidDevice(removeDeviceAddress);
                    break;
                case 141266:
                    HwWifiP2pService.this.logd("clear p2p valid deivce");
                    clearP2PValidDevice();
                    break;
                case 141268:
                    HwWifiP2pService.this.showUserToastIfP2pCollision();
                    HwWifiP2pService.wifiP2pServiceUtils.replyToMessage(HwWifiP2pService.this.mP2pStateMachine, message, 139278, HwWifiP2pService.WIFI_REPEATER_CLOSEING);
                    break;
                case 141269:
                    HwWifiP2pService.wifiP2pServiceUtils.replyToMessage(HwWifiP2pService.this.mP2pStateMachine, message, 139272, HwWifiP2pService.WIFI_REPEATER_CLOSEING);
                    break;
                case 141270:
                    HwWifiP2pService.wifiP2pServiceUtils.replyToMessage(HwWifiP2pService.this.mP2pStateMachine, message, 139278, HwWifiP2pService.WIFI_REPEATER_CLOSEING);
                    break;
                case 141271:
                    HwWifiP2pService.wifiP2pServiceUtils.replyToMessage(HwWifiP2pService.this.mP2pStateMachine, message, 139281, HwWifiP2pService.WIFI_REPEATER_CLOSEING);
                    if (HwWifiP2pService.this.getWifiRepeaterEnabled()) {
                        HwWifiP2pService.this.stopWifiRepeater(HwWifiP2pService.wifiP2pServiceUtils.getmGroup(HwWifiP2pService.this.mP2pStateMachine));
                        break;
                    }
                    break;
                case HwWifiP2pService.CMD_DEVICE_DELAY_IDLE /*143465*/:
                    HwWifiP2pService.this.mWifiP2pDataTrafficHandler.sendMessage(Message.obtain(HwWifiP2pService.this.mWifiP2pDataTrafficHandler, HwWifiP2pService.WIFI_REPEATER_CLOSE));
                    break;
                case HwWifiP2pService.CMD_SCREEN_ON /*143466*/:
                    Slog.d(HwWifiP2pService.TAG, "cancel alarm.");
                    HwWifiP2pService.this.mAlarmManager.cancel(HwWifiP2pService.this.mDefaultIdleIntent);
                    HwWifiP2pService.this.mAlarmManager.cancel(HwWifiP2pService.this.mDelayIdleIntent);
                    break;
                case HwWifiP2pService.CMD_SCREEN_OFF /*143467*/:
                    HwWifiP2pService.this.mLastTxBytes = 0;
                    HwWifiP2pService.this.mLastRxBytes = 0;
                    if (HwWifiP2pService.this.shouldDisconnectWifiP2p()) {
                        if (HwWifiP2pService.this.mNetworkInfo.getDetailedState() != DetailedState.CONNECTED || !HwWifiP2pService.this.mP2pNetworkInfo.isConnected()) {
                            Slog.d(HwWifiP2pService.TAG, "wifi or p2p is not connected.");
                            break;
                        }
                        Slog.d(HwWifiP2pService.TAG, "set default idle timer: 1800000 ms");
                        HwWifiP2pService.this.mAlarmManager.set(HwWifiP2pService.WIFI_REPEATER_CLOSE, System.currentTimeMillis() + HwWifiP2pService.DEFAULT_IDLE_MS, HwWifiP2pService.this.mDefaultIdleIntent);
                        break;
                    }
                    break;
                case 147459:
                    HwWifiP2pService.this.sendNetworkConnectedBroadcast(message.obj);
                    break;
                case 147460:
                    HwWifiP2pService.this.sendNetworkDisconnectedBroadcast(message.obj);
                    break;
                default:
                    HwWifiP2pService.this.loge("Unhandled message " + message);
                    return HwWifiP2pService.HWDBG;
            }
            return HwWifiP2pService.HWLOGW_E;
        }

        public boolean handleP2pEnabledStateExMessage(Message message) {
            return HwWifiP2pService.HWDBG;
        }

        public boolean handleOngoingGroupRemovalStateExMessage(Message message) {
            switch (message.what) {
                case 141271:
                    replyToMessage(message, 139282);
                    return HwWifiP2pService.HWLOGW_E;
                default:
                    HwWifiP2pService.this.loge("Unhandled message " + message);
                    return HwWifiP2pService.HWDBG;
            }
        }

        public boolean handleGroupNegotiationStateExMessage(Message message) {
            switch (message.what) {
                case 141271:
                    logd(getName() + " MAGICLINK_REMOVE_GC_GROUP");
                    String p2pInterface = message.obj.getString("iface");
                    logd(getName() + "p2pInterface :" + p2pInterface);
                    if (p2pInterface == null || p2pInterface.equals("")) {
                        HwWifiP2pService.wifiP2pServiceUtils.replyToMessage(HwWifiP2pService.this.mP2pStateMachine, message, 139281, HwWifiP2pService.WIFI_REPEATER_CLOSE);
                        transitionTo(HwWifiP2pService.wifiP2pServiceUtils.getmInactiveState(HwWifiP2pService.this.mP2pStateMachine));
                    } else {
                        logd(getName() + " MAGICLINK_REMOVE_GC_GROUP,p2pInterface !=null,now remove it");
                        if (this.mWifiNative.p2pGroupRemove(p2pInterface)) {
                            replyToMessage(message, 139282);
                            HwWifiP2pService.wifiP2pServiceUtils.sendP2pConnectionChangedBroadcast(HwWifiP2pService.this.mP2pStateMachine);
                        } else {
                            HwWifiP2pService.wifiP2pServiceUtils.handleGroupRemoved(HwWifiP2pService.this.mP2pStateMachine);
                            HwWifiP2pService.wifiP2pServiceUtils.replyToMessage(HwWifiP2pService.this.mP2pStateMachine, message, 139281, HwWifiP2pService.WIFI_REPEATER_CLOSE);
                        }
                        transitionTo(HwWifiP2pService.wifiP2pServiceUtils.getmInactiveState(HwWifiP2pService.this.mP2pStateMachine));
                    }
                    return HwWifiP2pService.HWLOGW_E;
                default:
                    HwWifiP2pService.this.loge("Unhandled message " + message);
                    return HwWifiP2pService.HWDBG;
            }
        }

        public boolean handleGroupCreatedStateExMessage(Message message) {
            switch (message.what) {
                case 141271:
                    logd(getName() + " MAGICLINK_REMOVE_GC_GROUP");
                    HwWifiP2pService.wifiP2pServiceUtils.enableBTCoex(HwWifiP2pService.this.mP2pStateMachine);
                    if (this.mWifiNative.p2pGroupRemove(HwWifiP2pService.wifiP2pServiceUtils.getmGroup(HwWifiP2pService.this.mP2pStateMachine).getInterface())) {
                        transitionTo(HwWifiP2pService.wifiP2pServiceUtils.getmOngoingGroupRemovalState(HwWifiP2pService.this.mP2pStateMachine));
                        replyToMessage(message, 139282);
                    } else {
                        HwWifiP2pService.wifiP2pServiceUtils.handleGroupRemoved(HwWifiP2pService.this.mP2pStateMachine);
                        transitionTo(HwWifiP2pService.wifiP2pServiceUtils.getmInactiveState(HwWifiP2pService.this.mP2pStateMachine));
                        HwWifiP2pService.wifiP2pServiceUtils.replyToMessage(HwWifiP2pService.this.mP2pStateMachine, message, 139281, HwWifiP2pService.WIFI_REPEATER_CLOSE);
                    }
                    if (HwWifiP2pService.this.getWifiRepeaterEnabled()) {
                        HwWifiP2pService.this.stopWifiRepeater(HwWifiP2pService.wifiP2pServiceUtils.getmGroup(HwWifiP2pService.this.mP2pStateMachine));
                    }
                    return HwWifiP2pService.HWLOGW_E;
                default:
                    HwWifiP2pService.this.loge("Unhandled message when=" + message.getWhen() + " what=" + message.what + " arg1=" + message.arg1 + " arg2=" + message.arg2);
                    return HwWifiP2pService.HWDBG;
            }
        }

        public boolean handleP2pNotSupportedStateMessage(Message message) {
            switch (message.what) {
                case 141268:
                    HwWifiP2pService.wifiP2pServiceUtils.replyToMessage(HwWifiP2pService.this.mP2pStateMachine, message, 139278, HwWifiP2pService.WIFI_REPEATER_OPEN);
                    return HwWifiP2pService.HWLOGW_E;
                default:
                    HwWifiP2pService.this.loge("Unhandled message " + message);
                    return HwWifiP2pService.HWDBG;
            }
        }

        public boolean handleInactiveStateMessage(Message message) {
            switch (message.what) {
                case 141267:
                    WifiP2pConfig beam_config = message.obj;
                    HwWifiP2pService.wifiP2pServiceUtils.setAutonomousGroup(HwWifiP2pService.this, HwWifiP2pService.HWDBG);
                    int beam_gc = this.mWifiNative.getGroupCapability(beam_config.deviceAddress);
                    HwWifiP2pService.this.updateGroupCapability(this.mPeers, beam_config.deviceAddress, beam_gc);
                    if (beam_connect(beam_config, HwWifiP2pService.TRY_REINVOCATION.booleanValue()) != HwWifiP2pService.CONNECT_FAILURE) {
                        HwWifiP2pService.this.updateStatus(this.mPeers, this.mSavedPeerConfig.deviceAddress, HwWifiP2pService.WIFI_REPEATER_OPEN);
                        sendPeersChangedBroadcast();
                        replyToMessage(message, 139273);
                        transitionTo(this.mGroupNegotiationState);
                        break;
                    }
                    replyToMessage(message, 139272);
                    break;
                case 141268:
                    Global.putInt(HwWifiP2pService.this.mContext.getContentResolver(), "wifi_repeater_on", HwWifiP2pService.WIFI_REPEATER_OPENING);
                    HwWifiP2pService.wifiP2pServiceUtils.setAutonomousGroup(HwWifiP2pService.this, HwWifiP2pService.HWLOGW_E);
                    if (HwWifiP2pService.this.mWifiRepeaterConfigChannel != null) {
                        this.mCreatPskGroupMsg = message;
                        WifiConfiguration userconfig = message.obj;
                        if (userconfig != null) {
                            HwWifiP2pService.this.mWifiRepeaterConfigChannel.sendMessage(HwWifiP2pService.CMD_SET_REPEATER_CONFIG, userconfig);
                            creatGroupForRepeater(userconfig);
                            break;
                        }
                        HwWifiP2pService.this.mWifiRepeaterConfigChannel.sendMessage(HwWifiP2pService.CMD_REQUEST_REPEATER_CONFIG);
                        break;
                    }
                    break;
                case 141269:
                    String str = "\n";
                    String[] tokens = ((Bundle) message.obj).getString("cfg").split(r19);
                    String p2pConfigInfo = "P\"" + tokens[HwWifiP2pService.WIFI_REPEATER_CLOSE] + "\"" + "\n" + tokens[HwWifiP2pService.WIFI_REPEATER_OPEN] + "\n" + "\"" + tokens[HwWifiP2pService.WIFI_REPEATER_CLOSEING] + "\"" + "\n" + tokens[HwWifiP2pService.WIFI_REPEATER_OPENING];
                    this.mWifiNative.magiclinkConnect(p2pConfigInfo);
                    break;
                case 141270:
                    boolean mret;
                    HwWifiP2pService.wifiP2pServiceUtils.setAutonomousGroup(HwWifiP2pService.this, HwWifiP2pService.HWLOGW_E);
                    int mnetId = message.arg1;
                    String freq = message.obj.getString("freq");
                    if (mnetId == -2) {
                        mnetId = this.mGroups.getNetworkId(HwWifiP2pService.wifiP2pServiceUtils.getmThisDevice(HwWifiP2pService.this).deviceAddress);
                        if (mnetId != HwWifiP2pService.CONNECT_FAILURE) {
                            mret = this.mWifiNative.magiclinkGroupAdd(mnetId, freq);
                        } else {
                            mret = this.mWifiNative.magiclinkGroupAdd(HwWifiP2pService.HWLOGW_E, freq);
                        }
                    } else {
                        mret = this.mWifiNative.magiclinkGroupAdd(HwWifiP2pService.HWDBG, freq);
                    }
                    if (!mret) {
                        HwWifiP2pService.wifiP2pServiceUtils.replyToMessage(HwWifiP2pService.this.mP2pStateMachine, message, 139278, HwWifiP2pService.WIFI_REPEATER_CLOSE);
                        break;
                    }
                    replyToMessage(message, 139279);
                    transitionTo(this.mGroupNegotiationState);
                    break;
                case HwWifiP2pService.CMD_RESPONSE_REPEATER_CONFIG /*143464*/:
                    WifiConfiguration config = message.obj;
                    if (config == null) {
                        HwWifiP2pService.this.loge("wifi repeater config is null!");
                        break;
                    }
                    creatGroupForRepeater(config);
                    break;
                case 147557:
                    String ifName = message.obj;
                    HwWifiP2pService.this.sendInterfaceCreatedBroadcast(ifName);
                    HwWifiP2pService.this.mMagicLinkDeviceFlag = HwWifiP2pService.HWLOGW_E;
                    transitionTo(this.mGroupNegotiationState);
                    break;
                default:
                    HwWifiP2pService hwWifiP2pService = HwWifiP2pService.this;
                    StringBuilder append = new StringBuilder().append("Unhandled message when=");
                    long when = message.getWhen();
                    int i = message.what;
                    i = message.arg1;
                    hwWifiP2pService.loge(append.append(r22).append(" what=").append(r0).append(" arg1=").append(r0).append(" arg2=").append(message.arg2).toString());
                    return HwWifiP2pService.HWDBG;
            }
            return HwWifiP2pService.HWLOGW_E;
        }

        private void creatGroupForRepeater(WifiConfiguration config) {
            HwWifiP2pService.this.mWifiRepeaterEnabled = HwWifiP2pService.HWLOGW_E;
            if (config.apChannel == 0) {
                if (HwWifiP2pService.WIFI_REPEATER_OPEN == config.apBand) {
                    int[] channel = this.mWifiNative.getChannelsForBand(HwWifiP2pService.WIFI_REPEATER_CLOSEING);
                    if (channel == null || channel.length <= 0) {
                        HwWifiP2pService.this.loge("wifirpt: SoftAp do not get available 5G channel list, so we choose 2.4G");
                        config.apChannel = DEFAULT_GROUP_OWNER_INTENT;
                        config.apBand = HwWifiP2pService.WIFI_REPEATER_CLOSE;
                    } else {
                        config.apChannel = channel[HwWifiP2pService.mRandom.nextInt(channel.length)];
                        config.apChannel = HwWifiP2pService.this.convertFrequencyToChannelNumber(config.apChannel);
                    }
                }
                if (config.apBand == 0) {
                    config.apChannel = DEFAULT_GROUP_OWNER_INTENT;
                }
            }
            if (HwWifiP2pService.this.isWifiConnected()) {
                if (!HwWifiP2pService.wifiNativeUtils.doBooleanCommand(this.mWifiNative, "P2P_GROUP_ADD " + new StringBuilder("WifiRepeater=y").append("\nssid=").append(config.SSID).append("\npsk=").append(new SensitiveArg(config.preSharedKey)).append("\nchannel=").append(config.apChannel).append("\nband=").append(config.apBand))) {
                    HwWifiP2pService.this.mWifiRepeaterEnabled = HwWifiP2pService.HWDBG;
                }
            } else {
                HwWifiP2pService.this.mWifiRepeaterEnabled = HwWifiP2pService.HWDBG;
                HwWifiP2pService.this.loge("wifirpt: isWifiConnected = false");
            }
            if (HwWifiP2pService.this.mWifiRepeaterEnabled) {
                Global.putInt(HwWifiP2pService.this.mContext.getContentResolver(), "wifi_repeater_on", DEFAULT_GROUP_OWNER_INTENT);
                replyToMessage(this.mCreatPskGroupMsg, 139279);
                transitionTo(this.mGroupNegotiationState);
                if (HwWifiP2pService.HWDBG) {
                    HwWifiP2pService.this.logd("wifirpt: CREATE_GROUP_PSK SUCCEEDED, now transitionTo GroupNegotiationState");
                    return;
                }
                return;
            }
            HwWifiP2pService.wifiP2pServiceUtils.replyToMessage(HwWifiP2pService.this.mP2pStateMachine, this.mCreatPskGroupMsg, 139278, HwWifiP2pService.WIFI_REPEATER_CLOSE);
            Global.putInt(HwWifiP2pService.this.mContext.getContentResolver(), "wifi_repeater_on", HwWifiP2pService.WIFI_REPEATER_OPEN_FAIL);
            HwWifiP2pService.this.loge("wifirpt: CREATE_GROUP_PSK FAILED, remain at this state.");
        }

        private synchronized void addP2PValidDevice(String deviceAddress) {
            if (deviceAddress != null) {
                Iterator<Pair<String, Long>> iter = HwWifiP2pService.this.mValidDeivceList.iterator();
                while (iter.hasNext()) {
                    if (((String) ((Pair) iter.next()).first).equals(deviceAddress)) {
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
                    if (((String) ((Pair) iter.next()).first).equals(deviceAddress)) {
                        iter.remove();
                    }
                }
            }
        }

        private void cleanupValidDevicelist() {
            long curTime = SystemClock.elapsedRealtime();
            Iterator<Pair<String, Long>> iter = HwWifiP2pService.this.mValidDeivceList.iterator();
            while (iter.hasNext()) {
                if (curTime - ((Long) ((Pair) iter.next()).second).longValue() > 15000) {
                    iter.remove();
                }
            }
        }

        private synchronized boolean isP2PValidDevice(String deviceAddress) {
            cleanupValidDevicelist();
            for (Pair<String, Long> entry : HwWifiP2pService.this.mValidDeivceList) {
                if (((String) entry.first).equals(deviceAddress)) {
                    return HwWifiP2pService.HWLOGW_E;
                }
            }
            return HwWifiP2pService.HWDBG;
        }

        private synchronized void clearP2PValidDevice() {
            HwWifiP2pService.this.mValidDeivceList.clear();
        }

        private int beam_connect(WifiP2pConfig config, boolean tryInvocation) {
            if (config == null) {
                HwWifiP2pService.this.loge("config is null");
                return HwWifiP2pService.CONNECT_FAILURE;
            }
            this.mSavedPeerConfig = config;
            WifiP2pDevice dev = this.mPeers.get(config.deviceAddress);
            if (dev == null) {
                HwWifiP2pService.this.loge("target device not found ");
                return HwWifiP2pService.CONNECT_FAILURE;
            }
            int netId;
            boolean join = dev.isGroupOwner();
            String ssid = this.mWifiNative.p2pGetSsid(dev.deviceAddress);
            HwWifiP2pService.this.logd("target ssid is " + ssid + " join:" + join);
            if (join && dev.isGroupLimit()) {
                HwWifiP2pService.this.logd("target device reaches group limit.");
                join = HwWifiP2pService.HWDBG;
            } else if (join) {
                netId = HwWifiP2pService.this.getNetworkId(this.mGroups, dev.deviceAddress, ssid);
                if (netId >= 0) {
                    return !this.mWifiNative.p2pGroupAdd(netId) ? HwWifiP2pService.CONNECT_FAILURE : HwWifiP2pService.WIFI_REPEATER_CLOSE;
                }
            }
            if (join || !dev.isDeviceLimit()) {
                if (!join && tryInvocation && dev.isInvitationCapable()) {
                    netId = -2;
                    if (config.netId < 0) {
                        netId = HwWifiP2pService.this.getNetworkId(this.mGroups, dev.deviceAddress);
                    } else if (config.deviceAddress.equals(HwWifiP2pService.this.getOwnerAddr(this.mGroups, config.netId))) {
                        netId = config.netId;
                    }
                    if (netId < 0) {
                        netId = getNetworkIdFromClientList(dev.deviceAddress);
                    }
                    HwWifiP2pService.this.logd("netId related with " + dev.deviceAddress + " = " + netId);
                    if (netId >= 0) {
                        if (this.mWifiNative.p2pReinvoke(netId, dev.deviceAddress)) {
                            this.mSavedPeerConfig.netId = netId;
                            return HwWifiP2pService.WIFI_REPEATER_CLOSE;
                        }
                        HwWifiP2pService.this.loge("p2pReinvoke() failed, update networks");
                        updatePersistentNetworks(HwWifiP2pService.RELOAD.booleanValue());
                    }
                }
                this.mWifiNative.p2pStopFind();
                p2pBeamConnectWithPinDisplay(config);
                return HwWifiP2pService.WIFI_REPEATER_CLOSE;
            }
            HwWifiP2pService.this.loge("target device reaches the device limit.");
            return HwWifiP2pService.CONNECT_FAILURE;
        }

        private void p2pBeamConnectWithPinDisplay(WifiP2pConfig config) {
            WifiP2pDevice dev = this.mPeers.get(config.deviceAddress);
            if (dev == null) {
                HwWifiP2pService.this.loge("target device is not found ");
                return;
            }
            String pin = p2pBeamConnect(config, dev.isGroupOwner());
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

        private void sendP2pConnectionStateBroadcast(int state) {
            HwWifiP2pService.this.logd("sending p2p connection state broadcast and state = " + state);
            Intent intent = new Intent("android.net.wifi.p2p.CONNECT_STATE_CHANGE");
            intent.addFlags(603979776);
            intent.putExtra("extraState", state);
            if (this.mSavedPeerConfig == null || state != HwWifiP2pService.WIFI_REPEATER_CLOSEING) {
                HwWifiP2pService.this.loge("GroupCreatedState:mSavedConnectConfig is null");
            } else {
                String opposeInterfaceAddressString = this.mSavedPeerConfig.deviceAddress;
                String conDeviceName = null;
                intent.putExtra("interfaceAddress", opposeInterfaceAddressString);
                for (WifiP2pDevice d : this.mPeers.getDeviceList()) {
                    if (d.deviceAddress != null && d.deviceAddress.equals(this.mSavedPeerConfig.deviceAddress)) {
                        conDeviceName = d.deviceName;
                        break;
                    }
                }
                intent.putExtra("oppDeviceName", conDeviceName);
                HwWifiP2pService.this.logd("oppDeviceName = " + conDeviceName);
                HwWifiP2pService.this.logd("opposeInterfaceAddressString = " + opposeInterfaceAddressString);
            }
            HwWifiP2pService.this.mContext.sendBroadcast(intent, "android.permission.ACCESS_WIFI_STATE");
        }

        public boolean autoAcceptConnection() {
            if (!isP2PValidDevice(this.mSavedPeerConfig.deviceAddress) && !isP2PValidDevice(getDeviceName(this.mSavedPeerConfig.deviceAddress))) {
                return HwWifiP2pService.HWDBG;
            }
            HwWifiP2pService.this.logd("notifyInvitationReceived is a valid device");
            removeP2PValidDevice(this.mSavedPeerConfig.deviceAddress);
            sendMessage(HwWifiP2pService.wifiP2pServiceUtils.getPeerConnectionUserAccept(HwWifiP2pService.this));
            return HwWifiP2pService.HWLOGW_E;
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
            List<String> args = new ArrayList();
            WpsInfo wps = config.wps;
            args.add(config.deviceAddress);
            switch (wps.setup) {
                case HwWifiP2pService.WIFI_REPEATER_CLOSE /*0*/:
                    args.add("pbc");
                    break;
                case HwWifiP2pService.WIFI_REPEATER_OPEN /*1*/:
                    if (TextUtils.isEmpty(wps.pin)) {
                        args.add("pin");
                    } else {
                        args.add(wps.pin);
                    }
                    args.add("display");
                    break;
                case HwWifiP2pService.WIFI_REPEATER_CLOSEING /*2*/:
                    args.add(wps.pin);
                    args.add("keypad");
                    break;
                case HwWifiP2pService.WIFI_REPEATER_OPENING /*3*/:
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
                    groupOwnerIntent = DEFAULT_GROUP_OWNER_INTENT;
                }
                args.add("go_intent=" + groupOwnerIntent);
            }
            args.add("beam");
            StringBuffer command = new StringBuffer("P2P_CONNECT ");
            for (String s : args) {
                command.append(s).append(HwCHRWifiCPUUsage.COL_SEP);
            }
            return HwWifiP2pService.wifiNativeUtils.doStringCommand(this.mWifiNative, command.toString());
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
            switch (msg.what) {
                case HwWifiP2pService.WIFI_REPEATER_CLOSE /*0*/:
                    HwWifiP2pService.this.handleUpdataDateTraffic();
                default:
            }
        }
    }

    static {
        RELOAD = Boolean.valueOf(HWLOGW_E);
        TRY_REINVOCATION = Boolean.valueOf(HWLOGW_E);
        NO_REINVOCATION = Boolean.valueOf(HWDBG);
        wifiP2pServiceUtils = (WifiP2pServiceUtils) EasyInvokeFactory.getInvokeUtils(WifiP2pServiceUtils.class);
        wifiNativeUtils = (WifiNativeUtils) EasyInvokeFactory.getInvokeUtils(WifiNativeUtils.class);
        boolean isLoggable = !Log.HWLog ? Log.HWModuleLog ? Log.isLoggable(TAG, WIFI_REPEATER_OPENING) : HWDBG : HWLOGW_E;
        HWDBG = isLoggable;
        mRandom = new Random(Calendar.getInstance().getTimeInMillis());
    }

    public HwWifiP2pService(Context context) {
        super(context);
        this.mValidDeivceList = new ArrayList();
        this.mHwP2pStateMachine = null;
        this.mWifiRepeaterEnabled = HWDBG;
        this.mIsWifiRepeaterTetherStarted = HWDBG;
        this.mNetworkInfo = new NetworkInfo(WIFI_REPEATER_OPEN, WIFI_REPEATER_CLOSE, "WIFI", "");
        this.mP2pNetworkInfo = new NetworkInfo(13, WIFI_REPEATER_CLOSE, "WIFI_P2P", "");
        this.mInterface = "";
        this.mLastTxBytes = 0;
        this.mLastRxBytes = 0;
        this.wifip2pThread = new HandlerThread("WifiP2pService");
        this.mWifiRepeaterFreq = WIFI_REPEATER_CLOSE;
        this.mWifiRepeaterBeginWorkTime = 0;
        this.mWifiRepeaterEndWorkTime = 0;
        this.mWiFiCHRManager = null;
        this.mReceiver = new BroadcastReceiver() {
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
        this.mAlarmReceiver = new BroadcastReceiver() {
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
        this.mMagicLinkDeviceFlag = HWDBG;
        this.mContext = context;
        if (this.mP2pStateMachine instanceof HwP2pStateMachine) {
            this.mHwP2pStateMachine = (HwP2pStateMachine) this.mP2pStateMachine;
        }
        this.wifip2pThread.start();
        this.mWifiP2pDataTrafficHandler = new WifiP2pDataTrafficHandler(this.wifip2pThread.getLooper());
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        this.mDefaultIdleIntent = PendingIntent.getBroadcast(this.mContext, WIFI_REPEATER_CLOSE, new Intent(ACTION_DEVICE_DELAY_IDLE, null), WIFI_REPEATER_CLOSE);
        this.mDelayIdleIntent = PendingIntent.getBroadcast(this.mContext, WIFI_REPEATER_CLOSE, new Intent(ACTION_DEVICE_DELAY_IDLE, null), WIFI_REPEATER_CLOSE);
        registerForBroadcasts();
        this.mWiFiCHRManager = HwWifiServiceFactory.getHwWifiCHRStateManager();
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
        return WIFI_REPEATER_OPEN == Global.getInt(this.mContext.getContentResolver(), "wifi_repeater_on", WIFI_REPEATER_CLOSE) ? HWLOGW_E : HWDBG;
    }

    private boolean shouldDisconnectWifiP2p() {
        if (this.mWifiRepeaterEnabled) {
            Slog.i(TAG, "WifiRepeater is open.");
            return HWDBG;
        }
        int SleepPolicy = Global.getInt(this.mContext.getContentResolver(), "wifi_sleep_policy", WIFI_REPEATER_CLOSEING);
        Slog.d(TAG, "SleepPolicy:" + SleepPolicy);
        if (SleepPolicy == WIFI_REPEATER_CLOSEING) {
            return HWLOGW_E;
        }
        return HWDBG;
    }

    private boolean checkP2pDataTrafficLine() {
        this.mInterface = wifiP2pServiceUtils.getmGroup(this.mP2pStateMachine).getInterface();
        Slog.d(TAG, "mInterface: " + this.mInterface);
        long txBytes = TrafficStats.getTxBytes(this.mInterface);
        long rxBytes = TrafficStats.getRxBytes(this.mInterface);
        long txSpeed = txBytes - this.mLastTxBytes;
        long rxSpeed = rxBytes - this.mLastRxBytes;
        Slog.d(TAG, " txBytes:" + txBytes + " rxBytes:" + rxBytes + " txSpeed:" + txSpeed + " rxSpeed:" + rxSpeed + " mLowDataTrafficLine:" + DEFAULT_LOW_DATA_TRAFFIC_LINE + " DELAY_IDLE_MS:" + DELAY_IDLE_MS);
        if (this.mLastTxBytes == 0 && this.mLastRxBytes == 0) {
            this.mLastTxBytes = txBytes;
            this.mLastRxBytes = rxBytes;
            return HWDBG;
        }
        this.mLastTxBytes = txBytes;
        this.mLastRxBytes = rxBytes;
        if (txSpeed + rxSpeed < DEFAULT_LOW_DATA_TRAFFIC_LINE) {
            return HWLOGW_E;
        }
        return HWDBG;
    }

    private void handleUpdataDateTraffic() {
        Slog.d(TAG, "handleUpdataDateTraffic");
        if (!this.mP2pNetworkInfo.isConnected()) {
            Slog.d(TAG, "p2p is disconnected.");
        } else if (checkP2pDataTrafficLine()) {
            Slog.w(TAG, "remove group, disconnect wifi p2p");
            this.mP2pStateMachine.sendMessage(139280);
        } else {
            this.mAlarmManager.set(WIFI_REPEATER_CLOSE, System.currentTimeMillis() + DELAY_IDLE_MS, this.mDelayIdleIntent);
        }
    }

    protected Object getHwP2pStateMachine(String name, Looper looper, boolean p2pSupported) {
        return new HwP2pStateMachine(name, looper, p2pSupported);
    }

    protected boolean handleDefaultStateMessage(Message message) {
        if (this.mHwP2pStateMachine != null) {
            return this.mHwP2pStateMachine.handleDefaultStateMessage(message);
        }
        return HWDBG;
    }

    protected boolean handleP2pNotSupportedStateMessage(Message message) {
        if (this.mHwP2pStateMachine != null) {
            return this.mHwP2pStateMachine.handleP2pNotSupportedStateMessage(message);
        }
        return HWDBG;
    }

    protected boolean handleInactiveStateMessage(Message message) {
        if (this.mHwP2pStateMachine != null) {
            return this.mHwP2pStateMachine.handleInactiveStateMessage(message);
        }
        return HWDBG;
    }

    protected boolean handleP2pEnabledStateExMessage(Message message) {
        if (this.mHwP2pStateMachine != null) {
            return this.mHwP2pStateMachine.handleP2pEnabledStateExMessage(message);
        }
        return HWDBG;
    }

    protected boolean handleGroupNegotiationStateExMessage(Message message) {
        if (this.mHwP2pStateMachine != null) {
            return this.mHwP2pStateMachine.handleGroupNegotiationStateExMessage(message);
        }
        return HWDBG;
    }

    protected boolean handleGroupCreatedStateExMessage(Message message) {
        if (this.mHwP2pStateMachine != null) {
            return this.mHwP2pStateMachine.handleGroupCreatedStateExMessage(message);
        }
        return HWDBG;
    }

    protected boolean handleOngoingGroupRemovalStateExMessage(Message message) {
        if (this.mHwP2pStateMachine != null) {
            return this.mHwP2pStateMachine.handleOngoingGroupRemovalStateExMessage(message);
        }
        return HWDBG;
    }

    protected void sendGroupConfigInfo(WifiP2pGroup mGroup) {
        String p2pConfigInfo = mGroup.getNetworkName() + "\n" + mGroup.getOwner().deviceAddress + "\n" + mGroup.getPassphrase() + "\n" + mGroup.getFrequence();
        Intent intent = new Intent("android.net.wifi.p2p.CONFIG_INFO");
        intent.putExtra("p2pconfigInfo", p2pConfigInfo);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, "com.huawei.instantshare.permission.ACCESS_INSTANTSHARE");
    }

    private void sendInterfaceCreatedBroadcast(String ifName) {
        logd("sending interface created broadcast");
        Intent intent = new Intent("android.net.wifi.p2p.INTERFACE_CREATED");
        intent.putExtra("p2pInterfaceName", ifName);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, "com.huawei.instantshare.permission.ACCESS_INSTANTSHARE");
    }

    private void sendNetworkConnectedBroadcast(String bssid) {
        logd("sending network connected broadcast");
        Intent intent = new Intent("android.net.wifi.p2p.NETWORK_CONNECTED_ACTION");
        intent.putExtra(WifiScanGenieDataBaseImpl.CHANNEL_TABLE_BSSID, bssid);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, "com.huawei.instantshare.permission.ACCESS_INSTANTSHARE");
    }

    private void sendNetworkDisconnectedBroadcast(String bssid) {
        logd("sending network disconnected broadcast");
        Intent intent = new Intent("android.net.wifi.p2p.NETWORK_DISCONNECTED_ACTION");
        intent.putExtra(WifiScanGenieDataBaseImpl.CHANNEL_TABLE_BSSID, bssid);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, "com.huawei.instantshare.permission.ACCESS_INSTANTSHARE");
    }

    protected void handleTetheringDhcpRange(String[] tetheringDhcpRanges) {
        for (int i = tetheringDhcpRanges.length + CONNECT_FAILURE; i >= 0; i += CONNECT_FAILURE) {
            if ("192.168.49.2".equals(tetheringDhcpRanges[i])) {
                tetheringDhcpRanges[i] = "192.168.49.101";
                return;
            }
        }
    }

    protected boolean handleClientHwMessage(Message message) {
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
                return HWLOGW_E;
            default:
                Slog.d(TAG, "ClientHandler.handleMessage ignoring msg=" + message);
                return HWDBG;
        }
    }

    protected void sendP2pConnectingStateBroadcast() {
        logd(" mHwP2pStateMachine = " + this.mHwP2pStateMachine + " this = " + this);
        this.mHwP2pStateMachine.sendP2pConnectionStateBroadcast(WIFI_REPEATER_OPEN);
    }

    protected void sendP2pFailStateBroadcast() {
        logd(" mHwP2pStateMachine = " + this.mHwP2pStateMachine + " this = " + this);
        this.mHwP2pStateMachine.sendP2pConnectionStateBroadcast(WIFI_REPEATER_OPENING);
        if (this.mIsWifiRepeaterTetherStarted && this.mWiFiCHRManager != null && (this.mWiFiCHRManager instanceof HwWifiCHRStateManagerImpl)) {
            this.mWiFiCHRManager.addRepeaterConnFailedCount(WIFI_REPEATER_OPEN);
        }
    }

    protected void sendP2pConnectedStateBroadcast() {
        logd(" mHwP2pStateMachine = " + this.mHwP2pStateMachine + " this = " + this);
        this.mHwP2pStateMachine.sendP2pConnectionStateBroadcast(WIFI_REPEATER_CLOSEING);
    }

    protected void clearValidDeivceList() {
        this.mValidDeivceList.clear();
    }

    protected boolean autoAcceptConnection() {
        logd(" mHwP2pStateMachine = " + this.mHwP2pStateMachine + " this = " + this);
        return this.mHwP2pStateMachine.autoAcceptConnection();
    }

    private void loge(String string) {
        Slog.e(TAG, string);
    }

    private void logd(String string) {
        Slog.d(TAG, string);
    }

    private ConnectivityManager getConnectivityManager() {
        return (ConnectivityManager) this.mContext.getSystemService("connectivity");
    }

    private boolean isWifiConnected() {
        NetworkInfo info = getConnectivityManager().getNetworkInfo(WIFI_REPEATER_OPEN);
        if (info != null) {
            return info.isConnected();
        }
        return HWDBG;
    }

    private int convertFrequencyToChannelNumber(int frequency) {
        if (frequency >= WifiScanGenieController.CHANNEL_1_FREQ && frequency <= 2484) {
            return ((frequency - 2412) / WIFI_REPEATER_OPEN_FAIL) + WIFI_REPEATER_OPEN;
        }
        if (frequency < 5170 || frequency > 5825) {
            return WIFI_REPEATER_CLOSE;
        }
        return ((frequency - 5170) / WIFI_REPEATER_OPEN_FAIL) + 34;
    }

    protected boolean startWifiRepeater(WifiP2pGroup group) {
        this.mTetherInterfaceName = group.getInterface();
        if (HWDBG) {
            logd("start wifi repeater, ifaceName=" + this.mTetherInterfaceName + ", mWifiRepeaterEnabled=" + this.mWifiRepeaterEnabled + ", isWifiConnected=" + isWifiConnected());
        }
        this.mWifiRepeaterFreq = group.getFrequence();
        HwWifiCHRStateManagerImpl hwWifiCHRStateManagerImpl = null;
        if (this.mWiFiCHRManager != null && (this.mWiFiCHRManager instanceof HwWifiCHRStateManagerImpl)) {
            hwWifiCHRStateManagerImpl = this.mWiFiCHRManager;
        }
        if (isWifiConnected()) {
            int resultCode = getConnectivityManager().tether(this.mTetherInterfaceName);
            if (HWDBG) {
                logd("ConnectivityManager.tether resultCode = " + resultCode);
            }
            if (resultCode == 0) {
                this.mIsWifiRepeaterTetherStarted = HWLOGW_E;
                Global.putInt(this.mContext.getContentResolver(), "wifi_repeater_on", WIFI_REPEATER_OPEN);
                if (hwWifiCHRStateManagerImpl != null) {
                    hwWifiCHRStateManagerImpl.addWifiRepeaterOpenedCount(WIFI_REPEATER_OPEN);
                }
                return HWLOGW_E;
            }
        }
        Global.putInt(this.mContext.getContentResolver(), "wifi_repeater_on", WIFI_REPEATER_OPEN_FAIL);
        if (hwWifiCHRStateManagerImpl != null) {
            hwWifiCHRStateManagerImpl.updateRepeaterOpenOrCloseError(ClientHandler.MSG_PROBE_WEB_START, WIFI_REPEATER_OPEN, "REPEATER_OPEN_OR_CLOSE_FAILED_UNKNOWN");
        }
        return HWDBG;
    }

    protected String getWifiRepeaterServerAddress() {
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
            int maxClientCount;
            if (0 == this.mWifiRepeaterBeginWorkTime && group.getClientList().size() == WIFI_REPEATER_OPEN) {
                this.mWifiRepeaterBeginWorkTime = SystemClock.elapsedRealtime();
            }
            if (group.getClientList().size() > 0) {
                maxClientCount = group.getClientList().size();
            } else {
                maxClientCount = WIFI_REPEATER_CLOSE;
            }
            if (this.mWiFiCHRManager != null && (this.mWiFiCHRManager instanceof HwWifiCHRStateManagerImpl)) {
                this.mWiFiCHRManager.setRepeaterMaxClientCount(maxClientCount);
            }
        }
    }

    public void handleClientDisconnect(WifiP2pGroup group) {
        HwWifiCHRStateManagerImpl hwWifiCHRStateManagerImpl = null;
        if (this.mIsWifiRepeaterTetherStarted) {
            if (this.mWiFiCHRManager != null && (this.mWiFiCHRManager instanceof HwWifiCHRStateManagerImpl)) {
                hwWifiCHRStateManagerImpl = this.mWiFiCHRManager;
            }
            if (!(group == null || group.getClientList().size() != 0 || hwWifiCHRStateManagerImpl == null)) {
                this.mWifiRepeaterEndWorkTime = SystemClock.elapsedRealtime();
                hwWifiCHRStateManagerImpl.setWifiRepeaterWorkingTime((this.mWifiRepeaterEndWorkTime - this.mWifiRepeaterBeginWorkTime) / 1000);
                this.mWifiRepeaterEndWorkTime = 0;
                this.mWifiRepeaterBeginWorkTime = 0;
            }
        }
    }

    protected void stopWifiRepeater(WifiP2pGroup group) {
        Global.putInt(this.mContext.getContentResolver(), "wifi_repeater_on", WIFI_REPEATER_CLOSEING);
        this.mWifiRepeaterEnabled = HWDBG;
        this.mWifiRepeaterEndWorkTime = SystemClock.elapsedRealtime();
        HwWifiCHRStateManagerImpl hwWifiCHRStateManagerImpl = null;
        if (this.mWiFiCHRManager != null && (this.mWiFiCHRManager instanceof HwWifiCHRStateManagerImpl)) {
            hwWifiCHRStateManagerImpl = this.mWiFiCHRManager;
        }
        if (!(group == null || group.getClientList().size() <= 0 || hwWifiCHRStateManagerImpl == null)) {
            hwWifiCHRStateManagerImpl.setWifiRepeaterWorkingTime((this.mWifiRepeaterEndWorkTime - this.mWifiRepeaterBeginWorkTime) / 1000);
        }
        this.mWifiRepeaterBeginWorkTime = 0;
        this.mWifiRepeaterEndWorkTime = 0;
        this.mWifiRepeaterFreq = WIFI_REPEATER_CLOSE;
        if (this.mIsWifiRepeaterTetherStarted) {
            int resultCode = getConnectivityManager().untether(this.mTetherInterfaceName);
            if (HWDBG) {
                logd("ConnectivityManager.untether resultCode = " + resultCode);
            }
            if (resultCode == 0) {
                this.mIsWifiRepeaterTetherStarted = HWDBG;
                Global.putInt(this.mContext.getContentResolver(), "wifi_repeater_on", WIFI_REPEATER_CLOSE);
                return;
            }
            loge("Untether initiate failed!");
            Global.putInt(this.mContext.getContentResolver(), "wifi_repeater_on", WIFI_REPEATER_CLOSE_FAIL);
            if (hwWifiCHRStateManagerImpl != null) {
                hwWifiCHRStateManagerImpl.updateRepeaterOpenOrCloseError(ClientHandler.MSG_PROBE_WEB_START, WIFI_REPEATER_CLOSE, "REPEATER_OPEN_OR_CLOSE_FAILED_UNKNOWN");
                return;
            }
            return;
        }
        Global.putInt(this.mContext.getContentResolver(), "wifi_repeater_on", WIFI_REPEATER_CLOSE);
    }

    protected boolean getWifiRepeaterEnabled() {
        return this.mWifiRepeaterEnabled;
    }

    protected void initWifiRepeaterConfig() {
        if (this.mWifiRepeaterConfigChannel == null) {
            this.mWifiRepeaterConfigChannel = new AsyncChannel();
            this.mWifiRepeaterConfigStore = WifiRepeaterConfigStore.makeWifiRepeaterConfigStore(this.mP2pStateMachine.getHandler());
            this.mWifiRepeaterConfigStore.loadRepeaterConfiguration();
            this.mWifiRepeaterConfigChannel.connectSync(this.mContext, this.mP2pStateMachine.getHandler(), this.mWifiRepeaterConfigStore.getMessenger());
        }
    }

    public void setWifiRepeaterConfiguration(WifiConfiguration config) {
        if (this.mWifiRepeaterConfigChannel != null) {
            this.mWifiRepeaterConfigChannel.sendMessage(CMD_SET_REPEATER_CONFIG, config);
        }
    }

    public WifiConfiguration syncGetWifiRepeaterConfiguration() {
        if (this.mWifiRepeaterConfigChannel == null) {
            return null;
        }
        Message resultMsg = this.mWifiRepeaterConfigChannel.sendMessageSynchronously(CMD_REQUEST_REPEATER_CONFIG);
        WifiConfiguration ret = resultMsg.obj;
        resultMsg.recycle();
        return ret;
    }

    private void enforceAccessPermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.ACCESS_WIFI_STATE", "WifiService");
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        String _arg0;
        switch (code) {
            case CODE_GET_WIFI_REPEATER_CONFIG /*1001*/:
                data.enforceInterface(DESCRIPTOR);
                enforceAccessPermission();
                Slog.d(TAG, "GetWifiRepeaterConfiguration ");
                WifiConfiguration _result = syncGetWifiRepeaterConfiguration();
                reply.writeNoException();
                if (_result != null) {
                    reply.writeInt(WIFI_REPEATER_OPEN);
                    _result.writeToParcel(reply, WIFI_REPEATER_OPEN);
                } else {
                    reply.writeInt(WIFI_REPEATER_CLOSE);
                }
                return HWLOGW_E;
            case CODE_SET_WIFI_REPEATER_CONFIG /*1002*/:
                WifiConfiguration wifiConfiguration;
                data.enforceInterface(DESCRIPTOR);
                enforceAccessPermission();
                Slog.d(TAG, "setWifiRepeaterConfiguration ");
                if (data.readInt() != 0) {
                    wifiConfiguration = (WifiConfiguration) WifiConfiguration.CREATOR.createFromParcel(data);
                } else {
                    wifiConfiguration = null;
                }
                setWifiRepeaterConfiguration(wifiConfiguration);
                reply.writeNoException();
                return HWLOGW_E;
            case CODE_WIFI_MAGICLINK_CONFIG_IP /*1003*/:
                String _arg1;
                String readString;
                data.enforceInterface(DESCRIPTOR);
                enforceAccessPermission();
                Slog.d(TAG, "configIPAddr ");
                if (data.readInt() != 0) {
                    _arg0 = data.readString();
                    _arg1 = data.readString();
                    readString = data.readString();
                } else {
                    _arg0 = null;
                    _arg1 = null;
                    readString = null;
                }
                configIPAddr(_arg0, _arg1, readString);
                reply.writeNoException();
                return HWLOGW_E;
            case CODE_WIFI_MAGICLINK_RELEASE_IP /*1004*/:
                data.enforceInterface(DESCRIPTOR);
                enforceAccessPermission();
                Slog.d(TAG, "setWifiRepeaterConfiguration ");
                if (data.readInt() != 0) {
                    _arg0 = data.readString();
                } else {
                    _arg0 = null;
                }
                releaseIPAddr(_arg0);
                reply.writeNoException();
                return HWLOGW_E;
            default:
                return super.onTransact(code, data, reply, flags);
        }
    }

    protected boolean processMessageForP2pCollision(Message msg, State state) {
        boolean mIsP2pCollision = HWDBG;
        if (state instanceof DefaultState) {
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
                        mIsP2pCollision = HWLOGW_E;
                        break;
                    }
                    break;
            }
        }
        if (state instanceof P2pEnabledState) {
            switch (msg.what) {
                case 139265:
                case 139315:
                case 139318:
                case 139329:
                case 139332:
                case 139335:
                    if (this.mWifiRepeaterEnabled) {
                        showUserToastIfP2pCollision();
                        mIsP2pCollision = HWLOGW_E;
                        break;
                    }
                    break;
            }
        }
        if (state instanceof InactiveState) {
            switch (msg.what) {
                case 139265:
                case 139274:
                case 139277:
                case 139329:
                case 139332:
                case 139335:
                    if (this.mWifiRepeaterEnabled) {
                        showUserToastIfP2pCollision();
                        mIsP2pCollision = HWLOGW_E;
                        break;
                    }
                    break;
            }
        }
        if (state instanceof GroupCreatingState) {
            switch (msg.what) {
                case 139265:
                case 139274:
                    if (this.mWifiRepeaterEnabled) {
                        showUserToastIfP2pCollision();
                        mIsP2pCollision = HWLOGW_E;
                        break;
                    }
                    break;
            }
        }
        if (!(state instanceof GroupCreatedState)) {
            return mIsP2pCollision;
        }
        switch (msg.what) {
            case 139271:
                if (!this.mWifiRepeaterEnabled) {
                    return mIsP2pCollision;
                }
                showUserToastIfP2pCollision();
                return HWLOGW_E;
            default:
                return mIsP2pCollision;
        }
    }

    private void showUserToastIfP2pCollision() {
        Toast.makeText(this.mContext, 33685832, WIFI_REPEATER_CLOSE).show();
    }

    protected boolean getMagicLinkDeviceFlag() {
        return this.mMagicLinkDeviceFlag;
    }

    protected void setmMagicLinkDeviceFlag(boolean magicLinkDeviceFlag) {
        this.mMagicLinkDeviceFlag = magicLinkDeviceFlag;
    }

    private String buildPrintableIpAddress(String originIpAddr) {
        if (originIpAddr == null) {
            return null;
        }
        byte[] ipAddrArray = NetworkUtils.numericToInetAddress(originIpAddr).getAddress();
        if (ipAddrArray.length != WIFI_REPEATER_CLOSE_FAIL) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int index = WIFI_REPEATER_CLOSE; index < WIFI_REPEATER_OPENING; index += WIFI_REPEATER_OPEN) {
            sb.append(ipAddrArray[index]);
            sb.append(".");
        }
        sb.append("***");
        return sb.toString();
    }

    private boolean configIPAddr(String ifName, String ipAddr, String gateway) {
        Exception e;
        Slog.d(TAG, "configIPAddr: " + ifName + HwCHRWifiCPUUsage.COL_SEP + buildPrintableIpAddress(ipAddr));
        try {
            this.mNwService.enableIpv6(ifName);
            InterfaceConfiguration ifcg = new InterfaceConfiguration();
            try {
                ifcg.setLinkAddress(new LinkAddress(NetworkUtils.numericToInetAddress(ipAddr), 24));
                ifcg.setInterfaceUp();
                this.mNwService.setInterfaceConfig(ifName, ifcg);
                RouteInfo connectedRoute = new RouteInfo(new LinkAddress((Inet4Address) NetworkUtils.numericToInetAddress(ipAddr), 24), null, ifName);
                List<RouteInfo> routes = new ArrayList(WIFI_REPEATER_OPENING);
                routes.add(connectedRoute);
                routes.add(new RouteInfo((IpPrefix) null, NetworkUtils.numericToInetAddress(gateway), ifName));
                Log.e(TAG, "add new RouteInfo() gateway:" + buildPrintableIpAddress(gateway) + " iface:" + ifName);
                this.mNwService.addInterfaceToLocalNetwork(ifName, routes);
                InterfaceConfiguration interfaceConfiguration = ifcg;
            } catch (Exception e2) {
                e = e2;
                Log.i(TAG, "", e);
                Slog.d(TAG, "configIPAddr: " + ifName + HwCHRWifiCPUUsage.COL_SEP + buildPrintableIpAddress(ipAddr) + "* ok");
                return HWLOGW_E;
            }
        } catch (Exception e3) {
            e = e3;
            Log.i(TAG, "", e);
            Slog.d(TAG, "configIPAddr: " + ifName + HwCHRWifiCPUUsage.COL_SEP + buildPrintableIpAddress(ipAddr) + "* ok");
            return HWLOGW_E;
        }
        Slog.d(TAG, "configIPAddr: " + ifName + HwCHRWifiCPUUsage.COL_SEP + buildPrintableIpAddress(ipAddr) + "* ok");
        return HWLOGW_E;
    }

    private boolean releaseIPAddr(String ifName) {
        if (ifName == null) {
            return HWDBG;
        }
        try {
            this.mNwService.disableIpv6(ifName);
            this.mNwService.clearInterfaceAddresses(ifName);
            return HWLOGW_E;
        } catch (Exception e) {
            Log.e(TAG, "Failed to clear addresses or disable IPv6" + e);
            return HWDBG;
        }
    }
}
