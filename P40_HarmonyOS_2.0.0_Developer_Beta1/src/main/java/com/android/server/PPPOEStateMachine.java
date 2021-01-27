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
import android.util.wifi.HwHiLog;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.wm.HwActivityTaskManagerServiceEx;
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
    static final Set<String> STATE_CODE = new HashSet();
    private static final String TAG = "PPPOEStateMachine";
    private ConnectivityManager mCm;
    private ConnectedState mConnectedState = new ConnectedState();
    private final AtomicLong mConnectedTimeAtomicLong = new AtomicLong(0);
    private ConnectingState mConnectingState = new ConnectingState();
    private Context mContext;
    private CountDownLatch mCountDownLatch = new CountDownLatch(1);
    private RouteInfo mDefRoute = null;
    private DefaultState mDefaultState = new DefaultState();
    private DisconnectedState mDisconnectedState = new DisconnectedState();
    private DisconnectingState mDisconnectingState = new DisconnectingState();
    private InitialState mInitialState = new InitialState();
    private NativeDaemonConnector mNativeConnetor;
    private final AtomicInteger mPPPOEState = new AtomicInteger(PPPOEInfo.Status.OFFLINE.ordinal());
    private StartFailedState mStartFailedState = new StartFailedState();
    private StopFailedState mStopFailedState = new StopFailedState();
    private PowerManager.WakeLock mWakeLock;

    static {
        STATE_CODE.add("0");
        STATE_CODE.add("1");
        STATE_CODE.add("2");
        STATE_CODE.add("3");
        STATE_CODE.add("4");
        STATE_CODE.add("5");
        STATE_CODE.add("6");
        STATE_CODE.add("7");
        STATE_CODE.add(PHASE_RUNNING);
        STATE_CODE.add(PHASE_TERMINATE);
        STATE_CODE.add(PHASE_DISCONNECT);
        STATE_CODE.add(PHASE_HOLDOFF);
        STATE_CODE.add("12");
    }

    public class NativeDaemonConnectorCallbacks implements INativeDaemonConnectorCallbacks {
        public NativeDaemonConnectorCallbacks() {
        }

        public void onDaemonConnected() {
            HwHiLog.d(PPPOEStateMachine.TAG, false, "Start native daemon connector success.", new Object[0]);
            PPPOEStateMachine.this.mCountDownLatch.countDown();
        }

        public boolean onCheckHoldWakeLock(int code) {
            return false;
        }

        public boolean onEvent(int code, String raw, String[] cooked) {
            if (code == 652) {
                HwHiLog.d(PPPOEStateMachine.TAG, false, "onEvent receive native daemon connector event, code=%{public}d,raw=%{public}s", new Object[]{Integer.valueOf(code), raw});
                PPPOEStateMachine.this.sendMessage(151555, raw);
            }
            return true;
        }
    }

    public PPPOEStateMachine(Context context, String name) {
        super(name);
        HwHiLog.d(TAG, false, " create PPPOE state Machine", new Object[0]);
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
        this.mNativeConnetor = new NativeDaemonConnector(new NativeDaemonConnectorCallbacks(), "netd", 10, PPPOE_TAG, 25, (PowerManager.WakeLock) null);
    }

    public void start() {
        new Thread((Runnable) this.mNativeConnetor, PPPOE_TAG).start();
        try {
            this.mCountDownLatch.await();
        } catch (InterruptedException e) {
            HwHiLog.e(TAG, false, "start happened error", new Object[0]);
        }
        PPPOEStateMachine.super.start();
    }

    private void checkAndSetConnectivityInstance() {
        if (this.mCm == null) {
            this.mCm = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        }
    }

    private class DefaultState extends State {
        private DefaultState() {
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case 69632:
                    if (msg.arg1 == 0) {
                        HwHiLog.d(PPPOEStateMachine.TAG, false, "New client listening to asynchronous messages", new Object[0]);
                        return true;
                    }
                    HwHiLog.e(PPPOEStateMachine.TAG, false, "Client connection failure, error=%{public}d", new Object[]{Integer.valueOf(msg.arg1)});
                    return true;
                case 69633:
                case 151555:
                    return true;
                case 69636:
                    if (msg.arg1 == 2) {
                        HwHiLog.e(PPPOEStateMachine.TAG, false, "Send failed, client connection lost", new Object[0]);
                        return true;
                    }
                    HwHiLog.d(PPPOEStateMachine.TAG, false, "Client connection lost with reason: %{public}d", new Object[]{Integer.valueOf(msg.arg1)});
                    return true;
                case 589825:
                    HwHiLog.d(PPPOEStateMachine.TAG, false, "start PPPOE", new Object[0]);
                    if (PPPOEStateMachine.this.mPPPOEState.get() == PPPOEInfo.Status.OFFLINE.ordinal()) {
                        PPPOEStateMachine pPPOEStateMachine = PPPOEStateMachine.this;
                        pPPOEStateMachine.transitionTo(pPPOEStateMachine.mConnectingState);
                        return true;
                    }
                    HwHiLog.w(PPPOEStateMachine.TAG, false, "the pppoe is already online.", new Object[0]);
                    Intent completeIntent = new Intent("android.net.wifi.PPPOE_COMPLETED_ACTION");
                    completeIntent.addFlags(HwActivityTaskManagerServiceEx.APP_ATTRIBUTE_FORCE_NOT_ROTATE_WINDOW);
                    completeIntent.putExtra("pppoe_result_status", "ALREADY_ONLINE");
                    PPPOEStateMachine.this.mContext.sendStickyBroadcast(completeIntent);
                    return true;
                case 589826:
                    HwHiLog.d(PPPOEStateMachine.TAG, false, "stop PPPOE", new Object[0]);
                    if (PPPOEStateMachine.this.mPPPOEState.get() != PPPOEInfo.Status.ONLINE.ordinal()) {
                        return true;
                    }
                    PPPOEStateMachine pPPOEStateMachine2 = PPPOEStateMachine.this;
                    pPPOEStateMachine2.transitionTo(pPPOEStateMachine2.mDisconnectingState);
                    return true;
                default:
                    return false;
            }
        }
    }

    /* access modifiers changed from: private */
    public class InitialState extends State {
        private InitialState() {
        }

        public boolean processMessage(Message msg) {
            if (msg.what != 589825) {
                return false;
            }
            if (PPPOEStateMachine.this.mPPPOEState.get() == PPPOEInfo.Status.OFFLINE.ordinal()) {
                PPPOEStateMachine pPPOEStateMachine = PPPOEStateMachine.this;
                pPPOEStateMachine.transitionTo(pPPOEStateMachine.mConnectingState);
                return true;
            }
            Intent completeIntent = new Intent("android.net.wifi.PPPOE_COMPLETED_ACTION");
            completeIntent.addFlags(HwActivityTaskManagerServiceEx.APP_ATTRIBUTE_FORCE_NOT_ROTATE_WINDOW);
            completeIntent.putExtra("pppoe_result_status", "ALREADY_ONLINE");
            PPPOEStateMachine.this.mContext.sendStickyBroadcast(completeIntent);
            return true;
        }
    }

    /* access modifiers changed from: private */
    public class ConnectingState extends State {
        private ConnectingState() {
        }

        public void enter() {
            final Message message = Message.obtain();
            message.copyFrom(PPPOEStateMachine.this.getCurrentMessage());
            new Thread(new Runnable() {
                /* class com.android.server.PPPOEStateMachine.ConnectingState.AnonymousClass1 */

                @Override // java.lang.Runnable
                public void run() {
                    PPPOEStateMachine.this.mWakeLock.acquire();
                    PPPOEStateMachine.this.mPPPOEState.set(PPPOEInfo.Status.CONNECTING.ordinal());
                    Intent intent = new Intent("android.net.wifi.PPPOE_STATE_CHANGED");
                    intent.addFlags(HwActivityTaskManagerServiceEx.APP_ATTRIBUTE_FORCE_NOT_ROTATE_WINDOW);
                    intent.putExtra("pppoe_state", "PPPOE_STATE_CONNECTING");
                    PPPOEStateMachine.this.mContext.sendStickyBroadcast(intent);
                    HwHiLog.d(PPPOEStateMachine.TAG, false, "start PPPOE", new Object[0]);
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
                    PPPOEStateMachine pPPOEStateMachine = PPPOEStateMachine.this;
                    pPPOEStateMachine.transitionTo(pPPOEStateMachine.mConnectedState);
                } else {
                    PPPOEStateMachine.this.mStartFailedState.setErrorCode("FAILURE_INTERNAL_ERROR");
                    PPPOEStateMachine pPPOEStateMachine2 = PPPOEStateMachine.this;
                    pPPOEStateMachine2.transitionTo(pPPOEStateMachine2.mStartFailedState);
                }
            }
            if (PPPOEStateMachine.PHASE_DISCONNECT.equals(cooked[1])) {
                PPPOEStateMachine.this.mStartFailedState.setErrorCode("FAILURE_INTERNAL_ERROR");
                PPPOEStateMachine pPPOEStateMachine3 = PPPOEStateMachine.this;
                pPPOEStateMachine3.transitionTo(pPPOEStateMachine3.mStartFailedState);
            }
            if (PPPOEStateMachine.STATE_CODE.contains(cooked[1])) {
                return true;
            }
            PPPOEStateMachine.this.mStartFailedState.setErrorCode(cooked[1]);
            PPPOEStateMachine pPPOEStateMachine4 = PPPOEStateMachine.this;
            pPPOEStateMachine4.transitionTo(pPPOEStateMachine4.mStartFailedState);
            return true;
        }

        private boolean updateConfig() {
            INetworkManagementService netService = INetworkManagementService.Stub.asInterface(ServiceManager.getService("network_management"));
            ConnectivityManager connectivityManager = (ConnectivityManager) PPPOEStateMachine.this.mContext.getSystemService("connectivity");
            LinkProperties prop = connectivityManager.getActiveLinkProperties();
            Network wifiNetwork = connectivityManager.getNetworkForType(1);
            if (wifiNetwork == null) {
                HwHiLog.e(PPPOEStateMachine.TAG, false, "ConnectingState, updateConfig, No Network for type WIFI!", new Object[0]);
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
                        PPPOEStateMachine.this.mDefRoute = routeInfo;
                        break;
                    }
                }
                if (PPPOEStateMachine.this.mDefRoute != null) {
                    HwHiLog.d(PPPOEStateMachine.TAG, false, "Remove default route %{private}s", new Object[]{PPPOEStateMachine.this.mDefRoute.toString()});
                    try {
                        netService.removeRoute(wifiNetwork.netId, PPPOEStateMachine.this.mDefRoute);
                    } catch (Exception e) {
                        HwHiLog.e(PPPOEStateMachine.TAG, false, "Failed to remove route %{private}s", new Object[]{PPPOEStateMachine.this.mDefRoute.toString()});
                        return false;
                    }
                }
            }
            String interfaceName = SystemProperties.get("ppp.interface", "ppp0");
            RouteInfo route = new RouteInfo(new LinkAddress(NetworkUtils.numericToInetAddress(SystemProperties.get("ppp.InetAddress", "0.0.0.0")), 0), NetworkUtils.numericToInetAddress(SystemProperties.get("ppp.gateway", "0.0.0.0")), interfaceName);
            List<RouteInfo> routes = new ArrayList<>();
            routes.add(route);
            HwHiLog.d(PPPOEStateMachine.TAG, false, "add route %{private}s", new Object[]{route.toString()});
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
                String[] args = NetworkUtils.makeStrings(dnses);
                int length = args.length;
                int i = 0;
                while (i < length) {
                    HwHiLog.e(PPPOEStateMachine.TAG, false, "after set dns servers: %{public}s", new Object[]{args[i]});
                    i++;
                    length = length;
                    args = args;
                    prop = prop;
                }
                HwHiLog.d(PPPOEStateMachine.TAG, false, "set net.dns1 :%{private}s net.dns2: %{private}s", new Object[]{dns1, dns2});
                return true;
            } catch (Exception e2) {
                HwHiLog.e(PPPOEStateMachine.TAG, false, "Failed to add route %{private}s", new Object[]{route});
                return false;
            }
        }
    }

    /* access modifiers changed from: private */
    public class ConnectedState extends State {
        private ConnectedState() {
        }

        public void enter() {
            HwHiLog.d(PPPOEStateMachine.TAG, false, "success to connect pppoe.", new Object[0]);
            PPPOEStateMachine.this.mPPPOEState.set(PPPOEInfo.Status.ONLINE.ordinal());
            PPPOEStateMachine.this.mConnectedTimeAtomicLong.set(System.currentTimeMillis());
            Intent completeIntent = new Intent("android.net.wifi.PPPOE_COMPLETED_ACTION");
            completeIntent.addFlags(HwActivityTaskManagerServiceEx.APP_ATTRIBUTE_FORCE_NOT_ROTATE_WINDOW);
            completeIntent.putExtra("pppoe_result_status", "SUCCESS");
            PPPOEStateMachine.this.mContext.sendStickyBroadcast(completeIntent);
            Intent connectIntent = new Intent("android.net.wifi.PPPOE_STATE_CHANGED");
            connectIntent.addFlags(HwActivityTaskManagerServiceEx.APP_ATTRIBUTE_FORCE_NOT_ROTATE_WINDOW);
            connectIntent.putExtra("pppoe_state", "PPPOE_STATE_CONNECTED");
            PPPOEStateMachine.this.mContext.sendStickyBroadcast(connectIntent);
        }

        public boolean processMessage(Message msg) {
            int i = msg.what;
            if (i != 151555) {
                if (i != 589826) {
                    return false;
                }
                PPPOEStateMachine pPPOEStateMachine = PPPOEStateMachine.this;
                pPPOEStateMachine.transitionTo(pPPOEStateMachine.mDisconnectingState);
            } else if (PPPOEStateMachine.PHASE_DISCONNECT.equals(NativeDaemonEvent.unescapeArgs((String) msg.obj)[1])) {
                HwHiLog.w(PPPOEStateMachine.TAG, false, "lost pppoe service.", new Object[0]);
                PPPOEStateMachine pPPOEStateMachine2 = PPPOEStateMachine.this;
                pPPOEStateMachine2.transitionTo(pPPOEStateMachine2.mDisconnectingState);
            }
            return true;
        }
    }

    /* access modifiers changed from: private */
    public class StartFailedState extends State {
        private String errorCode;

        private StartFailedState() {
        }

        public void enter() {
            HwHiLog.e(PPPOEStateMachine.TAG, false, "Failed to start PPPOE, error code is %{public}s", new Object[]{this.errorCode});
            PPPOEStateMachine.this.stopPPPOE();
            PPPOEStateMachine.this.mPPPOEState.set(PPPOEInfo.Status.OFFLINE.ordinal());
            Intent intent = new Intent("android.net.wifi.PPPOE_COMPLETED_ACTION");
            intent.addFlags(HwActivityTaskManagerServiceEx.APP_ATTRIBUTE_FORCE_NOT_ROTATE_WINDOW);
            intent.putExtra("pppoe_result_status", "FAILURE");
            intent.putExtra("pppoe_result_error_code", this.errorCode);
            PPPOEStateMachine.this.mContext.sendStickyBroadcast(intent);
            PPPOEStateMachine pPPOEStateMachine = PPPOEStateMachine.this;
            pPPOEStateMachine.transitionTo(pPPOEStateMachine.mInitialState);
        }

        public void setErrorCode(String errorCode2) {
            this.errorCode = errorCode2;
        }
    }

    /* access modifiers changed from: private */
    public class DisconnectingState extends State {
        private DisconnectingState() {
        }

        public void enter() {
            Message.obtain().copyFrom(PPPOEStateMachine.this.getCurrentMessage());
            new Thread(new Runnable() {
                /* class com.android.server.PPPOEStateMachine.DisconnectingState.AnonymousClass1 */

                @Override // java.lang.Runnable
                public void run() {
                    PPPOEStateMachine.this.mWakeLock.acquire();
                    Intent intent = new Intent("android.net.wifi.PPPOE_STATE_CHANGED");
                    intent.addFlags(HwActivityTaskManagerServiceEx.APP_ATTRIBUTE_FORCE_NOT_ROTATE_WINDOW);
                    intent.putExtra("pppoe_state", "PPPOE_STATE_DISCONNECTING");
                    PPPOEStateMachine.this.mContext.sendStickyBroadcast(intent);
                    HwHiLog.d(PPPOEStateMachine.TAG, false, "stop PPPOE", new Object[0]);
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
                PPPOEStateMachine pPPOEStateMachine = PPPOEStateMachine.this;
                pPPOEStateMachine.transitionTo(pPPOEStateMachine.mDisconnectedState);
            }
            if (PPPOEStateMachine.STATE_CODE.contains(cooked[1])) {
                return true;
            }
            PPPOEStateMachine.this.mStopFailedState.setErrorCode(cooked[1]);
            PPPOEStateMachine pPPOEStateMachine2 = PPPOEStateMachine.this;
            pPPOEStateMachine2.transitionTo(pPPOEStateMachine2.mStopFailedState);
            return true;
        }
    }

    /* access modifiers changed from: private */
    public class DisconnectedState extends State {
        private DisconnectedState() {
        }

        public void enter() {
            updateConfig();
            PPPOEStateMachine.this.mConnectedTimeAtomicLong.set(0);
            PPPOEStateMachine.this.mPPPOEState.set(PPPOEInfo.Status.OFFLINE.ordinal());
            Intent connectIntent = new Intent("android.net.wifi.PPPOE_STATE_CHANGED");
            connectIntent.addFlags(HwActivityTaskManagerServiceEx.APP_ATTRIBUTE_FORCE_NOT_ROTATE_WINDOW);
            connectIntent.putExtra("pppoe_state", "PPPOE_STATE_DISCONNECTED");
            PPPOEStateMachine.this.mContext.sendStickyBroadcast(connectIntent);
        }

        private boolean updateConfig() {
            INetworkManagementService netService = INetworkManagementService.Stub.asInterface(ServiceManager.getService("network_management"));
            ConnectivityManager connectivityManager = (ConnectivityManager) PPPOEStateMachine.this.mContext.getSystemService("connectivity");
            LinkProperties prop = connectivityManager.getActiveLinkProperties();
            Network wifiNetwork = connectivityManager.getNetworkForType(1);
            if (wifiNetwork == null) {
                HwHiLog.e(PPPOEStateMachine.TAG, false, "DisconnectedState, updateConfig, No Network for type WiFi!", new Object[0]);
                return false;
            }
            if (prop != null) {
                prop.getDnsServers();
            }
            String interfaceName = SystemProperties.get("wifi.interface", "wlan0");
            if (PPPOEStateMachine.this.mDefRoute == null) {
                return false;
            }
            HwHiLog.d(PPPOEStateMachine.TAG, false, "Reset default route via %{public}s", new Object[]{interfaceName});
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
                HwHiLog.d(PPPOEStateMachine.TAG, false, "set net.dns1 :%{private}s net.dns2: %{private}s", new Object[]{dns1, dns2});
                return true;
            } catch (Exception e) {
                HwHiLog.e(PPPOEStateMachine.TAG, false, "Failed to add route %{private}s", new Object[]{PPPOEStateMachine.this.mDefRoute.toString()});
                return false;
            }
        }
    }

    /* access modifiers changed from: private */
    public class StopFailedState extends State {
        private String errorCode;

        private StopFailedState() {
        }

        public void enter() {
            PPPOEStateMachine.this.mConnectedTimeAtomicLong.set(0);
            PPPOEStateMachine.this.mPPPOEState.set(PPPOEInfo.Status.OFFLINE.ordinal());
            HwHiLog.e(PPPOEStateMachine.TAG, false, "Failed to stop PPPOE , error code is %{public}s", new Object[]{this.errorCode});
        }

        public void setErrorCode(String errorCode2) {
            this.errorCode = errorCode2;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean stopPPPOE() {
        HwHiLog.d(TAG, false, "execute stop PPPOE", new Object[0]);
        try {
            this.mNativeConnetor.execute("pppoed", new Object[]{"stop"});
            return true;
        } catch (Exception e) {
            HwHiLog.e(TAG, false, "Failed to stop PPPOE", new Object[0]);
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean startPPPOE(PPPOEConfig serverConfig) {
        HwHiLog.d(TAG, false, "startPPPOE", new Object[0]);
        try {
            this.mNativeConnetor.execute("pppoed", buildStartArgs(serverConfig));
            return true;
        } catch (Exception e) {
            HwHiLog.e(TAG, false, "Failed to start PPPOE", new Object[0]);
            return false;
        }
    }

    public PPPOEInfo getPPPOEInfo() {
        HwHiLog.d(TAG, false, "getPPPOEInfo", new Object[0]);
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
