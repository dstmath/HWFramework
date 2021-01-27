package com.google.android.startop.iorap;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import com.android.server.zrhung.IZRHungService;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Objects;

public class PackageEvent implements Parcelable {
    public static final Parcelable.Creator<PackageEvent> CREATOR = new Parcelable.Creator<PackageEvent>() {
        /* class com.google.android.startop.iorap.PackageEvent.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public PackageEvent createFromParcel(Parcel in) {
            return new PackageEvent(in);
        }

        @Override // android.os.Parcelable.Creator
        public PackageEvent[] newArray(int size) {
            return new PackageEvent[size];
        }
    };
    private static final int TYPE_MAX = 0;
    public static final int TYPE_REPLACED = 0;
    public final String packageName;
    public final Uri packageUri;
    public final int type;

    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {
    }

    public static PackageEvent createReplaced(Uri packageUri2, String packageName2) {
        return new PackageEvent(0, packageUri2, packageName2);
    }

    private PackageEvent(int type2, Uri packageUri2, String packageName2) {
        this.type = type2;
        this.packageUri = packageUri2;
        this.packageName = packageName2;
        checkConstructorArguments();
    }

    private void checkConstructorArguments() {
        CheckHelpers.checkTypeInRange(this.type, 0);
        Objects.requireNonNull(this.packageUri, "packageUri");
        Objects.requireNonNull(this.packageName, IZRHungService.PARA_PACKAGENAME);
    }

    @Override // java.lang.Object
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof PackageEvent) {
            return equals((PackageEvent) other);
        }
        return false;
    }

    private boolean equals(PackageEvent other) {
        return this.type == other.type && Objects.equals(this.packageUri, other.packageUri) && Objects.equals(this.packageName, other.packageName);
    }

    @Override // java.lang.Object
    public String toString() {
        return String.format("{packageUri: %s, packageName: %s}", this.packageUri, this.packageName);
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.type);
        this.packageUri.writeToParcel(out, flags);
        out.writeString(this.packageName);
    }

    private PackageEvent(Parcel in) {
        this.type = in.readInt();
        this.packageUri = (Uri) Uri.CREATOR.createFromParcel(in);
        this.packageName = in.readString();
        checkConstructorArguments();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }
}
