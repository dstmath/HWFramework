package com.huawei.nearbysdk;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import com.huawei.nearbysdk.IInternalSocketListener.Stub;
import com.huawei.nearbysdk.NearbyConfig.BusinessTypeEnum;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class SocketListenerTransport extends Stub {
    private static final String MSG_KEY_IP = "IP";
    private static final String MSG_KEY_PASSPHARSE = "PASSPHARSE";
    private static final int SOCKET_TIMEOUT = 30000;
    static final String TAG = "SocketListenerTransport";
    private static final int TYPE_CONNECT_REQUEST = 2;
    private static final int TYPE_HWSHARE_I_CONNECT_REQUEST = 3;
    private static final int TYPE_STATUS_CHANGED = 1;
    private static final int TYPE_THREAD_RESPONSE_CLIENT = 4;
    private final BluetoothSocketTransport mBluetoothSocketTransport = new BluetoothSocketTransport();
    private SocketListener mListener;
    private final Handler mListenerHandler;
    private String mPasspharse = null;

    class ChannelCreateRequestImpl implements ChannelCreateRequest {
        IInternalChannelCreateRequest mInnerRequest;

        public ChannelCreateRequestImpl(IInternalChannelCreateRequest innerRequest) {
            this.mInnerRequest = innerRequest;
        }

        public int getBusinessId() {
            int result = -1;
            try {
                return this.mInnerRequest.getBusinessId();
            } catch (RemoteException e) {
                e.printStackTrace();
                return result;
            }
        }

        public int getSecurityType() {
            int result = 0;
            try {
                return this.mInnerRequest.getSecurityType();
            } catch (RemoteException e) {
                e.printStackTrace();
                return result;
            }
        }

        public String getTag() {
            String result = "";
            try {
                return this.mInnerRequest.getTag();
            } catch (RemoteException e) {
                e.printStackTrace();
                return result;
            }
        }

        public BusinessTypeEnum getBusinessType() {
            BusinessTypeEnum result = null;
            try {
                return NearbySDKUtils.getEnumFromInt(this.mInnerRequest.getBusinessType());
            } catch (RemoteException e) {
                e.printStackTrace();
                return result;
            }
        }

        public int getChannelId() {
            int result = -1;
            try {
                return this.mInnerRequest.getChannelId();
            } catch (RemoteException e) {
                e.printStackTrace();
                return result;
            }
        }

        public String getServiceUuid() {
            String result = "";
            try {
                return this.mInnerRequest.getServiceUuid();
            } catch (RemoteException e) {
                e.printStackTrace();
                return result;
            }
        }

        public int getPort() {
            int result = -1;
            try {
                return this.mInnerRequest.getPort();
            } catch (RemoteException e) {
                e.printStackTrace();
                return result;
            }
        }

        public NearbyDevice getRemoteNearbyDevice() {
            NearbyDevice result = null;
            try {
                return this.mInnerRequest.getRemoteNearbyDevice();
            } catch (RemoteException e) {
                e.printStackTrace();
                return result;
            }
        }

        public void accept(int port) {
            try {
                this.mInnerRequest.accept(port);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        public NearbySocket accept() throws IOException {
            return acceptTimer(0);
        }

        public NearbySocket acceptTimer(int timeOut) throws IOException {
            int channelId = -1;
            int protocol = -1;
            HwLog.e(SocketListenerTransport.TAG, "accept.. timeOut = " + timeOut);
            try {
                channelId = this.mInnerRequest.getChannelId();
                protocol = this.mInnerRequest.getProtocol();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            return createNearbySocketByChannelId(channelId, protocol, timeOut);
        }

        public void reject() {
            HwLog.d(SocketListenerTransport.TAG, "reject.. ");
            try {
                this.mInnerRequest.reject();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        public boolean equals(NearbyDevice nearbyDevice) {
            return true;
        }

        private NearbySocket createNearbySocketByChannelId(int channelId, int protocol, int timeOut) throws IOException {
            HwLog.d(SocketListenerTransport.TAG, "createNearbySocketByChannelId channelId = " + channelId + ";protocol = " + protocol);
            NearbySocket result;
            switch (channelId) {
                case 1:
                    try {
                        HwLog.i(SocketListenerTransport.TAG, "SecurityType: " + this.mInnerRequest.getSecurityType());
                        NearbyDevice nearbyDevice = this.mInnerRequest.getRemoteNearbyDevice();
                        if (nearbyDevice != null) {
                            HwLog.s(SocketListenerTransport.TAG, "BluetoothMac: " + String.valueOf(nearbyDevice.getBluetoothMac()));
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    result = createTCPNearbySocket(timeOut);
                    if (result != null) {
                        return result;
                    }
                    throw new SocketException("Socket is error ");
                case 2:
                    result = SocketListenerTransport.this.mBluetoothSocketTransport.createNearbySocketServer(this);
                    if (result != null) {
                        return result;
                    }
                    throw new SocketException("Socket is error " + getPort());
                case 3:
                    return createP2PNearbySocketFromAccept(protocol, timeOut);
                default:
                    return null;
            }
        }

        private NearbySocket createP2PNearbySocketFromAccept(int protocol, int timeOut) {
            HwLog.d(SocketListenerTransport.TAG, "createP2PNearbySocketFromAccept protocol = " + protocol);
            switch (protocol) {
                case 1:
                    return createTCPNearbySocket(timeOut);
                case 2:
                    return createUDPNearbySocket(timeOut);
                default:
                    return null;
            }
        }

        /* JADX WARNING: Removed duplicated region for block: B:14:0x008d  */
        /* JADX WARNING: Removed duplicated region for block: B:50:0x00d1 A:{SYNTHETIC, Splitter: B:50:0x00d1} */
        /* JADX WARNING: Removed duplicated region for block: B:14:0x008d  */
        /* JADX WARNING: Removed duplicated region for block: B:43:0x00c5 A:{SYNTHETIC, Splitter: B:43:0x00c5} */
        /* JADX WARNING: Removed duplicated region for block: B:33:0x00ad A:{SYNTHETIC, Splitter: B:33:0x00ad} */
        /* JADX WARNING: Removed duplicated region for block: B:23:0x009d A:{SYNTHETIC, Splitter: B:23:0x009d} */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private NearbySocket createTCPNearbySocket(int timeOut) {
            IOException e;
            RemoteException e2;
            Throwable th;
            ServerSocket serverSocket = null;
            String tag = "";
            HwLog.d(SocketListenerTransport.TAG, "createP2PNearbySocketFromAccept..");
            try {
                ServerSocket serverSocket2 = new ServerSocket(0);
                try {
                    TCPNearbySocket result;
                    serverSocket2.setSoTimeout(timeOut);
                    this.mInnerRequest.accept(serverSocket2.getLocalPort());
                    NearbyDevice nearbyDevice = this.mInnerRequest.getRemoteNearbyDevice();
                    int businessId = this.mInnerRequest.getBusinessId();
                    int businessType = this.mInnerRequest.getBusinessType();
                    tag = this.mInnerRequest.getTag();
                    int channelId = this.mInnerRequest.getChannelId();
                    HwLog.e(SocketListenerTransport.TAG, "accept before..");
                    Socket socket = serverSocket2.accept();
                    HwLog.e(SocketListenerTransport.TAG, "accept after..");
                    if (serverSocket2 != null) {
                        try {
                            serverSocket2.close();
                        } catch (IOException e3) {
                            e3.printStackTrace();
                        }
                    }
                    try {
                        result = new TCPNearbySocket(nearbyDevice, businessId, businessType, channelId, tag, this.mInnerRequest.getInnerNearbySocket());
                        try {
                            result.setSecurityType(this.mInnerRequest.getSecurityType());
                        } catch (RemoteException e4) {
                            e2 = e4;
                            e2.printStackTrace();
                            if (result != null) {
                            }
                            return result;
                        }
                    } catch (RemoteException e5) {
                        e2 = e5;
                        result = null;
                        e2.printStackTrace();
                        if (result != null) {
                        }
                        return result;
                    }
                    if (result != null) {
                        result.setSocket(socket);
                    }
                    return result;
                } catch (SocketTimeoutException e6) {
                    serverSocket = serverSocket2;
                    HwLog.e(SocketListenerTransport.TAG, "createTCPNearbySocket socket(1.0) connect timeout!");
                    if (serverSocket != null) {
                        try {
                            serverSocket.close();
                        } catch (IOException e32) {
                            e32.printStackTrace();
                        }
                    }
                    return null;
                } catch (RemoteException e7) {
                    e2 = e7;
                    serverSocket = serverSocket2;
                    e2.printStackTrace();
                    if (serverSocket != null) {
                        try {
                            serverSocket.close();
                        } catch (IOException e322) {
                            e322.printStackTrace();
                        }
                    }
                    return null;
                } catch (IOException e8) {
                    e322 = e8;
                    serverSocket = serverSocket2;
                    try {
                        e322.printStackTrace();
                        if (serverSocket != null) {
                            try {
                                serverSocket.close();
                            } catch (IOException e3222) {
                                e3222.printStackTrace();
                            }
                        }
                        return null;
                    } catch (Throwable th2) {
                        th = th2;
                        if (serverSocket != null) {
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    serverSocket = serverSocket2;
                    if (serverSocket != null) {
                        try {
                            serverSocket.close();
                        } catch (IOException e32222) {
                            e32222.printStackTrace();
                        }
                    }
                    throw th;
                }
            } catch (SocketTimeoutException e9) {
                HwLog.e(SocketListenerTransport.TAG, "createTCPNearbySocket socket(1.0) connect timeout!");
                if (serverSocket != null) {
                }
                return null;
            } catch (RemoteException e10) {
                e2 = e10;
                e2.printStackTrace();
                if (serverSocket != null) {
                }
                return null;
            } catch (IOException e11) {
                e32222 = e11;
                e32222.printStackTrace();
                if (serverSocket != null) {
                }
                return null;
            }
        }

        private NearbySocket createUDPNearbySocket(int timeOut) {
            return null;
        }
    }

    public SocketListenerTransport(SocketListener listener, Looper looper) {
        this.mListener = listener;
        this.mListenerHandler = new Handler(looper) {
            public void handleMessage(Message msg) {
                SocketListenerTransport.this._handleMessage(msg);
            }
        };
    }

    private void _handleMessage(Message msg) {
        HwLog.d(TAG, "_handleMessage: " + msg.toString());
        switch (msg.what) {
            case 1:
                this.mListener.onStatusChange(msg.arg1);
                return;
            case 2:
                HwLog.d(TAG, "TYPE_CONNECT_REQUEST Listener.onConnectRequest");
                this.mListener.onConnectRequest(new ChannelCreateRequestImpl(msg.obj));
                return;
            case 3:
                HwLog.d(TAG, "TYPE_HWSHARE_I_CONNECT_REQUEST createNearbySocketClient");
                InternalNearbySocket innerSocket = msg.obj;
                this.mPasspharse = msg.getData().getString(MSG_KEY_PASSPHARSE);
                createNearbySocketClient(innerSocket);
                return;
            case 4:
                HwLog.d(TAG, "TYPE_THREAD_RESPONSE_CLIENT");
                NearbySocket nearbySocket = msg.obj;
                if (this.mListener instanceof SocketBackwardCompatible) {
                    SocketBackwardCompatible listener = this.mListener;
                    HwLog.d(TAG, "Listener.onOldVerConnect mPasspharse = " + this.mPasspharse);
                    listener.onOldVerConnect(nearbySocket, this.mPasspharse);
                    return;
                }
                return;
            default:
                HwLog.e(TAG, "Unknow message id:" + msg.what + ", can not be here!");
                return;
        }
    }

    public void onStatusChange(int state) {
        HwLog.d(TAG, "onStatusChange state = " + state);
        Message msg = Message.obtain();
        msg.what = 1;
        msg.arg1 = state;
        if (!this.mListenerHandler.sendMessage(msg)) {
            HwLog.e(TAG, "onStatusChange: handler quitting,remove the listener. ");
        }
    }

    public void onConnectRequest(IInternalChannelCreateRequest request) {
        HwLog.d(TAG, "onConnectRequest");
        Message msg = Message.obtain();
        msg.what = 2;
        msg.obj = request;
        HwLog.e(TAG, "onConnectRequest: come in.");
        if (!this.mListenerHandler.sendMessage(msg)) {
            HwLog.e(TAG, "onConnectRequest: handler quitting,remove the listener. ");
        }
    }

    public void onHwShareIConnectRequest(InternalNearbySocket socket, String passpharse) {
        HwLog.d(TAG, "onHwShareIConnectRequest");
        Message msg = Message.obtain();
        msg.what = 3;
        msg.obj = socket;
        Bundle bundle = new Bundle();
        bundle.putString(MSG_KEY_PASSPHARSE, passpharse);
        msg.setData(bundle);
        HwLog.e(TAG, "onHwShareConnectRequest: come in.");
        if (!this.mListenerHandler.sendMessage(msg)) {
            HwLog.e(TAG, "onConnectRequest: handler quitting,remove the listener. ");
        }
    }

    private void createNearbySocketClient(final InternalNearbySocket innerSocket) {
        HwLog.d(TAG, "createNearbySocketClient");
        new Thread(new Runnable() {
            public void run() {
                Socket socket = new Socket();
                InternalNearbySocket iSocket = innerSocket;
                int protocol = -1;
                try {
                    socket.bind(new InetSocketAddress(iSocket.getLocalIpAddress(), 0));
                    socket.connect(new InetSocketAddress(iSocket.getIpAddress(), iSocket.getPort()), SocketListenerTransport.SOCKET_TIMEOUT);
                    protocol = innerSocket.getProtocol();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (RemoteException e2) {
                    e2.printStackTrace();
                }
                try {
                    socket.setKeepAlive(true);
                } catch (SocketException e3) {
                    e3.printStackTrace();
                }
                Message msg = Message.obtain();
                msg.what = 4;
                HwLog.d(SocketListenerTransport.TAG, "Create SocketClient success protocol = " + protocol);
                switch (protocol) {
                    case 1:
                        TCPNearbySocket result = new TCPNearbySocket(innerSocket);
                        result.setSocket(socket);
                        msg.obj = result;
                        break;
                }
                if (!SocketListenerTransport.this.mListenerHandler.sendMessage(msg)) {
                    HwLog.e(SocketListenerTransport.TAG, "createP2PNearbySocketClient : handler quitting,remove the listener. ");
                }
                HwLog.d(SocketListenerTransport.TAG, "createP2PNearbySocketClient: success.");
            }
        }).start();
    }
}
