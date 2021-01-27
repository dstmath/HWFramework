package com.android.server.wifi.dc;

import android.content.Context;
import android.net.wifi.IWifiActionListener;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManagerUtils;
import android.os.Bundle;
import android.os.Handler;
import android.os.INetworkManagementService;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.wifi.HwHiLog;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.wifi.ClientModeImpl;
import com.android.server.wifi.HwWifiServiceManager;
import com.android.server.wifi.HwWifiServiceManagerImpl;
import com.android.server.wifi.WifiConfigurationUtil;
import com.android.server.wifi.WifiInjector;
import com.android.server.wifi.WifiNative;
import com.android.server.wifi.p2p.WifiP2pNative;
import com.android.server.wifi.p2p.WifiP2pServiceImpl;
import com.android.server.wifi.util.NativeUtil;
import com.huawei.utils.reflect.EasyInvokeFactory;
import java.util.Locale;

public class DcController extends StateMachine {
    private static final int DC_CMD_SET_STATE = 116;
    private static final int DC_CONNECTED = 2;
    private static final int DC_CONNECTING = 1;
    private static final int DC_DISCONNECTED = 4;
    private static final int DC_DISCONNECTING = 3;
    private static final int DC_STATE_DISABLED = 0;
    private static final int DC_STATE_ENABLED = 1;
    private static final int MAGICLINK_CONNECT_RETRY_DELAY_TIME_MSEC = 3000;
    private static final int MAGICLINK_CONNECT_RETRY_LIMIT_TIMES = 3;
    private static final int MAGICLINK_GROUP_CREATING_WAIT_TIME_MS = 20000;
    private static final String ONEHOP_TV_MIRACAST_PACKAGE_NAME = "com.huawei.pcassistant";
    private static final String P2P_TETHER_IFAC_110X = "p2p-p2p0-";
    private static final String TAG = "DcController";
    private static DcController sDcController = null;
    private static WifiP2pManagerUtils sWifiP2pManagerUtils = EasyInvokeFactory.getInvokeUtils(WifiP2pManagerUtils.class);
    private WifiP2pManager.Channel mChannel;
    private WifiP2pManager.ChannelListener mChannelListener = new WifiP2pManager.ChannelListener() {
        /* class com.android.server.wifi.dc.DcController.AnonymousClass3 */

        @Override // android.net.wifi.p2p.WifiP2pManager.ChannelListener
        public void onChannelDisconnected() {
            DcController.this.mChannel = null;
            DcController.this.initWifiP2pChannel();
            HwHiLog.i(DcController.TAG, false, "Wifi p2p channel is disconnected.", new Object[0]);
        }
    };
    private Context mContext;
    private DcArbitra mDcArbitra;
    private DcChr mDcChr;
    private DcConnectedState mDcConnectedState = new DcConnectedState();
    private DcConnectingState mDcConnectingState = new DcConnectingState();
    private DcDisconnectingState mDcDisconnectingState = new DcDisconnectingState();
    private DcInActiveState mDcInActiveState = new DcInActiveState();
    private DcJniAdapter mDcJniAdapter;
    private DefaultState mDefaultState = new DefaultState();
    private String mGcIf = "";
    private Handler mHandler;
    private boolean mIsDcConfigGot = false;
    private boolean mIsDcConnectByApp = false;
    private boolean mIsDcConnecting = false;
    private boolean mIsMagiclinkConnected = false;
    private IWifiActionListener mListener = null;
    private WifiP2pManager.ActionListener mMagiclinkConnectListener = new WifiP2pManager.ActionListener() {
        /* class com.android.server.wifi.dc.DcController.AnonymousClass1 */

        @Override // android.net.wifi.p2p.WifiP2pManager.ActionListener
        public void onSuccess() {
            HwHiLog.d(DcController.TAG, false, "Start magiclinkConnect success.", new Object[0]);
            DcController dcController = DcController.this;
            dcController.notifyListenerOnSuccess(dcController.mListener);
        }

        @Override // android.net.wifi.p2p.WifiP2pManager.ActionListener
        public void onFailure(int reason) {
            DcController.this.mIsMagiclinkConnected = false;
            HwHiLog.i(DcController.TAG, false, "Start magiclinkConnect failed, error code = %{public}d", new Object[]{Integer.valueOf(reason)});
            DcController dcController = DcController.this;
            dcController.notifyListenerOnFailure(dcController.mListener, reason);
            if (reason == 2) {
                HwHiLog.i(DcController.TAG, false, "have other p2p service, conflict ", new Object[0]);
                DcController.this.sendMessage(11);
                DcHilinkController.getInstance().getDcHilinkHandler().sendEmptyMessage(11);
                return;
            }
            DcController.this.sendMessageDelayed(10, 3000);
        }
    };
    private int mMagiclinkConnectRetryTimes = 0;
    private INetworkManagementService mNmService;
    private WifiP2pServiceImpl.IP2pNotDhcpCallback mP2pNotDhcpCallback = new WifiP2pServiceImpl.IP2pNotDhcpCallback() {
        /* class com.android.server.wifi.dc.DcController.AnonymousClass4 */

        public void onP2pConnected(String interfaceName) {
            HwHiLog.d(DcController.TAG, false, "notifyDCP2pConnected", new Object[0]);
            if (TextUtils.isEmpty(interfaceName)) {
                HwHiLog.e(DcController.TAG, false, "p2p interface is empty", new Object[0]);
                return;
            }
            if (!interfaceName.equals(DcController.this.mGcIf)) {
                DcController.this.mGcIf = interfaceName;
            }
            DcHilinkController.getInstance().handleP2pConnected(DcController.this.mGcIf);
            DcController.this.sendMessage(32);
        }

        public boolean isP2pNotDhcpRunning() {
            return DcController.this.mIsDcConnecting;
        }
    };
    private WifiP2pManager.ActionListener mRemoveGcGroupListener = new WifiP2pManager.ActionListener() {
        /* class com.android.server.wifi.dc.DcController.AnonymousClass2 */

        @Override // android.net.wifi.p2p.WifiP2pManager.ActionListener
        public void onSuccess() {
            if (DcController.this.mIsMagiclinkConnected) {
                DcController.this.startOrStopHiP2p(false);
                DcHilinkController.getInstance().getDcHilinkHandler().sendEmptyMessage(27);
            }
            DcController.this.mIsMagiclinkConnected = false;
            DcController.this.mGcIf = "";
            HwHiLog.i(DcController.TAG, false, "Start MagiclinkRemoveGcGroup success.", new Object[0]);
            DcController.this.sendMessage(12);
        }

        @Override // android.net.wifi.p2p.WifiP2pManager.ActionListener
        public void onFailure(int reason) {
            DcController.this.removeGcGroup();
            DcController.this.mGcIf = "";
            HwHiLog.i(DcController.TAG, false, "Start MagiclinkRemoveGcGroup failed, error code=%{public}d", new Object[]{Integer.valueOf(reason)});
        }
    };
    private ScConnectedState mScConnectedState = new ScConnectedState();
    private WifiInjector mWifiInjector;
    private WifiManager mWifiManager;

