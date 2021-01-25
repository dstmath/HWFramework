package com.android.server;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.net.nsd.DnsSdTxtRecord;
import android.net.nsd.INsdManager;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Messenger;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Base64;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseIntArray;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.AsyncChannel;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.pm.DumpState;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

public class NsdService extends INsdManager.Stub {
    private static final boolean DBG = true;
    private static final int INVALID_ID = 0;
    private static final String MDNS_TAG = "mDnsConnector";
    private static final String TAG = "NsdService";
    private final HashMap<Messenger, ClientInfo> mClients = new HashMap<>();
    private final Context mContext;
    private final DaemonConnection mDaemon;
    private final NativeCallbackReceiver mDaemonCallback;
    private final SparseArray<ClientInfo> mIdToClientInfoMap = new SparseArray<>();
    private final NsdSettings mNsdSettings;
    private final NsdStateMachine mNsdStateMachine;
    private final AsyncChannel mReplyChannel = new AsyncChannel();
    private int mUniqueId = 1;

    interface DaemonConnectionSupplier {
        DaemonConnection get(NativeCallbackReceiver nativeCallbackReceiver);
    }

    /* access modifiers changed from: private */
    public class NsdStateMachine extends StateMachine {
        private final DefaultState mDefaultState = new DefaultState();
        private final DisabledState mDisabledState = new DisabledState();
        private final EnabledState mEnabledState = new EnabledState();

        /* access modifiers changed from: protected */
        public String getWhatToString(int what) {
            return NsdManager.nameOf(what);
        }

        private void registerForNsdSetting() {
            ContentObserver contentObserver = new ContentObserver(getHandler()) {
                /* class com.android.server.NsdService.NsdStateMachine.AnonymousClass1 */

                @Override // android.database.ContentObserver
                public void onChange(boolean selfChange) {
                    NsdService.this.notifyEnabled(NsdService.this.isNsdEnabled());
                }
            };
            NsdService.this.mNsdSettings.registerContentObserver(Settings.Global.getUriFor("nsd_on"), contentObserver);
        }

        NsdStateMachine(String name, Handler handler) {
            super(name, handler);
            addState(this.mDefaultState);
            addState(this.mDisabledState, this.mDefaultState);
            addState(this.mEnabledState, this.mDefaultState);
            setInitialState(NsdService.this.isNsdEnabled() ? this.mEnabledState : this.mDisabledState);
            setLogRecSize(25);
            registerForNsdSetting();
        }

        class DefaultState extends State {
            DefaultState() {
            }

            public boolean processMessage(Message msg) {
                switch (msg.what) {
                    case 69632:
                        if (msg.arg1 == 0) {
                            AsyncChannel c = (AsyncChannel) msg.obj;
                            Slog.i(NsdService.TAG, "New client listening to asynchronous messages");
                            c.sendMessage(69634);
                            NsdService.this.mClients.put(msg.replyTo, new ClientInfo(c, msg.replyTo));
                            return true;
                        }
                        Slog.e(NsdService.TAG, "Client connection failure, error=" + msg.arg1);
                        return true;
                    case 69633:
                        new AsyncChannel().connect(NsdService.this.mContext, NsdStateMachine.this.getHandler(), msg.replyTo);
                        return true;
                    case 69636:
                        int i = msg.arg1;
                        if (i == 2) {
                            Slog.e(NsdService.TAG, "Send failed, client connection lost");
                        } else if (i != 4) {
                            Slog.i(NsdService.TAG, "Client connection lost with reason: " + msg.arg1);
                        } else {
                            Slog.i(NsdService.TAG, "Client disconnected");
                        }
                        ClientInfo cInfo = (ClientInfo) NsdService.this.mClients.get(msg.replyTo);
                        if (cInfo != null) {
                            cInfo.expungeAllRequests();
                            NsdService.this.mClients.remove(msg.replyTo);
                        }
                        if (NsdService.this.mClients.size() != 0) {
                            return true;
                        }
                        NsdService.this.mDaemon.stop();
                        return true;
                    case 393217:
                        NsdService.this.replyToMessage(msg, 393219, 0);
                        return true;
                    case 393222:
                        NsdService.this.replyToMessage(msg, 393223, 0);
                        return true;
                    case 393225:
                        NsdService.this.replyToMessage(msg, 393226, 0);
                        return true;
                    case 393228:
                        NsdService.this.replyToMessage(msg, 393229, 0);
                        return true;
                    case 393234:
                        NsdService.this.replyToMessage(msg, 393235, 0);
                        return true;
                    default:
                        Slog.e(NsdService.TAG, "Unhandled " + msg);
                        return false;
                }
            }
        }

