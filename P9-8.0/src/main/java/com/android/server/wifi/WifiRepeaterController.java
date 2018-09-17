package com.android.server.wifi;

import android.content.Context;
import android.content.Intent;
import android.net.DhcpInfo;
import android.net.NetworkUtils;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pGroup;
import android.os.INetworkManagementService;
import android.os.INetworkManagementService.Stub;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.util.AsyncChannel;
import com.android.internal.util.State;
import com.android.server.wifi.wifipro.wifiscangenie.WifiScanGenieController;
import java.net.Inet4Address;
import java.util.Calendar;
import java.util.Random;

public class WifiRepeaterController extends WifiRepeater {
    private static final int BAND_2G = 0;
    private static final int BAND_5G = 1;
    private static final int BAND_ERROR = -1;
    private static final int CHANNEL_ERROR = -1;
    private static final int CMD_DOWNSTREAM_NETWORK_TETHERED = 4;
    private static final int CMD_DOWNSTREAM_NETWORK_UNTETHERED = 3;
    private static final int CMD_STOP_TETHERING = 0;
    private static final int CMD_UPSTREAM_NETWORK_CONNECT = 2;
    private static final int CMD_UPSTREAM_NETWORK_DISCONNECT = 1;
    private static final String EXTRA_WIFI_REPEATER_CLIENTS_SIZE = "wifi_repeater_clients_size";
    private static final long HANG_TIMEOUT = 30000;
    private static final int RPT_GATEWAY_MASK = 16777215;
    private static final int RPT_INVALID_INETADDR = 0;
    private static final String WIFI_REPEATER_CLIENTS_CHANGED_ACTION = "com.huawei.wifi.action.WIFI_REPEATER_CLIENTS_CHANGED";
    private static final int WIFI_REPEATER_CLOSE = 0;
    private static final int WIFI_REPEATER_OPEN = 1;
    private static final String WIFI_REPEATER_STATE_CHANGED_ACTION = "com.huawei.wifi.action.WIFI_REPEATER_STATE_CHANGED";
    private Context mContext;
    private State mDefaultState = new DefaultState(this, null);
    private WifiP2pGroup mDownstreamInfo;
    private State mHangState = new HangState(this, null);
    private boolean mShouldRestart = false;
    private State mTetheredState = new TetheredState(this, null);
    private State mUntetheredState = new UntetheredState(this, null);
    private WifiConfiguration mUpstreamConfig;
    private WifiInfo mUpstreamInfo;
    private AsyncChannel mWifiP2pChannel = new AsyncChannel();

    private class DefaultState extends State {
        /* synthetic */ DefaultState(WifiRepeaterController this$0, DefaultState -this1) {
            this();
        }

        private DefaultState() {
        }

        public boolean processMessage(Message message) {
            WifiRepeaterController.this.logStateAndMessage(this, message);
            return true;
        }
    }

    private class HangState extends State {
        /* synthetic */ HangState(WifiRepeaterController this$0, HangState -this1) {
            this();
        }

        private HangState() {
        }

        public void enter() {
            Log.d("WifiRepeater", "HangState enter.");
            WifiRepeaterController.this.sendMessageDelayed(0, WifiRepeaterController.HANG_TIMEOUT);
            WifiRepeaterController.this.persistStatus(0);
            WifiRepeaterController.this.sendStateChangedBroadcast();
            if (!WifiRepeaterController.this.pauseDownstream()) {
                WifiRepeaterController.this.removeMessages(0);
                WifiRepeaterController.this.stopTethering();
            }
        }

        public boolean processMessage(Message message) {
            WifiRepeaterController.this.logStateAndMessage(this, message);
            switch (message.what) {
                case 0:
                    WifiRepeaterController.this.stopTethering();
                    break;
                case 1:
                    break;
                case 2:
                    WifiRepeaterController.this.removeMessages(0);
                    if (!WifiRepeaterController.this.isFrequencyCollision() && !WifiRepeaterController.this.isGatewayCollision()) {
                        if (!WifiRepeaterController.this.resumeDownstream()) {
                            WifiRepeaterController.this.stopTethering();
                            break;
                        }
                        WifiRepeaterController.this.persistStatus(1);
                        WifiRepeaterController.this.sendStateChangedBroadcast();
                        WifiRepeaterController.this.transitionTo(WifiRepeaterController.this.mTetheredState);
                        break;
                    }
                    WifiRepeaterController.this.mShouldRestart = true;
                    WifiRepeaterController.this.stopTethering();
                    break;
                case 3:
                    WifiRepeaterController.this.transitionTo(WifiRepeaterController.this.mUntetheredState);
                    break;
                default:
                    return false;
            }
            return true;
        }
    }

