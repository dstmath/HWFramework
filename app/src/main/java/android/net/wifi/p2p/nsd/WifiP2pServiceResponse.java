package android.net.wifi.p2p.nsd;

import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.speech.tts.TextToSpeech;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WifiP2pServiceResponse implements Parcelable {
    public static final Creator<WifiP2pServiceResponse> CREATOR = null;
    private static int MAX_BUF_SIZE;
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
                case TextToSpeech.SUCCESS /*0*/:
                    return WifiManager.PPPOE_RESULT_SUCCESS;
                case SERVICE_PROTOCOL_NOT_AVAILABLE /*1*/:
                    return "SERVICE_PROTOCOL_NOT_AVAILABLE";
                case REQUESTED_INFORMATION_NOT_AVAILABLE /*2*/:
                    return "REQUESTED_INFORMATION_NOT_AVAILABLE";
                case BAD_REQUEST /*3*/:
                    return "BAD_REQUEST";
                default:
                    return "UNKNOWN";
            }
        }

        private Status() {
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.wifi.p2p.nsd.WifiP2pServiceResponse.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.wifi.p2p.nsd.WifiP2pServiceResponse.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.p2p.nsd.WifiP2pServiceResponse.<clinit>():void");
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

    public static List<WifiP2pServiceResponse> newInstance(String supplicantEvent) {
        List<WifiP2pServiceResponse> respList = new ArrayList();
        String[] args = supplicantEvent.split(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
        if (args.length != 4) {
            return null;
        }
        WifiP2pDevice dev = new WifiP2pDevice();
        dev.deviceAddress = args[1];
        byte[] bin = hexStr2Bin(args[3]);
        if (bin == null) {
            return null;
        }
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bin));
        while (dis.available() > 0) {
            int length = (dis.readUnsignedByte() + (dis.readUnsignedByte() << 8)) - 3;
            int type = dis.readUnsignedByte();
            int transId = dis.readUnsignedByte();
            int status = dis.readUnsignedByte();
            if (length < 0) {
                return null;
            }
            if (length != 0) {
                try {
                    if (length > MAX_BUF_SIZE) {
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
                    e.printStackTrace();
                    if (respList.size() > 0) {
                        return respList;
                    }
                    return null;
                }
            } else if (status == 0) {
                respList.add(new WifiP2pServiceResponse(type, status, transId, dev, null));
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
