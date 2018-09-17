package com.android.server.wifi.p2p;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.res.Resources;
import android.hdm.HwDeviceManager;
import android.net.ConnectivityManager;
import android.net.DhcpResults;
import android.net.InterfaceConfiguration;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.NetworkUtils;
import android.net.ip.IpManager;
import android.net.ip.IpManager.Callback;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pGroupList;
import android.net.wifi.p2p.WifiP2pGroupList.GroupDeleteListener;
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
import android.os.INetworkManagementService;
import android.os.INetworkManagementService.Stub;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.util.Flog;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.android.internal.util.AsyncChannel;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.wifi.HwWifiBigDataConstant;
import com.android.server.wifi.HwWifiCHRStateManager;
import com.android.server.wifi.HwWifiServiceFactory;
import com.android.server.wifi.WifiInjector;
import com.android.server.wifi.WifiP2pServiceHisiExt;
import com.android.server.wifi.WifiStateMachine;
import com.android.server.wifi.util.WifiAsyncChannel;
import com.android.server.wifi.util.WifiCommonUtils;
import com.android.server.wifi.util.WifiHandler;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONException;
import org.json.JSONObject;

public class WifiP2pServiceImpl extends AbsWifiP2pService {
    private static final int BASE = 143360;
    public static final int BLOCK_DISCOVERY = 143375;
    private static final boolean DBG = true;
    public static final int DISABLED = 0;
    public static final int DISABLE_P2P_TIMED_OUT = 143366;
    private static final int DISABLE_P2P_WAIT_TIME_MS = 5000;
    public static final int DISCONNECT_WIFI_REQUEST = 143372;
    public static final int DISCONNECT_WIFI_RESPONSE = 143373;
    private static final int DISCOVER_TIMEOUT_S = 120;
    private static final int DROP_WIFI_USER_ACCEPT = 143364;
    private static final int DROP_WIFI_USER_REJECT = 143365;
    public static final int ENABLED = 1;
    private static final Boolean FORM_GROUP = Boolean.valueOf(false);
    public static final int GROUP_CREATING_TIMED_OUT = 143361;
    private static final int GROUP_CREATING_WAIT_TIME_MS = 120000;
    private static final int GROUP_IDLE_TIME_S = 10;
    private static final boolean HWDBG;
    private static final int IPM_DHCP_RESULTS = 143392;
    private static final int IPM_POST_DHCP_ACTION = 143391;
    private static final int IPM_PRE_DHCP_ACTION = 143390;
    private static final int IPM_PROVISIONING_FAILURE = 143394;
    private static final int IPM_PROVISIONING_SUCCESS = 143393;
    private static final boolean IS_ATT;
    private static final Boolean JOIN_GROUP = Boolean.valueOf(true);
    private static final String NETWORKTYPE = "WIFI_P2P";
    private static final Boolean NO_RELOAD = Boolean.valueOf(false);
    static final int P2P_BLUETOOTH_COEXISTENCE_MODE_DISABLED = 1;
    static final int P2P_BLUETOOTH_COEXISTENCE_MODE_SENSE = 2;
    public static final int P2P_CONNECTION_CHANGED = 143371;
    private static final int P2P_DEVICE_OF_MIRACAST = 7;
    private static final int PEER_CONNECTION_USER_ACCEPT = 143362;
    private static final int PEER_CONNECTION_USER_REJECT = 143363;
    private static final Boolean RELOAD = Boolean.valueOf(true);
    private static final String SERVER_ADDRESS = "192.168.49.1";
    private static final String SERVER_ADDRESS_WIFI_BRIDGE = "192.168.43.1";
    public static final int SET_MIRACAST_MODE = 143374;
    public static final int SHOW_USER_CONFIRM_DIALOG = 143410;
    private static final String TAG = "WifiP2pService";
    private static int sDisableP2pTimeoutIndex = 0;
    private static int sGroupCreatingTimeoutIndex = 0;
    private boolean mAutonomousGroup;
    private ClientHandler mClientHandler;
    private HashMap<Messenger, ClientInfo> mClientInfoList = new HashMap();
    private Context mContext;
    private boolean mCreateWifiBridge = false;
    private DhcpResults mDhcpResults;
    private boolean mDiscoveryBlocked;
    private boolean mDiscoveryPostponed = false;
    private boolean mDiscoveryStarted;
    private IpManager mIpManager;
    private boolean mIsInvite = false;
    private boolean mJoinExistingGroup;
    private NetworkInfo mNetworkInfo;
    INetworkManagementService mNwService;
    private String mP2pServerAddress;
    protected P2pStateMachine mP2pStateMachine;
    private final boolean mP2pSupported;
    private AsyncChannel mReplyChannel = new WifiAsyncChannel(TAG);
    private String mServiceDiscReqId;
    private byte mServiceTransactionId = (byte) 0;
    private boolean mTemporarilyDisconnectedWifi = false;
    private WifiP2pDevice mThisDevice = new WifiP2pDevice();
    private HwWifiCHRStateManager mWiFiCHRManager;
    private AsyncChannel mWifiChannel;
    private WifiInjector mWifiInjector;
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
                    boolean isConnect = msg.what == 139271;
                    boolean isDiscoverPeers = msg.what == 139265;
                    boolean isRequestPeers = msg.what == 139283;
                    if (HwDeviceManager.disallowOp(45) && (isConnect || isDiscoverPeers || isRequestPeers)) {
                        Slog.d(WifiP2pServiceImpl.TAG, "wifiP2P function is forbidden,msg.what = " + msg.what);
                        Toast.makeText(WifiP2pServiceImpl.this.mContext, WifiP2pServiceImpl.this.mContext.getResources().getString(33686008), 0).show();
                        return;
                    }
                    WifiP2pServiceImpl.this.mP2pStateMachine.sendMessage(Message.obtain(msg));
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
        private Messenger mMessenger;
        private SparseArray<WifiP2pServiceRequest> mReqList;
        private List<WifiP2pServiceInfo> mServList;

        /* synthetic */ ClientInfo(WifiP2pServiceImpl this$0, Messenger m, ClientInfo -this2) {
            this(m);
        }

