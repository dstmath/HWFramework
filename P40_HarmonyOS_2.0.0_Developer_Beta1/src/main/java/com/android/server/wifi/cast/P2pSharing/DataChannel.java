package com.android.server.wifi.cast.P2pSharing;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.wifi.HwHiLog;
import com.android.server.wifi.hwcoex.HiCoexUtils;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/* access modifiers changed from: package-private */
public class DataChannel {
    static final int BASE = 2000;
    private static final int BUFFER_SIZE = 1024;
    private static final int CMD_CLIENT_SOCKET_CONNECTED = 2051;
    private static final int CMD_REPORT_EVENT = 2054;
    private static final int CMD_SERVER_SOCKET_ACCEPTED = 2050;
    private static final long CONNECT_RETRY_WAIT_TIME = 200;
    private static final int CORE_POOL_SIZE = 5;
    private static final int DEFAULT_STATE = 0;
    private static final long DELAY_TIME = 1000;
    private static final int DYNAMIC_ACCEPTED_STATE = 11;
    private static final int DYNAMIC_ACCEPTING_STATE = 9;
    private static final int DYNAMIC_CONNECTED_STATE = 10;
    private static final int DYNAMIC_CONNECTING_STATE = 8;
    private static final int DYNAMIC_CREATING_STATE = 7;
    static final int EVENT_DYNAMIC_CHANNEL_CLOSED = 2003;
    static final int EVENT_DYNAMIC_CHANNEL_CREATE_FAIL = 2008;
    static final int EVENT_DYNAMIC_CHANNEL_CREATE_SUCCESS = 2002;
    static final int EVENT_DYNAMIC_CONNECT_TIMEOUT = 2006;
    static final int EVENT_FIXED_CHANNEL_CLOSED = 2004;
    static final int EVENT_FIXED_CHANNEL_CREATE_FAIL = 2007;
    static final int EVENT_FIXED_CHANNEL_CREATE_SUCCESS = 2001;
    static final int EVENT_FIXED_CONNECT_TIMEOUT = 2005;
    private static final int FIXED_ACCEPTED_STATE = 5;
    private static final int FIXED_ACCEPTING_STATE = 3;
    private static final long FIXED_CHANNEL_CREATE_TIMEOUT = 10000;
    private static final int FIXED_CONNECTED_STATE = 4;
    private static final int FIXED_CONNECTING_STATE = 2;
    private static final int FIXED_CREATING_STATE = 1;
    private static final int FIXED_SOCKET_TIMEOUT = 10000;
    private static final byte[] HEART_BEAT_DATA = new byte[0];
    private static final int HEART_BEAT_INTERVAL_MILLIS = 10000;
    private static final int HEART_BEAT_TIMEOUT_MILLIS = 15000;
    private static final long KEEP_ALIVE_TIME = 60;
    private static final int KEY_NEGOTIATED_STATE = 6;
    private static final int MAX_CONNECTION_NUM = 1;
    private static final int MAX_POOL_SIZE = 32;
    private static final int MSG_KEY_NEGOTIATION_SUCCESS = 2156;
    private static final int P2P_SOCKET_PORT = 18888;
    private static final int RECEIVE_THRESHOLD = 50;
    private static final int RETRY_TIME = 20;
    private static final int SEND_AES_KEY = 2154;
    private static final int SEND_AES_KEY_RESPONSE = 2155;
    private static final int SEND_RSA_PUBLIC_KEY = 2152;
    private static final int SEND_RSA_PUBLIC_KEY_RESPONSE = 2153;
    private static final int SEND_SIGN_PUB_KEY = 2150;
    private static final int SEND_SIGN_PUB_KEY_RESPONSE = 2151;
    private static final int SOCKET_TIMEOUT = 60000;
    private static final String TAG = "P2pSharing:DataChannel";
    private static final int TYPE_BUSINESS_DATA = 2102;
    private static final int TYPE_REPLY_HEART_BEAT = 2101;
    private static final int TYPE_SEND_HEART_BEAT = 2100;
    private static final int TYPE_SIZE = 8;
    private byte[] mAesKey;
    private long mDataTransDelay;
    private ServerSocket mDynamicServerSocket;
    private Socket mDynamicSocket;
    private EncryptRsa mEncryptRsa;
    private long mFixChannelStartTime;
    private String mFixedPeerIp = "";
    private ServerSocket mFixedServerSocket;
    private Socket mFixedSocket;
    private DataChannelHandler mHandler;
    private final Runnable mHeartBeatTask = new Runnable() {
        /* class com.android.server.wifi.cast.P2pSharing.DataChannel.AnonymousClass3 */

        @Override // java.lang.Runnable
        public void run() {
            if (DataChannel.this.mHandler == null) {
                DataChannel.this.handleException();
                return;
            }
            DataChannel dataChannel = DataChannel.this;
            if (!dataChannel.isSocketOpen(dataChannel.mDynamicSocket)) {
                HwHiLog.w(DataChannel.TAG, false, "Client socket null", new Object[0]);
                DataChannel.this.handleException();
                return;
            }
            byte[] encryptedHeartBeats = EncryptGcm.encrypt(Utils.packageData(DataChannel.TYPE_SEND_HEART_BEAT, DataChannel.HEART_BEAT_DATA), DataChannel.this.mAesKey, Utils.convertInt2Byte(DataChannel.TYPE_SEND_HEART_BEAT));
            if (Utils.isEmptyByteArray(encryptedHeartBeats)) {
                HwHiLog.w(DataChannel.TAG, false, "Heart beat package encrypt failed", new Object[0]);
                DataChannel.this.handleException();
                return;
            }
            DataChannel.this.mHeartBeatTimeStamp = SystemClock.elapsedRealtime();
            DataChannel.this.sendSocketData(Utils.packageData(DataChannel.TYPE_BUSINESS_DATA, encryptedHeartBeats));
            DataChannel.this.mHandler.postDelayed(this, DataChannel.FIXED_CHANNEL_CREATE_TIMEOUT);
            DataChannel.this.mHandler.postDelayed(DataChannel.this.mHeartBeatTimeoutTask, 15000);
        }
    };
    private long mHeartBeatTimeStamp;
    private final Runnable mHeartBeatTimeoutTask = new Runnable() {
        /* class com.android.server.wifi.cast.P2pSharing.$$Lambda$DataChannel$SoYsWICXEBPSMymRcjFl38z7QHY */

        @Override // java.lang.Runnable
        public final void run() {
            DataChannel.this.lambda$new$1$DataChannel();
        }
    };
    private BufferedInputStream mInputStream;
    private ChannelListener mListener;
    private BufferedOutputStream mOutputStream;
    private byte[] mPublicKey;
    private Runnable mReadControlRunnable = new Runnable() {
        /* class com.android.server.wifi.cast.P2pSharing.DataChannel.AnonymousClass1 */

        @Override // java.lang.Runnable
        public void run() {
            if (DataChannel.this.mHandler == null || DataChannel.this.mReceiveNum.get() > 50) {
                DataChannel.this.handleException();
                return;
            }
            DataChannel.this.mReceiveNum.set(0);
            DataChannel.this.mHandler.postDelayed(this, DataChannel.DELAY_TIME);
        }
    };
    private final Runnable mReaderRunnable = new Runnable() {
        /* class com.android.server.wifi.cast.P2pSharing.DataChannel.AnonymousClass2 */

        @Override // java.lang.Runnable
        public void run() {
            if (DataChannel.this.mHandler == null) {
                DataChannel.this.handleException();
                return;
            }
            Socket socket = null;
            if (DataChannel.this.stateEquals(5) || DataChannel.this.stateEquals(4)) {
                socket = DataChannel.this.mFixedSocket;
            }
            if (DataChannel.this.stateEquals(11) || DataChannel.this.stateEquals(10)) {
                socket = DataChannel.this.mDynamicSocket;
            }
            if (!DataChannel.this.isSocketOpen(socket)) {
                HwHiLog.w(DataChannel.TAG, false, "Socket closed", new Object[0]);
                DataChannel.this.handleException();
                return;
            }
            try {
                HwHiLog.d(DataChannel.TAG, false, "Receive data ready", new Object[0]);
                DataChannel.this.mInputStream = new BufferedInputStream(socket.getInputStream());
                DataChannel.this.mReceiveNum.set(0);
                DataChannel.this.mHandler.postDelayed(DataChannel.this.mReadControlRunnable, DataChannel.DELAY_TIME);
                byte[] buffer = new byte[1024];
                while (true) {
                    if (DataChannel.this.isSocketOpen(socket)) {
                        int size = DataChannel.this.mInputStream.read(buffer);
                        if (size < 0 && DataChannel.this.stateEquals(9)) {
                            HwHiLog.w(DataChannel.TAG, false, "Client channel closed", new Object[0]);
                            DataChannel.this.closeFixedSocket();
                            break;
                        } else if (size <= 0) {
                            HwHiLog.w(DataChannel.TAG, false, "Read error", new Object[0]);
                            DataChannel.this.handleException();
                            break;
                        } else {
                            DataChannel.this.mReceiveNum.incrementAndGet();
                            byte[] data = new byte[size];
                            System.arraycopy(buffer, 0, data, 0, size);
                            DataChannel.this.handleReceivedData(data);
                        }
                    } else {
                        HwHiLog.w(DataChannel.TAG, false, "Read error: socket closed", new Object[0]);
                        DataChannel.this.handleException();
                        break;
                    }
                }
            } catch (IOException e) {
                HwHiLog.e(DataChannel.TAG, false, "Read error", new Object[0]);
                DataChannel.this.handleException();
            }
        }
    };
    private AtomicInteger mReceiveNum = new AtomicInteger();
    private String mServerIp;
    private Runnable mServerSocketTask = new Runnable() {
        /* class com.android.server.wifi.cast.P2pSharing.$$Lambda$DataChannel$N2Fz8mplFHaX0gYIP8EDCTmMVAw */

        @Override // java.lang.Runnable
        public final void run() {
            DataChannel.this.lambda$new$0$DataChannel();
        }
    };
    private byte[] mSignPublicKey;
    private AtomicInteger mSocketState = new AtomicInteger();
    private ThreadPoolExecutor mThreadPool;

