package android.telecom;

import android.telecom.Call.Callback;
import android.telecom.Call.Details;

class Call$4 implements Runnable {
    final /* synthetic */ Call this$0;
    final /* synthetic */ Call val$call;
    final /* synthetic */ Callback val$callback;
    final /* synthetic */ Details val$details;

    Call$4(Call this$0, Callback val$callback, Call val$call, Details val$details) {
        this.this$0 = this$0;
        this.val$callback = val$callback;
        this.val$call = val$call;
        this.val$details = val$details;
    }

    public void run() {
        this.val$callback.onDetailsChanged(this.val$call, this.val$details);
    }
}
