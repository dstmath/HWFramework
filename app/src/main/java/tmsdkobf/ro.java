package tmsdkobf;

import android.text.format.DateFormat;
import com.tencent.qqimagecompare.QQImageFeatureHSV;
import java.util.ArrayList;

/* compiled from: Unknown */
public class ro {
    public ArrayList<a> mItemList;
    public long mTime;
    public String mTimeString;

    /* compiled from: Unknown */
    public static class a extends rq {
        public QQImageFeatureHSV Og;
        public boolean mSelected;

        public a(rq rqVar) {
            super(rqVar);
        }

        public static String C(long j) {
            return j != 0 ? DateFormat.format("yyyy-MM-dd", j).toString() : "";
        }
    }

    public ro() {
        this.mItemList = new ArrayList();
    }

    public ro(long j) {
        this.mItemList = new ArrayList();
        this.mTime = j;
        this.mTimeString = a.C(j);
    }

    public ro(ro roVar) {
        this.mTime = roVar.mTime;
        this.mTimeString = roVar.mTimeString;
        this.mItemList = new ArrayList();
    }
}
