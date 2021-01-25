package android.security.keymaster;

import android.annotation.UnsupportedAppUsage;
import android.os.Parcel;
import java.util.Date;

/* access modifiers changed from: package-private */
public class KeymasterDateArgument extends KeymasterArgument {
    public final Date date;

    public KeymasterDateArgument(int tag, Date date2) {
        super(tag);
        if (KeymasterDefs.getTagType(tag) == 1610612736) {
            this.date = date2;
            return;
        }
        throw new IllegalArgumentException("Bad date tag " + tag);
    }

    @UnsupportedAppUsage
    public KeymasterDateArgument(int tag, Parcel in) {
        super(tag);
        this.date = new Date(in.readLong());
    }

    @Override // android.security.keymaster.KeymasterArgument
    public void writeValue(Parcel out) {
        out.writeLong(this.date.getTime());
    }
}
