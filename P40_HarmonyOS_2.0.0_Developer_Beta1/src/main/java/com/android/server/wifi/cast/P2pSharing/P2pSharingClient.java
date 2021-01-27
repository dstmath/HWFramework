package com.android.server.wifi.cast.P2pSharing;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiScanner;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.WorkSource;
import android.text.TextUtils;
import android.util.wifi.HwHiLog;
import com.android.server.wifi.WifiConfigManager;
import com.android.server.wifi.WifiInjector;
import com.android.server.wifi.cast.CastOptChr;
import com.android.server.wifi.util.ScanResultUtil;
import com.android.server.wifi.wifi2.HwWifi2Manager;
import com.huawei.distributedgw.DistributedGatewayManagerEx;
import com.huawei.distributedgw.DistributedGatewayStateCallback;
import com.huawei.distributedgw.InternetBorrowingRequestEx;
import com.huawei.distributedgw.InternetSharingRequestEx;
import java.nio.charset.StandardCharsets;
import org.json.JSONException;
import org.json.JSONObject;

public class P2pSharingClient implements P2pSharingInterface {
    private static final int CHANNEL_CREATE_TIME_OUT = 20000;
    private static final int CONNECT_INTERVAL = 500;
    private static final int DEFAULT_NETWORK_ID = -1;
    private static final int DEFAULT_PORT = -1;
    private static final int GET_DYNAMIC_PORT_TIME_OUT = 3000;
    private static final int MAX_RETRY_TIMES = 10;
    private static final int MSG_CONNECT_SEVER = 3;
    private static final int MSG_GET_DYNAMIC_PORT_TIME_OUT = 1;
    private static final int MSG_INTERNET_CHECK_RESULT_TIME_OUT = 4;
    private static final int MSG_TEMP_CHANNEL_CREATE_TIME_OUT = 0;
    private static final int MSG_WAIT_SERVER_CONFIG_RESP_TIME_OUT = 2;
    private static final int MSG_WIFI_STATE_CHECK = 5;
    private static final long RETRY_INTERVAL = 200;
    private static final int STATE_CONFIGURED = 5;
    private static final int STATE_CONFIG_NETWORK = 4;
    private static final int STATE_CREATE_CHANNEL = 3;
    private static final int STATE_DEFAULT = 0;
    private static final int STATE_NEGOTIATION_PORT = 1;
    private static final int STATE_NEGOTIATION_PORT_END = 2;
    private static final String TAG = "P2pSharing:P2pSharingClient";
    private static final int WAIT_INTERNET_CHECK_RESULT_TIME_OUT = 15000;
    private static final int WAIT_SERVER_RESP_TIME_OUT = 20000;
    private static final int WIFI_STATE_CHECK_DELAYED_TIME = 4000;
    private int clientState = 0;
    private Context context;
    private int curTryTimes = 0;
    private DataChannel dataChannel = new DataChannel(this);
    private int dynamicPort = -1;
    private InnerCallback innerCallback;
    private Handler innerHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        /* class com.android.server.wifi.cast.P2pSharing.$$Lambda$P2pSharingClient$jqqq6KbRgiA4_kJNu3eh3p_jPDo */

