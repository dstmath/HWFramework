package com.huawei.nearbysdk;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import com.huawei.nearbysdk.BluetoothNearbySocket;
import com.huawei.nearbysdk.SocketListenerTransport;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class BluetoothSocketTransport {
    private static final String TAG = "BluetoothSocketTransport";
    private boolean isSecure = false;
    private final BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();
    private Handler mHandler;

    public BluetoothSocketTransport(Looper looper) {
        this.mHandler = new Handler(looper);
    }

    private int getPort(BluetoothServerSocket serverSocket) {
        try {
            Field field_mSocket = serverSocket.getClass().getDeclaredField("mSocket");
            field_mSocket.setAccessible(true);
            BluetoothSocket socket = (BluetoothSocket) field_mSocket.get(serverSocket);
            Method getPortMethod = socket.getClass().getDeclaredMethod("getPort", new Class[0]);
            getPortMethod.setAccessible(true);
            return ((Integer) getPortMethod.invoke(socket, new Object[0])).intValue();
        } catch (Exception e) {
            HwLog.e(TAG, serverSocket + " getPort fail " + e.getLocalizedMessage());
            return -1;
        }
    }

    /* access modifiers changed from: package-private */
    public NearbySocket createNearbySocketServer(SocketListenerTransport.ChannelCreateRequestImpl requestImpl, int timeout, InternalNearbySocket internalNearbySocket) {
        BluetoothServerSocket portServerSocket;
        BluetoothSocket portSocket;
        if (timeout < 0) {
            HwLog.e(TAG, "invalid timeout value:" + timeout);
            return null;
        }
        NearbyDevice nearbyDevice = requestImpl.getRemoteNearbyDevice();
        int businessId = requestImpl.getBusinessId();
        if (requestImpl.getBusinessType() == null) {
            HwLog.e(TAG, "getBusinessType return null.");
            return null;
        }
        int businessType = requestImpl.getBusinessType().toNumber();
        int channelId = requestImpl.getChannelId();
        String tag = requestImpl.getTag();
        int port = requestImpl.getPort();
        String serviceUuid = requestImpl.getServiceUuid();
        try {
            Class<?> cls = this.mAdapter.getClass();
            if (this.isSecure) {
                portServerSocket = (BluetoothServerSocket) cls.getMethod("listenUsingRfcommOn", Integer.TYPE).invoke(this.mAdapter, Integer.valueOf(port));
            } else {
                portServerSocket = this.mAdapter.listenUsingInsecureRfcommWithServiceRecord("listenUsingInsecureRfcommWithServiceRecord", UUID.fromString(serviceUuid));
            }
            int port2 = getPort(portServerSocket);
            requestImpl.accept(port2);
            try {
                HwLog.d(TAG, " timeout " + timeout);
                if (timeout != 0) {
                    SocketAcceptMonitor socketAcceptMonitor = new SocketAcceptMonitor(portServerSocket, timeout);
                    socketAcceptMonitor.startTimeout();
                    portSocket = portServerSocket.accept();
                    HwLog.e(TAG, "accept");
                    socketAcceptMonitor.setSocketAccept();
                } else {
                    portSocket = portServerSocket.accept();
                }
                HwLog.d(TAG, "Socket port get");
                BluetoothNearbySocket build = new BluetoothNearbySocket.Builder().withBluetoothSocket(portSocket).withIsAvailability(portSocket != null).withRemoteNearbyDevice(nearbyDevice).withBusinessId(businessId).withBusinessType(businessType).withChannel(channelId).withTag(tag).withPort(port2).withServiceUuid(serviceUuid).withInternalNearbySocket(internalNearbySocket).withSecurityType(requestImpl.getSecurityType()).build();
                try {
                    portServerSocket.close();
                    return build;
                } catch (IOException e) {
                    HwLog.e(TAG, "close ServerSocket fail");
                    return build;
                }
            } catch (IOException e2) {
                HwLog.e(TAG, String.format("[Connected]Socket Type: accept(%d) failed", Integer.valueOf(port2)) + e2.getLocalizedMessage());
                return null;
            }
        } catch (Exception e3) {
            HwLog.e(TAG, ((Object) null) + " listenRfcomm fail " + e3.getLocalizedMessage());
            return null;
        }
    }

    /* access modifiers changed from: private */
    public class SocketAcceptMonitor {
        private BluetoothServerSocket mBluetoothServerSocket;
        private volatile boolean mIsAccept = false;
        private final Object mLock = new Object();
        private int mTimeout = 0;
        private Timer mTimer;
        private final TimerTask mTimerTask = new TimerTask() {
            /* class com.huawei.nearbysdk.BluetoothSocketTransport.SocketAcceptMonitor.AnonymousClass1 */

            @Override // java.util.TimerTask, java.lang.Runnable
            public void run() {
                synchronized (SocketAcceptMonitor.this.mLock) {
                    HwLog.e(BluetoothSocketTransport.TAG, " timeoutRunnable " + SocketAcceptMonitor.this.mIsAccept);
                    if (!SocketAcceptMonitor.this.mIsAccept) {
                        HwLog.e(BluetoothSocketTransport.TAG, "accept socket timeout:" + SocketAcceptMonitor.this.mTimeout);
                        SocketAcceptMonitor.this.timeoutProcess();
                    }
                }
            }
        };

        SocketAcceptMonitor(BluetoothServerSocket bluetoothServerSocket, int timeout) {
            this.mBluetoothServerSocket = bluetoothServerSocket;
            this.mTimeout = timeout;
            this.mTimer = new Timer(true);
        }

        /* access modifiers changed from: package-private */
        public void startTimeout() {
            HwLog.d(BluetoothSocketTransport.TAG, " startTimeout " + this.mTimeout);
            this.mTimer.schedule(this.mTimerTask, (long) this.mTimeout);
        }

        private void removeTimeoutProcess() {
            this.mTimer.cancel();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void timeoutProcess() {
            if (this.mBluetoothServerSocket != null) {
                try {
                    this.mBluetoothServerSocket.close();
                } catch (IOException e) {
                    HwLog.e(BluetoothSocketTransport.TAG, "close bluetooth server socket exception");
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void setSocketAccept() {
            HwLog.d(BluetoothSocketTransport.TAG, "setSocketAccept set socket accept");
            synchronized (this.mLock) {
                this.mIsAccept = true;
                removeTimeoutProcess();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void createNearbySocketClient(InternalNearbySocket innerSocket, ICreateSocketCallback cb, int connectTimeLeft) {
        BluetoothSocketClient socketClient = new BluetoothSocketClient(innerSocket, cb);
        socketClient.startTimeout(connectTimeLeft);
        socketClient.startConnectionThread();
    }

    private class BluetoothSocketClient implements Runnable {
        static final int STATE_CLOSED = -2;
        static final int STATE_CONNECTED = 3;
        static final int STATE_CONNECTING = 2;
        static final int STATE_FAILURE = -1;
        static final int STATE_INIT = 0;
        private static final String TAG = "BluetoothSocketClient";
        private final Object lock = new Object();
        private BluetoothDevice mBtDevice;
        private final ICreateSocketCallback mCallback;
        private final InternalNearbySocket mInnerSocket;
        private int mPort;
        private BluetoothSocket mPortSocket;
        private boolean mSecureConnect = false;
        private volatile int state = 0;
        private final Runnable timeoutRunnable = new Runnable() {
            /* class com.huawei.nearbysdk.BluetoothSocketTransport.BluetoothSocketClient.AnonymousClass1 */

            @Override // java.lang.Runnable
            public void run() {
                synchronized (BluetoothSocketClient.this.lock) {
                    if (BluetoothSocketClient.this.state == 2 || BluetoothSocketClient.this.state == 0) {
                        HwLog.w(BluetoothSocketClient.TAG, "device connect timeout, cancel connection!");
                        BluetoothSocketClient.this.cancel();
                        BluetoothSocketClient.this.mCallback.onStatusChange(1, null, BluetoothSocketClient.this.mPort);
                    }
                }
            }
        };

        BluetoothSocketClient(InternalNearbySocket innerSocket, ICreateSocketCallback cb) {
            this.mInnerSocket = innerSocket;
            this.mCallback = cb;
        }

        /* access modifiers changed from: package-private */
        public void startConnectionThread() {
            synchronized (this.lock) {
                this.state = 2;
            }
            BluetoothSocket initBluetoothSocket = initBluetoothSocket();
            this.mPortSocket = initBluetoothSocket;
            if (initBluetoothSocket != null) {
                new Thread(this).start();
            }
        }

        /* access modifiers changed from: package-private */
        public void cancel() {
            HwLog.i(TAG, "Socket cancel " + this);
            if (this.mPortSocket != null) {
                try {
                    this.mPortSocket.close();
                } catch (IOException e) {
                    HwLog.e(TAG, "close() of connect socket ERROR:" + e.getLocalizedMessage());
                }
            }
            synchronized (this.lock) {
                if (this.state == 0 || this.state == 2) {
                    doCleanUp();
                }
                this.state = -2;
            }
        }

        /* access modifiers changed from: package-private */
        public void startTimeout(int timeout) {
            BluetoothSocketTransport.this.mHandler.postDelayed(this.timeoutRunnable, (long) timeout);
        }

        private void doCleanUp() {
            BluetoothSocketTransport.this.mHandler.removeCallbacks(this.timeoutRunnable);
        }

        private BluetoothSocket initBluetoothSocket() {
            boolean z = false;
            HwLog.d(TAG, "createNearbySocketClient innerSocket");
            try {
                this.mPort = this.mInnerSocket.getPort();
                NearbyDevice nearbyDevice = this.mInnerSocket.getRemoteNearbyDevice();
                if (BluetoothSocketTransport.this.isSecure || this.mInnerSocket.getSecurityType() == 2) {
                    z = true;
                }
                this.mSecureConnect = z;
                HwLog.i(TAG, "isSecure:" + BluetoothSocketTransport.this.isSecure + " ,mSecureConnect:" + this.mSecureConnect + ",port:" + this.mPort);
                if (nearbyDevice == null) {
                    HwLog.e(TAG, "getRemoteNearbyDevice return null.");
                    return null;
                }
                this.mBtDevice = BluetoothSocketTransport.this.mAdapter.getRemoteDevice(nearbyDevice.getBluetoothMac());
                try {
                    Class<?> cls = this.mBtDevice.getClass();
                    if (this.mSecureConnect) {
                        return (BluetoothSocket) cls.getMethod("createRfcommSocket", Integer.TYPE).invoke(this.mBtDevice, Integer.valueOf(this.mPort));
                    }
                    return (BluetoothSocket) cls.getMethod("createInsecureRfcommSocket", Integer.TYPE).invoke(this.mBtDevice, Integer.valueOf(this.mPort));
                } catch (Exception e) {
                    HwLog.e(TAG, "createNearbySocketClient fail: " + e.getLocalizedMessage());
                    synchronized (this.lock) {
                        this.state = -1;
                        this.mCallback.onStatusChange(1, null, this.mPort);
                        return null;
                    }
                }
            } catch (RemoteException e2) {
                HwLog.e(TAG, "createNearbySocketClient RemoteException " + e2.getLocalizedMessage());
                synchronized (this.lock) {
                    this.state = -1;
                    this.mCallback.onStatusChange(1, null, -1);
                    return null;
                }
            }
        }

        @Override // java.lang.Runnable
        public void run() {
            try {
                this.mPortSocket.connect();
            } catch (IOException e) {
                HwLog.e(TAG, "socket connect fail: " + e.getLocalizedMessage());
                try {
                    this.mPortSocket.close();
                } catch (IOException e2) {
                    HwLog.e(TAG, "unable to close() socket during connection failure" + e2.getLocalizedMessage());
                }
            }
            doCleanUp();
            if (this.mPortSocket.isConnected()) {
                BluetoothNearbySocket nearbySocket = new BluetoothNearbySocket(this.mPortSocket, this.mInnerSocket);
                synchronized (this.lock) {
                    this.state = 3;
                    HwLog.d(TAG, "createNearbySocketClient: success mPortSocket");
                    this.mCallback.onStatusChange(0, nearbySocket, this.mPort);
                }
                return;
            }
            synchronized (this.lock) {
                this.state = -1;
                this.mCallback.onStatusChange(1, null, this.mPort);
                try {
                    this.mInnerSocket.close();
                } catch (RemoteException e3) {
                    HwLog.e(TAG, "mInnerSocket close failed: " + e3);
                }
            }
        }
    }
}
