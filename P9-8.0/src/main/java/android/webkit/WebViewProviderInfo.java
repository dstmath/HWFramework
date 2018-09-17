package android.webkit;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class WebViewProviderInfo implements Parcelable {
    public static final Creator<WebViewProviderInfo> CREATOR = new Creator<WebViewProviderInfo>() {
        public WebViewProviderInfo createFromParcel(Parcel in) {
            return new WebViewProviderInfo(in, null);
        }

        public WebViewProviderInfo[] newArray(int size) {
            return new WebViewProviderInfo[size];
        }
    };
    public final boolean availableByDefault;
    public final String description;
    public final boolean isFallback;
    public final String packageName;
    public final String[] signatures;

    /* synthetic */ WebViewProviderInfo(Parcel in, WebViewProviderInfo -this1) {
        this(in);
    }

    public WebViewProviderInfo(String packageName, String description, boolean availableByDefault, boolean isFallback, String[] signatures) {
        this.packageName = packageName;
        this.description = description;
        this.availableByDefault = availableByDefault;
        this.isFallback = isFallback;
        this.signatures = signatures;
    }

    private WebViewProviderInfo(Parcel in) {
        boolean z;
        boolean z2 = true;
        this.packageName = in.readString();
        this.description = in.readString();
        if (in.readInt() > 0) {
            z = true;
        } else {
            z = false;
        }
        this.availableByDefault = z;
        if (in.readInt() <= 0) {
            z2 = false;
        }
        this.isFallback = z2;
        this.signatures = in.createStringArray();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        int i;
        int i2 = 1;
        out.writeString(this.packageName);
        out.writeString(this.description);
        if (this.availableByDefault) {
            i = 1;
        } else {
            i = 0;
        }
        out.writeInt(i);
        if (!this.isFallback) {
            i2 = 0;
        }
        out.writeInt(i2);
        out.writeStringArray(this.signatures);
    }
}
