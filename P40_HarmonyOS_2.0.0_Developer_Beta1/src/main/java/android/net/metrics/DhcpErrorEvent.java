package android.net.metrics;

import android.annotation.SystemApi;
import android.net.metrics.IpConnectivityLog;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseArray;
import com.android.internal.util.MessageUtils;

@SystemApi
public final class DhcpErrorEvent implements IpConnectivityLog.Event {
    public static final int BOOTP_TOO_SHORT = 67174400;
    public static final int BUFFER_UNDERFLOW = 83951616;
    public static final Parcelable.Creator<DhcpErrorEvent> CREATOR = new Parcelable.Creator<DhcpErrorEvent>() {
        /* class android.net.metrics.DhcpErrorEvent.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public DhcpErrorEvent createFromParcel(Parcel in) {
            return new DhcpErrorEvent(in);
        }

        @Override // android.os.Parcelable.Creator
        public DhcpErrorEvent[] newArray(int size) {
            return new DhcpErrorEvent[size];
        }
    };
    public static final int DHCP_BAD_MAGIC_COOKIE = 67239936;
    public static final int DHCP_ERROR = 4;
    private static final int DHCP_ERROR_TYPE = 1024;
    public static final int DHCP_INVALID_OPTION_LENGTH = 67305472;
    public static final int DHCP_NO_COOKIE = 67502080;
    public static final int DHCP_NO_MSG_TYPE = 67371008;
    public static final int DHCP_UNKNOWN_MSG_TYPE = 67436544;
    public static final int L2_ERROR = 1;
    private static final int L2_ERROR_TYPE = 256;
    public static final int L2_TOO_SHORT = 16842752;
    public static final int L2_WRONG_ETH_TYPE = 16908288;
    public static final int L3_ERROR = 2;
    private static final int L3_ERROR_TYPE = 512;
    public static final int L3_INVALID_IP = 33751040;
    public static final int L3_NOT_IPV4 = 33685504;
    public static final int L3_TOO_SHORT = 33619968;
    public static final int L4_ERROR = 3;
    private static final int L4_ERROR_TYPE = 768;
    public static final int L4_NOT_UDP = 50397184;
    public static final int L4_WRONG_PORT = 50462720;
    public static final int MISC_ERROR = 5;
    private static final int MISC_ERROR_TYPE = 1280;
    public static final int PARSING_ERROR = 84082688;
    public static final int RECEIVE_ERROR = 84017152;
    public final int errorCode;

    public DhcpErrorEvent(int errorCode2) {
        this.errorCode = errorCode2;
    }

    private DhcpErrorEvent(Parcel in) {
        this.errorCode = in.readInt();
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.errorCode);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public static int errorCodeWithOption(int errorCode2, int option) {
        return (-65536 & errorCode2) | (option & 255);
    }

    public String toString() {
        return String.format("DhcpErrorEvent(%s)", Decoder.constants.get(this.errorCode));
    }

    static final class Decoder {
        static final SparseArray<String> constants = MessageUtils.findMessageNames(new Class[]{DhcpErrorEvent.class}, new String[]{"L2_", "L3_", "L4_", "BOOTP_", "DHCP_", "BUFFER_", "RECEIVE_", "PARSING_"});

        Decoder() {
        }
    }
}