        /* access modifiers changed from: package-private */
        public class DisabledState extends State {
            DisabledState() {
            }

            public void enter() {
                NsdService.this.sendNsdStateChangeBroadcast(false);
            }

            public boolean processMessage(Message msg) {
                if (msg.what != 393240) {
                    return false;
                }
                NsdStateMachine nsdStateMachine = NsdStateMachine.this;
                nsdStateMachine.transitionTo(nsdStateMachine.mEnabledState);
                return true;
            }
        }

        /* access modifiers changed from: package-private */
        public class EnabledState extends State {
            EnabledState() {
            }

            public void enter() {
                NsdService.this.sendNsdStateChangeBroadcast(true);
                if (NsdService.this.mClients.size() > 0) {
                    NsdService.this.mDaemon.start();
                }
            }

            public void exit() {
                if (NsdService.this.mClients.size() > 0) {
                    NsdService.this.mDaemon.stop();
                }
            }

            private boolean requestLimitReached(ClientInfo clientInfo) {
                if (clientInfo.mClientIds.size() < 10) {
                    return false;
                }
                Slog.i(NsdService.TAG, "Exceeded max outstanding requests " + clientInfo);
                return true;
            }

            private void storeRequestMap(int clientId, int globalId, ClientInfo clientInfo, int what) {
                clientInfo.mClientIds.put(clientId, globalId);
                clientInfo.mClientRequests.put(clientId, what);
                NsdService.this.mIdToClientInfoMap.put(globalId, clientInfo);
            }

            private void removeRequestMap(int clientId, int globalId, ClientInfo clientInfo) {
                clientInfo.mClientIds.delete(clientId);
                clientInfo.mClientRequests.delete(clientId);
                NsdService.this.mIdToClientInfoMap.remove(globalId);
            }

