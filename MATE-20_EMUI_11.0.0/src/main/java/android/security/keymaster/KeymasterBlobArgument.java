package android.security.keymaster;

import android.annotation.UnsupportedAppUsage;
import android.os.Parcel;

/* access modifiers changed from: package-private */
public class KeymasterBlobArgument extends KeymasterArgument {
    @UnsupportedAppUsage
    public final byte[] blob;

    @UnsupportedAppUsage
    public KeymasterBlobArgument(int tag, byte[] blob2) {
        super(tag);
        int tagType = KeymasterDefs.getTagType(tag);
        if (tagType == Integer.MIN_VALUE || tagType == -1879048192) {
            this.blob = blob2;
            return;
        }
        throw new IllegalArgumentException("Bad blob tag " + tag);
    }

    @UnsupportedAppUsage
    public KeymasterBlobArgument(int tag, Parcel in) {
        super(tag);
        this.blob = in.createByteArray();
    }

    @Override // android.security.keymaster.KeymasterArgument
    public void writeValue(Parcel out) {
        out.writeByteArray(this.blob);
    }
}
