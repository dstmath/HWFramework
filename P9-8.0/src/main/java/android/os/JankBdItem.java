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
    public int id = 0;
    public String marks;
    public ArrayList<Integer> sectionCnts = new ArrayList();
    public String timestamp;
    public int totaltime;

    static {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false : true;
        HWFLOW = isLoggable;
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
        if (nsectionnum < 0 || nsectionnum > 100) {
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
        int size = this.sectionCnts.size();
        int i = 0;
        index = index2;
        while (i < size && index < fieldnames.length) {
            index2 = index + 1;
            values.put(fieldnames[index], (Integer) this.sectionCnts.get(i));
            i++;
            index = index2;
        }
        return values;
    }
}
