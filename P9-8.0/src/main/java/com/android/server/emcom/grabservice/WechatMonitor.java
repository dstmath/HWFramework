package com.android.server.emcom.grabservice;

import android.app.Notification;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.emcom.SmartcareInfos.WechatInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import com.android.server.emcom.EmcomThread;
import com.android.server.emcom.SmartcareConfigSerializer;
import com.android.server.emcom.SmartcareProc;
import com.android.server.emcom.grabservice.AutoGrabService.AccessibilityEventCallback;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.PatternSyntaxException;

public class WechatMonitor implements AccessibilityEventCallback {
    private static final int CONFIG_ENTRY_SIZE = 2;
    private static final String CONFIG_ENTRY_SPLITER = ":";
    private static final String CONFIG_ITEM_SPLITER = ";";
    private static final int INVALID_VAL = -2;
    private static final int MSG_GRAB_TIMEOUT = 2;
    private static final int MSG_OPEN_TIMEOUT = 3;
    private static final int MSG_SEND_TIMEOUT = 1;
    private static final int MSG_UNKNOWN = 0;
    private static final int SEND_WALLET_TIP_COUNT = 1;
    private static final String TAG = "WechatMonitor";
    private static final long TIMEOUT_VAL = 5000;
    public static final int TYPE_GRAB_LATENCY = 2;
    public static final int TYPE_OPEN_LATENCY = 3;
    public static final int TYPE_SEND_LATENCY = 1;
    public static final int TYPE_UNKNOWN = 0;
    private static final String WECHAT_PKGNAME = "com.tencent.mm";
    private static final String WECHAT_PROC_NAME = "sys.wechat.smartcare.sample.open";
    private Map<String, String> mConfigMap = new HashMap();
    private Context mContext;
    private int mCurrGrabHashcode;
    private int mCurrOpenHashcode;
    private int mCurrSendHashcode;
    private DateFormat mDateFormat = new SimpleDateFormat("yyMMdd", Locale.getDefault());
    private boolean mGrabPacketStart;
    private long mGrabPacketTime;
    private boolean mGrabSuccess;
    private Handler mHandler;
    private boolean mHasInit;
    private boolean mOpenPacketStart;
    private long mOpenPacketTime;
    private boolean mOpenSuccess;
    private boolean mSendPacketStart;
    private long mSendPacketStartTime;
    private boolean mSendStartButtonPressed;
    private boolean mSendSuccess;
    private DateFormat mTimeFormat = new SimpleDateFormat("hhmmss", Locale.getDefault());
    private int mWalletTipCount;

    private static class ConfigItems {
        static final String CHAT_ID_KEY = "ChatIdKey";
        static final String DETAIL_UI = "DetailUi";
        static final String GRAB_START_UI = "GrabStartUi";
        static final String GROUP_MSG_IDENTIFIER = "GroupMsgIdentifier";
        static final String MSG_TYPE_KEY = "MsgTypeKey";
        static final String OPEN_START_UI = "OpenStartUi";
        static final String PACKET_IDENTIFIER = "PacketIdentifier";
        static final String PACKET_KEYWORD = "PacketKeyword";
        static final String PACKET_TYPE = "PacketType";
        static final String RECEIVE_UI = "ReceiveUi";
        public static final String SEND_START_BUTTON = "SendStartButton";
        public static final String SEND_START_KEYWORD = "SendStartKeyword";
        static final String SEND_START_UI = "SendStartUi";
        static final String SEND_SUCC_TIP = "SendSuccTip";
        static final String TOAST_CONTAINER = "ToastContainer";
        static final String WALLET_TIP = "WalletTip";

        private ConfigItems() {
        }
    }