    public /* synthetic */ void lambda$new$0$DataChannel() {
        if (this.mHandler == null || this.mListener == null) {
            HwHiLog.w(TAG, false, "Create server socket error", new Object[0]);
            handleException();
            return;
        }
        try {
            HwHiLog.d(TAG, false, "Start create server", new Object[0]);
            ServerSocket serverSocket = new ServerSocket(stateEquals(1) ? P2P_SOCKET_PORT : 0, 1, InetAddress.getByName(Utils.getP2pIpAddress()));
            serverSocket.setReuseAddress(true);
            boolean isFixedAccepting = this.mSocketState.compareAndSet(1, 3);
            boolean isDynamicAccepting = this.mSocketState.compareAndSet(7, 9);
            if (isFixedAccepting || isDynamicAccepting) {
                if (stateEquals(9)) {
                    this.mListener.onPortGet(serverSocket.getLocalPort());
                }
                serverSocket.setSoTimeout(stateEquals(3) ? HiCoexUtils.TIMEOUT_CONNECT : SOCKET_TIMEOUT);
                if (stateEquals(3)) {
                    this.mFixedServerSocket = serverSocket;
                }
                if (stateEquals(9)) {
                    this.mDynamicServerSocket = serverSocket;
                }
                Socket socket = serverSocket.accept();
                if (socket != null) {
                    HwHiLog.d(TAG, false, "Accept one socket", new Object[0]);
                    serverSocket.setSoTimeout(0);
                    this.mOutputStream = new BufferedOutputStream(socket.getOutputStream());
                    boolean isFixedAccepted = this.mSocketState.compareAndSet(3, 5);
                    boolean isDynamicAccepted = this.mSocketState.compareAndSet(9, 11);
                    if (isFixedAccepted || isDynamicAccepted) {
                        if (stateEquals(5)) {
                            this.mFixedSocket = socket;
                            this.mFixedPeerIp = getPeerDeviceIp(socket);
                        }
                        if (stateEquals(11)) {
                            this.mDynamicSocket = socket;
                            if (!this.mFixedPeerIp.equals(getPeerIp())) {
                                releaseOnException(serverSocket, socket);
                                return;
                            }
                            sendEventMsg(EVENT_DYNAMIC_CHANNEL_CREATE_SUCCESS);
                        }
                        this.mHandler.sendEmptyMessage(CMD_SERVER_SOCKET_ACCEPTED);
                        return;
                    }
                    releaseOnException(serverSocket, socket);
                    return;
                }
                return;
            }
            handleException();
            closeServerSocket(serverSocket);
        } catch (SocketTimeoutException e) {
            HwHiLog.e(TAG, false, "Server socket time out", new Object[0]);
            handleException();
        } catch (IOException e2) {
            HwHiLog.e(TAG, false, "Server socket create fail", new Object[0]);
            releaseOnException(null, null);
        }
    }

