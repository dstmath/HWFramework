package android.telecom;

import android.telecom.Call.Callback;

class Call$2 implements Runnable {
    final /* synthetic */ Call this$0;
    final /* synthetic */ Call val$call;
    final /* synthetic */ Callback val$callback;
    final /* synthetic */ Call val$newParent;

    Call$2(Call this$0, Callback val$callback, Call val$call, Call val$newParent) {
        this.this$0 = this$0;
        this.val$callback = val$callback;
        this.val$call = val$call;
        this.val$newParent = val$newParent;
    }

    public void run() {
        this.val$callback.onParentChanged(this.val$call, this.val$newParent);
    }
}
