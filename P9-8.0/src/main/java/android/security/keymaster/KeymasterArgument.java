package android.security.keymaster;

import android.os.Parcel;
import android.os.ParcelFormatException;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

abstract class KeymasterArgument implements Parcelable {
    public static final Creator<KeymasterArgument> CREATOR = new Creator<KeymasterArgument>() {
        public KeymasterArgument createFromParcel(Parcel in) {
            int pos = in.dataPosition();
            int tag = in.readInt();
            switch (KeymasterDefs.getTagType(tag)) {
                case Integer.MIN_VALUE:
                case KeymasterDefs.KM_BYTES /*-1879048192*/:
                    return new KeymasterBlobArgument(tag, in);
                case KeymasterDefs.KM_ULONG_REP /*-1610612736*/:
                case KeymasterDefs.KM_ULONG /*1342177280*/:
                    return new KeymasterLongArgument(tag, in);
                case 268435456:
                case 536870912:
                case KeymasterDefs.KM_UINT /*805306368*/:
                case 1073741824:
                    return new KeymasterIntArgument(tag, in);
                case KeymasterDefs.KM_DATE /*1610612736*/:
                    return new KeymasterDateArgument(tag, in);
                case KeymasterDefs.KM_BOOL /*1879048192*/:
                    return new KeymasterBooleanArgument(tag, in);
                default:
                    throw new ParcelFormatException("Bad tag: " + tag + " at " + pos);
            }
        }

        public KeymasterArgument[] newArray(int size) {
            return new KeymasterArgument[size];
        }
    };
    public final int tag;

    public abstract void writeValue(Parcel parcel);

    protected KeymasterArgument(int tag) {
        this.tag = tag;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.tag);
        writeValue(out);
    }
}
