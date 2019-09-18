package android.media.dtv;

import android.os.Parcel;
import android.os.Parcelable;

public class TunerTuneState implements Parcelable {
    public static final Parcelable.Creator<TunerTuneState> CREATOR = new Parcelable.Creator<TunerTuneState>() {
        public TunerTuneState createFromParcel(Parcel source) {
            return new TunerTuneState(source);
        }

        public TunerTuneState[] newArray(int size) {
            return new TunerTuneState[size];
        }
    };
    public static final String TAG = "TunerTuneState";
    private boolean mCs_Dmderr;
    private boolean mCs_Fatal;
    private boolean mCs_Frmlck;
    private boolean mCs_Mgerr;
    private boolean mCs_Ofdmerr;
    private boolean mCs_Sseqerr;
    private boolean mCs_Timeout;
    private boolean mCs_Tuned;
    private int mP;
    private int mTs_Error;
    private int mTs_Nosig;
    private boolean mTs_Sync;
    private boolean mTs_Tuned;

    private TunerTuneState(Parcel in) {
        boolean z = false;
        this.mCs_Sseqerr = in.readByte() == 1;
        this.mCs_Mgerr = in.readByte() == 1;
        this.mCs_Dmderr = in.readByte() == 1;
        this.mCs_Ofdmerr = in.readByte() == 1;
        this.mCs_Frmlck = in.readByte() == 1;
        this.mCs_Tuned = in.readByte() == 1;
        this.mCs_Timeout = in.readByte() == 1;
        this.mCs_Fatal = in.readByte() == 1;
        this.mTs_Sync = in.readByte() == 1;
        this.mTs_Tuned = in.readByte() == 1 ? true : z;
        this.mTs_Nosig = in.readInt();
        this.mTs_Error = in.readInt();
        this.mP = in.readInt();
    }

    public boolean ismCs_Sseqerr() {
        return this.mCs_Sseqerr;
    }

    public void setCs_Sseqerr(boolean cs_Sseqerr) {
        this.mCs_Sseqerr = cs_Sseqerr;
    }

    public boolean ismCs_Mgerr() {
        return this.mCs_Mgerr;
    }

    public void setCs_Mgerr(boolean cs_Mgerr) {
        this.mCs_Mgerr = cs_Mgerr;
    }

    public boolean ismCs_Dmderr() {
        return this.mCs_Dmderr;
    }

    public void setCs_Dmderr(boolean cs_Dmderr) {
        this.mCs_Dmderr = cs_Dmderr;
    }

    public boolean ismCs_Ofdmerr() {
        return this.mCs_Ofdmerr;
    }

    public void setCs_Ofdmerr(boolean cs_Ofdmerr) {
        this.mCs_Ofdmerr = cs_Ofdmerr;
    }

    public boolean ismCs_Frmlck() {
        return this.mCs_Frmlck;
    }

    public void setCs_Frmlck(boolean cs_Frmlck) {
        this.mCs_Frmlck = cs_Frmlck;
    }

    public boolean ismCs_Tuned() {
        return this.mCs_Tuned;
    }

    public void setCs_Tuned(boolean cs_Tuned) {
        this.mCs_Tuned = cs_Tuned;
    }

    public boolean ismCs_Timeout() {
        return this.mCs_Timeout;
    }

    public void setCs_Timeout(boolean cs_Timeout) {
        this.mCs_Timeout = cs_Timeout;
    }

    public boolean ismCs_Fatal() {
        return this.mCs_Fatal;
    }

    public void setCs_Fatal(boolean cs_Fatal) {
        this.mCs_Fatal = cs_Fatal;
    }

    public int getTs_Nosig() {
        return this.mTs_Nosig;
    }

    public void setTs_Nosig(int ts_Nosig) {
        this.mTs_Nosig = ts_Nosig;
    }

    public boolean ismTs_Sync() {
        return this.mTs_Sync;
    }

    public void setTs_Sync(boolean ts_Sync) {
        this.mTs_Sync = ts_Sync;
    }

    public boolean ismTs_Tuned() {
        return this.mTs_Tuned;
    }

    public void setTs_Tuned(boolean ts_Tuned) {
        this.mTs_Tuned = ts_Tuned;
    }

    public int getTs_Error() {
        return this.mTs_Error;
    }

    public void setTs_Error(int ts_Error) {
        this.mTs_Error = ts_Error;
    }

    public int getP() {
        return this.mP;
    }

    public void setP(int p) {
        this.mP = p;
    }

    public void setAll() {
        this.mCs_Sseqerr = true;
        this.mCs_Mgerr = true;
        this.mCs_Dmderr = true;
        this.mCs_Ofdmerr = true;
        this.mCs_Frmlck = true;
        this.mCs_Tuned = true;
        this.mCs_Timeout = true;
        this.mCs_Fatal = true;
        this.mTs_Nosig = 1;
        this.mTs_Sync = true;
        this.mTs_Tuned = true;
        this.mTs_Error = 1;
        this.mP = 1;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(this.mCs_Dmderr ? (byte) 1 : 0);
        dest.writeByte(this.mCs_Mgerr ? (byte) 1 : 0);
        dest.writeByte(this.mCs_Fatal ? (byte) 1 : 0);
        dest.writeByte(this.mCs_Frmlck ? (byte) 1 : 0);
        dest.writeByte(this.mCs_Ofdmerr ? (byte) 1 : 0);
        dest.writeByte(this.mCs_Sseqerr ? (byte) 1 : 0);
        dest.writeByte(this.mCs_Timeout ? (byte) 1 : 0);
        dest.writeByte(this.mCs_Tuned ? (byte) 1 : 0);
        dest.writeByte(this.mTs_Sync ? (byte) 1 : 0);
        dest.writeByte(this.mTs_Tuned ? (byte) 1 : 0);
        dest.writeInt(this.mTs_Error);
        dest.writeInt(this.mTs_Nosig);
        dest.writeInt(this.mP);
    }
}