    private class WechatMonitorHandler extends Handler {
        public WechatMonitorHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            if (msg == null) {
                Log.e(WechatMonitor.TAG, "message is null.");
                return;
            }
            switch (msg.what) {
                case 1:
                    WechatMonitor.this.handSendTimeoutMsg(msg.arg1);
                    break;
                case 2:
                    WechatMonitor.this.handGrabTimeoutMsg(msg.arg1);
                    break;
                case 3:
                    WechatMonitor.this.handOpenTimeoutMsg(msg.arg1);
                    break;
                default:
                    Log.e(WechatMonitor.TAG, "Unknown message type.");
                    break;
            }
        }
    }

    public WechatMonitor(Context context) {
        this.mContext = context;
        this.mHandler = new WechatMonitorHandler(EmcomThread.getInstanceLooper());
    }

    public void initConfig() {
        Map<String, String> configMap = SmartcareConfigSerializer.getInstance().getWechatConfigMap();
        if (configMap != null && (configMap.isEmpty() ^ 1) != 0) {
            updateCurrentVersionConfig();
        }
    }

    public void updateCurrentVersionConfig() {
        String config = getConfig(getVersion());
        if (TextUtils.isEmpty(config)) {
            this.mHasInit = false;
            this.mConfigMap.clear();
            Log.d(TAG, "wechat not install or version not match, clear config.");
            return;
        }
        parseConfigItems(config);
        Log.d(TAG, "configMap=" + this.mConfigMap);
        this.mHasInit = true;
    }

    private void parseConfigItems(String config) {
        if (TextUtils.isEmpty(config)) {
            Log.d(TAG, "config params is empty!");
            return;
        }
        for (String item : config.split(";")) {
            if (!TextUtils.isEmpty(item)) {
                String[] entry = item.split(CONFIG_ENTRY_SPLITER);
                if (entry.length == 2) {
                    this.mConfigMap.put(entry[0].trim(), entry[1].trim());
                } else {
                    Log.w(TAG, "config item error, not key-vaule format.");
                }
            }
        }
    }

    private String getVersion() {
        String versionName = null;
        try {
            return this.mContext.getPackageManager().getPackageInfo(WECHAT_PKGNAME, 0).versionName;
        } catch (NameNotFoundException e) {
            Log.i(TAG, "wechat is not install");
            return versionName;
        }
    }

    private void handleWinStateChangeEvent(AccessibilityEvent event) {
        CharSequence className = event.getClassName();
        if (!TextUtils.isEmpty(className)) {
            if (className.equals(this.mConfigMap.get("SendStartUi"))) {
                checkSendStart(event);
            } else if (className.equals(this.mConfigMap.get("ReceiveUi"))) {
                checkGrabSuccess(event);
            } else if (className.equals(this.mConfigMap.get("DetailUi"))) {
                checkOpenSuccess(event);
            } else if (className.equals(this.mConfigMap.get("ToastContainer"))) {
                checkSendSuccess(event);
            }
        }
    }

    private void checkSendSuccess(AccessibilityEvent event) {
        List<CharSequence> tips = event.getText();
        if (tips != null) {
            for (CharSequence c : tips) {
                if (!TextUtils.isEmpty(c) && this.mSendPacketStart && c.equals(this.mConfigMap.get("SendSuccTip"))) {
                    sendSendSuccMsg(event.getEventTime() - this.mSendPacketStartTime);
                    this.mSendPacketStart = false;
                    this.mSendPacketStartTime = 0;
                    this.mSendStartButtonPressed = false;
                    this.mWalletTipCount = 0;
                    return;
                }
            }
        }
    }

    private void checkOpenSuccess(AccessibilityEvent event) {
        if (this.mOpenPacketStart) {
            long openDelay = event.getEventTime() - this.mOpenPacketTime;
            if (openDelay < TIMEOUT_VAL) {
                sendOpenSuccMsg(openDelay);
            }
            this.mOpenPacketStart = false;
            this.mOpenSuccess = true;
            return;
        }
        checkGrabSuccess(event);
    }

    private void checkGrabSuccess(AccessibilityEvent event) {
        if (this.mGrabPacketStart) {
            this.mGrabPacketStart = false;
            this.mGrabSuccess = true;
            long delay = event.getEventTime() - this.mGrabPacketTime;
            if (delay < TIMEOUT_VAL) {
                sendGrabSuccMsg(delay);
            }
        }
    }

    private void checkSendStart(AccessibilityEvent event) {
        if (this.mSendStartButtonPressed) {
            List<CharSequence> list = event.getText();
            if (list != null) {
                for (CharSequence cs : list) {
                    if (cs.equals(this.mConfigMap.get("WalletTip"))) {
                        if (this.mWalletTipCount >= 1) {
                            this.mSendPacketStart = true;
                            this.mWalletTipCount = 0;
                            this.mSendPacketStartTime = event.getEventTime();
                            Message msg = this.mHandler.obtainMessage(1);
                            this.mCurrSendHashcode = Long.hashCode(this.mSendPacketStartTime);
                            msg.arg1 = this.mCurrSendHashcode;
                            this.mHandler.sendMessageDelayed(msg, TIMEOUT_VAL);
                            Log.d(TAG, "packet send start.");
                            return;
                        }
                        Log.d(TAG, "walletTipCount++");
                        this.mWalletTipCount++;
                    }
                }
            }
        }
    }

    private void handleNotifyEvent(AccessibilityEvent event) {
        CharSequence className = event.getClassName();
        if (className != null && className.toString().startsWith("android.widget.Toast")) {
            checkSendSuccess(event);
        }
    }

    public boolean isMoneyNotification(IMessage message) {
        String type = (String) this.mConfigMap.get("PacketType");
        if (TextUtils.isEmpty(type)) {
            Log.e(TAG, "type code configuration is missing.");
            return false;
        }
        try {
            if (!message.isMoney((String) this.mConfigMap.get("PacketIdentifier"), Integer.parseInt(type))) {
                Log.d(TAG, "Ignore the message, it's normal message");
                return false;
            } else if (message.isGroupMessage((String) this.mConfigMap.get("GroupMsgIdentifier"))) {
                return true;
            } else {
                Log.d(TAG, "Ignore the message, not a group chat");
                return false;
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "parse message type error.", e);
            return false;
        }
    }

    public boolean isMoneyNotification(Notification notification) {
        if (this.mHasInit && notification != null) {
            String msgType = (String) this.mConfigMap.get("MsgTypeKey");
            String key = (String) this.mConfigMap.get("ChatIdKey");
            if (!(TextUtils.isEmpty(msgType) || (TextUtils.isEmpty(key) ^ 1) == 0)) {
                try {
                    return isMoneyNotification(new WechatMessage(this.mContext, notification, msgType, key));
                } catch (NoSuchMethodException e) {
                    Log.e(TAG, "check notification NoSuchMethodException.", e);
                } catch (InvocationTargetException e2) {
                    Log.e(TAG, "check notification InvocationTargetException.", e2);
                } catch (IllegalAccessException e3) {
                    Log.e(TAG, "check notification IllegalAccessException.", e3);
                }
            }
        }
        return false;
    }

    private void handleClickEvent(AccessibilityEvent event) {
        if (event == null || event.getClassName() == null) {
            Log.e(TAG, "event is null or class name is null.");
            return;
        }
        CharSequence className = event.getClassName();
        if (!TextUtils.isEmpty(className)) {
            if (className.equals(this.mConfigMap.get("GrabStartUi"))) {
                handleGrabStart(event);
            } else if (AutoGrabTools.getTopActivity(this.mContext).equals(this.mConfigMap.get("ReceiveUi")) && className.equals(this.mConfigMap.get("OpenStartUi"))) {
                handleOpenStart(event);
            } else if (className.equals(this.mConfigMap.get(ConfigItems.SEND_START_BUTTON))) {
                handleSendStart(event);
            }
        }
    }

    private void handleSendStart(AccessibilityEvent event) {
        List<CharSequence> list = event.getText();
        if (list != null) {
            for (CharSequence cs : list) {
                if (cs.equals(this.mConfigMap.get(ConfigItems.SEND_START_KEYWORD))) {
                    this.mSendStartButtonPressed = true;
                    this.mSendSuccess = false;
                    Log.d(TAG, "begin to send packet.");
                    return;
                }
            }
        }
    }

    private void handleOpenStart(AccessibilityEvent event) {
        Log.d(TAG, "click open button");
        this.mOpenPacketStart = true;
        this.mOpenSuccess = false;
        this.mOpenPacketTime = event.getEventTime();
        Message msg = this.mHandler.obtainMessage(3);
        this.mCurrOpenHashcode = Long.hashCode(this.mOpenPacketTime);
        msg.arg1 = this.mCurrOpenHashcode;
        this.mHandler.sendMessageDelayed(msg, TIMEOUT_VAL);
    }

    private void handleGrabStart(AccessibilityEvent event) {
        List<CharSequence> list = event.getText();
        if (list != null) {
            for (CharSequence cs : list) {
                if (cs.equals(this.mConfigMap.get("PacketKeyword"))) {
                    this.mGrabPacketStart = true;
                    this.mGrabSuccess = false;
                    this.mGrabPacketTime = event.getEventTime();
                    Log.d(TAG, "click packet time:" + this.mGrabPacketTime);
                    Message msg = this.mHandler.obtainMessage(2);
                    this.mCurrGrabHashcode = Long.hashCode(this.mGrabPacketTime);
                    msg.arg1 = this.mCurrGrabHashcode;
                    this.mHandler.sendMessageDelayed(msg, TIMEOUT_VAL);
                    return;
                }
            }
        }
    }

    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (!this.mHasInit || event == null) {
            Log.e(TAG, "event is null or hasn't init configuration.");
            return;
        }
        if (WECHAT_PKGNAME.equals(event.getPackageName())) {
            switch (event.getEventType()) {
                case 1:
                    handleClickEvent(event);
                    break;
                case 32:
                case 2048:
                    handleWinStateChangeEvent(event);
                    break;
                case 64:
                    handleNotifyEvent(event);
                    break;
                default:
                    Log.d(TAG, "other event type, ignore it");
                    break;
            }
        }
    }

    public boolean updateSampleWinEnable(boolean enable) {
        Log.d(TAG, "update sample window of wechat");
        if (enable) {
            return checkWinEnable();
        }
        resetAllParams();
        return false;
    }

    private boolean checkWinEnable() {
        if (!Boolean.valueOf(SystemProperties.get(WECHAT_PROC_NAME)).booleanValue()) {
            return false;
        }
        Log.d(TAG, "sample window is open");
        return true;
    }

    public String getConfig(String version) {
        if (TextUtils.isEmpty(version)) {
            return null;
        }
        for (Entry<String, String> entry : SmartcareConfigSerializer.getInstance().getWechatConfigMap().entrySet()) {
            if (versionMatch(version, (String) entry.getKey())) {
                return (String) entry.getValue();
            }
        }
        return null;
    }

    private boolean versionMatch(String version, String checkVersion) {
        if (!(TextUtils.isEmpty(version) || (TextUtils.isEmpty(checkVersion) ^ 1) == 0)) {
            try {
                return version.matches(checkVersion);
            } catch (PatternSyntaxException e) {
                Log.e(TAG, "version pattern format error.");
            }
        }
        return false;
    }

    private void resetAllParams() {
        this.mSendPacketStart = false;
        this.mGrabPacketStart = false;
        this.mOpenPacketStart = false;
        this.mSendSuccess = false;
        this.mGrabSuccess = false;
        this.mOpenSuccess = false;
        this.mSendStartButtonPressed = false;
        this.mSendPacketStartTime = 0;
        this.mGrabPacketTime = 0;
        this.mOpenPacketTime = 0;
        this.mCurrGrabHashcode = 0;
        this.mCurrOpenHashcode = 0;
        this.mCurrSendHashcode = 0;
        this.mWalletTipCount = 0;
    }

    private void sendSendSuccMsg(long latency) {
        Log.d(TAG, "packet send success,cost time=" + latency);
        this.mCurrSendHashcode = 0;
        sendMsgToIMonitor(1, (int) latency);
    }

    private void sendGrabSuccMsg(long latency) {
        Log.d(TAG, "grab packet success, grab latency=" + latency);
        this.mCurrGrabHashcode = 0;
        sendMsgToIMonitor(2, (int) latency);
    }

    private void sendOpenSuccMsg(long latency) {
        Log.d(TAG, "open packet success, open latency=" + latency);
        this.mCurrOpenHashcode = 0;
        sendMsgToIMonitor(3, (int) latency);
    }

    private void sendOpenFailMsg() {
        Log.d(TAG, "open packet timeout.");
        sendMsgToIMonitor(3, -2);
    }

    private void sendGrabFailMsg() {
        Log.d(TAG, "grab packet timeout.");
        sendMsgToIMonitor(2, -2);
    }

    private void sendSendFailMsg() {
        Log.d(TAG, "send packet timeout.");
        sendMsgToIMonitor(1, -2);
    }

    private void sendMsgToIMonitor(int type, int latancy) {
        Log.i(TAG, "sendMsgToIMonitor, eventType=" + type + ",latancy=" + latancy);
        if (checkWinEnable()) {
            SmartcareProc proc = SmartcareProc.getInstance();
            if (proc == null) {
                Log.e(TAG, "sendMsgToIMonitor error, SmartcareProc null reference");
                return;
            }
            WechatInfo wechatInfo = proc.getWechatInfo();
            wechatInfo.latancy = latancy;
            wechatInfo.successFlag = (byte) ((wechatInfo.latancy != -2 ? 1 : 0) & 255);
            wechatInfo.type = type;
            wechatInfo.startDate = Integer.parseInt(this.mDateFormat.format(Long.valueOf(System.currentTimeMillis())));
            wechatInfo.startTime = Integer.parseInt(this.mTimeFormat.format(Long.valueOf(System.currentTimeMillis())));
            wechatInfo.endTime = wechatInfo.startTime;
            proc.addToTask(WECHAT_PKGNAME, wechatInfo);
            return;
        }
        Log.i(TAG, "sample window is not enable.");
    }

    public void handOpenTimeoutMsg(int hashcode) {
        if (!this.mSendSuccess && hashcode == this.mCurrOpenHashcode) {
            sendOpenFailMsg();
            this.mCurrOpenHashcode = 0;
        }
    }

    public void handGrabTimeoutMsg(int hashcode) {
        if (!this.mGrabSuccess && hashcode == this.mCurrGrabHashcode) {
            sendGrabFailMsg();
            this.mCurrGrabHashcode = 0;
        }
    }

    public void handSendTimeoutMsg(int hashcode) {
        if (!this.mOpenSuccess && hashcode == this.mCurrSendHashcode) {
            sendSendFailMsg();
            this.mCurrSendHashcode = 0;
        }
    }
}