        private ClientInfo(Messenger m) {
            this.mMessenger = m;
            this.mReqList = new SparseArray();
            this.mServList = new ArrayList();
        }
    }

    protected class P2pStateMachine extends StateMachine {
        private AfterUserAuthorizingJoinState mAfterUserAuthorizingJoinState = new AfterUserAuthorizingJoinState();
        private DefaultState mDefaultState = new DefaultState();
        private FrequencyConflictState mFrequencyConflictState = new FrequencyConflictState();
        private WifiP2pGroup mGroup = null;
        private GroupCreatedState mGroupCreatedState = new GroupCreatedState();
        private GroupCreatingState mGroupCreatingState = new GroupCreatingState();
        protected GroupNegotiationState mGroupNegotiationState = new GroupNegotiationState();
        protected final WifiP2pGroupList mGroups = new WifiP2pGroupList(null, new GroupDeleteListener() {
            public void onDeleteGroup(int netId) {
                P2pStateMachine.this.logd("called onDeleteGroup() netId=" + netId);
                P2pStateMachine.this.mWifiNative.removeP2pNetwork(netId);
                P2pStateMachine.this.mWifiNative.saveConfig();
                P2pStateMachine.this.updatePersistentNetworks(WifiP2pServiceImpl.RELOAD.booleanValue());
                P2pStateMachine.this.sendP2pPersistentGroupsChangedBroadcast();
            }
        });
        private InactiveState mInactiveState = new InactiveState();
        private boolean mIsBTCoexDisabled = false;
        private OngoingGroupRemovalState mOngoingGroupRemovalState = new OngoingGroupRemovalState();
        private P2pDisabledState mP2pDisabledState = new P2pDisabledState();
        private P2pDisablingState mP2pDisablingState = new P2pDisablingState();
        private P2pEnabledState mP2pEnabledState = new P2pEnabledState();
        private P2pEnablingState mP2pEnablingState = new P2pEnablingState();
        private P2pNotSupportedState mP2pNotSupportedState = new P2pNotSupportedState();
        protected final WifiP2pDeviceList mPeers = new WifiP2pDeviceList();
        private final WifiP2pDeviceList mPeersLostDuringConnection = new WifiP2pDeviceList();
        private boolean mPendingReformGroupIndication = false;
        private ProvisionDiscoveryState mProvisionDiscoveryState = new ProvisionDiscoveryState();
        protected WifiP2pConfig mSavedPeerConfig = new WifiP2pConfig();
        private UserAuthorizingInviteRequestState mUserAuthorizingInviteRequestState = new UserAuthorizingInviteRequestState();
        private UserAuthorizingJoinState mUserAuthorizingJoinState = new UserAuthorizingJoinState();
        private UserAuthorizingNegotiationRequestState mUserAuthorizingNegotiationRequestState = new UserAuthorizingNegotiationRequestState();
        private WifiInjector mWifiInjector;
        private WifiP2pMonitor mWifiMonitor = WifiInjector.getInstance().getWifiP2pMonitor();
        protected WifiP2pNative mWifiNative = WifiInjector.getInstance().getWifiP2pNative();
        private final WifiP2pInfo mWifiP2pInfo = new WifiP2pInfo();

        class AfterUserAuthorizingJoinState extends State {
            AfterUserAuthorizingJoinState() {
            }

            public void enter() {
                P2pStateMachine.this.logd(getName());
            }

            public boolean processMessage(Message message) {
                P2pStateMachine.this.logd(getName() + message.toString());
                return false;
            }

            public void exit() {
            }
        }

        class DefaultState extends State {
            DefaultState() {
            }

            public boolean processMessage(Message message) {
                Object obj = null;
                P2pStateMachine.this.logd(getName() + message.toString());
                if (WifiP2pServiceImpl.this.processMessageForP2pCollision(message, this)) {
                    return true;
                }
                switch (message.what) {
                    case 69632:
                        if (message.arg1 != 0) {
                            P2pStateMachine.this.loge("Full connection failure, error = " + message.arg1);
                            WifiP2pServiceImpl.this.mWifiChannel = null;
                            P2pStateMachine.this.transitionTo(P2pStateMachine.this.mP2pDisabledState);
                            break;
                        }
                        P2pStateMachine.this.logd("Full connection with WifiStateMachine established");
                        WifiP2pServiceImpl.this.mWifiChannel = (AsyncChannel) message.obj;
                        break;
                    case 69633:
                        new WifiAsyncChannel(WifiP2pServiceImpl.TAG).connect(WifiP2pServiceImpl.this.mContext, P2pStateMachine.this.getHandler(), message.replyTo);
                        break;
                    case 69636:
                        if (message.arg1 == 2) {
                            P2pStateMachine.this.loge("Send failed, client connection lost");
                        } else {
                            P2pStateMachine.this.loge("Client connection lost with reason: " + message.arg1);
                        }
                        WifiP2pServiceImpl.this.mWifiChannel = null;
                        P2pStateMachine.this.transitionTo(P2pStateMachine.this.mP2pDisabledState);
                        break;
                    case WifiStateMachine.CMD_ENABLE_P2P /*131203*/:
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
                    case WifiP2pServiceImpl.IPM_PRE_DHCP_ACTION /*143390*/:
                    case WifiP2pServiceImpl.IPM_POST_DHCP_ACTION /*143391*/:
                    case WifiP2pServiceImpl.IPM_DHCP_RESULTS /*143392*/:
                    case WifiP2pServiceImpl.IPM_PROVISIONING_SUCCESS /*143393*/:
                    case WifiP2pServiceImpl.IPM_PROVISIONING_FAILURE /*143394*/:
                    case 147457:
                    case 147458:
                    case WifiP2pMonitor.P2P_DEVICE_FOUND_EVENT /*147477*/:
                    case WifiP2pMonitor.P2P_DEVICE_LOST_EVENT /*147478*/:
                    case WifiP2pMonitor.P2P_GROUP_FORMATION_FAILURE_EVENT /*147484*/:
                    case WifiP2pMonitor.P2P_GROUP_REMOVED_EVENT /*147486*/:
                    case WifiP2pMonitor.P2P_INVITATION_RESULT_EVENT /*147488*/:
                    case WifiP2pMonitor.P2P_FIND_STOPPED_EVENT /*147493*/:
                    case WifiP2pMonitor.P2P_SERV_DISC_RESP_EVENT /*147494*/:
                    case WifiP2pMonitor.P2P_PROV_DISC_FAILURE_EVENT /*147495*/:
                        break;
                    case WifiStateMachine.CMD_DISABLE_P2P_REQ /*131204*/:
                        if (WifiP2pServiceImpl.this.mWifiChannel == null) {
                            P2pStateMachine.this.loge("Unexpected disable request when WifiChannel is null");
                            break;
                        }
                        WifiP2pServiceImpl.this.mWifiChannel.sendMessage(WifiStateMachine.CMD_DISABLE_P2P_RSP);
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
                        P2pStateMachine p2pStateMachine = P2pStateMachine.this;
                        if (P2pStateMachine.this.mGroup != null) {
                            obj = new WifiP2pGroup(P2pStateMachine.this.mGroup);
                        }
                        p2pStateMachine.replyToMessage(message, 139288, obj);
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
                        if (!WifiP2pServiceImpl.this.getWfdPermission(message.sendingUid)) {
                            P2pStateMachine.this.replyToMessage(message, 139324, 0);
                            break;
                        }
                        P2pStateMachine.this.replyToMessage(message, 139324, 2);
                        break;
                    case 139326:
                        P2pStateMachine.this.replyToMessage(message, 139327, 2);
                        break;
                    case 139339:
                    case 139340:
                        P2pStateMachine.this.replyToMessage(message, 139341, null);
                        break;
                    case 139342:
                    case 139343:
                        P2pStateMachine.this.replyToMessage(message, 139345, 2);
                        break;
                    case WifiP2pServiceImpl.BLOCK_DISCOVERY /*143375*/:
                        WifiP2pServiceImpl.this.mDiscoveryBlocked = message.arg1 == 1;
                        WifiP2pServiceImpl.this.mDiscoveryPostponed = false;
                        if (WifiP2pServiceImpl.this.mDiscoveryBlocked) {
                            if (message.obj != null) {
                                try {
                                    message.obj.sendMessage(message.arg2);
                                    break;
                                } catch (Exception e) {
                                    P2pStateMachine.this.loge("unable to send BLOCK_DISCOVERY response: " + e);
                                    break;
                                }
                            }
                            Log.e(WifiP2pServiceImpl.TAG, "Illegal argument(s)");
                            break;
                        }
                        break;
                    case WifiP2pServiceImpl.SHOW_USER_CONFIRM_DIALOG /*143410*/:
                        WifiP2pServiceImpl.this.mWifiP2pServiceHisiExt.showP2pEanbleDialog();
                        break;
                    case WifiP2pMonitor.P2P_GROUP_STARTED_EVENT /*147485*/:
                        if (message.obj != null) {
                            P2pStateMachine.this.mGroup = (WifiP2pGroup) message.obj;
                            P2pStateMachine.this.loge("Unexpected group creation, remove " + P2pStateMachine.this.mGroup);
                            P2pStateMachine.this.mWifiNative.p2pGroupRemove(P2pStateMachine.this.mGroup.getInterface());
                            break;
                        }
                        Log.e(WifiP2pServiceImpl.TAG, "Illegal arguments");
                        break;
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
                AlertDialog dialog = new Builder(WifiP2pServiceImpl.this.mContext).setMessage(r.getString(17041255, new Object[]{P2pStateMachine.this.getDeviceName(P2pStateMachine.this.mSavedPeerConfig.deviceAddress)})).setPositiveButton(r.getString(17039905), new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        P2pStateMachine.this.sendMessage(WifiP2pServiceImpl.DROP_WIFI_USER_ACCEPT);
                    }
                }).setNegativeButton(r.getString(17039871), new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        P2pStateMachine.this.sendMessage(WifiP2pServiceImpl.DROP_WIFI_USER_REJECT);
                    }
                }).setOnCancelListener(new OnCancelListener() {
                    public void onCancel(DialogInterface arg0) {
                        P2pStateMachine.this.sendMessage(WifiP2pServiceImpl.DROP_WIFI_USER_REJECT);
                    }
                }).create();
                dialog.setCanceledOnTouchOutside(false);
                dialog.getWindow().setType(2003);
                LayoutParams attrs = dialog.getWindow().getAttributes();
                attrs.privateFlags = 16;
                dialog.getWindow().setAttributes(attrs);
                dialog.show();
                this.mFrequencyConflictDialog = dialog;
            }

            public boolean processMessage(Message message) {
                P2pStateMachine.this.logd(getName() + message.toString());
                switch (message.what) {
                    case WifiP2pServiceImpl.DROP_WIFI_USER_ACCEPT /*143364*/:
                        if (WifiP2pServiceImpl.this.mWifiChannel != null) {
                            WifiP2pServiceImpl.this.mWifiChannel.sendMessage(WifiP2pServiceImpl.DISCONNECT_WIFI_REQUEST, 1);
                        } else {
                            P2pStateMachine.this.loge("DROP_WIFI_USER_ACCEPT message received when WifiChannel is null");
                        }
                        WifiP2pServiceImpl.this.mTemporarilyDisconnectedWifi = true;
                        break;
                    case WifiP2pServiceImpl.DROP_WIFI_USER_REJECT /*143365*/:
                        P2pStateMachine.this.handleGroupCreationFailure();
                        P2pStateMachine.this.transitionTo(P2pStateMachine.this.mInactiveState);
                        break;
                    case WifiP2pServiceImpl.DISCONNECT_WIFI_RESPONSE /*143373*/:
                        P2pStateMachine.this.logd(getName() + "Wifi disconnected, retry p2p");
                        P2pStateMachine.this.transitionTo(P2pStateMachine.this.mInactiveState);
                        P2pStateMachine.this.sendMessage(139271, P2pStateMachine.this.mSavedPeerConfig);
                        break;
                    case WifiP2pMonitor.P2P_GO_NEGOTIATION_SUCCESS_EVENT /*147481*/:
                    case WifiP2pMonitor.P2P_GROUP_FORMATION_SUCCESS_EVENT /*147483*/:
                        P2pStateMachine.this.loge(getName() + "group sucess during freq conflict!");
                        break;
                    case WifiP2pMonitor.P2P_GO_NEGOTIATION_FAILURE_EVENT /*147482*/:
                    case WifiP2pMonitor.P2P_GROUP_FORMATION_FAILURE_EVENT /*147484*/:
                    case WifiP2pMonitor.P2P_GROUP_REMOVED_EVENT /*147486*/:
                        break;
                    case WifiP2pMonitor.P2P_GROUP_STARTED_EVENT /*147485*/:
                        P2pStateMachine.this.loge(getName() + "group started after freq conflict, handle anyway");
                        P2pStateMachine.this.deferMessage(message);
                        P2pStateMachine.this.transitionTo(P2pStateMachine.this.mGroupNegotiationState);
                        break;
                    default:
                        return false;
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
                P2pStateMachine.this.logd(getName() + "mPendingReformGroupIndication=" + P2pStateMachine.this.mPendingReformGroupIndication);
                if (P2pStateMachine.this.mPendingReformGroupIndication) {
                    P2pStateMachine.this.mPendingReformGroupIndication = false;
                    handlP2pGroupRestart();
                } else {
                    P2pStateMachine.this.mSavedPeerConfig.invalidate();
                    WifiP2pServiceImpl.this.mNetworkInfo.setDetailedState(DetailedState.CONNECTED, null, null);
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
                if (WifiP2pServiceImpl.this.getWifiRepeaterEnabled() && (WifiP2pServiceImpl.this.startWifiRepeater(P2pStateMachine.this.mGroup) ^ 1) != 0) {
                    P2pStateMachine.this.sendMessage(139280);
                }
                WifiP2pServiceImpl.this.notifyP2pChannelNumber(WifiCommonUtils.convertFrequencyToChannelNumber(P2pStateMachine.this.mGroup.getFrequence()));
            }

            public boolean processMessage(Message message) {
                P2pStateMachine.this.logd(getName() + "when=" + message.getWhen() + " what=" + message.what + " arg1=" + message.arg1 + " arg2=" + message.arg2);
                if (WifiP2pServiceImpl.this.processMessageForP2pCollision(message, this)) {
                    return true;
                }
                WifiP2pDevice device;
                String deviceAddress;
                switch (message.what) {
                    case WifiStateMachine.CMD_DISABLE_P2P_REQ /*131204*/:
                        P2pStateMachine.this.sendMessage(139280);
                        P2pStateMachine.this.deferMessage(message);
                        break;
                    case 139271:
                        WifiP2pConfig config = message.obj;
                        if (!P2pStateMachine.this.isConfigInvalid(config)) {
                            P2pStateMachine.this.logd("Inviting device : " + config.deviceAddress);
                            P2pStateMachine.this.mSavedPeerConfig = config;
                            if (!P2pStateMachine.this.mWifiNative.p2pInvite(P2pStateMachine.this.mGroup, config.deviceAddress)) {
                                P2pStateMachine.this.replyToMessage(message, 139272, 0);
                                break;
                            }
                            P2pStateMachine.this.mPeers.updateStatus(config.deviceAddress, 1);
                            P2pStateMachine.this.sendPeersChangedBroadcast();
                            P2pStateMachine.this.replyToMessage(message, 139273);
                            break;
                        }
                        P2pStateMachine.this.loge("Dropping connect request " + config);
                        P2pStateMachine.this.replyToMessage(message, 139272);
                        break;
                    case 139280:
                        P2pStateMachine.this.logd(getName() + " remove group");
                        if (WifiP2pServiceImpl.this.getWifiRepeaterEnabled()) {
                            WifiP2pServiceImpl.this.stopWifiRepeater(P2pStateMachine.this.mGroup);
                        }
                        P2pStateMachine.this.enableBTCoex();
                        if (!P2pStateMachine.this.mWifiNative.p2pGroupRemove(P2pStateMachine.this.mGroup.getInterface())) {
                            P2pStateMachine.this.handleGroupRemoved();
                            P2pStateMachine.this.transitionTo(P2pStateMachine.this.mInactiveState);
                            P2pStateMachine.this.replyToMessage(message, 139281, 0);
                            break;
                        }
                        P2pStateMachine.this.transitionTo(P2pStateMachine.this.mOngoingGroupRemovalState);
                        P2pStateMachine.this.replyToMessage(message, 139282);
                        break;
                    case 139326:
                        WpsInfo wps = message.obj;
                        if (wps != null) {
                            int i;
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
                            P2pStateMachine p2pStateMachine = P2pStateMachine.this;
                            if (ret) {
                                i = 139328;
                            } else {
                                i = 139327;
                            }
                            p2pStateMachine.replyToMessage(message, i);
                            break;
                        }
                        P2pStateMachine.this.replyToMessage(message, 139327);
                        break;
                    case WifiP2pServiceImpl.IPM_PRE_DHCP_ACTION /*143390*/:
                        P2pStateMachine.this.mWifiNative.setP2pPowerSave(P2pStateMachine.this.mGroup.getInterface(), false);
                        WifiP2pServiceImpl.this.mIpManager.completedPreDhcpAction();
                        break;
                    case WifiP2pServiceImpl.IPM_POST_DHCP_ACTION /*143391*/:
                        P2pStateMachine.this.mWifiNative.setP2pPowerSave(P2pStateMachine.this.mGroup.getInterface(), true);
                        break;
                    case WifiP2pServiceImpl.IPM_DHCP_RESULTS /*143392*/:
                        WifiP2pServiceImpl.this.mDhcpResults = (DhcpResults) message.obj;
                        break;
                    case WifiP2pServiceImpl.IPM_PROVISIONING_SUCCESS /*143393*/:
                        P2pStateMachine.this.logd("mDhcpResults: " + WifiP2pServiceImpl.this.mDhcpResults);
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
                            P2pStateMachine.this.loge("Failed to add iface to local network " + e2);
                            break;
                        }
                        break;
                    case WifiP2pServiceImpl.IPM_PROVISIONING_FAILURE /*143394*/:
                        P2pStateMachine.this.loge("IP provisioning failed");
                        P2pStateMachine.this.mWifiNative.p2pGroupRemove(P2pStateMachine.this.mGroup.getInterface());
                        break;
                    case WifiP2pMonitor.P2P_DEVICE_LOST_EVENT /*147478*/:
                        if (message.obj == null) {
                            Log.e(WifiP2pServiceImpl.TAG, "Illegal argument(s)");
                            return false;
                        }
                        device = message.obj;
                        if (!P2pStateMachine.this.mGroup.contains(device)) {
                            return false;
                        }
                        P2pStateMachine.this.logd("Add device to lost list " + device);
                        P2pStateMachine.this.mPeersLostDuringConnection.updateSupplicantDetails(device);
                        return true;
                    case WifiP2pMonitor.P2P_GROUP_STARTED_EVENT /*147485*/:
                        P2pStateMachine.this.loge("Duplicate group creation event notice, ignore");
                        break;
                    case WifiP2pMonitor.P2P_GROUP_REMOVED_EVENT /*147486*/:
                        P2pStateMachine.this.logd(getName() + " group removed");
                        P2pStateMachine.this.enableBTCoex();
                        P2pStateMachine.this.handleGroupRemoved();
                        P2pStateMachine.this.transitionTo(P2pStateMachine.this.mInactiveState);
                        P2pStateMachine.this.mWifiNative.p2pFlush();
                        break;
                    case WifiP2pMonitor.P2P_INVITATION_RESULT_EVENT /*147488*/:
                        P2pStatus status = message.obj;
                        if (status != P2pStatus.SUCCESS) {
                            P2pStateMachine.this.loge("Invitation result " + status);
                            if (status == P2pStatus.UNKNOWN_P2P_GROUP) {
                                int netId = P2pStateMachine.this.mGroup.getNetworkId();
                                if (netId >= 0) {
                                    P2pStateMachine.this.logd("Remove unknown client from the list");
                                    if (!P2pStateMachine.this.removeClientFromList(netId, P2pStateMachine.this.mSavedPeerConfig.deviceAddress, false)) {
                                        P2pStateMachine.this.loge("Already removed the client, ignore");
                                    }
                                    P2pStateMachine.this.sendMessage(139271, P2pStateMachine.this.mSavedPeerConfig);
                                    break;
                                }
                            }
                        }
                        break;
                    case WifiP2pMonitor.P2P_PROV_DISC_PBC_REQ_EVENT /*147489*/:
                    case WifiP2pMonitor.P2P_PROV_DISC_ENTER_PIN_EVENT /*147491*/:
                    case WifiP2pMonitor.P2P_PROV_DISC_SHOW_PIN_EVENT /*147492*/:
                        WifiP2pProvDiscEvent provDisc = message.obj;
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
                        P2pStateMachine.this.logd("mGroup.isGroupOwner()" + P2pStateMachine.this.mGroup.isGroupOwner());
                        if (P2pStateMachine.this.mGroup.isGroupOwner()) {
                            P2pStateMachine.this.logd("Device is GO, going to mUserAuthorizingJoinState");
                            P2pStateMachine.this.transitionTo(P2pStateMachine.this.mUserAuthorizingJoinState);
                            break;
                        }
                        break;
                    case WifiP2pMonitor.P2P_REMOVE_AND_REFORM_GROUP_EVENT /*147496*/:
                        Slog.d(WifiP2pServiceImpl.TAG, "Received event P2P_REMOVE_AND_REFORM_GROUP, remove P2P group");
                        handlP2pGroupRestart();
                        break;
                    case WifiP2pMonitor.AP_STA_DISCONNECTED_EVENT /*147497*/:
                        if (message.obj != null) {
                            device = message.obj;
                            deviceAddress = device.deviceAddress;
                            if (deviceAddress != null) {
                                P2pStateMachine.this.mPeers.updateStatus(deviceAddress, 3);
                                if (P2pStateMachine.this.mGroup.removeClient(deviceAddress)) {
                                    P2pStateMachine.this.logd("Removed client " + deviceAddress);
                                    if (WifiP2pServiceImpl.this.mAutonomousGroup || !P2pStateMachine.this.mGroup.isClientListEmpty()) {
                                        P2pStateMachine.this.sendP2pConnectionChangedBroadcast();
                                    } else {
                                        P2pStateMachine.this.logd("Client list empty, remove non-persistent p2p group");
                                        P2pStateMachine.this.mWifiNative.p2pGroupRemove(P2pStateMachine.this.mGroup.getInterface());
                                    }
                                } else {
                                    P2pStateMachine.this.logd("Failed to remove client " + deviceAddress);
                                    for (WifiP2pDevice c : P2pStateMachine.this.mGroup.getClientList()) {
                                        P2pStateMachine.this.logd("client " + c.deviceAddress);
                                    }
                                }
                                P2pStateMachine.this.sendPeersChangedBroadcast();
                                P2pStateMachine.this.logd(getName() + " ap sta disconnected");
                            } else {
                                P2pStateMachine.this.loge("Disconnect on unknown device: " + device);
                            }
                            WifiP2pServiceImpl.this.handleClientDisconnect(P2pStateMachine.this.mGroup);
                            break;
                        }
                        Log.e(WifiP2pServiceImpl.TAG, "Illegal argument(s)");
                        break;
                        break;
                    case WifiP2pMonitor.AP_STA_CONNECTED_EVENT /*147498*/:
                        if (message.obj != null) {
                            deviceAddress = message.obj.deviceAddress;
                            P2pStateMachine.this.mWifiNative.setP2pGroupIdle(P2pStateMachine.this.mGroup.getInterface(), 0);
                            if (deviceAddress != null) {
                                if (P2pStateMachine.this.mPeers.get(deviceAddress) != null) {
                                    P2pStateMachine.this.mGroup.addClient(P2pStateMachine.this.mPeers.get(deviceAddress));
                                } else {
                                    P2pStateMachine.this.mGroup.addClient(deviceAddress);
                                }
                                P2pStateMachine.this.mPeers.updateStatus(deviceAddress, 0);
                                P2pStateMachine.this.logd(getName() + " ap sta connected");
                                P2pStateMachine.this.sendPeersChangedBroadcast();
                            } else {
                                P2pStateMachine.this.loge("Connect on null device address, ignore");
                            }
                            P2pStateMachine.this.sendP2pConnectionChangedBroadcast();
                            WifiP2pServiceImpl.this.handleClientConnect(P2pStateMachine.this.mGroup);
                            break;
                        }
                        Log.e(WifiP2pServiceImpl.TAG, "Illegal argument(s)");
                        break;
                    case 196612:
                        DhcpResults dhcpResults = message.obj;
                        P2pStateMachine.this.enableBTCoex();
                        if (message.arg1 == 1 && dhcpResults != null) {
                            P2pStateMachine.this.logd("DhcpResults: " + dhcpResults);
                            P2pStateMachine.this.setWifiP2pInfoOnGroupFormation(dhcpResults.serverAddress);
                            P2pStateMachine.this.sendP2pConnectionChangedBroadcast();
                            P2pStateMachine.this.mWifiNative.setP2pPowerSave(P2pStateMachine.this.mGroup.getInterface(), true);
                            try {
                                if (!WifiP2pServiceHisiExt.hisiWifiEnabled()) {
                                    String iface = P2pStateMachine.this.mGroup.getInterface();
                                    WifiP2pServiceImpl.this.mNwService.addInterfaceToLocalNetwork(iface, dhcpResults.getRoutes(iface));
                                    break;
                                }
                                WifiP2pServiceImpl.this.mNwService.addInterfaceToLocalNetwork(HwWifiCHRStateManager.MAIN_IFACE, dhcpResults.getRoutes(HwWifiCHRStateManager.MAIN_IFACE));
                                break;
                            } catch (RemoteException e22) {
                                P2pStateMachine.this.loge("Failed to add iface to local network " + e22);
                                break;
                            } catch (IllegalStateException e3) {
                                P2pStateMachine.this.loge("Failed to add iface to local network " + e3);
                                break;
                            } catch (IllegalArgumentException e4) {
                                P2pStateMachine.this.loge("Failed to add iface to local network: " + e4);
                                break;
                            }
                        }
                        P2pStateMachine.this.loge("DHCP failed");
                        P2pStateMachine.this.mWifiNative.p2pGroupRemove(P2pStateMachine.this.mGroup.getInterface());
                        break;
                        break;
                    default:
                        return WifiP2pServiceImpl.this.handleGroupCreatedStateExMessage(message);
                }
                return true;
            }

            public void exit() {
                P2pStateMachine.this.updateThisDevice(3);
                P2pStateMachine.this.resetWifiP2pInfo();
                WifiP2pServiceImpl.this.mNetworkInfo.setDetailedState(DetailedState.DISCONNECTED, null, null);
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
                P2pStateMachine.this.sendMessageDelayed(P2pStateMachine.this.obtainMessage(WifiP2pServiceImpl.GROUP_CREATING_TIMED_OUT, WifiP2pServiceImpl.sGroupCreatingTimeoutIndex = WifiP2pServiceImpl.sGroupCreatingTimeoutIndex + 1, 0), 120000);
            }

            public boolean processMessage(Message message) {
                P2pStateMachine.this.logd(getName() + message.toString());
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
                    case WifiP2pMonitor.P2P_DEVICE_LOST_EVENT /*147478*/:
                        if (message.obj != null) {
                            WifiP2pDevice device = message.obj;
                            if (!P2pStateMachine.this.mSavedPeerConfig.deviceAddress.equals(device.deviceAddress)) {
                                P2pStateMachine.this.logd("mSavedPeerConfig " + P2pStateMachine.this.mSavedPeerConfig.deviceAddress + "device " + device.deviceAddress);
                                ret = false;
                                break;
                            }
                            P2pStateMachine.this.logd("Add device to lost list " + device);
                            P2pStateMachine.this.mPeersLostDuringConnection.updateSupplicantDetails(device);
                            break;
                        }
                        Log.e(WifiP2pServiceImpl.TAG, "Illegal argument(s)");
                        break;
                    case WifiP2pMonitor.P2P_GO_NEGOTIATION_SUCCESS_EVENT /*147481*/:
                        WifiP2pServiceImpl.this.mAutonomousGroup = false;
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
                P2pStateMachine.this.mPendingReformGroupIndication = false;
            }

            /* Code decompiled incorrectly, please refer to instructions dump. */
            public boolean processMessage(Message message) {
                P2pStateMachine.this.logd(getName() + message.toString());
                switch (message.what) {
                    case 139280:
                        if (WifiP2pServiceImpl.this.getWifiRepeaterEnabled()) {
                            P2pStateMachine.this.deferMessage(message);
                            break;
                        }
                        return false;
                    case WifiP2pMonitor.P2P_GO_NEGOTIATION_SUCCESS_EVENT /*147481*/:
                    case WifiP2pMonitor.P2P_GROUP_FORMATION_SUCCESS_EVENT /*147483*/:
                        P2pStateMachine.this.logd(getName() + " go success");
                        break;
                    case WifiP2pMonitor.P2P_GO_NEGOTIATION_FAILURE_EVENT /*147482*/:
                        if (message.obj == P2pStatus.NO_COMMON_CHANNEL) {
                            P2pStateMachine.this.transitionTo(P2pStateMachine.this.mFrequencyConflictState);
                            break;
                        }
                    case WifiP2pMonitor.P2P_GROUP_FORMATION_FAILURE_EVENT /*147484*/:
                        if (((P2pStatus) message.obj) == P2pStatus.NO_COMMON_CHANNEL) {
                            P2pStateMachine.this.transitionTo(P2pStateMachine.this.mFrequencyConflictState);
                            break;
                        }
                        break;
                    case WifiP2pMonitor.P2P_GROUP_STARTED_EVENT /*147485*/:
                        if (message.obj != null) {
                            P2pStateMachine.this.mGroup = (WifiP2pGroup) message.obj;
                            P2pStateMachine.this.logd(getName() + " group started");
                            if (P2pStateMachine.this.mGroup.getNetworkId() == -2) {
                                P2pStateMachine.this.updatePersistentNetworks(WifiP2pServiceImpl.NO_RELOAD.booleanValue());
                                P2pStateMachine.this.mGroup.setNetworkId(P2pStateMachine.this.mGroups.getNetworkId(P2pStateMachine.this.mGroup.getOwner().deviceAddress, P2pStateMachine.this.mGroup.getNetworkName()));
                            }
                            if (P2pStateMachine.this.mGroup.isGroupOwner()) {
                                WifiP2pServiceImpl.this.sendGroupConfigInfo(P2pStateMachine.this.mGroup);
                                if (!WifiP2pServiceImpl.this.mAutonomousGroup) {
                                    P2pStateMachine.this.mWifiNative.setP2pGroupIdle(P2pStateMachine.this.mGroup.getInterface(), 10);
                                }
                                if (WifiP2pServiceHisiExt.hisiWifiEnabled()) {
                                    P2pStateMachine.this.startDhcpServer(HwWifiCHRStateManager.MAIN_IFACE);
                                } else {
                                    P2pStateMachine.this.startDhcpServer(P2pStateMachine.this.mGroup.getInterface());
                                }
                            } else {
                                if (!WifiP2pServiceImpl.this.getMagicLinkDeviceFlag()) {
                                    P2pStateMachine.this.mWifiNative.setP2pGroupIdle(P2pStateMachine.this.mGroup.getInterface(), 10);
                                    if (WifiP2pServiceHisiExt.hisiWifiEnabled()) {
                                        WifiP2pServiceImpl.this.startIpManager(HwWifiCHRStateManager.MAIN_IFACE);
                                    } else {
                                        WifiP2pServiceImpl.this.startIpManager(P2pStateMachine.this.mGroup.getInterface());
                                    }
                                    if (P2pStateMachine.this.mWifiInjector == null) {
                                        P2pStateMachine.this.mWifiInjector = WifiInjector.getInstance();
                                    }
                                    P2pStateMachine.this.mWifiInjector.getWifiNative().setBluetoothCoexistenceMode(1);
                                    P2pStateMachine.this.mIsBTCoexDisabled = true;
                                }
                                WifiP2pDevice groupOwner = P2pStateMachine.this.mGroup.getOwner();
                                if (groupOwner != null) {
                                    WifiP2pDevice peer = P2pStateMachine.this.mPeers.get(groupOwner.deviceAddress);
                                    if (peer != null) {
                                        groupOwner.updateSupplicantDetails(peer);
                                        P2pStateMachine.this.mPeers.updateStatus(groupOwner.deviceAddress, 0);
                                        P2pStateMachine.this.sendPeersChangedBroadcast();
                                    } else {
                                        if (!(groupOwner == null || ("00:00:00:00:00:00".equals(groupOwner.deviceAddress) ^ 1) == 0)) {
                                            Matcher match = Pattern.compile("([0-9a-f]{2}:){5}[0-9a-f]{2}").matcher(groupOwner.deviceAddress);
                                            Log.e(WifiP2pServiceImpl.TAG, "try to judge groupOwner is valid or not");
                                            if (match.find()) {
                                                groupOwner.primaryDeviceType = "10-0050F204-5";
                                                P2pStateMachine.this.mPeers.updateSupplicantDetails(groupOwner);
                                                P2pStateMachine.this.mPeers.updateStatus(groupOwner.deviceAddress, 0);
                                                P2pStateMachine.this.sendPeersChangedBroadcast();
                                            }
                                        }
                                        P2pStateMachine.this.logw("Unknown group owner " + groupOwner);
                                    }
                                } else {
                                    P2pStateMachine.this.loge("Group owner is null.");
                                }
                            }
                            P2pStateMachine.this.transitionTo(P2pStateMachine.this.mGroupCreatedState);
                            break;
                        }
                        Log.e(WifiP2pServiceImpl.TAG, "Illegal argument(s)");
                        break;
                    case WifiP2pMonitor.P2P_GROUP_REMOVED_EVENT /*147486*/:
                        P2pStateMachine.this.logd(getName() + " go failure");
                        P2pStateMachine.this.handleGroupCreationFailure();
                        WifiP2pServiceImpl.this.sendP2pFailStateBroadcast();
                        P2pStateMachine.this.transitionTo(P2pStateMachine.this.mInactiveState);
                        break;
                    case WifiP2pMonitor.P2P_INVITATION_RESULT_EVENT /*147488*/:
                        P2pStatus status = (P2pStatus) message.obj;
                        if (status != P2pStatus.SUCCESS) {
                            P2pStateMachine.this.loge("Invitation result " + status);
                            if (status != P2pStatus.UNKNOWN_P2P_GROUP) {
                                if (status != P2pStatus.INFORMATION_IS_CURRENTLY_UNAVAILABLE) {
                                    if (status != P2pStatus.NO_COMMON_CHANNEL) {
                                        P2pStateMachine.this.handleGroupCreationFailure();
                                        P2pStateMachine.this.transitionTo(P2pStateMachine.this.mInactiveState);
                                        break;
                                    }
                                    P2pStateMachine.this.transitionTo(P2pStateMachine.this.mFrequencyConflictState);
                                    break;
                                }
                                P2pStateMachine.this.mSavedPeerConfig.netId = -2;
                                P2pStateMachine.this.p2pConnectWithPinDisplay(P2pStateMachine.this.mSavedPeerConfig);
                                break;
                            }
                            int netId = P2pStateMachine.this.mSavedPeerConfig.netId;
                            if (netId >= 0) {
                                P2pStateMachine.this.logd("Remove unknown client from the list");
                                P2pStateMachine.this.removeClientFromList(netId, P2pStateMachine.this.mSavedPeerConfig.deviceAddress, true);
                            }
                            P2pStateMachine.this.mSavedPeerConfig.netId = -2;
                            P2pStateMachine.this.p2pConnectWithPinDisplay(P2pStateMachine.this.mSavedPeerConfig);
                            break;
                        }
                        break;
                    case WifiP2pMonitor.P2P_REMOVE_AND_REFORM_GROUP_EVENT /*147496*/:
                        P2pStateMachine.this.logd("P2P_REMOVE_AND_REFORM_GROUP_EVENT event received in GroupNegotiationState state");
                        P2pStateMachine.this.mPendingReformGroupIndication = true;
                        break;
                    default:
                        return WifiP2pServiceImpl.this.handleGroupNegotiationStateExMessage(message);
                }
                return true;
            }
        }

        class InactiveState extends State {
            InactiveState() {
            }

            public void enter() {
                P2pStateMachine.this.logd(getName());
                WifiP2pServiceImpl.this.mIsInvite = false;
                WifiP2pServiceImpl.this.setmMagicLinkDeviceFlag(false);
                P2pStateMachine.this.mSavedPeerConfig.invalidate();
            }

            public boolean processMessage(Message message) {
                P2pStateMachine.this.logd(getName() + message.what);
                if (WifiP2pServiceImpl.this.processMessageForP2pCollision(message, this)) {
                    return true;
                }
                WifiP2pConfig config;
                switch (message.what) {
                    case 139268:
                        if (!P2pStateMachine.this.mWifiNative.p2pStopFind()) {
                            P2pStateMachine.this.replyToMessage(message, 139269, 0);
                            break;
                        }
                        P2pStateMachine.this.mWifiNative.p2pFlush();
                        WifiP2pServiceImpl.this.mServiceDiscReqId = null;
                        P2pStateMachine.this.replyToMessage(message, 139270);
                        break;
                    case 139271:
                        WifiP2pServiceImpl.this.setmMagicLinkDeviceFlag(false);
                        P2pStateMachine.this.logd(getName() + " sending connect");
                        config = message.obj;
                        if (!P2pStateMachine.this.isConfigInvalid(config)) {
                            WifiP2pServiceImpl.this.mAutonomousGroup = false;
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
                        }
                        P2pStateMachine.this.loge("Dropping connect requeset " + config);
                        P2pStateMachine.this.replyToMessage(message, 139272);
                        break;
                    case 139277:
                        boolean ret;
                        WifiP2pServiceImpl.this.mAutonomousGroup = true;
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
                        }
                        P2pStateMachine.this.replyToMessage(message, 139279);
                        P2pStateMachine.this.transitionTo(P2pStateMachine.this.mGroupNegotiationState);
                        break;
                    case 139329:
                        P2pStateMachine.this.logd(getName() + " start listen mode");
                        P2pStateMachine.this.mWifiNative.p2pFlush();
                        if (!P2pStateMachine.this.mWifiNative.p2pExtListen(true, 500, 500)) {
                            P2pStateMachine.this.replyToMessage(message, 139330);
                            break;
                        }
                        P2pStateMachine.this.replyToMessage(message, 139331);
                        break;
                    case 139332:
                        P2pStateMachine.this.logd(getName() + " stop listen mode");
                        if (P2pStateMachine.this.mWifiNative.p2pExtListen(false, 0, 0)) {
                            P2pStateMachine.this.replyToMessage(message, 139334);
                        } else {
                            P2pStateMachine.this.replyToMessage(message, 139333);
                        }
                        P2pStateMachine.this.mWifiNative.p2pFlush();
                        break;
                    case 139335:
                        if (message.obj != null) {
                            Bundle p2pChannels = message.obj;
                            int lc = p2pChannels.getInt("lc", 0);
                            int oc = p2pChannels.getInt("oc", 0);
                            P2pStateMachine.this.logd(getName() + " set listen and operating channel");
                            if (!P2pStateMachine.this.mWifiNative.p2pSetChannel(lc, oc)) {
                                P2pStateMachine.this.replyToMessage(message, 139336);
                                break;
                            }
                            P2pStateMachine.this.replyToMessage(message, 139337);
                            break;
                        }
                        Log.e(WifiP2pServiceImpl.TAG, "Illegal arguments(s)");
                        break;
                    case 139342:
                        String handoverSelect = null;
                        if (message.obj != null) {
                            handoverSelect = ((Bundle) message.obj).getString("android.net.wifi.p2p.EXTRA_HANDOVER_MESSAGE");
                        }
                        if (handoverSelect != null && P2pStateMachine.this.mWifiNative.initiatorReportNfcHandover(handoverSelect)) {
                            P2pStateMachine.this.replyToMessage(message, 139344);
                            P2pStateMachine.this.transitionTo(P2pStateMachine.this.mGroupCreatingState);
                            break;
                        }
                        P2pStateMachine.this.replyToMessage(message, 139345);
                        break;
                        break;
                    case 139343:
                        String handoverRequest = null;
                        if (message.obj != null) {
                            handoverRequest = ((Bundle) message.obj).getString("android.net.wifi.p2p.EXTRA_HANDOVER_MESSAGE");
                        }
                        if (handoverRequest != null && P2pStateMachine.this.mWifiNative.responderReportNfcHandover(handoverRequest)) {
                            P2pStateMachine.this.replyToMessage(message, 139344);
                            P2pStateMachine.this.transitionTo(P2pStateMachine.this.mGroupCreatingState);
                            break;
                        }
                        P2pStateMachine.this.replyToMessage(message, 139345);
                        break;
                    case 141268:
                        WifiP2pServiceImpl.this.mCreateWifiBridge = true;
                        return WifiP2pServiceImpl.this.handleInactiveStateMessage(message);
                    case WifiP2pMonitor.P2P_GO_NEGOTIATION_REQUEST_EVENT /*147479*/:
                        config = (WifiP2pConfig) message.obj;
                        if (!P2pStateMachine.this.isConfigInvalid(config)) {
                            P2pStateMachine.this.mSavedPeerConfig = config;
                            WifiP2pServiceImpl.this.mAutonomousGroup = false;
                            WifiP2pServiceImpl.this.mJoinExistingGroup = false;
                            P2pStateMachine.this.transitionTo(P2pStateMachine.this.mUserAuthorizingNegotiationRequestState);
                            break;
                        }
                        P2pStateMachine.this.loge("Dropping GO neg request " + config);
                        break;
                    case WifiP2pMonitor.P2P_GROUP_STARTED_EVENT /*147485*/:
                        if (message.obj != null) {
                            P2pStateMachine.this.mGroup = (WifiP2pGroup) message.obj;
                            P2pStateMachine.this.logd(getName() + " group started");
                            if (P2pStateMachine.this.mGroup.getNetworkId() != -2) {
                                P2pStateMachine.this.loge("Unexpected group creation, remove " + P2pStateMachine.this.mGroup);
                                P2pStateMachine.this.mWifiNative.p2pGroupRemove(P2pStateMachine.this.mGroup.getInterface());
                                break;
                            }
                            WifiP2pServiceImpl.this.mAutonomousGroup = false;
                            P2pStateMachine.this.deferMessage(message);
                            P2pStateMachine.this.transitionTo(P2pStateMachine.this.mGroupNegotiationState);
                            break;
                        }
                        Log.e(WifiP2pServiceImpl.TAG, "Invalid argument(s)");
                        break;
                    case WifiP2pMonitor.P2P_INVITATION_RECEIVED_EVENT /*147487*/:
                        if (message.obj != null) {
                            WifiP2pGroup group = message.obj;
                            WifiP2pDevice owner = group.getOwner();
                            if (owner == null) {
                                int id = group.getNetworkId();
                                if (id >= 0) {
                                    String addr = P2pStateMachine.this.mGroups.getOwnerAddr(id);
                                    if (addr == null) {
                                        P2pStateMachine.this.loge("Ignored invitation from null owner");
                                        break;
                                    }
                                    group.setOwner(new WifiP2pDevice(addr));
                                    owner = group.getOwner();
                                } else {
                                    P2pStateMachine.this.loge("Ignored invitation from null owner");
                                    break;
                                }
                            }
                            config = new WifiP2pConfig();
                            config.deviceAddress = group.getOwner().deviceAddress;
                            if (!P2pStateMachine.this.isConfigInvalid(config)) {
                                P2pStateMachine.this.mSavedPeerConfig = config;
                                if (owner != null) {
                                    owner = P2pStateMachine.this.mPeers.get(owner.deviceAddress);
                                    if (owner != null) {
                                        if (owner.wpsPbcSupported()) {
                                            P2pStateMachine.this.mSavedPeerConfig.wps.setup = 0;
                                        } else if (owner.wpsKeypadSupported()) {
                                            P2pStateMachine.this.mSavedPeerConfig.wps.setup = 2;
                                        } else if (owner.wpsDisplaySupported()) {
                                            P2pStateMachine.this.mSavedPeerConfig.wps.setup = 1;
                                        }
                                    }
                                }
                                WifiP2pServiceImpl.this.mAutonomousGroup = false;
                                WifiP2pServiceImpl.this.mJoinExistingGroup = true;
                                WifiP2pServiceImpl.this.mIsInvite = true;
                                P2pStateMachine.this.transitionTo(P2pStateMachine.this.mUserAuthorizingInviteRequestState);
                                break;
                            }
                            P2pStateMachine.this.loge("Dropping invitation request " + config);
                            break;
                        }
                        Log.e(WifiP2pServiceImpl.TAG, "Invalid argument(s)");
                        break;
                    case WifiP2pMonitor.P2P_PROV_DISC_PBC_REQ_EVENT /*147489*/:
                    case WifiP2pMonitor.P2P_PROV_DISC_ENTER_PIN_EVENT /*147491*/:
                        break;
                    case WifiP2pMonitor.P2P_PROV_DISC_SHOW_PIN_EVENT /*147492*/:
                        WifiP2pProvDiscEvent provDisc = message.obj;
                        WifiP2pDevice device = provDisc.device;
                        if (device != null) {
                            P2pStateMachine.this.notifyP2pProvDiscShowPinRequest(provDisc.pin, device.deviceAddress);
                            P2pStateMachine.this.mPeers.updateStatus(device.deviceAddress, 1);
                            P2pStateMachine.this.sendPeersChangedBroadcast();
                            P2pStateMachine.this.transitionTo(P2pStateMachine.this.mGroupNegotiationState);
                            break;
                        }
                        Slog.d(WifiP2pServiceImpl.TAG, "Device entry is null");
                        break;
                    default:
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
                P2pStateMachine.this.logd(getName() + message.toString());
                switch (message.what) {
                    case 139280:
                        P2pStateMachine.this.replyToMessage(message, 139282);
                        break;
                    case 141268:
                        if (!WifiP2pServiceImpl.this.getWifiRepeaterEnabled()) {
                            P2pStateMachine.this.deferMessage(message);
                            break;
                        }
                        break;
                    default:
                        return WifiP2pServiceImpl.this.handleOngoingGroupRemovalStateExMessage(message);
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
                P2pStateMachine.this.logd(getName() + message.toString());
                switch (message.what) {
                    case WifiStateMachine.CMD_ENABLE_P2P /*131203*/:
                        try {
                            WifiP2pServiceImpl.this.mNwService.setInterfaceUp(P2pStateMachine.this.mWifiNative.getInterfaceName());
                        } catch (RemoteException re) {
                            P2pStateMachine.this.loge("Unable to change interface settings: " + re);
                        } catch (IllegalStateException ie) {
                            P2pStateMachine.this.loge("Unable to change interface settings: " + ie);
                        }
                        P2pStateMachine.this.mWifiMonitor.startMonitoring(P2pStateMachine.this.mWifiNative.getInterfaceName());
                        P2pStateMachine.this.transitionTo(P2pStateMachine.this.mP2pEnablingState);
                        return true;
                    default:
                        return false;
                }
            }
        }

        class P2pDisablingState extends State {
            P2pDisablingState() {
            }

            public void enter() {
                P2pStateMachine.this.logd(getName());
                P2pStateMachine.this.sendMessageDelayed(P2pStateMachine.this.obtainMessage(WifiP2pServiceImpl.DISABLE_P2P_TIMED_OUT, WifiP2pServiceImpl.sDisableP2pTimeoutIndex = WifiP2pServiceImpl.sDisableP2pTimeoutIndex + 1, 0), 5000);
            }

            public boolean processMessage(Message message) {
                P2pStateMachine.this.logd(getName() + message.what);
                switch (message.what) {
                    case WifiStateMachine.CMD_ENABLE_P2P /*131203*/:
                    case WifiStateMachine.CMD_DISABLE_P2P_REQ /*131204*/:
                        P2pStateMachine.this.deferMessage(message);
                        break;
                    case WifiP2pServiceImpl.DISABLE_P2P_TIMED_OUT /*143366*/:
                        if (WifiP2pServiceImpl.sDisableP2pTimeoutIndex == message.arg1) {
                            P2pStateMachine.this.loge("P2p disable timed out");
                            P2pStateMachine.this.transitionTo(P2pStateMachine.this.mP2pDisabledState);
                            break;
                        }
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
                P2pStateMachine.this.sendP2pStateChangedBroadcast(true);
                WifiP2pServiceImpl.this.mNetworkInfo.setIsAvailable(true);
                if (!WifiP2pServiceHisiExt.hisiWifiEnabled()) {
                    P2pStateMachine.this.sendP2pConnectionChangedBroadcast();
                } else if (WifiP2pServiceImpl.this.isWifiP2pEnabled()) {
                    P2pStateMachine.this.sendP2pConnectionChangedBroadcast();
                }
                P2pStateMachine.this.initializeP2pSettings();
            }

            public boolean processMessage(Message message) {
                P2pStateMachine.this.logd(getName() + " when=" + message.getWhen() + " what=" + message.what + " arg1=" + message.arg1 + " arg2=" + message.arg2);
                if (WifiP2pServiceImpl.this.processMessageForP2pCollision(message, this)) {
                    return true;
                }
                switch (message.what) {
                    case WifiStateMachine.CMD_ENABLE_P2P /*131203*/:
                        break;
                    case WifiStateMachine.CMD_DISABLE_P2P_REQ /*131204*/:
                        if (P2pStateMachine.this.mPeers.clear()) {
                            P2pStateMachine.this.sendPeersChangedBroadcast();
                        }
                        if (P2pStateMachine.this.mGroups.clear()) {
                            P2pStateMachine.this.sendP2pPersistentGroupsChangedBroadcast();
                        }
                        P2pStateMachine.this.mWifiMonitor.stopMonitoring(P2pStateMachine.this.mWifiNative.getInterfaceName());
                        P2pStateMachine.this.transitionTo(P2pStateMachine.this.mP2pDisablingState);
                        break;
                    case 139265:
                        if (!WifiP2pServiceImpl.this.mDiscoveryBlocked) {
                            P2pStateMachine.this.clearSupplicantServiceRequest();
                            if (!P2pStateMachine.this.mWifiNative.p2pFind(120)) {
                                P2pStateMachine.this.replyToMessage(message, 139266, 0);
                                break;
                            }
                            P2pStateMachine.this.replyToMessage(message, 139267);
                            P2pStateMachine.this.sendP2pDiscoveryChangedBroadcast(true);
                            break;
                        }
                        P2pStateMachine.this.replyToMessage(message, 139266, 2);
                        break;
                    case 139268:
                        if (!P2pStateMachine.this.mWifiNative.p2pStopFind()) {
                            P2pStateMachine.this.replyToMessage(message, 139269, 0);
                            break;
                        }
                        P2pStateMachine.this.replyToMessage(message, 139270);
                        break;
                    case 139292:
                        P2pStateMachine.this.logd(getName() + " add service");
                        if (!P2pStateMachine.this.addLocalService(message.replyTo, message.obj)) {
                            P2pStateMachine.this.replyToMessage(message, 139293);
                            break;
                        }
                        P2pStateMachine.this.replyToMessage(message, 139294);
                        break;
                    case 139295:
                        P2pStateMachine.this.logd(getName() + " remove service");
                        P2pStateMachine.this.removeLocalService(message.replyTo, (WifiP2pServiceInfo) message.obj);
                        P2pStateMachine.this.replyToMessage(message, 139297);
                        break;
                    case 139298:
                        P2pStateMachine.this.logd(getName() + " clear service");
                        P2pStateMachine.this.clearLocalServices(message.replyTo);
                        P2pStateMachine.this.replyToMessage(message, 139300);
                        break;
                    case 139301:
                        P2pStateMachine.this.logd(getName() + " add service request");
                        if (!P2pStateMachine.this.addServiceRequest(message.replyTo, (WifiP2pServiceRequest) message.obj)) {
                            P2pStateMachine.this.replyToMessage(message, 139302);
                            break;
                        }
                        P2pStateMachine.this.replyToMessage(message, 139303);
                        break;
                    case 139304:
                        P2pStateMachine.this.logd(getName() + " remove service request");
                        P2pStateMachine.this.removeServiceRequest(message.replyTo, (WifiP2pServiceRequest) message.obj);
                        P2pStateMachine.this.replyToMessage(message, 139306);
                        break;
                    case 139307:
                        P2pStateMachine.this.logd(getName() + " clear service request");
                        P2pStateMachine.this.clearServiceRequests(message.replyTo);
                        P2pStateMachine.this.replyToMessage(message, 139309);
                        break;
                    case 139310:
                        if (!WifiP2pServiceImpl.this.mDiscoveryBlocked) {
                            P2pStateMachine.this.logd(getName() + " discover services");
                            if (P2pStateMachine.this.updateSupplicantServiceRequest()) {
                                if (!P2pStateMachine.this.mWifiNative.p2pFind(120)) {
                                    P2pStateMachine.this.replyToMessage(message, 139311, 0);
                                    break;
                                }
                                P2pStateMachine.this.replyToMessage(message, 139312);
                                break;
                            }
                            P2pStateMachine.this.replyToMessage(message, 139311, 3);
                            break;
                        }
                        P2pStateMachine.this.replyToMessage(message, 139311, 2);
                        break;
                    case 139315:
                        WifiP2pDevice d = message.obj;
                        if (d != null && P2pStateMachine.this.setAndPersistDeviceName(d.deviceName)) {
                            P2pStateMachine.this.logd("set device name " + d.deviceName);
                            P2pStateMachine.this.replyToMessage(message, 139317);
                            break;
                        }
                        P2pStateMachine.this.replyToMessage(message, 139316, 0);
                        break;
                        break;
                    case 139318:
                        P2pStateMachine.this.logd(getName() + " delete persistent group");
                        P2pStateMachine.this.mGroups.remove(message.arg1);
                        P2pStateMachine.this.replyToMessage(message, 139320);
                        break;
                    case 139323:
                        WifiP2pWfdInfo d2 = message.obj;
                        if (WifiP2pServiceImpl.this.getWfdPermission(message.sendingUid)) {
                            if (d2 != null && P2pStateMachine.this.setWfdInfo(d2)) {
                                P2pStateMachine.this.replyToMessage(message, 139325);
                                break;
                            }
                            P2pStateMachine.this.replyToMessage(message, 139324, 0);
                            break;
                        }
                        P2pStateMachine.this.replyToMessage(message, 139324, 0);
                        break;
                        break;
                    case 139329:
                        P2pStateMachine.this.logd(getName() + " start listen mode");
                        P2pStateMachine.this.mWifiNative.p2pFlush();
                        if (!P2pStateMachine.this.mWifiNative.p2pExtListen(true, 500, 500)) {
                            P2pStateMachine.this.replyToMessage(message, 139330);
                            break;
                        }
                        P2pStateMachine.this.replyToMessage(message, 139331);
                        break;
                    case 139332:
                        P2pStateMachine.this.logd(getName() + " stop listen mode");
                        if (P2pStateMachine.this.mWifiNative.p2pExtListen(false, 0, 0)) {
                            P2pStateMachine.this.replyToMessage(message, 139334);
                        } else {
                            P2pStateMachine.this.replyToMessage(message, 139333);
                        }
                        P2pStateMachine.this.mWifiNative.p2pFlush();
                        break;
                    case 139335:
                        Bundle p2pChannels = message.obj;
                        int lc = p2pChannels.getInt("lc", 0);
                        int oc = p2pChannels.getInt("oc", 0);
                        P2pStateMachine.this.logd(getName() + " set listen and operating channel");
                        if (!P2pStateMachine.this.mWifiNative.p2pSetChannel(lc, oc)) {
                            P2pStateMachine.this.replyToMessage(message, 139336);
                            break;
                        }
                        P2pStateMachine.this.replyToMessage(message, 139337);
                        break;
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
                            WifiP2pServiceImpl.this.mDiscoveryBlocked = blocked;
                            if (blocked && WifiP2pServiceImpl.this.mDiscoveryStarted) {
                                P2pStateMachine.this.mWifiNative.p2pStopFind();
                                WifiP2pServiceImpl.this.mDiscoveryPostponed = true;
                            }
                            if (!blocked && WifiP2pServiceImpl.this.mDiscoveryPostponed) {
                                WifiP2pServiceImpl.this.mDiscoveryPostponed = false;
                                P2pStateMachine.this.mWifiNative.p2pFind(120);
                            }
                            if (blocked) {
                                if (message.obj != null) {
                                    try {
                                        message.obj.sendMessage(message.arg2);
                                        break;
                                    } catch (Exception e) {
                                        P2pStateMachine.this.loge("unable to send BLOCK_DISCOVERY response: " + e);
                                        break;
                                    }
                                }
                                Log.e(WifiP2pServiceImpl.TAG, "Illegal argument(s)");
                                break;
                            }
                        }
                        break;
                    case 147458:
                        P2pStateMachine.this.loge("Unexpected loss of p2p socket connection");
                        WifiP2pServiceImpl.this.mWifiChannel.sendMessage(147458);
                        P2pStateMachine.this.transitionTo(P2pStateMachine.this.mP2pDisabledState);
                        break;
                    case WifiP2pMonitor.P2P_DEVICE_FOUND_EVENT /*147477*/:
                        if (message.obj != null) {
                            WifiP2pDevice device = message.obj;
                            if (!WifiP2pServiceImpl.this.mThisDevice.deviceAddress.equals(device.deviceAddress)) {
                                P2pStateMachine.this.mPeers.updateSupplicantDetails(device);
                                P2pStateMachine.this.sendPeersChangedBroadcast();
                                if (WifiP2pServiceHisiExt.hisiWifiEnabled() && WifiP2pServiceImpl.this.mWifiP2pServiceHisiExt.P2pFindDeviceUpdate) {
                                    WifiP2pServiceImpl.this.mWifiP2pServiceHisiExt.P2pFindDeviceUpdate = false;
                                    P2pStateMachine.this.updatePersistentNetworks(true);
                                    break;
                                }
                            }
                        }
                        Log.e(WifiP2pServiceImpl.TAG, "Illegal argument(s)");
                        break;
                        break;
                    case WifiP2pMonitor.P2P_DEVICE_LOST_EVENT /*147478*/:
                        if (message.obj != null) {
                            if (P2pStateMachine.this.mPeers.remove(((WifiP2pDevice) message.obj).deviceAddress) != null) {
                                P2pStateMachine.this.sendPeersChangedBroadcast();
                                break;
                            }
                        }
                        Log.e(WifiP2pServiceImpl.TAG, "Illegal argument(s)");
                        break;
                        break;
                    case WifiP2pMonitor.P2P_FIND_STOPPED_EVENT /*147493*/:
                        P2pStateMachine.this.sendP2pDiscoveryChangedBroadcast(false);
                        break;
                    case WifiP2pMonitor.P2P_SERV_DISC_RESP_EVENT /*147494*/:
                        P2pStateMachine.this.logd(getName() + " receive service response");
                        if (message.obj != null) {
                            for (WifiP2pServiceResponse resp : message.obj) {
                                resp.setSrcDevice(P2pStateMachine.this.mPeers.get(resp.getSrcDevice().deviceAddress));
                                P2pStateMachine.this.sendServiceResponse(resp);
                            }
                            break;
                        }
                        Log.e(WifiP2pServiceImpl.TAG, "Illegal argument(s)");
                        break;
                    default:
                        return WifiP2pServiceImpl.this.handleP2pEnabledStateExMessage(message);
                }
                return true;
            }

            public void exit() {
                P2pStateMachine.this.sendP2pDiscoveryChangedBroadcast(false);
                if (!WifiP2pServiceHisiExt.hisiWifiEnabled()) {
                    P2pStateMachine.this.sendP2pStateChangedBroadcast(false);
                }
                WifiP2pServiceImpl.this.mNetworkInfo.setIsAvailable(false);
            }
        }

        class P2pEnablingState extends State {
            P2pEnablingState() {
            }

            public void enter() {
                P2pStateMachine.this.logd(getName());
            }

            public boolean processMessage(Message message) {
                P2pStateMachine.this.logd(getName() + message.toString());
                switch (message.what) {
                    case WifiStateMachine.CMD_ENABLE_P2P /*131203*/:
                    case WifiStateMachine.CMD_DISABLE_P2P_REQ /*131204*/:
                        P2pStateMachine.this.deferMessage(message);
                        break;
                    case 147457:
                        P2pStateMachine.this.logd("P2p socket connection successful");
                        P2pStateMachine.this.transitionTo(P2pStateMachine.this.mInactiveState);
                        break;
                    case 147458:
                        P2pStateMachine.this.loge("P2p socket connection failed");
                        P2pStateMachine.this.transitionTo(P2pStateMachine.this.mP2pDisabledState);
                        break;
                    default:
                        return false;
                }
                return true;
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
                        if (!WifiP2pServiceImpl.this.getWfdPermission(message.sendingUid)) {
                            P2pStateMachine.this.replyToMessage(message, 139324, 0);
                            break;
                        }
                        P2pStateMachine.this.replyToMessage(message, 139324, 1);
                        break;
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
                P2pStateMachine.this.logd(getName() + message.toString());
                WifiP2pDevice device;
                switch (message.what) {
                    case WifiP2pMonitor.P2P_PROV_DISC_PBC_RSP_EVENT /*147490*/:
                        if (message.obj != null) {
                            device = message.obj.device;
                            if ((device == null || (device.deviceAddress.equals(P2pStateMachine.this.mSavedPeerConfig.deviceAddress) ^ 1) == 0) && P2pStateMachine.this.mSavedPeerConfig.wps.setup == 0) {
                                P2pStateMachine.this.logd("Found a match " + P2pStateMachine.this.mSavedPeerConfig);
                                P2pStateMachine.this.p2pConnectWithPinDisplay(P2pStateMachine.this.mSavedPeerConfig);
                                P2pStateMachine.this.transitionTo(P2pStateMachine.this.mGroupNegotiationState);
                                break;
                            }
                        }
                        Log.e(WifiP2pServiceImpl.TAG, "Invalid argument(s)");
                        break;
                    case WifiP2pMonitor.P2P_PROV_DISC_ENTER_PIN_EVENT /*147491*/:
                        if (message.obj != null) {
                            device = message.obj.device;
                            if ((device == null || (device.deviceAddress.equals(P2pStateMachine.this.mSavedPeerConfig.deviceAddress) ^ 1) == 0) && P2pStateMachine.this.mSavedPeerConfig.wps.setup == 2) {
                                P2pStateMachine.this.logd("Found a match " + P2pStateMachine.this.mSavedPeerConfig);
                                if (!TextUtils.isEmpty(P2pStateMachine.this.mSavedPeerConfig.wps.pin)) {
                                    P2pStateMachine.this.p2pConnectWithPinDisplay(P2pStateMachine.this.mSavedPeerConfig);
                                    P2pStateMachine.this.transitionTo(P2pStateMachine.this.mGroupNegotiationState);
                                    break;
                                }
                                WifiP2pServiceImpl.this.mJoinExistingGroup = false;
                                P2pStateMachine.this.transitionTo(P2pStateMachine.this.mUserAuthorizingNegotiationRequestState);
                                break;
                            }
                        }
                        Log.e(WifiP2pServiceImpl.TAG, "Illegal argument(s)");
                        break;
                    case WifiP2pMonitor.P2P_PROV_DISC_SHOW_PIN_EVENT /*147492*/:
                        if (message.obj != null) {
                            WifiP2pProvDiscEvent provDisc = message.obj;
                            device = provDisc.device;
                            if (device != null) {
                                if (device.deviceAddress.equals(P2pStateMachine.this.mSavedPeerConfig.deviceAddress) && P2pStateMachine.this.mSavedPeerConfig.wps.setup == 1) {
                                    P2pStateMachine.this.logd("Found a match " + P2pStateMachine.this.mSavedPeerConfig);
                                    P2pStateMachine.this.mSavedPeerConfig.wps.pin = provDisc.pin;
                                    P2pStateMachine.this.p2pConnectWithPinDisplay(P2pStateMachine.this.mSavedPeerConfig);
                                    P2pStateMachine.this.notifyInvitationSent(provDisc.pin, device.deviceAddress);
                                    P2pStateMachine.this.transitionTo(P2pStateMachine.this.mGroupNegotiationState);
                                    break;
                                }
                            }
                            Log.e(WifiP2pServiceImpl.TAG, "Invalid device");
                            break;
                        }
                        Log.e(WifiP2pServiceImpl.TAG, "Illegal argument(s)");
                        break;
                    case WifiP2pMonitor.P2P_PROV_DISC_FAILURE_EVENT /*147495*/:
                        P2pStateMachine.this.loge("provision discovery failed");
                        P2pStateMachine.this.handleGroupCreationFailure();
                        P2pStateMachine.this.transitionTo(P2pStateMachine.this.mInactiveState);
                        break;
                    default:
                        return false;
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
                P2pStateMachine.this.logd(getName() + message.toString());
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
                        P2pStateMachine.this.logd("User rejected invitation " + P2pStateMachine.this.mSavedPeerConfig);
                        P2pStateMachine.this.mWifiNative.p2pReject(P2pStateMachine.this.mSavedPeerConfig.deviceAddress);
                        P2pStateMachine.this.p2pConnectWithPinDisplay(P2pStateMachine.this.mSavedPeerConfig);
                        P2pStateMachine.this.mWifiNative.p2pFlush();
                        P2pStateMachine.this.mWifiNative.p2pFind(120);
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
                P2pStateMachine.this.logd(getName() + message.toString());
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
                    case WifiP2pMonitor.P2P_PROV_DISC_PBC_REQ_EVENT /*147489*/:
                    case WifiP2pMonitor.P2P_PROV_DISC_ENTER_PIN_EVENT /*147491*/:
                    case WifiP2pMonitor.P2P_PROV_DISC_SHOW_PIN_EVENT /*147492*/:
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
                P2pStateMachine.this.logd(getName() + message.toString());
                switch (message.what) {
                    case WifiP2pServiceImpl.PEER_CONNECTION_USER_ACCEPT /*143362*/:
                        P2pStateMachine.this.mWifiNative.p2pStopFind();
                        P2pStateMachine.this.p2pConnectWithPinDisplay(P2pStateMachine.this.mSavedPeerConfig);
                        P2pStateMachine.this.mPeers.updateStatus(P2pStateMachine.this.mSavedPeerConfig.deviceAddress, 1);
                        P2pStateMachine.this.sendPeersChangedBroadcast();
                        P2pStateMachine.this.transitionTo(P2pStateMachine.this.mGroupNegotiationState);
                        break;
                    case WifiP2pServiceImpl.PEER_CONNECTION_USER_REJECT /*143363*/:
                        P2pStateMachine.this.logd("User rejected negotiation " + P2pStateMachine.this.mSavedPeerConfig);
                        P2pStateMachine.this.mWifiNative.p2pReject(P2pStateMachine.this.mSavedPeerConfig.deviceAddress);
                        P2pStateMachine.this.p2pConnectWithPinDisplay(P2pStateMachine.this.mSavedPeerConfig);
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
            addState(this.mP2pEnablingState, this.mDefaultState);
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
            String interfaceName = this.mWifiNative.getInterfaceName();
            this.mWifiMonitor.registerHandler(interfaceName, WifiP2pMonitor.AP_STA_CONNECTED_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiP2pMonitor.AP_STA_DISCONNECTED_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiP2pMonitor.P2P_DEVICE_FOUND_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiP2pMonitor.P2P_DEVICE_LOST_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiP2pMonitor.P2P_FIND_STOPPED_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiP2pMonitor.P2P_GO_NEGOTIATION_FAILURE_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiP2pMonitor.P2P_GO_NEGOTIATION_REQUEST_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiP2pMonitor.P2P_GO_NEGOTIATION_SUCCESS_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiP2pMonitor.P2P_GROUP_FORMATION_FAILURE_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiP2pMonitor.P2P_GROUP_FORMATION_SUCCESS_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiP2pMonitor.P2P_GROUP_REMOVED_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiP2pMonitor.P2P_GROUP_STARTED_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiP2pMonitor.P2P_INVITATION_RECEIVED_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiP2pMonitor.P2P_INVITATION_RESULT_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiP2pMonitor.P2P_PROV_DISC_ENTER_PIN_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiP2pMonitor.P2P_PROV_DISC_FAILURE_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiP2pMonitor.P2P_PROV_DISC_PBC_REQ_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiP2pMonitor.P2P_PROV_DISC_PBC_RSP_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiP2pMonitor.P2P_PROV_DISC_SHOW_PIN_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiP2pMonitor.P2P_SERV_DISC_RESP_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, 147457, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, 147458, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiP2pMonitor.P2P_REMOVE_AND_REFORM_GROUP_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, 147556, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, 147557, getHandler());
        }

        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            super.dump(fd, pw, args);
            pw.println("mWifiP2pInfo " + this.mWifiP2pInfo);
            pw.println("mGroup " + this.mGroup);
            pw.println("mSavedPeerConfig " + this.mSavedPeerConfig);
            pw.println("mGroups" + this.mGroups);
            pw.println();
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

        private void sendP2pDiscoveryChangedBroadcast(boolean started) {
            if (WifiP2pServiceImpl.this.mDiscoveryStarted != started) {
                int i;
                WifiP2pServiceImpl.this.mDiscoveryStarted = started;
                logd("discovery change broadcast " + started);
                Intent intent = new Intent("android.net.wifi.p2p.DISCOVERY_STATE_CHANGE");
                intent.addFlags(67108864);
                String str = "discoveryState";
                if (started) {
                    i = 2;
                } else {
                    i = 1;
                }
                intent.putExtra(str, i);
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

        private void sendPeersChangedBroadcast() {
            logd("sending p2pPeers change broadcast");
            Intent intent = new Intent("android.net.wifi.p2p.PEERS_CHANGED");
            intent.putExtra("wifiP2pDeviceList", new WifiP2pDeviceList(this.mPeers));
            intent.addFlags(67108864);
            WifiP2pServiceImpl.this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        }

        private void sendP2pConnectionChangedBroadcast() {
            logd("sending p2p connection changed broadcast");
            Intent intent = new Intent("android.net.wifi.p2p.CONNECTION_STATE_CHANGE");
            intent.addFlags(603979776);
            intent.putExtra("wifiP2pInfo", new WifiP2pInfo(this.mWifiP2pInfo));
            intent.putExtra("networkInfo", new NetworkInfo(WifiP2pServiceImpl.this.mNetworkInfo));
            intent.putExtra("p2pGroupInfo", new WifiP2pGroup(this.mGroup));
            WifiP2pServiceImpl.this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
            if (WifiP2pServiceImpl.this.mWifiChannel != null) {
                WifiP2pServiceImpl.this.mWifiChannel.sendMessage(WifiP2pServiceImpl.P2P_CONNECTION_CHANGED, new NetworkInfo(WifiP2pServiceImpl.this.mNetworkInfo));
            } else {
                loge("sendP2pConnectionChangedBroadcast(): WifiChannel is null");
            }
        }

        private void sendP2pPersistentGroupsChangedBroadcast() {
            logd("sending p2p persistent groups changed broadcast");
            Intent intent = new Intent("android.net.wifi.p2p.PERSISTENT_GROUPS_CHANGED");
            intent.addFlags(67108864);
            WifiP2pServiceImpl.this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
        }

        private void startDhcpServer(String intf) {
            try {
                InterfaceConfiguration ifcg = WifiP2pServiceImpl.this.mNwService.getInterfaceConfig(intf);
                if (WifiP2pServiceImpl.this.mCreateWifiBridge) {
                    WifiP2pServiceImpl.this.mP2pServerAddress = WifiP2pServiceImpl.this.getWifiRepeaterServerAddress();
                    if (WifiP2pServiceImpl.this.mP2pServerAddress == null) {
                        WifiP2pServiceImpl.this.mP2pServerAddress = WifiP2pServiceImpl.SERVER_ADDRESS_WIFI_BRIDGE;
                    }
                } else {
                    WifiP2pServiceImpl.this.mP2pServerAddress = WifiP2pServiceImpl.SERVER_ADDRESS;
                }
                if (this.mGroup != null) {
                    this.mGroup.setP2pServerAddress(WifiP2pServiceImpl.this.mP2pServerAddress);
                }
                ifcg.setLinkAddress(new LinkAddress(NetworkUtils.numericToInetAddress(WifiP2pServiceImpl.this.mP2pServerAddress), 24));
                WifiP2pServiceImpl.this.mCreateWifiBridge = false;
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
                WifiP2pServiceImpl.this.mCreateWifiBridge = false;
                loge("Error configuring interface " + intf + ", :" + e);
            }
        }

        private void stopDhcpServer(String intf) {
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
                logd("Stopped Dhcp server");
            }
        }

        private void notifyP2pEnableFailure() {
            Resources r = Resources.getSystem();
            AlertDialog dialog = new Builder(WifiP2pServiceImpl.this.mContext).setTitle(r.getString(17041250)).setMessage(r.getString(17041254)).setPositiveButton(r.getString(17039370), null).create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.getWindow().setType(2003);
            LayoutParams attrs = dialog.getWindow().getAttributes();
            attrs.privateFlags = 16;
            dialog.getWindow().setAttributes(attrs);
            dialog.show();
        }

        private void addRowToDialog(ViewGroup group, int stringId, String value) {
            Resources r = Resources.getSystem();
            View row = LayoutInflater.from(WifiP2pServiceImpl.this.mContext).inflate(17367319, group, false);
            ((TextView) row.findViewById(16909079)).setText(r.getString(stringId));
            ((TextView) row.findViewById(16909456)).setText(value);
            group.addView(row);
        }

        protected void notifyInvitationSent(String pin, String peerAddress) {
            Resources r = Resources.getSystem();
            View textEntryView = LayoutInflater.from(WifiP2pServiceImpl.this.mContext).inflate(17367317, null);
            ViewGroup group = (ViewGroup) textEntryView.findViewById(16908972);
            addRowToDialog(group, 17041260, getDeviceName(peerAddress));
            addRowToDialog(group, 17041259, pin);
            AlertDialog dialog = new Builder(WifiP2pServiceImpl.this.mContext).setTitle(r.getString(17041257)).setView(textEntryView).setPositiveButton(r.getString(17039370), null).create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.getWindow().setType(2003);
            LayoutParams attrs = dialog.getWindow().getAttributes();
            attrs.privateFlags = 16;
            dialog.getWindow().setAttributes(attrs);
            dialog.show();
        }

        private void notifyP2pProvDiscShowPinRequest(final String pin, final String peerAddress) {
            Resources r = Resources.getSystem();
            String tempDevAddress = peerAddress;
            String tempPin = pin;
            View textEntryView = LayoutInflater.from(WifiP2pServiceImpl.this.mContext).inflate(17367317, null);
            ViewGroup group = (ViewGroup) textEntryView.findViewById(16908972);
            addRowToDialog(group, 17041260, getDeviceName(peerAddress));
            addRowToDialog(group, 17041259, pin);
            AlertDialog dialog = new Builder(WifiP2pServiceImpl.this.mContext).setTitle(r.getString(17041257)).setView(textEntryView).setPositiveButton(r.getString(17039521), new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    P2pStateMachine.this.mSavedPeerConfig = new WifiP2pConfig();
                    P2pStateMachine.this.mSavedPeerConfig.deviceAddress = peerAddress;
                    P2pStateMachine.this.mSavedPeerConfig.wps.setup = 1;
                    P2pStateMachine.this.mSavedPeerConfig.wps.pin = pin;
                    P2pStateMachine.this.mWifiNative.p2pConnect(P2pStateMachine.this.mSavedPeerConfig, WifiP2pServiceImpl.FORM_GROUP.booleanValue());
                }
            }).setCancelable(false).create();
            dialog.getWindow().setType(2003);
            dialog.show();
        }

        private void processStatistics(Context mContext, int eventID, int choice) {
            JSONObject eventMsg = new JSONObject();
            try {
                eventMsg.put(HwWifiBigDataConstant.KEY_CHOICE, choice);
            } catch (JSONException e) {
                loge("processStatistics put error." + e);
            }
            Flog.bdReport(mContext, eventID, eventMsg);
        }

        private void notifyInvitationReceived() {
            if (!WifiP2pServiceImpl.this.autoAcceptConnection()) {
                Resources r = Resources.getSystem();
                final WpsInfo wps = this.mSavedPeerConfig.wps;
                View textEntryView = LayoutInflater.from(WifiP2pServiceImpl.this.mContext).inflate(17367317, null);
                ViewGroup group = (ViewGroup) textEntryView.findViewById(16908972);
                View row = LayoutInflater.from(WifiP2pServiceImpl.this.mContext).inflate(17367318, group, false);
                ((TextView) row.findViewById(16909079)).setText(String.format(r.getString(33685699), new Object[]{getDeviceName(this.mSavedPeerConfig.deviceAddress)}));
                ((TextView) row.findViewById(16909079)).setTextColor(-16777216);
                group.addView(row);
                final EditText pin = (EditText) textEntryView.findViewById(16909476);
                AlertDialog dialog = new Builder(WifiP2pServiceImpl.this.mContext, 33947691).setTitle(r.getString(33685701)).setView(textEntryView).setPositiveButton(r.getString(33685700), new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (wps.setup == 2) {
                            P2pStateMachine.this.mSavedPeerConfig.wps.pin = pin.getText().toString();
                        }
                        P2pStateMachine.this.logd(P2pStateMachine.this.getName() + " accept invitation " + P2pStateMachine.this.mSavedPeerConfig);
                        P2pStateMachine.this.sendMessage(WifiP2pServiceImpl.PEER_CONNECTION_USER_ACCEPT);
                        P2pStateMachine.this.processStatistics(WifiP2pServiceImpl.this.mContext, 300, 0);
                    }
                }).setNegativeButton(r.getString(17039360), new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        P2pStateMachine.this.logd(P2pStateMachine.this.getName() + " ignore connect");
                        P2pStateMachine.this.sendMessage(WifiP2pServiceImpl.PEER_CONNECTION_USER_REJECT);
                        P2pStateMachine.this.processStatistics(WifiP2pServiceImpl.this.mContext, 300, 1);
                    }
                }).setOnCancelListener(new OnCancelListener() {
                    public void onCancel(DialogInterface arg0) {
                        P2pStateMachine.this.logd(P2pStateMachine.this.getName() + " ignore connect");
                        P2pStateMachine.this.sendMessage(WifiP2pServiceImpl.PEER_CONNECTION_USER_REJECT);
                        P2pStateMachine.this.processStatistics(WifiP2pServiceImpl.this.mContext, 300, 1);
                    }
                }).create();
                dialog.setCanceledOnTouchOutside(false);
                switch (wps.setup) {
                    case 1:
                        logd("Shown pin section visible");
                        addRowToDialog(group, 17041259, wps.pin);
                        break;
                    case 2:
                        logd("Enter pin section visible");
                        textEntryView.findViewById(16908855).setVisibility(0);
                        break;
                }
                if ((r.getConfiguration().uiMode & 5) == 5) {
                    dialog.setOnKeyListener(new OnKeyListener() {
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
                LayoutParams attrs = dialog.getWindow().getAttributes();
                attrs.privateFlags = 16;
                dialog.getWindow().setAttributes(attrs);
                dialog.show();
            }
        }

        protected void updatePersistentNetworks(boolean reload) {
            if (reload) {
                this.mGroups.clear();
            }
            if (this.mWifiNative.p2pListNetworks(this.mGroups) || reload) {
                for (WifiP2pGroup group : this.mGroups.getGroupList()) {
                    if (WifiP2pServiceImpl.this.mThisDevice.deviceAddress.equals(group.getOwner().deviceAddress)) {
                        group.setOwner(WifiP2pServiceImpl.this.mThisDevice);
                    }
                }
                this.mWifiNative.saveConfig();
                sendP2pPersistentGroupsChangedBroadcast();
            }
        }

        private boolean isConfigInvalid(WifiP2pConfig config) {
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

        private void p2pConnectWithPinDisplay(WifiP2pConfig config) {
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
            WifiP2pServiceImpl.this.mIsInvite = false;
        }

        private boolean reinvokePersistentGroup(WifiP2pConfig config) {
            if (config == null) {
                Log.e(WifiP2pServiceImpl.TAG, "Illegal argument(s)");
                return false;
            }
            WifiP2pDevice dev = fetchCurrentDeviceDetails(config);
            if (dev == null) {
                Log.e(WifiP2pServiceImpl.TAG, "Invalid device");
                return false;
            }
            int netId;
            boolean join = dev.isGroupOwner();
            String ssid = this.mWifiNative.p2pGetSsid(dev.deviceAddress);
            logd("target ssid is " + ssid + " join:" + join);
            if (join && dev.isGroupLimit()) {
                logd("target device reaches group limit.");
                join = false;
            } else if (join) {
                netId = this.mGroups.getNetworkId(dev.deviceAddress, ssid);
                if (netId >= 0) {
                    return this.mWifiNative.p2pGroupAdd(netId);
                }
            }
            if (join || !dev.isDeviceLimit()) {
                if (!join && dev.isInvitationCapable()) {
                    netId = -2;
                    if (config.netId < 0) {
                        netId = this.mGroups.getNetworkId(dev.deviceAddress);
                    } else if (config.deviceAddress.equals(this.mGroups.getOwnerAddr(config.netId))) {
                        netId = config.netId;
                    }
                    if (netId < 0) {
                        netId = getNetworkIdFromClientList(dev.deviceAddress);
                    }
                    logd("netId related with " + dev.deviceAddress + " = " + netId);
                    if (netId >= 0) {
                        if (this.mWifiNative.p2pReinvoke(netId, dev.deviceAddress)) {
                            config.netId = netId;
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

        protected int getNetworkIdFromClientList(String deviceAddress) {
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

        private boolean removeClientFromList(int netId, String addr, boolean isRemovable) {
            StringBuilder modifiedClientList = new StringBuilder();
            String[] currentClientList = getClientList(netId);
            boolean isClientRemoved = false;
            if (currentClientList != null) {
                for (String client : currentClientList) {
                    if (client.equalsIgnoreCase(addr)) {
                        isClientRemoved = true;
                    } else {
                        modifiedClientList.append(" ");
                        modifiedClientList.append(client);
                    }
                }
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

        private void setWifiP2pInfoOnGroupFormation(InetAddress serverInetAddress) {
            this.mWifiP2pInfo.groupFormed = true;
            this.mWifiP2pInfo.isGroupOwner = this.mGroup.isGroupOwner();
            this.mWifiP2pInfo.groupOwnerAddress = serverInetAddress;
        }

        private void resetWifiP2pInfo() {
            this.mWifiP2pInfo.groupFormed = false;
            this.mWifiP2pInfo.isGroupOwner = false;
            this.mWifiP2pInfo.groupOwnerAddress = null;
        }

        private String getDeviceName(String deviceAddress) {
            WifiP2pDevice d = this.mPeers.get(deviceAddress);
            if (d != null) {
                return d.deviceName;
            }
            return deviceAddress;
        }

        private String getCustomDeviceName(String deviceName) {
            if (!SystemProperties.getBoolean("ro.config.hw_wifi_bt_name", false) || !TextUtils.isEmpty(deviceName)) {
                return deviceName;
            }
            StringBuilder sb = new StringBuilder();
            String uuidStr = UUID.randomUUID().toString();
            String marketing_name = SystemProperties.get("ro.config.marketing_name");
            if (TextUtils.isEmpty(marketing_name)) {
                sb.append("HUAWEI ").append(Build.PRODUCT).append("_").append(uuidStr.substring(24, 28).toUpperCase(Locale.US));
                deviceName = sb.toString();
            } else {
                sb.append(marketing_name).append("_").append(uuidStr.substring(24, 28).toUpperCase(Locale.US));
                deviceName = sb.toString();
            }
            Global.putString(WifiP2pServiceImpl.this.mContext.getContentResolver(), "wifi_p2p_device_name", deviceName);
            return deviceName;
        }

        private String getPersistedDeviceName() {
            String deviceName = getCustomDeviceName(Global.getString(WifiP2pServiceImpl.this.mContext.getContentResolver(), "wifi_p2p_device_name"));
            if (deviceName != null) {
                return deviceName;
            }
            deviceName = SystemProperties.get("ro.config.marketing_name");
            if (!TextUtils.isEmpty(deviceName)) {
                return deviceName;
            }
            String id = Secure.getString(WifiP2pServiceImpl.this.mContext.getContentResolver(), "android_id");
            if (id == null || id.length() <= 4 || (WifiP2pServiceImpl.IS_ATT ^ 1) == 0) {
                return Build.MODEL;
            }
            return Build.MODEL + "_" + id.substring(0, 4);
        }

        private boolean setAndPersistDeviceName(String devName) {
            if (devName == null) {
                return false;
            }
            if (this.mWifiNative.setDeviceName(devName)) {
                WifiP2pServiceImpl.this.mThisDevice.deviceName = devName;
                this.mWifiNative.setP2pSsidPostfix("-" + getSsidPostFix(WifiP2pServiceImpl.this.mThisDevice.deviceName));
                Global.putString(WifiP2pServiceImpl.this.mContext.getContentResolver(), "wifi_p2p_device_name", devName);
                sendThisDeviceChangedBroadcast();
                return true;
            }
            loge("Failed to set device name " + devName);
            return false;
        }

        private boolean setWfdInfo(WifiP2pWfdInfo wfdInfo) {
            boolean success;
            if (!wfdInfo.isWfdEnabled()) {
                success = this.mWifiNative.setWfdEnable(false);
            } else if (this.mWifiNative.setWfdEnable(true)) {
                success = this.mWifiNative.setWfdDeviceInfo(wfdInfo.getDeviceInfoHex());
            } else {
                success = false;
            }
            if (success) {
                WifiP2pServiceImpl.this.mThisDevice.wfdInfo = wfdInfo;
                sendThisDeviceChangedBroadcast();
                return true;
            }
            loge("Failed to set wfd properties");
            return false;
        }

        private void initializeP2pSettings() {
            WifiP2pServiceImpl.this.mThisDevice.deviceName = getPersistedDeviceName();
            this.mWifiNative.setP2pDeviceName(WifiP2pServiceImpl.this.mThisDevice.deviceName);
            this.mWifiNative.setP2pSsidPostfix("-" + getSsidPostFix(WifiP2pServiceImpl.this.mThisDevice.deviceName));
            this.mWifiNative.setP2pDeviceType(WifiP2pServiceImpl.this.mThisDevice.primaryDeviceType);
            this.mWifiNative.setConfigMethods("virtual_push_button physical_display keypad");
            WifiP2pServiceImpl.this.mThisDevice.deviceAddress = this.mWifiNative.p2pGetDeviceAddress();
            updateThisDevice(3);
            WifiP2pServiceImpl.this.mClientInfoList.clear();
            this.mWifiNative.p2pFlush();
            this.mWifiNative.p2pServiceFlush();
            WifiP2pServiceImpl.this.mServiceTransactionId = (byte) 0;
            WifiP2pServiceImpl.this.mServiceDiscReqId = null;
            WifiP2pServiceImpl.this.clearValidDeivceList();
            updatePersistentNetworks(WifiP2pServiceImpl.RELOAD.booleanValue());
        }

        private void updateThisDevice(int status) {
            WifiP2pServiceImpl.this.mThisDevice.status = status;
            sendThisDeviceChangedBroadcast();
        }

        private void handleGroupCreationFailure() {
            resetWifiP2pInfo();
            WifiP2pServiceImpl.this.mNetworkInfo.setDetailedState(DetailedState.FAILED, null, null);
            sendP2pConnectionChangedBroadcast();
            boolean peersChanged = this.mPeers.remove(this.mPeersLostDuringConnection);
            if (!(TextUtils.isEmpty(this.mSavedPeerConfig.deviceAddress) || this.mPeers.remove(this.mSavedPeerConfig.deviceAddress) == null)) {
                peersChanged = true;
            }
            if (peersChanged) {
                sendPeersChangedBroadcast();
            }
            this.mPeersLostDuringConnection.clear();
            WifiP2pServiceImpl.this.mServiceDiscReqId = null;
            sendMessage(139265);
        }

        private void handleGroupRemoved() {
            if (this.mGroup.isGroupOwner()) {
                if (WifiP2pServiceHisiExt.hisiWifiEnabled()) {
                    stopDhcpServer(HwWifiCHRStateManager.MAIN_IFACE);
                } else {
                    stopDhcpServer(this.mGroup.getInterface());
                }
            } else if (WifiP2pServiceImpl.this.getMagicLinkDeviceFlag()) {
                WifiP2pServiceImpl.this.setmMagicLinkDeviceFlag(false);
            } else {
                logd("stop IpManager");
                WifiP2pServiceImpl.this.stopIpManager();
                try {
                    WifiP2pServiceImpl.this.mNwService.removeInterfaceFromLocalNetwork(this.mGroup.getInterface());
                } catch (RemoteException e) {
                    loge("Failed to remove iface from local network " + e);
                }
            }
            try {
                if (WifiP2pServiceHisiExt.hisiWifiEnabled()) {
                    WifiP2pServiceImpl.this.mNwService.clearInterfaceAddresses(HwWifiCHRStateManager.MAIN_IFACE);
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
            WifiP2pServiceImpl.this.mServiceDiscReqId = null;
            if (WifiP2pServiceImpl.this.mTemporarilyDisconnectedWifi) {
                if (WifiP2pServiceImpl.this.mWifiChannel != null) {
                    WifiP2pServiceImpl.this.mWifiChannel.sendMessage(WifiP2pServiceImpl.DISCONNECT_WIFI_REQUEST, 0);
                } else {
                    loge("handleGroupRemoved(): WifiChannel is null");
                }
                WifiP2pServiceImpl.this.mTemporarilyDisconnectedWifi = false;
            }
            WifiP2pServiceImpl.this.notifyRptGroupRemoved();
        }

        protected void replyToMessage(Message msg, int what) {
            if (msg.replyTo != null) {
                Message dstMsg = obtainMessage(msg);
                dstMsg.what = what;
                WifiP2pServiceImpl.this.mReplyChannel.replyToMessage(msg, dstMsg);
            }
        }

        private void replyToMessage(Message msg, int what, int arg1) {
            if (msg.replyTo != null) {
                Message dstMsg = obtainMessage(msg);
                dstMsg.what = what;
                dstMsg.arg1 = arg1;
                WifiP2pServiceImpl.this.mReplyChannel.replyToMessage(msg, dstMsg);
            }
        }

        private void replyToMessage(Message msg, int what, Object obj) {
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

        protected void logd(String s) {
            Slog.d(WifiP2pServiceImpl.TAG, s);
        }

        protected void loge(String s) {
            Slog.e(WifiP2pServiceImpl.TAG, s);
        }

        private boolean updateSupplicantServiceRequest() {
            clearSupplicantServiceRequest();
            StringBuffer sb = new StringBuffer();
            for (ClientInfo c : WifiP2pServiceImpl.this.mClientInfoList.values()) {
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
            WifiP2pServiceImpl.this.mServiceDiscReqId = this.mWifiNative.p2pServDiscReq("00:00:00:00:00:00", sb.toString());
            if (WifiP2pServiceImpl.this.mServiceDiscReqId == null) {
                return false;
            }
            return true;
        }

        private void clearSupplicantServiceRequest() {
            if (WifiP2pServiceImpl.this.mServiceDiscReqId != null) {
                this.mWifiNative.p2pServDiscCancelReq(WifiP2pServiceImpl.this.mServiceDiscReqId);
                WifiP2pServiceImpl.this.mServiceDiscReqId = null;
            }
        }

        private boolean addServiceRequest(Messenger m, WifiP2pServiceRequest req) {
            if (m == null || req == null) {
                Log.e(WifiP2pServiceImpl.TAG, "Illegal argument(s)");
                return false;
            }
            clearClientDeadChannels();
            ClientInfo clientInfo = getClientInfo(m, true);
            if (clientInfo == null) {
                return false;
            }
            WifiP2pServiceImpl wifiP2pServiceImpl = WifiP2pServiceImpl.this;
            wifiP2pServiceImpl.mServiceTransactionId = (byte) (wifiP2pServiceImpl.mServiceTransactionId + 1);
            if (WifiP2pServiceImpl.this.mServiceTransactionId == (byte) 0) {
                wifiP2pServiceImpl = WifiP2pServiceImpl.this;
                wifiP2pServiceImpl.mServiceTransactionId = (byte) (wifiP2pServiceImpl.mServiceTransactionId + 1);
            }
            req.setTransactionId(WifiP2pServiceImpl.this.mServiceTransactionId);
            clientInfo.mReqList.put(WifiP2pServiceImpl.this.mServiceTransactionId, req);
            if (WifiP2pServiceImpl.this.mServiceDiscReqId == null) {
                return true;
            }
            return updateSupplicantServiceRequest();
        }

        private void removeServiceRequest(Messenger m, WifiP2pServiceRequest req) {
            if (m == null || req == null) {
                Log.e(WifiP2pServiceImpl.TAG, "Illegal argument(s)");
            }
            ClientInfo clientInfo = getClientInfo(m, false);
            if (clientInfo != null) {
                boolean removed = false;
                for (int i = 0; i < clientInfo.mReqList.size(); i++) {
                    if (req.equals(clientInfo.mReqList.valueAt(i))) {
                        removed = true;
                        clientInfo.mReqList.removeAt(i);
                        break;
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

        private void clearServiceRequests(Messenger m) {
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

        private boolean addLocalService(Messenger m, WifiP2pServiceInfo servInfo) {
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

        private void removeLocalService(Messenger m, WifiP2pServiceInfo servInfo) {
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

        private void clearLocalServices(Messenger m) {
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

        private void sendServiceResponse(WifiP2pServiceResponse resp) {
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
                    continue;
                }
            }
        }

        private void clearClientDeadChannels() {
            ArrayList<Messenger> deadClients = new ArrayList();
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
            for (Messenger m : deadClients) {
                clearClientInfo(m);
            }
        }

        private ClientInfo getClientInfo(Messenger m, boolean createIfNotExist) {
            ClientInfo clientInfo = (ClientInfo) WifiP2pServiceImpl.this.mClientInfoList.get(m);
            if (clientInfo != null || !createIfNotExist) {
                return clientInfo;
            }
            logd("add a new client");
            clientInfo = new ClientInfo(WifiP2pServiceImpl.this, m, null);
            WifiP2pServiceImpl.this.mClientInfoList.put(m, clientInfo);
            return clientInfo;
        }

        private void enableBTCoex() {
            if (this.mIsBTCoexDisabled) {
                this.mWifiInjector.getWifiNative().setBluetoothCoexistenceMode(2);
                this.mIsBTCoexDisabled = false;
            }
        }

        private String getSsidPostFix(String deviceName) {
            String ssidPostFix = deviceName;
            if (deviceName != null) {
                byte[] ssidPostFixBytes = deviceName.getBytes();
                while (ssidPostFixBytes.length > 22) {
                    ssidPostFix = ssidPostFix.substring(0, ssidPostFix.length() - 1);
                    ssidPostFixBytes = ssidPostFix.getBytes();
                }
            }
            return ssidPostFix;
        }

        private WifiP2pDeviceList getPeers(Bundle pkg, int uid) {
            String pkgName = pkg.getString("android.net.wifi.p2p.CALLING_PACKAGE");
            boolean scanPermission = false;
            if (this.mWifiInjector == null) {
                this.mWifiInjector = WifiInjector.getInstance();
            }
            try {
                scanPermission = this.mWifiInjector.getWifiPermissionsUtil().canAccessScanResults(pkgName, uid, 26);
            } catch (SecurityException e) {
                Log.e(WifiP2pServiceImpl.TAG, "Security Exception, cannot access peer list");
            }
            if (scanPermission) {
                return new WifiP2pDeviceList(this.mPeers);
            }
            return new WifiP2pDeviceList();
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

    static {
        boolean z;
        boolean z2 = false;
        if (Log.HWLog) {
            z = true;
        } else if (Log.HWModuleLog) {
            z = Log.isLoggable(TAG, 3);
        } else {
            z = false;
        }
        HWDBG = z;
        if ("07".equals(SystemProperties.get("ro.config.hw_opta", HwWifiCHRStateManager.TYPE_AP_VENDOR))) {
            z2 = "840".equals(SystemProperties.get("ro.config.hw_optb", HwWifiCHRStateManager.TYPE_AP_VENDOR));
        }
        IS_ATT = z2;
    }

    public WifiP2pServiceImpl(Context context) {
        this.mContext = context;
        this.mNetworkInfo = new NetworkInfo(13, 0, NETWORKTYPE, "");
        this.mP2pSupported = this.mContext.getPackageManager().hasSystemFeature("android.hardware.wifi.direct");
        this.mThisDevice.primaryDeviceType = this.mContext.getResources().getString(17039815);
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
        this.mWiFiCHRManager = HwWifiServiceFactory.getHwWifiCHRStateManager();
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
        this.mNwService = Stub.asInterface(ServiceManager.getService("network_management"));
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

    private void stopIpManager() {
        if (this.mIpManager != null) {
            this.mIpManager.shutdown();
            this.mIpManager = null;
        }
        this.mDhcpResults = null;
    }

    private void startIpManager(String ifname) {
        stopIpManager();
        this.mIpManager = new IpManager(this.mContext, ifname, new Callback() {
            public void onPreDhcpAction() {
                WifiP2pServiceImpl.this.mP2pStateMachine.sendMessage(WifiP2pServiceImpl.IPM_PRE_DHCP_ACTION);
            }

            public void onPostDhcpAction() {
                WifiP2pServiceImpl.this.mP2pStateMachine.sendMessage(WifiP2pServiceImpl.IPM_POST_DHCP_ACTION);
            }

            public void onNewDhcpResults(DhcpResults dhcpResults) {
                WifiP2pServiceImpl.this.mP2pStateMachine.sendMessage(WifiP2pServiceImpl.IPM_DHCP_RESULTS, dhcpResults);
            }

            public void onProvisioningSuccess(LinkProperties newLp) {
                WifiP2pServiceImpl.this.mP2pStateMachine.sendMessage(WifiP2pServiceImpl.IPM_PROVISIONING_SUCCESS);
            }

            public void onProvisioningFailure(LinkProperties newLp) {
                WifiP2pServiceImpl.this.mP2pStateMachine.sendMessage(WifiP2pServiceImpl.IPM_PROVISIONING_FAILURE);
            }
        }, this.mNwService);
        IpManager ipManager = this.mIpManager;
        this.mIpManager.startProvisioning(IpManager.buildProvisioningConfiguration().withoutIPv6().withoutIpReachabilityMonitor().withPreDhcpAction(30000).withProvisioningTimeoutMs(36000).build());
    }

    public Messenger getMessenger() {
        enforceAccessPermission();
        enforceChangePermission();
        return new Messenger(this.mClientHandler);
    }

    public Messenger getP2pStateMachineMessenger() {
        enforceConnectivityInternalOrLocationHardwarePermission();
        enforceAccessPermission();
        enforceChangePermission();
        return new Messenger(this.mP2pStateMachine.getHandler());
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

    private boolean getWfdPermission(int uid) {
        if (this.mWifiInjector == null) {
            this.mWifiInjector = WifiInjector.getInstance();
        }
        return this.mWifiInjector.getWifiPermissionsWrapper().getUidPermission("android.permission.CONFIGURE_WIFI_DISPLAY", uid) != -1;
    }

    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
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
        pw.println();
        IpManager ipManager = this.mIpManager;
        if (ipManager != null) {
            pw.println("mIpManager:");
            ipManager.dump(fd, pw, args);
        }
    }

    public void handleClientConnect(WifiP2pGroup group) {
    }

    public void handleClientDisconnect(WifiP2pGroup group) {
    }

    public void notifyRptGroupRemoved() {
    }
}
