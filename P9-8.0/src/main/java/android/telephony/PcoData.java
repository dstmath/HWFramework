package android.telephony;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class PcoData implements Parcelable {
    public static final Creator<PcoData> CREATOR = new Creator() {
        public PcoData createFromParcel(Parcel in) {
            return new PcoData(in);
        }

        public PcoData[] newArray(int size) {
            return new PcoData[size];
        }
    };
    public final String bearerProto;
    public final int cid;
    public final byte[] contents;
    public final int pcoId;

    public PcoData(int cid, String bearerProto, int pcoId, byte[] contents) {
        this.cid = cid;
        this.bearerProto = bearerProto;
        this.pcoId = pcoId;
        this.contents = contents;
    }

    public PcoData(Parcel in) {
        this.cid = in.readInt();
        this.bearerProto = in.readString();
        this.pcoId = in.readInt();
        this.contents = in.createByteArray();
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.cid);
        out.writeString(this.bearerProto);
        out.writeInt(this.pcoId);
        out.writeByteArray(this.contents);
    }

    public int describeContents() {
        return 0;
    }

    public String toString() {
        return "PcoData(" + this.cid + ", " + this.bearerProto + ", " + this.pcoId + ", contents[" + this.contents.length + "])";
    }
}
