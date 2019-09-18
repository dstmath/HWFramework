package android.security.keymaster;

import android.os.Parcel;

class KeymasterBlobArgument extends KeymasterArgument {
    public final byte[] blob;

    public KeymasterBlobArgument(int tag, byte[] blob2) {
        super(tag);
        int tagType = KeymasterDefs.getTagType(tag);
        if (tagType == Integer.MIN_VALUE || tagType == -1879048192) {
            this.blob = blob2;
            return;
        }
        throw new IllegalArgumentException("Bad blob tag " + tag);
    }

    public KeymasterBlobArgument(int tag, Parcel in) {
        super(tag);
        this.blob = in.createByteArray();
    }

    public void writeValue(Parcel out) {
        out.writeByteArray(this.blob);
    }
}