    public /* synthetic */ void lambda$new$1$DataChannel() {
        HwHiLog.w(TAG, false, "Heart beat receive timeout", new Object[0]);
        handleException();
    }

    DataChannel(ChannelListener listener) {
        this.mListener = listener;
        this.mHandler = new DataChannelHandler(Looper.getMainLooper());
        this.mThreadPool = new ThreadPoolExecutor(5, 32, KEEP_ALIVE_TIME, TimeUnit.SECONDS, new LinkedBlockingQueue<>(32), new ThreadFactory() {
            /* class com.android.server.wifi.cast.P2pSharing.DataChannel.AnonymousClass4 */
            private final AtomicInteger mThreadNumber = new AtomicInteger(1);

            @Override // java.util.concurrent.ThreadFactory
            public Thread newThread(Runnable runnable) {
                return new Thread(runnable, "P2pSharing-" + this.mThreadNumber.getAndIncrement());
            }
        }, new ThreadPoolExecutor.DiscardOldestPolicy());
        this.mSocketState.set(0);
        this.mEncryptRsa = new EncryptRsa();
    }

    /* access modifiers changed from: package-private */
    public void createFixedSocket(String serverAddress, long retryInterval) {
        if (TextUtils.isEmpty(serverAddress)) {
            HwHiLog.w(TAG, false, "Create fixed socket param is empty", new Object[0]);
            sendEventMsg(EVENT_FIXED_CHANNEL_CREATE_FAIL);
        } else if (!this.mSocketState.compareAndSet(0, 1)) {
            handleException();
        } else {
            this.mServerIp = serverAddress;
            this.mFixChannelStartTime = SystemClock.elapsedRealtime();
            createSocket(P2P_SOCKET_PORT, retryInterval);
        }
    }

    /* access modifiers changed from: package-private */
    public void createDynamicSocket(String serverAddress, int port, long retryInterval) {
        if (TextUtils.isEmpty(serverAddress)) {
            HwHiLog.w(TAG, false, "Address is empty", new Object[0]);
            sendEventMsg(EVENT_DYNAMIC_CHANNEL_CREATE_FAIL);
        } else if (!this.mSocketState.compareAndSet(6, 7)) {
            handleException();
        } else {
            this.mServerIp = serverAddress;
            createSocket(port, retryInterval);
        }
    }

