package android.telecom;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

public class GatewayInfo implements Parcelable {
    public static final Parcelable.Creator<GatewayInfo> CREATOR = new Parcelable.Creator<GatewayInfo>() {
        /* class android.telecom.GatewayInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public GatewayInfo createFromParcel(Parcel source) {
            return new GatewayInfo(source.readString(), Uri.CREATOR.createFromParcel(source), Uri.CREATOR.createFromParcel(source));
        }

        @Override // android.os.Parcelable.Creator
        public GatewayInfo[] newArray(int size) {
            return new GatewayInfo[size];
        }
    };
    private final Uri mGatewayAddress;
    private final String mGatewayProviderPackageName;
    private final Uri mOriginalAddress;

    public GatewayInfo(String packageName, Uri gatewayUri, Uri originalAddress) {
        this.mGatewayProviderPackageName = packageName;
        this.mGatewayAddress = gatewayUri;
        this.mOriginalAddress = originalAddress;
    }

    public String getGatewayProviderPackageName() {
        return this.mGatewayProviderPackageName;
    }

    public Uri getGatewayAddress() {
        return this.mGatewayAddress;
    }

    public Uri getOriginalAddress() {
        return this.mOriginalAddress;
    }

    public boolean isEmpty() {
        return TextUtils.isEmpty(this.mGatewayProviderPackageName) || this.mGatewayAddress == null;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel destination, int flags) {
        destination.writeString(this.mGatewayProviderPackageName);
        this.mGatewayAddress.writeToParcel(destination, 0);
        this.mOriginalAddress.writeToParcel(destination, 0);
    }
}