            public boolean processMessage(Message msg) {
                switch (msg.what) {
                    case 69632:
                        if (msg.arg1 == 0 && NsdService.this.mClients.size() == 0) {
                            NsdService.this.mDaemon.start();
                        }
                        return false;
                    case 69636:
                        return false;
                    case 393217:
                        Slog.i(NsdService.TAG, "Discover services");
                        NsdServiceInfo servInfo = (NsdServiceInfo) msg.obj;
                        ClientInfo clientInfo = (ClientInfo) NsdService.this.mClients.get(msg.replyTo);
                        if (clientInfo == null) {
                            NsdService.this.replyToMessage(msg, 393219, 0);
                            return true;
                        } else if (requestLimitReached(clientInfo)) {
                            NsdService.this.replyToMessage(msg, 393219, 4);
                            return true;
                        } else {
                            int id = NsdService.this.getUniqueId();
                            if (NsdService.this.discoverServices(id, servInfo.getServiceType())) {
                                Slog.i(NsdService.TAG, "Discover " + msg.arg2 + " " + id + servInfo.getServiceType());
                                storeRequestMap(msg.arg2, id, clientInfo, msg.what);
                                NsdService.this.replyToMessage(msg, 393218, servInfo);
                                return true;
                            }
                            NsdService.this.stopServiceDiscovery(id);
                            NsdService.this.replyToMessage(msg, 393219, 0);
                            return true;
                        }
                    case 393222:
                        Slog.i(NsdService.TAG, "Stop service discovery");
                        ClientInfo clientInfo2 = (ClientInfo) NsdService.this.mClients.get(msg.replyTo);
                        try {
                            int id2 = clientInfo2.mClientIds.get(msg.arg2);
                            removeRequestMap(msg.arg2, id2, clientInfo2);
                            if (NsdService.this.stopServiceDiscovery(id2)) {
                                NsdService.this.replyToMessage(msg, 393224);
                                return true;
                            }
                            NsdService.this.replyToMessage(msg, 393223, 0);
                            return true;
                        } catch (NullPointerException e) {
                            NsdService.this.replyToMessage(msg, 393223, 0);
                            return true;
                        }
                    case 393225:
                        Slog.i(NsdService.TAG, "Register service");
                        ClientInfo clientInfo3 = (ClientInfo) NsdService.this.mClients.get(msg.replyTo);
                        if (requestLimitReached(clientInfo3)) {
                            NsdService.this.replyToMessage(msg, 393226, 4);
                            return true;
                        }
                        int id3 = NsdService.this.getUniqueId();
                        if (NsdService.this.registerService(id3, (NsdServiceInfo) msg.obj)) {
                            Slog.i(NsdService.TAG, "Register " + msg.arg2 + " " + id3);
                            storeRequestMap(msg.arg2, id3, clientInfo3, msg.what);
                            return true;
                        }
                        NsdService.this.unregisterService(id3);
                        NsdService.this.replyToMessage(msg, 393226, 0);
                        return true;
                    case 393228:
                        Slog.i(NsdService.TAG, "unregister service");
                        ClientInfo clientInfo4 = (ClientInfo) NsdService.this.mClients.get(msg.replyTo);
                        try {
                            int id4 = clientInfo4.mClientIds.get(msg.arg2);
                            removeRequestMap(msg.arg2, id4, clientInfo4);
                            if (NsdService.this.unregisterService(id4)) {
                                NsdService.this.replyToMessage(msg, 393230);
                                return true;
                            }
                            NsdService.this.replyToMessage(msg, 393229, 0);
                            return true;
                        } catch (NullPointerException e2) {
                            NsdService.this.replyToMessage(msg, 393229, 0);
                            return true;
                        }
                    case 393234:
                        Slog.i(NsdService.TAG, "Resolve service");
                        NsdServiceInfo servInfo2 = (NsdServiceInfo) msg.obj;
                        ClientInfo clientInfo5 = (ClientInfo) NsdService.this.mClients.get(msg.replyTo);
                        if (clientInfo5.mResolvedService != null) {
                            NsdService.this.replyToMessage(msg, 393235, 3);
                            return true;
                        }
                        int id5 = NsdService.this.getUniqueId();
                        if (NsdService.this.resolveService(id5, servInfo2)) {
                            clientInfo5.mResolvedService = new NsdServiceInfo();
                            storeRequestMap(msg.arg2, id5, clientInfo5, msg.what);
                            return true;
                        }
                        NsdService.this.replyToMessage(msg, 393235, 0);
                        return true;
                    case 393241:
                        NsdStateMachine nsdStateMachine = NsdStateMachine.this;
                        nsdStateMachine.transitionTo(nsdStateMachine.mDisabledState);
                        return true;
                    case 393242:
                        NativeEvent event = (NativeEvent) msg.obj;
                        if (!handleNativeEvent(event.code, event.raw, event.cooked)) {
                            return false;
                        }
                        return true;
                    default:
                        return false;
                }
            }

