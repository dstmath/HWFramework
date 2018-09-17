package android.net.wifi.p2p;

import android.content.Context;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceResponse;
import android.net.wifi.p2p.nsd.WifiP2pServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pServiceRequest;
import android.net.wifi.p2p.nsd.WifiP2pServiceResponse;
import android.net.wifi.p2p.nsd.WifiP2pUpnpServiceResponse;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.Process;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.util.AsyncChannel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WifiP2pManager {
    public static final int ADD_LOCAL_SERVICE = 139292;
    public static final int ADD_LOCAL_SERVICE_FAILED = 139293;
    public static final int ADD_LOCAL_SERVICE_SUCCEEDED = 139294;
    public static final int ADD_SERVICE_REQUEST = 139301;
    public static final int ADD_SERVICE_REQUEST_FAILED = 139302;
    public static final int ADD_SERVICE_REQUEST_SUCCEEDED = 139303;
    private static final int BASE = 139264;
    public static final int BUSY = 2;
    public static final String CALLING_PACKAGE = "android.net.wifi.p2p.CALLING_PACKAGE";
    public static final int CANCEL_CONNECT = 139274;
    public static final int CANCEL_CONNECT_FAILED = 139275;
    public static final int CANCEL_CONNECT_SUCCEEDED = 139276;
    public static final int CLEAR_LOCAL_SERVICES = 139298;
    public static final int CLEAR_LOCAL_SERVICES_FAILED = 139299;
    public static final int CLEAR_LOCAL_SERVICES_SUCCEEDED = 139300;
    public static final int CLEAR_SERVICE_REQUESTS = 139307;
    public static final int CLEAR_SERVICE_REQUESTS_FAILED = 139308;
    public static final int CLEAR_SERVICE_REQUESTS_SUCCEEDED = 139309;
    public static final int CONNECT = 139271;
    public static final int CONNECT_FAILED = 139272;
    public static final int CONNECT_SUCCEEDED = 139273;
    public static final int CREATE_GROUP = 139277;
    public static final int CREATE_GROUP_FAILED = 139278;
    public static final int CREATE_GROUP_SUCCEEDED = 139279;
    public static final int DELETE_PERSISTENT_GROUP = 139318;
    public static final int DELETE_PERSISTENT_GROUP_FAILED = 139319;
    public static final int DELETE_PERSISTENT_GROUP_SUCCEEDED = 139320;
    public static final int DISCOVER_PEERS = 139265;
    public static final int DISCOVER_PEERS_FAILED = 139266;
    public static final int DISCOVER_PEERS_SUCCEEDED = 139267;
    public static final int DISCOVER_SERVICES = 139310;
    public static final int DISCOVER_SERVICES_FAILED = 139311;
    public static final int DISCOVER_SERVICES_SUCCEEDED = 139312;
    public static final int ERROR = 0;
    public static final String EXTRA_DISCOVERY_STATE = "discoveryState";
    public static final String EXTRA_HANDOVER_MESSAGE = "android.net.wifi.p2p.EXTRA_HANDOVER_MESSAGE";
    public static final String EXTRA_NETWORK_INFO = "networkInfo";
    public static final String EXTRA_P2P_DEVICE_LIST = "wifiP2pDeviceList";
    public static final String EXTRA_P2P_FLAG = "extra_p2p_flag";
    public static final String EXTRA_WIFI_P2P_DEVICE = "wifiP2pDevice";
    public static final String EXTRA_WIFI_P2P_GROUP = "p2pGroupInfo";
    public static final String EXTRA_WIFI_P2P_INFO = "wifiP2pInfo";
    public static final String EXTRA_WIFI_STATE = "wifi_p2p_state";
    public static final int GET_HANDOVER_REQUEST = 139339;
    public static final int GET_HANDOVER_SELECT = 139340;
    public static final int INITIATOR_REPORT_NFC_HANDOVER = 139342;
    public static final int MIRACAST_DISABLED = 0;
    public static final int MIRACAST_SINK = 2;
    public static final int MIRACAST_SOURCE = 1;
    public static final int NO_SERVICE_REQUESTS = 3;
    public static final int P2P_UNSUPPORTED = 1;
    public static final int PING = 139313;
    public static final int REMOVE_GROUP = 139280;
    public static final int REMOVE_GROUP_FAILED = 139281;
    public static final int REMOVE_GROUP_SUCCEEDED = 139282;
    public static final int REMOVE_LOCAL_SERVICE = 139295;
    public static final int REMOVE_LOCAL_SERVICE_FAILED = 139296;
    public static final int REMOVE_LOCAL_SERVICE_SUCCEEDED = 139297;
    public static final int REMOVE_SERVICE_REQUEST = 139304;
    public static final int REMOVE_SERVICE_REQUEST_FAILED = 139305;
    public static final int REMOVE_SERVICE_REQUEST_SUCCEEDED = 139306;
    public static final int REPORT_NFC_HANDOVER_FAILED = 139345;
    public static final int REPORT_NFC_HANDOVER_SUCCEEDED = 139344;
    public static final int REQUEST_CONNECTION_INFO = 139285;
    public static final int REQUEST_GROUP_INFO = 139287;
    public static final int REQUEST_PEERS = 139283;
    public static final int REQUEST_PERSISTENT_GROUP_INFO = 139321;
    public static final int RESPONDER_REPORT_NFC_HANDOVER = 139343;
    public static final int RESPONSE_CONNECTION_INFO = 139286;
    public static final int RESPONSE_GET_HANDOVER_MESSAGE = 139341;
    public static final int RESPONSE_GROUP_INFO = 139288;
    public static final int RESPONSE_PEERS = 139284;
    public static final int RESPONSE_PERSISTENT_GROUP_INFO = 139322;
    public static final int RESPONSE_SERVICE = 139314;
    public static final int SET_CHANNEL = 139335;
    public static final int SET_CHANNEL_FAILED = 139336;
    public static final int SET_CHANNEL_SUCCEEDED = 139337;
    public static final int SET_DEVICE_NAME = 139315;
    public static final int SET_DEVICE_NAME_FAILED = 139316;
    public static final int SET_DEVICE_NAME_SUCCEEDED = 139317;
    public static final int SET_WFD_INFO = 139323;
    public static final int SET_WFD_INFO_FAILED = 139324;
    public static final int SET_WFD_INFO_SUCCEEDED = 139325;
    public static final int START_LISTEN = 139329;
    public static final int START_LISTEN_FAILED = 139330;
    public static final int START_LISTEN_SUCCEEDED = 139331;
    public static final int START_WPS = 139326;
    public static final int START_WPS_FAILED = 139327;
    public static final int START_WPS_SUCCEEDED = 139328;
    public static final int STOP_DISCOVERY = 139268;
    public static final int STOP_DISCOVERY_FAILED = 139269;
    public static final int STOP_DISCOVERY_SUCCEEDED = 139270;
    public static final int STOP_LISTEN = 139332;
    public static final int STOP_LISTEN_FAILED = 139333;
    public static final int STOP_LISTEN_SUCCEEDED = 139334;
    private static final String TAG = "WifiP2pManager";
    public static final String WIFI_P2P_CONNECTION_CHANGED_ACTION = "android.net.wifi.p2p.CONNECTION_STATE_CHANGE";
    public static final String WIFI_P2P_DISCOVERY_CHANGED_ACTION = "android.net.wifi.p2p.DISCOVERY_STATE_CHANGE";
    public static final int WIFI_P2P_DISCOVERY_STARTED = 2;
    public static final int WIFI_P2P_DISCOVERY_STOPPED = 1;
    public static final String WIFI_P2P_FLAG_CHANGED_ACTION = "android.net.wifi.p2p.WIFI_P2P_FLAG_CHANGED_ACTION";
    public static final String WIFI_P2P_NETWORK_CHANGED_ACTION = "android.net.wifi.p2p.WIFI_P2P_NETWORK_CHANGED_ACTION";
    public static final int WIFI_P2P_OFF = 0;
    public static final int WIFI_P2P_OFF_BY_STA = 3;
    public static final int WIFI_P2P_ON = 1;
    public static final int WIFI_P2P_ON_BY_USER = 2;
    public static final String WIFI_P2P_PEERS_CHANGED_ACTION = "android.net.wifi.p2p.PEERS_CHANGED";
    public static final String WIFI_P2P_PERSISTENT_GROUPS_CHANGED_ACTION = "android.net.wifi.p2p.PERSISTENT_GROUPS_CHANGED";
    public static final String WIFI_P2P_STATE_CHANGED_ACTION = "android.net.wifi.p2p.STATE_CHANGED";
    public static final int WIFI_P2P_STATE_DISABLED = 1;
    public static final int WIFI_P2P_STATE_ENABLED = 2;
    public static final String WIFI_P2P_THIS_DEVICE_CHANGED_ACTION = "android.net.wifi.p2p.THIS_DEVICE_CHANGED";
    IWifiP2pManager mService;

    public interface ActionListener {
        void onFailure(int i);

        void onSuccess();
    }

    public static class Channel {
        private static final int INVALID_LISTENER_KEY = 0;
        private AsyncChannel mAsyncChannel = new AsyncChannel();
        private ChannelListener mChannelListener;
        Context mContext;
        private DnsSdServiceResponseListener mDnsSdServRspListener;
        private DnsSdTxtRecordListener mDnsSdTxtListener;
        private P2pHandler mHandler;
        private int mListenerKey = 0;
        private HashMap<Integer, Object> mListenerMap = new HashMap();
        private Object mListenerMapLock = new Object();
        private ServiceResponseListener mServRspListener;
        private UpnpServiceResponseListener mUpnpServRspListener;

        class P2pHandler extends Handler {
            P2pHandler(Looper looper) {
                super(looper);
            }

            public void handleMessage(Message message) {
                Object listener = Channel.this.getListener(message.arg2);
                switch (message.what) {
                    case 69636:
                        if (Channel.this.mChannelListener != null) {
                            Channel.this.mChannelListener.onChannelDisconnected();
                            Channel.this.mChannelListener = null;
                            return;
                        }
                        return;
                    case WifiP2pManager.DISCOVER_PEERS_FAILED /*139266*/:
                    case WifiP2pManager.STOP_DISCOVERY_FAILED /*139269*/:
                    case WifiP2pManager.CONNECT_FAILED /*139272*/:
                    case WifiP2pManager.CANCEL_CONNECT_FAILED /*139275*/:
                    case WifiP2pManager.CREATE_GROUP_FAILED /*139278*/:
                    case WifiP2pManager.REMOVE_GROUP_FAILED /*139281*/:
                    case WifiP2pManager.ADD_LOCAL_SERVICE_FAILED /*139293*/:
                    case WifiP2pManager.REMOVE_LOCAL_SERVICE_FAILED /*139296*/:
                    case WifiP2pManager.CLEAR_LOCAL_SERVICES_FAILED /*139299*/:
                    case WifiP2pManager.ADD_SERVICE_REQUEST_FAILED /*139302*/:
                    case WifiP2pManager.REMOVE_SERVICE_REQUEST_FAILED /*139305*/:
                    case WifiP2pManager.CLEAR_SERVICE_REQUESTS_FAILED /*139308*/:
                    case WifiP2pManager.DISCOVER_SERVICES_FAILED /*139311*/:
                    case WifiP2pManager.SET_DEVICE_NAME_FAILED /*139316*/:
                    case WifiP2pManager.DELETE_PERSISTENT_GROUP_FAILED /*139319*/:
                    case WifiP2pManager.SET_WFD_INFO_FAILED /*139324*/:
                    case WifiP2pManager.START_WPS_FAILED /*139327*/:
                    case WifiP2pManager.START_LISTEN_FAILED /*139330*/:
                    case WifiP2pManager.STOP_LISTEN_FAILED /*139333*/:
                    case WifiP2pManager.SET_CHANNEL_FAILED /*139336*/:
                    case WifiP2pManager.REPORT_NFC_HANDOVER_FAILED /*139345*/:
                        if (listener != null) {
                            ((ActionListener) listener).onFailure(message.arg1);
                            return;
                        }
                        return;
                    case WifiP2pManager.DISCOVER_PEERS_SUCCEEDED /*139267*/:
                    case WifiP2pManager.STOP_DISCOVERY_SUCCEEDED /*139270*/:
                    case WifiP2pManager.CONNECT_SUCCEEDED /*139273*/:
                    case WifiP2pManager.CANCEL_CONNECT_SUCCEEDED /*139276*/:
                    case WifiP2pManager.CREATE_GROUP_SUCCEEDED /*139279*/:
                    case WifiP2pManager.REMOVE_GROUP_SUCCEEDED /*139282*/:
                    case WifiP2pManager.ADD_LOCAL_SERVICE_SUCCEEDED /*139294*/:
                    case WifiP2pManager.REMOVE_LOCAL_SERVICE_SUCCEEDED /*139297*/:
                    case WifiP2pManager.CLEAR_LOCAL_SERVICES_SUCCEEDED /*139300*/:
                    case WifiP2pManager.ADD_SERVICE_REQUEST_SUCCEEDED /*139303*/:
                    case WifiP2pManager.REMOVE_SERVICE_REQUEST_SUCCEEDED /*139306*/:
                    case WifiP2pManager.CLEAR_SERVICE_REQUESTS_SUCCEEDED /*139309*/:
                    case WifiP2pManager.DISCOVER_SERVICES_SUCCEEDED /*139312*/:
                    case WifiP2pManager.SET_DEVICE_NAME_SUCCEEDED /*139317*/:
                    case WifiP2pManager.DELETE_PERSISTENT_GROUP_SUCCEEDED /*139320*/:
                    case WifiP2pManager.SET_WFD_INFO_SUCCEEDED /*139325*/:
                    case WifiP2pManager.START_WPS_SUCCEEDED /*139328*/:
                    case WifiP2pManager.START_LISTEN_SUCCEEDED /*139331*/:
                    case WifiP2pManager.STOP_LISTEN_SUCCEEDED /*139334*/:
                    case WifiP2pManager.SET_CHANNEL_SUCCEEDED /*139337*/:
                    case WifiP2pManager.REPORT_NFC_HANDOVER_SUCCEEDED /*139344*/:
                        if (listener != null) {
                            ((ActionListener) listener).onSuccess();
                            return;
                        }
                        return;
                    case WifiP2pManager.RESPONSE_PEERS /*139284*/:
                        WifiP2pDeviceList peers = message.obj;
                        if (listener != null) {
                            ((PeerListListener) listener).onPeersAvailable(peers);
                            return;
                        }
                        return;
                    case WifiP2pManager.RESPONSE_CONNECTION_INFO /*139286*/:
                        WifiP2pInfo wifiP2pInfo = message.obj;
                        if (listener != null) {
                            ((ConnectionInfoListener) listener).onConnectionInfoAvailable(wifiP2pInfo);
                            return;
                        }
                        return;
                    case WifiP2pManager.RESPONSE_GROUP_INFO /*139288*/:
                        WifiP2pGroup group = message.obj;
                        if (listener != null) {
                            ((GroupInfoListener) listener).onGroupInfoAvailable(group);
                            return;
                        }
                        return;
                    case WifiP2pManager.RESPONSE_SERVICE /*139314*/:
                        Channel.this.handleServiceResponse(message.obj);
                        return;
                    case WifiP2pManager.RESPONSE_PERSISTENT_GROUP_INFO /*139322*/:
                        WifiP2pGroupList groups = message.obj;
                        if (listener != null) {
                            ((PersistentGroupInfoListener) listener).onPersistentGroupInfoAvailable(groups);
                            return;
                        }
                        return;
                    case WifiP2pManager.RESPONSE_GET_HANDOVER_MESSAGE /*139341*/:
                        Bundle handoverBundle = message.obj;
                        if (listener != null) {
                            String handoverMessage;
                            if (handoverBundle != null) {
                                handoverMessage = handoverBundle.getString(WifiP2pManager.EXTRA_HANDOVER_MESSAGE);
                            } else {
                                handoverMessage = null;
                            }
                            ((HandoverMessageListener) listener).onHandoverMessageAvailable(handoverMessage);
                            return;
                        }
                        return;
                    default:
                        Log.d(WifiP2pManager.TAG, "Ignored " + message);
                        return;
                }
            }
        }

        Channel(Context context, Looper looper, ChannelListener l) {
            this.mHandler = new P2pHandler(looper);
            this.mChannelListener = l;
            this.mContext = context;
        }

        private void handleServiceResponse(WifiP2pServiceResponse resp) {
            if (resp instanceof WifiP2pDnsSdServiceResponse) {
                handleDnsSdServiceResponse((WifiP2pDnsSdServiceResponse) resp);
            } else if (resp instanceof WifiP2pUpnpServiceResponse) {
                if (this.mUpnpServRspListener != null) {
                    handleUpnpServiceResponse((WifiP2pUpnpServiceResponse) resp);
                }
            } else if (this.mServRspListener != null) {
                this.mServRspListener.onServiceAvailable(resp.getServiceType(), resp.getRawData(), resp.getSrcDevice());
            }
        }

        private void handleUpnpServiceResponse(WifiP2pUpnpServiceResponse resp) {
            this.mUpnpServRspListener.onUpnpServiceAvailable(resp.getUniqueServiceNames(), resp.getSrcDevice());
        }

        private void handleDnsSdServiceResponse(WifiP2pDnsSdServiceResponse resp) {
            if (resp.getDnsType() == 12) {
                if (this.mDnsSdServRspListener != null) {
                    this.mDnsSdServRspListener.onDnsSdServiceAvailable(resp.getInstanceName(), resp.getDnsQueryName(), resp.getSrcDevice());
                }
            } else if (resp.getDnsType() != 16) {
                Log.e(WifiP2pManager.TAG, "Unhandled resp " + resp);
            } else if (this.mDnsSdTxtListener != null) {
                this.mDnsSdTxtListener.onDnsSdTxtRecordAvailable(resp.getDnsQueryName(), resp.getTxtRecord(), resp.getSrcDevice());
            }
        }

        private int putListener(Object listener) {
            if (listener == null) {
                return 0;
            }
            int key;
            synchronized (this.mListenerMapLock) {
                do {
                    key = this.mListenerKey;
                    this.mListenerKey = key + 1;
                } while (key == 0);
                this.mListenerMap.put(Integer.valueOf(key), listener);
            }
            return key;
        }

        private Object getListener(int key) {
            if (key == 0) {
                return null;
            }
            Object remove;
            synchronized (this.mListenerMapLock) {
                remove = this.mListenerMap.remove(Integer.valueOf(key));
            }
            return remove;
        }

        public AsyncChannel getAsyncChannel() {
            return this.mAsyncChannel;
        }
    }

    public interface ChannelListener {
        void onChannelDisconnected();
    }

    public interface ConnectionInfoListener {
        void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo);
    }

    public interface DnsSdServiceResponseListener {
        void onDnsSdServiceAvailable(String str, String str2, WifiP2pDevice wifiP2pDevice);
    }

    public interface DnsSdTxtRecordListener {
        void onDnsSdTxtRecordAvailable(String str, Map<String, String> map, WifiP2pDevice wifiP2pDevice);
    }

    public interface GroupInfoListener {
        void onGroupInfoAvailable(WifiP2pGroup wifiP2pGroup);
    }

    public interface HandoverMessageListener {
        void onHandoverMessageAvailable(String str);
    }

    public interface PeerListListener {
        void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList);
    }

    public interface PersistentGroupInfoListener {
        void onPersistentGroupInfoAvailable(WifiP2pGroupList wifiP2pGroupList);
    }

    public interface ServiceResponseListener {
        void onServiceAvailable(int i, byte[] bArr, WifiP2pDevice wifiP2pDevice);
    }

    public interface UpnpServiceResponseListener {
        void onUpnpServiceAvailable(List<String> list, WifiP2pDevice wifiP2pDevice);
    }

    public WifiP2pManager(IWifiP2pManager service) {
        this.mService = service;
    }

    private static void checkChannel(Channel c) {
        if (c == null) {
            throw new IllegalArgumentException("Channel needs to be initialized");
        }
    }

    private static void checkServiceInfo(WifiP2pServiceInfo info) {
        if (info == null) {
            throw new IllegalArgumentException("service info is null");
        }
    }

    private static void checkServiceRequest(WifiP2pServiceRequest req) {
        if (req == null) {
            throw new IllegalArgumentException("service request is null");
        }
    }

    private static void checkP2pConfig(WifiP2pConfig c) {
        if (c == null) {
            throw new IllegalArgumentException("config cannot be null");
        } else if (TextUtils.isEmpty(c.deviceAddress)) {
            throw new IllegalArgumentException("deviceAddress cannot be empty");
        }
    }

    public Channel initialize(Context srcContext, Looper srcLooper, ChannelListener listener) {
        return initalizeChannel(srcContext, srcLooper, listener, getMessenger());
    }

    public Channel initializeInternal(Context srcContext, Looper srcLooper, ChannelListener listener) {
        return initalizeChannel(srcContext, srcLooper, listener, getP2pStateMachineMessenger());
    }

    private Channel initalizeChannel(Context srcContext, Looper srcLooper, ChannelListener listener, Messenger messenger) {
        if (messenger == null) {
            return null;
        }
        Channel c = new Channel(srcContext, srcLooper, listener);
        if (c.mAsyncChannel.connectSync(srcContext, c.mHandler, messenger) == 0) {
            return c;
        }
        return null;
    }

    public void discoverPeers(Channel c, ActionListener listener) {
        checkChannel(c);
        Log.d(TAG, "discoverPeers, pid:" + Process.myPid() + ", tid:" + Process.myTid() + ", uid:" + Process.myUid());
        c.mAsyncChannel.sendMessage(DISCOVER_PEERS, 0, c.putListener(listener));
    }

    public void stopPeerDiscovery(Channel c, ActionListener listener) {
        checkChannel(c);
        Log.d(TAG, "stopPeerDiscovery, pid:" + Process.myPid() + ", tid:" + Process.myTid() + ", uid:" + Process.myUid());
        c.mAsyncChannel.sendMessage(STOP_DISCOVERY, 0, c.putListener(listener));
    }

    public void connect(Channel c, WifiP2pConfig config, ActionListener listener) {
        checkChannel(c);
        checkP2pConfig(config);
        Log.d(TAG, "connect, pid:" + Process.myPid() + ", tid:" + Process.myTid() + ", uid:" + Process.myUid());
        c.mAsyncChannel.sendMessage(CONNECT, 0, c.putListener(listener), config);
    }

    public void cancelConnect(Channel c, ActionListener listener) {
        checkChannel(c);
        c.mAsyncChannel.sendMessage(CANCEL_CONNECT, 0, c.putListener(listener));
    }

    public void createGroup(Channel c, ActionListener listener) {
        checkChannel(c);
        c.mAsyncChannel.sendMessage(CREATE_GROUP, -2, c.putListener(listener));
    }

    public void removeGroup(Channel c, ActionListener listener) {
        checkChannel(c);
        c.mAsyncChannel.sendMessage(REMOVE_GROUP, 0, c.putListener(listener));
    }

    public void listen(Channel c, boolean enable, ActionListener listener) {
        checkChannel(c);
        c.mAsyncChannel.sendMessage(enable ? START_LISTEN : STOP_LISTEN, 0, c.putListener(listener));
    }

    public void setWifiP2pChannels(Channel c, int lc, int oc, ActionListener listener) {
        checkChannel(c);
        Bundle p2pChannels = new Bundle();
        p2pChannels.putInt("lc", lc);
        p2pChannels.putInt("oc", oc);
        c.mAsyncChannel.sendMessage(SET_CHANNEL, 0, c.putListener(listener), p2pChannels);
    }

    public void startWps(Channel c, WpsInfo wps, ActionListener listener) {
        checkChannel(c);
        c.mAsyncChannel.sendMessage(START_WPS, 0, c.putListener(listener), wps);
    }

    public void addLocalService(Channel c, WifiP2pServiceInfo servInfo, ActionListener listener) {
        checkChannel(c);
        checkServiceInfo(servInfo);
        c.mAsyncChannel.sendMessage(ADD_LOCAL_SERVICE, 0, c.putListener(listener), servInfo);
    }

    public void removeLocalService(Channel c, WifiP2pServiceInfo servInfo, ActionListener listener) {
        checkChannel(c);
        checkServiceInfo(servInfo);
        c.mAsyncChannel.sendMessage(REMOVE_LOCAL_SERVICE, 0, c.putListener(listener), servInfo);
    }

    public void clearLocalServices(Channel c, ActionListener listener) {
        checkChannel(c);
        c.mAsyncChannel.sendMessage(CLEAR_LOCAL_SERVICES, 0, c.putListener(listener));
    }

    public void setServiceResponseListener(Channel c, ServiceResponseListener listener) {
        checkChannel(c);
        c.mServRspListener = listener;
    }

    public void setDnsSdResponseListeners(Channel c, DnsSdServiceResponseListener servListener, DnsSdTxtRecordListener txtListener) {
        checkChannel(c);
        c.mDnsSdServRspListener = servListener;
        c.mDnsSdTxtListener = txtListener;
    }

    public void setUpnpServiceResponseListener(Channel c, UpnpServiceResponseListener listener) {
        checkChannel(c);
        c.mUpnpServRspListener = listener;
    }

    public void discoverServices(Channel c, ActionListener listener) {
        checkChannel(c);
        c.mAsyncChannel.sendMessage(DISCOVER_SERVICES, 0, c.putListener(listener));
    }

    public void addServiceRequest(Channel c, WifiP2pServiceRequest req, ActionListener listener) {
        checkChannel(c);
        checkServiceRequest(req);
        c.mAsyncChannel.sendMessage(ADD_SERVICE_REQUEST, 0, c.putListener(listener), req);
    }

    public void removeServiceRequest(Channel c, WifiP2pServiceRequest req, ActionListener listener) {
        checkChannel(c);
        checkServiceRequest(req);
        c.mAsyncChannel.sendMessage(REMOVE_SERVICE_REQUEST, 0, c.putListener(listener), req);
    }

    public void clearServiceRequests(Channel c, ActionListener listener) {
        checkChannel(c);
        c.mAsyncChannel.sendMessage(CLEAR_SERVICE_REQUESTS, 0, c.putListener(listener));
    }

    public void requestPeers(Channel c, PeerListListener listener) {
        checkChannel(c);
        Bundle callingPackage = new Bundle();
        callingPackage.putString(CALLING_PACKAGE, c.mContext.getOpPackageName());
        c.mAsyncChannel.sendMessage(REQUEST_PEERS, 0, c.putListener(listener), callingPackage);
    }

    public void requestConnectionInfo(Channel c, ConnectionInfoListener listener) {
        checkChannel(c);
        c.mAsyncChannel.sendMessage(REQUEST_CONNECTION_INFO, 0, c.putListener(listener));
    }

    public void requestGroupInfo(Channel c, GroupInfoListener listener) {
        checkChannel(c);
        c.mAsyncChannel.sendMessage(REQUEST_GROUP_INFO, 0, c.putListener(listener));
    }

    public void setDeviceName(Channel c, String devName, ActionListener listener) {
        checkChannel(c);
        WifiP2pDevice d = new WifiP2pDevice();
        d.deviceName = devName;
        c.mAsyncChannel.sendMessage(SET_DEVICE_NAME, 0, c.putListener(listener), d);
    }

    public void setWFDInfo(Channel c, WifiP2pWfdInfo wfdInfo, ActionListener listener) {
        checkChannel(c);
        try {
            this.mService.checkConfigureWifiDisplayPermission();
        } catch (RemoteException e) {
            e.rethrowFromSystemServer();
        }
        c.mAsyncChannel.sendMessage(SET_WFD_INFO, 0, c.putListener(listener), wfdInfo);
    }

    public void deletePersistentGroup(Channel c, int netId, ActionListener listener) {
        checkChannel(c);
        c.mAsyncChannel.sendMessage(DELETE_PERSISTENT_GROUP, netId, c.putListener(listener));
    }

    public void requestPersistentGroupInfo(Channel c, PersistentGroupInfoListener listener) {
        checkChannel(c);
        c.mAsyncChannel.sendMessage(REQUEST_PERSISTENT_GROUP_INFO, 0, c.putListener(listener));
    }

    public void setMiracastMode(int mode) {
        try {
            this.mService.setMiracastMode(mode);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public Messenger getMessenger() {
        try {
            if (this.mService != null) {
                return this.mService.getMessenger();
            }
            return null;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public Messenger getP2pStateMachineMessenger() {
        try {
            return this.mService.getP2pStateMachineMessenger();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void getNfcHandoverRequest(Channel c, HandoverMessageListener listener) {
        checkChannel(c);
        c.mAsyncChannel.sendMessage(GET_HANDOVER_REQUEST, 0, c.putListener(listener));
    }

    public void getNfcHandoverSelect(Channel c, HandoverMessageListener listener) {
        checkChannel(c);
        c.mAsyncChannel.sendMessage(GET_HANDOVER_SELECT, 0, c.putListener(listener));
    }

    public void initiatorReportNfcHandover(Channel c, String handoverSelect, ActionListener listener) {
        checkChannel(c);
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_HANDOVER_MESSAGE, handoverSelect);
        c.mAsyncChannel.sendMessage(INITIATOR_REPORT_NFC_HANDOVER, 0, c.putListener(listener), bundle);
    }

    public void responderReportNfcHandover(Channel c, String handoverRequest, ActionListener listener) {
        checkChannel(c);
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_HANDOVER_MESSAGE, handoverRequest);
        c.mAsyncChannel.sendMessage(RESPONDER_REPORT_NFC_HANDOVER, 0, c.putListener(listener), bundle);
    }
}
