package com.huawei.nearbysdk;

import android.bluetooth.BluetoothSocket;
import android.os.RemoteException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketAddress;

public class BluetoothNearbySocket implements NearbySocket {
    private static final int INVALID_VALUE = -1;
    private static final String TAG = "BluetoothNearbySocket";
    InternalNearbySocket iNearbySocket;
    boolean mAvailability;
    int mBusinessId;
    int mBusinessType;
    int mChannel;
    int mPort;
    NearbyDevice mRemoteNearbyDevice;
    int mSecurityType;
    String mServiceUuid;
    BluetoothSocket mSocket;
    String mTag;

    public BluetoothNearbySocket(BluetoothSocket socket, InternalNearbySocket internalNearbySocket) {
        this.mSocket = null;
        this.mAvailability = false;
        this.mRemoteNearbyDevice = null;
        this.mBusinessId = -1;
        this.mBusinessType = -1;
        this.mChannel = -1;
        this.mTag = "";
        this.mPort = -1;
        this.mServiceUuid = null;
        this.iNearbySocket = null;
        this.mSecurityType = 0;
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
            HwLog.e(TAG, "BluetoothNearbySocket() fail: RemoteException");
        }
    }

    public BluetoothNearbySocket(BluetoothSocket socket, NearbyDevice nearbyDevice, int businessId, int businessType, int channel, String tag, int port, String serviceUuid) {
        this.mSocket = null;
        this.mAvailability = false;
        this.mRemoteNearbyDevice = null;
        this.mBusinessId = -1;
        this.mBusinessType = -1;
        this.mChannel = -1;
        this.mTag = "";
        this.mPort = -1;
        this.mServiceUuid = null;
        this.iNearbySocket = null;
        this.mSecurityType = 0;
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

    public static class Builder {
        int mBusinessId = -1;
        int mBusinessType = -1;
        int mChannel = -1;
        boolean mIsAvailability = false;
        InternalNearbySocket mNearbySocket = null;
        int mPort = -1;
        NearbyDevice mRemoteNearbyDevice = null;
        int mSecurityType = 0;
        String mServiceUuid = null;
        BluetoothSocket mSocket = null;
        String mTag = "";

        public Builder withBluetoothSocket(BluetoothSocket socket) {
            this.mSocket = socket;
            return this;
        }

        public Builder withIsAvailability(boolean isAvailability) {
            this.mIsAvailability = isAvailability;
            return this;
        }

        public Builder withRemoteNearbyDevice(NearbyDevice remoteNearbyDevice) {
            this.mRemoteNearbyDevice = remoteNearbyDevice;
            return this;
        }

        public Builder withBusinessId(int businessId) {
            this.mBusinessId = businessId;
            return this;
        }

        public Builder withBusinessType(int businessType) {
            this.mBusinessType = businessType;
            return this;
        }

        public Builder withChannel(int channel) {
            this.mChannel = channel;
            return this;
        }

        public Builder withTag(String tag) {
            this.mTag = tag;
            return this;
        }

        public Builder withPort(int port) {
            this.mPort = port;
            return this;
        }

        public Builder withServiceUuid(String serviceUuid) {
            this.mServiceUuid = serviceUuid;
            return this;
        }

        public Builder withInternalNearbySocket(InternalNearbySocket nearbySocket) {
            this.mNearbySocket = nearbySocket;
            return this;
        }

        public Builder withSecurityType(int securityType) {
            this.mSecurityType = securityType;
            return this;
        }

        public BluetoothNearbySocket build() {
            return new BluetoothNearbySocket(this);
        }
    }

    private BluetoothNearbySocket(Builder builder) {
        this.mSocket = null;
        this.mAvailability = false;
        this.mRemoteNearbyDevice = null;
        this.mBusinessId = -1;
        this.mBusinessType = -1;
        this.mChannel = -1;
        this.mTag = "";
        this.mPort = -1;
        this.mServiceUuid = null;
        this.iNearbySocket = null;
        this.mSecurityType = 0;
        this.mSocket = builder.mSocket;
        this.mAvailability = builder.mIsAvailability;
        this.mRemoteNearbyDevice = builder.mRemoteNearbyDevice;
        this.mBusinessId = builder.mBusinessId;
        this.mBusinessType = builder.mBusinessType;
        this.mChannel = builder.mChannel;
        this.mTag = builder.mTag;
        this.mPort = builder.mPort;
        this.mServiceUuid = builder.mServiceUuid;
        this.iNearbySocket = builder.mNearbySocket;
        this.mSecurityType = builder.mSecurityType;
    }

    public void setSecurityType(int securityType) {
        HwLog.d(TAG, "setSecurityType = " + securityType);
        this.mSecurityType = securityType;
    }

    @Override // com.huawei.nearbysdk.NearbySocket
    public int getSecurityType() {
        return this.mSecurityType;
    }

    public boolean isValidity() {
        return this.mAvailability;
    }

    @Override // com.huawei.nearbysdk.NearbySocket
    public boolean close() {
        HwLog.d(TAG, "close.");
        try {
            if (this.mSocket != null) {
                this.mSocket.close();
                this.mSocket = null;
            }
            if (this.iNearbySocket == null) {
                return true;
            }
            HwLog.d(TAG, "internalNearbySocket.close()");
            this.iNearbySocket.close();
            return true;
        } catch (IOException e) {
            HwLog.e(TAG, "close() fail: IOexception");
            return true;
        } catch (RemoteException e2) {
            HwLog.e(TAG, "close() fail: RemoteException");
            return true;
        }
    }

    @Override // com.huawei.nearbysdk.NearbySocket
    public InputStream getInputStream() {
        HwLog.d(TAG, "getInputStream");
        try {
            if (this.mSocket == null || !this.mAvailability) {
                return null;
            }
            return this.mSocket.getInputStream();
        } catch (IOException e) {
            HwLog.e(TAG, "getInputStream fail: IOexception");
            return null;
        }
    }

    @Override // com.huawei.nearbysdk.NearbySocket
    public OutputStream getOutputStream() {
        HwLog.d(TAG, "getOutputStream");
        try {
            if (this.mSocket == null || !this.mAvailability) {
                return null;
            }
            return this.mSocket.getOutputStream();
        } catch (IOException e) {
            HwLog.e(TAG, "getOutputStream fail: IOexception");
            return null;
        }
    }

    @Override // com.huawei.nearbysdk.NearbySocket
    public void shutdownInput() {
        HwLog.d(TAG, "shutdownInput");
    }

    @Override // com.huawei.nearbysdk.NearbySocket
    public void shutdownOutput() {
        HwLog.d(TAG, "shutdownOutput");
    }

    @Override // com.huawei.nearbysdk.NearbySocket
    public int getBusinessId() {
        return this.mBusinessId;
    }

    @Override // com.huawei.nearbysdk.NearbySocket
    public int getBusinessType() {
        return this.mBusinessType;
    }

    @Override // com.huawei.nearbysdk.NearbySocket
    public int getChannel() {
        return this.mChannel;
    }

    @Override // com.huawei.nearbysdk.NearbySocket
    public String getTag() {
        return this.mTag;
    }

    @Override // com.huawei.nearbysdk.NearbySocket
    public NearbyDevice getRemoteNearbyDevice() {
        return this.mRemoteNearbyDevice;
    }

    @Override // com.huawei.nearbysdk.NearbySocket
    public SocketAddress getLocalSocketIpAddress() {
        return null;
    }

    @Override // com.huawei.nearbysdk.NearbySocket
    public SocketAddress getRemoteSocketIpAddress() {
        return null;
    }

    @Override // com.huawei.nearbysdk.NearbySocket
    public int getPort() {
        return this.mPort;
    }

    @Override // com.huawei.nearbysdk.NearbySocket
    public String getServiceUuid() {
        return this.mServiceUuid;
    }

    @Override // com.huawei.nearbysdk.NearbySocket
    public boolean registerSocketStatus(SocketStatusListener statuslistener) {
        return true;
    }
}
