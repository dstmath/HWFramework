package android.os;

import android.content.ContentValues;
import android.os.Parcelable.Creator;

public class JankEventData implements Parcelable {
    public static final Creator<JankEventData> CREATOR = null;
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.os.JankEventData.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.os.JankEventData.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.os.JankEventData.<clinit>():void");
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
