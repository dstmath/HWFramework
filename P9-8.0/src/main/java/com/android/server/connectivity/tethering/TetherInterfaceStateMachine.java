package com.android.server.connectivity.tethering;

import android.net.INetworkStatsService;
import android.net.InterfaceConfiguration;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.NetworkUtils;
import android.net.util.SharedLog;
import android.os.INetworkManagementService;
import android.os.Looper;
import android.os.Message;
import android.util.SparseArray;
import com.android.internal.util.MessageUtils;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;

public class TetherInterfaceStateMachine extends StateMachine {
    private static final int BASE_IFACE = 327780;
    public static final int CMD_INTERFACE_DOWN = 327784;
    public static final int CMD_IPV6_TETHER_UPDATE = 327793;
    public static final int CMD_IP_FORWARDING_DISABLE_ERROR = 327788;
    public static final int CMD_IP_FORWARDING_ENABLE_ERROR = 327787;
    public static final int CMD_SET_DNS_FORWARDERS_ERROR = 327791;
    public static final int CMD_START_TETHERING_ERROR = 327789;
    public static final int CMD_STOP_TETHERING_ERROR = 327790;
    public static final int CMD_TETHER_CONNECTION_CHANGED = 327792;
    public static final int CMD_TETHER_REQUESTED = 327782;
    public static final int CMD_TETHER_UNREQUESTED = 327783;
    private static final boolean DBG = false;
    private static final String TAG = "TetherInterfaceSM";
    private static final String USB_NEAR_IFACE_ADDR = "192.168.42.129";
    private static final int USB_PREFIX_LENGTH = 24;
    private static final boolean VDBG = false;
    private static final String WIFI_HOST_IFACE_ADDR = "192.168.43.1";
    private static final int WIFI_HOST_IFACE_PREFIX_LENGTH = 24;
    private static final Class[] messageClasses = new Class[]{TetherInterfaceStateMachine.class};
    private static final SparseArray<String> sMagicDecoderRing = MessageUtils.findMessageNames(messageClasses);
    private final IPv6TetheringInterfaceServices mIPv6TetherSvc;
    private final String mIfaceName;
    private final State mInitialState = new InitialState();
    private final int mInterfaceType;
    private int mLastError = 0;
    private final State mLocalHotspotState = new LocalHotspotState();
    private final SharedLog mLog;
    private String mMyUpstreamIfaceName;
    private final INetworkManagementService mNMService;
    private final INetworkStatsService mStatsService;
    private final IControlsTethering mTetherController;
    private final State mTetheredState = new TetheredState();
    private final State mUnavailableState = new UnavailableState();

    class BaseServingState extends State {
        BaseServingState() {
        }

        public void enter() {
            if (TetherInterfaceStateMachine.this.configureIfaceIp(true)) {
                try {
                    TetherInterfaceStateMachine.this.mNMService.tetherInterface(TetherInterfaceStateMachine.this.mIfaceName);
                    if (!TetherInterfaceStateMachine.this.mIPv6TetherSvc.start()) {
                        TetherInterfaceStateMachine.this.mLog.e("Failed to start IPv6TetheringInterfaceServices");
                        return;
                    }
                    return;
                } catch (Exception e) {
                    TetherInterfaceStateMachine.this.mLog.e("Error Tethering: " + e);
                    TetherInterfaceStateMachine.this.mLastError = 6;
                    return;
                }
            }
            TetherInterfaceStateMachine.this.mLastError = 10;
        }

        public void exit() {
            TetherInterfaceStateMachine.this.mIPv6TetherSvc.stop();
            try {
                TetherInterfaceStateMachine.this.mNMService.untetherInterface(TetherInterfaceStateMachine.this.mIfaceName);
            } catch (Exception e) {
                TetherInterfaceStateMachine.this.mLastError = 7;
                TetherInterfaceStateMachine.this.mLog.e("Failed to untether interface: " + e);
            }
            TetherInterfaceStateMachine.this.configureIfaceIp(false);
        }

        public boolean processMessage(Message message) {
            TetherInterfaceStateMachine.this.maybeLogMessage(this, message.what);
            switch (message.what) {
                case TetherInterfaceStateMachine.CMD_TETHER_UNREQUESTED /*327783*/:
                    TetherInterfaceStateMachine.this.transitionTo(TetherInterfaceStateMachine.this.mInitialState);
                    break;
                case TetherInterfaceStateMachine.CMD_INTERFACE_DOWN /*327784*/:
                    TetherInterfaceStateMachine.this.transitionTo(TetherInterfaceStateMachine.this.mUnavailableState);
                    break;
                case TetherInterfaceStateMachine.CMD_IP_FORWARDING_ENABLE_ERROR /*327787*/:
                case TetherInterfaceStateMachine.CMD_IP_FORWARDING_DISABLE_ERROR /*327788*/:
                case TetherInterfaceStateMachine.CMD_START_TETHERING_ERROR /*327789*/:
                case TetherInterfaceStateMachine.CMD_STOP_TETHERING_ERROR /*327790*/:
                case TetherInterfaceStateMachine.CMD_SET_DNS_FORWARDERS_ERROR /*327791*/:
                    TetherInterfaceStateMachine.this.mLastError = 5;
                    TetherInterfaceStateMachine.this.transitionTo(TetherInterfaceStateMachine.this.mInitialState);
                    break;
                case TetherInterfaceStateMachine.CMD_IPV6_TETHER_UPDATE /*327793*/:
                    TetherInterfaceStateMachine.this.mIPv6TetherSvc.updateUpstreamIPv6LinkProperties((LinkProperties) message.obj);
                    break;
                default:
                    return false;
            }
            return true;
        }
    }

