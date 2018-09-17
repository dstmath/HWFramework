package android.hardware.input;

import android.os.LocaleList;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class KeyboardLayout implements Parcelable, Comparable<KeyboardLayout> {
    public static final Creator<KeyboardLayout> CREATOR = new Creator<KeyboardLayout>() {
        public KeyboardLayout createFromParcel(Parcel source) {
            return new KeyboardLayout(source, null);
        }

        public KeyboardLayout[] newArray(int size) {
            return new KeyboardLayout[size];
        }
    };
    private final String mCollection;
    private final String mDescriptor;
    private final String mLabel;
    private final LocaleList mLocales;
    private final int mPriority;
    private final int mProductId;
    private final int mVendorId;

    /* synthetic */ KeyboardLayout(Parcel source, KeyboardLayout -this1) {
        this(source);
    }

    public KeyboardLayout(String descriptor, String label, String collection, int priority, LocaleList locales, int vid, int pid) {
        this.mDescriptor = descriptor;
        this.mLabel = label;
        this.mCollection = collection;
        this.mPriority = priority;
        this.mLocales = locales;
        this.mVendorId = vid;
        this.mProductId = pid;
    }

    private KeyboardLayout(Parcel source) {
        this.mDescriptor = source.readString();
        this.mLabel = source.readString();
        this.mCollection = source.readString();
        this.mPriority = source.readInt();
        this.mLocales = (LocaleList) LocaleList.CREATOR.createFromParcel(source);
        this.mVendorId = source.readInt();
        this.mProductId = source.readInt();
    }

    public String getDescriptor() {
        return this.mDescriptor;
    }

    public String getLabel() {
        return this.mLabel;
    }

    public String getCollection() {
        return this.mCollection;
    }

    public LocaleList getLocales() {
        return this.mLocales;
    }

    public int getVendorId() {
        return this.mVendorId;
    }

    public int getProductId() {
        return this.mProductId;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mDescriptor);
        dest.writeString(this.mLabel);
        dest.writeString(this.mCollection);
        dest.writeInt(this.mPriority);
        this.mLocales.writeToParcel(dest, 0);
        dest.writeInt(this.mVendorId);
        dest.writeInt(this.mProductId);
    }

    public int compareTo(KeyboardLayout another) {
        int result = Integer.compare(another.mPriority, this.mPriority);
        if (result == 0) {
            result = this.mLabel.compareToIgnoreCase(another.mLabel);
        }
        if (result == 0) {
            return this.mCollection.compareToIgnoreCase(another.mCollection);
        }
        return result;
    }

    public String toString() {
        if (this.mCollection.isEmpty()) {
            return this.mLabel;
        }
        return this.mLabel + " - " + this.mCollection;
    }
}
