package android.os;

import android.content.ContentValues;
import android.os.Parcelable.Creator;
import java.util.ArrayList;
import java.util.List;

public class JankBdData implements Parcelable {
    private static final int CASENUM_MAX = 10000;
    public static final Creator<JankBdData> CREATOR = null;
    private static final int TOTALTIME_MAX = 8640000;
    List<JankBdItem> jankbditems;
    public String timestamp;
    public int totaltime;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.os.JankBdData.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.os.JankBdData.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.os.JankBdData.<clinit>():void");
    }

    private JankBdData(Parcel in) {
        this.jankbditems = new ArrayList();
        this.timestamp = in.readString();
        if (this.timestamp != null) {
            this.totaltime = in.readInt();
            if (this.totaltime >= 0 && this.totaltime <= TOTALTIME_MAX) {
                int casenum = in.readInt();
                if (casenum >= 0 && casenum <= CASENUM_MAX) {
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
        for (int i = 0; i < this.jankbditems.size(); i++) {
            valuesList.add(((JankBdItem) this.jankbditems.get(i)).getContentValues(fieldnames));
        }
        return valuesList;
    }

    public List<JankBdItem> getItems() {
        return this.jankbditems;
    }
}
