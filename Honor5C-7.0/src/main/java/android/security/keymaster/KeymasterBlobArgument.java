package android.security.keymaster;

import android.os.Parcel;

class KeymasterBlobArgument extends KeymasterArgument {
    public final byte[] blob;

    public KeymasterBlobArgument(int tag, byte[] blob) {
        super(tag);
        switch (KeymasterDefs.getTagType(tag)) {
            case KeymasterDefs.KM_BIGNUM /*-2147483648*/:
            case KeymasterDefs.KM_BYTES /*-1879048192*/:
                this.blob = blob;
            default:
                throw new IllegalArgumentException("Bad blob tag " + tag);
        }
    }

    public KeymasterBlobArgument(int tag, Parcel in) {
        super(tag);
        this.blob = in.createByteArray();
    }

    public void writeValue(Parcel out) {
        out.writeByteArray(this.blob);
    }
}
