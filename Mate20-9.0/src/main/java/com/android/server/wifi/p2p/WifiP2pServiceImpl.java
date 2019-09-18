package com.android.server.wifi.p2p;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.hdm.HwDeviceManager;
import android.net.ConnectivityManager;
import android.net.DhcpResults;
import android.net.InterfaceConfiguration;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.NetworkInfo;
import android.net.NetworkUtils;
import android.net.ip.IpClient;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pGroupList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pProvDiscEvent;
import android.net.wifi.p2p.WifiP2pWfdInfo;
import android.net.wifi.p2p.nsd.WifiP2pServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pServiceRequest;
import android.net.wifi.p2p.nsd.WifiP2pServiceResponse;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.IDeviceIdleController;
import android.os.INetworkManagementService;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Flog;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.android.internal.util.AsyncChannel;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.wifi.HalDeviceManager;
import com.android.server.wifi.HwWifiBigDataConstant;
import com.android.server.wifi.HwWifiCHRService;
import com.android.server.wifi.HwWifiServiceFactory;
import com.android.server.wifi.SupplicantStaIfaceHal;
import com.android.server.wifi.WifiInjector;
import com.android.server.wifi.WifiP2pServiceHisiExt;
import com.android.server.wifi.WifiStateMachine;
import com.android.server.wifi.p2p.WifiP2pServiceImpl;
import com.android.server.wifi.util.WifiAsyncChannel;
import com.android.server.wifi.util.WifiCommonUtils;
import com.android.server.wifi.util.WifiHandler;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONException;
import org.json.JSONObject;

public class WifiP2pServiceImpl extends AbsWifiP2pService {
    private static final int APKACTION_P2PSCAN = 13;
    private static final int BASE = 143360;
    public static final int BLOCK_DISCOVERY = 143375;
    private static final boolean DBG = true;
    private static final int DELAY_TIME = 2000;
    public static final int DISABLED = 0;
    public static final int DISABLE_P2P = 143377;
    public static final int DISABLE_P2P_TIMED_OUT = 143366;
    private static final int DISABLE_P2P_WAIT_TIME_MS = 5000;
    public static final int DISCONNECT_WIFI_REQUEST = 143372;
    public static final int DISCONNECT_WIFI_RESPONSE = 143373;
    private static final int DISCOVER_TIMEOUT_S = 120;
    private static final int DROP_WIFI_USER_ACCEPT = 143364;
    private static final int DROP_WIFI_USER_REJECT = 143365;
    private static final String EMPTY_DEVICE_ADDRESS = "00:00:00:00:00:00";
    public static final int ENABLED = 1;
    public static final int ENABLE_P2P = 143376;
    /* access modifiers changed from: private */
    public static final Boolean FORM_GROUP = false;
    public static final int GROUP_CREATING_TIMED_OUT = 143361;
    private static final int GROUP_CREATING_WAIT_TIME_MS = 120000;
    private static final int GROUP_IDLE_TIME_S = 10;
    private static final boolean HWDBG = (Log.HWLog || (Log.HWModuleLog && Log.isLoggable(TAG, 3)));
    private static final int IPC_DHCP_RESULTS = 143392;
    private static final int IPC_POST_DHCP_ACTION = 143391;
    private static final int IPC_PRE_DHCP_ACTION = 143390;
    private static final int IPC_PROVISIONING_FAILURE = 143394;
    private static final int IPC_PROVISIONING_SUCCESS = 143393;
    /* access modifiers changed from: private */
    public static final boolean IS_ATT;
    /* access modifiers changed from: private */
    public static final boolean IS_VERIZON = ("389".equals(SystemProperties.get("ro.config.hw_opta", "0")) && "840".equals(SystemProperties.get("ro.config.hw_optb", "0")));
    private static final Boolean JOIN_GROUP = true;
    private static final String NETWORKTYPE = "WIFI_P2P";
    private static final Boolean NO_RELOAD = false;
    static final int P2P_BLUETOOTH_COEXISTENCE_MODE_DISABLED = 1;
    static final int P2P_BLUETOOTH_COEXISTENCE_MODE_SENSE = 2;
    public static final int P2P_CONNECTION_CHANGED = 143371;
    private static final int P2P_DEVICE_OF_MIRACAST = 7;
    private static final int P2P_DEVICE_RETRY_MAX = 5;
    private static final int P2P_DISCONNECT = -1;
    private static final String PACKAGE_NAME = "com.huawei.android.wfdft";
    private static final int PEER_CONNECTION_USER_ACCEPT = 143362;
    private static final int PEER_CONNECTION_USER_REJECT = 143363;
    /* access modifiers changed from: private */
    public static final Boolean RELOAD = true;
    private static final String SERVER_ADDRESS = "192.168.49.1";
    private static final String SERVER_ADDRESS_WIFI_BRIDGE = "192.168.43.1";
    public static final int SET_MIRACAST_MODE = 143374;
    public static final int SHOW_USER_CONFIRM_DIALOG = 143410;
    private static final String TAG = "WifiP2pService";
    /* access modifiers changed from: private */
    public static int sDisableP2pTimeoutIndex = 0;
    /* access modifiers changed from: private */
    public static int sGroupCreatingTimeoutIndex = 0;
    /* access modifiers changed from: private */
    public boolean mAutonomousGroup;
    private ClientHandler mClientHandler;
    /* access modifiers changed from: private */
    public HashMap<Messenger, ClientInfo> mClientInfoList = new HashMap<>();
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public boolean mCreateWifiBridge = false;
    /* access modifiers changed from: private */
    public final Map<IBinder, DeathHandlerData> mDeathDataByBinder = new HashMap();
    /* access modifiers changed from: private */
    public DhcpResults mDhcpResults;
    /* access modifiers changed from: private */
    public boolean mDiscoveryBlocked;
    /* access modifiers changed from: private */
    public boolean mDiscoveryPostponed = false;
    /* access modifiers changed from: private */
    public boolean mDiscoveryStarted;
    /* access modifiers changed from: private */
    public HwWifiCHRService mHwWifiCHRService;
    /* access modifiers changed from: private */
    public IpClient mIpClient;
    /* access modifiers changed from: private */
    public boolean mIsInvite = false;
    /* access modifiers changed from: private */
    public boolean mJoinExistingGroup;
    private Object mLock = new Object();
    /* access modifiers changed from: private */
    public NetworkInfo mNetworkInfo;
    INetworkManagementService mNwService;
    /* access modifiers changed from: private */
    public String mP2pServerAddress;
    protected P2pStateMachine mP2pStateMachine;
    private final boolean mP2pSupported;
    /* access modifiers changed from: private */
    public AsyncChannel mReplyChannel = new WifiAsyncChannel(TAG);
    /* access modifiers changed from: private */
    public String mServiceDiscReqId;
    /* access modifiers changed from: private */
    public byte mServiceTransactionId = 0;
    /* access modifiers changed from: private */
    public boolean mTemporarilyDisconnectedWifi = false;
    /* access modifiers changed from: private */
    public WifiP2pDevice mThisDevice = new WifiP2pDevice();
    /* access modifiers changed from: private */
    public AsyncChannel mWifiChannel;
    private WifiInjector mWifiInjector;
    /* access modifiers changed from: private */
    public int mWifiP2pDevCreateRetry = 0;
    WifiP2pServiceHisiExt mWifiP2pServiceHisiExt = null;

