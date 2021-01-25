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

public class SocketListenerTransport extends IInternalSocketListener.Stub {
    private static final String MSG_KEY_IP = "IP";
    private static final String MSG_KEY_PASSPHARSE = "PASSPHARSE";
    private static final int SOCKET_TIMEOUT = 30000;
    static final String TAG = "SocketListenerTransport";
    private static final int TYPE_CONNECT_REQUEST = 2;
    private static final int TYPE_HWSHARE_I_CONNECT_REQUEST = 3;
    private static final int TYPE_STATUS_CHANGED = 1;
    private static final int TYPE_THREAD_RESPONSE_CLIENT = 4;
    private final BluetoothSocketTransport mBluetoothSocketTransport;
    private SocketListener mListener;
    private final Handler mListenerHandler;
    private String mPasspharse = null;

    public SocketListenerTransport(SocketListener listener, Looper looper) {
        this.mBluetoothSocketTransport = new BluetoothSocketTransport(looper);
        this.mListener = listener;
        this.mListenerHandler = new Handler(looper) {
            /* class com.huawei.nearbysdk.SocketListenerTransport.AnonymousClass1 */

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                SocketListenerTransport.this._handleMessage(msg);
            }
        };
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void _handleMessage(Message msg) {
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
                InternalNearbySocket innerSocket = (InternalNearbySocket) msg.obj;
                Bundle bundle = msg.getData();
                if (bundle == null) {
                    HwLog.e(TAG, "bundle is null.");
                    return;
                }
                this.mPasspharse = bundle.getString(MSG_KEY_PASSPHARSE);
                createNearbySocketClient(innerSocket);
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

    @Override // com.huawei.nearbysdk.IInternalSocketListener
    public void onStatusChange(int state) {
        HwLog.d(TAG, "onStatusChange state = " + state);
        Message msg = Message.obtain();
        msg.what = 1;
        msg.arg1 = state;
        if (!this.mListenerHandler.sendMessage(msg)) {
            HwLog.e(TAG, "onStatusChange: handler quitting,remove the listener. ");
        }
    }

    @Override // com.huawei.nearbysdk.IInternalSocketListener
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

    @Override // com.huawei.nearbysdk.IInternalSocketListener
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
            /* class com.huawei.nearbysdk.SocketListenerTransport.AnonymousClass2 */

            @Override // java.lang.Runnable
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

    /* access modifiers changed from: package-private */
    public class ChannelCreateRequestImpl implements ChannelCreateRequest {
        IInternalChannelCreateRequest mInnerRequest;

        public ChannelCreateRequestImpl(IInternalChannelCreateRequest innerRequest) {
            this.mInnerRequest = innerRequest;
        }

        @Override // com.huawei.nearbysdk.ChannelCreateRequest
        public int getBusinessId() {
            try {
                return this.mInnerRequest.getBusinessId();
            } catch (RemoteException e) {
                HwLog.e(SocketListenerTransport.TAG, "getBusinessId() fail: " + e);
                return -1;
            }
        }

        @Override // com.huawei.nearbysdk.ChannelCreateRequest
        public int getSecurityType() {
            try {
                return this.mInnerRequest.getSecurityType();
            } catch (RemoteException e) {
                HwLog.e(SocketListenerTransport.TAG, "getSecurityType() fail: " + e);
                return 0;
            }
        }

        @Override // com.huawei.nearbysdk.ChannelCreateRequest
        public String getTag() {
            try {
                return this.mInnerRequest.getTag();
            } catch (RemoteException e) {
                HwLog.e(SocketListenerTransport.TAG, "getTag() fail: " + e);
                return "";
            }
        }

        @Override // com.huawei.nearbysdk.ChannelCreateRequest
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

        @Override // com.huawei.nearbysdk.ChannelCreateRequest
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

        @Override // com.huawei.nearbysdk.ChannelCreateRequest
        public NearbySocket accept() throws IOException {
            return acceptTimer(0);
        }

        @Override // com.huawei.nearbysdk.ChannelCreateRequest
        public NearbySocket acceptTimer(int timeOut) throws IOException {
            int channelId = -1;
            int protocol = -1;
            InternalNearbySocket internalNearbySocket = null;
            HwLog.e(SocketListenerTransport.TAG, "accept.. timeOut = " + timeOut);
            try {
                channelId = this.mInnerRequest.getChannelId();
                protocol = this.mInnerRequest.getProtocol();
                internalNearbySocket = this.mInnerRequest.getInnerNearbySocket();
            } catch (RemoteException e) {
                HwLog.e(SocketListenerTransport.TAG, "acceptTimer fail: " + e);
            }
            return createNearbySocketByChannelId(channelId, protocol, timeOut, internalNearbySocket);
        }

        @Override // com.huawei.nearbysdk.ChannelCreateRequest
        public void reject() {
            HwLog.d(SocketListenerTransport.TAG, "reject.. ");
            try {
                this.mInnerRequest.reject();
            } catch (RemoteException e) {
                HwLog.e(SocketListenerTransport.TAG, "reject fail: RemoteException");
            }
        }

        @Override // com.huawei.nearbysdk.ChannelCreateRequest
        public boolean equals(NearbyDevice nearbyDevice) {
            return true;
        }

        @Override // com.huawei.nearbysdk.ChannelCreateRequest
        public void busy() {
            HwLog.d(SocketListenerTransport.TAG, "busy.. ");
            try {
                this.mInnerRequest.busy();
            } catch (RemoteException e) {
                HwLog.e(SocketListenerTransport.TAG, "busy fail: RemoteException");
            }
        }

        private NearbySocket createNearbySocketByChannelId(int channelId, int protocol, int timeOut, InternalNearbySocket innerNearbySocket) throws IOException {
            HwLog.d(SocketListenerTransport.TAG, "createNearbySocketByChannelId channelId = " + channelId + ";protocol = " + protocol);
            if (channelId != 6) {
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
                        NearbySocket result2 = SocketListenerTransport.this.mBluetoothSocketTransport.createNearbySocketServer(this, timeOut, innerNearbySocket);
                        if (result2 != null) {
                            return result2;
                        }
                        throw new SocketException("Socket is error " + getPort());
                    case 3:
                        break;
                    default:
                        return null;
                }
            }
            return createP2PNearbySocketFromAccept(protocol, timeOut);
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

        /* JADX WARNING: Removed duplicated region for block: B:37:0x0126 A[SYNTHETIC, Splitter:B:37:0x0126] */
        private NearbySocket createTCPNearbySocket(int timeOut) {
            TCPNearbySocket result;
            InternalNearbySocket internalNearbySocket;
            RemoteException e;
            Exception e2;
            int localPort = -1;
            ServerSocket serverSocket = null;
            Socket socket = null;
            NearbyDevice nearbyDevice = null;
            int businessId = -1;
            int businessType = -1;
            String tag = "";
            int channelId = -1;
            boolean isSocketExceptionFlag = false;
            boolean isNearbySocktExceptionFlag = false;
            HwLog.d(SocketListenerTransport.TAG, "createP2PNearbySocketFromAccept..");
            try {
                serverSocket = new ServerSocket(0);
                try {
                    serverSocket.setSoTimeout(timeOut);
                    localPort = serverSocket.getLocalPort();
                    this.mInnerRequest.accept(localPort);
                    nearbyDevice = this.mInnerRequest.getRemoteNearbyDevice();
                    businessId = this.mInnerRequest.getBusinessId();
                    businessType = this.mInnerRequest.getBusinessType();
                    tag = this.mInnerRequest.getTag();
                    channelId = this.mInnerRequest.getChannelId();
                    HwLog.e(SocketListenerTransport.TAG, "accept before..");
                    socket = serverSocket.accept();
                    HwLog.e(SocketListenerTransport.TAG, "accept after..");
                    try {
                        serverSocket.close();
                    } catch (IOException e3) {
                        HwLog.e(SocketListenerTransport.TAG, "serverSocket close fail: " + e3);
                    }
                } catch (RemoteException | IOException e4) {
                    e2 = e4;
                }
            } catch (RemoteException | IOException e5) {
                e2 = e5;
                isSocketExceptionFlag = true;
                try {
                    HwLog.e(SocketListenerTransport.TAG, "createTCPNearbySocket Exception " + e2);
                    if (serverSocket != null) {
                        try {
                            serverSocket.close();
                        } catch (IOException e6) {
                            HwLog.e(SocketListenerTransport.TAG, "serverSocket close fail: " + e6);
                        }
                    }
                    result = null;
                    internalNearbySocket = this.mInnerRequest.getInnerNearbySocket();
                    try {
                        result = new TCPNearbySocket(nearbyDevice, businessId, businessType, channelId, tag, internalNearbySocket);
                        result.setSecurityType(this.mInnerRequest.getSecurityType());
                    } catch (RemoteException e7) {
                        e = e7;
                    }
                    return getTcpNearbySocket(socket, internalNearbySocket, result, isSocketExceptionFlag, isNearbySocktExceptionFlag);
                } catch (Throwable th) {
                    e = th;
                    if (serverSocket != null) {
                        try {
                            serverSocket.close();
                        } catch (IOException e8) {
                            HwLog.e(SocketListenerTransport.TAG, "serverSocket close fail: " + e8);
                        }
                    }
                    throw e;
                }
            } catch (Throwable th2) {
                e = th2;
                if (serverSocket != null) {
                }
                throw e;
            }
            result = null;
            try {
                internalNearbySocket = this.mInnerRequest.getInnerNearbySocket();
                result = new TCPNearbySocket(nearbyDevice, businessId, businessType, channelId, tag, internalNearbySocket);
                result.setSecurityType(this.mInnerRequest.getSecurityType());
            } catch (RemoteException e9) {
                e = e9;
                internalNearbySocket = null;
                HwLog.e(SocketListenerTransport.TAG, "RemoteException: " + e);
                isNearbySocktExceptionFlag = true;
                return getTcpNearbySocket(socket, internalNearbySocket, result, isSocketExceptionFlag, isNearbySocktExceptionFlag);
            }
            return getTcpNearbySocket(socket, internalNearbySocket, result, isSocketExceptionFlag, isNearbySocktExceptionFlag);
        }

        private TCPNearbySocket getTcpNearbySocket(Socket socket, InternalNearbySocket internalNearbySocket, TCPNearbySocket result, boolean isSocketExceptionFlag, boolean isNearbySocktExceptionFlag) {
            if (isSocketExceptionFlag && socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    HwLog.e(SocketListenerTransport.TAG, "IOException: socket.close()");
                }
            }
            if (isNearbySocktExceptionFlag && internalNearbySocket != null) {
                try {
                    internalNearbySocket.close();
                } catch (RemoteException e2) {
                    HwLog.e(SocketListenerTransport.TAG, "RemoteException: " + e2);
                }
            }
            if (isSocketExceptionFlag || isNearbySocktExceptionFlag) {
                HwLog.d(SocketListenerTransport.TAG, "isSocketExceptionFlag is " + isSocketExceptionFlag);
                return null;
            }
            result.setSocket(socket);
            return result;
        }

        private NearbySocket createUDPNearbySocket(int timeOut) {
            return null;
        }
    }
}
