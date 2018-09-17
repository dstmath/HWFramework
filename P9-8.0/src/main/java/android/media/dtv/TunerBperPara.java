package android.media.dtv;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class TunerBperPara implements Parcelable {
    public static final byte BPERMODE_BIT_NUM = (byte) 2;
    public static final byte BPERMODE_PACKET_NUM = (byte) 0;
    public static final byte BPERMODE_TIME_LENGTH = (byte) 1;
    public static final Creator<TunerBperPara> CREATOR = new Creator<TunerBperPara>() {
        public TunerBperPara createFromParcel(Parcel source) {
            return new TunerBperPara(source, null);
        }

        public TunerBperPara[] newArray(int size) {
            return new TunerBperPara[size];
        }
    };
    public static final String TAG = "TunerBperPara";
    private byte mBit_Berdlen;
    private int mBit_Dlen;
    private byte mBperMode;
    private byte mPkt_Bervlen0;
    private byte mPkt_Bervlen1;
    private byte mPkt_Bervlen2;
    private int mPkt_Vlen0;
    private int mPkt_Vlen1;
    private int mPkt_Vlen2;
    private byte mTime_Bertim0;
    private byte mTime_Bertim1;
    private int mTime_Tlen0;
    private int mTime_Tlen1;

    /* synthetic */ TunerBperPara(Parcel in, TunerBperPara -this1) {
        this(in);
    }

    public byte getBperMode() {
        return this.mBperMode;
    }

    public void setBperMode(byte bperMode) {
        this.mBperMode = bperMode;
    }

    public int getPkt_Vlen0() {
        return this.mPkt_Vlen0;
    }

    public void setPkt_Vlen0(int pkt_Vlen0) {
        this.mPkt_Vlen0 = pkt_Vlen0;
    }

    public int getPkt_Vlen1() {
        return this.mPkt_Vlen1;
    }

    public void setPkt_Vlen1(int pkt_Vlen1) {
        this.mPkt_Vlen1 = pkt_Vlen1;
    }

    public int getPkt_Vlen2() {
        return this.mPkt_Vlen2;
    }

    public void setPkt_Vlen2(int pkt_Vlen2) {
        this.mPkt_Vlen2 = pkt_Vlen2;
    }

    public byte getPkt_Bervlen0() {
        return this.mPkt_Bervlen0;
    }

    public void setPkt_Bervlen0(byte pkt_Bervlen0) {
        this.mPkt_Bervlen0 = pkt_Bervlen0;
    }

    public byte getPkt_Bervlen1() {
        return this.mPkt_Bervlen1;
    }

    public void setPkt_Bervlen1(byte pkt_Bervlen1) {
        this.mPkt_Bervlen1 = pkt_Bervlen1;
    }

    public byte getPkt_Bervlen2() {
        return this.mPkt_Bervlen2;
    }

    public void setPkt_Bervlen2(byte pkt_Bervlen2) {
        this.mPkt_Bervlen2 = pkt_Bervlen2;
    }

    public int getTime_Tlen0() {
        return this.mTime_Tlen0;
    }

    public void setTime_Tlen0(int time_Tlen0) {
        this.mTime_Tlen0 = time_Tlen0;
    }

    public int getTime_Tlen1() {
        return this.mTime_Tlen1;
    }

    public void setTime_Tlen1(int time_Tlen1) {
        this.mTime_Tlen1 = time_Tlen1;
    }

    public byte getTime_Bertim0() {
        return this.mTime_Bertim0;
    }

    public void setTime_Bertim0(byte time_Bertim0) {
        this.mTime_Bertim0 = time_Bertim0;
    }

    public byte getTime_Bertim1() {
        return this.mTime_Bertim1;
    }

    public void setTime_Bertim1(byte time_Bertim1) {
        this.mTime_Bertim1 = time_Bertim1;
    }

    public int getBit_Dlen() {
        return this.mBit_Dlen;
    }

    public void setBit_Dlen(int bit_Dlen) {
        this.mBit_Dlen = bit_Dlen;
    }

    public byte getBit_Berdlen() {
        return this.mBit_Berdlen;
    }

    public void setBit_Berdlen(byte bit_Berdlen) {
        this.mBit_Berdlen = bit_Berdlen;
    }

    private TunerBperPara(Parcel in) {
        readFromParcel(in);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(this.mBperMode);
        if (this.mBperMode == (byte) 0) {
            dest.writeInt(this.mPkt_Vlen0);
            dest.writeInt(this.mPkt_Vlen1);
            dest.writeInt(this.mPkt_Vlen2);
            dest.writeByte(this.mPkt_Bervlen0);
            dest.writeByte(this.mPkt_Bervlen1);
            dest.writeByte(this.mPkt_Bervlen2);
        } else if ((byte) 1 == this.mBperMode) {
            dest.writeInt(this.mTime_Tlen0);
            dest.writeInt(this.mTime_Tlen1);
            dest.writeByte(this.mTime_Bertim0);
            dest.writeByte(this.mTime_Bertim1);
        } else if ((byte) 2 == this.mBperMode) {
            dest.writeByte(this.mBit_Berdlen);
            dest.writeInt(this.mBit_Dlen);
        }
    }

    public void readFromParcel(Parcel source) {
        this.mBperMode = source.readByte();
        if (this.mBperMode == (byte) 0) {
            this.mPkt_Vlen0 = source.readInt();
            this.mPkt_Vlen1 = source.readInt();
            this.mPkt_Vlen2 = source.readInt();
            this.mPkt_Bervlen0 = source.readByte();
            this.mPkt_Bervlen1 = source.readByte();
            this.mPkt_Bervlen2 = source.readByte();
        } else if ((byte) 1 == this.mBperMode) {
            this.mTime_Tlen0 = source.readInt();
            this.mTime_Tlen1 = source.readInt();
            this.mTime_Bertim0 = source.readByte();
            this.mTime_Bertim1 = source.readByte();
        } else if ((byte) 2 == this.mBperMode) {
            this.mBit_Berdlen = source.readByte();
            this.mBit_Dlen = source.readInt();
        }
    }
}
