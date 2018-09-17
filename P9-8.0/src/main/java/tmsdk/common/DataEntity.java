package tmsdk.common;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class DataEntity implements Parcelable {
    public static final Creator<DataEntity> CREATOR = new Creator<DataEntity>() {
        /* renamed from: aC */
        public DataEntity[] newArray(int i) {
            return new DataEntity[i];
        }

        /* renamed from: d */
        public DataEntity createFromParcel(Parcel parcel) {
            return new DataEntity(parcel, null);
        }
    };
    private Bundle wX;
    private int wY;

    public DataEntity(int i) {
        this.wY = i;
        this.wX = new Bundle();
    }

    private DataEntity(Parcel parcel) {
        this.wY = parcel.readInt();
        this.wX = parcel.readBundle();
    }

    /* synthetic */ DataEntity(Parcel parcel, AnonymousClass1 anonymousClass1) {
        this(parcel);
    }

    public Bundle bundle() {
        return this.wX;
    }

    public int describeContents() {
        return 0;
    }

    public int what() {
        return this.wY;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.wY);
        parcel.writeBundle(this.wX);
    }
}
