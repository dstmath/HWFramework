package tmsdkobf;

import android.content.Context;
import tmsdk.common.TMServiceFactory;
import tmsdk.common.creator.BaseManagerC;

public final class ox extends BaseManagerC {
    private oy Je;
    private pa Jf;

    public ov c(ov ovVar, int i) {
        return !ic.bE() ? this.Je.c(ovVar, i) : new ov();
    }

    public ov g(String str, int i) {
        return !ic.bE() ? this.Je.g(str, i) : new ov();
    }

    public void onCreate(Context context) {
        this.Je = new oy();
        this.Je.onCreate(context);
        a(this.Je);
        this.Jf = TMServiceFactory.getSystemInfoService();
    }
}
