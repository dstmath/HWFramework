package android.webkit;

import android.annotation.UnsupportedAppUsage;
import android.content.pm.PackageInfo;
import android.os.Parcel;
import android.os.Parcelable;

public final class WebViewProviderResponse implements Parcelable {
    public static final Parcelable.Creator<WebViewProviderResponse> CREATOR = new Parcelable.Creator<WebViewProviderResponse>() {
        /* class android.webkit.WebViewProviderResponse.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public WebViewProviderResponse createFromParcel(Parcel in) {
            return new WebViewProviderResponse(in);
        }

        @Override // android.os.Parcelable.Creator
        public WebViewProviderResponse[] newArray(int size) {
            return new WebViewProviderResponse[size];
        }
    };
    @UnsupportedAppUsage
    public final PackageInfo packageInfo;
    public final int status;

    public WebViewProviderResponse(PackageInfo packageInfo2, int status2) {
        this.packageInfo = packageInfo2;
        this.status = status2;
    }

    private WebViewProviderResponse(Parcel in) {
        this.packageInfo = (PackageInfo) in.readTypedObject(PackageInfo.CREATOR);
        this.status = in.readInt();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeTypedObject(this.packageInfo, flags);
        out.writeInt(this.status);
    }
}
