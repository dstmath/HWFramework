package com.android.server;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.nsd.DnsSdTxtRecord;
import android.net.nsd.INsdManager.Stub;
import android.net.nsd.NsdServiceInfo;
import android.os.Binder;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.util.Base64;
import android.util.Slog;
import android.util.SparseArray;
import com.android.internal.util.AsyncChannel;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.NativeDaemonConnector.Command;
import com.android.server.wm.WindowState;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

public class NsdService extends Stub {
    private static final int BASE = 393216;
    private static final int CMD_TO_STRING_COUNT = 19;
    private static final boolean DBG = false;
    private static final String MDNS_TAG = "mDnsConnector";
    private static final String TAG = "NsdService";
    private static String[] sCmdToString;
    private int INVALID_ID;
    private HashMap<Messenger, ClientInfo> mClients;
    private ContentResolver mContentResolver;
    private Context mContext;
    private SparseArray<ClientInfo> mIdToClientInfoMap;
    private NativeDaemonConnector mNativeConnector;
    private final CountDownLatch mNativeDaemonConnected;
    private NsdStateMachine mNsdStateMachine;
    private AsyncChannel mReplyChannel;
    private int mUniqueId;

    private class ClientInfo {
        private static final int MAX_LIMIT = 10;
        private final AsyncChannel mChannel;
        private SparseArray<Integer> mClientIds;
        private SparseArray<Integer> mClientRequests;
        private final Messenger mMessenger;
        private NsdServiceInfo mResolvedService;

        private ClientInfo(AsyncChannel c, Messenger m) {
            this.mClientIds = new SparseArray();
            this.mClientRequests = new SparseArray();
            this.mChannel = c;
            this.mMessenger = m;
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
            return NsdService.DBG;
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

    class NativeResponseCode {
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
    }

    private class NsdStateMachine extends StateMachine {
        private final DefaultState mDefaultState;
        private final DisabledState mDisabledState;
        private final EnabledState mEnabledState;

        /* renamed from: com.android.server.NsdService.NsdStateMachine.1 */
        class AnonymousClass1 extends ContentObserver {
            AnonymousClass1(Handler $anonymous0) {
                super($anonymous0);
            }

            public void onChange(boolean selfChange) {
                if (NsdService.this.isNsdEnabled()) {
                    NsdService.this.mNsdStateMachine.sendMessage(393240);
                } else {
                    NsdService.this.mNsdStateMachine.sendMessage(393241);
                }
            }
        }

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
                        c.sendMessage(69634);
                        NsdService.this.mClients.put(msg.replyTo, new ClientInfo(c, msg.replyTo, null));
                        break;
                    case 69633:
                        new AsyncChannel().connect(NsdService.this.mContext, NsdStateMachine.this.getHandler(), msg.replyTo);
                        break;
                    case 69636:
                        switch (msg.arg1) {
                            case WindowState.LOW_RESOLUTION_COMPOSITION_ON /*2*/:
                                Slog.e(NsdService.TAG, "Send failed, client connection lost");
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
                        return NsdService.DBG;
                }
                return true;
            }
        }

        class DisabledState extends State {
            DisabledState() {
            }

            public void enter() {
                NsdService.this.sendNsdStateChangeBroadcast(NsdService.DBG);
            }