        @Override // android.os.Handler.Callback
        public final boolean handleMessage(Message message) {
            return P2pSharingClient.this.lambda$new$0$P2pSharingClient(message);
        }
    });
    private boolean isChannelCreated = false;
    private boolean isWifiDisconnectByP2pSharing = false;
    private P2pSharingListener p2pSharingListener;
    private InternetBorrowingRequestEx requestEx;
    private WifiConfiguration savedConfig;
    private DistributedGatewayStateCallback stateCallback = new DistributedGatewayStateCallback() {
        /* class com.android.server.wifi.cast.P2pSharing.P2pSharingClient.AnonymousClass1 */

        public void onSharingStateChanged(InternetSharingRequestEx internetSharingRequestEx, int state) {
            HwHiLog.d(P2pSharingClient.TAG, false, "onSharingStateChanged", new Object[0]);
        }

        public void onBorrowingStateChanged(InternetBorrowingRequestEx internetBorrowingRequestEx, int state) {
            HwHiLog.d(P2pSharingClient.TAG, false, "onBorrowingStateChanged:" + state, new Object[0]);
            P2pSharingClient.this.innerHandler.removeMessages(4);
            P2pSharingClient.this.onStateChanged(state);
        }
    };
    private WifiConfigManager wifiConfigManager = WifiInjector.getInstance().getWifiConfigManager();

    /* access modifiers changed from: private */
    public interface InnerCallback {
        void onDynamicPortRecv();
    }

    public /* synthetic */ boolean lambda$new$0$P2pSharingClient(Message message) {
        if (message == null) {
            HwHiLog.d(TAG, false, "message is null", new Object[0]);
            return false;
        }
        HwHiLog.d(TAG, false, "handleMessage:" + message.what, new Object[0]);
        int i = message.what;
        if (i == 0 || i == 1 || i == 2) {
            processTimeOut();
        } else if (i == 3) {
            tryConnectServer();
        } else if (i == 4) {
            handleInternetCheckTimeOut();
        } else if (i == 5) {
            checkWiFiState();
        } else if (i == 1001) {
            handleConfigServerResp(message.obj);
        } else if (i == 1005) {
            handleDynamicPortResp(message);
        } else if (i != 1006) {
            HwHiLog.d(TAG, false, "unsupported type", new Object[0]);
            feedbackWithCallback(1, 3);
        } else {
            feedbackWithCallback(3, 1);
        }
        return false;
    }

    @Override // com.android.server.wifi.cast.P2pSharing.P2pSharingInterface
    public void setP2pSharingListener(P2pSharingListener listener) {
        this.p2pSharingListener = listener;
    }

    @Override // com.android.server.wifi.cast.P2pSharing.P2pSharingInterface
    public void onP2pStateChanged(boolean isConnected) {
        HwHiLog.d(TAG, false, "onP2pStateChanged:" + isConnected, new Object[0]);
        if (!isConnected && this.clientState != 0) {
            startRecoverLocal();
        }
    }

    private <T> int getDynamicPort(T resp) {
        this.innerHandler.removeMessages(1);
        if (resp instanceof JSONObject) {
            return resp.optInt(Constants.EXTRA, -1);
        }
        HwHiLog.e(TAG, false, "invalid resp", new Object[0]);
        return -1;
    }

    private void processTimeOut() {
        HwHiLog.d(TAG, false, "processTimeOut", new Object[0]);
        feedbackWithCallback(1, 2);
    }

    private void reset() {
        HwHiLog.d(TAG, false, "reset", new Object[0]);
        this.dynamicPort = -1;
        this.clientState = 0;
        this.curTryTimes = 0;
        this.isChannelCreated = false;
    }

    @Override // com.android.server.wifi.cast.P2pSharing.ChannelListener
    public void onDataReceived(byte[] data) {
        if (data == null || data.length <= 0) {
            HwHiLog.d(TAG, false, "receive invalid data", new Object[0]);
            feedbackWithCallback(1, 3);
            return;
        }
        try {
            JSONObject respJson = new JSONObject(new String(data, StandardCharsets.UTF_8));
            Message msg = Message.obtain();
            msg.what = respJson.optInt(Constants.CMD_TYPE, 1009);
            msg.obj = respJson;
            this.innerHandler.sendMessage(msg);
        } catch (JSONException e) {
            HwHiLog.e(TAG, false, "parse failed", new Object[0]);
            feedbackWithCallback(1, 3);
        }
    }

    @Override // com.android.server.wifi.cast.P2pSharing.ChannelListener
    public void onPortGet(int port) {
    }

    private void handleDynamicPortResp(Message message) {
        if (this.clientState == 1) {
            this.clientState = 2;
            HwHiLog.d(TAG, false, "to STATE_NEGOTIATION_PORT_END", new Object[0]);
            this.dynamicPort = getDynamicPort(message.obj);
            DataChannel dataChannel2 = this.dataChannel;
            if (dataChannel2 != null) {
                dataChannel2.closeFixedSocket();
            }
            InnerCallback innerCallback2 = this.innerCallback;
            if (innerCallback2 != null) {
                innerCallback2.onDynamicPortRecv();
            }
        }
    }

    private <T> void handleConfigServerResp(T resp) {
        HwHiLog.d(TAG, false, "handleConfigServerResp", new Object[0]);
        if (!(resp instanceof JSONObject)) {
            HwHiLog.d(TAG, false, "invalid config server resp", new Object[0]);
            return;
        }
        this.innerHandler.removeMessages(2);
        T jsonObject = resp;
        if (jsonObject.optInt(Constants.CONFIG_SERVER_RESULT, 1) == 0) {
            HwHiLog.d(TAG, false, "remote config success", new Object[0]);
            if (!startConfigLocal(jsonObject.optString(Constants.DEVICE_NAME, ""))) {
                HwHiLog.e(TAG, false, "config local fail", new Object[0]);
                feedbackWithCallback(1, 3);
                return;
            }
            return;
        }
        feedbackWithCallback(1, jsonObject.optInt(Constants.RESULT_REASON, 3));
    }

    @Override // com.android.server.wifi.cast.P2pSharing.ChannelListener
    public void onChannelEvent(int eventId) {
        HwHiLog.d(TAG, false, "eventId:" + eventId, new Object[0]);
        switch (eventId) {
            case 2001:
                sendDynamicPortRequest();
                return;
            case 2002:
                this.isChannelCreated = true;
                startConfigRemote();
                return;
            case 2003:
            case 2005:
            case 2006:
            case 2007:
            case 2008:
                handleChannelException();
                return;
            case 2004:
            default:
                HwHiLog.d(TAG, false, "unsupported channel event", new Object[0]);
                return;
        }
    }

    public void startP2pBorrowing() {
        HwHiLog.d(TAG, false, "startP2pBorrowing", new Object[0]);
        int i = this.clientState;
        if (i < 2) {
            HwHiLog.d(TAG, false, "port is not ready", new Object[0]);
            if (this.innerCallback == null) {
                this.innerCallback = new InnerCallback() {
                    /* class com.android.server.wifi.cast.P2pSharing.P2pSharingClient.AnonymousClass2 */

                    @Override // com.android.server.wifi.cast.P2pSharing.P2pSharingClient.InnerCallback
                    public void onDynamicPortRecv() {
                        P2pSharingClient.this.startP2pBorrowing();
                    }
                };
            }
        } else if (i == 2) {
            createChannel();
        } else if (this.isChannelCreated && i == 3) {
            startConfigRemote();
        }
    }

    private void createChannel() {
        if (this.dataChannel != null) {
            HwHiLog.d(TAG, false, "to STATE_CREATE_CHANNEL", new Object[0]);
            this.clientState = 3;
            this.dataChannel.createDynamicSocket(Utils.getServerIpAddress(Utils.getP2pIpAddress()), this.dynamicPort, RETRY_INTERVAL);
        }
    }

    @Override // com.android.server.wifi.cast.P2pSharing.P2pSharingInterface
    public void setContext(Context context2) {
        this.context = context2;
    }

    @Override // com.android.server.wifi.cast.P2pSharing.P2pSharingInterface
    public void setUpTempConnection() {
        HwHiLog.d(TAG, false, "setUpTempConnection, to STATE_NEGOTIATION_PORT", new Object[0]);
        if (this.clientState != 0) {
            HwHiLog.d(TAG, false, "the channel has been setup", new Object[0]);
            return;
        }
        this.innerHandler.sendEmptyMessageDelayed(0, 20000);
        this.clientState = 1;
        tryConnectServer();
    }

    public void stopP2pBorrowing() {
        HwHiLog.d(TAG, false, "stop borrowing", new Object[0]);
        this.clientState = 3;
        startRecoverLocal();
        startRecoverRemoteConfig();
    }

    private void tryConnectServer() {
        HwHiLog.d(TAG, false, "tryConnectServer:" + this.curTryTimes, new Object[0]);
        if (this.curTryTimes < 10) {
            String localIp = Utils.getP2pIpAddress();
            if (TextUtils.isEmpty(localIp)) {
                this.innerHandler.sendEmptyMessageDelayed(3, 500);
                this.curTryTimes++;
                return;
            }
            String serverIp = Utils.getServerIpAddress(localIp);
            DataChannel dataChannel2 = this.dataChannel;
            if (dataChannel2 != null) {
                dataChannel2.createFixedSocket(serverIp, RETRY_INTERVAL);
            }
        }
    }

    private void startRecoverLocal() {
        HwHiLog.d(TAG, false, "startRecoverLocal", new Object[0]);
        CastOptChr castOptChr = CastOptChr.getInstance();
        if (castOptChr != null) {
            castOptChr.handleP2pSharingEnded();
        }
        DistributedGatewayManagerEx.disableInternetBorrowing(this.requestEx);
    }

    private void startRecoverRemoteConfig() {
        HwHiLog.d(TAG, false, "startRecoverRemoteConfig", new Object[0]);
        if (!sendRequest(1002)) {
            HwHiLog.d(TAG, false, "send CMD_RECOVER_SERVER_REQUEST failed", new Object[0]);
            feedbackWithCallback(1, 2);
        }
    }

    private void startConfigRemote() {
        HwHiLog.d(TAG, false, "startConfigRemote, to STATE_CONFIG_NETWORK", new Object[0]);
        this.innerHandler.sendEmptyMessageDelayed(2, 20000);
        this.clientState = 4;
        JSONObject extra = new JSONObject();
        int wifiSecurity = Utils.getWiFiSecurity(this.context);
        if (wifiSecurity == -1) {
            HwHiLog.d(TAG, false, "get wifi security failed", new Object[0]);
            feedbackWithCallback(1, 3);
            return;
        }
        try {
            extra.put(Constants.DEVICE_TYPE, 1);
            extra.put(Constants.WIFI_SECURITY, wifiSecurity);
            extra.put(Constants.DEVICE_NAME, Utils.getDeviceName());
            if (!sendRequestWithExtra(1000, extra)) {
                HwHiLog.d(TAG, false, "send CMD_CONFIG_SERVER_REQUEST failed", new Object[0]);
                feedbackWithCallback(1, 2);
            }
        } catch (JSONException e) {
            HwHiLog.d(TAG, false, "assemble device type failed", new Object[0]);
            feedbackWithCallback(1, 3);
        }
    }

    private boolean startConfigLocal(String peerDeviceName) {
        HwHiLog.d(TAG, false, "startConfigLocal", new Object[0]);
        this.requestEx = new InternetBorrowingRequestEx(Utils.getLocalNetworkInterfaceName(), Utils.getServerIpAddress(Utils.getP2pIpAddress()));
        this.requestEx.setServiceName(peerDeviceName);
        if (DistributedGatewayManagerEx.isInternetBorrowing(this.requestEx)) {
            return true;
        }
        DistributedGatewayManagerEx.regStateCallback(this.stateCallback);
        if (!DistributedGatewayManagerEx.enableInternetBorrowing(this.requestEx)) {
            return false;
        }
        this.savedConfig = Utils.getCurrentWifiConfiguration();
        Utils.enableWiFiAutoConnect(this.context, false);
        this.isWifiDisconnectByP2pSharing = true;
        this.innerHandler.sendEmptyMessageDelayed(4, 15000);
        return true;
    }

    private void sendDynamicPortRequest() {
        HwHiLog.d(TAG, false, "sendDynamicPortRequest", new Object[0]);
        this.innerHandler.removeMessages(0);
        if (!sendRequest(1004)) {
            HwHiLog.d(TAG, false, "send CMD_DYNAMIC_PORT_REQUEST failed", new Object[0]);
            feedbackWithCallback(1, 2);
            return;
        }
        this.innerHandler.sendEmptyMessageDelayed(1, 3000);
    }

    private void feedbackWithCallback(int evenId, int reasonId) {
        HwHiLog.d(TAG, false, "feedback eventId:" + evenId + " reasonId:" + reasonId, new Object[0]);
        P2pSharingListener p2pSharingListener2 = this.p2pSharingListener;
        if (p2pSharingListener2 != null) {
            p2pSharingListener2.onEvent(evenId, reasonId);
        }
    }

    private boolean sendRequest(int cmdType) {
        JSONObject request = new JSONObject();
        try {
            request.put(Constants.CMD_TYPE, cmdType);
            if (this.dataChannel != null) {
                if (this.dataChannel.send(request.toString().getBytes(StandardCharsets.UTF_8))) {
                    return true;
                }
            }
            HwHiLog.d(TAG, false, "sendRequest failed for " + cmdType, new Object[0]);
            return false;
        } catch (JSONException e) {
            HwHiLog.d(TAG, false, "assemble request failed", new Object[0]);
            return false;
        }
    }

    private boolean sendRequestWithExtra(int cmdType, JSONObject extra) {
        JSONObject req = new JSONObject();
        try {
            req.put(Constants.CMD_TYPE, cmdType);
            req.put(Constants.EXTRA, extra);
            if (this.dataChannel != null) {
                if (this.dataChannel.send(req.toString().getBytes(StandardCharsets.UTF_8))) {
                    return true;
                }
            }
            HwHiLog.d(TAG, false, "sendRequestWithExtra failed for " + cmdType, new Object[0]);
            return false;
        } catch (JSONException e) {
            HwHiLog.d(TAG, false, "assemble request failed", new Object[0]);
            return false;
        }
    }

    @Override // com.android.server.wifi.cast.P2pSharing.P2pSharingInterface
    public void release() {
        HwHiLog.d(TAG, false, "release", new Object[0]);
        Handler handler = this.innerHandler;
        if (handler != null) {
            handler.removeMessages(0);
            this.innerHandler.removeMessages(1);
            this.innerHandler.removeMessages(2);
            this.innerHandler.removeMessages(4);
        }
        this.p2pSharingListener = null;
        DataChannel dataChannel2 = this.dataChannel;
        if (dataChannel2 != null) {
            dataChannel2.release();
            this.dataChannel = null;
        }
        DistributedGatewayStateCallback distributedGatewayStateCallback = this.stateCallback;
        if (distributedGatewayStateCallback != null) {
            DistributedGatewayManagerEx.unregStateCallback(distributedGatewayStateCallback);
            this.stateCallback = null;
        }
        InternetBorrowingRequestEx internetBorrowingRequestEx = this.requestEx;
        if (internetBorrowingRequestEx != null) {
            DistributedGatewayManagerEx.disableInternetBorrowing(internetBorrowingRequestEx);
            this.requestEx = null;
        }
        recoverWiFi();
        reset();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onStateChanged(int state) {
        if (state == 0) {
            handleInternetUnavailable();
        } else if (state == 1) {
            handleInternetAvailable();
        }
    }

    private void handleInternetAvailable() {
        HwHiLog.d(TAG, false, "handleInternetAvailable", new Object[0]);
        if (this.clientState == 4) {
            HwHiLog.d(TAG, false, "to STATE_CONFIGURED", new Object[0]);
            this.clientState = 5;
            feedbackWithCallback(0, 0);
            sendRequest(1007);
            CastOptChr castOptChr = CastOptChr.getInstance();
            if (castOptChr != null) {
                castOptChr.handleP2pSharingStarted();
            }
        }
    }

    private void handleInternetUnavailable() {
        HwHiLog.d(TAG, false, "handleInternetUnavailable", new Object[0]);
        if (this.clientState == 4) {
            feedbackWithCallback(1, 0);
        } else {
            feedbackWithCallback(3, 1);
        }
        stopP2pBorrowing();
    }

    private void handleInternetCheckTimeOut() {
        HwHiLog.d(TAG, false, "handleInternetCheckTimeOut", new Object[0]);
        feedbackWithCallback(1, 1);
    }

    private void handleChannelException() {
        HwHiLog.d(TAG, false, "handleChannelException", new Object[0]);
        if (this.clientState != 5) {
            feedbackWithCallback(1, 2);
        } else {
            feedbackWithCallback(3, 2);
        }
    }

    private void recoverWiFi() {
        if (!isNeedRecoverWiFi()) {
            HwHiLog.w(TAG, false, "needn't recover WiFi", new Object[0]);
            return;
        }
        HwHiLog.d(TAG, false, "recoverWiFi", new Object[0]);
        this.innerHandler.removeMessages(5);
        this.innerHandler.sendEmptyMessageDelayed(5, 4000);
        startWiFiScan(true);
        this.isWifiDisconnectByP2pSharing = false;
    }

    private boolean isNeedRecoverWiFi() {
        if (this.isWifiDisconnectByP2pSharing) {
            return !Utils.isWiFiConnected(this.context);
        }
        HwHiLog.w(TAG, false, "WiFi isn't disconnected by P2pSharing", new Object[0]);
        return false;
    }

    private void checkWiFiState() {
        boolean isConnected = Utils.isWiFiConnected(this.context);
        HwHiLog.d(TAG, false, "checkWiFiState:" + isConnected, new Object[0]);
        if (!isConnected) {
            Utils.enableWiFiAutoConnect(this.context, true);
            startWiFiScan(false);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void processScanResults(ScanResult[] scanResults) {
        HwHiLog.d(TAG, false, "processScanResults:" + scanResults.length, new Object[0]);
        boolean isTargetFound = false;
        int length = scanResults.length;
        int i = 0;
        while (true) {
            if (i >= length) {
                break;
            }
            WifiConfiguration targetWifiConfig = this.wifiConfigManager.getConfiguredNetworkForScanDetailAndCache(ScanResultUtil.toScanDetail(scanResults[i]));
            if (targetWifiConfig != null && this.savedConfig != null && targetWifiConfig.networkId == this.savedConfig.networkId) {
                HwHiLog.d(TAG, false, "scanResult contains target", new Object[0]);
                Utils.connectWiFi(this.context, targetWifiConfig.networkId);
                isTargetFound = true;
                break;
            }
            i++;
        }
        if (!isTargetFound) {
            Utils.connectWiFi(this.context, -1);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startWiFiScan(final boolean isNeedConnectLastNetwork) {
        HwHiLog.d(TAG, false, "startWiFiScan need process result:" + isNeedConnectLastNetwork, new Object[0]);
        WifiScanner.ScanSettings scanSettings = new WifiScanner.ScanSettings();
        scanSettings.type = 2;
        scanSettings.band = 7;
        scanSettings.reportEvents = 3;
        WorkSource workSource = new WorkSource((int) HwWifi2Manager.CLOSE_WIFI2_WIFI1_ROAM);
        WifiScanner scanner = WifiInjector.getInstance().getWifiScanner();
        WifiScanner.ScanListener scanListener = new WifiScanner.ScanListener() {
            /* class com.android.server.wifi.cast.P2pSharing.P2pSharingClient.AnonymousClass3 */

            public void onSuccess() {
                HwHiLog.d(P2pSharingClient.TAG, false, "onSuccess", new Object[0]);
            }

            public void onFailure(int reason, String description) {
                HwHiLog.d(P2pSharingClient.TAG, false, "onFailure:" + reason + " " + description, new Object[0]);
            }

            public void onResults(WifiScanner.ScanData[] scanDatas) {
                HwHiLog.d(P2pSharingClient.TAG, false, "onResults", new Object[0]);
                if (isNeedConnectLastNetwork) {
                    if (scanDatas == null || scanDatas[0] == null) {
                        Utils.enableWiFiAutoConnect(P2pSharingClient.this.context, true);
                        P2pSharingClient.this.startWiFiScan(false);
                        return;
                    }
                    P2pSharingClient.this.processScanResults(scanDatas[0].getResults());
                }
            }

            public void onFullResult(ScanResult fullScanResult) {
                HwHiLog.d(P2pSharingClient.TAG, false, "onFullResult", new Object[0]);
            }

            public void onPeriodChanged(int periodInMs) {
                HwHiLog.d(P2pSharingClient.TAG, false, "onPeriodChanged", new Object[0]);
            }
        };
        if (scanner != null) {
            scanner.startScan(scanSettings, scanListener, workSource);
        }
    }
}