    private class ClientHandler extends WifiHandler {
        ClientHandler(String tag, Looper looper) {
            super(tag, looper);
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 139265:
                case 139268:
                case 139271:
                case 139274:
                case 139277:
                case 139280:
                case 139283:
                case 139285:
                case 139287:
                case 139292:
                case 139295:
                case 139298:
                case 139301:
                case 139304:
                case 139307:
                case 139310:
                case 139315:
                case 139318:
                case 139321:
                case 139323:
                case 139326:
                case 139329:
                case 139332:
                case 139335:
                    boolean isRequestPeers = true;
                    boolean isConnect = msg.what == 139271;
                    boolean isDiscoverPeers = msg.what == 139265;
                    if (msg.what != 139283) {
                        isRequestPeers = false;
                    }
                    if (!HwDeviceManager.disallowOp(45) || (!isConnect && !isDiscoverPeers && !isRequestPeers)) {
                        WifiP2pServiceImpl.this.mP2pStateMachine.sendMessage(Message.obtain(msg));
                        return;
                    }
                    Slog.d(WifiP2pServiceImpl.TAG, "wifiP2P function is forbidden,msg.what = " + msg.what);
                    Toast.makeText(WifiP2pServiceImpl.this.mContext, WifiP2pServiceImpl.this.mContext.getResources().getString(33686008), 0).show();
                    return;
                default:
                    if (HwDeviceManager.disallowOp(45)) {
                        Slog.d(WifiP2pServiceImpl.TAG, "wifiP2P function is forbidden,msg.what = " + msg.what);
                        Toast.makeText(WifiP2pServiceImpl.this.mContext, WifiP2pServiceImpl.this.mContext.getResources().getString(33686008), 0).show();
                        return;
                    }
                    WifiP2pServiceImpl.this.handleClientHwMessage(Message.obtain(msg));
                    return;
            }
        }
    }

    private class ClientInfo {
        /* access modifiers changed from: private */
        public Messenger mMessenger;
        /* access modifiers changed from: private */
        public SparseArray<WifiP2pServiceRequest> mReqList;
        /* access modifiers changed from: private */
        public List<WifiP2pServiceInfo> mServList;

        private ClientInfo(Messenger m) {
            this.mMessenger = m;
            this.mReqList = new SparseArray<>();
            this.mServList = new ArrayList();
        }
    }

    private class DeathHandlerData {
        IBinder.DeathRecipient mDeathRecipient;
        Messenger mMessenger;

        DeathHandlerData(IBinder.DeathRecipient dr, Messenger m) {
            this.mDeathRecipient = dr;
            this.mMessenger = m;
        }

        public String toString() {
            return "deathRecipient=" + this.mDeathRecipient + ", messenger=" + this.mMessenger;
        }
    }

    protected class P2pStateMachine extends StateMachine {
        /* access modifiers changed from: private */
        public AfterUserAuthorizingJoinState mAfterUserAuthorizingJoinState = new AfterUserAuthorizingJoinState();
        private DefaultState mDefaultState = new DefaultState();
        /* access modifiers changed from: private */
        public FrequencyConflictState mFrequencyConflictState = new FrequencyConflictState();
        /* access modifiers changed from: private */
        public WifiP2pGroup mGroup;
        /* access modifiers changed from: private */
        public GroupCreatedState mGroupCreatedState = new GroupCreatedState();
        /* access modifiers changed from: private */
        public GroupCreatingState mGroupCreatingState = new GroupCreatingState();
        protected GroupNegotiationState mGroupNegotiationState = new GroupNegotiationState();
        protected final WifiP2pGroupList mGroups = new WifiP2pGroupList(null, new WifiP2pGroupList.GroupDeleteListener() {
            public void onDeleteGroup(int netId) {
                P2pStateMachine p2pStateMachine = P2pStateMachine.this;
                p2pStateMachine.logd("called onDeleteGroup() netId=" + netId);
                P2pStateMachine.this.mWifiNative.removeP2pNetwork(netId);
                P2pStateMachine.this.mWifiNative.saveConfig();
                P2pStateMachine.this.updatePersistentNetworks(WifiP2pServiceImpl.RELOAD.booleanValue());
                P2pStateMachine.this.sendP2pPersistentGroupsChangedBroadcast();
            }
        });
        IDeviceIdleController mIDeviceIdleController;
        /* access modifiers changed from: private */
        public InactiveState mInactiveState = new InactiveState();
        /* access modifiers changed from: private */
        public String mInterfaceName;
        /* access modifiers changed from: private */
        public boolean mIsBTCoexDisabled = false;
        private boolean mIsInterfaceAvailable = false;
        /* access modifiers changed from: private */
        public boolean mIsWifiEnabled = false;
        /* access modifiers changed from: private */
        public OngoingGroupRemovalState mOngoingGroupRemovalState = new OngoingGroupRemovalState();
        /* access modifiers changed from: private */
        public P2pDisabledState mP2pDisabledState = new P2pDisabledState();
        /* access modifiers changed from: private */
        public P2pDisablingState mP2pDisablingState = new P2pDisablingState();
        private P2pEnabledState mP2pEnabledState = new P2pEnabledState();
        private P2pNotSupportedState mP2pNotSupportedState = new P2pNotSupportedState();
        protected final WifiP2pDeviceList mPeers = new WifiP2pDeviceList();
        /* access modifiers changed from: private */
        public final WifiP2pDeviceList mPeersLostDuringConnection = new WifiP2pDeviceList();
        /* access modifiers changed from: private */
        public boolean mPendingReformGroupIndication = false;
        /* access modifiers changed from: private */
        public ProvisionDiscoveryState mProvisionDiscoveryState = new ProvisionDiscoveryState();
        protected WifiP2pConfig mSavedPeerConfig = new WifiP2pConfig();
        /* access modifiers changed from: private */
        public UserAuthorizingInviteRequestState mUserAuthorizingInviteRequestState = new UserAuthorizingInviteRequestState();
        /* access modifiers changed from: private */
        public UserAuthorizingJoinState mUserAuthorizingJoinState = new UserAuthorizingJoinState();
        /* access modifiers changed from: private */
        public UserAuthorizingNegotiationRequestState mUserAuthorizingNegotiationRequestState = new UserAuthorizingNegotiationRequestState();
        /* access modifiers changed from: private */
        public WifiInjector mWifiInjector;
        /* access modifiers changed from: private */
        public WifiP2pMonitor mWifiMonitor = WifiInjector.getInstance().getWifiP2pMonitor();
        protected WifiP2pNative mWifiNative = WifiInjector.getInstance().getWifiP2pNative();
        /* access modifiers changed from: private */
        public final WifiP2pInfo mWifiP2pInfo = new WifiP2pInfo();

        class AfterUserAuthorizingJoinState extends State {
            AfterUserAuthorizingJoinState() {
            }

            public void enter() {
                P2pStateMachine.this.logd(getName());
            }

            public boolean processMessage(Message message) {
                P2pStateMachine p2pStateMachine = P2pStateMachine.this;
                p2pStateMachine.logd(getName() + message.toString());
                return false;
            }

            public void exit() {
            }
        }

        class DefaultState extends State {
            DefaultState() {
            }

            public boolean processMessage(Message message) {
                P2pStateMachine p2pStateMachine = P2pStateMachine.this;
                p2pStateMachine.logd(getName() + message.toString());
                if (WifiP2pServiceImpl.this.processMessageForP2pCollision(message, this)) {
                    return true;
                }
                WifiP2pGroup wifiP2pGroup = null;
                switch (message.what) {
                    case -1:
                        P2pStateMachine.this.removePowerSaveWhitelist();
                        break;
                    case 69632:
                        if (message.arg1 != 0) {
                            P2pStateMachine p2pStateMachine2 = P2pStateMachine.this;
                            p2pStateMachine2.loge("Full connection failure, error = " + message.arg1);
                            AsyncChannel unused = WifiP2pServiceImpl.this.mWifiChannel = null;
                            P2pStateMachine.this.transitionTo(P2pStateMachine.this.mP2pDisabledState);
                            break;
                        } else {
                            P2pStateMachine.this.logd("Full connection with WifiStateMachine established");
                            AsyncChannel unused2 = WifiP2pServiceImpl.this.mWifiChannel = (AsyncChannel) message.obj;
                            break;
                        }
                    case 69633:
                        new WifiAsyncChannel(WifiP2pServiceImpl.TAG).connect(WifiP2pServiceImpl.this.mContext, P2pStateMachine.this.getHandler(), message.replyTo);
                        break;
                    case 69636:
                        if (message.arg1 == 2) {
                            P2pStateMachine.this.loge("Send failed, client connection lost");
                        } else {
                            P2pStateMachine p2pStateMachine3 = P2pStateMachine.this;
                            p2pStateMachine3.loge("Client connection lost with reason: " + message.arg1);
                        }
                        AsyncChannel unused3 = WifiP2pServiceImpl.this.mWifiChannel = null;
                        P2pStateMachine.this.transitionTo(P2pStateMachine.this.mP2pDisabledState);
                        break;
                    case 139265:
                        P2pStateMachine.this.replyToMessage(message, 139266, 2);
                        break;
                    case 139268:
                        P2pStateMachine.this.replyToMessage(message, 139269, 2);
                        break;
                    case 139271:
                        P2pStateMachine.this.replyToMessage(message, 139272, 2);
                        break;
                    case 139274:
                        P2pStateMachine.this.replyToMessage(message, 139275, 2);
                        break;
                    case 139277:
                        P2pStateMachine.this.replyToMessage(message, 139278, 2);
                        break;
                    case 139280:
                        P2pStateMachine.this.replyToMessage(message, 139281, 2);
                        if (WifiP2pServiceImpl.this.getWifiRepeaterEnabled()) {
                            WifiP2pServiceImpl.this.stopWifiRepeater(P2pStateMachine.this.mGroup);
                            break;
                        }
                        break;
                    case 139283:
                        P2pStateMachine.this.replyToMessage(message, 139284, (Object) P2pStateMachine.this.getPeers((Bundle) message.obj, message.sendingUid));
                        break;
                    case 139285:
                        P2pStateMachine.this.replyToMessage(message, 139286, (Object) new WifiP2pInfo(P2pStateMachine.this.mWifiP2pInfo));
                        break;
                    case 139287:
                        P2pStateMachine p2pStateMachine4 = P2pStateMachine.this;
                        if (P2pStateMachine.this.mGroup != null) {
                            wifiP2pGroup = new WifiP2pGroup(P2pStateMachine.this.mGroup);
                        }
                        p2pStateMachine4.replyToMessage(message, 139288, (Object) wifiP2pGroup);
                        break;
                    case 139292:
                        P2pStateMachine.this.replyToMessage(message, 139293, 2);
                        break;
                    case 139295:
                        P2pStateMachine.this.replyToMessage(message, 139296, 2);
                        break;
                    case 139298:
                        P2pStateMachine.this.replyToMessage(message, 139299, 2);
                        break;
                    case 139301:
                        P2pStateMachine.this.replyToMessage(message, 139302, 2);
                        break;
                    case 139304:
                        P2pStateMachine.this.replyToMessage(message, 139305, 2);
                        break;
                    case 139307:
                        P2pStateMachine.this.replyToMessage(message, 139308, 2);
                        break;
                    case 139310:
                        P2pStateMachine.this.replyToMessage(message, 139311, 2);
                        break;
                    case 139315:
                        P2pStateMachine.this.replyToMessage(message, 139316, 2);
                        break;
                    case 139318:
                        P2pStateMachine.this.replyToMessage(message, 139318, 2);
                        break;
                    case 139321:
                        P2pStateMachine.this.replyToMessage(message, 139322, (Object) new WifiP2pGroupList(P2pStateMachine.this.mGroups, null));
                        break;
                    case 139323:
                        if (WifiP2pServiceImpl.this.getWfdPermission(message.sendingUid)) {
                            P2pStateMachine.this.replyToMessage(message, 139324, 2);
                            break;
                        } else {
                            P2pStateMachine.this.replyToMessage(message, 139324, 0);
                            break;
                        }
                    case 139326:
                        P2pStateMachine.this.replyToMessage(message, 139327, 2);
                        break;
                    case 139329:
                    case 139332:
                    case 139335:
                    case WifiP2pServiceImpl.GROUP_CREATING_TIMED_OUT /*143361*/:
                    case WifiP2pServiceImpl.PEER_CONNECTION_USER_ACCEPT /*143362*/:
                    case WifiP2pServiceImpl.PEER_CONNECTION_USER_REJECT /*143363*/:
                    case WifiP2pServiceImpl.DROP_WIFI_USER_ACCEPT /*143364*/:
                    case WifiP2pServiceImpl.DROP_WIFI_USER_REJECT /*143365*/:
                    case WifiP2pServiceImpl.DISABLE_P2P_TIMED_OUT /*143366*/:
                    case WifiP2pServiceImpl.DISCONNECT_WIFI_RESPONSE /*143373*/:
                    case WifiP2pServiceImpl.SET_MIRACAST_MODE /*143374*/:
                    case WifiP2pServiceImpl.ENABLE_P2P /*143376*/:
                    case WifiP2pServiceImpl.IPC_PRE_DHCP_ACTION /*143390*/:
                    case WifiP2pServiceImpl.IPC_POST_DHCP_ACTION /*143391*/:
                    case WifiP2pServiceImpl.IPC_DHCP_RESULTS /*143392*/:
                    case WifiP2pServiceImpl.IPC_PROVISIONING_SUCCESS /*143393*/:
                    case WifiP2pServiceImpl.IPC_PROVISIONING_FAILURE /*143394*/:
                    case 147457:
                    case 147458:
                    case WifiP2pMonitor.P2P_DEVICE_FOUND_EVENT:
                    case WifiP2pMonitor.P2P_DEVICE_LOST_EVENT:
                    case WifiP2pMonitor.P2P_GROUP_FORMATION_FAILURE_EVENT:
                    case WifiP2pMonitor.P2P_GROUP_REMOVED_EVENT:
                    case WifiP2pMonitor.P2P_INVITATION_RESULT_EVENT:
                    case WifiP2pMonitor.P2P_FIND_STOPPED_EVENT:
                    case WifiP2pMonitor.P2P_SERV_DISC_RESP_EVENT:
                    case WifiP2pMonitor.P2P_PROV_DISC_FAILURE_EVENT:
                        break;
                    case 139339:
                    case 139340:
                        P2pStateMachine.this.replyToMessage(message, 139341, (Object) null);
                        break;
                    case 139342:
                    case 139343:
                        P2pStateMachine.this.replyToMessage(message, 139345, 2);
                        break;
                    case WifiP2pServiceImpl.BLOCK_DISCOVERY /*143375*/:
                        boolean unused4 = WifiP2pServiceImpl.this.mDiscoveryBlocked = message.arg1 == 1;
                        boolean unused5 = WifiP2pServiceImpl.this.mDiscoveryPostponed = false;
                        if (WifiP2pServiceImpl.this.mDiscoveryBlocked) {
                            if (message.obj != null) {
                                try {
                                    ((StateMachine) message.obj).sendMessage(message.arg2);
                                    break;
                                } catch (Exception e) {
                                    P2pStateMachine p2pStateMachine5 = P2pStateMachine.this;
                                    p2pStateMachine5.loge("unable to send BLOCK_DISCOVERY response: " + e);
                                    break;
                                }
                            } else {
                                Log.e(WifiP2pServiceImpl.TAG, "Illegal argument(s)");
                                break;
                            }
                        }
                        break;
                    case WifiP2pServiceImpl.DISABLE_P2P /*143377*/:
                        if (WifiP2pServiceImpl.this.mWifiChannel == null) {
                            P2pStateMachine.this.loge("Unexpected disable request when WifiChannel is null");
                            break;
                        } else {
                            WifiP2pServiceImpl.this.mWifiChannel.sendMessage(WifiStateMachine.CMD_DISABLE_P2P_RSP);
                            break;
                        }
                    case WifiP2pServiceImpl.SHOW_USER_CONFIRM_DIALOG /*143410*/:
                        WifiP2pServiceImpl.this.mWifiP2pServiceHisiExt.showP2pEanbleDialog();
                        break;
                    case WifiP2pMonitor.P2P_GROUP_STARTED_EVENT:
                        if (message.obj != null) {
                            WifiP2pGroup unused6 = P2pStateMachine.this.mGroup = (WifiP2pGroup) message.obj;
                            P2pStateMachine p2pStateMachine6 = P2pStateMachine.this;
                            p2pStateMachine6.loge("Unexpected group creation, remove " + P2pStateMachine.this.mGroup);
                            P2pStateMachine.this.mWifiNative.p2pGroupRemove(P2pStateMachine.this.mGroup.getInterface());
                            break;
                        } else {
                            Log.e(WifiP2pServiceImpl.TAG, "Illegal arguments");
                            break;
                        }
                    default:
                        return WifiP2pServiceImpl.this.handleDefaultStateMessage(message);
                }
                return true;
            }
        }

        class FrequencyConflictState extends State {
            private AlertDialog mFrequencyConflictDialog;

            FrequencyConflictState() {
            }

            public void enter() {
                P2pStateMachine.this.logd(getName());
                notifyFrequencyConflict();
            }

            private void notifyFrequencyConflict() {
                P2pStateMachine.this.logd("Notify frequency conflict");
                Resources r = Resources.getSystem();
                AlertDialog dialog = new AlertDialog.Builder(WifiP2pServiceImpl.this.mContext).setMessage(r.getString(17041406, new Object[]{P2pStateMachine.this.getDeviceName(P2pStateMachine.this.mSavedPeerConfig.deviceAddress)})).setPositiveButton(r.getString(17039952), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        P2pStateMachine.this.sendMessage(WifiP2pServiceImpl.DROP_WIFI_USER_ACCEPT);
                    }
                }).setNegativeButton(r.getString(17039912), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        P2pStateMachine.this.sendMessage(WifiP2pServiceImpl.DROP_WIFI_USER_REJECT);
                    }
                }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                    public void onCancel(DialogInterface arg0) {
                        P2pStateMachine.this.sendMessage(WifiP2pServiceImpl.DROP_WIFI_USER_REJECT);
                    }
                }).create();
                dialog.setCanceledOnTouchOutside(false);
                dialog.getWindow().setType(2003);
                WindowManager.LayoutParams attrs = dialog.getWindow().getAttributes();
                attrs.privateFlags = 16;
                dialog.getWindow().setAttributes(attrs);
                dialog.show();
                this.mFrequencyConflictDialog = dialog;
            }

            public boolean processMessage(Message message) {
                P2pStateMachine p2pStateMachine = P2pStateMachine.this;
                p2pStateMachine.logd(getName() + message.toString());
                int i = message.what;
                if (i != 143373) {
                    switch (i) {
                        case WifiP2pServiceImpl.DROP_WIFI_USER_ACCEPT /*143364*/:
                            if (WifiP2pServiceImpl.this.mWifiChannel != null) {
                                WifiP2pServiceImpl.this.mWifiChannel.sendMessage(WifiP2pServiceImpl.DISCONNECT_WIFI_REQUEST, 1);
                            } else {
                                P2pStateMachine.this.loge("DROP_WIFI_USER_ACCEPT message received when WifiChannel is null");
                            }
                            boolean unused = WifiP2pServiceImpl.this.mTemporarilyDisconnectedWifi = true;
                            break;
                        case WifiP2pServiceImpl.DROP_WIFI_USER_REJECT /*143365*/:
                            P2pStateMachine.this.handleGroupCreationFailure();
                            P2pStateMachine.this.transitionTo(P2pStateMachine.this.mInactiveState);
                            break;
                        default:
                            switch (i) {
                                case WifiP2pMonitor.P2P_GO_NEGOTIATION_SUCCESS_EVENT:
                                case WifiP2pMonitor.P2P_GROUP_FORMATION_SUCCESS_EVENT:
                                    P2pStateMachine p2pStateMachine2 = P2pStateMachine.this;
                                    p2pStateMachine2.loge(getName() + "group sucess during freq conflict!");
                                    break;
                                case WifiP2pMonitor.P2P_GO_NEGOTIATION_FAILURE_EVENT:
                                case WifiP2pMonitor.P2P_GROUP_FORMATION_FAILURE_EVENT:
                                case WifiP2pMonitor.P2P_GROUP_REMOVED_EVENT:
                                    break;
                                case WifiP2pMonitor.P2P_GROUP_STARTED_EVENT:
                                    P2pStateMachine p2pStateMachine3 = P2pStateMachine.this;
                                    p2pStateMachine3.loge(getName() + "group started after freq conflict, handle anyway");
                                    P2pStateMachine.this.deferMessage(message);
                                    P2pStateMachine.this.transitionTo(P2pStateMachine.this.mGroupNegotiationState);
                                    break;
                                default:
                                    return false;
                            }
                    }
                } else {
                    P2pStateMachine p2pStateMachine4 = P2pStateMachine.this;
                    p2pStateMachine4.logd(getName() + "Wifi disconnected, retry p2p");
                    P2pStateMachine.this.transitionTo(P2pStateMachine.this.mInactiveState);
                    P2pStateMachine.this.sendMessage(139271, P2pStateMachine.this.mSavedPeerConfig);
                }
                return true;
            }

            public void exit() {
                if (this.mFrequencyConflictDialog != null) {
                    this.mFrequencyConflictDialog.dismiss();
                }
            }
        }

        class GroupCreatedState extends State {
            GroupCreatedState() {
            }

            private boolean handlP2pGroupRestart() {
                boolean remove = true;
                if (P2pStateMachine.this.mWifiNative.p2pGroupRemove(P2pStateMachine.this.mGroup.getInterface())) {
                    Slog.d(WifiP2pServiceImpl.TAG, "Removed P2P group successfully");
                    P2pStateMachine.this.transitionTo(P2pStateMachine.this.mOngoingGroupRemovalState);
                } else {
                    Slog.d(WifiP2pServiceImpl.TAG, "Failed to remove the P2P group");
                    P2pStateMachine.this.handleGroupRemoved();
                    P2pStateMachine.this.transitionTo(P2pStateMachine.this.mInactiveState);
                    remove = false;
                }
                if (WifiP2pServiceImpl.this.mAutonomousGroup) {
                    Slog.d(WifiP2pServiceImpl.TAG, "AutonomousGroup is set, reform P2P Group");
                    P2pStateMachine.this.sendMessage(139277);
                } else {
                    Slog.d(WifiP2pServiceImpl.TAG, "AutonomousGroup is not set, will not reform P2P Group");
                }
                return remove;
            }

            public void enter() {
                P2pStateMachine.this.logd(getName());
                P2pStateMachine p2pStateMachine = P2pStateMachine.this;
                p2pStateMachine.logd(getName() + "mPendingReformGroupIndication=" + P2pStateMachine.this.mPendingReformGroupIndication);
                if (P2pStateMachine.this.mPendingReformGroupIndication) {
                    boolean unused = P2pStateMachine.this.mPendingReformGroupIndication = false;
                    handlP2pGroupRestart();
                } else {
                    P2pStateMachine.this.mSavedPeerConfig.invalidate();
                    WifiP2pServiceImpl.this.mNetworkInfo.setDetailedState(NetworkInfo.DetailedState.CONNECTED, null, null);
                    if (WifiP2pServiceHisiExt.hisiWifiEnabled()) {
                        WifiP2pServiceImpl.this.mWifiP2pServiceHisiExt.sendP2pNetworkChangedBroadcast();
                    }
                    P2pStateMachine.this.updateThisDevice(0);
                    WifiP2pServiceImpl.this.sendP2pConnectedStateBroadcast();
                    if (P2pStateMachine.this.mGroup.isGroupOwner()) {
                        P2pStateMachine.this.setWifiP2pInfoOnGroupFormation(NetworkUtils.numericToInetAddress(WifiP2pServiceImpl.this.mP2pServerAddress));
                    }
                    if (WifiP2pServiceImpl.this.mAutonomousGroup) {
                        P2pStateMachine.this.sendP2pConnectionChangedBroadcast();
                    }
                }
                if (WifiP2pServiceImpl.this.getWifiRepeaterEnabled() && !WifiP2pServiceImpl.this.startWifiRepeater(P2pStateMachine.this.mGroup)) {
                    P2pStateMachine.this.sendMessage(139280);
                }
                WifiP2pServiceImpl.this.notifyP2pChannelNumber(WifiCommonUtils.convertFrequencyToChannelNumber(P2pStateMachine.this.mGroup.getFrequence()));
            }

            public boolean processMessage(Message message) {
                P2pStateMachine p2pStateMachine = P2pStateMachine.this;
                p2pStateMachine.logd(getName() + "when=" + message.getWhen() + " what=" + message.what + " arg1=" + message.arg1 + " arg2=" + message.arg2);
                if (WifiP2pServiceImpl.this.processMessageForP2pCollision(message, this)) {
                    return true;
                }
                switch (message.what) {
                    case 139271:
                        WifiP2pConfig config = (WifiP2pConfig) message.obj;
                        if (!P2pStateMachine.this.isConfigInvalid(config)) {
                            P2pStateMachine p2pStateMachine2 = P2pStateMachine.this;
                            p2pStateMachine2.logd("Inviting device : " + config.deviceAddress);
                            P2pStateMachine.this.mSavedPeerConfig = config;
                            if (!P2pStateMachine.this.mWifiNative.p2pInvite(P2pStateMachine.this.mGroup, config.deviceAddress)) {
                                P2pStateMachine.this.replyToMessage(message, 139272, 0);
                                break;
                            } else {
                                P2pStateMachine.this.mPeers.updateStatus(config.deviceAddress, 1);
                                P2pStateMachine.this.sendPeersChangedBroadcast();
                                P2pStateMachine.this.replyToMessage(message, 139273);
                                break;
                            }
                        } else {
                            P2pStateMachine p2pStateMachine3 = P2pStateMachine.this;
                            p2pStateMachine3.loge("Dropping connect request " + config);
                            P2pStateMachine.this.replyToMessage(message, 139272);
                            break;
                        }
                    case 139280:
                        P2pStateMachine p2pStateMachine4 = P2pStateMachine.this;
                        p2pStateMachine4.logd(getName() + " remove group");
                        if (WifiP2pServiceImpl.this.getWifiRepeaterEnabled()) {
                            WifiP2pServiceImpl.this.stopWifiRepeater(P2pStateMachine.this.mGroup);
                        }
                        P2pStateMachine.this.enableBTCoex();
                        if (!P2pStateMachine.this.mWifiNative.p2pGroupRemove(P2pStateMachine.this.mGroup.getInterface())) {
                            P2pStateMachine.this.handleGroupRemoved();
                            P2pStateMachine.this.transitionTo(P2pStateMachine.this.mInactiveState);
                            P2pStateMachine.this.replyToMessage(message, 139281, 0);
                            break;
                        } else {
                            P2pStateMachine.this.transitionTo(P2pStateMachine.this.mOngoingGroupRemovalState);
                            P2pStateMachine.this.replyToMessage(message, 139282);
                            break;
                        }
                    case 139326:
                        WpsInfo wps = (WpsInfo) message.obj;
                        int i = 139327;
                        if (wps != null) {
                            boolean ret = true;
                            if (wps.setup == 0) {
                                ret = P2pStateMachine.this.mWifiNative.startWpsPbc(P2pStateMachine.this.mGroup.getInterface(), null);
                            } else if (wps.pin == null) {
                                String pin = P2pStateMachine.this.mWifiNative.startWpsPinDisplay(P2pStateMachine.this.mGroup.getInterface(), null);
                                try {
                                    Integer.parseInt(pin);
                                    P2pStateMachine.this.notifyInvitationSent(pin, "any");
                                } catch (NumberFormatException e) {
                                    ret = false;
                                }
                            } else {
                                ret = P2pStateMachine.this.mWifiNative.startWpsPinKeypad(P2pStateMachine.this.mGroup.getInterface(), wps.pin);
                            }
                            P2pStateMachine p2pStateMachine5 = P2pStateMachine.this;
                            if (ret) {
                                i = 139328;
                            }
                            p2pStateMachine5.replyToMessage(message, i);
                            break;
                        } else {
                            P2pStateMachine.this.replyToMessage(message, 139327);
                            break;
                        }
                    case WifiP2pServiceImpl.DISABLE_P2P /*143377*/:
                        P2pStateMachine.this.sendMessage(139280);
                        P2pStateMachine.this.deferMessage(message);
                        break;
                    case WifiP2pServiceImpl.IPC_PRE_DHCP_ACTION /*143390*/:
                        P2pStateMachine.this.mWifiNative.setP2pPowerSave(P2pStateMachine.this.mGroup.getInterface(), false);
                        WifiP2pServiceImpl.this.mIpClient.completedPreDhcpAction();
                        break;
                    case WifiP2pServiceImpl.IPC_POST_DHCP_ACTION /*143391*/:
                        P2pStateMachine.this.mWifiNative.setP2pPowerSave(P2pStateMachine.this.mGroup.getInterface(), true);
                        break;
                    case WifiP2pServiceImpl.IPC_DHCP_RESULTS /*143392*/:
                        DhcpResults unused = WifiP2pServiceImpl.this.mDhcpResults = (DhcpResults) message.obj;
                        break;
                    case WifiP2pServiceImpl.IPC_PROVISIONING_SUCCESS /*143393*/:
                        P2pStateMachine p2pStateMachine6 = P2pStateMachine.this;
                        p2pStateMachine6.logd("mDhcpResults: " + WifiP2pServiceImpl.this.mDhcpResults);
                        P2pStateMachine.this.enableBTCoex();
                        if (WifiP2pServiceImpl.this.mDhcpResults != null) {
                            P2pStateMachine.this.setWifiP2pInfoOnGroupFormation(WifiP2pServiceImpl.this.mDhcpResults.serverAddress);
                        }
                        P2pStateMachine.this.sendP2pConnectionChangedBroadcast();
                        try {
                            String ifname = P2pStateMachine.this.mGroup.getInterface();
                            if (WifiP2pServiceImpl.this.mDhcpResults != null) {
                                WifiP2pServiceImpl.this.mNwService.addInterfaceToLocalNetwork(ifname, WifiP2pServiceImpl.this.mDhcpResults.getRoutes(ifname));
                                break;
                            }
                        } catch (RemoteException e2) {
                            P2pStateMachine p2pStateMachine7 = P2pStateMachine.this;
                            p2pStateMachine7.loge("Failed to add iface to local network " + e2);
                            break;
                        }
                        break;
                    case WifiP2pServiceImpl.IPC_PROVISIONING_FAILURE /*143394*/:
                        P2pStateMachine.this.loge("IP provisioning failed");
                        P2pStateMachine.this.mWifiNative.p2pGroupRemove(P2pStateMachine.this.mGroup.getInterface());
                        break;
                    case WifiP2pMonitor.P2P_DEVICE_LOST_EVENT:
                        if (message.obj == null) {
                            Log.e(WifiP2pServiceImpl.TAG, "Illegal argument(s)");
                            return false;
                        }
                        WifiP2pDevice device = (WifiP2pDevice) message.obj;
                        if (!P2pStateMachine.this.mGroup.contains(device)) {
                            return false;
                        }
                        P2pStateMachine p2pStateMachine8 = P2pStateMachine.this;
                        p2pStateMachine8.logd("Add device to lost list " + device);
                        P2pStateMachine.this.mPeersLostDuringConnection.updateSupplicantDetails(device);
                        return true;
                    case WifiP2pMonitor.P2P_GROUP_STARTED_EVENT:
                        P2pStateMachine.this.loge("Duplicate group creation event notice, ignore");
                        break;
                    case WifiP2pMonitor.P2P_GROUP_REMOVED_EVENT:
                        if (WifiP2pServiceImpl.this.getWifiRepeaterEnabled()) {
                            WifiP2pServiceImpl.this.stopWifiRepeater(P2pStateMachine.this.mGroup);
                        }
                        P2pStateMachine p2pStateMachine9 = P2pStateMachine.this;
                        p2pStateMachine9.logd(getName() + " group removed");
                        P2pStateMachine.this.enableBTCoex();
                        P2pStateMachine.this.handleGroupRemoved();
                        P2pStateMachine.this.transitionTo(P2pStateMachine.this.mInactiveState);
                        P2pStateMachine.this.mWifiNative.p2pFlush();
                        break;
                    case WifiP2pMonitor.P2P_INVITATION_RESULT_EVENT:
                        P2pStatus status = (P2pStatus) message.obj;
                        if (status != P2pStatus.SUCCESS) {
                            P2pStateMachine p2pStateMachine10 = P2pStateMachine.this;
                            p2pStateMachine10.loge("Invitation result " + status);
                            if (status == P2pStatus.UNKNOWN_P2P_GROUP) {
                                int netId = P2pStateMachine.this.mGroup.getNetworkId();
                                if (netId >= 0) {
                                    P2pStateMachine.this.logd("Remove unknown client from the list");
                                    boolean unused2 = P2pStateMachine.this.removeClientFromList(netId, P2pStateMachine.this.mSavedPeerConfig.deviceAddress, false);
                                    P2pStateMachine.this.sendMessage(139271, P2pStateMachine.this.mSavedPeerConfig);
                                    break;
                                }
                            }
                        }
                        break;
                    case WifiP2pMonitor.P2P_PROV_DISC_PBC_REQ_EVENT:
                    case WifiP2pMonitor.P2P_PROV_DISC_ENTER_PIN_EVENT:
                    case WifiP2pMonitor.P2P_PROV_DISC_SHOW_PIN_EVENT:
                        WifiP2pProvDiscEvent provDisc = (WifiP2pProvDiscEvent) message.obj;
                        P2pStateMachine.this.mSavedPeerConfig = new WifiP2pConfig();
                        if (!(provDisc == null || provDisc.device == null)) {
                            P2pStateMachine.this.mSavedPeerConfig.deviceAddress = provDisc.device.deviceAddress;
                        }
                        if (message.what == 147491) {
                            P2pStateMachine.this.mSavedPeerConfig.wps.setup = 2;
                        } else if (message.what == 147492) {
                            P2pStateMachine.this.mSavedPeerConfig.wps.setup = 1;
                            P2pStateMachine.this.mSavedPeerConfig.wps.pin = provDisc.pin;
                        } else {
                            P2pStateMachine.this.mSavedPeerConfig.wps.setup = 0;
                        }
                        P2pStateMachine p2pStateMachine11 = P2pStateMachine.this;
                        p2pStateMachine11.logd("mGroup.isGroupOwner()" + P2pStateMachine.this.mGroup.isGroupOwner());
                        if (P2pStateMachine.this.mGroup.isGroupOwner()) {
                            P2pStateMachine.this.logd("Device is GO, going to mUserAuthorizingJoinState");
                            P2pStateMachine.this.transitionTo(P2pStateMachine.this.mUserAuthorizingJoinState);
                            break;
                        }
                        break;
                    case WifiP2pMonitor.P2P_REMOVE_AND_REFORM_GROUP_EVENT:
                        Slog.d(WifiP2pServiceImpl.TAG, "Received event P2P_REMOVE_AND_REFORM_GROUP, remove P2P group");
                        handlP2pGroupRestart();
                        break;
                    case WifiP2pMonitor.AP_STA_DISCONNECTED_EVENT:
                        if (message.obj != null) {
                            WifiP2pDevice device2 = (WifiP2pDevice) message.obj;
                            String deviceAddress = device2.deviceAddress;
                            if (deviceAddress != null) {
                                P2pStateMachine.this.mPeers.updateStatus(deviceAddress, 3);
                                if (P2pStateMachine.this.mGroup.removeClient(deviceAddress)) {
                                    P2pStateMachine p2pStateMachine12 = P2pStateMachine.this;
                                    p2pStateMachine12.logd("Removed client " + deviceAddress);
                                    if (WifiP2pServiceImpl.this.mAutonomousGroup || !P2pStateMachine.this.mGroup.isClientListEmpty()) {
                                        P2pStateMachine.this.sendP2pConnectionChangedBroadcast();
                                    } else {
                                        P2pStateMachine.this.logd("Client list empty, remove non-persistent p2p group");
                                        P2pStateMachine.this.mWifiNative.p2pGroupRemove(P2pStateMachine.this.mGroup.getInterface());
                                    }
                                } else {
                                    P2pStateMachine p2pStateMachine13 = P2pStateMachine.this;
                                    p2pStateMachine13.logd("Failed to remove client " + deviceAddress);
                                    Iterator<WifiP2pDevice> it = P2pStateMachine.this.mGroup.getClientList().iterator();
                                    while (it.hasNext()) {
                                        P2pStateMachine p2pStateMachine14 = P2pStateMachine.this;
                                        p2pStateMachine14.logd("client " + it.next().deviceAddress);
                                    }
                                }
                                P2pStateMachine.this.sendPeersChangedBroadcast();
                                P2pStateMachine p2pStateMachine15 = P2pStateMachine.this;
                                p2pStateMachine15.logd(getName() + " ap sta disconnected");
                            } else {
                                P2pStateMachine p2pStateMachine16 = P2pStateMachine.this;
                                p2pStateMachine16.loge("Disconnect on unknown device: " + device2);
                            }
                            WifiP2pServiceImpl.this.handleClientDisconnect(P2pStateMachine.this.mGroup);
                            break;
                        } else {
                            Log.e(WifiP2pServiceImpl.TAG, "Illegal argument(s)");
                            break;
                        }
                        break;
                    case WifiP2pMonitor.AP_STA_CONNECTED_EVENT:
                        if (message.obj != null) {
                            String deviceAddress2 = ((WifiP2pDevice) message.obj).deviceAddress;
                            P2pStateMachine.this.mWifiNative.setP2pGroupIdle(P2pStateMachine.this.mGroup.getInterface(), 0);
                            if (deviceAddress2 != null) {
                                if (P2pStateMachine.this.mPeers.get(deviceAddress2) != null) {
                                    P2pStateMachine.this.mGroup.addClient(P2pStateMachine.this.mPeers.get(deviceAddress2));
                                } else {
                                    P2pStateMachine.this.mGroup.addClient(deviceAddress2);
                                }
                                P2pStateMachine.this.mPeers.updateStatus(deviceAddress2, 0);
                                P2pStateMachine p2pStateMachine17 = P2pStateMachine.this;
                                p2pStateMachine17.logd(getName() + " ap sta connected");
                                P2pStateMachine.this.sendPeersChangedBroadcast();
                            } else {
                                P2pStateMachine.this.loge("Connect on null device address, ignore");
                            }
                            P2pStateMachine.this.sendP2pConnectionChangedBroadcast();
                            WifiP2pServiceImpl.this.handleClientConnect(P2pStateMachine.this.mGroup);
                            break;
                        } else {
                            Log.e(WifiP2pServiceImpl.TAG, "Illegal argument(s)");
                            break;
                        }
                    case 196612:
                        DhcpResults dhcpResults = (DhcpResults) message.obj;
                        P2pStateMachine.this.enableBTCoex();
                        if (message.arg1 == 1 && dhcpResults != null) {
                            P2pStateMachine p2pStateMachine18 = P2pStateMachine.this;
                            p2pStateMachine18.logd("DhcpResults: " + dhcpResults);
                            P2pStateMachine.this.setWifiP2pInfoOnGroupFormation(dhcpResults.serverAddress);
                            P2pStateMachine.this.sendP2pConnectionChangedBroadcast();
                            P2pStateMachine.this.mWifiNative.setP2pPowerSave(P2pStateMachine.this.mGroup.getInterface(), true);
                            try {
                                if (!WifiP2pServiceHisiExt.hisiWifiEnabled()) {
                                    String iface = P2pStateMachine.this.mGroup.getInterface();
                                    WifiP2pServiceImpl.this.mNwService.addInterfaceToLocalNetwork(iface, dhcpResults.getRoutes(iface));
                                    break;
                                } else {
                                    WifiP2pServiceImpl.this.mNwService.addInterfaceToLocalNetwork("wlan0", dhcpResults.getRoutes("wlan0"));
                                    break;
                                }
                            } catch (RemoteException e3) {
                                P2pStateMachine p2pStateMachine19 = P2pStateMachine.this;
                                p2pStateMachine19.loge("Failed to add iface to local network " + e3);
                                break;
                            } catch (IllegalStateException e4) {
                                P2pStateMachine p2pStateMachine20 = P2pStateMachine.this;
                                p2pStateMachine20.loge("Failed to add iface to local network " + e4);
                                break;
                            } catch (IllegalArgumentException e5) {
                                P2pStateMachine p2pStateMachine21 = P2pStateMachine.this;
                                p2pStateMachine21.loge("Failed to add iface to local network: " + e5);
                                break;
                            }
                        } else {
                            P2pStateMachine.this.loge("DHCP failed");
                            P2pStateMachine.this.mWifiNative.p2pGroupRemove(P2pStateMachine.this.mGroup.getInterface());
                            break;
                        }
                    default:
                        return WifiP2pServiceImpl.this.handleGroupCreatedStateExMessage(message);
                }
                return true;
            }

            public void exit() {
                P2pStateMachine.this.updateThisDevice(3);
                P2pStateMachine.this.resetWifiP2pInfo();
                WifiP2pServiceImpl.this.mNetworkInfo.setDetailedState(NetworkInfo.DetailedState.DISCONNECTED, null, null);
                if (WifiP2pServiceHisiExt.hisiWifiEnabled()) {
                    WifiP2pServiceImpl.this.mWifiP2pServiceHisiExt.sendP2pNetworkChangedBroadcast();
                }
                P2pStateMachine.this.sendP2pConnectionChangedBroadcast();
                WifiP2pServiceImpl.this.notifyP2pState(WifiCommonUtils.STATE_DISCONNECTED);
            }
        }

        class GroupCreatingState extends State {
            GroupCreatingState() {
            }

            public void enter() {
                P2pStateMachine.this.logd(getName());
                P2pStateMachine.this.sendMessageDelayed(P2pStateMachine.this.obtainMessage(WifiP2pServiceImpl.GROUP_CREATING_TIMED_OUT, WifiP2pServiceImpl.access$5404(), 0), 120000);
            }

            public boolean processMessage(Message message) {
                P2pStateMachine p2pStateMachine = P2pStateMachine.this;
                p2pStateMachine.logd(getName() + message.toString());
                if (WifiP2pServiceImpl.this.processMessageForP2pCollision(message, this)) {
                    return true;
                }
                boolean ret = true;
                switch (message.what) {
                    case 139265:
                        P2pStateMachine.this.replyToMessage(message, 139266, 2);
                        break;
                    case 139274:
                        P2pStateMachine.this.mWifiNative.p2pCancelConnect();
                        P2pStateMachine.this.handleGroupCreationFailure();
                        P2pStateMachine.this.transitionTo(P2pStateMachine.this.mInactiveState);
                        P2pStateMachine.this.replyToMessage(message, 139276);
                        break;
                    case WifiP2pServiceImpl.GROUP_CREATING_TIMED_OUT /*143361*/:
                        if (WifiP2pServiceImpl.sGroupCreatingTimeoutIndex == message.arg1) {
                            P2pStateMachine.this.logd("Group negotiation timed out");
                            P2pStateMachine.this.handleGroupCreationFailure();
                            P2pStateMachine.this.transitionTo(P2pStateMachine.this.mInactiveState);
                            break;
                        }
                        break;
                    case WifiP2pMonitor.P2P_DEVICE_LOST_EVENT:
                        if (message.obj != null) {
                            WifiP2pDevice device = (WifiP2pDevice) message.obj;
                            if (P2pStateMachine.this.mSavedPeerConfig.deviceAddress.equals(device.deviceAddress)) {
                                P2pStateMachine p2pStateMachine2 = P2pStateMachine.this;
                                p2pStateMachine2.logd("Add device to lost list " + device);
                                P2pStateMachine.this.mPeersLostDuringConnection.updateSupplicantDetails(device);
                                break;
                            } else {
                                P2pStateMachine p2pStateMachine3 = P2pStateMachine.this;
                                p2pStateMachine3.logd("mSavedPeerConfig " + P2pStateMachine.this.mSavedPeerConfig.deviceAddress + "device " + device.deviceAddress);
                                ret = false;
                                break;
                            }
                        } else {
                            Log.e(WifiP2pServiceImpl.TAG, "Illegal argument(s)");
                            break;
                        }
                    case WifiP2pMonitor.P2P_GO_NEGOTIATION_SUCCESS_EVENT:
                        boolean unused = WifiP2pServiceImpl.this.mAutonomousGroup = false;
                        P2pStateMachine.this.transitionTo(P2pStateMachine.this.mGroupNegotiationState);
                        break;
                    default:
                        ret = false;
                        break;
                }
                return ret;
            }
        }

        class GroupNegotiationState extends State {
            GroupNegotiationState() {
            }

            public void enter() {
                P2pStateMachine.this.logd(getName());
                WifiP2pServiceImpl.this.sendP2pConnectingStateBroadcast();
                boolean unused = P2pStateMachine.this.mPendingReformGroupIndication = false;
            }

            /* JADX WARNING: Can't fix incorrect switch cases order */
            public boolean processMessage(Message message) {
                P2pStateMachine p2pStateMachine = P2pStateMachine.this;
                p2pStateMachine.logd(getName() + message.toString());
                int i = message.what;
                if (i != 139280) {
                    if (i == 147479) {
                        P2pStateMachine p2pStateMachine2 = P2pStateMachine.this;
                        p2pStateMachine2.logd(getName() + " deal with P2P_GO_NEGOTIATION_REQUEST_EVENT");
                        P2pStateMachine.this.deferMessage(message);
                        P2pStateMachine.this.transitionTo(P2pStateMachine.this.mInactiveState);
                    } else if (i == 147488) {
                        P2pStatus status = (P2pStatus) message.obj;
                        if (status != P2pStatus.SUCCESS) {
                            P2pStateMachine p2pStateMachine3 = P2pStateMachine.this;
                            p2pStateMachine3.loge("Invitation result " + status);
                            if (status == P2pStatus.UNKNOWN_P2P_GROUP) {
                                int netId = P2pStateMachine.this.mSavedPeerConfig.netId;
                                if (netId >= 0) {
                                    P2pStateMachine.this.logd("Remove unknown client from the list");
                                    boolean unused = P2pStateMachine.this.removeClientFromList(netId, P2pStateMachine.this.mSavedPeerConfig.deviceAddress, true);
                                }
                                P2pStateMachine.this.mSavedPeerConfig.netId = -2;
                                P2pStateMachine.this.p2pConnectWithPinDisplay(P2pStateMachine.this.mSavedPeerConfig);
                            } else if (status == P2pStatus.INFORMATION_IS_CURRENTLY_UNAVAILABLE) {
                                P2pStateMachine.this.mSavedPeerConfig.netId = -2;
                                P2pStateMachine.this.p2pConnectWithPinDisplay(P2pStateMachine.this.mSavedPeerConfig);
                            } else if (status == P2pStatus.NO_COMMON_CHANNEL) {
                                P2pStateMachine.this.transitionTo(P2pStateMachine.this.mFrequencyConflictState);
                            } else {
                                P2pStateMachine.this.handleGroupCreationFailure();
                                P2pStateMachine.this.transitionTo(P2pStateMachine.this.mInactiveState);
                            }
                        }
                    } else if (i != 147496) {
                        switch (i) {
                            case WifiP2pMonitor.P2P_GO_NEGOTIATION_SUCCESS_EVENT:
                            case WifiP2pMonitor.P2P_GROUP_FORMATION_SUCCESS_EVENT:
                                P2pStateMachine p2pStateMachine4 = P2pStateMachine.this;
                                p2pStateMachine4.logd(getName() + " go success");
                                break;
                            case WifiP2pMonitor.P2P_GO_NEGOTIATION_FAILURE_EVENT:
                                if (((P2pStatus) message.obj) == P2pStatus.NO_COMMON_CHANNEL) {
                                    P2pStateMachine.this.transitionTo(P2pStateMachine.this.mFrequencyConflictState);
                                    break;
                                }
                            case WifiP2pMonitor.P2P_GROUP_FORMATION_FAILURE_EVENT:
                                if (((P2pStatus) message.obj) == P2pStatus.NO_COMMON_CHANNEL) {
                                    P2pStateMachine.this.transitionTo(P2pStateMachine.this.mFrequencyConflictState);
                                    break;
                                }
                                break;
                            case WifiP2pMonitor.P2P_GROUP_STARTED_EVENT:
                                if (message.obj != null) {
                                    WifiP2pGroup unused2 = P2pStateMachine.this.mGroup = (WifiP2pGroup) message.obj;
                                    P2pStateMachine p2pStateMachine5 = P2pStateMachine.this;
                                    p2pStateMachine5.logd(getName() + " group started");
                                    if (P2pStateMachine.this.mGroup.isGroupOwner() && WifiP2pServiceImpl.EMPTY_DEVICE_ADDRESS.equals(P2pStateMachine.this.mGroup.getOwner().deviceAddress)) {
                                        P2pStateMachine.this.mGroup.getOwner().deviceAddress = WifiP2pServiceImpl.this.mThisDevice.deviceAddress;
                                    }
                                    if (P2pStateMachine.this.mGroup.getNetworkId() == -2) {
                                        P2pStateMachine.this.updatePersistentNetworks(WifiP2pServiceImpl.RELOAD.booleanValue());
                                        P2pStateMachine.this.mGroup.setNetworkId(P2pStateMachine.this.mGroups.getNetworkId(P2pStateMachine.this.mGroup.getOwner().deviceAddress, P2pStateMachine.this.mGroup.getNetworkName()));
                                    }
                                    if (P2pStateMachine.this.mGroup.isGroupOwner()) {
                                        WifiP2pServiceImpl.this.sendGroupConfigInfo(P2pStateMachine.this.mGroup);
                                        if (!WifiP2pServiceImpl.this.mAutonomousGroup) {
                                            P2pStateMachine.this.mWifiNative.setP2pGroupIdle(P2pStateMachine.this.mGroup.getInterface(), 10);
                                        }
                                        if (WifiP2pServiceHisiExt.hisiWifiEnabled()) {
                                            P2pStateMachine.this.startDhcpServer("wlan0");
                                        } else {
                                            P2pStateMachine.this.startDhcpServer(P2pStateMachine.this.mGroup.getInterface());
                                        }
                                    } else {
                                        if (!WifiP2pServiceImpl.this.getMagicLinkDeviceFlag()) {
                                            P2pStateMachine.this.mWifiNative.setP2pGroupIdle(P2pStateMachine.this.mGroup.getInterface(), 10);
                                            if (WifiP2pServiceHisiExt.hisiWifiEnabled()) {
                                                WifiP2pServiceImpl.this.startIpClient("wlan0");
                                            } else {
                                                WifiP2pServiceImpl.this.startIpClient(P2pStateMachine.this.mGroup.getInterface());
                                            }
                                            if (P2pStateMachine.this.mWifiInjector == null) {
                                                WifiInjector unused3 = P2pStateMachine.this.mWifiInjector = WifiInjector.getInstance();
                                            }
                                            P2pStateMachine.this.mWifiInjector.getWifiNative().setBluetoothCoexistenceMode(P2pStateMachine.this.mInterfaceName, 1);
                                            boolean unused4 = P2pStateMachine.this.mIsBTCoexDisabled = true;
                                        }
                                        WifiP2pDevice groupOwner = P2pStateMachine.this.mGroup.getOwner();
                                        if (groupOwner != null) {
                                            WifiP2pDevice peer = P2pStateMachine.this.mPeers.get(groupOwner.deviceAddress);
                                            if (peer != null) {
                                                groupOwner.updateSupplicantDetails(peer);
                                                P2pStateMachine.this.mPeers.updateStatus(groupOwner.deviceAddress, 0);
                                                P2pStateMachine.this.sendPeersChangedBroadcast();
                                            } else {
                                                if (groupOwner != null && !WifiP2pServiceImpl.EMPTY_DEVICE_ADDRESS.equals(groupOwner.deviceAddress)) {
                                                    Matcher match = Pattern.compile("([0-9a-f]{2}:){5}[0-9a-f]{2}").matcher(groupOwner.deviceAddress);
                                                    Log.e(WifiP2pServiceImpl.TAG, "try to judge groupOwner is valid or not");
                                                    if (match.find()) {
                                                        groupOwner.primaryDeviceType = "10-0050F204-5";
                                                        P2pStateMachine.this.mPeers.updateSupplicantDetails(groupOwner);
                                                        P2pStateMachine.this.mPeers.updateStatus(groupOwner.deviceAddress, 0);
                                                        P2pStateMachine.this.sendPeersChangedBroadcast();
                                                    }
                                                }
                                                P2pStateMachine p2pStateMachine6 = P2pStateMachine.this;
                                                p2pStateMachine6.logw("Unknown group owner " + groupOwner);
                                            }
                                        } else {
                                            P2pStateMachine.this.loge("Group owner is null.");
                                        }
                                    }
                                    P2pStateMachine.this.transitionTo(P2pStateMachine.this.mGroupCreatedState);
                                    break;
                                } else {
                                    Log.e(WifiP2pServiceImpl.TAG, "Illegal argument(s)");
                                    break;
                                }
                            case WifiP2pMonitor.P2P_GROUP_REMOVED_EVENT:
                                P2pStateMachine p2pStateMachine7 = P2pStateMachine.this;
                                p2pStateMachine7.logd(getName() + " go failure");
                                P2pStateMachine.this.handleGroupCreationFailure();
                                WifiP2pServiceImpl.this.sendP2pFailStateBroadcast();
                                P2pStateMachine.this.transitionTo(P2pStateMachine.this.mInactiveState);
                                break;
                            default:
                                return WifiP2pServiceImpl.this.handleGroupNegotiationStateExMessage(message);
                        }
                    } else {
                        P2pStateMachine.this.logd("P2P_REMOVE_AND_REFORM_GROUP_EVENT event received in GroupNegotiationState state");
                        boolean unused5 = P2pStateMachine.this.mPendingReformGroupIndication = true;
                    }
                } else if (!WifiP2pServiceImpl.this.getWifiRepeaterEnabled()) {
                    return false;
                } else {
                    P2pStateMachine.this.deferMessage(message);
                }
                return true;
            }
        }

        class InactiveState extends State {
            InactiveState() {
            }

            public void enter() {
                P2pStateMachine.this.logd(getName());
                boolean unused = WifiP2pServiceImpl.this.mIsInvite = false;
                WifiP2pServiceImpl.this.setmMagicLinkDeviceFlag(false);
                P2pStateMachine.this.mSavedPeerConfig.invalidate();
            }

            public boolean processMessage(Message message) {
                boolean ret;
                P2pStateMachine p2pStateMachine = P2pStateMachine.this;
                p2pStateMachine.logd(getName() + message.what);
                if (WifiP2pServiceImpl.this.processMessageForP2pCollision(message, this)) {
                    return true;
                }
                switch (message.what) {
                    case 139268:
                        if (P2pStateMachine.this.mWifiNative.p2pStopFind()) {
                            P2pStateMachine.this.mWifiNative.p2pFlush();
                            String unused = WifiP2pServiceImpl.this.mServiceDiscReqId = null;
                            P2pStateMachine.this.replyToMessage(message, 139270);
                        } else {
                            P2pStateMachine.this.replyToMessage(message, 139269, 0);
                        }
                        WifiP2pServiceImpl.this.handleP2pStopFind(message.sendingUid);
                        break;
                    case 139271:
                        WifiP2pServiceImpl.this.setmMagicLinkDeviceFlag(false);
                        P2pStateMachine p2pStateMachine2 = P2pStateMachine.this;
                        p2pStateMachine2.logd(getName() + " sending connect");
                        WifiP2pConfig config = (WifiP2pConfig) message.obj;
                        if (!P2pStateMachine.this.isConfigInvalid(config)) {
                            boolean unused2 = WifiP2pServiceImpl.this.mAutonomousGroup = false;
                            P2pStateMachine.this.mWifiNative.p2pStopFind();
                            if (P2pStateMachine.this.reinvokePersistentGroup(config)) {
                                P2pStateMachine.this.transitionTo(P2pStateMachine.this.mGroupNegotiationState);
                            } else {
                                P2pStateMachine.this.transitionTo(P2pStateMachine.this.mProvisionDiscoveryState);
                            }
                            P2pStateMachine.this.mSavedPeerConfig = config;
                            P2pStateMachine.this.mPeers.updateStatus(P2pStateMachine.this.mSavedPeerConfig.deviceAddress, 1);
                            P2pStateMachine.this.sendPeersChangedBroadcast();
                            P2pStateMachine.this.replyToMessage(message, 139273);
                            break;
                        } else {
                            P2pStateMachine p2pStateMachine3 = P2pStateMachine.this;
                            p2pStateMachine3.loge("Dropping connect requeset " + config);
                            P2pStateMachine.this.replyToMessage(message, 139272);
                            break;
                        }
                    case 139277:
                        boolean unused3 = WifiP2pServiceImpl.this.mAutonomousGroup = true;
                        if (message.arg1 == -2) {
                            int netId = P2pStateMachine.this.mGroups.getNetworkId(WifiP2pServiceImpl.this.mThisDevice.deviceAddress);
                            if (netId != -1) {
                                ret = P2pStateMachine.this.mWifiNative.p2pGroupAdd(netId);
                            } else {
                                ret = P2pStateMachine.this.mWifiNative.p2pGroupAdd(true);
                            }
                        } else {
                            ret = P2pStateMachine.this.mWifiNative.p2pGroupAdd(false);
                        }
                        if (!ret) {
                            P2pStateMachine.this.replyToMessage(message, 139278, 0);
                            break;
                        } else {
                            P2pStateMachine.this.replyToMessage(message, 139279);
                            P2pStateMachine.this.transitionTo(P2pStateMachine.this.mGroupNegotiationState);
                            break;
                        }
                    case 139329:
                        P2pStateMachine p2pStateMachine4 = P2pStateMachine.this;
                        p2pStateMachine4.logd(getName() + " start listen mode");
                        P2pStateMachine.this.mWifiNative.p2pFlush();
                        if (!P2pStateMachine.this.mWifiNative.p2pExtListen(true, 500, 500)) {
                            P2pStateMachine.this.replyToMessage(message, 139330);
                            break;
                        } else {
                            P2pStateMachine.this.replyToMessage(message, 139331);
                            break;
                        }
                    case 139332:
                        P2pStateMachine p2pStateMachine5 = P2pStateMachine.this;
                        p2pStateMachine5.logd(getName() + " stop listen mode");
                        if (P2pStateMachine.this.mWifiNative.p2pExtListen(false, 0, 0)) {
                            P2pStateMachine.this.replyToMessage(message, 139334);
                        } else {
                            P2pStateMachine.this.replyToMessage(message, 139333);
                        }
                        P2pStateMachine.this.mWifiNative.p2pFlush();
                        break;
                    case 139335:
                        if (message.obj != null) {
                            Bundle p2pChannels = (Bundle) message.obj;
                            int lc = p2pChannels.getInt("lc", 0);
                            int oc = p2pChannels.getInt("oc", 0);
                            P2pStateMachine p2pStateMachine6 = P2pStateMachine.this;
                            p2pStateMachine6.logd(getName() + " set listen and operating channel");
                            if (!P2pStateMachine.this.mWifiNative.p2pSetChannel(lc, oc)) {
                                P2pStateMachine.this.replyToMessage(message, 139336);
                                break;
                            } else {
                                P2pStateMachine.this.replyToMessage(message, 139337);
                                break;
                            }
                        } else {
                            Log.e(WifiP2pServiceImpl.TAG, "Illegal arguments(s)");
                            break;
                        }
                    case 139342:
                        String handoverSelect = null;
                        if (message.obj != null) {
                            handoverSelect = ((Bundle) message.obj).getString("android.net.wifi.p2p.EXTRA_HANDOVER_MESSAGE");
                        }
                        if (handoverSelect != null && P2pStateMachine.this.mWifiNative.initiatorReportNfcHandover(handoverSelect)) {
                            P2pStateMachine.this.replyToMessage(message, 139344);
                            P2pStateMachine.this.transitionTo(P2pStateMachine.this.mGroupCreatingState);
                            break;
                        } else {
                            P2pStateMachine.this.replyToMessage(message, 139345);
                            break;
                        }
                    case 139343:
                        String handoverRequest = null;
                        if (message.obj != null) {
                            handoverRequest = ((Bundle) message.obj).getString("android.net.wifi.p2p.EXTRA_HANDOVER_MESSAGE");
                        }
                        if (handoverRequest != null && P2pStateMachine.this.mWifiNative.responderReportNfcHandover(handoverRequest)) {
                            P2pStateMachine.this.replyToMessage(message, 139344);
                            P2pStateMachine.this.transitionTo(P2pStateMachine.this.mGroupCreatingState);
                            break;
                        } else {
                            P2pStateMachine.this.replyToMessage(message, 139345);
                            break;
                        }
                        break;
                    case WifiP2pMonitor.P2P_GO_NEGOTIATION_REQUEST_EVENT:
                        WifiP2pConfig config2 = (WifiP2pConfig) message.obj;
                        if (!P2pStateMachine.this.isConfigInvalid(config2)) {
                            P2pStateMachine.this.mSavedPeerConfig = config2;
                            boolean unused4 = WifiP2pServiceImpl.this.mAutonomousGroup = false;
                            boolean unused5 = WifiP2pServiceImpl.this.mJoinExistingGroup = false;
                            P2pStateMachine.this.transitionTo(P2pStateMachine.this.mUserAuthorizingNegotiationRequestState);
                            break;
                        } else {
                            P2pStateMachine p2pStateMachine7 = P2pStateMachine.this;
                            p2pStateMachine7.loge("Dropping GO neg request " + config2);
                            break;
                        }
                    case WifiP2pMonitor.P2P_GROUP_STARTED_EVENT:
                        if (message.obj != null) {
                            WifiP2pGroup unused6 = P2pStateMachine.this.mGroup = (WifiP2pGroup) message.obj;
                            P2pStateMachine p2pStateMachine8 = P2pStateMachine.this;
                            p2pStateMachine8.logd(getName() + " group started");
                            if (P2pStateMachine.this.mGroup.isGroupOwner() && WifiP2pServiceImpl.EMPTY_DEVICE_ADDRESS.equals(P2pStateMachine.this.mGroup.getOwner().deviceAddress)) {
                                P2pStateMachine.this.mGroup.getOwner().deviceAddress = WifiP2pServiceImpl.this.mThisDevice.deviceAddress;
                            }
                            if (P2pStateMachine.this.mGroup.getNetworkId() != -2) {
                                P2pStateMachine p2pStateMachine9 = P2pStateMachine.this;
                                p2pStateMachine9.loge("Unexpected group creation, remove " + P2pStateMachine.this.mGroup);
                                P2pStateMachine.this.mWifiNative.p2pGroupRemove(P2pStateMachine.this.mGroup.getInterface());
                                break;
                            } else {
                                boolean unused7 = WifiP2pServiceImpl.this.mAutonomousGroup = false;
                                P2pStateMachine.this.deferMessage(message);
                                P2pStateMachine.this.transitionTo(P2pStateMachine.this.mGroupNegotiationState);
                                break;
                            }
                        } else {
                            Log.e(WifiP2pServiceImpl.TAG, "Invalid argument(s)");
                            break;
                        }
                    case WifiP2pMonitor.P2P_INVITATION_RECEIVED_EVENT:
                        if (message.obj != null) {
                            WifiP2pGroup group = (WifiP2pGroup) message.obj;
                            WifiP2pDevice owner = group.getOwner();
                            if (owner == null) {
                                int id = group.getNetworkId();
                                if (id >= 0) {
                                    String addr = P2pStateMachine.this.mGroups.getOwnerAddr(id);
                                    if (addr == null) {
                                        P2pStateMachine.this.loge("Ignored invitation from null owner");
                                        break;
                                    } else {
                                        group.setOwner(new WifiP2pDevice(addr));
                                        owner = group.getOwner();
                                    }
                                } else {
                                    P2pStateMachine.this.loge("Ignored invitation from null owner");
                                    break;
                                }
                            }
                            WifiP2pConfig config3 = new WifiP2pConfig();
                            config3.deviceAddress = group.getOwner().deviceAddress;
                            if (!P2pStateMachine.this.isConfigInvalid(config3)) {
                                P2pStateMachine.this.mSavedPeerConfig = config3;
                                if (owner != null) {
                                    WifiP2pDevice wifiP2pDevice = P2pStateMachine.this.mPeers.get(owner.deviceAddress);
                                    WifiP2pDevice owner2 = wifiP2pDevice;
                                    if (wifiP2pDevice != null) {
                                        if (owner2.wpsPbcSupported()) {
                                            P2pStateMachine.this.mSavedPeerConfig.wps.setup = 0;
                                        } else if (owner2.wpsKeypadSupported()) {
                                            P2pStateMachine.this.mSavedPeerConfig.wps.setup = 2;
                                        } else if (owner2.wpsDisplaySupported()) {
                                            P2pStateMachine.this.mSavedPeerConfig.wps.setup = 1;
                                        }
                                    }
                                }
                                boolean unused8 = WifiP2pServiceImpl.this.mAutonomousGroup = false;
                                boolean unused9 = WifiP2pServiceImpl.this.mJoinExistingGroup = true;
                                boolean unused10 = WifiP2pServiceImpl.this.mIsInvite = true;
                                P2pStateMachine.this.transitionTo(P2pStateMachine.this.mUserAuthorizingInviteRequestState);
                                break;
                            } else {
                                P2pStateMachine p2pStateMachine10 = P2pStateMachine.this;
                                p2pStateMachine10.loge("Dropping invitation request " + config3);
                                break;
                            }
                        } else {
                            Log.e(WifiP2pServiceImpl.TAG, "Invalid argument(s)");
                            break;
                        }
                    case WifiP2pMonitor.P2P_PROV_DISC_PBC_REQ_EVENT:
                    case WifiP2pMonitor.P2P_PROV_DISC_ENTER_PIN_EVENT:
                        break;
                    case WifiP2pMonitor.P2P_PROV_DISC_SHOW_PIN_EVENT:
                        if (message.obj != null) {
                            WifiP2pProvDiscEvent provDisc = (WifiP2pProvDiscEvent) message.obj;
                            WifiP2pDevice device = provDisc.device;
                            if (device != null) {
                                P2pStateMachine.this.notifyP2pProvDiscShowPinRequest(provDisc.pin, device.deviceAddress);
                                P2pStateMachine.this.mPeers.updateStatus(device.deviceAddress, 1);
                                P2pStateMachine.this.sendPeersChangedBroadcast();
                                P2pStateMachine.this.transitionTo(P2pStateMachine.this.mGroupNegotiationState);
                                break;
                            } else {
                                P2pStateMachine.this.loge("Device entry is null");
                                break;
                            }
                        } else {
                            Log.e(WifiP2pServiceImpl.TAG, "Illegal argument(s)");
                            break;
                        }
                    default:
                        if (message.what != HwWifiServiceFactory.getHwConstantUtils().getWifiP2pCreateGroupPskVal()) {
                            return WifiP2pServiceImpl.this.handleInactiveStateMessage(message);
                        }
                        boolean unused11 = WifiP2pServiceImpl.this.mCreateWifiBridge = true;
                        return WifiP2pServiceImpl.this.handleInactiveStateMessage(message);
                }
                return true;
            }
        }

        class OngoingGroupRemovalState extends State {
            OngoingGroupRemovalState() {
            }

            public void enter() {
                P2pStateMachine.this.logd(getName());
            }

            public boolean processMessage(Message message) {
                P2pStateMachine p2pStateMachine = P2pStateMachine.this;
                p2pStateMachine.logd(getName() + message.toString());
                if (message.what == 139280) {
                    P2pStateMachine.this.replyToMessage(message, 139282);
                } else if (message.what != HwWifiServiceFactory.getHwConstantUtils().getWifiP2pCreateGroupPskVal()) {
                    return WifiP2pServiceImpl.this.handleOngoingGroupRemovalStateExMessage(message);
                } else {
                    if (!WifiP2pServiceImpl.this.getWifiRepeaterEnabled()) {
                        P2pStateMachine.this.deferMessage(message);
                    }
                }
                return true;
            }
        }

        class P2pDisabledState extends State {
            P2pDisabledState() {
            }

            public void enter() {
                P2pStateMachine.this.logd(getName());
            }

            public boolean processMessage(Message message) {
                P2pStateMachine p2pStateMachine = P2pStateMachine.this;
                p2pStateMachine.logd(getName() + message.toString());
                if (message.what != 143376) {
                    return false;
                }
                if (!P2pStateMachine.this.mIsWifiEnabled) {
                    Log.e(WifiP2pServiceImpl.TAG, "Ignore P2P enable since wifi is disabled");
                } else {
                    String unused = P2pStateMachine.this.mInterfaceName = P2pStateMachine.this.mWifiNative.setupInterface(
                    /*  JADX ERROR: Method code generation error
                        jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x004b: INVOKE  (wrap: com.android.server.wifi.p2p.WifiP2pServiceImpl$P2pStateMachine
                          0x0036: IGET  (r0v5 com.android.server.wifi.p2p.WifiP2pServiceImpl$P2pStateMachine) = (r4v0 'this' com.android.server.wifi.p2p.WifiP2pServiceImpl$P2pStateMachine$P2pDisabledState A[THIS]) com.android.server.wifi.p2p.WifiP2pServiceImpl.P2pStateMachine.P2pDisabledState.this$1 com.android.server.wifi.p2p.WifiP2pServiceImpl$P2pStateMachine), (wrap: java.lang.String
                          0x0047: INVOKE  (r1v5 java.lang.String) = (wrap: com.android.server.wifi.p2p.WifiP2pNative
                          0x003a: IGET  (r1v4 com.android.server.wifi.p2p.WifiP2pNative) = (wrap: com.android.server.wifi.p2p.WifiP2pServiceImpl$P2pStateMachine
                          0x0038: IGET  (r1v3 com.android.server.wifi.p2p.WifiP2pServiceImpl$P2pStateMachine) = (r4v0 'this' com.android.server.wifi.p2p.WifiP2pServiceImpl$P2pStateMachine$P2pDisabledState A[THIS]) com.android.server.wifi.p2p.WifiP2pServiceImpl.P2pStateMachine.P2pDisabledState.this$1 com.android.server.wifi.p2p.WifiP2pServiceImpl$P2pStateMachine) com.android.server.wifi.p2p.WifiP2pServiceImpl.P2pStateMachine.mWifiNative com.android.server.wifi.p2p.WifiP2pNative), (wrap: com.android.server.wifi.p2p.-$$Lambda$WifiP2pServiceImpl$P2pStateMachine$P2pDisabledState$13XANUNRJEt7WjtJr5tKTd2g-PM
                          0x003e: CONSTRUCTOR  (r2v2 com.android.server.wifi.p2p.-$$Lambda$WifiP2pServiceImpl$P2pStateMachine$P2pDisabledState$13XANUNRJEt7WjtJr5tKTd2g-PM) = (r4v0 'this' com.android.server.wifi.p2p.WifiP2pServiceImpl$P2pStateMachine$P2pDisabledState A[THIS]) com.android.server.wifi.p2p.-$$Lambda$WifiP2pServiceImpl$P2pStateMachine$P2pDisabledState$13XANUNRJEt7WjtJr5tKTd2g-PM.<init>(com.android.server.wifi.p2p.WifiP2pServiceImpl$P2pStateMachine$P2pDisabledState):void CONSTRUCTOR), (wrap: android.os.Handler
                          0x0043: INVOKE  (r3v1 android.os.Handler) = (wrap: com.android.server.wifi.p2p.WifiP2pServiceImpl$P2pStateMachine
                          0x0041: IGET  (r3v0 com.android.server.wifi.p2p.WifiP2pServiceImpl$P2pStateMachine) = (r4v0 'this' com.android.server.wifi.p2p.WifiP2pServiceImpl$P2pStateMachine$P2pDisabledState A[THIS]) com.android.server.wifi.p2p.WifiP2pServiceImpl.P2pStateMachine.P2pDisabledState.this$1 com.android.server.wifi.p2p.WifiP2pServiceImpl$P2pStateMachine) com.android.server.wifi.p2p.WifiP2pServiceImpl.P2pStateMachine.getHandler():android.os.Handler type: VIRTUAL) com.android.server.wifi.p2p.WifiP2pNative.setupInterface(com.android.server.wifi.HalDeviceManager$InterfaceDestroyedListener, android.os.Handler):java.lang.String type: VIRTUAL) com.android.server.wifi.p2p.WifiP2pServiceImpl.P2pStateMachine.access$1902(com.android.server.wifi.p2p.WifiP2pServiceImpl$P2pStateMachine, java.lang.String):java.lang.String type: STATIC in method: com.android.server.wifi.p2p.WifiP2pServiceImpl.P2pStateMachine.P2pDisabledState.processMessage(android.os.Message):boolean, dex: wifi-service_classes.dex
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:245)
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:210)
                        	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:109)
                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:55)
                        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                        	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:98)
                        	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:156)
                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:62)
                        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                        	at jadx.core.codegen.MethodGen.addRegionInsns(MethodGen.java:211)
                        	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:204)
                        	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:317)
                        	at jadx.core.codegen.ClassGen.addMethods(ClassGen.java:263)
                        	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:226)
                        	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:111)
                        	at jadx.core.codegen.ClassGen.addInnerClasses(ClassGen.java:238)
                        	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:225)
                        	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:111)
                        	at jadx.core.codegen.ClassGen.addInnerClasses(ClassGen.java:238)
                        	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:225)
                        	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:111)
                        	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:77)
                        	at jadx.core.codegen.CodeGen.wrapCodeGen(CodeGen.java:44)
                        	at jadx.core.codegen.CodeGen.generateJavaCode(CodeGen.java:33)
                        	at jadx.core.codegen.CodeGen.generate(CodeGen.java:21)
                        	at jadx.core.ProcessClass.generateCode(ProcessClass.java:61)
                        	at jadx.core.dex.nodes.ClassNode.decompile(ClassNode.java:273)
                        Caused by: jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0000: IPUT  (wrap: java.lang.String
                          0x0047: INVOKE  (r1v5 java.lang.String) = (wrap: com.android.server.wifi.p2p.WifiP2pNative
                          0x003a: IGET  (r1v4 com.android.server.wifi.p2p.WifiP2pNative) = (wrap: com.android.server.wifi.p2p.WifiP2pServiceImpl$P2pStateMachine
                          0x0038: IGET  (r1v3 com.android.server.wifi.p2p.WifiP2pServiceImpl$P2pStateMachine) = (r4v0 'this' com.android.server.wifi.p2p.WifiP2pServiceImpl$P2pStateMachine$P2pDisabledState A[THIS]) com.android.server.wifi.p2p.WifiP2pServiceImpl.P2pStateMachine.P2pDisabledState.this$1 com.android.server.wifi.p2p.WifiP2pServiceImpl$P2pStateMachine) com.android.server.wifi.p2p.WifiP2pServiceImpl.P2pStateMachine.mWifiNative com.android.server.wifi.p2p.WifiP2pNative), (wrap: com.android.server.wifi.p2p.-$$Lambda$WifiP2pServiceImpl$P2pStateMachine$P2pDisabledState$13XANUNRJEt7WjtJr5tKTd2g-PM
                          0x003e: CONSTRUCTOR  (r2v2 com.android.server.wifi.p2p.-$$Lambda$WifiP2pServiceImpl$P2pStateMachine$P2pDisabledState$13XANUNRJEt7WjtJr5tKTd2g-PM) = (r4v0 'this' com.android.server.wifi.p2p.WifiP2pServiceImpl$P2pStateMachine$P2pDisabledState A[THIS]) com.android.server.wifi.p2p.-$$Lambda$WifiP2pServiceImpl$P2pStateMachine$P2pDisabledState$13XANUNRJEt7WjtJr5tKTd2g-PM.<init>(com.android.server.wifi.p2p.WifiP2pServiceImpl$P2pStateMachine$P2pDisabledState):void CONSTRUCTOR), (wrap: android.os.Handler
                          0x0043: INVOKE  (r3v1 android.os.Handler) = (wrap: com.android.server.wifi.p2p.WifiP2pServiceImpl$P2pStateMachine
                          0x0041: IGET  (r3v0 com.android.server.wifi.p2p.WifiP2pServiceImpl$P2pStateMachine) = (r4v0 'this' com.android.server.wifi.p2p.WifiP2pServiceImpl$P2pStateMachine$P2pDisabledState A[THIS]) com.android.server.wifi.p2p.WifiP2pServiceImpl.P2pStateMachine.P2pDisabledState.this$1 com.android.server.wifi.p2p.WifiP2pServiceImpl$P2pStateMachine) com.android.server.wifi.p2p.WifiP2pServiceImpl.P2pStateMachine.getHandler():android.os.Handler type: VIRTUAL) com.android.server.wifi.p2p.WifiP2pNative.setupInterface(com.android.server.wifi.HalDeviceManager$InterfaceDestroyedListener, android.os.Handler):java.lang.String type: VIRTUAL), (wrap: com.android.server.wifi.p2p.WifiP2pServiceImpl$P2pStateMachine
                          0x0036: IGET  (r0v5 com.android.server.wifi.p2p.WifiP2pServiceImpl$P2pStateMachine) = (r4v0 'this' com.android.server.wifi.p2p.WifiP2pServiceImpl$P2pStateMachine$P2pDisabledState A[THIS]) com.android.server.wifi.p2p.WifiP2pServiceImpl.P2pStateMachine.P2pDisabledState.this$1 com.android.server.wifi.p2p.WifiP2pServiceImpl$P2pStateMachine) com.android.server.wifi.p2p.WifiP2pServiceImpl.P2pStateMachine.mInterfaceName java.lang.String in method: com.android.server.wifi.p2p.WifiP2pServiceImpl.P2pStateMachine.P2pDisabledState.processMessage(android.os.Message):boolean, dex: wifi-service_classes.dex
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:245)
                        	at jadx.core.codegen.InsnGen.inlineMethod(InsnGen.java:908)
                        	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:673)
                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:357)
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:239)
                        	... 32 more
                        Caused by: jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0047: INVOKE  (r1v5 java.lang.String) = (wrap: com.android.server.wifi.p2p.WifiP2pNative
                          0x003a: IGET  (r1v4 com.android.server.wifi.p2p.WifiP2pNative) = (wrap: com.android.server.wifi.p2p.WifiP2pServiceImpl$P2pStateMachine
                          0x0038: IGET  (r1v3 com.android.server.wifi.p2p.WifiP2pServiceImpl$P2pStateMachine) = (r4v0 'this' com.android.server.wifi.p2p.WifiP2pServiceImpl$P2pStateMachine$P2pDisabledState A[THIS]) com.android.server.wifi.p2p.WifiP2pServiceImpl.P2pStateMachine.P2pDisabledState.this$1 com.android.server.wifi.p2p.WifiP2pServiceImpl$P2pStateMachine) com.android.server.wifi.p2p.WifiP2pServiceImpl.P2pStateMachine.mWifiNative com.android.server.wifi.p2p.WifiP2pNative), (wrap: com.android.server.wifi.p2p.-$$Lambda$WifiP2pServiceImpl$P2pStateMachine$P2pDisabledState$13XANUNRJEt7WjtJr5tKTd2g-PM
                          0x003e: CONSTRUCTOR  (r2v2 com.android.server.wifi.p2p.-$$Lambda$WifiP2pServiceImpl$P2pStateMachine$P2pDisabledState$13XANUNRJEt7WjtJr5tKTd2g-PM) = (r4v0 'this' com.android.server.wifi.p2p.WifiP2pServiceImpl$P2pStateMachine$P2pDisabledState A[THIS]) com.android.server.wifi.p2p.-$$Lambda$WifiP2pServiceImpl$P2pStateMachine$P2pDisabledState$13XANUNRJEt7WjtJr5tKTd2g-PM.<init>(com.android.server.wifi.p2p.WifiP2pServiceImpl$P2pStateMachine$P2pDisabledState):void CONSTRUCTOR), (wrap: android.os.Handler
                          0x0043: INVOKE  (r3v1 android.os.Handler) = (wrap: com.android.server.wifi.p2p.WifiP2pServiceImpl$P2pStateMachine
                          0x0041: IGET  (r3v0 com.android.server.wifi.p2p.WifiP2pServiceImpl$P2pStateMachine) = (r4v0 'this' com.android.server.wifi.p2p.WifiP2pServiceImpl$P2pStateMachine$P2pDisabledState A[THIS]) com.android.server.wifi.p2p.WifiP2pServiceImpl.P2pStateMachine.P2pDisabledState.this$1 com.android.server.wifi.p2p.WifiP2pServiceImpl$P2pStateMachine) com.android.server.wifi.p2p.WifiP2pServiceImpl.P2pStateMachine.getHandler():android.os.Handler type: VIRTUAL) com.android.server.wifi.p2p.WifiP2pNative.setupInterface(com.android.server.wifi.HalDeviceManager$InterfaceDestroyedListener, android.os.Handler):java.lang.String type: VIRTUAL in method: com.android.server.wifi.p2p.WifiP2pServiceImpl.P2pStateMachine.P2pDisabledState.processMessage(android.os.Message):boolean, dex: wifi-service_classes.dex
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:245)
                        	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:108)
                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:418)
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:220)
                        	... 36 more
                        Caused by: jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x003e: CONSTRUCTOR  (r2v2 com.android.server.wifi.p2p.-$$Lambda$WifiP2pServiceImpl$P2pStateMachine$P2pDisabledState$13XANUNRJEt7WjtJr5tKTd2g-PM) = (r4v0 'this' com.android.server.wifi.p2p.WifiP2pServiceImpl$P2pStateMachine$P2pDisabledState A[THIS]) com.android.server.wifi.p2p.-$$Lambda$WifiP2pServiceImpl$P2pStateMachine$P2pDisabledState$13XANUNRJEt7WjtJr5tKTd2g-PM.<init>(com.android.server.wifi.p2p.WifiP2pServiceImpl$P2pStateMachine$P2pDisabledState):void CONSTRUCTOR in method: com.android.server.wifi.p2p.WifiP2pServiceImpl.P2pStateMachine.P2pDisabledState.processMessage(android.os.Message):boolean, dex: wifi-service_classes.dex
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:245)
                        	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:108)
                        	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:776)
                        	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:717)
                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:357)
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:220)
                        	... 39 more
                        Caused by: jadx.core.utils.exceptions.JadxRuntimeException: Expected class to be processed at this point, class: com.android.server.wifi.p2p.-$$Lambda$WifiP2pServiceImpl$P2pStateMachine$P2pDisabledState$13XANUNRJEt7WjtJr5tKTd2g-PM, state: NOT_LOADED
                        	at jadx.core.dex.nodes.ClassNode.ensureProcessed(ClassNode.java:260)
                        	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:595)
                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:353)
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:220)
                        	... 44 more
                        */
                    /*
                        this = this;
                        com.android.server.wifi.p2p.WifiP2pServiceImpl$P2pStateMachine r0 = com.android.server.wifi.p2p.WifiP2pServiceImpl.P2pStateMachine.this
                        java.lang.StringBuilder r1 = new java.lang.StringBuilder
                        r1.<init>()
                        java.lang.String r2 = r4.getName()
                        r1.append(r2)
                        java.lang.String r2 = r5.toString()
                        r1.append(r2)
                        java.lang.String r1 = r1.toString()
                        r0.logd(r1)
                        int r0 = r5.what
                        r1 = 143376(0x23010, float:2.00913E-40)
                        if (r0 == r1) goto L_0x0025
                        r0 = 0
                        return r0
                    L_0x0025:
                        com.android.server.wifi.p2p.WifiP2pServiceImpl$P2pStateMachine r0 = com.android.server.wifi.p2p.WifiP2pServiceImpl.P2pStateMachine.this
                        boolean r0 = r0.mIsWifiEnabled
                        if (r0 != 0) goto L_0x0036
                        java.lang.String r0 = "WifiP2pService"
                        java.lang.String r1 = "Ignore P2P enable since wifi is disabled"
                        android.util.Log.e(r0, r1)
                        goto L_0x00af
                    L_0x0036:
                        com.android.server.wifi.p2p.WifiP2pServiceImpl$P2pStateMachine r0 = com.android.server.wifi.p2p.WifiP2pServiceImpl.P2pStateMachine.this
                        com.android.server.wifi.p2p.WifiP2pServiceImpl$P2pStateMachine r1 = com.android.server.wifi.p2p.WifiP2pServiceImpl.P2pStateMachine.this
                        com.android.server.wifi.p2p.WifiP2pNative r1 = r1.mWifiNative
                        com.android.server.wifi.p2p.-$$Lambda$WifiP2pServiceImpl$P2pStateMachine$P2pDisabledState$13XANUNRJEt7WjtJr5tKTd2g-PM r2 = new com.android.server.wifi.p2p.-$$Lambda$WifiP2pServiceImpl$P2pStateMachine$P2pDisabledState$13XANUNRJEt7WjtJr5tKTd2g-PM
                        r2.<init>(r4)
                        com.android.server.wifi.p2p.WifiP2pServiceImpl$P2pStateMachine r3 = com.android.server.wifi.p2p.WifiP2pServiceImpl.P2pStateMachine.this
                        android.os.Handler r3 = r3.getHandler()
                        java.lang.String r1 = r1.setupInterface(r2, r3)
                        java.lang.String unused = r0.mInterfaceName = r1
                        com.android.server.wifi.p2p.WifiP2pServiceImpl$P2pStateMachine r0 = com.android.server.wifi.p2p.WifiP2pServiceImpl.P2pStateMachine.this
                        java.lang.String r0 = r0.mInterfaceName
                        if (r0 != 0) goto L_0x005e
                        java.lang.String r0 = "WifiP2pService"
                        java.lang.String r1 = "Failed to setup interface for P2P"
                        android.util.Log.e(r0, r1)
                        goto L_0x00af
                    L_0x005e:
                        com.android.server.wifi.p2p.WifiP2pServiceImpl$P2pStateMachine r0 = com.android.server.wifi.p2p.WifiP2pServiceImpl.P2pStateMachine.this     // Catch:{ RemoteException -> 0x0086, IllegalStateException -> 0x006e }
                        com.android.server.wifi.p2p.WifiP2pServiceImpl r0 = com.android.server.wifi.p2p.WifiP2pServiceImpl.this     // Catch:{ RemoteException -> 0x0086, IllegalStateException -> 0x006e }
                        android.os.INetworkManagementService r0 = r0.mNwService     // Catch:{ RemoteException -> 0x0086, IllegalStateException -> 0x006e }
                        com.android.server.wifi.p2p.WifiP2pServiceImpl$P2pStateMachine r1 = com.android.server.wifi.p2p.WifiP2pServiceImpl.P2pStateMachine.this     // Catch:{ RemoteException -> 0x0086, IllegalStateException -> 0x006e }
                        java.lang.String r1 = r1.mInterfaceName     // Catch:{ RemoteException -> 0x0086, IllegalStateException -> 0x006e }
                        r0.setInterfaceUp(r1)     // Catch:{ RemoteException -> 0x0086, IllegalStateException -> 0x006e }
                        goto L_0x009d
                    L_0x006e:
                        r0 = move-exception
                        com.android.server.wifi.p2p.WifiP2pServiceImpl$P2pStateMachine r1 = com.android.server.wifi.p2p.WifiP2pServiceImpl.P2pStateMachine.this
                        java.lang.StringBuilder r2 = new java.lang.StringBuilder
                        r2.<init>()
                        java.lang.String r3 = "Unable to change interface settings: "
                        r2.append(r3)
                        r2.append(r0)
                        java.lang.String r2 = r2.toString()
                        r1.loge(r2)
                        goto L_0x009e
                    L_0x0086:
                        r0 = move-exception
                        com.android.server.wifi.p2p.WifiP2pServiceImpl$P2pStateMachine r1 = com.android.server.wifi.p2p.WifiP2pServiceImpl.P2pStateMachine.this
                        java.lang.StringBuilder r2 = new java.lang.StringBuilder
                        r2.<init>()
                        java.lang.String r3 = "Unable to change interface settings: "
                        r2.append(r3)
                        r2.append(r0)
                        java.lang.String r2 = r2.toString()
                        r1.loge(r2)
                    L_0x009d:
                    L_0x009e:
                        com.android.server.wifi.p2p.WifiP2pServiceImpl$P2pStateMachine r0 = com.android.server.wifi.p2p.WifiP2pServiceImpl.P2pStateMachine.this
                        r0.registerForWifiMonitorEvents()
                        com.android.server.wifi.p2p.WifiP2pServiceImpl$P2pStateMachine r0 = com.android.server.wifi.p2p.WifiP2pServiceImpl.P2pStateMachine.this
                        com.android.server.wifi.p2p.WifiP2pServiceImpl$P2pStateMachine r1 = com.android.server.wifi.p2p.WifiP2pServiceImpl.P2pStateMachine.this
                        com.android.server.wifi.p2p.WifiP2pServiceImpl$P2pStateMachine$InactiveState r1 = r1.mInactiveState
                        r0.transitionTo(r1)
                    L_0x00af:
                        r0 = 1
                        return r0
                    */
                    throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.p2p.WifiP2pServiceImpl.P2pStateMachine.P2pDisabledState.processMessage(android.os.Message):boolean");
                }
            }

            class P2pDisablingState extends State {
                P2pDisablingState() {
                }

                public void enter() {
                    P2pStateMachine.this.logd(getName());
                    P2pStateMachine.this.sendMessageDelayed(P2pStateMachine.this.obtainMessage(WifiP2pServiceImpl.DISABLE_P2P_TIMED_OUT, WifiP2pServiceImpl.access$1804(), 0), 5000);
                }

                public boolean processMessage(Message message) {
                    P2pStateMachine p2pStateMachine = P2pStateMachine.this;
                    p2pStateMachine.logd(getName() + message.what);
                    switch (message.what) {
                        case WifiP2pServiceImpl.DISABLE_P2P_TIMED_OUT /*143366*/:
                            if (WifiP2pServiceImpl.sDisableP2pTimeoutIndex == message.arg1) {
                                P2pStateMachine.this.loge("P2p disable timed out");
                                P2pStateMachine.this.transitionTo(P2pStateMachine.this.mP2pDisabledState);
                                break;
                            }
                            break;
                        case WifiP2pServiceImpl.ENABLE_P2P /*143376*/:
                        case WifiP2pServiceImpl.DISABLE_P2P /*143377*/:
                            P2pStateMachine.this.deferMessage(message);
                            break;
                        case 147458:
                            P2pStateMachine.this.logd("p2p socket connection lost");
                            P2pStateMachine.this.transitionTo(P2pStateMachine.this.mP2pDisabledState);
                            break;
                        default:
                            return false;
                    }
                    return true;
                }

                public void exit() {
                    if (WifiP2pServiceImpl.this.mWifiChannel != null) {
                        WifiP2pServiceImpl.this.mWifiChannel.sendMessage(WifiStateMachine.CMD_DISABLE_P2P_RSP);
                    } else {
                        P2pStateMachine.this.loge("P2pDisablingState exit(): WifiChannel is null");
                    }
                }
            }

            class P2pEnabledState extends State {
                P2pEnabledState() {
                }

                public void enter() {
                    P2pStateMachine.this.logd(getName());
                    WifiP2pServiceImpl.this.mNetworkInfo.setIsAvailable(true);
                    if (!WifiP2pServiceHisiExt.hisiWifiEnabled()) {
                        P2pStateMachine.this.sendP2pConnectionChangedBroadcast();
                    } else if (WifiP2pServiceImpl.this.isWifiP2pEnabled()) {
                        P2pStateMachine.this.sendP2pConnectionChangedBroadcast();
                    }
                    P2pStateMachine.this.initializeP2pSettings();
                }

                public boolean processMessage(Message message) {
                    P2pStateMachine p2pStateMachine = P2pStateMachine.this;
                    p2pStateMachine.logd(getName() + " when=" + message.getWhen() + " what=" + message.what + " arg1=" + message.arg1 + " arg2=" + message.arg2);
                    if (WifiP2pServiceImpl.this.processMessageForP2pCollision(message, this)) {
                        return true;
                    }
                    switch (message.what) {
                        case 139265:
                            if (!WifiP2pServiceImpl.this.mDiscoveryBlocked && WifiP2pServiceImpl.this.allowP2pFind(message.sendingUid)) {
                                P2pStateMachine.this.clearSupplicantServiceRequest();
                                if (!P2pStateMachine.this.mWifiNative.p2pFind(WifiP2pServiceImpl.this.addScanChannelInTimeout(message.arg1, WifiP2pServiceImpl.DISCOVER_TIMEOUT_S))) {
                                    P2pStateMachine.this.replyToMessage(message, 139266, 0);
                                    WifiP2pServiceImpl.this.handleP2pStopFind(message.sendingUid);
                                    break;
                                } else {
                                    P2pStateMachine.this.replyToMessage(message, 139267);
                                    P2pStateMachine.this.sendP2pDiscoveryChangedBroadcast(true);
                                    if (WifiP2pServiceImpl.this.mHwWifiCHRService != null) {
                                        WifiP2pServiceImpl.this.mHwWifiCHRService.updateApkChangewWifiStatus(13, WifiP2pServiceImpl.this.mContext.getPackageManager().getNameForUid(message.sendingUid));
                                        break;
                                    }
                                }
                            } else {
                                P2pStateMachine.this.replyToMessage(message, 139266, 2);
                                break;
                            }
                            break;
                        case 139268:
                            if (P2pStateMachine.this.mWifiNative.p2pStopFind()) {
                                P2pStateMachine.this.replyToMessage(message, 139270);
                            } else {
                                P2pStateMachine.this.replyToMessage(message, 139269, 0);
                            }
                            WifiP2pServiceImpl.this.handleP2pStopFind(message.sendingUid);
                            break;
                        case 139292:
                            P2pStateMachine p2pStateMachine2 = P2pStateMachine.this;
                            p2pStateMachine2.logd(getName() + " add service");
                            if (!P2pStateMachine.this.addLocalService(message.replyTo, (WifiP2pServiceInfo) message.obj)) {
                                P2pStateMachine.this.replyToMessage(message, 139293);
                                break;
                            } else {
                                P2pStateMachine.this.replyToMessage(message, 139294);
                                break;
                            }
                        case 139295:
                            P2pStateMachine p2pStateMachine3 = P2pStateMachine.this;
                            p2pStateMachine3.logd(getName() + " remove service");
                            P2pStateMachine.this.removeLocalService(message.replyTo, (WifiP2pServiceInfo) message.obj);
                            P2pStateMachine.this.replyToMessage(message, 139297);
                            break;
                        case 139298:
                            P2pStateMachine p2pStateMachine4 = P2pStateMachine.this;
                            p2pStateMachine4.logd(getName() + " clear service");
                            P2pStateMachine.this.clearLocalServices(message.replyTo);
                            P2pStateMachine.this.replyToMessage(message, 139300);
                            break;
                        case 139301:
                            P2pStateMachine p2pStateMachine5 = P2pStateMachine.this;
                            p2pStateMachine5.logd(getName() + " add service request");
                            if (P2pStateMachine.this.addServiceRequest(message.replyTo, (WifiP2pServiceRequest) message.obj)) {
                                P2pStateMachine.this.replyToMessage(message, 139303);
                                break;
                            } else {
                                P2pStateMachine.this.replyToMessage(message, 139302);
                                break;
                            }
                        case 139304:
                            P2pStateMachine p2pStateMachine6 = P2pStateMachine.this;
                            p2pStateMachine6.logd(getName() + " remove service request");
                            P2pStateMachine.this.removeServiceRequest(message.replyTo, (WifiP2pServiceRequest) message.obj);
                            P2pStateMachine.this.replyToMessage(message, 139306);
                            break;
                        case 139307:
                            P2pStateMachine p2pStateMachine7 = P2pStateMachine.this;
                            p2pStateMachine7.logd(getName() + " clear service request");
                            P2pStateMachine.this.clearServiceRequests(message.replyTo);
                            P2pStateMachine.this.replyToMessage(message, 139309);
                            break;
                        case 139310:
                            if (!WifiP2pServiceImpl.this.mDiscoveryBlocked) {
                                P2pStateMachine p2pStateMachine8 = P2pStateMachine.this;
                                p2pStateMachine8.logd(getName() + " discover services");
                                if (P2pStateMachine.this.updateSupplicantServiceRequest()) {
                                    if (!P2pStateMachine.this.mWifiNative.p2pFind(WifiP2pServiceImpl.DISCOVER_TIMEOUT_S)) {
                                        P2pStateMachine.this.replyToMessage(message, 139311, 0);
                                        break;
                                    } else {
                                        P2pStateMachine.this.replyToMessage(message, 139312);
                                        break;
                                    }
                                } else {
                                    P2pStateMachine.this.replyToMessage(message, 139311, 3);
                                    break;
                                }
                            } else {
                                P2pStateMachine.this.replyToMessage(message, 139311, 2);
                                break;
                            }
                        case 139315:
                            WifiP2pDevice d = (WifiP2pDevice) message.obj;
                            if (d != null && P2pStateMachine.this.setAndPersistDeviceName(d.deviceName)) {
                                P2pStateMachine p2pStateMachine9 = P2pStateMachine.this;
                                p2pStateMachine9.logd("set device name " + d.deviceName);
                                P2pStateMachine.this.replyToMessage(message, 139317);
                                break;
                            } else {
                                P2pStateMachine.this.replyToMessage(message, 139316, 0);
                                break;
                            }
                        case 139318:
                            WifiP2pWfdInfo d2 = P2pStateMachine.this;
                            d2.logd(getName() + " delete persistent group");
                            P2pStateMachine.this.mGroups.remove(message.arg1);
                            P2pStateMachine.this.replyToMessage(message, 139320);
                            break;
                        case 139323:
                            WifiP2pWfdInfo d3 = (WifiP2pWfdInfo) message.obj;
                            if (WifiP2pServiceImpl.this.getWfdPermission(message.sendingUid)) {
                                if (d3 != null && P2pStateMachine.this.setWfdInfo(d3)) {
                                    P2pStateMachine.this.replyToMessage(message, 139325);
                                    break;
                                } else {
                                    P2pStateMachine.this.replyToMessage(message, 139324, 0);
                                    break;
                                }
                            } else {
                                P2pStateMachine.this.replyToMessage(message, 139324, 0);
                                break;
                            }
                            break;
                        case 139329:
                            P2pStateMachine p2pStateMachine10 = P2pStateMachine.this;
                            p2pStateMachine10.logd(getName() + " start listen mode");
                            P2pStateMachine.this.mWifiNative.p2pFlush();
                            if (!P2pStateMachine.this.mWifiNative.p2pExtListen(true, 500, 500)) {
                                P2pStateMachine.this.replyToMessage(message, 139330);
                                break;
                            } else {
                                P2pStateMachine.this.replyToMessage(message, 139331);
                                break;
                            }
                        case 139332:
                            P2pStateMachine p2pStateMachine11 = P2pStateMachine.this;
                            p2pStateMachine11.logd(getName() + " stop listen mode");
                            if (P2pStateMachine.this.mWifiNative.p2pExtListen(false, 0, 0)) {
                                P2pStateMachine.this.replyToMessage(message, 139334);
                            } else {
                                P2pStateMachine.this.replyToMessage(message, 139333);
                            }
                            P2pStateMachine.this.mWifiNative.p2pFlush();
                            break;
                        case 139335:
                            Bundle p2pChannels = (Bundle) message.obj;
                            int lc = p2pChannels.getInt("lc", 0);
                            int oc = p2pChannels.getInt("oc", 0);
                            P2pStateMachine p2pStateMachine12 = P2pStateMachine.this;
                            p2pStateMachine12.logd(getName() + " set listen and operating channel");
                            if (!P2pStateMachine.this.mWifiNative.p2pSetChannel(lc, oc)) {
                                P2pStateMachine.this.replyToMessage(message, 139336);
                                break;
                            } else {
                                P2pStateMachine.this.replyToMessage(message, 139337);
                                break;
                            }
                        case 139339:
                            Bundle requestBundle = new Bundle();
                            requestBundle.putString("android.net.wifi.p2p.EXTRA_HANDOVER_MESSAGE", P2pStateMachine.this.mWifiNative.getNfcHandoverRequest());
                            P2pStateMachine.this.replyToMessage(message, 139341, (Object) requestBundle);
                            break;
                        case 139340:
                            Bundle selectBundle = new Bundle();
                            selectBundle.putString("android.net.wifi.p2p.EXTRA_HANDOVER_MESSAGE", P2pStateMachine.this.mWifiNative.getNfcHandoverSelect());
                            P2pStateMachine.this.replyToMessage(message, 139341, (Object) selectBundle);
                            break;
                        case WifiP2pServiceImpl.SET_MIRACAST_MODE /*143374*/:
                            P2pStateMachine.this.mWifiNative.setMiracastMode(message.arg1);
                            break;
                        case WifiP2pServiceImpl.BLOCK_DISCOVERY /*143375*/:
                            boolean blocked = message.arg1 == 1;
                            if (WifiP2pServiceImpl.this.mDiscoveryBlocked != blocked) {
                                boolean unused = WifiP2pServiceImpl.this.mDiscoveryBlocked = blocked;
                                if (blocked && WifiP2pServiceImpl.this.mDiscoveryStarted) {
                                    P2pStateMachine.this.mWifiNative.p2pStopFind();
                                    boolean unused2 = WifiP2pServiceImpl.this.mDiscoveryPostponed = true;
                                }
                                if (!blocked && WifiP2pServiceImpl.this.mDiscoveryPostponed) {
                                    boolean unused3 = WifiP2pServiceImpl.this.mDiscoveryPostponed = false;
                                    P2pStateMachine.this.mWifiNative.p2pFind(WifiP2pServiceImpl.DISCOVER_TIMEOUT_S);
                                }
                                if (blocked) {
                                    if (message.obj != null) {
                                        try {
                                            ((StateMachine) message.obj).sendMessage(message.arg2);
                                            break;
                                        } catch (Exception e) {
                                            P2pStateMachine p2pStateMachine13 = P2pStateMachine.this;
                                            p2pStateMachine13.loge("unable to send BLOCK_DISCOVERY response: " + e);
                                            break;
                                        }
                                    } else {
                                        Log.e(WifiP2pServiceImpl.TAG, "Illegal argument(s)");
                                        break;
                                    }
                                }
                            }
                            break;
                        case WifiP2pServiceImpl.ENABLE_P2P /*143376*/:
                            break;
                        case WifiP2pServiceImpl.DISABLE_P2P /*143377*/:
                            if (P2pStateMachine.this.mPeers.clear()) {
                                P2pStateMachine.this.sendPeersChangedBroadcast();
                            }
                            if (P2pStateMachine.this.mGroups.clear()) {
                                P2pStateMachine.this.sendP2pPersistentGroupsChangedBroadcast();
                            }
                            P2pStateMachine.this.mWifiMonitor.stopMonitoring(P2pStateMachine.this.mInterfaceName);
                            P2pStateMachine.this.mWifiNative.teardownInterface();
                            P2pStateMachine.this.transitionTo(P2pStateMachine.this.mP2pDisablingState);
                            break;
                        case 147458:
                            P2pStateMachine.this.loge("Unexpected loss of p2p socket connection");
                            WifiP2pServiceImpl.this.mWifiChannel.sendMessage(147458);
                            P2pStateMachine.this.transitionTo(P2pStateMachine.this.mP2pDisabledState);
                            break;
                        case WifiP2pMonitor.P2P_DEVICE_FOUND_EVENT:
                            if (message.obj != null) {
                                WifiP2pDevice device = (WifiP2pDevice) message.obj;
                                if (!WifiP2pServiceImpl.this.mThisDevice.deviceAddress.equals(device.deviceAddress)) {
                                    P2pStateMachine.this.mPeers.updateSupplicantDetails(device);
                                    P2pStateMachine.this.sendPeersChangedBroadcast();
                                    if (WifiP2pServiceHisiExt.hisiWifiEnabled() && WifiP2pServiceImpl.this.mWifiP2pServiceHisiExt.P2pFindDeviceUpdate) {
                                        WifiP2pServiceImpl.this.mWifiP2pServiceHisiExt.P2pFindDeviceUpdate = false;
                                        P2pStateMachine.this.updatePersistentNetworks(true);
                                        break;
                                    }
                                }
                            } else {
                                Log.e(WifiP2pServiceImpl.TAG, "Illegal argument(s)");
                                break;
                            }
                            break;
                        case WifiP2pMonitor.P2P_DEVICE_LOST_EVENT:
                            if (message.obj != null) {
                                if (P2pStateMachine.this.mPeers.remove(((WifiP2pDevice) message.obj).deviceAddress) != null) {
                                    P2pStateMachine.this.sendPeersChangedBroadcast();
                                    break;
                                }
                            } else {
                                Log.e(WifiP2pServiceImpl.TAG, "Illegal argument(s)");
                                break;
                            }
                            break;
                        case WifiP2pMonitor.P2P_FIND_STOPPED_EVENT:
                            P2pStateMachine.this.sendP2pDiscoveryChangedBroadcast(false);
                            WifiP2pServiceImpl.this.handleP2pStopFind(message.sendingUid);
                            break;
                        case WifiP2pMonitor.P2P_SERV_DISC_RESP_EVENT:
                            P2pStateMachine p2pStateMachine14 = P2pStateMachine.this;
                            p2pStateMachine14.logd(getName() + " receive service response");
                            if (message.obj != null) {
                                for (WifiP2pServiceResponse resp : (List) message.obj) {
                                    resp.setSrcDevice(P2pStateMachine.this.mPeers.get(resp.getSrcDevice().deviceAddress));
                                    P2pStateMachine.this.sendServiceResponse(resp);
                                }
                                break;
                            } else {
                                Log.e(WifiP2pServiceImpl.TAG, "Illegal argument(s)");
                                break;
                            }
                        default:
                            return WifiP2pServiceImpl.this.handleP2pEnabledStateExMessage(message);
                    }
                    return true;
                }

                public void exit() {
                    P2pStateMachine.this.sendP2pDiscoveryChangedBroadcast(false);
                    WifiP2pServiceImpl.this.mNetworkInfo.setIsAvailable(false);
                }
            }

            class P2pNotSupportedState extends State {
                P2pNotSupportedState() {
                }

                public boolean processMessage(Message message) {
                    switch (message.what) {
                        case 139265:
                            P2pStateMachine.this.replyToMessage(message, 139266, 1);
                            break;
                        case 139268:
                            P2pStateMachine.this.replyToMessage(message, 139269, 1);
                            break;
                        case 139271:
                            P2pStateMachine.this.replyToMessage(message, 139272, 1);
                            break;
                        case 139274:
                            P2pStateMachine.this.replyToMessage(message, 139275, 1);
                            break;
                        case 139277:
                            P2pStateMachine.this.replyToMessage(message, 139278, 1);
                            break;
                        case 139280:
                            P2pStateMachine.this.replyToMessage(message, 139281, 1);
                            if (WifiP2pServiceImpl.this.getWifiRepeaterEnabled()) {
                                WifiP2pServiceImpl.this.stopWifiRepeater(P2pStateMachine.this.mGroup);
                                break;
                            }
                            break;
                        case 139292:
                            P2pStateMachine.this.replyToMessage(message, 139293, 1);
                            break;
                        case 139295:
                            P2pStateMachine.this.replyToMessage(message, 139296, 1);
                            break;
                        case 139298:
                            P2pStateMachine.this.replyToMessage(message, 139299, 1);
                            break;
                        case 139301:
                            P2pStateMachine.this.replyToMessage(message, 139302, 1);
                            break;
                        case 139304:
                            P2pStateMachine.this.replyToMessage(message, 139305, 1);
                            break;
                        case 139307:
                            P2pStateMachine.this.replyToMessage(message, 139308, 1);
                            break;
                        case 139310:
                            P2pStateMachine.this.replyToMessage(message, 139311, 1);
                            break;
                        case 139315:
                            P2pStateMachine.this.replyToMessage(message, 139316, 1);
                            break;
                        case 139318:
                            P2pStateMachine.this.replyToMessage(message, 139318, 1);
                            break;
                        case 139323:
                            if (WifiP2pServiceImpl.this.getWfdPermission(message.sendingUid)) {
                                P2pStateMachine.this.replyToMessage(message, 139324, 1);
                                break;
                            } else {
                                P2pStateMachine.this.replyToMessage(message, 139324, 0);
                                break;
                            }
                        case 139326:
                            P2pStateMachine.this.replyToMessage(message, 139327, 1);
                            break;
                        case 139329:
                            P2pStateMachine.this.replyToMessage(message, 139330, 1);
                            break;
                        case 139332:
                            P2pStateMachine.this.replyToMessage(message, 139333, 1);
                            break;
                        default:
                            return WifiP2pServiceImpl.this.handleP2pNotSupportedStateMessage(message);
                    }
                    return true;
                }
            }

            class ProvisionDiscoveryState extends State {
                ProvisionDiscoveryState() {
                }

                public void enter() {
                    P2pStateMachine.this.logd(getName());
                    P2pStateMachine.this.mWifiNative.p2pProvisionDiscovery(P2pStateMachine.this.mSavedPeerConfig);
                }

                public boolean processMessage(Message message) {
                    P2pStateMachine p2pStateMachine = P2pStateMachine.this;
                    p2pStateMachine.logd(getName() + message.toString());
                    int i = message.what;
                    if (i == 147479) {
                        P2pStateMachine p2pStateMachine2 = P2pStateMachine.this;
                        p2pStateMachine2.logd(getName() + " deal with P2P_GO_NEGOTIATION_REQUEST_EVENT");
                        P2pStateMachine.this.deferMessage(message);
                        P2pStateMachine.this.transitionTo(P2pStateMachine.this.mInactiveState);
                    } else if (i != 147495) {
                        switch (i) {
                            case WifiP2pMonitor.P2P_PROV_DISC_PBC_RSP_EVENT:
                                if (message.obj != null) {
                                    WifiP2pDevice device = ((WifiP2pProvDiscEvent) message.obj).device;
                                    if ((device == null || device.deviceAddress.equals(P2pStateMachine.this.mSavedPeerConfig.deviceAddress)) && P2pStateMachine.this.mSavedPeerConfig.wps.setup == 0) {
                                        P2pStateMachine p2pStateMachine3 = P2pStateMachine.this;
                                        p2pStateMachine3.logd("Found a match " + P2pStateMachine.this.mSavedPeerConfig);
                                        P2pStateMachine.this.p2pConnectWithPinDisplay(P2pStateMachine.this.mSavedPeerConfig);
                                        P2pStateMachine.this.transitionTo(P2pStateMachine.this.mGroupNegotiationState);
                                        break;
                                    }
                                } else {
                                    Log.e(WifiP2pServiceImpl.TAG, "Invalid argument(s)");
                                    break;
                                }
                            case WifiP2pMonitor.P2P_PROV_DISC_ENTER_PIN_EVENT:
                                if (message.obj != null) {
                                    WifiP2pDevice device2 = ((WifiP2pProvDiscEvent) message.obj).device;
                                    if ((device2 == null || device2.deviceAddress.equals(P2pStateMachine.this.mSavedPeerConfig.deviceAddress)) && P2pStateMachine.this.mSavedPeerConfig.wps.setup == 2) {
                                        P2pStateMachine p2pStateMachine4 = P2pStateMachine.this;
                                        p2pStateMachine4.logd("Found a match " + P2pStateMachine.this.mSavedPeerConfig);
                                        if (TextUtils.isEmpty(P2pStateMachine.this.mSavedPeerConfig.wps.pin)) {
                                            boolean unused = WifiP2pServiceImpl.this.mJoinExistingGroup = false;
                                            P2pStateMachine.this.transitionTo(P2pStateMachine.this.mUserAuthorizingNegotiationRequestState);
                                            break;
                                        } else {
                                            P2pStateMachine.this.p2pConnectWithPinDisplay(P2pStateMachine.this.mSavedPeerConfig);
                                            P2pStateMachine.this.transitionTo(P2pStateMachine.this.mGroupNegotiationState);
                                            break;
                                        }
                                    }
                                } else {
                                    Log.e(WifiP2pServiceImpl.TAG, "Illegal argument(s)");
                                    break;
                                }
                            case WifiP2pMonitor.P2P_PROV_DISC_SHOW_PIN_EVENT:
                                if (message.obj != null) {
                                    WifiP2pProvDiscEvent provDisc = (WifiP2pProvDiscEvent) message.obj;
                                    WifiP2pDevice device3 = provDisc.device;
                                    if (device3 != null) {
                                        if (device3.deviceAddress.equals(P2pStateMachine.this.mSavedPeerConfig.deviceAddress) && P2pStateMachine.this.mSavedPeerConfig.wps.setup == 1) {
                                            P2pStateMachine p2pStateMachine5 = P2pStateMachine.this;
                                            p2pStateMachine5.logd("Found a match " + P2pStateMachine.this.mSavedPeerConfig);
                                            P2pStateMachine.this.mSavedPeerConfig.wps.pin = provDisc.pin;
                                            P2pStateMachine.this.p2pConnectWithPinDisplay(P2pStateMachine.this.mSavedPeerConfig);
                                            P2pStateMachine.this.notifyInvitationSent(provDisc.pin, device3.deviceAddress);
                                            P2pStateMachine.this.transitionTo(P2pStateMachine.this.mGroupNegotiationState);
                                            break;
                                        }
                                    } else {
                                        Log.e(WifiP2pServiceImpl.TAG, "Invalid device");
                                        break;
                                    }
                                } else {
                                    Log.e(WifiP2pServiceImpl.TAG, "Illegal argument(s)");
                                    break;
                                }
                            default:
                                return false;
                        }
                    } else {
                        P2pStateMachine.this.loge("provision discovery failed");
                        P2pStateMachine.this.handleGroupCreationFailure();
                        P2pStateMachine.this.transitionTo(P2pStateMachine.this.mInactiveState);
                    }
                    return true;
                }
            }

            class UserAuthorizingInviteRequestState extends State {
                UserAuthorizingInviteRequestState() {
                }

                public void enter() {
                    P2pStateMachine.this.logd(getName());
                    P2pStateMachine.this.notifyInvitationReceived();
                }

                public boolean processMessage(Message message) {
                    P2pStateMachine p2pStateMachine = P2pStateMachine.this;
                    p2pStateMachine.logd(getName() + message.toString());
                    switch (message.what) {
                        case WifiP2pServiceImpl.PEER_CONNECTION_USER_ACCEPT /*143362*/:
                            P2pStateMachine.this.mWifiNative.p2pStopFind();
                            if (!P2pStateMachine.this.reinvokePersistentGroup(P2pStateMachine.this.mSavedPeerConfig)) {
                                P2pStateMachine.this.p2pConnectWithPinDisplay(P2pStateMachine.this.mSavedPeerConfig);
                            }
                            P2pStateMachine.this.mPeers.updateStatus(P2pStateMachine.this.mSavedPeerConfig.deviceAddress, 1);
                            P2pStateMachine.this.sendPeersChangedBroadcast();
                            P2pStateMachine.this.transitionTo(P2pStateMachine.this.mGroupNegotiationState);
                            break;
                        case WifiP2pServiceImpl.PEER_CONNECTION_USER_REJECT /*143363*/:
                            P2pStateMachine p2pStateMachine2 = P2pStateMachine.this;
                            p2pStateMachine2.logd("User rejected invitation " + P2pStateMachine.this.mSavedPeerConfig);
                            P2pStateMachine.this.mWifiNative.p2pReject(P2pStateMachine.this.mSavedPeerConfig.deviceAddress);
                            P2pStateMachine.this.p2pConnectWithPinDisplay(P2pStateMachine.this.mSavedPeerConfig);
                            P2pStateMachine.this.mWifiNative.p2pFlush();
                            P2pStateMachine.this.mWifiNative.p2pFind(WifiP2pServiceImpl.DISCOVER_TIMEOUT_S);
                            P2pStateMachine.this.transitionTo(P2pStateMachine.this.mInactiveState);
                            break;
                        default:
                            return false;
                    }
                    return true;
                }

                public void exit() {
                }
            }

            class UserAuthorizingJoinState extends State {
                UserAuthorizingJoinState() {
                }

                public void enter() {
                    P2pStateMachine.this.logd(getName());
                    P2pStateMachine.this.notifyInvitationReceived();
                }

                public boolean processMessage(Message message) {
                    P2pStateMachine p2pStateMachine = P2pStateMachine.this;
                    p2pStateMachine.logd(getName() + message.toString());
                    switch (message.what) {
                        case WifiP2pServiceImpl.PEER_CONNECTION_USER_ACCEPT /*143362*/:
                            P2pStateMachine.this.mWifiNative.p2pStopFind();
                            if (P2pStateMachine.this.mSavedPeerConfig.wps.setup == 0) {
                                P2pStateMachine.this.mWifiNative.startWpsPbc(P2pStateMachine.this.mGroup.getInterface(), null);
                            } else {
                                P2pStateMachine.this.mWifiNative.startWpsPinKeypad(P2pStateMachine.this.mGroup.getInterface(), P2pStateMachine.this.mSavedPeerConfig.wps.pin);
                            }
                            P2pStateMachine.this.transitionTo(P2pStateMachine.this.mAfterUserAuthorizingJoinState);
                            break;
                        case WifiP2pServiceImpl.PEER_CONNECTION_USER_REJECT /*143363*/:
                            P2pStateMachine.this.logd("User rejected incoming request");
                            P2pStateMachine.this.transitionTo(P2pStateMachine.this.mAfterUserAuthorizingJoinState);
                            break;
                        case WifiP2pMonitor.P2P_PROV_DISC_PBC_REQ_EVENT:
                        case WifiP2pMonitor.P2P_PROV_DISC_ENTER_PIN_EVENT:
                        case WifiP2pMonitor.P2P_PROV_DISC_SHOW_PIN_EVENT:
                            break;
                        default:
                            return false;
                    }
                    return true;
                }

                public void exit() {
                }
            }

            class UserAuthorizingNegotiationRequestState extends State {
                UserAuthorizingNegotiationRequestState() {
                }

                public void enter() {
                    P2pStateMachine.this.logd(getName());
                    P2pStateMachine.this.notifyInvitationReceived();
                }

                public boolean processMessage(Message message) {
                    P2pStateMachine p2pStateMachine = P2pStateMachine.this;
                    p2pStateMachine.logd(getName() + message.toString());
                    switch (message.what) {
                        case WifiP2pServiceImpl.PEER_CONNECTION_USER_ACCEPT /*143362*/:
                            P2pStateMachine.this.mWifiNative.p2pStopFind();
                            P2pStateMachine.this.p2pConnectWithPinDisplay(P2pStateMachine.this.mSavedPeerConfig);
                            P2pStateMachine.this.mPeers.updateStatus(P2pStateMachine.this.mSavedPeerConfig.deviceAddress, 1);
                            P2pStateMachine.this.sendPeersChangedBroadcast();
                            P2pStateMachine.this.transitionTo(P2pStateMachine.this.mGroupNegotiationState);
                            break;
                        case WifiP2pServiceImpl.PEER_CONNECTION_USER_REJECT /*143363*/:
                            P2pStateMachine p2pStateMachine2 = P2pStateMachine.this;
                            p2pStateMachine2.logd("User rejected negotiation " + P2pStateMachine.this.mSavedPeerConfig);
                            P2pStateMachine.this.mWifiNative.p2pReject(P2pStateMachine.this.mSavedPeerConfig.deviceAddress);
                            P2pStateMachine.this.p2pConnectWithPinDisplay(P2pStateMachine.this.mSavedPeerConfig);
                            P2pStateMachine.this.mPeers.updateStatus(P2pStateMachine.this.mSavedPeerConfig.deviceAddress, 3);
                            P2pStateMachine.this.sendPeersChangedBroadcast();
                            P2pStateMachine.this.logd("p2p_reject delay 1500ms to start find ");
                            P2pStateMachine.this.sendMessageDelayed(139265, 1500);
                            P2pStateMachine.this.transitionTo(P2pStateMachine.this.mInactiveState);
                            break;
                        default:
                            return false;
                    }
                    return true;
                }

                public void exit() {
                }
            }

            P2pStateMachine(String name, Looper looper, boolean p2pSupported) {
                super(name, looper);
                if (WifiP2pServiceHisiExt.hisiWifiEnabled()) {
                    WifiP2pServiceImpl.this.mWifiP2pServiceHisiExt.mWifiP2pInfo = this.mWifiP2pInfo;
                    WifiP2pServiceImpl.this.mWifiP2pServiceHisiExt.mGroup = this.mGroup;
                }
                addState(this.mDefaultState);
                addState(this.mP2pNotSupportedState, this.mDefaultState);
                addState(this.mP2pDisablingState, this.mDefaultState);
                addState(this.mP2pDisabledState, this.mDefaultState);
                addState(this.mP2pEnabledState, this.mDefaultState);
                addState(this.mInactiveState, this.mP2pEnabledState);
                addState(this.mGroupCreatingState, this.mP2pEnabledState);
                addState(this.mUserAuthorizingInviteRequestState, this.mGroupCreatingState);
                addState(this.mUserAuthorizingNegotiationRequestState, this.mGroupCreatingState);
                addState(this.mProvisionDiscoveryState, this.mGroupCreatingState);
                addState(this.mGroupNegotiationState, this.mGroupCreatingState);
                addState(this.mFrequencyConflictState, this.mGroupCreatingState);
                addState(this.mGroupCreatedState, this.mP2pEnabledState);
                addState(this.mUserAuthorizingJoinState, this.mGroupCreatedState);
                addState(this.mAfterUserAuthorizingJoinState, this.mGroupCreatedState);
                addState(this.mOngoingGroupRemovalState, this.mGroupCreatedState);
                if (p2pSupported) {
                    setInitialState(this.mP2pDisabledState);
                } else {
                    setInitialState(this.mP2pNotSupportedState);
                }
                setLogRecSize(50);
                setLogOnlyTransitions(true);
                if (p2pSupported) {
                    WifiP2pServiceImpl.this.mContext.registerReceiver(new BroadcastReceiver(WifiP2pServiceImpl.this) {
                        public void onReceive(Context context, Intent intent) {
                            if (intent.getIntExtra("wifi_state", 4) == 3) {
                                boolean unused = P2pStateMachine.this.mIsWifiEnabled = true;
                                int unused2 = WifiP2pServiceImpl.this.mWifiP2pDevCreateRetry = 0;
                                P2pStateMachine.this.checkAndReEnableP2p();
                            } else {
                                boolean unused3 = P2pStateMachine.this.mIsWifiEnabled = false;
                                P2pStateMachine.this.sendMessage(WifiP2pServiceImpl.DISABLE_P2P);
                            }
                            P2pStateMachine.this.checkAndSendP2pStateChangedBroadcast();
                        }
                    }, new IntentFilter("android.net.wifi.WIFI_STATE_CHANGED"));
                    this.mWifiNative.registerInterfaceAvailableListener(new HalDeviceManager.InterfaceAvailableForRequestListener() {
                        public final void onAvailabilityChanged(boolean z) {
                            WifiP2pServiceImpl.P2pStateMachine.lambda$new$0(WifiP2pServiceImpl.P2pStateMachine.this, z);
                        }
                    }, getHandler());
                }
            }

            public static /* synthetic */ void lambda$new$0(P2pStateMachine p2pStateMachine, boolean isAvailable) {
                p2pStateMachine.mIsInterfaceAvailable = isAvailable;
                if (p2pStateMachine.mIsWifiEnabled && isAvailable && WifiP2pServiceImpl.this.mWifiP2pDevCreateRetry > 5) {
                    Log.d(WifiP2pServiceImpl.TAG, "Endless loop, try to stop create P2P device!");
                    p2pStateMachine.mIsInterfaceAvailable = false;
                    int unused = WifiP2pServiceImpl.this.mWifiP2pDevCreateRetry = 0;
                }
                if (isAvailable) {
                    p2pStateMachine.checkAndReEnableP2p();
                    if (p2pStateMachine.mIsWifiEnabled) {
                        int unused2 = WifiP2pServiceImpl.this.mWifiP2pDevCreateRetry = WifiP2pServiceImpl.this.mWifiP2pDevCreateRetry + 1;
                    }
                }
                p2pStateMachine.checkAndSendP2pStateChangedBroadcast();
                p2pStateMachine.mIsInterfaceAvailable = isAvailable;
            }

            public void registerForWifiMonitorEvents() {
                this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiP2pMonitor.AP_STA_CONNECTED_EVENT, getHandler());
                this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiP2pMonitor.AP_STA_DISCONNECTED_EVENT, getHandler());
                this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiP2pMonitor.P2P_DEVICE_FOUND_EVENT, getHandler());
                this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiP2pMonitor.P2P_DEVICE_LOST_EVENT, getHandler());
                this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiP2pMonitor.P2P_FIND_STOPPED_EVENT, getHandler());
                this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiP2pMonitor.P2P_GO_NEGOTIATION_FAILURE_EVENT, getHandler());
                this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiP2pMonitor.P2P_GO_NEGOTIATION_REQUEST_EVENT, getHandler());
                this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiP2pMonitor.P2P_GO_NEGOTIATION_SUCCESS_EVENT, getHandler());
                this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiP2pMonitor.P2P_GROUP_FORMATION_FAILURE_EVENT, getHandler());
                this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiP2pMonitor.P2P_GROUP_FORMATION_SUCCESS_EVENT, getHandler());
                this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiP2pMonitor.P2P_GROUP_REMOVED_EVENT, getHandler());
                this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiP2pMonitor.P2P_GROUP_STARTED_EVENT, getHandler());
                this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiP2pMonitor.P2P_INVITATION_RECEIVED_EVENT, getHandler());
                this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiP2pMonitor.P2P_INVITATION_RESULT_EVENT, getHandler());
                this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiP2pMonitor.P2P_PROV_DISC_ENTER_PIN_EVENT, getHandler());
                this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiP2pMonitor.P2P_PROV_DISC_FAILURE_EVENT, getHandler());
                this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiP2pMonitor.P2P_PROV_DISC_PBC_REQ_EVENT, getHandler());
                this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiP2pMonitor.P2P_PROV_DISC_PBC_RSP_EVENT, getHandler());
                this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiP2pMonitor.P2P_PROV_DISC_SHOW_PIN_EVENT, getHandler());
                this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiP2pMonitor.P2P_SERV_DISC_RESP_EVENT, getHandler());
                this.mWifiMonitor.registerHandler(this.mInterfaceName, 147457, getHandler());
                this.mWifiMonitor.registerHandler(this.mInterfaceName, 147458, getHandler());
                this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiP2pMonitor.P2P_REMOVE_AND_REFORM_GROUP_EVENT, getHandler());
                this.mWifiMonitor.registerHandler(this.mInterfaceName, 147556, getHandler());
                this.mWifiMonitor.registerHandler(this.mInterfaceName, 147557, getHandler());
                this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiP2pMonitor.HW_P2P_DEVICE_FOUND_EVENT, getHandler());
                this.mWifiMonitor.startMonitoring(this.mInterfaceName);
            }

            public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
                WifiP2pServiceImpl.super.dump(fd, pw, args);
                pw.println("mWifiP2pInfo " + this.mWifiP2pInfo);
                pw.println("mGroup " + this.mGroup);
                pw.println("mSavedPeerConfig " + this.mSavedPeerConfig);
                pw.println("mGroups" + this.mGroups);
                pw.println();
            }

            /* access modifiers changed from: private */
            public void checkAndReEnableP2p() {
                Log.d(WifiP2pServiceImpl.TAG, "Wifi enabled=" + this.mIsWifiEnabled + ", P2P Interface availability=" + this.mIsInterfaceAvailable + ", Number of clients=" + WifiP2pServiceImpl.this.mDeathDataByBinder.size());
                if (this.mIsWifiEnabled && this.mIsInterfaceAvailable && !WifiP2pServiceImpl.this.mDeathDataByBinder.isEmpty()) {
                    sendMessage(WifiP2pServiceImpl.ENABLE_P2P);
                }
            }

            /* access modifiers changed from: private */
            public void checkAndSendP2pStateChangedBroadcast() {
                Log.d(WifiP2pServiceImpl.TAG, "Wifi enabled=" + this.mIsWifiEnabled + ", P2P Interface availability=" + this.mIsInterfaceAvailable);
                sendP2pStateChangedBroadcast(this.mIsWifiEnabled && this.mIsInterfaceAvailable);
            }

            private void sendP2pStateChangedBroadcast(boolean enabled) {
                logd("p2pState change broadcast " + enabled);
                Intent intent = new Intent("android.net.wifi.p2p.STATE_CHANGED");
                intent.addFlags(67108864);
                if (enabled) {
                    intent.putExtra("wifi_p2p_state", 2);
                } else {
                    intent.putExtra("wifi_p2p_state", 1);
                }
                WifiP2pServiceImpl.this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
            }

            /* access modifiers changed from: private */
            public void sendP2pDiscoveryChangedBroadcast(boolean started) {
                int i;
                if (WifiP2pServiceImpl.this.mDiscoveryStarted != started) {
                    boolean unused = WifiP2pServiceImpl.this.mDiscoveryStarted = started;
                    logd("discovery change broadcast " + started);
                    Intent intent = new Intent("android.net.wifi.p2p.DISCOVERY_STATE_CHANGE");
                    intent.addFlags(67108864);
                    if (started) {
                        i = 2;
                    } else {
                        i = 1;
                    }
                    intent.putExtra("discoveryState", i);
                    WifiP2pServiceImpl.this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
                }
            }

            private void sendThisDeviceChangedBroadcast() {
                logd("sending this device change broadcast ");
                Intent intent = new Intent("android.net.wifi.p2p.THIS_DEVICE_CHANGED");
                intent.addFlags(67108864);
                intent.putExtra("wifiP2pDevice", new WifiP2pDevice(WifiP2pServiceImpl.this.mThisDevice));
                WifiP2pServiceImpl.this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
            }

            /* access modifiers changed from: private */
            public void sendPeersChangedBroadcast() {
                logd("sending p2pPeers change broadcast");
                Intent intent = new Intent("android.net.wifi.p2p.PEERS_CHANGED");
                intent.putExtra("wifiP2pDeviceList", new WifiP2pDeviceList(this.mPeers));
                intent.addFlags(67108864);
                WifiP2pServiceImpl.this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
            }

            /* access modifiers changed from: private */
            public void sendP2pConnectionChangedBroadcast() {
                logd("sending p2p connection changed broadcast");
                NetworkInfo networkInfo = new NetworkInfo(WifiP2pServiceImpl.this.mNetworkInfo);
                Intent intent = new Intent("android.net.wifi.p2p.CONNECTION_STATE_CHANGE");
                intent.addFlags(603979776);
                intent.putExtra("wifiP2pInfo", new WifiP2pInfo(this.mWifiP2pInfo));
                intent.putExtra("networkInfo", networkInfo);
                intent.putExtra("p2pGroupInfo", new WifiP2pGroup(this.mGroup));
                initPowerSaveWhitelist(networkInfo);
                WifiP2pServiceImpl.this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
                if (WifiP2pServiceImpl.this.mWifiChannel != null) {
                    WifiP2pServiceImpl.this.mWifiChannel.sendMessage(WifiP2pServiceImpl.P2P_CONNECTION_CHANGED, new NetworkInfo(WifiP2pServiceImpl.this.mNetworkInfo));
                } else {
                    loge("sendP2pConnectionChangedBroadcast(): WifiChannel is null");
                }
            }

            private void initPowerSaveWhitelist(NetworkInfo networkInfo) {
                if (this.mIDeviceIdleController == null) {
                    this.mIDeviceIdleController = IDeviceIdleController.Stub.asInterface(ServiceManager.getService("deviceidle"));
                }
                logd("initPowerSaveWhitelist");
                removeMessages(-1);
                if (networkInfo.isConnected()) {
                    addPowerSaveWhitelist();
                } else {
                    sendMessageDelayed(-1, 2000);
                }
            }

            private void addPowerSaveWhitelist() {
                try {
                    this.mIDeviceIdleController.addPowerSaveWhitelistApp(WifiP2pServiceImpl.PACKAGE_NAME);
                } catch (RemoteException e) {
                    loge("addPowerSaveWhitelistApp RemoteException : " + e.toString());
                }
            }

            /* access modifiers changed from: private */
            public void removePowerSaveWhitelist() {
                try {
                    this.mIDeviceIdleController.removePowerSaveWhitelistApp(WifiP2pServiceImpl.PACKAGE_NAME);
                } catch (RemoteException e) {
                    loge("removePowerSaveWhitelistApp RemoteException : " + e.toString());
                }
            }

            /* access modifiers changed from: private */
            public void sendP2pPersistentGroupsChangedBroadcast() {
                logd("sending p2p persistent groups changed broadcast");
                Intent intent = new Intent("android.net.wifi.p2p.PERSISTENT_GROUPS_CHANGED");
                intent.addFlags(67108864);
                WifiP2pServiceImpl.this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
            }

            /* access modifiers changed from: private */
            public void startDhcpServer(String intf) {
                try {
                    InterfaceConfiguration ifcg = WifiP2pServiceImpl.this.mNwService.getInterfaceConfig(intf);
                    if (WifiP2pServiceImpl.this.mCreateWifiBridge) {
                        String unused = WifiP2pServiceImpl.this.mP2pServerAddress = WifiP2pServiceImpl.this.getWifiRepeaterServerAddress();
                        if (WifiP2pServiceImpl.this.mP2pServerAddress == null) {
                            String unused2 = WifiP2pServiceImpl.this.mP2pServerAddress = WifiP2pServiceImpl.SERVER_ADDRESS_WIFI_BRIDGE;
                        }
                    } else {
                        String unused3 = WifiP2pServiceImpl.this.mP2pServerAddress = WifiP2pServiceImpl.SERVER_ADDRESS;
                    }
                    if (this.mGroup != null) {
                        this.mGroup.setP2pServerAddress(WifiP2pServiceImpl.this.mP2pServerAddress);
                    }
                    ifcg.setLinkAddress(new LinkAddress(NetworkUtils.numericToInetAddress(WifiP2pServiceImpl.this.mP2pServerAddress), 24));
                    boolean unused4 = WifiP2pServiceImpl.this.mCreateWifiBridge = false;
                    ifcg.setInterfaceUp();
                    WifiP2pServiceImpl.this.mNwService.setInterfaceConfig(intf, ifcg);
                    String[] tetheringDhcpRanges = ((ConnectivityManager) WifiP2pServiceImpl.this.mContext.getSystemService("connectivity")).getTetheredDhcpRanges();
                    WifiP2pServiceImpl.this.handleTetheringDhcpRange(tetheringDhcpRanges);
                    if (WifiP2pServiceImpl.this.mNwService.isTetheringStarted()) {
                        logd("Stop existing tethering and restart it");
                        WifiP2pServiceImpl.this.mNwService.stopTethering();
                    }
                    WifiP2pServiceImpl.this.mNwService.tetherInterface(intf);
                    WifiP2pServiceImpl.this.mNwService.startTethering(tetheringDhcpRanges);
                    logd("Started Dhcp server on " + intf);
                } catch (Exception e) {
                    boolean unused5 = WifiP2pServiceImpl.this.mCreateWifiBridge = false;
                    loge("Error configuring interface " + intf + ", :" + e);
                }
            }

            private void stopDhcpServer(String intf) {
                String str;
                try {
                    WifiP2pServiceImpl.this.mNwService.untetherInterface(intf);
                    for (String temp : WifiP2pServiceImpl.this.mNwService.listTetheredInterfaces()) {
                        logd("List all interfaces " + temp);
                        if (temp.compareTo(intf) != 0) {
                            logd("Found other tethering interfaces, so keep tethering alive");
                            return;
                        }
                    }
                    WifiP2pServiceImpl.this.mNwService.stopTethering();
                    logd("Stopped Dhcp server");
                } catch (Exception e) {
                    loge("Error stopping Dhcp server" + e);
                } finally {
                    str = "Stopped Dhcp server";
                    logd(str);
                }
            }

            private void notifyP2pEnableFailure() {
                Resources r = Resources.getSystem();
                AlertDialog dialog = new AlertDialog.Builder(WifiP2pServiceImpl.this.mContext).setTitle(r.getString(17041401)).setMessage(r.getString(17041405)).setPositiveButton(r.getString(17039370), null).create();
                dialog.setCanceledOnTouchOutside(false);
                dialog.getWindow().setType(2003);
                WindowManager.LayoutParams attrs = dialog.getWindow().getAttributes();
                attrs.privateFlags = 16;
                dialog.getWindow().setAttributes(attrs);
                dialog.show();
            }

            private void addRowToDialog(ViewGroup group, int stringId, String value) {
                Resources r = Resources.getSystem();
                View row = LayoutInflater.from(WifiP2pServiceImpl.this.mContext).inflate(17367337, group, false);
                ((TextView) row.findViewById(16909113)).setText(r.getString(stringId));
                ((TextView) row.findViewById(16909513)).setText(value);
                group.addView(row);
            }

            /* access modifiers changed from: protected */
            public void notifyInvitationSent(String pin, String peerAddress) {
                Resources r = Resources.getSystem();
                View textEntryView = LayoutInflater.from(WifiP2pServiceImpl.this.mContext).inflate(17367335, null);
                ViewGroup group = (ViewGroup) textEntryView.findViewById(16908998);
                addRowToDialog(group, 17041411, getDeviceName(peerAddress));
                addRowToDialog(group, 17041410, pin);
                AlertDialog dialog = new AlertDialog.Builder(WifiP2pServiceImpl.this.mContext).setTitle(r.getString(17041408)).setView(textEntryView).setPositiveButton(r.getString(17039370), null).create();
                dialog.setCanceledOnTouchOutside(false);
                dialog.getWindow().setType(2003);
                WindowManager.LayoutParams attrs = dialog.getWindow().getAttributes();
                attrs.privateFlags = 16;
                dialog.getWindow().setAttributes(attrs);
                dialog.show();
            }

            /* access modifiers changed from: private */
            public void notifyP2pProvDiscShowPinRequest(String pin, String peerAddress) {
                Resources r = Resources.getSystem();
                final String tempDevAddress = peerAddress;
                final String tempPin = pin;
                View textEntryView = LayoutInflater.from(WifiP2pServiceImpl.this.mContext).inflate(17367335, null);
                ViewGroup group = (ViewGroup) textEntryView.findViewById(16908998);
                addRowToDialog(group, 17041411, getDeviceName(peerAddress));
                addRowToDialog(group, 17041410, pin);
                AlertDialog dialog = new AlertDialog.Builder(WifiP2pServiceImpl.this.mContext).setTitle(r.getString(17041408)).setView(textEntryView).setPositiveButton(r.getString(17039525), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        P2pStateMachine.this.mSavedPeerConfig = new WifiP2pConfig();
                        P2pStateMachine.this.mSavedPeerConfig.deviceAddress = tempDevAddress;
                        P2pStateMachine.this.mSavedPeerConfig.wps.setup = 1;
                        P2pStateMachine.this.mSavedPeerConfig.wps.pin = tempPin;
                        P2pStateMachine.this.mWifiNative.p2pConnect(P2pStateMachine.this.mSavedPeerConfig, WifiP2pServiceImpl.FORM_GROUP.booleanValue());
                    }
                }).create();
                dialog.setCanceledOnTouchOutside(false);
                dialog.getWindow().setType(2003);
                WindowManager.LayoutParams attrs = dialog.getWindow().getAttributes();
                attrs.privateFlags = 16;
                dialog.getWindow().setAttributes(attrs);
                dialog.show();
            }

            /* access modifiers changed from: private */
            public void processStatistics(Context mContext, int eventID, int choice) {
                JSONObject eventMsg = new JSONObject();
                try {
                    eventMsg.put(HwWifiBigDataConstant.KEY_CHOICE, choice);
                } catch (JSONException e) {
                    loge("processStatistics put error." + e);
                }
                Flog.bdReport(mContext, eventID, eventMsg);
            }

            /* access modifiers changed from: private */
            public void notifyInvitationReceived() {
                if (!WifiP2pServiceImpl.this.autoAcceptConnection()) {
                    Resources r = Resources.getSystem();
                    final WpsInfo wps = this.mSavedPeerConfig.wps;
                    View textEntryView = LayoutInflater.from(WifiP2pServiceImpl.this.mContext).inflate(17367335, null);
                    ViewGroup group = (ViewGroup) textEntryView.findViewById(16908998);
                    View row = LayoutInflater.from(WifiP2pServiceImpl.this.mContext).inflate(17367336, group, false);
                    ((TextView) row.findViewById(16909113)).setText(String.format(r.getString(33685699), new Object[]{getDeviceName(this.mSavedPeerConfig.deviceAddress)}));
                    ((TextView) row.findViewById(16909113)).setTextColor(-16777216);
                    group.addView(row);
                    final EditText pin = (EditText) textEntryView.findViewById(16909534);
                    AlertDialog dialog = new AlertDialog.Builder(WifiP2pServiceImpl.this.mContext, 33947691).setTitle(r.getString(33685701)).setView(textEntryView).setPositiveButton(r.getString(33685700), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (wps.setup == 2) {
                                P2pStateMachine.this.mSavedPeerConfig.wps.pin = pin.getText().toString();
                            }
                            P2pStateMachine p2pStateMachine = P2pStateMachine.this;
                            p2pStateMachine.logd(P2pStateMachine.this.getName() + " accept invitation " + P2pStateMachine.this.mSavedPeerConfig);
                            P2pStateMachine.this.sendMessage(WifiP2pServiceImpl.PEER_CONNECTION_USER_ACCEPT);
                            P2pStateMachine.this.processStatistics(WifiP2pServiceImpl.this.mContext, SupplicantStaIfaceHal.HAL_CALL_THRESHOLD_MS, 0);
                        }
                    }).setNegativeButton(r.getString(17039360), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            P2pStateMachine p2pStateMachine = P2pStateMachine.this;
                            p2pStateMachine.logd(P2pStateMachine.this.getName() + " ignore connect");
                            P2pStateMachine.this.sendMessage(WifiP2pServiceImpl.PEER_CONNECTION_USER_REJECT);
                            P2pStateMachine.this.processStatistics(WifiP2pServiceImpl.this.mContext, SupplicantStaIfaceHal.HAL_CALL_THRESHOLD_MS, 1);
                        }
                    }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                        public void onCancel(DialogInterface arg0) {
                            P2pStateMachine p2pStateMachine = P2pStateMachine.this;
                            p2pStateMachine.logd(P2pStateMachine.this.getName() + " ignore connect");
                            P2pStateMachine.this.sendMessage(WifiP2pServiceImpl.PEER_CONNECTION_USER_REJECT);
                            P2pStateMachine.this.processStatistics(WifiP2pServiceImpl.this.mContext, SupplicantStaIfaceHal.HAL_CALL_THRESHOLD_MS, 1);
                        }
                    }).create();
                    dialog.setCanceledOnTouchOutside(false);
                    switch (wps.setup) {
                        case 1:
                            logd("Shown pin section visible");
                            addRowToDialog(group, 17041410, wps.pin);
                            break;
                        case 2:
                            logd("Enter pin section visible");
                            textEntryView.findViewById(16908877).setVisibility(0);
                            break;
                    }
                    if ((r.getConfiguration().uiMode & 5) == 5) {
                        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                                if (keyCode != 164) {
                                    return false;
                                }
                                P2pStateMachine.this.sendMessage(WifiP2pServiceImpl.PEER_CONNECTION_USER_ACCEPT);
                                dialog.dismiss();
                                return true;
                            }
                        });
                    }
                    dialog.getWindow().setType(2003);
                    WindowManager.LayoutParams attrs = dialog.getWindow().getAttributes();
                    attrs.privateFlags = 16;
                    dialog.getWindow().setAttributes(attrs);
                    dialog.show();
                }
            }

            /* access modifiers changed from: protected */
            public void updatePersistentNetworks(boolean reload) {
                if (reload) {
                    this.mGroups.clear();
                }
                if (this.mWifiNative.p2pListNetworks(this.mGroups) || reload) {
                    for (WifiP2pGroup group : this.mGroups.getGroupList()) {
                        if (group.getOwner() != null && WifiP2pServiceImpl.this.mThisDevice.deviceAddress.equals(group.getOwner().deviceAddress)) {
                            group.setOwner(WifiP2pServiceImpl.this.mThisDevice);
                        }
                    }
                    this.mWifiNative.saveConfig();
                    sendP2pPersistentGroupsChangedBroadcast();
                }
            }

            /* access modifiers changed from: private */
            public boolean isConfigInvalid(WifiP2pConfig config) {
                if (config == null || TextUtils.isEmpty(config.deviceAddress) || this.mPeers.get(config.deviceAddress) == null) {
                    return true;
                }
                return false;
            }

            private WifiP2pDevice fetchCurrentDeviceDetails(WifiP2pConfig config) {
                if (config == null) {
                    return null;
                }
                this.mPeers.updateGroupCapability(config.deviceAddress, this.mWifiNative.getGroupCapability(config.deviceAddress));
                return this.mPeers.get(config.deviceAddress);
            }

            private boolean isMiracastDevice(String deviceType) {
                if (deviceType == null) {
                    return false;
                }
                String[] tokens = deviceType.split("-");
                try {
                    if (tokens.length > 0 && Integer.parseInt(tokens[0]) == 7) {
                        logd("As connecting miracast device ,set go_intent = 14 to let it works as GO ");
                        return true;
                    }
                } catch (NumberFormatException e) {
                    loge("isMiracastDevice: " + e);
                }
                return false;
            }

            private boolean wifiIsConnected() {
                if (WifiP2pServiceImpl.this.mContext == null) {
                    return false;
                }
                WifiManager wifiMgr = (WifiManager) WifiP2pServiceImpl.this.mContext.getSystemService("wifi");
                if (wifiMgr != null && wifiMgr.getWifiState() == 3) {
                    NetworkInfo wifiInfo = ((ConnectivityManager) WifiP2pServiceImpl.this.mContext.getSystemService("connectivity")).getNetworkInfo(1);
                    if (wifiInfo != null) {
                        logd("wifiIsConnected: " + wifiInfo.isConnected());
                        return wifiInfo.isConnected();
                    }
                }
                return false;
            }

            /* access modifiers changed from: private */
            public void p2pConnectWithPinDisplay(WifiP2pConfig config) {
                if (config == null) {
                    Log.e(WifiP2pServiceImpl.TAG, "Illegal argument(s)");
                    return;
                }
                WifiP2pDevice dev = fetchCurrentDeviceDetails(config);
                if (dev == null) {
                    loge("target device is not found " + config.deviceAddress);
                    return;
                }
                if ((dev.primaryDeviceType != null && isMiracastDevice(dev.primaryDeviceType)) || wifiIsConnected()) {
                    logd("set groupOwnerIntent is 14");
                    config.groupOwnerIntent = 14;
                }
                String pin = this.mWifiNative.p2pConnect(config, dev.isGroupOwner());
                try {
                    Integer.parseInt(pin);
                    notifyInvitationSent(pin, config.deviceAddress);
                } catch (NumberFormatException e) {
                }
                boolean unused = WifiP2pServiceImpl.this.mIsInvite = false;
            }

            /* access modifiers changed from: private */
            public boolean reinvokePersistentGroup(WifiP2pConfig config) {
                if (config == null) {
                    Log.e(WifiP2pServiceImpl.TAG, "Illegal argument(s)");
                    return false;
                }
                WifiP2pDevice dev = fetchCurrentDeviceDetails(config);
                if (dev == null) {
                    Log.e(WifiP2pServiceImpl.TAG, "Invalid device");
                    return false;
                }
                boolean join = dev.isGroupOwner();
                String ssid = this.mWifiNative.p2pGetSsid(dev.deviceAddress);
                logd("target ssid is " + ssid + " join:" + join);
                if (join && dev.isGroupLimit()) {
                    logd("target device reaches group limit.");
                    join = false;
                } else if (join) {
                    int netId = this.mGroups.getNetworkId(dev.deviceAddress, ssid);
                    if (netId >= 0) {
                        if (!this.mWifiNative.p2pGroupAdd(netId)) {
                            return false;
                        }
                        sendReinvokePGBroadcast(netId);
                        return true;
                    }
                }
                if (join || !dev.isDeviceLimit()) {
                    if (!join && dev.isInvitationCapable()) {
                        int netId2 = -2;
                        if (config.netId < 0) {
                            netId2 = this.mGroups.getNetworkId(dev.deviceAddress);
                        } else if (config.deviceAddress.equals(this.mGroups.getOwnerAddr(config.netId))) {
                            netId2 = config.netId;
                        }
                        if (netId2 < 0) {
                            netId2 = getNetworkIdFromClientList(dev.deviceAddress);
                        }
                        logd("netId related with " + dev.deviceAddress + " = " + netId2);
                        if (netId2 >= 0) {
                            if (this.mWifiNative.p2pReinvoke(netId2, dev.deviceAddress)) {
                                config.netId = netId2;
                                sendReinvokePGBroadcast(netId2);
                                return true;
                            }
                            loge("p2pReinvoke() failed, update networks");
                            updatePersistentNetworks(WifiP2pServiceImpl.RELOAD.booleanValue());
                            return false;
                        }
                    }
                    return false;
                }
                loge("target device reaches the device limit.");
                return false;
            }

            private void sendReinvokePGBroadcast(int netId) {
                Intent intent = new Intent("com.huawei.net.wifi.p2p.REINVOKE_PERSISTENT_GROUP_ACTION");
                intent.putExtra("com.huawei.net.wifi.p2p.EXTRA_REINVOKE_NETID", netId);
                WifiP2pServiceImpl.this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, "com.huawei.permission.REINVOKE_PERSISTENT");
            }

            /* access modifiers changed from: protected */
            public int getNetworkIdFromClientList(String deviceAddress) {
                if (deviceAddress == null) {
                    return -1;
                }
                for (WifiP2pGroup group : this.mGroups.getGroupList()) {
                    int netId = group.getNetworkId();
                    String[] p2pClientList = getClientList(netId);
                    if (p2pClientList != null) {
                        for (String client : p2pClientList) {
                            if (deviceAddress.equalsIgnoreCase(client)) {
                                return netId;
                            }
                        }
                        continue;
                    }
                }
                return -1;
            }

            private String[] getClientList(int netId) {
                String p2pClients = this.mWifiNative.getP2pClientList(netId);
                if (p2pClients == null) {
                    return null;
                }
                return p2pClients.split(" ");
            }

            /* access modifiers changed from: private */
            public boolean removeClientFromList(int netId, String addr, boolean isRemovable) {
                StringBuilder modifiedClientList = new StringBuilder();
                String[] currentClientList = getClientList(netId);
                boolean isClientRemoved = false;
                if (currentClientList != null) {
                    boolean isClientRemoved2 = false;
                    for (String client : currentClientList) {
                        if (!client.equalsIgnoreCase(addr)) {
                            modifiedClientList.append(" ");
                            modifiedClientList.append(client);
                        } else {
                            isClientRemoved2 = true;
                        }
                    }
                    isClientRemoved = isClientRemoved2;
                }
                if (modifiedClientList.length() == 0 && isRemovable) {
                    logd("Remove unknown network");
                    this.mGroups.remove(netId);
                    return true;
                } else if (!isClientRemoved) {
                    return false;
                } else {
                    logd("Modified client list: " + modifiedClientList);
                    if (modifiedClientList.length() == 0) {
                        modifiedClientList.append("\"\"");
                    }
                    this.mWifiNative.setP2pClientList(netId, modifiedClientList.toString());
                    this.mWifiNative.saveConfig();
                    return true;
                }
            }

            /* access modifiers changed from: private */
            public void setWifiP2pInfoOnGroupFormation(InetAddress serverInetAddress) {
                this.mWifiP2pInfo.groupFormed = true;
                this.mWifiP2pInfo.isGroupOwner = this.mGroup.isGroupOwner();
                this.mWifiP2pInfo.groupOwnerAddress = serverInetAddress;
            }

            /* access modifiers changed from: private */
            public void resetWifiP2pInfo() {
                this.mWifiP2pInfo.groupFormed = false;
                this.mWifiP2pInfo.isGroupOwner = false;
                this.mWifiP2pInfo.groupOwnerAddress = null;
            }

            /* access modifiers changed from: private */
            public String getDeviceName(String deviceAddress) {
                WifiP2pDevice d = this.mPeers.get(deviceAddress);
                if (d != null) {
                    return d.deviceName;
                }
                return deviceAddress;
            }

            private String getCustomDeviceName(String deviceName) {
                String deviceName2;
                if (!SystemProperties.getBoolean("ro.config.hw_wifi_bt_name", false) || !TextUtils.isEmpty(deviceName)) {
                    return deviceName;
                }
                StringBuilder sb = new StringBuilder();
                String uuidStr = UUID.randomUUID().toString();
                String marketing_name = SystemProperties.get("ro.config.marketing_name");
                if (!TextUtils.isEmpty(marketing_name)) {
                    sb.append(marketing_name);
                    sb.append("_");
                    sb.append(uuidStr.substring(24, 28).toUpperCase(Locale.ENGLISH));
                    deviceName2 = sb.toString();
                } else {
                    sb.append("HUAWEI ");
                    sb.append(Build.PRODUCT);
                    sb.append("_");
                    sb.append(uuidStr.substring(24, 28).toUpperCase(Locale.ENGLISH));
                    deviceName2 = sb.toString();
                }
                Settings.Global.putString(WifiP2pServiceImpl.this.mContext.getContentResolver(), "wifi_p2p_device_name", deviceName2);
                return deviceName2;
            }

            private String getPersistedDeviceName() {
                String deviceName = getCustomDeviceName(Settings.Global.getString(WifiP2pServiceImpl.this.mContext.getContentResolver(), "wifi_p2p_device_name"));
                if (deviceName != null) {
                    return deviceName;
                }
                String deviceName2 = SystemProperties.get("ro.config.marketing_name");
                if (!TextUtils.isEmpty(deviceName2)) {
                    return deviceName2;
                }
                String id = Settings.Secure.getString(WifiP2pServiceImpl.this.mContext.getContentResolver(), "android_id");
                if (id == null || id.length() <= 4 || WifiP2pServiceImpl.IS_ATT || WifiP2pServiceImpl.IS_VERIZON) {
                    return Build.MODEL;
                }
                return Build.MODEL + "_" + id.substring(0, 4);
            }

            /* access modifiers changed from: private */
            public boolean setAndPersistDeviceName(String devName) {
                if (devName == null) {
                    return false;
                }
                if (!this.mWifiNative.setDeviceName(devName)) {
                    loge("Failed to set device name " + devName);
                    return false;
                }
                WifiP2pServiceImpl.this.mThisDevice.deviceName = devName;
                WifiP2pNative wifiP2pNative = this.mWifiNative;
                wifiP2pNative.setP2pSsidPostfix("-" + getSsidPostFix(WifiP2pServiceImpl.this.mThisDevice.deviceName));
                Settings.Global.putString(WifiP2pServiceImpl.this.mContext.getContentResolver(), "wifi_p2p_device_name", devName);
                sendThisDeviceChangedBroadcast();
                return true;
            }

            /* access modifiers changed from: private */
            public boolean setWfdInfo(WifiP2pWfdInfo wfdInfo) {
                boolean success;
                if (!wfdInfo.isWfdEnabled()) {
                    success = this.mWifiNative.setWfdEnable(false);
                } else {
                    success = this.mWifiNative.setWfdEnable(true) && this.mWifiNative.setWfdDeviceInfo(wfdInfo.getDeviceInfoHex());
                }
                if (!success) {
                    loge("Failed to set wfd properties");
                    return false;
                }
                WifiP2pServiceImpl.this.mThisDevice.wfdInfo = wfdInfo;
                sendThisDeviceChangedBroadcast();
                return true;
            }

            /* access modifiers changed from: private */
            public void initializeP2pSettings() {
                WifiP2pServiceImpl.this.mThisDevice.deviceName = getPersistedDeviceName();
                this.mWifiNative.setP2pDeviceName(WifiP2pServiceImpl.this.mThisDevice.deviceName);
                WifiP2pNative wifiP2pNative = this.mWifiNative;
                wifiP2pNative.setP2pSsidPostfix("-" + getSsidPostFix(WifiP2pServiceImpl.this.mThisDevice.deviceName));
                this.mWifiNative.setP2pDeviceType(WifiP2pServiceImpl.this.mThisDevice.primaryDeviceType);
                this.mWifiNative.setConfigMethods("virtual_push_button physical_display keypad");
                WifiP2pServiceImpl.this.mThisDevice.deviceAddress = this.mWifiNative.p2pGetDeviceAddress();
                updateThisDevice(3);
                WifiP2pServiceImpl.this.mClientInfoList.clear();
                this.mWifiNative.p2pFlush();
                this.mWifiNative.p2pServiceFlush();
                byte unused = WifiP2pServiceImpl.this.mServiceTransactionId = (byte) 0;
                String unused2 = WifiP2pServiceImpl.this.mServiceDiscReqId = null;
                WifiP2pServiceImpl.this.clearValidDeivceList();
                updatePersistentNetworks(WifiP2pServiceImpl.RELOAD.booleanValue());
            }

            /* access modifiers changed from: private */
            public void updateThisDevice(int status) {
                WifiP2pServiceImpl.this.mThisDevice.status = status;
                sendThisDeviceChangedBroadcast();
            }

            /* access modifiers changed from: private */
            public void handleGroupCreationFailure() {
                resetWifiP2pInfo();
                WifiP2pServiceImpl.this.mNetworkInfo.setDetailedState(NetworkInfo.DetailedState.FAILED, null, null);
                sendP2pConnectionChangedBroadcast();
                boolean peersChanged = this.mPeers.remove(this.mPeersLostDuringConnection);
                if (!TextUtils.isEmpty(this.mSavedPeerConfig.deviceAddress) && this.mPeers.remove(this.mSavedPeerConfig.deviceAddress) != null) {
                    peersChanged = true;
                }
                if (peersChanged) {
                    sendPeersChangedBroadcast();
                }
                this.mPeersLostDuringConnection.clear();
                String unused = WifiP2pServiceImpl.this.mServiceDiscReqId = null;
                sendMessage(139265);
            }

            /* access modifiers changed from: private */
            public void handleGroupRemoved() {
                if (this.mGroup.isGroupOwner()) {
                    if (WifiP2pServiceHisiExt.hisiWifiEnabled()) {
                        stopDhcpServer("wlan0");
                    } else {
                        stopDhcpServer(this.mGroup.getInterface());
                    }
                } else if (!WifiP2pServiceImpl.this.getMagicLinkDeviceFlag()) {
                    logd("stop IpManager");
                    WifiP2pServiceImpl.this.stopIpClient();
                    try {
                        WifiP2pServiceImpl.this.mNwService.removeInterfaceFromLocalNetwork(this.mGroup.getInterface());
                    } catch (RemoteException e) {
                        loge("Failed to remove iface from local network " + e);
                    }
                } else {
                    WifiP2pServiceImpl.this.setmMagicLinkDeviceFlag(false);
                }
                try {
                    if (WifiP2pServiceHisiExt.hisiWifiEnabled()) {
                        WifiP2pServiceImpl.this.mNwService.clearInterfaceAddresses("wlan0");
                    } else {
                        WifiP2pServiceImpl.this.mNwService.clearInterfaceAddresses(this.mGroup.getInterface());
                    }
                } catch (Exception e2) {
                    loge("Failed to clear addresses " + e2);
                }
                this.mWifiNative.setP2pGroupIdle(this.mGroup.getInterface(), 0);
                boolean peersChanged = false;
                for (WifiP2pDevice d : this.mGroup.getClientList()) {
                    if (this.mPeers.remove(d)) {
                        peersChanged = true;
                    }
                }
                if (this.mPeers.remove(this.mGroup.getOwner())) {
                    peersChanged = true;
                }
                if (this.mPeers.remove(this.mPeersLostDuringConnection)) {
                    peersChanged = true;
                }
                if (peersChanged) {
                    sendPeersChangedBroadcast();
                }
                this.mGroup = null;
                this.mPeersLostDuringConnection.clear();
                String unused = WifiP2pServiceImpl.this.mServiceDiscReqId = null;
                if (WifiP2pServiceImpl.this.mTemporarilyDisconnectedWifi) {
                    if (WifiP2pServiceImpl.this.mWifiChannel != null) {
                        WifiP2pServiceImpl.this.mWifiChannel.sendMessage(WifiP2pServiceImpl.DISCONNECT_WIFI_REQUEST, 0);
                    } else {
                        loge("handleGroupRemoved(): WifiChannel is null");
                    }
                    boolean unused2 = WifiP2pServiceImpl.this.mTemporarilyDisconnectedWifi = false;
                }
                WifiP2pServiceImpl.this.notifyRptGroupRemoved();
            }

            /* access modifiers changed from: protected */
            public void replyToMessage(Message msg, int what) {
                if (msg.replyTo != null) {
                    Message dstMsg = obtainMessage(msg);
                    dstMsg.what = what;
                    WifiP2pServiceImpl.this.mReplyChannel.replyToMessage(msg, dstMsg);
                }
            }

            /* access modifiers changed from: private */
            public void replyToMessage(Message msg, int what, int arg1) {
                if (msg.replyTo != null) {
                    Message dstMsg = obtainMessage(msg);
                    dstMsg.what = what;
                    dstMsg.arg1 = arg1;
                    WifiP2pServiceImpl.this.mReplyChannel.replyToMessage(msg, dstMsg);
                }
            }

            /* access modifiers changed from: private */
            public void replyToMessage(Message msg, int what, Object obj) {
                if (msg.replyTo != null) {
                    Message dstMsg = obtainMessage(msg);
                    dstMsg.what = what;
                    dstMsg.obj = obj;
                    WifiP2pServiceImpl.this.mReplyChannel.replyToMessage(msg, dstMsg);
                }
            }

            private Message obtainMessage(Message srcMsg) {
                Message msg = Message.obtain();
                msg.arg2 = srcMsg.arg2;
                return msg;
            }

            /* access modifiers changed from: protected */
            public void logd(String s) {
                Slog.d(WifiP2pServiceImpl.TAG, s);
            }

            /* access modifiers changed from: protected */
            public void loge(String s) {
                Slog.e(WifiP2pServiceImpl.TAG, s);
            }

            /* access modifiers changed from: private */
            public boolean updateSupplicantServiceRequest() {
                clearSupplicantServiceRequest();
                StringBuffer sb = new StringBuffer();
                Iterator it = WifiP2pServiceImpl.this.mClientInfoList.values().iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    ClientInfo c = (ClientInfo) it.next();
                    for (int i = 0; i < c.mReqList.size(); i++) {
                        WifiP2pServiceRequest req = (WifiP2pServiceRequest) c.mReqList.valueAt(i);
                        if (req != null) {
                            sb.append(req.getSupplicantQuery());
                        }
                    }
                }
                if (sb.length() == 0) {
                    return false;
                }
                String unused = WifiP2pServiceImpl.this.mServiceDiscReqId = this.mWifiNative.p2pServDiscReq(WifiP2pServiceImpl.EMPTY_DEVICE_ADDRESS, sb.toString());
                if (WifiP2pServiceImpl.this.mServiceDiscReqId == null) {
                    return false;
                }
                return true;
            }

            /* access modifiers changed from: private */
            public void clearSupplicantServiceRequest() {
                if (WifiP2pServiceImpl.this.mServiceDiscReqId != null) {
                    this.mWifiNative.p2pServDiscCancelReq(WifiP2pServiceImpl.this.mServiceDiscReqId);
                    String unused = WifiP2pServiceImpl.this.mServiceDiscReqId = null;
                }
            }

            /* access modifiers changed from: private */
            public boolean addServiceRequest(Messenger m, WifiP2pServiceRequest req) {
                if (m == null || req == null) {
                    Log.e(WifiP2pServiceImpl.TAG, "Illegal argument(s)");
                    return false;
                }
                clearClientDeadChannels();
                ClientInfo clientInfo = getClientInfo(m, true);
                if (clientInfo == null) {
                    return false;
                }
                WifiP2pServiceImpl.access$8704(WifiP2pServiceImpl.this);
                if (WifiP2pServiceImpl.this.mServiceTransactionId == 0) {
                    WifiP2pServiceImpl.access$8704(WifiP2pServiceImpl.this);
                }
                req.setTransactionId(WifiP2pServiceImpl.this.mServiceTransactionId);
                clientInfo.mReqList.put(WifiP2pServiceImpl.this.mServiceTransactionId, req);
                if (WifiP2pServiceImpl.this.mServiceDiscReqId == null) {
                    return true;
                }
                return updateSupplicantServiceRequest();
            }

            /* access modifiers changed from: private */
            public void removeServiceRequest(Messenger m, WifiP2pServiceRequest req) {
                if (m == null || req == null) {
                    Log.e(WifiP2pServiceImpl.TAG, "Illegal argument(s)");
                }
                int i = 0;
                ClientInfo clientInfo = getClientInfo(m, false);
                if (clientInfo != null) {
                    boolean removed = false;
                    while (true) {
                        if (i >= clientInfo.mReqList.size()) {
                            break;
                        } else if (req.equals(clientInfo.mReqList.valueAt(i))) {
                            removed = true;
                            clientInfo.mReqList.removeAt(i);
                            break;
                        } else {
                            i++;
                        }
                    }
                    if (removed) {
                        if (clientInfo.mReqList.size() == 0 && clientInfo.mServList.size() == 0) {
                            logd("remove client information from framework");
                            WifiP2pServiceImpl.this.mClientInfoList.remove(clientInfo.mMessenger);
                        }
                        if (WifiP2pServiceImpl.this.mServiceDiscReqId != null) {
                            updateSupplicantServiceRequest();
                        }
                    }
                }
            }

            /* access modifiers changed from: private */
            public void clearServiceRequests(Messenger m) {
                if (m == null) {
                    Log.e(WifiP2pServiceImpl.TAG, "Illegal argument(s)");
                    return;
                }
                ClientInfo clientInfo = getClientInfo(m, false);
                if (clientInfo != null && clientInfo.mReqList.size() != 0) {
                    clientInfo.mReqList.clear();
                    if (clientInfo.mServList.size() == 0) {
                        logd("remove channel information from framework");
                        WifiP2pServiceImpl.this.mClientInfoList.remove(clientInfo.mMessenger);
                    }
                    if (WifiP2pServiceImpl.this.mServiceDiscReqId != null) {
                        updateSupplicantServiceRequest();
                    }
                }
            }

            /* access modifiers changed from: private */
            public boolean addLocalService(Messenger m, WifiP2pServiceInfo servInfo) {
                if (m == null || servInfo == null) {
                    Log.e(WifiP2pServiceImpl.TAG, "Illegal arguments");
                    return false;
                }
                clearClientDeadChannels();
                ClientInfo clientInfo = getClientInfo(m, true);
                if (clientInfo == null || !clientInfo.mServList.add(servInfo)) {
                    return false;
                }
                if (this.mWifiNative.p2pServiceAdd(servInfo)) {
                    return true;
                }
                clientInfo.mServList.remove(servInfo);
                return false;
            }

            /* access modifiers changed from: private */
            public void removeLocalService(Messenger m, WifiP2pServiceInfo servInfo) {
                if (m == null || servInfo == null) {
                    Log.e(WifiP2pServiceImpl.TAG, "Illegal arguments");
                    return;
                }
                ClientInfo clientInfo = getClientInfo(m, false);
                if (clientInfo != null) {
                    this.mWifiNative.p2pServiceDel(servInfo);
                    clientInfo.mServList.remove(servInfo);
                    if (clientInfo.mReqList.size() == 0 && clientInfo.mServList.size() == 0) {
                        logd("remove client information from framework");
                        WifiP2pServiceImpl.this.mClientInfoList.remove(clientInfo.mMessenger);
                    }
                }
            }

            /* access modifiers changed from: private */
            public void clearLocalServices(Messenger m) {
                if (m == null) {
                    Log.e(WifiP2pServiceImpl.TAG, "Illegal argument(s)");
                    return;
                }
                ClientInfo clientInfo = getClientInfo(m, false);
                if (clientInfo != null) {
                    for (WifiP2pServiceInfo servInfo : clientInfo.mServList) {
                        this.mWifiNative.p2pServiceDel(servInfo);
                    }
                    clientInfo.mServList.clear();
                    if (clientInfo.mReqList.size() == 0) {
                        logd("remove client information from framework");
                        WifiP2pServiceImpl.this.mClientInfoList.remove(clientInfo.mMessenger);
                    }
                }
            }

            private void clearClientInfo(Messenger m) {
                clearLocalServices(m);
                clearServiceRequests(m);
            }

            /* access modifiers changed from: private */
            public void sendServiceResponse(WifiP2pServiceResponse resp) {
                if (resp == null) {
                    Log.e(WifiP2pServiceImpl.TAG, "sendServiceResponse with null response");
                    return;
                }
                for (ClientInfo c : WifiP2pServiceImpl.this.mClientInfoList.values()) {
                    if (((WifiP2pServiceRequest) c.mReqList.get(resp.getTransactionId())) != null) {
                        Message msg = Message.obtain();
                        msg.what = 139314;
                        msg.arg1 = 0;
                        msg.arg2 = 0;
                        msg.obj = resp;
                        if (c.mMessenger != null) {
                            try {
                                c.mMessenger.send(msg);
                            } catch (RemoteException e) {
                                logd("detect dead channel");
                                clearClientInfo(c.mMessenger);
                                return;
                            }
                        }
                    }
                }
            }

            private void clearClientDeadChannels() {
                ArrayList<Messenger> deadClients = new ArrayList<>();
                for (ClientInfo c : WifiP2pServiceImpl.this.mClientInfoList.values()) {
                    Message msg = Message.obtain();
                    msg.what = 139313;
                    msg.arg1 = 0;
                    msg.arg2 = 0;
                    msg.obj = null;
                    if (c.mMessenger != null) {
                        try {
                            c.mMessenger.send(msg);
                        } catch (RemoteException e) {
                            logd("detect dead channel");
                            deadClients.add(c.mMessenger);
                        }
                    }
                }
                Iterator<Messenger> it = deadClients.iterator();
                while (it.hasNext()) {
                    clearClientInfo(it.next());
                }
            }

            private ClientInfo getClientInfo(Messenger m, boolean createIfNotExist) {
                ClientInfo clientInfo = (ClientInfo) WifiP2pServiceImpl.this.mClientInfoList.get(m);
                if (clientInfo != null || !createIfNotExist) {
                    return clientInfo;
                }
                logd("add a new client");
                ClientInfo clientInfo2 = new ClientInfo(m);
                WifiP2pServiceImpl.this.mClientInfoList.put(m, clientInfo2);
                return clientInfo2;
            }

            /* access modifiers changed from: private */
            public void enableBTCoex() {
                if (this.mIsBTCoexDisabled) {
                    this.mWifiInjector.getWifiNative().setBluetoothCoexistenceMode(this.mInterfaceName, 2);
                    this.mIsBTCoexDisabled = false;
                }
            }

            private String getSsidPostFix(String deviceName) {
                String ssidPostFix = deviceName;
                if (ssidPostFix == null) {
                    return ssidPostFix;
                }
                byte[] ssidPostFixBytes = ssidPostFix.getBytes();
                while (ssidPostFixBytes.length > 22) {
                    ssidPostFix = ssidPostFix.substring(0, ssidPostFix.length() - 1);
                    ssidPostFixBytes = ssidPostFix.getBytes();
                }
                if (ssidPostFixBytes.length != 14) {
                    return ssidPostFix;
                }
                return ssidPostFix + " ";
            }

            /* access modifiers changed from: private */
            public WifiP2pDeviceList getPeers(Bundle pkg, int uid) {
                String pkgName = pkg.getString("android.net.wifi.p2p.CALLING_PACKAGE");
                if (this.mWifiInjector == null) {
                    this.mWifiInjector = WifiInjector.getInstance();
                }
                try {
                    this.mWifiInjector.getWifiPermissionsUtil().enforceCanAccessScanResults(pkgName, uid);
                    return new WifiP2pDeviceList(this.mPeers);
                } catch (SecurityException e) {
                    Log.v(WifiP2pServiceImpl.TAG, "Security Exception, cannot access peer list");
                    return new WifiP2pDeviceList();
                }
            }
        }

        public enum P2pStatus {
            SUCCESS,
            INFORMATION_IS_CURRENTLY_UNAVAILABLE,
            INCOMPATIBLE_PARAMETERS,
            LIMIT_REACHED,
            INVALID_PARAMETER,
            UNABLE_TO_ACCOMMODATE_REQUEST,
            PREVIOUS_PROTOCOL_ERROR,
            NO_COMMON_CHANNEL,
            UNKNOWN_P2P_GROUP,
            BOTH_GO_INTENT_15,
            INCOMPATIBLE_PROVISIONING_METHOD,
            REJECTED_BY_USER,
            UNKNOWN;

            public static P2pStatus valueOf(int error) {
                switch (error) {
                    case 0:
                        return SUCCESS;
                    case 1:
                        return INFORMATION_IS_CURRENTLY_UNAVAILABLE;
                    case 2:
                        return INCOMPATIBLE_PARAMETERS;
                    case 3:
                        return LIMIT_REACHED;
                    case 4:
                        return INVALID_PARAMETER;
                    case 5:
                        return UNABLE_TO_ACCOMMODATE_REQUEST;
                    case 6:
                        return PREVIOUS_PROTOCOL_ERROR;
                    case 7:
                        return NO_COMMON_CHANNEL;
                    case 8:
                        return UNKNOWN_P2P_GROUP;
                    case 9:
                        return BOTH_GO_INTENT_15;
                    case 10:
                        return INCOMPATIBLE_PROVISIONING_METHOD;
                    case 11:
                        return REJECTED_BY_USER;
                    default:
                        return UNKNOWN;
                }
            }
        }

        static /* synthetic */ int access$1804() {
            int i = sDisableP2pTimeoutIndex + 1;
            sDisableP2pTimeoutIndex = i;
            return i;
        }

        static /* synthetic */ int access$5404() {
            int i = sGroupCreatingTimeoutIndex + 1;
            sGroupCreatingTimeoutIndex = i;
            return i;
        }

        static /* synthetic */ byte access$8704(WifiP2pServiceImpl x0) {
            byte b = (byte) (x0.mServiceTransactionId + 1);
            x0.mServiceTransactionId = b;
            return b;
        }

        static {
            boolean z = true;
            if (!"07".equals(SystemProperties.get("ro.config.hw_opta", "0")) || !"840".equals(SystemProperties.get("ro.config.hw_optb", "0"))) {
                z = false;
            }
            IS_ATT = z;
        }

        public WifiP2pServiceImpl(Context context) {
            this.mContext = context;
            this.mNetworkInfo = new NetworkInfo(13, 0, NETWORKTYPE, "");
            this.mHwWifiCHRService = HwWifiServiceFactory.getHwWifiCHRService();
            this.mP2pSupported = this.mContext.getPackageManager().hasSystemFeature("android.hardware.wifi.direct");
            this.mThisDevice.primaryDeviceType = this.mContext.getResources().getString(17039849);
            if (WifiP2pServiceHisiExt.hisiWifiEnabled()) {
                this.mWifiP2pServiceHisiExt = new WifiP2pServiceHisiExt(this.mContext, this.mThisDevice, this.mWifiChannel, this.mNetworkInfo);
            }
            HandlerThread wifiP2pThread = new HandlerThread(TAG);
            wifiP2pThread.start();
            this.mClientHandler = new ClientHandler(TAG, wifiP2pThread.getLooper());
            this.mP2pStateMachine = (P2pStateMachine) getHwP2pStateMachine(TAG, wifiP2pThread.getLooper(), this.mP2pSupported);
            if (this.mP2pStateMachine == null) {
                Slog.d(TAG, "use android origin P2pStateMachine");
                this.mP2pStateMachine = new P2pStateMachine(TAG, wifiP2pThread.getLooper(), this.mP2pSupported);
            }
            if (WifiP2pServiceHisiExt.hisiWifiEnabled()) {
                this.mWifiP2pServiceHisiExt.mP2pStateMachine = this.mP2pStateMachine;
            }
            this.mP2pStateMachine.start();
            if (SystemProperties.getBoolean("ro.config.hw_wifibridge", false)) {
                initWifiRepeaterConfig();
            }
        }

        public boolean setWifiP2pEnabled(int p2pFlag) {
            if (WifiP2pServiceHisiExt.hisiWifiEnabled()) {
                return this.mWifiP2pServiceHisiExt.setWifiP2pEnabled(p2pFlag);
            }
            return false;
        }

        public boolean isWifiP2pEnabled() {
            if (WifiP2pServiceHisiExt.hisiWifiEnabled()) {
                return this.mWifiP2pServiceHisiExt.isWifiP2pEnabled();
            }
            return false;
        }

        public void setRecoveryWifiFlag(boolean flag) {
            enforceChangePermission();
            if (WifiP2pServiceHisiExt.hisiWifiEnabled()) {
                this.mWifiP2pServiceHisiExt.setRecoveryWifiFlag(flag);
            }
        }

        public void connectivityServiceReady() {
            this.mNwService = INetworkManagementService.Stub.asInterface(ServiceManager.getService("network_management"));
        }

        private void enforceAccessPermission() {
            this.mContext.enforceCallingOrSelfPermission("android.permission.ACCESS_WIFI_STATE", TAG);
        }

        private void enforceChangePermission() {
            this.mContext.enforceCallingOrSelfPermission("android.permission.CHANGE_WIFI_STATE", TAG);
        }

        private void enforceConnectivityInternalPermission() {
            this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        }

        private int checkConnectivityInternalPermission() {
            return this.mContext.checkCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL");
        }

        private int checkLocationHardwarePermission() {
            return this.mContext.checkCallingOrSelfPermission("android.permission.LOCATION_HARDWARE");
        }

        private void enforceConnectivityInternalOrLocationHardwarePermission() {
            if (checkConnectivityInternalPermission() != 0 && checkLocationHardwarePermission() != 0) {
                enforceConnectivityInternalPermission();
            }
        }

        /* access modifiers changed from: private */
        public void stopIpClient() {
            if (this.mIpClient != null) {
                this.mIpClient.shutdown();
                this.mIpClient = null;
            }
            this.mDhcpResults = null;
        }

        /* access modifiers changed from: private */
        public void startIpClient(String ifname) {
            stopIpClient();
            this.mIpClient = new IpClient(this.mContext, ifname, new IpClient.Callback() {
                public void onPreDhcpAction() {
                    WifiP2pServiceImpl.this.mP2pStateMachine.sendMessage(WifiP2pServiceImpl.IPC_PRE_DHCP_ACTION);
                }

                public void onPostDhcpAction() {
                    WifiP2pServiceImpl.this.mP2pStateMachine.sendMessage(WifiP2pServiceImpl.IPC_POST_DHCP_ACTION);
                }

                public void onNewDhcpResults(DhcpResults dhcpResults) {
                    WifiP2pServiceImpl.this.mP2pStateMachine.sendMessage(WifiP2pServiceImpl.IPC_DHCP_RESULTS, dhcpResults);
                }

                public void onProvisioningSuccess(LinkProperties newLp) {
                    WifiP2pServiceImpl.this.mP2pStateMachine.sendMessage(WifiP2pServiceImpl.IPC_PROVISIONING_SUCCESS);
                }

                public void onProvisioningFailure(LinkProperties newLp) {
                    WifiP2pServiceImpl.this.mP2pStateMachine.sendMessage(WifiP2pServiceImpl.IPC_PROVISIONING_FAILURE);
                }
            }, this.mNwService);
            IpClient ipClient = this.mIpClient;
            this.mIpClient.startProvisioning(IpClient.buildProvisioningConfiguration().withoutIPv6().withoutIpReachabilityMonitor().withPreDhcpAction(WifiStateMachine.LAST_SELECTED_NETWORK_EXPIRATION_AGE_MILLIS).withProvisioningTimeoutMs(36000).build());
        }

        public Messenger getMessenger(IBinder binder) {
            Messenger messenger;
            enforceAccessPermission();
            enforceChangePermission();
            synchronized (this.mLock) {
                messenger = new Messenger(this.mClientHandler);
                Log.d(TAG, "getMessenger: uid=" + getCallingUid() + ", binder=" + binder + ", messenger=" + messenger);
                IBinder.DeathRecipient dr = new IBinder.DeathRecipient(binder) {
                    private final /* synthetic */ IBinder f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void binderDied() {
                        WifiP2pServiceImpl.lambda$getMessenger$0(WifiP2pServiceImpl.this, this.f$1);
                    }
                };
                try {
                    binder.linkToDeath(dr, 0);
                    this.mDeathDataByBinder.put(binder, new DeathHandlerData(dr, messenger));
                } catch (RemoteException e) {
                    Log.e(TAG, "Error on linkToDeath: e=" + e);
                }
                this.mWifiP2pDevCreateRetry = 0;
                this.mP2pStateMachine.sendMessage(ENABLE_P2P);
            }
            return messenger;
        }

        public static /* synthetic */ void lambda$getMessenger$0(WifiP2pServiceImpl wifiP2pServiceImpl, IBinder binder) {
            Log.d(TAG, "binderDied: binder=" + binder);
            wifiP2pServiceImpl.close(binder);
        }

        public Messenger getP2pStateMachineMessenger() {
            enforceConnectivityInternalOrLocationHardwarePermission();
            enforceAccessPermission();
            enforceChangePermission();
            return new Messenger(this.mP2pStateMachine.getHandler());
        }

        /* JADX WARNING: Code restructure failed: missing block: B:20:0x0070, code lost:
            r0 = r1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:21:0x0071, code lost:
            return;
         */
        public void close(IBinder binder) {
            enforceAccessPermission();
            enforceChangePermission();
            synchronized (this.mLock) {
                DeathHandlerData dhd = this.mDeathDataByBinder.get(binder);
                if (dhd == null) {
                    Log.w(TAG, "close(): no death recipient for binder");
                    return;
                }
                binder.unlinkToDeath(dhd.mDeathRecipient, 0);
                this.mDeathDataByBinder.remove(binder);
                if (dhd.mMessenger != null && this.mDeathDataByBinder.isEmpty()) {
                    try {
                        dhd.mMessenger.send(this.mClientHandler.obtainMessage(139268));
                        dhd.mMessenger.send(this.mClientHandler.obtainMessage(139280));
                    } catch (RemoteException e) {
                        Log.e(TAG, "close: Failed sending clean-up commands: e=" + e);
                    }
                    this.mP2pStateMachine.sendMessage(DISABLE_P2P);
                }
            }
        }

        public void setMiracastMode(int mode) {
            enforceConnectivityInternalPermission();
            checkConfigureWifiDisplayPermission();
            this.mP2pStateMachine.sendMessage(SET_MIRACAST_MODE, mode);
        }

        public void checkConfigureWifiDisplayPermission() {
            if (!getWfdPermission(Binder.getCallingUid())) {
                throw new SecurityException("Wifi Display Permission denied for uid = " + Binder.getCallingUid());
            }
        }

        /* access modifiers changed from: private */
        public boolean getWfdPermission(int uid) {
            if (this.mWifiInjector == null) {
                this.mWifiInjector = WifiInjector.getInstance();
            }
            return this.mWifiInjector.getWifiPermissionsWrapper().getUidPermission("android.permission.CONFIGURE_WIFI_DISPLAY", uid) != -1;
        }

        /* access modifiers changed from: protected */
        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
                pw.println("Permission Denial: can't dump WifiP2pService from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
                return;
            }
            this.mP2pStateMachine.dump(fd, pw, args);
            pw.println("mAutonomousGroup " + this.mAutonomousGroup);
            pw.println("mJoinExistingGroup " + this.mJoinExistingGroup);
            pw.println("mDiscoveryStarted " + this.mDiscoveryStarted);
            pw.println("mNetworkInfo " + this.mNetworkInfo);
            pw.println("mTemporarilyDisconnectedWifi " + this.mTemporarilyDisconnectedWifi);
            pw.println("mServiceDiscReqId " + this.mServiceDiscReqId);
            pw.println("mDeathDataByBinder " + this.mDeathDataByBinder);
            pw.println();
            IpClient ipClient = this.mIpClient;
            if (ipClient != null) {
                pw.println("mIpClient:");
                ipClient.dump(fd, pw, args);
            }
        }

        public void handleClientConnect(WifiP2pGroup group) {
        }

        public void handleClientDisconnect(WifiP2pGroup group) {
        }

        public void notifyRptGroupRemoved() {
        }

        public void setWifiRepeaterState(int state) {
        }

        /* access modifiers changed from: protected */
        public boolean allowP2pFind(int uid) {
            return true;
        }

        /* access modifiers changed from: protected */
        public void handleP2pStopFind(int uid) {
        }
    }
