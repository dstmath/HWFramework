package com.huawei.nearbysdk;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import com.huawei.nearbysdk.IInternalSocketListener;
import com.huawei.nearbysdk.NearbyConfig;
import com.huawei.nearbysdk.util.Util;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class SocketListenerTransport extends IInternalSocketListener.Stub {
    private static final String MSG_KEY_IP = "IP";
    private static final String MSG_KEY_PASSPHARSE = "PASSPHARSE";
    private static final int SOCKET_TIMEOUT = 30000;
    static final String TAG = "SocketListenerTransport";
    private static final int TYPE_CONNECT_REQUEST = 2;
    private static final int TYPE_HWSHARE_I_CONNECT_REQUEST = 3;
    private static final int TYPE_STATUS_CHANGED = 1;
    private static final int TYPE_THREAD_RESPONSE_CLIENT = 4;
    /* access modifiers changed from: private */
    public final BluetoothSocketTransport mBluetoothSocketTransport;
    private SocketListener mListener;
    /* access modifiers changed from: private */
    public final Handler mListenerHandler;
    private String mPasspharse = null;

    class ChannelCreateRequestImpl implements ChannelCreateRequest {
        IInternalChannelCreateRequest mInnerRequest;

        public ChannelCreateRequestImpl(IInternalChannelCreateRequest innerRequest) {
            this.mInnerRequest = innerRequest;
        }

        public int getBusinessId() {
            try {
                return this.mInnerRequest.getBusinessId();
            } catch (RemoteException e) {
                HwLog.e(SocketListenerTransport.TAG, "getBusinessId() fail: " + e);
                return -1;
            }
        }

        public int getSecurityType() {
            try {
                return this.mInnerRequest.getSecurityType();
            } catch (RemoteException e) {
                HwLog.e(SocketListenerTransport.TAG, "getSecurityType() fail: " + e);
                return 0;
            }
        }

        public String getTag() {
            try {
                return this.mInnerRequest.getTag();
            } catch (RemoteException e) {
                HwLog.e(SocketListenerTransport.TAG, "getTag() fail: " + e);
                return "";
            }
        }

        public NearbyConfig.BusinessTypeEnum getBusinessType() {
            try {
                return NearbySDKUtils.getEnumFromInt(this.mInnerRequest.getBusinessType());
            } catch (RemoteException e) {
                HwLog.e(SocketListenerTransport.TAG, "getBusinessType fail: " + e);
                return null;
            }
        }

        public int getChannelId() {
            try {
                return this.mInnerRequest.getChannelId();
            } catch (RemoteException e) {
                HwLog.e(SocketListenerTransport.TAG, "getChannelId fail: " + e);
                return -1;
            }
        }

        public String getServiceUuid() {
            try {
                return this.mInnerRequest.getServiceUuid();
            } catch (RemoteException e) {
                HwLog.e(SocketListenerTransport.TAG, "getServiceUuid fail: " + e);
                return "";
            }
        }

        public int getPort() {
            try {
                return this.mInnerRequest.getPort();
            } catch (RemoteException e) {
                HwLog.e(SocketListenerTransport.TAG, "getPort fail: " + e);
                return -1;
            }
        }

        public NearbyDevice getRemoteNearbyDevice() {
            NearbyDevice result = null;
            try {
                result = this.mInnerRequest.getRemoteNearbyDevice();
            } catch (RemoteException e) {
                HwLog.e(SocketListenerTransport.TAG, "getRemoteNearbyDevice fail: " + e);
            }
            String summary = "null";
            if (result != null) {
                summary = result.getSummary();
            }
            HwLog.i(SocketListenerTransport.TAG, "getRemoteNearbyDevice result=" + result + " summary[" + summary.length() + "]=" + summary.hashCode());
            return result;
        }

        public void accept(int port) {
            try {
                this.mInnerRequest.accept(port);
            } catch (RemoteException e) {
                HwLog.e(SocketListenerTransport.TAG, "accept fail: RemoteException");
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
                HwLog.e(SocketListenerTransport.TAG, "acceptTimer fail: " + e);
            }
            return createNearbySocketByChannelId(channelId, protocol, timeOut);
        }

        public void reject() {
            HwLog.d(SocketListenerTransport.TAG, "reject.. ");
            try {
                this.mInnerRequest.reject();
            } catch (RemoteException e) {
                HwLog.e(SocketListenerTransport.TAG, "reject fail: RemoteException");
            }
        }

        public boolean equals(NearbyDevice nearbyDevice) {
            return true;
        }

        private NearbySocket createNearbySocketByChannelId(int channelId, int protocol, int timeOut) throws IOException {
            HwLog.d(SocketListenerTransport.TAG, "createNearbySocketByChannelId channelId = " + channelId + ";protocol = " + protocol);
            switch (channelId) {
                case 1:
                    try {
                        HwLog.i(SocketListenerTransport.TAG, "SecurityType: " + this.mInnerRequest.getSecurityType());
                        NearbyDevice nearbyDevice = this.mInnerRequest.getRemoteNearbyDevice();
                        if (nearbyDevice != null) {
                            HwLog.d(SocketListenerTransport.TAG, "BluetoothMac: " + Util.toFrontHalfString(nearbyDevice.getBluetoothMac()));
                        }
                    } catch (RemoteException e) {
                        HwLog.e(SocketListenerTransport.TAG, "RemoteException " + e);
                    }
                    NearbySocket result = createTCPNearbySocket(timeOut);
                    if (result != null) {
                        return result;
                    }
                    throw new SocketException("Socket is error ");
                case 2:
                    NearbySocket result2 = SocketListenerTransport.this.mBluetoothSocketTransport.createNearbySocketServer(this);
                    if (result2 != null) {
                        return result2;
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

        /* JADX WARNING: Removed duplicated region for block: B:108:0x01b1 A[SYNTHETIC, Splitter:B:108:0x01b1] */
        /* JADX WARNING: Removed duplicated region for block: B:115:0x01d3 A[SYNTHETIC, Splitter:B:115:0x01d3] */
        /* JADX WARNING: Removed duplicated region for block: B:86:0x014c A[SYNTHETIC, Splitter:B:86:0x014c] */
        /* JADX WARNING: Removed duplicated region for block: B:97:0x0186 A[SYNTHETIC, Splitter:B:97:0x0186] */
        private NearbySocket createTCPNearbySocket(int timeOut) {
            NearbyDevice nearbyDevice;
            int businessId;
            int businessType;
            String tag;
            NearbyDevice nearbyDevice2;
            NearbyDevice nearbyDevice3;
            int localPort = -1;
            ServerSocket serverSocket = null;
            Socket socket = null;
            NearbyDevice nearbyDevice4 = null;
            HwLog.d(SocketListenerTransport.TAG, "createP2PNearbySocketFromAccept..");
            try {
                serverSocket = new ServerSocket(0);
                try {
                    serverSocket.setSoTimeout(timeOut);
                    localPort = serverSocket.getLocalPort();
                    this.mInnerRequest.accept(localPort);
                    nearbyDevice = this.mInnerRequest.getRemoteNearbyDevice();
                } catch (SocketTimeoutException e) {
                    e = e;
                    SocketTimeoutException socketTimeoutException = e;
                    HwLog.e(SocketListenerTransport.TAG, "createTCPNearbySocket socket(1.0) connect timeout!");
                    if (serverSocket != null) {
                    }
                    return null;
                } catch (RemoteException e2) {
                    e = e2;
                    HwLog.e(SocketListenerTransport.TAG, "createTCPNearbySocket RemoteException " + e);
                    if (serverSocket != null) {
                    }
                    return null;
                } catch (IOException e3) {
                    e = e3;
                    try {
                        HwLog.e(SocketListenerTransport.TAG, "createTCPNearbySocket IOException " + e);
                        if (serverSocket != null) {
                        }
                        return null;
                    } catch (Throwable th) {
                        th = th;
                    }
                }
                try {
                    businessId = this.mInnerRequest.getBusinessId();
                    try {
                        businessType = this.mInnerRequest.getBusinessType();
                        try {
                            tag = this.mInnerRequest.getTag();
                        } catch (SocketTimeoutException e4) {
                            e = e4;
                            nearbyDevice4 = nearbyDevice;
                            int i = businessId;
                            int i2 = businessType;
                            SocketTimeoutException socketTimeoutException2 = e;
                            HwLog.e(SocketListenerTransport.TAG, "createTCPNearbySocket socket(1.0) connect timeout!");
                            if (serverSocket != null) {
                            }
                            return null;
                        } catch (RemoteException e5) {
                            e = e5;
                            nearbyDevice4 = nearbyDevice;
                            int i3 = businessId;
                            int i4 = businessType;
                            HwLog.e(SocketListenerTransport.TAG, "createTCPNearbySocket RemoteException " + e);
                            if (serverSocket != null) {
                            }
                            return null;
                        } catch (IOException e6) {
                            e = e6;
                            nearbyDevice4 = nearbyDevice;
                            int i5 = businessId;
                            int i6 = businessType;
                            HwLog.e(SocketListenerTransport.TAG, "createTCPNearbySocket IOException " + e);
                            if (serverSocket != null) {
                            }
                            return null;
                        } catch (Throwable th2) {
                            th = th2;
                            int i7 = businessId;
                            int i8 = businessType;
                            Throwable th3 = th;
                            if (serverSocket != null) {
                            }
                            throw th3;
                        }
                    } catch (SocketTimeoutException e7) {
                        e = e7;
                        nearbyDevice4 = nearbyDevice;
                        int i9 = businessId;
                        SocketTimeoutException socketTimeoutException22 = e;
                        HwLog.e(SocketListenerTransport.TAG, "createTCPNearbySocket socket(1.0) connect timeout!");
                        if (serverSocket != null) {
                            try {
                                serverSocket.close();
                            } catch (IOException e8) {
                                IOException iOException = e8;
                                HwLog.e(SocketListenerTransport.TAG, "serverSocket close fail: " + e8);
                            }
                        }
                        return null;
                    } catch (RemoteException e9) {
                        e = e9;
                        nearbyDevice4 = nearbyDevice;
                        int i10 = businessId;
                        HwLog.e(SocketListenerTransport.TAG, "createTCPNearbySocket RemoteException " + e);
                        if (serverSocket != null) {
                            try {
                                serverSocket.close();
                            } catch (IOException e10) {
                                IOException iOException2 = e10;
                                HwLog.e(SocketListenerTransport.TAG, "serverSocket close fail: " + e10);
                            }
                        }
                        return null;
                    } catch (IOException e11) {
                        e = e11;
                        nearbyDevice4 = nearbyDevice;
                        int i11 = businessId;
                        HwLog.e(SocketListenerTransport.TAG, "createTCPNearbySocket IOException " + e);
                        if (serverSocket != null) {
                            try {
                                serverSocket.close();
                            } catch (IOException e12) {
                                IOException iOException3 = e12;
                                HwLog.e(SocketListenerTransport.TAG, "serverSocket close fail: " + e12);
                            }
                        }
                        return null;
                    } catch (Throwable th4) {
                        th = th4;
                        int i12 = businessId;
                        Throwable th32 = th;
                        if (serverSocket != null) {
                            try {
                                serverSocket.close();
                            } catch (IOException e13) {
                                IOException iOException4 = e13;
                                HwLog.e(SocketListenerTransport.TAG, "serverSocket close fail: " + e13);
                            }
                        }
                        throw th32;
                    }
                } catch (SocketTimeoutException e14) {
                    e = e14;
                    nearbyDevice4 = nearbyDevice;
                    SocketTimeoutException socketTimeoutException222 = e;
                    HwLog.e(SocketListenerTransport.TAG, "createTCPNearbySocket socket(1.0) connect timeout!");
                    if (serverSocket != null) {
                    }
                    return null;
                } catch (RemoteException e15) {
                    e = e15;
                    nearbyDevice4 = nearbyDevice;
                    HwLog.e(SocketListenerTransport.TAG, "createTCPNearbySocket RemoteException " + e);
                    if (serverSocket != null) {
                    }
                    return null;
                } catch (IOException e16) {
                    e = e16;
                    nearbyDevice4 = nearbyDevice;
                    HwLog.e(SocketListenerTransport.TAG, "createTCPNearbySocket IOException " + e);
                    if (serverSocket != null) {
                    }
                    return null;
                } catch (Throwable th5) {
                    th = th5;
                    Throwable th322 = th;
                    if (serverSocket != null) {
                    }
                    throw th322;
                }
                try {
                    int channelId = this.mInnerRequest.getChannelId();
                    try {
                        HwLog.e(SocketListenerTransport.TAG, "accept before..");
                        socket = serverSocket.accept();
                        HwLog.e(SocketListenerTransport.TAG, "accept after..");
                        try {
                            serverSocket.close();
                        } catch (IOException e17) {
                            IOException iOException5 = e17;
                            HwLog.e(SocketListenerTransport.TAG, "serverSocket close fail: " + e17);
                        }
                        TCPNearbySocket result = null;
                        try {
                            TCPNearbySocket tCPNearbySocket = new TCPNearbySocket(nearbyDevice, businessId, businessType, channelId, tag, this.mInnerRequest.getInnerNearbySocket());
                            result = tCPNearbySocket;
                            result.setSecurityType(this.mInnerRequest.getSecurityType());
                        } catch (RemoteException e18) {
                            HwLog.e(SocketListenerTransport.TAG, "RemoteException: " + e18);
                        }
                        if (result != null) {
                            result.setSocket(socket);
                        }
                        return result;
                    } catch (SocketTimeoutException e19) {
                        e = e19;
                        nearbyDevice2 = nearbyDevice;
                        int i13 = businessId;
                        int i14 = businessType;
                        int i15 = channelId;
                    } catch (RemoteException e20) {
                        e = e20;
                        nearbyDevice3 = nearbyDevice;
                        int i16 = businessId;
                        int i17 = businessType;
                        int i18 = channelId;
                        HwLog.e(SocketListenerTransport.TAG, "createTCPNearbySocket RemoteException " + e);
                        if (serverSocket != null) {
                        }
                        return null;
                    } catch (IOException e21) {
                        e = e21;
                        nearbyDevice4 = nearbyDevice;
                        int i19 = businessId;
                        int i20 = businessType;
                        int i21 = channelId;
                        HwLog.e(SocketListenerTransport.TAG, "createTCPNearbySocket IOException " + e);
                        if (serverSocket != null) {
                        }
                        return null;
                    } catch (Throwable th6) {
                        th = th6;
                        Socket socket2 = socket;
                        int i22 = businessId;
                        int i23 = businessType;
                        int i24 = channelId;
                        Throwable th3222 = th;
                        if (serverSocket != null) {
                        }
                        throw th3222;
                    }
                } catch (SocketTimeoutException e22) {
                    e = e22;
                    nearbyDevice2 = nearbyDevice;
                    int i25 = businessId;
                    int i26 = businessType;
                    SocketTimeoutException socketTimeoutException2222 = e;
                    HwLog.e(SocketListenerTransport.TAG, "createTCPNearbySocket socket(1.0) connect timeout!");
                    if (serverSocket != null) {
                    }
                    return null;
                } catch (RemoteException e23) {
                    e = e23;
                    nearbyDevice3 = nearbyDevice;
                    int i27 = businessId;
                    int i28 = businessType;
                    HwLog.e(SocketListenerTransport.TAG, "createTCPNearbySocket RemoteException " + e);
                    if (serverSocket != null) {
                    }
                    return null;
                } catch (IOException e24) {
                    e = e24;
                    nearbyDevice4 = nearbyDevice;
                    int i29 = businessId;
                    int i30 = businessType;
                    HwLog.e(SocketListenerTransport.TAG, "createTCPNearbySocket IOException " + e);
                    if (serverSocket != null) {
                    }
                    return null;
                } catch (Throwable th7) {
                    th = th7;
                    int i31 = businessId;
                    int i32 = businessType;
                    Throwable th32222 = th;
                    if (serverSocket != null) {
                    }
                    throw th32222;
                }
            } catch (SocketTimeoutException e25) {
                e = e25;
                int i33 = timeOut;
                SocketTimeoutException socketTimeoutException22222 = e;
                HwLog.e(SocketListenerTransport.TAG, "createTCPNearbySocket socket(1.0) connect timeout!");
                if (serverSocket != null) {
                }
                return null;
            } catch (RemoteException e26) {
                e = e26;
                int i34 = timeOut;
                HwLog.e(SocketListenerTransport.TAG, "createTCPNearbySocket RemoteException " + e);
                if (serverSocket != null) {
                }
                return null;
            } catch (IOException e27) {
                e = e27;
                int i35 = timeOut;
                HwLog.e(SocketListenerTransport.TAG, "createTCPNearbySocket IOException " + e);
                if (serverSocket != null) {
                }
                return null;
            } catch (Throwable th8) {
                th = th8;
                int i36 = timeOut;
                Throwable th322222 = th;
                if (serverSocket != null) {
                }
                throw th322222;
            }
        }

        private NearbySocket createUDPNearbySocket(int timeOut) {
            return null;
        }
    }

    public SocketListenerTransport(SocketListener listener, Looper looper) {
        this.mBluetoothSocketTransport = new BluetoothSocketTransport(looper);
        this.mListener = listener;
        this.mListenerHandler = new Handler(looper) {
            public void handleMessage(Message msg) {
                SocketListenerTransport.this._handleMessage(msg);
            }
        };
    }

    /* access modifiers changed from: private */
    public void _handleMessage(Message msg) {
        HwLog.d(TAG, "_handleMessage: " + msg.toString());
        switch (msg.what) {
            case 1:
                this.mListener.onStatusChange(msg.arg1);
                return;
            case 2:
                HwLog.d(TAG, "TYPE_CONNECT_REQUEST Listener.onConnectRequest");
                this.mListener.onConnectRequest(new ChannelCreateRequestImpl((IInternalChannelCreateRequest) msg.obj));
                return;
            case 3:
                HwLog.d(TAG, "TYPE_HWSHARE_I_CONNECT_REQUEST createNearbySocketClient");
                this.mPasspharse = msg.getData().getString(MSG_KEY_PASSPHARSE);
                createNearbySocketClient((InternalNearbySocket) msg.obj);
                return;
            case 4:
                HwLog.d(TAG, "TYPE_THREAD_RESPONSE_CLIENT");
                NearbySocket nearbySocket = (NearbySocket) msg.obj;
                if (this.mListener instanceof SocketBackwardCompatible) {
                    HwLog.d(TAG, "Listener.onOldVerConnect mPasspharse = " + this.mPasspharse);
                    ((SocketBackwardCompatible) this.mListener).onOldVerConnect(nearbySocket, this.mPasspharse);
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
                    HwLog.e(SocketListenerTransport.TAG, "socket bind or connect error: " + e);
                } catch (RemoteException e2) {
                    HwLog.e(SocketListenerTransport.TAG, "socket bind or connect error: " + e2);
                }
                try {
                    socket.setKeepAlive(true);
                } catch (SocketException e3) {
                    HwLog.e(SocketListenerTransport.TAG, "socket setKeepAlive error: " + e3);
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
