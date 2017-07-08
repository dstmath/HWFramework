package tmsdkobf;

import android.content.Context;
import tmsdk.common.TMServiceFactory;
import tmsdk.common.creator.BaseManagerC;

/* compiled from: Unknown */
public final class qa extends BaseManagerC {
    private qb Je;
    private qd Jf;

    public py b(py pyVar, int i) {
        return !jg.cl() ? this.Je.b(pyVar, i) : new py();
    }

    public py i(String str, int i) {
        return !jg.cl() ? this.Je.i(str, i) : new py();
    }

    public void onCreate(Context context) {
        this.Je = new qb();
        this.Je.onCreate(context);
        a(this.Je);
        this.Jf = TMServiceFactory.getSystemInfoService();
    }
}
