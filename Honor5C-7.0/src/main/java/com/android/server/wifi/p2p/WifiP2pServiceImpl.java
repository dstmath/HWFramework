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
import android.net.wifi.WifiSsid;
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
import android.os.Handler;
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
import com.android.internal.util.AsyncChannel;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.wifi.HwWifiBigDataConstant;
import com.android.server.wifi.HwWifiCHRStateManager;
import com.android.server.wifi.HwWifiServiceFactory;
import com.android.server.wifi.WifiLastResortWatchdog;
import com.android.server.wifi.WifiMonitor;
import com.android.server.wifi.WifiNative;
import com.android.server.wifi.WifiP2pServiceHisiExt;
import com.android.server.wifi.WifiStateMachine;
import com.google.protobuf.nano.Extension;
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
    private static final Boolean FORM_GROUP = null;
    public static final int GROUP_CREATING_TIMED_OUT = 143361;
    private static final int GROUP_CREATING_WAIT_TIME_MS = 120000;
    private static final int GROUP_IDLE_TIME_S = 10;
    private static final boolean HWDBG = false;
    private static final int IPM_DHCP_RESULTS = 143392;
    private static final int IPM_POST_DHCP_ACTION = 143391;
    private static final int IPM_PRE_DHCP_ACTION = 143390;
    private static final int IPM_PROVISIONING_FAILURE = 143394;
    private static final int IPM_PROVISIONING_SUCCESS = 143393;
    private static final Boolean JOIN_GROUP = null;
    private static final String NETWORKTYPE = "WIFI_P2P";
    private static final Boolean NO_RELOAD = null;
    static final int P2P_BLUETOOTH_COEXISTENCE_MODE_DISABLED = 1;
    static final int P2P_BLUETOOTH_COEXISTENCE_MODE_SENSE = 2;
    public static final int P2P_CONNECTION_CHANGED = 143371;
    private static final int P2P_DEVICE_OF_MIRACAST = 7;
    private static final int PEER_CONNECTION_USER_ACCEPT = 143362;
    private static final int PEER_CONNECTION_USER_REJECT = 143363;
    private static final Boolean RELOAD = null;
    private static final String SERVER_ADDRESS = "192.168.49.1";
    private static final String SERVER_ADDRESS_WIFI_BRIDGE = "192.168.43.1";
    public static final int SET_MIRACAST_MODE = 143374;
    public static final int SHOW_USER_CONFIRM_DIALOG = 143410;
    private static final String TAG = "WifiP2pService";
    private static int mDisableP2pTimeoutIndex;
    private static int mGroupCreatingTimeoutIndex;
    private boolean mAutonomousGroup;
    private ClientHandler mClientHandler;
    private HashMap<Messenger, ClientInfo> mClientInfoList;
    private Context mContext;
    private boolean mCreateWifiBridge;
    private DhcpResults mDhcpResults;
    private boolean mDiscoveryBlocked;
    private boolean mDiscoveryPostponed;
    private boolean mDiscoveryStarted;
    private IpManager mIpManager;
    private boolean mIsInvite;
    private boolean mJoinExistingGroup;
    private NetworkInfo mNetworkInfo;
    INetworkManagementService mNwService;
    private String mP2pServerAddress;
    protected P2pStateMachine mP2pStateMachine;
    private final boolean mP2pSupported;
    private AsyncChannel mReplyChannel;
    private String mServiceDiscReqId;
    private byte mServiceTransactionId;
    private boolean mTemporarilyDisconnectedWifi;
    private WifiP2pDevice mThisDevice;
    private HwWifiCHRStateManager mWiFiCHRManager;
    private AsyncChannel mWifiChannel;
    WifiP2pServiceHisiExt mWifiP2pServiceHisiExt;

    private class ClientHandler extends Handler {
        ClientHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
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
                    WifiP2pServiceImpl.this.mP2pStateMachine.sendMessage(Message.obtain(msg));
                default:
                    WifiP2pServiceImpl.this.handleClientHwMessage(Message.obtain(msg));
            }
        }
    }

    private class ClientInfo {
        private Messenger mMessenger;
        private SparseArray<WifiP2pServiceRequest> mReqList;
        private List<WifiP2pServiceInfo> mServList;

        private ClientInfo(Messenger m) {
            this.mMessenger = m;
            this.mReqList = new SparseArray();
            this.mServList = new ArrayList();
        }
    }

    protected class P2pStateMachine extends StateMachine {
        private AfterUserAuthorizingJoinState mAfterUserAuthorizingJoinState;
        private DefaultState mDefaultState;
        private FrequencyConflictState mFrequencyConflictState;
        private WifiP2pGroup mGroup;
        private GroupCreatedState mGroupCreatedState;
        private GroupCreatingState mGroupCreatingState;
        protected GroupNegotiationState mGroupNegotiationState;
        protected final WifiP2pGroupList mGroups;
        private InactiveState mInactiveState;
        private boolean mIsBTCoexDisabled;
        private OngoingGroupRemovalState mOngoingGroupRemovalState;
        private P2pDisabledState mP2pDisabledState;
        private P2pDisablingState mP2pDisablingState;
        private P2pEnabledState mP2pEnabledState;
        private P2pEnablingState mP2pEnablingState;
        private P2pNotSupportedState mP2pNotSupportedState;
        protected final WifiP2pDeviceList mPeers;
        private final WifiP2pDeviceList mPeersLostDuringConnection;
        private boolean mPendingReformGroupIndication;
        private ProvisionDiscoveryState mProvisionDiscoveryState;
        protected WifiP2pConfig mSavedPeerConfig;
        private UserAuthorizingInviteRequestState mUserAuthorizingInviteRequestState;
        private UserAuthorizingJoinState mUserAuthorizingJoinState;
        private UserAuthorizingNegotiationRequestState mUserAuthorizingNegotiationRequestState;
        private WifiMonitor mWifiMonitor;
        protected WifiNative mWifiNative;
        private final WifiP2pInfo mWifiP2pInfo;

        /* renamed from: com.android.server.wifi.p2p.WifiP2pServiceImpl.P2pStateMachine.2 */
        class AnonymousClass2 implements OnClickListener {
            final /* synthetic */ String val$tempDevAddress;
            final /* synthetic */ String val$tempPin;

            AnonymousClass2(String val$tempDevAddress, String val$tempPin) {
                this.val$tempDevAddress = val$tempDevAddress;
                this.val$tempPin = val$tempPin;
            }

            public void onClick(DialogInterface dialog, int which) {
                P2pStateMachine.this.mSavedPeerConfig = new WifiP2pConfig();
                P2pStateMachine.this.mSavedPeerConfig.deviceAddress = this.val$tempDevAddress;
                P2pStateMachine.this.mSavedPeerConfig.wps.setup = WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_DISABLED;
                P2pStateMachine.this.mSavedPeerConfig.wps.pin = this.val$tempPin;
                P2pStateMachine.this.mWifiNative.p2pConnect(P2pStateMachine.this.mSavedPeerConfig, WifiP2pServiceImpl.FORM_GROUP.booleanValue());
            }
        }

        /* renamed from: com.android.server.wifi.p2p.WifiP2pServiceImpl.P2pStateMachine.3 */
        class AnonymousClass3 implements OnClickListener {
            final /* synthetic */ EditText val$pin;
            final /* synthetic */ WpsInfo val$wps;

            AnonymousClass3(WpsInfo val$wps, EditText val$pin) {
                this.val$wps = val$wps;
                this.val$pin = val$pin;
            }

            public void onClick(DialogInterface dialog, int which) {
                if (this.val$wps.setup == WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_SENSE) {
                    P2pStateMachine.this.mSavedPeerConfig.wps.pin = this.val$pin.getText().toString();
                }
                P2pStateMachine.this.logd(P2pStateMachine.this.getName() + " accept invitation " + P2pStateMachine.this.mSavedPeerConfig);
                P2pStateMachine.this.sendMessage(WifiP2pServiceImpl.PEER_CONNECTION_USER_ACCEPT);
                P2pStateMachine.this.processStatistics(WifiP2pServiceImpl.this.mContext, 300, WifiP2pServiceImpl.DISABLED);
            }
        }

        class AfterUserAuthorizingJoinState extends State {
            AfterUserAuthorizingJoinState() {
            }

            public void enter() {
                P2pStateMachine.this.logd(getName());
            }

            public boolean processMessage(Message message) {
                P2pStateMachine.this.logd(getName() + message.toString());
                return WifiP2pServiceImpl.HWDBG;
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
                    return WifiP2pServiceImpl.DBG;
                }
                switch (message.what) {
                    case 69632:
                        if (message.arg1 != 0) {
                            P2pStateMachine.this.loge("Full connection failure, error = " + message.arg1);
                            WifiP2pServiceImpl.this.mWifiChannel = null;
                            break;
                        }
                        P2pStateMachine.this.logd("Full connection with WifiStateMachine established");
                        WifiP2pServiceImpl.this.mWifiChannel = (AsyncChannel) message.obj;
                        break;
                    case 69633:
                        new AsyncChannel().connect(WifiP2pServiceImpl.this.mContext, P2pStateMachine.this.getHandler(), message.replyTo);
                        break;
                    case 69636:
                        if (message.arg1 == WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_SENSE) {
                            P2pStateMachine.this.loge("Send failed, client connection lost");
                        } else {
                            P2pStateMachine.this.loge("Client connection lost with reason: " + message.arg1);
                        }
                        WifiP2pServiceImpl.this.mWifiChannel = null;
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
                    case WifiMonitor.SUP_CONNECTION_EVENT /*147457*/:
                    case WifiMonitor.SUP_DISCONNECTION_EVENT /*147458*/:
                    case WifiMonitor.SCAN_RESULTS_EVENT /*147461*/:
                    case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT /*147462*/:
                    case WifiMonitor.AUTHENTICATION_FAILURE_EVENT /*147463*/:
                    case WifiMonitor.WPS_SUCCESS_EVENT /*147464*/:
                    case WifiMonitor.WPS_FAIL_EVENT /*147465*/:
                    case WifiMonitor.WPS_OVERLAP_EVENT /*147466*/:
                    case WifiMonitor.WPS_TIMEOUT_EVENT /*147467*/:
                    case WifiMonitor.P2P_DEVICE_FOUND_EVENT /*147477*/:
                    case WifiMonitor.P2P_DEVICE_LOST_EVENT /*147478*/:
                    case WifiMonitor.P2P_GROUP_FORMATION_FAILURE_EVENT /*147484*/:
                    case WifiMonitor.P2P_GROUP_REMOVED_EVENT /*147486*/:
                    case WifiMonitor.P2P_INVITATION_RESULT_EVENT /*147488*/:
                    case WifiMonitor.P2P_FIND_STOPPED_EVENT /*147493*/:
                    case WifiMonitor.P2P_SERV_DISC_RESP_EVENT /*147494*/:
                    case WifiMonitor.P2P_PROV_DISC_FAILURE_EVENT /*147495*/:
                        break;
                    case WifiStateMachine.CMD_DISABLE_P2P_REQ /*131204*/:
                        WifiP2pServiceImpl.this.mWifiChannel.sendMessage(WifiStateMachine.CMD_DISABLE_P2P_RSP);
                        break;
                    case 139265:
                        P2pStateMachine.this.replyToMessage(message, 139266, (int) WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_SENSE);
                        break;
                    case 139268:
                        P2pStateMachine.this.replyToMessage(message, 139269, (int) WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_SENSE);
                        break;
                    case 139271:
                        P2pStateMachine.this.replyToMessage(message, 139272, (int) WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_SENSE);
                        break;
                    case 139274:
                        P2pStateMachine.this.replyToMessage(message, 139275, (int) WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_SENSE);
                        break;
                    case 139277:
                        P2pStateMachine.this.replyToMessage(message, 139278, (int) WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_SENSE);
                        break;
                    case 139280:
                        P2pStateMachine.this.replyToMessage(message, 139281, (int) WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_SENSE);
                        if (WifiP2pServiceImpl.this.getWifiRepeaterEnabled()) {
                            WifiP2pServiceImpl.this.stopWifiRepeater(P2pStateMachine.this.mGroup);
                            break;
                        }
                        break;
                    case 139283:
                        P2pStateMachine.this.replyToMessage(message, 139284, (Object) new WifiP2pDeviceList(P2pStateMachine.this.mPeers));
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
                        P2pStateMachine.this.replyToMessage(message, 139293, (int) WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_SENSE);
                        break;
                    case 139295:
                        P2pStateMachine.this.replyToMessage(message, 139296, (int) WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_SENSE);
                        break;
                    case 139298:
                        P2pStateMachine.this.replyToMessage(message, 139299, (int) WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_SENSE);
                        break;
                    case 139301:
                        P2pStateMachine.this.replyToMessage(message, 139302, (int) WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_SENSE);
                        break;
                    case 139304:
                        P2pStateMachine.this.replyToMessage(message, 139305, (int) WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_SENSE);
                        break;
                    case 139307:
                        P2pStateMachine.this.replyToMessage(message, 139308, (int) WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_SENSE);
                        break;
                    case 139310:
                        P2pStateMachine.this.replyToMessage(message, 139311, (int) WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_SENSE);
                        break;
                    case 139315:
                        P2pStateMachine.this.replyToMessage(message, 139316, (int) WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_SENSE);
                        break;
                    case 139318:
                        P2pStateMachine.this.replyToMessage(message, 139318, (int) WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_SENSE);
                        break;
                    case 139321:
                        P2pStateMachine.this.replyToMessage(message, 139322, (Object) new WifiP2pGroupList(P2pStateMachine.this.mGroups, null));
                        break;
                    case 139323:
                        P2pStateMachine.this.replyToMessage(message, 139324, (int) WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_SENSE);
                        break;
                    case 139326:
                        P2pStateMachine.this.replyToMessage(message, 139327, (int) WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_SENSE);
                        break;
                    case 139339:
                    case 139340:
                        P2pStateMachine.this.replyToMessage(message, 139341, null);
                        break;
                    case 139342:
                    case 139343:
                        P2pStateMachine.this.replyToMessage(message, 139345, (int) WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_SENSE);
                        break;
                    case WifiP2pServiceImpl.BLOCK_DISCOVERY /*143375*/:
                        boolean z;
                        WifiP2pServiceImpl wifiP2pServiceImpl = WifiP2pServiceImpl.this;
                        if (message.arg1 == WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_DISABLED) {
                            z = WifiP2pServiceImpl.DBG;
                        } else {
                            z = WifiP2pServiceImpl.HWDBG;
                        }
                        wifiP2pServiceImpl.mDiscoveryBlocked = z;
                        WifiP2pServiceImpl.this.mDiscoveryPostponed = WifiP2pServiceImpl.HWDBG;
                        if (WifiP2pServiceImpl.this.mDiscoveryBlocked) {
                            try {
                                message.obj.sendMessage(message.arg2);
                                break;
                            } catch (Exception e) {
                                P2pStateMachine.this.loge("unable to send BLOCK_DISCOVERY response: " + e);
                                break;
                            }
                        }
                        break;
                    case WifiP2pServiceImpl.SHOW_USER_CONFIRM_DIALOG /*143410*/:
                        WifiP2pServiceImpl.this.mWifiP2pServiceHisiExt.showP2pEanbleDialog();
                        break;
                    case WifiMonitor.P2P_GROUP_STARTED_EVENT /*147485*/:
                        P2pStateMachine.this.mGroup = (WifiP2pGroup) message.obj;
                        P2pStateMachine.this.loge("Unexpected group creation, remove " + P2pStateMachine.this.mGroup);
                        P2pStateMachine.this.mWifiNative.p2pGroupRemove(P2pStateMachine.this.mGroup.getInterface());
                        break;
                    default:
                        return WifiP2pServiceImpl.this.handleDefaultStateMessage(message);
                }
                return WifiP2pServiceImpl.DBG;
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
                Builder builder = new Builder(WifiP2pServiceImpl.this.mContext);
                Object[] objArr = new Object[WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_DISABLED];
                objArr[WifiP2pServiceImpl.DISABLED] = P2pStateMachine.this.getDeviceName(P2pStateMachine.this.mSavedPeerConfig.deviceAddress);
                AlertDialog dialog = builder.setMessage(r.getString(17040349, objArr)).setPositiveButton(r.getString(17040383), new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        P2pStateMachine.this.sendMessage(WifiP2pServiceImpl.DROP_WIFI_USER_ACCEPT);
                    }
                }).setNegativeButton(r.getString(17040342), new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        P2pStateMachine.this.sendMessage(WifiP2pServiceImpl.DROP_WIFI_USER_REJECT);
                    }
                }).setOnCancelListener(new OnCancelListener() {
                    public void onCancel(DialogInterface arg0) {
                        P2pStateMachine.this.sendMessage(WifiP2pServiceImpl.DROP_WIFI_USER_REJECT);
                    }
                }).create();
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
                        WifiP2pServiceImpl.this.mWifiChannel.sendMessage(WifiP2pServiceImpl.DISCONNECT_WIFI_REQUEST, WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_DISABLED);
                        WifiP2pServiceImpl.this.mTemporarilyDisconnectedWifi = WifiP2pServiceImpl.DBG;
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
                    case WifiMonitor.P2P_GO_NEGOTIATION_SUCCESS_EVENT /*147481*/:
                    case WifiMonitor.P2P_GROUP_FORMATION_SUCCESS_EVENT /*147483*/:
                        P2pStateMachine.this.loge(getName() + "group sucess during freq conflict!");
                        break;
                    case WifiMonitor.P2P_GO_NEGOTIATION_FAILURE_EVENT /*147482*/:
                    case WifiMonitor.P2P_GROUP_FORMATION_FAILURE_EVENT /*147484*/:
                    case WifiMonitor.P2P_GROUP_REMOVED_EVENT /*147486*/:
                        break;
                    case WifiMonitor.P2P_GROUP_STARTED_EVENT /*147485*/:
                        P2pStateMachine.this.loge(getName() + "group started after freq conflict, handle anyway");
                        P2pStateMachine.this.deferMessage(message);
                        P2pStateMachine.this.transitionTo(P2pStateMachine.this.mGroupNegotiationState);
                        break;
                    default:
                        return WifiP2pServiceImpl.HWDBG;
                }
                return WifiP2pServiceImpl.DBG;
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
                boolean remove = WifiP2pServiceImpl.DBG;
                if (P2pStateMachine.this.mWifiNative.p2pGroupRemove(P2pStateMachine.this.mGroup.getInterface())) {
                    Slog.d(WifiP2pServiceImpl.TAG, "Removed P2P group successfully");
                    P2pStateMachine.this.transitionTo(P2pStateMachine.this.mOngoingGroupRemovalState);
                } else {
                    Slog.d(WifiP2pServiceImpl.TAG, "Failed to remove the P2P group");
                    P2pStateMachine.this.handleGroupRemoved();
                    P2pStateMachine.this.transitionTo(P2pStateMachine.this.mInactiveState);
                    remove = WifiP2pServiceImpl.HWDBG;
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
                    P2pStateMachine.this.mPendingReformGroupIndication = WifiP2pServiceImpl.HWDBG;
                    handlP2pGroupRestart();
                } else {
                    P2pStateMachine.this.mSavedPeerConfig.invalidate();
                    WifiP2pServiceImpl.this.mNetworkInfo.setDetailedState(DetailedState.CONNECTED, null, null);
                    if (WifiP2pServiceHisiExt.hisiWifiEnabled()) {
                        WifiP2pServiceImpl.this.mWifiP2pServiceHisiExt.sendP2pNetworkChangedBroadcast();
                    }
                    P2pStateMachine.this.updateThisDevice(WifiP2pServiceImpl.DISABLED);
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
            }

            public boolean processMessage(Message message) {
                P2pStateMachine p2pStateMachine = P2pStateMachine.this;
                int i = message.what;
                i = message.arg1;
                r0.logd(getName() + "when=" + message.getWhen() + " what=" + r0 + " arg1=" + r0 + " arg2=" + message.arg2);
                if (WifiP2pServiceImpl.this.processMessageForP2pCollision(message, this)) {
                    return WifiP2pServiceImpl.DBG;
                }
                WifiNative wifiNative;
                P2pStateMachine p2pStateMachine2;
                int i2;
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
                            wifiNative = P2pStateMachine.this.mWifiNative;
                            p2pStateMachine2 = P2pStateMachine.this;
                            if (!r0.p2pInvite(r0.mGroup, config.deviceAddress)) {
                                P2pStateMachine.this.replyToMessage(message, 139272, WifiP2pServiceImpl.DISABLED);
                                break;
                            }
                            P2pStateMachine.this.mPeers.updateStatus(config.deviceAddress, WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_DISABLED);
                            P2pStateMachine.this.sendPeersChangedBroadcast();
                            P2pStateMachine.this.replyToMessage(message, 139273);
                            break;
                        }
                        P2pStateMachine.this.loge("Dropping connect requeset " + config);
                        P2pStateMachine.this.replyToMessage(message, 139272);
                        break;
                    case 139280:
                        P2pStateMachine.this.logd(getName() + " remove group");
                        P2pStateMachine.this.enableBTCoex();
                        if (P2pStateMachine.this.mWifiNative.p2pGroupRemove(P2pStateMachine.this.mGroup.getInterface())) {
                            P2pStateMachine.this.transitionTo(P2pStateMachine.this.mOngoingGroupRemovalState);
                            P2pStateMachine.this.replyToMessage(message, 139282);
                        } else {
                            P2pStateMachine.this.handleGroupRemoved();
                            P2pStateMachine.this.transitionTo(P2pStateMachine.this.mInactiveState);
                            P2pStateMachine.this.replyToMessage(message, 139281, WifiP2pServiceImpl.DISABLED);
                        }
                        if (WifiP2pServiceImpl.this.getWifiRepeaterEnabled()) {
                            WifiP2pServiceImpl.this.stopWifiRepeater(P2pStateMachine.this.mGroup);
                            break;
                        }
                        break;
                    case 139326:
                        WpsInfo wps = message.obj;
                        if (wps != null) {
                            boolean ret = WifiP2pServiceImpl.DBG;
                            if (wps.setup == 0) {
                                ret = P2pStateMachine.this.mWifiNative.startWpsPbc(P2pStateMachine.this.mGroup.getInterface(), null);
                            } else if (wps.pin == null) {
                                String pin = P2pStateMachine.this.mWifiNative.startWpsPinDisplay(P2pStateMachine.this.mGroup.getInterface());
                                try {
                                    Integer.parseInt(pin);
                                    P2pStateMachine.this.notifyInvitationSent(pin, WifiLastResortWatchdog.BSSID_ANY);
                                } catch (NumberFormatException e) {
                                    ret = WifiP2pServiceImpl.HWDBG;
                                }
                            } else {
                                wifiNative = P2pStateMachine.this.mWifiNative;
                                p2pStateMachine2 = P2pStateMachine.this;
                                ret = r0.startWpsPinKeypad(r0.mGroup.getInterface(), wps.pin);
                            }
                            p2pStateMachine2 = P2pStateMachine.this;
                            if (ret) {
                                i2 = 139328;
                            } else {
                                i2 = 139327;
                            }
                            p2pStateMachine2.replyToMessage(message, i2);
                            break;
                        }
                        P2pStateMachine.this.replyToMessage(message, 139327);
                        break;
                    case WifiP2pServiceImpl.IPM_PRE_DHCP_ACTION /*143390*/:
                        P2pStateMachine.this.mWifiNative.setP2pPowerSave(P2pStateMachine.this.mGroup.getInterface(), WifiP2pServiceImpl.HWDBG);
                        WifiP2pServiceImpl.this.mIpManager.completedPreDhcpAction();
                        break;
                    case WifiP2pServiceImpl.IPM_POST_DHCP_ACTION /*143391*/:
                        P2pStateMachine.this.mWifiNative.setP2pPowerSave(P2pStateMachine.this.mGroup.getInterface(), WifiP2pServiceImpl.DBG);
                        break;
                    case WifiP2pServiceImpl.IPM_DHCP_RESULTS /*143392*/:
                        WifiP2pServiceImpl.this.mDhcpResults = (DhcpResults) message.obj;
                        break;
                    case WifiP2pServiceImpl.IPM_PROVISIONING_SUCCESS /*143393*/:
                        P2pStateMachine.this.logd("mDhcpResults: " + WifiP2pServiceImpl.this.mDhcpResults);
                        P2pStateMachine.this.enableBTCoex();
                        P2pStateMachine.this.setWifiP2pInfoOnGroupFormation(WifiP2pServiceImpl.this.mDhcpResults.serverAddress);
                        P2pStateMachine.this.sendP2pConnectionChangedBroadcast();
                        try {
                            String ifname = P2pStateMachine.this.mGroup.getInterface();
                            WifiP2pServiceImpl.this.mNwService.addInterfaceToLocalNetwork(ifname, WifiP2pServiceImpl.this.mDhcpResults.getRoutes(ifname));
                            break;
                        } catch (RemoteException e2) {
                            P2pStateMachine.this.loge("Failed to add iface to local network " + e2);
                            break;
                        }
                    case WifiP2pServiceImpl.IPM_PROVISIONING_FAILURE /*143394*/:
                        P2pStateMachine.this.loge("IP provisioning failed");
                        P2pStateMachine.this.mWifiNative.p2pGroupRemove(P2pStateMachine.this.mGroup.getInterface());
                        break;
                    case WifiMonitor.P2P_DEVICE_LOST_EVENT /*147478*/:
                        device = (WifiP2pDevice) message.obj;
                        if (!P2pStateMachine.this.mGroup.contains(device)) {
                            return WifiP2pServiceImpl.HWDBG;
                        }
                        P2pStateMachine.this.logd("Add device to lost list " + device);
                        P2pStateMachine.this.mPeersLostDuringConnection.updateSupplicantDetails(device);
                        return WifiP2pServiceImpl.DBG;
                    case WifiMonitor.P2P_GROUP_STARTED_EVENT /*147485*/:
                        P2pStateMachine.this.loge("Duplicate group creation event notice, ignore");
                        break;
                    case WifiMonitor.P2P_GROUP_REMOVED_EVENT /*147486*/:
                        P2pStateMachine.this.logd(getName() + " group removed");
                        P2pStateMachine.this.enableBTCoex();
                        P2pStateMachine.this.handleGroupRemoved();
                        P2pStateMachine.this.transitionTo(P2pStateMachine.this.mInactiveState);
                        P2pStateMachine.this.mWifiNative.p2pFlush();
                        break;
                    case WifiMonitor.P2P_INVITATION_RESULT_EVENT /*147488*/:
                        P2pStatus status = message.obj;
                        if (status != P2pStatus.SUCCESS) {
                            P2pStateMachine.this.loge("Invitation result " + status);
                            if (status == P2pStatus.UNKNOWN_P2P_GROUP) {
                                int netId = P2pStateMachine.this.mGroup.getNetworkId();
                                if (netId >= 0) {
                                    P2pStateMachine.this.logd("Remove unknown client from the list");
                                    if (!P2pStateMachine.this.removeClientFromList(netId, P2pStateMachine.this.mSavedPeerConfig.deviceAddress, WifiP2pServiceImpl.HWDBG)) {
                                        P2pStateMachine.this.loge("Already removed the client, ignore");
                                    }
                                    P2pStateMachine.this.sendMessage(139271, P2pStateMachine.this.mSavedPeerConfig);
                                    break;
                                }
                            }
                        }
                        break;
                    case WifiMonitor.P2P_PROV_DISC_PBC_REQ_EVENT /*147489*/:
                    case WifiMonitor.P2P_PROV_DISC_ENTER_PIN_EVENT /*147491*/:
                    case WifiMonitor.P2P_PROV_DISC_SHOW_PIN_EVENT /*147492*/:
                        WifiP2pProvDiscEvent provDisc = message.obj;
                        p2pStateMachine = P2pStateMachine.this;
                        p2pStateMachine.mSavedPeerConfig = new WifiP2pConfig();
                        WifiP2pConfig wifiP2pConfig = P2pStateMachine.this.mSavedPeerConfig;
                        wifiP2pConfig.deviceAddress = provDisc.device.deviceAddress;
                        i2 = message.what;
                        if (r0 == 147491) {
                            P2pStateMachine.this.mSavedPeerConfig.wps.setup = WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_SENSE;
                        } else {
                            i2 = message.what;
                            if (r0 == 147492) {
                                P2pStateMachine.this.mSavedPeerConfig.wps.setup = WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_DISABLED;
                                WpsInfo wpsInfo = P2pStateMachine.this.mSavedPeerConfig.wps;
                                wpsInfo.pin = provDisc.pin;
                            } else {
                                P2pStateMachine.this.mSavedPeerConfig.wps.setup = WifiP2pServiceImpl.DISABLED;
                            }
                        }
                        P2pStateMachine.this.logd("mGroup.isGroupOwner()" + P2pStateMachine.this.mGroup.isGroupOwner());
                        if (P2pStateMachine.this.mGroup.isGroupOwner()) {
                            P2pStateMachine.this.logd("Device is GO, going to mUserAuthorizingJoinState");
                            P2pStateMachine.this.transitionTo(P2pStateMachine.this.mUserAuthorizingJoinState);
                            break;
                        }
                        break;
                    case WifiMonitor.P2P_REMOVE_AND_REFORM_GROUP_EVENT /*147496*/:
                        Slog.d(WifiP2pServiceImpl.TAG, "Received event P2P_REMOVE_AND_REFORM_GROUP, remove P2P group");
                        if (!handlP2pGroupRestart()) {
                            P2pStateMachine.this.replyToMessage(message, 139281, WifiP2pServiceImpl.DISABLED);
                            break;
                        }
                        P2pStateMachine.this.replyToMessage(message, 139282);
                        break;
                    case WifiMonitor.AP_STA_DISCONNECTED_EVENT /*147497*/:
                        device = (WifiP2pDevice) message.obj;
                        deviceAddress = device.deviceAddress;
                        if (deviceAddress != null) {
                            P2pStateMachine.this.mPeers.updateStatus(deviceAddress, 3);
                            if (P2pStateMachine.this.mGroup.removeClient(deviceAddress)) {
                                P2pStateMachine.this.logd("Removed client " + deviceAddress);
                                if (!WifiP2pServiceImpl.this.mAutonomousGroup) {
                                    if (P2pStateMachine.this.mGroup.isClientListEmpty()) {
                                        P2pStateMachine.this.logd("Client list empty, remove non-persistent p2p group");
                                        P2pStateMachine.this.mWifiNative.p2pGroupRemove(P2pStateMachine.this.mGroup.getInterface());
                                    }
                                }
                                P2pStateMachine.this.sendP2pConnectionChangedBroadcast();
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
                    case WifiMonitor.AP_STA_CONNECTED_EVENT /*147498*/:
                        deviceAddress = message.obj.deviceAddress;
                        P2pStateMachine.this.mWifiNative.setP2pGroupIdle(P2pStateMachine.this.mGroup.getInterface(), WifiP2pServiceImpl.DISABLED);
                        if (deviceAddress != null) {
                            if (P2pStateMachine.this.mPeers.get(deviceAddress) != null) {
                                P2pStateMachine.this.mGroup.addClient(P2pStateMachine.this.mPeers.get(deviceAddress));
                            } else {
                                P2pStateMachine.this.mGroup.addClient(deviceAddress);
                            }
                            P2pStateMachine.this.mPeers.updateStatus(deviceAddress, WifiP2pServiceImpl.DISABLED);
                            P2pStateMachine.this.logd(getName() + " ap sta connected");
                            P2pStateMachine.this.sendPeersChangedBroadcast();
                        } else {
                            P2pStateMachine.this.loge("Connect on null device address, ignore");
                        }
                        P2pStateMachine.this.sendP2pConnectionChangedBroadcast();
                        WifiP2pServiceImpl.this.handleClientConnect(P2pStateMachine.this.mGroup);
                        break;
                    case 196612:
                        DhcpResults dhcpResults = message.obj;
                        P2pStateMachine.this.enableBTCoex();
                        i2 = message.arg1;
                        if (r0 == WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_DISABLED && dhcpResults != null) {
                            P2pStateMachine.this.logd("DhcpResults: " + dhcpResults);
                            P2pStateMachine.this.setWifiP2pInfoOnGroupFormation(dhcpResults.serverAddress);
                            P2pStateMachine.this.sendP2pConnectionChangedBroadcast();
                            P2pStateMachine.this.mWifiNative.setP2pPowerSave(P2pStateMachine.this.mGroup.getInterface(), WifiP2pServiceImpl.DBG);
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
                return WifiP2pServiceImpl.DBG;
            }

            public void exit() {
                P2pStateMachine.this.updateThisDevice(3);
                P2pStateMachine.this.resetWifiP2pInfo();
                WifiP2pServiceImpl.this.mNetworkInfo.setDetailedState(DetailedState.DISCONNECTED, null, null);
                if (WifiP2pServiceHisiExt.hisiWifiEnabled()) {
                    WifiP2pServiceImpl.this.mWifiP2pServiceHisiExt.sendP2pNetworkChangedBroadcast();
                }
                P2pStateMachine.this.sendP2pConnectionChangedBroadcast();
            }
        }

        class GroupCreatingState extends State {
            GroupCreatingState() {
            }

            public void enter() {
                P2pStateMachine.this.logd(getName());
                P2pStateMachine.this.sendMessageDelayed(P2pStateMachine.this.obtainMessage(WifiP2pServiceImpl.GROUP_CREATING_TIMED_OUT, WifiP2pServiceImpl.mGroupCreatingTimeoutIndex = WifiP2pServiceImpl.mGroupCreatingTimeoutIndex + WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_DISABLED, WifiP2pServiceImpl.DISABLED), 120000);
            }

            public boolean processMessage(Message message) {
                P2pStateMachine.this.logd(getName() + message.toString());
                if (WifiP2pServiceImpl.this.processMessageForP2pCollision(message, this)) {
                    return WifiP2pServiceImpl.DBG;
                }
                boolean ret = WifiP2pServiceImpl.DBG;
                switch (message.what) {
                    case 139265:
                        P2pStateMachine.this.replyToMessage(message, 139266, (int) WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_SENSE);
                        break;
                    case 139274:
                        P2pStateMachine.this.mWifiNative.p2pCancelConnect();
                        P2pStateMachine.this.handleGroupCreationFailure();
                        P2pStateMachine.this.transitionTo(P2pStateMachine.this.mInactiveState);
                        P2pStateMachine.this.replyToMessage(message, 139276);
                        break;
                    case WifiP2pServiceImpl.GROUP_CREATING_TIMED_OUT /*143361*/:
                        if (WifiP2pServiceImpl.mGroupCreatingTimeoutIndex == message.arg1) {
                            P2pStateMachine.this.logd("Group negotiation timed out");
                            P2pStateMachine.this.handleGroupCreationFailure();
                            P2pStateMachine.this.transitionTo(P2pStateMachine.this.mInactiveState);
                            break;
                        }
                        break;
                    case WifiMonitor.P2P_DEVICE_LOST_EVENT /*147478*/:
                        WifiP2pDevice device = message.obj;
                        if (!P2pStateMachine.this.mSavedPeerConfig.deviceAddress.equals(device.deviceAddress)) {
                            P2pStateMachine.this.logd("mSavedPeerConfig " + P2pStateMachine.this.mSavedPeerConfig.deviceAddress + "device " + device.deviceAddress);
                            ret = WifiP2pServiceImpl.HWDBG;
                            break;
                        }
                        P2pStateMachine.this.logd("Add device to lost list " + device);
                        P2pStateMachine.this.mPeersLostDuringConnection.updateSupplicantDetails(device);
                        break;
                    case WifiMonitor.P2P_GO_NEGOTIATION_SUCCESS_EVENT /*147481*/:
                        WifiP2pServiceImpl.this.mAutonomousGroup = WifiP2pServiceImpl.HWDBG;
                        P2pStateMachine.this.transitionTo(P2pStateMachine.this.mGroupNegotiationState);
                        break;
                    default:
                        ret = WifiP2pServiceImpl.HWDBG;
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
                P2pStateMachine.this.mPendingReformGroupIndication = WifiP2pServiceImpl.HWDBG;
            }

            /* JADX WARNING: inconsistent code. */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public boolean processMessage(Message message) {
                P2pStateMachine.this.logd(getName() + message.toString());
                switch (message.what) {
                    case WifiMonitor.P2P_GO_NEGOTIATION_SUCCESS_EVENT /*147481*/:
                    case WifiMonitor.P2P_GROUP_FORMATION_SUCCESS_EVENT /*147483*/:
                        P2pStateMachine.this.logd(getName() + " go success");
                        break;
                    case WifiMonitor.P2P_GO_NEGOTIATION_FAILURE_EVENT /*147482*/:
                        if (message.obj == P2pStatus.NO_COMMON_CHANNEL) {
                            P2pStateMachine.this.transitionTo(P2pStateMachine.this.mFrequencyConflictState);
                            break;
                        }
                    case WifiMonitor.P2P_GROUP_FORMATION_FAILURE_EVENT /*147484*/:
                        if (((P2pStatus) message.obj) == P2pStatus.NO_COMMON_CHANNEL) {
                            P2pStateMachine.this.transitionTo(P2pStateMachine.this.mFrequencyConflictState);
                            break;
                        }
                        break;
                    case WifiMonitor.P2P_GROUP_STARTED_EVENT /*147485*/:
                        P2pStateMachine.this.mGroup = (WifiP2pGroup) message.obj;
                        P2pStateMachine.this.logd(getName() + " group started");
                        if (P2pStateMachine.this.mGroup.getNetworkId() == -2) {
                            P2pStateMachine.this.updatePersistentNetworks(WifiP2pServiceImpl.NO_RELOAD.booleanValue());
                            P2pStateMachine.this.mGroup.setNetworkId(P2pStateMachine.this.mGroups.getNetworkId(P2pStateMachine.this.mGroup.getOwner().deviceAddress, P2pStateMachine.this.mGroup.getNetworkName()));
                        }
                        if (P2pStateMachine.this.mGroup.isGroupOwner()) {
                            WifiP2pServiceImpl.this.sendGroupConfigInfo(P2pStateMachine.this.mGroup);
                            if (!WifiP2pServiceImpl.this.mAutonomousGroup) {
                                P2pStateMachine.this.mWifiNative.setP2pGroupIdle(P2pStateMachine.this.mGroup.getInterface(), WifiP2pServiceImpl.GROUP_IDLE_TIME_S);
                            }
                            if (WifiP2pServiceHisiExt.hisiWifiEnabled()) {
                                P2pStateMachine.this.startDhcpServer(HwWifiCHRStateManager.MAIN_IFACE);
                            } else {
                                P2pStateMachine.this.startDhcpServer(P2pStateMachine.this.mGroup.getInterface());
                            }
                        } else {
                            if (!WifiP2pServiceImpl.this.getMagicLinkDeviceFlag()) {
                                P2pStateMachine.this.mWifiNative.setP2pGroupIdle(P2pStateMachine.this.mGroup.getInterface(), WifiP2pServiceImpl.GROUP_IDLE_TIME_S);
                                if (WifiP2pServiceHisiExt.hisiWifiEnabled()) {
                                    WifiP2pServiceImpl.this.startIpManager(HwWifiCHRStateManager.MAIN_IFACE);
                                } else {
                                    WifiP2pServiceImpl.this.startIpManager(P2pStateMachine.this.mGroup.getInterface());
                                }
                                P2pStateMachine.this.mWifiNative.setBluetoothCoexistenceMode(WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_DISABLED);
                                P2pStateMachine.this.mIsBTCoexDisabled = WifiP2pServiceImpl.DBG;
                            }
                            WifiP2pDevice groupOwner = P2pStateMachine.this.mGroup.getOwner();
                            if (groupOwner != null) {
                                WifiP2pDevice peer = P2pStateMachine.this.mPeers.get(groupOwner.deviceAddress);
                                if (peer != null) {
                                    groupOwner.updateSupplicantDetails(peer);
                                    P2pStateMachine.this.mPeers.updateStatus(groupOwner.deviceAddress, WifiP2pServiceImpl.DISABLED);
                                    P2pStateMachine.this.sendPeersChangedBroadcast();
                                } else {
                                    if (!(groupOwner == null || "00:00:00:00:00:00".equals(groupOwner.deviceAddress))) {
                                        Matcher match = Pattern.compile("([0-9a-f]{2}:){5}[0-9a-f]{2}").matcher(groupOwner.deviceAddress);
                                        Log.e(WifiP2pServiceImpl.TAG, "try to judge groupOwner is valid or not");
                                        if (match.find()) {
                                            groupOwner.primaryDeviceType = "10-0050F204-5";
                                            P2pStateMachine.this.mPeers.updateSupplicantDetails(groupOwner);
                                            P2pStateMachine.this.mPeers.updateStatus(groupOwner.deviceAddress, WifiP2pServiceImpl.DISABLED);
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
                    case WifiMonitor.P2P_GROUP_REMOVED_EVENT /*147486*/:
                        P2pStateMachine.this.logd(getName() + " go failure");
                        P2pStateMachine.this.handleGroupCreationFailure();
                        WifiP2pServiceImpl.this.sendP2pFailStateBroadcast();
                        P2pStateMachine.this.transitionTo(P2pStateMachine.this.mInactiveState);
                        break;
                    case WifiMonitor.P2P_INVITATION_RESULT_EVENT /*147488*/:
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
                                P2pStateMachine.this.removeClientFromList(netId, P2pStateMachine.this.mSavedPeerConfig.deviceAddress, WifiP2pServiceImpl.DBG);
                            }
                            P2pStateMachine.this.mSavedPeerConfig.netId = -2;
                            P2pStateMachine.this.p2pConnectWithPinDisplay(P2pStateMachine.this.mSavedPeerConfig);
                            break;
                        }
                        break;
                    case WifiMonitor.P2P_REMOVE_AND_REFORM_GROUP_EVENT /*147496*/:
                        P2pStateMachine.this.logd("P2P_REMOVE_AND_REFORM_GROUP_EVENT event received in GroupNegotiationState state");
                        P2pStateMachine.this.mPendingReformGroupIndication = WifiP2pServiceImpl.DBG;
                        break;
                    default:
                        return WifiP2pServiceImpl.this.handleGroupNegotiationStateExMessage(message);
                }
                return WifiP2pServiceImpl.DBG;
            }
        }

        class InactiveState extends State {
            InactiveState() {
            }

            public void enter() {
                P2pStateMachine.this.logd(getName());
                WifiP2pServiceImpl.this.mIsInvite = WifiP2pServiceImpl.HWDBG;
                WifiP2pServiceImpl.this.setmMagicLinkDeviceFlag(WifiP2pServiceImpl.HWDBG);
                P2pStateMachine.this.mSavedPeerConfig.invalidate();
            }

            public boolean processMessage(Message message) {
                P2pStateMachine.this.logd(getName() + message.what);
                if (WifiP2pServiceImpl.this.processMessageForP2pCollision(message, this)) {
                    return WifiP2pServiceImpl.DBG;
                }
                WifiP2pConfig config;
                switch (message.what) {
                    case 139268:
                        if (!P2pStateMachine.this.mWifiNative.p2pStopFind()) {
                            P2pStateMachine.this.replyToMessage(message, 139269, WifiP2pServiceImpl.DISABLED);
                            break;
                        }
                        P2pStateMachine.this.mWifiNative.p2pFlush();
                        WifiP2pServiceImpl.this.mServiceDiscReqId = null;
                        P2pStateMachine.this.replyToMessage(message, 139270);
                        break;
                    case 139271:
                        WifiP2pServiceImpl.this.setmMagicLinkDeviceFlag(WifiP2pServiceImpl.HWDBG);
                        P2pStateMachine.this.logd(getName() + " sending connect");
                        config = message.obj;
                        if (!P2pStateMachine.this.isConfigInvalid(config)) {
                            WifiP2pServiceImpl.this.mAutonomousGroup = WifiP2pServiceImpl.HWDBG;
                            P2pStateMachine.this.mWifiNative.p2pStopFind();
                            if (P2pStateMachine.this.reinvokePersistentGroup(config)) {
                                P2pStateMachine.this.transitionTo(P2pStateMachine.this.mGroupNegotiationState);
                            } else {
                                P2pStateMachine.this.transitionTo(P2pStateMachine.this.mProvisionDiscoveryState);
                            }
                            P2pStateMachine.this.mSavedPeerConfig = config;
                            P2pStateMachine.this.mPeers.updateStatus(P2pStateMachine.this.mSavedPeerConfig.deviceAddress, WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_DISABLED);
                            P2pStateMachine.this.sendPeersChangedBroadcast();
                            P2pStateMachine.this.replyToMessage(message, 139273);
                            break;
                        }
                        P2pStateMachine.this.loge("Dropping connect requeset " + config);
                        P2pStateMachine.this.replyToMessage(message, 139272);
                        break;
                    case 139277:
                        boolean ret;
                        WifiP2pServiceImpl.this.mAutonomousGroup = WifiP2pServiceImpl.DBG;
                        if (message.arg1 == -2) {
                            int netId = P2pStateMachine.this.mGroups.getNetworkId(WifiP2pServiceImpl.this.mThisDevice.deviceAddress);
                            if (netId != -1) {
                                ret = P2pStateMachine.this.mWifiNative.p2pGroupAdd(netId);
                            } else {
                                ret = P2pStateMachine.this.mWifiNative.p2pGroupAdd((boolean) WifiP2pServiceImpl.DBG);
                            }
                        } else {
                            ret = P2pStateMachine.this.mWifiNative.p2pGroupAdd((boolean) WifiP2pServiceImpl.HWDBG);
                        }
                        if (!ret) {
                            P2pStateMachine.this.replyToMessage(message, 139278, WifiP2pServiceImpl.DISABLED);
                            break;
                        }
                        P2pStateMachine.this.replyToMessage(message, 139279);
                        P2pStateMachine.this.transitionTo(P2pStateMachine.this.mGroupNegotiationState);
                        break;
                    case 139329:
                        P2pStateMachine.this.logd(getName() + " start listen mode");
                        P2pStateMachine.this.mWifiNative.p2pFlush();
                        if (!P2pStateMachine.this.mWifiNative.p2pExtListen(WifiP2pServiceImpl.DBG, 500, 500)) {
                            P2pStateMachine.this.replyToMessage(message, 139330);
                            break;
                        }
                        P2pStateMachine.this.replyToMessage(message, 139331);
                        break;
                    case 139332:
                        P2pStateMachine.this.logd(getName() + " stop listen mode");
                        if (P2pStateMachine.this.mWifiNative.p2pExtListen(WifiP2pServiceImpl.HWDBG, WifiP2pServiceImpl.DISABLED, WifiP2pServiceImpl.DISABLED)) {
                            P2pStateMachine.this.replyToMessage(message, 139334);
                        } else {
                            P2pStateMachine.this.replyToMessage(message, 139333);
                        }
                        P2pStateMachine.this.mWifiNative.p2pFlush();
                        break;
                    case 139335:
                        Bundle p2pChannels = message.obj;
                        int lc = p2pChannels.getInt("lc", WifiP2pServiceImpl.DISABLED);
                        int oc = p2pChannels.getInt("oc", WifiP2pServiceImpl.DISABLED);
                        P2pStateMachine.this.logd(getName() + " set listen and operating channel");
                        if (!P2pStateMachine.this.mWifiNative.p2pSetChannel(lc, oc)) {
                            P2pStateMachine.this.replyToMessage(message, 139336);
                            break;
                        }
                        P2pStateMachine.this.replyToMessage(message, 139337);
                        break;
                    case 139342:
                        String handoverSelect = null;
                        if (message.obj != null) {
                            handoverSelect = ((Bundle) message.obj).getString("android.net.wifi.p2p.EXTRA_HANDOVER_MESSAGE");
                        }
                        if (handoverSelect != null) {
                            if (P2pStateMachine.this.mWifiNative.initiatorReportNfcHandover(handoverSelect)) {
                                P2pStateMachine.this.replyToMessage(message, 139344);
                                P2pStateMachine.this.transitionTo(P2pStateMachine.this.mGroupCreatingState);
                                break;
                            }
                        }
                        P2pStateMachine.this.replyToMessage(message, 139345);
                        break;
                    case 139343:
                        String handoverRequest = null;
                        if (message.obj != null) {
                            handoverRequest = ((Bundle) message.obj).getString("android.net.wifi.p2p.EXTRA_HANDOVER_MESSAGE");
                        }
                        if (handoverRequest != null) {
                            if (P2pStateMachine.this.mWifiNative.responderReportNfcHandover(handoverRequest)) {
                                P2pStateMachine.this.replyToMessage(message, 139344);
                                P2pStateMachine.this.transitionTo(P2pStateMachine.this.mGroupCreatingState);
                                break;
                            }
                        }
                        P2pStateMachine.this.replyToMessage(message, 139345);
                        break;
                    case 141268:
                        WifiP2pServiceImpl.this.mCreateWifiBridge = WifiP2pServiceImpl.DBG;
                        return WifiP2pServiceImpl.this.handleInactiveStateMessage(message);
                    case WifiMonitor.P2P_GO_NEGOTIATION_REQUEST_EVENT /*147479*/:
                        config = (WifiP2pConfig) message.obj;
                        if (!P2pStateMachine.this.isConfigInvalid(config)) {
                            P2pStateMachine.this.mSavedPeerConfig = config;
                            WifiP2pServiceImpl.this.mAutonomousGroup = WifiP2pServiceImpl.HWDBG;
                            WifiP2pServiceImpl.this.mJoinExistingGroup = WifiP2pServiceImpl.HWDBG;
                            P2pStateMachine.this.transitionTo(P2pStateMachine.this.mUserAuthorizingNegotiationRequestState);
                            break;
                        }
                        P2pStateMachine.this.loge("Dropping GO neg request " + config);
                        break;
                    case WifiMonitor.P2P_GROUP_STARTED_EVENT /*147485*/:
                        P2pStateMachine.this.mGroup = (WifiP2pGroup) message.obj;
                        P2pStateMachine.this.logd(getName() + " group started");
                        if (P2pStateMachine.this.mGroup.getNetworkId() != -2) {
                            P2pStateMachine.this.loge("Unexpected group creation, remove " + P2pStateMachine.this.mGroup);
                            P2pStateMachine.this.mWifiNative.p2pGroupRemove(P2pStateMachine.this.mGroup.getInterface());
                            break;
                        }
                        WifiP2pServiceImpl.this.mAutonomousGroup = WifiP2pServiceImpl.HWDBG;
                        P2pStateMachine.this.deferMessage(message);
                        P2pStateMachine.this.transitionTo(P2pStateMachine.this.mGroupNegotiationState);
                        break;
                    case WifiMonitor.P2P_INVITATION_RECEIVED_EVENT /*147487*/:
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
                            owner = P2pStateMachine.this.mPeers.get(owner.deviceAddress);
                            if (owner != null) {
                                if (owner.wpsPbcSupported()) {
                                    P2pStateMachine.this.mSavedPeerConfig.wps.setup = WifiP2pServiceImpl.DISABLED;
                                } else if (owner.wpsKeypadSupported()) {
                                    P2pStateMachine.this.mSavedPeerConfig.wps.setup = WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_SENSE;
                                } else if (owner.wpsDisplaySupported()) {
                                    P2pStateMachine.this.mSavedPeerConfig.wps.setup = WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_DISABLED;
                                }
                            }
                            WifiP2pServiceImpl.this.mAutonomousGroup = WifiP2pServiceImpl.HWDBG;
                            WifiP2pServiceImpl.this.mJoinExistingGroup = WifiP2pServiceImpl.DBG;
                            WifiP2pServiceImpl.this.mIsInvite = WifiP2pServiceImpl.DBG;
                            P2pStateMachine.this.transitionTo(P2pStateMachine.this.mUserAuthorizingInviteRequestState);
                            break;
                        }
                        P2pStateMachine.this.loge("Dropping invitation request " + config);
                        break;
                    case WifiMonitor.P2P_PROV_DISC_PBC_REQ_EVENT /*147489*/:
                    case WifiMonitor.P2P_PROV_DISC_ENTER_PIN_EVENT /*147491*/:
                        break;
                    case WifiMonitor.P2P_PROV_DISC_SHOW_PIN_EVENT /*147492*/:
                        WifiP2pProvDiscEvent provDisc = message.obj;
                        WifiP2pDevice device = provDisc.device;
                        if (device != null) {
                            P2pStateMachine.this.notifyP2pProvDiscShowPinRequest(provDisc.pin, device.deviceAddress);
                            P2pStateMachine.this.mPeers.updateStatus(device.deviceAddress, WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_DISABLED);
                            P2pStateMachine.this.sendPeersChangedBroadcast();
                            P2pStateMachine.this.transitionTo(P2pStateMachine.this.mGroupNegotiationState);
                            break;
                        }
                        Slog.d(WifiP2pServiceImpl.TAG, "Device entry is null");
                        break;
                    default:
                        return WifiP2pServiceImpl.this.handleInactiveStateMessage(message);
                }
                return WifiP2pServiceImpl.DBG;
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
                        return WifiP2pServiceImpl.DBG;
                    default:
                        return WifiP2pServiceImpl.this.handleOngoingGroupRemovalStateExMessage(message);
                }
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
                        if (P2pStateMachine.this.mWifiMonitor.startMonitoring(P2pStateMachine.this.mWifiNative.getInterfaceName())) {
                            P2pStateMachine.this.transitionTo(P2pStateMachine.this.mP2pEnablingState);
                        } else {
                            P2pStateMachine.this.loge("start  monitoring failed, do not transition");
                        }
                        return WifiP2pServiceImpl.DBG;
                    default:
                        return WifiP2pServiceImpl.HWDBG;
                }
            }
        }

        class P2pDisablingState extends State {
            P2pDisablingState() {
            }

            public void enter() {
                P2pStateMachine.this.logd(getName());
                P2pStateMachine.this.sendMessageDelayed(P2pStateMachine.this.obtainMessage(WifiP2pServiceImpl.DISABLE_P2P_TIMED_OUT, WifiP2pServiceImpl.mDisableP2pTimeoutIndex = WifiP2pServiceImpl.mDisableP2pTimeoutIndex + WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_DISABLED, WifiP2pServiceImpl.DISABLED), 5000);
            }

            public boolean processMessage(Message message) {
                P2pStateMachine.this.logd(getName() + message.what);
                switch (message.what) {
                    case WifiStateMachine.CMD_ENABLE_P2P /*131203*/:
                    case WifiStateMachine.CMD_DISABLE_P2P_REQ /*131204*/:
                        P2pStateMachine.this.deferMessage(message);
                        break;
                    case WifiP2pServiceImpl.DISABLE_P2P_TIMED_OUT /*143366*/:
                        if (WifiP2pServiceImpl.mDisableP2pTimeoutIndex == message.arg1) {
                            P2pStateMachine.this.loge("P2p disable timed out");
                            P2pStateMachine.this.transitionTo(P2pStateMachine.this.mP2pDisabledState);
                            break;
                        }
                        break;
                    case WifiMonitor.SUP_DISCONNECTION_EVENT /*147458*/:
                        P2pStateMachine.this.logd("p2p socket connection lost");
                        P2pStateMachine.this.transitionTo(P2pStateMachine.this.mP2pDisabledState);
                        break;
                    default:
                        return WifiP2pServiceImpl.HWDBG;
                }
                return WifiP2pServiceImpl.DBG;
            }

            public void exit() {
                WifiP2pServiceImpl.this.mWifiChannel.sendMessage(WifiStateMachine.CMD_DISABLE_P2P_RSP);
            }
        }

        class P2pEnabledState extends State {
            P2pEnabledState() {
            }

            public void enter() {
                P2pStateMachine.this.logd(getName());
                P2pStateMachine.this.sendP2pStateChangedBroadcast(WifiP2pServiceImpl.DBG);
                WifiP2pServiceImpl.this.mNetworkInfo.setIsAvailable(WifiP2pServiceImpl.DBG);
                if (!WifiP2pServiceHisiExt.hisiWifiEnabled()) {
                    P2pStateMachine.this.sendP2pConnectionChangedBroadcast();
                } else if (WifiP2pServiceImpl.this.isWifiP2pEnabled()) {
                    P2pStateMachine.this.sendP2pConnectionChangedBroadcast();
                }
                P2pStateMachine.this.initializeP2pSettings();
            }

            public boolean processMessage(Message message) {
                P2pStateMachine p2pStateMachine = P2pStateMachine.this;
                int i = message.what;
                r0.logd(getName() + " when=" + message.getWhen() + " what=" + r0 + " arg1=" + message.arg1 + " arg2=" + message.arg2);
                if (WifiP2pServiceImpl.this.processMessageForP2pCollision(message, this)) {
                    return WifiP2pServiceImpl.DBG;
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
                            if (!P2pStateMachine.this.mWifiNative.p2pFind(WifiP2pServiceImpl.DISCOVER_TIMEOUT_S)) {
                                P2pStateMachine.this.replyToMessage(message, 139266, WifiP2pServiceImpl.DISABLED);
                                break;
                            }
                            P2pStateMachine.this.replyToMessage(message, 139267);
                            P2pStateMachine.this.sendP2pDiscoveryChangedBroadcast(WifiP2pServiceImpl.DBG);
                            break;
                        }
                        P2pStateMachine.this.replyToMessage(message, 139266, WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_SENSE);
                        break;
                    case 139268:
                        if (!P2pStateMachine.this.mWifiNative.p2pStopFind()) {
                            P2pStateMachine.this.replyToMessage(message, 139269, WifiP2pServiceImpl.DISABLED);
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
                            if (!P2pStateMachine.this.updateSupplicantServiceRequest()) {
                                P2pStateMachine.this.replyToMessage(message, 139311, 3);
                                break;
                            }
                            if (!P2pStateMachine.this.mWifiNative.p2pFind(WifiP2pServiceImpl.DISCOVER_TIMEOUT_S)) {
                                P2pStateMachine.this.replyToMessage(message, 139311, WifiP2pServiceImpl.DISABLED);
                                break;
                            }
                            P2pStateMachine.this.replyToMessage(message, 139312);
                            break;
                        }
                        P2pStateMachine.this.replyToMessage(message, 139311, WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_SENSE);
                        break;
                    case 139315:
                        WifiP2pDevice d = message.obj;
                        if (d != null) {
                            if (P2pStateMachine.this.setAndPersistDeviceName(d.deviceName)) {
                                P2pStateMachine.this.logd("set device name " + d.deviceName);
                                P2pStateMachine.this.replyToMessage(message, 139317);
                                break;
                            }
                        }
                        P2pStateMachine.this.replyToMessage(message, 139316, WifiP2pServiceImpl.DISABLED);
                        break;
                    case 139318:
                        P2pStateMachine.this.logd(getName() + " delete persistent group");
                        P2pStateMachine.this.mGroups.remove(message.arg1);
                        P2pStateMachine.this.replyToMessage(message, 139320);
                        break;
                    case 139323:
                        WifiP2pWfdInfo d2 = message.obj;
                        if (d2 != null) {
                            if (P2pStateMachine.this.setWfdInfo(d2)) {
                                P2pStateMachine.this.replyToMessage(message, 139325);
                                break;
                            }
                        }
                        P2pStateMachine.this.replyToMessage(message, 139324, WifiP2pServiceImpl.DISABLED);
                        break;
                    case 139329:
                        P2pStateMachine.this.logd(getName() + " start listen mode");
                        P2pStateMachine.this.mWifiNative.p2pFlush();
                        if (!P2pStateMachine.this.mWifiNative.p2pExtListen(WifiP2pServiceImpl.DBG, 500, 500)) {
                            P2pStateMachine.this.replyToMessage(message, 139330);
                            break;
                        }
                        P2pStateMachine.this.replyToMessage(message, 139331);
                        break;
                    case 139332:
                        P2pStateMachine.this.logd(getName() + " stop listen mode");
                        if (P2pStateMachine.this.mWifiNative.p2pExtListen(WifiP2pServiceImpl.HWDBG, WifiP2pServiceImpl.DISABLED, WifiP2pServiceImpl.DISABLED)) {
                            P2pStateMachine.this.replyToMessage(message, 139334);
                        } else {
                            P2pStateMachine.this.replyToMessage(message, 139333);
                        }
                        P2pStateMachine.this.mWifiNative.p2pFlush();
                        break;
                    case 139335:
                        Bundle p2pChannels = message.obj;
                        int lc = p2pChannels.getInt("lc", WifiP2pServiceImpl.DISABLED);
                        int oc = p2pChannels.getInt("oc", WifiP2pServiceImpl.DISABLED);
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
                        int i2 = message.arg1;
                        boolean blocked = r0 == WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_DISABLED ? WifiP2pServiceImpl.DBG : WifiP2pServiceImpl.HWDBG;
                        if (WifiP2pServiceImpl.this.mDiscoveryBlocked != blocked) {
                            WifiP2pServiceImpl.this.mDiscoveryBlocked = blocked;
                            if (blocked) {
                                if (WifiP2pServiceImpl.this.mDiscoveryStarted) {
                                    P2pStateMachine.this.mWifiNative.p2pStopFind();
                                    WifiP2pServiceImpl.this.mDiscoveryPostponed = WifiP2pServiceImpl.DBG;
                                }
                            }
                            if (!blocked) {
                                if (WifiP2pServiceImpl.this.mDiscoveryPostponed) {
                                    WifiP2pServiceImpl.this.mDiscoveryPostponed = WifiP2pServiceImpl.HWDBG;
                                    P2pStateMachine.this.mWifiNative.p2pFind(WifiP2pServiceImpl.DISCOVER_TIMEOUT_S);
                                }
                            }
                            if (blocked) {
                                try {
                                    message.obj.sendMessage(message.arg2);
                                    break;
                                } catch (Exception e) {
                                    P2pStateMachine.this.loge("unable to send BLOCK_DISCOVERY response: " + e);
                                    break;
                                }
                            }
                        }
                        break;
                    case WifiMonitor.SUP_DISCONNECTION_EVENT /*147458*/:
                        P2pStateMachine.this.loge("Unexpected loss of p2p socket connection");
                        WifiP2pServiceImpl.this.mWifiChannel.sendMessage(WifiMonitor.SUP_DISCONNECTION_EVENT);
                        P2pStateMachine.this.transitionTo(P2pStateMachine.this.mP2pDisabledState);
                        break;
                    case WifiMonitor.P2P_DEVICE_FOUND_EVENT /*147477*/:
                        WifiP2pDevice device = message.obj;
                        if (!WifiP2pServiceImpl.this.mThisDevice.deviceAddress.equals(device.deviceAddress)) {
                            P2pStateMachine.this.mPeers.updateSupplicantDetails(device);
                            P2pStateMachine.this.sendPeersChangedBroadcast();
                            if (WifiP2pServiceHisiExt.hisiWifiEnabled()) {
                                if (WifiP2pServiceImpl.this.mWifiP2pServiceHisiExt.P2pFindDeviceUpdate) {
                                    WifiP2pServiceImpl.this.mWifiP2pServiceHisiExt.P2pFindDeviceUpdate = WifiP2pServiceImpl.HWDBG;
                                    P2pStateMachine.this.updatePersistentNetworks(WifiP2pServiceImpl.DBG);
                                    break;
                                }
                            }
                        }
                        break;
                    case WifiMonitor.P2P_DEVICE_LOST_EVENT /*147478*/:
                        if (P2pStateMachine.this.mPeers.remove(((WifiP2pDevice) message.obj).deviceAddress) != null) {
                            P2pStateMachine.this.sendPeersChangedBroadcast();
                            break;
                        }
                        break;
                    case WifiMonitor.P2P_FIND_STOPPED_EVENT /*147493*/:
                        P2pStateMachine.this.sendP2pDiscoveryChangedBroadcast(WifiP2pServiceImpl.HWDBG);
                        break;
                    case WifiMonitor.P2P_SERV_DISC_RESP_EVENT /*147494*/:
                        P2pStateMachine.this.logd(getName() + " receive service response");
                        for (WifiP2pServiceResponse resp : message.obj) {
                            resp.setSrcDevice(P2pStateMachine.this.mPeers.get(resp.getSrcDevice().deviceAddress));
                            P2pStateMachine.this.sendServiceResponse(resp);
                        }
                        break;
                    default:
                        return WifiP2pServiceImpl.this.handleP2pEnabledStateExMessage(message);
                }
                return WifiP2pServiceImpl.DBG;
            }

            public void exit() {
                P2pStateMachine.this.sendP2pDiscoveryChangedBroadcast(WifiP2pServiceImpl.HWDBG);
                if (!WifiP2pServiceHisiExt.hisiWifiEnabled()) {
                    P2pStateMachine.this.sendP2pStateChangedBroadcast(WifiP2pServiceImpl.HWDBG);
                }
                WifiP2pServiceImpl.this.mNetworkInfo.setIsAvailable(WifiP2pServiceImpl.HWDBG);
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
                    case WifiMonitor.SUP_CONNECTION_EVENT /*147457*/:
                        P2pStateMachine.this.logd("P2p socket connection successful");
                        P2pStateMachine.this.transitionTo(P2pStateMachine.this.mInactiveState);
                        break;
                    case WifiMonitor.SUP_DISCONNECTION_EVENT /*147458*/:
                        P2pStateMachine.this.loge("P2p socket connection failed");
                        P2pStateMachine.this.transitionTo(P2pStateMachine.this.mP2pDisabledState);
                        break;
                    default:
                        return WifiP2pServiceImpl.HWDBG;
                }
                return WifiP2pServiceImpl.DBG;
            }
        }

        class P2pNotSupportedState extends State {
            P2pNotSupportedState() {
            }

            public boolean processMessage(Message message) {
                switch (message.what) {
                    case 139265:
                        P2pStateMachine.this.replyToMessage(message, 139266, (int) WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_DISABLED);
                        break;
                    case 139268:
                        P2pStateMachine.this.replyToMessage(message, 139269, (int) WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_DISABLED);
                        break;
                    case 139271:
                        P2pStateMachine.this.replyToMessage(message, 139272, (int) WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_DISABLED);
                        break;
                    case 139274:
                        P2pStateMachine.this.replyToMessage(message, 139275, (int) WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_DISABLED);
                        break;
                    case 139277:
                        P2pStateMachine.this.replyToMessage(message, 139278, (int) WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_DISABLED);
                        break;
                    case 139280:
                        P2pStateMachine.this.replyToMessage(message, 139281, (int) WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_DISABLED);
                        if (WifiP2pServiceImpl.this.getWifiRepeaterEnabled()) {
                            WifiP2pServiceImpl.this.stopWifiRepeater(P2pStateMachine.this.mGroup);
                            break;
                        }
                        break;
                    case 139292:
                        P2pStateMachine.this.replyToMessage(message, 139293, (int) WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_DISABLED);
                        break;
                    case 139295:
                        P2pStateMachine.this.replyToMessage(message, 139296, (int) WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_DISABLED);
                        break;
                    case 139298:
                        P2pStateMachine.this.replyToMessage(message, 139299, (int) WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_DISABLED);
                        break;
                    case 139301:
                        P2pStateMachine.this.replyToMessage(message, 139302, (int) WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_DISABLED);
                        break;
                    case 139304:
                        P2pStateMachine.this.replyToMessage(message, 139305, (int) WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_DISABLED);
                        break;
                    case 139307:
                        P2pStateMachine.this.replyToMessage(message, 139308, (int) WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_DISABLED);
                        break;
                    case 139310:
                        P2pStateMachine.this.replyToMessage(message, 139311, (int) WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_DISABLED);
                        break;
                    case 139315:
                        P2pStateMachine.this.replyToMessage(message, 139316, (int) WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_DISABLED);
                        break;
                    case 139318:
                        P2pStateMachine.this.replyToMessage(message, 139318, (int) WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_DISABLED);
                        break;
                    case 139323:
                        P2pStateMachine.this.replyToMessage(message, 139324, (int) WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_DISABLED);
                        break;
                    case 139326:
                        P2pStateMachine.this.replyToMessage(message, 139327, (int) WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_DISABLED);
                        break;
                    case 139329:
                        P2pStateMachine.this.replyToMessage(message, 139330, (int) WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_DISABLED);
                        break;
                    case 139332:
                        P2pStateMachine.this.replyToMessage(message, 139333, (int) WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_DISABLED);
                        break;
                    default:
                        return WifiP2pServiceImpl.this.handleP2pNotSupportedStateMessage(message);
                }
                return WifiP2pServiceImpl.DBG;
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
                switch (message.what) {
                    case WifiMonitor.P2P_PROV_DISC_PBC_RSP_EVENT /*147490*/:
                        if (message.obj.device.deviceAddress.equals(P2pStateMachine.this.mSavedPeerConfig.deviceAddress) && P2pStateMachine.this.mSavedPeerConfig.wps.setup == 0) {
                            P2pStateMachine.this.logd("Found a match " + P2pStateMachine.this.mSavedPeerConfig);
                            P2pStateMachine.this.p2pConnectWithPinDisplay(P2pStateMachine.this.mSavedPeerConfig);
                            P2pStateMachine.this.transitionTo(P2pStateMachine.this.mGroupNegotiationState);
                            break;
                        }
                    case WifiMonitor.P2P_PROV_DISC_ENTER_PIN_EVENT /*147491*/:
                        if (((WifiP2pProvDiscEvent) message.obj).device.deviceAddress.equals(P2pStateMachine.this.mSavedPeerConfig.deviceAddress) && P2pStateMachine.this.mSavedPeerConfig.wps.setup == WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_SENSE) {
                            P2pStateMachine.this.logd("Found a match " + P2pStateMachine.this.mSavedPeerConfig);
                            if (!TextUtils.isEmpty(P2pStateMachine.this.mSavedPeerConfig.wps.pin)) {
                                P2pStateMachine.this.p2pConnectWithPinDisplay(P2pStateMachine.this.mSavedPeerConfig);
                                P2pStateMachine.this.transitionTo(P2pStateMachine.this.mGroupNegotiationState);
                                break;
                            }
                            WifiP2pServiceImpl.this.mJoinExistingGroup = WifiP2pServiceImpl.HWDBG;
                            P2pStateMachine.this.transitionTo(P2pStateMachine.this.mUserAuthorizingNegotiationRequestState);
                            break;
                        }
                    case WifiMonitor.P2P_PROV_DISC_SHOW_PIN_EVENT /*147492*/:
                        WifiP2pProvDiscEvent provDisc = (WifiP2pProvDiscEvent) message.obj;
                        WifiP2pDevice device = provDisc.device;
                        if (device.deviceAddress.equals(P2pStateMachine.this.mSavedPeerConfig.deviceAddress) && P2pStateMachine.this.mSavedPeerConfig.wps.setup == WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_DISABLED) {
                            P2pStateMachine.this.logd("Found a match " + P2pStateMachine.this.mSavedPeerConfig);
                            P2pStateMachine.this.mSavedPeerConfig.wps.pin = provDisc.pin;
                            P2pStateMachine.this.p2pConnectWithPinDisplay(P2pStateMachine.this.mSavedPeerConfig);
                            P2pStateMachine.this.notifyInvitationSent(provDisc.pin, device.deviceAddress);
                            P2pStateMachine.this.transitionTo(P2pStateMachine.this.mGroupNegotiationState);
                            break;
                        }
                    case WifiMonitor.P2P_PROV_DISC_FAILURE_EVENT /*147495*/:
                        P2pStateMachine.this.loge("provision discovery failed");
                        P2pStateMachine.this.handleGroupCreationFailure();
                        P2pStateMachine.this.transitionTo(P2pStateMachine.this.mInactiveState);
                        break;
                    default:
                        return WifiP2pServiceImpl.HWDBG;
                }
                return WifiP2pServiceImpl.DBG;
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
                        P2pStateMachine.this.mPeers.updateStatus(P2pStateMachine.this.mSavedPeerConfig.deviceAddress, WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_DISABLED);
                        P2pStateMachine.this.sendPeersChangedBroadcast();
                        P2pStateMachine.this.transitionTo(P2pStateMachine.this.mGroupNegotiationState);
                        break;
                    case WifiP2pServiceImpl.PEER_CONNECTION_USER_REJECT /*143363*/:
                        P2pStateMachine.this.logd("User rejected invitation " + P2pStateMachine.this.mSavedPeerConfig);
                        P2pStateMachine.this.transitionTo(P2pStateMachine.this.mInactiveState);
                        break;
                    default:
                        return WifiP2pServiceImpl.HWDBG;
                }
                return WifiP2pServiceImpl.DBG;
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
                    case WifiMonitor.P2P_PROV_DISC_PBC_REQ_EVENT /*147489*/:
                    case WifiMonitor.P2P_PROV_DISC_ENTER_PIN_EVENT /*147491*/:
                    case WifiMonitor.P2P_PROV_DISC_SHOW_PIN_EVENT /*147492*/:
                        break;
                    default:
                        return WifiP2pServiceImpl.HWDBG;
                }
                return WifiP2pServiceImpl.DBG;
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
                        P2pStateMachine.this.mPeers.updateStatus(P2pStateMachine.this.mSavedPeerConfig.deviceAddress, WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_DISABLED);
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
                        return WifiP2pServiceImpl.HWDBG;
                }
                return WifiP2pServiceImpl.DBG;
            }

            public void exit() {
            }
        }

        P2pStateMachine(String name, Looper looper, boolean p2pSupported) {
            super(name, looper);
            this.mDefaultState = new DefaultState();
            this.mP2pNotSupportedState = new P2pNotSupportedState();
            this.mP2pDisablingState = new P2pDisablingState();
            this.mP2pDisabledState = new P2pDisabledState();
            this.mP2pEnablingState = new P2pEnablingState();
            this.mP2pEnabledState = new P2pEnabledState();
            this.mInactiveState = new InactiveState();
            this.mGroupCreatingState = new GroupCreatingState();
            this.mUserAuthorizingInviteRequestState = new UserAuthorizingInviteRequestState();
            this.mUserAuthorizingNegotiationRequestState = new UserAuthorizingNegotiationRequestState();
            this.mProvisionDiscoveryState = new ProvisionDiscoveryState();
            this.mGroupNegotiationState = new GroupNegotiationState();
            this.mFrequencyConflictState = new FrequencyConflictState();
            this.mGroupCreatedState = new GroupCreatedState();
            this.mUserAuthorizingJoinState = new UserAuthorizingJoinState();
            this.mAfterUserAuthorizingJoinState = new AfterUserAuthorizingJoinState();
            this.mOngoingGroupRemovalState = new OngoingGroupRemovalState();
            this.mWifiNative = WifiNative.getP2pNativeInterface();
            this.mWifiMonitor = WifiMonitor.getInstance();
            this.mPeers = new WifiP2pDeviceList();
            this.mPeersLostDuringConnection = new WifiP2pDeviceList();
            this.mGroups = new WifiP2pGroupList(null, new GroupDeleteListener() {
                public void onDeleteGroup(int netId) {
                    P2pStateMachine.this.logd("called onDeleteGroup() netId=" + netId);
                    P2pStateMachine.this.mWifiNative.removeNetwork(netId);
                    P2pStateMachine.this.mWifiNative.saveConfig();
                    P2pStateMachine.this.updatePersistentNetworks(WifiP2pServiceImpl.RELOAD.booleanValue());
                    P2pStateMachine.this.sendP2pPersistentGroupsChangedBroadcast();
                }
            });
            this.mWifiP2pInfo = new WifiP2pInfo();
            this.mGroup = null;
            this.mIsBTCoexDisabled = WifiP2pServiceImpl.HWDBG;
            this.mPendingReformGroupIndication = WifiP2pServiceImpl.HWDBG;
            this.mSavedPeerConfig = new WifiP2pConfig();
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
            setLogOnlyTransitions(WifiP2pServiceImpl.DBG);
            String interfaceName = this.mWifiNative.getInterfaceName();
            this.mWifiMonitor.registerHandler(interfaceName, WifiMonitor.AP_STA_CONNECTED_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiMonitor.AP_STA_DISCONNECTED_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiMonitor.AUTHENTICATION_FAILURE_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiMonitor.NETWORK_CONNECTION_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiMonitor.NETWORK_DISCONNECTION_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiMonitor.P2P_DEVICE_FOUND_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiMonitor.P2P_DEVICE_LOST_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiMonitor.P2P_FIND_STOPPED_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiMonitor.P2P_GO_NEGOTIATION_FAILURE_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiMonitor.P2P_GO_NEGOTIATION_REQUEST_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiMonitor.P2P_GO_NEGOTIATION_SUCCESS_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiMonitor.P2P_GROUP_FORMATION_FAILURE_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiMonitor.P2P_GROUP_FORMATION_SUCCESS_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiMonitor.P2P_GROUP_REMOVED_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiMonitor.P2P_GROUP_STARTED_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiMonitor.P2P_INVITATION_RECEIVED_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiMonitor.P2P_INVITATION_RESULT_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiMonitor.P2P_PROV_DISC_ENTER_PIN_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiMonitor.P2P_PROV_DISC_FAILURE_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiMonitor.P2P_PROV_DISC_PBC_REQ_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiMonitor.P2P_PROV_DISC_PBC_RSP_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiMonitor.P2P_PROV_DISC_SHOW_PIN_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiMonitor.P2P_SERV_DISC_RESP_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiMonitor.SCAN_RESULTS_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiMonitor.SUP_CONNECTION_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiMonitor.SUP_DISCONNECTION_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiMonitor.WPS_FAIL_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiMonitor.WPS_OVERLAP_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiMonitor.WPS_SUCCESS_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiMonitor.WPS_TIMEOUT_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiMonitor.P2P_GC_INTERFACE_CREATED_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(interfaceName, WifiMonitor.P2P_GO_INTERFACE_CREATED_EVENT, getHandler());
        }

        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            super.dump(fd, pw, args);
            pw.println("mWifiP2pInfo " + this.mWifiP2pInfo);
            pw.println("mGroup " + this.mGroup);
            pw.println("mSavedPeerConfig " + this.mSavedPeerConfig);
            pw.println();
        }

        private void sendP2pStateChangedBroadcast(boolean enabled) {
            logd("p2pState change broadcast " + enabled);
            Intent intent = new Intent("android.net.wifi.p2p.STATE_CHANGED");
            intent.addFlags(67108864);
            if (enabled) {
                intent.putExtra("wifi_p2p_state", WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_SENSE);
            } else {
                intent.putExtra("wifi_p2p_state", WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_DISABLED);
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
                    i = WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_SENSE;
                } else {
                    i = WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_DISABLED;
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
            WifiP2pServiceImpl.this.mWifiChannel.sendMessage(WifiP2pServiceImpl.P2P_CONNECTION_CHANGED, new NetworkInfo(WifiP2pServiceImpl.this.mNetworkInfo));
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
                ifcg.setLinkAddress(new LinkAddress(NetworkUtils.numericToInetAddress(WifiP2pServiceImpl.this.mP2pServerAddress), 24));
                WifiP2pServiceImpl.this.mCreateWifiBridge = WifiP2pServiceImpl.HWDBG;
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
                WifiP2pServiceImpl.this.mCreateWifiBridge = WifiP2pServiceImpl.HWDBG;
                loge("Error configuring interface " + intf + ", :" + e);
            }
        }

        private void stopDhcpServer(String intf) {
            try {
                WifiP2pServiceImpl.this.mNwService.untetherInterface(intf);
                String[] listTetheredInterfaces = WifiP2pServiceImpl.this.mNwService.listTetheredInterfaces();
                int length = listTetheredInterfaces.length;
                for (int i = WifiP2pServiceImpl.DISABLED; i < length; i += WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_DISABLED) {
                    String temp = listTetheredInterfaces[i];
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
            AlertDialog dialog = new Builder(WifiP2pServiceImpl.this.mContext).setTitle(r.getString(17040336)).setMessage(r.getString(17040338)).setPositiveButton(r.getString(17039370), null).create();
            dialog.getWindow().setType(2003);
            LayoutParams attrs = dialog.getWindow().getAttributes();
            attrs.privateFlags = 16;
            dialog.getWindow().setAttributes(attrs);
            dialog.show();
        }

        private void addRowToDialog(ViewGroup group, int stringId, String value) {
            Resources r = Resources.getSystem();
            View row = LayoutInflater.from(WifiP2pServiceImpl.this.mContext).inflate(17367307, group, WifiP2pServiceImpl.HWDBG);
            ((TextView) row.findViewById(16909384)).setText(r.getString(stringId));
            ((TextView) row.findViewById(16909187)).setText(value);
            group.addView(row);
        }

        protected void notifyInvitationSent(String pin, String peerAddress) {
            Resources r = Resources.getSystem();
            View textEntryView = LayoutInflater.from(WifiP2pServiceImpl.this.mContext).inflate(17367306, null);
            ViewGroup group = (ViewGroup) textEntryView.findViewById(16909381);
            addRowToDialog(group, 17040346, getDeviceName(peerAddress));
            addRowToDialog(group, 17040348, pin);
            AlertDialog dialog = new Builder(WifiP2pServiceImpl.this.mContext).setTitle(r.getString(17040343)).setView(textEntryView).setPositiveButton(r.getString(17039370), null).create();
            dialog.getWindow().setType(2003);
            LayoutParams attrs = dialog.getWindow().getAttributes();
            attrs.privateFlags = 16;
            dialog.getWindow().setAttributes(attrs);
            dialog.show();
        }

        private void notifyP2pProvDiscShowPinRequest(String pin, String peerAddress) {
            Resources r = Resources.getSystem();
            String tempDevAddress = peerAddress;
            String tempPin = pin;
            View textEntryView = LayoutInflater.from(WifiP2pServiceImpl.this.mContext).inflate(17367306, null);
            ViewGroup group = (ViewGroup) textEntryView.findViewById(16909381);
            addRowToDialog(group, 17040346, getDeviceName(peerAddress));
            addRowToDialog(group, 17040348, pin);
            AlertDialog dialog = new Builder(WifiP2pServiceImpl.this.mContext).setTitle(r.getString(17040343)).setView(textEntryView).setPositiveButton(r.getString(17040341), new AnonymousClass2(peerAddress, pin)).setCancelable(WifiP2pServiceImpl.HWDBG).create();
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
                WpsInfo wps = this.mSavedPeerConfig.wps;
                View textEntryView = LayoutInflater.from(WifiP2pServiceImpl.this.mContext).inflate(17367306, null);
                ViewGroup group = (ViewGroup) textEntryView.findViewById(16909381);
                View row = LayoutInflater.from(WifiP2pServiceImpl.this.mContext).inflate(17367322, group, WifiP2pServiceImpl.HWDBG);
                TextView textView = (TextView) row.findViewById(16909384);
                String string = r.getString(33685691);
                Object[] objArr = new Object[WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_DISABLED];
                objArr[WifiP2pServiceImpl.DISABLED] = getDeviceName(this.mSavedPeerConfig.deviceAddress);
                textView.setText(String.format(string, objArr));
                ((TextView) row.findViewById(16909384)).setTextColor(-16777216);
                group.addView(row);
                AlertDialog dialog = new Builder(WifiP2pServiceImpl.this.mContext, 33947691).setTitle(r.getString(33685693)).setView(textEntryView).setPositiveButton(r.getString(33685692), new AnonymousClass3(wps, (EditText) textEntryView.findViewById(16909383))).setNegativeButton(r.getString(17039360), new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        P2pStateMachine.this.logd(P2pStateMachine.this.getName() + " ignore connect");
                        P2pStateMachine.this.sendMessage(WifiP2pServiceImpl.PEER_CONNECTION_USER_REJECT);
                        P2pStateMachine.this.processStatistics(WifiP2pServiceImpl.this.mContext, 300, WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_DISABLED);
                    }
                }).setOnCancelListener(new OnCancelListener() {
                    public void onCancel(DialogInterface arg0) {
                        P2pStateMachine.this.logd(P2pStateMachine.this.getName() + " ignore connect");
                        P2pStateMachine.this.sendMessage(WifiP2pServiceImpl.PEER_CONNECTION_USER_REJECT);
                        P2pStateMachine.this.processStatistics(WifiP2pServiceImpl.this.mContext, 300, WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_DISABLED);
                    }
                }).create();
                switch (wps.setup) {
                    case WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_DISABLED /*1*/:
                        logd("Shown pin section visible");
                        addRowToDialog(group, 17040348, wps.pin);
                        break;
                    case WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_SENSE /*2*/:
                        logd("Enter pin section visible");
                        textEntryView.findViewById(16909382).setVisibility(WifiP2pServiceImpl.DISABLED);
                        break;
                }
                if ((r.getConfiguration().uiMode & 5) == 5) {
                    dialog.setOnKeyListener(new OnKeyListener() {
                        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                            if (keyCode != 164) {
                                return WifiP2pServiceImpl.HWDBG;
                            }
                            P2pStateMachine.this.sendMessage(WifiP2pServiceImpl.PEER_CONNECTION_USER_ACCEPT);
                            dialog.dismiss();
                            return WifiP2pServiceImpl.DBG;
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
            String listStr = this.mWifiNative.listNetworks();
            if (listStr != null) {
                boolean isSaveRequired = WifiP2pServiceImpl.HWDBG;
                String[] lines = listStr.split("\n");
                if (lines != null) {
                    if (reload) {
                        this.mGroups.clear();
                    }
                    for (int i = WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_DISABLED; i < lines.length; i += WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_DISABLED) {
                        String[] result = lines[i].split("\t");
                        if (result != null && result.length >= 4) {
                            String ssid = result[WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_DISABLED];
                            if (!TextUtils.isEmpty(ssid)) {
                                ssid = WifiSsid.createFromAsciiEncoded(ssid).toString();
                            }
                            String bssid = result[WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_SENSE];
                            String flags = result[3];
                            try {
                                int netId = Integer.parseInt(result[WifiP2pServiceImpl.DISABLED]);
                                if (flags.indexOf("[CURRENT]") == -1) {
                                    if (flags.indexOf("[P2P-PERSISTENT]") == -1) {
                                        logd("clean up the unused persistent group. netId=" + netId);
                                        this.mWifiNative.removeNetwork(netId);
                                        isSaveRequired = WifiP2pServiceImpl.DBG;
                                    } else if (!this.mGroups.contains(netId)) {
                                        WifiP2pGroup group = new WifiP2pGroup();
                                        group.setNetworkId(netId);
                                        group.setNetworkName(ssid);
                                        String mode = this.mWifiNative.getNetworkVariable(netId, "mode");
                                        if (mode != null && mode.equals("3")) {
                                            group.setIsGroupOwner(WifiP2pServiceImpl.DBG);
                                        }
                                        if (bssid.equalsIgnoreCase(WifiP2pServiceImpl.this.mThisDevice.deviceAddress)) {
                                            group.setOwner(WifiP2pServiceImpl.this.mThisDevice);
                                        } else {
                                            WifiP2pDevice device = new WifiP2pDevice();
                                            device.deviceAddress = bssid;
                                            group.setOwner(device);
                                        }
                                        this.mGroups.add(group);
                                        isSaveRequired = WifiP2pServiceImpl.DBG;
                                    }
                                }
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    if (reload || isSaveRequired) {
                        this.mWifiNative.saveConfig();
                        sendP2pPersistentGroupsChangedBroadcast();
                    }
                }
            }
        }

        private boolean isConfigInvalid(WifiP2pConfig config) {
            if (config == null || TextUtils.isEmpty(config.deviceAddress) || this.mPeers.get(config.deviceAddress) == null) {
                return WifiP2pServiceImpl.DBG;
            }
            return WifiP2pServiceImpl.HWDBG;
        }

        private WifiP2pDevice fetchCurrentDeviceDetails(WifiP2pConfig config) {
            this.mPeers.updateGroupCapability(config.deviceAddress, this.mWifiNative.getGroupCapability(config.deviceAddress));
            return this.mPeers.get(config.deviceAddress);
        }

        private boolean isMiracastDevice(String deviceType) {
            if (deviceType == null) {
                return WifiP2pServiceImpl.HWDBG;
            }
            String[] tokens = deviceType.split("-");
            try {
                if (tokens.length > 0 && Integer.parseInt(tokens[WifiP2pServiceImpl.DISABLED]) == WifiP2pServiceImpl.P2P_DEVICE_OF_MIRACAST) {
                    logd("As connecting miracast device ,set go_intent = 14 to let it works as GO ");
                    return WifiP2pServiceImpl.DBG;
                }
            } catch (NumberFormatException e) {
                loge("isMiracastDevice: " + e);
            }
            return WifiP2pServiceImpl.HWDBG;
        }

        private boolean wifiIsConnected() {
            if (WifiP2pServiceImpl.this.mContext == null) {
                return WifiP2pServiceImpl.HWDBG;
            }
            WifiManager wifiMgr = (WifiManager) WifiP2pServiceImpl.this.mContext.getSystemService("wifi");
            if (wifiMgr != null && wifiMgr.getWifiState() == 3) {
                NetworkInfo wifiInfo = ((ConnectivityManager) WifiP2pServiceImpl.this.mContext.getSystemService("connectivity")).getNetworkInfo(WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_DISABLED);
                if (wifiInfo != null) {
                    logd("wifiIsConnected: " + wifiInfo.isConnected());
                    return wifiInfo.isConnected();
                }
            }
            return WifiP2pServiceImpl.HWDBG;
        }

        private void p2pConnectWithPinDisplay(WifiP2pConfig config) {
            WifiP2pDevice dev = fetchCurrentDeviceDetails(config);
            if (dev == null) {
                loge("target device is not found " + config.deviceAddress);
                return;
            }
            boolean join;
            if ((dev.primaryDeviceType != null && isMiracastDevice(dev.primaryDeviceType)) || wifiIsConnected()) {
                logd("set groupOwnerIntent is 14");
                config.groupOwnerIntent = 14;
            }
            if (WifiP2pServiceImpl.this.mIsInvite) {
                join = WifiP2pServiceImpl.DBG;
            } else {
                join = dev.isGroupOwner();
            }
            String pin = this.mWifiNative.p2pConnect(config, join);
            try {
                Integer.parseInt(pin);
                notifyInvitationSent(pin, config.deviceAddress);
            } catch (NumberFormatException e) {
            }
            WifiP2pServiceImpl.this.mIsInvite = WifiP2pServiceImpl.HWDBG;
        }

        private boolean reinvokePersistentGroup(WifiP2pConfig config) {
            int netId;
            WifiP2pDevice dev = fetchCurrentDeviceDetails(config);
            boolean join = dev.isGroupOwner();
            String ssid = this.mWifiNative.p2pGetSsid(dev.deviceAddress);
            logd("target ssid is " + ssid + " join:" + join);
            if (join && dev.isGroupLimit()) {
                logd("target device reaches group limit.");
                join = WifiP2pServiceImpl.HWDBG;
            } else if (join) {
                netId = this.mGroups.getNetworkId(dev.deviceAddress, ssid);
                if (netId >= 0) {
                    return !this.mWifiNative.p2pGroupAdd(netId) ? WifiP2pServiceImpl.HWDBG : WifiP2pServiceImpl.DBG;
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
                            return WifiP2pServiceImpl.DBG;
                        }
                        loge("p2pReinvoke() failed, update networks");
                        updatePersistentNetworks(WifiP2pServiceImpl.RELOAD.booleanValue());
                        return WifiP2pServiceImpl.HWDBG;
                    }
                }
                return WifiP2pServiceImpl.HWDBG;
            }
            loge("target device reaches the device limit.");
            return WifiP2pServiceImpl.HWDBG;
        }

        protected int getNetworkIdFromClientList(String deviceAddress) {
            if (deviceAddress == null) {
                return -1;
            }
            for (WifiP2pGroup group : this.mGroups.getGroupList()) {
                int netId = group.getNetworkId();
                String[] p2pClientList = getClientList(netId);
                if (p2pClientList != null) {
                    int length = p2pClientList.length;
                    for (int i = WifiP2pServiceImpl.DISABLED; i < length; i += WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_DISABLED) {
                        if (deviceAddress.equalsIgnoreCase(p2pClientList[i])) {
                            return netId;
                        }
                    }
                    continue;
                }
            }
            return -1;
        }

        private String[] getClientList(int netId) {
            String p2pClients = this.mWifiNative.getNetworkVariable(netId, "p2p_client_list");
            if (p2pClients == null) {
                return null;
            }
            return p2pClients.split(" ");
        }

        private boolean removeClientFromList(int netId, String addr, boolean isRemovable) {
            StringBuilder modifiedClientList = new StringBuilder();
            String[] currentClientList = getClientList(netId);
            boolean isClientRemoved = WifiP2pServiceImpl.HWDBG;
            if (currentClientList != null) {
                int length = currentClientList.length;
                for (int i = WifiP2pServiceImpl.DISABLED; i < length; i += WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_DISABLED) {
                    String client = currentClientList[i];
                    if (client.equalsIgnoreCase(addr)) {
                        isClientRemoved = WifiP2pServiceImpl.DBG;
                    } else {
                        modifiedClientList.append(" ");
                        modifiedClientList.append(client);
                    }
                }
            }
            if (modifiedClientList.length() == 0 && isRemovable) {
                logd("Remove unknown network");
                this.mGroups.remove(netId);
                return WifiP2pServiceImpl.DBG;
            } else if (!isClientRemoved) {
                return WifiP2pServiceImpl.HWDBG;
            } else {
                logd("Modified client list: " + modifiedClientList);
                if (modifiedClientList.length() == 0) {
                    modifiedClientList.append("\"\"");
                }
                this.mWifiNative.setNetworkVariable(netId, "p2p_client_list", modifiedClientList.toString());
                this.mWifiNative.saveConfig();
                return WifiP2pServiceImpl.DBG;
            }
        }

        private void setWifiP2pInfoOnGroupFormation(InetAddress serverInetAddress) {
            this.mWifiP2pInfo.groupFormed = WifiP2pServiceImpl.DBG;
            this.mWifiP2pInfo.isGroupOwner = this.mGroup.isGroupOwner();
            this.mWifiP2pInfo.groupOwnerAddress = serverInetAddress;
        }

        private void resetWifiP2pInfo() {
            this.mWifiP2pInfo.groupFormed = WifiP2pServiceImpl.HWDBG;
            this.mWifiP2pInfo.isGroupOwner = WifiP2pServiceImpl.HWDBG;
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
            if (!SystemProperties.getBoolean("ro.config.hw_wifi_bt_name", WifiP2pServiceImpl.HWDBG) || !TextUtils.isEmpty(deviceName)) {
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
            if (id == null || id.length() <= 4) {
                return Build.MODEL;
            }
            return Build.MODEL + "_" + id.substring(WifiP2pServiceImpl.DISABLED, 4);
        }

        private boolean setAndPersistDeviceName(String devName) {
            if (devName == null) {
                return WifiP2pServiceImpl.HWDBG;
            }
            if (this.mWifiNative.setDeviceName(devName)) {
                WifiP2pServiceImpl.this.mThisDevice.deviceName = devName;
                this.mWifiNative.setP2pSsidPostfix("-" + getSsidPostFix(WifiP2pServiceImpl.this.mThisDevice.deviceName));
                Global.putString(WifiP2pServiceImpl.this.mContext.getContentResolver(), "wifi_p2p_device_name", devName);
                sendThisDeviceChangedBroadcast();
                return WifiP2pServiceImpl.DBG;
            }
            loge("Failed to set device name " + devName);
            return WifiP2pServiceImpl.HWDBG;
        }

        private boolean setWfdInfo(WifiP2pWfdInfo wfdInfo) {
            boolean wfdEnable;
            if (!wfdInfo.isWfdEnabled()) {
                wfdEnable = this.mWifiNative.setWfdEnable(WifiP2pServiceImpl.HWDBG);
            } else if (this.mWifiNative.setWfdEnable(WifiP2pServiceImpl.DBG)) {
                wfdEnable = this.mWifiNative.setWfdDeviceInfo(wfdInfo.getDeviceInfoHex());
            } else {
                wfdEnable = WifiP2pServiceImpl.HWDBG;
            }
            if (wfdEnable) {
                WifiP2pServiceImpl.this.mThisDevice.wfdInfo = wfdInfo;
                sendThisDeviceChangedBroadcast();
                return WifiP2pServiceImpl.DBG;
            }
            loge("Failed to set wfd properties");
            return WifiP2pServiceImpl.HWDBG;
        }

        private void initializeP2pSettings() {
            this.mWifiNative.setPersistentReconnect(WifiP2pServiceImpl.DBG);
            WifiP2pServiceImpl.this.mThisDevice.deviceName = getPersistedDeviceName();
            this.mWifiNative.setDeviceName(WifiP2pServiceImpl.this.mThisDevice.deviceName);
            this.mWifiNative.setP2pSsidPostfix("-" + getSsidPostFix(WifiP2pServiceImpl.this.mThisDevice.deviceName));
            this.mWifiNative.setDeviceType(WifiP2pServiceImpl.this.mThisDevice.primaryDeviceType);
            this.mWifiNative.setConfigMethods("virtual_push_button physical_display keypad");
            this.mWifiNative.setConcurrencyPriority("sta");
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
                peersChanged = WifiP2pServiceImpl.DBG;
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
                WifiP2pServiceImpl.this.setmMagicLinkDeviceFlag(WifiP2pServiceImpl.HWDBG);
            } else {
                logd("stop IpManager");
                WifiP2pServiceImpl.this.stopIpManager();
                try {
                    WifiP2pServiceImpl.this.mNwService.removeInterfaceFromLocalNetwork(this.mGroup.getInterface());
                } catch (RemoteException e) {
                    loge("Failed to remove iface from local network " + e);
                }
                if (WifiP2pServiceImpl.this.mWiFiCHRManager != null) {
                    WifiP2pServiceImpl.this.mWiFiCHRManager.waitForDhcpStopping("p2p");
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
            this.mWifiNative.setP2pGroupIdle(this.mGroup.getInterface(), WifiP2pServiceImpl.DISABLED);
            boolean peersChanged = WifiP2pServiceImpl.HWDBG;
            for (WifiP2pDevice d : this.mGroup.getClientList()) {
                if (this.mPeers.remove(d)) {
                    peersChanged = WifiP2pServiceImpl.DBG;
                }
            }
            if (this.mPeers.remove(this.mGroup.getOwner())) {
                peersChanged = WifiP2pServiceImpl.DBG;
            }
            if (this.mPeers.remove(this.mPeersLostDuringConnection)) {
                peersChanged = WifiP2pServiceImpl.DBG;
            }
            if (peersChanged) {
                sendPeersChangedBroadcast();
            }
            this.mGroup = null;
            this.mPeersLostDuringConnection.clear();
            WifiP2pServiceImpl.this.mServiceDiscReqId = null;
            if (WifiP2pServiceImpl.this.mTemporarilyDisconnectedWifi) {
                WifiP2pServiceImpl.this.mWifiChannel.sendMessage(WifiP2pServiceImpl.DISCONNECT_WIFI_REQUEST, WifiP2pServiceImpl.DISABLED);
                WifiP2pServiceImpl.this.mTemporarilyDisconnectedWifi = WifiP2pServiceImpl.HWDBG;
            }
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
                for (int i = WifiP2pServiceImpl.DISABLED; i < c.mReqList.size(); i += WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_DISABLED) {
                    WifiP2pServiceRequest req = (WifiP2pServiceRequest) c.mReqList.valueAt(i);
                    if (req != null) {
                        sb.append(req.getSupplicantQuery());
                    }
                }
            }
            if (sb.length() == 0) {
                return WifiP2pServiceImpl.HWDBG;
            }
            WifiP2pServiceImpl.this.mServiceDiscReqId = this.mWifiNative.p2pServDiscReq("00:00:00:00:00:00", sb.toString());
            if (WifiP2pServiceImpl.this.mServiceDiscReqId == null) {
                return WifiP2pServiceImpl.HWDBG;
            }
            return WifiP2pServiceImpl.DBG;
        }

        private void clearSupplicantServiceRequest() {
            if (WifiP2pServiceImpl.this.mServiceDiscReqId != null) {
                this.mWifiNative.p2pServDiscCancelReq(WifiP2pServiceImpl.this.mServiceDiscReqId);
                WifiP2pServiceImpl.this.mServiceDiscReqId = null;
            }
        }

        private boolean addServiceRequest(Messenger m, WifiP2pServiceRequest req) {
            clearClientDeadChannels();
            ClientInfo clientInfo = getClientInfo(m, WifiP2pServiceImpl.DBG);
            if (clientInfo == null) {
                return WifiP2pServiceImpl.HWDBG;
            }
            WifiP2pServiceImpl wifiP2pServiceImpl = WifiP2pServiceImpl.this;
            wifiP2pServiceImpl.mServiceTransactionId = (byte) (wifiP2pServiceImpl.mServiceTransactionId + WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_DISABLED);
            if (WifiP2pServiceImpl.this.mServiceTransactionId == null) {
                wifiP2pServiceImpl = WifiP2pServiceImpl.this;
                wifiP2pServiceImpl.mServiceTransactionId = (byte) (wifiP2pServiceImpl.mServiceTransactionId + WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_DISABLED);
            }
            req.setTransactionId(WifiP2pServiceImpl.this.mServiceTransactionId);
            clientInfo.mReqList.put(WifiP2pServiceImpl.this.mServiceTransactionId, req);
            if (WifiP2pServiceImpl.this.mServiceDiscReqId == null) {
                return WifiP2pServiceImpl.DBG;
            }
            return updateSupplicantServiceRequest();
        }

        private void removeServiceRequest(Messenger m, WifiP2pServiceRequest req) {
            ClientInfo clientInfo = getClientInfo(m, WifiP2pServiceImpl.HWDBG);
            if (clientInfo != null) {
                boolean removed = WifiP2pServiceImpl.HWDBG;
                for (int i = WifiP2pServiceImpl.DISABLED; i < clientInfo.mReqList.size(); i += WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_DISABLED) {
                    if (req.equals(clientInfo.mReqList.valueAt(i))) {
                        removed = WifiP2pServiceImpl.DBG;
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
            ClientInfo clientInfo = getClientInfo(m, WifiP2pServiceImpl.HWDBG);
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
            clearClientDeadChannels();
            ClientInfo clientInfo = getClientInfo(m, WifiP2pServiceImpl.DBG);
            if (clientInfo == null || !clientInfo.mServList.add(servInfo)) {
                return WifiP2pServiceImpl.HWDBG;
            }
            if (this.mWifiNative.p2pServiceAdd(servInfo)) {
                return WifiP2pServiceImpl.DBG;
            }
            clientInfo.mServList.remove(servInfo);
            return WifiP2pServiceImpl.HWDBG;
        }

        private void removeLocalService(Messenger m, WifiP2pServiceInfo servInfo) {
            ClientInfo clientInfo = getClientInfo(m, WifiP2pServiceImpl.HWDBG);
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
            ClientInfo clientInfo = getClientInfo(m, WifiP2pServiceImpl.HWDBG);
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
            for (ClientInfo c : WifiP2pServiceImpl.this.mClientInfoList.values()) {
                if (((WifiP2pServiceRequest) c.mReqList.get(resp.getTransactionId())) != null) {
                    Message msg = Message.obtain();
                    msg.what = 139314;
                    msg.arg1 = WifiP2pServiceImpl.DISABLED;
                    msg.arg2 = WifiP2pServiceImpl.DISABLED;
                    msg.obj = resp;
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

        private void clearClientDeadChannels() {
            ArrayList<Messenger> deadClients = new ArrayList();
            for (ClientInfo c : WifiP2pServiceImpl.this.mClientInfoList.values()) {
                Message msg = Message.obtain();
                msg.what = 139313;
                msg.arg1 = WifiP2pServiceImpl.DISABLED;
                msg.arg2 = WifiP2pServiceImpl.DISABLED;
                msg.obj = null;
                try {
                    c.mMessenger.send(msg);
                } catch (RemoteException e) {
                    logd("detect dead channel");
                    deadClients.add(c.mMessenger);
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
            clientInfo = new ClientInfo(m, null);
            WifiP2pServiceImpl.this.mClientInfoList.put(m, clientInfo);
            return clientInfo;
        }

        private void enableBTCoex() {
            if (this.mIsBTCoexDisabled) {
                this.mWifiNative.setBluetoothCoexistenceMode(WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_SENSE);
                this.mIsBTCoexDisabled = WifiP2pServiceImpl.HWDBG;
            }
        }

        private String getSsidPostFix(String deviceName) {
            String ssidPostFix = deviceName;
            if (deviceName != null) {
                byte[] ssidPostFixBytes = deviceName.getBytes();
                while (ssidPostFixBytes.length > 22) {
                    ssidPostFix = ssidPostFix.substring(WifiP2pServiceImpl.DISABLED, ssidPostFix.length() - 1);
                    ssidPostFixBytes = ssidPostFix.getBytes();
                }
            }
            return ssidPostFix;
        }
    }

    public enum P2pStatus {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.p2p.WifiP2pServiceImpl.P2pStatus.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.p2p.WifiP2pServiceImpl.P2pStatus.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.p2p.WifiP2pServiceImpl.P2pStatus.<clinit>():void");
        }

        public static P2pStatus valueOf(int error) {
            switch (error) {
                case WifiP2pServiceImpl.DISABLED /*0*/:
                    return SUCCESS;
                case WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_DISABLED /*1*/:
                    return INFORMATION_IS_CURRENTLY_UNAVAILABLE;
                case WifiP2pServiceImpl.P2P_BLUETOOTH_COEXISTENCE_MODE_SENSE /*2*/:
                    return INCOMPATIBLE_PARAMETERS;
                case Extension.TYPE_INT64 /*3*/:
                    return LIMIT_REACHED;
                case Extension.TYPE_UINT64 /*4*/:
                    return INVALID_PARAMETER;
                case Extension.TYPE_INT32 /*5*/:
                    return UNABLE_TO_ACCOMMODATE_REQUEST;
                case Extension.TYPE_FIXED64 /*6*/:
                    return PREVIOUS_PROTOCOL_ERROR;
                case WifiP2pServiceImpl.P2P_DEVICE_OF_MIRACAST /*7*/:
                    return NO_COMMON_CHANNEL;
                case Extension.TYPE_BOOL /*8*/:
                    return UNKNOWN_P2P_GROUP;
                case Extension.TYPE_STRING /*9*/:
                    return BOTH_GO_INTENT_15;
                case WifiP2pServiceImpl.GROUP_IDLE_TIME_S /*10*/:
                    return INCOMPATIBLE_PROVISIONING_METHOD;
                case Extension.TYPE_MESSAGE /*11*/:
                    return REJECTED_BY_USER;
                default:
                    return UNKNOWN;
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.p2p.WifiP2pServiceImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.p2p.WifiP2pServiceImpl.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.p2p.WifiP2pServiceImpl.<clinit>():void");
    }

    public WifiP2pServiceImpl(Context context) {
        this.mReplyChannel = new AsyncChannel();
        this.mWifiChannel = null;
        this.mThisDevice = new WifiP2pDevice();
        this.mWifiP2pServiceHisiExt = null;
        this.mIsInvite = HWDBG;
        this.mDiscoveryPostponed = HWDBG;
        this.mTemporarilyDisconnectedWifi = HWDBG;
        this.mCreateWifiBridge = HWDBG;
        this.mServiceTransactionId = (byte) 0;
        this.mClientInfoList = new HashMap();
        this.mContext = context;
        this.mNetworkInfo = new NetworkInfo(13, DISABLED, NETWORKTYPE, "");
        this.mP2pSupported = this.mContext.getPackageManager().hasSystemFeature("android.hardware.wifi.direct");
        this.mThisDevice.primaryDeviceType = this.mContext.getResources().getString(17039415);
        if (WifiP2pServiceHisiExt.hisiWifiEnabled()) {
            this.mWifiP2pServiceHisiExt = new WifiP2pServiceHisiExt(this.mContext, this.mThisDevice, this.mWifiChannel, this.mNetworkInfo);
        }
        HandlerThread wifiP2pThread = new HandlerThread(TAG);
        wifiP2pThread.start();
        this.mClientHandler = new ClientHandler(wifiP2pThread.getLooper());
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
        if (SystemProperties.getBoolean("ro.config.hw_wifibridge", HWDBG)) {
            initWifiRepeaterConfig();
        }
    }

    public boolean setWifiP2pEnabled(int p2pFlag) {
        if (WifiP2pServiceHisiExt.hisiWifiEnabled()) {
            return this.mWifiP2pServiceHisiExt.setWifiP2pEnabled(p2pFlag);
        }
        return HWDBG;
    }

    public boolean isWifiP2pEnabled() {
        if (WifiP2pServiceHisiExt.hisiWifiEnabled()) {
            return this.mWifiP2pServiceHisiExt.isWifiP2pEnabled();
        }
        return HWDBG;
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
            this.mIpManager.stop();
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
        this.mP2pStateMachine.sendMessage(SET_MIRACAST_MODE, mode);
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
}
