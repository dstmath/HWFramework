package android.os;

import android.content.ContentValues;
import android.util.Log;
import java.util.ArrayList;

public class JankBdItem {
    private static boolean HWFLOW = false;
    private static final int SECTIONNUM_MAX = 100;
    private static final String TAG = "JankShield";
    public String appname;
    public String casename;
    public int id;
    public String marks;
    public ArrayList<Integer> sectionCnts;
    public String timestamp;
    public int totaltime;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.os.JankBdItem.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.os.JankBdItem.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.os.JankBdItem.<clinit>():void");
    }

    public JankBdItem() {
        this.id = 0;
        this.sectionCnts = new ArrayList();
    }

    public int getId() {
        return this.id;
    }

    public boolean readFromParcel(Parcel src, String time, int ttime) {
        this.sectionCnts.clear();
        this.timestamp = time;
        this.totaltime = ttime;
        this.casename = src.readString();
        if (this.casename == null) {
            return false;
        }
        this.appname = src.readString();
        if (this.appname == null) {
            return false;
        }
        this.marks = src.readString();
        if (this.marks == null) {
            return false;
        }
        int nsectionnum = src.readInt();
        if (nsectionnum < 0 || nsectionnum > SECTIONNUM_MAX) {
            return false;
        }
        for (int i = 0; i < nsectionnum; i++) {
            this.sectionCnts.add(Integer.valueOf(src.readInt()));
        }
        return true;
    }

    public boolean isEmpty() {
        int nsectionnum = this.sectionCnts.size();
        for (int i = 0; i < nsectionnum; i++) {
            if (((Integer) this.sectionCnts.get(i)).intValue() != 0) {
                return false;
            }
        }
        return true;
    }

    public boolean add(JankBdItem b) {
        if (b.sectionCnts.size() > this.sectionCnts.size()) {
            if (HWFLOW) {
                Log.i(TAG, "a.size" + this.sectionCnts.size() + ", b.size" + b.sectionCnts.size());
            }
            return false;
        }
        this.totaltime += b.totaltime;
        int nsectionnum = b.sectionCnts.size();
        for (int i = 0; i < nsectionnum; i++) {
            this.sectionCnts.set(i, Integer.valueOf(((Integer) b.sectionCnts.get(i)).intValue() + ((Integer) this.sectionCnts.get(i)).intValue()));
        }
        return true;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.casename);
        dest.writeString(this.appname);
        dest.writeString(this.marks);
        int nsectionnum = this.sectionCnts.size();
        dest.writeInt(nsectionnum);
        for (int i = 0; i < nsectionnum; i++) {
            dest.writeInt(((Integer) this.sectionCnts.get(i)).intValue());
        }
    }

    public ContentValues getContentValues(String[] fieldnames) {
        if (fieldnames == null || fieldnames.length < 4) {
            return null;
        }
        ContentValues values = new ContentValues();
        values.put(fieldnames[0], this.casename);
        int index = 1 + 1;
        values.put(fieldnames[1], this.timestamp);
        int index2 = index + 1;
        values.put(fieldnames[index], this.appname);
        index = index2 + 1;
        values.put(fieldnames[index2], Integer.valueOf(this.totaltime));
        index2 = index + 1;
        values.put(fieldnames[index], this.marks);
        int i = 0;
        while (i < this.sectionCnts.size() && index2 < fieldnames.length) {
            index = index2 + 1;
            values.put(fieldnames[index2], (Integer) this.sectionCnts.get(i));
            i++;
            index2 = index;
        }
        return values;
    }
}