            private boolean handleNativeEvent(int code, String raw, String[] cooked) {
                int id = Integer.parseInt(cooked[1]);
                ClientInfo clientInfo = (ClientInfo) NsdService.this.mIdToClientInfoMap.get(id);
                if (clientInfo == null) {
                    Slog.e(NsdService.TAG, String.format("id %d for %s has no client mapping", Integer.valueOf(id), NativeResponseCode.nameOf(code)));
                    return false;
                }
                int clientId = clientInfo.getClientId(id);
                if (clientId < 0) {
                    Slog.i(NsdService.TAG, String.format("Notification %s for listener id %d that is no longer active", NativeResponseCode.nameOf(code), Integer.valueOf(id)));
                    return false;
                }
                Slog.i(NsdService.TAG, String.format("Native daemon message %s: %s", NativeResponseCode.nameOf(code), raw));
                switch (code) {
                    case NativeResponseCode.SERVICE_DISCOVERY_FAILED /* 602 */:
                        clientInfo.mChannel.sendMessage(393219, 0, clientId);
                        break;
                    case NativeResponseCode.SERVICE_FOUND /* 603 */:
                        clientInfo.mChannel.sendMessage(393220, 0, clientId, new NsdServiceInfo(cooked[2], cooked[3]));
                        break;
                    case NativeResponseCode.SERVICE_LOST /* 604 */:
                        clientInfo.mChannel.sendMessage(393221, 0, clientId, new NsdServiceInfo(cooked[2], cooked[3]));
                        break;
                    case NativeResponseCode.SERVICE_REGISTRATION_FAILED /* 605 */:
                        clientInfo.mChannel.sendMessage(393226, 0, clientId);
                        break;
                    case NativeResponseCode.SERVICE_REGISTERED /* 606 */:
                        clientInfo.mChannel.sendMessage(393227, id, clientId, new NsdServiceInfo(cooked[2], null));
                        break;
                    case NativeResponseCode.SERVICE_RESOLUTION_FAILED /* 607 */:
                        NsdService.this.stopResolveService(id);
                        removeRequestMap(clientId, id, clientInfo);
                        clientInfo.mResolvedService = null;
                        clientInfo.mChannel.sendMessage(393235, 0, clientId);
                        break;
                    case NativeResponseCode.SERVICE_RESOLVED /* 608 */:
                        int index = 0;
                        while (index < cooked[2].length() && cooked[2].charAt(index) != '.') {
                            if (cooked[2].charAt(index) == '\\') {
                                index++;
                            }
                            index++;
                        }
                        if (index < cooked[2].length()) {
                            String name = cooked[2].substring(0, index);
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
                            } else {
                                storeRequestMap(clientId, id2, clientInfo, 393234);
                                break;
                            }
                        } else {
                            Slog.e(NsdService.TAG, "Invalid service found " + raw);
                            break;
                        }
                        break;
                    case NativeResponseCode.SERVICE_UPDATED /* 609 */:
                    case NativeResponseCode.SERVICE_UPDATE_FAILED /* 610 */:
                        break;
                    case NativeResponseCode.SERVICE_GET_ADDR_FAILED /* 611 */:
                        NsdService.this.stopGetAddrInfo(id);
                        removeRequestMap(clientId, id, clientInfo);
                        clientInfo.mResolvedService = null;
                        clientInfo.mChannel.sendMessage(393235, 0, clientId);
                        break;
                    case NativeResponseCode.SERVICE_GET_ADDR_SUCCESS /* 612 */:
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
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String unescape(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        int i = 0;
        while (true) {
            if (i >= s.length()) {
                break;
            }
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
                    c = (char) (((c - '0') * 100) + ((s.charAt(i + 1) - '0') * 10) + (s.charAt(i + 2) - '0'));
                    i += 2;
                }
            }
            sb.append(c);
            i++;
        }
        return sb.toString();
    }

    @VisibleForTesting
    NsdService(Context ctx, NsdSettings settings, Handler handler, DaemonConnectionSupplier fn) {
        this.mContext = ctx;
        this.mNsdSettings = settings;
        this.mNsdStateMachine = new NsdStateMachine(TAG, handler);
        this.mNsdStateMachine.start();
        this.mDaemonCallback = new NativeCallbackReceiver();
        this.mDaemon = fn.get(this.mDaemonCallback);
    }

    public static NsdService create(Context context) throws InterruptedException {
        NsdSettings settings = NsdSettings.makeDefault(context);
        HandlerThread thread = new HandlerThread(TAG);
        thread.start();
        NsdService service = new NsdService(context, settings, new Handler(thread.getLooper()), $$Lambda$1xUIIN0BU8izGcnYWTVzczLBFU.INSTANCE);
        service.mDaemonCallback.awaitConnection();
        return service;
    }

    public Messenger getMessenger() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.INTERNET", TAG);
        return new Messenger(this.mNsdStateMachine.getHandler());
    }

