package ohos.wifi.p2p;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ohos.eventhandler.EventHandler;
import ohos.eventhandler.EventRunner;
import ohos.eventhandler.InnerEvent;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;
import ohos.utils.Sequenceable;
import ohos.wifi.InnerUtils;

class WifiP2pMessenger extends RemoteObject implements IRemoteBroker {
    private static final int BASE = 139264;
    private static final int BASE_SYSTEM_ASYNC_CHANNEL = 69632;
    private static final int CANCEL_CONNECT_FAILED = 139275;
    private static final int CANCEL_CONNECT_SUCCEEDED = 139276;
    private static final int CMD_CHANNEL_DISCONNECTED = 69636;
    private static final int CONNECT_FAILED = 139272;
    private static final int CONNECT_SUCCEEDED = 139273;
    private static final int CREATE_GROUP_FAILED = 139278;
    private static final int CREATE_GROUP_SUCCEEDED = 139279;
    private static final int DELETE_PERSISTENT_GROUP_FAILED = 139319;
    private static final int DELETE_PERSISTENT_GROUP_SUCCEEDED = 139320;
    private static final int DISCOVER_PEERS_FAILED = 139266;
    private static final int DISCOVER_PEERS_SUCCEEDED = 139267;
    private static final HiLogLabel LABEL = new HiLogLabel(3, InnerUtils.LOG_ID_WIFI, "WifiP2pMessenger");
    private static final Object LOCK = new Object();
    private static final int MAX_PEERS_SIZE = 32;
    private static final int MAX_PERSIS_GROUP_SIZE = 2048;
    private static final int REMOVE_GROUP_FAILED = 139281;
    private static final int REMOVE_GROUP_SUCCEEDED = 139282;
    private static final int RESPONSE_CONNECTION_INFO = 139286;
    private static final int RESPONSE_DEVICE_INFO = 139362;
    private static final int RESPONSE_GROUP_INFO = 139288;
    private static final int RESPONSE_NETWORK_INFO = 139359;
    private static final int RESPONSE_PEERS = 139284;
    private static final int RESPONSE_PERSISTENT_GROUP_INFO = 139322;
    private static final int SET_DEVICE_NAME_FAILED = 139316;
    private static final int SET_DEVICE_NAME_SUCCEEDED = 139317;
    private static final int START_LISTEN_FAILED = 139330;
    private static final int START_LISTEN_SUCCEEDED = 139331;
    private static final int STOP_DISCOVERY_FAILED = 139269;
    private static final int STOP_DISCOVERY_SUCCEEDED = 139270;
    private static final int STOP_LISTEN_FAILED = 139333;
    private static final int STOP_LISTEN_SUCCEEDED = 139334;
    private Map<Integer, WifiP2pCallback> mCallbacks = new HashMap();
    private EventRunner mEventRunner;

    @Override // ohos.rpc.IRemoteBroker
    public IRemoteObject asObject() {
        return this;
    }

    public WifiP2pMessenger(EventRunner eventRunner) {
        super("ohos.wifi.p2p.WifiP2pMessenger");
        this.mEventRunner = eventRunner;
    }

    public void addCallback(int i, WifiP2pCallback wifiP2pCallback) {
        synchronized (LOCK) {
            if (this.mCallbacks == null) {
                this.mCallbacks = new HashMap();
            }
            this.mCallbacks.put(Integer.valueOf(i), wifiP2pCallback);
        }
    }

    /* access modifiers changed from: private */
    public class CallbackHandler extends EventHandler {
        public CallbackHandler(EventRunner eventRunner) {
            super(eventRunner);
        }

