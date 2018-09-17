package com.android.server.emcom.grabservice;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings.System;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Stack;

public class WechatGrabController extends BaseGrabController {
    private static final int CONFIG_ENTRY_SIZE = 2;
    private static final String CONFIG_ENTRY_SPLITER = ":";
    private static final String CONFIG_ITEM_SPLITER = ";";
    private static final int FLAG_STATE_NEW_MONEY_NOTIFY_ARRVIVE = 1;
    private static final int FLAG_STATE_PKG_FOUND_SUCCESS = 2;
    private static final int FLAG_STATE_PKG_OPEND_SUCCESS = 3;
    private static final int FLAG_STATE_READY = 0;
    private static final int GRAB_TIMEOUT = 2;
    private static final int GRAB_TIMEOUT_VAL = 25000;
    private static final String KEY_TOUCH_DISABLE_MODE = "touch_disable_mode";
    private static final int NOTIFY_TIMEOUT = 1;
    private static final int NOTIFY_TIMEOUT_VAL = 30000;
    private static final String TAG = "GrabService";
    private static final int TOUCH_DISABLE_DISABLE = 0;
    private static final int TOUCH_DISABLE_ENABLE = 1;
    static final String WECHAT_PKGNAME = "com.tencent.mm";
    private Map<String, String> mConfigParams = new HashMap();
    private int mGrabingId;
    private Handler mHandler;
    private boolean mIsDeviceLockedBefore;
    private boolean mIsTouchDisableBefore;
    private boolean mIsWaittingResult;
    private int mNotifyId;
    private Random mRandrom = new Random();
    private boolean mReceiveGrabCmd;
    private AccessibilityService mService;
    private volatile int mState;
    private WechatMessage mWechatMessage;

    private static class ConfigItems {
        static final String BASE_DELAY = "baseDelay";
        static final String CHAT_ID_KEY = "chatIdKey";
        static final String DETAIL_UI = "DetailUi";
        static final String GRAB_FAIL_TIP = "grabFailTip";
        static final String GROUP_MSG_IDENTIFIER = "groupMsgIdentifier";
        static final String LAUNCHER_UI = "LauncherUi";
        static final String MSG_TYPE_KEY = "msgTypeKey";
        static final String OPEN_BUTTON = "openButton";
        static final String PACKET_IDENTIFIER = "packetIdentifier";
        static final String PACKET_KEYWORD = "packetKeyword";
        static final String PACKET_MSG = "packetMsg";
        static final String PACKET_TYPE = "packetType";
        static final String RECEIVE_UI = "ReceiveUi";

        private ConfigItems() {
        }
    }

    public WechatGrabController(AccessibilityService service, Handler handler, String pkgName) {
        super(pkgName);
        this.mService = service;
        this.mHandler = handler;
    }

    protected void handleWinStateChangeEvent(AccessibilityEvent event) {
        if (event.getClassName() == null) {
            Log.e(TAG, "event class name is null");
            return;
        }
        String launcherUi = (String) this.mConfigParams.get("LauncherUi");
        String receiveUi = (String) this.mConfigParams.get("ReceiveUi");
        String detailUi = (String) this.mConfigParams.get("DetailUi");
        if (TextUtils.isEmpty(launcherUi) || TextUtils.isEmpty(receiveUi) || TextUtils.isEmpty(detailUi)) {
            Log.w(TAG, "class name config is missing");
            return;
        }
        String className = event.getClassName().toString();
        if (className.endsWith(launcherUi) && this.mState == 1) {
            Log.d(TAG, "enter the wechat launcher UI");
            if (this.mReceiveGrabCmd) {
                searchPacket();
            }
        } else if (className.contains(receiveUi)) {
            if (this.mReceiveGrabCmd && this.mState == 2) {
                openPacket();
            } else if (!this.mReceiveGrabCmd && this.mState == 1) {
                Log.d(TAG, "enter receuve ui,send cancle message");
                afterGrabPacket(false);
                sendMessage((byte) 3);
            }
        } else if (className.endsWith(detailUi)) {
            if (this.mState == 3) {
                this.mIsWaittingResult = false;
                afterGrabPacket(true);
            } else if (this.mState == 2) {
                afterGrabPacket(false);
            }
        } else if (this.mIsWaittingResult && AutoGrabTools.getTopActivity(this.mService).endsWith(receiveUi) && getOpenPacketFailNode() != null) {
            this.mIsWaittingResult = false;
            afterGrabPacket(false);
        }
    }