    private class TetheredState extends State {
        /* synthetic */ TetheredState(WifiRepeaterController this$0, TetheredState -this1) {
            this();
        }

        private TetheredState() {
        }

        public void enter() {
            Log.d("WifiRepeater", "TetheredState enter.");
            WifiRepeaterController.this.mShouldRestart = false;
        }

        public boolean processMessage(Message message) {
            WifiRepeaterController.this.logStateAndMessage(this, message);
            switch (message.what) {
                case 1:
                    WifiRepeaterController.this.transitionTo(WifiRepeaterController.this.mHangState);
                    break;
                case 3:
                    WifiRepeaterController.this.transitionTo(WifiRepeaterController.this.mUntetheredState);
                    break;
                default:
                    return false;
            }
            return true;
        }
    }

    private class UntetheredState extends State {
        /* synthetic */ UntetheredState(WifiRepeaterController this$0, UntetheredState -this1) {
            this();
        }

        private UntetheredState() {
        }

        public void enter() {
            Log.d("WifiRepeater", "UntetheredState enter.");
            if (WifiRepeaterController.this.mShouldRestart) {
                WifiRepeaterController.this.mShouldRestart = false;
                WifiRepeaterController.this.restartTethering();
            }
        }

        public boolean processMessage(Message message) {
            WifiRepeaterController.this.logStateAndMessage(this, message);
            switch (message.what) {
                case 4:
                    WifiRepeaterController.this.transitionTo(WifiRepeaterController.this.mTetheredState);
                    return true;
                default:
                    return false;
            }
        }
    }

    public WifiRepeaterController(Context context, Messenger messenger) {
        this.mContext = context;
        this.mWifiP2pChannel.connectSync(this.mContext, getHandler(), messenger);
        initStateMachine();
    }

    public void handleP2pUntethered() {
        Log.d("WifiRepeater", "handleP2pUntethered");
        this.mDownstreamInfo = null;
        sendMessage(3);
    }

    public void handleP2pTethered(WifiP2pGroup group) {
        Log.d("WifiRepeater", "handleP2pTethered: " + group);
        this.mDownstreamInfo = group;
        sendMessage(4);
    }

    public void handleWifiDisconnect() {
        Log.d("WifiRepeater", "handleWifiDisconnect");
        this.mUpstreamInfo = null;
        sendMessage(1);
    }

    public void handleWifiConnect(WifiInfo wifiInfo, WifiConfiguration wifiConfig) {
        Log.d("WifiRepeater", "handleWifiConnect.");
        this.mUpstreamInfo = wifiInfo;
        this.mUpstreamConfig = wifiConfig;
        sendMessage(2);
    }