    class InitialState extends State {
        InitialState() {
        }

        public void enter() {
            TetherInterfaceStateMachine.this.sendInterfaceState(1);
        }

        public boolean processMessage(Message message) {
            TetherInterfaceStateMachine.this.maybeLogMessage(this, message.what);
            switch (message.what) {
                case TetherInterfaceStateMachine.CMD_TETHER_REQUESTED /*327782*/:
                    TetherInterfaceStateMachine.this.mLastError = 0;
                    switch (message.arg1) {
                        case 2:
                            TetherInterfaceStateMachine.this.transitionTo(TetherInterfaceStateMachine.this.mTetheredState);
                            return true;
                        case 3:
                            TetherInterfaceStateMachine.this.transitionTo(TetherInterfaceStateMachine.this.mLocalHotspotState);
                            return true;
                        default:
                            TetherInterfaceStateMachine.this.mLog.e("Invalid tethering interface serving state specified.");
                            return true;
                    }
                case TetherInterfaceStateMachine.CMD_INTERFACE_DOWN /*327784*/:
                    TetherInterfaceStateMachine.this.transitionTo(TetherInterfaceStateMachine.this.mUnavailableState);
                    return true;
                case TetherInterfaceStateMachine.CMD_IPV6_TETHER_UPDATE /*327793*/:
                    TetherInterfaceStateMachine.this.mIPv6TetherSvc.updateUpstreamIPv6LinkProperties((LinkProperties) message.obj);
                    return true;
                default:
                    return false;
            }
        }
    }

    class LocalHotspotState extends BaseServingState {
        LocalHotspotState() {
            super();
        }

        public void enter() {
            super.enter();
            if (TetherInterfaceStateMachine.this.mLastError != 0) {
                TetherInterfaceStateMachine.this.transitionTo(TetherInterfaceStateMachine.this.mInitialState);
            }
            TetherInterfaceStateMachine.this.sendInterfaceState(3);
        }

        public boolean processMessage(Message message) {
            if (super.processMessage(message)) {
                return true;
            }
            TetherInterfaceStateMachine.this.maybeLogMessage(this, message.what);
            switch (message.what) {
                case TetherInterfaceStateMachine.CMD_TETHER_REQUESTED /*327782*/:
                    TetherInterfaceStateMachine.this.mLog.e("CMD_TETHER_REQUESTED while in local-only hotspot mode.");
                    break;
                case TetherInterfaceStateMachine.CMD_TETHER_CONNECTION_CHANGED /*327792*/:
                    break;
                default:
                    return false;
            }
            return true;
        }
    }

    class TetheredState extends BaseServingState {
        TetheredState() {
            super();
        }

        public void enter() {
            super.enter();
            if (TetherInterfaceStateMachine.this.mLastError != 0) {
                TetherInterfaceStateMachine.this.transitionTo(TetherInterfaceStateMachine.this.mInitialState);
            }
            TetherInterfaceStateMachine.this.sendInterfaceState(2);
        }

        public void exit() {
            cleanupUpstream();
            super.exit();
        }

        private void cleanupUpstream() {
            if (TetherInterfaceStateMachine.this.mMyUpstreamIfaceName != null) {
                cleanupUpstreamInterface(TetherInterfaceStateMachine.this.mMyUpstreamIfaceName);
                TetherInterfaceStateMachine.this.mMyUpstreamIfaceName = null;
            }
        }

        private void cleanupUpstreamInterface(String upstreamIface) {
            try {
                TetherInterfaceStateMachine.this.mStatsService.forceUpdate();
            } catch (Exception e) {
            }
            try {
                TetherInterfaceStateMachine.this.mNMService.stopInterfaceForwarding(TetherInterfaceStateMachine.this.mIfaceName, upstreamIface);
            } catch (Exception e2) {
            }
            try {
                TetherInterfaceStateMachine.this.mNMService.disableNat(TetherInterfaceStateMachine.this.mIfaceName, upstreamIface);
            } catch (Exception e3) {
            }
        }

