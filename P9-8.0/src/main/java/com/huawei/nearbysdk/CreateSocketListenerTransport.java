package com.huawei.nearbysdk;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import com.huawei.nearbysdk.ICreateSocketListener.Stub;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class CreateSocketListenerTransport extends Stub {
    public static final int CANCEL_SUCCESS = 4;
    public static final int CREATE_NULL_SOCKET_ERROR = 2;
    public static final int MESSAGE_HANDLER_ERROR = 3;
    static final String TAG = "CreateSocketListenerTransport";
    public static final int TIME_LEFT_ERROR = 1;
    private static final int TYPE_CREATE_FAIL = 2;
    private static final int TYPE_HWSHARE_I_REMOTE = 3;
    private static final int TYPE_INNERSOCKET_CREATE_SUCCESS = 1;
    private static final int TYPE_THREAD_RESPONSE_CLIENT = 4;
    private static final int TYPE_THREAD_RESPONSE_SERVER = 5;
    private boolean hadCancel = false;
    private final BluetoothSocketTransport mBluetoothSocketTransport;
    private CreateSocketListener mListener;
    private final Handler mListenerHandler;
    private NearbyAdapter mNearbyAdapter;
    private ServerSocket mServerSocket;
    private long mStartTime = 0;
    private int mTimeOut = 0;

    class BRCreateSocketCB implements ICreateSocketCallback {
        BRCreateSocketCB() {
        }

        public void onStatusChange(int status, @Nullable NearbySocket nearbySocket, int arg) {
            if (status == 0) {
                CreateSocketListenerTransport.this.sendMessage(4, nearbySocket, arg, status);
            } else {
                CreateSocketListenerTransport.this.onCreateFail(NearbyConfig.ERROR_CLIENT_CONNECT_FAILED);
            }
        }
    }

    class CreateP2PClientRunable implements Runnable {
        InternalNearbySocket innerSocket;
        int protocol;
        int timeLeft;

        public CreateP2PClientRunable(InternalNearbySocket socket, int pro, int timeout) {
            this.innerSocket = socket;
            this.protocol = pro;
            this.timeLeft = timeout;
        }

        public void run() {
            Message msg = Message.obtain();
            boolean hasException = false;
            Socket socket = new Socket();
            InternalNearbySocket iSocket = this.innerSocket;
            try {
                int connectTimeLeft = this.timeLeft;
                if (this.timeLeft == 0) {
                    connectTimeLeft = 1;
                }
                socket.bind(new InetSocketAddress(iSocket.getLocalIpAddress(), 0));
                socket.connect(new InetSocketAddress(iSocket.getIpAddress(), iSocket.getPort()), connectTimeLeft);
            } catch (IOException e) {
                hasException = true;
                HwLog.e(CreateSocketListenerTransport.TAG, "createP2PNearbySocketClient : IOException");
                e.printStackTrace();
            } catch (RemoteException e2) {
                hasException = true;
                HwLog.e(CreateSocketListenerTransport.TAG, "createP2PNearbySocketClient : RemoteException");
                e2.printStackTrace();
            }
            try {
                socket.setKeepAlive(true);
            } catch (SocketException e3) {
                hasException = true;
                HwLog.e(CreateSocketListenerTransport.TAG, "createP2PNearbySocketClient : SocketException");
                e3.printStackTrace();
            }
            if (hasException) {
                try {
                    this.innerSocket.close();
                } catch (RemoteException e22) {
                    e22.printStackTrace();
                }
                msg.what = 2;
                msg.arg1 = NearbyConfig.ERROR_CREATE_THREAD_EXCEPTION;
                if (!CreateSocketListenerTransport.this.mListenerHandler.sendMessage(msg)) {
                    HwLog.e(CreateSocketListenerTransport.TAG, "createP2PNearbySocketClient : handler quitting,remove the listener. ");
                }
            }
            Message new_msg = Message.obtain();
            new_msg.what = 4;
            new_msg.arg1 = this.protocol;
            HwLog.d(CreateSocketListenerTransport.TAG, "Thread send msg.Create socketClient success");
            switch (this.protocol) {
                case 1:
                    TCPNearbySocket result = new TCPNearbySocket(this.innerSocket);
                    result.setSocket(socket);
                    new_msg.obj = result;
                    break;
            }
            if (!CreateSocketListenerTransport.this.mListenerHandler.sendMessage(new_msg)) {
                HwLog.e(CreateSocketListenerTransport.TAG, "createP2PNearbySocketClient : handler quitting,remove the listener. ");
            }
            HwLog.d(CreateSocketListenerTransport.TAG, "createP2PNearbySocketClient: success.");
        }
    }

    class CreateP2PServerRunable implements Runnable {
        InternalNearbySocket innerSocket;
        int protocol;
        int soTimeout;

        public CreateP2PServerRunable(InternalNearbySocket socket, int pro, int timeout) {
            this.innerSocket = socket;
            this.protocol = pro;
            this.soTimeout = timeout;
        }

        public void run() {
            Message msg = Message.obtain();
            boolean hasException = false;
            try {
                CreateSocketListenerTransport.this.mServerSocket = new ServerSocket(this.innerSocket.getPort());
                CreateSocketListenerTransport.this.mServerSocket.setSoTimeout(this.soTimeout);
                HwLog.d(CreateSocketListenerTransport.TAG, "setSoTimeout  soTimeout = " + this.soTimeout);
                Socket socket = CreateSocketListenerTransport.this.mServerSocket.accept();
                if (socket != null) {
                    msg.what = 5;
                    HwLog.d(CreateSocketListenerTransport.TAG, "Thread send msg.Create socketServer success");
                    switch (this.protocol) {
                        case 1:
                            TCPServerNearbySocket result = new TCPServerNearbySocket(this.innerSocket);
                            result.setServerSocket(CreateSocketListenerTransport.this.mServerSocket);
                            result.setSocket(socket);
                            msg.obj = result;
                            break;
                    }
                    if (!CreateSocketListenerTransport.this.mListenerHandler.sendMessage(msg)) {
                        HwLog.e(CreateSocketListenerTransport.TAG, "createP2PNearbySocketServer : handler quitting,remove the listener. ");
                    }
                }
            } catch (SocketTimeoutException e) {
                hasException = true;
                HwLog.e(CreateSocketListenerTransport.TAG, "createP2PNearbySocketServer connect timeout!");
            } catch (IOException e2) {
                hasException = true;
                HwLog.e(CreateSocketListenerTransport.TAG, "createP2PNearbySocketServer ServerSocket closed.");
            } catch (RemoteException e3) {
                hasException = true;
                HwLog.e(CreateSocketListenerTransport.TAG, "createP2PNearbySocketServer ServerSocket RemoteException.");
                e3.printStackTrace();
            }
            HwLog.d(CreateSocketListenerTransport.TAG, "createP2PNearbySocketServer: finish.");
            if (hasException) {
                CreateSocketListenerTransport.this.mServerSocket = null;
                Message new_msg = Message.obtain();
                try {
                    this.innerSocket.close();
                } catch (RemoteException e32) {
                    e32.printStackTrace();
                }
                new_msg.what = 2;
                new_msg.arg1 = NearbyConfig.ERROR_CREATE_THREAD_EXCEPTION;
                if (!CreateSocketListenerTransport.this.mListenerHandler.sendMessage(new_msg)) {
                    HwLog.e(CreateSocketListenerTransport.TAG, "createP2PNearbySocketServer : handler quitting,remove the listener. ");
                }
            }
        }
    }

    static class TCPServerNearbySocket extends TCPNearbySocket {
        private ServerSocket serverSocket = null;

        public TCPServerNearbySocket(InternalNearbySocket innerSocket) {
            super(innerSocket);
            HwLog.d(CreateSocketListenerTransport.TAG, "TCPServerNearbySocket construct");
        }

        public ServerSocket getServerSocket() {
            return this.serverSocket;
        }

        public void setServerSocket(ServerSocket socket) {
            HwLog.d(CreateSocketListenerTransport.TAG, "TCPServerNearbySocket setServerSocket socket:" + socket);
            this.serverSocket = socket;
        }

        public boolean close() {
            HwLog.d(CreateSocketListenerTransport.TAG, "TCPServerNearbySocket close");
            boolean result = super.close();
            if (this.serverSocket != null) {
                try {
                    this.serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return result;
        }
    }

    public void setTimeOut(int timeOut) {
        HwLog.d(TAG, "timeOut = " + timeOut);
        this.mTimeOut = timeOut;
    }

    public void setStartTime(long startTime) {
        HwLog.d(TAG, "startTime = " + startTime);
        this.mStartTime = startTime;
    }

    CreateSocketListenerTransport(NearbyAdapter neabyAdapter, CreateSocketListener listener, Looper looper) {
        this.mNearbyAdapter = neabyAdapter;
        this.mBluetoothSocketTransport = new BluetoothSocketTransport();
        this.mListener = listener;
        this.mListenerHandler = new Handler(looper) {
            public void handleMessage(Message msg) {
                CreateSocketListenerTransport.this._handleMessage(msg);
            }
        };
    }

    private void _handleMessage(Message msg) {
        HwLog.d(TAG, "_handleMessage: " + msg.toString());
        switch (msg.what) {
            case 1:
                HwLog.d(TAG, "TYPE_INNERSOCKET_CREATE_SUCCESS createNearbySocketByChannelId()");
                InternalNearbySocket innerSocket = msg.obj;
                if (!this.hadCancel) {
                    createNearbySocketByChannelId(innerSocket, msg.arg1);
                    break;
                }
                try {
                    innerSocket.close();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                onCreateFailCallBack(4);
                return;
            case 2:
                HwLog.d(TAG, "TYPE_CREATE_FAIL Listener.onCreateFail");
                onCreateFailCallBack(msg.arg1);
                break;
            case 3:
                HwLog.d(TAG, "TYPE_HWSHARE_I_REMOTE createP2PNearbySocketServer");
                InternalNearbySocket socket = msg.obj;
                if (socket == null) {
                    onCreateFailCallBack(2);
                } else if (this.hadCancel) {
                    try {
                        socket.close();
                    } catch (RemoteException e2) {
                        e2.printStackTrace();
                    }
                    onCreateFailCallBack(4);
                    return;
                }
                createP2PNearbySocketServer(socket, 1, msg.arg1);
                break;
            case 4:
                HwLog.d(TAG, "TYPE_THREAD_RESPONSE_CLIENT Listener.onCreateSuccess");
                NearbySocket nearbySocket = msg.obj;
                if (nearbySocket != null) {
                    if (!this.hadCancel) {
                        onCreateSuccessCallBack(nearbySocket);
                        break;
                    } else {
                        onCreateFailCallBack(4);
                        break;
                    }
                }
                onCreateFailCallBack(2);
                break;
            case 5:
                HwLog.d(TAG, "TYPE_THREAD_RESPONSE_SERVER ready to listener.onOldVerConnect");
                NearbySocket result = msg.obj;
                if (result == null) {
                    onCreateFailCallBack(2);
                } else if (this.hadCancel) {
                    result.close();
                    onCreateFailCallBack(4);
                    return;
                }
                if (this.mListener instanceof SocketBackwardCompatible) {
                    this.mListener.onOldVerConnect(result, null);
                    HwLog.d(TAG, "onOldVerConnect: return nearbySocket success.");
                    break;
                }
                break;
            default:
                HwLog.e(TAG, "Unknow message id:" + msg.what + ", can not be here!");
                break;
        }
    }

    private void onCreateFailCallBack(int errorCode) {
        HwLog.e(TAG, "errorCode = " + errorCode);
        clear();
        this.mListener.onCreateFail(errorCode);
        if (this.mNearbyAdapter != null) {
            this.mNearbyAdapter.removeCreateSocketListener(this.mListener);
        }
    }

    private void onCreateSuccessCallBack(NearbySocket nearbySocket) {
        HwLog.e(TAG, "onCreateSuccessCallBack");
        this.mListener.onCreateSuccess(nearbySocket);
        if (this.mNearbyAdapter != null) {
            this.mNearbyAdapter.removeCreateSocketListener(this.mListener);
        }
    }

    private void createNearbySocketByChannelId(InternalNearbySocket innerSocket, int connectTimeLeft) {
        HwLog.d(TAG, "createNearbySocketByChannelId");
        try {
            int channelId = innerSocket.getChannelId();
            int protocol = innerSocket.getProtocol();
            HwLog.d(TAG, "check channelId = " + channelId);
            switch (channelId) {
                case 2:
                    this.mBluetoothSocketTransport.createNearbySocketClient(innerSocket, new BRCreateSocketCB());
                    break;
                case 3:
                    createP2PNearbySocketClient(innerSocket, protocol, connectTimeLeft);
                    break;
                default:
                    try {
                        innerSocket.close();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    onCreateFailCallBack(NearbyConfig.ERROR_UNSUPPORT_CHANNLE);
                    break;
            }
        } catch (RemoteException e2) {
            onCreateFailCallBack(NearbyConfig.ERROR_REMOTE_DATA_EXCEPTION);
            e2.printStackTrace();
        }
    }

    private void createP2PNearbySocketClient(InternalNearbySocket innerSocket, int protocol, int timeLeft) {
        HwLog.d(TAG, "createP2PNearbySocketClient protocol = " + protocol);
        new Thread(new CreateP2PClientRunable(innerSocket, protocol, timeLeft)).start();
    }

    private void createP2PNearbySocketServer(InternalNearbySocket innerSocket, int protocol, int soTimeout) {
        HwLog.d(TAG, "createP2PNearbySocketServer protocol = " + protocol);
        new Thread(new CreateP2PServerRunable(innerSocket, protocol, soTimeout)).start();
    }

    void sendMessage(int msgWhat, Object obj, int para, int status) {
        Message msg = this.mListenerHandler.obtainMessage(msgWhat, para, status, obj);
        if (!this.mListenerHandler.sendMessage(msg)) {
            HwLog.e(TAG, "sendMessage fail with msg=" + msg.toString());
        }
    }

    public void onCreateSuccess(InternalNearbySocket socket) {
        HwLog.d(TAG, "onCreateSuccess socket = " + socket);
        int timeLeft = getTimeLeft();
        if (timeLeft == 1) {
            try {
                socket.close();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            onCreateFailCallBack(NearbyConfig.ERROR_TIME_OUT);
            HwLog.d(TAG, "onCreateSuccess TIME_OUT mStartTime = " + this.mStartTime + " TimeOut = " + this.mTimeOut + " timeLeft = " + timeLeft);
            return;
        }
        Message msg = Message.obtain();
        msg.what = 1;
        msg.obj = socket;
        msg.arg1 = timeLeft;
        if (!this.mListenerHandler.sendMessage(msg)) {
            messageError(socket);
        }
    }

    private int getTimeLeft() {
        long timeNow = System.currentTimeMillis();
        if (this.mStartTime > timeNow) {
            HwLog.d(TAG, "getTimeLeft get wrong time: mStartTime = " + this.mStartTime + " timeNow = " + timeNow);
        }
        long timeLeft = (((long) this.mTimeOut) + this.mStartTime) - timeNow;
        if (timeLeft < 0) {
            return 1;
        }
        return (int) timeLeft;
    }

    public void onCreateFail(int failCode) {
        HwLog.d(TAG, "onCreateFail failCode = " + failCode);
        Message msg = Message.obtain();
        msg.what = 2;
        msg.arg1 = failCode;
        if (!this.mListenerHandler.sendMessage(msg)) {
            messageError(null);
        }
    }

    public void onHwShareIRemote(InternalNearbySocket socket) {
        HwLog.d(TAG, "onHwShareIRemote socket = " + socket);
        int timeLeft = getTimeLeft();
        if (timeLeft == 1) {
            try {
                socket.close();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            onCreateFailCallBack(NearbyConfig.ERROR_TIME_OUT);
            HwLog.d(TAG, "onCreateSuccess TIME_OUT mStartTime = " + this.mStartTime + " TimeOut = " + this.mTimeOut + " timeLeft = " + timeLeft);
            return;
        }
        Message msg = Message.obtain();
        msg.what = 3;
        msg.obj = socket;
        msg.arg1 = timeLeft;
        if (!this.mListenerHandler.sendMessage(msg)) {
            messageError(socket);
        }
    }

    public void cancel() {
        this.hadCancel = true;
        HwLog.d(TAG, "CreateSocketListenerTransport get cancle CMD.");
        closeServerSocket();
    }

    private void messageError(InternalNearbySocket socket) {
        HwLog.e(TAG, "messageError: handler quitting,remove the listener. ");
        if (socket != null) {
            try {
                socket.close();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        onCreateFailCallBack(3);
    }

    private void clear() {
        HwLog.d(TAG, "clear");
        closeServerSocket();
        resetOpenTimeOut();
    }

    private void closeServerSocket() {
        if (this.mServerSocket != null) {
            try {
                this.mServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Throwable th) {
                this.mServerSocket = null;
            }
            this.mServerSocket = null;
        }
    }

    private void resetOpenTimeOut() {
        this.mStartTime = 0;
    }
}
