package com.huawei.nearbysdk;

import android.os.RemoteException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketAddress;

public class TCPNearbySocket implements NearbySocket {
    private static final String TAG = "TCPNS";
    InternalNearbySocket iNearbySocket = null;
    boolean mAvailability = false;
    int mBusinessId = -1;
    int mBusinessType = -1;
    int mChannel = -1;
    NearbyDevice mRemoteNearbyDevice = null;
    int mSecurityType = 0;
    Socket mSocket = null;
    String mTag = "";

    public TCPNearbySocket(InternalNearbySocket internalNearbySocket) {
        HwLog.d(TAG, "TCPNearbySocket construct");
        try {
            this.mBusinessId = internalNearbySocket.getBusinessId();
            this.mBusinessType = internalNearbySocket.getBusinessType();
            this.mChannel = internalNearbySocket.getChannelId();
            this.mTag = internalNearbySocket.getTag();
            this.iNearbySocket = internalNearbySocket;
            this.mRemoteNearbyDevice = internalNearbySocket.getRemoteNearbyDevice();
        } catch (RemoteException e) {
            HwLog.e(TAG, "TCPNearbySocket fail: " + e.toString());
        }
    }

    public TCPNearbySocket(NearbyDevice nearbyDevice, int businessId, int businessType, int channel, String tag, InternalNearbySocket internalNearbySocket) {
        HwLog.d(TAG, "TCPNearbySocket construct businessId = " + businessId + ";businessType = " + businessType + ";channel = " + channel + ";tag = " + tag);
        this.mRemoteNearbyDevice = nearbyDevice;
        this.mBusinessId = businessId;
        this.mBusinessType = businessType;
        this.mChannel = channel;
        this.mTag = tag;
        this.iNearbySocket = internalNearbySocket;
    }

    public void setSocket(Socket socket) {
        HwLog.d(TAG, "setSocket");
        this.mSocket = socket;
        if (socket != null) {
            this.mAvailability = true;
        }
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
        if (this.mSocket != null) {
            try {
                this.mSocket.close();
            } catch (IOException e) {
                HwLog.e(TAG, "mSocket close fail: " + e);
            }
        }
        if (this.iNearbySocket != null) {
            HwLog.d(TAG, "internalNearbySocket.close()");
            try {
                this.iNearbySocket.close();
                return true;
            } catch (RemoteException e2) {
                HwLog.e(TAG, "iNearbySocket close fail: " + e2);
                return true;
            }
        } else {
            HwLog.d(TAG, "iNearbySocket is null!!!!");
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
            HwLog.e(TAG, "getInputStream fail: " + e);
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
            HwLog.e(TAG, "getOutputStream fail: " + e);
            return null;
        }
    }

    @Override // com.huawei.nearbysdk.NearbySocket
    public void shutdownInput() {
        HwLog.d(TAG, "shutdownInput");
        try {
            if (this.mSocket != null) {
                this.mSocket.shutdownInput();
            }
        } catch (IOException e) {
            HwLog.e(TAG, "shutdownInput fail: " + e);
        }
    }

    @Override // com.huawei.nearbysdk.NearbySocket
    public void shutdownOutput() {
        HwLog.d(TAG, "shutdownOutput");
        try {
            if (this.mSocket != null) {
                this.mSocket.shutdownOutput();
            }
        } catch (IOException e) {
            HwLog.e(TAG, "shutdownOutput fail: " + e);
        }
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
        if (this.mSocket != null) {
            return this.mSocket.getLocalSocketAddress();
        }
        return null;
    }

    @Override // com.huawei.nearbysdk.NearbySocket
    public SocketAddress getRemoteSocketIpAddress() {
        if (this.mSocket != null) {
            return this.mSocket.getRemoteSocketAddress();
        }
        return null;
    }

    @Override // com.huawei.nearbysdk.NearbySocket
    public int getPort() {
        return this.mSocket.getPort();
    }

    @Override // com.huawei.nearbysdk.NearbySocket
    public String getServiceUuid() {
        return "";
    }

    @Override // com.huawei.nearbysdk.NearbySocket
    public boolean registerSocketStatus(SocketStatusListener statuslistener) {
        return true;
    }
}
