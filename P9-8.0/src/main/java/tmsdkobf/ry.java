package tmsdkobf;

import android.text.format.DateFormat;
import com.tencent.qqimagecompare.QQImageFeatureHSV;
import java.util.ArrayList;

public class ry {
    public ArrayList<a> mItemList = new ArrayList();
    public long mTime;
    public String mTimeString;

    public static class a extends sa {
        public QQImageFeatureHSV Qd;
        public boolean mSelected;

        public a(sa saVar) {
            super(saVar);
        }

        public static String L(long j) {
            return j != 0 ? DateFormat.format("yyyy-MM-dd", j).toString() : "";
        }
    }

    public ry(long j) {
        this.mTime = j;
        this.mTimeString = a.L(j);
    }

    public ry(ry ryVar) {
        this.mTime = ryVar.mTime;
        this.mTimeString = ryVar.mTimeString;
    }
}
