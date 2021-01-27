package android.webkit;

import android.annotation.SystemApi;
import android.annotation.UnsupportedAppUsage;
import android.content.pm.Signature;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Base64;

@SystemApi
public final class WebViewProviderInfo implements Parcelable {
    public static final Parcelable.Creator<WebViewProviderInfo> CREATOR = new Parcelable.Creator<WebViewProviderInfo>() {
        /* class android.webkit.WebViewProviderInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public WebViewProviderInfo createFromParcel(Parcel in) {
            return new WebViewProviderInfo(in);
        }

        @Override // android.os.Parcelable.Creator
        public WebViewProviderInfo[] newArray(int size) {
            return new WebViewProviderInfo[size];
        }
    };
    public final boolean availableByDefault;
    public final String description;
    public final boolean isFallback;
    public final String packageName;
    public final Signature[] signatures;

    public WebViewProviderInfo(String packageName2, String description2, boolean availableByDefault2, boolean isFallback2, String[] signatures2) {
        this.packageName = packageName2;
        this.description = description2;
        this.availableByDefault = availableByDefault2;
        this.isFallback = isFallback2;
        if (signatures2 == null) {
            this.signatures = new Signature[0];
            return;
        }
        this.signatures = new Signature[signatures2.length];
        for (int n = 0; n < signatures2.length; n++) {
            this.signatures[n] = new Signature(Base64.decode(signatures2[n], 0));
        }
    }

    @UnsupportedAppUsage
    private WebViewProviderInfo(Parcel in) {
        this.packageName = in.readString();
        this.description = in.readString();
        boolean z = true;
        this.availableByDefault = in.readInt() > 0;
        this.isFallback = in.readInt() <= 0 ? false : z;
        this.signatures = (Signature[]) in.createTypedArray(Signature.CREATOR);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.packageName);
        out.writeString(this.description);
        out.writeInt(this.availableByDefault ? 1 : 0);
        out.writeInt(this.isFallback ? 1 : 0);
        out.writeTypedArray(this.signatures, 0);
    }
}
