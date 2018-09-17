package defpackage;

import android.content.Context;
import java.util.HashMap;

/* renamed from: bm */
final class bm implements Runnable {
    final /* synthetic */ al bX;
    final /* synthetic */ int bY;
    final /* synthetic */ HashMap bZ;
    final /* synthetic */ Context val$context;

    bm(Context context, al alVar, int i, HashMap hashMap) {
        this.val$context = context;
        this.bX = alVar;
        this.bY = i;
        this.bZ = hashMap;
    }

    public void run() {
        bl.a(this.val$context, this.bX, this.bY, this.bZ);
    }
}
