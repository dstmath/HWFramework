package android.telecom;

import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import com.android.internal.telecom.IVideoProvider;
import java.util.ArrayList;
import java.util.List;

public final class ParcelableConference implements Parcelable {
    public static final Parcelable.Creator<ParcelableConference> CREATOR = new Parcelable.Creator<ParcelableConference>() {
        /* class android.telecom.ParcelableConference.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ParcelableConference createFromParcel(Parcel source) {
            ClassLoader classLoader = ParcelableConference.class.getClassLoader();
            int state = source.readInt();
            int capabilities = source.readInt();
            List<String> connectionIds = new ArrayList<>(2);
            source.readList(connectionIds, classLoader);
            long connectTimeMillis = source.readLong();
            Bundle extras = source.readBundle(classLoader);
            return new ParcelableConference((PhoneAccountHandle) source.readParcelable(classLoader), state, capabilities, source.readInt(), connectionIds, IVideoProvider.Stub.asInterface(source.readStrongBinder()), source.readInt(), connectTimeMillis, source.readLong(), (StatusHints) source.readParcelable(classLoader), extras, (Uri) source.readParcelable(classLoader), source.readInt(), source.readString(), source.readInt());
        }

        @Override // android.os.Parcelable.Creator
        public ParcelableConference[] newArray(int size) {
            return new ParcelableConference[size];
        }
    };
    private final Uri mAddress;
    private final int mAddressPresentation;
    private final String mCallerDisplayName;
    private final int mCallerDisplayNamePresentation;
    private long mConnectElapsedTimeMillis = 0;
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

    public ParcelableConference(PhoneAccountHandle phoneAccount, int state, int connectionCapabilities, int connectionProperties, List<String> connectionIds, IVideoProvider videoProvider, int videoState, long connectTimeMillis, long connectElapsedTimeMillis, StatusHints statusHints, Bundle extras, Uri address, int addressPresentation, String callerDisplayName, int callerDisplayNamePresentation) {
        this.mPhoneAccount = phoneAccount;
        this.mState = state;
        this.mConnectionCapabilities = connectionCapabilities;
        this.mConnectionProperties = connectionProperties;
        this.mConnectionIds = connectionIds;
        this.mVideoProvider = videoProvider;
        this.mVideoState = videoState;
        this.mConnectTimeMillis = connectTimeMillis;
        this.mStatusHints = statusHints;
        this.mExtras = extras;
        this.mConnectElapsedTimeMillis = connectElapsedTimeMillis;
        this.mAddress = address;
        this.mAddressPresentation = addressPresentation;
        this.mCallerDisplayName = callerDisplayName;
        this.mCallerDisplayNamePresentation = callerDisplayNamePresentation;
    }

    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("account: ");
        stringBuffer.append(this.mPhoneAccount);
        stringBuffer.append(", state: ");
        stringBuffer.append(Connection.stateToString(this.mState));
        stringBuffer.append(", capabilities: ");
        stringBuffer.append(Connection.capabilitiesToString(this.mConnectionCapabilities));
        stringBuffer.append(", properties: ");
        stringBuffer.append(Connection.propertiesToString(this.mConnectionProperties));
        stringBuffer.append(", connectTime: ");
        stringBuffer.append(this.mConnectTimeMillis);
        stringBuffer.append(", children: ");
        stringBuffer.append(this.mConnectionIds);
        stringBuffer.append(", VideoState: ");
        stringBuffer.append(this.mVideoState);
        stringBuffer.append(", VideoProvider: ");
        stringBuffer.append(this.mVideoProvider);
        return stringBuffer.toString();
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

    public long getConnectElapsedTimeMillis() {
        return this.mConnectElapsedTimeMillis;
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

    public Uri getHandle() {
        return this.mAddress;
    }

    public int getHandlePresentation() {
        return this.mAddressPresentation;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel destination, int flags) {
        destination.writeParcelable(this.mPhoneAccount, 0);
        destination.writeInt(this.mState);
        destination.writeInt(this.mConnectionCapabilities);
        destination.writeList(this.mConnectionIds);
        destination.writeLong(this.mConnectTimeMillis);
        IVideoProvider iVideoProvider = this.mVideoProvider;
        destination.writeStrongBinder(iVideoProvider != null ? iVideoProvider.asBinder() : null);
        destination.writeInt(this.mVideoState);
        destination.writeParcelable(this.mStatusHints, 0);
        destination.writeBundle(this.mExtras);
        destination.writeInt(this.mConnectionProperties);
        destination.writeLong(this.mConnectElapsedTimeMillis);
        destination.writeParcelable(this.mAddress, 0);
        destination.writeInt(this.mAddressPresentation);
        destination.writeString(this.mCallerDisplayName);
        destination.writeInt(this.mCallerDisplayNamePresentation);
    }
}
