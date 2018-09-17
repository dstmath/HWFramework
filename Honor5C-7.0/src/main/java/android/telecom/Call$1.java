package android.telecom;

import android.telecom.Call.Callback;

class Call$1 implements Runnable {
    final /* synthetic */ Call this$0;
    final /* synthetic */ Call val$call;
    final /* synthetic */ Callback val$callback;
    final /* synthetic */ int val$newState;

    Call$1(Call this$0, Callback val$callback, Call val$call, int val$newState) {
        this.this$0 = this$0;
        this.val$callback = val$callback;
        this.val$call = val$call;
        this.val$newState = val$newState;
    }

    public void run() {
        this.val$callback.onStateChanged(this.val$call, this.val$newState);
    }
}
