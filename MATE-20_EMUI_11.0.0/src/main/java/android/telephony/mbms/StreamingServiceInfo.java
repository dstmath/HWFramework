package android.telephony.mbms;

import android.annotation.SystemApi;
import android.os.Parcel;
import android.os.Parcelable;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class StreamingServiceInfo extends ServiceInfo implements Parcelable {
    public static final Parcelable.Creator<StreamingServiceInfo> CREATOR = new Parcelable.Creator<StreamingServiceInfo>() {
        /* class android.telephony.mbms.StreamingServiceInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public StreamingServiceInfo createFromParcel(Parcel source) {
            return new StreamingServiceInfo(source);
        }

        @Override // android.os.Parcelable.Creator
        public StreamingServiceInfo[] newArray(int size) {
            return new StreamingServiceInfo[size];
        }
    };

    @SystemApi
    public StreamingServiceInfo(Map<Locale, String> names, String className, List<Locale> locales, String serviceId, Date start, Date end) {
        super(names, className, locales, serviceId, start, end);
    }

    private StreamingServiceInfo(Parcel in) {
        super(in);
    }

    @Override // android.telephony.mbms.ServiceInfo, android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }
}