    /* access modifiers changed from: package-private */
    public void createFixedServerSocket() {
        HwHiLog.i(TAG, false, "Create fixed serverSocket", new Object[0]);
        if (!this.mSocketState.compareAndSet(0, 1)) {
            handleException();
        } else {
            createServerSocket();
        }
    }

    /* access modifiers changed from: package-private */
    public void createDynamicServerSocket() {
        HwHiLog.i(TAG, false, "Create dynamic serverSocket", new Object[0]);
        if (!this.mSocketState.compareAndSet(6, 7)) {
            handleException();
        } else {
            createServerSocket();
        }
    }

    /* access modifiers changed from: package-private */
    public void closeFixedSocket() {
        HwHiLog.i(TAG, false, "Close fixed socket", new Object[0]);
        closeSocket(this.mFixedSocket);
        closeServerSocket(this.mFixedServerSocket);
    }

    /* access modifiers changed from: package-private */
    public void closeDynamicSocket() {
        HwHiLog.i(TAG, false, "Close dynamic socket", new Object[0]);
        closeSocket(this.mDynamicSocket);
        closeServerSocket(this.mDynamicServerSocket);
    }

    /* access modifiers changed from: package-private */
    public boolean send(byte[] data) {
        Socket socket;
        try {
            if (isSocketOpen(this.mFixedSocket)) {
                socket = this.mFixedSocket;
            } else {
                socket = isSocketOpen(this.mDynamicSocket) ? this.mDynamicSocket : this.mFixedSocket;
            }
            if (isSocketOpen(socket)) {
                if (!socket.isOutputShutdown()) {
                    if (this.mOutputStream == null) {
                        this.mOutputStream = new BufferedOutputStream(socket.getOutputStream());
                    }
                    if (Utils.isEmptyByteArray(this.mAesKey)) {
                        HwHiLog.e(TAG, false, "Key is null", new Object[0]);
                        handleException();
                        return false;
                    }
                    sendSocketData(Utils.packageData(TYPE_BUSINESS_DATA, EncryptGcm.encrypt(Utils.packageData(TYPE_BUSINESS_DATA, sign(data)), this.mAesKey, Utils.convertInt2Byte(TYPE_BUSINESS_DATA))));
                    return true;
                }
            }
            HwHiLog.w(TAG, false, "Send fail: socket not available", new Object[0]);
            handleException();
            return false;
        } catch (IOException e) {
            HwHiLog.e(TAG, false, "Write error", new Object[0]);
            handleException();
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public long getDataTransDelay() {
        return this.mDataTransDelay;
    }

    /* access modifiers changed from: package-private */
    public void release() {
        HwHiLog.i(TAG, false, "release", new Object[0]);
        closeFixedSocket();
        closeDynamicSocket();
        stopTimer();
        this.mSocketState.set(0);
        Utils.resetArrays(this.mAesKey);
        Utils.resetArrays(this.mSignPublicKey);
        ThreadPoolExecutor threadPoolExecutor = this.mThreadPool;
        if (threadPoolExecutor != null) {
            threadPoolExecutor.shutdown();
        }
        this.mEncryptRsa.clearKeyStore();
        this.mFixedPeerIp = "";
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startKeyNegotiation() {
        sendSignPublicKey(SEND_SIGN_PUB_KEY);
    }

    private void closeSocket(Socket socket) {
        if (socket == null || socket.isClosed()) {
            HwHiLog.w(TAG, false, "Socket has been closed", new Object[0]);
            return;
        }
        InputStream inputStream = null;
        try {
            InputStream inputStream2 = socket.getInputStream();
            if (inputStream2 != null) {
                inputStream2.close();
            }
            if (inputStream2 != null) {
                try {
                    inputStream2.close();
                } catch (IOException e) {
                    HwHiLog.e(TAG, false, "Close input stream error again", new Object[0]);
                }
            }
        } catch (IOException e2) {
            HwHiLog.e(TAG, false, "Close input stream error", new Object[0]);
            if (0 != 0) {
                inputStream.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    inputStream.close();
                } catch (IOException e3) {
                    HwHiLog.e(TAG, false, "Close input stream error again", new Object[0]);
                }
            }
            throw th;
        }
        OutputStream outputStream = null;
        try {
            OutputStream outputStream2 = socket.getOutputStream();
            if (outputStream2 != null) {
                outputStream2.close();
            }
            if (outputStream2 != null) {
                try {
                    outputStream2.close();
                } catch (IOException e4) {
                    HwHiLog.e(TAG, false, "Close output stream error again", new Object[0]);
                }
            }
        } catch (IOException e5) {
            HwHiLog.e(TAG, false, "Close output stream error", new Object[0]);
            if (0 != 0) {
                outputStream.close();
            }
        } catch (Throwable th2) {
            if (0 != 0) {
                try {
                    outputStream.close();
                } catch (IOException e6) {
                    HwHiLog.e(TAG, false, "Close output stream error again", new Object[0]);
                }
            }
            throw th2;
        }
        safelyClose(socket);
        DataChannelHandler dataChannelHandler = this.mHandler;
        if (dataChannelHandler != null) {
            dataChannelHandler.removeCallbacks(this.mReadControlRunnable);
        }
        ThreadPoolExecutor threadPoolExecutor = this.mThreadPool;
        if (threadPoolExecutor != null) {
            threadPoolExecutor.remove(this.mReaderRunnable);
        }
    }

    private void safelyClose(Socket socket) {
        if (socket != null && !socket.isClosed()) {
            try {
                if (!socket.isInputShutdown()) {
                    socket.shutdownInput();
                }
                if (!socket.isOutputShutdown()) {
                    socket.shutdownOutput();
                }
                socket.close();
                HwHiLog.i(TAG, false, "Socket closed, state: " + this.mSocketState.get(), new Object[0]);
                try {
                    socket.close();
                } catch (IOException e) {
                    HwHiLog.e(TAG, false, "Safely close error", new Object[0]);
                }
            } catch (IOException e2) {
                HwHiLog.e(TAG, false, "Safely close error", new Object[0]);
                socket.close();
            } catch (Throwable th) {
                try {
                    socket.close();
                } catch (IOException e3) {
                    HwHiLog.e(TAG, false, "Safely close error", new Object[0]);
                }
                throw th;
            }
        }
    }

    private void closeServerSocket(ServerSocket serverSocket) {
        if (serverSocket == null || serverSocket.isClosed()) {
            HwHiLog.w(TAG, false, "Server socket has been closed", new Object[0]);
            return;
        }
        try {
            serverSocket.close();
            try {
                serverSocket.close();
            } catch (IOException e) {
                HwHiLog.e(TAG, false, "Close server socket error", new Object[0]);
            }
        } catch (IOException e2) {
            HwHiLog.e(TAG, false, "Close server socket error", new Object[0]);
            serverSocket.close();
        } catch (Throwable th) {
            try {
                serverSocket.close();
            } catch (IOException e3) {
                HwHiLog.e(TAG, false, "Close server socket error", new Object[0]);
            }
            throw th;
        }
    }

    private void releaseOnException(ServerSocket serverSocket, Socket socket) {
        handleException();
        safelyClose(socket);
        closeServerSocket(serverSocket);
    }

    private void createServerSocket() {
        ThreadPoolExecutor threadPoolExecutor = this.mThreadPool;
        if (threadPoolExecutor == null) {
            HwHiLog.w(TAG, false, "Create server socket error", new Object[0]);
            handleException();
            return;
        }
        threadPoolExecutor.execute(this.mServerSocketTask);
    }

    private void createSocket(int port, long retryInterval) {
        ThreadPoolExecutor threadPoolExecutor = this.mThreadPool;
        if (threadPoolExecutor == null) {
            HwHiLog.w(TAG, false, "Create socket error", new Object[0]);
            handleException();
            return;
        }
        threadPoolExecutor.execute(new Runnable(port, retryInterval) {
            /* class com.android.server.wifi.cast.P2pSharing.$$Lambda$DataChannel$0BfpHWk2zx4uYLixxvhGFj0sTU */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ long f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // java.lang.Runnable
            public final void run() {
                DataChannel.this.lambda$createSocket$2$DataChannel(this.f$1, this.f$2);
            }
        });
    }

    public /* synthetic */ void lambda$createSocket$2$DataChannel(int port, long retryInterval) {
        this.mSocketState.compareAndSet(1, 2);
        this.mSocketState.compareAndSet(7, 8);
        String clientIp = Utils.getP2pIpAddress();
        int retryTime = 0;
        boolean isConnected = false;
        while (true) {
            if (retryTime >= 20) {
                break;
            } else if (doConnect(clientIp, this.mServerIp, port)) {
                isConnected = true;
                break;
            } else {
                HwHiLog.i(TAG, false, "Connect failed, retry", new Object[0]);
                try {
                    Thread.sleep(retryInterval <= 0 ? CONNECT_RETRY_WAIT_TIME : retryInterval);
                } catch (InterruptedException e) {
                    HwHiLog.e(TAG, false, "Sleep failed", new Object[0]);
                }
                retryTime++;
            }
        }
        if (!isConnected) {
            HwHiLog.e(TAG, false, "Create socket fail", new Object[0]);
            handleException();
        }
    }

    private boolean doConnect(String clientIp, String serverIp, int serverPort) {
        if (this.mHandler == null) {
            return false;
        }
        HwHiLog.i(TAG, false, "doConnect", new Object[0]);
        Socket socket = new Socket();
        try {
            socket.bind(new InetSocketAddress(clientIp, 0));
            socket.connect(new InetSocketAddress(serverIp, serverPort), HiCoexUtils.TIMEOUT_CONNECT);
            if (!socket.isConnected()) {
                socket.close();
                return false;
            }
            if (this.mOutputStream != null) {
                this.mOutputStream.close();
            }
            this.mOutputStream = new BufferedOutputStream(socket.getOutputStream());
            boolean isFixedConnected = this.mSocketState.compareAndSet(2, 4);
            boolean isDynamicConnected = this.mSocketState.compareAndSet(8, 10);
            if (isFixedConnected || isDynamicConnected) {
                if (stateEquals(4)) {
                    this.mFixedSocket = socket;
                }
                if (stateEquals(10)) {
                    this.mDynamicSocket = socket;
                    socket.setKeepAlive(true);
                    sendEventMsg(EVENT_DYNAMIC_CHANNEL_CREATE_SUCCESS);
                }
                this.mHandler.sendEmptyMessage(CMD_CLIENT_SOCKET_CONNECTED);
                return true;
            }
            safelyClose(socket);
            return false;
        } catch (IOException e) {
            safelyClose(socket);
            return false;
        }
    }

    /* access modifiers changed from: private */
    public class DataChannelHandler extends Handler {
        DataChannelHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg == null || (msg.what != DataChannel.CMD_REPORT_EVENT && DataChannel.this.mThreadPool == null)) {
                HwHiLog.e(DataChannel.TAG, false, "handleMessage error", new Object[0]);
                return;
            }
            HwHiLog.d(DataChannel.TAG, false, "handleMessage:" + msg.what, new Object[0]);
            int i = msg.what;
            if (i == DataChannel.CMD_SERVER_SOCKET_ACCEPTED) {
                DataChannel.this.mThreadPool.execute(DataChannel.this.mReaderRunnable);
            } else if (i == DataChannel.CMD_CLIENT_SOCKET_CONNECTED) {
                HwHiLog.i(DataChannel.TAG, false, "Client connected", new Object[0]);
                DataChannel.this.mThreadPool.execute(DataChannel.this.mReaderRunnable);
                if (DataChannel.this.stateEquals(4)) {
                    DataChannel.this.startKeyNegotiation();
                }
                if (DataChannel.this.stateEquals(10)) {
                    DataChannel.this.mHandler.postDelayed(DataChannel.this.mHeartBeatTask, DataChannel.FIXED_CHANNEL_CREATE_TIMEOUT);
                }
            } else if (i == DataChannel.CMD_REPORT_EVENT && DataChannel.this.mListener != null) {
                DataChannel.this.mListener.onChannelEvent(((Integer) msg.obj).intValue());
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleReceivedData(byte[] data) {
        if (!Utils.isDataValid(data)) {
            handleException();
            return;
        }
        int type = Utils.getDataType(data);
        if (isTimeout(type)) {
            HwHiLog.w(TAG, false, "Create fix channel time out", new Object[0]);
            handleException();
            return;
        }
        byte[] dataValues = Utils.getDataValue(data);
        if (type != TYPE_BUSINESS_DATA) {
            switch (type) {
                case SEND_SIGN_PUB_KEY /* 2150 */:
                    handleSendSignPubKey(dataValues);
                    return;
                case SEND_SIGN_PUB_KEY_RESPONSE /* 2151 */:
                    handleSendSignPubKeyResponse(dataValues);
                    return;
                case SEND_RSA_PUBLIC_KEY /* 2152 */:
                    handleSendRsaPublicKey(dataValues);
                    return;
                case SEND_RSA_PUBLIC_KEY_RESPONSE /* 2153 */:
                    handleSendRsaPubKeyResponse(dataValues);
                    return;
                case SEND_AES_KEY /* 2154 */:
                    handleSendAesKey(dataValues);
                    return;
                case SEND_AES_KEY_RESPONSE /* 2155 */:
                    handleSendAesKeyResponse(dataValues);
                    return;
                default:
                    handleException();
                    return;
            }
        } else {
            handleBusinessData(dataValues);
        }
    }

    private void handleSendSignPubKey(byte[] dataValues) {
        if (Utils.isEmptyByteArray(dataValues)) {
            HwHiLog.w(TAG, false, "DataValues is empty", new Object[0]);
            handleException();
            return;
        }
        this.mSignPublicKey = (byte[]) dataValues.clone();
        sendSignPublicKey(SEND_SIGN_PUB_KEY_RESPONSE);
    }

    private void handleSendSignPubKeyResponse(byte[] dataValues) {
        if (Utils.isEmptyByteArray(dataValues)) {
            handleException();
            return;
        }
        this.mSignPublicKey = (byte[]) dataValues.clone();
        sendRsaPublicKey(SEND_RSA_PUBLIC_KEY);
    }

    private void handleSendRsaPublicKey(byte[] data) {
        byte[] pubKey = verify(data);
        if (Utils.isEmptyByteArray(pubKey)) {
            HwHiLog.w(TAG, false, "Verify failed", new Object[0]);
            handleException();
            return;
        }
        this.mPublicKey = (byte[]) pubKey.clone();
        Utils.resetArrays(pubKey);
        sendRsaPublicKey(SEND_RSA_PUBLIC_KEY_RESPONSE);
    }

    private void handleSendRsaPubKeyResponse(byte[] data) {
        byte[] pubKey = verify(data);
        if (Utils.isEmptyByteArray(pubKey)) {
            HwHiLog.w(TAG, false, "Verify failed", new Object[0]);
            handleException();
            return;
        }
        this.mPublicKey = (byte[]) pubKey.clone();
        Utils.resetArrays(pubKey);
        this.mAesKey = EncryptGcm.getRandomBytes(16);
        byte[] keyAndSignature = sign(this.mAesKey);
        if (Utils.isEmptyByteArray(keyAndSignature)) {
            HwHiLog.w(TAG, false, "Signature failed", new Object[0]);
            handleException();
            return;
        }
        sendSocketData(Utils.packageData(SEND_AES_KEY, this.mEncryptRsa.encryptByteArray(keyAndSignature, this.mPublicKey)));
    }

    private void sendRsaPublicKey(int type) {
        byte[] pubKey = this.mEncryptRsa.getEncryptPublicKey();
        byte[] keyAndSignature = sign(pubKey);
        if (Utils.isEmptyByteArray(pubKey) || Utils.isEmptyByteArray(keyAndSignature)) {
            HwHiLog.w(TAG, false, "Send content is empty", new Object[0]);
            handleException();
            return;
        }
        sendSocketData(Utils.packageData(type, keyAndSignature));
        Utils.resetArrays(pubKey);
        Utils.resetArrays(keyAndSignature);
    }

    private void sendSignPublicKey(int type) {
        byte[] signPublicKey = this.mEncryptRsa.getSignPublicKey();
        if (Utils.isEmptyByteArray(signPublicKey)) {
            HwHiLog.w(TAG, false, "Send sign content is empty", new Object[0]);
            handleException();
            return;
        }
        sendSocketData(Utils.packageData(type, signPublicKey));
        Utils.resetArrays(signPublicKey);
    }

    private void handleSendAesKey(byte[] data) {
        byte[] decryptedData = this.mEncryptRsa.decryptByteArray(data);
        if (Utils.isEmptyByteArray(decryptedData)) {
            HwHiLog.w(TAG, false, "Decrypt error", new Object[0]);
            handleException();
            return;
        }
        this.mAesKey = verify(decryptedData);
        if (Utils.isEmptyByteArray(this.mAesKey)) {
            HwHiLog.e(TAG, false, "Verify error", new Object[0]);
            handleException();
            return;
        }
        byte[] keyNegotiateResult = sign(Utils.convertInt2Byte(MSG_KEY_NEGOTIATION_SUCCESS));
        if (Utils.isEmptyByteArray(keyNegotiateResult)) {
            HwHiLog.e(TAG, false, "Signature error", new Object[0]);
            handleException();
            return;
        }
        sendSocketData(Utils.packageData(SEND_AES_KEY_RESPONSE, EncryptGcm.encrypt(keyNegotiateResult, this.mAesKey, Utils.convertInt2Byte(TYPE_BUSINESS_DATA))));
        this.mSocketState.compareAndSet(5, 6);
        Utils.resetArrays(keyNegotiateResult);
    }

    private void handleSendAesKeyResponse(byte[] data) {
        if (Utils.isEmptyByteArray(data)) {
            HwHiLog.w(TAG, false, "Response data is empty", new Object[0]);
            handleException();
            return;
        }
        byte[] recvData = verify(EncryptGcm.decrypt(data, this.mAesKey));
        if (Utils.isEmptyByteArray(recvData)) {
            HwHiLog.w(TAG, false, "Verify error" + recvData.length, new Object[0]);
            handleException();
            return;
        }
        Utils.resetArrays(this.mPublicKey);
        if (Utils.convertByte2Int(recvData) != MSG_KEY_NEGOTIATION_SUCCESS) {
            HwHiLog.w(TAG, false, "Key negotiation failed", new Object[0]);
            handleException();
            return;
        }
        this.mSocketState.compareAndSet(4, 6);
        sendEventMsg(EVENT_FIXED_CHANNEL_CREATE_SUCCESS);
    }

    private boolean isTimeout(int msgType) {
        return (msgType == SEND_SIGN_PUB_KEY_RESPONSE || msgType == SEND_RSA_PUBLIC_KEY_RESPONSE || msgType == SEND_AES_KEY_RESPONSE) && SystemClock.elapsedRealtime() - this.mFixChannelStartTime > FIXED_CHANNEL_CREATE_TIMEOUT;
    }

    private void handleBusinessData(byte[] data) {
        if (Utils.isEmptyByteArray(this.mAesKey)) {
            HwHiLog.w(TAG, false, "Decrypt key is empty", new Object[0]);
            handleException();
            return;
        }
        byte[] decryptedData = EncryptGcm.decrypt(data, this.mAesKey);
        if (!Utils.isDataValid(decryptedData)) {
            handleException();
            return;
        }
        byte[] dataValues = Utils.getDataValue(decryptedData);
        switch (Utils.getDataType(decryptedData)) {
            case TYPE_SEND_HEART_BEAT /* 2100 */:
                handleHearBeatFromClient();
                return;
            case TYPE_REPLY_HEART_BEAT /* 2101 */:
                this.mDataTransDelay = (SystemClock.elapsedRealtime() - this.mHeartBeatTimeStamp) / 2;
                this.mHandler.removeCallbacks(this.mHeartBeatTimeoutTask);
                return;
            case TYPE_BUSINESS_DATA /* 2102 */:
                HwHiLog.d(TAG, false, "Business data received", new Object[0]);
                byte[] verifiedData = verify(dataValues);
                if (this.mListener != null && !Utils.isEmptyByteArray(verifiedData)) {
                    this.mListener.onDataReceived(verifiedData);
                    return;
                }
                return;
            default:
                return;
        }
    }

    private void handleHearBeatFromClient() {
        this.mHandler.removeCallbacks(this.mHeartBeatTimeoutTask);
        if (!isSocketOpen(this.mDynamicSocket)) {
            handleException();
            return;
        }
        byte[] encryptedHearts = EncryptGcm.encrypt(Utils.packageData(TYPE_REPLY_HEART_BEAT, HEART_BEAT_DATA), this.mAesKey, Utils.convertInt2Byte(TYPE_REPLY_HEART_BEAT));
        if (Utils.isEmptyByteArray(encryptedHearts)) {
            HwHiLog.w(TAG, false, "Heart beat reply failed", new Object[0]);
            handleException();
            return;
        }
        sendSocketData(Utils.packageData(TYPE_BUSINESS_DATA, encryptedHearts));
        this.mHandler.postDelayed(this.mHeartBeatTimeoutTask, 15000);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendSocketData(byte[] data) {
        ThreadPoolExecutor threadPoolExecutor = this.mThreadPool;
        if (threadPoolExecutor == null || this.mOutputStream == null) {
            HwHiLog.w(TAG, false, "Send data error", new Object[0]);
            handleException();
            return;
        }
        threadPoolExecutor.execute(new Runnable(data) {
            /* class com.android.server.wifi.cast.P2pSharing.$$Lambda$DataChannel$hmVXJCrW1MO7FqK854q9tH3DCPM */
            private final /* synthetic */ byte[] f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                DataChannel.this.lambda$sendSocketData$3$DataChannel(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$sendSocketData$3$DataChannel(byte[] data) {
        try {
            this.mOutputStream.write(data);
            this.mOutputStream.flush();
        } catch (IOException e) {
            HwHiLog.e(TAG, false, "Write error", new Object[0]);
            handleException();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isSocketOpen(Socket socket) {
        return socket != null && !socket.isClosed() && socket.isConnected();
    }

    private void stopTimer() {
        HwHiLog.i(TAG, false, "Stop heart beat timer", new Object[0]);
        DataChannelHandler dataChannelHandler = this.mHandler;
        if (dataChannelHandler != null) {
            dataChannelHandler.removeCallbacks(this.mHeartBeatTask);
            this.mHandler.removeCallbacks(this.mHeartBeatTimeoutTask);
            this.mHandler.removeCallbacks(this.mReadControlRunnable);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleException() {
        HwHiLog.i(TAG, false, "Handle exception, state:" + this.mSocketState.get(), new Object[0]);
        switch (this.mSocketState.get()) {
            case 1:
            case 2:
            case 3:
                closeFixedSocket();
                sendEventMsg(EVENT_FIXED_CHANNEL_CREATE_FAIL);
                return;
            case 4:
            case 5:
            case 6:
                closeFixedSocket();
                sendEventMsg(EVENT_FIXED_CHANNEL_CLOSED);
                return;
            case 7:
            case 8:
                closeFixedSocket();
                sendEventMsg(EVENT_DYNAMIC_CHANNEL_CREATE_FAIL);
                return;
            case 9:
            case 10:
            case 11:
                closeDynamicSocket();
                stopTimer();
                sendEventMsg(EVENT_DYNAMIC_CHANNEL_CLOSED);
                return;
            default:
                HwHiLog.w(TAG, false, "Invalid state", new Object[0]);
                return;
        }
    }

    private void sendEventMsg(int eventId) {
        Message msg = Message.obtain();
        msg.what = CMD_REPORT_EVENT;
        msg.obj = Integer.valueOf(eventId);
        DataChannelHandler dataChannelHandler = this.mHandler;
        if (dataChannelHandler != null) {
            dataChannelHandler.sendMessage(msg);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean stateEquals(int state) {
        return this.mSocketState.get() == state;
    }

    private byte[] sign(byte[] data) {
        if (Utils.isEmptyByteArray(data)) {
            return new byte[0];
        }
        return Utils.combineData(data, this.mEncryptRsa.sign(data));
    }

    private byte[] verify(byte[] data) {
        if (Utils.isEmptyByteArray(data) || data.length <= 8) {
            return new byte[0];
        }
        byte[] lenBytes = new byte[4];
        System.arraycopy(data, 0, lenBytes, 0, 4);
        int dataLen = Utils.convertByte2Int(lenBytes);
        if (dataLen > data.length - 8) {
            return new byte[0];
        }
        byte[] realData = new byte[dataLen];
        System.arraycopy(data, 4, realData, 0, dataLen);
        System.arraycopy(data, dataLen + 4, lenBytes, 0, 4);
        int signatureLen = Utils.convertByte2Int(lenBytes);
        if (signatureLen > (data.length - 8) - dataLen) {
            return new byte[0];
        }
        byte[] signature = new byte[signatureLen];
        System.arraycopy(data, dataLen + 4 + 4, signature, 0, signatureLen);
        if (this.mEncryptRsa.verify(realData, this.mSignPublicKey, signature)) {
            return realData;
        }
        return new byte[0];
    }

    private String getPeerDeviceIp(Socket socket) {
        if (!isSocketOpen(socket) || socket.getInetAddress() == null) {
            return "";
        }
        return socket.getInetAddress().getHostAddress();
    }

    /* access modifiers changed from: package-private */
    public String getPeerIp() {
        return getPeerDeviceIp(this.mDynamicSocket);
    }
}
