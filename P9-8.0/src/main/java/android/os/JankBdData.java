package android.os;

import android.content.ContentValues;
import android.os.Parcelable.Creator;
import java.util.ArrayList;
import java.util.List;

public class JankBdData implements Parcelable {
    private static final int CASENUM_MAX = 10000;
    public static final Creator<JankBdData> CREATOR = new Creator<JankBdData>() {
        public JankBdData createFromParcel(Parcel in) {
            return new JankBdData(in, null);
        }

        public JankBdData[] newArray(int size) {
            return new JankBdData[size];
        }
    };
    private static final int TOTALTIME_MAX = 8640000;
    List<JankBdItem> jankbditems;
    public String timestamp;
    public int totaltime;

    /* synthetic */ JankBdData(Parcel in, JankBdData -this1) {
        this(in);
    }

    private JankBdData(Parcel in) {
        this.jankbditems = new ArrayList();
        this.timestamp = in.readString();
        if (this.timestamp != null) {
            this.totaltime = in.readInt();
            if (this.totaltime >= 0 && this.totaltime <= TOTALTIME_MAX) {
                int casenum = in.readInt();
                if (casenum >= 0 && casenum <= 10000) {
                    int i = 0;
                    while (i < casenum) {
                        JankBdItem item = new JankBdItem();
                        if (item.readFromParcel(in, this.timestamp, this.totaltime)) {
                            this.jankbditems.add(item);
                            i++;
                        } else {
                            this.jankbditems.clear();
                            return;
                        }
                    }
                }
            }
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        int casenum = this.jankbditems.size();
        dest.writeString(this.timestamp);
        dest.writeInt(this.totaltime);
        dest.writeInt(casenum);
        for (int i = 0; i < casenum; i++) {
            ((JankBdItem) this.jankbditems.get(i)).writeToParcel(dest, flags);
        }
    }

    public List<ContentValues> getContentValues(String[] fieldnames) {
        if (fieldnames == null || fieldnames.length != 14) {
            return null;
        }
        List<ContentValues> valuesList = new ArrayList();
        int size = this.jankbditems.size();
        for (int i = 0; i < size; i++) {
            valuesList.add(((JankBdItem) this.jankbditems.get(i)).getContentValues(fieldnames));
        }
        return valuesList;
    }

    public List<JankBdItem> getItems() {
        return this.jankbditems;
    }
}
