package com.android.server.wifi.cast.P2pSharing;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.wifi.HwHiLog;
import android.widget.Toast;
import com.android.server.wifi.cast.CastOptChr;
import com.huawei.distributedgw.DistributedGatewayManagerEx;
import com.huawei.distributedgw.DistributedGatewayStateCallback;
import com.huawei.distributedgw.InternetBorrowingRequestEx;
import com.huawei.distributedgw.InternetSharingRequestEx;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import org.json.JSONException;
import org.json.JSONObject;

public class P2pSharingServer implements P2pSharingInterface {
    private static final String EXIT_INTERFACE_NAME = "wlan0";
    private static final int MAX_TRY_TIME = 3;
    private static final String TAG = "P2pSharing:P2pSharingServer";
    private Context context;
    private DataChannel dataChannel = new DataChannel(this);
    private String deviceName = "";
    private DistributedGatewayStateCallback distributedGatewayStateCallback = new DistributedGatewayStateCallback() {
        /* class com.android.server.wifi.cast.P2pSharing.P2pSharingServer.AnonymousClass1 */

        public void onSharingStateChanged(InternetSharingRequestEx internetSharingRequestEx, int state) {
            HwHiLog.d(P2pSharingServer.TAG, false, "onSharingStateChanged:" + state, new Object[0]);
            P2pSharingServer.this.onStateChanged(state);
        }

        public void onBorrowingStateChanged(InternetBorrowingRequestEx internetBorrowingRequestEx, int i) {
            HwHiLog.d(P2pSharingServer.TAG, false, "onBorrowingStateChanged", new Object[0]);
        }
    };
    private Handler innerHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        /* class com.android.server.wifi.cast.P2pSharing.$$Lambda$P2pSharingServer$ODeYimtHWnuenWrEVbEwEtZx8EY */

