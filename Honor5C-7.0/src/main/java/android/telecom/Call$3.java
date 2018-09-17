package android.telecom;

import android.telecom.Call.Callback;
import java.util.List;

class Call$3 implements Runnable {
    final /* synthetic */ Call this$0;
    final /* synthetic */ Call val$call;
    final /* synthetic */ Callback val$callback;
    final /* synthetic */ List val$children;

    Call$3(Call this$0, Callback val$callback, Call val$call, List val$children) {
        this.this$0 = this$0;
        this.val$callback = val$callback;
        this.val$call = val$call;
        this.val$children = val$children;
    }

    public void run() {
        this.val$callback.onChildrenChanged(this.val$call, this.val$children);
    }
}
