package android.telecom;

import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.android.internal.telecom.IVideoProvider;
import com.android.internal.telecom.IVideoProvider.Stub;
import java.util.ArrayList;
import java.util.List;

public final class ParcelableConference implements Parcelable {
    public static final Creator<ParcelableConference> CREATOR = new Creator<ParcelableConference>() {
        public ParcelableConference createFromParcel(Parcel source) {
            ClassLoader classLoader = ParcelableConference.class.getClassLoader();
            PhoneAccountHandle phoneAccount = (PhoneAccountHandle) source.readParcelable(classLoader);
            int state = source.readInt();
            int capabilities = source.readInt();
            List<String> connectionIds = new ArrayList(2);
            source.readList(connectionIds, classLoader);
            long connectTimeMillis = source.readLong();
            return new ParcelableConference(phoneAccount, state, capabilities, source.readInt(), connectionIds, Stub.asInterface(source.readStrongBinder()), source.readInt(), connectTimeMillis, (StatusHints) source.readParcelable(classLoader), source.readBundle(classLoader));
        }

        public ParcelableConference[] newArray(int size) {
            return new ParcelableConference[size];
        }
    };
    private long mConnectTimeMillis = 0;
    private int mConnectionCapabilities;
    private List<String> mConnectionIds;
    private int mConnectionProperties;
    private Bundle mExtras;
    private PhoneAccountHandle mPhoneAccount;
    private int mState;
    private StatusHints mStatusHints;
    private final IVideoProvider mVideoProvider;
    private final int mVideoState;

    public ParcelableConference(PhoneAccountHandle phoneAccount, int state, int connectionCapabilities, int connectionProperties, List<String> connectionIds, IVideoProvider videoProvider, int videoState, long connectTimeMillis, StatusHints statusHints, Bundle extras) {
        this.mPhoneAccount = phoneAccount;
        this.mState = state;
        this.mConnectionCapabilities = connectionCapabilities;
        this.mConnectionProperties = connectionProperties;
        this.mConnectionIds = connectionIds;
        this.mConnectTimeMillis = 0;
        this.mVideoProvider = videoProvider;
        this.mVideoState = videoState;
        this.mConnectTimeMillis = connectTimeMillis;
        this.mStatusHints = statusHints;
        this.mExtras = extras;
    }

    public String toString() {
        return new StringBuffer().append("account: ").append(this.mPhoneAccount).append(", state: ").append(Connection.stateToString(this.mState)).append(", capabilities: ").append(Connection.capabilitiesToString(this.mConnectionCapabilities)).append(", properties: ").append(Connection.propertiesToString(this.mConnectionProperties)).append(", connectTime: ").append(this.mConnectTimeMillis).append(", children: ").append(this.mConnectionIds).append(", VideoState: ").append(this.mVideoState).append(", VideoProvider: ").append(this.mVideoProvider).toString();
    }

    public PhoneAccountHandle getPhoneAccount() {
        return this.mPhoneAccount;
    }

    public int getState() {
        return this.mState;
    }

    public int getConnectionCapabilities() {
        return this.mConnectionCapabilities;
    }

    public int getConnectionProperties() {
        return this.mConnectionProperties;
    }

    public List<String> getConnectionIds() {
        return this.mConnectionIds;
    }

    public long getConnectTimeMillis() {
        return this.mConnectTimeMillis;
    }

    public IVideoProvider getVideoProvider() {
        return this.mVideoProvider;
    }

    public int getVideoState() {
        return this.mVideoState;
    }

    public StatusHints getStatusHints() {
        return this.mStatusHints;
    }

    public Bundle getExtras() {
        return this.mExtras;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel destination, int flags) {
        IBinder iBinder = null;
        destination.writeParcelable(this.mPhoneAccount, 0);
        destination.writeInt(this.mState);
        destination.writeInt(this.mConnectionCapabilities);
        destination.writeList(this.mConnectionIds);
        destination.writeLong(this.mConnectTimeMillis);
        if (this.mVideoProvider != null) {
            iBinder = this.mVideoProvider.asBinder();
        }
        destination.writeStrongBinder(iBinder);
        destination.writeInt(this.mVideoState);
        destination.writeParcelable(this.mStatusHints, 0);
        destination.writeBundle(this.mExtras);
        destination.writeInt(this.mConnectionProperties);
    }
}
