package com.android.server.wifi.aware;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.RttManager.RttParams;
import android.net.wifi.aware.Characteristics;
import android.net.wifi.aware.ConfigRequest;
import android.net.wifi.aware.ConfigRequest.Builder;
import android.net.wifi.aware.IWifiAwareDiscoverySessionCallback;
import android.net.wifi.aware.IWifiAwareEventCallback;
import android.net.wifi.aware.PublishConfig;
import android.net.wifi.aware.SubscribeConfig;
import android.net.wifi.aware.WifiAwareNetworkSpecifier;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;
import com.android.internal.util.MessageUtils;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.internal.util.WakeupMessage;
import com.android.server.wifi.hotspot2.anqp.Constants;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class WifiAwareStateManager {
    private static final byte[] ALL_ZERO_MAC = new byte[]{(byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0};
    private static final int COMMAND_TYPE_CONNECT = 100;
    private static final int COMMAND_TYPE_CREATE_ALL_DATA_PATH_INTERFACES = 112;
    private static final int COMMAND_TYPE_CREATE_DATA_PATH_INTERFACE = 114;
    private static final int COMMAND_TYPE_DELETE_ALL_DATA_PATH_INTERFACES = 113;
    private static final int COMMAND_TYPE_DELETE_DATA_PATH_INTERFACE = 115;
    private static final int COMMAND_TYPE_DISABLE_USAGE = 109;
    private static final int COMMAND_TYPE_DISCONNECT = 101;
    private static final int COMMAND_TYPE_ENABLE_USAGE = 108;
    private static final int COMMAND_TYPE_END_DATA_PATH = 118;
    private static final int COMMAND_TYPE_ENQUEUE_SEND_MESSAGE = 107;
    private static final int COMMAND_TYPE_GET_CAPABILITIES = 111;
    private static final int COMMAND_TYPE_INITIATE_DATA_PATH_SETUP = 116;
    private static final int COMMAND_TYPE_PUBLISH = 103;
    private static final int COMMAND_TYPE_RESPOND_TO_DATA_PATH_SETUP_REQUEST = 117;
    private static final int COMMAND_TYPE_START_RANGING = 110;
    private static final int COMMAND_TYPE_SUBSCRIBE = 105;
    private static final int COMMAND_TYPE_TERMINATE_SESSION = 102;
    private static final int COMMAND_TYPE_TRANSMIT_NEXT_MESSAGE = 119;
    private static final int COMMAND_TYPE_UPDATE_PUBLISH = 104;
    private static final int COMMAND_TYPE_UPDATE_SUBSCRIBE = 106;
    private static final boolean DBG = false;
    public static final String HAL_COMMAND_TIMEOUT_TAG = "WifiAwareStateManager HAL Command Timeout";
    public static final String HAL_DATA_PATH_CONFIRM_TIMEOUT_TAG = "WifiAwareStateManager HAL Data Path Confirm Timeout";
    public static final String HAL_SEND_MESSAGE_TIMEOUT_TAG = "WifiAwareStateManager HAL Send Message Timeout";
    private static final String MESSAGE_BUNDLE_KEY_CALLING_PACKAGE = "calling_package";
    private static final String MESSAGE_BUNDLE_KEY_CHANNEL = "channel";
    private static final String MESSAGE_BUNDLE_KEY_CHANNEL_REQ_TYPE = "channel_request_type";
    private static final String MESSAGE_BUNDLE_KEY_CONFIG = "config";
    private static final String MESSAGE_BUNDLE_KEY_FILTER_DATA = "filter_data";
    private static final String MESSAGE_BUNDLE_KEY_INTERFACE_NAME = "interface_name";
    private static final String MESSAGE_BUNDLE_KEY_MAC_ADDRESS = "mac_address";
    private static final String MESSAGE_BUNDLE_KEY_MESSAGE = "message";
    private static final String MESSAGE_BUNDLE_KEY_MESSAGE_ARRIVAL_SEQ = "message_arrival_seq";
    private static final String MESSAGE_BUNDLE_KEY_MESSAGE_DATA = "message_data";
    private static final String MESSAGE_BUNDLE_KEY_MESSAGE_ID = "message_id";
    private static final String MESSAGE_BUNDLE_KEY_MESSAGE_PEER_ID = "message_peer_id";
    private static final String MESSAGE_BUNDLE_KEY_NOTIFY_IDENTITY_CHANGE = "notify_identity_chg";
    private static final String MESSAGE_BUNDLE_KEY_PASSPHRASE = "passphrase";
    private static final String MESSAGE_BUNDLE_KEY_PEER_ID = "peer_id";
    private static final String MESSAGE_BUNDLE_KEY_PID = "pid";
    private static final String MESSAGE_BUNDLE_KEY_PMK = "pmk";
    private static final String MESSAGE_BUNDLE_KEY_RANGING_ID = "ranging_id";
    private static final String MESSAGE_BUNDLE_KEY_REQ_INSTANCE_ID = "req_instance_id";
    private static final String MESSAGE_BUNDLE_KEY_RETRY_COUNT = "retry_count";
    private static final String MESSAGE_BUNDLE_KEY_SEND_MESSAGE_ENQUEUE_TIME = "message_queue_time";
    private static final String MESSAGE_BUNDLE_KEY_SENT_MESSAGE = "send_message";
    private static final String MESSAGE_BUNDLE_KEY_SESSION_ID = "session_id";
    private static final String MESSAGE_BUNDLE_KEY_SESSION_TYPE = "session_type";
    private static final String MESSAGE_BUNDLE_KEY_SSI_DATA = "ssi_data";
    private static final String MESSAGE_BUNDLE_KEY_STATUS_CODE = "status_code";
    private static final String MESSAGE_BUNDLE_KEY_SUCCESS_FLAG = "success_flag";
    private static final String MESSAGE_BUNDLE_KEY_UID = "uid";
    private static final int MESSAGE_TYPE_COMMAND = 1;
    private static final int MESSAGE_TYPE_DATA_PATH_TIMEOUT = 6;
    private static final int MESSAGE_TYPE_NOTIFICATION = 3;
    private static final int MESSAGE_TYPE_RESPONSE = 2;
    private static final int MESSAGE_TYPE_RESPONSE_TIMEOUT = 4;
    private static final int MESSAGE_TYPE_SEND_MESSAGE_TIMEOUT = 5;
    private static final int NOTIFICATION_TYPE_AWARE_DOWN = 306;
    private static final int NOTIFICATION_TYPE_CLUSTER_CHANGE = 302;
    private static final int NOTIFICATION_TYPE_INTERFACE_CHANGE = 301;
    private static final int NOTIFICATION_TYPE_MATCH = 303;
    private static final int NOTIFICATION_TYPE_MESSAGE_RECEIVED = 305;
    private static final int NOTIFICATION_TYPE_ON_DATA_PATH_CONFIRM = 310;
    private static final int NOTIFICATION_TYPE_ON_DATA_PATH_END = 311;
    private static final int NOTIFICATION_TYPE_ON_DATA_PATH_REQUEST = 309;
    private static final int NOTIFICATION_TYPE_ON_MESSAGE_SEND_FAIL = 308;
    private static final int NOTIFICATION_TYPE_ON_MESSAGE_SEND_SUCCESS = 307;
    private static final int NOTIFICATION_TYPE_SESSION_TERMINATED = 304;
    private static final int RESPONSE_TYPE_ON_CAPABILITIES_UPDATED = 206;
    private static final int RESPONSE_TYPE_ON_CONFIG_FAIL = 201;
    private static final int RESPONSE_TYPE_ON_CONFIG_SUCCESS = 200;
    private static final int RESPONSE_TYPE_ON_CREATE_INTERFACE = 207;
    private static final int RESPONSE_TYPE_ON_DELETE_INTERFACE = 208;
    private static final int RESPONSE_TYPE_ON_END_DATA_PATH = 212;
    private static final int RESPONSE_TYPE_ON_INITIATE_DATA_PATH_FAIL = 210;
    private static final int RESPONSE_TYPE_ON_INITIATE_DATA_PATH_SUCCESS = 209;
    private static final int RESPONSE_TYPE_ON_MESSAGE_SEND_QUEUED_FAIL = 205;
    private static final int RESPONSE_TYPE_ON_MESSAGE_SEND_QUEUED_SUCCESS = 204;
    private static final int RESPONSE_TYPE_ON_RESPOND_TO_DATA_PATH_SETUP_REQUEST = 211;
    private static final int RESPONSE_TYPE_ON_SESSION_CONFIG_FAIL = 203;
    private static final int RESPONSE_TYPE_ON_SESSION_CONFIG_SUCCESS = 202;
    private static final String TAG = "WifiAwareStateManager";
    private static final boolean VDBG = false;
    private static final SparseArray<String> sSmToString = MessageUtils.findMessageNames(new Class[]{WifiAwareStateManager.class}, new String[]{"MESSAGE_TYPE", "COMMAND_TYPE", "RESPONSE_TYPE", "NOTIFICATION_TYPE"});
    private volatile Capabilities mCapabilities;
    private volatile Characteristics mCharacteristics = null;
    private final SparseArray<WifiAwareClientState> mClients = new SparseArray();
    private Context mContext;
    private ConfigRequest mCurrentAwareConfiguration = null;
    private byte[] mCurrentDiscoveryInterfaceMac = ALL_ZERO_MAC;
    private boolean mCurrentIdentityNotification = false;
    private WifiAwareDataPathStateManager mDataPathMgr;
    private WifiAwareRttStateManager mRtt;
    private WifiAwareStateMachine mSm;
    private volatile boolean mUsageEnabled = false;
    private WifiAwareNativeApi mWifiAwareNativeApi;

    class WifiAwareStateMachine extends StateMachine {
        private static final long AWARE_SEND_MESSAGE_TIMEOUT = 10000;
        private static final long AWARE_WAIT_FOR_DP_CONFIRM_TIMEOUT = 5000;
        private static final int TRANSACTION_ID_IGNORE = 0;
        private Message mCurrentCommand;
        private short mCurrentTransactionId = (short) 0;
        private final Map<WifiAwareNetworkSpecifier, WakeupMessage> mDataPathConfirmTimeoutMessages = new ArrayMap();
        private DefaultState mDefaultState = new DefaultState(this, null);
        private final Map<Short, Message> mFwQueuedSendMessages = new LinkedHashMap();
        private final SparseArray<Message> mHostQueuedSendMessages = new SparseArray();
        public int mNextSessionId = 1;
        private short mNextTransactionId = (short) 1;
        private int mSendArrivalSequenceCounter = 0;
        private WakeupMessage mSendMessageTimeoutMessage = new WakeupMessage(WifiAwareStateManager.this.mContext, getHandler(), WifiAwareStateManager.HAL_SEND_MESSAGE_TIMEOUT_TAG, 5);
        private boolean mSendQueueBlocked = false;
        private WaitForResponseState mWaitForResponseState = new WaitForResponseState(this, null);
        private WaitState mWaitState = new WaitState(this, null);

        private class DefaultState extends State {
            /* synthetic */ DefaultState(WifiAwareStateMachine this$1, DefaultState -this1) {
                this();
            }

            private DefaultState() {
            }

            public boolean processMessage(Message msg) {
                switch (msg.what) {
                    case 3:
                        WifiAwareStateMachine.this.processNotification(msg);
                        return true;
                    case 5:
                        WifiAwareStateMachine.this.processSendMessageTimeout();
                        return true;
                    case 6:
                        WifiAwareNetworkSpecifier networkSpecifier = msg.obj;
                        WifiAwareStateManager.this.mDataPathMgr.handleDataPathTimeout(networkSpecifier);
                        WifiAwareStateMachine.this.mDataPathConfirmTimeoutMessages.remove(networkSpecifier);
                        return true;
                    default:
                        Log.wtf(WifiAwareStateManager.TAG, "DefaultState: should not get non-NOTIFICATION in this state: msg=" + msg);
                        return false;
                }
            }
        }

        private class WaitForResponseState extends State {
            private static final long AWARE_COMMAND_TIMEOUT = 5000;
            private WakeupMessage mTimeoutMessage;

            /* synthetic */ WaitForResponseState(WifiAwareStateMachine this$1, WaitForResponseState -this1) {
                this();
            }

            private WaitForResponseState() {
            }

            public void enter() {
                this.mTimeoutMessage = new WakeupMessage(WifiAwareStateManager.this.mContext, WifiAwareStateMachine.this.getHandler(), WifiAwareStateManager.HAL_COMMAND_TIMEOUT_TAG, 4, WifiAwareStateMachine.this.mCurrentCommand.arg1, WifiAwareStateMachine.this.mCurrentTransactionId);
                this.mTimeoutMessage.schedule(SystemClock.elapsedRealtime() + AWARE_COMMAND_TIMEOUT);
            }

            public void exit() {
                this.mTimeoutMessage.cancel();
            }

            public boolean processMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        WifiAwareStateMachine.this.deferMessage(msg);
                        return true;
                    case 2:
                        if (msg.arg2 == WifiAwareStateMachine.this.mCurrentTransactionId) {
                            WifiAwareStateMachine.this.processResponse(msg);
                            WifiAwareStateMachine.this.transitionTo(WifiAwareStateMachine.this.mWaitState);
                        } else {
                            Log.w(WifiAwareStateManager.TAG, "WaitForResponseState: processMessage: non-matching transaction ID on RESPONSE (a very late response) -- msg=" + msg);
                        }
                        return true;
                    case 4:
                        if (msg.arg2 == WifiAwareStateMachine.this.mCurrentTransactionId) {
                            WifiAwareStateMachine.this.processTimeout(msg);
                            WifiAwareStateMachine.this.transitionTo(WifiAwareStateMachine.this.mWaitState);
                        } else {
                            Log.w(WifiAwareStateManager.TAG, "WaitForResponseState: processMessage: non-matching transaction ID on RESPONSE_TIMEOUT (either a non-cancelled timeout or a race condition with cancel) -- msg=" + msg);
                        }
                        return true;
                    default:
                        return false;
                }
            }
        }

        private class WaitState extends State {
            /* synthetic */ WaitState(WifiAwareStateMachine this$1, WaitState -this1) {
                this();
            }

            private WaitState() {
            }

            public boolean processMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        if (WifiAwareStateMachine.this.processCommand(msg)) {
                            WifiAwareStateMachine.this.transitionTo(WifiAwareStateMachine.this.mWaitForResponseState);
                        }
                        return true;
                    case 2:
                    case 4:
                        WifiAwareStateMachine.this.deferMessage(msg);
                        return true;
                    default:
                        return false;
                }
            }
        }

        WifiAwareStateMachine(String name, Looper looper) {
            super(name, looper);
            addState(this.mDefaultState);
            addState(this.mWaitState, this.mDefaultState);
            addState(this.mWaitForResponseState, this.mDefaultState);
            setInitialState(this.mWaitState);
        }

        public void onAwareDownCleanupSendQueueState() {
            this.mSendQueueBlocked = false;
            this.mHostQueuedSendMessages.clear();
            this.mFwQueuedSendMessages.clear();
        }

        private void processNotification(Message msg) {
            int reason;
            short transactionId;
            WifiAwareNetworkSpecifier networkSpecifier;
            WakeupMessage timeout;
            switch (msg.arg1) {
                case WifiAwareStateManager.NOTIFICATION_TYPE_INTERFACE_CHANGE /*301*/:
                    WifiAwareStateManager.this.onInterfaceAddressChangeLocal((byte[]) msg.obj);
                    break;
                case WifiAwareStateManager.NOTIFICATION_TYPE_CLUSTER_CHANGE /*302*/:
                    WifiAwareStateManager.this.onClusterChangeLocal(msg.arg2, (byte[]) msg.obj);
                    break;
                case WifiAwareStateManager.NOTIFICATION_TYPE_MATCH /*303*/:
                    WifiAwareStateManager.this.onMatchLocal(msg.arg2, msg.getData().getInt(WifiAwareStateManager.MESSAGE_BUNDLE_KEY_REQ_INSTANCE_ID), msg.getData().getByteArray(WifiAwareStateManager.MESSAGE_BUNDLE_KEY_MAC_ADDRESS), msg.getData().getByteArray(WifiAwareStateManager.MESSAGE_BUNDLE_KEY_SSI_DATA), msg.getData().getByteArray(WifiAwareStateManager.MESSAGE_BUNDLE_KEY_FILTER_DATA));
                    break;
                case WifiAwareStateManager.NOTIFICATION_TYPE_SESSION_TERMINATED /*304*/:
                    int pubSubId = msg.arg2;
                    reason = ((Integer) msg.obj).intValue();
                    WifiAwareStateManager.this.onSessionTerminatedLocal(pubSubId, msg.getData().getBoolean(WifiAwareStateManager.MESSAGE_BUNDLE_KEY_SESSION_TYPE), reason);
                    break;
                case WifiAwareStateManager.NOTIFICATION_TYPE_MESSAGE_RECEIVED /*305*/:
                    WifiAwareStateManager.this.onMessageReceivedLocal(msg.arg2, ((Integer) msg.obj).intValue(), msg.getData().getByteArray(WifiAwareStateManager.MESSAGE_BUNDLE_KEY_MAC_ADDRESS), msg.getData().getByteArray(WifiAwareStateManager.MESSAGE_BUNDLE_KEY_MESSAGE_DATA));
                    break;
                case WifiAwareStateManager.NOTIFICATION_TYPE_AWARE_DOWN /*306*/:
                    reason = msg.arg2;
                    WifiAwareStateManager.this.onAwareDownLocal();
                    break;
                case WifiAwareStateManager.NOTIFICATION_TYPE_ON_MESSAGE_SEND_SUCCESS /*307*/:
                    transactionId = (short) msg.arg2;
                    Message queuedSendCommand = (Message) this.mFwQueuedSendMessages.get(Short.valueOf(transactionId));
                    if (queuedSendCommand == null) {
                        Log.w(WifiAwareStateManager.TAG, "processNotification: NOTIFICATION_TYPE_ON_MESSAGE_SEND_SUCCESS: transactionId=" + transactionId + " - no such queued send command (timed-out?)");
                    } else {
                        this.mFwQueuedSendMessages.remove(Short.valueOf(transactionId));
                        updateSendMessageTimeout();
                        WifiAwareStateManager.this.onMessageSendSuccessLocal(queuedSendCommand);
                    }
                    this.mSendQueueBlocked = false;
                    WifiAwareStateManager.this.transmitNextMessage();
                    break;
                case WifiAwareStateManager.NOTIFICATION_TYPE_ON_MESSAGE_SEND_FAIL /*308*/:
                    transactionId = (short) msg.arg2;
                    reason = ((Integer) msg.obj).intValue();
                    Message sentMessage = (Message) this.mFwQueuedSendMessages.get(Short.valueOf(transactionId));
                    if (sentMessage != null) {
                        this.mFwQueuedSendMessages.remove(Short.valueOf(transactionId));
                        updateSendMessageTimeout();
                        int retryCount = sentMessage.getData().getInt(WifiAwareStateManager.MESSAGE_BUNDLE_KEY_RETRY_COUNT);
                        if (retryCount <= 0 || reason != 9) {
                            WifiAwareStateManager.this.onMessageSendFailLocal(sentMessage, reason);
                        } else {
                            sentMessage.getData().putInt(WifiAwareStateManager.MESSAGE_BUNDLE_KEY_RETRY_COUNT, retryCount - 1);
                            this.mHostQueuedSendMessages.put(sentMessage.getData().getInt(WifiAwareStateManager.MESSAGE_BUNDLE_KEY_MESSAGE_ARRIVAL_SEQ), sentMessage);
                        }
                        this.mSendQueueBlocked = false;
                        WifiAwareStateManager.this.transmitNextMessage();
                        break;
                    }
                    Log.w(WifiAwareStateManager.TAG, "processNotification: NOTIFICATION_TYPE_ON_MESSAGE_SEND_FAIL: transactionId=" + transactionId + " - no such queued send command (timed-out?)");
                    break;
                    break;
                case WifiAwareStateManager.NOTIFICATION_TYPE_ON_DATA_PATH_REQUEST /*309*/:
                    networkSpecifier = WifiAwareStateManager.this.mDataPathMgr.onDataPathRequest(msg.arg2, msg.getData().getByteArray(WifiAwareStateManager.MESSAGE_BUNDLE_KEY_MAC_ADDRESS), ((Integer) msg.obj).intValue());
                    if (networkSpecifier != null) {
                        timeout = new WakeupMessage(WifiAwareStateManager.this.mContext, getHandler(), WifiAwareStateManager.HAL_DATA_PATH_CONFIRM_TIMEOUT_TAG, 6, 0, 0, networkSpecifier);
                        this.mDataPathConfirmTimeoutMessages.put(networkSpecifier, timeout);
                        timeout.schedule(SystemClock.elapsedRealtime() + AWARE_WAIT_FOR_DP_CONFIRM_TIMEOUT);
                        break;
                    }
                    break;
                case WifiAwareStateManager.NOTIFICATION_TYPE_ON_DATA_PATH_CONFIRM /*310*/:
                    networkSpecifier = WifiAwareStateManager.this.mDataPathMgr.onDataPathConfirm(msg.arg2, msg.getData().getByteArray(WifiAwareStateManager.MESSAGE_BUNDLE_KEY_MAC_ADDRESS), msg.getData().getBoolean(WifiAwareStateManager.MESSAGE_BUNDLE_KEY_SUCCESS_FLAG), msg.getData().getInt(WifiAwareStateManager.MESSAGE_BUNDLE_KEY_STATUS_CODE), msg.getData().getByteArray(WifiAwareStateManager.MESSAGE_BUNDLE_KEY_MESSAGE_DATA));
                    if (networkSpecifier != null) {
                        timeout = (WakeupMessage) this.mDataPathConfirmTimeoutMessages.remove(networkSpecifier);
                        if (timeout != null) {
                            timeout.cancel();
                            break;
                        }
                    }
                    break;
                case WifiAwareStateManager.NOTIFICATION_TYPE_ON_DATA_PATH_END /*311*/:
                    WifiAwareStateManager.this.mDataPathMgr.onDataPathEnd(msg.arg2);
                    break;
                default:
                    Log.wtf(WifiAwareStateManager.TAG, "processNotification: this isn't a NOTIFICATION -- msg=" + msg);
                    return;
            }
        }

        private boolean processCommand(Message msg) {
            boolean waitForResponse;
            if (this.mCurrentCommand != null) {
                Log.wtf(WifiAwareStateManager.TAG, "processCommand: receiving a command (msg=" + msg + ") but current (previous) command isn't null (prev_msg=" + this.mCurrentCommand + ")");
                this.mCurrentCommand = null;
            }
            short s = this.mNextTransactionId;
            this.mNextTransactionId = (short) (s + 1);
            this.mCurrentTransactionId = (short) s;
            Bundle data;
            switch (msg.arg1) {
                case 100:
                    IWifiAwareEventCallback callback = msg.obj;
                    ConfigRequest configRequest = (ConfigRequest) msg.getData().getParcelable(WifiAwareStateManager.MESSAGE_BUNDLE_KEY_CONFIG);
                    waitForResponse = WifiAwareStateManager.this.connectLocal(this.mCurrentTransactionId, msg.arg2, msg.getData().getInt(WifiAwareStateManager.MESSAGE_BUNDLE_KEY_UID), msg.getData().getInt(WifiAwareStateManager.MESSAGE_BUNDLE_KEY_PID), msg.getData().getString(WifiAwareStateManager.MESSAGE_BUNDLE_KEY_CALLING_PACKAGE), callback, configRequest, msg.getData().getBoolean(WifiAwareStateManager.MESSAGE_BUNDLE_KEY_NOTIFY_IDENTITY_CHANGE));
                    break;
                case 101:
                    waitForResponse = WifiAwareStateManager.this.disconnectLocal(this.mCurrentTransactionId, msg.arg2);
                    break;
                case 102:
                    WifiAwareStateManager.this.terminateSessionLocal(msg.arg2, ((Integer) msg.obj).intValue());
                    waitForResponse = false;
                    break;
                case 103:
                    PublishConfig publishConfig = (PublishConfig) msg.getData().getParcelable(WifiAwareStateManager.MESSAGE_BUNDLE_KEY_CONFIG);
                    waitForResponse = WifiAwareStateManager.this.publishLocal(this.mCurrentTransactionId, msg.arg2, publishConfig, msg.obj);
                    break;
                case 104:
                    waitForResponse = WifiAwareStateManager.this.updatePublishLocal(this.mCurrentTransactionId, msg.arg2, msg.getData().getInt(WifiAwareStateManager.MESSAGE_BUNDLE_KEY_SESSION_ID), (PublishConfig) msg.obj);
                    break;
                case 105:
                    SubscribeConfig subscribeConfig = (SubscribeConfig) msg.getData().getParcelable(WifiAwareStateManager.MESSAGE_BUNDLE_KEY_CONFIG);
                    waitForResponse = WifiAwareStateManager.this.subscribeLocal(this.mCurrentTransactionId, msg.arg2, subscribeConfig, (IWifiAwareDiscoverySessionCallback) msg.obj);
                    break;
                case 106:
                    waitForResponse = WifiAwareStateManager.this.updateSubscribeLocal(this.mCurrentTransactionId, msg.arg2, msg.getData().getInt(WifiAwareStateManager.MESSAGE_BUNDLE_KEY_SESSION_ID), (SubscribeConfig) msg.obj);
                    break;
                case 107:
                    Message sendMsg = obtainMessage(msg.what);
                    sendMsg.copyFrom(msg);
                    sendMsg.getData().putInt(WifiAwareStateManager.MESSAGE_BUNDLE_KEY_MESSAGE_ARRIVAL_SEQ, this.mSendArrivalSequenceCounter);
                    this.mHostQueuedSendMessages.put(this.mSendArrivalSequenceCounter, sendMsg);
                    this.mSendArrivalSequenceCounter++;
                    waitForResponse = false;
                    if (!this.mSendQueueBlocked) {
                        WifiAwareStateManager.this.transmitNextMessage();
                        break;
                    }
                    break;
                case WifiAwareStateManager.COMMAND_TYPE_ENABLE_USAGE /*108*/:
                    WifiAwareStateManager.this.enableUsageLocal();
                    waitForResponse = false;
                    break;
                case WifiAwareStateManager.COMMAND_TYPE_DISABLE_USAGE /*109*/:
                    WifiAwareStateManager.this.disableUsageLocal();
                    waitForResponse = false;
                    break;
                case 110:
                    data = msg.getData();
                    RttParams[] params = (RttParams[]) msg.obj;
                    WifiAwareStateManager.this.startRangingLocal(msg.arg2, data.getInt(WifiAwareStateManager.MESSAGE_BUNDLE_KEY_SESSION_ID), params, data.getInt(WifiAwareStateManager.MESSAGE_BUNDLE_KEY_RANGING_ID));
                    waitForResponse = false;
                    break;
                case WifiAwareStateManager.COMMAND_TYPE_GET_CAPABILITIES /*111*/:
                    if (WifiAwareStateManager.this.mCapabilities != null) {
                        waitForResponse = false;
                        break;
                    }
                    waitForResponse = WifiAwareStateManager.this.mWifiAwareNativeApi.getCapabilities(this.mCurrentTransactionId);
                    break;
                case 112:
                    WifiAwareStateManager.this.mDataPathMgr.createAllInterfaces();
                    waitForResponse = false;
                    break;
                case 113:
                    WifiAwareStateManager.this.mDataPathMgr.deleteAllInterfaces();
                    waitForResponse = false;
                    break;
                case WifiAwareStateManager.COMMAND_TYPE_CREATE_DATA_PATH_INTERFACE /*114*/:
                    waitForResponse = WifiAwareStateManager.this.mWifiAwareNativeApi.createAwareNetworkInterface(this.mCurrentTransactionId, (String) msg.obj);
                    break;
                case WifiAwareStateManager.COMMAND_TYPE_DELETE_DATA_PATH_INTERFACE /*115*/:
                    waitForResponse = WifiAwareStateManager.this.mWifiAwareNativeApi.deleteAwareNetworkInterface(this.mCurrentTransactionId, (String) msg.obj);
                    break;
                case WifiAwareStateManager.COMMAND_TYPE_INITIATE_DATA_PATH_SETUP /*116*/:
                    data = msg.getData();
                    WifiAwareNetworkSpecifier networkSpecifier = msg.obj;
                    waitForResponse = WifiAwareStateManager.this.initiateDataPathSetupLocal(this.mCurrentTransactionId, networkSpecifier, data.getInt(WifiAwareStateManager.MESSAGE_BUNDLE_KEY_PEER_ID), data.getInt(WifiAwareStateManager.MESSAGE_BUNDLE_KEY_CHANNEL_REQ_TYPE), data.getInt(WifiAwareStateManager.MESSAGE_BUNDLE_KEY_CHANNEL), data.getByteArray(WifiAwareStateManager.MESSAGE_BUNDLE_KEY_MAC_ADDRESS), data.getString(WifiAwareStateManager.MESSAGE_BUNDLE_KEY_INTERFACE_NAME), data.getByteArray(WifiAwareStateManager.MESSAGE_BUNDLE_KEY_PMK), data.getString(WifiAwareStateManager.MESSAGE_BUNDLE_KEY_PASSPHRASE));
                    if (waitForResponse) {
                        WakeupMessage timeout = new WakeupMessage(WifiAwareStateManager.this.mContext, getHandler(), WifiAwareStateManager.HAL_DATA_PATH_CONFIRM_TIMEOUT_TAG, 6, 0, 0, networkSpecifier);
                        this.mDataPathConfirmTimeoutMessages.put(networkSpecifier, timeout);
                        timeout.schedule(SystemClock.elapsedRealtime() + AWARE_WAIT_FOR_DP_CONFIRM_TIMEOUT);
                        break;
                    }
                    break;
                case WifiAwareStateManager.COMMAND_TYPE_RESPOND_TO_DATA_PATH_SETUP_REQUEST /*117*/:
                    data = msg.getData();
                    int ndpId = msg.arg2;
                    waitForResponse = WifiAwareStateManager.this.respondToDataPathRequestLocal(this.mCurrentTransactionId, ((Boolean) msg.obj).booleanValue(), ndpId, data.getString(WifiAwareStateManager.MESSAGE_BUNDLE_KEY_INTERFACE_NAME), data.getByteArray(WifiAwareStateManager.MESSAGE_BUNDLE_KEY_PMK), data.getString(WifiAwareStateManager.MESSAGE_BUNDLE_KEY_PASSPHRASE));
                    break;
                case WifiAwareStateManager.COMMAND_TYPE_END_DATA_PATH /*118*/:
                    waitForResponse = WifiAwareStateManager.this.endDataPathLocal(this.mCurrentTransactionId, msg.arg2);
                    break;
                case WifiAwareStateManager.COMMAND_TYPE_TRANSMIT_NEXT_MESSAGE /*119*/:
                    if (!this.mSendQueueBlocked && this.mHostQueuedSendMessages.size() != 0) {
                        Message sendMessage = (Message) this.mHostQueuedSendMessages.valueAt(0);
                        this.mHostQueuedSendMessages.removeAt(0);
                        data = sendMessage.getData();
                        int clientId = sendMessage.arg2;
                        int sessionId = sendMessage.getData().getInt(WifiAwareStateManager.MESSAGE_BUNDLE_KEY_SESSION_ID);
                        int peerId = data.getInt(WifiAwareStateManager.MESSAGE_BUNDLE_KEY_MESSAGE_PEER_ID);
                        byte[] message = data.getByteArray(WifiAwareStateManager.MESSAGE_BUNDLE_KEY_MESSAGE);
                        int messageId = data.getInt(WifiAwareStateManager.MESSAGE_BUNDLE_KEY_MESSAGE_ID);
                        msg.getData().putParcelable(WifiAwareStateManager.MESSAGE_BUNDLE_KEY_SENT_MESSAGE, sendMessage);
                        waitForResponse = WifiAwareStateManager.this.sendFollowonMessageLocal(this.mCurrentTransactionId, clientId, sessionId, peerId, message, messageId);
                        break;
                    }
                    waitForResponse = false;
                    break;
                    break;
                default:
                    waitForResponse = false;
                    Log.wtf(WifiAwareStateManager.TAG, "processCommand: this isn't a COMMAND -- msg=" + msg);
                    break;
            }
            if (waitForResponse) {
                this.mCurrentCommand = obtainMessage(msg.what);
                this.mCurrentCommand.copyFrom(msg);
            } else {
                this.mCurrentTransactionId = (short) 0;
            }
            return waitForResponse;
        }

        private void processResponse(Message msg) {
            if (this.mCurrentCommand == null) {
                Log.wtf(WifiAwareStateManager.TAG, "processResponse: no existing command stored!? msg=" + msg);
                this.mCurrentTransactionId = (short) 0;
                return;
            }
            Message sentMessage;
            switch (msg.arg1) {
                case 200:
                    WifiAwareStateManager.this.onConfigCompletedLocal(this.mCurrentCommand);
                    break;
                case 201:
                    WifiAwareStateManager.this.onConfigFailedLocal(this.mCurrentCommand, ((Integer) msg.obj).intValue());
                    break;
                case 202:
                    WifiAwareStateManager.this.onSessionConfigSuccessLocal(this.mCurrentCommand, ((Integer) msg.obj).intValue(), msg.getData().getBoolean(WifiAwareStateManager.MESSAGE_BUNDLE_KEY_SESSION_TYPE));
                    break;
                case 203:
                    int reason = ((Integer) msg.obj).intValue();
                    WifiAwareStateManager.this.onSessionConfigFailLocal(this.mCurrentCommand, msg.getData().getBoolean(WifiAwareStateManager.MESSAGE_BUNDLE_KEY_SESSION_TYPE), reason);
                    break;
                case WifiAwareStateManager.RESPONSE_TYPE_ON_MESSAGE_SEND_QUEUED_SUCCESS /*204*/:
                    sentMessage = (Message) this.mCurrentCommand.getData().getParcelable(WifiAwareStateManager.MESSAGE_BUNDLE_KEY_SENT_MESSAGE);
                    sentMessage.getData().putLong(WifiAwareStateManager.MESSAGE_BUNDLE_KEY_SEND_MESSAGE_ENQUEUE_TIME, SystemClock.elapsedRealtime());
                    this.mFwQueuedSendMessages.put(Short.valueOf(this.mCurrentTransactionId), sentMessage);
                    updateSendMessageTimeout();
                    if (!this.mSendQueueBlocked) {
                        WifiAwareStateManager.this.transmitNextMessage();
                        break;
                    }
                    break;
                case WifiAwareStateManager.RESPONSE_TYPE_ON_MESSAGE_SEND_QUEUED_FAIL /*205*/:
                    if (((Integer) msg.obj).intValue() != 11) {
                        WifiAwareStateManager.this.onMessageSendFailLocal((Message) this.mCurrentCommand.getData().getParcelable(WifiAwareStateManager.MESSAGE_BUNDLE_KEY_SENT_MESSAGE), 1);
                        if (!this.mSendQueueBlocked) {
                            WifiAwareStateManager.this.transmitNextMessage();
                            break;
                        }
                    }
                    sentMessage = (Message) this.mCurrentCommand.getData().getParcelable(WifiAwareStateManager.MESSAGE_BUNDLE_KEY_SENT_MESSAGE);
                    this.mHostQueuedSendMessages.put(sentMessage.getData().getInt(WifiAwareStateManager.MESSAGE_BUNDLE_KEY_MESSAGE_ARRIVAL_SEQ), sentMessage);
                    this.mSendQueueBlocked = true;
                    break;
                    break;
                case WifiAwareStateManager.RESPONSE_TYPE_ON_CAPABILITIES_UPDATED /*206*/:
                    WifiAwareStateManager.this.onCapabilitiesUpdatedResponseLocal((Capabilities) msg.obj);
                    break;
                case WifiAwareStateManager.RESPONSE_TYPE_ON_CREATE_INTERFACE /*207*/:
                    WifiAwareStateManager.this.onCreateDataPathInterfaceResponseLocal(this.mCurrentCommand, msg.getData().getBoolean(WifiAwareStateManager.MESSAGE_BUNDLE_KEY_SUCCESS_FLAG), msg.getData().getInt(WifiAwareStateManager.MESSAGE_BUNDLE_KEY_STATUS_CODE));
                    break;
                case 208:
                    WifiAwareStateManager.this.onDeleteDataPathInterfaceResponseLocal(this.mCurrentCommand, msg.getData().getBoolean(WifiAwareStateManager.MESSAGE_BUNDLE_KEY_SUCCESS_FLAG), msg.getData().getInt(WifiAwareStateManager.MESSAGE_BUNDLE_KEY_STATUS_CODE));
                    break;
                case WifiAwareStateManager.RESPONSE_TYPE_ON_INITIATE_DATA_PATH_SUCCESS /*209*/:
                    WifiAwareStateManager.this.onInitiateDataPathResponseSuccessLocal(this.mCurrentCommand, ((Integer) msg.obj).intValue());
                    break;
                case 210:
                    WifiAwareStateManager.this.onInitiateDataPathResponseFailLocal(this.mCurrentCommand, ((Integer) msg.obj).intValue());
                    break;
                case 211:
                    WifiAwareStateManager.this.onRespondToDataPathSetupRequestResponseLocal(this.mCurrentCommand, msg.getData().getBoolean(WifiAwareStateManager.MESSAGE_BUNDLE_KEY_SUCCESS_FLAG), msg.getData().getInt(WifiAwareStateManager.MESSAGE_BUNDLE_KEY_STATUS_CODE));
                    break;
                case 212:
                    WifiAwareStateManager.this.onEndPathEndResponseLocal(this.mCurrentCommand, msg.getData().getBoolean(WifiAwareStateManager.MESSAGE_BUNDLE_KEY_SUCCESS_FLAG), msg.getData().getInt(WifiAwareStateManager.MESSAGE_BUNDLE_KEY_STATUS_CODE));
                    break;
                default:
                    Log.wtf(WifiAwareStateManager.TAG, "processResponse: this isn't a RESPONSE -- msg=" + msg);
                    this.mCurrentCommand = null;
                    this.mCurrentTransactionId = (short) 0;
                    return;
            }
            this.mCurrentCommand = null;
            this.mCurrentTransactionId = (short) 0;
        }

        private void processTimeout(Message msg) {
            if (this.mCurrentCommand == null) {
                Log.wtf(WifiAwareStateManager.TAG, "processTimeout: no existing command stored!? msg=" + msg);
                this.mCurrentTransactionId = (short) 0;
                return;
            }
            switch (msg.arg1) {
                case 100:
                    WifiAwareStateManager.this.onConfigFailedLocal(this.mCurrentCommand, 1);
                    break;
                case 101:
                    WifiAwareStateManager.this.onConfigFailedLocal(this.mCurrentCommand, 1);
                    break;
                case 102:
                    Log.wtf(WifiAwareStateManager.TAG, "processTimeout: TERMINATE_SESSION - shouldn't be waiting!");
                    break;
                case 103:
                    WifiAwareStateManager.this.onSessionConfigFailLocal(this.mCurrentCommand, true, 1);
                    break;
                case 104:
                    WifiAwareStateManager.this.onSessionConfigFailLocal(this.mCurrentCommand, true, 1);
                    break;
                case 105:
                    WifiAwareStateManager.this.onSessionConfigFailLocal(this.mCurrentCommand, false, 1);
                    break;
                case 106:
                    WifiAwareStateManager.this.onSessionConfigFailLocal(this.mCurrentCommand, false, 1);
                    break;
                case 107:
                    Log.wtf(WifiAwareStateManager.TAG, "processTimeout: ENQUEUE_SEND_MESSAGE - shouldn't be waiting!");
                    break;
                case WifiAwareStateManager.COMMAND_TYPE_ENABLE_USAGE /*108*/:
                    Log.wtf(WifiAwareStateManager.TAG, "processTimeout: ENABLE_USAGE - shouldn't be waiting!");
                    break;
                case WifiAwareStateManager.COMMAND_TYPE_DISABLE_USAGE /*109*/:
                    Log.wtf(WifiAwareStateManager.TAG, "processTimeout: DISABLE_USAGE - shouldn't be waiting!");
                    break;
                case 110:
                    Log.wtf(WifiAwareStateManager.TAG, "processTimeout: START_RANGING - shouldn't be waiting!");
                    break;
                case WifiAwareStateManager.COMMAND_TYPE_GET_CAPABILITIES /*111*/:
                    Log.e(WifiAwareStateManager.TAG, "processTimeout: GET_CAPABILITIES timed-out - strange, will try again when next enabled!?");
                    break;
                case 112:
                    Log.wtf(WifiAwareStateManager.TAG, "processTimeout: CREATE_ALL_DATA_PATH_INTERFACES - shouldn't be waiting!");
                    break;
                case 113:
                    Log.wtf(WifiAwareStateManager.TAG, "processTimeout: DELETE_ALL_DATA_PATH_INTERFACES - shouldn't be waiting!");
                    break;
                case WifiAwareStateManager.COMMAND_TYPE_CREATE_DATA_PATH_INTERFACE /*114*/:
                    WifiAwareStateManager.this.onCreateDataPathInterfaceResponseLocal(this.mCurrentCommand, false, 0);
                    break;
                case WifiAwareStateManager.COMMAND_TYPE_DELETE_DATA_PATH_INTERFACE /*115*/:
                    WifiAwareStateManager.this.onDeleteDataPathInterfaceResponseLocal(this.mCurrentCommand, false, 0);
                    break;
                case WifiAwareStateManager.COMMAND_TYPE_INITIATE_DATA_PATH_SETUP /*116*/:
                    WifiAwareStateManager.this.onInitiateDataPathResponseFailLocal(this.mCurrentCommand, 0);
                    break;
                case WifiAwareStateManager.COMMAND_TYPE_RESPOND_TO_DATA_PATH_SETUP_REQUEST /*117*/:
                    WifiAwareStateManager.this.onRespondToDataPathSetupRequestResponseLocal(this.mCurrentCommand, false, 0);
                    break;
                case WifiAwareStateManager.COMMAND_TYPE_END_DATA_PATH /*118*/:
                    WifiAwareStateManager.this.onEndPathEndResponseLocal(this.mCurrentCommand, false, 0);
                    break;
                case WifiAwareStateManager.COMMAND_TYPE_TRANSMIT_NEXT_MESSAGE /*119*/:
                    WifiAwareStateManager.this.onMessageSendFailLocal((Message) this.mCurrentCommand.getData().getParcelable(WifiAwareStateManager.MESSAGE_BUNDLE_KEY_SENT_MESSAGE), 1);
                    this.mSendQueueBlocked = false;
                    WifiAwareStateManager.this.transmitNextMessage();
                    break;
                default:
                    Log.wtf(WifiAwareStateManager.TAG, "processTimeout: this isn't a COMMAND -- msg=" + msg);
                    break;
            }
            this.mCurrentCommand = null;
            this.mCurrentTransactionId = (short) 0;
        }

        private void updateSendMessageTimeout() {
            Iterator<Message> it = this.mFwQueuedSendMessages.values().iterator();
            if (it.hasNext()) {
                this.mSendMessageTimeoutMessage.schedule(((Message) it.next()).getData().getLong(WifiAwareStateManager.MESSAGE_BUNDLE_KEY_SEND_MESSAGE_ENQUEUE_TIME) + AWARE_SEND_MESSAGE_TIMEOUT);
                return;
            }
            this.mSendMessageTimeoutMessage.cancel();
        }

        private void processSendMessageTimeout() {
            boolean first = true;
            long currentTime = SystemClock.elapsedRealtime();
            Iterator<Entry<Short, Message>> it = this.mFwQueuedSendMessages.entrySet().iterator();
            while (it.hasNext()) {
                Entry<Short, Message> entry = (Entry) it.next();
                short transactionId = ((Short) entry.getKey()).shortValue();
                Message message = (Message) entry.getValue();
                long messageEnqueueTime = message.getData().getLong(WifiAwareStateManager.MESSAGE_BUNDLE_KEY_SEND_MESSAGE_ENQUEUE_TIME);
                if (!first && AWARE_SEND_MESSAGE_TIMEOUT + messageEnqueueTime > currentTime) {
                    break;
                }
                WifiAwareStateManager.this.onMessageSendFailLocal(message, 1);
                it.remove();
                first = false;
            }
            updateSendMessageTimeout();
            this.mSendQueueBlocked = false;
            WifiAwareStateManager.this.transmitNextMessage();
        }

        protected String getLogRecString(Message msg) {
            StringBuilder sb = new StringBuilder(WifiAwareStateManager.messageToString(msg));
            if (msg.what == 1 && this.mCurrentTransactionId != (short) 0) {
                sb.append(" (Transaction ID=").append(this.mCurrentTransactionId).append(")");
            }
            return sb.toString();
        }

        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            pw.println("WifiAwareStateMachine:");
            pw.println("  mNextTransactionId: " + this.mNextTransactionId);
            pw.println("  mNextSessionId: " + this.mNextSessionId);
            pw.println("  mCurrentCommand: " + this.mCurrentCommand);
            pw.println("  mCurrentTransaction: " + this.mCurrentTransactionId);
            pw.println("  mSendQueueBlocked: " + this.mSendQueueBlocked);
            pw.println("  mSendArrivalSequenceCounter: " + this.mSendArrivalSequenceCounter);
            pw.println("  mHostQueuedSendMessages: [" + this.mHostQueuedSendMessages + "]");
            pw.println("  mFwQueuedSendMessages: [" + this.mFwQueuedSendMessages + "]");
            super.dump(fd, pw, args);
        }
    }

    public void setNative(WifiAwareNativeApi wifiAwareNativeApi) {
        this.mWifiAwareNativeApi = wifiAwareNativeApi;
    }

    public void start(Context context, Looper looper) {
        Log.i(TAG, "start()");
        this.mContext = context;
        this.mSm = new WifiAwareStateMachine(TAG, looper);
        this.mSm.setDbg(false);
        this.mSm.start();
        this.mRtt = new WifiAwareRttStateManager();
        this.mDataPathMgr = new WifiAwareDataPathStateManager(this);
        this.mDataPathMgr.start(this.mContext, this.mSm.getHandler().getLooper());
    }

    public void startLate() {
        this.mRtt.start(this.mContext, this.mSm.getHandler().getLooper());
    }

    WifiAwareClientState getClient(int clientId) {
        return (WifiAwareClientState) this.mClients.get(clientId);
    }

    public Capabilities getCapabilities() {
        return this.mCapabilities;
    }

    public Characteristics getCharacteristics() {
        if (this.mCharacteristics == null && this.mCapabilities != null) {
            this.mCharacteristics = this.mCapabilities.toPublicCharacteristics();
        }
        return this.mCharacteristics;
    }

    public void connect(int clientId, int uid, int pid, String callingPackage, IWifiAwareEventCallback callback, ConfigRequest configRequest, boolean notifyOnIdentityChanged) {
        Message msg = this.mSm.obtainMessage(1);
        msg.arg1 = 100;
        msg.arg2 = clientId;
        msg.obj = callback;
        msg.getData().putParcelable(MESSAGE_BUNDLE_KEY_CONFIG, configRequest);
        msg.getData().putInt(MESSAGE_BUNDLE_KEY_UID, uid);
        msg.getData().putInt(MESSAGE_BUNDLE_KEY_PID, pid);
        msg.getData().putString(MESSAGE_BUNDLE_KEY_CALLING_PACKAGE, callingPackage);
        msg.getData().putBoolean(MESSAGE_BUNDLE_KEY_NOTIFY_IDENTITY_CHANGE, notifyOnIdentityChanged);
        this.mSm.sendMessage(msg);
    }

    public void disconnect(int clientId) {
        Message msg = this.mSm.obtainMessage(1);
        msg.arg1 = 101;
        msg.arg2 = clientId;
        this.mSm.sendMessage(msg);
    }

    public void terminateSession(int clientId, int sessionId) {
        Message msg = this.mSm.obtainMessage(1);
        msg.arg1 = 102;
        msg.arg2 = clientId;
        msg.obj = Integer.valueOf(sessionId);
        this.mSm.sendMessage(msg);
    }

    public void publish(int clientId, PublishConfig publishConfig, IWifiAwareDiscoverySessionCallback callback) {
        Message msg = this.mSm.obtainMessage(1);
        msg.arg1 = 103;
        msg.arg2 = clientId;
        msg.obj = callback;
        msg.getData().putParcelable(MESSAGE_BUNDLE_KEY_CONFIG, publishConfig);
        this.mSm.sendMessage(msg);
    }

    public void updatePublish(int clientId, int sessionId, PublishConfig publishConfig) {
        Message msg = this.mSm.obtainMessage(1);
        msg.arg1 = 104;
        msg.arg2 = clientId;
        msg.obj = publishConfig;
        msg.getData().putInt(MESSAGE_BUNDLE_KEY_SESSION_ID, sessionId);
        this.mSm.sendMessage(msg);
    }

    public void subscribe(int clientId, SubscribeConfig subscribeConfig, IWifiAwareDiscoverySessionCallback callback) {
        Message msg = this.mSm.obtainMessage(1);
        msg.arg1 = 105;
        msg.arg2 = clientId;
        msg.obj = callback;
        msg.getData().putParcelable(MESSAGE_BUNDLE_KEY_CONFIG, subscribeConfig);
        this.mSm.sendMessage(msg);
    }

    public void updateSubscribe(int clientId, int sessionId, SubscribeConfig subscribeConfig) {
        Message msg = this.mSm.obtainMessage(1);
        msg.arg1 = 106;
        msg.arg2 = clientId;
        msg.obj = subscribeConfig;
        msg.getData().putInt(MESSAGE_BUNDLE_KEY_SESSION_ID, sessionId);
        this.mSm.sendMessage(msg);
    }

    public void sendMessage(int clientId, int sessionId, int peerId, byte[] message, int messageId, int retryCount) {
        Message msg = this.mSm.obtainMessage(1);
        msg.arg1 = 107;
        msg.arg2 = clientId;
        msg.getData().putInt(MESSAGE_BUNDLE_KEY_SESSION_ID, sessionId);
        msg.getData().putInt(MESSAGE_BUNDLE_KEY_MESSAGE_PEER_ID, peerId);
        msg.getData().putByteArray(MESSAGE_BUNDLE_KEY_MESSAGE, message);
        msg.getData().putInt(MESSAGE_BUNDLE_KEY_MESSAGE_ID, messageId);
        msg.getData().putInt(MESSAGE_BUNDLE_KEY_RETRY_COUNT, retryCount);
        this.mSm.sendMessage(msg);
    }

    public void startRanging(int clientId, int sessionId, RttParams[] params, int rangingId) {
        Message msg = this.mSm.obtainMessage(1);
        msg.arg1 = 110;
        msg.arg2 = clientId;
        msg.obj = params;
        msg.getData().putInt(MESSAGE_BUNDLE_KEY_SESSION_ID, sessionId);
        msg.getData().putInt(MESSAGE_BUNDLE_KEY_RANGING_ID, rangingId);
        this.mSm.sendMessage(msg);
    }

    public void enableUsage() {
        Message msg = this.mSm.obtainMessage(1);
        msg.arg1 = COMMAND_TYPE_ENABLE_USAGE;
        this.mSm.sendMessage(msg);
    }

    public void disableUsage() {
        Message msg = this.mSm.obtainMessage(1);
        msg.arg1 = COMMAND_TYPE_DISABLE_USAGE;
        this.mSm.sendMessage(msg);
    }

    public boolean isUsageEnabled() {
        return this.mUsageEnabled;
    }

    public void queryCapabilities() {
        Message msg = this.mSm.obtainMessage(1);
        msg.arg1 = COMMAND_TYPE_GET_CAPABILITIES;
        this.mSm.sendMessage(msg);
    }

    public void createAllDataPathInterfaces() {
        Message msg = this.mSm.obtainMessage(1);
        msg.arg1 = 112;
        this.mSm.sendMessage(msg);
    }

    public void deleteAllDataPathInterfaces() {
        Message msg = this.mSm.obtainMessage(1);
        msg.arg1 = 113;
        this.mSm.sendMessage(msg);
    }

    public void createDataPathInterface(String interfaceName) {
        Message msg = this.mSm.obtainMessage(1);
        msg.arg1 = COMMAND_TYPE_CREATE_DATA_PATH_INTERFACE;
        msg.obj = interfaceName;
        this.mSm.sendMessage(msg);
    }

    public void deleteDataPathInterface(String interfaceName) {
        Message msg = this.mSm.obtainMessage(1);
        msg.arg1 = COMMAND_TYPE_DELETE_DATA_PATH_INTERFACE;
        msg.obj = interfaceName;
        this.mSm.sendMessage(msg);
    }

    public void initiateDataPathSetup(WifiAwareNetworkSpecifier networkSpecifier, int peerId, int channelRequestType, int channel, byte[] peer, String interfaceName, byte[] pmk, String passphrase) {
        Message msg = this.mSm.obtainMessage(1);
        msg.arg1 = COMMAND_TYPE_INITIATE_DATA_PATH_SETUP;
        msg.obj = networkSpecifier;
        msg.getData().putInt(MESSAGE_BUNDLE_KEY_PEER_ID, peerId);
        msg.getData().putInt(MESSAGE_BUNDLE_KEY_CHANNEL_REQ_TYPE, channelRequestType);
        msg.getData().putInt(MESSAGE_BUNDLE_KEY_CHANNEL, channel);
        msg.getData().putByteArray(MESSAGE_BUNDLE_KEY_MAC_ADDRESS, peer);
        msg.getData().putString(MESSAGE_BUNDLE_KEY_INTERFACE_NAME, interfaceName);
        msg.getData().putByteArray(MESSAGE_BUNDLE_KEY_PMK, pmk);
        msg.getData().putString(MESSAGE_BUNDLE_KEY_PASSPHRASE, passphrase);
        this.mSm.sendMessage(msg);
    }

    public void respondToDataPathRequest(boolean accept, int ndpId, String interfaceName, byte[] pmk, String passphrase) {
        Message msg = this.mSm.obtainMessage(1);
        msg.arg1 = COMMAND_TYPE_RESPOND_TO_DATA_PATH_SETUP_REQUEST;
        msg.arg2 = ndpId;
        msg.obj = Boolean.valueOf(accept);
        msg.getData().putString(MESSAGE_BUNDLE_KEY_INTERFACE_NAME, interfaceName);
        msg.getData().putByteArray(MESSAGE_BUNDLE_KEY_PMK, pmk);
        msg.getData().putString(MESSAGE_BUNDLE_KEY_PASSPHRASE, passphrase);
        this.mSm.sendMessage(msg);
    }

    public void endDataPath(int ndpId) {
        Message msg = this.mSm.obtainMessage(1);
        msg.arg1 = COMMAND_TYPE_END_DATA_PATH;
        msg.arg2 = ndpId;
        this.mSm.sendMessage(msg);
    }

    private void transmitNextMessage() {
        Message msg = this.mSm.obtainMessage(1);
        msg.arg1 = COMMAND_TYPE_TRANSMIT_NEXT_MESSAGE;
        this.mSm.sendMessage(msg);
    }

    public void onConfigSuccessResponse(short transactionId) {
        Message msg = this.mSm.obtainMessage(2);
        msg.arg1 = 200;
        msg.arg2 = transactionId;
        this.mSm.sendMessage(msg);
    }

    public void onConfigFailedResponse(short transactionId, int reason) {
        Message msg = this.mSm.obtainMessage(2);
        msg.arg1 = 201;
        msg.arg2 = transactionId;
        msg.obj = Integer.valueOf(reason);
        this.mSm.sendMessage(msg);
    }

    public void onSessionConfigSuccessResponse(short transactionId, boolean isPublish, int pubSubId) {
        Message msg = this.mSm.obtainMessage(2);
        msg.arg1 = 202;
        msg.arg2 = transactionId;
        msg.obj = Integer.valueOf(pubSubId);
        msg.getData().putBoolean(MESSAGE_BUNDLE_KEY_SESSION_TYPE, isPublish);
        this.mSm.sendMessage(msg);
    }

    public void onSessionConfigFailResponse(short transactionId, boolean isPublish, int reason) {
        Message msg = this.mSm.obtainMessage(2);
        msg.arg1 = 203;
        msg.arg2 = transactionId;
        msg.obj = Integer.valueOf(reason);
        msg.getData().putBoolean(MESSAGE_BUNDLE_KEY_SESSION_TYPE, isPublish);
        this.mSm.sendMessage(msg);
    }

    public void onMessageSendQueuedSuccessResponse(short transactionId) {
        Message msg = this.mSm.obtainMessage(2);
        msg.arg1 = RESPONSE_TYPE_ON_MESSAGE_SEND_QUEUED_SUCCESS;
        msg.arg2 = transactionId;
        this.mSm.sendMessage(msg);
    }

    public void onMessageSendQueuedFailResponse(short transactionId, int reason) {
        Message msg = this.mSm.obtainMessage(2);
        msg.arg1 = RESPONSE_TYPE_ON_MESSAGE_SEND_QUEUED_FAIL;
        msg.arg2 = transactionId;
        msg.obj = Integer.valueOf(reason);
        this.mSm.sendMessage(msg);
    }

    public void onCapabilitiesUpdateResponse(short transactionId, Capabilities capabilities) {
        Message msg = this.mSm.obtainMessage(2);
        msg.arg1 = RESPONSE_TYPE_ON_CAPABILITIES_UPDATED;
        msg.arg2 = transactionId;
        msg.obj = capabilities;
        this.mSm.sendMessage(msg);
    }

    public void onCreateDataPathInterfaceResponse(short transactionId, boolean success, int reasonOnFailure) {
        Message msg = this.mSm.obtainMessage(2);
        msg.arg1 = RESPONSE_TYPE_ON_CREATE_INTERFACE;
        msg.arg2 = transactionId;
        msg.getData().putBoolean(MESSAGE_BUNDLE_KEY_SUCCESS_FLAG, success);
        msg.getData().putInt(MESSAGE_BUNDLE_KEY_STATUS_CODE, reasonOnFailure);
        this.mSm.sendMessage(msg);
    }

    public void onDeleteDataPathInterfaceResponse(short transactionId, boolean success, int reasonOnFailure) {
        Message msg = this.mSm.obtainMessage(2);
        msg.arg1 = 208;
        msg.arg2 = transactionId;
        msg.getData().putBoolean(MESSAGE_BUNDLE_KEY_SUCCESS_FLAG, success);
        msg.getData().putInt(MESSAGE_BUNDLE_KEY_STATUS_CODE, reasonOnFailure);
        this.mSm.sendMessage(msg);
    }

    public void onInitiateDataPathResponseSuccess(short transactionId, int ndpId) {
        Message msg = this.mSm.obtainMessage(2);
        msg.arg1 = RESPONSE_TYPE_ON_INITIATE_DATA_PATH_SUCCESS;
        msg.arg2 = transactionId;
        msg.obj = Integer.valueOf(ndpId);
        this.mSm.sendMessage(msg);
    }

    public void onInitiateDataPathResponseFail(short transactionId, int reason) {
        Message msg = this.mSm.obtainMessage(2);
        msg.arg1 = 210;
        msg.arg2 = transactionId;
        msg.obj = Integer.valueOf(reason);
        this.mSm.sendMessage(msg);
    }

    public void onRespondToDataPathSetupRequestResponse(short transactionId, boolean success, int reasonOnFailure) {
        Message msg = this.mSm.obtainMessage(2);
        msg.arg1 = 211;
        msg.arg2 = transactionId;
        msg.getData().putBoolean(MESSAGE_BUNDLE_KEY_SUCCESS_FLAG, success);
        msg.getData().putInt(MESSAGE_BUNDLE_KEY_STATUS_CODE, reasonOnFailure);
        this.mSm.sendMessage(msg);
    }

    public void onEndDataPathResponse(short transactionId, boolean success, int reasonOnFailure) {
        Message msg = this.mSm.obtainMessage(2);
        msg.arg1 = 212;
        msg.arg2 = transactionId;
        msg.getData().putBoolean(MESSAGE_BUNDLE_KEY_SUCCESS_FLAG, success);
        msg.getData().putInt(MESSAGE_BUNDLE_KEY_STATUS_CODE, reasonOnFailure);
        this.mSm.sendMessage(msg);
    }

    public void onInterfaceAddressChangeNotification(byte[] mac) {
        Message msg = this.mSm.obtainMessage(3);
        msg.arg1 = NOTIFICATION_TYPE_INTERFACE_CHANGE;
        msg.obj = mac;
        this.mSm.sendMessage(msg);
    }

    public void onClusterChangeNotification(int flag, byte[] clusterId) {
        Message msg = this.mSm.obtainMessage(3);
        msg.arg1 = NOTIFICATION_TYPE_CLUSTER_CHANGE;
        msg.arg2 = flag;
        msg.obj = clusterId;
        this.mSm.sendMessage(msg);
    }

    public void onMatchNotification(int pubSubId, int requestorInstanceId, byte[] peerMac, byte[] serviceSpecificInfo, byte[] matchFilter) {
        Message msg = this.mSm.obtainMessage(3);
        msg.arg1 = NOTIFICATION_TYPE_MATCH;
        msg.arg2 = pubSubId;
        msg.getData().putInt(MESSAGE_BUNDLE_KEY_REQ_INSTANCE_ID, requestorInstanceId);
        msg.getData().putByteArray(MESSAGE_BUNDLE_KEY_MAC_ADDRESS, peerMac);
        msg.getData().putByteArray(MESSAGE_BUNDLE_KEY_SSI_DATA, serviceSpecificInfo);
        msg.getData().putByteArray(MESSAGE_BUNDLE_KEY_FILTER_DATA, matchFilter);
        this.mSm.sendMessage(msg);
    }

    public void onSessionTerminatedNotification(int pubSubId, int reason, boolean isPublish) {
        Message msg = this.mSm.obtainMessage(3);
        msg.arg1 = NOTIFICATION_TYPE_SESSION_TERMINATED;
        msg.arg2 = pubSubId;
        msg.obj = Integer.valueOf(reason);
        msg.getData().putBoolean(MESSAGE_BUNDLE_KEY_SESSION_TYPE, isPublish);
        this.mSm.sendMessage(msg);
    }

    public void onMessageReceivedNotification(int pubSubId, int requestorInstanceId, byte[] peerMac, byte[] message) {
        Message msg = this.mSm.obtainMessage(3);
        msg.arg1 = NOTIFICATION_TYPE_MESSAGE_RECEIVED;
        msg.arg2 = pubSubId;
        msg.obj = Integer.valueOf(requestorInstanceId);
        msg.getData().putByteArray(MESSAGE_BUNDLE_KEY_MAC_ADDRESS, peerMac);
        msg.getData().putByteArray(MESSAGE_BUNDLE_KEY_MESSAGE_DATA, message);
        this.mSm.sendMessage(msg);
    }

    public void onAwareDownNotification(int reason) {
        Message msg = this.mSm.obtainMessage(3);
        msg.arg1 = NOTIFICATION_TYPE_AWARE_DOWN;
        msg.arg2 = reason;
        this.mSm.sendMessage(msg);
    }

    public void onMessageSendSuccessNotification(short transactionId) {
        Message msg = this.mSm.obtainMessage(3);
        msg.arg1 = NOTIFICATION_TYPE_ON_MESSAGE_SEND_SUCCESS;
        msg.arg2 = transactionId;
        this.mSm.sendMessage(msg);
    }

    public void onMessageSendFailNotification(short transactionId, int reason) {
        Message msg = this.mSm.obtainMessage(3);
        msg.arg1 = NOTIFICATION_TYPE_ON_MESSAGE_SEND_FAIL;
        msg.arg2 = transactionId;
        msg.obj = Integer.valueOf(reason);
        this.mSm.sendMessage(msg);
    }

    public void onDataPathRequestNotification(int pubSubId, byte[] mac, int ndpId) {
        Message msg = this.mSm.obtainMessage(3);
        msg.arg1 = NOTIFICATION_TYPE_ON_DATA_PATH_REQUEST;
        msg.arg2 = pubSubId;
        msg.obj = Integer.valueOf(ndpId);
        msg.getData().putByteArray(MESSAGE_BUNDLE_KEY_MAC_ADDRESS, mac);
        this.mSm.sendMessage(msg);
    }

    public void onDataPathConfirmNotification(int ndpId, byte[] mac, boolean accept, int reason, byte[] message) {
        Message msg = this.mSm.obtainMessage(3);
        msg.arg1 = NOTIFICATION_TYPE_ON_DATA_PATH_CONFIRM;
        msg.arg2 = ndpId;
        msg.getData().putByteArray(MESSAGE_BUNDLE_KEY_MAC_ADDRESS, mac);
        msg.getData().putBoolean(MESSAGE_BUNDLE_KEY_SUCCESS_FLAG, accept);
        msg.getData().putInt(MESSAGE_BUNDLE_KEY_STATUS_CODE, reason);
        msg.getData().putByteArray(MESSAGE_BUNDLE_KEY_MESSAGE_DATA, message);
        this.mSm.sendMessage(msg);
    }

    public void onDataPathEndNotification(int ndpId) {
        Message msg = this.mSm.obtainMessage(3);
        msg.arg1 = NOTIFICATION_TYPE_ON_DATA_PATH_END;
        msg.arg2 = ndpId;
        this.mSm.sendMessage(msg);
    }

    private void sendAwareStateChangedBroadcast(boolean enabled) {
        Intent intent = new Intent("android.net.wifi.aware.action.WIFI_AWARE_STATE_CHANGED");
        intent.addFlags(1073741824);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    private boolean connectLocal(short transactionId, int clientId, int uid, int pid, String callingPackage, IWifiAwareEventCallback callback, ConfigRequest configRequest, boolean notifyIdentityChange) {
        if (this.mUsageEnabled) {
            if (this.mClients.get(clientId) != null) {
                Log.e(TAG, "connectLocal: entry already exists for clientId=" + clientId);
            }
            ConfigRequest merged = mergeConfigRequests(configRequest);
            if (merged == null) {
                Log.e(TAG, "connectLocal: requested configRequest=" + configRequest + ", incompatible with current configurations");
                try {
                    callback.onConnectFail(1);
                } catch (RemoteException e) {
                    Log.w(TAG, "connectLocal onConnectFail(): RemoteException (FYI): " + e);
                }
                return false;
            } else if (this.mCurrentAwareConfiguration != null && this.mCurrentAwareConfiguration.equals(merged) && this.mCurrentIdentityNotification == notifyIdentityChange) {
                try {
                    callback.onConnectSuccess(clientId);
                } catch (RemoteException e2) {
                    Log.w(TAG, "connectLocal onConnectSuccess(): RemoteException (FYI): " + e2);
                }
                WifiAwareClientState client = new WifiAwareClientState(this.mContext, clientId, uid, pid, callingPackage, callback, configRequest, notifyIdentityChange);
                client.onInterfaceAddressChange(this.mCurrentDiscoveryInterfaceMac);
                this.mClients.append(clientId, client);
                return false;
            } else {
                boolean success = this.mWifiAwareNativeApi.enableAndConfigure(transactionId, merged, !doesAnyClientNeedIdentityChangeNotifications() ? notifyIdentityChange : true, this.mCurrentAwareConfiguration == null);
                if (!success) {
                    try {
                        callback.onConnectFail(1);
                    } catch (RemoteException e22) {
                        Log.w(TAG, "connectLocal onConnectFail(): RemoteException (FYI):  " + e22);
                    }
                }
                return success;
            }
        }
        Log.w(TAG, "connect(): called with mUsageEnabled=false");
        return false;
    }

    private boolean disconnectLocal(short transactionId, int clientId) {
        WifiAwareClientState client = (WifiAwareClientState) this.mClients.get(clientId);
        if (client == null) {
            Log.e(TAG, "disconnectLocal: no entry for clientId=" + clientId);
            return false;
        }
        this.mClients.delete(clientId);
        client.destroy();
        if (this.mClients.size() == 0) {
            this.mCurrentAwareConfiguration = null;
            this.mWifiAwareNativeApi.disable((short) 0);
            return false;
        }
        ConfigRequest merged = mergeConfigRequests(null);
        if (merged == null) {
            Log.wtf(TAG, "disconnectLocal: got an incompatible merge on remaining configs!?");
            return false;
        }
        boolean notificationReqs = doesAnyClientNeedIdentityChangeNotifications();
        if (merged.equals(this.mCurrentAwareConfiguration) && this.mCurrentIdentityNotification == notificationReqs) {
            return false;
        }
        return this.mWifiAwareNativeApi.enableAndConfigure(transactionId, merged, notificationReqs, false);
    }

    private void terminateSessionLocal(int clientId, int sessionId) {
        WifiAwareClientState client = (WifiAwareClientState) this.mClients.get(clientId);
        if (client == null) {
            Log.e(TAG, "terminateSession: no client exists for clientId=" + clientId);
        } else {
            client.terminateSession(sessionId);
        }
    }

    private boolean publishLocal(short transactionId, int clientId, PublishConfig publishConfig, IWifiAwareDiscoverySessionCallback callback) {
        if (((WifiAwareClientState) this.mClients.get(clientId)) == null) {
            Log.e(TAG, "publishLocal: no client exists for clientId=" + clientId);
            return false;
        }
        boolean success = this.mWifiAwareNativeApi.publish(transactionId, 0, publishConfig);
        if (!success) {
            try {
                callback.onSessionConfigFail(1);
            } catch (RemoteException e) {
                Log.w(TAG, "publishLocal onSessionConfigFail(): RemoteException (FYI): " + e);
            }
        }
        return success;
    }

    private boolean updatePublishLocal(short transactionId, int clientId, int sessionId, PublishConfig publishConfig) {
        WifiAwareClientState client = (WifiAwareClientState) this.mClients.get(clientId);
        if (client == null) {
            Log.e(TAG, "updatePublishLocal: no client exists for clientId=" + clientId);
            return false;
        }
        WifiAwareDiscoverySessionState session = client.getSession(sessionId);
        if (session != null) {
            return session.updatePublish(transactionId, publishConfig);
        }
        Log.e(TAG, "updatePublishLocal: no session exists for clientId=" + clientId + ", sessionId=" + sessionId);
        return false;
    }

    private boolean subscribeLocal(short transactionId, int clientId, SubscribeConfig subscribeConfig, IWifiAwareDiscoverySessionCallback callback) {
        if (((WifiAwareClientState) this.mClients.get(clientId)) == null) {
            Log.e(TAG, "subscribeLocal: no client exists for clientId=" + clientId);
            return false;
        }
        boolean success = this.mWifiAwareNativeApi.subscribe(transactionId, 0, subscribeConfig);
        if (!success) {
            try {
                callback.onSessionConfigFail(1);
            } catch (RemoteException e) {
                Log.w(TAG, "subscribeLocal onSessionConfigFail(): RemoteException (FYI): " + e);
            }
        }
        return success;
    }

    private boolean updateSubscribeLocal(short transactionId, int clientId, int sessionId, SubscribeConfig subscribeConfig) {
        WifiAwareClientState client = (WifiAwareClientState) this.mClients.get(clientId);
        if (client == null) {
            Log.e(TAG, "updateSubscribeLocal: no client exists for clientId=" + clientId);
            return false;
        }
        WifiAwareDiscoverySessionState session = client.getSession(sessionId);
        if (session != null) {
            return session.updateSubscribe(transactionId, subscribeConfig);
        }
        Log.e(TAG, "updateSubscribeLocal: no session exists for clientId=" + clientId + ", sessionId=" + sessionId);
        return false;
    }

    private boolean sendFollowonMessageLocal(short transactionId, int clientId, int sessionId, int peerId, byte[] message, int messageId) {
        WifiAwareClientState client = (WifiAwareClientState) this.mClients.get(clientId);
        if (client == null) {
            Log.e(TAG, "sendFollowonMessageLocal: no client exists for clientId=" + clientId);
            return false;
        }
        WifiAwareDiscoverySessionState session = client.getSession(sessionId);
        if (session != null) {
            return session.sendMessage(transactionId, peerId, message, messageId);
        }
        Log.e(TAG, "sendFollowonMessageLocal: no session exists for clientId=" + clientId + ", sessionId=" + sessionId);
        return false;
    }

    private void enableUsageLocal() {
        if (!this.mUsageEnabled) {
            this.mUsageEnabled = true;
            queryCapabilities();
            createAllDataPathInterfaces();
            sendAwareStateChangedBroadcast(true);
        }
    }

    private void disableUsageLocal() {
        if (this.mUsageEnabled) {
            onAwareDownLocal();
            deleteAllDataPathInterfaces();
            this.mUsageEnabled = false;
            this.mWifiAwareNativeApi.disable((short) 0);
            sendAwareStateChangedBroadcast(false);
        }
    }

    private void startRangingLocal(int clientId, int sessionId, RttParams[] params, int rangingId) {
        WifiAwareClientState client = (WifiAwareClientState) this.mClients.get(clientId);
        if (client == null) {
            Log.e(TAG, "startRangingLocal: no client exists for clientId=" + clientId);
            return;
        }
        WifiAwareDiscoverySessionState session = client.getSession(sessionId);
        if (session == null) {
            Log.e(TAG, "startRangingLocal: no session exists for clientId=" + clientId + ", sessionId=" + sessionId);
            client.onRangingFailure(rangingId, -4, "Invalid session ID");
            return;
        }
        for (RttParams param : params) {
            String peerIdStr = param.bssid;
            try {
                param.bssid = session.getMac(Integer.parseInt(peerIdStr), ":");
                if (param.bssid == null) {
                    Log.d(TAG, "startRangingLocal: no MAC address for peer ID=" + peerIdStr);
                    param.bssid = "";
                }
            } catch (NumberFormatException e) {
                Log.e(TAG, "startRangingLocal: invalid peer ID specification (in bssid field): '" + peerIdStr + "'");
                param.bssid = "";
            }
        }
        this.mRtt.startRanging(rangingId, client, params);
    }

    private boolean initiateDataPathSetupLocal(short transactionId, WifiAwareNetworkSpecifier networkSpecifier, int peerId, int channelRequestType, int channel, byte[] peer, String interfaceName, byte[] pmk, String passphrase) {
        boolean success = this.mWifiAwareNativeApi.initiateDataPath(transactionId, peerId, channelRequestType, channel, peer, interfaceName, pmk, passphrase, this.mCapabilities);
        if (!success) {
            this.mDataPathMgr.onDataPathInitiateFail(networkSpecifier, 1);
        }
        return success;
    }

    private boolean respondToDataPathRequestLocal(short transactionId, boolean accept, int ndpId, String interfaceName, byte[] pmk, String passphrase) {
        boolean success = this.mWifiAwareNativeApi.respondToDataPathRequest(transactionId, accept, ndpId, interfaceName, pmk, passphrase, this.mCapabilities);
        if (!success) {
            this.mDataPathMgr.onRespondToDataPathRequest(ndpId, false);
        }
        return success;
    }

    private boolean endDataPathLocal(short transactionId, int ndpId) {
        return this.mWifiAwareNativeApi.endDataPath(transactionId, ndpId);
    }

    private void onConfigCompletedLocal(Message completedCommand) {
        if (completedCommand.arg1 == 100) {
            Bundle data = completedCommand.getData();
            int clientId = completedCommand.arg2;
            IWifiAwareEventCallback callback = completedCommand.obj;
            ConfigRequest configRequest = (ConfigRequest) data.getParcelable(MESSAGE_BUNDLE_KEY_CONFIG);
            int uid = data.getInt(MESSAGE_BUNDLE_KEY_UID);
            int pid = data.getInt(MESSAGE_BUNDLE_KEY_PID);
            boolean notifyIdentityChange = data.getBoolean(MESSAGE_BUNDLE_KEY_NOTIFY_IDENTITY_CHANGE);
            WifiAwareClientState client = new WifiAwareClientState(this.mContext, clientId, uid, pid, data.getString(MESSAGE_BUNDLE_KEY_CALLING_PACKAGE), callback, configRequest, notifyIdentityChange);
            this.mClients.put(clientId, client);
            try {
                callback.onConnectSuccess(clientId);
            } catch (RemoteException e) {
                Log.w(TAG, "onConfigCompletedLocal onConnectSuccess(): RemoteException (FYI): " + e);
            }
            client.onInterfaceAddressChange(this.mCurrentDiscoveryInterfaceMac);
        } else if (completedCommand.arg1 != 101) {
            Log.wtf(TAG, "onConfigCompletedLocal: unexpected completedCommand=" + completedCommand);
            return;
        }
        this.mCurrentAwareConfiguration = mergeConfigRequests(null);
        if (this.mCurrentAwareConfiguration == null) {
            Log.wtf(TAG, "onConfigCompletedLocal: got a null merged configuration after config!?");
        }
        this.mCurrentIdentityNotification = doesAnyClientNeedIdentityChangeNotifications();
    }

    private void onConfigFailedLocal(Message failedCommand, int reason) {
        if (failedCommand.arg1 == 100) {
            try {
                failedCommand.obj.onConnectFail(reason);
            } catch (RemoteException e) {
                Log.w(TAG, "onConfigFailedLocal onConnectFail(): RemoteException (FYI): " + e);
            }
        } else if (failedCommand.arg1 != 101) {
            Log.wtf(TAG, "onConfigFailedLocal: unexpected failedCommand=" + failedCommand);
        }
    }

    private void onSessionConfigSuccessLocal(Message completedCommand, int pubSubId, boolean isPublish) {
        int clientId;
        WifiAwareClientState client;
        int sessionId;
        if (completedCommand.arg1 == 103 || completedCommand.arg1 == 105) {
            clientId = completedCommand.arg2;
            IWifiAwareDiscoverySessionCallback callback = completedCommand.obj;
            client = (WifiAwareClientState) this.mClients.get(clientId);
            if (client == null) {
                Log.e(TAG, "onSessionConfigSuccessLocal: no client exists for clientId=" + clientId);
                return;
            }
            WifiAwareStateMachine wifiAwareStateMachine = this.mSm;
            sessionId = wifiAwareStateMachine.mNextSessionId;
            wifiAwareStateMachine.mNextSessionId = sessionId + 1;
            try {
                callback.onSessionStarted(sessionId);
                client.addSession(new WifiAwareDiscoverySessionState(this.mWifiAwareNativeApi, sessionId, pubSubId, callback, isPublish));
            } catch (RemoteException e) {
                Log.e(TAG, "onSessionConfigSuccessLocal: onSessionStarted() RemoteException=" + e);
            }
        } else if (completedCommand.arg1 == 104 || completedCommand.arg1 == 106) {
            clientId = completedCommand.arg2;
            sessionId = completedCommand.getData().getInt(MESSAGE_BUNDLE_KEY_SESSION_ID);
            client = (WifiAwareClientState) this.mClients.get(clientId);
            if (client == null) {
                Log.e(TAG, "onSessionConfigSuccessLocal: no client exists for clientId=" + clientId);
                return;
            }
            WifiAwareDiscoverySessionState session = client.getSession(sessionId);
            if (session == null) {
                Log.e(TAG, "onSessionConfigSuccessLocal: no session exists for clientId=" + clientId + ", sessionId=" + sessionId);
                return;
            }
            try {
                session.getCallback().onSessionConfigSuccess();
            } catch (RemoteException e2) {
                Log.e(TAG, "onSessionConfigSuccessLocal: onSessionConfigSuccess() RemoteException=" + e2);
            }
        } else {
            Log.wtf(TAG, "onSessionConfigSuccessLocal: unexpected completedCommand=" + completedCommand);
        }
    }

    private void onSessionConfigFailLocal(Message failedCommand, boolean isPublish, int reason) {
        if (failedCommand.arg1 == 103 || failedCommand.arg1 == 105) {
            try {
                failedCommand.obj.onSessionConfigFail(reason);
            } catch (RemoteException e) {
                Log.w(TAG, "onSessionConfigFailLocal onSessionConfigFail(): RemoteException (FYI): " + e);
            }
        } else if (failedCommand.arg1 == 104 || failedCommand.arg1 == 106) {
            int clientId = failedCommand.arg2;
            int sessionId = failedCommand.getData().getInt(MESSAGE_BUNDLE_KEY_SESSION_ID);
            WifiAwareClientState client = (WifiAwareClientState) this.mClients.get(clientId);
            if (client == null) {
                Log.e(TAG, "onSessionConfigFailLocal: no client exists for clientId=" + clientId);
                return;
            }
            WifiAwareDiscoverySessionState session = client.getSession(sessionId);
            if (session == null) {
                Log.e(TAG, "onSessionConfigFailLocal: no session exists for clientId=" + clientId + ", sessionId=" + sessionId);
                return;
            }
            try {
                session.getCallback().onSessionConfigFail(reason);
            } catch (RemoteException e2) {
                Log.e(TAG, "onSessionConfigFailLocal: onSessionConfigFail() RemoteException=" + e2);
            }
        } else {
            Log.wtf(TAG, "onSessionConfigFailLocal: unexpected failedCommand=" + failedCommand);
        }
    }

    private void onMessageSendSuccessLocal(Message completedCommand) {
        int clientId = completedCommand.arg2;
        int sessionId = completedCommand.getData().getInt(MESSAGE_BUNDLE_KEY_SESSION_ID);
        int messageId = completedCommand.getData().getInt(MESSAGE_BUNDLE_KEY_MESSAGE_ID);
        WifiAwareClientState client = (WifiAwareClientState) this.mClients.get(clientId);
        if (client == null) {
            Log.e(TAG, "onMessageSendSuccessLocal: no client exists for clientId=" + clientId);
            return;
        }
        WifiAwareDiscoverySessionState session = client.getSession(sessionId);
        if (session == null) {
            Log.e(TAG, "onMessageSendSuccessLocal: no session exists for clientId=" + clientId + ", sessionId=" + sessionId);
            return;
        }
        try {
            session.getCallback().onMessageSendSuccess(messageId);
        } catch (RemoteException e) {
            Log.w(TAG, "onMessageSendSuccessLocal: RemoteException (FYI): " + e);
        }
    }

    private void onMessageSendFailLocal(Message failedCommand, int reason) {
        int clientId = failedCommand.arg2;
        int sessionId = failedCommand.getData().getInt(MESSAGE_BUNDLE_KEY_SESSION_ID);
        int messageId = failedCommand.getData().getInt(MESSAGE_BUNDLE_KEY_MESSAGE_ID);
        WifiAwareClientState client = (WifiAwareClientState) this.mClients.get(clientId);
        if (client == null) {
            Log.e(TAG, "onMessageSendFailLocal: no client exists for clientId=" + clientId);
            return;
        }
        WifiAwareDiscoverySessionState session = client.getSession(sessionId);
        if (session == null) {
            Log.e(TAG, "onMessageSendFailLocal: no session exists for clientId=" + clientId + ", sessionId=" + sessionId);
            return;
        }
        try {
            session.getCallback().onMessageSendFail(messageId, reason);
        } catch (RemoteException e) {
            Log.e(TAG, "onMessageSendFailLocal: onMessageSendFail RemoteException=" + e);
        }
    }

    private void onCapabilitiesUpdatedResponseLocal(Capabilities capabilities) {
        this.mCapabilities = capabilities;
        this.mCharacteristics = null;
    }

    private void onCreateDataPathInterfaceResponseLocal(Message command, boolean success, int reasonOnFailure) {
        if (success) {
            this.mDataPathMgr.onInterfaceCreated((String) command.obj);
        } else {
            Log.e(TAG, "onCreateDataPathInterfaceResponseLocal: failed when trying to create interface " + command.obj + ". Reason code=" + reasonOnFailure);
        }
    }

    private void onDeleteDataPathInterfaceResponseLocal(Message command, boolean success, int reasonOnFailure) {
        if (success) {
            this.mDataPathMgr.onInterfaceDeleted((String) command.obj);
        } else {
            Log.e(TAG, "onDeleteDataPathInterfaceResponseLocal: failed when trying to delete interface " + command.obj + ". Reason code=" + reasonOnFailure);
        }
    }

    private void onInitiateDataPathResponseSuccessLocal(Message command, int ndpId) {
        this.mDataPathMgr.onDataPathInitiateSuccess((WifiAwareNetworkSpecifier) command.obj, ndpId);
    }

    private void onInitiateDataPathResponseFailLocal(Message command, int reason) {
        this.mDataPathMgr.onDataPathInitiateFail((WifiAwareNetworkSpecifier) command.obj, reason);
    }

    private void onRespondToDataPathSetupRequestResponseLocal(Message command, boolean success, int reasonOnFailure) {
        this.mDataPathMgr.onRespondToDataPathRequest(command.arg2, success);
    }

    private void onEndPathEndResponseLocal(Message command, boolean success, int reasonOnFailure) {
    }

    private void onInterfaceAddressChangeLocal(byte[] mac) {
        this.mCurrentDiscoveryInterfaceMac = mac;
        for (int i = 0; i < this.mClients.size(); i++) {
            ((WifiAwareClientState) this.mClients.valueAt(i)).onInterfaceAddressChange(mac);
        }
    }

    private void onClusterChangeLocal(int flag, byte[] clusterId) {
        for (int i = 0; i < this.mClients.size(); i++) {
            ((WifiAwareClientState) this.mClients.valueAt(i)).onClusterChange(flag, clusterId, this.mCurrentDiscoveryInterfaceMac);
        }
    }

    private void onMatchLocal(int pubSubId, int requestorInstanceId, byte[] peerMac, byte[] serviceSpecificInfo, byte[] matchFilter) {
        Pair<WifiAwareClientState, WifiAwareDiscoverySessionState> data = getClientSessionForPubSubId(pubSubId);
        if (data == null) {
            Log.e(TAG, "onMatch: no session found for pubSubId=" + pubSubId);
        } else {
            ((WifiAwareDiscoverySessionState) data.second).onMatch(requestorInstanceId, peerMac, serviceSpecificInfo, matchFilter);
        }
    }

    private void onSessionTerminatedLocal(int pubSubId, boolean isPublish, int reason) {
        Pair<WifiAwareClientState, WifiAwareDiscoverySessionState> data = getClientSessionForPubSubId(pubSubId);
        if (data == null) {
            Log.e(TAG, "onSessionTerminatedLocal: no session found for pubSubId=" + pubSubId);
            return;
        }
        try {
            ((WifiAwareDiscoverySessionState) data.second).getCallback().onSessionTerminated(reason);
        } catch (RemoteException e) {
            Log.w(TAG, "onSessionTerminatedLocal onSessionTerminated(): RemoteException (FYI): " + e);
        }
        ((WifiAwareClientState) data.first).removeSession(((WifiAwareDiscoverySessionState) data.second).getSessionId());
    }

    private void onMessageReceivedLocal(int pubSubId, int requestorInstanceId, byte[] peerMac, byte[] message) {
        Pair<WifiAwareClientState, WifiAwareDiscoverySessionState> data = getClientSessionForPubSubId(pubSubId);
        if (data == null) {
            Log.e(TAG, "onMessageReceivedLocal: no session found for pubSubId=" + pubSubId);
        } else {
            ((WifiAwareDiscoverySessionState) data.second).onMessageReceived(requestorInstanceId, peerMac, message);
        }
    }

    private void onAwareDownLocal() {
        this.mClients.clear();
        this.mCurrentAwareConfiguration = null;
        this.mSm.onAwareDownCleanupSendQueueState();
        this.mDataPathMgr.onAwareDownCleanupDataPaths();
        this.mCurrentDiscoveryInterfaceMac = ALL_ZERO_MAC;
    }

    private Pair<WifiAwareClientState, WifiAwareDiscoverySessionState> getClientSessionForPubSubId(int pubSubId) {
        for (int i = 0; i < this.mClients.size(); i++) {
            WifiAwareClientState client = (WifiAwareClientState) this.mClients.valueAt(i);
            WifiAwareDiscoverySessionState session = client.getAwareSessionStateForPubSubId(pubSubId);
            if (session != null) {
                return new Pair(client, session);
            }
        }
        return null;
    }

    private ConfigRequest mergeConfigRequests(ConfigRequest configRequest) {
        if (this.mClients.size() == 0 && configRequest == null) {
            Log.e(TAG, "mergeConfigRequests: invalid state - called with 0 clients registered!");
            return null;
        }
        int band;
        boolean support5gBand = false;
        int masterPreference = 0;
        boolean clusterIdValid = false;
        int clusterLow = 0;
        int clusterHigh = Constants.SHORT_MASK;
        int[] discoveryWindowInterval = new int[]{-1, -1};
        if (configRequest != null) {
            support5gBand = configRequest.mSupport5gBand;
            masterPreference = configRequest.mMasterPreference;
            clusterIdValid = true;
            clusterLow = configRequest.mClusterLow;
            clusterHigh = configRequest.mClusterHigh;
            discoveryWindowInterval = configRequest.mDiscoveryWindowInterval;
        }
        for (int i = 0; i < this.mClients.size(); i++) {
            ConfigRequest cr = ((WifiAwareClientState) this.mClients.valueAt(i)).getConfigRequest();
            if (cr.mSupport5gBand) {
                support5gBand = true;
            }
            masterPreference = Math.max(masterPreference, cr.mMasterPreference);
            if (!clusterIdValid) {
                clusterIdValid = true;
                clusterLow = cr.mClusterLow;
                clusterHigh = cr.mClusterHigh;
            } else if (clusterLow != cr.mClusterLow) {
                return null;
            } else {
                if (clusterHigh != cr.mClusterHigh) {
                    return null;
                }
            }
            for (band = 0; band <= 1; band++) {
                if (discoveryWindowInterval[band] == -1) {
                    discoveryWindowInterval[band] = cr.mDiscoveryWindowInterval[band];
                } else if (cr.mDiscoveryWindowInterval[band] != -1) {
                    if (discoveryWindowInterval[band] == 0) {
                        discoveryWindowInterval[band] = cr.mDiscoveryWindowInterval[band];
                    } else if (cr.mDiscoveryWindowInterval[band] != 0) {
                        discoveryWindowInterval[band] = Math.min(discoveryWindowInterval[band], cr.mDiscoveryWindowInterval[band]);
                    }
                }
            }
        }
        Builder builder = new Builder().setSupport5gBand(support5gBand).setMasterPreference(masterPreference).setClusterLow(clusterLow).setClusterHigh(clusterHigh);
        for (band = 0; band <= 1; band++) {
            if (discoveryWindowInterval[band] != -1) {
                builder.setDiscoveryWindowInterval(band, discoveryWindowInterval[band]);
            }
        }
        return builder.build();
    }

    private boolean doesAnyClientNeedIdentityChangeNotifications() {
        for (int i = 0; i < this.mClients.size(); i++) {
            if (((WifiAwareClientState) this.mClients.valueAt(i)).getNotifyIdentityChange()) {
                return true;
            }
        }
        return false;
    }

    private static String messageToString(Message msg) {
        StringBuilder sb = new StringBuilder();
        String s = (String) sSmToString.get(msg.what);
        if (s == null) {
            s = "<unknown>";
        }
        sb.append(s).append("/");
        if (msg.what == 3 || msg.what == 1 || msg.what == 2) {
            s = (String) sSmToString.get(msg.arg1);
            if (s == null) {
                s = "<unknown>";
            }
            sb.append(s);
        }
        if (msg.what == 2 || msg.what == 4) {
            sb.append(" (Transaction ID=").append(msg.arg2).append(")");
        }
        return sb.toString();
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("AwareStateManager:");
        pw.println("  mClients: [" + this.mClients + "]");
        pw.println("  mUsageEnabled: " + this.mUsageEnabled);
        pw.println("  mCapabilities: [" + this.mCapabilities + "]");
        pw.println("  mCurrentAwareConfiguration: " + this.mCurrentAwareConfiguration);
        pw.println("  mCurrentIdentityNotification: " + this.mCurrentIdentityNotification);
        for (int i = 0; i < this.mClients.size(); i++) {
            ((WifiAwareClientState) this.mClients.valueAt(i)).dump(fd, pw, args);
        }
        this.mSm.dump(fd, pw, args);
        this.mRtt.dump(fd, pw, args);
        this.mDataPathMgr.dump(fd, pw, args);
        this.mWifiAwareNativeApi.dump(fd, pw, args);
    }
}
