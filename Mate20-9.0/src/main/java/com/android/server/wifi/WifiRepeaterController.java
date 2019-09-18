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
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.util.AsyncChannel;
import com.android.internal.util.State;
import com.android.server.wifi.util.ApConfigUtil;
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
    private State mDefaultState = new DefaultState();
    private WifiP2pGroup mDownstreamInfo;
    /* access modifiers changed from: private */
    public State mHangState = new HangState();
    /* access modifiers changed from: private */
    public boolean mShouldRestart = false;
    /* access modifiers changed from: private */
    public State mTetheredState = new TetheredState();
    /* access modifiers changed from: private */
    public State mUntetheredState = new UntetheredState();
    private int mUpstreamBand = -1;
    private WifiConfiguration mUpstreamConfig;
    private WifiInfo mUpstreamInfo;
    private AsyncChannel mWifiP2pChannel = new AsyncChannel();

    private class DefaultState extends State {
        private DefaultState() {
        }

        public boolean processMessage(Message message) {
            WifiRepeaterController.this.logStateAndMessage(this, message);
            return true;
        }
    }

    private class HangState extends State {
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
                        } else {
                            WifiRepeaterController.this.persistStatus(1);
                            WifiRepeaterController.this.sendStateChangedBroadcast();
                            WifiRepeaterController.this.transitionTo(WifiRepeaterController.this.mTetheredState);
                            break;
                        }
                    } else {
                        boolean unused = WifiRepeaterController.this.mShouldRestart = true;
                        WifiRepeaterController.this.stopTethering();
                        break;
                    }
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
        private TetheredState() {
        }

        public void enter() {
            Log.d("WifiRepeater", "TetheredState enter.");
            boolean unused = WifiRepeaterController.this.mShouldRestart = false;
        }

        public boolean processMessage(Message message) {
            WifiRepeaterController.this.logStateAndMessage(this, message);
            int i = message.what;
            if (i == 1) {
                WifiRepeaterController.this.transitionTo(WifiRepeaterController.this.mHangState);
            } else if (i != 3) {
                return false;
            } else {
                WifiRepeaterController.this.transitionTo(WifiRepeaterController.this.mUntetheredState);
            }
            return true;
        }
    }

    private class UntetheredState extends State {
        private UntetheredState() {
        }

        public void enter() {
            Log.d("WifiRepeater", "UntetheredState enter.");
            if (WifiRepeaterController.this.mShouldRestart) {
                boolean unused = WifiRepeaterController.this.mShouldRestart = false;
                WifiRepeaterController.this.restartTethering();
            }
        }

        public boolean processMessage(Message message) {
            WifiRepeaterController.this.logStateAndMessage(this, message);
            if (message.what != 4) {
                return false;
            }
            WifiRepeaterController.this.transitionTo(WifiRepeaterController.this.mTetheredState);
            return true;
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
        int result;
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
        if (1 != upstreamBand) {
            result = upstreamChannel;
            this.mUpstreamBand = upstreamBand;
        } else if (isSupportRsdb()) {
            result = getRandom2GChannel();
            this.mUpstreamBand = 0;
        } else if (isDfsChannel(this.mUpstreamInfo.getFrequency()) || isIndoorChannel(this.mUpstreamInfo.getFrequency())) {
            int result2 = ApConfigUtil.getSelected5GChannel(SoftApChannelXmlParse.convertChannelListToFrequency(HwSoftApManager.getChannelListFor5GWithoutIndoor()));
            if (result2 == -1) {
                result = getRandom2GChannel();
                this.mUpstreamBand = 0;
            } else {
                this.mUpstreamBand = 1;
                result = result2;
            }
        } else {
            result = upstreamChannel;
            this.mUpstreamBand = upstreamBand;
        }
        Log.d("WifiRepeater", "retrieveDownstreamChannel: " + result);
        return result;
    }

    public int retrieveDownstreamBand() {
        if (this.mUpstreamInfo == null) {
            Log.e("WifiRepeater", "retrieveDownstreamBand: mUpstreamInfo == null;");
            return -1;
        } else if (-1 == convertFreqToBand(this.mUpstreamInfo.getFrequency())) {
            Log.e("WifiRepeater", "retrieveDownstreamBand: upstreamBand == BAND_ERROR");
            return -1;
        } else {
            int result = this.mUpstreamBand;
            Log.d("WifiRepeater", "retrieveDownstreamBand: " + result);
            return result;
        }
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

    /* access modifiers changed from: private */
    public void restartTethering() {
        Log.d("WifiRepeater", "restartTethering");
        if (isEncryptionTypeTetheringAllowed()) {
            this.mWifiP2pChannel.sendMessage(141268);
        }
    }

    /* access modifiers changed from: private */
    public void stopTethering() {
        Log.d("WifiRepeater", "stopTethering");
        this.mWifiP2pChannel.sendMessage(HwWifiStateMachine.CMD_STOP_WIFI_REPEATER);
    }

    /* access modifiers changed from: private */
    public boolean pauseDownstream() {
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

    /* access modifiers changed from: private */
    public boolean resumeDownstream() {
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
        return INetworkManagementService.Stub.asInterface(ServiceManager.getService("network_management"));
    }

    /* access modifiers changed from: private */
    public boolean isFrequencyCollision() {
        int upBand;
        int downBand;
        boolean z = true;
        if (this.mUpstreamInfo == null || this.mDownstreamInfo == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("isFrequencyCollision:");
            sb.append(this.mUpstreamInfo == null ? "mUpstreamInfo==null" : "");
            sb.append(this.mDownstreamInfo == null ? "mDownstreamInfo==null" : "");
            Log.e("WifiRepeater", sb.toString());
            return true;
        }
        int upFreq = this.mUpstreamInfo.getFrequency();
        int downFreq = this.mDownstreamInfo.getFrequence();
        Log.d("WifiRepeater", "isFrequencyCollision: upFreq=" + upFreq + " downFreq=" + downFreq + " upBand=" + upBand + " downBand=" + downBand);
        if (isSupportRsdb()) {
            if (upBand != downBand || upFreq == downFreq) {
                z = false;
            }
            return z;
        }
        if (upFreq == downFreq) {
            z = false;
        }
        return z;
    }

    /* access modifiers changed from: private */
    public boolean isGatewayCollision() {
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

    private boolean isIndoorChannel(int frequency) {
        boolean ret = false;
        HwWifiStateMachine wifiStateMachine = WifiInjector.getInstance().getWifiStateMachine();
        if (wifiStateMachine instanceof HwWifiStateMachine) {
            ret = wifiStateMachine.getSoftApChannelXmlParse().isIndoorChannel(frequency, WifiInjector.getInstance().getWifiCountryCode().getCountryCodeSentToDriver());
        }
        Log.d("WifiRepeater", "isIndoorChannel: " + ret);
        return ret;
    }

    /* access modifiers changed from: private */
    public void persistStatus(int status) {
        Log.d("WifiRepeater", "persistStatus: " + status);
        Settings.Global.putInt(this.mContext.getContentResolver(), "wifi_repeater_on", status);
    }

    private int getRandom2GChannel() {
        int[] channel2G = {1, 6, 10};
        int result = channel2G[new Random(Calendar.getInstance().getTimeInMillis()).nextInt(channel2G.length)];
        Log.d("WifiRepeater", "getRandom2GChannel: " + result);
        return result;
    }

    private int convertFreqToChannel(int frequency) {
        if (frequency >= 2412 && frequency <= 2484) {
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

    /* access modifiers changed from: private */
    public void logStateAndMessage(State state, Message message) {
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
