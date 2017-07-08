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
    static final Set<String> stateCode = null;
    private ConnectivityManager mCm;
    private ConnectedState mConnectedState;
    private final AtomicLong mConnectedTimeAtomicLong;
    private ConnectingState mConnectingState;
    private Context mContext;
    private CountDownLatch mCountDownLatch;
    private RouteInfo mDefRoute;
    private DefaultState mDefaultState;
    private DisconnectedState mDisconnectedState;
    private DisconnectingState mDisconnectingState;
    private InitialState mInitialState;
    private NativeDaemonConnector mNativeConnetor;
    private final AtomicInteger mPPPOEState;
    private StartFailedState mStartFailedState;
    private StopFailedState mStopFailedState;
    private WakeLock mWakeLock;

    private class ConnectedState extends State {
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
            return PPPOEStateMachine.DEBUG;
        }
    }

    private class ConnectingState extends State {

        /* renamed from: com.android.server.PPPOEStateMachine.ConnectingState.1 */
        class AnonymousClass1 implements Runnable {
            final /* synthetic */ Message val$message;

            AnonymousClass1(Message val$message) {
                this.val$message = val$message;
            }

            public void run() {
                PPPOEStateMachine.this.mWakeLock.acquire();
                PPPOEStateMachine.this.mPPPOEState.set(Status.CONNECTING.ordinal());
                Intent intent = new Intent("android.net.wifi.PPPOE_STATE_CHANGED");
                intent.addFlags(67108864);
                intent.putExtra("pppoe_state", "PPPOE_STATE_CONNECTING");
                PPPOEStateMachine.this.mContext.sendStickyBroadcast(intent);
                PPPOEConfig serverInfoConfig = this.val$message.obj;
                Log.d(PPPOEStateMachine.TAG, "start PPPOE");
                if (!PPPOEStateMachine.this.startPPPOE(serverInfoConfig)) {
                    PPPOEStateMachine.this.mStartFailedState.setErrorCode("FAILURE_INTERNAL_ERROR");
                    PPPOEStateMachine.this.transitionTo(PPPOEStateMachine.this.mStartFailedState);
                }
                PPPOEStateMachine.this.mWakeLock.release();
            }
        }

        private ConnectingState() {
        }

        public void enter() {
            Message message = new Message();
            message.copyFrom(PPPOEStateMachine.this.getCurrentMessage());
            new Thread(new AnonymousClass1(message)).start();
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case 151555:
                    String[] cooked = NativeDaemonEvent.unescapeArgs(msg.obj);
                    if (PPPOEStateMachine.PHASE_RUNNING.equals(cooked[1])) {
                        if (updateConfig()) {
                            PPPOEStateMachine.this.transitionTo(PPPOEStateMachine.this.mConnectedState);
                            return PPPOEStateMachine.DEBUG;
                        }
                        PPPOEStateMachine.this.mStartFailedState.setErrorCode("FAILURE_INTERNAL_ERROR");
                        PPPOEStateMachine.this.transitionTo(PPPOEStateMachine.this.mStartFailedState);
                        return PPPOEStateMachine.DEBUG;
                    } else if (PPPOEStateMachine.PHASE_DISCONNECT.equals(cooked[1])) {
                        PPPOEStateMachine.this.mStartFailedState.setErrorCode("FAILURE_INTERNAL_ERROR");
                        PPPOEStateMachine.this.transitionTo(PPPOEStateMachine.this.mStartFailedState);
                        return PPPOEStateMachine.DEBUG;
                    } else if (PPPOEStateMachine.stateCode.contains(cooked[1])) {
                        return PPPOEStateMachine.DEBUG;
                    } else {
                        PPPOEStateMachine.this.mStartFailedState.setErrorCode(cooked[1]);
                        PPPOEStateMachine.this.transitionTo(PPPOEStateMachine.this.mStartFailedState);
                        return PPPOEStateMachine.DEBUG;
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
                    try {
                        netService.setDnsServersForNetwork(wifiNetwork.netId, NetworkUtils.makeStrings(dnses), prop.getDomains());
                    } catch (Exception e2) {
                        Log.e(PPPOEStateMachine.TAG, "exception setting dns servers: " + e2);
                    }
                    SystemProperties.set("net.dns2", dns2);
                }
                String dns1 = SystemProperties.get("net." + interfaceName + ".dns1");
                if (dns1 != null && dns1.trim().length() > 0) {
                    prop.addDnsServer(NetworkUtils.numericToInetAddress(dns1));
                    try {
                        netService.setDnsServersForNetwork(wifiNetwork.netId, NetworkUtils.makeStrings(dnses), prop.getDomains());
                    } catch (Exception e22) {
                        Log.e(PPPOEStateMachine.TAG, "exception setting dns servers: " + e22);
                    }
                    SystemProperties.set("net.dns1", dns1);
                }
                for (String arg : NetworkUtils.makeStrings(dnses)) {
                    Log.e(PPPOEStateMachine.TAG, "after set dns servers: " + arg);
                }
                Log.d(PPPOEStateMachine.TAG, "set net.dns1 :" + dns1 + " net.dns2: " + dns2);
                return PPPOEStateMachine.DEBUG;
            } catch (Exception e222) {
                Log.e(PPPOEStateMachine.TAG, "Failed to add route " + routeInfo2, e222);
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
                        return PPPOEStateMachine.DEBUG;
                    }
                    Log.e(PPPOEStateMachine.TAG, "Client connection failure, error=" + msg.arg1);
                    return PPPOEStateMachine.DEBUG;
                case 69633:
                case 151555:
                    return PPPOEStateMachine.DEBUG;
                case 69636:
                    if (msg.arg1 == 2) {
                        Log.e(PPPOEStateMachine.TAG, "Send failed, client connection lost");
                        return PPPOEStateMachine.DEBUG;
                    }
                    Log.d(PPPOEStateMachine.TAG, "Client connection lost with reason: " + msg.arg1);
                    return PPPOEStateMachine.DEBUG;
                case 589825:
                    Log.d(PPPOEStateMachine.TAG, "start PPPOE");
                    if (PPPOEStateMachine.this.mPPPOEState.get() == Status.OFFLINE.ordinal()) {
                        PPPOEStateMachine.this.transitionTo(PPPOEStateMachine.this.mConnectingState);
                        return PPPOEStateMachine.DEBUG;
                    }
                    Log.w(PPPOEStateMachine.TAG, "the pppoe is already online.");
                    Intent completeIntent = new Intent("android.net.wifi.PPPOE_COMPLETED_ACTION");
                    completeIntent.addFlags(67108864);
                    completeIntent.putExtra("pppoe_result_status", "ALREADY_ONLINE");
                    PPPOEStateMachine.this.mContext.sendStickyBroadcast(completeIntent);
                    return PPPOEStateMachine.DEBUG;
                case 589826:
                    Log.d(PPPOEStateMachine.TAG, "stop PPPOE");
                    if (PPPOEStateMachine.this.mPPPOEState.get() != Status.ONLINE.ordinal()) {
                        return PPPOEStateMachine.DEBUG;
                    }
                    PPPOEStateMachine.this.transitionTo(PPPOEStateMachine.this.mDisconnectingState);
                    return PPPOEStateMachine.DEBUG;
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
            Collection dnses = null;
            if (prop != null) {
                dnses = prop.getDnsServers();
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
                        try {
                            netService.setDnsServersForNetwork(wifiNetwork.netId, NetworkUtils.makeStrings(dnses), prop.getDomains());
                        } catch (Exception e) {
                            Log.e(PPPOEStateMachine.TAG, "exception setting dns servers: " + e);
                        }
                    }
                    SystemProperties.set("net.dns1", dns1);
                }
                String dns2 = SystemProperties.get("dhcp." + interfaceName + ".dns2");
                if (dns2 != null && dns2.trim().length() > 0) {
                    dns = NetworkUtils.numericToInetAddress(dns2);
                    if (prop != null) {
                        prop.addDnsServer(dns);
                        try {
                            netService.setDnsServersForNetwork(wifiNetwork.netId, NetworkUtils.makeStrings(dnses), prop.getDomains());
                        } catch (Exception e2) {
                            Log.e(PPPOEStateMachine.TAG, "exception setting dns servers: " + e2);
                        }
                    }
                    SystemProperties.set("net.dns2", dns2);
                }
                Log.d(PPPOEStateMachine.TAG, "set net.dns1 :" + dns1 + " net.dns2: " + dns2);
                return PPPOEStateMachine.DEBUG;
            } catch (Exception e22) {
                Log.e(PPPOEStateMachine.TAG, "Failed to add route " + PPPOEStateMachine.this.mDefRoute, e22);
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
            switch (msg.what) {
                case 151555:
                    String[] cooked = NativeDaemonEvent.unescapeArgs(msg.obj);
                    if (PPPOEStateMachine.PHASE_DEAD.equals(cooked[1])) {
                        PPPOEStateMachine.this.transitionTo(PPPOEStateMachine.this.mDisconnectedState);
                        return PPPOEStateMachine.DEBUG;
                    } else if (PPPOEStateMachine.stateCode.contains(cooked[1])) {
                        return PPPOEStateMachine.DEBUG;
                    } else {
                        PPPOEStateMachine.this.mStopFailedState.setErrorCode(cooked[1]);
                        PPPOEStateMachine.this.transitionTo(PPPOEStateMachine.this.mStopFailedState);
                        return PPPOEStateMachine.DEBUG;
                    }
                default:
                    return false;
            }
        }
    }

    private class InitialState extends State {
        private InitialState() {
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case 589825:
                    if (PPPOEStateMachine.this.mPPPOEState.get() == Status.OFFLINE.ordinal()) {
                        PPPOEStateMachine.this.transitionTo(PPPOEStateMachine.this.mConnectingState);
                        return PPPOEStateMachine.DEBUG;
                    }
                    Intent completeIntent = new Intent("android.net.wifi.PPPOE_COMPLETED_ACTION");
                    completeIntent.addFlags(67108864);
                    completeIntent.putExtra("pppoe_result_status", "ALREADY_ONLINE");
                    PPPOEStateMachine.this.mContext.sendStickyBroadcast(completeIntent);
                    return PPPOEStateMachine.DEBUG;
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
            return PPPOEStateMachine.DEBUG;
        }
    }

    private class StartFailedState extends State {
        private String errorCode;

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
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.PPPOEStateMachine.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.PPPOEStateMachine.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.PPPOEStateMachine.<clinit>():void");
    }

    public PPPOEStateMachine(Context context, String name) {
        super(name);
        this.mPPPOEState = new AtomicInteger(Status.OFFLINE.ordinal());
        this.mConnectedTimeAtomicLong = new AtomicLong(0);
        this.mCountDownLatch = new CountDownLatch(1);
        this.mDefaultState = new DefaultState();
        this.mInitialState = new InitialState();
        this.mConnectingState = new ConnectingState();
        this.mConnectedState = new ConnectedState();
        this.mStartFailedState = new StartFailedState();
        this.mDisconnectingState = new DisconnectingState();
        this.mDisconnectedState = new DisconnectedState();
        this.mStopFailedState = new StopFailedState();
        this.mDefRoute = null;
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
        this.mNativeConnetor = new NativeDaemonConnector(new NativeDaemonConnectorCallbacks(), "netd", RESPONSE_QUEUE_SIZE, PPPOE_TAG, MAX_LOG_SIZE, null);
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
            return DEBUG;
        } catch (Exception e) {
            Log.e(TAG, "Failed to stop PPPOE.", e);
            return false;
        }
    }

    private boolean startPPPOE(PPPOEConfig serverConfig) {
        Log.d(TAG, "startPPPOE");
        try {
            this.mNativeConnetor.execute("pppoed", buildStartArgs(serverConfig));
            return DEBUG;
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
