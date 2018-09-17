package tmsdkobf;

import android.content.Context;
import tmsdk.common.creator.BaseManagerC;

public final class ot extends BaseManagerC {
    private ou IW;

    public int a(em emVar) {
        return this.IW.a(emVar);
    }

    public int hw() {
        return this.IW.hw();
    }

    public void onCreate(Context context) {
        this.IW = new ou();
        this.IW.onCreate(context);
        a(this.IW);
    }
}
