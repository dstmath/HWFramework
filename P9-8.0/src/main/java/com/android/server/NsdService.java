package com.android.server;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.nsd.DnsSdTxtRecord;
import android.net.nsd.INsdManager.Stub;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Message;
import android.os.Messenger;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.util.Base64;
import android.util.Slog;
import android.util.SparseArray;
import com.android.internal.util.AsyncChannel;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.NativeDaemonConnector.Command;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

public class NsdService extends Stub {
    private static final boolean DBG = true;
    private static final int INVALID_ID = 0;
    private static final String MDNS_TAG = "mDnsConnector";
    private static final String TAG = "NsdService";
    private final HashMap<Messenger, ClientInfo> mClients = new HashMap();
    private final ContentResolver mContentResolver;
    private final Context mContext;
    private final SparseArray<ClientInfo> mIdToClientInfoMap = new SparseArray();
    private final NativeDaemonConnector mNativeConnector;
    private final CountDownLatch mNativeDaemonConnected = new CountDownLatch(1);
    private final NsdStateMachine mNsdStateMachine;
    private final AsyncChannel mReplyChannel = new AsyncChannel();
    private int mUniqueId = 1;

    private class ClientInfo {
        private static final int MAX_LIMIT = 10;
        private final AsyncChannel mChannel;
        private final SparseArray<Integer> mClientIds;
        private final SparseArray<Integer> mClientRequests;
        private final Messenger mMessenger;
        private NsdServiceInfo mResolvedService;

        /* synthetic */ ClientInfo(NsdService this$0, AsyncChannel c, Messenger m, ClientInfo -this3) {
            this(c, m);
        }

        private ClientInfo(AsyncChannel c, Messenger m) {
            this.mClientIds = new SparseArray();
            this.mClientRequests = new SparseArray();
            this.mChannel = c;
            this.mMessenger = m;
            Slog.d(NsdService.TAG, "New client, channel: " + c + " messenger: " + m);
        }

        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("mChannel ").append(this.mChannel).append("\n");
            sb.append("mMessenger ").append(this.mMessenger).append("\n");
            sb.append("mResolvedService ").append(this.mResolvedService).append("\n");
            for (int i = 0; i < this.mClientIds.size(); i++) {
                int clientID = this.mClientIds.keyAt(i);
                sb.append("clientId ").append(clientID).append(" mDnsId ").append(this.mClientIds.valueAt(i)).append(" type ").append(this.mClientRequests.get(clientID)).append("\n");
            }
            return sb.toString();
        }

        private void expungeAllRequests() {
            for (int i = 0; i < this.mClientIds.size(); i++) {
                int clientId = this.mClientIds.keyAt(i);
                int globalId = ((Integer) this.mClientIds.valueAt(i)).intValue();
                NsdService.this.mIdToClientInfoMap.remove(globalId);
                Slog.d(NsdService.TAG, "Terminating client-ID " + clientId + " global-ID " + globalId + " type " + this.mClientRequests.get(clientId));
                switch (((Integer) this.mClientRequests.get(clientId)).intValue()) {
                    case 393217:
                        NsdService.this.stopServiceDiscovery(globalId);
                        break;
                    case 393225:
                        NsdService.this.unregisterService(globalId);
                        break;
                    case 393234:
                        NsdService.this.stopResolveService(globalId);
                        break;
                    default:
                        break;
                }
            }
            this.mClientIds.clear();
            this.mClientRequests.clear();
        }

