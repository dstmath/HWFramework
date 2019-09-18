package android.security.keymaster;

import android.os.Parcel;

class KeymasterLongArgument extends KeymasterArgument {
    public final long value;

    public KeymasterLongArgument(int tag, long value2) {
        super(tag);
        int tagType = KeymasterDefs.getTagType(tag);
        if (tagType == -1610612736 || tagType == 1342177280) {
            this.value = value2;
            return;
        }
        throw new IllegalArgumentException("Bad long tag " + tag);
    }

    public KeymasterLongArgument(int tag, Parcel in) {
        super(tag);
        this.value = in.readLong();
    }

    public void writeValue(Parcel out) {
        out.writeLong(this.value);
    }
}
