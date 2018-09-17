package tmsdkobf;

import android.content.Context;
import tmsdk.common.creator.BaseManagerC;

public final class pj extends BaseManagerC {
    private pk JL;

    public boolean a(Runnable runnable, long j) {
        return this.JL.a(runnable, j);
    }

    public void onCreate(Context context) {
        this.JL = new pk();
        this.JL.onCreate(context);
        a(this.JL);
    }
}
