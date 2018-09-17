package com.huawei.nearbysdk;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.UUID;

public class BluetoothSocketTransport {
    private static final String TAG = "BluetoothSocketTransport";
    private static final boolean isSecure = false;
    private final BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();

    private int getPort(BluetoothServerSocket serverSocket) {
        int port = -1;
        try {
            Field field_mSocket = serverSocket.getClass().getDeclaredField("mSocket");
            field_mSocket.setAccessible(true);
            BluetoothSocket socket = (BluetoothSocket) field_mSocket.get(serverSocket);
            Method getPortMethod = socket.getClass().getDeclaredMethod("getPort", new Class[0]);
            getPortMethod.setAccessible(true);
            return ((Integer) getPortMethod.invoke(socket, new Object[0])).intValue();
        } catch (Exception e) {
            HwLog.e(TAG, serverSocket + " getPort fail ", e);
            return port;
        }
    }

    @Nullable
    NearbySocket createNearbySocketServer(ChannelCreateRequestImpl requestImpl) {
        BluetoothServerSocket portServerSocket = null;
        NearbyDevice nearbyDevice = requestImpl.getRemoteNearbyDevice();
        int businessId = requestImpl.getBusinessId();
        int businessType = requestImpl.getBusinessType().toNumber();
        int channelId = requestImpl.getChannelId();
        String tag = requestImpl.getTag();
        int port = requestImpl.getPort();
        String serviceUuid = requestImpl.getServiceUuid();
        try {
            Class cls = this.mAdapter.getClass();
            portServerSocket = this.mAdapter.listenUsingInsecureRfcommWithServiceRecord("listenUsingInsecureRfcommWithServiceRecord", UUID.fromString(serviceUuid));
            port = getPort(portServerSocket);
            requestImpl.accept(port);
            try {
                BluetoothSocket portSocket = portServerSocket.accept();
                HwLog.d(TAG, "Socket port get");
                BluetoothNearbySocket nearbySocket = new BluetoothNearbySocket(portSocket, nearbyDevice, businessId, businessType, channelId, tag, port, serviceUuid);
                nearbySocket.setSecurityType(requestImpl.getSecurityType());
                return nearbySocket;
            } catch (IOException e) {
                e.printStackTrace();
                HwLog.e(TAG, String.format("[Connected]Socket Type: accept(%d) failed", new Object[]{Integer.valueOf(port)}), e);
                return null;
            }
        } catch (Exception e2) {
            HwLog.e(TAG, portServerSocket + " listenRfcomm fail ", e2);
            return null;
        }
    }

    void createNearbySocketClient(InternalNearbySocket innerSocket, ICreateSocketCallback cb) {
        HwLog.d(TAG, "createNearbySocketClient innerSocket");
        try {
            NearbyDevice mNearbyDevice = innerSocket.getRemoteNearbyDevice();
            String mServiceUuid = innerSocket.getServiceUuid();
            final int mPort = innerSocket.getPort();
            BluetoothDevice mBluetoothDevice = this.mAdapter.getRemoteDevice(mNearbyDevice.getBluetoothMac());
            try {
                final BluetoothSocket mPortSocket = (BluetoothSocket) mBluetoothDevice.getClass().getMethod("createInsecureRfcommSocket", new Class[]{Integer.TYPE}).invoke(mBluetoothDevice, new Object[]{Integer.valueOf(mPort)});
                final ICreateSocketCallback iCreateSocketCallback = cb;
                final InternalNearbySocket internalNearbySocket = innerSocket;
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            mPortSocket.connect();
                            iCreateSocketCallback.onStatusChange(0, new BluetoothNearbySocket(mPortSocket, internalNearbySocket), mPort);
                            HwLog.d(BluetoothSocketTransport.TAG, "createNearbySocketClient: success mPortSocket");
                        } catch (IOException e) {
                            e.printStackTrace();
                            try {
                                mPortSocket.close();
                            } catch (IOException e2) {
                                e.printStackTrace();
                                HwLog.e(BluetoothSocketTransport.TAG, "unable to close() socket during connection failure", e2);
                            }
                            iCreateSocketCallback.onStatusChange(1, null, mPort);
                        }
                    }
                }).start();
            } catch (Exception e) {
                e.printStackTrace();
                cb.onStatusChange(1, null, mPort);
            }
        } catch (RemoteException e2) {
            e2.printStackTrace();
            cb.onStatusChange(1, null, -1);
        }
    }
}
