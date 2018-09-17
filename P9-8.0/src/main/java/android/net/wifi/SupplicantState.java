package android.net.wifi;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public enum SupplicantState implements Parcelable {
    DISCONNECTED,
    INTERFACE_DISABLED,
    INACTIVE,
    SCANNING,
    AUTHENTICATING,
    ASSOCIATING,
    ASSOCIATED,
    FOUR_WAY_HANDSHAKE,
    GROUP_HANDSHAKE,
    COMPLETED,
    DORMANT,
    UNINITIALIZED,
    INVALID;
    
    public static final Creator<SupplicantState> CREATOR = null;

    static {
        CREATOR = new Creator<SupplicantState>() {
            public SupplicantState createFromParcel(Parcel in) {
                return SupplicantState.valueOf(in.readString());
            }

            public SupplicantState[] newArray(int size) {
                return new SupplicantState[size];
            }
        };
    }

    public static boolean isValidState(SupplicantState state) {
        return (state == UNINITIALIZED || state == INVALID) ? false : true;
    }

    public static boolean isHandshakeState(SupplicantState state) {
        switch (-getandroid-net-wifi-SupplicantStateSwitchesValues()[state.ordinal()]) {
            case 1:
            case 2:
            case 3:
            case 7:
            case 8:
                return true;
            case 4:
            case 5:
            case 6:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
                return false;
            default:
                throw new IllegalArgumentException("Unknown supplicant state");
        }
    }

    public static boolean isConnecting(SupplicantState state) {
        switch (-getandroid-net-wifi-SupplicantStateSwitchesValues()[state.ordinal()]) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 7:
            case 8:
                return true;
            case 5:
            case 6:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
                return false;
            default:
                throw new IllegalArgumentException("Unknown supplicant state");
        }
    }

    public static boolean isDriverActive(SupplicantState state) {
        switch (-getandroid-net-wifi-SupplicantStateSwitchesValues()[state.ordinal()]) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 12:
                return true;
            case 10:
            case 11:
            case 13:
                return false;
            default:
                throw new IllegalArgumentException("Unknown supplicant state");
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name());
    }
}
