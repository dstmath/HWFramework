package android.net.wifi.hotspot2;

import android.annotation.SystemApi;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.net.wifi.WifiSsid;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@SystemApi
public final class OsuProvider implements Parcelable {
    public static final Parcelable.Creator<OsuProvider> CREATOR = new Parcelable.Creator<OsuProvider>() {
        /* class android.net.wifi.hotspot2.OsuProvider.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public OsuProvider createFromParcel(Parcel in) {
            String serviceDescription = in.readString();
            Uri serverUri = (Uri) in.readParcelable(null);
            String nai = in.readString();
            List<Integer> methodList = new ArrayList<>();
            in.readList(methodList, null);
            return new OsuProvider((WifiSsid) in.readParcelable(null), (HashMap) in.readBundle().getSerializable("friendlyNameMap"), serviceDescription, serverUri, nai, methodList, (Icon) in.readParcelable(null));
        }

        @Override // android.os.Parcelable.Creator
        public OsuProvider[] newArray(int size) {
            return new OsuProvider[size];
        }
    };
    public static final int METHOD_OMA_DM = 0;
    public static final int METHOD_SOAP_XML_SPP = 1;
    private final Map<String, String> mFriendlyNames;
    private final Icon mIcon;
    private final List<Integer> mMethodList;
    private final String mNetworkAccessIdentifier;
    private WifiSsid mOsuSsid;
    private final Uri mServerUri;
    private final String mServiceDescription;

    public OsuProvider(WifiSsid osuSsid, Map<String, String> friendlyNames, String serviceDescription, Uri serverUri, String nai, List<Integer> methodList, Icon icon) {
        this.mOsuSsid = osuSsid;
        this.mFriendlyNames = friendlyNames;
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
            this.mFriendlyNames = null;
            this.mServiceDescription = null;
            this.mServerUri = null;
            this.mNetworkAccessIdentifier = null;
            this.mMethodList = new ArrayList();
            this.mIcon = null;
            return;
        }
        this.mOsuSsid = source.mOsuSsid;
        this.mFriendlyNames = source.mFriendlyNames;
        this.mServiceDescription = source.mServiceDescription;
        this.mServerUri = source.mServerUri;
        this.mNetworkAccessIdentifier = source.mNetworkAccessIdentifier;
        List<Integer> list = source.mMethodList;
        if (list == null) {
            this.mMethodList = new ArrayList();
        } else {
            this.mMethodList = new ArrayList(list);
        }
        this.mIcon = source.mIcon;
    }

    public WifiSsid getOsuSsid() {
        return this.mOsuSsid;
    }

    public void setOsuSsid(WifiSsid osuSsid) {
        this.mOsuSsid = osuSsid;
    }

    public String getFriendlyName() {
        Map<String, String> map = this.mFriendlyNames;
        if (map == null || map.isEmpty()) {
            return null;
        }
        String friendlyName = this.mFriendlyNames.get(Locale.getDefault().getLanguage());
        if (friendlyName != null) {
            return friendlyName;
        }
        String friendlyName2 = this.mFriendlyNames.get("en");
        if (friendlyName2 != null) {
            return friendlyName2;
        }
        Map<String, String> map2 = this.mFriendlyNames;
        return map2.get(map2.keySet().stream().findFirst().get());
    }

    public Map<String, String> getFriendlyNameList() {
        return this.mFriendlyNames;
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
        return this.mMethodList;
    }

    public Icon getIcon() {
        return this.mIcon;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.mOsuSsid, flags);
        dest.writeString(this.mServiceDescription);
        dest.writeParcelable(this.mServerUri, flags);
        dest.writeString(this.mNetworkAccessIdentifier);
        dest.writeList(this.mMethodList);
        dest.writeParcelable(this.mIcon, flags);
        Bundle bundle = new Bundle();
        bundle.putSerializable("friendlyNameMap", (HashMap) this.mFriendlyNames);
        dest.writeBundle(bundle);
    }

    public boolean equals(Object thatObject) {
        Uri uri;
        List<Integer> list;
        if (this == thatObject) {
            return true;
        }
        if (!(thatObject instanceof OsuProvider)) {
            return false;
        }
        OsuProvider that = (OsuProvider) thatObject;
        WifiSsid wifiSsid = this.mOsuSsid;
        if (wifiSsid != null ? wifiSsid.equals(that.mOsuSsid) : that.mOsuSsid == null) {
            if (this.mFriendlyNames == null) {
                if (that.mFriendlyNames == null) {
                    return true;
                }
                return false;
            }
        }
        if (this.mFriendlyNames.equals(that.mFriendlyNames) && TextUtils.equals(this.mServiceDescription, that.mServiceDescription) && ((uri = this.mServerUri) != null ? uri.equals(that.mServerUri) : that.mServerUri == null) && TextUtils.equals(this.mNetworkAccessIdentifier, that.mNetworkAccessIdentifier) && ((list = this.mMethodList) != null ? list.equals(that.mMethodList) : that.mMethodList == null)) {
            Icon icon = this.mIcon;
            if (icon == null) {
                if (that.mIcon == null) {
                    return true;
                }
            } else if (icon.sameAs(that.mIcon)) {
                return true;
            }
        }
        return false;
    }

    public int hashCode() {
        return Objects.hash(this.mOsuSsid, this.mServiceDescription, this.mFriendlyNames, this.mServerUri, this.mNetworkAccessIdentifier, this.mMethodList);
    }

    public String toString() {
        return "OsuProvider{mOsuSsid=" + this.mOsuSsid + " mFriendlyNames=" + this.mFriendlyNames + " mServiceDescription=" + this.mServiceDescription + " mServerUri=" + this.mServerUri + " mNetworkAccessIdentifier=" + this.mNetworkAccessIdentifier + " mMethodList=" + this.mMethodList + " mIcon=" + this.mIcon;
    }
}
