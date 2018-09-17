package android.net.wifi;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class PasspointManagementObjectDefinition implements Parcelable {
    public static final Creator<PasspointManagementObjectDefinition> CREATOR = new Creator<PasspointManagementObjectDefinition>() {
        public PasspointManagementObjectDefinition createFromParcel(Parcel in) {
            return new PasspointManagementObjectDefinition(in.readString(), in.readString(), in.readString());
        }

        public PasspointManagementObjectDefinition[] newArray(int size) {
            return new PasspointManagementObjectDefinition[size];
        }
    };
    private final String mBaseUri;
    private final String mMoTree;
    private final String mUrn;

    public PasspointManagementObjectDefinition(String baseUri, String urn, String moTree) {
        this.mBaseUri = baseUri;
        this.mUrn = urn;
        this.mMoTree = moTree;
    }

    public String getBaseUri() {
        return this.mBaseUri;
    }

    public String getUrn() {
        return this.mUrn;
    }

    public String getMoTree() {
        return this.mMoTree;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mBaseUri);
        dest.writeString(this.mUrn);
        dest.writeString(this.mMoTree);
    }
}
