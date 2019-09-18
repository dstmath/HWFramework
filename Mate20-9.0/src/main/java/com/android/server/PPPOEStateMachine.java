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
import android.os.INetworkManagementService;
import android.os.Message;
import android.os.PowerManager;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.Log;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
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
    /* access modifiers changed from: private */
    public ConnectedState mConnectedState = new ConnectedState();
    /* access modifiers changed from: private */
    public final AtomicLong mConnectedTimeAtomicLong = new AtomicLong(0);
    /* access modifiers changed from: private */
    public ConnectingState mConnectingState = new ConnectingState();
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public CountDownLatch mCountDownLatch = new CountDownLatch(1);
    /* access modifiers changed from: private */
    public RouteInfo mDefRoute = null;
    private DefaultState mDefaultState = new DefaultState();
    /* access modifiers changed from: private */
    public DisconnectedState mDisconnectedState = new DisconnectedState();
    /* access modifiers changed from: private */
    public DisconnectingState mDisconnectingState = new DisconnectingState();
    /* access modifiers changed from: private */
    public InitialState mInitialState = new InitialState();
    private NativeDaemonConnector mNativeConnetor;
    /* access modifiers changed from: private */
    public final AtomicInteger mPPPOEState = new AtomicInteger(PPPOEInfo.Status.OFFLINE.ordinal());
    /* access modifiers changed from: private */
    public StartFailedState mStartFailedState = new StartFailedState();
    /* access modifiers changed from: private */
    public StopFailedState mStopFailedState = new StopFailedState();
    /* access modifiers changed from: private */
    public PowerManager.WakeLock mWakeLock;

    private class ConnectedState extends State {
        private ConnectedState() {
        }

        public void enter() {
            Log.d(PPPOEStateMachine.TAG, "success to connect pppoe.");
            PPPOEStateMachine.this.mPPPOEState.set(PPPOEInfo.Status.ONLINE.ordinal());
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
            int i = msg.what;
            if (i != 151555) {
                if (i != 589826) {
                    return false;
                }
                PPPOEStateMachine.this.transitionTo(PPPOEStateMachine.this.mDisconnectingState);
            } else if (PPPOEStateMachine.PHASE_DISCONNECT.equals(NativeDaemonEvent.unescapeArgs((String) msg.obj)[1])) {
                Log.w(PPPOEStateMachine.TAG, "lost pppoe service.");
                PPPOEStateMachine.this.transitionTo(PPPOEStateMachine.this.mDisconnectingState);
            }
            return true;
        }
    }

    private class ConnectingState extends State {
        private ConnectingState() {
        }

        public void enter() {
            final Message message = new Message();
            message.copyFrom(PPPOEStateMachine.this.getCurrentMessage());
            new Thread(new Runnable() {
                public void run() {
                    PPPOEStateMachine.this.mWakeLock.acquire();
                    PPPOEStateMachine.this.mPPPOEState.set(PPPOEInfo.Status.CONNECTING.ordinal());
                    Intent intent = new Intent("android.net.wifi.PPPOE_STATE_CHANGED");
                    intent.addFlags(67108864);
                    intent.putExtra("pppoe_state", "PPPOE_STATE_CONNECTING");
                    PPPOEStateMachine.this.mContext.sendStickyBroadcast(intent);
                    Log.d(PPPOEStateMachine.TAG, "start PPPOE");
                    if (!PPPOEStateMachine.this.startPPPOE((PPPOEConfig) message.obj)) {
                        PPPOEStateMachine.this.mStartFailedState.setErrorCode("FAILURE_INTERNAL_ERROR");
                        PPPOEStateMachine.this.transitionTo(PPPOEStateMachine.this.mStartFailedState);
                    }
                    PPPOEStateMachine.this.mWakeLock.release();
                }
            }).start();
        }

        public boolean processMessage(Message msg) {
            if (msg.what != 151555) {
                return false;
            }
            String[] cooked = NativeDaemonEvent.unescapeArgs((String) msg.obj);
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
        }

        private boolean updateConfig() {
            INetworkManagementService netService = INetworkManagementService.Stub.asInterface(ServiceManager.getService("network_management"));
            ConnectivityManager connectivityManager = (ConnectivityManager) PPPOEStateMachine.this.mContext.getSystemService("connectivity");
            LinkProperties prop = connectivityManager.getActiveLinkProperties();
            Network wifiNetwork = connectivityManager.getNetworkForType(1);
            if (wifiNetwork == null) {
                Log.e(PPPOEStateMachine.TAG, "ConnectingState, updateConfig, No Network for type WIFI!");
                return false;
            }
            if (prop != null) {
                Iterator<RouteInfo> it = prop.getRoutes().iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    RouteInfo routeInfo = it.next();
                    if (routeInfo.isDefaultRoute()) {
                        RouteInfo unused = PPPOEStateMachine.this.mDefRoute = routeInfo;
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
            InetAddress destAddr = NetworkUtils.numericToInetAddress(SystemProperties.get("ppp.InetAddress", "0.0.0.0"));
            RouteInfo route = new RouteInfo(new LinkAddress(destAddr, 0), NetworkUtils.numericToInetAddress(SystemProperties.get("ppp.gateway", "0.0.0.0")), interfaceName);
            List<RouteInfo> routes = new ArrayList<>();
            routes.add(route);
            Log.d(PPPOEStateMachine.TAG, "add route " + route.toString());
            try {
                netService.addInterfaceToLocalNetwork(interfaceName, routes);
                if (prop != null) {
                    List<InetAddress> dnsServers = prop.getDnsServers();
                    String dns2 = SystemProperties.get("net." + interfaceName + ".dns2");
                    if (dns2 != null && dns2.trim().length() > 0) {
                        prop.addDnsServer(NetworkUtils.numericToInetAddress(dns2));
                        SystemProperties.set("net.dns2", dns2);
                    }
                    String dns1 = SystemProperties.get("net." + interfaceName + ".dns1");
                    if (dns1 == null || dns1.trim().length() <= 0) {
                    } else {
                        prop.addDnsServer(NetworkUtils.numericToInetAddress(dns1));
                        INetworkManagementService iNetworkManagementService = netService;
                        SystemProperties.set("net.dns1", dns1);
                    }
                    String[] args = NetworkUtils.makeStrings(dnsServers);
                    int length = args.length;
                    List<InetAddress> list = dnsServers;
                    int i = 0;
                    while (i < length) {
                        ConnectivityManager connectivityManager2 = connectivityManager;
                        String arg = args[i];
                        LinkProperties prop2 = prop;
                        Log.e(PPPOEStateMachine.TAG, "after set dns servers: " + arg);
                        i++;
                        connectivityManager = connectivityManager2;
                        args = args;
                        prop = prop2;
                        wifiNetwork = wifiNetwork;
                    }
                    ConnectivityManager connectivityManager3 = connectivityManager;
                    LinkProperties linkProperties = prop;
                    Network network = wifiNetwork;
                    Log.d(PPPOEStateMachine.TAG, "set net.dns1 :" + dns1 + " net.dns2: " + dns2);
                    return true;
                }
                ConnectivityManager connectivityManager4 = connectivityManager;
                LinkProperties linkProperties2 = prop;
                Network network2 = wifiNetwork;
                return false;
            } catch (Exception e2) {
                INetworkManagementService iNetworkManagementService2 = netService;
                ConnectivityManager connectivityManager5 = connectivityManager;
                LinkProperties linkProperties3 = prop;
                Network network3 = wifiNetwork;
                Exception exc = e2;
                Log.e(PPPOEStateMachine.TAG, "Failed to add route " + route, e2);
                return false;
            }
        }
    }

    private class DefaultState extends State {
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
                    if (PPPOEStateMachine.this.mPPPOEState.get() == PPPOEInfo.Status.OFFLINE.ordinal()) {
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
                    if (PPPOEStateMachine.this.mPPPOEState.get() != PPPOEInfo.Status.ONLINE.ordinal()) {
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
        private DisconnectedState() {
        }

        public void enter() {
            updateConfig();
            PPPOEStateMachine.this.mConnectedTimeAtomicLong.set(0);
            PPPOEStateMachine.this.mPPPOEState.set(PPPOEInfo.Status.OFFLINE.ordinal());
            Intent connectIntent = new Intent("android.net.wifi.PPPOE_STATE_CHANGED");
            connectIntent.addFlags(67108864);
            connectIntent.putExtra("pppoe_state", "PPPOE_STATE_DISCONNECTED");
            PPPOEStateMachine.this.mContext.sendStickyBroadcast(connectIntent);
        }

        private boolean updateConfig() {
            INetworkManagementService netService = INetworkManagementService.Stub.asInterface(ServiceManager.getService("network_management"));
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
                netService.addRoute(wifiNetwork.netId, PPPOEStateMachine.this.mDefRoute);
                String dns1 = SystemProperties.get("dhcp." + interfaceName + ".dns1");
                if (dns1 != null && dns1.trim().length() > 0) {
                    InetAddress dns = NetworkUtils.numericToInetAddress(dns1);
                    if (prop != null) {
                        prop.addDnsServer(dns);
                    }
                    SystemProperties.set("net.dns1", dns1);
                }
                String dns2 = SystemProperties.get("dhcp." + interfaceName + ".dns2");
                if (dns2 != null && dns2.trim().length() > 0) {
                    InetAddress dns3 = NetworkUtils.numericToInetAddress(dns2);
                    if (prop != null) {
                        prop.addDnsServer(dns3);
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
            if (msg.what != 151555) {
                return false;
            }
            String[] cooked = NativeDaemonEvent.unescapeArgs((String) msg.obj);
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
        }
    }

    private class InitialState extends State {
        private InitialState() {
        }

        public boolean processMessage(Message msg) {
            if (msg.what != 589825) {
                return false;
            }
            if (PPPOEStateMachine.this.mPPPOEState.get() == PPPOEInfo.Status.OFFLINE.ordinal()) {
                PPPOEStateMachine.this.transitionTo(PPPOEStateMachine.this.mConnectingState);
                return true;
            }
            Intent completeIntent = new Intent("android.net.wifi.PPPOE_COMPLETED_ACTION");
            completeIntent.addFlags(67108864);
            completeIntent.putExtra("pppoe_result_status", "ALREADY_ONLINE");
            PPPOEStateMachine.this.mContext.sendStickyBroadcast(completeIntent);
            return true;
        }
    }

    public class NativeDaemonConnectorCallbacks implements INativeDaemonConnectorCallbacks {
        public NativeDaemonConnectorCallbacks() {
        }

        public void onDaemonConnected() {
            Log.d(PPPOEStateMachine.TAG, "Start native daemon connector success.");
            PPPOEStateMachine.this.mCountDownLatch.countDown();
        }

        public boolean onCheckHoldWakeLock(int code) {
            return false;
        }

        public boolean onEvent(int code, String raw, String[] cooked) {
            if (code == 652) {
                Log.d(PPPOEStateMachine.TAG, "onEvent receive native daemon connector event, code=" + code + ",raw=" + raw);
                PPPOEStateMachine.this.sendMessage(151555, raw);
            }
            return true;
        }
    }

    private class StartFailedState extends State {
        private String errorCode;

        private StartFailedState() {
        }

        public void enter() {
            Log.e(PPPOEStateMachine.TAG, "Failed to start PPPOE, error code is " + this.errorCode);
            boolean unused = PPPOEStateMachine.this.stopPPPOE();
            PPPOEStateMachine.this.mPPPOEState.set(PPPOEInfo.Status.OFFLINE.ordinal());
            Intent intent = new Intent("android.net.wifi.PPPOE_COMPLETED_ACTION");
            intent.addFlags(67108864);
            intent.putExtra("pppoe_result_status", "FAILURE");
            intent.putExtra("pppoe_result_error_code", this.errorCode);
            PPPOEStateMachine.this.mContext.sendStickyBroadcast(intent);
            PPPOEStateMachine.this.transitionTo(PPPOEStateMachine.this.mInitialState);
        }

        public void setErrorCode(String errorCode2) {
            this.errorCode = errorCode2;
        }
    }

    private class StopFailedState extends State {
        private String errorCode;

        private StopFailedState() {
        }

        public void enter() {
            PPPOEStateMachine.this.mConnectedTimeAtomicLong.set(0);
            PPPOEStateMachine.this.mPPPOEState.set(PPPOEInfo.Status.OFFLINE.ordinal());
            Log.e(PPPOEStateMachine.TAG, "Failed to stop PPPOE , error code is " + this.errorCode);
        }

        public void setErrorCode(String errorCode2) {
            this.errorCode = errorCode2;
        }
    }

    static {
        stateCode.add("0");
        stateCode.add("1");
        stateCode.add("2");
        stateCode.add("3");
        stateCode.add("4");
        stateCode.add("5");
        stateCode.add("6");
        stateCode.add("7");
        stateCode.add(PHASE_RUNNING);
        stateCode.add(PHASE_TERMINATE);
        stateCode.add(PHASE_DISCONNECT);
        stateCode.add(PHASE_HOLDOFF);
        stateCode.add("12");
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
        NativeDaemonConnector nativeDaemonConnector = new NativeDaemonConnector(new NativeDaemonConnectorCallbacks(), "netd", 10, PPPOE_TAG, 25, null);
        this.mNativeConnetor = nativeDaemonConnector;
    }

    public void start() {
        new Thread(this.mNativeConnetor, PPPOE_TAG).start();
        try {
            this.mCountDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        PPPOEStateMachine.super.start();
    }

    private void checkAndSetConnectivityInstance() {
        if (this.mCm == null) {
            this.mCm = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        }
    }

    /* access modifiers changed from: private */
    public boolean stopPPPOE() {
        Log.d(TAG, "execute stop PPPOE");
        try {
            this.mNativeConnetor.execute("pppoed", new Object[]{"stop"});
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to stop PPPOE.", e);
            return false;
        }
    }

    /* access modifiers changed from: private */
    public boolean startPPPOE(PPPOEConfig serverConfig) {
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
        PPPOEInfo.Status status = PPPOEInfo.Status.fromInt(this.mPPPOEState.get());
        long onLineTime = 0;
        if (status.equals(PPPOEInfo.Status.ONLINE)) {
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
