package android.net.wifi.hotspot2;

import android.graphics.drawable.Icon;
import android.net.Uri;
import android.net.wifi.WifiSsid;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class OsuProvider implements Parcelable {
    public static final Parcelable.Creator<OsuProvider> CREATOR = new Parcelable.Creator<OsuProvider>() {
        public OsuProvider createFromParcel(Parcel in) {
            Parcel parcel = in;
            String friendlyName = in.readString();
            String serviceDescription = in.readString();
            String nai = in.readString();
            List<Integer> methodList = new ArrayList<>();
            parcel.readList(methodList, null);
            OsuProvider osuProvider = new OsuProvider((WifiSsid) parcel.readParcelable(null), friendlyName, serviceDescription, (Uri) parcel.readParcelable(null), nai, methodList, (Icon) parcel.readParcelable(null));
            return osuProvider;
        }

        public OsuProvider[] newArray(int size) {
            return new OsuProvider[size];
        }
    };
    public static final int METHOD_OMA_DM = 0;
    public static final int METHOD_SOAP_XML_SPP = 1;
    private final String mFriendlyName;
    private final Icon mIcon;
    private final List<Integer> mMethodList;
    private final String mNetworkAccessIdentifier;
    private final WifiSsid mOsuSsid;
    private final Uri mServerUri;
    private final String mServiceDescription;

    public OsuProvider(WifiSsid osuSsid, String friendlyName, String serviceDescription, Uri serverUri, String nai, List<Integer> methodList, Icon icon) {
        this.mOsuSsid = osuSsid;
        this.mFriendlyName = friendlyName;
        this.mServiceDescription = serviceDescription;
        this.mServerUri = serverUri;
        this.mNetworkAccessIdentifier = nai;
        if (methodList == null) {
            this.mMethodList = new ArrayList();
        } else {
            this.mMethodList = new ArrayList(methodList);
        }
        this.mIcon = icon;
    }

    public OsuProvider(OsuProvider source) {
        if (source == null) {
            this.mOsuSsid = null;
            this.mFriendlyName = null;
            this.mServiceDescription = null;
            this.mServerUri = null;
            this.mNetworkAccessIdentifier = null;
            this.mMethodList = new ArrayList();
            this.mIcon = null;
            return;
        }
        this.mOsuSsid = source.mOsuSsid;
        this.mFriendlyName = source.mFriendlyName;
        this.mServiceDescription = source.mServiceDescription;
        this.mServerUri = source.mServerUri;
        this.mNetworkAccessIdentifier = source.mNetworkAccessIdentifier;
        if (source.mMethodList == null) {
            this.mMethodList = new ArrayList();
        } else {
            this.mMethodList = new ArrayList(source.mMethodList);
        }
        this.mIcon = source.mIcon;
    }

    public WifiSsid getOsuSsid() {
        return this.mOsuSsid;
    }

    public String getFriendlyName() {
        return this.mFriendlyName;
    }

    public String getServiceDescription() {
        return this.mServiceDescription;
    }

    public Uri getServerUri() {
        return this.mServerUri;
    }

    public String getNetworkAccessIdentifier() {
        return this.mNetworkAccessIdentifier;
    }

    public List<Integer> getMethodList() {
        return Collections.unmodifiableList(this.mMethodList);
    }

    public Icon getIcon() {
        return this.mIcon;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.mOsuSsid, flags);
        dest.writeString(this.mFriendlyName);
        dest.writeString(this.mServiceDescription);
        dest.writeParcelable(this.mServerUri, flags);
        dest.writeString(this.mNetworkAccessIdentifier);
        dest.writeList(this.mMethodList);
        dest.writeParcelable(this.mIcon, flags);
    }

    public boolean equals(Object thatObject) {
        boolean z = true;
        if (this == thatObject) {
            return true;
        }
        if (!(thatObject instanceof OsuProvider)) {
            return false;
        }
        OsuProvider that = (OsuProvider) thatObject;
        if (this.mOsuSsid != null ? this.mOsuSsid.equals(that.mOsuSsid) : that.mOsuSsid == null) {
            if (TextUtils.equals(this.mFriendlyName, that.mFriendlyName)) {
                if (TextUtils.equals(this.mServiceDescription, that.mServiceDescription)) {
                    if (this.mServerUri == null) {
                    }
                    if (TextUtils.equals(this.mNetworkAccessIdentifier, that.mNetworkAccessIdentifier)) {
                        if (this.mMethodList == null) {
                        }
                        if (this.mIcon == null) {
                        }
                    }
                }
            }
        }
        z = false;
        return z;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.mOsuSsid, this.mFriendlyName, this.mServiceDescription, this.mServerUri, this.mNetworkAccessIdentifier, this.mMethodList, this.mIcon});
    }

    public String toString() {
        return "OsuProvider{mOsuSsid=" + this.mOsuSsid + " mFriendlyName=" + this.mFriendlyName + " mServiceDescription=" + this.mServiceDescription + " mServerUri=" + this.mServerUri + " mNetworkAccessIdentifier=" + this.mNetworkAccessIdentifier + " mMethodList=" + this.mMethodList + " mIcon=" + this.mIcon;
    }
}
