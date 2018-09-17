package tmsdkobf;

import android.content.Context;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import tmsdk.common.creator.BaseManagerC;

public final class pq extends BaseManagerC {
    private pr Kz;

    public int a(ec ecVar, AtomicReference<eg> atomicReference) {
        return this.Kz.b(ecVar, atomicReference);
    }

    public pl hV() {
        return this.Kz.hV();
    }

    public void onCreate(Context context) {
        this.Kz = new pr();
        this.Kz.onCreate(context);
        a(this.Kz);
    }

    public int u(List<es> list) {
        return this.Kz.u(list);
    }
}