    public void handleNotification(Notification notification) {
        if (notification != null) {
            String msgTypeKey = (String) this.mConfigParams.get("msgTypeKey");
            String chatIdKey = (String) this.mConfigParams.get("chatIdKey");
            if (TextUtils.isEmpty(msgTypeKey) || TextUtils.isEmpty(chatIdKey)) {
                Log.e(TAG, "notification config is missing.");
                return;
            }
            try {
                WechatMessage localWechatMessage = new WechatMessage(this.mService, notification, msgTypeKey, chatIdKey);
                if (isMoneyNotification(localWechatMessage)) {
                    Log.d(TAG, "new packet receive");
                    onNewPacketReceive(localWechatMessage);
                }
            } catch (NoSuchMethodException e) {
                Log.e(TAG, "check notification NoSuchMethodException.", e);
            } catch (InvocationTargetException e2) {
                Log.e(TAG, "check notification InvocationTargetException.", e2);
            } catch (IllegalAccessException e3) {
                Log.e(TAG, "check notification IllegalAccessException.", e3);
            }
        }
    }

    private void onNewPacketReceive(WechatMessage localWechatMessage) {
        this.mWechatMessage = localWechatMessage;
        this.mNotifyId = (this.mWechatMessage.wechatId + SystemClock.currentThreadTimeMillis()).hashCode();
        sendMessage((byte) 1);
        if (this.mState == 0) {
            this.mState = 1;
        }
        this.mHandler.sendMessageDelayed(Message.obtain(this.mHandler, 4, 1, this.mNotifyId), 30000);
    }

    private void sendMessage(byte eventType) {
        this.mHandler.obtainMessage(3, new BluetoothMessage((byte) 1, eventType, (byte) 1, AutoGrabTools.intToByteArray(this.mNotifyId))).sendToTarget();
    }

    private void afterGrabPacket(boolean result) {
        Log.d(TAG, "grab success:" + result);
        if (this.mIsDeviceLockedBefore) {
            Log.d(TAG, "device is locked before, lock device again");
            this.mService.performGlobalAction(1);
            backToHome();
            AutoGrabTools.lockDevice(this.mService);
        }
        if (this.mIsTouchDisableBefore) {
            System.putInt(this.mService.getContentResolver(), KEY_TOUCH_DISABLE_MODE, 1);
        }
        resetParams();
        AutoGrabTools.setAccessibilityServiceEnable(this.mService, AutoGrabService.AUTOGRAB_SERVICE_COMPOENT_NAME, false);
    }

    private void backToHome() {
        AutoGrabTools.backToHome(this.mService);
    }

    private boolean isMoneyNotification(IMessage message) {
        String packetMsgIndentifier = (String) this.mConfigParams.get("packetIdentifier");
        String packetMsgType = (String) this.mConfigParams.get("packetType");
        String groupMsgIndentifer = (String) this.mConfigParams.get("groupMsgIdentifier");
        if (TextUtils.isEmpty(packetMsgIndentifier) || TextUtils.isEmpty(packetMsgType) || TextUtils.isEmpty(groupMsgIndentifer)) {
            Log.e(TAG, "packet config is missing.");
            return false;
        }
        try {
            if (!message.isMoney(AutoGrabTools.unicode2String(packetMsgIndentifier), Integer.parseInt(packetMsgType))) {
                Log.d(TAG, "Ignore the message, it's normal message");
                return false;
            } else if (message.isGroupMessage(groupMsgIndentifer)) {
                return true;
            } else {
                Log.d(TAG, "Ignore the message, not a group chat");
                return false;
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "packet type format error.");
            return false;
        }
    }

