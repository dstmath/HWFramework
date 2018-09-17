package tmsdkobf;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

public abstract class id {
    private IBinder mBinder;

    public void d(Intent intent) {
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
