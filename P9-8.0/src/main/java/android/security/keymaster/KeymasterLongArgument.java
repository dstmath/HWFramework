package android.security.keymaster;

import android.os.Parcel;

class KeymasterLongArgument extends KeymasterArgument {
    public final long value;

    public KeymasterLongArgument(int tag, long value) {
        super(tag);
        switch (KeymasterDefs.getTagType(tag)) {
            case KeymasterDefs.KM_ULONG_REP /*-1610612736*/:
            case KeymasterDefs.KM_ULONG /*1342177280*/:
                this.value = value;
                return;
            default:
                throw new IllegalArgumentException("Bad long tag " + tag);
        }
    }

    public KeymasterLongArgument(int tag, Parcel in) {
        super(tag);
        this.value = in.readLong();
    }

    public void writeValue(Parcel out) {
        out.writeLong(this.value);
    }
}
