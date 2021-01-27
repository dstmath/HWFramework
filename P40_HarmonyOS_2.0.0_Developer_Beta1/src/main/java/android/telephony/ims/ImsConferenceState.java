package android.telephony.ims;

import android.annotation.SystemApi;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.telecom.Log;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@SystemApi
public final class ImsConferenceState implements Parcelable {
    public static final Parcelable.Creator<ImsConferenceState> CREATOR = new Parcelable.Creator<ImsConferenceState>() {
        /* class android.telephony.ims.ImsConferenceState.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ImsConferenceState createFromParcel(Parcel in) {
            return new ImsConferenceState(in);
        }

        @Override // android.os.Parcelable.Creator
        public ImsConferenceState[] newArray(int size) {
            return new ImsConferenceState[size];
        }
    };
    public static final String DISPLAY_TEXT = "display-text";
    public static final String ENDPOINT = "endpoint";
    public static final String SIP_STATUS_CODE = "sipstatuscode";
    public static final String STATUS = "status";
    public static final String STATUS_ALERTING = "alerting";
    public static final String STATUS_CONNECTED = "connected";
    public static final String STATUS_CONNECT_FAIL = "connect-fail";
    public static final String STATUS_DIALING_IN = "dialing-in";
    public static final String STATUS_DIALING_OUT = "dialing-out";
    public static final String STATUS_DISCONNECTED = "disconnected";
    public static final String STATUS_DISCONNECTING = "disconnecting";
    public static final String STATUS_MUTED_VIA_FOCUS = "muted-via-focus";
    public static final String STATUS_ON_HOLD = "on-hold";
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_SEND_ONLY = "sendonly";
    public static final String STATUS_SEND_RECV = "sendrecv";
    public static final String USER = "user";
    public static final String USER_ENTITY = "user-entity";
    public final HashMap<String, Bundle> mParticipants;

    public ImsConferenceState() {
        this.mParticipants = new HashMap<>();
    }

    private ImsConferenceState(Parcel in) {
        this.mParticipants = new HashMap<>();
        readFromParcel(in);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        Set<Map.Entry<String, Bundle>> entries;
        out.writeInt(this.mParticipants.size());
        if (this.mParticipants.size() > 0 && (entries = this.mParticipants.entrySet()) != null) {
            for (Map.Entry<String, Bundle> entry : entries) {
                out.writeString(entry.getKey());
                out.writeParcelable(entry.getValue(), 0);
            }
        }
    }

    private void readFromParcel(Parcel in) {
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            this.mParticipants.put(in.readString(), (Bundle) in.readParcelable(null));
        }
    }

    public static int getConnectionStateForStatus(String status) {
        if (status.equals(STATUS_PENDING)) {
            return 0;
        }
        if (status.equals(STATUS_DIALING_IN)) {
            return 2;
        }
        if (status.equals(STATUS_ALERTING) || status.equals(STATUS_DIALING_OUT)) {
            return 3;
        }
        if (status.equals(STATUS_ON_HOLD) || status.equals(STATUS_SEND_ONLY)) {
            return 5;
        }
        if (status.equals("connected") || status.equals(STATUS_MUTED_VIA_FOCUS) || status.equals(STATUS_DISCONNECTING) || status.equals(STATUS_SEND_RECV) || !status.equals(STATUS_DISCONNECTED)) {
            return 4;
        }
        return 6;
    }

    public String toString() {
        Set<Map.Entry<String, Bundle>> entries;
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append(ImsConferenceState.class.getSimpleName());
        sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
        if (this.mParticipants.size() > 0 && (entries = this.mParticipants.entrySet()) != null) {
            sb.append("<");
            for (Map.Entry<String, Bundle> entry : entries) {
                sb.append(Log.pii(entry.getKey()));
                sb.append(": ");
                Bundle participantData = entry.getValue();
                for (String key : participantData.keySet()) {
                    sb.append(key);
                    sb.append("=");
                    if (ENDPOINT.equals(key) || "user".equals(key) || USER_ENTITY.equals(key) || DISPLAY_TEXT.equals(key)) {
                        sb.append(Log.pii(participantData.get(key)));
                    } else {
                        sb.append(participantData.get(key));
                    }
                    sb.append(", ");
                }
            }
            sb.append(">");
        }
        sb.append("]");
        return sb.toString();
    }
}
