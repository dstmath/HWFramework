package tmsdkobf;

import android.content.Context;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import tmsdk.common.creator.BaseManagerC;

final class pr extends BaseManagerC {
    public static String TAG = "WupSessionManagerImpl";
    private po KA;
    private Context mContext;

    pr() {
    }

    public int b(ec ecVar, AtomicReference<eg> atomicReference) {
        pp bP = pm.bP(9);
        HashMap hashMap = new HashMap(3);
        hashMap.put("phonetype", this.KA.ht());
        hashMap.put("userinfo", this.KA.hu());
        hashMap.put("deviceinfo", ecVar);
        bP.Kv = hashMap;
        int a = this.KA.a(bP, true);
        if (a != 0) {
            return a;
        }
        Object a2 = this.KA.a(bP.Kx, "guidinfo", new eg());
        if (a2 != null) {
            atomicReference.set((eg) a2);
        }
        return 0;
    }

    public int getSingletonType() {
        return 1;
    }

    public pl hV() {
        return this.KA;
    }

    public void onCreate(Context context) {
        this.mContext = context;
        this.KA = new po(this.mContext);
    }

    public int u(List<es> list) {
        pp bP = pm.bP(12);
        HashMap hashMap = new HashMap(3);
        hashMap.put("phonetype", this.KA.hO());
        hashMap.put("userinfo", this.KA.hP());
        hashMap.put("vecSmsReport", list);
        bP.Kv = hashMap;
        int a = this.KA.a(bP);
        return a == 0 ? 0 : a;
    }
}
