package com.android.server.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.IRttManager.Stub;
import android.net.wifi.RttManager.ParcelableRttParams;
import android.net.wifi.RttManager.ParcelableRttResults;
import android.net.wifi.RttManager.ResponderConfig;
import android.net.wifi.RttManager.RttCapabilities;
import android.net.wifi.RttManager.RttParams;
import android.net.wifi.RttManager.RttResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.util.Slog;
import com.android.internal.util.AsyncChannel;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.SystemService;
import com.android.server.wifi.WifiNative.RttEventHandler;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public final class RttService extends SystemService {
    public static final boolean DBG = true;
    private static final String TAG = "RttService";
    private final HandlerThread mHandlerThread;
    RttServiceImpl mImpl;

    static class RttServiceImpl extends Stub {
        private static final int BASE = 160512;
        private static final int CMD_DRIVER_LOADED = 160512;
        private static final int CMD_DRIVER_UNLOADED = 160513;
        private static final int CMD_ISSUE_NEXT_REQUEST = 160514;
        private static final int CMD_RTT_RESPONSE = 160515;
        private static final int MAX_RESPONDER_DURATION_SECONDS = 600;
        private ClientHandler mClientHandler;
        private HashMap<Messenger, ClientInfo> mClients;
        private final Context mContext;
        private RttEventHandler mEventHandler;
        private final Looper mLooper;
        private Queue<RttRequest> mRequestQueue;
        private RttStateMachine mStateMachine;
        private final WifiNative mWifiNative;

        private class ClientHandler extends Handler {
            ClientHandler(Looper looper) {
                super(looper);
            }

            public void handleMessage(Message msg) {
                int i = 0;
                Log.d(RttService.TAG, "ClientHandler got" + msg + " what = " + getDescription(msg.what));
                switch (msg.what) {
                    case 69633:
                        AsyncChannel ac = new AsyncChannel();
                        ac.connected(RttServiceImpl.this.mContext, this, msg.replyTo);
                        RttServiceImpl.this.mClients.put(msg.replyTo, new ClientInfo(ac, msg.replyTo));
                        ac.replyToMessage(msg, 69634, 0);
                    case 69636:
                        if (msg.arg1 == 2) {
                            Slog.e(RttService.TAG, "Send failed, client connection lost");
                        } else {
                            Slog.d(RttService.TAG, "Client connection lost with reason: " + msg.arg1);
                        }
                        Slog.d(RttService.TAG, "closing client " + msg.replyTo);
                        ClientInfo ci = (ClientInfo) RttServiceImpl.this.mClients.remove(msg.replyTo);
                        if (ci != null) {
                            ci.cleanup();
                        }
                    default:
                        if (((ClientInfo) RttServiceImpl.this.mClients.get(msg.replyTo)) == null) {
                            Slog.e(RttService.TAG, "Could not find client info for message " + msg.replyTo);
                            RttServiceImpl.this.replyFailed(msg, -3, "Could not find listener");
                        } else if (RttServiceImpl.this.enforcePermissionCheck(msg)) {
                            int[] validCommands = new int[]{160256, 160257, 160261, 160262};
                            int length = validCommands.length;
                            while (i < length) {
                                if (validCommands[i] == msg.what) {
                                    RttServiceImpl.this.mStateMachine.sendMessage(Message.obtain(msg));
                                    return;
                                }
                                i++;
                            }
                            RttServiceImpl.this.replyFailed(msg, -4, "Invalid request");
                        } else {
                            RttServiceImpl.this.replyFailed(msg, -5, "Client doesn't have LOCATION_HARDWARE permission");
                        }
                }
            }

            private String getDescription(int what) {
                switch (what) {
                    case 160261:
                        return "CMD_OP_ENABLE_RESPONDER";
                    case 160262:
                        return "CMD_OP_DISABLE_RESPONDER";
                    default:
                        return "CMD_UNKNOWN";
                }
            }
        }

        private class ClientInfo {
            private final AsyncChannel mChannel;
            private final Messenger mMessenger;
            HashMap<Integer, RttRequest> mRequests;
            Set<Integer> mResponderRequests;

            ClientInfo(AsyncChannel c, Messenger m) {
                this.mRequests = new HashMap();
                this.mResponderRequests = new HashSet();
                this.mChannel = c;
                this.mMessenger = m;
            }

            void addResponderRequest(int key) {
                this.mResponderRequests.add(Integer.valueOf(key));
            }

            void removeResponderRequest(int key) {
                this.mResponderRequests.remove(Integer.valueOf(key));
            }

            boolean addRttRequest(int key, ParcelableRttParams parcelableParams) {
                if (parcelableParams == null) {
                    return false;
                }
                RttParams[] params = parcelableParams.mParams;
                RttRequest request = new RttRequest(null);
                request.key = Integer.valueOf(key);
                request.ci = this;
                request.params = params;
                this.mRequests.put(Integer.valueOf(key), request);
                RttServiceImpl.this.mRequestQueue.add(request);
                return RttService.DBG;
            }

            void removeRttRequest(int key) {
                this.mRequests.remove(Integer.valueOf(key));
            }

            void reportResponderEnableSucceed(int key, ResponderConfig config) {
                this.mChannel.sendMessage(160263, 0, key, config);
            }

            void reportResponderEnableFailed(int key, int reason) {
                this.mChannel.sendMessage(160264, reason, key);
                this.mResponderRequests.remove(Integer.valueOf(key));
            }

            void reportResult(RttRequest request, RttResult[] results) {
                this.mChannel.sendMessage(160259, 0, request.key.intValue(), new ParcelableRttResults(results));
                this.mRequests.remove(request.key);
            }

            void reportFailed(RttRequest request, int reason, String description) {
                reportFailed(request.key.intValue(), reason, description);
            }

            void reportFailed(int key, int reason, String description) {
                Bundle bundle = new Bundle();
                bundle.putString("android.net.wifi.RttManager.Description", description);
                this.mChannel.sendMessage(160258, key, reason, bundle);
                this.mRequests.remove(Integer.valueOf(key));
            }

            void reportAborted(int key) {
                this.mChannel.sendMessage(160260, 0, key);
                cleanup();
            }

            void cleanup() {
                this.mRequests.clear();
                RttServiceImpl.this.mRequestQueue.clear();
                this.mResponderRequests.clear();
                RttServiceImpl.this.mStateMachine.sendMessage(160262);
            }
        }

        private class RttRequest {
            ClientInfo ci;
            Integer key;
            RttParams[] params;

            private RttRequest() {
            }

            public String toString() {
                String str = getClass().getName() + "@" + Integer.toHexString(hashCode());
                if (this.key != null) {
                    return str + " key: " + this.key;
                }
                return str + " key: " + " , null";
            }
        }

        class RttStateMachine extends StateMachine {
            DefaultState mDefaultState;
            EnabledState mEnabledState;
            InitiatorEnabledState mInitiatorEnabledState;
            ResponderConfig mResponderConfig;
            ResponderEnabledState mResponderEnabledState;

            class DefaultState extends State {
                DefaultState() {
                }

                public boolean processMessage(Message msg) {
                    Log.d(RttService.TAG, "DefaultState got" + msg);
                    switch (msg.what) {
                        case 160256:
                            RttServiceImpl.this.replyFailed(msg, -2, "Try later");
                            break;
                        case 160257:
                            return RttService.DBG;
                        case 160261:
                            ClientInfo client = (ClientInfo) RttServiceImpl.this.mClients.get(msg.replyTo);
                            if (client != null) {
                                client.reportResponderEnableFailed(msg.arg2, -2);
                                break;
                            }
                            Log.e(RttService.TAG, "client not connected yet!");
                            break;
                        case 160262:
                            return RttService.DBG;
                        case RttServiceImpl.CMD_DRIVER_LOADED /*160512*/:
                            RttStateMachine.this.transitionTo(RttStateMachine.this.mEnabledState);
                            break;
                        case RttServiceImpl.CMD_ISSUE_NEXT_REQUEST /*160514*/:
                            RttStateMachine.this.deferMessage(msg);
                            break;
                        default:
                            return false;
                    }
                    return RttService.DBG;
                }
            }

            class EnabledState extends State {
                EnabledState() {
                }

                public boolean processMessage(Message msg) {
                    Log.d(RttService.TAG, "EnabledState got" + msg);
                    ClientInfo ci = (ClientInfo) RttServiceImpl.this.mClients.get(msg.replyTo);
                    switch (msg.what) {
                        case 160256:
                            ParcelableRttParams params = msg.obj;
                            if (params != null && params.mParams != null && params.mParams.length != 0) {
                                if (!ci.addRttRequest(msg.arg2, params)) {
                                    RttServiceImpl.this.replyFailed(msg, -4, "Unspecified");
                                    break;
                                }
                                RttStateMachine.this.sendMessage(RttServiceImpl.CMD_ISSUE_NEXT_REQUEST);
                                break;
                            }
                            RttServiceImpl.this.replyFailed(msg, -4, "No params");
                            break;
                            break;
                        case 160257:
                            for (RttRequest request : RttServiceImpl.this.mRequestQueue) {
                                if (request.key.intValue() == msg.arg2) {
                                    Log.d(RttService.TAG, "Cancelling not-yet-scheduled RTT");
                                    RttServiceImpl.this.mRequestQueue.remove(request);
                                    request.ci.reportAborted(request.key.intValue());
                                    break;
                                }
                            }
                            break;
                        case 160261:
                            int key = msg.arg2;
                            RttStateMachine.this.mResponderConfig = RttServiceImpl.this.mWifiNative.enableRttResponder(RttServiceImpl.MAX_RESPONDER_DURATION_SECONDS);
                            Log.d(RttService.TAG, "mWifiNative.enableRttResponder called");
                            if (RttStateMachine.this.mResponderConfig == null) {
                                Log.e(RttService.TAG, "enable responder failed");
                                ci.reportResponderEnableFailed(key, -1);
                                break;
                            }
                            RttStateMachine.this.mResponderConfig.macAddress = RttServiceImpl.this.mWifiNative.getMacAddress();
                            ci.addResponderRequest(key);
                            ci.reportResponderEnableSucceed(key, RttStateMachine.this.mResponderConfig);
                            RttStateMachine.this.transitionTo(RttStateMachine.this.mResponderEnabledState);
                            break;
                        case 160262:
                            break;
                        case RttServiceImpl.CMD_DRIVER_UNLOADED /*160513*/:
                            RttStateMachine.this.transitionTo(RttStateMachine.this.mDefaultState);
                            break;
                        case RttServiceImpl.CMD_ISSUE_NEXT_REQUEST /*160514*/:
                            RttStateMachine.this.deferMessage(msg);
                            RttStateMachine.this.transitionTo(RttStateMachine.this.mInitiatorEnabledState);
                            break;
                        default:
                            return false;
                    }
                    return RttService.DBG;
                }
            }

            class InitiatorEnabledState extends State {
                RttRequest mOutstandingRequest;

                InitiatorEnabledState() {
                }

                public boolean processMessage(Message msg) {
                    Log.d(RttService.TAG, "RequestPendingState got" + msg);
                    switch (msg.what) {
                        case 160257:
                            if (this.mOutstandingRequest != null && msg.arg2 == this.mOutstandingRequest.key.intValue()) {
                                Log.d(RttService.TAG, "Cancelling ongoing RTT of: " + msg.arg2);
                                RttServiceImpl.this.mWifiNative.cancelRtt(this.mOutstandingRequest.params);
                                this.mOutstandingRequest.ci.reportAborted(this.mOutstandingRequest.key.intValue());
                                this.mOutstandingRequest = null;
                                RttStateMachine.this.sendMessage(RttServiceImpl.CMD_ISSUE_NEXT_REQUEST);
                                break;
                            }
                            return false;
                        case RttServiceImpl.CMD_DRIVER_UNLOADED /*160513*/:
                            if (this.mOutstandingRequest != null) {
                                RttServiceImpl.this.mWifiNative.cancelRtt(this.mOutstandingRequest.params);
                                Log.d(RttService.TAG, "abort key: " + this.mOutstandingRequest.key);
                                this.mOutstandingRequest.ci.reportAborted(this.mOutstandingRequest.key.intValue());
                                this.mOutstandingRequest = null;
                            }
                            RttStateMachine.this.transitionTo(RttStateMachine.this.mDefaultState);
                            break;
                        case RttServiceImpl.CMD_ISSUE_NEXT_REQUEST /*160514*/:
                            if (this.mOutstandingRequest != null) {
                                Log.d(RttService.TAG, "Current mOutstandingRequest.key is: " + this.mOutstandingRequest.key);
                                Log.d(RttService.TAG, "Ignoring CMD_ISSUE_NEXT_REQUEST");
                                break;
                            }
                            this.mOutstandingRequest = RttServiceImpl.this.issueNextRequest();
                            if (this.mOutstandingRequest == null) {
                                RttStateMachine.this.transitionTo(RttStateMachine.this.mEnabledState);
                            }
                            if (this.mOutstandingRequest == null) {
                                Log.d(RttService.TAG, "CMD_ISSUE_NEXT_REQUEST: mOutstandingRequest =null ");
                                break;
                            }
                            Log.d(RttService.TAG, "new mOutstandingRequest.key is: " + this.mOutstandingRequest.key);
                            break;
                        case RttServiceImpl.CMD_RTT_RESPONSE /*160515*/:
                            Log.d(RttService.TAG, "Received an RTT response from: " + msg.arg2);
                            this.mOutstandingRequest.ci.reportResult(this.mOutstandingRequest, (RttResult[]) msg.obj);
                            this.mOutstandingRequest = null;
                            RttStateMachine.this.sendMessage(RttServiceImpl.CMD_ISSUE_NEXT_REQUEST);
                            break;
                        default:
                            return false;
                    }
                    return RttService.DBG;
                }
            }

            class ResponderEnabledState extends State {
                ResponderEnabledState() {
                }

                public boolean processMessage(Message msg) {
                    Log.d(RttService.TAG, "ResponderEnabledState got " + msg);
                    ClientInfo ci = (ClientInfo) RttServiceImpl.this.mClients.get(msg.replyTo);
                    int key = msg.arg2;
                    switch (msg.what) {
                        case 160256:
                        case 160257:
                            RttServiceImpl.this.replyFailed(msg, -6, "Initiator not allowed when responder is turned on");
                            return RttService.DBG;
                        case 160261:
                            ci.addResponderRequest(key);
                            ci.reportResponderEnableSucceed(key, RttStateMachine.this.mResponderConfig);
                            return RttService.DBG;
                        case 160262:
                            if (ci != null) {
                                ci.removeResponderRequest(key);
                            }
                            if (!RttStateMachine.this.hasOutstandingReponderRequests()) {
                                if (!RttServiceImpl.this.mWifiNative.disableRttResponder()) {
                                    Log.e(RttService.TAG, "disable responder failed");
                                }
                                Log.d(RttService.TAG, "mWifiNative.disableRttResponder called");
                                RttStateMachine.this.transitionTo(RttStateMachine.this.mEnabledState);
                            }
                            return RttService.DBG;
                        default:
                            return false;
                    }
                }
            }

            RttStateMachine(Looper looper) {
                super("RttStateMachine", looper);
                this.mDefaultState = new DefaultState();
                this.mEnabledState = new EnabledState();
                this.mInitiatorEnabledState = new InitiatorEnabledState();
                this.mResponderEnabledState = new ResponderEnabledState();
                addState(this.mDefaultState);
                addState(this.mEnabledState);
                addState(this.mInitiatorEnabledState, this.mEnabledState);
                addState(this.mResponderEnabledState, this.mEnabledState);
                setInitialState(this.mDefaultState);
            }

            private boolean hasOutstandingReponderRequests() {
                for (ClientInfo client : RttServiceImpl.this.mClients.values()) {
                    if (!client.mResponderRequests.isEmpty()) {
                        return RttService.DBG;
                    }
                }
                return false;
            }
        }

        public Messenger getMessenger() {
            return new Messenger(this.mClientHandler);
        }

        RttServiceImpl(Context context, Looper looper) {
            this.mRequestQueue = new LinkedList();
            this.mClients = new HashMap(4);
            this.mEventHandler = new RttEventHandler() {
                public void onRttResults(RttResult[] result) {
                    RttServiceImpl.this.mStateMachine.sendMessage(RttServiceImpl.CMD_RTT_RESPONSE, result);
                }
            };
            this.mContext = context;
            this.mWifiNative = WifiNative.getWlanNativeInterface();
            this.mLooper = looper;
        }

        public void startService() {
            this.mClientHandler = new ClientHandler(this.mLooper);
            this.mStateMachine = new RttStateMachine(this.mLooper);
            this.mContext.registerReceiver(new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    int state = intent.getIntExtra("scan_enabled", 1);
                    Log.d(RttService.TAG, "SCAN_AVAILABLE : " + state);
                    if (state == 3) {
                        RttServiceImpl.this.mStateMachine.sendMessage(RttServiceImpl.CMD_DRIVER_LOADED);
                    } else if (state == 1) {
                        RttServiceImpl.this.mStateMachine.sendMessage(RttServiceImpl.CMD_DRIVER_UNLOADED);
                    }
                }
            }, new IntentFilter("wifi_scan_available"));
            this.mStateMachine.start();
        }

        void replySucceeded(Message msg, Object obj) {
            if (msg.replyTo != null) {
                Message reply = Message.obtain();
                reply.what = 160259;
                reply.arg2 = msg.arg2;
                reply.obj = obj;
                try {
                    msg.replyTo.send(reply);
                } catch (RemoteException e) {
                }
            }
        }

        void replyFailed(Message msg, int reason, String description) {
            Message reply = Message.obtain();
            reply.what = 160258;
            reply.arg1 = reason;
            reply.arg2 = msg.arg2;
            Bundle bundle = new Bundle();
            bundle.putString("android.net.wifi.RttManager.Description", description);
            reply.obj = bundle;
            try {
                if (msg.replyTo != null) {
                    msg.replyTo.send(reply);
                }
            } catch (RemoteException e) {
            }
        }

        boolean enforcePermissionCheck(Message msg) {
            try {
                this.mContext.enforcePermission("android.permission.LOCATION_HARDWARE", -1, msg.sendingUid, "LocationRTT");
                return RttService.DBG;
            } catch (SecurityException e) {
                Log.e(RttService.TAG, "UID: " + msg.sendingUid + " has no LOCATION_HARDWARE Permission");
                return false;
            }
        }

        RttRequest issueNextRequest() {
            while (!this.mRequestQueue.isEmpty()) {
                RttRequest request = (RttRequest) this.mRequestQueue.remove();
                if (request != null) {
                    if (this.mWifiNative.requestRtt(request.params, this.mEventHandler)) {
                        Log.d(RttService.TAG, "Issued next RTT request with key: " + request.key);
                        return request;
                    }
                    Log.e(RttService.TAG, "Fail to issue key at native layer");
                    request.ci.reportFailed(request, -1, "Failed to start");
                }
            }
            Log.d(RttService.TAG, "No more requests left");
            return null;
        }

        public RttCapabilities getRttCapabilities() {
            return this.mWifiNative.getRttCapabilities();
        }
    }

    public RttService(Context context) {
        super(context);
        this.mHandlerThread = new HandlerThread("WifiRttService");
        this.mHandlerThread.start();
        Log.i(TAG, "Creating rttmanager");
    }

    public void onStart() {
        this.mImpl = new RttServiceImpl(getContext(), this.mHandlerThread.getLooper());
        Log.i(TAG, "Starting rttmanager");
        publishBinderService("rttmanager", this.mImpl);
    }

    public void onBootPhase(int phase) {
        if (phase == 500) {
            Log.i(TAG, "Registering rttmanager");
            if (this.mImpl == null) {
                this.mImpl = new RttServiceImpl(getContext(), this.mHandlerThread.getLooper());
            }
            this.mImpl.startService();
        }
    }
}
