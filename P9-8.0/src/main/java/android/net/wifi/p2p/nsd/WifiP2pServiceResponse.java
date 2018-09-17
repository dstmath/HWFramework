package android.net.wifi.p2p.nsd;

import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.Log;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WifiP2pServiceResponse implements Parcelable {
    public static final Creator<WifiP2pServiceResponse> CREATOR = new Creator<WifiP2pServiceResponse>() {
        public WifiP2pServiceResponse createFromParcel(Parcel in) {
            int type = in.readInt();
            int status = in.readInt();
            int transId = in.readInt();
            WifiP2pDevice dev = (WifiP2pDevice) in.readParcelable(null);
            int len = in.readInt();
            byte[] data = null;
            if (len > 0) {
                data = new byte[len];
                in.readByteArray(data);
            }
            if (type == 1) {
                return WifiP2pDnsSdServiceResponse.newInstance(status, transId, dev, data);
            }
            if (type == 2) {
                return WifiP2pUpnpServiceResponse.newInstance(status, transId, dev, data);
            }
            return new WifiP2pServiceResponse(type, status, transId, dev, data);
        }

        public WifiP2pServiceResponse[] newArray(int size) {
            return new WifiP2pServiceResponse[size];
        }
    };
    private static int MAX_BUF_SIZE = 1024;
    protected byte[] mData;
    protected WifiP2pDevice mDevice;
    protected int mServiceType;
    protected int mStatus;
    protected int mTransId;

    public static class Status {
        public static final int BAD_REQUEST = 3;
        public static final int REQUESTED_INFORMATION_NOT_AVAILABLE = 2;
        public static final int SERVICE_PROTOCOL_NOT_AVAILABLE = 1;
        public static final int SUCCESS = 0;

        public static String toString(int status) {
            switch (status) {
                case 0:
                    return WifiManager.PPPOE_RESULT_SUCCESS;
                case 1:
                    return "SERVICE_PROTOCOL_NOT_AVAILABLE";
                case 2:
                    return "REQUESTED_INFORMATION_NOT_AVAILABLE";
                case 3:
                    return "BAD_REQUEST";
                default:
                    return "UNKNOWN";
            }
        }

        private Status() {
        }
    }

    protected WifiP2pServiceResponse(int serviceType, int status, int transId, WifiP2pDevice device, byte[] data) {
        this.mServiceType = serviceType;
        this.mStatus = status;
        this.mTransId = transId;
        this.mDevice = device;
        this.mData = data;
    }

    public int getServiceType() {
        return this.mServiceType;
    }

    public int getStatus() {
        return this.mStatus;
    }

    public int getTransactionId() {
        return this.mTransId;
    }

    public byte[] getRawData() {
        return this.mData;
    }

    public WifiP2pDevice getSrcDevice() {
        return this.mDevice;
    }

    public void setSrcDevice(WifiP2pDevice dev) {
        if (dev != null) {
            this.mDevice = dev;
        }
    }

    public static List<WifiP2pServiceResponse> newInstance(String srcAddr, byte[] tlvsBin) {
        List<WifiP2pServiceResponse> respList = new ArrayList();
        WifiP2pDevice dev = new WifiP2pDevice();
        dev.deviceAddress = srcAddr;
        if (tlvsBin == null) {
            return null;
        }
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(tlvsBin));
        while (dis.available() > 0) {
            try {
                int length = (dis.readUnsignedByte() + (dis.readUnsignedByte() << 8)) - 3;
                int type = dis.readUnsignedByte();
                int transId = dis.readUnsignedByte();
                int status = dis.readUnsignedByte();
                if (length < 0) {
                    return null;
                }
                if (length == 0) {
                    if (status == 0) {
                        respList.add(new WifiP2pServiceResponse(type, status, transId, dev, null));
                    }
                } else if (length > MAX_BUF_SIZE) {
                    dis.skip((long) length);
                } else {
                    WifiP2pServiceResponse resp;
                    byte[] data = new byte[length];
                    dis.readFully(data);
                    if (type == 1) {
                        resp = WifiP2pDnsSdServiceResponse.newInstance(status, transId, dev, data);
                    } else if (type == 2) {
                        resp = WifiP2pUpnpServiceResponse.newInstance(status, transId, dev, data);
                    } else {
                        resp = new WifiP2pServiceResponse(type, status, transId, dev, data);
                    }
                    if (resp != null && resp.getStatus() == 0) {
                        respList.add(resp);
                    }
                }
            } catch (IOException e) {
                Log.e("newInstance fail", e.getMessage());
                if (respList.size() > 0) {
                    return respList;
                }
                return null;
            }
        }
        return respList;
    }

    private static byte[] hexStr2Bin(String hex) {
        int sz = hex.length() / 2;
        byte[] b = new byte[(hex.length() / 2)];
        int i = 0;
        while (i < sz) {
            try {
                b[i] = (byte) Integer.parseInt(hex.substring(i * 2, (i * 2) + 2), 16);
                i++;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return b;
    }

    public String toString() {
        StringBuffer sbuf = new StringBuffer();
        sbuf.append("serviceType:").append(this.mServiceType);
        sbuf.append(" status:").append(Status.toString(this.mStatus));
        sbuf.append(" srcAddr:").append(this.mDevice.deviceAddress);
        sbuf.append(" data:").append(Arrays.toString(this.mData));
        return sbuf.toString();
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (o == this) {
            return true;
        }
        if (!(o instanceof WifiP2pServiceResponse)) {
            return false;
        }
        WifiP2pServiceResponse req = (WifiP2pServiceResponse) o;
        if (req.mServiceType == this.mServiceType && req.mStatus == this.mStatus && equals(req.mDevice.deviceAddress, this.mDevice.deviceAddress)) {
            z = Arrays.equals(req.mData, this.mData);
        }
        return z;
    }

    private boolean equals(Object a, Object b) {
        if (a == null && b == null) {
            return true;
        }
        if (a != null) {
            return a.equals(b);
        }
        return false;
    }

    public int hashCode() {
        int i;
        int i2 = 0;
        int i3 = (((((this.mServiceType + 527) * 31) + this.mStatus) * 31) + this.mTransId) * 31;
        if (this.mDevice.deviceAddress == null) {
            i = 0;
        } else {
            i = this.mDevice.deviceAddress.hashCode();
        }
        i = (i3 + i) * 31;
        if (this.mData != null) {
            i2 = Arrays.hashCode(this.mData);
        }
        return i + i2;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mServiceType);
        dest.writeInt(this.mStatus);
        dest.writeInt(this.mTransId);
        dest.writeParcelable(this.mDevice, flags);
        if (this.mData == null || this.mData.length == 0) {
            dest.writeInt(0);
            return;
        }
        dest.writeInt(this.mData.length);
        dest.writeByteArray(this.mData);
    }
}
