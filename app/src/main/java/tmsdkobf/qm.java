package tmsdkobf;

import android.content.Context;
import tmsdk.common.creator.BaseManagerC;

/* compiled from: Unknown */
public final class qm extends BaseManagerC {
    private qn JL;

    public boolean a(Runnable runnable, long j) {
        return this.JL.a(runnable, j);
    }

    public void onCreate(Context context) {
        this.JL = new qn();
        this.JL.onCreate(context);
        a(this.JL);
    }
}
