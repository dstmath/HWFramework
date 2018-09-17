package com.huawei.nearbysdk;

import android.bluetooth.BluetoothSocket;
import android.os.RemoteException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketAddress;

public class BluetoothNearbySocket implements NearbySocket {
    private static final String TAG = "BluetoothNearbySocket";
    InternalNearbySocket iNearbySocket = null;
    boolean mAvailability = false;
    int mBusinessId = -1;
    int mBusinessType = -1;
    int mChannel = -1;
    int mPort = -1;
    NearbyDevice mRemoteNearbyDevice = null;
    int mSecurityType = 0;
    String mServiceUuid = null;
    BluetoothSocket mSocket = null;
    String mTag = "";

    public BluetoothNearbySocket(BluetoothSocket socket, InternalNearbySocket internalNearbySocket) {
        HwLog.d(TAG, "BluetoothNearbySocket construct InternalNearbySocket");
        this.mSocket = socket;
        if (socket != null) {
            this.mAvailability = true;
        }
        try {
            this.mRemoteNearbyDevice = internalNearbySocket.getRemoteNearbyDevice();
            this.mBusinessId = internalNearbySocket.getBusinessId();
            this.mBusinessType = internalNearbySocket.getBusinessType();
            this.mSecurityType = internalNearbySocket.getSecurityType();
            this.mChannel = internalNearbySocket.getChannelId();
            this.mTag = internalNearbySocket.getTag();
            this.mPort = internalNearbySocket.getPort();
            this.mServiceUuid = internalNearbySocket.getServiceUuid();
            this.iNearbySocket = internalNearbySocket;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public BluetoothNearbySocket(BluetoothSocket socket, NearbyDevice nearbyDevice, int businessId, int businessType, int channel, String tag, int port, String serviceUuid) {
        HwLog.d(TAG, "BluetoothNearbySocket construct businessId = " + businessId + ";businessType = " + businessType + ";channel = " + channel + ";tag = " + tag);
        this.mSocket = socket;
        if (socket != null) {
            this.mAvailability = true;
        }
        this.mRemoteNearbyDevice = nearbyDevice;
        this.mBusinessId = businessId;
        this.mBusinessType = businessType;
        this.mChannel = channel;
        this.mTag = tag;
        this.mPort = port;
        this.mServiceUuid = serviceUuid;
    }

    public void setSecurityType(int securityType) {
        HwLog.d(TAG, "setSecurityType = " + securityType);
        this.mSecurityType = securityType;
    }

    public int getSecurityType() {
        return this.mSecurityType;
    }

    public boolean isValidity() {
        return this.mAvailability;
    }

    public boolean close() {
        HwLog.d(TAG, "close.");
        try {
            if (this.mSocket != null) {
                this.mSocket.close();
                this.mSocket = null;
            }
            if (this.iNearbySocket != null) {
                HwLog.d(TAG, "internalNearbySocket.close()");
                this.iNearbySocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (RemoteException e2) {
            e2.printStackTrace();
        }
        return true;
    }

    public InputStream getInputStream() {
        HwLog.d(TAG, "getInputStream");
        try {
            if (this.mSocket != null && this.mAvailability) {
                return this.mSocket.getInputStream();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public OutputStream getOutputStream() {
        HwLog.d(TAG, "getOutputStream");
        try {
            if (this.mSocket != null && this.mAvailability) {
                return this.mSocket.getOutputStream();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void shutdownInput() {
        HwLog.d(TAG, "shutdownInput");
    }

    public void shutdownOutput() {
        HwLog.d(TAG, "shutdownOutput");
    }

    public int getBusinessId() {
        return this.mBusinessId;
    }

    public int getBusinessType() {
        return this.mBusinessType;
    }

    public int getChannel() {
        return this.mChannel;
    }

    public String getTag() {
        return this.mTag;
    }

    public NearbyDevice getRemoteNearbyDevice() {
        return this.mRemoteNearbyDevice;
    }

    public SocketAddress getLocalSocketIpAddress() {
        return null;
    }

    public SocketAddress getRemoteSocketIpAddress() {
        return null;
    }

    public int getPort() {
        return this.mPort;
    }

    public String getServiceUuid() {
        return this.mServiceUuid;
    }

    public boolean registerSocketStatus(SocketStatusListener statuslistener) {
        return true;
    }
}
