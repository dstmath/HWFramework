package com.android.server;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkUtils;
import android.net.RouteInfo;
import android.net.wifi.PPPOEConfig;
import android.net.wifi.PPPOEInfo;
import android.net.wifi.PPPOEInfo.Status;
import android.os.INetworkManagementService;
import android.os.INetworkManagementService.Stub;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.Log;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class PPPOEStateMachine extends StateMachine {
    private static final boolean DEBUG = true;
    public static final int MAX_LOG_SIZE = 25;
    private static final String NETWORKTYPE = "WIFI_PPPOE";
    public static final String PHASE_AUTHENTICATE = "5";
    public static final String PHASE_CALLBACK = "6";
    public static final String PHASE_DEAD = "0";
    public static final String PHASE_DISCONNECT = "10";
    public static final String PHASE_DORMANT = "3";
    public static final String PHASE_ESTABLISH = "4";
    public static final String PHASE_HOLDOFF = "11";
    public static final String PHASE_INITIALIZE = "1";
    public static final String PHASE_MASTER = "12";
    public static final String PHASE_NETWORK = "7";
    public static final String PHASE_RUNNING = "8";
    public static final String PHASE_SERIALCONN = "2";
    public static final String PHASE_TERMINATE = "9";
    public static final int PPPOE_EVENT_CODE = 652;
    private static final String PPPOE_TAG = "PPPOEConnector";
    public static final int RESPONSE_QUEUE_SIZE = 10;
    private static final String TAG = "PPPOEStateMachine";
    static final Set<String> stateCode = new HashSet();
    private ConnectivityManager mCm;
    private ConnectedState mConnectedState = new ConnectedState(this, null);
    private final AtomicLong mConnectedTimeAtomicLong = new AtomicLong(0);
    private ConnectingState mConnectingState = new ConnectingState(this, null);
    private Context mContext;
    private CountDownLatch mCountDownLatch = new CountDownLatch(1);
    private RouteInfo mDefRoute = null;
    private DefaultState mDefaultState = new DefaultState(this, null);
    private DisconnectedState mDisconnectedState = new DisconnectedState(this, null);
    private DisconnectingState mDisconnectingState = new DisconnectingState(this, null);
    private InitialState mInitialState = new InitialState(this, null);
    private NativeDaemonConnector mNativeConnetor;
    private final AtomicInteger mPPPOEState = new AtomicInteger(Status.OFFLINE.ordinal());
    private StartFailedState mStartFailedState = new StartFailedState(this, null);
    private StopFailedState mStopFailedState = new StopFailedState(this, null);
    private WakeLock mWakeLock;

    private class ConnectedState extends State {
        /* synthetic */ ConnectedState(PPPOEStateMachine this$0, ConnectedState -this1) {
            this();
        }

        private ConnectedState() {
        }

        public void enter() {
            Log.d(PPPOEStateMachine.TAG, "success to connect pppoe.");
            PPPOEStateMachine.this.mPPPOEState.set(Status.ONLINE.ordinal());
            PPPOEStateMachine.this.mConnectedTimeAtomicLong.set(System.currentTimeMillis());
            Intent completeIntent = new Intent("android.net.wifi.PPPOE_COMPLETED_ACTION");
            completeIntent.addFlags(67108864);
            completeIntent.putExtra("pppoe_result_status", "SUCCESS");
            PPPOEStateMachine.this.mContext.sendStickyBroadcast(completeIntent);
            Intent connectIntent = new Intent("android.net.wifi.PPPOE_STATE_CHANGED");
            connectIntent.addFlags(67108864);
            connectIntent.putExtra("pppoe_state", "PPPOE_STATE_CONNECTED");
            PPPOEStateMachine.this.mContext.sendStickyBroadcast(connectIntent);
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case 151555:
                    if (PPPOEStateMachine.PHASE_DISCONNECT.equals(NativeDaemonEvent.unescapeArgs(msg.obj)[1])) {
                        Log.w(PPPOEStateMachine.TAG, "lost pppoe service.");
                        PPPOEStateMachine.this.transitionTo(PPPOEStateMachine.this.mDisconnectingState);
                        break;
                    }
                    break;
                case 589826:
                    PPPOEStateMachine.this.transitionTo(PPPOEStateMachine.this.mDisconnectingState);
                    break;
                default:
                    return false;
            }
            return true;
        }
    }

    private class ConnectingState extends State {
        /* synthetic */ ConnectingState(PPPOEStateMachine this$0, ConnectingState -this1) {
            this();
        }

        private ConnectingState() {
        }

        public void enter() {
            final Message message = new Message();
            message.copyFrom(PPPOEStateMachine.this.getCurrentMessage());
            new Thread(new Runnable() {
                public void run() {
                    PPPOEStateMachine.this.mWakeLock.acquire();
                    PPPOEStateMachine.this.mPPPOEState.set(Status.CONNECTING.ordinal());
                    Intent intent = new Intent("android.net.wifi.PPPOE_STATE_CHANGED");
                    intent.addFlags(67108864);
                    intent.putExtra("pppoe_state", "PPPOE_STATE_CONNECTING");
                    PPPOEStateMachine.this.mContext.sendStickyBroadcast(intent);
                    PPPOEConfig serverInfoConfig = message.obj;
                    Log.d(PPPOEStateMachine.TAG, "start PPPOE");
                    if (!PPPOEStateMachine.this.startPPPOE(serverInfoConfig)) {
                        PPPOEStateMachine.this.mStartFailedState.setErrorCode("FAILURE_INTERNAL_ERROR");
                        PPPOEStateMachine.this.transitionTo(PPPOEStateMachine.this.mStartFailedState);
                    }
                    PPPOEStateMachine.this.mWakeLock.release();
                }
            }).start();
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case 151555:
                    String[] cooked = NativeDaemonEvent.unescapeArgs(msg.obj);
                    if (PPPOEStateMachine.PHASE_RUNNING.equals(cooked[1])) {
                        if (updateConfig()) {
                            PPPOEStateMachine.this.transitionTo(PPPOEStateMachine.this.mConnectedState);
                            return true;
                        }
                        PPPOEStateMachine.this.mStartFailedState.setErrorCode("FAILURE_INTERNAL_ERROR");
                        PPPOEStateMachine.this.transitionTo(PPPOEStateMachine.this.mStartFailedState);
                        return true;
                    } else if (PPPOEStateMachine.PHASE_DISCONNECT.equals(cooked[1])) {
                        PPPOEStateMachine.this.mStartFailedState.setErrorCode("FAILURE_INTERNAL_ERROR");
                        PPPOEStateMachine.this.transitionTo(PPPOEStateMachine.this.mStartFailedState);
                        return true;
                    } else if (PPPOEStateMachine.stateCode.contains(cooked[1])) {
                        return true;
                    } else {
                        PPPOEStateMachine.this.mStartFailedState.setErrorCode(cooked[1]);
                        PPPOEStateMachine.this.transitionTo(PPPOEStateMachine.this.mStartFailedState);
                        return true;
                    }
                default:
                    return false;
            }
        }

        private boolean updateConfig() {
            INetworkManagementService netService = Stub.asInterface(ServiceManager.getService("network_management"));
            ConnectivityManager connectivityManager = (ConnectivityManager) PPPOEStateMachine.this.mContext.getSystemService("connectivity");
            LinkProperties prop = connectivityManager.getActiveLinkProperties();
            Network wifiNetwork = connectivityManager.getNetworkForType(1);
            if (wifiNetwork == null) {
                Log.e(PPPOEStateMachine.TAG, "ConnectingState, updateConfig, No Network for type WIFI!");
                return false;
            }
            if (prop != null) {
                for (RouteInfo routeInfo : prop.getRoutes()) {
                    if (routeInfo.isDefaultRoute()) {
                        PPPOEStateMachine.this.mDefRoute = routeInfo;
                        break;
                    }
                }
                if (PPPOEStateMachine.this.mDefRoute != null) {
                    Log.d(PPPOEStateMachine.TAG, "Remove default route " + PPPOEStateMachine.this.mDefRoute.toString());
                    try {
                        netService.removeRoute(wifiNetwork.netId, PPPOEStateMachine.this.mDefRoute);
                    } catch (Exception e) {
                        Log.e(PPPOEStateMachine.TAG, "Failed to remove route " + PPPOEStateMachine.this.mDefRoute, e);
                        return false;
                    }
                }
            }
            String interfaceName = SystemProperties.get("ppp.interface", "ppp0");
            RouteInfo routeInfo2 = new RouteInfo(new LinkAddress(NetworkUtils.numericToInetAddress(SystemProperties.get("ppp.InetAddress", "0.0.0.0")), 0), NetworkUtils.numericToInetAddress(SystemProperties.get("ppp.gateway", "0.0.0.0")), interfaceName);
            List<RouteInfo> routes = new ArrayList();
            routes.add(routeInfo2);
            Log.d(PPPOEStateMachine.TAG, "add route " + routeInfo2.toString());
            try {
                netService.addInterfaceToLocalNetwork(interfaceName, routes);
                if (prop == null) {
                    return false;
                }
                Collection<InetAddress> dnses = prop.getDnsServers();
                String dns2 = SystemProperties.get("net." + interfaceName + ".dns2");
                if (dns2 != null && dns2.trim().length() > 0) {
                    prop.addDnsServer(NetworkUtils.numericToInetAddress(dns2));
                    SystemProperties.set("net.dns2", dns2);
                }
                String dns1 = SystemProperties.get("net." + interfaceName + ".dns1");
                if (dns1 != null && dns1.trim().length() > 0) {
                    prop.addDnsServer(NetworkUtils.numericToInetAddress(dns1));
                    SystemProperties.set("net.dns1", dns1);
                }
                for (String arg : NetworkUtils.makeStrings(dnses)) {
                    Log.e(PPPOEStateMachine.TAG, "after set dns servers: " + arg);
                }
                Log.d(PPPOEStateMachine.TAG, "set net.dns1 :" + dns1 + " net.dns2: " + dns2);
                return true;
            } catch (Exception e2) {
                Log.e(PPPOEStateMachine.TAG, "Failed to add route " + routeInfo2, e2);
                return false;
            }
        }
    }

    private class DefaultState extends State {
        /* synthetic */ DefaultState(PPPOEStateMachine this$0, DefaultState -this1) {
            this();
        }

        private DefaultState() {
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case 69632:
                    if (msg.arg1 == 0) {
                        Log.d(PPPOEStateMachine.TAG, "New client listening to asynchronous messages");
                        return true;
                    }
                    Log.e(PPPOEStateMachine.TAG, "Client connection failure, error=" + msg.arg1);
                    return true;
                case 69633:
                case 151555:
                    return true;
                case 69636:
                    if (msg.arg1 == 2) {
                        Log.e(PPPOEStateMachine.TAG, "Send failed, client connection lost");
                        return true;
                    }
                    Log.d(PPPOEStateMachine.TAG, "Client connection lost with reason: " + msg.arg1);
                    return true;
                case 589825:
                    Log.d(PPPOEStateMachine.TAG, "start PPPOE");
                    if (PPPOEStateMachine.this.mPPPOEState.get() == Status.OFFLINE.ordinal()) {
                        PPPOEStateMachine.this.transitionTo(PPPOEStateMachine.this.mConnectingState);
                        return true;
                    }
                    Log.w(PPPOEStateMachine.TAG, "the pppoe is already online.");
                    Intent completeIntent = new Intent("android.net.wifi.PPPOE_COMPLETED_ACTION");
                    completeIntent.addFlags(67108864);
                    completeIntent.putExtra("pppoe_result_status", "ALREADY_ONLINE");
                    PPPOEStateMachine.this.mContext.sendStickyBroadcast(completeIntent);
                    return true;
                case 589826:
                    Log.d(PPPOEStateMachine.TAG, "stop PPPOE");
                    if (PPPOEStateMachine.this.mPPPOEState.get() != Status.ONLINE.ordinal()) {
                        return true;
                    }
                    PPPOEStateMachine.this.transitionTo(PPPOEStateMachine.this.mDisconnectingState);
                    return true;
                default:
                    return false;
            }
        }
    }

    private class DisconnectedState extends State {
        /* synthetic */ DisconnectedState(PPPOEStateMachine this$0, DisconnectedState -this1) {
            this();
        }

        private DisconnectedState() {
        }

        public void enter() {
            updateConfig();
            PPPOEStateMachine.this.mConnectedTimeAtomicLong.set(0);
            PPPOEStateMachine.this.mPPPOEState.set(Status.OFFLINE.ordinal());
            Intent connectIntent = new Intent("android.net.wifi.PPPOE_STATE_CHANGED");
            connectIntent.addFlags(67108864);
            connectIntent.putExtra("pppoe_state", "PPPOE_STATE_DISCONNECTED");
            PPPOEStateMachine.this.mContext.sendStickyBroadcast(connectIntent);
        }

        private boolean updateConfig() {
            INetworkManagementService netService = Stub.asInterface(ServiceManager.getService("network_management"));
            ConnectivityManager connectivityManager = (ConnectivityManager) PPPOEStateMachine.this.mContext.getSystemService("connectivity");
            LinkProperties prop = connectivityManager.getActiveLinkProperties();
            Network wifiNetwork = connectivityManager.getNetworkForType(1);
            if (wifiNetwork == null) {
                Log.e(PPPOEStateMachine.TAG, "DisconnectedState, updateConfig, No Network for type WiFi!");
                return false;
            }
            if (prop != null) {
                Collection<InetAddress> dnses = prop.getDnsServers();
            }
            String interfaceName = SystemProperties.get("wifi.interface", "wlan0");
            if (PPPOEStateMachine.this.mDefRoute == null) {
                return false;
            }
            Log.d(PPPOEStateMachine.TAG, "Reset default route via %s" + interfaceName);
            try {
                InetAddress dns;
                netService.addRoute(wifiNetwork.netId, PPPOEStateMachine.this.mDefRoute);
                String dns1 = SystemProperties.get("dhcp." + interfaceName + ".dns1");
                if (dns1 != null && dns1.trim().length() > 0) {
                    dns = NetworkUtils.numericToInetAddress(dns1);
                    if (prop != null) {
                        prop.addDnsServer(dns);
                    }
                    SystemProperties.set("net.dns1", dns1);
                }
                String dns2 = SystemProperties.get("dhcp." + interfaceName + ".dns2");
                if (dns2 != null && dns2.trim().length() > 0) {
                    dns = NetworkUtils.numericToInetAddress(dns2);
                    if (prop != null) {
                        prop.addDnsServer(dns);
                    }
                    SystemProperties.set("net.dns2", dns2);
                }
                Log.d(PPPOEStateMachine.TAG, "set net.dns1 :" + dns1 + " net.dns2: " + dns2);
                return true;
            } catch (Exception e) {
                Log.e(PPPOEStateMachine.TAG, "Failed to add route " + PPPOEStateMachine.this.mDefRoute, e);
                return false;
            }
        }
    }

    private class DisconnectingState extends State {
        /* synthetic */ DisconnectingState(PPPOEStateMachine this$0, DisconnectingState -this1) {
            this();
        }

        private DisconnectingState() {
        }

        public void enter() {
            new Message().copyFrom(PPPOEStateMachine.this.getCurrentMessage());
            new Thread(new Runnable() {
                public void run() {
                    PPPOEStateMachine.this.mWakeLock.acquire();
                    Intent intent = new Intent("android.net.wifi.PPPOE_STATE_CHANGED");
                    intent.addFlags(67108864);
                    intent.putExtra("pppoe_state", "PPPOE_STATE_DISCONNECTING");
                    PPPOEStateMachine.this.mContext.sendStickyBroadcast(intent);
                    Log.d(PPPOEStateMachine.TAG, "stop PPPOE");
                    if (!PPPOEStateMachine.this.stopPPPOE()) {
                        PPPOEStateMachine.this.mStopFailedState.setErrorCode("FAILURE_INTERNAL_ERROR");
                        PPPOEStateMachine.this.transitionTo(PPPOEStateMachine.this.mStopFailedState);
                    }
                    PPPOEStateMachine.this.mWakeLock.release();
                }
            }).start();
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case 151555:
                    String[] cooked = NativeDaemonEvent.unescapeArgs(msg.obj);
                    if ("0".equals(cooked[1])) {
                        PPPOEStateMachine.this.transitionTo(PPPOEStateMachine.this.mDisconnectedState);
                        return true;
                    } else if (PPPOEStateMachine.stateCode.contains(cooked[1])) {
                        return true;
                    } else {
                        PPPOEStateMachine.this.mStopFailedState.setErrorCode(cooked[1]);
                        PPPOEStateMachine.this.transitionTo(PPPOEStateMachine.this.mStopFailedState);
                        return true;
                    }
                default:
                    return false;
            }
        }
    }

    private class InitialState extends State {
        /* synthetic */ InitialState(PPPOEStateMachine this$0, InitialState -this1) {
            this();
        }

        private InitialState() {
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case 589825:
                    if (PPPOEStateMachine.this.mPPPOEState.get() == Status.OFFLINE.ordinal()) {
                        PPPOEStateMachine.this.transitionTo(PPPOEStateMachine.this.mConnectingState);
                        return true;
                    }
                    Intent completeIntent = new Intent("android.net.wifi.PPPOE_COMPLETED_ACTION");
                    completeIntent.addFlags(67108864);
                    completeIntent.putExtra("pppoe_result_status", "ALREADY_ONLINE");
                    PPPOEStateMachine.this.mContext.sendStickyBroadcast(completeIntent);
                    return true;
                default:
                    return false;
            }
        }
    }

    public class NativeDaemonConnectorCallbacks implements INativeDaemonConnectorCallbacks {
        public void onDaemonConnected() {
            Log.d(PPPOEStateMachine.TAG, "Start native daemon connector success.");
            PPPOEStateMachine.this.mCountDownLatch.countDown();
        }

        public boolean onCheckHoldWakeLock(int code) {
            return false;
        }

        public boolean onEvent(int code, String raw, String[] cooked) {
            if (code == PPPOEStateMachine.PPPOE_EVENT_CODE) {
                Log.d(PPPOEStateMachine.TAG, "onEvent receive native daemon connector event, code=" + code + ",raw=" + raw);
                PPPOEStateMachine.this.sendMessage(151555, raw);
            }
            return true;
        }
    }

    private class StartFailedState extends State {
        private String errorCode;

        /* synthetic */ StartFailedState(PPPOEStateMachine this$0, StartFailedState -this1) {
            this();
        }

        private StartFailedState() {
        }

        public void enter() {
            Log.e(PPPOEStateMachine.TAG, "Failed to start PPPOE, error code is " + this.errorCode);
            PPPOEStateMachine.this.stopPPPOE();
            PPPOEStateMachine.this.mPPPOEState.set(Status.OFFLINE.ordinal());
            Intent intent = new Intent("android.net.wifi.PPPOE_COMPLETED_ACTION");
            intent.addFlags(67108864);
            intent.putExtra("pppoe_result_status", "FAILURE");
            intent.putExtra("pppoe_result_error_code", this.errorCode);
            PPPOEStateMachine.this.mContext.sendStickyBroadcast(intent);
            PPPOEStateMachine.this.transitionTo(PPPOEStateMachine.this.mInitialState);
        }

        public void setErrorCode(String errorCode) {
            this.errorCode = errorCode;
        }
    }

    private class StopFailedState extends State {
        private String errorCode;

        /* synthetic */ StopFailedState(PPPOEStateMachine this$0, StopFailedState -this1) {
            this();
        }

        private StopFailedState() {
        }

        public void enter() {
            PPPOEStateMachine.this.mConnectedTimeAtomicLong.set(0);
            PPPOEStateMachine.this.mPPPOEState.set(Status.OFFLINE.ordinal());
            Log.e(PPPOEStateMachine.TAG, "Failed to stop PPPOE , error code is " + this.errorCode);
        }

        public void setErrorCode(String errorCode) {
            this.errorCode = errorCode;
        }
    }

    static {
        stateCode.add("0");
        stateCode.add("1");
        stateCode.add(PHASE_SERIALCONN);
        stateCode.add(PHASE_DORMANT);
        stateCode.add(PHASE_ESTABLISH);
        stateCode.add(PHASE_AUTHENTICATE);
        stateCode.add(PHASE_CALLBACK);
        stateCode.add(PHASE_NETWORK);
        stateCode.add(PHASE_RUNNING);
        stateCode.add(PHASE_TERMINATE);
        stateCode.add(PHASE_DISCONNECT);
        stateCode.add(PHASE_HOLDOFF);
        stateCode.add(PHASE_MASTER);
    }

    public PPPOEStateMachine(Context context, String name) {
        super(name);
        Log.d(TAG, " create PPPOE state Machine");
        this.mContext = context;
        this.mWakeLock = ((PowerManager) this.mContext.getSystemService("power")).newWakeLock(1, TAG);
        addState(this.mDefaultState);
        addState(this.mInitialState, this.mDefaultState);
        addState(this.mConnectingState, this.mDefaultState);
        addState(this.mConnectedState, this.mDefaultState);
        addState(this.mStartFailedState, this.mDefaultState);
        addState(this.mDisconnectingState, this.mDefaultState);
        addState(this.mDisconnectedState, this.mDefaultState);
        addState(this.mStopFailedState, this.mDefaultState);
        setInitialState(this.mInitialState);
        this.mNativeConnetor = new NativeDaemonConnector(new NativeDaemonConnectorCallbacks(), "netd", 10, PPPOE_TAG, 25, null);
    }

    public void start() {
        new Thread(this.mNativeConnetor, PPPOE_TAG).start();
        try {
            this.mCountDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        super.start();
    }

    private void checkAndSetConnectivityInstance() {
        if (this.mCm == null) {
            this.mCm = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        }
    }

    private boolean stopPPPOE() {
        Log.d(TAG, "execute stop PPPOE");
        try {
            this.mNativeConnetor.execute("pppoed", new Object[]{"stop"});
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to stop PPPOE.", e);
            return false;
        }
    }

    private boolean startPPPOE(PPPOEConfig serverConfig) {
        Log.d(TAG, "startPPPOE");
        try {
            this.mNativeConnetor.execute("pppoed", buildStartArgs(serverConfig));
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to start PPPOE", e);
            return false;
        }
    }

    public PPPOEInfo getPPPOEInfo() {
        Log.d(TAG, "getPPPOEInfo");
        Status status = Status.fromInt(this.mPPPOEState.get());
        long onLineTime = 0;
        if (status.equals(Status.ONLINE)) {
            onLineTime = (System.currentTimeMillis() - this.mConnectedTimeAtomicLong.get()) / 1000;
        }
        return new PPPOEInfo(status, onLineTime);
    }

    private Object[] buildStartArgs(PPPOEConfig serverConfig) {
        Object[] cfgArgs = serverConfig.getArgs();
        int argc = cfgArgs.length + 1;
        Object[] argv = new Object[argc];
        argv[0] = "start";
        for (int i = 1; i < argc; i++) {
            argv[i] = cfgArgs[i - 1];
        }
        return argv;
    }
}