    public void setEnabled(boolean isEnabled) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        this.mNsdSettings.putEnabledStatus(isEnabled);
        notifyEnabled(isEnabled);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyEnabled(boolean isEnabled) {
        this.mNsdStateMachine.sendMessage(isEnabled ? 393240 : 393241);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendNsdStateChangeBroadcast(boolean isEnabled) {
        Intent intent = new Intent("android.net.nsd.STATE_CHANGED");
        intent.addFlags(DumpState.DUMP_HANDLE);
        intent.putExtra("nsd_state", isEnabled ? 2 : 1);
        this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isNsdEnabled() {
        boolean ret = this.mNsdSettings.isEnabled();
        StringBuilder sb = new StringBuilder();
        sb.append("Network service discovery is ");
        sb.append(ret ? "enabled" : "disabled");
        Slog.i(TAG, sb.toString());
        return ret;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getUniqueId() {
        int i = this.mUniqueId + 1;
        this.mUniqueId = i;
        if (i != 0) {
            return this.mUniqueId;
        }
        int i2 = this.mUniqueId + 1;
        this.mUniqueId = i2;
        return i2;
    }

    /* access modifiers changed from: package-private */
    public static final class NativeResponseCode {
        private static final SparseArray<String> CODE_NAMES = new SparseArray<>();
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
            String name = CODE_NAMES.get(code);
            if (name == null) {
                return Integer.toString(code);
            }
            return name;
        }
    }

    private class NativeEvent {
        final int code;
        final String[] cooked;
        final String raw;

        NativeEvent(int code2, String raw2, String[] cooked2) {
            this.code = code2;
            this.raw = raw2;
            this.cooked = cooked2;
        }
    }

    class NativeCallbackReceiver implements INativeDaemonConnectorCallbacks {
        private final CountDownLatch connected = new CountDownLatch(1);

        NativeCallbackReceiver() {
        }

        public void awaitConnection() throws InterruptedException {
            this.connected.await();
        }

        @Override // com.android.server.INativeDaemonConnectorCallbacks
        public void onDaemonConnected() {
            this.connected.countDown();
        }

        @Override // com.android.server.INativeDaemonConnectorCallbacks
        public boolean onCheckHoldWakeLock(int code) {
            return false;
        }

        @Override // com.android.server.INativeDaemonConnectorCallbacks
        public boolean onEvent(int code, String raw, String[] cooked) {
            NsdService.this.mNsdStateMachine.sendMessage(393242, new NativeEvent(code, raw, cooked));
            return true;
        }
    }

    @VisibleForTesting
    public static class DaemonConnection {
        final NativeDaemonConnector mNativeConnector;

        DaemonConnection(NativeCallbackReceiver callback) {
            this.mNativeConnector = new NativeDaemonConnector(callback, "mdns", 10, NsdService.MDNS_TAG, 25, null);
            new Thread(this.mNativeConnector, NsdService.MDNS_TAG).start();
        }

        public boolean execute(Object... args) {
            Slog.i(NsdService.TAG, "mdnssd " + Arrays.toString(args));
            try {
                this.mNativeConnector.execute("mdnssd", args);
                return true;
            } catch (NativeDaemonConnectorException e) {
                Slog.e(NsdService.TAG, "Failed to execute mdnssd " + Arrays.toString(args), e);
                return false;
            }
        }

        public void start() {
            execute("start-service");
        }

        public void stop() {
            execute("stop-service");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean registerService(int regId, NsdServiceInfo service) {
        Slog.i(TAG, "registerService: " + regId + " " + service);
        String name = service.getServiceName();
        String type = service.getServiceType();
        int port = service.getPort();
        return this.mDaemon.execute("register", Integer.valueOf(regId), name, type, Integer.valueOf(port), Base64.encodeToString(service.getTxtRecord(), 0).replace("\n", ""));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean unregisterService(int regId) {
        return this.mDaemon.execute("stop-register", Integer.valueOf(regId));
    }

    private boolean updateService(int regId, DnsSdTxtRecord t) {
        if (t == null) {
            return false;
        }
        return this.mDaemon.execute("update", Integer.valueOf(regId), Integer.valueOf(t.size()), t.getRawData());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean discoverServices(int discoveryId, String serviceType) {
        return this.mDaemon.execute("discover", Integer.valueOf(discoveryId), serviceType);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean stopServiceDiscovery(int discoveryId) {
        return this.mDaemon.execute("stop-discover", Integer.valueOf(discoveryId));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean resolveService(int resolveId, NsdServiceInfo service) {
        return this.mDaemon.execute("resolve", Integer.valueOf(resolveId), service.getServiceName(), service.getServiceType(), "local.");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean stopResolveService(int resolveId) {
        return this.mDaemon.execute("stop-resolve", Integer.valueOf(resolveId));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean getAddrInfo(int resolveId, String hostname) {
        return this.mDaemon.execute("getaddrinfo", Integer.valueOf(resolveId), hostname);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean stopGetAddrInfo(int resolveId) {
        return this.mDaemon.execute("stop-getaddrinfo", Integer.valueOf(resolveId));
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void replyToMessage(Message msg, int what) {
        if (msg.replyTo != null) {
            Message dstMsg = obtainMessage(msg);
            dstMsg.what = what;
            this.mReplyChannel.replyToMessage(msg, dstMsg);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void replyToMessage(Message msg, int what, int arg1) {
        if (msg.replyTo != null) {
            Message dstMsg = obtainMessage(msg);
            dstMsg.what = what;
            dstMsg.arg1 = arg1;
            this.mReplyChannel.replyToMessage(msg, dstMsg);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void replyToMessage(Message msg, int what, Object obj) {
        if (msg.replyTo != null) {
            Message dstMsg = obtainMessage(msg);
            dstMsg.what = what;
            dstMsg.obj = obj;
            this.mReplyChannel.replyToMessage(msg, dstMsg);
        }
    }

    /* access modifiers changed from: private */
    public class ClientInfo {
        private static final int MAX_LIMIT = 10;
        private final AsyncChannel mChannel;
        private final SparseIntArray mClientIds;
        private final SparseIntArray mClientRequests;
        private final Messenger mMessenger;
        private NsdServiceInfo mResolvedService;

        private ClientInfo(AsyncChannel c, Messenger m) {
            this.mClientIds = new SparseIntArray();
            this.mClientRequests = new SparseIntArray();
            this.mChannel = c;
            this.mMessenger = m;
            Slog.i(NsdService.TAG, "New client, channel: " + c + " messenger: " + m);
        }

        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("mChannel ");
            sb.append(this.mChannel);
            sb.append("\n");
            sb.append("mMessenger ");
            sb.append(this.mMessenger);
            sb.append("\n");
            sb.append("mResolvedService ");
            sb.append(this.mResolvedService);
            sb.append("\n");
            for (int i = 0; i < this.mClientIds.size(); i++) {
                int clientID = this.mClientIds.keyAt(i);
                sb.append("clientId ");
                sb.append(clientID);
                sb.append(" mDnsId ");
                sb.append(this.mClientIds.valueAt(i));
                sb.append(" type ");
                sb.append(this.mClientRequests.get(clientID));
                sb.append("\n");
            }
            return sb.toString();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void expungeAllRequests() {
            for (int i = 0; i < this.mClientIds.size(); i++) {
                int clientId = this.mClientIds.keyAt(i);
                int globalId = this.mClientIds.valueAt(i);
                NsdService.this.mIdToClientInfoMap.remove(globalId);
                Slog.i(NsdService.TAG, "Terminating client-ID " + clientId + " global-ID " + globalId + " type " + this.mClientRequests.get(clientId));
                int i2 = this.mClientRequests.get(clientId);
                if (i2 == 393217) {
                    NsdService.this.stopServiceDiscovery(globalId);
                } else if (i2 == 393225) {
                    NsdService.this.unregisterService(globalId);
                } else if (i2 == 393234) {
                    NsdService.this.stopResolveService(globalId);
                }
            }
            this.mClientIds.clear();
            this.mClientRequests.clear();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private int getClientId(int globalId) {
            int idx = this.mClientIds.indexOfValue(globalId);
            if (idx < 0) {
                return idx;
            }
            return this.mClientIds.keyAt(idx);
        }
    }

    @VisibleForTesting
    public interface NsdSettings {
        boolean isEnabled();

        void putEnabledStatus(boolean z);

        void registerContentObserver(Uri uri, ContentObserver contentObserver);

        static NsdSettings makeDefault(Context context) {
            final ContentResolver resolver = context.getContentResolver();
            return new NsdSettings() {
                /* class com.android.server.NsdService.NsdSettings.AnonymousClass1 */

                @Override // com.android.server.NsdService.NsdSettings
                public boolean isEnabled() {
                    return Settings.Global.getInt(resolver, "nsd_on", 1) == 1;
                }

                @Override // com.android.server.NsdService.NsdSettings
                public void putEnabledStatus(boolean isEnabled) {
                    Settings.Global.putInt(resolver, "nsd_on", isEnabled ? 1 : 0);
                }

                @Override // com.android.server.NsdService.NsdSettings
                public void registerContentObserver(Uri uri, ContentObserver observer) {
                    resolver.registerContentObserver(uri, false, observer);
                }
            };
        }
    }
}
