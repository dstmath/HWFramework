package tmsdkobf;

import android.content.Context;
import tmsdk.common.creator.BaseManagerC;

/* compiled from: Unknown */
public final class qk extends BaseManagerC {
    private ql Jx;

    public qj c(qj qjVar) {
        return !jg.cl() ? this.Jx.c(qjVar) : null;
    }

    public void onCreate(Context context) {
        this.Jx = new ql();
        this.Jx.onCreate(context);
        a(this.Jx);
    }
}
