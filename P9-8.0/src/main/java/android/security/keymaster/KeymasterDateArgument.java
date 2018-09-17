package android.security.keymaster;

import android.os.Parcel;
import java.util.Date;

class KeymasterDateArgument extends KeymasterArgument {
    public final Date date;

    public KeymasterDateArgument(int tag, Date date) {
        super(tag);
        switch (KeymasterDefs.getTagType(tag)) {
            case KeymasterDefs.KM_DATE /*1610612736*/:
                this.date = date;
                return;
            default:
                throw new IllegalArgumentException("Bad date tag " + tag);
        }
    }

    public KeymasterDateArgument(int tag, Parcel in) {
        super(tag);
        this.date = new Date(in.readLong());
    }

    public void writeValue(Parcel out) {
        out.writeLong(this.date.getTime());
    }
}