            public boolean processMessage(Message msg) {
                switch (msg.what) {
                    case 393240:
                        NsdStateMachine.this.transitionTo(NsdStateMachine.this.mEnabledState);
                        return true;
                    default:
                        return NsdService.DBG;
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
                if (clientInfo.mClientIds.size() >= 10) {
                    return true;
                }
                return NsdService.DBG;
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
                        return NsdService.DBG;
                    case 69636:
                        return NsdService.DBG;
                    case 393217:
                        servInfo = msg.obj;
                        clientInfo = (ClientInfo) NsdService.this.mClients.get(msg.replyTo);
                        if (requestLimitReached(clientInfo)) {
                            NsdService.this.replyToMessage(msg, 393219, 4);
                            return true;
                        }
                        id = NsdService.this.getUniqueId();
                        if (NsdService.this.discoverServices(id, servInfo.getServiceType())) {
                            storeRequestMap(msg.arg2, id, clientInfo, msg.what);
                            NsdService.this.replyToMessage(msg, 393218, (Object) servInfo);
                            return true;
                        }
                        NsdService.this.stopServiceDiscovery(id);
                        NsdService.this.replyToMessage(msg, 393219, 0);
                        return true;
                    case 393222:
                        clientInfo = (ClientInfo) NsdService.this.mClients.get(msg.replyTo);
                        try {
                            id = ((Integer) clientInfo.mClientIds.get(msg.arg2)).intValue();
                            removeRequestMap(msg.arg2, id, clientInfo);
                            if (NsdService.this.stopServiceDiscovery(id)) {
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
                        clientInfo = (ClientInfo) NsdService.this.mClients.get(msg.replyTo);
                        if (requestLimitReached(clientInfo)) {
                            NsdService.this.replyToMessage(msg, 393226, 4);
                            return true;
                        }
                        id = NsdService.this.getUniqueId();
                        if (NsdService.this.registerService(id, (NsdServiceInfo) msg.obj)) {
                            storeRequestMap(msg.arg2, id, clientInfo, msg.what);
                            return true;
                        }
                        NsdService.this.unregisterService(id);
                        NsdService.this.replyToMessage(msg, 393226, 0);
                        return true;
                    case 393228:
                        clientInfo = (ClientInfo) NsdService.this.mClients.get(msg.replyTo);
                        try {
                            id = ((Integer) clientInfo.mClientIds.get(msg.arg2)).intValue();
                            removeRequestMap(msg.arg2, id, clientInfo);
                            if (NsdService.this.unregisterService(id)) {
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
                        servInfo = (NsdServiceInfo) msg.obj;
                        clientInfo = (ClientInfo) NsdService.this.mClients.get(msg.replyTo);
                        if (clientInfo.mResolvedService != null) {
                            NsdService.this.replyToMessage(msg, 393235, 3);
                            return true;
                        }
                        id = NsdService.this.getUniqueId();
                        if (NsdService.this.resolveService(id, servInfo)) {
                            clientInfo.mResolvedService = new NsdServiceInfo();
                            storeRequestMap(msg.arg2, id, clientInfo, msg.what);
                            return true;
                        }
                        NsdService.this.replyToMessage(msg, 393235, 0);
                        return true;
                    case 393241:
                        NsdStateMachine.this.transitionTo(NsdStateMachine.this.mDisabledState);
                        return true;
                    case 393242:
                        NativeEvent event = msg.obj;
                        if (handleNativeEvent(event.code, event.raw, event.cooked)) {
                            return true;
                        }
                        return NsdService.DBG;
                    default:
                        return NsdService.DBG;
                }
            }

            private boolean handleNativeEvent(int code, String raw, String[] cooked) {
                boolean handled = true;
                int id = Integer.parseInt(cooked[1]);
                ClientInfo clientInfo = (ClientInfo) NsdService.this.mIdToClientInfoMap.get(id);
                if (clientInfo == null) {
                    Slog.e(NsdService.TAG, "Unique id with no client mapping: " + id);
                    return NsdService.DBG;
                }
                int clientId = clientInfo.getClientId(id);
                if (clientId < 0) {
                    Slog.d(NsdService.TAG, "Notification for a listener that is no longer active: " + id);
                    return NsdService.DBG;
                }
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
                            }
                            storeRequestMap(clientId, id2, clientInfo, 393234);
                            break;
                        }
                        Slog.e(NsdService.TAG, "Invalid service found " + raw);
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
                        handled = NsdService.DBG;
                        break;
                }
                return handled;
            }
        }

        protected String getWhatToString(int what) {
            return NsdService.cmdToString(what);
        }

        private void registerForNsdSetting() {
            NsdService.this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("nsd_on"), NsdService.DBG, new AnonymousClass1(getHandler()));
        }

        NsdStateMachine(String name) {
            super(name);
            this.mDefaultState = new DefaultState();
            this.mDisabledState = new DisabledState();
            this.mEnabledState = new EnabledState();
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.NsdService.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.NsdService.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.NsdService.<clinit>():void");
    }

    private static String cmdToString(int cmd) {
        cmd -= BASE;
        if (cmd < 0 || cmd >= sCmdToString.length) {
            return null;
        }
        return sCmdToString[cmd];
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
        this.mClients = new HashMap();
        this.mIdToClientInfoMap = new SparseArray();
        this.mReplyChannel = new AsyncChannel();
        this.INVALID_ID = 0;
        this.mUniqueId = 1;
        this.mNativeDaemonConnected = new CountDownLatch(1);
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
        return Global.getInt(this.mContentResolver, "nsd_on", 1) == 1 ? true : DBG;
    }

