package android.telecom;

import android.os.Bundle;
import android.telecom.Call.Callback;

class Call$10 implements Runnable {
    final /* synthetic */ Call this$0;
    final /* synthetic */ Call val$call;
    final /* synthetic */ Callback val$callback;
    final /* synthetic */ String val$event;
    final /* synthetic */ Bundle val$extras;

    Call$10(Call this$0, Callback val$callback, Call val$call, String val$event, Bundle val$extras) {
        this.this$0 = this$0;
        this.val$callback = val$callback;
        this.val$call = val$call;
        this.val$event = val$event;
        this.val$extras = val$extras;
    }

    public void run() {
        this.val$callback.onConnectionEvent(this.val$call, this.val$event, this.val$extras);
    }
}
