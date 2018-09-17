package android.telecom;

import android.telecom.Call.Callback;

class Call$9 implements Runnable {
    final /* synthetic */ Call this$0;
    final /* synthetic */ Call val$call;
    final /* synthetic */ Callback val$callback;

    Call$9(Call this$0, Callback val$callback, Call val$call) {
        this.this$0 = this$0;
        this.val$callback = val$callback;
        this.val$call = val$call;
    }

    public void run() {
        this.val$callback.onConferenceableCallsChanged(this.val$call, Call.-get2(this.this$0));
    }
}
