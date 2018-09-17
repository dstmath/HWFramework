package tmsdk.fg.module.urlcheck;

import android.content.Context;
import tmsdk.fg.creator.BaseManagerF;
import tmsdkobf.jg;
import tmsdkobf.ma;

/* compiled from: Unknown */
public class UrlCheckManagerV3 extends BaseManagerF {
    private b OX;

    public boolean checkUrl(String str, int i, ICheckUrlCallbackV3 iCheckUrlCallbackV3) {
        if (jg.cl()) {
            return false;
        }
        ma.by(29956);
        return this.OX.a(str, i, iCheckUrlCallbackV3);
    }

    public void onCreate(Context context) {
        this.OX = new b();
        this.OX.onCreate(context);
        a(this.OX);
    }
}
