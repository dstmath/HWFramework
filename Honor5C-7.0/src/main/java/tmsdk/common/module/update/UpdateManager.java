package tmsdk.common.module.update;

import android.content.Context;
import java.util.List;
import tmsdk.common.creator.BaseManagerC;
import tmsdkobf.ly;
import tmsdkobf.ma;

/* compiled from: Unknown */
public final class UpdateManager extends BaseManagerC {
    private a JN;

    public void addObserver(long j, IUpdateObserver iUpdateObserver) {
        this.JN.addObserver(j, iUpdateObserver);
    }

    public void cancel() {
        this.JN.cancel();
    }

    public void check(long j, ICheckListener iCheckListener) {
        ma.d(29968, "" + j);
        this.JN.check(j, iCheckListener);
        ly.ep();
    }

    public String getFileSavePath() {
        return this.JN.getFileSavePath();
    }

    public void onCreate(Context context) {
        this.JN = new a();
        this.JN.onCreate(context);
        a(this.JN);
    }

    public void removeObserver(long j) {
        this.JN.removeObserver(j);
    }

    public boolean update(List<UpdateInfo> list, IUpdateListener iUpdateListener) {
        ly.eq();
        return this.JN.update(list, iUpdateListener);
    }
}
