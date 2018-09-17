package android.security.keymaster;

import android.os.Parcel;

class KeymasterBooleanArgument extends KeymasterArgument {
    public final boolean value;

    public KeymasterBooleanArgument(int tag) {
        super(tag);
        this.value = true;
        switch (KeymasterDefs.getTagType(tag)) {
            case KeymasterDefs.KM_BOOL /*1879048192*/:
            default:
                throw new IllegalArgumentException("Bad bool tag " + tag);
        }
    }

    public KeymasterBooleanArgument(int tag, Parcel in) {
        super(tag);
        this.value = true;
    }

    public void writeValue(Parcel out) {
    }
}