    private int getUniqueId() {
        int i = this.mUniqueId + 1;
        this.mUniqueId = i;
        if (i != this.INVALID_ID) {
            return this.mUniqueId;
        }
        i = this.mUniqueId + 1;
        this.mUniqueId = i;
        return i;
    }

    private boolean startMDnsDaemon() {
        try {
            this.mNativeConnector.execute("mdnssd", "start-service");
            return true;
        } catch (NativeDaemonConnectorException e) {
            Slog.e(TAG, "Failed to start daemon" + e);
            return DBG;
        }
    }

    private boolean stopMDnsDaemon() {
        try {
            this.mNativeConnector.execute("mdnssd", "stop-service");
            return true;
        } catch (NativeDaemonConnectorException e) {
            Slog.e(TAG, "Failed to start daemon" + e);
            return DBG;
        }
    }

    private boolean registerService(int regId, NsdServiceInfo service) {
        try {
            this.mNativeConnector.execute(new Command("mdnssd", "register", Integer.valueOf(regId), service.getServiceName(), service.getServiceType(), Integer.valueOf(service.getPort()), Base64.encodeToString(service.getTxtRecord(), 0).replace("\n", "")));
            return true;
        } catch (NativeDaemonConnectorException e) {
            Slog.e(TAG, "Failed to execute registerService " + e);
            return DBG;
        }
    }

    private boolean unregisterService(int regId) {
        try {
            this.mNativeConnector.execute("mdnssd", "stop-register", Integer.valueOf(regId));
            return true;
        } catch (NativeDaemonConnectorException e) {
            Slog.e(TAG, "Failed to execute unregisterService " + e);
            return DBG;
        }
    }

    private boolean updateService(int regId, DnsSdTxtRecord t) {
        if (t == null) {
            return DBG;
        }
        try {
            this.mNativeConnector.execute("mdnssd", "update", Integer.valueOf(regId), Integer.valueOf(t.size()), t.getRawData());
            return true;
        } catch (NativeDaemonConnectorException e) {
            Slog.e(TAG, "Failed to updateServices " + e);
            return DBG;
        }
    }

    private boolean discoverServices(int discoveryId, String serviceType) {
        try {
            this.mNativeConnector.execute("mdnssd", "discover", Integer.valueOf(discoveryId), serviceType);
            return true;
        } catch (NativeDaemonConnectorException e) {
            Slog.e(TAG, "Failed to discoverServices " + e);
            return DBG;
        }
    }

    private boolean stopServiceDiscovery(int discoveryId) {
        try {
            this.mNativeConnector.execute("mdnssd", "stop-discover", Integer.valueOf(discoveryId));
            return true;
        } catch (NativeDaemonConnectorException e) {
            Slog.e(TAG, "Failed to stopServiceDiscovery " + e);
            return DBG;
        }
    }

    private boolean resolveService(int resolveId, NsdServiceInfo service) {
        try {
            this.mNativeConnector.execute("mdnssd", "resolve", Integer.valueOf(resolveId), service.getServiceName(), service.getServiceType(), "local.");
            return true;
        } catch (NativeDaemonConnectorException e) {
            Slog.e(TAG, "Failed to resolveService " + e);
            return DBG;
        }
    }

    private boolean stopResolveService(int resolveId) {
        try {
            this.mNativeConnector.execute("mdnssd", "stop-resolve", Integer.valueOf(resolveId));
            return true;
        } catch (NativeDaemonConnectorException e) {
            Slog.e(TAG, "Failed to stop resolve " + e);
            return DBG;
        }
    }

    private boolean getAddrInfo(int resolveId, String hostname) {
        try {
            this.mNativeConnector.execute("mdnssd", "getaddrinfo", Integer.valueOf(resolveId), hostname);
            return true;
        } catch (NativeDaemonConnectorException e) {
            Slog.e(TAG, "Failed to getAddrInfo " + e);
            return DBG;
        }
    }

    private boolean stopGetAddrInfo(int resolveId) {
        try {
            this.mNativeConnector.execute("mdnssd", "stop-getaddrinfo", Integer.valueOf(resolveId));
            return true;
        } catch (NativeDaemonConnectorException e) {
            Slog.e(TAG, "Failed to stopGetAddrInfo " + e);
            return DBG;
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
            pw.println("Permission Denial: can't dump ServiceDiscoverService from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
            return;
        }
        for (ClientInfo client : this.mClients.values()) {
            pw.println("Client Info");
            pw.println(client);
        }
        this.mNsdStateMachine.dump(fd, pw, args);
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
