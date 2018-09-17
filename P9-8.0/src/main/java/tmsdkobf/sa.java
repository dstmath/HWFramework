package tmsdkobf;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class sa {
    public boolean Qn = false;
    public long mDbId;
    public boolean mIsOut = false;
    public boolean mIsScreenShot = false;
    public String mPath = "";
    public long mSize;
    public long mTime;

    public sa(long j, long j2, String str, long j3) {
        boolean z = false;
        this.mTime = j;
        this.mSize = j2;
        this.mPath = str;
        this.mIsScreenShot = rz.dA(this.mPath);
        if (!rz.dE(this.mPath)) {
            z = true;
        }
        this.mIsOut = z;
        this.Qn = rz.dF(rz.di(this.mPath));
        this.mDbId = j3;
    }

    public sa(sa saVar) {
        this.mTime = saVar.mTime;
        this.mSize = saVar.mSize;
        this.mPath = saVar.mPath;
        this.mIsScreenShot = saVar.mIsScreenShot;
        this.mIsOut = saVar.mIsOut;
        this.Qn = saVar.Qn;
        this.mDbId = saVar.mDbId;
    }

    public static void O(List<sa> list) {
        if (list != null && !list.isEmpty()) {
            Collections.sort(list, new Comparator<sa>() {
                /* renamed from: a */
                public int compare(sa saVar, sa saVar2) {
                    int i = 1;
                    int i2 = 0;
                    if (saVar.mTime == saVar2.mTime) {
                        return 0;
                    }
                    if (saVar.mTime >= saVar2.mTime) {
                        i2 = 1;
                    }
                    if (i2 == 0) {
                        i = -1;
                    }
                    return i;
                }
            });
        }
    }
}
