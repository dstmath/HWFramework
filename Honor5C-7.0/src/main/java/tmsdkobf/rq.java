package tmsdkobf;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/* compiled from: Unknown */
public class rq {
    public boolean Oq;
    public long mDbId;
    public boolean mIsOut;
    public boolean mIsScreenShot;
    public String mPath;
    public long mSize;
    public long mTime;

    public rq(long j, long j2, String str, long j3) {
        boolean z = false;
        this.mPath = "";
        this.mIsOut = false;
        this.mIsScreenShot = false;
        this.Oq = false;
        this.mTime = j;
        this.mSize = j2;
        this.mPath = str;
        this.mIsScreenShot = rp.dE(this.mPath);
        if (!rp.dI(this.mPath)) {
            z = true;
        }
        this.mIsOut = z;
        this.Oq = rp.dJ(rp.aN(this.mPath));
        this.mDbId = j3;
    }

    public rq(rq rqVar) {
        this.mPath = "";
        this.mIsOut = false;
        this.mIsScreenShot = false;
        this.Oq = false;
        this.mTime = rqVar.mTime;
        this.mSize = rqVar.mSize;
        this.mPath = rqVar.mPath;
        this.mIsScreenShot = rqVar.mIsScreenShot;
        this.mIsOut = rqVar.mIsOut;
        this.Oq = rqVar.Oq;
        this.mDbId = rqVar.mDbId;
    }

    public static void I(List<rq> list) {
        if (list != null && !list.isEmpty()) {
            Collections.sort(list, new Comparator<rq>() {
                public int a(rq rqVar, rq rqVar2) {
                    int i = 1;
                    int i2 = 0;
                    if (rqVar.mTime == rqVar2.mTime) {
                        return 0;
                    }
                    if (rqVar.mTime >= rqVar2.mTime) {
                        i2 = 1;
                    }
                    if (i2 == 0) {
                        i = -1;
                    }
                    return i;
                }

                public /* synthetic */ int compare(Object obj, Object obj2) {
                    return a((rq) obj, (rq) obj2);
                }
            });
        }
    }
}
