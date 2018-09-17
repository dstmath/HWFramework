package tmsdkobf;

import android.content.Context;
import android.os.HandlerThread;
import tmsdk.common.creator.BaseManagerC;

public class pb extends BaseManagerC implements pd {
    private ix Jj;

    public HandlerThread a(String str, int i, long j) {
        return this.Jj.a(str, i, j);
    }

    public Thread a(Runnable runnable, String str, long j) {
        return this.Jj.a(runnable, str, j);
    }

    public void a(int i, Runnable runnable, String str, long j) {
        this.Jj.a(i, runnable, str, j, false, null);
    }

    public void a(Runnable runnable) {
        this.Jj.a(runnable);
    }

    public void b(Runnable runnable, String str, long j) {
        this.Jj.a(runnable, str, j, false, null);
    }

    public void c(Runnable runnable, String str, long j) {
        this.Jj.b(runnable, str, j, false, null);
    }

    public void onCreate(Context context) {
        this.Jj = new ix();
        this.Jj.onCreate(context);
        a(this.Jj);
    }
}
