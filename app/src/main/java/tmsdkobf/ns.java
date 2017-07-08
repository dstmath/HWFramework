package tmsdkobf;

import android.content.Context;
import tmsdk.common.creator.BaseManagerC;

/* compiled from: Unknown */
public final class ns extends BaseManagerC {
    private nt Db;
    private nr Dc;

    public synchronized np fv() {
        np fv = this.Db.fv();
        if (jg.cl()) {
            if (this.Dc == null) {
                this.Dc = new nr(fv);
            }
            return this.Dc;
        }
        return this.Db.fv();
    }

    public void onCreate(Context context) {
        this.Db = new nt();
        this.Db.onCreate(context);
        a(this.Db);
    }
}