    private void searchPacket() {
        Log.d(TAG, "enter searchPacket method.");
        SystemClock.sleep((long) getDelay());
        AccessibilityNodeInfo packet = getPacketByKeywords();
        if (packet != null) {
            this.mState = 2;
            packet.performAction(16);
            return;
        }
        Log.d(TAG, "not found the package.");
        afterGrabPacket(false);
    }

    private int getDelay() {
        int baseDelay = 1;
        if (this.mConfigParams.get("baseDelay") != null) {
            try {
                baseDelay = Integer.parseInt((String) this.mConfigParams.get("baseDelay"));
            } catch (NumberFormatException e) {
                Log.i(TAG, "parse delay error.");
            }
        }
        return this.mRandrom.nextInt(baseDelay) + baseDelay;
    }

    private void openPacket() {
        Log.d(TAG, "enter openPacket method");
        SystemClock.sleep((long) getDelay());
        AccessibilityNodeInfo openButton = getOpenPacketBtn();
        if (openButton == null || !TextUtils.isEmpty(openButton.getText())) {
            afterGrabPacket(false);
            return;
        }
        Log.d(TAG, "find the open packet button.");
        this.mState = 3;
        openButton.performAction(16);
        this.mIsWaittingResult = true;
    }

    private AccessibilityNodeInfo getPacketByKeywords() {
        String packetKeywords = (String) this.mConfigParams.get("packetKeyword");
        String packetMsgContainer = (String) this.mConfigParams.get("packetMsg");
        if (TextUtils.isEmpty(packetKeywords) || TextUtils.isEmpty(packetMsgContainer)) {
            Log.e(TAG, "packet keyword is empty.");
            return null;
        }
        packetKeywords = AutoGrabTools.unicode2String(packetKeywords);
        AccessibilityNodeInfo rootNode = this.mService.getRootInActiveWindow();
        if (rootNode == null) {
            Log.d(TAG, "root node is null!");
            return null;
        }
        List<AccessibilityNodeInfo> nodeInfos = rootNode.findAccessibilityNodeInfosByText(packetKeywords);
        int startIndex = nodeInfos.size() - 1;
        if (startIndex < 0) {
            return null;
        }
        int i = startIndex;
        while (i > -1) {
            AccessibilityNodeInfo node = (AccessibilityNodeInfo) nodeInfos.get(i);
            while (node != null && (node.isClickable() ^ 1) != 0) {
                node = node.getParent();
            }
            if (node == null || !node.getClassName().toString().equals(packetMsgContainer)) {
                i--;
            } else if (node.getContentDescription() != null) {
                return null;
            } else {
                Log.d(TAG, "package is found.");
                return node;
            }
        }
        return null;
    }

    private AccessibilityNodeInfo getOpenPacketBtn() {
        Log.d(TAG, "enter getOpenPacketBtn method.");
        String openButtonContainer = (String) this.mConfigParams.get("openButton");
        if (TextUtils.isEmpty(openButtonContainer)) {
            Log.e(TAG, "open packet config is missing.");
            return null;
        }
        AccessibilityNodeInfo rootNode = this.mService.getRootInActiveWindow();
        if (rootNode == null) {
            Log.w(TAG, "root node is null!");
            return null;
        }
        Stack<AccessibilityNodeInfo> stack = new Stack();
        stack.add(rootNode);
        while (!stack.isEmpty()) {
            AccessibilityNodeInfo node = (AccessibilityNodeInfo) stack.pop();
            if (node.getClassName().toString().equals(openButtonContainer)) {
                return node;
            }
            int k = node.getChildCount();
            if (k > 0) {
                for (int i = 0; i < k; i++) {
                    stack.add(node.getChild(i));
                }
            }
        }
        return null;
    }