        public void processEvent(InnerEvent innerEvent) {
            WifiP2pCallback wifiP2pCallback;
            if (innerEvent != null) {
                synchronized (WifiP2pMessenger.LOCK) {
                    wifiP2pCallback = WifiP2pMessenger.this.getWifiP2pCallback(innerEvent);
                }
                Object obj = innerEvent.object;
                HiLog.info(WifiP2pMessenger.LABEL, "WifiP2pMessenger receive event: %{public}d", Integer.valueOf(innerEvent.eventId));
                switch (innerEvent.eventId) {
                    case WifiP2pMessenger.CMD_CHANNEL_DISCONNECTED /* 69636 */:
                        WifiP2pMessenger.this.handleChannelDisconnected(wifiP2pCallback, obj);
                        return;
                    case WifiP2pMessenger.DISCOVER_PEERS_FAILED /* 139266 */:
                    case WifiP2pMessenger.STOP_DISCOVERY_FAILED /* 139269 */:
                    case WifiP2pMessenger.CONNECT_FAILED /* 139272 */:
                    case WifiP2pMessenger.CANCEL_CONNECT_FAILED /* 139275 */:
                    case WifiP2pMessenger.CREATE_GROUP_FAILED /* 139278 */:
                    case WifiP2pMessenger.REMOVE_GROUP_FAILED /* 139281 */:
                    case WifiP2pMessenger.SET_DEVICE_NAME_FAILED /* 139316 */:
                    case WifiP2pMessenger.DELETE_PERSISTENT_GROUP_FAILED /* 139319 */:
                    case WifiP2pMessenger.START_LISTEN_FAILED /* 139330 */:
                    case WifiP2pMessenger.STOP_LISTEN_FAILED /* 139333 */:
                        WifiP2pMessenger.this.handleEventFailed(wifiP2pCallback, obj);
                        return;
                    case WifiP2pMessenger.DISCOVER_PEERS_SUCCEEDED /* 139267 */:
                    case WifiP2pMessenger.STOP_DISCOVERY_SUCCEEDED /* 139270 */:
                    case WifiP2pMessenger.CONNECT_SUCCEEDED /* 139273 */:
                    case WifiP2pMessenger.CANCEL_CONNECT_SUCCEEDED /* 139276 */:
                    case WifiP2pMessenger.CREATE_GROUP_SUCCEEDED /* 139279 */:
                    case WifiP2pMessenger.REMOVE_GROUP_SUCCEEDED /* 139282 */:
                    case WifiP2pMessenger.SET_DEVICE_NAME_SUCCEEDED /* 139317 */:
                    case WifiP2pMessenger.DELETE_PERSISTENT_GROUP_SUCCEEDED /* 139320 */:
                    case WifiP2pMessenger.START_LISTEN_SUCCEEDED /* 139331 */:
                    case WifiP2pMessenger.STOP_LISTEN_SUCCEEDED /* 139334 */:
                        if (wifiP2pCallback != null) {
                            wifiP2pCallback.eventExecOk();
                            return;
                        }
                        return;
                    case WifiP2pMessenger.RESPONSE_PEERS /* 139284 */:
                        WifiP2pMessenger.this.handleResponsePeers(wifiP2pCallback, obj);
                        return;
                    case WifiP2pMessenger.RESPONSE_CONNECTION_INFO /* 139286 */:
                        WifiP2pMessenger.this.handleConnectionInfo(wifiP2pCallback, obj);
                        return;
                    case WifiP2pMessenger.RESPONSE_GROUP_INFO /* 139288 */:
                        WifiP2pMessenger.this.handleGroupInfo(wifiP2pCallback, obj);
                        return;
                    case WifiP2pMessenger.RESPONSE_PERSISTENT_GROUP_INFO /* 139322 */:
                        WifiP2pMessenger.this.handlePersistentGroupInfo(wifiP2pCallback, obj);
                        return;
                    case WifiP2pMessenger.RESPONSE_NETWORK_INFO /* 139359 */:
                        WifiP2pMessenger.this.handleNetworkInfo(wifiP2pCallback, obj);
                        return;
                    case WifiP2pMessenger.RESPONSE_DEVICE_INFO /* 139362 */:
                        WifiP2pMessenger.this.handleDeviceInfo(wifiP2pCallback, obj);
                        return;
                    default:
                        HiLog.warn(WifiP2pMessenger.LABEL, "ignored event: %{public}d", Integer.valueOf(innerEvent.eventId));
                        return;
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private WifiP2pCallback getWifiP2pCallback(InnerEvent innerEvent) {
        Object obj = innerEvent.object;
        if (!(obj instanceof P2pEventObject)) {
            return null;
        }
        return this.mCallbacks.get(Integer.valueOf(((P2pEventObject) obj).getParam2()));
    }

    @Override // ohos.rpc.RemoteObject
    public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
        HiLog.info(LABEL, "onRemoteRequest, code: %{public}d", Integer.valueOf(i));
        if (i != 1) {
            return super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
        }
        messageParcel.readInt();
        messageParcel.readInt();
        messageParcel.readString();
        if (messageParcel.readInt() == 0) {
            messageParcel2.writeInt(0);
            return true;
        }
        int readInt = messageParcel.readInt();
        messageParcel.readInt();
        int readInt2 = messageParcel.readInt();
        Sequenceable sequenceable = null;
        if (messageParcel.readInt() == 1) {
            messageParcel.readString();
            switch (readInt) {
                case RESPONSE_PEERS /* 139284 */:
                    handlePeersResponse(messageParcel, readInt, readInt2);
                    return true;
                case RESPONSE_CONNECTION_INFO /* 139286 */:
                    sequenceable = new WifiP2pLinkedInfo();
                    break;
                case RESPONSE_GROUP_INFO /* 139288 */:
                    sequenceable = new WifiP2pGroup();
                    break;
                case RESPONSE_PERSISTENT_GROUP_INFO /* 139322 */:
                    handleGroupInfoResponse(messageParcel, readInt, readInt2);
                    return true;
                case RESPONSE_NETWORK_INFO /* 139359 */:
                    sequenceable = new WifiP2pNetworkInfo();
                    break;
                case RESPONSE_DEVICE_INFO /* 139362 */:
                    sequenceable = new WifiP2pDevice();
                    break;
                default:
                    HiLog.warn(LABEL, "ignored event: %{public}d", Integer.valueOf(readInt));
                    break;
            }
            if (sequenceable != null) {
                sequenceable.unmarshalling(messageParcel);
            }
        }
        sendInnerEvent(readInt, readInt2, sequenceable);
        return true;
    }

    private void handlePeersResponse(MessageParcel messageParcel, int i, int i2) {
        int readInt = messageParcel.readInt();
        if (readInt < 0 || readInt > 32) {
            HiLog.warn(LABEL, "handlePeersResponse has illegal device size : %{public}d", Integer.valueOf(readInt));
            return;
        }
        ArrayList arrayList = new ArrayList(readInt);
        for (int i3 = 0; i3 < readInt; i3++) {
            WifiP2pDevice wifiP2pDevice = new WifiP2pDevice();
            messageParcel.readString();
            wifiP2pDevice.unmarshalling(messageParcel);
            arrayList.add(wifiP2pDevice);
        }
        sendInnerEvent(i, i2, arrayList);
    }

    private void handleGroupInfoResponse(MessageParcel messageParcel, int i, int i2) {
        int readInt = messageParcel.readInt();
        if (readInt < 0 || readInt > 2048) {
            HiLog.warn(LABEL, "handleGroupInfoResponse has illegal device size : %{public}d", Integer.valueOf(readInt));
            return;
        }
        ArrayList arrayList = new ArrayList(readInt);
        for (int i3 = 0; i3 < readInt; i3++) {
            WifiP2pGroup wifiP2pGroup = new WifiP2pGroup();
            messageParcel.readString();
            wifiP2pGroup.unmarshalling(messageParcel);
            arrayList.add(wifiP2pGroup);
        }
        sendInnerEvent(i, i2, arrayList);
    }

    private void sendInnerEvent(int i, int i2, Object obj) {
        InnerEvent innerEvent = InnerEvent.get();
        P2pEventObject p2pEventObject = new P2pEventObject(0, i2, obj);
        innerEvent.eventId = i;
        innerEvent.object = p2pEventObject;
        new CallbackHandler(this.mEventRunner).sendEvent(innerEvent);
    }

    /* access modifiers changed from: private */
    public static class P2pEventObject {
        private Object object;
        private int param1;
        private int param2;

        public P2pEventObject(int i, int i2, Object obj) {
            this.param1 = i;
            this.param2 = i2;
            this.object = obj;
        }

        public int getParam1() {
            return this.param1;
        }

        public int getParam2() {
            return this.param2;
        }

        public Object getObject() {
            return this.object;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleChannelDisconnected(WifiP2pCallback wifiP2pCallback, Object obj) {
        if ((obj instanceof P2pEventObject) && wifiP2pCallback != null) {
            wifiP2pCallback.eventP2pControllerDisconnected();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleResponsePeers(WifiP2pCallback wifiP2pCallback, Object obj) {
        try {
            if ((obj instanceof P2pEventObject) && wifiP2pCallback != null) {
                Object object = ((P2pEventObject) obj).getObject();
                if (object instanceof List) {
                    wifiP2pCallback.eventP2pDevicesList((List) object);
                }
            }
        } catch (ClassCastException unused) {
            HiLog.error(LABEL, "handleResponsePeers has wrong class cast!", new Object[0]);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlePersistentGroupInfo(WifiP2pCallback wifiP2pCallback, Object obj) {
        try {
            if ((obj instanceof P2pEventObject) && wifiP2pCallback != null) {
                Object object = ((P2pEventObject) obj).getObject();
                if (object instanceof List) {
                    wifiP2pCallback.eventP2pPersistentGroup((List) object);
                }
            }
        } catch (ClassCastException unused) {
            HiLog.error(LABEL, "handlePersistentGroupInfo has wrong class cast!", new Object[0]);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleNetworkInfo(WifiP2pCallback wifiP2pCallback, Object obj) {
        if ((obj instanceof P2pEventObject) && wifiP2pCallback != null) {
            Object object = ((P2pEventObject) obj).getObject();
            if (object instanceof WifiP2pNetworkInfo) {
                wifiP2pCallback.eventP2pNetwork((WifiP2pNetworkInfo) object);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleConnectionInfo(WifiP2pCallback wifiP2pCallback, Object obj) {
        if ((obj instanceof P2pEventObject) && wifiP2pCallback != null) {
            Object object = ((P2pEventObject) obj).getObject();
            if (object instanceof WifiP2pLinkedInfo) {
                wifiP2pCallback.eventP2pLinkedInfo((WifiP2pLinkedInfo) object);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleDeviceInfo(WifiP2pCallback wifiP2pCallback, Object obj) {
        if ((obj instanceof P2pEventObject) && wifiP2pCallback != null) {
            Object object = ((P2pEventObject) obj).getObject();
            if (object instanceof WifiP2pDevice) {
                wifiP2pCallback.eventP2pDevice((WifiP2pDevice) object);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleGroupInfo(WifiP2pCallback wifiP2pCallback, Object obj) {
        if ((obj instanceof P2pEventObject) && wifiP2pCallback != null) {
            Object object = ((P2pEventObject) obj).getObject();
            if (object instanceof WifiP2pGroup) {
                wifiP2pCallback.eventP2pGroup((WifiP2pGroup) object);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleEventFailed(WifiP2pCallback wifiP2pCallback, Object obj) {
        if ((obj instanceof P2pEventObject) && wifiP2pCallback != null) {
            wifiP2pCallback.eventExecFail(((P2pEventObject) obj).getParam1());
        }
    }
}
