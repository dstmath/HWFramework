package android.os;

import android.content.ContentValues;
import android.os.Parcelable.Creator;

public class JankEventData implements Parcelable {
    public static final Creator<JankEventData> CREATOR = new Creator<JankEventData>() {
        public JankEventData createFromParcel(Parcel in) {
            return new JankEventData(in, null);
        }

        public JankEventData[] newArray(int size) {
            return new JankEventData[size];
        }
    };
    public String CpuLoadTop_proc1;
    public String CpuLoadTop_proc2;
    public String CpuLoadTop_proc3;
    public int CpuLoad_proc1;
    public int CpuLoad_proc2;
    public int CpuLoad_proc3;
    public String arg1;
    public int arg2;
    public String casename;
    public int cpu_load;
    public int freemem;
    public int freestorage;
    public int limit_freq;
    public int mIoWaitLoad;
    public String timestamp;

    /* synthetic */ JankEventData(Parcel in, JankEventData -this1) {
        this(in);
    }

    private JankEventData(Parcel in) {
        this.casename = in.readString();
        this.timestamp = in.readString();
        this.arg1 = in.readString();
        this.arg2 = in.readInt();
        this.cpu_load = in.readInt();
        this.freemem = in.readInt();
        this.freestorage = in.readInt();
        this.limit_freq = in.readInt();
        this.CpuLoadTop_proc1 = in.readString();
        this.CpuLoadTop_proc2 = in.readString();
        this.CpuLoadTop_proc3 = in.readString();
        this.CpuLoad_proc1 = in.readInt();
        this.CpuLoad_proc2 = in.readInt();
        this.CpuLoad_proc3 = in.readInt();
        this.mIoWaitLoad = in.readInt();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.casename);
        dest.writeString(this.timestamp);
        dest.writeString(this.arg1);
        dest.writeInt(this.arg2);
        dest.writeInt(this.cpu_load);
        dest.writeInt(this.freemem);
        dest.writeInt(this.freestorage);
        dest.writeInt(this.limit_freq);
        dest.writeString(this.CpuLoadTop_proc1);
        dest.writeString(this.CpuLoadTop_proc2);
        dest.writeString(this.CpuLoadTop_proc3);
        dest.writeInt(this.CpuLoad_proc1);
        dest.writeInt(this.CpuLoad_proc2);
        dest.writeInt(this.CpuLoad_proc3);
        dest.writeInt(this.mIoWaitLoad);
    }

    public ContentValues getContentValues(String[] fieldnames) {
        if (fieldnames == null || fieldnames.length != 17) {
            return null;
        }
        ContentValues values = new ContentValues();
        values.put(fieldnames[0], this.casename);
        int index = 1 + 1;
        values.put(fieldnames[1], this.timestamp);
        int index2 = index + 1;
        values.put(fieldnames[index], this.arg1);
        index = index2 + 1;
        values.put(fieldnames[index2], Integer.valueOf(this.arg2));
        index2 = index + 1;
        values.put(fieldnames[index], Integer.valueOf(this.cpu_load));
        index = index2 + 1;
        values.put(fieldnames[index2], Integer.valueOf(this.freemem));
        index2 = index + 1;
        values.put(fieldnames[index], Integer.valueOf(this.freestorage));
        index = index2 + 1;
        values.put(fieldnames[index2], Integer.valueOf(this.limit_freq));
        index2 = index + 1;
        values.put(fieldnames[index], this.CpuLoadTop_proc1);
        index = index2 + 1;
        values.put(fieldnames[index2], this.CpuLoadTop_proc2);
        index2 = index + 1;
        values.put(fieldnames[index], this.CpuLoadTop_proc3);
        index = index2 + 1;
        values.put(fieldnames[index2], Integer.valueOf(this.CpuLoad_proc1));
        index2 = index + 1;
        values.put(fieldnames[index], Integer.valueOf(this.CpuLoad_proc2));
        index = index2 + 1;
        values.put(fieldnames[index2], Integer.valueOf(this.CpuLoad_proc3));
        index2 = index + 1;
        values.put(fieldnames[index], Integer.valueOf(this.mIoWaitLoad));
        return values;
    }
}