    private DcController(Context context) {
        super(TAG);
        this.mContext = context;
        this.mWifiInjector = WifiInjector.getInstance();
        this.mNmService = INetworkManagementService.Stub.asInterface(ServiceManager.getService("network_management"));
        this.mHandler = getHandler();
        this.mDcArbitra = DcArbitra.createDcArbitra(context);
        this.mDcJniAdapter = DcJniAdapter.getInstance();
        this.mDcChr = DcChr.getInstance();
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        addState(this.mDefaultState);
        addState(this.mDcInActiveState, this.mDefaultState);
        addState(this.mScConnectedState, this.mDcInActiveState);
        addState(this.mDcConnectingState, this.mScConnectedState);
        addState(this.mDcConnectedState, this.mScConnectedState);
        addState(this.mDcDisconnectingState, this.mScConnectedState);
        setInitialState(this.mDcInActiveState);
        start();
    }

    public static DcController createDcController(Context context) {
        if (sDcController == null) {
            sDcController = new DcController(context);
        }
        return sDcController;
    }

    public static DcController getInstance() {
        return sDcController;
    }

    public Handler getDcControllerHandler() {
        return this.mHandler;
    }

    public void handleUpdateScanResults() {
        this.mDcArbitra.updateScanResults();
        sendMessage(30);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void logStateAndMessage(State state, Message message) {
        HwHiLog.d(TAG, false, "%{public}s : handle message: %{public}s", new Object[]{state.getClass().getSimpleName(), DcUtils.getStateAndMessageString(state, message)});
    }

    private void magiclinkConnect(WifiP2pManager.Channel wifiP2pChannel, String config) {
        HwHiLog.d(TAG, false, "DC magiclinkConnect enter", new Object[0]);
        Bundle bundle = new Bundle();
        bundle.putString("cfg", config);
        wifiP2pChannel.getAsyncChannel().sendMessage(141269, 0, sWifiP2pManagerUtils.putListener(wifiP2pChannel, this.mMagiclinkConnectListener), bundle);
    }

    private void magiclinkRemoveGcGroup(WifiP2pManager.Channel wifiP2pChannel, String iface) {
        HwHiLog.d(TAG, false, "magiclinkRemoveGcGroup enter", new Object[0]);
        Bundle bundle = new Bundle();
        bundle.putString("iface", iface);
        wifiP2pChannel.getAsyncChannel().sendMessage(141271, 0, sWifiP2pManagerUtils.putListener(wifiP2pChannel, this.mRemoveGcGroupListener), bundle);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initWifiP2pChannel() {
        WifiP2pManager wifiP2pManager = (WifiP2pManager) this.mContext.getSystemService("wifip2p");
        if (this.mChannel == null && wifiP2pManager != null) {
            Context context = this.mContext;
            this.mChannel = wifiP2pManager.initialize(context, context.getMainLooper(), this.mChannelListener);
            if (this.mChannel == null) {
                HwHiLog.w(TAG, false, "mWifiP2pManager initialize failed, channel is null", new Object[0]);
            }
        }
    }

    public void startMagiclinkConnect(DcConfiguration selectedDcConfig) {
        WifiManager wifiManager;
        if (this.mIsMagiclinkConnected) {
            HwHiLog.i(TAG, false, "magiclinkConnected, abort", new Object[0]);
            return;
        }
        initWifiP2pChannel();
        if (this.mChannel == null || (wifiManager = this.mWifiManager) == null) {
            HwHiLog.e(TAG, false, "mChannel or mWifiManager is null", new Object[0]);
            return;
        }
        String wifiMacAddr = wifiManager.getConnectionInfo().getMacAddress();
        if (TextUtils.isEmpty(wifiMacAddr)) {
            HwHiLog.e(TAG, false, "get_wifi_mac_address is empty", new Object[0]);
            return;
        }
        String p2pMacAddr = DcUtils.wifiAddr2p2pAddr(wifiMacAddr);
        if (TextUtils.isEmpty(p2pMacAddr)) {
            HwHiLog.e(TAG, false, "get_p2p_mac_address is empty", new Object[0]);
            return;
        }
        this.mMagiclinkConnectRetryTimes++;
        String connectInfo = selectedDcConfig.getSsid() + "\n" + selectedDcConfig.getBssid() + "\n" + selectedDcConfig.getPreSharedKey() + "\n" + selectedDcConfig.getFrequency() + "\n1\n1\n" + p2pMacAddr;
        HwHiLog.d(TAG, false, "startMagiclinkConnect %{private}s", new Object[]{connectInfo});
        magiclinkConnect(this.mChannel, connectInfo);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyListenerOnSuccess(IWifiActionListener listener) {
        if (listener != null) {
            try {
                listener.onSuccess();
            } catch (RemoteException e) {
                HwHiLog.e(TAG, false, "Exceptions happen at MagiclinkConnectListener onSuccess", new Object[0]);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyListenerOnFailure(IWifiActionListener listener, int reason) {
        if (listener != null) {
            try {
                listener.onFailure(reason);
            } catch (RemoteException e) {
                HwHiLog.e(TAG, false, "Exceptions happen when notufy listener OnFailure", new Object[0]);
            }
        }
    }

    public void dcConnect(WifiConfiguration configuration, IWifiActionListener listener) {
        if (this.mIsDcConnectByApp) {
            HwHiLog.i(TAG, false, "dcConnect is calling by other app", new Object[0]);
            notifyListenerOnFailure(listener, 2);
            return;
        }
        DcHilinkController dcHilinkController = DcHilinkController.getInstance();
        DcArbitra dcArbitra = DcArbitra.getInstance();
        if (this.mWifiManager == null || dcArbitra == null || dcHilinkController == null || configuration == null || configuration.BSSID == null || !WifiConfigurationUtil.validate(configuration, true) || !dcHilinkController.isWifiAndP2pStateAllowDc()) {
            HwHiLog.w(TAG, false, "dcConnect: do not allow APP to connect DC", new Object[0]);
            notifyListenerOnFailure(listener, 0);
            return;
        }
        int dcFrequency = dcArbitra.getFrequencyForBssid(configuration.BSSID);
        WifiInfo wifiInfo = this.mWifiManager.getConnectionInfo();
        if (wifiInfo == null || ((ScanResult.is24GHz(wifiInfo.getFrequency()) && ScanResult.is24GHz(dcFrequency)) || (ScanResult.is5GHz(wifiInfo.getFrequency()) && ScanResult.is5GHz(dcFrequency)))) {
            HwHiLog.w(TAG, false, "dcConnect: dcFrequency is the same with wifi, return", new Object[0]);
            notifyListenerOnFailure(listener, 1);
            return;
        }
        String securityType = configuration.getSsidAndSecurityTypeString().substring(configuration.SSID.length());
        if (WifiConfiguration.KeyMgmt.strings[0].equals(securityType) || WifiConfiguration.KeyMgmt.strings[1].equals(securityType) || WifiConfiguration.KeyMgmt.strings[4].equals(securityType)) {
            initWifiP2pChannel();
            if (this.mChannel == null) {
                HwHiLog.e(TAG, false, "mChannel is null", new Object[0]);
                notifyListenerOnFailure(listener, 0);
                return;
            }
            String preSharedKey = "";
            if (!WifiConfiguration.KeyMgmt.strings[0].equals(securityType)) {
                preSharedKey = NativeUtil.removeEnclosingQuotes(configuration.preSharedKey);
            }
            String connectInfo = NativeUtil.removeEnclosingQuotes(configuration.SSID) + "\n" + configuration.BSSID.toLowerCase(Locale.ROOT) + "\n" + preSharedKey + "\n" + dcFrequency + "\n1";
            HwHiLog.i(TAG, false, "dcConnect %{private}s", new Object[]{connectInfo});
            magiclinkConnect(this.mChannel, connectInfo);
            this.mIsDcConnectByApp = true;
            this.mListener = listener;
            sendMessage(35);
            return;
        }
        HwHiLog.e(TAG, false, "dcConnect: AuthType is not allowed", new Object[0]);
        notifyListenerOnFailure(listener, 0);
    }

    public boolean isWifiDcActive() {
        return getCurrentState() == this.mDcConnectedState;
    }

    public boolean dcDisconnect() {
        WifiP2pManager.Channel channel;
        this.mListener = null;
        String dcIface = getDcInterface();
        if (TextUtils.isEmpty(dcIface) || (channel = this.mChannel) == null) {
            HwHiLog.i(TAG, false, "dcDisconnect abort", new Object[0]);
            return false;
        }
        magiclinkRemoveGcGroup(channel, dcIface);
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void removeGcGroup() {
        if (this.mIsMagiclinkConnected || !TextUtils.isEmpty(this.mGcIf)) {
            WifiP2pManager.Channel channel = this.mChannel;
            if (channel == null) {
                HwHiLog.e(TAG, false, "mChannel is null", new Object[0]);
            } else {
                magiclinkRemoveGcGroup(channel, this.mGcIf);
            }
        } else {
            HwHiLog.i(TAG, false, "magiclinkDisconnected, abort", new Object[0]);
            DcHilinkController.getInstance().getDcHilinkHandler().sendEmptyMessage(12);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isDcP2pConnectStart(DcConfiguration dcSelectedNetwork) {
        if (dcSelectedNetwork == null) {
            HwHiLog.d(TAG, false, "no network to start DC", new Object[0]);
            this.mIsDcConfigGot = true;
            if (!this.mDcArbitra.isValidDcConfigSaved()) {
                DcHilinkController.getInstance().getDcHilinkHandler().sendEmptyMessage(31);
            }
            return false;
        }
        this.mDcChr.uploadDcConnectTotalCount();
        this.mDcChr.setDcConnectStartTime(SystemClock.elapsedRealtime());
        HwWifiServiceManager hwWifiServiceManager = HwWifiServiceManagerImpl.getDefault();
        if (hwWifiServiceManager instanceof HwWifiServiceManagerImpl) {
            this.mIsDcConnecting = true;
            ((HwWifiServiceManagerImpl) hwWifiServiceManager).getHwWifiP2pService().registerP2pNotDhcpCallback(this.mP2pNotDhcpCallback);
        }
        this.mDcChr.setDcP2pConnectStartTime(SystemClock.elapsedRealtime());
        startMagiclinkConnect(dcSelectedNetwork);
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startOrStopHiP2p(boolean isEnable) {
        String wifiInterface = SystemProperties.get("wifi.interface", "wlan0");
        HwHiLog.i(TAG, false, "wifiInterface=%{public}s p2pInterface=%{public}s enable=%{public}s", new Object[]{wifiInterface, this.mGcIf, Boolean.valueOf(isEnable)});
        if (!TextUtils.isEmpty(wifiInterface) && !TextUtils.isEmpty(this.mGcIf)) {
            this.mDcJniAdapter.startOrStopHiP2p(wifiInterface, this.mGcIf, isEnable);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setDcState(int state) {
        HwHiLog.d(TAG, false, "dcState = %{public}d", new Object[]{Integer.valueOf(state)});
        String wifiInterface = SystemProperties.get("wifi.interface", "wlan0");
        if (this.mWifiInjector == null) {
            this.mWifiInjector = WifiInjector.getInstance();
        }
        WifiNative wifiNative = this.mWifiInjector.getWifiNative();
        if (wifiNative == null || wifiNative.mHwWifiNativeEx == null) {
            HwHiLog.e(TAG, false, "wifiNative or mHwWifiNativeEx is null", new Object[0]);
        } else if (wifiNative.mHwWifiNativeEx.sendCmdToDriver(wifiInterface, 116, new byte[]{(byte) state}) < 0) {
            HwHiLog.e(TAG, false, "set dc enable state command error", new Object[0]);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setWifiApType(int type) {
        WifiInfo wifiInfo;
        if (this.mWifiInjector == null) {
            this.mWifiInjector = WifiInjector.getInstance();
        }
        ClientModeImpl clientModeImpl = this.mWifiInjector.getClientModeImpl();
        if (clientModeImpl != null && (wifiInfo = clientModeImpl.getWifiInfo()) != null) {
            wifiInfo.setWifiApType(type);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String getDcInterface() {
        INetworkManagementService iNetworkManagementService = this.mNmService;
        if (iNetworkManagementService == null) {
            HwHiLog.e(TAG, false, "mNmService is null", new Object[0]);
            return "";
        }
        try {
            String[] ifaces = iNetworkManagementService.listInterfaces();
            if (ifaces != null) {
                for (String iface : ifaces) {
                    if (iface != null && iface.startsWith(P2P_TETHER_IFAC_110X)) {
                        return iface;
                    }
                }
            }
            return "";
        } catch (RemoteException | IllegalStateException e) {
            HwHiLog.e(TAG, false, "Error listing Interfaces", new Object[0]);
            return "";
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void removeGroupAfterCreationFail() {
        String chosenIface = getDcInterface();
        if (TextUtils.isEmpty(chosenIface)) {
            HwHiLog.e(TAG, false, "could not find iface", new Object[0]);
            return;
        }
        HwHiLog.d(TAG, false, "chosenIface is " + chosenIface, new Object[0]);
        if (this.mWifiInjector == null) {
            this.mWifiInjector = WifiInjector.getInstance();
        }
        WifiP2pNative wifiNative = this.mWifiInjector.getWifiP2pNative();
        if (wifiNative == null) {
            HwHiLog.e(TAG, false, "wifiNative is null", new Object[0]);
        } else if (!wifiNative.p2pGroupRemove(chosenIface)) {
            HwHiLog.e(TAG, false, "Failed to remove the P2P group", new Object[0]);
        } else if (!TextUtils.isEmpty(this.mGcIf)) {
            this.mGcIf = "";
        }
    }

    public boolean isDcDisconnectSuccess(String pkgName) {
        if (TextUtils.isEmpty(pkgName) || !ONEHOP_TV_MIRACAST_PACKAGE_NAME.equals(pkgName)) {
            HwHiLog.e(TAG, false, "pkgName Error", new Object[0]);
            return false;
        }
        HwHiLog.d(TAG, false, "pkgName = %{public}s", new Object[]{pkgName});
        if (getCurrentState() == this.mDcConnectedState) {
            removeGcGroup();
            return true;
        } else if (getCurrentState() != this.mDcConnectingState) {
            return false;
        } else {
            if (!TextUtils.isEmpty(this.mGcIf)) {
                removeGcGroup();
            } else {
                removeGroupAfterCreationFail();
            }
            return true;
        }
    }

    class DefaultState extends State {
        DefaultState() {
        }

        public void enter() {
            HwHiLog.d(DcController.TAG, false, "%{public}s enter.", new Object[]{getName()});
        }

        public boolean processMessage(Message message) {
            DcController.this.logStateAndMessage(this, message);
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public class DcInActiveState extends State {
        DcInActiveState() {
        }

        public void enter() {
            HwHiLog.d(DcController.TAG, false, "%{public}s enter.", new Object[]{getName()});
            DcController.this.mIsDcConfigGot = false;
            DcController.this.mGcIf = "";
            DcController.this.mIsMagiclinkConnected = false;
            DcController.this.mIsDcConnectByApp = false;
            DcController.this.mListener = null;
        }

        public boolean processMessage(Message message) {
            DcController.this.logStateAndMessage(this, message);
            if (message.what != 0) {
                return true;
            }
            DcController dcController = DcController.this;
            dcController.transitionTo(dcController.mScConnectedState);
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public class ScConnectedState extends State {
        ScConnectedState() {
        }

        public void enter() {
            HwHiLog.d(DcController.TAG, false, "%{public}s enter.", new Object[]{getName()});
            DcController.this.mIsMagiclinkConnected = false;
            DcController.this.mIsDcConnecting = false;
            DcController.this.mIsDcConfigGot = false;
            DcController.this.mIsDcConnectByApp = false;
            DcController.this.mListener = null;
        }

        public boolean processMessage(Message message) {
            DcController.this.logStateAndMessage(this, message);
            DcHilinkController dcHilinkController = DcHilinkController.getInstance();
            int i = message.what;
            if (i != 1) {
                if (!(i == 7 || i == 18)) {
                    if (i != 24) {
                        if (i != 32) {
                            if (i == 35) {
                                DcController dcController = DcController.this;
                                dcController.transitionTo(dcController.mDcConnectingState);
                            } else if (i != 3) {
                                if (i != 4) {
                                    if (i != 29) {
                                        if (i != 30) {
                                            return true;
                                        }
                                        if ((dcHilinkController == null || dcHilinkController.isDcAllowed()) && DcController.this.mIsDcConfigGot && DcController.this.mDcArbitra.isValidDcConfigSaved()) {
                                            if (DcController.this.isDcP2pConnectStart(DcController.this.mDcArbitra.selectDcNetwork())) {
                                                DcController dcController2 = DcController.this;
                                                dcController2.transitionTo(dcController2.mDcConnectingState);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (!TextUtils.isEmpty(DcController.this.mGcIf)) {
                            DcController.this.removeGcGroup();
                        }
                    } else {
                        handleMsgGetDcConfigSucc(dcHilinkController, message);
                    }
                    return true;
                }
                DcController.this.mIsDcConfigGot = false;
                return true;
            }
            DcController dcController3 = DcController.this;
            dcController3.transitionTo(dcController3.mDcInActiveState);
            return true;
        }

        private void handleMsgGetDcConfigSucc(DcHilinkController dcHilinkController, Message message) {
            if (DcController.this.mHandler.hasMessages(24)) {
                DcController.this.mHandler.removeMessages(24);
            }
            if (dcHilinkController == null || dcHilinkController.isDcAllowed()) {
                HwHiLog.d(DcController.TAG, false, "MSG_GET_DC_CONFIG_SUCC", new Object[0]);
                if (message.obj instanceof String) {
                    if (DcController.this.isDcP2pConnectStart(DcController.this.mDcArbitra.selectDcNetworkFromPayload((String) message.obj))) {
                        DcController dcController = DcController.this;
                        dcController.transitionTo(dcController.mDcConnectingState);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public class DcConnectingState extends State {
        DcConnectingState() {
        }

        public void enter() {
            HwHiLog.d(DcController.TAG, false, "%{public}s enter.", new Object[]{getName()});
            DcController.this.mDcChr.uploadDcState(1);
            DcController.this.mIsDcConfigGot = false;
            DcController.this.sendMessageDelayed(143361, 20000);
        }

        /* JADX WARNING: Removed duplicated region for block: B:42:0x00a0  */
        public boolean processMessage(Message message) {
            DcController.this.logStateAndMessage(this, message);
            DcHilinkController dcHilinkController = DcHilinkController.getInstance();
            DcMonitor dcMonitor = DcMonitor.getInstance();
            if (dcMonitor == null || dcHilinkController == null) {
                HwHiLog.e(DcController.TAG, false, "dcMonitor or dcHilinkController is null", new Object[0]);
                return true;
            }
            int i = message.what;
            if (i != 1) {
                if (i != 7) {
                    if (i == 29) {
                        DcController.this.removeGroupAfterCreationFail();
                        DcController dcController = DcController.this;
                        dcController.transitionTo(dcController.mDcDisconnectingState);
                    } else if (i == 32) {
                        handleMsgDcP2pCconnected();
                    } else if (i == 143361) {
                        handleGroupCreatingTimeout(dcHilinkController, dcMonitor);
                    } else if (i != 3) {
                        if (i != 4) {
                            if (i != 5) {
                                if (i == 10) {
                                    handleMsgMagiclinkConnectFail();
                                } else if (i != 11) {
                                    if (i == 14) {
                                        DcController.this.startOrStopHiP2p(true);
                                        DcController dcController2 = DcController.this;
                                        dcController2.transitionTo(dcController2.mDcConnectedState);
                                    } else if (i != 15) {
                                        return true;
                                    } else {
                                        handleMsgRouterHiP2pStartFail();
                                    }
                                }
                            }
                            handleMsgMagiclinkConnectFailP2pConflict();
                        } else {
                            handleMsgP2pConnected();
                        }
                    }
                    return true;
                }
                if (message.what == 7 || !DcController.this.mIsDcConnectByApp) {
                    if (TextUtils.isEmpty(DcController.this.mGcIf)) {
                        DcController.this.removeGroupAfterCreationFail();
                    }
                    handleMsgMagiclinkConnectFailP2pConflict();
                    return true;
                }
                HwHiLog.i(DcController.TAG, false, "Dc Connect By App, do not deal with MSG_GAME_STOP", new Object[0]);
                return true;
            }
            DcController.this.mDcChr.uploadDcConnectWifiDisconnectCount();
            if (message.what == 7) {
            }
            if (TextUtils.isEmpty(DcController.this.mGcIf)) {
            }
            handleMsgMagiclinkConnectFailP2pConflict();
            return true;
        }

        public void exit() {
            DcController.this.mIsDcConnecting = false;
            if (DcController.this.mHandler.hasMessages(143361)) {
                DcController.this.mHandler.removeMessages(143361);
            }
        }

        private void handleGroupCreatingTimeout(DcHilinkController dcHilinkController, DcMonitor dcMonitor) {
            DcController.this.removeGroupAfterCreationFail();
            if (dcMonitor.isWifiConnected()) {
                DcController dcController = DcController.this;
                dcController.transitionTo(dcController.mScConnectedState);
            } else {
                DcController dcController2 = DcController.this;
                dcController2.transitionTo(dcController2.mDcInActiveState);
            }
            dcHilinkController.getDcHilinkHandler().sendEmptyMessage(10);
            if (DcController.this.mIsDcConnectByApp) {
                DcController dcController3 = DcController.this;
                dcController3.notifyListenerOnFailure(dcController3.mListener, 0);
            }
        }

        private void handleMsgDcP2pCconnected() {
            if (!TextUtils.isEmpty(DcController.this.mGcIf)) {
                HwHiLog.d(DcController.TAG, false, "MagiclinkConnected", new Object[0]);
                DcController.this.mIsMagiclinkConnected = true;
                DcController.this.mDcChr.uploadDcP2pConnectDura(SystemClock.elapsedRealtime());
                DcHilinkController.getInstance().getDcHilinkHandler().sendEmptyMessage(25);
                return;
            }
            DcHilinkController.getInstance().getDcHilinkHandler().sendEmptyMessage(11);
            DcController dcController = DcController.this;
            dcController.transitionTo(dcController.mScConnectedState);
        }

        private void handleMsgMagiclinkConnectFailP2pConflict() {
            if (!TextUtils.isEmpty(DcController.this.mGcIf)) {
                DcController.this.removeGcGroup();
            }
            DcController.this.mListener = null;
            if (!DcMonitor.getInstance().isWifiConnected()) {
                DcController dcController = DcController.this;
                dcController.transitionTo(dcController.mDcInActiveState);
                return;
            }
            DcController dcController2 = DcController.this;
            dcController2.transitionTo(dcController2.mScConnectedState);
        }

        private void handleMsgP2pConnected() {
            if (DcController.this.mIsDcConnectByApp) {
                DcController dcController = DcController.this;
                dcController.mGcIf = dcController.getDcInterface();
                DcController dcController2 = DcController.this;
                dcController2.notifyListenerOnSuccess(dcController2.mListener);
                DcController dcController3 = DcController.this;
                dcController3.transitionTo(dcController3.mDcConnectedState);
            }
        }

        private void handleMsgRouterHiP2pStartFail() {
            if (!TextUtils.isEmpty(DcController.this.mGcIf)) {
                DcController.this.removeGcGroup();
            }
            DcController dcController = DcController.this;
            dcController.transitionTo(dcController.mScConnectedState);
        }

        private void handleMsgMagiclinkConnectFail() {
            DcController.this.mDcChr.uploadDcP2pConnectFailCount();
            HwHiLog.d(DcController.TAG, false, "magiclinkconnect fail, retry most 3 times, RetryTimes=%{public}d", new Object[]{Integer.valueOf(DcController.this.mMagiclinkConnectRetryTimes)});
            if (!TextUtils.isEmpty(DcController.this.mGcIf)) {
                DcController.this.removeGcGroup();
            }
            DcController.this.mIsDcConnecting = false;
            if (DcController.this.mMagiclinkConnectRetryTimes <= 3) {
                DcConfiguration dcSelectedNetwork = DcController.this.mDcArbitra.getSelectedDcConfig();
                if (dcSelectedNetwork != null) {
                    DcController.this.mIsDcConnecting = true;
                    DcController.this.startMagiclinkConnect(dcSelectedNetwork);
                    return;
                }
                return;
            }
            DcHilinkController.getInstance().getDcHilinkHandler().sendEmptyMessage(10);
            DcController dcController = DcController.this;
            dcController.transitionTo(dcController.mScConnectedState);
        }
    }

    /* access modifiers changed from: package-private */
    public class DcConnectedState extends State {
        DcConnectedState() {
        }

        public void enter() {
            HwHiLog.d(DcController.TAG, false, "%{public}s enter.", new Object[]{getName()});
            DcController.this.mIsDcConnecting = false;
            DcController.this.mDcChr.uploadDcConnectSuccCount();
            DcController.this.mDcChr.uploadDcConnectDura(SystemClock.elapsedRealtime());
            DcController.this.mDcChr.uploadDcState(2);
            DcController.this.setDcState(1);
            DcController.this.setWifiApType(100);
        }

        public boolean processMessage(Message message) {
            DcController.this.logStateAndMessage(this, message);
            int i = message.what;
            if (i != 1) {
                if (i == 3) {
                    DcController.this.mIsMagiclinkConnected = false;
                    DcController.this.mDcChr.uploadDcState(4);
                    DcController dcController = DcController.this;
                    dcController.transitionTo(dcController.mDcInActiveState);
                } else if (i == 5) {
                    DcController.this.mIsMagiclinkConnected = false;
                    if (DcMonitor.getInstance().isWifiConnected()) {
                        DcController.this.mDcChr.uploadDcAbnormalDisconnectCount();
                    }
                    DcController.this.deferMessage(message);
                    DcController dcController2 = DcController.this;
                    dcController2.transitionTo(dcController2.mDcDisconnectingState);
                } else if (i != 7 && i != 29 && i != 34) {
                    return true;
                } else {
                    if (DcController.this.mIsDcConnectByApp) {
                        HwHiLog.i(DcController.TAG, false, "Dc Connect By App, do not deal with MSG_WIFI_SIGNAL_BAD", new Object[0]);
                    }
                }
                return true;
            }
            DcController.this.removeGcGroup();
            DcController dcController3 = DcController.this;
            dcController3.transitionTo(dcController3.mDcDisconnectingState);
            return true;
        }

        public void exit() {
            DcController.this.setDcState(0);
            DcController.this.startOrStopHiP2p(false);
            DcController.this.mGcIf = "";
            DcController.this.mListener = null;
            DcController.this.setWifiApType(0);
        }
    }

    /* access modifiers changed from: package-private */
    public class DcDisconnectingState extends State {
        DcDisconnectingState() {
        }

        public void enter() {
            HwHiLog.d(DcController.TAG, false, "%{public}s enter.", new Object[]{getName()});
            DcController.this.mDcChr.uploadDcState(3);
        }

        public boolean processMessage(Message message) {
            DcController.this.logStateAndMessage(this, message);
            DcHilinkController dcHilinkController = DcHilinkController.getInstance();
            DcMonitor dcMonitor = DcMonitor.getInstance();
            if (dcMonitor == null || dcHilinkController == null) {
                HwHiLog.e(DcController.TAG, false, "DcDisconnectingState dcMonitor or dcHilinkController is null", new Object[0]);
                return true;
            }
            int i = message.what;
            if (i != 0) {
                if (i == 1 || i == 3) {
                    DcController dcController = DcController.this;
                    dcController.transitionTo(dcController.mDcInActiveState);
                    return true;
                } else if (i == 5 || i == 7) {
                    handleMsgP2pDisconnected(dcHilinkController, dcMonitor);
                    return true;
                } else if (i == 12 || i == 17) {
                    if (dcMonitor.isWifiConnected()) {
                        DcController dcController2 = DcController.this;
                        dcController2.transitionTo(dcController2.mScConnectedState);
                    } else {
                        DcController dcController3 = DcController.this;
                        dcController3.transitionTo(dcController3.mDcInActiveState);
                    }
                    return true;
                } else if (i != 29) {
                    return true;
                }
            }
            DcController dcController4 = DcController.this;
            dcController4.transitionTo(dcController4.mScConnectedState);
            return true;
        }

        public void exit() {
            DcController.this.mDcChr.uploadDcState(4);
        }

        private void handleMsgP2pDisconnected(DcHilinkController dcHilinkController, DcMonitor dcMonitor) {
            if (!DcController.this.mIsMagiclinkConnected) {
                dcHilinkController.getDcHilinkHandler().sendEmptyMessage(12);
                if (dcMonitor.isWifiConnected()) {
                    DcController dcController = DcController.this;
                    dcController.transitionTo(dcController.mScConnectedState);
                    return;
                }
                DcController dcController2 = DcController.this;
                dcController2.transitionTo(dcController2.mDcInActiveState);
                return;
            }
            DcController.this.mIsMagiclinkConnected = false;
            if (dcMonitor.isWifiConnected()) {
                dcHilinkController.getDcHilinkHandler().sendEmptyMessage(27);
                return;
            }
            DcController dcController3 = DcController.this;
            dcController3.transitionTo(dcController3.mDcInActiveState);
        }
    }
}