        private int getClientId(int globalId) {
            int nSize = this.mClientIds.size();
            for (int i = 0; i < nSize; i++) {
                if (globalId == ((Integer) this.mClientIds.valueAt(i)).intValue()) {
                    return this.mClientIds.keyAt(i);
                }
            }
            return -1;
        }
    }

    class NativeCallbackReceiver implements INativeDaemonConnectorCallbacks {
        NativeCallbackReceiver() {
        }

        public void onDaemonConnected() {
            NsdService.this.mNativeDaemonConnected.countDown();
        }

        public boolean onCheckHoldWakeLock(int code) {
            return false;
        }

        public boolean onEvent(int code, String raw, String[] cooked) {
            NsdService.this.mNsdStateMachine.sendMessage(393242, new NativeEvent(code, raw, cooked));
            return true;
        }
    }

    private class NativeEvent {
        final int code;
        final String[] cooked;
        final String raw;

        NativeEvent(int code, String raw, String[] cooked) {
            this.code = code;
            this.raw = raw;
            this.cooked = cooked;
        }
    }

    static final class NativeResponseCode {
        private static final SparseArray<String> CODE_NAMES = new SparseArray();
        public static final int SERVICE_DISCOVERY_FAILED = 602;
        public static final int SERVICE_FOUND = 603;
        public static final int SERVICE_GET_ADDR_FAILED = 611;
        public static final int SERVICE_GET_ADDR_SUCCESS = 612;
        public static final int SERVICE_LOST = 604;
        public static final int SERVICE_REGISTERED = 606;
        public static final int SERVICE_REGISTRATION_FAILED = 605;
        public static final int SERVICE_RESOLUTION_FAILED = 607;
        public static final int SERVICE_RESOLVED = 608;
        public static final int SERVICE_UPDATED = 609;
        public static final int SERVICE_UPDATE_FAILED = 610;

        NativeResponseCode() {
        }

        static {
            CODE_NAMES.put(SERVICE_DISCOVERY_FAILED, "SERVICE_DISCOVERY_FAILED");
            CODE_NAMES.put(SERVICE_FOUND, "SERVICE_FOUND");
            CODE_NAMES.put(SERVICE_LOST, "SERVICE_LOST");
            CODE_NAMES.put(SERVICE_REGISTRATION_FAILED, "SERVICE_REGISTRATION_FAILED");
            CODE_NAMES.put(SERVICE_REGISTERED, "SERVICE_REGISTERED");
            CODE_NAMES.put(SERVICE_RESOLUTION_FAILED, "SERVICE_RESOLUTION_FAILED");
            CODE_NAMES.put(SERVICE_RESOLVED, "SERVICE_RESOLVED");
            CODE_NAMES.put(SERVICE_UPDATED, "SERVICE_UPDATED");
            CODE_NAMES.put(SERVICE_UPDATE_FAILED, "SERVICE_UPDATE_FAILED");
            CODE_NAMES.put(SERVICE_GET_ADDR_FAILED, "SERVICE_GET_ADDR_FAILED");
            CODE_NAMES.put(SERVICE_GET_ADDR_SUCCESS, "SERVICE_GET_ADDR_SUCCESS");
        }

        static String nameOf(int code) {
            String name = (String) CODE_NAMES.get(code);
            if (name == null) {
                return Integer.toString(code);
            }
            return name;
        }
    }

    private class NsdStateMachine extends StateMachine {
        private final DefaultState mDefaultState = new DefaultState();
        private final DisabledState mDisabledState = new DisabledState();
        private final EnabledState mEnabledState = new EnabledState();

        class DefaultState extends State {
            DefaultState() {
            }

            public boolean processMessage(Message msg) {
                switch (msg.what) {
                    case 69632:
                        if (msg.arg1 != 0) {
                            Slog.e(NsdService.TAG, "Client connection failure, error=" + msg.arg1);
                            break;
                        }
                        AsyncChannel c = msg.obj;
                        Slog.d(NsdService.TAG, "New client listening to asynchronous messages");
                        c.sendMessage(69634);
                        NsdService.this.mClients.put(msg.replyTo, new ClientInfo(NsdService.this, c, msg.replyTo, null));
                        break;
                    case 69633:
                        new AsyncChannel().connect(NsdService.this.mContext, NsdStateMachine.this.getHandler(), msg.replyTo);
                        break;
                    case 69636:
                        switch (msg.arg1) {
                            case 2:
                                Slog.e(NsdService.TAG, "Send failed, client connection lost");
                                break;
                            case 4:
                                Slog.d(NsdService.TAG, "Client disconnected");
                                break;
                            default:
                                Slog.d(NsdService.TAG, "Client connection lost with reason: " + msg.arg1);
                                break;
                        }
                        ClientInfo cInfo = (ClientInfo) NsdService.this.mClients.get(msg.replyTo);
                        if (cInfo != null) {
                            cInfo.expungeAllRequests();
                            NsdService.this.mClients.remove(msg.replyTo);
                        }
                        if (NsdService.this.mClients.size() == 0) {
                            NsdService.this.stopMDnsDaemon();
                            break;
                        }
                        break;
                    case 393217:
                        NsdService.this.replyToMessage(msg, 393219, 0);
                        break;
                    case 393222:
                        NsdService.this.replyToMessage(msg, 393223, 0);
                        break;
                    case 393225:
                        NsdService.this.replyToMessage(msg, 393226, 0);
                        break;
                    case 393228:
                        NsdService.this.replyToMessage(msg, 393229, 0);
                        break;
                    case 393234:
                        NsdService.this.replyToMessage(msg, 393235, 0);
                        break;
                    default:
                        Slog.e(NsdService.TAG, "Unhandled " + msg);
                        return false;
                }
                return true;
            }
        }

        class DisabledState extends State {
            DisabledState() {
            }

            public void enter() {
                NsdService.this.sendNsdStateChangeBroadcast(false);
            }

            public boolean processMessage(Message msg) {
                switch (msg.what) {
                    case 393240:
                        NsdStateMachine.this.transitionTo(NsdStateMachine.this.mEnabledState);
                        return true;
                    default:
                        return false;
                }
            }
        }

        class EnabledState extends State {
            EnabledState() {
            }

            public void enter() {
                NsdService.this.sendNsdStateChangeBroadcast(true);
                if (NsdService.this.mClients.size() > 0) {
                    NsdService.this.startMDnsDaemon();
                }
            }

            public void exit() {
                if (NsdService.this.mClients.size() > 0) {
                    NsdService.this.stopMDnsDaemon();
                }
            }

            private boolean requestLimitReached(ClientInfo clientInfo) {
                if (clientInfo.mClientIds.size() < 10) {
                    return false;
                }
                Slog.d(NsdService.TAG, "Exceeded max outstanding requests " + clientInfo);
                return true;
            }

            private void storeRequestMap(int clientId, int globalId, ClientInfo clientInfo, int what) {
                clientInfo.mClientIds.put(clientId, Integer.valueOf(globalId));
                clientInfo.mClientRequests.put(clientId, Integer.valueOf(what));
                NsdService.this.mIdToClientInfoMap.put(globalId, clientInfo);
            }

            private void removeRequestMap(int clientId, int globalId, ClientInfo clientInfo) {
                clientInfo.mClientIds.remove(clientId);
                clientInfo.mClientRequests.remove(clientId);
                NsdService.this.mIdToClientInfoMap.remove(globalId);
            }

            public boolean processMessage(Message msg) {
                NsdServiceInfo servInfo;
                ClientInfo clientInfo;
                int id;
                switch (msg.what) {
                    case 69632:
                        if (msg.arg1 == 0 && NsdService.this.mClients.size() == 0) {
                            NsdService.this.startMDnsDaemon();
                        }
                        return false;
                    case 69636:
                        return false;
                    case 393217:
                        Slog.d(NsdService.TAG, "Discover services");
                        servInfo = msg.obj;
                        clientInfo = (ClientInfo) NsdService.this.mClients.get(msg.replyTo);
                        if (!requestLimitReached(clientInfo)) {
                            id = NsdService.this.getUniqueId();
                            if (!NsdService.this.discoverServices(id, servInfo.getServiceType())) {
                                NsdService.this.stopServiceDiscovery(id);
                                NsdService.this.replyToMessage(msg, 393219, 0);
                                break;
                            }
                            Slog.d(NsdService.TAG, "Discover " + msg.arg2 + " " + id + servInfo.getServiceType());
                            storeRequestMap(msg.arg2, id, clientInfo, msg.what);
                            NsdService.this.replyToMessage(msg, 393218, (Object) servInfo);
                            break;
                        }
                        NsdService.this.replyToMessage(msg, 393219, 4);
                        break;
                    case 393222:
                        Slog.d(NsdService.TAG, "Stop service discovery");
                        clientInfo = (ClientInfo) NsdService.this.mClients.get(msg.replyTo);
                        try {
                            id = ((Integer) clientInfo.mClientIds.get(msg.arg2)).intValue();
                            removeRequestMap(msg.arg2, id, clientInfo);
                            if (!NsdService.this.stopServiceDiscovery(id)) {
                                NsdService.this.replyToMessage(msg, 393223, 0);
                                break;
                            }
                            NsdService.this.replyToMessage(msg, 393224);
                            break;
                        } catch (NullPointerException e) {
                            NsdService.this.replyToMessage(msg, 393223, 0);
                            break;
                        }
                    case 393225:
                        Slog.d(NsdService.TAG, "Register service");
                        clientInfo = (ClientInfo) NsdService.this.mClients.get(msg.replyTo);
                        if (!requestLimitReached(clientInfo)) {
                            id = NsdService.this.getUniqueId();
                            if (!NsdService.this.registerService(id, (NsdServiceInfo) msg.obj)) {
                                NsdService.this.unregisterService(id);
                                NsdService.this.replyToMessage(msg, 393226, 0);
                                break;
                            }
                            Slog.d(NsdService.TAG, "Register " + msg.arg2 + " " + id);
                            storeRequestMap(msg.arg2, id, clientInfo, msg.what);
                            break;
                        }
                        NsdService.this.replyToMessage(msg, 393226, 4);
                        break;
                    case 393228:
                        Slog.d(NsdService.TAG, "unregister service");
                        clientInfo = (ClientInfo) NsdService.this.mClients.get(msg.replyTo);
                        try {
                            id = ((Integer) clientInfo.mClientIds.get(msg.arg2)).intValue();
                            removeRequestMap(msg.arg2, id, clientInfo);
                            if (!NsdService.this.unregisterService(id)) {
                                NsdService.this.replyToMessage(msg, 393229, 0);
                                break;
                            }
                            NsdService.this.replyToMessage(msg, 393230);
                            break;
                        } catch (NullPointerException e2) {
                            NsdService.this.replyToMessage(msg, 393229, 0);
                            break;
                        }
                    case 393234:
                        Slog.d(NsdService.TAG, "Resolve service");
                        servInfo = (NsdServiceInfo) msg.obj;
                        clientInfo = (ClientInfo) NsdService.this.mClients.get(msg.replyTo);
                        if (clientInfo.mResolvedService == null) {
                            id = NsdService.this.getUniqueId();
                            if (!NsdService.this.resolveService(id, servInfo)) {
                                NsdService.this.replyToMessage(msg, 393235, 0);
                                break;
                            }
                            clientInfo.mResolvedService = new NsdServiceInfo();
                            storeRequestMap(msg.arg2, id, clientInfo, msg.what);
                            break;
                        }
                        NsdService.this.replyToMessage(msg, 393235, 3);
                        break;
                    case 393241:
                        NsdStateMachine.this.transitionTo(NsdStateMachine.this.mDisabledState);
                        break;
                    case 393242:
                        NativeEvent event = msg.obj;
                        if (!handleNativeEvent(event.code, event.raw, event.cooked)) {
                            return false;
                        }
                        break;
                    default:
                        return false;
                }
                return true;
            }

            private boolean handleNativeEvent(int code, String raw, String[] cooked) {
                int id = Integer.parseInt(cooked[1]);
                ClientInfo clientInfo = (ClientInfo) NsdService.this.mIdToClientInfoMap.get(id);
                String name;
                if (clientInfo == null) {
                    name = NativeResponseCode.nameOf(code);
                    Slog.e(NsdService.TAG, String.format("id %d for %s has no client mapping", new Object[]{Integer.valueOf(id), name}));
                    return false;
                }
                int clientId = clientInfo.getClientId(id);
                if (clientId < 0) {
                    name = NativeResponseCode.nameOf(code);
                    Slog.d(NsdService.TAG, String.format("Notification %s for listener id %d that is no longer active", new Object[]{name, Integer.valueOf(id)}));
                    return false;
                }
                name = NativeResponseCode.nameOf(code);
                Slog.d(NsdService.TAG, String.format("Native daemon message %s: %s", new Object[]{name, raw}));
                switch (code) {
                    case NativeResponseCode.SERVICE_DISCOVERY_FAILED /*602*/:
                        clientInfo.mChannel.sendMessage(393219, 0, clientId);
                        break;
                    case NativeResponseCode.SERVICE_FOUND /*603*/:
                        clientInfo.mChannel.sendMessage(393220, 0, clientId, new NsdServiceInfo(cooked[2], cooked[3]));
                        break;
                    case NativeResponseCode.SERVICE_LOST /*604*/:
                        clientInfo.mChannel.sendMessage(393221, 0, clientId, new NsdServiceInfo(cooked[2], cooked[3]));
                        break;
                    case NativeResponseCode.SERVICE_REGISTRATION_FAILED /*605*/:
                        clientInfo.mChannel.sendMessage(393226, 0, clientId);
                        break;
                    case NativeResponseCode.SERVICE_REGISTERED /*606*/:
                        clientInfo.mChannel.sendMessage(393227, id, clientId, new NsdServiceInfo(cooked[2], null));
                        break;
                    case NativeResponseCode.SERVICE_RESOLUTION_FAILED /*607*/:
                        NsdService.this.stopResolveService(id);
                        removeRequestMap(clientId, id, clientInfo);
                        clientInfo.mResolvedService = null;
                        clientInfo.mChannel.sendMessage(393235, 0, clientId);
                        break;
                    case NativeResponseCode.SERVICE_RESOLVED /*608*/:
                        int index = 0;
                        while (index < cooked[2].length() && cooked[2].charAt(index) != '.') {
                            if (cooked[2].charAt(index) == '\\') {
                                index++;
                            }
                            index++;
                        }
                        if (index < cooked[2].length()) {
                            name = cooked[2].substring(0, index);
                            String type = cooked[2].substring(index).replace(".local.", "");
                            clientInfo.mResolvedService.setServiceName(NsdService.this.unescape(name));
                            clientInfo.mResolvedService.setServiceType(type);
                            clientInfo.mResolvedService.setPort(Integer.parseInt(cooked[4]));
                            clientInfo.mResolvedService.setTxtRecords(cooked[6]);
                            NsdService.this.stopResolveService(id);
                            removeRequestMap(clientId, id, clientInfo);
                            int id2 = NsdService.this.getUniqueId();
                            if (!NsdService.this.getAddrInfo(id2, cooked[3])) {
                                clientInfo.mChannel.sendMessage(393235, 0, clientId);
                                clientInfo.mResolvedService = null;
                                break;
                            }
                            storeRequestMap(clientId, id2, clientInfo, 393234);
                            break;
                        }
                        Slog.e(NsdService.TAG, "Invalid service found " + raw);
                        break;
                        break;
                    case NativeResponseCode.SERVICE_UPDATED /*609*/:
                    case NativeResponseCode.SERVICE_UPDATE_FAILED /*610*/:
                        break;
                    case NativeResponseCode.SERVICE_GET_ADDR_FAILED /*611*/:
                        NsdService.this.stopGetAddrInfo(id);
                        removeRequestMap(clientId, id, clientInfo);
                        clientInfo.mResolvedService = null;
                        clientInfo.mChannel.sendMessage(393235, 0, clientId);
                        break;
                    case NativeResponseCode.SERVICE_GET_ADDR_SUCCESS /*612*/:
                        try {
                            clientInfo.mResolvedService.setHost(InetAddress.getByName(cooked[4]));
                            clientInfo.mChannel.sendMessage(393236, 0, clientId, clientInfo.mResolvedService);
                        } catch (UnknownHostException e) {
                            clientInfo.mChannel.sendMessage(393235, 0, clientId);
                        }
                        NsdService.this.stopGetAddrInfo(id);
                        removeRequestMap(clientId, id, clientInfo);
                        clientInfo.mResolvedService = null;
                        break;
                    default:
                        return false;
                }
                return true;
            }
        }

        protected String getWhatToString(int what) {
            return NsdManager.nameOf(what);
        }

        private void registerForNsdSetting() {
            NsdService.this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("nsd_on"), false, new ContentObserver(getHandler()) {
                public void onChange(boolean selfChange) {
                    if (NsdService.this.isNsdEnabled()) {
                        NsdService.this.mNsdStateMachine.sendMessage(393240);
                    } else {
                        NsdService.this.mNsdStateMachine.sendMessage(393241);
                    }
                }
            });
        }

        NsdStateMachine(String name) {
            super(name);
            addState(this.mDefaultState);
            addState(this.mDisabledState, this.mDefaultState);
            addState(this.mEnabledState, this.mDefaultState);
            if (NsdService.this.isNsdEnabled()) {
                setInitialState(this.mEnabledState);
            } else {
                setInitialState(this.mDisabledState);
            }
            setLogRecSize(25);
            registerForNsdSetting();
        }
    }

    private String unescape(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        int i = 0;
        while (i < s.length()) {
            char c = s.charAt(i);
            if (c == '\\') {
                i++;
                if (i >= s.length()) {
                    Slog.e(TAG, "Unexpected end of escape sequence in: " + s);
                    break;
                }
                c = s.charAt(i);
                if (!(c == '.' || c == '\\')) {
                    if (i + 2 >= s.length()) {
                        Slog.e(TAG, "Unexpected end of escape sequence in: " + s);
                        break;
                    }
                    c = (char) ((((c - 48) * 100) + ((s.charAt(i + 1) - 48) * 10)) + (s.charAt(i + 2) - 48));
                    i += 2;
                }
            }
            sb.append(c);
            i++;
        }
        return sb.toString();
    }

    private NsdService(Context context) {
        this.mContext = context;
        this.mContentResolver = context.getContentResolver();
        this.mNativeConnector = new NativeDaemonConnector(new NativeCallbackReceiver(), "mdns", 10, MDNS_TAG, 25, null);
        this.mNsdStateMachine = new NsdStateMachine(TAG);
        this.mNsdStateMachine.start();
        new Thread(this.mNativeConnector, MDNS_TAG).start();
    }

    public static NsdService create(Context context) throws InterruptedException {
        NsdService service = new NsdService(context);
        service.mNativeDaemonConnected.await();
        return service;
    }

    public Messenger getMessenger() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.INTERNET", TAG);
        return new Messenger(this.mNsdStateMachine.getHandler());
    }

    public void setEnabled(boolean enable) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        Global.putInt(this.mContentResolver, "nsd_on", enable ? 1 : 0);
        if (enable) {
            this.mNsdStateMachine.sendMessage(393240);
        } else {
            this.mNsdStateMachine.sendMessage(393241);
        }
    }

    private void sendNsdStateChangeBroadcast(boolean enabled) {
        Intent intent = new Intent("android.net.nsd.STATE_CHANGED");
        intent.addFlags(67108864);
        if (enabled) {
            intent.putExtra("nsd_state", 2);
        } else {
            intent.putExtra("nsd_state", 1);
        }
        this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    private boolean isNsdEnabled() {
        boolean ret = Global.getInt(this.mContentResolver, "nsd_on", 1) == 1;
        Slog.d(TAG, "Network service discovery enabled " + ret);
        return ret;
    }

    private int getUniqueId() {
        int i = this.mUniqueId + 1;
        this.mUniqueId = i;
        if (i != 0) {
            return this.mUniqueId;
        }
        i = this.mUniqueId + 1;
        this.mUniqueId = i;
        return i;
    }

    private boolean startMDnsDaemon() {
        Slog.d(TAG, "startMDnsDaemon");
        try {
            this.mNativeConnector.execute("mdnssd", "start-service");
            return true;
        } catch (NativeDaemonConnectorException e) {
            Slog.e(TAG, "Failed to start daemon" + e);
            return false;
        }
    }

    private boolean stopMDnsDaemon() {
        Slog.d(TAG, "stopMDnsDaemon");
        try {
            this.mNativeConnector.execute("mdnssd", "stop-service");
            return true;
        } catch (NativeDaemonConnectorException e) {
            Slog.e(TAG, "Failed to start daemon" + e);
            return false;
        }
    }

    private boolean registerService(int regId, NsdServiceInfo service) {
        Slog.d(TAG, "registerService: " + regId + " " + service);
        try {
            this.mNativeConnector.execute(new Command("mdnssd", "register", Integer.valueOf(regId), service.getServiceName(), service.getServiceType(), Integer.valueOf(service.getPort()), Base64.encodeToString(service.getTxtRecord(), 0).replace("\n", "")));
            return true;
        } catch (NativeDaemonConnectorException e) {
            Slog.e(TAG, "Failed to execute registerService " + e);
            return false;
        }
    }

    private boolean unregisterService(int regId) {
        Slog.d(TAG, "unregisterService: " + regId);
        try {
            this.mNativeConnector.execute("mdnssd", "stop-register", Integer.valueOf(regId));
            return true;
        } catch (NativeDaemonConnectorException e) {
            Slog.e(TAG, "Failed to execute unregisterService " + e);
            return false;
        }
    }

    private boolean updateService(int regId, DnsSdTxtRecord t) {
        Slog.d(TAG, "updateService: " + regId + " " + t);
        if (t == null) {
            return false;
        }
        try {
            this.mNativeConnector.execute("mdnssd", "update", Integer.valueOf(regId), Integer.valueOf(t.size()), t.getRawData());
            return true;
        } catch (NativeDaemonConnectorException e) {
            Slog.e(TAG, "Failed to updateServices " + e);
            return false;
        }
    }

    private boolean discoverServices(int discoveryId, String serviceType) {
        Slog.d(TAG, "discoverServices: " + discoveryId + " " + serviceType);
        try {
            this.mNativeConnector.execute("mdnssd", "discover", Integer.valueOf(discoveryId), serviceType);
            return true;
        } catch (NativeDaemonConnectorException e) {
            Slog.e(TAG, "Failed to discoverServices " + e);
            return false;
        }
    }

    private boolean stopServiceDiscovery(int discoveryId) {
        Slog.d(TAG, "stopServiceDiscovery: " + discoveryId);
        try {
            this.mNativeConnector.execute("mdnssd", "stop-discover", Integer.valueOf(discoveryId));
            return true;
        } catch (NativeDaemonConnectorException e) {
            Slog.e(TAG, "Failed to stopServiceDiscovery " + e);
            return false;
        }
    }

    private boolean resolveService(int resolveId, NsdServiceInfo service) {
        Slog.d(TAG, "resolveService: " + resolveId + " " + service);
        try {
            this.mNativeConnector.execute("mdnssd", "resolve", Integer.valueOf(resolveId), service.getServiceName(), service.getServiceType(), "local.");
            return true;
        } catch (NativeDaemonConnectorException e) {
            Slog.e(TAG, "Failed to resolveService " + e);
            return false;
        }
    }

    private boolean stopResolveService(int resolveId) {
        Slog.d(TAG, "stopResolveService: " + resolveId);
        try {
            this.mNativeConnector.execute("mdnssd", "stop-resolve", Integer.valueOf(resolveId));
            return true;
        } catch (NativeDaemonConnectorException e) {
            Slog.e(TAG, "Failed to stop resolve " + e);
            return false;
        }
    }

    private boolean getAddrInfo(int resolveId, String hostname) {
        Slog.d(TAG, "getAdddrInfo: " + resolveId);
        try {
            this.mNativeConnector.execute("mdnssd", "getaddrinfo", Integer.valueOf(resolveId), hostname);
            return true;
        } catch (NativeDaemonConnectorException e) {
            Slog.e(TAG, "Failed to getAddrInfo " + e);
            return false;
        }
    }

    private boolean stopGetAddrInfo(int resolveId) {
        Slog.d(TAG, "stopGetAdddrInfo: " + resolveId);
        try {
            this.mNativeConnector.execute("mdnssd", "stop-getaddrinfo", Integer.valueOf(resolveId));
            return true;
        } catch (NativeDaemonConnectorException e) {
            Slog.e(TAG, "Failed to stopGetAddrInfo " + e);
            return false;
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (DumpUtils.checkDumpPermission(this.mContext, TAG, pw)) {
            for (ClientInfo client : this.mClients.values()) {
                pw.println("Client Info");
                pw.println(client);
            }
            this.mNsdStateMachine.dump(fd, pw, args);
        }
    }

    private Message obtainMessage(Message srcMsg) {
        Message msg = Message.obtain();
        msg.arg2 = srcMsg.arg2;
        return msg;
    }

    private void replyToMessage(Message msg, int what) {
        if (msg.replyTo != null) {
            Message dstMsg = obtainMessage(msg);
            dstMsg.what = what;
            this.mReplyChannel.replyToMessage(msg, dstMsg);
        }
    }

    private void replyToMessage(Message msg, int what, int arg1) {
        if (msg.replyTo != null) {
            Message dstMsg = obtainMessage(msg);
            dstMsg.what = what;
            dstMsg.arg1 = arg1;
            this.mReplyChannel.replyToMessage(msg, dstMsg);
        }
    }

    private void replyToMessage(Message msg, int what, Object obj) {
        if (msg.replyTo != null) {
            Message dstMsg = obtainMessage(msg);
            dstMsg.what = what;
            dstMsg.obj = obj;
            this.mReplyChannel.replyToMessage(msg, dstMsg);
        }
    }
}
