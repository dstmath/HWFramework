package android.security.keymaster;

import android.os.Parcel;

class KeymasterBooleanArgument extends KeymasterArgument {
    public final boolean value = true;

    public KeymasterBooleanArgument(int tag) {
        super(tag);
        switch (KeymasterDefs.getTagType(tag)) {
            case KeymasterDefs.KM_BOOL /*1879048192*/:
                return;
            default:
                throw new IllegalArgumentException("Bad bool tag " + tag);
        }
    }

    public KeymasterBooleanArgument(int tag, Parcel in) {
        super(tag);
    }

    public void writeValue(Parcel out) {
    }
}