    private AccessibilityNodeInfo getOpenPacketFailNode() {
        String grabFailIndentifier = (String) this.mConfigParams.get("grabFailTip");
        if (TextUtils.isEmpty(grabFailIndentifier)) {
            Log.d(TAG, "grab fail tips config is missing.");
            return null;
        }
        grabFailIndentifier = AutoGrabTools.unicode2String(grabFailIndentifier);
        AccessibilityNodeInfo rootNode = this.mService.getRootInActiveWindow();
        if (rootNode == null) {
            Log.d(TAG, "root node is null!");
            return null;
        }
        List<AccessibilityNodeInfo> nodes = rootNode.findAccessibilityNodeInfosByText(grabFailIndentifier);
        if (nodes == null || nodes.size() <= 0) {
            return null;
        }
        return (AccessibilityNodeInfo) nodes.get(0);
    }

    public boolean isGrabingNow() {
        return this.mState > 0;
    }

    private void sendPendingIntent() {
        if (this.mWechatMessage == null) {
            Log.d(TAG, "wechat message is null!");
            afterGrabPacket(false);
            return;
        }
        PendingIntent pendingIntent = this.mWechatMessage.pendingIntent;
        if (pendingIntent == null) {
            Log.d(TAG, "pendingIntent is null!");
            afterGrabPacket(false);
            return;
        }
        try {
            pendingIntent.send();
            this.mGrabingId = (this.mWechatMessage.wechatId + SystemClock.currentThreadTimeMillis()).hashCode();
            this.mHandler.sendMessageDelayed(Message.obtain(this.mHandler, 4, 2, this.mGrabingId), 25000);
        } catch (CanceledException e) {
            Log.e(TAG, "the PendingIntent is no longer allowing more intents to be sent through it.");
            afterGrabPacket(false);
        }
    }

    public void executeCommand(int cmdId, int notifyId) {
        boolean z = true;
        switch (cmdId) {
            case 4:
                Log.d(TAG, "user choose not to grab.");
                resetParams();
                break;
            case 6:
                if (this.mState == 1) {
                    this.mReceiveGrabCmd = true;
                    if (System.getInt(this.mService.getContentResolver(), KEY_TOUCH_DISABLE_MODE, 1) != 1) {
                        z = false;
                    }
                    this.mIsTouchDisableBefore = z;
                    System.putInt(this.mService.getContentResolver(), KEY_TOUCH_DISABLE_MODE, 0);
                    this.mIsDeviceLockedBefore = AutoGrabTools.unlockDevice(this.mService);
                    sendPendingIntent();
                    break;
                }
                Log.d(TAG, "receive a grab command, but no new package receive, ingore it");
                afterGrabPacket(false);
                return;
            default:
                Log.w(TAG, "unknown command id:" + cmdId);
                break;
        }
    }

    private void resetParams() {
        this.mState = 0;
        this.mNotifyId = 0;
        this.mGrabingId = 0;
        this.mWechatMessage = null;
        this.mReceiveGrabCmd = false;
        this.mIsWaittingResult = false;
        this.mIsTouchDisableBefore = false;
        this.mIsDeviceLockedBefore = false;
    }

    public void handleTimeout(int type, int id) {
        switch (type) {
            case 1:
                if (id == this.mNotifyId) {
                    Log.d(TAG, "nofity timeout, send cancle message.");
                    sendMessage((byte) 3);
                    afterGrabPacket(false);
                    return;
                }
                return;
            case 2:
                if (id == this.mGrabingId) {
                    Log.d(TAG, "grab timeout.");
                    afterGrabPacket(false);
                    return;
                }
                return;
            default:
                Log.w(TAG, "unknown timeout type:" + type);
                return;
        }
    }

    public void parseGrabParams(String params) {
        if (TextUtils.isEmpty(params)) {
            Log.d(TAG, "auto grab params is empty!");
            return;
        }
        for (String item : params.split(";")) {
            String[] entry = item.split(CONFIG_ENTRY_SPLITER);
            if (entry.length == 2) {
                this.mConfigParams.put(entry[0].trim(), entry[1].trim());
            } else {
                Log.w(TAG, "config item error, not key-vaule format.");
            }
        }
    }
}
