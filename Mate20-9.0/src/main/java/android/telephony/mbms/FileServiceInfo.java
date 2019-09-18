package android.telephony.mbms;

import android.annotation.SystemApi;
import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class FileServiceInfo extends ServiceInfo implements Parcelable {
    public static final Parcelable.Creator<FileServiceInfo> CREATOR = new Parcelable.Creator<FileServiceInfo>() {
        public FileServiceInfo createFromParcel(Parcel source) {
            return new FileServiceInfo(source);
        }

        public FileServiceInfo[] newArray(int size) {
            return new FileServiceInfo[size];
        }
    };
    private final List<FileInfo> files;

    @SystemApi
    public FileServiceInfo(Map<Locale, String> newNames, String newClassName, List<Locale> newLocales, String newServiceId, Date start, Date end, List<FileInfo> newFiles) {
        super(newNames, newClassName, newLocales, newServiceId, start, end);
        this.files = new ArrayList(newFiles);
    }

    FileServiceInfo(Parcel in) {
        super(in);
        this.files = new ArrayList();
        in.readList(this.files, FileInfo.class.getClassLoader());
    }

    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeList(this.files);
    }

    public int describeContents() {
        return 0;
    }

    public List<FileInfo> getFiles() {
        return this.files;
    }
}
