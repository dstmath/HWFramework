package tmsdk.common.module.update;

import android.content.Context;
import java.util.List;
import tmsdk.common.creator.BaseManagerC;
import tmsdkobf.kr;
import tmsdkobf.kt;

public final class UpdateManager extends BaseManagerC {
    private a JP;

    public void addObserver(long j, IUpdateObserver iUpdateObserver) {
        this.JP.addObserver(j, iUpdateObserver);
    }

    public void cancel() {
        this.JP.cancel();
    }

    public void check(long j, ICheckListener iCheckListener, long j2) {
        kt.f(29968, "" + j);
        this.JP.a(j, iCheckListener);
        kr.dz();
    }

    public String getFileSavePath() {
        return this.JP.getFileSavePath();
    }

    public void onCreate(Context context) {
        this.JP = new a();
        this.JP.onCreate(context);
        a(this.JP);
    }

    public void removeObserver(long j) {
        this.JP.removeObserver(j);
    }

    public boolean update(List<UpdateInfo> list, IUpdateListener iUpdateListener) {
        return this.JP.update(list, iUpdateListener);
    }
}
