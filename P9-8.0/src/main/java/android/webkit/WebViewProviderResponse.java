package android.webkit;

import android.content.pm.PackageInfo;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class WebViewProviderResponse implements Parcelable {
    public static final Creator<WebViewProviderResponse> CREATOR = new Creator<WebViewProviderResponse>() {
        public WebViewProviderResponse createFromParcel(Parcel in) {
            return new WebViewProviderResponse(in, null);
        }

        public WebViewProviderResponse[] newArray(int size) {
            return new WebViewProviderResponse[size];
        }
    };
    public final PackageInfo packageInfo;
    public final int status;

    public WebViewProviderResponse(PackageInfo packageInfo, int status) {
        this.packageInfo = packageInfo;
        this.status = status;
    }

    private WebViewProviderResponse(Parcel in) {
        this.packageInfo = (PackageInfo) in.readTypedObject(PackageInfo.CREATOR);
        this.status = in.readInt();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeTypedObject(this.packageInfo, flags);
        out.writeInt(this.status);
    }
}
