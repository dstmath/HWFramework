package tmsdkobf;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

/* compiled from: Unknown */
public abstract class jh {
    private IBinder mBinder;

    public void e(Intent intent) {
    }

    public final boolean equals(Object obj) {
        return obj != null && getClass() == obj.getClass();
    }

    public synchronized IBinder getBinder() {
        if (this.mBinder == null) {
            this.mBinder = onBind();
        }
        return this.mBinder;
    }

    public abstract IBinder onBind();

    public void onCreate(Context context) {
    }

    public void onDestory() {
    }
}
