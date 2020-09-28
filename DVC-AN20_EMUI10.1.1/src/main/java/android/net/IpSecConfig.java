package android.net;

import android.os.Parcel;
import android.os.Parcelable;
import com.android.internal.annotations.VisibleForTesting;

public final class IpSecConfig implements Parcelable {
    public static final Parcelable.Creator<IpSecConfig> CREATOR = new Parcelable.Creator<IpSecConfig>() {
        /* class android.net.IpSecConfig.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public IpSecConfig createFromParcel(Parcel in) {
            return new IpSecConfig(in);
        }

        @Override // android.os.Parcelable.Creator
        public IpSecConfig[] newArray(int size) {
            return new IpSecConfig[size];
        }
    };
    private static final String TAG = "IpSecConfig";
    private IpSecAlgorithm mAuthenticatedEncryption;
    private IpSecAlgorithm mAuthentication;
    private String mDestinationAddress;
    private int mEncapRemotePort;
    private int mEncapSocketResourceId;
    private int mEncapType;
    private IpSecAlgorithm mEncryption;
    private int mMarkMask;
    private int mMarkValue;
    private int mMode;
    private int mNattKeepaliveInterval;
    private Network mNetwork;
    private String mSourceAddress;
    private int mSpiResourceId;
    private int mXfrmInterfaceId;

    public void setMode(int mode) {
        this.mMode = mode;
    }

    public void setSourceAddress(String sourceAddress) {
        this.mSourceAddress = sourceAddress;
    }

    public void setDestinationAddress(String destinationAddress) {
        this.mDestinationAddress = destinationAddress;
    }

    public void setSpiResourceId(int resourceId) {
        this.mSpiResourceId = resourceId;
    }

    public void setEncryption(IpSecAlgorithm encryption) {
        this.mEncryption = encryption;
    }

    public void setAuthentication(IpSecAlgorithm authentication) {
        this.mAuthentication = authentication;
    }

    public void setAuthenticatedEncryption(IpSecAlgorithm authenticatedEncryption) {
        this.mAuthenticatedEncryption = authenticatedEncryption;
    }

    public void setNetwork(Network network) {
        this.mNetwork = network;
    }

    public void setEncapType(int encapType) {
        this.mEncapType = encapType;
    }

    public void setEncapSocketResourceId(int resourceId) {
        this.mEncapSocketResourceId = resourceId;
    }

    public void setEncapRemotePort(int port) {
        this.mEncapRemotePort = port;
    }

    public void setNattKeepaliveInterval(int interval) {
        this.mNattKeepaliveInterval = interval;
    }

    public void setMarkValue(int mark) {
        this.mMarkValue = mark;
    }

    public void setMarkMask(int mask) {
        this.mMarkMask = mask;
    }

    public void setXfrmInterfaceId(int xfrmInterfaceId) {
        this.mXfrmInterfaceId = xfrmInterfaceId;
    }

    public int getMode() {
        return this.mMode;
    }

    public String getSourceAddress() {
        return this.mSourceAddress;
    }

    public int getSpiResourceId() {
        return this.mSpiResourceId;
    }

    public String getDestinationAddress() {
        return this.mDestinationAddress;
    }

    public IpSecAlgorithm getEncryption() {
        return this.mEncryption;
    }

    public IpSecAlgorithm getAuthentication() {
        return this.mAuthentication;
    }

    public IpSecAlgorithm getAuthenticatedEncryption() {
        return this.mAuthenticatedEncryption;
    }

    public Network getNetwork() {
        return this.mNetwork;
    }

    public int getEncapType() {
        return this.mEncapType;
    }

    public int getEncapSocketResourceId() {
        return this.mEncapSocketResourceId;
    }

    public int getEncapRemotePort() {
        return this.mEncapRemotePort;
    }

    public int getNattKeepaliveInterval() {
        return this.mNattKeepaliveInterval;
    }

    public int getMarkValue() {
        return this.mMarkValue;
    }

    public int getMarkMask() {
        return this.mMarkMask;
    }

    public int getXfrmInterfaceId() {
        return this.mXfrmInterfaceId;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.mMode);
        out.writeString(this.mSourceAddress);
        out.writeString(this.mDestinationAddress);
        out.writeParcelable(this.mNetwork, flags);
        out.writeInt(this.mSpiResourceId);
        out.writeParcelable(this.mEncryption, flags);
        out.writeParcelable(this.mAuthentication, flags);
        out.writeParcelable(this.mAuthenticatedEncryption, flags);
        out.writeInt(this.mEncapType);
        out.writeInt(this.mEncapSocketResourceId);
        out.writeInt(this.mEncapRemotePort);
        out.writeInt(this.mNattKeepaliveInterval);
        out.writeInt(this.mMarkValue);
        out.writeInt(this.mMarkMask);
        out.writeInt(this.mXfrmInterfaceId);
    }

    @VisibleForTesting
    public IpSecConfig() {
        this.mMode = 0;
        this.mSourceAddress = "";
        this.mDestinationAddress = "";
        this.mSpiResourceId = -1;
        this.mEncapType = 0;
        this.mEncapSocketResourceId = -1;
    }

    @VisibleForTesting
    public IpSecConfig(IpSecConfig c) {
        this.mMode = 0;
        this.mSourceAddress = "";
        this.mDestinationAddress = "";
        this.mSpiResourceId = -1;
        this.mEncapType = 0;
        this.mEncapSocketResourceId = -1;
        this.mMode = c.mMode;
        this.mSourceAddress = c.mSourceAddress;
        this.mDestinationAddress = c.mDestinationAddress;
        this.mNetwork = c.mNetwork;
        this.mSpiResourceId = c.mSpiResourceId;
        this.mEncryption = c.mEncryption;
        this.mAuthentication = c.mAuthentication;
        this.mAuthenticatedEncryption = c.mAuthenticatedEncryption;
        this.mEncapType = c.mEncapType;
        this.mEncapSocketResourceId = c.mEncapSocketResourceId;
        this.mEncapRemotePort = c.mEncapRemotePort;
        this.mNattKeepaliveInterval = c.mNattKeepaliveInterval;
        this.mMarkValue = c.mMarkValue;
        this.mMarkMask = c.mMarkMask;
        this.mXfrmInterfaceId = c.mXfrmInterfaceId;
    }

    private IpSecConfig(Parcel in) {
        this.mMode = 0;
        this.mSourceAddress = "";
        this.mDestinationAddress = "";
        this.mSpiResourceId = -1;
        this.mEncapType = 0;
        this.mEncapSocketResourceId = -1;
        this.mMode = in.readInt();
        this.mSourceAddress = in.readString();
        this.mDestinationAddress = in.readString();
        this.mNetwork = (Network) in.readParcelable(Network.class.getClassLoader());
        this.mSpiResourceId = in.readInt();
        this.mEncryption = (IpSecAlgorithm) in.readParcelable(IpSecAlgorithm.class.getClassLoader());
        this.mAuthentication = (IpSecAlgorithm) in.readParcelable(IpSecAlgorithm.class.getClassLoader());
        this.mAuthenticatedEncryption = (IpSecAlgorithm) in.readParcelable(IpSecAlgorithm.class.getClassLoader());
        this.mEncapType = in.readInt();
        this.mEncapSocketResourceId = in.readInt();
        this.mEncapRemotePort = in.readInt();
        this.mNattKeepaliveInterval = in.readInt();
        this.mMarkValue = in.readInt();
        this.mMarkMask = in.readInt();
        this.mXfrmInterfaceId = in.readInt();
    }

    public String toString() {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("{mMode=");
        strBuilder.append(this.mMode == 1 ? "TUNNEL" : "TRANSPORT");
        strBuilder.append(", mSourceAddress=");
        strBuilder.append(this.mSourceAddress);
        strBuilder.append(", mDestinationAddress=");
        strBuilder.append(this.mDestinationAddress);
        strBuilder.append(", mNetwork=");
        strBuilder.append(this.mNetwork);
        strBuilder.append(", mEncapType=");
        strBuilder.append(this.mEncapType);
        strBuilder.append(", mEncapSocketResourceId=");
        strBuilder.append(this.mEncapSocketResourceId);
        strBuilder.append(", mEncapRemotePort=");
        strBuilder.append(this.mEncapRemotePort);
        strBuilder.append(", mNattKeepaliveInterval=");
        strBuilder.append(this.mNattKeepaliveInterval);
        strBuilder.append("{mSpiResourceId=");
        strBuilder.append(this.mSpiResourceId);
        strBuilder.append(", mEncryption=");
        strBuilder.append(this.mEncryption);
        strBuilder.append(", mAuthentication=");
        strBuilder.append(this.mAuthentication);
        strBuilder.append(", mAuthenticatedEncryption=");
        strBuilder.append(this.mAuthenticatedEncryption);
        strBuilder.append(", mMarkValue=");
        strBuilder.append(this.mMarkValue);
        strBuilder.append(", mMarkMask=");
        strBuilder.append(this.mMarkMask);
        strBuilder.append(", mXfrmInterfaceId=");
        strBuilder.append(this.mXfrmInterfaceId);
        strBuilder.append("}");
        return strBuilder.toString();
    }

    @VisibleForTesting
    public static boolean equals(IpSecConfig lhs, IpSecConfig rhs) {
        Network network;
        return (lhs == null || rhs == null) ? lhs == rhs : lhs.mMode == rhs.mMode && lhs.mSourceAddress.equals(rhs.mSourceAddress) && lhs.mDestinationAddress.equals(rhs.mDestinationAddress) && (((network = lhs.mNetwork) != null && network.equals(rhs.mNetwork)) || lhs.mNetwork == rhs.mNetwork) && lhs.mEncapType == rhs.mEncapType && lhs.mEncapSocketResourceId == rhs.mEncapSocketResourceId && lhs.mEncapRemotePort == rhs.mEncapRemotePort && lhs.mNattKeepaliveInterval == rhs.mNattKeepaliveInterval && lhs.mSpiResourceId == rhs.mSpiResourceId && IpSecAlgorithm.equals(lhs.mEncryption, rhs.mEncryption) && IpSecAlgorithm.equals(lhs.mAuthenticatedEncryption, rhs.mAuthenticatedEncryption) && IpSecAlgorithm.equals(lhs.mAuthentication, rhs.mAuthentication) && lhs.mMarkValue == rhs.mMarkValue && lhs.mMarkMask == rhs.mMarkMask && lhs.mXfrmInterfaceId == rhs.mXfrmInterfaceId;
    }
}
