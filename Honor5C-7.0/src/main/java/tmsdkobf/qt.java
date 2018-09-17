package tmsdkobf;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import tmsdk.common.ErrorCode;
import tmsdk.common.creator.BaseManagerC;

/* compiled from: Unknown */
public final class qt extends BaseManagerC {
    private qu KC;

    public int a(String str, AtomicReference<et> atomicReference) {
        return this.KC.a(str, (AtomicReference) atomicReference);
    }

    public int a(List<eg> list, ArrayList<cp> arrayList, int i) {
        return this.KC.a((List) list, (ArrayList) arrayList, true, i);
    }

    public int a(List<String> list, AtomicReference<du> atomicReference) {
        return this.KC.a((List) list, (AtomicReference) atomicReference);
    }

    public int a(da daVar, AtomicReference<dh> atomicReference) {
        return this.KC.b(daVar, atomicReference);
    }

    public int a(dj djVar, di diVar) {
        return this.KC.a(djVar, diVar);
    }

    public int a(dr drVar) {
        return this.KC.a(drVar);
    }

    public int a(ew ewVar, AtomicReference<ez> atomicReference, ArrayList<ey> arrayList, int i) {
        try {
            return this.KC.a(ewVar, (AtomicReference) atomicReference, (ArrayList) arrayList, i);
        } catch (Exception e) {
            return ErrorCode.ERR_WUP;
        }
    }

    public int ib() {
        return this.KC.ib();
    }

    public qo ic() {
        return this.KC.ic();
    }

    public void onCreate(Context context) {
        this.KC = new qu();
        this.KC.onCreate(context);
        a(this.KC);
    }

    public int x(List<ed> list) {
        return this.KC.x(list);
    }

    public int y(List<em> list) {
        return this.KC.y(list);
    }
}
