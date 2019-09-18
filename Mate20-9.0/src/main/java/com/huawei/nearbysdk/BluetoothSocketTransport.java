package com.huawei.nearbysdk;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import com.huawei.nearbysdk.SocketListenerTransport;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.UUID;

public class BluetoothSocketTransport {
    private static final String TAG = "BluetoothSocketTransport";
    /* access modifiers changed from: private */
    public boolean isSecure = false;
    /* access modifiers changed from: private */
    public final BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();
    /* access modifiers changed from: private */
    public Handler mHandler;

    private class BluetoothSocketClient implements Runnable {
        static final int STATE_CLOSED = -2;
        static final int STATE_CONNECTED = 3;
        static final int STATE_CONNECTING = 2;
        static final int STATE_FAILURE = -1;
        static final int STATE_INIT = 0;
        private static final String TAG = "BluetoothSocketClient";
        /* access modifiers changed from: private */
        public final Object lock = new Object();
        private BluetoothDevice mBtDevice;
        /* access modifiers changed from: private */
        public final ICreateSocketCallback mCallback;
        private final InternalNearbySocket mInnerSocket;
        /* access modifiers changed from: private */
        public int mPort;
        private BluetoothSocket mPortSocket;
        private boolean mSecureConnect = false;
        /* access modifiers changed from: private */
        public volatile int state = 0;
        private final Runnable timeoutRunnable = new Runnable() {
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

        /* JADX WARNING: Removed duplicated region for block: B:14:0x007d A[Catch:{ Exception -> 0x00be }] */
        /* JADX WARNING: Removed duplicated region for block: B:15:0x009d A[Catch:{ Exception -> 0x00be }] */
        private BluetoothSocket initBluetoothSocket() {
            boolean z;
            BluetoothSocket socket;
            HwLog.d(TAG, "createNearbySocketClient innerSocket");
            try {
                this.mPort = this.mInnerSocket.getPort();
                NearbyDevice nearbyDevice = this.mInnerSocket.getRemoteNearbyDevice();
                if (!BluetoothSocketTransport.this.isSecure) {
                    if (this.mInnerSocket.getSecurityType() != 2) {
                        z = false;
                        this.mSecureConnect = z;
                        HwLog.i(TAG, "isSecure:" + BluetoothSocketTransport.this.isSecure + " ,mSecureConnect:" + this.mSecureConnect + ",port:" + this.mPort);
                        this.mBtDevice = BluetoothSocketTransport.this.mAdapter.getRemoteDevice(nearbyDevice.getBluetoothMac());
                        Class cls = this.mBtDevice.getClass();
                        if (!this.mSecureConnect) {
                            socket = (BluetoothSocket) cls.getMethod("createRfcommSocket", new Class[]{Integer.TYPE}).invoke(this.mBtDevice, new Object[]{Integer.valueOf(this.mPort)});
                        } else {
                            socket = (BluetoothSocket) cls.getMethod("createInsecureRfcommSocket", new Class[]{Integer.TYPE}).invoke(this.mBtDevice, new Object[]{Integer.valueOf(this.mPort)});
                        }
                        return socket;
                    }
                }
                z = true;
                this.mSecureConnect = z;
                HwLog.i(TAG, "isSecure:" + BluetoothSocketTransport.this.isSecure + " ,mSecureConnect:" + this.mSecureConnect + ",port:" + this.mPort);
                this.mBtDevice = BluetoothSocketTransport.this.mAdapter.getRemoteDevice(nearbyDevice.getBluetoothMac());
                try {
                    Class cls2 = this.mBtDevice.getClass();
                    if (!this.mSecureConnect) {
                    }
                    return socket;
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
            }
        }
    }

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
    public NearbySocket createNearbySocketServer(SocketListenerTransport.ChannelCreateRequestImpl requestImpl) {
        BluetoothServerSocket portServerSocket;
        NearbyDevice nearbyDevice = requestImpl.getRemoteNearbyDevice();
        int businessId = requestImpl.getBusinessId();
        int businessType = requestImpl.getBusinessType().toNumber();
        int channelId = requestImpl.getChannelId();
        String tag = requestImpl.getTag();
        int port = requestImpl.getPort();
        String serviceUuid = requestImpl.getServiceUuid();
        try {
            Class cls = this.mAdapter.getClass();
            if (this.isSecure) {
                try {
                    portServerSocket = (BluetoothServerSocket) cls.getMethod("listenUsingRfcommOn", new Class[]{Integer.TYPE}).invoke(this.mAdapter, new Object[]{Integer.valueOf(port)});
                } catch (Exception e) {
                    e = e;
                    String str = serviceUuid;
                    HwLog.e(TAG, null + " listenRfcomm fail " + e.getLocalizedMessage());
                    return null;
                }
            } else {
                portServerSocket = this.mAdapter.listenUsingInsecureRfcommWithServiceRecord("listenUsingInsecureRfcommWithServiceRecord", UUID.fromString(serviceUuid));
            }
            int port2 = getPort(portServerSocket);
            requestImpl.accept(port2);
            try {
                BluetoothSocket portSocket = portServerSocket.accept();
                HwLog.d(TAG, "Socket port get");
                int i = port2;
                String str2 = serviceUuid;
                BluetoothNearbySocket bluetoothNearbySocket = new BluetoothNearbySocket(portSocket, nearbyDevice, businessId, businessType, channelId, tag, port2, serviceUuid);
                bluetoothNearbySocket.setSecurityType(requestImpl.getSecurityType());
                return bluetoothNearbySocket;
            } catch (IOException e2) {
                String str3 = serviceUuid;
                IOException iOException = e2;
                HwLog.e(TAG, String.format("[Connected]Socket Type: accept(%d) failed", new Object[]{Integer.valueOf(port2)}) + e2.getLocalizedMessage());
                return null;
            }
        } catch (Exception e3) {
            e = e3;
            String str4 = serviceUuid;
            HwLog.e(TAG, null + " listenRfcomm fail " + e.getLocalizedMessage());
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public void createNearbySocketClient(InternalNearbySocket innerSocket, ICreateSocketCallback cb, int connectTimeLeft) {
        BluetoothSocketClient socketClient = new BluetoothSocketClient(innerSocket, cb);
        socketClient.startTimeout(connectTimeLeft);
        socketClient.startConnectionThread();
    }
}
