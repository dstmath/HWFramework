package com.android.server.wifi.HwQoE;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import com.android.server.wifi.scanner.ScanResultRecords;
import com.huawei.hilink.framework.aidl.CallRequest;
import com.huawei.hilink.framework.aidl.DiscoverRequest;
import com.huawei.hilink.framework.aidl.HilinkServiceProxy;
import com.huawei.hilink.framework.aidl.ResponseCallbackWrapper;
import com.huawei.hilink.framework.aidl.ServiceFoundCallbackWrapper;
import com.huawei.hilink.framework.aidl.ServiceRecord;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HwQoEHilink {
    private static final int ACC_FAST_DETECT_INTVAL_MSEC = 3000;
    private static final int ACC_NORMAL_DETECT_INTVAL_MSEC = 30000;
    private static final int ACC_STATE_SWITCH_DELAY_TIME_MSEC = 2000;
    private static final String ANDROID_PACKAGE_INSTALLER = "com.android.packageinstaller";
    private static final int APP_NAME_MAX_LEN = 64;
    private static final int DISCONNECT_HILINK_DELAY_TIME_MSEC = 1080000;
    private static final int DISCOVER_LIMIT_RETRY_TIMES = 3;
    private static final int FAST_DETECT_LIMIT_TIMES = 20;
    private static final int FIVE_ELEMENT_LIMIT_NUM = 60;
    private static final int GAME_INFO_SPLIT_INDEX_PORT = 2;
    private static final int GAME_INFO_SPLIT_INDEX_UID = 8;
    private static final int GAME_INFO_SPLIT_MIN_NUM = 9;
    private static final int MSG_HIGAME_FAST_DETECT = 1;
    private static final int MSG_HIGAME_NORMAL_DETECT = 2;
    private static final int MSG_HIGAME_SET_ACC_GAME_MODE = 0;
    private static final int PROTOCOL_TYPE_TCP = 6;
    private static final int PROTOCOL_TYPE_UDP = 17;
    private static final String TAG = "HwQoEHilink";
    private static HwQoEHilink mHwQoEHilink;
    /* access modifiers changed from: private */
    public boolean mAccGameDataDetected = false;
    /* access modifiers changed from: private */
    public boolean mAccGameEnabled = false;
    /* access modifiers changed from: private */
    public AccGameHandler mAccGameHandler;
    private String mAppName = "";
    private final Context mContext;
    private long mElapsedScreenOffTime = 0;
    /* access modifiers changed from: private */
    public int mFastDetectTimes = 0;
    private boolean mGameEnabled = false;
    /* access modifiers changed from: private */
    public int mHilinkAccRetryTimes = 0;
    /* access modifiers changed from: private */
    public boolean mHilinkAccSupport = false;
    private boolean mHilinkServiceOpened = false;
    private HilinkServiceProxy mHilinkServiceProxy;
    /* access modifiers changed from: private */
    public String mRemoteHilinkIp = "";
    /* access modifiers changed from: private */
    public int mRemoteHilinkPort = -1;
    private int mWifiState = HwQoEUtils.QOE_MSG_WIFI_DISCONNECT;

    private class AccGameHandler extends Handler {
        private AccGameHandler() {
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    HwQoEHilink.this.setAccGameMode();
                    return;
                case 1:
                    HwQoEHilink.this.accGameAction(true);
                    if (HwQoEHilink.this.mAccGameEnabled) {
                        if (HwQoEHilink.this.mFastDetectTimes < 20) {
                            int unused = HwQoEHilink.this.mFastDetectTimes = HwQoEHilink.this.mFastDetectTimes + 1;
                        }
                        if (HwQoEHilink.this.mFastDetectTimes >= 20 || HwQoEHilink.this.mAccGameDataDetected) {
                            HwQoEHilink.this.mAccGameHandler.sendEmptyMessageDelayed(2, 30000);
                            return;
                        } else {
                            HwQoEHilink.this.mAccGameHandler.sendEmptyMessageDelayed(1, 3000);
                            return;
                        }
                    } else {
                        return;
                    }
                case 2:
                    HwQoEHilink.this.accGameAction(true);
                    if (HwQoEHilink.this.mAccGameEnabled) {
                        HwQoEHilink.this.mAccGameHandler.sendEmptyMessageDelayed(2, 30000);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    private static class ResponseCallback extends ResponseCallbackWrapper {
        private ResponseCallback() {
        }

        public void onRecieveError(int errorCode) throws RemoteException {
            Log.d(HwQoEHilink.TAG, "response recieve error : " + errorCode);
        }

        public void onRecieveResponse(int callID, String payload) throws RemoteException {
            Log.d(HwQoEHilink.TAG, "response recieve callID : " + callID + " payload: " + payload);
        }
    }

    private class ServiceFoundCallback extends ServiceFoundCallbackWrapper {
        private ServiceFoundCallback() {
        }

        public void onFoundError(int errorCode) throws RemoteException {
            Log.d(HwQoEHilink.TAG, "service found error = " + errorCode);
        }

        public void onFoundService(ServiceRecord serviceRecord) throws RemoteException {
            if (serviceRecord == null) {
                Log.d(HwQoEHilink.TAG, "service Record is null!");
                return;
            }
            boolean unused = HwQoEHilink.this.mHilinkAccSupport = true;
            int unused2 = HwQoEHilink.this.mHilinkAccRetryTimes = 0;
            String unused3 = HwQoEHilink.this.mRemoteHilinkIp = serviceRecord.getRemoteIP();
            int unused4 = HwQoEHilink.this.mRemoteHilinkPort = serviceRecord.getRemotePort();
            HwQoEHilink.this.sendGameAccInfo(true);
        }
    }

    public static synchronized HwQoEHilink getInstance(Context ctx) {
        HwQoEHilink hwQoEHilink;
        synchronized (HwQoEHilink.class) {
            if (mHwQoEHilink == null) {
                mHwQoEHilink = new HwQoEHilink(ctx);
            }
            hwQoEHilink = mHwQoEHilink;
        }
        return hwQoEHilink;
    }

    public void handleAccGameStateChanged(boolean enable, String appName) {
        if (!TextUtils.isEmpty(appName)) {
            this.mGameEnabled = enable;
            this.mAppName = appName;
            triggerAccSwitch();
        }
    }

    public void handleAccWifiStateChanged(int state) {
        this.mWifiState = state;
        if (state != 115) {
            switch (state) {
                case HwQoEUtils.QOE_MSG_WIFI_DISABLE /*108*/:
                case HwQoEUtils.QOE_MSG_WIFI_DISCONNECT /*110*/:
                    break;
                case HwQoEUtils.QOE_MSG_WIFI_CONNECTED /*109*/:
                    triggerAccSwitch();
                    return;
                default:
                    Log.e(TAG, "unknow wifi state");
                    return;
            }
        }
        initAccGameParams();
    }

    public void handleScreenStateChanged(boolean isScreenOn) {
        Log.d(TAG, "handleScreenStateChanged, isScreenOn :" + isScreenOn);
        if (!isScreenOn) {
            this.mElapsedScreenOffTime = SystemClock.elapsedRealtime();
        } else if (this.mElapsedScreenOffTime != 0 && this.mHilinkServiceOpened && SystemClock.elapsedRealtime() - this.mElapsedScreenOffTime > 1080000) {
            Log.d(TAG, "screen off > 18mins, reset hilink");
            this.mElapsedScreenOffTime = 0;
            initAccGameParams();
            triggerAccSwitch();
        }
    }

    private HwQoEHilink(Context ctx) {
        this.mContext = ctx;
        this.mAccGameHandler = new AccGameHandler();
    }

    private void initAccGameParams() {
        Log.d(TAG, "init Game config");
        this.mRemoteHilinkIp = "";
        this.mAccGameEnabled = false;
        this.mHilinkAccSupport = false;
        this.mAccGameDataDetected = false;
        this.mRemoteHilinkPort = -1;
        this.mHilinkAccRetryTimes = 0;
        this.mFastDetectTimes = 0;
        stopAccTimer();
        if (this.mHilinkServiceOpened) {
            this.mHilinkServiceProxy.close();
            this.mHilinkServiceOpened = false;
        }
    }

    private boolean isHilinkGateway() {
        WifiManager wifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        if (wifiManager == null) {
            Log.d(TAG, "wifiManager is null!");
            return false;
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo == null) {
            Log.d(TAG, "wifiInfo is null!");
            return false;
        }
        String bssid = wifiInfo.getBSSID();
        if (bssid == null) {
            Log.d(TAG, "bssid is null!");
            return false;
        } else if (ScanResultRecords.getDefault().isHiLink(bssid)) {
            return true;
        } else {
            return false;
        }
    }

    private void triggerAccSwitch() {
        boolean enable;
        if (!isHilinkGateway() || !this.mGameEnabled || this.mWifiState != 109) {
            enable = false;
        } else {
            enable = true;
        }
        if (this.mAccGameEnabled != enable) {
            if (this.mHilinkAccSupport || this.mHilinkAccRetryTimes <= 3) {
                Log.d(TAG, "enable: " + enable + ", appName: " + this.mAppName);
                this.mAccGameEnabled = enable;
                if (this.mAccGameHandler.hasMessages(0)) {
                    this.mAccGameHandler.removeMessages(0);
                }
                if (!this.mHilinkServiceOpened) {
                    this.mAccGameHandler.sendEmptyMessageDelayed(0, 2000);
                } else {
                    this.mAccGameHandler.sendEmptyMessage(0);
                }
                return;
            }
            Log.d(TAG, "triggerAccSwitch, router don't support game acceleration!");
            this.mAccGameEnabled = false;
            stopAccTimer();
        }
    }

    /* access modifiers changed from: private */
    public void setAccGameMode() {
        Log.d(TAG, "set acc game mode, acc: " + this.mAccGameEnabled);
        if (!this.mHilinkServiceOpened) {
            this.mHilinkServiceProxy = new HilinkServiceProxy(this.mContext);
            this.mHilinkServiceOpened = true;
        }
        if (this.mAccGameEnabled) {
            startAccTimer();
            return;
        }
        stopAccTimer();
        if (this.mHilinkAccSupport) {
            sendGameAccInfo(false);
        }
    }

    private void startAccTimer() {
        Log.d(TAG, "start acc timer");
        this.mAccGameDataDetected = false;
        this.mFastDetectTimes = 0;
        this.mAccGameHandler.sendEmptyMessageDelayed(1, 3000);
    }

    private void stopAccTimer() {
        Log.d(TAG, "stop acc timer");
        this.mAccGameDataDetected = false;
        this.mFastDetectTimes = 0;
        if (this.mAccGameHandler.hasMessages(1)) {
            this.mAccGameHandler.removeMessages(1);
        }
        if (this.mAccGameHandler.hasMessages(2)) {
            this.mAccGameHandler.removeMessages(2);
        }
        if (this.mAccGameHandler.hasMessages(0)) {
            this.mAccGameHandler.removeMessages(0);
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0025, code lost:
        return;
     */
    public synchronized void accGameAction(boolean accelerate) {
        if (!this.mHilinkAccSupport && this.mHilinkAccRetryTimes > 3) {
            Log.d(TAG, "accGameAction, router don't support game acceleration!");
            this.mAccGameEnabled = false;
            stopAccTimer();
        } else if (this.mHilinkAccSupport) {
            sendGameAccInfo(accelerate);
        } else {
            detectAndSendGameAccInfo();
        }
    }

    private void detectAndSendGameAccInfo() {
        DiscoverRequest.Builder builder = new DiscoverRequest.Builder();
        builder.setServiceType("st=appawareMngr");
        DiscoverRequest request = builder.build();
        Log.d(TAG, "prepare to detect hilik ability");
        this.mHilinkAccRetryTimes++;
        if (request == null || !this.mHilinkServiceOpened) {
            Log.d(TAG, "discover request or HilinkServiceProxy is null!");
            return;
        }
        if (this.mHilinkServiceProxy.discover(request, new ServiceFoundCallback()) != 0) {
            Log.d(TAG, "discover Service failed! ret = " + ret);
        }
    }

    /* access modifiers changed from: private */
    public void sendGameAccInfo(boolean accelerate) {
        if (!this.mHilinkAccSupport) {
            Log.d(TAG, "sendGameAccInfo,router don't support game acceleration!");
        } else if (TextUtils.isEmpty(this.mRemoteHilinkIp) || this.mRemoteHilinkPort < 0) {
            Log.d(TAG, "remote hilink ip or port is invalid, don't send info to it");
        } else {
            Log.d(TAG, "prepare to send hilik message");
            String payload = buildHilinkPayload(accelerate);
            if (payload != null) {
                Log.d(TAG, "payload length: " + payload.length());
                CallRequest.Builder builder = new CallRequest.Builder();
                builder.setServiceID("appawareMngr");
                builder.setMethod(1);
                builder.setRemoteIP(this.mRemoteHilinkIp).setRemotePort(this.mRemoteHilinkPort);
                builder.setPayload(payload);
                CallRequest request = builder.build();
                if (request == null || !this.mHilinkServiceOpened) {
                    Log.d(TAG, "call request or HilinkServiceProxy is null!");
                } else {
                    int ret = this.mHilinkServiceProxy.call(request, new ResponseCallback());
                    if (ret != 0) {
                        Log.d(TAG, "call failed! ret = " + ret);
                    }
                }
            }
        }
    }

    private String buildHilinkPayload(boolean accelerate) {
        String appName;
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        if (this.mAppName == null) {
            Log.d(TAG, "appName is null!");
            return null;
        }
        HwQoEContentAware hwQoEContentAware = HwQoEContentAware.getInstance();
        if (hwQoEContentAware == null) {
            Log.d(TAG, "hwQoEContentAware is null");
            return null;
        }
        int appUid = hwQoEContentAware.getAppUid(this.mAppName);
        if (appUid < 0) {
            Log.d(TAG, "the game is not exist,appName: " + this.mAppName);
            this.mAccGameEnabled = false;
            stopAccTimer();
            return null;
        }
        Log.d(TAG, "buildHilinkPayload, accelerate: " + accelerate + ", appUid: " + appUid + ", appName: " + this.mAppName);
        WifiManager wifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        if (wifiManager == null) {
            Log.d(TAG, "wifiManager is null!");
            return null;
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo == null) {
            Log.d(TAG, "wifiInfo is null!");
            return null;
        }
        String localIP = transIPHexToStr(wifiInfo.getIpAddress());
        try {
            if (this.mAppName.length() > APP_NAME_MAX_LEN) {
                appName = this.mAppName.substring(0, APP_NAME_MAX_LEN);
            } else {
                appName = this.mAppName;
            }
            if (accelerate) {
                buildSocketInfo(appUid, 6, jsonArray, localIP);
                buildSocketInfo(appUid, 17, jsonArray, localIP);
                if (jsonArray.length() == 0) {
                    Log.d(TAG, "do not found game 5 elements");
                    return null;
                }
                this.mAccGameDataDetected = true;
                jsonObject.put("action", "create");
                jsonObject.put("pkgName", appName);
                jsonObject.put("accelMode", "");
                jsonObject.put("data", jsonArray);
            } else {
                JSONObject json5elem = new JSONObject();
                json5elem.put("clientIp", localIP);
                json5elem.put("clientPort", 0);
                json5elem.put("serverIp", "");
                json5elem.put("serverPort", 0);
                json5elem.put("proto", 0);
                jsonArray.put(json5elem);
                jsonObject.put("action", "delete");
                jsonObject.put("pkgName", appName);
                jsonObject.put("accelMode", "");
                jsonObject.put("data", jsonArray);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Json Exception", e);
        } catch (IOException e2) {
            Log.e(TAG, "IO Exception", e2);
        }
        return jsonObject.toString();
    }

    private String transIPHexToStr(int ip) {
        return (ip & 255) + "." + ((ip >> 8) & 255) + "." + ((ip >> 16) & 255) + "." + ((ip >> 24) & 255);
    }

    private void buildSocketInfo(int uid, int protoType, JSONArray jsonArray, String clientIp) throws IOException, JSONException {
        String filePath;
        String filePath2;
        int i = protoType;
        if (i == 6) {
            filePath = "/proc/net/tcp";
        } else if (i != 17) {
            Log.d(TAG, "invalid protocol: " + i);
            return;
        } else {
            filePath = "/proc/net/udp";
        }
        String filePath3 = filePath;
        try {
            FileInputStream fs = new FileInputStream(filePath3);
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(fs, "UTF-8"));
                Pattern patternPort = Pattern.compile(":([0-9|A-F|a-f]{4})");
                String strUid = Integer.toString(uid);
                StringBuffer bufDbg = new StringBuffer();
                String lineData = br.readLine();
                while (true) {
                    String lineData2 = lineData;
                    if (lineData2 == null) {
                        JSONArray jSONArray = jsonArray;
                        String str = filePath3;
                        break;
                    } else if (jsonArray.length() >= FIVE_ELEMENT_LIMIT_NUM) {
                        Log.d(TAG, "five element reach the max limit!");
                        JSONArray jSONArray2 = jsonArray;
                        String str2 = filePath3;
                        break;
                    } else {
                        String[] splitData = lineData2.split("\\s+");
                        if (splitData.length < 9) {
                            Log.d(TAG, "file data is not correct!");
                            br.close();
                            return;
                        }
                        if (splitData[8].equals(strUid)) {
                            if (splitData[2].contains("0100007F")) {
                                lineData = br.readLine();
                            } else {
                                Matcher matcher = patternPort.matcher(splitData[2]);
                                if (matcher.find()) {
                                    try {
                                        int clientPort = Integer.decode("0x" + matcher.group(1)).intValue();
                                        JSONObject json5elem = new JSONObject();
                                        filePath2 = filePath3;
                                        json5elem.put("clientIp", clientIp);
                                        json5elem.put("clientPort", clientPort);
                                        json5elem.put("serverIp", "");
                                        json5elem.put("serverPort", 0);
                                        json5elem.put("proto", i);
                                        jsonArray.put(json5elem);
                                        bufDbg.append(" ");
                                        bufDbg.append(clientPort);
                                        lineData = br.readLine();
                                        filePath3 = filePath2;
                                    } catch (Exception e) {
                                        JSONArray jSONArray3 = jsonArray;
                                        String str3 = filePath3;
                                        String filePath4 = clientIp;
                                        Log.e(TAG, "getFlowInfo Exception", e);
                                        br.close();
                                        return;
                                    }
                                }
                            }
                        }
                        JSONArray jSONArray4 = jsonArray;
                        filePath2 = filePath3;
                        lineData = br.readLine();
                        filePath3 = filePath2;
                    }
                }
                String strDbg = bufDbg.toString();
                if (!TextUtils.isEmpty(strDbg)) {
                    Log.d(TAG, "protocol: " + i + ", port: " + strDbg);
                }
                br.close();
            } catch (UnsupportedEncodingException e2) {
                JSONArray jSONArray5 = jsonArray;
                String str4 = filePath3;
                Log.e(TAG, "UnsupportedEncoding Exception", e2);
                fs.close();
            }
        } catch (FileNotFoundException e3) {
            JSONArray jSONArray6 = jsonArray;
            String str5 = filePath3;
            Log.e(TAG, "FileNotFound Exception", e3);
        }
    }
}
