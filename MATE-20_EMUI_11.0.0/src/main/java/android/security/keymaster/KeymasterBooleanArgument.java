package android.security.keymaster;

import android.annotation.UnsupportedAppUsage;
import android.os.Parcel;

/* access modifiers changed from: package-private */
public class KeymasterBooleanArgument extends KeymasterArgument {
    public final boolean value = true;

    public KeymasterBooleanArgument(int tag) {
        super(tag);
        if (KeymasterDefs.getTagType(tag) != 1879048192) {
            throw new IllegalArgumentException("Bad bool tag " + tag);
        }
    }

    @UnsupportedAppUsage
    public KeymasterBooleanArgument(int tag, Parcel in) {
        super(tag);
    }

    @Override // android.security.keymaster.KeymasterArgument
    public void writeValue(Parcel out) {
    }
}
