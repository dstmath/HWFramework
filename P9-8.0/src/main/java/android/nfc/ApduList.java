package android.nfc;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.ArrayList;
import java.util.List;

public class ApduList implements Parcelable {
    public static final Creator<ApduList> CREATOR = new Creator<ApduList>() {
        public ApduList createFromParcel(Parcel in) {
            return new ApduList(in, null);
        }

        public ApduList[] newArray(int size) {
            return new ApduList[size];
        }
    };
    private ArrayList<byte[]> commands;

    /* synthetic */ ApduList(Parcel in, ApduList -this1) {
        this(in);
    }

    public ApduList() {
        this.commands = new ArrayList();
    }

    public void add(byte[] command) {
        this.commands.add(command);
    }

    public List<byte[]> get() {
        return this.commands;
    }

    private ApduList(Parcel in) {
        this.commands = new ArrayList();
        int count = in.readInt();
        for (int i = 0; i < count; i++) {
            byte[] cmd = new byte[in.readInt()];
            in.readByteArray(cmd);
            this.commands.add(cmd);
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.commands.size());
        for (byte[] cmd : this.commands) {
            dest.writeInt(cmd.length);
            dest.writeByteArray(cmd);
        }
    }
}