    public void handleClientListChanged(WifiP2pGroup group) {
        Log.d("WifiRepeater", "handleClientListChanged: size=" + group.getClientList().size());
        Intent intent = new Intent(WIFI_REPEATER_CLIENTS_CHANGED_ACTION);
        intent.putExtra(EXTRA_WIFI_REPEATER_CLIENTS_SIZE, group.getClientList().size());
        this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    public void sendStateChangedBroadcast() {
        Log.d("WifiRepeater", "sendStateChangedBroadcast");
        this.mContext.sendStickyBroadcastAsUser(new Intent(WIFI_REPEATER_STATE_CHANGED_ACTION), UserHandle.ALL);
    }

    public int retrieveDownstreamChannel() {
        if (this.mUpstreamInfo == null) {
            Log.e("WifiRepeater", "retrieveDownstreamChannel: mUpstreamInfo == null;");
            return -1;
        }
        int upstreamChannel = convertFreqToChannel(this.mUpstreamInfo.getFrequency());
        int upstreamBand = convertFreqToBand(this.mUpstreamInfo.getFrequency());
        if (-1 == upstreamChannel || -1 == upstreamBand) {
            Log.e("WifiRepeater", "retrieveDownstreamChannel: upstreamChannel == CHANNEL_ERROR");
            return -1;
        }
        int result;
        if (1 == upstreamBand && (isSupportRsdb() || isDfsChannel(this.mUpstreamInfo.getFrequency()))) {
            result = getRandom2GChannel();
        } else {
            result = upstreamChannel;
        }
        Log.d("WifiRepeater", "retrieveDownstreamChannel: " + result);
        return result;
    }

    public int retrieveDownstreamBand() {
        if (this.mUpstreamInfo == null) {
            Log.e("WifiRepeater", "retrieveDownstreamBand: mUpstreamInfo == null;");
            return -1;
        }
        int upstreamBand = convertFreqToBand(this.mUpstreamInfo.getFrequency());
        if (-1 == upstreamBand) {
            Log.e("WifiRepeater", "retrieveDownstreamBand: upstreamBand == BAND_ERROR");
            return -1;
        }
        int result;
        if (isSupportRsdb() || isDfsChannel(this.mUpstreamInfo.getFrequency())) {
            result = 0;
        } else {
            result = upstreamBand;
        }
        Log.d("WifiRepeater", "retrieveDownstreamBand: " + result);
        return result;
    }

    public boolean isEncryptionTypeTetheringAllowed() {
        if (this.mUpstreamConfig == null) {
            Log.e("WifiRepeater", "isEncryptionTypeTetheringAllowed: mUpstreamConfig==null");
            return false;
        } else if (this.mUpstreamConfig.enterpriseConfig == null) {
            Log.d("WifiRepeater", "isEncryptionTypeTetheringAllowed: enterpriseConfig is null, return true.");
            return true;
        } else {
            if (this.mUpstreamConfig.enterpriseConfig != null) {
                int eapMethod = this.mUpstreamConfig.enterpriseConfig.getEapMethod();
                Log.d("WifiRepeater", "isEncryptionTypeTetheringAllowed: eapMethod=" + eapMethod);
                if (1 == eapMethod || 2 == eapMethod) {
                    return false;
                }
            }
            return true;
        }
    }

    private void initStateMachine() {
        Log.d("WifiRepeater", "initStateMachine.");
        addState(this.mUntetheredState, this.mDefaultState);
        addState(this.mTetheredState, this.mDefaultState);
        addState(this.mHangState, this.mDefaultState);
        setInitialState(this.mUntetheredState);
        start();
    }

    private void restartTethering() {
        Log.d("WifiRepeater", "restartTethering");
        if (isEncryptionTypeTetheringAllowed()) {
            this.mWifiP2pChannel.sendMessage(141268);
        }
    }

    private void stopTethering() {
        Log.d("WifiRepeater", "stopTethering");
        this.mWifiP2pChannel.sendMessage(HwWifiStateMachine.CMD_STOP_WIFI_REPEATER);
    }

    private boolean pauseDownstream() {
        try {
            getNwService().setIpForwardingEnabled(false);
            Log.d("WifiRepeater", "pauseDownstream: success.");
            return true;
        } catch (IllegalStateException e) {
            Log.e("WifiRepeater", "resumeDownstream exception: " + e.getMessage());
            return false;
        } catch (IllegalArgumentException e2) {
            Log.e("WifiRepeater", "resumeDownstream exception: " + e2.getMessage());
            return false;
        } catch (RemoteException e3) {
            Log.e("WifiRepeater", "resumeDownstream exception: " + e3.getMessage());
            return false;
        } catch (Exception e4) {
            Log.e("WifiRepeater", "pauseDownstream exception: " + e4.getMessage());
            return false;
        }
    }

    private boolean resumeDownstream() {
        if (!isEncryptionTypeTetheringAllowed()) {
            return false;
        }
        try {
            getNwService().setIpForwardingEnabled(true);
            Log.d("WifiRepeater", "resumeDownstream: success.");
            return true;
        } catch (IllegalStateException e) {
            Log.e("WifiRepeater", "resumeDownstream exception: " + e.getMessage());
            return false;
        } catch (IllegalArgumentException e2) {
            Log.e("WifiRepeater", "resumeDownstream exception: " + e2.getMessage());
            return false;
        } catch (RemoteException e3) {
            Log.e("WifiRepeater", "resumeDownstream exception: " + e3.getMessage());
            return false;
        } catch (Exception e4) {
            Log.e("WifiRepeater", "resumeDownstream exception: " + e4.getMessage());
            return false;
        }
    }

    private INetworkManagementService getNwService() {
        return Stub.asInterface(ServiceManager.getService("network_management"));
    }

    private boolean isFrequencyCollision() {
        boolean z = true;
        boolean z2 = false;
        if (this.mUpstreamInfo == null || this.mDownstreamInfo == null) {
            Log.e("WifiRepeater", "isFrequencyCollision:" + (this.mUpstreamInfo == null ? "mUpstreamInfo==null" : "") + (this.mDownstreamInfo == null ? "mDownstreamInfo==null" : ""));
            return true;
        }
        int upFreq = this.mUpstreamInfo.getFrequency();
        int downFreq = this.mDownstreamInfo.getFrequence();
        int upBand = convertFreqToBand(upFreq);
        int downBand = convertFreqToBand(downFreq);
        Log.d("WifiRepeater", "isFrequencyCollision: upFreq=" + upFreq + " downFreq=" + downFreq + " upBand=" + upBand + " downBand=" + downBand);
        if (isSupportRsdb()) {
            if (upBand == downBand && upFreq != downFreq) {
                z2 = true;
            }
            return z2;
        }
        if (upFreq == downFreq) {
            z = false;
        }
        return z;
    }

    private boolean isGatewayCollision() {
        int upstreamGateway = 0;
        int downstreamGateway = 0;
        WifiManager wifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        if (wifiManager != null) {
            DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
            if (dhcpInfo != null) {
                upstreamGateway = RPT_GATEWAY_MASK & dhcpInfo.gateway;
            }
        }
        if (this.mDownstreamInfo != null) {
            String addr = this.mDownstreamInfo.getP2pServerAddress();
            if (!TextUtils.isEmpty(addr)) {
                downstreamGateway = RPT_GATEWAY_MASK & NetworkUtils.inetAddressToInt((Inet4Address) NetworkUtils.numericToInetAddress(addr));
            }
        }
        Log.d("WifiRepeater", "gateway check: upstream=" + upstreamGateway + " downstream=" + downstreamGateway);
        return upstreamGateway == downstreamGateway;
    }

    private boolean isSupportRsdb() {
        boolean ret = WifiInjector.getInstance().getWifiNative().isSupportRsdbByDriver();
        Log.d("WifiRepeater", "isSupportRsdb: " + ret);
        return ret;
    }

    private boolean isDfsChannel(int frequency) {
        boolean ret = WifiInjector.getInstance().getWifiNative().isDfsChannel(frequency);
        Log.d("WifiRepeater", "isDfsChannel: " + ret);
        return ret;
    }

    private void persistStatus(int status) {
        Log.d("WifiRepeater", "persistStatus: " + status);
        Global.putInt(this.mContext.getContentResolver(), "wifi_repeater_on", status);
    }

    private int getRandom2GChannel() {
        int[] channel2G = new int[]{1, 6, 10};
        int result = channel2G[new Random(Calendar.getInstance().getTimeInMillis()).nextInt(channel2G.length)];
        Log.d("WifiRepeater", "getRandom2GChannel: " + result);
        return result;
    }

    private int convertFreqToChannel(int frequency) {
        if (frequency >= WifiScanGenieController.CHANNEL_1_FREQ && frequency <= 2484) {
            return ((frequency - 2412) / 5) + 1;
        }
        if (frequency >= 5170 && frequency <= 5825) {
            return ((frequency - 5170) / 5) + 34;
        }
        Log.e("WifiRepeater", "convertFreqToChannel: CHANNEL_ERROR");
        return -1;
    }

    private int convertFreqToBand(int frequency) {
        if (frequency > 2400 && frequency < 2500) {
            return 0;
        }
        if (frequency > 4900 && frequency < 5900) {
            return 1;
        }
        Log.e("WifiRepeater", "convertFreqToBand: BAND_ERROR");
        return -1;
    }

    private void logStateAndMessage(State state, Message message) {
        String str;
        switch (message.what) {
            case 0:
                str = "CMD_STOP_TETHERING";
                break;
            case 1:
                str = "CMD_UPSTREAM_NETWORK_DISCONNECT";
                break;
            case 2:
                str = "CMD_UPSTREAM_NETWORK_CONNECT";
                break;
            case 3:
                str = "CMD_DOWNSTREAM_NETWORK_UNTETHERED";
                break;
            case 4:
                str = "CMD_DOWNSTREAM_NETWORK_TETHERED";
                break;
            default:
                str = "what:" + Integer.toString(message.what);
                break;
        }
        Log.d("WifiRepeater", state.getClass().getSimpleName() + ": handle message: " + str);
    }
}
