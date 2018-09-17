package android.telecom;

import android.telecom.Call.Callback;

class Call$7 implements Runnable {
    final /* synthetic */ Call this$0;
    final /* synthetic */ Call val$call;
    final /* synthetic */ Callback val$callback;
    final /* synthetic */ String val$remainingPostDialSequence;

    Call$7(Call this$0, Callback val$callback, Call val$call, String val$remainingPostDialSequence) {
        this.this$0 = this$0;
        this.val$callback = val$callback;
        this.val$call = val$call;
        this.val$remainingPostDialSequence = val$remainingPostDialSequence;
    }

    public void run() {
        this.val$callback.onPostDialWait(this.val$call, this.val$remainingPostDialSequence);
    }
}
