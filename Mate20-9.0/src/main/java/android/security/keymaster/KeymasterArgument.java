package android.security.keymaster;

import android.os.Parcel;
import android.os.ParcelFormatException;
import android.os.Parcelable;

abstract class KeymasterArgument implements Parcelable {
    public static final Parcelable.Creator<KeymasterArgument> CREATOR = new Parcelable.Creator<KeymasterArgument>() {
        public KeymasterArgument createFromParcel(Parcel in) {
            int pos = in.dataPosition();
            int tag = in.readInt();
            int tagType = KeymasterDefs.getTagType(tag);
            if (tagType == Integer.MIN_VALUE || tagType == -1879048192) {
                return new KeymasterBlobArgument(tag, in);
            }
            if (tagType != -1610612736) {
                if (tagType == 268435456 || tagType == 536870912 || tagType == 805306368 || tagType == 1073741824) {
                    return new KeymasterIntArgument(tag, in);
                }
                if (tagType != 1342177280) {
                    if (tagType == 1610612736) {
                        return new KeymasterDateArgument(tag, in);
                    }
                    if (tagType == 1879048192) {
                        return new KeymasterBooleanArgument(tag, in);
                    }
                    throw new ParcelFormatException("Bad tag: " + tag + " at " + pos);
                }
            }
            return new KeymasterLongArgument(tag, in);
        }

        public KeymasterArgument[] newArray(int size) {
            return new KeymasterArgument[size];
        }
    };
    public final int tag;

    public abstract void writeValue(Parcel parcel);

    protected KeymasterArgument(int tag2) {
        this.tag = tag2;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.tag);
        writeValue(out);
    }
}