        public boolean processMessage(Message message) {
            if (super.processMessage(message)) {
                return true;
            }
            TetherInterfaceStateMachine.this.maybeLogMessage(this, message.what);
            boolean retValue = true;
            switch (message.what) {
                case TetherInterfaceStateMachine.CMD_TETHER_REQUESTED /*327782*/:
                    TetherInterfaceStateMachine.this.mLog.e("CMD_TETHER_REQUESTED while already tethering.");
                    break;
                case TetherInterfaceStateMachine.CMD_TETHER_CONNECTION_CHANGED /*327792*/:
                    String newUpstreamIfaceName = message.obj;
                    if (!(TetherInterfaceStateMachine.this.mMyUpstreamIfaceName == null && newUpstreamIfaceName == null) && (TetherInterfaceStateMachine.this.mMyUpstreamIfaceName == null || !TetherInterfaceStateMachine.this.mMyUpstreamIfaceName.equals(newUpstreamIfaceName))) {
                        cleanupUpstream();
                        if (newUpstreamIfaceName != null) {
                            try {
                                TetherInterfaceStateMachine.this.mNMService.enableNat(TetherInterfaceStateMachine.this.mIfaceName, newUpstreamIfaceName);
                                TetherInterfaceStateMachine.this.mNMService.startInterfaceForwarding(TetherInterfaceStateMachine.this.mIfaceName, newUpstreamIfaceName);
                            } catch (Exception e) {
                                TetherInterfaceStateMachine.this.mLog.e("Exception enabling NAT: " + e);
                                cleanupUpstreamInterface(newUpstreamIfaceName);
                                TetherInterfaceStateMachine.this.mLastError = 8;
                                TetherInterfaceStateMachine.this.transitionTo(TetherInterfaceStateMachine.this.mInitialState);
                                return true;
                            }
                        }
                        TetherInterfaceStateMachine.this.mMyUpstreamIfaceName = newUpstreamIfaceName;
                        break;
                    }
                default:
                    retValue = false;
                    break;
            }
            return retValue;
        }
    }

    class UnavailableState extends State {
        UnavailableState() {
        }

        public void enter() {
            TetherInterfaceStateMachine.this.mLastError = 0;
            TetherInterfaceStateMachine.this.sendInterfaceState(0);
        }
    }

    public TetherInterfaceStateMachine(String ifaceName, Looper looper, int interfaceType, SharedLog log, INetworkManagementService nMService, INetworkStatsService statsService, IControlsTethering tetherController, IPv6TetheringInterfaceServices ipv6Svc) {
        super(ifaceName, looper);
        this.mLog = log.forSubComponent(ifaceName);
        this.mNMService = nMService;
        this.mStatsService = statsService;
        this.mTetherController = tetherController;
        this.mIfaceName = ifaceName;
        this.mInterfaceType = interfaceType;
        this.mIPv6TetherSvc = ipv6Svc;
        addState(this.mInitialState);
        addState(this.mLocalHotspotState);
        addState(this.mTetheredState);
        addState(this.mUnavailableState);
        setInitialState(this.mInitialState);
    }

    public String interfaceName() {
        return this.mIfaceName;
    }

    public int interfaceType() {
        return this.mInterfaceType;
    }

    public int lastError() {
        return this.mLastError;
    }

    public void stop() {
        sendMessage(CMD_INTERFACE_DOWN);
    }

    public void unwanted() {
        sendMessage(CMD_TETHER_UNREQUESTED);
    }

    private boolean configureIfaceIp(boolean enabled) {
        String ipAsString;
        int prefixLen;
        if (this.mInterfaceType == 1) {
            ipAsString = USB_NEAR_IFACE_ADDR;
            prefixLen = 24;
        } else if (this.mInterfaceType != 0) {
            return true;
        } else {
            ipAsString = WIFI_HOST_IFACE_ADDR;
            prefixLen = 24;
        }
        try {
            InterfaceConfiguration ifcg = this.mNMService.getInterfaceConfig(this.mIfaceName);
            if (ifcg != null) {
                ifcg.setLinkAddress(new LinkAddress(NetworkUtils.numericToInetAddress(ipAsString), prefixLen));
                if (this.mInterfaceType == 0) {
                    ifcg.ignoreInterfaceUpDownStatus();
                } else if (enabled) {
                    ifcg.setInterfaceUp();
                } else {
                    ifcg.setInterfaceDown();
                }
                ifcg.clearFlag("running");
                this.mNMService.setInterfaceConfig(this.mIfaceName, ifcg);
            }
            return true;
        } catch (Exception e) {
            this.mLog.e("Error configuring interface " + e);
            return false;
        }
    }

    private void maybeLogMessage(State state, int what) {
    }

    private void sendInterfaceState(int newInterfaceState) {
        this.mTetherController.notifyInterfaceStateChange(this.mIfaceName, this, newInterfaceState, this.mLastError);
    }
}