        @Override // android.os.Handler.Callback
        public final boolean handleMessage(Message message) {
            return P2pSharingServer.this.lambda$new$0$P2pSharingServer(message);
        }
    });
    private boolean isConfiged = false;
    private InternetSharingRequestEx sharingRequest;

    public /* synthetic */ boolean lambda$new$0$P2pSharingServer(Message message) {
        if (message == null) {
            HwHiLog.d(TAG, false, "message is null", new Object[0]);
            return false;
        }
        HwHiLog.d(TAG, false, "handleMessage:" + message.what, new Object[0]);
        int i = message.what;
        if (i == 1000) {
            handleConfigServerRequest(message.obj);
        } else if (i == 1002) {
            stopP2pSharing();
        } else if (i == 1004) {
            this.dataChannel.createDynamicServerSocket();
        } else if (i != 1007) {
            HwHiLog.d(TAG, false, "unknown msg", new Object[0]);
        } else {
            showOptToast(this.deviceName);
        }
        return false;
    }

    private <T> void handleConfigServerRequest(T obj) {
        HwHiLog.d(TAG, false, "handleConfigServerRequest", new Object[0]);
        if (!(obj instanceof JSONObject)) {
            HwHiLog.d(TAG, false, "req format error", new Object[0]);
            return;
        }
        JSONObject extra = obj.optJSONObject(Constants.EXTRA);
        if (extra == null) {
            HwHiLog.d(TAG, false, "extra format error", new Object[0]);
        } else if (!Utils.isWiFiConnected(this.context)) {
            HwHiLog.d(TAG, false, "wifi is not connected", new Object[0]);
            sendRespWithResult(1001, 1, 1);
        } else {
            int clientWifiSecurity = extra.optInt(Constants.WIFI_SECURITY, -1);
            if (clientWifiSecurity == -1) {
                sendRespWithResult(1001, 1, 3);
                return;
            }
            int serverWifiSecurity = Utils.getWiFiSecurity(this.context);
            HwHiLog.d(TAG, false, "serverWifiSecurity:" + serverWifiSecurity, new Object[0]);
            if (serverWifiSecurity == 5 || serverWifiSecurity < clientWifiSecurity) {
                sendRespWithResult(1001, 1, 1);
                return;
            }
            String curBorrowingIp = "";
            this.deviceName = extra.optString(Constants.DEVICE_NAME, curBorrowingIp);
            int curBorrowingDevice = extra.optInt(Constants.DEVICE_TYPE, 1);
            DataChannel dataChannel2 = this.dataChannel;
            if (dataChannel2 != null) {
                curBorrowingIp = dataChannel2.getPeerIp();
            }
            startP2pSharing(curBorrowingDevice, curBorrowingIp);
        }
    }

    @Override // com.android.server.wifi.cast.P2pSharing.ChannelListener
    public void onDataReceived(byte[] data) {
        if (data == null || data.length <= 0) {
            HwHiLog.d(TAG, false, "receive invalid data", new Object[0]);
            sendRespWithResult(1009, 1, 3);
            return;
        }
        try {
            JSONObject respJson = new JSONObject(new String(data, StandardCharsets.UTF_8));
            Message msg = Message.obtain();
            msg.what = respJson.optInt(Constants.CMD_TYPE, 1009);
            msg.obj = respJson;
            this.innerHandler.sendMessage(msg);
        } catch (JSONException e) {
            HwHiLog.d(TAG, false, "parse failed", new Object[0]);
            sendRespWithResult(1009, 1, 3);
        }
    }

    @Override // com.android.server.wifi.cast.P2pSharing.ChannelListener
    public void onPortGet(int dynamicPort) {
        HwHiLog.d(TAG, false, "onPortGet", new Object[0]);
        if (!sendResp(1005, dynamicPort)) {
            HwHiLog.d(TAG, false, "send resp for port failed", new Object[0]);
            release();
        }
    }

    @Override // com.android.server.wifi.cast.P2pSharing.ChannelListener
    public void onChannelEvent(int eventId) {
        HwHiLog.d(TAG, false, "eventId " + eventId, new Object[0]);
        switch (eventId) {
            case 2002:
                this.dataChannel.closeFixedSocket();
                CastOptChr castOptChr = CastOptChr.getInstance();
                if (castOptChr != null) {
                    castOptChr.handleP2pSharingOptStarted();
                    return;
                }
                return;
            case 2003:
            case 2004:
            case 2007:
            case 2008:
                CastOptChr castOptChr2 = CastOptChr.getInstance();
                if (castOptChr2 != null) {
                    castOptChr2.handleP2pSharingFail(2);
                }
                release();
                return;
            case 2005:
            case 2006:
            default:
                return;
        }
    }

    private void showOptToast(String peerDeviceName) {
        HwHiLog.d(TAG, false, "show a toast", new Object[0]);
        Context context2 = this.context;
        if (context2 != null) {
            Toast.makeText(this.context, context2.getString(33686112, peerDeviceName), 1).show();
            CastOptChr castOptChr = CastOptChr.getInstance();
            if (castOptChr != null) {
                castOptChr.handleP2pSharingStarted();
            }
        }
    }

    private void startP2pSharing(int deviceType, String clientIp) {
        NetworkInterface ni = Utils.getP2pNetworkInterface();
        if (ni == null) {
            sendRespWithResult(1001, 1, 3);
            return;
        }
        String p2pInterfaceName = ni.getName();
        HwHiLog.d(TAG, false, "startP2pSharing:" + deviceType + " " + p2pInterfaceName, new Object[0]);
        this.sharingRequest = new InternetSharingRequestEx(deviceType, p2pInterfaceName);
        this.sharingRequest.setExitIfaceName(EXIT_INTERFACE_NAME);
        this.sharingRequest.setRequestIp(clientIp);
        if (DistributedGatewayManagerEx.isInternetSharing(this.sharingRequest)) {
            HwHiLog.d(TAG, false, p2pInterfaceName + " is already sharing", new Object[0]);
            sendConfigSuccessResp(4);
            return;
        }
        DistributedGatewayManagerEx.regStateCallback(this.distributedGatewayStateCallback);
        tryEnableInternetSharing();
    }

    private void tryEnableInternetSharing() {
        int tryTimes = 0;
        while (true) {
            if (tryTimes >= 3) {
                break;
            } else if (DistributedGatewayManagerEx.enableInternetSharing(this.sharingRequest)) {
                HwHiLog.d(TAG, false, "call enableInternetSharing ok", new Object[0]);
                break;
            } else {
                tryTimes++;
            }
        }
        if (tryTimes >= 3) {
            HwHiLog.e(TAG, false, "config failed", new Object[0]);
            sendRespWithResult(1001, 1, 3);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onStateChanged(int state) {
        if (state == 0) {
            handleNetworkUnavailable();
        } else if (state == 1) {
            this.isConfiged = true;
            sendConfigSuccessResp(0);
        }
    }

    private void handleNetworkUnavailable() {
        HwHiLog.d(TAG, false, "handleNetworkUnavailable", new Object[0]);
        if (this.isConfiged) {
            sendResp(1006, 1);
        } else {
            sendRespWithResult(1001, 1, 1);
        }
        stopP2pSharing();
    }

    private void stopP2pSharing() {
        HwHiLog.d(TAG, false, "stopP2pSharing", new Object[0]);
        if (DistributedGatewayManagerEx.isInternetSharing(this.sharingRequest)) {
            DistributedGatewayManagerEx.disableInternetSharing(this.sharingRequest);
        }
        CastOptChr castOptChr = CastOptChr.getInstance();
        if (castOptChr != null) {
            castOptChr.handleP2pSharingEnded();
        }
    }

    @Override // com.android.server.wifi.cast.P2pSharing.P2pSharingInterface
    public void setContext(Context context2) {
        this.context = context2;
    }

    @Override // com.android.server.wifi.cast.P2pSharing.P2pSharingInterface
    public void setUpTempConnection() {
        HwHiLog.d(TAG, false, "setUpTempConnection", new Object[0]);
        this.dataChannel.createFixedServerSocket();
    }

    private boolean sendData(JSONObject data) {
        DataChannel dataChannel2 = this.dataChannel;
        return dataChannel2 != null && dataChannel2.send(data.toString().getBytes(StandardCharsets.UTF_8));
    }

    private void sendConfigSuccessResp(int reasonId) {
        JSONObject resp = new JSONObject();
        try {
            resp.put(Constants.CMD_TYPE, 1001);
            resp.put(Constants.CONFIG_SERVER_RESULT, 0);
            resp.put(Constants.RESULT_REASON, reasonId);
            resp.put(Constants.DEVICE_NAME, Utils.getDeviceName());
            if (!sendData(resp)) {
                HwHiLog.d(TAG, false, "sendRespWithResult failed for config resp", new Object[0]);
            }
        } catch (JSONException e) {
            HwHiLog.d(TAG, false, "assemble config resp failed", new Object[0]);
        }
    }

    private boolean sendResp(int cmdType, int extra) {
        JSONObject resp = new JSONObject();
        try {
            resp.put(Constants.CMD_TYPE, cmdType);
            resp.put(Constants.EXTRA, extra);
            if (sendData(resp)) {
                return true;
            }
            HwHiLog.d(TAG, false, "sendResp failed for " + cmdType, new Object[0]);
            return false;
        } catch (JSONException e) {
            HwHiLog.d(TAG, false, "assemble response failed", new Object[0]);
            return false;
        }
    }

    private boolean sendRespWithResult(int cmdType, int resultId, int reasonId) {
        JSONObject resp = new JSONObject();
        try {
            resp.put(Constants.CMD_TYPE, cmdType);
            resp.put(Constants.CONFIG_SERVER_RESULT, resultId);
            resp.put(Constants.RESULT_REASON, reasonId);
            if (sendData(resp)) {
                return true;
            }
            HwHiLog.d(TAG, false, "sendRespWithResult failed for " + cmdType, new Object[0]);
            return false;
        } catch (JSONException e) {
            HwHiLog.d(TAG, false, "assemble response failed", new Object[0]);
            return false;
        }
    }

    @Override // com.android.server.wifi.cast.P2pSharing.P2pSharingInterface
    public void release() {
        HwHiLog.d(TAG, false, "release", new Object[0]);
        DataChannel dataChannel2 = this.dataChannel;
        if (dataChannel2 != null) {
            dataChannel2.release();
            this.dataChannel = null;
        }
        DistributedGatewayStateCallback distributedGatewayStateCallback2 = this.distributedGatewayStateCallback;
        if (distributedGatewayStateCallback2 != null) {
            DistributedGatewayManagerEx.unregStateCallback(distributedGatewayStateCallback2);
            this.distributedGatewayStateCallback = null;
        }
        InternetSharingRequestEx internetSharingRequestEx = this.sharingRequest;
        if (internetSharingRequestEx != null) {
            DistributedGatewayManagerEx.disableInternetSharing(internetSharingRequestEx);
            this.sharingRequest = null;
        }
        this.isConfiged = false;
    }

    @Override // com.android.server.wifi.cast.P2pSharing.P2pSharingInterface
    public void setP2pSharingListener(P2pSharingListener listener) {
    }

    @Override // com.android.server.wifi.cast.P2pSharing.P2pSharingInterface
    public void onP2pStateChanged(boolean isConnected) {
        HwHiLog.d(TAG, false, "onP2pStateChanged:" + isConnected, new Object[0]);
        if (!isConnected) {
            CastOptChr castOptChr = CastOptChr.getInstance();
            if (castOptChr != null) {
                castOptChr.handleP2pSharingFail(1);
            }
            stopP2pSharing();
        }
    }
}
