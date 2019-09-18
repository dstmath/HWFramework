package android.net.wifi.p2p.nsd;

import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WifiP2pServiceResponse implements Parcelable {
    public static final Parcelable.Creator<WifiP2pServiceResponse> CREATOR = new Parcelable.Creator<WifiP2pServiceResponse>() {
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
            byte[] data2 = data;
            if (type == 1) {
                return WifiP2pDnsSdServiceResponse.newInstance(status, transId, dev, data2);
            }
            if (type == 2) {
                return WifiP2pUpnpServiceResponse.newInstance(status, transId, dev, data2);
            }
            WifiP2pServiceResponse wifiP2pServiceResponse = new WifiP2pServiceResponse(type, status, transId, dev, data2);
            return wifiP2pServiceResponse;
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
        WifiP2pServiceResponse resp;
        byte[] bArr = tlvsBin;
        List<WifiP2pServiceResponse> respList = new ArrayList<>();
        WifiP2pDevice dev = new WifiP2pDevice();
        dev.deviceAddress = srcAddr;
        List<WifiP2pServiceResponse> list = null;
        if (bArr == null) {
            return null;
        }
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bArr));
        while (true) {
            DataInputStream dis2 = dis;
            try {
                if (dis2.available() <= 0) {
                    return respList;
                }
                int length = (dis2.readUnsignedByte() + (dis2.readUnsignedByte() << 8)) - 3;
                int type = dis2.readUnsignedByte();
                int transId = dis2.readUnsignedByte();
                int status = dis2.readUnsignedByte();
                if (length < 0) {
                    return list;
                }
                if (length == 0) {
                    if (status == 0) {
                        WifiP2pServiceResponse wifiP2pServiceResponse = r3;
                        WifiP2pServiceResponse wifiP2pServiceResponse2 = new WifiP2pServiceResponse(type, status, transId, dev, null);
                        respList.add(wifiP2pServiceResponse);
                    }
                } else if (length > MAX_BUF_SIZE) {
                    dis2.skip((long) length);
                } else {
                    byte[] data = new byte[length];
                    dis2.readFully(data);
                    if (type == 1) {
                        resp = WifiP2pDnsSdServiceResponse.newInstance(status, transId, dev, data);
                    } else if (type == 2) {
                        resp = WifiP2pUpnpServiceResponse.newInstance(status, transId, dev, data);
                    } else {
                        WifiP2pServiceResponse wifiP2pServiceResponse3 = new WifiP2pServiceResponse(type, status, transId, dev, data);
                        resp = wifiP2pServiceResponse3;
                        if (resp != null && resp.getStatus() == 0) {
                            respList.add(resp);
                        }
                    }
                    respList.add(resp);
                }
                dis = dis2;
                list = null;
            } catch (IOException e) {
                Log.e("newInstance fail", e.getMessage());
                if (respList.size() > 0) {
                    return respList;
                }
                return null;
            }
        }
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
        sbuf.append("serviceType:");
        sbuf.append(this.mServiceType);
        sbuf.append(" status:");
        sbuf.append(Status.toString(this.mStatus));
        sbuf.append(" srcAddr:");
        sbuf.append(this.mDevice.deviceAddress);
        sbuf.append(" data:");
        sbuf.append(Arrays.toString(this.mData));
        return sbuf.toString();
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (o == this) {
            return true;
        }
        if (!(o instanceof WifiP2pServiceResponse)) {
            return false;
        }
        WifiP2pServiceResponse req = (WifiP2pServiceResponse) o;
        if (req.mServiceType != this.mServiceType || req.mStatus != this.mStatus || !equals(req.mDevice.deviceAddress, this.mDevice.deviceAddress) || !Arrays.equals(req.mData, this.mData)) {
            z = false;
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
        int i = 0;
        int hashCode = 31 * ((31 * ((31 * ((31 * ((31 * 17) + this.mServiceType)) + this.mStatus)) + this.mTransId)) + (this.mDevice.deviceAddress == null ? 0 : this.mDevice.deviceAddress.hashCode()));
        if (this.mData != null) {
            i = Arrays.hashCode(this.mData);
        }
        return hashCode + i;
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
