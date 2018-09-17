package tmsdk.common.module.qscanner.impl;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.ArrayList;

public class b implements Parcelable {
    public static final Creator<b> CREATOR = new Creator<b>() {
        /* renamed from: bb */
        public b[] newArray(int i) {
            return new b[i];
        }

        /* renamed from: l */
        public b createFromParcel(Parcel parcel) {
            b bVar = new b();
            bVar.id = parcel.readInt();
            bVar.type = parcel.readInt();
            bVar.BR = parcel.readLong();
            bVar.banUrls = parcel.createStringArrayList();
            bVar.banIps = parcel.createStringArrayList();
            bVar.name = parcel.readString();
            return bVar;
        }
    };
    public long BR = 0;
    public ArrayList<String> banIps = null;
    public ArrayList<String> banUrls = null;
    public int id = 0;
    public String name = null;
    public int type = 0;

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.id);
        parcel.writeInt(this.type);
        parcel.writeLong(this.BR);
        parcel.writeStringList(this.banUrls);
        parcel.writeStringList(this.banIps);
        parcel.writeString(this.name);
    }
}
